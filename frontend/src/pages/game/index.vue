<template>
  <view class="game-page">
    <!-- 顶部背景 -->
    <view class="header-bg"></view>
    
    <!-- 用户信息 -->
    <view class="user-section">
      <view class="user-card">
        <view class="user-avatar">
          <text>😎</text>
        </view>
        <view class="user-info">
          <text class="user-name">我</text>
          <view class="user-stats">
            <view class="stat-item">
              <text class="stat-num">{{ userPoints }}</text>
              <text class="stat-label">积分</text>
            </view>
            <view class="stat-item">
              <text class="stat-num">{{ rank }}</text>
              <text class="stat-label">排名</text>
            </view>
          </view>
        </view>
      </view>
    </view>
    
    <!-- 游戏列表 -->
    <view class="games-section">
      <view class="section-title">🎮 家庭游戏</view>
      
      <view class="game-grid">
        <view class="game-card" v-for="game in games" :key="game.id" @click="playGame(game)">
          <view class="game-icon" :style="{ background: game.color }">
            <text>{{ game.icon }}</text>
          </view>
          <text class="game-name">{{ game.name }}</text>
          <text class="game-desc">{{ game.desc }}</text>
          <view class="game-reward">
            <text>奖励: {{ game.reward }}积分</text>
          </view>
        </view>
      </view>
    </view>
    
    <!-- 排行榜 -->
    <view class="rank-section">
      <view class="section-title">🏆 排行榜</view>
      
      <view class="rank-list">
        <view 
          v-for="(item, index) in rankList" 
          :key="index"
          class="rank-item"
          :class="{ top3: index < 3 }"
        >
          <view class="rank-num">
            <text v-if="index === 0">🥇</text>
            <text v-else-if="index === 1">🥈</text>
            <text v-else-if="index === 2">🥉</text>
            <text v-else>{{ index + 1 }}</text>
          </view>
          <view class="rank-avatar">
            <text>{{ item.avatar }}</text>
          </view>
          <text class="rank-name">{{ item.name }}</text>
          <text class="rank-points">{{ item.points }}分</text>
        </view>
      </view>
    </view>
    
    <!-- 游戏记录 -->
    <view class="history-section">
      <view class="section-title">📜 游戏记录</view>
      
      <view class="history-list">
        <view v-for="record in history" :key="record.id" class="history-item">
          <view class="history-icon">
            <text>{{ record.gameIcon }}</text>
          </view>
          <view class="history-info">
            <text class="history-name">{{ record.gameName }}</text>
            <text class="history-time">{{ record.time }}</text>
          </view>
          <view class="history-reward" :class="{ positive: record.points > 0 }">
            <text>{{ record.points > 0 ? '+' : '' }}{{ record.points }}分</text>
          </view>
        </view>
      </view>
    </view>
    
    <!-- 每日任务 -->
    <view class="tasks-section">
      <view class="section-title">📋 每日任务</view>
      
      <view class="tasks-list">
        <view v-for="task in dailyTasks" :key="task.id" class="task-item" :class="{ completed: task.completed }">
          <view class="task-check">
            <text v-if="task.completed">✓</text>
          </view>
          <view class="task-info">
            <text class="task-name">{{ task.name }}</text>
            <text class="task-desc">{{ task.desc }}</text>
          </view>
          <view class="task-reward">
            <text>+{{ task.reward }}分</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'

const userPoints = ref(1250)
const rank = ref(2)

const games = ref([
  { id: 1, name: '答题挑战', desc: '答对问题赢积分', icon: '🧠', color: 'linear-gradient(135deg, #667eea, #764ba2)', reward: 50 },
  { id: 2, name: '每日签到', desc: '签到领积分', icon: '📅', color: 'linear-gradient(135deg, #11998e, #38ef7d)', reward: 10 },
  { id: 3, name: '幸运抽奖', desc: '运气好就中奖', icon: '🎰', color: 'linear-gradient(135deg, #f093fb, #f5576c)', reward: 100 },
  { id: 4, name: '成语接龙', desc: '展现才华', icon: '📝', color: 'linear-gradient(135deg, #4facfe, #00f2fe)', reward: 30 },
  { id: 5, name: '猜歌名', desc: '听歌猜名', icon: '🎵', color: 'linear-gradient(135deg, #fa709a, #fee140)', reward: 40 },
  { id: 6, name: '表情包大赛', desc: '斗图谁怕谁', icon: '😄', color: 'linear-gradient(135deg, #a18cd1, #fbc2eb)', reward: 20 }
])

const rankList = ref([
  { name: '老婆', points: 2580, avatar: '👩' },
  { name: '我', points: 1250, avatar: '😎' },
  { name: '老公', points: 980, avatar: '👨' },
  { name: '宝宝', points: 450, avatar: '👶' }
])

const history = ref([
  { id: 1, gameName: '答题挑战', gameIcon: '🧠', time: '今天 14:30', points: 30 },
  { id: 2, gameName: '每日签到', gameIcon: '📅', time: '今天 08:00', points: 10 },
  { id: 3, gameName: '成语接龙', gameIcon: '📝', time: '昨天 20:15', points: -10 },
  { id: 4, gameName: '幸运抽奖', gameIcon: '🎰', time: '昨天 19:30', points: 50 }
])

const dailyTasks = ref([
  { id: 1, name: '每日签到', desc: '签到1次', reward: 10, completed: true },
  { id: 2, name: '答题达人', desc: '答对3道题', reward: 30, completed: true },
  { id: 3, name: '分享游戏', desc: '分享给家人', reward: 20, completed: false },
  { id: 4, name: '连续登录', desc: '登录3天', reward: 50, completed: false }
])

const playGame = (game) => {
  uni.showToast({ title: `开始${game.name}`, icon: 'none' })
}
</script>

<style lang="scss" scoped>
.game-page {
  min-height: 100vh;
  background: #f8f9fc;
  padding-bottom: 40rpx;
}

.header-bg {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 350rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.user-section {
  position: relative;
  padding: 100rpx 32rpx 32rpx;
  
  .user-card {
    display: flex;
    align-items: center;
    background: #fff;
    border-radius: 24rpx;
    padding: 32rpx;
    box-shadow: 0 8rpx 32rpx rgba(102, 126, 234, 0.2);
    
    .user-avatar {
      width: 120rpx;
      height: 120rpx;
      background: linear-gradient(135deg, #667eea, #764ba2);
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 60rpx;
      margin-right: 24rpx;
    }
    
    .user-info {
      flex: 1;
      
      .user-name {
        font-size: 36rpx;
        font-weight: 600;
        color: #2d3748;
        display: block;
        margin-bottom: 16rpx;
      }
      
      .user-stats {
        display: flex;
        gap: 40rpx;
        
        .stat-item {
          text-align: center;
          
          .stat-num {
            font-size: 36rpx;
            font-weight: 600;
            color: #667eea;
            display: block;
          }
          
          .stat-label {
            font-size: 24rpx;
            color: #8b9aad;
          }
        }
      }
    }
  }
}

.section-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #2d3748;
  margin: 32rpx 32rpx 24rpx;
}

.games-section {
  .game-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 24rpx;
    padding: 0 32rpx;
    
    .game-card {
      background: #fff;
      border-radius: 24rpx;
      padding: 24rpx;
      text-align: center;
      
      .game-icon {
        width: 100rpx;
        height: 100rpx;
        border-radius: 24rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        margin: 0 auto 16rpx;
        font-size: 50rpx;
      }
      
      .game-name {
        font-size: 28rpx;
        font-weight: 600;
        color: #2d3748;
        display: block;
        margin-bottom: 8rpx;
      }
      
      .game-desc {
        font-size: 24rpx;
        color: #8b9aad;
        display: block;
        margin-bottom: 16rpx;
      }
      
      .game-reward {
        display: inline-block;
        padding: 8rpx 20rpx;
        background: rgba(102, 126, 234, 0.1);
        border-radius: 20rpx;
        font-size: 22rpx;
        color: #667eea;
      }
    }
  }
}

.rank-section {
  .rank-list {
    margin: 0 32rpx;
    background: #fff;
    border-radius: 24rpx;
    padding: 16rpx;
    
    .rank-item {
      display: flex;
      align-items: center;
      padding: 20rpx;
      border-radius: 16rpx;
      
      &.top3 {
        background: linear-gradient(90deg, rgba(102, 126, 234, 0.05), transparent);
      }
      
      .rank-num {
        width: 60rpx;
        font-size: 28rpx;
        color: #8b9aad;
        
        text {
          font-size: 36rpx;
        }
      }
      
      .rank-avatar {
        width: 64rpx;
        height: 64rpx;
        background: #f1f5f9;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-right: 16rpx;
        font-size: 32rpx;
      }
      
      .rank-name {
        flex: 1;
        font-size: 28rpx;
        color: #2d3748;
      }
      
      .rank-points {
        font-size: 28rpx;
        font-weight: 600;
        color: #667eea;
      }
    }
  }
}

.history-section {
  .history-list {
    margin: 0 32rpx;
    background: #fff;
    border-radius: 24rpx;
    padding: 16rpx;
    
    .history-item {
      display: flex;
      align-items: center;
      padding: 20rpx;
      border-bottom: 2rpx solid #f1f5f9;
      
      &:last-child {
        border-bottom: none;
      }
      
      .history-icon {
        width: 64rpx;
        height: 64rpx;
        background: #f8f9fc;
        border-radius: 16rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-right: 16rpx;
        font-size: 32rpx;
      }
      
      .history-info {
        flex: 1;
        
        .history-name {
          font-size: 28rpx;
          color: #2d3748;
          display: block;
          margin-bottom: 4rpx;
        }
        
        .history-time {
          font-size: 24rpx;
          color: #8b9aad;
        }
      }
      
      .history-reward {
        font-size: 28rpx;
        font-weight: 500;
        color: #8b9aad;
        
        &.positive {
          color: #68d391;
        }
      }
    }
  }
}

.tasks-section {
  .tasks-list {
    margin: 0 32rpx;
    background: #fff;
    border-radius: 24rpx;
    padding: 16rpx;
    
    .task-item {
      display: flex;
      align-items: center;
      padding: 20rpx;
      border-radius: 16rpx;
      
      &.completed {
        opacity: 0.6;
        
        .task-name {
          text-decoration: line-through;
        }
      }
      
      .task-check {
        width: 44rpx;
        height: 44rpx;
        border: 3rpx solid #ddd;
        border-radius: 50%;
        margin-right: 16rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        color: #667eea;
        font-size: 24rpx;
      }
      
      .task-info {
        flex: 1;
        
        .task-name {
          font-size: 28rpx;
          color: #2d3748;
          display: block;
          margin-bottom: 4rpx;
        }
        
        .task-desc {
          font-size: 24rpx;
          color: #8b9aad;
        }
      }
      
      .task-reward {
        font-size: 26rpx;
        color: #667eea;
        font-weight: 500;
      }
    }
  }
}
</style>
