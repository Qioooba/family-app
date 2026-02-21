<template>
  <view class="calendar-page">
    <!-- 顶部日历 -->
    <view class="calendar-header"
      <view class="header-bg"></view>
      
      <view class="month-selector"
        <u-icon name="arrow-left" size="36" color="#fff" @click="changeMonth(-1)"></u-icon>
        
        <text class="month-text">{{ currentYearMonth }}</text>
        
        <u-icon name="arrow-right" size="36" color="#fff" @click="changeMonth(1)"></u-icon>
      </view>
      
      <view class="calendar-grid"
        <view class="week-row"
          <text v-for="day in weekDays" :key="day" class="week-day">{{ day }}</text>
        </view>
        
        <view class="days-grid"
          <view 
            v-for="(day, index) in calendarDays" 
            :key="index"
            class="day-cell"
            :class="{ 
              'other-month': !day.isCurrentMonth, 
              'today': day.isToday,
              'has-event': day.hasEvent 
            }"
            @click="selectDay(day)"
          >
            <text class="day-num">{{ day.date }}</text>
            
            <view v-if="day.hasEvent" class="event-dots"
              <view 
                v-for="(dot, i) in day.dots" 
                :key="i"
                class="dot"
                :style="{ background: dot.color }"
              ></view>
            </view>
          </view>
        </view>
      </view>
    </view>
    
    <!-- 今日倒计时 -->
    <view class="countdown-section"
      <view class="countdown-card" v-if="todayCountdown"
        <view class="countdown-icon">{{ todayCountdown.icon }}</view>
        
        <view class="countdown-info"
          <text class="countdown-title">{{ todayCountdown.title }}</text>
          
          <text class="countdown-date">{{ todayCountdown.date }}</text>
        </view>
        
        <view class="countdown-days"
          <text class="days-num">{{ todayCountdown.days }}</text>
          
          <text class="days-label">{{ todayCountdown.days > 0 ? '天后' : '就是今天' }}</text>
        </view>
      </view>
    </view>
    
    <!-- 纪念日列表 -->
    <view class="anniversary-section"
      <view class="section-header"
        <text class="section-title">📅 纪念日列表</text>
        
        <view class="filter-tabs"
          <text 
            class="tab" 
            :class="{ active: anniFilter === 'upcoming' }"
            @click="anniFilter = 'upcoming'"
          >即将到期</text>
          
          <text 
            class="tab" 
            :class="{ active: anniFilter === 'all' }"
            @click="anniFilter = 'all'"
          >全部</text>
          
          <text 
            class="tab" 
            :class="{ active: anniFilter === 'completed' }"
            @click="anniFilter = 'completed'"
          >已过去</text>
        </view>
      </view>
      
      <view class="anniversary-list"
        <view 
          v-for="item in filteredAnniversaries" 
          :key="item.id"
          class="anniversary-card"
          @click="goDetail(item)"
        >
          <view class="card-icon" :style="{ background: item.bgColor }"
            <text>{{ item.icon }}</text>
          </view>
          
          <view class="card-content"
            <text class="card-title">{{ item.title }}</text>
            
            <text class="card-date">{{ item.date }} · {{ item.dateType === 'lunar' ? '农历' : '阳历' }}</text>
            
            <view v-if="item.relatedPerson" class="related-person"
              <image :src="item.relatedPerson.avatar" />
              <text>{{ item.relatedPerson.name }}</text>
            </view>
          </view>
          
          <view class="card-days"
            <text class="days-num" :class="{ 'is-today': item.days === 0 }">
              {{ item.days === 0 ? '今天' : item.days }}
            </text>
            
            <text v-if="item.days !== 0" class="days-label">{{ item.days > 0 ? '天后' : '天前' }}</text>
          </view>
        </view>
      </view>
    </view>
    
    <!-- 添加按钮 -->
    <view class="fab-btn" @click="createAnniversary"
      <u-icon name="plus" size="48" color="#fff"></u-icon>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'

const currentYearMonth = ref('2025年2月')
const anniFilter = ref('upcoming')
const weekDays = ['日', '一', '二', '三', '四', '五', '六']

const calendarDays = ref([
  { date: 26, isCurrentMonth: false, isToday: false, hasEvent: false },
  { date: 27, isCurrentMonth: false, isToday: false, hasEvent: false },
  { date: 28, isCurrentMonth: false, isToday: false, hasEvent: false },
  { date: 29, isCurrentMonth: false, isToday: false, hasEvent: false },
  { date: 30, isCurrentMonth: false, isToday: false, hasEvent: false },
  { date: 31, isCurrentMonth: false, isToday: false, hasEvent: false },
  { date: 1, isCurrentMonth: true, isToday: false, hasEvent: true, dots: [{ color: '#FF6B6B' }] },
  { date: 2, isCurrentMonth: true, isToday: false, hasEvent: false },
  { date: 3, isCurrentMonth: true, isToday: false, hasEvent: false },
  { date: 4, isCurrentMonth: true, isToday: false, hasEvent: true, dots: [{ color: '#4ECDC4' }] },
  { date: 5, isCurrentMonth: true, isToday: false, hasEvent: false },
  { date: 6, isCurrentMonth: true, isToday: false, hasEvent: false },
  { date: 7, isCurrentMonth: true, isToday: false, hasEvent: false },
  { date: 8, isCurrentMonth: true, isToday: false, hasEvent: true, dots: [{ color: '#FFE66D' }] },
  { date: 9, isCurrentMonth: true, isToday: false, hasEvent: false },
  { date: 10, isCurrentMonth: true, isToday: false, hasEvent: false },
  { date: 11, isCurrentMonth: true, isToday: false, hasEvent: false },
  { date: 12, isCurrentMonth: true, isToday: false, hasEvent: false },
  { date: 13, isCurrentMonth: true, isToday: false, hasEvent: false },
  { date: 14, isCurrentMonth: true, isToday: false, hasEvent: false },
  { date: 15, isCurrentMonth: true, isToday: false, hasEvent: false },
  { date: 16, isCurrentMonth: true, isToday: false, hasEvent: false },
  { date: 17, isCurrentMonth: true, isToday: false, hasEvent: false },
  { date: 18, isCurrentMonth: true, isToday: false, hasEvent: false },
  { date: 19, isCurrentMonth: true, isToday: false, hasEvent: false },
  { date: 20, isCurrentMonth: true, isToday: false, hasEvent: false },
  { date: 21, isCurrentMonth: true, isToday: true, hasEvent: false },
  { date: 22, isCurrentMonth: true, isToday: false, hasEvent: false },
  { date: 23, isCurrentMonth: true, isToday: false, hasEvent: false },
  { date: 24, isCurrentMonth: true, isToday: false, hasEvent: false },
  { date: 25, isCurrentMonth: true, isToday: false, hasEvent: true, dots: [{ color: '#FF6B6B' }] },
  { date: 26, isCurrentMonth: true, isToday: false, hasEvent: false },
  { date: 27, isCurrentMonth: true, isToday: false, hasEvent: false },
  { date: 28, isCurrentMonth: true, isToday: false, hasEvent: false },
])

const todayCountdown = ref({
  icon: '💒',
  title: '结婚纪念日',
  date: '12月25日',
  days: 5
})

const anniversaries = ref([
  {
    id: 1,
    title: '结婚纪念日',
    date: '12月25日',
    dateType: 'solar',
    days: 5,
    icon: '💒',
    bgColor: '#FFE4E1',
    relatedPerson: { name: '爸爸妈妈', avatar: '/static/avatar/parents.jpg' }
  },
  {
    id: 2,
    title: '宝贝生日',
    date: '1月8日',
    dateType: 'solar',
    days: 19,
    icon: '🎂',
    bgColor: '#FFF8DC',
    relatedPerson: { name: '宝贝', avatar: '/static/avatar/baby.jpg' }
  },
  {
    id: 3,
    title: '在一起1000天',
    date: '2月14日',
    dateType: 'solar',
    days: -7,
    icon: '💕',
    bgColor: '#FFE4E1',
    relatedPerson: null
  },
  {
    id: 4,
    title: '春节',
    date: '正月初一',
    dateType: 'lunar',
    days: 25,
    icon: '🧧',
    bgColor: '#FFD700',
    relatedPerson: null
  }
])

const filteredAnniversaries = computed(() => {
  if (anniFilter.value === 'upcoming') {
    return anniversaries.value.filter(a => a.days >= 0).sort((a, b) => a.days - b.days)
  } else if (anniFilter.value === 'completed') {
    return anniversaries.value.filter(a => a.days < 0).sort((a, b) => b.days - a.days)
  }
  return anniversaries.value.sort((a, b) => Math.abs(a.days) - Math.abs(b.days))
})

const changeMonth = (delta) => {
  console.log('切换月份:', delta)
}

const selectDay = (day) => {
  console.log('选择日期:', day)
}

const goDetail = (item) => {
  uni.navigateTo({ url: `/pages/calendar/detail?id=${item.id}` })
}

const createAnniversary = () => {
  uni.navigateTo({ url: '/pages/calendar/create' })
}
</script>

<style lang="scss" scoped>
.calendar-page {
  min-height: 100vh;
  background: #f5f6fa;
  padding-bottom: 120rpx;
}

.calendar-header {
  position: relative;
  padding: 40rpx;
  padding-top: 60rpx;
  
  .header-bg {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 500rpx;
    background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
    border-radius: 0 0 40rpx 40rpx;
  }
  
  .month-selector {
    position: relative;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 30rpx;
    
    .month-text {
      font-size: 36rpx;
      color: #fff;
      font-weight: 600;
      margin: 0 40rpx;
    }
  }
  
  .calendar-grid {
    position: relative;
    background: #fff;
    border-radius: 20rpx;
    padding: 20rpx;
    box-shadow: 0 4rpx 20rpx rgba(0,0,0,0.08);
    
    .week-row {
      display: flex;
      margin-bottom: 20rpx;
      
      .week-day {
        flex: 1;
        text-align: center;
        font-size: 26rpx;
        color: #999;
      }
    }
    
    .days-grid {
      display: flex;
      flex-wrap: wrap;
      
      .day-cell {
        width: 14.28%;
        height: 90rpx;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        position: relative;
        
        &.other-month {
          .day-num {
            color: #ccc;
          }
        }
        
        &.today {
          .day-num {
            width: 50rpx;
            height: 50rpx;
            background: #fa709a;
            color: #fff;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
          }
        }
        
        .day-num {
          font-size: 28rpx;
          color: #333;
        }
        
        .event-dots {
          display: flex;
          gap: 6rpx;
          margin-top: 6rpx;
          
          .dot {
            width: 8rpx;
            height: 8rpx;
            border-radius: 50%;
          }
        }
      }
    }
  }
}

.countdown-section {
  margin: 30rpx;
  margin-top: -20rpx;
  
  .countdown-card {
    display: flex;
    align-items: center;
    background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
    border-radius: 20rpx;
    padding: 30rpx;
    
    .countdown-icon {
      width: 100rpx;
      height: 100rpx;
      background: rgba(255,255,255,0.3);
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 50rpx;
      margin-right: 24rpx;
    }
    
    .countdown-info {
      flex: 1;
      
      .countdown-title {
        font-size: 32rpx;
        color: #fff;
        font-weight: 600;
        display: block;
        margin-bottom: 8rpx;
      }
      
      .countdown-date {
        font-size: 26rpx;
        color: rgba(255,255,255,0.9);
      }
    }
    
    .countdown-days {
      text-align: center;
      
      .days-num {
        font-size: 52rpx;
        font-weight: bold;
        color: #fff;
        display: block;
      }
      
      .days-label {
        font-size: 24rpx;
        color: rgba(255,255,255,0.9);
      }
    }
  }
}

.anniversary-section {
  margin: 0 30rpx;
  padding: 30rpx;
  background: #fff;
  border-radius: 20rpx;
  
  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24rpx;
    
    .section-title {
      font-size: 32rpx;
      font-weight: 600;
      color: #333;
    }
    
    .filter-tabs {
      display: flex;
      background: #f5f6fa;
      border-radius: 30rpx;
      padding: 4rpx;
      
      .tab {
        padding: 12rpx 20rpx;
        font-size: 24rpx;
        color: #666;
        border-radius: 26rpx;
        
        &.active {
          background: #fa709a;
          color: #fff;
        }
      }
    }
  }
  
  .anniversary-list {
    .anniversary-card {
      display: flex;
      align-items: center;
      padding: 24rpx;
      background: #f9f9f9;
      border-radius: 16rpx;
      margin-bottom: 20rpx;
      
      &:last-child {
        margin-bottom: 0;
      }
      
      .card-icon {
        width: 90rpx;
        height: 90rpx;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 44rpx;
        margin-right: 24rpx;
      }
      
      .card-content {
        flex: 1;
        
        .card-title {
          font-size: 30rpx;
          color: #333;
          font-weight: 500;
          display: block;
          margin-bottom: 8rpx;
        }
        
        .card-date {
          font-size: 24rpx;
          color: #999;
          display: block;
          margin-bottom: 8rpx;
        }
        
        .related-person {
          display: flex;
          align-items: center;
          
          image {
            width: 32rpx;
            height: 32rpx;
            border-radius: 50%;
            margin-right: 8rpx;
          }
          
          text {
            font-size: 22rpx;
            color: #666;
          }
        }
      }
      
      .card-days {
        text-align: right;
        
        .days-num {
          font-size: 44rpx;
          font-weight: bold;
          color: #fa709a;
          display: block;
          
          &.is-today {
            color: #5AD8A6;
          }
        }
        
        .days-label {
          font-size: 22rpx;
          color: #999;
        }
      }
    }
  }
}

.fab-btn {
  position: fixed;
  right: 40rpx;
  bottom: 160rpx;
  width: 100rpx;
  height: 100rpx;
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8rpx 24rpx rgba(250, 112, 154, 0.4);
  z-index: 100;
}
</style>
