<template>
  <view class="water-page">
    <!-- 导航栏 -->
    <view class="nav-bar">
      <view class="back-btn" @click="goBack">
        <u-icon name="arrow-left" size="40" color="#333"></u-icon>
      </view>
      <text class="title">喝水打卡</text>
      <view class="right-btn" @click="showSettings">
        <u-icon name="setting" size="40" color="#333"></u-icon>
      </view>
    </view>

    <!-- 主内容区 -->
    <view class="content">
      <!-- 日期显示 -->
      <view class="date-section">
        <text class="date-text">{{ todayText }}</text>
        <text class="week-text">{{ weekText }}</text>
      </view>

      <!-- 水杯动画区域 -->
      <view class="water-cup-section">
        <view class="cup-container">
          <view class="cup">
            <view class="water" :style="{ height: waterPercent + '%' }">
              <view class="water-wave"></text>
            </view>
            <view class="cup-markings">
              <view class="marking" style="top: 25%"></text>
              <view class="marking" style="top: 50%"></text>
              <view class="marking" style="top: 75%"></text>
            </view>
          </view>
          <view class="cup-stand"></text>
        </view>

        <!-- 喝水数据 -->
        <view class="water-stats">
          <view class="stats-item">
            <text class="stats-num">{{ todayAmount }}</text>
            <text class="stats-unit">ml</text>
          </view>
          <text class="stats-divider">/</text>
          <view class="stats-item">
            <text class="stats-num target">{{ targetAmount }}</text>
            <text class="stats-unit">ml 目标</text>
          </view>
        </view>

        <!-- 完成度 -->
        <view class="progress-text">
          <text :class="{ 'completed': waterPercent >= 100 }">
            {{ waterPercent >= 100 ? '今日目标已达成! 🎉' : `已完成 ${waterPercent}%` }}
          </text>
        </view>
      </view>

      <!-- 快捷喝水按钮 -->
      <view class="quick-add-section">
        <text class="section-title">快速记录</text>
        <view class="quick-buttons">
          <view
            v-for="amount in quickAmounts"
            :key="amount"
            class="quick-btn"
            :class="{ 'active': selectedAmount === amount }"
            @click="selectedAmount = amount"
          >
            <u-icon name="minus-circle" size="36" color="#5B8FF9"></u-icon>
            <text>{{ amount }}ml</text>
          </view>
        </view>
        <button class="add-btn" @click="recordWater">
          <u-icon name="plus" size="40" color="#fff"></u-icon>
          <text>记录喝水</text>
        </button>
      </view>

      <!-- 今日记录 -->
      <view class="records-section">
        <view class="section-header">
          <text class="section-title">今日记录</text>
          <text class="record-count">{{ records.length }} 次</text>
        </view>

        <view v-if="records.length === 0" class="empty-state">
          <u-icon name="clock" size="80" color="#ddd"></u-icon>
          <text>还没有喝水记录，快来打卡吧~</text>
        </view>

        <view v-else class="records-list">
          <view
            v-for="(record, index) in records"
            :key="index"
            class="record-item"
          >
            <view class="record-icon">
              <u-icon name="clock" size="36" color="#5B8FF9"></u-icon>
            </view>
            <view class="record-info">
              <text class="record-amount">+{{ record.amount }}ml</text>
              <text class="record-time">{{ formatTime(record.recordTime) }}</text>
            </view>
            <view class="record-progress">
              <text>{{ calculateProgress(record.amount) }}%</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 健康提示 -->
      <view class="tips-section">
        <view class="tip-card">
          <u-icon name="info-circle" size="40" color="#5AD8A6"></u-icon>
          <view class="tip-content">
            <text class="tip-title">健康小贴士</text>
            <text class="tip-text">{{ currentTip }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 目标设置弹窗 -->
    <view v-if="settingsVisible" class="modal-overlay" @click="closeSettings">
      <view class="modal-content" @click.stop>
        <view class="modal-header">
          <text>设置目标</text>
          <text class="close-btn" @click="closeSettings">✕</text>
        </view>

        <view class="target-options">
          <view
            v-for="target in targetOptions"
            :key="target"
            class="target-option"
            :class="{ 'active': tempTarget === target }"
            @click="tempTarget = target"
          >
            <text class="target-value">{{ target }}ml</text>
            <text class="target-desc">{{ getTargetDesc(target) }}</text>
          </view>
        </view>

        <view class="modal-actions">
          <button class="btn-cancel" @click="closeSettings">取消</button>
          <button class="btn-confirm" @click="saveTarget">保存</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { waterApi } from '../../api/index.js'

const todayAmount = ref(0)
const targetAmount = ref(2000)
const records = ref([])
const selectedAmount = ref(200)
const settingsVisible = ref(false)
const tempTarget = ref(2000)

const quickAmounts = [100, 200, 250, 350, 500]
const targetOptions = [1500, 2000, 2500, 3000, 3500]

const healthTips = [
  '早起一杯水，有助于唤醒身体代谢',
  '饭前30分钟喝水，有助于控制饮食量',
  '运动前后记得补充水分',
  '久坐办公时，每小时喝一杯水',
  '睡前1小时减少饮水，避免夜间频繁起夜',
  '感到口渴时，身体已经轻度缺水了'
]

const currentTip = computed(() => {
  const hour = new Date().getHours()
  if (hour < 9) return healthTips[0]
  if (hour < 12) return healthTips[3]
  if (hour < 14) return healthTips[1]
  if (hour < 18) return healthTips[3]
  return healthTips[4]
})

const todayText = computed(() => {
  return new Date().toLocaleDateString('zh-CN', { month: 'long', day: 'numeric' })
})

const weekText = computed(() => {
  const days = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  return days[new Date().getDay()]
})

const waterPercent = computed(() => {
  return Math.min(Math.round((todayAmount.value / targetAmount.value) * 100), 100)
})

onMounted(() => {
  loadTodayData()
})

const loadTodayData = async () => {
  try {
    const userInfo = uni.getStorageSync('userInfo')
    const userId = userInfo?.id || 1
    const res = await waterApi.getToday(userId)
    if (res) {
      todayAmount.value = res.todayAmount || 0
      targetAmount.value = res.targetAmount || 2000
      records.value = res.records || []
    }
  } catch (e) {
    console.error('加载喝水数据失败', e)
    uni.showToast({ title: '加载失败', icon: 'none' })
  }
}

const recordWater = async () => {
  try {
    const userInfo = uni.getStorageSync('userInfo')
    const userId = userInfo?.id || 1

    await waterApi.record({
      userId: userId,
      amount: selectedAmount.value,
      recordTime: new Date().toISOString()
    })

    uni.showToast({ title: '打卡成功', icon: 'success' })
    await loadTodayData()
  } catch (e) {
    console.error('记录喝水失败', e)
    uni.showToast({ title: '记录失败', icon: 'none' })
  }
}

const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  return `${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

const calculateProgress = (amount) => {
  return Math.round((amount / targetAmount.value) * 100)
}

const showSettings = () => {
  tempTarget.value = targetAmount.value
  settingsVisible.value = true
}

const closeSettings = () => {
  settingsVisible.value = false
}

const saveTarget = () => {
  targetAmount.value = tempTarget.value
  uni.setStorageSync('waterTarget', tempTarget.value)
  uni.showToast({ title: '设置已保存', icon: 'success' })
  closeSettings()
}

const getTargetDesc = (target) => {
  if (target <= 1500) return '轻松模式'
  if (target <= 2000) return '标准推荐'
  if (target <= 2500) return '健康模式'
  return '运动模式'
}

const goBack = () => {
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
.water-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #E6F7FF 0%, #f5f6fa 100%);
}

.nav-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 30rpx;
  padding-top: 60rpx;
  background: transparent;

  .back-btn, .right-btn {
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
}

.content {
  padding: 0 30rpx 40rpx;
}

.date-section {
  text-align: center;
  margin-bottom: 30rpx;

  .date-text {
    font-size: 36rpx;
    font-weight: 600;
    color: #333;
    margin-right: 16rpx;
  }

  .week-text {
    font-size: 28rpx;
    color: #666;
  }
}

.water-cup-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 40rpx;

  .cup-container {
    position: relative;
    margin-bottom: 30rpx;

    .cup {
      width: 200rpx;
      height: 280rpx;
      background: rgba(255, 255, 255, 0.9);
      border: 4rpx solid #5B8FF9;
      border-radius: 0 0 30rpx 30rpx;
      position: relative;
      overflow: hidden;
      box-shadow: 0 8rpx 30rpx rgba(91, 143, 249, 0.2);

      .water {
        position: absolute;
        bottom: 0;
        left: 0;
        right: 0;
        background: linear-gradient(180deg, #5B8FF9 0%, #2E6AD8 100%);
        transition: height 0.5s ease;

        .water-wave {
          position: absolute;
          top: -10rpx;
          left: 0;
          right: 0;
          height: 20rpx;
          background: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 1440 320'%3E%3Cpath fill='%235B8FF9' fill-opacity='1' d='M0,96L48,112C96,128,192,160,288,160C384,160,480,128,576,112C672,96,768,96,864,112C960,128,1056,160,1152,160C1248,160,1344,128,1392,112L1440,96L1440,320L1392,320C1344,320,1248,320,1152,320C1056,320,960,320,864,320C768,320,672,320,576,320C480,320,384,320,288,320C192,320,96,320,48,320L0,320Z'%3E%3C/path%3E%3C/svg%3E");
          background-size: cover;
          animation: wave 2s linear infinite;
        }
      }

      .cup-markings {
        position: absolute;
        right: 0;
        top: 0;
        bottom: 0;
        width: 30rpx;

        .marking {
          position: absolute;
          right: 0;
          width: 20rpx;
          height: 2rpx;
          background: rgba(91, 143, 249, 0.3);
        }
      }
    }

    .cup-stand {
      width: 120rpx;
      height: 20rpx;
      background: rgba(91, 143, 249, 0.3);
      border-radius: 10rpx;
      margin: 0 auto;
      margin-top: 10rpx;
    }
  }

  .water-stats {
    display: flex;
    align-items: baseline;
    gap: 16rpx;
    margin-bottom: 16rpx;

    .stats-item {
      display: flex;
      align-items: baseline;
      gap: 8rpx;

      .stats-num {
        font-size: 56rpx;
        font-weight: bold;
        color: #5B8FF9;

        &.target {
          font-size: 40rpx;
          color: #999;
        }
      }

      .stats-unit {
        font-size: 24rpx;
        color: #666;
      }
    }

    .stats-divider {
      font-size: 40rpx;
      color: #ccc;
    }
  }

  .progress-text {
    text {
      font-size: 28rpx;
      color: #666;

      &.completed {
        color: #5AD8A6;
        font-weight: 600;
      }
    }
  }
}

@keyframes wave {
  0% { transform: translateX(0); }
  100% { transform: translateX(-50%); }
}

.quick-add-section {
  background: #fff;
  border-radius: 20rpx;
  padding: 30rpx;
  margin-bottom: 30rpx;

  .section-title {
    font-size: 30rpx;
    font-weight: 600;
    color: #333;
    display: block;
    margin-bottom: 24rpx;
  }

  .quick-buttons {
    display: flex;
    flex-wrap: wrap;
    gap: 16rpx;
    margin-bottom: 30rpx;

    .quick-btn {
      display: flex;
      align-items: center;
      gap: 8rpx;
      padding: 16rpx 24rpx;
      background: #f5f6fa;
      border-radius: 30rpx;
      border: 2rpx solid transparent;
      transition: all 0.3s;

      text {
        font-size: 26rpx;
        color: #666;
      }

      &.active {
        background: #E6F7FF;
        border-color: #5B8FF9;

        text {
          color: #5B8FF9;
        }
      }
    }
  }

  .add-btn {
    width: 100%;
    height: 90rpx;
    background: linear-gradient(135deg, #5B8FF9 0%, #2E6AD8 100%);
    border-radius: 45rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 12rpx;
    border: none;

    text {
      font-size: 30rpx;
      color: #fff;
    }

    &:active {
      opacity: 0.9;
    }
  }
}

.records-section {
  background: #fff;
  border-radius: 20rpx;
  padding: 30rpx;
  margin-bottom: 30rpx;

  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24rpx;

    .section-title {
      font-size: 30rpx;
      font-weight: 600;
      color: #333;
    }

    .record-count {
      font-size: 26rpx;
      color: #999;
    }
  }

  .empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 60rpx 0;

    text {
      margin-top: 20rpx;
      font-size: 28rpx;
      color: #999;
    }
  }

  .records-list {
    display: flex;
    flex-direction: column;

    .record-item {
      display: flex;
      align-items: center;
      padding: 24rpx 0;
      border-bottom: 1rpx solid #f0f0f0;

      &:last-child {
        border-bottom: none;
      }

      .record-icon {
        width: 60rpx;
        height: 60rpx;
        background: #E6F7FF;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-right: 20rpx;
      }

      .record-info {
        flex: 1;
        display: flex;
        flex-direction: column;

        .record-amount {
          font-size: 30rpx;
          font-weight: 600;
          color: #333;
        }

        .record-time {
          font-size: 24rpx;
          color: #999;
          margin-top: 4rpx;
        }
      }

      .record-progress {
        text {
          font-size: 26rpx;
          color: #5B8FF9;
          font-weight: 500;
        }
      }
    }
  }
}

.tips-section {
  .tip-card {
    display: flex;
    align-items: flex-start;
    gap: 20rpx;
    background: linear-gradient(135deg, #E6FFF2 0%, #F0FFF8 100%);
    border-radius: 20rpx;
    padding: 30rpx;

    .tip-content {
      flex: 1;

      .tip-title {
        font-size: 28rpx;
        font-weight: 600;
        color: #333;
        display: block;
        margin-bottom: 8rpx;
      }

      .tip-text {
        font-size: 26rpx;
        color: #666;
        line-height: 1.5;
      }
    }
  }
}

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
  width: 100%;
  background: #fff;
  border-radius: 30rpx 30rpx 0 0;
  padding: 40rpx 30rpx;

  .modal-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 40rpx;

    text {
      font-size: 36rpx;
      font-weight: 600;
      color: #333;
    }

    .close-btn {
      font-size: 40rpx;
      color: #999;
      padding: 10rpx;
    }
  }

  .target-options {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 20rpx;
    margin-bottom: 40rpx;

    .target-option {
      padding: 30rpx;
      background: #f5f6fa;
      border-radius: 16rpx;
      text-align: center;
      border: 2rpx solid transparent;
      transition: all 0.3s;

      .target-value {
        display: block;
        font-size: 36rpx;
        font-weight: 600;
        color: #333;
        margin-bottom: 8rpx;
      }

      .target-desc {
        font-size: 24rpx;
        color: #999;
      }

      &.active {
        background: #E6F7FF;
        border-color: #5B8FF9;

        .target-value {
          color: #5B8FF9;
        }
      }
    }
  }

  .modal-actions {
    display: flex;
    gap: 20rpx;

    button {
      flex: 1;
      height: 90rpx;
      border-radius: 45rpx;
      font-size: 30rpx;
      border: none;
    }

    .btn-cancel {
      background: #f5f6fa;
      color: #666;
    }

    .btn-confirm {
      background: linear-gradient(135deg, #5B8FF9 0%, #2E6AD8 100%);
      color: #fff;
    }
  }
}
</style>
