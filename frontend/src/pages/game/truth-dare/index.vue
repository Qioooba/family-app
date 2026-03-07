<template>
  <view class="game-page">
    <!-- 头部 -->
    <view class="header">
      <view class="header-left" @click="goBack">
        <u-icon name="arrow-left" color="#fff" size="40"></u-icon>
      </view>
      <view class="header-title">真心话大冒险</view>
      <view class="header-right" @click="showRules">
        <text>规则</text>
      </view>
    </view>

    <!-- 模式选择 -->
    <view class="mode-section" v-if="!gameStarted">
      <view class="section-title">选择模式</view>
      <view class="mode-list">
        <view 
          v-for="mode in modes" 
          :key="mode.value"
          class="mode-card"
          :class="{ active: selectedMode === mode.value }"
          @click="selectedMode = mode.value"
        >
          <text class="mode-icon">{{ mode.icon }}</text>
          <text class="mode-name">{{ mode.label }}</text>
        </view>
      </view>
    </view>

    <!-- 游戏主体 -->
    <view class="game-body" v-else>
      <!-- 转盘区域 -->
      <view class="wheel-section">
        <view class="wheel-container" :style="{ transform: `rotate(${rotation}deg)` }">
          <view class="wheel">
            <view 
              v-for="(item, index) in wheelItems" 
              :key="index"
              class="wheel-item"
              :style="{ transform: `rotate(${index * 45}deg)` }"
            >
              <text>{{ item }}</text>
            </view>
          </view>
        </view>
        <view class="wheel-pointer">▼</view>
      </view>

      <!-- 开始按钮 -->
      <view class="spin-btn" @click="spinWheel" v-if="!isSpinning && !showResult">
        <text>开始转盘</text>
      </view>

      <!-- 结果展示 -->
      <view class="result-card" v-if="showResult">
        <view class="result-type" :class="resultType">
          <text>{{ resultType === 'truth' ? '💭 真心话' : '🎯 大冒险' }}</text>
        </view>
        <view class="result-content">
          <text>{{ currentQuestion }}</text>
        </view>
        <view class="result-actions">
          <view class="btn btn-complete" @click="completeChallenge">
            <text>✅ 完成挑战</text>
          </view>
          <view class="btn btn-skip" @click="skipChallenge">
            <text>🔄 换一个</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 开始游戏按钮 -->
    <view class="start-section" v-if="!gameStarted">
      <view class="btn-start" @click="startGame">
        <text>开始游戏</text>
      </view>
    </view>

    <!-- 玩家列表（多人模式） -->
    <view class="players-section" v-if="selectedMode === 'multi' && gameStarted">
      <view class="section-title">选择玩家</view>
      <scroll-view class="players-list" scroll-x>
        <view 
          v-for="(player, index) in players" 
          :key="index"
          class="player-item"
          :class="{ active: currentPlayer === index }"
          @click="currentPlayer = index"
        >
          <view class="player-avatar">
            <text>{{ player.avatar }}</text>
          </view>
          <text class="player-name">{{ player.name }}</text>
        </view>
      </scroll-view>
    </view>

    <!-- 历史记录 -->
    <view class="history-section" v-if="gameHistory.length > 0">
      <view class="section-title">游戏记录</view>
      <view class="history-list">
        <view 
          v-for="(record, index) in gameHistory.slice(-5)" 
          :key="index"
          class="history-item"
        >
          <text class="history-type" :class="record.type">
            {{ record.type === 'truth' ? '真' : '冒' }}
          </text>
          <text class="history-content">{{ record.content }}</text>
          <text class="history-player" v-if="record.player">{{ record.player }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { gameApi } from '../../../api/game.js'

// 游戏模式
const modes = [
  { label: '经典模式', value: 'classic', icon: '🎲' },
  { label: '情侣模式', value: 'couple', icon: '💕' },
  { label: '家庭模式', value: 'family', icon: '👨‍👩‍👧‍👦' },
  { label: '多人模式', value: 'multi', icon: '👥' }
]

const selectedMode = ref('classic')
const gameStarted = ref(false)
const isSpinning = ref(false)
const rotation = ref(0)
const showResult = ref(false)
const resultType = ref('')
const currentQuestion = ref('')
const currentPlayer = ref(0)
const gameHistory = ref([])

// 转盘项目
const wheelItems = ['真心话', '大冒险', '真心话', '大冒险', '真心话', '大冒险', '真心话', '大冒险']

// 题目库
const questions = {
  truth: {
    classic: [
      '你做过最尴尬的事情是什么？',
      '你最害怕失去什么？',
      '你最后悔的事情是什么？',
      '你最想对谁说声对不起？',
      '你最大的秘密是什么？'
    ],
    couple: [
      '第一次约会是什么感觉？',
      '对方最吸引你的地方是什么？',
      '你们最甜蜜的回忆是什么？',
      '你最想一起去的地方是哪里？',
      '对方最让你感动的瞬间是什么？'
    ],
    family: [
      '家里谁做饭最好吃？',
      '你最喜欢家庭的哪个传统？',
      '你最感谢家人的一件事是什么？',
      '小时候最难忘的家庭活动是什么？',
      '你最想和家人一起完成的事情是什么？'
    ]
  },
  dare: {
    classic: [
      '模仿一种动物的叫声',
      '用方言说一段话',
      '做10个俯卧撑',
      '给手机通讯录第3个人打电话',
      '唱一首歌'
    ],
    couple: [
      '对视30秒不许笑',
      '给对方捶背1分钟',
      '说出对方的5个优点',
      '模仿对方生气的样子',
      '给对方一个拥抱'
    ],
    family: [
      '给全家人讲一个笑话',
      '表演一个才艺',
      '说出每个家人的一个优点',
      '和左边的人交换座位',
      '给每个人说一句祝福'
    ]
  }
}

// 玩家列表（多人模式）
const players = ref([
  { name: '玩家1', avatar: '👤' },
  { name: '玩家2', avatar: '👤' },
  { name: '玩家3', avatar: '👤' },
  { name: '玩家4', avatar: '👤' }
])

// 返回
const goBack = () => {
  uni.navigateBack()
}

// 显示规则
const showRules = () => {
  uni.showModal({
    title: '游戏规则',
    content: '转动转盘，选择真心话或大冒险。\n真心话：诚实回答问题\n大冒险：完成指定挑战',
    showCancel: false
  })
}

// 开始游戏
const startGame = () => {
  gameStarted.value = true
}

// 转动转盘
const spinWheel = () => {
  if (isSpinning.value) return
  
  isSpinning.value = true
  showResult.value = false
  
  // 随机旋转角度（至少转3圈）
  const randomDegree = 1080 + Math.floor(Math.random() * 720)
  rotation.value += randomDegree
  
  // 计算结果
  const finalRotation = rotation.value % 360
  const segment = Math.floor(finalRotation / 45)
  const isTruth = segment % 2 === 0
  
  setTimeout(() => {
    isSpinning.value = false
    showResult.value = true
    resultType.value = isTruth ? 'truth' : 'dare'
    
    // 随机选择题目
    const modeQuestions = questions[resultType.value][selectedMode.value] || questions[resultType.value].classic
    currentQuestion.value = modeQuestions[Math.floor(Math.random() * modeQuestions.length)]
  }, 3000)
}

// 完成挑战
const completeChallenge = async () => {
  // 添加到历史记录
  gameHistory.value.push({
    type: resultType.value,
    content: currentQuestion.value,
    player: selectedMode.value === 'multi' ? players.value[currentPlayer.value].name : null,
    time: new Date()
  })
  
  // 添加积分
  try {
    await gameApi.addPoints({
      gameType: 'truth-dare',
      points: 5,
      description: '完成真心话大冒险挑战'
    })
  } catch (e) {
    console.error('添加积分失败', e)
  }
  
  uni.showToast({ title: '挑战完成！+5分', icon: 'none' })
  
  // 重置
  showResult.value = false
  
  // 多人模式下切换玩家
  if (selectedMode.value === 'multi') {
    currentPlayer.value = (currentPlayer.value + 1) % players.value.length
  }
}

// 跳过挑战
const skipChallenge = () => {
  const modeQuestions = questions[resultType.value][selectedMode.value] || questions[resultType.value].classic
  currentQuestion.value = modeQuestions[Math.floor(Math.random() * modeQuestions.length)]
}
</script>

<style lang="scss" scoped>
.game-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #9b59b6 0%, #f8f9fc 30%);
  padding-bottom: 40rpx;
}

// 头部
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 100rpx 32rpx 40rpx;
  
  .header-title {
    font-size: 40rpx;
    font-weight: 700;
    color: #fff;
  }
  
  .header-right {
    text {
      font-size: 28rpx;
      color: rgba(255,255,255,0.9);
    }
  }
}

// 模式选择
.mode-section {
  margin: 0 32rpx 32rpx;
  
  .section-title {
    font-size: 32rpx;
    font-weight: 600;
    color: #fff;
    margin-bottom: 24rpx;
  }
  
  .mode-list {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 20rpx;
    
    .mode-card {
      background: rgba(255,255,255,0.2);
      backdrop-filter: blur(10rpx);
      border-radius: 24rpx;
      padding: 40rpx 24rpx;
      text-align: center;
      border: 2rpx solid transparent;
      transition: all 0.3s ease;
      
      &.active {
        background: #fff;
        border-color: #9b59b6;
      }
      
      .mode-icon {
        font-size: 60rpx;
        display: block;
        margin-bottom: 16rpx;
      }
      
      .mode-name {
        font-size: 28rpx;
        color: #2d3748;
        font-weight: 500;
      }
    }
  }
}

// 游戏主体
.game-body {
  margin: 0 32rpx;
}

// 转盘区域
.wheel-section {
  position: relative;
  display: flex;
  justify-content: center;
  margin-bottom: 48rpx;
  
  .wheel-container {
    width: 560rpx;
    height: 560rpx;
    transition: transform 3s cubic-bezier(0.23, 1, 0.32, 1);
    
    .wheel {
      width: 100%;
      height: 100%;
      border-radius: 50%;
      background: conic-gradient(
        #ff6b6b 0deg 45deg,
        #4ecdc4 45deg 90deg,
        #ff6b6b 90deg 135deg,
        #4ecdc4 135deg 180deg,
        #ff6b6b 180deg 225deg,
        #4ecdc4 225deg 270deg,
        #ff6b6b 270deg 315deg,
        #4ecdc4 315deg 360deg
      );
      position: relative;
      box-shadow: 0 16rpx 48rpx rgba(0,0,0,0.15);
      
      .wheel-item {
        position: absolute;
        top: 50%;
        left: 50%;
        transform-origin: 0 0;
        margin-left: 80rpx;
        margin-top: -20rpx;
        
        text {
          font-size: 28rpx;
          color: #fff;
          font-weight: 600;
          text-shadow: 0 2rpx 4rpx rgba(0,0,0,0.3);
        }
      }
    }
  }
  
  .wheel-pointer {
    position: absolute;
    top: -20rpx;
    left: 50%;
    transform: translateX(-50%);
    font-size: 48rpx;
    color: #ff6b6b;
    z-index: 10;
  }
}

// 开始按钮
.spin-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 80rpx 32rpx;
  padding: 32rpx 0;
  background: linear-gradient(135deg, #9b59b6 0%, #8e44ad 100%);
  border-radius: 48rpx;
  box-shadow: 0 12rpx 36rpx rgba(155, 89, 182, 0.4);
  
  text {
    font-size: 36rpx;
    font-weight: 700;
    color: #fff;
  }
}

// 结果卡片
.result-card {
  background: #fff;
  border-radius: 32rpx;
  padding: 40rpx;
  box-shadow: 0 8rpx 32rpx rgba(0,0,0,0.1);
  
  .result-type {
    text-align: center;
    margin-bottom: 24rpx;
    
    text {
      font-size: 40rpx;
      font-weight: 700;
    }
    
    &.truth {
      text {
        color: #4ecdc4;
      }
    }
    
    &.dare {
      text {
        color: #ff6b6b;
      }
    }
  }
  
  .result-content {
    background: #f8f9fc;
    border-radius: 20rpx;
    padding: 40rpx 32rpx;
    margin-bottom: 32rpx;
    text-align: center;
    
    text {
      font-size: 34rpx;
      color: #2d3748;
      line-height: 1.6;
      font-weight: 500;
    }
  }
  
  .result-actions {
    display: flex;
    gap: 24rpx;
    
    .btn {
      flex: 1;
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 24rpx 0;
      border-radius: 20rpx;
      
      text {
        font-size: 28rpx;
        font-weight: 600;
      }
      
      &.btn-complete {
        background: linear-gradient(135deg, #68d391 0%, #48bb78 100%);
        
        text {
          color: #fff;
        }
      }
      
      &.btn-skip {
        background: #f1f5f9;
        
        text {
          color: #5a6c7d;
        }
      }
    }
  }
}

// 开始区域
.start-section {
  margin: 0 32rpx;
  
  .btn-start {
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 32rpx 0;
    background: linear-gradient(135deg, #9b59b6 0%, #8e44ad 100%);
    border-radius: 48rpx;
    box-shadow: 0 12rpx 36rpx rgba(155, 89, 182, 0.4);
    
    text {
      font-size: 36rpx;
      font-weight: 700;
      color: #fff;
    }
  }
}

// 玩家区域
.players-section {
  margin: 32rpx;
  
  .section-title {
    font-size: 30rpx;
    font-weight: 600;
    color: #2d3748;
    margin-bottom: 20rpx;
  }
  
  .players-list {
    white-space: nowrap;
    
    .player-item {
      display: inline-flex;
      flex-direction: column;
      align-items: center;
      margin-right: 24rpx;
      padding: 20rpx 32rpx;
      background: #fff;
      border-radius: 20rpx;
      border: 4rpx solid transparent;
      
      &.active {
        border-color: #9b59b6;
      }
      
      .player-avatar {
        font-size: 48rpx;
        margin-bottom: 8rpx;
      }
      
      .player-name {
        font-size: 24rpx;
        color: #5a6c7d;
      }
    }
  }
}

// 历史记录
.history-section {
  margin: 32rpx;
  
  .section-title {
    font-size: 30rpx;
    font-weight: 600;
    color: #2d3748;
    margin-bottom: 20rpx;
  }
  
  .history-list {
    background: #fff;
    border-radius: 24rpx;
    padding: 24rpx;
    box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.06);
    
    .history-item {
      display: flex;
      align-items: center;
      padding: 20rpx 0;
      border-bottom: 2rpx solid #f1f5f9;
      
      &:last-child {
        border-bottom: none;
      }
      
      .history-type {
        width: 48rpx;
        height: 48rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        border-radius: 50%;
        font-size: 24rpx;
        color: #fff;
        margin-right: 16rpx;
        flex-shrink: 0;
        
        &.truth {
          background: #4ecdc4;
        }
        
        &.dare {
          background: #ff6b6b;
        }
      }
      
      .history-content {
        flex: 1;
        font-size: 28rpx;
        color: #2d3748;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
      
      .history-player {
        font-size: 24rpx;
        color: #8b9aad;
        margin-left: 16rpx;
      }
    }
  }
}
</style>