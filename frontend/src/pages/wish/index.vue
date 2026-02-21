<template>
  <view class="wish-page">
    <!-- 顶部统计 -->
    <view class="wish-header">
      <view class="header-bg"></view>
      
      <view class="wish-stats">
        <view class="stat-item">
          <text class="stat-num">{{ stats.total }}</text>
          <text class="stat-label">总心愿</text>
        </view>
        
        <view class="stat-item">
          <text class="stat-num">{{ stats.completed }}</text>
          <text class="stat-label">已实现</text>
        </view>
        
        <view class="stat-item">
          <text class="stat-num">{{ stats.inProgress }}</text>
          <text class="stat-label">进行中</text>
        </view>
      </view>
    </view>
    
    <!-- 心愿类型标签 -->
    <view class="type-tabs">
      <scroll-view scroll-x class="tabs-scroll">
        <view 
          v-for="(type, index) in wishTypes" 
          :key="index"
          class="tab-item"
          :class="{ active: currentType === type.key }"
          @click="currentType = type.key"
        >
          <text class="tab-icon">{{ type.icon }}</text>
          <text class="tab-name">{{ type.name }}</text>
        </view>
      </scroll-view>
    </view>
    
    <!-- 心愿墙 -->
    <view class="wish-wall">
      <view class="wall-header">
        <text class="wall-title">💫 心愿墙</text>
        
        <view class="filter-btns"
003e
          <text 
            class="filter-btn" 
            :class="{ active: filter === 'all' }"
            @click="filter = 'all'"
          >全部</text>
          
          <text 
            class="filter-btn" 
            :class="{ active: filter === 'mine' }"
            @click="filter = 'mine'"
          >我的</text>
          
          <text 
            class="filter-btn" 
            :class="{ active: filter === 'family' }"
            @click="filter = 'family'"
          >家庭</text>
        </view>
      </view>
      
      <view class="wish-grid">
        <view 
          v-for="wish in filteredWishes" 
          :key="wish.id"
          class="wish-card"
          :class="{ completed: wish.status === 2 }"
          @click="goDetail(wish)"
        >
          <view class="wish-image">
            <image :src="wish.image || '/static/wish-default.png'" mode="aspectFill" />
            
            <view class="wish-type-tag">{{ getTypeName(wish.type) }}</view>
            
            <view v-if="wish.status === 2" class="completed-badge">已实现</view>
          </view>
          
          <view class="wish-content">
            <text class="wish-title">{{ wish.title }}</text>
            
            <view class="wish-progress" v-if="wish.status === 1">
              <view class="progress-bar"
                >view class="progress-fill" :style="{ width: wish.progress + '%' }"></view>
              </view>
              <text class="progress-text">{{ wish.progress }}%</text>
            </view>
            
            <view class="wish-footer">
              <view class="wish-user"
                <image :src="wish.userAvatar" />
                <text>{{ wish.userName }}</text>
              </view>
              
              <view v-if="wish.claimantId" class="claimant"
                <u-icon name="heart-fill" size="20" color="#ff4d4f"></u-icon>
                <text>已认领</text>
              </view>
            </view>
          </view>
        </view>
      </view>
    </view>
    
    <!-- 家庭目标 -->
    <view class="family-goals">
      <view class="section-title">🎯 家庭共同目标</text>
      
      <view 
        v-for="goal in familyGoals" 
        :key="goal.id"
        class="goal-card"
      >
        <view class="goal-icon">{{ goal.icon }}</view>
        
        <view class="goal-info"
          <text class="goal-title">{{ goal.title }}</text>
          
          <view class="goal-progress"
            <view class="progress-bar"
              <view class="progress-fill" :style="{ width: goal.progress + '%' }"></view>
            </view>
            
            <text class="progress-amount">{{ goal.current }}/{{ goal.target }} {{ goal.unit }}</text>
          </view>
        </view>
        
        <text class="goal-days">还剩{{ goal.remainingDays }}天</text>
      </view>
    </view>
    
    <!-- 添加按钮 -->
    <view class="fab-btn" @click="createWish"
      <u-icon name="plus" size="48" color="#fff"></u-icon>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'

const currentType = ref('all')
const filter = ref('all')

const stats = ref({ total: 12, completed: 5, inProgress: 3 })

const wishTypes = [
  { key: 'all', name: '全部', icon: '✨' },
  { key: 'item', name: '物品', icon: '🎁' },
  { key: 'experience', name: '体验', icon: '🎈' },
  { key: 'learn', name: '学习', icon: '📚' },
  { key: 'relation', name: '关系', icon: '💕' },
  { key: 'goal', name: '目标', icon: '🎯' }
]

const wishes = ref([
  {
    id: 1,
    title: '去日本看樱花',
    type: 'experience',
    status: 1,
    progress: 60,
    image: '/static/wish/sakura.jpg',
    userName: '妈妈',
    userAvatar: '/static/avatar/mom.jpg',
    claimantId: null
  },
  {
    id: 2,
    title: '买一台咖啡机',
    type: 'item',
    status: 2,
    progress: 100,
    image: '/static/wish/coffee.jpg',
    userName: '爸爸',
    userAvatar: '/static/avatar/dad.jpg',
    claimantId: 2
  },
  {
    id: 3,
    title: '学会弹吉他',
    type: 'learn',
    status: 1,
    progress: 30,
    image: '/static/wish/guitar.jpg',
    userName: '宝贝',
    userAvatar: '/static/avatar/baby.jpg',
    claimantId: null
  },
  {
    id: 4,
    title: '全家去海边度假',
    type: 'relation',
    status: 0,
    progress: 0,
    image: '/static/wish/beach.jpg',
    userName: '爷爷',
    userAvatar: '/static/avatar/grandpa.jpg',
    claimantId: null
  }
])

const familyGoals = ref([
  {
    id: 1,
    title: '2025年家庭旅行基金',
    icon: '✈️',
    current: 15000,
    target: 30000,
    unit: '元',
    progress: 50,
    remainingDays: 180
  },
  {
    id: 2,
    title: '全家健身打卡',
    icon: '💪',
    current: 45,
    target: 100,
    unit: '次',
    progress: 45,
    remainingDays: 60
  }
])

const filteredWishes = computed(() => {
  let result = wishes.value
  if (currentType.value !== 'all') {
    result = result.filter(w => w.type === currentType.value)
  }
  return result
})

const getTypeName = (type) => {
  const map = { item: '物品', experience: '体验', learn: '学习', relation: '关系', goal: '目标' }
  return map[type] || '其他'
}

const goDetail = (wish) => {
  uni.navigateTo({ url: `/pages/wish/detail?id=${wish.id}` })
}

const createWish = () => {
  uni.navigateTo({ url: '/pages/wish/create' })
}
</script>

<style lang="scss" scoped>
.wish-page {
  min-height: 100vh;
  background: #f5f6fa;
  padding-bottom: 120rpx;
}

.wish-header {
  position: relative;
  padding: 40rpx;
  padding-top: 60rpx;
  
  .header-bg {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 280rpx;
    background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
    border-radius: 0 0 40rpx 40rpx;
  }
  
  .wish-stats {
    position: relative;
    display: flex;
    justify-content: space-around;
    background: #fff;
    border-radius: 20rpx;
    padding: 30rpx;
    box-shadow: 0 4rpx 20rpx rgba(0,0,0,0.08);
    
    .stat-item {
      text-align: center;
      
      .stat-num {
        font-size: 48rpx;
        font-weight: bold;
        color: #f5576c;
        display: block;
      }
      
      .stat-label {
        font-size: 24rpx;
        color: #999;
        margin-top: 8rpx;
      }
    }
  }
}

.type-tabs {
  margin: 30rpx 0;
  
  .tabs-scroll {
    white-space: nowrap;
    padding: 0 20rpx;
    
    .tab-item {
      display: inline-flex;
      align-items: center;
      padding: 16rpx 32rpx;
      margin-right: 16rpx;
      background: #fff;
      border-radius: 32rpx;
      
      &.active {
        background: #f5576c;
        
        .tab-name {
          color: #fff;
        }
      }
      
      .tab-icon {
        margin-right: 8rpx;
      }
      
      .tab-name {
        font-size: 26rpx;
        color: #666;
      }
    }
  }
}

.wish-wall {
  padding: 0 30rpx;
  
  .wall-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24rpx;
    
    .wall-title {
      font-size: 32rpx;
      font-weight: 600;
      color: #333;
    }
    
    .filter-btns {
      display: flex;
      background: #fff;
      border-radius: 30rpx;
      padding: 4rpx;
      
      .filter-btn {
        padding: 12rpx 24rpx;
        font-size: 24rpx;
        color: #666;
        border-radius: 26rpx;
        
        &.active {
          background: #f5576c;
          color: #fff;
        }
      }
    }
  }
  
  .wish-grid {
    display: flex;
    flex-wrap: wrap;
    gap: 20rpx;
    
    .wish-card {
      width: calc(50% - 10rpx);
      background: #fff;
      border-radius: 16rpx;
      overflow: hidden;
      box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.04);
      
      &.completed {
        opacity: 0.8;
      }
      
      .wish-image {
        position: relative;
        height: 240rpx;
        
        image {
          width: 100%;
          height: 100%;
        }
        
        .wish-type-tag {
          position: absolute;
          top: 12rpx;
          left: 12rpx;
          padding: 6rpx 16rpx;
          background: rgba(0,0,0,0.5);
          color: #fff;
          font-size: 22rpx;
          border-radius: 8rpx;
        }
        
        .completed-badge {
          position: absolute;
          top: 12rpx;
          right: 12rpx;
          padding: 6rpx 16rpx;
          background: #5AD8A6;
          color: #fff;
          font-size: 22rpx;
          border-radius: 8rpx;
        }
      }
      
      .wish-content {
        padding: 20rpx;
        
        .wish-title {
          font-size: 28rpx;
          color: #333;
          font-weight: 500;
          display: block;
          margin-bottom: 16rpx;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
        
        .wish-progress {
          margin-bottom: 16rpx;
          
          .progress-bar {
            height: 8rpx;
            background: #f0f0f0;
            border-radius: 4rpx;
            overflow: hidden;
            margin-bottom: 8rpx;
            
            .progress-fill {
              height: 100%;
              background: linear-gradient(90deg, #f093fb, #f5576c);
              border-radius: 4rpx;
            }
          }
          
          .progress-text {
            font-size: 22rpx;
            color: #f5576c;
          }
        }
        
        .wish-footer {
          display: flex;
          justify-content: space-between;
          align-items: center;
          
          .wish-user {
            display: flex;
            align-items: center;
            
            image {
              width: 36rpx;
              height: 36rpx;
              border-radius: 50%;
              margin-right: 8rpx;
            }
            
            text {
              font-size: 22rpx;
              color: #999;
            }
          }
          
          .claimant {
            display: flex;
            align-items: center;
            
            text {
              margin-left: 4rpx;
              font-size: 22rpx;
              color: #ff4d4f;
            }
          }
        }
      }
    }
  }
}

.family-goals {
  margin: 30rpx;
  padding: 30rpx;
  background: #fff;
  border-radius: 20rpx;
  
  .section-title {
    font-size: 32rpx;
    font-weight: 600;
    color: #333;
    display: block;
    margin-bottom: 24rpx;
  }
  
  .goal-card {
    display: flex;
    align-items: center;
    padding: 24rpx;
    background: #f9f9f9;
    border-radius: 16rpx;
    margin-bottom: 20rpx;
    
    &:last-child {
      margin-bottom: 0;
    }
    
    .goal-icon {
      width: 80rpx;
      height: 80rpx;
      background: #FFF0F6;
      border-radius: 20rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 40rpx;
      margin-right: 20rpx;
    }
    
    .goal-info {
      flex: 1;
      
      .goal-title {
        font-size: 28rpx;
        color: #333;
        font-weight: 500;
        display: block;
        margin-bottom: 12rpx;
      }
      
      .goal-progress {
        .progress-bar {
          height: 12rpx;
          background: #f0f0f0;
          border-radius: 6rpx;
          overflow: hidden;
          margin-bottom: 8rpx;
          
          .progress-fill {
            height: 100%;
            background: linear-gradient(90deg, #f093fb, #f5576c);
            border-radius: 6rpx;
          }
        }
        
        .progress-amount {
          font-size: 24rpx;
          color: #f5576c;
        }
      }
    }
    
    .goal-days {
      font-size: 24rpx;
      color: #999;
      margin-left: 20rpx;
    }
  }
}

.fab-btn {
  position: fixed;
  right: 40rpx;
  bottom: 160rpx;
  width: 100rpx;
  height: 100rpx;
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8rpx 24rpx rgba(245, 87, 108, 0.4);
  z-index: 100;
}
</style>
