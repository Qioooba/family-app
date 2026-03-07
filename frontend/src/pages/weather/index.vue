<template>
  <view class="weather-page">
    <!-- 渐变背景 -->
    <view class="bg-gradient"></view>
    
    <!-- 加载状态 -->
    <view v-if="isLoading" class="loading-mask">
      <view class="loading-spinner">
        <view class="spinner-dot"></view>
        <view class="spinner-dot"></view>
        <view class="spinner-dot"></view>
      </view>
      <text class="loading-text">获取天气中...</text>
    </view>
    
    <!-- 头部 - 位置显示 -->
    <view class="header-bar">
      <view class="location-wrapper" @click="hasLocationPermission ? showCityPicker = true : requestLocation()">
        <view class="location-icon-wrapper">
          <text class="location-icon">📍</text>
        </view>
        <view class="location-info">
          <text class="location-name" :class="{ 'no-permission': !hasLocationPermission }">
            {{ fullLocation || '点击获取位置' }}
          </text>
          <text class="location-time">{{ updateTime }}</text>
        </view>
        <text class="location-arrow" v-if="hasLocationPermission">▼</text>
      </view>
    </view>
    
    <scroll-view scroll-y class="weather-content" :scroll-with-animation="true">
      <!-- 当前天气卡片 -->
      <view class="current-weather-card">
        <view class="weather-main">
          <view class="weather-icon-wrapper" :class="{ 'night': !currentWeather.isDay }">
            <text class="weather-icon">{{ currentWeather.icon }}</text>
          </view>
          <view class="weather-info">
            <text class="temperature">{{ currentWeather.temperature }}°</text>
            <text class="weather-desc">{{ currentWeather.description }}</text>
            <view class="temp-range">
              <text class="temp-high">{{ currentWeather.tempMax }}°</text>
              <text class="temp-divider">/</text>
              <text class="temp-low">{{ currentWeather.tempMin }}°</text>
              <text class="feels-like"> 体感{{ currentWeather.feelsLike }}°</text>
            </view>
          </view>
        </view>
      </view>
      
      <!-- 天气详情卡片 -->
      <view class="weather-details-card">
        <view class="detail-item">
          <view class="detail-icon-wrapper">
            <text class="detail-icon">💨</text>
          </view>
          <text class="detail-label">风向</text>
          <text class="detail-value">{{ currentWeather.windDirection }}</text>
          <text class="detail-sub">{{ currentWeather.windLevel }}级</text>
        </view>
        <view class="detail-item">
          <view class="detail-icon-wrapper">
            <text class="detail-icon">💧</text>
          </view>
          <text class="detail-label">湿度</text>
          <text class="detail-value">{{ currentWeather.humidity }}%</text>
          <text class="detail-sub">相对湿度</text>
        </view>
        <view class="detail-item">
          <view class="detail-icon-wrapper">
            <text class="detail-icon">👁️</text>
          </view>
          <text class="detail-label">气压</text>
          <text class="detail-value">{{ currentWeather.pressure }}</text>
          <text class="detail-sub">hPa</text>
        </view>
        <view class="detail-item">
          <view class="detail-icon-wrapper">
            <text class="detail-icon">☁️</text>
          </view>
          <text class="detail-label">云量</text>
          <text class="detail-value">{{ currentWeather.cloudCover || '--' }}</text>
          <text class="detail-sub">%</text>
        </view>
      </view>
      
      <!-- 逐小时预报 - 新功能 -->
      <view class="hourly-section" v-if="hourlyForecast.length > 0">
        <view class="section-header">
          <text class="section-icon">🕐</text>
          <text class="section-title">逐小时预报</text>
          <text class="section-subtitle">未来24小时</text>
        </view>
        
        <scroll-view scroll-x class="hourly-scroll" :scroll-with-animation="true">
          <view class="hourly-list">
            <view 
              v-for="(item, index) in hourlyForecast" 
              :key="index"
              class="hourly-item"
              :class="{ 'current': index === 0 }"
            >
              <text class="hourly-time">{{ index === 0 ? '现在' : item.hour }}</text>
              <view class="hourly-icon-wrapper">
                <text class="hourly-icon">{{ item.icon }}</text>
              </view>
              <text class="hourly-temp">{{ item.temperature }}°</text>
              <text v-if="item.hasRain" class="hourly-rain">{{ item.description }}</text>
            </view>
          </view>
        </scroll-view>
      </view>
      
      <!-- 未来预报 -->
      <view class="forecast-section" v-if="forecast.length > 0">
        <view class="section-header">
          <text class="section-icon">📅</text>
          <text class="section-title">未来7天预报</text>
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
              <view class="forecast-icon-wrapper">
                <text class="forecast-icon">{{ item.icon }}</text>
              </view>
              <text class="forecast-desc">{{ item.description }}</text>
            </view>
            <view class="forecast-temp-bar">
              <text class="temp-min">{{ item.tempMin }}°</text>
              <view class="temp-bar">
                <view 
                  class="temp-bar-fill"
                  :style="getTempBarStyle(item.tempMin, item.tempMax)"
                ></view>
              </view>
              <text class="temp-max">{{ item.tempMax }}°</text>
            </view>
          </view>
        </view>
      </view>
      
      <!-- 生活指数 -->
      <view class="life-index-section">
        <view class="section-header">
          <text class="section-icon">📊</text>
          <text class="section-title">生活指数</text>
        </view>
        
        <view class="index-grid">
          <view 
            v-for="(item, index) in lifeIndex" 
            :key="index"
            class="index-card"
            @click="showIndexDetail(item)"
          >
            <view class="index-icon-wrapper">
              <text class="index-icon">{{ item.icon }}</text>
            </view>
            <text class="index-name">{{ item.name }}</text>
            <text class="index-level" :class="'level-' + item.level">{{ item.levelText }}</text>
          </view>
        </view>
      </view>
      
      <!-- 底部空白 -->
      <view class="bottom-spacer"></view>
    </scroll-view>
    
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
  currentDistrict,
  fullLocation,
  hasLocationPermission,
  isLoading,
  updateTime,
  currentWeather,
  forecast,
  hourlyForecast,
  lifeIndex,
  searchKeyword,
  searchResults,
  showCityPicker,
  onSearchInput,
  selectCity,
  showIndexDetail,
  loadWeatherData,
  requestLocation
} from './useWeather'

// 获取温度条样式
const getTempBarStyle = (min, max) => {
  const minTemp = Math.min(...forecast.value.map(f => f.tempMin))
  const maxTemp = Math.max(...forecast.value.map(f => f.tempMax))
  const range = maxTemp - minTemp || 1
  
  const leftPercent = ((min - minTemp) / range) * 100
  const widthPercent = ((max - min) / range) * 60 + 20
  
  return {
    left: leftPercent + '%',
    width: widthPercent + '%',
    background: `linear-gradient(90deg, #64B5F6, #FFB74D)`
  }
}

onMounted(() => {
  loadWeatherData()
})
</script>

<style lang="scss" scoped>
// 颜色变量 - 柔和配色
$primary-blue: #5B8FF9;
$primary-light: #8BAEFD;
$bg-gradient-start: #E8F4FD;
$bg-gradient-mid: #D4E8FC;
$bg-gradient-end: #F0F7FF;
$card-bg: rgba(255, 255, 255, 0.95);
$card-shadow: 0 8rpx 32rpx rgba(91, 143, 249, 0.1);
$text-primary: #2D3748;
$text-secondary: #718096;
$text-tertiary: #A0AEC0;
$border-radius-lg: 32rpx;
$border-radius-xl: 48rpx;

.weather-page {
  min-height: 100vh;
  position: relative;
  background: linear-gradient(180deg, $bg-gradient-start 0%, $bg-gradient-mid 50%, $bg-gradient-end 100%);
  overflow-x: hidden;
  touch-action: pan-y;
  display: flex;
  flex-direction: column;
}

.bg-gradient {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: 
    radial-gradient(ellipse at 10% 20%, rgba(91, 143, 249, 0.15) 0%, transparent 50%),
    radial-gradient(ellipse at 90% 80%, rgba(255, 183, 77, 0.1) 0%, transparent 50%);
  pointer-events: none;
}

// 加载遮罩
.loading-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10rpx);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  z-index: 100;
  
  .loading-spinner {
    display: flex;
    gap: 16rpx;
    margin-bottom: 24rpx;
    
    .spinner-dot {
      width: 16rpx;
      height: 16rpx;
      background: $primary-blue;
      border-radius: 50%;
      animation: bounce 1.4s ease-in-out infinite both;
      
      &:nth-child(1) { animation-delay: -0.32s; }
      &:nth-child(2) { animation-delay: -0.16s; }
    }
  }
  
  .loading-text {
    font-size: 28rpx;
    color: $text-secondary;
  }
}

@keyframes bounce {
  0%, 80%, 100% { transform: scale(0); }
  40% { transform: scale(1); }
}

// 头部
.header-bar {
  position: relative;
  padding: 100rpx 32rpx 20rpx;
  z-index: 10;
}

.location-wrapper {
  display: flex;
  align-items: center;
  gap: 16rpx;
  
  .location-icon-wrapper {
    width: 64rpx;
    height: 64rpx;
    background: linear-gradient(135deg, $primary-blue, $primary-light);
    border-radius: 20rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 4rpx 16rpx rgba(91, 143, 249, 0.3);
    
    .location-icon {
      font-size: 32rpx;
    }
  }
  
  .location-info {
    display: flex;
    flex-direction: column;
    
    .location-name {
      font-size: 36rpx;
      font-weight: 700;
      color: $text-primary;
      
      &.no-permission {
        color: $primary-blue;
      }
    }
    
    .location-time {
      font-size: 24rpx;
      color: $text-tertiary;
      margin-top: 4rpx;
    }
  }
  
  .location-arrow {
    font-size: 24rpx;
    color: $text-tertiary;
    margin-left: 8rpx;
  }
}

// 天气内容区域
.weather-content {
  flex: 1;
  padding: 0 32rpx;
  box-sizing: border-box;
  width: 100%;
}

// 当前天气卡片
.current-weather-card {
  margin-top: 20rpx;
  background: $card-bg;
  border-radius: $border-radius-xl;
  padding: 48rpx 40rpx;
  box-shadow: $card-shadow;
  backdrop-filter: blur(20rpx);
  
  .weather-main {
    display: flex;
    align-items: center;
    justify-content: space-between;
    
    .weather-icon-wrapper {
      width: 200rpx;
      height: 200rpx;
      background: linear-gradient(135deg, #FFD93D 0%, #FFB347 100%);
      border-radius: 60rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      box-shadow: 0 20rpx 60rpx rgba(255, 179, 71, 0.3);
      
      &.night {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        box-shadow: 0 20rpx 60rpx rgba(102, 126, 234, 0.3);
      }
      
      .weather-icon {
        font-size: 120rpx;
      }
    }
    
    .weather-info {
      flex: 1;
      text-align: right;
      
      .temperature {
        font-size: 120rpx;
        font-weight: 300;
        color: $text-primary;
        line-height: 1;
      }
      
      .weather-desc {
        font-size: 36rpx;
        color: $text-secondary;
        margin-top: 12rpx;
        display: block;
        font-weight: 500;
      }
      
      .temp-range {
        margin-top: 20rpx;
        display: flex;
        align-items: center;
        justify-content: flex-end;
        gap: 8rpx;
        
        .temp-high {
          font-size: 32rpx;
          color: #FFB74D;
          font-weight: 600;
        }
        
        .temp-divider {
          font-size: 28rpx;
          color: $text-tertiary;
        }
        
        .temp-low {
          font-size: 32rpx;
          color: $primary-blue;
          font-weight: 600;
        }
        
        .feels-like {
          font-size: 24rpx;
          color: $text-tertiary;
          margin-left: 16rpx;
          padding: 6rpx 16rpx;
          background: rgba(91, 143, 249, 0.1);
          border-radius: 20rpx;
        }
      }
    }
  }
}

// 天气详情卡片
.weather-details-card {
  margin-top: 24rpx;
  background: $card-bg;
  border-radius: $border-radius-lg;
  padding: 32rpx;
  box-shadow: $card-shadow;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20rpx;
  
  .detail-item {
    text-align: center;
    
    .detail-icon-wrapper {
      width: 72rpx;
      height: 72rpx;
      background: linear-gradient(135deg, rgba(91, 143, 249, 0.1), rgba(91, 143, 249, 0.05));
      border-radius: 24rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      margin: 0 auto 16rpx;
      
      .detail-icon {
        font-size: 40rpx;
      }
    }
    
    .detail-label {
      font-size: 22rpx;
      color: $text-tertiary;
      display: block;
      margin-bottom: 8rpx;
    }
    
    .detail-value {
      font-size: 32rpx;
      color: $text-primary;
      font-weight: 600;
      display: block;
    }
    
    .detail-sub {
      font-size: 20rpx;
      color: $text-tertiary;
      margin-top: 4rpx;
      display: block;
    }
  }
}

// 区块头部
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
    font-weight: 700;
    color: $text-primary;
  }
  
  .section-subtitle {
    font-size: 24rpx;
    color: $text-tertiary;
    margin-left: 16rpx;
  }
}

// 逐小时预报
.hourly-section {
  margin-top: 32rpx;
  background: $card-bg;
  border-radius: $border-radius-lg;
  padding: 32rpx;
  box-shadow: $card-shadow;
  overflow: hidden;
  
  .hourly-scroll {
    width: 100%;
    margin: 0 -32rpx;
    padding: 0 32rpx;
    box-sizing: content-box;
    
    .hourly-list {
      display: flex;
      gap: 24rpx;
      
      .hourly-item {
        display: flex;
        flex-direction: column;
        align-items: center;
        min-width: 100rpx;
        padding: 20rpx 16rpx;
        background: rgba(91, 143, 249, 0.05);
        border-radius: 24rpx;
        
        &.current {
          background: linear-gradient(135deg, $primary-blue, $primary-light);
          
          .hourly-time,
          .hourly-temp {
            color: #fff;
          }
          
          .hourly-icon-wrapper {
            background: rgba(255, 255, 255, 0.2);
          }
        }
        
        .hourly-time {
          font-size: 24rpx;
          color: $text-secondary;
          font-weight: 500;
        }
        
        .hourly-icon-wrapper {
          width: 72rpx;
          height: 72rpx;
          background: rgba(91, 143, 249, 0.1);
          border-radius: 24rpx;
          display: flex;
          align-items: center;
          justify-content: center;
          margin: 16rpx 0;
          
          .hourly-icon {
            font-size: 40rpx;
          }
        }
        
        .hourly-temp {
          font-size: 28rpx;
          color: $text-primary;
          font-weight: 600;
        }
        
        .hourly-rain {
          font-size: 20rpx;
          color: $primary-blue;
          margin-top: 8rpx;
          background: rgba(91, 143, 249, 0.1);
          padding: 4rpx 12rpx;
          border-radius: 12rpx;
          white-space: nowrap;
        }
      }
    }
  }
}

// 未来预报
.forecast-section {
  margin-top: 32rpx;
  background: $card-bg;
  border-radius: $border-radius-lg;
  padding: 32rpx;
  box-shadow: $card-shadow;
  
  .forecast-list {
    .forecast-item {
      display: flex;
      align-items: center;
      padding: 24rpx 0;
      border-bottom: 1rpx solid rgba(0, 0, 0, 0.03);
      
      &:last-child {
        border-bottom: none;
      }
      
      &.today {
        .forecast-date {
          color: $primary-blue;
          font-weight: 700;
        }
      }
      
      .forecast-date {
        width: 220rpx;
        font-size: 28rpx;
        color: $text-secondary;
        font-weight: 500;
      }
      
      .forecast-weather {
        flex: 1;
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 16rpx;
        
        .forecast-icon-wrapper {
          width: 56rpx;
          height: 56rpx;
          background: rgba(91, 143, 249, 0.1);
          border-radius: 20rpx;
          display: flex;
          align-items: center;
          justify-content: center;
          
          .forecast-icon {
            font-size: 36rpx;
          }
        }
        
        .forecast-desc {
          font-size: 26rpx;
          color: $text-secondary;
          min-width: 100rpx;
        }
      }
      
      .forecast-temp-bar {
        width: 200rpx;
        display: flex;
        align-items: center;
        gap: 16rpx;
        
        .temp-min {
          font-size: 28rpx;
          color: $primary-blue;
          font-weight: 600;
        }
        
        .temp-max {
          font-size: 28rpx;
          color: #FFB74D;
          font-weight: 600;
        }
        
        .temp-bar {
          flex: 1;
          height: 8rpx;
          background: rgba(0, 0, 0, 0.05);
          border-radius: 4rpx;
          position: relative;
          
          .temp-bar-fill {
            position: absolute;
            height: 100%;
            border-radius: 4rpx;
          }
        }
      }
    }
  }
}

// 生活指数
.life-index-section {
  margin-top: 32rpx;
  background: $card-bg;
  border-radius: $border-radius-lg;
  padding: 32rpx;
  box-shadow: $card-shadow;
  margin-bottom: 40rpx;
  
  .index-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 20rpx;
    
    .index-card {
      text-align: center;
      padding: 28rpx 0;
      background: rgba(91, 143, 249, 0.05);
      border-radius: 24rpx;
      transition: all 0.2s;
      
      &:active {
        transform: scale(0.96);
        background: rgba(91, 143, 249, 0.1);
      }
      
      .index-icon-wrapper {
        width: 80rpx;
        height: 80rpx;
        background: linear-gradient(135deg, rgba(91, 143, 249, 0.15), rgba(91, 143, 249, 0.05));
        border-radius: 28rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        margin: 0 auto 16rpx;
        
        .index-icon {
          font-size: 44rpx;
        }
      }
      
      .index-name {
        font-size: 24rpx;
        color: $text-secondary;
        display: block;
        margin-bottom: 8rpx;
      }
      
      .index-level {
        font-size: 24rpx;
        font-weight: 600;
        padding: 6rpx 16rpx;
        border-radius: 16rpx;
        display: inline-block;
        
        &.level-1 { background: rgba(104, 211, 145, 0.15); color: #38a169; }
        &.level-2 { background: rgba(246, 173, 85, 0.15); color: #d69e2e; }
        &.level-3 { background: rgba(252, 129, 129, 0.15); color: #e53e3e; }
        &.level-4 { background: rgba(155, 89, 182, 0.15); color: #805ad5; }
      }
    }
  }
}

.bottom-spacer {
  height: 60rpx;
}

// 城市选择弹窗
.city-picker-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 1000;
  display: flex;
  align-items: flex-end;
}

.city-picker {
  width: 100%;
  height: 70vh;
  background: $bg-gradient-end;
  border-radius: 48rpx 48rpx 0 0;
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
    padding: 40rpx 40rpx 32rpx;
    
    .picker-title {
      font-size: 36rpx;
      font-weight: 700;
      color: $text-primary;
    }
    
    .close-btn {
      font-size: 32rpx;
      color: $text-tertiary;
      padding: 16rpx;
      width: 64rpx;
      height: 64rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      background: rgba(0, 0, 0, 0.05);
      border-radius: 20rpx;
    }
  }
  
  .search-box {
    margin: 0 40rpx 24rpx;
    display: flex;
    align-items: center;
    background: #fff;
    border-radius: 24rpx;
    padding: 24rpx 32rpx;
    box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.05);
    
    .search-icon {
      font-size: 28rpx;
      color: $text-tertiary;
      margin-right: 20rpx;
    }
    
    .search-input {
      flex: 1;
      font-size: 30rpx;
      color: $text-primary;
    }
  }
  
  .city-list {
    flex: 1;
    padding: 0 40rpx;
    
    .search-results {
      .city-option {
        padding: 32rpx 0;
        border-bottom: 1rpx solid rgba(0, 0, 0, 0.05);
        
        &:active {
          background: rgba(91, 143, 249, 0.05);
          margin: 0 -40rpx;
          padding: 32rpx 40rpx;
        }
        
        .city-name {
          font-size: 32rpx;
          color: $text-primary;
          font-weight: 600;
          display: block;
        }
        
        .city-region {
          font-size: 26rpx;
          color: $text-tertiary;
          margin-top: 8rpx;
          display: block;
        }
      }
    }
    
    .no-result {
      text-align: center;
      padding: 80rpx;
      color: $text-tertiary;
      font-size: 28rpx;
    }
    
    .current-location {
      padding: 40rpx 0;
      
      .location-btn {
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 16rpx;
        padding: 32rpx;
        background: linear-gradient(135deg, $primary-blue, $primary-light);
        border-radius: 24rpx;
        color: #fff;
        font-size: 30rpx;
        font-weight: 600;
        box-shadow: 0 8rpx 32rpx rgba(91, 143, 249, 0.3);
        
        &:active {
          transform: scale(0.98);
        }
        
        .location-icon {
          font-size: 36rpx;
        }
      }
    }
  }
}
</style>
