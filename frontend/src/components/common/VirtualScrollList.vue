<template>
  <scroll-view
    class="virtual-scroll-list"
    scroll-y
    :scroll-top="innerScrollTop"
    :style="{ height: containerHeight + 'rpx' }"
    @scroll="onScroll"
    @scrolltolower="$emit('scrolltolower')"
    enhanced
    :show-scrollbar="showScrollbar"
    enable-back-to-top
    :lower-threshold="lowerThreshold"
  >
    <!-- 总高度占位 -->
    <view class="virtual-list-phantom" :style="{ height: totalHeight + 'rpx' }">
      <!-- 可视区域内容 -->
      <view class="virtual-list-wrapper" :style="wrapperStyle">
        <view
          v-for="item in visibleData"
          :key="getItemKey(item)"
          class="virtual-list-item"
          :style="getItemStyle(item)"
        >
          <slot :item="item.data" :index="item.index">
            <view class="default-item">
              <text>{{ item.data.name || `Item ${item.index}` }}</text>
            </view>
          </slot>
        </view>
      </view>
    </view>
    
    <!-- 加载更多 -->
    <view v-if="loading" class="loading-more">
      <up-loading-icon mode="circle" size="20"></up-loading-icon>
      <text class="loading-text">加载中...</text>
    </view>
    
    <!-- 没有更多 -->
    <view v-else-if="finished && data.length > 0" class="no-more">
      <text>没有更多了</text>
    </view>
  </scroll-view>
</template>

<script setup>
import { ref, computed, watch } from 'vue'

const props = defineProps({
  // 数据列表
  data: {
    type: Array,
    default: () => []
  },
  // 唯一键字段或函数
  keyField: {
    type: [String, Function],
    default: 'id'
  },
  // 每项高度（rpx）
  itemHeight: {
    type: Number,
    default: 120
  },
  // 容器高度（rpx）
  containerHeight: {
    type: Number,
    default: 800
  },
  // 可视区域外缓冲数量
  buffer: {
    type: Number,
    default: 3
  },
  // 是否显示滚动条
  showScrollbar: {
    type: Boolean,
    default: false
  },
  // 距离底部多少触发加载更多
  lowerThreshold: {
    type: Number,
    default: 100
  },
  // 加载中状态
  loading: {
    type: Boolean,
    default: false
  },
  // 是否已加载完成
  finished: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['scroll', 'scrolltolower'])

// 内部状态
const scrollTop = ref(0)
const innerScrollTop = ref(0)

// 计算总高度（rpx）
const totalHeight = computed(() => {
  return props.data.length * props.itemHeight
})

// 开始索引
const startIndex = computed(() => {
  const index = Math.floor(scrollTop.value / props.itemHeight) - props.buffer
  return Math.max(0, index)
})

// 结束索引
const endIndex = computed(() => {
  const visibleCount = Math.ceil(props.containerHeight / props.itemHeight)
  const index = startIndex.value + visibleCount + props.buffer * 2
  return Math.min(props.data.length, index)
})

// 可视区域数据
const visibleData = computed(() => {
  const result = []
  for (let i = startIndex.value; i < endIndex.value; i++) {
    if (i >= 0 && i < props.data.length) {
      result.push({
        data: props.data[i],
        index: i,
        _virtualId: getItemKey({ data: props.data[i], index: i })
      })
    }
  }
  return result
})

// 包装器样式
const wrapperStyle = computed(() => {
  const offset = startIndex.value * props.itemHeight
  return {
    transform: `translateY(${offset}rpx)`
  }
})

// 获取每项的 key
const getItemKey = (item) => {
  if (typeof props.keyField === 'function') {
    return props.keyField(item.data, item.index)
  }
  return item.data[props.keyField] || `index-${item.index}`
}

// 获取每项样式
const getItemStyle = (item) => {
  return {
    height: `${props.itemHeight}rpx`
  }
}

// 滚动事件处理
const onScroll = (e) => {
  const newScrollTop = e.detail?.scrollTop || 0
  scrollTop.value = newScrollTop
  emit('scroll', e)
}

// 滚动到指定索引
const scrollToIndex = (index, animated = true) => {
  const targetScrollTop = index * props.itemHeight
  innerScrollTop.value = targetScrollTop
  scrollTop.value = targetScrollTop
}

// 滚动到顶部
const scrollToTop = () => {
  innerScrollTop.value = 0
  scrollTop.value = 0
}

// 重置
const reset = () => {
  scrollTop.value = 0
  innerScrollTop.value = 0
}

// 监听数据变化，如果数据变少，重置滚动位置
watch(() => props.data.length, (newVal, oldVal) => {
  if (newVal < oldVal) {
    reset()
  }
})

// 暴露方法
defineExpose({
  scrollToIndex,
  scrollToTop,
  reset
})
</script>

<style lang="scss" scoped>
.virtual-scroll-list {
  position: relative;
}

.virtual-list-phantom {
  position: relative;
}

.virtual-list-wrapper {
  position: absolute;
  left: 0;
  right: 0;
  top: 0;
  will-change: transform;
}

.virtual-list-item {
  box-sizing: border-box;
}

.default-item {
  display: flex;
  align-items: center;
  height: 100%;
  padding: 0 30rpx;
  border-bottom: 1rpx solid rgba(0,0,0,0.05);
  font-size: 28rpx;
  color: #333;
}

.loading-more {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 30rpx;
  
  .loading-text {
    margin-left: 12rpx;
    font-size: 24rpx;
    color: #999;
  }
}

.no-more {
  text-align: center;
  padding: 30rpx;
  font-size: 24rpx;
  color: #999;
}
</style>
