<template>
  <view class="page">
    <!-- 顶部导航 -->
    <view class="nav-bar">
      <view class="back-btn" @click="goBack">
        <text class="icon">‹</text>
      </view>
      <text class="title">提醒管理</text>
      <view class="right-btn" @click="goAdd">
        <text class="icon">+</text>
      </view>
    </view>
    
    <!-- 统计卡片 -->
    <view class="stats-card">
      <view class="stat-item">
        <text class="stat-num">{{ activeCount }}</text>
        <text class="stat-label">启用中</text>
      </view>
      <view class="stat-divider"></view>
      <view class="stat-item">
        <text class="stat-num">{{ reminders.length }}</text>
        <text class="stat-label">总计</text>
      </view>
      <view class="stat-divider"></view>
      <view class="stat-item">
        <text class="stat-num">{{ todayCount }}</text>
        <text class="stat-label">今日</text>
      </view>
    </view>
    
    <!-- 提醒列表 -->
    <scroll-view class="reminder-list" scroll-y @scrolltolower="loadMore">
      <view 
        v-for="item in reminders" 
        :key="item.id"
        class="reminder-card"
        :class="{ disabled: item.status !== 1 }"
        @click="goDetail(item)"
      >
        <view class="card-left">
          <view class="type-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%)">
            <text>⏰</text>
          </view>
          <view class="status-dot" v-if="item.status === 1"></view>
        </view>
        
        <view class="card-body">
          <view class="card-header">
            <text class="reminder-name">{{ item.reminderName }}</text>
          </view>
          <view class="card-info">
            <text class="frequency">{{ formatFrequency(item) }}</text>
            <text class="next-time" v-if="item.nextExecuteTime">
              下次: {{ formatTime(item.nextExecuteTime) }}
            </text>
          </view>
          <view class="card-tags" v-if="showTags(item)">
            <text v-if="item.pushScope === 'ALL'" class="tag tag-all">全员</text>
            <text v-if="item.pushScope === 'SPECIFIED'" class="tag tag-specified">指定</text>
            <text v-if="hasQuietHours(item)" class="tag tag-quiet">免打扰</text>
            <text v-if="isWorkDaysOnly(item)" class="tag tag-workday">工作日</text>
          </view>
        </view>
        
        <view class="card-right" @click.stop="toggleStatus(item)">
          <switch 
            :checked="item.status === 1" 
            color="#667eea"
            @change="onSwitchChange($event, item)"
          />
        </view>
      </view>
      
      <!-- 空状态 -->
      <view v-if="reminders.length === 0 && !loading" class="empty-state">
        <text class="empty-icon">⏰</text>
        <text class="empty-title">暂无提醒</text>
        <text class="empty-desc">点击下方按钮创建第一个提醒</text>
      </view>
      
      <!-- 加载更多 -->
      <view v-if="loading" class="loading-more">
        <text>加载中...</text>
      </view>
    </scroll-view>
    
    <!-- 底部操作栏 -->
    <view class="bottom-bar">
      <button class="add-btn" @click="goAdd">
        <text class="add-icon">+</text>
        <text>新建提醒</text>
      </button>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      reminders: [],
      loading: false,
      page: 1,
      pageSize: 20,
      hasMore: true
    }
  },
  computed: {
    activeCount() {
      return this.reminders.filter(r => r.status === 1).length
    },
    todayCount() {
      const today = new Date().toISOString().split('T')[0]
      return this.reminders.filter(r => {
        if (!r.nextExecuteTime) return false
        return r.nextExecuteTime.includes(today)
      }).length
    }
  },
  onLoad() {
    this.loadReminders()
  },
  onShow() {
    this.loadReminders()
  },
  onPullDownRefresh() {
    this.page = 1
    this.hasMore = true
    this.loadReminders().finally(() => {
      uni.stopPullDownRefresh()
    })
  },
  methods: {
    // 加载提醒列表
    async loadReminders() {
      if (this.loading) return
      this.loading = true
      
      try {
        const res = await uni.request({
          url: '/api/reminder/list',
          method: 'GET'
        })
        
        if (res.data?.code === 0) {
          this.reminders = res.data.data || []
        } else {
          uni.showToast({ title: res.data?.message || '加载失败', icon: 'none' })
        }
      } catch (e) {
        console.error('加载提醒失败', e)
        uni.showToast({ title: '加载失败', icon: 'none' })
      } finally {
        this.loading = false
      }
    },
    
    // 加载更多
    loadMore() {
      // 如果需要分页，这里实现加载更多逻辑
    },
    
    // 切换状态
    async toggleStatus(item) {
      try {
        const res = await uni.request({
          url: '/api/reminder/toggle',
          method: 'POST',
          data: { id: item.id }
        })
        
        if (res.data?.code === 0) {
          item.status = item.status === 1 ? 0 : 1
          uni.showToast({ 
            title: item.status === 1 ? '已启用' : '已停用', 
            icon: 'none' 
          })
        } else {
          uni.showToast({ title: res.data?.message || '操作失败', icon: 'none' })
        }
      } catch (e) {
        console.error('切换状态失败', e)
        uni.showToast({ title: '操作失败', icon: 'none' })
      }
    },
    
    // Switch 组件变更处理
    onSwitchChange(e, item) {
      // 阻止默认行为，手动调用 toggle
      this.toggleStatus(item)
    },
    
    // 获取图标
    getIcon(type) {
      const map = {
        WATER: '💧',
        MEDICINE: '💊',
        EXPIRE: '📦',
        BIRTHDAY: '🎂',
        FINANCE: '💰',
        SYSTEM: '⏰'
      }
      return map[type] || '⏰'
    },
    
    // 获取图标背景色
    getIconBg(type) {
      const map = {
        WATER: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
        MEDICINE: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
        EXPIRE: 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)',
        BIRTHDAY: 'linear-gradient(135deg, #ff9a9e 0%, #fecfef 100%)',
        FINANCE: 'linear-gradient(135deg, #30cfd0 0%, #330867 100%)',
        SYSTEM: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
      }
      return map[type] || map.SYSTEM
    },
    
    // 获取类型文本
    getTypeText(type) {
      const map = {
        WATER: '喝水',
        MEDICINE: '用药',
        EXPIRE: '保质期',
        BIRTHDAY: '生日',
        FINANCE: '财务',
        SYSTEM: '系统'
      }
      return map[type] || '提醒'
    },
    
    // 格式化频率
    formatFrequency(item) {
      const typeMap = {
        ONCE: '一次性',
        DAILY: '每天',
        HOURLY: '每小时',
        WEEKLY: '每周',
        MONTHLY: '每月',
        YEARLY: '每年',
        INTERVAL: '间隔'
      }
      
      let text = typeMap[item.frequencyType] || item.frequencyType
      
      // 解析频率配置获取具体时间
      try {
        const config = JSON.parse(item.frequencyConfig || '{}')
        if (config.fixedTime) {
          text += ` ${config.fixedTime}`
        } else if (config.intervalMinutes) {
          text += ` ${config.intervalMinutes}分钟`
        } else if (config.intervalHours) {
          text += ` ${config.intervalHours}小时`
        } else if (config.intervalDays) {
          text += ` ${config.intervalDays}天`
        }
      } catch (e) {}
      
      return text
    },
    
    // 格式化时间
    formatTime(timeStr) {
      if (!timeStr) return '-'
      const date = new Date(timeStr.replace(/-/g, '/'))
      const now = new Date()
      const isToday = date.toDateString() === now.toDateString()
      const isTomorrow = new Date(now.getTime() + 86400000).toDateString() === date.toDateString()
      
      const hours = date.getHours().toString().padStart(2, '0')
      const minutes = date.getMinutes().toString().padStart(2, '0')
      const time = `${hours}:${minutes}`
      
      if (isToday) return `今天 ${time}`
      if (isTomorrow) return `明天 ${time}`
      return `${date.getMonth() + 1}/${date.getDate()} ${time}`
    },
    
    // 显示标签
    showTags(item) {
      return item.pushScope === 'ALL' || 
             item.pushScope === 'SPECIFIED' || 
             this.hasQuietHours(item) || 
             this.isWorkDaysOnly(item)
    },
    
    // 是否有免打扰设置
    hasQuietHours(item) {
      return item.quietHoursStart && item.quietHoursEnd
    },
    
    // 是否仅工作日
    isWorkDaysOnly(item) {
      try {
        const config = JSON.parse(item.frequencyConfig || '{}')
        return config.workDaysOnly === true
      } catch (e) {
        return false
      }
    },
    
    // 导航
    goBack() {
      uni.navigateBack()
    },
    goAdd() {
      uni.navigateTo({ url: '/pages/reminder/add' })
    },
    goDetail(item) {
      uni.navigateTo({ url: `/pages/reminder/detail?id=${item.id}` })
    }
  }
}
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: #f5f6fa;
  display: flex;
  flex-direction: column;
}

// 导航栏
.nav-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 60rpx 30rpx 20rpx;
  background: #fff;
  
  .back-btn, .right-btn {
    width: 60rpx;
    height: 60rpx;
    display: flex;
    align-items: center;
    justify-content: center;
  }
  
  .icon {
    font-size: 48rpx;
    color: #333;
  }
  
  .title {
    font-size: 34rpx;
    font-weight: 600;
    color: #333;
  }
}

// 统计卡片
.stats-card {
  display: flex;
  align-items: center;
  justify-content: space-around;
  margin: 20rpx;
  padding: 30rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 20rpx;
  
  .stat-item {
    display: flex;
    flex-direction: column;
    align-items: center;
  }
  
  .stat-num {
    font-size: 48rpx;
    font-weight: 700;
    color: #fff;
  }
  
  .stat-label {
    font-size: 24rpx;
    color: rgba(255,255,255,0.8);
    margin-top: 8rpx;
  }
  
  .stat-divider {
    width: 2rpx;
    height: 60rpx;
    background: rgba(255,255,255,0.3);
  }
}

// 列表
.reminder-list {
  flex: 1;
  padding: 0 20rpx;
  padding-bottom: 120rpx;
}

.reminder-card {
  display: flex;
  align-items: center;
  background: #fff;
  padding: 24rpx;
  border-radius: 16rpx;
  margin-bottom: 16rpx;
  box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.04);
  
  &.disabled {
    opacity: 0.6;
  }
}

.card-left {
  position: relative;
  margin-right: 20rpx;
  
  .type-icon {
    width: 88rpx;
    height: 88rpx;
    border-radius: 20rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 44rpx;
  }
  
  .status-dot {
    position: absolute;
    top: -4rpx;
    right: -4rpx;
    width: 20rpx;
    height: 20rpx;
    background: #52c41a;
    border-radius: 50%;
    border: 4rpx solid #fff;
  }
}

.card-body {
  flex: 1;
  min-width: 0;
}

.card-header {
  display: flex;
  align-items: center;
  margin-bottom: 8rpx;
  
  .reminder-name {
    font-size: 30rpx;
    font-weight: 600;
    color: #333;
    margin-right: 12rpx;
  }
  
  .reminder-type {
    font-size: 22rpx;
    color: #667eea;
    background: rgba(102,126,234,0.1);
    padding: 4rpx 12rpx;
    border-radius: 8rpx;
  }
}

.card-info {
  display: flex;
  flex-direction: column;
  gap: 4rpx;
  
  .frequency {
    font-size: 26rpx;
    color: #666;
  }
  
  .next-time {
    font-size: 24rpx;
    color: #999;
  }
}

.card-tags {
  display: flex;
  gap: 8rpx;
  margin-top: 12rpx;
  flex-wrap: wrap;
  
  .tag {
    font-size: 20rpx;
    padding: 4rpx 12rpx;
    border-radius: 8rpx;
  }
  
  .tag-all {
    background: #e6f7ff;
    color: #1890ff;
  }
  
  .tag-specified {
    background: #f6ffed;
    color: #52c41a;
  }
  
  .tag-quiet {
    background: #fff7e6;
    color: #fa8c16;
  }
  
  .tag-workday {
    background: #f9f0ff;
    color: #722ed1;
  }
}

.card-right {
  margin-left: 16rpx;
}

// 空状态
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 100rpx 40rpx;
  
  .empty-icon {
    font-size: 120rpx;
    margin-bottom: 20rpx;
  }
  
  .empty-title {
    font-size: 32rpx;
    font-weight: 600;
    color: #333;
    margin-bottom: 12rpx;
  }
  
  .empty-desc {
    font-size: 26rpx;
    color: #999;
  }
}

// 底部栏
.bottom-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  padding: 20rpx 30rpx;
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
  background: #fff;
  border-top: 1rpx solid #f0f0f0;
}

.add-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  width: 100%;
  height: 88rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 44rpx;
  color: #fff;
  font-size: 30rpx;
  font-weight: 500;
  
  .add-icon {
    font-size: 36rpx;
  }
}

.loading-more {
  text-align: center;
  padding: 30rpx;
  color: #999;
  font-size: 24rpx;
}
</style>
