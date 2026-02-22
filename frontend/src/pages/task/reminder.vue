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
            <view class="switch-info">
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
              v-for="(option, index) in quickTimeOptions" 
              :key="index"
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

      <!-- 重复提醒 -->
      <view v-if="enabled" class="section">
        <text class="section-title">重复提醒</text>
        
        <view class="repeat-options">
          <view
            v-for="(option, index) in repeatOptions" 
            :key="index"
            class="repeat-card"
            :class="{ active: repeatType === option.value }"
            @click="repeatType = option.value"
          >
            <view class="repeat-radio">
              <view class="radio-inner" v-if="repeatType === option.value"></view>
            </view>
            <text class="repeat-label">{{ option.label }}</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import dayjs from 'dayjs'

// 响应式数据
const enabled = ref(true)
const reminderDate = ref('')
const reminderTime = ref('')
const repeatType = ref('none')

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

// 页面加载
onMounted(() => {
  const defaultTime = dayjs().add(30, 'minute')
  reminderDate.value = defaultTime.format('YYYY-MM-DD')
  reminderTime.value = defaultTime.format('HH:mm')
})

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
    repeatType: repeatType.value
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
</style>
