<template>
  <view class="load-more-demo">
    <!-- 页面标题 -->
    <view class="page-header">
      <text class="page-title">上拉加载 2.0</text>
      <text class="page-subtitle">Load More 2.0 Demo</text>
    </view>
    
    <!-- 数据列表 -->
    <scroll-view 
      scroll-y 
      class="content-scroll"
      @scrolltolower="onScrollToLower"
      :lower-threshold="100"
    >
      <view class="data-list">
        <view class="data-item" v-for="item in dataList" :key="item.id">
          <view class="item-image">
            <text>{{ item.emoji }}</text>
          </view>
          
          <view class="item-content">
            <text class="item-title">{{ item.title }}</text>
            <text class="item-desc">{{ item.desc }}</text>
            <view class="item-meta">
              <text class="meta-tag">{{ item.tag }}</text>
              <text class="meta-time">{{ item.time }}</text>
            </view>
          </view>
        </view>
      </view>
      
      <!-- 上拉加载组件 -->
      <LoadMore2
        ref="loadMoreRef"
        :status="loadStatus"
        :auto-load="autoLoad"
        :threshold="100"
        :show-skeleton="showSkeleton"
        :skeleton-rows="2"
        :theme="currentTheme"
        @load-more="onLoadMore"
        @retry="onRetry"
        @state-change="onStateChange"
      />
    </scroll-view>
    
    <!-- 控制面板 -->
    <view class="control-panel">
      <view class="panel-title">⚙️ 控制面板</view>
      
      <view class="control-section">
        <text class="section-label">当前状态</text>
        <view class="status-badge" :class="loadStatus">{{ statusText }}</view>
      </view>
      
      <view class="control-section">
        <text class="section-label">自动加载</text>
        <switch :checked="autoLoad" @change="autoLoad = !autoLoad" />
      </view>
      
      <view class="control-section">
        <text class="section-label">显示骨架屏</text>
        <switch :checked="showSkeleton" @change="showSkeleton = !showSkeleton" />
      </view>
      
      <view class="control-section">
        <text class="section-label">主题风格</text>
        <view class="theme-selector">
          <view 
            class="theme-option" 
            :class="{ active: currentTheme === 'default' }"
            @click="currentTheme = 'default'"
          >默认</view>
          <view 
            class="theme-option" 
            :class="{ active: currentTheme === 'simple' }"
            @click="currentTheme = 'simple'"
          >简约</view>
          <view 
            class="theme-option" 
            :class="{ active: currentTheme === 'card' }"
            @click="currentTheme = 'card'"
          >卡片</view>
        </view>
      </view>
      
      <view class="control-buttons">
        <view class="btn primary" @click="loadNextPage">加载下一页</view>
        <view class="btn success" @click="setNoMore">设置无更多</view>
        <view class="btn error" @click="setError">模拟错误</view>
        <view class="btn warning" @click="reset">重置</view>
      </view>
      
      <view class="stats-section">
        <view class="stat-item">
          <text class="stat-value">{{ dataList.length }}</text>
          <text class="stat-label">当前条数</text>
        </view>
        <view class="stat-item">
          <text class="stat-value">{{ currentPage }}</text>
          <text class="stat-label">当前页码</text>
        </view>
        <view class="stat-item">
          <text class="stat-value">{{ totalPages }}</text>
          <text class="stat-label">总页数</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import LoadMore2 from '@/components/common/LoadMore2.vue'

const loadMoreRef = ref(null)
const loadStatus = ref('default')
const autoLoad = ref(true)
const showSkeleton = ref(false)
const currentTheme = ref('default')
const currentPage = ref(1)
const totalPages = ref(10)

// 模拟数据生成
const generateData = (page, count = 10) => {
  const emojis = ['📱', '💻', '🎮', '📷', '🎨', '🎵', '📚', '✈️', '🍕', '🎬']
  const tags = ['科技', '娱乐', '生活', '工作', '学习']
  const titles = [
    '智能设备使用心得',
    '周末去哪儿玩',
    '美食探店记录',
    '工作效率提升技巧',
    '读书分享',
    '旅行日记',
    '美食制作教程',
    '电影推荐',
    '音乐分享',
    '生活小技巧'
  ]
  
  return Array.from({ length: count }, (_, i) => ({
    id: `${page}-${i}`,
    emoji: emojis[Math.floor(Math.random() * emojis.length)],
    title: `${titles[Math.floor(Math.random() * titles.length)]} #${(page - 1) * count + i + 1}`,
    desc: '这是一段示例描述文字，用于展示列表项的内容...',
    tag: tags[Math.floor(Math.random() * tags.length)],
    time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  }))
}

// 数据列表
const dataList = ref(generateData(1))

// 状态文本
const statusText = computed(() => {
  const texts = {
    default: '默认',
    ready: '准备加载',
    loading: '加载中',
    noMore: '无更多',
    error: '错误'
  }
  return texts[loadStatus.value] || loadStatus.value
})

// 加载更多
const onLoadMore = ({ finish, noMore, error }) => {
  loadStatus.value = 'loading'
  
  // 模拟异步加载
  setTimeout(() => {
    if (loadStatus.value === 'error') {
      error()
      return
    }
    
    if (currentPage.value >= totalPages.value) {
      noMore()
      return
    }
    
    currentPage.value++
    const newData = generateData(currentPage.value)
    dataList.value.push(...newData)
    finish()
  }, 1500)
}

// 滚动到底部
const onScrollToLower = () => {
  if (autoLoad.value && loadStatus.value !== 'loading' && loadStatus.value !== 'noMore') {
    loadNextPage()
  }
}

// 状态变化
const onStateChange = (state) => {
  loadStatus.value = state
}

// 重试
const onRetry = () => {
  loadStatus.value = 'default'
  loadNextPage()
}

// 加载下一页
const loadNextPage = () => {
  if (loadStatus.value === 'loading') return
  loadMoreRef.value?.loadMore()
}

// 设置无更多
const setNoMore = () => {
  loadMoreRef.value?.noMoreLoad()
}

// 模拟错误
const setError = () => {
  loadMoreRef.value?.errorLoad()
}

// 重置
const reset = () => {
  currentPage.value = 1
  dataList.value = generateData(1)
  loadStatus.value = 'default'
}
</script>

<style lang="scss" scoped>
.load-more-demo {
  min-height: 100vh;
  background: #f5f6fa;
  display: flex;
  flex-direction: column;
}

.page-header {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  padding: 40rpx;
  text-align: center;
  flex-shrink: 0;
  
  .page-title {
    display: block;
    font-size: 44rpx;
    font-weight: bold;
    color: #fff;
    margin-bottom: 8rpx;
  }
  
  .page-subtitle {
    font-size: 26rpx;
    color: rgba(255,255,255,0.8);
  }
}

.content-scroll {
  flex: 1;
  height: calc(100vh - 400rpx);
}

.data-list {
  padding: 20rpx;
  
  .data-item {
    display: flex;
    background: #fff;
    border-radius: 16rpx;
    padding: 24rpx;
    margin-bottom: 20rpx;
    box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.04);
    
    .item-image {
      width: 100rpx;
      height: 100rpx;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      border-radius: 20rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 24rpx;
      flex-shrink: 0;
      
      text {
        font-size: 48rpx;
      }
    }
    
    .item-content {
      flex: 1;
      
      .item-title {
        display: block;
        font-size: 30rpx;
        font-weight: 600;
        color: #333;
        margin-bottom: 12rpx;
      }
      
      .item-desc {
        display: block;
        font-size: 26rpx;
        color: #999;
        margin-bottom: 16rpx;
        line-height: 1.5;
      }
      
      .item-meta {
        display: flex;
        align-items: center;
        
        .meta-tag {
          padding: 6rpx 16rpx;
          background: #f0f5ff;
          color: #5B8FF9;
          font-size: 22rpx;
          border-radius: 8rpx;
          margin-right: 16rpx;
        }
        
        .meta-time {
          font-size: 22rpx;
          color: #bbb;
        }
      }
    }
  }
}

.control-panel {
  background: #fff;
  border-radius: 24rpx 24rpx 0 0;
  padding: 30rpx;
  flex-shrink: 0;
  box-shadow: 0 -4rpx 20rpx rgba(0,0,0,0.08);
  
  .panel-title {
    font-size: 32rpx;
    font-weight: 600;
    color: #333;
    margin-bottom: 24rpx;
    text-align: center;
  }
  
  .control-section {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 20rpx 0;
    border-bottom: 2rpx solid #f5f5f5;
    
    .section-label {
      font-size: 28rpx;
      color: #666;
    }
    
    .status-badge {
      padding: 8rpx 24rpx;
      border-radius: 8rpx;
      font-size: 24rpx;
      font-weight: 500;
      
      &.default {
        background: #f5f5f5;
        color: #999;
      }
      
      &.ready {
        background: #f0f5ff;
        color: #5B8FF9;
      }
      
      &.loading {
        background: #fff7e6;
        color: #faad14;
      }
      
      &.noMore {
        background: #f6ffed;
        color: #52c41a;
      }
      
      &.error {
        background: #fff1f0;
        color: #ff4d4f;
      }
    }
    
    .theme-selector {
      display: flex;
      gap: 16rpx;
      
      .theme-option {
        padding: 12rpx 24rpx;
        background: #f5f5f5;
        border-radius: 8rpx;
        font-size: 24rpx;
        color: #666;
        
        &.active {
          background: #5B8FF9;
          color: #fff;
        }
      }
    }
  }
  
  .control-buttons {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 16rpx;
    margin-top: 24rpx;
    
    .btn {
      padding: 20rpx 0;
      border-radius: 12rpx;
      text-align: center;
      font-size: 26rpx;
      font-weight: 500;
      
      &.primary {
        background: #5B8FF9;
        color: #fff;
      }
      
      &.success {
        background: #52c41a;
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
  
  .stats-section {
    display: flex;
    justify-content: space-around;
    margin-top: 24rpx;
    padding-top: 24rpx;
    border-top: 2rpx solid #f5f5f5;
    
    .stat-item {
      text-align: center;
      
      .stat-value {
        display: block;
        font-size: 36rpx;
        font-weight: bold;
        color: #333;
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
