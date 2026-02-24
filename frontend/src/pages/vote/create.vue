<template>
  <view class="create-vote-page">
    <view class="nav-bar">
      <view class="back-btn" @click="goBack">
        <up-icon name="arrow-left" size="40" color="#333"></up-icon>
      </view>
      <text class="title">发起投票</text>
      <view class="right-btn" @click="submitVote">
        <text>发布</text>
      </view>
    </view>
    
    <view class="form-container">
      <!-- 投票标题 -->
      <view class="form-item">
        <text class="label">投票主题</text>
        <input v-model="form.title" placeholder="例如：周末去哪里玩？" class="input" />
      </view>
      
      <!-- 投票说明 -->
      <view class="form-item">
        <text class="label">投票说明</text>
        <textarea 
          v-model="form.description" 
          placeholder="补充说明投票的背景和目的..."
          class="textarea"
        />
      </view>
      
      <!-- 选项 -->
      <view class="form-item">
        <view class="option-header">
          <text class="label">投票选项</text>
          <text class="tip">至少2个选项</text>
        </view>
        
        <view class="option-list">
          <view 
            v-for="(option, index) in form.options" 
            :key="index"
            class="option-item"
          >
            <view class="option-index">{{ String.fromCharCode(65 + index) }}</view>
            <input 
              v-model="form.options[index]" 
              :placeholder="`选项 ${index + 1}`"
              class="option-input"
            />
            
            <view 
              v-if="form.options.length > 2"
              class="delete-btn"
              @click="removeOption(index)"
            >
              <up-icon name="close" size="32" color="#999"></up-icon>
            </view>
          </view>
        </view>
        
        <view class="add-option" @click="addOption">
          <up-icon name="plus" size="32" color="#5B8FF9"></up-icon>
          <text>添加选项</text>
        </view>
      </view>
      
      <!-- 投票类型 -->
      <view class="form-item">
        <text class="label">投票类型</text>
        <view class="type-list">
          <view 
            v-for="t in voteTypes" 
            :key="t.value"
            class="type-item"
            :class="{ active: form.type === t.value }"
            @click="form.type = t.value"
          >
            {{ t.label }}
          </view>
        </view>
      </view>
      
      <!-- 截止时间 -->
      <view class="form-item">
        <text class="label">截止时间</text>
        <view class="deadline-list">
          <view 
            v-for="d in deadlines" 
            :key="d.value"
            class="deadline-item"
            :class="{ active: form.deadline === d.value }"
            @click="form.deadline = d.value"
          >
            {{ d.label }}
          </view>
        </view>
      </view>
      
      <!-- 匿名投票 -->
      <view class="form-item">
        <view class="switch-item">
          <text class="label">匿名投票</text>
          <switch :checked="form.anonymous" @change="form.anonymous = !form.anonymous" color="#5B8FF9" />
        </view>
        <text class="tip-text">开启后，投票结果不会显示投票人</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'

const form = ref({
  title: '',
  description: '',
  options: ['', ''],
  type: 'single',
  deadline: '1d',
  anonymous: false
})

const voteTypes = [
  { label: '单选', value: 'single' },
  { label: '多选', value: 'multiple' },
  { label: '评分', value: 'rating' }
]

const deadlines = [
  { label: '1天后', value: '1d' },
  { label: '3天后', value: '3d' },
  { label: '7天后', value: '7d' },
  { label: '30天后', value: '30d' }
]

const goBack = () => {
  uni.navigateBack()
}

const addOption = () => {
  if (form.value.options.length >= 8) {
    uni.showToast({ title: '最多8个选项', icon: 'none' })
    return
  }
  form.value.options.push('')
}

const removeOption = (index) => {
  form.value.options.splice(index, 1)
}

const submitVote = () => {
  if (!form.value.title.trim()) {
    uni.showToast({ title: '请输入投票主题', icon: 'none' })
    return
  }
  
  const validOptions = form.value.options.filter(o => o.trim())
  if (validOptions.length < 2) {
    uni.showToast({ title: '请至少填写2个选项', icon: 'none' })
    return
  }
  
  uni.showToast({ title: '发布成功', icon: 'success' })
  setTimeout(() => uni.navigateBack(), 1500)
}
</script>

<style lang="scss" scoped>
.create-vote-page {
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
    background: #E8684A;
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
  }
  
  .input {
    font-size: 30rpx;
    color: #333;
    height: 60rpx;
    margin-top: 20rpx;
  }
  
  .textarea {
    font-size: 28rpx;
    color: #333;
    height: 120rpx;
    width: 100%;
    margin-top: 20rpx;
  }
}

.option-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
  
  .tip {
    font-size: 24rpx;
    color: #999;
  }
}

.option-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
  
  .option-item {
    display: flex;
    align-items: center;
    gap: 16rpx;
    background: #f5f5f5;
    border-radius: 12rpx;
    padding: 16rpx 20rpx;
    
    .option-index {
      width: 48rpx;
      height: 48rpx;
      background: #5B8FF9;
      color: #fff;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 24rpx;
      font-weight: 600;
    }
    
    .option-input {
      flex: 1;
      font-size: 28rpx;
      background: transparent;
    }
    
    .delete-btn {
      padding: 10rpx;
    }
  }
}

.add-option {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  padding: 30rpx;
  margin-top: 20rpx;
  border: 2rpx dashed #ddd;
  border-radius: 12rpx;
  
  text {
    font-size: 28rpx;
    color: #5B8FF9;
  }
}

.type-list, .deadline-list {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
  margin-top: 20rpx;
  
  .type-item, .deadline-item {
    padding: 16rpx 32rpx;
    background: #f5f5f5;
    border-radius: 30rpx;
    font-size: 26rpx;
    color: #666;
    
    &.active {
      background: #E8684A;
      color: #fff;
    }
  }
}

.switch-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.tip-text {
  font-size: 24rpx;
  color: #999;
  margin-top: 16rpx;
  display: block;
}
</style>