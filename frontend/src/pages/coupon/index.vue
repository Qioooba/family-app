<template>
  <view class="coupon-page">
    <!-- 顶部背景 -->
    <view class="header-bg"></view>
    
    <!-- 头部 -->
    <view class="header-bar">
      <text class="title">优惠券</text>
      <text class="subtitle">省钱神器</text>
    </view>
    
    <!-- 统计卡片 -->
    <view class="stats-cards">
      <view class="stat-card">
        <text class="stat-num">{{ availableCount }}</text>
        <text class="stat-label">可用</text>
      </view>
      <view class="stat-card">
        <text class="stat-num">{{ usedCount }}</text>
        <text class="stat-label">已用</text>
      </view>
      <view class="stat-card">
        <text class="stat-num">{{ expiredCount }}</text>
        <text class="stat-label">已过期</text>
      </view>
    </view>
    
    <!-- 标签切换 -->
    <view class="tabs">
      <view 
        class="tab" 
        :class="{ active: currentTab === 0 }"
        @click="currentTab = 0"
      >
        <text>可用</text>
      </view>
      <view 
        class="tab" 
        :class="{ active: currentTab === 1 }"
        @click="currentTab = 1"
      >
        <text>已使用</text>
      </view>
      <view 
        class="tab" 
        :class="{ active: currentTab === 2 }"
        @click="currentTab = 2"
      >
        <text>已过期</text>
      </view>
    </view>
    
    <!-- 优惠券列表 -->
    <view class="coupon-list">
      <view v-if="filteredCoupons.length === 0" class="empty-state">
        <text class="empty-icon">🎫</text>
        <text class="empty-text">暂无优惠券</text>
      </view>
      
      <view 
        v-for="coupon in filteredCoupons" 
        :key="coupon.id"
        class="coupon-card"
        :class="{ disabled: coupon.status !== 0 }"
      >
        <!-- 左侧金额 -->
        <view class="coupon-left">
          <text class="currency">¥</text>
          <text class="amount">{{ coupon.amount }}</text>
        </view>
        
        <!-- 右侧信息 -->
        <view class="coupon-right">
          <text class="coupon-name">{{ coupon.name }}</text>
          <text class="coupon-desc">{{ coupon.desc }}</text>
          <view class="coupon-meta">
            <text class="expire-date">有效期至 {{ coupon.expireDate }}</text>
          </view>
        </view>
        
        <!-- 使用按钮 -->
        <view class="coupon-action">
          <text v-if="coupon.status === 0" class="use-btn" @click="useCoupon(coupon)">立即使用</text>
          <text v-else-if="coupon.status === 1" class="used-text">已使用</text>
          <text v-else class="expired-text">已过期</text>
        </view>
      </view>
    </view>
    
    <!-- 添加按钮 -->
    <view class="add-btn" @click="showAddModal = true">
      <text>+</text>
    </view>
    
    <!-- 添加优惠券弹窗 -->
    <view v-if="showAddModal" class="modal-overlay" @click="showAddModal = false">
      <view class="modal-content" @click.stop>
        <view class="modal-header">
          <text class="modal-title">添加优惠券</text>
          <text class="modal-close" @click="showAddModal = false">✕</text>
        </view>
        
        <view class="modal-body">
          <view class="form-item">
            <text class="form-label">优惠券名称 *</text>
            <input class="form-input" v-model="newCoupon.name" placeholder="如：满100减20" />
          </view>
          
          <view class="form-item">
            <text class="form-label">优惠金额 *</text>
            <input class="form-input" type="number" v-model="newCoupon.amount" placeholder="20" />
          </view>
          
          <view class="form-item">
            <text class="form-label">使用条件</text>
            <input class="form-input" v-model="newCoupon.condition" placeholder="如：满100元可用" />
          </view>
          
          <view class="form-item">
            <text class="form-label">有效期至</text>
            <picker mode="date" :value="newCoupon.expireDate" @change="onExpireDateChange">
              <view class="date-picker">
                <text :class="{ placeholder: !newCoupon.expireDate }">
                  {{ newCoupon.expireDate || '选择日期' }}
                </text>
              </view>
            </picker>
          </view>
          
          <view class="form-item">
            <text class="form-label">备注</text>
            <input class="form-input" v-model="newCoupon.desc" placeholder="备注信息" />
          </view>
        </view>
        
        <view class="modal-footer">
          <view class="cancel-btn" @click="showAddModal = false">
            <text>取消</text>
          </view>
          <view class="submit-btn" @click="addCoupon">
            <text>添加</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'

const currentTab = ref(0)
const showAddModal = ref(false)

const newCoupon = ref({
  name: '',
  amount: '',
  condition: '',
  expireDate: '',
  desc: ''
})

const coupons = ref([
  { id: 1, name: '满100减20', amount: 20, desc: '全品类可用', condition: '满100元', expireDate: '2026-03-31', status: 0 },
  { id: 2, name: '新人专享', amount: 50, desc: '新用户首单', condition: '无门槛', expireDate: '2026-04-15', status: 0 },
  { id: 3, name: '水果专场', amount: 10, desc: '水果生鲜', condition: '满50元', expireDate: '2026-03-20', status: 0 },
  { id: 4, name: '早餐满减', amount: 5, desc: '早餐专用', condition: '满20元', expireDate: '2026-03-10', status: 1 },
  { id: 5, name: '春节特惠', amount: 30, desc: '春节活动', condition: '满200元', expireDate: '2026-02-01', status: 2 }
])

const availableCount = computed(() => coupons.value.filter(c => c.status === 0).length)
const usedCount = computed(() => coupons.value.filter(c => c.status === 1).length)
const expiredCount = computed(() => coupons.value.filter(c => c.status === 2).length)

const filteredCoupons = computed(() => {
  return coupons.value.filter(c => c.status === currentTab.value)
})

const onExpireDateChange = (e) => {
  newCoupon.value.expireDate = e.detail.value
}

const addCoupon = () => {
  if (!newCoupon.value.name.trim() || !newCoupon.value.amount) {
    uni.showToast({ title: '请填写完整信息', icon: 'none' })
    return
  }
  
  coupons.value.unshift({
    id: Date.now(),
    name: newCoupon.value.name,
    amount: parseInt(newCoupon.value.amount),
    desc: newCoupon.value.desc || '无',
    condition: newCoupon.value.condition || '无门槛',
    expireDate: newCoupon.value.expireDate || '2026-12-31',
    status: 0
  })
  
  showAddModal.value = false
  newCoupon.value = { name: '', amount: '', condition: '', expireDate: '', desc: '' }
  uni.showToast({ title: '添加成功', icon: 'success' })
}

const useCoupon = (coupon) => {
  uni.showModal({
    title: '使用优惠券',
    content: `确定使用 ${coupon.name}？`,
    success: (res) => {
      if (res.confirm) {
        coupon.status = 1
        uni.showToast({ title: '使用成功', icon: 'success' })
      }
    }
  })
}
</script>

<style lang="scss" scoped>
.coupon-page {
  min-height: 100vh;
  background: #f8f9fc;
  padding-bottom: 120rpx;
}

.header-bg {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 300rpx;
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.header-bar {
  position: relative;
  padding: 100rpx 32rpx 32rpx;
  
  .title {
    font-size: 48rpx;
    font-weight: 700;
    color: #fff;
    display: block;
  }
  
  .subtitle {
    font-size: 26rpx;
    color: rgba(255,255,255,0.8);
  }
}

.stats-cards {
  position: relative;
  display: flex;
  justify-content: space-around;
  margin: 0 32rpx;
  padding: 32rpx;
  background: #fff;
  border-radius: 24rpx;
  box-shadow: 0 8rpx 24rpx rgba(0,0,0,0.05);
  
  .stat-card {
    text-align: center;
    
    .stat-num {
      font-size: 40rpx;
      font-weight: 700;
      color: #f5576c;
      display: block;
    }
    
    .stat-label {
      font-size: 24rpx;
      color: #8b9aad;
    }
  }
}

.tabs {
  display: flex;
  margin: 32rpx;
  background: #fff;
  border-radius: 24rpx;
  padding: 8rpx;
  
  .tab {
    flex: 1;
    text-align: center;
    padding: 20rpx;
    border-radius: 20rpx;
    font-size: 28rpx;
    color: #8b9aad;
    
    &.active {
      background: linear-gradient(135deg, #f093fb, #f5576c);
      color: #fff;
      font-weight: 500;
    }
  }
}

.coupon-list {
  padding: 0 32rpx;
  
  .empty-state {
    text-align: center;
    padding: 100rpx 0;
    
    .empty-icon {
      font-size: 100rpx;
      display: block;
      margin-bottom: 20rpx;
    }
    
    .empty-text {
      font-size: 30rpx;
      color: #8b9aad;
    }
  }
}

.coupon-card {
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 20rpx;
  margin-bottom: 20rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.05);
  
  &.disabled {
    opacity: 0.6;
    
    .coupon-left {
      background: #e2e8f0;
    }
  }
  
  .coupon-left {
    width: 180rpx;
    padding: 32rpx 20rpx;
    background: linear-gradient(135deg, #f093fb, #f5576c);
    text-align: center;
    
    .currency {
      font-size: 28rpx;
      color: #fff;
    }
    
    .amount {
      font-size: 56rpx;
      font-weight: 700;
      color: #fff;
    }
  }
  
  .coupon-right {
    flex: 1;
    padding: 20rpx 24rpx;
    
    .coupon-name {
      font-size: 30rpx;
      font-weight: 600;
      color: #2d3748;
      display: block;
      margin-bottom: 8rpx;
    }
    
    .coupon-desc {
      font-size: 24rpx;
      color: #8b9aad;
      display: block;
      margin-bottom: 8rpx;
    }
    
    .coupon-meta {
      .expire-date {
        font-size: 22rpx;
        color: #f5576c;
      }
    }
  }
  
  .coupon-action {
    padding: 0 24rpx;
    
    .use-btn {
      padding: 16rpx 32rpx;
      background: linear-gradient(135deg, #f093fb, #f5576c);
      border-radius: 30rpx;
      font-size: 26rpx;
      color: #fff;
    }
    
    .used-text, .expired-text {
      font-size: 26rpx;
      color: #8b9aad;
    }
  }
}

.add-btn {
  position: fixed;
  bottom: 60rpx;
  right: 40rpx;
  width: 120rpx;
  height: 120rpx;
  background: linear-gradient(135deg, #f093fb, #f5576c);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 12rpx 40rpx rgba(245, 87, 108, 0.4);
  
  text {
    font-size: 60rpx;
    color: #fff;
    font-weight: 300;
  }
}

// 弹窗样式
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
  background: #fff;
  border-radius: 40rpx 40rpx 0 0;
  width: 100%;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 40rpx;
  
  .modal-title {
    font-size: 36rpx;
    font-weight: 600;
  }
  
  .modal-close {
    font-size: 40rpx;
    color: #8b9aad;
  }
}

.modal-body {
  padding: 0 40rpx 40rpx;
}

.form-item {
  margin-bottom: 32rpx;
  
  .form-label {
    font-size: 26rpx;
    color: #5a6c7d;
    display: block;
    margin-bottom: 12rpx;
  }
  
  .form-input {
    width: 100%;
    padding: 24rpx;
    background: #f8f9fc;
    border-radius: 20rpx;
    font-size: 30rpx;
  }
}

.date-picker {
  padding: 24rpx;
  background: #f8f9fc;
  border-radius: 20rpx;
  
  text {
    font-size: 30rpx;
    
    &.placeholder { color: #a0aec0; }
  }
}

.modal-footer {
  display: flex;
  gap: 24rpx;
  padding: 20rpx 40rpx 60rpx;
  
  .cancel-btn, .submit-btn {
    flex: 1;
    text-align: center;
    padding: 28rpx;
    border-radius: 32rpx;
    font-size: 30rpx;
  }
  
  .cancel-btn {
    background: #f1f5f9;
    color: #5a6c7d;
  }
  
  .submit-btn {
    background: linear-gradient(135deg, #f093fb, #f5576c);
    color: #fff;
  }
}
</style>
