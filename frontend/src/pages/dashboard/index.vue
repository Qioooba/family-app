<template>
  <view class="dashboard-container">
    <!-- 头部 -->
    <view class="dashboard-header">
      <view class="header-title">
        <text class="icon">📊</text>
        <text class="title">数据看板</text>
      </view>
      <view class="date-selector">
        <text>{{ currentDate }}</text>
      </view>
    </view>

    <!-- 今日概览 -->
    <view class="section today-overview">
      <view class="section-title">今日概览</view>
      <view class="overview-grid">
        <view class="overview-item">
          <view class="overview-value">{{ todayData.todayTasks || 0 }}</view>
          <view class="overview-label">待办任务</view>
        </view>
        <view class="overview-item">
          <view class="overview-value calorie">{{ todayData.todayCalories || 0 }}</view>
          <view class="overview-label">今日摄入(kcal)</view>
        </view>
        <view class="overview-item">
          <view class="overview-value">{{ todayData.weeklyCompletedTasks || 0 }}</view>
          <view class="overview-label">本周完成</view>
        </view>
        <view class="overview-item">
          <view class="overview-value">{{ todayData.memberCount || 0 }}</view>
          <view class="overview-label">家庭成员</view>
        </view>
      </view>
    </view>

    <!-- 周热量趋势图 -->
    <view class="section">
      <view class="section-title">本周热量趋势</view>
      <view class="chart-container">
        <view v-if="calorieTrend.length > 0" class="chart-bars">
          <view 
            v-for="(item, index) in calorieTrend" 
            :key="index"
            class="chart-bar-item"
          >
            <view class="bar-wrapper">
              <view 
                class="bar" 
                :style="{ height: getBarHeight(item.calories) + '%' }"
                :class="{ high: item.calories > 2500, low: item.calories < 1500 }"
              ></view>
            </view>
            <view class="bar-label">{{ getWeekDay(index) }}</view>
            <view class="bar-value">{{ item.calories }}</view>
          </view>
        </view>
        <view v-else class="chart-empty">暂无数据</view>
      </view>
      
      <view class="chart-summary">
        <text>本周平均: <text class="highlight">{{ weeklyAvgCalories }}</text> kcal/天</text>
      </view>
    </view>

    <!-- 任务统计 -->
    <view class="section">
      <view class="section-title">任务统计</view>
      
      <view class="stats-row">
        <view class="stat-circle">
          <view class="circle-chart">
            <view class="circle-bg"></view>
            <view class="circle-progress" :style="{ '--progress': taskCompletionRate + '%' }"></view>
            <view class="circle-text">
              <text class="percentage">{{ taskCompletionRate }}%</text>
              <text class="label">完成率</text>
            </view>
          </view>
        </view>
        
        <view class="stat-details">
          <view class="detail-item"
                v-for="(item, index) in taskStatusList" 
                :key="index"
          >
            <view class="detail-dot" :style="{ background: item.color }"></view>
            <view class="detail-info">
              <text class="detail-value">{{ item.count }}</text>
              <text class="detail-label">{{ item.label }}</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 月度概览 -->
    <view class="section">
      <view class="section-title">本月概览</view>
      
      <view class="monthly-stats">
        <view class="monthly-item"
          v-for="(item, index) in monthlyData" 
          :key="index"
        >
          <view class="monthly-icon" :style="{ background: item.bgColor }">
            <text>{{ item.icon }}</text>
          </view>
          
          <view class="monthly-info"
          >
            <text class="monthly-value">{{ item.value }}</text>
            <text class="monthly-label">{{ item.label }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 高级报表入口 -->
    <view class="section report-section">
      <view class="section-title">高级报表</view>
      <view class="report-grid">
        <view class="report-card" @click="navigateTo('/pages/dashboard/monthly')">
          <view class="report-icon monthly">📊</view>
          <view class="report-info">
            <text class="report-name">月度报告</text>
            <text class="report-desc">任务、饮食、心愿统计</text>
          </view>
          <text class="report-arrow">›</text>
        </view>
        
        <view class="report-card" @click="navigateTo('/pages/dashboard/finance')">
          <view class="report-icon finance">💰</view>
          <view class="report-info">
            <text class="report-name">财务报表</text>
            <text class="report-desc">收支、预算、储蓄追踪</text>
          </view>
          <text class="report-arrow">›</text>
        </view>
        
        <view class="report-card" @click="navigateTo('/pages/dashboard/yearly-memory')">
          <view class="report-icon memory">🎞️</view>
          <view class="report-info">
            <text class="report-name">年度回忆</text>
            <text class="report-desc">照片墙、里程碑、徽章</text>
          </view>
          <text class="report-arrow">›</text>
        </view>
      </view>
    </view>

    <!-- 年度徽章 -->
    <view class="section badges-section" v-if="badges.length > 0"
  >
      <view class="section-title">年度成就</view>
      
      <view class="badges-list"
      >
        <view 
          class="badge-item" 
          v-for="(badge, index) in badges" 
          :key="index"
        >
          <view class="badge-icon"
          >{{ badge.icon }}</view>
          
          <view class="badge-info"
          >
            <text class="badge-name"
          >{{ badge.name }}</text>
            <text class="badge-desc"
          >{{ badge.description }}</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { statsApi } from '@/api/stats'

const currentDate = ref('')
const todayData = ref({})
const calorieTrend = ref([])
const weeklyAvgCalories = ref(0)
const taskStats = ref({
  totalTasks: 0,
  completedTasks: 0,
  completionRate: 0,
  statusDistribution: {}
})
const monthlyData = ref([])
const badges = ref([])

// 任务状态列表
const taskStatusList = computed(() => [
  { label: '待办', count: taskStats.value.statusDistribution?.[0] || 0, color: '#2979ff' },
  { label: '进行中', count: taskStats.value.statusDistribution?.[1] || 0, color: '#ffa726' },
  { label: '已完成', count: taskStats.value.statusDistribution?.[2] || 0, color: '#66bb6a' },
  { label: '已取消', count: taskStats.value.statusDistribution?.[3] || 0, color: '#9e9e9e' }
])

// 任务完成率
const taskCompletionRate = computed(() => {
  return Math.round(taskStats.value.completionRate || 0)
})

// 获取星期几
const getWeekDay = (index) => {
  const days = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
  return days[index]
}

// 计算柱状图高度
const getBarHeight = (calories) => {
  const max = Math.max(...calorieTrend.value.map(d => d.calories), 2500)
  return Math.min((calories / max) * 100, 100)
}

// 加载数据
const loadData = async () => {
  const now = new Date()
  currentDate.value = `${now.getFullYear()}年${now.getMonth() + 1}月${now.getDate()}日`
  
  try {
    // 今日概览
    const todayRes = await statsApi.getTodayOverview()
    if (todayRes.code === 200) {
      todayData.value = todayRes.data
    }
    
    // 饮食统计（周）
    const dietRes = await statsApi.getDietStats('weekly')
    if (dietRes.code === 200) {
      calorieTrend.value = dietRes.data.calorieTrend || []
      weeklyAvgCalories.value = dietRes.data.avgCalories || 0
    }
    
    // 任务统计
    const startDate = new Date(now.getTime() - 30 * 24 * 60 * 60 * 1000)
    const endDate = new Date()
    const taskRes = await statsApi.getTaskStats(
      formatDate(startDate),
      formatDate(endDate)
    )
    if (taskRes.code === 200) {
      taskStats.value = taskRes.data
    }
    
    // 月度数据
    const monthRes = await statsApi.getPersonalStats('monthly', formatDate(now).substring(0, 7))
    if (monthRes.code === 200) {
      const data = monthRes.data
      monthlyData.value = [
        { icon: '🔥', value: (data.totalCalories || 0) + ' kcal', label: '本月总热量', bgColor: '#fff3e0' },
        { icon: '📅', value: (data.dietDays || 0) + ' 天', label: '记录天数', bgColor: '#e3f2fd' },
        { icon: '⚡', value: (data.avgDailyCalories || 0) + ' kcal', label: '日均热量', bgColor: '#f3e5f5' }
      ]
    }
    
    // 年度徽章
    const yearRes = await statsApi.getYearlyStats(now.getFullYear())
    if (yearRes.code === 200) {
      badges.value = yearRes.data.badges || []
    }
  } catch (error) {
    console.error('加载统计数据失败', error)
  }
}

const formatDate = (date) => {
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

// 页面导航
const navigateTo = (url) => {
  uni.navigateTo({ url })
}

onMounted(() => {
  loadData()
})
</script>

<style lang="scss" scoped>
.dashboard-container {
  min-height: 100vh;
  background: #f5f7fa;
  padding-bottom: 40rpx;
}

.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 40rpx 30rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  
  .header-title {
    display: flex;
    align-items: center;
    
    .icon {
      font-size: 48rpx;
      margin-right: 16rpx;
    }
    
    .title {
      font-size: 40rpx;
      font-weight: 600;
      color: #fff;
    }
  }
  
  .date-selector {
    font-size: 26rpx;
    color: rgba(255, 255, 255, 0.8);
  }
}

.section {
  margin: 30rpx;
  padding: 30rpx;
  background: #fff;
  border-radius: 24rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.05);
}

.section-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #333;
  margin-bottom: 24rpx;
  padding-left: 20rpx;
  border-left: 8rpx solid #667eea;
}

.today-overview {
  .overview-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 20rpx;
  }
  
  .overview-item {
    text-align: center;
    padding: 20rpx 10rpx;
    background: #f8f9fa;
    border-radius: 16rpx;
    
    .overview-value {
      font-size: 40rpx;
      font-weight: 700;
      color: #667eea;
      
      &.calorie {
        color: #ff6b6b;
      }
    }
    
    .overview-label {
      font-size: 24rpx;
      color: #999;
      margin-top: 8rpx;
    }
  }
}

.chart-container {
  height: 300rpx;
  display: flex;
  align-items: flex-end;
  padding: 20rpx 0;
  
  .chart-bars {
    display: flex;
    justify-content: space-around;
    width: 100%;
    height: 100%;
    
    .chart-bar-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      flex: 1;
      height: 100%;
      
      .bar-wrapper {
        flex: 1;
        width: 60%;
        display: flex;
        align-items: flex-end;
        background: #f5f5f5;
        border-radius: 8rpx 8rpx 0 0;
        overflow: hidden;
        
        .bar {
          width: 100%;
          background: linear-gradient(180deg, #667eea 0%, #764ba2 100%);
          border-radius: 8rpx 8rpx 0 0;
          transition: height 0.5s ease;
          
          &.high {
            background: linear-gradient(180deg, #ff6b6b 0%, #ee5a5a 100%);
          }
          
          &.low {
            background: linear-gradient(180deg, #66bb6a 0%, #4caf50 100%);
          }
        }
      }
      
      .bar-label {
        font-size: 22rpx;
        color: #999;
        margin-top: 12rpx;
      }
      
      .bar-value {
        font-size: 20rpx;
        color: #667eea;
        margin-top: 4rpx;
      }
    }
  }
  
  .chart-empty {
    width: 100%;
    text-align: center;
    color: #999;
    font-size: 28rpx;
  }
}

.chart-summary {
  text-align: center;
  padding-top: 20rpx;
  font-size: 28rpx;
  color: #666;
  
  .highlight {
    color: #667eea;
    font-weight: 600;
    font-size: 32rpx;
  }
}

.stats-row {
  display: flex;
  align-items: center;
}

.stat-circle {
  width: 200rpx;
  height: 200rpx;
  margin-right: 40rpx;
  
  .circle-chart {
    position: relative;
    width: 100%;
    height: 100%;
    
    .circle-bg {
      position: absolute;
      width: 100%;
      height: 100%;
      border-radius: 50%;
      background: #f0f0f0;
    }
    
    .circle-progress {
      position: absolute;
      width: 100%;
      height: 100%;
      border-radius: 50%;
      background: conic-gradient(
        #667eea 0deg,
        #667eea calc(var(--progress) * 3.6deg),
        transparent calc(var(--progress) * 3.6deg)
      );
      -webkit-mask: radial-gradient(transparent 55%, black 56%);
      mask: radial-gradient(transparent 55%, black 56%);
    }
    
    .circle-text {
      position: absolute;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      text-align: center;
      
      .percentage {
        display: block;
        font-size: 48rpx;
        font-weight: 700;
        color: #667eea;
      }
      
      .label {
        font-size: 24rpx;
        color: #999;
      }
    }
  }
}

.stat-details {
  flex: 1;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20rpx;
  
  .detail-item {
    display: flex;
    align-items: center;
    padding: 20rpx;
    background: #f8f9fa;
    border-radius: 16rpx;
    
    .detail-dot {
      width: 16rpx;
      height: 16rpx;
      border-radius: 50%;
      margin-right: 16rpx;
    }
    
    .detail-info {
      flex: 1;
      
      .detail-value {
        display: block;
        font-size: 36rpx;
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

.monthly-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20rpx;
  
  .monthly-item {
    text-align: center;
    padding: 30rpx 20rpx;
    background: #f8f9fa;
    border-radius: 20rpx;
    
    .monthly-icon {
      width: 80rpx;
      height: 80rpx;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      margin: 0 auto 16rpx;
      font-size: 40rpx;
    }
    
    .monthly-value {
      display: block;
      font-size: 32rpx;
      font-weight: 600;
      color: #333;
      margin-bottom: 8rpx;
    }
    
    .monthly-label {
      font-size: 24rpx;
      color: #999;
    }
  }
}

.badges-section {
  .badges-list {
    display: flex;
    flex-wrap: wrap;
    gap: 20rpx;
  }
  
  .badge-item {
    display: flex;
    align-items: center;
    padding: 20rpx;
    background: linear-gradient(135deg, #fff9e6 0%, #fff3cc 100%);
    border-radius: 16rpx;
    flex: 1;
    min-width: 280rpx;
    
    .badge-icon {
      font-size: 56rpx;
      margin-right: 20rpx;
    }
    
    .badge-info {
      flex: 1;
      
      .badge-name {
        display: block;
        font-size: 28rpx;
        font-weight: 600;
        color: #333;
      }
      
      .badge-desc {
        font-size: 22rpx;
        color: #999;
      }
    }
  }
}

// 高级报表入口
.report-section {
  .report-grid {
    display: flex;
    flex-direction: column;
    gap: 20rpx;
  }
  
  .report-card {
    display: flex;
    align-items: center;
    padding: 30rpx;
    background: #f8f9fa;
    border-radius: 20rpx;
    transition: all 0.3s ease;
    
    &:active {
      background: #f0f0f0;
      transform: scale(0.98);
    }
    
    .report-icon {
      width: 88rpx;
      height: 88rpx;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 44rpx;
      margin-right: 24rpx;
      
      &.monthly {
        background: linear-gradient(135deg, #e0e7ff 0%, #c7d2fe 100%);
      }
      
      &.finance {
        background: linear-gradient(135deg, #dcfce7 0%, #bbf7d0 100%);
      }
      
      &.memory {
        background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
      }
    }
    
    .report-info {
      flex: 1;
      
      .report-name {
        display: block;
        font-size: 32rpx;
        font-weight: 600;
        color: #333;
        margin-bottom: 8rpx;
      }
      
      .report-desc {
        font-size: 24rpx;
        color: #999;
      }
    }
    
    .report-arrow {
      font-size: 40rpx;
      color: #ccc;
    }
  }
}
</style>
