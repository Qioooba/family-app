<template>
  <view class="page-container">
    <view class="header">
      <view class="header-left" @click="goBack"><text>‹</text></view>
      <view class="header-title">PWA安装</view>
    </view>
    
    <view class="pwa-content"
003e
      <view class="pwa-icon">📱</view>
      <text class="pwa-title">安装到主屏幕</text>
      <text class="pwa-desc">像原生应用一样使用，无需下载</text>
      
      <view class="features-list"
003e
        <view class="feature-item" v-for="f in features" :key="f">
          <text class="check">✓</text>
          <text>{{ f }}</text>
        </view>
      </view>
      
      <view class="install-btn" @click="installPWA"
003e
        <text>立即安装</text>
      </view>
    </view>
  </view>
</template>

<script setup>
const features = ['离线访问', '快速启动', '推送通知', '占用空间小']

const installPWA = () => {
  uni.showModal({
    title: '添加到主屏幕',
    content: '请使用浏览器菜单中的"添加到主屏幕"功能',
    showCancel: false
  })
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
.pwa-content { padding: 60px 40px; text-align: center; }
.pwa-icon { font-size: 80px; margin-bottom: 24px; }
.pwa-title { font-size: 24px; font-weight: 700; color: #1F2937; display: block; margin-bottom: 8px; }
.pwa-desc { font-size: 15px; color: #9CA3AF; display: block; margin-bottom: 40px; }
.features-list { text-align: left; margin-bottom: 40px; }
.feature-item { display: flex; align-items: center; gap: 12px; padding: 14px 0; border-bottom: 1px solid #F3F4F6; }
.feature-item:last-child { border-bottom: none; }
.feature-item .check { width: 24px; height: 24px; background: #10B981; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 12px; color: #fff; }
.feature-item text:last-child { font-size: 15px; color: #374151; }
.install-btn { height: 54px; background: linear-gradient(135deg, #6366F1, #4F46E5); border-radius: 27px; display: flex; align-items: center; justify-content: center; }
.install-btn text { font-size: 17px; font-weight: 600; color: #fff; }
</style>
