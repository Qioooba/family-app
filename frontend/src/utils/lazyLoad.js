/**
 * 图片懒加载指令
 * 使用 IntersectionObserver API 实现
 */

const lazyImages = new Map()
let observer = null

// 创建 IntersectionObserver
function createObserver() {
  if (observer) return observer
  
  observer = uni.createIntersectionObserver({
    thresholds: [0, 0.5, 1],
    initialRatio: 0
  })
  
  return observer
}

/**
 * 懒加载指令
 * 使用方式: v-lazy="imageUrl"
 */
export const lazyDirective = {
  mounted(el, binding) {
    const src = binding.value
    if (!src) return
    
    // 保存原始src
    el.dataset.src = src
    
    // 设置占位图或背景色
    if (el.tagName === 'IMG') {
      el.style.backgroundColor = '#f0f0f0'
    }
    
    // 创建观察器
    const observer = createObserver()
    
    observer.relativeToViewport({ bottom: 100 }).observe(el, (res) => {
      if (res.intersectionRatio > 0) {
        // 进入视口，加载图片
        loadImage(el)
        // 停止观察
        observer.disconnect()
      }
    })
    
    lazyImages.set(el, observer)
  },
  
  updated(el, binding) {
    // 更新src时重新加载
    const newSrc = binding.value
    if (newSrc && newSrc !== el.dataset.src) {
      el.dataset.src = newSrc
      if (el.tagName === 'IMG') {
        el.src = newSrc
      } else {
        el.style.backgroundImage = `url(${newSrc})`
      }
    }
  },
  
  unmounted(el) {
    // 清理
    const observer = lazyImages.get(el)
    if (observer) {
      observer.disconnect()
      lazyImages.delete(el)
    }
  }
}

/**
 * 加载图片
 */
function loadImage(el) {
  const src = el.dataset.src
  if (!src) return
  
  // 预加载图片
  uni.getImageInfo({
    src,
    success: () => {
      if (el.tagName === 'IMG') {
        el.src = src
        el.style.backgroundColor = ''
      } else {
        el.style.backgroundImage = `url(${src})`
      }
      el.classList.add('lazy-loaded')
    },
    fail: () => {
      // 加载失败，显示错误占位图
      el.classList.add('lazy-error')
    }
  })
}

/**
 * 批量懒加载
 * @param {Array} list 数据列表
 * @param {number} pageSize 每页数量
 * @returns {Object} 分页数据
 */
export function useLazyLoad(list, pageSize = 20) {
  const displayList = ref([])
  const currentPage = ref(1)
  const hasMore = computed(() => {
    return displayList.value.length < list.value.length
  })
  
  const loadMore = () => {
    const start = (currentPage.value - 1) * pageSize
    const end = start + pageSize
    const newItems = list.value.slice(start, end)
    displayList.value.push(...newItems)
    currentPage.value++
  }
  
  // 初始化加载第一页
  loadMore()
  
  return {
    displayList,
    hasMore,
    loadMore
  }
}

/**
 * 虚拟滚动列表
 * @param {Array} list 完整数据列表
 * @param {number} itemHeight 每项高度
 * @param {number} bufferSize 缓冲数量
 */
export function useVirtualList(list, itemHeight = 80, bufferSize = 5) {
  const containerRef = ref(null)
  const scrollTop = ref(0)
  const containerHeight = ref(0)
  
  // 可视区域能显示的项目数
  const visibleCount = computed(() => {
    return Math.ceil(containerHeight.value / itemHeight) + bufferSize * 2
  })
  
  // 起始索引
  const startIndex = computed(() => {
    return Math.max(0, Math.floor(scrollTop.value / itemHeight) - bufferSize)
  })
  
  // 结束索引
  const endIndex = computed(() => {
    return Math.min(list.value.length, startIndex.value + visibleCount.value)
  })
  
  // 可视区域数据
  const visibleList = computed(() => {
    return list.value.slice(startIndex.value, endIndex.value).map((item, index) => ({
      ...item,
      _index: startIndex.value + index,
      _style: {
        position: 'absolute',
        top: `${(startIndex.value + index) * itemHeight}px`,
        height: `${itemHeight}px`,
        width: '100%'
      }
    }))
  })
  
  // 总高度
  const totalHeight = computed(() => {
    return list.value.length * itemHeight
  })
  
  // 偏移量
  const offset = computed(() => {
    return startIndex.value * itemHeight
  })
  
  // 滚动事件处理
  const onScroll = (e) => {
    scrollTop.value = e.detail.scrollTop
  }
  
  // 初始化容器高度
  const initContainer = () => {
    if (containerRef.value) {
      const query = uni.createSelectorQuery()
      query.select(containerRef.value).boundingClientRect((rect) => {
        containerHeight.value = rect.height
      }).exec()
    }
  }
  
  onMounted(() => {
    initContainer()
  })
  
  return {
    containerRef,
    visibleList,
    totalHeight,
    offset,
    onScroll,
    startIndex,
    endIndex
  }
}
