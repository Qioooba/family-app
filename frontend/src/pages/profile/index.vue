<template>
  <view class="profile-page"
003e
    <!-- 用户信息卡 -->
    <view class="user-header"
      <view class="header-bg"></view>
      
      <view class="user-info"
        <image class="user-avatar" :src="userInfo.avatar || '/static/default-avatar.png'" />
        
        <view class="user-detail"
          <text class="user-name">{{ userInfo.nickname || '未设置昵称' }}</text>
          
          <text class="user-phone">{{ userInfo.phone || '' }}</text>
          
          <view class="user-stats"
            <view class="stat"
              <text class="num">{{ stats.tasks }}</text>
              <text class="label">任务</text>
            </view>
            
            <view class="stat"
              <text class="num">{{ stats.wishes }}</text>
              <text class="label">心愿</text>
            </view>
            
            <view class="stat"
              <text class="num">{{ stats.recipes }}</text>
              <text class="label">菜谱</text>
            </view>
          </view>
        </view>
        
        <view class="edit-btn" @click="editProfile"
          <u-icon name="edit-pen" size="32" color="#fff"></u-icon>
        </view>
      </view>
    </view>
    
    <!-- 健康数据卡片 -->
    <view class="health-card"
      <view class="card-header"
        <text class="card-title">💪 健康数据</text>
        
        <text class="edit-link" @click="editHealth">编辑 ></text>
      </view>
      
      <view class="health-grid"
        <view class="health-item"
          <text class="item-label">身高</text>
          <text class="item-value">{{ healthData.height || '--' }} cm</text>
        </view>
        
        <view class="health-item"
          <text class="item-label">体重</text>
          <text class="item-value">{{ healthData.weight || '--' }} kg</text>
        </view>
        
        <view class="health-item"
          <text class="item-label">BMI</text>
          <text class="item-value" :class="bmiStatus.class">{{ bmiStatus.value }}</text>
        </view>
        
        <view class="health-item"
          <text class="item-label">目标体重</text>
          <text class="item-value">{{ healthData.targetWeight || '--' }} kg</text>
        </view>
      </view>
    </view>
    
    <!-- 功能菜单 -->
    <view class="menu-list"
      <view class="menu-group"
        <view class="menu-item" @click="goMyTasks"
          <view class="item-left"
            <view class="icon-box" style="background: #5B8FF9;"
              <u-icon name="file-text" size="32" color="#fff"></u-icon>
            </view>
            
            <text class="item-title">我的任务</text>
          </view>
          
          <u-icon name="arrow-right" size="28" color="#ccc"></u-icon>
        </view>
        
        <view class="menu-item" @click="goMyWishes"
          <view class="item-left"
            <view class="icon-box" style="background: #F5576C;"
              <u-icon name="heart" size="32" color="#fff"></u-icon>
            </view>
            
            <text class="item-title">我的心愿</text>
          </view>
          
          <u-icon name="arrow-right" size="28" color="#ccc"></u-icon>
        </view>
        
        <view class="menu-item" @click="goMyRecipes"
          <view class="item-left"
            <view class="icon-box" style="background: #FAAD14;"
              <u-icon name="photo" size="32" color="#fff"></u-icon>
            </view>
            
            <text class="item-title">我的菜谱</text>
          </view>
          
          <u-icon name="arrow-right" size="28" color="#ccc"></u-icon>
        </view>
      </view>
      
      <view class="menu-group"
        <view class="menu-item" @click="goSettings"
          <view class="item-left"
            <view class="icon-box" style="background: #8C9EFF;"
              <u-icon name="setting" size="32" color="#fff"></u-icon>
            </view>
            
            <text class="item-title">设置</text>
          </view>
          
          <u-icon name="arrow-right" size="28" color="#ccc"></u-icon>
        </view>
        
        <view class="menu-item" @click="goFeedback"
          <view class="item-left"
            <view class="icon-box" style="background: #4DB6AC;"
              <u-icon name="chat" size="32" color="#fff"></u-icon>
            </view>
            
            <text class="item-title">反馈建议</text>
          </view>
          
          <u-icon name="arrow-right" size="28" color="#ccc"></u-icon>
        </view>
        
        <view class="menu-item" @click="goAbout"
          <view class="item-left"
            <view class="icon-box" style="background: #90A4AE;"
              <u-icon name="info-circle" size="32" color="#fff"></u-icon>
            </view>
            
            <text class="item-title">关于我们</text>
          </view>
          
          <u-icon name="arrow-right" size="28" color="#ccc"></u-icon>
        </view>
      </view>
      
      <view class="menu-group"
        <view class="menu-item logout" @click="logout"
          <view class="item-left"
            <view class="icon-box" style="background: #FF6B6B;"
              <u-icon name="minus-circle" size="32" color="#fff"></u-icon>
            </view>
            
            <text class="item-title">退出登录</text>
          </view>
        </view>
      </view>
    </view>
    
    <!-- 版本信息 -->
    <view class="version-info"
      <text>家庭小程序 v1.0.0</text>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'

const userInfo = ref({
  nickname: '快乐爸爸',
  phone: '138****1234',
  avatar: '/static/avatar/dad.jpg'
})

const stats = ref({
  tasks: 56,
  wishes: 12,
  recipes: 8
})

const healthData = ref({
  height: 175,
  weight: 70,
  targetWeight: 68
})

const bmiStatus = computed(() => {
  const h = healthData.value.height / 100
  const w = healthData.value.weight
  if (!h || !w) return { value: '--', class: '' }
  
  const bmi = (w / (h * h)).toFixed(1)
  let cls = 'normal'
  if (bmi < 18.5) cls = 'underweight'
  else if (bmi >= 24) cls = 'overweight'
  
  return { value: bmi, class: cls }
})

const editProfile = () => {
  uni.navigateTo({ url: '/pages/profile/edit' })
}

const editHealth = () => {
  uni.navigateTo({ url: '/pages/profile/health' })
}

const goMyTasks = () => {
  uni.navigateTo({ url: '/pages/profile/my-tasks' })
}

const goMyWishes = () => {
  uni.navigateTo({ url: '/pages/profile/my-wishes' })
}

const goMyRecipes = () => {
  uni.navigateTo({ url: '/pages/profile/my-recipes' })
}

const goSettings = () => {
  uni.navigateTo({ url: '/pages/profile/settings' })
}

const goFeedback = () => {
  uni.navigateTo({ url: '/pages/profile/feedback' })
}

const goAbout = () => {
  uni.navigateTo({ url: '/pages/profile/about' })
}

const logout = () => {
  uni.showModal({
    title: '确认退出',
    content: '确定要退出登录吗？',
    success: (res) => {
      if (res.confirm) {
        uni.removeStorageSync('token')
        uni.removeStorageSync('userInfo')
        uni.reLaunch({ url: '/pages/login/index' })
      }
    }
  })
}
</script>

<style lang="scss" scoped>
.profile-page {
  min-height: 100vh;
  background: #f5f6fa;
  padding-bottom: 40rpx;
}

.user-header {
  position: relative;
  padding: 40rpx;
  padding-top: 80rpx;
  margin-bottom: 100rpx;
  
  .header-bg {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 400rpx;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 0 0 40rpx 40rpx;
  }
  
  .user-info {
    position: relative;
    display: flex;
    align-items: flex-start;
    
    .user-avatar {
      width: 160rpx;
      height: 160rpx;
      border-radius: 50%;
      border: 6rpx solid #fff;
      margin-right: 30rpx;
    }
    
    .user-detail {
      flex: 1;
      padding-top: 20rpx;
      
      .user-name {
        font-size: 40rpx;
        color: #fff;
        font-weight: bold;
        display: block;
        margin-bottom: 12rpx;
      }
      
      .user-phone {
        font-size: 26rpx;
        color: rgba(255,255,255,0.8);
        display: block;
        margin-bottom: 20rpx;
      }
      
      .user-stats {
        display: flex;
        gap: 40rpx;
        
        .stat {
          text-align: center;
          
          .num {
            font-size: 36rpx;
            color: #fff;
            font-weight: bold;
            display: block;
          }
          
          .label {
            font-size: 24rpx;
            color: rgba(255,255,255,0.8);
            margin-top: 4rpx;
          }
        }
      }
    }
    
    .edit-btn {
      padding: 12rpx;
      background: rgba(255,255,255,0.2);
      border-radius: 50%;
    }
  }
}

.health-card {
  margin: 0 30rpx;
  padding: 30rpx;
  background: #fff;
  border-radius: 20rpx;
  margin-top: -60rpx;
  position: relative;
  box-shadow: 0 4rpx 20rpx rgba(0,0,0,0.08);
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24rpx;
    
    .card-title {
      font-size: 30rpx;
      font-weight: 600;
      color: #333;
    }
    
    .edit-link {
      font-size: 26rpx;
      color: #5B8FF9;
    }
  }
  
  .health-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 20rpx;
    
    .health-item {
      text-align: center;
      
      .item-label {
        font-size: 24rpx;
        color: #999;
        display: block;
        margin-bottom: 12rpx;
      }
      
      .item-value {
        font-size: 32rpx;
        color: #333;
        font-weight: 500;
        
        &.underweight {
          color: #FAAD14;
        }
        
        &.normal {
          color: #52C41A;
        }
        
        &.overweight {
          color: #FF6B6B;
        }
      }
    }
  }
}

.menu-list {
  margin: 30rpx;
  
  .menu-group {
    background: #fff;
    border-radius: 20rpx;
    margin-bottom: 20rpx;
    overflow: hidden;
    
    .menu-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 28rpx 30rpx;
      border-bottom: 2rpx solid #f5f5f5;
      
      &:last-child {
        border-bottom: none;
      }
      
      &.logout {
        .item-title {
          color: #FF6B6B;
        }
      }
      
      .item-left {
        display: flex;
        align-items: center;
        
        .icon-box {
          width: 60rpx;
          height: 60rpx;
          border-radius: 16rpx;
          display: flex;
          align-items: center;
          justify-content: center;
          margin-right: 20rpx;
        }
        
        .item-title {
          font-size: 30rpx;
          color: #333;
        }
      }
    }
  }
}

.version-info {
  text-align: center;
  padding: 40rpx 0;
  
  text {
    font-size: 24rpx;
    color: #999;
  }
}
</style>
