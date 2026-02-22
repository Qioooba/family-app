<template>
  <view class="page-header" :style="{ background: bgColor }">
    <view class="header-left" @click="$emit('back')">
      <text class="back-icon">‹</text>
    </view>
    <view class="header-title">{{ title }}</view>
    <view class="header-action" @click="$emit('action')" v-if="showAction">
      <text class="action-icon">{{ actionIcon }}</text>
    </view>
    <view class="header-right" v-else></view>
  </view>
</template>

<script setup>
defineProps({
  title: { type: String, default: '' },
  bgColor: { type: String, default: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)' },
  showAction: { type: Boolean, default: true },
  actionIcon: { type: String, default: '+' }
})

defineEmits(['back', 'action'])
</script>

<style lang="scss" scoped>
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 100rpx 30rpx 30rpx;
  
  .header-left {
    width: 60rpx;
    height: 60rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    background: rgba(255,255,255,0.1);
    border-radius: 50%;
    
    .back-icon {
      font-size: 36rpx;
      color: #fff;
      font-weight: 300;
    }
  }
  
  .header-title {
    font-size: 36rpx;
    font-weight: 600;
    color: #fff;
  }
  
  .header-action {
    width: 60rpx;
    height: 60rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    background: rgba(255,255,255,0.2);
    border-radius: 50%;
    
    .action-icon {
      font-size: 32rpx;
      color: #fff;
      font-weight: 300;
    }
  }
  
  .header-right {
    width: 60rpx;
  }
}
</style>
