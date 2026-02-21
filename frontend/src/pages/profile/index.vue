<template>
  <view class="page-container">
    <view class="header">
      <view class="header-title">我的</view>
    </view>
    
    <view class="profile-card" v-if="userInfo">
      <view class="user-info">
        <image class="avatar" :src="userInfo.avatar || '/static/avatar-default.png'" mode="aspectFill" />
        <view class="user-detail">
          <view class="user-name">{{ userInfo.nickname || userInfo.username }}</view>
          <view class="user-id">ID: {{ userInfo.id }}</view>
        </view>
      </view>
      
      <view class="points-section">
        <view class="points-item">
          <text class="points-num">{{ userInfo.points || 0 }}</text>
          <text class="points-label">积分</text>
        </view>
        <view class="points-divider"></view>
        
        <view class="points-item">
          <text class="points-num">{{ userInfo.badgeCount || 0 }}</text>
          <text class="points-label">徽章</text>
        </view>
      </view>
    </view>
    
    <view class="profile-card" v-else @click="goLogin">
      <view class="user-info">
        <view class="avatar">👤</view>
        <view class="user-detail">
          <view class="user-name">未登录</view>
          <view class="user-id">点击登录</view>
        </view>
      </view>
    </view>
    
    <view class="menu-section">
      <view class="menu-group">
        <view class="menu-item" @click="goToPage('/pages/profile/family')">
          <text class="menu-icon">👨‍👩‍👧</text>
          <text class="menu-text">我的家庭</text>
          <text class="menu-arrow">›</text>
        </view>
        
        <view class="menu-item" @click="goToPage('/pages/game/index')">
          <text class="menu-icon">🎮</text>
          <text class="menu-text">家庭游戏</text>
          <text class="menu-arrow">›</text>
        </view>
        
        <view class="menu-item" @click="goToPage('/pages/dashboard/index')">
          <text class="menu-icon">📊</text>
          <text class="menu-text">数据统计</text>
          <text class="menu-arrow">›</text>
        </view>
      </view>
      
      <view class="menu-group">
        <view class="menu-item" @click="goToPage('/pages/profile/settings')">
          <text class="menu-icon">⚙️</text>
          <text class="menu-text">设置</text>
          <text class="menu-arrow">›</text>
        </view>
        
        <view class="menu-item" @click="showAbout">
          <text class="menu-icon">ℹ️</text>
          <text class="menu-text">关于我们</text>
          <text class="menu-arrow">›</text>
        </view>
      </view>
    </view>
    
    
    <view class="logout-btn" @click="logout" v-if="isLogin">
      <text>退出登录</text>
    </view>
  </view>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useUserStore } from '../../stores/user'

const userStore = useUserStore()
const userInfo = computed(() => userStore.userInfo)
const isLogin = computed(() => userStore.isLogin)

onMounted(() => {
  // 如果已登录，刷新用户信息
  if (isLogin.value) {
    userStore.getUserInfo()
  }
})

const goToPage = (url) => {
  uni.navigateTo({ url })
}

const goLogin = () => {
  uni.navigateTo({ url: '/pages/login/index' })
}

const showAbout = () => {
  uni.showModal({
    title: '关于家庭助手',
    content: '版本：v1.0.0\n\n基于 DeepSeek AI 的智能家庭管理平台',
    showCancel: false
  })
}

const logout = () => {
  uni.showModal({
    title: '确认退出',
    content: '确定要退出登录吗？',
    success: (res) => {
      if (res.confirm) {
        userStore.logout()
      }
    }
  })
}
</script>

<style lang="scss" scoped>
.page-container {
  min-height: 100vh;
  background: #F5F7FA;
}

.header {
  padding: 50px 20px 20px;
  background: linear-gradient(135deg, #E3F2FD, #BBDEFB);
  
  .header-title {
    font-size: 24px;
    font-weight: 700;
    color: #2C3E50;
    text-align: center;
  }
}

.profile-card {
  background: #fff;
  margin: -30px 15px 15px;
  border-radius: 20px;
  padding: 20px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.08);
}

.user-info {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  
  .avatar {
    width: 70px;
    height: 70px;
    background: linear-gradient(135deg, #4CAF50, #81C784);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 36px;
    margin-right: 16px;
    border: 4px solid #E8F5E9;
  }
  
  .user-detail {
    .user-name {
      font-size: 20px;
      font-weight: 600;
      color: #2C3E50;
      margin-bottom: 4px;
    }
    
    .user-id {
      font-size: 13px;
      color: #7F8C8D;
    }
  }
}

.points-section {
  display: flex;
  justify-content: center;
  align-items: center;
  padding-top: 15px;
  border-top: 1px solid #F0F0F0;
  
  .points-item {
    flex: 1;
    text-align: center;
    
    .points-num {
      display: block;
      font-size: 28px;
      font-weight: 700;
      color: #FF9800;
      margin-bottom: 4px;
    }
    
    .points-label {
      font-size: 13px;
      color: #7F8C8D;
    }
  }
  
  .points-divider {
    width: 1px;
    height: 40px;
    background: #E0E6ED;
  }
}

.menu-section {
  padding: 0 15px;
}

.menu-group {
  background: #fff;
  border-radius: 16px;
  margin-bottom: 15px;
  overflow: hidden;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #F5F7FA;
  
  &:last-child {
    border-bottom: none;
  }
  
  .menu-icon {
    font-size: 24px;
    margin-right: 12px;
  }
  
  .menu-text {
    flex: 1;
    font-size: 15px;
    color: #2C3E50;
  }
  
  .menu-arrow {
    font-size: 20px;
    color: #BDC3C7;
  }
}

.logout-btn {
  margin: 30px 15px;
  padding: 16px;
  background: #fff;
  border-radius: 16px;
  text-align: center;
  
  text {
    font-size: 15px;
    color: #F44336;
  }
}
</style>
