<template>
  <view class="food-page">
    <!-- 顶部营养概览 -->
    <view class="nutrition-header"
      <view class="header-bg"></view>
      
      <view class="date-selector"
        <u-icon name="arrow-left" size="32" color="#fff" @click="changeDate(-1)"></u-icon>
        <text class="date-text">{{ currentDate }}</text>
        
        <u-icon name="arrow-right" size="32" color="#fff" @click="changeDate(1)"></u-icon>
        
        <text class="today-btn" @click="goToday">今天</text>
      </view>
      
      <view class="calorie-ring"
        <view class="ring-content"
          <text class="calorie-num">{{ todayStats.intake }}</text>
          <text class="calorie-label">已摄入/卡</text>
          <text class="calorie-target">目标 {{ todayStats.target }}</text>
        </view>
        
        <view class="ring-progress" :style="{ background: getRingColor() }"
          <view class="ring-inner"></view>
        </view>
      </view>
      
      <view class="nutrition-bars"
        <view class="nutri-item"
          <text class="nutri-label">蛋白质</text>
          <view class="nutri-bar"
            <view class="bar-fill protein" :style="{ width: todayStats.proteinPercent + '%' }"></view>
          </view>
          <text class="nutri-value">{{ todayStats.protein }}g</text>
        </view>
        
        <view class="nutri-item"
          <text class="nutri-label">碳水</text>
          <view class="nutri-bar"
            <view class="bar-fill carbs" :style="{ width: todayStats.carbsPercent + '%' }"></view>
          </view>
          <text class="nutri-value">{{ todayStats.carbs }}g</text>
        </view>
        
        <view class="nutri-item"
          <text class="nutri-label">脂肪</text>
          <view class="nutri-bar"
            <view class="bar-fill fat" :style="{ width: todayStats.fatPercent + '%' }"></view>
          </view>
          <text class="nutri-value">{{ todayStats.fat }}g</text>
        </view>
      </view>
    </view>
    
    <!-- 快捷记录按钮 -->
    <view class="quick-actions"
      <view class="action-btn" @click="scanFood"
        <view class="btn-icon camera"
          <u-icon name="camera-fill" size="44" color="#fff"></u-icon>
        </view>
        <text>拍照识别</text>
      </view>
      
      <view class="action-btn" @click="voiceRecord"
        <view class="btn-icon voice"
          <u-icon name="mic-fill" size="44" color="#fff"></u-icon>
        </view>
        <text>语音记录</text>
      </view>
      
      <view class="action-btn" @click="manualRecord"
        <view class="btn-icon manual"
          <u-icon name="edit-pen-fill" size="44" color="#fff"></u-icon>
        </view>
        <text>手动记录</text>
      </view>
      
      <view class="action-btn" @click="scanBarcode"
        <view class="btn-icon barcode"
          <u-icon name="grid-fill" size="44" color="#fff"></u-icon>
        </view>
        <text>扫码识别</text>
      </view>
    </view>
    
    <!-- 今日饮食记录 -->
    <view class="diet-records"
      <view class="section-header"
        <text class="section-title">🍽️ 今日饮食</text>
        <text class="total-calories">共 {{ todayStats.intake }} 卡</text>
      </view>
      
      <view class="meal-list"
        <view v-for="meal in meals" :key="meal.type" class="meal-section"
          <view class="meal-header"
            <view class="meal-title"
              <text class="meal-icon">{{ meal.icon }}</text>
              <text class="meal-name">{{ meal.name }}</text>
            </view>
            
            <text class="meal-calories">{{ meal.totalCalories }} 卡</text>
          </view>
          
          <view class="food-items"
            <view 
              v-for="(food, index) in meal.foods" 
              :key="index"
              class="food-item"
              @longpress="deleteFood(meal, index)"
            >
              <image :src="food.image" mode="aspectFill" />
              
              <view class="food-info"
                <text class="food-name">{{ food.name }}</text>
                <text class="food-amount">{{ food.amount }}</text>
              </view>
              
              <text class="food-calories">{{ food.calories }} 卡</text>
            </view>
            
            <view class="add-food" @click="addFood(meal.type)"
              <u-icon name="plus" size="32" color="#5B8FF9"></u-icon>
              <text>添加食物</text>
            </view>
          </view>
        </view>
      </view>
    </view>
    
    <!-- AI饮食建议 -->
    <view class="ai-advice"
      <view class="advice-header"
        <u-icon name="chat-fill" size="32" color="#5B8FF9"></u-icon>
        <text>AI 饮食建议</text>
      </view>
      
      <view class="advice-content"
        <text>{{ aiAdvice }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'

const currentDate = ref('2月21日 周五')

const todayStats = ref({
  intake: 1250,
  target: 2000,
  protein: 45,
  proteinPercent: 60,
  carbs: 120,
  carbsPercent: 40,
  fat: 35,
  fatPercent: 35
})

const meals = ref([
  {
    type: 'breakfast',
    name: '早餐',
    icon: '🌅',
    totalCalories: 380,
    foods: [
      { name: '全麦面包', amount: '2片 60g', calories: 150, image: '/static/food/bread.jpg' },
      { name: '煮鸡蛋', amount: '1个 50g', calories: 70, image: '/static/food/egg.jpg' },
      { name: '牛奶', amount: '1杯 250ml', calories: 160, image: '/static/food/milk.jpg' }
    ]
  },
  {
    type: 'lunch',
    name: '午餐',
    icon: '☀️',
    totalCalories: 580,
    foods: [
      { name: '米饭', amount: '1碗 150g', calories: 174, image: '/static/food/rice.jpg' },
      { name: '番茄炒蛋', amount: '1份 200g', calories: 180, image: '/static/food/egg-tomato.jpg' },
      { name: '红烧肉', amount: '1份 100g', calories: 226, image: '/static/food/pork.jpg' }
    ]
  },
  {
    type: 'dinner',
    name: '晚餐',
    icon: '🌙',
    totalCalories: 0,
    foods: []
  },
  {
    type: 'snack',
    name: '加餐',
    icon: '🍵',
    totalCalories: 290,
    foods: [
      { name: '苹果', amount: '1个 200g', calories: 104, image: '/static/food/apple.jpg' },
      { name: '坚果', amount: '1小把 30g', calories: 186, image: '/static/food/nuts.jpg' }
    ]
  }
])

const aiAdvice = ref('今天蛋白质摄入充足，但蔬菜摄入不足，建议晚餐多吃绿叶蔬菜。碳水摄入略高，可适当减少主食量。')

const getRingColor = () => {
  const percent = todayStats.value.intake / todayStats.value.target
  if (percent < 0.5) return 'conic-gradient(#5AD8A6 0deg, #5AD8A6 ' + (percent * 360) + 'deg, #e0e0e0 ' + (percent * 360) + 'deg)'
  if (percent < 0.8) return 'conic-gradient(#FAAD14 0deg, #FAAD14 ' + (percent * 360) + 'deg, #e0e0e0 ' + (percent * 360) + 'deg)'
  return 'conic-gradient(#FF4D4F 0deg, #FF4D4F ' + (percent * 360) + 'deg, #e0e0e0 ' + (percent * 360) + 'deg)'
}

const changeDate = (delta) => {
  console.log('切换日期:', delta)
}

const goToday = () => {
  currentDate.value = '2月21日 周五'
}

const scanFood = () => {
  uni.navigateTo({ url: '/pages/food/scan' })
}

const voiceRecord = () => {
  uni.showToast({ title: '语音记录开发中', icon: 'none' })
}

const manualRecord = () => {
  uni.navigateTo({ url: '/pages/food/manual' })
}

const scanBarcode = () => {
  uni.scanCode({
    success: (res) => {
      console.log('扫码结果:', res.result)
    }
  })
}

const addFood = (mealType) => {
  uni.navigateTo({ url: `/pages/food/search?mealType=${mealType}` })
}

const deleteFood = (meal, index) => {
  uni.showModal({
    title: '删除记录',
    content: '确定删除这条饮食记录吗？',
    success: (res) => {
      if (res.confirm) {
        meal.foods.splice(index, 1)
        meal.totalCalories = meal.foods.reduce((sum, f) => sum + f.calories, 0)
      }
    }
  })
}
</script>

<style lang="scss" scoped>
.food-page {
  min-height: 100vh;
  background: #f5f6fa;
  padding-bottom: 40rpx;
}

.nutrition-header {
  position: relative;
  padding: 40rpx;
  padding-top: 60rpx;
  
  .header-bg {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 500rpx;
    background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
    border-radius: 0 0 40rpx 40rpx;
  }
  
  .date-selector {
    position: relative;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 40rpx;
    
    .date-text {
      font-size: 32rpx;
      color: #fff;
      font-weight: 500;
      margin: 0 30rpx;
    }
    
    .today-btn {
      position: absolute;
      right: 0;
      font-size: 26rpx;
      color: #fff;
      padding: 8rpx 20rpx;
      background: rgba(255,255,255,0.2);
      border-radius: 20rpx;
    }
  }
  
  .calorie-ring {
    position: relative;
    width: 280rpx;
    height: 280rpx;
    margin: 0 auto 40rpx;
    
    .ring-progress {
      width: 100%;
      height: 100%;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      
      .ring-inner {
        width: 220rpx;
        height: 220rpx;
        background: #fff;
        border-radius: 50%;
      }
    }
    
    .ring-content {
      position: absolute;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      text-align: center;
      z-index: 1;
      
      .calorie-num {
        font-size: 56rpx;
        font-weight: bold;
        color: #333;
        display: block;
      }
      
      .calorie-label {
        font-size: 24rpx;
        color: #999;
        margin-top: 8rpx;
        display: block;
      }
      
      .calorie-target {
        font-size: 22rpx;
        color: #999;
        margin-top: 8rpx;
        display: block;
      }
    }
  }
  
  .nutrition-bars {
    position: relative;
    
    .nutri-item {
      display: flex;
      align-items: center;
      margin-bottom: 16rpx;
      
      &:last-child {
        margin-bottom: 0;
      }
      
      .nutri-label {
        width: 100rpx;
        font-size: 26rpx;
        color: #fff;
      }
      
      .nutri-bar {
        flex: 1;
        height: 16rpx;
        background: rgba(255,255,255,0.3);
        border-radius: 8rpx;
        overflow: hidden;
        margin: 0 20rpx;
        
        .bar-fill {
          height: 100%;
          border-radius: 8rpx;
          
          &.protein {
            background: #FF9F43;
          }
          
          &.carbs {
            background: #FFD93D;
          }
          
          &.fat {
            background: #6BCB77;
          }
        }
      }
      
      .nutri-value {
        width: 80rpx;
        font-size: 26rpx;
        color: #fff;
        text-align: right;
      }
    }
  }
}

.quick-actions {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20rpx;
  padding: 30rpx;
  margin: 0 30rpx;
  background: #fff;
  border-radius: 20rpx;
  margin-top: -40rpx;
  position: relative;
  box-shadow: 0 4rpx 20rpx rgba(0,0,0,0.08);
  
  .action-btn {
    display: flex;
    flex-direction: column;
    align-items: center;
    
    .btn-icon {
      width: 100rpx;
      height: 100rpx;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-bottom: 16rpx;
      
      &.camera {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      }
      
      &.voice {
        background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
      }
      
      &.manual {
        background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
      }
      
      &.barcode {
        background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
      }
    }
    
    text {
      font-size: 24rpx;
      color: #666;
    }
  }
}

.diet-records {
  margin: 30rpx;
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
    
    .total-calories {
      font-size: 28rpx;
      color: #5B8FF9;
      font-weight: 500;
    }
  }
  
  .meal-list {
    .meal-section {
      margin-bottom: 30rpx;
      
      &:last-child {
        margin-bottom: 0;
      }
      
      .meal-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 16rpx;
        
        .meal-title {
          display: flex;
          align-items: center;
          
          .meal-icon {
            font-size: 36rpx;
            margin-right: 12rpx;
          }
          
          .meal-name {
            font-size: 30rpx;
            color: #333;
            font-weight: 500;
          }
        }
        
        .meal-calories {
          font-size: 28rpx;
          color: #5B8FF9;
        }
      }
      
      .food-items {
        padding-left: 48rpx;
        
        .food-item {
          display: flex;
          align-items: center;
          padding: 16rpx 0;
          border-bottom: 2rpx solid #f5f5f5;
          
          &:last-child {
            border-bottom: none;
          }
          
          image {
            width: 80rpx;
            height: 80rpx;
            border-radius: 12rpx;
            margin-right: 20rpx;
          }
          
          .food-info {
            flex: 1;
            
            .food-name {
              font-size: 28rpx;
              color: #333;
              display: block;
            }
            
            .food-amount {
              font-size: 24rpx;
              color: #999;
              margin-top: 4rpx;
            }
          }
          
          .food-calories {
            font-size: 28rpx;
            color: #5B8FF9;
            font-weight: 500;
          }
        }
        
        .add-food {
          display: flex;
          align-items: center;
          justify-content: center;
          padding: 24rpx;
          border: 2rpx dashed #5B8FF9;
          border-radius: 12rpx;
          margin-top: 16rpx;
          
          text {
            margin-left: 12rpx;
            font-size: 28rpx;
            color: #5B8FF9;
          }
        }
      }
    }
  }
}

.ai-advice {
  margin: 0 30rpx;
  padding: 30rpx;
  background: linear-gradient(135deg, #F0F5FF 0%, #E6F7FF 100%);
  border-radius: 20rpx;
  border-left: 6rpx solid #5B8FF9;
  
  .advice-header {
    display: flex;
    align-items: center;
    margin-bottom: 16rpx;
    
    text {
      margin-left: 12rpx;
      font-size: 30rpx;
      font-weight: 600;
      color: #5B8FF9;
    }
  }
  
  .advice-content {
    text {
      font-size: 28rpx;
      color: #666;
      line-height: 1.6;
    }
  }
}
</style>
