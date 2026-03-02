<template>
  <view class="page-container">
    <!-- 头部 -->
    <view class="header">
      <view class="header-left" @click="goBack">
        <text class="back-icon">‹</text>
      </view>
      <view class="header-title">任务热力图</view>
      <view class="header-action" @click="showDatePicker">
        <text class="icon">📅</text>
      </view>
    </view>
    
    <!-- 统计概览 -->
    <view class="stats-overview">
      <view class="stat-card">
        <text class="stat-number">{{ stats.total }}</text>
        <text class="stat-label">年度完成</text>
      </view>
      <view class="stat-card streak">
        <text class="stat-number">{{ stats.streak }}</text>
        <text class="stat-label">连续天数</text>
      </view>
      <view class="stat-card best">
        <text class="stat-number">{{ stats.best }}</text>
        <text class="stat-label">单日最佳</text>
      </view>
      <view class="stat-card avg">
        <text class="stat-number">{{ stats.avg }}</text>
        <text class="stat-label">日均完成</text>
      </view>
    </view>
    
    <!-- 当前月份/年份显示 -->
    <view class="date-display">
      <text class="year-text">{{ currentYear }}年</text>
      <view class="month-selector">
        <text class="nav-arrow" @click="prevYear">‹</text>
        <view class="months-row">
          <text 
            v-for="(m, index) in 12" :key="m.id || index" 
            
            class="month-tag"
            :class="{ active: selectedMonth === m }"
            @click="selectMonth(m)"
          >{{ m }}月</text>
        </view>
        <text class="nav-arrow" @click="nextYear">›</text>
      </view>
    </view>
    
    <!-- 热力图主体 -->
    <view class="heatmap-container">
      <view class="heatmap-header">
        <text class="heatmap-title">{{ selectedMonth }}月完成热力图</text>
        <view class="level-legend">
          <text>少</text>
          <view class="level-box level-0"></view>
          <view class="level-box level-1"></view>
          <view class="level-box level-2"></view>
          <view class="level-box level-3"></view>
          <view class="level-box level-4"></view>
          <text>多</text>
        </view>
      </view>
      
      <!-- 月份热力图 -->
      <view class="month-heatmap">
        <!-- 星期标题 -->
        <view class="weekday-labels">
          <text v-for="(day, index) in weekdays" :key="day.id || index" >{{ day }}</text>
        </view>
        
        <!-- 日期网格 -->
        <view class="days-grid">
          <view 
            v-for="(day, index) in monthDays" 
            :key="index"
            class="day-cell"
            :class="{ 
              'other-month': !day.isCurrentMonth,
              'is-today': day.isToday,
              [`level-${day.level}`]: true
            }"
            @click="showDayDetail(day)"
          >
            <text class="day-number">{{ day.date }}</text>
            <text v-if="day.count > 0" class="day-count">{{ day.count }}</text>
          </view>
        </view>
      </view>
    </view>
    
    <!-- 年度热力图 -->
    <view class="year-heatmap-section">
      <view class="section-header">
        <text class="section-title">{{ currentYear }}年概览</text>
        <text class="section-subtitle">{{ yearTotalCompleted }} 次完成</text>
      </view>
      
      <view class="year-grid">
        <view 
          v-for="(week, wIndex) in yearWeeks" 
          :key="wIndex"
          class="week-column"
        >
          <view 
            v-for="(day, dIndex) in week" 
            :key="dIndex"
            class="year-day"
            :class="{ [`level-${day.level}`]: day.level >= 0 }"
            @click="showDayDetail(day)">          ></view>
        </view>
      </view>
      
      <view class="year-labels">
        <text v-for="month in [1,4,7,10]" :key="month">{{ month }}月</text>
      </view>
    </view>
    
    <!-- 分类统计 -->
    <view class="category-stats" v-if="categoryStats.length > 0">
      <view class="section-header">
        <text class="section-title">分类统计</text>
      </view>
      
      <view class="category-list">
        <view 
          v-for="(cat, index) in categoryStats" :key="cat.id || index" 
          
          class="category-item"
        >
          <view class="category-info">
            <text class="category-name">{{ cat.name }}</text>
            <text class="category-count">{{ cat.count }} 次</text>
          </view>
          
          <view class="category-bar">
            <view class="category-progress" :style="{ width: cat.percentage + '%', background: cat.color }"></view>
          </view>
          
          <text class="category-percent">{{ cat.percentage }}%</text>
        </view>
      </view>
    </view>
    
    <!-- 每日详情弹窗 -->
    <view v-if="selectedDay" class="modal-overlay" @click="closeDayDetail">
      <view class="modal-content" @click.stop>
        <view class="modal-header">
          <text class="modal-title">{{ selectedDay.fullDate }} 完成详情</text>
          <text class="close-btn" @click="closeDayDetail">✕</text>
        </view>
        
        <view class="modal-body">
          <view class="day-summary">
            <text class="summary-number">{{ selectedDay.count }}</text>
            <text class="summary-label">完成任务数</text>
          </view>
          
          <view class="completed-list" v-if="selectedDay.tasks?.length > 0">
            <view 
              v-for="task in selectedDay.tasks" 
              :key="task.id"
              class="completed-item"
            >
              <text class="item-icon">✓</text>
              <text class="item-title">{{ task.title }}</text>
              <text class="item-time">{{ task.completedTime }}</text>
            </view>
          </view>
          
          <view v-else class="empty-day">
            <text>这一天没有完成任务</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { taskApi } from '../../api/index.js'
import dayjs from 'dayjs'

// 常量
const weekdays = ['日', '一', '二', '三', '四', '五', '六']
const categoryColors = ['#10B981', '#3B82F6', '#F59E0B', '#EC4899', '#8B5CF6', '#EF4444']

// 响应式数据
const currentYear = ref(dayjs().year())
const selectedMonth = ref(dayjs().month() + 1)
const completedTasks = ref([])
const selectedDay = ref(null)

// 计算属性
const stats = computed(() => {
  const total = completedTasks.value.length
  
  // 计算连续天数
  const dates = [...new Set(completedTasks.value.map(t => t.completedDate))].sort()
  let streak = 0
  let maxStreak = 0
  let lastDate = null
  
  dates.forEach(date => {
    if (lastDate && dayjs(date).diff(dayjs(lastDate), 'day') === 1) {
      streak++
    } else {
      streak = 1
    }
    maxStreak = Math.max(maxStreak, streak)
    lastDate = date
  })
  
  // 检查今天的连续
  const today = dayjs().format('YYYY-MM-DD')
  if (!dates.includes(today)) {
    // 如果今天没完成，检查昨天
    const yesterday = dayjs().subtract(1, 'day').format('YYYY-MM-DD')
    if (!dates.includes(yesterday)) {
      streak = 0
    }
  }
  
  // 单日最佳
  const dailyCounts = {}
  completedTasks.value.forEach(t => {
    dailyCounts[t.completedDate] = (dailyCounts[t.completedDate] || 0) + 1
  })
  const best = Math.max(0, ...Object.values(dailyCounts))
  
  // 日均完成
  const daysWithData = Object.keys(dailyCounts).length || 1
  const avg = Math.round(total / daysWithData * 10) / 10
  
  return { total, streak: maxStreak, best, avg }
})

const monthDays = computed(() => {
  const days = []
  const firstDay = dayjs(`${currentYear.value}-${selectedMonth.value}-1`)
  const startOfMonth = firstDay.startOf('month')
  const endOfMonth = firstDay.endOf('month')
  
  // 获取日历起始日（周日开始）
  const startOfCalendar = startOfMonth.startOf('week')
  const endOfCalendar = endOfMonth.endOf('week')
  
  let current = startOfCalendar
  while (current.isBefore(endOfCalendar) || current.isSame(endOfCalendar, 'day')) {
    const dateStr = current.format('YYYY-MM-DD')
    const dayTasks = completedTasks.value.filter(t => t.completedDate === dateStr)
    const count = dayTasks.length
    
    // 计算热力等级
    let level = 0
    if (count > 0) level = 1
    if (count >= 3) level = 2
    if (count >= 5) level = 3
    if (count >= 8) level = 4
    
    days.push({
      date: current.date(),
      fullDate: dateStr,
      isCurrentMonth: current.month() + 1 === selectedMonth.value,
      isToday: current.isSame(dayjs(), 'day'),
      count,
      level,
      tasks: dayTasks
    })
    
    current = current.add(1, 'day')
  }
  
  return days
})

const yearWeeks = computed(() => {
  const weeks = []
  const startOfYear = dayjs(`${currentYear.value}-01-01`).startOf('week')
  const endOfYear = dayjs(`${currentYear.value}-12-31`).endOf('week')
  
  let currentWeek = []
  let current = startOfYear
  
  while (current.isBefore(endOfYear) || current.isSame(endOfYear, 'day')) {
    const dateStr = current.format('YYYY-MM-DD')
    const count = completedTasks.value.filter(t => t.completedDate === dateStr).length
    
    let level = -1
    if (current.year() === currentYear.value) {
      level = 0
      if (count > 0) level = 1
      if (count >= 3) level = 2
      if (count >= 5) level = 3
      if (count >= 8) level = 4
    }
    
    currentWeek.push({
      date: current.date(),
      fullDate: dateStr,
      level,
      count
    })
    
    if (currentWeek.length === 7) {
      weeks.push(currentWeek)
      currentWeek = []
    }
    
    current = current.add(1, 'day')
  }
  
  if (currentWeek.length > 0) {
    weeks.push(currentWeek)
  }
  
  return weeks
})

const yearTotalCompleted = computed(() => {
  return completedTasks.value.filter(t => 
    t.completedDate.startsWith(String(currentYear.value))
  ).length
})

const categoryStats = computed(() => {
  const categories = {}
  completedTasks.value.forEach(task => {
    const cat = task.categoryName || '未分类'
    categories[cat] = (categories[cat] || 0) + 1
  })
  
  const total = completedTasks.value.length || 1
  const sorted = Object.entries(categories)
    .sort((a, b) => b[1] - a[1])
    .slice(0, 6)
  
  return sorted.map(([name, count], index) => ({
    name,
    count,
    percentage: Math.round(count / total * 100),
    color: categoryColors[index % categoryColors.length]
  }))
})

// 方法
const loadCompletedTasks = async () => {
  try {
    const familyId = uni.getStorageSync('currentFamilyId') || 1
    // 实际应该调用获取已完成任务历史的API
    // const res = await taskApi.getCompletedHistory(familyId, currentYear.value)
    
    // 模拟数据
    generateMockData()
  } catch (e) {
    console.error('加载数据失败', e)
  }
}

const generateMockData = () => {
  const mockData = []
  const categories = ['家务', '购物', '工作', '学习', '运动', '其他']
  
  // 生成过去一年的模拟完成记录
  for (let i = 0; i < 365; i++) {
    const date = dayjs().subtract(i, 'day')
    
    // 随机生成每天完成的数量（大部分天数0-3个，少数天数更多）
    const probability = Math.random()
    let count = 0
    if (probability > 0.6) count = Math.floor(Math.random() * 3) + 1
    if (probability > 0.9) count = Math.floor(Math.random() * 5) + 3
    
    for (let j = 0; j < count; j++) {
      mockData.push({
        id: `task-${i}-${j}`,
        title: `完成任务 ${i}-${j}`,
        completedDate: date.format('YYYY-MM-DD'),
        completedTime: date.format('HH:mm'),
        categoryName: categories[Math.floor(Math.random() * categories.length)]
      })
    }
  }
  
  completedTasks.value = mockData
}

const prevYear = () => {
  currentYear.value--
  loadCompletedTasks()
}

const nextYear = () => {
  currentYear.value++
  loadCompletedTasks()
}

const selectMonth = (month) => {
  selectedMonth.value = month
}

const showDatePicker = () => {
  uni.showActionSheet({
    itemList: ['选择年份', '查看本月', '查看全部'],
    success: (res) => {
      if (res.tapIndex === 1) {
        selectedMonth.value = dayjs().month() + 1
        currentYear.value = dayjs().year()
      }
    }
  })
}

const showDayDetail = (day) => {
  selectedDay.value = day
}

const closeDayDetail = () => {
  selectedDay.value = null
}

const goBack = () => {
  uni.navigateBack()
}

onMounted(() => {
  loadCompletedTasks()
})
</script>

<style lang="scss" scoped>
.page-container {
  min-height: 100vh;
  background: linear-gradient(180deg, #F8FAFC 0%, #F1F5F9 100%);
  padding-bottom: 30px;
}

// 头部
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 50px 20px 20px;
  background: linear-gradient(135deg, #14B8A6 0%, #0D9488 100%);
  
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

// 统计概览
.stats-overview {
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
    
    &.streak .stat-number {
      color: #F59E0B;
    }
    
    &.best .stat-number {
      color: #10B981;
    }
    &.avg .stat-number {
      color: #3B82F6;
    }
    
    .stat-number {
      font-size: 22px;
      font-weight: 700;
      color: #14B8A6;
      display: block;
      margin-bottom: 4px;
    }
    
    .stat-label {
      font-size: 10px;
      color: #9CA3AF;
    }
  }
}

// 日期显示
.date-display {
  background: #fff;
  margin: 0 15px 15px;
  padding: 16px;
  border-radius: 16px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  
  .year-text {
    font-size: 16px;
    font-weight: 600;
    color: #1F2937;
    display: block;
    margin-bottom: 12px;
  }
  
  .month-selector {
    display: flex;
    align-items: center;
    gap: 10px;
    
    .nav-arrow {
      font-size: 18px;
      color: #9CA3AF;
      padding: 5px;
    }
    
    .months-row {
      flex: 1;
      display: flex;
      flex-wrap: wrap;
      gap: 6px;
      justify-content: center;
      
      .month-tag {
        font-size: 12px;
        padding: 6px 10px;
        background: #F3F4F6;
        border-radius: 10px;
        color: #6B7280;
        
        &.active {
          background: #14B8A6;
          color: #fff;
          font-weight: 500;
        }
      }
    }
  }
}

// 热力图容器
.heatmap-container {
  background: #fff;
  margin: 0 15px 15px;
  padding: 16px;
  border-radius: 16px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
}

.heatmap-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  
  .heatmap-title {
    font-size: 15px;
    font-weight: 600;
    color: #1F2937;
  }
  
  .level-legend {
    display: flex;
    align-items: center;
    gap: 4px;
    
    text {
      font-size: 11px;
      color: #9CA3AF;
    }
    
    .level-box {
      width: 12px;
      height: 12px;
      border-radius: 3px;
      
      &.level-0 { background: #E5E7EB; }
      &.level-1 { background: #99F6E4; }
      &.level-2 { background: #5EEAD4; }
      &.level-3 { background: #2DD4BF; }
      &.level-4 { background: #0D9488; }
    }
  }
}

// 月份热力图
.month-heatmap {
  .weekday-labels {
    display: grid;
    grid-template-columns: repeat(7, 1fr);
    gap: 6px;
    margin-bottom: 8px;
    
    text {
      text-align: center;
      font-size: 12px;
      color: #9CA3AF;
    }
  }
  
  .days-grid {
    display: grid;
    grid-template-columns: repeat(7, 1fr);
    gap: 6px;
  }
  
  .day-cell {
    aspect-ratio: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    border-radius: 8px;
    background: #E5E7EB;
    transition: all 0.2s;
    
    &.other-month {
      opacity: 0.3;
    }
    
    &.is-today {
      box-shadow: 0 0 0 2px #14B8A6;
    }
    
    &.level-1 { background: #99F6E4; }
    &.level-2 { background: #5EEAD4; }
    &.level-3 { background: #2DD4BF; }
    &.level-4 { background: #0D9488; }
    
    .day-number {
      font-size: 12px;
      font-weight: 500;
      color: #374151;
    }
    
    .day-count {
      font-size: 9px;
      color: #fff;
      margin-top: 2px;
    }
  }
}

// 年度热力图
.year-heatmap-section {
  background: #fff;
  margin: 0 15px 15px;
  padding: 16px;
  border-radius: 16px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  
  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    
    .section-title {
      font-size: 15px;
      font-weight: 600;
      color: #1F2937;
    }
    
    .section-subtitle {
      font-size: 13px;
      color: #9CA3AF;
    }
  }
  
  .year-grid {
    display: flex;
    gap: 3px;
    overflow-x: auto;
    padding-bottom: 8px;
    
    .week-column {
      display: flex;
      flex-direction: column;
      gap: 3px;
      
      .year-day {
        width: 10px;
        height: 10px;
        border-radius: 2px;
        background: #E5E7EB;
        
        &.level-1 { background: #99F6E4; }
        &.level-2 { background: #5EEAD4; }
        &.level-3 { background: #2DD4BF; }
        &.level-4 { background: #0D9488; }
      }
    }
  }
  
  .year-labels {
    display: flex;
    justify-content: space-between;
    margin-top: 8px;
    padding-right: 20px;
    
    text {
      font-size: 10px;
      color: #9CA3AF;
    }
  }
}

// 分类统计
.category-stats {
  background: #fff;
  margin: 0 15px;
  padding: 16px;
  border-radius: 16px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  
  .section-header {
    margin-bottom: 16px;
    
    .section-title {
      font-size: 15px;
      font-weight: 600;
      color: #1F2937;
    }
  }
  
  .category-list {
    .category-item {
      margin-bottom: 14px;
      
      &:last-child {
        margin-bottom: 0;
      }
      
      .category-info {
        display: flex;
        justify-content: space-between;
        margin-bottom: 6px;
        
        .category-name {
          font-size: 14px;
          color: #374151;
        }
        
        .category-count {
          font-size: 13px;
          color: #9CA3AF;
        }
      }
      
      .category-bar {
        flex: 1;
        height: 8px;
        background: #F3F4F6;
        border-radius: 4px;
        overflow: hidden;
        margin-right: 10px;
        
        .category-progress {
          height: 100%;
          border-radius: 4px;
          transition: width 0.5s;
        }
      }
      
      .category-percent {
        font-size: 12px;
        color: #6B7280;
        min-width: 35px;
        text-align: right;
      }
    }
  }
}

// 弹窗
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
  max-height: 60vh;
  background: #fff;
  border-radius: 24px;
  overflow: hidden;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  background: linear-gradient(135deg, #14B8A6, #0D9488);
  
  .modal-title {
    font-size: 17px;
    font-weight: 600;
    color: #fff;
  }
  
  .close-btn {
    font-size: 20px;
    color: rgba(255,255,255,0.8);
  }
}

.modal-body {
  padding: 20px;
  max-height: 300px;
  overflow-y: auto;
}

.day-summary {
  text-align: center;
  padding: 20px 0;
  border-bottom: 1px solid #F3F4F6;
  margin-bottom: 16px;
  
  .summary-number {
    font-size: 48px;
    font-weight: 700;
    color: #14B8A6;
    display: block;
  }
  
  .summary-label {
    font-size: 14px;
    color: #9CA3AF;
  }
}

.completed-list {
  .completed-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 12px 0;
    border-bottom: 1px solid #F3F4F6;
    
    &:last-child {
      border-bottom: none;
    }
    
    .item-icon {
      width: 24px;
      height: 24px;
      background: #D1FAE5;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 12px;
      color: #059669;
    }
    
    .item-title {
      flex: 1;
      font-size: 14px;
      color: #374151;
    }
    
    .item-time {
      font-size: 12px;
      color: #9CA3AF;
    }
  }
}

.empty-day {
  text-align: center;
  padding: 40px;
  
  text {
    font-size: 14px;
    color: #9CA3AF;
  }
}
</style>
