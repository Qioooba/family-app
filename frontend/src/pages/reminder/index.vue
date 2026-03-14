<template>
  <view class="reminder-page">
    <view class="header">
      <text class="title">提醒管理</text>
      <view class="add-btn" @click="goAdd">
        <text>+</text>
      </view>
    </view>
    
    <view class="reminder-list" v-if="reminders.length > 0">
      <view 
        v-for="(item, index) in reminders" 
        :key="item.id"
        class="reminder-item"
        @click="goDetail(item)"
      >
        <view class="reminder-icon" :style="{ background: getIconBg(item.reminderType) }">
          <text>{{ getIcon(item.reminderType) }}</text>
        </view>
        <view class="reminder-info">
          <text class="reminder-name">{{ item.reminderName }}</text>
          <text class="reminder-time" v-if="item.remindTime">{{ item.remindTime }}</text>
        </view>
        <view class="reminder-switch" @click.stop="toggleStatus(item)">
          <switch :checked="item.status === 1" color="#07c160" />
        </view>
      </view>
    </view>
    
    <view class="empty-state" v-else>
      <text class="empty-text">还没有提醒</text>
      <text class="empty-subtext">点击右上角+添加提醒</text>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      reminders: []
    }
  },
  onLoad() {
    this.loadReminders()
  },
  onShow() {
    this.loadReminders()
  },
  methods: {
    async loadReminders() {
      try {
        const res = await uni.request({
          url: '/api/reminder/list',
          method: 'GET'
        })
        if (res.data && res.data.code === 0) {
          this.reminders = res.data.data || []
        }
      } catch (e) {
        console.error('加载提醒失败', e)
      }
    },
    getIcon(type) {
      const iconMap = {
        'WATER': '💧',
        'MEDICINE': '💊',
        'EXPIRE': '📦',
        'BIRTHDAY': '🎂',
        'FINANCE': '💰',
        'SYSTEM': '⏰'
      }
      return iconMap[type] || '⏰'
    },
    getIconBg(type) {
      const bgMap = {
        'WATER': 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
        'MEDICINE': 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
        'EXPIRE': 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)',
        'BIRTHDAY': 'linear-gradient(135deg, #ff9a9e 0%, #fecfef 100%)',
        'FINANCE': 'linear-gradient(135deg, #30cfd0 0%, #330867 100%)',
        'SYSTEM': 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
      }
      return bgMap[type] || bgMap['SYSTEM']
    },
    goAdd() {
      uni.navigateTo({
        url: '/pages/reminder/add'
      })
    },
    goDetail(item) {
      uni.navigateTo({
        url: `/pages/reminder/detail?id=${item.id}`
      })
    },
    async toggleStatus(item) {
      try {
        await uni.request({
          url: '/api/reminder/toggle',
          method: 'POST',
          data: { id: item.id }
        })
        this.loadReminders()
      } catch (e) {
        console.error('切换状态失败', e)
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.reminder-page {
  min-height: 100vh;
  background: #f5f6fa;
  padding: 20rpx;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx;
  background: #fff;
  border-radius: 16rpx;
  margin-bottom: 20rpx;
}

.title {
  font-size: 32rpx;
  font-weight: 600;
  color: #333;
}

.add-btn {
  width: 60rpx;
  height: 60rpx;
  background: #07c160;
  border-radius: 30rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 36rpx;
}

.reminder-list {
  background: #fff;
  border-radius: 16rpx;
  overflow: hidden;
}

.reminder-item {
  display: flex;
  align-items: center;
  padding: 24rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.reminder-item:last-child {
  border-bottom: none;
}

.reminder-icon {
  width: 80rpx;
  height: 80rpx;
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40rpx;
  margin-right: 20rpx;
}

.reminder-info {
  flex: 1;
}

.reminder-name {
  font-size: 28rpx;
  color: #333;
  font-weight: 500;
  display: block;
}

.reminder-time {
  font-size: 24rpx;
  color: #999;
  margin-top: 8rpx;
  display: block;
}

.empty-state {
  text-align: center;
  padding: 100rpx 0;
}

.empty-text {
  font-size: 28rpx;
  color: #999;
  display: block;
}

.empty-subtext {
  font-size: 24rpx;
  color: #ccc;
  margin-top: 16rpx;
  display: block;
}
</style>
