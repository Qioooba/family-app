/**
 * 首屏加载优化工具
 * 包含骨架屏、预加载、资源优化等功能
 */

import { ref, onMounted } from 'vue'

/**
 * 骨架屏 Hook
 * 用于数据加载前显示占位内容
 * @param {Object} options 配置
 */
export function useSkeleton(options = {}) {
  const { minDuration = 300 } = options
  
  const loading = ref(true)
  const startTime = ref(0)
  
  const show = () => {
    loading.value = true
    startTime.value = Date.now()
  }
  
  const hide = () => {
    const elapsed = Date.now() - startTime.value
    const remaining = Math.max(0, minDuration - elapsed)
    
    setTimeout(() => {
      loading.value = false
    }, remaining)
  }
  
  onMounted(() => {
    show()
  })
  
  return {
    loading,
    show,
    hide
  }
}

/**
 * 预加载资源
 * @param {Array} resources 资源列表
 * @param {Object} options 配置
 */
export function preloadResources(resources, options = {}) {
  const { 
    onProgress = () => {},
    onComplete = () => {},
    onError = () => {},
    concurrent = 3,
    timeout = 10000
  } = options
  
  const total = resources.length
  let loaded = 0
  let failed = 0
  const loading = new Set()
  const queue = [...resources]
  
  const loadResource = (resource) => {
    return new Promise((resolve, reject) => {
      const timer = setTimeout(() => {
        reject(new Error('Timeout'))
      }, timeout)
      
      if (resource.type === 'image' || resource.url.match(/\.(jpg|jpeg|png|gif|webp)$/i)) {
        uni.getImageInfo({
          src: resource.url,
          success: () => {
            clearTimeout(timer)
            resolve(resource)
          },
          fail: (err) => {
            clearTimeout(timer)
            reject(err)
          }
        })
      } else if (resource.type === 'font' || resource.url.match(/\.(woff2?|ttf|otf)$/i)) {
        // 字体预加载（H5）
        if (typeof document !== 'undefined') {
          const font = new FontFace(resource.name || 'preload-font', `url(${resource.url})`)
          font.load()
            .then(() => {
              document.fonts.add(font)
              clearTimeout(timer)
              resolve(resource)
            })
            .catch((err) => {
              clearTimeout(timer)
              reject(err)
            })
        } else {
          clearTimeout(timer)
          resolve(resource)
        }
      } else {
        // 其他资源
        uni.request({
          url: resource.url,
          method: 'HEAD',
          success: () => {
            clearTimeout(timer)
            resolve(resource)
          },
          fail: (err) => {
            clearTimeout(timer)
            reject(err)
          }
        })
      }
    })
  }
  
  const processQueue = async () => {
    while (queue.length > 0 && loading.size < concurrent) {
      const resource = queue.shift()
      if (!resource) continue
      
      loading.add(resource.url)
      
      try {
        await loadResource(resource)
        loaded++
        onProgress({ loaded, failed, total, resource })
      } catch (err) {
        failed++
        onError({ resource, error: err })
      } finally {
        loading.delete(resource.url)
      }
    }
    
    if (loaded + failed >= total) {
      onComplete({ loaded, failed, total })
    } else if (queue.length > 0) {
      processQueue()
    }
  }
  
  // 启动加载
  for (let i = 0; i < Math.min(concurrent, resources.length); i++) {
    processQueue()
  }
  
  return {
    get progress() {
      return total > 0 ? (loaded + failed) / total : 0
    }
  }
}

/**
 * 首屏关键资源预加载
 * @param {Array} criticalResources 关键资源列表
 */
export function preloadCriticalResources(criticalResources = []) {
  const defaultResources = [
    // 默认的关键资源
    { url: '/static/logo.png', type: 'image' },
    { url: '/static/tabbar/home.png', type: 'image' },
    { url: '/static/tabbar/home-active.png', type: 'image' }
  ]
  
  const resources = [...defaultResources, ...criticalResources]
  
  return preloadResources(resources, {
    concurrent: 6,
    timeout: 5000
  })
}

/**
 * 延迟加载非关键资源
 * @param {Function} callback 延迟执行的函数
 * @param {number} delay 延迟时间（毫秒）
 */
export function lazyLoadNonCritical(callback, delay = 2000) {
  // 使用 requestIdleCallback 或 setTimeout
  if (typeof uni !== 'undefined' && uni.requestIdleCallback) {
    uni.requestIdleCallback(callback, { timeout: delay })
  } else {
    setTimeout(callback, delay)
  }
}

/**
 * 首屏性能监控 Hook
 */
export function usePerformanceMonitor() {
  const metrics = ref({
    fcp: 0, // First Contentful Paint
    lcp: 0, // Largest Contentful Paint
    tti: 0, // Time to Interactive
    pageLoad: 0
  })
  
  onMounted(() => {
    // 页面加载完成时间
    if (typeof performance !== 'undefined') {
      // 使用 PerformanceObserver 监控性能指标
      if (PerformanceObserver) {
        try {
          // 监控 FCP
          const fcpObserver = new PerformanceObserver((list) => {
            const entries = list.getEntries()
            for (const entry of entries) {
              if (entry.name === 'first-contentful-paint') {
                metrics.value.fcp = entry.startTime
                console.log('[Performance] FCP:', entry.startTime)
              }
            }
          })
          fcpObserver.observe({ entryTypes: ['paint'] })
          
          // 监控 LCP
          const lcpObserver = new PerformanceObserver((list) => {
            const entries = list.getEntries()
            const lastEntry = entries[entries.length - 1]
            metrics.value.lcp = lastEntry.startTime
            console.log('[Performance] LCP:', lastEntry.startTime)
          })
          lcpObserver.observe({ entryTypes: ['largest-contentful-paint'] })
        } catch (e) {
          console.log('PerformanceObserver not supported')
        }
      }
      
      // 页面加载时间
      window.addEventListener('load', () => {
        setTimeout(() => {
          const timing = performance.timing
          metrics.value.pageLoad = timing.loadEventEnd - timing.navigationStart
          console.log('[Performance] Page Load:', metrics.value.pageLoad)
        }, 0)
      })
    }
  })
  
  return {
    metrics
  }
}

/**
 * 动态导入组件（代码分割）
 * @param {Function} importer 导入函数
 * @param {Object} options 配置
 */
export function lazyComponent(importer, options = {}) {
  const { loadingComponent = null, errorComponent = null, delay = 200 } = options
  
  return {
    component: importer,
    loading: loadingComponent,
    error: errorComponent,
    delay
  }
}

/**
 * 图片渐进式加载
 * 先加载低质量占位图，再加载高清图
 */
export function useProgressiveImage() {
  const loadProgressive = (lowQualitySrc, highQualitySrc) => {
    return new Promise((resolve) => {
      // 先加载低质量图
      const img = new Image()
      img.onload = () => {
        resolve({ src: lowQualitySrc, quality: 'low' })
        
        // 再加载高质量图
        const highImg = new Image()
        highImg.onload = () => {
          resolve({ src: highQualitySrc, quality: 'high' })
        }
        highImg.src = highQualitySrc
      }
      img.src = lowQualitySrc
    })
  }
  
  return {
    loadProgressive
  }
}
