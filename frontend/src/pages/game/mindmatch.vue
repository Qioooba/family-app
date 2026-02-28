<template>
  <view class="mindmatch-page">
    <!-- 顶部导航 -->
    <view class="header">
      <view class="back-btn" @click="goBack">
        <text class="icon">‹</text>
      </view>
      <view class="title">默契问答</view>
      <view class="stats-btn" @click="showStats">
        <text>📊</text>
      </view>
    </view>

    <!-- 默契值卡片 -->
    <view class="match-rate-card" v-if="matchRate">
      <view class="match-rate-icon">💕</view>
      <view class="match-rate-info">
        <text class="match-rate-label">家庭默契值</text>
        <text class="match-rate-value">{{ matchRate.matchRate }}%</text>
      </view>
      <view class="match-rate-games">
        <text>已玩 {{ matchRate.totalGames }} 局</text>
      </view>
    </view>

    <!-- 游戏状态 -->
    <view class="game-container">
      <!-- 等待开始 -->
      <view v-if="gameStatus === 'idle'" class="idle-state">
        <view class="idle-icon">💑</view>
        <view class="idle-title">默契大考验</view>
        <view class="idle-desc">和家人一起回答相同问题，看看有多默契！</view>
        
        <view class="family-select">
          <view class="select-label">选择对战家人</view>
          <view class="family-members">
            <view 
              v-for="member in familyMembers" 
              :key="member.id"
              class="member-item"
              :class="{ selected: selectedMember?.id === member.id }"
              @click="selectMember(member)"
            >
              <image class="member-avatar" :src="member.avatar || defaultAvatar" mode="aspectFill" />
              <text class="member-name">{{ member.nickname || member.name }}</text>
            </view>
          </view>
        </view>
        
        <button class="start-btn" :disabled="!selectedMember" @click="startGame">
          开始挑战
        </button>
      </view>

      <!-- 回答问题中 -->
      <view v-else-if="gameStatus === 'playing'" class="playing-state">
        <view class="question-card">
          <view class="question-type">{{ currentQuestion?.typeText }}</view>
          <view class="question-text">{{ currentQuestion?.question }}</view>
        </view>
        
        <view class="answer-section">
          <view class="answer-hint">请输入你的答案</view>
          <input 
            class="answer-input" 
            v-model="myAnswer" 
            placeholder="输入你的答案..." 
            confirm-type="done"
            @confirm="submitAnswer"
          />
          <button class="submit-btn" @click="submitAnswer" :disabled="!myAnswer">
            提交答案
          </button>
        </view>
        
        <view class="waiting-hint" v-if="!bothAnswered">
          <text>等待对方回答中...</text>
          <view class="spinner"></view>
        </view>
      </view>

      <!-- 结果展示 -->
      <view v-else-if="gameStatus === 'result'" class="result-state">
        <view class="result-card" :class="{ match: isMatch }">
          <view class="result-icon">{{ isMatch ? '🎉' : '😅' }}</view>
          <view class="result-title">{{ isMatch ? '心有灵犀!' : '答案不同' }}</view>
          
          <view class="answers-comparison">
            <view class="answer-item">
              <text class="player-label">你的答案</text>
              <text class="answer-text">{{ session?.player1Answer }}</text>
            </view>
            <view class="vs">VS</view>
            <view class="answer-item">
              <text class="player-label">对方答案</text>
              <text class="answer-text">{{ session?.player2Answer }}</text>
            </view>
          </view>
          
          <view class="points-earned">
            <text class="points-label">获得</text>
            <text class="points-value">+{{ session?.roundPoints || 0 }}</text>
            <text class="points-label">积分</text>
          </view>
        </view>
        
        <view class="result-actions">
          <button class="continue-btn" @click="continueGame">下一题</button>
          <button class="end-btn" @click="endGame">结束游戏</button>
        </view>
      </view>
    </view>

    <!-- 底部装饰 -->
    <view style="height: 40px;"></view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { gameApi } from '../../api/game'
import { familyApi } from '../../api/family'

const defaultAvatar = 'https://api.dicebear.com/7.x/avataaars/svg?seed=default'

// 游戏状态
const gameStatus = ref('idle') // idle/playing/result
const familyMembers = ref([])
const selectedMember = ref(null)
const currentQuestion = ref(null)
const myAnswer = ref('')
const session = ref(null)
const matchRate = ref(null)

// 计算属性
const bothAnswered = computed(() => {
  return session.value?.player1Answered && session.value?.player2Answered
})

const isMatch = computed(() => {
  if (!session.value) return false
  const ans1 = session.value.player1Answer || ''
  const ans2 = session.value.player2Answer || ''
  return ans1.toLowerCase().trim() === ans2.toLowerCase().trim()
})

// 加载家庭成员
const loadFamilyMembers = async () => {
  try {
    const res = await familyApi.getMyFamily()
    if (res.data?.code === 0 && res.data.data) {
      const members = res.data.data.members || []
      // 过滤掉当前用户
      const currentUserId = uni.getStorageSync('userId')
      familyMembers.value = members.filter(m => m.userId != currentUserId)
    }
  } catch (e) {
    console.error('加载家庭成员失败', e)
    // 使用模拟数据
    familyMembers.value = [
      { id: 2, name: '老婆', nickname: '老婆', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=wife&backgroundColor=ffdfbf' },
      { id: 3, name: '老公', nickname: '老公', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=husband&backgroundColor=c0aede' }
    ]
  }
}

// 加载默契值
const loadMatchRate = async () => {
  try {
    const familyId = uni.getStorageSync('familyId')
    if (!familyId) return
    
    const res = await gameApi.getMindMatchRate(familyId)
    if (res.data?.code === 0) {
      matchRate.value = res.data.data
    }
  } catch (e) {
    console.error('加载默契值失败', e)
  }
}

// 选择对战成员
const selectMember = (member) => {
  selectedMember.value = member
}

// 开始游戏
const startGame = async () => {
  if (!selectedMember.value) return
  
  try {
    const familyId = uni.getStorageSync('familyId')
    const res = await gameApi.createMindMatchSession(familyId, selectedMember.value.id)
    
    if (res.data?.code === 0 && res.data.data) {
      session.value = res.data.data
      // 获取问题详情
      await loadQuestion()
      gameStatus.value = 'playing'
    } else {
      uni.showToast({
        title: res.data?.msg || '创建游戏失败',
        icon: 'none'
      })
    }
  } catch (e) {
    console.error('开始游戏失败', e)
    uni.showToast({
      title: '创建游戏失败',
      icon: 'none'
    })
  }
}

// 加载问题
const loadQuestion = async () => {
  try {
    const res = await gameApi.getMindMatchQuestion()
    if (res.data?.code === 0 && res.data.data) {
      const q = res.data.data
      currentQuestion.value = {
        ...q,
        typeText: q.type === 'spouse' ? '💕 夫妻默契' : q.type === 'parent' ? '👨‍👩‍👧 亲子默契' : '👨‍👩‍👧‍👦 家庭默契'
      }
    }
  } catch (e) {
    console.error('加载问题失败', e)
  }
}

// 提交答案
const submitAnswer = async () => {
  if (!myAnswer.value.trim() || !session.value) return
  
  try {
    const res = await gameApi.answerMindMatchQuestion(session.value.id, myAnswer.value)
    if (res.data?.code === 0 && res.data.data) {
      session.value = res.data.data
      
      // 检查是否双方都回答了
      if (session.value.player1Answered && session.value.player2Answered) {
        gameStatus.value = 'result'
      } else {
        // 等待对方回答
        pollForPartnerAnswer()
      }
    }
  } catch (e) {
    console.error('提交答案失败', e)
    uni.showToast({
      title: '提交失败',
      icon: 'none'
    })
  }
}

// 轮询等待对方回答
const pollForPartnerAnswer = () => {
  const poll = setInterval(async () => {
    try {
      const res = await gameApi.getMindMatchSession(session.value.id)
      if (res.data?.code === 0 && res.data.data) {
        session.value = res.data.data
        
        if (session.value.player1Answered && session.value.player2Answered) {
          clearInterval(poll)
          gameStatus.value = 'result'
        }
      }
    } catch (e) {
      clearInterval(poll)
    }
  }, 2000)
  
  // 30秒后停止轮询
  setTimeout(() => {
    clearInterval(poll)
    if (gameStatus.value === 'playing') {
      gameStatus.value = 'result'
    }
  }, 30000)
}

// 继续下一题
const continueGame = async () => {
  myAnswer.value = ''
  await startGame()
}

// 结束游戏
const endGame = () => {
  gameStatus.value = 'idle'
  session.value = null
  myAnswer.value = ''
  loadMatchRate()
}

// 显示统计
const showStats = async () => {
  try {
    const res = await gameApi.getMyMindMatchStats()
    if (res.data?.code === 0) {
      const stats = res.data.data
      uni.showModal({
        title: '📊 我的战绩',
        content: `总游戏局数: ${stats.totalGames}\n答对次数: ${stats.matchCount}\n总获得积分: ${stats.totalPoints}\n默契正确率: ${stats.matchRate}%`,
        showCancel: false
      })
    }
  } catch (e) {
    console.error('获取统计失败', e)
  }
}

// 返回
const goBack = () => {
  uni.navigateBack()
}

// 初始化
onMounted(() => {
  loadFamilyMembers()
  loadMatchRate()
})
</script>

<script>
export default {
  onLoad() {
    // 初始化问题库
    gameApi.initMindMatchQuestions().catch(() => {})
  }
}
</script>

<style lang="scss" scoped>
.mindmatch-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #1a1a3e 0%, #2d1b4e 50%, #1a1a3e 100%);
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 50px 20px 20px;
  
  .back-btn {
    width: 40px;
    height: 40px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: rgba(255,255,255,0.1);
    border-radius: 12px;
    
    .icon {
      font-size: 28px;
      color: #fff;
    }
  }
  
  .title {
    font-size: 20px;
    font-weight: 700;
    color: #fff;
  }
  
  .stats-btn {
    width: 40px;
    height: 40px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: rgba(255,255,255,0.1);
    border-radius: 12px;
    font-size: 20px;
  }
}

.match-rate-card {
  display: flex;
  align-items: center;
  gap: 16px;
  margin: 0 20px 20px;
  padding: 16px 20px;
  background: linear-gradient(135deg, rgba(236,72,153,0.2) 0%, rgba(168,85,247,0.2) 100%);
  border: 1px solid rgba(236,72,153,0.3);
  border-radius: 16px;
  
  .match-rate-icon {
    font-size: 36px;
  }
  
  .match-rate-info {
    flex: 1;
    
    .match-rate-label {
      display: block;
      font-size: 13px;
      color: rgba(255,255,255,0.7);
    }
    
    .match-rate-value {
      display: block;
      font-size: 28px;
      font-weight: 700;
      color: #f472b6;
    }
  }
  
  .match-rate-games {
    font-size: 12px;
    color: rgba(255,255,255,0.6);
  }
}

.game-container {
  padding: 20px;
}

.idle-state {
  text-align: center;
  padding-top: 40px;
  
  .idle-icon {
    font-size: 80px;
    margin-bottom: 20px;
  }
  
  .idle-title {
    font-size: 28px;
    font-weight: 700;
    color: #fff;
    margin-bottom: 12px;
  }
  
  .idle-desc {
    font-size: 14px;
    color: rgba(255,255,255,0.6);
    margin-bottom: 40px;
  }
  
  .family-select {
    margin-bottom: 40px;
    
    .select-label {
      font-size: 14px;
      color: rgba(255,255,255,0.7);
      margin-bottom: 16px;
    }
    
    .family-members {
      display: flex;
      justify-content: center;
      gap: 20px;
      flex-wrap: wrap;
    }
    
    .member-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 8px;
      padding: 16px;
      background: rgba(255,255,255,0.05);
      border: 2px solid rgba(255,255,255,0.1);
      border-radius: 16px;
      transition: all 0.3s;
      
      &.selected {
        border-color: #ec4899;
        background: rgba(236,72,153,0.1);
      }
      
      .member-avatar {
        width: 64px;
        height: 64px;
        border-radius: 50%;
      }
      
      .member-name {
        font-size: 14px;
        color: #fff;
      }
    }
  }
  
  .start-btn {
    width: 200px;
    height: 50px;
    background: linear-gradient(135deg, #ec4899 0%, #a855f7 100%);
    border: none;
    border-radius: 25px;
    font-size: 16px;
    font-weight: 600;
    color: #fff;
    
    &:disabled {
      opacity: 0.5;
    }
  }
}

.playing-state {
  .question-card {
    padding: 30px 24px;
    background: rgba(255,255,255,0.08);
    border-radius: 20px;
    margin-bottom: 30px;
    
    .question-type {
      font-size: 14px;
      color: #a78bfa;
      margin-bottom: 12px;
    }
    
    .question-text {
      font-size: 22px;
      font-weight: 600;
      color: #fff;
      line-height: 1.5;
    }
  }
  
  .answer-section {
    .answer-hint {
      font-size: 14px;
      color: rgba(255,255,255,0.6);
      margin-bottom: 12px;
    }
    
    .answer-input {
      width: 100%;
      height: 56px;
      padding: 0 20px;
      background: rgba(255,255,255,0.08);
      border: 1px solid rgba(255,255,255,0.1);
      border-radius: 16px;
      font-size: 16px;
      color: #fff;
      margin-bottom: 20px;
      
      &::placeholder {
        color: rgba(255,255,255,0.4);
      }
    }
    
    .submit-btn {
      width: 100%;
      height: 50px;
      background: linear-gradient(135deg, #ec4899 0%, #a855f7 100%);
      border: none;
      border-radius: 25px;
      font-size: 16px;
      font-weight: 600;
      color: #fff;
      
      &:disabled {
        opacity: 0.5;
      }
    }
  }
  
  .waiting-hint {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 12px;
    margin-top: 30px;
    font-size: 14px;
    color: rgba(255,255,255,0.6);
    
    .spinner {
      width: 20px;
      height: 20px;
      border: 2px solid rgba(255,255,255,0.2);
      border-top-color: #ec4899;
      border-radius: 50%;
      animation: spin 1s linear infinite;
    }
  }
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.result-state {
  .result-card {
    padding: 30px;
    background: rgba(255,255,255,0.08);
    border-radius: 20px;
    text-align: center;
    
    &.match {
      background: linear-gradient(135deg, rgba(236,72,153,0.2) 0%, rgba(168,85,247,0.2) 100%);
      border: 1px solid rgba(236,72,153,0.3);
    }
    
    .result-icon {
      font-size: 60px;
      margin-bottom: 16px;
    }
    
    .result-title {
      font-size: 24px;
      font-weight: 700;
      color: #fff;
      margin-bottom: 24px;
    }
    
    .answers-comparison {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 16px;
      margin-bottom: 24px;
      
      .answer-item {
        flex: 1;
        
        .player-label {
          display: block;
          font-size: 12px;
          color: rgba(255,255,255,0.6);
          margin-bottom: 8px;
        }
        
        .answer-text {
          display: block;
          font-size: 16px;
          font-weight: 500;
          color: #fff;
          padding: 12px;
          background: rgba(255,255,255,0.1);
          border-radius: 12px;
        }
      }
      
      .vs {
        font-size: 14px;
        font-weight: 600;
        color: rgba(255,255,255,0.4);
      }
    }
    
    .points-earned {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 8px;
      
      .points-label {
        font-size: 14px;
        color: rgba(255,255,255,0.6);
      }
      
      .points-value {
        font-size: 24px;
        font-weight: 700;
        color: #fbbf24;
      }
    }
  }
  
  .result-actions {
    display: flex;
    gap: 16px;
    margin-top: 24px;
    
    .continue-btn, .end-btn {
      flex: 1;
      height: 50px;
      border-radius: 25px;
      font-size: 16px;
      font-weight: 600;
    }
    
    .continue-btn {
      background: linear-gradient(135deg, #ec4899 0%, #a855f7 100%);
      border: none;
      color: #fff;
    }
    
    .end-btn {
      background: rgba(255,255,255,0.1);
      border: 1px solid rgba(255,255,255,0.2);
      color: rgba(255,255,255,0.8);
    }
  }
}
</style>
