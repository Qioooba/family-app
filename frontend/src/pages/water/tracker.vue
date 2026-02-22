<template>
  <view class="water-page">
    <!-- 顶部导航 -->
    <view class="nav-bar"
>
      <view class="back-btn" @click="goBack">
        <u-icon name="arrow-left" size="40" color="#333"></u-icon>
      </view>
      <text class="title">喝水打卡</text>
      <view class="right-btn" @click="showSettings">
        <u-icon name="setting" size="36" color="#333"></u-icon>
      </view>
    </view>

    <view class="content">
      <!-- 主视觉区域 -->
      <view class="main-visual">
        <!-- 水滴进度 -->
        <view class="water-drop-container">
          <view class="water-drop"
>
            <view class="drop-bg"
></view>
            <view 
              class="drop-fill"
              :style="{ height: waterPercent + '%' }"
            >
              <view class="wave wave1"></view>
              <view class="wave wave2"></view>
            </view>
            
            <view class="drop-content"
003e
              <text class="water-amount">{{ todayIntake }}</text>
              <text class="water-unit">毫升</text>
              <text class="water-target">目标 {{ dailyTarget }}ml</text>
            </view>
          </view>

          <!-- 完成度百分比 -->
          <view class="percent-badge"
>
            <text class="percent-num">{{ waterPercent }}%</text>
          </view>
        </view>

        <!-- 8杯水进度 -->
        <view class="cups-section"
003e
          <text class="cups-title">今日8杯水</text>
          
          <view class="cups-grid"
>
            <view
              v-for="(cup, index) in 8"
              :key="index"
              class="cup-item"
              :class="{ 
                filled: index < filledCups,
                active: index === filledCups && waterPercent < 100
              }"
              @click="addWater(cupSize, index)"
            >
              <view class="cup-icon"
>
                <text v-if="index < filledCups">💧</text>
                <text v-else>🥛</text>
              </view>
              
              <text class="cup-label"
003e
                {{ cupLabels[index] || `第${index + 1}杯` }}
              </text>
            </view>
          </view>
        </view>
      </view>

      <!-- 快捷添加 -->
      <view class="quick-add-section"
>
        <text class="section-title">快捷添加</text>
        
        <view class="quick-buttons"
>
          <view
            v-for="item in quickAmounts"
            :key="item.amount"
            class="quick-btn"
            @click="addWater(item.amount)"
          >
            <view class="btn-icon">{{ item.icon }}</view>
            <text class="btn-amount">+{{ item.amount }}ml</text>
            <text class="btn-name">{{ item.name }}</text>
          </view>
        </view>
      </view>

      <!-- 今日记录 -->
      <view class="records-section">
        <view class="section-header"
>
          <text class="section-title">今日记录</text>
          <text class="record-count">{{ waterRecords.length }}次</text>
        </view>

        <scroll-view class="records-list" scroll-y style="max-height: 400rpx;"
>
          <view
            v-for="(record, index) in waterRecords"
            :key="index"
            class="record-item"
          >
            <view class="record-info"
003e
              <view class="record-icon">{{ record.icon }}</view>
              <view class="record-detail">
                <text class="record-name">{{ record.name }}</text>
                <text class="record-time">{{ record.time }}</text>
              </view>
            </view>
            
            <view class="record-amount"
>
              <text>+{{ record.amount }}ml</text>
            </view>
            
            <view class="record-delete" @click="deleteRecord(index)"
>
              <u-icon name="trash" size="28" color="#FF4D4F"></u-icon>
            </view>
          </view>
          
          <view v-if="waterRecords.length === 0" class="empty-records"
003e
            <u-icon name="file-text" size="60" color="#ddd"></u-icon>
            <text>今天还没有喝水记录</text>
          </view>
        </scroll-view>
      </view>

      <!-- 喝水提醒 -->
      <view class="reminder-section"
003e
        <view class="reminder-card"
>
          <view class="reminder-info"
003e
            <u-icon name="bell" size="40" color="#1890FF"></u-icon>
            <view class="reminder-text"
>
              <text class="reminder-title">喝水提醒</text>
              <text class="reminder-desc">{{ reminderEnabled ? '已开启定时提醒' : '定时提醒已关闭' }}</text>
            </view>
          </view>
          
          <switch 
            :checked="reminderEnabled" 
            color="#1890FF"
            @change="toggleReminder"
          />
        </view>

        <view v-if="reminderEnabled" class="reminder-times"
>
          <text class="times-label">提醒时间</text>
          
          <view class="times-list">
            <view
              v-for="time in reminderTimes"
              :key="time"
              class="time-tag"
            >
              {{ time }}
            </view>
          </view>
        </view>
      </view>

      <!-- 统计 -->
      <view class="stats-section">
        <text class="section-title">本周统计</text>
        
        <view class="week-chart"
>
          <view
            v-for="(day, index) in weekData"
            :key="index"
            class="day-bar"
          >
            <view class="bar-container">
              <view 
                class="bar-fill"
                :style="{ height: (day.amount / dailyTarget * 100) + '%' }"
                :class="{ completed: day.amount >= dailyTarget }"
              ></view>
            </view>
            
            <text class="day-label">{{ day.short }}</text>
            
            <text v-if="day.isToday" class="today-dot"></text>
          </view>
        </view>
      </view>
    </view>

    <!-- 设置弹窗 -->
    <u-popup
      v-model:show="settingsVisible"
      mode="bottom"
      round
      closeable
    >
      <view class="settings-content"
003e
        <view class="settings-header"
>
          <text class="settings-title">喝水设置</text>
        </view>

        <view class="settings-body"
>
          <view class="setting-item">
            <text class="setting-label">每日目标</text>
            <view class="setting-value"
>
              <input
                v-model.number="dailyTarget"
                type="number"
                class="target-input"
              />
              <text class="unit">ml</text>
            </view>
          </view>

          <view class="setting-item">
            <text class="setting-label"
>每杯水量</text>
            <view class="setting-value">
              <input
                v-model.number="cupSize"
                type="number"
                class="target-input"
              />
              <text class="unit">ml</text>
            </view>
          </view>

          <view class="setting-item"
>
            <text class="setting-label">提醒间隔</text>
            <view class="interval-options"
>
              <view
                v-for="interval in reminderIntervals"
                :key="interval"
                class="interval-option"
                :class="{ active: reminderInterval === interval }"
                @click="reminderInterval = interval"
              >
                {{ interval }}分钟
              </view>
            </view>
          </view>
        </view>

        <view class="settings-footer"
>
          <view class="btn-confirm" @click="saveSettings">保存设置</view>
        </view>
      </view>
    </u-popup>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import dayjs from 'dayjs'

// 响应式数据
const dailyTarget = ref(2000)
const cupSize = ref(250)
const todayIntake = ref(1250)
const reminderEnabled = ref(true)
const reminderInterval = ref(60)
const settingsVisible = ref(false)

// 快捷水量
const quickAmounts = [
  { amount: 100, name: '小口', icon: '☕' },
  { amount: 200, name: '半杯', icon: '🥤' },
  { amount: 250, name: '一杯', icon: '🥛' },
  { amount: 500, name: '一瓶', icon: '🍶' }
]

// 水杯标签
const cupLabels = ['晨起', '早餐', '上午', '午餐', '下午', '晚餐', '晚上', '睡前']

// 提醒时间
const reminderTimes = ref(['09:00', '11:00', '14:00', '16:00', '19:00', '21:00'])
const reminderIntervals = [30, 60, 90, 120]

// 喝水记录
const waterRecords = ref([
  { name: '晨起一杯水', amount: 250, time: '07:30', icon: '☀️' },
  { name: '早餐', amount: 200, time: '08:00', icon: '🍳' },
  { name: '上午补水', amount: 250, time: '10:30', icon: '💧' },
  { name: '午餐', amount: 300, time: '12:00', icon: '🍚' },
  { name: '下午补水', amount: 250, time: '15:00', icon: '💧' }
])

// 本周数据
const weekData = ref([
  { short: '一', amount: 2000, isToday: false },
  { short: '二', amount: 1800, isToday: false },
  { short: '三', amount: 2200, isToday: false },
  { short: '四', amount: 1500, isToday: false },
  { short: '五', amount: 2000, isToday: false },
  { short: '六', amount: 1700, isToday: false },
  { short: '日', amount: 1250, isToday: true }
])

// 计算属性
const waterPercent = computed(() => {
  return Math.min(Math.round((todayIntake.value / dailyTarget.value) * 100), 100)
})

const filledCups = computed(() => {
  return Math.floor(todayIntake.value / cupSize.value)
})

// 页面加载
onMounted(() => {
  loadSettings()
  loadTodayRecords()
})

// 加载设置
const loadSettings = () => {
  const settings = uni.getStorageSync('waterSettings')
  if (settings) {
    dailyTarget.value = settings.dailyTarget || 2000
    cupSize.value = settings.cupSize || 250
    reminderEnabled.value = settings.reminderEnabled !== false
    reminderInterval.value = settings.reminderInterval || 60
  }
}

// 加载今日记录
const loadTodayRecords = () => {
  const today = dayjs().format('YYYY-MM-DD')
  const records = uni.getStorageSync(`waterRecords_${today}`)
  if (records) {
    waterRecords.value = records
    // 重新计算今日摄入量
    todayIntake.value = records.reduce((sum, r) => sum + r.amount, 0)
  }
}

// 添加水量
const addWater = (amount, cupIndex) => {
  // 如果是点击已填满的杯子，不做处理
  if (cupIndex !== undefined && cupIndex < filledCups.value) {
    uni.showToast({ title: '这杯水已经喝过啦', icon: 'none' })
    return
  }
  
  todayIntake.value += amount
  
  const record = {
    name: cupIndex !== undefined ? cupLabels[cupIndex] || `第${cupIndex + 1}杯` : '喝水',
    amount,
    time: dayjs().format('HH:mm'),
    icon: '💧'
  }
  
  waterRecords.value.unshift(record)
  
  // 保存记录
  const today = dayjs().format('YYYY-MM-DD')
  uni.setStorageSync(`waterRecords_${today}`, waterRecords.value)
  
  // 检查是否达成目标
  if (todayIntake.value >= dailyTarget.value && todayIntake.value - amount < dailyTarget.value) {
    uni.showModal({
      title: '🎉 恭喜！',
      content: '今日喝水目标已达成！继续保持健康生活习惯！',
      showCancel: false
    })
  } else {
    uni.showToast({ title: `+${amount}ml`, icon: 'none' })
  }
  
  // 更新本周数据
  updateWeekData()
}

// 删除记录
const deleteRecord = (index) => {
  uni.showModal({
    title: '确认删除',
    content: '删除这条记录？',
    success: (res) => {
      if (res.confirm) {
        const record = waterRecords.value[index]
        todayIntake.value -= record.amount
        waterRecords.value.splice(index, 1)
        
        // 保存记录
        const today = dayjs().format('YYYY-MM-DD')
        uni.setStorageSync(`waterRecords_${today}`, waterRecords.value)
        
        updateWeekData()
        uni.showToast({ title: '已删除', icon: 'success' })
      }
    }
  })
}

// 更新本周数据
const updateWeekData = () => {
  const todayIndex = weekData.value.findIndex(d => d.isToday)
  if (todayIndex > -1) {
    weekData.value[todayIndex].amount = todayIntake.value
  }
}

// 切换提醒
const toggleReminder = (e) => {
  reminderEnabled.value = e.detail.value
  
  if (reminderEnabled.value) {
    uni.showToast({ title: '提醒已开启', icon: 'success' })
    // 这里可以设置定时提醒
    scheduleReminders()
  } else {
    uni.showToast({ title: '提醒已关闭', icon: 'none' })
    // 取消提醒
    cancelReminders()
  }
  
  saveSettings()
}

// 设置提醒
const scheduleReminders = () => {
  // #ifdef APP-PLUS
  // 实际应用中这里使用原生通知
  // #endif
}

// 取消提醒
const cancelReminders = () => {
  // #ifdef APP-PLUS
  // #endif
}

// 显示设置
const showSettings = () => {
  settingsVisible.value = true
}

// 保存设置
const saveSettings = () => {
  const settings = {
    dailyTarget: dailyTarget.value,
    cupSize: cupSize.value,
    reminderEnabled: reminderEnabled.value,
    reminderInterval: reminderInterval.value
  }
  uni.setStorageSync('waterSettings', settings)
  settingsVisible.value = false
  uni.showToast({ title: '设置已保存', icon: 'success' })
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
    width: 60rpx;
    height: 60rpx;
    display: flex;
    align-items: center;
    justify-content: center;
  }
}

.content {
  padding: 0 30rpx 50rpx;
}

// 主视觉区域
.main-visual {
  background: #fff;
  border-radius: 32rpx;
  padding: 40rpx;
  margin-bottom: 30rpx;
}

// 水滴容器
.water-drop-container {
  display: flex;
  justify-content: center;
  position: relative;
  margin-bottom: 40rpx;
}

.water-drop {
  position: relative;
  width: 320rpx;
  height: 380rpx;
  border-radius: 50% 50% 50% 50% / 60% 60% 40% 40%;
  overflow: hidden;
  background: #f0f8ff;
  box-shadow: 
    inset 0 0 40rpx rgba(24, 144, 255, 0.1),
    0 8rpx 32rpx rgba(24, 144, 255, 0.15);

  .drop-bg {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: linear-gradient(180deg, #f0f8ff 0%, #e6f7ff 100%);
  }

  .drop-fill {
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    background: linear-gradient(180deg, #40A9FF 0%, #1890FF 100%);
    transition: height 0.5s ease;
    overflow: hidden;

    .wave {
      position: absolute;
      width: 200%;
      height: 40rpx;
      background: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 1440 320'%3E%3Cpath fill='%2340A9FF' fill-opacity='0.3' d='M0,192L48,197.3C96,203,192,213,288,229.3C384,245,480,267,576,250.7C672,235,768,181,864,181.3C960,181,1056,235,1152,234.7C1248,235,1344,181,1392,154.7L1440,128L1440,320L1392,320C1344,320,1248,320,1152,320C1056,320,960,320,864,320C768,320,672,320,576,320C480,320,384,320,288,320C192,320,96,320,48,320L0,320Z'%3E%3C/path%3E%3C/svg%3E");
      background-size: 50% 100%;
      animation: wave 3s linear infinite;

      &.wave2 {
        top: 10rpx;
        opacity: 0.5;
        animation-delay: -1s;
      }
    }
  }

  .drop-content {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    text-align: center;
    z-index: 1;

    .water-amount {
      font-size: 72rpx;
      font-weight: 700;
      color: #333;
      display: block;
      line-height: 1;
    }

    .water-unit {
      font-size: 28rpx;
      color: #666;
      display: block;
      margin-top: 8rpx;
    }

    .water-target {
      font-size: 24rpx;
      color: #999;
      display: block;
      margin-top: 16rpx;
    }
  }
}

@keyframes wave {
  0% {
    transform: translateX(0);
  }
  100% {
    transform: translateX(-50%);
  }
}

.percent-badge {
  position: absolute;
  top: 20rpx;
  right: 40rpx;
  width: 100rpx;
  height: 100rpx;
  background: #1890FF;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8rpx 24rpx rgba(24, 144, 255, 0.3);

  .percent-num {
    font-size: 32rpx;
    font-weight: 700;
    color: #fff;
  }
}

// 8杯水进度
.cups-section {
  .cups-title {
    font-size: 28rpx;
    font-weight: 600;
    color: #333;
    text-align: center;
    display: block;
    margin-bottom: 24rpx;
  }

  .cups-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 20rpx;

    .cup-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 20rpx 10rpx;
      background: #f5f6fa;
      border-radius: 16rpx;
      transition: all 0.3s;

      .cup-icon {
        font-size: 40rpx;
        margin-bottom: 8rpx;
      }

      .cup-label {
        font-size: 22rpx;
        color: #999;
      }

      &.filled {
        background: #E6F7FF;

        .cup-label {
          color: #1890FF;
        }
      }

      &.active {
        background: #FFF7E6;
        border: 2rpx solid #FAAD14;
        animation: pulse 1.5s infinite;

        .cup-label {
          color: #FAAD14;
        }
      }
    }
  }
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.05);
  }
}

// 快捷添加
.quick-add-section {
  margin-bottom: 30rpx;

  .section-title {
    font-size: 30rpx;
    font-weight: 600;
    color: #333;
    margin-bottom: 20rpx;
    display: block;
  }

  .quick-buttons {
    display: flex;
    gap: 20rpx;

    .quick-btn {
      flex: 1;
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 24rpx;
      background: #fff;
      border-radius: 16rpx;
      box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);

      .btn-icon {
        font-size: 48rpx;
        margin-bottom: 12rpx;
      }

      .btn-amount {
        font-size: 32rpx;
        font-weight: 600;
        color: #1890FF;
      }

      .btn-name {
        font-size: 24rpx;
        color: #999;
        margin-top: 4rpx;
      }
    }
  }
}

// 记录区域
.records-section {
  background: #fff;
  border-radius: 24rpx;
  padding: 30rpx;
  margin-bottom: 30rpx;

  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20rpx;

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
}

.record-item {
  display: flex;
  align-items: center;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #f5f5f5;

  &:last-child {
    border-bottom: none;
  }

  .record-info {
    flex: 1;
    display: flex;
    align-items: center;
    gap: 16rpx;

    .record-icon {
      font-size: 36rpx;
    }

    .record-detail {
      .record-name {
        font-size: 28rpx;
        color: #333;
        display: block;
      }

      .record-time {
        font-size: 22rpx;
        color: #999;
      }
    }
  }

  .record-amount {
    font-size: 28rpx;
    font-weight: 600;
    color: #1890FF;
    margin-right: 20rpx;
  }

  .record-delete {
    padding: 10rpx;
  }
}

.empty-records {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 60rpx 0;

  text {
    font-size: 26rpx;
    color: #999;
    margin-top: 16rpx;
  }
}

// 提醒区域
.reminder-section {
  margin-bottom: 30rpx;

  .reminder-card {
    display: flex;
    justify-content: space-between;
    align-items: center;
    background: #fff;
    border-radius: 16rpx;
    padding: 24rpx 30rpx;

    .reminder-info {
      display: flex;
      align-items: center;
      gap: 16rpx;

      .reminder-text {
        .reminder-title {
          font-size: 30rpx;
          font-weight: 500;
          color: #333;
          display: block;
        }

        .reminder-desc {
          font-size: 24rpx;
          color: #999;
          margin-top: 4rpx;
          display: block;
        }
      }
    }
  }

  .reminder-times {
    margin-top: 20rpx;
    padding: 20rpx 30rpx;
    background: #fff;
    border-radius: 16rpx;

    .times-label {
      font-size: 26rpx;
      color: #666;
      margin-bottom: 16rpx;
      display: block;
    }

    .times-list {
      display: flex;
      flex-wrap: wrap;
      gap: 16rpx;

      .time-tag {
        padding: 10rpx 20rpx;
        background: #E6F7FF;
        border-radius: 8rpx;
        font-size: 24rpx;
        color: #1890FF;
      }
    }
  }
}

// 统计区域
.stats-section {
  background: #fff;
  border-radius: 24rpx;
  padding: 30rpx;

  .section-title {
    font-size: 30rpx;
    font-weight: 600;
    color: #333;
    margin-bottom: 30rpx;
    display: block;
  }
}

.week-chart {
  display: flex;
  justify-content: space-around;
  align-items: flex-end;
  height: 200rpx;

  .day-bar {
    display: flex;
    flex-direction: column;
    align-items: center;
    flex: 1;
    position: relative;

    .bar-container {
      width: 40rpx;
      height: 140rpx;
      background: #f5f5f5;
      border-radius: 20rpx;
      overflow: hidden;
      position: relative;

      .bar-fill {
        position: absolute;
        bottom: 0;
        left: 0;
        right: 0;
        background: #1890FF;
        border-radius: 20rpx;
        transition: height 0.3s;

        &.completed {
          background: #52C41A;
        }
      }
    }

    .day-label {
      font-size: 24rpx;
      color: #999;
      margin-top: 12rpx;
    }

    .today-dot {
      position: absolute;
      bottom: -16rpx;
      width: 8rpx;
      height: 8rpx;
      background: #1890FF;
      border-radius: 50%;
    }
  }
}

// 设置弹窗
.settings-content {
  padding: 30rpx 0;

  .settings-header {
    text-align: center;
    padding: 0 30rpx 30rpx;
    border-bottom: 1rpx solid #f5f5f5;

    .settings-title {
      font-size: 32rpx;
      font-weight: 600;
      color: #333;
    }
  }

  .settings-body {
    padding: 30rpx;
  }

  .setting-item {
    margin-bottom: 30rpx;

    .setting-label {
      font-size: 28rpx;
      color: #333;
      margin-bottom: 16rpx;
      display: block;
    }

    .setting-value {
      display: flex;
      align-items: center;
      background: #f5f6fa;
      border-radius: 12rpx;
      padding: 0 24rpx;

      .target-input {
        flex: 1;
        height: 80rpx;
        font-size: 36rpx;
        color: #333;
        text-align: center;
        background: transparent;
      }

      .unit {
        font-size: 28rpx;
        color: #666;
      }
    }

    .interval-options {
      display: flex;
      gap: 16rpx;

      .interval-option {
        flex: 1;
        padding: 20rpx;
        background: #f5f6fa;
        border-radius: 12rpx;
        text-align: center;
        font-size: 26rpx;
        color: #666;
        border: 2rpx solid transparent;

        &.active {
          border-color: #1890FF;
          color: #1890FF;
          background: #E6F7FF;
        }
      }
    }
  }

  .settings-footer {
    padding: 0 30rpx;

    .btn-confirm {
      padding: 24rpx 0;
      background: #1890FF;
      border-radius: 12rpx;
      text-align: center;
      font-size: 30rpx;
      color: #fff;
    }
  }
}
</style>