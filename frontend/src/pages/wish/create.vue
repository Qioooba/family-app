<template>
  <view class="create-wish-page">
    <view class="nav-bar">
      <view class="back-btn" @click="goBack">
        <u-icon name="arrow-left" size="40" color="#333"></u-icon>
      </view>
      <text class="title">添加心愿</text>
      <view class="right-btn" @click="saveWish">
        <text>保存</text>
      </view>
    </view>
    
    <view class="form-container">
      <!-- 心愿内容 -->
      <view class="form-item">
        <text class="label">心愿内容</text>
        <input v-model="form.content" placeholder="写下你的心愿..." class="input" />
      </view>
      
      <!-- 心愿类型 -->
      <view class="form-item">
        <text class="label">类型</text>
        <view class="type-list">
          <view 
            v-for="type in wishTypes" 
            :key="type.value"
            class="type-item"
            :class="{ active: form.type === type.value }"
            @click="form.type = type.value"
          >
            <text class="icon">{{ type.icon }}</text>
            {{ type.label }}
          </view>
        </view>
      </view>
      
      <!-- 可见性 -->
      <view class="form-item">
        <text class="label">可见范围</text>
        <view class="visibility-list">
          <view 
            v-for="v in visibility" 
            :key="v.value"
            class="visibility-item"
            :class="{ active: form.visibility === v.value }"
            @click="form.visibility = v.value"
          >
            {{ v.label }}
          </view>
        </view>
      </view>
      
      <!-- 预算 -->
      <view class="form-item">
        <text class="label">预计花费</text>
        <view class="budget-input">
          <text class="prefix">¥</text>
          <input 
            v-model="form.budget" 
            type="digit" 
            placeholder="0.00" 
            class="input"
          />
        </view>
      </view>
      
      <!-- 描述 -->
      <view class="form-item">
        <text class="label">详细描述</text>
        <textarea 
          v-model="form.description" 
          placeholder="详细描述你的心愿，让更多人了解..."
          class="textarea"
        />
      </view>
      
      <!-- 图片 -->
      <view class="form-item">
        <text class="label">添加图片</text>
        <view class="image-list">
          <view class="image-item add" @click="chooseImage">
            <u-icon name="plus" size="48" color="#ccc"></u-icon>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'

const form = ref({
  content: '',
  type: 'item',
  visibility: 'family',
  budget: '',
  description: '',
  images: []
})

const wishTypes = [
  { label: '物品', value: 'item', icon: '🎁' },
  { label: '体验', value: 'experience', icon: '✈️' },
  { label: '学习', value: 'learning', icon: '📚' },
  { label: '关系', value: 'relationship', icon: '💝' }
]

const visibility = [
  { label: '仅自己', value: 'private' },
  { label: '伴侣', value: 'partner' },
  { label: '家庭成员', value: 'family' },
  { label: '公开', value: 'public' }
]

const goBack = () => {
  uni.navigateBack()
}

const chooseImage = () => {
  uni.chooseImage({
    count: 1,
    success: (res) => {
      form.value.images.push(res.tempFilePaths[0])
    }
  })
}

const saveWish = () => {
  if (!form.value.content.trim()) {
    uni.showToast({ title: '请输入心愿内容', icon: 'none' })
    return
  }
  
  uni.showToast({ title: '添加成功', icon: 'success' })
  setTimeout(() => uni.navigateBack(), 1500)
}
</script>

<style lang="scss" scoped>
.create-wish-page {
  min-height: 100vh;
  background: #f5f6fa;
}

.nav-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 30rpx;
  padding-top: 60rpx;
  background: #fff;
  
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
    color: #333;
  }
  
  .right-btn {
    padding: 12rpx 30rpx;
    background: #F6BD16;
    border-radius: 30rpx;
    
    text {
      font-size: 28rpx;
      color: #fff;
    }
  }
}

.form-container {
  padding: 30rpx;
}

.form-item {
  background: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
  
  .label {
    font-size: 28rpx;
    font-weight: 600;
    color: #333;
    display: block;
    margin-bottom: 20rpx;
  }
  
  .input {
    font-size: 30rpx;
    color: #333;
    height: 60rpx;
  }
  
  .textarea {
    font-size: 28rpx;
    color: #333;
    height: 160rpx;
    width: 100%;
  }
}

.type-list {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
  
  .type-item {
    display: flex;
    align-items: center;
    gap: 8rpx;
    padding: 16rpx 32rpx;
    background: #f5f5f5;
    border-radius: 30rpx;
    font-size: 26rpx;
    color: #666;
    
    .icon {
      font-size: 32rpx;
    }
    
    &.active {
      background: #F6BD16;
      color: #fff;
    }
  }
}

.visibility-list {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
  
  .visibility-item {
    padding: 16rpx 32rpx;
    background: #f5f5f5;
    border-radius: 30rpx;
    font-size: 26rpx;
    color: #666;
    
    &.active {
      background: #F6BD16;
      color: #fff;
    }
  }
}

.budget-input {
  display: flex;
  align-items: center;
  gap: 12rpx;
  
  .prefix {
    font-size: 32rpx;
    color: #333;
    font-weight: 600;
  }
  
  .input {
    flex: 1;
  }
}

.image-list {
  display: flex;
  flex-wrap: wrap;
  gap: 20rpx;
  
  .image-item {
    width: 160rpx;
    height: 160rpx;
    border-radius: 16rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    
    &.add {
      border: 2rpx dashed #ddd;
      background: #fafafa;
    }
  }
}
</style>