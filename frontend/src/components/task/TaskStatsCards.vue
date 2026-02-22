<template>
  <view class="stats-cards">
    <view class="stat-card">
      <text class="stat-value">{{ stats.total }}</text>
      <text class="stat-label">{{ totalLabel }}</text>
    </view>
    <view class="stat-card completed">
      <text class="stat-value">{{ stats.completed }}</text>
      <text class="stat-label">{{ completedLabel }}</text>
    </view>
    <view class="stat-card pending">
      <text class="stat-value">{{ stats.pending }}</text>
      <text class="stat-label">{{ pendingLabel }}</text>
    </view>
    <view class="stat-card overdue">
      <text class="stat-value">{{ stats.overdue }}</text>
      <text class="stat-label">{{ overdueLabel }}</text>
    </view>
  </view>
</template>

<script setup>
defineProps({
  stats: {
    type: Object,
    default: () => ({ total: 0, completed: 0, pending: 0, overdue: 0 })
  },
  totalLabel: { type: String, default: '本月任务' },
  completedLabel: { type: String, default: '已完成' },
  pendingLabel: { type: String, default: '待办' },
  overdueLabel: { type: String, default: '已逾期' }
})
</script>

<style lang="scss" scoped>
.stats-cards {
  display: flex;
  padding: 20rpx;
  gap: 16rpx;
  
  .stat-card {
    flex: 1;
    background: #fff;
    border-radius: 16rpx;
    padding: 24rpx 16rpx;
    text-align: center;
    box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.06);
    
    &.completed {
      .stat-value { color: #52C41A; }
    }
    
    &.pending {
      .stat-value { color: #1890FF; }
    }
    
    &.overdue {
      .stat-value { color: #FF4D4F; }
    }
    
    .stat-value {
      font-size: 36rpx;
      font-weight: 700;
      color: #333;
      display: block;
      margin-bottom: 8rpx;
    }
    
    .stat-label {
      font-size: 22rpx;
      color: #999;
    }
  }
}
</style>
