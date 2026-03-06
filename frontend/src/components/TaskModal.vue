<template>
  <view>
    <!-- 统一任务弹窗 - 中间弹出式 -->
    <u-popup
      v-model="visible"
      mode="center"
      border-radius="20"
      width="85%"
      :closeable="true"
      @close="close"
    >
      <view class="task-modal-content">
        <!-- 标题 -->
        <view class="modal-header">
          <text class="modal-title">{{ isEdit ? '编辑待办' : '添加待办' }}</text>
        </view>
        
        <!-- 表单内容 -->
        <view class="modal-body">
          <!-- 任务标题 -->
          <view class="form-item">
            <text class="form-label">任务标题</text>
            <input 
              class="form-input" 
              v-model="form.title" 
              placeholder="请输入待办事项"
            />
          </view>
          
          <!-- 备注 -->
          <view class="form-item">
            <text class="form-label">备注</text>
            <textarea 
              class="form-textarea" 
              v-model="form.remark" 
              placeholder="添加备注说明..."
              :auto-height="true"
            />
          </view>
          
          <!-- 截止时间 -->
          <view class="form-item">
            <text class="form-label">截止时间</text>
            <view class="form-picker" @click="showDatePicker = true">
              <text class="picker-text">{{ form.dueTime || '请选择时间' }}</text>
              <text class="picker-arrow">›</text>
            </view>
          </view>
          
          <!-- 指派给 -->
          <view class="form-item">
            <text class="form-label">指派给</text>
            <view class="form-picker" @click="showMemberPicker = true">
              <text class="picker-text">{{ getMemberName(form.assigneeId) || '点击选择' }}</text>
              <text class="picker-arrow">›</text>
            </view>
          </view>
        </view>
        
        <!-- 底部按钮 -->
        <view class="modal-footer">
          <button class="btn-cancel" @click="close">取消</button>
          <button class="btn-confirm" @click="save">{{ isEdit ? '保存' : '创建' }}</button>
        </view>
      </view>
    </u-popup>
    
    <!-- 日期时间选择器弹窗 -->
    <u-popup
      v-model="showDatePicker"
      mode="bottom"
      border-radius="20"
      height="50%"
      :closeable="true"
    >
      <view class="picker-content">
        <view class="picker-header">
          <text class="picker-title">选择时间</text>
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
        <view class="picker-actions">
          <button class="btn-cancel" @click="showDatePicker = false">取消</button>
          <button class="btn-confirm" @click="confirmDatePicker">确定</button>
        </view>
      </view>
    </u-popup>
    
    <!-- 成员选择弹窗 -->
    <u-popup
      v-model="showMemberPicker"
      mode="bottom"
      border-radius="20"
      height="50%"
      :closeable="true"
    >
      <view class="picker-content">
        <view class="picker-header">
          <text class="picker-title">选择家庭成员</text>
        </view>
        <scroll-view class="member-list" scroll-y>
          <view 
            v-for="member in familyMembers" 
            :key="member.userId"
            class="member-item"
            :class="{ active: form.assigneeId === member.userId }"
            @click="selectMember(member.userId)"
          >
            <image 
              class="member-avatar" 
              :src="member.avatar || '/static/avatar-default.png'" 
              mode="aspectFill" 
            />
            <text class="member-name">{{ member.nickname || member.name || '家人' }}</text>
            <text v-if="form.assigneeId === member.userId" class="member-check">✓</text>
          </view>
        </scroll-view>
      </view>
    </u-popup>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { taskApi, familyApi } from '../api/index.js'

// Props
const props = defineProps({
  // 可以传入家庭成员列表，如果不传则组件自己加载
  members: {
    type: Array,
    default: () => []
  }
})

// Emits
const emit = defineEmits(['success', 'close'])

// ========== 状态 ==========
const visible = ref(false)
const isEdit = ref(false)
const currentTaskId = ref(null)
const familyMembers = ref([])

// 表单数据
const form = ref({
  title: '',
  remark: '',
  dueTime: '',
  assigneeId: null
})

// 选择器状态
const showDatePicker = ref(false)
const showMemberPicker = ref(false)
const yearRange = ref([2024, 2025, 2026, 2027, 2028])
const pickerValue = ref([1, new Date().getMonth(), new Date().getDate() - 1, 15, 0])

// 计算当月天数
const daysInMonth = computed(() => {
  const year = yearRange.value[pickerValue.value[0]]
  const month = pickerValue.value[1] + 1
  return new Date(year, month, 0).getDate()
})

// ========== 方法 ==========

// 打开弹窗 - 创建模式
const open = () => {
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
  // 确保家庭成员已加载
  ensureFamilyMembers()
}

// 打开弹窗 - 编辑模式
const openEdit = (task) => {
  if (!task || !task.id) {
    console.error('TaskModal.openEdit: task or task.id is required')
    return
  }
  
  isEdit.value = true
  currentTaskId.value = task.id
  form.value = {
    title: task.title || '',
    remark: task.remark || '',
    dueTime: formatTime(task.dueTime) || '',
    assigneeId: task.assigneeId || null
  }
  
  visible.value = true
  // 确保家庭成员已加载
  ensureFamilyMembers()
}

// 关闭弹窗
const close = () => {
  visible.value = false
  isEdit.value = false
  currentTaskId.value = null
  form.value = {
    title: '',
    remark: '',
    dueTime: '',
    assigneeId: null
  }
  emit('close')
}

// 保存任务
const save = async () => {
  if (!form.value.title.trim()) {
    uni.showToast({ title: '请输入任务标题', icon: 'none' })
    return
  }

  try {
    const familyId = uni.getStorageSync('currentFamilyId') || 1
    const userInfo = uni.getStorageSync('userInfo') || {}
    const currentUserId = userInfo.id || userInfo.userId
    
    // 处理截止时间格式
    let dueTime = form.value.dueTime
    if (dueTime) {
      dueTime = dueTime.replace(' ', 'T')
      if (!dueTime.endsWith(':00')) {
        dueTime += ':00'
      }
    }
    
    // 处理 assigneeId，未选择时默认给自己
    let assigneeId = form.value.assigneeId
    if (!assigneeId) {
      assigneeId = currentUserId
    }

    const data = {
      title: form.value.title.trim(),
      remark: form.value.remark.trim(),
      familyId: familyId,
      dueTime: dueTime,
      assigneeId: assigneeId,
      status: 0,
      priority: 0
    }

    if (isEdit.value && currentTaskId.value) {
      // 更新任务
      data.id = currentTaskId.value
      await taskApi.update(data)
      uni.showToast({ title: '更新成功', icon: 'success' })
    } else {
      // 创建任务
      data.creatorId = currentUserId
      await taskApi.create(data)
      uni.showToast({ title: '添加成功', icon: 'success' })
    }
    
    close()
    emit('success')
  } catch (e) {
    console.error('保存任务失败', e)
    uni.showToast({ title: '保存失败: ' + (e.message || ''), icon: 'none', duration: 3000 })
  }
}

// 确保家庭成员已加载
const ensureFamilyMembers = async () => {
  if (props.members && props.members.length > 0) {
    familyMembers.value = props.members
    return
  }
  
  if (familyMembers.value.length === 0) {
    await loadFamilyMembers()
  }
}

// 加载家庭成员
const loadFamilyMembers = async () => {
  try {
    const familyId = uni.getStorageSync('currentFamilyId') || 1
    const members = await familyApi.getMembers(familyId)
    familyMembers.value = members || []
  } catch (e) {
    console.error('加载家庭成员失败', e)
    familyMembers.value = []
  }
}

// 获取成员名称
const getMemberName = (userId) => {
  if (!userId) return ''
  const member = familyMembers.value.find(m => m.userId === userId)
  return member?.nickname || member?.name || '家人'
}

// 日期选择器变化
const onPickerChange = (e) => {
  pickerValue.value = e.detail.value
}

// 确认日期选择
const confirmDatePicker = () => {
  const year = yearRange.value[pickerValue.value[0]]
  const month = String(pickerValue.value[1] + 1).padStart(2, '0')
  const day = String(pickerValue.value[2] + 1).padStart(2, '0')
  const hour = String(pickerValue.value[3]).padStart(2, '0')
  const minute = String(pickerValue.value[4]).padStart(2, '0')
  
  form.value.dueTime = `${year}-${month}-${day} ${hour}:${minute}:00`
  showDatePicker.value = false
}

// 选择成员
const selectMember = (userId) => {
  form.value.assigneeId = userId
  showMemberPicker.value = false
}

// 格式化时间
const formatTime = (timeValue) => {
  if (!timeValue) return ''
  if (Array.isArray(timeValue)) {
    const [year, month, day, hour, minute] = timeValue
    return `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')} ${String(hour).padStart(2, '0')}:${String(minute).padStart(2, '0')}:00`
  }
  return timeValue
}

// 暴露方法给父组件
defineExpose({
  open,
  openEdit,
  close
})
</script>

<style lang="scss" scoped>
.task-modal-content {
  padding: 24px;
  background: #fff;
}

.modal-header {
  text-align: center;
  margin-bottom: 24px;
  
  .modal-title {
    font-size: 20px;
    font-weight: 600;
    color: #2D5A3D;
  }
}

.modal-body {
  .form-item {
    margin-bottom: 20px;
    
    .form-label {
      display: block;
      font-size: 14px;
      color: #5A7A5A;
      margin-bottom: 8px;
      font-weight: 500;
    }
    
    .form-input {
      width: 100%;
      height: 48px;
      background: #F8FBF8;
      border-radius: 12px;
      padding: 0 16px;
      font-size: 16px;
      color: #3D5A4D;
      border: 1px solid #E8F5E9;
      box-sizing: border-box;
      
      &:focus {
        border-color: #81C784;
      }
    }
    
    .form-textarea {
      width: 100%;
      min-height: 80px;
      background: #F8FBF8;
      border-radius: 12px;
      padding: 12px 16px;
      font-size: 15px;
      color: #3D5A4D;
      border: 1px solid #E8F5E9;
      box-sizing: border-box;
      
      &:focus {
        border-color: #81C784;
      }
    }
    
    .form-picker {
      display: flex;
      align-items: center;
      justify-content: space-between;
      height: 48px;
      background: #F8FBF8;
      border-radius: 12px;
      padding: 0 16px;
      border: 1px solid #E8F5E9;
      
      .picker-text {
        font-size: 15px;
        color: #3D5A4D;
      }
      
      .picker-arrow {
        font-size: 20px;
        color: #8B9A8B;
      }
    }
  }
}

.modal-footer {
  display: flex;
  gap: 12px;
  margin-top: 24px;
  
  button {
    flex: 1;
    height: 44px;
    border-radius: 22px;
    font-size: 15px;
    border: none;
    display: flex;
    align-items: center;
    justify-content: center;
  }
  
  .btn-cancel {
    background: #F5F7FA;
    color: #7F8C8D;
  }
  
  .btn-confirm {
    background: linear-gradient(135deg, #81C784, #4CAF50);
    color: #fff;
  }
}

// ========== 选择器样式 ==========
.picker-content {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #fff;
}

.picker-header {
  padding: 20px;
  text-align: center;
  border-bottom: 1px solid #F0F5F0;
  
  .picker-title {
    font-size: 18px;
    font-weight: 600;
    color: #2D5A3D;
  }
}

.picker-view {
  flex: 1;
  height: 300px;
  
  .picker-item {
    line-height: 48px;
    text-align: center;
    font-size: 16px;
    color: #3D5A4D;
  }
}

.picker-actions {
  display: flex;
  gap: 12px;
  padding: 20px;
  border-top: 1px solid #F0F5F0;
  
  button {
    flex: 1;
    height: 44px;
    border-radius: 22px;
    font-size: 15px;
    border: none;
  }
  
  .btn-cancel {
    background: #F5F7FA;
    color: #7F8C8D;
  }
  
  .btn-confirm {
    background: linear-gradient(135deg, #81C784, #4CAF50);
    color: #fff;
  }
}

// 成员列表
.member-list {
  flex: 1;
  padding: 10px 20px;
  
  .member-item {
    display: flex;
    align-items: center;
    padding: 14px 16px;
    margin-bottom: 10px;
    background: #F8FBF8;
    border-radius: 12px;
    border: 2px solid transparent;
    transition: all 0.2s ease;
    
    &.active {
      border-color: #4CAF50;
      background: #E8F5E9;
    }
    
    &:active {
      transform: scale(0.98);
    }
    
    .member-avatar {
      width: 44px;
      height: 44px;
      border-radius: 50%;
      margin-right: 14px;
      background: linear-gradient(135deg, #81C784, #4CAF50);
    }
    
    .member-name {
      flex: 1;
      font-size: 16px;
      color: #3D5A4D;
    }
    
    .member-check {
      font-size: 18px;
      color: #4CAF50;
      font-weight: bold;
    }
  }
}
</style>