<template>
  <view class="reminder-setting-page">
    <!-- 顶部导航 -->
    <view class="nav-bar">
      <view class="back-btn" @click="goBack">
        <u-icon name="arrow-left" size="40" color="#333"></u-icon>
      </view>
      <text class="title">提醒设置</text>
      <view class="right-btn" @click="saveReminder">
        <text>保存</text>
      </view>
    </view>

    <view class="content">
      <!-- 提醒开关 -->
      <view class="section">
        <view class="switch-card">
          <view class="switch-row">
            <view class="switch-info"
>
              <u-icon name="bell" size="40" color="#5B8FF9"></u-icon>
              <view class="switch-text">
                <text class="switch-title">开启提醒</text>
                <text class="switch-desc">到达设定时间时收到通知</text>
              </view>
            </view>
            <switch 
              :checked="enabled" 
              color="#5B8FF9"
              @change="enabled = $event.detail.value"
            />
          </view>
        </view>
      </view>

      <!-- 时间选择器 -->
      <view v-if="enabled" class="section">
        <text class="section-title">提醒时间</text>
        
        <view class="time-cards">
          <view class="time-card">
            <text class="card-label">日期</text>
            <picker mode="date" :value="reminderDate" @change="onDateChange">
              <view class="picker-value">
                <text class="date-text">{{ reminderDate || '选择日期' }}</text>
                <u-icon name="arrow-right" size="28" color="#999"></u-icon>
              </view>
            </picker>
          </view>

          <view class="time-card">
            <text class="card-label">时间</text>
            <picker mode="time" :value="reminderTime" @change="onTimeChange">
              <view class="picker-value">
                <text class="time-text">{{ reminderTime || '选择时间' }}</text>
                <u-icon name="arrow-right" size="28" color="#999"></u-icon>
              </view>
            </picker>
          </view>
        </view>

        <!-- 快速时间选择 -->
        <view class="quick-time-section">
          <text class="quick-title">快捷选择</text>
          <view class="quick-options">
            <view
              v-for="option in quickTimeOptions"
              :key="option.label"
              class="quick-option"
              :class="{ active: isQuickTimeSelected(option) }"
              @click="selectQuickTime(option)"
            >
              <u-icon :name="option.icon" size="28" :color="isQuickTimeSelected(option) ? '#5B8FF9' : '#999'"></u-icon>
              <text>{{ option.label }}</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 位置提醒 -->
      <view v-if="enabled" class="section">
        <text class="section-title">位置提醒</text>
        
        <view class="location-card">
          <view class="location-header">
            <view class="location-info">
              <u-icon name="map" size="40" color="#5B8FF9"></u-icon>
              <view class="location-text">
                <text class="location-title">{{ location.name || '选择位置' }}</text>
                <text class="location-address" v-if="location.address">{{ location.address }}</text>
              </view>
            </view>
            <view class="location-switch"
003e
              <switch 
                :checked="locationEnabled" 
                color="#5B8FF9"
                @change="locationEnabled = $event.detail.value"
              />
            </view>
          </view>

          <!-- 地图选点 -->
          <view v-if="locationEnabled" class="map-container">
            <map
              id="locationMap"
              class="location-map"
              :latitude="location.latitude"
              :longitude="location.longitude"
              :markers="markers"
              :scale="15"
              show-location
              @tap="onMapTap"
            />

            <view class="map-actions">
              <view class="map-btn" @click="chooseLocation">
                <u-icon name="search" size="28" color="#5B8FF9"></u-icon>
                <text>搜索位置</text>
              </view>
              <view class="map-btn" @click="getCurrentLocation"
003e
                <u-icon name="定位" size="28" color="#5B8FF9"></u-icon>
                <text>当前位置</text>
              </view>
            </view>
          </view>

          <!-- 提醒范围 -->
          <view v-if="locationEnabled && location.latitude" class="radius-section"
003e
            <text class="radius-label">提醒范围：{{ location.radius }}米</text>
            <slider 
              :value="location.radius" 
              min="100" 
              max="1000" 
              step="100"
              activeColor="#5B8FF9"
              @change="onRadiusChange"
              @changing="onRadiusChanging"
            />
            <view class="radius-hint">
              <text>进入此范围时触发提醒</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 重复提醒 -->
      <view v-if="enabled" class="section">
        <text class="section-title">重复提醒</text>
        
        <view class="repeat-options"
003e
          <view
            v-for="option in repeatOptions"
            :key="option.value"
            class="repeat-card"
            :class="{ active: repeatType === option.value }"
            @click="repeatType = option.value"
          >
            <view class="repeat-radio"
003e
              <view class="radio-inner" v-if="repeatType === option.value"></view>
            </view>
            <text class="repeat-label">{{ option.label }}</text>
          </view>
        </view>
      </view>

      <!-- 提醒方式 -->
      <view v-if="enabled" class="section">
        <text class="section-title">提醒方式</text>
        
        <view class="method-list">
          <view class="method-card">
            <view class="method-info">
              <u-icon name="notification" size="36" color="#5B8FF9"></u-icon>
              <view class="method-text">
                <text class="method-title">应用内通知</text>
                <text class="method-desc">打开应用时显示提醒</text>
              </view>
            </view>
            <switch :checked="notifyInApp" color="#5B8FF9" @change="notifyInApp = $event.detail.value" />
          </view>

          <view class="method-card">
            <view class="method-info">
              <u-icon name="message" size="36" color="#5B8FF9"></u-icon>
              <view class="method-text">
                <text class="method-title">推送通知</text>
                <text class="method-desc">即使关闭应用也会提醒</text>
              </view>
            </view>
            <switch :checked="notifyPush" color="#5B8FF9" @change="notifyPush = $event.detail.value" />
          </view>

          <view class="method-card">
            <view class="method-info">
              <u-icon name="volume-up" size="36" color="#5B8FF9"></u-icon>
              <view class="method-text">
                <text class="method-title">声音提醒</text>
                <text class="method-desc">播放提示音</text>
              </view>
            </view>
            <switch :checked="notifySound" color="#5B8FF9" @change="notifySound = $event.detail.value" />
          </view>

          <view class="method-card">
            <view class="method-info">
              <u-icon name="minus-circle" size="36" color="#5B8FF9"></u-icon>
              <view class="method-text">
                <text class="method-title">振动</text>
                <text class="method-desc">振动提醒</text>
              </view>
            </view>
            <switch :checked="notifyVibrate" color="#5B8FF9" @change="notifyVibrate = $event.detail.value" />
          </view>
        </view>
      </view>

      <!-- 提醒预览 -->
      <view v-if="enabled" class="section">
        <text class="section-title">提醒预览</text>
        
        <view class="preview-card">
          <view class="preview-icon">
            <u-icon name="bell" size="60" color="#5B8FF9"></u-icon>
          </view>
          <view class="preview-content">
            <text class="preview-title">{{ previewTitle }}</text>
            <text class="preview-time">{{ previewTime }}</text>
            <text class="preview-location" v-if="locationEnabled && location.name"
003e
              <u-icon name="map" size="20" color="#999"></u-icon>
              {{ location.name }}
            </text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import dayjs from 'dayjs'

// 响应式数据
const enabled = ref(true)
const reminderDate = ref('')
const reminderTime = ref('')
const locationEnabled = ref(false)
const location = ref({
  name: '',
  address: '',
  latitude: 39.9042,
  longitude: 116.4074,
  radius: 500
})
const repeatType = ref('none')
const notifyInApp = ref(true)
const notifyPush = ref(true)
const notifySound = ref(true)
const notifyVibrate = ref(true)

// 快捷时间选项
const quickTimeOptions = [
  { label: '5分钟后', value: 5, unit: 'minute', icon: 'clock' },
  { label: '15分钟后', value: 15, unit: 'minute', icon: 'clock' },
  { label: '30分钟后', value: 30, unit: 'minute', icon: 'clock' },
  { label: '1小时后', value: 1, unit: 'hour', icon: 'clock' },
  { label: '今天18:00', value: '18:00', unit: 'today', icon: 'moon' },
  { label: '明天9:00', value: '09:00', unit: 'tomorrow', icon: 'sun' }
]

// 重复选项
const repeatOptions = [
  { label: '不重复', value: 'none' },
  { label: '每天', value: 'daily' },
  { label: '每周', value: 'weekly' },
  { label: '每月', value: 'monthly' }
]

// 地图标记
const markers = computed(() => {
  if (!location.value.latitude) return []
  return [{
    id: 1,
    latitude: location.value.latitude,
    longitude: location.value.longitude,
    title: location.value.name || '提醒位置',
    iconPath: '/static/location.png',
    width: 40,
    height: 40
  }]
})

// 页面加载
onMounted(() => {
  // 设置默认时间为30分钟后
  const defaultTime = dayjs().add(30, 'minute')
  reminderDate.value = defaultTime.format('YYYY-MM-DD')
  reminderTime.value = defaultTime.format('HH:mm')
  
  // 获取当前位置
  getCurrentLocation()
  
  // 加载已有设置
  loadSettings()
})

// 加载已有设置
const loadSettings = () => {
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const options = currentPage.$page?.options || currentPage.options || {}
  
  if (options.reminder) {
    try {
      const data = JSON.parse(decodeURIComponent(options.reminder))
      enabled.value = data.enabled ?? true
      reminderDate.value = data.date || ''
      reminderTime.value = data.time || ''
      if (data.location) {
        location.value = { ...location.value, ...data.location }
        locationEnabled.value = true
      }
      repeatType.value = data.repeatType || 'none'
      notifyInApp.value = data.notifyInApp ?? true
      notifyPush.value = data.notifyPush ?? true
      notifySound.value = data.notifySound ?? true
      notifyVibrate.value = data.notifyVibrate ?? true
    } catch (e) {
      console.error('解析提醒设置失败', e)
    }
  }
}

// 日期变化
const onDateChange = (e) => {
  reminderDate.value = e.detail.value
}

// 时间变化
const onTimeChange = (e) => {
  reminderTime.value = e.detail.value
}

// 检查是否选中快捷时间
const isQuickTimeSelected = (option) => {
  if (option.unit === 'minute' || option.unit === 'hour') {
    const targetTime = dayjs().add(option.value, option.unit)
    return reminderDate.value === targetTime.format('YYYY-MM-DD') && 
           reminderTime.value === targetTime.format('HH:mm')
  } else if (option.unit === 'today') {
    return reminderDate.value === dayjs().format('YYYY-MM-DD') && 
           reminderTime.value === option.value
  } else if (option.unit === 'tomorrow') {
    return reminderDate.value === dayjs().add(1, 'day').format('YYYY-MM-DD') && 
           reminderTime.value === option.value
  }
  return false
}

// 选择快捷时间
const selectQuickTime = (option) => {
  if (option.unit === 'minute' || option.unit === 'hour') {
    const targetTime = dayjs().add(option.value, option.unit)
    reminderDate.value = targetTime.format('YYYY-MM-DD')
    reminderTime.value = targetTime.format('HH:mm')
  } else if (option.unit === 'today') {
    reminderDate.value = dayjs().format('YYYY-MM-DD')
    reminderTime.value = option.value
  } else if (option.unit === 'tomorrow') {
    reminderDate.value = dayjs().add(1, 'day').format('YYYY-MM-DD')
    reminderTime.value = option.value
  }
}

// 地图点击
const onMapTap = (e) => {
  location.value.latitude = e.detail.latitude
  location.value.longitude = e.detail.longitude
  // 反向地理编码获取地址
  reverseGeocode(e.detail.latitude, e.detail.longitude)
}

// 选择位置
const chooseLocation = () => {
  uni.chooseLocation({
    success: (res) => {
      location.value.name = res.name
      location.value.address = res.address
      location.value.latitude = res.latitude
      location.value.longitude = res.longitude
    },
    fail: (err) => {
      console.error('选择位置失败', err)
      uni.showToast({ title: '选择位置失败', icon: 'none' })
    }
  })
}

// 获取当前位置
const getCurrentLocation = () => {
  uni.getLocation({
    type: 'gcj02',
    success: (res) => {
      location.value.latitude = res.latitude
      location.value.longitude = res.longitude
      reverseGeocode(res.latitude, res.longitude)
    },
    fail: (err) => {
      console.error('获取位置失败', err)
    }
  })
}

// 反向地理编码
const reverseGeocode = (latitude, longitude) => {
  // 这里应该调用地图SDK的逆地理编码
  // 简化处理，仅更新坐标
}

// 范围变化
const onRadiusChange = (e) => {
  location.value.radius = e.detail.value
}

const onRadiusChanging = (e) => {
  location.value.radius = e.detail.value
}

// 预览标题
const previewTitle = computed(() => {
  return enabled.value ? '任务提醒' : '提醒已关闭'
})

// 预览时间
const previewTime = computed(() => {
  if (!enabled.value) return '-'
  if (!reminderDate.value || !reminderTime.value) return '未设置'
  
  const reminder = dayjs(`${reminderDate.value} ${reminderTime.value}`)
  const now = dayjs()
  const diffMinutes = reminder.diff(now, 'minute')
  
  if (diffMinutes < 0) {
    return `已过期 (${reminder.format('MM-DD HH:mm')})`
  } else if (diffMinutes < 60) {
    return `${diffMinutes}分钟后 (${reminder.format('HH:mm')})`
  } else if (diffMinutes < 1440) {
    return `${Math.floor(diffMinutes / 60)}小时后 (${reminder.format('HH:mm')})`
  } else {
    return reminder.format('MM-DD HH:mm')
  }
})

// 保存提醒设置
const saveReminder = () => {
  if (enabled.value && (!reminderDate.value || !reminderTime.value)) {
    uni.showToast({ title: '请设置提醒时间', icon: 'none' })
    return
  }
  
  const reminderData = {
    enabled: enabled.value,
    date: reminderDate.value,
    time: reminderTime.value,
    location: locationEnabled.value ? location.value : null,
    repeatType: repeatType.value,
    notifyInApp: notifyInApp.value,
    notifyPush: notifyPush.value,
    notifySound: notifySound.value,
    notifyVibrate: notifyVibrate.value
  }
  
  // 通知上一页
  const eventChannel = getOpenerEventChannel()
  if (eventChannel && eventChannel.emit) {
    eventChannel.emit('onReminderSet', reminderData)
  }
  
  uni.showToast({ title: '保存成功', icon: 'success' })
  setTimeout(() => uni.navigateBack(), 1500)
}

const goBack = () => {
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
.reminder-setting-page {
  min-height: 100vh;
  background: #f5f6fa;
}

.nav-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 30rpx;
  padding-top: 60rpx;
  background: #fff;
  border-bottom: 1rpx solid #f0f0f0;

  .back-btn {
    width: 60rpx;
    height: 60rpx;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .title {
    font-size: 34rpx;
    font-weight: 600;
    color: #333;
  }

  .right-btn {
    padding: 12rpx 30rpx;
    background: #5B8FF9;
    border-radius: 30rpx;

    text {
      font-size: 28rpx;
      color: #fff;
    }
  }
}

.content {
  padding: 30rpx;
  padding-bottom: 50rpx;
}

.section {
  margin-bottom: 40rpx;
}

.section-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #333;
  margin-bottom: 20rpx;
  display: block;
}

// 开关卡片
.switch-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 30rpx;

  .switch-row {
    display: flex;
    align-items: center;
    justify-content: space-between;
  }

  .switch-info {
    display: flex;
    align-items: center;
    gap: 20rpx;
  }

  .switch-text {
    .switch-title {
      font-size: 32rpx;
      font-weight: 500;
      color: #333;
      display: block;
    }

    .switch-desc {
      font-size: 24rpx;
      color: #999;
      margin-top: 4rpx;
      display: block;
    }
  }
}

// 时间卡片
.time-cards {
  display: flex;
  gap: 20rpx;
  margin-bottom: 30rpx;

  .time-card {
    flex: 1;
    background: #fff;
    border-radius: 16rpx;
    padding: 24rpx;

    .card-label {
      font-size: 24rpx;
      color: #999;
      margin-bottom: 12rpx;
      display: block;
    }

    .picker-value {
      display: flex;
      align-items: center;
      justify-content: space-between;

      .date-text, .time-text {
        font-size: 32rpx;
        font-weight: 600;
        color: #333;
      }
    }
  }
}

// 快捷时间
.quick-time-section {
  .quick-title {
    font-size: 26rpx;
    color: #666;
    margin-bottom: 16rpx;
    display: block;
  }

  .quick-options {
    display: flex;
    flex-wrap: wrap;
    gap: 16rpx;

    .quick-option {
      display: flex;
      align-items: center;
      gap: 8rpx;
      padding: 16rpx 24rpx;
      background: #fff;
      border-radius: 30rpx;
      border: 2rpx solid transparent;

      text {
        font-size: 24rpx;
        color: #666;
      }

      &.active {
        border-color: #5B8FF9;
        background: #f0f5ff;

        text {
          color: #5B8FF9;
        }
      }
    }
  }
}

// 位置卡片
.location-card {
  background: #fff;
  border-radius: 16rpx;
  overflow: hidden;

  .location-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 24rpx 30rpx;
    border-bottom: 1rpx solid #f5f5f5;

    .location-info {
      display: flex;
      align-items: center;
      gap: 16rpx;
      flex: 1;
    }

    .location-text {
      flex: 1;
      min-width: 0;

      .location-title {
        font-size: 30rpx;
        font-weight: 500;
        color: #333;
        display: block;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .location-address {
        font-size: 24rpx;
        color: #999;
        margin-top: 4rpx;
        display: block;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
    }
  }
}

// 地图容器
.map-container {
  .location-map {
    width: 100%;
    height: 400rpx;
  }

  .map-actions {
    display: flex;
    gap: 20rpx;
    padding: 20rpx 30rpx;
    border-top: 1rpx solid #f5f5f5;

    .map-btn {
      flex: 1;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 12rpx;
      padding: 20rpx;
      background: #f0f5ff;
      border-radius: 12rpx;

      text {
        font-size: 28rpx;
        color: #5B8FF9;
      }
    }
  }
}

// 范围设置
.radius-section {
  padding: 24rpx 30rpx;
  border-top: 1rpx solid #f5f5f5;

  .radius-label {
    font-size: 28rpx;
    color: #333;
    margin-bottom: 16rpx;
    display: block;
  }

  .radius-hint {
    text {
      font-size: 24rpx;
      color: #999;
    }
  }
}

// 重复选项
.repeat-options {
  display: flex;
  gap: 16rpx;
  flex-wrap: wrap;

  .repeat-card {
    display: flex;
    align-items: center;
    gap: 16rpx;
    padding: 24rpx 32rpx;
    background: #fff;
    border-radius: 16rpx;
    border: 2rpx solid transparent;

    .repeat-radio {
      width: 36rpx;
      height: 36rpx;
      border-radius: 50%;
      border: 4rpx solid #ddd;
      display: flex;
      align-items: center;
      justify-content: center;

      .radio-inner {
        width: 18rpx;
        height: 18rpx;
        border-radius: 50%;
        background: #5B8FF9;
      }
    }

    .repeat-label {
      font-size: 28rpx;
      color: #333;
    }

    &.active {
      border-color: #5B8FF9;
      background: #f0f5ff;

      .repeat-radio {
        border-color: #5B8FF9;
      }

      .repeat-label {
        color: #5B8FF9;
      }
    }
  }
}

// 提醒方式
.method-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;

  .method-card {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 24rpx 30rpx;
    background: #fff;
    border-radius: 16rpx;

    .method-info {
      display: flex;
      align-items: center;
      gap: 20rpx;
    }

    .method-text {
      .method-title {
        font-size: 30rpx;
        color: #333;
        display: block;
      }

      .method-desc {
        font-size: 24rpx;
        color: #999;
        margin-top: 4rpx;
        display: block;
      }
    }
  }
}

// 预览卡片
.preview-card {
  display: flex;
  align-items: center;
  gap: 24rpx;
  padding: 30rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16rpx;

  .preview-icon {
    width: 100rpx;
    height: 100rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    background: rgba(255, 255, 255, 0.2);
    border-radius: 50%;
  }

  .preview-content {
    flex: 1;

    .preview-title {
      font-size: 32rpx;
      font-weight: 600;
      color: #fff;
      display: block;
    }

    .preview-time {
      font-size: 26rpx;
      color: rgba(255, 255, 255, 0.9);
      margin-top: 8rpx;
      display: block;
    }

    .preview-location {
      display: flex;
      align-items: center;
      gap: 8rpx;
      font-size: 24rpx;
      color: rgba(255, 255, 255, 0.8);
      margin-top: 8rpx;
    }
  }
}
</style>