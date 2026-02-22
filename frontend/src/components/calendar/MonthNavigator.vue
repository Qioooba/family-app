<template>
  <view class="month-navigator">
    <view class="nav-btn" @click="$emit('prev')">
      <text class="nav-icon">‹</text>
    </view>
    <view class="month-display" @click="$emit('select')">
      <text class="month-year">{{ year }}年</text>
      <text class="month-text">{{ month }}月</text>
      <text v-if="lunar" class="month-lunar">{{ lunar }}</text>
    </view>
    <view class="nav-btn" @click="$emit('next')">
      <text class="nav-icon">›</text>
    </view>
  </view>
</template>

<script setup>
defineProps({
  year: { type: Number, default: 2025 },
  month: { type: Number, default: 1 },
  lunar: { type: String, default: '' }
})

defineEmits(['prev', 'next', 'select'])
</script>

<style lang="scss" scoped>
.month-navigator {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 30rpx;
  
  .nav-btn {
    width: 60rpx;
    height: 60rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    background: rgba(255,255,255,0.1);
    border-radius: 50%;
    
    .nav-icon {
      font-size: 32rpx;
      color: #fff;
      font-weight: 300;
    }
  }
  
  .month-display {
    display: flex;
    flex-direction: column;
    align-items: center;
    
    .month-year {
      font-size: 24rpx;
      color: rgba(255,255,255,0.7);
      margin-bottom: 4rpx;
    }
    
    .month-text {
      font-size: 40rpx;
      font-weight: 700;
      color: #fff;
    }
    
    .month-lunar {
      font-size: 22rpx;
      color: rgba(255,255,255,0.6);
      margin-top: 4rpx;
    }
  }
}
</style>
