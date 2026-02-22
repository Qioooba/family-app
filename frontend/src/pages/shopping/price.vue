<template>
  <view class="price-track-page">
    <!-- 头部 -->
    <view class="page-header">
      <view class="header-title">价格追踪</view>
      <view class="header-action" @click="showAddModal = true">
        <text class="icon">+</text>
      </view>
    </view>

    <!-- 统计卡片 -->
    <view class="stats-cards">
      <view class="stat-card">
        <text class="stat-value">{{ trackingList.length }}</text>
        <text class="stat-label">追踪商品</text>
      </view>
      <view class="stat-card highlight">
        <text class="stat-value">{{ priceDropCount }}</text>
        <text class="stat-label">降价提醒</text>
      </view>
      <view class="stat-card">
        <text class="stat-value">{{ saveAmount }}</text>
        <text class="stat-label">累计节省</text>
      </view>
    </view>

    <!-- 追踪列表 -->
    <view class="tracking-section">
      <view class="section-header">
        <text class="section-title">我的追踪</text>
        <view class="sort-btn" @click="toggleSort">
          <text>{{ sortByDrop ? '按降价排序' : '按时间排序' }}</text>
        </view>
      </view>

      <scroll-view class="tracking-list" scroll-y refresher-enabled :refresher-triggered="refreshing" @refresherrefresh="onRefresh">
        <view v-for="(item, index) in sortedList" :key="index" class="track-card" @click="viewDetail(item)">
          <!-- 商品信息 -->
          <view class="product-info">
            <view class="product-image-wrapper">
              <LazyImage v-if="item.image" :src="item.image" mode="aspectFill" width="64px" height="64px" radius="12px"/>
              <text v-else class="placeholder">📦</text>
            </view>
            <view class="product-detail">
              <text class="product-name">{{ item.productName }}</text>
              <text class="product-specs">{{ item.specs || '规格未知' }}</text>
              <view class="price-tags">
                <text class="current-price">¥{{ item.currentPrice }}</text>
                <text v-if="item.originalPrice" class="original-price">¥{{ item.originalPrice }}</text>
              </view>
            </view>
          </view>

          <!-- 价格走势 -->
          <view class="price-trend">
            <view class="trend-chart">
              <view class="chart-bars">
                <view 
                  v-for="(price, i) in item.recentPrices" 
                  :key="i"
                  class="bar"
                  :style="{ height: getBarHeight(price, item) + '%' }"
                  :class="{ lowest: isLowest(price, item.recentPrices) }"
                ></text>
              </view>
              <view class="chart-labels">
                <text>{{ getDaysAgo(item.recentPrices.length - 1) }}天</text>
                <text>今天</text>
              </view>
            </view>
            <view class="trend-info">
              <view class="trend-badge" :class="getTrendClass(item)">
                <text>{{ getTrendText(item) }}</text>
              </view>
              <text class="lowest-price">最低 ¥{{ item.lowestPrice }}</text>
            </view>
          </view>

          <!-- 操作栏 -->
          <view class="card-actions">
            <view class="action-btn" @click.stop="viewHistory(item)">
              <text>📊 历史价格</text>
            </view>
            <view class="action-btn" @click.stop="viewCompare(item)">
              <text>🔍 全网比价</text>
            </view>
            <view class="action-btn delete" @click.stop="deleteTracking(item)">
              <text>🗑️</text>
            </view>
          </view>

          <!-- 降价提醒标签 -->
          <view v-if="item.isPriceDrop" class="drop-badge">
            <text>🔥 降价 {{ item.dropPercent }}%</text>
          </view>
        </view>

        <!-- 空状态 -->
        <view v-if="trackingList.length === 0" class="empty-state">
          <text class="empty-icon">📈</text>
          <text class="empty-text">还没有追踪任何商品</text>
          <text class="empty-tip">点击右上角 + 添加追踪</text>
        </view>
      </scroll-view>
    </view>

    <!-- 添加追踪弹窗 -->
    <view v-if="showAddModal" class="modal-overlay">
      <view class="modal-mask" @click="showAddModal = false"></text>
      <view class="modal-content">
        <view class="modal-header">
          <text class="modal-title">添加价格追踪</text>
          <view class="close-btn" @click="showAddModal = false">×</view>
        </view>

        <view class="modal-body">
          <!-- 扫码添加 -->
          <view class="add-method scan-method" @click="scanToAdd">
            <view class="method-icon">📷</view>
            <view class="method-info">
              <text class="method-name">扫码添加</text>
              <text class="method-desc">扫描商品条码自动识别</text>
            </view>
          </view>

          <!-- 手动添加 -->
          <view class="add-method manual-method">
            <view class="method-icon">⌨️</view>
            <view class="method-info">
              <text class="method-name">手动输入</text>
              <text class="method-desc">输入商品条码或名称</text>
            </view>
            <input 
              v-model="manualBarcode"
              class="manual-input"
              placeholder="输入商品条码..."
              @confirm="addByBarcode"
            />
          </view>

          <!-- 最近浏览 -->
          <view v-if="recentProducts.length > 0" class="recent-section">
            <text class="section-subtitle">最近浏览</text>
            <scroll-view class="recent-list" scroll-x>
              <view 
                v-for="(product, i) in recentProducts" 
                :key="i"
                class="recent-item"
                @click="addRecent(product)"
              >
                <view class="recent-image-wrapper">
                  <LazyImage v-if="product.image" :src="product.image" mode="aspectFill" width="64px" height="64px" radius="12px"/>
                  <text v-else>📦</text>
                </view>
                <text class="recent-name">{{ product.name }}</text>
              </view>
            </scroll-view>
          </view>
        </view>
      </view>
    </view>

    <!-- 价格历史详情弹窗 -->
    <view v-if="showHistoryModal" class="modal-overlay">
      <view class="modal-mask" @click="showHistoryModal = false"></text>
      <view class="modal-content large">
        <view class="modal-header">
          <text class="modal-title">价格走势</text>
          <view class="close-btn" @click="showHistoryModal = false">×</view>
        </view>

        <view v-if="selectedItem" class="modal-body">
          <view class="detail-header">
            <text class="detail-name">{{ selectedItem.productName }}</text>
            <view class="detail-price">
              <text class="current">¥{{ selectedItem.currentPrice }}</text>
              <text class="range">历史最低 ¥{{ selectedItem.lowestPrice }}</text>
            </view>
          </view>

          <!-- 价格曲线图 -->
          <view class="chart-container">
            <view class="price-chart">
              <view class="chart-y-axis">
                <text v-for="i in 5" :key="i">¥{{ getYAxisValue(i) }}</text>
              </view>
              <view class="chart-main">
                <view class="grid-lines">
                  <view v-for="i in 5" :key="i" class="grid-line"></text>
                </view>
                <view class="price-line">
                  <view 
                    v-for="(point, i) in priceHistoryData" 
                    :key="i"
                    class="price-point"
                    :style="{ 
                      left: getPointX(i) + '%', 
                      bottom: getPointY(point.price) + '%' 
                    }"
                  >
                    <view class="point-dot" :class="{ lowest: point.isLowest }"></text>
                    <view class="point-tooltip">¥{{ point.price }}</view>
                  </view>
                  <view class="line-path" :style="linePathStyle"></text>
                </view>
                <view class="chart-x-axis">
                  <text v-for="(point, i) in xAxisLabels" :key="i">{{ point }}</text>
                </view>
              </view>
            </view>
          </view>

          <!-- 价格统计 -->
          <view class="price-stats">
            <view class="stat-item">
              <text class="stat-label">最高价</text>
              <text class="stat-value high">¥{{ priceStats.highest }}</text>
            </view>
            <view class="stat-item">
              <text class="stat-label">平均价</text>
              <text class="stat-value">¥{{ priceStats.average }}</text>
            </view>
            <view class="stat-item">
              <text class="stat-label">最低价</text>
              <text class="stat-value low">¥{{ priceStats.lowest }}</text>
            </view>
          </view>

          <!-- 价格提醒设置 -->
          <view class="alert-setting">
            <text class="setting-title">降价提醒设置</text>
            <view class="alert-options">
              <view 
                v-for="option in alertOptions" 
                :key="option.value"
                class="alert-option"
                :class="{ active: selectedAlert === option.value }"
                @click="selectedAlert = option.value"
              >
                <text>{{ option.label }}</text>
              </view>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 比价弹窗 -->
    <view v-if="showCompareModal" class="modal-overlay">
      <view class="modal-mask" @click="showCompareModal = false"></text>
      <view class="modal-content">
        <view class="modal-header">
          <text class="modal-title">全网比价</text>
          <view class="close-btn" @click="showCompareModal = false">×</view>
        </view>

        <view v-if="compareData" class="modal-body">
          <view class="compare-product">
            <LazyImage v-if="compareData.image" :src="compareData.image" mode="aspectFill" width="80px" height="80px" radius="12px"/>
            <text class="compare-name">{{ compareData.productName }}</text>
          </view>

          <view class="platform-list">
            <view 
              v-for="(platform, i) in compareData.platforms" 
              :key="i"
              class="platform-item"
              :class="{ best: platform.isLowest }"
            >
              <view class="platform-info">
                <text class="platform-name">{{ platform.name }}</text>
                <text class="platform-price">¥{{ platform.price }}</text>
              </view>
              <view class="platform-tag" v-if="platform.isLowest">
                <text>最低价</text>
              </view>
            </view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { shoppingApi } from '../../api/index.js'
import LazyImage from '@/components/common/LazyImage.vue'

const trackingList = ref([])
const refreshing = ref(false)
const sortByDrop = ref(false)
const showAddModal = ref(false)
const showHistoryModal = ref(false)
const showCompareModal = ref(false)
const manualBarcode = ref('')
const selectedItem = ref(null)
const priceHistoryData = ref([])
const compareData = ref(null)
const selectedAlert = ref(10)
const recentProducts = ref([])

// 统计计算
const priceDropCount = computed(() => {
  return trackingList.value.filter(item => item.isPriceDrop).length
})

const saveAmount = computed(() => {
  return trackingList.value.reduce((sum, item) => {
    if (item.originalPrice && item.currentPrice < item.originalPrice) {
      return sum + (item.originalPrice - item.currentPrice)
    }
    return sum
  }, 0).toFixed(0)
})

const sortedList = computed(() => {
  let list = [...trackingList.value]
  if (sortByDrop.value) {
    list.sort((a, b) => (b.dropPercent || 0) - (a.dropPercent || 0))
  } else {
    list.sort((a, b) => new Date(b.createTime) - new Date(a.createTime))
  }
  return list
})

const alertOptions = [
  { label: '降价5%', value: 5 },
  { label: '降价10%', value: 10 },
  { label: '降价20%', value: 20 },
  { label: '历史最低', value: 'lowest' }
]

const priceStats = computed(() => {
  if (!priceHistoryData.value.length) return { highest: 0, lowest: 0, average: 0 }
  const prices = priceHistoryData.value.map(p => p.price)
  return {
    highest: Math.max(...prices),
    lowest: Math.min(...prices),
    average: (prices.reduce((a, b) => a + b, 0) / prices.length).toFixed(1)
  }
})

const xAxisLabels = computed(() => {
  const labels = []
  const len = priceHistoryData.value.length
  if (len <= 5) {
    return priceHistoryData.value.map((_, i) => getDaysAgo(len - 1 - i))
  }
  for (let i = 0; i < 5; i++) {
    labels.push(getDaysAgo(Math.floor((len - 1) * (1 - i / 4))))
  }
  return labels
})

const linePathStyle = computed(() => {
  if (priceHistoryData.value.length < 2) return {}
  const points = priceHistoryData.value.map((p, i) => ({
    x: getPointX(i),
    y: getPointY(p.price)
  }))
  // 简化：使用CSS渐变模拟曲线
  return {}
})

// 加载追踪列表
const loadTrackingList = async () => {
  try {
    const familyId = uni.getStorageSync('currentFamilyId') || 1
    const res = await shoppingApi.getPriceTracking(familyId)
    trackingList.value = res || []
  } catch (e) {
    console.error('加载价格追踪失败', e)
  }
}

// 刷新
const onRefresh = async () => {
  refreshing.value = true
  await loadTrackingList()
  refreshing.value = false
}

// 切换排序
const toggleSort = () => {
  sortByDrop.value = !sortByDrop.value
}

// 扫码添加
const scanToAdd = () => {
  uni.scanCode({
    success: (res) => {
      addByBarcode(res.result)
    }
  })
}

// 通过条码添加
const addByBarcode = async (barcode) => {
  try {
    const familyId = uni.getStorageSync('currentFamilyId') || 1
    await shoppingApi.addPriceTracking({
      familyId,
      barcode: typeof barcode === 'string' ? barcode : manualBarcode.value
    })
    uni.showToast({ title: '添加成功', icon: 'success' })
    showAddModal.value = false
    manualBarcode.value = ''
    loadTrackingList()
  } catch (e) {
    uni.showToast({ title: '添加失败', icon: 'none' })
  }
}

// 添加最近商品
const addRecent = (product) => {
  addByBarcode(product.barcode)
}

// 查看详情
const viewDetail = (item) => {
  viewHistory(item)
}

// 查看历史价格
const viewHistory = async (item) => {
  selectedItem.value = item
  showHistoryModal.value = true
  try {
    const res = await shoppingApi.getPriceHistory(item.barcode)
    priceHistoryData.value = (res || []).map((p, i, arr) => ({
      ...p,
      isLowest: p.price === Math.min(...arr.map(x => x.price))
    }))
  } catch (e) {
    // 使用模拟数据
    generateMockHistory(item)
  }
}

// 生成模拟历史数据
const generateMockHistory = (item) => {
  const data = []
  const basePrice = item.currentPrice * 1.2
  for (let i = 6; i >= 0; i--) {
    const date = new Date()
    date.setDate(date.getDate() - i)
    data.push({
      date: date.toISOString().split('T')[0],
      price: (basePrice * (0.9 + Math.random() * 0.2)).toFixed(2),
      isLowest: i === 0 && item.currentPrice < basePrice * 0.95
    })
  }
  priceHistoryData.value = data
}

// 查看比价
const viewCompare = async (item) => {
  selectedItem.value = item
  showCompareModal.value = true
  try {
    const res = await shoppingApi.getPriceCompare(item.barcode)
    compareData.value = res
  } catch (e) {
    // 使用模拟数据
    compareData.value = {
      productName: item.productName,
      image: item.image,
      platforms: [
        { name: '京东', price: item.currentPrice, isLowest: true },
        { name: '天猫', price: (item.currentPrice * 1.05).toFixed(2), isLowest: false },
        { name: '拼多多', price: (item.currentPrice * 0.95).toFixed(2), isLowest: false },
        { name: '苏宁', price: (item.currentPrice * 1.02).toFixed(2), isLowest: false }
      ]
    }
  }
}

// 删除追踪
const deleteTracking = (item) => {
  uni.showModal({
    title: '确认删除',
    content: `停止追踪 ${item.productName} 的价格？`,
    success: async (res) => {
      if (res.confirm) {
        try {
          await shoppingApi.deletePriceTracking(item.id)
          uni.showToast({ title: '已删除', icon: 'success' })
          loadTrackingList()
        } catch (e) {
          uni.showToast({ title: '删除失败', icon: 'none' })
        }
      }
    }
  })
}

// 辅助函数
const getBarHeight = (price, item) => {
  if (!item.highestPrice || !item.lowestPrice) return 50
  const range = item.highestPrice - item.lowestPrice
  if (range === 0) return 50
  return 20 + ((price - item.lowestPrice) / range) * 80
}

const isLowest = (price, prices) => {
  return price === Math.min(...prices)
}

const getDaysAgo = (days) => {
  if (days === 0) return '今'
  return days
}

const getTrendClass = (item) => {
  if (item.isPriceDrop) return 'down'
  if (item.currentPrice > item.averagePrice) return 'up'
  return 'stable'
}

const getTrendText = (item) => {
  if (item.isPriceDrop) return `↓ ${item.dropPercent}%`
  if (item.currentPrice > item.averagePrice) return '↑ 偏高'
  return '→ 平稳'
}

const getYAxisValue = (i) => {
  const max = Math.max(...priceHistoryData.value.map(p => p.price))
  const min = Math.min(...priceHistoryData.value.map(p => p.price))
  const step = (max - min) / 4
  return (max - step * (i - 1)).toFixed(0)
}

const getPointX = (i) => {
  const len = priceHistoryData.value.length
  if (len <= 1) return 50
  return (i / (len - 1)) * 100
}

const getPointY = (price) => {
  const prices = priceHistoryData.value.map(p => p.price)
  const max = Math.max(...prices)
  const min = Math.min(...prices)
  const range = max - min
  if (range === 0) return 50
  return ((price - min) / range) * 80 + 10
}

onMounted(() => {
  loadTrackingList()
  // 加载最近浏览（从本地存储）
  recentProducts.value = uni.getStorageSync('recentProducts') || []
})
</script>

<style lang="scss" scoped>
.price-track-page {
  min-height: 100vh;
  background: #0f0f23;
  padding-bottom: 100px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 50px 20px 20px;

  .header-title {
    font-size: 24px;
    font-weight: 700;
    color: #fff;
  }

  .header-action {
    width: 40px;
    height: 40px;
    background: linear-gradient(135deg, #6366f1, #8b5cf6);
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;

    .icon {
      font-size: 24px;
      color: #fff;
    }
  }
}

.stats-cards {
  display: flex;
  gap: 12px;
  padding: 0 20px;
  margin-bottom: 20px;

  .stat-card {
    flex: 1;
    background: rgba(255,255,255,0.05);
    border-radius: 16px;
    padding: 16px;
    text-align: center;
    border: 1px solid rgba(255,255,255,0.08);

    &.highlight {
      background: linear-gradient(135deg, rgba(245,158,11,0.2), rgba(217,119,6,0.2));
      border-color: rgba(245,158,11,0.3);
    }

    .stat-value {
      display: block;
      font-size: 24px;
      font-weight: 700;
      color: #fff;
      margin-bottom: 4px;
    }

    .stat-label {
      font-size: 12px;
      color: #64748b;
    }
  }
}

.tracking-section {
  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 0 20px;
    margin-bottom: 12px;

    .section-title {
      font-size: 17px;
      font-weight: 600;
      color: #fff;
    }

    .sort-btn {
      padding: 6px 12px;
      background: rgba(255,255,255,0.05);
      border-radius: 20px;
      font-size: 12px;
      color: #64748b;
    }
  }
}

.tracking-list {
  height: calc(100vh - 280px);
}

.track-card {
  margin: 0 20px 12px;
  padding: 16px;
  background: rgba(255,255,255,0.05);
  border-radius: 20px;
  border: 1px solid rgba(255,255,255,0.08);
  position: relative;

  .product-info {
    display: flex;
    gap: 12px;
    margin-bottom: 16px;

    .product-image-wrapper {
      width: 64px;
      height: 64px;
      background: rgba(255,255,255,0.05);
      border-radius: 12px;
      overflow: hidden;
      display: flex;
      align-items: center;
      justify-content: center;

      .placeholder {
        font-size: 28px;
      }
    }

    .product-detail {
      flex: 1;

      .product-name {
        display: block;
        font-size: 15px;
        font-weight: 500;
        color: #fff;
        margin-bottom: 4px;
      }

      .product-specs {
        font-size: 12px;
        color: #64748b;
        margin-bottom: 8px;
      }

      .price-tags {
        display: flex;
        align-items: baseline;
        gap: 8px;

        .current-price {
          font-size: 20px;
          font-weight: 700;
          color: #ef4444;
        }

        .original-price {
          font-size: 13px;
          color: #64748b;
          text-decoration: line-through;
        }
      }
    }
  }

  .price-trend {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    padding: 12px;
    background: rgba(0,0,0,0.2);
    border-radius: 12px;

    .trend-chart {
      flex: 1;

      .chart-bars {
        display: flex;
        align-items: flex-end;
        gap: 4px;
        height: 40px;
        margin-bottom: 4px;

        .bar {
          flex: 1;
          background: #6366f1;
          border-radius: 2px;
          min-height: 4px;

          &.lowest {
            background: #10b981;
          }
        }
      }

      .chart-labels {
        display: flex;
        justify-content: space-between;
        font-size: 10px;
        color: #64748b;
      }
    }

    .trend-info {
      text-align: right;
      margin-left: 12px;

      .trend-badge {
        display: inline-block;
        padding: 4px 8px;
        border-radius: 6px;
        font-size: 12px;
        font-weight: 600;
        margin-bottom: 4px;

        &.down {
          background: rgba(16,185,129,0.2);
          color: #10b981;
        }

        &.up {
          background: rgba(239,68,68,0.2);
          color: #ef4444;
        }

        &.stable {
          background: rgba(100,116,139,0.2);
          color: #64748b;
        }
      }

      .lowest-price {
        font-size: 11px;
        color: #64748b;
      }
    }
  }

  .card-actions {
    display: flex;
    gap: 8px;

    .action-btn {
      flex: 1;
      padding: 10px 0;
      background: rgba(255,255,255,0.05);
      border-radius: 10px;
      text-align: center;
      font-size: 12px;
      color: #94a3b8;

      &.delete {
        flex: 0 0 44px;
      }
    }
  }

  .drop-badge {
    position: absolute;
    top: 12px;
    right: 12px;
    padding: 4px 8px;
    background: linear-gradient(135deg, #ef4444, #dc2626);
    border-radius: 6px;
    font-size: 11px;
    color: #fff;
    font-weight: 600;
  }
}

.empty-state {
  text-align: center;
  padding: 60px 20px;

  .empty-icon {
    font-size: 64px;
    margin-bottom: 16px;
  }

  .empty-text {
    display: block;
    font-size: 16px;
    color: #fff;
    margin-bottom: 8px;
  }

  .empty-tip {
    font-size: 13px;
    color: #64748b;
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
    max-height: 80vh;
    background: #1a1a2e;
    border-radius: 24px 24px 0 0;
    overflow: hidden;

    &.large {
      max-height: 90vh;
    }

    .modal-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 20px;
      border-bottom: 1px solid rgba(255,255,255,0.08);

      .modal-title {
        font-size: 18px;
        font-weight: 600;
        color: #fff;
      }

      .close-btn {
        width: 32px;
        height: 32px;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 24px;
        color: #64748b;
      }
    }

    .modal-body {
      padding: 20px;
      max-height: calc(80vh - 80px);
      overflow-y: auto;
    }
  }
}

// 添加方式
.add-method {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: rgba(255,255,255,0.05);
  border-radius: 16px;
  margin-bottom: 12px;

  .method-icon {
    width: 48px;
    height: 48px;
    background: rgba(99,102,241,0.2);
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 24px;
  }

  .method-info {
    flex: 1;

    .method-name {
      display: block;
      font-size: 15px;
      font-weight: 600;
      color: #fff;
      margin-bottom: 4px;
    }

    .method-desc {
      font-size: 12px;
      color: #64748b;
    }
  }
}

.manual-input {
  width: 100%;
  margin-top: 12px;
  padding: 12px 16px;
  background: rgba(0,0,0,0.2);
  border: 1px solid rgba(255,255,255,0.1);
  border-radius: 10px;
  font-size: 14px;
  color: #fff;
}

.recent-section {
  margin-top: 20px;

  .section-subtitle {
    font-size: 14px;
    color: #64748b;
    margin-bottom: 12px;
    display: block;
  }
}

.recent-list {
  white-space: nowrap;
}

.recent-item {
  display: inline-flex;
  flex-direction: column;
  align-items: center;
  margin-right: 12px;

  .recent-image-wrapper {
    width: 64px;
    height: 64px;
    background: rgba(255,255,255,0.05);
    border-radius: 12px;
    overflow: hidden;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 8px;
  }

  .recent-name {
    font-size: 11px;
    color: #94a3b8;
    max-width: 64px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

// 详情弹窗
.detail-header {
  text-align: center;
  margin-bottom: 20px;

  .detail-name {
    display: block;
    font-size: 16px;
    font-weight: 600;
    color: #fff;
    margin-bottom: 8px;
  }

  .detail-price {
    .current {
      font-size: 28px;
      font-weight: 700;
      color: #ef4444;
      margin-right: 8px;
    }

    .range {
      font-size: 13px;
      color: #64748b;
    }
  }
}

// 图表
.chart-container {
  margin-bottom: 20px;
}

.price-chart {
  display: flex;
  height: 200px;

  .chart-y-axis {
    width: 40px;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    text-align: right;
    padding-right: 8px;
    font-size: 10px;
    color: #64748b;
  }

  .chart-main {
    flex: 1;
    position: relative;

    .grid-lines {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 30px;

      .grid-line {
        height: 20%;
        border-bottom: 1px dashed rgba(255,255,255,0.1);
      }
    }

    .price-line {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 30px;

      .price-point {
        position: absolute;
        transform: translateX(-50%);

        .point-dot {
          width: 8px;
          height: 8px;
          background: #6366f1;
          border-radius: 50%;

          &.lowest {
            background: #10b981;
            box-shadow: 0 0 8px #10b981;
          }
        }

        .point-tooltip {
          position: absolute;
          bottom: 12px;
          left: 50%;
          transform: translateX(-50%);
          font-size: 10px;
          color: #fff;
          white-space: nowrap;
        }
      }
    }

    .chart-x-axis {
      position: absolute;
      bottom: 0;
      left: 0;
      right: 0;
      display: flex;
      justify-content: space-between;
      font-size: 10px;
      color: #64748b;
    }
  }
}

.price-stats {
  display: flex;
  justify-content: space-around;
  padding: 16px;
  background: rgba(255,255,255,0.05);
  border-radius: 12px;
  margin-bottom: 20px;

  .stat-item {
    text-align: center;

    .stat-label {
      display: block;
      font-size: 12px;
      color: #64748b;
      margin-bottom: 4px;
    }

    .stat-value {
      font-size: 18px;
      font-weight: 600;
      color: #fff;

      &.high {
        color: #ef4444;
      }

      &.low {
        color: #10b981;
      }
    }
  }
}

.alert-setting {
  .setting-title {
    display: block;
    font-size: 14px;
    color: #64748b;
    margin-bottom: 12px;
  }

  .alert-options {
    display: flex;
    gap: 8px;
    flex-wrap: wrap;

    .alert-option {
      padding: 10px 16px;
      background: rgba(255,255,255,0.05);
      border-radius: 20px;
      font-size: 13px;
      color: #94a3b8;

      &.active {
        background: linear-gradient(135deg, #6366f1, #8b5cf6);
        color: #fff;
      }
    }
  }
}

// 比价弹窗
.compare-product {
  text-align: center;
  margin-bottom: 20px;

  .compare-name {
    font-size: 15px;
    color: #fff;
  }
}

.platform-list {
  .platform-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px;
    background: rgba(255,255,255,0.05);
    border-radius: 12px;
    margin-bottom: 8px;

    &.best {
      background: rgba(16,185,129,0.1);
      border: 1px solid rgba(16,185,129,0.3);
    }

    .platform-info {
      .platform-name {
        display: block;
        font-size: 14px;
        color: #fff;
        margin-bottom: 4px;
      }

      .platform-price {
        font-size: 18px;
        font-weight: 700;
        color: #ef4444;
      }
    }

    .platform-tag {
      padding: 4px 10px;
      background: #10b981;
      border-radius: 4px;
      font-size: 11px;
      color: #fff;
    }
  }
}
</style>