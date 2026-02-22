<template>
  <view class="nutrition-page">
    <!-- 导航栏 -->
    <view class="nav-bar">
      <view class="back-btn" @click="goBack">
        <u-icon name="arrow-left" size="40" color="#333"></u-icon>
      </view>
      <text class="title">营养成分查询</text>
      <view class="right-btn" @click="showScan">
        <u-icon name="scan" size="40" color="#333"></u-icon>
      </view>
    </view>

    <!-- 搜索栏 -->
    <view class="search-section">
      <view class="search-box">
        <u-icon name="search" size="36" color="#999"></u-icon>
        <input
          v-model="searchKey"
          placeholder="搜索食物，如：鸡蛋、苹果、米饭..."
          class="search-input"
          @confirm="search"
          focus
        />
        <view v-if="searchKey" class="clear-btn" @click="clearSearch">
          <u-icon name="close-circle" size="32" color="#999"></u-icon>
        </view>
      </view>
    </view>

    <!-- 搜索结果 -->
    <view v-if="searchResults.length > 0" class="search-results">
      <view class="results-header">
        <text>搜索结果</text>
        <text class="results-count">{{ searchResults.length }}个结果</text>
      </view>
      <view
        v-for="(food, index) in searchResults" :key="food.id || index"
        
        class="food-item"
        @click="showFoodDetail(food)"
      >
        <image :src="food.image || '/static/food/default.png'" mode="aspectFill" class="food-img" />
        <view class="food-info">
          <text class="food-name">{{ food.name }}</text>
          <text class="food-calories">{{ food.calories }} 千卡/100g</text>
        </view>
        <u-icon name="arrow-right" size="32" color="#ccc"></u-icon>
      </view>
    </view>

    <!-- 分类浏览 -->
    <view v-else class="browse-section">
      <!-- 分类标签 -->
      <view class="category-tabs">
        <scroll-view scroll-x class="tabs-scroll">
          <view
            v-for="(cat, index) in categories" :key="cat.id || index"
            
            class="tab-item"
            :class="{ active: currentCategory === cat.value }"
            @click="switchCategory(cat.value)"
          >
            <text>{{ cat.icon }} {{ cat.label }}</text>
          </view>
        </scroll-view>
      </view>

      <!-- 分类内容 -->
      <view class="category-content">
        <view class="foods-grid">
          <view
            v-for="(food, index) in categoryFoods" :key="food.id || index"
            
            class="food-card"
            @click="showFoodDetail(food)"
          >
            <image :src="food.image || '/static/food/default.png'" mode="aspectFill" class="card-img" />
            <text class="card-name">{{ food.name }}</text>
            <text class="card-calories">{{ food.calories }}卡</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 常用食物 -->
    <view v-if="!searchKey && favorites.length > 0" class="favorites-section">
      <view class="section-header">
        <text class="section-title">⭐ 常用食物</text>
      </view>
      <view class="favorites-list">
        <view
          v-for="(food, index) in favorites" :key="food.id || index"
          
          class="favorite-item"
          @click="showFoodDetail(food)"
        >
          <text class="fav-name">{{ food.name }}</text>
          <text class="fav-cal">{{ food.calories }}卡</text>
        </view>
      </view>
    </view>

    <!-- 营养详情弹窗 -->
    <view v-if="detailVisible" class="modal-overlay" @click="closeDetail">
      <view class="detail-modal" @click.stop>
        <view class="modal-header">
          <image
            :src="currentFood.image || '/static/food/default.png'"
            mode="aspectFill"
            class="detail-img"
          />
          <view class="header-info">
            <text class="detail-name">{{ currentFood.name }}</text>
            <text class="detail-calories">{{ currentFood.calories }} 千卡/100克</text>
          </view>
          <text class="close-btn" @click="closeDetail">✕</text>
        </view>

        <!-- 营养分布图 -->
        <view class="nutrition-chart">
          <text class="chart-title">营养分布</text>
          <view class="chart-content">
            <view class="pie-chart">
              <view
                class="pie-segment protein"
                :style="{ flex: currentFood.protein || 20 }"
              >
                <text>蛋白质</text>
              </view>
              <view
                class="pie-segment fat"
                :style="{ flex: currentFood.fat || 30 }"
              >
                <text>脂肪</text>
              </view>
              <view
                class="pie-segment carbs"
                :style="{ flex: currentFood.carbohydrates || 50 }"
              >
                <text>碳水</text>
              </view>
            </view>
          </view>
        </view>

        <!-- 详细营养数据 -->
        <view class="nutrition-details">
          <view class="detail-item">
            <text class="item-label">蛋白质</text>
            <view class="item-bar"
>
              <view class="bar-fill protein" :style="{ width: getPercent('protein') }"></view>
            </view>
            <text class="item-value">{{ currentFood.protein || 0 }}g</text>
          </view>

          <view class="detail-item">
            <text class="item-label">脂肪</text>
            <view class="item-bar">
              <view class="bar-fill fat" :style="{ width: getPercent('fat') }"></view>
            </view>
            <text class="item-value">{{ currentFood.fat || 0 }}g</text>
          </view>

          <view class="detail-item">
            <text class="item-label">碳水化合物</text>
            <view class="item-bar">
              <view class="bar-fill carbs" :style="{ width: getPercent('carbohydrates') }"></view>
            </view>
            <text class="item-value">{{ currentFood.carbohydrates || 0 }}g</text>
          </view>

          <view class="detail-item">
            <text class="item-label">膳食纤维</text>
            <text class="item-value plain">{{ currentFood.fiber || 0 }}g</text>
          </view>

          <view class="detail-item">
            <text class="item-label">维生素C</text>
            <text class="item-value plain">{{ currentFood.vitaminC || 0 }}mg</text>
          </view>

          <view class="detail-item">
            <text class="item-label">钙</text>
            <text class="item-value plain">{{ currentFood.calcium || 0 }}mg</text>
          </view>

          <view class="detail-item">
            <text class="item-label">铁</text>
            <text class="item-value plain">{{ currentFood.iron || 0 }}mg</text>
          </view>
        </view>

        <!-- 操作按钮 -->
        <view class="modal-actions">
          <button class="btn-fav" :class="{ active: isFavorite }" @click="toggleFavorite">
            <u-icon :name="isFavorite ? 'star-fill' : 'star'" size="32" :color="isFavorite ? '#FF6B6B' : '#999'"></u-icon>
            <text>{{ isFavorite ? '已收藏' : '收藏' }}</text>
          </button>
          <button class="btn-add" @click="addToRecord">添加到记录</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { dietApi } from '../../api/index.js'

const searchKey = ref('')
const searchResults = ref([])
const currentCategory = ref('common')
const detailVisible = ref(false)
const currentFood = ref({})
const favorites = ref([])

const categories = [
  { label: '常见', value: 'common', icon: '🍽️' },
  { label: '主食', value: 'staple', icon: '🍚' },
  { label: '肉类', value: 'meat', icon: '🥩' },
  { label: '蔬菜', value: 'vegetable', icon: '🥬' },
  { label: '水果', value: 'fruit', icon: '🍎' },
  { label: '饮品', value: 'drink', icon: '🥤' },
  { label: '零食', value: 'snack', icon: '🍪' },
  { label: '海鲜', value: 'seafood', icon: '🦐' }
]

// 模拟食物数据
const foodsData = {
  common: [
    { id: 1, name: '鸡蛋', calories: 144, protein: 13.3, fat: 8.8, carbohydrates: 2.8, fiber: 0, image: '' },
    { id: 2, name: '牛奶', calories: 54, protein: 3.0, fat: 3.2, carbohydrates: 3.4, fiber: 0, calcium: 104 },
    { id: 3, name: '米饭', calories: 116, protein: 2.6, fat: 0.3, carbohydrates: 25.9, fiber: 0.3 },
    { id: 4, name: '面包', calories: 265, protein: 8.4, fat: 3.5, carbohydrates: 50.6, fiber: 2.7 }
  ],
  staple: [
    { id: 5, name: '米饭', calories: 116, protein: 2.6, fat: 0.3, carbohydrates: 25.9, fiber: 0.3 },
    { id: 6, name: '面条', calories: 137, protein: 4.5, fat: 0.5, carbohydrates: 28.5, fiber: 1.2 },
    { id: 7, name: '馒头', calories: 223, protein: 7.0, fat: 1.1, carbohydrates: 47.0, fiber: 1.3 },
    { id: 8, name: '玉米', calories: 112, protein: 4.0, fat: 1.2, carbohydrates: 22.8, fiber: 2.7 }
  ],
  meat: [
    { id: 9, name: '鸡胸肉', calories: 133, protein: 19.4, fat: 5.0, carbohydrates: 2.5, fiber: 0, iron: 0.6 },
    { id: 10, name: '牛肉', calories: 125, protein: 20.0, fat: 4.2, carbohydrates: 2.0, fiber: 0, iron: 2.6 },
    { id: 11, name: '猪肉', calories: 143, protein: 20.0, fat: 6.2, carbohydrates: 0, fiber: 0, iron: 1.5 },
    { id: 12, name: '鸡蛋', calories: 144, protein: 13.3, fat: 8.8, carbohydrates: 2.8, fiber: 0 }
  ],
  vegetable: [
    { id: 13, name: '西兰花', calories: 34, protein: 2.8, fat: 0.4, carbohydrates: 7.0, fiber: 2.6, vitaminC: 89 },
    { id: 14, name: '番茄', calories: 18, protein: 0.9, fat: 0.2, carbohydrates: 3.9, fiber: 1.2, vitaminC: 14 },
    { id: 15, name: '黄瓜', calories: 16, protein: 0.7, fat: 0.1, carbohydrates: 3.6, fiber: 0.5 },
    { id: 16, name: '菠菜', calories: 23, protein: 2.9, fat: 0.4, carbohydrates: 3.6, fiber: 2.2, iron: 2.7 }
  ],
  fruit: [
    { id: 17, name: '苹果', calories: 52, protein: 0.3, fat: 0.2, carbohydrates: 13.8, fiber: 2.4, vitaminC: 4.6 },
    { id: 18, name: '香蕉', calories: 89, protein: 1.1, fat: 0.3, carbohydrates: 22.8, fiber: 2.6, vitaminC: 8.7 },
    { id: 19, name: '橙子', calories: 47, protein: 0.9, fat: 0.1, carbohydrates: 11.8, fiber: 2.4, vitaminC: 53.2 },
    { id: 20, name: '葡萄', calories: 69, protein: 0.7, fat: 0.2, carbohydrates: 18.1, fiber: 0.9, vitaminC: 3.2 }
  ],
  drink: [
    { id: 21, name: '牛奶', calories: 54, protein: 3.0, fat: 3.2, carbohydrates: 3.4, fiber: 0, calcium: 104 },
    { id: 22, name: '豆浆', calories: 31, protein: 2.4, fat: 1.6, carbohydrates: 1.8, fiber: 0.3, calcium: 10 },
    { id: 23, name: '酸奶', calories: 72, protein: 3.5, fat: 2.7, carbohydrates: 9.0, fiber: 0, calcium: 120 },
    { id: 24, name: '橙汁', calories: 45, protein: 0.7, fat: 0.2, carbohydrates: 10.4, fiber: 0.2, vitaminC: 50 }
  ],
  snack: [
    { id: 25, name: '坚果', calories: 607, protein: 20.0, fat: 54.0, carbohydrates: 21.0, fiber: 8.0 },
    { id: 26, name: '巧克力', calories: 546, protein: 4.9, fat: 31.0, carbohydrates: 61.0, fiber: 7.0 },
    { id: 27, name: '饼干', calories: 502, protein: 7.0, fat: 25.0, carbohydrates: 64.0, fiber: 2.0 },
    { id: 28, name: '薯片', calories: 536, protein: 7.0, fat: 35.0, carbohydrates: 53.0, fiber: 4.0 }
  ],
  seafood: [
    { id: 29, name: '虾', calories: 85, protein: 20.0, fat: 0.5, carbohydrates: 0, fiber: 0, iron: 0.5 },
    { id: 30, name: '三文鱼', calories: 208, protein: 20.0, fat: 13.0, carbohydrates: 0, fiber: 0 },
    { id: 31, name: '金枪鱼', calories: 144, protein: 30.0, fat: 1.0, carbohydrates: 0, fiber: 0 },
    { id: 32, name: '螃蟹', calories: 97, protein: 19.0, fat: 2.0, carbohydrates: 0, fiber: 0, iron: 1.8 }
  ]
}

const categoryFoods = computed(() => {
  return foodsData[currentCategory.value] || []
})

const isFavorite = computed(() => {
  return favorites.value.some(f => f.id === currentFood.value.id)
})

onMounted(() => {
  // 加载收藏
  const saved = uni.getStorageSync('favoriteFoods')
  if (saved) {
    favorites.value = JSON.parse(saved)
  }
})

const search = async () => {
  if (!searchKey.value.trim()) {
    searchResults.value = []
    return
  }

  // 模拟搜索
  const allFoods = Object.values(foodsData).flat()
  const results = allFoods.filter(food =>
    food.name.includes(searchKey.value)
  )
  searchResults.value = results
}

const clearSearch = () => {
  searchKey.value = ''
  searchResults.value = []
}

const switchCategory = (value) => {
  currentCategory.value = value
}

const showFoodDetail = (food) => {
  currentFood.value = food
  detailVisible.value = true
}

const closeDetail = () => {
  detailVisible.value = false
}

const getPercent = (key) => {
  const maxMap = { protein: 30, fat: 30, carbohydrates: 100 }
  const value = currentFood.value[key] || 0
  const max = maxMap[key] || 100
  return Math.min((value / max) * 100, 100) + '%'
}

const toggleFavorite = () => {
  const index = favorites.value.findIndex(f => f.id === currentFood.value.id)
  if (index > -1) {
    favorites.value.splice(index, 1)
    uni.showToast({ title: '已取消收藏', icon: 'none' })
  } else {
    favorites.value.push(currentFood.value)
    uni.showToast({ title: '已收藏', icon: 'success' })
  }
  uni.setStorageSync('favoriteFoods', JSON.stringify(favorites.value))
}

const addToRecord = () => {
  // 将食物添加到饮食记录
  const food = currentFood.value
  uni.showModal({
    title: '添加到记录',
    content: `将 ${food.name} 添加到今日饮食记录？`,
    success: (res) => {
      if (res.confirm) {
        // 这里可以调用dietApi.addRecord
        uni.showToast({ title: '添加成功', icon: 'success' })
        closeDetail()
      }
    }
  })
}

const showScan = () => {
  uni.showToast({ title: '扫码识别功能开发中', icon: 'none' })
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

  .back-btn, .right-btn {
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
}

.search-section {
  padding: 20rpx 30rpx;
  background: #fff;

  .search-box {
    display: flex;
    align-items: center;
    background: #f5f6fa;
    border-radius: 40rpx;
    padding: 0 30rpx;
    height: 80rpx;

    .search-input {
      flex: 1;
      margin-left: 16rpx;
      font-size: 28rpx;
    }

    .clear-btn {
      padding: 10rpx;
    }
  }
}

.search-results {
  background: #fff;
  padding: 0 30rpx;

  .results-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 20rpx 0;
    border-bottom: 1rpx solid #f0f0f0;

    text {
      font-size: 28rpx;
      color: #333;
      font-weight: 600;
    }

    .results-count {
      font-size: 24rpx;
      color: #999;
      font-weight: normal;
    }
  }

  .food-item {
    display: flex;
    align-items: center;
    padding: 24rpx 0;
    border-bottom: 1rpx solid #f0f0f0;

    .food-img {
      width: 100rpx;
      height: 100rpx;
      border-radius: 16rpx;
      background: #f5f6fa;
    }

    .food-info {
      flex: 1;
      margin-left: 20rpx;

      .food-name {
        display: block;
        font-size: 30rpx;
        color: #333;
        margin-bottom: 8rpx;
      }

      .food-calories {
        font-size: 24rpx;
        color: #999;
      }
    }
  }
}

.browse-section {
  .category-tabs {
    background: #fff;
    padding: 20rpx 0;

    .tabs-scroll {
      white-space: nowrap;
      padding: 0 20rpx;

      .tab-item {
        display: inline-block;
        padding: 16rpx 30rpx;
        margin-right: 16rpx;
        background: #f5f6fa;
        border-radius: 30rpx;

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
  }

  .category-content {
    padding: 20rpx 30rpx;

    .foods-grid {
      display: grid;
      grid-template-columns: repeat(4, 1fr);
      gap: 20rpx;

      .food-card {
        text-align: center;
        background: #fff;
        border-radius: 16rpx;
        padding: 20rpx 10rpx;

        .card-img {
          width: 100rpx;
          height: 100rpx;
          border-radius: 50%;
          background: #f5f6fa;
          margin-bottom: 12rpx;
        }

        .card-name {
          display: block;
          font-size: 26rpx;
          color: #333;
          margin-bottom: 4rpx;
        }

        .card-calories {
          font-size: 22rpx;
          color: #999;
        }
      }
    }
  }
}

.favorites-section {
  margin-top: 20rpx;
  padding: 0 30rpx;

  .section-header {
    margin-bottom: 20rpx;

    .section-title {
      font-size: 30rpx;
      font-weight: 600;
      color: #333;
    }
  }

  .favorites-list {
    display: flex;
    flex-wrap: wrap;
    gap: 16rpx;

    .favorite-item {
      background: #fff;
      border-radius: 30rpx;
      padding: 16rpx 30rpx;
      display: flex;
      align-items: center;
      gap: 16rpx;

      .fav-name {
        font-size: 26rpx;
        color: #333;
      }

      .fav-cal {
        font-size: 22rpx;
        color: #5AD8A6;
      }
    }
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
  align-items: flex-end;
  justify-content: center;
  z-index: 1000;
}

.detail-modal {
  width: 100%;
  background: #fff;
  border-radius: 30rpx 30rpx 0 0;
  max-height: 85vh;
  overflow-y: auto;

  .modal-header {
    display: flex;
    align-items: center;
    padding: 30rpx;
    border-bottom: 1rpx solid #f0f0f0;
    position: relative;

    .detail-img {
      width: 120rpx;
      height: 120rpx;
      border-radius: 20rpx;
      background: #f5f6fa;
      margin-right: 20rpx;
    }

    .header-info {
      flex: 1;

      .detail-name {
        display: block;
        font-size: 36rpx;
        font-weight: 600;
        color: #333;
        margin-bottom: 12rpx;
      }

      .detail-calories {
        font-size: 28rpx;
        color: #5AD8A6;
      }
    }

    .close-btn {
      position: absolute;
      top: 20rpx;
      right: 20rpx;
      font-size: 40rpx;
      color: #999;
      padding: 20rpx;
    }
  }

  .nutrition-chart {
    padding: 30rpx;

    .chart-title {
      display: block;
      font-size: 28rpx;
      font-weight: 600;
      color: #333;
      margin-bottom: 20rpx;
    }

    .chart-content {
      .pie-chart {
        display: flex;
        height: 60rpx;
        border-radius: 30rpx;
        overflow: hidden;

        .pie-segment {
          display: flex;
          align-items: center;
          justify-content: center;

          text {
            font-size: 22rpx;
            color: #fff;
          }

          &.protein {
            background: #5B8FF9;
          }

          &.fat {
            background: #F6BD16;
          }

          &.carbs {
            background: #5AD8A6;
          }
        }
      }
    }
  }

  .nutrition-details {
    padding: 0 30rpx 30rpx;

    .detail-item {
      display: flex;
      align-items: center;
      padding: 20rpx 0;
      border-bottom: 1rpx solid #f0f0f0;

      .item-label {
        width: 160rpx;
        font-size: 28rpx;
        color: #666;
      }

      .item-bar {
        flex: 1;
        height: 16rpx;
        background: #f0f0f0;
        border-radius: 8rpx;
        overflow: hidden;
        margin-right: 20rpx;

        .bar-fill {
          height: 100%;
          border-radius: 8rpx;
          transition: width 0.3s;

          &.protein {
            background: #5B8FF9;
          }

          &.fat {
            background: #F6BD16;
          }

          &.carbs {
            background: #5AD8A6;
          }
        }
      }

      .item-value {
        width: 100rpx;
        text-align: right;
        font-size: 28rpx;
        font-weight: 600;
        color: #333;

        &.plain {
          font-weight: normal;
          color: #666;
        }
      }
    }
  }

  .modal-actions {
    display: flex;
    gap: 20rpx;
    padding: 30rpx;

    button {
      flex: 1;
      height: 90rpx;
      border-radius: 45rpx;
      font-size: 30rpx;
      border: none;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 12rpx;
    }

    .btn-fav {
      background: #f5f6fa;
      color: #666;

      &.active {
        background: #FFF0F0;
        color: #FF6B6B;
      }
    }

    .btn-add {
      background: linear-gradient(135deg, #5AD8A6 0%, #3CB88E 100%);
      color: #fff;
    }
  }
}
</style>
