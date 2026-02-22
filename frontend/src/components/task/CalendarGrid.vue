<template>
  <!-- 日历网格 -->
  <view class="calendar-grid">
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
            @click="$emit('select-day', day)"
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
              ></view>
              <text v-if="day.tasks.length > 3" class="more-tasks">+{{ day.tasks.length - 3 }}</text>
            </view>
            
            <!-- 今日标记 -->
            <view v-if="day.isToday" class="today-badge">今</view>
          </view>
        </view>
      </swiper-item>
    </swiper>
  </view>
</template>

<script setup>
import { ref, computed, watch } from 'vue'

const props = defineProps({
  currentYear: Number,
  currentMonth: Number,
  selectedDate: Date,
  tasks: Array,
  showLunar: Boolean,
  swiperIndex: Number
})

const emit = defineEmits(['select-day', 'swiper-change'])

const weekdays = ['日', '一', '二', '三', '四', '五', '六']

// 生成月份数据
const generateMonthData = (year, month) => {
  const firstDay = new Date(year, month - 1, 1)
  const lastDay = new Date(year, month, 0)
  const daysInMonth = lastDay.getDate()
  const startWeekday = firstDay.getDay()
  
  const days = []
  
  // 上个月的日期
  const prevMonthLastDay = new Date(year, month - 1, 0).getDate()
  for (let i = startWeekday - 1; i >= 0; i--) {
    days.push({
      date: prevMonthLastDay - i,
      isCurrentMonth: false,
      isToday: false,
      tasks: [],
      fullDate: new Date(year, month - 2, prevMonthLastDay - i)
    })
  }
  
  // 当前月的日期
  const today = new Date()
  for (let i = 1; i <= daysInMonth; i++) {
    const date = new Date(year, month - 1, i)
    const dayTasks = props.tasks.filter(task => {
      if (!task.dueDate) return false
      const taskDate = new Date(task.dueDate)
      return taskDate.toDateString() === date.toDateString()
    })
    
    days.push({
      date: i,
      isCurrentMonth: true,
      isToday: date.toDateString() === today.toDateString(),
      isWeekend: date.getDay() === 0 || date.getDay() === 6,
      tasks: dayTasks,
      visibleTasks: dayTasks.slice(0, 3),
      allCompleted: dayTasks.length > 0 && dayTasks.every(t => t.status === 2),
      hasOverdue: dayTasks.some(t => isOverdue(t) && t.status !== 2),
      fullDate: date
    })
  }
  
  // 下个月的日期
  const remainingCells = 42 - days.length
  for (let i = 1; i <= remainingCells; i++) {
    days.push({
      date: i,
      isCurrentMonth: false,
      isToday: false,
      tasks: [],
      fullDate: new Date(year, month, i)
    })
  }
  
  return { days }
}

const isOverdue = (task) => {
  if (!task.dueDate || task.status === 2) return false
  const due = new Date(task.dueDate)
  const now = new Date()
  return due < now && due.toDateString() !== now.toDateString()
}

const isSelectedDay = (day) => {
  return day.fullDate.toDateString() === props.selectedDate.toDateString()
}

// 三个月数据（前月、当月、下月）
const visibleMonths = computed(() => {
  const prevMonth = props.currentMonth === 1 ? 12 : props.currentMonth - 1
  const prevYear = props.currentMonth === 1 ? props.currentYear - 1 : props.currentYear
  const nextMonth = props.currentMonth === 12 ? 1 : props.currentMonth + 1
  const nextYear = props.currentMonth === 12 ? props.currentYear + 1 : props.currentYear
  
  return [
    generateMonthData(prevYear, prevMonth),
    generateMonthData(props.currentYear, props.currentMonth),
    generateMonthData(nextYear, nextMonth)
  ]
})

const onSwiperChange = (e) => {
  emit('swiper-change', e.detail.current)
}
</script>

<style lang="scss" scoped>
.weekdays {
  display: flex;
  padding: 20rpx 0;
  
  .weekday {
    flex: 1;
    text-align: center;
    font-size: 26rpx;
    color: #666;
    
    &.weekend {
      color: #ff6b6b;
    }
  }
}

.calendar-swiper {
  height: 600rpx;
}

.days-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 8rpx;
  padding: 0 20rpx;
}

.day-cell {
  aspect-ratio: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
  padding: 8rpx 4rpx;
  border-radius: 12rpx;
  position: relative;
  min-height: 90rpx;
  
  &.other-month {
    opacity: 0.3;
  }
  
  &.is-today {
    background: #e3f2fd;
  }
  
  &.is-selected {
    background: #4caf50;
    .solar-date, .lunar-date {
      color: #fff;
    }
  }
  
  &.has-tasks:not(.is-selected) {
    background: #f5f5f5;
  }
  
  &.all-completed:not(.is-selected) {
    background: #e8f5e9;
  }
  
  &.has-overdue:not(.is-selected) {
    background: #ffebee;
  }
}

.day-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  
  .solar-date {
    font-size: 28rpx;
    font-weight: 500;
    color: #333;
  }
  
  .lunar-date {
    font-size: 20rpx;
    color: #999;
    margin-top: 4rpx;
  }
}

.task-indicators {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 4rpx;
  margin-top: 8rpx;
  max-width: 60rpx;
  
  .task-dot {
    width: 8rpx;
    height: 8rpx;
    border-radius: 50%;
    background: #999;
    
    &.completed {
      background: #4caf50;
    }
    
    &.high-priority {
      background: #f44336;
    }
    
    &.medium-priority {
      background: #ff9800;
    }
  }
  
  .more-tasks {
    font-size: 18rpx;
    color: #999;
  }
}

.today-badge {
  position: absolute;
  top: 4rpx;
  right: 4rpx;
  width: 32rpx;
  height: 32rpx;
  background: #2196f3;
  color: #fff;
  font-size: 20rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
