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
import { ref, onMounted, computed } from 'vue'
import { anniversaryApi } from '../../api/index.js'

const currentYear = ref(new Date().getFullYear())
const currentMonth = ref(new Date().getMonth() + 1)
const weekdays = ['日', '一', '二', '三', '四', '五', '六']
const events = ref([])
const loading = ref(false)

// 加载纪念日列表
const loadAnniversaries = async () => {
  loading.value = true
  try {
    const familyId = uni.getStorageSync('currentFamilyId') || 1
    const res = await anniversaryApi.getList(familyId)
    events.value = res || []
  } catch (e) {
    console.error('加载纪念日失败', e)
  } finally {
    loading.value = false
  }
}

// 页面加载时获取数据
onMounted(() => {
  loadAnniversaries()
})

// 计算属性：即将到来的纪念日
const upcomingEvents = computed(() => {
  return events.value.map(event => {
    const eventDate = new Date(event.date)
    const today = new Date()
    const diffTime = eventDate - today
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))
    
    return {
      ...event,
      icon: event.icon || '📅',
      days: diffDays > 0 ? diffDays : 0
    }
  }).filter(e => e.days >= 0).sort((a, b) => a.days - b.days)
})

// 计算当月天数
const daysInMonth = computed(() => {
  const days = new Date(currentYear.value, currentMonth.value, 0).getDate()
  return Array.from({ length: days }, (_, i) => i + 1)
})

const isToday = (day) => {
  const today = new Date()
  return day === today.getDate() && 
         currentMonth.value === today.getMonth() + 1 &&
         currentYear.value === today.getFullYear()
}

const hasEvent = (day) => {
  return events.value.some(e => {
    const date = new Date(e.date)
    return date.getDate() === day &&
           date.getMonth() + 1 === currentMonth.value &&
           date.getFullYear() === currentYear.value
  })
}

const selectDay = (day) => {
  const dateStr = `${currentYear.value}-${String(currentMonth.value).padStart(2, '0')}-${String(day).padStart(2, '0')}`
  const event = events.value.find(e => e.date === dateStr)
  if (event) {
    uni.showModal({
      title: event.title,
      content: `${event.date}\n${event.description || ''}`,
      showCancel: true,
      confirmText: '删除',
      cancelText: '关闭',
      success: async (res) => {
        if (res.confirm) {
          try {
            await anniversaryApi.delete(event.id)
            uni.showToast({ title: '已删除', icon: 'success' })
            loadAnniversaries()
          } catch (e) {
            uni.showToast({ title: '删除失败', icon: 'none' })
          }
        }
      }
    })
  } else {
    uni.showToast({ title: `${day}日 无纪念日`, icon: 'none' })
  }
}

const showAddModal = () => {
  uni.showModal({
    title: '添加纪念日',
    editable: true,
    placeholderText: '输入纪念日名称...',
    success: async (res) => {
      if (res.confirm && res.content) {
        try {
          const familyId = uni.getStorageSync('currentFamilyId') || 1
          const today = new Date()
          const dateStr = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, '0')}-${String(today.getDate()).padStart(2, '0')}`
          
          await anniversaryApi.create({
            title: res.content,
            familyId: familyId,
            date: dateStr
          })
          uni.showToast({ title: '添加成功', icon: 'success' })
          loadAnniversaries()
        } catch (e) {
          console.error('添加失败', e)
          uni.showToast({ title: '添加失败', icon: 'none' })
        }
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
