<template>
  <view class="page-container">
    <view class="header">
      <view class="header-left" @click="goBack"><text>‹</text></view>
      <view class="header-title">VR虚拟现实</view>
    </view>
    
    <view class="vr-container"
>
      <view class="vr-view"
>
        <text class="vr-icon">🥽</text>
        <text class="vr-text">VR沉浸式任务管理</text>
        <text class="vr-hint">请在VR设备中查看</text>
      </view>
    </view>
    
    <view class="vr-features"
>
      <view v-for="f in features" :key="f" class="feature-item"
>
        <text>✓ {{ f }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
const features = ['360度全景任务墙', '手势交互操作', '沉浸式专注模式', '虚拟协作空间']

const goBack = () => uni.navigateBack()
</script>

<style lang="scss" scoped>
.page-container { min-height: 100vh; background: #0f0f23; }
.header {
  display: flex; justify-content: space-between; align-items: center;
  padding: 50px 20px 20px;
  .header-left { width: 40px; height: 40px; display: flex; align-items: center; justify-content: center; }
  .header-left text { font-size: 32px; color: #fff; }
  .header-title { font-size: 20px; font-weight: 600; color: #fff; }
}
.vr-container { height: 400px; display: flex; align-items: center; justify-content: center; }
.vr-view { text-align: center; }
.vr-icon { font-size: 100px; display: block; margin-bottom: 20px; }
.vr-text { font-size: 24px; color: #fff; font-weight: 600; display: block; margin-bottom: 12px; }
.vr-hint { font-size: 14px; color: #00d4ff; }
.vr-features { padding: 20px; }
.feature-item { padding: 16px; background: rgba(255,255,255,0.05); border-radius: 12px; margin-bottom: 12px; }
.feature-item text { font-size: 15px; color: #fff; }
</style>
