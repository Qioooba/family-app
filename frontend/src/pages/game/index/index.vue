<template>
  <view class="game-page">
    <!-- 头部 -->
    <view class="header-section">
      <view class="header-bg">
        <view class="bg-pattern"></view>
      </view>
      <view class="header-content">
        <view class="header-title">
          <text class="title-icon">🎮</text>
          <text class="title-text">家庭游戏</text>
        </view>
        <view class="header-subtitle">和家人一起欢乐互动</view>
      </view>
    </view>

    <!-- 用户积分卡片 -->
    <view class="points-card animate-in">
      <view class="points-info">
        <view class="points-item">
          <text class="points-value">{{ userPoints }}</text>
          <text class="points-label">我的积分</text>
        </view>
        <view class="points-divider"></view>
        <view class="points-item">
          <text class="points-value">{{ userRank }}</text>
          <text class="points-label">当前排名</text>
        </view>
        <view class="points-divider"></view>
        <view class="points-item">
          <text class="points-value">{{ gamesPlayed }}</text>
          <text class="points-label">游戏次数</text>
        </view>
      </view>
      <view class="points-action" @click="goToConfig">
        <u-icon name="setting" color="#fff" size="32"></u-icon>
        <text>游戏设置</text>
      </view>
    </view>

    <!-- 游戏入口网格 -->
    <view class="games-section">
      <view class="section-header">
        <text class="section-icon">🎯</text>
        <text class="section-title">全部游戏</text>
      </view>
      
      <view class="games-grid">
        <view 
          v-for="(game, index) in games" 
          :key="game.id"
          class="game-card"
          :class="`game-${index}`"
          :style="{ animationDelay: `${index * 0.05}s` }"
          @click="navigateToGame(game.path)"
        >
          <view class="game-icon-wrapper" :style="{ background: game.bgColor, boxShadow: game.shadow }">
            <text class="game-icon">{{ game.icon }}</text>
          </view>
          <view class="game-info">
            <text class="game-name">{{ game.name }}</text>
            <text class="game-desc">{{ game.desc }}</text>
          </view>
          <view class="game-arrow">
            <u-icon name="arrow-right" color="#ccc" size="28"></u-icon>
          </view>
        </view>
      </view>
    </view>

    <!-- 积分排行榜 -->
    <view class="ranking-section animate-in">
      <view class="section-header">
        <view class="title-wrapper">
          <text class="section-icon">🏆</text>
          <text class="section-title">积分排行榜</text>
        </view>
        <view class="more-btn" @click="showFullRanking">
          <text>查看全部</text>
          <u-icon name="arrow-right" color="#6B8DD6" size="24"></u-icon>
        </view>
      </view>
      
      <view class="ranking-list">
        <view 
          v-for="(user, index) in rankingList.slice(0, 5)" 
          :key="user.userId"
          class="ranking-item"
          :class="{ 'top-three': index < 3 }"
          :style="{ animationDelay: `${index * 0.08}s` }"
        >
          <view class="rank-badge" :class="`rank-${index + 1}`">
            <text v-if="index < 3">{{ ['🥇', '🥈', '🥉'][index] }}</text>
            <text v-else>{{ index + 1 }}</text>
          </view>
          <view class="user-avatar">
            <image v-if="user.avatar" :src="user.avatar" mode="aspectFill" />
            <text v-else class="avatar-text">{{ user.nickname?.charAt(0) || '?' }}</text>
          </view>
          <view class="user-info">
            <text class="user-name">{{ user.nickname || '未知用户' }}</text>
            <text class="user-games">{{ user.gameCount || 0 }} 场游戏</text>
          </view>
          <view class="user-points">
            <text class="points-num">{{ user.points || 0 }}</text>
            <text class="points-unit">分</text>
          </view>
        </view>
      </view>
      
      <view v-if="rankingList.length === 0" class="empty-state">
        <text class="empty-icon">📊</text>
        <text class="empty-text">暂无排行榜数据</text>
      </view>
    </view>

    <!-- 最近游戏记录 -->
    <view class="history-section animate-in">
      <view class="section-header">
        <view class="title-wrapper">
          <text class="section-icon">📝</text>
          <text class="section-title">最近游戏</text>
        </view>
        <view class="more-btn" @click="showFullHistory">
          <text>查看全部</text>
          <u-icon name="arrow-right" color="#6B8DD6" size="24"></u-icon>
        </view>
      </view>
      
      <view class="history-list">
        <view 
          v-for="(record, index) in recentGames" 
          :key="record.id"
          class="history-item"
          :style="{ animationDelay: `${index * 0.08}s` }"
        >
          <view class="history-icon-wrapper" :style="{ background: getGameByType(record.gameType)?.bgColor }">
            <text class="history-icon">{{ getGameByType(record.gameType)?.icon || '🎮' }}</text>
          </view>
          <view class="history-info">
            <text class="history-game">{{ getGameName(record.gameType) }}</text>
            <text class="history-time">{{ formatTime(record.createTime) }}</text>
          </view>
          <view class="history-result" :class="{ win: record.isWin }">
            <text v-if="record.pointsChange > 0" class="points-change">+{{ record.pointsChange }}</text>
            <text v-else-if="record.pointsChange < 0" class="points-change negative">{{ record.pointsChange }}</text>
            <text v-else class="points-change">{{ record.result || '完成' }}</text>
          </view>
        </view>
      </view>
      
      <view v-if="recentGames.length === 0" class="empty-state">
        <text class="empty-icon">🎲</text>
        <text class="empty-text">还没有游戏记录</text>
        <text class="empty-subtext">快去选择一款游戏开始吧！</text>
      </view>
    </view>

    <!-- 底部间距 -->
    <view class="bottom-space"></view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { gameApi } from '../../../api/game.js'
import { useUserStore } from '../../../stores/user'

const userStore = useUserStore()

// 用户数据
const userPoints = ref(0)
const userRank = ref('--')
const gamesPlayed = ref(0)

// 游戏列表
const games = [
  { 
    id: 'riddle', 
    name: '猜谜语', 
    desc: '益智解谜，开动脑筋',
    icon: '🤔', 
    bgColor: '#FF6B6B', 
    shadow: '0 8rpx 20rpx rgba(255, 107, 107, 0.35)',
    path: '/pages/game/riddle/index'
  },
  { 
    id: 'quiz', 
    name: '问答竞赛', 
    desc: '知识问答，谁是学霸',
    icon: '❓', 
    bgColor: '#4facfe', 
    shadow: '0 8rpx 20rpx rgba(79, 172, 254, 0.35)',
    path: '/pages/game/quiz/index'
  },
  { 
    id: 'idiom', 
    name: '成语接龙', 
    desc: '成语大挑战',
    icon: '📚', 
    bgColor: '#FFD700', 
    shadow: '0 8rpx 20rpx rgba(255, 215, 0, 0.35)',
    path: '/pages/game/idiom/index'
  },
  { 
    id: 'truth-dare', 
    name: '真心话大冒险', 
    desc: '真心话还是大冒险',
    icon: '🎲', 
    bgColor: '#9b59b6', 
    shadow: '0 8rpx 20rpx rgba(155, 89, 182, 0.35)',
    path: '/pages/game/truth-dare/index'
  },
  { 
    id: 'undercover', 
    name: '谁是卧底', 
    desc: '找出隐藏的卧底',
    icon: '🕵️', 
    bgColor: '#e74c3c', 
    shadow: '0 8rpx 20rpx rgba(231, 76, 60, 0.35)',
    path: '/pages/game/undercover/index'
  },
  { 
    id: 'draw-guess', 
    name: '你画我猜', 
    desc: '画画猜猜看',
    icon: '🎨', 
    bgColor: '#f39c12', 
    shadow: '0 8rpx 20rpx rgba(243, 156, 18, 0.35)',
    path: '/pages/game/draw-guess/index'
  },
  { 
    id: 'karaoke', 
    name: 'K歌评分', 
    desc: '一展歌喉',
    icon: '🎤', 
    bgColor: '#e84393', 
    shadow: '0 8rpx 20rpx rgba(232, 67, 147, 0.35)',
    path: '/pages/game/karaoke/index'
  },
  { 
    id: 'sports', 
    name: '家庭运动会', 
    desc: '运动竞技挑战',
    icon: '🏃', 
    bgColor: '#00b894', 
    shadow: '0 8rpx 20rpx rgba(0, 184, 148, 0.35)',
    path: '/pages/game/sports/index'
  },
  { 
    id: 'cooking', 
    name: '厨艺比拼', 
    desc: '展示你的厨艺',
    icon: '👨‍🍳', 
    bgColor: '#fdcb6e', 
    shadow: '0 8rpx 20rpx rgba(253, 203, 110, 0.35)',
    path: '/pages/game/cooking/index'
  },
  { 
    id: 'treasure', 
    name: '家庭寻宝', 
    desc: '寻找隐藏的宝藏',
    icon: '💎', 
    bgColor: '#00cec9', 
    shadow: '0 8rpx 20rpx rgba(0, 206, 201, 0.35)',
    path: '/pages/game/treasure/index'
  }
]

// 排行榜数据
const rankingList = ref([])

// 最近游戏记录
const recentGames = ref([])

// 根据游戏类型获取游戏信息
const getGameByType = (type) => {
  return games.find(g => g.id === type)
}

// 获取游戏名称
const getGameName = (type) => {
  const game = getGameByType(type)
  return game ? game.name : '未知游戏'
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const month = date.getMonth() + 1
  const day = date.getDate()
  const hours = date.getHours().toString().padStart(2, '0')
  const minutes = date.getMinutes().toString().padStart(2, '0')
  return `${month}/${day} ${hours}:${minutes}`
}

// 加载用户积分数据
const loadUserPoints = async () => {
  try {
    const res = await gameApi.getUserPoints()
    if (res) {
      userPoints.value = res.points || 0
      gamesPlayed.value = res.gameCount || 0
    }
  } catch (e) {
    console.error('加载用户积分失败', e)
  }
}

// 加载排行榜
const loadRankings = async () => {
  try {
    const familyId = uni.getStorageSync('currentFamilyId') || 1
    const res = await gameApi.getRankings(familyId)
    if (res && Array.isArray(res)) {
      rankingList.value = res
      // 计算当前用户排名
      const currentUserId = userStore.userInfo?.id
      const rank = res.findIndex(u => u.userId === currentUserId)
      if (rank !== -1) {
        userRank.value = rank + 1
      }
    }
  } catch (e) {
    console.error('加载排行榜失败', e)
  }
}

// 加载最近游戏记录
const loadRecentGames = async () => {
  try {
    const res = await gameApi.getPointsHistory()
    if (res && Array.isArray(res)) {
      recentGames.value = res.slice(0, 5)
    }
  } catch (e) {
    console.error('加载游戏记录失败', e)
  }
}

// 导航到游戏页面
const navigateToGame = (path) => {
  uni.navigateTo({ url: path })
}

// 跳转到设置页面
const goToConfig = () => {
  uni.navigateTo({ url: '/pages/game/config/index' })
}

// 显示完整排行榜
const showFullRanking = () => {
  uni.showToast({ title: '功能开发中', icon: 'none' })
}

// 显示完整历史记录
const showFullHistory = () => {
  uni.showToast({ title: '功能开发中', icon: 'none' })
}

// 页面显示时加载数据
onShow(() => {
  loadUserPoints()
  loadRankings()
  loadRecentGames()
})

onMounted(() => {
  loadUserPoints()
  loadRankings()
  loadRecentGames()
})
</script>

<style lang="scss" scoped>
.game-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #f8f9fc 0%, #f0f4f8 100%);
  padding-bottom: 40rpx;
}

// 头部区域
.header-section {
  position: relative;
  padding: 40rpx 32rpx 80rpx;
  padding-top: 100rpx;
  overflow: hidden;
  
  .header-bg {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 400rpx;
    background: linear-gradient(135deg, #FF6B6B 0%, #ee5a24 50%, #ff9f43 100%);
    border-radius: 0 0 60rpx 60rpx;
    box-shadow: 0 20rpx 60rpx rgba(255, 107, 107, 0.3);
    
    .bg-pattern {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      opacity: 0.1;
      background-image: 
        radial-gradient(circle at 20% 50%, rgba(255,255,255,0.8) 0%, transparent 50%),
        radial-gradient(circle at 80% 20%, rgba(255,255,255,0.6) 0%, transparent 40%);
    }
  }
  
  .header-content {
    position: relative;
    z-index: 1;
    text-align: center;
    
    .header-title {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 16rpx;
      margin-bottom: 16rpx;
      
      .title-icon {
        font-size: 48rpx;
      }
      
      .title-text {
        font-size: 48rpx;
        font-weight: 700;
        color: #fff;
        text-shadow: 0 4rpx 16rpx rgba(0,0,0,0.2);
      }
    }
    
    .header-subtitle {
      font-size: 28rpx;
      color: rgba(255,255,255,0.9);
      font-weight: 500;
    }
  }
}

// 积分卡片
.points-card {
  margin: -40rpx 32rpx 32rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 32rpx;
  padding: 36rpx;
  position: relative;
  z-index: 2;
  box-shadow: 0 16rpx 48rpx rgba(102, 126, 234, 0.35);
  
  &.animate-in {
    animation: slideIn 0.5s ease-out forwards;
    opacity: 0;
    transform: translateY(20rpx);
  }
  
  .points-info {
    display: flex;
    justify-content: space-around;
    align-items: center;
    margin-bottom: 24rpx;
    
    .points-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      
      .points-value {
        font-size: 48rpx;
        font-weight: 700;
        color: #fff;
        margin-bottom: 8rpx;
      }
      
      .points-label {
        font-size: 24rpx;
        color: rgba(255,255,255,0.8);
      }
    }
    
    .points-divider {
      width: 2rpx;
      height: 60rpx;
      background: rgba(255,255,255,0.3);
    }
  }
  
  .points-action {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8rpx;
    padding-top: 24rpx;
    border-top: 2rpx solid rgba(255,255,255,0.2);
    
    text {
      font-size: 26rpx;
      color: rgba(255,255,255,0.9);
      font-weight: 500;
    }
  }
}

// 游戏区域
.games-section {
  margin: 0 32rpx 32rpx;
  
  .section-header {
    display: flex;
    align-items: center;
    margin-bottom: 24rpx;
    
    .section-icon {
      font-size: 36rpx;
      margin-right: 12rpx;
    }
    
    .section-title {
      font-size: 32rpx;
      font-weight: 600;
      color: #2d3748;
    }
  }
  
  .games-grid {
    display: flex;
    flex-direction: column;
    gap: 20rpx;
    
    .game-card {
      display: flex;
      align-items: center;
      background: #fff;
      border-radius: 24rpx;
      padding: 28rpx;
      box-shadow: 0 8rpx 24rpx rgba(107, 141, 214, 0.1), 0 2rpx 8rpx rgba(0,0,0,0.04);
      transition: all 0.3s ease;
      animation: fadeInUp 0.5s ease-out forwards;
      opacity: 0;
      
      &:active {
        transform: scale(0.98);
        box-shadow: 0 4rpx 16rpx rgba(107, 141, 214, 0.15);
      }
      
      .game-icon-wrapper {
        width: 88rpx;
        height: 88rpx;
        border-radius: 24rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        flex-shrink: 0;
        margin-right: 24rpx;
        
        .game-icon {
          font-size: 44rpx;
        }
      }
      
      .game-info {
        flex: 1;
        min-width: 0;
        
        .game-name {
          font-size: 32rpx;
          font-weight: 600;
          color: #2d3748;
          margin-bottom: 8rpx;
          display: block;
        }
        
        .game-desc {
          font-size: 24rpx;
          color: #8b9aad;
        }
      }
      
      .game-arrow {
        flex-shrink: 0;
      }
    }
  }
}

// 排行榜区域
.ranking-section {
  background: #fff;
  border-radius: 32rpx;
  margin: 0 32rpx 32rpx;
  padding: 32rpx;
  box-shadow: 0 8rpx 32rpx rgba(107, 141, 214, 0.08), 0 2rpx 8rpx rgba(0,0,0,0.03);
  
  &.animate-in {
    animation: slideIn 0.5s ease-out forwards;
    opacity: 0;
    transform: translateY(20rpx);
  }
  
  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 28rpx;
    
    .title-wrapper {
      display: flex;
      align-items: center;
      
      .section-icon {
        font-size: 36rpx;
        margin-right: 12rpx;
      }
      
      .section-title {
        font-size: 32rpx;
        font-weight: 600;
        color: #2d3748;
      }
    }
    
    .more-btn {
      display: flex;
      align-items: center;
      gap: 4rpx;
      padding: 10rpx 16rpx;
      background: rgba(107, 141, 214, 0.08);
      border-radius: 24rpx;
      
      text {
        font-size: 24rpx;
        color: #6B8DD6;
        font-weight: 500;
      }
    }
  }
  
  .ranking-list {
    .ranking-item {
      display: flex;
      align-items: center;
      padding: 20rpx 0;
      border-bottom: 2rpx solid #f1f5f9;
      animation: fadeInUp 0.4s ease-out forwards;
      opacity: 0;
      
      &:last-child {
        border-bottom: none;
      }
      
      &.top-three {
        background: linear-gradient(90deg, transparent, rgba(255,215,0,0.05), transparent);
        border-radius: 16rpx;
        padding: 24rpx 16rpx;
        margin: 0 -16rpx;
      }
      
      .rank-badge {
        width: 56rpx;
        height: 56rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-right: 20rpx;
        
        &.rank-1, &.rank-2, &.rank-3 {
          font-size: 40rpx;
        }
        
        text {
          font-size: 28rpx;
          color: #8b9aad;
          font-weight: 600;
        }
      }
      
      .user-avatar {
        width: 72rpx;
        height: 72rpx;
        border-radius: 50%;
        overflow: hidden;
        margin-right: 20rpx;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        display: flex;
        align-items: center;
        justify-content: center;
        
        image {
          width: 100%;
          height: 100%;
        }
        
        .avatar-text {
          font-size: 32rpx;
          color: #fff;
          font-weight: 600;
        }
      }
      
      .user-info {
        flex: 1;
        min-width: 0;
        
        .user-name {
          font-size: 30rpx;
          color: #2d3748;
          font-weight: 500;
          display: block;
          margin-bottom: 4rpx;
        }
        
        .user-games {
          font-size: 22rpx;
          color: #8b9aad;
        }
      }
      
      .user-points {
        text-align: right;
        
        .points-num {
          font-size: 36rpx;
          font-weight: 700;
          color: #FF6B6B;
        }
        
        .points-unit {
          font-size: 22rpx;
          color: #8b9aad;
        }
      }
    }
  }
}

// 历史记录区域
.history-section {
  background: #fff;
  border-radius: 32rpx;
  margin: 0 32rpx;
  padding: 32rpx;
  box-shadow: 0 8rpx 32rpx rgba(107, 141, 214, 0.08), 0 2rpx 8rpx rgba(0,0,0,0.03);
  
  &.animate-in {
    animation: slideIn 0.5s ease-out forwards;
    opacity: 0;
    transform: translateY(20rpx);
    animation-delay: 0.1s;
  }
  
  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 28rpx;
    
    .title-wrapper {
      display: flex;
      align-items: center;
      
      .section-icon {
        font-size: 36rpx;
        margin-right: 12rpx;
      }
      
      .section-title {
        font-size: 32rpx;
        font-weight: 600;
        color: #2d3748;
      }
    }
    
    .more-btn {
      display: flex;
      align-items: center;
      gap: 4rpx;
      padding: 10rpx 16rpx;
      background: rgba(107, 141, 214, 0.08);
      border-radius: 24rpx;
      
      text {
        font-size: 24rpx;
        color: #6B8DD6;
        font-weight: 500;
      }
    }
  }
  
  .history-list {
    .history-item {
      display: flex;
      align-items: center;
      padding: 20rpx 0;
      border-bottom: 2rpx solid #f1f5f9;
      animation: fadeInUp 0.4s ease-out forwards;
      opacity: 0;
      
      &:last-child {
        border-bottom: none;
      }
      
      .history-icon-wrapper {
        width: 72rpx;
        height: 72rpx;
        border-radius: 20rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-right: 20rpx;
        flex-shrink: 0;
        
        .history-icon {
          font-size: 36rpx;
        }
      }
      
      .history-info {
        flex: 1;
        min-width: 0;
        
        .history-game {
          font-size: 30rpx;
          color: #2d3748;
          font-weight: 500;
          display: block;
          margin-bottom: 4rpx;
        }
        
        .history-time {
          font-size: 22rpx;
          color: #8b9aad;
        }
      }
      
      .history-result {
        text-align: right;
        
        .points-change {
          font-size: 32rpx;
          font-weight: 700;
          color: #68d391;
          
          &.negative {
            color: #fc8181;
          }
        }
      }
    }
  }
}

// 空状态
.empty-state {
  text-align: center;
  padding: 60rpx 0;
  
  .empty-icon {
    font-size: 80rpx;
    margin-bottom: 20rpx;
    display: block;
  }
  
  .empty-text {
    font-size: 30rpx;
    color: #5a6c7d;
    font-weight: 500;
    display: block;
    margin-bottom: 8rpx;
  }
  
  .empty-subtext {
    font-size: 26rpx;
    color: #8b9aad;
  }
}

// 底部间距
.bottom-space {
  height: 40rpx;
}

// 动画
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes slideIn {
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>