<template>
  <view class="page-container">
    <!-- 头部 -->
    <PageHeader 
      title="任务日历" 
      @back="goBack" 
      @action="showAddModal"
    />
    
    <!-- 统计卡片 -->
    <TaskStatsCards :stats="monthStats" />
    
    <!-- 日历区域 -->
    <view class="calendar-section">
      <!-- 月份导航 -->
      <MonthNavigator 
        :year="currentYear"
        :month="currentMonth"
        :lunar="lunarInfo?.month"
        @prev="prevMonth"
        @next="nextMonth"
      />
      
      <!-- 回到今天 -->
      <view class="today-btn" @click="goToToday">
        <text>回到今天</text>
      </view>
      
      <!-- 星期标题 -->
      <view class="weekdays">
        <text 
          v-for="(day, index) in weekdays" 
          :key="index"
          class="weekday"
          :class="{ weekend: index === 0 || index === 6 }"
        >{{ day }}</text>
      </view>
      
      <!-- 日期网格 -->
      <swiper 
        class="calendar-swiper"
        :current="swiperIndex"
        @change="onSwiperChange"
        :circular="true"
      >
        <swiper-item v-for="(monthData, mIndex) in visibleMonths" :key="mIndex">
          <view class="days-grid">
            <view 
              v-for="(day, index) in monthData.days" 
              :key="index"
              class="day-cell"
              :class="{ 
                'other-month': !day.isCurrentMonth,
                'is-today': day.isToday,
                'is-selected': isSelectedDay(day),
                'has-tasks': day.tasks.length > 0,
                'all-completed': day.allCompleted,
                'has-overdue': day.hasOverdue,
                weekend: day.isWeekend
              }"
              @click="selectDay(day)"
            >
              <view class="day-content">
                <text class="solar-date">{{ day.date }}</text>
                <text v-if="day.lunar && showLunar" class="lunar-date">{{ day.lunar }}</text>
              </view>
              
              <!-- 任务状态指示器 -->
              <view class="task-indicators" v-if="day.tasks.length > 0">
                <view 
                  v-for="(task, tIndex) in day.visibleTasks" 
                  :key="tIndex"
                  class="task-dot"
                  :class="{
                    'completed': task.status === 2,
                    'high-priority': task.priority === 2 && task.status !== 2,
                    'medium-priority': task.priority === 1 && task.status !== 2
                  }"
                ></text>
                <text v-if="day.tasks.length > 3" class="more-tasks">+{{ day.tasks.length - 3 }}</text>
              </view>
              
              <!-- 今日标记 -->
              <view v-if="day.isToday" class="today-badge">今</view>
            </view>
          </view>
        </swiper-item>
      </swiper>
    </view>
    
    <!-- 选中日期任务列表 -->
    <view class="tasks-section">
      <view class="section-header">
        <view class="section-title">
          <text class="date-text">{{ selectedDateStr }}</text>
          <text class="task-count" v-if="selectedDayTasks.length > 0">({{ selectedDayTasks.length }})</text>
        </view>
        <view class="section-actions">
          <text class="filter-btn" @click="showFilter = !showFilter">
            {{ currentFilter === 'all' ? '全部' : currentFilter === 'pending' ? '待办' : '已完成' }}
          </text>
        </view>
      </view>
      
      <!-- 筛选下拉 -->
      <view v-if="showFilter" class="filter-dropdown">
        <text 
          v-for="filter in filters" 
          :key="filter.value"
          class="filter-option"
          :class="{ active: currentFilter === filter.value }"
          @click="selectFilter(filter.value)"
        >{{ filter.label }}</text>
      </view>
      
      <!-- 任务列表 -->
      <scroll-view class="tasks-list" scroll-y>
        <view 
          v-for="(task, index) in filteredDayTasks" 
          :key="task.id"
          class="task-card"
          :class="{ 
            completed: task.status === 2,
            overdue: isOverdue(task) && task.status !== 2
          }"
          @click="viewTask(task)"
          @longpress="showTaskMenu(task)"
        >
          <view class="task-checkbox" :class="{ checked: task.status === 2 }" @click.stop="toggleTask(task)">
            <text v-if="task.status === 2" class="check-icon">✓</text>
          </view>
          
          <view class="task-content">
            <view class="task-header">
              <text class="task-title">{{ task.title }}</text>
              <view class="task-priority" :class="'priority-' + task.priority">
                {{ priorityText(task.priority) }}
              </view>
            </view>
            
            <view class="task-meta">
              <text class="meta-item" v-if="task.categoryName">
                <text class="meta-icon">📁</text>{{ task.categoryName }}
              </text>
              <text class="meta-item" v-if="task.assigneeName">
                <text class="meta-icon">👤</text>{{ task.assigneeName }}
              </text>
              <text class="meta-item" v-if="task.dueTime">
                <text class="meta-icon">⏰</text>{{ task.dueTime }}
              </text>
            </view>
            
            <!-- 子任务进度 -->
            <view v-if="task.subtasks && task.subtasks.length > 0" class="subtask-progress">
              <view class="progress-bar">
                <view class="progress-fill" :style="{ width: subtaskProgress(task) + '%' }"></text>
              </view>
              <text class="progress-text">{{ subtaskCompleted(task) }}/{{ task.subtasks.length }}</text>
            </view>
          </view>
          
          <view class="task-status">
            <text v-if="task.status === 2" class="status-badge completed">已完成</text>
            <text v-else-if="isOverdue(task)" class="status-badge overdue">已逾期</text>
            <text v-else class="status-badge pending">进行中</text>
          </view>
        </view>
        
        <!-- 空状态 -->
        <view v-if="filteredDayTasks.length === 0" class="empty-state">
          <text class="empty-icon">📅</text>
          <text class="empty-text">{{ selectedDayTasks.length === 0 ? '该日期暂无任务' : '没有符合条件的任务' }}</text>
          <text class="empty-action" @click="showAddModal">点击添加任务</text>
        </view>
      </scroll-view>
    </view>
    
    <!-- 底部快速操作 -->
    <view class="quick-actions">
      <view class="action-btn" @click="switchView('list')">
        <text class="action-icon">📋</text>
        <text class="action-text">列表</text>
      </view>
      <view class="action-btn active">
        <text class="action-icon">📅</text>
        <text class="action-text">日历</text>
      </view>
      <view class="action-btn" @click="switchView('gantt')">
        <text class="action-icon">📊</text>
        <text class="action-text">甘特图</text>
      </view>
      <view class="action-btn" @click="switchView('board')">
        <text class="action-icon">📌</text>
        <text class="action-text">看板</text>
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
          <text class="label">截止日期</text>
          <picker mode="date" :value="newTask.dueDate" @change="onDateChange">
            <view class="picker">{{ newTask.dueDate || '请选择日期' }}</view>
          </picker>
        </view>
        
        <view class="form-item">
          <text class="label">截止时间</text>
          <picker mode="time" :value="newTask.dueTime" @change="onTimeChange">
            <view class="picker">{{ newTask.dueTime || '请选择时间(可选)' }}</view>
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
        
        <view class="form-item">
          <text class="label">分类</text>
          <view class="category-options">
            <view 
              v-for="cat in categories" 
              :key="cat.id"
              class="category-option"
              :class="{ active: newTask.categoryId === cat.id }"
              @click="newTask.categoryId = cat.id"
            >
              <text>{{ cat.name }}</text>
            </view>
          </view>
        </view>
        
        <view class="form-actions">
          <button class="btn-cancel" @click="closeModal">取消</button>
          <button class="btn-confirm" @click="addTask">确认添加</button>
        </view>
      </view>
    </view>
    
    <!-- 任务详情弹窗 -->
    <view v-if="showDetailModal" class="modal-overlay" @click="closeDetailModal">
      <view class="modal-content detail-modal" @click.stop>
        <view class="modal-header">
          <text class="modal-title">任务详情</text>
          <text class="close-btn" @click="closeDetailModal">✕</text>
        </view>
        
        <view v-if="selectedTask" class="task-detail">
          <view class="detail-header">
            <view class="detail-checkbox" :class="{ checked: selectedTask.status === 2 }" @click="toggleTask(selectedTask)">
              <text v-if="selectedTask.status === 2" class="check-icon">✓</text>
            </view>
            <view class="detail-title" :class="{ completed: selectedTask.status === 2 }">{{ selectedTask.title }}</view>
          </view>
          
          <view class="detail-info">
            <view class="info-item">
              <text class="info-label">📅 截止日期</text>
              <text class="info-value">{{ selectedTask.dueDate || '未设置' }}</text>
            </view>
            <view class="info-item">
              <text class="info-label">⏰ 截止时间</text>
              <text class="info-value">{{ selectedTask.dueTime || '未设置' }}</text>
            </view>
            <view class="info-item">
              <text class="info-label">👤 负责人</text>
              <text class="info-value">{{ selectedTask.assigneeName || '未指派' }}</text>
            </view>
            <view class="info-item">
              <text class="info-label">📁 分类</text>
              <text class="info-value">{{ selectedTask.categoryName || '未分类' }}</text>
            </view>
            <view class="info-item">
              <text class="info-label">⚡ 优先级</text>
              <text class="info-value" :class="'priority-text-' + selectedTask.priority">{{ priorityText(selectedTask.priority) }}</text>
            </view>
          </view>
          
          <!-- 子任务 -->
          <view v-if="selectedTask.subtasks && selectedTask.subtasks.length > 0" class="subtask-section">
            <view class="section-title">
              子任务 ({{ subtaskCompleted(selectedTask) }}/{{ selectedTask.subtasks.length }})
            </view>
            <view 
              v-for="(sub, idx) in selectedTask.subtasks" 
              :key="idx"
              class="subtask-item"
              @click="toggleSubtask(sub)"
            >
              <view class="subtask-checkbox" :class="{ checked: sub.status === 1 }"></text>
              <text class="subtask-title" :class="{ completed: sub.status === 1 }">{{ sub.title }}</text>
            </view>
          </view>
          
          <view class="detail-actions">
            <view class="detail-btn edit" @click="editTask(selectedTask)">
              <text>✏️ 编辑</text>
            </view>
            <view class="detail-btn delete" @click="deleteTask(selectedTask)">
              <text>🗑️ 删除</text>
            </view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { taskApi } from '../../api/index.js'
import { solarToLunar } from '../../utils/lunar.js'
import dayjs from 'dayjs'
import PageHeader from '../../components/common/PageHeader.vue'
import TaskStatsCards from '../../components/task/TaskStatsCards.vue'
import MonthNavigator from '../../components/calendar/MonthNavigator.vue'

// 常量定义
const weekdays = ['日', '一', '二', '三', '四', '五', '六']
const priorities = ['普通', '重要', '紧急']
const categories = [
  { name: '待办', id: 1 },
  { name: '购物', id: 2 },
  { name: '家务', id: 3 },
  { name: '工作', id: 4 }
]
const filters = [
  { label: '全部', value: 'all' },
  { label: '待办', value: 'pending' },
  { label: '已完成', value: 'completed' }
]

// 响应式数据
const currentYear = ref(new Date().getFullYear())
const currentMonth = ref(new Date().getMonth() + 1)
const selectedDate = ref(new Date())
const tasks = ref([])
const loading = ref(false)
const showLunar = ref(true)
const swiperIndex = ref(1)
const showFilter = ref(false)
const currentFilter = ref('all')
const showModal = ref(false)
const showDetailModal = ref(false)
const selectedTask = ref(null)

// 新增任务表单
const newTask = ref({
  title: '',
  dueDate: '',
  dueTime: '',
  priority: 0,
  categoryId: 1
})

// 计算属性
const lunarInfo = computed(() => {
  const lunar = solarToLunar(currentYear.value, currentMonth.value, 1)
  return lunar ? { month: lunar.month } : null
})

// 月份统计数据
const monthStats = computed(() => {
  const currentMonthTasks = tasks.value.filter(task => {
    if (!task.dueDate) return false
    const taskDate = new Date(task.dueDate)
    return taskDate.getFullYear() === currentYear.value && 
           taskDate.getMonth() + 1 === currentMonth.value
  })
  
  const completed = currentMonthTasks.filter(t => t.status === 2).length
  const pending = currentMonthTasks.filter(t => t.status !== 2).length
  const overdue = currentMonthTasks.filter(t => isOverdue(t) && t.status !== 2).length
  
  return {
    total: currentMonthTasks.length,
    completed,
    pending,
    overdue
  }
})

// 可见月份数据（用于swiper）
const visibleMonths = computed(() => {
  const months = []
  
  // 上个月
  let prevYear = currentYear.value
  let prevMonth = currentMonth.value - 1
  if (prevMonth < 1) {
    prevMonth = 12
    prevYear--
  }
  months.push(generateMonthDays(prevYear, prevMonth))
  
  // 当前月
  months.push(generateMonthDays(currentYear.value, currentMonth.value))
  
  // 下个月
  let nextYear = currentYear.value
  let nextMonth = currentMonth.value + 1
  if (nextMonth > 12) {
    nextMonth = 1
    nextYear++
  }
  months.push(generateMonthDays(nextYear, nextMonth))
  
  return months
})

// 生成某月日期数据
const generateMonthDays = (year, month) => {
  const days = []
  const firstDay = new Date(year, month - 1, 1)
  const lastDay = new Date(year, month, 0)
  const firstDayWeek = firstDay.getDay()
  const daysInMonth = lastDay.getDate()
  
  // 上个月的日期
  const prevMonth = month === 1 ? 12 : month - 1
  const prevYear = month === 1 ? year - 1 : year
  const prevMonthDays = new Date(prevYear, prevMonth, 0).getDate()
  
  for (let i = firstDayWeek - 1; i >= 0; i--) {
    const date = prevMonthDays - i
    const fullDate = `${prevYear}-${String(prevMonth).padStart(2, '0')}-${String(date).padStart(2, '0')}`
    days.push(createDayObject(date, fullDate, false, prevYear, prevMonth))
  }
  
  // 当前月的日期
  const today = new Date()
  for (let i = 1; i <= daysInMonth; i++) {
    const fullDate = `${year}-${String(month).padStart(2, '0')}-${String(i).padStart(2, '0')}`
    const isToday = year === today.getFullYear() && 
                    month === today.getMonth() + 1 && 
                    i === today.getDate()
    days.push(createDayObject(i, fullDate, true, year, month, isToday))
  }
  
  // 下个月的日期
  const remainingCells = 42 - days.length // 6行 x 7列
  const nextMonth = month === 12 ? 1 : month + 1
  const nextYear = month === 12 ? year + 1 : year
  
  for (let i = 1; i <= remainingCells; i++) {
    const fullDate = `${nextYear}-${String(nextMonth).padStart(2, '0')}-${String(i).padStart(2, '0')}`
    days.push(createDayObject(i, fullDate, false, nextYear, nextMonth))
  }
  
  return { year, month, days }
}

// 创建日期对象
const createDayObject = (date, fullDate, isCurrentMonth, year, month, isToday = false) => {
  const dayTasks = tasks.value.filter(task => task.dueDate === fullDate)
  const completedTasks = dayTasks.filter(t => t.status === 2)
  const allCompleted = dayTasks.length > 0 && dayTasks.length === completedTasks.length
  const hasOverdue = dayTasks.some(t => isOverdue(t) && t.status !== 2)
  
  // 获取农历
  let lunar = ''
  if (showLunar.value) {
    const lunarInfo = solarToLunar(year, month, date)
    if (lunarInfo) {
      lunar = lunarInfo.day === '初一' ? lunarInfo.month : lunarInfo.day
    }
  }
  
  const dateObj = new Date(year, month - 1, date)
  const isWeekend = dateObj.getDay() === 0 || dateObj.getDay() === 6
  
  return {
    date,
    fullDate,
    year,
    month,
    isCurrentMonth,
    isToday,
    isWeekend,
    lunar,
    tasks: dayTasks,
    visibleTasks: dayTasks.slice(0, 3),
    allCompleted,
    hasOverdue
  }
}

// 选中日期的任务
const selectedDayTasks = computed(() => {
  const dateStr = dayjs(selectedDate.value).format('YYYY-MM-DD')
  return tasks.value.filter(task => task.dueDate === dateStr)
})

// 筛选后的任务
const filteredDayTasks = computed(() => {
  if (currentFilter.value === 'all') return selectedDayTasks.value
  if (currentFilter.value === 'pending') return selectedDayTasks.value.filter(t => t.status !== 2)
  if (currentFilter.value === 'completed') return selectedDayTasks.value.filter(t => t.status === 2)
  return selectedDayTasks.value
})

// 选中的日期字符串
const selectedDateStr = computed(() => {
  const today = dayjs().format('YYYY-MM-DD')
  const selected = dayjs(selectedDate.value).format('YYYY-MM-DD')
  
  if (selected === today) return '今天'
  if (selected === dayjs().add(1, 'day').format('YYYY-MM-DD')) return '明天'
  if (selected === dayjs().subtract(1, 'day').format('YYYY-MM-DD')) return '昨天'
  
  return dayjs(selectedDate.value).format('M月D日')
})

// 方法定义
const loadTasks = async () => {
  loading.value = true
  try {
    const familyId = uni.getStorageSync('currentFamilyId') || 1
    const res = await taskApi.getList(familyId)
    tasks.value = res || []
  } catch (e) {
    console.error('加载任务失败', e)
    uni.showToast({ title: '加载任务失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

const prevMonth = () => {
  swiperIndex.value = 0
}

const nextMonth = () => {
  swiperIndex.value = 2
}

const onSwiperChange = (e) => {
  const newIndex = e.detail.current
  
  if (newIndex === 0) {
    // 滑到上个月
    currentMonth.value--
    if (currentMonth.value < 1) {
      currentMonth.value = 12
      currentYear.value--
    }
    swiperIndex.value = 1
  } else if (newIndex === 2) {
    // 滑到下个月
    currentMonth.value++
    if (currentMonth.value > 12) {
      currentMonth.value = 1
      currentYear.value++
    }
    swiperIndex.value = 1
  }
}

const goToToday = () => {
  const today = new Date()
  currentYear.value = today.getFullYear()
  currentMonth.value = today.getMonth() + 1
  selectedDate.value = today
  swiperIndex.value = 1
}

const selectDay = (day) => {
  selectedDate.value = new Date(day.year, day.month - 1, day.date)
}

const isSelectedDay = (day) => {
  const selected = dayjs(selectedDate.value).format('YYYY-MM-DD')
  return day.fullDate === selected
}

const isOverdue = (task) => {
  if (!task.dueDate || task.status === 2) return false
  const dueDate = new Date(task.dueDate)
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  return dueDate < today
}

const priorityText = (p) => priorities[p] || '普通'

const subtaskCompleted = (task) => {
  if (!task.subtasks) return 0
  return task.subtasks.filter(s => s.status === 1).length
}

const subtaskProgress = (task) => {
  if (!task.subtasks || task.subtasks.length === 0) return 0
  return Math.round((subtaskCompleted(task) / task.subtasks.length) * 100)
}

const toggleTask = async (task) => {
  const newStatus = task.status === 2 ? 0 : 2
  try {
    if (newStatus === 2) {
      await taskApi.complete(task.id)
    } else {
      // 恢复任务状态
      await taskApi.update({ ...task, status: 0 })
    }
    task.status = newStatus
    uni.showToast({ title: newStatus === 2 ? '任务已完成' : '任务已恢复', icon: 'none' })
  } catch (e) {
    console.error('更新任务状态失败', e)
    uni.showToast({ title: '操作失败', icon: 'none' })
  }
}

const viewTask = (task) => {
  selectedTask.value = task
  showDetailModal.value = true
}

const closeDetailModal = () => {
  showDetailModal.value = false
  selectedTask.value = null
}

const toggleSubtask = async (sub) => {
  try {
    await taskApi.toggleSubtask(sub.id)
    sub.status = sub.status === 0 ? 1 : 0
  } catch (e) {
    console.error('切换子任务状态失败', e)
  }
}

const selectFilter = (filter) => {
  currentFilter.value = filter
  showFilter.value = false
}

const showAddModal = () => {
  newTask.value = {
    title: '',
    dueDate: dayjs(selectedDate.value).format('YYYY-MM-DD'),
    dueTime: '',
    priority: 0,
    categoryId: 1
  }
  showModal.value = true
}

const closeModal = () => {
  showModal.value = false
}

const onDateChange = (e) => {
  newTask.value.dueDate = e.detail.value
}

const onTimeChange = (e) => {
  newTask.value.dueTime = e.detail.value
}

const addTask = async () => {
  if (!newTask.value.title.trim()) {
    uni.showToast({ title: '请输入任务标题', icon: 'none' })
    return
  }

  try {
    const familyId = uni.getStorageSync('currentFamilyId') || 1
    await taskApi.create({
      ...newTask.value,
      familyId
    })
    uni.showToast({ title: '添加成功', icon: 'success' })
    closeModal()
    loadTasks()
  } catch (e) {
    console.error('创建任务失败', e)
    uni.showToast({ title: '创建失败', icon: 'none' })
  }
}

const editTask = (task) => {
  uni.navigateTo({
    url: `/pages/task/detail?id=${task.id}&mode=edit`
  })
}

const deleteTask = (task) => {
  uni.showModal({
    title: '确认删除',
    content: '确定要删除这个任务吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          await taskApi.delete(task.id)
          uni.showToast({ title: '已删除', icon: 'success' })
          closeDetailModal()
          loadTasks()
        } catch (e) {
          uni.showToast({ title: '删除失败', icon: 'none' })
        }
      }
    }
  })
}

const showTaskMenu = (task) => {
  const itemList = ['编辑', '删除', task.status === 2 ? '标记为未完成' : '标记为完成']
  uni.showActionSheet({
    itemList,
    success: (res) => {
      switch (res.tapIndex) {
        case 0:
          editTask(task)
          break
        case 1:
          deleteTask(task)
          break
        case 2:
          toggleTask(task)
          break
      }
    }
  })
}

const goBack = () => {
  uni.navigateBack()
}

const switchView = (view) => {
  switch (view) {
    case 'list':
      uni.navigateTo({ url: '/pages/task/index' })
      break
    case 'gantt':
      uni.navigateTo({ url: '/pages/task/gantt' })
      break
    case 'board':
      uni.navigateTo({ url: '/pages/task/board' })
      break
  }
}

// 生命周期
onMounted(() => {
  loadTasks()
})
</script>

<style lang="scss" scoped>
.page-container {
  min-height: 100vh;
  background: linear-gradient(180deg, #F8FAFC 0%, #F1F5F9 100%);
  padding-bottom: 100px;
}

// 头部
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 50px 20px 20px;
  background: linear-gradient(135deg, #667EEA 0%, #764BA2 100%);
  
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
    background: rgba(255,255,255,0.2);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    
    .icon {
      font-size: 24px;
      color: #fff;
      font-weight: 300;
    }
  }
}

// 统计卡片
.stats-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 10px;
  padding: 15px;
  margin-top: -10px;
  
  .stat-card {
    background: #fff;
    border-radius: 16px;
    padding: 15px 8px;
    text-align: center;
    box-shadow: 0 2px 8px rgba(0,0,0,0.04);
    
    &.completed {
      .stat-value { color: #10B981; }
      background: linear-gradient(135deg, #ECFDF5, #D1FAE5);
    }
    
    &.pending {
      .stat-value { color: #F59E0B; }
      background: linear-gradient(135deg, #FFFBEB, #FEF3C7);
    }
    
    &.overdue {
      .stat-value { color: #EF4444; }
      background: linear-gradient(135deg, #FEF2F2, #FEE2E2);
    }
    
    .stat-value {
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

// 日历区域
.calendar-section {
  background: #fff;
  margin: 0 15px 15px;
  border-radius: 20px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
}

// 月份导航
.month-navigator {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  
  .nav-btn {
    width: 36px;
    height: 36px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #F3F4F6;
    border-radius: 50%;
    
    .nav-icon {
      font-size: 20px;
      color: #4B5563;
      font-weight: 300;
    }
  }
  
  .month-display {
    text-align: center;
    
    .month-year {
      font-size: 13px;
      color: #9CA3AF;
      display: block;
    }
    
    .month-text {
      font-size: 22px;
      font-weight: 700;
      color: #1F2937;
    }
    
    .month-lunar {
      font-size: 11px;
      color: #9CA3AF;
      margin-left: 5px;
    }
  }
}

.today-btn {
  text-align: center;
  margin-bottom: 15px;
  
  text {
    font-size: 13px;
    color: #667EEA;
    background: rgba(102, 126, 234, 0.1);
    padding: 6px 16px;
    border-radius: 20px;
  }
}

// 星期标题
.weekdays {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 4px;
  margin-bottom: 8px;
  
  .weekday {
    text-align: center;
    font-size: 13px;
    color: #6B7280;
    font-weight: 500;
    padding: 8px 0;
    
    &.weekend {
      color: #EF4444;
    }
  }
}

// 日历滑动容器
.calendar-swiper {
  height: 320px;
}

// 日期网格
.days-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  grid-template-rows: repeat(6, 1fr);
  gap: 4px;
  height: 100%;
}

.day-cell {
  aspect-ratio: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
  padding: 4px;
  border-radius: 12px;
  position: relative;
  min-height: 44px;
  
  &.other-month {
    opacity: 0.4;
  }
  
  &.is-today {
    background: linear-gradient(135deg, #667EEA, #764BA2);
    
    .solar-date {
      color: #fff;
      font-weight: 600;
    }
    
    .lunar-date {
      color: rgba(255,255,255,0.7);
    }
  }
  
  &.is-selected:not(.is-today) {
    background: rgba(102, 126, 234, 0.1);
    border: 2px solid #667EEA;
  }
  
  &.has-tasks {
    .solar-date {
      font-weight: 600;
    }
  }
  
  &.all-completed {
    background: rgba(16, 185, 129, 0.08);
  }
  
  &.has-overdue {
    background: rgba(239, 68, 68, 0.05);
  }
  
  &.weekend:not(.is-today) {
    .solar-date {
      color: #EF4444;
    }
  }
}

.day-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
}

.solar-date {
  font-size: 15px;
  color: #374151;
  line-height: 1.2;
}

.lunar-date {
  font-size: 10px;
  color: #9CA3AF;
  margin-top: 2px;
}

// 任务指示器
.task-indicators {
  display: flex;
  align-items: center;
  gap: 3px;
  margin-top: 2px;
  
  .task-dot {
    width: 5px;
    height: 5px;
    border-radius: 50%;
    background: #F59E0B;
    
    &.completed {
      background: #10B981;
    }
    
    &.high-priority {
      background: #EF4444;
      width: 6px;
      height: 6px;
    }
    
    &.medium-priority {
      background: #F59E0B;
    }
  }
  
  .more-tasks {
    font-size: 9px;
    color: #9CA3AF;
    margin-left: 1px;
  }
}

.today-badge {
  position: absolute;
  top: 2px;
  right: 2px;
  font-size: 8px;
  color: #fff;
  background: rgba(255,255,255,0.3);
  padding: 1px 3px;
  border-radius: 4px;
}

// 任务区域
.tasks-section {
  background: #fff;
  margin: 0 15px;
  border-radius: 20px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
  max-height: 350px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  
  .section-title {
    display: flex;
    align-items: center;
    gap: 8px;
    
    .date-text {
      font-size: 18px;
      font-weight: 600;
      color: #1F2937;
    }
    
    .task-count {
      font-size: 14px;
      color: #9CA3AF;
    }
  }
  
  .filter-btn {
    font-size: 13px;
    color: #667EEA;
    background: rgba(102, 126, 234, 0.1);
    padding: 6px 12px;
    border-radius: 16px;
  }
}

.filter-dropdown {
  display: flex;
  gap: 10px;
  margin-bottom: 15px;
  padding-bottom: 15px;
  border-bottom: 1px solid #F3F4F6;
  
  .filter-option {
    font-size: 13px;
    color: #6B7280;
    padding: 6px 14px;
    background: #F3F4F6;
    border-radius: 16px;
    
    &.active {
      background: #667EEA;
      color: #fff;
    }
  }
}

.tasks-list {
  max-height: 240px;
}

// 任务卡片
.task-card {
  display: flex;
  align-items: flex-start;
  padding: 16px;
  background: #F9FAFB;
  border-radius: 16px;
  margin-bottom: 12px;
  transition: all 0.2s;
  
  &:active {
    transform: scale(0.98);
    background: #F3F4F6;
  }
  
  &.completed {
    opacity: 0.7;
    background: #ECFDF5;
    
    .task-title {
      text-decoration: line-through;
      color: #6B7280;
    }
  }
  
  &.overdue {
    background: #FEF2F2;
    border: 1px solid #FECACA;
  }
}

.task-checkbox {
  width: 24px;
  height: 24px;
  border: 2px solid #D1D5DB;
  border-radius: 50%;
  margin-right: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  margin-top: 2px;
  
  &.checked {
    background: #10B981;
    border-color: #10B981;
  }
  
  .check-icon {
    color: #fff;
    font-size: 12px;
    font-weight: bold;
  }
}

.task-content {
  flex: 1;
  min-width: 0;
}

.task-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 8px;
  
  .task-title {
    font-size: 15px;
    font-weight: 500;
    color: #1F2937;
    flex: 1;
    margin-right: 8px;
    line-height: 1.4;
  }
  
  .task-priority {
    font-size: 11px;
    padding: 3px 8px;
    border-radius: 10px;
    font-weight: 500;
    flex-shrink: 0;
    
    &.priority-0 {
      background: #E5E7EB;
      color: #6B7280;
    }
    
    &.priority-1 {
      background: #FEF3C7;
      color: #D97706;
    }
    
    &.priority-2 {
      background: #FEE2E2;
      color: #DC2626;
    }
  }
}

.task-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 8px;
  
  .meta-item {
    font-size: 12px;
    color: #6B7280;
    display: flex;
    align-items: center;
    gap: 4px;
    
    .meta-icon {
      font-size: 12px;
    }
  }
}

.subtask-progress {
  display: flex;
  align-items: center;
  gap: 8px;
  
  .progress-bar {
    flex: 1;
    height: 4px;
    background: #E5E7EB;
    border-radius: 2px;
    overflow: hidden;
    
    .progress-fill {
      height: 100%;
      background: #10B981;
      border-radius: 2px;
      transition: width 0.3s;
    }
  }
  
  .progress-text {
    font-size: 11px;
    color: #6B7280;
  }
}

.task-status {
  margin-left: 8px;
  
  .status-badge {
    font-size: 11px;
    padding: 4px 8px;
    border-radius: 8px;
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
}

// 空状态
.empty-state {
  text-align: center;
  padding: 40px 20px;
  
  .empty-icon {
    font-size: 48px;
    margin-bottom: 12px;
    display: block;
  }
  
  .empty-text {
    font-size: 14px;
    color: #9CA3AF;
    display: block;
    margin-bottom: 8px;
  }
  
  .empty-action {
    font-size: 13px;
    color: #667EEA;
    text-decoration: underline;
  }
}

// 底部快速操作
.quick-actions {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  justify-content: space-around;
  background: #fff;
  padding: 12px 20px calc(12px + env(safe-area-inset-bottom));
  box-shadow: 0 -2px 10px rgba(0,0,0,0.05);
  
  .action-btn {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 4px;
    padding: 8px 16px;
    border-radius: 12px;
    
    &.active {
      background: rgba(102, 126, 234, 0.1);
      
      .action-text {
        color: #667EEA;
      }
    }
    
    .action-icon {
      font-size: 20px;
    }
    
    .action-text {
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
  max-height: 80vh;
  background: #fff;
  border-radius: 24px;
  padding: 24px;
  overflow-y: auto;
  
  &.detail-modal {
    max-height: 70vh;
  }
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
    padding: 4px;
  }
}

// 表单样式
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

.priority-options, .category-options {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.priority-option, .category-option {
  padding: 10px 20px;
  background: #F3F4F6;
  border-radius: 12px;
  font-size: 14px;
  color: #6B7280;
  
  &.active {
    background: #667EEA;
    color: #fff;
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
    background: linear-gradient(135deg, #667EEA, #764BA2);
    color: #fff;
  }
}

// 任务详情样式
.task-detail {
  .detail-header {
    display: flex;
    align-items: flex-start;
    margin-bottom: 20px;
    
    .detail-checkbox {
      width: 28px;
      height: 28px;
      border: 2px solid #D1D5DB;
      border-radius: 50%;
      margin-right: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
      
      &.checked {
        background: #10B981;
        border-color: #10B981;
      }
      
      .check-icon {
        color: #fff;
        font-size: 14px;
        font-weight: bold;
      }
    }
    
    .detail-title {
      font-size: 18px;
      font-weight: 600;
      color: #1F2937;
      line-height: 1.4;
      flex: 1;
      
      &.completed {
        text-decoration: line-through;
        color: #9CA3AF;
      }
    }
  }
  
  .detail-info {
    background: #F9FAFB;
    border-radius: 16px;
    padding: 16px;
    margin-bottom: 20px;
    
    .info-item {
      display: flex;
      justify-content: space-between;
      padding: 10px 0;
      border-bottom: 1px solid #E5E7EB;
      
      &:last-child {
        border-bottom: none;
      }
      
      .info-label {
        font-size: 14px;
        color: #6B7280;
      }
      
      .info-value {
        font-size: 14px;
        color: #1F2937;
        font-weight: 500;
        
        &.priority-text-0 { color: #6B7280; }
        &.priority-text-1 { color: #D97706; }
        &.priority-text-2 { color: #DC2626; }
      }
    }
  }
}

.subtask-section {
  margin-bottom: 20px;
  
  .section-title {
    font-size: 15px;
    font-weight: 600;
    color: #374151;
    margin-bottom: 12px;
  }
  
  .subtask-item {
    display: flex;
    align-items: center;
    padding: 12px 0;
    border-bottom: 1px solid #F3F4F6;
    
    .subtask-checkbox {
      width: 20px;
      height: 20px;
      border: 2px solid #D1D5DB;
      border-radius: 50%;
      margin-right: 12px;
      
      &.checked {
        background: #10B981;
        border-color: #10B981;
      }
    }
    
    .subtask-title {
      font-size: 14px;
      color: #374151;
      flex: 1;
      
      &.completed {
        text-decoration: line-through;
        color: #9CA3AF;
      }
    }
  }
}

.detail-actions {
  display: flex;
  gap: 12px;
  
  .detail-btn {
    flex: 1;
    padding: 14px;
    border-radius: 12px;
    text-align: center;
    font-size: 14px;
    font-weight: 500;
    
    &.edit {
      background: #DBEAFE;
      color: #2563EB;
    }
    
    &.delete {
      background: #FEE2E2;
      color: #DC2626;
    }
  }
}
</style>
