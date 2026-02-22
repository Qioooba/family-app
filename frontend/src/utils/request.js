/**
 * 优化的请求封装
 * 支持重试、缓存、统一错误处理
 */

import { cacheManager } from './cache.js'

// 基础配置
const CONFIG = {
  BASE_URL: 'http://localhost:8080',
  USER_SERVICE_URL: 'http://localhost:8081',
  TIMEOUT: 30000,
  RETRY_TIMES: 2,
  RETRY_DELAY: 1000
}

// H5开发模式下使用代理（修复手机访问问题）
const isH5 = typeof window !== 'undefined' && window.location
const isDev = isH5 && (window.location.hostname === 'localhost' || window.location.hostname === '192.168.1.209')
if (isH5 && isDev) {
  CONFIG.BASE_URL = ''
  CONFIG.USER_SERVICE_URL = ''
}

// 请求队列（用于防止重复请求）
const pendingRequests = new Map()

/**
 * 判断是否是用户相关API
 */
const isUserApi = (url) => {
  return url.startsWith('/user/') || url === '/user/login' || url === '/user/register'
}

/**
 * 获取基础URL
 */
const getBaseUrl = (url) => {
  return isUserApi(url) ? CONFIG.USER_SERVICE_URL : CONFIG.BASE_URL
}

/**
 * 生成请求唯一标识
 */
const generateRequestKey = (options) => {
  return `${options.method || 'GET'}_${options.url}_${JSON.stringify(options.data || {})}`
}

/**
 * 延迟函数
 */
const delay = (ms) => new Promise(resolve => setTimeout(resolve, ms))

/**
 * 核心请求函数
 */
const baseRequest = async (options, retryCount = 0) => {
  const token = uni.getStorageSync('token')
  const baseUrl = getBaseUrl(options.url)
  
  return new Promise((resolve, reject) => {
    const requestTask = uni.request({
      url: baseUrl + options.url,
      method: options.method || 'GET',
      data: options.data || {},
      timeout: options.timeout || CONFIG.TIMEOUT,
      header: {
        'Content-Type': 'application/json',
        'Authorization': token || '',
        'X-Request-Id': generateRequestId(),
        ...(options.headers || {})
      },
      success: async (res) => {
        // 处理响应
        const result = handleResponse(res, options)
        if (result.success) {
          resolve(result.data)
        } else if (result.retryable && retryCount < CONFIG.RETRY_TIMES) {
          // 可重试的错误
          console.warn(`请求失败，${CONFIG.RETRY_DELAY}ms后重试(${retryCount + 1}/${CONFIG.RETRY_TIMES})`)
          await delay(CONFIG.RETRY_DELAY * (retryCount + 1))
          resolve(baseRequest(options, retryCount + 1))
        } else {
          reject(result.error)
        }
      },
      fail: async (err) => {
        // 网络错误，尝试重试
        if (retryCount < CONFIG.RETRY_TIMES) {
          console.warn(`网络错误，${CONFIG.RETRY_DELAY}ms后重试(${retryCount + 1}/${CONFIG.RETRY_TIMES})`)
          await delay(CONFIG.RETRY_DELAY * (retryCount + 1))
          resolve(baseRequest(options, retryCount + 1))
        } else {
          handleNetworkError(err, options)
          reject(err)
        }
      }
    })
    
    // 支持取消请求
    if (options.cancelToken) {
      options.cancelToken.requestTask = requestTask
    }
  })
}

/**
 * 生成请求ID
 */
const generateRequestId = () => {
  return `${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
}

/**
 * 处理响应
 */
const handleResponse = (res, options) => {
  const { statusCode, data } = res
  
  // HTTP错误
  if (statusCode < 200 || statusCode >= 300) {
    return {
      success: false,
      retryable: statusCode >= 500, // 服务器错误可重试
      error: {
        code: statusCode,
        message: getHttpErrorMessage(statusCode),
        data: data
      }
    }
  }
  
  // 业务错误
  if (data.code !== 200) {
    handleBusinessError(data, options)
    return {
      success: false,
      retryable: false,
      error: {
        code: data.code,
        message: data.message || '请求失败',
        data: data.data
      }
    }
  }
  
  return {
    success: true,
    data: data.data
  }
}

/**
 * 处理业务错误
 */
const handleBusinessError = (data, options) => {
  const { code, message } = data
  
  switch (code) {
    case 401:
      // 未授权，清除token并跳转登录
      uni.removeStorageSync('token')
      uni.removeStorageSync('userInfo')
      cacheManager.clear()
      
      // 延迟跳转，避免多个401同时触发
      setTimeout(() => {
        uni.navigateTo({ 
          url: '/pages/login/index',
          fail: () => {
            uni.reLaunch({ url: '/pages/login/index' })
          }
        })
      }, 100)
      break
      
    case 403:
      uni.showToast({ title: '没有权限', icon: 'none' })
      break
      
    case 404:
      if (!options.silent) {
        uni.showToast({ title: '资源不存在', icon: 'none' })
      }
      break
      
    case 429:
      uni.showToast({ title: '请求过于频繁，请稍后重试', icon: 'none' })
      break
      
    case 500:
    case 502:
    case 503:
    case 504:
      if (!options.silent) {
        uni.showToast({ title: '服务器繁忙，请稍后重试', icon: 'none' })
      }
      break
      
    default:
      if (!options.silent && !options.noErrorToast) {
        uni.showToast({ 
          title: message || '请求失败', 
          icon: 'none',
          duration: 2000
        })
      }
  }
}

/**
 * 处理网络错误
 */
const handleNetworkError = (err, options) => {
  if (!options.silent) {
    uni.showToast({ 
      title: '网络连接失败，请检查网络', 
      icon: 'none',
      duration: 2000
    })
  }
}

/**
 * 获取HTTP错误信息
 */
const getHttpErrorMessage = (statusCode) => {
  const messages = {
    400: '请求参数错误',
    401: '未授权，请重新登录',
    403: '没有权限访问',
    404: '资源不存在',
    405: '请求方法不允许',
    408: '请求超时',
    413: '请求体过大',
    429: '请求过于频繁',
    500: '服务器内部错误',
    502: '网关错误',
    503: '服务不可用',
    504: '网关超时'
  }
  return messages[statusCode] || `HTTP错误: ${statusCode}`
}

/**
 * 主请求函数
 */
const request = async (options) => {
  // 检查是否需要缓存
  if (options.cache && options.method === 'GET') {
    const cacheKey = options.cacheKey || generateRequestKey(options)
    const cached = cacheManager.get(cacheKey)
    if (cached) {
      return Promise.resolve(cached)
    }
  }
  
  // 检查是否有进行中的相同请求
  const requestKey = generateRequestKey(options)
  if (pendingRequests.has(requestKey) && !options.allowDuplicate) {
    return pendingRequests.get(requestKey)
  }
  
  // 执行请求
  const promise = baseRequest(options).then(data => {
    // 缓存GET请求结果
    if (options.cache && options.method === 'GET') {
      const cacheKey = options.cacheKey || requestKey
      cacheManager.set(cacheKey, data, {
        ttl: options.cacheTTL || 5 * 60 * 1000
      })
    }
    pendingRequests.delete(requestKey)
    return data
  }).catch(err => {
    pendingRequests.delete(requestKey)
    throw err
  })
  
  pendingRequests.set(requestKey, promise)
  return promise
}

/**
 * 取消令牌（用于取消请求）
 */
export class CancelToken {
  constructor() {
    this.requestTask = null
  }
  
  cancel(message = '请求已取消') {
    if (this.requestTask) {
      this.requestTask.abort()
      this.requestTask = null
    }
  }
}

/**
 * 上传文件
 */
request.upload = (url, filePath, options = {}) => {
  const token = uni.getStorageSync('token')
  const baseUrl = getBaseUrl(url)
  
  return new Promise((resolve, reject) => {
    uni.uploadFile({
      url: baseUrl + url,
      filePath,
      name: options.name || 'file',
      formData: options.formData || {},
      header: {
        'Authorization': token || ''
      },
      success: (res) => {
        try {
          const data = JSON.parse(res.data)
          if (data.code === 200) {
            resolve(data.data)
          } else {
            handleBusinessError(data, options)
            reject(data)
          }
        } catch (e) {
          resolve(res.data)
        }
      },
      fail: reject
    })
  })
}

/**
 * 下载文件
 */
request.download = (url, options = {}) => {
  const baseUrl = getBaseUrl(url)
  
  return new Promise((resolve, reject) => {
    uni.downloadFile({
      url: baseUrl + url,
      success: (res) => {
        if (res.statusCode === 200) {
          resolve(res.tempFilePath)
        } else {
          reject(new Error(`下载失败: ${res.statusCode}`))
        }
      },
      fail: reject
    })
  })
}

// 快捷方法
request.get = (url, params = {}, options = {}) => request({ url, method: 'GET', data: params, ...options })
request.post = (url, data = {}, options = {}) => request({ url, method: 'POST', data, ...options })
request.put = (url, data = {}, options = {}) => request({ url, method: 'PUT', data, ...options })
request.delete = (url, params = {}, options = {}) => request({ url, method: 'DELETE', data: params, ...options })

/**
 * 带缓存的GET请求
 */
request.getWithCache = (url, params = {}, cacheOptions = {}) => {
  return request({
    url,
    method: 'GET',
    data: params,
    cache: true,
    cacheTTL: cacheOptions.ttl || 5 * 60 * 1000,
    cacheKey: cacheOptions.key,
    ...cacheOptions
  })
}

/**
 * 清除指定URL的缓存
 */
request.clearCache = (url, params = {}) => {
  const cacheKey = generateRequestKey({ url, method: 'GET', data: params })
  cacheManager.delete(cacheKey)
}

/**
 * 批量请求
 */
request.all = (requests) => Promise.all(requests)

/**
 * 设置全局配置
 */
request.setConfig = (config) => {
  Object.assign(CONFIG, config)
}

export { request }
