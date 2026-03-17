<template>
  <view class="page-container">
    <!-- 状态栏背景 -->
    <view class="status-bar-bg"></view>
    
    <!-- 顶部快捷入口 -->
    <view class="quick-access">
      <view class="access-item" v-for="(item, index) in quickAccess" :key="index" @click="handleQuickAccess(item)"
>
        <view class="access-icon" :style="{ background: item.color }"
>
          <text>{{ item.icon }}</text>
        </view>
        <text class="access-label">{{ item.label }}</text>
      </view>
    </view>
    
    <!-- 今日概览卡片 -->
    <view class="overview-section">
      <view class="section-title"
>
        <text>今日概览</text>
        <text class="date-text">{{ todayDate }}</text>
      </view>
      
      <view class="overview-grid"
>
        <view class="overview-card primary" @click="goToTodayTasks"
>
          <view class="card-bg-icon">📋</view>
          <view class="card-content"
>
            <text class="card-number">{{ todayStats.pending }}</text>
            <text class="card-label">待办任务</text>
          </view>
          <view v-if="todayStats.pending > 0" class="card-badge">{{ todayStats.pending }}</view>
        </view>
        
        <view class="overview-card success" @click="goToCompleted"
>
          <view class="card-bg-icon">✓</view>
          <view class="card-content"
>
            <text class="card-number">{{ todayStats.completed }}</text>
            <text class="card-label">已完成</text>
          </view>
        </view>
        
        <view class="overview-card warning" @click="goToOverdue"
>
          <view class="card-bg-icon">⚠️</view>
          <view class="card-content"
>
            <text class="card-number">{{ todayStats.overdue }}</text>
            <text class="card-label">已逾期</text>
          </view>
          <view v-if="todayStats.overdue > 0" class="card-badge urgent">{{ todayStats.overdue }}</view>
        </view>
        
        <view class="overview-card info" @click="goToStats"
>
          <view class="card-bg-icon">📊</view>
          <view class="card-content"
>
            <text class="card-number">{{ completionRate }}%</text>
            <text class="card-label">完成率</text>
          </view>
        </view>
      </view>
    </view>
    
    <!-- 快速操作卡片 -->
    <view class="quick-actions-section"
>
      <view class="section-header"
>
        <text class="section-title">快速操作</text>
      </view>
      
      <view class="actions-grid"
>
        <view class="action-card" v-for="(action, index) in quickActions" :key="index" @click="handleAction(action)"
>
          <view class="action-icon" :style="{ background: action.bgColor }"
>
            <text>{{ action.icon }}</text>
          </view>
          <view class="action-info"
>
            <text class="action-title">{{ action.title }}</text>
            <text class="action-desc">{{ action.description }}</text>
          </view>
          <text class="action-arrow">›</text>
        </view>
      </view>
    </view>
    
    <!-- 最近任务卡片 -->
    <view class="recent-tasks-section" v-if="recentTasks.length > 0"
>
      <view class="section-header"
>
        <text class="section-title">最近更新</text>
        <text class="view-all" @click="viewAllTasks">查看全部 ›</text>
      </view>
      
      <view class="recent-list"
>
        <view 
          v-for="task in recentTasks.slice(0, 5)" 
          :key="task.id"
          class="recent-item"
          @click="goToTask(task)"
        >
          <view class="item-status" :class="'status-' + task.status"></view>
          
          <view class="item-content"
>
            <text class="item-title">{{ task.title }}</text>
            <text class="item-meta">{{ getTaskMeta(task) }}</text>
          </view>
          
          <view class="item-action" @click.stop="quickComplete(task)">
            <text v-if="task.status !== 2">⭕</text>
            <text v-else>✅</text>
          </view>
        </view>
      </view>
    </view>
    
    <!-- 快捷视图卡片 -->
    <view class="quick-views-section"
>
      <view class="section-header"
>
        <text class="section-title">快捷视图</text>
      </view>
      
      <view class="views-row"
>
        <view class="view-item" v-for="(view, index) in quickViews" :key="index" @click="goToView(view)"
>
          <view class="view-icon">{{ view.icon }}</view>
          <text class="view-name">{{ view.name }}</text>
        </view>
      </view>
    </view>
    
    <!-- 智能建议卡片 -->
    <view class="suggestions-section" v-if="suggestions.length > 0"
>
      <view class="section-header"
>
        <text class="section-title">💡 智能建议</text>
      </view>
      
      <view class="suggestion-list"
>
        <view 
          v-for="(suggestion, index) in suggestions" 
          :key="index"
          class="suggestion-card"
          :class="suggestion.type"
          @click="handleSuggestion(suggestion)"
        >
          <text class="suggestion-icon">{{ suggestion.icon }}</text>
          
          <view class="suggestion-content"
>
            <text class="suggestion-title">{{ suggestion.title }}</text>
            <text class="suggestion-desc">{{ suggestion.description }}</text>
          </view>
        </view>
      </view>
    </view>
    
    <!-- 底部空间 -->
    <view class="bottom-space"></view>
    
    <!-- 悬浮添加按钮 -->
    <view class="fab-add" @click="quickAddTask"
>
      <text>+</text>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { taskApi } from '../../api/index.js'
import dayjs from 'dayjs'

// 快捷入口
const quickAccess = [
  { icon: '📅', label: '日历', color: '#667EEA', action: 'calendar' },
  { icon: '📊', label: '甘特图', color: '#10B981', action: 'gantt' },
  { icon: '📌', label: '看板', color: '#F59E0B', action: 'board' },
  { icon: '⏰', label: '提醒', color: '#EF4444', action: 'reminder' },
  { icon: '📈', label: '统计', color: '#8B5CF6', action: 'stats' }
]

// 快速操作
const quickActions = [
  { 
    icon: '✏️', 
    title: '语音创建', 
    description: '说话快速创建任务',
    bgColor: '#DBEAFE',
    action: 'voice'
  },
  { 
    icon: '📷', 
    title: '拍照识别', 
    description: '拍照OCR识别任务',
    bgColor: '#D1FAE5',
    action: 'camera'
  },
  { 
    icon: '🔄', 
    title: '批量完成', 
    description: '快速完成多个任务',
    bgColor: '#FEF3C7',
    action: 'batch'
  },
  { 
    icon: '🏷️', 
    title: '智能分类', 
    description: '自动分类整理任务',
    bgColor: '#F3E8FF',
    action: 'classify'
  }
]

// 快捷视图
const quickViews = [
  { icon: '📋', name: '列表', path: '/pages/task/index' },
  { icon: '📅', name: '日历', path: '/pages/task/calendar' },
  { icon: '📊', name: '甘特图', path: '/pages/task/gantt' },
  { icon: '📌', name: '看板', path: '/pages/task/board' },
  { icon: '⏳', name: '时间线', path: '/pages/task/timeline' },
  { icon: '🔗', name: '依赖图', path: '/pages/task/dependency' },
  { icon: '🗺️', name: '热力图', path: '/pages/task/heatmap' }
]

// 响应式数据
const tasks = ref([])
const recentTasks = ref([])

// 计算属性
const todayDate = computed(() => {
  return dayjs().format('MM月DD日 dddd')
})

const todayStats = computed(() => {
  const today = dayjs().format('YYYY-MM-DD')
  const todayTasks = tasks.value.filter(t => t.dueDate === today)
  
  const pending = todayTasks.filter(t => t.status !== 2).length
  const completed = todayTasks.filter(t => t.status === 2).length
  const overdue = tasks.value.filter(t => {
    if (!t.dueDate || t.status === 2) return false
    return dayjs(t.dueDate).isBefore(dayjs(), 'day')
  }).length
  
  return { pending, completed, overdue }
})

const completionRate = computed(() => {
  const total = todayStats.value.pending + todayStats.value.completed
  if (total === 0) return 0
  return Math.round(todayStats.value.completed / total * 100)
})

const suggestions = computed(() => {
  const list = []
  
  // 逾期任务提醒
  if (todayStats.value.overdue > 0) {
    list.push({
      type: 'warning',
      icon: '⚠️',
      title: `有 ${todayStats.value.overdue} 个逾期任务`,
      description: '建议优先处理逾期任务',
      action: 'overdue'
    })
  }
  
  // 今日待办提醒
  if (todayStats.value.pending > 0) {
    list.push({
      type: 'info',
      icon: '📋',
      title: `今日还有 ${todayStats.value.pending} 个待办`,
      description: '保持专注，逐一完成',
      action: 'today'
    })
  }
  
  // 完成鼓励
  if (todayStats.value.completed > 0 && todayStats.value.pending === 0) {
    list.push({
      type: 'success',
      icon: '🎉',
      title: '太棒了！今日任务全部完成',
      description: '继续保持高效状态',
      action: 'celebrate'
    })
  }
  
  // 连续完成建议
  if (tasks.value.length > 5 && todayStats.value.completed === 0) {
    list.push({
      type: 'tip',
      icon: '💡',
      title: '开始今天第一个任务吧',
      description: '从小任务开始建立动力',
      action: 'start'
    })
  }
  
  return list.slice(0, 2)
})

// 方法
const loadTasks = async () => {
  try {
    const familyId = uni.getStorageSync('currentFamilyId') || 1
    const res = await taskApi.getList(familyId)
    tasks.value = res || []
    
    // 获取最近更新的任务
    recentTasks.value = [...tasks.value]
      .sort((a, b) => new Date(b.updatedAt || b.createdAt) - new Date(a.updatedAt || a.createdAt))
      .slice(0, 10)
  } catch (e) {
    console.error('加载任务失败', e)
  }
}

const getTaskMeta = (task) => {
  if (task.status === 2) return '已完成'
  if (task.dueDate) {
    const due = dayjs(task.dueDate)
    if (due.isBefore(dayjs(), 'day')) return `已逾期 ${dayjs().diff(due, 'day')} 天`
    if (due.isSame(dayjs(), 'day')) return '今天到期'
    return `还有 ${due.diff(dayjs(), 'day')} 天`
  }
  return '无截止日期'
}

// 导航
const handleQuickAccess = (item) => {
  const paths = {
    calendar: '/pages/task/calendar',
    gantt: '/pages/task/gantt',
    board: '/pages/task/board',
    reminder: '/pages/task/reminder',
    stats: '/pages/task/heatmap'
  }
  if (paths[item.action]) {
    uni.navigateTo({ url: paths[item.action] })
  }
}

const handleAction = (action) => {
  switch (action.action) {
    case 'voice':
      uni.showToast({ title: '语音功能开发中', icon: 'none' })
      break
    case 'camera':
      uni.navigateTo({ url: '/pages/task/create?mode=camera' })
      break
    case 'batch':
      uni.showToast({ title: '批量完成功能开发中', icon: 'none' })
      break
    case 'classify':
      uni.showToast({ title: '智能分类功能开发中', icon: 'none' })
      break
  }
}

const goToTodayTasks = () => {
  uni.setStorageSync('taskFilter', 'today')
  uni.switchTab({ url: '/pages/task/index' })
}

const goToCompleted = () => {
  uni.setStorageSync('taskFilter', 'completed')
  uni.switchTab({ url: '/pages/task/index' })
}

const goToOverdue = () => {
  uni.setStorageSync('taskFilter', 'overdue')
  uni.switchTab({ url: '/pages/task/index' })
}

const goToStats = () => {
  uni.navigateTo({ url: '/pages/task/heatmap' })
}

const viewAllTasks = () => {
  uni.removeStorageSync('taskFilter')
  uni.switchTab({ url: '/pages/task/index' })
}

const goToTask = (task) => {
  uni.navigateTo({ url: `/pages/task/detail?id=${task.id}` })
}

const goToView = (view) => {
  uni.navigateTo({ url: view.path })
}

const handleSuggestion = (suggestion) => {
  switch (suggestion.action) {
    case 'overdue':
      goToOverdue()
      break
    case 'today':
      goToTodayTasks()
      break
    case 'celebrate':
      uni.showModal({
        title: '🎉 恭喜！',
        content: '你今天完成了所有任务！',
        showCancel: false
      })
      break
    case 'start':
      quickAddTask()
      break
  }
}

const quickComplete = async (task) => {
  if (task.status === 2) {
    uni.showToast({ title: '任务已完成', icon: 'none' })
    return
  }
  
  try {
    await taskApi.complete(task.id)
    task.status = 2
    uni.showToast({ title: '已完成', icon: 'success' })
    loadTasks()
  } catch (e) {
    uni.showToast({ title: '操作失败', icon: 'none' })
  }
}

const quickAddTask = () => {
  uni.navigateTo({ url: '/pages/task/create' })
}

onMounted(() => {
  loadTasks()
})
</script>

<style lang="scss" scoped>
.page-container {
  min-height: 100vh;
  background: #F8FAFC;
  position: relative;
}

// 状态栏背景
.status-bar-bg {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 120px;
  background: linear-gradient(135deg, #667EEA 0%, #764BA2 100%);
  z-index: -1;
}

// 快捷入口
.quick-access {
  display: flex;
  justify-content: space-around;
  padding: 50px 15px 20px;
  
  .access-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
    
    .access-icon {
      width: 50px;
      height: 50px;
      border-radius: 16px;
      display: flex;
      align-items: center;
      justify-content: center;
      box-shadow: 0 4px 12px rgba(0,0,0,0.15);
      
      text {
        font-size: 24px;
      }
    }
    
    .access-label {
      font-size: 12px;
      color: #fff;
    }
  }
}

// 区块标题
.section-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  margin-bottom: 12px;
  
  text {
    font-size: 17px;
    font-weight: 600;
    color: #1F2937;
  }
  
  .date-text {
    font-size: 13px;
    color: #9CA3AF;
    font-weight: normal;
  }
  
  .view-all {
    font-size: 13px;
    color: #667EEA;
  }
}

// 今日概览
.overview-section {
  padding: 15px;
}

.overview-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.overview-card {
  background: #fff;
  border-radius: 20px;
  padding: 20px;
  position: relative;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0,0,0,0.06);
  
  &.primary {
    background: linear-gradient(135deg, #667EEA, #764BA2);
    
    .card-number, .card-label {
      color: #fff;
    }
  }
  
  &.success {
    background: linear-gradient(135deg, #10B981, #059669);
    
    .card-number, .card-label {
      color: #fff;
    }
  }
  
  &.warning {
    background: linear-gradient(135deg, #F59E0B, #D97706);
    
    .card-number, .card-label {
      color: #fff;
    }
  }
  
  &.info {
    background: linear-gradient(135deg, #3B82F6, #2563EB);
    
    .card-number, .card-label {
      color: #fff;
    }
  }
  
  .card-bg-icon {
    position: absolute;
    right: -10px;
    bottom: -10px;
    font-size: 60px;
    opacity: 0.2;
  }
  
  .card-content {
    position: relative;
    z-index: 1;
  }
  
  .card-number {
    font-size: 32px;
    font-weight: 700;
    color: #1F2937;
    display: block;
    margin-bottom: 4px;
  }
  
  .card-label {
    font-size: 13px;
    color: #6B7280;
  }
  
  .card-badge {
    position: absolute;
    top: 12px;
    right: 12px;
    min-width: 20px;
    height: 20px;
    background: #fff;
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 11px;
    font-weight: 600;
    color: #667EEA;
    padding: 0 6px;
    
    &.urgent {
      color: #EF4444;
      animation: pulse 1.5s infinite;
    }
  }
}

@keyframes pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.1); }
}

// 快速操作
.quick-actions-section {
  padding: 15px;
}

.section-header {
  padding: 0 20px;
  margin-bottom: 12px;
  
  .section-title {
    font-size: 17px;
    font-weight: 600;
    color: #1F2937;
  }
}

.actions-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  padding: 0 5px;
}

.action-card {
  display: flex;
  align-items: center;
  gap: 12px;
  background: #fff;
  border-radius: 16px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  
  .action-icon {
    width: 44px;
    height: 44px;
    border-radius: 14px;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
    
    text {
      font-size: 20px;
    }
  }
  
  .action-info {
    flex: 1;
    min-width: 0;
    
    .action-title {
      font-size: 14px;
      font-weight: 600;
      color: #1F2937;
      display: block;
      margin-bottom: 2px;
    }
    
    .action-desc {
      font-size: 11px;
      color: #9CA3AF;
    }
  }
  
  .action-arrow {
    font-size: 18px;
    color: #D1D5DB;
  }
}

// 最近任务
.recent-tasks-section {
  padding: 15px;
}

.recent-list {
  background: #fff;
  border-radius: 16px;
  padding: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
}

.recent-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-bottom: 1px solid #F3F4F6;
  
  &:last-child {
    border-bottom: none;
  }
  
  .item-status {
    width: 8px;
    height: 8px;
    border-radius: 50%;
    flex-shrink: 0;
    
    &.status-0 { background: #6B7280; }
    &.status-1 { background: #3B82F6; }
    &.status-2 { background: #10B981; }
  }
  
  .item-content {
    flex: 1;
    min-width: 0;
    
    .item-title {
      font-size: 14px;
      color: #1F2937;
      display: block;
      margin-bottom: 4px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
    
    .item-meta {
      font-size: 11px;
      color: #9CA3AF;
    }
  }
  
  .item-action {
    width: 32px;
    height: 32px;
    display: flex;
    align-items: center;
    justify-content: center;
    
    text {
      font-size: 18px;
    }
  }
}

// 快捷视图
.quick-views-section {
  padding: 15px;
}

.views-row {
  display: flex;
  gap: 12px;
  overflow-x: auto;
  padding: 0 5px 10px;
}

.view-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 16px 20px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  flex-shrink: 0;
  
  .view-icon {
    font-size: 28px;
  }
  
  .view-name {
    font-size: 12px;
    color: #6B7280;
  }
}

// 智能建议
.suggestions-section {
  padding: 15px;
}

.suggestion-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.suggestion-card {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 16px;
  border-radius: 16px;
  
  &.warning {
    background: linear-gradient(135deg, #FEF3C7, #FDE68A);
  }
  
  &.info {
    background: linear-gradient(135deg, #DBEAFE, #BFDBFE);
  }
  
  &.success {
    background: linear-gradient(135deg, #D1FAE5, #A7F3D0);
  }
  
  &.tip {
    background: linear-gradient(135deg, #F3E8FF, #E9D5FF);
  }
  
  .suggestion-icon {
    font-size: 24px;
  }
  
  .suggestion-content {
    flex: 1;
    
    .suggestion-title {
      font-size: 14px;
      font-weight: 600;
      color: #1F2937;
      display: block;
      margin-bottom: 4px;
    }
    
    .suggestion-desc {
      font-size: 12px;
      color: #6B7280;
    }
  }
}

// 底部空间
.bottom-space {
  height: 100px;
}

// 悬浮添加按钮
.fab-add {
  position: fixed;
  bottom: 30px;
  right: 20px;
  width: 56px;
  height: 56px;
  background: linear-gradient(135deg, #667EEA, #764BA2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.4);
  z-index: 100;
  
  text {
    font-size: 28px;
    color: #fff;
    font-weight: 300;
  }
}
</style>
