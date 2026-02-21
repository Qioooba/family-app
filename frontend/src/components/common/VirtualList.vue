<template>
  <view class="virtual-list-container">
    <!-- 总高度占位 -->
    <view 
      class="virtual-list-content"
      :style="{ height: totalHeight + 'px' }"
    >
      <!-- 可视区域内容 -->
      <view 
        class="virtual-list-items"
        :style="contentStyle"
      >
        <view
          v-for="item in visibleData"
          :key="item[keyProp] || item._index"
          class="virtual-list-item"
          :style="{ height: itemHeight + 'px' }"
        >
          <slot :item="item" :index="item._index">
            <view class="default-item">
              {{ item[labelProp] || item.name || `Item ${item._index}` }}
            </view>
          </slot>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, watch, onMounted, nextTick } from 'vue'

const props = defineProps({
  // 完整数据列表
  data: {
    type: Array,
    default: () => []
  },
  // 每项高度（像素）
  itemHeight: {
    type: Number,
    default: 80
  },
  // 可视区域外缓冲数量
  bufferSize: {
    type: Number,
    default: 5
  },
  // 容器高度（像素）
  containerHeight: {
    type: Number,
    default: 600
  },
  // 唯一键字段
  keyProp: {
    type: String,
    default: 'id'
  },
  // 显示文本字段
  labelProp: {
    type: String,
    default: 'name'
  }
})

const emit = defineEmits(['scroll', 'scrolltolower'])

const scrollTop = ref(0)
const listContainerRef = ref(null)

// 计算总高度
const totalHeight = computed(() => {
  return props.data.length * props.itemHeight
})

// 可视区域能显示的项目数
const visibleCount = computed(() => {
  return Math.ceil(props.containerHeight / props.itemHeight) + props.bufferSize * 2
})

// 起始索引
const startIndex = computed(() => {
  return Math.max(0, Math.floor(scrollTop.value / props.itemHeight) - props.bufferSize)
})

// 结束索引
const endIndex = computed(() => {
  return Math.min(props.data.length, startIndex.value + visibleCount.value)
})

// 可视区域数据
const visibleData = computed(() => {
  return props.data.slice(startIndex.value, endIndex.value).map((item, index) => ({
    ...item,
    _index: startIndex.value + index
  }))
})

// 内容偏移样式
const contentStyle = computed(() => ({
  transform: `translateY(${startIndex.value * props.itemHeight}px)`
}))

// 滚动事件处理
const onScroll = (e) => {
  const newScrollTop = e.detail?.scrollTop || e.target?.scrollTop || 0
  scrollTop.value = newScrollTop
  emit('scroll', { scrollTop: newScrollTop })
  
  // 判断是否滚动到底部
  const scrollHeight = totalHeight.value
  const clientHeight = props.containerHeight
  if (newScrollTop + clientHeight >= scrollHeight - 50) {
    emit('scrolltolower')
  }
}

// 滚动到指定索引
const scrollToIndex = (index) => {
  scrollTop.value = index * props.itemHeight
}

// 滚动到指定位置
const scrollTo = (position) => {
  scrollTop.value = position
}

// 重置滚动位置
const resetScroll = () => {
  scrollTop.value = 0
}

// 暴露方法
defineExpose({
  scrollToIndex,
  scrollTo,
  resetScroll,
  onScroll
})

// 监听数据变化，重置滚动位置
watch(() => props.data.length, (newLen, oldLen) => {
  if (newLen < oldLen) {
    resetScroll()
  }
})
</script>

<style lang="scss" scoped>
.virtual-list-container {
  height: 100%;
  overflow: hidden;
  position: relative;
}

.virtual-list-content {
  position: relative;
}

.virtual-list-items {
  position: absolute;
  left: 0;
  right: 0;
  will-change: transform;
}

.virtual-list-item {
  box-sizing: border-box;
}

.default-item {
  display: flex;
  align-items: center;
  padding: 0 20rpx;
  height: 100%;
  border-bottom: 1rpx solid #eee;
  font-size: 28rpx;
  color: #333;
}
</style>
