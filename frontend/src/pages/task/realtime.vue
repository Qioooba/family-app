<template>
  <view class="page-container">
    <view class="header">
      <view class="header-left" @click="goBack"><text>‹</text></view>
      <view class="header-title">实时同步</view>
      <view class="sync-status" :class="{ online: isOnline }">
        <text>{{ isOnline ? '在线' : '离线' }}</text>
      </view>
    </view>
    
    <view class="sync-stats"
003e
      <view class="stat-item"
003e
        <text class="stat-value">{{ syncStats.pending }}</text>
        <text class="stat-label">待同步</text>
      </view>
      <view class="stat-item"
003e
        <text class="stat-value">{{ syncStats.synced }}</text>
        <text class="stat-label">已同步</text>
      </view>
      <view class="stat-item"
003e
        <text class="stat-value">{{ syncStats.conflicts }}</text>
        <text class="stat-label">冲突</text>
      </view>
    </view>
    
    <view class="sync-log"
003e
      <view class="section-title">同步日志</view>
      
      <view class="log-list"
003e
        <view v-for="(log, index) in syncLogs" :key="index" class="log-item" :class="log.type"
003e
          <text class="log-time">{{ log.time }}</text>
          <text class="log-message">{{ log.message }}</text>
        </view>
      </view>
    </view>
    
    <view class="sync-actions"
003e
      <view class="action-btn" @click="forceSync"
003e强制同步</view>
      <view class="action-btn secondary" @click="clearCache">清除缓存</view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'

const isOnline = ref(true)

const syncStats = ref({
  pending: 0,
  synced: 128,
  conflicts: 0
})

const syncLogs = ref([
  { time: '10:23', message: '任务列表同步完成', type: 'success' },
  { time: '10:15', message: '新增任务已同步', type: 'success' },
  { time: '09:45', message: '网络已连接', type: 'info' },
  { time: '09:30', message: '本地更改待同步', type: 'warning' }
])

const forceSync = () => {
  uni.showLoading({ title: '同步中...' })
  setTimeout(() => {
    uni.hideLoading()
    uni.showToast({ title: '同步完成', icon: 'success' })
  }, 1500)
}

const clearCache = () => {
  uni.showModal({
    title: '清除缓存',
    content: '确定要清除本地缓存吗？',
    success: (res) => {
      if (res.confirm) {
        uni.showToast({ title: '缓存已清除', icon: 'success' })
      }
    }
  })
}

const goBack = () => uni.navigateBack()
</script>

<style lang="scss" scoped>
.page-container { min-height: 100vh; background: #F8FAFC; }
.header {
  display: flex; justify-content: space-between; align-items: center;
  padding: 50px 20px 20px;
  background: linear-gradient(135deg, #10B981, #059669);
  .header-left { width: 40px; height: 40px; display: flex; align-items: center; justify-content: center; }
  .header-left text { font-size: 32px; color: #fff; }
  .header-title { font-size: 20px; font-weight: 600; color: #fff; }
  .sync-status { padding: 6px 14px; background: #EF4444; border-radius: 14px; }
  .sync-status.online { background: rgba(255,255,255,0.3); }
  .sync-status text { font-size: 12px; color: #fff; font-weight: 500; }
}
.sync-stats { display: flex; justify-content: space-around; padding: 20px; background: #fff; margin: -10px 15px 15px; border-radius: 16px; box-shadow: 0 2px 8px rgba(0,0,0,0.04); }
.stat-item { text-align: center; }
.stat-value { font-size: 28px; font-weight: 700; color: #10B981; display: block; margin-bottom: 4px; }
.stat-label { font-size: 12px; color: #9CA3AF; }
.sync-log { padding: 0 15px 15px; }
.section-title { font-size: 16px; font-weight: 600; color: #1F2937; margin-bottom: 12px; display: block; }
.log-list { background: #fff; border-radius: 16px; padding: 8px; }
.log-item { display: flex; gap: 12px; padding: 12px; border-bottom: 1px solid #F3F4F6; }
.log-item:last-child { border-bottom: none; }
.log-time { font-size: 12px; color: #9CA3AF; width: 50px; }
.log-message { font-size: 14px; color: #374151; flex: 1; }
.log-item.success .log-message { color: #059669; }
.log-item.warning .log-message { color: #D97706; }
.sync-actions { display: flex; gap: 12px; padding: 0 15px; }
.action-btn { flex: 1; height: 48px; background: linear-gradient(135deg, #10B981, #059669); border-radius: 24px; display: flex; align-items: center; justify-content: center; font-size: 15px; font-weight: 500; color: #fff; }
.action-btn.secondary { background: #F3F4F6; color: #6B7280; }
</style>
