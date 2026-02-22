<template>
  <view class="page-container">
    <view class="header">
      <view class="header-left" @click="goBack"><text>‹</text></text>
      <view class="header-title">天气关联</view>
    </view>
    
    <view class="weather-card">
      <view class="weather-main"
>
        <text class="weather-icon">{{ weather.icon }}</text>
        <view class="weather-info"
>
          <text class="temperature">{{ weather.temp }}°C</text>
          <text class="condition">{{ weather.condition }}</text>
        </view>
      </view>
      
      <view class="weather-details"
>
        <view class="detail-item"
>
          <text class="label">湿度</text>
          <text class="value">{{ weather.humidity }}%</text>
        </view>
        <view class="detail-item"
>
          <text class="label">风速</text>
          <text class="value">{{ weather.wind }}级</text>
        </view>
        <view class="detail-item"
>
          <text class="label">空气质量</text>
          <text class="value">{{ weather.aqi }}</text>
        </view>
      </view>
    </view>
    
    <view class="weather-tasks"
>
      <view class="section-title">🌤️ 天气相关任务</view>
      
      <view v-for="task in weatherTasks" :key="task.id" class="task-card" :class="task.type"
>
        <view class="task-icon">{{ task.icon }}</view>
        <view class="task-content"
>
          <text class="task-title">{{ task.title }}</text>
          <text class="task-desc">{{ task.description }}</text>
        </view>
        <view class="task-action" @click="handleTask(task)"
>
          <text>{{ task.action }}</text>
        </view>
      </view>
    </view>
    
    <view class="forecast-section"
>
      <view class="section-title">📅 未来天气</view>
      
      <view class="forecast-list"
>
        <view v-for="day in forecast" :key="day.date" class="forecast-item"
>
          <text class="day">{{ day.day }}</text>
          <text class="icon">{{ day.icon }}</text>
          <text class="temp">{{ day.temp }}°</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'

const weather = ref({
  icon: '☀️',
  temp: 25,
  condition: '晴',
  humidity: 45,
  wind: 3,
  aqi: '良'
})

const weatherTasks = ref([
  { id: 1, icon: '☂️', title: '带伞提醒', description: '今天有雨，记得带伞', action: '设置', type: 'rain' },
  { id: 2, icon: '🧥', title: '穿衣建议', description: '今天降温，注意保暖', action: '查看', type: 'cold' },
  { id: 3, icon: '🚗', title: '洗车提醒', description: '未来三天无雨，适合洗车', action: '添加', type: 'sunny' },
  { id: 4, icon: '🏃', title: '运动建议', description: '空气质量良好，适合户外运动', action: '创建', type: 'sport' }
])

const forecast = ref([
  { day: '明天', icon: '⛅', temp: '22-28' },
  { day: '后天', icon: '🌧️', temp: '18-24' },
  { day: '周四', icon: '☁️', temp: '20-26' },
  { day: '周五', icon: '☀️', temp: '23-30' }
])

const handleTask = (task) => {
  uni.showToast({ title: `${task.title}已${task.action}`, icon: 'none' })
}

const goBack = () => uni.navigateBack()
</script>

<style lang="scss" scoped>
.page-container { min-height: 100vh; background: #F8FAFC; }
.header {
  display: flex; justify-content: space-between; align-items: center;
  padding: 50px 20px 20px;
  background: linear-gradient(135deg, #3B82F6, #2563EB);
  .header-left { width: 40px; height: 40px; display: flex; align-items: center; justify-content: center; }
  .header-left text { font-size: 32px; color: #fff; }
  .header-title { font-size: 20px; font-weight: 600; color: #fff; }
}
.weather-card { background: linear-gradient(135deg, #3B82F6, #2563EB); margin: -20px 15px 15px; padding: 20px; border-radius: 20px; color: #fff; }
.weather-main { display: flex; align-items: center; gap: 20px; margin-bottom: 20px; }
.weather-icon { font-size: 60px; }
.temperature { font-size: 40px; font-weight: 700; display: block; }
.condition { font-size: 16px; opacity: 0.9; }
.weather-details { display: flex; justify-content: space-around; padding-top: 16px; border-top: 1px solid rgba(255,255,255,0.2); }
.detail-item { text-align: center; }
.detail-item .label { font-size: 12px; opacity: 0.8; display: block; margin-bottom: 4px; }
.detail-item .value { font-size: 16px; font-weight: 600; }
.weather-tasks { padding: 0 15px 15px; }
.section-title { font-size: 16px; font-weight: 600; color: #1F2937; margin-bottom: 12px; padding: 0 5px; }
.task-card { display: flex; align-items: center; gap: 12px; background: #fff; border-radius: 16px; padding: 16px; margin-bottom: 10px; border-left: 4px solid #3B82F6; }
.task-card.rain { border-color: #3B82F6; }
.task-card.cold { border-color: #8B5CF6; }
.task-card.sunny { border-color: #F59E0B; }
.task-card.sport { border-color: #10B981; }
.task-icon { font-size: 28px; }
.task-content { flex: 1; }
.task-title { font-size: 15px; font-weight: 600; color: #1F2937; display: block; margin-bottom: 4px; }
.task-desc { font-size: 12px; color: #6B7280; }
.task-action { padding: 6px 14px; background: #DBEAFE; color: #2563EB; border-radius: 14px; font-size: 12px; font-weight: 500; }
.forecast-section { padding: 0 15px; }
.forecast-list { display: flex; gap: 10px; }
.forecast-item { flex: 1; background: #fff; border-radius: 16px; padding: 16px; text-align: center; }
.forecast-item .day { font-size: 13px; color: #6B7280; display: block; margin-bottom: 8px; }
.forecast-item .icon { font-size: 28px; display: block; margin-bottom: 8px; }
.forecast-item .temp { font-size: 14px; font-weight: 600; color: #1F2937; }
</style>
