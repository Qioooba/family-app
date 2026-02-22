<template>
  <view class="moments-create">
    <view class="header">
      <text class="title">发布动态</text>
    </view>
    <view class="content">
      <textarea class="input" placeholder="分享你的新鲜事..." v-model="content" />
      <view class="actions">
        <button class="btn-primary" @click="publish">发布</button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'

const content = ref('')

const publish = () => {
  if (!content.value.trim()) {
    uni.showToast({ title: '请输入内容', icon: 'none' })
    return
  }
  // TODO: 实现发布逻辑
  uni.showToast({ title: '发布成功', icon: 'success' })
  setTimeout(() => {
    uni.navigateBack()
  }, 1500)
}
</script>

<style scoped>
.moments-create {
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
.content {
  padding: 40rpx;
}
.input {
  width: 100%;
  height: 400rpx;
  padding: 20rpx;
  background: #fff;
  border-radius: 12rpx;
  box-sizing: border-box;
}
.actions {
  margin-top: 40rpx;
}
.btn-primary {
  background: #5B8FF9;
  color: #fff;
  border-radius: 40rpx;
}
</style>