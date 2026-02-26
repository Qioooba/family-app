<template>
  <view v-if="visible" class="modal-overlay" @click="handleClose">
    <view class="modal-content" @click.stop>
      <view class="modal-header">
        <text class="modal-title">{{ isEdit ? '✏️ 编辑纪念日' : '💝 添加纪念日' }}</text>
        <view class="modal-close" @click="handleClose">
          <u-icon name="close" size="32" color="#999"></u-icon>
        </view>
      </view>

      <view class="modal-body">
        <!-- 名称 -->
        <view class="form-item">
          <text class="form-label">纪念日名称 <text class="required">*</text></text>
          <input
            v-model="formData.title"
            type="text"
            class="form-input"
            placeholder="如：结婚纪念日、宝宝生日"
            maxlength="50"
          />
        </view>

        <!-- 类型 -->
        <view class="form-item">
          <text class="form-label">纪念类型</text>
          <view class="type-options">
            <view
              v-for="type in typeOptions"
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

        <!-- 日期 -->
        <view class="form-item">
          <text class="form-label">纪念日期 <text class="required">*</text></text>
          <view class="date-row">
            <view class="date-input-wrapper" @click="showDatePicker = true">
              <text class="date-text" :class="{ placeholder: !formData.targetDate }">
                {{ formData.targetDate || '请选择日期' }}
              </text>
              <u-icon name="calendar" size="28" color="#999"></u-icon>
            </view>
            <!-- 农历/公历切换 -->
            <view class="date-type-toggle">
              <view
                class="toggle-item"
                :class="{ active: formData.dateType === 'solar' }"
                @click="formData.dateType = 'solar'"
              >
                公历
              </view>
              <view
                class="toggle-item"
                :class="{ active: formData.dateType === 'lunar' }"
                @click="formData.dateType = 'lunar'"
              >
                农历
              </view>
            </view>
          </view>
        </view>

        <!-- 是否重复 -->
        <view class="form-item">
          <view class="switch-row">
            <text class="form-label">每年重复提醒</text>
            <view
              class="custom-switch"
              :class="{ active: formData.isRecurring === 1 }"
              @click="formData.isRecurring = formData.isRecurring === 1 ? 0 : 1"
            >
              <view class="switch-dot"></view>
            </view>
          </view>
        </view>

        <!-- 提前提醒 -->
        <view class="form-item">
          <text class="form-label">提前提醒</text>
          <view class="reminder-options">
            <view
              v-for="day in reminderOptions"
              :key="day"
              class="reminder-option"
              :class="{ active: selectedReminders.includes(day) }"
              @click="toggleReminder(day)"
            >
              {{ day }}天
            </view>
          </view>
        </view>

        <!-- 描述 -->
        <view class="form-item">
          <text class="form-label">备注说明</text>
          <textarea
            v-model="formData.description"
            class="form-textarea"
            placeholder="添加一些纪念日的备注信息..."
            maxlength="200"
          />
        </view>
      </view>

      <view class="modal-footer">
        <view v-if="isEdit" class="btn-delete" @click="handleDelete">
          <u-icon name="trash" size="28" color="#ff6b6b"></u-icon>
        </view>
        <view class="btn-cancel" @click="handleClose">取消</view>
        <view class="btn-confirm" @click="handleConfirm">{{ isEdit ? '保存' : '添加' }}</view>
      </view>
    </view>

    <!-- 日期选择器 -->
    <up-datetime-picker
      v-model="showDatePicker"
      mode="date"
      :value="formData.targetDate"
      @confirm="onDateConfirm"
    />
  </view>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { anniversaryApi } from '@/api/anniversary'
import { useUserStore } from '@/stores/user'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  data: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['close', 'confirm'])
const userStore = useUserStore()

const showDatePicker = ref(false)

// 纪念日类型选项
const typeOptions = [
  { value: 'birthday', label: '生日', icon: '🎂' },
  { value: 'wedding', label: '结婚', icon: '💒' },
  { value: 'love', label: '恋爱', icon: '💕' },
  { value: 'family', label: '家庭', icon: '🏠' },
  { value: 'traditional', label: '传统', icon: '🎊' },
  { value: 'custom', label: '自定义', icon: '💝' }
]

// 提醒选项
const reminderOptions = [1, 3, 7, 14, 30]

// 表单数据
const formData = ref({
  title: '',
  type: 'custom',
  targetDate: '',
  dateType: 'solar',
  isRecurring: 1,
  description: '',
  icon: ''
})

const selectedReminders = ref([3])

const isEdit = computed(() => !!props.data?.id)

// 监听数据变化
watch(() => props.data, (newVal) => {
  if (newVal) {
    formData.value = {
      id: newVal.id,
      title: newVal.title || '',
      type: newVal.type || 'custom',
      targetDate: newVal.targetDate || '',
      dateType: newVal.dateType || 'solar',
      isRecurring: newVal.isRecurring ?? 1,
      description: newVal.description || '',
      icon: newVal.icon || ''
    }
    // 解析提醒天数
    if (newVal.reminderDays) {
      try {
        selectedReminders.value = JSON.parse(newVal.reminderDays)
      } catch {
        selectedReminders.value = [3]
      }
    }
  } else {
    resetForm()
  }
}, { immediate: true })

// 重置表单
const resetForm = () => {
  formData.value = {
    title: '',
    type: 'custom',
    targetDate: '',
    dateType: 'solar',
    isRecurring: 1,
    description: '',
    icon: ''
  }
  selectedReminders.value = [3]
}

// 切换提醒选项
const toggleReminder = (day) => {
  const index = selectedReminders.value.indexOf(day)
  if (index > -1) {
    selectedReminders.value.splice(index, 1)
  } else {
    selectedReminders.value.push(day)
  }
  selectedReminders.value.sort((a, b) => a - b)
}

// 日期确认
const onDateConfirm = (e) => {
  formData.value.targetDate = e.value
  showDatePicker.value = false
}

// 关闭弹窗
const handleClose = () => {
  emit('close')
}

// 删除
const handleDelete = () => {
  uni.showModal({
    title: '确认删除',
    content: '确定要删除这个纪念日吗？',
    confirmColor: '#ff6b6b',
    success: async (res) => {
      if (res.confirm) {
        try {
          uni.showLoading({ title: '删除中...' })
          await anniversaryApi.delete(props.data.id)
          uni.hideLoading()
          uni.showToast({ title: '删除成功', icon: 'success' })
          emit('confirm')
        } catch (error) {
          uni.hideLoading()
          uni.showToast({ title: '删除失败', icon: 'none' })
        }
      }
    }
  })
}

// 确认提交
const handleConfirm = async () => {
  // 验证
  if (!formData.value.title.trim()) {
    uni.showToast({ title: '请输入纪念日名称', icon: 'none' })
    return
  }
  if (!formData.value.targetDate) {
    uni.showToast({ title: '请选择日期', icon: 'none' })
    return
  }

  try {
    uni.showLoading({ title: isEdit.value ? '保存中...' : '添加中...' })

    // 获取当前家庭ID
    const familyId = uni.getStorageSync('currentFamilyId')
    if (!familyId) {
      uni.hideLoading()
      uni.showToast({ title: '请先选择家庭', icon: 'none' })
      return
    }

    const submitData = {
      ...formData.value,
      familyId: familyId,
      reminderDays: JSON.stringify(selectedReminders.value),
      icon: typeOptions.find(t => t.value === formData.value.type)?.icon || '💝'
    }

    if (isEdit.value) {
      await anniversaryApi.update(submitData)
    } else {
      await anniversaryApi.create(submitData)
    }

    uni.hideLoading()
    uni.showToast({ title: isEdit.value ? '保存成功' : '添加成功', icon: 'success' })
    emit('confirm')
  } catch (error) {
    uni.hideLoading()
    console.error('提交失败:', error)
    uni.showToast({ title: '操作失败，请重试', icon: 'none' })
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
  z-index: 1000;
  animation: fadeIn 0.2s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.modal-content {
  width: 640rpx;
  max-height: 80vh;
  background: #fff;
  border-radius: 32rpx;
  overflow: hidden;
  animation: slideUp 0.3s ease-out;
  display: flex;
  flex-direction: column;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(50rpx);
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
  padding: 32rpx;
  border-bottom: 2rpx solid #f1f5f9;

  .modal-title {
    font-size: 32rpx;
    font-weight: 600;
    color: #2d3748;
  }

  .modal-close {
    width: 56rpx;
    height: 56rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 50%;
    background: #f7fafc;

    &:active {
      background: #edf2f7;
    }
  }
}

.modal-body {
  padding: 32rpx;
  max-height: 60vh;
  overflow-y: auto;
}

.form-item {
  margin-bottom: 32rpx;

  &:last-child {
    margin-bottom: 0;
  }

  .form-label {
    display: block;
    font-size: 28rpx;
    color: #4a5568;
    margin-bottom: 16rpx;
    font-weight: 500;

    .required {
      color: #fc8181;
      margin-left: 4rpx;
    }
  }

  .form-input {
    width: 100%;
    height: 88rpx;
    background: #f7fafc;
    border: 2rpx solid #e2e8f0;
    border-radius: 16rpx;
    padding: 0 24rpx;
    font-size: 28rpx;
    color: #2d3748;

    &:focus {
      border-color: #7FD8BE;
      background: #fff;
    }
  }

  .form-textarea {
    width: 100%;
    height: 160rpx;
    background: #f7fafc;
    border: 2rpx solid #e2e8f0;
    border-radius: 16rpx;
    padding: 20rpx 24rpx;
    font-size: 28rpx;
    color: #2d3748;

    &:focus {
      border-color: #7FD8BE;
      background: #fff;
    }
  }
}

// 类型选项
.type-options {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;

  .type-option {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 16rpx 24rpx;
    background: #f7fafc;
    border: 2rpx solid #e2e8f0;
    border-radius: 16rpx;
    min-width: 80rpx;

    &.active {
      background: linear-gradient(135deg, #A8E6CF, #7FD8BE);
      border-color: #7FD8BE;

      .type-name {
        color: #fff;
      }
    }

    &:active {
      transform: scale(0.95);
    }

    .type-icon {
      font-size: 40rpx;
      margin-bottom: 8rpx;
    }

    .type-name {
      font-size: 24rpx;
      color: #4a5568;
    }
  }
}

// 日期行
.date-row {
  display: flex;
  align-items: center;
  gap: 16rpx;

  .date-input-wrapper {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: space-between;
    height: 88rpx;
    background: #f7fafc;
    border: 2rpx solid #e2e8f0;
    border-radius: 16rpx;
    padding: 0 24rpx;

    .date-text {
      font-size: 28rpx;
      color: #2d3748;

      &.placeholder {
        color: #a0aec0;
      }
    }
  }

  .date-type-toggle {
    display: flex;
    background: #f7fafc;
    border-radius: 16rpx;
    overflow: hidden;
    border: 2rpx solid #e2e8f0;

    .toggle-item {
      padding: 20rpx 24rpx;
      font-size: 26rpx;
      color: #4a5568;
      background: transparent;
      transition: all 0.2s;

      &.active {
        background: linear-gradient(135deg, #A8E6CF, #7FD8BE);
        color: #fff;
      }
    }
  }
}

// 开关行
.switch-row {
  display: flex;
  justify-content: space-between;
  align-items: center;

  .custom-switch {
    width: 88rpx;
    height: 48rpx;
    background: #e2e8f0;
    border-radius: 24rpx;
    position: relative;
    transition: all 0.3s;

    &.active {
      background: linear-gradient(135deg, #A8E6CF, #7FD8BE);

      .switch-dot {
        transform: translateX(40rpx);
      }
    }

    .switch-dot {
      width: 40rpx;
      height: 40rpx;
      background: #fff;
      border-radius: 50%;
      position: absolute;
      top: 4rpx;
      left: 4rpx;
      transition: all 0.3s;
      box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
    }
  }
}

// 提醒选项
.reminder-options {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;

  .reminder-option {
    padding: 12rpx 24rpx;
    background: #f7fafc;
    border: 2rpx solid #e2e8f0;
    border-radius: 24rpx;
    font-size: 26rpx;
    color: #4a5568;
    transition: all 0.2s;

    &.active {
      background: linear-gradient(135deg, #FFD3B6, #FFAAA5);
      border-color: #FFAAA5;
      color: #fff;
    }

    &:active {
      transform: scale(0.95);
    }
  }
}

// 底部按钮
.modal-footer {
  display: flex;
  padding: 24rpx 32rpx;
  gap: 20rpx;
  border-top: 2rpx solid #f1f5f9;

  .btn-delete {
    width: 80rpx;
    height: 80rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #fff0f0;
    border-radius: 40rpx;

    &:active {
      background: #ffe0e0;
    }
  }

  .btn-cancel,
  .btn-confirm {
    flex: 1;
    height: 80rpx;
    line-height: 80rpx;
    text-align: center;
    border-radius: 40rpx;
    font-size: 28rpx;
    font-weight: 500;
    border: none;

    &:active {
      transform: scale(0.98);
    }
  }

  .btn-cancel {
    background: #f7fafc;
    color: #4a5568;
  }

  .btn-confirm {
    background: linear-gradient(135deg, #A8E6CF, #7FD8BE);
    color: #fff;
  }
}
</style>
