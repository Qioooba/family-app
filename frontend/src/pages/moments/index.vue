<template>
  <view class="moments-page">
    <!-- 顶部背景 -->
    <view class="header-bg"></view>
    
    <!-- 发布按钮 -->
    <view class="post-btn" @click="showPostModal = true">
      <text>+</text>
    </view>
    
    <!-- 动态列表 -->
    <view class="moments-list">
      <view v-if="loading" class="loading-state">
        <text>加载中...</text>
      </view>
      
      <view v-else-if="moments.length === 0" class="empty-state">
        <text class="empty-icon">📝</text>
        <text class="empty-text">还没有动态</text>
        <text class="empty-sub">发布第一条动态吧</text>
      </view>
      
      <view v-else class="moment-cards">
        <view 
          v-for="moment in moments" 
          :key="moment.id"
          class="moment-card"
        >
          <!-- 用户信息 -->
          <view class="moment-header">
            <view class="user-avatar">
              <text>{{ moment.userAvatar }}</text>
            </view>
            <view class="user-info">
              <text class="user-name">{{ moment.userName }}</text>
              <text class="moment-time">{{ moment.time }}</text>
            </view>
            <view class="more-btn">
              <text>⋮</text>
            </view>
          </view>
          
          <!-- 内容 -->
          <view class="moment-content">
            <text>{{ moment.content }}</text>
          </view>
          
          <!-- 图片 -->
          <view v-if="moment.images && moment.images.length > 0" class="moment-images">
            <image 
              v-for="(img, idx) in moment.images" 
              :key="idx"
              :src="img"
              class="moment-img"
              mode="aspectFill"
              @click="previewImage(moment.images, idx)"
            />
          </view>
          
          <!-- 互动按钮 -->
          <view class="moment-actions">
            <view class="action-item" @click="toggleLike(moment)">
              <text class="action-icon">{{ moment.isLiked ? '❤️' : '🤍' }}</text>
              <text class="action-count">{{ moment.likeCount }}</text>
            </view>
            <view class="action-item" @click="showComment(moment)">
              <text class="action-icon">💬</text>
              <text class="action-count">{{ moment.commentCount }}</text>
            </view>
            <view class="action-item" @click="shareMoment(moment)">
              <text class="action-icon">🔗</text>
            </view>
          </view>
          
          <!-- 评论 -->
          <view v-if="moment.comments && moment.comments.length > 0" class="moment-comments">
            <view v-for="comment in moment.comments" :key="comment.id" class="comment-item">
              <text class="comment-user">{{ comment.userName }}:</text>
              <text class="comment-text">{{ comment.content }}</text>
            </view>
          </view>
        </view>
      </view>
    </view>
    
    <!-- 发布弹窗 -->
    <view v-if="showPostModal" class="modal-overlay" @click="showPostModal = false">
      <view class="modal-content" @click.stop>
        <view class="modal-header">
          <text class="modal-title">发布动态</text>
          <text class="modal-close" @click="showPostModal = false">✕</text>
        </view>
        
        <view class="modal-body">
          <textarea 
            class="content-input" 
            v-model="newMoment.content" 
            placeholder="分享你的生活..."
            maxlength="500"
          />
          
          <view class="image-uploader">
            <view v-for="(img, idx) in newMoment.images" :key="idx" class="uploaded-img">
              <image :src="img" mode="aspectFill" />
              <text class="remove-btn" @click="removeImage(idx)">✕</text>
            </view>
            <view v-if="newMoment.images.length < 9" class="upload-btn" @click="chooseImage">
              <text>+</text>
            </view>
          </view>
        </view>
        
        <view class="modal-footer">
          <view class="cancel-btn" @click="showPostModal = false">
            <text>取消</text>
          </view>
          <view class="submit-btn" @click="postMoment">
            <text>发布</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'

const loading = ref(false)
const showPostModal = ref(false)

const newMoment = ref({
  content: '',
  images: []
})

const moments = ref([
  {
    id: 1,
    userName: '老婆',
    userAvatar: '👩',
    time: '今天 14:30',
    content: '今天天气真好，带宝宝去公园玩沙子啦！🌞',
    images: [],
    likeCount: 5,
    commentCount: 2,
    isLiked: false,
    comments: [
      { id: 1, userName: '老公', content: '玩得开心！' },
      { id: 2, userName: '宝宝', content: '下次还要去！' }
    ]
  },
  {
    id: 2,
    userName: '老公',
    userAvatar: '👨',
    time: '昨天 20:15',
    content: '今天做了顿丰盛的晚餐，红烧肉味道不错！🍖',
    images: [],
    likeCount: 8,
    commentCount: 3,
    isLiked: true,
    comments: [
      { id: 1, userName: '老婆', content: '看起来很好吃的样子' },
      { id: 2, userName: '宝宝', content: '我要吃红烧肉！' },
      { id: 3, userName: '老婆', content: '老公辛苦了 ❤️' }
    ]
  }
])

const chooseImage = () => {
  if (newMoment.value.images.length >= 9) {
    uni.showToast({ title: '最多9张图片', icon: 'none' })
    return
  }
  
  uni.chooseImage({
    count: 9 - newMoment.value.images.length,
    success: (res) => {
      newMoment.value.images.push(...res.tempFilePaths)
    }
  })
}

const removeImage = (idx) => {
  newMoment.value.images.splice(idx, 1)
}

const postMoment = () => {
  if (!newMoment.value.content.trim() && newMoment.value.images.length === 0) {
    uni.showToast({ title: '请输入内容或选择图片', icon: 'none' })
    return
  }
  
  const moment = {
    id: Date.now(),
    userName: '我',
    userAvatar: '😎',
    time: '刚刚',
    content: newMoment.value.content,
    images: newMoment.value.images,
    likeCount: 0,
    commentCount: 0,
    isLiked: false,
    comments: []
  }
  
  moments.value.unshift(moment)
  showPostModal.value = false
  newMoment.value = { content: '', images: [] }
  uni.showToast({ title: '发布成功', icon: 'success' })
}

const toggleLike = (moment) => {
  moment.isLiked = !moment.isLiked
  moment.likeCount += moment.isLiked ? 1 : -1
}

const showComment = (moment) => {
  uni.showToast({ title: '评论功能开发中', icon: 'none' })
}

const shareMoment = (moment) => {
  uni.showToast({ title: '分享功能开发中', icon: 'none' })
}

const previewImage = (images, current) => {
  uni.previewImage({
    urls: images,
    current
  })
}
</script>

<style lang="scss" scoped>
.moments-page {
  min-height: 100vh;
  background: #f8f9fc;
  padding-bottom: 40rpx;
}

.header-bg {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 200rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.post-btn {
  position: fixed;
  bottom: 60rpx;
  right: 40rpx;
  width: 120rpx;
  height: 120rpx;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 12rpx 40rpx rgba(102, 126, 234, 0.4);
  z-index: 100;
  
  text {
    font-size: 60rpx;
    color: #fff;
    font-weight: 300;
  }
}

.moments-list {
  position: relative;
  padding: 32rpx;
}

.empty-state {
  text-align: center;
  padding: 100rpx 0;
  
  .empty-icon {
    font-size: 100rpx;
    display: block;
    margin-bottom: 20rpx;
  }
  
  .empty-text {
    font-size: 32rpx;
    color: #2d3748;
    display: block;
  }
  
  .empty-sub {
    font-size: 26rpx;
    color: #8b9aad;
  }
}

.moment-card {
  background: #fff;
  border-radius: 24rpx;
  padding: 32rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.05);
  
  .moment-header {
    display: flex;
    align-items: center;
    margin-bottom: 20rpx;
    
    .user-avatar {
      width: 80rpx;
      height: 80rpx;
      background: linear-gradient(135deg, #667eea, #764ba2);
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 40rpx;
      margin-right: 20rpx;
    }
    
    .user-info {
      flex: 1;
      
      .user-name {
        font-size: 30rpx;
        font-weight: 600;
        color: #2d3748;
        display: block;
        margin-bottom: 4rpx;
      }
      
      .moment-time {
        font-size: 24rpx;
        color: #8b9aad;
      }
    }
    
    .more-btn {
      padding: 10rpx;
      color: #8b9aad;
      font-size: 32rpx;
    }
  }
  
  .moment-content {
    margin-bottom: 20rpx;
    
    text {
      font-size: 30rpx;
      color: #2d3748;
      line-height: 1.6;
    }
  }
  
  .moment-images {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 8rpx;
    margin-bottom: 20rpx;
    
    .moment-img {
      width: 100%;
      height: 200rpx;
      border-radius: 12rpx;
    }
  }
  
  .moment-actions {
    display: flex;
    gap: 40rpx;
    padding: 20rpx 0;
    border-top: 2rpx solid #f1f5f9;
    border-bottom: 2rpx solid #f1f5f9;
    
    .action-item {
      display: flex;
      align-items: center;
      gap: 8rpx;
      
      .action-icon {
        font-size: 36rpx;
      }
      
      .action-count {
        font-size: 26rpx;
        color: #8b9aad;
      }
    }
  }
  
  .moment-comments {
    margin-top: 16rpx;
    padding: 16rpx;
    background: #f8f9fc;
    border-radius: 16rpx;
    
    .comment-item {
      margin-bottom: 8rpx;
      
      &:last-child {
        margin-bottom: 0;
      }
      
      .comment-user {
        font-size: 26rpx;
        color: #667eea;
        font-weight: 500;
        margin-right: 8rpx;
      }
      
      .comment-text {
        font-size: 26rpx;
        color: #2d3748;
      }
    }
  }
}

// 弹窗样式
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: flex-end;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: #fff;
  border-radius: 40rpx 40rpx 0 0;
  width: 100%;
  max-height: 85vh;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 40rpx;
  
  .modal-title {
    font-size: 36rpx;
    font-weight: 600;
  }
  
  .modal-close {
    font-size: 40rpx;
    color: #8b9aad;
  }
}

.modal-body {
  padding: 0 40rpx 40rpx;
}

.content-input {
  width: 100%;
  height: 200rpx;
  padding: 24rpx;
  background: #f8f9fc;
  border-radius: 20rpx;
  font-size: 30rpx;
  margin-bottom: 24rpx;
}

.image-uploader {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
  
  .uploaded-img {
    position: relative;
    width: 160rpx;
    height: 160rpx;
    border-radius: 16rpx;
    overflow: hidden;
    
    image {
      width: 100%;
      height: 100%;
    }
    
    .remove-btn {
      position: absolute;
      top: 8rpx;
      right: 8rpx;
      width: 36rpx;
      height: 36rpx;
      background: rgba(0,0,0,0.5);
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #fff;
      font-size: 24rpx;
    }
  }
  
  .upload-btn {
    width: 160rpx;
    height: 160rpx;
    background: #f8f9fc;
    border-radius: 16rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    
    text {
      font-size: 60rpx;
      color: #8b9aad;
    }
  }
}

.modal-footer {
  display: flex;
  gap: 24rpx;
  padding: 20rpx 40rpx 60rpx;
  
  .cancel-btn, .submit-btn {
    flex: 1;
    text-align: center;
    padding: 28rpx;
    border-radius: 32rpx;
    font-size: 30rpx;
  }
  
  .cancel-btn {
    background: #f1f5f9;
    color: #5a6c7d;
  }
  
  .submit-btn {
    background: linear-gradient(135deg, #667eea, #764ba2);
    color: #fff;
  }
}
</style>
