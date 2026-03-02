<template>
  <view class="switch-family-page">
    <view class="nav-bar">
      <view class="back-btn" @click="goBack">
        <u-icon name="arrow-left" size="40" color="#333"></u-icon>
      </view>
      <text class="title">家庭信息</text>
      <view class="right-placeholder"></view>
    </view>
    
    <view class="content">
      <view class="info-card">
        <image :src="currentFamily?.avatar || '/static/family/default.png'" class="family-avatar" />
        
        <view class="family-info">
          <text class="family-name">{{ currentFamily?.name || '幸福小家' }}</text>
          <text class="family-desc">当前家庭</text>
        </view>
        
        <view class="current-badge">
          <u-icon name="checkmark" size="28" color="#fff"></u-icon>
        </view>
      </view>
      
      <view class="tip-section">
        <u-icon name="info-circle" size="32" color="#5B8FF9"></u-icon>
        <text class="tip-text">当前为单家庭模式，每个账号只能拥有一个家庭</text>
      </view>
      
      <view class="action-section">
        <view class="action-btn primary" @click="goToFamilyPage">
          <u-icon name="home" size="36" color="#fff"></u-icon>
          <text>进入家庭空间</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getDefaultFamily } from '@/utils/defaultFamily.js'

const currentFamily = ref(null)

const goBack = () => {
  uni.navigateBack()
}

const goToFamilyPage = () => {
  uni.switchTab({ url: '/pages/family/index' })
}

onMounted(async () => {
  // 获取当前默认家庭
  currentFamily.value = await getDefaultFamily()
  console.log('[FamilySwitch] 当前家庭:', currentFamily.value)
})
</script>

<style lang="scss" scoped>
.switch-family-page {
  min-height: 100vh;
  background: #f5f6fa;
}

.nav-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 30rpx;
  padding-top: 60rpx;
  background: #fff;
  
  .back-btn {
    width: 60rpx;
    height: 60rpx;
    display: flex;
    align-items: center;
    justify-content: center;
  }
  
  .title {
    font-size: 34rpx;
    font-weight: 600;
    color: #333;
  }
  
  .right-placeholder {
    width: 60rpx;
  }
}

.content {
  padding: 40rpx 30rpx;
}

.info-card {
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 24rpx;
  padding: 40rpx;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.08);
  
  .family-avatar {
    width: 120rpx;
    height: 120rpx;
    border-radius: 50%;
    background: #f0f0f0;
    margin-right: 24rpx;
  }
  
  .family-info {
    flex: 1;
    
    .family-name {
      font-size: 36rpx;
      font-weight: 600;
      color: #333;
      display: block;
      margin-bottom: 8rpx;
    }
    
    .family-desc {
      font-size: 26rpx;
      color: #999;
    }
  }
  
  .current-badge {
    width: 56rpx;
    height: 56rpx;
    background: #5B8FF9;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
  }
}

.tip-section {
  display: flex;
  align-items: flex-start;
  margin-top: 40rpx;
  padding: 24rpx;
  background: #F0F5FF;
  border-radius: 16rpx;
  
  .tip-text {
    flex: 1;
    margin-left: 16rpx;
    font-size: 28rpx;
    color: #5B8FF9;
    line-height: 1.5;
  }
}

.action-section {
  margin-top: 60rpx;
  
  .action-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 16rpx;
    padding: 28rpx 0;
    border-radius: 24rpx;
    font-size: 32rpx;
    font-weight: 500;
    
    &.primary {
      background: linear-gradient(135deg, #5B8FF9, #6B8DD6);
      color: #fff;
      box-shadow: 0 8rpx 24rpx rgba(91, 143, 249, 0.3);
    }
    
    &:active {
      transform: scale(0.98);
    }
  }
}
</style>
