<template>
  <view class="switch-family-page">
    <view class="nav-bar">
      <view class="back-btn" @click="goBack">
        <u-icon name="arrow-left" size="40" color="#333"></u-icon>
      </view>
      <text class="title">切换家庭</text>
      <view class="right-placeholder"></view>
    </view>
    
    <view class="family-list">
      <view 
        v-for="family in families" 
        :key="family.id"
        class="family-card"
        :class="{ active: currentFamilyId === family.id }"
        @click="switchFamily(family)"
      >
        <view class="family-info">
          <image :src="family.avatar" class="family-avatar" />
          
          <view class="info">
            <text class="name">{{ family.name }}</text>
            <text class="members">{{ family.memberCount }} 位成员</text>          
          </view>
        </view>
        
        <view v-if="currentFamilyId === family.id" class="current-badge">
          <u-icon name="checkmark" size="28" color="#fff"></u-icon>
        </view>
      </view>
    </view>
    
    <view class="add-section">
      <view class="add-btn" @click="createFamily">
        <u-icon name="plus" size="40" color="#5B8FF9"></u-icon>
        <text>创建新家庭</text>
      </view>
      
      <view class="add-btn" @click="joinFamily">
        <u-icon name="share" size="40" color="#5AD8A6"></u-icon>
        <text>加入家庭</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'

const currentFamilyId = ref(1)

const families = ref([
  {
    id: 1,
    name: '幸福小家',
    avatar: '/static/family/default.png',
    memberCount: 3,
    role: 'owner'
  },
  {
    id: 2,
    name: '爷爷奶奶家',
    avatar: '/static/family/grandparents.png',
    memberCount: 5,
    role: 'member'
  }
])

const goBack = () => {
  uni.navigateBack()
}

const switchFamily = (family) => {
  currentFamilyId.value = family.id
  uni.showToast({ title: `已切换到${family.name}`, icon: 'success' })
  setTimeout(() => uni.navigateBack(), 1500)
}

const createFamily = () => {
  uni.navigateTo({ url: '/pages/family/create' })
}

const joinFamily = () => {
  uni.showModal({
    title: '加入家庭',
    editable: true,
    placeholderText: '请输入家庭邀请码',
    success: (res) => {
      if (res.confirm && res.content) {
        uni.showToast({ title: '加入成功', icon: 'success' })
      }
    }
  })
}
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

.family-list {
  padding: 30rpx;
}

.family-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-radius: 20rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
  border: 4rpx solid transparent;
  
  &.active {
    border-color: #5B8FF9;
    background: #F0F5FF;
  }
  
  .family-info {
    display: flex;
    align-items: center;
    gap: 24rpx;
    
    .family-avatar {
      width: 100rpx;
      height: 100rpx;
      border-radius: 50%;
      background: #f0f0f0;
    }
    
    .info {
      .name {
        font-size: 32rpx;
        font-weight: 600;
        color: #333;
        display: block;
        margin-bottom: 8rpx;
      }
      
      .members {
        font-size: 26rpx;
        color: #999;
      }
    }
  }
  
  .current-badge {
    width: 48rpx;
    height: 48rpx;
    background: #5B8FF9;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
  }
}

.add-section {
  display: flex;
  gap: 30rpx;
  padding: 0 30rpx;
  margin-top: 40rpx;
  
  .add-btn {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 16rpx;
    padding: 40rpx 0;
    background: #fff;
    border-radius: 20rpx;
    
    text {
      font-size: 28rpx;
      color: #666;
    }
  }
}
</style>