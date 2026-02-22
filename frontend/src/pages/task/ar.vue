<template>
  <view class="page-container">
    <view class="header">
      <view class="header-left" @click="goBack"><text>‹</text></view>
      <view class="header-title">AR增强现实</view>
    </view>
    
    <view class="ar-container"
003e
      <view class="camera-view"
003e
        <text class="ar-icon">📷</text>
        <text class="ar-text">AR相机视图</text>
        <text class="ar-hint">将任务标记放置在现实场景中</text>
      </view>
      
      <view class="ar-markers"
003e
        <view v-for="marker in markers" :key="marker.id" class="ar-marker" :style="{ left: marker.x + '%', top: marker.y + '%' }"
003e
          <text>📍</text>
        </view>
      </view>
    </view>
    
    <view class="ar-tasks"
003e
      <view class="section-title">附近的任务</view>
      
      <view v-for="task in nearbyTasks" :key="task.id" class="task-item"
003e
        <text class="task-icon">{{ task.icon }}</text>
        <view class="task-info"
003e
          <text class="task-title">{{ task.title }}</text>
          <text class="task-distance">{{ task.distance }}</text>
        </view>
        <view class="place-btn" @click="placeMarker(task)"
003e放置</view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'

const markers = ref([
  { id: 1, x: 30, y: 40 },
  { id: 2, x: 70, y: 60 }
])

const nearbyTasks = ref([
  { id: 1, title: '超市购物', icon: '🛒', distance: '50米' },
  { id: 2, title: '取快递', icon: '📦', distance: '100米' }
])

const placeMarker = (task) => {
  markers.value.push({
    id: Date.now(),
    x: 20 + Math.random() * 60,
    y: 20 + Math.random() * 60
  })
  uni.showToast({ title: '已放置标记', icon: 'none' })
}

const goBack = () => uni.navigateBack()
</script>

<style lang="scss" scoped>
.page-container { min-height: 100vh; background: #000; }
.header {
  display: flex; justify-content: space-between; align-items: center;
  padding: 50px 20px 20px;
  background: rgba(0,0,0,0.5);
  .header-left { width: 40px; height: 40px; display: flex; align-items: center; justify-content: center; }
  .header-left text { font-size: 32px; color: #fff; }
  .header-title { font-size: 20px; font-weight: 600; color: #fff; }
}
.ar-container { position: relative; height: 400px; background: linear-gradient(180deg, #1a1a2e, #16213e); }
.camera-view { position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%); text-align: center; }
.ar-icon { font-size: 60px; display: block; margin-bottom: 16px; }
.ar-text { font-size: 20px; color: #fff; font-weight: 600; display: block; margin-bottom: 8px; }
.ar-hint { font-size: 14px; color: #9CA3AF; }
.ar-markers { position: absolute; width: 100%; height: 100%; }
.ar-marker { position: absolute; font-size: 30px; }
.ar-tasks { position: fixed; bottom: 0; left: 0; right: 0; background: #fff; border-radius: 24px 24px 0 0; padding: 20px; }
.section-title { font-size: 16px; font-weight: 600; color: #1F2937; margin-bottom: 16px; display: block; }
.task-item { display: flex; align-items: center; gap: 12px; padding: 12px 0; border-bottom: 1px solid #F3F4F6; }
.task-item:last-child { border-bottom: none; }
.task-icon { font-size: 28px; }
.task-info { flex: 1; }
.task-title { font-size: 15px; font-weight: 600; color: #1F2937; display: block; }
.task-distance { font-size: 13px; color: #9CA3AF; }
.place-btn { padding: 8px 16px; background: #3B82F6; color: #fff; border-radius: 16px; font-size: 13px; }
</style>
