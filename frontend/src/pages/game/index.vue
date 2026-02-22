<template>
  <view class="game-page">
    <!-- 头部 -->
    <view class="game-header"
    >
      <view class="header-title">
        <text class="title-icon">🎮</text>
        <text>家庭游戏</text>
      </view>
      <view class="points-badge" @click="goPoints"
      >
        <text class="points-icon">💎</text>
        <text class="points-value">{{ userPoints }}</text>
      </view>
    </view>

    <!-- 转盘游戏 -->
    <view class="wheel-section"
    >
      <view class="section-title">🎯 幸运转盘</view>
      
      <view class="wheel-container"
      >
        <view class="wheel-wrapper"
        >
          <view class="wheel" :style="{ transform: `rotate(${rotation}deg)` }"
          >
            <view 
              v-for="(item, index) in wheelItems" 
              :key="index"
              class="wheel-item"
              :style="{ 
                transform: `rotate(${index * (360 / wheelItems.length)}deg)`,
                background: item.color 
              }"
            >
              <text class="wheel-item-text">{{ item.name }}</text>
            </view>
          </view>
          <view class="wheel-pointer">▲</view>
        </view>
        
        <view class="wheel-btn" :class="{ spinning: isSpinning }" @click="spin"
        >
          <text v-if="!isSpinning">开始</text>
          <text v-else">转动中...</text>
        </view>
      </view>
      
      <!-- 转盘选择器 -->
      <scroll-view class="wheel-selector" scroll-x
      >
        <view 
          v-for="(wheel, index) in wheels" 
          :key="index"
          class="wheel-option"
          :class="{ active: currentWheel === index }"
          @click="selectWheel(index)"
        >
          <text class="wheel-option-icon">{{ wheel.icon }}</text>
          <text class="wheel-option-name">{{ wheel.name }}</text>
        </view>
      </scroll-view>
    </view>

    <!-- 排行榜 -->
    <view class="ranking-section"
    >
      <view class="section-header"
      >
        <text class="section-title">🏆 本周排行</text>
        <view class="rank-tabs"
        >
          <text 
            v-for="(tab, index) in rankTabs" 
            :key="index"
            class="rank-tab"
            :class="{ active: currentRankTab === index }"
            @click="currentRankTab = index"
          >
            {{ tab }}
          </text>
        </view>
      </view>
      
      <view class="ranking-list"
      >
        <view 
          v-for="(user, index) in rankings" 
          :key="index"
          class="rank-item"
          :class="{ 'top3': index < 3, 'me': user.isMe }"
        >
          <view class="rank-number">
            <text v-if="index < 3" class="rank-medal">{{ ['🥇', '🥈', '🥉'][index] }}</text>
            <text v-else">{{ index + 1 }}</text>
          </view>
          
          <image class="rank-avatar" :src="user.avatar" mode="aspectFill" />
          
          <view class="rank-info"
          >
            <text class="rank-name">{{ user.name }}</text>
            <text class="rank-score">{{ user.score }} 分</text>
          </view>
          
          <view class="rank-trend" :class="user.trend"
          >
            <text>{{ user.trend === 'up' ? '↑' : user.trend === 'down' ? '↓' : '-' }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 成就徽章 -->
    <view class="achievements-section"
    >
      <view class="section-header"
      >
        <text class="section-title">🎖️ 成就徽章</text>
        <text class="section-more">查看全部 ›</text>
      </view>
      
      <view class="achievements-grid"
      >
        <view 
          v-for="(badge, index) in achievements" 
          :key="index"
          class="achievement-item"
          :class="{ unlocked: badge.unlocked }"
          @click="showBadgeDetail(badge)"
        >
          <view class="badge-icon">{{ badge.icon }}</view>
          <text class="badge-name">{{ badge.name }}</text>
          <view v-if="badge.unlocked" class="badge-glow"></text>
        </view>
      </view>
    </view>

    <!-- 底部装饰 -->
    <view style="height: 40px;"></text>
  </view>
</template>

<script setup>
import { ref } from 'vue'

const userPoints = ref(1250)
const rotation = ref(0)
const isSpinning = ref(false)
const currentWheel = ref(0)
const currentRankTab = ref(0)

const wheels = ref([
  { name: '家务分配', icon: '🧹', items: ['洗碗', '拖地', '倒垃圾', '整理房间', '做饭', '免做一次'] },
  { name: '吃什么', icon: '🍽️', items: ['中餐', '西餐', '日料', '火锅', '烧烤', '随便'] },
  { name: '谁买单', icon: '💰', items: ['爸爸', '妈妈', '孩子', 'AA制', '石头剪刀布', '免单'] }
])

const wheelItems = ref([
  { name: '洗碗', color: '#3b82f6', icon: '🍽️' },
  { name: '拖地', color: '#10b981', icon: '🧹' },
  { name: '倒垃圾', color: '#f59e0b', icon: '🗑️' },
  { name: '做饭', color: '#ec4899', icon: '🍳' },
  { name: '整理', color: '#8b5cf6', icon: '📦' },
  { name: '免做', color: '#ef4444', icon: '🎉' }
])

const rankTabs = ref(['积分', '任务', '活跃'])

const rankings = ref([
  { name: '妈妈', avatar: '/static/avatar/mom.png', score: 2580, trend: 'up', isMe: false },
  { name: '爸爸', avatar: '/static/avatar/dad.png', score: 1920, trend: 'same', isMe: false },
  { name: '我', avatar: '/static/avatar/me.png', score: 1250, trend: 'up', isMe: true },
  { name: '宝贝', avatar: '/static/avatar/baby.png', score: 980, trend: 'down', isMe: false }
])

const achievements = ref([
  { name: '任务达人', icon: '🏆', unlocked: true, rarity: 'legendary' },
  { name: '家务能手', icon: '🌟', unlocked: true, rarity: 'epic' },
  { name: '美食家', icon: '🍳', unlocked: true, rarity: 'rare' },
  { name: '早起鸟', icon: '🌅', unlocked: false, rarity: 'rare' },
  { name: '运动健将', icon: '💪', unlocked: false, rarity: 'common' },
  { name: '???', icon: '🔒', unlocked: false, rarity: 'secret' }
])

const selectWheel = (index) => {
  currentWheel.value = index
  // 更新转盘项目
}

const spin = () => {
  if (isSpinning.value) return
  
  isSpinning.value = true
  const spins = 5 + Math.random() * 5
  const degrees = spins * 360 + Math.random() * 360
  
  rotation.value += degrees
  
  setTimeout(() => {
    isSpinning.value = false
    const resultIndex = Math.floor(Math.random() * wheelItems.value.length)
    uni.showModal({
      title: '🎉 恭喜',
      content: `结果是：${wheelItems.value[resultIndex].name}`,
      showCancel: false
    })
  }, 5000)
}

const goPoints = () => {
  uni.navigateTo({ url: '/pages/game/points' })
}

const showBadgeDetail = (badge) => {
  uni.showToast({
    title: badge.unlocked ? badge.name : '未解锁',
    icon: 'none'
  })
}
</script>

<style lang="scss" scoped>
.game-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #0f0f23 0%, #1a1a3e 50%, #0f0f23 100%);
}

.game-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 50px 20px 20px;
  
  .header-title {
    display: flex;
    align-items: center;
    gap: 10px;
    font-size: 24px;
    font-weight: 700;
    color: #fff;
    
    .title-icon {
      font-size: 32px;
    }
  }
  
  .points-badge {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 10px 16px;
    background: linear-gradient(135deg, rgba(245,158,11,0.2) 0%, rgba(217,119,6,0.1) 100%);
    border: 1px solid rgba(245,158,11,0.3);
    border-radius: 20px;
    
    .points-icon {
      font-size: 18px;
    }
    
    .points-value {
      font-size: 16px;
      font-weight: 700;
      color: #fbbf24;
    }
  }
}

.wheel-section {
  padding: 20px;
  
  .section-title {
    font-size: 18px;
    font-weight: 600;
    color: #fff;
    margin-bottom: 20px;
  }
}

.wheel-container {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.wheel-wrapper {
  position: relative;
  width: 280px;
  height: 280px;
  margin-bottom: 30px;
}

.wheel {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  position: relative;
  overflow: hidden;
  transition: transform 5s cubic-bezier(0.17, 0.67, 0.12, 0.99);
  box-shadow: 0 0 60px rgba(99,102,241,0.3);
}

.wheel-item {
  position: absolute;
  width: 50%;
  height: 50%;
  transform-origin: right bottom;
  display: flex;
  align-items: center;
  justify-content: center;
  
  .wheel-item-text {
    transform: rotate(30deg);
    font-size: 14px;
    font-weight: 600;
    color: #fff;
    text-shadow: 0 2px 4px rgba(0,0,0,0.3);
  }
}

.wheel-pointer {
  position: absolute;
  top: -20px;
  left: 50%;
  transform: translateX(-50%);
  font-size: 30px;
  color: #ef4444;
  z-index: 10;
  filter: drop-shadow(0 2px 4px rgba(0,0,0,0.3));
}

.wheel-btn {
  width: 120px;
  height: 48px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: 600;
  color: #fff;
  box-shadow: 0 8px 30px rgba(102,126,234,0.4);
  
  &.spinning {
    opacity: 0.7;
  }
  
  &:active {
    transform: scale(0.95);
  }
}

.wheel-selector {
  margin-top: 20px;
  white-space: nowrap;
  
  .wheel-option {
    display: inline-flex;
    flex-direction: column;
    align-items: center;
    gap: 6px;
    padding: 12px 20px;
    margin-right: 10px;
    background: rgba(255,255,255,0.05);
    border-radius: 16px;
    border: 1px solid rgba(255,255,255,0.08);
    
    &.active {
      background: rgba(99,102,241,0.2);
      border-color: rgba(99,102,241,0.5);
    }
    
    .wheel-option-icon {
      font-size: 24px;
    }
    
    .wheel-option-name {
      font-size: 12px;
      color: #e2e8f0;
    }
  }
}

.ranking-section {
  padding: 0 20px;
  margin-bottom: 20px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  
  .section-title {
    font-size: 17px;
    font-weight: 600;
    color: #fff;
  }
  
  .rank-tabs {
    display: flex;
    gap: 8px;
    padding: 4px;
    background: rgba(255,255,255,0.08);
    border-radius: 10px;
    
    .rank-tab {
      padding: 6px 14px;
      font-size: 12px;
      color: #64748b;
      border-radius: 8px;
      
      &.active {
        background: rgba(99,102,241,0.3);
        color: #fff;
      }
    }
  }
}

.ranking-list {
  background: rgba(255,255,255,0.05);
  border-radius: 20px;
  padding: 16px;
  border: 1px solid rgba(255,255,255,0.08);
}

.rank-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: 12px;
  margin-bottom: 8px;
  
  &.top3 {
    background: rgba(255,255,255,0.05);
  }
  
  &.me {
    background: rgba(99,102,241,0.15);
    border: 1px solid rgba(99,102,241,0.3);
  }
  
  &:last-child {
    margin-bottom: 0;
  }
  
  .rank-number {
    width: 32px;
    text-align: center;
    font-size: 14px;
    font-weight: 600;
    color: #64748b;
    
    .rank-medal {
      font-size: 20px;
    }
  }
  
  .rank-avatar {
    width: 40px;
    height: 40px;
    border-radius: 50%;
  }
  
  .rank-info {
    flex: 1;
    
    .rank-name {
      display: block;
      font-size: 15px;
      font-weight: 500;
      color: #fff;
    }
    
    .rank-score {
      font-size: 13px;
      color: #fbbf24;
      margin-top: 2px;
    }
  }
  
  .rank-trend {
    font-size: 14px;
    font-weight: 600;
    
    &.up { color: #22c55e; }
    &.down { color: #ef4444; }
    &.same { color: #64748b; }
  }
}

.achievements-section {
  padding: 0 20px;
  
  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 15px;
    
    .section-title {
      font-size: 17px;
      font-weight: 600;
      color: #fff;
    }
    
    .section-more {
      font-size: 13px;
      color: #6366f1;
    }
  }
}

.achievements-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.achievement-item {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 20px 10px;
  background: rgba(255,255,255,0.05);
  border-radius: 16px;
  border: 1px solid rgba(255,255,255,0.08);
  
  &:not(.unlocked) {
    opacity: 0.4;
  }
  
  .badge-icon {
    font-size: 36px;
  }
  
  .badge-name {
    font-size: 12px;
    color: #e2e8f0;
  }
  
  .badge-glow {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    width: 60px;
    height: 60px;
    background: radial-gradient(circle, rgba(251,191,36,0.4) 0%, transparent 70%);
    animation: glow 2s ease-in-out infinite;
  }
}

@keyframes glow {
  0%, 100% { opacity: 0.5; transform: translate(-50%, -50%) scale(1); }
  50% { opacity: 1; transform: translate(-50%, -50%) scale(1.1); }
}
</style>
