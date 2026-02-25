<template>
  <view class="home-page">
    <PullRefresh2
      ref="pullRefreshRef"
      :enabled="true"
      :threshold="80"
      :haptic-enabled="true"
      @refresh="onRefresh"
    >
      <!-- 骨架屏 -->
      <Skeleton v-if="pageLoading" type="card" :rows="5" show-image :list-count="3" />
    
    <!-- 实际内容 -->
      <!-- 顶部欢迎区 -->
    <view class="header-section">
      <view class="header-bg">
        <view class="bg-pattern"></view>
      </view>
      <view class="header-content">
        <view class="header-left">
          <view class="greeting">
            <text class="time-label">{{ greeting }}</text>
            <text class="user-name">{{ userStore.userInfo?.nickname || '亲爱的用户' }}</text>
          </view>
          <text class="current-date">{{ currentDate }}</text>
        </view>
        
        <view class="header-right">
          <view class="family-selector" @click="selectFamily">
            <text class="family-name">{{ currentFamily?.name || '幸福小家' }}</text>
            <view class="selector-arrow">
              <text class="arrow-icon">▼</text>
            </view>
          </view>
        </view>
      </view>
    </view>
    
    <!-- 快捷功能入口 -->
    <view class="quick-actions">
      <view 
        v-for="(item, index) in quickActions" 
        :key="index"
        class="action-item"
        :class="`item-${index}`"
        @click="navigateTo(item.path)"
        :style="{ animationDelay: `${index * 0.05}s` }"
      >
        <view class="icon-box" :style="{ background: item.bgColor, boxShadow: item.shadow }">
          <text class="icon-emoji">{{ item.icon }}</text>
        </view>
        <text class="action-name">{{ item.name }}</text>
      </view>
    </view>
    
    <!-- 今日概览卡片 -->
    <view class="overview-cards animate-in">
      <!-- 今日喝水 -->
      <view class="overview-card water-card" @click="navigateTo('/pages/water/index')">
        <view class="card-header">
          <view class="card-icon-wrapper water-icon">
            <text class="card-icon">💧</text>
          </view>
          <text class="card-title">今日喝水</text>
        </view>
        <view class="water-progress-section">
          <view class="water-progress-bar">
            <view 
              class="water-progress-fill"
              :style="{ width: (overviewData.water / overviewData.waterTarget * 100) + '%' }"
            ></view>
          </view>
          <text class="water-text">{{ overviewData.water }}ml / {{ overviewData.waterTarget }}ml</text>
        </view>
      </view>
      
      <!-- 今日饮食 -->
      <view class="overview-card diet-card" @click="navigateTo('/pages/food/record')">
        <view class="card-header">
          <view class="card-icon-wrapper diet-icon">
            <text class="card-icon">🍽️</text>
          </view>
          <text class="card-title">今日饮食</text>
        </view>
        <view class="card-value">
          <text class="value-num">{{ overviewData.calories }}</text>
          <text class="value-unit">千卡</text>
        </view>
      </view>
    </view>
    
    <!-- 今日概览 -->
    <view class="section-card animate-in">
      <view class="section-header">
        <view class="title-wrapper">
          <text class="section-icon">📋</text>
          <text class="section-title">今日待办</text>
        </view>
        <view class="more-btn" @click="navigateTo('/pages/task/index')">
          <text>更多</text>
          ›
        </view>
      </view>
      
      <view v-if="todayTasks.length > 0" class="task-list">
        <view 
          v-for="(task, index) in todayTasks" 
          :key="task.id"
          class="task-item"
          :class="{ completed: task.status === 2 }"
          :style="{ animationDelay: `${index * 0.08}s` }"
          @click="goTaskDetail(task)"
        >
          <view class="task-checkbox" @click.stop="toggleTask(task)">
            <view v-if="task.status === 2" class="checked">
              ✓
            </view>
            <view v-else class="unchecked"></view>
          </view>
          
          <view class="task-info">
            <text class="task-title" :class="{ completed: task.status === 2 }">
              {{ task.title }}
            </text>
            <text class="task-meta">
              <text class="assignee">{{ task.assigneeName }}</text>
              <text class="divider">·</text>
              <text class="time">{{ task.time }}</text>
            </text>
          </view>
          
          <view 
            class="task-priority"
            :class="{ high: task.priority === 2, medium: task.priority === 1, low: task.priority === 0 }"
          >
            <text v-if="task.priority === 2">紧急</text>
            <text v-else-if="task.priority === 1">重要</text>
            <text v-else>普通</text>
          </view>
        </view>
      </view>
      
      <view v-else class="empty-state">
        <view class="empty-icon-wrapper">
          <text class="empty-icon">✨</text>
        </view>
        <text class="empty-text">今天没有待办事项</text>
        <text class="empty-subtext">享受生活吧！</text>
      </view>
    </view>
    
    <!-- 纪念日提醒 -->
    <view class="section-card anniversary-card animate-in" v-if="upcomingAnniversaries.length > 0">
      <view class="section-header">
        <view class="title-wrapper">
          <text class="section-icon">💝</text>
          <text class="section-title">近期纪念</text>
        </view>
        <view class="more-btn" @click="navigateTo('/pages/calendar/index')">
          <text>更多</text>
          ›
        </view>
      </view>
      
      <view class="anniversary-list">
        <view 
          v-for="(item, index) in upcomingAnniversaries" 
          :key="item.id"
          class="anni-item"
          :style="{ animationDelay: `${index * 0.1}s` }"
        >
          <view class="anni-icon-wrapper" :class="{ urgent: item.days <= 3 }">
            <text class="anni-icon">{{ item.icon }}</text>
          </view>
          
          <view class="anni-info">
            <text class="anni-title">{{ item.title }}</text>
            <view class="anni-date-wrapper">
              <text class="anni-date">{{ item.date }}</text>
            </view>
          </view>
          
          <view class="anni-days" :class="{ today: item.days === 0, soon: item.days <= 3 && item.days > 0 }">
            <text class="days-num">{{ item.days === 0 ? '今天' : item.days }}</text>
            <text class="days-label">{{ item.days > 0 ? '天后' : '' }}</text>
          </view>
        </view>
      </view>
    </view>
    
    <!-- 今日菜谱推荐 -->
    <view class="section-card recipe-section animate-in">
      <view class="section-header">
        <view class="title-wrapper">
          <text class="section-icon">🍳</text>
          <text class="section-title">今日菜谱</text>
        </view>
        <view class="more-btn" @click="navigateTo('/pages/recipe/index')">
          <text>更多</text>
          ›
        </view>
      </view>
      
      <view class="recipe-scroll">
        <scroll-view scroll-x class="recipe-list" enhanced :show-scrollbar="false">
          <view 
            v-for="(recipe, index) in todayRecipes" 
            :key="recipe.id"
            class="recipe-card"
            :style="{ animationDelay: `${index * 0.1}s` }"
            @click="goRecipeDetail(recipe)"
          >
            <view class="recipe-image-wrapper">
              <LazyImage 
                :src="recipe.cover" 
                mode="aspectFill"
                width="280rpx"
                height="180rpx"
                :threshold="50"
              />
              <view class="recipe-overlay">
                <text class="cook-time">⏱️ {{ recipe.time }}分</text>
              </view>
            </view>
            
            <view class="recipe-info">
              <text class="recipe-name">{{ recipe.name }}</text>
              <view class="recipe-meta">
                <view class="meta-item">
                  <text class="meta-icon">🔥</text>
                  <text class="meta-text">{{ recipe.calories }}卡</text>
                </view>
              </view>
            </view>
          </view>
        </scroll-view>
      </view>
    </view>
    
    </PullRefresh2>
    
    <!-- 设置饮水目标弹窗 -->
    <WaterGoalModal
      :visible="waterGoalModalVisible"
      :current-target="overviewData.waterTarget"
      @close="waterGoalModalVisible = false"
      @confirm="handleSetWaterGoal"
    />
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '../../stores/user'
import { waterApi } from '../../api/water'
import { taskApi } from '../../api/task'
import LazyImage from '@/components/common/LazyImage.vue'
import Skeleton from '@/components/common/Skeleton.vue'
import PullRefresh2 from '@/components/common/PullRefresh2.vue'
import { useSkeleton } from '@/utils/performance.js'

const userStore = useUserStore()
const pullRefreshRef = ref(null)
const { loading: pageLoading, hide: hideSkeleton } = useSkeleton({ minDuration: 500 })

// 问候语
const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '凌晨好'
  if (hour < 9) return '早上好'
  if (hour < 12) return '上午好'
  if (hour < 14) return '中午好'
  if (hour < 18) return '下午好'
  return '晚上好'
})

// 当前日期
const currentDate = computed(() => {
  const now = new Date()
  const month = now.getMonth() + 1
  const day = now.getDate()
  const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  const weekDay = weekDays[now.getDay()]
  return `${month}月${day}日 ${weekDay}`
})

const currentFamily = ref({ name: '幸福小家' })

// 快捷功能 - 添加阴影
const quickActions = [
  { name: '纪念日', icon: '❤️', bgColor: '#FF6B6B', shadow: '0 8rpx 20rpx rgba(255, 107, 107, 0.35)', path: '/pages/anniversary/index' },
  { name: '记账', icon: '💰', bgColor: '#68d391', shadow: '0 8rpx 20rpx rgba(104, 211, 145, 0.35)', path: '/pages/food/record' },
  { name: 'AI', icon: '🤖', bgColor: '#9B59B6', shadow: '0 8rpx 20rpx rgba(155, 89, 182, 0.35)', path: '/pages/ai/chat' },
  { name: '天气', icon: '🌤️', bgColor: '#4facfe', shadow: '0 8rpx 20rpx rgba(79, 172, 254, 0.35)', path: '/pages/weather/index' }
]

// 今日任务
const todayTasks = ref([])

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

// 今日概览数据
const overviewData = ref({
  water: 500,
  waterTarget: 2000,
  calories: 1200,
  expense: 128
})

const navigateTo = (path) => {
  // tabBar 页面使用 switchTab
  if (path === '/pages/task/index' || path === '/pages/task/') {
    uni.switchTab({ url: '/pages/task/index' })
  } else {
    uni.navigateTo({ url: path })
  }
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

// 下拉刷新
const onRefresh = async ({ finish, success, error }) => {
  try {
    // 模拟数据刷新
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // 随机刷新数据（模拟）
    todayTasks.value = todayTasks.value.map(task => ({
      ...task,
      status: Math.random() > 0.7 ? 2 : task.status
    }))
    
    success()
  } catch (e) {
    error()
  }
}

onMounted(async () => {
  // 加载用户信息
  try {
    if (!userStore.userInfo) {
      await userStore.getUserInfo()
    }
  } catch (e) {
    console.log('用户未登录或获取用户信息失败', e)
  }
  
  // 加载首页数据
  // 模拟数据加载完成，隐藏骨架屏
  setTimeout(() => {
    hideSkeleton()
  }, 800)
  
  // 加载喝水数据
  try {
    const userId = userStore.userInfo?.id || uni.getStorageSync('userInfo')?.id || 1
    // 获取今日喝水量
    const waterData = await waterApi.getToday(userId)
    if (waterData) {
      overviewData.value.water = waterData.todayAmount || 0
    }
    // 获取喝水目标
    const targetData = await waterApi.getTarget(userId)
    if (targetData) {
      overviewData.value.waterTarget = targetData.targetAmount || 2000
    }
  } catch (e) {
    console.error('加载喝水数据失败', e)
  }
  
  // 加载今日待办数据
  try {
    const familyId = uni.getStorageSync('currentFamilyId') || 1
    const taskData = await taskApi.getTodayTasks(familyId)
    if (taskData && taskData.length > 0) {
      todayTasks.value = taskData.slice(0, 3).map(task => ({
        ...task,
        assigneeName: task.assigneeName || '家人',
        time: task.dueTime ? '今天 ' + task.dueTime.substring(11, 16) : '今天'
      }))
    }
  } catch (e) {
    console.error('加载待办数据失败', e)
  }
})
</script>

<style lang="scss" scoped>
.home-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #f8f9fc 0%, #f0f4f8 100%);
  padding-bottom: 140rpx;
}

// 顶部欢迎区
.header-section {
  position: relative;
  padding: 40rpx 32rpx 60rpx;
  padding-top: 100rpx;
  overflow: hidden;
  
  .header-bg {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 420rpx;
    background: linear-gradient(135deg, #6B8DD6 0%, #8B5CF6 50%, #A78BFA 100%);
    border-radius: 0 0 60rpx 60rpx;
    box-shadow: 0 20rpx 60rpx rgba(107, 141, 214, 0.3);
    
    .bg-pattern {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      opacity: 0.1;
      background-image: radial-gradient(circle at 20% 50%, rgba(255,255,255,0.8) 0%, transparent 50%),
                        radial-gradient(circle at 80% 20%, rgba(255,255,255,0.6) 0%, transparent 40%);
    }
  }
  
  .header-content {
    position: relative;
    z-index: 1;
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
  }
  
  .header-left {
    .greeting {
      margin-bottom: 12rpx;
      
      .time-label {
        display: block;
        font-size: 28rpx;
        color: rgba(255,255,255,0.85);
        margin-bottom: 8rpx;
        font-weight: 500;
        letter-spacing: 1rpx;
      }
      
      .user-name {
        font-size: 48rpx;
        font-weight: 700;
        color: #fff;
        text-shadow: 0 4rpx 12rpx rgba(0,0,0,0.1);
      }
    }
    
    .current-date {
      display: block;
      font-size: 24rpx;
      color: rgba(255,255,255,0.75);
      margin-top: 8rpx;
    }
  }
  
  .header-right {
    .family-selector {
    display: inline-flex;
    align-items: center;
    background: rgba(255,255,255,0.2);
    backdrop-filter: blur(10rpx);
    padding: 16rpx 28rpx;
    border-radius: 40rpx;
    border: 1rpx solid rgba(255,255,255,0.15);
    transition: all 0.25s ease;
    
    &:active {
      transform: scale(0.96);
      background: rgba(255,255,255,0.25);
    }
    
    .family-name {
      font-size: 26rpx;
      color: #fff;
      margin-right: 12rpx;
      font-weight: 500;
    }
    
    .selector-arrow {
      opacity: 0.8;
      
      .arrow-icon {
        color: #fff;
        font-size: 20rpx;
      }
    }
  }
  }
}

// 快捷功能入口
.quick-actions {
  display: flex;
  justify-content: space-between;
  flex-wrap: wrap;
  padding: 36rpx 48rpx;
  margin: 0 32rpx;
  background: #fff;
  border-radius: 28rpx;
  margin-top: -50rpx;
  position: relative;
  box-shadow: 0 12rpx 40rpx rgba(107, 141, 214, 0.12), 0 4rpx 12rpx rgba(0,0,0,0.04);
  z-index: 2;
  
  .action-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    width: calc(50% - 24rpx);
    margin-bottom: 28rpx;
    animation: fadeInUp 0.5s ease-out forwards;
    opacity: 0;
    
    &:nth-child(n+3) {
      margin-bottom: 0;
    }
    
    &:active {
      transform: scale(0.92);
    }
    
    .icon-box {
      width: 96rpx;
      height: 96rpx;
      border-radius: 28rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-bottom: 16rpx;
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      
      &:active {
        transform: scale(0.9);
      }
    }
    
    .action-name {
      font-size: 24rpx;
      color: #5a6c7d;
      font-weight: 500;
    }
  }
}

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

// 今日概览卡片
.overview-cards {
  display: flex;
  justify-content: space-between;
  padding: 0 32rpx;
  margin-top: 28rpx;
  
  .overview-card {
    flex: 1;
    background: #fff;
    border-radius: 24rpx;
    padding: 24rpx;
    margin-right: 20rpx;
    box-shadow: 0 8rpx 24rpx rgba(107, 141, 214, 0.1), 0 2rpx 8rpx rgba(0,0,0,0.04);
    transition: all 0.3s ease;
    animation: fadeInUp 0.5s ease-out forwards;
    opacity: 0;
    
    &:last-child {
      margin-right: 0;
    }
    
    &:active {
      transform: scale(0.96);
    }
    
    .card-header {
      display: flex;
      align-items: center;
      margin-bottom: 20rpx;
      
      .card-icon-wrapper {
        width: 56rpx;
        height: 56rpx;
        border-radius: 16rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-right: 12rpx;
        
        .card-icon {
          font-size: 28rpx;
        }
        
        &.water-icon {
          background: linear-gradient(135deg, #4ECDC4, #44A08D);
          box-shadow: 0 6rpx 16rpx rgba(78, 205, 196, 0.35);
        }
        
        &.diet-icon {
          background: linear-gradient(135deg, #FF6B6B, #FF8E8E);
          box-shadow: 0 6rpx 16rpx rgba(255, 107, 107, 0.35);
        }
        
        &.expense-icon {
          background: linear-gradient(135deg, #68d391, #48bb78);
          box-shadow: 0 6rpx 16rpx rgba(104, 211, 145, 0.35);
        }
      }
      
      .card-title {
        font-size: 26rpx;
        color: #5a6c7d;
        font-weight: 500;
      }
    }
    
    .water-progress-section {
      .water-progress-bar {
        height: 12rpx;
        background: #e8f5f3;
        border-radius: 6rpx;
        overflow: hidden;
        margin-bottom: 12rpx;
        
        .water-progress-fill {
          height: 100%;
          background: linear-gradient(90deg, #4ECDC4, #44A08D);
          border-radius: 6rpx;
          transition: width 0.5s ease;
        }
      }
      
      .water-text {
        font-size: 22rpx;
        color: #8b9aad;
      }
    }
    
    .card-value {
      .value-num {
        font-size: 40rpx;
        font-weight: 700;
        color: #2d3748;
      }
      
      .value-unit {
        font-size: 24rpx;
        color: #8b9aad;
        margin-left: 6rpx;
      }
    }
  }
}

// 卡片基础样式
.section-card {
  background: #fff;
  border-radius: 28rpx;
  margin: 28rpx 32rpx;
  padding: 32rpx;
  box-shadow: 0 8rpx 32rpx rgba(107, 141, 214, 0.08), 0 2rpx 8rpx rgba(0,0,0,0.03);
  transition: all 0.3s ease;
  
  &.animate-in {
    animation: slideIn 0.5s ease-out forwards;
    opacity: 0;
    transform: translateY(20rpx);
  }
  
  &:active {
    transform: scale(0.995);
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
        letter-spacing: -0.3rpx;
      }
    }
    
    .more-btn {
      display: flex;
      align-items: center;
      padding: 10rpx 16rpx;
      background: rgba(107, 141, 214, 0.08);
      border-radius: 24rpx;
      transition: all 0.2s ease;
      
      &:active {
        background: rgba(107, 141, 214, 0.15);
        transform: scale(0.95);
      }
      
      text {
        font-size: 24rpx;
        color: #6B8DD6;
        font-weight: 500;
        margin-right: 6rpx;
      }
    }
  }
}

@keyframes slideIn {
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

// 任务列表
.task-list {
  .task-item {
    display: flex;
    align-items: center;
    padding: 24rpx 0;
    border-bottom: 2rpx solid #f1f5f9;
    transition: all 0.2s ease;
    animation: fadeInUp 0.4s ease-out forwards;
    opacity: 0;
    
    &:last-child {
      border-bottom: none;
    }
    
    &:active {
      background: #f8f9fc;
      border-radius: 16rpx;
      margin: 0 -16rpx;
      padding-left: 16rpx;
      padding-right: 16rpx;
    }
    
    &.completed {
      .task-title {
        color: #8b9aad;
      }
    }
    
    .task-checkbox {
      margin-right: 24rpx;
      
      .unchecked {
        width: 44rpx;
        height: 44rpx;
        border: 3rpx solid #e2e8f0;
        border-radius: 50%;
        transition: all 0.2s ease;
      }
      
      .checked {
        width: 44rpx;
        height: 44rpx;
        background: linear-gradient(135deg, #68d391, #48bb78);
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        box-shadow: 0 4rpx 12rpx rgba(104, 211, 145, 0.35);
      }
    }
    
    .task-info {
      flex: 1;
      
      .task-title {
        font-size: 30rpx;
        color: #2d3748;
        display: block;
        margin-bottom: 10rpx;
        font-weight: 500;
        transition: all 0.2s ease;
        
        &.completed {
          text-decoration: line-through;
          color: #8b9aad;
        }
      }
      
      .task-meta {
        font-size: 24rpx;
        color: #8b9aad;
        
        .assignee {
          color: #6B8DD6;
          font-weight: 500;
        }
        
        .divider {
          margin: 0 8rpx;
          opacity: 0.5;
        }
      }
    }
    
    .task-priority {
      padding: 8rpx 18rpx;
      border-radius: 24rpx;
      font-size: 22rpx;
      font-weight: 500;
      
      &.high {
        background: rgba(252, 129, 129, 0.12);
        color: #fc8181;
      }
      
      &.medium {
        background: rgba(246, 173, 85, 0.12);
        color: #f6ad55;
      }
      
      &.low {
        background: rgba(104, 211, 145, 0.12);
        color: #68d391;
      }
    }
  }
}

// 空状态
.empty-state {
  text-align: center;
  padding: 60rpx 0;
  
  .empty-icon-wrapper {
    width: 120rpx;
    height: 120rpx;
    background: linear-gradient(135deg, #f0fff4, #e6fffa);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 auto 24rpx;
    
    .empty-icon {
      font-size: 60rpx;
    }
  }
  
  .empty-text {
    display: block;
    font-size: 30rpx;
    color: #5a6c7d;
    font-weight: 500;
    margin-bottom: 8rpx;
  }
  
  .empty-subtext {
    display: block;
    font-size: 26rpx;
    color: #8b9aad;
  }
}

// 纪念日卡片
.anniversary-card {
  background: linear-gradient(135deg, #fff 0%, #fff5f5 100%);
}

.anniversary-list {
  .anni-item {
    display: flex;
    align-items: center;
    padding: 24rpx 0;
    border-bottom: 2rpx solid rgba(226, 232, 240, 0.5);
    animation: fadeInUp 0.4s ease-out forwards;
    opacity: 0;
    
    &:last-child {
      border-bottom: none;
    }
    
    .anni-icon-wrapper {
      width: 88rpx;
      height: 88rpx;
      background: linear-gradient(135deg, #fff0f6, #ffe4e6);
      border-radius: 24rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 44rpx;
      margin-right: 24rpx;
      transition: all 0.3s ease;
      
      &.urgent {
        background: linear-gradient(135deg, #fc8181, #f56565);
        box-shadow: 0 8rpx 24rpx rgba(252, 129, 129, 0.35);
      }
    }
    
    .anni-info {
      flex: 1;
      
      .anni-title {
        font-size: 30rpx;
        color: #2d3748;
        display: block;
        margin-bottom: 10rpx;
        font-weight: 500;
      }
      
      .anni-date-wrapper {
        .anni-date {
          font-size: 24rpx;
          color: #8b9aad;
        }
      }
    }
    
    .anni-days {
      text-align: center;
      padding: 16rpx 24rpx;
      background: rgba(107, 141, 214, 0.08);
      border-radius: 20rpx;
      min-width: 100rpx;
      
      &.today {
        background: linear-gradient(135deg, #68d391, #48bb78);
        box-shadow: 0 8rpx 24rpx rgba(104, 211, 145, 0.35);
        
        .days-num, .days-label {
          color: #fff;
        }
      }
      
      &.soon {
        background: linear-gradient(135deg, #fc8181, #f56565);
        box-shadow: 0 8rpx 24rpx rgba(252, 129, 129, 0.35);
        
        .days-num, .days-label {
          color: #fff;
        }
      }
      
      .days-num {
        font-size: 36rpx;
        font-weight: 700;
        color: #6B8DD6;
        display: block;
        line-height: 1.2;
      }
      
      .days-label {
        font-size: 22rpx;
        color: #8b9aad;
      }
    }
  }
}

// 菜谱区域
.recipe-section {
  .recipe-scroll {
    margin: 0 -32rpx;
    padding: 0 32rpx;
  }
  
  .recipe-list {
    white-space: nowrap;
    
    .recipe-card {
      display: inline-block;
      width: 300rpx;
      margin-right: 24rpx;
      border-radius: 24rpx;
      overflow: hidden;
      background: #fff;
      box-shadow: 0 8rpx 24rpx rgba(0,0,0,0.08);
      transition: all 0.3s ease;
      animation: fadeInUp 0.5s ease-out forwards;
      opacity: 0;
      
      &:active {
        transform: scale(0.96);
      }
      
      &:last-child {
        margin-right: 0;
      }
      
      .recipe-image-wrapper {
        position: relative;
        
        .recipe-overlay {
          position: absolute;
          bottom: 0;
          left: 0;
          right: 0;
          padding: 20rpx;
          background: linear-gradient(transparent, rgba(0,0,0,0.6));
          
          .cook-time {
            font-size: 24rpx;
            color: #fff;
            font-weight: 500;
          }
        }
      }
      
      .recipe-info {
        padding: 20rpx;
        
        .recipe-name {
          font-size: 30rpx;
          color: #2d3748;
          display: block;
          margin-bottom: 16rpx;
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
          font-weight: 500;
        }
        
        .recipe-meta {
          .meta-item {
            display: flex;
            align-items: center;
            
            .meta-icon {
              font-size: 24rpx;
              margin-right: 8rpx;
            }
            
            .meta-text {
              font-size: 24rpx;
              color: #8b9aad;
            }
          }
        }
      }
    }
  }
}

// 健康卡片
.health-card {
  background: linear-gradient(135deg, #fff 0%, #f0fff4 100%);
}

.health-stats {
  display: flex;
  justify-content: space-around;
  padding: 28rpx 0;
  
  .stat-item {
    text-align: center;
    flex: 1;
    
    .stat-icon-wrapper {
      width: 80rpx;
      height: 80rpx;
      border-radius: 24rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      margin: 0 auto 16rpx;
      box-shadow: 0 8rpx 20rpx rgba(0,0,0,0.15);
      
      .stat-icon {
        font-size: 36rpx;
      }
    }
    
    .stat-value {
      font-size: 40rpx;
      font-weight: 700;
      color: #2d3748;
      display: block;
      line-height: 1.3;
    }
    
    .stat-label {
      font-size: 24rpx;
      color: #8b9aad;
      margin-top: 8rpx;
      display: block;
    }
  }
}

.calorie-progress {
  margin-top: 28rpx;
  padding-top: 28rpx;
  border-top: 2rpx solid #f1f5f9;
  
  .progress-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16rpx;
    
    .progress-title {
      font-size: 26rpx;
      color: #5a6c7d;
      font-weight: 500;
    }
    
    .progress-percent {
      font-size: 32rpx;
      font-weight: 700;
      color: #6B8DD6;
    }
  }
  
  .progress-bar {
    height: 16rpx;
    background: #e2e8f0;
    border-radius: 8rpx;
    overflow: hidden;
    
    .progress-fill {
      height: 100%;
      background: linear-gradient(90deg, #6B8DD6, #68d391);
      border-radius: 8rpx;
      transition: width 0.6s cubic-bezier(0.4, 0, 0.2, 1);
      position: relative;
      overflow: hidden;
      
      .progress-shine {
        position: absolute;
        top: 0;
        left: -100%;
        width: 100%;
        height: 100%;
        background: linear-gradient(90deg, transparent, rgba(255,255,255,0.4), transparent);
        animation: shine 2s infinite;
      }
    }
  }
  
  .progress-text {
    display: block;
    text-align: center;
    font-size: 26rpx;
    color: #8b9aad;
    margin-top: 16rpx;
  }
}

@keyframes shine {
  0% {
    left: -100%;
  }
  100% {
    left: 100%;
  }
}
</style>