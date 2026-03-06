<template>
  <view class="wish-page">
    <!-- 顶部背景 -->
    <view class="header-bg">
      <view class="bg-pattern"></view>
    </view>
    
    <!-- 顶部标题栏 -->
    <view class="header-bar">
      <view class="header-left">
        <text class="title">心愿墙</text>
        <text class="subtitle">记录我们的愿望</text>
      </view>
      <view class="header-right" @click="showFilter = !showFilter">
        <text class="filter-icon">🔍</text>
      </view>
    </view>
    
    <!-- 筛选弹窗 -->
    <view v-if="showFilter" class="filter-popup" @click="showFilter = false">
      <view class="filter-content" @click.stop>
        <view class="filter-header">
          <text class="filter-title">筛选</text>
          <text class="filter-close" @click="showFilter = false">✕</text>
        </view>
        <view class="filter-body">
          <view class="filter-item">
            <text class="filter-label">可见范围</text>
            <view class="filter-options">
              <view 
                class="filter-option" 
                :class="{ active: filterType === 'all' }"
                @click="filterType = 'all'"
              >
                <text>全部</text>
              </view>
              <view 
                class="filter-option" 
                :class="{ active: filterType === 'couple' }"
                @click="filterType = 'couple'"
              >
                <text>仅情侣</text>
              </view>
              <view 
                class="filter-option" 
                :class="{ active: filterType === 'my' }"
                @click="filterType = 'my'"
              >
                <text>我的</text>
              </view>
            </view>
          </view>
          <view class="filter-item">
            <text class="filter-label">状态</text>
            <view class="filter-options">
              <view 
                class="filter-option" 
                :class="{ active: filterStatus === null }"
                @click="filterStatus = null"
              >
                <text>全部</text>
              </view>
              <view 
                class="filter-option" 
                :class="{ active: filterStatus === 0 }"
                @click="filterStatus = 0"
              >
                <text>待认领</text>
              </view>
              <view 
                class="filter-option" 
                :class="{ active: filterStatus === 1 }"
                @click="filterStatus = 1"
              >
                <text>进行中</text>
              </view>
              <view 
                class="filter-option" 
                :class="{ active: filterStatus === 2 }"
                @click="filterStatus = 2"
              >
                <text>已完成</text>
              </view>
            </view>
          </view>
        </view>
      </view>
    </view>
    
    <!-- 心愿列表 -->
    <scroll-view scroll-y class="wish-list" refresher-enabled @refresherrefresh="onRefresh">
      <view v-if="loading" class="loading-state">
        <text class="loading-text">加载中...</text>
      </view>
      
      <view v-else-if="filteredWishes.length === 0" class="empty-state">
        <view class="empty-icon-wrapper">
          <text class="empty-icon">💝</text>
        </view>
        <text class="empty-text">还没有心愿</text>
        <text class="empty-subtext">添加第一个愿望吧</text>
      </view>
      
      <view v-else class="wish-cards">
        <view 
          v-for="(wish, index) in filteredWishes" 
          :key="wish.id"
          class="wish-card"
          :class="{ completed: wish.status === 2, claimed: wish.status === 1 }"
          :style="{ animationDelay: `${index * 0.05}s` }"
          @click="goDetail(wish)"
        >
          <!-- 卡片头部 -->
          <view class="wish-header">
            <view class="wish-type-tag" :class="wish.type || 'default'">
              <text>{{ getTypeName(wish.type) }}</text>
            </view>
            <view class="wish-status" :class="`status-${wish.status}`">
              <text>{{ getStatusName(wish.status) }}</text>
            </view>
          </view>
          
          <!-- 卡片内容 -->
          <view class="wish-content">
            <text class="wish-title">{{ wish.title }}</text>
            <text v-if="wish.description" class="wish-desc">{{ wish.description }}</text>
          </view>
          
          <!-- 卡片底部 -->
          <view class="wish-footer">
            <view class="wish-info">
              <view v-if="wish.budgetMin || wish.budgetMax" class="wish-budget">
                <text class="budget-icon">💰</text>
                <text class="budget-text">{{ formatBudget(wish.budgetMin, wish.budgetMax) }}</text>
              </view>
              <view v-if="wish.expectDate" class="wish-date">
                <text class="date-icon">📅</text>
                <text class="date-text">{{ wish.expectDate }}</text>
              </view>
            </view>
            
            <!-- 进度条 -->
            <view v-if="wish.status === 1 && wish.progress > 0" class="wish-progress">
              <view class="progress-bar">
                <view class="progress-fill" :style="{ width: wish.progress + '%' }"></view>
              </view>
              <text class="progress-text">{{ wish.progress }}%</text>
            </view>
            
            <!-- 操作按钮 -->
            <view class="wish-actions" @click.stop>
              <view v-if="wish.status === 0" class="action-btn claim-btn" @click="claimWish(wish)">
                <text>认领</text>
              </view>
              <view v-if="wish.status === 1 && wish.claimantId === currentUserId" class="action-btn progress-btn" @click="showProgressModal(wish)">
                <text>更新进度</text>
              </view>
              <view v-if="wish.status === 1 && (wish.userId === currentUserId || wish.claimantId === currentUserId)" class="action-btn complete-btn" @click="completeWish(wish)">
                <text>完成</text>
              </view>
            </view>
          </view>
          
          <!-- 认领人信息 -->
          <view v-if="wish.claimantId" class="claimant-info">
            <text class="claimant-icon">🎁</text>
            <text class="claimant-text">{{ wish.claimantName || 'Ta' }} 正在准备</text>
          </view>
        </view>
      </view>
    </scroll-view>
    
    <!-- 添加按钮 -->
    <view class="add-btn" @click="showCreateModal = true">
      <text class="add-icon">+</text>
    </view>
    
    <!-- 创建心愿弹窗 -->
    <view v-if="showCreateModal" class="modal-overlay" @click="showCreateModal = false">
      <view class="modal-content" @click.stop>
        <view class="modal-header">
          <text class="modal-title">许下心愿</text>
          <text class="modal-close" @click="showCreateModal = false">✕</text>
        </view>
        
        <view class="modal-body">
          <!-- 标题 -->
          <view class="form-item">
            <text class="form-label">心愿标题 *</text>
            <input 
              class="form-input" 
              v-model="newWish.title" 
              placeholder="写下你的愿望"
              maxlength="50"
            />
          </view>
          
          <!-- 描述 -->
          <view class="form-item">
            <text class="form-label">详细描述</text>
            <textarea 
              class="form-textarea" 
              v-model="newWish.description" 
              placeholder="描述一下这个愿望..."
              maxlength="200"
            />
          </view>
          
          <!-- 类型 -->
          <view class="form-item">
            <text class="form-label">愿望类型</text>
            <view class="type-selector">
              <view 
                v-for="type in wishTypes" 
                :key="type.value"
                class="type-option"
                :class="{ active: newWish.type === type.value }"
                @click="newWish.type = type.value"
              >
                <text class="type-icon">{{ type.icon }}</text>
                <text class="type-name">{{ type.label }}</text>
              </view>
            </view>
          </view>
          
          <!-- 预算 -->
          <view class="form-item">
            <text class="form-label">预算范围</text>
            <view class="budget-inputs">
              <input 
                class="form-input budget" 
                type="number"
                v-model="newWish.budgetMin" 
                placeholder="最低"
              />
              <text class="budget-separator">-</text>
              <input 
                class="form-input budget" 
                type="number"
                v-model="newWish.budgetMax" 
                placeholder="最高"
              />
              <text class="budget-unit">元</text>
            </view>
          </view>
          
          <!-- 期望日期 -->
          <view class="form-item">
            <text class="form-label">期望实现日期</text>
            <picker mode="date" :value="newWish.expectDate" @change="onDateChange">
              <view class="date-picker">
                <text :class="{ placeholder: !newWish.expectDate }">
                  {{ newWish.expectDate || '选择日期' }}
                </text>
              </view>
            </picker>
          </view>
          
          <!-- 可见范围 -->
          <view class="form-item">
            <text class="form-label">可见范围</text>
            <view class="visibility-selector">
              <view 
                class="visibility-option"
                :class="{ active: newWish.visibility === 'couple' }"
                @click="newWish.visibility = 'couple'"
              >
                <text class="visibility-icon">💑</text>
                <text>仅情侣</text>
              </view>
              <view 
                class="visibility-option"
                :class="{ active: newWish.visibility === 'family' }"
                @click="newWish.visibility = 'family'"
              >
                <text class="visibility-icon">👨‍👩‍👧</text>
                <text>全家可见</text>
              </view>
            </view>
          </view>
        </view>
        
        <view class="modal-footer">
          <view class="cancel-btn" @click="showCreateModal = false">
            <text>取消</text>
          </view>
          <view class="submit-btn" @click="createWish">
            <text>许愿</text>
          </view>
        </view>
      </view>
    </view>
    
    <!-- 进度更新弹窗 -->
    <view v-if="showProgressModal" class="modal-overlay" @click="showProgressModal = false">
      <view class="modal-content small" @click.stop>
        <view class="modal-header">
          <text class="modal-title">更新进度</text>
          <text class="modal-close" @click="showProgressModal = false">✕</text>
        </view>
        
        <view class="modal-body">
          <view class="progress-slider">
            <text class="progress-value">{{ currentProgress }}%</text>
            <slider 
              :value="currentProgress" 
              min="0" 
              max="100" 
              step="10"
              activeColor="#8B5CF6"
              backgroundColor="#E2E8F0"
              block-color="#8B5CF6"
              block-size="24"
              @change="onProgressChange"
            />
            <view class="progress-labels">
              <text>0%</text>
              <text>50%</text>
              <text>100%</text>
            </view>
          </view>
        </view>
        
        <view class="modal-footer">
          <view class="cancel-btn" @click="showProgressModal = false">
            <text>取消</text>
          </view>
          <view class="submit-btn" @click="updateProgress">
            <text>保存</text>
          </view>
        </view>
      </view>
    </view>
    
    <!-- 心愿详情弹窗 -->
    <view v-if="showDetailModal" class="modal-overlay" @click="showDetailModal = false">
      <view class="modal-content large" @click.stop>
        <view class="modal-header">
          <text class="modal-title">心愿详情</text>
          <text class="modal-close" @click="showDetailModal = false">✕</text>
        </view>
        
        <scroll-view scroll-y class="detail-content">
          <view class="detail-header">
            <view class="detail-type-tag" :class="currentWish.type || 'default'">
              <text>{{ getTypeName(currentWish.type) }}</text>
            </view>
            <view class="detail-status" :class="`status-${currentWish.status}`">
              <text>{{ getStatusName(currentWish.status) }}</text>
            </view>
          </view>
          
          <text class="detail-title">{{ currentWish.title }}</text>
          
          <text v-if="currentWish.description" class="detail-desc">{{ currentWish.description }}</text>
          
          <view class="detail-info">
            <view v-if="currentWish.budgetMin || currentWish.budgetMax" class="info-row">
              <text class="info-icon">💰</text>
              <text class="info-label">预算：</text>
              <text class="info-value">{{ formatBudget(currentWish.budgetMin, currentWish.budgetMax) }}</text>
            </view>
            <view v-if="currentWish.expectDate" class="info-row">
              <text class="info-icon">📅</text>
              <text class="info-label">期望日期：</text>
              <text class="info-value">{{ currentWish.expectDate }}</text>
            </view>
            <view v-if="currentWish.creatorName" class="info-row">
              <text class="info-icon">👤</text>
              <text class="info-label">许愿人：</text>
              <text class="info-value">{{ currentWish.creatorName }}</text>
            </view>
            <view v-if="currentWish.claimantName" class="info-row">
              <text class="info-icon">🎁</text>
              <text class="info-label">认领人：</text>
              <text class="info-value">{{ currentWish.claimantName }}</text>
            </view>
          </view>
          
          <view v-if="currentWish.status === 1 && currentWish.progress > 0" class="detail-progress">
            <text class="progress-label">完成进度</text>
            <view class="progress-bar large">
              <view class="progress-fill" :style="{ width: currentWish.progress + '%' }"></view>
            </view>
            <text class="progress-value">{{ currentWish.progress }}%</text>
          </view>
        </scroll-view>
        
        <view class="detail-actions">
          <view v-if="currentWish.userId === currentUserId" class="action-btn danger" @click="deleteWish(currentWish)">
            <text>删除</text>
          </view>
          <view class="action-btn secondary" @click="showDetailModal = false">
            <text>关闭</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { wishApi } from '../../api/wish'

const loading = ref(true)
const wishes = ref([])
const showFilter = ref(false)
const showCreateModal = ref(false)
const showProgressModal = ref(false)
const showDetailModal = ref(false)
const currentUserId = ref(1)
const currentWish = ref({})
const currentProgress = ref(0)

// 筛选条件
const filterType = ref('all')
const filterStatus = ref(null)

// 新建心愿表单
const newWish = ref({
  title: '',
  description: '',
  type: 'gift',
  budgetMin: '',
  budgetMax: '',
  expectDate: '',
  visibility: 'couple'
})

// 心愿类型
const wishTypes = [
  { value: 'gift', label: '礼物', icon: '🎁' },
  { value: 'travel', label: '旅行', icon: '✈️' },
  { value: 'dinner', label: '约会', icon: '🍽️' },
  { value: 'electronic', label: '数码', icon: '📱' },
  { value: 'other', label: '其他', icon: '💝' }
]

// 加载心愿列表
const loadWishes = async () => {
  loading.value = true
  console.log('开始加载心愿列表...')
  try {
    const familyId = uni.getStorageSync('currentFamilyId') || 1
    console.log('familyId:', familyId)
    const res = await wishApi.getList(familyId)
    console.log('API返回:', res)
    if (res && Array.isArray(res)) {
      wishes.value = res
      console.log('心愿列表:', wishes.value)
    } else if (res && res.data && Array.isArray(res.data)) {
      wishes.value = res.data
      console.log('心愿列表(从data):', wishes.value)
    } else {
      console.log('返回格式不对:', res)
      wishes.value = []
    }
  } catch (e) {
    console.error('加载心愿列表失败:', e)
    uni.showToast({ title: '加载失败', icon: 'none' })
    wishes.value = []
  } finally {
    loading.value = false
  }
}

// 筛选后的心愿
const filteredWishes = computed(() => {
  let result = wishes.value
  
  // 按类型筛选
  if (filterType.value === 'my') {
    result = result.filter(w => w.userId === currentUserId.value || w.claimantId === currentUserId.value)
  }
  
  // 按状态筛选
  if (filterStatus.value !== null) {
    result = result.filter(w => w.status === filterStatus.value)
  }
  
  return result
})

// 获取类型名称
const getTypeName = (type) => {
  const found = wishTypes.find(t => t.value === type)
  return found ? found.label : '愿望'
}

// 获取状态名称
const getStatusName = (status) => {
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
  newWish.value.expectDate = e.detail.value
}

// 进度变化
const onProgressChange = (e) => {
  currentProgress.value = e.detail.value
}

// 刷新
const onRefresh = async () => {
  await loadWishes()
  uni.showToast({ title: '刷新成功', icon: 'success' })
}

// 跳转到详情
const goDetail = async (wish) => {
  try {
    const res = await wishApi.getDetail(wish.id)
    if (res) {
      currentWish.value = res
      showDetailModal.value = true
    }
  } catch (e) {
    console.error('获取详情失败:', e)
  }
}

// 认领心愿
const claimWish = async (wish) => {
  try {
    await wishApi.claim(wish.id)
    uni.showToast({ title: '认领成功', icon: 'success' })
    await loadWishes()
  } catch (e) {
    uni.showToast({ title: '认领失败', icon: 'none' })
  }
}

// 显示进度更新弹窗
const showProgressModalFn = (wish) => {
  currentWish.value = wish
  currentProgress.value = wish.progress || 0
  showProgressModal.value = true
}

// 更新进度
const updateProgress = async () => {
  try {
    await wishApi.updateProgress(currentWish.value.id, { progress: currentProgress.value })
    uni.showToast({ title: '更新成功', icon: 'success' })
    showProgressModal.value = false
    await loadWishes()
  } catch (e) {
    uni.showToast({ title: '更新失败', icon: 'none' })
  }
}

// 完成心愿
const completeWish = async (wish) => {
  try {
    await wishApi.complete(wish.id)
    uni.showToast({ title: '太棒了！', icon: 'success' })
    await loadWishes()
  } catch (e) {
    uni.showToast({ title: '操作失败', icon: 'none' })
  }
}

// 创建心愿
const createWish = async () => {
  if (!newWish.value.title.trim()) {
    uni.showToast({ title: '请输入心愿', icon: 'none' })
    return
  }
  
  try {
    const familyId = uni.getStorageSync('currentFamilyId') || 1
    await wishApi.create({
      ...newWish.value,
      familyId
    })
    uni.showToast({ title: '许愿成功', icon: 'success' })
    showCreateModal.value = false
    // 重置表单
    newWish.value = {
      title: '',
      description: '',
      type: 'gift',
      budgetMin: '',
      budgetMax: '',
      expectDate: '',
      visibility: 'couple'
    }
    await loadWishes()
  } catch (e) {
    uni.showToast({ title: '创建失败', icon: 'none' })
  }
}

// 删除心愿
const deleteWish = async (wish) => {
  uni.showModal({
    title: '确认删除',
    content: '确定要删除这个心愿吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          await wishApi.delete(wish.id)
          uni.showToast({ title: '删除成功', icon: 'success' })
          showDetailModal.value = false
          await loadWishes()
        } catch (e) {
          uni.showToast({ title: '删除失败', icon: 'none' })
        }
      }
    }
  })
}

// 页面加载
onMounted(() => {
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
  height: 400rpx;
  background: linear-gradient(135deg, #FF6B6B 0%, #FF8E8E 50%, #FFB6C1 100%);
  border-radius: 0 0 60rpx 60rpx;
  z-index: 0;
  
  .bg-pattern {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    opacity: 0.1;
    background-image: radial-gradient(circle at 20% 50%, rgba(255,255,255,0.8) 0%, transparent 50%),
                      radial-gradient(circle at 80% 20%, rgba(255,255,255,0.6) 0%, transparent 40%);
  }
}

.header-bar {
  position: relative;
  z-index: 1;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 100rpx 32rpx 40rpx;
  
  .header-left {
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
  
  .header-right {
    width: 80rpx;
    height: 80rpx;
    background: rgba(255,255,255,0.2);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    
    .filter-icon {
      font-size: 36rpx;
    }
  }
}

.wish-list {
  position: relative;
  z-index: 1;
  height: calc(100vh - 300rpx);
  padding: 0 32rpx;
}

.loading-state, .empty-state {
  text-align: center;
  padding: 100rpx 0;
  
  .loading-text {
    color: #8b9aad;
    font-size: 28rpx;
  }
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
    display: block;
    margin-bottom: 8rpx;
  }
  
  .empty-subtext {
    font-size: 26rpx;
    color: #8b9aad;
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
  
  &.claimed {
    border: 2rpx solid #FF6B6B;
  }
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.wish-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

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
  &.default { background: rgba(107, 141, 214, 0.1); color: #6B8DD6; }
}

.wish-status {
  padding: 8rpx 20rpx;
  border-radius: 20rpx;
  font-size: 22rpx;
  
  &.status-0 { background: rgba(107, 141, 214, 0.1); color: #6B8DD6; }
  &.status-1 { background: rgba(246, 173, 85, 0.1); color: #f6ad55; }
  &.status-2 { background: rgba(104, 211, 145, 0.1); color: #68d391; }
}

.wish-content {
  margin-bottom: 20rpx;
  
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
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.wish-footer {
  .wish-info {
    display: flex;
    gap: 24rpx;
    margin-bottom: 16rpx;
    
    .wish-budget, .wish-date {
      display: flex;
      align-items: center;
      gap: 8rpx;
      
      .budget-icon, .date-icon {
        font-size: 24rpx;
      }
      
      .budget-text, .date-text {
        font-size: 24rpx;
        color: #8b9aad;
      }
    }
  }
  
  .wish-progress {
    display: flex;
    align-items: center;
    gap: 16rpx;
    margin-bottom: 16rpx;
    
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
        transition: width 0.3s ease;
      }
    }
    
    .progress-text {
      font-size: 24rpx;
      color: #FF6B6B;
      font-weight: 500;
    }
  }
  
  .wish-actions {
    display: flex;
    gap: 16rpx;
    
    .action-btn {
      flex: 1;
      text-align: center;
      padding: 20rpx 0;
      border-radius: 24rpx;
      font-size: 26rpx;
      font-weight: 500;
      
      &.claim-btn {
        background: linear-gradient(135deg, #FF6B6B, #FF8E8E);
        color: #fff;
      }
      
      &.progress-btn {
        background: rgba(107, 141, 214, 0.1);
        color: #6B8DD6;
      }
      
      &.complete-btn {
        background: linear-gradient(135deg, #68d391, #48bb78);
        color: #fff;
      }
    }
  }
}

.claimant-info {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding-top: 16rpx;
  border-top: 2rpx solid #f1f5f9;
  margin-top: 16rpx;
  
  .claimant-icon {
    font-size: 24rpx;
  }
  
  .claimant-text {
    font-size: 24rpx;
    color: #FF6B6B;
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
  animation: slideUp 0.3s ease;
  
  &.small {
    max-height: 50vh;
  }
  
  &.large {
    max-height: 90vh;
  }
}

@keyframes slideUp {
  from { transform: translateY(100%); }
  to { transform: translateY(0); }
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 40rpx 40rpx 20rpx;
  
  .modal-title {
    font-size: 36rpx;
    font-weight: 600;
    color: #2d3748;
  }
  
  .modal-close {
    font-size: 40rpx;
    color: #8b9aad;
    padding: 10rpx;
  }
}

.modal-body {
  padding: 20rpx 40rpx 40rpx;
  max-height: 60vh;
}

.detail-content {
  max-height: 50vh;
  padding: 0 40rpx;
}

.modal-footer {
  display: flex;
  gap: 24rpx;
  padding: 20rpx 40rpx 60rpx;
  
  .cancel-btn, .submit-btn {
    flex: 1;
    text-align: center;
    padding: 28rpx 0;
    border-radius: 32rpx;
    font-size: 30rpx;
    font-weight: 500;
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

.type-selector {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
  
  .type-option {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 20rpx 28rpx;
    background: #f8f9fc;
    border-radius: 20rpx;
    border: 2rpx solid transparent;
    
    &.active {
      background: rgba(255, 107, 107, 0.1);
      border-color: #FF6B6B;
    }
    
    .type-icon {
      font-size: 40rpx;
      margin-bottom: 8rpx;
    }
    
    .type-name {
      font-size: 24rpx;
      color: #5a6c7d;
    }
  }
}

.budget-inputs {
  display: flex;
  align-items: center;
  gap: 16rpx;
  
  .budget {
    flex: 1;
    text-align: center;
  }
  
  .budget-separator, .budget-unit {
    color: #8b9aad;
    font-size: 28rpx;
  }
}

.date-picker {
  padding: 24rpx;
  background: #f8f9fc;
  border-radius: 20rpx;
  
  text {
    color: #2d3748;
    font-size: 30rpx;
    
    &.placeholder {
      color: #a0aec0;
    }
  }
}

.visibility-selector {
  display: flex;
  gap: 24rpx;
  
  .visibility-option {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 24rpx;
    background: #f8f9fc;
    border-radius: 20rpx;
    border: 2rpx solid transparent;
    
    &.active {
      background: rgba(255, 107, 107, 0.1);
      border-color: #FF6B6B;
    }
    
    .visibility-icon {
      font-size: 48rpx;
      margin-bottom: 8rpx;
    }
  }
}

.progress-slider {
  padding: 20rpx;
  
  .progress-value {
    font-size: 48rpx;
    font-weight: 700;
    color: #FF6B6B;
    text-align: center;
    display: block;
    margin-bottom: 20rpx;
  }
  
  .progress-labels {
    display: flex;
    justify-content: space-between;
    color: #8b9aad;
    font-size: 24rpx;
    margin-top: 8rpx;
  }
}

.filter-popup {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 1000;
}

.filter-content {
  background: #fff;
  border-radius: 0 0 40rpx 40rpx;
  padding: 40rpx;
  animation: slideDown 0.3s ease;
}

@keyframes slideDown {
  from { transform: translateY(-100%); }
  to { transform: translateY(0); }
}

.filter-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32rpx;
  
  .filter-title {
    font-size: 32rpx;
    font-weight: 600;
    color: #2d3748;
  }
  
  .filter-close {
    font-size: 40rpx;
    color: #8b9aad;
  }
}

.filter-item {
  margin-bottom: 32rpx;
  
  .filter-label {
    font-size: 26rpx;
    color: #5a6c7d;
    display: block;
    margin-bottom: 16rpx;
  }
  
  .filter-options {
    display: flex;
    gap: 16rpx;
    
    .filter-option {
      padding: 16rpx 32rpx;
      background: #f8f9fc;
      border-radius: 20rpx;
      font-size: 26rpx;
      color: #5a6c7d;
      
      &.active {
        background: rgba(255, 107, 107, 0.1);
        color: #FF6B6B;
      }
    }
  }
}

// 详情页样式
.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;
}

.detail-type-tag, .detail-status {
  padding: 12rpx 24rpx;
  border-radius: 24rpx;
  font-size: 24rpx;
  font-weight: 500;
}

.detail-title {
  font-size: 40rpx;
  font-weight: 700;
  color: #2d3748;
  display: block;
  margin-bottom: 16rpx;
}

.detail-desc {
  font-size: 28rpx;
  color: #5a6c7d;
  display: block;
  margin-bottom: 32rpx;
  line-height: 1.6;
}

.detail-info {
  background: #f8f9fc;
  border-radius: 24rpx;
  padding: 24rpx;
  margin-bottom: 24rpx;
  
  .info-row {
    display: flex;
    align-items: center;
    margin-bottom: 16rpx;
    
    &:last-child {
      margin-bottom: 0;
    }
    
    .info-icon {
      font-size: 28rpx;
      margin-right: 12rpx;
    }
    
    .info-label {
      font-size: 26rpx;
      color: #8b9aad;
      margin-right: 8rpx;
    }
    
    .info-value {
      font-size: 26rpx;
      color: #2d3748;
    }
  }
}

.detail-progress {
  background: #f8f9fc;
  border-radius: 24rpx;
  padding: 24rpx;
  
  .progress-label {
    font-size: 26rpx;
    color: #5a6c7d;
    display: block;
    margin-bottom: 16rpx;
  }
  
  .progress-bar.large {
    height: 16rpx;
    
    .progress-fill {
      background: linear-gradient(90deg, #FF6B6B, #FF8E8E);
    }
  }
  
  .progress-value {
    font-size: 32rpx;
    font-weight: 600;
    color: #FF6B6B;
    display: block;
    text-align: center;
    margin-top: 12rpx;
  }
}

.detail-actions {
  display: flex;
  gap: 24rpx;
  padding: 20rpx 40rpx 60rpx;
  
  .action-btn {
    flex: 1;
    text-align: center;
    padding: 28rpx 0;
    border-radius: 32rpx;
    font-size: 30rpx;
    font-weight: 500;
    
    &.danger {
      background: rgba(252, 129, 129, 0.1);
      color: #fc8181;
    }
    
    &.secondary {
      background: #f1f5f9;
      color: #5a6c7d;
    }
  }
}
</style>
