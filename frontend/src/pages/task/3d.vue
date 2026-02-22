<template>
  <view class="page-container">
    <view class="header">
      <view class="header-left" @click="goBack"><text>‹</text></view>
      <view class="header-title">3D展示</view>
    </view>
    
    <view class="three-d-container"
>
      <view class="placeholder-3d"
>
        <text class="icon-3d">🧊</text>
        <text class="text-3d">3D任务视图</text>
        <text class="hint-3d">旋转查看任务立方体</text>
      </view>
    </view>
    
    <view class="controls"
>
      <view class="control-btn" v-for="btn in controls" :key="btn" @click="rotate(btn)">
        {{ btn }}
      </view>
    </view>
  </view>
</template>

<script setup>
const controls = ['上', '下', '左', '右']

const rotate = (dir) => {
  uni.showToast({ title: `向${dir}旋转`, icon: 'none' })
}

const goBack = () => uni.navigateBack()
</script>

<style lang="scss" scoped>
.page-container { min-height: 100vh; background: #1F2937; }
.header {
  display: flex; justify-content: space-between; align-items: center;
  padding: 50px 20px 20px;
  .header-left { width: 40px; height: 40px; display: flex; align-items: center; justify-content: center; }
  .header-left text { font-size: 32px; color: #fff; }
  .header-title { font-size: 20px; font-weight: 600; color: #fff; }
}
.three-d-container { height: 400px; display: flex; align-items: center; justify-content: center; }
.placeholder-3d { text-align: center; }
.icon-3d { font-size: 100px; display: block; margin-bottom: 20px; }
.text-3d { font-size: 24px; color: #fff; font-weight: 600; display: block; margin-bottom: 8px; }
.hint-3d { font-size: 14px; color: #9CA3AF; }
.controls { display: flex; justify-content: center; gap: 20px; padding: 20px; }
.control-btn { width: 60px; height: 60px; background: #374151; border-radius: 50%; display: flex; align-items: center; justify-content: center; color: #fff; font-size: 14px; }
</style>
