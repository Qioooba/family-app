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
      <view class="section-title">今日值班</view>
      <view v-if="todaySchedules.length > 0" class="today-list">
        <view 
          v-for="(item, index) in todaySchedules" 
          :key="index"
          class="today-item"
        >
          <view class="today-task">{{ item.taskName }}</view>
          <view class="today-assignee">👤 {{ getAssigneeName(item.assigneeId) }}</view>
        </view>
      </view>
      <view v-else class="today-empty">
        <text>今天没有排班任务，休息一下吧！☕️</text>
      </view>
    </view>
    
    <!-- 周排班表 -->
    <view class="schedule-section">
      <view class="section-title">周排班表</view>
      
      <view class="week-header">
        <view v-for="day in weekDays" :key="day" class="week-day">{{ day }}</view>
      </view>
      
      <view class="schedule-grid">
        <view 
          v-for="(day, dayIndex) in 7" 
          :key="dayIndex"
          class="day-column"
        >
          <view 
            v-for="(schedule, sIndex) in getSchedulesByDay(dayIndex + 1)" 
            :key="sIndex"
            class="schedule-item"
            @click="viewSchedule(schedule)"
          >
            <text class="item-name">{{ schedule.taskName }}</text>
            <text class="item-assignee">{{ getAssigneeName(schedule.assigneeId) }}</text>
          </view>
        </view>
      </view>
    </view>
    
    <!-- 添加排班弹窗 -->
    <view v-if="showModal" class="modal-overlay" @click="closeModal">
      <view class="modal-content" @click.stop>
        <view class="modal-header">
          <text>添加排班</text>
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
              v-for="member in familyMembers" 
              :key="member.id"
              class="member-option"
              :class="{ active: newSchedule.assigneeId === member.id }"
              @click="newSchedule.assigneeId = member.id"
            >
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
              :class="{ active: newSchedule.scheduleDay === index + 1 }"
              @click="newSchedule.scheduleDay = index + 1"
            >
              <text>{{ day }}</text>
            </view>
          </view>
        </view>
        
        
        <view class="form-actions">
          <button class="btn-cancel" @click="closeModal">取消</button>
          <button class="btn-confirm" @click="addSchedule">确认添加</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const weekDays = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
const scheduleTypes = [
  { label: '每周', value: 'weekly' },
  { label: '每天', value: 'daily' }
]

const familyMembers = ref([
  { id: 1, name: '爸爸' },
  { id: 2, name: '妈妈' },
  { id: 3, name: '宝贝' }
])

const schedules = ref([
  { id: 1, taskName: '洗碗', assigneeId: 1, scheduleType: 'weekly', scheduleDay: 1, status: 1 },
  { id: 2, taskName: '拖地', assigneeId: 2, scheduleType: 'weekly', scheduleDay: 2, status: 1 },
  { id: 3, taskName: '倒垃圾', assigneeId: 3, scheduleType: 'weekly', scheduleDay: 3, status: 1 },
  { id: 4, taskName: '洗碗', assigneeId: 2, scheduleType: 'weekly', scheduleDay: 4, status: 1 },
  { id: 5, taskName: '整理房间', assigneeId: 1, scheduleType: 'weekly', scheduleDay: 6, status: 1 }
])

const todaySchedules = ref([])
const showModal = ref(false)

const newSchedule = ref({
  taskName: '',
  assigneeId: null,
  scheduleType: 'weekly',
  scheduleDay: 1
})

onMounted(() => {
  loadTodaySchedules()
})

const loadTodaySchedules = () => {
  const today = new Date().getDay()
  const dayOfWeek = today === 0 ? 7 : today
  todaySchedules.value = schedules.value.filter(s => s.scheduleDay === dayOfWeek && s.status === 1)
}

const getAssigneeName = (id) => {
  const member = familyMembers.value.find(m => m.id === id)
  return member ? member.name : '未知'
}

const getSchedulesByDay = (day) => {
  return schedules.value.filter(s => s.scheduleDay === day && s.status === 1)
}

const viewSchedule = (schedule) => {
  uni.showModal({
    title: schedule.taskName,
    content: `值班人员：${getAssigneeName(schedule.assigneeId)}\n周期：${schedule.scheduleType === 'weekly' ? '每周' : '每天'}`,
    showCancel: true,
    confirmText: '删除',
    cancelText: '关闭',
    success: (res) => {
      if (res.confirm) {
        deleteSchedule(schedule)
      }
    }
  })
}

const deleteSchedule = (schedule) => {
  const index = schedules.value.findIndex(s => s.id === schedule.id)
  if (index > -1) {
    schedules.value.splice(index, 1)
    loadTodaySchedules()
    uni.showToast({ title: '已删除', icon: 'success' })
  }
}

const showAddModal = () => {
  newSchedule.value = {
    taskName: '',
    assigneeId: familyMembers.value[0]?.id,
    scheduleType: 'weekly',
    scheduleDay: 1
  }
  showModal.value = true
}

const closeModal = () => {
  showModal.value = false
}

const addSchedule = () => {
  if (!newSchedule.value.taskName) {
    uni.showToast({ title: '请输入任务名称', icon: 'none' })
    return
  }
  
  schedules.value.push({
    id: Date.now(),
    ...newSchedule.value,
    status: 1
  })
  
  loadTodaySchedules()
  uni.showToast({ title: '添加成功', icon: 'success' })
  closeModal()
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
    font-size: 16px;
    font-weight: 600;
    color: #2C3E50;
    margin-bottom: 15px;
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
      font-size: 13px;
      color: #2196F3;
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

.week-header {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 5px;
  margin-bottom: 10px;
  
  .week-day {
    text-align: center;
    font-size: 12px;
    color: #7F8C8D;
    padding: 8px 0;
  }
}

.schedule-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 5px;
  
  .day-column {
    min-height: 150px;
    background: #F8F9FA;
    border-radius: 8px;
    padding: 5px;
  }
  
  .schedule-item {
    background: #E3F2FD;
    border-radius: 6px;
    padding: 6px;
    margin-bottom: 5px;
    
    .item-name {
      display: block;
      font-size: 11px;
      color: #1976D2;
      font-weight: 500;
      margin-bottom: 2px;
    }
    
    .item-assignee {
      font-size: 10px;
      color: #2196F3;
    }
  }
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

.member-list, .type-options, .day-options {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  
  .member-option, .type-option, .day-option {
    padding: 10px 20px;
    background: #F5F7FA;
    border-radius: 20px;
    font-size: 14px;
    color: #666;
    
    &.active {
      background: #2196F3;
      color: #fff;
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
