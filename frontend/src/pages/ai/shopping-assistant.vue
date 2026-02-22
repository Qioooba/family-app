<template>
  <view class="shopping-container">
    <!-- 自定义导航栏 -->
    <view class="custom-nav">
      <view class="nav-back" @click="goBack">
        <text class="icon">←</text>
      </view>
      <text class="nav-title">AI 购物助手</text>
      <view class="nav-right" @click="showSettings">
        <text class="icon">⚙️</text>
      </view>
    </view>

    <!-- 智能购物清单生成 -->
    <view class="section-card">
      <view class="section-header">
        <view class="header-left">
          <text class="section-icon">🤖</text>
          <text class="section-title">智能购物清单</text>
        </view>
        <view class="generate-btn" @click="generateList" :class="{ generating: isGenerating }">
          <text class="btn-icon">✨</text>
          <text class="btn-text">{{ isGenerating ? '生成中...' : '一键生成' }}</text>
        </view>
      </view>
      
      <view class="ai-input-area">
        <textarea 
          class="ai-input"
          v-model="aiPrompt"
          placeholder="描述你的需求，例如：周末想做三菜一汤，适合4口人，预算100元"
          :maxlength="200"
        />
        <text class="input-count">{{ aiPrompt.length }}/200</text>
      </view>

      <!-- 生成的清单 -->
      <view v-if="generatedList.length > 0" class="generated-list">
        <view class="list-header">
          <text class="list-title">📋 推荐清单</text>
          <text class="list-info">共 {{ generatedList.length }} 项 · 预估 ¥{{ totalEstimate }}</text>
        </view>
        <view 
          v-for="(item, index) in generatedList" 
          :key="index"
          class="list-item"
          :class="{ checked: item.checked }"
          @click="toggleItem(item)"
        >
          <view class="item-check">
            <text class="check-icon">{{ item.checked ? '✅' : '⭕' }}</text>
          </view>
          <view class="item-info">
            <text class="item-name">{{ item.name }}</text>
            <text class="item-reason">{{ item.reason }}</text>
          </view>
          <view class="item-meta">
            <text class="item-qty">x{{ item.quantity }}</text>
            <text class="item-price">¥{{ item.price }}</text>
          </view>
        </view>
        <view class="add-all-btn" @click="addAllToCart">
          <text>🛒 全部加入购物清单</text>
        </view>
      </view>
    </view>

    <!-- 库存不足提醒 -->
    <view class="section-card warning">
      <view class="section-header">
        <view class="header-left">
          <text class="section-icon">⚠️</text>
          <text class="section-title">库存不足提醒</text>
        </view>
        <text class="badge">{{ lowStockItems.length }}</text>
      </view>
      
      <view class="stock-list">
        <view 
          v-for="(item, index) in lowStockItems" 
          :key="index"
          class="stock-item"
        >
          <view class="stock-icon">{{ item.icon }}</view>
          <view class="stock-info">
            <text class="stock-name">{{ item.name }}</text>
            <view class="stock-bar">
              <view class="stock-progress" :style="{ width: item.percent + '%', background: item.color }"></view>
            </view>
            <text class="stock-status">剩余 {{ item.current }}/{{ item.max }} {{ item.unit }}</text>
          </view>
          <view class="stock-action" @click="quickAdd(item)">
            <text class="add-icon">➕</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 常用购物清单 -->
    <view class="section-card">
      <view class="section-header">
        <view class="header-left">
          <text class="section-icon">📦</text>
          <text class="section-title">常用清单模板</text>
        </view>
      </view>
      
      <scroll-view class="template-scroll" scroll-x>
        <view class="template-list">
          <view 
            v-for="(template, index) in templates" 
            :key="index"
            class="template-card"
            @click="useTemplate(template)"
          >
            <text class="template-icon">{{ template.icon }}</text>
            <text class="template-name">{{ template.name }}</text>
            <text class="template-count">{{ template.count }} 项商品</text>
          </view>
        </view>
      </scroll-view>
    </view>

    <!-- 购物清单 -->
    <view class="section-card">
      <view class="section-header">
        <view class="header-left">
          <text class="section-icon">🛍️</text>
          <text class="section-title">当前购物清单</text>
        </view>
        <text class="clear-text" @click="clearList">清空</text>
      </view>
      
      <view v-if="shoppingList.length === 0" class="empty-state">
        <text class="empty-icon">🛒</text>
        <text class="empty-text">购物清单为空</text>
        <text class="empty-sub">点击上方商品添加到清单</text>
      </view>
      
      <view v-else class="shopping-items">
        <view 
          v-for="(item, index) in shoppingList" 
          :key="index"
          class="shopping-item"
        >
          <view class="item-icon">{{ item.icon }}</view>
          <view class="item-detail">
            <text class="item-title">{{ item.name }}</text>
            <text class="item-note">{{ item.note }}</text>
          </view>
          <view class="item-controls">
            <view class="qty-control">
              <text class="qty-btn" @click="decreaseQty(index)">−</text>
              <text class="qty-num">{{ item.quantity }}</text>
              <text class="qty-btn" @click="increaseQty(index)">+</text>
            </view>
            <text class="remove-btn" @click="removeItem(index)">🗑️</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 底部结算栏 -->
    <view class="bottom-bar" v-if="shoppingList.length > 0">
      <view class="bar-info">
        <text class="total-label">共 {{ totalItems }} 件</text>
        <text class="total-price">¥{{ totalPrice }}</text>
      </view>
      <view class="checkout-btn" @click="goShopping">
        <text>去购买</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'

// AI 输入
const aiPrompt = ref('')
const isGenerating = ref(false)

// 生成的清单
const generatedList = ref([])

// 库存不足列表
const lowStockItems = ref([
  { icon: '🥛', name: '牛奶', current: 1, max: 10, unit: '盒', percent: 10, color: '#f44336' },
  { icon: '🥚', name: '鸡蛋', current: 3, max: 20, unit: '个', percent: 15, color: '#ff9800' },
  { icon: '🍚', name: '大米', current: 2, max: 10, unit: 'kg', percent: 20, color: '#ff9800' },
  { icon: '🧻', name: '卫生纸', current: 2, max: 12, unit: '卷', percent: 17, color: '#f44336' }
])

// 清单模板
const templates = ref([
  { icon: '🥬', name: '蔬菜生鲜', count: 12, items: ['西红柿', '黄瓜', '青菜', '鸡蛋'] },
  { icon: '🥩', name: '肉类采购', count: 8, items: ['鸡胸肉', '猪肉', '牛肉', '鱼肉'] },
  { icon: '🧴', name: '日用品', count: 10, items: ['洗发水', '牙膏', '纸巾', '洗衣液'] },
  { icon: '🍪', name: '零食饮料', count: 15, items: ['薯片', '可乐', '巧克力', '饼干'] },
  { icon: '🍼', name: '母婴用品', count: 6, items: ['奶粉', '纸尿裤', '湿巾'] }
])

// 购物清单
const shoppingList = ref([
  { icon: '🥛', name: '纯牛奶', note: '24盒装', quantity: 1, price: 59 },
  { icon: '🥚', name: '土鸡蛋', note: '30枚装', quantity: 2, price: 25 }
])

// 计算属性
const totalEstimate = computed(() => {
  return generatedList.value.reduce((sum, item) => sum + item.price * item.quantity, 0)
})

const totalItems = computed(() => {
  return shoppingList.value.reduce((sum, item) => sum + item.quantity, 0)
})

const totalPrice = computed(() => {
  return shoppingList.value.reduce((sum, item) => sum + item.price * item.quantity, 0)
})

// 生成智能清单
const generateList = () => {
  if (!aiPrompt.value.trim()) {
    uni.showToast({ title: '请输入需求描述', icon: 'none' })
    return
  }
  
  isGenerating.value = true
  
  // 模拟 AI 生成
  setTimeout(() => {
    generatedList.value = [
      { name: '五花肉', reason: '适合做红烧肉', quantity: 500, unit: 'g', price: 28, checked: false },
      { name: '土豆', reason: '搭配肉类的绝佳选择', quantity: 3, unit: '个', price: 6, checked: false },
      { name: '胡萝卜', reason: '增加菜品色彩和营养', quantity: 2, unit: '根', price: 4, checked: false },
      { name: '青椒', reason: '提升口感层次', quantity: 3, unit: '个', price: 5, checked: false },
      { name: '葱姜蒜套装', reason: '烹饪必备调料', quantity: 1, unit: '套', price: 8, checked: false },
      { name: '生抽', reason: '家中库存不足', quantity: 1, unit: '瓶', price: 12, checked: false }
    ]
    isGenerating.value = false
    uni.showToast({ title: '生成成功！', icon: 'success' })
  }, 1500)
}

// 切换选中状态
const toggleItem = (item) => {
  item.checked = !item.checked
}

// 全部加入购物车
const addAllToCart = () => {
  const items = generatedList.value.filter(item => !item.checked)
  items.forEach(item => {
    shoppingList.value.push({
      icon: '🛒',
      name: item.name,
      note: item.reason,
      quantity: Math.ceil(item.quantity / 100) || 1,
      price: item.price
    })
    item.checked = true
  })
  uni.showToast({ title: `已添加 ${items.length} 项`, icon: 'success' })
}

// 快速添加库存不足商品
const quickAdd = (item) => {
  shoppingList.value.push({
    icon: item.icon,
    name: item.name,
    note: `补货 - 剩余${item.current}${item.unit}`,
    quantity: Math.ceil((item.max - item.current) / 2),
    price: Math.floor(Math.random() * 30) + 10
  })
  uni.showToast({ title: '已添加到清单', icon: 'success' })
}

// 使用模板
const useTemplate = (template) => {
  template.items.forEach((name, index) => {
    shoppingList.value.push({
      icon: template.icon,
      name: name,
      note: `来自「${template.name}」模板`,
      quantity: 1,
      price: Math.floor(Math.random() * 20) + 5
    })
  })
  uni.showToast({ title: `已添加 ${template.name} 模板`, icon: 'success' })
}

// 数量控制
const increaseQty = (index) => {
  shoppingList.value[index].quantity++
}

const decreaseQty = (index) => {
  if (shoppingList.value[index].quantity > 1) {
    shoppingList.value[index].quantity--
  }
}

// 删除商品
const removeItem = (index) => {
  shoppingList.value.splice(index, 1)
}

// 清空清单
const clearList = () => {
  uni.showModal({
    title: '确认清空',
    content: '确定要清空购物清单吗？',
    success: (res) => {
      if (res.confirm) {
        shoppingList.value = []
      }
    }
  })
}

// 去购买
const goShopping = () => {
  uni.navigateTo({ url: '/pages/shopping/list' })
}

// 返回
const goBack = () => {
  uni.navigateBack()
}

// 设置
const showSettings = () => {
  uni.showToast({ title: '设置功能开发中', icon: 'none' })
}
</script>

<style lang="scss" scoped>
.shopping-container {
  min-height: 100vh;
  background: #f5f7fa;
  padding-bottom: 140rpx;
}

// 自定义导航栏
.custom-nav {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 80rpx 30rpx 20rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  
  .nav-back, .nav-right {
    width: 60rpx;
    height: 60rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    
    .icon {
      font-size: 36rpx;
      color: #fff;
    }
  }
  
  .nav-title {
    font-size: 36rpx;
    font-weight: 600;
    color: #fff;
  }
}

// 区块卡片
.section-card {
  margin: 20rpx;
  padding: 30rpx;
  background: #fff;
  border-radius: 24rpx;
  box-shadow: 0 4rpx 20rpx rgba(0,0,0,0.05);
  
  &.warning {
    border-left: 8rpx solid #ff9800;
  }
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;
  
  .header-left {
    display: flex;
    align-items: center;
    
    .section-icon {
      font-size: 40rpx;
      margin-right: 16rpx;
    }
    
    .section-title {
      font-size: 32rpx;
      font-weight: 600;
      color: #333;
    }
  }
  
  .badge {
    background: #ff5722;
    color: #fff;
    font-size: 24rpx;
    padding: 4rpx 16rpx;
    border-radius: 20rpx;
    font-weight: 600;
  }
  
  .clear-text {
    font-size: 26rpx;
    color: #999;
  }
}

// AI 输入区域
.ai-input-area {
  position: relative;
  
  .ai-input {
    width: 100%;
    height: 140rpx;
    padding: 20rpx;
    background: #f8f9fa;
    border-radius: 16rpx;
    font-size: 28rpx;
    box-sizing: border-box;
  }
  
  .input-count {
    position: absolute;
    bottom: 16rpx;
    right: 20rpx;
    font-size: 22rpx;
    color: #999;
  }
}

.generate-btn {
  display: flex;
  align-items: center;
  padding: 16rpx 28rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 30rpx;
  
  &.generating {
    opacity: 0.7;
  }
  
  .btn-icon {
    font-size: 28rpx;
    margin-right: 8rpx;
  }
  
  .btn-text {
    font-size: 26rpx;
    color: #fff;
    font-weight: 500;
  }
}

// 生成的清单
.generated-list {
  margin-top: 30rpx;
  padding-top: 30rpx;
  border-top: 2rpx solid #f0f0f0;
  
  .list-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20rpx;
    
    .list-title {
      font-size: 30rpx;
      font-weight: 600;
      color: #333;
    }
    
    .list-info {
      font-size: 24rpx;
      color: #667eea;
    }
  }
}

.list-item {
  display: flex;
  align-items: center;
  padding: 20rpx;
  background: #f8f9fa;
  border-radius: 16rpx;
  margin-bottom: 16rpx;
  
  &.checked {
    opacity: 0.5;
    
    .item-name {
      text-decoration: line-through;
    }
  }
  
  .item-check {
    margin-right: 20rpx;
    
    .check-icon {
      font-size: 40rpx;
    }
  }
  
  .item-info {
    flex: 1;
    
    .item-name {
      font-size: 30rpx;
      font-weight: 500;
      color: #333;
      margin-bottom: 8rpx;
    }
    
    .item-reason {
      font-size: 24rpx;
      color: #999;
    }
  }
  
  .item-meta {
    text-align: right;
    
    .item-qty {
      font-size: 24rpx;
      color: #666;
      margin-right: 12rpx;
    }
    
    .item-price {
      font-size: 32rpx;
      font-weight: 600;
      color: #ff5722;
    }
  }
}

.add-all-btn {
  text-align: center;
  padding: 24rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16rpx;
  margin-top: 20rpx;
  
  text {
    font-size: 30rpx;
    color: #fff;
    font-weight: 500;
  }
}

// 库存列表
.stock-list {
  .stock-item {
    display: flex;
    align-items: center;
    padding: 20rpx 0;
    border-bottom: 1rpx solid #f5f5f5;
    
    &:last-child {
      border-bottom: none;
    }
    
    .stock-icon {
      width: 72rpx;
      height: 72rpx;
      border-radius: 16rpx;
      background: #fff3e0;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 40rpx;
      margin-right: 20rpx;
    }
    
    .stock-info {
      flex: 1;
      
      .stock-name {
        font-size: 30rpx;
        font-weight: 500;
        color: #333;
        margin-bottom: 12rpx;
      }
      
      .stock-bar {
        height: 12rpx;
        background: #f0f0f0;
        border-radius: 6rpx;
        margin-bottom: 8rpx;
        overflow: hidden;
        
        .stock-progress {
          height: 100%;
          border-radius: 6rpx;
          transition: width 0.3s;
        }
      }
      
      .stock-status {
        font-size: 24rpx;
        color: #999;
      }
    }
    
    .stock-action {
      width: 64rpx;
      height: 64rpx;
      border-radius: 50%;
      background: #e8f5e9;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-left: 20rpx;
      
      .add-icon {
        font-size: 32rpx;
        color: #4caf50;
      }
    }
  }
}

// 模板列表
.template-scroll {
  white-space: nowrap;
}

.template-list {
  display: flex;
  gap: 20rpx;
}

.template-card {
  display: inline-flex;
  flex-direction: column;
  align-items: center;
  padding: 30rpx 40rpx;
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  border-radius: 20rpx;
  min-width: 180rpx;
  
  .template-icon {
    font-size: 48rpx;
    margin-bottom: 16rpx;
  }
  
  .template-name {
    font-size: 28rpx;
    font-weight: 600;
    color: #fff;
    margin-bottom: 8rpx;
  }
  
  .template-count {
    font-size: 24rpx;
    color: rgba(255,255,255,0.8);
  }
  
  &:nth-child(2) {
    background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  }
  
  &:nth-child(3) {
    background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
  }
  
  &:nth-child(4) {
    background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
  }
  
  &:nth-child(5) {
    background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%);
  }
}

// 空状态
.empty-state {
  text-align: center;
  padding: 60rpx 0;
  
  .empty-icon {
    font-size: 80rpx;
    margin-bottom: 20rpx;
  }
  
  .empty-text {
    font-size: 30rpx;
    color: #666;
    margin-bottom: 12rpx;
  }
  
  .empty-sub {
    font-size: 26rpx;
    color: #999;
  }
}

// 购物清单
.shopping-items {
  .shopping-item {
    display: flex;
    align-items: center;
    padding: 20rpx 0;
    border-bottom: 1rpx solid #f5f5f5;
    
    &:last-child {
      border-bottom: none;
    }
    
    .item-icon {
      width: 64rpx;
      height: 64rpx;
      border-radius: 50%;
      background: #e3f2fd;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 36rpx;
      margin-right: 20rpx;
    }
    
    .item-detail {
      flex: 1;
      
      .item-title {
        font-size: 30rpx;
        font-weight: 500;
        color: #333;
        margin-bottom: 8rpx;
      }
      
      .item-note {
        font-size: 24rpx;
        color: #999;
      }
    }
    
    .item-controls {
      display: flex;
      align-items: center;
      
      .qty-control {
        display: flex;
        align-items: center;
        background: #f5f5f5;
        border-radius: 30rpx;
        padding: 8rpx 16rpx;
        margin-right: 20rpx;
        
        .qty-btn {
          width: 40rpx;
          height: 40rpx;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 32rpx;
          color: #667eea;
          font-weight: 600;
        }
        
        .qty-num {
          font-size: 28rpx;
          color: #333;
          margin: 0 20rpx;
          min-width: 40rpx;
          text-align: center;
        }
      }
      
      .remove-btn {
        font-size: 36rpx;
        padding: 10rpx;
      }
    }
  }
}

// 底部结算栏
.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 30rpx 40rpx;
  background: #fff;
  box-shadow: 0 -4rpx 20rpx rgba(0,0,0,0.08);
  
  .bar-info {
    display: flex;
    align-items: baseline;
    
    .total-label {
      font-size: 26rpx;
      color: #999;
      margin-right: 20rpx;
    }
    
    .total-price {
      font-size: 44rpx;
      font-weight: 700;
      color: #ff5722;
    }
  }
  
  .checkout-btn {
    padding: 24rpx 60rpx;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 40rpx;
    
    text {
      font-size: 32rpx;
      font-weight: 600;
      color: #fff;
    }
  }
}
</style>