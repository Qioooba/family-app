<template>
  <view class="morning-container">
    <!-- 自定义导航栏 -->
    <view class="custom-nav">
      <view class="nav-back" @click="goBack">
        <text class="icon">←</text>
      </view>
      <text class="nav-title">早安日报</text>
      <view class="nav-right" @click="shareReport">
        <text class="icon">📤</text>
      </view>
    </view>

    <!-- 日期问候 -->
    <view class="greeting-section">
      <text class="greeting-emoji">☀️</text>
      <text class="greeting-text">{{ greeting }}</text>
      <text class="greeting-name">{{ userName }}</text>
      <text class="greeting-date">{{ fullDate }}</text>
    </view>

    <!-- 今日天气 -->
    <view class="weather-card">
      <view class="weather-header">
        <view class="location">
          <text class="loc-icon">📍</text>
          <text class="loc-text">{{ weather.city }}</text>
        </view>
        <text class="update-time">更新于 {{ weather.updateTime }}</text>
      </view>
      
      <view class="weather-main">
        <view class="weather-icon">{{ weather.icon }}</view>
        <view class="weather-temp">
          <text class="temp-num">{{ weather.temperature }}</text>
          <text class="temp-unit">°C</text>
        </view>
        <view class="weather-desc">
          <text class="weather-text">{{ weather.description }}</text>
          <text class="weather-feel">体感 {{ weather.feelsLike }}°C</text>
        </view>
      </view>
      
      <view class="weather-details">
        <view class="detail-item">
          <text class="detail-icon">💧</text>
          <text class="detail-label">湿度</text>
          <text class="detail-value">{{ weather.humidity }}%</text>
        </view>
        <view class="detail-item">
          <text class="detail-icon">💨</text>
          <text class="detail-label">风速</text>
          <text class="detail-value">{{ weather.wind }}级</text>
        </view>
        <view class="detail-item">
          <text class="detail-icon">👁️</text>
          <text class="detail-label">能见度</text>
          <text class="detail-value">{{ weather.visibility }}km</text>
        </view>
        <view class="detail-item">
          <text class="detail-icon">🔆</text>
          <text class="detail-label">紫外线</text>
          <text class="detail-value">{{ weather.uv }}</text>
        </view>
      </view>

      <!-- 穿衣建议 -->
      <view class="clothing-tips">
        <text class="tips-title">👔 穿衣建议</text>
        <text class="tips-content">{{ weather.clothingTip }}</text>
      </view>
    </view>

    <!-- 今日待办 -->
    <view class="todo-card">
      <view class="card-header">
        <view class="header-left">
          <text class="header-icon">📋</text>
          <text class="header-title">今日待办</text>
        </view>
        <view class="header-right">
          <text class="progress-text">{{ completedCount }}/{{ todos.length }}</text>
          <view class="progress-ring">
            <view class="progress-fill" :style="{ width: todoProgress + '%' }"></text>
          </view>
        </view>
      </view>
      
      <view class="todo-list">
        <view 
          v-for="(todo, index) in todos" 
          :key="index"
          class="todo-item"
          :class="{ completed: todo.completed, urgent: todo.urgent }"
          @click="toggleTodo(todo)"
        >
          <view class="todo-check">
            <text class="check-icon">{{ todo.completed ? '✅' : '⭕' }}</text>
          </view>
          <view class="todo-content">
            <text class="todo-title">{{ todo.title }}</text>
            <view class="todo-meta">
              <text class="todo-time">⏰ {{ todo.time }}</text>
              <text v-if="todo.category" class="todo-category">{{ todo.category }}</text>
            </view>
          </view>
          <view v-if="todo.urgent" class="urgent-badge">紧急</view>
        </view>
      </view>
      
      <view v-if="todos.length === 0" class="empty-todo">
        <text class="empty-icon">🎉</text>
        <text class="empty-text">今日暂无待办事项</text>
        <text class="empty-sub">享受美好的一天吧！</text>
      </view>

      <view class="add-todo-btn" @click="addTodo">
        <text class="add-icon">➕</text>
        <text>添加待办</text>
      </view>
    </view>

    <!-- 日程提醒 -->
    <view class="schedule-card">
      <view class="card-header">
        <view class="header-left">
          <text class="header-icon">📅</text>
          <text class="header-title">今日日程</text>
        </view>
        <text class="view-all" @click="viewCalendar">查看全部 →</text>
      </view>
      
      <view class="schedule-list">
        <view 
          v-for="(event, index) in schedules" 
          :key="index"
          class="schedule-item"
        >
          <view class="schedule-time">
            <text class="time-start">{{ event.startTime }}</text>
            <view class="time-line"></text>
            <text class="time-end">{{ event.endTime }}</text>
          </view>
          <view class="schedule-content">
            <view class="content-main">
              <text class="event-title">{{ event.title }}</text>
              <text v-if="event.isImportant" class="important-badge">重要</text>
            </view>
            <view class="event-location" v-if="event.location">
              <text class="loc-icon">📍</text>
              <text>{{ event.location }}</text>
            </view>
            <view class="event-members" v-if="event.members">
              <text class="member-avatars">{{ event.members }}</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 励志语录 -->
    <view class="quote-card">
      <view class="quote-decoration">"</view>
      <view class="quote-content">
        <text class="quote-text">{{ quote.text }}</text>
        <text class="quote-author">—— {{ quote.author }}</text>
      </view>
      <view class="quote-actions">
        <view class="quote-btn" @click="refreshQuote">
          <text class="btn-icon">🔄</text>
          <text>换一句</text>
        </view>
        <view class="quote-btn" @click="copyQuote">
          <text class="btn-icon">📋</text>
          <text>复制</text>
        </view>
        <view class="quote-btn" @click="shareQuote">
          <text class="btn-icon">📤</text>
          <text>分享</text>
        </view>
      </view>
    </view>

    <!-- 健康小贴士 -->
    <view class="health-tips">
      <view class="tips-header">
        <text class="tips-icon">💡</text>
        <text class="tips-title">今日健康小贴士</text>
      </view>
      <text class="tips-desc">{{ healthTip }}</text>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'

// 用户信息
const userName = ref('亲爱的')

// 日期相关
const now = new Date()
const hours = now.getHours()

const greeting = computed(() => {
  if (hours < 6) return '夜深了'
  if (hours < 9) return '早上好'
  if (hours < 12) return '上午好'
  if (hours < 14) return '中午好'
  if (hours < 18) return '下午好'
  return '晚上好'
})

const fullDate = computed(() => {
  const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  const month = now.getMonth() + 1
  const date = now.getDate()
  const weekDay = weekDays[now.getDay()]
  return `${month}月${date}日 ${weekDay}`
})

// 天气数据
const weather = ref({
  city: '北京市',
  updateTime: '07:00',
  temperature: 22,
  description: '多云转晴',
  feelsLike: 24,
  icon: '⛅',
  humidity: 65,
  wind: 2,
  visibility: 10,
  uv: '中等',
  clothingTip: '今日温度适宜，建议穿着轻便外套。早晚温差较大，请注意添衣保暖。'
})

// 待办事项
const todos = ref([
  { title: '晨跑30分钟', time: '07:00', category: '运动', completed: true, urgent: false },
  { title: '准备早餐', time: '08:00', category: '生活', completed: true, urgent: false },
  { title: '项目会议', time: '10:00', category: '工作', completed: false, urgent: true },
  { title: '提交周报', time: '17:00', category: '工作', completed: false, urgent: true },
  { title: '超市采购', time: '18:30', category: '生活', completed: false, urgent: false }
])

const completedCount = computed(() => todos.value.filter(t => t.completed).length)
const todoProgress = computed(() => (completedCount.value / todos.value.length) * 100)

// 日程
const schedules = ref([
  {
    title: '团队周会',
    startTime: '09:30',
    endTime: '10:30',
    location: '会议室A',
    members: '👨‍💼👩‍💼👨‍💻👩‍💻',
    isImportant: true
  },
  {
    title: '客户演示',
    startTime: '14:00',
    endTime: '15:30',
    location: '线上会议',
    members: '👨‍💼👩‍💼',
    isImportant: true
  },
  {
    title: '健身时间',
    startTime: '19:00',
    endTime: '20:30',
    location: '社区健身房',
    members: null,
    isImportant: false
  }
])

// 励志语录
const quotes = [
  { text: '每一个不曾起舞的日子，都是对生命的辜负。', author: '尼采' },
  { text: '成功的路上并不拥挤，因为坚持的人不多。', author: '佚名' },
  { text: '生活不是等待暴风雨过去，而是学会在雨中跳舞。', author: '维维安·格林' },
  { text: '你的时间有限，不要为别人而活。', author: '乔布斯' },
  { text: '种一棵树最好的时间是十年前，其次是现在。', author: '丹比萨·莫约' },
  { text: '相信自己，你比你想象的更强大。', author: '佚名' }
]

const quote = ref(quotes[Math.floor(Math.random() * quotes.length)])

// 健康小贴士
const healthTips = [
  '早起一杯温水，有助于唤醒身体机能，促进新陈代谢。',
  '早餐要吃好，建议摄入优质蛋白质和碳水化合物。',
  '久坐办公记得每小时起身活动5分钟，保护颈椎和腰椎。',
  '下午3-4点是人体疲劳期，适当补充坚果或水果。',
  '晚餐宜清淡，七分饱即可，有助于睡眠质量。'
]
const healthTip = ref(healthTips[Math.floor(Math.random() * healthTips.length)])

// 切换待办状态
const toggleTodo = (todo) => {
  todo.completed = !todo.completed
  if (todo.completed) {
    uni.vibrateShort()
  }
}

// 添加待办
const addTodo = () => {
  uni.navigateTo({ url: '/pages/task/create' })
}

// 查看日历
const viewCalendar = () => {
  uni.navigateTo({ url: '/pages/task/calendar' })
}

// 刷新语录
const refreshQuote = () => {
  const current = quote.value
  let next = quotes[Math.floor(Math.random() * quotes.length)]
  while (next === current && quotes.length > 1) {
    next = quotes[Math.floor(Math.random() * quotes.length)]
  }
  quote.value = next
}

// 复制语录
const copyQuote = () => {
  uni.setClipboardData({
    data: `${quote.value.text} —— ${quote.value.author}`,
    success: () => {
      uni.showToast({ title: '已复制', icon: 'success' })
    }
  })
}

// 分享语录
const shareQuote = () => {
  uni.showShareMenu({
    withShareTicket: true,
    menus: ['shareAppMessage', 'shareTimeline']
  })
}

// 分享日报
const shareReport = () => {
  uni.showActionSheet({
    itemList: ['生成图片分享', '发送给好友', '分享到朋友圈'],
    success: (res) => {
      uni.showToast({ title: '分享成功', icon: 'success' })
    }
  })
}

// 返回
const goBack = () => {
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
.morning-container {
  min-height: 100vh;
  background: linear-gradient(180deg, #ffecd2 0%, #fcb69f 50%, #ffecd2 100%);
  padding-bottom: 40rpx;
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
      color: #333;
    }
  }
  
  .nav-title {
    font-size: 36rpx;
    font-weight: 600;
    color: #333;
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
    animation: sun-pulse 2s ease-in-out infinite;
  }
  
  .greeting-text {
    font-size: 48rpx;
    font-weight: 700;
    color: #333;
    margin-right: 16rpx;
  }
  
  .greeting-name {
    font-size: 48rpx;
    font-weight: 700;
    color: #ff6b6b;
  }
  
  .greeting-date {
    display: block;
    font-size: 30rpx;
    color: #666;
    margin-top: 16rpx;
  }
}

@keyframes sun-pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.1); }
}

// 天气卡片
.weather-card {
  margin: 20rpx;
  padding: 30rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 32rpx;
  color: #fff;
  box-shadow: 0 8rpx 32rpx rgba(102, 126, 234, 0.3);
}

.weather-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30rpx;
  
  .location {
    display: flex;
    align-items: center;
    
    .loc-icon {
      font-size: 28rpx;
      margin-right: 8rpx;
    }
    
    .loc-text {
      font-size: 28rpx;
      font-weight: 500;
    }
  }
  
  .update-time {
    font-size: 24rpx;
    opacity: 0.7;
  }
}

.weather-main {
  display: flex;
  align-items: center;
  margin-bottom: 30rpx;
  
  .weather-icon {
    font-size: 100rpx;
    margin-right: 30rpx;
  }
  
  .weather-temp {
    display: flex;
    align-items: flex-start;
    margin-right: 30rpx;
    
    .temp-num {
      font-size: 96rpx;
      font-weight: 300;
      line-height: 1;
    }
    
    .temp-unit {
      font-size: 40rpx;
      margin-top: 10rpx;
    }
  }
  
  .weather-desc {
    .weather-text {
      display: block;
      font-size: 36rpx;
      font-weight: 500;
      margin-bottom: 8rpx;
    }
    
    .weather-feel {
      font-size: 26rpx;
      opacity: 0.8;
    }
  }
}

.weather-details {
  display: flex;
  justify-content: space-around;
  padding: 24rpx 0;
  border-top: 1rpx solid rgba(255,255,255,0.2);
  border-bottom: 1rpx solid rgba(255,255,255,0.2);
  margin-bottom: 24rpx;
  
  .detail-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    
    .detail-icon {
      font-size: 36rpx;
      margin-bottom: 8rpx;
    }
    
    .detail-label {
      font-size: 24rpx;
      opacity: 0.7;
      margin-bottom: 4rpx;
    }
    
    .detail-value {
      font-size: 28rpx;
      font-weight: 500;
    }
  }
}

.clothing-tips {
  padding: 20rpx;
  background: rgba(255,255,255,0.15);
  border-radius: 16rpx;
  
  .tips-title {
    display: block;
    font-size: 28rpx;
    font-weight: 600;
    margin-bottom: 12rpx;
  }
  
  .tips-content {
    font-size: 26rpx;
    opacity: 0.9;
    line-height: 1.6;
  }
}

// 待办卡片
.todo-card {
  margin: 20rpx;
  padding: 30rpx;
  background: #fff;
  border-radius: 32rpx;
  box-shadow: 0 4rpx 20rpx rgba(0,0,0,0.05);
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
      font-size: 34rpx;
      font-weight: 600;
      color: #333;
    }
  }
  
  .header-right {
    display: flex;
    align-items: center;
    gap: 16rpx;
    
    .progress-text {
      font-size: 26rpx;
      color: #667eea;
      font-weight: 600;
    }
    
    .progress-ring {
      width: 100rpx;
      height: 12rpx;
      background: #f0f0f0;
      border-radius: 6rpx;
      overflow: hidden;
      
      .progress-fill {
        height: 100%;
        background: linear-gradient(90deg, #667eea, #764ba2);
        border-radius: 6rpx;
        transition: width 0.3s;
      }
    }
  }
  
  .view-all {
    font-size: 26rpx;
    color: #667eea;
  }
}

.todo-list {
  .todo-item {
    display: flex;
    align-items: center;
    padding: 20rpx;
    background: #f8f9fa;
    border-radius: 16rpx;
    margin-bottom: 16rpx;
    
    &.completed {
      opacity: 0.6;
      
      .todo-title {
        text-decoration: line-through;
        color: #999;
      }
    }
    
    &.urgent {
      border-left: 6rpx solid #ff5722;
    }
    
    .todo-check {
      margin-right: 20rpx;
      
      .check-icon {
        font-size: 44rpx;
      }
    }
    
    .todo-content {
      flex: 1;
      
      .todo-title {
        font-size: 30rpx;
        color: #333;
        margin-bottom: 8rpx;
      }
      
      .todo-meta {
        display: flex;
        gap: 16rpx;
        
        .todo-time {
          font-size: 24rpx;
          color: #999;
        }
        
        .todo-category {
          padding: 4rpx 16rpx;
          background: #e3f2fd;
          color: #1976d2;
          font-size: 22rpx;
          border-radius: 12rpx;
        }
      }
    }
    
    .urgent-badge {
      padding: 6rpx 16rpx;
      background: #ffebee;
      color: #f44336;
      font-size: 22rpx;
      border-radius: 12rpx;
    }
  }
}

.empty-todo {
  text-align: center;
  padding: 60rpx 0;
  
  .empty-icon {
    font-size: 80rpx;
    margin-bottom: 20rpx;
    display: block;
  }
  
  .empty-text {
    display: block;
    font-size: 32rpx;
    color: #333;
    margin-bottom: 12rpx;
  }
  
  .empty-sub {
    font-size: 26rpx;
    color: #999;
  }
}

.add-todo-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24rpx;
  background: #f5f5f5;
  border-radius: 16rpx;
  margin-top: 20rpx;
  font-size: 28rpx;
  color: #667eea;
  
  .add-icon {
    margin-right: 12rpx;
  }
}

// 日程卡片
.schedule-card {
  margin: 20rpx;
  padding: 30rpx;
  background: #fff;
  border-radius: 32rpx;
  box-shadow: 0 4rpx 20rpx rgba(0,0,0,0.05);
}

.schedule-list {
  .schedule-item {
    display: flex;
    padding: 20rpx 0;
    border-bottom: 1rpx solid #f5f5f5;
    
    &:last-child {
      border-bottom: none;
    }
    
    .schedule-time {
      display: flex;
      flex-direction: column;
      align-items: center;
      width: 100rpx;
      padding-right: 24rpx;
      
      .time-start, .time-end {
        font-size: 26rpx;
        color: #667eea;
        font-weight: 500;
      }
      
      .time-line {
        flex: 1;
        width: 2rpx;
        background: #e0e0e0;
        margin: 8rpx 0;
      }
    }
    
    .schedule-content {
      flex: 1;
      
      .content-main {
        display: flex;
        align-items: center;
        margin-bottom: 12rpx;
        
        .event-title {
          font-size: 30rpx;
          font-weight: 500;
          color: #333;
          margin-right: 16rpx;
        }
        
        .important-badge {
          padding: 4rpx 12rpx;
          background: #fff3e0;
          color: #ff9800;
          font-size: 20rpx;
          border-radius: 8rpx;
        }
      }
      
      .event-location {
        display: flex;
        align-items: center;
        font-size: 26rpx;
        color: #999;
        margin-bottom: 12rpx;
        
        .loc-icon {
          margin-right: 8rpx;
        }
      }
      
      .event-members {
        .member-avatars {
          font-size: 32rpx;
          letter-spacing: -8rpx;
        }
      }
    }
  }
}

// 语录卡片
.quote-card {
  position: relative;
  margin: 20rpx;
  padding: 40rpx 30rpx;
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  border-radius: 32rpx;
  color: #fff;
  overflow: hidden;
  
  .quote-decoration {
    position: absolute;
    top: 20rpx;
    left: 30rpx;
    font-size: 120rpx;
    opacity: 0.1;
    font-family: serif;
    line-height: 1;
  }
}

.quote-content {
  position: relative;
  z-index: 1;
  margin-bottom: 30rpx;
  
  .quote-text {
    display: block;
    font-size: 32rpx;
    line-height: 1.8;
    margin-bottom: 20rpx;
    text-align: center;
  }
  
  .quote-author {
    display: block;
    text-align: right;
    font-size: 28rpx;
    opacity: 0.9;
  }
}

.quote-actions {
  display: flex;
  justify-content: center;
  gap: 40rpx;
  padding-top: 24rpx;
  border-top: 1rpx solid rgba(255,255,255,0.2);
  
  .quote-btn {
    display: flex;
    align-items: center;
    font-size: 26rpx;
    opacity: 0.9;
    
    &:active {
      opacity: 0.7;
    }
    
    .btn-icon {
      margin-right: 8rpx;
    }
  }
}

// 健康小贴士
.health-tips {
  margin: 20rpx;
  padding: 24rpx 30rpx;
  background: #e8f5e9;
  border-radius: 24rpx;
  border-left: 8rpx solid #4caf50;
  
  .tips-header {
    display: flex;
    align-items: center;
    margin-bottom: 12rpx;
    
    .tips-icon {
      font-size: 32rpx;
      margin-right: 12rpx;
    }
    
    .tips-title {
      font-size: 30rpx;
      font-weight: 600;
      color: #2e7d32;
    }
  }
  
  .tips-desc {
    font-size: 28rpx;
    color: #558b2f;
    line-height: 1.6;
  }
}
</style>