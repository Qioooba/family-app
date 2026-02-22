<template>
  <view class="monthly-report-page">
    <!-- 自定义导航栏 -->
    <view class="custom-nav">
      <view class="nav-back" @click="goBack">
        <text class="icon">‹</text>
      </view>
      <view class="nav-title">月度报告</view>
      <view class="nav-action" @click="showMonthPicker = true">
        <text>{{ currentMonth }}</text>
        <text class="icon-small">▼</text>
      </view>
    </view>

    <!-- 月度总结卡片 -->
    <view class="summary-card">
      <view class="summary-header">
        <text class="summary-title">{{ currentMonth }}总结</text>
        <view class="summary-badge" :class="summaryData.level">
          <text>{{ summaryData.levelText }}</text>
        </view>
      </view>
      <view class="summary-content">
        <view class="summary-item">
          <text class="summary-value">{{ summaryData.totalScore }}</text>
          <text class="summary-label">综合评分</text>
        </view>
        <view class="summary-divider"></view>
        <view class="summary-item">
          <text class="summary-value">{{ summaryData.rank }}%</text>
          <text class="summary-label">超越家庭</text>
        </view>
        <view class="summary-divider"></view>
        <view class="summary-item">
          <text class="summary-value">{{ summaryData.growth }}%</text>
          <text class="summary-label">环比增长</text>
        </view>
      </view>
      <view class="summary-quote">
        <text>"{{ summaryData.quote }}"</text>
      </view>
    </view>

    <!-- 任务完成统计 -->
    <view class="section">
      <view class="section-header">
        <view class="section-title">
          <text class="title-icon">📋</text>
          <text>任务完成统计</text>
        </view>
        <view class="section-badge">
          <text>完成率 {{ taskStats.completionRate }}%</text>
        </view>
      </view>
      <view class="chart-wrapper">
        <view class="task-chart">
          <view 
            v-for="(item, index) in taskStats.weeklyData" 
            :key="index"
            class="task-bar-item"
          >
            <view class="bar-stack">
              <view 
                class="bar-completed" 
                :style="{ height: getTaskBarHeight(item.completed, item.total) + '%' }"
              ></view>
              <view 
                class="bar-remaining" 
                :style="{ height: getTaskBarHeight(item.total - item.completed, item.total) + '%' }"
              ></view>
            </view>
            <text class="bar-label">{{ item.week }}</text>
            <text class="bar-value">{{ item.completed }}/{{ item.total }}</text>
          </view>
        </view>
      </view>
      <view class="task-legend">
        <view class="legend-item">
          <view class="legend-dot completed"></view>
          <text>已完成</text>
        </view>
        <view class="legend-item">
          <view class="legend-dot remaining"></view>
          <text>未完成</text>
        </view>
      </view>
    </view>

    <!-- 饮食记录报表 -->
    <view class="section">
      <view class="section-header">
        <view class="section-title">
          <text class="title-icon">🍽️</text>
          <text>饮食记录趋势</text>
        </view>
        <view class="section-badge calorie">
          <text>日均 {{ dietStats.avgCalories }} kcal</text>
        </view>
      </view>
      <view class="chart-wrapper">
        <view class="diet-chart">
          <view class="chart-y-axis">
            <text v-for="i in 5" :key="i">{{ getDietYAxis(i) }}</text>
          </view>
          <view class="diet-line-area">
            <view class="grid-lines">
              <view v-for="i in 5" :key="i" class="grid-line"></view>
            </view>
            <svg class="line-svg" viewBox="0 0 100 100" preserveAspectRatio="none">
              <defs>
                <linearGradient id="lineGradient" x1="0%" y1="0%" x2="0%" y2="100%">
                  <stop offset="0%" style="stop-color:#ff6b6b;stop-opacity:0.3" />
                  <stop offset="100%" style="stop-color:#ff6b6b;stop-opacity:0" />
                </linearGradient>
              </defs>
              <polygon 
                :points="getAreaPoints()" 
                fill="url(#lineGradient)"
              />
              <polyline 
                :points="getLinePoints()" 
                fill="none" 
                stroke="#ff6b6b" 
                stroke-width="2"
                stroke-linecap="round"
                stroke-linejoin="round"
              />
            </svg>
            <view class="data-points">
              <view 
                v-for="(item, index) in dietStats.dailyData" 
                :key="index"
                class="data-point"
                :style="{ 
                  left: getPointPosition(index, dietStats.dailyData.length, 'x') + '%',
                  bottom: getPointPosition(item.calories, 3000, 'y') + '%'
                }"
              >
                <view class="point-dot"></view>
              </view>
            </view>
          </view>
        </view>
        <view class="diet-x-axis">
          <text v-for="(item, index) in dietStats.dailyData" :key="index">
            {{ item.date.substring(8) }}日
          </text>
        </view>
      </view>
      <view class="diet-stats">
        <view class="diet-stat-item">
          <text class="stat-value">{{ dietStats.totalDays }}</text>
          <text class="stat-label">记录天数</text>
        </view>
        <view class="diet-stat-item">
          <text class="stat-value">{{ dietStats.highCalorieDays }}</text>
          <text class="stat-label">高热量日</text>
        </view>
        <view class="diet-stat-item">
          <text class="stat-value">{{ dietStats.lowCalorieDays }}</text>
          <text class="stat-label">低热量日</text>
        </view>
      </view>
    </view>

    <!-- 心愿实现统计 -->
    <view class="section">
      <view class="section-header">
        <view class="section-title">
          <text class="title-icon">⭐</text>
          <text>心愿实现统计</text>
        </view>
      </view>
      <view class="wish-stats">
        <view class="wish-circle-chart">
          <view class="circle-bg">
            <view class="circle-progress" :style="{ '--progress': wishStats.completionRate }"></view>
            <view class="circle-center">
              <text class="circle-value">{{ wishStats.completionRate }}%</text>
              <text class="circle-label">实现率</text>
            </view>
          </view>
        </view>
        <view class="wish-details">
          <view class="wish-detail-item">
            <view class="detail-icon completed">✓</view>
            <view class="detail-info">
              <text class="detail-value">{{ wishStats.completed }}</text>
              <text class="detail-label">已实现</text>
            </view>
          </view>
          <view class="wish-detail-item">
            <view class="detail-icon pending">○</view>
            <view class="detail-info">
              <text class="detail-value">{{ wishStats.pending }}</text>
              <text class="detail-label">待实现</text>
            </view>
          </view>
          <view class="wish-detail-item">
            <view class="detail-icon total">★</view>
            <view class="detail-info">
              <text class="detail-value">{{ wishStats.totalAmount }}元</text>
              <text class="detail-label">总预算</text>
            </view>
          </view>
        </view>
      </view>
      <view class="wish-list" v-if="wishStats.recentWishes.length > 0">
        <view class="wish-list-title">本月实现的心愿</view>
        <view 
          v-for="(wish, index) in wishStats.recentWishes" 
          :key="index"
          class="wish-item"
        >
          <text class="wish-icon">{{ wish.icon }}</text>
          <view class="wish-info">
            <text class="wish-name">{{ wish.name }}</text>
            <text class="wish-date">{{ wish.completedDate }}</text>
          </view>
          <text class="wish-amount">{{ wish.amount }}元</text>
        </view>
      </view>
    </view>

    <!-- 月份选择弹窗 -->
    <view v-if="showMonthPicker" class="modal-overlay">
      <view class="modal-mask" @click="showMonthPicker = false"></view>
      <view class="modal-content">
        <view class="modal-header">
          <text>选择月份</text>
          <text class="close-btn" @click="showMonthPicker = false">✕</text>
        </view>
        <view class="month-grid">
          <view 
            v-for="month in availableMonths" 
            :key="month"
            class="month-item"
            :class="{ active: month === currentMonth }"
            @click="selectMonth(month)"
          >
            <text>{{ month }}</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { statsApi } from '@/api/stats'

const currentMonth = ref('')
const showMonthPicker = ref(false)

// 月度总结数据
const summaryData = ref({
  totalScore: 85,
  rank: 68,
  growth: 12,
  level: 'good',
  levelText: '优秀',
  quote: '本月表现很棒，继续保持！'
})

// 任务统计数据
const taskStats = ref({
  completionRate: 78,
  weeklyData: [
    { week: '第一周', completed: 18, total: 22 },
    { week: '第二周', completed: 20, total: 24 },
    { week: '第三周', completed: 16, total: 20 },
    { week: '第四周', completed: 21, total: 25 }
  ]
})

// 饮食统计数据
const dietStats = ref({
  avgCalories: 2150,
  totalDays: 28,
  highCalorieDays: 5,
  lowCalorieDays: 8,
  dailyData: [
    { date: '2024-01-01', calories: 2100 },
    { date: '2024-01-05', calories: 2350 },
    { date: '2024-01-10', calories: 1980 },
    { date: '2024-01-15', calories: 2200 },
    { date: '2024-01-20', calories: 2450 },
    { date: '2024-01-25', calories: 2050 },
    { date: '2024-01-30', calories: 2180 }
  ]
})

// 心愿统计数据
const wishStats = ref({
  completionRate: 65,
  completed: 13,
  pending: 7,
  totalAmount: 5680,
  recentWishes: [
    { icon: '📚', name: '购买新书', completedDate: '01-15', amount: 128 },
    { icon: '🎮', name: '游戏手柄', completedDate: '01-20', amount: 399 },
    { icon: '🍰', name: '生日蛋糕', completedDate: '01-25', amount: 268 }
  ]
})

// 可用月份列表
const availableMonths = computed(() => {
  const months = []
  const now = new Date()
  for (let i = 0; i < 12; i++) {
    const d = new Date(now.getFullYear(), now.getMonth() - i, 1)
    months.push(`${d.getFullYear()}年${d.getMonth() + 1}月`)
  }
  return months
})

// 初始化
onMounted(() => {
  const now = new Date()
  currentMonth.value = `${now.getFullYear()}年${now.getMonth() + 1}月`
  loadMonthlyData()
})

// 加载月度数据
const loadMonthlyData = async () => {
  try {
    const yearMonth = currentMonth.value.replace('年', '-').replace('月', '')
    const res = await statsApi.getPersonalStats('monthly', yearMonth)
    if (res.code === 200 && res.data) {
      // 更新数据
    }
  } catch (error) {
    console.error('加载月度数据失败', error)
  }
}

// 返回上一页
const goBack = () => {
  uni.navigateBack()
}

// 选择月份
const selectMonth = (month) => {
  currentMonth.value = month
  showMonthPicker.value = false
  loadMonthlyData()
}

// 计算任务柱状图高度
const getTaskBarHeight = (value, total) => {
  return total > 0 ? (value / total) * 100 : 0
}

// 获取饮食图表Y轴刻度
const getDietYAxis = (i) => {
  const max = 3000
  return Math.round(max - (max / 4) * (i - 1))
}

// 获取折线点坐标
const getLinePoints = () => {
  const data = dietStats.value.dailyData
  if (data.length === 0) return ''
  
  const maxCalories = 3000
  const points = data.map((item, index) => {
    const x = (index / (data.length - 1)) * 100
    const y = 100 - (item.calories / maxCalories) * 100
    return `${x},${y}`
  })
  return points.join(' ')
}

// 获取面积图坐标
const getAreaPoints = () => {
  const linePoints = getLinePoints()
  if (!linePoints) return ''
  return `0,100 ${linePoints} 100,100`
}

// 获取数据点位置
const getPointPosition = (value, max, axis) => {
  if (axis === 'x') {
    const index = value
    const total = dietStats.value.dailyData.length
    return total > 1 ? (index / (total - 1)) * 100 : 50
  }
  return (value / max) * 100
}
</script>

<style lang="scss" scoped>
.monthly-report-page {
  min-height: 100vh;
  background: #f5f7fa;
  padding-bottom: 40rpx;
}

// 自定义导航栏
.custom-nav {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 50rpx 30rpx 20rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  
  .nav-back {
    width: 60rpx;
    height: 60rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    
    .icon {
      font-size: 48rpx;
      color: #fff;
    }
  }
  
  .nav-title {
    font-size: 36rpx;
    font-weight: 600;
    color: #fff;
  }
  
  .nav-action {
    display: flex;
    align-items: center;
    gap: 8rpx;
    padding: 12rpx 24rpx;
    background: rgba(255,255,255,0.15);
    border-radius: 30rpx;
    
    text {
      font-size: 26rpx;
      color: #fff;
    }
    
    .icon-small {
      font-size: 20rpx;
      opacity: 0.8;
    }
  }
}

// 月度总结卡片
.summary-card {
  margin: 30rpx;
  padding: 40rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 32rpx;
  
  .summary-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 30rpx;
    
    .summary-title {
      font-size: 32rpx;
      font-weight: 600;
      color: #fff;
    }
    
    .summary-badge {
      padding: 8rpx 24rpx;
      border-radius: 24rpx;
      
      &.excellent {
        background: #ffd700;
        color: #333;
      }
      
      &.good {
        background: #4ade80;
        color: #fff;
      }
      
      &.average {
        background: #fbbf24;
        color: #fff;
      }
      
      text {
        font-size: 24rpx;
        font-weight: 600;
      }
    }
  }
  
  .summary-content {
    display: flex;
    justify-content: space-around;
    align-items: center;
    margin-bottom: 30rpx;
    
    .summary-item {
      text-align: center;
      
      .summary-value {
        display: block;
        font-size: 48rpx;
        font-weight: 700;
        color: #fff;
        margin-bottom: 8rpx;
      }
      
      .summary-label {
        font-size: 24rpx;
        color: rgba(255,255,255,0.7);
      }
    }
    
    .summary-divider {
      width: 2rpx;
      height: 60rpx;
      background: rgba(255,255,255,0.2);
    }
  }
  
  .summary-quote {
    padding: 20rpx;
    background: rgba(255,255,255,0.1);
    border-radius: 16rpx;
    text-align: center;
    
    text {
      font-size: 26rpx;
      color: rgba(255,255,255,0.9);
      font-style: italic;
    }
  }
}

// 通用区块样式
.section {
  margin: 30rpx;
  padding: 30rpx;
  background: #fff;
  border-radius: 24rpx;
  box-shadow: 0 4rpx 20rpx rgba(0,0,0,0.05);
  
  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 30rpx;
    
    .section-title {
      display: flex;
      align-items: center;
      gap: 12rpx;
      font-size: 32rpx;
      font-weight: 600;
      color: #333;
      
      .title-icon {
        font-size: 36rpx;
      }
    }
    
    .section-badge {
      padding: 8rpx 20rpx;
      background: #e0e7ff;
      border-radius: 24rpx;
      
      &.calorie {
        background: #ffe4e1;
        
        text {
          color: #ff6b6b;
        }
      }
      
      text {
        font-size: 24rpx;
        color: #667eea;
        font-weight: 500;
      }
    }
  }
}

// 任务图表
.chart-wrapper {
  padding: 20rpx 0;
}

.task-chart {
  display: flex;
  justify-content: space-around;
  height: 280rpx;
  
  .task-bar-item {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    
    .bar-stack {
      flex: 1;
      width: 60rpx;
      display: flex;
      flex-direction: column;
      justify-content: flex-end;
      background: #f0f0f0;
      border-radius: 12rpx;
      overflow: hidden;
      
      .bar-completed {
        background: linear-gradient(180deg, #4ade80 0%, #22c55e 100%);
        transition: height 0.5s ease;
      }
      
      .bar-remaining {
        background: #e5e7eb;
        transition: height 0.5s ease;
      }
    }
    
    .bar-label {
      font-size: 24rpx;
      color: #666;
      margin-top: 12rpx;
    }
    
    .bar-value {
      font-size: 22rpx;
      color: #999;
      margin-top: 4rpx;
    }
  }
}

.task-legend {
  display: flex;
  justify-content: center;
  gap: 40rpx;
  margin-top: 20rpx;
  
  .legend-item {
    display: flex;
    align-items: center;
    gap: 12rpx;
    
    .legend-dot {
      width: 20rpx;
      height: 20rpx;
      border-radius: 50%;
      
      &.completed {
        background: #4ade80;
      }
      
      &.remaining {
        background: #e5e7eb;
      }
    }
    
    text {
      font-size: 24rpx;
      color: #666;
    }
  }
}

// 饮食图表
.diet-chart {
  display: flex;
  height: 300rpx;
  
  .chart-y-axis {
    width: 80rpx;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    align-items: flex-end;
    padding-right: 16rpx;
    
    text {
      font-size: 20rpx;
      color: #999;
    }
  }
  
  .diet-line-area {
    flex: 1;
    position: relative;
    
    .grid-lines {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      display: flex;
      flex-direction: column;
      justify-content: space-between;
      
      .grid-line {
        height: 1rpx;
        background: #f0f0f0;
      }
    }
    
    .line-svg {
      position: absolute;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
    }
    
    .data-points {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      
      .data-point {
        position: absolute;
        width: 24rpx;
        height: 24rpx;
        transform: translate(-50%, 50%);
        
        .point-dot {
          width: 100%;
          height: 100%;
          background: #ff6b6b;
          border: 4rpx solid #fff;
          border-radius: 50%;
          box-shadow: 0 2rpx 8rpx rgba(255,107,107,0.4);
        }
      }
    }
  }
}

.diet-x-axis {
  display: flex;
  justify-content: space-between;
  padding-left: 80rpx;
  margin-top: 16rpx;
  
  text {
    font-size: 20rpx;
    color: #999;
  }
}

.diet-stats {
  display: flex;
  justify-content: space-around;
  margin-top: 30rpx;
  padding-top: 30rpx;
  border-top: 1rpx solid #f0f0f0;
  
  .diet-stat-item {
    text-align: center;
    
    .stat-value {
      display: block;
      font-size: 36rpx;
      font-weight: 700;
      color: #ff6b6b;
      margin-bottom: 8rpx;
    }
    
    .stat-label {
      font-size: 24rpx;
      color: #999;
    }
  }
}

// 心愿统计
.wish-stats {
  display: flex;
  align-items: center;
  gap: 40rpx;
  
  .wish-circle-chart {
    width: 200rpx;
    height: 200rpx;
    
    .circle-bg {
      position: relative;
      width: 100%;
      height: 100%;
      border-radius: 50%;
      background: #f0f0f0;
      
      .circle-progress {
        position: absolute;
        width: 100%;
        height: 100%;
        border-radius: 50%;
        background: conic-gradient(
          #fbbf24 0deg,
          #fbbf24 calc(var(--progress) * 3.6deg),
          #f0f0f0 calc(var(--progress) * 3.6deg)
        );
        -webkit-mask: radial-gradient(transparent 55%, black 56%);
        mask: radial-gradient(transparent 55%, black 56%);
      }
      
      .circle-center {
        position: absolute;
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%);
        text-align: center;
        
        .circle-value {
          display: block;
          font-size: 44rpx;
          font-weight: 700;
          color: #fbbf24;
        }
        
        .circle-label {
          font-size: 22rpx;
          color: #999;
        }
      }
    }
  }
  
  .wish-details {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 24rpx;
    
    .wish-detail-item {
      display: flex;
      align-items: center;
      gap: 20rpx;
      
      .detail-icon {
        width: 56rpx;
        height: 56rpx;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 28rpx;
        
        &.completed {
          background: #dcfce7;
          color: #22c55e;
        }
        
        &.pending {
          background: #e0e7ff;
          color: #667eea;
        }
        
        &.total {
          background: #fef3c7;
          color: #fbbf24;
        }
      }
      
      .detail-info {
        .detail-value {
          display: block;
          font-size: 32rpx;
          font-weight: 600;
          color: #333;
        }
        
        .detail-label {
          font-size: 24rpx;
          color: #999;
        }
      }
    }
  }
}

.wish-list {
  margin-top: 30rpx;
  padding-top: 30rpx;
  border-top: 1rpx solid #f0f0f0;
  
  .wish-list-title {
    font-size: 28rpx;
    font-weight: 600;
    color: #333;
    margin-bottom: 20rpx;
  }
  
  .wish-item {
    display: flex;
    align-items: center;
    gap: 20rpx;
    padding: 20rpx 0;
    
    .wish-icon {
      font-size: 40rpx;
    }
    
    .wish-info {
      flex: 1;
      
      .wish-name {
        display: block;
        font-size: 28rpx;
        color: #333;
        margin-bottom: 4rpx;
      }
      
      .wish-date {
        font-size: 22rpx;
        color: #999;
      }
    }
    
    .wish-amount {
      font-size: 28rpx;
      font-weight: 600;
      color: #fbbf24;
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
  z-index: 1000;
  display: flex;
  align-items: flex-end;
  
  .modal-mask {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0,0,0,0.5);
  }
  
  .modal-content {
    position: relative;
    width: 100%;
    background: #fff;
    border-radius: 40rpx 40rpx 0 0;
    padding: 40rpx;
    animation: slideUp 0.3s ease;
    
    @keyframes slideUp {
      from { transform: translateY(100%); }
      to { transform: translateY(0); }
    }
    
    .modal-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 40rpx;
      
      text {
        font-size: 32rpx;
        font-weight: 600;
        color: #333;
        
        &.close-btn {
          font-size: 36rpx;
          color: #999;
        }
      }
    }
    
    .month-grid {
      display: grid;
      grid-template-columns: repeat(3, 1fr);
      gap: 20rpx;
      
      .month-item {
        padding: 24rpx;
        background: #f5f7fa;
        border-radius: 16rpx;
        text-align: center;
        
        &.active {
          background: #667eea;
          
          text {
            color: #fff;
          }
        }
        
        text {
          font-size: 26rpx;
          color: #666;
        }
      }
    }
  }
}
</style>