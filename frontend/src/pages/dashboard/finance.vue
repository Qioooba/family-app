<template>
  <view class="finance-report-page">
    <!-- 自定义导航栏 -->
    <view class="custom-nav">
      <view class="nav-back" @click="goBack">
        <text class="icon">‹</text>
      </view>
      <view class="nav-title">财务报表</view>
      <view class="nav-action" @click="showPeriodPicker = true">
        <text>{{ currentPeriod.label }}</text>
        <text class="icon-small">▼</text>
      </view>
    </view>

    <!-- 财务概览卡片 -->
    <view class="finance-overview">
      <view class="overview-header">
        <text class="overview-title">收支概览</text>
        <text class="overview-subtitle">{{ currentPeriod.dateRange }}</text>
      </view>
      
      <view class="overview-cards">
        <view class="overview-card income">
          <view class="card-icon">💰</view>
          <view class="card-info">
            <text class="card-label">总收入</text>
            <text class="card-value">¥{{ financeData.totalIncome }}</text>
          </view>
        </view>
        
        <view class="overview-card expense">
          <view class="card-icon">💸</view>
          <view class="card-info">
            <text class="card-label">总支出</text>
            <text class="card-value">¥{{ financeData.totalExpense }}</text>
          </view>
        </view>
      </view>
      
      <view class="balance-section">
        <view class="balance-item">
          <text class="balance-label">净结余</text>
          <text class="balance-value" :class="{ negative: financeData.balance < 0 }">
            ¥{{ financeData.balance }}
          </text>
        </view>
        <view class="savings-rate">
          <text class="rate-label">储蓄率</text>
          <text class="rate-value">{{ financeData.savingsRate }}%</text>
        </view>
      </view>
    </view>

    <!-- 收支趋势图表 -->
    <view class="section">
      <view class="section-header">
        <view class="section-title">
          <text class="title-icon">📈</text>
          <text>收支趋势</text>
        </view>
      </view>
      
      <view class="trend-chart-wrapper">
        <view class="trend-chart">
          <view class="chart-y-axis">
            <text v-for="i in 5" :key="i">{{ getTrendYAxis(i) }}</text>
          </view>
          
          <view class="trend-lines-area">
            <view class="grid-lines">
              <view v-for="i in 5" :key="i" class="grid-line"></view>
            </view>
            
            <!-- SVG 双折线图 -->
            <svg class="trend-svg" viewBox="0 0 100 100" preserveAspectRatio="none">
              <!-- 收入线 -->
              <polyline 
                :points="getIncomeLinePoints()" 
                fill="none" 
                stroke="#22c55e" 
                stroke-width="2.5"
                stroke-linecap="round"
                stroke-linejoin="round"
              />
              <!-- 支出线 -->
              <polyline 
                :points="getExpenseLinePoints()" 
                fill="none" 
                stroke="#ef4444" 
                stroke-width="2.5"
                stroke-linecap="round"
                stroke-linejoin="round"
              />
            </svg>
            
            <!-- 数据点 -->
            <view class="trend-points">
              <view 
                v-for="(item, index) in trendData" 
                :key="'income-'+index"
                class="trend-point income"
                :style="{ 
                  left: getPointPosition(index, trendData.length, 'x') + '%',
                  bottom: getPointPosition(item.income, maxTrendValue, 'y') + '%'
                }"
              >
                <view class="point-dot"></view>
              </view>
              <view 
                v-for="(item, index) in trendData" 
                :key="'expense-'+index"
                class="trend-point expense"
                :style="{ 
                  left: getPointPosition(index, trendData.length, 'x') + '%',
                  bottom: getPointPosition(item.expense, maxTrendValue, 'y') + '%'
                }"
              >
                <view class="point-dot"></view>
              </view>
            </view>
          </view>
        </view>
        
        <view class="trend-x-axis">
          <text v-for="(item, index) in trendData" :key="index">{{ item.label }}</text>
        </view>
      </view>
      
      <view class="trend-legend">
        <view class="legend-item">
          <view class="legend-line income"></view>
          <text>收入</text>
        </view>
        <view class="legend-item">
          <view class="legend-line expense"></view>
          <text>支出</text>
        </view>
      </view>
    </view>

    <!-- 预算进度 -->
    <view class="section">
      <view class="section-header">
        <view class="section-title">
          <text class="title-icon">🎯</text>
          <text>预算进度</text>
        </view>
        <view class="section-badge" :class="budgetStatus.type">
          <text>{{ budgetStatus.text }}</text>
        </view>
      </view>
      
      <view class="budget-list">
        <view 
          v-for="(budget, index) in budgetData" 
          :key="index"
          class="budget-item"
        >
          <view class="budget-info">
            <view class="budget-icon" :style="{ background: budget.color + '20' }">
              <text :style="{ color: budget.color }">{{ budget.icon }}</text>
            </view>
            <view class="budget-text">
              <text class="budget-name">{{ budget.name }}</text>
              <text class="budget-amount">¥{{ budget.spent }} / ¥{{ budget.total }}</text>
            </view>
          </view>
          
          <view class="budget-progress">
            <view class="progress-bg">
              <view 
                class="progress-fill" 
                :style="{ 
                  width: Math.min((budget.spent / budget.total) * 100, 100) + '%',
                  background: budget.color
                }"
                :class="{ warning: budget.spent / budget.total > 0.8 }"
              ></view>
            </view>
            <text class="progress-percent">{{ Math.round((budget.spent / budget.total) * 100) }}%</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 储蓄追踪 -->
    <view class="section">
      <view class="section-header">
        <view class="section-title">
          <text class="title-icon">🏦</text>
          <text>储蓄追踪</text>
        </view>
        <view class="section-badge">
          <text>目标 ¥{{ savingsGoal }}</text>
        </view>
      </view>
      
      <view class="savings-card">
        <view class="savings-header">
          <view class="savings-total">
            <text class="savings-label">当前储蓄</text>
            <text class="savings-value">¥{{ currentSavings }}</text>
          </view>
          <view class="savings-goal">
            <text class="goal-label">目标进度</text>
            <text class="goal-value">{{ savingsProgress }}%</text>
          </view>
        </view>
        
        <!-- 面积图 -->
        <view class="savings-chart">
          <view class="savings-svg-wrapper">
            <svg class="savings-svg" viewBox="0 0 100 60" preserveAspectRatio="none">
              <defs>
                <linearGradient id="savingsGradient" x1="0%" y1="0%" x2="0%" y2="100%">
                  <stop offset="0%" style="stop-color:#667eea;stop-opacity:0.4" />
                  <stop offset="100%" style="stop-color:#667eea;stop-opacity:0.05" />
                </linearGradient>
              </defs>
              <polygon 
                :points="getSavingsAreaPoints()" 
                fill="url(#savingsGradient)"
              />
              <polyline 
                :points="getSavingsLinePoints()" 
                fill="none" 
                stroke="#667eea" 
                stroke-width="2"
                stroke-linecap="round"
                stroke-linejoin="round"
              />
            </svg>
          </view>
        </view>
        
        <view class="savings-timeline">
          <text v-for="(item, index) in savingsData" :key="index">{{ item.month }}</text>
        </view>
        
        <view class="savings-stats">
          <view class="savings-stat">
            <text class="stat-value">+¥{{ monthlyAvgSavings }}</text>
            <text class="stat-label">月均储蓄</text>
          </view>
          <view class="savings-stat">
            <text class="stat-value">{{ monthsToGoal }}个月</text>
            <text class="stat-label">预计达成</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 支出分类饼图 -->
    <view class="section">
      <view class="section-header">
        <view class="section-title">
          <text class="title-icon">🥧</text>
          <text>支出分类</text>
        </view>
      </view>
      
      <view class="expense-pie-section">
        <view class="pie-chart-wrapper">
          <view class="pie-chart">
            <view 
              v-for="(item, index) in expenseCategories" 
              :key="index"
              class="pie-slice"
              :style="getPieSliceStyle(index)"
            ></view>
            <view class="pie-center">
              <text class="pie-total">¥{{ financeData.totalExpense }}</text>
              <text class="pie-label">总支出</text>
            </view>
          </view>
        </view>
        
        <view class="expense-legend">
          <view 
            v-for="(item, index) in expenseCategories" 
            :key="index"
            class="legend-row"
          >
            <view class="legend-marker">
              <view class="marker-dot" :style="{ background: item.color }"></view>
              <text class="marker-label">{{ item.name }}</text>
            </view>
            <view class="legend-values">
              <text class="legend-amount">¥{{ item.amount }}</text>
              <text class="legend-percent">{{ item.percent }}%</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 周期选择弹窗 -->
    <view v-if="showPeriodPicker" class="modal-overlay">
      <view class="modal-mask" @click="showPeriodPicker = false"></view>
      <view class="modal-content">
        <view class="modal-header">
          <text>选择周期</text>
          <text class="close-btn" @click="showPeriodPicker = false">✕</text>
        </view>
        
        <view class="period-list">
          <view 
            v-for="period in periods" 
            :key="period.value"
            class="period-item"
            :class="{ active: currentPeriod.value === period.value }"
            @click="selectPeriod(period)"
          >
            <text>{{ period.label }}</text>
            <text v-if="currentPeriod.value === period.value" class="check-mark">✓</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'

const showPeriodPicker = ref(false)

const periods = [
  { label: '本月', value: 'month', dateRange: '2024年1月1日 - 1月31日' },
  { label: '上月', value: 'lastMonth', dateRange: '2023年12月1日 - 12月31日' },
  { label: '本季度', value: 'quarter', dateRange: '2024年1月1日 - 3月31日' },
  { label: '本年度', value: 'year', dateRange: '2024年1月1日 - 12月31日' }
]

const currentPeriod = ref(periods[0])

// 财务数据
const financeData = ref({
  totalIncome: 15800,
  totalExpense: 8960,
  balance: 6840,
  savingsRate: 43
})

// 趋势数据
const trendData = ref([
  { label: '1日', income: 0, expense: 320 },
  { label: '5日', income: 8000, expense: 450 },
  { label: '10日', income: 0, expense: 680 },
  { label: '15日', income: 0, expense: 520 },
  { label: '20日', income: 7800, expense: 890 },
  { label: '25日', income: 0, expense: 410 },
  { label: '30日', income: 0, expense: 380 }
])

const maxTrendValue = 10000

// 预算数据
const budgetData = ref([
  { name: '餐饮美食', icon: '🍽️', total: 3000, spent: 2150, color: '#ff6b6b' },
  { name: '交通出行', icon: '🚗', total: 1500, spent: 980, color: '#3b82f6' },
  { name: '购物消费', icon: '🛍️', total: 2000, spent: 1680, color: '#f59e0b' },
  { name: '娱乐休闲', icon: '🎮', total: 1000, spent: 650, color: '#8b5cf6' },
  { name: '生活缴费', icon: '💡', total: 800, spent: 520, color: '#10b981' }
])

// 储蓄数据
const savingsGoal = ref(50000)
const currentSavings = ref(28650)
const monthlyAvgSavings = ref(4200)

const savingsData = ref([
  { month: '8月', amount: 18000 },
  { month: '9月', amount: 20500 },
  { month: '10月', amount: 22800 },
  { month: '11月', amount: 25600 },
  { month: '12月', amount: 26500 },
  { month: '1月', amount: 28650 }
])

// 支出分类
const expenseCategories = ref([
  { name: '餐饮美食', amount: 2150, percent: 24, color: '#ff6b6b' },
  { name: '交通出行', amount: 1680, percent: 19, color: '#3b82f6' },
  { name: '购物消费', amount: 1520, percent: 17, color: '#f59e0b' },
  { name: '娱乐休闲', amount: 1280, percent: 14, color: '#8b5cf6' },
  { name: '生活缴费', amount: 980, percent: 11, color: '#10b981' },
  { name: '其他', amount: 1350, percent: 15, color: '#6b7280' }
])

// 计算属性
const budgetStatus = computed(() => {
  const totalSpent = budgetData.value.reduce((sum, b) => sum + b.spent, 0)
  const totalBudget = budgetData.value.reduce((sum, b) => sum + b.total, 0)
  const ratio = totalSpent / totalBudget
  
  if (ratio > 0.95) return { type: 'danger', text: '超支预警' }
  if (ratio > 0.8) return { type: 'warning', text: '接近上限' }
  return { type: 'good', text: '预算正常' }
})

const savingsProgress = computed(() => {
  return Math.round((currentSavings.value / savingsGoal.value) * 100)
})

const monthsToGoal = computed(() => {
  const remaining = savingsGoal.value - currentSavings.value
  return Math.ceil(remaining / monthlyAvgSavings.value)
})

// 初始化
onMounted(() => {
  loadFinanceData()
})

// 加载财务数据
const loadFinanceData = async () => {
  // 模拟加载数据
}

// 返回上一页
const goBack = () => {
  uni.navigateBack()
}

// 选择周期
const selectPeriod = (period) => {
  currentPeriod.value = period
  showPeriodPicker.value = false
  loadFinanceData()
}

// 获取趋势图Y轴刻度
const getTrendYAxis = (i) => {
  return Math.round(maxTrendValue - (maxTrendValue / 4) * (i - 1))
}

// 获取收入折线点
const getIncomeLinePoints = () => {
  return trendData.value.map((item, index) => {
    const x = trendData.value.length > 1 ? (index / (trendData.value.length - 1)) * 100 : 50
    const y = 100 - (item.income / maxTrendValue) * 100
    return `${x},${y}`
  }).join(' ')
}

// 获取支出折线点
const getExpenseLinePoints = () => {
  return trendData.value.map((item, index) => {
    const x = trendData.value.length > 1 ? (index / (trendData.value.length - 1)) * 100 : 50
    const y = 100 - (item.expense / maxTrendValue) * 100
    return `${x},${y}`
  }).join(' ')
}

// 获取数据点位置
const getPointPosition = (value, max, axis) => {
  if (axis === 'x') {
    const index = value
    return trendData.value.length > 1 ? (index / (trendData.value.length - 1)) * 100 : 50
  }
  return (value / max) * 100
}

// 获取储蓄面积图点
const getSavingsAreaPoints = () => {
  const linePoints = getSavingsLinePoints()
  if (!linePoints) return ''
  return `0,60 ${linePoints} 100,60`
}

// 获取储蓄折线点
const getSavingsLinePoints = () => {
  const data = savingsData.value
  if (data.length === 0) return ''
  
  const maxAmount = savingsGoal.value
  const points = data.map((item, index) => {
    const x = (index / (data.length - 1)) * 100
    const y = 60 - (item.amount / maxAmount) * 60
    return `${x},${y}`
  })
  return points.join(' ')
}

// 获取饼图切片样式
const getPieSliceStyle = (index) => {
  const categories = expenseCategories.value
  let cumulativePercent = 0
  
  for (let i = 0; i < index; i++) {
    cumulativePercent += categories[i].percent
  }
  
  const item = categories[index]
  return {
    background: `conic-gradient(${item.color} 0deg, ${item.color} ${item.percent * 3.6}deg, transparent ${item.percent * 3.6}deg)`,
    transform: `rotate(${cumulativePercent * 3.6}deg)`
  }
}
</script>

<style lang="scss" scoped>
.finance-report-page {
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

// 财务概览
.finance-overview {
  margin: 30rpx;
  padding: 40rpx;
  background: #fff;
  border-radius: 32rpx;
  box-shadow: 0 4rpx 20rpx rgba(0,0,0,0.05);
  
  .overview-header {
    margin-bottom: 30rpx;
    
    .overview-title {
      display: block;
      font-size: 32rpx;
      font-weight: 600;
      color: #333;
      margin-bottom: 8rpx;
    }
    
    .overview-subtitle {
      font-size: 24rpx;
      color: #999;
    }
  }
  
  .overview-cards {
    display: flex;
    gap: 20rpx;
    margin-bottom: 30rpx;
    
    .overview-card {
      flex: 1;
      display: flex;
      align-items: center;
      gap: 20rpx;
      padding: 30rpx;
      border-radius: 20rpx;
      
      &.income {
        background: linear-gradient(135deg, #dcfce7 0%, #bbf7d0 100%);
      }
      
      &.expense {
        background: linear-gradient(135deg, #fee2e2 0%, #fecaca 100%);
      }
      
      .card-icon {
        font-size: 48rpx;
      }
      
      .card-info {
        .card-label {
          display: block;
          font-size: 24rpx;
          color: #666;
          margin-bottom: 8rpx;
        }
        
        .card-value {
          font-size: 36rpx;
          font-weight: 700;
          color: #333;
        }
      }
    }
  }
  
  .balance-section {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 30rpx;
    background: #f8fafc;
    border-radius: 20rpx;
    
    .balance-item {
      .balance-label {
        display: block;
        font-size: 24rpx;
        color: #666;
        margin-bottom: 8rpx;
      }
      
      .balance-value {
        font-size: 40rpx;
        font-weight: 700;
        color: #22c55e;
        
        &.negative {
          color: #ef4444;
        }
      }
    }
    
    .savings-rate {
      text-align: right;
      
      .rate-label {
        display: block;
        font-size: 24rpx;
        color: #666;
        margin-bottom: 8rpx;
      }
      
      .rate-value {
        font-size: 36rpx;
        font-weight: 700;
        color: #667eea;
      }
    }
  }
}

// 通用区块
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
      border-radius: 24rpx;
      
      &.good {
        background: #dcfce7;
        text { color: #22c55e; }
      }
      
      &.warning {
        background: #fef3c7;
        text { color: #f59e0b; }
      }
      
      &.danger {
        background: #fee2e2;
        text { color: #ef4444; }
      }
      
      text {
        font-size: 24rpx;
        font-weight: 500;
      }
    }
  }
}

// 收支趋势图
.trend-chart-wrapper {
  padding: 20rpx 0;
}

.trend-chart {
  display: flex;
  height: 280rpx;
  
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
  
  .trend-lines-area {
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
    
    .trend-svg {
      position: absolute;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
    }
    
    .trend-points {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      
      .trend-point {
        position: absolute;
        width: 24rpx;
        height: 24rpx;
        transform: translate(-50%, 50%);
        
        &.income .point-dot {
          background: #22c55e;
          box-shadow: 0 2rpx 8rpx rgba(34,197,94,0.4);
        }
        
        &.expense .point-dot {
          background: #ef4444;
          box-shadow: 0 2rpx 8rpx rgba(239,68,68,0.4);
        }
        
        .point-dot {
          width: 100%;
          height: 100%;
          border: 4rpx solid #fff;
          border-radius: 50%;
        }
      }
    }
  }
}

.trend-x-axis {
  display: flex;
  justify-content: space-between;
  padding-left: 80rpx;
  margin-top: 16rpx;
  
  text {
    font-size: 20rpx;
    color: #999;
  }
}

.trend-legend {
  display: flex;
  justify-content: center;
  gap: 40rpx;
  margin-top: 20rpx;
  
  .legend-item {
    display: flex;
    align-items: center;
    gap: 12rpx;
    
    .legend-line {
      width: 40rpx;
      height: 4rpx;
      border-radius: 2rpx;
      
      &.income {
        background: #22c55e;
      }
      
      &.expense {
        background: #ef4444;
      }
    }
    
    text {
      font-size: 24rpx;
      color: #666;
    }
  }
}

// 预算进度
.budget-list {
  display: flex;
  flex-direction: column;
  gap: 30rpx;
  
  .budget-item {
    .budget-info {
      display: flex;
      align-items: center;
      gap: 20rpx;
      margin-bottom: 16rpx;
      
      .budget-icon {
        width: 72rpx;
        height: 72rpx;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        
        text {
          font-size: 36rpx;
        }
      }
      
      .budget-text {
        flex: 1;
        
        .budget-name {
          display: block;
          font-size: 28rpx;
          color: #333;
          margin-bottom: 4rpx;
        }
        
        .budget-amount {
          font-size: 24rpx;
          color: #999;
        }
      }
    }
    
    .budget-progress {
      display: flex;
      align-items: center;
      gap: 20rpx;
      
      .progress-bg {
        flex: 1;
        height: 12rpx;
        background: #f0f0f0;
        border-radius: 6rpx;
        overflow: hidden;
        
        .progress-fill {
          height: 100%;
          border-radius: 6rpx;
          transition: width 0.5s ease;
          
          &.warning {
            background: #f59e0b !important;
          }
        }
      }
      
      .progress-percent {
        width: 80rpx;
        text-align: right;
        font-size: 24rpx;
        color: #666;
      }
    }
  }
}

// 储蓄追踪
.savings-card {
  .savings-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 30rpx;
    
    .savings-total {
      .savings-label {
        display: block;
        font-size: 24rpx;
        color: #666;
        margin-bottom: 8rpx;
      }
      
      .savings-value {
        font-size: 44rpx;
        font-weight: 700;
        color: #667eea;
      }
    }
    
    .savings-goal {
      text-align: right;
      
      .goal-label {
        display: block;
        font-size: 24rpx;
        color: #666;
        margin-bottom: 8rpx;
      }
      
      .goal-value {
        font-size: 36rpx;
        font-weight: 700;
        color: #22c55e;
      }
    }
  }
  
  .savings-chart {
    height: 180rpx;
    margin-bottom: 20rpx;
    
    .savings-svg-wrapper {
      width: 100%;
      height: 100%;
      
      .savings-svg {
        width: 100%;
        height: 100%;
      }
    }
  }
  
  .savings-timeline {
    display: flex;
    justify-content: space-between;
    margin-bottom: 30rpx;
    
    text {
      font-size: 20rpx;
      color: #999;
    }
  }
  
  .savings-stats {
    display: flex;
    justify-content: space-around;
    padding-top: 30rpx;
    border-top: 1rpx solid #f0f0f0;
    
    .savings-stat {
      text-align: center;
      
      .stat-value {
        display: block;
        font-size: 32rpx;
        font-weight: 600;
        color: #667eea;
        margin-bottom: 8rpx;
      }
      
      .stat-label {
        font-size: 24rpx;
        color: #999;
      }
    }
  }
}

// 支出分类饼图
.expense-pie-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  
  .pie-chart-wrapper {
    margin-bottom: 30rpx;
    
    .pie-chart {
      position: relative;
      width: 280rpx;
      height: 280rpx;
      border-radius: 50%;
      overflow: hidden;
      background: #f0f0f0;
      
      .pie-slice {
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        border-radius: 50%;
      }
      
      .pie-center {
        position: absolute;
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%);
        width: 160rpx;
        height: 160rpx;
        background: #fff;
        border-radius: 50%;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        
        .pie-total {
          font-size: 36rpx;
          font-weight: 700;
          color: #333;
        }
        
        .pie-label {
          font-size: 22rpx;
          color: #999;
        }
      }
    }
  }
  
  .expense-legend {
    width: 100%;
    
    .legend-row {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 16rpx 0;
      border-bottom: 1rpx solid #f0f0f0;
      
      &:last-child {
        border-bottom: none;
      }
      
      .legend-marker {
        display: flex;
        align-items: center;
        gap: 16rpx;
        
        .marker-dot {
          width: 24rpx;
          height: 24rpx;
          border-radius: 50%;
        }
        
        .marker-label {
          font-size: 28rpx;
          color: #333;
        }
      }
      
      .legend-values {
        display: flex;
        align-items: center;
        gap: 20rpx;
        
        .legend-amount {
          font-size: 28rpx;
          font-weight: 600;
          color: #333;
        }
        
        .legend-percent {
          font-size: 24rpx;
          color: #999;
          width: 60rpx;
          text-align: right;
        }
      }
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
    
    .period-list {
      .period-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 30rpx 0;
        border-bottom: 1rpx solid #f0f0f0;
        
        &:last-child {
          border-bottom: none;
        }
        
        &.active {
          text {
            color: #667eea;
            font-weight: 600;
          }
        }
        
        text {
          font-size: 30rpx;
          color: #333;
          
          &.check-mark {
            width: 44rpx;
            height: 44rpx;
            background: #667eea;
            border-radius: 50%;
            color: #fff;
            font-size: 24rpx;
            display: flex;
            align-items: center;
            justify-content: center;
          }
        }
      }
    }
  }
}
</style>