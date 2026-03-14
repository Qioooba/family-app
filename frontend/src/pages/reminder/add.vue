<template>
  <view class="add-page">
    <view class="form">
      <view class="form-item">
        <text class="label">提醒名称</text>
        <input class="input" v-model="form.reminderName" placeholder="输入提醒名称" />
      </view>
      
      <view class="form-item">
        <text class="label">提醒类型</text>
        <view class="type-grid">
          <view 
            v-for="type in types" 
            :key="type.value"
            class="type-item"
            :class="{ active: form.reminderType === type.value }"
            @click="form.reminderType = type.value"
          >
            <text class="type-icon">{{ type.icon }}</text>
            <text class="type-name">{{ type.label }}</text>
          </view>
        </view>
      </view>
      
      <view class="form-item">
        <text class="label">提醒时间</text>
        <picker mode="time" :value="form.remindTime" @change="onTimeChange">
          <view class="picker">
            {{ form.remindTime || '选择时间' }}
          </view>
        </picker>
      </view>
      
      <view class="btn-group">
        <button class="btn-cancel" @click="goBack">取消</button>
        <button class="btn-save" @click="saveReminder">保存</button>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      form: {
        reminderName: '',
        reminderType: 'WATER',
        remindTime: ''
      },
      types: [
        { value: 'WATER', label: '喝水', icon: '💧' },
        { value: 'MEDICINE', label: '用药', icon: '💊' },
        { value: 'EXPIRE', label: '保质期', icon: '📦' },
        { value: 'BIRTHDAY', label: '生日', icon: '🎂' },
        { value: 'FINANCE', label: '财务', icon: '💰' },
        { value: 'SYSTEM', label: '其他', icon: '⏰' }
      ]
    }
  },
  methods: {
    onTimeChange(e) {
      this.form.remindTime = e.detail.value
    },
    async saveReminder() {
      if (!this.form.reminderName) {
        uni.showToast({ title: '请输入提醒名称', icon: 'none' })
        return
      }
      if (!this.form.remindTime) {
        uni.showToast({ title: '请选择提醒时间', icon: 'none' })
        return
      }
      
      try {
        const res = await uni.request({
          url: '/api/reminder/add',
          method: 'POST',
          data: this.form
        })
        if (res.data && res.data.code === 0) {
          uni.showToast({ title: '创建成功', icon: 'success' })
          setTimeout(() => {
            uni.navigateBack()
          }, 1000)
        } else {
          uni.showToast({ title: res.data?.message || '创建失败', icon: 'none' })
        }
      } catch (e) {
        uni.showToast({ title: '创建失败', icon: 'none' })
      }
    },
    goBack() {
      uni.navigateBack()
    }
  }
}
</script>

<style lang="scss" scoped>
.add-page {
  min-height: 100vh;
  background: #f5f6fa;
  padding: 20rpx;
}

.form {
  background: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
}

.form-item {
  margin-bottom: 40rpx;
}

.label {
  font-size: 28rpx;
  color: #333;
  font-weight: 500;
  display: block;
  margin-bottom: 16rpx;
}

.input {
  border: 1rpx solid #eee;
  border-radius: 12rpx;
  padding: 20rpx;
  font-size: 28rpx;
}

.type-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20rpx;
}

.type-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20rpx;
  border: 2rpx solid #eee;
  border-radius: 12rpx;
}

.type-item.active {
  border-color: #07c160;
  background: #e8f5e9;
}

.type-icon {
  font-size: 48rpx;
  margin-bottom: 8rpx;
}

.type-name {
  font-size: 24rpx;
  color: #666;
}

.picker {
  border: 1rpx solid #eee;
  border-radius: 12rpx;
  padding: 20rpx;
  font-size: 28rpx;
}

.btn-group {
  display: flex;
  gap: 20rpx;
  margin-top: 60rpx;
}

.btn-cancel, .btn-save {
  flex: 1;
  height: 88rpx;
  line-height: 88rpx;
  border-radius: 44rpx;
  font-size: 30rpx;
}

.btn-cancel {
  background: #f5f5f5;
  color: #666;
}

.btn-save {
  background: #07c160;
  color: #fff;
}
</style>
