<template>
  <view class="section-card anniversary-card animate-in" v-if="anniversaries.length > 0">
    <view class="section-header">
      <view class="title-wrapper">
        <text class="section-icon">💝</text>
        <text class="section-title">{{ title }}</text>
      </view>
      <view class="header-actions">
        <view class="add-btn" @click="openModal()">
          <text class="add-btn-text">+ 添加</text>
        </view>
        <view class="more-btn" @click="goToList"
        >
          <text>更多</text>
          <u-icon name="arrow-right" size="22" color="#7FD8BE"></u-icon>
        </view>
      </view>
    </view>

    <view class="anniversary-list">
      <view
        v-for="(item, index) in anniversaries"
        :key="item.id"
        class="anni-item"
        :style="{ animationDelay: `${index * 0.1}s` }"
        @click="openEditModal(item)"
      >
        <view class="anni-icon-wrapper" :class="{ urgent: item.daysUntil <= 3 && item.daysUntil >= 0 }">
          <text class="anni-icon">{{ item.icon || getDefaultIcon(item.type) }}</text>
        </view>

        <view class="anni-info">
          <text class="anni-title">{{ item.title }}</text>
          <view class="anni-date-wrapper">
            <text class="anni-date">{{ formatDate(item.nextAnniversaryDate || item.targetDate) }}</text>
            <text v-if="item.isRecurring === 1" class="anni-recurring">每年</text>
          </view>
        </view>

        <view class="anni-days" :class="{ 
          today: item.daysUntil === 0, 
          soon: item.daysUntil <= 3 && item.daysUntil > 0,
          passed: item.daysUntil < 0
        }">
          <text class="days-num">{{ getDaysText(item.daysUntil) }}</text>
          <text class="days-label" v-if="item.daysUntil > 0">天后</text>
        </view>
      </view>
    </view>

    <!-- 添加/编辑弹窗 -->
    <AnniversaryModal
      :visible="showModal"
      :data="editingItem"
      @close="closeModal"
      @confirm="handleConfirm"
    />
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import AnniversaryModal from './AnniversaryModal.vue'

const props = defineProps({
  familyId: {
    type: [Number, String],
    required: true
  },
  anniversaries: {
    type: Array,
    default: () => []
  },
  title: {
    type: String,
    default: '近期纪念'
  }
})

const emit = defineEmits(['refresh'])

const showModal = ref(false)
const editingItem = ref(null)

// 默认图标映射
const getDefaultIcon = (type) => {
  const icons = {
    birthday: '🎂',
    wedding: '💒',
    love: '💕',
    family: '🏠',
    traditional: '🎊',
    custom: '💝'
  }
  return icons[type] || '💝'
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const month = date.getMonth() + 1
  const day = date.getDate()
  return `${month}月${day}日`
}

// 获取天数显示文本
const getDaysText = (days) => {
  if (days === 0) return '今天'
  if (days < 0) return `已${Math.abs(days)}天`
  return days
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

// 处理确认
const handleConfirm = () => {
  closeModal()
  emit('refresh')
}

// 跳转到列表页
const goToList = () => {
  uni.navigateTo({
    url: '/pages/anniversary/index'
  })
}
</script>

<style lang="scss" scoped>
.section-card {
  background: #fff;
  border-radius: 28rpx;
  margin: 28rpx 32rpx;
  padding: 32rpx;
  box-shadow: 0 8rpx 32rpx rgba(168, 230, 207, 0.12), 0 2rpx 8rpx rgba(0,0,0,0.03);

  &.animate-in {
    animation: slideIn 0.5s ease-out forwards;
  }
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(20rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 28rpx;

  .title-wrapper {
    display: flex;
    align-items: center;

    .section-icon {
      font-size: 36rpx;
      margin-right: 12rpx;
    }

    .section-title {
      font-size: 32rpx;
      font-weight: 600;
      color: #2d3748;
    }
  }

  .header-actions {
    display: flex;
    align-items: center;
    gap: 16rpx;
  }

  .add-btn {
    display: flex;
    align-items: center;
    padding: 10rpx 20rpx;
    background: linear-gradient(135deg, #FFD3B6, #FFAAA5);
    border-radius: 24rpx;
    box-shadow: 0 4rpx 12rpx rgba(255, 211, 182, 0.4);
    transition: all 0.2s ease;

    &:active {
      transform: scale(0.95);
    }

    .add-btn-text {
      font-size: 24rpx;
      color: #fff;
      font-weight: 500;
    }
  }

  .more-btn {
    display: flex;
    align-items: center;
    padding: 10rpx 16rpx;
    background: rgba(168, 230, 207, 0.15);
    border-radius: 24rpx;
    transition: all 0.2s ease;

    &:active {
      background: rgba(168, 230, 207, 0.25);
      transform: scale(0.95);
    }

    text {
      font-size: 24rpx;
      color: #7FD8BE;
      font-weight: 500;
      margin-right: 6rpx;
    }
  }
}

// 纪念日卡片
.anniversary-card {
  background: linear-gradient(135deg, #fff 0%, #fff5f5 100%);
}

.anniversary-list {
  .anni-item {
    display: flex;
    align-items: center;
    padding: 24rpx 0;
    border-bottom: 2rpx solid rgba(226, 232, 240, 0.5);
    animation: fadeInUp 0.4s ease-out forwards;
    opacity: 0;

    &:last-child {
      border-bottom: none;
    }

    &:active {
      background: rgba(0, 0, 0, 0.02);
      border-radius: 12rpx;
      margin: 0 -12rpx;
      padding-left: 12rpx;
      padding-right: 12rpx;
    }

    .anni-icon-wrapper {
      width: 88rpx;
      height: 88rpx;
      background: linear-gradient(135deg, #fff0f6, #ffe4e6);
      border-radius: 24rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 44rpx;
      margin-right: 24rpx;
      transition: all 0.3s ease;

      &.urgent {
        background: linear-gradient(135deg, #fc8181, #f56565);
        box-shadow: 0 8rpx 24rpx rgba(252, 129, 129, 0.35);
      }
    }

    .anni-info {
      flex: 1;

      .anni-title {
        font-size: 30rpx;
        color: #2d3748;
        display: block;
        margin-bottom: 10rpx;
        font-weight: 500;
      }

      .anni-date-wrapper {
        display: flex;
        align-items: center;
        gap: 12rpx;

        .anni-date {
          font-size: 24rpx;
          color: #8b9aad;
        }

        .anni-recurring {
          font-size: 20rpx;
          color: #7FD8BE;
          background: rgba(127, 216, 190, 0.15);
          padding: 4rpx 12rpx;
          border-radius: 12rpx;
        }
      }
    }

    .anni-days {
      text-align: center;
      padding: 16rpx 24rpx;
      background: rgba(168, 230, 207, 0.15);
      border-radius: 20rpx;
      min-width: 100rpx;

      &.today {
        background: linear-gradient(135deg, #A8E6CF, #7FD8BE);
        box-shadow: 0 8rpx 24rpx rgba(168, 230, 207, 0.4);

        .days-num, .days-label {
          color: #fff;
        }
      }

      &.soon {
        background: linear-gradient(135deg, #FFD3B6, #FFAAA5);
        box-shadow: 0 8rpx 24rpx rgba(255, 211, 182, 0.4);

        .days-num, .days-label {
          color: #fff;
        }
      }

      &.passed {
        background: #f1f5f9;

        .days-num {
          color: #a0aec0;
        }
      }

      .days-num {
        font-size: 36rpx;
        font-weight: 700;
        color: #7FD8BE;
        display: block;
        line-height: 1.2;
      }

      .days-label {
        font-size: 22rpx;
        color: #8b9aad;
      }
    }
  }
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
