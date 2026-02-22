/**
 * 请求管理器 - 支持请求合并
 */
class RequestManager {
  constructor() {
    this.pendingRequests = new Map()
    this.mergeWindow = 100 // 合并时间窗口
  }

  // 生成请求key
  getRequestKey(config) {
    return `${config.method || 'GET'}_${config.url}_${JSON.stringify(config.data || {})}`
  }

  // 合并请求
  mergeRequest(config) {
    const key = this.getRequestKey(config)
    
    if (this.pendingRequests.has(key)) {
      // 返回已有Promise
      return this.pendingRequests.get(key)
    }
    
    const promise = new Promise((resolve, reject) => {
      // 延迟执行，等待合并
      setTimeout(() => {
        this.pendingRequests.delete(key)
        // 执行实际请求
        this.executeRequest(config).then(resolve).catch(reject)
      }, this.mergeWindow)
    })
    
    this.pendingRequests.set(key, promise)
    return promise
  }

  // 执行请求
  executeRequest(config) {
    return new Promise((resolve, reject) => {
      uni.request({
        ...config,
        success: resolve,
        fail: reject
      })
    })
  }

  // 批量请求
  batchRequests(requests) {
    return Promise.all(requests.map(r => this.mergeRequest(r)))
  }
}

export default new RequestManager()
