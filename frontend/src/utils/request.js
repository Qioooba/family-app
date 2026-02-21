// 请求封装
const BASE_URL = 'http://localhost:8080'
const USER_SERVICE_URL = 'http://localhost:8081'

// 判断是否是用户相关API
const isUserApi = (url) => {
  return url.startsWith('/user/') || url === '/user/login' || url === '/user/register'
}

// 请求拦截
const request = (options) => {
  return new Promise((resolve, reject) => {
    const token = uni.getStorageSync('token')
    const baseUrl = isUserApi(options.url) ? USER_SERVICE_URL : BASE_URL
    
    uni.request({
      url: baseUrl + options.url,
      method: options.method || 'GET',
      data: options.data || {},
      header: {
        'Content-Type': 'application/json',
        'Authorization': token || ''
      },
      success: (res) => {
        if (res.statusCode === 200) {
          if (res.data.code === 200) {
            resolve(res.data.data)
          } else if (res.data.code === 401) {
            uni.removeStorageSync('token')
            uni.navigateTo({ url: '/pages/login/index' })
            reject(res.data)
          } else {
            uni.showToast({
              title: res.data.message || '请求失败',
              icon: 'none'
            })
            reject(res.data)
          }
        } else {
          uni.showToast({
            title: '网络错误',
            icon: 'none'
          })
          reject(res)
        }
      },
      fail: (err) => {
        uni.showToast({
          title: '网络请求失败',
          icon: 'none'
        })
        reject(err)
      }
    })
  })
}

// GET请求
request.get = (url, params = {}) => {
  return request({ url, method: 'GET', data: params })
}

// POST请求
request.post = (url, data = {}) => {
  return request({ url, method: 'POST', data })
}

// PUT请求
request.put = (url, data = {}) => {
  return request({ url, method: 'PUT', data })
}

// DELETE请求
request.delete = (url, params = {}) => {
  return request({ url, method: 'DELETE', data: params })
}

export { request }
