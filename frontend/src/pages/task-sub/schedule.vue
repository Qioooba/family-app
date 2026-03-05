<template>
  <view class="page-container">
    <view class="header">
      <view class="header-title">家务排班表 📅</view>
      <view class="header-action" @click="showAddModal">
        <text class="icon">+</text>
      </view>
    </view>
    
    <!-- 今日值班 -->
    <view class="today-section">
      <view class="section-title">
        <text>今日值班</text>
        <text class="today-date">{{ todayDate }}</text>
      </view>
      <view v-if="todaySchedules.length > 0" class="today-list">
        <view 
          v-for="(item, index) in todaySchedules" 
          :key="item.id"
          class="today-item"
        >
          <view class="today-task">{{ item.taskName }}</view>
          <view class="today-assignee">
            <image :src="getAssigneeAvatar(item.assigneeId)" class="assignee-avatar" />
            <text>{{ getAssigneeName(item.assigneeId) }}</text>
          </view>
        </view>
      </view>
      <view v-else class="today-empty">
        <text>今天没有排班任务，休息一下吧！☕️</text>
      </view>
    </view>
    
    <!-- 周排班表 -->
    <view class="schedule-section">
      <view class="section-title">周排班表</view>
      
      <view class="week-view">
        <view 
          v-for="(day, dayIndex) in weekDays" 
          :key="dayIndex"
          class="day-card"
          :class="{ today: isToday(dayIndex + 1) }"
        >
          <view class="day-header">{{ day }}</view>
          <view class="day-content">
            <view 
              v-for="(schedule, sIndex) in getSchedulesByDay(dayIndex + 1)" 
              :key="schedule.id"
              class="schedule-item"
              :class="{ dragging: draggedSchedule?.id === schedule.id }"
              draggable="true"
              @longpress="onLongPress(schedule, $event)"
              @touchstart="onTouchStart(schedule, $event)"
              @touchend="onTouchEnd"
              @click="viewSchedule(schedule)"
            >
              <text class="item-name">{{ schedule.taskName }}</text>
              <view class="item-assignee">
                <image :src="getAssigneeAvatar(schedule.assigneeId)" class="mini-avatar" />
              </view>
            </view>
            <view 
              v-if="getSchedulesByDay(dayIndex + 1).length === 0" 
              class="empty-slot"
              @click="quickAdd(dayIndex + 1)"
            >
              <text class="empty-text">+</text>
            </view>
          </view>
        </view>
      </view>
    </view>
    
    <!-- 拖拽提示 -->
    <view v-if="draggedSchedule" class="drag-hint">
      <text>拖拽到其他日期可调整排班</text>
    </view>
    
    <!-- 添加排班弹窗 -->
    <view v-if="showModal" class="modal-overlay" @click="closeModal">
      <view class="modal-content" @click.stop>
        <view class="modal-header">
          <text>{{ isEdit ? '编辑排班' : '添加排班' }}</text>
          <text class="close-btn" @click="closeModal">✕</text>
        </view>
        
        <view class="form-item">
          <text class="label">任务名称</text>
          <input class="input" v-model="newSchedule.taskName" placeholder="如：洗碗、拖地" />
        </view>
        
        <view class="form-item">
          <text class="label">值班人员</text>
          <view class="member-list">
            <view 
              v-for="(member, index) in familyMembers" :key="member.id || index"
              class="member-option"
              :class="{ active: newSchedule.assigneeId === member.id }"
              @click="newSchedule.assigneeId = member.id"
            >
              <image :src="member.avatar" class="member-avatar" />
              <text>{{ member.name }}</text>
            </view>
          </view>
        </view>
        
        <view class="form-item">
          <text class="label">排班周期</text>
          <view class="type-options">
            <view 
              v-for="(type, index) in scheduleTypes" 
              :key="index"
              class="type-option"
              :class="{ active: newSchedule.scheduleType === type.value }"
              @click="newSchedule.scheduleType = type.value"
            >
              <text>{{ type.label }}</text>
            </view>
          </view>
        </view>
        
        <view class="form-item">
          <text class="label">周几</text>
          <view class="day-options">
            <view 
              v-for="(day, index) in weekDays" 
              :key="index"
              class="day-option"
              :class="{ active: newSchedule.scheduleDay === index + 1, today: isToday(index + 1) }"
              @click="newSchedule.scheduleDay = index + 1"
            >
              <text>{{ day }}</text>
            </view>
          </view>
        </view>
        
        <view class="form-actions">
          <button v-if="isEdit" class="btn-danger" @click="deleteSchedule">删除</button>
          <button class="btn-cancel" @click="closeModal">取消</button>
          <button class="btn-confirm" @click="saveSchedule">确认</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { scheduleApi, familyApi } from '../../api/index.js'

const weekDays = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
const scheduleTypes = [
  { label: '每周', value: 'weekly' },
  { label: '每天', value: 'daily' }
]

const familyId = ref(null)
const familyMembers = ref([])
const schedules = ref([])
const todaySchedules = ref([])
const showModal = ref(false)
const isEdit = ref(false)
const editingId = ref(null)
const draggedSchedule = ref(null)
const touchStartTime = ref(0)

const newSchedule = ref({
  taskName: '',
  assigneeId: null,
  scheduleType: 'weekly',
  scheduleDay: 1
})

const todayDate = computed(() => {
  const today = new Date()
  return `${today.getMonth() + 1}月${today.getDate()}日`
})

onMounted(() => {
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  familyId.value = currentPage.options.familyId || uni.getStorageSync('currentFamilyId')
  
  if (familyId.value) {
    loadFamilyMembers()
    loadSchedules()
    loadTodaySchedules()
  }
})

// 加载家庭成员
const loadFamilyMembers = async () => {
  try {
    const res = await familyApi.getMembers(familyId.value)
    familyMembers.value = res || []
    if (familyMembers.value.length > 0 && !newSchedule.value.assigneeId) {
      newSchedule.value.assigneeId = familyMembers.value[0].id
    }
  } catch (e) {
    console.error('加载家庭成员失败', e)
  }
}

// 加载排班列表
const loadSchedules = async () => {
  try {
    const res = await scheduleApi.getList(familyId.value)
    schedules.value = res || []
  } catch (e) {
    console.error('加载排班失败', e)
    uni.showToast({ title: '加载排班失败', icon: 'none' })
  }
}

// 加载今日排班
const loadTodaySchedules = async () => {
  try {
    const res = await scheduleApi.getToday(familyId.value)
    todaySchedules.value = res || []
  } catch (e) {
    console.error('加载今日排班失败', e)
  }
}

// 获取成员名称
const getAssigneeName = (id) => {
  const member = familyMembers.value.find(m => m.id === id)
  return member ? member.name : '未知'
}

// 获取成员头像
const getAssigneeAvatar = (id) => {
  const member = familyMembers.value.find(m => m.id === id)
  return member ? member.avatar : '../../static/avatar/default.png'
}

// 按日期获取排班
const getSchedulesByDay = (day) => {
  return schedules.value.filter(s => s.scheduleDay === day && s.status === 1)
}

// 判断是否是今天
const isToday = (day) => {
  const today = new Date().getDay()
  const dayOfWeek = today === 0 ? 7 : today
  return day === dayOfWeek
}

// 查看排班详情
const viewSchedule = (schedule) => {
  editingId.value = schedule.id
  newSchedule.value = {
    taskName: schedule.taskName,
    assigneeId: schedule.assigneeId,
    scheduleType: schedule.scheduleType,
    scheduleDay: schedule.scheduleDay
  }
  isEdit.value = true
  showModal.value = true
}

// 长按开始拖拽
const onLongPress = (schedule, e) => {
  draggedSchedule.value = schedule
  uni.vibrateShort()
}

// 触摸开始
const onTouchStart = (schedule, e) => {
  touchStartTime.value = Date.now()
}

// 触摸结束
const onTouchEnd = () => {
  const touchDuration = Date.now() - touchStartTime.value
  if (touchDuration < 300) {
    // 短按不处理，由 click 事件处理
    draggedSchedule.value = null
  }
}

// 显示添加弹窗
const showAddModal = () => {
  isEdit.value = false
  editingId.value = null
  newSchedule.value = {
    taskName: '',
    assigneeId: familyMembers.value[0]?.id,
    scheduleType: 'weekly',
    scheduleDay: new Date().getDay() || 7
  }
  showModal.value = true
}

// 快速添加
const quickAdd = (day) => {
  isEdit.value = false
  editingId.value = null
  newSchedule.value = {
    taskName: '',
    assigneeId: familyMembers.value[0]?.id,
    scheduleType: 'weekly',
    scheduleDay: day
  }
  showModal.value = true
}

// 关闭弹窗
const closeModal = () => {
  showModal.value = false
  isEdit.value = false
  editingId.value = null
}

// 保存排班
const saveSchedule = async () => {
  if (!newSchedule.value.taskName.trim()) {
    uni.showToast({ title: '请输入任务名称', icon: 'none' })
    return
  }
  
  try {
    const data = {
      familyId: familyId.value,
      taskName: newSchedule.value.taskName.trim(),
      assigneeId: newSchedule.value.assigneeId,
      scheduleType: newSchedule.value.scheduleType,
      scheduleDay: newSchedule.value.scheduleDay
    }
    
    await scheduleApi.create(data)
    
    uni.showToast({ title: '添加成功', icon: 'success' })
    closeModal()
    await loadSchedules()
    await loadTodaySchedules()
  } catch (e) {
    console.error('保存排班失败', e)
    uni.showToast({ title: '保存失败', icon: 'none' })
  }
}

// 删除排班
const deleteSchedule = async () => {
  uni.showModal({
    title: '确认删除',
    content: '确定删除这个排班吗？',
    confirmColor: '#FF4D4F',
    success: async (res) => {
      if (res.confirm) {
        try {
          await scheduleApi.delete(editingId.value)
          uni.showToast({ title: '已删除', icon: 'success' })
          closeModal()
          await loadSchedules()
          await loadTodaySchedules()
        } catch (e) {
          console.error('删除排班失败', e)
          uni.showToast({ title: '删除失败', icon: 'none' })
        }
      }
    }
  })
}
</script>

<style lang="scss" scoped>
.page-container {
  min-height: 100vh;
  background: #F5F7FA;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 50px 20px 20px;
  background: linear-gradient(135deg, #E3F2FD, #BBDEFB);
  
  .header-title {
    font-size: 22px;
    font-weight: 700;
    color: #2C3E50;
  }
  
  .header-action {
    width: 44px;
    height: 44px;
    background: #2196F3;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    
    .icon {
      font-size: 28px;
      color: #fff;
      font-weight: 300;
    }
  }
}

.today-section {
  margin: 15px;
  padding: 20px;
  background: #fff;
  border-radius: 16px;
  
  .section-title {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-size: 16px;
    font-weight: 600;
    color: #2C3E50;
    margin-bottom: 15px;
    
    .today-date {
      font-size: 13px;
      color: #999;
      font-weight: normal;
    }
  }
}

.today-list {
  .today-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 0;
    border-bottom: 1px solid #F0F0F0;
    
    &:last-child {
      border-bottom: none;
    }
    
    .today-task {
      font-size: 15px;
      color: #333;
    }
    
    .today-assignee {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 13px;
      color: #2196F3;
      
      .assignee-avatar {
        width: 28px;
        height: 28px;
        border-radius: 50%;
      }
    }
  }
}

.today-empty {
  text-align: center;
  padding: 30px;
  color: #999;
  font-size: 14px;
}

.schedule-section {
  margin: 15px;
  padding: 20px;
  background: #fff;
  border-radius: 16px;
  
  .section-title {
    font-size: 16px;
    font-weight: 600;
    color: #2C3E50;
    margin-bottom: 15px;
  }
}

.week-view {
  display: flex;
  gap: 8px;
  overflow-x: auto;
  padding-bottom: 10px;
  
  .day-card {
    flex: 1;
    min-width: 80px;
    background: #F8F9FA;
    border-radius: 12px;
    padding: 10px 5px;
    
    &.today {
      background: #E3F2FD;
      border: 2px solid #2196F3;
    }
    
    .day-header {
      text-align: center;
      font-size: 13px;
      color: #7F8C8D;
      margin-bottom: 10px;
      font-weight: 500;
    }
    
    .day-content {
      min-height: 100px;
      
      .schedule-item {
        background: #fff;
        border-radius: 8px;
        padding: 8px 5px;
        margin-bottom: 6px;
        box-shadow: 0 2px 4px rgba(0,0,0,0.05);
        
        &.dragging {
          opacity: 0.5;
          transform: scale(0.95);
        }
        
        .item-name {
          display: block;
          font-size: 11px;
          color: #333;
          font-weight: 500;
          margin-bottom: 4px;
          text-align: center;
        }
        
        .item-assignee {
          display: flex;
          justify-content: center;
          
          .mini-avatar {
            width: 20px;
            height: 20px;
            border-radius: 50%;
          }
        }
      }
      
      .empty-slot {
        height: 50px;
        display: flex;
        align-items: center;
        justify-content: center;
        border: 2px dashed #ddd;
        border-radius: 8px;
        
        .empty-text {
          font-size: 20px;
          color: #ccc;
        }
        
        &:active {
          border-color: #2196F3;
          background: #E3F2FD;
        }
      }
    }
  }
}

.drag-hint {
  position: fixed;
  bottom: 100px;
  left: 50%;
  transform: translateX(-50%);
  background: rgba(0,0,0,0.7);
  color: #fff;
  padding: 10px 20px;
  border-radius: 20px;
  font-size: 13px;
}

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
  background: #fff;
  border-radius: 20px;
  padding: 20px;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  font-size: 18px;
  font-weight: 600;
  
  .close-btn {
    font-size: 20px;
    color: #7F8C8D;
  }
}

.form-item {
  margin-bottom: 20px;
  
  .label {
    display: block;
    font-size: 14px;
    color: #2C3E50;
    margin-bottom: 8px;
  }
  
  .input {
    width: 100%;
    height: 44px;
    background: #F5F7FA;
    border-radius: 10px;
    padding: 0 15px;
    font-size: 14px;
  }
}

.member-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  
  .member-option {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 5px;
    padding: 10px 15px;
    background: #F5F7FA;
    border-radius: 12px;
    font-size: 13px;
    color: #666;
    
    .member-avatar {
      width: 36px;
      height: 36px;
      border-radius: 50%;
    }
    
    &.active {
      background: #E3F2FD;
      color: #2196F3;
      border: 2px solid #2196F3;
    }
  }
}

.type-options, .day-options {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  
  .type-option, .day-option {
    padding: 10px 20px;
    background: #F5F7FA;
    border-radius: 20px;
    font-size: 14px;
    color: #666;
    
    &.active {
      background: #2196F3;
      color: #fff;
    }
    
    &.today {
      border: 2px solid #2196F3;
    }
  }
}

.form-actions {
  display: flex;
  gap: 12px;
  margin-top: 30px;
  
  button {
    flex: 1;
    height: 44px;
    border-radius: 22px;
    font-size: 15px;
    border: none;
  }
  
  .btn-danger {
    background: #FFF1F0;
    color: #FF4D4F;
  }
  
  .btn-cancel {
    background: #F5F7FA;
    color: #7F8C8D;
  }
  
  .btn-confirm {
    background: #2196F3;
    color: #fff;
  }
}
</style>
