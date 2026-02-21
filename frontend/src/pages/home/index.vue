<template>
  <view class="home-page">
    <!-- 顶部欢迎区 -->
    <view class="header-section">
      <view class="header-bg"></view>
      <view class="header-content">
        <view class="greeting">
          <text class="time-label">{{ greeting }}</text>
          <text class="user-name">{{ userStore.userInfo?.nickname || '亲爱的用户' }}</text>
        </view>
        
        <view class="family-selector" @click="selectFamily">
          <text class="family-name">{{ currentFamily?.name || '选择家庭' }}</text>
          <u-icon name="arrow-down" size="24" color="#fff"></u-icon>
        </view>
      </view>
    </view>
    
    <!-- 快捷功能入口 -->
    <view class="quick-actions">
      <view 
        v-for="(item, index) in quickActions" 
        :key="index"
        class="action-item"
        @click="navigateTo(item.path)"
      >
        <view class="icon-box" :style="{ background: item.bgColor }">
          <u-icon :name="item.icon" size="40" color="#fff"></u-icon>
        </view>
        <text class="action-name">{{ item.name }}</text>
      </view>
    </view>
    
    <!-- 今日概览 -->
    <view class="section-card">
      <view class="section-header">
        <text class="section-title">📋 今日待办</text>
        <text class="more" @click="navigateTo('/pages/task/index')">更多 ></text>
      </view>
      
      <view v-if="todayTasks.length > 0" class="task-list">
        <view 
          v-for="task in todayTasks" 
          :key="task.id"
          class="task-item"
          @click="goTaskDetail(task)"
        >
          <view class="task-checkbox" @click.stop="toggleTask(task)">
            <view v-if="task.status === 2" class="checked"></view>
            <view v-else class="unchecked"></view>
          </view>
          
          <view class="task-info">
            <text class="task-title" :class="{ completed: task.status === 2 }">
              {{ task.title }}
            </text>
            <text class="task-meta">{{ task.assigneeName }} · {{ task.time }}</text>
          </view>
          
          <view 
            class="task-priority"
            :class="{ high: task.priority === 2, medium: task.priority === 1 }"
          >
          </view>
        </view>
      </view>
      
      <view v-else class="empty-state">
        <u-icon name="checkmark-circle" size="60" color="#ddd"></u-icon>
        <text>今天没有待办事项，享受生活吧！</text>
      </view>
    </view>
    
    <!-- 纪念日提醒 -->
    <view class="section-card" v-if="upcomingAnniversaries.length > 0">
      <view class="section-header">
        <text class="section-title">💝 近期纪念</text>
        <text class="more" @click="navigateTo('/pages/calendar/index')">更多 ></text>
      </view>
      
      <view class="anniversary-list">
        <view 
          v-for="item in upcomingAnniversaries" 
          :key="item.id"
          class="anni-item"
        >
          <view class="anni-icon">{{ item.icon }}</view>
          
          <view class="anni-info">
            <text class="anni-title">{{ item.title }}</text>
            <text class="anni-date">{{ item.date }}</text>
          </view>
          
          <view class="anni-days">
            <text class="days-num">{{ item.days }}</text>
            <text class="days-label">{{ item.days > 0 ? '天后' : '今天' }}</text>
          </view>
        </view>
      </view>
    </view>
    
    <!-- 今日菜谱推荐 -->
    <view class="section-card">
      <view class="section-header">
        <text class="section-title">🍳 今日菜谱</text>
        <text class="more" @click="navigateTo('/pages/recipe/index')">更多 ></text>
      </view>
      
      <view class="recipe-scroll">
        <scroll-view scroll-x class="recipe-list">
          <view 
            v-for="recipe in todayRecipes" 
            :key="recipe.id"
            class="recipe-card"
            @click="goRecipeDetail(recipe)"
          >
            <image :src="recipe.cover" mode="aspectFill" />
            
            <view class="recipe-info">
              <text class="recipe-name">{{ recipe.name }}</text>
              <view class="recipe-meta">
                <text>⏱️ {{ recipe.time }}分钟</text>
                <text>🔥 {{ recipe.calories }}卡</text>
              </view>
            </view>
          </view>
        </scroll-view>
      </view>
    </view>
    
    <!-- 健康数据概览 -->
    <view class="section-card">
      <view class="section-header">
        <text class="section-title">💪 健康概览</text>
        <text class="more" @click="navigateTo('/pages/food/record')">记录 ></text>
      </view>
      
      <view class="health-stats">
        <view class="stat-item">
          <text class="stat-value">{{ healthData.calories }}</text>
          <text class="stat-label">已摄入(卡)</text>
        </view>
        
        <view class="stat-item">
          <text class="stat-value">{{ healthData.target - healthData.calories }}</text>
          <text class="stat-label">剩余(卡)</text>
        </view>
        
        <view class="stat-item">
          <text class="stat-value">{{ healthData.water }}</text>
          <text class="stat-label">饮水(杯)</text>
        </view>
      </view>
      
      <view class="calorie-progress">
        <view class="progress-bar">
          <view 
            class="progress-fill"
            :style="{ width: (healthData.calories / healthData.target * 100) + '%' }"
          ></view>
        </view>
        <text class="progress-text">
          {{ healthData.calories }}/{{ healthData.target }} 卡 ({{ Math.round(healthData.calories / healthData.target * 100) }}%)
        </text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '../../stores/user'

const userStore = useUserStore()

// 问候语
const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '🌙 凌晨好'
  if (hour < 9) return '🌅 早上好'
  if (hour < 12) return '☀️ 上午好'
  if (hour < 14) return '🍱 中午好'
  if (hour < 18) return '🌤️ 下午好'
  return '🌆 晚上好'
})

const currentFamily = ref({ name: '幸福小家' })

// 快捷功能
const quickActions = [
  { name: '添加任务', icon: 'file-text', bgColor: '#5B8FF9', path: '/pages/task/create' },
  { name: '记录饮食', icon: 'photo', bgColor: '#5AD8A6', path: '/pages/food/record' },
  { name: '喝水打卡', icon: 'minus-circle', bgColor: '#2E6AD8', path: '/pages/water/index' },
  { name: 'AI助手', icon: 'robot', bgColor: '#9B59B6', path: '/pages/ai/index' },
  { name: '数据看板', icon: 'bar-chart', bgColor: '#3498DB', path: '/pages/dashboard/index' }
]

// 今日任务
const todayTasks = ref([
  { id: 1, title: '买牛奶和鸡蛋', assigneeName: '妈妈', time: '今天 18:00', priority: 1, status: 0 },
  { id: 2, title: '给孩子检查作业', assigneeName: '爸爸', time: '今天 20:00', priority: 2, status: 0 },
  { id: 3, title: '周末出游规划', assigneeName: '全家', time: '今天 21:00', priority: 0, status: 2 }
])

// 纪念日
const upcomingAnniversaries = ref([
  { id: 1, title: '结婚纪念日', date: '12月25日', days: 5, icon: '💒' },
  { id: 2, title: '宝贝生日', date: '1月8日', days: 19, icon: '🎂' }
])

// 今日菜谱
const todayRecipes = ref([
  { id: 1, name: '番茄炒蛋', cover: '/static/recipes/fanqie.jpg', time: 15, calories: 180 },
  { id: 2, name: '红烧排骨', cover: '/static/recipes/paigu.jpg', time: 45, calories: 320 },
  { id: 3, name: '蒜蓉西兰花', cover: '/static/recipes/xilanhua.jpg', time: 10, calories: 85 }
])

// 健康数据
const healthData = ref({
  calories: 1250,
  target: 2000,
  water: 5
})

const navigateTo = (path) => {
  uni.navigateTo({ url: path })
}

const selectFamily = () => {
  uni.navigateTo({ url: '/pages/family/switch' })
}

const toggleTask = (task) => {
  task.status = task.status === 2 ? 0 : 2
}

const goTaskDetail = (task) => {
  uni.navigateTo({ url: `/pages/task/detail?id=${task.id}` })
}

const goRecipeDetail = (recipe) => {
  uni.navigateTo({ url: `/pages/recipe/detail?id=${recipe.id}` })
}

onMounted(() => {
  // 加载首页数据
})
</script>

<style lang="scss" scoped>
.home-page {
  min-height: 100vh;
  background: #f5f6fa;
  padding-bottom: 120rpx;
}

.header-section {
  position: relative;
  padding: 40rpx;
  padding-top: 80rpx;
  overflow: hidden;
  
  .header-bg {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 400rpx;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 0 0 40rpx 40rpx;
  }
  
  .header-content {
    position: relative;
    z-index: 1;
  }
  
  .greeting {
    margin-bottom: 20rpx;
    
    .time-label {
      display: block;
      font-size: 28rpx;
      color: rgba(255,255,255,0.8);
      margin-bottom: 8rpx;
    }
    
    .user-name {
      font-size: 44rpx;
      font-weight: bold;
      color: #fff;
    }
  }
  
  .family-selector {
    display: inline-flex;
    align-items: center;
    background: rgba(255,255,255,0.2);
    padding: 12rpx 24rpx;
    border-radius: 30rpx;
    
    .family-name {
      font-size: 26rpx;
      color: #fff;
      margin-right: 8rpx;
    }
  }
}

.quick-actions {
  display: flex;
  justify-content: space-around;
  padding: 30rpx 20rpx;
  margin: 0 30rpx;
  background: #fff;
  border-radius: 20rpx;
  margin-top: -60rpx;
  position: relative;
  box-shadow: 0 4rpx 20rpx rgba(0,0,0,0.05);
  
  .action-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    
    .icon-box {
      width: 100rpx;
      height: 100rpx;
      border-radius: 24rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-bottom: 16rpx;
    }
    
    .action-name {
      font-size: 24rpx;
      color: #666;
    }
  }
}

.section-card {
  background: #fff;
  border-radius: 20rpx;
  margin: 30rpx;
  padding: 30rpx;
  
  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24rpx;
    
    .section-title {
      font-size: 32rpx;
      font-weight: 600;
      color: #333;
    }
    
    .more {
      font-size: 26rpx;
      color: #999;
    }
  }
}

.task-list {
  .task-item {
    display: flex;
    align-items: center;
    padding: 20rpx 0;
    border-bottom: 2rpx solid #f5f5f5;
    
    &:last-child {
      border-bottom: none;
    }
    
    .task-checkbox {
      margin-right: 20rpx;
      
      .unchecked {
        width: 40rpx;
        height: 40rpx;
        border: 2rpx solid #ddd;
        border-radius: 50%;
      }
      
      .checked {
        width: 40rpx;
        height: 40rpx;
        background: #5AD8A6;
        border-radius: 50%;
        position: relative;
        
        &::after {
          content: '✓';
          color: #fff;
          font-size: 24rpx;
          position: absolute;
          top: 50%;
          left: 50%;
          transform: translate(-50%, -50%);
        }
      }
    }
    
    .task-info {
      flex: 1;
      
      .task-title {
        font-size: 28rpx;
        color: #333;
        display: block;
        margin-bottom: 8rpx;
        
        &.completed {
          text-decoration: line-through;
          color: #999;
        }
      }
      
      .task-meta {
        font-size: 24rpx;
        color: #999;
      }
    }
    
    .task-priority {
      width: 8rpx;
      height: 8rpx;
      border-radius: 50%;
      background: #ddd;
      
      &.high {
        background: #ff4d4f;
      }
      
      &.medium {
        background: #faad14;
      }
    }
  }
}

.empty-state {
  text-align: center;
  padding: 60rpx 0;
  
  text {
    display: block;
    margin-top: 20rpx;
    font-size: 26rpx;
    color: #999;
  }
}

.anniversary-list {
  .anni-item {
    display: flex;
    align-items: center;
    padding: 20rpx 0;
    border-bottom: 2rpx solid #f5f5f5;
    
    &:last-child {
      border-bottom: none;
    }
    
    .anni-icon {
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
    
    .anni-info {
      flex: 1;
      
      .anni-title {
        font-size: 28rpx;
        color: #333;
        display: block;
        margin-bottom: 8rpx;
      }
      
      .anni-date {
        font-size: 24rpx;
        color: #999;
      }
    }
    
    .anni-days {
      text-align: right;
      
      .days-num {
        font-size: 36rpx;
        font-weight: bold;
        color: #E8684A;
        display: block;
      }
      
      .days-label {
        font-size: 22rpx;
        color: #999;
      }
    }
  }
}

.recipe-scroll {
  .recipe-list {
    white-space: nowrap;
    
    .recipe-card {
      display: inline-block;
      width: 280rpx;
      margin-right: 20rpx;
      border-radius: 16rpx;
      overflow: hidden;
      background: #f9f9f9;
      
      image {
        width: 100%;
        height: 180rpx;
      }
      
      .recipe-info {
        padding: 16rpx;
        
        .recipe-name {
          font-size: 28rpx;
          color: #333;
          display: block;
          margin-bottom: 12rpx;
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
        }
        
        .recipe-meta {
          display: flex;
          justify-content: space-between;
          
          text {
            font-size: 22rpx;
            color: #999;
          }
        }
      }
    }
  }
}

.health-stats {
  display: flex;
  justify-content: space-around;
  padding: 20rpx 0;
  
  .stat-item {
    text-align: center;
    
    .stat-value {
      font-size: 40rpx;
      font-weight: bold;
      color: #5B8FF9;
      display: block;
    }
    
    .stat-label {
      font-size: 24rpx;
      color: #999;
      margin-top: 8rpx;
    }
  }
}

.calorie-progress {
  margin-top: 20rpx;
  
  .progress-bar {
    height: 16rpx;
    background: #f0f0f0;
    border-radius: 8rpx;
    overflow: hidden;
    
    .progress-fill {
      height: 100%;
      background: linear-gradient(90deg, #5B8FF9, #5AD8A6);
      border-radius: 8rpx;
      transition: width 0.3s;
    }
  }
  
  .progress-text {
    display: block;
    text-align: center;
    font-size: 24rpx;
    color: #666;
    margin-top: 12rpx;
  }
}
</style>
