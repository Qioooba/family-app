<template>
  <view class="page-container">
    <view class="header">
      <view class="header-left" @click="goBack"><text>‹</text></text>
      <view class="header-title">协作编辑</view>
    </view>
    
    <view class="collab-info"
>
      <view class="online-users"
>
        <text class="users-label">在线成员</text>
        <view class="users-avatars"
>
          <view v-for="user in onlineUsers" :key="user.id" class="user-avatar" :style="{ background: user.color }">
            {{ user.name[0] }}
          </view>
          <view class="user-count">+{{ onlineUsers.length }}</view>
        </view>
      </view>
    </view>
    
    <view class="edit-area"
>
      <input class="title-input" v-model="taskTitle" placeholder="任务标题" />
      <textarea class="content-input" v-model="taskContent" placeholder="任务描述..." />
    </view>
    
    <view class="collab-actions"
>
      <view class="action-btn" @click="invite"
邀请</view>
      <view class="action-btn primary" @click="save"
保存</view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'

const onlineUsers = ref([
  { id: 1, name: '我', color: '#3B82F6' },
  { id: 2, name: '妈妈', color: '#EC4899' }
])

const taskTitle = ref('')
const taskContent = ref('')

const invite = () => {
  uni.showActionSheet({
    itemList: ['微信邀请', '复制链接'],
    success: (res) => {
      uni.showToast({ title: '邀请已发送', icon: 'none' })
    }
  })
}

const save = () => {
  uni.showToast({ title: '已保存', icon: 'success' })
}

const goBack = () => uni.navigateBack()
</script>

<style lang="scss" scoped>
.page-container { min-height: 100vh; background: #F8FAFC; }
.header {
  display: flex; justify-content: space-between; align-items: center;
  padding: 50px 20px 20px;
  background: linear-gradient(135deg, #3B82F6, #2563EB);
  .header-left { width: 40px; height: 40px; display: flex; align-items: center; justify-content: center; }
  .header-left text { font-size: 32px; color: #fff; }
  .header-title { font-size: 20px; font-weight: 600; color: #fff; }
}
.collab-info { padding: 20px; }
.online-users { display: flex; justify-content: space-between; align-items: center; background: #fff; border-radius: 16px; padding: 16px; }
.users-label { font-size: 15px; font-weight: 600; color: #1F2937; }
.users-avatars { display: flex; align-items: center; }
.user-avatar { width: 36px; height: 36px; border-radius: 50%; display: flex; align-items: center; justify-content: center; color: #fff; font-size: 14px; font-weight: 600; margin-left: -8px; border: 2px solid #fff; }
.user-count { margin-left: 8px; font-size: 13px; color: #6B7280; background: #F3F4F6; padding: 4px 10px; border-radius: 12px; }
.edit-area { padding: 0 20px 20px; }
.title-input { width: 100%; height: 56px; background: #fff; border-radius: 16px; padding: 0 20px; font-size: 18px; font-weight: 600; margin-bottom: 12px; }
.content-input { width: 100%; min-height: 200px; background: #fff; border-radius: 16px; padding: 20px; font-size: 15px; }
.collab-actions { display: flex; gap: 16px; padding: 0 20px; }
.action-btn { flex: 1; height: 50px; background: #F3F4F6; border-radius: 25px; display: flex; align-items: center; justify-content: center; font-size: 16px; color: #6B7280; }
.action-btn.primary { background: linear-gradient(135deg, #3B82F6, #2563EB); color: #fff; }
</style>
