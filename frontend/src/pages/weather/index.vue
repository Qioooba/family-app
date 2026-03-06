<template>
  <view class="weather-page">
    <!-- 顶部背景 -->
    <view class="header-bg"></view>
    
    <!-- 头部 -->
    <view class="header-bar">
      <view class="location" @click="showCityPicker = true">
        <text class="location-icon">📍</text>
        <text class="location-name">{{ currentCity }}</text>
        <text class="location-arrow">▼</text>
      </view>
      <text class="update-time">{{ updateTime }}</text>
    </view>
    
    <!-- 当前天气 -->
    <view class="current-weather">
      <view class="weather-main">
        <view class="weather-icon-wrapper">
          <text class="weather-icon">{{ currentWeather.icon }}</text>
        </view>
        <view class="weather-info">
          <text class="temperature">{{ currentWeather.temperature }}°</text>
          <text class="weather-desc">{{ currentWeather.description }}</text>
          <view class="temp-range">
            <text>{{ currentWeather.tempMin }}° / {{ currentWeather.tempMax }}°</text>
          </view>
        </view>
      </view>
      
      <!-- 天气详情 -->
      <view class="weather-details">
        <view class="detail-item">
          <text class="detail-icon">💨</text>
          <text class="detail-label">风向</text>
          <text class="detail-value">{{ currentWeather.windDirection }} {{ currentWeather.windLevel }}级</text>
        </view>
        <view class="detail-item">
          <text class="detail-icon">💧</text>
          <text class="detail-label">湿度</text>
          <text class="detail-value">{{ currentWeather.humidity }}%</text>
        </view>
        <view class="detail-item">
          <text class="detail-icon">👁️</text>
          <text class="detail-label">能见度</text>
          <text class="detail-value">{{ currentWeather.visibility }}km</text>
        </view>
        <view class="detail-item">
          <text class="detail-icon">🔽</text>
          <text class="detail-label">气压</text>
          <text class="detail-value">{{ currentWeather.pressure }}hPa</text>
        </view>
      </view>
    </view>
    
    <!-- 生活指数 -->
    <view class="life-index section">
      <view class="section-header">
        <text class="section-title">生活指数</text>
      </view>
      
      <view class="index-grid">
        <view 
          v-for="(item, index) in lifeIndex" 
          :key="index"
          class="index-item"
          @click="showIndexDetail(item)"
        >
          <text class="index-icon">{{ item.icon }}</text>
          <text class="index-name">{{ item.name }}</text>
          <text class="index-level" :class="'level-' + item.level">{{ item.levelText }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const currentCity = ref('北京市')
const showCityPicker = ref(false)
const updateTime = ref('今天 08:30 更新')

const currentWeather = ref({
  temperature: 18,
  description: '多云',
  icon: '⛅',
  tempMin: 12,
  tempMax: 22,
  windDirection: '东南',
  windLevel: 3,
  humidity: 45,
  visibility: 10,
  pressure: 1013
})

const lifeIndex = ref([
  { name: '穿衣', icon: '👔', level: 2, levelText: '舒适' },
  { name: '紫外线', icon: '☀️', level: 3, levelText: '中等' },
  { name: '洗车', icon: '🚗', level: 1, levelText: '适宜' },
  { name: '运动', icon: '🏃', level: 2, levelText: '适宜' },
  { name: '感冒', icon: '🤧', level: 1, levelText: '少发' },
  { name: '钓鱼', icon: '🎣', level: 2, levelText: '适宜' }
])

const showIndexDetail = (item) => {
  uni.showToast({ title: item.name + ': ' + item.levelText, icon: 'none' })
}

onMounted(() => {
  console.log('天气页面加载')
})
</script>

<style lang="scss" scoped>
.weather-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #4facfe 0%, #00f2fe 100%);
  position: relative;
  padding-bottom: 40rpx;
}

.header-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 500rpx;
  background: linear-gradient(180deg, rgba(255,255,255,0.1) 0%, transparent 100%);
}

.header-bar {
  position: relative;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 100rpx 40rpx 40rpx;
  
  .location {
    display: flex;
    align-items: center;
    gap: 8rpx;
    
    .location-icon {
      font-size: 32rpx;
    }
    
    .location-name {
      font-size: 36rpx;
      font-weight: 600;
      color: #fff;
    }
    
    .location-arrow {
      font-size: 24rpx;
      color: rgba(255,255,255,0.8);
    }
  }
  
  .update-time {
    font-size: 24rpx;
    color: rgba(255,255,255,0.7);
  }
}

.current-weather {
  position: relative;
  padding: 40rpx;
  
  .weather-main {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 40rpx;
    margin-bottom: 60rpx;
    
    .weather-icon-wrapper {
      width: 160rpx;
      height: 160rpx;
      background: rgba(255,255,255,0.2);
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      
      .weather-icon {
        font-size: 100rpx;
      }
    }
    
    .weather-info {
      text-align: center;
      
      .temperature {
        font-size: 120rpx;
        font-weight: 200;
        color: #fff;
        line-height: 1;
      }
      
      .weather-desc {
        font-size: 36rpx;
        color: #fff;
        margin-top: 16rpx;
        display: block;
      }
      
      .temp-range {
        margin-top: 12rpx;
        
        text {
          font-size: 28rpx;
          color: rgba(255,255,255,0.8);
        }
      }
    }
  }
  
  .weather-details {
    display: flex;
    justify-content: space-around;
    background: rgba(255,255,255,0.15);
    border-radius: 32rpx;
    padding: 32rpx 20rpx;
    backdrop-filter: blur(10rpx);
    
    .detail-item {
      text-align: center;
      
      .detail-icon {
        font-size: 40rpx;
        display: block;
        margin-bottom: 8rpx;
      }
      
      .detail-label {
        font-size: 22rpx;
        color: rgba(255,255,255,0.7);
        display: block;
        margin-bottom: 4rpx;
      }
      
      .detail-value {
        font-size: 26rpx;
        color: #fff;
        font-weight: 500;
      }
    }
  }
}

.section {
  margin: 0 32rpx 32rpx;
  background: rgba(255,255,255,0.95);
  border-radius: 32rpx;
  padding: 32rpx;
  
  .section-header {
    margin-bottom: 24rpx;
    
    .section-title {
      font-size: 32rpx;
      font-weight: 600;
      color: #2d3748;
    }
  }
}

.life-index {
  .index-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 24rpx;
    
    .index-item {
      text-align: center;
      padding: 24rpx 0;
      background: #f8f9fc;
      border-radius: 20rpx;
      
      .index-icon {
        font-size: 48rpx;
        display: block;
        margin-bottom: 8rpx;
      }
      
      .index-name {
        font-size: 24rpx;
        color: #5a6c7d;
        display: block;
        margin-bottom: 8rpx;
      }
      
      .index-level {
        font-size: 24rpx;
        font-weight: 500;
        padding: 4rpx 16rpx;
        border-radius: 12rpx;
        display: inline-block;
        
        &.level-1 { background: rgba(104, 211, 145, 0.2); color: #68d391; }
        &.level-2 { background: rgba(246, 173, 85, 0.2); color: #f6ad55; }
        &.level-3 { background: rgba(252, 129, 129, 0.2); color: #fc8181; }
        &.level-4 { background: rgba(155, 89, 182, 0.2); color: #9B59B6; }
      }
    }
  }
}
</style>
