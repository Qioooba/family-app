<template>
  <view class="recipe-detail-page">
    <view class="recipe-image">
      <image :src="recipe.cover" mode="aspectFill" />
      <view class="nav-bar">
        <view class="back-btn" @click="goBack">
          <view class="icon-bg">
            <u-icon name="arrow-left" size="36" color="#fff"></u-icon>
          </view>
        </view>
        
        <view class="actions">
          <view class="icon-bg" @click="toggleFavorite">
            <u-icon :name="isFavorite ? 'heart-fill' : 'heart'" size="36" :color="isFavorite ? '#FF4D4F' : '#fff'"></u-icon>
          </view>
          <view class="icon-bg" @click="shareRecipe">
            <u-icon name="share" size="36" color="#fff"></u-icon>
          </view>
        </view>
      </view>
      
      <view class="recipe-badge">
        <text class="time">⏱️ {{ recipe.time }}分钟</text>
        <text class="calories">🔥 {{ recipe.calories }}卡</text>
      </view>
    </view>
    
    <view class="detail-container">
      <view class="recipe-header">
        <text class="name">{{ recipe.name }}</text>
        <view class="tags">
          <text v-for="tag in recipe.tags" :key="tag" class="tag">{{ tag }}</text>
        </view>
        
        <view class="stats">
          <view class="stat-item">
            <u-icon name="eye" size="28" color="#999"></u-icon>
            <text>{{ recipe.views }} 浏览</text>
          </view>
          <view class="stat-item">
            <u-icon name="heart" size="28" color="#999"></u-icon>
            <text>{{ recipe.likes }} 收藏</text>
          </view>
          <view class="stat-item">
            <u-icon name="checkmark-circle" size="28" color="#999"></u-icon>
            <text>{{ recipe.made }} 人做过</text>
          </view>
        </view>
      </view>
      
      <!-- 食材 -->
      <view class="section">
        <view class="section-title">
          <text>食材清单</text>
          <text class="portion">{{ recipe.portion }}人份</text>
        </view>
        
        <view class="ingredient-list">
          <view 
            v-for="item in recipe.ingredients" 
            :key="item.name"
            class="ingredient-item"
            :class="{ checked: item.checked }"
            @click="item.checked = !item.checked"
          >
            <view class="checkbox">
              <u-icon v-if="item.checked" name="checkmark" size="24" color="#5AD8A6"></u-icon>
            </view>
            <text class="name">{{ item.name }}</text>
            <text class="amount">{{ item.amount }}</text>
          </view>
        </view>
      </view>
      
      <!-- 步骤 -->
      <view class="section">
        <view class="section-title"><text>烹饪步骤</text></view>
        
        <view class="step-list">
          <view 
            v-for="(step, index) in recipe.steps" 
            :key="index"
            class="step-item"
          >
            <view class="step-num">{{ index + 1 }}</view>
            <view class="step-content">
              <image v-if="step.image" :src="step.image" mode="aspectFill" class="step-img" />
              <text class="step-text">{{ step.text }}</text>
            </view>
          </view>
        </view>
      </view>
      
      <!-- 营养信息 -->
      <view class="section">
        <view class="section-title"><text>营养成分</text></view>
        
        <view class="nutrition-list">
          <view class="nutrition-item">
            <text class="value">{{ recipe.nutrition.protein }}g</text>
            <text class="label">蛋白质</text>
          </view>
          <view class="nutrition-item">
            <text class="value">{{ recipe.nutrition.fat }}g</text>
            <text class="label">脂肪</text>
          </view>
          <view class="nutrition-item">
            <text class="value">{{ recipe.nutrition.carbs }}g</text>
            <text class="label">碳水</text>
          </view>
          <view class="nutrition-item">
            <text class="value">{{ recipe.nutrition.fiber }}g</text>
            <text class="label">纤维</text>
          </view>
        </view>
      </view>
    </view>
    
    <!-- 底部操作 -->
    <view class="bottom-actions">
      <view class="action-btn secondary" @click="addToPlan">
        <u-icon name="calendar" size="32" color="#5B8FF9"></u-icon>
        <text>加入计划</text>
      </view>
      
      <view class="action-btn primary" @click="markAsMade">
        <u-icon name="checkmark" size="32" color="#fff"></u-icon>
        <text>标记做过</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { recipeApi } from '../../api/index.js'

const recipeId = ref(null)
const isFavorite = ref(false)
const loading = ref(false)

const recipe = ref({
  id: 1,
  name: '番茄炒蛋',
  cover: '/static/recipes/fanqie.jpg',
  time: 15,
  calories: 180,
  tags: ['家常菜', '快手菜', '低卡'],
  views: 2340,
  likes: 568,
  made: 123,
  portion: 2,
  ingredients: [
    { name: '番茄', amount: '2个', checked: false },
    { name: '鸡蛋', amount: '3个', checked: false },
    { name: '葱', amount: '适量', checked: false },
    { name: '盐', amount: '1小勺', checked: false },
    { name: '糖', amount: '1小勺', checked: false },
    { name: '油', amount: '2大勺', checked: false }
  ],
  steps: [
    { text: '番茄洗净切块，鸡蛋打散备用', image: '' },
    { text: '锅中放油，倒入蛋液快速划散', image: '' },
    { text: '蛋液凝固后盛出备用', image: '' },
    { text: '锅中再放油，放入番茄翻炒出汁', image: '' },
    { text: '加入炒好的鸡蛋，调入盐和糖', image: '' },
    { text: '撒上葱花，翻炒均匀即可出锅', image: '' }
  ],
  nutrition: {
    protein: 12,
    fat: 8,
    carbs: 15,
    fiber: 2
  }
})

onMounted(() => {
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  recipeId.value = currentPage.options.id
  if (recipeId.value) {
    loadRecipeDetail(recipeId.value)
  }
})

// 加载菜谱详情
const loadRecipeDetail = async (id) => {
  loading.value = true
  try {
    const res = await recipeApi.getById(id)
    if (res) {
      // 合并数据，保持前端需要的结构
      recipe.value = { 
        ...recipe.value, 
        ...res,
        ingredients: res.ingredients || recipe.value.ingredients,
        steps: res.steps || recipe.value.steps,
        nutrition: res.nutrition || recipe.value.nutrition
      }
      isFavorite.value = res.isFavorite || false
    }
  } catch (e) {
    console.error('加载菜谱详情失败', e)
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  uni.navigateBack()
}

const toggleFavorite = async () => {
  try {
    const userId = uni.getStorageSync('userInfo')?.id || 1
    await recipeApi.favorite(recipeId.value, userId)
    isFavorite.value = !isFavorite.value
    uni.showToast({ 
      title: isFavorite.value ? '已收藏' : '取消收藏', 
      icon: 'none' 
    })
  } catch (e) {
    console.error('收藏失败', e)
    uni.showToast({ title: '操作失败', icon: 'none' })
  }
}

const shareRecipe = () => {
  uni.showShareMenu({
    withShareTicket: true,
    menus: ['shareAppMessage', 'shareTimeline']
  })
}

const addToPlan = () => {
  uni.showActionSheet({
    itemList: ['今天', '明天', '本周'],
    success: () => {
      uni.showToast({ title: '已加入计划', icon: 'success' })
    }
  })
}

const markAsMade = async () => {
  try {
    const userId = uni.getStorageSync('userInfo')?.id || 1
    await recipeApi.recordCooking(recipeId.value, userId)
    uni.showToast({ title: '已标记做过', icon: 'success' })
    recipe.value.made = (recipe.value.made || 0) + 1
  } catch (e) {
    console.error('记录失败', e)
    uni.showToast({ title: '记录失败', icon: 'none' })
  }
}
</script>

<style lang="scss" scoped>
.recipe-detail-page {
  min-height: 100vh;
  background: #f5f6fa;
  padding-bottom: 140rpx;
}

.recipe-image {
  position: relative;
  height: 500rpx;
  
  image {
    width: 100%;
    height: 100%;
  }
  
  .nav-bar {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    display: flex;
    justify-content: space-between;
    padding: 60rpx 30rpx 20rpx;
    
    .icon-bg {
      width: 72rpx;
      height: 72rpx;
      background: rgba(0,0,0,0.3);
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
    }
    
    .actions {
      display: flex;
      gap: 20rpx;
    }
  }
  
  .recipe-badge {
    position: absolute;
    bottom: 30rpx;
    right: 30rpx;
    display: flex;
    gap: 20rpx;
    
    text {
      padding: 12rpx 24rpx;
      background: rgba(0,0,0,0.5);
      border-radius: 30rpx;
      font-size: 24rpx;
      color: #fff;
    }
  }
}

.detail-container {
  margin-top: -40rpx;
  background: #f5f6fa;
  border-radius: 40rpx 40rpx 0 0;
  position: relative;
  z-index: 1;
}

.recipe-header {
  background: #fff;
  padding: 40rpx;
  margin-bottom: 20rpx;
  
  .name {
    font-size: 44rpx;
    font-weight: bold;
    color: #333;
    display: block;
    margin-bottom: 20rpx;
  }
  
  .tags {
    display: flex;
    gap: 16rpx;
    margin-bottom: 30rpx;
    
    .tag {
      padding: 8rpx 20rpx;
      background: #E6F7FF;
      border-radius: 20rpx;
      font-size: 24rpx;
      color: #1890FF;
    }
  }
  
  .stats {
    display: flex;
    gap: 40rpx;
    
    .stat-item {
      display: flex;
      align-items: center;
      gap: 8rpx;
      font-size: 24rpx;
      color: #999;
    }
  }
}

.section {
  background: #fff;
  padding: 30rpx;
  margin-bottom: 20rpx;
  
  .section-title {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 30rpx;
    font-size: 32rpx;
    font-weight: 600;
    color: #333;
    
    .portion {
      font-size: 26rpx;
      color: #999;
      font-weight: normal;
    }
  }
}

.ingredient-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
  
  .ingredient-item {
    display: flex;
    align-items: center;
    gap: 20rpx;
    padding: 20rpx;
    background: #f9f9f9;
    border-radius: 12rpx;
    
    &.checked {
      opacity: 0.6;
      
      .name {
        text-decoration: line-through;
      }
    }
    
    .checkbox {
      width: 40rpx;
      height: 40rpx;
      border: 2rpx solid #ddd;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
    }
    
    .name {
      flex: 1;
      font-size: 28rpx;
      color: #333;
    }
    
    .amount {
      font-size: 26rpx;
      color: #999;
    }
  }
}

.step-list {
  display: flex;
  flex-direction: column;
  gap: 40rpx;
  
  .step-item {
    display: flex;
    gap: 24rpx;
    
    .step-num {
      width: 48rpx;
      height: 48rpx;
      background: #5B8FF9;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 26rpx;
      font-weight: 600;
      color: #fff;
      flex-shrink: 0;
    }
    
    .step-content {
      flex: 1;
      
      .step-img {
        width: 100%;
        height: 300rpx;
        border-radius: 16rpx;
        margin-bottom: 16rpx;
      }
      
      .step-text {
        font-size: 28rpx;
        color: #333;
        line-height: 1.6;
      }
    }
  }
}

.nutrition-list {
  display: flex;
  justify-content: space-around;
  
  .nutrition-item {
    text-align: center;
    
    .value {
      font-size: 36rpx;
      font-weight: bold;
      color: #5B8FF9;
      display: block;
      margin-bottom: 8rpx;
    }
    
    .label {
      font-size: 24rpx;
      color: #999;
    }
  }
}

.bottom-actions {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  gap: 20rpx;
  padding: 20rpx 30rpx;
  padding-bottom: 40rpx;
  background: #fff;
  box-shadow: 0 -4rpx 20rpx rgba(0,0,0,0.05);
  
  .action-btn {
    flex: 1;
    height: 90rpx;
    border-radius: 45rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 12rpx;
    
    &.secondary {
      background: #E6F7FF;
      
      text {
        font-size: 28rpx;
        color: #5B8FF9;
      }
    }
    
    &.primary {
      background: #5B8FF9;
      
      text {
        font-size: 28rpx;
        color: #fff;
      }
    }
  }
}
</style>