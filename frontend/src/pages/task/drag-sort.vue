<template>
  <view class="drag-sort-page">
    <!-- 顶部导航 -->
    <view class="nav-bar">
      <view class="back-btn" @click="goBack">
        <up-icon name="arrow-left" size="40" color="#333"></up-icon>
      </view>
      <text class="title">调整优先级</text>
      <view class="right-btn" @click="saveOrder">
        <text>保存</text>
      </view>
    </view>

    <!-- 提示信息 -->
    <view class="tips-bar">
      <up-icon name="info-circle" size="32" color="#5B8FF9"></up-icon>
      <text class="tips-text">长按拖动调整任务顺序，优先级自动更新</text>
    </view>

    <!-- 任务列表 -->
    <scroll-view 
      class="task-list" 
      scroll-y 
      enhanced 
      :show-scrollbar="false"
      :style="{ height: listHeight + 'px' }"
    >
      <view class="list-container" ref="listContainer">
        <view
          v-for="(task, index) in taskList"
          :key="task.id"
          class="task-item"
          :class="{ 
            dragging: draggingIndex === index,
            'drag-over': dragOverIndex === index && draggingIndex !== index
          }"
          :style="getItemStyle(index)"
          @touchstart="onTouchStart($event, index)"
          @touchmove="onTouchMove($event, index)"
          @touchend="onTouchEnd"
          @longpress="onLongPress(index)"
        >
          <!-- 拖拽手柄 -->
          <view class="drag-handle">
            <view class="drag-icon">
              <view class="dot"></view>
              <view class="dot"></view>
              <view class="dot"></view>
            </view>
            <text class="priority-num">{{ index + 1 }}</text>
          </view>

          <!-- 任务内容 -->
          <view class="task-content">
            <view class="task-header">
              <text class="task-title" :class="{ completed: task.status === 1 }">{{ task.title }}</text>
              <view class="priority-badge" :class="getPriorityClass(task.priority)">
                {{ getPriorityLabel(task.priority) }}
              </view>
            </view>
            <view class="task-meta">
              <text class="meta-item" v-if="task.category">
                <up-icon name="tags" size="20" color="#999"></up-icon>
                {{ getCategoryLabel(task.category) }}
              </text>
              <text class="meta-item" v-if="task.deadline">
                <up-icon name="clock" size="20" color="#999"></up-icon>
                {{ formatDate(task.deadline) }}
              </text>
              <text class="meta-item" v-if="task.assigneeName">
                <up-icon name="account" size="20" color="#999"></up-icon>
                {{ task.assigneeName }}
              </text>
            </view>
          </view>

          <!-- 拖拽时的浮动提示 -->
          <view v-if="draggingIndex === index" class="floating-badge">
            <text>移动到第 {{ dragOverIndex + 1 }} 位</text>
          </view>
        </view>
      </view>
    </scroll-view>

    <!-- 底部操作栏 -->
    <view class="bottom-bar">
      <view class="action-btn" @click="resetOrder">
        <up-icon name="reload" size="32" color="#666"></up-icon>
        <text>重置顺序</text>
      </view>
      <view class="action-btn" @click="sortByPriority">
        <up-icon name="sort" size="32" color="#666"></up-icon>
        <text>按优先级排序</text>
      </view>
      <view class="action-btn" @click="sortByDeadline">
        <up-icon name="calendar" size="32" color="#666"></up-icon>
        <text>按截止日期</text>
      </view>
    </view>

    <!-- 拖动时的占位提示 -->
    <view v-if="isDragging" class="drag-overlay">
      <view class="drag-placeholder" :style="placeholderStyle">
        <text>{{ taskList[draggingIndex]?.title }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import dayjs from 'dayjs'

// 响应式数据
const taskList = ref([])
const originalList = ref([])
const isDragging = ref(false)
const draggingIndex = ref(-1)
const dragOverIndex = ref(-1)
const startY = ref(0)
const currentY = ref(0)
const itemHeight = ref(120) // 每个任务项的高度
const listContainer = ref(null)
const listHeight = ref(600)

// 页面加载时获取任务列表
onMounted(() => {
  loadTasks()
  calculateListHeight()
})

// 计算列表高度
const calculateListHeight = () => {
  const systemInfo = uni.getSystemInfoSync()
  // 减去导航栏、提示栏和底部操作栏的高度
  listHeight.value = systemInfo.windowHeight - 180 - 100 - 120
}

// 加载任务列表
const loadTasks = () => {
  // 从本地存储或页面参数获取
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const options = currentPage.$page?.options || currentPage.options || {}
  
  if (options.tasks) {
    try {
      const tasks = JSON.parse(decodeURIComponent(options.tasks))
      taskList.value = tasks
      originalList.value = JSON.parse(JSON.stringify(tasks))
    } catch (e) {
      console.error('解析任务列表失败', e)
      loadMockTasks()
    }
  } else {
    loadMockTasks()
  }
}

// 模拟数据（开发测试用）
const loadMockTasks = () => {
  taskList.value = [
    { id: 1, title: '去超市买菜', priority: 2, status: 0, category: 'shopping', deadline: '2026-02-22 18:00', assigneeName: '爸爸' },
    { id: 2, title: '整理客厅', priority: 1, status: 0, category: 'housework', deadline: '2026-02-22 20:00', assigneeName: '妈妈' },
    { id: 3, title: '陪孩子做作业', priority: 2, status: 0, category: 'parenting', deadline: '2026-02-22 19:30', assigneeName: '爸爸' },
    { id: 4, title: '缴纳水电费', priority: 0, status: 0, category: 'finance', deadline: '2026-02-25', assigneeName: '妈妈' },
    { id: 5, title: '预约牙医', priority: 1, status: 0, category: 'other', deadline: '2026-02-23', assigneeName: '爸爸' },
    { id: 6, title: '清洗空调滤网', priority: 0, status: 0, category: 'housework', deadline: '2026-02-24', assigneeName: '爸爸' },
    { id: 7, title: '购买学习用品', priority: 1, status: 0, category: 'shopping', deadline: '2026-02-23', assigneeName: '妈妈' },
    { id: 8, title: '准备周末出游计划', priority: 0, status: 0, category: 'other', deadline: '2026-02-25', assigneeName: '爸爸' }
  ]
  originalList.value = JSON.parse(JSON.stringify(taskList.value))
}

// 获取优先级样式
const getPriorityClass = (priority) => {
  const classes = ['priority-low', 'priority-medium', 'priority-high']
  return classes[priority] || 'priority-low'
}

// 获取优先级标签
const getPriorityLabel = (priority) => {
  const labels = ['普通', '重要', '紧急']
  return labels[priority] || '普通'
}

// 获取分类标签
const getCategoryLabel = (category) => {
  const labels = {
    shopping: '购物',
    housework: '家务',
    finance: '财务',
    parenting: '育儿',
    other: '其他'
  }
  return labels[category] || '其他'
}

// 格式化日期
const formatDate = (date) => {
  return dayjs(date).format('MM-DD HH:mm')
}

// 长按开始拖拽
const onLongPress = (index) => {
  // #ifdef APP-PLUS || H5
  startDrag(index)
  // #endif
}

// 触摸开始
const onTouchStart = (e, index) => {
  startY.value = e.touches[0].clientY
  
  // #ifdef MP-WEIXIN
  // 小程序使用长按触发
  // #endif
}

// 触摸移动
const onTouchMove = (e, index) => {
  if (!isDragging.value) return
  
  e.preventDefault()
  currentY.value = e.touches[0].clientY
  
  // 计算拖动的位置
  const deltaY = currentY.value - startY.value
  
  // 计算目标位置
  const newIndex = Math.round((draggingIndex.value * itemHeight.value + deltaY) / itemHeight.value)
  
  // 限制范围
  if (newIndex >= 0 && newIndex < taskList.value.length && newIndex !== dragOverIndex.value) {
    dragOverIndex.value = newIndex
  }
}

// 触摸结束
const onTouchEnd = () => {
  if (!isDragging.value) return
  
  // 交换位置
  if (dragOverIndex.value !== -1 && dragOverIndex.value !== draggingIndex.value) {
    const item = taskList.value.splice(draggingIndex.value, 1)[0]
    taskList.value.splice(dragOverIndex.value, 0, item)
  }
  
  // 重置状态
  isDragging.value = false
  draggingIndex.value = -1
  dragOverIndex.value = -1
  startY.value = 0
  currentY.value = 0
}

// 开始拖拽（App和H5）
const startDrag = (index) => {
  isDragging.value = true
  draggingIndex.value = index
  dragOverIndex.value = index
  
  // 振动反馈
  // #ifdef APP-PLUS
  uni.vibrateShort()
  // #endif
}

// 获取项目样式
const getItemStyle = (index) => {
  if (draggingIndex.value === index) {
    return {
      opacity: 0.3,
      transform: 'scale(0.95)'
    }
  }
  if (dragOverIndex.value === index && draggingIndex.value !== -1) {
    return {
      borderTop: '4rpx solid #5B8FF9',
      background: '#f0f5ff'
    }
  }
  return {}
}

// 占位符样式
const placeholderStyle = computed(() => {
  if (draggingIndex.value === -1) return {}
  
  const deltaY = currentY.value - startY.value
  return {
    transform: `translateY(${deltaY}px)`
  }
})

// 保存顺序
const saveOrder = async () => {
  // 更新优先级
  const updatedTasks = taskList.value.map((task, index) => ({
    ...task,
    sortOrder: index,
    priority: Math.min(2, Math.floor(index / 3)) // 根据位置重新分配优先级
  }))
  
  try {
    // 调用API保存
    // await taskApi.batchUpdatePriority(updatedTasks)
    
    // 通知上一页
    const eventChannel = getOpenerEventChannel()
    if (eventChannel && eventChannel.emit) {
      eventChannel.emit('onOrderChanged', updatedTasks)
    }
    
    uni.showToast({
      title: '保存成功',
      icon: 'success'
    })
    
    setTimeout(() => {
      uni.navigateBack()
    }, 1500)
  } catch (e) {
    uni.showToast({
      title: '保存失败',
      icon: 'none'
    })
  }
}

// 重置顺序
const resetOrder = () => {
  uni.showModal({
    title: '提示',
    content: '确定要重置为初始顺序吗？',
    success: (res) => {
      if (res.confirm) {
        taskList.value = JSON.parse(JSON.stringify(originalList.value))
        uni.showToast({
          title: '已重置',
          icon: 'success'
        })
      }
    }
  })
}

// 按优先级排序
const sortByPriority = () => {
  taskList.value.sort((a, b) => b.priority - a.priority)
  uni.showToast({
    title: '已按优先级排序',
    icon: 'none'
  })
}

// 按截止日期排序
const sortByDeadline = () => {
  taskList.value.sort((a, b) => {
    if (!a.deadline) return 1
    if (!b.deadline) return -1
    return dayjs(a.deadline).valueOf() - dayjs(b.deadline).valueOf()
  })
  uni.showToast({
    title: '已按截止日期排序',
    icon: 'none'
  })
}

const goBack = () => {
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
.drag-sort-page {
  min-height: 100vh;
  background: #f5f6fa;
}

.nav-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 30rpx;
  padding-top: 60rpx;
  background: #fff;
  border-bottom: 1rpx solid #f0f0f0;

  .back-btn {
    width: 60rpx;
    height: 60rpx;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .title {
    font-size: 34rpx;
    font-weight: 600;
    color: #333;
  }

  .right-btn {
    padding: 12rpx 30rpx;
    background: #5B8FF9;
    border-radius: 30rpx;

    text {
      font-size: 28rpx;
      color: #fff;
    }
  }
}

.tips-bar {
  display: flex;
  align-items: center;
  gap: 12rpx;
  padding: 20rpx 30rpx;
  background: #f0f5ff;

  .tips-text {
    font-size: 26rpx;
    color: #5B8FF9;
  }
}

.task-list {
  padding: 20rpx 30rpx;
}

.list-container {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.task-item {
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  transition: all 0.2s;
  position: relative;

  &.dragging {
    box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.1);
  }

  &.drag-over {
    border-top: 4rpx solid #5B8FF9;
  }
}

.drag-handle {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-right: 24rpx;
  padding: 12rpx;

  .drag-icon {
    display: flex;
    flex-direction: column;
    gap: 6rpx;
    margin-bottom: 8rpx;

    .dot {
      width: 8rpx;
      height: 8rpx;
      border-radius: 50%;
      background: #ccc;
    }
  }

  .priority-num {
    font-size: 24rpx;
    font-weight: 600;
    color: #999;
    width: 40rpx;
    height: 40rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #f5f6fa;
    border-radius: 50%;
  }
}

.task-content {
  flex: 1;
  min-width: 0;
}

.task-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12rpx;

  .task-title {
    font-size: 30rpx;
    font-weight: 500;
    color: #333;
    flex: 1;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;

    &.completed {
      text-decoration: line-through;
      color: #999;
    }
  }

  .priority-badge {
    padding: 6rpx 16rpx;
    border-radius: 8rpx;
    font-size: 22rpx;
    margin-left: 16rpx;
    flex-shrink: 0;

    &.priority-low {
      background: #E6F7FF;
      color: #1890FF;
    }

    &.priority-medium {
      background: #FFF7E6;
      color: #FAAD14;
    }

    &.priority-high {
      background: #FFF1F0;
      color: #FF4D4F;
    }
  }
}

.task-meta {
  display: flex;
  gap: 24rpx;
  flex-wrap: wrap;

  .meta-item {
    display: flex;
    align-items: center;
    gap: 6rpx;
    font-size: 24rpx;
    color: #999;
  }
}

.floating-badge {
  position: absolute;
  right: 24rpx;
  top: 50%;
  transform: translateY(-50%);
  background: #5B8FF9;
  padding: 8rpx 16rpx;
  border-radius: 8rpx;

  text {
    font-size: 22rpx;
    color: #fff;
  }
}

.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  justify-content: space-around;
  padding: 20rpx 30rpx;
  padding-bottom: calc(20rpx + constant(safe-area-inset-bottom));
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
  background: #fff;
  border-top: 1rpx solid #f0f0f0;

  .action-btn {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8rpx;
    padding: 12rpx 24rpx;

    text {
      font-size: 24rpx;
      color: #666;
    }
  }
}

.drag-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
  z-index: 999;
}

.drag-placeholder {
  position: absolute;
  left: 30rpx;
  right: 30rpx;
  top: 200rpx;
  background: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.15);
  display: flex;
  align-items: center;
  justify-content: center;

  text {
    font-size: 30rpx;
    color: #333;
    font-weight: 500;
  }
}
</style>