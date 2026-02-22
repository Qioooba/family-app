<template>
  <view class="my-coupons-page">
    <!-- 头部 -->
    <view class="header">
      <view class="back-btn" @click="goBack">
        <text class="icon">←</text>
      </view>
      <text class="title">我的券包</text>
      <view class="history-btn" @click="showHistory">
        <text>使用记录</text>
      </view>
    </view>

    <!-- 统计卡片 -->
    <view class="stats-section">
      <view class="stats-card"
        v-for="(stat, index) in statsList" 
        :key="index"
        :style="{ background: stat.bgColor }"
      >
        <text class="stats-num">{{ stat.value }}</text>
        <text class="stats-label">{{ stat.label }}</text>
      </view>
    </view>

    <!-- 分类标签 -->
    <view class="category-tabs">
      <view 
        v-for="tab in tabs" 
        :key="tab.value"
        class="tab-item"
        :class="{ active: currentTab === tab.value }"
        @click="switchTab(tab.value)"
      >
        <text>{{ tab.label }}</text>
        <view v-if="tab.count > 0" class="tab-badge">{{ tab.count }}</view>
      </view>
    </view>

    <!-- 券包列表 -->
    <scroll-view class="coupons-list" scroll-y @scrolltolower="loadMore">
      <view 
        v-for="coupon in filteredCoupons" 
        :key="coupon.id"
        class="coupon-card"
        :class="[coupon.status, { 'expiring': isExpiringSoon(coupon) }]"
        @click="showCouponDetail(coupon)"
      >
        <!-- 左侧类型标识 -->
        <view class="coupon-type" :style="{ background: getTypeColor(coupon.type) }">
          <text class="type-icon">{{ getTypeIcon(coupon.type) }}</text>
          <text class="type-name">{{ getTypeName(coupon.type) }}</text>
        </view>

        <!-- 中间内容 -->
        <view class="coupon-content">
          <text class="coupon-name">{{ coupon.name }}</text>
          <text class="coupon-desc">{{ coupon.description }}</text>
          
          <view class="coupon-meta"
          >
            <view class="meta-item"
            >
              <text class="meta-icon">⏰</text>
              <text class="meta-text" :class="{ 'expiring': isExpiringSoon(coupon) }">{{ formatExpireDate(coupon) }}</text>
            </view>
            <view class="meta-item"
            >
              <text class="meta-icon">📍</text>
              <text class="meta-text">{{ coupon.merchant || '全平台通用' }}</text>
            </view>
          </view>
        </view>

        <!-- 右侧状态 -->
        <view class="coupon-status"
        >
          <view v-if="coupon.status === 'unused'" class="status-badge unused"
          >
            <text>未使用</text>
          </view>
          <view v-else-if="coupon.status === 'used'" class="status-badge used"
          >
            <text>已使用</text>
          </view>
          <view v-else class="status-badge expired"
          >
            <text>已过期</text>
          </view>
          
          <view v-if="coupon.status === 'unused'" class="use-btn" @click.stop="useCoupon(coupon)"
          >
            <text>去使用</text>
          </view>
        </view>
      </view>

      <view v-if="filteredCoupons.length === 0" class="empty-state"
      >
        <text class="empty-icon">🎫</text>
        <text class="empty-title">暂无{{ currentTabLabel }}券</text>
        <text class="empty-desc">快去积分商城兑换吧</text>
        <view class="empty-btn" @click="goToShop"
        >
          <text>去兑换</text>
        </view>
      </view>
    </scroll-view>

    <!-- 券详情弹窗 -->
    <view v-if="showDetailModal" class="modal-overlay"
    >
      <view class="modal-mask" @click="closeDetailModal"></text>
      <view class="modal-content"
      >
        <view v-if="selectedCoupon" class="coupon-detail"
        >
          <!-- 券头部 -->
          <view class="detail-header" :style="{ background: getTypeColor(selectedCoupon.type) }"
          >
            <text class="detail-type"
          >{{ getTypeName(selectedCoupon.type) }}</text>
            <text class="detail-name"
          >{{ selectedCoupon.name }}</text>
            <view class="detail-value" v-if="selectedCoupon.value"
          >
              <text class="value-symbol">¥</text>
              <text class="value-num"
          >{{ selectedCoupon.value }}</text>
            </view>
          </view>

          <!-- 核销码 -->
          <view v-if="selectedCoupon.status === 'unused'" class="code-section"
          >
            <text class="code-label"
          >核销码</text>
            <view class="code-display"
          >
              <view class="code-text"
          >{{ selectedCoupon.code || generateCode() }}</view>
              <view class="code-barcode"
          >
                <view class="barcode-line" v-for="i in 30" :key="i" :style="{ width: Math.random() * 4 + 2 + 'px' }"
          ></text>
              </view>
            </view>
            <text class="code-tip"
          >出示给商家扫码核销</text>
          </view>

          <!-- 使用信息 -->
          <view class="info-section"
          >
            <view class="info-item"
          >
              <text class="info-label"
          >有效期至</text>
              <text class="info-value"
          >{{ selectedCoupon.expireDate }}</text>
            </view>
            <view class="info-item"
          >
              <text class="info-label"
          >适用商户</text>
              <text class="info-value"
          >{{ selectedCoupon.merchant || '全平台通用' }}</text>
            </view>
            <view class="info-item"
          >
              <text class="info-label"
          >使用规则</text>
              <text class="info-value rules"
          >{{ selectedCoupon.rules || '无特殊使用规则' }}</text>
            </view>
          </view>

          <!-- 操作按钮 -->
          <view class="detail-actions"
          >
            <view v-if="selectedCoupon.status === 'unused'" class="action-btn primary" @click="useCoupon(selectedCoupon)"
          >
              <text>立即使用</text>
            </view>
            <view class="action-btn" @click="shareCoupon(selectedCoupon)"
          >
              <text>分享给好友</text>
            </view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'

const currentTab = ref('unused')
const coupons = ref([])
const showDetailModal = ref(false)
const selectedCoupon = ref(null)

const tabs = [
  { label: '未使用', value: 'unused', count: 0 },
  { label: '已使用', value: 'used', count: 0 },
  { label: '已过期', value: 'expired', count: 0 }
]

// 模拟券数据
const mockCoupons = [
  {
    id: 1,
    name: '星巴克中杯拿铁',
    description: '任意门店可用，仅限中杯',
    type: 'drink',
    value: 33,
    status: 'unused',
    expireDate: '2026-03-15',
    merchant: '星巴克',
    rules: '每单限用一张，不与其他优惠同享'
  },
  {
    id: 2,
    name: '满100减20券',
    description: '全场通用，满100元可用',
    type: 'discount',
    value: 20,
    status: 'unused',
    expireDate: '2026-02-28',
    merchant: '全家便利店',
    rules: '仅限线下门店使用'
  },
  {
    id: 3,
    name: '电影通兑券',
    description: '2D/3D普通厅通兑',
    type: 'entertainment',
    value: 45,
    status: 'unused',
    expireDate: '2026-02-25',
    merchant: '万达影城',
    rules: 'IMAX及特殊场次不可用'
  },
  {
    id: 4,
    name: '肯德基套餐券',
    description: '超值单人套餐',
    type: 'food',
    value: 35,
    status: 'used',
    expireDate: '2026-01-20',
    merchant: '肯德基',
    usedDate: '2026-01-15'
  },
  {
    id: 5,
    name: '超市购物券',
    description: '无门槛抵扣10元',
    type: 'shopping',
    value: 10,
    status: 'expired',
    expireDate: '2026-01-10',
    merchant: '永辉超市'
  }
]

const statsList = computed(() => [
  { label: '未使用', value: coupons.value.filter(c => c.status === 'unused').length, bgColor: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)' },
  { label: '已使用', value: coupons.value.filter(c => c.status === 'used').length, bgColor: 'linear-gradient(135deg, #10b981 0%, #059669 100%)' },
  { label: '已过期', value: coupons.value.filter(c => c.status === 'expired').length, bgColor: 'linear-gradient(135deg, #ef4444 0%, #dc2626 100%)' }
])

const filteredCoupons = computed(() => {
  return coupons.value.filter(c => c.status === currentTab.value)
})

const currentTabLabel = computed(() => {
  const tab = tabs.find(t => t.value === currentTab.value)
  return tab ? tab.label : ''
})

onMounted(() => {
  loadCoupons()
})

const loadCoupons = () => {
  // 模拟加载数据
  coupons.value = mockCoupons
}

const switchTab = (value) => {
  currentTab.value = value
}

const getTypeColor = (type) => {
  const colors = {
    drink: '#5B8FF9',
    food: '#F6BD16',
    discount: '#E8684A',
    entertainment: '#5AD8A6',
    shopping: '#9270CA',
    privilege: '#FF6B6B'
  }
  return colors[type] || '#999'
}

const getTypeIcon = (type) => {
  const icons = {
    drink: '☕',
    food: '🍔',
    discount: '🏷️',
    entertainment: '🎬',
    shopping: '🛒',
    privilege: '👑'
  }
  return icons[type] || '🎫'
}

const getTypeName = (type) => {
  const names = {
    drink: '饮品券',
    food: '美食券',
    discount: '折扣券',
    entertainment: '娱乐券',
    shopping: '购物券',
    privilege: '特权券'
  }
  return names[type] || '优惠券'
}

const isExpiringSoon = (coupon) => {
  if (coupon.status !== 'unused') return false
  const days = Math.ceil((new Date(coupon.expireDate) - new Date()) / (1000 * 60 * 60 * 24))
  return days >= 0 && days <= 7
}

const formatExpireDate = (coupon) => {
  const days = Math.ceil((new Date(coupon.expireDate) - new Date()) / (1000 * 60 * 60 * 24))
  if (days < 0) return '已过期'
  if (days === 0) return '今天到期'
  if (days <= 7) return `${days}天后过期`
  return `${coupon.expireDate}到期`
}

const showCouponDetail = (coupon) => {
  selectedCoupon.value = coupon
  showDetailModal.value = true
}

const closeDetailModal = () => {
  showDetailModal.value = false
  selectedCoupon.value = null
}

const useCoupon = (coupon) => {
  if (coupon.status !== 'unused') return
  
  uni.showModal({
    title: '确认使用',
    content: `确定要使用"${coupon.name}"吗？`,
    success: (res) => {
      if (res.confirm) {
        // 模拟使用
        coupon.status = 'used'
        coupon.usedDate = new Date().toISOString().split('T')[0]
        uni.showToast({ title: '使用成功', icon: 'success' })
        closeDetailModal()
      }
    }
  })
}

const shareCoupon = (coupon) => {
  uni.showShareMenu({
    withShareTicket: true,
    menus: ['shareAppMessage', 'shareTimeline']
  })
}

const generateCode = () => {
  // 生成随机核销码
  return Math.random().toString(36).substring(2, 10).toUpperCase()
}

const showHistory = () => {
  uni.showToast({ title: '使用记录功能开发中', icon: 'none' })
}

const goToShop = () => {
  uni.navigateTo({ url: '/pages/game/points' })
}

const loadMore = () => {
  // 加载更多
}

const goBack = () => {
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
.my-coupons-page {
  min-height: 100vh;
  background: #0f0f23;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 50rpx 30rpx 30rpx;

  .back-btn {
    width: 60rpx;
    height: 60rpx;
    display: flex;
    align-items: center;
    justify-content: center;

    .icon {
      font-size: 40rpx;
      color: #fff;
    }
  }

  .title {
    font-size: 36rpx;
    font-weight: 600;
    color: #fff;
  }

  .history-btn {
    padding: 12rpx 24rpx;
    background: rgba(255,255,255,0.1);
    border-radius: 30rpx;

    text {
      font-size: 24rpx;
      color: #94a3b8;
    }
  }
}

.stats-section {
  display: flex;
  gap: 20rpx;
  padding: 0 30rpx;
  margin-bottom: 30rpx;

  .stats-card {
    flex: 1;
    padding: 30rpx 20rpx;
    border-radius: 20rpx;
    text-align: center;

    .stats-num {
      display: block;
      font-size: 48rpx;
      font-weight: 700;
      color: #fff;
      margin-bottom: 8rpx;
    }

    .stats-label {
      font-size: 24rpx;
      color: rgba(255,255,255,0.7);
    }
  }
}

.category-tabs {
  display: flex;
  gap: 30rpx;
  padding: 0 30rpx 30rpx;
  border-bottom: 1rpx solid rgba(255,255,255,0.1);

  .tab-item {
    position: relative;
    padding: 20rpx 0;
    font-size: 28rpx;
    color: #64748b;

    &.active {
      color: #fff;
      font-weight: 600;

      &::after {
        content: '';
        position: absolute;
        bottom: 0;
        left: 50%;
        transform: translateX(-50%);
        width: 40rpx;
        height: 4rpx;
        background: #667eea;
        border-radius: 2rpx;
      }
    }

    .tab-badge {
      position: absolute;
      top: 10rpx;
      right: -20rpx;
      min-width: 32rpx;
      padding: 0 10rpx;
      height: 32rpx;
      background: #ef4444;
      border-radius: 16rpx;
      font-size: 20rpx;
      color: #fff;
      text-align: center;
      line-height: 32rpx;
    }
  }
}

.coupons-list {
  height: calc(100vh - 400rpx);
  padding: 30rpx;

  .coupon-card {
    display: flex;
    background: rgba(255,255,255,0.05);
    border-radius: 20rpx;
    overflow: hidden;
    margin-bottom: 24rpx;
    border: 1rpx solid rgba(255,255,255,0.08);

    &.used, &.expired {
      opacity: 0.6;
    }

    &.expiring {
      border-color: rgba(245,158,11,0.5);
      box-shadow: 0 0 20rpx rgba(245,158,11,0.1);
    }

    .coupon-type {
      width: 120rpx;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 20rpx 0;

      .type-icon {
        font-size: 48rpx;
        margin-bottom: 8rpx;
      }

      .type-name {
        font-size: 22rpx;
        color: #fff;
      }
    }

    .coupon-content {
      flex: 1;
      padding: 24rpx;
      border-left: 2rpx dashed rgba(255,255,255,0.1);

      .coupon-name {
        display: block;
        font-size: 30rpx;
        font-weight: 600;
        color: #fff;
        margin-bottom: 8rpx;
      }

      .coupon-desc {
        font-size: 24rpx;
        color: #64748b;
        margin-bottom: 16rpx;
      }

      .coupon-meta {
        display: flex;
        gap: 24rpx;

        .meta-item {
          display: flex;
          align-items: center;
          gap: 8rpx;

          .meta-icon {
            font-size: 24rpx;
          }

          .meta-text {
            font-size: 22rpx;
            color: #64748b;

            &.expiring {
              color: #f59e0b;
            }
          }
        }
      }
    }

    .coupon-status {
      width: 140rpx;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 24rpx;

      .status-badge {
        padding: 8rpx 20rpx;
        border-radius: 20rpx;
        margin-bottom: 16rpx;

        &.unused {
          background: rgba(16,185,129,0.2);
          text {
            color: #10b981;
          }
        }

        &.used {
          background: rgba(100,116,139,0.2);
          text {
            color: #64748b;
          }
        }

        &.expired {
          background: rgba(239,68,68,0.2);
          text {
            color: #ef4444;
          }
        }

        text {
          font-size: 22rpx;
        }
      }

      .use-btn {
        padding: 12rpx 24rpx;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        border-radius: 24rpx;

        text {
          font-size: 24rpx;
          color: #fff;
        }
      }
    }
  }

  .empty-state {
    text-align: center;
    padding: 100rpx 0;

    .empty-icon {
      font-size: 80rpx;
      margin-bottom: 20rpx;
    }

    .empty-title {
      display: block;
      font-size: 32rpx;
      color: #fff;
      margin-bottom: 16rpx;
    }

    .empty-desc {
      display: block;
      font-size: 26rpx;
      color: #64748b;
      margin-bottom: 40rpx;
    }

    .empty-btn {
      display: inline-block;
      padding: 24rpx 60rpx;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      border-radius: 40rpx;

      text {
        font-size: 28rpx;
        color: #fff;
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
  z-index: 1000;
  display: flex;
  align-items: flex-end;
  justify-content: center;

  .modal-mask {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0,0,0,0.7);
  }

  .modal-content {
    position: relative;
    width: 100%;
    background: #1a1a2e;
    border-radius: 40rpx 40rpx 0 0;
    max-height: 85vh;
    overflow-y: auto;
    animation: slideUp 0.3s ease;

    @keyframes slideUp {
      from { transform: translateY(100%); }
      to { transform: translateY(0); }
    }
  }
}

.coupon-detail {
  .detail-header {
    padding: 60rpx 40rpx;
    text-align: center;
    border-radius: 40rpx 40rpx 0 0;

    .detail-type {
      display: block;
      font-size: 26rpx;
      color: rgba(255,255,255,0.8);
      margin-bottom: 16rpx;
    }

    .detail-name {
      display: block;
      font-size: 40rpx;
      font-weight: 600;
      color: #fff;
      margin-bottom: 30rpx;
    }

    .detail-value {
      .value-symbol {
        font-size: 40rpx;
        color: #fbbf24;
      }

      .value-num {
        font-size: 80rpx;
        font-weight: 700;
        color: #fbbf24;
      }
    }
  }

  .code-section {
    padding: 40rpx;
    text-align: center;

    .code-label {
      display: block;
      font-size: 28rpx;
      color: #64748b;
      margin-bottom: 20rpx;
    }

    .code-display {
      background: #fff;
      border-radius: 20rpx;
      padding: 40rpx;
      margin-bottom: 20rpx;

      .code-text {
        font-size: 48rpx;
        font-weight: 700;
        color: #333;
        letter-spacing: 8rpx;
        margin-bottom: 20rpx;
      }

      .code-barcode {
        display: flex;
        justify-content: center;
        gap: 4rpx;
        height: 60rpx;
        align-items: center;

        .barcode-line {
          height: 100%;
          background: #333;
        }
      }
    }

    .code-tip {
      font-size: 24rpx;
      color: #94a3b8;
    }
  }

  .info-section {
    padding: 0 40rpx;

    .info-item {
      display: flex;
      justify-content: space-between;
      padding: 24rpx 0;
      border-bottom: 1rpx solid rgba(255,255,255,0.1);

      .info-label {
        font-size: 28rpx;
        color: #64748b;
      }

      .info-value {
        font-size: 28rpx;
        color: #fff;

        &.rules {
          max-width: 400rpx;
          text-align: right;
        }
      }
    }
  }

  .detail-actions {
    padding: 40rpx;
    display: flex;
    gap: 20rpx;

    .action-btn {
      flex: 1;
      padding: 28rpx 0;
      background: rgba(255,255,255,0.1);
      border-radius: 40rpx;
      text-align: center;

      &.primary {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      }

      text {
        font-size: 30rpx;
        color: #fff;
      }
    }
  }
}
</style>