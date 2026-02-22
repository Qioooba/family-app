<template>
  <view class="theme-toggle" @click="toggleTheme">
    <view class="toggle-icon" :class="{ dark: isDarkMode }">
      <text class="icon-text">{{ isDarkMode ? '🌙' : '☀️' }}</text>
    </view>
    <text class="toggle-text">{{ isDarkMode ? '深色模式' : '浅色模式' }}</text>
  </view>
</template>

<script setup>
import { isDarkMode, toggleTheme } from '@/utils/theme.js'
</script>

<style lang="scss" scoped>
.theme-toggle {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 20rpx 30rpx;
  background: var(--bg-card);
  border-radius: 16rpx;
  
  .toggle-icon {
    width: 48rpx;
    height: 48rpx;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #FFF7E6;
    transition: all 0.3s ease;
    
    &.dark {
      background: #1a1a2e;
    }
    
    .icon-text {
      font-size: 28rpx;
    }
  }
  
  .toggle-text {
    font-size: 28rpx;
    color: var(--text-primary);
  }
}
</style>
