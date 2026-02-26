<template>
  <view class="create-wish-page">
    <view class="nav-bar">
      <view class="back-btn" @click="goBack">
        <u-icon name="arrow-left" size="40" color="#666"></u-icon>
      </view>
      <view class="title-wrapper">
        <text class="title-icon">{{ isEdit ? '✏️' : '🌸' }}</text>
        <text class="title">{{ isEdit ? '编辑心愿' : '许下心愿' }}</text>
      </view>
      <view class="right-btn" :class="{ disabled: !form.content.trim() }" @click="saveWish">
        <text>{{ isEdit ? '保存' : '许愿' }}</text>
      </view>
    </view>
    
    <view class="form-container">
      <!-- 心愿内容 -->
      <view class="form-item-warm">
        <view class="form-label-wrapper">
          <text class="form-label-icon">💝</text>
          <text class="label">心愿内容 <text class="required">*</text></text>
        </view>
        <view class="input-wrapper">
          <input v-model="form.content" placeholder="写下你的心愿，例如：和家人一起看日出..." class="input" maxlength="100" />
        </view>
      </view>
      
      <!-- 心愿类型 - 新图标 -->
      <view class="form-item-warm">
        <view class="form-label-wrapper">
          <text class="form-label-icon">🏷️</text>
          <text class="label">心愿类型</text>
        </view>
        <view class="type-list">
          <view 
            v-for="type in wishTypes" 
            :key="type.value"
            class="type-item"
            :class="{ active: form.type === type.value }"
            @click="form.type = type.value"
          >
            <view class="type-icon-wrapper" :style="{ background: type.bgColor }">
              <text class="icon">{{ type.icon }}</text>
            </view>
            <text class="name">{{ type.label }}</text>
          </view>
        </view>
      </view>
      
      <!-- 优先级 - 新样式 -->
      <view class="form-item-warm">
        <view class="form-label-wrapper">
          <text class="form-label-icon">⭐</text>
          <text class="label">优先级</text>
        </view>
        <view class="priority-list">
          <view 
            v-for="p in priorities" 
            :key="p.value"
            class="priority-item"
            :class="{ active: form.priority === p.value }"
            @click="form.priority = p.value"
          >
            <text class="icon">{{ p.icon }}</text>
            <text class="name">{{ p.label }}</text>
          </view>
        </view>
      </view>
      
      <!-- 期望实现时间 - 精确到10分钟 -->
      <view class="form-item-warm">
        <view class="form-label-wrapper">
          <text class="form-label-icon">📅</text>
          <text class="label">期望实现时间</text>
        </view>
        <picker mode="date" :value="form.expectDate" @change="onDateChange">
          <view class="datetime-picker" :class="{ active: form.expectDate }">
            <text class="datetime-icon">📆</text>
            <text class="datetime-text">{{ form.expectDate || '选择日期' }}</text>
            <text class="datetime-arrow">›</text>
          </view>
        </picker>
        <!-- 时间选择（10分钟间隔） -->
        <view v-if="form.expectDate" class="time-picker-section">
          <text class="time-label">选择时间</text>
          <picker mode="multiSelector" :range="timeRange" :value="timeIndex" @change="onTimeChange" @columnchange="onTimeColumnChange">
            <view class="time-picker" :class="{ active: form.expectTime }">
              <text class="time-icon">🕐</text>
              <text class="time-text">{{ form.expectTime || '选择时间' }}</text>
              <text class="time-arrow">›</text>
            </view>
          </picker>
        </view>
      </view>
      
      <!-- 可见性 -->
      <view class="form-item-warm">
        <view class="form-label-wrapper">
          <text class="form-label-icon">👁️</text>
          <text class="label">可见范围</text>
        </view>
        <view class="visibility-list">
          <view 
            v-for="v in visibility" 
            :key="v.value"
            class="visibility-item"
            :class="{ active: form.visibility === v.value }"
            @click="form.visibility = v.value"
          >
            <text class="icon">{{ v.icon }}</text>
            <text class="name">{{ v.label }}</text>
          </view>
        </view>
      </view>
      
      <!-- 描述 -->
      <view class="form-item-warm">
        <view class="form-label-wrapper">
          <text class="form-label-icon">📝</text>
          <text class="label">详细描述</text>
        </view>
        <view class="textarea-wrapper">
          <textarea 
            v-model="form.description" 
            placeholder="详细描述你的心愿，让家人更了解你的想法..."
            class="textarea"
            maxlength="500"
          />
          <text class="char-count-warm">{{ form.description?.length || 0 }}/500</text>
        </view>
      </view>
      
      <!-- 图片 -->
      <view class="form-item-warm">
        <view class="form-label-wrapper">
          <text class="form-label-icon">🖼️</text>
          <text class="label">添加图片</text>
        </view>
        <view class="image-list">
          <view v-for="(img, index) in form.images" :key="index" class="image-item">
            <image :src="img" mode="aspectFill" class="uploaded-img" />
            <view class="delete-img" @click="removeImage(index)">
              <text>✕</text>
            </view>
          </view>
          <view v-if="form.images.length < 3" class="image-item add" @click="chooseImage">
            <text class="add-icon">+</text>
          </view>
        </view>
        <text class="image-tip">最多添加3张图片，让心愿更生动</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { wishApi } from '../../api/index.js'

const props = defineProps({
  id: {
    type: String,
    default: ''
  }
})

const isEdit = ref(false)
const wishId = ref(null)

const form = ref({
  content: '',
  type: 'item',
  priority: 2,
  visibility: 'family',
  expectDate: '',
  expectTime: '',
  description: '',
  images: []
})

// ==================== 优化后的7种心愿类型 - 更温馨的图标和配色 ====================
const wishTypes = [
  { label: '心动好物', value: 'item', icon: '🎁', bgColor: 'linear-gradient(135deg, #ffd3b6, #ffaaa5)' },
  { label: '浪漫体验', value: 'experience', icon: '🌅', bgColor: 'linear-gradient(135deg, #a8e6cf, #7fcdbb)' },
  { label: '学习成长', value: 'learning', icon: '📖', bgColor: 'linear-gradient(135deg, #c7ceea, #a8b2e6)' },
  { label: '梦想目标', value: 'goal', icon: '🌟', bgColor: 'linear-gradient(135deg, #ffd93d, #ffc93d)' },
  { label: '亲情友情', value: 'relationship', icon: '💕', bgColor: 'linear-gradient(135deg, #ff8fa3, #ffc2d1)' },
  { label: '温暖公益', value: 'charity', icon: '🌻', bgColor: 'linear-gradient(135deg, #b8e6b8, #98d498)' },
  { label: '其他心愿', value: 'custom', icon: '💫', bgColor: 'linear-gradient(135deg, #e0e0e0, #d0d0d0)' }
]

// 优先级 - 温馨图标
const priorities = [
  { label: '心愿优先', value: 3, icon: '🔥' },
  { label: '一般心愿', value: 2, icon: '⭐' },
  { label: '随缘心愿', value: 1, icon: '🌙' }
]

// 可见性 - 带图标
const visibility = [
  { label: '仅自己', value: 'private', icon: '🔒' },
  { label: '伴侣', value: 'partner', icon: '💑' },
  { label: '家庭', value: 'family', icon: '👨‍👩‍👧‍👦' },
  { label: '公开', value: 'public', icon: '🌍' }
]

// ==================== 10分钟间隔时间选择器配置 ====================
const hours = Array.from({ length: 24 }, (_, i) => String(i).padStart(2, '0') + '时')
const minutes = Array.from({ length: 6 }, (_, i) => String(i * 10).padStart(2, '0') + '分')
const timeRange = [hours, minutes]
const timeIndex = ref([8, 0]) // 默认08:00

onMounted(() => {
  // 检查是否为编辑模式
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const options = currentPage.options || currentPage.$route?.query || {}
  
  if (options.id) {
    isEdit.value = true
    wishId.value = options.id
    loadWishDetail(options.id)
  }
})

const loadWishDetail = async (id) => {
  try {
    uni.showLoading({ title: '加载中...' })
    const res = await wishApi.getDetail(id)
    
    if (res) {
      form.value = {
        content: res.title || '',
        type: res.type || 'item',
        priority: res.priority || 2,
        visibility: res.visibility || 'family',
        expectDate: res.expectDate || '',
        expectTime: res.expectTime || '',
        description: res.description || '',
        images: res.images || []
      }
      
      // 设置时间索引
      if (res.expectTime) {
        const [hour, minute] = res.expectTime.split(':')
        timeIndex.value = [parseInt(hour), parseInt(minute) / 10]
      }
    }
    
    uni.hideLoading()
  } catch (e) {
    console.error('加载心愿详情失败', e)
    uni.hideLoading()
    uni.showToast({ title: '加载失败', icon: 'none' })
  }
}

const goBack = () => {
  uni.navigateBack()
}

const onDateChange = (e) => {
  form.value.expectDate = e.detail.value
  // 默认选择早上8点
  if (!form.value.expectTime) {
    form.value.expectTime = '08:00'
    timeIndex.value = [8, 0]
  }
}

// ==================== 时间选择器方法（10分钟间隔） ====================
const onTimeChange = (e) => {
  const [hourIdx, minuteIdx] = e.detail.value
  const hour = String(hourIdx).padStart(2, '0')
  const minute = String(minuteIdx * 10).padStart(2, '0')
  form.value.expectTime = `${hour}:${minute}`
  timeIndex.value = [hourIdx, minuteIdx]
}

const onTimeColumnChange = (e) => {
  const { column, value } = e.detail
  timeIndex.value[column] = value
}

const chooseImage = () => {
  const remainCount = 3 - form.value.images.length
  if (remainCount <= 0) {
    uni.showToast({ title: '最多3张图片', icon: 'none' })
    return
  }
  
  uni.chooseImage({
    count: remainCount,
    success: (res) => {
      form.value.images.push(...res.tempFilePaths)
    }
  })
}

const removeImage = (index) => {
  form.value.images.splice(index, 1)
}

const saveWish = async () => {
  if (!form.value.content.trim()) {
    uni.showToast({ title: '请输入心愿内容', icon: 'none' })
    return
  }
  
  try {
    uni.showLoading({ title: isEdit.value ? '保存中...' : '许愿中...' })
    
    const familyId = uni.getStorageSync('currentFamilyId') || 1
    const userInfo = uni.getStorageSync('userInfo')
    
    const data = {
      title: form.value.content.trim(),
      description: form.value.description.trim(),
      type: form.value.type,
      priority: form.value.priority,
      expectDate: form.value.expectDate || null,
      expectTime: form.value.expectTime || null,
      visibility: form.value.visibility,
      images: form.value.images,
      familyId: familyId,
      userId: userInfo?.id
    }
    
    if (isEdit.value && wishId.value) {
      await wishApi.update({
        id: wishId.value,
        ...data
      })
      uni.showToast({ title: '✨ 更新成功', icon: 'none' })
    } else {
      await wishApi.create(data)
      uni.showToast({ title: '✨ 心愿已许下', icon: 'none', duration: 2000 })
    }
    
    setTimeout(() => {
      uni.navigateBack()
    }, 1500)
  } catch (e) {
    console.error('保存心愿失败', e)
    uni.hideLoading()
    uni.showToast({ title: '保存失败', icon: 'none' })
  }
}
</script>

<style lang="scss" scoped>
// 温馨小清新配色变量
$primary-color: #ff8fa3;
$primary-light: #ffc2d1;
$primary-soft: #fff0f3;
$secondary-color: #a8e6cf;
$accent-color: #ffd3b6;
$text-primary: #4a4a4a;
$text-secondary: #666;
$text-light: #999;
$bg-color: #fef9f9;
$card-bg: #fff;
$border-radius-sm: 16rpx;
$border-radius-md: 24rpx;
$border-radius-lg: 40rpx;
$border-radius-xl: 60rpx;

.create-wish-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #fff0f3 0%, #fef9f9 100%);
}

.nav-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 30rpx;
  padding-top: 60rpx;
  background: linear-gradient(135deg, #fff 0%, #fff8f9 100%);
  box-shadow: 0 2rpx 20rpx rgba(255, 143, 163, 0.1);
  
  .back-btn {
    width: 64rpx;
    height: 64rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    background: linear-gradient(135deg, #fff0f3 0%, #ffe8ed 100%);
    border-radius: 50%;
    transition: all 0.2s;
    
    &:active {
      transform: scale(0.95);
    }
  }
  
  .title-wrapper {
    display: flex;
    align-items: center;
    gap: 12rpx;
  }
  
  .title-icon {
    font-size: 36rpx;
  }
  
  .title {
    font-size: 34rpx;
    font-weight: 700;
    color: $text-primary;
    letter-spacing: 2rpx;
    background: linear-gradient(135deg, #ff8fa3, #ffc2d1);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
  }
  
  .right-btn {
    padding: 16rpx 40rpx;
    background: linear-gradient(135deg, #ff8fa3, #ffc2d1);
    border-radius: 32rpx;
    box-shadow: 0 4rpx 20rpx rgba(255, 143, 163, 0.35);
    transition: all 0.2s;
    
    &:active {
      transform: scale(0.95);
    }
    
    &.disabled {
      opacity: 0.5;
      pointer-events: none;
    }
    
    text {
      font-size: 28rpx;
      color: #fff;
      font-weight: 600;
      letter-spacing: 2rpx;
    }
  }
}

.form-container {
  padding: 30rpx;
}

// ==================== 温馨圆润表单样式 ====================
.form-item-warm {
  background: linear-gradient(135deg, #fff 0%, #fff8f9 100%);
  border-radius: $border-radius-md;
  padding: 36rpx;
  margin-bottom: 28rpx;
  box-shadow: 0 4rpx 24rpx rgba(255, 143, 163, 0.08);
  border: 2rpx solid rgba(255, 194, 209, 0.2);
}

.form-label-wrapper {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 20rpx;
}

.form-label-icon {
  font-size: 32rpx;
}

.label {
  font-size: 30rpx;
  font-weight: 600;
  color: $text-primary;
  letter-spacing: 1rpx;
  
  .required {
    color: #ff8fa3;
  }
}

// 输入框样式
.input-wrapper {
  background: #f8f8f8;
  border-radius: 20rpx;
  padding: 4rpx;
  box-shadow: inset 0 1rpx 4rpx rgba(0, 0, 0, 0.05);
}

.input {
  font-size: 30rpx;
  color: $text-primary;
  height: 88rpx;
  padding: 0 28rpx;
  background: #fff;
  border-radius: 16rpx;
  width: 100%;
  box-sizing: border-box;
}

// 文本域样式
.textarea-wrapper {
  background: #f8f8f8;
  border-radius: 20rpx;
  padding: 4rpx;
  box-shadow: inset 0 1rpx 4rpx rgba(0, 0, 0, 0.05);
  position: relative;
}

.textarea {
  font-size: 30rpx;
  color: $text-primary;
  height: 200rpx;
  width: 100%;
  padding: 24rpx 28rpx;
  background: #fff;
  border-radius: 16rpx;
  box-sizing: border-box;
}

.char-count-warm {
  position: absolute;
  bottom: 16rpx;
  right: 20rpx;
  font-size: 22rpx;
  color: $text-light;
  background: rgba(255, 255, 255, 0.9);
  padding: 4rpx 12rpx;
  border-radius: 12rpx;
}

// ==================== 心愿类型选择 - 新样式 ====================
.type-list {
  display: flex;
  flex-wrap: wrap;
  gap: 20rpx;
}

.type-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10rpx;
  padding: 16rpx 12rpx;
  background: linear-gradient(135deg, #fff 0%, #fafafa 100%);
  border-radius: 20rpx;
  border: 3rpx solid transparent;
  transition: all 0.25s cubic-bezier(0.34, 1.56, 0.64, 1);
  min-width: 110rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
  
  &:active {
    transform: scale(0.95);
  }
  
  &.active {
    border-color: #ff8fa3;
    background: linear-gradient(135deg, #fff0f3 0%, #ffe8ed 100%);
    box-shadow: 0 4rpx 16rpx rgba(255, 143, 163, 0.2);
    transform: translateY(-4rpx);
  }
}

.type-icon-wrapper {
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
}

.icon {
  font-size: 32rpx;
}

.name {
  font-size: 22rpx;
  color: $text-secondary;
  font-weight: 500;
}

.type-item.active .name {
  color: #ff8fa3;
  font-weight: 600;
}

// ==================== 优先级选择 ====================
.priority-list {
  display: flex;
  gap: 20rpx;
}

.priority-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10rpx;
  padding: 20rpx 12rpx;
  background: linear-gradient(135deg, #fff 0%, #fafafa 100%);
  border-radius: 20rpx;
  border: 3rpx solid transparent;
  transition: all 0.25s cubic-bezier(0.34, 1.56, 0.64, 1);
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
  
  &:active {
    transform: scale(0.95);
  }
  
  &.active {
    border-color: #ff8fa3;
    background: linear-gradient(135deg, #fff0f3 0%, #ffe8ed 100%);
    box-shadow: 0 4rpx 16rpx rgba(255, 143, 163, 0.2);
    transform: translateY(-4rpx);
  }
}

.priority-item .icon {
  font-size: 36rpx;
}

.priority-item .name {
  font-size: 24rpx;
}

.priority-item.active .name {
  color: #ff8fa3;
  font-weight: 600;
}

// ==================== 日期时间选择器 ====================
.datetime-picker {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 26rpx 32rpx;
  background: #f8f8f8;
  border-radius: 20rpx;
  border: 2rpx solid transparent;
  transition: all 0.2s;
  box-shadow: inset 0 1rpx 4rpx rgba(0, 0, 0, 0.05);
  
  &.active {
    border-color: #ffc2d1;
    background: linear-gradient(135deg, #fff0f3 0%, #fff 100%);
  }
}

.datetime-icon {
  font-size: 32rpx;
}

.datetime-text {
  flex: 1;
  font-size: 30rpx;
  color: $text-primary;
  font-weight: 500;
}

.datetime-arrow {
  font-size: 32rpx;
  color: #ffc2d1;
  font-weight: 600;
}

// 时间选择区域
.time-picker-section {
  margin-top: 24rpx;
  padding-top: 24rpx;
  border-top: 2rpx dashed rgba(255, 194, 209, 0.4);
}

.time-label {
  display: block;
  font-size: 26rpx;
  color: $text-secondary;
  margin-bottom: 16rpx;
  margin-left: 8rpx;
}

.time-picker {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 24rpx 32rpx;
  background: #f8f8f8;
  border-radius: 20rpx;
  border: 2rpx solid transparent;
  transition: all 0.2s;
  box-shadow: inset 0 1rpx 4rpx rgba(0, 0, 0, 0.05);
  
  &.active {
    border-color: #ffc2d1;
    background: linear-gradient(135deg, #fff0f3 0%, #fff 100%);
  }
}

.time-icon {
  font-size: 32rpx;
}

.time-text {
  flex: 1;
  font-size: 30rpx;
  color: $text-primary;
  font-weight: 500;
}

.time-arrow {
  font-size: 32rpx;
  color: #ffc2d1;
  font-weight: 600;
}

// ==================== 可见性选择 ====================
.visibility-list {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
}

.visibility-item {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 18rpx 28rpx;
  background: linear-gradient(135deg, #fff 0%, #fafafa 100%);
  border-radius: $border-radius-xl;
  font-size: 26rpx;
  color: $text-secondary;
  transition: all 0.2s;
  border: 2rpx solid transparent;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
  
  &:active {
    transform: scale(0.95);
  }
  
  &.active {
    background: linear-gradient(135deg, #fff0f3 0%, #ffe8ed 100%);
    border-color: $primary-color;
    color: $primary-color;
    box-shadow: 0 4rpx 16rpx rgba(255, 143, 163, 0.2);
  }
  
  .icon {
    font-size: 28rpx;
  }
  
  .name {
    font-size: 26rpx;
    font-weight: 500;
  }
}

.visibility-item.active .name {
  font-weight: 600;
}

// ==================== 图片上传 ====================
.image-list {
  display: flex;
  flex-wrap: wrap;
  gap: 20rpx;
}

.image-item {
  width: 180rpx;
  height: 180rpx;
  border-radius: $border-radius-sm;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.08);
  
  &.add {
    border: 3rpx dashed #ffc2d1;
    background: linear-gradient(135deg, #fff0f3 0%, #fff8f9 100%);
    transition: all 0.2s;
    
    &:active {
      background: linear-gradient(135deg, #ffe8ed 0%, #fff0f3 100%);
      transform: scale(0.95);
    }
    
    .add-icon {
      font-size: 60rpx;
      color: #ff8fa3;
      font-weight: 300;
    }
  }
  
  .uploaded-img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
  
  .delete-img {
    position: absolute;
    top: 8rpx;
    right: 8rpx;
    width: 44rpx;
    height: 44rpx;
    background: rgba(255, 143, 163, 0.9);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.2);
    
    text {
      font-size: 22rpx;
      color: #fff;
      font-weight: 600;
    }
  }
}

.image-tip {
  display: block;
  font-size: 24rpx;
  color: $text-light;
  margin-top: 20rpx;
  margin-left: 8rpx;
}

// ==================== Picker 层级优化 ====================
picker {
  position: relative !important;
  z-index: 1000 !important;
}

picker-view {
  z-index: 1000 !important;
}

uni-picker {
  z-index: 1000 !important;
}

.uni-picker-container {
  z-index: 1001 !important;
}

picker-view-column {
  z-index: 1000 !important;
}

.datetime-picker,
.time-picker {
  position: relative !important;
  z-index: 999 !important;
}
</style>