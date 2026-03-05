<template>
  <view class="points-page">
    <!-- 头部 -->
    <view class="header">
      <view class="back-btn" @click="goBack">
        <text class="icon">←</text>
      </view>
      <text class="title">积分兑换</text>
      <view class="placeholder"></view>
    </view>

    <!-- 积分卡片 -->
    <view class="points-card">
      <view class="card-bg"></view>
      <view class="points-info">
        <text class="points-label">我的积分</text>
        <view class="points-value-wrapper">
          <text class="points-num" :class="{ 'animate': isAnimating }">{{ displayPoints }}</text>
          <text class="points-unit">分</text>
        </view>
        <text class="points-desc">积分可用于兑换家庭特权和礼品</text>
      </view>
      <view class="points-icon">💎</view>
    </view>

    <!-- 兑换分类 -->
    <view class="category-tabs">
      <view 
        v-for="cat in categories" 
        :key="cat.value"
        class="tab-item"
        :class="{ active: currentCategory === cat.value }"
        @click="switchCategory(cat.value)"
      >
        <text class="tab-icon">{{ cat.icon }}</text>
        <text class="tab-name">{{ cat.label }}</text>
      </view>
    </view>

    <!-- 兑换商品列表 -->
    <scroll-view class="goods-list" scroll-y @scrolltolower="loadMore">
      <view 
        v-for="item in filteredGoods" 
        :key="item.id"
        class="goods-card"
        :class="{ 'disabled': item.stock === 0 || userPoints < item.points }"
      >
        <view class="goods-image">
          <image :src="item.image || '../../static/gift/default.png'" mode="aspectFill" />
          <view v-if="item.stock === 0" class="sold-out">已兑完</view>
        </view>
        
        <view class="goods-info">
          <text class="goods-name">{{ item.name }}</text>
          <text class="goods-desc">{{ item.description }}</text>
          
          <view class="goods-footer">
            <view class="points-cost">
              <text class="cost-num">{{ item.points }}</text>
              <text class="cost-unit">积分</text>
            </view>
            <view 
              class="exchange-btn"
              :class="{ 'disabled': item.stock === 0 || userPoints < item.points }"
              @click="exchange(item)"
            >
              {{ item.stock === 0 ? '已兑完' : userPoints < item.points ? '积分不足' : '立即兑换' }}
            </view>
          </view>
        </view>
      </view>

      <view v-if="filteredGoods.length === 0" class="empty-state">
        <text class="empty-icon">🎁</text>
        <text class="empty-text">该分类暂无商品</text>
      </view>
    </scroll-view>

    <!-- 兑换确认弹窗 -->
    <view v-if="showExchangeModal" class="modal-overlay">
      <view class="modal-mask" @click="closeExchangeModal"></view>
      <view class="modal-content">
        <view class="modal-header">
          <text class="modal-title">确认兑换</text>
          <view class="close-btn" @click="closeExchangeModal">×</view>
        </view>

        <view v-if="selectedItem" class="exchange-confirm">
          <image :src="selectedItem.image || '../../static/gift/default.png'" class="confirm-image" mode="aspectFill" />
          <text class="confirm-name">{{ selectedItem.name }}</text>
          <view class="confirm-points">
            <text>消耗积分：</text>
            <text class="deduct-num">-{{ selectedItem.points }}</text>
          </view>
          <view class="confirm-balance">
            <text>兑换后余额：</text>
            <text class="balance-num">{{ userPoints - selectedItem.points }}</text>
          </view>
        </view>

        <view class="modal-actions">
          <view class="btn btn-cancel" @click="closeExchangeModal">取消</view>
          <view class="btn btn-confirm" @click="confirmExchange">确认兑换</view>
        </view>
      </view>
    </view>

    <!-- 兑换成功动画 -->
    <view v-if="showSuccessAnimation" class="success-overlay">
      <view class="success-content">
        <view class="success-circle">
          <text class="success-icon">✓</text>
        </view>
        <text class="success-text">兑换成功!</text>
        <text class="success-desc">-{{ deductedPoints }} 积分</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { gameApi } from '../../api/index.js'

const userPoints = ref(1250)
const displayPoints = ref(1250)
const isAnimating = ref(false)
const currentCategory = ref('all')
const goodsList = ref([])
const showExchangeModal = ref(false)
const selectedItem = ref(null)
const showSuccessAnimation = ref(false)
const deductedPoints = ref(0)

const categories = [
  { label: '全部', value: 'all', icon: '📦' },
  { label: '特权', value: 'privilege', icon: '👑' },
  { label: '礼品', value: 'gift', icon: '🎁' },
  { label: '券包', value: 'coupon', icon: '🎫' }
]

// 模拟兑换商品数据
const mockGoods = [
  { id: 1, name: '免家务券', description: '免除一次家务任务', points: 500, stock: 10, category: 'privilege', image: '' },
  { id: 2, name: '决定权', description: '一次家庭决策权', points: 800, stock: 5, category: 'privilege', image: '' },
  { id: 3, name: '零食礼包', description: '精选零食大礼包', points: 300, stock: 20, category: 'gift', image: '' },
  { id: 4, name: '电影票', description: '双人电影票兑换券', points: 600, stock: 8, category: 'gift', image: '' },
  { id: 5, name: '满减券', description: '购物满100减20', points: 200, stock: 50, category: 'coupon', image: '' },
  { id: 6, name: '外卖红包', description: '外卖平台10元红包', points: 150, stock: 30, category: 'coupon', image: '' },
  { id: 7, name: '游乐园门票', description: '亲子游乐园门票', points: 1000, stock: 3, category: 'gift', image: '' },
  { id: 8, name: '周末睡懒觉', description: '周末可睡懒觉特权', points: 400, stock: 15, category: 'privilege', image: '' }
]

const filteredGoods = computed(() => {
  if (currentCategory.value === 'all') return goodsList.value
  return goodsList.value.filter(item => item.category === currentCategory.value)
})

onMounted(() => {
  loadUserPoints()
  loadGoodsList()
})

const loadUserPoints = async () => {
  try {
    const res = await gameApi.getUserPoints()
    if (res) {
      userPoints.value = res.points || 0
      displayPoints.value = userPoints.value
    }
  } catch (e) {
    console.error('加载积分失败', e)
    // 使用默认值
    userPoints.value = 1250
    displayPoints.value = 1250
  }
}

const loadGoodsList = async () => {
  // 模拟加载商品列表
  goodsList.value = mockGoods
}

const switchCategory = (value) => {
  currentCategory.value = value
}

const exchange = (item) => {
  if (item.stock === 0 || userPoints.value < item.points) return
  selectedItem.value = item
  showExchangeModal.value = true
}

const closeExchangeModal = () => {
  showExchangeModal.value = false
  selectedItem.value = null
}

const confirmExchange = async () => {
  if (!selectedItem.value) return

  try {
    // 调用兑换API
    // await gameApi.exchangeGoods(selectedItem.value.id)
    
    // 积分扣减动画
    deductedPoints.value = selectedItem.value.points
    animatePointsDeduction(userPoints.value, userPoints.value - selectedItem.value.points)
    
    closeExchangeModal()
    showSuccessAnimation.value = true
    
    // 更新积分
    userPoints.value -= selectedItem.value.points
    
    // 减少库存
    const item = goodsList.value.find(g => g.id === selectedItem.value.id)
    if (item) item.stock--

    setTimeout(() => {
      showSuccessAnimation.value = false
    }, 2000)
  } catch (e) {
    uni.showToast({ title: '兑换失败', icon: 'none' })
  }
}

// 积分扣减动画
const animatePointsDeduction = (from, to) => {
  isAnimating.value = true
  const duration = 1000
  const startTime = Date.now()
  
  const animate = () => {
    const elapsed = Date.now() - startTime
    const progress = Math.min(elapsed / duration, 1)
    
    // 缓动函数
    const easeOutQuart = 1 - Math.pow(1 - progress, 4)
    displayPoints.value = Math.floor(from + (to - from) * easeOutQuart)
    
    if (progress < 1) {
      requestAnimationFrame(animate)
    } else {
      isAnimating.value = false
    }
  }
  
  animate()
}

const loadMore = () => {
  // 加载更多
}

const goBack = () => {
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
.points-page {
  min-height: 100vh;
  background: #0f0f23;
  padding-bottom: 30rpx;
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

  .placeholder {
    width: 60rpx;
  }
}

.points-card {
  position: relative;
  margin: 0 30rpx 30rpx;
  padding: 40rpx;
  border-radius: 24rpx;
  overflow: hidden;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);

  .card-bg {
    position: absolute;
    top: -50%;
    right: -20%;
    width: 300rpx;
    height: 300rpx;
    background: radial-gradient(circle, rgba(255,255,255,0.1) 0%, transparent 70%);
    border-radius: 50%;
  }

  .points-info {
    position: relative;
    z-index: 1;

    .points-label {
      font-size: 28rpx;
      color: rgba(255,255,255,0.8);
    }

    .points-value-wrapper {
      display: flex;
      align-items: baseline;
      margin: 20rpx 0;

      .points-num {
        font-size: 80rpx;
        font-weight: 700;
        color: #fff;
        transition: all 0.3s;

        &.animate {
          transform: scale(1.1);
          color: #fbbf24;
        }
      }

      .points-unit {
        font-size: 32rpx;
        color: rgba(255,255,255,0.8);
        margin-left: 10rpx;
      }
    }

    .points-desc {
      font-size: 24rpx;
      color: rgba(255,255,255,0.6);
    }
  }

  .points-icon {
    position: absolute;
    right: 40rpx;
    top: 50%;
    transform: translateY(-50%);
    font-size: 100rpx;
    opacity: 0.3;
  }
}

.category-tabs {
  display: flex;
  gap: 20rpx;
  padding: 0 30rpx;
  margin-bottom: 30rpx;

  .tab-item {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 20rpx 0;
    background: rgba(255,255,255,0.05);
    border-radius: 16rpx;
    border: 2rpx solid transparent;
    transition: all 0.3s;

    .tab-icon {
      font-size: 40rpx;
      margin-bottom: 8rpx;
    }

    .tab-name {
      font-size: 24rpx;
      color: #94a3b8;
    }

    &.active {
      background: rgba(99,102,241,0.2);
      border-color: rgba(99,102,241,0.5);

      .tab-name {
        color: #fff;
        font-weight: 600;
      }
    }
  }
}

.goods-list {
  height: calc(100vh - 600rpx);
  padding: 0 30rpx;

  .goods-card {
    display: flex;
    background: rgba(255,255,255,0.05);
    border-radius: 20rpx;
    padding: 24rpx;
    margin-bottom: 20rpx;
    border: 1rpx solid rgba(255,255,255,0.08);

    &.disabled {
      opacity: 0.5;
    }

    .goods-image {
      position: relative;
      width: 160rpx;
      height: 160rpx;
      border-radius: 16rpx;
      overflow: hidden;
      background: rgba(255,255,255,0.1);
      margin-right: 24rpx;

      image {
        width: 100%;
        height: 100%;
      }

      .sold-out {
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background: rgba(0,0,0,0.6);
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 28rpx;
        color: #fff;
        font-weight: 600;
      }
    }

    .goods-info {
      flex: 1;
      display: flex;
      flex-direction: column;

      .goods-name {
        font-size: 30rpx;
        font-weight: 600;
        color: #fff;
        margin-bottom: 8rpx;
      }

      .goods-desc {
        font-size: 24rpx;
        color: #64748b;
        margin-bottom: 16rpx;
      }

      .goods-footer {
        margin-top: auto;
        display: flex;
        justify-content: space-between;
        align-items: center;

        .points-cost {
          .cost-num {
            font-size: 36rpx;
            font-weight: 700;
            color: #fbbf24;
          }

          .cost-unit {
            font-size: 24rpx;
            color: #64748b;
            margin-left: 6rpx;
          }
        }

        .exchange-btn {
          padding: 16rpx 32rpx;
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          border-radius: 30rpx;
          font-size: 26rpx;
          color: #fff;
          font-weight: 500;

          &.disabled {
            background: rgba(255,255,255,0.1);
            color: #64748b;
          }
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

    .empty-text {
      font-size: 28rpx;
      color: #64748b;
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

      .modal-title {
        font-size: 36rpx;
        font-weight: 600;
        color: #fff;
      }

      .close-btn {
        font-size: 48rpx;
        color: #64748b;
      }
    }
  }
}

.exchange-confirm {
  text-align: center;
  padding: 40rpx 0;

  .confirm-image {
    width: 200rpx;
    height: 200rpx;
    border-radius: 20rpx;
    margin-bottom: 30rpx;
  }

  .confirm-name {
    display: block;
    font-size: 34rpx;
    font-weight: 600;
    color: #fff;
    margin-bottom: 40rpx;
  }

  .confirm-points, .confirm-balance {
    display: flex;
    justify-content: center;
    align-items: center;
    gap: 16rpx;
    font-size: 28rpx;
    color: #94a3b8;
    margin-bottom: 16rpx;

    .deduct-num {
      color: #ef4444;
      font-weight: 600;
    }

    .balance-num {
      color: #10b981;
      font-weight: 600;
    }
  }
}

.modal-actions {
  display: flex;
  gap: 20rpx;
  margin-top: 40rpx;

  .btn {
    flex: 1;
    padding: 28rpx 0;
    border-radius: 40rpx;
    text-align: center;
    font-size: 30rpx;
    font-weight: 500;

    &.btn-cancel {
      background: rgba(255,255,255,0.1);
      color: #94a3b8;
    }

    &.btn-confirm {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: #fff;
    }
  }
}

// 成功动画
.success-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.8);
  z-index: 2000;
  display: flex;
  align-items: center;
  justify-content: center;

  .success-content {
    text-align: center;
    animation: scaleIn 0.3s ease;

    @keyframes scaleIn {
      from { transform: scale(0.5); opacity: 0; }
      to { transform: scale(1); opacity: 1; }
    }

    .success-circle {
      width: 160rpx;
      height: 160rpx;
      background: linear-gradient(135deg, #10b981 0%, #059669 100%);
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      margin: 0 auto 30rpx;
      animation: pulse 1s ease infinite;

      @keyframes pulse {
        0%, 100% { transform: scale(1); }
        50% { transform: scale(1.1); }
      }

      .success-icon {
        font-size: 80rpx;
        color: #fff;
      }
    }

    .success-text {
      display: block;
      font-size: 44rpx;
      font-weight: 600;
      color: #fff;
      margin-bottom: 16rpx;
    }

    .success-desc {
      font-size: 32rpx;
      color: #fbbf24;
    }
  }
}
</style>