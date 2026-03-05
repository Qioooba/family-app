<template>
  <view class="create-task-page">
    <view class="header">
      <text class="title">新建任务</text>
      <text class="close" @click="goBack">✕</text>
    </view>
    
    <view class="form">
      <view class="form-item">
        <text class="label">任务标题</text>
        <input 
          v-model="form.title" 
          class="input" 
          placeholder="请输入任务标题"
          maxlength="50"
        />
      </view>
      
      <view class="form-item">
        <text class="label">任务内容</text>
        <textarea 
          v-model="form.content" 
          class="textarea" 
          placeholder="请输入任务内容（可选）"
          maxlength="500"
        />
      </view>
      
      <view class="form-item">
        <text class="label">截止时间</text>
        <picker mode="multiSelector" :range="dateRange" :value="dateIndex" @change="onDateChange">
          <view class="picker">{{ form.dueTime || '请选择截止时间' }}</view>
        </picker>
      </view>
      
      <view class="form-item">
        <text class="label">优先级</text>
        <view class="priority-list">
          <view 
            v-for="(item, index) in priorities" 
            :key="index"
            class="priority-item"
            :class="{ active: form.priority === item.value }"
            @click="form.priority = item.value"
          >
            <text>{{ item.label }}</text>
          </view>
        </view>
      </view>
      
      <button class="submit-btn" :disabled="!form.title || loading" @click="submit">
        {{ loading ? '创建中...' : '创建任务' }}
      </button>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { taskApi } from '../../api/index'

const loading = ref(false)

const form = reactive({
  title: '',
  content: '',
  dueTime: '',
  priority: 0
})

const priorities = [
  { label: '普通', value: 0 },
  { label: '重要', value: 1 },
  { label: '紧急', value: 2 }
]

// 日期选择器数据
const dateRange = [
  ['2024', '2025', '2026'],
  ['01', '02', '03', '04', '05', '06', '07', '08', '09', '10', '11', '12'],
  ['01', '02', '03', '04', '05', '06', '07', '08', '09', '10', '11', '12', '13', '14', '15', '16', '17', '18', '19', '20', '21', '22', '23', '24', '25', '26', '27', '28', '29', '30', '31'],
  ['00', '01', '02', '03', '04', '05', '06', '07', '08', '09', '10', '11', '12', '13', '14', '15', '16', '17', '18', '19', '20', '21', '22', '23'],
  ['00', '15', '30', '45']
]

const dateIndex = ref([2, 2, 4, 14, 0])

const onDateChange = (e) => {
  const value = e.detail.value
  const year = dateRange[0][value[0]]
  const month = dateRange[1][value[1]]
  const day = dateRange[2][value[2]]
  const hour = dateRange[3][value[3]]
  const minute = dateRange[4][value[4]]
  form.dueTime = `${year}-${month}-${day} ${hour}:${minute}`
}

const goBack = () => {
  uni.navigateBack()
}

const submit = async () => {
  if (!form.title.trim()) {
    uni.showToast({ title: '请输入任务标题', icon: 'none' })
    return
  }
  
  loading.value = true
  try {
    const familyId = uni.getStorageSync('currentFamilyId') || 1
    await taskApi.create({
      ...form,
      familyId,
      status: 0
    })
    uni.showToast({ title: '创建成功', icon: 'success' })
    setTimeout(() => {
      uni.navigateBack()
    }, 500)
  } catch (e) {
    uni.showToast({ title: '创建失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.create-task-page {
  min-height: 100vh;
  background: #f5f5f5;
  
  .header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 40rpx 32rpx;
    background: #fff;
    
    .title {
      font-size: 36rpx;
      font-weight: 600;
      color: #333;
    }
    
    .close {
      font-size: 40rpx;
      color: #999;
      padding: 8rpx;
    }
  }
  
  .form {
    padding: 32rpx;
    
    .form-item {
      margin-bottom: 32rpx;
      background: #fff;
      border-radius: 16rpx;
      padding: 24rpx;
      
      .label {
        font-size: 28rpx;
        color: #666;
        margin-bottom: 16rpx;
        display: block;
      }
      
      .input {
        width: 100%;
        height: 80rpx;
        background: #f5f5f5;
        border-radius: 12rpx;
        padding: 0 24rpx;
        font-size: 30rpx;
      }
      
      .textarea {
        width: 100%;
        height: 200rpx;
        background: #f5f5f5;
        border-radius: 12rpx;
        padding: 24rpx;
        font-size: 30rpx;
      }
      
      .picker {
        height: 80rpx;
        line-height: 80rpx;
        background: #f5f5f5;
        border-radius: 12rpx;
        padding: 0 24rpx;
        font-size: 30rpx;
        color: #333;
      }
      
      .priority-list {
        display: flex;
        gap: 20rpx;
        
        .priority-item {
          flex: 1;
          height: 70rpx;
          display: flex;
          align-items: center;
          justify-content: center;
          background: #f5f5f5;
          border-radius: 12rpx;
          font-size: 28rpx;
          color: #666;
          
          &.active {
            background: linear-gradient(135deg, #6B8DD6, #8B5CF6);
            color: #fff;
          }
        }
      }
    }
    
    .submit-btn {
      width: 100%;
      height: 90rpx;
      background: linear-gradient(135deg, #6B8DD6, #8B5CF6);
      color: #fff;
      border-radius: 16rpx;
      font-size: 32rpx;
      font-weight: 600;
      margin-top: 40rpx;
      
      &[disabled] {
        opacity: 0.6;
      }
    }
  }
}
</style>
