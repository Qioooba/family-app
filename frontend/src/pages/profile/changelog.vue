<template>
  <view class="changelog-page">
    <!-- 导航栏 -->
    <view class="nav-bar">
      <view class="back-btn" @click="goBack">
        <u-icon name="arrow-left" size="40" color="#333"></u-icon>
      </view>
      <text class="title">更新历史</text>
    </view>

    <!-- 版本列表 -->
    <view class="version-list">
      <view class="version-item">
        <view class="version-header">
          <text class="version-number">v1.0.3</text>
          <text class="version-date">2026-03-13</text>
        </view>
        <view class="version-content">
          <view class="change-item">
            <text class="change-tag add">新增</text>
            <text class="change-desc">版本自动更新检测功能</text>
          </view>
          <view class="change-item">
            <text class="change-tag add">新增</text>
            <text class="change-desc">生产环境保留vConsole调试功能</text>
          </view>
          <view class="change-item">
            <text class="change-tag fix">修复</text>
            <text class="change-desc">头像显示问题</text>
          </view>
          <view class="change-item">
            <text class="change-tag fix">修复</text>
            <text class="change-desc">待办列表头像显示格式</text>
          </view>
        </view>
      </view>

      <view class="version-item">
        <view class="version-header">
          <text class="version-number">v1.0.2</text>
          <text class="version-date">2026-03-12</text>
        </view>
        <view class="version-content">
          <view class="change-item">
            <text class="change-tag add">新增</text>
            <text class="change-desc">企业微信推送免密登录功能</text>
          </view>
          <view class="change-item">
            <text class="change-tag add">新增</text>
            <text class="change-desc">待办列表显示双头像（创建者→执行者）</text>
          </view>
          <view class="change-item">
            <text class="change-tag fix">修复</text>
            <text class="change-desc">心愿墙日期显示问题</text>
          </view>
        </view>
      </view>

      <view class="version-item">
        <view class="version-header">
          <text class="version-number">v1.0.1</text>
          <text class="version-date">2026-03-11</text>
        </view>
        <view class="version-content">
          <view class="change-item">
            <text class="change-tag add">新增</text>
            <text class="change-desc">企业微信推送通知功能</text>
          </view>
          <view class="change-item">
            <text class="change-tag add">新增</text>
            <text class="change-desc">喝水打卡125ml和175ml档位</text>
          </view>
        </view>
      </view>

      <view class="version-item">
        <view class="version-header">
          <text class="version-number">v1.0.0</text>
          <text class="version-date">2026-03-01</text>
        </view>
        <view class="version-content">
          <view class="change-item">
            <text class="change-tag add">新增</text>
            <text class="change-desc">家庭小程序正式上线</text>
          </view>
          <view class="change-item">
            <text class="change-tag add">新增</text>
            <text class="change-desc">任务管理、纪念日、心愿墙、喝水打卡</text>
          </view>
          <view class="change-item">
            <text class="change-tag add">新增</text>
            <text class="change-desc">家庭游戏中心（10+小游戏）</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 当前版本信息 -->
    <view class="current-version">
      <text class="version-text">当前版本：v1.0.3</text>
      <text class="build-text">Build 20260313.1</text>
    </view>
  </view>
</template>

<script setup>
import { useUserStore } from '../../stores/user'

const userStore = useUserStore()

const goBack = () => {
  uni.navigateBack()
}
</script>

<style scoped lang="scss">
.changelog-page {
  min-height: 100vh;
  background: #f8f9fa;
}

.nav-bar {
  display: flex;
  align-items: center;
  padding: 40rpx 30rpx 20rpx;
  background: #fff;
  
  .back-btn {
    width: 60rpx;
    height: 60rpx;
    display: flex;
    align-items: center;
    justify-content: center;
  }
  
  .title {
    flex: 1;
    text-align: center;
    font-size: 34rpx;
    font-weight: 600;
    color: #333;
    margin-right: 60rpx;
  }
}

.version-list {
  padding: 30rpx;
}

.version-item {
  background: #fff;
  border-radius: 20rpx;
  padding: 30rpx;
  margin-bottom: 30rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.05);
}

.version-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
  padding-bottom: 20rpx;
  border-bottom: 1rpx solid #f0f0f0;
  
  .version-number {
    font-size: 32rpx;
    font-weight: 700;
    color: #333;
  }
  
  .version-date {
    font-size: 26rpx;
    color: #999;
  }
}

.version-content {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.change-item {
  display: flex;
  align-items: flex-start;
  gap: 16rpx;
}

.change-tag {
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
  font-size: 22rpx;
  font-weight: 500;
  flex-shrink: 0;
  
  &.add {
    background: #e6f7ff;
    color: #1890ff;
  }
  
  &.fix {
    background: #fff7e6;
    color: #fa8c16;
  }
  
  &.opt {
    background: #f6ffed;
    color: #52c41a;
  }
}

.change-desc {
  font-size: 28rpx;
  color: #333;
  line-height: 1.5;
}

.current-version {
  text-align: center;
  padding: 40rpx 30rpx 60rpx;
  
  .version-text {
    display: block;
    font-size: 28rpx;
    color: #666;
    margin-bottom: 8rpx;
  }
  
  .build-text {
    display: block;
    font-size: 24rpx;
    color: #999;
  }
}
</style>
