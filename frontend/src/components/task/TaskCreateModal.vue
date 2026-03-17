<template>
  <!-- 添加任务弹窗 - 通用组件 -->
  <view v-if="visible" class="modal-overlay" @click="handleClose">
    <view class="modal-content task-modal" @click.stop>
      <!-- 顶部：添加待办 -->
      <view class="modal-top">
        <view class="close-btn" @click="handleClose">
          <text class="close-icon">✕</text>
        </view>
        <text class="modal-title">{{ mode === 'edit' ? '编辑待办' : '添加待办' }}</text>
        <view class="save-btn" @click="handleSave">
          <text class="save-text">保存</text>
        </view>
      </view>
      
      <!-- 中间：输入框 -->
      <view class="modal-body">
        <input 
          class="task-input" 
          v-model="formData.title" 
          placeholder="请输入待办事项"
          focus
        />
        
        <!-- 备注 -->
        <textarea 
          class="remark-input" 
          v-model="formData.remark" 
          placeholder="添加备注..."
          :auto-height="true"
          :maxlength="500"
        />
      </view>
      
      <!-- 底部：提醒时间和分配人员 -->
      <view class="modal-bottom">
        <!-- 提醒时间 -->
        <view class="form-row" @click="showDateTimePicker">
          <text class="row-icon">⏰</text>
          <text class="row-label">提醒时间</text>
          <text class="row-value">
            {{ formData.dueDate || '今天' }} {{ formData.dueTime || '15:00' }}
          </text>
        </view>
        
        <!-- 分配人员 -->
        <view class="form-row" @click="openMemberPicker">
          <text class="row-icon">👤</text>
          <text class="row-label">分配人员</text>
          <view class="member-select">
            <text v-if="!formData.assigneeId" class="select-placeholder">点击选择</text>
            <text v-else class="select-value">{{ getMemberName(formData.assigneeId) }}</text>
            <text class="row-arrow">›</text>
          </view>
        </view>
      </view>
      
      <!-- 日期时间选择器 -->
      <view v-if="showTimePicker" class="picker-overlay" @click="closePicker">
        <view class="picker-container" @click.stop>
          <view class="picker-header">
            <text class="picker-cancel" @click="closePicker">取消</text>
            <text class="picker-title">选择时间</text>
            <text class="picker-confirm" @click="confirmPicker">确定</text>
          </view>
          
          <picker-view class="picker-view" :value="pickerValue" @change="onPickerChange">
            <picker-view-column>
              <view v-for="year in yearRange" :key="year" class="picker-item">{{ year }}年</view>
            </picker-view-column>
            <picker-view-column>
              <view v-for="month in 12" :key="'m'+month" class="picker-item">{{ month }}月</view>
            </picker-view-column>
            <picker-view-column>
              <view v-for="day in daysInMonth" :key="'d'+day" class="picker-item">{{ day }}日</view>
            </picker-view-column>
            <picker-view-column>
              <view v-for="hour in 24" :key="'h'+hour" class="picker-item">{{ String(hour-1).padStart(2, '0') }}时</view>
            </picker-view-column>
            <picker-view-column>
              <view v-for="minute in 60" :key="'min'+minute" class="picker-item">{{ String(minute-1).padStart(2, '0') }}分</view>
            </picker-view-column>
          </picker-view>
        </view>
      </view>
      
      <!-- 成员选择弹窗 -->
      <view v-if="showMemberPicker" class="picker-overlay" @click="closeMemberPicker">
        <view class="member-picker-container" @click.stop>
          <view class="picker-header">
            <text class="picker-title">选择家庭成员</text>
          </view>
          
          <view class="member-list">
            <view 
              v-for="member in familyMembers" 
              :key="member.userId"
              class="member-item"
              :class="{ active: formData.assigneeId === member.userId }"
              @click="selectMember(member.userId)"
            >
              <image 
                class="member-avatar" 
                :src="member.avatar || '/static/avatar-default.png'" 
                mode="aspectFill" 
              />
              <text class="member-name">{{ member.nickname || member.name || '家人' }}</text>
              <view v-if="formData.assigneeId === member.userId" class="check-icon">
                <text class="check-mark">✓</text>
              </view>
            </view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { taskApi } from '../api/task'
import { familyApi } from '../api/family'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  mode: {
    type: String,
    default: 'create' // 'create' 或 'edit'
  },
  taskId: {
    type: Number,
    default: null
  },
  initialData: {
    type: Object,
    default: () => ({})
  }
})

const emit = defineEmits(['close', 'success'])

// 表单数据
const formData = ref({
  title: '',
  remark: '',
  dueDate: '',
  dueTime: '',
  assigneeId: null
})

// 年份范围
const yearRange = ref([2024, 2025, 2026, 2027, 2028])
const pickerValue = ref([1, new Date().getMonth(), new Date().getDate() - 1, 15, 0])

// 计算当月天数
const daysInMonth = computed(() => {
  const year = yearRange.value[pickerValue.value[0]]
  const month = pickerValue.value[1] + 1
  return new Date(year, month, 0).getDate()
})

// 家庭成员
const familyMembers = ref([])

// 弹窗显示状态
const showTimePicker = ref(false)
const showMemberPicker = ref(false)

// 获取成员名称
const getMemberName = (userId) => {
  if (!userId) return '点击选择'
  const member = familyMembers.value.find(m => m.userId === userId)
  return member ? (member.nickname || member.name || '家人') : '点击选择'
}

// 加载家庭成员
const loadFamilyMembers = async () => {
  try {
    const familyId = uni.getStorageSync('currentFamilyId') || 1
    const res = await familyApi.getMembers(familyId)
    familyMembers.value = res || []
  } catch (e) {
    console.error('加载家庭成员失败:', e)
  }
}

// 显示日期时间选择器
const showDateTimePicker = () => {
  const now = new Date()
  pickerValue.value = [
    1, // 默认2025年
    now.getMonth(),
    now.getDate() - 1,
    15,
    0
  ]
  showTimePicker.value = true
}

// 关闭日期时间选择器
const closePicker = () => {
  showTimePicker.value = false
}

// 确认日期时间选择
const confirmPicker = () => {
  const year = yearRange.value[pickerValue.value[0]]
  const month = String(pickerValue.value[1] + 1).padStart(2, '0')
  const day = String(pickerValue.value[2] + 1).padStart(2, '0')
  const hour = String(pickerValue.value[3]).padStart(2, '0')
  const minute = String(pickerValue.value[4]).padStart(2, '0')
  
  formData.value.dueDate = `${year}-${month}-${day}`
  formData.value.dueTime = `${hour}:${minute}`
  showTimePicker.value = false
}

// 日期时间选择变化
const onPickerChange = (e) => {
  pickerValue.value = e.detail.value
}

// 打开成员选择器
const openMemberPicker = () => {
  showMemberPicker.value = true
}

// 关闭成员选择器
const closeMemberPicker = () => {
  showMemberPicker.value = false
}

// 选择成员
const selectMember = (userId) => {
  formData.value.assigneeId = userId
  showMemberPicker.value = false
}

// 关闭弹窗
const handleClose = () => {
  emit('close')
  // 重置表单
  formData.value = {
    title: '',
    remark: '',
    dueDate: '',
    dueTime: '',
    assigneeId: null
  }
}

// 保存
const handleSave = async () => {
  if (!formData.value.title.trim()) {
    uni.showToast({ title: '请输入待办事项', icon: 'none' })
    return
  }
  
  try {
    const familyId = uni.getStorageSync('currentFamilyId') || 1
    const dueTime = formData.value.dueDate && formData.value.dueTime 
      ? `${formData.value.dueDate} ${formData.value.dueTime}`
      : null
    
    await taskApi.create({
      title: formData.value.title,
      remark: formData.value.remark,
      familyId,
      assigneeId: formData.value.assigneeId,
      dueTime,
      status: 0,
      priority: 0
    })
    
    uni.showToast({ title: '添加成功', icon: 'success' })
    emit('success')
    handleClose()
  } catch (error) {
    console.error('添加任务失败:', error)
    uni.showToast({ title: '添加失败', icon: 'none' })
  }
}

// 打开弹窗时根据模式初始化数据
watch(() => props.visible, (newVal) => {
  if (newVal) {
    loadFamilyMembers()
    if (props.mode === 'edit' && props.initialData) {
      // 编辑模式：使用传入的数据初始化
      formData.value = {
        title: props.initialData.title || '',
        remark: props.initialData.remark || '',
        dueDate: props.initialData.dueDate || '',
        dueTime: props.initialData.dueTime || '',
        assigneeId: props.initialData.assigneeId || null
      }
    } else {
      // 创建模式：重置表单
      formData.value = {
        title: '',
        remark: '',
        dueDate: '',
        dueTime: '',
        assigneeId: null
      }
    }
  }
})
</script>

<style lang="scss" scoped>
// 导入首页的样式
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  align-items: center;
  z-index: 1000;
  animation: fadeIn 0.2s ease;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.modal-content {
  background: #fff;
  border-radius: 32rpx 32rpx 0 0;
  width: 100vw;
  max-height: 80vh;
  box-sizing: border-box;
  animation: slideUp 0.3s ease;
}

@keyframes slideUp {
  from { transform: translateY(100%); }
  to { transform: translateY(0); }
}

.task-modal {
  padding: 32rpx;
  box-sizing: border-box;
  
  .modal-top {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 40rpx;
    
    .close-btn {
      width: 64rpx;
      height: 64rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      
      .close-icon {
        font-size: 40rpx;
        color: #8b9aad;
      }
    }
    
    .modal-title {
      font-size: 36rpx;
      font-weight: 600;
      color: #2d3748;
    }
    
    .save-btn {
      padding: 16rpx 32rpx;
      background: linear-gradient(135deg, #6B8DD6, #8B5CF6);
      border-radius: 32rpx;
      
      .save-text {
        font-size: 28rpx;
        color: #fff;
        font-weight: 500;
      }
    }
  }
  
  .modal-body {
    margin-bottom: 40rpx;
    
    .task-input {
      width: 100%;
      height: 100rpx;
      font-size: 36rpx;
      color: #2d3748;
      border: none;
      border-bottom: 2rpx solid #e2e8f0;
      padding: 0;
      box-sizing: border-box;
      
      &::placeholder {
        color: #a0aec0;
      }
    }
    
    .remark-input {
      width: 100%;
      min-height: 120rpx;
      margin-top: 24rpx;
      padding: 20rpx;
      font-size: 30rpx;
      color: #2d3748;
      background: #f7fafc;
      border-radius: 16rpx;
      box-sizing: border-box;
      
      &::placeholder {
        color: #a0aec0;
      }
    }
  }
  
  .modal-bottom {
    .form-row {
      display: flex;
      align-items: center;
      padding: 24rpx 0;
      border-bottom: 2rpx solid #f1f5f9;
      
      &:last-child {
        border-bottom: none;
      }
      
      .row-icon {
        font-size: 36rpx;
        margin-right: 20rpx;
      }
      
      .row-label {
        font-size: 30rpx;
        color: #2d3748;
        flex: 1;
      }
      
      .row-value {
        font-size: 28rpx;
        color: #6B8DD6;
      }
      
      .member-select {
        display: flex;
        align-items: center;
        
        .select-placeholder {
          font-size: 28rpx;
          color: #a0aec0;
        }
        
        .select-value {
          font-size: 28rpx;
          color: #6B8DD6;
        }
        
        .row-arrow {
          font-size: 32rpx;
          color: #a0aec0;
          margin-left: 8rpx;
        }
      }
    }
  }
}

// 日期时间选择器
.picker-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  z-index: 1001;
}

.picker-container {
  background: #fff;
  width: 100vw;
  border-radius: 32rpx 32rpx 0 0;
  padding: 32rpx;
  box-sizing: border-box;
  
  .picker-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20rpx;
    
    .picker-cancel {
      font-size: 30rpx;
      color: #8b9aad;
    }
    
    .picker-title {
      font-size: 32rpx;
      font-weight: 600;
      color: #2d3748;
    }
    
    .picker-confirm {
      font-size: 30rpx;
      color: #6B8DD6;
      font-weight: 500;
    }
  }
  
  .picker-view {
    height: 400rpx;
    
    .picker-item {
      line-height: 80rpx;
      text-align: center;
      font-size: 30rpx;
      color: #2d3748;
    }
  }
}

// 成员选择器
.member-picker-container {
  background: #fff;
  width: 100vw;
  border-radius: 32rpx 32rpx 0 0;
  padding: 32rpx;
  max-height: 60vh;
  box-sizing: border-box;
  
  .picker-header {
    text-align: center;
    margin-bottom: 32rpx;
    
    .picker-title {
      font-size: 32rpx;
      font-weight: 600;
      color: #2d3748;
    }
  }
  
  .member-list {
    width: 100%;
    
    .member-item {
      display: flex;
      align-items: center;
      width: 100%;
      padding: 20rpx;
      border-radius: 16rpx;
      margin-bottom: 12rpx;
      transition: all 0.2s ease;
      box-sizing: border-box;
      
      &.active {
        background: #f0f4ff;
        
        .member-name {
          color: #6B8DD6;
          font-weight: 500;
        }
      }
      
      .member-avatar {
        width: 72rpx;
        height: 72rpx;
        border-radius: 50%;
        margin-right: 20rpx;
        background: linear-gradient(135deg, #6B8DD6, #8B5CF6);
        flex-shrink: 0;
        
        &.default {
          background: linear-gradient(135deg, #68d391, #48bb78);
        }
      }
      
      .member-name {
        font-size: 30rpx;
        color: #2d3748;
        flex: 1;
        min-width: 0;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
      
      .check-icon {
        width: 40rpx;
        height: 40rpx;
        border-radius: 50%;
        background: #6B8DD6;
        display: flex;
        align-items: center;
        justify-content: center;
        flex-shrink: 0;
        margin-left: auto;
        
        .check-mark {
          color: #fff;
          font-size: 26rpx;
          font-weight: bold;
          line-height: 1;
        }
      }
    }
  }
}
</style>
