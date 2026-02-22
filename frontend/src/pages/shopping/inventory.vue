<template>
  <view class="shopping-inventory">
    <view class="header">
      <text class="title">库存管理</text>
    </view>
    <view class="inventory-list">
      <view class="category" v-for="category in inventory" :key="category.name">
        <text class="category-title">{{ category.name }}</text>
        <view class="items">
          <view class="item" v-for="item in category.items" :key="item.id">
            <image class="icon" :src="item.icon" mode="aspectFit" />
            <view class="info">
              <text class="name">{{ item.name }}</text>
              <text class="stock" :class="{ low: item.stock < 5 }">库存: {{ item.stock }}</text>
            </view>
            <view class="actions">
              <button class="btn-minus" @click="updateStock(item, -1)">-</button>
              <button class="btn-plus" @click="updateStock(item, 1)">+</button>
            </view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'

const inventory = ref([
  {
    name: '食品',
    items: [
      { id: 1, name: '大米', icon: '/static/food.png', stock: 10 },
      { id: 2, name: '食用油', icon: '/static/food.png', stock: 3 },
      { id: 3, name: '盐', icon: '/static/food.png', stock: 2 }
    ]
  },
  {
    name: '日用品',
    items: [
      { id: 4, name: '洗衣液', icon: '/static/daily.png', stock: 5 },
      { id: 5, name: '纸巾', icon: '/static/daily.png', stock: 8 }
    ]
  }
])

const updateStock = (item, delta) => {
  item.stock = Math.max(0, item.stock + delta)
}
</script>

<style scoped>
.shopping-inventory {
  min-height: 100vh;
  background: #f5f5f5;
}
.header {
  padding: 40rpx;
  background: #fff;
}
.title {
  font-size: 36rpx;
  font-weight: bold;
}
.inventory-list {
  padding: 20rpx;
}
.category {
  margin-bottom: 30rpx;
}
.category-title {
  display: block;
  padding: 20rpx;
  font-size: 32rpx;
  font-weight: bold;
  color: #666;
}
.items {
  background: #fff;
  border-radius: 12rpx;
  overflow: hidden;
}
.item {
  display: flex;
  align-items: center;
  padding: 30rpx;
  border-bottom: 1rpx solid #eee;
}
.icon {
  width: 80rpx;
  height: 80rpx;
  margin-right: 20rpx;
}
.info {
  flex: 1;
}
.name {
  display: block;
  font-size: 32rpx;
}
.stock {
  font-size: 24rpx;
  color: #999;
}
.stock.low {
  color: #ff4d4f;
}
.actions {
  display: flex;
  gap: 20rpx;
}
.btn-minus, .btn-plus {
  width: 60rpx;
  height: 60rpx;
  line-height: 60rpx;
  text-align: center;
  border-radius: 50%;
  font-size: 32rpx;
  padding: 0;
}
.btn-minus {
  background: #f5f5f5;
  color: #666;
}
.btn-plus {
  background: #5B8FF9;
  color: #fff;
}
</style>