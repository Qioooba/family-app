<template>
  <view class="task-detail-page">
    <!-- 顶部导航 -->
    <view class="nav-bar">
      <view class="back-btn" @click="goBack">
        <u-icon name="arrow-left" size="40" color="#333"></u-icon>
      </view>
      <text class="title">任务详情</text>
      <view class="more-btn" @click="showActions">
        <u-icon name="more-dot-fill" size="40" color="#333"></u-icon>
      </view>
    </view>
    
    <view class="detail-container">
      <!-- 任务头部 -->
      <view class="task-header" :class="{ completed: task.status === 2 }">
        <view class="status-badge" :class="getStatusClass">{{ getStatusText }}</view>
        <view class="task-title">
          <text :class="{ 'line-through': task.status === 2 }">{{ task.title }}</text>
        </view>
        
        <view class="task-meta">
          <view class="meta-item">
            <u-icon name="clock" size="28" color="#999"></u-icon>
            <text>截止: {{ task.deadline }}</text>
          </view>
          <view class="meta-item">
            <u-icon name="tag" size="28" color="#999"></u-icon>
            <text>{{ getCategoryText }}</text>
          </view>
        </view>
      </view>
      
      <!-- 执行人 -->
      <view class="section-card">
        <view class="section-title">执行人</view>
        <view class="assignee-info">
          <image :src="task.assigneeAvatar" class="avatar" />
          <view class="info">
            <text class="name">{{ task.assigneeName }}</text>
            <text class="time">创建于 {{ task.createTime }}</text>
          </view>
        </view>
      </view>
      
      <!-- 优先级 -->
      <view class="section-card">
        <view class="section-title">优先级</view>
        <view class="priority-display" :class="getPriorityClass">
          <view class="priority-dot"></view>
          <text>{{ getPriorityText }}</text>
        </view>
      </view>
      
      <!-- 备注 -->
      <view class="section-card" v-if="task.remark">
        <view class="section-title">备注</view>
        <view class="remark-content">{{ task.remark }}</view>
      </view>
      
      <!-- 子任务 -->
      <view class="section-card subtask-section">
        <view class="section-title">
          <text>子任务</text>
          <text class="subtask-count" v-if="subtasks.length > 0">
            {{ completedSubtasks }}/{{ subtasks.length }}
          </text>
        </view>
        
        <!-- 进度条 -->
        <view v-if="subtasks.length > 0" class="progress-bar">
          <view class="progress-fill" :style="{ width: subtaskProgress + '%' }"></view>
        </view>
        <text v-if="subtasks.length > 0" class="progress-text">{{ subtaskProgress }}%</text>
        
        <!-- 子任务列表 -->
        <view class="subtask-list">
          <view 
            v-for="(sub, index) in subtasks" 
            :key="sub.id"
            class="subtask-item"
          >
            <view 
              class="subtask-checkbox" 
              :class="{ checked: sub.status === 1 }"
              @click="toggleSubtask(sub)"
            >
              <u-icon v-if="sub.status === 1" name="checkmark" size="24" color="#fff"></u-icon>
            </view>
            <text class="subtask-title" :class="{ completed: sub.status === 1 }">{{ sub.title }}</text>
            <view class="subtask-delete" @click="deleteSubtask(sub.id, index)">
              <u-icon name="close" size="28" color="#999"></u-icon>
            </view>
          </view>
        </view>
        
        <!-- 添加子任务 -->
        <view class="add-subtask">
          <input 
            v-model="newSubtaskTitle"
            class="subtask-input"
            placeholder="添加子任务..."
            confirm-type="done"
            @confirm="addSubtask"
          />
          <view class="add-btn" :class="{ active: newSubtaskTitle.trim() }" @click="addSubtask">
            <u-icon name="plus" size="32" color="#fff"></u-icon>
          </view>
        </view>
      </view>
      
      <!-- 操作按钮 -->
      <view class="action-buttons">
        <view 
          v-if="task.status !== 2" 
          class="btn btn-primary"
          @click="completeTask"
        >
          <u-icon name="checkmark" size="32" color="#fff"></u-icon>
          标记完成
        </view>
        <view 
          v-else 
          class="btn btn-secondary"
          @click="reopenTask"
        >
          重新打开
        </view>
        
        <view class="btn btn-danger" @click="deleteTask">
          删除任务
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { taskApi } from '../../api/index.js'

const taskId = ref(null)
const task = ref({
  id: 1,
  title: '买牛奶和鸡蛋',
  status: 0,
  deadline: '2026-02-21 18:00',
  category: 'shopping',
  priority: 1,
  assigneeName: '妈妈',
  assigneeAvatar: '/static/avatar/mom.png',
  createTime: '2026-02-21 09:00',
  remark: '记得买有机牛奶，鸡蛋要土鸡蛋'
})
const subtasks = ref([])
const newSubtaskTitle = ref('')
const loading = ref(false)

onMounted(() => {
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  taskId.value = currentPage.options.id
  // 加载任务详情
  if (taskId.value) {
    loadTaskDetail(taskId.value)
  }
})

// 加载任务详情
const loadTaskDetail = async (id) => {
  loading.value = true
  try {
    const res = await taskApi.getById(id)
    if (res) {
      task.value = res
    }
    // 加载子任务列表
    await loadSubtasks(id)
  } catch (e) {
    console.error('加载任务详情失败', e)
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

// 加载子任务列表
const loadSubtasks = async (id) => {
  try {
    const res = await taskApi.getSubtasks(id)
    subtasks.value = res || []
  } catch (e) {
    console.error('加载子任务失败', e)
    subtasks.value = []
  }
}

// 添加子任务
const addSubtask = async () => {
  const title = newSubtaskTitle.value.trim()
  if (!title) return
  
  try {
    const res = await taskApi.addSubtask({
      taskId: taskId.value,
      title: title,
      sortOrder: subtasks.value.length
    })
    
    // 添加成功，更新列表
    subtasks.value.push({
      id: res,
      taskId: taskId.value,
      title: title,
      status: 0,
      sortOrder: subtasks.value.length
    })
    
    newSubtaskTitle.value = ''
    uni.showToast({ title: '添加成功', icon: 'success' })
  } catch (e) {
    console.error('添加子任务失败', e)
    uni.showToast({ title: '添加失败', icon: 'none' })
  }
}

// 切换子任务状态
const toggleSubtask = async (sub) => {
  try {
    await taskApi.toggleSubtask(sub.id)
    sub.status = sub.status === 0 ? 1 : 0
    
    // 如果所有子任务完成，提示完成任务
    if (subtaskProgress.value === 100 && task.value.status !== 2) {
      uni.showModal({
        title: '子任务全部完成',
        content: '是否将主任务标记为完成？',
        success: (res) => {
          if (res.confirm) {
            completeTask()
          }
        }
      })
    }
  } catch (e) {
    console.error('切换子任务状态失败', e)
    uni.showToast({ title: '操作失败', icon: 'none' })
  }
}

// 删除子任务
const deleteSubtask = async (id, index) => {
  uni.showModal({
    title: '确认删除',
    content: '确定删除这个子任务吗？',
    confirmColor: '#FF4D4F',
    success: async (res) => {
      if (res.confirm) {
        try {
          await taskApi.deleteSubtask(id)
          subtasks.value.splice(index, 1)
          uni.showToast({ title: '已删除', icon: 'success' })
        } catch (e) {
          console.error('删除子任务失败', e)
          uni.showToast({ title: '删除失败', icon: 'none' })
        }
      }
    }
  })
}

const getStatusClass = computed(() => {
  const map = { 0: 'pending', 1: 'progress', 2: 'completed' }
  return map[task.value.status] || 'pending'
})

const getStatusText = computed(() => {
  const map = { 0: '待处理', 1: '进行中', 2: '已完成' }
  return map[task.value.status] || '待处理'
})

const getCategoryText = computed(() => {
  const map = {
    shopping: '购物',
    housework: '家务',
    finance: '财务',
    parenting: '育儿',
    other: '其他'
  }
  return map[task.value.category] || '其他'
})

const getPriorityClass = computed(() => {
  const map = { 0: 'normal', 1: 'medium', 2: 'high' }
  return map[task.value.priority] || 'normal'
})

const getPriorityText = computed(() => {
  const map = { 0: '普通', 1: '重要', 2: '紧急' }
  return map[task.value.priority] || '普通'
})

// 子任务计算属性
const completedSubtasks = computed(() => {
  return subtasks.value.filter(s => s.status === 1).length
})

const subtaskProgress = computed(() => {
  if (subtasks.value.length === 0) return 0
  return Math.round((completedSubtasks.value / subtasks.value.length) * 100)
})

const goBack = () => {
  uni.navigateBack()
}

const showActions = () => {
  uni.showActionSheet({
    itemList: ['编辑', '转发', '删除'],
    success: (res) => {
      if (res.tapIndex === 0) {
        uni.navigateTo({ url: `/pages/task/edit?id=${taskId.value}` })
      } else if (res.tapIndex === 2) {
        deleteTask()
      }
    }
  })
}

const completeTask = async () => {
  try {
    await taskApi.complete(taskId.value)
    task.value.status = 2
    uni.showToast({ title: '已完成', icon: 'success' })
  } catch (e) {
    console.error('完成任务失败', e)
    uni.showToast({ title: '操作失败', icon: 'none' })
  }
}

const reopenTask = async () => {
  try {
    // 重新打开任务 - 使用update API修改状态
    await taskApi.update({ id: taskId.value, status: 0 })
    task.value.status = 0
    uni.showToast({ title: '已重新打开', icon: 'success' })
  } catch (e) {
    console.error('重新打开任务失败', e)
    uni.showToast({ title: '操作失败', icon: 'none' })
  }
}

const deleteTask = async () => {
  uni.showModal({
    title: '确认删除',
    content: '删除后无法恢复，是否继续？',
    confirmColor: '#FF4D4F',
    success: async (res) => {
      if (res.confirm) {
        try {
          await taskApi.delete(taskId.value)
          uni.showToast({ title: '已删除', icon: 'success' })
          setTimeout(() => uni.navigateBack(), 1500)
        } catch (e) {
          console.error('删除任务失败', e)
          uni.showToast({ title: '删除失败', icon: 'none' })
        }
      }
    }
  })
}
</script>

<style lang="scss" scoped>
.task-detail-page {
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
  
  .back-btn, .more-btn {
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
}

.detail-container {
  padding: 30rpx;
}

.task-header {
  background: #fff;
  border-radius: 20rpx;
  padding: 40rpx;
  margin-bottom: 30rpx;
  
  .status-badge {
    display: inline-block;
    padding: 8rpx 20rpx;
    border-radius: 20rpx;
    font-size: 24rpx;
    margin-bottom: 20rpx;
    
    &.pending {
      background: #E6F7FF;
      color: #1890FF;
    }
    
    &.progress {
      background: #FFF7E6;
      color: #FAAD14;
    }
    
    &.completed {
      background: #F6FFED;
      color: #52C41A;
    }
  }
  
  .task-title {
    font-size: 40rpx;
    font-weight: 600;
    color: #333;
    margin-bottom: 30rpx;
    line-height: 1.5;
    
    .line-through {
      text-decoration: line-through;
      color: #999;
    }
  }
  
  .task-meta {
    display: flex;
    flex-wrap: wrap;
    gap: 20rpx;
    
    .meta-item {
      display: flex;
      align-items: center;
      gap: 8rpx;
      font-size: 26rpx;
      color: #666;
    }
  }
}

.section-card {
  background: #fff;
  border-radius: 20rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
  
  .section-title {
    font-size: 28rpx;
    font-weight: 600;
    color: #333;
    margin-bottom: 20rpx;
  }
}

.assignee-info {
  display: flex;
  align-items: center;
  gap: 20rpx;
  
  .avatar {
    width: 80rpx;
    height: 80rpx;
    border-radius: 50%;
  }
  
  .info {
    .name {
      font-size: 30rpx;
      color: #333;
      display: block;
      margin-bottom: 8rpx;
    }
    
    .time {
      font-size: 24rpx;
      color: #999;
    }
  }
}

.priority-display {
  display: inline-flex;
  align-items: center;
  gap: 12rpx;
  padding: 16rpx 30rpx;
  border-radius: 30rpx;
  font-size: 28rpx;
  
  &.normal {
    background: #E6F7FF;
    color: #1890FF;
    .priority-dot { background: #1890FF; }
  }
  
  &.medium {
    background: #FFF7E6;
    color: #FAAD14;
    .priority-dot { background: #FAAD14; }
  }
  
  &.high {
    background: #FFF1F0;
    color: #FF4D4F;
    .priority-dot { background: #FF4D4F; }
  }
  
  .priority-dot {
    width: 16rpx;
    height: 16rpx;
    border-radius: 50%;
  }
}

.remark-content {
  font-size: 28rpx;
  color: #666;
  line-height: 1.6;
}

// 子任务样式
.subtask-section {
  .section-title {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .subtask-count {
      font-size: 26rpx;
      color: #999;
      font-weight: normal;
    }
  }
  
  .progress-bar {
    height: 8rpx;
    background: #f0f0f0;
    border-radius: 4rpx;
    margin-bottom: 10rpx;
    overflow: hidden;
    
    .progress-fill {
      height: 100%;
      background: linear-gradient(90deg, #5AD8A6, #5B8FF9);
      border-radius: 4rpx;
      transition: width 0.3s ease;
    }
  }
  
  .progress-text {
    font-size: 24rpx;
    color: #5AD8A6;
    margin-bottom: 20rpx;
    display: block;
  }
  
  .subtask-list {
    margin-bottom: 20rpx;
    
    .subtask-item {
      display: flex;
      align-items: center;
      gap: 20rpx;
      padding: 20rpx 0;
      border-bottom: 1rpx solid #f5f5f5;
      
      &:last-child {
        border-bottom: none;
      }
      
      .subtask-checkbox {
        width: 40rpx;
        height: 40rpx;
        border-radius: 50%;
        border: 2rpx solid #ddd;
        display: flex;
        align-items: center;
        justify-content: center;
        flex-shrink: 0;
        transition: all 0.2s;
        
        &.checked {
          background: #5AD8A6;
          border-color: #5AD8A6;
        }
      }
      
      .subtask-title {
        flex: 1;
        font-size: 28rpx;
        color: #333;
        transition: all 0.2s;
        
        &.completed {
          text-decoration: line-through;
          color: #999;
        }
      }
      
      .subtask-delete {
        width: 50rpx;
        height: 50rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        opacity: 0.6;
        
        &:active {
          opacity: 1;
        }
      }
    }
  }
  
  .add-subtask {
    display: flex;
    align-items: center;
    gap: 16rpx;
    margin-top: 10rpx;
    
    .subtask-input {
      flex: 1;
      height: 70rpx;
      background: #f5f6fa;
      border-radius: 35rpx;
      padding: 0 30rpx;
      font-size: 28rpx;
      color: #333;
    }
    
    .add-btn {
      width: 70rpx;
      height: 70rpx;
      border-radius: 50%;
      background: #ccc;
      display: flex;
      align-items: center;
      justify-content: center;
      transition: all 0.2s;
      
      &.active {
        background: #5AD8A6;
      }
      
      &:active {
        transform: scale(0.95);
      }
    }
  }
}

.action-buttons {
  display: flex;
  gap: 20rpx;
  margin-top: 40rpx;
  
  .btn {
    flex: 1;
    height: 90rpx;
    border-radius: 45rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 30rpx;
    
    &-primary {
      background: #5AD8A6;
      color: #fff;
      gap: 12rpx;
    }
    
    &-secondary {
      background: #5B8FF9;
      color: #fff;
    }
    
    &-danger {
      background: #FFF1F0;
      color: #FF4D4F;
      border: 2rpx solid #FFCCC7;
    }
  }
}
</style>