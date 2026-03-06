<template>
  <view class="page-container">
    <view class="header">
      <view class="header-title">智能菜谱 🍳</view>
      <view class="header-action" @click="aiRecommend">
        <text class="icon">⭐</text>
      </view>
    </view>
    
    <view class="search-bar">
      <input class="search-input" placeholder="搜索菜谱、食材..." v-model="searchKey" />
      <view class="search-btn" @click="search">🔍</view>
    </view>
    
    <view class="category-tags">
      <scroll-view scroll-x>
        <view 
          v-for="(cat, index) in categories" 
          :key="index"
          class="category-tag"
          @click="filterByCategory(cat)"
        >
          <text>{{ cat }}</text>
        </view>
      </scroll-view>
    </view>
    
    <scroll-view class="recipe-list" scroll-y>
      <view 
        v-for="(recipe, index) in recipes" 
        :key="index"
        class="recipe-card"
        @click="viewRecipe(recipe)"
      >
        <image class="recipe-image" :src="recipe.image" mode="aspectFill" />
        
        <view class="recipe-info">
          <view class="recipe-title">{{ recipe.name }}</view>
          <view class="recipe-meta">
            <text>⏱️ {{ recipe.time }}分钟</text>
            <text>🔥 {{ recipe.calories }}kcal</text>
          </view>
          <view class="recipe-tags">
            <view class="tag" v-for="(tag, i) in recipe.tags" :key="i">{{ tag }}</view>
          </view>
        </view>
        
        <view class="recipe-actions">
          <view class="action-btn" :class="{ active: recipe.isFavorite }" @click.stop="toggleFavorite(recipe)">
            <text>{{ recipe.isFavorite ? '❤️' : '🤍' }}</text>
          </view>
          <view class="action-btn" @click.stop="cookIt(recipe)">
            <text>👨‍🍳 做过</text>
          </view>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { recipeApi } from '../../api/index.js'

const searchKey = ref('')
const categories = ['家常菜', '快手菜', '下饭菜', '汤羹', '面食', '烘焙', '宝宝辅食', '减脂餐']
const recipes = ref([])
const loading = ref(false)
const currentCategory = ref('')

// 加载菜谱列表
const loadRecipes = async () => {
  loading.value = true
  try {
    const familyId = uni.getStorageSync('currentFamilyId') || 1
    const params = {
      familyId: familyId,
      keyword: searchKey.value || undefined,
      category: currentCategory.value || undefined
    }
    const res = await recipeApi.getFamilyRecipes(familyId)
    recipes.value = res || []
  } catch (e) {
    console.error('加载菜谱失败', e)
    uni.showToast({ title: '加载菜谱失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

// 页面加载时获取菜谱
onMounted(() => {
  loadRecipes()
})

const search = async () => {
  await loadRecipes()
}

const filterByCategory = (cat) => {
  currentCategory.value = cat
  loadRecipes()
  uni.showToast({ title: `筛选: ${cat}`, icon: 'none' })
}

const viewRecipe = (recipe) => {
  uni.navigateTo({
    url: `/pages/recipe/detail?id=${recipe.id}`
  })
}

const toggleFavorite = async (recipe) => {
  try {
    const userId = uni.getStorageSync('userInfo')?.id || 1
    await recipeApi.favorite(recipe.id, userId)
    recipe.isFavorite = !recipe.isFavorite
    uni.showToast({ 
      title: recipe.isFavorite ? '已收藏' : '取消收藏', 
      icon: 'none' 
    })
  } catch (e) {
    console.error('收藏失败', e)
    uni.showToast({ title: '操作失败', icon: 'none' })
  }
}

const cookIt = (recipe) => {
  uni.showModal({
    title: '确认记录',
    content: `确定要记录做过"${recipe.name}"吗？`,
    success: async (res) => {
      if (res.confirm) {
        try {
          const userId = uni.getStorageSync('userInfo')?.id || 1
          await recipeApi.recordCooking(recipe.id, userId)
          uni.showToast({ title: '已记录到做过', icon: 'success' })
        } catch (e) {
          console.error('记录失败', e)
          uni.showToast({ title: '记录失败', icon: 'none' })
        }
      }
    }
  })
}

const aiRecommend = () => {
  uni.showModal({
    title: '⭐ 智能菜谱推荐',
    content: '根据您的口味偏好，推荐以下菜谱：\n\n1. 番茄土豆炖牛肉\n2. 蒜蓉西兰花\n3. 可乐鸡翅',
    showCancel: true,
    confirmText: '查看详情',
    cancelText: '关闭',
    success: (res) => {
      if (res.confirm) {
        uni.navigateTo({ url: '/pages/recipe/index' })
      }
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
  background: linear-gradient(135deg, #FFF3E0, #FFE0B2);
  
  .header-title {
    font-size: 24px;
    font-weight: 700;
    color: #2C3E50;
  }
  
  .header-action {
    width: 44px;
    height: 44px;
    background: #FF9800;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    
    .icon {
      font-size: 22px;
    }
  }
}

.search-bar {
  display: flex;
  padding: 15px;
  gap: 10px;
  background: #fff;
  
  .search-input {
    flex: 1;
    height: 40px;
    background: #F5F7FA;
    border-radius: 20px;
    padding: 0 15px;
    font-size: 14px;
  }
  
  .search-btn {
    width: 40px;
    height: 40px;
    background: #FF9800;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
  }
}

.category-tags {
  padding: 10px 15px;
  background: #fff;
  white-space: nowrap;
  
  .category-tag {
    display: inline-block;
    padding: 8px 16px;
    margin-right: 10px;
    background: #FFF3E0;
    border-radius: 16px;
    font-size: 13px;
    color: #FF9800;
  }
}

.recipe-list {
  padding: 15px;
  height: calc(100vh - 240px);
}

.recipe-card {
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  margin-bottom: 15px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  
  .recipe-image {
    width: 100%;
    height: 180px;
  }
  
  .recipe-info {
    padding: 15px;
    
    .recipe-title {
      font-size: 16px;
      font-weight: 600;
      color: #2C3E50;
      margin-bottom: 8px;
    }
    
    .recipe-meta {
      display: flex;
      gap: 15px;
      font-size: 12px;
      color: #7F8C8D;
      margin-bottom: 10px;
    }
    
    .recipe-tags {
      display: flex;
      gap: 8px;
      
      .tag {
        padding: 4px 10px;
        background: #FFF3E0;
        color: #FF9800;
        border-radius: 8px;
        font-size: 11px;
      }
    }
  }
  
  .recipe-actions {
    display: flex;
    border-top: 1px solid #F0F0F0;
    
    .action-btn {
      flex: 1;
      padding: 12px;
      text-align: center;
      font-size: 14px;
      color: #7F8C8D;
      
      &.active {
        color: #E91E63;
      }
      
      &:first-child {
        border-right: 1px solid #F0F0F0;
      }
    }
  }
}
</style>
