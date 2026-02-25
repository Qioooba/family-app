/**
 * 优化的请求封装
 * 支持重试、缓存、统一错误处理
 */

import { cacheManager } from './cache.js'

// 基础配置
const CONFIG = {
  BASE_URL: 'http://localhost:8092',
  USER_SERVICE_URL: 'http://localhost:8092',
  TIMEOUT: 30000,
  RETRY_TIMES: 2,
  RETRY_DELAY: 1000
}

// H5开发模式下使用代理（修复手机访问问题）
const isH5 = typeof window !== 'undefined' && window.location
const isDev = isH5 && (window.location.hostname === 'localhost' || window.location.hostname === '192.168.1.209')

// 外网访问时使用代理模式（H5 环境都走代理）
const useProxy = isH5 || (window && window.location && window.location.hostname !== 'localhost' && window.location.hostname !== '127.0.0.1')

if (isH5 && (isDev || useProxy)) {
  CONFIG.BASE_URL = ''
  CONFIG.USER_SERVICE_URL = ''
}

// 请求队列（用于防止重复请求）
const pendingRequests = new Map()

// 全局跳转锁，防止重复跳转
let isNavigatingToLogin = false

// 获取跳转锁状态（兼容H5和小程序）
const getNavigatingLock = () => {
  try {
    const lockTime = uni.getStorageSync('__navigatingToLogin')
    if (!lockTime) return isNavigatingToLogin
    // 锁有效期5秒，超过则认为已释放
    return isNavigatingToLogin || (Date.now() - lockTime) < 5000
  } catch (e) {
    return isNavigatingToLogin
  }
}

// 设置跳转锁状态
const setNavigatingLock = (value) => {
  isNavigatingToLogin = value
  try {
    if (value) {
      uni.setStorageSync('__navigatingToLogin', Date.now())
    } else {
      uni.removeStorageSync('__navigatingToLogin')
    }
  } catch (e) {
    console.error('设置跳转锁失败:', e)
  }
}

/**
 * 判断是否是用户相关API
 */
const isUserApi = (url) => {
  return url.startsWith('/api/user/') || url.startsWith('/user/') || url === '/user/login' || url === '/user/register'
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

// 简化的 token 获取方式
const getToken = () => {
  const token = uni.getStorageSync('token') || ''
  console.log('[Token] 读取token:', token ? token.substring(0, 20) + '...' : '空')
  return token
}

/**
 * 核心请求函数
 */
const baseRequest = async (options, retryCount = 0) => {
  const token = getToken()
  const baseUrl = getBaseUrl(options.url)
  
  // 详细的token调试日志
  console.log(`[Request Debug] ${options.method || 'GET'} ${options.url}`)
  console.log(`[Request Debug] token原始值:`, token)
  console.log(`[Request Debug] token类型:`, typeof token)
  console.log(`[Request Debug] token长度:`, token ? token.length : 0)
  console.log(`[Request Debug] token存在:`, !!token)
  
  // 检查token格式
  if (token) {
    console.log(`[Request Debug] token前缀:`, token.substring(0, 20) + '...')
    console.log(`[Request Debug] token是否包含空格:`, token.includes(' '))
    console.log(`[Request Debug] token是否包含Bearer:`, token.toLowerCase().includes('bearer'))
  }
  
  return new Promise((resolve, reject) => {
    const requestHeaders = {
      'Content-Type': 'application/json',
      'Authorization': token ? `Bearer ${token}` : '',
      'X-Request-Id': generateRequestId(),
      ...(options.headers || {})
    }
    
    console.log(`[Request Debug] 请求头:`, JSON.stringify(requestHeaders, null, 2))
    
    const requestTask = uni.request({
      url: baseUrl + options.url,
      method: options.method || 'GET',
      data: options.data || {},
      timeout: options.timeout || CONFIG.TIMEOUT,
      header: requestHeaders,
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
    // 特别处理HTTP 401 - 未授权，需要清除token并跳转
    if (statusCode === 401) {
      handleBusinessError({ code: 401, message: '未授权，请重新登录' }, options)
    }
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
  
  // 如果设置了 silent 选项，不显示错误提示，也不处理跳转
  if (options.silent) {
    console.log(`[Silent Mode] 错误码: ${code}, 消息: ${message}`)
    return
  }
  
  // 4010 是自定义的"未登录但不跳转"错误码，静默处理
  if (code === 4010) {
    console.log(`[4010] 需要登录: ${message}`)
    // 只显示提示，不自动跳转
    uni.showToast({ title: message || '请先登录', icon: 'none' })
    return
  }
  
  switch (code) {
    case 401:
      // 未授权，清除token并跳转登录
      uni.removeStorageSync('token')
      uni.removeStorageSync('userInfo')
      cacheManager.clear()
      
      // 检查当前是否在登录页或注册页
      const currentPages = getCurrentPages()
      const currentRoute = currentPages.length > 0 ? currentPages[currentPages.length - 1].route : ''
      const whiteList = ['pages/login/index', 'pages/register/index']
      
      if (!whiteList.includes(currentRoute) && !getNavigatingLock()) {
        // 设置跳转锁，防止重复跳转
        setNavigatingLock(true)
        // 延迟跳转，避免多个401同时触发
        setTimeout(() => {
          uni.navigateTo({ 
            url: '/pages/login/index',
            success: () => {
              // 跳转成功后500ms释放锁
              setTimeout(() => {
                setNavigatingLock(false)
              }, 500)
            },
            fail: () => {
              uni.reLaunch({ url: '/pages/login/index' })
              // 跳转失败后500ms释放锁
              setTimeout(() => {
                setNavigatingLock(false)
              }, 500)
            }
          })
        }, 100)
      }
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
        'Authorization': token ? `Bearer ${token}` : ''
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
request.delete = (url, params = {}, options = {}) => {
  // DELETE 请求将参数附加到 URL 查询字符串，而不是请求体
  // 避免某些服务器/网关无法正确处理 DELETE 请求体的问题
  let fullUrl = url
  if (params && Object.keys(params).length > 0) {
    const queryString = Object.entries(params)
      .map(([key, value]) => `${encodeURIComponent(key)}=${encodeURIComponent(value)}`)
      .join('&')
    fullUrl = `${url}${url.includes('?') ? '&' : '?'}${queryString}`
  }
  return request({ url: fullUrl, method: 'DELETE', ...options })
}

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
