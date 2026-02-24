<template>
  <view class="coupon-page">
    <!-- 顶部导航 -->
    <view class="nav-bar">
      <view class="back-btn" @click="goBack"
>
        <up-icon name="arrow-left" size="40" color="#333"></up-icon>
      </view>
      <text class="title">优惠券</text>
      <view class="right-btn" @click="showAddModal"
>
        <up-icon name="plus" size="36" color="#fff"></up-icon>
      </view>
    </view>

    <view class="content">
      <!-- 统计卡片 -->
      <view class="stats-cards">
        <view class="stat-card">
          <text class="stat-value">{{ stats.total }}</text>
          <text class="stat-label">全部券</text>
        </view>
        
        <view class="stat-card active">
          <text class="stat-value">{{ stats.active }}</text>
          <text class="stat-label">可用</text>
        </view>
        
        <view class="stat-card warning">
          <text class="stat-value">{{ stats.expiring }}</text>
          <text class="stat-label">即将过期</text>
        </view>
        
        <view class="stat-card">
          <text class="stat-value">¥{{ stats.totalValue }}</text>
          <text class="stat-label">总价值</text>
        </view>
      </view>

      <!-- 筛选标签 -->
      <view class="filter-tabs">
        <view
          v-for="tab in filterTabs"
          :key="tab.value"
          class="filter-tab"
          :class="{ active: currentFilter === tab.value }"
          @click="currentFilter = tab.value"
        >
          {{ tab.label }}
          <text v-if="tab.badge" class="tab-badge">{{ tab.badge }}</text>
        </view>
      </view>

      <!-- 优惠券列表 -->
      <scroll-view class="coupon-list" scroll-y refresher-enabled @refresherrefresh="onRefresh"
>
        <view
          v-for="coupon in filteredCoupons"
          :key="coupon.id"
          class="coupon-card"
          :class="{ 
            expired: coupon.status === 'expired',
            used: coupon.status === 'used',
            expiring: coupon.isExpiring && coupon.status === 'active'
          }"
        >
          <!-- 券面设计 -->
          <view class="coupon-face">
            <!-- 左侧金额 -->
            <view class="coupon-left">
              <view class="coupon-amount"
>
                <text v-if="coupon.type === 'discount'" class="amount-symbol">{{ coupon.discount }}折</text>
                <view v-else class="amount-wrap"
>
                  <text class="amount-symbol">¥</text>
                  <text class="amount-value">{{ coupon.amount }}</text>
                </view>
              </view>
              
              <text class="coupon-condition">{{ coupon.condition }}</text>
              
              <!-- 锯齿边缘效果 -->
              <view class="sawtooth"></view>
            </view>

            <!-- 右侧信息 -->
            <view class="coupon-right"
>
              <view class="coupon-header"
>
                <text class="coupon-title">{{ coupon.title }}</text>
                <view class="coupon-tag" :class="coupon.status">
                  {{ getStatusText(coupon.status) }}
                </view>
              </view>

              <view class="coupon-shop">
                <up-icon name="shop" size="24" color="#999"></up-icon>
                <text>{{ coupon.shop }}</text>
              </view>

              <view class="coupon-desc" v-if="coupon.description"
>
                {{ coupon.description }}
              </view>

              <view class="coupon-footer"
>
                <view class="coupon-time">
                  <up-icon name="clock" size="20" color="#999"></up-icon>
                  <text :class="{ urgent: coupon.isExpiring }">{{ formatExpiry(coupon) }}</text>
                </view>

                <view class="coupon-actions">
                  <text 
                    v-if="coupon.status === 'active'" 
                    class="action-btn use"
                    @click="useCoupon(coupon)"
                  >
                    立即使用
                  </text>
                  <text 
                    class="action-btn"
                    @click="showCouponDetail(coupon)"
                  >
                    详情
                  </text>
                </view>
              </view>

              <!-- 即将过期提醒 -->
              <view v-if="coupon.isExpiring && coupon.status === 'active'" class="expiring-badge"
>
                <up-icon name="bell" size="20" color="#fff"></up-icon>
                <text>{{ coupon.daysLeft }}天后过期</text>
              </view>
            </view>
          </view>
        </view>

        <!-- 空状态 -->
        <view v-if="filteredCoupons.length === 0" class="empty-state"
>
          <up-icon name="ticket" size="100" color="#ddd"></up-icon>
          <text class="empty-text">{{ emptyText }}</text>
          <text class="empty-hint" @click="showAddModal">点击添加优惠券</text>
        </view>
      </scroll-view>
    </view>

    <!-- 添加优惠券弹窗 -->
    <up-popup
      v-model:show="addModalVisible"
      mode="bottom"
      round
      closeable
    >
      <view class="modal-content">
        <view class="modal-header"
>
          <text class="modal-title">添加优惠券</text>
        </view>

        <view class="modal-body">
          <!-- 添加方式切换 -->
          <view class="add-type-tabs"
>
            <view
              v-for="type in addTypes"
              :key="type.value"
              class="type-tab"
              :class="{ active: addType === type.value }"
              @click="addType = type.value"
            >
              <up-icon :name="type.icon" size="32" :color="addType === type.value ? '#5B8FF9' : '#999'"
</up-icon>
              {{ type.label }}
            </view>
          </view>

          <!-- 扫码添加 -->
          <view v-if="addType === 'scan'" class="scan-section">
            <view class="scan-area" @click="scanCouponCode"
>
              <up-icon name="scan" size="80" color="#5B8FF9"></up-icon>
              <text class="scan-text">点击扫描优惠券码</text>
              <text class="scan-hint">支持二维码/条形码</text>
            </view>
          </view>

          <!-- 手动添加 -->
          <view v-if="addType === 'manual'" class="form-section"
>
            <view class="form-row"
>
              <view class="form-item"
>
                <text class="form-label">券类型</text>
                <view class="type-options"
>
                  <view
                    v-for="t in couponTypes"
                    :key="t.value"
                    class="type-option"
                    :class="{ active: couponForm.type === t.value }"
                    @click="couponForm.type = t.value"
                  >
                    {{ t.label }}
                  </view>
                </view>
              </view>
            </view>

            <view class="form-row">
              <view class="form-item"
>
                <text class="form-label">{{ couponForm.type === 'discount' ? '折扣' : '金额' }}</text>
                <view class="amount-input-wrap"
>
                  <text class="currency">{{ couponForm.type === 'discount' ? '' : '¥' }}</text>
                  <input
                    v-model="couponForm.amount"
                    type="digit"
                    :placeholder="couponForm.type === 'discount' ? '如：8.5' : '优惠金额'"
                    class="amount-input"
                  />
                  <text v-if="couponForm.type === 'discount'" class="unit">折</text>
                </view>
              </view>
            </view>

            <view class="form-row"
>
              <view class="form-item full"
>
                <text class="form-label">使用门槛</text>
                <input
                  v-model="couponForm.condition"
                  placeholder="如：满100可用，无门槛等"
                  class="form-input"
                />
              </view>
            </view>

            <view class="form-row">
              <view class="form-item full"
>
                <text class="form-label">券名称</text>
                <input
                  v-model="couponForm.title"
                  placeholder="如：京东生鲜优惠券"
                  class="form-input"
                />
              </view>
            </view>

            <view class="form-row">
              <view class="form-item full"
>
                <text class="form-label">适用商家</text>
                <input
                  v-model="couponForm.shop"
                  placeholder="如：京东、美团等"
                  class="form-input"
                />
              </view>
            </view>

            <view class="form-row"
>
              <view class="form-item"
>
                <text class="form-label">有效期至</text>
                <picker mode="date" :value="couponForm.expiryDate" @change="onExpiryChange">
                  <view class="picker-value">
                    {{ couponForm.expiryDate || '选择日期' }}
                  </view>
                </picker>
              </view>
            </view>

            <view class="form-row">
              <view class="form-item full"
>
                <text class="form-label">备注（可选）</text>
                <textarea
                  v-model="couponForm.description"
                  placeholder="添加备注信息..."
                  class="form-textarea"
                />
              </view>
            </view>

            <view class="reminder-setting"
>
              <text class="reminder-label">到期提醒</text>
              <view class="reminder-options">
                <view
                  v-for="day in reminderDays"
                  :key="day"
                  class="reminder-option"
                  :class="{ active: couponForm.reminderDays.includes(day) }"
                  @click="toggleReminderDay(day)"
                >
                  {{ day }}天前
                </view>
              </view>
            </view>
          </view>
        </view>

        <view class="modal-footer"
>
          <view class="btn-cancel" @click="addModalVisible = false">取消</view>
          <view class="btn-confirm" @click="saveCoupon">添加</view>
        </view>
      </view>
    </up-popup>

    <!-- 优惠券详情弹窗 -->
    <up-popup
      v-model:show="detailModalVisible"
      mode="center"
      round
    >
      <view v-if="selectedCoupon" class="detail-modal"
>
        <view class="detail-header"
>
          <text class="detail-title">{{ selectedCoupon.title }}</text>
          <view class="detail-status" :class="selectedCoupon.status"
>
            {{ getStatusText(selectedCoupon.status) }}
          </view>
        </view>

        <view class="detail-amount"
>
          <text v-if="selectedCoupon.type === 'discount'" class="discount-text">
            {{ selectedCoupon.discount }}折
          </text>
          <view v-else class="amount-wrap"
>
            <text class="amount-symbol">¥</text>
            <text class="amount-value">{{ selectedCoupon.amount }}</text>
          </view>
          <text class="condition-text">{{ selectedCoupon.condition }}</text>
        </view>

        <view class="detail-info"
>
          <view class="info-row">
            <text class="info-label">适用商家</text>
            <text class="info-value">{{ selectedCoupon.shop }}</text>
          </view>
          
          <view class="info-row"
>
            <text class="info-label">有效期</text>
            <text class="info-value">至 {{ selectedCoupon.expiryDate }}</text>
          </view>
          
          <view class="info-row"
>
            <text class="info-label">券码</text>
            <view class="code-wrap">
              <text class="info-value code">{{ selectedCoupon.code || 'N/A' }}</text>
              <text class="copy-btn" @click="copyCode(selectedCoupon.code)">复制</text>
            </view>
          </view>
          
          <view v-if="selectedCoupon.description" class="info-row vertical"
>
            <text class="info-label">备注</text>
            <text class="info-value">{{ selectedCoupon.description }}</text>
          </view>
        </view>

        <view class="detail-actions"
>
          <view 
            v-if="selectedCoupon.status === 'active'" 
            class="btn-use"
            @click="useCoupon(selectedCoupon)"
          >
            立即使用
          </view>
          <view class="btn-delete" @click="deleteCoupon(selectedCoupon)">
            删除
          </view>
        </view>
      </view>
    </up-popup>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import dayjs from 'dayjs'

// 响应式数据
const coupons = ref([])
const currentFilter = ref('all')
const addModalVisible = ref(false)
const detailModalVisible = ref(false)
const selectedCoupon = ref(null)
const addType = ref('manual')

// 优惠券表单
const couponForm = ref({
  type: 'amount',
  amount: '',
  title: '',
  shop: '',
  condition: '',
  expiryDate: '',
  description: '',
  reminderDays: [3, 1]
})

// 选项数据
const filterTabs = [
  { label: '全部', value: 'all' },
  { label: '可用', value: 'active', badge: 0 },
  { label: '已过期', value: 'expired' },
  { label: '已使用', value: 'used' }
]

const addTypes = [
  { label: '手动添加', value: 'manual', icon: 'edit-pen' },
  { label: '扫码添加', value: 'scan', icon: 'scan' }
]

const couponTypes = [
  { label: '满减券', value: 'amount' },
  { label: '折扣券', value: 'discount' }
]

const reminderDays = [7, 3, 1]

// 模拟数据
const mockCoupons = [
  {
    id: 1,
    type: 'amount',
    amount: 50,
    title: '京东生鲜优惠券',
    shop: '京东',
    condition: '满199可用',
    expiryDate: '2026-02-25',
    description: '仅限生鲜品类使用',
    status: 'active',
    code: 'JD2026SH50'
  },
  {
    id: 2,
    type: 'discount',
    discount: 8.5,
    title: '美团外卖折扣券',
    shop: '美团',
    condition: '无门槛',
    expiryDate: '2026-02-23',
    description: '最高抵扣20元',
    status: 'active',
    code: 'MT85OFF'
  },
  {
    id: 3,
    type: 'amount',
    amount: 20,
    title: '淘宝购物券',
    shop: '淘宝',
    condition: '满100可用',
    expiryDate: '2026-02-28',
    description: '全品类通用',
    status: 'active',
    code: 'TB20COUPON'
  },
  {
    id: 4,
    type: 'amount',
    amount: 100,
    title: '盒马鲜生优惠券',
    shop: '盒马',
    condition: '满299可用',
    expiryDate: '2026-02-20',
    description: '新用户专享',
    status: 'expired',
    code: 'HM100NEW'
  },
  {
    id: 5,
    type: 'amount',
    amount: 30,
    title: '拼多多优惠券',
    shop: '拼多多',
    condition: '满50可用',
    expiryDate: '2026-02-24',
    description: '百亿补贴可用',
    status: 'used',
    code: 'PDD30OFF'
  }
]

// 页面加载
onMounted(() => {
  loadCoupons()
  checkExpiringCoupons()
})

// 加载优惠券
const loadCoupons = () => {
  // 从本地存储或接口获取
  const stored = uni.getStorageSync('coupons')
  if (stored) {
    coupons.value = stored
  } else {
    coupons.value = mockCoupons
    uni.setStorageSync('coupons', mockCoupons)
  }
  
  // 计算过期状态
  updateCouponStatus()
}

// 更新优惠券状态
const updateCouponStatus = () => {
  const today = dayjs()
  
  coupons.value.forEach(coupon => {
    if (coupon.status === 'active') {
      const expiry = dayjs(coupon.expiryDate)
      const daysLeft = expiry.diff(today, 'day')
      
      coupon.daysLeft = daysLeft
      coupon.isExpiring = daysLeft <= 3 && daysLeft >= 0
      
      if (daysLeft < 0) {
        coupon.status = 'expired'
      }
    }
  })
  
  // 更新筛选标签的badge
  filterTabs[1].badge = coupons.value.filter(c => c.status === 'active').length
}

// 检查即将过期的优惠券
const checkExpiringCoupons = () => {
  const expiring = coupons.value.filter(c => c.isExpiring && c.status === 'active')
  if (expiring.length > 0) {
    // 发送通知
    expiring.forEach(coupon => {
      // 检查是否已经提醒过
      const reminderKey = `coupon_reminder_${coupon.id}_${coupon.expiryDate}`
      const reminded = uni.getStorageSync(reminderKey)
      
      if (!reminded) {
        // 设置提醒
        uni.showModal({
          title: '优惠券即将过期',
          content: `${coupon.title} 还有${coupon.daysLeft}天过期，记得使用哦！`,
          showCancel: false
        })
        uni.setStorageSync(reminderKey, true)
      }
    })
  }
}

// 计算属性
const filteredCoupons = computed(() => {
  let list = [...coupons.value].sort((a, b) => {
    // 按状态和时间排序
    if (a.status !== b.status) {
      if (a.status === 'active') return -1
      if (b.status === 'active') return 1
    }
    return dayjs(b.expiryDate).valueOf() - dayjs(a.expiryDate).valueOf()
  })
  
  if (currentFilter.value !== 'all') {
    list = list.filter(c => c.status === currentFilter.value)
  }
  
  return list
})

const stats = computed(() => {
  const total = coupons.value.length
  const active = coupons.value.filter(c => c.status === 'active').length
  const expiring = coupons.value.filter(c => c.isExpiring && c.status === 'active').length
  const totalValue = coupons.value
    .filter(c => c.status === 'active')
    .reduce((sum, c) => sum + (parseFloat(c.amount) || 0), 0)
  
  return { total, active, expiring, totalValue }
})

const emptyText = computed(() => {
  const map = {
    all: '暂无优惠券',
    active: '暂无可用优惠券',
    expired: '暂无过期优惠券',
    used: '暂无已使用优惠券'
  }
  return map[currentFilter.value] || '暂无优惠券'
})

// 方法
const getStatusText = (status) => {
  const map = { active: '可用', expired: '已过期', used: '已使用' }
  return map[status] || status
}

const formatExpiry = (coupon) => {
  if (coupon.status === 'expired') return '已过期'
  if (coupon.status === 'used') return '已使用'
  
  const daysLeft = coupon.daysLeft
  if (daysLeft === 0) return '今天过期'
  if (daysLeft === 1) return '明天过期'
  if (daysLeft <= 3) return `${daysLeft}天后过期`
  return `有效期至 ${coupon.expiryDate}`
}

const showAddModal = () => {
  addType.value = 'manual'
  couponForm.value = {
    type: 'amount',
    amount: '',
    title: '',
    shop: '',
    condition: '',
    expiryDate: dayjs().add(30, 'day').format('YYYY-MM-DD'),
    description: '',
    reminderDays: [3, 1]
  }
  addModalVisible.value = true
}

const onExpiryChange = (e) => {
  couponForm.value.expiryDate = e.detail.value
}

const toggleReminderDay = (day) => {
  const index = couponForm.value.reminderDays.indexOf(day)
  if (index > -1) {
    couponForm.value.reminderDays.splice(index, 1)
  } else {
    couponForm.value.reminderDays.push(day)
    couponForm.value.reminderDays.sort((a, b) => b - a)
  }
}

const scanCouponCode = () => {
  uni.scanCode({
    scanType: ['qrCode', 'barCode'],
    success: (res) => {
      // 解析优惠券码
      couponForm.value.code = res.result
      // 尝试解析其他信息
      uni.showToast({ title: '扫描成功', icon: 'success' })
      addType.value = 'manual'
    },
    fail: () => {
      uni.showToast({ title: '扫描失败', icon: 'none' })
    }
  })
}

const saveCoupon = () => {
  // 验证
  if (!couponForm.value.amount) {
    uni.showToast({ title: couponForm.value.type === 'discount' ? '请输入折扣' : '请输入金额', icon: 'none' })
    return
  }
  if (!couponForm.value.title.trim()) {
    uni.showToast({ title: '请输入券名称', icon: 'none' })
    return
  }
  if (!couponForm.value.expiryDate) {
    uni.showToast({ title: '请选择有效期', icon: 'none' })
    return
  }
  
  const newCoupon = {
    id: Date.now(),
    ...couponForm.value,
    status: 'active',
    daysLeft: dayjs(couponForm.value.expiryDate).diff(dayjs(), 'day'),
    isExpiring: false
  }
  
  if (couponForm.value.type === 'discount') {
    newCoupon.discount = couponForm.value.amount
    delete newCoupon.amount
  }
  
  coupons.value.unshift(newCoupon)
  uni.setStorageSync('coupons', coupons.value)
  
  addModalVisible.value = false
  uni.showToast({ title: '添加成功', icon: 'success' })
  
  // 更新状态
  updateCouponStatus()
}

const showCouponDetail = (coupon) => {
  selectedCoupon.value = coupon
  detailModalVisible.value = true
}

const copyCode = (code) => {
  if (!code) {
    uni.showToast({ title: '无券码', icon: 'none' })
    return
  }
  uni.setClipboardData({
    data: code,
    success: () => {
      uni.showToast({ title: '已复制', icon: 'success' })
    }
  })
}

const useCoupon = (coupon) => {
  uni.showModal({
    title: '确认使用',
    content: `使用 ${coupon.title}？`,
    success: (res) => {
      if (res.confirm) {
        coupon.status = 'used'
        uni.setStorageSync('coupons', coupons.value)
        uni.showToast({ title: '标记为已使用', icon: 'success' })
        detailModalVisible.value = false
        updateCouponStatus()
      }
    }
  })
}

const deleteCoupon = (coupon) => {
  uni.showModal({
    title: '确认删除',
    content: '确定要删除这张优惠券吗？',
    success: (res) => {
      if (res.confirm) {
        const index = coupons.value.findIndex(c => c.id === coupon.id)
        if (index > -1) {
          coupons.value.splice(index, 1)
          uni.setStorageSync('coupons', coupons.value)
          uni.showToast({ title: '已删除', icon: 'success' })
          detailModalVisible.value = false
          updateCouponStatus()
        }
      }
    }
  })
}

const onRefresh = () => {
  setTimeout(() => {
    updateCouponStatus()
    uni.stopPullDownRefresh()
  }, 1000)
}

const goBack = () => {
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
.coupon-page {
  min-height: 100vh;
  background: #f5f6fa;
}

.nav-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 30rpx;
  padding-top: 60rpx;
  background: linear-gradient(135deg, #FF6B6B 0%, #EE5A6F 100%);

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
    width: 60rpx;
    height: 60rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    background: rgba(255, 255, 255, 0.2);
    border-radius: 50%;
  }
}

.content {
  padding: 0 30rpx 30rpx;
}

// 统计卡片
.stats-cards {
  display: flex;
  gap: 16rpx;
  margin-top: -40rpx;
  position: relative;
  z-index: 1;

  .stat-card {
    flex: 1;
    background: #fff;
    border-radius: 16rpx;
    padding: 24rpx;
    text-align: center;
    box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);

    .stat-value {
      font-size: 36rpx;
      font-weight: 700;
      color: #333;
      display: block;
      margin-bottom: 8rpx;
    }

    .stat-label {
      font-size: 24rpx;
      color: #999;
    }

    &.active {
      .stat-value {
        color: #52C41A;
      }
    }

    &.warning {
      .stat-value {
        color: #FF4D4F;
      }
    }
  }
}

// 筛选标签
.filter-tabs {
  display: flex;
  gap: 20rpx;
  margin: 30rpx 0 20rpx;
  padding: 0 10rpx;

  .filter-tab {
    position: relative;
    padding: 12rpx 24rpx;
    font-size: 28rpx;
    color: #666;
    border-radius: 30rpx;
    transition: all 0.3s;

    &.active {
      background: #FF6B6B;
      color: #fff;
    }

    .tab-badge {
      position: absolute;
      top: -8rpx;
      right: -8rpx;
      min-width: 32rpx;
      height: 32rpx;
      padding: 0 8rpx;
      background: #FF4D4F;
      border-radius: 16rpx;
      font-size: 20rpx;
      color: #fff;
      display: flex;
      align-items: center;
      justify-content: center;
    }
  }
}

// 优惠券列表
.coupon-list {
  max-height: calc(100vh - 400rpx);
}

.coupon-card {
  margin-bottom: 20rpx;

  &.expired, &.used {
    opacity: 0.6;
    filter: grayscale(0.5);
  }

  &.expiring {
    .coupon-face {
      box-shadow: 0 4rpx 16rpx rgba(255, 77, 79, 0.2);
    }
  }
}

.coupon-face {
  display: flex;
  background: #fff;
  border-radius: 20rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.06);
}

.coupon-left {
  position: relative;
  width: 200rpx;
  background: linear-gradient(135deg, #FF6B6B 0%, #EE5A6F 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 30rpx;
  color: #fff;

  .coupon-amount {
    text-align: center;

    .amount-wrap {
      display: flex;
      align-items: flex-start;
    }

    .amount-symbol {
      font-size: 32rpx;
      font-weight: 600;
    }

    .amount-value {
      font-size: 64rpx;
      font-weight: 700;
      line-height: 1;
    }
  }

  .coupon-condition {
    font-size: 22rpx;
    opacity: 0.9;
    margin-top: 12rpx;
    text-align: center;
  }

  .sawtooth {
    position: absolute;
    right: -8rpx;
    top: 0;
    bottom: 0;
    width: 16rpx;
    background: linear-gradient(to bottom, #fff 50%, transparent 50%);
    background-size: 16rpx 16rpx;
  }
}

.coupon-right {
  flex: 1;
  padding: 24rpx;
  position: relative;

  .coupon-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 12rpx;

    .coupon-title {
      font-size: 30rpx;
      font-weight: 600;
      color: #333;
      flex: 1;
      margin-right: 16rpx;
    }

    .coupon-tag {
      padding: 4rpx 12rpx;
      border-radius: 8rpx;
      font-size: 20rpx;

      &.active {
        background: #F6FFED;
        color: #52C41A;
      }

      &.expired {
        background: #f5f5f5;
        color: #999;
      }

      &.used {
        background: #f5f5f5;
        color: #999;
      }
    }
  }

  .coupon-shop {
    display: flex;
    align-items: center;
    gap: 8rpx;
    font-size: 24rpx;
    color: #666;
    margin-bottom: 8rpx;
  }

  .coupon-desc {
    font-size: 22rpx;
    color: #999;
    margin-bottom: 16rpx;
  }

  .coupon-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .coupon-time {
      display: flex;
      align-items: center;
      gap: 6rpx;
      font-size: 22rpx;
      color: #999;

      .urgent {
        color: #FF4D4F;
      }
    }

    .coupon-actions {
      display: flex;
      gap: 16rpx;

      .action-btn {
        font-size: 24rpx;
        color: #999;

        &.use {
          color: #FF6B6B;
          font-weight: 500;
        }
      }
    }
  }

  .expiring-badge {
    position: absolute;
    top: 0;
    right: 0;
    display: flex;
    align-items: center;
    gap: 6rpx;
    padding: 6rpx 12rpx;
    background: #FF4D4F;
    border-radius: 0 20rpx 0 12rpx;

    text {
      font-size: 20rpx;
      color: #fff;
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
    font-size: 24rpx;
    color: #FF6B6B;
    margin-top: 12rpx;
  }
}

// 弹窗样式
.modal-content {
  padding: 30rpx 0;

  .modal-header {
    text-align: center;
    padding: 0 30rpx 30rpx;
    border-bottom: 1rpx solid #f5f5f5;

    .modal-title {
      font-size: 32rpx;
      font-weight: 600;
      color: #333;
    }
  }

  .modal-body {
    padding: 30rpx;
  }

  .modal-footer {
    display: flex;
    gap: 20rpx;
    padding: 0 30rpx;

    .btn-cancel, .btn-confirm {
      flex: 1;
      padding: 24rpx 0;
      border-radius: 12rpx;
      text-align: center;
      font-size: 30rpx;
    }

    .btn-cancel {
      background: #f5f5f5;
      color: #666;
    }

    .btn-confirm {
      background: #FF6B6B;
      color: #fff;
    }
  }
}

// 添加方式切换
.add-type-tabs {
  display: flex;
  gap: 20rpx;
  margin-bottom: 30rpx;

  .type-tab {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 12rpx;
    padding: 30rpx;
    background: #f5f6fa;
    border-radius: 16rpx;
    font-size: 28rpx;
    color: #666;
    border: 2rpx solid transparent;

    &.active {
      border-color: #FF6B6B;
      background: #FFF1F0;
      color: #FF6B6B;
    }
  }
}

// 扫码区域
.scan-section {
  .scan-area {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 80rpx;
    background: #f5f6fa;
    border-radius: 16rpx;
    border: 2rpx dashed #ddd;

    .scan-text {
      font-size: 30rpx;
      color: #333;
      margin-top: 20rpx;
    }

    .scan-hint {
      font-size: 24rpx;
      color: #999;
      margin-top: 12rpx;
    }
  }
}

// 表单样式
.form-section {
  .form-row {
    margin-bottom: 24rpx;

    .form-item {
      flex: 1;

      &.full {
        width: 100%;
      }
    }
  }

  .form-label {
    font-size: 26rpx;
    color: #666;
    margin-bottom: 12rpx;
    display: block;
  }

  .type-options {
    display: flex;
    gap: 16rpx;

    .type-option {
      flex: 1;
      padding: 20rpx;
      background: #f5f6fa;
      border-radius: 12rpx;
      text-align: center;
      font-size: 28rpx;
      color: #666;
      border: 2rpx solid transparent;

      &.active {
        border-color: #FF6B6B;
        background: #FFF1F0;
        color: #FF6B6B;
      }
    }
  }

  .amount-input-wrap {
    display: flex;
    align-items: center;
    background: #f5f6fa;
    border-radius: 12rpx;
    padding: 0 24rpx;

    .currency, .unit {
      font-size: 32rpx;
      color: #333;
      font-weight: 600;
    }

    .amount-input {
      flex: 1;
      height: 80rpx;
      text-align: center;
      font-size: 36rpx;
      color: #333;
      background: transparent;
    }
  }

  .form-input, .form-textarea, .picker-value {
    width: 100%;
    background: #f5f6fa;
    border-radius: 12rpx;
    padding: 24rpx;
    font-size: 28rpx;
    color: #333;
  }

  .form-textarea {
    height: 140rpx;
  }
}

// 提醒设置
.reminder-setting {
  margin-top: 30rpx;
  padding-top: 30rpx;
  border-top: 1rpx solid #f5f5f5;

  .reminder-label {
    font-size: 26rpx;
    color: #666;
    margin-bottom: 16rpx;
    display: block;
  }

  .reminder-options {
    display: flex;
    gap: 16rpx;

    .reminder-option {
      flex: 1;
      padding: 16rpx;
      background: #f5f6fa;
      border-radius: 12rpx;
      text-align: center;
      font-size: 26rpx;
      color: #666;
      border: 2rpx solid transparent;

      &.active {
        border-color: #FF6B6B;
        background: #FFF1F0;
        color: #FF6B6B;
      }
    }
  }
}

// 详情弹窗
.detail-modal {
  width: 600rpx;
  padding: 40rpx;

  .detail-header {
    text-align: center;
    margin-bottom: 30rpx;

    .detail-title {
      font-size: 32rpx;
      font-weight: 600;
      color: #333;
      display: block;
      margin-bottom: 16rpx;
    }

    .detail-status {
      display: inline-block;
      padding: 6rpx 20rpx;
      border-radius: 8rpx;
      font-size: 24rpx;

      &.active {
        background: #F6FFED;
        color: #52C41A;
      }

      &.expired, &.used {
        background: #f5f5f5;
        color: #999;
      }
    }
  }

  .detail-amount {
    text-align: center;
    padding: 40rpx;
    background: linear-gradient(135deg, #FF6B6B 0%, #EE5A6F 100%);
    border-radius: 20rpx;
    margin-bottom: 30rpx;
    color: #fff;

    .discount-text, .amount-value {
      font-size: 72rpx;
      font-weight: 700;
      display: block;
    }

    .amount-symbol {
      font-size: 40rpx;
      font-weight: 600;
    }

    .condition-text {
      font-size: 28rpx;
      opacity: 0.9;
      margin-top: 12rpx;
      display: block;
    }
  }

  .detail-info {
    .info-row {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 20rpx 0;
      border-bottom: 1rpx solid #f5f5f5;

      &.vertical {
        flex-direction: column;
        align-items: flex-start;

        .info-value {
          margin-top: 12rpx;
        }
      }

      .info-label {
        font-size: 26rpx;
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

      .code-wrap {
        display: flex;
        align-items: center;
        gap: 16rpx;

        .copy-btn {
          font-size: 24rpx;
          color: #FF6B6B;
        }
      }
    }
  }

  .detail-actions {
    margin-top: 30rpx;

    .btn-use, .btn-delete {
      padding: 24rpx 0;
      border-radius: 12rpx;
      text-align: center;
      font-size: 30rpx;
    }

    .btn-use {
      background: #FF6B6B;
      color: #fff;
      margin-bottom: 16rpx;
    }

    .btn-delete {
      background: #f5f5f5;
      color: #999;
    }
  }
}
</style>