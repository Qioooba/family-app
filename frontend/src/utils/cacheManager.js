/**
 * 缓存管理器
 */
class CacheManager {
  constructor() {
    this.cache = new Map()
    this.maxAge = 5 * 60 * 1000 // 5分钟
  }

  // 生成缓存key
  getKey(url, params) {
    return `${url}_${JSON.stringify(params || {})}`
  }

  // 获取缓存
  get(url, params) {
    const key = this.getKey(url, params)
    const cached = this.cache.get(key)
    
    if (!cached) return null
    
    // 检查过期
    if (Date.now() - cached.timestamp > this.maxAge) {
      this.cache.delete(key)
      return null
    }
    
    return cached.data
  }

  // 设置缓存
  set(url, params, data) {
    const key = this.getKey(url, params)
    this.cache.set(key, {
      data,
      timestamp: Date.now()
    })
  }

  // 清除缓存
  clear() {
    this.cache.clear()
  }

  // 清除指定URL缓存
  clearByUrl(url) {
    for (const key of this.cache.keys()) {
      if (key.startsWith(url)) {
        this.cache.delete(key)
      }
    }
  }
}

export default new CacheManager()
