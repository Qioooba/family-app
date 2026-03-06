<template>
  <view class="anniversary-page">
    <!-- 顶部背景 -->
    <view class="header-bg"></view>
    
    <!-- 顶部标题 -->
    <view class="header-bar">
      <text class="title">纪念日</text>
      <text class="subtitle">记录重要时刻</text>
    </view>
    
    <!-- 添加按钮 -->
    <view class="add-btn" @click="showAddModal = true">
      <text>+</text>
    </view>
    
    <!-- 纪念日列表 -->
    <view class="anniversary-list">
      <view v-if="loading" class="loading-state">
        <text>加载中...</text>
      </view>
      
      <view v-else-if="anniversaries.length === 0" class="empty-state">
        <text class="empty-icon">📅</text>
        <text class="empty-text">还没有纪念日</text>
        <text class="empty-sub">点击+添加第一个纪念日</text>
      </view>
      
      <view v-else class="cards">
        <view 
          v-for="(item, index) in anniversaries" 
          :key="item.id"
          class="anniversary-card"
          :class="{ passed: isPassed(item.targetDate) }"
          :style="{ animationDelay: `${index * 0.05}s` }"
          @click="showDetail(item)"
        >
          <!-- 卡片头部 -->
          <view class="card-header">
            <view class="icon-wrapper" :style="{ background: getIconBg(item.type) }">
              <text class="card-icon">{{ getIcon(item.type) }}</text>
            </view>
            <view class="card-info">
              <text class="card-title">{{ item.title }}</text>
              <text class="card-date">{{ formatDate(item.targetDate) }}</text>
            </view>
            <view class="card-days">
              <text class="days-num">{{ getDaysUntil(item.targetDate) }}</text>
              <text class="days-label">天后</text>
            </view>
          </view>
          
          <!-- 进度条 -->
          <view v-if="item.isRepeat" class="progress-section">
            <view class="progress-bar">
              <view class="progress-fill" :style="{ width: getProgress(item.targetDate) + '%' }"></view>
            </view>
            <text class="progress-text">第{{ getYearCount(item.targetDate) }}年</text>
          </view>
          
          <!-- 操作按钮 -->
          <view class="card-actions" @click.stop>
            <view class="action-btn" @click="editItem(item)">
              <text>编辑</text>
            </view>
            <view class="action-btn delete" @click="deleteItem(item)">
              <text>删除</text>
            </view>
          </view>
        </view>
      </view>
    </view>
    
    <!-- 添加/编辑弹窗 -->
    <view v-if="showAddModal" class="modal-overlay" @click="closeModal">
      <view class="modal-content" @click.stop>
        <view class="modal-header">
          <text class="modal-title">{{ editingItem ? '编辑纪念日' : '添加纪念日' }}</text>
          <text class="modal-close" @click="closeModal">✕</text>
        </view>
        
        <view class="modal-body">
          <!-- 标题 -->
          <view class="form-item">
            <text class="form-label">标题 *</text>
            <input class="form-input" v-model="formData.title" placeholder="如：结婚纪念日" />
          </view>
          
          <!-- 日期 -->
          <view class="form-item">
            <text class="form-label">日期 *</text>
            <picker mode="date" :value="formData.date" @change="onDateChange">
              <view class="date-picker">
                <text :class="{ placeholder: !formData.date }">{{ formData.date || '选择日期' }}</text>
              </view>
            </picker>
          </view>
          
          <!-- 类型 -->
          <view class="form-item">
            <text class="form-label">类型</text>
            <view class="type-selector">
              <view 
                v-for="type in types" 
                :key="type.value"
                class="type-option"
                :class="{ active: formData.type === type.value }"
                @click="formData.type = type.value"
              >
                <text class="type-icon">{{ type.icon }}</text>
                <text class="type-name">{{ type.label }}</text>
              </view>
            </view>
          </view>
          
          <!-- 描述 -->
          <view class="form-item">
            <text class="form-label">描述</text>
            <textarea class="form-textarea" v-model="formData.description" placeholder="添加描述..." />
          </view>
          
          <!-- 重复 -->
          <view class="form-item switch-item">
            <text class="form-label">每年重复</text>
            <switch 
              :checked="formData.isRepeat" 
              @change="formData.isRepeat = !formData.isRepeat"
              color="#8B5CF6"
            />
          </view>
        </view>
        
        <view class="modal-footer">
          <view class="cancel-btn" @click="closeModal">
            <text>取消</text>
          </view>
          <view class="submit-btn" @click="saveItem">
            <text>保存</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { anniversaryApi } from '../../api/anniversary'

const loading = ref(true)
const anniversaries = ref([])
const showAddModal = ref(false)
const editingItem = ref(null)

const formData = ref({
  title: '',
  date: '',
  type: 'birthday',
  description: '',
  isRepeat: true
})

const types = [
  { value: 'birthday', label: '生日', icon: '🎂' },
  { value: 'wedding', label: '结婚', icon: '💒' },
  { value: 'love', label: '恋爱', icon: '💕' },
  { value: 'work', label: '工作', icon: '💼' },
  { value: 'family', label: '家人', icon: '👨‍👩‍👧' },
  { value: 'other', label: '其他', icon: '📌' }
]

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const familyId = uni.getStorageSync('currentFamilyId') || 1
    const res = await anniversaryApi.getList(familyId)
    if (res && Array.isArray(res)) {
      anniversaries.value = res
    }
  } catch (e) {
    console.error('加载失败:', e)
  } finally {
    loading.value = false
  }
}

// 格式化日期
const formatDate = (dateValue) => {
  if (!dateValue) return ''
  
  // 处理数组格式 [2026, 5, 6]
  if (Array.isArray(dateValue)) {
    const [year, month, day] = dateValue
    return `${month}月${day}日`
  }
  
  // 处理字符串格式
  const date = new Date(dateValue)
  if (isNaN(date.getTime())) return ''
  return `${date.getMonth() + 1}月${date.getDate()}日`
}

// 是否已过
const isPassed = (dateStr) => {
  if (!dateStr) return false
  const date = new Date(dateStr)
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  return date < today
}

// 距离天数
const getDaysUntil = (dateValue) => {
  if (!dateValue) return 0
  
  let date
  // 处理数组格式 [2026, 5, 6]
  if (Array.isArray(dateValue)) {
    const [year, month, day] = dateValue
    date = new Date(year, month - 1, day)
  } else {
    date = new Date(dateValue)
  }
  
  if (isNaN(date.getTime())) return 0
  
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  date.setHours(0, 0, 0, 0)
  const diff = date - today
  return Math.ceil(diff / (1000 * 60 * 60 * 24))
}

// 进度
const getProgress = (dateStr) => {
  if (!dateStr) return 0
  const days = getDaysUntil(dateStr)
  if (days > 365) return 0
  if (days < 0) return 100
  return Math.round((365 - days) / 365 * 100)
}

// 第几年
const getYearCount = (dateStr) => {
  if (!dateStr) return 1
  const date = new Date(dateStr)
  const today = new Date()
  return today.getFullYear() - date.getFullYear() + 1
}

// 获取图标
const getIcon = (type) => {
  const found = types.find(t => t.value === type)
  return found ? found.icon : '📌'
}

// 获取图标背景
const getIconBg = (type) => {
  const colors = {
    birthday: 'linear-gradient(135deg, #FF6B6B, #FF8E8E)',
    wedding: 'linear-gradient(135deg, #FFB6C1, #FFC0CB)',
    love: 'linear-gradient(135deg, #FF6B81, #FF8FAB)',
    work: 'linear-gradient(135deg, #6B8DD6, #8B5CF6)',
    family: 'linear-gradient(135deg, #68D391, #48BB78)',
    other: 'linear-gradient(135deg, #F6AD55, #ED8936)'
  }
  return colors[type] || colors.other
}

// 日期选择
const onDateChange = (e) => {
  formData.value.date = e.detail.value
}

// 编辑
const editItem = (item) => {
  editingItem.value = item
  formData.value = {
    title: item.title || '',
    date: item.targetDate || '',
    type: item.type || 'birthday',
    description: item.description || '',
    isRepeat: item.isRepeat || false
  }
  showAddModal.value = true
}

// 删除
const deleteItem = (item) => {
  uni.showModal({
    title: '确认删除',
    content: '确定要删除这个纪念日吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          await anniversaryApi.delete(item.id)
          uni.showToast({ title: '删除成功', icon: 'success' })
          await loadData()
        } catch (e) {
          uni.showToast({ title: '删除失败', icon: 'none' })
        }
      }
    }
  })
}

// 保存
const saveItem = async () => {
  if (!formData.value.title.trim()) {
    uni.showToast({ title: '请输入标题', icon: 'none' })
    return
  }
  if (!formData.value.date) {
    uni.showToast({ title: '请选择日期', icon: 'none' })
    return
  }
  
  try {
    const familyId = uni.getStorageSync('currentFamilyId') || 1
    const data = {
      ...formData.value,
      targetDate: formData.value.date,  // 将date转为targetDate
      familyId
    }
    // 删除date字段，避免重复
    delete data.date
    
    if (editingItem.value) {
      await anniversaryApi.update({ ...data, id: editingItem.value.id })
    } else {
      await anniversaryApi.create(data)
    }
    
    uni.showToast({ title: '保存成功', icon: 'success' })
    closeModal()
    await loadData()
  } catch (e) {
    console.error('保存失败:', e)
    uni.showToast({ title: '保存失败', icon: 'none' })
  }
}

// 关闭弹窗
const closeModal = () => {
  showAddModal.value = false
  editingItem.value = null
  formData.value = {
    title: '',
    date: '',
    type: 'birthday',
    description: '',
    isRepeat: true
  }
}

// 显示详情
const showDetail = (item) => {
  editItem(item)
}

onMounted(() => {
  loadData()
})
</script>

<style lang="scss" scoped>
.anniversary-page {
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
  background: linear-gradient(135deg, #8B5CF6 0%, #A78BFA 100%);
  border-radius: 0 0 60rpx 60rpx;
}

.header-bar {
  position: relative;
  z-index: 1;
  padding: 120rpx 32rpx 32rpx;
  
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

.add-btn {
  position: fixed;
  bottom: 60rpx;
  right: 40rpx;
  width: 120rpx;
  height: 120rpx;
  background: linear-gradient(135deg, #8B5CF6, #A78BFA);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 12rpx 40rpx rgba(139, 92, 246, 0.4);
  z-index: 100;
  
  text {
    font-size: 60rpx;
    color: #fff;
    font-weight: 300;
  }
}

.anniversary-list {
  position: relative;
  z-index: 1;
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
  .empty-icon {
    font-size: 120rpx;
    display: block;
    margin-bottom: 24rpx;
  }
  
  .empty-text {
    font-size: 32rpx;
    color: #2d3748;
    display: block;
  }
  
  .empty-sub {
    font-size: 26rpx;
    color: #8b9aad;
    margin-top: 8rpx;
  }
}

.cards {
  padding-top: 20rpx;
}

.anniversary-card {
  background: #fff;
  border-radius: 32rpx;
  padding: 32rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 8rpx 32rpx rgba(139, 92, 246, 0.08);
  animation: slideUp 0.4s ease-out forwards;
  opacity: 0;
  
  &.passed {
    opacity: 0.6;
  }
}

@keyframes slideUp {
  from { opacity: 0; transform: translateY(20rpx); }
  to { opacity: 1; transform: translateY(0); }
}

.card-header {
  display: flex;
  align-items: center;
  margin-bottom: 24rpx;
  
  .icon-wrapper {
    width: 96rpx;
    height: 96rpx;
    border-radius: 24rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 24rpx;
    
    .card-icon {
      font-size: 48rpx;
    }
  }
  
  .card-info {
    flex: 1;
    
    .card-title {
      font-size: 32rpx;
      font-weight: 600;
      color: #2d3748;
      display: block;
      margin-bottom: 8rpx;
    }
    
    .card-date {
      font-size: 26rpx;
      color: #8b9aad;
    }
  }
  
  .card-days {
    text-align: center;
    
    .days-num {
      font-size: 40rpx;
      font-weight: 700;
      color: #8B5CF6;
      display: block;
    }
    
    .days-label {
      font-size: 22rpx;
      color: #8b9aad;
    }
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
      background: linear-gradient(90deg, #8B5CF6, #A78BFA);
      border-radius: 4rpx;
    }
  }
  
  .progress-text {
    font-size: 24rpx;
    color: #8b9aad;
    white-space: nowrap;
  }
}

.card-actions {
  display: flex;
  gap: 16rpx;
  
  .action-btn {
    flex: 1;
    text-align: center;
    padding: 16rpx;
    background: #f8f9fc;
    border-radius: 16rpx;
    font-size: 26rpx;
    color: #5a6c7d;
    
    &.delete {
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
    
    &::placeholder {
      color: #a0aec0;
    }
  }
  
  .form-textarea {
    height: 160rpx;
    resize: none;
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

.type-selector {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
  
  .type-option {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 16rpx 24rpx;
    background: #f8f9fc;
    border-radius: 16rpx;
    border: 2rpx solid transparent;
    
    &.active {
      background: rgba(139, 92, 246, 0.1);
      border-color: #8B5CF6;
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

.switch-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  
  .form-label {
    margin-bottom: 0;
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
    background: linear-gradient(135deg, #8B5CF6, #A78BFA);
    color: #fff;
  }
}
</style>
