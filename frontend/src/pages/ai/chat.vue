<template>
  <view class="chat-page">
    <!-- 顶部导航 -->
    <view class="nav-bar">
      <view class="back-btn" @click="goBack">
        <u-icon name="arrow-left" size="40" color="#333"></u-icon>
      </view>
      
      <view class="ai-info">
        <text class="ai-name">AI助手</text>
        <view class="ai-status">
          <view class="status-dot"></view>
          <text>在线</text>
        </view>
      </view>
      
      <view class="right-placeholder"></view>
    </view>
    
    <!-- 消息列表 -->
    <scroll-view 
      class="message-list" 
      scroll-y 
      :scroll-top="scrollTop"
      :scroll-with-animation="true"
    >
      <!-- 欢迎消息 -->
      <view v-if="messages.length === 0" class="welcome-area">
        <view class="welcome-card">
          <text class="welcome-title">👋 您好！我是您的家庭AI助手</text>
          <text class="welcome-desc">我可以帮您：</text>
          
          <view class="suggestion-list">
            <view 
              v-for="suggestion in suggestions" 
              :key="suggestion"
              class="suggestion-item"
              @click="sendMessage(suggestion)"
            >
              {{ suggestion }}
            </view>
          </view>
        </view>
      </view>
      
      <!-- 消息气泡 -->
      <view 
        v-for="(msg, index) in messages" 
        :key="index"
        class="message-item"
        :class="{ 'user': msg.role === 'user', 'ai': msg.role === 'assistant' }"
      >
        <!-- AI头像 -->
        <view v-if="msg.role === 'assistant'" class="avatar ai-avatar">
          <text>🤖</text>
        </view>
        
        <view class="message-content">
          <!-- 文本消息 -->
          <text class="message-text" v-if="msg.type === 'text'">{{ msg.content }}</text>
          
          <!-- 菜谱卡片 -->
          <view v-if="msg.type === 'recipe'" class="recipe-card">
            <image :src="msg.data.image" mode="aspectFill" class="recipe-img" />
            <view class="recipe-info">
              <text class="recipe-name">{{ msg.data.name }}</text>
              <text class="recipe-desc">{{ msg.data.desc }}</text>
              <view class="recipe-meta">
                <text>⏱️ {{ msg.data.time }}分钟</text>
                <text>🔥 {{ msg.data.calories }}卡</text>
              </view>
            </view>
          </view>
          
          <!-- 按钮组 -->
          <view v-if="msg.actions" class="message-actions">
            <view 
              v-for="action in msg.actions" 
              :key="action.text"
              class="action-btn"
              @click="handleAction(action)"
            >
              {{ action.text }}
            </view>
          </view>
          
          <!-- 加载中 -->
          <view v-if="msg.loading" class="loading-dots">
            <view class="dot"></view>
            <view class="dot"></view>
            <view class="dot"></view>
          </view>
        </view>
        
        <!-- 用户头像 -->
        <view v-if="msg.role === 'user'" class="avatar user-avatar">
          <image :src="userAvatar" mode="aspectFill" />
        </view>
      </view>
    </scroll-view>
    
    <!-- 底部输入区 -->
    <view class="input-area">
      <view class="toolbar">
        <view class="tool-btn" @click="showQuickActions">
          <u-icon name="plus-circle" size="44" color="#999"></u-icon>
        </view>
        
        <input 
          v-model="inputMessage" 
          class="message-input"
          placeholder="输入消息..."
          confirm-type="send"
          @confirm="sendMessage()"
        />
        
        <view class="tool-btn" @click="startVoice">
          <u-icon name="mic" size="44" color="#999"></u-icon>
        </view>
        
        <view 
          class="send-btn" 
          :class="{ active: inputMessage.trim() }"
          @click="sendMessage()"
        >
          <u-icon name="arrow-upward" size="36" color="#fff"></u-icon>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'

const inputMessage = ref('')
const messages = ref([])
const scrollTop = ref(0)
const userAvatar = ref('/static/avatar/user.png')

const suggestions = [
  '今晚吃什么？',
  '根据冰箱食材推荐菜谱',
  '分析我今天的营养摄入',
  '生成周末购物清单',
  '推荐适合孩子的菜谱'
]

onMounted(() => {
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const scene = currentPage.options.scene
  
  if (scene === 'recipe') {
    sendMessage('根据冰箱食材推荐菜谱')
  } else if (scene === 'nutrition') {
    sendMessage('分析我今天的营养摄入')
  } else if (scene === 'shopping') {
    sendMessage('生成购物清单')
  }
})

const goBack = () => {
  uni.navigateBack()
}

const scrollToBottom = () => {
  nextTick(() => {
    scrollTop.value = messages.value.length * 1000
  })
}

const sendMessage = (content = inputMessage.value) => {
  if (!content.trim()) return
  
  // 添加用户消息
  messages.value.push({
    role: 'user',
    type: 'text',
    content: content
  })
  
  inputMessage.value = ''
  scrollToBottom()
  
  // 模拟AI回复
  simulateAIResponse(content)
}

const simulateAIResponse = (userMessage) => {
  // 添加加载消息
  messages.value.push({
    role: 'assistant',
    type: 'text',
    content: '',
    loading: true
  })
  
  scrollToBottom()
  
  // 模拟延迟
  setTimeout(() => {
    // 移除加载消息
    messages.value.pop()
    
    // 根据用户消息生成回复
    let response = generateResponse(userMessage)
    messages.value.push(response)
    scrollToBottom()
  }, 1500)
}

const generateResponse = (userMessage) => {
  if (userMessage.includes('今晚') || userMessage.includes('吃')) {
    return {
      role: 'assistant',
      type: 'text',
      content: '根据您冰箱里的食材（土豆、牛肉、番茄），我为您推荐这道「番茄土豆炖牛肉」：',
      actions: [
        { text: '查看详细做法', action: 'view_recipe' },
        { text: '加入购物清单', action: 'add_shopping' }
      ]
    }
  } else if (userMessage.includes('营养')) {
    return {
      role: 'assistant',
      type: 'text',
      content: '今日营养摄入分析：\n\n✅ 蛋白质：65g（达标）\n⚠️ 碳水：280g（略超）\n✅ 脂肪：55g（正常）\n❌ 纤维：12g（不足）\n\n建议：晚餐多摄入蔬菜，减少主食。'
    }
  } else if (userMessage.includes('购物')) {
    return {
      role: 'assistant',
      type: 'text',
      content: '已根据本周菜谱生成购物清单：\n\n🥬 蔬菜：西兰花、胡萝卜、青椒\n🥩 肉类：鸡胸肉 500g\n🥚 其他：鸡蛋 12个\n\n预计花费：¥85'
    }
  } else {
    return {
      role: 'assistant',
      type: 'text',
      content: '收到！我可以帮您：\n1. 推荐菜谱\n2. 分析营养摄入\n3. 生成购物清单\n4. 提醒待办事项\n\n请告诉我您需要什么帮助~'
    }
  }
}

const handleAction = (action) => {
  if (action.action === 'view_recipe') {
    uni.navigateTo({ url: '/pages/recipe/detail?id=1' })
  } else if (action.action === 'add_shopping') {
    uni.showToast({ title: '已加入购物清单', icon: 'success' })
  }
}

const showQuickActions = () => {
  uni.showActionSheet({
    itemList: ['📷 拍照识菜', '📎 发送图片', '📍 发送位置'],
    success: (res) => {
      // 处理快捷操作
    }
  })
}

const startVoice = () => {
  uni.navigateTo({ url: '/pages/ai/voice' })
}
</script>

<style lang="scss" scoped>
.chat-page {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f6fa;
}

.nav-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 30rpx;
  padding-top: 60rpx;
  background: #fff;
  border-bottom: 2rpx solid #f0f0f0;
  
  .back-btn {
    width: 60rpx;
    height: 60rpx;
    display: flex;
    align-items: center;
    justify-content: center;
  }
  
  .ai-info {
    display: flex;
    flex-direction: column;
    align-items: center;
    
    .ai-name {
      font-size: 32rpx;
      font-weight: 600;
      color: #333;
    }
    
    .ai-status {
      display: flex;
      align-items: center;
      gap: 8rpx;
      margin-top: 4rpx;
      
      .status-dot {
        width: 12rpx;
        height: 12rpx;
        background: #52C41A;
        border-radius: 50%;
      }
      
      text {
        font-size: 22rpx;
        color: #999;
      }
    }
  }
  
  .right-placeholder {
    width: 60rpx;
  }
}

.message-list {
  flex: 1;
  padding: 30rpx;
  overflow-y: auto;
}

.welcome-area {
  .welcome-card {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 24rpx;
    padding: 40rpx;
    margin-bottom: 30rpx;
    
    .welcome-title {
      font-size: 32rpx;
      font-weight: 600;
      color: #fff;
      display: block;
      margin-bottom: 20rpx;
    }
    
    .welcome-desc {
      font-size: 28rpx;
      color: rgba(255,255,255,0.8);
      display: block;
      margin-bottom: 30rpx;
    }
    
    .suggestion-list {
      display: flex;
      flex-wrap: wrap;
      gap: 16rpx;
      
      .suggestion-item {
        padding: 16rpx 28rpx;
        background: rgba(255,255,255,0.2);
        border-radius: 30rpx;
        font-size: 26rpx;
        color: #fff;
        
        &:active {
          background: rgba(255,255,255,0.3);
        }
      }
    }
  }
}

.message-item {
  display: flex;
  gap: 20rpx;
  margin-bottom: 30rpx;
  
  &.user {
    flex-direction: row-reverse;
    
    .message-content {
      align-items: flex-end;
      
      .message-text {
        background: #5B8FF9;
        color: #fff;
      }
    }
  }
  
  .avatar {
    width: 72rpx;
    height: 72rpx;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
    
    &.ai-avatar {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      font-size: 36rpx;
    }
    
    &.user-avatar {
      overflow: hidden;
      
      image {
        width: 100%;
        height: 100%;
      }
    }
  }
  
  .message-content {
    display: flex;
    flex-direction: column;
    max-width: 70%;
    
    .message-text {
      padding: 24rpx;
      background: #fff;
      border-radius: 20rpx;
      font-size: 28rpx;
      color: #333;
      line-height: 1.6;
      white-space: pre-wrap;
    }
    
    .recipe-card {
      background: #fff;
      border-radius: 20rpx;
      overflow: hidden;
      margin-top: 16rpx;
      
      .recipe-img {
        width: 100%;
        height: 200rpx;
      }
      
      .recipe-info {
        padding: 20rpx;
        
        .recipe-name {
          font-size: 30rpx;
          font-weight: 600;
          color: #333;
          display: block;
          margin-bottom: 8rpx;
        }
        
        .recipe-desc {
          font-size: 24rpx;
          color: #999;
          display: block;
          margin-bottom: 12rpx;
        }
        
        .recipe-meta {
          display: flex;
          gap: 20rpx;
          
          text {
            font-size: 24rpx;
            color: #666;
          }
        }
      }
    }
    
    .message-actions {
      display: flex;
      gap: 16rpx;
      margin-top: 16rpx;
      
      .action-btn {
        padding: 16rpx 28rpx;
        background: #E6F7FF;
        border-radius: 30rpx;
        font-size: 26rpx;
        color: #1890FF;
        
        &:active {
          background: #BAE7FF;
        }
      }
    }
    
    .loading-dots {
      display: flex;
      gap: 12rpx;
      padding: 24rpx;
      
      .dot {
        width: 16rpx;
        height: 16rpx;
        background: #999;
        border-radius: 50%;
        animation: bounce 1.4s infinite ease-in-out both;
        
        &:nth-child(1) { animation-delay: -0.32s; }
        &:nth-child(2) { animation-delay: -0.16s; }
      }
    }
  }
}

@keyframes bounce {
  0%, 80%, 100% { transform: scale(0); }
  40% { transform: scale(1); }
}

.input-area {
  background: #fff;
  padding: 20rpx 30rpx;
  padding-bottom: 40rpx;
  border-top: 2rpx solid #f0f0f0;
  
  .toolbar {
    display: flex;
    align-items: center;
    gap: 16rpx;
    
    .tool-btn {
      width: 72rpx;
      height: 72rpx;
      display: flex;
      align-items: center;
      justify-content: center;
    }
    
    .message-input {
      flex: 1;
      height: 80rpx;
      background: #f5f5f5;
      border-radius: 40rpx;
      padding: 0 30rpx;
      font-size: 28rpx;
    }
    
    .send-btn {
      width: 72rpx;
      height: 72rpx;
      background: #ddd;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      transition: all 0.3s;
      
      &.active {
        background: #5B8FF9;
      }
    }
  }
}
</style>