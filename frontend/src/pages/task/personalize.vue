<template>
  <view class="page-container">
    <view class="header">
      <view class="header-left" @click="goBack"><text>‹</text></view>
      <view class="header-title">个性化首页</view>
    </view>
    
    <view class="widgets-section"
>
      <view class="section-title">我的组件</view>
      
      <view class="widgets-grid"
>
        <view v-for="w in widgets" :key="w.id" class="widget-card" :class="{ enabled: w.enabled }">
          <text class="widget-icon">{{ w.icon }}</text>
          <text class="widget-name">{{ w.name }}</text>
          <view class="widget-toggle" @click="toggleWidget(w)"
>
            <view class="toggle-circle" :class="{ on: w.enabled }"></view>
          </view>
        </view>
      </view>
    </view>
    
    <view class="theme-section"
>
      <view class="section-title">主题颜色</view>
      
      <view class="theme-colors"
>
        <view v-for="color in themes" :key="color" class="color-item" :style="{ background: color }" @click="setTheme(color)"
>
          <text v-if="currentTheme === color">✓</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'

const widgets = ref([
  { id: 1, name: '今日任务', icon: '📋', enabled: true },
  { id: 2, name: '习惯打卡', icon: '✓', enabled: true },
  { id: 3, name: '积分排行', icon: '🏆', enabled: false },
  { id: 4, name: '天气', icon: '🌤️', enabled: false }
])

const themes = ['#3B82F6', '#10B981', '#F59E0B', '#EF4444', '#8B5CF6', '#EC4899']
const currentTheme = ref('#3B82F6')

const toggleWidget = (w) => {
  w.enabled = !w.enabled
}

const setTheme = (color) => {
  currentTheme.value = color
  uni.showToast({ title: '主题已更换', icon: 'none' })
}

const goBack = () => uni.navigateBack()
</script>

<style lang="scss" scoped>
.page-container { min-height: 100vh; background: #F8FAFC; }
.header {
  display: flex; justify-content: space-between; align-items: center;
  padding: 50px 20px 20px;
  background: linear-gradient(135deg, #EC4899, #DB2777);
  .header-left { width: 40px; height: 40px; display: flex; align-items: center; justify-content: center; }
  .header-left text { font-size: 32px; color: #fff; }
  .header-title { font-size: 20px; font-weight: 600; color: #fff; }
}
.widgets-section { padding: 20px; }
.section-title { font-size: 16px; font-weight: 600; color: #1F2937; margin-bottom: 16px; display: block; }
.widgets-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px; }
.widget-card { background: #fff; border-radius: 16px; padding: 20px; display: flex; align-items: center; gap: 12px; opacity: 0.5; }
.widget-card.enabled { opacity: 1; box-shadow: 0 2px 8px rgba(0,0,0,0.06); }
.widget-icon { font-size: 24px; }
.widget-name { flex: 1; font-size: 15px; font-weight: 500; color: #374151; }
.widget-toggle { width: 44px; height: 24px; background: #E5E7EB; border-radius: 12px; padding: 2px; }
.widget-card.enabled .widget-toggle { background: #10B981; }
.toggle-circle { width: 20px; height: 20px; background: #fff; border-radius: 50%; transition: transform 0.3s; }
.toggle-circle.on { transform: translateX(20px); }
.theme-section { padding: 0 20px; }
.theme-colors { display: flex; gap: 12px; }
.color-item { width: 50px; height: 50px; border-radius: 50%; display: flex; align-items: center; justify-content: center; }
.color-item text { font-size: 20px; color: #fff; font-weight: bold; }
</style>
