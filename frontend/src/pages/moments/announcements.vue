<template>
  <view class="announcements-page">
    <!-- 自定义导航栏 -->
    <view class="custom-navbar">
      <view class="navbar-back" @click="goBack">
        <text class="icon">←</text>
      </view>
      <view class="navbar-title">家庭公告</view>
      <view v-if="isAdmin" class="navbar-action" @click="createAnnouncement"
      >
        <text class="icon">✏️</text>
      </view>
      <view v-else class="navbar-placeholder"></view>
    </view>

    <!-- 置顶公告区 -->
    <view v-if="pinnedAnnouncements.length > 0" class="pinned-section">
      <view class="section-title">
        <text class="icon">📌</text>
        <text>置顶公告</text>
      </view>
      <view 
        v-for="item in pinnedAnnouncements" 
        :key="item.id"
        class="announcement-card pinned"
        @click="showDetail(item)"
      >
        <view class="card-header"
        >
          <view class="announcement-type" :class="item.type">
            <text>{{ getTypeLabel(item.type) }}</text>
          </view>
          <view v-if="isExpiringSoon(item)" class="expire-tag">
            <text>即将过期</text>
          </view>
        </view>
        <view class="card-title">{{ item.title }}</view>
        <view class="card-content">{{ item.summary }}</view>
        
        <view v-if="item.images && item.images.length > 0" class="card-images"
        >
          <image 
            v-for="(img, index) in item.images.slice(0, 3)" 
            :key="index"
            class="preview-image"
            :src="img" 
            mode="aspectFill"
            @click.stop="previewImage(item.images, index)"
          />
          <view v-if="item.images.length > 3" class="image-more"
          >+{{ item.images.length - 3 }}</view>
        </view>
        
        <view class="card-footer"
        >
          <view class="author-info"
          >
            <image class="author-avatar" :src="item.authorAvatar" mode="aspectFill" />
            <text class="author-name">{{ item.authorName }}</text>
          </view>
          <view class="meta-info"
          >
            <text class="meta-item"
            >
              <text class="icon">👁️</text> {{ item.viewCount }}
            </text>
            <text class="meta-item"
            >
              <text class="icon">💬</text> {{ item.commentCount }}
            </text>
            <text class="meta-item"
            >
              <text class="icon">⏰</text> {{ formatExpireTime(item.expireTime) }}
            </text>
          </view>
        </view>
      </view>
    </view>

    <!-- 普通公告列表 -->
    <scroll-view 
      class="announcements-list" 
      scroll-y
      @scrolltolower="loadMore"
      :lower-threshold="100"
      enable-back-to-top
    >
      <view class="section-title"
      >
        <text class="icon">📋</text>
        <text>全部公告</text>
      </view>

      <!-- 空状态 -->
      <view v-if="normalAnnouncements.length === 0" class="empty-state"
      >
        <text class="empty-icon">📭</text>
        <text class="empty-text">暂无公告</text>
        <text v-if="isAdmin" class="empty-tip">点击右上角发布第一条公告吧</text>
      </view>

      <view 
        v-for="item in normalAnnouncements" 
        :key="item.id"
        class="announcement-card"
        @click="showDetail(item)"
      >
        <view class="card-header"
        >
          <view class="announcement-type" :class="item.type"
          >
            <text>{{ getTypeLabel(item.type) }}</text>
          </view>
          <view v-if="isExpiringSoon(item)" class="expire-tag"
          >
            <text>即将过期</text>
          </view>
          <view v-if="item.isNew" class="new-tag"
          >NEW</view>
        </view>
        
        <view class="card-title"
        >{{ item.title }}</view>
        <view class="card-content"
        >{{ item.summary }}</view>
        
        <view class="card-footer"
        >
          <view class="author-info"
          >
            <image class="author-avatar" :src="item.authorAvatar" mode="aspectFill" />
            <text class="author-name">{{ item.authorName }}</text>
            <text class="publish-time">{{ item.publishTime }}</text>
          </view>
          <view class="meta-info"
          >
            <text class="meta-item"
            >👁️ {{ item.viewCount }}</text>
          </view>
        </view>
      </view>
      
      <view v-if="loading" class="loading-more"
      >
        <text>加载中...</text>
      </view>
    </scroll-view>

    <!-- 公告详情弹窗 -->
    <view v-if="currentDetail" class="detail-modal" @click="closeDetail"
    >
      <scroll-view class="detail-content" scroll-y @click.stop>
        <view class="detail-header"
        >
          <view class="detail-type" :class="currentDetail.type"
          >{{ getTypeLabel(currentDetail.type) }}</view>
          <text class="detail-close" @click="closeDetail">✕</text>
        </view>
        
        <view class="detail-title"
        >{{ currentDetail.title }}</view>
        
        <view class="detail-meta"
        >
          <image class="meta-avatar" :src="currentDetail.authorAvatar" mode="aspectFill" />
          <view class="meta-info"
          >
            <text class="meta-name">{{ currentDetail.authorName }}</text>
            <text class="meta-time"
            >{{ currentDetail.publishTime }} · 有效期至 {{ formatExpireTime(currentDetail.expireTime) }}</text>
          </view>
        </view>
        
        <view class="detail-body"
        >
          <text class="detail-text"
          >{{ currentDetail.content }}</text>
          
          <view v-if="currentDetail.images && currentDetail.images.length > 0" class="detail-images"
          >
            <image 
              v-for="(img, index) in currentDetail.images" 
              :key="index"
              class="detail-image"
              :src="img" 
              mode="widthFix"
              @click="previewImage(currentDetail.images, index)"
            />
          </view>
        </view>
        
        <!-- 操作栏 -->
        <view class="detail-actions"
        >
          <view 
            class="action-btn" 
            :class="{ active: currentDetail.isLiked }"
            @click="toggleLike(currentDetail)"
          >
            <text class="icon"
            >{{ currentDetail.isLiked ? '❤️' : '🤍' }}</text>
            <text>{{ currentDetail.likeCount || '点赞' }}</text>
          </view>
          <view class="action-btn" @click="showCommentInput"
          >
            <text class="icon">💬</text>
            <text>评论</text>
          </view>
          <view class="action-btn" @click="shareAnnouncement"
          
          >
            <text class="icon">↗️</text>
            <text>分享</text>
          </view>
        </view>
        
        <!-- 评论列表 -->
        <view v-if="currentDetail.comments && currentDetail.comments.length > 0" class="comments-section"
        >
          <view class="comments-header"
          >评论 ({{ currentDetail.comments.length }})</view>
          <view 
            v-for="(comment, index) in currentDetail.comments" 
            :key="index"
            class="comment-item"
          >
            <image class="comment-avatar" :src="comment.avatar" mode="aspectFill" />
            <view class="comment-content"
            >
              <text class="comment-name"
              >{{ comment.name }}</text>
              <text class="comment-text"
              >{{ comment.content }}</text>
              <text class="comment-time"
              >{{ comment.time }}</text>
            </view>
          </view>
        </view>
      </scroll-view>
    </view>

    <!-- 发布公告弹窗 -->
    <view v-if="showCreateModal" class="create-modal" @click="closeCreateModal"
    >
      <view class="create-content" @click.stop>
        <view class="create-header"
        >
          <text class="create-title">发布公告</text>
          <text class="create-close" @click="closeCreateModal">✕</text>
        </view>
        
        <scroll-view class="create-body" scroll-y>
          <!-- 公告类型 -->
          <view class="form-item"
          >
            <text class="form-label">公告类型</text>
            <view class="type-options"
            >
              <view 
                v-for="type in announcementTypes" 
                :key="type.value"
                class="type-option"
                :class="{ active: newAnnouncement.type === type.value }"
                @click="newAnnouncement.type = type.value"
              >
                <text>{{ type.label }}</text>
              </view>
            </view>
          </view>
          
          <!-- 标题 -->
          <view class="form-item"
          >
            <text class="form-label">标题</text>
            <input 
              class="form-input" 
              v-model="newAnnouncement.title"
              placeholder="请输入公告标题"
              maxlength="50"
            />
          </view>
          
          <!-- 内容 -->
          <view class="form-item"
          >
            <text class="form-label">内容</text>
            <textarea 
              class="form-textarea" 
              v-model="newAnnouncement.content"
              placeholder="请输入公告内容"
              maxlength="500"
            />
          </view>
          
          <!-- 有效期 -->
          <view class="form-item"
          >
            <text class="form-label">有效期</text>
            <picker mode="selector" :range="expireOptions" @change="onExpireChange"
            >
              <view class="picker-value"
              >{{ expireOptions[newAnnouncement.expireIndex] }}</view>
            </picker>
          </view>
          
          <!-- 置顶设置 -->
          <view class="form-item"
          >
            <text class="form-label">置顶公告</text>
            <switch :checked="newAnnouncement.isPinned" @change="togglePinned" color="#667eea" />
          </view>
        </scroll-view>
        
        <view class="create-footer"
        >
          <view class="btn-cancel" @click="closeCreateModal">取消</view>
          <view class="btn-submit" @click="submitAnnouncement">发布</view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'

const isAdmin = ref(true) // 实际应从用户信息获取
const loading = ref(false)
const currentDetail = ref(null)
const showCreateModal = ref(false)

const announcementTypes = [
  { label: '普通', value: 'normal' },
  { label: '重要', value: 'important' },
  { label: '紧急', value: 'urgent' },
  { label: '活动', value: 'activity' }
]

const expireOptions = ['3天', '7天', '15天', '30天', '永久']

const newAnnouncement = ref({
  type: 'normal',
  title: '',
  content: '',
  expireIndex: 1,
  isPinned: false
})

const announcements = ref([
  {
    id: 1,
    type: 'important',
    title: '🎉 周末家庭聚餐通知',
    summary: '本周六晚上6点，全家一起去吃海底捞，记得准时参加哦！',
    content: '亲爱的家人们，\n\n本周六晚上6点，全家一起去吃海底捞庆祝爸爸升职！\n\n时间：周六 18:00\n地点：市中心海底捞\n注意事项：\n1. 请准时到达\n2. 可以带一位朋友\n3. 穿漂亮点，我们要拍照留念\n\n期待与大家共度美好时光！',
    authorName: '妈妈',
    authorAvatar: '../../static/avatar/mom.png',
    publishTime: '今天 10:00',
    expireTime: new Date(Date.now() + 2 * 24 * 60 * 60 * 1000).toISOString(),
    viewCount: 12,
    commentCount: 5,
    likeCount: 8,
    isLiked: false,
    isPinned: true,
    isNew: false,
    images: ['../../static/food/haidilao.jpg'],
    comments: [
      { name: '爸爸', avatar: '../../static/avatar/dad.png', content: '太好了！我一定准时到', time: '10:05' },
      { name: '宝贝', avatar: '../../static/avatar/baby.png', content: '我要吃虾滑！', time: '10:12' }
    ]
  },
  {
    id: 2,
    type: 'urgent',
    title: '⚠️ 停水通知',
    summary: '明天上午9点至下午5点小区停水，请提前储水',
    content: '各位家人，\n\n物业通知：明天上午9点至下午5点进行管道维修，届时将停水。\n\n请大家提前储水，做好生活安排。\n\n如有疑问请联系物业：12345678',
    authorName: '爸爸',
    authorAvatar: '../../static/avatar/dad.png',
    publishTime: '昨天 20:30',
    expireTime: new Date(Date.now() + 1 * 24 * 60 * 60 * 1000).toISOString(),
    viewCount: 15,
    commentCount: 2,
    likeCount: 3,
    isLiked: true,
    isPinned: true,
    isNew: false
  },
  {
    id: 3,
    type: 'activity',
    title: '🏃 家庭运动会报名',
    summary: '下月家庭运动会开始报名啦！项目丰富，奖品多多',
    content: '家人们，\n\n一年一度的家庭运动会即将开始！\n\n项目：\n- 羽毛球\n- 乒乓球\n- 跳绳\n- 家庭接力\n\n奖品：\n一等奖：家庭影院套装\n二等奖：空气净化器\n三等奖：智能音箱\n\n报名方式：私信妈妈',
    authorName: '妈妈',
    authorAvatar: '../../static/avatar/mom.png',
    publishTime: '2天前',
    expireTime: new Date(Date.now() + 20 * 24 * 60 * 60 * 1000).toISOString(),
    viewCount: 20,
    commentCount: 8,
    likeCount: 15,
    isLiked: false,
    isPinned: false,
    isNew: true
  },
  {
    id: 4,
    type: 'normal',
    title: '📚 图书馆还书提醒',
    summary: '宝贝借的书本周五到期，记得去还',
    content: '宝贝，\n\n你上个月借的《哈利波特》本周五到期，记得去图书馆还书或者续借哦。\n\n逾期会有罚款的~',
    authorName: '爸爸',
    authorAvatar: '../../static/avatar/dad.png',
    publishTime: '3天前',
    expireTime: new Date(Date.now() + 5 * 24 * 60 * 60 * 1000).toISOString(),
    viewCount: 8,
    commentCount: 1,
    likeCount: 2,
    isLiked: false,
    isPinned: false,
    isNew: false
  },
  {
    id: 5,
    type: 'important',
    title: '💰 家庭预算会议',
    summary: '本周日晚上8点召开家庭财务会议，讨论下月预算',
    content: '各位，\n\n本周日晚上8点召开家庭财务会议，讨论下个月的家庭预算分配。\n\n主要议题：\n1. 下月生活费预算\n2. 宝贝兴趣班费用\n3. 家庭旅游基金\n4. 其他大额支出\n\n请大家提前准备。',
    authorName: '爸爸',
    authorAvatar: '../../static/avatar/dad.png',
    publishTime: '5天前',
    expireTime: new Date(Date.now() + 3 * 24 * 60 * 60 * 1000).toISOString(),
    viewCount: 10,
    commentCount: 3,
    likeCount: 5,
    isLiked: false,
    isPinned: false,
    isNew: false
  }
])

const pinnedAnnouncements = computed(() => {
  return announcements.value.filter(item => item.isPinned)
})

const normalAnnouncements = computed(() => {
  return announcements.value.filter(item => !item.isPinned)
})

const getTypeLabel = (type) => {
  const labels = {
    normal: '普通',
    important: '重要',
    urgent: '紧急',
    activity: '活动'
  }
  return labels[type] || '普通'
}

const isExpiringSoon = (item) => {
  const expireDate = new Date(item.expireTime)
  const now = new Date()
  const diffHours = (expireDate - now) / (1000 * 60 * 60)
  return diffHours > 0 && diffHours < 48
}

const formatExpireTime = (expireTime) => {
  const date = new Date(expireTime)
  const now = new Date()
  const diffDays = Math.ceil((date - now) / (1000 * 60 * 60 * 24))
  
  if (diffDays <= 0) return '已过期'
  if (diffDays === 1) return '明天'
  if (diffDays <= 7) return `${diffDays}天后`
  return `${date.getMonth() + 1}月${date.getDate()}日`
}

const goBack = () => {
  uni.navigateBack()
}

const createAnnouncement = () => {
  if (!isAdmin.value) {
    uni.showToast({ title: '只有管理员可以发布公告', icon: 'none' })
    return
  }
  showCreateModal.value = true
}

const closeCreateModal = () => {
  showCreateModal.value = false
  newAnnouncement.value = {
    type: 'normal',
    title: '',
    content: '',
    expireIndex: 1,
    isPinned: false
  }
}

const onExpireChange = (e) => {
  newAnnouncement.value.expireIndex = e.detail.value
}

const togglePinned = () => {
  newAnnouncement.value.isPinned = !newAnnouncement.value.isPinned
}

const submitAnnouncement = () => {
  if (!newAnnouncement.value.title.trim()) {
    uni.showToast({ title: '请输入标题', icon: 'none' })
    return
  }
  if (!newAnnouncement.value.content.trim()) {
    uni.showToast({ title: '请输入内容', icon: 'none' })
    return
  }
  
  const expireDays = [3, 7, 15, 30, 365][newAnnouncement.value.expireIndex]
  const expireTime = new Date(Date.now() + expireDays * 24 * 60 * 60 * 1000).toISOString()
  
  const announcement = {
    id: Date.now(),
    ...newAnnouncement.value,
    summary: newAnnouncement.value.content.slice(0, 50) + '...',
    authorName: '我',
    authorAvatar: '../../static/avatar/me.png',
    publishTime: '刚刚',
    expireTime,
    viewCount: 0,
    commentCount: 0,
    likeCount: 0,
    isLiked: false,
    isNew: true
  }
  
  announcements.value.unshift(announcement)
  closeCreateModal()
  uni.showToast({ title: '发布成功', icon: 'success' })
}

const showDetail = (item) => {
  currentDetail.value = item
  item.viewCount++
}

const closeDetail = () => {
  currentDetail.value = null
}

const previewImage = (images, current) => {
  uni.previewImage({ urls: images, current: images[current] })
}

const toggleLike = (item) => {
  item.isLiked = !item.isLiked
  item.likeCount += item.isLiked ? 1 : -1
}

const showCommentInput = () => {
  uni.showToast({ title: '评论功能开发中', icon: 'none' })
}

const shareAnnouncement = () => {
  uni.showShareMenu({
    withShareTicket: true,
    menus: ['shareAppMessage', 'shareTimeline']
  })
}

const loadMore = () => {
  loading.value = true
  setTimeout(() => {
    loading.value = false
  }, 500)
}
</script>

<style lang="scss" scoped>
.announcements-page {
  min-height: 100vh;
  background: #0f0f23;
  display: flex;
  flex-direction: column;
}

.custom-navbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 50px 20px 15px;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
  
  .navbar-back {
    width: 36px;
    height: 36px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: rgba(255,255,255,0.1);
    border-radius: 10px;
    
    .icon {
      font-size: 18px;
      color: #fff;
    }
  }
  
  .navbar-title {
    font-size: 18px;
    font-weight: 600;
    color: #fff;
  }
  
  .navbar-action {
    width: 36px;
    height: 36px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 10px;
    
    .icon {
      font-size: 16px;
    }
  }
  
  .navbar-placeholder {
    width: 36px;
  }
}

.pinned-section {
  padding: 15px 15px 0;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  
  .icon {
    font-size: 16px;
  }
  
  text {
    font-size: 14px;
    font-weight: 600;
    color: #94a3b8;
  }
}

.announcement-card {
  margin-bottom: 12px;
  padding: 16px;
  background: rgba(255,255,255,0.05);
  border-radius: 16px;
  border: 1px solid rgba(255,255,255,0.08);
  
  &.pinned {
    background: rgba(245,158,11,0.08);
    border: 1px solid rgba(245,158,11,0.2);
  }
  
  .card-header {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 10px;
  }
  
  .announcement-type {
    padding: 4px 10px;
    border-radius: 6px;
    font-size: 11px;
    
    &.normal {
      background: rgba(148,163,184,0.2);
      color: #94a3b8;
    }
    
    &.important {
      background: rgba(59,130,246,0.2);
      color: #3b82f6;
    }
    
    &.urgent {
      background: rgba(239,68,68,0.2);
      color: #ef4444;
    }
    
    &.activity {
      background: rgba(16,185,129,0.2);
      color: #10b981;
    }
  }
  
  .expire-tag {
    padding: 2px 8px;
    background: rgba(245,158,11,0.2);
    border-radius: 4px;
    
    text {
      font-size: 10px;
      color: #f59e0b;
    }
  }
  
  .new-tag {
    padding: 2px 6px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 4px;
    font-size: 9px;
    color: #fff;
  }
  
  .card-title {
    font-size: 15px;
    font-weight: 600;
    color: #fff;
    margin-bottom: 8px;
    line-height: 1.4;
  }
  
  .card-content {
    font-size: 13px;
    color: #94a3b8;
    line-height: 1.6;
    margin-bottom: 12px;
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
  }
  
  .card-images {
    display: flex;
    gap: 8px;
    margin-bottom: 12px;
    
    .preview-image {
      width: 80px;
      height: 80px;
      border-radius: 8px;
      object-fit: cover;
    }
    
    .image-more {
      width: 80px;
      height: 80px;
      background: rgba(255,255,255,0.1);
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 14px;
      color: #94a3b8;
    }
  }
  
  .card-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-top: 12px;
    border-top: 1px solid rgba(255,255,255,0.08);
  }
  
  .author-info {
    display: flex;
    align-items: center;
    gap: 8px;
    
    .author-avatar {
      width: 24px;
      height: 24px;
      border-radius: 50%;
    }
    
    .author-name {
      font-size: 12px;
      color: #64748b;
    }
    
    .publish-time {
      font-size: 11px;
      color: #475569;
      margin-left: 8px;
    }
  }
  
  .meta-info {
    display: flex;
    gap: 12px;
    
    .meta-item {
      font-size: 11px;
      color: #64748b;
      
      .icon {
        margin-right: 2px;
      }
    }
  }
}

.announcements-list {
  flex: 1;
  padding: 15px;
  overflow-y: auto;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 0;
  
  .empty-icon {
    font-size: 60px;
    margin-bottom: 20px;
  }
  
  .empty-text {
    font-size: 16px;
    color: #64748b;
    margin-bottom: 8px;
  }
  
  .empty-tip {
    font-size: 12px;
    color: #475569;
  }
}

.loading-more {
  text-align: center;
  padding: 20px;
  color: #64748b;
  font-size: 13px;
}

/* 详情弹窗 */
.detail-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.8);
  z-index: 1000;
  
  .detail-content {
    height: 100%;
    padding: 50px 20px 20px;
    
    .detail-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20px;
    }
    
    .detail-type {
      padding: 6px 14px;
      border-radius: 8px;
      font-size: 12px;
      
      &.normal {
        background: rgba(148,163,184,0.2);
        color: #94a3b8;
      }
      
      &.important {
        background: rgba(59,130,246,0.2);
        color: #3b82f6;
      }
      
      &.urgent {
        background: rgba(239,68,68,0.2);
        color: #ef4444;
      }
      
      &.activity {
        background: rgba(16,185,129,0.2);
        color: #10b981;
      }
    }
    
    .detail-close {
      width: 36px;
      height: 36px;
      display: flex;
      align-items: center;
      justify-content: center;
      background: rgba(255,255,255,0.1);
      border-radius: 50%;
      font-size: 18px;
      color: #fff;
    }
    
    .detail-title {
      font-size: 20px;
      font-weight: 700;
      color: #fff;
      line-height: 1.4;
      margin-bottom: 16px;
    }
    
    .detail-meta {
      display: flex;
      align-items: center;
      gap: 12px;
      margin-bottom: 24px;
      padding-bottom: 16px;
      border-bottom: 1px solid rgba(255,255,255,0.08);
      
      .meta-avatar {
        width: 40px;
        height: 40px;
        border-radius: 50%;
      }
      
      .meta-info {
        display: flex;
        flex-direction: column;
        
        .meta-name {
          font-size: 14px;
          font-weight: 600;
          color: #fff;
        }
        
        .meta-time {
          font-size: 12px;
          color: #64748b;
          margin-top: 4px;
        }
      }
    }
    
    .detail-body {
      .detail-text {
        font-size: 15px;
        color: #e2e8f0;
        line-height: 1.8;
        white-space: pre-wrap;
      }
      
      .detail-images {
        display: flex;
        flex-direction: column;
        gap: 12px;
        margin-top: 20px;
        
        .detail-image {
          width: 100%;
          border-radius: 12px;
        }
      }
    }
    
    .detail-actions {
      display: flex;
      gap: 20px;
      margin-top: 24px;
      padding-top: 20px;
      border-top: 1px solid rgba(255,255,255,0.08);
      
      .action-btn {
        flex: 1;
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 8px;
        padding: 12px;
        background: rgba(255,255,255,0.05);
        border-radius: 12px;
        
        .icon {
          font-size: 18px;
        }
        
        text {
          font-size: 14px;
          color: #94a3b8;
        }
        
        &.active {
          background: rgba(236,72,153,0.1);
          
          text {
            color: #ec4899;
          }
        }
      }
    }
    
    .comments-section {
      margin-top: 24px;
      padding-top: 20px;
      border-top: 1px solid rgba(255,255,255,0.08);
      
      .comments-header {
        font-size: 14px;
        font-weight: 600;
        color: #fff;
        margin-bottom: 16px;
      }
      
      .comment-item {
        display: flex;
        gap: 12px;
        margin-bottom: 16px;
        
        .comment-avatar {
          width: 36px;
          height: 36px;
          border-radius: 50%;
        }
        
        .comment-content {
          flex: 1;
          
          .comment-name {
            display: block;
            font-size: 13px;
            font-weight: 600;
            color: #3b82f6;
            margin-bottom: 4px;
          }
          
          .comment-text {
            display: block;
            font-size: 13px;
            color: #e2e8f0;
            line-height: 1.5;
            margin-bottom: 4px;
          }
          
          .comment-time {
            font-size: 11px;
            color: #64748b;
          }
        }
      }
    }
  }
}

/* 发布弹窗 */
.create-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.8);
  display: flex;
  align-items: flex-end;
  z-index: 1001;
  
  .create-content {
    width: 100%;
    height: 85vh;
    background: #1a1a2e;
    border-radius: 24px 24px 0 0;
    display: flex;
    flex-direction: column;
    animation: slideUp 0.3s ease;
    
    @keyframes slideUp {
      from {
        transform: translateY(100%);
      }
      to {
        transform: translateY(0);
      }
    }
    
    .create-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 20px;
      border-bottom: 1px solid rgba(255,255,255,0.08);
      
      .create-title {
        font-size: 18px;
        font-weight: 600;
        color: #fff;
      }
      
      .create-close {
        font-size: 20px;
        color: #64748b;
        padding: 5px;
      }
    }
    
    .create-body {
      flex: 1;
      padding: 20px;
      overflow-y: auto;
      
      .form-item {
        margin-bottom: 20px;
        
        .form-label {
          display: block;
          font-size: 14px;
          font-weight: 600;
          color: #fff;
          margin-bottom: 10px;
        }
        
        .type-options {
          display: flex;
          gap: 10px;
          flex-wrap: wrap;
          
          .type-option {
            padding: 8px 16px;
            background: rgba(255,255,255,0.05);
            border-radius: 8px;
            border: 1px solid rgba(255,255,255,0.1);
            
            text {
              font-size: 13px;
              color: #94a3b8;
            }
            
            &.active {
              background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
              border-color: transparent;
              
              text {
                color: #fff;
              }
            }
          }
        }
        
        .form-input {
          width: 100%;
          height: 44px;
          padding: 0 14px;
          background: rgba(255,255,255,0.05);
          border-radius: 10px;
          border: 1px solid rgba(255,255,255,0.1);
          color: #fff;
          font-size: 14px;
        }
        
        .form-textarea {
          width: 100%;
          height: 120px;
          padding: 12px 14px;
          background: rgba(255,255,255,0.05);
          border-radius: 10px;
          border: 1px solid rgba(255,255,255,0.1);
          color: #fff;
          font-size: 14px;
        }
        
        .picker-value {
          padding: 12px 14px;
          background: rgba(255,255,255,0.05);
          border-radius: 10px;
          font-size: 14px;
          color: #fff;
        }
      }
    }
    
    .create-footer {
      display: flex;
      gap: 12px;
      padding: 15px 20px;
      border-top: 1px solid rgba(255,255,255,0.08);
      
      .btn-cancel {
        flex: 1;
        padding: 12px;
        text-align: center;
        background: rgba(255,255,255,0.05);
        border-radius: 10px;
        font-size: 14px;
        color: #94a3b8;
      }
      
      .btn-submit {
        flex: 1;
        padding: 12px;
        text-align: center;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        border-radius: 10px;
        font-size: 14px;
        color: #fff;
        font-weight: 600;
      }
    }
  }
}
</style>
