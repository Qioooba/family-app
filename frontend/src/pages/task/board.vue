<template>
  <view class="page-container">
    <!-- 头部 -->
    <view class="header">
      <view class="header-left" @click="goBack">
        <text class="back-icon">‹</text>
      </view>
      <view class="header-title">任务看板</view>
      <view class="header-right">
        <view class="filter-btn" @click="showFilterOptions">
          <text class="filter-icon">🔍</text>
        </view>
        <view class="add-btn" @click="showAddModal">
          <text class="icon">+</text>
        </view>
      </view>
    </view>
    
    <!-- 统计栏 -->
    <view class="stats-bar">
      <view class="stat-item">
        <text class="stat-dot" style="background: #6B7280;"></text>
        <text class="stat-label">待办 {{ stats.todo }}</text>
      </view>
      <view class="stat-item">
        <text class="stat-dot" style="background: #3B82F6;"></text>
        <text class="stat-label">进行中 {{ stats.doing }}</text>
      </view>
      <view class="stat-item">
        <text class="stat-dot" style="background: #10B981;"></text>
        <text class="stat-label">已完成 {{ stats.done }}</text>
      </view>
    </view>
    
    <!-- 看板主体 -->
    <scroll-view class="board-container" scroll-x :scroll-with-animation="true">
      <view class="board-columns">
        <!-- 待办列 -->
        <view 
          class="board-column"
          :class="{ 'drag-over': dragOverColumn === 'todo' }"
          @touchstart="onColumnTouchStart('todo')"
          @touchend="onColumnTouchEnd"
        >
          <view class="column-header">
            <view class="column-title">
              <view class="status-dot" style="background: #6B7280;"></text>
              <text>待办</text>
              <text class="count-badge">{{ todoTasks.length }}</text>
            </view>
            <view class="column-actions">
              <text class="action-icon" @click="quickAdd('todo')">+</text>
            </view>
          </view>
          
          <view class="column-content"
          >
            <view 
              v-for="(task, index) in todoTasks" 
              :key="task.id"
              class="task-card"
              :class="{ 
                'dragging': draggingTask?.id === task.id,
                'high-priority': task.priority === 2,
                'medium-priority': task.priority === 1
              }"
              :style="getDragStyle(task)"
              :data-task-id="task.id"
              @touchstart="onTaskTouchStart($event, task, 'todo', index)"
              @touchmove="onTaskTouchMove"
              @touchend="onTaskTouchEnd"
              @longpress="showTaskMenu(task)"
              @click="viewTask(task)"
            >
              <view class="card-header">
                <view class="priority-indicator" :class="'p' + task.priority"></view>
                <text class="card-title">{{ task.title }}</text>
              </view>
              
              <view class="card-meta">
                <text v-if="task.dueDate" class="due-date" :class="{ overdue: isOverdue(task) }">
                  📅 {{ formatDate(task.dueDate) }}
                </text>
                <text v-if="task.assigneeName" class="assignee">
                  👤 {{ task.assigneeName }}
                </text>
              </view>
              
              <view v-if="task.subtasks?.length > 0" class="card-progress">
                <view class="mini-progress">
                  <view class="mini-progress-fill" :style="{ width: getProgress(task) + '%' }"></text>
                </view>
                <text class="progress-text">{{ getProgress(task) }}%</text>
              </view>
              
              <view class="card-tags">
                <text v-if="task.categoryName" class="tag">{{ task.categoryName }}</text>
                <text v-if="isOverdue(task)" class="tag overdue">已逾期</text>
              </view>
            </view>
            
            <view v-if="todoTasks.length === 0" class="empty-column">
              <text class="empty-icon">📝</text>
              <text class="empty-text">暂无待办任务</text>
              <text class="empty-action" @click="quickAdd('todo')">+ 添加任务</text>
            </view>
          </view>
        </view>
        
        <!-- 进行中列 -->
        <view 
          class="board-column"
          :class="{ 'drag-over': dragOverColumn === 'doing' }"
          @touchstart="onColumnTouchStart('doing')"
          @touchend="onColumnTouchEnd"
        >
          <view class="column-header blue">
            <view class="column-title">
              <view class="status-dot" style="background: #3B82F6;"></text>
              <text>进行中</text>
              <text class="count-badge">{{ doingTasks.length }}</text>
            </view>
          </view>
          
          <view class="column-content"
          >
            <view 
              v-for="(task, index) in doingTasks" 
              :key="task.id"
              class="task-card"
              :class="{ 
                'dragging': draggingTask?.id === task.id,
                'high-priority': task.priority === 2,
                'medium-priority': task.priority === 1
              }"
              :style="getDragStyle(task)"
              :data-task-id="task.id"
              @touchstart="onTaskTouchStart($event, task, 'doing', index)"
              @touchmove="onTaskTouchMove"
              @touchend="onTaskTouchEnd"
              @longpress="showTaskMenu(task)"
              @click="viewTask(task)"
            >
              <view class="card-header">
                <view class="priority-indicator" :class="'p' + task.priority"></view>
                <text class="card-title">{{ task.title }}</text>
              </view>
              
              <view class="card-meta">
                <text v-if="task.dueDate" class="due-date" :class="{ overdue: isOverdue(task) }">
                  📅 {{ formatDate(task.dueDate) }}
                </text>
                <text v-if="task.assigneeName" class="assignee">
                  👤 {{ task.assigneeName }}
                </text>
              </view>
              
              <view v-if="task.subtasks?.length > 0" class="card-progress">
                <view class="mini-progress">
                  <view class="mini-progress-fill" :style="{ width: getProgress(task) + '%' }"></text>
                </view>
                <text class="progress-text">{{ getProgress(task) }}%</text>
              </view>
              
              <view class="card-tags">
                <text v-if="task.categoryName" class="tag">{{ task.categoryName }}</text>
                <text v-if="isOverdue(task)" class="tag overdue">已逾期</text>
              </view>
            </view>
            
            <view v-if="doingTasks.length === 0" class="empty-column">
              <text class="empty-icon">🚀</text>
              <text class="empty-text">拖入任务开始工作</text>
            </view>
          </view>
        </view>
        
        <!-- 已完成列 -->
        <view 
          class="board-column"
          :class="{ 'drag-over': dragOverColumn === 'done' }"
          @touchstart="onColumnTouchStart('done')"
          @touchend="onColumnTouchEnd"
        >
          <view class="column-header green">
            <view class="column-title">
              <view class="status-dot" style="background: #10B981;"></text>
              <text>已完成</text>
              <text class="count-badge">{{ doneTasks.length }}</text>
            </view>
          </view>
          
          <view class="column-content"
          >
            <view 
              v-for="(task, index) in doneTasks" 
              :key="task.id"
              class="task-card completed"
              :class="{ 'dragging': draggingTask?.id === task.id }"
              :style="getDragStyle(task)"
              :data-task-id="task.id"
              @touchstart="onTaskTouchStart($event, task, 'done', index)"
              @touchmove="onTaskTouchMove"
              @touchend="onTaskTouchEnd"
              @longpress="showTaskMenu(task)"
              @click="viewTask(task)"
            >
              <view class="card-header">
                <text class="completed-icon">✓</text>
                <text class="card-title completed">{{ task.title }}</text>
              </view>
              
              <view class="card-meta">
                <text v-if="task.completedAt" class="completed-date">
                  ✅ {{ formatDate(task.completedAt) }}
                </text>
                <text v-else-if="task.dueDate" class="due-date">
                  📅 {{ formatDate(task.dueDate) }}
                </text>
              </view>
              
              <view class="card-tags">
                <text v-if="task.categoryName" class="tag completed">{{ task.categoryName }}</text>
              </view>
            </view>
            
            <view v-if="doneTasks.length === 0" class="empty-column">
              <text class="empty-icon">🎉</text>
              <text class="empty-text">还没有完成的任务</text>
            </view>
          </view>
        </view>
      </view>
    </scroll-view>
    
    <!-- 底部导航 -->
    <view class="bottom-nav">
      <view class="nav-item" @click="switchView('list')">
        <text class="nav-icon">📋</text>
        <text class="nav-text">列表</text>
      </view>
      <view class="nav-item" @click="switchView('calendar')">
        <text class="nav-icon">📅</text>
        <text class="nav-text">日历</text>
      </view>
      <view class="nav-item" @click="switchView('gantt')">
        <text class="nav-icon">📊</text>
        <text class="nav-text">甘特图</text>
      </view>
      <view class="nav-item active">
        <text class="nav-icon">📌</text>
        <text class="nav-text">看板</text>
      </view>
    </view>
    
    <!-- 添加任务弹窗 -->
    <view v-if="showModal" class="modal-overlay" @click="closeModal">
      <view class="modal-content" @click.stop>
        <view class="modal-header">
          <text class="modal-title">添加新任务</text>
          <text class="close-btn" @click="closeModal">✕</text>
        </view>
        
        <view class="form-item">
          <text class="label">任务标题</text>
          <input class="input" v-model="newTask.title" placeholder="输入任务标题" />
        </view>
        
        <view class="form-item">
          <text class="label">状态</text>
          <view class="status-options">
            <view 
              v-for="(status, index) in statuses" :key="status.id || index" 
              
              class="status-option"
              :class="{ active: newTask.status === status.value }"
              @click="newTask.status = status.value"
            >
              <view class="status-dot" :style="{ background: status.color }"></text>
              <text>{{ status.label }}</text>
            </view>
          </view>
        </view>
        
        <view class="form-item">
          <text class="label">截止日期</text>
          <picker mode="date" :value="newTask.dueDate" @change="onDateChange">
            <view class="picker">{{ newTask.dueDate || '请选择日期' }}</view>
          </picker>
        </view>
        
        <view class="form-item">
          <text class="label">优先级</text>
          <view class="priority-options">
            <view 
              v-for="(p, i) in priorities" 
              :key="i"
              class="priority-option"
              :class="{ active: newTask.priority === i }"
              @click="newTask.priority = i"
            >
              <text>{{ p }}</text>
            </view>
          </view>
        </view>
        
        <view class="form-actions">
          <button class="btn-cancel" @click="closeModal">取消</button>
          <button class="btn-confirm" @click="addTask">确认添加</button>
        </view>
      </view>
    </view>
    
    <!-- 拖拽提示 -->
    <view v-if="isDragging" class="drag-hint">
      <text>拖动到目标列松手即可移动</text>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { taskApi } from '../../api/index.js'
import dayjs from 'dayjs'

// 常量
const priorities = ['普通', '重要', '紧急']
const statuses = [
  { label: '待办', value: 'todo', color: '#6B7280' },
  { label: '进行中', value: 'doing', color: '#3B82F6' },
  { label: '已完成', value: 'done', color: '#10B981' }
]

// 响应式数据
const tasks = ref([])
const showModal = ref(false)
const isDragging = ref(false)
const draggingTask = ref(null)
const dragOverColumn = ref(null)
const dragSourceColumn = ref(null)
const dragSourceIndex = ref(null)
const touchStartPos = ref({ x: 0, y: 0 })
const touchCurrentPos = ref({ x: 0, y: 0 })

// 新增任务
const newTask = ref({
  title: '',
  status: 'todo',
  dueDate: '',
  priority: 0
})

// 计算属性
const todoTasks = computed(() => tasks.value.filter(t => t.status === 0))
const doingTasks = computed(() => tasks.value.filter(t => t.status === 1))
const doneTasks = computed(() => tasks.value.filter(t => t.status === 2))

const stats = computed(() => ({
  todo: todoTasks.value.length,
  doing: doingTasks.value.length,
  done: doneTasks.value.length
}))

// 拖拽样式
const getDragStyle = (task) => {
  if (draggingTask.value?.id !== task.id) return {}
  
  return {
    position: 'fixed',
    left: touchCurrentPos.value.x - 100 + 'px',
    top: touchCurrentPos.value.y - 30 + 'px',
    width: '200px',
    zIndex: 1000,
    transform: 'rotate(3deg) scale(1.05)',
    boxShadow: '0 10px 40px rgba(0,0,0,0.2)'
  }
}

// 方法
const loadTasks = async () => {
  try {
    const familyId = uni.getStorageSync('currentFamilyId') || 1
    const res = await taskApi.getList(familyId)
    // 转换状态值到看板列
    tasks.value = (res || []).map(task => ({
      ...task,
      // 0: 待办, 1: 进行中, 2: 已完成
      status: task.status === 2 ? 2 : task.status === 1 ? 1 : 0
    }))
  } catch (e) {
    console.error('加载任务失败', e)
    uni.showToast({ title: '加载失败', icon: 'none' })
  }
}

const isOverdue = (task) => {
  if (!task.dueDate || task.status === 2) return false
  return dayjs(task.dueDate).isBefore(dayjs(), 'day')
}

const formatDate = (date) => {
  if (!date) return ''
  const d = dayjs(date)
  const today = dayjs()
  
  if (d.isSame(today, 'day')) return '今天'
  if (d.isSame(today.add(1, 'day'), 'day')) return '明天'
  if (d.isSame(today.subtract(1, 'day'), 'day')) return '昨天'
  
  return d.format('MM-DD')
}

const getProgress = (task) => {
  if (!task.subtasks || task.subtasks.length === 0) return 0
  const completed = task.subtasks.filter(s => s.status === 1).length
  return Math.round((completed / task.subtasks.length) * 100)
}

// 拖拽相关
const onTaskTouchStart = (e, task, column, index) => {
  const touch = e.touches[0]
  touchStartPos.value = { x: touch.clientX, y: touch.clientY }
  touchCurrentPos.value = { x: touch.clientX, y: touch.clientY }
  
  draggingTask.value = task
  dragSourceColumn.value = column
  dragSourceIndex.value = index
  
  // 延迟显示拖拽效果
  setTimeout(() => {
    if (draggingTask.value?.id === task.id) {
      isDragging.value = true
    }
  }, 200)
}

const onTaskTouchMove = (e) => {
  if (!isDragging.value) {
    const touch = e.touches[0]
    const deltaX = touch.clientX - touchStartPos.value.x
    const deltaY = touch.clientY - touchStartPos.value.y
    
    // 移动超过10px才开始拖拽
    if (Math.abs(deltaX) > 10 || Math.abs(deltaY) > 10) {
      isDragging.value = true
    }
  }
  
  if (isDragging.value) {
    const touch = e.touches[0]
    touchCurrentPos.value = { x: touch.clientX, y: touch.clientY }
    
    // 检测当前在哪个列上方
    checkDragOverColumn(touch.clientX, touch.clientY)
  }
}

const onTaskTouchEnd = async () => {
  if (isDragging.value && dragOverColumn.value && dragOverColumn.value !== dragSourceColumn.value) {
    // 移动任务到目标列
    await moveTaskToColumn(draggingTask.value, dragOverColumn.value)
  }
  
  // 重置拖拽状态
  isDragging.value = false
  draggingTask.value = null
  dragOverColumn.value = null
  dragSourceColumn.value = null
  dragSourceIndex.value = null
}

const checkDragOverColumn = (x, y) => {
  // 使用 uni.createSelectorQuery 获取列的位置信息
  const query = uni.createSelectorQuery().in(this)
  query.selectAll('.board-column').boundingClientRect(rects => {
    rects.forEach((rect, index) => {
      if (x >= rect.left && x <= rect.right &&
          y >= rect.top && y <= rect.bottom) {
        dragOverColumn.value = ['todo', 'doing', 'done'][index]
      }
    })
  }).exec()
}

const onColumnTouchStart = (column) => {
  if (isDragging.value) {
    dragOverColumn.value = column
  }
}

const onColumnTouchEnd = () => {
  dragOverColumn.value = null
}

const moveTaskToColumn = async (task, targetColumn) => {
  const statusMap = { todo: 0, doing: 1, done: 2 }
  const newStatus = statusMap[targetColumn]
  
  if (task.status === newStatus) return
  
  try {
    // 更新本地状态
    task.status = newStatus
    
    // 调用API更新
    if (newStatus === 2) {
      await taskApi.complete(task.id)
      task.completedAt = new Date().toISOString()
    } else {
      await taskApi.update({ ...task, status: newStatus })
    }
    
    uni.showToast({ 
      title: targetColumn === 'done' ? '任务已完成' : '状态已更新', 
      icon: 'none' 
    })
  } catch (e) {
    console.error('移动任务失败', e)
    uni.showToast({ title: '移动失败', icon: 'none' })
    // 恢复状态
    loadTasks()
  }
}

// 任务操作
const viewTask = (task) => {
  if (isDragging.value) return
  uni.navigateTo({ url: `/pages/task/detail?id=${task.id}` })
}

const showTaskMenu = (task) => {
  if (isDragging.value) return
  
  const items = ['编辑', '删除']
  if (task.status !== 2) items.push('标记完成')
  else items.push('标记未完成')
  
  uni.showActionSheet({
    itemList: items,
    success: async (res) => {
      switch (res.tapIndex) {
        case 0:
          uni.navigateTo({ url: `/pages/task/detail?id=${task.id}&mode=edit` })
          break
        case 1:
          deleteTask(task)
          break
        case 2:
          toggleTaskStatus(task)
          break
      }
    }
  })
}

const toggleTaskStatus = async (task) => {
  try {
    if (task.status === 2) {
      await taskApi.update({ ...task, status: 0 })
      task.status = 0
      task.completedAt = null
    } else {
      await taskApi.complete(task.id)
      task.status = 2
      task.completedAt = new Date().toISOString()
    }
    uni.showToast({ title: '状态已更新', icon: 'none' })
  } catch (e) {
    uni.showToast({ title: '操作失败', icon: 'none' })
  }
}

const deleteTask = (task) => {
  uni.showModal({
    title: '确认删除',
    content: '确定要删除这个任务吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          await taskApi.delete(task.id)
          tasks.value = tasks.value.filter(t => t.id !== task.id)
          uni.showToast({ title: '已删除', icon: 'success' })
        } catch (e) {
          uni.showToast({ title: '删除失败', icon: 'none' })
        }
      }
    }
  })
}

// 添加任务
const showAddModal = () => {
  newTask.value = {
    title: '',
    status: 'todo',
    dueDate: dayjs().format('YYYY-MM-DD'),
    priority: 0
  }
  showModal.value = true
}

const quickAdd = (status) => {
  newTask.value = {
    title: '',
    status,
    dueDate: dayjs().format('YYYY-MM-DD'),
    priority: 0
  }
  showModal.value = true
}

const closeModal = () => {
  showModal.value = false
}

const onDateChange = (e) => {
  newTask.value.dueDate = e.detail.value
}

const addTask = async () => {
  if (!newTask.value.title.trim()) {
    uni.showToast({ title: '请输入任务标题', icon: 'none' })
    return
  }

  try {
    const familyId = uni.getStorageSync('currentFamilyId') || 1
    const statusMap = { todo: 0, doing: 1, done: 2 }
    
    await taskApi.create({
      ...newTask.value,
      familyId,
      status: statusMap[newTask.value.status]
    })
    
    uni.showToast({ title: '添加成功', icon: 'success' })
    closeModal()
    loadTasks()
  } catch (e) {
    uni.showToast({ title: '添加失败', icon: 'none' })
  }
}

// 导航
const goBack = () => {
  uni.navigateBack()
}

const showFilterOptions = () => {
  uni.showActionSheet({
    itemList: ['按优先级排序', '按截止日期排序', '只看我的任务', '显示已完成的任务'],
    success: (res) => {
      uni.showToast({ title: '筛选功能开发中', icon: 'none' })
    }
  })
}

const switchView = (view) => {
  const urls = {
    list: '/pages/task/index',
    calendar: '/pages/task/calendar',
    gantt: '/pages/task/gantt'
  }
  if (urls[view]) {
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
  background: linear-gradient(180deg, #F8FAFC 0%, #F1F5F9 100%);
  padding-bottom: 80px;
}

// 头部
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 50px 20px 20px;
  background: linear-gradient(135deg, #F59E0B 0%, #D97706 100%);
  
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
  
  .header-right {
    display: flex;
    gap: 10px;
    
    .filter-btn, .add-btn {
      width: 40px;
      height: 40px;
      display: flex;
      align-items: center;
      justify-content: center;
      background: rgba(255,255,255,0.2);
      border-radius: 50%;
    }
    
    .icon, .filter-icon {
      font-size: 20px;
      color: #fff;
    }
  }
}

// 统计栏
.stats-bar {
  display: flex;
  justify-content: space-around;
  padding: 15px;
  background: #fff;
  margin: 10px 15px;
  border-radius: 16px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  
  .stat-item {
    display: flex;
    align-items: center;
    gap: 6px;
    
    .stat-dot {
      width: 8px;
      height: 8px;
      border-radius: 50%;
    }
    
    .stat-label {
      font-size: 13px;
      color: #6B7280;
    }
  }
}

// 看板容器
.board-container {
  padding: 0 15px;
  height: calc(100vh - 280px);
}

.board-columns {
  display: flex;
  gap: 12px;
  padding-bottom: 20px;
}

// 看板列
.board-column {
  width: 280px;
  flex-shrink: 0;
  background: #F3F4F6;
  border-radius: 16px;
  display: flex;
  flex-direction: column;
  max-height: 100%;
  
  &.drag-over {
    background: #E0E7FF;
    box-shadow: 0 0 0 2px #3B82F6;
  }
}

.column-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #E5E7EB;
  
  &.blue {
    background: linear-gradient(135deg, #DBEAFE, #BFDBFE);
    border-radius: 16px 16px 0 0;
  }
  
  &.green {
    background: linear-gradient(135deg, #D1FAE5, #A7F3D0);
    border-radius: 16px 16px 0 0;
  }
  
  .column-title {
    display: flex;
    align-items: center;
    gap: 8px;
    
    .status-dot {
      width: 8px;
      height: 8px;
      border-radius: 50%;
    }
    
    text {
      font-size: 15px;
      font-weight: 600;
      color: #374151;
    }
    
    .count-badge {
      font-size: 11px;
      color: #6B7280;
      background: rgba(0,0,0,0.1);
      padding: 2px 8px;
      border-radius: 10px;
    }
  }
  
  .column-actions {
    .action-icon {
      font-size: 20px;
      color: #6B7280;
      width: 28px;
      height: 28px;
      display: flex;
      align-items: center;
      justify-content: center;
      background: rgba(255,255,255,0.5);
      border-radius: 50%;
    }
  }
}

.column-content {
  flex: 1;
  padding: 12px;
  overflow-y: auto;
}

// 任务卡片
.task-card {
  background: #fff;
  border-radius: 12px;
  padding: 12px;
  margin-bottom: 10px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.08);
  transition: all 0.2s;
  
  &:active {
    transform: scale(0.98);
  }
  
  &.dragging {
    opacity: 0.9;
    pointer-events: none;
  }
  
  &.high-priority {
    border-left: 3px solid #EF4444;
  }
  
  &.medium-priority {
    border-left: 3px solid #F59E0B;
  }
  
  &.completed {
    background: #F0FDF4;
    
    .card-title {
      text-decoration: line-through;
      color: #6B7280;
    }
  }
}

.card-header {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  margin-bottom: 8px;
  
  .priority-indicator {
    width: 3px;
    height: 16px;
    border-radius: 2px;
    margin-top: 3px;
    
    &.p0 { background: #6B7280; }
    &.p1 { background: #F59E0B; }
    &.p2 { background: #EF4444; }
  }
  
  .card-title {
    flex: 1;
    font-size: 14px;
    font-weight: 500;
    color: #1F2937;
    line-height: 1.4;
  }
  
  .completed-icon {
    width: 20px;
    height: 20px;
    background: #10B981;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 12px;
    color: #fff;
    font-weight: bold;
  }
}

.card-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 8px;
  
  .due-date, .assignee, .completed-date {
    font-size: 11px;
    color: #6B7280;
    background: #F3F4F6;
    padding: 3px 8px;
    border-radius: 8px;
    
    &.overdue {
      background: #FEE2E2;
      color: #DC2626;
    }
  }
}

.card-progress {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  
  .mini-progress {
    flex: 1;
    height: 4px;
    background: #E5E7EB;
    border-radius: 2px;
    overflow: hidden;
    
    .mini-progress-fill {
      height: 100%;
      background: #3B82F6;
      border-radius: 2px;
      transition: width 0.3s;
    }
  }
  
  .progress-text {
    font-size: 10px;
    color: #6B7280;
  }
}

.card-tags {
  display: flex;
  gap: 6px;
  
  .tag {
    font-size: 10px;
    padding: 3px 8px;
    background: #E0E7FF;
    color: #3B82F6;
    border-radius: 8px;
    
    &.overdue {
      background: #FEE2E2;
      color: #DC2626;
    }
    
    &.completed {
      background: #D1FAE5;
      color: #059669;
    }
  }
}

// 空列提示
.empty-column {
  text-align: center;
  padding: 40px 20px;
  
  .empty-icon {
    font-size: 40px;
    margin-bottom: 10px;
    display: block;
  }
  
  .empty-text {
    font-size: 13px;
    color: #9CA3AF;
    display: block;
    margin-bottom: 10px;
  }
  
  .empty-action {
    font-size: 13px;
    color: #3B82F6;
    background: rgba(59, 130, 246, 0.1);
    padding: 6px 16px;
    border-radius: 16px;
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
      background: rgba(245, 158, 11, 0.1);
      
      .nav-text {
        color: #F59E0B;
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
  background: #fff;
  border-radius: 24px;
  padding: 24px;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  
  .modal-title {
    font-size: 18px;
    font-weight: 600;
    color: #1F2937;
  }
  
  .close-btn {
    font-size: 20px;
    color: #9CA3AF;
  }
}

.form-item {
  margin-bottom: 20px;
  
  .label {
    display: block;
    font-size: 14px;
    color: #374151;
    font-weight: 500;
    margin-bottom: 8px;
  }
  
  .input {
    width: 100%;
    height: 48px;
    background: #F3F4F6;
    border-radius: 12px;
    padding: 0 16px;
    font-size: 15px;
    color: #1F2937;
  }
  
  .picker {
    height: 48px;
    line-height: 48px;
    background: #F3F4F6;
    border-radius: 12px;
    padding: 0 16px;
    font-size: 15px;
    color: #6B7280;
  }
}

.status-options, .priority-options {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.status-option, .priority-option {
  padding: 10px 16px;
  background: #F3F4F6;
  border-radius: 12px;
  font-size: 14px;
  color: #6B7280;
  display: flex;
  align-items: center;
  gap: 6px;
  
  &.active {
    background: #F59E0B;
    color: #fff;
  }
  
  .status-dot {
    width: 8px;
    height: 8px;
    border-radius: 50%;
  }
}

.form-actions {
  display: flex;
  gap: 12px;
  margin-top: 30px;
  
  button {
    flex: 1;
    height: 48px;
    border-radius: 24px;
    font-size: 15px;
    border: none;
    font-weight: 500;
  }
  
  .btn-cancel {
    background: #F3F4F6;
    color: #6B7280;
  }
  
  .btn-confirm {
    background: linear-gradient(135deg, #F59E0B, #D97706);
    color: #fff;
  }
}

// 拖拽提示
.drag-hint {
  position: fixed;
  bottom: 100px;
  left: 50%;
  transform: translateX(-50%);
  background: rgba(0,0,0,0.8);
  padding: 10px 20px;
  border-radius: 20px;
  
  text {
    color: #fff;
    font-size: 13px;
  }
}
</style>
