<template>
  <view class="nutrition-page">
    <!-- 顶部导航 -->
    <view class="nav-bar"
003e
      <view class="back-btn" @click="goBack"
>
        <u-icon name="arrow-left" size="40" color="#333"></u-icon>
      </view>
      <text class="title">营养成分</text>
      <view class="right-btn" @click="showDatePicker">
        <text>{{ selectedDate }}</text>
        <u-icon name="arrow-down" size="24" color="#333"></u-icon>
      </view>
    </view>

    <view class="content"
003e
      <!-- 今日概览卡片 -->
      <view class="overview-card"
>
        <view class="overview-header">
          <text class="overview-title">今日摄入</text>
          <view class="calorie-badge"
>
            <text class="calorie-num">{{ todayIntake.calories }}</text>
            <text class="calorie-unit">千卡</text>
          </view>
        </view>

        <!-- 营养环形图 -->
        <view class="chart-section">
          <view class="nutrition-chart"
>
            <!-- 简化的环形图实现 -->
            <view class="donut-chart"
>
              <view class="donut-ring"></view>
              <view class="donut-segments"
>
                <view 
                  v-for="(segment, index) in chartSegments" 
                  :key="index"
                  class="segment"
                  :style="segment.style"
                ></view>
              </view>
              <view class="donut-center"
>
                <text class="center-label">目标完成度</text>
                <text class="center-value">{{ completionRate }}%</text>
              </view>
            </view>
          </view>

          <!-- 图例 -->
          <view class="chart-legend">
            <view
              v-for="item in nutritionLegend"
              :key="item.name"
              class="legend-item"
            >
              <view class="legend-dot" :style="{ background: item.color }"></view>
              <view class="legend-info"
>
                <text class="legend-name">{{ item.name }}</text>
                <text class="legend-value">{{ item.value }}g</text>
              </view>
              <text class="legend-percent">{{ item.percent }}%</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 三大营养素进度 -->
      <view class="macros-section">
        <text class="section-title">三大营养素</text>
        
        <view class="macro-list"
003e
          <view
            v-for="macro in macros"
            :key="macro.name"
            class="macro-item"
          >
            <view class="macro-header"
003e
              <view class="macro-info">
                <text class="macro-icon">{{ macro.icon }}</text>
                <view class="macro-text">
                  <text class="macro-name">{{ macro.name }}</text>
                  <text class="macro-target">目标 {{ macro.target }}g</text>
                </view>
              </view>
              <text class="macro-current">{{ macro.current }}g</text>
            </view>

            <view class="macro-progress">
              <view class="progress-track"></view>
              <view 
                class="progress-fill"
                :class="macro.status"
                :style="{ width: Math.min(macro.percent, 100) + '%' }"
              ></view>
            </view>

            <text class="macro-hint">{{ macro.hint }}</text>
          </view>
        </view>
      </view>

      <!-- 微量元素 -->
      <view class="micros-section">
        <view class="section-header"
>
          <text class="section-title">微量元素</text>
          <text class="section-more" @click="showAllMicros">查看全部</text>
        </view>

        <view class="micro-grid">
          <view
            v-for="micro in micros"
            :key="micro.name"
            class="micro-item"
            :class="micro.status"
          >
            <view class="micro-icon" :style="{ background: micro.color + '20', color: micro.color }">
              <text>{{ micro.icon }}</text>
            </view>
            <text class="micro-name">{{ micro.name }}</text>
            
            <view class="micro-bar">
              <view class="micro-track"></view>
              <view 
                class="micro-fill"
                :style="{ width: Math.min(micro.percent, 100) + '%', background: micro.color }"
              ></view>
            </view>
            
            <view class="micro-values">
              <text class="micro-current">{{ micro.current }}{{ micro.unit }}</text>
              <text class="micro-target">/{{ micro.target }}{{ micro.unit }}</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 饮食记录 -->
      <view class="records-section">
        <view class="section-header">
          <text class="section-title">饮食记录</text>
          <text class="section-action" @click="addRecord">+ 添加</text>
        </view>

        <view class="meal-list">
          <view
            v-for="meal in meals"
            :key="meal.type"
            class="meal-card"
            @click="showMealDetail(meal)"
          >
            <view class="meal-header">
              <view class="meal-info">
                <text class="meal-icon">{{ meal.icon }}</text>
                <view class="meal-text"
>
                  <text class="meal-name">{{ meal.name }}</text>
                  <text class="meal-time">{{ meal.time }}</text>
                </view>
              </view>
              <text class="meal-calories">{{ meal.calories }}千卡</text>
            </view>

            <view v-if="meal.foods.length" class="meal-foods"
>
              <text 
                v-for="(food, index) in meal.foods" 
                :key="index"
                class="food-tag"
              >
                {{ food.name }}
              </text>
            </view>

            <view v-else class="meal-empty">
              <text>暂无记录</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 摄入建议 -->
      <view class="advice-section"
003e
        <text class="section-title">摄入建议</text>
        
        <view class="advice-list">
          <view
            v-for="advice in advices"
            :key="advice.id"
            class="advice-card"
            :class="advice.type"
          >
            <view class="advice-icon">
              <text>{{ advice.icon }}</text>
            </view>
            
            <view class="advice-content"
003e
              <text class="advice-title">{{ advice.title }}</text>
              <text class="advice-desc">{{ advice.description }}</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 日期选择弹窗 -->
    <u-popup
      v-model:show="datePickerVisible"
      mode="bottom"
      round
    >
      <view class="date-picker-content"
003e
        <view class="picker-header">
          <text>选择日期</text>
        </view>
        
        <picker-view 
          class="date-picker-view"
          :value="dateValue"
          @change="onDatePickerChange"
        >
          <picker-view-column>
            <view v-for="year in years" :key="year" class="picker-item">{{ year }}年</view>
          </picker-view-column>
          <picker-view-column>
            <view v-for="month in months" :key="month" class="picker-item">{{ month }}月</view>
          </picker-view-column>
          <picker-view-column>
            <view v-for="day in days" :key="day" class="picker-item">{{ day }}日</view>
          </picker-view-column>
        </picker-view>
        
        <view class="picker-actions"
>
          <view class="btn-cancel" @click="datePickerVisible = false">取消</view>
          <view class="btn-confirm" @click="confirmDate">确定</view>
        </view>
      </view>
    </u-popup>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import dayjs from 'dayjs'

// 响应式数据
const selectedDate = ref(dayjs().format('MM-DD'))
const datePickerVisible = ref(false)
const dateValue = ref([0, 0, 0])

// 今日摄入数据
const todayIntake = ref({
  calories: 1850,
  carbs: 220,
  protein: 85,
  fat: 65,
  fiber: 18,
  sugar: 45,
  sodium: 1800
})

// 目标数据
const targets = ref({
  calories: 2200,
  carbs: 280,
  protein: 100,
  fat: 75,
  fiber: 25,
  sugar: 50,
  sodium: 2300
})

// 营养图例数据
const nutritionLegend = computed(() => {
  const total = todayIntake.value.carbs * 4 + todayIntake.value.protein * 4 + todayIntake.value.fat * 9
  return [
    { 
      name: '碳水化合物', 
      value: todayIntake.value.carbs, 
      color: '#5B8FF9',
      percent: Math.round(todayIntake.value.carbs * 4 / total * 100)
    },
    { 
      name: '蛋白质', 
      value: todayIntake.value.protein, 
      color: '#52C41A',
      percent: Math.round(todayIntake.value.protein * 4 / total * 100)
    },
    { 
      name: '脂肪', 
      value: todayIntake.value.fat, 
      color: '#FAAD14',
      percent: Math.round(todayIntake.value.fat * 9 / total * 100)
    }
  ]
})

// 环形图段
const chartSegments = computed(() => {
  const total = nutritionLegend.value.reduce((sum, item) => sum + item.percent, 0)
  let currentAngle = 0
  
  return nutritionLegend.value.map(item => {
    const angle = (item.percent / total) * 360
    const style = {
      background: `conic-gradient(${item.color} ${angle}deg, transparent ${angle}deg)`,
      transform: `rotate(${currentAngle}deg)`
    }
    currentAngle += angle
    return { style, color: item.color }
  })
})

// 完成度
const completionRate = computed(() => {
  return Math.round((todayIntake.value.calories / targets.value.calories) * 100)
})

// 三大营养素
const macros = computed(() => {
  const data = [
    {
      name: '碳水化合物',
      icon: '🌾',
      current: todayIntake.value.carbs,
      target: targets.value.carbs,
      unit: 'g'
    },
    {
      name: '蛋白质',
      icon: '🥩',
      current: todayIntake.value.protein,
      target: targets.value.protein,
      unit: 'g'
    },
    {
      name: '脂肪',
      icon: '🥑',
      current: todayIntake.value.fat,
      target: targets.value.fat,
      unit: 'g'
    }
  ]
  
  return data.map(item => {
    const percent = (item.current / item.target) * 100
    let status = 'normal'
    let hint = ''
    
    if (percent < 50) {
      status = 'low'
      hint = '摄入不足，建议增加'
    } else if (percent > 100) {
      status = 'high'
      hint = '摄入超标，注意控制'
    } else {
      hint = '摄入量适中，继续保持'
    }
    
    return { ...item, percent, status, hint }
  })
})

// 微量元素
const micros = ref([
  { name: '膳食纤维', icon: '🥬', current: 18, target: 25, unit: 'g', color: '#52C41A', status: 'normal' },
  { name: '维生素C', icon: '🍊', current: 45, target: 100, unit: 'mg', color: '#FA8C16', status: 'low' },
  { name: '钙', icon: '🥛', current: 520, target: 800, unit: 'mg', color: '#1890FF', status: 'low' },
  { name: '铁', icon: '🥩', current: 12, target: 15, unit: 'mg', color: '#F5222D', status: 'normal' },
  { name: '锌', icon: '🦪', current: 8, target: 12, unit: 'mg', color: '#722ED1', status: 'low' },
  { name: '钠', icon: '🧂', current: 1800, target: 2300, unit: 'mg', color: '#13C2C2', status: 'normal' }
].map(m => ({
  ...m,
  percent: (m.current / m.target) * 100,
  status: m.current < m.target * 0.6 ? 'low' : m.current > m.target ? 'high' : 'normal'
})))

// 饮食记录
const meals = ref([
  {
    type: 'breakfast',
    name: '早餐',
    icon: '🌅',
    time: '08:30',
    calories: 520,
    foods: [
      { name: '全麦面包', calories: 150 },
      { name: '牛奶', calories: 120 },
      { name: '鸡蛋', calories: 140 }
    ]
  },
  {
    type: 'lunch',
    name: '午餐',
    icon: '☀️',
    time: '12:00',
    calories: 680,
    foods: [
      { name: '米饭', calories: 200 },
      { name: '红烧肉', calories: 280 },
      { name: '青菜', calories: 80 }
    ]
  },
  {
    type: 'dinner',
    name: '晚餐',
    icon: '🌙',
    time: '18:30',
    calories: 450,
    foods: [
      { name: '小米粥', calories: 120 },
      { name: '清蒸鱼', calories: 180 },
      { name: '凉拌黄瓜', calories: 50 }
    ]
  },
  {
    type: 'snack',
    name: '加餐',
    icon: '🍿',
    time: '15:30',
    calories: 200,
    foods: [
      { name: '苹果', calories: 80 },
      { name: '坚果', calories: 120 }
    ]
  }
])

// 摄入建议
const advices = ref([
  {
    id: 1,
    type: 'warning',
    icon: '⚠️',
    title: '蛋白质摄入不足',
    description: '今日蛋白质摄入仅达目标的85%，建议晚餐增加瘦肉、鱼类或豆制品。'
  },
  {
    id: 2,
    type: 'tip',
    icon: '💡',
    title: '膳食纤维建议',
    description: '建议多吃蔬菜水果，有助于肠道健康和饱腹感。'
  },
  {
    id: 3,
    type: 'success',
    icon: '✅',
    title: '碳水化合物摄入适中',
    description: '今日碳水摄入控制良好，继续保持！'
  }
])

// 日期选择器数据
const years = Array.from({ length: 5 }, (_, i) => dayjs().year() - 2 + i)
const months = Array.from({ length: 12 }, (_, i) => i + 1)
const days = Array.from({ length: 31 }, (_, i) => i + 1)

// 方法
const showDatePicker = () => {
  const now = dayjs()
  dateValue.value = [
    years.indexOf(now.year()),
    now.month(),
    now.date() - 1
  ]
  datePickerVisible.value = true
}

const onDatePickerChange = (e) => {
  dateValue.value = e.detail.value
}

const confirmDate = () => {
  const [yIndex, mIndex, dIndex] = dateValue.value
  const date = dayjs()
    .year(years[yIndex])
    .month(mIndex)
    .date(dIndex + 1)
  
  selectedDate.value = date.format('MM-DD')
  datePickerVisible.value = false
  
  // 加载该日期的数据
  loadDataForDate(date)
}

const loadDataForDate = (date) => {
  // 模拟加载数据
  console.log('加载日期数据', date.format('YYYY-MM-DD'))
}

const showAllMicros = () => {
  uni.showModal({
    title: '微量元素详情',
    content: '钙、铁、锌、维生素A、B、C、D、E、K...',
    showCancel: false
  })
}

const addRecord = () => {
  uni.showActionSheet({
    itemList: ['早餐', '午餐', '晚餐', '加餐'],
    success: (res) => {
      const mealTypes = ['breakfast', 'lunch', 'dinner', 'snack']
      uni.navigateTo({
        url: `/pages/food/record?type=${mealTypes[res.tapIndex]}`
      })
    }
  })
}

const showMealDetail = (meal) => {
  const foods = meal.foods.map(f => `${f.name} ${f.calories}千卡`).join('\n')
  uni.showModal({
    title: `${meal.name} (${meal.calories}千卡)`,
    content: foods || '暂无记录',
    showCancel: false
  })
}

const goBack = () => {
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
.nutrition-page {
  min-height: 100vh;
  background: #f5f6fa;
}

.nav-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 30rpx;
  padding-top: 60rpx;
  background: #fff;
  border-bottom: 1rpx solid #f0f0f0;

  .back-btn {
    width: 60rpx;
    height: 60rpx;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .title {
    font-size: 34rpx;
    font-weight: 600;
    color: #333;
  }

  .right-btn {
    display: flex;
    align-items: center;
    gap: 8rpx;
    padding: 10rpx 20rpx;
    background: #f5f6fa;
    border-radius: 30rpx;

    text {
      font-size: 26rpx;
      color: #333;
    }
  }
}

.content {
  padding: 30rpx;
  padding-bottom: 50rpx;
}

// 概览卡片
.overview-card {
  background: #fff;
  border-radius: 24rpx;
  padding: 30rpx;
  margin-bottom: 30rpx;

  .overview-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 30rpx;

    .overview-title {
      font-size: 30rpx;
      font-weight: 600;
      color: #333;
    }

    .calorie-badge {
      display: flex;
      align-items: baseline;
      gap: 8rpx;

      .calorie-num {
        font-size: 48rpx;
        font-weight: 700;
        color: #5B8FF9;
      }

      .calorie-unit {
        font-size: 24rpx;
        color: #999;
      }
    }
  }
}

// 图表区域
.chart-section {
  display: flex;
  gap: 40rpx;
}

.nutrition-chart {
  flex-shrink: 0;
}

.donut-chart {
  position: relative;
  width: 280rpx;
  height: 280rpx;

  .donut-ring {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    border-radius: 50%;
    background: #f5f5f5;
  }

  .donut-segments {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    border-radius: 50%;

    .segment {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      border-radius: 50%;
      clip-path: polygon(50% 50%, 50% 0%, 100% 0%, 100% 100%, 0% 100%, 0% 0%, 50% 0%);
    }
  }

  .donut-center {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    width: 180rpx;
    height: 180rpx;
    background: #fff;
    border-radius: 50%;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;

    .center-label {
      font-size: 22rpx;
      color: #999;
    }

    .center-value {
      font-size: 48rpx;
      font-weight: 700;
      color: #5B8FF9;
    }
  }
}

// 图例
.chart-legend {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 20rpx;

  .legend-item {
    display: flex;
    align-items: center;
    gap: 16rpx;

    .legend-dot {
      width: 16rpx;
      height: 16rpx;
      border-radius: 50%;
    }

    .legend-info {
      flex: 1;

      .legend-name {
        font-size: 24rpx;
        color: #666;
        display: block;
      }

      .legend-value {
        font-size: 28rpx;
        font-weight: 600;
        color: #333;
      }
    }

    .legend-percent {
      font-size: 26rpx;
      color: #999;
    }
  }
}

// 三大营养素
.macros-section {
  margin-bottom: 30rpx;

  .section-title {
    font-size: 30rpx;
    font-weight: 600;
    color: #333;
    margin-bottom: 20rpx;
    display: block;
  }
}

.macro-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.macro-item {
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;

  .macro-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16rpx;

    .macro-info {
      display: flex;
      align-items: center;
      gap: 16rpx;

      .macro-icon {
        font-size: 40rpx;
      }

      .macro-text {
        .macro-name {
          font-size: 28rpx;
          font-weight: 500;
          color: #333;
          display: block;
        }

        .macro-target {
          font-size: 22rpx;
          color: #999;
        }
      }
    }

    .macro-current {
      font-size: 32rpx;
      font-weight: 600;
      color: #5B8FF9;
    }
  }

  .macro-progress {
    position: relative;
    height: 16rpx;
    background: #f5f5f5;
    border-radius: 8rpx;
    overflow: hidden;
    margin-bottom: 12rpx;

    .progress-fill {
      height: 100%;
      border-radius: 8rpx;
      transition: all 0.3s;

      &.normal {
        background: #52C41A;
      }

      &.low {
        background: #FAAD14;
      }

      &.high {
        background: #FF4D4F;
      }
    }
  }

  .macro-hint {
    font-size: 22rpx;
    color: #999;
  }
}

// 微量元素
.micros-section {
  margin-bottom: 30rpx;

  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20rpx;

    .section-title {
      font-size: 30rpx;
      font-weight: 600;
      color: #333;
    }

    .section-more {
      font-size: 26rpx;
      color: #5B8FF9;
    }
  }
}

.micro-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16rpx;
}

.micro-item {
  background: #fff;
  border-radius: 16rpx;
  padding: 20rpx;
  text-align: center;

  .micro-icon {
    width: 60rpx;
    height: 60rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 50%;
    font-size: 32rpx;
    margin: 0 auto 12rpx;
  }

  .micro-name {
    font-size: 24rpx;
    color: #333;
    display: block;
    margin-bottom: 12rpx;
  }

  .micro-bar {
    position: relative;
    height: 8rpx;
    background: #f5f5f5;
    border-radius: 4rpx;
    overflow: hidden;
    margin-bottom: 12rpx;

    .micro-fill {
      height: 100%;
      border-radius: 4rpx;
    }
  }

  .micro-values {
    .micro-current {
      font-size: 28rpx;
      font-weight: 600;
      color: #333;
    }

    .micro-target {
      font-size: 22rpx;
      color: #999;
    }
  }

  &.low .micro-current {
    color: #FAAD14;
  }

  &.high .micro-current {
    color: #FF4D4F;
  }
}

// 饮食记录
.records-section {
  margin-bottom: 30rpx;

  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20rpx;

    .section-title {
      font-size: 30rpx;
      font-weight: 600;
      color: #333;
    }

    .section-action {
      font-size: 26rpx;
      color: #5B8FF9;
    }
  }
}

.meal-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.meal-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;

  .meal-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16rpx;

    .meal-info {
      display: flex;
      align-items: center;
      gap: 16rpx;

      .meal-icon {
        font-size: 40rpx;
      }

      .meal-text {
        .meal-name {
          font-size: 28rpx;
          font-weight: 500;
          color: #333;
          display: block;
        }

        .meal-time {
          font-size: 22rpx;
          color: #999;
        }
      }
    }

    .meal-calories {
      font-size: 28rpx;
      font-weight: 600;
      color: #5B8FF9;
    }
  }

  .meal-foods {
    display: flex;
    flex-wrap: wrap;
    gap: 12rpx;

    .food-tag {
      padding: 8rpx 16rpx;
      background: #f0f5ff;
      border-radius: 8rpx;
      font-size: 24rpx;
      color: #5B8FF9;
    }
  }

  .meal-empty {
    padding: 20rpx;
    text-align: center;

    text {
      font-size: 24rpx;
      color: #999;
    }
  }
}

// 摄入建议
.advice-section {
  .section-title {
    font-size: 30rpx;
    font-weight: 600;
    color: #333;
    margin-bottom: 20rpx;
    display: block;
  }
}

.advice-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.advice-card {
  display: flex;
  align-items: flex-start;
  gap: 20rpx;
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  border-left: 6rpx solid transparent;

  .advice-icon {
    font-size: 40rpx;
    flex-shrink: 0;
  }

  .advice-content {
    flex: 1;

    .advice-title {
      font-size: 28rpx;
      font-weight: 600;
      color: #333;
      display: block;
      margin-bottom: 8rpx;
    }

    .advice-desc {
      font-size: 24rpx;
      color: #666;
      line-height: 1.6;
    }
  }

  &.warning {
    border-left-color: #FAAD14;
    background: #FFFBE6;
  }

  &.tip {
    border-left-color: #1890FF;
    background: #E6F7FF;
  }

  &.success {
    border-left-color: #52C41A;
    background: #F6FFED;
  }
}

// 日期选择器
.date-picker-content {
  padding: 30rpx 0;

  .picker-header {
    text-align: center;
    padding: 0 30rpx 20rpx;
    font-size: 32rpx;
    font-weight: 600;
    color: #333;
  }

  .date-picker-view {
    height: 400rpx;

    .picker-item {
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 32rpx;
      color: #333;
    }
  }

  .picker-actions {
    display: flex;
    gap: 20rpx;
    padding: 20rpx 30rpx 0;

    .btn-cancel, .btn-confirm {
      flex: 1;
      padding: 24rpx 0;
      border-radius: 12rpx;
      text-align: center;
      font-size: 30rpx;
    }

    .btn-cancel {
      background: #f5f5f5;
      color: #666;
    }

    .btn-confirm {
      background: #5B8FF9;
      color: #fff;
    }
  }
}
</style>