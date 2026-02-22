<template>
  <view class="page-container">
    <!-- 头部 -->
    <view class="header">
      <view class="header-left" @click="goBack">
        <text class="back-icon">‹</text>
      </view>
      <view class="header-title">任务时间线</view>
      <view class="header-action" @click="showFilterModal">
        <text class="icon">🔍</text>
      </view>
    </view>
    
    <!-- 任务选择器 -->
    <view class="task-selector" @click="showTaskSelector">
      <view class="selected-task" v-if="currentTask">
        <text class="task-icon">📋</text>
        <view class="task-info"
>
          <text class="task-title">{{ currentTask.title }}</text>
          <text class="task-status" :class="'status-' + currentTask.status">{{ getStatusText(currentTask) }}</text>
        </view>
      </view>
      <view class="selected-task empty" v-else
>
        <text class="placeholder">选择任务查看历史</text>
      </view>
      <text class="arrow-icon">›</text>
    </view>
    
    <!-- 时间线统计 -->
    <view class="timeline-stats" v-if="currentTask">
      <view class="stat-item">
        <text class="stat-value">{{ timelineStats.totalEvents }}</text>
        <text class="stat-label">操作记录</text>
      </view>
      <view class="stat-divider"></text>
      <view class="stat-item"
>
        <text class="stat-value">{{ timelineStats.duration }}</text>
        <text class="stat-label">持续天数</text>
      </view>
      <view class="stat-divider"></text>
      <view class="stat-item">
        <text class="stat-value">{{ timelineStats.updates }}</text>
        <text class="stat-label">修改次数</text>
      </view>
    </view>
    
    <!-- 时间线主体 -->
    <scroll-view class="timeline-container" scroll-y v-if="currentTask">
      <view class="timeline"
>
        <!-- 时间线起点 -->
        <view class="timeline-start">
          <view class="start-node">
            <text class="start-icon">🚀</text>
          </view>
          <view class="start-content">
            <text class="start-title">任务创建</text>
            <text class="start-time">{{ formatDateTime(currentTask.createdAt) }}</text>
          </view>
        </view>
        
        <!-- 时间线事件 -->
        <view 
          v-for="(event, index) in timelineEvents" 
          :key="index"
          class="timeline-item"
          :class="{ 
            'is-first': index === 0,
            'is-last': index === timelineEvents.length - 1,
            'type-create': event.type === 'create',
            'type-update': event.type === 'update',
            'type-complete': event.type === 'complete',
            'type-assign': event.type === 'assign',
            'type-comment': event.type === 'comment',
            'type-reminder': event.type === 'reminder'
          }"
        >
          <view class="timeline-marker">
            <view class="marker-line-top"></text>
            <view class="marker-dot">
              <text class="marker-icon">{{ getEventIcon(event) }}</text>
            </view>
            <view class="marker-line-bottom"></text>
          </view>
          
          <view class="timeline-content"
>
            <view class="event-header">
              <text class="event-title">{{ event.title }}</text>
              <text class="event-time">{{ formatRelativeTime(event.time) }}</text>
            </view>
            
            <view class="event-detail" v-if="event.detail">
              <text>{{ event.detail }}</text>
            </view>
            
            <view class="event-changes" v-if="event.changes?.length"
>
              <view v-for="(change, cidx) in event.changes" :key="cidx" class="change-item">
                <text class="change-label">{{ change.field }}:</text>
                <view class="change-values"
>
                  <text class="old-value">{{ change.oldValue || '空' }}</text>
                  <text class="arrow">➜</text>
                  <text class="new-value">{{ change.newValue || '空' }}</text>
                </view>
              </view>
            </view>
            
            <view class="event-footer" v-if="event.user">
              <text class="user-name">👤 {{ event.user }}</text>
            </view>
          </view>
        </view>
        
        <!-- 时间线终点 -->
        <view class="timeline-end" v-if="currentTask.status === 2">
          <view class="end-node"
>
            <text class="end-icon">✓</text>
          </view>
          <view class="end-content">
            <text class="end-title">任务完成</text>
            <text class="end-time" v-if="currentTask.completedAt">{{ formatDateTime(currentTask.completedAt) }}</text>
          </view>
        </view>
        
        <!-- 当前状态 -->
        <view class="timeline-current" v-else
>
          <view class="current-node"
>
            <view class="pulse-ring"></text>
            <text class="current-icon">⏳</text>
          </view>
          <view class="current-content">
            <text class="current-title">进行中</text>
            <text class="current-desc">任务尚未完成</text>
          </view>
        </view>
      </view>
      
      <!-- 底部空间 -->
      <view class="bottom-space"></text>
    </scroll-view>
    
    <!-- 空状态 -->
    <view class="empty-state" v-else
>
      <text class="empty-icon">📊</text>
      <text class="empty-title">选择任务查看时间线</text>
      <text class="empty-desc">查看任务从创建到完成的完整历史记录</text>
      <view class="empty-action" @click="showTaskSelector"
>
        <text>选择任务</text>
      </view>
    </view>
    
    <!-- 任务选择弹窗 -->
    <view v-if="showTaskModal" class="modal-overlay" @click="closeTaskModal">
      <view class="modal-content" @click.stop>
        <view class="modal-header">
          <text class="modal-title">选择任务</text>
          <text class="close-btn" @click="closeTaskModal">✕</text>
        </view>
        
        <view class="search-bar">
          <input 
            class="search-input" 
            v-model="searchKeyword" 
            placeholder="搜索任务..."
            @input="filterTasks"
          />
        </view>
        
        <scroll-view class="task-list" scroll-y>
          <view 
            v-for="(task, index) in filteredTasks" :key="task.id || index" 
            
            class="task-option"
            :class="{ active: currentTask?.id === task.id }"
            @click="selectTask(task)"
          >
            <view class="option-status" :class="'status-' + task.status"></view>
            <view class="option-info">
              <text class="option-title">{{ task.title }}</text>
              <text class="option-date">{{ formatDate(task.createdAt) }}</text>
            </view>
            <text v-if="currentTask?.id === task.id" class="check-icon">✓</text>
          </view>
          
          <view v-if="filteredTasks.length === 0" class="no-result">
            <text>没有找到任务</text>
          </view>
        </scroll-view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { taskApi } from '../../api/index.js'
import dayjs from 'dayjs'

// 响应式数据
const tasks = ref([])
const currentTask = ref(null)
const showTaskModal = ref(false)
const searchKeyword = ref('')
const timelineEvents = ref([])

// 模拟时间线数据（实际应该从后端获取）
const mockTimelineData = {
  create: (task) => ([{
    type: 'create',
    title: '创建任务',
    time: task.createdAt,
    detail: `创建了任务 "${task.title}"`,
    user: task.creatorName || '我'
  }]),
  
  updates: [
    {
      type: 'update',
      title: '更新任务',
      time: null, // 动态生成
      changes: [
        { field: '标题', oldValue: '旧标题', newValue: '新标题' }
      ],
      user: '我'
    },
    {
      type: 'assign',
      title: '指派任务',
      time: null,
      detail: '将任务指派给 张三',
      user: '我'
    },
    {
      type: 'update',
      title: '修改截止日期',
      time: null,
      changes: [
        { field: '截止日期', oldValue: '2024-01-15', newValue: '2024-01-20' }
      ],
      user: '我'
    },
    {
      type: 'comment',
      title: '添加评论',
      time: null,
      detail: '这个任务需要尽快完成',
      user: '我'
    },
    {
      type: 'reminder',
      title: '设置提醒',
      time: null,
      detail: '设置了提前1天提醒',
      user: '我'
    },
    {
      type: 'update',
      title: '标记进行中',
      time: null,
      detail: '将任务状态改为进行中',
      user: '我'
    },
    {
      type: 'complete',
      title: '完成任务',
      time: null,
      detail: '标记任务为已完成',
      user: '我'
    }
  ]
}

// 计算属性
const filteredTasks = computed(() => {
  if (!searchKeyword.value) return tasks.value
  const kw = searchKeyword.value.toLowerCase()
  return tasks.value.filter(t => t.title?.toLowerCase().includes(kw))
})

const timelineStats = computed(() => {
  if (!currentTask.value) return { totalEvents: 0, duration: 0, updates: 0 }
  
  const totalEvents = timelineEvents.value.length + 1 // +1 for create event
  const duration = currentTask.value.createdAt 
    ? dayjs().diff(dayjs(currentTask.value.createdAt), 'day') + 1
    : 0
  const updates = timelineEvents.value.filter(e => e.type === 'update').length
  
  return { totalEvents, duration, updates }
})

// 方法
const loadTasks = async () => {
  try {
    const familyId = uni.getStorageSync('currentFamilyId') || 1
    const res = await taskApi.getList(familyId)
    tasks.value = res || []
  } catch (e) {
    console.error('加载任务失败', e)
  }
}

const loadTimeline = async (taskId) => {
  // 实际应该调用API获取时间线数据
  // const res = await taskApi.getTimeline(taskId)
  // timelineEvents.value = res || []
  
  // 使用模拟数据
  generateMockTimeline()
}

const generateMockTimeline = () => {
  if (!currentTask.value) return
  
  const events = [...mockTimelineData.create(currentTask.value)]
  
  // 根据任务状态生成对应的事件
  const baseTime = dayjs(currentTask.value.createdAt)
  
  if (currentTask.value.status >= 0) {
    // 待办状态
    events.push({
      ...mockTimelineData.updates[0],
      time: baseTime.add(1, 'hour').toISOString()
    })
  }
  
  if (currentTask.value.assigneeId) {
    events.push({
      ...mockTimelineData.updates[1],
      time: baseTime.add(2, 'hour').toISOString()
    })
  }
  
  if (currentTask.value.dueDate) {
    events.push({
      ...mockTimelineData.updates[2],
      time: baseTime.add(1, 'day').toISOString()
    })
  }
  
  if (currentTask.value.status >= 1) {
    events.push({
      ...mockTimelineData.updates[5],
      time: baseTime.add(2, 'day').toISOString()
    })
  }
  
  if (currentTask.value.status === 2) {
    events.push({
      ...mockTimelineData.updates[6],
      time: currentTask.value.completedAt || baseTime.add(3, 'day').toISOString()
    })
  }
  
  timelineEvents.value = events.slice(1) // 去掉创建事件，因为起点已经显示了
}

const getStatusText = (task) => {
  const map = { 0: '待办', 1: '进行中', 2: '已完成' }
  return map[task.status] || '待办'
}

const getEventIcon = (event) => {
  const icons = {
    create: '🚀',
    update: '✏️',
    complete: '✓',
    assign: '👤',
    comment: '💬',
    reminder: '🔔',
    delete: '🗑️'
  }
  return icons[event.type] || '📝'
}

const formatDate = (date) => {
  if (!date) return ''
  return dayjs(date).format('MM-DD')
}

const formatDateTime = (date) => {
  if (!date) return ''
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

const formatRelativeTime = (date) => {
  if (!date) return ''
  const d = dayjs(date)
  const now = dayjs()
  
  if (d.isSame(now, 'day')) return d.format('今天 HH:mm')
  if (d.isSame(now.subtract(1, 'day'), 'day')) return d.format('昨天 HH:mm')
  if (d.isSame(now.add(1, 'day'), 'day')) return d.format('明天 HH:mm')
  
  const diffDays = now.diff(d, 'day')
  if (diffDays < 7) return d.format('ddd HH:mm')
  if (diffDays < 30) return `${diffDays}天前`
  
  return d.format('MM-DD HH:mm')
}

const showTaskSelector = () => {
  showTaskModal.value = true
  searchKeyword.value = ''
}

const closeTaskModal = () => {
  showTaskModal.value = false
}

const selectTask = (task) => {
  currentTask.value = task
  loadTimeline(task.id)
  closeTaskModal()
}

const filterTasks = () => {
  // 计算属性自动处理
}

const showFilterModal = () => {
  uni.showActionSheet({
    itemList: ['按时间排序', '只看更新', '只看评论', '只看指派'],
    success: (res) => {
      uni.showToast({ title: '筛选功能开发中', icon: 'none' })
    }
  })
}

const goBack = () => {
  uni.navigateBack()
}

onMounted(() => {
  loadTasks()
  
  // 如果URL带taskId参数，自动加载
  const pages = getCurrentPages()
  const page = pages[pages.length - 1]
  const taskId = page?.$page?.options?.taskId
  
  if (taskId) {
    loadTasks().then(() => {
      const task = tasks.value.find(t => t.id == taskId)
      if (task) selectTask(task)
    })
  }
})
</script>

<style lang="scss" scoped>
.page-container {
  min-height: 100vh;
  background: linear-gradient(180deg, #F8FAFC 0%, #F1F5F9 100%);
}

// 头部
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 50px 20px 20px;
  background: linear-gradient(135deg, #8B5CF6 0%, #7C3AED 100%);
  
  .header-left {
    width: 40px;
    height: 40px;
    display: flex;
    align-items: center;
    justify-content: center;
    
    .back-icon {
      font-size: 32px;
      color: #fff;
      font-weight: 300;
    }
  }
  
  .header-title {
    font-size: 20px;
    font-weight: 600;
    color: #fff;
  }
  
  .header-action {
    width: 40px;
    height: 40px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: rgba(255,255,255,0.2);
    border-radius: 50%;
    
    .icon {
      font-size: 16px;
    }
  }
}

// 任务选择器
.task-selector {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  margin: 15px;
  padding: 16px;
  border-radius: 16px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  
  .selected-task {
    display: flex;
    align-items: center;
    gap: 12px;
    flex: 1;
    
    .task-icon {
      font-size: 24px;
    }
    
    .task-info {
      flex: 1;
      
      .task-title {
        font-size: 16px;
        font-weight: 600;
        color: #1F2937;
        display: block;
        margin-bottom: 4px;
      }
      
      .task-status {
        font-size: 12px;
        padding: 2px 8px;
        border-radius: 8px;
        
        &.status-0 {
          background: #F3F4F6;
          color: #6B7280;
        }
        
        &.status-1 {
          background: #DBEAFE;
          color: #2563EB;
        }
        &.status-2 {
          background: #D1FAE5;
          color: #059669;
        }
      }
    }
    
    &.empty .placeholder {
      font-size: 15px;
      color: #9CA3AF;
    }
  }
  
  .arrow-icon {
    font-size: 20px;
    color: #9CA3AF;
  }
}

// 时间线统计
.timeline-stats {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20px;
  padding: 15px;
  margin: 0 15px 15px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  
  .stat-item {
    text-align: center;
    
    .stat-value {
      font-size: 24px;
      font-weight: 700;
      color: #7C3AED;
      display: block;
    }
    
    .stat-label {
      font-size: 12px;
      color: #9CA3AF;
    }
  }
  
  .stat-divider {
    width: 1px;
    height: 30px;
    background: #E5E7EB;
  }
}

// 时间线容器
.timeline-container {
  height: calc(100vh - 280px);
  padding: 0 15px;
}

.timeline {
  padding: 20px 0;
}

// 时间线起点
.timeline-start {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding-bottom: 20px;
  
  .start-node {
    width: 40px;
    height: 40px;
    background: linear-gradient(135deg, #8B5CF6, #7C3AED);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
    
    .start-icon {
      font-size: 18px;
    }
  }
  
  .start-content {
    flex: 1;
    padding-top: 8px;
    
    .start-title {
      font-size: 16px;
      font-weight: 600;
      color: #1F2937;
      display: block;
    }
    
    .start-time {
      font-size: 13px;
      color: #9CA3AF;
      margin-top: 4px;
      display: block;
    }
  }
}

// 时间线事件
.timeline-item {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  
  .timeline-marker {
    width: 40px;
    display: flex;
    flex-direction: column;
    align-items: center;
    flex-shrink: 0;
    
    .marker-line-top,
    .marker-line-bottom {
      width: 2px;
      flex: 1;
      background: #E5E7EB;
    }
    
    .marker-dot {
      width: 32px;
      height: 32px;
      background: #fff;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      margin: 8px 0;
      box-shadow: 0 2px 8px rgba(0,0,0,0.1);
      
      .marker-icon {
        font-size: 14px;
      }
    }
  }
  
  .timeline-content {
    flex: 1;
    background: #fff;
    border-radius: 16px;
    padding: 16px;
    margin-bottom: 12px;
    box-shadow: 0 1px 3px rgba(0,0,0,0.08);
    
    .event-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 8px;
      
      .event-title {
        font-size: 15px;
        font-weight: 600;
        color: #1F2937;
      }
      
      .event-time {
        font-size: 12px;
        color: #9CA3AF;
      }
    }
    
    .event-detail {
      font-size: 13px;
      color: #6B7280;
      line-height: 1.5;
      margin-bottom: 12px;
    }
    
    .event-changes {
      background: #F9FAFB;
      border-radius: 12px;
      padding: 12px;
      margin-bottom: 12px;
      
      .change-item {
        margin-bottom: 8px;
        
        &:last-child {
          margin-bottom: 0;
        }
        
        .change-label {
          font-size: 12px;
          color: #9CA3AF;
          display: block;
          margin-bottom: 4px;
        }
        
        .change-values {
          display: flex;
          align-items: center;
          gap: 8px;
          
          .old-value {
            font-size: 13px;
            color: #6B7280;
            background: #F3F4F6;
            padding: 4px 10px;
            border-radius: 6px;
            text-decoration: line-through;
          }
          
          .arrow {
            color: #9CA3AF;
            font-size: 12px;
          }
          
          .new-value {
            font-size: 13px;
            color: #059669;
            background: #D1FAE5;
            padding: 4px 10px;
            border-radius: 6px;
            font-weight: 500;
          }
        }
      }
    }
    
    .event-footer {
      .user-name {
        font-size: 12px;
        color: #9CA3AF;
      }
    }
  }
  
  // 不同类型的事件样式
  &.type-create .marker-dot {
    background: #EDE9FE;
  }
  
  &.type-complete .marker-dot {
    background: #D1FAE5;
  }
  
  &.type-update .marker-dot {
    background: #DBEAFE;
  }
  
  &.type-assign .marker-dot {
    background: #FEF3C7;
  }
  
  &.type-comment .marker-dot {
    background: #E0E7FF;
  }
  
  &.type-reminder .marker-dot {
    background: #FEE2E2;
  }
}

// 时间线终点
.timeline-end {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding-top: 20px;
  
  .end-node {
    width: 40px;
    height: 40px;
    background: linear-gradient(135deg, #10B981, #059669);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
    
    .end-icon {
      font-size: 18px;
      color: #fff;
      font-weight: bold;
    }
  }
  
  .end-content {
    flex: 1;
    padding-top: 8px;
    
    .end-title {
      font-size: 16px;
      font-weight: 600;
      color: #059669;
      display: block;
    }
    
    .end-time {
      font-size: 13px;
      color: #9CA3AF;
      margin-top: 4px;
      display: block;
    }
  }
}

// 当前状态
.timeline-current {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding-top: 20px;
  
  .current-node {
    width: 40px;
    height: 40px;
    background: linear-gradient(135deg, #F59E0B, #D97706);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
    position: relative;
    
    .pulse-ring {
      position: absolute;
      width: 100%;
      height: 100%;
      border-radius: 50%;
      background: rgba(245, 158, 11, 0.3);
      animation: pulse 2s infinite;
    }
    
    .current-icon {
      font-size: 18px;
      position: relative;
      z-index: 1;
    }
  }
  
  .current-content {
    flex: 1;
    padding-top: 8px;
    
    .current-title {
      font-size: 16px;
      font-weight: 600;
      color: #D97706;
      display: block;
    }
    
    .current-desc {
      font-size: 13px;
      color: #9CA3AF;
      margin-top: 4px;
      display: block;
    }
  }
}

@keyframes pulse {
  0% {
    transform: scale(1);
    opacity: 1;
  }
  100% {
    transform: scale(1.5);
    opacity: 0;
  }
}

// 空状态
.empty-state {
  text-align: center;
  padding: 80px 40px;
  
  .empty-icon {
    font-size: 64px;
    margin-bottom: 20px;
    display: block;
  }
  
  .empty-title {
    font-size: 18px;
    font-weight: 600;
    color: #1F2937;
    margin-bottom: 8px;
    display: block;
  }
  
  .empty-desc {
    font-size: 14px;
    color: #9CA3AF;
    margin-bottom: 24px;
    display: block;
  }
  
  .empty-action {
    display: inline-block;
    background: linear-gradient(135deg, #8B5CF6, #7C3AED);
    color: #fff;
    padding: 12px 32px;
    border-radius: 24px;
    font-size: 15px;
    font-weight: 500;
  }
}

// 弹窗样式
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
}

.modal-content {
  width: 100%;
  max-height: 70vh;
  background: #fff;
  border-radius: 24px;
  overflow: hidden;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  background: linear-gradient(135deg, #8B5CF6, #7C3AED);
  
  .modal-title {
    font-size: 18px;
    font-weight: 600;
    color: #fff;
  }
  
  .close-btn {
    font-size: 20px;
    color: rgba(255,255,255,0.8);
  }
}

.search-bar {
  padding: 15px;
  border-bottom: 1px solid #F3F4F6;
  
  .search-input {
    width: 100%;
    height: 44px;
    background: #F3F4F6;
    border-radius: 22px;
    padding: 0 20px;
    font-size: 15px;
  }
}

.task-list {
  max-height: 400px;
  padding: 10px;
}

.task-option {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px;
  border-radius: 12px;
  margin-bottom: 8px;
  
  &.active {
    background: #EDE9FE;
  }
  
  .option-status {
    width: 10px;
    height: 10px;
    border-radius: 50%;
    
    &.status-0 { background: #6B7280; }
    &.status-1 { background: #3B82F6; }
    &.status-2 { background: #10B981; }
  }
  
  .option-info {
    flex: 1;
    
    .option-title {
      font-size: 15px;
      color: #1F2937;
      display: block;
      margin-bottom: 4px;
    }
    
    .option-date {
      font-size: 12px;
      color: #9CA3AF;
    }
  }
  
  .check-icon {
    font-size: 16px;
    color: #7C3AED;
    font-weight: bold;
  }
}

.no-result {
  text-align: center;
  padding: 40px;
  
  text {
    font-size: 14px;
    color: #9CA3AF;
  }
}

.bottom-space {
  height: 40px;
}
</style>
