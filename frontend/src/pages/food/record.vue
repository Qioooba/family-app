<template>
  <view class="food-record-page">
    <!-- 导航栏 -->
    <view class="nav-bar">
      <view class="back-btn" @click="goBack">
        <up-icon name="arrow-left" size="40" color="#333"></up-icon>
      </view>
      <text class="title">记录饮食</text>
      <view class="setting-btn" @click="showTargetModal">
        <up-icon name="setting" size="36" color="#5AD8A6"></up-icon>
      </view>
    </view>
    
    <scroll-view class="record-container" scroll-y>
      <!-- 顶部统计卡片 -->
      <view class="stats-card">
        <view class="stats-header">
          <text class="date">{{ today }}</text>
          <view class="total-calories" @click="showTargetModal">
            <text class="num">{{ todayCalories }}</text>
            <text class="unit">千卡</text>
            <up-icon name="edit-pen" size="24" color="rgba(255,255,255,0.8)" class="edit-icon"></up-icon>
          </view>
        </view>
        
        <view class="progress-bar">
          <view class="progress-fill" :style="{ width: progressPercent + '%', background: progressColor }"></view>
        </view>
        
        <view class="stats-footer">
          <text>目标 {{ targetCalories }} 千卡</text>
          <text :class="{ 'over': remaining < 0 }">{{ remainingText }}</text>
        </view>
      </view>

      <!-- 拍照识别区域 -->
      <view class="section-card">
        <view class="section-title">
          <text class="title-icon">📷</text>
          <text>AI拍照识别</text>
        </view>
        
        <view class="camera-area" @click="takePhoto">
          <view v-if="photoUrls.length === 0" class="camera-placeholder">
            <view class="camera-icon">
              <up-icon name="camera" size="60" color="#5AD8A6"></up-icon>
            </view>
            <text class="camera-text">点击拍照识别食物</text>
            <text class="camera-subtext">AI自动识别食物并估算卡路里（可拍多张）</text>
          </view>
          <view v-else class="photo-grid">
            <view v-for="(photo, index) in photoUrls" :key="index" class="photo-item">
              <image :src="photo" mode="aspectFill" class="preview-img" />
              <view class="remove-photo-btn" @click.stop="removePhoto(index)">
                <up-icon name="close" size="24" color="#fff"></up-icon>
              </view>
            </view>
            <view class="add-more-btn" @click.stop="takePhoto">
              <up-icon name="plus" size="40" color="#5AD8A6"></up-icon>
              <text>继续添加</text>
            </view>
          </view>
        </view>
        
        <!-- 识别结果 -->
        <view v-if="recognizedResult" class="recognized-result">
          <view class="result-header">
            <text class="result-title">识别结果</text>
            <text class="result-edit" @click="editRecognizedResult">修正</text>
          </view>
          <view class="food-list">
            <view v-for="(food, index) in recognizedResult" :key="index" class="food-item">
              <view class="food-info">
                <text class="food-name">{{ food.name }}</text>
                <text class="food-weight">{{ food.weight }}g</text>
              </view>
              <text class="food-calories">{{ food.calories }}卡</text>
            </view>
          </view>
          <view class="result-total">
            <text>预估总热量：</text>
            <text class="total-num">{{ recognizedTotalCalories }}千卡</text>
          </view>
        </view>

        <!-- AI识别中状态 -->
        <view v-if="isRecognizing" class="recognizing-state">
          <view class="loading-spinner"></view>
          <text>AI正在识别食物...</text>
        </view>
      </view>
      
      <!-- 手动录入表单 -->
      <view class="section-card">
        <view class="section-title">
          <text class="title-icon">✏️</text>
          <text>手动录入</text>
        </view>
        
        <!-- 餐次选择 -->
        <view class="meal-selector">
          <view 
            v-for="(meal, index) in meals" 
            :key="index"
            class="meal-item"
            :class="{ active: form.meal === meal.value }"
            @click="form.meal = meal.value"
          >
            <text class="meal-icon">{{ meal.icon }}</text>
            <text class="meal-label">{{ meal.label }}</text>
          </view>
        </view>
        
        <!-- 食物名称输入 -->
        <view class="input-group">
          <text class="input-label">食物名称</text>
          <view class="input-wrapper">
            <input 
              v-model="form.foodName" 
              placeholder="例如：米饭、鸡蛋" 
              class="input"
              @input="onFoodNameInput"
              @focus="showSearchResults = true"
            />
            <view v-if="form.foodName" class="clear-btn" @click="clearFoodName">
              <up-icon name="close-circle" size="32" color="#ccc"></up-icon>
            </view>
          </view>
          <!-- 搜索结果下拉框 -->
          <view v-if="showSearchResults && searchResults.length > 0" class="search-results">
            <view 
              v-for="(food, index) in searchResults" 
              :key="index" 
              class="search-result-item"
              @click="selectSearchFood(food)"
            >
              <view class="result-left">
                <text class="food-name">{{ food.name }}</text>
                <text class="food-cal">{{ food.calories }}卡/{{ food.unit }}</text>
              </view>
              <text class="select-text">选择</text>
            </view>
          </view>
        </view>
        
        <!-- 重量输入 -->
        <view class="input-group">
          <text class="input-label">多少克？ <text class="input-hint">输入重量(g)</text></text>
          <view class="weight-input-wrapper">
            <input 
              v-model="form.weight" 
              type="digit" 
              placeholder="请输入食物重量" 
              class="input weight-input"
              @input="calculateCalories"
            />
            <text class="unit-text">g</text>
          </view>
          <!-- 快捷重量选择 -->
          <view class="quick-weights">
            <view 
              v-for="(w, index) in quickWeights" 
              :key="index"
              class="quick-weight-tag"
              :class="{ active: form.weight == w }"
              @click="setWeight(w)"
            >
              {{ w }}g
            </view>
          </view>
        </view>
        
        <!-- AI计算结果 -->
        <view v-if="form.calories > 0" class="calorie-result">
          <text class="result-label">AI计算结果</text>
          <view class="result-value">
            <text class="calorie-num">{{ form.calories }}</text>
            <text class="calorie-unit">千卡</text>
          </view>
        </view>
        
        <!-- 常用食物快捷选择 -->
        <view class="quick-foods">
          <text class="quick-title">常用食物</text>
          <view class="food-tags">
            <view 
              v-for="(food, index) in quickFoods" 
              :key="index"
              class="food-tag"
              @click="selectQuickFood(food)"
            >
              {{ food.name }}
            </view>
          </view>
        </view>
      </view>
      
      <!-- 当天饮食展示 -->
      <view class="section-card today-records" v-if="todayRecords.length > 0">
        <view class="section-title">
          <text class="title-icon">📋</text>
          <text>今日已记录 ({{ todayRecords.length }}条)</text>
        </view>
        
        <view class="record-list">
          <view v-for="(record, index) in todayRecords" :key="index" class="record-item">
            <image v-if="record.photo" :src="record.photo" mode="aspectFill" class="record-thumb" />
            <view v-else class="record-thumb-placeholder">
              <text>{{ record.meal === 'breakfast' ? '🌅' : record.meal === 'lunch' ? '☀️' : record.meal === 'dinner' ? '🌙' : '🍪' }}</text>
            </view>
            <view class="record-info">
              <text class="record-name">{{ record.foodName }}</text>
              <text class="record-detail">{{ record.weight }}g · {{ mealLabels[record.meal] }}</text>
            </view>
            <text class="record-calories">{{ record.calories }}卡</text>
          </view>
        </view>
      </view>
      
      <!-- 保存按钮 -->
      <view class="save-section">
        <button 
          class="save-btn" 
          :class="{ disabled: !canSave }"
          :disabled="!canSave"
          @click="saveRecord"
        >
          <text v-if="!isSaving">保存记录</text>
          <text v-else>保存中...</text>
        </button>
        <button class="continue-btn" @click="saveAndContinue">
          <text>保存并继续添加</text>
        </button>
      </view>
      
      <!-- 底部留白 -->
      <view class="bottom-space"></view>
    </scroll-view>

    <!-- 每日目标设置弹窗 -->
    <view v-if="targetModalVisible" class="modal-overlay" @click="closeTargetModal">
      <view class="modal-content" @click.stop>
        <view class="modal-header">
          <text>设置每日目标</text>
          <text class="close-btn" @click="closeTargetModal">✕</text>
        </view>
        
        <view class="target-input-section">
          <text class="input-desc">每日卡路里目标</text>
          <view class="target-input-wrapper">
            <input 
              v-model="targetInput" 
              type="number" 
              class="target-input"
              placeholder="2000"
            />
            <text class="unit">千卡</text>
          </view>
          
          <!-- 快捷目标选择 -->
          <view class="quick-targets">
            <view 
              v-for="(t, index) in quickTargets" 
              :key="index"
              class="quick-target-tag"
              :class="{ active: targetInput == t }"
              @click="targetInput = t.toString()"
            >
              {{ t }}卡
            </view>
          </view>
        </view>
        
        <view class="modal-actions">
          <button class="btn-cancel" @click="closeTargetModal">取消</button>
          <button class="btn-confirm" @click="saveTarget">保存</button>
        </view>
      </view>
    </view>

    <!-- 识别结果修正弹窗 -->
    <view v-if="editModalVisible" class="modal-overlay" @click="closeEditModal">
      <view class="modal-content edit-modal" @click.stop>
        <view class="modal-header">
          <text>修正识别结果</text>
          <text class="close-btn" @click="closeEditModal">✕</text>
        </view>
        
        <scroll-view class="edit-list" scroll-y>
          <view 
            v-for="(food, index) in editingResult" 
            :key="index"
            class="edit-item"
          >
            <view class="edit-field">
              <text class="field-label">食物</text>
              <input v-model="food.name" class="field-input" />
            </view>
            <view class="edit-row">
              <view class="edit-field half">
                <text class="field-label">重量(g)</text>
                <input v-model="food.weight" type="digit" class="field-input" />
              </view>
              <view class="edit-field half">
                <text class="field-label">热量(卡)</text>
                <input v-model="food.calories" type="digit" class="field-input" />
              </view>
            </view>
            <view class="delete-btn" @click="removeFoodItem(index)">
              <up-icon name="trash" size="32" color="#ff4d4f"></up-icon>
            </view>
          </view>
        </scroll-view>
        
        <view class="add-food-btn" @click="addFoodItem">
          <up-icon name="plus" size="28" color="#5AD8A6"></up-icon>
          <text>添加食物</text>
        </view>
        
        <view class="modal-actions">
          <button class="btn-cancel" @click="closeEditModal">取消</button>
          <button class="btn-continue" @click="saveEditAndContinue">继续添加</button>
          <button class="btn-confirm" @click="confirmEdit">确认</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { dietApi, userApi } from '../../api/index.js'

const today = new Date().toLocaleDateString('zh-CN', { month: 'long', day: 'numeric', weekday: 'short' })
const todayDate = new Date().toISOString().split('T')[0]
const todayCalories = ref(0)
const targetCalories = ref(2000)
const currentUser = ref(null)

// 表单数据
const form = ref({
  meal: 'breakfast',
  foodName: '',
  weight: '',
  calories: ''
})

// 拍照识别相关
const photoUrl = ref('')
const photoUrls = ref([])  // 支持多张照片
const isRecognizing = ref(false)
const recognizedResult = ref(null)
const recognizedResults = ref([])  // 多张照片识别结果

// 搜索相关
const searchResults = ref([])
const showSearchResults = ref(false)

// 弹窗相关
const targetModalVisible = ref(false)
const targetInput = ref('2000')
const editModalVisible = ref(false)
const editingResult = ref([])
const isSaving = ref(false)

// 今日记录
const todayRecords = ref([])

// 餐次标签
const mealLabels = {
  breakfast: '早餐',
  lunch: '午餐',
  dinner: '晚餐',
  snack: '加餐'
}

// 餐次选项
const meals = [
  { label: '早餐', value: 'breakfast', icon: '🌅' },
  { label: '午餐', value: 'lunch', icon: '☀️' },
  { label: '晚餐', value: 'dinner', icon: '🌙' },
  { label: '加餐', value: 'snack', icon: '🍪' }
]

// 快捷重量
const quickWeights = [50, 100, 150, 200, 250, 300]

// 快捷目标
const quickTargets = [1500, 1800, 2000, 2200, 2500, 2800]

// 常用食物
const quickFoods = [
  { name: '米饭(1碗)', calories: 200, weight: 150 },
  { name: '鸡蛋(1个)', calories: 70, weight: 50 },
  { name: '牛奶(1杯)', calories: 150, weight: 250 },
  { name: '苹果(1个)', calories: 95, weight: 180 },
  { name: '香蕉(1根)', calories: 105, weight: 120 },
  { name: '鸡胸肉(100g)', calories: 165, weight: 100 },
  { name: '牛肉(100g)', calories: 250, weight: 100 },
  { name: '青菜(100g)', calories: 25, weight: 100 }
]

// 食物数据库
const foodDatabase = [
  { name: '米饭', calories: 116, unit: '100g' },
  { name: '小米粥', calories: 46, unit: '100g' },
  { name: '面条', calories: 137, unit: '100g' },
  { name: '馒头', calories: 223, unit: '100g' },
  { name: '鸡蛋', calories: 144, unit: '100g' },
  { name: '牛奶', calories: 54, unit: '100ml' },
  { name: '豆浆', calories: 31, unit: '100ml' },
  { name: '面包', calories: 265, unit: '100g' },
  { name: '苹果', calories: 52, unit: '100g' },
  { name: '香蕉', calories: 89, unit: '100g' },
  { name: '橙子', calories: 47, unit: '100g' },
  { name: '葡萄', calories: 69, unit: '100g' },
  { name: '鸡胸肉', calories: 165, unit: '100g' },
  { name: '牛肉', calories: 250, unit: '100g' },
  { name: '猪肉', calories: 242, unit: '100g' },
  { name: '鱼肉', calories: 206, unit: '100g' },
  { name: '虾', calories: 85, unit: '100g' },
  { name: '豆腐', calories: 76, unit: '100g' },
  { name: '西红柿', calories: 18, unit: '100g' },
  { name: '黄瓜', calories: 16, unit: '100g' },
  { name: '白菜', calories: 13, unit: '100g' },
  { name: '菠菜', calories: 23, unit: '100g' },
  { name: '西兰花', calories: 34, unit: '100g' },
  { name: '胡萝卜', calories: 41, unit: '100g' },
  { name: '土豆', calories: 77, unit: '100g' },
  { name: '红薯', calories: 86, unit: '100g' },
  { name: '玉米', calories: 106, unit: '100g' },
  { name: '可乐', calories: 43, unit: '100ml' },
  { name: '酸奶', calories: 72, unit: '100g' },
  { name: '可乐鸡翅', calories: 180, unit: '100g' },
  { name: '红烧肉', calories: 470, unit: '100g' },
  { name: '清蒸鱼', calories: 120, unit: '100g' },
  { name: '炒青菜', calories: 45, unit: '100g' },
  { name: '煎蛋', calories: 195, unit: '100g' },
  { name: '炒饭', calories: 163, unit: '100g' },
  { name: '饺子', calories: 220, unit: '100g' },
  { name: '包子', calories: 230, unit: '100g' },
  { name: '油条', calories: 386, unit: '100g' },
  { name: '煎饼果子', calories: 160, unit: '100g' }
]

// 计算属性
const remaining = computed(() => targetCalories.value - todayCalories.value)
const remainingText = computed(() => {
  if (remaining.value >= 0) {
    return `剩余 ${remaining.value} 千卡`
  } else {
    return `超出 ${Math.abs(remaining.value)} 千卡`
  }
})
const progressPercent = computed(() => {
  const percent = (todayCalories.value / targetCalories.value) * 100
  return Math.min(percent, 100)
})
const progressColor = computed(() => {
  if (progressPercent.value >= 100) return '#ff4d4f'
  if (progressPercent.value >= 80) return '#faad14'
  return '#5AD8A6'
})

const recognizedTotalCalories = computed(() => {
  if (!recognizedResult.value) return 0
  return recognizedResult.value.reduce((sum, food) => sum + (parseInt(food.calories) || 0), 0)
})

const canSave = computed(() => {
  return form.value.foodName && form.value.weight > 0 && form.value.calories > 0
})

onMounted(() => {
  loadCurrentUser()
  loadTargetFromStorage()
})

// 加载当前用户信息
const loadCurrentUser = async () => {
  try {
    const res = await userApi.getCurrentUser()
    if (res) {
      currentUser.value = res
      targetCalories.value = res.targetCalories || 2000
      loadTodayStatistics()
    }
  } catch (e) {
    console.error('加载用户信息失败', e)
  }
}

// 加载今日统计数据
const loadTodayStatistics = async () => {
  if (!currentUser.value?.id) return
  try {
    // 加载统计数据
    const res = await dietApi.getDayStatistics(currentUser.value.id, todayDate)
    if (res) {
      todayCalories.value = res.intake || 0
      if (res.target) targetCalories.value = res.target
    }
    
    // 加载今日记录列表
    const recordsRes = await dietApi.getDayRecords(currentUser.value.id, todayDate)
    if (recordsRes && recordsRes.records) {
      todayRecords.value = recordsRes.records
    }
  } catch (e) {
    console.error('加载今日统计失败', e)
  }
}

// 从本地存储加载目标
const loadTargetFromStorage = () => {
  const saved = uni.getStorageSync('dailyCalorieTarget')
  if (saved) {
    targetCalories.value = parseInt(saved)
  }
}

// 拍照识别 - 支持多张照片
const takePhoto = () => {
  uni.chooseImage({
    count: 9,  // 最多9张
    sourceType: ['camera', 'album'],
    success: (res) => {
      const newPhotos = res.tempFilePaths
      photoUrls.value.push(...newPhotos)
      // 识别所有新照片
      recognizeMultipleFoods(newPhotos)
    }
  })
}

// 识别多张照片
const recognizeMultipleFoods = async (imagePaths) => {
  for (const imagePath of imagePaths) {
    await recognizeFood(imagePath)
  }
}

// 重新拍照 - 改为删除单张照片
const removePhoto = (index) => {
  photoUrls.value.splice(index, 1)
  recognizedResults.value.splice(index, 1)
  if (photoUrls.value.length === 0) {
    recognizedResult.value = null
  }
}

// AI识别食物
const recognizeFood = async (imagePath) => {
  isRecognizing.value = true
  
  try {
    // 调用AI识别API
    const res = await dietApi.recognizeFood(imagePath)
    
    if (res && res.foods && res.foods.length > 0) {
      // 合并识别结果
      const newFoods = res.foods.map(food => ({
        name: food.name,
        weight: food.weight || 100,
        calories: food.calories || 0
      }))
      
      if (!recognizedResult.value) {
        recognizedResult.value = []
      }
      recognizedResult.value.push(...newFoods)
      recognizedResults.value.push({
        photo: imagePath,
        foods: newFoods
      })
    } else {
      // Mock数据用于测试
      const mockFoods = [
        { name: '米饭', weight: 200, calories: 232 },
        { name: '红烧肉', weight: 100, calories: 470 }
      ]
      
      if (!recognizedResult.value) {
        recognizedResult.value = []
      }
      recognizedResult.value.push(...mockFoods)
      recognizedResults.value.push({
        photo: imagePath,
        foods: mockFoods
      })
    }
  } catch (e) {
    console.error('识别失败', e)
    uni.showToast({ title: '识别失败，请手动输入', icon: 'none' })
  } finally {
    isRecognizing.value = false
  }
}

// 编辑识别结果
const editRecognizedResult = () => {
  editingResult.value = JSON.parse(JSON.stringify(recognizedResult.value))
  editModalVisible.value = true
}

// 关闭编辑弹窗
const closeEditModal = () => {
  editModalVisible.value = false
}

// 确认编辑
const confirmEdit = () => {
  recognizedResult.value = editingResult.value
  closeEditModal()
}

// 保存编辑并继续添加（修正弹窗中的继续添加按钮）
const saveEditAndContinue = () => {
  recognizedResult.value = editingResult.value
  // 添加一个新的空食物项继续编辑
  editingResult.value.push({ name: '', weight: '', calories: '' })
  // 保持弹窗打开，让用户继续添加
  uni.showToast({ title: '请填写新食物', icon: 'none' })
}

// 添加食物项
const addFoodItem = () => {
  editingResult.value.push({ name: '', weight: '', calories: '' })
}

// 删除食物项
const removeFoodItem = (index) => {
  editingResult.value.splice(index, 1)
}

// 搜索食物
const onFoodNameInput = () => {
  const keyword = form.value.foodName
  if (!keyword) {
    searchResults.value = []
    return
  }
  searchResults.value = foodDatabase.filter(food => 
    food.name.includes(keyword)
  ).slice(0, 5)
}

// 选择搜索结果
const selectSearchFood = (food) => {
  form.value.foodName = food.name
  form.value.weight = 100
  form.value.calories = food.calories
  showSearchResults.value = false
}

// 清空食物名称
const clearFoodName = () => {
  form.value.foodName = ''
  form.value.calories = ''
  searchResults.value = []
}

// 设置重量
const setWeight = (weight) => {
  form.value.weight = weight
  calculateCalories()
}

// 计算卡路里 - 调用AI API
const calculateCalories = async () => {
  const weight = parseFloat(form.value.weight)
  const foodName = form.value.foodName
  if (!weight || !foodName) return
  
  try {
    // 调用API获取AI计算的卡路里
    const res = await dietApi.calculateCalories({
      foodName: foodName,
      weight: weight
    })
    
    if (res && res.calories) {
      form.value.calories = res.calories
    } else {
      // 本地计算作为fallback
      const food = foodDatabase.find(f => f.name === foodName)
      if (food) {
        form.value.calories = Math.round((food.calories * weight) / 100)
      }
    }
  } catch (e) {
    console.error('AI计算失败', e)
    // 本地计算作为fallback
    const food = foodDatabase.find(f => f.name === foodName)
    if (food) {
      form.value.calories = Math.round((food.calories * weight) / 100)
    }
  }
}

// 选择常用食物
const selectQuickFood = (food) => {
  form.value.foodName = food.name.replace(/\(.+\)/, '')
  form.value.weight = food.weight
  form.value.calories = food.calories
}

// 保存记录
const saveRecord = async () => {
  if (!canSave.value) {
    uni.showToast({ title: '请填写完整信息', icon: 'none' })
    return
  }
  
  // 检查用户登录状态
  if (!currentUser.value?.id) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    return
  }
  
  isSaving.value = true
  
  try {
    const recordData = {
      userId: currentUser.value.id,
      meal: form.value.meal,
      foodName: form.value.foodName,
      weight: parseFloat(form.value.weight),
      calories: parseFloat(form.value.calories),
      recordDate: todayDate,
      photo: photoUrls.value.length > 0 ? photoUrls.value[0] : null
    }
    
    console.log('保存记录:', recordData)
    
    const res = await dietApi.addRecord(recordData)
    console.log('保存结果:', res)
    
    // 如果有拍照识别结果，也保存
    if (recognizedResult.value && recognizedResult.value.length > 0) {
      for (const food of recognizedResult.value) {
        await dietApi.addRecord({
          userId: currentUser.value.id,
          meal: form.value.meal,
          foodName: food.name,
          weight: parseFloat(food.weight),
          calories: parseFloat(food.calories),
          recordDate: todayDate,
          photo: photoUrls.value.length > 0 ? photoUrls.value[0] : null
        })
      }
    }
    
    await loadTodayStatistics()
    
    uni.showToast({ title: '保存成功', icon: 'success' })
    
    // 延迟返回
    setTimeout(() => {
      uni.navigateBack()
    }, 1000)
  } catch (e) {
    console.error('保存失败', e)
    uni.showToast({ title: '保存失败: ' + (e.message || '请重试'), icon: 'none' })
  } finally {
    isSaving.value = false
  }
}

// 保存并继续添加
const saveAndContinue = async () => {
  if (!canSave.value) {
    uni.showToast({ title: '请填写完整信息', icon: 'none' })
    return
  }
  
  // 检查用户登录状态
  if (!currentUser.value?.id) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    return
  }
  
  isSaving.value = true
  
  try {
    const recordData = {
      userId: currentUser.value.id,
      meal: form.value.meal,
      foodName: form.value.foodName,
      weight: parseFloat(form.value.weight),
      calories: parseFloat(form.value.calories),
      recordDate: todayDate,
      photo: photoUrls.value.length > 0 ? photoUrls.value[0] : null
    }
    
    console.log('保存并继续:', recordData)
    
    const res = await dietApi.addRecord(recordData)
    console.log('保存结果:', res)
    
    await loadTodayStatistics()
    
    // 重置表单但保留餐次
    const currentMeal = form.value.meal
    form.value = {
      meal: currentMeal,
      foodName: '',
      weight: '',
      calories: ''
    }
    photoUrls.value = []
    recognizedResult.value = null
    recognizedResults.value = []
    
    uni.showToast({ title: '保存成功，继续添加', icon: 'success' })
  } catch (e) {
    console.error('保存失败', e)
    uni.showToast({ title: '保存失败: ' + (e.message || '请重试'), icon: 'none' })
  } finally {
    isSaving.value = false
  }
}

// 显示目标设置弹窗
const showTargetModal = () => {
  targetInput.value = targetCalories.value.toString()
  targetModalVisible.value = true
}

// 关闭目标设置弹窗
const closeTargetModal = () => {
  targetModalVisible.value = false
}

// 保存目标
const saveTarget = () => {
  const target = parseInt(targetInput.value)
  if (!target || target < 500 || target > 5000) {
    uni.showToast({ title: '请输入有效的目标值(500-5000)', icon: 'none' })
    return
  }
  
  targetCalories.value = target
  uni.setStorageSync('dailyCalorieTarget', target.toString())
  
  uni.showToast({ title: '目标设置成功', icon: 'success' })
  closeTargetModal()
}

// 返回上一页
const goBack = () => {
  uni.navigateBack()
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
  
  .setting-btn {
    width: 60rpx;
    height: 60rpx;
    display: flex;
    align-items: center;
    justify-content: center;
  }
}

.record-container {
  padding: 30rpx;
  height: calc(100vh - 140rpx);
}

// 统计卡片
.stats-card {
  background: linear-gradient(135deg, #5AD8A6 0%, #5B8FF9 100%);
  border-radius: 24rpx;
  padding: 40rpx;
  margin-bottom: 30rpx;
  box-shadow: 0 8rpx 32rpx rgba(90, 216, 166, 0.3);
  
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
      display: flex;
      align-items: center;
      gap: 8rpx;
      
      .num {
        font-size: 56rpx;
        font-weight: bold;
        color: #fff;
      }
      
      .unit {
        font-size: 26rpx;
        color: rgba(255,255,255,0.8);
      }
      
      .edit-icon {
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
      transition: width 0.3s, background 0.3s;
    }
  }
  
  .stats-footer {
    display: flex;
    justify-content: space-between;
    font-size: 24rpx;
    color: rgba(255,255,255,0.9);
    
    .over {
      color: #FFE4E1;
      font-weight: 600;
    }
  }
}

// 区块卡片
.section-card {
  background: #fff;
  border-radius: 20rpx;
  padding: 30rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 4rpx 20rpx rgba(0,0,0,0.05);
  
  .section-title {
    display: flex;
    align-items: center;
    gap: 12rpx;
    font-size: 30rpx;
    font-weight: 600;
    color: #333;
    margin-bottom: 24rpx;
    
    .title-icon {
      font-size: 36rpx;
    }
  }
}

// 拍照区域
.camera-area {
  height: 320rpx;
  background: linear-gradient(135deg, #f0f9f4 0%, #e6f7ff 100%);
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  border: 2rpx dashed #5AD8A6;
  
  .camera-placeholder {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 12rpx;
    
    .camera-icon {
      width: 100rpx;
      height: 100rpx;
      background: rgba(90, 216, 166, 0.1);
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
    }
    
    .camera-text {
      font-size: 30rpx;
      color: #333;
      font-weight: 500;
    }
    
    .camera-subtext {
      font-size: 24rpx;
      color: #999;
    }
  }
  
  .preview-img {
    width: 100%;
    height: 100%;
  }
  
  .photo-grid {
    display: flex;
    flex-wrap: wrap;
    gap: 16rpx;
    width: 100%;
    height: 100%;
    padding: 16rpx;
    
    .photo-item {
      position: relative;
      width: calc(33.33% - 11rpx);
      height: 160rpx;
      border-radius: 12rpx;
      overflow: hidden;
      
      .preview-img {
        width: 100%;
        height: 100%;
      }
      
      .remove-photo-btn {
        position: absolute;
        top: 4rpx;
        right: 4rpx;
        width: 40rpx;
        height: 40rpx;
        background: rgba(0,0,0,0.5);
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
      }
    }
    
    .add-more-btn {
      width: calc(33.33% - 11rpx);
      height: 160rpx;
      background: rgba(90, 216, 166, 0.1);
      border-radius: 12rpx;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      gap: 8rpx;
      border: 2rpx dashed #5AD8A6;
      
      text {
        font-size: 22rpx;
        color: #5AD8A6;
      }
    }
  }
}

// 识别中状态
.recognizing-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16rpx;
  padding: 40rpx;
  
  .loading-spinner {
    width: 48rpx;
    height: 48rpx;
    border: 4rpx solid #f0f0f0;
    border-top-color: #5AD8A6;
    border-radius: 50%;
    animation: spin 0.8s linear infinite;
  }
  
  text {
    font-size: 26rpx;
    color: #666;
  }
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

// 识别结果
.recognized-result {
  margin-top: 24rpx;
  padding-top: 24rpx;
  border-top: 2rpx solid #f0f0f0;
  
  .result-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16rpx;
    
    .result-title {
      font-size: 28rpx;
      font-weight: 600;
      color: #333;
    }
    
    .result-edit {
      font-size: 26rpx;
      color: #5AD8A6;
    }
  }
  
  .food-list {
    display: flex;
    flex-direction: column;
    gap: 12rpx;
    
    .food-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 20rpx;
      background: #f9f9f9;
      border-radius: 12rpx;
      
      .food-info {
        display: flex;
        flex-direction: column;
        gap: 4rpx;
        
        .food-name {
          font-size: 28rpx;
          color: #333;
          font-weight: 500;
        }
        
        .food-weight {
          font-size: 24rpx;
          color: #999;
        }
      }
      
      .food-calories {
        font-size: 28rpx;
        color: #5AD8A6;
        font-weight: 600;
      }
    }
  }
  
  .result-total {
    display: flex;
    justify-content: flex-end;
    align-items: center;
    gap: 8rpx;
    margin-top: 16rpx;
    padding-top: 16rpx;
    border-top: 2rpx solid #f0f0f0;
    font-size: 26rpx;
    color: #666;
    
    .total-num {
      font-size: 32rpx;
      color: #5AD8A6;
      font-weight: 600;
    }
  }
}

// 餐次选择
.meal-selector {
  display: flex;
  gap: 16rpx;
  margin-bottom: 30rpx;
  
  .meal-item {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8rpx;
    padding: 20rpx 0;
    background: #f5f5f5;
    border-radius: 16rpx;
    transition: all 0.2s;
    
    .meal-icon {
      font-size: 36rpx;
    }
    
    .meal-label {
      font-size: 24rpx;
      color: #666;
    }
    
    &.active {
      background: linear-gradient(135deg, #5AD8A6 0%, #5B8FF9 100%);
      
      .meal-label {
        color: #fff;
        font-weight: 500;
      }
    }
  }
}

// 输入组
.input-group {
  margin-bottom: 30rpx;
  position: relative;
  
  .input-label {
    display: block;
    font-size: 28rpx;
    color: #333;
    margin-bottom: 12rpx;
    font-weight: 500;
    
    .input-hint {
      font-size: 24rpx;
      color: #999;
      font-weight: normal;
      margin-left: 8rpx;
    }
  }
  
  .input-wrapper {
    position: relative;
    
    .input {
      width: 100%;
      height: 88rpx;
      background: #f5f5f5;
      border-radius: 12rpx;
      padding: 0 24rpx;
      font-size: 28rpx;
      padding-right: 70rpx;
    }
    
    .clear-btn {
      position: absolute;
      right: 20rpx;
      top: 50%;
      transform: translateY(-50%);
    }
  }
  
  // 搜索结果
  .search-results {
    position: absolute;
    top: 100%;
    left: 0;
    right: 0;
    background: #fff;
    border-radius: 12rpx;
    box-shadow: 0 8rpx 32rpx rgba(0,0,0,0.1);
    z-index: 100;
    margin-top: 8rpx;
    overflow: hidden;
    
    .search-result-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 24rpx;
      border-bottom: 1rpx solid #f0f0f0;
      
      &:last-child {
        border-bottom: none;
      }
      
      &:active {
        background: #f9f9f9;
      }
      
      .result-left {
        display: flex;
        flex-direction: column;
        gap: 4rpx;
        
        .food-name {
          font-size: 28rpx;
          color: #333;
        }
        
        .food-cal {
          font-size: 24rpx;
          color: #5AD8A6;
        }
      }
      
      .select-text {
        font-size: 24rpx;
        color: #5AD8A6;
        padding: 8rpx 16rpx;
        background: rgba(90, 216, 166, 0.1);
        border-radius: 20rpx;
      }
    }
  }
}

// 重量输入
.weight-input-wrapper {
  display: flex;
  align-items: center;
  gap: 16rpx;
  position: relative;
  
  .weight-input {
    flex: 1;
    text-align: right;
    padding-right: 60rpx;
    font-weight: 600;
    font-size: 32rpx;
    border: 2rpx solid #5AD8A6;
    background: #f0fff7;
  }
  
  .unit-text {
    position: absolute;
    right: 24rpx;
    font-size: 28rpx;
    color: #5AD8A6;
    font-weight: 600;
  }
}

// 快捷重量
.quick-weights {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  margin-top: 16rpx;
  
  .quick-weight-tag {
    padding: 12rpx 24rpx;
    background: #f0f0f0;
    border-radius: 24rpx;
    font-size: 24rpx;
    color: #666;
    
    &.active {
      background: linear-gradient(135deg, #5AD8A6 0%, #5B8FF9 100%);
      color: #fff;
    }
  }
}

// 卡路里计算结果
.calorie-result {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx;
  background: linear-gradient(135deg, #f0f9f4 0%, #e6f7ff 100%);
  border-radius: 16rpx;
  margin-bottom: 24rpx;
  
  .result-label {
    font-size: 26rpx;
    color: #666;
  }
  
  .result-value {
    display: flex;
    align-items: baseline;
    gap: 8rpx;
    
    .calorie-num {
      font-size: 48rpx;
      font-weight: bold;
      color: #5AD8A6;
    }
    
    .calorie-unit {
      font-size: 24rpx;
      color: #999;
    }
  }
}

// 常用食物
.quick-foods {
  .quick-title {
    display: block;
    font-size: 26rpx;
    color: #666;
    margin-bottom: 16rpx;
  }
  
  .food-tags {
    display: flex;
    flex-wrap: wrap;
    gap: 12rpx;
    
    .food-tag {
      padding: 16rpx 24rpx;
      background: #e6f7ff;
      border-radius: 28rpx;
      font-size: 24rpx;
      color: #5B8FF9;
      
      &:active {
        background: #d0e8ff;
      }
    }
  }
}

// 保存区域
.save-section {
  padding: 20rpx 0 40rpx;
  
  .save-btn {
    width: 100%;
    height: 96rpx;
    background: linear-gradient(135deg, #5AD8A6 0%, #5B8FF9 100%);
    border-radius: 48rpx;
    color: #fff;
    font-size: 32rpx;
    font-weight: 600;
    border: none;
    margin-bottom: 20rpx;
    box-shadow: 0 8rpx 24rpx rgba(90, 216, 166, 0.4);
    
    &.disabled {
      background: #ccc;
      box-shadow: none;
    }
    
    &:active {
      transform: scale(0.98);
    }
  }
  
  .continue-btn {
    width: 100%;
    height: 88rpx;
    background: #fff;
    border: 2rpx solid #5AD8A6;
    border-radius: 44rpx;
    color: #5AD8A6;
    font-size: 30rpx;
    
    &:active {
      background: #f0f9f4;
    }
  }
}

.bottom-space {
  height: 40rpx;
}

// 弹窗样式
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
  width: 80%;
  background: #fff;
  border-radius: 24rpx;
  padding: 40rpx;
  
  &.edit-modal {
    max-height: 70vh;
    display: flex;
    flex-direction: column;
  }
  
  .modal-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 40rpx;
    
    text {
      font-size: 34rpx;
      font-weight: 600;
      color: #333;
    }
    
    .close-btn {
      font-size: 40rpx;
      color: #999;
    }
  }
}

// 目标设置
.target-input-section {
  text-align: center;
  margin-bottom: 40rpx;
  
  .input-desc {
    display: block;
    font-size: 26rpx;
    color: #666;
    margin-bottom: 20rpx;
  }
  
  .target-input-wrapper {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 12rpx;
    margin-bottom: 30rpx;
    
    .target-input {
      width: 200rpx;
      height: 80rpx;
      background: #f5f5f5;
      border-radius: 12rpx;
      text-align: center;
      font-size: 40rpx;
      font-weight: 600;
      color: #333;
    }
    
    .unit {
      font-size: 28rpx;
      color: #666;
    }
  }
  
  .quick-targets {
    display: flex;
    flex-wrap: wrap;
    justify-content: center;
    gap: 16rpx;
    
    .quick-target-tag {
      padding: 16rpx 32rpx;
      background: #f0f0f0;
      border-radius: 28rpx;
      font-size: 26rpx;
      color: #666;
      
      &.active {
        background: linear-gradient(135deg, #5AD8A6 0%, #5B8FF9 100%);
        color: #fff;
      }
    }
  }
}

// 编辑列表
.edit-list {
  max-height: 400rpx;
  margin-bottom: 20rpx;
  
  .edit-item {
    position: relative;
    padding: 20rpx;
    background: #f9f9f9;
    border-radius: 12rpx;
    margin-bottom: 16rpx;
    
    .delete-btn {
      position: absolute;
      top: 20rpx;
      right: 20rpx;
    }
    
    .edit-field {
      margin-bottom: 16rpx;
      
      &.half {
        flex: 1;
      }
      
      .field-label {
        display: block;
        font-size: 24rpx;
        color: #999;
        margin-bottom: 8rpx;
      }
      
      .field-input {
        width: 100%;
        height: 64rpx;
        background: #fff;
        border-radius: 8rpx;
        padding: 0 16rpx;
        font-size: 28rpx;
      }
    }
    
    .edit-row {
      display: flex;
      gap: 16rpx;
    }
  }
}

.add-food-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  padding: 20rpx;
  border: 2rpx dashed #5AD8A6;
  border-radius: 12rpx;
  margin-bottom: 30rpx;
  
  text {
    font-size: 26rpx;
    color: #5AD8A6;
  }
}

// 弹窗按钮
.modal-actions {
  display: flex;
  gap: 16rpx;
  
  button {
    flex: 1;
    height: 88rpx;
    border-radius: 44rpx;
    font-size: 28rpx;
    border: none;
  }
  
  .btn-cancel {
    background: #f5f5f5;
    color: #666;
  }
  
  .btn-continue {
    background: #e6f7ff;
    color: #5B8FF9;
    border: 2rpx solid #5B8FF9;
  }
  
  .btn-confirm {
    background: linear-gradient(135deg, #5AD8A6 0%, #5B8FF9 100%);
    color: #fff;
  }
}

// 今日记录区域
.today-records {
  .record-list {
    display: flex;
    flex-direction: column;
    gap: 16rpx;
    
    .record-item {
      display: flex;
      align-items: center;
      gap: 16rpx;
      padding: 16rpx;
      background: #f9f9f9;
      border-radius: 16rpx;
      
      .record-thumb {
        width: 80rpx;
        height: 80rpx;
        border-radius: 12rpx;
        object-fit: cover;
      }
      
      .record-thumb-placeholder {
        width: 80rpx;
        height: 80rpx;
        border-radius: 12rpx;
        background: linear-gradient(135deg, #f0f9f4 0%, #e6f7ff 100%);
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 36rpx;
      }
      
      .record-info {
        flex: 1;
        display: flex;
        flex-direction: column;
        gap: 4rpx;
        
        .record-name {
          font-size: 28rpx;
          color: #333;
          font-weight: 500;
        }
        
        .record-detail {
          font-size: 24rpx;
          color: #999;
        }
      }
      
      .record-calories {
        font-size: 28rpx;
        color: #5AD8A6;
        font-weight: 600;
      }
    }
  }
}
</style>