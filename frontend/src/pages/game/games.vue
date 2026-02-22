<template>
  <view class="games-page">
    <!-- 自定义导航栏 -->
    <view class="custom-nav">
      <view class="nav-back" @click="goBack">
        <text class="icon">‹</text>
      </view>
      <view class="nav-title">更多游戏</view>
      <view class="nav-action" @click="showMyPoints">
        <text>💎 {{ myPoints }}</text>
      </view>
    </view>

    <!-- 每日推荐 -->
    <view class="featured-game">
      <view class="featured-card" @click="startGame(featuredGame)">
        <view class="featured-badge">今日推荐</view>
        <view class="featured-icon">{{ featuredGame.icon }}</view>
        <text class="featured-name">{{ featuredGame.name }}</text>
        <text class="featured-desc">{{ featuredGame.description }}</text>
        <view class="featured-stats">
          <text>👥 {{ featuredGame.players }} 人在玩</text>
          <text>⭐ {{ featuredGame.rating }}分</text>
        </view>
        <button class="play-btn">开始游戏</button>
      </view>
    </view>

    <!-- 游戏分类 -->
    <view class="game-categories">
      <view class="category-section">
        <view class="section-header">
          <text class="section-title">🧩 益智游戏</text>
        </view>
        
        <view class="games-grid">
          <view 
            v-for="game in puzzleGames" 
            :key="game.id"
            class="game-card"
            @click="startGame(game)"
          >
            <view class="game-icon">{{ game.icon }}</view>
            <text class="game-name">{{ game.name }}</text>
            <text class="game-desc">{{ game.description }}</text>
            <view class="game-reward">
              <text>💎 +{{ game.reward }}</text>
            </view>
          </view>
        </view>
      </view>

      <view class="category-section">
        <view class="section-header">
          <text class="section-title">❓ 知识问答</text>
        </view>
        
        <view class="games-grid">
          <view 
            v-for="game in quizGames" 
            :key="game.id"
            class="game-card"
            @click="startGame(game)"
          >
            <view class="game-icon">{{ game.icon }}</view>
            <text class="game-name">{{ game.name }}</text>
            <text class="game-desc">{{ game.description }}</text>
            <view class="game-reward">
              <text>💎 +{{ game.reward }}</text>
            </view>
          </view>
        </view>
      </view>

      <view class="category-section">
        <view class="section-header">
          <text class="section-title">🎯 休闲游戏</text>
        </view>
        
        <view class="games-grid">
          <view 
            v-for="game in casualGames" 
            :key="game.id"
            class="game-card"
            @click="startGame(game)"
          >
            <view class="game-icon">{{ game.icon }}</view>
            <text class="game-name">{{ game.name }}</text>
            <text class="game-desc">{{ game.description }}</text>
            <view class="game-reward">
              <text>💎 +{{ game.reward }}</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 游戏记录 -->
    <view class="game-history">
      <view class="section-header">
        <text class="section-title">📜 游戏记录</text>
        <text class="view-all" @click="viewAllHistory">查看全部 ›</text>
      </view>

      <view 
        v-for="record in gameHistory.slice(0, 3)" 
        :key="record.id"
        class="history-item"
      >
        <view class="history-icon">{{ record.icon }}</view>
        <view class="history-info">
          <text class="game-title">{{ record.gameName }}</text>
          <text class="play-time">{{ record.time }}</text>
        </view>
        <view class="history-result">
          <text class="score">{{ record.score }}分</text>
          <text v-if="record.reward > 0" class="reward">+{{ record.reward }}💎</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'

const myPoints = ref(1250)

const featuredGame = ref({
  id: 0,
  name: '每日猜谜',
  description: '每天更新10道谜语，答对即可获得积分奖励！',
  icon: '🤔',
  players: 128,
  rating: 4.8,
  reward: 50
})

const puzzleGames = ref([
  {
    id: 1,
    name: '猜谜语',
    description: '经典谜语猜猜看',
    icon: '🎭',
    reward: 30
  },
  {
    id: 2,
    name: '脑筋急转弯',
    description: '考验你的思维',
    icon: '🧠',
    reward: 40
  },
  {
    id: 3,
    name: '成语接龙',
    description: '接龙挑战',
    icon: '🐉',
    reward: 35
  }
])

const quizGames = ref([
  {
    id: 4,
    name: '家庭知识',
    description: '了解家庭的点滴',
    icon: '🏠',
    reward: 50
  },
  {
    id: 5,
    name: '百科问答',
    description: '广博的知识库',
    icon: '📚',
    reward: 45
  },
  {
    id: 6,
    name: '趣味测验',
    description: '有趣的测试题',
    icon: '📝',
    reward: 30
  }
])

const casualGames = ref([
  {
    id: 7,
    name: '幸运转盘',
    description: '试试手气',
    icon: '🎰',
    reward: '随机'
  },
  {
    id: 8,
    name: '每日签到',
    description: '签到领积分',
    icon: '📅',
    reward: 20
  },
  {
    id: 9,
    name: '抽奖活动',
    description: '大奖等你拿',
    icon: '🎁',
    reward: '随机'
  }
])

const gameHistory = ref([
  {
    id: 1,
    gameName: '每日猜谜',
    icon: '🤔',
    score: 80,
    reward: 40,
    time: '今天 10:30'
  },
  {
    id: 2,
    gameName: '成语接龙',
    icon: '🐉',
    score: 150,
    reward: 35,
    time: '昨天 20:15'
  },
  {
    id: 3,
    gameName: '家庭知识',
    icon: '🏠',
    score: 200,
    reward: 50,
    time: '昨天 19:30'
  }
])

const goBack = () => {
  uni.navigateBack()
}

const showMyPoints = () => {
  uni.showModal({
    title: '我的积分',
    content: `当前积分: ${myPoints.value}\n积分可用于兑换奖品和解锁新功能`,
    showCancel: false
  })
}

const startGame = (game) => {
  uni.showModal({
    title: game.name,
    content: `奖励: ${game.reward}积分\n准备好了吗？`,
    confirmText: '开始',
    success: (res) => {
      if (res.confirm) {
        uni.showToast({ title: '游戏开始！', icon: 'success' })
      }
    }
  })
}

const viewAllHistory = () => {
  uni.showToast({ title: '查看全部记录', icon: 'none' })
}
</script>

<style lang="scss" scoped>
.games-page {
  min-height: 100vh;
  background: #0f0f23;
}

.custom-nav {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 50px 20px 20px;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);

  .nav-back {
    width: 36px;
    height: 36px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: rgba(255,255,255,0.1);
    border-radius: 10px;

    .icon {
      font-size: 24px;
      color: #fff;
    }
  }

  .nav-title {
    font-size: 18px;
    font-weight: 600;
    color: #fff;
  }

  .nav-action {
    display: flex;
    align-items: center;
    gap: 4px;
    padding: 8px 12px;
    background: rgba(245,158,11,0.2);
    border-radius: 8px;
    font-size: 14px;
    color: #f59e0b;
  }
}

.featured-game {
  padding: 15px;

  .featured-card {
    position: relative;
    padding: 25px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 20px;
    text-align: center;

    .featured-badge {
      position: absolute;
      top: 15px;
      left: 15px;
      padding: 4px 12px;
      background: rgba(245,158,11,0.9);
      border-radius: 8px;
      font-size: 12px;
      color: #fff;
    }

    .featured-icon {
      font-size: 60px;
      margin-bottom: 15px;
    }

    .featured-name {
      display: block;
      font-size: 22px;
      font-weight: 700;
      color: #fff;
      margin-bottom: 8px;
    }

    .featured-desc {
      display: block;
      font-size: 14px;
      color: rgba(255,255,255,0.8);
      margin-bottom: 15px;
    }

    .featured-stats {
      display: flex;
      justify-content: center;
      gap: 20px;
      margin-bottom: 20px;
      font-size: 13px;
      color: rgba(255,255,255,0.7);
    }

    .play-btn {
      width: 200px;
      padding: 14px;
      background: #fff;
      border-radius: 12px;
      font-size: 16px;
      font-weight: 600;
      color: #667eea;
      border: none;

      &::after {
        border: none;
      }
    }
  }
}

.game-categories {
  padding: 0 15px;

  .category-section {
    margin-bottom: 20px;

    .section-header {
      margin-bottom: 12px;

      .section-title {
        font-size: 16px;
        font-weight: 600;
        color: #fff;
      }
    }

    .games-grid {
      display: grid;
      grid-template-columns: repeat(3, 1fr);
      gap: 10px;

      .game-card {
        padding: 15px 10px;
        background: rgba(255,255,255,0.05);
        border-radius: 12px;
        text-align: center;

        .game-icon {
          font-size: 32px;
          margin-bottom: 8px;
        }

        .game-name {
          display: block;
          font-size: 13px;
          font-weight: 500;
          color: #fff;
          margin-bottom: 4px;
        }

        .game-desc {
          display: block;
          font-size: 10px;
          color: #64748b;
          margin-bottom: 8px;
        }

        .game-reward {
          padding: 4px 8px;
          background: rgba(245,158,11,0.1);
          border-radius: 6px;
          display: inline-block;

          text {
            font-size: 11px;
            color: #f59e0b;
          }
        }
      }
    }
  }
}

.game-history {
  padding: 0 15px 30px;

  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 15px;

    .section-title {
      font-size: 16px;
      font-weight: 600;
      color: #fff;
    }

    .view-all {
      font-size: 13px;
      color: #3b82f6;
    }
  }

  .history-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 12px;
    background: rgba(255,255,255,0.05);
    border-radius: 12px;
    margin-bottom: 8px;

    .history-icon {
      width: 40px;
      height: 40px;
      background: rgba(255,255,255,0.1);
      border-radius: 10px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 20px;
    }

    .history-info {
      flex: 1;

      .game-title {
        display: block;
        font-size: 14px;
        color: #fff;
        margin-bottom: 2px;
      }

      .play-time {
        font-size: 11px;
        color: #64748b;
      }
    }

    .history-result {
      text-align: right;

      .score {
        display: block;
        font-size: 14px;
        font-weight: 600;
        color: #fff;
        margin-bottom: 2px;
      }

      .reward {
        font-size: 11px;
        color: #f59e0b;
      }
    }
  }
}
</style>
