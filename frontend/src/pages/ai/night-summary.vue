<template>
  <view class="night-container">
    <!-- 自定义导航栏 -->
    <view class="custom-nav">
      <view class="nav-back" @click="goBack">
        <text class="icon">←</text>
      </view>
      <text class="nav-title">晚安总结</text>
      <view class="nav-right" @click="shareSummary">
        <text class="icon">📤</text>
      </view>
    </view>

    <!-- 晚安问候 -->
    <view class="greeting-section">
      <text class="greeting-emoji">🌙</text>
      <text class="greeting-text">{{ greeting }}</text>
      <text class="greeting-sub">今天是 {{ fullDate }}</text>
    </view>

    <!-- 今日数据概览 -->
    <view class="stats-card">
      <view class="stats-header">
        <text class="stats-title">📊 今日回顾</text>
        <view class="mood-selector">
          <text class="mood-label">今日心情</text>
          <view class="mood-options">
            <text 
              v-for="(mood, index) in moods" :key="mood.id || index" 
              
              class="mood-item"
              :class="{ active: selectedMood === mood.value }"
              @click="selectMood(mood.value)"
            >
              {{ mood.icon }}
            </text>
          </view>
        </view>
      </view>
      
      <view class="stats-grid">
        <view class="stat-item">
          <view class="stat-icon">✅</view>
          <view class="stat-info">
            <text class="stat-num">{{ completedTasks }}</text>
            <text class="stat-label">完成任务</text>
          </view>
        </view>
        <view class="stat-item">
          <view class="stat-icon">📋</view>
          <view class="stat-info">
            <text class="stat-num">{{ totalTasks }}</text>
            <text class="stat-label">待办总数</text>
          </view>
        </view>
        <view class="stat-item">
          <view class="stat-icon">🍽️</view>
          <view class="stat-info">
            <text class="stat-num">{{ mealsRecorded }}</text>
            <text class="stat-label">记录饮食</text>
          </view>
        </view>
        <view class="stat-item">
          <view class="stat-icon">💧</view>
          <view class="stat-info">
            <text class="stat-num">{{ waterCount }}</text>
            <text class="stat-label">喝水(杯)</text>
          </view>
        </view>
      </view>

      <!-- 完成度进度条 -->
      <view class="completion-section">
        <view class="completion-header">
          <text class="completion-title">今日完成度</text>
          <text class="completion-percent">{{ completionRate }}%</text>
        </view>
        <view class="completion-bar">
          <view class="completion-fill" :style="{ width: completionRate + '%' }"></view>
        </view>
        <text class="completion-desc">{{ completionText }}</text>
      </view>
    </view>

    <!-- 饮食记录 -->
    <view class="diet-card">
      <view class="card-header">
        <view class="header-left">
          <text class="header-icon">🥗</text>
          <text class="header-title">今日饮食</text>
        </view>
        <text class="total-calories">{{ totalCalories }} 千卡</text>
      </view>
      
      <view class="meal-list">
        <view v-for="(meal, index) in meals" :key="index" class="meal-item">
          <view class="meal-icon">{{ meal.icon }}</view>
          <view class="meal-info">
            <text class="meal-name">{{ meal.name }}</text>
            <view class="meal-foods">
              <text v-for="(food, fIndex) in meal.foods" :key="fIndex" class="food-tag">{{ food }}</text>
            </view>
          </view>
          <text class="meal-calories">{{ meal.calories }}千卡</text>
        </view>
      </view>
    </view>

    <!-- 明日预览 -->
    <view class="tomorrow-card">
      <view class="card-header">
        <view class="header-left">
          <text class="header-icon">🔮</text>
          <text class="header-title">明日预览</text>
        </view>
        <text class="tomorrow-date">{{ tomorrowDate }}</text>
      </view>
      
      <!-- 明日天气 -->
      <view class="tomorrow-weather">
        <view class="weather-info">
          <text class="weather-icon">{{ tomorrowWeather.icon }}</text>
          <view class="weather-detail">
            <text class="weather-temp">{{ tomorrowWeather.temperature }}</text>
            <text class="weather-desc">{{ tomorrowWeather.description }}</text>
          </view>
        </view>
        <view class="weather-tips">
          <text>{{ tomorrowWeather.tip }}</text>
        </view>
      </view>

      <!-- 明日日程 -->
      <view class="tomorrow-schedule">
        <view class="schedule-title">明日重要日程</view>
        <view class="schedule-items">
          <view v-for="(event, index) in tomorrowEvents" :key="index" class="schedule-item">
            <view class="item-time">{{ event.time }}</view>
            <view class="item-content">
              <view class="item-dot" :style="{ background: event.color }"></view>
              <text class="item-title">{{ event.title }}</text>
            </view>
          </view>
        </view>
        <view v-if="tomorrowEvents.length === 0" class="no-events">
          <text>🎉 明日暂无重要日程，可以好好休息</text>
        </view>
      </view>
    </view>

    <!-- 打卡功能 -->
    <view class="checkin-card">
      <view class="card-header">
        <view class="header-left">
          <text class="header-icon">📍</text>
          <text class="header-title">今日打卡</text>
        </view>
        <text class="checkin-streak">🔥 连续 {{ streakDays }} 天</text>
      </view>
      
      <view class="checkin-grid">
        <view 
          v-for="(item, index) in checkinItems" 
          :key="index"
          class="checkin-item"
          :class="{ checked: item.checked }"
          @click="toggleCheckin(item)"
        >
          <view class="checkin-icon">{{ item.checked ? item.iconActive : item.icon }}</view>
          <text class="checkin-name">{{ item.name }}</text>
          <view v-if="item.checked" class="check-badge">✓</view>
        </view>
      </view>
    </view>

    <!-- 今日感悟 -->
    <view class="reflection-card">
      <view class="card-header">
        <text class="header-title">📝 今日感悟</text>
      </view>
      <textarea
        class="reflection-input"
        v-model="reflection"
        placeholder="记录下今天的收获、感悟或感恩..."
        :maxlength="200"
      />
      <text class="input-count">{{ reflection.length }}/200</text>
    </view>

    <!-- 睡眠提醒 -->
    <view class="sleep-tips">
      <view class="tips-icon">😴</view>
      <view class="tips-content">
        <text class="tips-title">睡眠小贴士</text>
        <text class="tips-desc">{{ sleepTip }}</text>
      </view>
    </view>

    <!-- 底部按钮 -->
    <view class="bottom-actions">
      <view class="action-btn secondary" @click="viewStats">
        <text class="btn-icon">📊</text>
        <text>查看统计</text>
      </view>
      <view class="action-btn primary" @click="completeDay">
        <text class="btn-icon">🌙</text>
        <text>完成今日</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'

// 日期相关
const now = new Date()
const hours = now.getHours()

const greeting = computed(() => {
  if (hours < 18) return '下午好'
  if (hours < 22) return '晚上好'
  return '夜深了，早点休息'
})

const fullDate = computed(() => {
  const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  const month = now.getMonth() + 1
  const date = now.getDate()
  const weekDay = weekDays[now.getDay()]
  return `${month}月${date}日 ${weekDay}`
})

const tomorrowDate = computed(() => {
  const tomorrow = new Date(now)
  tomorrow.setDate(tomorrow.getDate() + 1)
  const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  return `${tomorrow.getMonth() + 1}月${tomorrow.getDate()}日 ${weekDays[tomorrow.getDay()]}`
})

// 心情选择
const moods = [
  { icon: '😢', value: 'sad' },
  { icon: '😔', value: 'bad' },
  { icon: '😐', value: 'normal' },
  { icon: '😊', value: 'good' },
  { icon: '😄', value: 'great' }
]
const selectedMood = ref('good')
const selectMood = (value) => {
  selectedMood.value = value
  uni.vibrateShort()
}

// 统计数据
const completedTasks = ref(8)
const totalTasks = ref(10)
const mealsRecorded = ref(3)
const waterCount = ref(6)

const completionRate = computed(() => {
  return Math.round((completedTasks.value / totalTasks.value) * 100)
})

const completionText = computed(() => {
  const rate = completionRate.value
  if (rate >= 100) return '太棒了！今日任务全部完成 🎉'
  if (rate >= 80) return '表现不错，继续保持 💪'
  if (rate >= 60) return '还可以，明天再接再厉 👍'
  return '今天有点忙，明天加油 💪'
})

// 饮食记录
const meals = ref([
  {
    name: '早餐',
    icon: '🌅',
    foods: ['燕麦粥', '水煮蛋', '牛奶'],
    calories: 380
  },
  {
    name: '午餐',
    icon: '☀️',
    foods: ['糙米饭', '清蒸鱼', '炒青菜'],
    calories: 620
  },
  {
    name: '晚餐',
    icon: '🌙',
    foods: ['小米粥', '鸡胸肉', '凉拌黄瓜'],
    calories: 450
  }
])

const totalCalories = computed(() => {
  return meals.value.reduce((sum, meal) => sum + meal.calories, 0)
})

// 明日天气
const tomorrowWeather = ref({
  icon: '⛅',
  temperature: '20° ~ 28°',
  description: '多云转晴',
  tip: '明日温差较大，记得带件外套，适合户外活动'
})

// 明日日程
const tomorrowEvents = ref([
  { time: '09:00', title: '晨会', color: '#5B8FF9' },
  { time: '14:00', title: '项目评审', color: '#5AD8A6' },
  { time: '19:00', title: '瑜伽课', color: '#F6BD16' }
])

// 打卡数据
const streakDays = ref(12)
const checkinItems = ref([
  { name: '早起', icon: '🛏️', iconActive: '🌅', checked: true },
  { name: '早餐', icon: '🍳', iconActive: '🥐', checked: true },
  { name: '运动', icon: '🏃', iconActive: '💪', checked: true },
  { name: '喝水', icon: '💧', iconActive: '🥤', checked: true },
  { name: '阅读', icon: '📚', iconActive: '📖', checked: false },
  { name: '早睡', icon: '😴', iconActive: '🌙', checked: false }
])

const toggleCheckin = (item) => {
  item.checked = !item.checked
  if (item.checked) {
    uni.vibrateShort()
  }
}

// 今日感悟
const reflection = ref('')

// 睡眠小贴士
const sleepTips = [
  '睡前1小时避免使用电子设备，有助于提高睡眠质量。',
  '保持卧室温度在18-22°C之间，有助于更快入睡。',
  '睡前喝一杯温牛奶或温水，有助于放松身心。',
  '尝试4-7-8呼吸法：吸气4秒，屏息7秒，呼气8秒。',
  '睡前进行轻度拉伸，可以缓解一天的疲劳。'
]
const sleepTip = ref(sleepTips[Math.floor(Math.random() * sleepTips.length)])

// 分享总结
const shareSummary = () => {
  uni.showActionSheet({
    itemList: ['生成图片分享', '发送给好友', '分享到朋友圈'],
    success: () => {
      uni.showToast({ title: '分享成功', icon: 'success' })
    }
  })
}

// 查看统计
const viewStats = () => {
  uni.navigateTo({ url: '/pages/task/statistics' })
}

// 完成今日
const completeDay = () => {
  uni.showModal({
    title: '确认完成',
    content: '确定要完成今日总结并准备休息了吗？',
    confirmText: '确认',
    cancelText: '再等等',
    success: (res) => {
      if (res.confirm) {
        uni.showToast({
          title: '晚安，好梦 🌙',
          icon: 'none',
          duration: 2000
        })
        setTimeout(() => {
          uni.navigateBack()
        }, 1500)
      }
    }
  })
}

// 返回
const goBack = () => {
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
.night-container {
  min-height: 100vh;
  background: linear-gradient(180deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
  padding-bottom: 160rpx;
}

// 自定义导航栏
.custom-nav {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 80rpx 30rpx 20rpx;
  
  .nav-back, .nav-right {
    width: 60rpx;
    height: 60rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    
    .icon {
      font-size: 40rpx;
      color: #fff;
    }
  }
  
  .nav-title {
    font-size: 36rpx;
    font-weight: 600;
    color: #fff;
  }
}

// 问候区域
.greeting-section {
  text-align: center;
  padding: 20rpx 40rpx 40rpx;
  
  .greeting-emoji {
    font-size: 80rpx;
    display: block;
    margin-bottom: 20rpx;
    animation: moon-float 3s ease-in-out infinite;
  }
  
  .greeting-text {
    font-size: 48rpx;
    font-weight: 700;
    color: #fff;
    margin-right: 16rpx;
  }
  
  .greeting-sub {
    display: block;
    font-size: 28rpx;
    color: rgba(255,255,255,0.6);
    margin-top: 16rpx;
  }
}

@keyframes moon-float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10rpx); }
}

// 统计卡片
.stats-card {
  margin: 20rpx;
  padding: 30rpx;
  background: rgba(255,255,255,0.1);
  backdrop-filter: blur(10px);
  border-radius: 32rpx;
  border: 1rpx solid rgba(255,255,255,0.1);
}

.stats-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30rpx;
  
  .stats-title {
    font-size: 32rpx;
    font-weight: 600;
    color: #fff;
  }
  
  .mood-selector {
    display: flex;
    align-items: center;
    
    .mood-label {
      font-size: 24rpx;
      color: rgba(255,255,255,0.6);
      margin-right: 16rpx;
    }
    
    .mood-options {
      display: flex;
      gap: 12rpx;
      
      .mood-item {
        font-size: 40rpx;
        opacity: 0.4;
        transition: all 0.3s;
        
        &.active {
          opacity: 1;
          transform: scale(1.2);
        }
      }
    }
  }
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20rpx;
  margin-bottom: 30rpx;
  
  .stat-item {
    text-align: center;
    padding: 20rpx 10rpx;
    background: rgba(255,255,255,0.05);
    border-radius: 20rpx;
    
    .stat-icon {
      font-size: 44rpx;
      margin-bottom: 12rpx;
    }
    
    .stat-info {
      .stat-num {
        display: block;
        font-size: 36rpx;
        font-weight: 700;
        color: #fff;
        margin-bottom: 4rpx;
      }
      
      .stat-label {
        font-size: 22rpx;
        color: rgba(255,255,255,0.6);
      }
    }
  }
}

.completion-section {
  padding: 24rpx;
  background: rgba(102, 126, 234, 0.2);
  border-radius: 20rpx;
  
  .completion-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16rpx;
    
    .completion-title {
      font-size: 28rpx;
      color: rgba(255,255,255,0.8);
    }
    
    .completion-percent {
      font-size: 36rpx;
      font-weight: 700;
      color: #667eea;
    }
  }
  
  .completion-bar {
    height: 16rpx;
    background: rgba(255,255,255,0.1);
    border-radius: 8rpx;
    overflow: hidden;
    margin-bottom: 16rpx;
    
    .completion-fill {
      height: 100%;
      background: linear-gradient(90deg, #667eea, #764ba2);
      border-radius: 8rpx;
      transition: width 0.5s ease;
    }
  }
  
  .completion-desc {
    font-size: 26rpx;
    color: rgba(255,255,255,0.7);
  }
}

// 饮食卡片
.diet-card {
  margin: 20rpx;
  padding: 30rpx;
  background: rgba(255,255,255,0.1);
  backdrop-filter: blur(10px);
  border-radius: 32rpx;
  border: 1rpx solid rgba(255,255,255,0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;
  
  .header-left {
    display: flex;
    align-items: center;
    
    .header-icon {
      font-size: 40rpx;
      margin-right: 16rpx;
    }
    
    .header-title {
      font-size: 32rpx;
      font-weight: 600;
      color: #fff;
    }
  }
  
  .total-calories {
    font-size: 28rpx;
    color: #f6bd16;
    font-weight: 600;
  }
}

.meal-list {
  .meal-item {
    display: flex;
    align-items: center;
    padding: 20rpx 0;
    border-bottom: 1rpx solid rgba(255,255,255,0.1);
    
    &:last-child {
      border-bottom: none;
    }
    
    .meal-icon {
      font-size: 48rpx;
      margin-right: 20rpx;
    }
    
    .meal-info {
      flex: 1;
      
      .meal-name {
        font-size: 30rpx;
        font-weight: 500;
        color: #fff;
        margin-bottom: 12rpx;
        display: block;
      }
      
      .meal-foods {
        display: flex;
        flex-wrap: wrap;
        gap: 12rpx;
        
        .food-tag {
          padding: 6rpx 16rpx;
          background: rgba(255,255,255,0.1);
          border-radius: 16rpx;
          font-size: 24rpx;
          color: rgba(255,255,255,0.8);
        }
      }
    }
    
    .meal-calories {
      font-size: 28rpx;
      color: #f6bd16;
      font-weight: 500;
    }
  }
}

// 明日卡片
.tomorrow-card {
  margin: 20rpx;
  padding: 30rpx;
  background: rgba(255,255,255,0.1);
  backdrop-filter: blur(10px);
  border-radius: 32rpx;
  border: 1rpx solid rgba(255,255,255,0.1);
  
  .tomorrow-date {
    font-size: 26rpx;
    color: rgba(255,255,255,0.6);
  }
}

.tomorrow-weather {
  padding: 24rpx;
  background: rgba(102, 126, 234, 0.2);
  border-radius: 20rpx;
  margin-bottom: 24rpx;
  
  .weather-info {
    display: flex;
    align-items: center;
    margin-bottom: 20rpx;
    
    .weather-icon {
      font-size: 72rpx;
      margin-right: 24rpx;
    }
    
    .weather-detail {
      .weather-temp {
        display: block;
        font-size: 40rpx;
        font-weight: 700;
        color: #fff;
        margin-bottom: 8rpx;
      }
      
      .weather-desc {
        font-size: 28rpx;
        color: rgba(255,255,255,0.7);
      }
    }
  }
  
  .weather-tips {
    padding: 16rpx 20rpx;
    background: rgba(255,255,255,0.1);
    border-radius: 12rpx;
    
    text {
      font-size: 26rpx;
      color: rgba(255,255,255,0.8);
    }
  }
}

.tomorrow-schedule {
  .schedule-title {
    font-size: 28rpx;
    font-weight: 600;
    color: #fff;
    margin-bottom: 20rpx;
  }
  
  .schedule-items {
    .schedule-item {
      display: flex;
      align-items: center;
      margin-bottom: 16rpx;
      
      &:last-child {
        margin-bottom: 0;
      }
      
      .item-time {
        width: 100rpx;
        font-size: 26rpx;
        color: #667eea;
        font-weight: 500;
      }
      
      .item-content {
        display: flex;
        align-items: center;
        flex: 1;
        padding: 16rpx 20rpx;
        background: rgba(255,255,255,0.05);
        border-radius: 12rpx;
        
        .item-dot {
          width: 16rpx;
          height: 16rpx;
          border-radius: 50%;
          margin-right: 16rpx;
        }
        
        .item-title {
          font-size: 28rpx;
          color: #fff;
        }
      }
    }
  }
  
  .no-events {
    text-align: center;
    padding: 30rpx;
    background: rgba(255,255,255,0.05);
    border-radius: 16rpx;
    
    text {
      font-size: 28rpx;
      color: rgba(255,255,255,0.6);
    }
  }
}

// 打卡卡片
.checkin-card {
  margin: 20rpx;
  padding: 30rpx;
  background: rgba(255,255,255,0.1);
  backdrop-filter: blur(10px);
  border-radius: 32rpx;
  border: 1rpx solid rgba(255,255,255,0.1);
  
  .checkin-streak {
    font-size: 26rpx;
    color: #ff9800;
    font-weight: 500;
  }
}

.checkin-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20rpx;
  
  .checkin-item {
    position: relative;
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 30rpx 20rpx;
    background: rgba(255,255,255,0.05);
    border-radius: 20rpx;
    transition: all 0.3s;
    
    &.checked {
      background: linear-gradient(135deg, rgba(102, 126, 234, 0.3), rgba(118, 75, 162, 0.3));
      
      .checkin-icon {
        transform: scale(1.1);
      }
    }
    
    .checkin-icon {
      font-size: 52rpx;
      margin-bottom: 16rpx;
      transition: transform 0.3s;
    }
    
    .checkin-name {
      font-size: 26rpx;
      color: rgba(255,255,255,0.8);
    }
    
    .check-badge {
      position: absolute;
      top: 16rpx;
      right: 16rpx;
      width: 32rpx;
      height: 32rpx;
      border-radius: 50%;
      background: #4caf50;
      color: #fff;
      font-size: 20rpx;
      display: flex;
      align-items: center;
      justify-content: center;
    }
  }
}

// 感悟卡片
.reflection-card {
  margin: 20rpx;
  padding: 30rpx;
  background: rgba(255,255,255,0.1);
  backdrop-filter: blur(10px);
  border-radius: 32rpx;
  border: 1rpx solid rgba(255,255,255,0.1);
  position: relative;
  
  .header-title {
    font-size: 32rpx;
    font-weight: 600;
    color: #fff;
  }
}

.reflection-input {
  width: 100%;
  min-height: 160rpx;
  padding: 20rpx;
  margin-top: 20rpx;
  background: rgba(255,255,255,0.05);
  border-radius: 16rpx;
  font-size: 28rpx;
  color: #fff;
  line-height: 1.6;
  box-sizing: border-box;
}

.input-count {
  position: absolute;
  bottom: 50rpx;
  right: 50rpx;
  font-size: 24rpx;
  color: rgba(255,255,255,0.4);
}

// 睡眠提示
.sleep-tips {
  display: flex;
  align-items: center;
  margin: 20rpx;
  padding: 24rpx 30rpx;
  background: rgba(102, 126, 234, 0.2);
  border-radius: 24rpx;
  
  .tips-icon {
    font-size: 56rpx;
    margin-right: 24rpx;
  }
  
  .tips-content {
    flex: 1;
    
    .tips-title {
      display: block;
      font-size: 30rpx;
      font-weight: 600;
      color: #fff;
      margin-bottom: 8rpx;
    }
    
    .tips-desc {
      font-size: 26rpx;
      color: rgba(255,255,255,0.7);
      line-height: 1.5;
    }
  }
}

// 底部按钮
.bottom-actions {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  gap: 20rpx;
  padding: 20rpx 30rpx 40rpx;
  background: rgba(0,0,0,0.3);
  backdrop-filter: blur(10px);
  
  .action-btn {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 28rpx 0;
    border-radius: 16rpx;
    
    &.primary {
      flex: 2;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      
      .btn-icon {
        font-size: 36rpx;
        margin-right: 12rpx;
      }
      
      text {
        font-size: 30rpx;
        font-weight: 600;
        color: #fff;
      }
    }
    
    &.secondary {
      background: rgba(255,255,255,0.1);
      border: 2rpx solid rgba(255,255,255,0.2);
      
      .btn-icon {
        font-size: 32rpx;
        margin-right: 12rpx;
      }
      
      text {
        font-size: 28rpx;
        color: rgba(255,255,255,0.9);
      }
    }
  }
}
</style>