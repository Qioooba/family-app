<template>
  <view class="page-container">
    <view class="header">
      <view class="header-title">饮食记录 🥗</view>
      <view class="header-actions">
        <view class="action-btn" @click="showWeightModal">⚖️</view>
        <view class="action-btn primary" @click="showAddModal">+</view>
      </view>
    </view>
    
    <!-- 今日摄入 + 体重卡片 -->
    <view class="summary-cards">
      <view class="summary-card calories">
        <view class="card-title">今日摄入</view>
        <view class="card-value">{{ todayCalories }}</view>
        <view class="card-unit">千卡 / 目标 2000</view>
        <view class="mini-progress">
          <view class="mini-fill" :style="{ width: Math.min(todayCalories/2000*100, 100) + '%' }"></view>
        </view>
      </view>
      
      <view class="summary-card weight" @click="showWeightHistory">
        <view class="card-title">当前体重</view>
        <view class="card-value">{{ latestWeight || '--' }}</view>
        <view class="card-unit">{{ latestWeight ? 'kg' : '未记录' }}</view>
        <view v-if="weightChange" class="weight-change" :class="{ down: weightChange < 0 }">
          <text>{{ weightChange > 0 ? '↑' : '↓' }} {{ Math.abs(weightChange) }}kg</text>
        </view>
      </view>
    </view>
    
    <!-- 营养分析卡片 -->
    <view class="nutrition-card">
      <view class="card-header">
        <text>今日营养</text>
        <text class="more" @click="showNutritionDetail">详情 ›</text>
      </view>
      
      <view class="nutrition-bars">
        <view v-for="(item, index) in nutritionData" :key="index" class="nutrition-item">
          <view class="nutrition-label">
            <text>{{ item.name }}</text>
            <text class="value">{{ item.current }}/{{ item.target }}{{ item.unit }}</text>
          </view>
          
          <view class="nutrition-bar">
            <view class="nutrition-fill" :style="{ width: Math.min(item.current/item.target*100, 100) + '%', background: item.color }"></view>
          </view>
        </view>
      </view>
    </view>
    
    <!-- 餐次标签 -->
    <view class="meal-tabs">
      <view 
        v-for="(meal, index) in meals" 
        :key="index"
        class="meal-tab"
        :class="{ active: currentMeal === index }"
        @click="currentMeal = index"
      >
        <text class="meal-name">{{ meal.name }}</text>
        <text class="meal-cal">{{ meal.calories }}kcal</text>
      </view>
    </view>
    
    <!-- 食物列表 -->
    <scroll-view class="food-list" scroll-y>
      <view 
        v-for="(food, index) in currentFoods" 
        :key="index"
        class="food-card"
      >
        <image class="food-image" :src="food.image" mode="aspectFill" />
        
        <view class="food-info">
          <view class="food-name">{{ food.name }}</view>
          <view class="food-quantity">{{ food.quantity }}{{ food.unit }}</view>
        </view>
        
        <view class="food-calories">
          <text>{{ food.calories }}</text>
          <text class="unit">千卡</text>
        </view>
        
        <view class="food-delete" @click="deleteFood(index)">
          <text>🗑️</text>
        </view>
      </view>
      
      <view v-if="currentFoods.length === 0" class="empty-tip">
        <text>暂无记录，点击右上角添加</text>
      </view>
    </scroll-view>
    
    
    <!-- AI识别按钮 -->
    <view class="ai-recognize" @click="aiRecognize">
      <text class="ai-icon">📷</text>
      <text>AI拍照识别食物</text>
    </view>
    
    
    <!-- 体重记录弹窗 -->
    <view v-if="showWeightDlg" class="modal-overlay" @click="closeWeightModal">
      <view class="modal-content" @click.stop>
        <view class="modal-header">
          <text>记录体重</text>
          <text class="close-btn" @click="closeWeightModal">✕</text>
        </view>
        
        <view class="weight-input-section">
          <view class="weight-display">
            <input 
              type="digit" 
              v-model="newWeight" 
              class="weight-input" 
              placeholder="0.0"
            />
            <text class="weight-unit">kg</text>
          </view>
          
          <view class="weight-slider">
            <slider :value="sliderValue" @change="onWeightChange" min="30" max="150" step="0.1" />
          </view>
          
          
          <view class="weight-note">
            <input 
              v-model="weightNote" 
              placeholder="添加备注（如：早起空腹）" 
              class="note-input"
            />
          </view>
        </view>
        
        
        <view class="form-actions">
          <button class="btn-cancel" @click="closeWeightModal">取消</button>
          <button class="btn-confirm" @click="saveWeight">保存</button>
        </view>
        
        
        <!-- 最近记录 -->
        <view v-if="weightHistory.length > 0" class="recent-weights">
          <view class="recent-title">最近记录</view>
          
          <view class="weight-trend-mini">
            <view 
              v-for="(w, idx) in weightHistory.slice(0, 5)" 
              :key="idx"
              class="trend-point"
              :style="{ height: calculateTrendHeight(w.weight) }"
            ></view>
          </view>
        </view>
      </view>
    </view>
    
    
    <!-- 体重历史弹窗 -->
    <view v-if="showHistoryDlg" class="modal-overlay" @click="closeHistoryModal"
003e
      <view class="modal-content history-modal" @click.stop>
        <view class="modal-header">
          <text>体重趋势</text>
          <text class="close-btn" @click="closeHistoryModal">✕</text>
        </view>
        
        <view class="weight-chart">
          <view class="chart-placeholder">
            <text>📈 体重变化曲线</text>
            <text class="sub-text">{{ weightHistory.length }} 条记录</text>
          </view>
        </view>
        
        
        <scroll-view class="history-list" scroll-y>
          <view 
            v-for="(w, idx) in weightHistory" 
            :key="idx"
            class="history-item"
          >
            <view class="history-date">
              <text>{{ w.date }}</text>
              <text v-if="w.note" class="history-note">{{ w.note }}</text>
            </view>
            
            <view class="history-weight">
              <text>{{ w.weight }}kg</text>
            </view>
          </view>
        </scroll-view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'

const meals = [
  { name: '早餐', calories: 450 },
  { name: '午餐', calories: 680 },
  { name: '晚餐', calories: 520 },
  { name: '加餐', calories: 120 }
]
const currentMeal = ref(0)

// 体重相关
const latestWeight = ref(70.5)
const weightHistory = ref([
  { date: '2025-02-21', weight: 70.5, note: '早起空腹' },
  { date: '2025-02-20', weight: 70.8, note: '' },
  { date: '2025-02-19', weight: 71.0, note: '' },
  { date: '2025-02-18', weight: 71.2, note: '运动后' },
  { date: '2025-02-17', weight: 71.5, note: '' }
])
const showWeightDlg = ref(false)
const showHistoryDlg = ref(false)
const newWeight = ref('')
const weightNote = ref('')

const weightChange = computed(() => {
  if (weightHistory.value.length < 2) return 0
  return (weightHistory.value[0].weight - weightHistory.value[1].weight).toFixed(1)
})

const sliderValue = computed(() => {
  return parseFloat(newWeight.value) || 70
})

const todayCalories = computed(() => {
  return meals.reduce((sum, m) => sum + m.calories, 0)
})

const nutritionData = ref([
  { name: '蛋白质', current: 45, target: 60, unit: 'g', color: '#4CAF50' },
  { name: '碳水化合物', current: 180, target: 250, unit: 'g', color: '#FF9800' },
  { name: '脂肪', current: 55, target: 65, unit: 'g', color: '#F44336' },
  { name: '膳食纤维', current: 18, target: 25, unit: 'g', color: '#9C27B0' }
])

const foods = ref([
  [
    { name: '牛奶', quantity: 250, unit: 'ml', calories: 150, image: '/static/food/milk.png' },
    { name: '全麦面包', quantity: 2, unit: '片', calories: 180, image: '/static/food/bread.png' }
  ],
  [
    { name: '米饭', quantity: 1, unit: '碗', calories: 230, image: '/static/food/rice.png' },
    { name: '番茄炒蛋', quantity: 1, unit: '份', calories: 180, image: '/static/food/egg.png' }
  ],
  [],
  [
    { name: '苹果', quantity: 1, unit: '个', calories: 80, image: '/static/food/apple.png' }
  ]
])

const currentFoods = computed(() => {
  return foods.value[currentMeal.value] || []
})

onMounted(() => {
  // 加载体重历史
  loadWeightHistory()
})

const loadWeightHistory = () => {
  // 这里可以调用API加载历史数据
  // uni.request({ url: '/api/diet/weight/history', ... })
}

const onWeightChange = (e) => {
  newWeight.value = e.detail.value.toFixed(1)
}

const calculateTrendHeight = (weight) => {
  const min = Math.min(...weightHistory.value.map(w => w.weight))
  const max = Math.max(...weightHistory.value.map(w => w.weight))
  const range = max - min || 1
  return Math.round(((weight - min) / range) * 100) + '%'
}

const showWeightModal = () => {
  newWeight.value = latestWeight.value.toString()
  weightNote.value = ''
  showWeightDlg.value = true
}

const closeWeightModal = () => {
  showWeightDlg.value = false
}

const saveWeight = () => {
  const weight = parseFloat(newWeight.value)
  if (!weight || weight <= 0) {
    uni.showToast({ title: '请输入有效体重', icon: 'none' })
    return
  }
  
  const record = {
    date: new Date().toISOString().split('T')[0],
    weight: weight,
    note: weightNote.value
  }
  
  weightHistory.value.unshift(record)
  latestWeight.value = weight
  
  // 调用API保存
  // uni.request({
  //   url: '/api/diet/weight/record',
  //   method: 'POST',
  //   data: { weight, note: weightNote.value }
  // })
  
  uni.showToast({ title: '记录成功', icon: 'success' })
  closeWeightModal()
}

const showWeightHistory = () => {
  showHistoryDlg.value = true
}

const closeHistoryModal = () => {
  showHistoryDlg.value = false
}

const showNutritionDetail = () => {
  uni.showModal({
    title: '今日营养详情',
    content: `蛋白质：${nutritionData.value[0].current}/${nutritionData.value[0].target}g\n碳水化合物：${nutritionData.value[1].current}/${nutritionData.value[1].target}g\n脂肪：${nutritionData.value[2].current}/${nutritionData.value[2].target}g\n膳食纤维：${nutritionData.value[3].current}/${nutritionData.value[3].target}g`,
    showCancel: false
  })
}

const showAddModal = () => {
  uni.showActionSheet({
    itemList: ['拍照识别', '手动输入', '语音输入'],
    success: (res) => {
      if (res.tapIndex === 0) aiRecognize()
      else if (res.tapIndex === 1) manualInput()
    }
  })
}

const manualInput = () => {
  uni.showModal({
    title: '添加食物',
    editable: true,
    placeholderText: '输入食物名称...',
    success: (res) => {
      if (res.confirm && res.content) {
        foods.value[currentMeal.value].push({
          name: res.content,
          quantity: 1,
          unit: '份',
          calories: 200,
          image: '/static/food/default.png'
        })
        uni.showToast({ title: '添加成功', icon: 'success' })
      }
    }
  })
}

const deleteFood = (index) => {
  uni.showModal({
    title: '确认删除',
    content: '确定要删除这条记录吗？',
    success: (res) => {
      if (res.confirm) {
        foods.value[currentMeal.value].splice(index, 1)
        uni.showToast({ title: '已删除', icon: 'success' })
      }
    }
  })
}

const aiRecognize = () => {
  uni.chooseImage({
    count: 1,
    success: () => {
      uni.showLoading({ title: '识别中...' })
      setTimeout(() => {
        uni.hideLoading()
        uni.showModal({
          title: '识别结果',
          content: '识别到：番茄炒蛋\n预估热量：180千卡',
          showCancel: true,
          confirmText: '添加记录',
          success: (res) => {
            if (res.confirm) {
              foods.value[currentMeal.value].push({
                name: '番茄炒蛋',
                quantity: 1,
                unit: '份',
                calories: 180,
                image: '/static/food/egg.png'
              })
              uni.showToast({ title: '添加成功', icon: 'success' })
            }
          }
        })
      }, 1500)
    }
  })
}
</script>

<style lang="scss" scoped>
.page-container {
  min-height: 100vh;
  background: #F5F7FA;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 50px 20px 20px;
  background: linear-gradient(135deg, #E8F5E9, #C8E6C9);
  
  .header-title {
    font-size: 24px;
    font-weight: 700;
    color: #2C3E50;
  }
  
  .header-actions {
    display: flex;
    gap: 10px;
    
    .action-btn {
      width: 44px;
      height: 44px;
      background: rgba(255,255,255,0.8);
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 20px;
      
      &.primary {
        background: #4CAF50;
        font-size: 28px;
        color: #fff;
        font-weight: 300;
      }
    }
  }
}

.summary-cards {
  display: flex;
  gap: 12px;
  padding: 15px;
  
  .summary-card {
    flex: 1;
    padding: 20px;
    border-radius: 16pxn    text-align: center;
    
    &.calories {
      background: linear-gradient(135deg, #FF9800, #FFB74D);
      color: #fff;
    }
    
    &.weight {
      background: #fff;
      border: 2px solid #E0E6ED;
    }
    
    .card-title {
      font-size: 13px;
      opacity: 0.9;
      margin-bottom: 8px;
    }
    
    .card-value {
      font-size: 36px;
      font-weight: 700;
      margin-bottom: 4px;
    }
    
    .card-unit {
      font-size: 12px;
      opacity: 0.8;
    }
    
    .mini-progress {
      height: 6px;
      background: rgba(255,255,255,0.3);
      border-radius: 3px;
      margin-top: 10px;
      overflow: hidden;
      
      .mini-fill {
        height: 100%;
        background: #fff;
        border-radius: 3px;
      }
    }
    
    .weight-change {
      margin-top: 8px;
      padding: 4px 10px;
      background: #E8F5E9;
      border-radius: 12px;
      font-size: 12px;
      color: #4CAF50;
      display: inline-block;
      
      &.down {
        background: #FFEBEE;
        color: #F44336;
      }
    }
  }
}

.nutrition-card {
  margin: 0 15px 15px;
  padding: 20px;
  background: #fff;
  border-radius: 16px;
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 15px;
    
    text {
      font-size: 16px;
      font-weight: 600;
      color: #2C3E50;
    }
    
    .more {
      font-size: 13px;
      color: #4CAF50;
    }
  }
}

.nutrition-bars {
  .nutrition-item {
    margin-bottom: 12px;
    
    .nutrition-label {
      display: flex;
      justify-content: space-between;
      margin-bottom: 6px;
      font-size: 13px;
      color: #666;
      
      .value {
        color: #333;
        font-weight: 500;
      }
    }
    
    .nutrition-bar {
      height: 8px;
      background: #E0E6ED;
      border-radius: 4px;
      overflow: hidden;
      
      .nutrition-fill {
        height: 100%;
        border-radius: 4px;
        transition: width 0.3s;
      }
    }
  }
}

.meal-tabs {
  display: flex;
  background: #fff;
  padding: 0 15px 15px;
  
  .meal-tab {
    flex: 1;
    text-align: center;
    padding: 12px;
    border-radius: 12px;
    
    .meal-name {
      display: block;
      font-size: 14px;
      color: #2C3E50;
      margin-bottom: 4px;
    }
    
    .meal-cal {
      font-size: 12px;
      color: #7F8C8D;
    }
    
    &.active {
      background: #E8F5E9;
      
      .meal-name {
        color: #4CAF50;
        font-weight: 600;
      }
    }
  }
}

.food-list {
  padding: 15px;
  height: calc(100vh - 480px);
}

.food-card {
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 16px;
  padding: 12px;
  margin-bottom: 10px;
  
  .food-image {
    width: 60px;
    height: 60px;
    border-radius: 12px;
    margin-right: 12px;
  }
  
  .food-info {
    flex: 1;
    
    .food-name {
      font-size: 15px;
      font-weight: 500;
      color: #2C3E50;
      margin-bottom: 4px;
    }
    
    .food-quantity {
      font-size: 12px;
      color: #7F8C8D;
    }
  }
  
  .food-calories {
    text-align: right;
    margin-right: 12px;
    
    text {
      display: block;
      font-size: 18px;
      font-weight: 600;
      color: #4CAF50;
    }
    
    .unit {
      font-size: 11px;
      color: #7F8C8D;
    }
  }
  
  .food-delete {
    padding: 8px;
  }
}

.empty-tip {
  text-align: center;
  padding: 40px;
  color: #7F8C8D;
  font-size: 14px;
}

.ai-recognize {
  position: fixed;
  bottom: 30px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 14px 28px;
  background: linear-gradient(135deg, #4CAF50, #81C784);
  border-radius: 28px;
  color: #fff;
  font-size: 15px;
  box-shadow: 0 8px 24px rgba(76,175,80,0.4);
  
  .ai-icon {
    font-size: 20px;
  }
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  width: 85%;
  background: #fff;
  border-radius: 20px;
  padding: 20px;
  max-height: 80vh;
  overflow-y: auto;
  
  &.history-modal {
    height: 70vh;
  }
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  font-size: 18px;
  font-weight: 600;
  
  .close-btn {
    font-size: 20px;
    color: #7F8C8D;
  }
}

.weight-input-section {
  .weight-display {
    display: flex;
    align-items: baseline;
    justify-content: center;
    margin-bottom: 20px;
    
    .weight-input {
      width: 150px;
      font-size: 48px;
      font-weight: 700;
      color: #4CAF50;
      text-align: center;
      background: transparent;
      border: none;
    }
    
    .weight-unit {
      font-size: 20px;
      color: #666;
      margin-left: 8px;
    }
  }
  
  .weight-slider {
    margin-bottom: 20px;
  }
  
  .weight-note {
    .note-input {
      width: 100%;
      height: 44px;
      background: #F5F7FA;
      border-radius: 10px;
      padding: 0 15px;
      font-size: 14px;
    }
  }
}

.recent-weights {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #EEE;
  
  .recent-title {
    font-size: 14px;
    color: #666;
    margin-bottom: 15px;
  }
}

.weight-trend-mini {
  display: flex;
  align-items: flex-end;
  justify-content: center;
  gap: 8px;
  height: 60px;
  
  .trend-point {
    width: 8px;
    background: #4CAF50;
    border-radius: 4px;
    min-height: 10%;
  }
}

.form-actions {
  display: flex;
  gap: 12px;
  margin-top: 30px;
  
  button {
    flex: 1;
    height: 44px;
    border-radius: 22px;
    font-size: 15px;
    border: none;
  }
  
  .btn-cancel {
    background: #F5F7FA;
    color: #7F8C8D;
  }
  
  .btn-confirm {
    background: #4CAF50;
    color: #fff;
  }
}

.weight-chart {
  padding: 30px;
  background: #F8F9FA;
  border-radius: 16px;
  margin-bottom: 20px;
  
  .chart-placeholder {
    text-align: center;
    
    text {
      display: block;
      font-size: 16px;
      color: #333;
      margin-bottom: 8px;
    }
    
    .sub-text {
      font-size: 13px;
      color: #999;
    }
  }
}

.history-list {
  max-height: 300px;
}

.history-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 0;
  border-bottom: 1px solid #F0F0F0;
  
  .history-date {
    text {
      display: block;
      font-size: 15px;
      color: #333;
    }
    
    .history-note {
      font-size: 12px;
      color: #999;
      margin-top: 4px;
    }
  }
  
  .history-weight {
    font-size: 18px;
    font-weight: 600;
    color: #4CAF50;
  }
}
</style>
