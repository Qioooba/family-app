<template>
  <!-- 首页待办任务创建/编辑弹窗 -->
  <view v-if="visible" class="modal-overlay" @click="handleClose">
    <view class="modal-content" @click.stop>
      <view class="modal-header">
        <view class="header-left">
          <text class="modal-icon">{{ isEditMode ? '✏️' : '✨' }}</text>
          <text class="modal-title">{{ isEditMode ? '编辑任务' : '新建待办' }}</text>
        </view>
        <view class="close-btn" @click="handleClose">
          <u-icon name="close" size="20" color="#999"></u-icon>
        </view>
      </view>
      
      <!-- 任务标题 -->
      <view class="form-item">
        <text class="label">
          <text class="required">*</text>
          任务标题
        </text>
        <input 
          class="input" 
          v-model="form.title" 
          placeholder="请输入任务标题"
          maxlength="50"
        />
      </view>
      
      <!-- 日期和时间选择 - 使用原生picker -->
      <view class="form-item">
        <text class="label">截止日期</text>
        <view class="datetime-picker">
          <view class="picker date-picker" @click="showDatePicker">
            <text class="picker-icon">📅</text>
            <text class="picker-text">{{ form.dueDate }}</text>
            <u-icon name="arrow-down" size="14" color="#ccc"></u-icon>
          </view>
          <view class="picker time-picker" @click="showTimePicker">
            <text class="picker-icon">🕐</text>
            <text class="picker-text">{{ form.dueTime }}</text>
            <u-icon name="arrow-down" size="14" color="#ccc"></u-icon>
          </view>
        </view>
      </view>
      
      <!-- 优先级选择 -->
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
            <text class="priority-dot" :class="`priority-${i}`"></text>
            <text class="priority-text">{{ p }}</text>
          </view>
        </view>
      </view>
      
      <!-- 指派给选择 -->
      <view class="form-item" v-if="members.length > 0">
        <text class="label">指派给</text>
        <view class="assignee-options">
          <view 
            class="assignee-option"
            :class="{ active: !form.assigneeId }"
            @click="form.assigneeId = null"
          >
            <text class="assignee-text">自己</text>
          </view>
          <view 
            v-for="(member, i) in members" 
            :key="member.id"
            class="assignee-option"
            :class="{ active: form.assigneeId === member.userId }"
            @click="form.assigneeId = member.userId"
          >
            <image 
              class="assignee-avatar" 
              :src="member.avatar || '../../static/avatar-default.png'" 
              mode="aspectFill" 
            />
            <text class="assignee-text">{{ member.nickname || member.name || '家人' }}</text>
          </view>
        </view>
      </view>
      
      <!-- 操作按钮 -->
      <view class="form-actions">
        <view class="btn-cancel" @click="handleClose">
          <text>取消</text>
        </view>
        <view class="btn-confirm" @click="handleConfirm">
          <text>{{ isEditMode ? '保存修改' : '确认创建' }}</text>
        </view>
      </view>
    </view>
    
    <!-- 自定义日期选择器弹窗 - 放在最外层确保层级最高 -->
    <view v-if="datePickerVisible" class="picker-overlay" @click="closeDatePicker">
      <view class="picker-popup" @click.stop>
        <view class="picker-header">
          <text class="picker-cancel" @click="closeDatePicker">取消</text>
          <text class="picker-title">选择日期</text>
          <text class="picker-confirm" @click="confirmDatePicker">确定</text>
        </view>
        <picker-view class="picker-view" :value="datePickerValue" @change="onDatePickerChange">
          <picker-view-column>
            <view v-for="year in yearRange" :key="year" class="picker-item">{{ year }}年</view>
          </picker-view-column>
          <picker-view-column>
            <view v-for="month in 12" :key="month" class="picker-item">{{ month }}月</view>
          </picker-view-column>
          <picker-view-column>
            <view v-for="day in daysInMonth" :key="day" class="picker-item">{{ day }}日</view>
          </picker-view-column>
        </picker-view>
      </view>
    </view>
    
    <!-- 自定义时间选择器弹窗 -->
    <view v-if="timePickerVisible" class="picker-overlay" @click="closeTimePicker">
      <view class="picker-popup" @click.stop>
        <view class="picker-header">
          <text class="picker-cancel" @click="closeTimePicker">取消</text>
          <text class="picker-title">选择时间</text>
          <text class="picker-confirm" @click="confirmTimePicker">确定</text>
        </view>
        <picker-view class="picker-view" :value="timePickerValue" @change="onTimePickerChange">
          <picker-view-column>
            <view v-for="hour in 24" :key="hour-1" class="picker-item">{{ String(hour-1).padStart(2, '0') }}时</view>
          </picker-view-column>
          <picker-view-column>
            <view v-for="minute in 60" :key="minute-1" class="picker-item">{{ String(minute-1).padStart(2, '0') }}分</view>
          </picker-view-column>
        </picker-view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, watch, computed } from 'vue'

const props = defineProps({
  visible: Boolean,
  mode: {
    type: String,
    default: 'create' // 'create' 或 'edit'
  },
  task: {
    type: Object,
    default: () => null
  },
  members: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['close', 'confirm', 'update'])

// 是否为编辑模式
const isEditMode = computed(() => props.mode === 'edit' && props.task)

// 获取今天的日期字符串 YYYY-MM-DD
const getTodayString = () => {
  const today = new Date()
  const year = today.getFullYear()
  const month = String(today.getMonth() + 1).padStart(2, '0')
  const day = String(today.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

const today = getTodayString()
const currentYear = new Date().getFullYear()

// 年份范围：当前年份前后10年
const yearRange = computed(() => {
  const years = []
  for (let i = currentYear - 10; i <= currentYear + 10; i++) {
    years.push(i)
  }
  return years
})

// 根据选择的年月计算当月天数
const daysInMonth = computed(() => {
  const year = yearRange.value[datePickerValue.value[0]] || currentYear
  const month = (datePickerValue.value[1] || 0) + 1
  return new Date(year, month, 0).getDate()
})

// 日期选择器状态
const datePickerVisible = ref(false)
const datePickerValue = ref([10, new Date().getMonth(), new Date().getDate() - 1]) // 默认选中今天

// 时间选择器状态
const timePickerVisible = ref(false)
const timePickerValue = ref([23, 59])

// 临时存储选择值
const tempDateValue = ref([...datePickerValue.value])
const tempTimeValue = ref([...timePickerValue.value])

// 表单数据
const form = ref({
  title: '',
  dueDate: today,
  dueTime: '23:59',
  priority: 0,
  assigneeId: null
})

// 从任务数据中提取日期和时间
const extractDateTime = (dueTime) => {
  if (!dueTime) return { date: today, time: '23:59' }
  try {
    const date = new Date(dueTime)
    if (isNaN(date.getTime())) {
      // 尝试解析 YYYY-MM-DD HH:mm 格式
      const parts = dueTime.split(' ')
      if (parts.length === 2) {
        return { date: parts[0], time: parts[1] }
      }
      return { date: today, time: '23:59' }
    }
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    const hours = String(date.getHours()).padStart(2, '0')
    const minutes = String(date.getMinutes()).padStart(2, '0')
    return { date: `${year}-${month}-${day}`, time: `${hours}:${minutes}` }
  } catch (e) {
    return { date: today, time: '23:59' }
  }
}

// 解析日期字符串到picker值
const parseDateToPickerValue = (dateStr) => {
  const parts = dateStr.split('-')
  if (parts.length === 3) {
    const yearIndex = yearRange.value.indexOf(parseInt(parts[0]))
    return [
      yearIndex >= 0 ? yearIndex : 10,
      parseInt(parts[1]) - 1,
      parseInt(parts[2]) - 1
    ]
  }
  return [10, new Date().getMonth(), new Date().getDate() - 1]
}

// 解析时间字符串到picker值
const parseTimeToPickerValue = (timeStr) => {
  const parts = timeStr.split(':')
  if (parts.length === 2) {
    return [parseInt(parts[0]), parseInt(parts[1])]
  }
  return [23, 59]
}

// 监听弹窗显示，重置表单或加载任务数据
watch(() => props.visible, (val) => {
  if (val) {
    if (isEditMode.value && props.task) {
      // 编辑模式：加载任务数据
      const { date, time } = extractDateTime(props.task.dueTime)
      form.value = {
        title: props.task.title || '',
        dueDate: date,
        dueTime: time,
        priority: props.task.priority || 0
      }
      datePickerValue.value = parseDateToPickerValue(date)
      timePickerValue.value = parseTimeToPickerValue(time)
    } else {
      // 创建模式：重置表单
      form.value = {
        title: '',
        dueDate: today,
        dueTime: '23:59',
        priority: 0
      }
      datePickerValue.value = [10, new Date().getMonth(), new Date().getDate() - 1]
      timePickerValue.value = [23, 59]
    }
  }
}, { immediate: true })

// 优先级选项
const priorities = ['普通', '重要', '紧急']

// 显示日期选择器
const showDatePicker = () => {
  tempDateValue.value = [...datePickerValue.value]
  datePickerVisible.value = true
}

// 关闭日期选择器
const closeDatePicker = () => {
  datePickerVisible.value = false
}

// 确认日期选择
const confirmDatePicker = () => {
  datePickerValue.value = [...tempDateValue.value]
  const year = yearRange.value[datePickerValue.value[0]]
  const month = String(datePickerValue.value[1] + 1).padStart(2, '0')
  const day = String(datePickerValue.value[2] + 1).padStart(2, '0')
  form.value.dueDate = `${year}-${month}-${day}`
  closeDatePicker()
}

// 日期选择变化
const onDatePickerChange = (e) => {
  tempDateValue.value = e.detail.value
}

// 显示时间选择器
const showTimePicker = () => {
  tempTimeValue.value = [...timePickerValue.value]
  timePickerVisible.value = true
}

// 关闭时间选择器
const closeTimePicker = () => {
  timePickerVisible.value = false
}

// 确认时间选择
const confirmTimePicker = () => {
  timePickerValue.value = [...tempTimeValue.value]
  const hours = String(timePickerValue.value[0]).padStart(2, '0')
  const minutes = String(timePickerValue.value[1]).padStart(2, '0')
  form.value.dueTime = `${hours}:${minutes}`
  closeTimePicker()
}

// 时间选择变化
const onTimePickerChange = (e) => {
  tempTimeValue.value = e.detail.value
}

// 关闭弹窗
const handleClose = () => {
  datePickerVisible.value = false
  timePickerVisible.value = false
  emit('close')
}

// 确认按钮点击
const handleConfirm = () => {
  if (!form.value.title.trim()) {
    uni.showToast({ title: '请输入任务标题', icon: 'none' })
    return
  }
  
  // 组装任务数据
  const taskData = {
    title: form.value.title.trim(),
    dueDate: form.value.dueDate,
    priority: form.value.priority,
    dueTime: form.value.dueTime,
    assigneeId: form.value.assigneeId
  }
  
  if (isEditMode.value) {
    // 编辑模式：触发 update 事件
    emit('update', {
      ...taskData,
      id: props.task.id,
      status: props.task.status
    })
  } else {
    // 创建模式：触发 confirm 事件
    emit('confirm', taskData)
  }
}
</script>

<style lang="scss" scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 99999 !important;
  animation: fadeIn 0.2s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.modal-content {
  width: 82%;
  background: #fff;
  border-radius: 32rpx;
  padding: 40rpx;
  box-shadow: 0 20rpx 60rpx rgba(0, 0, 0, 0.2);
  animation: slideUp 0.3s ease-out;
  position: relative;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(40rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 40rpx;
  
  .header-left {
    display: flex;
    align-items: center;
    gap: 12rpx;
    
    .modal-icon {
      font-size: 36rpx;
    }
    
    .modal-title {
      font-size: 36rpx;
      font-weight: 600;
      color: #2d3748;
    }
  }
  
  .close-btn {
    width: 56rpx;
    height: 56rpx;
    background: #f5f5f5;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.2s ease;
    
    &:active {
      transform: scale(0.9);
      background: #e8e8e8;
    }
  }
}

.form-item {
  margin-bottom: 32rpx;
  
  .label {
    display: block;
    font-size: 28rpx;
    color: #4a5568;
    margin-bottom: 16rpx;
    font-weight: 500;
    
    .required {
      color: #fc8181;
      margin-right: 4rpx;
    }
  }
  
  .input {
    width: 100%;
    height: 96rpx;
    background: #f7fafc;
    border-radius: 16rpx;
    padding: 0 28rpx;
    font-size: 30rpx;
    color: #2d3748;
    box-sizing: border-box;
    transition: all 0.2s ease;
    
    &:focus {
      background: #edf2f7;
    }
  }

  .datetime-picker {
    display: flex;
    gap: 20rpx;

    .picker {
      flex: 1;
      height: 96rpx;
      background: #f7fafc;
      border-radius: 16rpx;
      padding: 0 28rpx;
      display: flex;
      align-items: center;
      gap: 16rpx;
      transition: all 0.2s ease;
      
      &:active {
        background: #edf2f7;
      }
      
      .picker-icon {
        font-size: 32rpx;
      }
      
      .picker-text {
        flex: 1;
        font-size: 30rpx;
        color: #2d3748;
      }
    }
  }
}

.priority-options {
  display: flex;
  gap: 20rpx;
  
  .priority-option {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 12rpx;
    padding: 24rpx 0;
    background: #f7fafc;
    border-radius: 16rpx;
    transition: all 0.2s ease;
    
    &:active {
      transform: scale(0.98);
    }
    
    &.active {
      background: linear-gradient(135deg, #A8E6CF, #7FD8BE);
      
      .priority-text {
        color: #fff;
      }
    }
    
    .priority-dot {
      width: 16rpx;
      height: 16rpx;
      border-radius: 50%;
      
      &.priority-0 {
        background: #68d391;
      }
      
      &.priority-1 {
        background: #f6ad55;
      }
      
      &.priority-2 {
        background: #fc8181;
      }
    }
    
    .priority-text {
      font-size: 28rpx;
      color: #4a5568;
      font-weight: 500;
    }
  }
}

.assignee-options {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
  
  .assignee-option {
    display: flex;
    align-items: center;
    gap: 12rpx;
    padding: 16rpx 24rpx;
    background: #f7fafc;
    border-radius: 40rpx;
    transition: all 0.2s ease;
    
    &:active {
      transform: scale(0.98);
    }
    
    &.active {
      background: linear-gradient(135deg, #A8E6CF, #7FD8BE);
      
      .assignee-text {
        color: #fff;
      }
    }
    
    .assignee-avatar {
      width: 44rpx;
      height: 44rpx;
      border-radius: 50%;
    }
    
    .assignee-text {
      font-size: 26rpx;
      color: #4a5568;
      font-weight: 500;
    }
  }
}

.form-actions {
  display: flex;
  gap: 24rpx;
  margin-top: 48rpx;
  
  .btn-cancel, .btn-confirm {
    flex: 1;
    height: 96rpx;
    border-radius: 48rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.25s ease;
    
    &:active {
      transform: scale(0.96);
    }
    
    text {
      font-size: 30rpx;
      font-weight: 600;
    }
  }
  
  .btn-cancel {
    background: #f7fafc;
    
    text {
      color: #718096;
    }
    
    &:active {
      background: #edf2f7;
    }
  }
  
  .btn-confirm {
    background: linear-gradient(135deg, #A8E6CF, #7FD8BE);
    box-shadow: 0 8rpx 24rpx rgba(168, 230, 207, 0.4);
    
    text {
      color: #fff;
    }
    
    &:active {
      box-shadow: 0 4rpx 16rpx rgba(168, 230, 207, 0.3);
    }
  }
}

// 自定义选择器弹窗 - 最高层级
.picker-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: flex-end;
  justify-content: center;
  z-index: 100000 !important;
  animation: fadeIn 0.2s ease-out;
}

.picker-popup {
  width: 100%;
  background: #fff;
  border-radius: 32rpx 32rpx 0 0;
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
  animation: slideUpPicker 0.3s ease-out;
}

@keyframes slideUpPicker {
  from {
    transform: translateY(100%);
  }
  to {
    transform: translateY(0);
  }
}

.picker-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx 40rpx;
  border-bottom: 1rpx solid #f0f0f0;
  
  .picker-cancel {
    font-size: 30rpx;
    color: #999;
  }
  
  .picker-title {
    font-size: 32rpx;
    font-weight: 600;
    color: #333;
  }
  
  .picker-confirm {
    font-size: 30rpx;
    color: #7FD8BE;
    font-weight: 600;
  }
}

.picker-view {
  height: 400rpx;
  
  .picker-item {
    line-height: 80rpx;
    text-align: center;
    font-size: 32rpx;
    color: #333;
  }
}
</style>