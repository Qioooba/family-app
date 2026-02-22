<template>
  <view class="shopping-page">
    <!-- 头部 -->
    <view class="shopping-header">
      <view class="header-title">智能购物</view>
      <view class="scan-btn" @click="scanCode">
        <text class="icon">📷</text>
        <text>扫码</text>
      </view>
    </view>

    <!-- 快捷操作 -->
    <view class="quick-actions">
      <view class="action-card" @click="createList">
        <view class="action-icon" style="background: linear-gradient(135deg, #3b82f6, #2563eb);">📝</view>
        <view class="action-info">
          <text class="action-name">新建清单</text>
          <text class="action-desc">创建购物计划</text>
        </view>
      </view>
      
      <view class="action-card" @click="goInventory">
        <view class="action-icon" style="background: linear-gradient(135deg, #10b981, #059669);">📦</view>
        <view class="action-info">
          <text class="action-name">库存管理</text>
          <text class="action-desc">查看家中存货</text>
        </view>
      </view>

      <view class="action-card" @click="goPriceTrack">
        <view class="action-icon" style="background: linear-gradient(135deg, #f59e0b, #d97706);">📈</view>
        <view class="action-info">
          <text class="action-name">价格追踪</text>
          <text class="action-desc">监控价格变动</text>
        </view>
      </view>
    </view>

    <!-- 进行中的清单 -->
    <view class="section">
      <view class="section-header"
      >
        <text class="section-title">进行中的清单</text>
        <text class="section-more">查看全部 ›</text>
      </view>
      
      <view v-for="(list, index) in activeLists" :key="index" class="list-card"
      >
        <view class="list-header"
        >
          <view class="list-info"
          >
            <text class="list-name">{{ list.name }}</text>
            <text class="list-meta">{{ list.itemCount }}项 · 预计¥{{ list.estimatedAmount }}</text>
          </view>
          <view class="list-progress"
          >
            <view class="progress-ring"
          >
              <text class="progress-text">{{ list.progress }}%</text>
            </view>
          </view>
        </view>
        
        <view class="list-items"
        >
          <view 
            v-for="(item, i) in list.items.slice(0, 3)" 
            :key="item.id || i"
            class="list-item"
            :class="{ checked: item.checked }"
          >
            <view class="checkbox" :class="{ checked: item.checked }"></text>
            <text class="item-name">{{ item.name }}</text>
            <text class="item-qty">{{ item.quantity }}{{ item.unit }}</text>
          </view>
          <view v-if="list.items.length > 3" class="more-items"
          >
            还有 {{ list.items.length - 3 }} 项...
          </view>
        </view>
        
        <view class="list-footer"
        >
          <view class="assignee"
          >
            <text>指派给：{{ list.assignee }}</text>
          </view>
          <view class="list-actions"
          >
            <text class="action">编辑</text>
            <text class="action primary">去购买</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 临期提醒 -->
    <view class="section">
      <view class="section-header"
      >
        <text class="section-title">⏰ 临期提醒</text>
      </view>
      
      <view class="expiry-list"
      >
        <view v-for="(item, index) in expiringItems" :key="index" class="expiry-item"
        >
          <view class="expiry-icon" :class="{ urgent: item.days <= 3 }"
          >{{ item.days <= 3 ? '⚠️' : '⏰' }}</view>
          <view class="expiry-info"
          >
            <text class="expiry-name">{{ item.name }}</text>
            <text class="expiry-date" :class="{ urgent: item.days <= 3 }">
              {{ item.days <= 0 ? '已过期' : item.days + '天后过期' }}
            </text>
          </view>
          <view class="expiry-qty">{{ item.quantity }}{{ item.unit }}</view>
        </view>
      </view>
    </view>
    
    <!-- 扫码结果弹窗 -->
    <view v-if="showScanModal" class="scan-modal">
      <view class="scan-modal-mask" @click="closeScanModal"></text>
      
      <view class="scan-modal-content">
        <view class="scan-modal-header">
          <text class="scan-modal-title">扫码录入</text>
          <view class="close-btn" @click="closeScanModal">×</view>
        </view>
        
        <view v-if="scanLoading" class="scan-loading">
          <view class="loading-spinner"></text>
          <text class="loading-text">正在识别商品...</text>
        </view>
        
        <view v-else class="scan-form">
          <!-- 商品图片 -->
          <view class="product-image-section">
            <view v-if="scanResult.image" class="product-image">
              <image :src="scanResult.image" mode="aspectFill"></image>
            </view>
            <view v-else class="product-image-placeholder">
              <text class="placeholder-icon">📦</text>
            </view>
          </view>
          
          <!-- 条码信息 -->
          <view class="form-item">
            <text class="form-label">商品条码</text>
            <view class="form-value barcode">{{ scanResult.barcode }}</view>
          </view>
          
          <!-- 商品名称 -->
          <view class="form-item">
            <text class="form-label">商品名称 *</text>
            <input 
              v-model="scanResult.name"
              class="form-input"
              placeholder="输入商品名称"
              :disabled="scanResult.source === 'database'"
            />
          </view>
          
          <!-- 品牌 -->
          <view class="form-item">
            <text class="form-label">品牌</text>
            <input 
              v-model="scanResult.brand"
              class="form-input"
              placeholder="输入品牌"
            />
          </view>
          
          <!-- 价格 -->
          <view class="form-item">
            <text class="form-label">参考价格</text>
            <view class="price-input-wrapper">
              <text class="currency">¥</text>
              <input 
                v-model="scanResult.price"
                class="form-input price-input"
                type="digit"
                placeholder="0.00"
              />
            </view>
          </view>
          
          <!-- 分类 -->
          <view class="form-item">
            <text class="form-label">分类</text>
            <picker mode="selector" :range="categories" :value="categoryIndex" @change="onCategoryChange">
              <view class="form-picker">{{ scanResult.category }}</view>
            </picker>
          </view>
          
          <!-- 添加到清单选择 -->
          <view class="form-item" v-if="activeLists.length > 0">
            <text class="form-label">添加到清单</text>
            <picker mode="selector" :range="listNames" :value="listIndex" @change="onListChange">
              <view class="form-picker">{{ selectedListId ? getListNameById(selectedListId) : '不添加到清单（加入库存）' }}</view>
            </picker>
          </view>
          
          <!-- 来源标识 -->
          <view class="source-tag" v-if="scanResult.source === 'database'">
            <text class="tag">🗄️ 来自商品库</text>
          </view>
          <view class="source-tag manual" v-else>
            <text class="tag">✏️ 手动录入</text>
          </view>
          
          <!-- 操作按钮 -->
          <view class="scan-actions">
            <view class="btn btn-secondary" @click="closeScanModal">取消</view>
            <view class="btn btn-primary" @click="confirmAddProduct">确认添加</view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { shoppingApi } from '../../api/index.js'

const activeLists = ref([])
const expiringItems = ref([])
const loading = ref(false)

// 扫码相关
const scanResult = ref(null)
const showScanModal = ref(false)
const scanLoading = ref(false)
const selectedListId = ref(null)

// 分类选项
const categories = ['其他', '食品', '饮料', '日用品', '洗护', '生鲜', '零食', '酒水']
const categoryIndex = computed(() => {
  if (!scanResult.value) return 0
  return categories.indexOf(scanResult.value.category) || 0
})

// 清单名称列表
const listNames = computed(() => {
  return ['不添加到清单（加入库存）', ...activeLists.value.map(l => l.name)]
})

const listIndex = computed(() => {
  if (!selectedListId.value) return 0
  const index = activeLists.value.findIndex(l => l.id === selectedListId.value)
  return index >= 0 ? index + 1 : 0
})

// 根据ID获取清单名称
const getListNameById = (id) => {
  const list = activeLists.value.find(l => l.id === id)
  return list ? list.name : '未知清单'
}

// 分类选择变更
const onCategoryChange = (e) => {
  if (scanResult.value) {
    scanResult.value.category = categories[e.detail.value]
  }
}

// 清单选择变更
const onListChange = (e) => {
  const index = e.detail.value
  if (index === 0) {
    selectedListId.value = null
  } else {
    selectedListId.value = activeLists.value[index - 1]?.id || null
  }
}

// 加载购物清单
const loadShoppingLists = async () => {
  loading.value = true
  try {
    const familyId = uni.getStorageSync('currentFamilyId') || 1
    const res = await shoppingApi.getLists(familyId)
    activeLists.value = res || []
  } catch (e) {
    console.error('加载购物清单失败', e)
  } finally {
    loading.value = false
  }
}

// 页面加载时获取数据
onMounted(() => {
  loadShoppingLists()
})

const createList = () => {
  uni.showModal({
    title: '新建购物清单',
    editable: true,
    placeholderText: '输入清单名称...',
    success: async (res) => {
      if (res.confirm && res.content) {
        try {
          const familyId = uni.getStorageSync('currentFamilyId') || 1
          await shoppingApi.createList({
            name: res.content,
            familyId: familyId
          })
          uni.showToast({ title: '创建成功', icon: 'success' })
          loadShoppingLists()
        } catch (e) {
          uni.showToast({ title: '创建失败', icon: 'none' })
        }
      }
    }
  })
}

const goInventory = () => {
  uni.showToast({ title: '库存管理开发中', icon: 'none' })
}

const goPriceTrack = () => {
  uni.navigateTo({
    url: '/pages/shopping/price'
  })
}

// 扫码录入
const scanCode = () => {
  uni.scanCode({
    onlyFromCamera: false,
    scanType: ['barCode', 'qrCode'],
    success: (res) => {
      handleScanResult(res.result)
    },
    fail: (err) => {
      console.error('扫码失败:', err)
      uni.showToast({ title: '扫码失败', icon: 'none' })
    }
  })
}

// 处理扫码结果
const handleScanResult = async (barcode) => {
  scanLoading.value = true
  showScanModal.value = true
  
  try {
    // 调用后端接口识别商品
    const res = await shoppingApi.scanProduct({ barcode })
    
    if (res && res.productName) {
      scanResult.value = {
        barcode,
        name: res.productName,
        brand: res.brand || '',
        price: res.price || 0,
        category: res.category || '其他',
        image: res.image || '',
        specs: res.specs || '',
        source: res.source || 'database'
      }
    } else {
      // 未识别，显示手动输入
      scanResult.value = {
        barcode,
        name: '',
        brand: '',
        price: 0,
        category: '其他',
        image: '',
        specs: '',
        source: 'manual'
      }
    }
  } catch (e) {
    console.error('识别商品失败:', e)
    // 默认显示手动输入
    scanResult.value = {
      barcode,
      name: '',
      brand: '',
      price: 0,
      category: '其他',
      image: '',
      specs: '',
      source: 'manual'
    }
  } finally {
    scanLoading.value = false
  }
}

// 关闭扫码弹窗
const closeScanModal = () => {
  showScanModal.value = false
  scanResult.value = null
  scanLoading.value = false
}

// 确认添加商品
const confirmAddProduct = async () => {
  if (!scanResult.value.name) {
    uni.showToast({ title: '请输入商品名称', icon: 'none' })
    return
  }
  
  // 如果选择了清单，添加到清单
  if (selectedListId.value) {
    try {
      await shoppingApi.addItem({
        listId: selectedListId.value,
        name: scanResult.value.name,
        quantity: 1,
        unit: '件',
        estimatedPrice: scanResult.value.price,
        barcode: scanResult.value.barcode,
        category: scanResult.value.category
      })
      uni.showToast({ title: '已添加到清单', icon: 'success' })
    } catch (e) {
      uni.showToast({ title: '添加失败', icon: 'none' })
    }
  } else {
    // 否则添加到库存
    try {
      const familyId = uni.getStorageSync('currentFamilyId') || 1
      await shoppingApi.addInventory({
        familyId,
        productName: scanResult.value.name,
        barcode: scanResult.value.barcode,
        category: scanResult.value.category,
        quantity: 1,
        unit: '件',
        expiryDate: null
      })
      uni.showToast({ title: '已添加到库存', icon: 'success' })
    } catch (e) {
      uni.showToast({ title: '添加失败', icon: 'none' })
    }
  }
  
  closeScanModal()
}

</script>

<style lang="scss" scoped>
.shopping-page {
  min-height: 100vh;
  background: #0f0f23;
  padding-bottom: 100px;
}

.shopping-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 50px 20px 20px;
  
  .header-title {
    font-size: 24px;
    font-weight: 700;
    color: #fff;
  }
  
  .scan-btn {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 10px 16px;
    background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
    border-radius: 20px;
    color: #fff;
    font-size: 14px;
    
    .icon {
      font-size: 18px;
    }
  }
}

.quick-actions {
  display: flex;
  gap: 12px;
  padding: 0 20px;
  margin-bottom: 20px;
  
  .action-card {
    flex: 1;
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 16px;
    background: rgba(255,255,255,0.05);
    border-radius: 16px;
    border: 1px solid rgba(255,255,255,0.08);
    
    .action-icon {
      width: 48px;
      height: 48px;
      border-radius: 14px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 24px;
    }
    
    .action-info {
      .action-name {
        display: block;
        font-size: 15px;
        font-weight: 600;
        color: #fff;
      }
      
      .action-desc {
        font-size: 12px;
        color: #64748b;
        margin-top: 4px;
      }
    }
  }
}

.section {
  margin-bottom: 20px;
  
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
    
    .section-more {
      font-size: 13px;
      color: #6366f1;
    }
  }
}

.list-card {
  margin: 0 20px 12px;
  padding: 18px;
  background: rgba(255,255,255,0.05);
  border-radius: 20px;
  border: 1px solid rgba(255,255,255,0.08);
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  
  .list-name {
    display: block;
    font-size: 16px;
    font-weight: 600;
    color: #fff;
  }
  
  .list-meta {
    font-size: 12px;
    color: #64748b;
    margin-top: 4px;
  }
  
  .progress-ring {
    width: 44px;
    height: 44px;
    border-radius: 50%;
    border: 3px solid rgba(99,102,241,0.3);
    border-top-color: #6366f1;
    display: flex;
    align-items: center;
    justify-content: center;
    
    .progress-text {
      font-size: 12px;
      font-weight: 600;
      color: #6366f1;
    }
  }
}

.list-items {
  padding: 12px;
  background: rgba(255,255,255,0.03);
  border-radius: 12px;
  margin-bottom: 15px;
  
  .list-item {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 8px 0;
    
    &.checked {
      opacity: 0.5;
    }
    
    .checkbox {
      width: 18px;
      height: 18px;
      border: 2px solid #64748b;
      border-radius: 50%;
      
      &.checked {
        background: #10b981;
        border-color: #10b981;
      }
    }
    
    .item-name {
      flex: 1;
      font-size: 14px;
      color: #e2e8f0;
      
      text-decoration: line-through;
      opacity: 0.5;
    }
    
    &:not(.checked) .item-name {
      text-decoration: none;
      opacity: 1;
    }
    
    .item-qty {
      font-size: 12px;
      color: #64748b;
    }
  }
  
  .more-items {
    text-align: center;
    padding: 8px;
    font-size: 12px;
    color: #64748b;
  }
}

.list-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  
  .assignee {
    font-size: 12px;
    color: #64748b;
  }
  
  .list-actions {
    display: flex;
    gap: 16px;
    
    .action {
      font-size: 13px;
      color: #64748b;
      
      &.primary {
        color: #6366f1;
        font-weight: 500;
      }
    }
  }
}

.expiry-list {
  padding: 0 20px;
}

.expiry-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px;
  background: rgba(255,255,255,0.05);
  border-radius: 12px;
  margin-bottom: 8px;
  
  .expiry-icon {
    font-size: 20px;
    
    &.urgent {
      animation: pulse 1.5s infinite;
    }
  }
  
  .expiry-info {
    flex: 1;
    
    .expiry-name {
      display: block;
      font-size: 14px;
      color: #fff;
    }
    
    .expiry-date {
      font-size: 12px;
      color: #f59e0b;
      margin-top: 2px;
      
      &.urgent {
        color: #ef4444;
      }
    }
  }
  
  .expiry-qty {
    font-size: 13px;
    color: #64748b;
  }
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

// 扫码弹窗样式
.scan-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 1000;
  display: flex;
  align-items: flex-end;
  justify-content: center;
  
  .scan-modal-mask {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0,0,0,0.6);
  }
  
  .scan-modal-content {
    position: relative;
    width: 100%;
    max-height: 80vh;
    background: #1a1a2e;
    border-radius: 24px 24px 0 0;
    padding: 20px;
    animation: slideUp 0.3s ease;
    overflow-y: auto;
  }
  
  @keyframes slideUp {
    from { transform: translateY(100%); }
    to { transform: translateY(0); }
  }
  
  .scan-modal-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    
    .scan-modal-title {
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
  
  .scan-loading {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 40px 0;
    
    .loading-spinner {
      width: 48px;
      height: 48px;
      border: 3px solid rgba(99,102,241,0.2);
      border-top-color: #6366f1;
      border-radius: 50%;
      animation: spin 1s linear infinite;
    }
    
    @keyframes spin {
      to { transform: rotate(360deg); }
    }
    
    .loading-text {
      margin-top: 16px;
      font-size: 14px;
      color: #64748b;
    }
  }
  
  .scan-form {
    .product-image-section {
      display: flex;
      justify-content: center;
      margin-bottom: 20px;
      
      .product-image {
        width: 120px;
        height: 120px;
        border-radius: 16px;
        overflow: hidden;
        background: rgba(255,255,255,0.05);
        
        image {
          width: 100%;
          height: 100%;
        }
      }
      
      .product-image-placeholder {
        width: 120px;
        height: 120px;
        border-radius: 16px;
        background: rgba(255,255,255,0.05);
        display: flex;
        align-items: center;
        justify-content: center;
        
        .placeholder-icon {
          font-size: 48px;
        }
      }
    }
    
    .form-item {
      margin-bottom: 16px;
      
      .form-label {
        display: block;
        font-size: 13px;
        color: #64748b;
        margin-bottom: 8px;
      }
      
      .form-value {
        padding: 12px 16px;
        background: rgba(255,255,255,0.05);
        border-radius: 12px;
        font-size: 14px;
        color: #fff;
        
        &.barcode {
          font-family: monospace;
          color: #94a3b8;
        }
      }
      
      .form-input {
        width: 100%;
        padding: 12px 16px;
        background: rgba(255,255,255,0.05);
        border: 1px solid rgba(255,255,255,0.1);
        border-radius: 12px;
        font-size: 14px;
        color: #fff;
        
        &:focus {
          border-color: #6366f1;
        }
      }
      
      .price-input-wrapper {
        display: flex;
        align-items: center;
        padding: 12px 16px;
        background: rgba(255,255,255,0.05);
        border: 1px solid rgba(255,255,255,0.1);
        border-radius: 12px;
        
        .currency {
          font-size: 14px;
          color: #64748b;
          margin-right: 8px;
        }
        
        .price-input {
          flex: 1;
          padding: 0;
          background: transparent;
          border: none;
        }
      }
      
      .form-picker {
        padding: 12px 16px;
        background: rgba(255,255,255,0.05);
        border: 1px solid rgba(255,255,255,0.1);
        border-radius: 12px;
        font-size: 14px;
        color: #fff;
      }
    }
    
    .source-tag {
      text-align: center;
      margin-bottom: 20px;
      
      .tag {
        display: inline-block;
        padding: 6px 12px;
        background: rgba(16,185,129,0.15);
        color: #10b981;
        font-size: 12px;
        border-radius: 20px;
      }
      
      &.manual .tag {
        background: rgba(245,158,11,0.15);
        color: #f59e0b;
      }
    }
    
    .scan-actions {
      display: flex;
      gap: 12px;
      
      .btn {
        flex: 1;
        padding: 14px 0;
        border-radius: 12px;
        font-size: 15px;
        font-weight: 500;
        text-align: center;
        
        &.btn-secondary {
          background: rgba(255,255,255,0.1);
          color: #fff;
        }
        
        &.btn-primary {
          background: linear-gradient(135deg, #6366f1, #8b5cf6);
          color: #fff;
        }
      }
    }
  }
}
</style>
