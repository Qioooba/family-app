<template>
  <view class="coupons-page">
    <!-- 自定义导航栏 -->
    <view class="custom-nav">
      <view class="nav-back" @click="goBack">
        <text class="icon">‹</text>
      </view>
      <view class="nav-title">优惠券</view>
      <view class="nav-action" @click="showAddModal">
        <text>+ 添加</text>
      </view>
    </view>

    <!-- 统计卡片 -->
    <view class="coupon-stats">
      <view class="stat-card total">
        <text class="stat-icon">🎫</text>
        <text class="stat-value">{{ totalValue }}</text>
        <text class="stat-label">优惠券总价值</text>
      </view>
      <view class="stats-row">
        <view class="stat-item">
          <text class="item-value">{{ activeCount }}</text>
          <text class="item-label">未使用</text>
        </view>
        <view class="stat-item">
          <text class="item-value">{{ expiringCount }}</text>
          <text class="item-label warning">即将过期</text>
        </view>
        <view class="stat-item">
          <text class="item-value">{{ usedCount }}</text>
          <text class="item-label">已使用</text>
        </view>
      </view>
    </view>

    <!-- 状态筛选 -->
    <view class="filter-tabs">
      <view 
        v-for="tab in filterTabs" 
        :key="tab.value"
        class="tab-item"
        :class="{ active: currentTab === tab.value }"
        @click="currentTab = tab.value"
      >
        <text>{{ tab.label }}</text>
        <text v-if="tab.count > 0" class="tab-badge">{{ tab.count }}</text>
      </view>
    </view>

    <!-- 优惠券列表 -->
    <scroll-view class="coupon-list" scroll-y @scrolltolower="loadMore">
      <view 
        v-for="coupon in filteredCoupons" 
        :key="coupon.id"
        class="coupon-card"
        :class="[coupon.status, { expiring: isExpiringSoon(coupon) }]"
        @click="viewDetail(coupon)"
      >
        <view class="coupon-left">
          <view class="discount-value">
            <text class="currency">{{ coupon.type === 'discount' ? '¥' : '' }}</text>
            <text class="amount">{{ coupon.value }}</text>
            <text v-if="coupon.type === 'discount'" class="unit">折</text>
          </view>
          <text class="condition">{{ coupon.condition }}</text>
        </view>

        <view class="coupon-divider"></text>

        <view class="coupon-right">
          <view class="coupon-header">
            <text class="coupon-name">{{ coupon.name }}</text>
            <view v-if="isExpiringSoon(coupon)" class="expiring-tag">即将过期</view>
          </view>
          
          <text class="coupon-store">🏪 {{ coupon.store }}</text>
          <text class="coupon-date">有效期至 {{ coupon.expireDate }}</text>
          
          <view class="coupon-tags">
            <text v-for="tag in coupon.tags" :key="tag" class="tag">{{ tag }}</text>
          </view>
        </view>

        <view v-if="coupon.status === 'used'" class="used-overlay">
          <text>已使用</text>
        </view>
        <view v-if="coupon.status === 'expired'" class="expired-overlay">
          <text>已过期</text>
        </view>
      </view>

      <view class="empty-state" v-if="filteredCoupons.length === 0">
        <text class="empty-icon">🎫</text>
        <text class="empty-text">暂无优惠券</text>
      </view>

      <view class="loading-more" v-if="loading">
        <text>加载中...</text>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'

const loading = ref(false)
const currentTab = ref('active')

const filterTabs = ref([
  { label: '未使用', value: 'active', count: 5 },
  { label: '已使用', value: 'used', count: 3 },
  { label: '已过期', value: 'expired', count: 2 }
])

const coupons = ref([
  {
    id: 1,
    name: '全场通用券',
    store: '永辉超市',
    type: 'amount',
    value: '50',
    condition: '满200元可用',
    expireDate: '2025-02-25',
    status: 'active',
    tags: ['全场通用', '可叠加']
  },
  {
    id: 2,
    name: '生鲜专享券',
    store: '盒马鲜生',
    type: 'amount',
    value: '30',
    condition: '满100元可用',
    expireDate: '2025-02-23',
    status: 'active',
    tags: ['生鲜专享']
  },
  {
    id: 3,
    name: '周末折扣券',
    store: '沃尔玛',
    type: 'discount',
    value: '8.5',
    condition: '满150元可用',
    expireDate: '2025-02-24',
    status: 'active',
    tags: ['周末可用']
  },
  {
    id: 4,
    name: '新用户专享',
    store: '叮咚买菜',
    type: 'amount',
    value: '20',
    condition: '无门槛',
    expireDate: '2025-02-28',
    status: 'active',
    tags: ['新人专享']
  },
  {
    id: 5,
    name: '满减优惠券',
    store: '美团买菜',
    type: 'amount',
    value: '15',
    condition: '满80元可用',
    expireDate: '2025-02-22',
    status: 'active',
    tags: ['限时']
  },
  {
    id: 6,
    name: '年货节优惠券',
    store: '京东到家',
    type: 'amount',
    value: '100',
    condition: '满500元可用',
    expireDate: '2025-01-31',
    status: 'used',
    tags: ['已核销']
  },
  {
    id: 7,
    name: '品牌折扣券',
    store: '天猫超市',
    type: 'discount',
    value: '9',
    condition: '满200元可用',
    expireDate: '2025-01-15',
    status: 'expired',
    tags: ['已过期']
  }
])

const filteredCoupons = computed(() => {
  return coupons.value.filter(c => c.status === currentTab.value)
})

const totalValue = computed(() => {
  return coupons.value
    .filter(c => c.status === 'active')
    .reduce((sum, c) => sum + parseInt(c.value), 0)
})

const activeCount = computed(() => coupons.value.filter(c => c.status === 'active').length)
const expiringCount = computed(() => coupons.value.filter(c => c.status === 'active' && isExpiringSoon(c)).length)
const usedCount = computed(() => coupons.value.filter(c => c.status === 'used').length)

const isExpiringSoon = (coupon) => {
  const expireDate = new Date(coupon.expireDate)
  const today = new Date()
  const diffDays = Math.ceil((expireDate - today) / (1000 * 60 * 60 * 24))
  return diffDays <= 3 && diffDays >= 0
}

const goBack = () => {
  uni.navigateBack()
}

const showAddModal = () => {
  uni.showToast({ title: '添加优惠券功能', icon: 'none' })
}

const viewDetail = (coupon) => {
  uni.showModal({
    title: coupon.name,
    content: `适用店铺: ${coupon.store}\n优惠: ${coupon.type === 'discount' ? coupon.value + '折' : '¥' + coupon.value}\n使用条件: ${coupon.condition}\n有效期: ${coupon.expireDate}`,
    showCancel: false
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
.coupons-page {
  min-height: 100vh;
  background: #0f0f23;
}

.custom-nav {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 50px 20px 20px;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);

  .nav-back {
    width: 36px;
    height: 36px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: rgba(255,255,255,0.1);
    border-radius: 10px;

    .icon {
      font-size: 24px;
      color: #fff;
    }
  }

  .nav-title {
    font-size: 18px;
    font-weight: 600;
    color: #fff;
  }

  .nav-action {
    padding: 8px 16px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 8px;
    font-size: 13px;
    color: #fff;
  }
}

.coupon-stats {
  padding: 15px;

  .stat-card {
    padding: 20px;
    border-radius: 16px;
    text-align: center;
    margin-bottom: 15px;

    &.total {
      background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
    }

    .stat-icon {
      font-size: 36px;
      margin-bottom: 8px;
    }

    .stat-value {
      display: block;
      font-size: 36px;
      font-weight: 700;
      color: #fff;
      margin-bottom: 4px;
    }

    .stat-label {
      font-size: 13px;
      color: rgba(255,255,255,0.8);
    }
  }

  .stats-row {
    display: flex;
    gap: 10px;

    .stat-item {
      flex: 1;
      padding: 15px;
      background: rgba(255,255,255,0.05);
      border-radius: 12px;
      text-align: center;

      .item-value {
        display: block;
        font-size: 22px;
        font-weight: 700;
        color: #fff;
        margin-bottom: 4px;
      }

      .item-label {
        font-size: 12px;
        color: #64748b;

        &.warning {
          color: #f59e0b;
        }
      }
    }
  }
}

.filter-tabs {
  display: flex;
  padding: 0 15px 15px;
  gap: 10px;

  .tab-item {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 6px;
    padding: 12px;
    background: rgba(255,255,255,0.05);
    border-radius: 10px;
    font-size: 14px;
    color: #64748b;
    transition: all 0.3s;

    &.active {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: #fff;
    }

    .tab-badge {
      min-width: 18px;
      height: 18px;
      padding: 0 6px;
      background: rgba(239,68,68,0.8);
      border-radius: 9px;
      font-size: 11px;
      color: #fff;
      display: flex;
      align-items: center;
      justify-content: center;
    }
  }
}

.coupon-list {
  padding: 0 15px 30px;

  .coupon-card {
    display: flex;
    background: rgba(255,255,255,0.05);
    border-radius: 16px;
    margin-bottom: 12px;
    overflow: hidden;
    position: relative;

    &.expiring {
      border: 1px solid rgba(245,158,11,0.3);
    }

    &.used,
    &.expired {
      opacity: 0.6;
    }

    .coupon-left {
      width: 100px;
      padding: 20px;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;

      .discount-value {
        display: flex;
        align-items: baseline;
        margin-bottom: 4px;

        .currency {
          font-size: 16px;
          color: #fff;
        }

        .amount {
          font-size: 32px;
          font-weight: 700;
          color: #fff;
        }

        .unit {
          font-size: 14px;
          color: #fff;
        }
      }

      .condition {
        font-size: 11px;
        color: rgba(255,255,255,0.8);
      }
    }

    .coupon-divider {
      width: 1px;
      background: rgba(255,255,255,0.1);
      position: relative;

      &::before,
      &::after {
        content: '';
        position: absolute;
        left: -6px;
        width: 12px;
        height: 12px;
        background: #0f0f23;
        border-radius: 50%;
      }

      &::before {
        top: -6px;
      }

      &::after {
        bottom: -6px;
      }
    }

    .coupon-right {
      flex: 1;
      padding: 16px;

      .coupon-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 8px;

        .coupon-name {
          font-size: 15px;
          font-weight: 600;
          color: #fff;
        }

        .expiring-tag {
          padding: 2px 8px;
          background: rgba(245,158,11,0.2);
          border-radius: 4px;
          font-size: 10px;
          color: #f59e0b;
        }
      }

      .coupon-store {
        display: block;
        font-size: 13px;
        color: #94a3b8;
        margin-bottom: 4px;
      }

      .coupon-date {
        display: block;
        font-size: 12px;
        color: #64748b;
        margin-bottom: 8px;
      }

      .coupon-tags {
        display: flex;
        flex-wrap: wrap;
        gap: 6px;

        .tag {
          padding: 2px 8px;
          background: rgba(59,130,246,0.1);
          border-radius: 4px;
          font-size: 10px;
          color: #3b82f6;
        }
      }
    }

    .used-overlay,
    .expired-overlay {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: rgba(0,0,0,0.5);
      display: flex;
      align-items: center;
      justify-content: center;

      text {
        font-size: 24px;
        color: #fff;
        font-weight: 700;
        transform: rotate(-15deg);
        border: 2px solid #fff;
        padding: 8px 20px;
        border-radius: 8px;
      }
    }
  }

  .empty-state {
    text-align: center;
    padding: 60px 20px;

    .empty-icon {
      font-size: 60px;
      margin-bottom: 16px;
    }

    .empty-text {
      font-size: 15px;
      color: #64748b;
    }
  }
}

.loading-more {
  text-align: center;
  padding: 20px;
  color: #64748b;
  font-size: 13px;
}
</style>
