<template>
  <view class="anniversary-page">
    <!-- 页面标题 -->
    <view class="page-header">
      <view class="header-bg">
        <view class="bg-pattern"></view>
      </view>
      <view class="header-content">
        <view class="title-wrapper">
          <text class="page-title">💝 纪念日</text>
          <text class="page-subtitle">记录每个重要时刻</text>
        </view>
        <view class="add-btn" @click="openModal()">
          <text class="add-icon">+</text>
        </view>
      </view>
    </view>

    <!-- 分类筛选 -->
    <view class="filter-section">
      <scroll-view scroll-x class="filter-scroll" show-scrollbar="false">
        <view
          v-for="filter in filterOptions"
          :key="filter.value"
          class="filter-item"
          :class="{ active: currentFilter === filter.value }"
          @click="currentFilter = filter.value"
        >
          <text class="filter-icon">{{ filter.icon }}</text>
          <text class="filter-name">{{ filter.label }}</text>
        </view>
      </scroll-view>
    </view>

    <!-- 纪念日列表 -->
    <view class="anniversary-list" v-if="filteredList.length > 0">
      <view
        v-for="(item, index) in filteredList"
        :key="item.id"
        class="anni-card"
        :style="{ animationDelay: `${index * 0.08}s` }"
        @click="openEditModal(item)"
      >
        <view class="card-left">
          <view class="anni-icon-wrapper" :class="{ urgent: item.daysUntil <= 3 && item.daysUntil >= 0 }">
            <text class="anni-icon">{{ item.icon || getDefaultIcon(item.type) }}</text>
          </view>
        </view>

        <view class="card-center">
          <text class="anni-title">{{ item.title }}</text>
          <view class="anni-meta">
            <text class="anni-date">{{ formatFullDate(item.nextAnniversaryDate || item.targetDate) }}</text>
            <view class="meta-tags">
              <text v-if="item.isRecurring === 1" class="meta-tag recurring">每年</text>
              <text v-if="item.dateType === 'lunar'" class="meta-tag lunar">农历</text>
            </view>
          </view>
          <text v-if="item.description" class="anni-desc">{{ item.description }}</text>
        </view>

        <view class="card-right">
          <view class="days-badge" :class="{
            today: item.daysUntil === 0,
            soon: item.daysUntil <= 3 && item.daysUntil > 0,
            passed: item.daysUntil < 0
          }">
            <text class="days-num">{{ formatDays(item.daysUntil) }}</text>
            <text class="days-text">{{ getDaysText(item.daysUntil) }}</text>
          </view>
          <up-icon name="arrow-right" size="28" color="#ccc"></up-icon>
        </view>
      </view>
    </view>

    <!-- 空状态 -->
    <view class="empty-state" v-else>
      <view class="empty-illustration">
        <view class="empty-icon-bg">
          <text class="empty-icon">💝</text>
        </view>
      </view>
      <text class="empty-text">还没有纪念日</text>
      <text class="empty-subtext">添加重要的日子，记录美好时刻</text>
      <view class="empty-action" @click="openModal()">
        <text>+ 添加第一个纪念日</text>
      </view>
    </view>

    <!-- 添加/编辑弹窗 -->
    <AnniversaryModal
      :visible="showModal"
      :data="editingItem"
      @close="closeModal"
      @confirm="loadAnniversaries"
    />
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { anniversaryApi } from '@/api/anniversary'
import AnniversaryModal from '@/components/anniversary/AnniversaryModal.vue'

const anniversaryList = ref([])
const currentFilter = ref('all')
const showModal = ref(false)
const editingItem = ref(null)

// 筛选选项
const filterOptions = [
  { value: 'all', label: '全部', icon: '💝' },
  { value: 'birthday', label: '生日', icon: '🎂' },
  { value: 'wedding', label: '结婚', icon: '💒' },
  { value: 'love', label: '恋爱', icon: '💕' },
  { value: 'family', label: '家庭', icon: '🏠' },
  { value: 'traditional', label: '传统', icon: '🎊' },
  { value: 'custom', label: '自定义', icon: '✨' }
]

// 过滤后的列表
const filteredList = computed(() => {
  if (currentFilter.value === 'all') {
    return anniversaryList.value
  }
  return anniversaryList.value.filter(item => item.type === currentFilter.value)
})

// 获取默认图标
const getDefaultIcon = (type) => {
  const icons = {
    birthday: '🎂',
    wedding: '💒',
    love: '💕',
    family: '🏠',
    traditional: '🎊',
    custom: '✨'
  }
  return icons[type] || '💝'
}

// 格式化完整日期
const formatFullDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const year = date.getFullYear()
  const month = date.getMonth() + 1
  const day = date.getDate()
  return `${year}年${month}月${day}日`
}

// 格式化天数
const formatDays = (days) => {
  if (days === 0) return '今'
  if (days < 0) return Math.abs(days)
  return days
}

// 获取天数文本
const getDaysText = (days) => {
  if (days === 0) return '今天'
  if (days < 0) return '天前'
  return '天后'
}

// 加载纪念日列表
const loadAnniversaries = async () => {
  try {
    const familyId = uni.getStorageSync('currentFamilyId')
    if (!familyId) {
      uni.showToast({ title: '请先选择家庭', icon: 'none' })
      return
    }

    uni.showLoading({ title: '加载中...' })
    const res = await anniversaryApi.getList(familyId)
    uni.hideLoading()

    if (res && res.data) {
      anniversaryList.value = res.data
    } else if (Array.isArray(res)) {
      anniversaryList.value = res
    } else {
      anniversaryList.value = []
    }
  } catch (error) {
    uni.hideLoading()
    console.error('加载纪念日失败:', error)
    uni.showToast({ title: '加载失败', icon: 'none' })
  }
}

// 打开添加弹窗
const openModal = () => {
  editingItem.value = null
  showModal.value = true
}

// 打开编辑弹窗
const openEditModal = (item) => {
  editingItem.value = { ...item }
  showModal.value = true
}

// 关闭弹窗
const closeModal = () => {
  showModal.value = false
  editingItem.value = null
}

onMounted(() => {
  loadAnniversaries()
})

onShow(() => {
  loadAnniversaries()
})
</script>

<style lang="scss" scoped>
.anniversary-page {
  min-height: 100vh;
  background: #FAFAFA;
  padding-bottom: 40rpx;
}

// 页面头部
.page-header {
  position: relative;
  padding: 40rpx 32rpx 60rpx;
  padding-top: 100rpx;
  overflow: hidden;

  .header-bg {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 360rpx;
    background: linear-gradient(135deg, #FFD3B6 0%, #FFAAA5 50%, #A8E6CF 100%);
    border-radius: 0 0 60rpx 60rpx;
    box-shadow: 0 20rpx 60rpx rgba(255, 211, 182, 0.4);

    .bg-pattern {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      opacity: 0.1;
      background-image: radial-gradient(circle at 20% 50%, rgba(255,255,255,0.8) 0%, transparent 50%),
                        radial-gradient(circle at 80% 20%, rgba(255,255,255,0.6) 0%, transparent 40%);
    }
  }

  .header-content {
    position: relative;
    z-index: 1;
    display: flex;
    justify-content: space-between;
    align-items: center;

    .title-wrapper {
      .page-title {
        display: block;
        font-size: 48rpx;
        font-weight: 700;
        color: #fff;
        margin-bottom: 12rpx;
        text-shadow: 0 4rpx 12rpx rgba(0,0,0,0.1);
      }

      .page-subtitle {
        font-size: 28rpx;
        color: rgba(255,255,255,0.9);
      }
    }

    .add-btn {
      width: 88rpx;
      height: 88rpx;
      background: rgba(255,255,255,0.3);
      backdrop-filter: blur(10rpx);
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      border: 2rpx solid rgba(255,255,255,0.3);
      transition: all 0.3s;

      &:active {
        transform: scale(0.9);
        background: rgba(255,255,255,0.5);
      }

      .add-icon {
        font-size: 48rpx;
        color: #fff;
        font-weight: 300;
      }
    }
  }
}

// 筛选区域
.filter-section {
  margin: -20rpx 32rpx 32rpx;
  background: #fff;
  border-radius: 24rpx;
  padding: 24rpx;
  box-shadow: 0 8rpx 32rpx rgba(0,0,0,0.06);

  .filter-scroll {
    white-space: nowrap;

    .filter-item {
      display: inline-flex;
      align-items: center;
      padding: 16rpx 24rpx;
      background: #f7fafc;
      border-radius: 28rpx;
      margin-right: 16rpx;
      transition: all 0.2s;

      &.active {
        background: linear-gradient(135deg, #A8E6CF, #7FD8BE);

        .filter-name {
          color: #fff;
        }
      }

      &:active {
        transform: scale(0.95);
      }

      .filter-icon {
        font-size: 32rpx;
        margin-right: 8rpx;
      }

      .filter-name {
        font-size: 26rpx;
        color: #4a5568;
      }
    }
  }
}

// 纪念日列表
.anniversary-list {
  padding: 0 32rpx;

  .anni-card {
    display: flex;
    align-items: center;
    padding: 28rpx;
    background: #fff;
    border-radius: 24rpx;
    margin-bottom: 24rpx;
    box-shadow: 0 4rpx 20rpx rgba(0,0,0,0.04);
    animation: fadeInUp 0.4s ease-out forwards;
    opacity: 0;

    &:active {
      transform: scale(0.98);
      box-shadow: 0 8rpx 32rpx rgba(0,0,0,0.08);
    }

    .card-left {
      .anni-icon-wrapper {
        width: 96rpx;
        height: 96rpx;
        background: linear-gradient(135deg, #fff0f6, #ffe4e6);
        border-radius: 24rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 48rpx;
        margin-right: 24rpx;

        &.urgent {
          background: linear-gradient(135deg, #fc8181, #f56565);
          box-shadow: 0 8rpx 24rpx rgba(252, 129, 129, 0.35);

          .anni-icon {
            filter: brightness(0) invert(1);
          }
        }
      }
    }

    .card-center {
      flex: 1;
      min-width: 0;

      .anni-title {
        font-size: 32rpx;
        color: #2d3748;
        font-weight: 600;
        display: block;
        margin-bottom: 12rpx;
      }

      .anni-meta {
        display: flex;
        align-items: center;
        flex-wrap: wrap;
        gap: 12rpx;
        margin-bottom: 8rpx;

        .anni-date {
          font-size: 24rpx;
          color: #8b9aad;
        }

        .meta-tags {
          display: flex;
          gap: 8rpx;

          .meta-tag {
            font-size: 20rpx;
            padding: 4rpx 12rpx;
            border-radius: 12rpx;

            &.recurring {
              color: #7FD8BE;
              background: rgba(127, 216, 190, 0.15);
            }

            &.lunar {
              color: #FFAAA5;
              background: rgba(255, 170, 165, 0.15);
            }
          }
        }
      }

      .anni-desc {
        font-size: 24rpx;
        color: #a0aec0;
        display: -webkit-box;
        -webkit-line-clamp: 1;
        -webkit-box-orient: vertical;
        overflow: hidden;
      }
    }

    .card-right {
      display: flex;
      align-items: center;
      gap: 16rpx;

      .days-badge {
        display: flex;
        flex-direction: column;
        align-items: center;
        padding: 16rpx 20rpx;
        background: rgba(168, 230, 207, 0.15);
        border-radius: 16rpx;
        min-width: 80rpx;

        &.today {
          background: linear-gradient(135deg, #A8E6CF, #7FD8BE);
          box-shadow: 0 8rpx 24rpx rgba(168, 230, 207, 0.4);

          .days-num, .days-text {
            color: #fff;
          }
        }

        &.soon {
          background: linear-gradient(135deg, #FFD3B6, #FFAAA5);
          box-shadow: 0 8rpx 24rpx rgba(255, 211, 182, 0.4);

          .days-num, .days-text {
            color: #fff;
          }
        }

        &.passed {
          background: #f1f5f9;

          .days-num, .days-text {
            color: #a0aec0;
          }
        }

        .days-num {
          font-size: 32rpx;
          font-weight: 700;
          color: #7FD8BE;
          line-height: 1.2;
        }

        .days-text {
          font-size: 20rpx;
          color: #8b9aad;
        }
      }
    }
  }
}

// 空状态
.empty-state {
  text-align: center;
  padding: 120rpx 60rpx;

  .empty-illustration {
    width: 200rpx;
    height: 200rpx;
    margin: 0 auto 40rpx;

    .empty-icon-bg {
      width: 160rpx;
      height: 160rpx;
      background: linear-gradient(135deg, #fff0f6, #ffe4e6);
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      margin: 20rpx auto;
      box-shadow: 0 8rpx 32rpx rgba(255, 211, 182, 0.4);

      .empty-icon {
        font-size: 80rpx;
      }
    }
  }

  .empty-text {
    display: block;
    font-size: 32rpx;
    color: #2d3748;
    font-weight: 600;
    margin-bottom: 12rpx;
  }

  .empty-subtext {
    display: block;
    font-size: 26rpx;
    color: #8b9aad;
    margin-bottom: 40rpx;
  }

  .empty-action {
    display: inline-flex;
    align-items: center;
    padding: 24rpx 48rpx;
    background: linear-gradient(135deg, #FFD3B6, #FFAAA5);
    border-radius: 40rpx;
    box-shadow: 0 8rpx 24rpx rgba(255, 211, 182, 0.4);

    text {
      font-size: 28rpx;
      color: #fff;
      font-weight: 500;
    }

    &:active {
      transform: scale(0.96);
    }
  }
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
