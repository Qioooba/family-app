<template>
  <!-- 添加任务弹窗 -->
  <view v-if="visible" class="modal-overlay" @click="$emit('close')">
    <view class="modal-content" @click.stop>
      <view class="modal-header">
        <text class="modal-title">{{ isEdit ? '编辑任务' : '添加新任务' }}</text>
        <text class="close-btn" @click="$emit('close')">✕</text>
      </view>
      
      <view class="form-item">
        <text class="label">任务标题</text>
        <input 
          class="input" 
          v-model="form.title" 
          placeholder="输入任务标题"
        />
      </view>
      
      <view class="form-item">
        <text class="label">截止日期</text>
        <picker mode="date" :value="form.dueDate" @change="onDateChange">
          <view class="picker">{{ form.dueDate || '请选择日期' }}</view>
        </picker>
      </view>
      
      <view class="form-item">
        <text class="label">截止时间</text>
        <picker mode="time" :value="form.dueTime" @change="onTimeChange">
          <view class="picker">{{ form.dueTime || '请选择时间(可选)' }}</view>
        </picker>
      </view>
      
      <view class="form-item">
        <text class="label">优先级</text>
        <view class="priority-options">
          <view 
            v-for="(p, i) in priorities" 
            :key="i"
            class="priority-option"
            :class="{ active: form.priority === i }"
            @click="form.priority = i"
          >
            <text>{{ p }}</text>
          </view>
        </view>
      </view>
      
      <view class="form-item">
        <text class="label">分类</text>
        <view class="category-options">
          <view 
            v-for="cat in categories" 
            :key="cat.id"
            class="category-option"
            :class="{ active: form.categoryId === cat.id }"
            @click="form.categoryId = cat.id"
          >
            <text>{{ cat.name }}</text>
          </view>
        </view>
      </view>
      
      <view class="form-actions">
        <button class="btn-cancel" @click="$emit('close')">取消</button>
        <button class="btn-confirm" @click="confirm">{{ isEdit ? '保存' : '确认添加' }}</button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  visible: Boolean,
  task: Object,
  priorities: Array,
  categories: Array
})

const emit = defineEmits(['close', 'confirm'])

const isEdit = ref(false)
const form = ref({
  title: '',
  dueDate: '',
  dueTime: '',
  priority: 0,
  categoryId: 1
})

watch(() => props.visible, (val) => {
  if (val) {
    if (props.task) {
      isEdit.value = true
      form.value = { ...props.task }
    } else {
      isEdit.value = false
      form.value = {
        title: '',
        dueDate: '',
        dueTime: '',
        priority: 0,
        categoryId: 1
      }
    }
  }
})

const onDateChange = (e) => {
  form.value.dueDate = e.detail.value
}

const onTimeChange = (e) => {
  form.value.dueTime = e.detail.value
}

const confirm = () => {
  if (!form.value.title.trim()) {
    uni.showToast({ title: '请输入任务标题', icon: 'none' })
    return
  }
  emit('confirm', { ...form.value, isEdit: isEdit.value })
}
</script>

<style lang="scss" scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  width: 85%;
  max-height: 80vh;
  background: #fff;
  border-radius: 20rpx;
  padding: 30rpx;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30rpx;
  
  .modal-title {
    font-size: 32rpx;
    font-weight: 600;
    color: #333;
  }
  
  .close-btn {
    font-size: 36rpx;
    color: #999;
  }
}

.form-item {
  margin-bottom: 30rpx;
  
  .label {
    display: block;
    font-size: 28rpx;
    color: #333;
    margin-bottom: 16rpx;
  }
  
  .input {
    width: 100%;
    height: 88rpx;
    background: #f5f5f5;
    border-radius: 12rpx;
    padding: 0 24rpx;
    font-size: 28rpx;
  }
  
  .picker {
    height: 88rpx;
    line-height: 88rpx;
    background: #f5f5f5;
    border-radius: 12rpx;
    padding: 0 24rpx;
    font-size: 28rpx;
    color: #666;
  }
}

.priority-options, .category-options {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
  
  .priority-option, .category-option {
    padding: 16rpx 32rpx;
    background: #f5f5f5;
    border-radius: 12rpx;
    font-size: 26rpx;
    color: #666;
    
    &.active {
      background: #4caf50;
      color: #fff;
    }
  }
}

.form-actions {
  display: flex;
  gap: 20rpx;
  margin-top: 40rpx;
  
  button {
    flex: 1;
    height: 88rpx;
    border-radius: 44rpx;
    font-size: 30rpx;
    border: none;
  }
  
  .btn-cancel {
    background: #f5f5f5;
    color: #666;
  }
  
  .btn-confirm {
    background: #4caf50;
    color: #fff;
  }
}
</style>
