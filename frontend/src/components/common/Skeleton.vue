<template>
  <view class="skeleton-container" :class="{ animate: animate, dark: isDarkMode }">
    <!-- 骨架屏内容 -->
    <view v-if="type === 'card'" class="skeleton-card">
      <view class="skeleton-header" v-if="showHeader">
        <view class="skeleton-avatar" :style="avatarStyle"></view>
        <view class="skeleton-title-wrapper">
          <view class="skeleton-title" :style="{ width: titleWidth }"></view>
          <view class="skeleton-subtitle" v-if="showSubtitle"></view>
        </view>
      </view>
      
      <view class="skeleton-content">
        <view 
          v-for="i in rows" 
          :key="i"
          class="skeleton-row"
          :style="{ width: getRowWidth(i) }"
        ></view>
      </view>
      
      <view class="skeleton-image" v-if="showImage" :style="imageStyle"></view>
    </view>
    
    <!-- 列表骨架屏 -->
    <view v-else-if="type === 'list'" class="skeleton-list">
      <view 
        v-for="item in listCount" 
        :key="item"
        class="skeleton-list-item"
      >
        <view class="skeleton-list-avatar" v-if="showAvatar"></view>
        
        <view class="skeleton-list-content">
          <view class="skeleton-list-title"></view>
          <view class="skeleton-list-desc"></view>
        </view>
      </view>
    </view>
    
    <!-- 图文列表骨架屏 -->
    <view v-else-if="type === 'image-list'" class="skeleton-image-list">
      <view 
        v-for="item in listCount" 
        :key="item"
        class="skeleton-image-item"
      >
        <view class="skeleton-image-box"></view>
        
        <view class="skeleton-image-info">
          <view class="skeleton-image-title"></view>
          <view class="skeleton-image-meta"></view>
        </view>
      </view>
    </view>
    
    <!-- 自定义骨架屏 -->
    <view v-else class="skeleton-custom">
      <slot>
        <view class="skeleton-default">
          <view class="skeleton-block" style="height: 200rpx;"></view>
          <view class="skeleton-block" style="height: 100rpx; margin-top: 20rpx;"></view>
          <view class="skeleton-block" style="height: 100rpx; margin-top: 20rpx;"></view>
        </view>
      </slot>
    </view>
  </view>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  // 骨架屏类型：card、list、image-list、custom
  type: {
    type: String,
    default: 'card'
  },
  // 是否显示动画
  animate: {
    type: Boolean,
    default: true
  },
  // 暗黑模式
  isDarkMode: {
    type: Boolean,
    default: false
  },
  // 行数
  rows: {
    type: Number,
    default: 3
  },
  // 列表项数量
  listCount: {
    type: Number,
    default: 5
  },
  // 是否显示头像
  showAvatar: {
    type: Boolean,
    default: true
  },
  // 是否显示标题
  showHeader: {
    type: Boolean,
    default: true
  },
  // 是否显示副标题
  showSubtitle: {
    type: Boolean,
    default: false
  },
  // 是否显示图片
  showImage: {
    type: Boolean,
    default: false
  },
  // 标题宽度
  titleWidth: {
    type: String,
    default: '60%'
  },
  // 头像大小
  avatarSize: {
    type: [String, Number],
    default: '80rpx'
  },
  // 图片高度
  imageHeight: {
    type: [String, Number],
    default: '300rpx'
  }
})

const avatarStyle = computed(() => {
  const size = typeof props.avatarSize === 'number' ? `${props.avatarSize}rpx` : props.avatarSize
  return {
    width: size,
    height: size
  }
})

const imageStyle = computed(() => {
  const height = typeof props.imageHeight === 'number' ? `${props.imageHeight}rpx` : props.imageHeight
  return {
    height
  }
})

const getRowWidth = (index) => {
  // 让行宽有变化，更真实
  const widths = ['100%', '85%', '70%', '90%', '60%']
  return widths[(index - 1) % widths.length]
}
</script>

<style lang="scss" scoped>
// 骨架屏基础样式
.skeleton-container {
  // 骨架元素基础样式
  .skeleton-base {
    background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
    background-size: 200% 100%;
    border-radius: 8rpx;
  }
  
  // 动画效果
  &.animate {
    .skeleton-avatar,
    .skeleton-title,
    .skeleton-subtitle,
    .skeleton-row,
    .skeleton-image,
    .skeleton-list-avatar,
    .skeleton-list-title,
    .skeleton-list-desc,
    .skeleton-image-box,
    .skeleton-image-title,
    .skeleton-image-meta,
    .skeleton-block {
      animation: shimmer 1.5s infinite;
    }
  }
  
  // 暗黑模式
  &.dark {
    .skeleton-avatar,
    .skeleton-title,
    .skeleton-subtitle,
    .skeleton-row,
    .skeleton-image,
    .skeleton-list-avatar,
    .skeleton-list-title,
    .skeleton-list-desc,
    .skeleton-image-box,
    .skeleton-image-title,
    .skeleton-image-meta,
    .skeleton-block {
      background: linear-gradient(90deg, rgba(255,255,255,0.1) 25%, rgba(255,255,255,0.15) 50%, rgba(255,255,255,0.1) 75%);
      background-size: 200% 100%;
    }
  }
}

// 卡片骨架屏
.skeleton-card {
  padding: 30rpx;
  background: #fff;
  border-radius: 16rpx;
  
  .skeleton-header {
    display: flex;
    align-items: center;
    margin-bottom: 20rpx;
    
    .skeleton-avatar {
      @extend .skeleton-base;
      border-radius: 50%;
      margin-right: 20rpx;
    }
    
    .skeleton-title-wrapper {
      flex: 1;
      
      .skeleton-title {
        @extend .skeleton-base;
        height: 32rpx;
        margin-bottom: 12rpx;
      }
      
      .skeleton-subtitle {
        @extend .skeleton-base;
        width: 40%;
        height: 24rpx;
      }
    }
  }
  
  .skeleton-content {
    margin-bottom: 20rpx;
    
    .skeleton-row {
      @extend .skeleton-base;
      height: 28rpx;
      margin-bottom: 12rpx;
      
      &:last-child {
        margin-bottom: 0;
        width: 60% !important;
      }
    }
  }
  
  .skeleton-image {
    @extend .skeleton-base;
    width: 100%;
    border-radius: 12rpx;
  }
}

// 列表骨架屏
.skeleton-list {
  .skeleton-list-item {
    display: flex;
    align-items: center;
    padding: 24rpx 30rpx;
    background: #fff;
    border-bottom: 1rpx solid #f0f0f0;
    
    .skeleton-list-avatar {
      @extend .skeleton-base;
      width: 80rpx;
      height: 80rpx;
      border-radius: 50%;
      margin-right: 20rpx;
    }
    
    .skeleton-list-content {
      flex: 1;
      
      .skeleton-list-title {
        @extend .skeleton-base;
        height: 32rpx;
        width: 60%;
        margin-bottom: 12rpx;
      }
      
      .skeleton-list-desc {
        @extend .skeleton-base;
        height: 24rpx;
        width: 80%;
      }
    }
  }
}

// 图文列表骨架屏
.skeleton-image-list {
  display: flex;
  flex-wrap: wrap;
  padding: 20rpx;
  
  .skeleton-image-item {
    width: calc(50% - 20rpx);
    margin: 10rpx;
    background: #fff;
    border-radius: 12rpx;
    overflow: hidden;
    
    .skeleton-image-box {
      @extend .skeleton-base;
      width: 100%;
      height: 200rpx;
    }
    
    .skeleton-image-info {
      padding: 16rpx;
      
      .skeleton-image-title {
        @extend .skeleton-base;
        height: 28rpx;
        margin-bottom: 8rpx;
      }
      
      .skeleton-image-meta {
        @extend .skeleton-base;
        height: 24rpx;
        width: 60%;
      }
    }
  }
}

// 自定义骨架屏
.skeleton-custom {
  .skeleton-block {
    @extend .skeleton-base;
    border-radius: 12rpx;
  }
}

// 闪烁动画
@keyframes shimmer {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}
</style>
