<template>
  <view class="game-page">
    <!-- 头部 -->
    <view class="header">
      <view class="header-left" @click="goBack">
        <u-icon name="arrow-left" color="#fff" size="40"></u-icon>
      </view>
      <view class="header-title">谁是卧底</view>
      <view class="header-right" @click="showRules">
        <text>规则</text>
      </view>
    </view>

    <!-- 游戏状态 -->
    <view class="game-status" v-if="gameState !== 'waiting'">
      <view class="status-item">
        <text class="status-round">第 {{ currentRound }} 轮</text>
      </view>
      <view class="status-item">
        <text class="status-role" v-if="myRole">{{ myRole === 'undercover' ? '🕵️ 卧底' : '👥 平民' }}</text>
      </view>
    </view>

    <!-- 等待开始 -->
    <view class="waiting-section" v-if="gameState === 'waiting'">
      <view class="room-info">
        <text class="room-code">房间号: {{ roomCode }}</text>
        <text class="room-tip">邀请家人加入游戏</text>
      </view>
      
      <view class="players-area">
        <view class="section-title">玩家列表 ({{ players.length }}/{{ maxPlayers }})</view>
        <view class="players-grid">
          <view 
            v-for="(player, index) in players" 
            :key="index"
            class="player-card"
            :class="{ ready: player.isReady }"
          >
            <view class="player-avatar">
              <text>{{ player.avatar || '👤' }}</text>
            </view>
            <text class="player-name">{{ player.name }}</text>
            <text class="player-status" v-if="player.isHost">房主</text>
          </view>
          <view class="player-card empty" v-for="i in (maxPlayers - players.length)" :key="`empty-${i}`">
            <view class="empty-slot">+</view>
            <text class="empty-text">等待加入</text>
          </view>
        </view>
      </view>

      <view class="action-area">
        <view class="btn btn-invite" @click="invitePlayers">
          <text>邀请玩家</text>
        </view>
        <view class="btn btn-start" :class="{ disabled: players.length < 4 }" @click="startGame">
          <text>开始游戏</text>
        </view>
      </view>
    </view>

    <!-- 词语展示 -->
    <view class="word-section" v-if="gameState === 'showing-word'">
      <view class="word-card">
        <text class="word-hint">你的词语是</text>
        <text class="word-text">{{ myWord }}</text>
        <text class="word-role">{{ myRole === 'undercover' ? '(你是卧底)' : '(你是平民)' }}</text>
      </view>
      <view class="btn btn-confirm" @click="confirmWord">
        <text>确认记住</text>
      </view>
    </view>

    <!-- 描述阶段 -->
    <view class="desc-section" v-if="gameState === 'describing'">
      <view class="current-player" v-if="currentPlayer">
        <text>当前描述: {{ currentPlayer.name }}</text>
      </view>
      
      <view class="desc-input-area" v-if="isMyTurn">
        <view class="input-wrapper">
          <textarea 
            v-model="myDescription"
            class="desc-input"
            placeholder="请描述你的词语（不能说出词语本身）..."
            maxlength="50"
          />
        </view>
        <view class="btn btn-submit" @click="submitDescription">
          <text>提交描述</text>
        </view>
      </view>

      <view class="descriptions-list" v-if="descriptions.length > 0">
        <view class="section-title">本轮描述</view>
        <view 
          v-for="(desc, index) in descriptions" 
          :key="index"
          class="desc-item"
        >
          <view class="desc-player">{{ desc.playerName }}</view>
          <view class="desc-content">{{ desc.content }}</view>
        </view>
      </view>
    </view>

    <!-- 投票阶段 -->
    <view class="vote-section" v-if="gameState === 'voting'">
      <view class="vote-title">投票选出你认为是卧底的人</view>
      <view class="vote-players">
        <view 
          v-for="player in alivePlayers" 
          :key="player.id"
          class="vote-player"
          :class="{ selected: votedPlayer === player.id, self: player.id === myId }"
          @click="votePlayer(player.id)"
        >
          <view class="player-avatar">
            <text>{{ player.avatar || '👤' }}</text>
          </view>
          <text class="player-name">{{ player.name }}</text>
          <view class="vote-count" v-if="player.votes > 0">{{ player.votes }}票</view>
        </view>
      </view>
      <view class="btn btn-confirm-vote" @click="confirmVote">
        <text>确认投票</text>
      </view>
    </view>

    <!-- 投票结果 -->
    <view class="vote-result" v-if="gameState === 'vote-result'">
      <view class="result-title">投票结果</view>
      <view class="eliminated-player">
        <text>{{ eliminatedPlayer?.name }} 被淘汰了</text>
        <text class="eliminated-role">TA是 {{ eliminatedPlayer?.role === 'undercover' ? '卧底' : '平民' }}</text>
      </view>
      <view class="btn btn-next" @click="nextRound">
        <text>下一轮</text>
      </view>
    </view>

    <!-- 游戏结束 -->
    <view class="gameover-section" v-if="gameState === 'gameover'">
      <view class="winner-display">
        <text class="winner-icon">{{ winner === 'undercover' ? '🕵️' : '👥' }}</text>
        <text class="winner-title">{{ winner === 'undercover' ? '卧底获胜！' : '平民获胜！' }}</text>
      </view>
      <view class="roles-reveal">
        <view 
          v-for="player in players" 
          :key="player.id"
          class="role-item"
        >
          <text class="player-name">{{ player.name }}</text>
          <text class="player-role" :class="player.role">
            {{ player.role === 'undercover' ? '🕵️ 卧底' : '👥 平民' }}
          </text>
        </view>
      </view>
      <view class="action-area">
        <view class="btn btn-again" @click="restartGame">
          <text>再来一局</text>
        </view>
        <view class="btn btn-back" @click="goBack">
          <text>返回大厅</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { gameApi } from '../../../api/game.js'

// 游戏状态
const gameState = ref('waiting') // waiting, showing-word, describing, voting, vote-result, gameover
const roomCode = ref('8888')
const currentRound = ref(1)
const maxPlayers = ref(8)
const myId = ref(1)
const myRole = ref('')
const myWord = ref('')
const myDescription = ref('')
const currentPlayer = ref(null)
const votedPlayer = ref(null)
const eliminatedPlayer = ref(null)
const winner = ref('')

// 玩家列表
const players = ref([
  { id: 1, name: '我', avatar: '👤', isHost: true, isReady: true, role: '', votes: 0 },
  { id: 2, name: '玩家2', avatar: '👤', isHost: false, isReady: true, role: '', votes: 0 },
  { id: 3, name: '玩家3', avatar: '👤', isHost: false, isReady: true, role: '', votes: 0 },
  { id: 4, name: '玩家4', avatar: '👤', isHost: false, isReady: true, role: '', votes: 0 }
])

// 描述列表
const descriptions = ref([])

// 存活玩家
const alivePlayers = computed(() => players.value.filter(p => !p.isEliminated))

// 是否轮到当前玩家
const isMyTurn = computed(() => currentPlayer.value?.id === myId.value)

// 返回
const goBack = () => {
  uni.navigateBack()
}

// 显示规则
const showRules = () => {
  uni.showModal({
    title: '谁是卧底规则',
    content: '1. 每人获得一个词语，卧底词语与他人不同\n2. 轮流描述自己的词语，不能直接说出\n3. 投票选出最可疑的人\n4. 卧底存活到最后即获胜',
    showCancel: false
  })
}

// 邀请玩家
const invitePlayers = () => {
  uni.showModal({
    title: '邀请家人',
    content: `房间号: ${roomCode.value}\n分享给家人加入游戏`,
    showCancel: false
  })
}

// 开始游戏
const startGame = () => {
  if (players.value.length < 4) {
    uni.showToast({ title: '至少需要4人', icon: 'none' })
    return
  }
  
  // 分配角色
  const roles = ['undercover', 'civilian', 'civilian', 'civilian']
  // 随机打乱
  for (let i = roles.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [roles[i], roles[j]] = [roles[j], roles[i]]
  }
  
  players.value.forEach((player, index) => {
    player.role = roles[index] || 'civilian'
    if (player.id === myId.value) {
      myRole.value = player.role
      myWord.value = player.role === 'undercover' ? '苹果' : '香蕉'
    }
  })
  
  gameState.value = 'showing-word'
}

// 确认记住词语
const confirmWord = () => {
  gameState.value = 'describing'
  currentPlayer.value = players.value[0]
}

// 提交描述
const submitDescription = () => {
  if (!myDescription.value.trim()) {
    uni.showToast({ title: '请输入描述', icon: 'none' })
    return
  }
  
  descriptions.value.push({
    playerName: '我',
    content: myDescription.value
  })
  
  // 切换到下一个玩家
  const currentIndex = players.value.findIndex(p => p.id === currentPlayer.value.id)
  const nextIndex = (currentIndex + 1) % players.value.length
  currentPlayer.value = players.value[nextIndex]
  myDescription.value = ''
  
  // 所有人都描述完后进入投票阶段
  if (descriptions.value.length >= players.value.length) {
    gameState.value = 'voting'
  }
}

// 投票
const votePlayer = (playerId) => {
  if (playerId === myId.value) {
    uni.showToast({ title: '不能投自己', icon: 'none' })
    return
  }
  votedPlayer.value = playerId
}

// 确认投票
const confirmVote = () => {
  if (!votedPlayer.value) {
    uni.showToast({ title: '请选择投票对象', icon: 'none' })
    return
  }
  
  // 模拟投票结果
  const randomEliminated = players.value[Math.floor(Math.random() * players.value.length)]
  eliminatedPlayer.value = randomEliminated
  gameState.value = 'vote-result'
}

// 下一轮
const nextRound = () => {
  currentRound.value++
  descriptions.value = []
  votedPlayer.value = null
  eliminatedPlayer.value = null
  gameState.value = 'describing'
  currentPlayer.value = players.value[0]
  
  // 检查游戏结束条件
  if (currentRound.value > 3) {
    winner.value = 'civilian'
    gameState.value = 'gameover'
  }
}

// 重新开始
const restartGame = () => {
  gameState.value = 'waiting'
  currentRound.value = 1
  myRole.value = ''
  myWord.value = ''
  descriptions.value = []
  votedPlayer.value = null
  eliminatedPlayer.value = null
  players.value.forEach(p => {
    p.role = ''
    p.votes = 0
    p.isEliminated = false
  })
}
</script>

<style lang="scss" scoped>
.game-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #e74c3c 0%, #f8f9fc 30%);
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

// 游戏状态
.game-status {
  display: flex;
  justify-content: space-around;
  margin: 0 32rpx 24rpx;
  
  .status-item {
    background: rgba(255,255,255,0.9);
    padding: 16rpx 32rpx;
    border-radius: 32rpx;
    
    .status-round {
      font-size: 28rpx;
      color: #e74c3c;
      font-weight: 600;
    }
    
    .status-role {
      font-size: 28rpx;
      color: #2d3748;
      font-weight: 600;
    }
  }
}

// 等待区域
.waiting-section {
  margin: 0 32rpx;
  
  .room-info {
    text-align: center;
    margin-bottom: 32rpx;
    
    .room-code {
      display: block;
      font-size: 56rpx;
      font-weight: 700;
      color: #e74c3c;
      margin-bottom: 12rpx;
    }
    
    .room-tip {
      font-size: 26rpx;
      color: #8b9aad;
    }
  }
  
  .players-area {
    .section-title {
      font-size: 30rpx;
      font-weight: 600;
      color: #2d3748;
      margin-bottom: 20rpx;
    }
    
    .players-grid {
      display: grid;
      grid-template-columns: repeat(4, 1fr);
      gap: 16rpx;
      margin-bottom: 32rpx;
      
      .player-card {
        background: #fff;
        border-radius: 20rpx;
        padding: 24rpx 12rpx;
        text-align: center;
        position: relative;
        
        &.ready {
          border: 4rpx solid #68d391;
        }
        
        &.empty {
          background: rgba(255,255,255,0.5);
          border: 2rpx dashed #ccc;
        }
        
        .player-avatar {
          font-size: 48rpx;
          margin-bottom: 8rpx;
        }
        
        .player-name {
          font-size: 24rpx;
          color: #2d3748;
          display: block;
        }
        
        .player-status {
          position: absolute;
          top: 8rpx;
          right: 8rpx;
          font-size: 20rpx;
          color: #e74c3c;
          background: rgba(231, 76, 60, 0.1);
          padding: 4rpx 12rpx;
          border-radius: 12rpx;
        }
        
        .empty-slot {
          width: 72rpx;
          height: 72rpx;
          display: flex;
          align-items: center;
          justify-content: center;
          background: #e2e8f0;
          border-radius: 50%;
          margin: 0 auto 8rpx;
          font-size: 40rpx;
          color: #8b9aad;
        }
        
        .empty-text {
          font-size: 22rpx;
          color: #8b9aad;
        }
      }
    }
  }
  
  .action-area {
    display: flex;
    gap: 24rpx;
    
    .btn {
      flex: 1;
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 28rpx 0;
      border-radius: 24rpx;
      
      text {
        font-size: 30rpx;
        font-weight: 600;
        color: #fff;
      }
      
      &.btn-invite {
        background: #fff;
        box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.08);
        
        text {
          color: #e74c3c;
        }
      }
      
      &.btn-start {
        background: linear-gradient(135deg, #e74c3c 0%, #c0392b 100%);
        box-shadow: 0 8rpx 24rpx rgba(231, 76, 60, 0.35);
        
        &.disabled {
          background: #ccc;
          box-shadow: none;
        }
      }
    }
  }
}

// 词语展示
.word-section {
  margin: 0 32rpx;
  
  .word-card {
    background: #fff;
    border-radius: 32rpx;
    padding: 80rpx 48rpx;
    text-align: center;
    margin-bottom: 32rpx;
    box-shadow: 0 8rpx 32rpx rgba(0,0,0,0.1);
    
    .word-hint {
      display: block;
      font-size: 28rpx;
      color: #8b9aad;
      margin-bottom: 24rpx;
    }
    
    .word-text {
      display: block;
      font-size: 72rpx;
      font-weight: 700;
      color: #e74c3c;
      margin-bottom: 16rpx;
    }
    
    .word-role {
      display: block;
      font-size: 28rpx;
      color: #f6ad55;
    }
  }
  
  .btn-confirm {
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 28rpx 0;
    background: linear-gradient(135deg, #e74c3c 0%, #c0392b 100%);
    border-radius: 24rpx;
    box-shadow: 0 8rpx 24rpx rgba(231, 76, 60, 0.35);
    
    text {
      font-size: 30rpx;
      font-weight: 600;
      color: #fff;
    }
  }
}

// 描述阶段
.desc-section {
  margin: 0 32rpx;
  
  .current-player {
    text-align: center;
    margin-bottom: 24rpx;
    
    text {
      font-size: 30rpx;
      color: #2d3748;
      font-weight: 500;
    }
  }
  
  .desc-input-area {
    margin-bottom: 32rpx;
    
    .input-wrapper {
      background: #fff;
      border-radius: 24rpx;
      padding: 24rpx;
      margin-bottom: 24rpx;
      box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.06);
      
      .desc-input {
        width: 100%;
        height: 160rpx;
        font-size: 30rpx;
        color: #2d3748;
      }
    }
    
    .btn-submit {
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 28rpx 0;
      background: linear-gradient(135deg, #e74c3c 0%, #c0392b 100%);
      border-radius: 24rpx;
      
      text {
        font-size: 30rpx;
        font-weight: 600;
        color: #fff;
      }
    }
  }
  
  .descriptions-list {
    .section-title {
      font-size: 30rpx;
      font-weight: 600;
      color: #2d3748;
      margin-bottom: 20rpx;
    }
    
    .desc-item {
      background: #fff;
      border-radius: 20rpx;
      padding: 24rpx;
      margin-bottom: 16rpx;
      
      .desc-player {
        font-size: 26rpx;
        color: #e74c3c;
        font-weight: 600;
        margin-bottom: 8rpx;
      }
      
      .desc-content {
        font-size: 30rpx;
        color: #2d3748;
      }
    }
  }
}

// 投票阶段
.vote-section {
  margin: 0 32rpx;
  
  .vote-title {
    font-size: 32rpx;
    font-weight: 600;
    color: #2d3748;
    text-align: center;
    margin-bottom: 32rpx;
  }
  
  .vote-players {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 20rpx;
    margin-bottom: 32rpx;
    
    .vote-player {
      background: #fff;
      border-radius: 20rpx;
      padding: 32rpx 16rpx;
      text-align: center;
      border: 4rpx solid transparent;
      position: relative;
      
      &.selected {
        border-color: #e74c3c;
      }
      
      &.self {
        opacity: 0.5;
      }
      
      .player-avatar {
        font-size: 56rpx;
        margin-bottom: 12rpx;
      }
      
      .player-name {
        font-size: 26rpx;
        color: #2d3748;
        display: block;
      }
      
      .vote-count {
        position: absolute;
        top: 8rpx;
        right: 8rpx;
        background: #e74c3c;
        color: #fff;
        font-size: 22rpx;
        padding: 4rpx 12rpx;
        border-radius: 12rpx;
      }
    }
  }
  
  .btn-confirm-vote {
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 28rpx 0;
    background: linear-gradient(135deg, #e74c3c 0%, #c0392b 100%);
    border-radius: 24rpx;
    
    text {
      font-size: 30rpx;
      font-weight: 600;
      color: #fff;
    }
  }
}

// 投票结果
.vote-result {
  margin: 0 32rpx;
  text-align: center;
  
  .result-title {
    font-size: 36rpx;
    font-weight: 700;
    color: #2d3748;
    margin-bottom: 32rpx;
  }
  
  .eliminated-player {
    background: #fff;
    border-radius: 24rpx;
    padding: 48rpx;
    margin-bottom: 32rpx;
    
    text {
      display: block;
      font-size: 36rpx;
      color: #2d3748;
      margin-bottom: 16rpx;
    }
    
    .eliminated-role {
      font-size: 28rpx;
      color: #e74c3c;
    }
  }
  
  .btn-next {
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 28rpx 0;
    background: linear-gradient(135deg, #e74c3c 0%, #c0392b 100%);
    border-radius: 24rpx;
    
    text {
      font-size: 30rpx;
      font-weight: 600;
      color: #fff;
    }
  }
}

// 游戏结束
.gameover-section {
  margin: 0 32rpx;
  
  .winner-display {
    text-align: center;
    margin-bottom: 48rpx;
    
    .winner-icon {
      display: block;
      font-size: 120rpx;
      margin-bottom: 16rpx;
    }
    
    .winner-title {
      font-size: 48rpx;
      font-weight: 700;
      color: #e74c3c;
    }
  }
  
  .roles-reveal {
    background: #fff;
    border-radius: 24rpx;
    padding: 32rpx;
    margin-bottom: 32rpx;
    
    .role-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 20rpx 0;
      border-bottom: 2rpx solid #f1f5f9;
      
      &:last-child {
        border-bottom: none;
      }
      
      .player-name {
        font-size: 30rpx;
        color: #2d3748;
      }
      
      .player-role {
        font-size: 28rpx;
        font-weight: 600;
        
        &.undercover {
          color: #e74c3c;
        }
        
        &.civilian {
          color: #68d391;
        }
      }
    }
  }
  
  .action-area {
    display: flex;
    gap: 24rpx;
    
    .btn {
      flex: 1;
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 28rpx 0;
      border-radius: 24rpx;
      
      text {
        font-size: 30rpx;
        font-weight: 600;
        color: #fff;
      }
      
      &.btn-again {
        background: linear-gradient(135deg, #e74c3c 0%, #c0392b 100%);
      }
      
      &.btn-back {
        background: #8b9aad;
      }
    }
  }
}
</style>