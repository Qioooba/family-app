<template>
  <view class="challenges-page">
    <!-- 自定义导航栏 -->
    <view class="custom-nav">
      <view class="nav-back" @click="goBack">
        <text class="icon">‹</text>
      </view>
      <view class="nav-title">家庭挑战</view>
      <view class="nav-action" @click="showCreateModal">
        <text>+ 创建</text>
      </view>
    </view>

    <!-- 我的成就 -->
    <view class="achievement-section">
      <view class="achievement-card">
        <view class="user-avatar">
          <text>👤</text>
        </view>
        <view class="achievement-info">
          <text class="user-name">我的成就</text>
          <view class="achievement-stats">
            <text>已完成 {{ completedChallenges }} 个挑战</text>
            <text class="divider">|</text>
            <text>连续打卡 {{ streakDays }} 天</text>
          </view>
        </view>
        <view class="rank-badge">
          <text class="rank-icon">🏆</text>
          <text class="rank-text">第{{ myRank }}名</text>
        </view>
      </view>
    </view>

    <!-- 挑战类型筛选 -->
    <view class="filter-tabs">
      <view 
        v-for="tab in filterTabs" 
        :key="tab.value"
        class="tab-item"
        :class="{ active: currentTab === tab.value }"
        @click="currentTab = tab.value"
      >
        <text>{{ tab.label }}</text>
      </view>
    </view>

    <!-- 挑战列表 -->
    <scroll-view class="challenge-list" scroll-y @scrolltolower="loadMore">
      <view 
        v-for="challenge in filteredChallenges" 
        :key="challenge.id"
        class="challenge-card"
        :class="challenge.status"
      >
        <view class="challenge-header">
          <view class="challenge-icon">
            <text>{{ challenge.icon }}</text>
          </view>
          <view class="challenge-info">
            <text class="challenge-name">{{ challenge.name }}</text>
            <text class="challenge-desc">{{ challenge.description }}</text>
          </view>
          <view class="challenge-reward">
            <text class="reward-icon">💎</text>
            <text class="reward-value">+{{ challenge.reward }}</text>
          </view>
        </view>

        <view class="challenge-progress">
          <view class="progress-info">
            <text class="progress-text">进度 {{ challenge.current }}/{{ challenge.target }}</text>
            <text class="progress-percent">{{ Math.round(challenge.current / challenge.target * 100) }}%</text>
          </view>
          <view class="progress-bar">
            <view 
              class="progress-fill" 
              :style="{ width: (challenge.current / challenge.target * 100) + '%' }"
            ></text>
          </view>
        </view>

        <view class="challenge-footer">
          <view class="participants">
            <text v-for="(user, idx) in challenge.participants.slice(0, 3)" :key="idx" class="participant-avatar">
              {{ user.avatar }}
            </text>
            <text v-if="challenge.participants.length > 3" class="more-count">+{{ challenge.participants.length - 3 }}</text>
          </view>
          
          <button 
            v-if="challenge.status === 'active'" 
            class="checkin-btn"
            :class="{ disabled: challenge.isCheckedIn }"
            :disabled="challenge.isCheckedIn"
            @click="checkIn(challenge)"
          >
            {{ challenge.isCheckedIn ? '已打卡' : '打卡' }}
          </button>
          
          <view v-if="challenge.status === 'completed'" class="completed-badge">
            <text>✓ 已完成</text>
          </view>
        </view>
      </view>

      <!-- 排行榜入口 -->
      <view class="ranking-entry" @click="goToRanking">
        <text class="icon">🏆</text>
        <view class="entry-info">
          <text class="entry-title">家庭排行榜</text>
          <text class="entry-desc">查看家庭成员挑战排名</text>
        </view>
        <text class="arrow">›</text>
      </view>

      <view class="loading-more" v-if="loading">
        <text>加载中...</text>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'

const loading = ref(false)
const currentTab = ref('active')
const completedChallenges = ref(12)
const streakDays = ref(7)
const myRank = ref(2)

const filterTabs = ref([
  { label: '进行中', value: 'active' },
  { label: '已完成', value: 'completed' },
  { label: '全部', value: 'all' }
])

const challenges = ref([
  {
    id: 1,
    name: '每日运动挑战',
    description: '每天运动30分钟以上，连续坚持7天',
    icon: '🏃',
    reward: 50,
    current: 5,
    target: 7,
    status: 'active',
    isCheckedIn: true,
    participants: [
      { name: '爸爸', avatar: '👨' },
      { name: '妈妈', avatar: '👩' },
      { name: '宝贝', avatar: '👶' }
    ]
  },
  {
    id: 2,
    name: '早餐打卡',
    description: '每天吃健康早餐，拍照打卡',
    icon: '🍳',
    reward: 30,
    current: 3,
    target: 7,
    status: 'active',
    isCheckedIn: false,
    participants: [
      { name: '妈妈', avatar: '👩' },
      { name: '宝贝', avatar: '👶' }
    ]
  },
  {
    id: 3,
    name: '阅读挑战',
    description: '每天阅读30分钟，累计21天',
    icon: '📚',
    reward: 100,
    current: 18,
    target: 21,
    status: 'active',
    isCheckedIn: false,
    participants: [
      { name: '爸爸', avatar: '👨' },
      { name: '宝贝', avatar: '👶' }
    ]
  },
  {
    id: 4,
    name: '早睡挑战',
    description: '每晚10点前睡觉，连续14天',
    icon: '😴',
    reward: 80,
    current: 14,
    target: 14,
    status: 'completed',
    isCheckedIn: true,
    participants: [
      { name: '妈妈', avatar: '👩' }
    ]
  },
  {
    id: 5,
    name: '家务小能手',
    description: '每天完成一项家务任务',
    icon: '🧹',
    reward: 40,
    current: 30,
    target: 30,
    status: 'completed',
    isCheckedIn: true,
    participants: [
      { name: '爸爸', avatar: '👨' },
      { name: '妈妈', avatar: '👩' },
      { name: '宝贝', avatar: '👶' }
    ]
  }
])

const filteredChallenges = computed(() => {
  if (currentTab.value === 'all') return challenges.value
  return challenges.value.filter(c => c.status === currentTab.value)
})

const goBack = () => {
  uni.navigateBack()
}

const showCreateModal = () => {
  uni.showToast({ title: '创建挑战功能', icon: 'none' })
}

const checkIn = (challenge) => {
  challenge.isCheckedIn = true
  challenge.current++
  if (challenge.current >= challenge.target) {
    challenge.status = 'completed'
  }
  uni.showToast({ 
    title: `打卡成功! +${challenge.reward}积分`, 
    icon: 'success' 
  })
}

const goToRanking = () => {
  uni.showToast({ title: '查看排行榜', icon: 'none' })
}

const loadMore = () => {
  loading.value = true
  setTimeout(() => {
    loading.value = false
  }, 500)
}
</script>

<style lang="scss" scoped>
.challenges-page {
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
    padding: 8px 16px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 8px;
    font-size: 13px;
    color: #fff;
  }
}

.achievement-section {
  padding: 15px;

  .achievement-card {
    display: flex;
    align-items: center;
    gap: 15px;
    padding: 20px;
    background: linear-gradient(135deg, rgba(245,158,11,0.2) 0%, rgba(217,119,6,0.1) 100%);
    border-radius: 16px;
    border: 1px solid rgba(245,158,11,0.2);

    .user-avatar {
      width: 50px;
      height: 50px;
      background: rgba(245,158,11,0.2);
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 24px;
    }

    .achievement-info {
      flex: 1;

      .user-name {
        display: block;
        font-size: 16px;
        font-weight: 600;
        color: #fff;
        margin-bottom: 6px;
      }

      .achievement-stats {
        display: flex;
        align-items: center;
        gap: 10px;
        font-size: 13px;
        color: #94a3b8;

        .divider {
          color: rgba(255,255,255,0.2);
        }
      }
    }

    .rank-badge {
      text-align: center;

      .rank-icon {
        display: block;
        font-size: 28px;
        margin-bottom: 2px;
      }

      .rank-text {
        font-size: 12px;
        color: #f59e0b;
      }
    }
  }
}

.filter-tabs {
  display: flex;
  padding: 0 15px 15px;
  gap: 10px;

  .tab-item {
    flex: 1;
    padding: 12px;
    background: rgba(255,255,255,0.05);
    border-radius: 10px;
    text-align: center;
    font-size: 14px;
    color: #64748b;
    transition: all 0.3s;

    &.active {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: #fff;
    }
  }
}

.challenge-list {
  padding: 0 15px 30px;

  .challenge-card {
    padding: 16px;
    background: rgba(255,255,255,0.05);
    border-radius: 16px;
    margin-bottom: 12px;

    &.completed {
      opacity: 0.7;
      background: rgba(34,197,94,0.05);
    }

    .challenge-header {
      display: flex;
      align-items: center;
      gap: 12px;
      margin-bottom: 15px;

      .challenge-icon {
        width: 48px;
        height: 48px;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        border-radius: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 24px;
      }

      .challenge-info {
        flex: 1;

        .challenge-name {
          display: block;
          font-size: 16px;
          font-weight: 600;
          color: #fff;
          margin-bottom: 4px;
        }

        .challenge-desc {
          font-size: 12px;
          color: #64748b;
        }
      }

      .challenge-reward {
        display: flex;
        align-items: center;
        gap: 4px;
        padding: 6px 12px;
        background: rgba(245,158,11,0.2);
        border-radius: 8px;

        .reward-icon {
          font-size: 14px;
        }

        .reward-value {
          font-size: 14px;
          font-weight: 600;
          color: #f59e0b;
        }
      }
    }

    .challenge-progress {
      margin-bottom: 15px;

      .progress-info {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 8px;

        .progress-text {
          font-size: 13px;
          color: #94a3b8;
        }

        .progress-percent {
          font-size: 14px;
          font-weight: 600;
          color: #667eea;
        }
      }

      .progress-bar {
        height: 8px;
        background: rgba(255,255,255,0.1);
        border-radius: 4px;
        overflow: hidden;

        .progress-fill {
          height: 100%;
          background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
          border-radius: 4px;
          transition: width 0.3s;
        }
      }
    }

    .challenge-footer {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding-top: 12px;
      border-top: 1px solid rgba(255,255,255,0.05);

      .participants {
        display: flex;
        align-items: center;

        .participant-avatar {
          width: 28px;
          height: 28px;
          background: rgba(255,255,255,0.1);
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 14px;
          margin-left: -8px;
          border: 2px solid #0f0f23;

          &:first-child {
            margin-left: 0;
          }
        }

        .more-count {
          margin-left: 6px;
          font-size: 12px;
          color: #64748b;
        }
      }

      .checkin-btn {
        padding: 8px 20px;
        background: linear-gradient(135deg, #22c55e 0%, #16a34a 100%);
        border-radius: 8px;
        font-size: 13px;
        color: #fff;
        border: none;

        &.disabled {
          background: rgba(255,255,255,0.1);
          color: #64748b;
        }

        &::after {
          border: none;
        }
      }

      .completed-badge {
        padding: 6px 12px;
        background: rgba(34,197,94,0.2);
        border-radius: 8px;
        font-size: 12px;
        color: #22c55e;
      }
    }
  }

  .ranking-entry {
    display: flex;
    align-items: center;
    gap: 15px;
    padding: 20px;
    margin-top: 10px;
    background: linear-gradient(135deg, rgba(245,158,11,0.1) 0%, rgba(217,119,6,0.05) 100%);
    border-radius: 16px;
    border: 1px solid rgba(245,158,11,0.2);

    .icon {
      font-size: 32px;
    }

    .entry-info {
      flex: 1;

      .entry-title {
        display: block;
        font-size: 15px;
        font-weight: 600;
        color: #fff;
        margin-bottom: 4px;
      }

      .entry-desc {
        font-size: 12px;
        color: #64748b;
      }
    }

    .arrow {
      font-size: 20px;
      color: #f59e0b;
    }
  }
}

.loading-more {
  text-align: center;
  padding: 20px;
  color: #64748b;
  font-size: 13px;
}
</style>
