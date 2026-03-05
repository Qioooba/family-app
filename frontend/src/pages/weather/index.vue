<template>
  <view class="weather-page">
    <!-- 顶部背景 -->
    <view class="header-bg">
      <view class="bg-gradient"></view>
    </view>
    
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
    
    <!-- 未来预报 -->
    <view class="forecast section">
      <view class="section-header">
        <text class="section-title">未来7天</text>
      </view>
      
      <view class="forecast-list">
        <view 
          v-for="(day, index) in forecast" 
          :key="index"
          class="forecast-item"
          :class="{ today: index === 0 }"
        >
          <text class="day-name">{{ day.date }}</text>
          <text class="day-icon">{{ day.icon }}</text>
          <view class="day-temp">
            <text class="temp-high">{{ day.tempMax }}°</text>
            <view class="temp-bar">
              <view class="temp-fill" :style="{ width: day.tempPercent + '%' }"></view>
            </view>
            <text class="temp-low">{{ day.tempMin }}°</text>
          </view>
          <text class="day-weather">{{ day.weather }}</text>
        </view>
      </view>
    </view>
    
    <!-- 城市选择弹窗 -->
    <view v-if="showCityPicker" class="modal-overlay" @click="showCityPicker = false">
      <view class="modal-content" @click.stop>
        <view class="modal-header">
          <text class="modal-title">选择城市</text>
          <text class="modal-close" @click="showCityPicker = false">✕</text>
        </view>
        
        <view class="city-search">
          <input 
            class="search-input" 
            v-model="searchCity" 
            placeholder="搜索城市"
          />
        </view>
        
        <scroll-view scroll-y class="city-list">
          <view 
            v-for="city in filteredCities" 
            :key="city.code"
            class="city-item"
            :class="{ active: currentCity === city.name }"
            @click="selectCity(city)"
          >
            <text class="city-name">{{ city.name }}</text>
            <text v-if="currentCity === city.name" class="city-check">✓</text>
          </view>
        </scroll-view>
      </view>
    </view>
    
    <!-- 指数详情弹窗 -->
    <view v-if="showIndexModal" class="modal-overlay" @click="showIndexModal = false">
      <view class="modal-content small" @click.stop>
        <view class="modal-header">
          <text class="modal-title">{{ currentIndex.name }}</text>
          <text class="modal-close" @click="showIndexModal = false">✕</text>
        </view>
        
        <view class="index-detail-content">
          <view class="index-level-large" :class="'level-' + currentIndex.level">
            <text>{{ currentIndex.levelText }}</text>
          </view>          
          <text class="index-desc">{{ currentIndex.description }}</text>
          
          <text class="index-suggestion">{{ currentIndex.suggestion }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'

const currentCity = ref('北京市')
const showCityPicker = ref(false)
const showIndexModal = ref(false)
const searchCity = ref('')
const currentIndex = ref({})
const updateTime = ref('今天 08:30 更新')

// 当前天气
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

// 生活指数
const lifeIndex = ref([
  { 
    name: '穿衣', 
    icon: '👔', 
    level: 2, 
    levelText: '舒适',
    description: '建议穿长袖衬衫、单裤等服装',
    suggestion: '早晚温差较大，建议携带薄外套'
  },
  { 
    name: '紫外线', 
    icon: '☀️', 
    level: 3, 
    levelText: '中等',
    description: '紫外线强度中等',
    suggestion: '外出时请涂抹防晒霜，佩戴太阳镜'
  },
  { 
    name: '洗车', 
    icon: '🚗', 
    level: 1, 
    levelText: '适宜',
    description: '天气适宜洗车',
    suggestion: '未来两天无雨，适宜洗车'
  },
  { 
    name: '运动', 
    icon: '🏃', 
    level: 2, 
    levelText: '适宜',
    description: '天气不错，适宜户外运动',
    suggestion: '可以选择慢跑、散步等运动'
  },
  { 
    name: '感冒', 
    icon: '🤧', 
    level: 1, 
    levelText: '少发',
    description: '感冒几率较低',
    suggestion: '注意保暖，多喝水'
  },
  { 
    name: '钓鱼', 
    icon: '🎣', 
    level: 2, 
    levelText: '适宜',
    description: '天气适宜垂钓',
    suggestion: '气压适宜，鱼儿活跃'
  }
])

// 未来预报
const forecast = ref([
  { date: '今天', icon: '⛅', tempMax: 22, tempMin: 12, weather: '多云', tempPercent: 70 },
  { date: '明天', icon: '🌤️', tempMax: 24, tempMin: 14, weather: '晴', tempPercent: 80 },
  { date: '周三', icon: '☀️', tempMax: 26, tempMin: 15, weather: '晴', tempPercent: 90 },
  { date: '周四', icon: '⛅', tempMax: 23, tempMin: 13, weather: '多云', tempPercent: 75 },
  { date: '周五', icon: '🌧️', tempMax: 20, tempMin: 11, weather: '小雨', tempPercent: 60 },
  { date: '周六', icon: '🌦️', tempMax: 19, tempMin: 10, weather: '阵雨', tempPercent: 55 },
  { date: '周日', icon: '⛅', tempMax: 21, tempMin: 12, weather: '多云', tempPercent: 65 }
])

// 城市列表
const cities = ref([
  { name: '北京市', code: '110000' },
  { name: '上海市', code: '310000' },
  { name: '广州市', code: '440100' },
  { name: '深圳市', code: '440300' },
  { name: '杭州市', code: '330100' },
  { name: '南京市', code: '320100' },
  { name: '成都市', code: '510100' },
  { name: '武汉市', code: '420100' },
  { name: '西安市', code: '610100' },
  { name: '重庆市', code: '500000' }
])

// 筛选城市
const filteredCities = computed(() => {
  if (!searchCity.value) return cities.value
  return cities.value.filter(city => 
    city.name.includes(searchCity.value)
  )
})

// 选择城市
const selectCity = (city) => {
  currentCity.value = city.name
  showCityPicker.value = false
  // 这里应该调用天气API获取新城市的天气
  uni.showToast({ title: `已切换到${city.name}`, icon: 'none' })
}

// 显示指数详情
const showIndexDetail = (item) => {
  currentIndex.value = item
  showIndexModal.value = true
}

// 页面加载时获取天气
const loadWeather = async () => {
  // 这里应该调用天气API
  // const res = await weatherApi.getCurrent(currentCity.value)
}

onMounted(() => {
  loadWeather()
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
  pointer-events: none;
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

.forecast {
  .forecast-list {
    .forecast-item {
      display: flex;
      align-items: center;
      padding: 24rpx 0;
      border-bottom: 2rpx solid #f1f5f9;
      
      &:last-child {
        border-bottom: none;
      }
      
      &.today {
        background: linear-gradient(90deg, rgba(79, 172, 254, 0.1), transparent);
        margin: 0 -32rpx;
        padding-left: 32rpx;
        padding-right: 32rpx;
        border-radius: 16rpx;
      }
      
      .day-name {
        width: 100rpx;
        font-size: 28rpx;
        color: #5a6c7d;
        font-weight: 500;
      }
      
      .day-icon {
        width: 60rpx;
        font-size: 40rpx;
        text-align: center;
      }
      
      .day-temp {
        flex: 1;
        display: flex;
        align-items: center;
        gap: 16rpx;
        margin: 0 24rpx;
        
        .temp-high {
          font-size: 28rpx;
          color: #2d3748;
          font-weight: 500;
          width: 60rpx;
          text-align: right;
        }
        
        .temp-bar {
          flex: 1;
          height: 8rpx;
          background: #e2e8f0;
          border-radius: 4rpx;
          overflow: hidden;
          
          .temp-fill {
            height: 100%;
            background: linear-gradient(90deg, #4facfe, #00f2fe);
            border-radius: 4rpx;
          }
        }
        
        .temp-low {
          font-size: 28rpx;
          color: #8b9aad;
          width: 60rpx;
        }
      }
      
      .day-weather {
        width: 100rpx;
        font-size: 26rpx;
        color: #8b9aad;
        text-align: right;
      }
    }
  }
}

// 弹窗样式
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: flex-end;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: #fff;
  border-radius: 40rpx 40rpx 0 0;
  width: 100%;
  max-height: 80vh;
  animation: slideUp 0.3s ease;
  
  &.small {
    max-height: 60vh;
  }
}

@keyframes slideUp {
  from { transform: translateY(100%); }
  to { transform: translateY(0); }
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 40rpx 40rpx 20rpx;
  
  .modal-title {
    font-size: 36rpx;
    font-weight: 600;
    color: #2d3748;
  }
  
  .modal-close {
    font-size: 40rpx;
    color: #8b9aad;
  }
}

.city-search {
  padding: 0 40rpx 20rpx;
  
  .search-input {
    width: 100%;
    padding: 24rpx 32rpx;
    background: #f8f9fc;
    border-radius: 32rpx;
    font-size: 30rpx;
    
    &::placeholder {
      color: #a0aec0;
    }
  }
}

.city-list {
  max-height: 60vh;
  padding: 0 40rpx;
  
  .city-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 28rpx 0;
    border-bottom: 2rpx solid #f1f5f9;
    
    &.active {
      .city-name {
        color: #4facfe;
        font-weight: 600;
      }
    }
    
    .city-name {
      font-size: 32rpx;
      color: #2d3748;
    }
    
    .city-check {
      font-size: 32rpx;
      color: #4facfe;
    }
  }
}

.index-detail-content {
  padding: 20rpx 40rpx 60rpx;
  text-align: center;
  
  .index-level-large {
    display: inline-block;
    padding: 16rpx 48rpx;
    border-radius: 32rpx;
    font-size: 36rpx;
    font-weight: 600;
    margin-bottom: 32rpx;
    
    &.level-1 { background: rgba(104, 211, 145, 0.2); color: #68d391; }
    &.level-2 { background: rgba(246, 173, 85, 0.2); color: #f6ad55; }
    &.level-3 { background: rgba(252, 129, 129, 0.2); color: #fc8181; }
    &.level-4 { background: rgba(155, 89, 182, 0.2); color: #9B59B6; }
  }
  
  .index-desc {
    font-size: 30rpx;
    color: #2d3748;
    display: block;
    margin-bottom: 24rpx;
    line-height: 1.6;
  }
  
  .index-suggestion {
    font-size: 26rpx;
    color: #8b9aad;
    display: block;
    line-height: 1.6;
    padding: 24rpx;
    background: #f8f9fc;
    border-radius: 20rpx;
  }
}
</style>
