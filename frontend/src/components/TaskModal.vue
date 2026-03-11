<template>
  <view class="task-modal-wrapper">
    <!-- 遮罩层 -->
    <view class="modal-mask" v-if="visible" @click="close"></view>
    
    <!-- 弹窗内容 -->
    <view class="modal-container" v-if="visible" :class="{ 'show': visible }">
      <!-- 头部 -->
      <view class="modal-header">
        <text class="modal-title">{{ isEdit ? '编辑待办' : '添加待办' }}</text>
        <view class="close-btn" @click="close">
          <text class="close-icon">×</text>
        </view>
      </view>
      
      <!-- 表单内容 -->
      <scroll-view class="modal-body" scroll-y>
        <!-- 任务标题 -->
        <view class="form-group">
          <text class="form-label">任务标题 <text class="required">*</text></text>
          <input 
            class="form-input" 
            v-model="form.title" 
            placeholder="请输入待办事项标题"
            maxlength="50"
          />
        </view>
        
        <!-- 备注 -->
        <view class="form-group">
          <text class="form-label">备注</text>
          <textarea 
            class="form-textarea" 
            v-model="form.remark" 
            placeholder="添加备注说明..."
            maxlength="200"
            auto-height
          />
        </view>
        
        <!-- 截止时间 -->
        <view class="form-group" @click="showDateTimePicker">
          <text class="form-label">截止时间</text>
          <view class="form-picker">
            <text class="picker-value" :class="{ 'placeholder': !form.dueTime }">
              {{ form.dueTime ? formatDisplayDateTime(form.dueTime) : '请选择截止时间' }}
            </text>
            <text class="picker-arrow">›</text>
          </view>
        </view>
        
        <!-- 指派给 -->
        <view class="form-group" @click="showMemberPicker">
          <text class="form-label">指派给</text>
          <view class="form-picker">
            <text class="picker-value" :class="{ 'placeholder': !form.assigneeId }">
              {{ getAssigneeName() }}
            </text>
            <text class="picker-arrow">›</text>
          </view>
        </view>
      </scroll-view>
      
      <!-- 底部按钮 -->
      <view class="modal-footer">
        <view class="btn btn-cancel" @click="close">取消</view>
        <view class="btn btn-submit" :class="{ 'disabled': !form.title.trim() }" @click="save">
          {{ isEdit ? '保存' : '创建' }}
        </view>
      </view>
      
      <!-- 时间选择器弹窗 -->
      <view class="picker-popup" v-if="showDatePicker" @click.stop>
        <view class="picker-header">
          <text class="picker-cancel" @click="showDatePicker = false">取消</text>
          <text class="picker-title">选择时间</text>
          <text class="picker-confirm" @click="confirmDateTime">确定</text>
        </view>
        <picker-view class="picker-view" :value="dateValue" @change="onDateChange">
          <picker-view-column>
            <view class="picker-item" v-for="year in years" :key="year">{{ year }}年</view>
          </picker-view-column>
          <picker-view-column>
            <view class="picker-item" v-for="month in 12" :key="month">{{ month }}月</view>
          </picker-view-column>
          <picker-view-column>
            <view class="picker-item" v-for="day in daysInMonth" :key="day">{{ day }}日</view>
          </picker-view-column>
          <picker-view-column>
            <view class="picker-item" v-for="hour in 24" :key="hour">{{ String(hour-1).padStart(2, '0') }}时</view>
          </picker-view-column>
          <picker-view-column>
            <view class="picker-item" v-for="minute in 60" :key="minute">{{ String(minute-1).padStart(2, '0') }}分</view>
          </picker-view-column>
        </picker-view>
      </view>
      
      <!-- 人员选择器弹窗 -->
      <view class="picker-popup member-popup" v-if="showMemberPickerFlag" @click.stop>
        <view class="picker-header">
          <text class="picker-cancel" @click="showMemberPickerFlag = false">取消</text>
          <text class="picker-title">选择指派人员</text>
          <text class="picker-confirm" @click="showMemberPickerFlag = false">确定</text>
        </view>
        <scroll-view class="member-list" scroll-y>
          <view 
            class="member-item" 
            v-for="member in members" 
            :key="member.userId"
            :class="{ active: form.assigneeId === member.userId }"
            @click="selectMember(member)"
          >
            <image class="member-avatar" :src="getAvatarUrl(member.avatar) || '/static/avatar-default.png'" />
            <text class="member-name">{{ member.nickname || member.name || '家人' }}</text>
            <view class="member-check" v-if="form.assigneeId === member.userId">✓</view>
          </view>
        </scroll-view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { taskApi } from '@/api/task.js'
import { formatDateTime, extractDateTime } from '@/utils/dateHelper.js'
import { getAvatarUrl } from '@/utils/request.js'

// 接收props
const props = defineProps({
  members: {
    type: Array,
    default: () => []
  }
})

// 发送事件
const emit = defineEmits(['success', 'close'])

// 响应式状态
const visible = ref(false)
const isEdit = ref(false)
const currentTaskId = ref(null)
const showDatePicker = ref(false)
const showMemberPickerFlag = ref(false)

// 表单数据
const form = ref({
  title: '',
  remark: '',
  dueTime: '',
  assigneeId: null
})

// 日期选择器数据
const years = Array.from({ length: 10 }, (_, i) => new Date().getFullYear() + i)
const dateValue = ref([0, new Date().getMonth(), new Date().getDate() - 1, new Date().getHours(), new Date().getMinutes()])

// 计算属性：当前月份的天数
const daysInMonth = computed(() => {
  const year = years[dateValue.value[0]]
  const month = dateValue.value[1] + 1
  return new Date(year, month, 0).getDate()
})

// 打开弹窗 - 创建模式
const open = () => {
  console.log('TaskModal open called')
  isEdit.value = false
  currentTaskId.value = null
  
  // 默认时间为当前时间
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  const hours = String(now.getHours()).padStart(2, '0')
  const minutes = String(now.getMinutes()).padStart(2, '0')
  
  form.value = {
    title: '',
    remark: '',
    dueTime: `${year}-${month}-${day} ${hours}:${minutes}:00`,
    assigneeId: null
  }
  
  visible.value = true
  console.log('弹窗已打开，visible:', visible.value)
}

// 打开弹窗 - 编辑模式
const openEdit = (task) => {
  console.log('TaskModal openEdit called', task)
  if (!task || !task.id) {
    console.error('编辑任务时task或task.id为空')
    return
  }
  
  isEdit.value = true
  currentTaskId.value = task.id
  
  // 使用 extractDateTime 处理数组格式或字符串格式的日期
  const { date, time } = extractDateTime(task.dueTime)
  const dueTimeStr = date && time ? `${date} ${time}:00` : ''
  
  form.value = {
    title: task.title || '',
    remark: task.remark || '',
    dueTime: dueTimeStr,
    assigneeId: task.assigneeId || null
  }
  
  visible.value = true
}

// 关闭弹窗
const close = () => {
  visible.value = false
  showDatePicker.value = false
  showMemberPickerFlag.value = false
  emit('close')
}

// 显示时间选择器
const showDateTimePicker = () => {
  console.log('显示时间选择器')
  showDatePicker.value = true
}

// 日期选择变化
const onDateChange = (e) => {
  dateValue.value = e.detail.value
}

// 确认时间选择
const confirmDateTime = () => {
  const year = years[dateValue.value[0]]
  const month = String(dateValue.value[1] + 1).padStart(2, '0')
  const day = String(dateValue.value[2] + 1).padStart(2, '0')
  const hour = String(dateValue.value[3]).padStart(2, '0')
  const minute = String(dateValue.value[4]).padStart(2, '0')
  
  // 使用空格格式: yyyy-MM-dd HH:mm:ss (后端期望的格式)
  form.value.dueTime = `${year}-${month}-${day} ${hour}:${minute}:00`
  showDatePicker.value = false
}

// 显示人员选择器
const showMemberPicker = () => {
  console.log('显示人员选择器，成员列表:', props.members)
  if (!props.members || props.members.length === 0) {
    uni.showToast({ title: '暂无家庭成员', icon: 'none' })
    return
  }
  showMemberPickerFlag.value = true
}

// 选择成员
const selectMember = (member) => {
  form.value.assigneeId = member.userId
}

// 获取指派人名称
const getAssigneeName = () => {
  if (!form.value.assigneeId) return '请选择指派人员'
  const member = props.members.find(m => m.userId === form.value.assigneeId)
  return member ? (member.nickname || member.name || '家人') : '未知'
}

// 格式化日期时间显示
const formatDisplayDateTime = (dateTimeStr) => {
  if (!dateTimeStr) return ''
  return formatDateTime(dateTimeStr, 'datetime')
}

// 保存任务
const save = async () => {
  if (!form.value.title.trim()) {
    uni.showToast({ title: '请输入任务标题', icon: 'none' })
    return
  }
  
  // 验证日期格式
  if (form.value.dueTime) {
    const dateRegex = /^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}$/
    if (!dateRegex.test(form.value.dueTime)) {
      console.error('日期格式不正确:', form.value.dueTime)
      uni.showToast({ title: '日期格式错误', icon: 'none' })
      return
    }
  }
  
  try {
    const userInfo = uni.getStorageSync('userInfo') || {}
    const familyId = uni.getStorageSync('currentFamilyId') || 1
    const userId = userInfo.id || userInfo.userId
    
    if (!userId) {
      uni.showToast({ title: '请先登录', icon: 'none' })
      return
    }
    
    // 准备请求数据
    const data = {
      title: form.value.title.trim(),
      remark: form.value.remark.trim(),
      familyId: Number(familyId),
      dueTime: form.value.dueTime,
      assigneeId: Number(form.value.assigneeId || userId),
      status: 0,
      priority: 0
    }
    
    console.log('保存任务数据:', JSON.stringify(data))
    
    // 调用API
    if (isEdit.value && currentTaskId.value) {
      data.id = currentTaskId.value
      const res = await taskApi.update(data)
      console.log('更新成功:', res)
      uni.showToast({ title: '更新成功', icon: 'success' })
    } else {
      data.creatorId = Number(userId)
      const res = await taskApi.create(data)
      console.log('创建成功:', res)
      uni.showToast({ title: '创建成功', icon: 'success' })
    }
    
    close()
    emit('success')
  } catch (error) {
    console.error('保存任务失败:', error)
    let msg = '保存失败'
    if (error.message) {
      msg += ': ' + error.message
    }
    uni.showToast({ title: msg, icon: 'none', duration: 3000 })
  }
}

// 暴露方法
defineExpose({
  open,
  openEdit,
  close
})
</script>

<style lang="scss" scoped>
.task-modal-wrapper {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 1000;
  pointer-events: none;
}

.modal-mask {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  pointer-events: auto;
  animation: fadeIn 0.2s ease;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.modal-container {
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%) scale(0.9);
  width: 85%;
  max-width: 600rpx;
  background: #fff;
  border-radius: 24rpx;
  overflow: hidden;
  pointer-events: auto;
  opacity: 0;
  transition: all 0.3s ease;
}

.modal-container.show {
  opacity: 1;
  transform: translate(-50%, -50%) scale(1);
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 32rpx 32rpx 24rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.modal-title {
  font-size: 36rpx;
  font-weight: 600;
  color: #333;
}

.close-btn {
  width: 56rpx;
  height: 56rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: #f5f5f5;
}

.close-icon {
  font-size: 40rpx;
  color: #999;
  line-height: 1;
}

.modal-body {
  max-height: 900rpx;
  padding: 24rpx 32rpx;
}

.form-group {
  margin-bottom: 32rpx;
}

.form-label {
  display: block;
  font-size: 28rpx;
  color: #666;
  margin-bottom: 16rpx;
}

.required {
  color: #ff4d4f;
  margin-left: 4rpx;
}

.form-input {
  width: 100%;
  height: 88rpx;
  padding: 0 24rpx;
  background: #f8f9fa;
  border-radius: 12rpx;
  font-size: 30rpx;
  color: #333;
  box-sizing: border-box;
}

.form-input::placeholder {
  color: #bbb;
}

.form-textarea {
  width: 100%;
  min-height: 160rpx;
  padding: 20rpx 24rpx;
  background: #f8f9fa;
  border-radius: 12rpx;
  font-size: 30rpx;
  color: #333;
  box-sizing: border-box;
}

.form-textarea::placeholder {
  color: #bbb;
}

.form-picker {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 88rpx;
  padding: 0 24rpx;
  background: #f8f9fa;
  border-radius: 12rpx;
}

.picker-value {
  font-size: 30rpx;
  color: #333;
}

.picker-value.placeholder {
  color: #bbb;
}

.picker-arrow {
  font-size: 32rpx;
  color: #999;
}

.modal-footer {
  display: flex;
  padding: 24rpx 32rpx 40rpx;
  gap: 24rpx;
}

.btn {
  flex: 1;
  height: 88rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 44rpx;
  font-size: 32rpx;
  font-weight: 500;
}

.btn-cancel {
  background: #f5f5f5;
  color: #666;
}

.btn-submit {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
}

.btn-submit.disabled {
  background: #ccc;
  pointer-events: none;
}

// 选择器弹窗
.picker-popup {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  background: #fff;
  border-radius: 24rpx 24rpx 0 0;
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
  animation: slideUp 0.3s ease;
}

@keyframes slideUp {
  from { transform: translateY(100%); }
  to { transform: translateY(0); }
}

.picker-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 32rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

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
  color: #667eea;
  font-weight: 500;
}

.picker-view {
  height: 400rpx;
}

.picker-item {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32rpx;
  color: #333;
}

// 人员选择器
.member-popup {
  max-height: 60vh;
}

.member-list {
  max-height: 400rpx;
  padding: 0 32rpx;
}

.member-item {
  display: flex;
  align-items: center;
  padding: 24rpx 0;
  border-bottom: 1rpx solid #f5f5f5;
}

.member-item.active {
  background: #f0f4ff;
  margin: 0 -32rpx;
  padding: 24rpx 32rpx;
}

.member-avatar {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  margin-right: 24rpx;
}

.member-name {
  flex: 1;
  font-size: 30rpx;
  color: #333;
}

.member-check {
  width: 40rpx;
  height: 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #667eea;
  border-radius: 50%;
  color: #fff;
  font-size: 24rpx;
}
</style>