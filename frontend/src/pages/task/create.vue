<template>
  <view class="create-task-page">
    <!-- 顶部导航 -->
    <view class="nav-bar">
      <view class="back-btn" @click="goBack">
        <u-icon name="arrow-left" size="40" color="#333"></u-icon>
      </view>
      <text class="title">创建任务</text>
      <view class="right-btn" @click="saveTask">
        <text>保存</text>
      </view>
    </view>
    
    <view class="form-container">
      <!-- 任务标题 -->
      <view class="form-item">
        <text class="label">任务标题</text>
        <input 
          v-model="form.title" 
          placeholder="请输入任务标题" 
          class="input"
        />
      </view>
      
      <!-- 任务分类 -->
      <view class="form-item">
        <text class="label">分类</text>
        <view class="category-list">
          <view 
            v-for="cat in categories" 
            :key="cat.value"
            class="category-item"
            :class="{ active: form.category === cat.value }"
            @click="form.category = cat.value"
          >
            {{ cat.label }}
          </view>
        </view>
      </view>
      
      <!-- 优先级 -->
      <view class="form-item">
        <text class="label">优先级</text>
        <view class="priority-list">
          <view 
            v-for="p in priorities" 
            :key="p.value"
            class="priority-item"
            :class="{ active: form.priority === p.value, [p.class]: true }"
            @click="form.priority = p.value"
          >
            <view class="dot"></view>
            {{ p.label }}
          </view>
        </view>
      </view>
      
      <!-- 截止时间 -->
      <view class="form-item">
        <text class="label">截止时间</text>
        <picker mode="multiSelector" :range="dateRange" :value="dateIndex" @change="onDateChange">
          <view class="picker-value">
            {{ form.deadline || '请选择截止时间' }}
            <u-icon name="arrow-right" size="28" color="#999"></u-icon>
          </view>
        </picker>
      </view>
      
      <!-- 指派给 -->
      <view class="form-item">
        <text class="label">指派给</text>
        <view class="member-list">
          <view 
            v-for="member in members" 
            :key="member.id"
            class="member-item"
            :class="{ active: form.assigneeId === member.id }"
            @click="form.assigneeId = member.id"
          >
            <image :src="member.avatar" class="avatar" />
            <text class="name">{{ member.name }}</text>
          </view>
        </view>
      </view>
      
      <!-- 备注 -->
      <view class="form-item">
        <text class="label">备注</text>
        <textarea 
          v-model="form.remark" 
          placeholder="添加备注说明..."
          class="textarea"
        />
      </view>
      
      <!-- 重复设置 -->
      <view class="form-item">
        <text class="label">重复</text>
        <view class="repeat-list">
          <view 
            v-for="r in repeats" 
            :key="r.value"
            class="repeat-item"
            :class="{ active: form.repeat === r.value }"
            @click="form.repeat = r.value"
          >
            {{ r.label }}
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { taskApi } from '../../api/index.js'

const form = ref({
  title: '',
  category: 'shopping',
  priority: 0,
  deadline: '',
  assigneeId: null,
  remark: '',
  repeat: 'none'
})

const categories = [
  { label: '购物', value: 'shopping' },
  { label: '家务', value: 'housework' },
  { label: '财务', value: 'finance' },
  { label: '育儿', value: 'parenting' },
  { label: '其他', value: 'other' }
]

const priorities = [
  { label: '普通', value: 0, class: 'normal' },
  { label: '重要', value: 1, class: 'medium' },
  { label: '紧急', value: 2, class: 'high' }
]

const repeats = [
  { label: '不重复', value: 'none' },
  { label: '每天', value: 'daily' },
  { label: '每周', value: 'weekly' },
  { label: '每月', value: 'monthly' }
]

const members = ref([
  { id: 1, name: '爸爸', avatar: '/static/avatar/dad.png' },
  { id: 2, name: '妈妈', avatar: '/static/avatar/mom.png' },
  { id: 3, name: '宝贝', avatar: '/static/avatar/kid.png' }
])

// 生成日期选择器数据
const dateRange = computed(() => {
  const years = []
  const months = []
  const days = []
  const hours = []
  const minutes = ['00', '30']
  
  const now = new Date()
  for (let i = now.getFullYear(); i <= now.getFullYear() + 1; i++) {
    years.push(i + '年')
  }
  for (let i = 1; i <= 12; i++) {
    months.push(i + '月')
  }
  for (let i = 1; i <= 31; i++) {
    days.push(i + '日')
  }
  for (let i = 0; i < 24; i++) {
    hours.push(i + '时')
  }
  
  return [years, months, days, hours, minutes]
})

const dateIndex = ref([0, 0, 0, 0, 0])

const onDateChange = (e) => {
  const val = e.detail.value
  const year = dateRange.value[0][val[0]].replace('年', '')
  const month = dateRange.value[1][val[1]].replace('月', '').padStart(2, '0')
  const day = dateRange.value[2][val[2]].replace('日', '').padStart(2, '0')
  const hour = dateRange.value[3][val[3]].replace('时', '').padStart(2, '0')
  const minute = dateRange.value[4][val[4]]
  
  form.value.deadline = `${year}-${month}-${day} ${hour}:${minute}`
}

const goBack = () => {
  uni.navigateBack()
}

const saveTask = async () => {
  if (!form.value.title.trim()) {
    uni.showToast({ title: '请输入任务标题', icon: 'none' })
    return
  }
  
  try {
    const familyId = uni.getStorageSync('currentFamilyId') || 1
    const data = {
      title: form.value.title,
      familyId: familyId,
      category: form.value.category,
      priority: form.value.priority,
      deadline: form.value.deadline,
      assigneeId: form.value.assigneeId,
      remark: form.value.remark,
      repeat: form.value.repeat,
      status: 0
    }
    await taskApi.create(data)
    uni.showToast({ title: '创建成功', icon: 'success' })
    setTimeout(() => {
      uni.navigateBack()
    }, 1500)
  } catch (e) {
    console.error('创建任务失败', e)
    uni.showToast({ title: '创建失败', icon: 'none' })
  }
}
</script>

<style lang="scss" scoped>
.create-task-page {
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
    background: #5B8FF9;
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
  
  .picker-value {
    display: flex;
    align-items: center;
    justify-content: space-between;
    font-size: 30rpx;
    color: #333;
  }
}

.category-list {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
  
  .category-item {
    padding: 16rpx 32rpx;
    background: #f5f5f5;
    border-radius: 30rpx;
    font-size: 26rpx;
    color: #666;
    
    &.active {
      background: #5B8FF9;
      color: #fff;
    }
  }
}

.priority-list {
  display: flex;
  gap: 20rpx;
  
  .priority-item {
    display: flex;
    align-items: center;
    gap: 12rpx;
    padding: 16rpx 30rpx;
    background: #f5f5f5;
    border-radius: 30rpx;
    font-size: 26rpx;
    color: #666;
    
    .dot {
      width: 16rpx;
      height: 16rpx;
      border-radius: 50%;
      background: #ddd;
    }
    
    &.active {
      &.normal {
        background: #E6F7FF;
        color: #1890FF;
        .dot { background: #1890FF; }
      }
      &.medium {
        background: #FFF7E6;
        color: #FAAD14;
        .dot { background: #FAAD14; }
      }
      &.high {
        background: #FFF1F0;
        color: #FF4D4F;
        .dot { background: #FF4D4F; }
      }
    }
  }
}

.member-list {
  display: flex;
  gap: 30rpx;
  
  .member-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    opacity: 0.5;
    
    &.active {
      opacity: 1;
      
      .avatar {
        border: 4rpx solid #5B8FF9;
      }
    }
    
    .avatar {
      width: 100rpx;
      height: 100rpx;
      border-radius: 50%;
      margin-bottom: 12rpx;
      border: 4rpx solid transparent;
    }
    
    .name {
      font-size: 24rpx;
      color: #333;
    }
  }
}

.repeat-list {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
  
  .repeat-item {
    padding: 16rpx 32rpx;
    background: #f5f5f5;
    border-radius: 30rpx;
    font-size: 26rpx;
    color: #666;
    
    &.active {
      background: #5B8FF9;
      color: #fff;
    }
  }
}
</style>