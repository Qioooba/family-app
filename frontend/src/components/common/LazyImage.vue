<template>
  <view 
    class="lazy-image-wrapper"
    :class="[state, { 'circle': circle }]"
    :style="wrapperStyle"
    @click="$emit('click', $event)"
  >
    <!-- 占位图/加载中 -->
    <view v-if="state === 'pending' || state === 'loading'" class="placeholder">
      <slot name="placeholder">
        <view class="default-placeholder" :style="{ backgroundColor: placeholderColor }">
          <view v-if="showLoading" class="loading-icon">
            <text class="loading-text">...</text>
          </view>
          <text v-else class="placeholder-icon">📷</text>
        </view>
      </slot>
    </view>
    
    <!-- 加载失败 -->
    <view v-else-if="state === 'error'" class="error-placeholder" @click.stop="retry">
      <slot name="error">
        <view class="default-error">
          <text class="error-icon">⚠️</text>
          <text class="error-text">加载失败</text>
          <text class="retry-text">点击重试</text>
        </view>
      </slot>
    </view>
    
    <!-- 实际图片 -->
    <image
      v-show="state === 'loaded'"
      ref="imageRef"
      class="lazy-image"
      :class="{ 'fade-in': fadeIn, 'circle': circle }"
      :src="currentSrc"
      :mode="mode"
      :webp="webp"
      :lazy-load="false"
      :show-menu-by-longpress="showMenuByLongpress"
      @load="onImageLoad"
      @error="onImageError"
    />
  </view>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { preloadImage } from '@/utils/lazyLoad.js'

const props = defineProps({
  // 图片地址
  src: {
    type: String,
    default: ''
  },
  // 图片模式
  mode: {
    type: String,
    default: 'aspectFill'
  },
  // 容器宽度
  width: {
    type: [String, Number],
    default: '100%'
  },
  // 容器高度
  height: {
    type: [String, Number],
    default: '100%'
  },
  // 圆角大小
  radius: {
    type: [String, Number],
    default: 0
  },
  // 占位图背景色
  placeholderColor: {
    type: String,
    default: '#f0f0f0'
  },
  // 是否显示加载动画
  showLoading: {
    type: Boolean,
    default: true
  },
  // 加载动画颜色
  loadingColor: {
    type: String,
    default: '#999'
  },
  // 图标颜色
  iconColor: {
    type: String,
    default: '#ccc'
  },
  // 是否圆形
  circle: {
    type: Boolean,
    default: false
  },
  // 是否使用webp
  webp: {
    type: Boolean,
    default: false
  },
  // 长按显示菜单
  showMenuByLongpress: {
    type: Boolean,
    default: false
  },
  // 是否启用懒加载（默认启用）
  lazy: {
    type: Boolean,
    default: true
  },
  // 距离视口多少距离开始加载
  threshold: {
    type: Number,
    default: 100
  },
  // 加载成功是否淡入
  fadeIn: {
    type: Boolean,
    default: true
  },
  // 重试次数
  retryCount: {
    type: Number,
    default: 3
  }
})

const emit = defineEmits(['click', 'load', 'error'])

const imageRef = ref(null)
const state = ref('pending') // pending, loading, loaded, error
const currentSrc = ref('')
const retryTimes = ref(0)
let observer = null

// 包装器样式
const wrapperStyle = computed(() => {
  const style = {}
  if (props.width) style.width = typeof props.width === 'number' ? `${props.width}rpx` : props.width
  if (props.height) style.height = typeof props.height === 'number' ? `${props.height}rpx` : props.height
  if (props.radius) style.borderRadius = typeof props.radius === 'number' ? `${props.radius}rpx` : props.radius
  return style
})

// 创建观察器
const createObserver = () => {
  if (!props.lazy) {
    startLoad()
    return
  }

  // 使用 IntersectionObserver
  try {
    observer = uni.createIntersectionObserver({
      thresholds: [0],
      initialRatio: 0
    })
    
    const targetEl = imageRef.value?.$el
    if (!targetEl) {
      // 如果获取不到元素，直接加载
      startLoad()
      return
    }
    
    observer.relativeToViewport({ bottom: props.threshold }).observe(targetEl, (res) => {
      if (res.intersectionRatio > 0 && state.value === 'pending') {
        startLoad()
        observer.disconnect()
      }
    })
  } catch (e) {
    console.warn('IntersectionObserver not supported, loading directly:', e)
    startLoad()
  }
}

// 开始加载
const startLoad = async () => {
  if (!props.src || state.value === 'loading') return
  
  state.value = 'loading'
  
  try {
    await preloadImage(props.src)
    currentSrc.value = props.src
    // 图片加载完成会触发 @load 事件
  } catch (error) {
    handleError()
  }
}

// 图片加载成功
const onImageLoad = () => {
  state.value = 'loaded'
  retryTimes.value = 0
  emit('load')
}

// 图片加载失败
const onImageError = () => {
  handleError()
}

// 处理错误
const handleError = () => {
  if (retryTimes.value < props.retryCount) {
    retryTimes.value++
    // 延迟重试
    setTimeout(() => {
      currentSrc.value = props.src + (props.src.includes('?') ? '&' : '?') + '_retry=' + Date.now()
    }, 1000 * retryTimes.value)
  } else {
    state.value = 'error'
    emit('error')
  }
}

// 重试
const retry = () => {
  if (state.value === 'error') {
    retryTimes.value = 0
    startLoad()
  }
}

// 监听 src 变化
watch(() => props.src, (newSrc, oldSrc) => {
  if (newSrc !== oldSrc) {
    state.value = 'pending'
    currentSrc.value = ''
    retryTimes.value = 0
    
    if (!props.lazy || observer) {
      // 如果已经挂载，重新创建观察器或直接加载
      if (observer) {
        observer.disconnect()
        createObserver()
      } else {
        startLoad()
      }
    }
  }
})

onMounted(() => {
  createObserver()
})

onUnmounted(() => {
  if (observer) {
    observer.disconnect()
    observer = null
  }
})
</script>

<style lang="scss" scoped>
.lazy-image-wrapper {
  position: relative;
  overflow: hidden;
  
  &.circle {
    border-radius: 50%;
  }
  
  .placeholder,
  .error-placeholder {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    display: flex;
    align-items: center;
    justify-content: center;
  }
  
  .default-placeholder {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    
    .placeholder-icon {
      font-size: 48rpx;
      opacity: 0.5;
    }
    
    .loading-icon {
      display: flex;
      align-items: center;
      justify-content: center;
      
      .loading-text {
        font-size: 32rpx;
        color: #999;
        animation: pulse 1s ease-in-out infinite;
      }
    }
  }
  
  .default-error {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    
    .error-icon {
      font-size: 48rpx;
    }
    
    .error-text {
      font-size: 24rpx;
      color: #ef5350;
      margin-top: 8rpx;
    }
    
    .retry-text {
      font-size: 22rpx;
      color: #999;
      margin-top: 4rpx;
    }
  }
  
  @keyframes pulse {
    0%, 100% { opacity: 0.4; }
    50% { opacity: 1; }
  }
  
  .lazy-image {
    width: 100%;
    height: 100%;
    
    &.fade-in {
      animation: fadeIn 0.3s ease-out;
    }
    
    &.circle {
      border-radius: 50%;
    }
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}
</style>
