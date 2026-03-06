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
          
          <!-- 可见范围 -->
          <view class="form-item">
            <text class="form-label">可见范围</text>
            <view class="visibility-list">
              <view 
                class="visibility-item"
                :class="{ active: formData.visibility === 'couple' }"
                @click="formData.visibility = 'couple'"
              >
                <text class="visibility-icon">💑</text>
                <text>仅情侣</text>
              </view>
              <view 
                class="visibility-item"
                :class="{ active: formData.visibility === 'family' }"
                @click="formData.visibility = 'family'"
              >
                <text class="visibility-icon">👨‍👩‍👧</text>
                <text>全家可见</text>
              </view>
            </view>
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

// 表单数据
const formData = ref({
  title: '',
  description: '',
  type: 'gift',
  budgetMin: '',
  budgetMax: '',
  expectDate: '',
  visibility: 'couple'
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
    expectDate: '',
    visibility: 'couple'
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
      visibility: formData.value.visibility,
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
  console.log('加载心愿列表')
  
  try {
    const familyId = uni.getStorageSync('currentFamilyId') || 1
    const res = await uni.request({
      url: `https://qioba.cn:8443/api/wish/list?familyId=${familyId}`,
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
// ... 样式代码保持不变 ...
</style>
