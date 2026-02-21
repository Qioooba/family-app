<template>
  <view class="ai-container">
    <!-- 头部 -->
    <view class="ai-header">
      <view class="ai-title">
        <text class="icon">🤖</text>
        <text class="title">AI 家庭助手</text>
      </view>
      <view class="ai-subtitle">你的智能生活顾问</view>
    </view>

    <!-- 快捷功能入口 -->
    <view class="quick-actions">
      <view class="action-item" @click="quickAsk('今晚吃什么？')">
        <text class="action-icon">🍳</text>
        <text class="action-text">菜谱推荐</text>
      </view>
      <view class="action-item" @click="quickAsk('帮我分析今日饮食')">
        <text class="action-icon">🥗</text>
        <text class="action-text">营养分析</text>
      </view>
      <view class="action-item" @click="quickAsk('生成购物清单')">
        <text class="action-icon">🛒</text>
        <text class="action-text">购物助手</text>
      </view>
      <view class="action-item" @click="quickAsk('今天有什么待办？')">
        <text class="action-icon">📋</text>
        <text class="action-text">待办提醒</text>
      </view>
    </view>

    <!-- 聊天区域 -->
    <scroll-view class="chat-container" scroll-y :scroll-top="scrollTop">
      <view v-for="(msg, index) in messages" :key="index" class="message-item"
            :class="msg.role">
        <view class="avatar">
          <text>{{ msg.role === 'user' ? '👤' : '🤖' }}</text>
        </view>
        <view class="message-content">
          <view class="message-bubble">
            <text class="message-text">{{ msg.content }}</text>
          </view>
          <view class="message-time">{{ formatTime(msg.time) }}</view>
        </view>
      </view>
      
      <!-- 加载状态 -->
      <view v-if="loading" class="message-item assistant">
        <view class="avatar">
          <text>🤖</text>
        </view>
        <view class="message-content">
          <view class="message-bubble loading">
            <view class="loading-dots">
              <text>思考中</text>
              <view class="dots">
                <view class="dot"></view>
                <view class="dot"></view>
                <view class="dot"></view>
              </view>
            </view>
          </view>
        </view>
      </view>
    </scroll-view>

    <!-- 输入区域 -->
    <view class="input-area">
      <view class="input-wrapper">
        <input 
          class="chat-input"
          v-model="inputMessage"
          type="text"
          placeholder="输入问题，如：今晚吃什么？"
          confirm-type="send"
          @confirm="sendMessage"
        />
        <view class="voice-btn" @click="startVoiceInput">
          <text class="icon">🎤</text>
        </view>
      </view>
      
      <view class="send-btn" :class="{ active: inputMessage.trim() }" @click="sendMessage">
        <text class="icon">➤</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, nextTick } from 'vue'
import { aiApi } from '@/api/ai'

const messages = ref([
  {
    role: 'assistant',
    content: '你好！我是你的AI家庭助手 🤖\n\n我可以帮你：\n• 🍳 推荐菜谱\n• 🥗 分析营养\n• 🛒 生成购物清单\n• 📋 提醒待办事项\n• 💡 回答家庭相关问题\n\n有什么可以帮你的吗？',
    time: Date.now()
  }
])

const inputMessage = ref('')
const loading = ref(false)
const scrollTop = ref(0)
const sessionId = ref('')

// 快捷提问
const quickAsk = (question) => {
  inputMessage.value = question
  sendMessage()
}

// 发送消息
const sendMessage = async () => {
  const message = inputMessage.value.trim()
  if (!message || loading.value) return
  
  // 添加用户消息
  messages.value.push({
    role: 'user',
    content: message,
    time: Date.now()
  })
  
  inputMessage.value = ''
  loading.value = true
  scrollToBottom()
  
  try {
    const res = await aiApi.chat({
      message,
      sessionId: sessionId.value
    })
    
    if (res.code === 200) {
      sessionId.value = res.data.sessionId
      messages.value.push({
        role: 'assistant',
        content: res.data.reply,
        time: Date.now()
      })
    }
  } catch (error) {
    messages.value.push({
      role: 'assistant',
      content: '抱歉，服务暂时不可用，请稍后再试。',
      time: Date.now()
    })
  } finally {
    loading.value = false
    scrollToBottom()
  }
}

// 语音输入
const startVoiceInput = () => {
  uni.showToast({
    title: '语音功能开发中',
    icon: 'none'
  })
}

// 滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    scrollTop.value = messages.value.length * 1000
  })
}

// 格式化时间
const formatTime = (timestamp) => {
  const date = new Date(timestamp)
  return `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
}
</script>

<style lang="scss" scoped>
.ai-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: linear-gradient(180deg, #f0f7ff 0%, #ffffff 100%);
}

.ai-header {
  padding: 20rpx 30rpx;
  background: #fff;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
  
  .ai-title {
    display: flex;
    align-items: center;
    
    .icon {
      font-size: 48rpx;
      margin-right: 16rpx;
    }
    
    .title {
      font-size: 36rpx;
      font-weight: 600;
      color: #333;
    }
  }
  
  .ai-subtitle {
    font-size: 26rpx;
    color: #999;
    margin-top: 8rpx;
    margin-left: 64rpx;
  }
}

.quick-actions {
  display: flex;
  justify-content: space-around;
  padding: 20rpx;
  background: #fff;
  margin: 20rpx;
  border-radius: 20rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.05);
  
  .action-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 20rpx;
    
    .action-icon {
      font-size: 48rpx;
      margin-bottom: 12rpx;
    }
    
    .action-text {
      font-size: 24rpx;
      color: #666;
    }
  }
}

.chat-container {
  flex: 1;
  padding: 20rpx;
  overflow-y: auto;
}

.message-item {
  display: flex;
  margin-bottom: 30rpx;
  
  &.user {
    flex-direction: row-reverse;
    
    .message-content {
      align-items: flex-end;
    }
    
    .message-bubble {
      background: #2979ff;
      color: #fff;
      border-bottom-right-radius: 8rpx;
    }
  }
  
  &.assistant {
    .message-bubble {
      background: #fff;
      color: #333;
      border-bottom-left-radius: 8rpx;
    }
  }
  
  .avatar {
    width: 80rpx;
    height: 80rpx;
    border-radius: 50%;
    background: #f5f5f5;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 40rpx;
  }
  
  .message-content {
    flex: 1;
    display: flex;
    flex-direction: column;
    margin: 0 20rpx;
    max-width: 70%;
  }
  
  .message-bubble {
    padding: 24rpx;
    border-radius: 24rpx;
    box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.08);
    
    .message-text {
      font-size: 30rpx;
      line-height: 1.6;
      white-space: pre-wrap;
    }
    
    &.loading {
      padding: 30rpx 40rpx;
    }
  }
  
  .message-time {
    font-size: 22rpx;
    color: #999;
    margin-top: 12rpx;
  }
}

.loading-dots {
  display: flex;
  align-items: center;
  
  text {
    font-size: 26rpx;
    color: #999;
    margin-right: 16rpx;
  }
  
  .dots {
    display: flex;
    gap: 8rpx;
    
    .dot {
      width: 12rpx;
      height: 12rpx;
      border-radius: 50%;
      background: #ccc;
      animation: dot-pulse 1.4s ease-in-out infinite;
      
      &:nth-child(2) {
        animation-delay: 0.2s;
      }
      
      &:nth-child(3) {
        animation-delay: 0.4s;
      }
    }
  }
}

@keyframes dot-pulse {
  0%, 80%, 100% {
    transform: scale(0.8);
    opacity: 0.5;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}

.input-area {
  display: flex;
  align-items: center;
  padding: 20rpx;
  background: #fff;
  border-top: 1rpx solid #f0f0f0;
  
  .input-wrapper {
    flex: 1;
    display: flex;
    align-items: center;
    background: #f5f7fa;
    border-radius: 40rpx;
    padding: 0 20rpx;
    margin-right: 20rpx;
    
    .chat-input {
      flex: 1;
      height: 80rpx;
      font-size: 30rpx;
      padding: 0 20rpx;
    }
    
    .voice-btn {
      width: 60rpx;
      height: 60rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      
      .icon {
        font-size: 40rpx;
      }
    }
  }
  
  .send-btn {
    width: 80rpx;
    height: 80rpx;
    border-radius: 50%;
    background: #e0e0e0;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.3s;
    
    .icon {
      font-size: 36rpx;
      color: #fff;
    }
    
    &.active {
      background: #2979ff;
    }
  }
}
</style>
