<template>
  <view class="nutritionist-container">
    <!-- 自定义导航栏 -->
    <view class="custom-nav">
      <view class="nav-back" @click="goBack">
        <text class="icon">←</text>
      </view>
      <text class="nav-title">AI 营养师</text>
      <view class="nav-right">
        <text class="icon">🥗</text>
      </view>
    </view>

    <!-- 健康评分卡片 -->
    <view class="health-score-card">
      <view class="score-header">
        <text class="score-title">今日健康评分</text>
        <text class="score-date">{{ today }}</text>
      </view>
      <view class="score-content">
        <view class="ring-chart">
          <view class="ring-bg"></view>
          <view class="ring-progress" :style="ringStyle"></view>
          <view class="ring-inner">
            <text class="score-num">{{ healthScore }}</text>
            <text class="score-label">分</text>
          </view>
        </view>
        <view class="score-tags">
          <view class="tag" :class="scoreLevel.class">{{ scoreLevel.text }}</view>
          <view class="score-desc">{{ scoreLevel.desc }}</view>
        </view>
      </view>
    </view>

    <!-- 营养摄入环形图 -->
    <view class="nutrition-chart-card">
      <view class="card-header">
        <text class="card-title">今日营养摄入</text>
        <text class="card-subtitle">目标: 2000千卡</text>
      </view>
      <view class="chart-container">
        <view class="donut-chart">
          <view 
            v-for="(item, index) in nutritionData" 
            :key="index"
            class="donut-segment"
            :style="getSegmentStyle(item, index)"
          ></view>
          <view class="donut-center">
            <text class="center-value">{{ totalCalories }}</text>
            <text class="center-label">千卡</text>
          </view>
        </view>
        <view class="chart-legend">
          <view v-for="(item, index) in nutritionData" :key="index" class="legend-item">
            <view class="legend-dot" :style="{ background: item.color }"></view>
            <text class="legend-name">{{ item.name }}</text>
            <text class="legend-value">{{ item.value }}g</text>
            <text class="legend-percent">{{ item.percent }}%</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 营养摄入建议卡片 -->
    <view class="suggestion-card">
      <view class="card-header">
        <text class="card-title">💡 营养建议</text>
      </view>
      <view class="suggestion-list">
        <view v-for="(item, index) in suggestions" :key="index" class="suggestion-item">
          <view class="suggestion-icon">{{ item.icon }}</view>
          <view class="suggestion-content">
            <text class="suggestion-title">{{ item.title }}</text>
            <text class="suggestion-desc">{{ item.desc }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 饮食建议列表 -->
    <view class="diet-tips-card">
      <view class="card-header">
        <text class="card-title">🍽️ 今日饮食建议</text>
      </view>
      <view class="meal-list">
        <view v-for="(meal, index) in mealSuggestions" :key="index" class="meal-item">
          <view class="meal-header">
            <text class="meal-name">{{ meal.name }}</text>
            <text class="meal-time">{{ meal.time }}</text>
          </view>
          <view class="meal-foods">
            <view v-for="(food, fIndex) in meal.foods" :key="fIndex" class="food-tag">
              {{ food }}
            </view>
          </view>
          <view class="meal-calories">
            <text class="cal-label">建议摄入</text>
            <text class="cal-value">{{ meal.calories }}千卡</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 底部按钮 -->
    <view class="bottom-actions">
      <view class="action-btn primary" @click="recordMeal">
        <text class="btn-icon">📝</text>
        <text class="btn-text">记录饮食</text>
      </view>
      <view class="action-btn" @click="viewReport">
        <text class="btn-icon">📊</text>
        <text class="btn-text">查看报告</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'

// 今日日期
const today = ref(new Date().toLocaleDateString('zh-CN', { month: 'long', day: 'numeric' }))

// 健康评分
const healthScore = ref(85)
const scoreLevel = computed(() => {
  const score = healthScore.value
  if (score >= 90) return { text: '优秀', class: 'excellent', desc: '营养均衡，继续保持！' }
  if (score >= 80) return { text: '良好', class: 'good', desc: '整体不错，还有提升空间' }
  if (score >= 60) return { text: '一般', class: 'normal', desc: '需要注意饮食搭配' }
  return { text: '需改善', class: 'poor', desc: '建议调整饮食习惯' }
})

// 环形进度条样式
const ringStyle = computed(() => {
  const percent = healthScore.value
  const deg = (percent / 100) * 360
  return {
    background: `conic-gradient(#4CAF50 0deg ${deg}deg, transparent ${deg}deg 360deg)`
  }
})

// 营养数据
const nutritionData = ref([
  { name: '碳水化合物', value: 180, percent: 45, color: '#5B8FF9' },
  { name: '蛋白质', value: 80, percent: 25, color: '#5AD8A6' },
  { name: '脂肪', value: 50, percent: 20, color: '#F6BD16' },
  { name: '膳食纤维', value: 25, percent: 10, color: '#E8684A' }
])

const totalCalories = computed(() => {
  return nutritionData.value.reduce((sum, item) => sum + item.value * 4, 0)
})

// 计算环形图每一段的角度
let currentAngle = 0
const getSegmentStyle = (item, index) => {
  const startAngle = currentAngle
  const endAngle = startAngle + (item.percent / 100) * 360
  currentAngle = endAngle
  return {
    background: `conic-gradient(${item.color} ${startAngle}deg ${endAngle}deg, transparent ${endAngle}deg 360deg)`
  }
}

// 营养建议
const suggestions = ref([
  { icon: '💧', title: '多喝水', desc: '今日饮水量不足，建议再喝500ml' },
  { icon: '🥬', title: '增加蔬菜', desc: '蔬菜摄入量偏低，晚餐建议增加绿叶蔬菜' },
  { icon: '🍎', title: '适量水果', desc: '今日水果摄入充足，继续保持' },
  { icon: '🥛', title: '补充蛋白质', desc: '运动后建议补充优质蛋白' }
])

// 饮食建议
const mealSuggestions = ref([
  {
    name: '早餐',
    time: '07:00-08:00',
    foods: ['燕麦粥', '水煮蛋', '牛奶', '苹果'],
    calories: 450
  },
  {
    name: '午餐',
    time: '12:00-13:00',
    foods: ['糙米饭', '清蒸鱼', '炒时蔬', '豆腐汤'],
    calories: 650
  },
  {
    name: '晚餐',
    time: '18:00-19:00',
    foods: ['小米粥', '鸡胸肉', '凉拌黄瓜', '酸奶'],
    calories: 500
  },
  {
    name: '加餐',
    time: '15:00-16:00',
    foods: ['坚果', '酸奶'],
    calories: 150
  }
])

// 返回
const goBack = () => {
  uni.navigateBack()
}

// 记录饮食
const recordMeal = () => {
  uni.navigateTo({ url: '/pages/food/record' })
}

// 查看报告
const viewReport = () => {
  uni.navigateTo({ url: '/pages/nutrition/index' })
}
</script>

<style lang="scss" scoped>
.nutritionist-container {
  min-height: 100vh;
  background: linear-gradient(180deg, #f0f7ff 0%, #ffffff 100%);
  padding-bottom: 160rpx;
}

// 自定义导航栏
.custom-nav {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 80rpx 30rpx 20rpx;
  background: #fff;
  
  .nav-back {
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
  
  .nav-right {
    width: 60rpx;
    height: 60rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    
    .icon {
      font-size: 40rpx;
    }
  }
}

// 健康评分卡片
.health-score-card {
  margin: 20rpx;
  padding: 30rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 24rpx;
  color: #fff;
  
  .score-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 30rpx;
    
    .score-title {
      font-size: 32rpx;
      font-weight: 600;
    }
    
    .score-date {
      font-size: 26rpx;
      opacity: 0.8;
    }
  }
  
  .score-content {
    display: flex;
    align-items: center;
    gap: 40rpx;
  }
  
  .ring-chart {
    position: relative;
    width: 180rpx;
    height: 180rpx;
    
    .ring-bg {
      position: absolute;
      inset: 0;
      border-radius: 50%;
      background: rgba(255,255,255,0.2);
    }
    
    .ring-progress {
      position: absolute;
      inset: 0;
      border-radius: 50%;
      -webkit-mask: radial-gradient(transparent 60%, black 61%);
      mask: radial-gradient(transparent 60%, black 61%);
    }
    
    .ring-inner {
      position: absolute;
      inset: 20rpx;
      border-radius: 50%;
      background: rgba(255,255,255,0.15);
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      
      .score-num {
        font-size: 56rpx;
        font-weight: 700;
      }
      
      .score-label {
        font-size: 24rpx;
        opacity: 0.8;
      }
    }
  }
  
  .score-tags {
    flex: 1;
    
    .tag {
      display: inline-block;
      padding: 12rpx 30rpx;
      border-radius: 30rpx;
      font-size: 28rpx;
      font-weight: 600;
      margin-bottom: 16rpx;
      
      &.excellent {
        background: rgba(76, 175, 80, 0.3);
      }
      
      &.good {
        background: rgba(255, 193, 7, 0.3);
      }
      
      &.normal {
        background: rgba(255, 152, 0, 0.3);
      }
      
      &.poor {
        background: rgba(244, 67, 54, 0.3);
      }
    }
    
    .score-desc {
      font-size: 26rpx;
      opacity: 0.9;
      line-height: 1.5;
    }
  }
}

// 营养摄入图表
.nutrition-chart-card {
  margin: 20rpx;
  padding: 30rpx;
  background: #fff;
  border-radius: 24rpx;
  box-shadow: 0 4rpx 20rpx rgba(0,0,0,0.05);
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 30rpx;
    
    .card-title {
      font-size: 32rpx;
      font-weight: 600;
      color: #333;
    }
    
    .card-subtitle {
      font-size: 26rpx;
      color: #999;
    }
  }
}

.chart-container {
  display: flex;
  align-items: center;
  gap: 30rpx;
}

.donut-chart {
  position: relative;
  width: 240rpx;
  height: 240rpx;
  
  .donut-segment {
    position: absolute;
    inset: 0;
    border-radius: 50%;
    -webkit-mask: radial-gradient(transparent 55%, black 56%);
    mask: radial-gradient(transparent 55%, black 56%);
  }
  
  .donut-center {
    position: absolute;
    inset: 40rpx;
    border-radius: 50%;
    background: #f8f9fa;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    
    .center-value {
      font-size: 48rpx;
      font-weight: 700;
      color: #333;
    }
    
    .center-label {
      font-size: 24rpx;
      color: #999;
    }
  }
}

.chart-legend {
  flex: 1;
  
  .legend-item {
    display: flex;
    align-items: center;
    margin-bottom: 20rpx;
    
    .legend-dot {
      width: 24rpx;
      height: 24rpx;
      border-radius: 6rpx;
      margin-right: 16rpx;
    }
    
    .legend-name {
      flex: 1;
      font-size: 26rpx;
      color: #666;
    }
    
    .legend-value {
      font-size: 26rpx;
      color: #333;
      font-weight: 500;
      margin-right: 16rpx;
    }
    
    .legend-percent {
      font-size: 24rpx;
      color: #999;
      width: 60rpx;
      text-align: right;
    }
  }
}

// 建议卡片
.suggestion-card {
  margin: 20rpx;
  padding: 30rpx;
  background: #fff;
  border-radius: 24rpx;
  box-shadow: 0 4rpx 20rpx rgba(0,0,0,0.05);
  
  .card-header {
    margin-bottom: 24rpx;
    
    .card-title {
      font-size: 32rpx;
      font-weight: 600;
      color: #333;
    }
  }
}

.suggestion-list {
  .suggestion-item {
    display: flex;
    align-items: flex-start;
    padding: 20rpx 0;
    border-bottom: 1rpx solid #f5f5f5;
    
    &:last-child {
      border-bottom: none;
    }
    
    .suggestion-icon {
      width: 60rpx;
      height: 60rpx;
      border-radius: 50%;
      background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 32rpx;
      margin-right: 20rpx;
      flex-shrink: 0;
    }
    
    .suggestion-content {
      flex: 1;
      
      .suggestion-title {
        font-size: 30rpx;
        font-weight: 600;
        color: #333;
        margin-bottom: 8rpx;
      }
      
      .suggestion-desc {
        font-size: 26rpx;
        color: #999;
        line-height: 1.5;
      }
    }
  }
}

// 饮食建议
.diet-tips-card {
  margin: 20rpx;
  padding: 30rpx;
  background: #fff;
  border-radius: 24rpx;
  box-shadow: 0 4rpx 20rpx rgba(0,0,0,0.05);
  
  .card-header {
    margin-bottom: 24rpx;
    
    .card-title {
      font-size: 32rpx;
      font-weight: 600;
      color: #333;
    }
  }
}

.meal-list {
  .meal-item {
    padding: 24rpx;
    background: #f8f9fa;
    border-radius: 16rpx;
    margin-bottom: 20rpx;
    
    &:last-child {
      margin-bottom: 0;
    }
    
    .meal-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16rpx;
      
      .meal-name {
        font-size: 30rpx;
        font-weight: 600;
        color: #333;
      }
      
      .meal-time {
        font-size: 24rpx;
        color: #999;
      }
    }
    
    .meal-foods {
      display: flex;
      flex-wrap: wrap;
      gap: 12rpx;
      margin-bottom: 16rpx;
      
      .food-tag {
        padding: 8rpx 20rpx;
        background: #e3f2fd;
        color: #1976d2;
        font-size: 24rpx;
        border-radius: 20rpx;
      }
    }
    
    .meal-calories {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding-top: 16rpx;
      border-top: 1rpx solid #e0e0e0;
      
      .cal-label {
        font-size: 24rpx;
        color: #999;
      }
      
      .cal-value {
        font-size: 28rpx;
        font-weight: 600;
        color: #ff9800;
      }
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
  background: #fff;
  box-shadow: 0 -4rpx 20rpx rgba(0,0,0,0.05);
  
  .action-btn {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 28rpx 0;
    border-radius: 16rpx;
    background: #f5f5f5;
    
    &.primary {
      flex: 2;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      
      .btn-text {
        color: #fff;
      }
    }
    
    .btn-icon {
      font-size: 32rpx;
      margin-right: 12rpx;
    }
    
    .btn-text {
      font-size: 30rpx;
      font-weight: 500;
      color: #666;
    }
  }
}
</style>