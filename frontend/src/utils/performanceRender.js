/**
 * 渲染性能优化工具
 * 包含防抖节流、虚拟滚动优化、渲染控制等功能
 */

import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'

/**
 * 防抖函数
 * @param {Function} fn 要执行的函数
 * @param {number} delay 延迟时间（毫秒）
 * @param {boolean} immediate 是否立即执行
 */
export function debounce(fn, delay = 300, immediate = false) {
  let timer = null
  
  const debounced = function(...args) {
    const callNow = immediate && !timer
    
    clearTimeout(timer)
    timer = setTimeout(() => {
      timer = null
      if (!immediate) {
        fn.apply(this, args)
      }
    }, delay)
    
    if (callNow) {
      fn.apply(this, args)
    }
  }
  
  debounced.cancel = () => {
    clearTimeout(timer)
    timer = null
  }
  
  return debounced
}

/**
 * 节流函数
 * @param {Function} fn 要执行的函数
 * @param {number} interval 间隔时间（毫秒）
 * @param {Object} options 配置选项
 */
export function throttle(fn, interval = 300, options = {}) {
  const { leading = true, trailing = true } = options
  let lastTime = 0
  let timer = null
  
  const throttled = function(...args) {
    const now = Date.now()
    
    if (!lastTime && !leading) {
      lastTime = now
    }
    
    const remaining = interval - (now - lastTime)
    
    if (remaining <= 0 || remaining > interval) {
      if (timer) {
        clearTimeout(timer)
        timer = null
      }
      lastTime = now
      fn.apply(this, args)
    } else if (!timer && trailing) {
      timer = setTimeout(() => {
        lastTime = leading ? Date.now() : 0
        timer = null
        fn.apply(this, args)
      }, remaining)
    }
  }
  
  throttled.cancel = () => {
    clearTimeout(timer)
    timer = null
    lastTime = 0
  }
  
  return throttled
}

/**
 * RAF 节流（使用 requestAnimationFrame）
 * 适用于动画和滚动事件
 * @param {Function} fn 要执行的函数
 */
export function rafThrottle(fn) {
  let ticking = false
  
  return function(...args) {
    if (!ticking) {
      ticking = true
      requestAnimationFrame(() => {
        ticking = false
        fn.apply(this, args)
      })
    }
  }
}

/**
 * 滚动性能优化 Hook
 * @param {Object} options 配置
 */
export function useOptimizedScroll(options = {}) {
  const {
    onScroll,
    onScrollEnd,
    throttleMs = 16, // ~60fps
    endDelay = 150
  } = options
  
  const scrollTop = ref(0)
  const isScrolling = ref(false)
  let scrollEndTimer = null
  
  const handleScroll = throttle((e) => {
    scrollTop.value = e.detail?.scrollTop || e.target?.scrollTop || 0
    isScrolling.value = true
    
    if (onScroll) {
      onScroll({ scrollTop: scrollTop.value, isScrolling: isScrolling.value })
    }
    
    // 滚动结束检测
    clearTimeout(scrollEndTimer)
    scrollEndTimer = setTimeout(() => {
      isScrolling.value = false
      if (onScrollEnd) {
        onScrollEnd({ scrollTop: scrollTop.value, isScrolling: isScrolling.value })
      }
    }, endDelay)
  }, throttleMs)
  
  onUnmounted(() => {
    clearTimeout(scrollEndTimer)
  })
  
  return {
    scrollTop,
    isScrolling,
    handleScroll
  }
}

/**
 * 列表渲染优化 Hook
 * 控制同时渲染的元素数量
 * @param {Ref<Array>} listRef 列表数据
 * @param {Object} options 配置
 */
export function useOptimizedList(listRef, options = {}) {
  const {
    renderBatch = 10, // 每批渲染数量
    renderInterval = 100, // 渲染间隔
    maxRender = 50 // 最大渲染数量
  } = options
  
  const renderedCount = ref(0)
  const isRendering = ref(false)
  let renderTimer = null
  
  const displayList = computed(() => {
    return listRef.value.slice(0, renderedCount.value)
  })
  
  const renderMore = () => {
    if (isRendering.value || renderedCount.value >= listRef.value.length) {
      return
    }
    
    isRendering.value = true
    
    renderTimer = setTimeout(() => {
      renderedCount.value = Math.min(
        renderedCount.value + renderBatch,
        listRef.value.length,
        maxRender
      )
      isRendering.value = false
      
      // 继续渲染下一批
      if (renderedCount.value < listRef.value.length && renderedCount.value < maxRender) {
        nextTick(() => {
          renderMore()
        })
      }
    }, renderInterval)
  }
  
  const reset = () => {
    renderedCount.value = 0
    isRendering.value = false
    clearTimeout(renderTimer)
    renderMore()
  }
  
  onMounted(() => {
    renderMore()
  })
  
  onUnmounted(() => {
    clearTimeout(renderTimer)
  })
  
  watch(() => listRef.value, () => {
    reset()
  }, { deep: false })
  
  return {
    displayList,
    isRendering,
    renderedCount,
    renderMore,
    reset
  }
}

/**
 * 计算属性缓存优化
 * 对昂贵的计算进行缓存
 * @param {Function} getter 计算函数
 * @param {Array} deps 依赖项
 */
export function useMemoized(getter, deps = []) {
  const cached = ref(null)
  const hasCache = ref(false)
  
  const result = computed(() => {
    // 检查依赖是否变化
    const depsChanged = deps.some((dep, i) => dep !== deps[i])
    
    if (!hasCache.value || depsChanged) {
      cached.value = getter()
      hasCache.value = true
    }
    
    return cached.value
  })
  
  const invalidate = () => {
    hasCache.value = false
  }
  
  return {
    result,
    invalidate
  }
}

/**
 * 元素可见性检测 Hook
 * 只在元素可见时渲染复杂内容
 * @param {Object} options 配置
 */
export function useVisibilityObserver(options = {}) {
  const { threshold = 0, rootMargin = '50px' } = options
  
  const isVisible = ref(false)
  const hasBeenVisible = ref(false)
  const targetRef = ref(null)
  let observer = null
  
  onMounted(() => {
    if (!targetRef.value) return
    
    if (typeof uni !== 'undefined' && uni.createIntersectionObserver) {
      observer = uni.createIntersectionObserver({
        thresholds: [threshold],
        initialRatio: 0
      })
      
      observer.relativeToViewport({ bottom: parseInt(rootMargin) })
        .observe(targetRef.value, (res) => {
          isVisible.value = res.intersectionRatio > 0
          if (isVisible.value) {
            hasBeenVisible.value = true
          }
        })
    }
  })
  
  onUnmounted(() => {
    if (observer) {
      observer.disconnect()
    }
  })
  
  return {
    isVisible,
    hasBeenVisible,
    targetRef
  }
}

/**
 * 减少重绘 Hook
 * 批量处理 DOM 更新
 */
export function useBatchUpdate() {
  const pendingUpdates = ref([])
  let rafId = null
  
  const scheduleUpdate = (updateFn) => {
    pendingUpdates.value.push(updateFn)
    
    if (!rafId) {
      rafId = requestAnimationFrame(() => {
        pendingUpdates.value.forEach(fn => fn())
        pendingUpdates.value = []
        rafId = null
      })
    }
  }
  
  onUnmounted(() => {
    if (rafId) {
      cancelAnimationFrame(rafId)
    }
  })
  
  return {
    scheduleUpdate
  }
}

/**
 * 长任务切片 Hook
 * 将长任务切分为多个小任务，避免阻塞主线程
 * @param {Array} tasks 任务列表
 * @param {Object} options 配置
 */
export function useChunkedTasks(tasks, options = {}) {
  const { chunkSize = 5, delay = 0 } = options
  
  const progress = ref(0)
  const isRunning = ref(false)
  const results = ref([])
  let currentIndex = 0
  let timeoutId = null
  
  const runChunk = () => {
    if (currentIndex >= tasks.length) {
      isRunning.value = false
      return
    }
    
    const chunk = tasks.slice(currentIndex, currentIndex + chunkSize)
    
    chunk.forEach(task => {
      try {
        const result = typeof task === 'function' ? task() : task
        results.value.push(result)
      } catch (e) {
        console.error('Task error:', e)
      }
    })
    
    currentIndex += chunkSize
    progress.value = Math.min(100, (currentIndex / tasks.length) * 100)
    
    // 让出主线程
    timeoutId = setTimeout(() => {
      if (typeof requestIdleCallback !== 'undefined') {
        requestIdleCallback(runChunk, { timeout: 100 })
      } else {
        runChunk()
      }
    }, delay)
  }
  
  const start = () => {
    if (isRunning.value) return
    
    isRunning.value = true
    currentIndex = 0
    results.value = []
    progress.value = 0
    runChunk()
  }
  
  const cancel = () => {
    clearTimeout(timeoutId)
    isRunning.value = false
  }
  
  onUnmounted(() => {
    cancel()
  })
  
  return {
    progress,
    isRunning,
    results,
    start,
    cancel
  }
}

// 导出默认对象
export default {
  debounce,
  throttle,
  rafThrottle,
  useOptimizedScroll,
  useOptimizedList,
  useVisibilityObserver,
  useBatchUpdate,
  useChunkedTasks
}
