<template>
  <view class="page-container">
    <view class="header">
      <view class="header-left" @click="goBack"><text>‹</text></view>
      <view class="header-title">WebRTC视频</view>
    </view>
    
    <view class="video-section"
>
      <view class="video-grid"
>
        <view v-for="user in users" :key="user.id" class="video-item"
>
          <view class="video-placeholder" :style="{ background: user.color }">
            <text>{{ user.name[0] }}</text>
          </view>
          <text class="user-name">{{ user.name }}</text>
        </view>
      </view>
    </view>
    
    <view class="controls-bar"
>
      <view class="control-btn" @click="toggleMic">🎤</view>
      <view class="control-btn" @click="toggleCam">📹</view>
      <view class="control-btn end" @click="endCall">📞</view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'

const users = ref([
  { id: 1, name: '我', color: '#3B82F6' },
  { id: 2, name: '妈妈', color: '#EC4899' }
])

const toggleMic = () => uni.showToast({ title: '切换麦克风', icon: 'none' })
const toggleCam = () => uni.showToast({ title: '切换摄像头', icon: 'none' })
const endCall = () => uni.navigateBack()

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
.video-section { padding: 20px; }
.video-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px; }
.video-item { aspect-ratio: 3/4; background: #374151; border-radius: 16px; overflow: hidden; position: relative; }
.video-placeholder { width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; }
.video-placeholder text { font-size: 40px; color: #fff; font-weight: 600; }
.user-name { position: absolute; bottom: 12px; left: 12px; font-size: 14px; color: #fff; background: rgba(0,0,0,0.5); padding: 4px 12px; border-radius: 12px; }
.controls-bar { position: fixed; bottom: 40px; left: 50%; transform: translateX(-50%); display: flex; gap: 20px; }
.control-btn { width: 60px; height: 60px; background: #4B5563; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 24px; }
.control-btn.end { background: #EF4444; }
</style>
