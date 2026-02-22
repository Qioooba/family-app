<template>
  <view class="page-container">
    <view class="header">
      <view class="header-left" @click="goBack"><text>‹</text></text>
      <view class="header-title">屏幕共享</view>
    </view>
    
    <view class="screen-preview"
>
      <view class="screen-content"
>
        <text class="screen-icon">🖥️</text>
        <text class="screen-text">屏幕共享预览</text>
        <text class="screen-status">{{ isSharing ? '共享中' : '未开始' }}</text>
      </view>
    </view>
    
    <view class="share-controls"
>
      <view class="control-btn" :class="{ active: isSharing }" @click="toggleShare"
>
        <text>{{ isSharing ? '停止共享' : '开始共享' }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'

const isSharing = ref(false)

const toggleShare = () => {
  isSharing.value = !isSharing.value
  uni.showToast({ title: isSharing.value ? '开始屏幕共享' : '已停止共享', icon: 'none' })
}

const goBack = () => uni.navigateBack()
</script>

<style lang="scss" scoped>
.page-container { min-height: 100vh; background: #F8FAFC; }
.header {
  display: flex; justify-content: space-between; align-items: center;
  padding: 50px 20px 20px;
  background: linear-gradient(135deg, #6366F1, #4F46E5);
  .header-left { width: 40px; height: 40px; display: flex; align-items: center; justify-content: center; }
  .header-left text { font-size: 32px; color: #fff; }
  .header-title { font-size: 20px; font-weight: 600; color: #fff; }
}
.screen-preview { margin: 20px; height: 400px; background: #1F2937; border-radius: 20px; display: flex; align-items: center; justify-content: center; }
.screen-content { text-align: center; }
.screen-icon { font-size: 80px; display: block; margin-bottom: 20px; }
.screen-text { font-size: 20px; color: #fff; font-weight: 600; display: block; margin-bottom: 12px; }
.screen-status { font-size: 14px; color: #9CA3AF; }
.share-controls { padding: 20px; }
.control-btn { height: 56px; background: #EF4444; border-radius: 28px; display: flex; align-items: center; justify-content: center; }
.control-btn.active { background: #10B981; }
.control-btn text { font-size: 17px; font-weight: 600; color: #fff; }
</style>
