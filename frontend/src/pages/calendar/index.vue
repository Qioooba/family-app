<template>
  <view class="page-container">
    <view class="header">
      <view class="header-title">纪念日 📅</view>
      <view class="header-action" @click="showAddModal">
        <text class="icon">+</text>
      </view>
    </view>
    
    <view class="calendar-section">
      <view class="month-header">
        <text class="month-text">{{ currentYear }}年{{ currentMonth }}月</text>
      </view>
      
      <view class="weekdays">
        <text v-for="day in weekdays" :key="day">{{ day }}</text>
      </view>
      
      <view class="days-grid">
        <view 
          v-for="(day, index) in daysInMonth" 
          :key="index"
          class="day-cell"
          :class="{ today: isToday(day), hasEvent: hasEvent(day) }"
          @click="selectDay(day)"
        >
          <text>{{ day }}</text>
          <view v-if="hasEvent(day)" class="event-dot"></view>
        </view>
      </view>
    </view>
    
    <view class="events-section">
      <view class="section-title">即将到来的纪念日</view>
      
      <scroll-view class="events-list" scroll-y>
        <view 
          v-for="(event, index) in upcomingEvents" 
          :key="index"
          class="event-card"
        >
          <view class="event-icon">{{ event.icon }}</view>
          
          <view class="event-info">
            <view class="event-title">{{ event.title }}</view>
            <view class="event-date">{{ event.date }}</view>
          </view>
          
          <view class="event-countdown">
            <text class="days">{{ event.days }}</text>
            <text class="label">天后</text>
          </view>
        </view>
      </scroll-view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'

const currentYear = ref(2025)
const currentMonth = ref(2)
const weekdays = ['日', '一', '二', '三', '四', '五', '六']

const daysInMonth = ref([1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28])

const events = ref([
  { day: 14, title: '情人节' },
  { day: 21, title: '家庭聚餐' },
  { day: 25, title: '结婚纪念日' }
])

const upcomingEvents = ref([
  { icon: '💒', title: '结婚纪念日', date: '2025年2月25日', days: 4 },
  { icon: '👨', title: '爸爸生日', date: '2025年3月5日', days: 12 },
  { icon: '🎂', title: '宝贝生日', date: '2025年3月15日', days: 22 }
])

const isToday = (day) => {
  return day === 21
}

const hasEvent = (day) => {
  return events.value.some(e => e.day === day)
}

const selectDay = (day) => {
  const event = events.value.find(e => e.day === day)
  if (event) {
    uni.showModal({
      title: event.title,
      content: `${currentYear.value}年${currentMonth.value}月${day}日`,
      showCancel: true,
      confirmText: '编辑',
      cancelText: '关闭'
    })
  } else {
    uni.showToast({ title: `${day}日 无事件`, icon: 'none' })
  }
}

const showAddModal = () => {
  uni.showModal({
    title: '添加纪念日',
    editable: true,
    placeholderText: '输入纪念日名称...',
    success: (res) => {
      if (res.confirm && res.content) {
        uni.showToast({ title: '添加成功', icon: 'success' })
      }
    }
  })
}
</script>

<style lang="scss" scoped>
.page-container {
  min-height: 100vh;
  background: #F5F7FA;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 50px 20px 20px;
  background: linear-gradient(135deg, #F3E5F5, #E1BEE7);
  
  .header-title {
    font-size: 24px;
    font-weight: 700;
    color: #2C3E50;
  }
  
  .header-action {
    width: 44px;
    height: 44px;
    background: #9C27B0;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    
    .icon {
      font-size: 28px;
      color: #fff;
      font-weight: 300;
    }
  }
}

.calendar-section {
  background: #fff;
  padding: 20px;
  
  .month-header {
    text-align: center;
    margin-bottom: 20px;
    
    .month-text {
      font-size: 18px;
      font-weight: 600;
      color: #2C3E50;
    }
  }
  
  .weekdays {
    display: flex;
    justify-content: space-around;
    margin-bottom: 10px;
    
    text {
      font-size: 14px;
      color: #7F8C8D;
    }
  }
  
  .days-grid {
    display: grid;
    grid-template-columns: repeat(7, 1fr);
    gap: 8px;
    
    .day-cell {
      aspect-ratio: 1;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      border-radius: 10px;
      font-size: 14px;
      position: relative;
      
      &.today {
        background: #9C27B0;
        color: #fff;
      }
      
      .event-dot {
        width: 6px;
        height: 6px;
        background: #F44336;
        border-radius: 50%;
        position: absolute;
        bottom: 4px;
      }
    }
  }
}

.events-section {
  padding: 20px;
  
  .section-title {
    font-size: 16px;
    font-weight: 600;
    color: #2C3E50;
    margin-bottom: 15px;
  }
}

.events-list {
  height: calc(100vh - 500px);
}

.event-card {
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 16px;
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  
  .event-icon {
    font-size: 36px;
    margin-right: 16px;
  }
  
  .event-info {
    flex: 1;
    
    .event-title {
      font-size: 16px;
      font-weight: 600;
      color: #2C3E50;
      margin-bottom: 4px;
    }
    
    .event-date {
      font-size: 13px;
      color: #7F8C8D;
    }
  }
  
  .event-countdown {
    text-align: center;
    padding: 8px 16px;
    background: #F3E5F5;
    border-radius: 12px;
    
    .days {
      display: block;
      font-size: 24px;
      font-weight: 700;
      color: #9C27B0;
    }
    
    .label {
      font-size: 11px;
      color: #9C27B0;
    }
  }
}
</style>
