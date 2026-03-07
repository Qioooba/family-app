<template>
  <view class="game-page">
    <!-- 头部 -->
    <view class="header">
      <view class="header-left" @click="goBack">
        <u-icon name="arrow-left" color="#fff" size="40"></u-icon>
      </view>
      <view class="header-title">家庭运动会</view>
      <view class="header-right" @click="showRules">
        <text>规则</text>
      </view>
    </view>

    <!-- 运动项目选择 -->
    <view class="sports-tabs">
      <view 
        v-for="item in sportsTypes" 
        :key="item.type"
        class="tab-item"
        :class="{ active: currentSport === item.type }"
        @click="switchSport(item.type)"
      >
        <text class="tab-icon">{{ item.icon }}</text>
        <text class="tab-name">{{ item.name }}</text>
      </view>
    </view>

    <!-- 当前运动项目 -->
    <view class="sport-card">
      <view class="sport-header">
        <text class="sport-icon">{{ currentSportInfo.icon }}</text>
        <view class="sport-info">
          <text class="sport-name">{{ currentSportInfo.name }}</text>
          <text class="sport-desc">{{ currentSportInfo.desc }}</text>
        </view>
      </view>
      
      <!-- 计时/计数模式切换 -->
      <view class="mode-switch">
        <view 
          class="mode-item"
          :class="{ active: mode === 'timer' }"
          @click="mode = 'timer'"
        >
          <text>计时模式</text>
        </view>
        <view 
          class="mode-item"
          :class="{ active: mode === 'counter' }"
          @click="mode = 'counter'"
        >
          <text>计数模式</text>
        </view>
      </view>

      <!-- 计时器 -->
      <view class="timer-section" v-if="mode === 'timer'">
        <view class="timer-display">
          <text class="timer-value">{{ formatTime(timerValue) }}</text>
        </view>
        <view class="timer-controls">
          <u-button 
            :type="isRunning ? 'warning' : 'primary'" 
            :text="isRunning ? '暂停' : (timerValue > 0 ? '继续' : '开始')"
            @click="toggleTimer"
          ></u-button>
          <u-button 
            type="info" 
            text="重置"
            @click="resetTimer"
            v-if="timerValue > 0"
          ></u-button>
          <u-button 
            type="success" 
            text="完成"
            @click="finishTimer"
            v-if="timerValue > 0 && !isRunning"
          ></u-button>
        </view>
      </view>

      <!-- 计数器 -->
      <view class="counter-section" v-else>
        <view class="counter-display">
          <text class="counter-value">{{ counterValue }}</text>
          <text class="counter-unit">{{ currentSportInfo.unit }}</text>
        </view>
        <view class="counter-controls">
          <view class="counter-btn minus" @click="decrementCounter">
            <u-icon name="minus" color="#fff" size="48"></u-icon>
          </view>
          <view class="counter-btn plus" @click="incrementCounter">
            <u-icon name="plus" color="#fff" size="48"></u-icon>
          </view>
        </view>
        <u-button 
          type="success" 
          text="记录成绩"
          @click="recordCounter"
          v-if="counterValue > 0"
        ></u-button>
      </view>
    </view>

    <!-- 成绩记录 -->
    <view class="records-section">
      <view class="section-title">
        <text>今日成绩</text>
        <text class="record-count">{{ todayRecords.length }}次</text>
      </view>
      <view class="records-list">
        <view 
          v-for="(record, index) in todayRecords" 
          :key="index"
          class="record-item"
        >
          <view class="record-info">
            <text class="record-time">{{ formatRecordTime(record.time) }}</text>
            <text class="record-type">{{ record.sportName }}</text>
          </view>
          <text class="record-value">{{ record.value }}{{ record.unit }}</text>
        </view>
        <view class="empty-records" v-if="todayRecords.length === 0">
          <text>暂无记录，开始运动吧！</text>
        </view>
      </view>
    </view>

    <!-- 排行榜 -->
    <view class="rank-section">
      <view class="section-title">
        <text>家庭排行榜</text>
      </view>
      <view class="rank-list">
        <view 
          v-for="(member, index) in rankings" 
          :key="index"
          class="rank-item"
          :class="{ 'rank-top3': index < 3 }"
        >
          <view class="rank-number">{{ index + 1 }}</view>
          <image :src="member.avatar" class="rank-avatar" mode="aspectFill"></image>
          <view class="rank-info">
            <text class="rank-name">{{ member.name }}</text>
            <text class="rank-score">{{ member.totalScore }}分</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 规则弹窗 -->
    <u-popup v-model:show="showRulePopup" mode="center" round="20">
      <view class="rules-popup">
        <view class="rules-title">游戏规则</view>
        <view class="rules-content">
          <text class="rule-item">1. 选择运动项目：跳绳、俯卧撑、平板支撑等</text>
          <text class="rule-item">2. 计时模式：记录完成运动所用的时间</text>
          <text class="rule-item">3. 计数模式：记录完成的次数（如跳绳次数）</text>
          <text class="rule-item">4. 每次运动后可以记录成绩</text>
          <text class="rule-item">5. 家庭成员之间可以相互挑战</text>
        </view>
        <u-button type="primary" text="知道了" @click="showRulePopup = false"></u-button>
      </view>
    </u-popup>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'

// 运动项目类型
const sportsTypes = [
  { type: 'rope', name: '跳绳', icon: '🪢', unit: '个', desc: '计时1分钟，记录跳绳次数' },
  { type: 'pushup', name: '俯卧撑', icon: '💪', unit: '个', desc: '记录完成的俯卧撑个数' },
  { type: 'plank', name: '平板支撑', icon: '🧘', unit: '秒', desc: '记录保持平板支撑的时间' },
  { type: 'squat', name: '深蹲', icon: '🦵', unit: '个', desc: '记录完成的深蹲个数' },
  { type: 'running', name: '跑步', icon: '🏃', unit: '米', desc: '记录跑步距离' }
]

const currentSport = ref('rope')
const mode = ref('timer')
const timerValue = ref(0)
const counterValue = ref(0)
const isRunning = ref(false)
const timerInterval = ref(null)
const showRulePopup = ref(false)

// 当前运动项目信息
const currentSportInfo = computed(() => {
  return sportsTypes.find(s => s.type === currentSport.value) || sportsTypes[0]
})

// 今日记录
const todayRecords = ref([])

// 排行榜
const rankings = ref([
  { name: '爸爸', avatar: '/static/avatar-default.svg', totalScore: 1250 },
  { name: '妈妈', avatar: '/static/avatar-default.svg', totalScore: 1180 },
  { name: '小明', avatar: '/static/avatar-default.svg', totalScore: 980 }
])

// 切换运动项目
const switchSport = (type) => {
  currentSport.value = type
  resetTimer()
  counterValue.value = 0
}

// 格式化时间
const formatTime = (seconds) => {
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
}

// 格式化记录时间
const formatRecordTime = (time) => {
  const date = new Date(time)
  return `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
}

// 计时器控制
const toggleTimer = () => {
  if (isRunning.value) {
    clearInterval(timerInterval.value)
    isRunning.value = false
  } else {
    isRunning.value = true
    timerInterval.value = setInterval(() => {
      timerValue.value++
    }, 1000)
  }
}

const resetTimer = () => {
  clearInterval(timerInterval.value)
  isRunning.value = false
  timerValue.value = 0
}

const finishTimer = () => {
  clearInterval(timerInterval.value)
  isRunning.value = false
  
  // 记录成绩
  const record = {
    sportName: currentSportInfo.value.name,
    value: timerValue.value,
    unit: '秒',
    time: new Date().getTime()
  }
  todayRecords.value.unshift(record)
  
  uni.showToast({
    title: '记录成功！',
    icon: 'success'
  })
  
  resetTimer()
}

// 计数器控制
const incrementCounter = () => {
  counterValue.value++
}

const decrementCounter = () => {
  if (counterValue.value > 0) {
    counterValue.value--
  }
}

const recordCounter = () => {
  const record = {
    sportName: currentSportInfo.value.name,
    value: counterValue.value,
    unit: currentSportInfo.value.unit,
    time: new Date().getTime()
  }
  todayRecords.value.unshift(record)
  
  uni.showToast({
    title: '记录成功！',
    icon: 'success'
  })
  
  counterValue.value = 0
}

// 返回上一页
const goBack = () => {
  uni.navigateBack()
}

// 显示规则
const showRules = () => {
  showRulePopup.value = true
}

onMounted(() => {
  // 加载今日记录
  const records = uni.getStorageSync('sportsRecords') || []
  todayRecords.value = records
})
</script>

<style lang="scss" scoped>
.game-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding-bottom: 40rpx;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 100rpx 32rpx 30rpx;
  
  .header-left, .header-right {
    width: 80rpx;
    height: 80rpx;
    display: flex;
    align-items: center;
    justify-content: center;
  }
  
  .header-right {
    text {
      color: #fff;
      font-size: 28rpx;
    }
  }
  
  .header-title {
    font-size: 36rpx;
    font-weight: bold;
    color: #fff;
  }
}

.sports-tabs {
  display: flex;
  padding: 20rpx 32rpx;
  gap: 20rpx;
  overflow-x: auto;
  
  .tab-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 20rpx 30rpx;
    background: rgba(255, 255, 255, 0.2);
    border-radius: 20rpx;
    min-width: 120rpx;
    
    &.active {
      background: #fff;
      
      .tab-name {
        color: #667eea;
      }
    }
    
    .tab-icon {
      font-size: 48rpx;
      margin-bottom: 8rpx;
    }
    
    .tab-name {
      font-size: 24rpx;
      color: #fff;
    }
  }
}

.sport-card {
  margin: 20rpx 32rpx;
  padding: 40rpx;
  background: #fff;
  border-radius: 30rpx;
  
  .sport-header {
    display: flex;
    align-items: center;
    margin-bottom: 30rpx;
    
    .sport-icon {
      font-size: 80rpx;
      margin-right: 20rpx;
    }
    
    .sport-info {
      flex: 1;
      
      .sport-name {
        font-size: 36rpx;
        font-weight: bold;
        color: #333;
        display: block;
      }
      
      .sport-desc {
        font-size: 24rpx;
        color: #999;
      }
    }
  }
}

.mode-switch {
  display: flex;
  background: #f5f5f5;
  border-radius: 16rpx;
  padding: 8rpx;
  margin-bottom: 30rpx;
  
  .mode-item {
    flex: 1;
    text-align: center;
    padding: 20rpx;
    border-radius: 12rpx;
    
    &.active {
      background: #667eea;
      
      text {
        color: #fff;
      }
    }
    
    text {
      font-size: 28rpx;
      color: #666;
    }
  }
}

.timer-section, .counter-section {
  text-align: center;
}

.timer-display {
  margin: 40rpx 0;
  
  .timer-value {
    font-size: 120rpx;
    font-weight: bold;
    color: #667eea;
    font-family: 'DIN Alternate', monospace;
  }
}

.timer-controls {
  display: flex;
  justify-content: center;
  gap: 20rpx;
  
  ::v-deep .u-button {
    min-width: 160rpx;
  }
}

.counter-display {
  margin: 40rpx 0;
  
  .counter-value {
    font-size: 120rpx;
    font-weight: bold;
    color: #667eea;
  }
  
  .counter-unit {
    font-size: 36rpx;
    color: #999;
    margin-left: 20rpx;
  }
}

.counter-controls {
  display: flex;
  justify-content: center;
  gap: 60rpx;
  margin-bottom: 40rpx;
  
  .counter-btn {
    width: 120rpx;
    height: 120rpx;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    
    &.minus {
      background: #ff6b6b;
    }
    
    &.plus {
      background: #52c41a;
    }
  }
}

.records-section, .rank-section {
  margin: 20rpx 32rpx;
  padding: 30rpx;
  background: #fff;
  border-radius: 30rpx;
  
  .section-title {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20rpx;
    
    text {
      font-size: 32rpx;
      font-weight: bold;
      color: #333;
    }
    
    .record-count {
      font-size: 24rpx;
      color: #667eea;
      background: rgba(102, 126, 234, 0.1);
      padding: 8rpx 20rpx;
      border-radius: 20rpx;
    }
  }
}

.record-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #f5f5f5;
  
  &:last-child {
    border-bottom: none;
  }
  
  .record-info {
    display: flex;
    flex-direction: column;
    
    .record-time {
      font-size: 24rpx;
      color: #999;
    }
    
    .record-type {
      font-size: 28rpx;
      color: #333;
      margin-top: 4rpx;
    }
  }
  
  .record-value {
    font-size: 32rpx;
    font-weight: bold;
    color: #667eea;
  }
}

.empty-records {
  text-align: center;
  padding: 60rpx;
  
  text {
    font-size: 28rpx;
    color: #999;
  }
}

.rank-item {
  display: flex;
  align-items: center;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #f5f5f5;
  
  &:last-child {
    border-bottom: none;
  }
  
  &.rank-top3 {
    .rank-number {
      background: linear-gradient(135deg, #ffd700, #ffaa00);
      color: #fff;
    }
  }
  
  .rank-number {
    width: 48rpx;
    height: 48rpx;
    border-radius: 50%;
    background: #f5f5f5;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 24rpx;
    font-weight: bold;
    color: #666;
    margin-right: 20rpx;
  }
  
  .rank-avatar {
    width: 80rpx;
    height: 80rpx;
    border-radius: 50%;
    margin-right: 20rpx;
  }
  
  .rank-info {
    flex: 1;
    
    .rank-name {
      font-size: 28rpx;
      color: #333;
      display: block;
    }
    
    .rank-score {
      font-size: 24rpx;
      color: #667eea;
      margin-top: 4rpx;
    }
  }
}

.rules-popup {
  padding: 40rpx;
  width: 600rpx;
  
  .rules-title {
    font-size: 36rpx;
    font-weight: bold;
    text-align: center;
    margin-bottom: 30rpx;
    color: #333;
  }
  
  .rules-content {
    margin-bottom: 30rpx;
    
    .rule-item {
      display: block;
      font-size: 28rpx;
      color: #666;
      line-height: 1.8;
      margin-bottom: 10rpx;
    }
  }
}
</style>
