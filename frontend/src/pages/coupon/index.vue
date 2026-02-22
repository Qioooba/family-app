<template>
  <view class="coupon-page">
    <!-- 导航栏 -->
    <view class="nav-bar">
      <view class="back-btn" @click="goBack">
        <u-icon name="arrow-left" size="40" color="#333"></u-icon>
      </view>
      <text class="title">优惠券管理</text>
      <view class="right-btn" @click="showAddModal">
        <u-icon name="plus" size="40" color="#333"></u-icon>
      </view>
    </view>

    <!-- 统计卡片 -->
    <view class="stats-card">
      <view class="stats-item">
        <text class="stats-num">{{ stats.unused }}</text>
        <text class="stats-label">未使用</text>
      </view>
      <view class="stats-divider"></text>
      <view class="stats-item">
        <text class="stats-num used">{{ stats.used }}</text>
        <text class="stats-label">已使用</text>
      </view>
      <view class="stats-divider"></text>
      <view class="stats-item">
        <text class="stats-num expired">{{ stats.expired }}</text>
        <text class="stats-label">已过期</text>
      </view>
    </view>

    <!-- 标签切换 -->
    <view class="tab-bar">
      <view
        v-for="tab in tabs"
        :key="tab.value"
        class="tab-item"
        :class="{ active: currentTab === tab.value }"
        @click="currentTab = tab.value"
      >
        <text>{{ tab.label }}</text>
        <view v-if="currentTab === tab.value" class="tab-line"></text>
      </view>
    </view>

    <!-- 优惠券列表 -->
    <scroll-view scroll-y class="coupon-list" @scrolltolower="loadMore">
      <view v-if="filteredCoupons.length === 0" class="empty-state">
        <u-icon name="coupon" size="100" color="#ddd"></u-icon>
        <text class="empty-title">暂无{{ currentTabLabel }}优惠券</text>
        <text class="empty-desc">点击右上角 + 号添加新优惠券</text>
      </view>

      <view v-else class="coupon-grid">
        <view
          v-for="coupon in filteredCoupons"
          :key="coupon.id"
          class="coupon-card"
          :class="[getCouponStatusClass(coupon)]"
        >
          <!-- 左侧装饰 -->
          <view class="coupon-left">
            <view class="coupon-icon">
              <u-icon name="coupon" size="48" color="#fff"></u-icon>
            </view>
          </view>

          <!-- 中间内容 -->
          <view class="coupon-main">
            <view class="coupon-header">
              <text class="coupon-name">{{ coupon.name }}</text>
              <view v-if="coupon.status === 0" class="status-badge unused">未使用</view>
              <view v-if="coupon.status === 1" class="status-badge used">已使用</view>
              <view v-if="coupon.status === 2" class="status-badge expired">已过期</view>
            </view>

            <view class="coupon-store" v-if="coupon.storeName">
              <u-icon name="home" size="24" color="#999"></u-icon>
              <text>{{ coupon.storeName }}</text>
            </view>

            <view class="coupon-desc" v-if="coupon.description">
              <text>{{ coupon.description }}</text>
            </view>

            <view class="coupon-footer">
              <view class="expire-date">
                <u-icon name="clock" size="24" color="#999"></u-icon>
                <text :class="{ 'expiring-soon': isExpiringSoon(coupon) }">
                  {{ formatExpireDate(coupon.expireDate) }}
                </text>
              </view>
            </view>
          </view>

          <!-- 右侧操作 -->
          <view class="coupon-right">
            <view class="coupon-value" v-if="coupon.value">
              <text class="value-symbol">¥</text>
              <text class="value-num">{{ coupon.value }}</text>
            </view>
            <view class="coupon-discount" v-else-if="coupon.discount">
              <text class="discount-num">{{ coupon.discount }}</text>
              <text class="discount-unit">折</text>
            </view>

            <!-- 操作按钮 -->
            <view class="actions">
              <view
                v-if="coupon.status === 0"
                class="action-btn primary"
                @click="useCoupon(coupon)"
              >
                使用
              </view>
              <view class="action-btn" @click="deleteCoupon(coupon)">
                删除
              </view>
            </view>
          </view>

          <!-- 已使用遮罩 -->
          <view v-if="coupon.status === 1" class="used-mask">
            <text>已使用</text>
          </view>
        </view>
      </view>

      <view v-if="loading" class="loading-more">
        <text>加载中...</text>
      </view>
    </scroll-view>

    <!-- 添加优惠券弹窗 -->
    <view v-if="addModalVisible" class="modal-overlay" @click="closeAddModal">
      <view class="modal-content" @click.stop>
        <view class="modal-header">
          <text>添加优惠券</text>
          <text class="close-btn" @click="closeAddModal">✕</text>
        </view>

        <view class="form-section">
          <view class="form-item">
            <text class="label">优惠券名称 *</text>
            <input
              v-model="form.name"
              placeholder="例如：满100减20"
              class="input"
            />
          </view>

          <view class="form-item">
            <text class="label">商家名称</text>
            <input
              v-model="form.storeName"
              placeholder="例如：星巴克、美团"
              class="input"
            />
          </view>

          <view class="form-item">
            <text class="label">优惠类型</text>
            <view class="type-selector">
              <view
                v-for="type in couponTypes"
                :key="type.value"
                class="type-option"
                :class="{ active: form.type === type.value }"
                @click="form.type = type.value"
              >
                {{ type.label }}
              </view>
            </view>
          </view>

          <view class="form-item" v-if="form.type === 'amount'">
            <text class="label">优惠金额</text>
            <view class="amount-input">
              <text class="currency">¥</text>
              <input
                v-model="form.value"
                type="digit"
                placeholder="0.00"
                class="input"
              />
            </view>
          </view>

          <view class="form-item" v-if="form.type === 'discount'">
            <text class="label">折扣</text>
            <view class="discount-input">
              <input
                v-model="form.discount"
                type="digit"
                placeholder="8.5"
                class="input"
              />
              <text class="unit">折</text>
            </view>
          </view>

          <view class="form-item">
            <text class="label">有效期至</text>
            <view class="date-picker" @click="showDatePicker">
              <text :class="{ 'placeholder': !form.expireDate }"
>
                {{ form.expireDate || '请选择到期日期' }}
              </text>
              <u-icon name="calendar" size="32" color="#999"></u-icon>
            </view>
          </view>

          <view class="form-item">
            <text class="label">备注说明</text>
            <textarea
              v-model="form.description"
              placeholder="例如：满100可用，仅限堂食..."
              class="textarea"
              :maxlength="200"
            />
          </view>
        </view>

        <view class="modal-actions">
          <button class="btn-cancel" @click="closeAddModal">取消</button>
          <button class="btn-confirm" @click="saveCoupon">保存</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { gameApi } from '../../api/index.js'

const coupons = ref([])
const currentTab = ref('all')
const loading = ref(false)
const addModalVisible = ref(false)

const tabs = [
  { label: '全部', value: 'all' },
  { label: '未使用', value: 'unused' },
  { label: '已使用', value: 'used' },
  { label: '已过期', value: 'expired' }
]

const couponTypes = [
  { label: '满减券', value: 'amount' },
  { label: '折扣券', value: 'discount' },
  { label: '礼品券', value: 'gift' }
]

const form = ref({
  name: '',
  storeName: '',
  type: 'amount',
  value: '',
  discount: '',
  expireDate: '',
  description: ''
})

const currentTabLabel = computed(() => {
  const tab = tabs.find(t => t.value === currentTab.value)
  return tab ? tab.label : ''
})

const filteredCoupons = computed(() => {
  if (currentTab.value === 'all') return coupons.value
  return coupons.value.filter(c => {
    const status = getCouponStatus(c)
    return status === currentTab.value
  })
})

const stats = computed(() => {
  return {
    unused: coupons.value.filter(c => getCouponStatus(c) === 'unused').length,
    used: coupons.value.filter(c => getCouponStatus(c) === 'used').length,
    expired: coupons.value.filter(c => getCouponStatus(c) === 'expired').length
  }
})

onMounted(() => {
  loadCoupons()
})

const loadCoupons = async () => {
  try {
    loading.value = true
    const familyInfo = uni.getStorageSync('currentFamily')
    const familyId = familyInfo?.id || 1
    const res = await gameApi.getCoupons(familyId)
    coupons.value = res || []
  } catch (e) {
    console.error('加载优惠券失败', e)
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

const getCouponStatus = (coupon) => {
  if (coupon.status === 1) return 'used'
  if (coupon.expireDate && new Date(coupon.expireDate) < new Date()) {
    return 'expired'
  }
  return 'unused'
}

const getCouponStatusClass = (coupon) => {
  const status = getCouponStatus(coupon)
  return {
    'status-unused': status === 'unused',
    'status-used': status === 'used',
    'status-expired': status === 'expired'
  }
}

const isExpiringSoon = (coupon) => {
  if (!coupon.expireDate || coupon.status !== 0) return false
  const daysUntilExpire = Math.ceil((new Date(coupon.expireDate) - new Date()) / (1000 * 60 * 60 * 24))
  return daysUntilExpire >= 0 && daysUntilExpire <= 7
}

const formatExpireDate = (dateStr) => {
  if (!dateStr) return '无到期日'
  const date = new Date(dateStr)
  const now = new Date()
  const daysUntilExpire = Math.ceil((date - now) / (1000 * 60 * 60 * 24))

  if (daysUntilExpire < 0) return '已过期'
  if (daysUntilExpire === 0) return '今天到期'
  if (daysUntilExpire <= 7) return `${daysUntilExpire}天后到期`

  return `${date.getMonth() + 1}月${date.getDate()}日到期`
}

const showAddModal = () => {
  form.value = {
    name: '',
    storeName: '',
    type: 'amount',
    value: '',
    discount: '',
    expireDate: '',
    description: ''
  }
  addModalVisible.value = true
}

const closeAddModal = () => {
  addModalVisible.value = false
}

const showDatePicker = () => {
  uni.showActionSheet({
    itemList: ['7天后', '15天后', '30天后', '90天后', '自定义'],
    success: (res) => {
      const days = [7, 15, 30, 90][res.tapIndex]
      if (days) {
        const date = new Date()
        date.setDate(date.getDate() + days)
        form.value.expireDate = date.toISOString().split('T')[0]
      } else {
        // 自定义日期选择
        uni.showToast({ title: '请使用输入', icon: 'none' })
      }
    }
  })
}

const saveCoupon = async () => {
  if (!form.value.name.trim()) {
    uni.showToast({ title: '请输入优惠券名称', icon: 'none' })
    return
  }

  try {
    const familyInfo = uni.getStorageSync('currentFamily')
    const data = {
      ...form.value,
      familyId: familyInfo?.id || 1
    }

    await gameApi.addCoupon(data)
    uni.showToast({ title: '添加成功', icon: 'success' })
    closeAddModal()
    loadCoupons()
  } catch (e) {
    console.error('添加优惠券失败', e)
    uni.showToast({ title: '添加失败', icon: 'none' })
  }
}

const useCoupon = async (coupon) => {
  uni.showModal({
    title: '确认使用',
    content: `确定要将"${coupon.name}"标记为已使用吗？`,
    success: async (res) => {
      if (res.confirm) {
        try {
          await gameApi.useCoupon(coupon.id)
          uni.showToast({ title: '已标记为使用', icon: 'success' })
          loadCoupons()
        } catch (e) {
          console.error('使用优惠券失败', e)
          uni.showToast({ title: '操作失败', icon: 'none' })
        }
      }
    }
  })
}

const deleteCoupon = async (coupon) => {
  uni.showModal({
    title: '确认删除',
    content: `确定要删除"${coupon.name}"吗？`,
    confirmColor: '#ff4d4f',
    success: async (res) => {
      if (res.confirm) {
        try {
          await gameApi.deleteCoupon(coupon.id)
          uni.showToast({ title: '删除成功', icon: 'success' })
          loadCoupons()
        } catch (e) {
          console.error('删除优惠券失败', e)
          uni.showToast({ title: '删除失败', icon: 'none' })
        }
      }
    }
  })
}

const loadMore = () => {
  // 加载更多逻辑
}

const goBack = () => {
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
.coupon-page {
  min-height: 100vh;
  background: #f5f6fa;
  display: flex;
  flex-direction: column;
}

.nav-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 30rpx;
  padding-top: 60rpx;
  background: #fff;

  .back-btn, .right-btn {
    width: 60rpx;
    height: 60rpx;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .title {
    font-size: 34rpx;
    font-weight: 600;
    color: #333;
  }
}

.stats-card {
  display: flex;
  justify-content: space-around;
  align-items: center;
  background: #fff;
  margin: 20rpx 30rpx;
  padding: 30rpx;
  border-radius: 20rpx;

  .stats-item {
    text-align: center;

    .stats-num {
      display: block;
      font-size: 40rpx;
      font-weight: bold;
      color: #5B8FF9;
      margin-bottom: 8rpx;

      &.used {
        color: #999;
      }

      &.expired {
        color: #ff4d4f;
      }
    }

    .stats-label {
      font-size: 24rpx;
      color: #666;
    }
  }

  .stats-divider {
    width: 2rpx;
    height: 60rpx;
    background: #f0f0f0;
  }
}

.tab-bar {
  display: flex;
  background: #fff;
  padding: 0 30rpx;
  border-bottom: 1rpx solid #f0f0f0;

  .tab-item {
    position: relative;
    padding: 30rpx 30rpx;
    margin-right: 20rpx;

    text {
      font-size: 28rpx;
      color: #666;
    }

    &.active {
      text {
        font-weight: 600;
        color: #5B8FF9;
      }
    }

    .tab-line {
      position: absolute;
      bottom: 0;
      left: 50%;
      transform: translateX(-50%);
      width: 40rpx;
      height: 4rpx;
      background: #5B8FF9;
      border-radius: 2rpx;
    }
  }
}

.coupon-list {
  flex: 1;
  padding: 20rpx 30rpx;

  .empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 100rpx 0;

    .empty-title {
      margin-top: 30rpx;
      font-size: 30rpx;
      color: #666;
    }

    .empty-desc {
      margin-top: 16rpx;
      font-size: 26rpx;
      color: #999;
    }
  }

  .coupon-grid {
    display: flex;
    flex-direction: column;
    gap: 20rpx;

    .coupon-card {
      display: flex;
      background: #fff;
      border-radius: 20rpx;
      overflow: hidden;
      position: relative;
      box-shadow: 0 4rpx 20rpx rgba(0,0,0,0.05);

      &.status-unused {
        .coupon-left {
          background: linear-gradient(135deg, #5B8FF9 0%, #2E6AD8 100%);
        }
      }

      &.status-used {
        .coupon-left {
          background: linear-gradient(135deg, #ccc 0%, #999 100%);
        }
        opacity: 0.7;
      }

      &.status-expired {
        .coupon-left {
          background: linear-gradient(135deg, #ff4d4f 0%, #cf1322 100%);
        }
        opacity: 0.7;
      }

      .coupon-left {
        width: 100rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        position: relative;

        &::before,
        &::after {
          content: '';
          position: absolute;
          right: -16rpx;
          width: 32rpx;
          height: 32rpx;
          background: #f5f6fa;
          border-radius: 50%;
        }

        &::before {
          top: -16rpx;
        }

        &::after {
          bottom: -16rpx;
        }

        .coupon-icon {
          width: 60rpx;
          height: 60rpx;
          background: rgba(255,255,255,0.2);
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
        }
      }

      .coupon-main {
        flex: 1;
        padding: 24rpx;
        border-right: 2rpx dashed #f0f0f0;

        .coupon-header {
          display: flex;
          align-items: center;
          margin-bottom: 12rpx;

          .coupon-name {
            flex: 1;
            font-size: 30rpx;
            font-weight: 600;
            color: #333;
          }

          .status-badge {
            padding: 4rpx 12rpx;
            border-radius: 8rpx;
            font-size: 20rpx;

            &.unused {
              background: #E6F7FF;
              color: #5B8FF9;
            }

            &.used {
              background: #f5f5f5;
              color: #999;
            }

            &.expired {
              background: #fff1f0;
              color: #ff4d4f;
            }
          }
        }

        .coupon-store {
          display: flex;
          align-items: center;
          gap: 8rpx;
          margin-bottom: 12rpx;

          text {
            font-size: 24rpx;
            color: #666;
          }
        }

        .coupon-desc {
          margin-bottom: 12rpx;

          text {
            font-size: 24rpx;
            color: #999;
            line-height: 1.4;
          }
        }

        .coupon-footer {
          .expire-date {
            display: flex;
            align-items: center;
            gap: 8rpx;

            text {
              font-size: 24rpx;
              color: #999;

              &.expiring-soon {
                color: #ff4d4f;
              }
            }
          }
        }
      }

      .coupon-right {
        width: 160rpx;
        padding: 24rpx;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;

        .coupon-value {
          display: flex;
          align-items: baseline;
          margin-bottom: 16rpx;

          .value-symbol {
            font-size: 28rpx;
            color: #ff4d4f;
          }

          .value-num {
            font-size: 48rpx;
            font-weight: bold;
            color: #ff4d4f;
          }
        }

        .coupon-discount {
          display: flex;
          align-items: baseline;
          margin-bottom: 16rpx;

          .discount-num {
            font-size: 48rpx;
            font-weight: bold;
            color: #ff4d4f;
          }

          .discount-unit {
            font-size: 28rpx;
            color: #ff4d4f;
          }
        }

        .actions {
          display: flex;
          flex-direction: column;
          gap: 12rpx;

          .action-btn {
            padding: 10rpx 24rpx;
            background: #f5f5f5;
            border-radius: 24rpx;
            font-size: 24rpx;
            color: #666;
            text-align: center;

            &.primary {
              background: #5B8FF9;
              color: #fff;
            }
          }
        }
      }

      .used-mask {
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background: rgba(255,255,255,0.6);
        display: flex;
        align-items: center;
        justify-content: center;

        text {
          font-size: 48rpx;
          font-weight: bold;
          color: #999;
          transform: rotate(-30deg);
          border: 4rpx solid #999;
          padding: 20rpx 40rpx;
          border-radius: 20rpx;
        }
      }
    }
  }

  .loading-more {
    text-align: center;
    padding: 30rpx;

    text {
      font-size: 26rpx;
      color: #999;
    }
  }
}

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
  width: 100%;
  background: #fff;
  border-radius: 30rpx 30rpx 0 0;
  padding: 40rpx 30rpx;
  max-height: 85vh;
  overflow-y: auto;

  .modal-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 40rpx;

    text {
      font-size: 36rpx;
      font-weight: 600;
      color: #333;
    }

    .close-btn {
      font-size: 40rpx;
      color: #999;
      padding: 10rpx;
    }
  }

  .form-section {
    .form-item {
      margin-bottom: 30rpx;

      .label {
        display: block;
        font-size: 28rpx;
        color: #333;
        margin-bottom: 16rpx;

        &::after {
          content: '';
          color: #ff4d4f;
        }
      }

      .input {
        width: 100%;
        height: 80rpx;
        background: #f5f6fa;
        border-radius: 12rpx;
        padding: 0 24rpx;
        font-size: 28rpx;
      }

      .type-selector {
        display: flex;
        gap: 20rpx;

        .type-option {
          flex: 1;
          text-align: center;
          padding: 20rpx 0;
          background: #f5f6fa;
          border-radius: 12rpx;
          font-size: 28rpx;
          color: #666;
          border: 2rpx solid transparent;

          &.active {
            background: #E6F7FF;
            border-color: #5B8FF9;
            color: #5B8FF9;
          }
        }
      }

      .amount-input {
        display: flex;
        align-items: center;
        background: #f5f6fa;
        border-radius: 12rpx;
        padding: 0 24rpx;

        .currency {
          font-size: 32rpx;
          color: #ff4d4f;
          margin-right: 12rpx;
        }

        .input {
          flex: 1;
          background: transparent;
          padding: 0;
        }
      }

      .discount-input {
        display: flex;
        align-items: center;
        background: #f5f6fa;
        border-radius: 12rpx;
        padding: 0 24rpx;

        .input {
          flex: 1;
          background: transparent;
          padding: 0;
        }

        .unit {
          font-size: 28rpx;
          color: #666;
          margin-left: 12rpx;
        }
      }

      .date-picker {
        display: flex;
        justify-content: space-between;
        align-items: center;
        height: 80rpx;
        background: #f5f6fa;
        border-radius: 12rpx;
        padding: 0 24rpx;

        text {
          font-size: 28rpx;
          color: #333;

          &.placeholder {
            color: #999;
          }
        }
      }

      .textarea {
        width: 100%;
        height: 160rpx;
        background: #f5f6fa;
        border-radius: 12rpx;
        padding: 20rpx 24rpx;
        font-size: 28rpx;
      }
    }
  }

  .modal-actions {
    display: flex;
    gap: 20rpx;
    margin-top: 40rpx;

    button {
      flex: 1;
      height: 90rpx;
      border-radius: 45rpx;
      font-size: 30rpx;
      border: none;
    }

    .btn-cancel {
      background: #f5f6fa;
      color: #666;
    }

    .btn-confirm {
      background: linear-gradient(135deg, #5B8FF9 0%, #2E6AD8 100%);
      color: #fff;
    }
  }
}
</style>
