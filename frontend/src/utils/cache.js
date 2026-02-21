/**
 * 前端缓存管理
 * 支持内存缓存和本地存储
 */

import { ref, computed } from 'vue'

// 内存缓存
const memoryCache = new Map()

// 缓存配置
const CACHE_CONFIG = {
  // 默认过期时间（毫秒）
  DEFAULT_TTL: 5 * 60 * 1000, // 5分钟
  
  // 最长缓存时间
  MAX_TTL: 60 * 60 * 1000, // 1小时
  
  // 缓存前缀
  PREFIX: 'family_app_cache_'
}

/**
 * 缓存项
 */
class CacheItem {
  constructor(data, ttl = CACHE_CONFIG.DEFAULT_TTL) {
    this.data = data
    this.expireAt = Date.now() + ttl
    this.createdAt = Date.now()
  }
  
  isExpired() {
    return Date.now() > this.expireAt
  }
}

/**
 * 缓存管理器
 */
export const cacheManager = {
  /**
   * 设置缓存
   * @param {string} key 缓存键
   * @param {*} data 缓存数据
   * @param {object} options 配置选项
   */
  set(key, data, options = {}) {
    const { 
      ttl = CACHE_CONFIG.DEFAULT_TTL,
      persist = false, // 是否持久化到本地存储
      memoryOnly = false // 仅内存缓存
    } = options
    
    const cacheItem = new CacheItem(data, ttl)
    
    // 内存缓存
    memoryCache.set(key, cacheItem)
    
    // 本地存储（如果需要）
    if (persist && !memoryOnly) {
      try {
        uni.setStorageSync(
          CACHE_CONFIG.PREFIX + key,
          JSON.stringify({
            data,
            expireAt: cacheItem.expireAt,
            createdAt: cacheItem.createdAt
          })
        )
      } catch (e) {
        console.warn('本地存储失败:', e)
      }
    }
  },
  
  /**
   * 获取缓存
   * @param {string} key 缓存键
   * @param {object} options 配置选项
   * @returns {*} 缓存数据或null
   */
  get(key, options = {}) {
    const { 
      checkStorage = true, // 是否检查本地存储
      removeIfExpired = true // 过期时是否删除
    } = options
    
    // 先检查内存缓存
    const memoryItem = memoryCache.get(key)
    if (memoryItem) {
      if (!memoryItem.isExpired()) {
        return memoryItem.data
      }
      // 过期删除
      if (removeIfExpired) {
        memoryCache.delete(key)
      }
    }
    
    // 检查本地存储
    if (checkStorage) {
      try {
        const storageData = uni.getStorageSync(CACHE_CONFIG.PREFIX + key)
        if (storageData) {
          const parsed = JSON.parse(storageData)
          if (parsed.expireAt > Date.now()) {
            // 恢复到内存缓存
            memoryCache.set(key, new CacheItem(parsed.data, parsed.expireAt - Date.now()))
            return parsed.data
          } else if (removeIfExpired) {
            uni.removeStorageSync(CACHE_CONFIG.PREFIX + key)
          }
        }
      } catch (e) {
        console.warn('读取本地存储失败:', e)
      }
    }
    
    return null
  },
  
  /**
   * 删除缓存
   * @param {string} key 缓存键
   */
  delete(key) {
    memoryCache.delete(key)
    try {
      uni.removeStorageSync(CACHE_CONFIG.PREFIX + key)
    } catch (e) {
      console.warn('删除本地存储失败:', e)
    }
  },
  
  /**
   * 清除所有缓存
   * @param {boolean} onlyExpired 仅清除过期缓存
   */
  clear(onlyExpired = false) {
    if (onlyExpired) {
      // 清除过期缓存
      for (const [key, item] of memoryCache) {
        if (item.isExpired()) {
          memoryCache.delete(key)
          uni.removeStorageSync(CACHE_CONFIG.PREFIX + key)
        }
      }
    } else {
      // 清除所有缓存
      memoryCache.clear()
      // 清除本地存储中带有前缀的项
      try {
        const keys = uni.getStorageInfoSync().keys
        keys.forEach(key => {
          if (key.startsWith(CACHE_CONFIG.PREFIX)) {
            uni.removeStorageSync(key)
          }
        })
      } catch (e) {
        console.warn('清除本地存储失败:', e)
      }
    }
  },
  
  /**
   * 检查缓存是否存在且有效
   * @param {string} key 缓存键
   * @returns {boolean}
   */
  has(key) {
    const item = memoryCache.get(key)
    if (item && !item.isExpired()) {
      return true
    }
    // 检查本地存储
    try {
      const storageData = uni.getStorageSync(CACHE_CONFIG.PREFIX + key)
      if (storageData) {
        const parsed = JSON.parse(storageData)
        return parsed.expireAt > Date.now()
      }
    } catch (e) {}
    return false
  },
  
  /**
   * 获取缓存信息
   * @returns {object}
   */
  getInfo() {
    const memorySize = memoryCache.size
    let storageSize = 0
    try {
      const keys = uni.getStorageInfoSync().keys
      storageSize = keys.filter(key => key.startsWith(CACHE_CONFIG.PREFIX)).length
    } catch (e) {}
    
    return {
      memorySize,
      storageSize,
      totalSize: memorySize + storageSize
    }
  }
}

/**
 * 使用缓存的 Composition API
 * @param {string} key 缓存键
 * @param {Function} fetcher 数据获取函数
 * @param {object} options 配置选项
 */
export function useCache(key, fetcher, options = {}) {
  const {
    ttl = CACHE_CONFIG.DEFAULT_TTL,
    immediate = true,
    onError = null
  } = options
  
  const data = ref(null)
  const loading = ref(false)
  const error = ref(null)
  
  const cachedData = cacheManager.get(key)
  if (cachedData) {
    data.value = cachedData
  }
  
  const execute = async (force = false) => {
    // 检查缓存
    if (!force) {
      const cached = cacheManager.get(key)
      if (cached) {
        data.value = cached
        return cached
      }
    }
    
    loading.value = true
    error.value = null
    
    try {
      const result = await fetcher()
      data.value = result
      cacheManager.set(key, result, { ttl })
      return result
    } catch (e) {
      error.value = e
      if (onError) onError(e)
      throw e
    } finally {
      loading.value = false
    }
  }
  
  const refresh = () => execute(true)
  
  const clear = () => {
    cacheManager.delete(key)
    data.value = null
  }
  
  if (immediate && !cachedData) {
    execute()
  }
  
  return {
    data,
    loading,
    error,
    execute,
    refresh,
    clear
  }
}

/**
 * 列表缓存管理
 * 专门用于列表数据的缓存
 */
export const listCache = {
  /**
   * 设置列表缓存
   * @param {string} listKey 列表键
   * @param {Array} items 列表项
   * @param {object} meta 元数据（分页信息等）
   */
  setList(listKey, items, meta = {}) {
    const cacheKey = `list_${listKey}`
    cacheManager.set(cacheKey, {
      items,
      meta,
      timestamp: Date.now()
    }, { ttl: CACHE_CONFIG.DEFAULT_TTL })
  },
  
  /**
   * 获取列表缓存
   * @param {string} listKey 列表键
   * @returns {object|null}
   */
  getList(listKey) {
    const cacheKey = `list_${listKey}`
    return cacheManager.get(cacheKey)
  },
  
  /**
   * 追加列表项到缓存
   * @param {string} listKey 列表键
   * @param {Array} newItems 新列表项
   */
  appendToList(listKey, newItems) {
    const cacheKey = `list_${listKey}`
    const existing = cacheManager.get(cacheKey)
    if (existing) {
      cacheManager.set(cacheKey, {
        ...existing,
        items: [...existing.items, ...newItems]
      })
    }
  },
  
  /**
   * 更新列表中的单个项
   * @param {string} listKey 列表键
   * @param {*} updatedItem 更新的项
   * @param {Function} matchFn 匹配函数
   */
  updateListItem(listKey, updatedItem, matchFn) {
    const cacheKey = `list_${listKey}`
    const existing = cacheManager.get(cacheKey)
    if (existing) {
      const items = existing.items.map(item => 
        matchFn(item) ? { ...item, ...updatedItem } : item
      )
      cacheManager.set(cacheKey, {
        ...existing,
        items
      })
    }
  },
  
  /**
   * 从列表中删除项
   * @param {string} listKey 列表键
   * @param {Function} matchFn 匹配函数
   */
  removeFromList(listKey, matchFn) {
    const cacheKey = `list_${listKey}`
    const existing = cacheManager.get(cacheKey)
    if (existing) {
      const items = existing.items.filter(item => !matchFn(item))
      cacheManager.set(cacheKey, {
        ...existing,
        items
      })
    }
  }
}

/**
 * 用户数据缓存
 * 专门用于用户相关数据
 */
export const userCache = {
  setUserInfo(userInfo) {
    cacheManager.set('user_info', userInfo, { 
      ttl: CACHE_CONFIG.MAX_TTL,
      persist: true 
    })
  },
  
  getUserInfo() {
    return cacheManager.get('user_info', { checkStorage: true })
  },
  
  setCurrentFamily(family) {
    cacheManager.set('current_family', family, {
      ttl: CACHE_CONFIG.MAX_TTL,
      persist: true
    })
  },
  
  getCurrentFamily() {
    return cacheManager.get('current_family', { checkStorage: true })
  },
  
  clearUserData() {
    cacheManager.delete('user_info')
    cacheManager.delete('current_family')
  }
}
