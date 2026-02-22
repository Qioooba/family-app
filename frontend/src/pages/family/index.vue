<template>
  <view class="family-page">
    <!-- 家庭信息卡 -->
    <view class="family-header">
      <view class="header-bg"></view>
      
      <view class="family-info">
        <image class="family-avatar" src="/static/family-avatar.jpg" />
        
        <view class="family-detail">
          <text class="family-name">幸福小家</text>
          
          <text class="family-id">家庭ID: 88273651</text>
          
          <view class="member-count">
            <u-icon name="account" size="24" color="#fff"></u-icon>
            <text>5位成员</text>
          </view>
        </view>
        
        <view class="invite-btn" @click="showInvite">
          <text>邀请成员</text>
        </view>
      </view>
    </view>
    
    <!-- 成员列表 -->
    <view class="members-section">
      <view class="section-header">
        <text class="section-title">👨‍👩‍👧‍👦 家庭成员</text>
      </view>
      
      <view class="member-list">
        <view 
          v-for="member in members" 
          :key="member.id"
          class="member-card"
        >
          <image class="member-avatar" :src="member.avatar" />
          
          <view class="member-info">
            <view class="name-row">
              <text class="member-name">{{ member.nickname }}</text>
              
              <view v-if="member.role === 'owner'" class="role-tag owner">家主</view>
              
              <view v-else-if="member.role === 'admin'" class="role-tag admin">管理员</view>
            </view>
            
            <text class="member-phone">{{ member.phone }}</text>
          </view>
          
          <text class="online-status" :class="{ online: member.isOnline }">{{ member.isOnline ? '在线' : '2小时前' }}</text>
        </view>
      </view>
    </view>
    
    <!-- 家庭功能 -->
    <view class="family-features">
      <view class="section-header">
        <text class="section-title">🏠 家庭空间</text>
      </view>
      
      <view class="feature-grid">
        <view 
          v-for="(feature, index) in features" 
          :key="index"
          class="feature-item"
          @click="goFeature(feature)"
        >
          <view class="feature-icon" :style="{ background: feature.bgColor }">
            <u-icon :name="feature.icon" size="44" color="#fff"></u-icon>
          </view>
          
          <text class="feature-name">{{ feature.name }}</text>
        </view>
      </view>
    </view>
    
    <!-- 家庭统计数据 -->
    <view class="family-stats">
      <view class="section-header">
        <text class="section-title">📊 本月统计</text>
      </view>
      
      <view class="stats-grid">
        <view class="stat-card">
          <text class="stat-num">{{ stats.tasksCompleted }}</text>
          <text class="stat-label">任务完成</text>
        </view>
        
        <view class="stat-card">
          <text class="stat-num">{{ stats.wishesCompleted }}</text>
          <text class="stat-label">心愿实现</text>
        </view>
        
        <view class="stat-card">
          <text class="stat-num">{{ stats.mealsCooked }}</text>
          <text class="stat-label">家常菜谱</text>
        </view>
        
        <view class="stat-card">
          <text class="stat-num">{{ stats.photos }}</text>
          <text class="stat-label">家庭照片</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'

const members = ref([
  { id: 1, nickname: '爸爸', avatar: '/static/avatar/dad.jpg', phone: '138****1234', role: 'owner', isOnline: true },
  { id: 2, nickname: '妈妈', avatar: '/static/avatar/mom.jpg', phone: '139****5678', role: 'admin', isOnline: true },
  { id: 3, nickname: '宝贝', avatar: '/static/avatar/baby.jpg', phone: '', role: 'member', isOnline: false },
  { id: 4, nickname: '爷爷', avatar: '/static/avatar/grandpa.jpg', phone: '136****9012', role: 'member', isOnline: false },
  { id: 5, nickname: '奶奶', avatar: '/static/avatar/grandma.jpg', phone: '137****3456', role: 'member', isOnline: false }
])

const features = [
  { name: '家庭相册', icon: 'photo', bgColor: '#FF6B6B', path: '/pages/family/album' },
  { name: '家庭账本', icon: 'rmb-circle', bgColor: '#4ECDC4', path: '/pages/family/account' },
  { name: '文件共享', icon: 'folder', bgColor: '#45B7D1', path: '/pages/family/files' },
  { name: '家庭动态', icon: 'chat', bgColor: '#96CEB4', path: '/pages/family/moments' }
]

const stats = ref({
  tasksCompleted: 128,
  wishesCompleted: 15,
  mealsCooked: 86,
  photos: 256
})

const showInvite = () => {
  uni.showModal({
    title: '邀请家人',
    content: '家庭邀请码：88273651\n\n分享给家人，让他们加入家庭',
    showCancel: false,
    confirmText: '复制邀请码',
    success: () => {
      uni.setClipboardData({
        data: '88273651',
        success: () => uni.showToast({ title: '已复制', icon: 'success' })
      })
    }
  })
}

const goFeature = (feature) => {
  uni.navigateTo({ url: feature.path })
}
</script>

<style lang="scss" scoped>
.family-page {
  min-height: 100vh;
  background: #f5f6fa;
  padding-bottom: 40rpx;
}

.family-header {
  position: relative;
  padding: 40rpx;
  padding-top: 60rpx;
  
  .header-bg {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 320rpx;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 0 0 40rpx 40rpx;
  }
  
  .family-info {
    position: relative;
    display: flex;
    align-items: center;
    
    .family-avatar {
      width: 140rpx;
      height: 140rpx;
      border-radius: 50%;
      border: 4rpx solid #fff;
      margin-right: 24rpx;
    }
    
    .family-detail {
      flex: 1;
      
      .family-name {
        font-size: 40rpx;
        color: #fff;
        font-weight: bold;
        display: block;
        margin-bottom: 8rpx;
      }
      
      .family-id {
        font-size: 24rpx;
        color: rgba(255,255,255,0.8);
        display: block;
        margin-bottom: 12rpx;
      }
      
      .member-count {
        display: flex;
        align-items: center;
        
        text {
          margin-left: 8rpx;
          font-size: 24rpx;
          color: rgba(255,255,255,0.9);
        }
      }
    }
    
    .invite-btn {
      padding: 16rpx 32rpx;
      background: rgba(255,255,255,0.2);
      border-radius: 30rpx;
      
      text {
        font-size: 26rpx;
        color: #fff;
      }
    }
  }
}

.members-section {
  margin: 30rpx;
  padding: 30rpx;
  background: #fff;
  border-radius: 20rpx;
  margin-top: -20rpx;
  position: relative;
  
  .section-header {
    margin-bottom: 24rpx;
    
    .section-title {
      font-size: 32rpx;
      font-weight: 600;
      color: #333;
    }
  }
  
  .member-list {
    .member-card {
      display: flex;
      align-items: center;
      padding: 20rpx 0;
      border-bottom: 2rpx solid #f5f5f5;
      
      &:last-child {
        border-bottom: none;
      }
      
      .member-avatar {
        width: 90rpx;
        height: 90rpx;
        border-radius: 50%;
        margin-right: 20rpx;
      }
      
      .member-info {
        flex: 1;
        
        .name-row {
          display: flex;
          align-items: center;
          margin-bottom: 8rpx;
          
          .member-name {
            font-size: 30rpx;
            color: #333;
            font-weight: 500;
            margin-right: 12rpx;
          }
          
          .role-tag {
            padding: 4rpx 12rpx;
            font-size: 20rpx;
            border-radius: 8rpx;
            
            &.owner {
              background: #FFF1F0;
              color: #FF4D4F;
            }
            
            &.admin {
              background: #F0F5FF;
              color: #5B8FF9;
            }
          }
        }
        
        .member-phone {
          font-size: 24rpx;
          color: #999;
        }
      }
      
      .online-status {
        font-size: 24rpx;
        color: #999;
        
        &.online {
          color: #52C41A;
        }
      }
    }
  }
}

.family-features {
  margin: 30rpx;
  padding: 30rpx;
  background: #fff;
  border-radius: 20rpx;
  
  .section-header {
    margin-bottom: 24rpx;
    
    .section-title {
      font-size: 32rpx;
      font-weight: 600;
      color: #333;
    }
  }
  
  .feature-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 20rpx;
    
    .feature-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      
      .feature-icon {
        width: 100rpx;
        height: 100rpx;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-bottom: 16rpx;
      }
      
      .feature-name {
        font-size: 24rpx;
        color: #666;
      }
    }
  }
}

.family-stats {
  margin: 30rpx;
  padding: 30rpx;
  background: #fff;
  border-radius: 20rpx;
  
  .section-header {
    margin-bottom: 24rpx;
    
    .section-title {
      font-size: 32rpx;
      font-weight: 600;
      color: #333;
    }
  }
  
  .stats-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 20rpx;
    
    .stat-card {
      text-align: center;
      padding: 24rpx;
      background: #f9f9f9;
      border-radius: 16rpx;
      
      .stat-num {
        font-size: 40rpx;
        font-weight: bold;
        color: #5B8FF9;
        display: block;
        margin-bottom: 8rpx;
      }
      
      .stat-label {
        font-size: 24rpx;
        color: #999;
      }
    }
  }
}
</style>
