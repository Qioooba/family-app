<template>
  <view class="page-container">
    <!-- 头部 -->
    <view class="header">
      <view class="header-left" @click="goBack">
        <text class="back-icon">‹</text>
      </view>
      <view class="header-title">任务甘特图</text>
      <view class="header-action" @click="showSettings">
        <text class="icon">⚙️</text>
      </view>
    </view>
    
    <!-- 统计概览 -->
    <view class="stats-overview">
      <view class="stat-item">
        <text class="stat-number">{{ ganttTasks.length }}</text>
        <text class="stat-label">显示任务</text>
      </view>
      <view class="stat-item">
        <text class="stat-number" :style="{ color: '#10B981' }">{{ completedCount }}</text>
        <text class="stat-label">已完成</text>
      </view>
      <view class="stat-item">
        <text class="stat-number" :style="{ color: '#F59E0B' }">{{ inProgressCount }}</text>
        <text class="stat-label">进行中</text>
      </view>
      <view class="stat-item">
        <text class="stat-number" :style="{ color: '#EF4444' }">{{ overdueCount }}</text>
        <text class="stat-label">已逾期</text>
      </view>
    </view>
    
    <!-- 时间范围选择器 -->
    <view class="time-range-selector">
      <view 
        v-for="range in timeRanges" 
        :key="range.value"
        class="range-option"
        :class="{ active: currentRange === range.value }"
        @click="changeTimeRange(range.value)"
      >
        <text>{{ range.label }}</text>
      </view>
    </view>
    
    <!-- 甘特图主体 -->
    <view class="gantt-container">
      <!-- 左侧任务列表 -->
      <view class="task-list-panel">
        <view class="panel-header">
          <text>任务名称</text>
        </view>
        <scroll-view class="task-list" scroll-y :scroll-top="scrollTop" @scroll="onTaskScroll">
          <view 
            v-for="(task, index) in ganttTasks" 
            :key="task.id"
            class="task-row"
            :class="{ 
              completed: task.status === 2,
              overdue: isTaskOverdue(task) && task.status !== 2,
              'high-priority': task.priority === 2
            }"
            @click="selectTask(task)"
          >
            <view class="task-row-content">
              <text class="task-name">{{ truncateText(task.title, 8) }}</text>
              <view class="task-badges">
                <text v-if="task.priority === 2" class="priority-badge high">急</text>
                <text v-if="isTaskOverdue(task) && task.status !== 2" class="overdue-badge">逾</text>
              </view>
            </view>
          </view>
          
          <view v-if="ganttTasks.length === 0" class="empty-tasks">
            <text>暂无任务</text>
          </view>
        </scroll-view>
      </view>
      
      <!-- 右侧时间轴 -->
      <view class="timeline-panel">
        <!-- 日期头部 -->
        <scroll-view 
          class="timeline-header" 
          scroll-x 
          :scroll-left="headerScrollLeft"
          @scroll="onHeaderScroll"
          scroll-with-animation
        >
          <view class="timeline-dates" :style="{ width: timelineWidth + 'px' }">
            <view 
              v-for="(date, index) in timelineDates" 
              :key="index"
              class="date-cell"
              :class="{ 
                'is-today': date.isToday,
                weekend: date.isWeekend
              }"
            >
              <text class="date-day">{{ date.day }}</text>
              <text class="date-week">{{ date.weekDay }}</text>
            </view>
          </view>
        </scroll-view>
        
        <!-- 甘特条区域 -->
        <scroll-view 
          class="gantt-bars-area" 
          scroll-y 
          scroll-x
          :scroll-top="scrollTop"
          :scroll-left="bodyScrollLeft"
          @scroll="onBodyScroll"
        >
          <view class="gantt-grid" :style="{ width: timelineWidth + 'px' }">
            <!-- 网格线 -->
            <view class="grid-lines">
              <view 
                v-for="(date, index) in timelineDates" 
                :key="index"
                class="grid-line"
                :class="{ 'is-today': date.isToday, weekend: date.isWeekend }"
              ></view>
            </view>
            
            <!-- 当前时间线 -->
            <view 
              v-if="currentTimePosition >= 0"
              class="current-time-line"
              :style="{ left: currentTimePosition + 'px' }"
            >
              <view class="time-indicator">现在</view>
            </view>
            
            <!-- 任务条 -->
            <view 
              v-for="(task, index) in ganttTasks" 
              :key="task.id"
              class="gantt-bar-row"
              @click="selectTask(task)"
            >
              <!-- 任务条 -->
              <view 
                v-if="getTaskBarStyle(task).visible"
                class="gantt-bar"
                :class="{
                  completed: task.status === 2,
                  overdue: isTaskOverdue(task) && task.status !== 2,
                  'high-priority': task.priority === 2 && task.status !== 2
                }"
                :style="getTaskBarStyle(task)"
                @click.stop="showTaskDetail(task)"
              >
                <text class="bar-text">{{ truncateText(task.title, 6) }}</text>
                <!-- 进度条 -->
                <view 
                  v-if="task.status === 2 || (task.subtasks && task.subtasks.length > 0)"
                  class="progress-overlay"
                  :style="{ width: getTaskProgress(task) + '%' }"
                ></view>
              </view>
              
              <!-- 里程碑标记 -->
              <view 
                v-if="task.isMilestone"
                class="milestone-marker"
                :style="{ left: getMilestonePosition(task) + 'px' }"
              >
                <text>🎯</text>
              </view>
            </view>
          </view>
        </scroll-view>
      </view>
    </view>
    
    <!-- 任务详情弹窗 -->
    <view v-if="showDetailModal" class="modal-overlay" @click="closeDetailModal">
      <view class="modal-content" @click.stop>
        <view class="modal-header">
          <view class="modal-title">
            <text class="task-icon">📋</text>
            <text>任务详情</text>
          </view>
          <text class="close-btn" @click="closeDetailModal">✕</text>
        </view>
        
        <view v-if="selectedTask" class="task-detail">
          <view class="detail-title" :class="{ completed: selectedTask.status === 2 }">{{ selectedTask.title }}</view>
          
          <view class="detail-timeline">
            <view class="timeline-item">
              <text class="timeline-label">开始时间</text>
              <text class="timeline-value">{{ selectedTask.startDate || selectedTask.dueDate || '未设置' }}</text>
            </view>
            <view class="timeline-arrow">➜</view>
            <view class="timeline-item">
              <text class="timeline-label">截止时间</text>
              <text class="timeline-value">{{ selectedTask.dueDate || '未设置' }}</text>
            </view>
            
            <view class="timeline-duration" v-if="selectedTask.duration">
              <text>{{ selectedTask.duration }}天</text>
            </view>
          </view>
          
          <view class="detail-info">
            <view class="info-row">
              <text class="info-label">状态</text>
              <view class="status-badge" :class="getStatusClass(selectedTask)">
                {{ getStatusText(selectedTask) }}
              </view>
            </view>
            
            <view class="info-row">
              <text class="info-label">优先级</text>
              <text class="info-value" :class="'priority-' + selectedTask.priority">{{ priorityText(selectedTask.priority) }}</text>
            </view>
            
            <view class="info-row">
              <text class="info-label">负责人</text>
              <text class="info-value">{{ selectedTask.assigneeName || '未指派' }}</text>
            </view>
            
            <view class="info-row">
              <text class="info-label">进度</text>
              <view class="progress-bar">
                <view class="progress-fill" :style="{ width: getTaskProgress(selectedTask) + '%' }"></view>
              </view>
              <text class="progress-text">{{ getTaskProgress(selectedTask) }}%</text>
            </view>
          </view>
          
          <view class="modal-actions">
            <view class="action-btn primary" @click="toggleTaskStatus(selectedTask)">
              {{ selectedTask.status === 2 ? '标记未完成' : '标记完成' }}
            </view>
            <view class="action-btn secondary" @click="goToDetail(selectedTask)">
              查看详情
            </view>
          </view>
        </view>
      </view>
    </view>
    
    <!-- 底部视图切换 -->
    <view class="bottom-nav">
      <view class="nav-item" @click="switchView('list')">
        <text class="nav-icon">📋</text>
        <text class="nav-text">列表</text>
      </view>
      <view class="nav-item" @click="switchView('calendar')">
        <text class="nav-icon">📅</text>
        <text class="nav-text">日历</text>
      </view>
      <view class="nav-item active">
        <text class="nav-icon">📊</text>
        <text class="nav-text">甘特图</text>
      </view>
      <view class="nav-item" @click="switchView('board')">
        <text class="nav-icon">📌</text>
        <text class="nav-text">看板</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { taskApi } from '../../api/index.js'
import dayjs from 'dayjs'

// 常量
const DAY_WIDTH = 50 // 每天宽度(px)
const ROW_HEIGHT = 50 // 每行高度(px)
const timeRanges = [
  { label: '一周', value: 'week' },
  { label: '两周', value: '2weeks' },
  { label: '一月', value: 'month' },
  { label: '三月', value: 'quarter' }
]
const priorities = ['普通', '重要', '紧急']

// 响应式数据
const tasks = ref([])
const currentRange = ref('month')
const selectedTask = ref(null)
const showDetailModal = ref(false)
const scrollTop = ref(0)
const headerScrollLeft = ref(0)
const bodyScrollLeft = ref(0)

// 时间范围计算
const dateRange = computed(() => {
  const today = dayjs()
  let start, end
  
  switch (currentRange.value) {
    case 'week':
      start = today.startOf('week')
      end = today.endOf('week')
      break
    case '2weeks':
      start = today.startOf('week')
      end = today.add(1, 'week').endOf('week')
      break
    case 'month':
      start = today.startOf('month').subtract(7, 'day')
      end = today.endOf('month').add(7, 'day')
      break
    case 'quarter':
      start = today.startOf('quarter').subtract(14, 'day')
      end = today.endOf('quarter').add(14, 'day')
      break
    default:
      start = today.startOf('month')
      end = today.endOf('month')
  }
  
  return { start, end }
})

// 时间线日期
const timelineDates = computed(() => {
  const dates = []
  const { start, end } = dateRange.value
  let current = start
  
  while (current.isBefore(end) || current.isSame(end, 'day')) {
    dates.push({
      date: current.format('YYYY-MM-DD'),
      day: current.format('D'),
      weekDay: getWeekDay(current.day()),
      isToday: current.isSame(dayjs(), 'day'),
      isWeekend: current.day() === 0 || current.day() === 6
    })
    current = current.add(1, 'day')
  }
  
  return dates
})

// 时间线总宽度
const timelineWidth = computed(() => {
  return timelineDates.value.length * DAY_WIDTH
})

// 甘特图任务（有过期日期的任务）
const ganttTasks = computed(() => {
  return tasks.value
    .filter(task => task.dueDate)
    .sort((a, b) => new Date(a.dueDate) - new Date(b.dueDate))
})

// 统计
const completedCount = computed(() => ganttTasks.value.filter(t => t.status === 2).length)
const inProgressCount = computed(() => ganttTasks.value.filter(t => t.status !== 2 && !isTaskOverdue(t)).length)
const overdueCount = computed(() => ganttTasks.value.filter(t => isTaskOverdue(t) && t.status !== 2).length)

// 当前时间线位置
const currentTimePosition = computed(() => {
  const today = dayjs()
  const { start } = dateRange.value
  
  if (today.isBefore(start, 'day') || today.isAfter(dateRange.value.end, 'day')) {
    return -1
  }
  
  const diffDays = today.diff(start, 'day')
  return diffDays * DAY_WIDTH + DAY_WIDTH / 2
})

// 方法
const getWeekDay = (day) => {
  const days = ['日', '一', '二', '三', '四', '五', '六']
  return days[day]
}

const truncateText = (text, length) => {
  if (!text) return ''
  return text.length > length ? text.substring(0, length) + '...' : text
}

const isTaskOverdue = (task) => {
  if (!task.dueDate || task.status === 2) return false
  return dayjs(task.dueDate).isBefore(dayjs(), 'day')
}

const getTaskBarStyle = (task) => {
  const { start, end } = dateRange.value
  const taskStart = task.startDate ? dayjs(task.startDate) : dayjs(task.dueDate).subtract(1, 'day')
  const taskEnd = dayjs(task.dueDate)
  
  // 检查任务是否在显示范围内
  if (taskEnd.isBefore(start, 'day') || taskStart.isAfter(end, 'day')) {
    return { visible: false }
  }
  
  // 计算位置
  const actualStart = taskStart.isBefore(start) ? start : taskStart
  const actualEnd = taskEnd.isAfter(end) ? end : taskEnd
  
  const startOffset = actualStart.diff(start, 'day') * DAY_WIDTH
  const duration = actualEnd.diff(actualStart, 'day') + 1
  const width = Math.max(duration * DAY_WIDTH - 4, 20) // 最小宽度20px
  
  return {
    visible: true,
    left: startOffset + 2 + 'px',
    width: width + 'px'
  }
}

const getMilestonePosition = (task) => {
  const { start } = dateRange.value
  const taskDate = dayjs(task.dueDate)
  const diffDays = taskDate.diff(start, 'day')
  return diffDays * DAY_WIDTH + DAY_WIDTH / 2 - 10
}

const getTaskProgress = (task) => {
  if (task.status === 2) return 100
  if (task.subtasks && task.subtasks.length > 0) {
    const completed = task.subtasks.filter(s => s.status === 1).length
    return Math.round((completed / task.subtasks.length) * 100)
  }
  return 0
}

const priorityText = (p) => priorities[p] || '普通'

const getStatusClass = (task) => {
  if (task.status === 2) return 'completed'
  if (isTaskOverdue(task)) return 'overdue'
  return 'pending'
}

const getStatusText = (task) => {
  if (task.status === 2) return '已完成'
  if (isTaskOverdue(task)) return '已逾期'
  return '进行中'
}

// 滚动同步
const onTaskScroll = (e) => {
  scrollTop.value = e.detail.scrollTop
}

const onHeaderScroll = (e) => {
  headerScrollLeft.value = e.detail.scrollLeft
  bodyScrollLeft.value = e.detail.scrollLeft
}

const onBodyScroll = (e) => {
  scrollTop.value = e.detail.scrollTop
  bodyScrollLeft.value = e.detail.scrollLeft
  headerScrollLeft.value = e.detail.scrollLeft
}

// 数据加载
const loadTasks = async () => {
  try {
    const familyId = uni.getStorageSync('currentFamilyId') || 1
    const res = await taskApi.getList(familyId)
    tasks.value = res || []
  } catch (e) {
    console.error('加载任务失败', e)
    uni.showToast({ title: '加载失败', icon: 'none' })
  }
}

// 交互
const changeTimeRange = (range) => {
  currentRange.value = range
}

const selectTask = (task) => {
  selectedTask.value = task
}

const showTaskDetail = (task) => {
  selectedTask.value = task
  showDetailModal.value = true
}

const closeDetailModal = () => {
  showDetailModal.value = false
}

const toggleTaskStatus = async (task) => {
  try {
    if (task.status === 2) {
      await taskApi.update({ ...task, status: 0 })
      task.status = 0
    } else {
      await taskApi.complete(task.id)
      task.status = 2
    }
    uni.showToast({ title: task.status === 2 ? '已完成' : '已恢复', icon: 'none' })
    closeDetailModal()
  } catch (e) {
    uni.showToast({ title: '操作失败', icon: 'none' })
  }
}

const goToDetail = (task) => {
  closeDetailModal()
  uni.navigateTo({ url: `/pages/task/detail?id=${task.id}` })
}

const goBack = () => {
  uni.navigateBack()
}

const showSettings = () => {
  uni.showActionSheet({
    itemList: ['按优先级排序', '按截止日期排序', '显示已完成', '隐藏已完成'],
    success: (res) => {
      // 处理设置选项
      uni.showToast({ title: '设置已保存', icon: 'none' })
    }
  })
}

const switchView = (view) => {
  const urls = {
    list: '/pages/task/index',
    calendar: '/pages/task/calendar',
    board: '/pages/task/board'
  }
  if (urls[view] && view !== 'gantt') {
    uni.navigateTo({ url: urls[view] })
  }
}

onMounted(() => {
  loadTasks()
})
</script>

<style lang="scss" scoped>
.page-container {
  min-height: 100vh;
  background: #F8FAFC;
  padding-bottom: 80px;
}

// 头部
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 50px 20px 20px;
  background: linear-gradient(135deg, #10B981 0%, #059669 100%);
  
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
      font-size: 18px;
    }
  }
}

// 统计概览
.stats-overview {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 10px;
  padding: 15px;
  margin-top: -10px;
  
  .stat-item {
    background: #fff;
    border-radius: 16px;
    padding: 15px 8px;
    text-align: center;
    box-shadow: 0 2px 8px rgba(0,0,0,0.04);
    
    .stat-number {
      font-size: 24px;
      font-weight: 700;
      color: #3B82F6;
      display: block;
      margin-bottom: 4px;
    }
    
    .stat-label {
      font-size: 11px;
      color: #6B7280;
    }
  }
}

// 时间范围选择
.time-range-selector {
  display: flex;
  gap: 8px;
  padding: 0 15px 15px;
  
  .range-option {
    flex: 1;
    padding: 10px;
    background: #fff;
    border-radius: 12px;
    text-align: center;
    font-size: 13px;
    color: #6B7280;
    box-shadow: 0 1px 3px rgba(0,0,0,0.05);
    
    &.active {
      background: linear-gradient(135deg, #10B981, #059669);
      color: #fff;
    }
  }
}

// 甘特图容器
.gantt-container {
  display: flex;
  background: #fff;
  margin: 0 15px;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
  height: calc(100vh - 350px);
}

// 左侧任务列表面板
.task-list-panel {
  width: 110px;
  flex-shrink: 0;
  border-right: 1px solid #E5E7EB;
  background: #F9FAFB;
  
  .panel-header {
    height: 50px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #F3F4F6;
    border-bottom: 1px solid #E5E7EB;
    
    text {
      font-size: 13px;
      font-weight: 600;
      color: #374151;
    }
  }
  
  .task-list {
    height: calc(100% - 50px);
    
    .task-row {
      height: 50px;
      display: flex;
      align-items: center;
      padding: 0 10px;
      border-bottom: 1px solid #F3F4F6;
      
      &.completed {
        opacity: 0.6;
      }
      
      &.overdue {
        background: #FEF2F2;
      }
      
      &.high-priority .task-name {
        color: #DC2626;
        font-weight: 600;
      }
      
      .task-row-content {
        flex: 1;
        display: flex;
        align-items: center;
        gap: 4px;
      }
      
      .task-name {
        font-size: 12px;
        color: #374151;
        flex: 1;
      }
      
      .task-badges {
        display: flex;
        gap: 2px;
      }
      
      .priority-badge, .overdue-badge {
        font-size: 9px;
        padding: 1px 3px;
        border-radius: 3px;
        font-weight: bold;
      }
      
      .priority-badge.high {
        background: #FEE2E2;
        color: #DC2626;
      }
      
      .overdue-badge {
        background: #FECACA;
        color: #991B1B;
      }
    }
    
    .empty-tasks {
      text-align: center;
      padding: 40px 10px;
      
      text {
        font-size: 12px;
        color: #9CA3AF;
      }
    }
  }
}

// 右侧时间轴面板
.timeline-panel {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.timeline-header {
  height: 50px;
  background: #F3F4F6;
  border-bottom: 1px solid #E5E7EB;
  white-space: nowrap;
}

.timeline-dates {
  display: flex;
  height: 100%;
}

.date-cell {
  width: 50px;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border-right: 1px solid #E5E7EB;
  
  &.is-today {
    background: #DBEAFE;
    
    .date-day {
      color: #2563EB;
      font-weight: 700;
    }
  }
  
  &.weekend {
    background: #FEF3C7;
  }
  
  .date-day {
    font-size: 14px;
    font-weight: 600;
    color: #374151;
  }
  
  .date-week {
    font-size: 10px;
    color: #9CA3AF;
    margin-top: 2px;
  }
}

.gantt-bars-area {
  flex: 1;
}

.gantt-grid {
  position: relative;
  min-height: 100%;
}

.grid-lines {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
}

.grid-line {
  width: 50px;
  border-right: 1px solid #F3F4F6;
  
  &.is-today {
    background: rgba(219, 234, 254, 0.3);
  }
  
  &.weekend {
    background: rgba(254, 243, 199, 0.2);
  }
}

.current-time-line {
  position: absolute;
  top: 0;
  bottom: 0;
  width: 2px;
  background: #EF4444;
  z-index: 10;
  
  .time-indicator {
    position: absolute;
    top: -18px;
    left: 50%;
    transform: translateX(-50%);
    font-size: 10px;
    color: #fff;
    background: #EF4444;
    padding: 2px 6px;
    border-radius: 4px;
    white-space: nowrap;
  }
}

.gantt-bar-row {
  position: relative;
  height: 50px;
  border-bottom: 1px solid #F3F4F6;
}

.gantt-bar {
  position: absolute;
  top: 12px;
  height: 26px;
  background: linear-gradient(135deg, #3B82F6, #2563EB);
  border-radius: 13px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 4px rgba(59, 130, 246, 0.3);
  overflow: hidden;
  
  &.completed {
    background: linear-gradient(135deg, #10B981, #059669);
    box-shadow: 0 2px 4px rgba(16, 185, 129, 0.3);
  }
  
  &.overdue {
    background: linear-gradient(135deg, #EF4444, #DC2626);
    box-shadow: 0 2px 4px rgba(239, 68, 68, 0.3);
    animation: pulse 2s infinite;
  }
  
  &.high-priority {
    border: 2px solid #F59E0B;
  }
  
  .bar-text {
    font-size: 11px;
    color: #fff;
    font-weight: 500;
    padding: 0 8px;
    white-space: nowrap;
  }
  
  .progress-overlay {
    position: absolute;
    top: 0;
    left: 0;
    bottom: 0;
    background: rgba(255,255,255,0.2);
    pointer-events: none;
  }
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.8; }
}

.milestone-marker {
  position: absolute;
  top: 10px;
  font-size: 20px;
  transform: translateX(-50%);
}

// 任务详情弹窗
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
  background: linear-gradient(135deg, #10B981, #059669);
  
  .modal-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 18px;
    font-weight: 600;
    color: #fff;
    
    .task-icon {
      font-size: 20px;
    }
  }
  
  .close-btn {
    font-size: 20px;
    color: rgba(255,255,255,0.8);
  }
}

.task-detail {
  padding: 20px;
}

.detail-title {
  font-size: 18px;
  font-weight: 600;
  color: #1F2937;
  margin-bottom: 20px;
  line-height: 1.4;
  
  &.completed {
    text-decoration: line-through;
    color: #9CA3AF;
  }
}

.detail-timeline {
  display: flex;
  align-items: center;
  gap: 15px;
  background: #F9FAFB;
  padding: 16px;
  border-radius: 16px;
  margin-bottom: 20px;
  
  .timeline-item {
    flex: 1;
    text-align: center;
  }
  
  .timeline-label {
    font-size: 11px;
    color: #9CA3AF;
    display: block;
    margin-bottom: 4px;
  }
  
  .timeline-value {
    font-size: 14px;
    color: #374151;
    font-weight: 500;
  }
  
  .timeline-arrow {
    font-size: 16px;
    color: #10B981;
  }
  
  .timeline-duration {
    position: absolute;
    margin-top: 50px;
    
    text {
      font-size: 11px;
      color: #10B981;
      background: rgba(16, 185, 129, 0.1);
      padding: 2px 8px;
      border-radius: 10px;
    }
  }
}

.detail-info {
  .info-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 0;
    border-bottom: 1px solid #F3F4F6;
    
    .info-label {
      font-size: 14px;
      color: #6B7280;
    }
    
    .info-value {
      font-size: 14px;
      color: #374151;
      font-weight: 500;
      
      &.priority-0 { color: #6B7280; }
      &.priority-1 { color: #D97706; }
      &.priority-2 { color: #DC2626; }
    }
    
    .status-badge {
      font-size: 12px;
      padding: 4px 10px;
      border-radius: 10px;
      font-weight: 500;
      
      &.completed {
        background: #D1FAE5;
        color: #059669;
      }
      
      &.overdue {
        background: #FEE2E2;
        color: #DC2626;
      }
      
      &.pending {
        background: #DBEAFE;
        color: #2563EB;
      }
    }
    
    .progress-bar {
      width: 100px;
      height: 6px;
      background: #E5E7EB;
      border-radius: 3px;
      overflow: hidden;
      
      .progress-fill {
        height: 100%;
        background: #10B981;
        border-radius: 3px;
        transition: width 0.3s;
      }
    }
    
    .progress-text {
      font-size: 12px;
      color: #6B7280;
      margin-left: 8px;
    }
  }
}

.modal-actions {
  display: flex;
  gap: 12px;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #F3F4F6;
  
  .action-btn {
    flex: 1;
    padding: 14px;
    border-radius: 12px;
    text-align: center;
    font-size: 15px;
    font-weight: 500;
    
    &.primary {
      background: linear-gradient(135deg, #10B981, #059669);
      color: #fff;
    }
    
    &.secondary {
      background: #F3F4F6;
      color: #374151;
    }
  }
}

// 底部导航
.bottom-nav {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  justify-content: space-around;
  background: #fff;
  padding: 12px 20px calc(12px + env(safe-area-inset-bottom));
  box-shadow: 0 -2px 10px rgba(0,0,0,0.05);
  
  .nav-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 4px;
    padding: 8px 16px;
    border-radius: 12px;
    
    &.active {
      background: rgba(16, 185, 129, 0.1);
      
      .nav-text {
        color: #10B981;
      }
    }
    
    .nav-icon {
      font-size: 20px;
    }
    
    .nav-text {
      font-size: 11px;
      color: #6B7280;
    }
  }
}
</style>
