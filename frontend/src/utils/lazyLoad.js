/**
 * 图片懒加载工具
 * 支持自定义指令 v-lazy 和组件方式
 */

import { ref, computed, onMounted, onUnmounted } from 'vue'

// 全局配置
const defaultConfig = {
  // 占位图
  placeholder: 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" width="100" height="100"%3E%3Crect width="100" height="100" fill="%23f0f0f0"/%3E%3C/svg%3E',
  // 错误图
  errorImage: 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" width="100" height="100"%3E%3Crect width="100" height="100" fill="%23ffebee"/%3E%3Ctext x="50%" y="50%" text-anchor="middle" fill="%23ef5350" font-size="14"%3E加载失败%3C/text%3E%3C/svg%3E',
  // 根边距，提前加载距离
  rootMargin: '100px',
  // 阈值
  threshold: 0
}

// 存储观察器实例
const observers = new WeakMap()
const loadedImages = new Set()

/**
 * 创建 IntersectionObserver 观察器
 * @param {Function} callback 回调函数
 * @param {Object} options 配置选项
 */
function createObserver(callback, options = {}) {
  const config = { ...defaultConfig, ...options }
  
  // 使用 uni.createIntersectionObserver (微信小程序)
  // 或 IntersectionObserver (H5)
  if (typeof uni !== 'undefined' && uni.createIntersectionObserver) {
    return {
      observe: (el, onIntersect) => {
        const observer = uni.createIntersectionObserver({
          thresholds: [0, 0.5, 1],
          initialRatio: 0
        })
        observer.relativeToViewport({ bottom: parseInt(config.rootMargin) }).observe(el, (res) => {
          if (res.intersectionRatio > 0) {
            onIntersect(res)
            observer.disconnect()
          }
        })
        return observer
      },
      disconnect: () => {}
    }
  }
  
  // H5 环境使用原生 IntersectionObserver
  if (typeof window !== 'undefined' && window.IntersectionObserver) {
    return new IntersectionObserver(callback, {
      rootMargin: config.rootMargin,
      threshold: config.threshold
    })
  }
  
  return null
}

/**
 * 预加载图片
 * @param {string} src 图片地址
 * @returns {Promise}
 */
export function preloadImage(src) {
  return new Promise((resolve, reject) => {
    if (!src) {
      reject(new Error('Empty image src'))
      return
    }
    
    // 如果是本地图片，直接返回
    if (src.startsWith('/') && !src.startsWith('//')) {
      resolve(src)
      return
    }
    
    // 使用 uni.getImageInfo 预加载
    uni.getImageInfo({
      src,
      success: () => resolve(src),
      fail: reject
    })
  })
}

/**
 * 懒加载指令 v-lazy
 * 使用方式: 
 *   <image v-lazy="imageUrl" />
 *   <image v-lazy="{ src: imageUrl, placeholder: '...', error: '...' }" />
 */
export const vLazy = {
  mounted(el, binding) {
    const options = typeof binding.value === 'object' ? binding.value : { src: binding.value }
    const src = options.src || binding.value
    const placeholder = options.placeholder || defaultConfig.placeholder
    const errorImage = options.error || defaultConfig.errorImage
    
    if (!src) return
    
    // 设置占位图
    if (el.tagName === 'IMG' || el.tagName === 'image') {
      el.src = placeholder
      el.style.backgroundColor = '#f0f0f0'
    } else {
      el.style.backgroundImage = `url(${placeholder})`
    }
    
    // 标记为未加载
    el.dataset.lazyState = 'pending'
    el.dataset.src = src
    
    // 创建观察器
    const observer = createObserver((entries) => {
      entries.forEach(entry => {
        if (entry.isIntersecting) {
          loadImage(el, src, errorImage)
          observer.unobserve(el)
        }
      })
    })
    
    if (observer) {
      // 微信小程序
      if (observer.observe) {
        const uniObserver = observer.observe(el, () => {
          loadImage(el, src, errorImage)
        })
        observers.set(el, uniObserver)
      } else {
        // H5
        observer.observe(el)
        observers.set(el, observer)
      }
    } else {
      // 不支持观察器，直接加载
      loadImage(el, src, errorImage)
    }
  },
  
  updated(el, binding) {
    const newSrc = typeof binding.value === 'object' ? binding.value.src : binding.value
    const oldSrc = el.dataset.src
    
    if (newSrc && newSrc !== oldSrc) {
      el.dataset.src = newSrc
      // 如果已经加载过，直接更新
      if (el.dataset.lazyState === 'loaded') {
        el.src = newSrc
      } else {
        // 重新触发懒加载
        el.dataset.lazyState = 'pending'
      }
    }
  },
  
  unmounted(el) {
    const observer = observers.get(el)
    if (observer) {
      if (observer.disconnect) observer.disconnect()
      if (observer.unobserve) observer.unobserve(el)
      observers.delete(el)
    }
  }
}

/**
 * 加载图片
 */
function loadImage(el, src, errorImage) {
  if (!src || el.dataset.lazyState === 'loaded') return
  
  preloadImage(src)
    .then(() => {
      if (el.tagName === 'IMG' || el.tagName === 'image') {
        el.src = src
        el.style.backgroundColor = ''
      } else {
        el.style.backgroundImage = `url(${src})`
      }
      el.dataset.lazyState = 'loaded'
      el.classList.add('lazy-loaded')
      loadedImages.add(src)
    })
    .catch(() => {
      if (el.tagName === 'IMG' || el.tagName === 'image') {
        el.src = errorImage
      } else {
        el.style.backgroundImage = `url(${errorImage})`
      }
      el.dataset.lazyState = 'error'
      el.classList.add('lazy-error')
    })
}

/**
 * 批量懒加载 Hook
 * @param {Ref<Array>} listRef 数据列表
 * @param {Object} options 配置
 */
export function useLazyList(listRef, options = {}) {
  const { pageSize = 20, preloadPages = 1 } = options
  
  const displayList = ref([])
  const currentPage = ref(1)
  const loading = ref(false)
  const finished = ref(false)
  
  const hasMore = computed(() => {
    return displayList.value.length < (listRef.value?.length || 0)
  })
  
  const loadMore = async () => {
    if (loading.value || finished.value) return
    
    loading.value = true
    const list = listRef.value || []
    const start = displayList.value.length
    const end = start + pageSize
    const newItems = list.slice(start, end)
    
    if (newItems.length > 0) {
      displayList.value.push(...newItems)
      currentPage.value++
    } else {
      finished.value = true
    }
    
    loading.value = false
  }
  
  const reset = () => {
    displayList.value = []
    currentPage.value = 1
    finished.value = false
    loadMore()
  }
  
  // 初始化
  onMounted(() => {
    loadMore()
  })
  
  return {
    displayList,
    loading,
    finished,
    hasMore,
    loadMore,
    reset
  }
}

/**
 * 虚拟列表 Hook
 * 适用于长列表场景
 * @param {Ref<Array>} listRef 数据列表
 * @param {Object} options 配置
 */
export function useVirtualList(listRef, options = {}) {
  const {
    itemHeight = 80,
    bufferSize = 5,
    containerHeight = 600
  } = options
  
  const scrollTop = ref(0)
  const startIndex = computed(() => {
    return Math.max(0, Math.floor(scrollTop.value / itemHeight) - bufferSize)
  })
  
  const endIndex = computed(() => {
    const list = listRef.value || []
    return Math.min(
      list.length,
      startIndex.value + Math.ceil(containerHeight / itemHeight) + bufferSize * 2
    )
  })
  
  const visibleList = computed(() => {
    const list = listRef.value || []
    return list.slice(startIndex.value, endIndex.value).map((item, index) => ({
      ...item,
      _index: startIndex.value + index,
      _offset: (startIndex.value + index) * itemHeight
    }))
  })
  
  const totalHeight = computed(() => {
    return (listRef.value?.length || 0) * itemHeight
  })
  
  const offsetStyle = computed(() => ({
    paddingTop: `${startIndex.value * itemHeight}px`,
    paddingBottom: `${Math.max(0, totalHeight.value - endIndex.value * itemHeight)}px`
  }))
  
  const onScroll = (e) => {
    scrollTop.value = e.detail?.scrollTop || e.target?.scrollTop || 0
  }
  
  return {
    visibleList,
    totalHeight,
    offsetStyle,
    onScroll,
    startIndex,
    endIndex
  }
}

/**
 * 图片预加载队列
 * 适用于需要优先加载首屏图片的场景
 */
export function createPreloadQueue(maxConcurrent = 3) {
  const queue = []
  const loading = new Set()
  let isProcessing = false
  
  const processQueue = async () => {
    if (isProcessing) return
    isProcessing = true
    
    while (queue.length > 0 && loading.size < maxConcurrent) {
      const task = queue.shift()
      if (!task) continue
      
      loading.add(task.src)
      
      try {
        await preloadImage(task.src)
        task.resolve(task.src)
      } catch (err) {
        task.reject(err)
      } finally {
        loading.delete(task.src)
      }
    }
    
    isProcessing = false
    
    if (queue.length > 0) {
      processQueue()
    }
  }
  
  const add = (src) => {
    return new Promise((resolve, reject) => {
      if (loadedImages.has(src)) {
        resolve(src)
        return
      }
      
      queue.push({ src, resolve, reject })
      processQueue()
    })
  }
  
  return { add }
}

// 导出默认配置
export { defaultConfig }
