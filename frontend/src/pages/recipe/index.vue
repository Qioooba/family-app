<template>
  <view class="recipe-page">
    <!-- 搜索栏 -->
    <view class="search-header">
      <view class="search-box"
        <u-icon name="search" size="32" color="#999"></u-icon>
        <input 
          type="text" 
          v-model="searchKeyword"
          placeholder="搜索菜谱、食材..."
          confirm-type="search"
          @confirm="onSearch"
        />
        
        <view v-if="searchKeyword" class="clear-btn" @click="searchKeyword = ''"
          <u-icon name="close-circle" size="32" color="#ccc"></u-icon>
        </view>
      </view>
      
      <view class="ai-btn" @click="goAIRecommend"
        <u-icon name="chat-fill" size="40" color="#fff"></u-icon>
      </view>
    </view>
    
    <!-- 分类快捷入口 -->
    <view class="category-grid"
      <view 
        v-for="(cat, index) in categories" 
        :key="index"
        class="category-item"
        @click="filterByCategory(cat.key)"
      >
        <view class="cat-icon" :style="{ background: cat.color }"
          <text>{{ cat.icon }}</text>
        </view>
        
        <text class="cat-name">{{ cat.name }}</text>
      </view>
    </view>
    
    <!-- 今日推荐/智能生成 -->
    <view class="ai-section" v-if="aiRecipes.length > 0"
      <view class="section-header"
        <view class="section-title"
          <u-icon name="chat-fill" size="32" color="#5B8FF9"></u-icon>
          <text>AI 智能推荐</text>
        </view>
        
        <text class="refresh" @click="refreshAI">🔄 换一批</text>
      </view>
      
      <view class="ai-reason">根据您冰箱里的{{ aiReason }}，推荐以下菜谱：</view>
      
      <scroll-view scroll-x class="recipe-scroll"
        <view 
          v-for="recipe in aiRecipes" 
          :key="recipe.id"
          class="recipe-card ai-card"
          @click="goDetail(recipe)"
        >
          <image :src="recipe.cover" mode="aspectFill" />
          
          <view class="recipe-info"
            <text class="recipe-name">{{ recipe.name }}</text>
            
            <view class="match-rate"
              <text>匹配度 {{ recipe.matchRate }}%</text>
            </view>
            
            <view class="recipe-meta"
              <text>⏱️ {{ recipe.time }}分钟</text>
              <text>🔥 {{ recipe.calories }}卡</text>
            </view>
          </view>
        </view>
      </scroll-view>
    </view>
    
    <!-- 菜谱列表 -->
    <view class="recipe-section"
      <view class="section-header"
        <text class="section-title">🍳 热门菜谱</text>
        
        <view class="filter-btns"
          <text 
            class="filter-btn" 
            :class="{ active: sortBy === 'hot' }"
            @click="sortBy = 'hot'"
          >最热</text>
          
          <text 
            class="filter-btn" 
            :class="{ active: sortBy === 'new' }"
            @click="sortBy = 'new'"
          >最新</text>
          
          <text 
            class="filter-btn" 
            :class="{ active: sortBy === 'rating' }"
            @click="sortBy = 'rating'"
          >好评</text>
        </view>
      </view>
      
      <view class="recipe-list"
        <view 
          v-for="recipe in recipes" 
          :key="recipe.id"
          class="recipe-item"
          @click="goDetail(recipe)"
        >
          <image :src="recipe.cover" mode="aspectFill" />
          
          <view class="item-info"
            <text class="item-name">{{ recipe.name }}</text>
            
            <view class="item-tags"
              <text class="tag difficulty">{{ recipe.difficulty }}</text>
              <text class="tag time">{{ recipe.time }}分钟</text>
              <text class="tag calories">{{ recipe.calories }}卡</text>
            </view>
            
            <view class="item-stats"
              <text>⭐ {{ recipe.rating }}</text>
              <text>💕 {{ recipe.favoriteCount }}</text>
              <text>👨‍🍳 {{ recipe.makeCount }}人做过</text>
            </view>
          </view>
        </view>
      </view>
    </view>
    
    <!-- 浮动菜单按钮 -->
    <view class="fab-menu"
      <view class="fab-item scan" @click="scanIngredient"
        <u-icon name="scan" size="40" color="#fff"></u-icon>
      </view>
      
      <view class="fab-item add" @click="createRecipe"
        <u-icon name="plus" size="48" color="#fff"></u-icon>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'

const searchKeyword = ref('')
const sortBy = ref('hot')
const aiReason = ref('土豆、牛肉、西红柿')

const categories = [
  { key: 'quick', name: '快手菜', icon: '⚡', color: '#FFD93D' },
  { key: 'breakfast', name: '早餐', icon: '🍳', color: '#FF9F43' },
  { key: 'lunch', name: '午餐', icon: '🍱', color: '#EE5A24' },
  { key: 'dinner', name: '晚餐', icon: '🥘', color: '#009432' },
  { key: 'soup', name: '汤羹', icon: '🍲', color: '#9980FA' },
  { key: 'dessert', name: '烘焙', icon: '🧁', color: '#F368E0' },
  { key: 'diet', name: '减脂', icon: '🥗', color: '#00D2D3' },
  { key: 'baby', name: '宝宝辅食', icon: '🍼', color: '#FF6B6B' }
]

const aiRecipes = ref([
  { id: 1, name: '番茄土豆炖牛肉', cover: '/static/recipes/beef-tomato.jpg', time: 60, calories: 320, matchRate: 98 },
  { id: 2, name: '西红柿炒鸡蛋', cover: '/static/recipes/egg-tomato.jpg', time: 10, calories: 150, matchRate: 95 },
  { id: 3, name: '土豆烧排骨', cover: '/static/recipes/potato-ribs.jpg', time: 45, calories: 380, matchRate: 92 }
])

const recipes = ref([
  {
    id: 101,
    name: '红烧肉',
    cover: '/static/recipes/hongshaorou.jpg',
    difficulty: '中等',
    time: 60,
    calories: 450,
    rating: 4.9,
    favoriteCount: 1234,
    makeCount: 5678
  },
  {
    id: 102,
    name: '麻婆豆腐',
    cover: '/static/recipes/mapo-doufu.jpg',
    difficulty: '简单',
    time: 20,
    calories: 220,
    rating: 4.8,
    favoriteCount: 892,
    makeCount: 3456
  },
  {
    id: 103,
    name: '清蒸鲈鱼',
    cover: '/static/recipes/qingzheng-luyu.jpg',
    difficulty: '中等',
    time: 25,
    calories: 180,
    rating: 4.9,
    favoriteCount: 756,
    makeCount: 2345
  }
])

const onSearch = () => {
  console.log('搜索:', searchKeyword.value)
}

const filterByCategory = (key) => {
  console.log('分类:', key)
}

const goAIRecommend = () => {
  uni.navigateTo({ url: '/pages/recipe/ai-recommend' })
}

const refreshAI = () => {
  console.log('刷新推荐')
}

const goDetail = (recipe) => {
  uni.navigateTo({ url: `/pages/recipe/detail?id=${recipe.id}` })
}

const scanIngredient = () => {
  uni.navigateTo({ url: '/pages/food/scan' })
}

const createRecipe = () => {
  uni.navigateTo({ url: '/pages/recipe/create' })
}
</script>

<style lang="scss" scoped>
.recipe-page {
  min-height: 100vh;
  background: #f5f6fa;
  padding-bottom: 120rpx;
}

.search-header {
  display: flex;
  align-items: center;
  padding: 20rpx 30rpx;
  background: #fff;
  
  .search-box {
    flex: 1;
    display: flex;
    align-items: center;
    background: #f5f6fa;
    border-radius: 36rpx;
    padding: 16rpx 24rpx;
    margin-right: 20rpx;
    
    input {
      flex: 1;
      margin-left: 16rpx;
      font-size: 28rpx;
    }
    
    .clear-btn {
      padding: 8rpx;
    }
  }
  
  .ai-btn {
    width: 72rpx;
    height: 72rpx;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
  }
}

.category-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20rpx;
  padding: 30rpx;
  background: #fff;
  margin-bottom: 20rpx;
  
  .category-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    
    .cat-icon {
      width: 100rpx;
      height: 100rpx;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-bottom: 12rpx;
      
      text {
        font-size: 48rpx;
      }
    }
    
    .cat-name {
      font-size: 24rpx;
      color: #666;
    }
  }
}

.ai-section {
  background: #fff;
  padding: 30rpx;
  margin-bottom: 20rpx;
  
  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16rpx;
    
    .section-title {
      display: flex;
      align-items: center;
      
      text {
        margin-left: 12rpx;
        font-size: 32rpx;
        font-weight: 600;
        color: #333;
        background: linear-gradient(90deg, #667eea, #764ba2);
        -webkit-background-clip: text;
        -webkit-text-fill-color: transparent;
      }
    }
    
    .refresh {
      font-size: 26rpx;
      color: #5B8FF9;
    }
  }
  
  .ai-reason {
    font-size: 26rpx;
    color: #666;
    margin-bottom: 24rpx;
    padding: 16rpx 20rpx;
    background: #F0F5FF;
    border-radius: 12rpx;
  }
  
  .recipe-scroll {
    white-space: nowrap;
    
    .recipe-card {
      display: inline-block;
      width: 300rpx;
      margin-right: 20rpx;
      border-radius: 16rpx;
      overflow: hidden;
      background: #f9f9f9;
      
      &.ai-card {
        border: 2rpx solid #5B8FF9;
        box-shadow: 0 4rpx 16rpx rgba(91, 143, 249, 0.2);
      }
      
      image {
        width: 100%;
        height: 200rpx;
      }
      
      .recipe-info {
        padding: 16rpx;
        
        .recipe-name {
          font-size: 28rpx;
          color: #333;
          font-weight: 500;
          display: block;
          margin-bottom: 12rpx;
        }
        
        .match-rate {
          margin-bottom: 12rpx;
          
          text {
            font-size: 22rpx;
            color: #5B8FF9;
            background: #F0F5FF;
            padding: 4rpx 12rpx;
            border-radius: 8rpx;
          }
        }
        
        .recipe-meta {
          display: flex;
          justify-content: space-between;
          
          text {
            font-size: 22rpx;
            color: #999;
          }
        }
      }
    }
  }
}

.recipe-section {
  background: #fff;
  padding: 30rpx;
  
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
    
    .filter-btns {
      display: flex;
      background: #f5f6fa;
      border-radius: 30rpx;
      padding: 4rpx;
      
      .filter-btn {
        padding: 12rpx 24rpx;
        font-size: 24rpx;
        color: #666;
        border-radius: 26rpx;
        
        &.active {
          background: #5B8FF9;
          color: #fff;
        }
      }
    }
  }
  
  .recipe-list {
    .recipe-item {
      display: flex;
      padding: 20rpx;
      background: #f9f9f9;
      border-radius: 16rpx;
      margin-bottom: 20rpx;
      
      image {
        width: 200rpx;
        height: 160rpx;
        border-radius: 12rpx;
        margin-right: 20rpx;
      }
      
      .item-info {
        flex: 1;
        display: flex;
        flex-direction: column;
        justify-content: space-between;
        
        .item-name {
          font-size: 30rpx;
          color: #333;
          font-weight: 500;
        }
        
        .item-tags {
          display: flex;
          gap: 12rpx;
          
          .tag {
            padding: 6rpx 16rpx;
            font-size: 22rpx;
            border-radius: 8rpx;
            
            &.difficulty {
              background: #E6F7FF;
              color: #1890ff;
            }
            
            &.time {
              background: #F6FFED;
              color: #52c41a;
            }
            
            &.calories {
              background: #FFF7E6;
              color: #fa8c16;
            }
          }
        }
        
        .item-stats {
          display: flex;
          gap: 20rpx;
          
          text {
            font-size: 24rpx;
            color: #999;
          }
        }
      }
    }
  }
}

.fab-menu {
  position: fixed;
  right: 40rpx;
  bottom: 160rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20rpx;
  z-index: 100;
  
  .fab-item {
    width: 90rpx;
    height: 90rpx;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 8rpx 24rpx rgba(0,0,0,0.15);
    
    &.scan {
      background: #5AD8A6;
    }
    
    &.add {
      width: 100rpx;
      height: 100rpx;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    }
  }
}
</style>
