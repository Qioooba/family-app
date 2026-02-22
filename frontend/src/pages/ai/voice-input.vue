<template>
  <view class="voice-container">
    <!-- 自定义导航栏 -->
    <view class="custom-nav">
      <view class="nav-back" @click="goBack">
        <text class="icon">←</text>
      </view>
      <text class="nav-title">语音输入</text>
      <view class="nav-right">
        <text class="icon">🎙️</text>
      </view>
    </view>

    <!-- 语音录制区域 -->
    <view class="voice-record-area" :class="{ recording: isRecording }">
      <!-- 波纹动画 -->
      <view class="wave-container">
        <view 
          v-for="(n, index) in 5" :key="index" 
          class="wave-ring"
          :class="{ active: isRecording }"
          :style="getWaveStyle(n)"
        ></text>
        
        <!-- 麦克风按钮 -->
        <view 
          class="mic-button"
          :class="{ recording: isRecording, paused: isPaused }"
          @touchstart="startRecord"
          @touchend="stopRecord"
          @touchcancel="stopRecord"
        >
          <text class="mic-icon">{{ isRecording ? '🔴' : '🎤' }}</text>
        </view>
      </view>

      <!-- 录制提示 -->
      <view class="record-tip">
        <text class="tip-main">{{ recordTip }}</text>
        <text class="tip-sub">{{ recordSubTip }}</text>
      </view>

      <!-- 录制时长 -->
      <view v-if="isRecording || recordTime > 0" class="record-timer">
        <text class="timer-display">{{ formatTime(recordTime) }}</text>
        <view class="timer-progress">
          <view class="timer-bar" :style="{ width: (recordTime / maxRecordTime * 100) + '%' }"></text>
        </view>
        <text class="timer-limit">{{ maxRecordTime }}s</text>
      </view>
    </view>

    <!-- 转文字结果 -->
    <view v-if="transcript || isTranscribing" class="transcript-card">
      <view class="card-header">
        <text class="header-title">📝 识别结果</text>
        <view class="header-actions">
          <text v-if="transcript" class="action-btn" @click="copyText">复制</text>
          <text v-if="transcript" class="action-btn" @click="clearText">清空</text>
        </view>
      </view>
      
      <view class="transcript-content">
        <!-- 转录中状态 -->
        <view v-if="isTranscribing" class="transcribing-state">
          <view class="loading-spinner"></text>
          <text>正在识别语音...</text>
        </view>
        
        <!-- 转录结果 -->
        <textarea
          v-else
          class="transcript-input"
          v-model="transcript"
          placeholder="语音内容将显示在这里..."
          :maxlength="500"
        />
      </view>

      <!-- 快捷编辑 -->
      <view v-if="transcript && !isTranscribing" class="quick-actions">
        <text class="quick-label">快捷操作：</text>
        <view class="quick-btns">
          <text class="quick-btn" @click="addPunctuation">添加标点</text>
          <text class="quick-btn" @click="toUpperCase">转大写</text>
          <text class="quick-btn" @click="toLowerCase">转小写</text>
        </view>
      </view>
    </view>

    <!-- 发送到AI助手 -->
    <view v-if="transcript && !isTranscribing" class="send-section">
      <view class="send-card" @click="sendToAI">
        <view class="send-icon">🤖</view>
        <view class="send-info">
          <text class="send-title">发送到 AI 助手</text>
          <text class="send-desc">让AI帮你分析和处理这段内容</text>
        </view>
        <text class="send-arrow">→</text>
      </view>
      
      <view class="quick-commands">
        <text class="commands-label">快捷指令：</text>
        <scroll-view class="commands-scroll" scroll-x>
          <view class="commands-list">
            <view 
              v-for="(cmd, index) in quickCommands" 
              :key="index"
              class="command-tag"
              @click="applyCommand(cmd)"
            >
              {{ cmd.icon }} {{ cmd.name }}
            </view>
          </view>
        </scroll-view>
      </view>
    </view>

    <!-- 历史记录 -->
    <view v-if="history.length > 0" class="history-section">
      <view class="section-header">
        <text class="section-title">📚 历史记录</text>
        <text class="clear-btn" @click="clearHistory">清空</text>
      </view>
      <view class="history-list">
        <view 
          v-for="(item, index) in history" 
          :key="index"
          class="history-item"
          @click="useHistory(item)"
        >
          <view class="history-icon">🎤</view>
          <view class="history-content">
            <text class="history-text">{{ item.text }}</text>
            <text class="history-time">{{ item.time }}</text>
          </view>
          <text class="history-arrow">→</text>
        </view>
      </view>
    </view>

    <!-- 底部提示 -->
    <view class="bottom-tip">
      <text class="tip-icon">💡</text>
      <text class="tip-text">按住麦克风按钮说话，松开发送</text>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'

// 录音状态
const isRecording = ref(false)
const isPaused = ref(false)
const isTranscribing = ref(false)
const recordTime = ref(0)
const maxRecordTime = 60
let recordTimer = null
let mediaRecorder = null

// 转录文字
const transcript = ref('')

// 历史记录
const history = ref([
  { text: '明天记得买牛奶和鸡蛋', time: '10分钟前' },
  { text: '这周末计划去公园野餐', time: '1小时前' },
  { text: '提醒我给妈妈打电话', time: '昨天' }
])

// 快捷指令
const quickCommands = ref([
  { icon: '📝', name: '创建任务', action: 'task' },
  { icon: '🛒', name: '加入购物', action: 'shopping' },
  { icon: '🍳', name: '菜谱推荐', action: 'recipe' },
  { icon: '📅', name: '添加日程', action: 'schedule' },
  { icon: '⏰', name: '设置提醒', action: 'reminder' },
  { icon: '🔍', name: '搜索', action: 'search' }
])

// 计算属性
const recordTip = computed(() => {
  if (isRecording.value) return '正在聆听...'
  if (isPaused.value) return '已暂停'
  return '按住说话'
})

const recordSubTip = computed(() => {
  if (isRecording.value) return '松开结束录音'
  return '点击麦克风开始录音'
})

// 波纹动画样式
const getWaveStyle = (n) => {
  const delay = (n - 1) * 0.2
  const duration = 1.5 + n * 0.2
  return {
    animationDelay: `${delay}s`,
    animationDuration: `${duration}s`
  }
}

// 格式化时间
const formatTime = (seconds) => {
  const mins = Math.floor(seconds / 60).toString().padStart(2, '0')
  const secs = (seconds % 60).toString().padStart(2, '0')
  return `${mins}:${secs}`
}

// 开始录音
const startRecord = () => {
  if (isTranscribing.value) return
  
  isRecording.value = true
  isPaused.value = false
  recordTime.value = 0
  transcript.value = ''
  
  // 开始计时
  recordTimer = setInterval(() => {
    recordTime.value++
    if (recordTime.value >= maxRecordTime) {
      stopRecord()
    }
  }, 1000)
  
  // 震动反馈
  uni.vibrateShort()
  
  // 开始录音
  uni.startRecord({
    success: () => {
      // 录音已开始
    },
    fail: (err) => {
      console.error('录音失败:', err)
      uni.showToast({ title: '录音失败', icon: 'none' })
      stopRecord()
    }
  })
}

// 停止录音
const stopRecord = () => {
  if (!isRecording.value) return
  
  isRecording.value = false
  clearInterval(recordTimer)
  
  // 停止录音并识别
  uni.stopRecord({
    success: (res) => {
      isTranscribing.value = true
      
      // 模拟语音识别
      setTimeout(() => {
        // 模拟识别结果
        const mockTexts = [
          '帮我记录一下，明天要去超市买牛奶、鸡蛋和面包',
          '提醒我这周五下午三点有家庭聚会',
          '查询一下红烧肉的做法',
          '创建一个新任务：整理房间'
        ]
        transcript.value = mockTexts[Math.floor(Math.random() * mockTexts.length)]
        isTranscribing.value = false
        
        // 添加到历史
        history.value.unshift({
          text: transcript.value,
          time: '刚刚'
        })
        
        uni.showToast({ title: '识别成功', icon: 'success' })
      }, 1500)
    },
    fail: () => {
      isTranscribing.value = false
      uni.showToast({ title: '识别失败', icon: 'none' })
    }
  })
}

// 复制文字
const copyText = () => {
  uni.setClipboardData({
    data: transcript.value,
    success: () => {
      uni.showToast({ title: '已复制', icon: 'success' })
    }
  })
}

// 清空文字
const clearText = () => {
  uni.showModal({
    title: '确认清空',
    content: '确定要清空当前内容吗？',
    success: (res) => {
      if (res.confirm) {
        transcript.value = ''
      }
    }
  })
}

// 添加标点
const addPunctuation = () => {
  transcript.value = transcript.value.replace(/([。！？])/g, '$1\n').trim()
  uni.showToast({ title: '已添加', icon: 'success' })
}

// 转大写
const toUpperCase = () => {
  transcript.value = transcript.value.toUpperCase()
}

// 转小写
const toLowerCase = () => {
  transcript.value = transcript.value.toLowerCase()
}

// 发送到AI助手
const sendToAI = () => {
  const pages = getCurrentPages()
  const prevPage = pages[pages.length - 2]
  
  if (prevPage && prevPage.route.includes('ai')) {
    prevPage.$vm.inputMessage = transcript.value
    uni.navigateBack()
  } else {
    uni.navigateTo({
      url: `/pages/ai/index?message=${encodeURIComponent(transcript.value)}`
    })
  }
}

// 应用快捷指令
const applyCommand = (cmd) => {
  const prefixMap = {
    task: '创建任务：',
    shopping: '添加到购物清单：',
    recipe: '推荐菜谱：',
    schedule: '添加日程：',
    reminder: '设置提醒：',
    search: '搜索：'
  }
  
  const message = prefixMap[cmd.action] + transcript.value
  
  uni.navigateTo({
    url: `/pages/ai/index?message=${encodeURIComponent(message)}`
  })
}

// 使用历史记录
const useHistory = (item) => {
  transcript.value = item.text
}

// 清空历史
const clearHistory = () => {
  uni.showModal({
    title: '确认清空',
    content: '确定要清空所有历史记录吗？',
    success: (res) => {
      if (res.confirm) {
        history.value = []
      }
    }
  })
}

// 返回
const goBack = () => {
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
.voice-container {
  min-height: 100vh;
  background: linear-gradient(180deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
  padding-bottom: 40rpx;
}

// 自定义导航栏
.custom-nav {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 80rpx 30rpx 20rpx;
  
  .nav-back, .nav-right {
    width: 60rpx;
    height: 60rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    
    .icon {
      font-size: 40rpx;
      color: #fff;
    }
  }
  
  .nav-title {
    font-size: 36rpx;
    font-weight: 600;
    color: #fff;
  }
}

// 语音录制区域
.voice-record-area {
  padding: 60rpx 40rpx;
  
  &.recording {
    .wave-ring {
      opacity: 1;
    }
  }
}

.wave-container {
  position: relative;
  width: 400rpx;
  height: 400rpx;
  margin: 0 auto 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.wave-ring {
  position: absolute;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(102, 126, 234, 0.3) 0%, transparent 70%);
  opacity: 0;
  
  &.active {
    animation: wave-pulse 2s ease-out infinite;
  }
  
  &:nth-child(1) { width: 200rpx; height: 200rpx; }
  &:nth-child(2) { width: 280rpx; height: 280rpx; }
  &:nth-child(3) { width: 360rpx; height: 360rpx; }
  &:nth-child(4) { width: 440rpx; height: 440rpx; }
  &:nth-child(5) { width: 520rpx; height: 520rpx; }
}

@keyframes wave-pulse {
  0% {
    transform: scale(0.8);
    opacity: 0.8;
  }
  100% {
    transform: scale(1.2);
    opacity: 0;
  }
}

.mic-button {
  width: 180rpx;
  height: 180rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8rpx 40rpx rgba(102, 126, 234, 0.4);
  z-index: 10;
  transition: all 0.3s;
  
  &.recording {
    background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
    transform: scale(1.1);
    box-shadow: 0 8rpx 40rpx rgba(245, 87, 108, 0.4);
    animation: mic-pulse 1s ease-in-out infinite;
  }
  
  &.paused {
    background: #666;
  }
  
  &:active {
    transform: scale(0.95);
  }
  
  .mic-icon {
    font-size: 72rpx;
  }
}

@keyframes mic-pulse {
  0%, 100% {
    box-shadow: 0 8rpx 40rpx rgba(245, 87, 108, 0.4);
  }
  50% {
    box-shadow: 0 8rpx 60rpx rgba(245, 87, 108, 0.6);
  }
}

.record-tip {
  text-align: center;
  margin-bottom: 40rpx;
  
  .tip-main {
    display: block;
    font-size: 40rpx;
    font-weight: 600;
    color: #fff;
    margin-bottom: 16rpx;
  }
  
  .tip-sub {
    font-size: 28rpx;
    color: rgba(255,255,255,0.6);
  }
}

.record-timer {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 20rpx;
  padding: 0 60rpx;
  
  .timer-display {
    font-size: 36rpx;
    font-weight: 600;
    color: #fff;
    font-family: monospace;
    min-width: 100rpx;
  }
  
  .timer-progress {
    flex: 1;
    height: 8rpx;
    background: rgba(255,255,255,0.2);
    border-radius: 4rpx;
    overflow: hidden;
    
    .timer-bar {
      height: 100%;
      background: linear-gradient(90deg, #667eea, #f5576c);
      border-radius: 4rpx;
      transition: width 1s linear;
    }
  }
  
  .timer-limit {
    font-size: 24rpx;
    color: rgba(255,255,255,0.5);
  }
}

// 转录结果卡片
.transcript-card {
  margin: 0 30rpx 30rpx;
  padding: 30rpx;
  background: rgba(255,255,255,0.1);
  backdrop-filter: blur(10px);
  border-radius: 24rpx;
  border: 1rpx solid rgba(255,255,255,0.1);
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24rpx;
    
    .header-title {
      font-size: 32rpx;
      font-weight: 600;
      color: #fff;
    }
    
    .header-actions {
      display: flex;
      gap: 24rpx;
      
      .action-btn {
        font-size: 26rpx;
        color: #667eea;
      }
    }
  }
}

.transcript-content {
  min-height: 160rpx;
  
  .transcribing-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 40rpx;
    color: rgba(255,255,255,0.7);
    
    .loading-spinner {
      width: 60rpx;
      height: 60rpx;
      border: 4rpx solid rgba(255,255,255,0.1);
      border-top-color: #667eea;
      border-radius: 50%;
      animation: spin 1s linear infinite;
      margin-bottom: 20rpx;
    }
    
    text {
      font-size: 28rpx;
    }
  }
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.transcript-input {
  width: 100%;
  min-height: 160rpx;
  padding: 20rpx;
  background: rgba(255,255,255,0.05);
  border-radius: 16rpx;
  font-size: 30rpx;
  color: #fff;
  line-height: 1.6;
  box-sizing: border-box;
}

.quick-actions {
  display: flex;
  align-items: center;
  margin-top: 24rpx;
  padding-top: 24rpx;
  border-top: 1rpx solid rgba(255,255,255,0.1);
  
  .quick-label {
    font-size: 26rpx;
    color: rgba(255,255,255,0.5);
    margin-right: 16rpx;
  }
  
  .quick-btns {
    display: flex;
    gap: 16rpx;
    
    .quick-btn {
      padding: 12rpx 24rpx;
      background: rgba(102, 126, 234, 0.3);
      border-radius: 30rpx;
      font-size: 24rpx;
      color: #667eea;
    }
  }
}

// 发送区域
.send-section {
  margin: 0 30rpx 30rpx;
}

.send-card {
  display: flex;
  align-items: center;
  padding: 30rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 24rpx;
  margin-bottom: 24rpx;
  
  &:active {
    opacity: 0.9;
  }
  
  .send-icon {
    width: 80rpx;
    height: 80rpx;
    border-radius: 50%;
    background: rgba(255,255,255,0.2);
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 40rpx;
    margin-right: 24rpx;
  }
  
  .send-info {
    flex: 1;
    
    .send-title {
      display: block;
      font-size: 32rpx;
      font-weight: 600;
      color: #fff;
      margin-bottom: 8rpx;
    }
    
    .send-desc {
      font-size: 26rpx;
      color: rgba(255,255,255,0.7);
    }
  }
  
  .send-arrow {
    font-size: 40rpx;
    color: rgba(255,255,255,0.8);
  }
}

.quick-commands {
  .commands-label {
    display: block;
    font-size: 26rpx;
    color: rgba(255,255,255,0.5);
    margin-bottom: 16rpx;
  }
  
  .commands-scroll {
    white-space: nowrap;
  }
  
  .commands-list {
    display: flex;
    gap: 16rpx;
  }
  
  .command-tag {
    display: inline-flex;
    align-items: center;
    padding: 16rpx 28rpx;
    background: rgba(255,255,255,0.1);
    border-radius: 30rpx;
    font-size: 26rpx;
    color: #fff;
    border: 1rpx solid rgba(255,255,255,0.1);
    
    &:active {
      background: rgba(255,255,255,0.2);
    }
  }
}

// 历史记录
.history-section {
  margin: 0 30rpx 30rpx;
  
  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20rpx;
    
    .section-title {
      font-size: 32rpx;
      font-weight: 600;
      color: #fff;
    }
    
    .clear-btn {
      font-size: 26rpx;
      color: rgba(255,255,255,0.5);
    }
  }
}

.history-list {
  .history-item {
    display: flex;
    align-items: center;
    padding: 24rpx;
    background: rgba(255,255,255,0.05);
    border-radius: 16rpx;
    margin-bottom: 16rpx;
    
    &:active {
      background: rgba(255,255,255,0.1);
    }
    
    .history-icon {
      width: 64rpx;
      height: 64rpx;
      border-radius: 50%;
      background: rgba(102, 126, 234, 0.3);
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 32rpx;
      margin-right: 20rpx;
    }
    
    .history-content {
      flex: 1;
      min-width: 0;
      
      .history-text {
        display: block;
        font-size: 28rpx;
        color: #fff;
        margin-bottom: 8rpx;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }
      
      .history-time {
        font-size: 24rpx;
        color: rgba(255,255,255,0.4);
      }
    }
    
    .history-arrow {
      font-size: 32rpx;
      color: rgba(255,255,255,0.3);
      margin-left: 16rpx;
    }
  }
}

// 底部提示
.bottom-tip {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 30rpx;
  
  .tip-icon {
    font-size: 32rpx;
    margin-right: 12rpx;
  }
  
  .tip-text {
    font-size: 26rpx;
    color: rgba(255,255,255,0.5);
  }
}
</style>