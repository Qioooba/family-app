<template>
  <view class="voice-page">
    <view class="nav-bar">
      <view class="back-btn" @click="goBack">
        <u-icon name="arrow-left" size="40" color="#333"></u-icon>
      </view>
      <text class="title">语音输入</text>
      <view class="right-placeholder"></view>
    </view>
    
    <view class="voice-container">
      <!-- 声波动画 -->
      <view class="wave-container">
        <view 
          v-for="n in 20" 
          :key="n"
          class="wave-bar"
          :style="{ 
            height: isRecording ? (Math.random() * 60 + 20) + '%' : '20%',
            animationDelay: (n * 0.1) + 's'
          }"
        ></view>
      </view>
      
      <!-- 录音时间 -->
      <view class="record-time" v-if="isRecording">
        <text class="time">{{ formatTime(recordTime) }}</text>
        <text class="hint">正在聆听...</text>
      </view>
      
      <view class="record-hint" v-else-if="!recognizedText">
        <text class="main-hint">按住说话</text>
        <text class="sub-hint">例如："记录早餐吃了两个包子"</text>
      </view>
      
      <!-- 识别结果 -->
      <view class="result-area" v-if="recognizedText">
        <view class="result-card">
          <text class="result-label">识别结果</text>
          <text class="result-text">{{ recognizedText }}</text>
          
          <view v-if="parsedIntent" class="parsed-info">
            <view class="intent-tag">意图: {{ parsedIntent.intent }}</view>
            <view class="entities" v-if="parsedIntent.entities">
              <view v-for="(value, key) in parsedIntent.entities" :key="key" class="entity-tag">
                {{ key }}: {{ value }}
              </view>
            </view>
          </view>
        </view>        
      </view>
      
      <!-- 录音按钮 -->
      <view class="control-area">
        <view 
          v-if="!recognizedText"
          class="record-btn"
          :class="{ recording: isRecording }"
          @touchstart="startRecording"
          @touchend="stopRecording"
        >
          <u-icon :name="isRecording ? 'mic-fill' : 'mic'" size="60" color="#fff"></u-icon>
        </view>
        
        <view v-else class="action-buttons">
          <view class="action-btn cancel" @click="reset">
            <u-icon name="close" size="40" color="#999"></u-icon>
            <text>重录</text>
          </view>
          
          <view class="action-btn confirm" @click="confirm">
            <u-icon name="checkmark" size="40" color="#fff"></u-icon>
            <text>确认</text>
          </view>
        </view>
      </view>
      
      <!-- 快捷指令 -->
      <view class="quick-commands">
        <text class="commands-title">试试这样说</text>
        
        <view class="commands-list">
          <view 
            v-for="cmd in quickCommands" 
            :key="cmd"
            class="command-item"
            @click="quickRecord(cmd)"
          >
            {{ cmd }}
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'

const isRecording = ref(false)
const recordTime = ref(0)
const recognizedText = ref('')
const parsedIntent = ref(null)
let recordTimer = null

const quickCommands = [
  '记录早餐吃了两个包子',
  '添加任务买牛奶',
  '我想吃红烧肉',
  '提醒我下午3点开会',
  '今天天气怎么样'
]

const formatTime = (seconds) => {
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
}

const goBack = () => {
  uni.navigateBack()
}

const startRecording = () => {
  isRecording.value = true
  recordTime.value = 0
  
  recordTimer = setInterval(() => {
    recordTime.value++
    if (recordTime.value >= 60) {
      stopRecording()
    }
  }, 1000)
}

const stopRecording = () => {
  if (!isRecording.value) return
  
  isRecording.value = false
  clearInterval(recordTimer)
  
  // 模拟语音识别结果
  setTimeout(() => {
    recognizedText.value = '记录早餐吃了两个包子，一杯牛奶'
    parsedIntent.value = {
      intent: 'add_diet_record',
      entities: {
        meal: '早餐',
        food: '包子、牛奶',
        quantity: '2个包子，1杯牛奶'
      }
    }
  }, 800)
}

const quickRecord = (text) => {
  startRecording()
  
  setTimeout(() => {
    stopRecording()
    recognizedText.value = text
    
    // 根据文本解析意图
    if (text.includes('早餐') || text.includes('午餐') || text.includes('晚餐')) {
      parsedIntent.value = {
        intent: 'add_diet_record',
        entities: { meal: 'breakfast' }
      }
    } else if (text.includes('任务')) {
      parsedIntent.value = {
        intent: 'add_task',
        entities: { title: text.replace('添加任务', '') }
      }
    }
  }, 1500)
}

const reset = () => {
  recognizedText.value = ''
  parsedIntent.value = null
  recordTime.value = 0
}

const confirm = () => {
  uni.showToast({ title: '已记录', icon: 'success' })
  setTimeout(() => {
    uni.navigateBack()
  }, 1500)
}
</script>

<style lang="scss" scoped>
.voice-page {
  height: 100vh;
  background: #f5f6fa;
}

.nav-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 30rpx;
  padding-top: 60rpx;
  background: #fff;
  
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
  
  .right-placeholder {
    width: 60rpx;
  }
}

.voice-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 60rpx 30rpx;
}

.wave-container {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  height: 200rpx;
  margin-bottom: 40rpx;
  
  .wave-bar {
    width: 8rpx;
    background: linear-gradient(180deg, #5B8FF9 0%, #5AD8A6 100%);
    border-radius: 4rpx;
    transition: height 0.1s ease;
    
    &:nth-child(5n) {
      background: linear-gradient(180deg, #F6BD16 0%, #E8684A 100%);
    }
  }
}

.record-time {
  text-align: center;
  margin-bottom: 40rpx;
  
  .time {
    font-size: 64rpx;
    font-weight: 300;
    color: #333;
    display: block;
  }
  
  .hint {
    font-size: 28rpx;
    color: #5B8FF9;
    margin-top: 16rpx;
    display: block;
  }
}

.record-hint {
  text-align: center;
  margin-bottom: 60rpx;
  
  .main-hint {
    font-size: 36rpx;
    font-weight: 600;
    color: #333;
    display: block;
    margin-bottom: 16rpx;
  }
  
  .sub-hint {
    font-size: 26rpx;
    color: #999;
  }
}

.result-area {
  width: 100%;
  margin-bottom: 40rpx;
  
  .result-card {
    background: #fff;
    border-radius: 20rpx;
    padding: 40rpx;
    
    .result-label {
      font-size: 26rpx;
      color: #999;
      display: block;
      margin-bottom: 20rpx;
    }
    
    .result-text {
      font-size: 34rpx;
      color: #333;
      line-height: 1.6;
      display: block;
      margin-bottom: 30rpx;
    }
    
    .parsed-info {
      padding-top: 30rpx;
      border-top: 2rpx solid #f0f0f0;
      
      .intent-tag {
        display: inline-block;
        padding: 8rpx 20rpx;
        background: #E6F7FF;
        border-radius: 20rpx;
        font-size: 24rpx;
        color: #1890FF;
        margin-bottom: 16rpx;
      }
      
      .entities {
        display: flex;
        flex-wrap: wrap;
        gap: 12rpx;
        
        .entity-tag {
          padding: 8rpx 16rpx;
          background: #F6FFED;
          border-radius: 16rpx;
          font-size: 22rpx;
          color: #52C41A;
        }
      }
    }
  }
}

.control-area {
  margin-bottom: 60rpx;
  
  .record-btn {
    width: 160rpx;
    height: 160rpx;
    background: linear-gradient(135deg, #5B8FF9 0%, #5AD8A6 100%);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 8rpx 30rpx rgba(91, 143, 249, 0.4);
    transition: all 0.3s;
    
    &.recording {
      transform: scale(1.1);
      box-shadow: 0 0 0 20rpx rgba(91, 143, 249, 0.2);
      animation: pulse 1.5s infinite;
    }
  }
  
  .action-buttons {
    display: flex;
    gap: 40rpx;
    
    .action-btn {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 12rpx;
      
      &.cancel {
        width: 120rpx;
        height: 120rpx;
        background: #f5f5f5;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
      }
      
      &.confirm {
        width: 140rpx;
        height: 140rpx;
        background: linear-gradient(135deg, #5B8FF9 0%, #5AD8A6 100%);
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
      }
      
      text {
        font-size: 24rpx;
        color: #666;
      }
    }
  }
}

@keyframes pulse {
  0%, 100% { box-shadow: 0 0 0 0 rgba(91, 143, 249, 0.4); }
  50% { box-shadow: 0 0 0 30rpx rgba(91, 143, 249, 0); }
}

.quick-commands {
  width: 100%;
  
  .commands-title {
    font-size: 28rpx;
    color: #999;
    display: block;
    margin-bottom: 20rpx;
    text-align: center;
  }
  
  .commands-list {
    display: flex;
    flex-direction: column;
    gap: 16rpx;
    
    .command-item {
      padding: 24rpx 30rpx;
      background: #fff;
      border-radius: 16rpx;
      font-size: 28rpx;
      color: #333;
      text-align: center;
      
      &:active {
        background: #f0f0f0;
      }
    }
  }
}
</style>