<template>
  <view class="wish-page">
    <!-- 顶部背景 -->
    <view class="header-bg"></view>
    
    <!-- 顶部标题栏 -->
    <view class="header-bar">
      <view class="header-left">
        <text class="title">心愿墙</text>
        <text class="subtitle">记录我们的愿望</text>
      </view>
    </view>
    
    <!-- Tab 状态筛选栏 -->
    <view class="tab-bar">
      <view 
        class="tab-item"
        :class="{ active: currentTab === 0 }"
        @click="switchTab(0)"
      >
        <text class="tab-text">待实现</text>
        <view v-if="currentTab === 0" class="tab-indicator"></view>
      </view>
      <view 
        class="tab-item"
        :class="{ active: currentTab === 1 }"
        @click="switchTab(1)"
      >
        <text class="tab-text">已实现</text>
        <view v-if="currentTab === 1" class="tab-indicator"></view>
      </view>
    </view>
    
    <!-- 心愿列表 -->
    <scroll-view scroll-y class="wish-list" refresher-enabled @refresherrefresh="onRefresh">
      <!-- 加载中 -->
      <view v-if="loading" class="loading-state">
        <text class="loading-text">加载中...</text>
      </view>
      
      <!-- 空状态 -->
      <view v-else-if="wishes.length === 0" class="empty-state">
        <view class="empty-icon-wrapper">
          <text class="empty-icon">💝</text>
        </view>
        <text class="empty-text">还没有心愿</text>
        <text class="empty-subtext">点击右下角添加第一个心愿</text>
      </view>
      
      <!-- 心愿卡片列表 -->
      <view v-else class="wish-cards">
        <view 
          v-for="(wish, index) in wishes" 
          :key="wish.id"
          class="wish-card"
          :class="{ completed: wish.status === 2 }"
          :style="{ animationDelay: index * 0.1 + 's' }"
        >
          <!-- 卡片头部 -->
          <view class="card-header">
            <view class="wish-type-tag" :class="wish.type || 'other'">
              <text>{{ getTypeName(wish.type) }}</text>
            </view>
            <view class="wish-status" :class="'status-' + wish.status">
              <text>{{ getStatusText(wish.status) }}</text>
            </view>
          </view>
          
          <!-- 标题 -->
          <text class="wish-title">{{ wish.title }}</text>
          
          <!-- 描述 -->
          <text v-if="wish.description" class="wish-desc">{{ wish.description }}</text>
          
          <!-- 预算 -->
          <view v-if="wish.budgetMin || wish.budgetMax" class="wish-budget">
            <text class="budget-icon">💰</text>
            <text class="budget-text">{{ formatBudget(wish.budgetMin, wish.budgetMax) }}</text>
          </view>
          
          <!-- 进度条 -->
          <view v-if="wish.status === 1" class="progress-section">
            <view class="progress-bar">
              <view class="progress-fill" :style="{ width: (wish.progress || 0) + '%' }"></view>
            </view>
            <text class="progress-text">{{ wish.progress || 0 }}%</text>
          </view>
          
          <!-- 操作按钮 -->
          <view class="card-actions">
            <view v-if="wish.status === 0" class="action-btn claim" @click="claimWish(wish)">
              <text>认领</text>
            </view>
            <view v-if="wish.status === 1" class="action-btn complete" @click="completeWish(wish)">
              <text>完成</text>
            </view>
            <view class="action-btn delete" @click="deleteWish(wish)">
              <text>删除</text>
            </view>
          </view>
        </view>
      </view>
    </scroll-view>
    
    <!-- 添加按钮 -->
    <view class="add-btn" @click="openAddModal">
      <text class="add-icon">+</text>
    </view>
    
    <!-- 添加心愿弹窗 -->
    <view v-if="showAddModal" class="modal-overlay" @click="closeAddModal">
      <view class="modal-content" @click.stop>
        <!-- 弹窗头部 -->
        <view class="modal-header">
          <text class="modal-title">许下心愿</text>
          <text class="modal-close" @click="closeAddModal">✕</text>
        </view>
        
        <!-- 弹窗内容 -->
        <view class="modal-body">
          <!-- 心愿标题 -->
          <view class="form-item">
            <text class="form-label">心愿标题 *</text>
            <input 
              class="form-input" 
              v-model="formData.title"
              placeholder="写下你的心愿"
              maxlength="50"
            />
          </view>
          
          <!-- 心愿描述 -->
          <view class="form-item">
            <text class="form-label">详细描述</text>
            <textarea 
              class="form-textarea" 
              v-model="formData.description"
              placeholder="描述一下这个心愿..."
              maxlength="200"
            />
          </view>
          
          <!-- 心愿类型 -->
          <view class="form-item">
            <text class="form-label">愿望类型</text>
            <view class="type-list">
              <view 
                v-for="type in wishTypes" 
                :key="type.value"
                class="type-item"
                :class="{ active: formData.type === type.value }"
                @click="formData.type = type.value"
              >
                <text class="type-icon">{{ type.icon }}</text>
                <text class="type-name">{{ type.label }}</text>
              </view>
            </view>
          </view>
          
          <!-- 预算 -->
          <view class="form-item">
            <text class="form-label">预算范围</text>
            <view class="budget-row">
              <input 
                class="budget-input" 
                type="number"
                v-model="formData.budgetMin"
                placeholder="最低"
              />
              <text class="budget-separator">-</text>
              <input 
                class="budget-input" 
                type="number"
                v-model="formData.budgetMax"
                placeholder="最高"
              />
              <text class="budget-unit">元</text>
            </view>
          </view>
          
          <!-- 期望日期 -->
          <view class="form-item">
            <text class="form-label">期望实现日期</text>
            <picker mode="date" :value="formData.expectDate" @change="onDateChange">
              <view class="date-picker">
                <text :class="{ placeholder: !formData.expectDate }">{{ formData.expectDate || '选择日期' }}</text>
              </view>
            </picker>
          </view>
        </view>
        
        <!-- 弹窗底部 -->
        <view class="modal-footer">
          <view class="cancel-btn" @click="closeAddModal">
            <text>取消</text>
          </view>
          <view class="submit-btn" @click="createWish">
            <text>许愿</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'

// 数据
const loading = ref(false)
const wishes = ref([])
const showAddModal = ref(false)
const currentTab = ref(0) // 0-待实现, 1-已实现

// 表单数据
const formData = ref({
  title: '',
  description: '',
  type: 'gift',
  budgetMin: '',
  budgetMax: '',
  expectDate: ''
})

// 愿望类型
const wishTypes = [
  { value: 'gift', label: '礼物', icon: '🎁' },
  { value: 'travel', label: '旅行', icon: '✈️' },
  { value: 'dinner', label: '约会', icon: '🍽️' },
  { value: 'electronic', label: '数码', icon: '📱' },
  { value: 'other', label: '其他', icon: '💝' }
]

// 获取类型名称
const getTypeName = (type) => {
  const found = wishTypes.find(t => t.value === type)
  return found ? found.label : '其他'
}

// 获取状态文本
const getStatusText = (status) => {
  const map = { 0: '待认领', 1: '进行中', 2: '已完成' }
  return map[status] || '未知'
}

// 格式化预算
const formatBudget = (min, max) => {
  if (min && max) return `${min}-${max}元`
  if (min) return `${min}元起`
  if (max) return `最高${max}元`
  return '面议'
}

// 日期选择
const onDateChange = (e) => {
  formData.value.expectDate = e.detail.value
}

// 打开添加弹窗
const openAddModal = () => {
  console.log('打开添加弹窗')
  // 重置表单
  formData.value = {
    title: '',
    description: '',
    type: 'gift',
    budgetMin: '',
    budgetMax: '',
    expectDate: ''
  }
  showAddModal.value = true
}

// 关闭添加弹窗
const closeAddModal = () => {
  console.log('关闭添加弹窗')
  showAddModal.value = false
}

// 创建心愿
const createWish = async () => {
  console.log('创建心愿', formData.value)
  
  if (!formData.value.title.trim()) {
    uni.showToast({ title: '请输入心愿标题', icon: 'none' })
    return
  }
  
  try {
    uni.showLoading({ title: '创建中...' })
    
    const familyId = uni.getStorageSync('currentFamilyId') || 1
    const data = {
      title: formData.value.title,
      description: formData.value.description,
      type: formData.value.type,
      familyId: familyId
    }
    
    // 添加预算
    if (formData.value.budgetMin) {
      data.budgetMin = parseFloat(formData.value.budgetMin)
    }
    if (formData.value.budgetMax) {
      data.budgetMax = parseFloat(formData.value.budgetMax)
    }
    
    // 添加日期
    if (formData.value.expectDate) {
      data.expectDate = formData.value.expectDate
    }
    
    console.log('发送数据:', data)
    
    // 调用API
    const res = await uni.request({
      url: 'https://qioba.cn:8443/api/wish/create',
      method: 'POST',
      header: {
        'Content-Type': 'application/json'
      },
      data: data
    })
    
    console.log('API返回:', res)
    
    uni.hideLoading()
    
    if (res.data && res.data.code === 200) {
      uni.showToast({ title: '许愿成功', icon: 'success' })
      closeAddModal()
      loadWishes()
    } else {
      const msg = res.data?.message || '创建失败'
      uni.showToast({ title: msg, icon: 'none' })
      console.error('创建失败:', res.data)
    }
  } catch (e) {
    uni.hideLoading()
    console.error('创建失败:', e)
    uni.showToast({ title: '创建失败: ' + (e.message || '网络错误'), icon: 'none' })
  }
}

// 认领心愿
const claimWish = async (wish) => {
  try {
    const res = await uni.request({
      url: `https://qioba.cn:8443/api/wish/claim/${wish.id}`,
      method: 'POST'
    })
    
    if (res.data?.code === 200) {
      uni.showToast({ title: '认领成功', icon: 'success' })
      loadWishes()
    } else {
      uni.showToast({ title: res.data?.message || '认领失败', icon: 'none' })
    }
  } catch (e) {
    uni.showToast({ title: '认领失败', icon: 'none' })
  }
}

// 完成心愿
const completeWish = async (wish) => {
  try {
    const res = await uni.request({
      url: `https://qioba.cn:8443/api/wish/complete/${wish.id}`,
      method: 'POST'
    })
    
    if (res.data?.code === 200) {
      uni.showToast({ title: '太棒了！', icon: 'success' })
      loadWishes()
    } else {
      uni.showToast({ title: res.data?.message || '操作失败', icon: 'none' })
    }
  } catch (e) {
    uni.showToast({ title: '操作失败', icon: 'none' })
  }
}

// 删除心愿
const deleteWish = (wish) => {
  uni.showModal({
    title: '确认删除',
    content: '确定要删除这个心愿吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          const result = await uni.request({
            url: `https://qioba.cn:8443/api/wish/${wish.id}`,
            method: 'DELETE'
          })
          
          if (result.data?.code === 200) {
            uni.showToast({ title: '删除成功', icon: 'success' })
            loadWishes()
          } else {
            uni.showToast({ title: result.data?.message || '删除失败', icon: 'none' })
          }
        } catch (e) {
          uni.showToast({ title: '删除失败', icon: 'none' })
        }
      }
    }
  })
}

// 加载心愿列表
const loadWishes = async () => {
  loading.value = true
  console.log('加载心愿列表, tab:', currentTab.value)
  
  try {
    const familyId = uni.getStorageSync('currentFamilyId') || 1
    // 构建URL，根据当前Tab传入status参数
    let url = `https://qioba.cn:8443/api/wish/list?familyId=${familyId}`
    if (currentTab.value !== null) {
      url += `&status=${currentTab.value}`
    }
    
    const res = await uni.request({
      url: url,
      method: 'GET'
    })
    
    console.log('API返回:', res)
    
    if (res.data?.code === 200) {
      wishes.value = res.data.data || []
      console.log('心愿列表:', wishes.value)
    } else {
      wishes.value = []
    }
  } catch (e) {
    console.error('加载失败:', e)
    wishes.value = []
  } finally {
    loading.value = false
  }
}

// 切换Tab
const switchTab = (tab) => {
  if (currentTab.value === tab) return
  currentTab.value = tab
  loadWishes()
}

// 下拉刷新
const onRefresh = async () => {
  await loadWishes()
  uni.showToast({ title: '刷新成功', icon: 'success' })
}

// 页面加载
onMounted(() => {
  console.log('页面加载')
  loadWishes()
})
</script>

<style lang="scss" scoped>
.wish-page {
  min-height: 100vh;
  background: #f8f9fc;
  position: relative;
}

.header-bg {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 350rpx;
  background: linear-gradient(135deg, #FF6B6B 0%, #FF8E8E 50%, #FFB6C1 100%);
  border-radius: 0 0 60rpx 60rpx;
  z-index: 0;
}

.header-bar {
  position: relative;
  z-index: 1;
  padding: 100rpx 32rpx 40rpx;
  
  .title {
    font-size: 48rpx;
    font-weight: 700;
    color: #fff;
    display: block;
  }
  
  .subtitle {
    font-size: 26rpx;
    color: rgba(255,255,255,0.8);
    margin-top: 8rpx;
  }
}

/* Tab 状态筛选栏 */
.tab-bar {
  position: relative;
  z-index: 1;
  display: flex;
  padding: 0 32rpx 24rpx;
  gap: 32rpx;
  
  .tab-item {
    position: relative;
    padding: 16rpx 0;
    
    .tab-text {
      font-size: 30rpx;
      color: rgba(255,255,255,0.7);
      font-weight: 500;
      transition: color 0.3s;
    }
    
    &.active {
      .tab-text {
        color: #fff;
        font-weight: 600;
      }
    }
    
    .tab-indicator {
      position: absolute;
      bottom: 0;
      left: 50%;
      transform: translateX(-50%);
      width: 40rpx;
      height: 4rpx;
      background: #fff;
      border-radius: 2rpx;
    }
  }
}

.add-btn {
  position: fixed;
  bottom: 60rpx;
  right: 40rpx;
  width: 120rpx;
  height: 120rpx;
  background: linear-gradient(135deg, #FF6B6B, #FF8E8E);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 12rpx 40rpx rgba(255, 107, 107, 0.4);
  z-index: 100;
  
  .add-icon {
    font-size: 60rpx;
    color: #fff;
    font-weight: 300;
  }
}

.wish-list {
  position: relative;
  z-index: 1;
  padding: 0 32rpx;
  height: calc(100vh - 380rpx);
}

.loading-state, .empty-state {
  text-align: center;
  padding: 100rpx 0;
}

.empty-state {
  .empty-icon-wrapper {
    width: 160rpx;
    height: 160rpx;
    background: linear-gradient(135deg, #fff0f6, #ffe4e6);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 auto 32rpx;
    
    .empty-icon {
      font-size: 80rpx;
    }
  }
  
  .empty-text {
    font-size: 32rpx;
    color: #2d3748;
    font-weight: 500;
  }
  
  .empty-subtext {
    font-size: 26rpx;
    color: #8b9aad;
    margin-top: 8rpx;
  }
}

.wish-cards {
  padding-top: 20rpx;
}

.wish-card {
  background: #fff;
  border-radius: 32rpx;
  padding: 32rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 8rpx 32rpx rgba(255, 107, 107, 0.08);
  animation: slideUp 0.4s ease-out forwards;
  opacity: 0;
  
  &.completed {
    opacity: 0.7;
    
    .wish-title {
      text-decoration: line-through;
      color: #8b9aad;
    }
  }
}

@keyframes slideUp {
  from { opacity: 0; transform: translateY(20rpx); }
  to { opacity: 1; transform: translateY(0); }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
  
  .wish-type-tag {
    padding: 8rpx 20rpx;
    border-radius: 20rpx;
    font-size: 22rpx;
    font-weight: 500;
    
    &.gift { background: rgba(255, 107, 107, 0.1); color: #FF6B6B; }
    &.travel { background: rgba(79, 172, 254, 0.1); color: #4facfe; }
    &.dinner { background: rgba(246, 173, 85, 0.1); color: #f6ad55; }
    &.electronic { background: rgba(155, 89, 182, 0.1); color: #9B59B6; }
    &.other { background: rgba(104, 211, 145, 0.1); color: #68d391; }
  }
  
  .wish-status {
    padding: 8rpx 20rpx;
    border-radius: 20rpx;
    font-size: 22rpx;
    
    &.status-0 { background: rgba(107, 141, 214, 0.1); color: #6B8DD6; }
    &.status-1 { background: rgba(246, 173, 85, 0.1); color: #f6ad55; }
    &.status-2 { background: rgba(104, 211, 145, 0.1); color: #68d391; }
  }
}

.wish-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #2d3748;
  display: block;
  margin-bottom: 8rpx;
}

.wish-desc {
  font-size: 26rpx;
  color: #8b9aad;
  display: block;
  margin-bottom: 16rpx;
}

.wish-budget {
  display: flex;
  align-items: center;
  gap: 8rpx;
  margin-bottom: 16rpx;
  
  .budget-icon {
    font-size: 24rpx;
  }
  
  .budget-text {
    font-size: 24rpx;
    color: #8b9aad;
  }
}

.progress-section {
  display: flex;
  align-items: center;
  gap: 16rpx;
  margin-bottom: 20rpx;
  
  .progress-bar {
    flex: 1;
    height: 8rpx;
    background: #f1f5f9;
    border-radius: 4rpx;
    overflow: hidden;
    
    .progress-fill {
      height: 100%;
      background: linear-gradient(90deg, #FF6B6B, #FF8E8E);
      border-radius: 4rpx;
    }
  }
  
  .progress-text {
    font-size: 24rpx;
    color: #FF6B6B;
    font-weight: 500;
  }
}

.card-actions {
  display: flex;
  gap: 16rpx;
  
  .action-btn {
    flex: 1;
    text-align: center;
    padding: 20rpx 0;
    border-radius: 24rpx;
    font-size: 26rpx;
    font-weight: 500;
    
    &.claim {
      background: linear-gradient(135deg, #FF6B6B, #FF8E8E);
      color: #fff;
    }
    
    &.complete {
      background: linear-gradient(135deg, #68d391, #48bb78);
      color: #fff;
    }
    
    &.delete {
      background: #f1f5f9;
      color: #fc8181;
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
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: flex-end;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: #fff;
  border-radius: 40rpx 40rpx 0 0;
  width: 100%;
  max-height: 85vh;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 40rpx;
  
  .modal-title {
    font-size: 36rpx;
    font-weight: 600;
    color: #2d3748;
  }
  
  .modal-close {
    font-size: 40rpx;
    color: #8b9aad;
  }
}

.modal-body {
  padding: 0 40rpx 40rpx;
  max-height: 60vh;
  overflow-y: auto;
}

.form-item {
  margin-bottom: 32rpx;
  
  .form-label {
    font-size: 26rpx;
    color: #5a6c7d;
    display: block;
    margin-bottom: 12rpx;
  }
  
  .form-input, .form-textarea {
    width: 100%;
    padding: 24rpx;
    background: #f8f9fc;
    border-radius: 20rpx;
    font-size: 30rpx;
    color: #2d3748;
    
    &::placeholder {
      color: #a0aec0;
    }
  }
  
  .form-textarea {
    height: 160rpx;
    resize: none;
  }
}

.type-list {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
  
  .type-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 16rpx 24rpx;
    background: #f8f9fc;
    border-radius: 16rpx;
    border: 2rpx solid transparent;
    
    &.active {
      background: rgba(255, 107, 107, 0.1);
      border-color: #FF6B6B;
    }
    
    .type-icon {
      font-size: 36rpx;
      margin-bottom: 4rpx;
    }
    
    .type-name {
      font-size: 22rpx;
      color: #5a6c7d;
    }
  }
}

.budget-row {
  display: flex;
  align-items: center;
  gap: 16rpx;
  
  .budget-input {
    flex: 1;
    padding: 20rpx;
    background: #f8f9fc;
    border-radius: 16rpx;
    font-size: 28rpx;
    text-align: center;
  }
  
  .budget-separator, .budget-unit {
    font-size: 28rpx;
    color: #8b9aad;
  }
}

.date-picker {
  padding: 24rpx;
  background: #f8f9fc;
  border-radius: 20rpx;
  
  text {
    font-size: 30rpx;
    color: #2d3748;
    
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
    background: linear-gradient(135deg, #FF6B6B, #FF8E8E);
    color: #fff;
  }
}
</style>
