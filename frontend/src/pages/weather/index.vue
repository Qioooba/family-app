<template>
  <view class="weather-page">
    <!-- 顶部背景 -->
    <view class="header-bg"></view>
    
    <!-- 加载状态 -->
    <view v-if="isLoading" class="loading-mask">
      <view class="loading-spinner"></view>
      <text class="loading-text">获取天气中...</text>
    </view>
    
    <!-- 头部 -->
    <view class="header-bar">
      <view class="location" @click="showCityPicker = true">
        <text class="location-icon">📍</text>
        <text class="location-name">{{ currentCity }}</text>
        <text class="location-arrow">▼</text>
      </view>
      <view class="header-actions">
        <text class="update-time">{{ updateTime }}</text>
        <text class="refresh-btn" @click="refreshWeather">🔄</text>
      </view>
    </view>
    
    <!-- 当前天气 -->
    <view class="current-weather">
      <view class="weather-main">
        <view class="weather-icon-wrapper" :class="{ 'night': !currentWeather.isDay }">
          <text class="weather-icon">{{ currentWeather.icon }}</text>
        </view>
        <view class="weather-info">
          <text class="temperature">{{ currentWeather.temperature }}°</text>
          <text class="weather-desc">{{ currentWeather.description }}</text>
          <view class="temp-range">
            <text>{{ currentWeather.tempMin }}° / {{ currentWeather.tempMax }}°</text>
            <text class="feels-like">体感 {{ currentWeather.feelsLike }}°</text>
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
          <text class="detail-label">气压</text>
          <text class="detail-value">{{ currentWeather.pressure }}hPa</text>
        </view>
        <view class="detail-item">
          <text class="detail-icon">☁️</text>
          <text class="detail-label">云量</text>
          <text class="detail-value">{{ currentWeather.cloudCover || '--' }}%</text>
        </view>
      </view>
    </view>
    
    <!-- 未来预报 -->
    <view class="forecast-section section" v-if="forecast.length > 0">
      <view class="section-header">
        <text class="section-icon">📅</text>
        <text class="section-title">未来预报</text>
      </view>
      
      <view class="forecast-list">
        <view 
          v-for="(item, index) in forecast" 
          :key="index"
          class="forecast-item"
          :class="{ 'today': index === 0 }"
        >
          <text class="forecast-date">{{ item.date }}</text>
          <view class="forecast-weather">
            <text class="forecast-icon">{{ item.icon }}</text>
            <text class="forecast-desc">{{ item.description }}</text>
          </view>
          <view class="forecast-temp">
            <text class="temp-max">{{ item.tempMax }}°</text>
            <text class="temp-min">{{ item.tempMin }}°</text>
          </view>
        </view>
      </view>
    </view>
    
    <!-- 生活指数 -->
    <view class="life-index section">
      <view class="section-header">
        <text class="section-icon">📊</text>
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
    
    <!-- 城市选择弹窗 -->
    <view v-if="showCityPicker" class="city-picker-overlay" @click="showCityPicker = false">
      <view class="city-picker" @click.stop>
        <view class="picker-header">
          <text class="picker-title">选择城市</text>
          <text class="close-btn" @click="showCityPicker = false">✕</text>
        </view>
        
        <view class="search-box">
          <text class="search-icon">🔍</text>
          <input 
            class="search-input" 
            v-model="searchKeyword"
            placeholder="搜索城市"
            @input="onSearchInput"
          />
        </view>
        
        <scroll-view scroll-y class="city-list">
          <!-- 搜索结果 -->
          <view v-if="searchResults.length > 0" class="search-results">
            <view 
              v-for="city in searchResults" 
              :key="city.name + city.latitude"
              class="city-option"
              @click="selectCity(city)"
            >
              <text class="city-name">{{ city.name }}</text>
              <text class="city-region">{{ city.admin1 }} {{ city.country }}</text>
            </view>
          </view>
          
          <!-- 默认提示 -->
          <view v-else-if="searchKeyword" class="no-result">
            <text>未找到城市</text>
          </view>
          
          <!-- 当前位置 -->
          <view v-else class="current-location">
            <view class="location-btn" @click="showCityPicker = false; loadWeatherData()">
              <text class="location-icon">📍</text>
              <text>使用当前位置</text>
            </view>
          </view>
        </scroll-view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { onMounted } from 'vue'
import {
  currentCity,
  isLoading,
  updateTime,
  currentWeather,
  forecast,
  lifeIndex,
  searchKeyword,
  searchResults,
  showCityPicker,
  onSearchInput,
  selectCity,
  refreshWeather,
  showIndexDetail,
  loadWeatherData
} from './useWeather'

onMounted(() => {
  loadWeatherData()
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

// 加载遮罩
.loading-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.3);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  z-index: 100;
  
  .loading-spinner {
    width: 80rpx;
    height: 80rpx;
    border: 6rpx solid rgba(255,255,255,0.3);
    border-top-color: #fff;
    border-radius: 50%;
    animation: spin 1s linear infinite;
  }
  
  .loading-text {
    margin-top: 20rpx;
    color: #fff;
    font-size: 28rpx;
  }
}

@keyframes spin {
  to { transform: rotate(360deg); }
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
    padding: 12rpx 20rpx;
    background: rgba(255,255,255,0.2);
    border-radius: 40rpx;
    backdrop-filter: blur(10rpx);
    
    .location-icon {
      font-size: 32rpx;
    }
    
    .location-name {
      font-size: 32rpx;
      font-weight: 600;
      color: #fff;
      max-width: 200rpx;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
    
    .location-arrow {
      font-size: 24rpx;
      color: rgba(255,255,255,0.8);
    }
  }
  
  .header-actions {
    display: flex;
    align-items: center;
    gap: 20rpx;
    
    .update-time {
      font-size: 24rpx;
      color: rgba(255,255,255,0.7);
    }
    
    .refresh-btn {
      font-size: 36rpx;
      padding: 10rpx;
      opacity: 0.9;
      
      &:active {
        transform: rotate(180deg);
        transition: transform 0.3s;
      }
    }
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
      width: 180rpx;
      height: 180rpx;
      background: linear-gradient(135deg, rgba(255,200,100,0.9), rgba(255,150,50,0.9));
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      box-shadow: 0 20rpx 60rpx rgba(255,150,50,0.4);
      
      &.night {
        background: linear-gradient(135deg, rgba(100,120,200,0.9), rgba(60,80,150,0.9));
        box-shadow: 0 20rpx 60rpx rgba(60,80,150,0.4);
      }
      
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
        text-shadow: 0 4rpx 20rpx rgba(0,0,0,0.1);
      }
      
      .weather-desc {
        font-size: 36rpx;
        color: #fff;
        margin-top: 16rpx;
        display: block;
        font-weight: 500;
      }
      
      .temp-range {
        margin-top: 16rpx;
        display: flex;
        flex-direction: column;
        gap: 8rpx;
        
        text {
          font-size: 28rpx;
          color: rgba(255,255,255,0.9);
        }
        
        .feels-like {
          font-size: 24rpx;
          color: rgba(255,255,255,0.7);
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
    border: 1rpx solid rgba(255,255,255,0.1);
    
    .detail-item {
      text-align: center;
      flex: 1;
      
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
  box-shadow: 0 8rpx 32rpx rgba(0,0,0,0.1);
  
  .section-header {
    display: flex;
    align-items: center;
    margin-bottom: 24rpx;
    
    .section-icon {
      font-size: 32rpx;
      margin-right: 12rpx;
    }
    
    .section-title {
      font-size: 32rpx;
      font-weight: 600;
      color: #2d3748;
    }
  }
}

// 预报区域
.forecast-section {
  .forecast-list {
    .forecast-item {
      display: flex;
      align-items: center;
      padding: 20rpx 0;
      border-bottom: 1rpx solid #f0f0f0;
      
      &:last-child {
        border-bottom: none;
      }
      
      &.today {
        background: linear-gradient(90deg, rgba(79,172,254,0.1), transparent);
        margin: 0 -32rpx;
        padding: 20rpx 32rpx;
        border-radius: 16rpx;
      }
      
      .forecast-date {
        width: 100rpx;
        font-size: 28rpx;
        color: #5a6c7d;
        font-weight: 500;
      }
      
      .forecast-weather {
        flex: 1;
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 12rpx;
        
        .forecast-icon {
          font-size: 40rpx;
        }
        
        .forecast-desc {
          font-size: 26rpx;
          color: #5a6c7d;
        }
      }
      
      .forecast-temp {
        width: 140rpx;
        display: flex;
        justify-content: flex-end;
        gap: 16rpx;
        
        .temp-max {
          font-size: 28rpx;
          color: #2d3748;
          font-weight: 600;
        }
        
        .temp-min {
          font-size: 28rpx;
          color: #8b9aad;
        }
      }
    }
  }
}

// 生活指数
.life-index {
  .index-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 20rpx;
    
    .index-item {
      text-align: center;
      padding: 24rpx 0;
      background: #f8f9fc;
      border-radius: 20rpx;
      transition: all 0.2s;
      
      &:active {
        transform: scale(0.95);
        background: #e8f4ff;
      }
      
      .index-icon {
        font-size: 44rpx;
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
        font-weight: 600;
        padding: 6rpx 16rpx;
        border-radius: 12rpx;
        display: inline-block;
        
        &.level-1 { background: rgba(104, 211, 145, 0.2); color: #38a169; }
        &.level-2 { background: rgba(246, 173, 85, 0.2); color: #d69e2e; }
        &.level-3 { background: rgba(252, 129, 129, 0.2); color: #e53e3e; }
        &.level-4 { background: rgba(155, 89, 182, 0.2); color: #805ad5; }
      }
    }
  }
}

// 城市选择弹窗
.city-picker-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.5);
  z-index: 1000;
  display: flex;
  align-items: flex-end;
}

.city-picker {
  width: 100%;
  height: 70vh;
  background: #fff;
  border-radius: 40rpx 40rpx 0 0;
  display: flex;
  flex-direction: column;
  animation: slideUp 0.3s ease;
  
  @keyframes slideUp {
    from { transform: translateY(100%); }
    to { transform: translateY(0); }
  }
  
  .picker-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 32rpx;
    border-bottom: 1rpx solid #f0f0f0;
    
    .picker-title {
      font-size: 34rpx;
      font-weight: 600;
      color: #2d3748;
    }
    
    .close-btn {
      font-size: 32rpx;
      color: #8b9aad;
      padding: 10rpx;
    }
  }
  
  .search-box {
    margin: 24rpx 32rpx;
    display: flex;
    align-items: center;
    background: #f5f7fa;
    border-radius: 16rpx;
    padding: 20rpx 24rpx;
    
    .search-icon {
      font-size: 28rpx;
      color: #8b9aad;
      margin-right: 16rpx;
    }
    
    .search-input {
      flex: 1;
      font-size: 30rpx;
      color: #2d3748;
    }
  }
  
  .city-list {
    flex: 1;
    padding: 0 32rpx;
    
    .search-results {
      .city-option {
        padding: 28rpx 0;
        border-bottom: 1rpx solid #f5f7fa;
        
        &:active {
          background: #f5f7fa;
        }
        
        .city-name {
          font-size: 32rpx;
          color: #2d3748;
          font-weight: 500;
          display: block;
        }
        
        .city-region {
          font-size: 26rpx;
          color: #8b9aad;
          margin-top: 8rpx;
          display: block;
        }
      }
    }
    
    .no-result {
      text-align: center;
      padding: 60rpx;
      color: #8b9aad;
      font-size: 28rpx;
    }
    
    .current-location {
      padding: 40rpx 0;
      
      .location-btn {
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 12rpx;
        padding: 28rpx;
        background: linear-gradient(135deg, #4facfe, #00f2fe);
        border-radius: 16rpx;
        color: #fff;
        font-size: 30rpx;
        font-weight: 500;
        
        &:active {
          opacity: 0.9;
        }
        
        .location-icon {
          font-size: 32rpx;
        }
      }
    }
  }
}
</style>
