<template>
  <view class="shopping-detail-page">
    <!-- 头部 -->
    <view class="detail-header">
      <view class="header-back" @click="goBack">
        <text class="back-icon">‹</text>
      </view>
      <view class="header-content">
        <input
          v-model="listData.name"
          class="list-title-input"
          placeholder="输入清单名称"
          :disabled="isViewOnly"
        />
        <text class="list-meta" v-if="listData.createdAt">{{ formatDate(listData.createdAt) }}</text>
      </view>
      <view class="header-actions">
        <view class="action-btn share" @click="shareList">
          <text class="btn-icon">↗️</text>
        </view>
        <view class="action-btn more" @click="showMoreOptions">
          <text class="btn-icon">⋯</text>
        </view>
      </view>
    </view>

    <!-- 进度统计 -->
    <view class="progress-section">
      <view class="progress-card">
        <view class="progress-info">
          <view class="progress-circle" :style="{ '--progress': progressPercent + '%' }">
            <text class="progress-value">{{ progressPercent }}%</text>
          </view>
          <view class="progress-stats">
            <text class="stats-title">购物进度</text>
            <text class="stats-text">{{ checkedCount }}/{{ totalCount }} 已完成</text>
          </view>
        </view>
        <view class="budget-info" v-if="listData.estimatedAmount">
          <text class="budget-label">预计花费</text>
          <text class="budget-value">¥{{ listData.estimatedAmount }}</text>
        </view>
      </view>
    </view>

    <!-- 分类筛选 -->
    <view class="category-filter">
      <scroll-view class="filter-scroll" scroll-x>
        <view
          v-for="(cat, index) in categories"
          :key="index"
          class="filter-item"
          :class="{ active: currentCategory === index }"
          @click="switchCategory(index)"
        >
          <text>{{ cat.name }}</text>
          <text class="filter-count" v-if="cat.count > 0">{{ cat.count }}</text>
        </view>
      </scroll-view>
    </view>

    <!-- 购物项列表 -->
    <scroll-view class="items-list" scroll-y>
      <view
        v-for="(item, index) in filteredItems"
        :key="item.id || index"
        class="shopping-item"
        :class="{ checked: item.checked }"
        @click="toggleItem(item)"
      >
        <view class="item-checkbox" :class="{ checked: item.checked }" @click.stop="toggleItem(item)">
          <text v-if="item.checked" class="check-icon">✓</text>
        </view>
        
        <view class="item-content">
          <view class="item-main">
            <text class="item-name" :class="{ checked: item.checked }">{{ item.name }}</text>
            <view class="item-tags">
              <text class="item-category">{{ item.category || '其他' }}</text>
            </view>
          </view>
          
          <view class="item-sub">
            <text class="item-quantity">{{ item.quantity }}{{ item.unit }}</text>
            <text class="item-price" v-if="item.estimatedPrice">¥{{ item.estimatedPrice }}</text>
          </view>
        </view>
        
        <view class="item-actions" @click.stop>
          <view class="action-btn" @click="editItem(item, index)">
            <text>✏️</text>
          </view>
          <view class="action-btn" @click="deleteItem(index)">
            <text>🗑️</text>
          </view>
        </view>
      </view>

      <!-- 添加项 -->
      <view class="add-item-row" @click="showAddItemModal">
        <text class="add-icon">+</text>
        <text class="add-text">添加物品</text>
      </view>

      <!-- 空状态 -->
      <view class="empty-state" v-if="filteredItems.length === 0 && listData.items?.length === 0">
        <text class="empty-icon">🛒</text>
        <text class="empty-text">清单还是空的</text>
        <text class="empty-tip">点击上方"添加物品"开始创建</text>
      </view>
    </scroll-view>

    <!-- 底部操作栏 -->
    <view class="bottom-bar">
      <view class="bar-left">
        <view class="bar-btn" @click="showAddItemModal">
          <text class="btn-icon">➕</text>
          <text class="btn-text">添加</text>
        </view>
        <view class="bar-btn" @click="scanToAdd">
          <text class="btn-icon">📷</text>
          <text class="btn-text">扫码</text>
        </view>
      </view>
      <view class="bar-right">
        <view class="save-btn" @click="saveList">
          <text>{{ isNew ? '创建清单' : '保存修改' }}</text>
        </view>
      </view>
    </view>

    <!-- 添加/编辑物品弹窗 -->
    <view v-if="showItemModal" class="item-modal">
      <view class="modal-mask" @click="closeItemModal"></view>
      <view class="modal-content">
        <view class="modal-header">
          <text class="modal-title">{{ editingItemIndex >= 0 ? '编辑物品' : '添加物品' }}</text>
          <view class="close-btn" @click="closeItemModal">×</view>
        </view>

        <view class="form-items">
          <view class="form-item">
            <text class="form-label">物品名称 *</text>
            <input
              v-model="itemForm.name"
              class="form-input"
              placeholder="输入物品名称"
              focus
            />
          </view>

          <view class="form-row">
            <view class="form-item half">
              <text class="form-label">数量</text>
              <input
                v-model="itemForm.quantity"
                class="form-input"
                type="number"
                placeholder="1"
              />
            </view>
            <view class="form-item half">
              <text class="form-label">单位</text>
              <picker mode="selector" :range="units" :value="unitIndex" @change="onUnitChange">
                <view class="form-picker">{{ itemForm.unit }}</view>
              </picker>
            </view>
          </view>

          <view class="form-item">
            <text class="form-label">分类</text>
            <picker mode="selector" :range="categoryOptions" :value="categoryIndex" @change="onCategoryChange">
              <view class="form-picker">{{ itemForm.category }}</view>
            </picker>
          </view>

          <view class="form-item">
            <text class="form-label">预估价格</text>
            <view class="price-input-wrapper">
              <text class="currency">¥</text>
              <input
                v-model="itemForm.estimatedPrice"
                class="form-input price-input"
                type="digit"
                placeholder="0.00"
              />
            </view>
          </view>

          <view class="form-item">
            <text class="form-label">备注</text>
            <input
              v-model="itemForm.remark"
              class="form-input"
              placeholder="可选，如品牌、规格等"
            />
          </view>
        </view>

        <view class="modal-actions">
          <view class="btn btn-secondary" @click="closeItemModal">取消</view>
          <view class="btn btn-primary" @click="confirmAddItem">确定</view>
        </view>
      </view>
    </view>

    <!-- 更多选项弹窗 -->
    <view v-if="showMoreModal" class="more-modal">
      <view class="modal-mask" @click="showMoreModal = false"></view>
      <view class="modal-content">
        <view class="option-list">
          <view class="option-item" @click="clearCompleted">
            <text class="option-icon">✓</text>
            <text class="option-text">清除已完成</text>
          </view>
          <view class="option-item" @click="sortItems">
            <text class="option-icon">⇅</text>
            <text class="option-text">按分类排序</text>
          </view>
          <view class="option-item danger" @click="deleteList">
            <text class="option-icon">🗑️</text>
            <text class="option-text">删除清单</text>
          </view>
        </view>
        <view class="cancel-btn" @click="showMoreModal = false">取消</view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { shoppingApi } from '../../api/index.js'

// 响应式数据
const listData = ref({
  name: '',
  items: [],
  estimatedAmount: 0,
  createdAt: null
})

const isNew = ref(true)
const isViewOnly = ref(false)
const showItemModal = ref(false)
const showMoreModal = ref(false)
const editingItemIndex = ref(-1)
const currentCategory = ref(0)

// 表单数据
const itemForm = ref({
  name: '',
  quantity: 1,
  unit: '个',
  category: '其他',
  estimatedPrice: '',
  remark: ''
})

// 选项
const units = ['个', '件', '斤', '公斤', '克', '升', '毫升', '包', '瓶', '盒', '袋', '箱', '罐']
const categoryOptions = ['其他', '生鲜', '零食', '饮料', '调料', '日用品', '洗护', '食品']

// 计算属性
const totalCount = computed(() => listData.value.items?.length || 0)
const checkedCount = computed(() => listData.value.items?.filter(i => i.checked).length || 0)
const progressPercent = computed(() => {
  if (totalCount.value === 0) return 0
  return Math.round((checkedCount.value / totalCount.value) * 100)
})

// 分类统计
const categories = computed(() => {
  const cats = [
    { name: '全部', count: listData.value.items?.length || 0 },
    ...categoryOptions.map(cat => ({
      name: cat,
      count: listData.value.items?.filter(i => i.category === cat).length || 0
    }))
  ]
  return cats.filter(c => c.count > 0 || c.name === '全部')
})

const filteredItems = computed(() => {
  if (currentCategory.value === 0) return listData.value.items || []
  const catName = categories.value[currentCategory.value]?.name
  return listData.value.items?.filter(i => i.category === catName) || []
})

const unitIndex = computed(() => units.indexOf(itemForm.value.unit) || 0)
const categoryIndex = computed(() => categoryOptions.indexOf(itemForm.value.category) || 0)

// 页面加载
onMounted(() => {
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const options = currentPage.options || {}
  
  // 加载模式
  if (options.mode === 'ai' || options.mode === 'template') {
    // AI生成或模板创建
    isNew.value = true
    if (options.data) {
      try {
        const data = JSON.parse(decodeURIComponent(options.data))
        listData.value = { ...data, items: data.items || [] }
      } catch (e) {
        console.error('解析数据失败', e)
      }
    }
  } else if (options.mode === 'create') {
    // 手动创建
    isNew.value = true
    listData.value.name = '新购物清单'
    listData.value.createdAt = new Date().toISOString()
  } else if (options.id) {
    // 查看/编辑已有清单
    isNew.value = false
    loadListDetail(options.id)
  }
})

// 加载清单详情
const loadListDetail = async (id) => {
  try {
    uni.showLoading({ title: '加载中...' })
    const res = await shoppingApi.getListDetail(id)
    listData.value = res
    uni.hideLoading()
  } catch (e) {
    uni.hideLoading()
    console.error('加载清单详情失败', e)
    uni.showToast({ title: '加载失败', icon: 'none' })
  }
}

// 切换分类
const switchCategory = (index) => {
  currentCategory.value = index
}

// 切换物品勾选状态
const toggleItem = (item) => {
  item.checked = !item.checked
  updateProgress()
}

// 更新进度
const updateProgress = () => {
  const items = listData.value.items || []
  const total = items.length
  const checked = items.filter(i => i.checked).length
  // 这里可以更新到后端
}

// 显示添加物品弹窗
const showAddItemModal = () => {
  editingItemIndex.value = -1
  itemForm.value = {
    name: '',
    quantity: 1,
    unit: '个',
    category: '其他',
    estimatedPrice: '',
    remark: ''
  }
  showItemModal.value = true
}

// 编辑物品
const editItem = (item, index) => {
  editingItemIndex.value = index
  itemForm.value = { ...item }
  showItemModal.value = true
}

// 关闭弹窗
const closeItemModal = () => {
  showItemModal.value = false
  editingItemIndex.value = -1
}

// 单位选择
const onUnitChange = (e) => {
  itemForm.value.unit = units[e.detail.value]
}

// 分类选择
const onCategoryChange = (e) => {
  itemForm.value.category = categoryOptions[e.detail.value]
}

// 确认添加/编辑物品
const confirmAddItem = () => {
  if (!itemForm.value.name.trim()) {
    uni.showToast({ title: '请输入物品名称', icon: 'none' })
    return
  }
  
  const item = {
    ...itemForm.value,
    id: Date.now() + Math.random(),
    checked: false
  }
  
  if (editingItemIndex.value >= 0) {
    // 编辑模式
    listData.value.items[editingItemIndex.value] = item
  } else {
    // 添加模式
    if (!listData.value.items) listData.value.items = []
    listData.value.items.push(item)
  }
  
  // 更新预算
  updateBudget()
  
  closeItemModal()
  uni.showToast({ title: editingItemIndex.value >= 0 ? '修改成功' : '添加成功', icon: 'success' })
}

// 删除物品
const deleteItem = (index) => {
  uni.showModal({
    title: '确认删除',
    content: '确定要删除这个物品吗？',
    success: (res) => {
      if (res.confirm) {
        listData.value.items.splice(index, 1)
        updateBudget()
        uni.showToast({ title: '已删除', icon: 'success' })
      }
    }
  })
}

// 更新预算
const updateBudget = () => {
  const items = listData.value.items || []
  listData.value.estimatedAmount = items.reduce((sum, item) => {
    return sum + (parseFloat(item.estimatedPrice) || 0) * (parseFloat(item.quantity) || 1)
  }, 0)
}

// 扫码添加
const scanToAdd = () => {
  uni.scanCode({
    onlyFromCamera: false,
    scanType: ['barCode', 'qrCode'],
    success: async (res) => {
      try {
        uni.showLoading({ title: '识别中...' })
        const productRes = await shoppingApi.scanProduct({ barcode: res.result })
        
        if (productRes && productRes.productName) {
          itemForm.value = {
            name: productRes.productName,
            quantity: 1,
            unit: '件',
            category: productRes.category || '其他',
            estimatedPrice: productRes.price || '',
            remark: productRes.brand || ''
          }
          showItemModal.value = true
        } else {
          uni.showToast({ title: '未找到商品信息', icon: 'none' })
        }
        uni.hideLoading()
      } catch (e) {
        uni.hideLoading()
        uni.showToast({ title: '识别失败', icon: 'none' })
      }
    }
  })
}

// 保存清单
const saveList = async () => {
  if (!listData.value.name.trim()) {
    uni.showToast({ title: '请输入清单名称', icon: 'none' })
    return
  }
  
  if (!listData.value.items || listData.value.items.length === 0) {
    uni.showToast({ title: '请至少添加一个物品', icon: 'none' })
    return
  }
  
  try {
    uni.showLoading({ title: '保存中...' })
    
    const familyId = uni.getStorageSync('currentFamilyId') || 1
    const data = {
      ...listData.value,
      familyId
    }
    
    if (isNew.value) {
      // 创建新清单
      // await shoppingApi.createList(data)
      console.log('创建清单:', data)
    } else {
      // 更新清单
      // await shoppingApi.updateList(data)
    }
    
    uni.hideLoading()
    uni.showToast({ title: '保存成功', icon: 'success' })
    
    setTimeout(() => {
      uni.navigateBack()
    }, 1000)
  } catch (e) {
    uni.hideLoading()
    uni.showToast({ title: '保存失败', icon: 'none' })
  }
}

// 分享清单
const shareList = () => {
  uni.showShareMenu({
    withShareTicket: true,
    menus: ['shareAppMessage', 'shareTimeline']
  })
}

// 显示更多选项
const showMoreOptions = () => {
  showMoreModal.value = true
}

// 清除已完成
const clearCompleted = () => {
  showMoreModal.value = false
  uni.showModal({
    title: '确认清除',
    content: '确定要清除所有已完成的物品吗？',
    success: (res) => {
      if (res.confirm) {
        listData.value.items = listData.value.items.filter(i => !i.checked)
        updateBudget()
        uni.showToast({ title: '已清除', icon: 'success' })
      }
    }
  })
}

// 按分类排序
const sortItems = () => {
  showMoreModal.value = false
  const categoryOrder = categoryOptions
  listData.value.items.sort((a, b) => {
    const indexA = categoryOrder.indexOf(a.category)
    const indexB = categoryOrder.indexOf(b.category)
    return indexA - indexB
  })
  uni.showToast({ title: '排序完成', icon: 'success' })
}

// 删除清单
const deleteList = () => {
  showMoreModal.value = false
  uni.showModal({
    title: '确认删除',
    content: '确定要删除这个清单吗？此操作不可恢复。',
    confirmColor: '#ef4444',
    success: async (res) => {
      if (res.confirm) {
        try {
          // await shoppingApi.deleteList(listData.value.id)
          uni.showToast({ title: '已删除', icon: 'success' })
          setTimeout(() => {
            uni.navigateBack()
          }, 1000)
        } catch (e) {
          uni.showToast({ title: '删除失败', icon: 'none' })
        }
      }
    }
  })
}

// 返回
const goBack = () => {
  uni.navigateBack()
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now.getDate() - date.getDate()
  
  if (diff === 0) return '今天'
  if (diff === 1) return '昨天'
  return `${date.getMonth() + 1}月${date.getDate()}日`
}
</script>

<style lang="scss" scoped>
.shopping-detail-page {
  min-height: 100vh;
  background: #f8f9fc;
  display: flex;
  flex-direction: column;
}

// 头部
.detail-header {
  display: flex;
  align-items: center;
  padding: 80rpx 32rpx 30rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  
  .header-back {
    width: 64rpx;
    height: 64rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 20rpx;
    
    .back-icon {
      font-size: 48rpx;
      color: #fff;
      font-weight: 300;
    }
  }
  
  .header-content {
    flex: 1;
    
    .list-title-input {
      font-size: 36rpx;
      font-weight: 600;
      color: #fff;
      background: transparent;
      border: none;
      padding: 0;
      
      &::placeholder {
        color: rgba(255,255,255,0.6);
      }
    }
    
    .list-meta {
      font-size: 24rpx;
      color: rgba(255,255,255,0.7);
      margin-top: 8rpx;
    }
  }
  
  .header-actions {
    display: flex;
    gap: 16rpx;
    
    .action-btn {
      width: 64rpx;
      height: 64rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      background: rgba(255,255,255,0.15);
      border-radius: 50%;
      
      .btn-icon {
        font-size: 32rpx;
      }
    }
  }
}

// 进度统计
.progress-section {
  padding: 24rpx 32rpx;
  margin-top: -20rpx;
  
  .progress-card {
    display: flex;
    justify-content: space-between;
    align-items: center;
    background: #fff;
    border-radius: 24rpx;
    padding: 28rpx;
    box-shadow: 0 8rpx 32rpx rgba(0,0,0,0.08);
    
    .progress-info {
      display: flex;
      align-items: center;
      gap: 24rpx;
      
      .progress-circle {
        width: 100rpx;
        height: 100rpx;
        border-radius: 50%;
        background: conic-gradient(
          #667eea var(--progress, 0%),
          #e2e8f0 var(--progress, 0%)
        );
        display: flex;
        align-items: center;
        justify-content: center;
        position: relative;
        
        &::before {
          content: '';
          width: 80rpx;
          height: 80rpx;
          background: #fff;
          border-radius: 50%;
          position: absolute;
        }
        
        .progress-value {
          font-size: 28rpx;
          font-weight: 600;
          color: #667eea;
          position: relative;
          z-index: 1;
        }
      }
      
      .progress-stats {
        .stats-title {
          display: block;
          font-size: 28rpx;
          font-weight: 600;
          color: #2d3748;
        }
        
        .stats-text {
          font-size: 24rpx;
          color: #718096;
          margin-top: 4rpx;
        }
      }
    }
    
    .budget-info {
      text-align: right;
      
      .budget-label {
        display: block;
        font-size: 24rpx;
        color: #718096;
      }
      
      .budget-value {
        font-size: 40rpx;
        font-weight: 700;
        color: #667eea;
      }
    }
  }
}

// 分类筛选
.category-filter {
  padding: 20rpx 32rpx;
  background: #fff;
  border-bottom: 1rpx solid #e2e8f0;
  
  .filter-scroll {
    white-space: nowrap;
    
    .filter-item {
      display: inline-flex;
      align-items: center;
      gap: 8rpx;
      padding: 12rpx 24rpx;
      margin-right: 16rpx;
      background: #f0f4f8;
      border-radius: 28rpx;
      font-size: 26rpx;
      color: #4a5568;
      
      &.active {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        color: #fff;
      }
      
      .filter-count {
        font-size: 22rpx;
        padding: 2rpx 10rpx;
        background: rgba(102, 126, 234, 0.2);
        border-radius: 20rpx;
      }
    }
  }
}

// 物品列表
.items-list {
  flex: 1;
  padding: 20rpx 32rpx;
  
  .shopping-item {
    display: flex;
    align-items: center;
    gap: 20rpx;
    padding: 24rpx;
    background: #fff;
    border-radius: 20rpx;
    margin-bottom: 16rpx;
    box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.06);
    
    &.checked {
      opacity: 0.7;
      background: #f8f9fc;
    }
    
    .item-checkbox {
      width: 44rpx;
      height: 44rpx;
      border: 3rpx solid #cbd5e0;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
      
      &.checked {
        background: #48bb78;
        border-color: #48bb78;
      }
      
      .check-icon {
        font-size: 24rpx;
        color: #fff;
      }
    }
    
    .item-content {
      flex: 1;
      min-width: 0;
      
      .item-main {
        display: flex;
        align-items: center;
        gap: 12rpx;
        margin-bottom: 8rpx;
        
        .item-name {
          font-size: 30rpx;
          font-weight: 500;
          color: #2d3748;
          
          &.checked {
            text-decoration: line-through;
            color: #a0aec0;
          }
        }
        
        .item-tags {
          .item-category {
            font-size: 20rpx;
            padding: 4rpx 12rpx;
            background: #e0e7ff;
            color: #667eea;
            border-radius: 8rpx;
          }
        }
      }
      
      .item-sub {
        display: flex;
        gap: 20rpx;
        
        .item-quantity {
          font-size: 24rpx;
          color: #718096;
        }
        
        .item-price {
          font-size: 24rpx;
          color: #667eea;
          font-weight: 500;
        }
      }
    }
    
    .item-actions {
      display: flex;
      gap: 20rpx;
      
      .action-btn {
        padding: 8rpx;
        font-size: 28rpx;
      }
    }
  }
  
  .add-item-row {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 12rpx;
    padding: 32rpx;
    background: #fff;
    border-radius: 20rpx;
    border: 2rpx dashed #cbd5e0;
    margin-bottom: 16rpx;
    
    .add-icon {
      font-size: 36rpx;
      color: #667eea;
    }
    
    .add-text {
      font-size: 28rpx;
      color: #667eea;
    }
  }
  
  .empty-state {
    text-align: center;
    padding: 100rpx 40rpx;
    
    .empty-icon {
      font-size: 80rpx;
      margin-bottom: 24rpx;
    }
    
    .empty-text {
      display: block;
      font-size: 32rpx;
      color: #4a5568;
      margin-bottom: 12rpx;
    }
    
    .empty-tip {
      font-size: 26rpx;
      color: #a0aec0;
    }
  }
}

// 底部操作栏
.bottom-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 32rpx 40rpx;
  background: #fff;
  border-top: 1rpx solid #e2e8f0;
  
  .bar-left {
    display: flex;
    gap: 40rpx;
    
    .bar-btn {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 6rpx;
      
      .btn-icon {
        font-size: 36rpx;
      }
      
      .btn-text {
        font-size: 22rpx;
        color: #4a5568;
      }
    }
  }
  
  .bar-right {
    .save-btn {
      padding: 20rpx 48rpx;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      border-radius: 40rpx;
      font-size: 28rpx;
      color: #fff;
      font-weight: 500;
    }
  }
}

// 添加/编辑弹窗
.item-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 1000;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  
  .modal-mask {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0,0,0,0.5);
  }
  
  .modal-content {
    position: relative;
    background: #fff;
    border-radius: 32rpx 32rpx 0 0;
    padding: 32rpx;
    animation: slideUp 0.3s ease;
    max-height: 80vh;
    overflow-y: auto;
    
    .modal-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 32rpx;
      
      .modal-title {
        font-size: 34rpx;
        font-weight: 600;
        color: #2d3748;
      }
      
      .close-btn {
        width: 56rpx;
        height: 56rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 40rpx;
        color: #718096;
      }
    }
    
    .form-items {
      .form-item {
        margin-bottom: 24rpx;
        
        &.half {
          flex: 1;
        }
        
        .form-label {
          display: block;
          font-size: 26rpx;
          color: #4a5568;
          margin-bottom: 12rpx;
        }
        
        .form-input {
          width: 100%;
          height: 80rpx;
          background: #f8f9fc;
          border-radius: 16rpx;
          padding: 0 24rpx;
          font-size: 28rpx;
          color: #2d3748;
        }
        
        .form-picker {
          height: 80rpx;
          line-height: 80rpx;
          background: #f8f9fc;
          border-radius: 16rpx;
          padding: 0 24rpx;
          font-size: 28rpx;
          color: #2d3748;
        }
      }
      
      .form-row {
        display: flex;
        gap: 20rpx;
      }
      
      .price-input-wrapper {
        display: flex;
        align-items: center;
        gap: 12rpx;
        padding: 0 24rpx;
        background: #f8f9fc;
        border-radius: 16rpx;
        
        .currency {
          font-size: 28rpx;
          color: #718096;
        }
        
        .price-input {
          flex: 1;
          background: transparent;
          padding: 0;
        }
      }
    }
    
    .modal-actions {
      display: flex;
      gap: 20rpx;
      margin-top: 32rpx;
      
      .btn {
        flex: 1;
        padding: 24rpx 0;
        text-align: center;
        border-radius: 16rpx;
        font-size: 30rpx;
        
        &.btn-secondary {
          background: #f0f4f8;
          color: #4a5568;
        }
        
        &.btn-primary {
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          color: #fff;
        }
      }
    }
  }
}

// 更多选项弹窗
.more-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 1000;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  
  .modal-mask {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0,0,0,0.5);
  }
  
  .modal-content {
    position: relative;
    padding: 24rpx;
    
    .option-list {
      background: #fff;
      border-radius: 24rpx;
      overflow: hidden;
      margin-bottom: 24rpx;
      
      .option-item {
        display: flex;
        align-items: center;
        gap: 24rpx;
        padding: 32rpx;
        border-bottom: 1rpx solid #f0f4f8;
        
        &:last-child {
          border-bottom: none;
        }
        
        &.danger {
          .option-text {
            color: #ef4444;
          }
        }
        
        .option-icon {
          font-size: 36rpx;
        }
        
        .option-text {
          font-size: 30rpx;
          color: #2d3748;
        }
      }
    }
    
    .cancel-btn {
      padding: 28rpx;
      background: #fff;
      border-radius: 24rpx;
      text-align: center;
      font-size: 30rpx;
      color: #4a5568;
    }
  }
}

@keyframes slideUp {
  from { transform: translateY(100%); }
  to { transform: translateY(0); }
}
</style>
