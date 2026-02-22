<template>
  <view class="page-container">
    <view class="header">
      <view class="header-left" @click="goBack"><text>‹</text></view>
      <view class="header-title">地理位置</view>
    </view>
    
    <view class="map-container">
      <view class="map-placeholder"
003e
        <text class="map-icon">🗺️</text>
        <text class="map-text">地图展示区域</text>
        <text class="map-hint">显示任务相关位置标记</text>
      </view>
    </view>
    
    <view class="location-list"
003e
      <view class="list-header">
        <text>位置任务</text>
        <text class="count">{{ locationTasks.length }}个</text>
      </view>
      
      <view v-for="task in locationTasks" :key="task.id" class="location-card"
003e
        <view class="location-marker"
003e📍</view>
        <view class="task-info"
003e
          <text class="task-title">{{ task.title }}</text>
          <text class="task-address">{{ task.address }}</text>
          <text class="task-distance">{{ task.distance }}</text>
        </view>
        <view class="navigate-btn" @click="navigate(task)"
003e导航</view>
      </view>
    </view>
    
    <view class="add-location-btn" @click="addLocationTask"
003e
      <text>+ 添加位置任务</text>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'

const locationTasks = ref([
  { id: 1, title: '超市购物', address: '沃尔玛超市', distance: '距离500米' },
  { id: 2, title: '取快递', address: '菜鸟驿站', distance: '距离200米' },
  { id: 3, title: '药店买药', address: '同仁堂药店', distance: '距离1.2公里' }
])

const navigate = (task) => {
  uni.openLocation({
    latitude: 39.9,
    longitude: 116.4,
    name: task.title,
    address: task.address
  })
}

const addLocationTask = () => {
  uni.showToast({ title: '添加位置功能开发中', icon: 'none' })
}

const goBack = () => uni.navigateBack()
</script>

<style lang="scss" scoped>
.page-container { min-height: 100vh; background: #F8FAFC; }
.header {
  display: flex; justify-content: space-between; align-items: center;
  padding: 50px 20px 20px;
  background: linear-gradient(135deg, #14B8A6, #0D9488);
  .header-left { width: 40px; height: 40px; display: flex; align-items: center; justify-content: center; }
  .header-left text { font-size: 32px; color: #fff; }
  .header-title { font-size: 20px; font-weight: 600; color: #fff; }
}
.map-container { height: 300px; background: #E5E7EB; }
.map-placeholder { height: 100%; display: flex; flex-direction: column; align-items: center; justify-content: center; }
.map-icon { font-size: 60px; margin-bottom: 12px; }
.map-text { font-size: 16px; color: #6B7280; margin-bottom: 4px; }
.map-hint { font-size: 13px; color: #9CA3AF; }
.location-list { padding: 20px; }
.list-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.list-header text { font-size: 16px; font-weight: 600; color: #1F2937; }
.list-header .count { font-size: 14px; color: #9CA3AF; font-weight: normal; }
.location-card { display: flex; align-items: center; gap: 12px; background: #fff; border-radius: 16px; padding: 16px; margin-bottom: 12px; }
.location-marker { font-size: 24px; }
.task-info { flex: 1; }
.task-title { font-size: 15px; font-weight: 600; color: #1F2937; display: block; margin-bottom: 4px; }
.task-address { font-size: 13px; color: #6B7280; display: block; margin-bottom: 2px; }
.task-distance { font-size: 12px; color: #14B8A6; }
.navigate-btn { padding: 8px 16px; background: #14B8A6; color: #fff; border-radius: 16px; font-size: 13px; }
.add-location-btn { margin: 20px; height: 50px; background: linear-gradient(135deg, #14B8A6, #0D9488); border-radius: 25px; display: flex; align-items: center; justify-content: center; }
.add-location-btn text { font-size: 16px; font-weight: 600; color: #fff; }
</style>
