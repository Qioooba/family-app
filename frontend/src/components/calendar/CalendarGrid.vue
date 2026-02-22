<template>
  <view class="calendar-section">
    <!-- 月份导航 -->
    <MonthNavigator 
      :year="currentYear"
      :month="currentMonth"
      :lunar="lunarInfo?.month"
      @prev="$emit('prevMonth')"
      @next="$emit('nextMonth')"
    />
    
    <!-- 回到今天 -->
    <view class="today-btn" @click="$emit('goToday')">
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
      @change="$emit('swiperChange', $event)"
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
            @click="$emit('selectDay', day)"
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
import MonthNavigator from './MonthNavigator.vue'

const props = defineProps({
  currentYear: {
    type: Number,
    required: true
  },
  currentMonth: {
    type: Number,
    required: true
  },
  swiperIndex: {
    type: Number,
    default: 1
  },
  visibleMonths: {
    type: Array,
    required: true
  },
  selectedDate: {
    type: Date,
    required: true
  },
  showLunar: {
    type: Boolean,
    default: true
  },
  lunarInfo: {
    type: Object,
    default: null
  }
})

const weekdays = ['日', '一', '二', '三', '四', '五', '六']

const isSelectedDay = (day) => {
  const selected = new Date(props.selectedDate)
  const selectedStr = `${selected.getFullYear()}-${String(selected.getMonth() + 1).padStart(2, '0')}-${String(selected.getDate()).padStart(2, '0')}`
  return day.fullDate === selectedStr
}

defineEmits(['prevMonth', 'nextMonth', 'goToday', 'swiperChange', 'selectDay'])
</script>

<style lang="scss" scoped>
.calendar-section {
  background: #fff;
  margin: 0 15px 15px;
  border-radius: 20px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
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
</style>
