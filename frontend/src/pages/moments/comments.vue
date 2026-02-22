<template>
  <view class="moments-comments">
    <view class="header">
      <text class="title">评论</text>
    </view>
    <view class="comment-list">
      <view class="comment-item" v-for="item in comments" :key="item.id">
        <image class="avatar" :src="item.avatar" mode="aspectFill" />
        <view class="comment-content">
          <text class="username">{{ item.username }}</text>
          <text class="text">{{ item.content }}</text>
          <text class="time">{{ item.time }}</text>
        </view>
      </view>
    </view>
    <view class="input-bar">
      <input class="input" placeholder="写评论..." v-model="newComment" />
      <button class="send" @click="sendComment">发送</button>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'

const newComment = ref('')
const comments = ref([
  { id: 1, username: '用户1', avatar: '/static/avatar.png', content: '真不错！', time: '2分钟前' },
  { id: 2, username: '用户2', avatar: '/static/avatar.png', content: '支持一下', time: '5分钟前' }
])

const sendComment = () => {
  if (!newComment.value.trim()) return
  comments.value.push({
    id: Date.now(),
    username: '我',
    avatar: '/static/avatar.png',
    content: newComment.value,
    time: '刚刚'
  })
  newComment.value = ''
}
</script>

<style scoped>
.moments-comments {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 120rpx;
}
.header {
  padding: 40rpx;
  background: #fff;
}
.title {
  font-size: 36rpx;
  font-weight: bold;
}
.comment-list {
  padding: 20rpx;
}
.comment-item {
  display: flex;
  padding: 20rpx;
  background: #fff;
  border-radius: 12rpx;
  margin-bottom: 20rpx;
}
.avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  margin-right: 20rpx;
}
.comment-content {
  flex: 1;
}
.username {
  font-weight: bold;
  color: #5B8FF9;
}
.text {
  display: block;
  margin: 10rpx 0;
}
.time {
  font-size: 24rpx;
  color: #999;
}
.input-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  padding: 20rpx;
  background: #fff;
  border-top: 1rpx solid #eee;
}
.input {
  flex: 1;
  padding: 20rpx;
  background: #f5f5f5;
  border-radius: 30rpx;
  margin-right: 20rpx;
}
.send {
  padding: 20rpx 40rpx;
  background: #5B8FF9;
  color: #fff;
  border-radius: 30rpx;
  font-size: 28rpx;
}
</style>