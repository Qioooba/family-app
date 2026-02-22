<template>
  <view class="pull-refresh-2" :style="containerStyle">
    <!-- 下拉指示器区域 -->
    <view class="pull-refresh__header" :style="headerStyle">
      <view class="pull-refresh__content">
        <!-- 加载动画 -->
        <view class="pull-refresh__spinner" v-if="status === 'refreshing'">
          <view class="spinner-ring">
            <view class="spinner-dot" v-for="i in 12" :key="i"></view>
          </view>
        </view>
        
        <!-- 箭头图标 -->
        <view class="pull-refresh__arrow" v-else :class="{ 'is-rotate': status === 'ready' }">
          <text class="arrow-icon">↓</text>
        </view>
        
        <!-- 状态文字 -->
        <view class="pull-refresh__text">
          <text class="status-text">{{ statusText }}</text>
          <text class="update-time" v-if="showUpdateTime && lastUpdateTime">
            上次更新: {{ lastUpdateTime }}
          </text>
        </view>
        
        <!-- 成功/失败图标 -->
        <view class="pull-refresh__result" v-if="status === 'success' || status === 'error'">
          <text class="result-icon success" v-if="status === 'success'">✓</text>
          <text class="result-icon error" v-if="status === 'error'">✗</text>
        </view>
      </view>
      
      <!-- 进度条 -->
      <view class="pull-refresh__progress" v-if="showProgress">
        <view class="progress-bar" :style="{ width: progressPercent + '%' }"></view>
      </view>
    </view>
    
    <!-- 内容区域 -->
    <view 
      class="pull-refresh__body"
      @touchstart="onTouchStart"
      @touchmove="onTouchMove"
      @touchend="onTouchEnd"
      @touchcancel="onTouchEnd"
      :style="bodyStyle"
    >
      <slot></slot>
    </view>
    
    <!-- 阴影遮罩（下拉时显示） -->
    <view class="pull-refresh__shadow" v-if="showShadow && pullDistance > 0" :style="shadowStyle"></view>
  </view>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'

const props = defineProps({
  // 是否启用下拉刷新
  enabled: {
    type: Boolean,
    default: true
  },
  // 触发刷新的阈值（px）
  threshold: {
    type: Number,
    default: 80
  },
  // 最大下拉距离（px）
  maxDistance: {
    type: Number,
    default: 150
  },
  // 刷新状态持续时间（ms）
  successDuration: {
    type: Number,
    default: 800
  },
  // 是否显示更新时间
  showUpdateTime: {
    type: Boolean,
    default: true
  },
  // 是否显示进度条
  showProgress: {
    type: Boolean,
    default: true
  },
  // 是否显示阴影
  showShadow: {
    type: Boolean,
    default: true
  },
  // 自定义提示文本
  pullingText: {
    type: String,
    default: '下拉刷新'
  },
  readyText: {
    type: String,
    default: '释放立即刷新'
  },
  refreshingText: {
    type: String,
    default: '正在刷新...'
  },
  successText: {
    type: String,
    default: '刷新成功'
  },
  errorText: {
    type: String,
    default: '刷新失败'
  },
  // 是否启用触觉反馈
  hapticEnabled: {
    type: Boolean,
    default: true
  },
  // 阻尼系数（0-1）
  damping: {
    type: Number,
    default: 0.8
  }
})

const emit = defineEmits(['refresh', 'stateChange', 'pulling'])

// 状态：pulling | ready | refreshing | success | error
const status = ref('normal')
const pullDistance = ref(0)
const startY = ref(0)
const startX = ref(0)
const lastUpdateTime = ref('')
const isDragging = ref(false)
const canRefresh = ref(false)

// 计算属性
const progressPercent = computed(() => {
  return Math.min((pullDistance.value / props.threshold) * 100, 100)
})

const statusText = computed(() => {
  const texts = {
    normal: props.pullingText,
    pulling: props.pullingText,
    ready: props.readyText,
    refreshing: props.refreshingText,
    success: props.successText,
    error: props.errorText
  }
  return texts[status.value] || props.pullingText
})

const containerStyle = computed(() => {
  return {
    transform: `translateY(${pullDistance.value}px)`,
    transition: isDragging.value ? 'none' : 'transform 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94)'
  }
})

const headerStyle = computed(() => {
  return {
    height: `${Math.max(pullDistance.value, 0)}px`,
    opacity: Math.min(pullDistance.value / props.threshold, 1)
  }
})

const bodyStyle = computed(() => {
  return {
    minHeight: '100vh'
  }
})

const shadowStyle = computed(() => {
  return {
    opacity: (pullDistance.value / props.maxDistance) * 0.3
  }
})

// 触觉反馈
const triggerHaptic = (type = 'light') => {
  if (!props.hapticEnabled) return
  
  try {
    const types = {
      light: uni.vibrateShort({ type: 'light' }),
      medium: uni.vibrateShort({ type: 'medium' }),
      heavy: uni.vibrateShort({ type: 'heavy' })
    }
    types[type] || uni.vibrateShort()
  } catch (e) {
    // 降级处理
    uni.vibrateShort()
  }
}

// 触摸开始
const onTouchStart = (e) => {
  if (!props.enabled || status.value === 'refreshing') return
  
  // 检查是否在顶部
  const scrollTop = e.currentTarget.scrollTop || 0
  if (scrollTop > 0) return
  
  const touch = e.touches[0]
  startY.value = touch.clientY
  startX.value = touch.clientX
  isDragging.value = true
  canRefresh.value = true
}

// 触摸移动
const onTouchMove = (e) => {
  if (!isDragging.value || !canRefresh.value || !props.enabled) return
  
  const touch = e.touches[0]
  const deltaY = touch.clientY - startY.value
  const deltaX = touch.clientX - startX.value
  
  // 判断滑动方向，如果水平滑动大于垂直滑动，则不触发
  if (Math.abs(deltaX) > Math.abs(deltaY)) {
    canRefresh.value = false
    return
  }
  
  // 只有向下滚动才触发
  if (deltaY < 0) {
    pullDistance.value = 0
    return
  }
  
  // 阻尼计算
  const dampedDistance = deltaY * props.damping
  pullDistance.value = Math.min(dampedDistance, props.maxDistance)
  
  // 更新状态
  if (pullDistance.value >= props.threshold) {
    if (status.value !== 'ready') {
      status.value = 'ready'
      triggerHaptic('medium')
      emit('stateChange', 'ready')
    }
  } else {
    if (status.value !== 'pulling') {
      status.value = 'pulling'
      emit('stateChange', 'pulling')
    }
  }
  
  emit('pulling', {
    distance: pullDistance.value,
    percent: progressPercent.value
  })
}

// 触摸结束
const onTouchEnd = () => {
  if (!isDragging.value) return
  
  isDragging.value = false
  
  if (status.value === 'ready') {
    // 触发刷新
    status.value = 'refreshing'
    pullDistance.value = props.threshold
    triggerHaptic('heavy')
    emit('stateChange', 'refreshing')
    emit('refresh', {
      finish: finishRefresh,
      success: successRefresh,
      error: errorRefresh
    })
  } else {
    // 回弹
    resetPull()
  }
}

// 完成刷新（通用）
const finishRefresh = () => {
  resetPull()
}

// 刷新成功
const successRefresh = () => {
  status.value = 'success'
  triggerHaptic('light')
  updateLastTime()
  
  setTimeout(() => {
    resetPull()
  }, props.successDuration)
}

// 刷新失败
const errorRefresh = () => {
  status.value = 'error'
  triggerHaptic('heavy')
  
  setTimeout(() => {
    resetPull()
  }, props.successDuration)
}

// 重置下拉状态
const resetPull = () => {
  pullDistance.value = 0
  status.value = 'normal'
  emit('stateChange', 'normal')
}

// 更新时间
const updateLastTime = () => {
  const now = new Date()
  const hours = String(now.getHours()).padStart(2, '0')
  const minutes = String(now.getMinutes()).padStart(2, '0')
  lastUpdateTime.value = `${hours}:${minutes}`
  
  // 存储到本地
  try {
    uni.setStorageSync('pull_refresh_last_time', lastUpdateTime.value)
  } catch (e) {}
}

// 加载上次更新时间
onMounted(() => {
  try {
    const saved = uni.getStorageSync('pull_refresh_last_time')
    if (saved) {
      lastUpdateTime.value = saved
    }
  } catch (e) {}
})

// 暴露方法
defineExpose({
  finishRefresh,
  successRefresh,
  errorRefresh,
  resetPull
})
</script>

<style lang="scss" scoped>
.pull-refresh-2 {
  position: relative;
  will-change: transform;
}

.pull-refresh__header {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-end;
  padding-bottom: 20rpx;
  transform: translateY(-100%);
  overflow: hidden;
}

.pull-refresh__content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20rpx;
}

// 箭头图标
.pull-refresh__arrow {
  width: 60rpx;
  height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.3s ease;
  
  .arrow-icon {
    font-size: 40rpx;
    color: #5B8FF9;
    font-weight: bold;
    transition: transform 0.3s ease;
  }
  
  &.is-rotate .arrow-icon {
    transform: rotate(180deg);
  }
}

// 加载动画
.pull-refresh__spinner {
  width: 60rpx;
  height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  
  .spinner-ring {
    position: relative;
    width: 50rpx;
    height: 50rpx;
    animation: rotate 1s linear infinite;
  }
  
  .spinner-dot {
    position: absolute;
    width: 8rpx;
    height: 8rpx;
    background: #5B8FF9;
    border-radius: 50%;
    top: 50%;
    left: 50%;
    margin: -4rpx 0 0 -4rpx;
  }
  
  @for $i from 1 through 12 {
    .spinner-dot:nth-child(#{$i}) {
      transform: rotate($i * 30deg) translateX(20rpx);
      opacity: $i / 12;
    }
  }
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

// 状态文字
.pull-refresh__text {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-top: 10rpx;
  
  .status-text {
    font-size: 28rpx;
    color: #666;
    font-weight: 500;
  }
  
  .update-time {
    font-size: 22rpx;
    color: #999;
    margin-top: 6rpx;
  }
}

// 结果图标
.pull-refresh__result {
  position: absolute;
  top: 20rpx;
  
  .result-icon {
    font-size: 48rpx;
    font-weight: bold;
    
    &.success {
      color: #52c41a;
    }
    
    &.error {
      color: #ff4d4f;
    }
  }
}

// 进度条
.pull-refresh__progress {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 4rpx;
  background: rgba(0, 0, 0, 0.05);
  
  .progress-bar {
    height: 100%;
    background: linear-gradient(90deg, #5B8FF9, #87b0fc);
    border-radius: 0 2rpx 2rpx 0;
    transition: width 0.1s ease;
  }
}

// 内容区域
.pull-refresh__body {
  background: #f5f6fa;
}

// 阴影遮罩
.pull-refresh__shadow {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  pointer-events: none;
  z-index: -1;
  transition: opacity 0.3s ease;
}

// 不同状态下的动画
.pull-refresh-2 {
  &.is-refreshing {
    .pull-refresh__header {
      animation: pulse 1.5s ease-in-out infinite;
    }
  }
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.8; }
}
</style>
