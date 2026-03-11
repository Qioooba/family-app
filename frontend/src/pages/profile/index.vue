<template>
  <view class="page-container">
    <view class="profile-card" v-if="userInfo">
      <view class="user-info">
        <view class="avatar-wrapper" @click="editAvatar">
          <image class="avatar" :src="displayAvatar" mode="aspectFill" />
          <view class="avatar-edit-icon">✏️</view>
        </view>
        <view class="user-detail">
          <view class="user-name" @click="editNickname">
            {{ userInfo.nickname || userInfo.username || '未设置昵称' }}
            <text class="edit-icon">✏️</text>
          </view>
          <view class="user-id">ID: {{ userInfo.phone || userInfo.id }}</view>
        </view>
      </view>
      
      <view class="points-section">
        <view class="points-item">
          <text class="points-num">{{ points }}</text>
          <text class="points-label">积分</text>
        </view>
        <view class="points-divider"></view>
        
        <view class="points-item">
          <text class="points-num">{{ badgeCount }}</text>
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
        <view class="menu-item" @click="goToPage('/pages/game/index')">
          <text class="menu-icon">🎮</text>
          <text class="menu-text">家庭游戏</text>
          <text class="menu-arrow">›</text>
        </view>
        
        <!-- <view class="menu-item" @click="goToPage('/pages/dashboard/index')">
          <text class="menu-icon">📊</text>
          <text class="menu-text">数据统计</text>
          <text class="menu-arrow">›</text>
        </view> -->
      </view>
      
      <view class="menu-group">
        <view class="menu-item" @click="goToPage('/pages/profile/settings')">
          <text class="menu-icon">⚙️</text>
          <text class="menu-text">设置</text>
          <text class="menu-arrow">›</text>
        </view>
        
        <view class="menu-item" @click="toggleTheme">
          <text class="menu-icon">{{ isDarkMode ? '🌙' : '☀️' }}</text>
          <text class="menu-text">{{ isDarkMode ? '深色模式' : '浅色模式' }}</text>
          <text class="menu-arrow">›</text>
        </view>
        
        <view class="menu-item" @click="showAbout">
          <text class="menu-icon">ℹ️</text>
          <text class="menu-text">关于我们</text>
          <text class="menu-arrow">›</text>
        </view>
        
        <view class="menu-item" @click="showUpdateHistory">
          <text class="menu-icon">📝</text>
          <text class="menu-text">更新历史</text>
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
import { computed, onMounted, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useUserStore } from '../../stores/user'
import { gameApi } from '../../api/game'
import { isDarkMode, toggleTheme } from '../../utils/theme.js'

const userStore = useUserStore()
const userInfo = computed(() => userStore.userInfo)
const isLogin = computed(() => userStore.isLogin)
const points = ref(0)
const badgeCount = ref(0)

// 计算显示的头像：优先使用微信头像
const displayAvatar = computed(() => {
  const info = userInfo.value
  if (!info) return '../../static/avatar-default.png'
  // 优先使用微信头像
  if (info.wxAvatar) return info.wxAvatar
  // 其次使用用户自定义头像
  if (info.avatar) {
    // 如果是相对路径，添加服务器地址
    if (info.avatar.startsWith('/uploads')) {
      return 'http://qioba.cn:3000' + info.avatar
    }
    return info.avatar
  }
  // 默认头像
  return '../../static/avatar-default.png'
})

// 每次页面显示时都加载用户信息
onShow(async () => {
  // 始终尝试获取最新的用户信息
  try {
    await userStore.getUserInfo()
  } catch (e) {
    console.log('获取用户信息失败', e)
  }
  
  // 如果已登录，刷新用户信息和积分
  if (isLogin.value) {
    loadUserData()
  }
})

const loadUserData = async () => {
  try {
    // 从本地存储获取用户信息
    const localUserInfo = uni.getStorageSync('userInfo')
    if (!localUserInfo) {
      // 本地没有，从服务器获取
      await userStore.getUserInfo()
    }
    
    // 获取积分
    const pointsRes = await gameApi.getUserPoints()
    if (pointsRes && pointsRes.points !== undefined) {
      points.value = pointsRes.points
    }
    
    // 获取徽章数
    const achievementsRes = await gameApi.getAchievements()
    if (achievementsRes && Array.isArray(achievementsRes)) {
      badgeCount.value = achievementsRes.filter(a => a.earned).length
    }
  } catch (e) {
    console.error('加载用户数据失败:', e)
  }
}

// 编辑昵称
const editNickname = () => {
  uni.showModal({
    title: '修改昵称',
    placeholderText: '请输入新昵称',
    editable: true,
    defaultText: userInfo.value?.nickname || '',
    success: async (res) => {
      if (res.confirm && res.content && res.content.trim()) {
        try {
          uni.showLoading({ title: '保存中...' })
          await userStore.updateUserInfo({
            nickname: res.content.trim()
          })
          uni.showToast({
            title: '修改成功',
            icon: 'success'
          })
        } catch (e) {
          uni.showToast({
            title: '修改失败',
            icon: 'none'
          })
        } finally {
          uni.hideLoading()
        }
      }
    }
  })
}

// 编辑头像
const editAvatar = () => {
  uni.showActionSheet({
    itemList: ['从相册选择', '拍照', '使用微信头像'],
    success: async (res) => {
      if (res.tapIndex === 0) {
        // 从相册选择
        chooseImage('album')
      } else if (res.tapIndex === 1) {
        // 拍照
        chooseImage('camera')
      } else if (res.tapIndex === 2) {
        // 使用微信头像 - 需要微信登录时获取
        useWechatAvatar()
      }
    }
  })
}

// 选择图片
const chooseImage = (sourceType) => {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    sourceType: [sourceType],
    success: async (res) => {
      const tempFilePath = res.tempFilePaths[0]
      // 上传头像
      await uploadAvatar(tempFilePath)
    }
  })
}

// 上传头像
const uploadAvatar = async (filePath) => {
  try {
    uni.showLoading({ title: '上传中...' })
    
    // 使用完整 URL，避免 url scheme 错误
    const baseUrl = 'https://qioba.cn:8443'
    
    // 这里调用上传接口
    const uploadRes = await new Promise((resolve, reject) => {
      uni.uploadFile({
        url: `${baseUrl}/api/user/avatar`,
        filePath: filePath,
        name: 'file',
        header: {
          'Authorization': uni.getStorageSync('token') || ''
        },
        success: (res) => {
          try {
            const data = JSON.parse(res.data)
            resolve(data)
          } catch (e) {
            reject(e)
          }
        },
        fail: reject
      })
    })
    
    if (uploadRes.data?.url) {
      await userStore.updateUserInfo({
        avatar: uploadRes.data.url
      })
      uni.showToast({
        title: '上传成功',
        icon: 'success'
      })
    }
  } catch (e) {
    console.error('上传头像失败:', e)
    uni.showToast({
      title: '上传失败，请重试',
      icon: 'none'
    })
  } finally {
    uni.hideLoading()
  }
}

// 使用微信头像
const useWechatAvatar = async () => {
  // #ifdef MP-WEIXIN
  // 获取微信头像需要用户授权
  uni.getUserProfile({
    desc: '用于完善用户资料',
    success: async (res) => {
      if (res.userInfo && res.userInfo.avatarUrl) {
        try {
          uni.showLoading({ title: '保存中...' })
          await userStore.updateUserInfo({
            wxAvatar: res.userInfo.avatarUrl
          })
          uni.showToast({
            title: '头像已更新',
            icon: 'success'
          })
        } catch (e) {
          uni.showToast({
            title: '设置失败',
            icon: 'none'
          })
        } finally {
          uni.hideLoading()
        }
      }
    },
    fail: () => {
      uni.showToast({
        title: '获取头像失败',
        icon: 'none'
      })
    }
  })
  // #endif
  // #ifndef MP-WEIXIN
  uni.showToast({
    title: '仅支持微信小程序',
    icon: 'none'
  })
  // #endif
}

const goToPage = (url) => {
  uni.navigateTo({ url })
}

const goLogin = () => {
  uni.navigateTo({ url: '/pages/login/index' })
}

const showAbout = () => {
  uni.showModal({
    title: '家庭小程序',
    content: '版本: 1.0.0\n\n这是一个家庭内部使用的小程序\n仅供家庭成员使用',
    showCancel: false,
    confirmText: '知道了',
    confirmColor: '#333333'
  })
}

const showPrivacyPolicy = () => {
  uni.showModal({
    title: '🔒 隐私政策',
    content: '━━━━━━━━━━━━━━━━━━\n\n【数据收集】\n我们仅收集为您提供服务所必需的数据，包括：\n• 账户信息（用户名、昵称、头像）\n• 家庭成员信息\n• 任务、心愿等家庭数据\n• 位置信息（仅用于任务提醒功能）\n\n【数据使用】\n您的数据仅用于：\n• 提供核心家庭管理功能\n• 改善用户体验\n• 数据备份与同步\n\n【数据保护】\n• 我们不会出售或分享您的个人数据给第三方\n• 您可以随时查看、修改或删除您的数据\n• 账户注销后，我们将删除您的所有个人数据\n\n【联系我们】\n如有问题，请联系开发者。\n\n━━━━━━━━━━━━━━━━━━\n\n✓ 我已阅读并同意隐私政策',
    showCancel: false,
    confirmText: '✓ 同意',
    confirmColor: '#68d391'
  })
}

const showUpdateHistory = () => {
  uni.showModal({
    title: '更新日志',
    content: 'v1.0.0 (2026-03)\n\n- 初始版本\n- 基础功能：任务管理、纪念日、天气\n- 微信一键登录',
    showCancel: false,
    confirmText: '知道了',
    confirmColor: '#333333'
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
  background: linear-gradient(180deg, #f8f9fc 0%, #f0f4f8 100%);
  padding-bottom: 160rpx; /* 为tabBar留出空间 */
}

.profile-card {
  background: #fff;
  margin: 32rpx 32rpx;
  border-radius: 32rpx;
  padding: 40rpx;
  box-shadow: 0 16rpx 48rpx rgba(107, 141, 214, 0.12);
  position: relative;
  z-index: 1;
}

.user-info {
  display: flex;
  align-items: center;
  margin-bottom: 32rpx;
  
  .avatar-wrapper {
    position: relative;
    margin-right: 28rpx;
  }
  
  .avatar {
    width: 140rpx;
    height: 140rpx;
    background: linear-gradient(135deg, #6B8DD6, #8B5CF6);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 72rpx;
    border: 6rpx solid rgba(107, 141, 214, 0.1);
    box-shadow: 0 8rpx 24rpx rgba(107, 141, 214, 0.2);
  }
  
  .avatar-edit-icon {
    position: absolute;
    right: 0;
    bottom: 0;
    width: 40rpx;
    height: 40rpx;
    background: #fff;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 20rpx;
    box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.15);
  }
  
  .user-detail {
    flex: 1;
    
    .user-name {
      font-size: 40rpx;
      font-weight: 700;
      color: #2d3748;
      margin-bottom: 12rpx;
      display: flex;
      align-items: center;
      
      .edit-icon {
        margin-left: 12rpx;
        font-size: 24rpx;
        opacity: 0.5;
      }
    }
    
    .user-id {
      font-size: 26rpx;
      color: #8b9aad;
      background: #f8f9fc;
      padding: 8rpx 20rpx;
      border-radius: 20rpx;
      display: inline-block;
    }
  }
}

.points-section {
  display: flex;
  justify-content: center;
  align-items: center;
  padding-top: 32rpx;
  border-top: 2rpx solid #f1f5f9;
  
  .points-item {
    flex: 1;
    text-align: center;
    
    .points-num {
      display: block;
      font-size: 48rpx;
      font-weight: 700;
      color: #f6ad55;
      margin-bottom: 8rpx;
    }
    
    .points-label {
      font-size: 26rpx;
      color: #8b9aad;
      font-weight: 500;
    }
  }
  
  .points-divider {
    width: 2rpx;
    height: 80rpx;
    background: linear-gradient(180deg, transparent, #e2e8f0, transparent);
  }
}

.menu-section {
  padding: 0 32rpx;
}

.menu-group {
  background: #fff;
  border-radius: 28rpx;
  margin-bottom: 24rpx;
  overflow: hidden;
  box-shadow: 0 8rpx 32rpx rgba(107, 141, 214, 0.06);
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 32rpx;
  border-bottom: 2rpx solid #f8f9fc;
  transition: all 0.2s ease;
  
  &:last-child {
    border-bottom: none;
  }
  
  &:active {
    background: #f8f9fc;
  }
  
  .menu-icon {
    font-size: 44rpx;
    margin-right: 24rpx;
  }
  
  .menu-text {
    flex: 1;
    font-size: 30rpx;
    color: #2d3748;
    font-weight: 500;
  }
  
  .menu-arrow {
    font-size: 36rpx;
    color: #cbd5e0;
    font-weight: 300;
  }
}

.logout-btn {
  margin: 48rpx 32rpx;
  padding: 32rpx;
  background: linear-gradient(135deg, #fff 0%, #fff5f5 100%);
  border-radius: 28rpx;
  text-align: center;
  box-shadow: 0 8rpx 24rpx rgba(252, 129, 129, 0.1);
  transition: all 0.25s ease;
  
  &:active {
    transform: scale(0.98);
  }
  
  text {
    font-size: 30rpx;
    color: #fc8181;
    font-weight: 600;
  }
}
</style>
