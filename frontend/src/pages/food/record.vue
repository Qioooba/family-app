<template>
  <view class="food-record-page">
    <view class="nav-bar">
      <view class="back-btn" @click="goBack">
        <u-icon name="arrow-left" size="40" color="#333"></u-icon>
      </view>
      <text class="title">记录饮食</text>
      <view class="right-btn" @click="saveRecord">
        <text>保存</text>
      </view>
    </view>
    
    <view class="record-container">
      <!-- 摄入统计 -->
      <view class="stats-card">
        <view class="stats-header">
          <text class="date">{{ today }}</text>
          <view class="total-calories">
            <text class="num">{{ todayCalories }}</text>
            <text class="unit">千卡</text>
          </view>
        </view>
        
        <view class="progress-bar">
          <view class="progress-fill" :style="{ width: progressPercent + '%' }"></view>
        </view>
        
        <view class="stats-footer">
          <text>目标 {{ targetCalories }} 千卡</text>
          <text :class="{ 'over': remaining < 0 }">{{ remaining >= 0 ? '剩余 ' + remaining : '超出 ' + Math.abs(remaining) }} 千卡</text>
        </view>
      </view>
      
      <!-- 记录方式选择 -->
      <view class="record-type">
        <view 
          v-for="type in recordTypes" 
          :key="type.value"
          class="type-item"
          :class="{ active: currentType === type.value }"
          @click="currentType = type.value"
        >
          <u-icon :name="type.icon" size="40" :color="currentType === type.value ? '#fff' : '#666'"></u-icon>
          <text>{{ type.label }}</text>
        </view>
      </view>
      
      <!-- 表单区域 -->
      <view class="form-card">
        <!-- 餐别选择 -->
        <view class="meal-selector">
          <view 
            v-for="meal in meals" 
            :key="meal.value"
            class="meal-item"
            :class="{ active: form.meal === meal.value }"
            @click="form.meal = meal.value"
          >
            {{ meal.label }}
          </view>
        </view>
        
        <!-- 拍照识别 -->
        <view v-if="currentType === 'camera'" class="camera-section">
          <view class="camera-area" @click="takePhoto">
            <view v-if="!form.photo" class="camera-placeholder">
              <u-icon name="camera" size="80" color="#ccc"></u-icon>
              <text>点击拍照识别食物</text>
            </view>
            <image v-else :src="form.photo" mode="aspectFill" class="preview-img" />
          </view>
          
          <view v-if="recognizedFood" class="recognized-result">
            <text class="result-title">识别结果</text>
            <view class="food-list">
              <view v-for="(food, index) in recognizedFood" :key="index" class="food-item">
                <text class="name">{{ food.name }}</text>
                <view class="info">
                  <text class="weight">{{ food.weight }}g</text>
                  <text class="calories">{{ food.calories }}卡</text>
                </view>
              </view>
            </view>          
          </view>
        </view>
        
        <!-- 手动输入 -->
        <view v-if="currentType === 'manual'" class="manual-section">
          <view class="input-item">
            <text class="label">食物名称</text>
            <input v-model="form.foodName" placeholder="例如：米饭、鸡蛋" class="input" />
          </view>
          
          <view class="input-item">
            <text class="label">重量(g)</text>
            <input v-model="form.weight" type="digit" placeholder="0" class="input" />
          </view>
          
          <view class="input-item">
            <text class="label">热量(千卡)</text>
            <input v-model="form.calories" type="digit" placeholder="0" class="input" />
          </view>
          
          <view class="quick-foods">
            <text class="section-title">常用食物</text>
            <view class="food-tags">
              <view 
                v-for="food in quickFoods" 
                :key="food.name"
                class="food-tag"
                @click="selectQuickFood(food)"
              >
                {{ food.name }} {{ food.calories }}卡
              </view>
            </view>
          </view>
        </view>
        
        <!-- 语音记录 -->
        <view v-if="currentType === 'voice'" class="voice-section">
          <view class="voice-btn" :class="{ recording: isRecording }" @click="toggleRecording"
003e
            <u-icon :name="isRecording ? 'mic-fill' : 'mic'" size="80" :color="isRecording ? '#fff' : '#5AD8A6'"  ></u-icon>
          </view>
          
          <text class="voice-hint">{{ isRecording ? '正在录音...' : '点击开始语音记录' }}</text>
          
          <view v-if="voiceText" class="voice-result">
            <text class="result-title">识别内容</text>
            <text class="result-text">{{ voiceText }}</text>          
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'

const today = new Date().toLocaleDateString('zh-CN', { month: 'long', day: 'numeric' })
const todayCalories = ref(1250)
const targetCalories = ref(2000)

const remaining = computed(() => targetCalories.value - todayCalories.value)
const progressPercent = computed(() => Math.min((todayCalories.value / targetCalories.value) * 100, 100))

const currentType = ref('camera')

const recordTypes = [
  { label: '拍照', value: 'camera', icon: 'camera' },
  { label: '手动', value: 'manual', icon: 'edit-pen' },
  { label: '语音', value: 'voice', icon: 'mic' }
]

const meals = [
  { label: '早餐', value: 'breakfast' },
  { label: '午餐', value: 'lunch' },
  { label: '晚餐', value: 'dinner' },
  { label: '加餐', value: 'snack' }
]

const form = ref({
  meal: 'breakfast',
  photo: '',
  foodName: '',
  weight: '',
  calories: ''
})

const quickFoods = [
  { name: '米饭(1碗)', calories: 200, weight: 150 },
  { name: '鸡蛋(1个)', calories: 70, weight: 50 },
  { name: '牛奶(1杯)', calories: 150, weight: 250 },
  { name: '苹果(1个)', calories: 95, weight: 180 },
  { name: '香蕉(1根)', calories: 105, weight: 120 },
  { name: '鸡胸肉(100g)', calories: 165, weight: 100 }
]

const recognizedFood = ref(null)
const isRecording = ref(false)
const voiceText = ref('')

const goBack = () => {
  uni.navigateBack()
}

const takePhoto = () => {
  uni.chooseImage({
    count: 1,
    sourceType: ['camera'],
    success: (res) => {
      form.value.photo = res.tempFilePaths[0]
      // 模拟识别结果
      recognizedFood.value = [
        { name: '米饭', weight: 200, calories: 260 },
        { name: '红烧肉', weight: 100, calories: 350 }
      ]
    }
  })
}

const selectQuickFood = (food) => {
  form.value.foodName = food.name
  form.value.weight = food.weight
  form.value.calories = food.calories
}

const toggleRecording = () => {
  isRecording.value = !isRecording.value
  if (isRecording.value) {
    // 模拟语音识别
    setTimeout(() => {
      isRecording.value = false
      voiceText.value = '中午吃了米饭、红烧肉和青菜'
    }, 2000)
  }
}

const saveRecord = () => {
  uni.showToast({ title: '记录成功', icon: 'success' })
  setTimeout(() => uni.navigateBack(), 1500)
}
</script>

<style lang="scss" scoped>
.food-record-page {
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
    padding: 12rpx 30rpx;
    background: #5AD8A6;
    border-radius: 30rpx;
    
    text {
      font-size: 28rpx;
      color: #fff;
    }
  }
}

.record-container {
  padding: 30rpx;
}

.stats-card {
  background: linear-gradient(135deg, #5AD8A6 0%, #5B8FF9 100%);
  border-radius: 20rpx;
  padding: 40rpx;
  margin-bottom: 30rpx;
  
  .stats-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 30rpx;
    
    .date {
      font-size: 28rpx;
      color: rgba(255,255,255,0.9);
    }
    
    .total-calories {
      .num {
        font-size: 56rpx;
        font-weight: bold;
        color: #fff;
      }
      
      .unit {
        font-size: 26rpx;
        color: rgba(255,255,255,0.8);
        margin-left: 8rpx;
      }
    }
  }
  
  .progress-bar {
    height: 12rpx;
    background: rgba(255,255,255,0.3);
    border-radius: 6rpx;
    overflow: hidden;
    margin-bottom: 20rpx;
    
    .progress-fill {
      height: 100%;
      background: #fff;
      border-radius: 6rpx;
      transition: width 0.3s;
    }
  }
  
  .stats-footer {
    display: flex;
    justify-content: space-between;
    font-size: 24rpx;
    color: rgba(255,255,255,0.9);
    
    .over {
      color: #FFE4E1;
    }
  }
}

.record-type {
  display: flex;
  justify-content: space-around;
  background: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
  
  .type-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 12rpx;
    padding: 20rpx 40rpx;
    border-radius: 16rpx;
    
    text {
      font-size: 26rpx;
      color: #666;
    }
    
    &.active {
      background: #5AD8A6;
      
      text {
        color: #fff;
      }
    }
  }
}

.form-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
}

.meal-selector {
  display: flex;
  gap: 16rpx;
  margin-bottom: 30rpx;
  
  .meal-item {
    flex: 1;
    text-align: center;
    padding: 20rpx 0;
    background: #f5f5f5;
    border-radius: 12rpx;
    font-size: 26rpx;
    color: #666;
    
    &.active {
      background: #5AD8A6;
      color: #fff;
    }
  }
}

.camera-section {
  .camera-area {
    height: 400rpx;
    background: #f9f9f9;
    border-radius: 16rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    overflow: hidden;
    
    .camera-placeholder {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 20rpx;
      
      text {
        font-size: 28rpx;
        color: #999;
      }
    }
    
    .preview-img {
      width: 100%;
      height: 100%;
    }
  }
}

.recognized-result {
  margin-top: 30rpx;
  
  .result-title {
    font-size: 28rpx;
    font-weight: 600;
    color: #333;
    display: block;
    margin-bottom: 20rpx;
  }
  
  .food-list {
    display: flex;
    flex-direction: column;
    gap: 16rpx;
    
    .food-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 20rpx;
      background: #f9f9f9;
      border-radius: 12rpx;
      
      .name {
        font-size: 28rpx;
        color: #333;
      }
      
      .info {
        display: flex;
        gap: 20rpx;
        
        .weight {
          font-size: 24rpx;
          color: #999;
        }
        
        .calories {
          font-size: 28rpx;
          color: #5AD8A6;
          font-weight: 600;
        }
      }
    }
  }
}

.manual-section {
  .input-item {
    margin-bottom: 30rpx;
    
    .label {
      font-size: 26rpx;
      color: #666;
      display: block;
      margin-bottom: 16rpx;
    }
    
    .input {
      height: 80rpx;
      background: #f5f5f5;
      border-radius: 12rpx;
      padding: 0 24rpx;
      font-size: 28rpx;
    }
  }
  
  .quick-foods {
    margin-top: 40rpx;
    
    .section-title {
      font-size: 28rpx;
      font-weight: 600;
      color: #333;
      display: block;
      margin-bottom: 20rpx;
    }
    
    .food-tags {
      display: flex;
      flex-wrap: wrap;
      gap: 16rpx;
      
      .food-tag {
        padding: 16rpx 24rpx;
        background: #E6F7FF;
        border-radius: 30rpx;
        font-size: 24rpx;
        color: #1890FF;
      }
    }
  }
}

.voice-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 60rpx 0;
  
  .voice-btn {
    width: 180rpx;
    height: 180rpx;
    border-radius: 50%;
    background: #E6F7FF;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.3s;
    
    &.recording {
      background: #FF4D4F;
      animation: pulse 1s infinite;
    }
  }
  
  .voice-hint {
    margin-top: 30rpx;
    font-size: 28rpx;
    color: #666;
  }
  
  .voice-result {
    width: 100%;
    margin-top: 40rpx;
    padding: 30rpx;
    background: #f9f9f9;
    border-radius: 16rpx;
    
    .result-title {
      font-size: 26rpx;
      color: #999;
      display: block;
      margin-bottom: 16rpx;
    }
    
    .result-text {
      font-size: 30rpx;
      color: #333;
      line-height: 1.6;
    }
  }
}

@keyframes pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.05); }
}
</style>