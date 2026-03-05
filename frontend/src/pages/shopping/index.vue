<template>
  <view class="shopping-page">
    <!-- 顶部搜索栏 -->
    <view class="search-bar">
      <view class="search-input-wrap">
        <text class="search-icon">🔍</text>
        <input 
          class="search-input" 
          v-model="keyword" 
          placeholder="搜索商品"
          @confirm="search"
        />
      </view>
    </view>
    
    <!-- 功能入口 -->
    <view class="quick-entry">
      <view class="entry-item" @click="goScan">
        <view class="entry-icon scan">
          <text>📷</text>
        </view>
        <text class="entry-text">扫码识别</text>
      </view>
      <view class="entry-item" @click="showAddModal = true">
        <view class="entry-icon add">
          <text>➕</text>
        </view>
        <text class="entry-text">手动添加</text>
      </view>
      <view class="entry-item" @click="goInventory">
        <view class="entry-icon inventory">
          <text>📦</text>
        </view>
        <text class="entry-text">库存管理</text>
      </view>
      <view class="entry-item" @click="goCoupon">
        <view class="entry-icon coupon">
          <text>🎫</text>
        </view>
        <text class="entry-text">优惠券</text>
      </view>
    </view>
    
    <!-- 购物清单 -->
    <view class="shopping-list">
      <view class="list-header">
        <text class="list-title">🛒 购物清单</text>
        <view class="list-actions">
          <text class="action-text" @click="clearCompleted">清空已完成</text>
        </view>
      </view>
      
      <view v-if="loading" class="loading-state">
        <text>加载中...</text>
      </view>
      
      <view v-else-if="shoppingList.length === 0" class="empty-state">
        <text class="empty-icon">🛒</text>
        <text class="empty-text">购物清单是空的</text>
        <text class="empty-sub">点击+添加商品</text>
      </view>
      
      <view v-else class="list-items">
        <view 
          v-for="item in shoppingList" 
          :key="item.id"
          class="shopping-item"
          :class="{ completed: item.status === 2 }"
          @click="toggleItem(item)"
        >
          <view class="item-checkbox">
            <view v-if="item.status === 2" class="checked">✓</view>
            <view v-else class="unchecked"></view>
          </view>
          
          <view class="item-info">
            <text class="item-name">{{ item.name }}</text>
            <view class="item-meta">
              <text v-if="item.quantity" class="item-quantity">x{{ item.quantity }}</text>
              <text v-if="item.expectedPrice" class="item-price">¥{{ item.expectedPrice }}</text>
            </view>
          </view>
          
          <view class="item-tags">
            <text v-if="item.isUrgent" class="tag urgent">急</text>
            <text v-if="item.isPromotion" class="tag promotion">促</text>
          </view>
          
          <view class="item-actions" @click.stop="showItemActions(item)">
            <text>⋮</text>
          </view>
        </view>
      </view>
      
      <!-- 统计 -->
      <view v-if="shoppingList.length > 0" class="list-stats">
        <view class="stats-item">
          <text class="stats-num">{{ pendingCount }}</text>
          <text class="stats-label">待购买</text>
        </view>
        <view class="stats-item">
          <text class="stats-num">{{ completedCount }}</text>
          <text class="stats-label">已完成</text>
        </view>
        <view class="stats-item">
          <text class="stats-num">¥{{ totalPrice }}</text>
          <text class="stats-label">预计总价</text>
        </view>
      </view>
    </view>
    
    <!-- 添加商品弹窗 -->
    <view v-if="showAddModal" class="modal-overlay" @click="showAddModal = false">
      <view class="modal-content" @click.stop>
        <view class="modal-header">
          <text class="modal-title">添加商品</text>
          <text class="modal-close" @click="showAddModal = false">✕</text>
        </view>
        
        <view class="modal-body">
          <view class="form-item">
            <text class="form-label">商品名称 *</text>
            <input class="form-input" v-model="newItem.name" placeholder="请输入商品名称" />
          </view>
          
          <view class="form-row">
            <view class="form-item">
              <text class="form-label">数量</text>
              <input class="form-input" type="number" v-model="newItem.quantity" placeholder="1" />
            </view>
            <view class="form-item">
              <text class="form-label">期望价格</text>
              <input class="form-input" type="number" v-model="newItem.expectedPrice" placeholder="0" />
            </view>
          </view>
          
          <view class="form-item">
            <text class="form-label">分类</text>
            <view class="category-selector">
              <view 
                v-for="cat in categories" 
                :key="cat.value"
                class="category-option"
                :class="{ active: newItem.category === cat.value }"
                @click="newItem.category = cat.value"
              >
                <text>{{ cat.icon }} {{ cat.label }}</text>
              </view>
            </view>
          </view>
          
          <view class="form-item">
            <text class="form-label">备注</text>
            <input class="form-input" v-model="newItem.remark" placeholder="备注信息" />
          </view>
          
          <view class="form-item tags">
            <view class="tag-option" :class="{ active: newItem.isUrgent }" @click="newItem.isUrgent = !newItem.isUrgent">
              <text>急</text>
            </view>
            <view class="tag-option" :class="{ active: newItem.isPromotion }" @click="newItem.isPromotion = !newItem.isPromotion">
              <text>促销</text>
            </view>
          </view>
        </view>
        
        <view class="modal-footer">
          <view class="cancel-btn" @click="showAddModal = false">
            <text>取消</text>
          </view>
          <view class="submit-btn" @click="addItem">
            <text>添加</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'

const loading = ref(false)
const keyword = ref('')
const showAddModal = ref(false)

const categories = [
  { value: 'food', label: '食品', icon: '🍎' },
  { value: 'fruit', label: '水果', icon: '🍌' },
  { value: 'vegetable', label: '蔬菜', icon: '🥬' },
  { value: 'drink', label: '饮料', icon: '🥤' },
  { value: 'snack', label: '零食', icon: '🍪' },
  { value: 'daily', label: '日用品', icon: '🧴' },
  { value: 'other', label: '其他', icon: '📦' }
]

const shoppingList = ref([
  { id: 1, name: '牛奶', quantity: 2, expectedPrice: 12, category: 'drink', status: 0, isUrgent: true },
  { id: 2, name: '面包', quantity: 1, expectedPrice: 8, category: 'food', status: 0, isPromotion: true },
  { id: 3, name: '苹果', quantity: 3, expectedPrice: 15, category: 'fruit', status: 2 },
  { id: 4, name: '洗衣液', quantity: 1, expectedPrice: 30, category: 'daily', status: 0 }
])

const newItem = ref({
  name: '',
  quantity: 1,
  expectedPrice: '',
  category: 'food',
  remark: '',
  isUrgent: false,
  isPromotion: false
})

const pendingCount = computed(() => shoppingList.value.filter(i => i.status === 0).length)
const completedCount = computed(() => shoppingList.value.filter(i => i.status === 2).length)
const totalPrice = computed(() => {
  return shoppingList.value
    .filter(i => i.status === 0)
    .reduce((sum, i) => sum + (i.expectedPrice || 0) * (i.quantity || 1), 0)
})

const search = () => {
  uni.showToast({ title: '搜索: ' + keyword.value, icon: 'none' })
}

const goScan = () => {
  uni.scanCode({
    success: (res) => {
      uni.showToast({ title: '扫码成功: ' + res.result, icon: 'none' })
    }
  })
}

const goInventory = () => {
  uni.navigateTo({ url: '/pages/shopping/inventory' })
}

const goCoupon = () => {
  uni.navigateTo({ url: '/pages/coupon/index' })
}

const toggleItem = (item) => {
  item.status = item.status === 2 ? 0 : 2
}

const clearCompleted = () => {
  shoppingList.value = shoppingList.value.filter(i => i.status !== 2)
}

const showItemActions = (item) => {
  uni.showActionSheet({
    itemList: ['编辑', '删除', item.status === 2 ? '标记未完成' : '标记完成'],
    success: (res) => {
      if (res.tapIndex === 1) {
        shoppingList.value = shoppingList.value.filter(i => i.id !== item.id)
      } else if (res.tapIndex === 2) {
        item.status = item.status === 2 ? 0 : 2
      }
    }
  })
}

const addItem = () => {
  if (!newItem.value.name.trim()) {
    uni.showToast({ title: '请输入商品名称', icon: 'none' })
    return
  }
  
  shoppingList.value.unshift({
    id: Date.now(),
    ...newItem.value,
    status: 0
  })
  
  showAddModal.value = false
  newItem.value = {
    name: '',
    quantity: 1,
    expectedPrice: '',
    category: 'food',
    remark: '',
    isUrgent: false,
    isPromotion: false
  }
  
  uni.showToast({ title: '添加成功', icon: 'success' })
}
</script>

<style lang="scss" scoped>
.shopping-page {
  min-height: 100vh;
  background: #f8f9fc;
}

.search-bar {
  background: linear-gradient(135deg, #667eea, #764ba2);
  padding: 100rpx 32rpx 32rpx;
  
  .search-input-wrap {
    display: flex;
    align-items: center;
    background: rgba(255,255,255,0.95);
    border-radius: 50rpx;
    padding: 20rpx 32rpx;
    
    .search-icon {
      font-size: 32rpx;
      margin-right: 16rpx;
    }
    
    .search-input {
      flex: 1;
      font-size: 28rpx;
    }
  }
}

.quick-entry {
  display: flex;
  justify-content: space-around;
  background: #fff;
  padding: 32rpx 0;
  margin-bottom: 20rpx;
  
  .entry-item {
    text-align: center;
    
    .entry-icon {
      width: 100rpx;
      height: 100rpx;
      border-radius: 24rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      margin: 0 auto 12rpx;
      font-size: 44rpx;
      
      &.scan { background: rgba(102, 126, 234, 0.1); }
      &.add { background: rgba(118, 75, 162, 0.1); }
      &.inventory { background: rgba(104, 211, 145, 0.1); }
      &.coupon { background: rgba(246, 173, 85, 0.1); }
    }
    
    .entry-text {
      font-size: 24rpx;
      color: #5a6c7d;
    }
  }
}

.shopping-list {
  margin: 0 32rpx;
  background: #fff;
  border-radius: 24rpx;
  padding: 32rpx;
  
  .list-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24rpx;
    
    .list-title {
      font-size: 32rpx;
      font-weight: 600;
      color: #2d3748;
    }
    
    .action-text {
      font-size: 26rpx;
      color: #8b9aad;
    }
  }
}

.empty-state {
  text-align: center;
  padding: 60rpx 0;
  
  .empty-icon {
    font-size: 100rpx;
    display: block;
    margin-bottom: 20rpx;
  }
  
  .empty-text {
    font-size: 30rpx;
    color: #2d3748;
    display: block;
  }
  
  .empty-sub {
    font-size: 26rpx;
    color: #8b9aad;
  }
}

.shopping-item {
  display: flex;
  align-items: center;
  padding: 24rpx 0;
  border-bottom: 2rpx solid #f1f5f9;
  
  &.completed {
    opacity: 0.6;
    
    .item-name {
      text-decoration: line-through;
      color: #8b9aad;
    }
  }
  
  .item-checkbox {
    margin-right: 20rpx;
    
    .checked {
      width: 44rpx;
      height: 44rpx;
      background: linear-gradient(135deg, #667eea, #764ba2);
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #fff;
      font-size: 24rpx;
    }
    
    .unchecked {
      width: 44rpx;
      height: 44rpx;
      border: 3rpx solid #ddd;
      border-radius: 50%;
    }
  }
  
  .item-info {
    flex: 1;
    
    .item-name {
      font-size: 30rpx;
      color: #2d3748;
      display: block;
      margin-bottom: 8rpx;
    }
    
    .item-meta {
      display: flex;
      gap: 16rpx;
      
      .item-quantity, .item-price {
        font-size: 24rpx;
        color: #8b9aad;
      }
    }
  }
  
  .item-tags {
    display: flex;
    gap: 8rpx;
    margin-right: 16rpx;
    
    .tag {
      padding: 4rpx 12rpx;
      border-radius: 8rpx;
      font-size: 20rpx;
      
      &.urgent { background: rgba(252, 129, 129, 0.1); color: #fc8181; }
      &.promotion { background: rgba(246, 173, 85, 0.1); color: #f6ad55; }
    }
  }
  
  .item-actions {
    padding: 10rpx;
    color: #8b9aad;
  }
}

.list-stats {
  display: flex;
  justify-content: space-around;
  margin-top: 32rpx;
  padding-top: 32rpx;
  border-top: 2rpx solid #f1f5f9;
  
  .stats-item {
    text-align: center;
    
    .stats-num {
      font-size: 36rpx;
      font-weight: 600;
      color: #667eea;
      display: block;
    }
    
    .stats-label {
      font-size: 24rpx;
      color: #8b9aad;
    }
  }
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
  align-items: flex-end;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: #fff;
  border-radius: 40rpx 40rpx 0 0;
  width: 100%;
  max-height: 80vh;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 40rpx;
  
  .modal-title {
    font-size: 36rpx;
    font-weight: 600;
  }
  
  .modal-close {
    font-size: 40rpx;
    color: #8b9aad;
  }
}

.modal-body {
  padding: 0 40rpx 40rpx;
  max-height: 60vh;
}

.form-item {
  margin-bottom: 32rpx;
  
  .form-label {
    font-size: 26rpx;
    color: #5a6c7d;
    display: block;
    margin-bottom: 12rpx;
  }
  
  .form-input {
    width: 100%;
    padding: 24rpx;
    background: #f8f9fc;
    border-radius: 20rpx;
    font-size: 30rpx;
  }
  
  &.tags {
    display: flex;
    gap: 16rpx;
    
    .tag-option {
      padding: 12rpx 24rpx;
      background: #f8f9fc;
      border-radius: 20rpx;
      font-size: 26rpx;
      
      &.active {
        background: rgba(102, 126, 234, 0.1);
        color: #667eea;
      }
    }
  }
}

.form-row {
  display: flex;
  gap: 24rpx;
  
  .form-item {
    flex: 1;
  }
}

.category-selector {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
  
  .category-option {
    padding: 12rpx 20rpx;
    background: #f8f9fc;
    border-radius: 16rpx;
    font-size: 24rpx;
    
    &.active {
      background: rgba(102, 126, 234, 0.1);
      color: #667eea;
    }
  }
}

.modal-footer {
  display: flex;
  gap: 24rpx;
  padding: 20rpx 40rpx 60rpx;
  
  .cancel-btn, .submit-btn {
    flex: 1;
    text-align: center;
    padding: 28rpx;
    border-radius: 32rpx;
    font-size: 30rpx;
  }
  
  .cancel-btn {
    background: #f1f5f9;
    color: #5a6c7d;
  }
  
  .submit-btn {
    background: linear-gradient(135deg, #667eea, #764ba2);
    color: #fff;
  }
}
</style>
