<template>
  <view class="shopping-list">
    <view class="header">
      <text class="title">购物清单</text>
      <button class="btn-add" @click="addItem">+ 添加</button>
    </view>
    <view class="list">
      <view class="item" v-for="item in items" :key="item.id" @click="toggleItem(item)">
        <view class="checkbox" :class="{ checked: item.checked }"></view>
        <text class="name" :class="{ done: item.checked }">{{ item.name }}</text>
        <text class="quantity">x{{ item.quantity }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'

const items = ref([
  { id: 1, name: '牛奶', quantity: 2, checked: false },
  { id: 2, name: '鸡蛋', quantity: 1, checked: true },
  { id: 3, name: '面包', quantity: 1, checked: false }
])

const toggleItem = (item) => {
  item.checked = !item.checked
}

const addItem = () => {
  uni.showToast({ title: '功能开发中', icon: 'none' })
}
</script>

<style scoped>
.shopping-list {
  min-height: 100vh;
  background: #f5f5f5;
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 40rpx;
  background: #fff;
}
.title {
  font-size: 36rpx;
  font-weight: bold;
}
.btn-add {
  padding: 20rpx 40rpx;
  background: #5B8FF9;
  color: #fff;
  border-radius: 30rpx;
  font-size: 28rpx;
}
.list {
  padding: 20rpx;
}
.item {
  display: flex;
  align-items: center;
  padding: 30rpx;
  background: #fff;
  border-radius: 12rpx;
  margin-bottom: 20rpx;
}
.checkbox {
  width: 40rpx;
  height: 40rpx;
  border: 2rpx solid #ddd;
  border-radius: 50%;
  margin-right: 20rpx;
}
.checkbox.checked {
  background: #5B8FF9;
  border-color: #5B8FF9;
}
.name {
  flex: 1;
  font-size: 32rpx;
}
.name.done {
  text-decoration: line-through;
  color: #999;
}
.quantity {
  color: #999;
}
</style>