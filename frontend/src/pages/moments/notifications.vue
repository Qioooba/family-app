<template>
  <view class="notifications-page">
    <!-- 自定义导航栏 -->
    <view class="custom-nav">
      <view class="nav-back" @click="goBack">
        <text class="icon">‹</text>
      </view>
      <view class="nav-title">通知中心</view>
      <view class="nav-action" @click="markAllRead">
        <text>全部已读</text>
      </view>
    </view>

    <!-- 通知统计 -->
    <view class="notification-stats">
      <view class="stat-item">
        <text class="stat-value">{{ unreadCount }}</text>
        <text class="stat-label">未读消息</text>
      </view>
      <view class="stat-divider"></text>
      <view class="stat-item">
        <text class="stat-value">{{ todayCount }}</text>
        <text class="stat-label">今日消息</text>
      </view>
      <view class="stat-divider"></text>
      <view class="stat-item">
        <text class="stat-value">{{ totalCount }}</text>
        <text class="stat-label">全部消息</text>
      </view>
    </view>

    <!-- 通知类型筛选 -->
    <view class="type-filter">
      <view 
        v-for="type in notificationTypes" 
        :key="type.value"
        class="filter-item"
        :class="{ active: currentType === type.value }"
        @click="currentType = type.value"
      >
        <text class="filter-icon">{{ type.icon }}</text>
        <text class="filter-name">{{ type.label }}</text>
        <text v-if="type.count > 0" class="filter-badge">{{ type.count }}</text>
      </view>
    </view>

    <!-- 通知列表 -->
    <scroll-view class="notification-list" scroll-y @scrolltolower="loadMore">
      <view 
        v-for="(notification, index) in filteredNotifications" 
        :key="notification.id || index"
        class="notification-item"
        :class="{ unread: !notification.isRead }"
        @click="handleNotification(notification)"
      >
        <view class="notification-icon" :class="notification.type">
          <text>{{ getNotificationIcon(notification.type) }}</text>
        </view>
        <view class="notification-content">
          <view class="notification-header">
            <text class="notification-title">{{ notification.title }}</text>
            <text class="notification-time">{{ notification.time }}</text>
          </view>
          <text class="notification-text">{{ notification.content }}</text>
          <view v-if="notification.action" class="notification-action">
            <text>{{ notification.action }}</text>
          </view>
        </view>
        <view v-if="!notification.isRead" class="unread-dot"></text>
      </view>

      <!-- 设置入口 -->
      <view class="settings-entry" @click="goToSettings">
        <text class="icon">⚙️</text>
        <text>通知设置</text>
        <text class="arrow">›</text>
      </view>

      <view class="loading-more" v-if="loading">
        <text>加载中...</text>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'

const loading = ref(false)
const currentType = ref('all')

const notificationTypes = ref([
  { label: '全部', value: 'all', icon: '🔔', count: 0 },
  { label: '系统', value: 'system', icon: '⚙️', count: 2 },
  { label: '评论', value: 'comment', icon: '💬', count: 3 },
  { label: '点赞', value: 'like', icon: '❤️', count: 5 },
  { label: '任务', value: 'task', icon: '✓', count: 1 }
])

const notifications = ref([
  {
    id: 1,
    type: 'system',
    title: '系统通知',
    content: '欢迎使用家庭助手APP，开启智慧家庭生活！',
    time: '10分钟前',
    isRead: false
  },
  {
    id: 2,
    type: 'comment',
    title: '妈妈 评论了你的动态',
    content: '看起来真好吃，晚上我也要试试！',
    time: '30分钟前',
    isRead: false
  },
  {
    id: 3,
    type: 'like',
    title: '爸爸 赞了你的照片',
    content: '周末一起去玩的照片真好看',
    time: '1小时前',
    isRead: true
  },
  {
    id: 4,
    type: 'task',
    title: '任务提醒',
    content: '您有一个任务即将截止：整理客厅',
    time: '2小时前',
    isRead: false,
    action: '查看任务'
  },
  {
    id: 5,
    type: 'system',
    title: '家庭公告',
    content: '新的家庭公告：周末家庭聚餐通知',
    time: '3小时前',
    isRead: true,
    action: '查看公告'
  },
  {
    id: 6,
    type: 'comment',
    title: '宝贝 回复了你的评论',
    content: '谢谢妈妈！我下次还要考100分',
    time: '昨天',
    isRead: true
  }
])

const filteredNotifications = computed(() => {
  if (currentType.value === 'all') return notifications.value
  return notifications.value.filter(n => n.type === currentType.value)
})

const unreadCount = computed(() => notifications.value.filter(n => !n.isRead).length)
const todayCount = computed(() => notifications.value.filter(n => n.time.includes('小时') || n.time.includes('分钟')).length)
const totalCount = computed(() => notifications.value.length)

const getNotificationIcon = (type) => {
  const icons = {
    system: '⚙️',
    comment: '💬',
    like: '❤️',
    task: '✓'
  }
  return icons[type] || '🔔'
}

const goBack = () => {
  uni.navigateBack()
}

const markAllRead = () => {
  notifications.value.forEach(n => n.isRead = true)
  uni.showToast({ title: '已全部标记为已读', icon: 'none' })
}

const handleNotification = (notification) => {
  notification.isRead = true
  if (notification.action) {
    uni.showToast({ title: `跳转: ${notification.action}`, icon: 'none' })
  }
}

const goToSettings = () => {
  uni.navigateTo({ url: '/pages/moments/notification-settings' })
}

const loadMore = () => {
  loading.value = true
  setTimeout(() => {
    loading.value = false
  }, 500)
}
</script>

<style lang="scss" scoped>
.notifications-page {
  min-height: 100vh;
  background: #0f0f23;
}

.custom-nav {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 50px 20px 20px;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);

  .nav-back {
    width: 36px;
    height: 36px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: rgba(255,255,255,0.1);
    border-radius: 10px;

    .icon {
      font-size: 24px;
      color: #fff;
    }
  }

  .nav-title {
    font-size: 18px;
    font-weight: 600;
    color: #fff;
  }

  .nav-action {
    padding: 8px 12px;
    background: rgba(255,255,255,0.1);
    border-radius: 8px;
    font-size: 13px;
    color: #3b82f6;
  }
}

.notification-stats {
  display: flex;
  align-items: center;
  justify-content: space-around;
  margin: 15px;
  padding: 20px;
  background: rgba(255,255,255,0.05);
  border-radius: 16px;

  .stat-item {
    text-align: center;

    .stat-value {
      display: block;
      font-size: 24px;
      font-weight: 700;
      color: #fff;
      margin-bottom: 4px;
    }

    .stat-label {
      font-size: 12px;
      color: #64748b;
    }
  }

  .stat-divider {
    width: 1px;
    height: 30px;
    background: rgba(255,255,255,0.1);
  }
}

.type-filter {
  display: flex;
  gap: 10px;
  padding: 0 15px 15px;
  overflow-x: auto;

  .filter-item {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 10px 16px;
    background: rgba(255,255,255,0.05);
    border-radius: 20px;
    white-space: nowrap;
    transition: all 0.3s;

    &.active {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    }

    .filter-icon {
      font-size: 14px;
    }

    .filter-name {
      font-size: 13px;
      color: #fff;
    }

    .filter-badge {
      min-width: 18px;
      height: 18px;
      padding: 0 6px;
      background: #ef4444;
      border-radius: 9px;
      font-size: 11px;
      color: #fff;
      display: flex;
      align-items: center;
      justify-content: center;
    }
  }
}

.notification-list {
  padding: 0 15px 30px;
}

.notification-item {
  display: flex;
  gap: 12px;
  padding: 16px;
  background: rgba(255,255,255,0.03);
  border-radius: 12px;
  margin-bottom: 10px;
  position: relative;

  &.unread {
    background: rgba(59,130,246,0.05);
    border: 1px solid rgba(59,130,246,0.1);
  }

  .notification-icon {
    width: 44px;
    height: 44px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 20px;
    flex-shrink: 0;

    &.system {
      background: rgba(245,158,11,0.2);
    }
    &.comment {
      background: rgba(59,130,246,0.2);
    }
    &.like {
      background: rgba(236,72,153,0.2);
    }
    &.task {
      background: rgba(34,197,94,0.2);
    }
  }

  .notification-content {
    flex: 1;
    min-width: 0;

    .notification-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 6px;

      .notification-title {
        font-size: 14px;
        font-weight: 600;
        color: #fff;
      }

      .notification-time {
        font-size: 12px;
        color: #64748b;
      }
    }

    .notification-text {
      font-size: 13px;
      color: #94a3b8;
      line-height: 1.5;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
    }

    .notification-action {
      margin-top: 8px;
      padding: 6px 12px;
      background: rgba(59,130,246,0.1);
      border-radius: 6px;
      display: inline-block;

      text {
        font-size: 12px;
        color: #3b82f6;
      }
    }
  }

  .unread-dot {
    position: absolute;
    top: 16px;
    right: 16px;
    width: 8px;
    height: 8px;
    background: #ef4444;
    border-radius: 50%;
  }
}

.settings-entry {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  margin-top: 20px;
  background: rgba(255,255,255,0.03);
  border-radius: 12px;

  .icon {
    font-size: 20px;
  }

  text {
    flex: 1;
    font-size: 14px;
    color: #fff;
  }

  .arrow {
    font-size: 18px;
    color: #64748b;
  }
}

.loading-more {
  text-align: center;
  padding: 20px;
  color: #64748b;
  font-size: 13px;
}
</style>
