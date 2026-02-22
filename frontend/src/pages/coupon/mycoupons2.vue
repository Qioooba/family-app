<template>
  <view class="mycoupons-page">
    <!-- 顶部导航 -->
    <view class="nav-bar"
>
      <view class="back-btn" @click="goBack"
>
        <u-icon name="arrow-left" size="40" color="#333"></u-icon>
      </view>
      <text class="title">我的券包</text>
      <view class="right-btn" @click="showExpired">
        <text>已过期</text>
      </view>
    </view>

    <view class="content">
      <!-- 统计卡片 -->
      <view class="stats-section">
        <view class="stats-card"
>
          <view class="stat-item"
>
            <text class="stat-value">{{ stats.total }}</text>
            <text class="stat-label">全部券</text>
          </view>
          
          <view class="stat-divider"></view>
          
          <view class="stat-item highlight"
>
            <text class="stat-value">{{ stats.available }}</text>
            <text class="stat-label">未使用</text>
          </view>
          
          <view class="stat-divider"></view>
          
          <view class="stat-item"
>
            <text class="stat-value">¥{{ stats.totalValue }}</text>
            <text class="stat-label">总价值</text>
          </view>
        </view>
      </view>

      <!-- 分类标签 -->
      <view class="category-section"
>
        <scroll-view class="category-list" scroll-x>
          <view
            v-for="cat in categories"
            :key="cat.value"
            class="category-item"
            :class="{ active: currentCategory === cat.value }"
            @click="currentCategory = cat.value"
          >
            <text class="category-name">{{ cat.name }}</text>
            <text v-if="cat.count" class="category-count">{{ cat.count }}</text>
          </view>
        </scroll-view>
      </view>

      <!-- 券列表 -->
      <view class="coupons-list">
        <view
          v-for="coupon in filteredCoupons"
          :key="coupon.id"
          class="coupon-card"
          :class="{ expired: coupon.status === 'expired', used: coupon.status === 'used' }"
        >
          <!-- 券面左侧 -->
          <view class="coupon-left"
>
            <view class="coupon-type-icon"
>{{ coupon.icon }}</view>
            
            <view class="coupon-amount"
>
              <text v-if="coupon.type === 'discount'" class="discount-text"
>{{ coupon.discount }}折</text>
              <view v-else class="amount-wrap"
>
                <text class="amount-symbol">¥</text>
                <text class="amount-value">{{ coupon.amount }}</text>
              </view>
            </view>
            
            <text class="coupon-condition">{{ coupon.condition }}</text>
          </view>

          <!-- 券面右侧 -->
          <view class="coupon-right"
>
            <view class="coupon-header"
>
              <view class="coupon-info"
>
                <text class="coupon-name">{{ coupon.name }}</text>
                <text class="coupon-shop">{{ coupon.shop }}</text>
              </view>
              
              <view class="coupon-status" :class="coupon.status">
                {{ statusText[coupon.status] }}
              </view>
            </view>

            <view class="coupon-validity"
>
              <text>有效期至 {{ coupon.expiryDate }}</text>
              <text v-if="coupon.daysLeft > 0 && coupon.status === 'available'" class="days-left"
>
                {{ coupon.daysLeft }}天后过期
              </text>
            </view>

            <!-- 核销码区域 -->
            <view v-if="coupon.status === 'available'" class="coupon-code-section"
>
              <view class="code-toggle" @click="toggleCode(coupon)">
                <text>{{ coupon.showCode ? '隐藏核销码' : '查看核销码' }}</text>
                <u-icon :name="coupon.showCode ? 'eye-off' : 'eye'" size="24" color="#5B8FF9"></u-icon>
              </view>

              <view v-if="coupon.showCode" class="code-display"
>
                <view class="barcode"
>
                  <view class="barcode-line"
>
                    <view 
                      v-for="n in 30" 
                      :key="n"
                      class="bar"
                      :style="{ width: Math.random() * 4 + 2 + 'px' }"
                    ></view>
                  </view>
                  <text class="barcode-num">{{ coupon.code }}</text>
                </view>
                
                <view class="qrcode"
>
                  <view class="qr-grid">
                    <view 
                      v-for="n in 36" 
                      :key="n"
                      class="qr-cell"
                      :class="{ filled: Math.random() > 0.5 }"
                    ></view>
                  </view>
                </view>

                <view class="code-actions"
>
                  <text class="action-link" @click="copyCode(coupon.code)">复制</text>
                  <text class="action-link" @click="shareCode(coupon)">分享</text>
                </view>
              </view>
            </view>

            <!-- 操作按钮 -->
            <view v-if="coupon.status === 'available'" class="coupon-actions"
>
              <text class="use-btn" @click="useCoupon(coupon)">立即使用</text>
              <text class="detail-link" @click="showDetail(coupon)">详情 ›</text>
            </view>
          </view>
        </view>

        <!-- 空状态 -->
        <view v-if="filteredCoupons.length === 0" class="empty-state"
>
          <u-icon name="ticket" size="100" color="#ddd"></u-icon>
          <text class="empty-text">暂无优惠券</text>
          <text class="empty-hint" @click="goToGet">去领券中心 ›</text>
        </view>
      </view>
    </view>

    <!-- 详情弹窗 -->
    <u-popup
      v-model:show="detailModalVisible"
      mode="bottom"
      round
      closeable
    >
      <view v-if="selectedCoupon" class="detail-modal"
>
        <view class="detail-header"
>
          <view 
            class="detail-icon"
            :style="{ background: selectedCoupon.color + '20', color: selectedCoupon.color }"
          >
            {{ selectedCoupon.icon }}
          </view>          
          
          <text class="detail-name">{{ selectedCoupon.name }}</text>
          
          <view class="detail-amount"
>
            <text v-if="selectedCoupon.type === 'discount'" class="discount"
>{{ selectedCoupon.discount }}折</text>
            <view v-else class="amount">
              <text class="symbol">¥</text>
              <text class="value">{{ selectedCoupon.amount }}</text>
            </view>
          </view>
        </view>

        <view class="detail-info"
>
          <view class="info-row">
            <text class="info-label">使用条件</text>
            <text class="info-value">{{ selectedCoupon.condition }}</text>
          </view>          
          
          <view class="info-row">
            <text class="info-label">适用商家</text>
            <text class="info-value">{{ selectedCoupon.shop }}</text>
          </view>          
          
          <view class="info-row">
            <text class="info-label">有效期</text>
            <text class="info-value">至 {{ selectedCoupon.expiryDate }}</text>
          </view>          
          
          <view class="info-row">
            <text class="info-label">券码</text>
            <view class="code-row">
              <text class="info-value code">{{ selectedCoupon.code }}</text>
              <text class="copy-link" @click="copyCode(selectedCoupon.code)">复制</text>
            </view>
          </view>          
          
          <view v-if="selectedCoupon.description" class="info-row"
>
            <text class="info-label">使用说明</text>
            <text class="info-value">{{ selectedCoupon.description }}</text>
          </view>
        </view>

        <view class="detail-actions"
>
          <view 
            v-if="selectedCoupon.status === 'available'" 
            class="btn-use"
            @click="useCoupon(selectedCoupon)"
          >
            立即使用
          </view>          
          
          <view class="btn-close" @click="detailModalVisible = false">关闭</view>
        </view>
      </view>
    </u-popup>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import dayjs from 'dayjs'

// 响应式数据
const currentCategory = ref('all')
const selectedCoupon = ref(null)
const detailModalVisible = ref(false)

// 状态文本
const statusText = {
  available: '未使用',
  used: '已使用',
  expired: '已过期'
}

// 分类
const categories = ref([
  { name: '全部', value: 'all', count: 6 },
  { name: '美食', value: 'food', count: 2 },
  { name: '购物', value: 'shopping', count: 2 },
  { name: '出行', value: 'travel', count: 1 },
  { name: '娱乐', value: 'entertainment', count: 1 }
])

// 优惠券数据
const coupons = ref([
  {
    id: 1,
    name: '麦当劳超值套餐券',
    shop: '麦当劳',
    type: 'amount',
    amount: 20,
    condition: '满50可用',
    expiryDate: '2026-03-31',
    daysLeft: 35,
    code: 'MDL2026032001',
    status: 'available',
    icon: '🍔',
    color: '#FF6B6B',
    category: 'food',
    showCode: false,
    description: '仅限指定门店使用，不可与其他优惠叠加'
  },
  {
    id: 2,
    name: '星巴克买一送一',
    shop: '星巴克',
    type: 'discount',
    discount: 5,
    condition: '任意饮品',
    expiryDate: '2026-03-15',
    daysLeft: 19,
    code: 'SBK85OFF2026',
    status: 'available',
    icon: '☕',
    color: '#52C41A',
    category: 'food',
    showCode: false,
    description: '限指定饮品使用'
  },
  {
    id: 3,
    name: '京东满减券',
    shop: '京东',
    type: 'amount',
    amount: 50,
    condition: '满299可用',
    expiryDate: '2026-03-10',
    daysLeft: 14,
    code: 'JD50OFF2026',
    status: 'available',
    icon: '📦',
    color: '#FAAD14',
    category: 'shopping',
    showCode: false,
    description: '全品类可用，部分商品除外'
  },
  {
    id: 4,
    name: '滴滴出行券',
    shop: '滴滴出行',
    type: 'amount',
    amount: 10,
    condition: '无门槛',
    expiryDate: '2026-03-05',
    daysLeft: 9,
    code: 'DD10YUAN2026',
    status: 'available',
    icon: '🚗',
    color: '#1890FF',
    category: 'travel',
    showCode: false,
    description: '快车、专车可用'
  },
  {
    id: 5,
    name: '美团电影票券',
    shop: '美团',
    type: 'amount',
    amount: 15,
    condition: '满60可用',
    expiryDate: '2026-02-28',
    daysLeft: 2,
    code: 'MTMOVIE2026',
    status: 'available',
    icon: '🎬',
    color: '#722ED1',
    category: 'entertainment',
    showCode: false,
    description: '2D/3D电影可用'
  },
  {
    id: 6,
    name: '淘宝购物券',
    shop: '淘宝',
    type: 'amount',
    amount: 30,
    condition: '满199可用',
    expiryDate: '2026-02-20',
    daysLeft: 0,
    code: 'TB30OFFUSED',
    status: 'used',
    icon: '🛍️',
    color: '#FAAD14',
    category: 'shopping',
    showCode: false,
    description: '已使用'
  }
])

// 计算属性
const stats = computed(() => {
  const total = coupons.value.length
  const available = coupons.value.filter(c => c.status === 'available').length
  const totalValue = coupons.value
    .filter(c => c.status === 'available')
    .reduce((sum, c) => sum + (c.amount || 0), 0)
  
  return { total, available, totalValue }
})

const filteredCoupons = computed(() => {
  let list = coupons.value.filter(c => c.status !== 'expired')
  
  if (currentCategory.value !== 'all') {
    list = list.filter(c => c.category === currentCategory.value)
  }
  
  // 按状态和时间排序
  return list.sort((a, b) => {
    if (a.status !== b.status) {
      return a.status === 'available' ? -1 : 1
    }
    return dayjs(a.expiryDate).valueOf() - dayjs(b.expiryDate).valueOf()
  })
})

// 方法
const toggleCode = (coupon) => {
  coupon.showCode = !coupon.showCode
}

const copyCode = (code) => {
  uni.setClipboardData({
    data: code,
    success: () => {
      uni.showToast({ title: '已复制', icon: 'success' })
    }
  })
}

const shareCode = (coupon) => {
  uni.showShareMenu({
    withShareTicket: true
  })
}

const useCoupon = (coupon) => {
  uni.showActionSheet({
    itemList: ['线上使用', '线下扫码', '赠送给好友'],
    success: (res) => {
      switch (res.tapIndex) {
        case 0:
          uni.showToast({ title: '跳转商家小程序', icon: 'none' })
          break
        case 1:
          coupon.showCode = true
          break
        case 2:
          shareCode(coupon)
          break
      }
    }
  })
}

const showDetail = (coupon) => {
  selectedCoupon.value = coupon
  detailModalVisible.value = true
}

const showExpired = () => {
  uni.showToast({ title: '查看已过期', icon: 'none' })
}

const goToGet = () => {
  uni.navigateTo({ url: '/pages/coupon/index' })
}

const goBack = () => {
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
.mycoupons-page {
  min-height: 100vh;
  background: #f5f6fa;
}

.nav-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 30rpx;
  padding-top: 60rpx;
  background: linear-gradient(135deg, #FF6B6B 0%, #FF8E8E 100%);

  .back-btn {
    width: 60rpx;
    height: 60rpx;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .title {
    font-size: 34rpx;
    font-weight: 600;
    color: #fff;
  }

  .right-btn {
    padding: 10rpx 24rpx;
    background: rgba(255, 255, 255, 0.2);
    border-radius: 30rpx;

    text {
      font-size: 26rpx;
      color: #fff;
    }
  }
}

.content {
  padding: 0 30rpx 50rpx;
}

// 统计卡片
.stats-section {
  margin-top: -40rpx;
  position: relative;
  z-index: 1;
  margin-bottom: 30rpx;
}

.stats-card {
  display: flex;
  align-items: center;
  justify-content: space-around;
  background: #fff;
  border-radius: 20rpx;
  padding: 30rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.08);

  .stat-item {
    display: flex;
    flex-direction: column;
    align-items: center;

    .stat-value {
      font-size: 40rpx;
      font-weight: 700;
      color: #333;
      margin-bottom: 8rpx;
    }

    .stat-label {
      font-size: 24rpx;
      color: #999;
    }

    &.highlight {
      .stat-value {
        color: #FF6B6B;
      }
    }
  }

  .stat-divider {
    width: 1rpx;
    height: 60rpx;
    background: #f0f0f0;
  }
}

// 分类
.category-section {
  margin-bottom: 30rpx;
}

.category-list {
  white-space: nowrap;

  .category-item {
    display: inline-flex;
    align-items: center;
    gap: 8rpx;
    padding: 16rpx 24rpx;
    background: #fff;
    border-radius: 30rpx;
    margin-right: 16rpx;

    .category-name {
      font-size: 26rpx;
      color: #666;
    }

    .category-count {
      padding: 4rpx 12rpx;
      background: #f5f5f5;
      border-radius: 20rpx;
      font-size: 22rpx;
      color: #999;
    }

    &.active {
      background: #FF6B6B;

      .category-name {
        color: #fff;
      }

      .category-count {
        background: rgba(255, 255, 255, 0.3);
        color: #fff;
      }
    }
  }
}

// 券列表
.coupons-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.coupon-card {
  display: flex;
  background: #fff;
  border-radius: 20rpx;
  overflow: hidden;

  &.expired, &.used {
    opacity: 0.6;

    .coupon-left {
      background: #ccc;
    }
  }
}

.coupon-left {
  width: 200rpx;
  background: linear-gradient(135deg, #FF6B6B 0%, #FF8E8E 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 24rpx;
  position: relative;

  .coupon-type-icon {
    font-size: 48rpx;
    margin-bottom: 12rpx;
  }

  .coupon-amount {
    margin-bottom: 8rpx;

    .discount-text {
      font-size: 40rpx;
      font-weight: 700;
      color: #fff;
    }

    .amount-wrap {
      display: flex;
      align-items: flex-start;

      .amount-symbol {
        font-size: 28rpx;
        font-weight: 600;
        color: #fff;
      }

      .amount-value {
        font-size: 56rpx;
        font-weight: 700;
        color: #fff;
        line-height: 1;
      }
    }
  }

  .coupon-condition {
    font-size: 22rpx;
    color: rgba(255, 255, 255, 0.9);
  }
}

.coupon-right {
  flex: 1;
  padding: 24rpx;

  .coupon-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 12rpx;

    .coupon-info {
      flex: 1;
      min-width: 0;

      .coupon-name {
        font-size: 30rpx;
        font-weight: 600;
        color: #333;
        display: block;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .coupon-shop {
        font-size: 24rpx;
        color: #999;
        margin-top: 4rpx;
        display: block;
      }
    }

    .coupon-status {
      padding: 6rpx 16rpx;
      border-radius: 8rpx;
      font-size: 22rpx;
      margin-left: 12rpx;

      &.available {
        background: #F6FFED;
        color: #52C41A;
      }

      &.used {
        background: #f5f5f5;
        color: #999;
      }

      &.expired {
        background: #f5f5f5;
        color: #999;
      }
    }
  }

  .coupon-validity {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16rpx;

    text {
      font-size: 24rpx;
      color: #999;
    }

    .days-left {
      color: #FF6B6B;
    }
  }

  .coupon-code-section {
    margin-bottom: 16rpx;

    .code-toggle {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 8rpx;
      padding: 16rpx;
      background: #f0f5ff;
      border-radius: 12rpx;

      text {
        font-size: 26rpx;
        color: #5B8FF9;
      }
    }

    .code-display {
      margin-top: 16rpx;
      padding: 24rpx;
      background: #f5f6fa;
      border-radius: 12rpx;
      text-align: center;

      .barcode {
        margin-bottom: 20rpx;

        .barcode-line {
          display: flex;
          justify-content: center;
          align-items: center;
          height: 60rpx;
          gap: 2rpx;
          margin-bottom: 12rpx;

          .bar {
            height: 100%;
            background: #333;
          }
        }

        .barcode-num {
          font-size: 24rpx;
          color: #666;
          font-family: monospace;
          letter-spacing: 4rpx;
        }
      }

      .qrcode {
        display: flex;
        justify-content: center;
        margin-bottom: 20rpx;

        .qr-grid {
          display: grid;
          grid-template-columns: repeat(6, 1fr);
          gap: 4rpx;
          width: 180rpx;
          height: 180rpx;

          .qr-cell {
            aspect-ratio: 1;
            background: #f5f5f5;

            &.filled {
              background: #333;
            }
          }
        }
      }

      .code-actions {
        display: flex;
        justify-content: center;
        gap: 40rpx;

        .action-link {
          font-size: 26rpx;
          color: #5B8FF9;
        }
      }
    }
  }

  .coupon-actions {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .use-btn {
      padding: 12rpx 32rpx;
      background: linear-gradient(135deg, #FF6B6B 0%, #FF8E8E 100%);
      border-radius: 24rpx;
      font-size: 26rpx;
      color: #fff;
    }

    .detail-link {
      font-size: 26rpx;
      color: #999;
    }
  }
}

// 空状态
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 100rpx 0;

  .empty-text {
    font-size: 28rpx;
    color: #999;
    margin-top: 20rpx;
  }

  .empty-hint {
    font-size: 26rpx;
    color: #FF6B6B;
    margin-top: 12rpx;
  }
}

// 详情弹窗
.detail-modal {
  padding: 30rpx 0;

  .detail-header {
    text-align: center;
    padding: 0 30rpx 30rpx;
    border-bottom: 1rpx solid #f5f5f5;

    .detail-icon {
      width: 120rpx;
      height: 120rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 50%;
      font-size: 60rpx;
      margin: 0 auto 20rpx;
    }

    .detail-name {
      font-size: 32rpx;
      font-weight: 600;
      color: #333;
      display: block;
      margin-bottom: 16rpx;
    }

    .detail-amount {
      .discount {
        font-size: 56rpx;
        font-weight: 700;
        color: #FF6B6B;
      }

      .amount {
        .symbol {
          font-size: 32rpx;
          color: #FF6B6B;
        }

        .value {
          font-size: 72rpx;
          font-weight: 700;
          color: #FF6B6B;
        }
      }
    }
  }

  .detail-info {
    padding: 30rpx;

    .info-row {
      display: flex;
      justify-content: space-between;
      padding: 20rpx 0;
      border-bottom: 1rpx solid #f5f5f5;

      .info-label {
        font-size: 28rpx;
        color: #999;
      }

      .info-value {
        font-size: 28rpx;
        color: #333;

        &.code {
          font-family: monospace;
          background: #f5f6fa;
          padding: 8rpx 16rpx;
          border-radius: 8rpx;
        }
      }

      .code-row {
        display: flex;
        align-items: center;
        gap: 16rpx;

        .copy-link {
          font-size: 26rpx;
          color: #5B8FF9;
        }
      }
    }
  }

  .detail-actions {
    display: flex;
    gap: 20rpx;
    padding: 0 30rpx;

    .btn-use {
      flex: 2;
      padding: 24rpx 0;
      background: linear-gradient(135deg, #FF6B6B 0%, #FF8E8E 100%);
      border-radius: 12rpx;
      text-align: center;
      font-size: 30rpx;
      color: #fff;
    }

    .btn-close {
      flex: 1;
      padding: 24rpx 0;
      background: #f5f5f5;
      border-radius: 12rpx;
      text-align: center;
      font-size: 30rpx;
      color: #666;
    }
  }
}
</style>