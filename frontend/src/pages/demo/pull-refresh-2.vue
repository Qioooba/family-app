<template>
  <view class="pull-refresh-demo">
    <!-- 页面标题 -->
    <view class="page-header">
      <text class="page-title">下拉刷新 2.0</text>
      <text class="page-subtitle">Pull to Refresh 2.0 Demo</text>
    </view>
    
    <!-- 下拉刷新组件 -->
    <PullRefresh2
      ref="pullRefreshRef"
      :enabled="true"
      :threshold="80"
      :max-distance="150"
      :damping="0.8"
      :haptic-enabled="true"
      :show-update-time="true"
      :show-progress="true"
      pulling-text="下拉刷新"
      ready-text="释放立即刷新"
      refreshing-text="正在刷新..."
      success-text="刷新成功"
      error-text="刷新失败"
      @refresh="onRefresh"
      @state-change="onStateChange"
      @pulling="onPulling"
    >
      <!-- 内容区域 -->
      <view class="content-wrapper">
        <!-- 状态展示 -->
        <view class="status-card">
          <view class="status-item">
            <text class="status-label">当前状态:</text>
            <text class="status-value" :class="currentStatus">{{ currentStatusText }}</text>
          </view>
          <view class="status-item">
            <text class="status-label">下拉距离:</text>
            <text class="status-value">{{ pullInfo.distance }}px</text>
          </view>
          <view class="status-item">
            <text class="status-label">完成进度:</text>
            <text class="status-value">{{ pullInfo.percent }}%</text>
          </view>
        </view>
        
        <!-- 功能说明 -->
        <view class="feature-card">
          <view class="card-title">✨ 功能特性</view>
          <view class="feature-list">
            <view class="feature-item" v-for="(feature, index) in features" :key="index">
              <view class="feature-icon">{{ feature.icon }}</view>
              <view class="feature-content">
                <text class="feature-title">{{ feature.title }}</text>
                <text class="feature-desc">{{ feature.desc }}</text>
              </view>
            </view>
          </view>
        </view>
        
        <!-- 数据列表 -->
        <view class="data-card">
          <view class="card-title">📋 模拟数据列表</view>
          <view class="data-list">
            <view class="data-item" v-for="item in dataList" :key="item.id">
              <view class="item-index">{{ item.id }}</view>
              <view class="item-content">
                <text class="item-title">{{ item.title }}</text>
                <text class="item-time">{{ item.time }}</text>
              </view>
              <view class="item-tag" :class="item.type">{{ item.tag }}</view>
            </view>
          </view>
        </view>
        
        <!-- 控制按钮 -->
        <view class="control-card">
          <view class="card-title">🎮 测试控制</view>
          <view class="control-buttons">
            <view class="btn primary" @click="simulateSuccess">模拟成功</view>
            <view class="btn error" @click="simulateError">模拟失败</view>
            <view class="btn warning" @click="reset">重置</view>
          </view>
        </view>
        
        <!-- 事件日志 -->
        <view class="log-card">
          <view class="card-title">📝 事件日志</view>
          <scroll-view scroll-y class="log-list">
            <view class="log-item" v-for="(log, index) in logs" :key="index">
              <text class="log-time">{{ log.time }}</text>
              <text class="log-content">{{ log.content }}</text>
            </view>
          </scroll-view>
        </view>
      </view>
    </PullRefresh2>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import PullRefresh2 from '@/components/common/PullRefresh2.vue'

const pullRefreshRef = ref(null)
const currentStatus = ref('normal')
const pullInfo = ref({ distance: 0, percent: 0 })
const logs = ref([])

// 功能特性列表
const features = [
  { icon: '🎨', title: '流畅动画', desc: '使用CSS3动画和缓动函数，提供丝滑的视觉体验' },
  { icon: '🔔', title: '多状态指示', desc: '支持pulling/ready/refreshing/success/error五种状态' },
  { icon: '📳', title: '触觉反馈', desc: '集成设备振动API，提供轻触、中等、重度三种反馈' },
  { icon: '📊', title: '进度显示', desc: '实时显示下拉进度，支持进度条和百分比' },
  { icon: '⏰', title: '更新时间', desc: '自动记录并显示上次刷新时间' },
  { icon: '⚙️', title: '高度可配', desc: '阻尼系数、阈值、最大距离等参数可配置' }
]

// 模拟数据
const dataList = ref([
  { id: 1, title: '家庭聚餐安排', time: '10:30', tag: '待办', type: 'pending' },
  { id: 2, title: '超市购物清单', time: '09:15', tag: '完成', type: 'success' },
  { id: 3, title: '孩子作业检查', time: '昨天', tag: '重要', type: 'important' },
  { id: 4, title: '周末出行计划', time: '昨天', tag: '完成', type: 'success' },
  { id: 5, title: '水电费缴纳', time: '前天', tag: '待办', type: 'pending' },
  { id: 6, title: '体检预约', time: '前天', tag: '重要', type: 'important' }
])

// 计算状态文本
const currentStatusText = computed(() => {
  const texts = {
    normal: '正常',
    pulling: '下拉中',
    ready: '准备刷新',
    refreshing: '刷新中',
    success: '刷新成功',
    error: '刷新失败'
  }
  return texts[currentStatus.value] || currentStatus.value
})

// 添加日志
const addLog = (content) => {
  const now = new Date()
  const time = `${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}:${String(now.getSeconds()).padStart(2, '0')}`
  logs.value.unshift({ time, content })
  if (logs.value.length > 20) logs.value.pop()
}

// 刷新事件
const onRefresh = async ({ finish, success, error }) => {
  addLog('🔄 开始刷新数据...')
  
  // 模拟异步请求
  setTimeout(() => {
    // 随机成功或失败
    if (Math.random() > 0.3) {
      success()
      addLog('✅ 数据刷新成功')
      // 更新数据时间戳
      updateDataTime()
    } else {
      error()
      addLog('❌ 数据刷新失败')
    }
  }, 1500)
}

// 状态变化
const onStateChange = (state) => {
  currentStatus.value = state
  addLog(`📌 状态变化: ${state}`)
}

// 下拉中
const onPulling = (info) => {
  pullInfo.value = info
}

// 更新时间
const updateDataTime = () => {
  const now = new Date()
  const timeStr = `${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}`
  dataList.value.forEach(item => {
    if (item.time.includes(':')) {
      item.time = timeStr
    }
  })
}

// 模拟成功
const simulateSuccess = () => {
  addLog('🎮 手动触发: 模拟成功')
  pullRefreshRef.value?.successRefresh()
}

// 模拟失败
const simulateError = () => {
  addLog('🎮 手动触发: 模拟失败')
  pullRefreshRef.value?.errorRefresh()
}

// 重置
const reset = () => {
  addLog('🎮 手动触发: 重置')
  pullRefreshRef.value?.resetPull()
  currentStatus.value = 'normal'
  pullInfo.value = { distance: 0, percent: 0 }
}
</script>

<style lang="scss" scoped>
.pull-refresh-demo {
  min-height: 100vh;
  background: #f5f6fa;
}

.page-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 60rpx 40rpx;
  text-align: center;
  
  .page-title {
    display: block;
    font-size: 48rpx;
    font-weight: bold;
    color: #fff;
    margin-bottom: 10rpx;
  }
  
  .page-subtitle {
    font-size: 28rpx;
    color: rgba(255,255,255,0.7);
  }
}

.content-wrapper {
  padding: 30rpx;
}

// 状态卡片
.status-card {
  background: #fff;
  border-radius: 20rpx;
  padding: 30rpx;
  margin-bottom: 30rpx;
  
  .status-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16rpx 0;
    border-bottom: 2rpx solid #f5f5f5;
    
    &:last-child {
      border-bottom: none;
    }
    
    .status-label {
      font-size: 28rpx;
      color: #666;
    }
    
    .status-value {
      font-size: 28rpx;
      font-weight: 600;
      color: #333;
      
      &.normal { color: #999; }
      &.pulling { color: #5B8FF9; }
      &.ready { color: #52c41a; }
      &.refreshing { color: #faad14; }
      &.success { color: #52c41a; }
      &.error { color: #ff4d4f; }
    }
  }
}

// 功能卡片
.feature-card {
  background: #fff;
  border-radius: 20rpx;
  padding: 30rpx;
  margin-bottom: 30rpx;
  
  .card-title {
    font-size: 32rpx;
    font-weight: 600;
    color: #333;
    margin-bottom: 24rpx;
  }
  
  .feature-list {
    .feature-item {
      display: flex;
      align-items: flex-start;
      padding: 20rpx 0;
      border-bottom: 2rpx solid #f5f5f5;
      
      &:last-child {
        border-bottom: none;
      }
      
      .feature-icon {
        width: 60rpx;
        height: 60rpx;
        background: #f0f5ff;
        border-radius: 16rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 32rpx;
        margin-right: 20rpx;
        flex-shrink: 0;
      }
      
      .feature-content {
        flex: 1;
        
        .feature-title {
          display: block;
          font-size: 28rpx;
          font-weight: 600;
          color: #333;
          margin-bottom: 8rpx;
        }
        
        .feature-desc {
          font-size: 24rpx;
          color: #999;
          line-height: 1.5;
        }
      }
    }
  }
}

// 数据卡片
.data-card {
  background: #fff;
  border-radius: 20rpx;
  padding: 30rpx;
  margin-bottom: 30rpx;
  
  .card-title {
    font-size: 32rpx;
    font-weight: 600;
    color: #333;
    margin-bottom: 24rpx;
  }
  
  .data-list {
    .data-item {
      display: flex;
      align-items: center;
      padding: 24rpx 0;
      border-bottom: 2rpx solid #f5f5f5;
      
      &:last-child {
        border-bottom: none;
      }
      
      .item-index {
        width: 48rpx;
        height: 48rpx;
        background: #f0f5ff;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 24rpx;
        font-weight: 600;
        color: #5B8FF9;
        margin-right: 20rpx;
        flex-shrink: 0;
      }
      
      .item-content {
        flex: 1;
        
        .item-title {
          display: block;
          font-size: 28rpx;
          color: #333;
          margin-bottom: 8rpx;
        }
        
        .item-time {
          font-size: 24rpx;
          color: #999;
        }
      }
      
      .item-tag {
        padding: 8rpx 20rpx;
        border-radius: 8rpx;
        font-size: 22rpx;
        
        &.pending {
          background: #fff7e6;
          color: #faad14;
        }
        
        &.success {
          background: #f6ffed;
          color: #52c41a;
        }
        
        &.important {
          background: #fff1f0;
          color: #ff4d4f;
        }
      }
    }
  }
}

// 控制卡片
.control-card {
  background: #fff;
  border-radius: 20rpx;
  padding: 30rpx;
  margin-bottom: 30rpx;
  
  .card-title {
    font-size: 32rpx;
    font-weight: 600;
    color: #333;
    margin-bottom: 24rpx;
  }
  
  .control-buttons {
    display: flex;
    gap: 20rpx;
    
    .btn {
      flex: 1;
      padding: 24rpx 0;
      border-radius: 12rpx;
      text-align: center;
      font-size: 28rpx;
      font-weight: 500;
      
      &.primary {
        background: #5B8FF9;
        color: #fff;
      }
      
      &.error {
        background: #ff4d4f;
        color: #fff;
      }
      
      &.warning {
        background: #faad14;
        color: #fff;
      }
    }
  }
}

// 日志卡片
.log-card {
  background: #fff;
  border-radius: 20rpx;
  padding: 30rpx;
  margin-bottom: 30rpx;
  
  .card-title {
    font-size: 32rpx;
    font-weight: 600;
    color: #333;
    margin-bottom: 24rpx;
  }
  
  .log-list {
    max-height: 400rpx;
    background: #1a1a2e;
    border-radius: 12rpx;
    padding: 20rpx;
    
    .log-item {
      padding: 12rpx 0;
      font-family: monospace;
      
      .log-time {
        color: #64748b;
        font-size: 22rpx;
        margin-right: 16rpx;
      }
      
      .log-content {
        color: #e2e8f0;
        font-size: 24rpx;
      }
    }
  }
}
</style>
