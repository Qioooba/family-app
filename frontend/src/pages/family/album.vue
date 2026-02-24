<template>
  <view class="album-page">
    <!-- 自定义导航栏 -->
    <up-navbar title="家庭相册" :autoBack="true" bgColor="#fff" :fixed="true" :placeholder="true"></up-navbar>
    
    <!-- 相册头部统计 -->
    <view class="album-header">
      <view class="album-stats">
        <view class="stat-item">
          <text class="stat-num">{{ totalPhotos }}</text>
          <text class="stat-label">照片</text>
        </view>
        <view class="stat-item">
          <text class="stat-num">{{ albums.length }}</text>
          <text class="stat-label">相册</text>
        </view>
        <view class="stat-item">
          <text class="stat-num">{{ tags.length }}</text>
          <text class="stat-label">标签</text>
        </view>
      </view>
    </view>
    
    <!-- 标签筛选 -->
    <view class="tag-filter">
      <scroll-view scroll-x class="tag-scroll">
        <view 
          class="tag-item" 
          :class="{ active: currentTag === '' }"
          @click="selectTag('')"
        >
          <text>全部</text>
        </view>
        <view 
          v-for="tag in tags" 
          :key="tag"
          class="tag-item"
          :class="{ active: currentTag === tag }"
          @click="selectTag(tag)"
        >
          <up-icon name="tags" size="20" color="currentColor"></up-icon>
          <text>{{ tag }}</text>
        </view>
      </scroll-view>
    </view>
    
    <!-- 视图切换 -->
    <view class="view-toggle">
      <view class="toggle-left">
        <view 
          class="toggle-btn" 
          :class="{ active: viewMode === 'timeline' }"
          @click="switchView('timeline')"
        >
          <up-icon name="clock" size="28" color="currentColor"></up-icon>
          <text>时光轴</text>
        </view>
        <view 
          class="toggle-btn" 
          :class="{ active: viewMode === 'album' }"
          @click="switchView('album')"
        >
          <up-icon name="photo" size="28" color="currentColor"></up-icon>
          <text>相册</text>
        </view>
      </view>
      <view class="upload-btn" @click="uploadPhotos">
        <up-icon name="plus" size="28" color="#fff"></up-icon>
        <text>上传</text>
      </view>
    </view>
    
    <!-- 时光轴视图 -->
    <scroll-view 
      v-if="viewMode === 'timeline'" 
      scroll-y 
      class="timeline-content"
      @scrolltolower="loadMore"
    >
      <view v-for="(group, date) in timelineGroups" :key="date" class="timeline-group">
        <view class="timeline-date">
          <text class="date-main">{{ formatDateMain(date) }}</text>
          <text class="date-sub">{{ formatDateSub(date) }}</text>
        </view>
        <view class="photo-grid">
          <view 
            v-for="photo in group" 
            :key="photo.id"
            class="photo-item"
            @click="previewPhoto(photo, group)"
          >
            <image :src="photo.url" mode="aspectFill" class="photo-img" />
            <view v-if="photo.tags.length" class="photo-tags-mini">
              <up-icon name="tags" size="16" color="#fff"></up-icon>
            </view>
          </view>
        </view>
      </view>
      <up-loadmore :status="loadStatus" />
    </scroll-view>
    
    <!-- 相册视图 -->
    <scroll-view 
      v-else 
      scroll-y 
      class="album-content"
      @scrolltolower="loadMoreAlbums"
    >
      <view class="album-grid">
        <view 
          v-for="album in albums" 
          :key="album.id"
          class="album-card"
          @click="openAlbum(album)"
        >
          <view class="album-cover">
            <image :src="album.cover" mode="aspectFill" />
            <view class="album-count">
              <up-icon name="photo" size="20" color="#fff"></up-icon>
              <text>{{ album.count }}</text>
            </view>
          </view>
          <view class="album-info">
            <text class="album-name">{{ album.name }}</text>
            <text class="album-date">{{ album.date }}</text>
          </view>
        </view>
        
        <!-- 创建相册 -->
        <view class="album-card create" @click="createAlbum">
          <view class="create-content">
            <up-icon name="plus-circle" size="60" color="#ccc"></up-icon>
            <text>创建相册</text>
          </view>
        </view>
      </view>
      <up-loadmore :status="albumLoadStatus" />
    </scroll-view>
    
    <!-- 底部操作栏 -->
    <view class="bottom-bar" v-if="selectedPhotos.length">
      <view class="selected-info">
        <text>已选择 {{ selectedPhotos.length }} 张</text>
      </view>
      <view class="action-btns">
        <view class="action-btn" @click="addToAlbum">
          <up-icon name="folder" size="32" color="#5B8FF9"></up-icon>
          <text>加入相册</text>
        </view>
        <view class="action-btn" @click="downloadPhotos">
          <up-icon name="download" size="32" color="#52C41A"></up-icon>
          <text>下载</text>
        </view>
        <view class="action-btn" @click="deletePhotos">
          <up-icon name="trash" size="32" color="#FF4D4F"></up-icon>
          <text>删除</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'

// 数据
const totalPhotos = ref(256)
const currentTag = ref('')
const viewMode = ref('timeline')
const loadStatus = ref('loadmore')
const albumLoadStatus = ref('loadmore')
const selectedPhotos = ref([])

const tags = ref(['旅行', '生日', '节日', '美食', '日常', '成长', '聚会'])

const photos = ref([
  { id: 1, url: '/static/album/photo1.jpg', date: '2024-02-20', tags: ['生日', '聚会'], width: 800, height: 600 },
  { id: 2, url: '/static/album/photo2.jpg', date: '2024-02-20', tags: ['生日'], width: 600, height: 800 },
  { id: 3, url: '/static/album/photo3.jpg', date: '2024-02-18', tags: ['旅行'], width: 800, height: 600 },
  { id: 4, url: '/static/album/photo4.jpg', date: '2024-02-18', tags: ['旅行', '美食'], width: 600, height: 600 },
  { id: 5, url: '/static/album/photo5.jpg', date: '2024-02-15', tags: ['日常'], width: 800, height: 600 },
  { id: 6, url: '/static/album/photo6.jpg', date: '2024-02-15', tags: ['成长'], width: 600, height: 800 },
  { id: 7, url: '/static/album/photo7.jpg', date: '2024-02-10', tags: ['节日'], width: 800, height: 600 },
  { id: 8, url: '/static/album/photo8.jpg', date: '2024-02-10', tags: ['节日', '美食'], width: 800, height: 800 }
])

const albums = ref([
  { id: 1, name: '2024春节', cover: '/static/album/photo7.jpg', count: 58, date: '2024-02-10' },
  { id: 2, name: '宝贝生日', cover: '/static/album/photo1.jpg', count: 36, date: '2024-02-20' },
  { id: 3, name: '海南之旅', cover: '/static/album/photo3.jpg', count: 128, date: '2024-02-18' },
  { id: 4, name: '日常生活', cover: '/static/album/photo5.jpg', count: 256, date: '2024-01-01' },
  { id: 5, name: '成长记录', cover: '/static/album/photo6.jpg', count: 89, date: '2023-12-01' },
  { id: 6, name: '家庭聚会', cover: '/static/album/photo2.jpg', count: 45, date: '2024-01-15' }
])

// 按日期分组
const timelineGroups = computed(() => {
  let filtered = photos.value
  if (currentTag.value) {
    filtered = photos.value.filter(p => p.tags.includes(currentTag.value))
  }
  
  const groups = {}
  filtered.forEach(photo => {
    if (!groups[photo.date]) {
      groups[photo.date] = []
    }
    groups[photo.date].push(photo)
  })
  return groups
})

// 格式化日期
const formatDateMain = (date) => {
  const d = new Date(date)
  return `${d.getMonth() + 1}月${d.getDate()}日`
}

const formatDateSub = (date) => {
  const d = new Date(date)
  const days = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  return days[d.getDay()]
}

// 选择标签
const selectTag = (tag) => {
  currentTag.value = tag
}

// 切换视图
const switchView = (mode) => {
  viewMode.value = mode
}

// 上传照片
const uploadPhotos = () => {
  uni.chooseImage({
    count: 9,
    sizeType: ['original', 'compressed'],
    sourceType: ['album', 'camera'],
    success: (res) => {
      uni.showLoading({ title: '上传中...' })
      // 模拟上传
      setTimeout(() => {
        res.tempFilePaths.forEach((path, index) => {
          photos.value.unshift({
            id: Date.now() + index,
            url: path,
            date: new Date().toISOString().split('T')[0],
            tags: [],
            width: 800,
            height: 600
          })
        })
        totalPhotos.value += res.tempFilePaths.length
        uni.hideLoading()
        uni.showToast({ title: `成功上传${res.tempFilePaths.length}张照片`, icon: 'success' })
      }, 1500)
    }
  })
}

// 预览照片
const previewPhoto = (photo, group) => {
  const urls = group.map(p => p.url)
  const current = urls.indexOf(photo.url)
  uni.previewImage({
    urls,
    current
  })
}

// 打开相册
const openAlbum = (album) => {
  uni.navigateTo({
    url: `/pages/family/album-detail?id=${album.id}&name=${album.name}`
  })
}

// 创建相册
const createAlbum = () => {
  uni.showModal({
    title: '创建相册',
    editable: true,
    placeholderText: '请输入相册名称',
    success: (res) => {
      if (res.confirm && res.content) {
        albums.value.unshift({
          id: Date.now(),
          name: res.content,
          cover: '/static/album/default.jpg',
          count: 0,
          date: new Date().toISOString().split('T')[0]
        })
        uni.showToast({ title: '创建成功', icon: 'success' })
      }
    }
  })
}

// 加载更多
const loadMore = () => {
  loadStatus.value = 'loading'
  setTimeout(() => {
    loadStatus.value = 'nomore'
  }, 500)
}

const loadMoreAlbums = () => {
  albumLoadStatus.value = 'loading'
  setTimeout(() => {
    albumLoadStatus.value = 'nomore'
  }, 500)
}

// 操作
const addToAlbum = () => {
  uni.showToast({ title: '已加入相册', icon: 'success' })
  selectedPhotos.value = []
}

const downloadPhotos = () => {
  uni.showToast({ title: '开始下载...', icon: 'loading' })
  selectedPhotos.value = []
}

const deletePhotos = () => {
  uni.showModal({
    title: '确认删除',
    content: `确定删除选中的${selectedPhotos.value.length}张照片吗？`,
    confirmColor: '#FF4D4F',
    success: (res) => {
      if (res.confirm) {
        uni.showToast({ title: '已删除', icon: 'success' })
        selectedPhotos.value = []
      }
    }
  })
}
</script>

<style lang="scss" scoped>
.album-page {
  min-height: 100vh;
  background: #f5f6fa;
}

.album-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 30rpx;
  
  .album-stats {
    display: flex;
    justify-content: space-around;
    
    .stat-item {
      text-align: center;
      
      .stat-num {
        display: block;
        font-size: 48rpx;
        font-weight: bold;
        color: #fff;
      }
      
      .stat-label {
        font-size: 24rpx;
        color: rgba(255,255,255,0.8);
      }
    }
  }
}

.tag-filter {
  background: #fff;
  padding: 20rpx 0;
  border-bottom: 2rpx solid #f5f5f5;
  
  .tag-scroll {
    white-space: nowrap;
    padding: 0 20rpx;
    
    .tag-item {
      display: inline-flex;
      align-items: center;
      padding: 12rpx 24rpx;
      margin-right: 16rpx;
      background: #f5f6fa;
      border-radius: 30rpx;
      
      text {
        font-size: 26rpx;
        color: #666;
        margin-left: 8rpx;
      }
      
      &.active {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        
        text, ::v-deep .u-icon {
          color: #fff !important;
        }
      }
    }
  }
}

.view-toggle {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 30rpx;
  background: #fff;
  
  .toggle-left {
    display: flex;
    background: #f5f6fa;
    border-radius: 30rpx;
    padding: 6rpx;
    
    .toggle-btn {
      display: flex;
      align-items: center;
      padding: 12rpx 30rpx;
      border-radius: 24rpx;
      
      text {
        font-size: 26rpx;
        color: #666;
        margin-left: 8rpx;
      }
      
      &.active {
        background: #fff;
        box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.1);
        
        text, ::v-deep .u-icon {
          color: #667eea !important;
        }
      }
    }
  }
  
  .upload-btn {
    display: flex;
    align-items: center;
    padding: 16rpx 30rpx;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 30rpx;
    
    text {
      font-size: 26rpx;
      color: #fff;
      margin-left: 8rpx;
    }
  }
}

.timeline-content {
  height: calc(100vh - 400rpx);
  padding: 20rpx;
  
  .timeline-group {
    margin-bottom: 30rpx;
    
    .timeline-date {
      display: flex;
      align-items: baseline;
      margin-bottom: 20rpx;
      
      .date-main {
        font-size: 36rpx;
        font-weight: 600;
        color: #333;
      }
      
      .date-sub {
        font-size: 24rpx;
        color: #999;
        margin-left: 12rpx;
      }
    }
    
    .photo-grid {
      display: grid;
      grid-template-columns: repeat(3, 1fr);
      gap: 12rpx;
      
      .photo-item {
        position: relative;
        aspect-ratio: 1;
        border-radius: 12rpx;
        overflow: hidden;
        background: #eee;
        
        .photo-img {
          width: 100%;
          height: 100%;
        }
        
        .photo-tags-mini {
          position: absolute;
          top: 8rpx;
          right: 8rpx;
          width: 32rpx;
          height: 32rpx;
          background: rgba(0,0,0,0.4);
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
        }
      }
    }
  }
}

.album-content {
  height: calc(100vh - 400rpx);
  padding: 20rpx;
  
  .album-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 20rpx;
    
    .album-card {
      background: #fff;
      border-radius: 16rpx;
      overflow: hidden;
      box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.06);
      
      &.create {
        aspect-ratio: 1;
        display: flex;
        align-items: center;
        justify-content: center;
        border: 2rpx dashed #ddd;
        box-shadow: none;
        
        .create-content {
          text-align: center;
          
          text {
            display: block;
            font-size: 28rpx;
            color: #999;
            margin-top: 16rpx;
          }
        }
      }
      
      .album-cover {
        position: relative;
        aspect-ratio: 1;
        
        image {
          width: 100%;
          height: 100%;
        }
        
        .album-count {
          position: absolute;
          bottom: 12rpx;
          right: 12rpx;
          display: flex;
          align-items: center;
          padding: 8rpx 16rpx;
          background: rgba(0,0,0,0.5);
          border-radius: 20rpx;
          
          text {
            font-size: 22rpx;
            color: #fff;
            margin-left: 6rpx;
          }
        }
      }
      
      .album-info {
        padding: 20rpx;
        
        .album-name {
          display: block;
          font-size: 30rpx;
          font-weight: 500;
          color: #333;
          margin-bottom: 8rpx;
        }
        
        .album-date {
          font-size: 24rpx;
          color: #999;
        }
      }
    }
  }
}

.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #fff;
  padding: 20rpx 30rpx;
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
  box-shadow: 0 -2rpx 20rpx rgba(0,0,0,0.08);
  
  .selected-info {
    text-align: center;
    margin-bottom: 20rpx;
    
    text {
      font-size: 28rpx;
      color: #666;
    }
  }
  
  .action-btns {
    display: flex;
    justify-content: space-around;
    
    .action-btn {
      display: flex;
      flex-direction: column;
      align-items: center;
      
      text {
        font-size: 24rpx;
        color: #666;
        margin-top: 8rpx;
      }
    }
  }
}
</style>