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

      <!-- 体重快捷入口 -->
      <view class="weight-entry" @click="showWeightModal">
        <view class="weight-info">
          <u-icon name="edit-pen" size="36" color="#5AD8A6"></u-icon>
          <view class="weight-text">
            <text class="label">今日体重</text>
            <text class="value">{{ latestWeight ? latestWeight + ' kg' : '未记录' }}</text>
          </view>
        </view>
        <u-icon name="arrow-right" size="32" color="#ccc"></u-icon>
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
          <view class="voice-btn" :class="{ recording: isRecording }" @click="toggleRecording">
            <u-icon :name="isRecording ? 'mic-fill' : 'mic'" size="80" :color="isRecording ? '#fff' : '#5AD8A6'"></u-icon>
          </view>
          
          <text class="voice-hint">{{ isRecording ? '正在录音...' : '点击开始语音记录' }}</text>
          
          <view v-if="voiceText" class="voice-result">
            <text class="result-title">识别内容</text>
            <text class="result-text">{{ voiceText }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 体重记录弹窗 -->
    <view v-if="weightModalVisible" class="modal-overlay" @click="closeWeightModal">
      <view class="modal-content weight-modal" @click.stop>
        <view class="modal-header">
          <text>记录体重</text>
          <text class="close-btn" @click="closeWeightModal">✕</text>
        </view>

        <!-- 体重输入 -->
        <view class="weight-input-section">
          <view class="weight-display">
            <input 
              v-model="weightInput" 
              type="digit" 
              class="weight-number"
              placeholder="0.0"
              maxlength="5"
            />
            <text class="weight-unit">kg</text>
          </view>

          <!-- 快速选择 -->
          <view class="quick-weights">
            <view 
              v-for="w in quickWeights" 
              :key="w"
              class="quick-weight-item"
              :class="{ active: weightInput === w.toString() }"
              @click="weightInput = w.toString()"
            >
              {{ w }}
            </view>
          </view>
        </view>

        <!-- 备注 -->
        <view class="form-item">
          <text class="label">备注（选填）</text>
          <input v-model="weightNote" class="input" placeholder="例如：空腹、运动后..." />
        </view>

        <!-- 历史趋势图 -->
        <view class="weight-chart-section" v-if="weightHistory.length > 1">
          <text class="chart-title">近7天趋势</text>
          <view class="chart-container">
            <view class="chart-line">
              <view 
                v-for="(point, index) in chartPoints" 
                :key="index"
                class="chart-point"
                :style="{ 
                  left: point.x + '%', 
                  bottom: point.y + '%',
                  background: index === chartPoints.length - 1 ? '#5AD8A6' : '#ccc'
                }"
              >
                <view class="point-tooltip">{{ point.weight }}</view>
              </view>
              <view class="chart-line-svg" v-if="chartPoints.length > 1">
                <svg viewBox="0 0 100 100" preserveAspectRatio="none">
                  <polyline 
                    :points="chartPath" 
                    fill="none" 
                    stroke="#5AD8A6" 
                    stroke-width="2"
                  />
                </svg>
              </view>
            </view>
            <view class="chart-labels">
              <text v-for="(item, index) in chartLabels" :key="index" class="chart-label">
                {{ item }}
              </text>
            </view>
          </view>
        </view>

        <!-- 历史列表 -->
        <view class="weight-history-section" v-if="weightHistory.length > 0">
          <text class="history-title">历史记录</text>
          <scroll-view scroll-y class="history-list">
            <view 
              v-for="(item, index) in weightHistory.slice(0, 10)" 
              :key="item.id || index"
              class="history-item"
            >
              <view class="history-left">
                <text class="history-weight">{{ item.weight }} kg</text>
                <text v-if="item.note" class="history-note">{{ item.note }}</text>
              </view>
              <text class="history-date">{{ formatDate(item.recordDate) }}</text>
            </view>
          </scroll-view>
        </view>

        <view class="modal-actions">
          <button class="btn-cancel" @click="closeWeightModal">取消</button>
          <button class="btn-confirm" @click="saveWeight">保存</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { dietApi } from '../../api/index.js'

const today = new Date().toLocaleDateString('zh-CN', { month: 'long', day: 'numeric' })
const todayCalories = ref(1250)
const targetCalories = ref(2000)

// 体重相关数据
const weightModalVisible = ref(false)
const weightInput = ref('')
const weightNote = ref('')
const latestWeight = ref(null)
const weightHistory = ref([])
const quickWeights = [45, 50, 55, 60, 65, 70, 75, 80]

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

onMounted(() => {
  loadLatestWeight()
})

// 加载最新体重
const loadLatestWeight = async () => {
  try {
    const res = await dietApi.getLatestWeight()
    if (res) {
      latestWeight.value = res.weight
    }
  } catch (e) {
    console.error('加载最新体重失败', e)
  }
}

// 加载体重历史
const loadWeightHistory = async () => {
  try {
    const res = await dietApi.getWeightHistory()
    weightHistory.value = res || []
  } catch (e) {
    console.error('加载体重历史失败', e)
  }
}

// 显示体重弹窗
const showWeightModal = async () => {
  weightModalVisible.value = true
  weightInput.value = ''
  weightNote.value = ''
  await loadWeightHistory()
}

// 关闭体重弹窗
const closeWeightModal = () => {
  weightModalVisible.value = false
}

// 保存体重
const saveWeight = async () => {
  const weight = parseFloat(weightInput.value)
  if (!weight || weight <= 0) {
    uni.showToast({ title: '请输入有效的体重', icon: 'none' })
    return
  }
  
  try {
    await dietApi.recordWeight({
      weight: weight,
      note: weightNote.value
    })
    
    latestWeight.value = weight
    uni.showToast({ title: '记录成功', icon: 'success' })
    closeWeightModal()
    await loadWeightHistory()
  } catch (e) {
    console.error('记录体重失败', e)
    uni.showToast({ title: '记录失败', icon: 'none' })
  }
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getMonth() + 1}/${date.getDate()}`
}

// 计算趋势图数据
const chartPoints = computed(() => {
  if (weightHistory.value.length < 2) return []
  
  const recent7 = weightHistory.value.slice(0, 7).reverse()
  const weights = recent7.map(item => parseFloat(item.weight))
  const min = Math.min(...weights) - 1
  const max = Math.max(...weights) + 1
  const range = max - min || 1
  
  return recent7.map((item, index) => ({
    x: (index / (recent7.length - 1)) * 80 + 10,
    y: ((parseFloat(item.weight) - min) / range) * 70 + 15,
    weight: item.weight
  }))
})

// 计算趋势图路径
const chartPath = computed(() => {
  return chartPoints.value.map(p => `${p.x},${100 - p.y}`).join(' ')
})

// 图表标签
const chartLabels = computed(() => {
  const recent7 = weightHistory.value.slice(0, 7).reverse()
  return recent7.map(item => formatDate(item.recordDate))
})

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

// 体重入口样式
.weight-entry {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
  padding: 30rpx;
  border-radius: 16rpx;
  margin-bottom: 20rpx;
  
  .weight-info {
    display: flex;
    align-items: center;
    gap: 20rpx;
    
    .weight-text {
      display: flex;
      flex-direction: column;
      
      .label {
        font-size: 24rpx;
        color: #999;
      }
      
      .value {
        font-size: 32rpx;
        font-weight: 600;
        color: #333;
      }
    }
  }
  
  &:active {
    background: #f9f9f9;
  }
}

// 体重弹窗样式
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: flex-end;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  width: 100%;
  background: #fff;
  border-radius: 30rpx 30rpx 0 0;
  padding: 40rpx 30rpx;
  max-height: 85vh;
  overflow-y: auto;
}

.weight-modal {
  .modal-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 40rpx;
    
    text {
      font-size: 36rpx;
      font-weight: 600;
      color: #333;
    }
    
    .close-btn {
      font-size: 40rpx;
      color: #999;
      padding: 10rpx;
    }
  }
  
  .weight-input-section {
    text-align: center;
    margin-bottom: 40rpx;
    
    .weight-display {
      display: flex;
      align-items: baseline;
      justify-content: center;
      gap: 10rpx;
      margin-bottom: 30rpx;
      
      .weight-number {
        font-size: 80rpx;
        font-weight: bold;
        color: #333;
        width: 250rpx;
        text-align: center;
        border: none;
        background: transparent;
      }
      
      .weight-unit {
        font-size: 40rpx;
        color: #999;
      }
    }
    
    .quick-weights {
      display: flex;
      flex-wrap: wrap;
      justify-content: center;
      gap: 16rpx;
      
      .quick-weight-item {
        padding: 16rpx 30rpx;
        background: #f5f5f5;
        border-radius: 30rpx;
        font-size: 28rpx;
        color: #666;
        
        &.active {
          background: #5AD8A6;
          color: #fff;
        }
      }
    }
  }
  
  .form-item {
    margin-bottom: 30rpx;
    
    .label {
      display: block;
      font-size: 26rpx;
      color: #666;
      margin-bottom: 16rpx;
    }
    
    .input {
      width: 100%;
      height: 80rpx;
      background: #f5f5f5;
      border-radius: 12rpx;
      padding: 0 24rpx;
      font-size: 28rpx;
    }
  }
  
  .weight-chart-section {
    margin-bottom: 30rpx;
    
    .chart-title {
      display: block;
      font-size: 28rpx;
      font-weight: 600;
      color: #333;
      margin-bottom: 20rpx;
    }
    
    .chart-container {
      background: #f9f9f9;
      border-radius: 16rpx;
      padding: 30rpx 20rpx;
      
      .chart-line {
        position: relative;
        height: 200rpx;
        margin-bottom: 20rpx;
        
        .chart-point {
          position: absolute;
          width: 16rpx;
          height: 16rpx;
          border-radius: 50%;
          transform: translate(-50%, 50%);
          
          .point-tooltip {
            position: absolute;
            bottom: 20rpx;
            left: 50%;
            transform: translateX(-50%);
            font-size: 20rpx;
            color: #666;
            white-space: nowrap;
          }
        }
        
        .chart-line-svg {
          position: absolute;
          top: 0;
          left: 0;
          right: 0;
          bottom: 30rpx;
          
          svg {
            width: 100%;
            height: 100%;
          }
        }
      }
      
      .chart-labels {
        display: flex;
        justify-content: space-between;
        
        .chart-label {
          font-size: 20rpx;
          color: #999;
        }
      }
    }
  }
  
  .weight-history-section {
    max-height: 300rpx;
    margin-bottom: 30rpx;
    
    .history-title {
      display: block;
      font-size: 28rpx;
      font-weight: 600;
      color: #333;
      margin-bottom: 20rpx;
    }
    
    .history-list {
      max-height: 250rpx;
      
      .history-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 20rpx 0;
        border-bottom: 1rpx solid #f0f0f0;
        
        &:last-child {
          border-bottom: none;
        }
        
        .history-left {
          display: flex;
          flex-direction: column;
          
          .history-weight {
            font-size: 30rpx;
            color: #333;
            font-weight: 500;
          }
          
          .history-note {
            font-size: 24rpx;
            color: #999;
            margin-top: 4rpx;
          }
        }
        
        .history-date {
          font-size: 24rpx;
          color: #999;
        }
      }
    }
  }
  
  .modal-actions {
    display: flex;
    gap: 20rpx;
    
    button {
      flex: 1;
      height: 90rpx;
      border-radius: 45rpx;
      font-size: 30rpx;
      border: none;
    }
    
    .btn-cancel {
      background: #f5f5f5;
      color: #666;
    }
    
    .btn-confirm {
      background: #5AD8A6;
      color: #fff;
    }
  }
}
</style>