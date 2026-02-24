<template>
  <view class="yearly-memory-page">
    <!-- 自定义导航栏 -->
    <view class="custom-nav">
      <view class="nav-back" @click="goBack">
        <text class="icon">‹</text>
      </view>
      <view class="nav-title">{{ currentYear }}年度回忆</view>
      <view class="nav-action" @click="showYearPicker = true">
        <text>切换年份</text>
        <text class="icon-small">▼</text>
      </view>
    </view>

    <!-- 年度总结数据卡片 -->
    <view class="yearly-summary">
      <view class="summary-title">
        <text class="year-badge">{{ currentYear }}</text>
        <text>年度成就</text>
      </view>
      
      <view class="stats-grid">
        <view class="stat-card tasks">
          <view class="stat-icon">📋</view>
          <view class="stat-content">
            <text class="stat-value">{{ yearlyStats.totalTasks }}</text>
            <text class="stat-label">完成任务</text>
          </view>
        </view>
        
        <view class="stat-card wishes">
          <view class="stat-icon">⭐</view>
          <view class="stat-content">
            <text class="stat-value">{{ yearlyStats.completedWishes }}</text>
            <text class="stat-label">心愿达成</text>
          </view>
        </view>
        
        <view class="stat-card calories">
          <view class="stat-icon">🔥</view>
          <view class="stat-content">
            <text class="stat-value">{{ yearlyStats.totalCalories }}万</text>
            <text class="stat-label">摄入热量</text>
          </view>
        </view>
        
        <view class="stat-card moments">
          <view class="stat-icon">📸</view>
          <view class="stat-content">
            <text class="stat-value">{{ yearlyStats.totalPhotos }}</text>
            <text class="stat-label">精彩瞬间</text>
          </view>
        </view>
        
        <view class="stat-card days">
          <view class="stat-icon">📅</view>
          <view class="stat-content">
            <text class="stat-value">{{ yearlyStats.activeDays }}</text>
            <text class="stat-label">活跃天数</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 精彩瞬间照片墙 -->
    <view class="section">
      <view class="section-header">
        <view class="section-title">
          <text class="title-icon">📷</text>
          <text>精彩瞬间</text>
        </view>
        <text class="view-more" @click="viewAllPhotos">查看全部 ›</text>
      </view>
      
      <view class="photo-wall">
        <view 
          v-for="(photo, index) in photoWall" 
          :key="index"
          class="photo-item"
          :class="photo.size"
          @click="previewPhoto(index)"
        >
          <image 
            :src="photo.url" 
            mode="aspectFill"
            class="photo-image"
          />
          <view class="photo-overlay">
            <text class="photo-date">{{ photo.date }}</text>
            <text class="photo-desc">{{ photo.description }}</text>
          </view>
          <view v-if="photo.isVideo" class="video-badge">▶</view>
        </view>
      </view>
    </view>

    <!-- 年度里程碑时间轴 -->
    <view class="section">
      <view class="section-header">
        <view class="section-title">
          <text class="title-icon">🎯</text>
          <text>年度里程碑</text>
        </view>
      </view>
      
      <view class="timeline">
        <view 
          v-for="(milestone, index) in milestones" 
          :key="index"
          class="timeline-item"
          :class="{ first: index === 0, last: index === milestones.length - 1 }"
        >
          <view class="timeline-left">
            <text class="timeline-date">{{ milestone.date }}</text>
            <text class="timeline-month">{{ milestone.month }}</text>
          </view>
          
          <view class="timeline-dot">
            <view class="dot-inner" :style="{ background: milestone.color }"></view>
          </view>
          
          <view class="timeline-content">
            <view class="content-card" :style="{ borderLeftColor: milestone.color }">
              <view class="card-header">
                <text class="milestone-icon">{{ milestone.icon }}</text>
                <text class="milestone-title">{{ milestone.title }}</text>
              </view>
              <text class="milestone-desc">{{ milestone.description }}</text>
              <view v-if="milestone.image" class="milestone-image">
                <image :src="milestone.image" mode="aspectFill" />
              </view>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 年度徽章 -->
    <view class="section badges-section">
      <view class="section-header">
        <view class="section-title">
          <text class="title-icon">🎖️</text>
          <text>年度徽章</text>
        </view>
      </view>
      
      <view class="badges-grid">
        <view 
          v-for="(badge, index) in yearlyBadges" 
          :key="index"
          class="badge-card"
          :class="{ earned: badge.earned }"
        >
          <view class="badge-icon-wrapper">
            <text class="badge-emoji">{{ badge.emoji }}</text>
            <view v-if="badge.earned" class="earned-mark">✓</view>
          </view>
          <view class="badge-info">
            <text class="badge-name">{{ badge.name }}</text>
            <text class="badge-desc">{{ badge.description }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 底部分享区域 -->
    <view class="share-section">
      <view class="share-title">分享你的年度回忆</view>
      <view class="share-buttons">
        <view class="share-btn wechat" @click="shareToWechat">
          <text class="btn-icon">💬</text>
          <text>分享给好友</text>
        </view>
        
        <view class="share-btn moments" @click="shareToMoments">
          <text class="btn-icon">📱</text>
          <text>分享到朋友圈</text>
        </view>
        
        <view class="share-btn save" @click="saveImage">
          <text class="btn-icon">💾</text>
          <text>保存图片</text>
        </view>
      </view>
    </view>

    <!-- 年份选择弹窗 -->
    <view v-if="showYearPicker" class="modal-overlay">
      <view class="modal-mask" @click="showYearPicker = false"></view>
      <view class="modal-content">
        <view class="modal-header">
          <text>选择年份</text>
          <text class="close-btn" @click="showYearPicker = false">✕</text>
        </view>
        
        <view class="year-list">
          <view 
            v-for="year in availableYears" 
            :key="year"
            class="year-item"
            :class="{ active: year === currentYear }"
            @click="selectYear(year)"
          >
            <text class="year-text">{{ year }}年</text>
            <view v-if="year === currentYear" class="year-check">✓</view>
          </view>
        </view>
      </view>
    </view>

    <!-- 照片预览弹窗 -->
    <view v-if="previewIndex !== null" class="preview-overlay" @click="closePreview">
      <view class="preview-container">
        <image 
          :src="photoWall[previewIndex]?.url" 
          mode="aspectFit"
          class="preview-image"
        />
        <view class="preview-info">
          <text class="preview-date">{{ photoWall[previewIndex]?.date }}</text>
          <text class="preview-desc">{{ photoWall[previewIndex]?.description }}</text>
        </view>
        
        <view class="preview-nav">
          <text 
            class="nav-btn" 
            :class="{ disabled: previewIndex === 0 }"
            @click.stop="prevPhoto"
          >‹</text>
          <text class="nav-indicator">{{ previewIndex + 1 }} / {{ photoWall.length }}</text>
          <text 
            class="nav-btn" 
            :class="{ disabled: previewIndex === photoWall.length - 1 }"
            @click.stop="nextPhoto"
          >›</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { statsApi } from '@/api/stats'

const currentYear = ref(new Date().getFullYear())
const showYearPicker = ref(false)
const previewIndex = ref(null)

// 年度统计数据（移除家庭评分）
const yearlyStats = ref({
  totalTasks: 368,
  completedWishes: 24,
  totalCalories: 78.5,
  totalPhotos: 156,
  activeDays: 312
})

// 照片墙数据
const photoWall = ref([
  { 
    url: '/static/photos/spring.jpg', 
    date: '03-15', 
    description: '春日踏青', 
    size: 'large',
    isVideo: false
  },
  { 
    url: '/static/photos/birthday.jpg', 
    date: '05-20', 
    description: '生日派对', 
    size: 'small',
    isVideo: true
  },
  { 
    url: '/static/photos/summer.jpg', 
    date: '07-08', 
    description: '海边度假', 
    size: 'small',
    isVideo: false
  },
  { 
    url: '/static/photos/autumn.jpg', 
    date: '10-01', 
    description: '国庆出游', 
    size: 'medium',
    isVideo: false
  },
  { 
    url: '/static/photos/winter.jpg', 
    date: '12-25', 
    description: '圣诞聚会', 
    size: 'small',
    isVideo: false
  },
  { 
    url: '/static/photos/family.jpg', 
    date: '02-10', 
    description: '全家福', 
    size: 'medium',
    isVideo: false
  }
])

// 里程碑数据（更新为小清新配色）
const milestones = ref([
  {
    date: '01',
    month: '1月',
    icon: '🎆',
    title: '新年愿望清单',
    description: '制定了20个新年愿望，开始为梦想努力',
    color: '#7DD3D8',
    image: ''
  },
  {
    date: '15',
    month: '3月',
    icon: '🌸',
    title: '春日家庭出游',
    description: '一起去了植物园，记录了美好的春日时光',
    color: '#FFB7C5',
    image: '/static/photos/spring.jpg'
  },
  {
    date: '20',
    month: '5月',
    icon: '🎂',
    title: '宝贝生日',
    description: '全家人一起庆祝，收到了最想要的礼物',
    color: '#FFD93D',
    image: '/static/photos/birthday.jpg'
  },
  {
    date: '08',
    month: '7月',
    icon: '🏖️',
    title: '海边度假',
    description: '一家人去海边玩了一周，留下了难忘的回忆',
    color: '#87CEEB',
    image: '/static/photos/summer.jpg'
  },
  {
    date: '01',
    month: '10月',
    icon: '🍂',
    title: '国庆旅行',
    description: '自驾游去了五个城市，收获了满满的快乐',
    color: '#F4A460',
    image: '/static/photos/autumn.jpg'
  },
  {
    date: '25',
    month: '12月',
    icon: '🎄',
    title: '年终总结',
    description: '回顾这一年的点点滴滴，感谢有你们相伴',
    color: '#98D8C8',
    image: ''
  }
])

// 年度徽章（移除家庭评分相关徽章）
const yearlyBadges = ref([
  { 
    emoji: '🔥', 
    name: '连续打卡', 
    description: '连续活跃30天',
    earned: true
  },
  { 
    emoji: '⭐', 
    name: '心愿达成者', 
    description: '完成10个心愿',
    earned: true
  },
  { 
    emoji: '📸', 
    name: '记录达人', 
    description: '上传50张照片',
    earned: true
  },
  { 
    emoji: '💪', 
    name: '任务王', 
    description: '完成100个任务',
    earned: true
  },
  { 
    emoji: '💰', 
    name: '理财高手', 
    description: '储蓄率达40%',
    earned: false
  }
])

// 可用年份
const availableYears = computed(() => {
  const years = []
  const current = new Date().getFullYear()
  for (let i = 0; i < 3; i++) {
    years.push(current - i)
  }
  return years
})

// 初始化
onMounted(() => {
  loadYearlyData()
})

// 加载年度数据
const loadYearlyData = async () => {
  try {
    const res = await statsApi.getYearlyStats(currentYear.value)
    if (res.code === 200 && res.data) {
      // 更新数据
    }
  } catch (error) {
    console.error('加载年度数据失败', error)
  }
}

// 返回上一页
const goBack = () => {
  uni.navigateBack()
}

// 选择年份
const selectYear = (year) => {
  currentYear.value = year
  showYearPicker.value = false
  loadYearlyData()
}

// 查看全部照片
const viewAllPhotos = () => {
  uni.showToast({ title: '查看全部照片功能开发中', icon: 'none' })
}

// 预览照片
const previewPhoto = (index) => {
  previewIndex.value = index
}

// 关闭预览
const closePreview = () => {
  previewIndex.value = null
}

// 上一张照片
const prevPhoto = () => {
  if (previewIndex.value > 0) {
    previewIndex.value--
  }
}

// 下一张照片
const nextPhoto = () => {
  if (previewIndex.value < photoWall.value.length - 1) {
    previewIndex.value++
  }
}

// 分享给好友
const shareToWechat = () => {
  uni.share({
    provider: 'weixin',
    scene: 'WXSceneSession',
    type: 0,
    title: `${currentYear.value}年度回忆`,
    summary: '查看我们家的精彩年度回顾',
    imageUrl: '/static/logo.png',
    success: () => {
      uni.showToast({ title: '分享成功', icon: 'success' })
    }
  })
}

// 分享到朋友圈
const shareToMoments = () => {
  uni.share({
    provider: 'weixin',
    scene: 'WXSenceTimeline',
    type: 0,
    title: `${currentYear.value}年度回忆`,
    summary: '查看我们家的精彩年度回顾',
    imageUrl: '/static/logo.png',
    success: () => {
      uni.showToast({ title: '分享成功', icon: 'success' })
    }
  })
}

// 保存图片
const saveImage = () => {
  uni.showLoading({ title: '生成图片中...' })
  setTimeout(() => {
    uni.hideLoading()
    uni.showToast({ title: '图片已保存', icon: 'success' })
  }, 1500)
}
</script>

<style lang="scss" scoped>
// 小清新配色方案
$bg-primary: #F0F9F4;      // 薄荷绿背景
$bg-secondary: #FFF5F7;    // 淡粉色背景
$color-primary: #7DD3D8;    // 薄荷绿主色
$color-secondary: #FFB7C5;  // 淡粉色
$color-accent: #87CEEB;     // 天蓝色
$color-text: #4A5568;       // 深灰文字
$color-text-light: #718096; // 浅灰文字
$color-white: #FFFFFF;
$shadow-soft: 0 4rpx 20rpx rgba(125, 211, 216, 0.15);

.yearly-memory-page {
  min-height: 100vh;
  background: linear-gradient(180deg, $bg-primary 0%, $bg-secondary 100%);
  padding-bottom: 60rpx;
}

// 自定义导航栏
.custom-nav {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 50rpx 30rpx 20rpx;
  
  .nav-back {
    width: 60rpx;
    height: 60rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    background: rgba($color-white, 0.8);
    border-radius: 50%;
    box-shadow: $shadow-soft;
    
    .icon {
      font-size: 48rpx;
      color: $color-text;
    }
  }
  
  .nav-title {
    font-size: 36rpx;
    font-weight: 600;
    color: $color-text;
  }
  
  .nav-action {
    display: flex;
    align-items: center;
    gap: 8rpx;
    padding: 12rpx 24rpx;
    background: rgba($color-white, 0.8);
    border-radius: 30rpx;
    box-shadow: $shadow-soft;
    
    text {
      font-size: 24rpx;
      color: $color-text;
    }
    
    .icon-small {
      font-size: 18rpx;
      color: $color-primary;
    }
  }
}

// 年度总结
.yearly-summary {
  padding: 40rpx 30rpx;
  
  .summary-title {
    display: flex;
    align-items: center;
    gap: 20rpx;
    margin-bottom: 40rpx;
    
    .year-badge {
      padding: 12rpx 32rpx;
      background: linear-gradient(135deg, $color-primary 0%, $color-accent 100%);
      border-radius: 40rpx;
      font-size: 40rpx;
      font-weight: 700;
      color: $color-white;
      box-shadow: 0 4rpx 16rpx rgba(125, 211, 216, 0.4);
    }
    
    text:last-child {
      font-size: 36rpx;
      font-weight: 600;
      color: $color-text;
    }
  }
  
  .stats-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 20rpx;
    
    .stat-card {
      padding: 30rpx 20rpx;
      background: $color-white;
      border-radius: 24rpx;
      text-align: center;
      box-shadow: $shadow-soft;
      
      &.tasks .stat-icon { 
        background: linear-gradient(135deg, #A8E6CF 0%, #88D8A3 100%);
      }
      &.wishes .stat-icon { 
        background: linear-gradient(135deg, #FFD3E1 0%, #FFB7C5 100%);
      }
      &.calories .stat-icon { 
        background: linear-gradient(135deg, #FFE4B5 0%, #FFD93D 100%);
      }
      &.moments .stat-icon { 
        background: linear-gradient(135deg, #E2D5F8 0%, #D4C4F7 100%);
      }
      &.days .stat-icon { 
        background: linear-gradient(135deg, #B8E6F0 0%, #87CEEB 100%);
      }
      
      .stat-icon {
        width: 72rpx;
        height: 72rpx;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        margin: 0 auto 16rpx;
        font-size: 36rpx;
        box-shadow: 0 4rpx 12rpx rgba(0,0,0,0.08);
      }
      
      .stat-content {
        .stat-value {
          display: block;
          font-size: 36rpx;
          font-weight: 700;
          color: $color-text;
          margin-bottom: 8rpx;
        }
        
        .stat-label {
          font-size: 22rpx;
          color: $color-text-light;
        }
      }
    }
  }
}

// 通用区块
.section {
  margin: 0 30rpx 40rpx;
  padding: 30rpx;
  background: $color-white;
  border-radius: 32rpx;
  box-shadow: $shadow-soft;
  
  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 30rpx;
    
    .section-title {
      display: flex;
      align-items: center;
      gap: 12rpx;
      font-size: 32rpx;
      font-weight: 600;
      color: $color-text;
      
      .title-icon {
        font-size: 36rpx;
      }
    }
    
    .view-more {
      font-size: 26rpx;
      color: $color-primary;
      font-weight: 500;
    }
  }
}

// 照片墙
.photo-wall {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  grid-auto-rows: 200rpx;
  gap: 16rpx;
  
  .photo-item {
    position: relative;
    border-radius: 20rpx;
    overflow: hidden;
    box-shadow: 0 4rpx 12rpx rgba(0,0,0,0.08);
    
    &.large {
      grid-column: span 2;
      grid-row: span 2;
    }
    
    &.medium {
      grid-column: span 2;
    }
    
    &.small {
      grid-column: span 1;
    }
    
    .photo-image {
      width: 100%;
      height: 100%;
    }
    
    .photo-overlay {
      position: absolute;
      bottom: 0;
      left: 0;
      right: 0;
      padding: 20rpx;
      background: linear-gradient(transparent, rgba(0,0,0,0.5));
      
      .photo-date {
        display: block;
        font-size: 22rpx;
        color: rgba(255,255,255,0.9);
        margin-bottom: 4rpx;
      }
      
      .photo-desc {
        font-size: 24rpx;
        color: $color-white;
      }
    }
    
    .video-badge {
      position: absolute;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      width: 80rpx;
      height: 80rpx;
      background: rgba($color-primary, 0.9);
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      color: $color-white;
      font-size: 32rpx;
      box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.2);
    }
  }
}

// 时间轴
.timeline {
  position: relative;
  padding-left: 20rpx;
  
  .timeline-item {
    display: flex;
    align-items: flex-start;
    margin-bottom: 40rpx;
    
    &.last {
      margin-bottom: 0;
    }
    
    .timeline-left {
      width: 80rpx;
      text-align: right;
      padding-right: 20rpx;
      
      .timeline-date {
        display: block;
        font-size: 32rpx;
        font-weight: 700;
        color: $color-text;
      }
      
      .timeline-month {
        font-size: 22rpx;
        color: $color-text-light;
      }
    }
    
    .timeline-dot {
      width: 40rpx;
      height: 40rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 20rpx;
      
      .dot-inner {
        width: 20rpx;
        height: 20rpx;
        border-radius: 50%;
        border: 4rpx solid rgba($color-primary, 0.3);
      }
    }
    
    .timeline-content {
      flex: 1;
      
      .content-card {
        padding: 24rpx;
        background: linear-gradient(135deg, #FAFAFA 0%, #F5F5F5 100%);
        border-radius: 20rpx;
        border-left: 6rpx solid;
        
        .card-header {
          display: flex;
          align-items: center;
          gap: 16rpx;
          margin-bottom: 12rpx;
          
          .milestone-icon {
            font-size: 40rpx;
          }
          
          .milestone-title {
            font-size: 30rpx;
            font-weight: 600;
            color: $color-text;
          }
        }
        
        .milestone-desc {
          display: block;
          font-size: 26rpx;
          color: $color-text-light;
          line-height: 1.6;
        }
        
        .milestone-image {
          margin-top: 16rpx;
          height: 160rpx;
          border-radius: 16rpx;
          overflow: hidden;
          box-shadow: 0 4rpx 12rpx rgba(0,0,0,0.08);
          
          image {
            width: 100%;
            height: 100%;
          }
        }
      }
    }
  }
}

// 徽章区块
.badges-section {
  .badges-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 20rpx;
    
    .badge-card {
      padding: 30rpx 20rpx;
      background: #F8F9FA;
      border-radius: 24rpx;
      text-align: center;
      opacity: 0.5;
      transition: all 0.3s ease;
      
      &.earned {
        opacity: 1;
        background: linear-gradient(135deg, $color-white 0%, #F0FDF4 100%);
        box-shadow: $shadow-soft;
        
        .badge-icon-wrapper {
          .badge-emoji {
            filter: none;
          }
        }
      }
      
      .badge-icon-wrapper {
        position: relative;
        display: inline-block;
        margin-bottom: 16rpx;
        
        .badge-emoji {
          font-size: 56rpx;
          filter: grayscale(100%);
        }
        
        .earned-mark {
          position: absolute;
          top: -4rpx;
          right: -4rpx;
          width: 32rpx;
          height: 32rpx;
          background: $color-primary;
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 18rpx;
          color: $color-white;
          box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.15);
        }
      }
      
      .badge-info {
        .badge-name {
          display: block;
          font-size: 26rpx;
          font-weight: 600;
          color: $color-text;
          margin-bottom: 8rpx;
        }
        
        .badge-desc {
          font-size: 20rpx;
          color: $color-text-light;
        }
      }
    }
  }
}

// 分享区域
.share-section {
  margin: 0 30rpx;
  padding: 40rpx 30rpx;
  background: linear-gradient(135deg, $color-primary 0%, $color-accent 100%);
  border-radius: 32rpx;
  text-align: center;
  box-shadow: 0 8rpx 32rpx rgba(125, 211, 216, 0.3);
  
  .share-title {
    font-size: 32rpx;
    font-weight: 600;
    color: $color-white;
    margin-bottom: 30rpx;
  }
  
  .share-buttons {
    display: flex;
    justify-content: center;
    gap: 20rpx;
    
    .share-btn {
      flex: 1;
      padding: 24rpx;
      background: rgba($color-white, 0.2);
      border-radius: 20rpx;
      backdrop-filter: blur(10rpx);
      transition: all 0.2s ease;
      
      .btn-icon {
        display: block;
        font-size: 40rpx;
        margin-bottom: 12rpx;
      }
      
      text:last-child {
        font-size: 24rpx;
        color: rgba($color-white, 0.95);
      }
      
      &:active {
        background: rgba($color-white, 0.35);
        transform: scale(0.98);
      }
    }
  }
}

// 年份选择弹窗
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 1000;
  display: flex;
  align-items: flex-end;
  
  .modal-mask {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0,0,0,0.4);
  }
  
  .modal-content {
    position: relative;
    width: 100%;
    background: $color-white;
    border-radius: 40rpx 40rpx 0 0;
    padding: 40rpx;
    animation: slideUp 0.3s ease;
    
    @keyframes slideUp {
      from { transform: translateY(100%); }
      to { transform: translateY(0); }
    }
    
    .modal-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 40rpx;
      
      text {
        font-size: 32rpx;
        font-weight: 600;
        color: $color-text;
        
        &.close-btn {
          font-size: 36rpx;
          color: $color-text-light;
        }
      }
    }
    
    .year-list {
      .year-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 30rpx;
        background: #F8F9FA;
        border-radius: 20rpx;
        margin-bottom: 20rpx;
        
        &.active {
          background: linear-gradient(135deg, rgba($color-primary, 0.1) 0%, rgba($color-accent, 0.1) 100%);
          border: 2rpx solid $color-primary;
          
          .year-text {
            color: $color-primary;
            font-weight: 600;
          }
        }
        
        .year-text {
          font-size: 32rpx;
          color: $color-text;
        }
        
        .year-check {
          width: 44rpx;
          height: 44rpx;
          background: $color-primary;
          border-radius: 50%;
          color: $color-white;
          font-size: 24rpx;
          display: flex;
          align-items: center;
          justify-content: center;
        }
      }
    }
  }
}

// 照片预览
.preview-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 2000;
  background: rgba(0,0,0,0.95);
  display: flex;
  align-items: center;
  justify-content: center;
  
  .preview-container {
    width: 100%;
    height: 100%;
    display: flex;
    flex-direction: column;
    
    .preview-image {
      flex: 1;
      width: 100%;
    }
    
    .preview-info {
      padding: 40rpx;
      text-align: center;
      
      .preview-date {
        display: block;
        font-size: 28rpx;
        color: rgba(255,255,255,0.7);
        margin-bottom: 12rpx;
      }
      
      .preview-desc {
        font-size: 32rpx;
        color: $color-white;
      }
    }
    
    .preview-nav {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 30rpx 60rpx 60rpx;
      
      .nav-btn {
        width: 80rpx;
        height: 80rpx;
        background: rgba($color-white, 0.2);
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 40rpx;
        color: $color-white;
        
        &.disabled {
          opacity: 0.3;
        }
      }
      
      .nav-indicator {
        font-size: 28rpx;
        color: rgba(255,255,255,0.7);
      }
    }
  }
}
</style>