<template>
  <view class="points-page"
>
    <!-- 顶部导航 -->
    <view class="nav-bar"
003e
      <view class="back-btn" @click="goBack"
>
        <u-icon name="arrow-left" size="40" color="#333"></u-icon>
      </view>
      <text class="title">积分兑换</text>
      <view class="right-btn" @click="showRules">
        <text>规则</text>
      </view>
    </view>

    <view class="content">
      <!-- 积分卡片 -->
      <view class="points-card"
>
        <view class="points-bg"></view>
        
        <view class="points-content"
003e
          <text class="points-label">我的积分</text>          
          <view class="points-value-wrap"
003e
            <text class="points-value" :class="{ 'animate-pulse': isAnimating }">{{ userPoints }}</text>
            <text class="points-unit">分</text>
          </view>
          
          <view class="points-info"
003e
            <text class="info-item">
              累计获得 {{ totalEarned }}
            </text>
            <text class="info-divider">|</text>
            <text class="info-item">
              已兑换 {{ totalSpent }}
            </text>
          </view>
        </view>
        
        <view class="points-decoration">
          <text class="coin-icon">🪙</text>
          <text class="star-icon">✨</text>
        </view>
      </view>

      <!-- 积分动画展示区域 -->
      <view v-if="showPointsAnimation" class="points-animation"
003e
        <view class="floating-points"
>
          <text v-for="n in 5" :key="n" class="floating-point" :style="getFloatingStyle(n)">+{{ recentPoints }}</text>
        </view>
      </view>

      <!-- 兑换分类 -->
      <view class="category-section">
        <scroll-view class="category-list" scroll-x>
          <view
            v-for="cat in categories"
            :key="cat.value"
            class="category-item"
            :class="{ active: currentCategory === cat.value }"
            @click="currentCategory = cat.value"
          >
            <text class="category-icon">{{ cat.icon }}</text>
            <text class="category-name">{{ cat.name }}</text>
          </view>
        </scroll-view>
      </view>

      <!-- 商品列表 -->
      <view class="goods-section">
        <view class="section-header"
>
          <text class="section-title">热门兑换</text>
          <text class="view-all" @click="viewAllGoods">全部 ›</text>
        </view>

        <view class="goods-grid">
          <view
            v-for="goods in filteredGoods"
            :key="goods.id"
            class="goods-card"
            :class="{ 'can-exchange': userPoints >= goods.points, 'hot': goods.isHot }"
          >
            <view v-if="goods.isHot" class="hot-badge">🔥 热门</view>
            
            <view class="goods-image"
>
              <text class="goods-icon">{{ goods.icon }}</text>
            </view>            
            
            <view class="goods-info"
>
              <text class="goods-name">{{ goods.name }}</text>
              
              <text class="goods-desc">{{ goods.description }}</text>
              
              <view class="goods-points">
                <text class="points-num">{{ goods.points }}</text>
                <text class="points-text">积分</text>
              </view>
            </view>

            <view 
              class="exchange-btn"
              :class="{ disabled: userPoints < goods.points }"
              @click="exchangeGoods(goods)"
            >
              {{ userPoints >= goods.points ? '立即兑换' : '积分不足' }}
            </view>
          </view>
        </view>
      </view>

      <!-- 兑换记录 -->
      <view class="history-section">
        <view class="section-header"
>
          <text class="section-title">兑换记录</text>
          <text class="view-all" @click="viewAllHistory">全部 ›</text>
        </view>

        <view class="history-list">
          <view
            v-for="record in exchangeHistory.slice(0, 5)"
            :key="record.id"
            class="history-item"
          >
            <view class="history-icon">{{ record.goodsIcon }}</view>
            
            <view class="history-info"
>
              <text class="history-name">{{ record.goodsName }}</text>
              <text class="history-time">{{ record.time }}</text>
            </view>
            
            <view class="history-points">
              <text class="points-deduct">-{{ record.points }}</text>
              <text class="status-badge" :class="record.status">
                {{ record.statusText }}
              </text>
            </view>
          </view>
          
          <view v-if="exchangeHistory.length === 0" class="empty-history"
003e
            <u-icon name="file-text" size="60" color="#ddd"></u-icon>
            <text>暂无兑换记录</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 兑换确认弹窗 -->
    <u-popup
      v-model:show="exchangeModalVisible"
      mode="bottom"
      round
      closeable
    >
      <view v-if="selectedGoods" class="exchange-modal"
003e
        <view class="modal-header"
003e
          <text class="modal-title">确认兑换</text>
        </view>

        <view class="modal-body"
>
          <view class="goods-preview"
003e
            <view class="preview-icon"
>{{ selectedGoods.icon }}</view>
            
            <view class="preview-info"
>
              <text class="preview-name">{{ selectedGoods.name }}</text>
              <text class="preview-desc">{{ selectedGoods.description }}</text>
            </view>          
          </view>

          <view class="exchange-info">
            <view class="info-row"
>
              <text class="info-label">所需积分</text>
              <text class="info-value points">{{ selectedGoods.points }} 积分</text>
            </view>            
            
            <view class="info-row">
              <text class="info-label">我的积分</text>
              <text class="info-value">{{ userPoints }} 积分</text>
            </view>            
            
            <view class="info-row total"
003e
              <text class="info-label">兑换后剩余</text>
              <text class="info-value remain">{{ userPoints - selectedGoods.points }} 积分</text>
            </view>
          </view>

          <view class="exchange-notice"
003e
            <u-icon name="info-circle" size="28" color="#FAAD14"></u-icon>
            <text>兑换后积分将立即扣除，虚拟商品将发送至账户</text>
          </view>
        </view>

        <view class="modal-footer"
>
          <view class="btn-cancel" @click="exchangeModalVisible = false">取消</view>
          <view class="btn-confirm" @click="confirmExchange">确认兑换</view>
        </view>
      </view>
    </u-popup>

    <!-- 兑换成功动画弹窗 -->
    <u-popup
      v-model:show="successModalVisible"
      mode="center"
    >
      <view class="success-modal"
>
        <view class="success-animation"
003e
          <view class="success-circle"
003e
            <u-icon name="checkmark" size="60" color="#fff"></u-icon>
          </view>          
          
          <view class="confetti"
003e
            <text v-for="n in 6" :key="n" class="confetti-piece" :style="getConfettiStyle(n)">🎉</text>
          </view>
        </view>

        <text class="success-title">兑换成功！</text>        
        <text class="success-desc"
003e
          您已成功兑换 {{ exchangedGoods?.name }}
        </text>        
        
        <view class="success-points"
>
          <text>消耗 {{ exchangedGoods?.points }} 积分</text>
        </view>

        <view class="success-actions"
>
          <view class="btn-continue" @click="successModalVisible = false"
003e
            继续兑换
          </view>          
          
          <view class="btn-view" @click="viewExchangeDetail">
            查看详情
          </view>
        </view>
      </view>
    </u-popup>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import dayjs from 'dayjs'

// 响应式数据
const userPoints = ref(2580)
const totalEarned = ref(5000)
const totalSpent = ref(2420)
const currentCategory = ref('all')
const selectedGoods = ref(null)
const exchangedGoods = ref(null)
const exchangeModalVisible = ref(false)
const successModalVisible = ref(false)
const isAnimating = ref(false)
const showPointsAnimation = ref(false)
const recentPoints = ref(0)

// 分类
const categories = [
  { name: '全部', value: 'all', icon: '🎁' },
  { name: '虚拟物品', value: 'virtual', icon: '🎫' },
  { name: '优惠券', value: 'coupon', icon: '🎟️' },
  { name: '实物礼品', value: 'physical', icon: '📦' },
  { name: '特权卡', value: 'vip', icon: '👑' }
]

// 兑换商品
const goodsList = ref([
  {
    id: 1,
    name: '京东50元券',
    description: '无门槛优惠券',
    points: 500,
    icon: '🎫',
    category: 'coupon',
    isHot: true
  },
  {
    id: 2,
    name: '月度会员',
    description: '30天VIP特权',
    points: 1000,
    icon: '👑',
    category: 'vip',
    isHot: true
  },
  {
    id: 3,
    name: '定制头像框',
    description: '专属个性头像框',
    points: 200,
    icon: '🖼️',
    category: 'virtual',
    isHot: false
  },
  {
    id: 4,
    name: '精美笔记本',
    description: '限量版周边',
    points: 800,
    icon: '📓',
    category: 'physical',
    isHot: false
  },
  {
    id: 5,
    name: '双倍积分卡',
    description: '7天内积分翻倍',
    points: 300,
    icon: '💰',
    category: 'virtual',
    isHot: true
  },
  {
    id: 6,
    name: '专属表情包',
    description: '限定表情包套装',
    points: 150,
    icon: '😊',
    category: 'virtual',
    isHot: false
  },
  {
    id: 7,
    name: '星巴克咖啡券',
    description: '中杯任意饮品',
    points: 400,
    icon: '☕',
    category: 'coupon',
    isHot: false
  },
  {
    id: 8,
    name: '定制马克杯',
    description: '专属logo马克杯',
    points: 600,
    icon: '☕',
    category: 'physical',
    isHot: false
  }
])

// 兑换记录
const exchangeHistory = ref([
  {
    id: 1,
    goodsName: '京东50元券',
    goodsIcon: '🎫',
    points: 500,
    time: '2026-02-20 14:30',
    status: 'completed',
    statusText: '已完成'
  },
  {
    id: 2,
    goodsName: '定制头像框',
    goodsIcon: '🖼️',
    points: 200,
    time: '2026-02-18 09:15',
    status: 'completed',
    statusText: '已完成'
  },
  {
    id: 3,
    goodsName: '月度会员',
    goodsIcon: '👑',
    points: 1000,
    time: '2026-02-15 20:00',
    status: 'completed',
    statusText: '已完成'
  }
])

// 计算属性
const filteredGoods = computed(() => {
  if (currentCategory.value === 'all') {
    return goodsList.value
  }
  return goodsList.value.filter(g => g.category === currentCategory.value)
})

// 方法
const exchangeGoods = (goods) => {
  if (userPoints.value < goods.points) {
    uni.showToast({ title: '积分不足', icon: 'none' })
    return
  }
  
  selectedGoods.value = goods
  exchangeModalVisible.value = true
}

const confirmExchange = () => {
  exchangeModalVisible.value = false
  
  // 扣除积分
  const points = selectedGoods.value.points
  userPoints.value -= points
  totalSpent.value += points
  
  // 添加到历史记录
  const record = {
    id: Date.now(),
    goodsName: selectedGoods.value.name,
    goodsIcon: selectedGoods.value.icon,
    points: points,
    time: dayjs().format('YYYY-MM-DD HH:mm'),
    status: 'completed',
    statusText: '已完成'
  }
  exchangeHistory.value.unshift(record)
  
  // 显示成功动画
  exchangedGoods.value = selectedGoods.value
  successModalVisible.value = true
  
  // 积分动画
  showPointsChange(-points)
}

const showPointsChange = (points) => {
  recentPoints.value = Math.abs(points)
  showPointsAnimation.value = true
  isAnimating.value = true
  
  setTimeout(() => {
    showPointsAnimation.value = false
    isAnimating.value = false
  }, 2000)
}

const getFloatingStyle = (n) => {
  const delay = (n - 1) * 0.2
  const x = (Math.random() - 0.5) * 200
  return {
    animationDelay: `${delay}s`,
    left: `${50 + x}px`
  }
}

const getConfettiStyle = (n) => {
  const angle = (n - 1) * 60
  const delay = (n - 1) * 0.1
  return {
    transform: `rotate(${angle}deg) translateX(100px)`,
    animationDelay: `${delay}s`
  }
}

const viewAllGoods = () => {
  uni.showToast({ title: '查看全部商品', icon: 'none' })
}

const viewAllHistory = () => {
  uni.navigateTo({ url: '/pages/game/points-history' })
}

const viewExchangeDetail = () => {
  successModalVisible.value = false
  uni.navigateTo({ url: '/pages/game/points-history' })
}

const showRules = () => {
  uni.showModal({
    title: '积分规则',
    content: '1. 完成任务可获得积分\n2. 每日签到可获得积分\n3. 积分可用于兑换商品\n4. 积分不可提现',
    showCancel: false
  })
}

const goBack = () => {
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
.points-page {
  min-height: 100vh;
  background: #f5f6fa;
}

.nav-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 30rpx;
  padding-top: 60rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);

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

// 积分卡片
.points-card {
  position: relative;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 24rpx;
  padding: 50rpx 40rpx;
  margin-top: -40rpx;
  margin-bottom: 30rpx;
  overflow: hidden;

  .points-bg {
    position: absolute;
    top: -50%;
    right: -20%;
    width: 300rpx;
    height: 300rpx;
    background: rgba(255, 255, 255, 0.1);
    border-radius: 50%;
  }

  .points-content {
    position: relative;
    z-index: 1;

    .points-label {
      font-size: 26rpx;
      color: rgba(255, 255, 255, 0.8);
      display: block;
      margin-bottom: 16rpx;
    }

    .points-value-wrap {
      display: flex;
      align-items: baseline;
      gap: 12rpx;
      margin-bottom: 20rpx;

      .points-value {
        font-size: 80rpx;
        font-weight: 700;
        color: #fff;
        line-height: 1;

        &.animate-pulse {
          animation: pulse 0.5s ease-in-out;
        }
      }

      .points-unit {
        font-size: 32rpx;
        color: rgba(255, 255, 255, 0.8);
      }
    }

    .points-info {
      display: flex;
      align-items: center;
      gap: 20rpx;

      .info-item {
        font-size: 24rpx;
        color: rgba(255, 255, 255, 0.7);
      }

      .info-divider {
        font-size: 24rpx;
        color: rgba(255, 255, 255, 0.3);
      }
    }
  }

  .points-decoration {
    position: absolute;
    top: 30rpx;
    right: 30rpx;

    .coin-icon {
      font-size: 80rpx;
      display: block;
    }

    .star-icon {
      font-size: 40rpx;
      position: absolute;
      top: -10rpx;
      right: -10rpx;
      animation: twinkle 1.5s infinite;
    }
  }
}

@keyframes pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.1); }
}

@keyframes twinkle {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.5; transform: scale(0.8); }
}

// 积分动画
.points-animation {
  position: fixed;
  top: 200rpx;
  left: 50%;
  transform: translateX(-50%);
  z-index: 999;

  .floating-points {
    position: relative;

    .floating-point {
      position: absolute;
      font-size: 36rpx;
      font-weight: 700;
      color: #FF6B6B;
      animation: float 2s ease-out forwards;
    }
  }
}

@keyframes float {
  0% {
    opacity: 1;
    transform: translateY(0);
  }
  100% {
    opacity: 0;
    transform: translateY(-200rpx);
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
    flex-direction: column;
    align-items: center;
    margin-right: 24rpx;
    padding: 20rpx 24rpx;
    background: #fff;
    border-radius: 16rpx;
    border: 2rpx solid transparent;

    .category-icon {
      font-size: 40rpx;
      margin-bottom: 8rpx;
    }

    .category-name {
      font-size: 24rpx;
      color: #666;
    }

    &.active {
      border-color: #667eea;
      background: #f0f5ff;

      .category-name {
        color: #667eea;
        font-weight: 500;
      }
    }
  }
}

// 商品区域
.goods-section {
  margin-bottom: 30rpx;

  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20rpx;

    .section-title {
      font-size: 30rpx;
      font-weight: 600;
      color: #333;
    }

    .view-all {
      font-size: 26rpx;
      color: #667eea;
    }
  }
}

.goods-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20rpx;
}

.goods-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  position: relative;

  &.can-exchange {
    border: 2rpx solid transparent;
  }

  &.hot {
    .goods-points {
      .points-num {
        color: #FF6B6B;
      }
    }
  }

  .hot-badge {
    position: absolute;
    top: 16rpx;
    right: 16rpx;
    padding: 6rpx 12rpx;
    background: linear-gradient(135deg, #FF6B6B 0%, #FF8E8E 100%);
    border-radius: 8rpx;
    font-size: 20rpx;
    color: #fff;
  }

  .goods-image {
    width: 120rpx;
    height: 120rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #f5f6fa;
    border-radius: 16rpx;
    margin-bottom: 16rpx;

    .goods-icon {
      font-size: 64rpx;
    }
  }

  .goods-info {
    margin-bottom: 16rpx;

    .goods-name {
      font-size: 28rpx;
      font-weight: 500;
      color: #333;
      display: block;
      margin-bottom: 8rpx;
    }

    .goods-desc {
      font-size: 22rpx;
      color: #999;
      display: block;
      margin-bottom: 12rpx;
    }

    .goods-points {
      .points-num {
        font-size: 32rpx;
        font-weight: 700;
        color: #667eea;
      }

      .points-text {
        font-size: 22rpx;
        color: #999;
      }
    }
  }

  .exchange-btn {
    width: 100%;
    padding: 16rpx 0;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 12rpx;
    text-align: center;
    font-size: 26rpx;
    color: #fff;

    &.disabled {
      background: #e8e8e8;
      color: #999;
    }
  }
}

// 历史记录
.history-section {
  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20rpx;

    .section-title {
      font-size: 30rpx;
      font-weight: 600;
      color: #333;
    }

    .view-all {
      font-size: 26rpx;
      color: #667eea;
    }
  }
}

.history-list {
  background: #fff;
  border-radius: 16rpx;
  padding: 0 24rpx;
}

.history-item {
  display: flex;
  align-items: center;
  padding: 24rpx 0;
  border-bottom: 1rpx solid #f5f5f5;

  &:last-child {
    border-bottom: none;
  }

  .history-icon {
    width: 72rpx;
    height: 72rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #f5f6fa;
    border-radius: 16rpx;
    font-size: 40rpx;
    margin-right: 20rpx;
  }

  .history-info {
    flex: 1;

    .history-name {
      font-size: 28rpx;
      color: #333;
      display: block;
    }

    .history-time {
      font-size: 22rpx;
      color: #999;
      margin-top: 4rpx;
      display: block;
    }
  }

  .history-points {
    text-align: right;

    .points-deduct {
      font-size: 28rpx;
      font-weight: 600;
      color: #FF6B6B;
      display: block;
    }

    .status-badge {
      font-size: 20rpx;
      padding: 4rpx 12rpx;
      border-radius: 8rpx;
      margin-top: 4rpx;
      display: inline-block;

      &.completed {
        background: #F6FFED;
        color: #52C41A;
      }
    }
  }
}

.empty-history {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 60rpx 0;

  text {
    font-size: 26rpx;
    color: #999;
    margin-top: 16rpx;
  }
}

// 兑换弹窗
.exchange-modal {
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

  .goods-preview {
    display: flex;
    align-items: center;
    gap: 24rpx;
    padding: 24rpx;
    background: #f5f6fa;
    border-radius: 16rpx;
    margin-bottom: 30rpx;

    .preview-icon {
      width: 100rpx;
      height: 100rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      background: #fff;
      border-radius: 16rpx;
      font-size: 56rpx;
    }

    .preview-info {
      flex: 1;

      .preview-name {
        font-size: 30rpx;
        font-weight: 500;
        color: #333;
        display: block;
        margin-bottom: 8rpx;
      }

      .preview-desc {
        font-size: 24rpx;
        color: #999;
      }
    }
  }

  .exchange-info {
    margin-bottom: 30rpx;

    .info-row {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 16rpx 0;

      &.total {
        border-top: 2rpx solid #f0f0f0;
        margin-top: 16rpx;
        padding-top: 24rpx;
      }

      .info-label {
        font-size: 28rpx;
        color: #666;
      }

      .info-value {
        font-size: 28rpx;
        color: #333;

        &.points {
          font-weight: 600;
          color: #FF6B6B;
        }

        &.remain {
          font-weight: 600;
          color: #52C41A;
        }
      }
    }
  }

  .exchange-notice {
    display: flex;
    align-items: center;
    gap: 12rpx;
    padding: 20rpx;
    background: #FFFBE6;
    border-radius: 12rpx;

    text {
      flex: 1;
      font-size: 24rpx;
      color: #666;
    }
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
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: #fff;
    }
  }
}

// 成功弹窗
.success-modal {
  width: 560rpx;
  background: #fff;
  border-radius: 24rpx;
  padding: 50rpx 40rpx;
  text-align: center;
}

.success-animation {
  position: relative;
  margin-bottom: 30rpx;

  .success-circle {
    width: 120rpx;
    height: 120rpx;
    background: linear-gradient(135deg, #52C41A 0%, #73D13D 100%);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 auto;
    animation: scaleIn 0.3s ease-out;
  }

  .confetti {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);

    .confetti-piece {
      position: absolute;
      font-size: 32rpx;
      animation: explode 1s ease-out forwards;
    }
  }
}

@keyframes scaleIn {
  0% {
    transform: scale(0);
  }
  100% {
    transform: scale(1);
  }
}

@keyframes explode {
  0% {
    opacity: 1;
    transform: rotate(0deg) translateX(0);
  }
  100% {
    opacity: 0;
    transform: rotate(360deg) translateX(150rpx);
  }
}

.success-title {
  font-size: 40rpx;
  font-weight: 700;
  color: #333;
  display: block;
  margin-bottom: 16rpx;
}

.success-desc {
  font-size: 28rpx;
  color: #666;
  display: block;
  margin-bottom: 20rpx;
}

.success-points {
  padding: 16rpx 32rpx;
  background: #FFF1F0;
  border-radius: 30rpx;
  display: inline-block;
  margin-bottom: 30rpx;

  text {
    font-size: 26rpx;
    color: #FF6B6B;
  }
}

.success-actions {
  display: flex;
  gap: 20rpx;

  .btn-continue, .btn-view {
    flex: 1;
    padding: 24rpx 0;
    border-radius: 12rpx;
    font-size: 28rpx;
  }

  .btn-continue {
    background: #f5f5f5;
    color: #666;
  }

  .btn-view {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: #fff;
  }
}
</style>