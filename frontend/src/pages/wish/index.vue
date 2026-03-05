<template>
  <view class="page-container">
    <view class="header">
      <view class="header-title">心愿墙 💝</view>
      <view class="header-action" @click="showAddModal">
        <text class="icon">+</text>
      </view>
    </view>
    
    <view class="filter-tabs">
      <view 
        v-for="(tab, index) in tabs" 
        :key="index"
        class="filter-tab"
        :class="{ active: currentTab === index }"
        @click="currentTab = index"
      >
        <text>{{ tab }}</text>
      </view>
    </view>
    
    <scroll-view class="wish-list" scroll-y>
      <view 
        v-for="(wish, index) in filteredWishes" 
        :key="index"
        class="wish-card"
        @click="viewWish(wish)"
      >
        <view class="wish-header">
          <view class="wish-icon">{{ wishIcons[wish.type] || '🎁' }}</view>
          <view class="wish-content">
            <view class="wish-title">{{ wish.title }}</view>
            <view class="wish-desc">{{ wish.description }}</view>
          </view>
        </view>
        
        <view class="wish-footer">
          <view class="wish-user">
            <image class="user-avatar" :src="wish.userAvatar" />
            <text>{{ wish.userName }}</text>
          </view>
          
          <view class="wish-actions">
            <view v-if="wish.claimerName" class="claimed-badge">
              <text>✓ {{ wish.claimerName }} 已认领</text>
            </view>
            <view v-else class="claim-btn" @click.stop="claimWish(wish)">
              <text>认领</text>
            </view>
          </view>
        </view>
        
        <view v-if="wish.progress > 0" class="progress-bar">
          <view class="progress-fill" :style="{ width: wish.progress + '%' }"></view>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'

const tabs = ['全部心愿', '我的心愿', '已认领', '已实现']
const currentTab = ref(0)

const wishIcons = {
  item: '🎁',
  experience: '✈️',
  goal: '🎯',
  custom: '💫'
}

const wishes = ref([
  {
    id: 1,
    title: '想要一个新书包',
    description: '开学需要一个新的双肩包，容量大一点',
    type: 'item',
    userName: '宝贝',
    userAvatar: '../../static/avatar/baby.png',
    claimerName: '爸爸',
    progress: 100,
    status: 1
  },
  {
    id: 2,
    title: '周末去动物园',
    description: '想去看大熊猫和长颈鹿',
    type: 'experience',
    userName: '宝贝',
    userAvatar: '../../static/avatar/baby.png',
    claimerName: '',
    progress: 0,
    status: 0
  },
  {
    id: 3,
    title: '学会骑自行车',
    description: '希望这个暑假能学会骑车',
    type: 'goal',
    userName: '宝贝',
    userAvatar: '../../static/avatar/baby.png',
    claimerName: '妈妈',
    progress: 60,
    status: 1
  }
])

const filteredWishes = computed(() => {
  if (currentTab.value === 0) return wishes.value
  if (currentTab.value === 1) return wishes.value.filter(w => w.userName === '宝贝')
  if (currentTab.value === 2) return wishes.value.filter(w => w.claimerName)
  if (currentTab.value === 3) return wishes.value.filter(w => w.progress === 100)
  return wishes.value
})

const viewWish = (wish) => {
  uni.showModal({
    title: wish.title,
    content: wish.description,
    showCancel: true,
    confirmText: wish.claimerName ? '查看进度' : '认领心愿',
    cancelText: '关闭',
    success: (res) => {
      if (res.confirm && !wish.claimerName) {
        claimWish(wish)
      }
    }
  })
}

const claimWish = (wish) => {
  if (wish.claimerName) {
    uni.showToast({ title: '已被' + wish.claimerName + '认领', icon: 'none' })
    return
  }
  
  uni.showModal({
    title: '确认认领',
    content: `确定要认领"${wish.title}"吗？`,
    success: (res) => {
      if (res.confirm) {
        wish.claimerName = '我'
        wish.progress = 10
        uni.showToast({ title: '认领成功！', icon: 'success' })
      }
    }
  })
}

const showAddModal = () => {
  uni.showModal({
    title: '添加心愿',
    editable: true,
    placeholderText: '输入心愿内容...',
    success: (res) => {
      if (res.confirm && res.content) {
        wishes.value.unshift({
          id: Date.now(),
          title: res.content,
          description: '',
          type: 'custom',
          userName: '我',
          userAvatar: '../../static/avatar/me.png',
          claimerName: '',
          progress: 0,
          status: 0
        })
        uni.showToast({ title: '添加成功', icon: 'success' })
      }
    }
  })
}
</script>

<style lang="scss" scoped>
.page-container {
  min-height: 100vh;
  background: #F5F7FA;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 50px 20px 20px;
  background: linear-gradient(135deg, #FCE4EC, #F8BBD9);
  
  .header-title {
    font-size: 24px;
    font-weight: 700;
    color: #2C3E50;
  }
  
  .header-action {
    width: 44px;
    height: 44px;
    background: #E91E63;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    
    .icon {
      font-size: 28px;
      color: #fff;
      font-weight: 300;
    }
  }
}

.filter-tabs {
  display: flex;
  padding: 15px;
  background: #fff;
  gap: 10px;
  
  .filter-tab {
    flex: 1;
    padding: 10px;
    text-align: center;
    background: #F5F7FA;
    border-radius: 20px;
    font-size: 13px;
    color: #7F8C8D;
    
    &.active {
      background: #E91E63;
      color: #fff;
    }
  }
}

.wish-list {
  padding: 15px;
  height: calc(100vh - 200px);
}

.wish-card {
  background: #fff;
  border-radius: 16px;
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
}

.wish-header {
  display: flex;
  gap: 12px;
  margin-bottom: 12px;
  
  .wish-icon {
    width: 48px;
    height: 48px;
    background: #FCE4EC;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 24px;
  }
  
  .wish-content {
    flex: 1;
    
    .wish-title {
      font-size: 16px;
      font-weight: 600;
      color: #2C3E50;
      margin-bottom: 6px;
    }
    
    .wish-desc {
      font-size: 13px;
      color: #7F8C8D;
    }
  }
}

.wish-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  
  .wish-user {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 13px;
    color: #7F8C8D;
    
    .user-avatar {
      width: 24px;
      height: 24px;
      border-radius: 50%;
    }
  }
  
  .wish-actions {
    .claimed-badge {
      padding: 6px 12px;
      background: #E8F5E9;
      color: #4CAF50;
      border-radius: 12px;
      font-size: 12px;
    }
    
    .claim-btn {
      padding: 6px 16px;
      background: #E91E63;
      color: #fff;
      border-radius: 12px;
      font-size: 13px;
    }
  }
}

.progress-bar {
  height: 6px;
  background: #E0E6ED;
  border-radius: 3px;
  overflow: hidden;
  
  .progress-fill {
    height: 100%;
    background: linear-gradient(90deg, #E91E63, #F06292);
    border-radius: 3px;
    transition: width 0.3s;
  }
}
</style>
