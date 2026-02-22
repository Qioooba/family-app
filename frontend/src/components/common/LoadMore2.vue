<template>
  <view class="load-more-2">
    <!-- 加载状态指示器 -->
    <view class="load-more__indicator" :class="status">
      <!-- 加载中动画 -->
      <view class="loading-spinner" v-if="status === 'loading'">
        <view class="spinner-container">
          <view class="spinner-circle" v-for="i in 3" :key="i"></view>
        </view>
        <view class="loading-text">
          <text>{{ loadingText }}</text>
          <view class="loading-dots">
            <text v-for="i in 3" :key="i" class="dot">.</text>
          </view>
        </view>
      </view>
      
      <!-- 没有更多 -->
      <view class="no-more" v-else-if="status === 'noMore'">
        <view class="divider"></view>
        <view class="no-more-content">
          <text class="no-more-icon">🏁</text>
          <text class="no-more-text">{{ noMoreText }}</text>
        </view>
        <view class="divider"></view>
      </view>
      
      <!-- 加载失败 -->
      <view class="load-failed" v-else-if="status === 'error'" @click="retry">
        <text class="error-icon">⚠️</text>
        <view class="error-content">
          <text class="error-text">{{ errorText }}</text>
          <text class="retry-text">{{ retryText }}</text>
        </view>
      </view>
      
      <!-- 准备加载 -->
      <view class="ready-load" v-else-if="status === 'ready'">
        <view class="arrow-icon">
          <text>↑</text>
        </view>
        <text class="ready-text">{{ readyText }}</text>
      </view>
      
      <!-- 默认状态 -->
      <view class="default-state" v-else
        @click="!autoLoad ? loadMore() : null"
      >
        <text class="default-text">{{ defaultText }}</text>
      </view>
    </view>
    
    <!-- 骨架屏加载更多 -->
    <view class="skeleton-more" v-if="showSkeleton && status === 'loading'">
      <view class="skeleton-item" v-for="i in skeletonRows" :key="i">
        <view class="skeleton-avatar"></view>
        <view class="skeleton-content">
          <view class="skeleton-line"></view>
          <view class="skeleton-line short"></view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'

const props = defineProps({
  // 当前状态: loading | noMore | error | ready | default
  status: {
    type: String,
    default: 'default'
  },
  // 是否自动加载
  autoLoad: {
    type: Boolean,
    default: true
  },
  // 距离底部多少像素触发加载
  threshold: {
    type: Number,
    default: 100
  },
  // 加载完成后的延迟（ms）
  delay: {
    type: Number,
    default: 300
  },
  // 是否显示骨架屏
  showSkeleton: {
    type: Boolean,
    default: false
  },
  // 骨架屏行数
  skeletonRows: {
    type: Number,
    default: 3
  },
  // 文本配置
  loadingText: {
    type: String,
    default: '正在加载更多'
  },
  noMoreText: {
    type: String,
    default: '没有更多内容了'
  },
  errorText: {
    type: String,
    default: '加载失败'
  },
  retryText: {
    type: String,
    default: '点击重试'
  },
  readyText: {
    type: String,
    default: '释放加载更多'
  },
  defaultText: {
    type: String,
    default: '上拉加载更多'
  },
  // 自定义样式
  theme: {
    type: String,
    default: 'default', // default | simple | card
    validator: (value) => ['default', 'simple', 'card'].includes(value)
  }
})

const emit = defineEmits(['loadMore', 'retry', 'stateChange'])

const isVisible = ref(false)
const observer = ref(null)

// 观察器配置
const initIntersectionObserver = () => {
  if (!props.autoLoad) return
  
  // #ifdef H5
  if (typeof IntersectionObserver !== 'undefined') {
    observer.value = new IntersectionObserver((entries) => {
      entries.forEach(entry => {
        if (entry.isIntersecting && props.status !== 'loading' && props.status !== 'noMore' && props.status !== 'error') {
          loadMore()
        }
      })
    }, {
      rootMargin: `0px 0px ${props.threshold}px 0px`,
      threshold: 0
    })
  }
  // #endif
}

// 加载更多
const loadMore = () => {
  if (props.status === 'loading' || props.status === 'noMore') return
  
  emit('stateChange', 'loading')
  emit('loadMore', {
    finish: finishLoad,
    noMore: noMoreLoad,
    error: errorLoad
  })
}

// 完成加载
const finishLoad = () => {
  setTimeout(() => {
    emit('stateChange', 'default')
  }, props.delay)
}

// 没有更多
const noMoreLoad = () => {
  emit('stateChange', 'noMore')
}

// 加载失败
const errorLoad = () => {
  emit('stateChange', 'error')
}

// 重试
const retry = () => {
  emit('retry')
  loadMore()
}

// 暴露方法
defineExpose({
  loadMore,
  finishLoad,
  noMoreLoad,
  errorLoad
})

onMounted(() => {
  initIntersectionObserver()
})

onUnmounted(() => {
  if (observer.value) {
    observer.value.disconnect()
  }
})
</script>

<style lang="scss" scoped>
.load-more-2 {
  width: 100%;
}

// 加载指示器
.load-more__indicator {
  padding: 40rpx 30rpx;
  display: flex;
  justify-content: center;
  align-items: center;
  
  // 加载中状态
  &.loading {
    .loading-spinner {
      display: flex;
      flex-direction: column;
      align-items: center;
      
      .spinner-container {
        display: flex;
        gap: 12rpx;
        margin-bottom: 16rpx;
        
        .spinner-circle {
          width: 16rpx;
          height: 16rpx;
          background: #5B8FF9;
          border-radius: 50%;
          animation: bounce 1.4s ease-in-out infinite both;
          
          &:nth-child(1) { animation-delay: -0.32s; }
          &:nth-child(2) { animation-delay: -0.16s; }
        }
      }
      
      .loading-text {
        display: flex;
        align-items: center;
        
        text {
          font-size: 26rpx;
          color: #999;
        }
        
        .loading-dots {
          display: flex;
          
          .dot {
            font-size: 26rpx;
            color: #999;
            animation: dots 1.4s infinite;
            
            &:nth-child(2) { animation-delay: 0.2s; }
            &:nth-child(3) { animation-delay: 0.4s; }
          }
        }
      }
    }
  }
}

@keyframes bounce {
  0%, 80%, 100% { transform: scale(0.6); opacity: 0.5; }
  40% { transform: scale(1); opacity: 1; }
}

@keyframes dots {
  0%, 20% { opacity: 0; transform: translateY(0); }
  50% { opacity: 1; transform: translateY(-4rpx); }
  80%, 100% { opacity: 0; transform: translateY(0); }
}

// 没有更多
.no-more {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  
  .divider {
    flex: 1;
    height: 2rpx;
    background: linear-gradient(90deg, transparent, #e0e0e0, transparent);
  }
  
  .no-more-content {
    display: flex;
    align-items: center;
    padding: 0 30rpx;
    
    .no-more-icon {
      font-size: 28rpx;
      margin-right: 12rpx;
    }
    
    .no-more-text {
      font-size: 26rpx;
      color: #bbb;
    }
  }
}

// 加载失败
.load-failed {
  display: flex;
  align-items: center;
  padding: 20rpx 40rpx;
  background: #fff2f0;
  border-radius: 12rpx;
  
  .error-icon {
    font-size: 36rpx;
    margin-right: 16rpx;
  }
  
  .error-content {
    display: flex;
    flex-direction: column;
    
    .error-text {
      font-size: 26rpx;
      color: #ff4d4f;
      margin-bottom: 4rpx;
    }
    
    .retry-text {
      font-size: 22rpx;
      color: #ff7875;
    }
  }
}

// 准备加载
.ready-load {
  display: flex;
  flex-direction: column;
  align-items: center;
  
  .arrow-icon {
    width: 48rpx;
    height: 48rpx;
    background: #f0f5ff;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 12rpx;
    animation: arrow-bounce 1s ease-in-out infinite;
    
    text {
      font-size: 28rpx;
      color: #5B8FF9;
      font-weight: bold;
    }
  }
  
  .ready-text {
    font-size: 26rpx;
    color: #5B8FF9;
  }
}

@keyframes arrow-bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-8rpx); }
}

// 默认状态
.default-state {
  padding: 20rpx 40rpx;
  
  .default-text {
    font-size: 26rpx;
    color: #999;
  }
}

// 骨架屏
.skeleton-more {
  padding: 0 30rpx;
  
  .skeleton-item {
    display: flex;
    padding: 24rpx 0;
    border-bottom: 2rpx solid #f5f5f5;
    
    &:last-child {
      border-bottom: none;
    }
    
    .skeleton-avatar {
      width: 80rpx;
      height: 80rpx;
      background: #f0f0f0;
      border-radius: 50%;
      margin-right: 20rpx;
      animation: skeleton-pulse 1.5s ease-in-out infinite;
    }
    
    .skeleton-content {
      flex: 1;
      
      .skeleton-line {
        height: 28rpx;
        background: #f0f0f0;
        border-radius: 4rpx;
        margin-bottom: 16rpx;
        animation: skeleton-pulse 1.5s ease-in-out infinite;
        
        &.short {
          width: 60%;
        }
      }
    }
  }
}

@keyframes skeleton-pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.4; }
}

// 主题样式
.load-more-2 {
  // 简约主题
  &.theme-simple {
    .load-more__indicator {
      padding: 20rpx;
    }
    
    .loading-spinner {
      .spinner-container {
        .spinner-circle {
          width: 12rpx;
          height: 12rpx;
        }
      }
    }
    
    .no-more {
      .no-more-content {
        .no-more-text {
          font-size: 24rpx;
        }
      }
    }
  }
  
  // 卡片主题
  &.theme-card {
    .load-more__indicator {
      background: #fff;
      margin: 20rpx 30rpx;
      border-radius: 16rpx;
      box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.04);
    }
    
    .load-failed {
      margin: 0 30rpx;
    }
  }
}
</style>
