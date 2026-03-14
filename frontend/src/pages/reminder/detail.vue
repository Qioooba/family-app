<template>
  <view class="detail-page">
    <view class="card">
      <view class="reminder-icon" :style="{ background: getIconBg(reminder.reminderType) }">
        <text>{{ getIcon(reminder.reminderType) }}</text>
      </view>
      <text class="reminder-name">{{ reminder.reminderName }}</text>
      <text class="reminder-time" v-if="reminder.remindTime">{{ reminder.remindTime }}</text>
    </view>
    
    <view class="info-list">
      <view class="info-item">
        <text class="info-label">提醒类型</text>
        <text class="info-value">{{ getTypeName(reminder.reminderType) }}</text>
      </view>
      <view class="info-item">
        <text class="info-label">状态</text>
        <text class="info-value" :class="{ active: reminder.status === 1 }">
          {{ reminder.status === 1 ? '已启用' : '已停用' }}
        </text>
      </view>
      <view class="info-item">
        <text class="info-label">创建时间</text>
        <text class="info-value">{{ reminder.createTime || '-' }}</text>
      </view>
    </view>
    
    <view class="btn-group">
      <button class="btn-delete" @click="deleteReminder">删除提醒</button>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      id: null,
      reminder: {}
    }
  },
  onLoad(options) {
    if (options.id) {
      this.id = options.id
      this.loadDetail()
    }
  },
  methods: {
    async loadDetail() {
      // 简化：重新加载列表
      try {
        const res = await uni.request({
          url: '/api/reminder/list',
          method: 'GET'
        })
        if (res.data && res.data.code === 0) {
          const list = res.data.data || []
          this.reminder = list.find(r => r.id === this.id) || {}
        }
      } catch (e) {
        console.error('加载详情失败', e)
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
    getTypeName(type) {
      const nameMap = {
        'WATER': '喝水提醒',
        'MEDICINE': '用药提醒',
        'EXPIRE': '保质期提醒',
        'BIRTHDAY': '生日提醒',
        'FINANCE': '财务提醒',
        'SYSTEM': '其他提醒'
      }
      return nameMap[type] || '其他'
    },
    async deleteReminder() {
      uni.showModal({
        title: '确认删除',
        content: '确定要删除这个提醒吗？',
        success: async (res) => {
          if (res.confirm) {
            try {
              await uni.request({
                url: '/api/reminder/delete',
                method: 'POST',
                data: { id: this.id }
              })
              uni.showToast({ title: '已删除' })
              setTimeout(() => {
                uni.navigateBack()
              }, 1000)
            } catch (e) {
              uni.showToast({ title: '删除失败', icon: 'none' })
            }
          }
        }
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.detail-page {
  min-height: 100vh;
  background: #f5f6fa;
  padding: 20rpx;
}

.card {
  background: #fff;
  border-radius: 16rpx;
  padding: 60rpx 30rpx;
  text-align: center;
}

.reminder-icon {
  width: 120rpx;
  height: 120rpx;
  border-radius: 30rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 60rpx;
  margin: 0 auto 30rpx;
}

.reminder-name {
  font-size: 36rpx;
  font-weight: 600;
  color: #333;
  display: block;
}

.reminder-time {
  font-size: 28rpx;
  color: #999;
  margin-top: 16rpx;
  display: block;
}

.info-list {
  background: #fff;
  border-radius: 16rpx;
  margin-top: 20rpx;
  padding: 0 30rpx;
}

.info-item {
  display: flex;
  justify-content: space-between;
  padding: 30rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
}

.info-item:last-child {
  border-bottom: none;
}

.info-label {
  font-size: 28rpx;
  color: #999;
}

.info-value {
  font-size: 28rpx;
  color: #333;
}

.info-value.active {
  color: #07c160;
}

.btn-group {
  margin-top: 60rpx;
  padding: 0 30rpx;
}

.btn-delete {
  width: 100%;
  height: 88rpx;
  line-height: 88rpx;
  background: #ff4d4f;
  color: #fff;
  border-radius: 44rpx;
  font-size: 30rpx;
}
</style>
