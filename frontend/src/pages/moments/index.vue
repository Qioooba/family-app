<template>
  <view class="moments-page">
    <!-- 头部 -->
    <view class="moments-header">
      <view class="header-title">家庭圈</view>
      <view class="header-actions">
        <view class="action-btn" @click="showNotifications">
          <text class="icon">🔔</text>
          <view v-if="unreadCount > 0" class="badge">{{ unreadCount }}</view>
        </view>
        <view class="action-btn primary" @click="createMoment"
        >
          <text class="icon">✏️</text>
          <text>发布</text>
        </view>
      </view>
    </view>

    <!-- 公告栏 -->
    <view class="announcement-bar" v-if="announcement">
      <text class="announcement-icon">📢</text>
      <text class="announcement-text">{{ announcement.title }}</text>
      <text class="announcement-more">查看 ›</text>
    </view>

    <!-- 动态列表 -->
    <scroll-view class="moments-list" scroll-y @scrolltolower="loadMore"
    >
      <view v-for="(moment, index) in moments" :key="index" class="moment-card"
      >
        <!-- 发布者信息 -->
        <view class="moment-header"
        >
          <image class="avatar" :src="moment.userAvatar" mode="aspectFill" />
          <view class="user-info"
          >
            <text class="nickname">{{ moment.userName }}</text>
            <text class="time">{{ moment.createTime }}</text>
          </view>
          <view class="more-btn">⋯</view>
        </view>

        <!-- 内容 -->
        <view class="moment-content"
        >
          <text class="content-text">{{ moment.content }}</text>
          
          <!-- 图片网格 -->
          <view class="image-grid" v-if="moment.images && moment.images.length > 0"
          >
            <image 
              v-for="(img, imgIndex) in moment.images" 
              :key="imgIndex"
              class="grid-image"
              :class="{ 'single': moment.images.length === 1 }"
              :src="img" 
              mode="aspectFill"
              @click="previewImage(moment.images, imgIndex)"
            />
          </view>
          
          <!-- 位置 -->
          <view class="location" v-if="moment.location"
          >
            <text class="icon">📍</text>
            <text>{{ moment.location }}</text>
          </view>
        </view>

        <!-- 互动栏 -->
        <view class="moment-actions"
        >
          <view 
            class="action-item" 
            :class="{ active: moment.isLiked }"
            @click="toggleLike(moment)"
          >
            <text class="icon">{{ moment.isLiked ? '❤️' : '🤍' }}</text>
            <text>{{ moment.likeCount || '点赞' }}</text>
          </view>
          
          <view class="action-item" @click="showComments(moment)"
          >
            <text class="icon">💬</text>
            <text>{{ moment.commentCount || '评论' }}</text>
          </view>
          
          <view class="action-item"
          >
            <text class="icon">↗️</text>
            <text>分享</text>
          </view>
        </view>

        <!-- 评论预览 -->
        <view class="comments-preview" v-if="moment.comments && moment.comments.length > 0"
        >
          <view 
            v-for="(comment, cIndex) in moment.comments.slice(0, 2)" 
            :key="cIndex"
            class="comment-item"
          >
            <text class="comment-user">{{ comment.userName }}：</text>
            <text class="comment-content">{{ comment.content }}</text>
          </view>
          <view 
            class="more-comments" 
            v-if="moment.comments.length > 2"
            @click="showComments(moment)"
          >
            查看全部 {{ moment.comments.length }} 条评论
          </view>
        </view>
      </view>
      
      <view class="loading-more" v-if="loading"
      >
        <text>加载中...</text>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref } from 'vue'

const unreadCount = ref(3)
const loading = ref(false)

const announcement = ref({
  title: '🎉 周末家庭聚餐，周六晚上6点，记得准时参加哦！',
  id: 1
})

const moments = ref([
  {
    id: 1,
    userName: '妈妈',
    userAvatar: '/static/avatar/mom.png',
    createTime: '10分钟前',
    content: '今天做了红烧肉，孩子们都说好吃！😋 配方：五花肉500g，生抽2勺，老抽1勺，冰糖适量...',
    images: ['/static/food/hongshaorou.jpg'],
    location: '家里厨房',
    likeCount: 8,
    commentCount: 3,
    isLiked: true,
    comments: [
      { userName: '爸爸', content: '确实好吃，配了两碗饭！' },
      { userName: '宝贝', content: '妈妈最棒！' }
    ]
  },
  {
    id: 2,
    userName: '爸爸',
    userAvatar: '/static/avatar/dad.png',
    createTime: '1小时前',
    content: '周末带孩子去动物园玩了一天，累并快乐着！🦁🐯🐼',
    images: ['/static/photos/zoo1.jpg', '/static/photos/zoo2.jpg', '/static/photos/zoo3.jpg'],
    location: '市动物园',
    likeCount: 15,
    commentCount: 5,
    isLiked: false
  },
  {
    id: 3,
    userName: '宝贝',
    userAvatar: '/static/avatar/baby.png',
    createTime: '3小时前',
    content: '今天数学考了100分！💯 开心！',
    likeCount: 20,
    commentCount: 8,
    isLiked: true
  }
])

const createMoment = () => {
  uni.navigateTo({ url: '/pages/moments/create' })
}

const showNotifications = () => {
  uni.navigateTo({ url: '/pages/moments/notifications' })
}

const toggleLike = (moment) => {
  moment.isLiked = !moment.isLiked
  moment.likeCount += moment.isLiked ? 1 : -1
}

const showComments = (moment) => {
  uni.navigateTo({ url: `/pages/moments/comments?id=${moment.id}` })
}

const previewImage = (images, current) => {
  uni.previewImage({ urls: images, current: images[current] })
}

const loadMore = () => {
  loading.value = true
  setTimeout(() => {
    loading.value = false
  }, 1000)
}
</script>

<style lang="scss" scoped>
.moments-page {
  min-height: 100vh;
  background: #0f0f23;
}

.moments-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 50px 20px 20px;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
  
  .header-title {
    font-size: 24px;
    font-weight: 700;
    color: #fff;
  }
  
  .header-actions {
    display: flex;
    gap: 12px;
  }
  
  .action-btn {
    position: relative;
    padding: 10px;
    background: rgba(255,255,255,0.1);
    border-radius: 12px;
    
    .icon {
      font-size: 20px;
    }
    
    &.primary {
      display: flex;
      align-items: center;
      gap: 6px;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: #fff;
      font-size: 14px;
      padding: 10px 16px;
    }
    
    .badge {
      position: absolute;
      top: -4px;
      right: -4px;
      width: 18px;
      height: 18px;
      background: #ef4444;
      border-radius: 50%;
      font-size: 11px;
      color: #fff;
      display: flex;
      align-items: center;
      justify-content: center;
    }
  }
}

.announcement-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 15px;
  padding: 12px 15px;
  background: linear-gradient(135deg, rgba(245,158,11,0.2) 0%, rgba(217,119,6,0.1) 100%);
  border-radius: 12px;
  border: 1px solid rgba(245,158,11,0.3);
  
  .announcement-icon {
    font-size: 18px;
  }
  
  .announcement-text {
    flex: 1;
    font-size: 13px;
    color: #fbbf24;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
  
  .announcement-more {
    font-size: 12px;
    color: #f59e0b;
  }
}

.moments-list {
  padding: 0 15px 100px;
}

.moment-card {
  margin-bottom: 15px;
  padding: 20px;
  background: rgba(255,255,255,0.05);
  border-radius: 20px;
  border: 1px solid rgba(255,255,255,0.08);
}

.moment-header {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
  
  .avatar {
    width: 44px;
    height: 44px;
    border-radius: 50%;
    margin-right: 12px;
  }
  
  .user-info {
    flex: 1;
    
    .nickname {
      display: block;
      font-size: 15px;
      font-weight: 600;
      color: #fff;
    }
    
    .time {
      font-size: 12px;
      color: #64748b;
      margin-top: 4px;
    }
  }
  
  .more-btn {
    font-size: 20px;
    color: #64748b;
    padding: 5px;
  }
}

.moment-content {
  margin-bottom: 15px;
  
  .content-text {
    font-size: 15px;
    color: #e2e8f0;
    line-height: 1.7;
    margin-bottom: 12px;
  }
  
  .image-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 6px;
    margin-bottom: 12px;
    
    .grid-image {
      aspect-ratio: 1;
      border-radius: 8px;
      
      &.single {
        grid-column: span 2;
        aspect-ratio: 16/9;
      }
    }
  }
  
  .location {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 12px;
    color: #3b82f6;
    
    .icon {
      font-size: 14px;
    }
  }
}

.moment-actions {
  display: flex;
  gap: 30px;
  padding-top: 15px;
  border-top: 1px solid rgba(255,255,255,0.08);
  
  .action-item {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 13px;
    color: #64748b;
    
    .icon {
      font-size: 18px;
    }
    
    &.active {
      color: #ec4899;
    }
  }
}

.comments-preview {
  margin-top: 15px;
  padding: 12px;
  background: rgba(255,255,255,0.03);
  border-radius: 12px;
  
  .comment-item {
    margin-bottom: 8px;
    font-size: 13px;
    line-height: 1.5;
    
    &:last-child {
      margin-bottom: 0;
    }
  }
  
  .comment-user {
    color: #3b82f6;
    font-weight: 500;
  }
  
  .comment-content {
    color: #94a3b8;
  }
  
  .more-comments {
    margin-top: 8px;
    font-size: 13px;
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
