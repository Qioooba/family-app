/**
 * 内存管理工具
 * 用于检测和防止内存泄漏
 */

import { onUnmounted, onMounted, watch, ref } from 'vue'

/**
 * 创建受控监听器，自动清理
 * @param {Object} options 配置
 */
export function useManagedListeners(options = {}) {
  const listeners = new Map()
  const timeouts = new Set()
  const intervals = new Set()
  const rafs = new Set()
  
  // 添加事件监听
  const addListener = (target, event, handler, opts = {}) => {
    target.addEventListener(event, handler, opts)
    
    const key = `${event}-${handler.toString()}`
    listeners.set(key, { target, event, handler, opts })
    
    return () => removeListener(key)
  }
  
  // 移除事件监听
  const removeListener = (key) => {
    const listener = listeners.get(key)
    if (listener) {
      listener.target.removeEventListener(
        listener.event,
        listener.handler,
        listener.opts
      )
      listeners.delete(key)
    }
  }
  
  // 创建受控 setTimeout
  const setManagedTimeout = (fn, delay) => {
    const id = setTimeout(() => {
      timeouts.delete(id)
      fn()
    }, delay)
    timeouts.add(id)
    return id
  }
  
  // 清除 timeout
  const clearManagedTimeout = (id) => {
    clearTimeout(id)
    timeouts.delete(id)
  }
  
  // 创建受控 setInterval
  const setManagedInterval = (fn, delay) => {
    const id = setInterval(fn, delay)
    intervals.add(id)
    return id
  }
  
  // 清除 interval
  const clearManagedInterval = (id) => {
    clearInterval(id)
    intervals.delete(id)
  }
  
  // 创建受控 requestAnimationFrame
  const requestManagedRAF = (fn) => {
    const id = requestAnimationFrame(() => {
      rafs.delete(id)
      fn()
    })
    rafs.add(id)
    return id
  }
  
  // 取消 RAF
  const cancelManagedRAF = (id) => {
    cancelAnimationFrame(id)
    rafs.delete(id)
  }
  
  // 清理所有
  const cleanup = () => {
    // 清理事件监听
    listeners.forEach((listener) => {
      listener.target.removeEventListener(
        listener.event,
        listener.handler,
        listener.opts
      )
    })
    listeners.clear()
    
    // 清理 timeouts
    timeouts.forEach(id => clearTimeout(id))
    timeouts.clear()
    
    // 清理 intervals
    intervals.forEach(id => clearInterval(id))
    intervals.clear()
    
    // 清理 RAFs
    rafs.forEach(id => cancelAnimationFrame(id))
    rafs.clear()
  }
  
  onUnmounted(() => {
    cleanup()
  })
  
  return {
    addListener,
    removeListener,
    setManagedTimeout,
    clearManagedTimeout,
    setManagedInterval,
    clearManagedInterval,
    requestManagedRAF,
    cancelManagedRAF,
    cleanup
  }
}

/**
 * 创建受控 Observer，自动清理
 * @param {Function} createObserver 创建 observer 的函数
 * @param {Function} cleanupObserver 清理 observer 的函数
 */
export function useManagedObserver(createObserver, cleanupObserver) {
  let observer = null
  
  const start = (...args) => {
    stop()
    observer = createObserver(...args)
    return observer
  }
  
  const stop = () => {
    if (observer) {
      cleanupObserver(observer)
      observer = null
    }
  }
  
  onUnmounted(() => {
    stop()
  })
  
  return {
    start,
    stop,
    get observer() {
      return observer
    }
  }
}

/**
 * 安全的 watch，自动清理
 * @param {Ref} source 监听源
 * @param {Function} callback 回调
 * @param {Object} options 配置
 */
export function useSafeWatch(source, callback, options = {}) {
  let isActive = true
  
  const stop = watch(source, (...args) => {
    if (isActive) {
      callback(...args)
    }
  }, options)
  
  onUnmounted(() => {
    isActive = false
    stop()
  })
  
  return stop
}

/**
 * 内存泄漏检测器（开发环境使用）
 */
export class MemoryLeakDetector {
  constructor(name = 'MemoryLeakDetector') {
    this.name = name
    this.snapshots = []
    this.isEnabled = typeof performance !== 'undefined' && performance.memory
  }
  
  // 记录内存快照
  snapshot(label = '') {
    if (!this.isEnabled) return null
    
    const snapshot = {
      label,
      time: Date.now(),
      memory: performance.memory
    }
    
    this.snapshots.push(snapshot)
    console.log(`[${this.name}] Snapshot "${label}":`, {
      used: (snapshot.memory.usedJSHeapSize / 1048576).toFixed(2) + ' MB',
      total: (snapshot.memory.totalJSHeapSize / 1048576).toFixed(2) + ' MB',
      limit: (snapshot.memory.jsHeapSizeLimit / 1048576).toFixed(2) + ' MB'
    })
    
    return snapshot
  }
  
  // 对比内存变化
  compare(label1, label2) {
    if (!this.isEnabled) return null
    
    const s1 = this.snapshots.find(s => s.label === label1)
    const s2 = this.snapshots.find(s => s.label === label2)
    
    if (!s1 || !s2) {
      console.warn(`[${this.name}] Snapshots not found:`, label1, label2)
      return null
    }
    
    const diff = {
      used: s2.memory.usedJSHeapSize - s1.memory.usedJSHeapSize,
      total: s2.memory.totalJSHeapSize - s1.memory.totalJSHeapSize,
      time: s2.time - s1.time
    }
    
    console.log(`[${this.name}] Compare "${label1}" -> "${label2}":`, {
      usedDiff: (diff.used / 1048576).toFixed(2) + ' MB',
      totalDiff: (diff.total / 1048576).toFixed(2) + ' MB',
      timeDiff: diff.time + 'ms'
    })
    
    return diff
  }
  
  // 检查是否存在内存泄漏风险
  checkLeak(thresholdMB = 50) {
    if (!this.isEnabled || this.snapshots.length < 2) return false
    
    const first = this.snapshots[0]
    const last = this.snapshots[this.snapshots.length - 1]
    const growth = (last.memory.usedJSHeapSize - first.memory.usedJSHeapSize) / 1048576
    
    if (growth > thresholdMB) {
      console.warn(`[${this.name}] Potential memory leak detected! Growth: ${growth.toFixed(2)} MB`)
      return true
    }
    
    return false
  }
  
  // 清理快照
  clear() {
    this.snapshots = []
  }
}

/**
 * 创建内存安全的资源管理器
 */
export function useResourceManager() {
  const resources = ref(new Map())
  const resourceId = ref(0)
  
  // 添加资源
  const add = (resource, cleanupFn) => {
    const id = ++resourceId.value
    resources.value.set(id, { resource, cleanupFn })
    return id
  }
  
  // 移除资源
  const remove = (id) => {
    const item = resources.value.get(id)
    if (item) {
      if (typeof item.cleanupFn === 'function') {
        item.cleanupFn(item.resource)
      }
      resources.value.delete(id)
    }
  }
  
  // 获取资源
  const get = (id) => {
    const item = resources.value.get(id)
    return item ? item.resource : null
  }
  
  // 清理所有资源
  const cleanup = () => {
    resources.value.forEach((item) => {
      if (typeof item.cleanupFn === 'function') {
        item.cleanupFn(item.resource)
      }
    })
    resources.value.clear()
  }
  
  onUnmounted(() => {
    cleanup()
  })
  
  return {
    add,
    remove,
    get,
    cleanup,
    size: computed(() => resources.value.size)
  }
}

/**
 * 防止闭包内存泄漏的 WeakMap 缓存
 */
export function useWeakCache() {
  const cache = new WeakMap()
  
  const set = (key, value) => {
    if (typeof key === 'object' && key !== null) {
      cache.set(key, value)
    }
  }
  
  const get = (key) => {
    return cache.get(key)
  }
  
  const has = (key) => {
    return cache.has(key)
  }
  
  const del = (key) => {
    return cache.delete(key)
  }
  
  return {
    set,
    get,
    has,
    delete: del
  }
}

/**
 * DOM 引用管理，防止 DOM 泄漏
 */
export function useDOMRefs() {
  const refs = new Map()
  
  const set = (name, el) => {
    refs.set(name, el)
  }
  
  const get = (name) => {
    return refs.get(name)
  }
  
  const clear = () => {
    refs.clear()
  }
  
  onUnmounted(() => {
    clear()
  })
  
  return {
    set,
    get,
    clear,
    refs
  }
}

// 导出默认对象
export default {
  useManagedListeners,
  useManagedObserver,
  useSafeWatch,
  MemoryLeakDetector,
  useResourceManager,
  useWeakCache,
  useDOMRefs
}
