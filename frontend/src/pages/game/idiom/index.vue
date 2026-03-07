<template>
  <view class="game-page">
    <!-- 头部 -->
    <view class="header">
      <view class="header-left" @click="goBack">
        <u-icon name="arrow-left" color="#fff" size="40"></u-icon>
      </view>
      <view class="header-title">成语接龙</view>
      <view class="header-right" @click="showRules">
        <text>规则</text>
      </view>
    </view>

    <!-- 游戏信息 -->
    <view class="game-info-card">
      <view class="info-item">
        <text class="info-value">{{ chainCount }}</text>
        <text class="info-label">接龙数</text>
      </view>
      <view class="info-divider"></view>
      <view class="info-item">
        <text class="info-value">{{ currentLevel }}</text>
        <text class="info-label">关卡</text>
      </view>
      <view class="info-divider"></view>
      <view class="info-item">
        <text class="info-value">{{ totalScore }}</text>
        <text class="info-label">总分</text>
      </view>
    </view>

    <!-- 成语链条展示 -->
    <view class="chain-container">
      <scroll-view class="chain-scroll" scroll-y>
        <view class="chain-list">
          <view 
            v-for="(item, index) in idiomChain" 
            :key="index"
            class="chain-item"
            :class="{ 'user-item': item.isUser, 'ai-item': !item.isUser }"
          >
            <view class="chain-avatar">
              <text>{{ item.isUser ? '👤' : '🤖' }}</text>
            </view>
            <view class="chain-content">
              <text class="chain-idiom">{{ item.idiom }}</text>
              <text class="chain-pinyin">{{ item.pinyin }}</text>
            </view>
          </view>
        </view>
      </scroll-view>
    </view>

    <!-- 当前提示 -->
    <view class="current-hint" v-if="lastIdiom">
      <text class="hint-text">请接 "{{ lastIdiom.charAt(lastIdiom.length - 1) }}" 开头的成语</text>
    </view>

    <!-- 输入区域 -->
    <view class="input-section">
      <view class="input-wrapper">
        <input 
          v-model="userInput"
          class="idiom-input"
          placeholder="输入四字成语..."
          maxlength="4"
          confirm-type="send"
          @confirm="submitIdiom"
        />
      </view>
      <view class="btn-submit" @click="submitIdiom">
        <text>发送</text>
      </view>
    </view>

    <!-- 提示按钮 -->
    <view class="hint-section">
      <view class="hint-btn" @click="showHint">
        <text>💡 提示 (-10分)</text>
      </view>
      <view class="giveup-btn" @click="giveUp">
        <text>🏳️ 认输</text>
      </view>
    </view>

    <!-- 提示弹窗 -->
    <u-popup :show="showHintPopup" mode="center" round="20" @close="showHintPopup = false">
      <view class="hint-popup">
        <view class="hint-title">💡 提示</view>
        <view class="hint-content" v-if="hintIdiom">
          <text>试试这个成语：</text>
          <text class="hint-idiom">{{ hintIdiom }}</text>
        </view>
        <view class="hint-actions">
          <view class="btn btn-use" @click="useHint">
            <text>使用提示</text>
          </view>
          <view class="btn btn-cancel" @click="showHintPopup = false">
            <text>取消</text>
          </view>
        </view>
      </view>
    </u-popup>

    <!-- 游戏结束弹窗 -->
    <u-popup :show="gameOver" mode="center" round="20">
      <view class="gameover-popup">
        <view class="gameover-icon">🎉</view>
        <view class="gameover-title">游戏结束</view>
        <view class="gameover-stats">
          <view class="stat-item">
            <text class="stat-value">{{ chainCount }}</text>
            <text class="stat-label">接龙数</text>
          </view>
          <view class="stat-item">
            <text class="stat-value">{{ totalScore }}</text>
            <text class="stat-label">总得分</text>
          </view>
        </view>
        <view class="gameover-reason">{{ gameOverReason }}</view>
        <view class="popup-actions">
          <view class="btn btn-restart" @click="restartGame">
            <text>再来一局</text>
          </view>
          <view class="btn btn-back" @click="goBack">
            <text>返回大厅</text>
          </view>
        </view>
      </view>
    </u-popup>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { gameApi } from '../../../api/game.js'

// 游戏状态
const chainCount = ref(0)
const currentLevel = ref(1)
const totalScore = ref(0)
const userInput = ref('')
const idiomChain = ref([])
const showHintPopup = ref(false)
const hintIdiom = ref('')
const gameOver = ref(false)
const gameOverReason = ref('')

// 计算最后一个成语
const lastIdiom = computed(() => {
  if (idiomChain.value.length === 0) return ''
  return idiomChain.value[idiomChain.value.length - 1].idiom
})

// 返回
const goBack = () => {
  uni.navigateBack()
}

// 显示规则
const showRules = () => {
  uni.showModal({
    title: '成语接龙规则',
    content: '1. 根据上一个成语的最后一个字接龙\n2. 必须是四字成语\n3. 不能重复使用已出现的成语\n4. 接龙越多得分越高！',
    showCancel: false
  })
}

// 开始游戏
const startGame = async () => {
  try {
    // AI 先出一个成语
    const mockIdioms = ['一马当先', '先见之明', '明察秋毫', '毫不犹豫', '豫章故郡']
    const randomIdiom = mockIdioms[Math.floor(Math.random() * mockIdioms.length)]
    
    idiomChain.value = [{
      idiom: randomIdiom,
      pinyin: 'Mock Pinyin',
      isUser: false
    }]
    chainCount.value = 0
  } catch (e) {
    console.error('开始游戏失败', e)
  }
}

// 提交成语
const submitIdiom = async () => {
  const input = userInput.value.trim()
  
  if (input.length !== 4) {
    uni.showToast({ title: '请输入四字成语', icon: 'none' })
    return
  }
  
  // 检查是否以正确字开头
  if (lastIdiom.value) {
    const lastChar = lastIdiom.value.charAt(lastIdiom.value.length - 1)
    if (input.charAt(0) !== lastChar) {
      uni.showToast({ title: `请以"${lastChar}"开头`, icon: 'none' })
      return
    }
  }
  
  // 检查是否已使用
  const used = idiomChain.value.some(item => item.idiom === input)
  if (used) {
    uni.showToast({ title: '该成语已使用', icon: 'none' })
    return
  }
  
  // TODO: 验证成语是否有效
  
  // 添加用户成语
  idiomChain.value.push({
    idiom: input,
    pinyin: '',
    isUser: true
  })
  
  chainCount.value++
  totalScore.value += 10 * currentLevel.value
  userInput.value = ''
  
  // AI 接龙
  setTimeout(() => {
    aiRespond()
  }, 1000)
}

// AI 回应
const aiRespond = async () => {
  const lastChar = lastIdiom.value.charAt(lastIdiom.value.length - 1)
  
  // 模拟 AI 查找成语
  const mockResponses = {
    '先': ['先入为主', '先声夺人', '先礼后兵'],
    '明': ['明知故犯', '明争暗斗', '明眸皓齿'],
    '毫': ['毫不犹豫', '毫不动摇', '毫无顾忌'],
    '豫': ['豫章故郡']
  }
  
  const responses = mockResponses[lastChar] || ['天下无双', '双管齐下', '下不为例']
  const randomResponse = responses[Math.floor(Math.random() * responses.length)]
  
  // 检查 AI 是否能接龙
  if (Math.random() > 0.9) {
    gameOver.value = true
    gameOverReason.value = 'AI 无法接龙，你赢了！🏆'
    addPoints(totalScore.value)
    return
  }
  
  idiomChain.value.push({
    idiom: randomResponse,
    pinyin: '',
    isUser: false
  })
  
  // 升级判定
  if (chainCount.value > 0 && chainCount.value % 5 === 0) {
    currentLevel.value++
    uni.showToast({ title: `升级到第 ${currentLevel.value} 关！`, icon: 'none' })
  }
}

// 显示提示
const showHint = () => {
  if (totalScore.value < 10) {
    uni.showToast({ title: '积分不足', icon: 'none' })
    return
  }
  
  const lastChar = lastIdiom.value.charAt(lastIdiom.value.length - 1)
  const mockHints = {
    '先': '先入为主',
    '明': '明知故犯',
    '毫': '毫不犹豫',
    '豫': '豫章故郡'
  }
  hintIdiom.value = mockHints[lastChar] || '天下无双'
  showHintPopup.value = true
}

// 使用提示
const useHint = () => {
  totalScore.value -= 10
  userInput.value = hintIdiom.value
  showHintPopup.value = false
}

// 认输
const giveUp = () => {
  uni.showModal({
    title: '确认认输',
    content: '确定要结束游戏吗？',
    success: (res) => {
      if (res.confirm) {
        gameOver.value = true
        gameOverReason.value = '你认输了，再接再厉！💪'
        addPoints(Math.floor(totalScore.value / 2))
      }
    }
  })
}

// 添加积分
const addPoints = async (points) => {
  try {
    await gameApi.addPoints({
      gameType: 'idiom',
      points: points,
      description: `成语接龙 接龙${chainCount.value}个`
    })
  } catch (e) {
    console.error('添加积分失败', e)
  }
}

// 重新开始
const restartGame = () => {
  idiomChain.value = []
  chainCount.value = 0
  currentLevel.value = 1
  totalScore.value = 0
  userInput.value = ''
  gameOver.value = false
  gameOverReason.value = ''
  startGame()
}

onMounted(() => {
  startGame()
})
</script>

<style lang="scss" scoped>
.game-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #FFD700 0%, #f8f9fc 30%);
  display: flex;
  flex-direction: column;
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

// 游戏信息
.game-info-card {
  display: flex;
  justify-content: space-around;
  align-items: center;
  background: #fff;
  border-radius: 24rpx;
  margin: 0 32rpx 24rpx;
  padding: 28rpx;
  box-shadow: 0 8rpx 32rpx rgba(255, 215, 0, 0.2);
  
  .info-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    
    .info-value {
      font-size: 44rpx;
      font-weight: 700;
      color: #FFD700;
      margin-bottom: 4rpx;
    }
    
    .info-label {
      font-size: 22rpx;
      color: #8b9aad;
    }
  }
  
  .info-divider {
    width: 2rpx;
    height: 48rpx;
    background: #e2e8f0;
  }
}

// 成语链条
.chain-container {
  flex: 1;
  margin: 0 32rpx 24rpx;
  background: #fff;
  border-radius: 32rpx;
  padding: 24rpx;
  box-shadow: 0 8rpx 32rpx rgba(0,0,0,0.06);
  
  .chain-scroll {
    height: 100%;
    
    .chain-list {
      .chain-item {
        display: flex;
        align-items: center;
        margin-bottom: 20rpx;
        
        &.user-item {
          flex-direction: row-reverse;
          
          .chain-content {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            margin-left: 0;
            margin-right: 16rpx;
            
            .chain-idiom, .chain-pinyin {
              color: #fff;
            }
          }
        }
        
        .chain-avatar {
          width: 72rpx;
          height: 72rpx;
          border-radius: 50%;
          background: #f1f5f9;
          display: flex;
          align-items: center;
          justify-content: center;
          flex-shrink: 0;
          
          text {
            font-size: 36rpx;
          }
        }
        
        .chain-content {
          flex: 1;
          margin-left: 16rpx;
          background: #f8f9fc;
          border-radius: 20rpx;
          padding: 20rpx 28rpx;
          
          .chain-idiom {
            font-size: 34rpx;
            font-weight: 600;
            color: #2d3748;
            display: block;
            margin-bottom: 4rpx;
          }
          
          .chain-pinyin {
            font-size: 22rpx;
            color: #8b9aad;
          }
        }
      }
    }
  }
}

// 当前提示
.current-hint {
  margin: 0 32rpx 20rpx;
  text-align: center;
  
  .hint-text {
    font-size: 28rpx;
    color: #FFD700;
    font-weight: 600;
    background: rgba(255, 215, 0, 0.1);
    padding: 16rpx 32rpx;
    border-radius: 32rpx;
  }
}

// 输入区域
.input-section {
  display: flex;
  gap: 16rpx;
  margin: 0 32rpx 20rpx;
  
  .input-wrapper {
    flex: 1;
    background: #fff;
    border-radius: 24rpx;
    padding: 8rpx;
    box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.06);
    
    .idiom-input {
      height: 88rpx;
      padding: 0 32rpx;
      font-size: 32rpx;
      color: #2d3748;
      text-align: center;
      letter-spacing: 8rpx;
    }
  }
  
  .btn-submit {
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 0 40rpx;
    background: linear-gradient(135deg, #FFD700 0%, #FFA500 100%);
    border-radius: 24rpx;
    box-shadow: 0 8rpx 24rpx rgba(255, 215, 0, 0.35);
    
    text {
      font-size: 30rpx;
      font-weight: 600;
      color: #fff;
    }
  }
}

// 提示区域
.hint-section {
  display: flex;
  justify-content: center;
  gap: 32rpx;
  margin: 0 32rpx 40rpx;
  
  .hint-btn, .giveup-btn {
    display: flex;
    align-items: center;
    gap: 8rpx;
    padding: 20rpx 32rpx;
    background: #fff;
    border-radius: 32rpx;
    box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.06);
    
    text {
      font-size: 26rpx;
      color: #5a6c7d;
    }
  }
  
  .hint-btn {
    text {
      color: #f6ad55;
    }
  }
}

// 提示弹窗
.hint-popup {
  padding: 48rpx;
  text-align: center;
  min-width: 480rpx;
  
  .hint-title {
    font-size: 36rpx;
    font-weight: 600;
    color: #2d3748;
    margin-bottom: 24rpx;
  }
  
  .hint-content {
    margin-bottom: 32rpx;
    
    text {
      font-size: 28rpx;
      color: #5a6c7d;
      display: block;
    }
    
    .hint-idiom {
      font-size: 40rpx;
      font-weight: 700;
      color: #FFD700;
      margin-top: 16rpx;
    }
  }
  
  .hint-actions {
    display: flex;
    gap: 24rpx;
    
    .btn {
      flex: 1;
      padding: 24rpx 0;
      border-radius: 20rpx;
      
      text {
        font-size: 28rpx;
        font-weight: 600;
      }
      
      &.btn-use {
        background: linear-gradient(135deg, #FFD700 0%, #FFA500 100%);
        
        text {
          color: #fff;
        }
      }
      
      &.btn-cancel {
        background: #f1f5f9;
        
        text {
          color: #5a6c7d;
        }
      }
    }
  }
}

// 游戏结束弹窗
.gameover-popup {
  padding: 60rpx 48rpx;
  text-align: center;
  min-width: 520rpx;
  
  .gameover-icon {
    font-size: 100rpx;
    margin-bottom: 24rpx;
  }
  
  .gameover-title {
    font-size: 40rpx;
    font-weight: 700;
    color: #2d3748;
    margin-bottom: 32rpx;
  }
  
  .gameover-stats {
    display: flex;
    justify-content: center;
    gap: 60rpx;
    margin-bottom: 24rpx;
    
    .stat-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      
      .stat-value {
        font-size: 56rpx;
        font-weight: 700;
        color: #FFD700;
      }
      
      .stat-label {
        font-size: 24rpx;
        color: #8b9aad;
        margin-top: 8rpx;
      }
    }
  }
  
  .gameover-reason {
    font-size: 28rpx;
    color: #5a6c7d;
    margin-bottom: 32rpx;
    padding: 16rpx;
    background: rgba(255, 215, 0, 0.1);
    border-radius: 16rpx;
  }
  
  .popup-actions {
    display: flex;
    gap: 24rpx;
    
    .btn {
      flex: 1;
      padding: 24rpx 0;
      border-radius: 20rpx;
      
      text {
        font-size: 28rpx;
        font-weight: 600;
      }
      
      &.btn-restart {
        background: linear-gradient(135deg, #FFD700 0%, #FFA500 100%);
        
        text {
          color: #fff;
        }
      }
      
      &.btn-back {
        background: #f1f5f9;
        
        text {
          color: #5a6c7d;
        }
      }
    }
  }
}
</style>