<template>
  <view class="page-container">
    <!-- 头部 -->
    <view class="header">
      <view class="header-title">待办事项</view>
      <view class="header-action" @click="showAddModal">
        <text class="icon">+</text>
      </view>
    </view>
    
    <!-- 分类标签 -->
    <scroll-view class="category-tabs" scroll-x>
      <view 
        v-for="(cat, index) in categories" 
        :key="cat.id || index"
        class="category-tab"
        :class="{ active: currentCategory === index }"
        @click="switchCategory(index)"
      >
        <text>{{ cat.name }}</text>
        <text v-if="cat.count > 0" class="tab-badge">{{ cat.count }}</text>
      </view>
    </scroll-view>
    
    <!-- 任务列表 -->
    <scroll-view class="task-list" scroll-y>
      <view 
        v-for="(task, index) in filteredTasks" 
        :key="task.id || index"
        class="task-card"
        :class="{ completed: task.status === 2, expanded: task.showSubtasks }"
        @click="viewTask(task)"
      >
        <view class="task-header">
          <view 
            class="checkbox" 
            :class="{ checked: task.status === 2 }" 
            @tap.stop="toggleTask(task)"
          ></view>
          <view class="task-title">{{ task.title }}</view>
          <view class="task-priority" :class="'priority-' + task.priority">{{ priorityText(task.priority) }}</view>
        </view>
        
        <view class="task-info">
          <text class="task-time">⏰ {{ task.dueTime }}</text>
          <text class="task-assignee">👤 {{ task.assigneeName || '未指派' }}</text>
        </view>
      </view>
      
      <view v-if="filteredTasks.length === 0" class="empty-state">
        <text class="empty-icon">📝</text>
        <text class="empty-text">暂无任务，点击右上角添加</text>
      </view>
    </scroll-view>
    
    <!-- 任务详情弹窗（含子任务） -->
    <view v-if="showDetailModal" class="modal-overlay" @click="closeDetailModal">
      <view class="modal-content detail-modal" @click.stop>
        <view class="modal-header">
          <text>任务详情</text>
          <text class="close-btn" @click="closeDetailModal">✕</text>
        </view>
        
        <view v-if="selectedTask" class="task-detail">
          <!-- 状态标签 -->
          <view class="detail-status" :class="selectedTask.status === 2 ? 'completed' : 'pending'">
            {{ selectedTask.status === 2 ? '✅ 已完成' : '📋 待处理' }}
          </view>
          
          <!-- 任务标题 -->
          <view class="detail-title">{{ selectedTask.title }}</view>
          
          <!-- 任务信息卡片 -->
          <view class="detail-cards">
            <view class="info-card">
              <text class="card-icon">⏰</text>
              <view class="card-content">
                <text class="card-label">截止时间</text>
                <text class="card-value">{{ selectedTask.dueTime || '未设置' }}</text>
              </view>
            </view>
            
            <view class="info-card">
              <text class="card-icon">👤</text>
              <view class="card-content">
                <text class="card-label">指派人</text>
                <text class="card-value">{{ selectedTask.assigneeName || '未指派' }}</text>
              </view>
            </view>
            
            <view class="info-card">
              <text class="card-icon">⚡</text>
              <view class="card-content">
                <text class="card-label">优先级</text>
                <text class="card-value priority-text" :class="'priority-' + selectedTask.priority">
                  {{ ['普通', '重要', '紧急'][selectedTask.priority] || '普通' }}
                </text>
              </view>
            </view>
          </view>
          
          <!-- 操作按钮 -->
          <view class="detail-actions">
            <view class="action-btn complete-btn" @tap="toggleTask(selectedTask)">
              {{ selectedTask.status === 2 ? '🔄 恢复任务' : '✅ 完成任务' }}
            </view>
            <view class="action-btn delete-btn" @click="deleteTask(selectedTask)">
              🗑️ 删除
            </view>
          </view>
        </view>
      </view>
    </view>
    
    <!-- 添加任务弹窗 - 新样式 -->
    <view v-if="showModal" class="modal-overlay" @click="closeModal">
      <view class="modal-content task-modal" @click.stop>
        <!-- 顶部：添加待办 -->
        <view class="modal-top">
          <view class="close-btn" @click="closeModal">
            <text class="close-icon">✕</text>
          </view>
          <text class="modal-title">添加待办</text>
          <view class="save-btn" @click="addTask">
            <text class="save-text">保存</text>
          </view>
        </view>
        
        <!-- 中间：输入框 -->
        <view class="modal-body">
          <input 
            class="task-input" 
            v-model="newTask.title" 
            placeholder="请输入待办事项"
            focus
          />
        </view>
        
        <!-- 底部：提醒时间和分配人员 -->
        <view class="modal-bottom">
          <!-- 提醒时间 -->
          <view class="form-row" @click="showDateTimePicker">
            <text class="row-icon">⏰</text>
            <text class="row-label">提醒时间</text>
            <text class="row-value">{{ newTask.dueDate || '今天' }} {{ newTask.dueTime || '15:00' }}</text>
          </view>
          
          <!-- 分配人员 -->
          <view class="form-row" @click="openMemberPicker">
            <text class="row-icon">👤</text>
            <text class="row-label">分配人员</text>
            <view class="member-select">
              <text v-if="!newTask.assigneeId" class="select-placeholder">点击选择</text>
              <text v-else class="select-value">{{ getMemberName(newTask.assigneeId) }}</text>
              <text class="row-arrow">›</text>
            </view>
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
              <view v-for="month in 12" :key="month" class="picker-item">{{ month }}月</view>
            </picker-view-column>
            <picker-view-column>
              <view v-for="day in daysInMonth" :key="day" class="picker-item">{{ day }}日</view>
            </picker-view-column>
            <picker-view-column>
              <view v-for="hour in 24" :key="hour-1" class="picker-item">{{ String(hour-1).padStart(2, '0') }}时</view>
            </picker-view-column>
            <picker-view-column>
              <view v-for="minute in minutes" :key="minute" class="picker-item">{{ String(minute).padStart(2, '0') }}分</view>
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
              class="member-item" 
              :class="{ active: !newTask.assigneeId }"
              @click="selectMember(null)"
            >
              <view class="member-avatar default">自己</view>
              <text class="member-name">自己</text>
            </view>
            <view 
              v-for="member in familyMembers" 
              :key="member.userId"
              class="member-item"
              :class="{ active: newTask.assigneeId === member.userId }"
              @click="selectMember(member.userId)"
            >
              <image 
                class="member-avatar" 
                :src="member.avatar || '/static/avatar-default.png'" 
                mode="aspectFill" 
              />
              <!-- 优先显示昵称 -->
              <text class="member-name">{{ member.nickname || member.name || '家人' }}</text>
            </view>
          </view>
        </view>
      </view>
    </view>
    
    <!-- 添加子任务弹窗 -->
    <view v-if="showSubtaskModal" class="modal-overlay" @click="closeSubtaskModal">
      <view class="modal-content" @click.stop>
        <view class="modal-header">
          <text>添加子任务</text>
          <text class="close-btn" @click="closeSubtaskModal">✕</text>
        </view>
        <view class="form-item">
          <input class="input" v-model="newSubtaskTitle" placeholder="输入子任务名称" />
        </view>
        <view class="form-actions">
          <button class="btn-cancel" @click="closeSubtaskModal">取消</button>
          <button class="btn-confirm" @click="confirmAddSubtask">确认</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { onShow, onLoad } from '@dcloudio/uni-app'
import { taskApi, familyApi } from '../../api/index.js'

// 页面加载时检查是否需要打开添加弹窗
onLoad((options) => {
  if (options && options.action === 'add') {
    // 延迟一点打开，确保页面渲染完成
    setTimeout(() => {
      showModal.value = true
    }, 300)
  }
})

// 获取今天的日期字符串
const getTodayString = () => {
  const today = new Date()
  const year = today.getFullYear()
  const month = String(today.getMonth() + 1).padStart(2, '0')
  const day = String(today.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

const categories = ref([
  { name: '待办', id: 1, status: 0, count: 0 },
  { name: '完成', id: 3, status: 2, count: 0 }
])

// 计算各分类数量
const updateCategoryCounts = async () => {
  try {
    const familyId = uni.getStorageSync('currentFamilyId') || 1
    // 分别查询待办和已完成的任务数量
    const [todoRes, doneRes] = await Promise.all([
      taskApi.getList(familyId, 0),
      taskApi.getList(familyId, 2)
    ])
    categories.value[0].count = (todoRes || []).length
    categories.value[1].count = (doneRes || []).length
  } catch (e) {
    console.error('更新分类数量失败', e)
  }
}

const priorities = ['普通', '重要', '紧急']

const currentCategory = ref(0)
const showModal = ref(false)
const showDetailModal = ref(false)
const showSubtaskModal = ref(false)
const selectedTask = ref(null)
const currentSubtaskTask = ref(null)
const newSubtaskTitle = ref('')
const tasks = ref([])
const loading = ref(false)
const isRefreshing = ref(false) // 防重复加载标志

// 家庭成员列表
const familyMembers = ref([
  { userId: 1, nickname: '爸爸' },
  { userId: 2, nickname: '妈妈' },
  { userId: 3, nickname: '孩子' }
])
const showTimePicker = ref(false)
const showMemberPicker = ref(false)
const pickerValue = ref([0, 0, 0, 15, 0])
const currentYear = new Date().getFullYear()
const yearRange = computed(() => {
  const years = []
  for (let i = currentYear - 1; i <= currentYear + 1; i++) {
    years.push(i)
  }
  return years
})

// 计算每月天数
const daysInMonth = computed(() => {
  const year = yearRange.value[pickerValue.value[0]] || currentYear
  const month = (pickerValue.value[1] || 0) + 1
  return new Date(year, month, 0).getDate()
})

// 分钟选项（5分钟步长）
const minutes = computed(() => {
  const mins = []
  for (let i = 0; i < 60; i += 5) {
    mins.push(i)
  }
  return mins
})

// 加载任务列表
const loadTasks = async (force = false) => {
  // 防重复：如果正在加载且不是强制刷新，则跳过
  if (!force && isRefreshing.value) {
    return
  }
  isRefreshing.value = true
  loading.value = true
  try {
    // 从本地存储获取家庭ID
    const familyId = uni.getStorageSync('currentFamilyId') || 1
    // 先加载家庭成员
    await loadFamilyMembers()
    // 获取当前分类的状态
    const status = categories.value[currentCategory.value]?.status
    // 根据状态查询任务
    const res = await taskApi.getList(familyId, status)
    // 把 assigneeId 转换为 assigneeName - 使用服务器返回的nickname
    tasks.value = (res.list || []).map(task => ({
      ...task,
      assigneeName: task.assigneeNickname || getMemberName(task.assigneeId) || '未指派'
    }))
    // 更新分类数量
    categories.value[0].count = res.todoCount || 0
    categories.value[1].count = res.doneCount || 0
  } catch (e) {
    console.error('加载任务失败', e)
    uni.showToast({ title: '加载任务失败', icon: 'none' })
  } finally {
    loading.value = false
    isRefreshing.value = false
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
    // 失败时使用默认成员
    familyMembers.value = [
      { userId: 1, nickname: '爸爸' },
      { userId: 2, nickname: '妈妈' },
      { userId: 3, nickname: '孩子' }
    ]
  }
}

// 页面加载时获取任务
onMounted(() => {
  loadTasks(true)
  loadFamilyMembers()
})

// 页面显示时自动刷新数据
onShow(() => {
  loadTasks(true).then(() => {
    // 延迟一点执行，确保任务加载完成
    setTimeout(() => {
      // 检查是否需要打开添加弹窗（从首页跳转过来）
      const openAddModal = uni.getStorageSync('taskOpenAddModal')
      if (openAddModal) {
        uni.removeStorageSync('taskOpenAddModal')
        showAddModal()
        return
      }
      
      // 检查是否有传入的任务ID（优先从本地存储读取）
      let taskId = uni.getStorageSync('pendingTaskDetailId')
      uni.removeStorageSync('pendingTaskDetailId')
      
      if (taskId) {
        const task = tasks.value.find(t => t.id === parseInt(taskId))
        if (task) {
          selectedTask.value = task
          showDetailModal.value = true
        }
      }
    }, 100)
  })
})

// 获取成员名称 - 显示昵称
const getMemberName = (userId) => {
  if (!userId) return '未指派'
  if (!familyMembers.value || !Array.isArray(familyMembers.value)) return '家人'
  const member = familyMembers.value.find(m => m.userId === userId)
  return member?.nickname || member?.name || '家人'
}

const newTask = ref({
  title: '',
  dueDate: getTodayString(),
  dueTime: '15:00',
  priority: 0,
  assigneeId: null
})

// 日期时间选择器 - 打开 picker 并初始化为当前时间
const showDateTimePicker = () => {
  const now = new Date()
  const yearIndex = yearRange.value.indexOf(now.getFullYear())
  const monthIndex = now.getMonth()
  const dayIndex = now.getDate() - 1
  const hourIndex = now.getHours()
  // 找到最近的5分钟刻度
  const minuteIndex = Math.floor(now.getMinutes() / 5)
  
  pickerValue.value = [yearIndex >= 0 ? yearIndex : 1, monthIndex, dayIndex, hourIndex, minuteIndex]
  showTimePicker.value = true
}

const closePicker = () => {
  showTimePicker.value = false
}

const onPickerChange = (e) => {
  pickerValue.value = e.detail.value
}

const confirmPicker = () => {
  const year = yearRange.value[pickerValue.value[0]]
  const month = String(pickerValue.value[1] + 1).padStart(2, '0')
  const day = String(pickerValue.value[2] + 1).padStart(2, '0')
  const hour = String(pickerValue.value[3]).padStart(2, '0')
  const minute = String(minutes.value[pickerValue.value[4]]).padStart(2, '0')
  newTask.value.dueDate = `${year}-${month}-${day}`
  newTask.value.dueTime = `${hour}:${minute}`
  closePicker()
}

// 成员选择
const openMemberPicker = () => {
  showMemberPicker.value = true
}

const closeMemberPicker = () => {
  showMemberPicker.value = false
}

const selectMember = (userId) => {
  newTask.value.assigneeId = userId
  closeMemberPicker()
}

const filteredTasks = computed(() => {
  try {
    if (!tasks.value || !Array.isArray(tasks.value)) {
      return []
    }
    // 根据当前分类过滤任务
    const status = categories.value[currentCategory.value]?.status
    let result = tasks.value
    if (status !== undefined) {
      result = result.filter(t => t.status === status)
    }
    
    // 按距离现在的时间由近及远排序（没有截止时间的排最后）
    return result.sort((a, b) => {
      // 都没有时间，保持原顺序
      if (!a.dueDate && !b.dueDate) return 0
      // a没有时间，排后面
      if (!a.dueDate) return 1
      // b没有时间，排后面
      if (!b.dueDate) return -1
      
      // 构建完整时间字符串
      const dateTimeA = `${a.dueDate} ${a.dueTime || '00:00'}`
      const dateTimeB = `${b.dueDate} ${b.dueTime || '00:00'}`
      
      // 计算与当前时间的距离（毫秒）
      const now = new Date().getTime()
      const timeA = new Date(dateTimeA).getTime()
      const timeB = new Date(dateTimeB).getTime()
      
      // 距离现在越近的排在越前面
      return Math.abs(timeA - now) - Math.abs(timeB - now)
    })
  } catch(e) {
    console.error('filteredTasks error:', e)
    return []
  }
})

const switchCategory = async (index) => {
  currentCategory.value = index
  // 切换分类时重新加载对应状态的任务
  await loadTasks(true)
}

const priorityText = (p) => priorities[p] || '普通'

const toggleTask = async (task) => {
  const newStatus = task.status === 2 ? 0 : 2
  try {
    console.log('toggleTask:', task.id, newStatus)
    if (newStatus === 2) {
      await taskApi.complete(task.id)
    }
    task.status = newStatus
    uni.showToast({ title: newStatus === 2 ? '任务已完成' : '任务已恢复', icon: 'none' })
  } catch (e) {
    console.error('更新任务状态失败', e)
    uni.showToast({ title: '操作失败: ' + e.message, icon: 'none' })
  }
}

const viewTask = (task) => {
  selectedTask.value = task
  showDetailModal.value = true
}

const closeDetailModal = () => {
  showDetailModal.value = false
  selectedTask.value = null
}

const subtaskCompleted = (task) => {
  if (!task.subtasks) return 0
  return task.subtasks.filter(s => s.status === 1).length
}

const subtaskProgress = (task) => {
  if (!task.subtasks || task.subtasks.length === 0) return 0
  return Math.round((subtaskCompleted(task) / task.subtasks.length) * 100)
}

const toggleSubtask = (task, sub) => {
  sub.status = sub.status === 0 ? 1 : 0
  // 检查是否全部完成
  if (subtaskProgress(task) === 100) {
    uni.showToast({ title: '所有子任务完成！', icon: 'success' })
  }
}

const addSubtask = (task) => {
  currentSubtaskTask.value = task
  newSubtaskTitle.value = ''
  showSubtaskModal.value = true
}

const closeSubtaskModal = () => {
  showSubtaskModal.value = false
  currentSubtaskTask.value = null
}

const confirmAddSubtask = () => {
  if (!newSubtaskTitle.value.trim()) {
    uni.showToast({ title: '请输入子任务名称', icon: 'none' })
    return
  }
  
  if (!currentSubtaskTask.value.subtasks) {
    currentSubtaskTask.value.subtasks = []
  }
  
  currentSubtaskTask.value.subtasks.push({
    title: newSubtaskTitle.value,
    status: 0
  })
  
  uni.showToast({ title: '添加成功', icon: 'success' })
  closeSubtaskModal()
}

const deleteSubtask = (task, sub, index) => {
  uni.showModal({
    title: '确认删除',
    content: '确定要删除这个子任务吗？',
    success: (res) => {
      if (res.confirm) {
        task.subtasks.splice(index, 1)
        uni.showToast({ title: '已删除', icon: 'success' })
      }
    }
  })
}

const deleteTask = async (task) => {
  uni.showModal({
    title: '确认删除',
    content: '确定要删除这个任务吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          await taskApi.delete(task.id)
          tasks.value = tasks.value.filter(t => t.id !== task.id)
          updateCategoryCounts()
          closeDetailModal()
          uni.showToast({ title: '已删除', icon: 'success' })
        } catch (e) {
          uni.showToast({ title: '删除失败', icon: 'none' })
        }
      }
    }
  })
}

const goToSchedule = () => {
  uni.navigateTo({ url: '/pages/task/schedule' })
}

const goToCalendar = () => {
  uni.navigateTo({ url: '/pages/task/calendar' })
}

const showAddModal = () => {
  // 重置表单
  newTask.value = {
    title: '',
    dueDate: getTodayString(),
    dueTime: '15:00',
    priority: 0,
    assigneeId: null
  }
  showModal.value = true
}

const closeModal = () => {
  showModal.value = false
  newTask.value = { 
    title: '', 
    dueDate: getTodayString(), 
    dueTime: '15:00',
    priority: 0, 
    assigneeId: null 
  }
}

const onDateChange = (e) => {
  newTask.value.dueDate = e.detail.value
}

const addTask = async () => {
  if (!newTask.value.title) {
    uni.showToast({ title: '请输入任务标题', icon: 'none' })
    return
  }

  try {
    const familyId = uni.getStorageSync('currentFamilyId') || 1
    // 获取当前用户ID（用于"分配给自己"的情况）
    const userInfo = uni.getStorageSync('userInfo') || {}
    const currentUserId = userInfo.id || userInfo.userId
    
    // 组合日期和时间
    let dueTimeValue = null
    if (newTask.value.dueDate && newTask.value.dueTime) {
      dueTimeValue = newTask.value.dueDate + ' ' + newTask.value.dueTime
    }
    
    // 处理分配人员：如果没有选择 assigneeId 或者选择的是"自己"
    let assigneeId = newTask.value.assigneeId
    if (assigneeId === null || assigneeId === undefined || assigneeId === '') {
      // 未选择时默认为当前用户（分配给自己）
      assigneeId = currentUserId || null
    }
    
    const data = {
      title: newTask.value.title,
      familyId: familyId,
      priority: newTask.value.priority || 0,
      dueTime: dueTimeValue,
      assigneeId: assigneeId
    }
    console.log('创建任务数据:', data)
    const res = await taskApi.create(data)
    console.log('创建任务结果:', res)
    uni.showToast({ title: '添加成功', icon: 'success' })
    closeModal()
    // 重新加载任务列表
    loadTasks()
  } catch (e) {
    console.error('创建任务失败', e)
    uni.showToast({ title: '创建失败: ' + (e.message || e), icon: 'none', duration: 3000 })
  }
}
</script>

<style lang="scss" scoped>
.page-container {
  min-height: 100vh;
  background: #F8FBF8;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 60px 24px 24px;
  background: linear-gradient(180deg, #E8F5E9 0%, #F8FBF8 100%);
  
  .header-title {
    font-size: 28px;
    font-weight: 600;
    color: #2D5A3D;
    letter-spacing: 1px;
  }
  
  .header-action {
    width: 48px;
    height: 48px;
    background: linear-gradient(135deg, #81C784, #4CAF50);
    border-radius: 16px;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 4px 12px rgba(76, 175, 80, 0.3);
    
    .icon {
      font-size: 28px;
      color: #fff;
      font-weight: 300;
    }
  }
}

.category-tabs {
  padding: 16px 20px;
  white-space: nowrap;
  background: #fff;
  border-bottom: 1px solid #F0F5F0;
  
  .category-tab {
    display: inline-block;
    padding: 10px 24px;
    margin-right: 12px;
    background: #F5FAF5;
    border-radius: 24px;
    font-size: 14px;
    color: #8B9A8B;
    transition: all 0.3s ease;
    position: relative;
    
    &.active {
      background: linear-gradient(135deg, #81C784, #4CAF50);
      color: #fff;
      box-shadow: 0 4px 12px rgba(76, 175, 80, 0.25);
    }
  }
  
  .tab-badge {
    display: inline-block;
    min-width: 20px;
    height: 20px;
    line-height: 20px;
    padding: 0 6px;
    margin-left: 6px;
    background: linear-gradient(135deg, #FF6B6B, #FF8E53);
    color: #fff;
    border-radius: 10px;
    font-size: 12px;
    font-weight: 600;
    box-shadow: 0 2px 8px rgba(255, 107, 107, 0.3);
  }
}

.view-switcher {
  display: flex;
  gap: 10px;
  margin: 10px 15px;
  
  .view-item {
    flex: 1;
    display: flex;
    align-items: center;
    padding: 12px 16px;
    background: linear-gradient(135deg, #E3F2FD, #BBDEFB);
    border-radius: 12px;
    
    &:first-child {
      background: linear-gradient(135deg, #EDE9FE, #DDD6FE);
    }
    
    .view-icon {
      font-size: 20px;
      margin-right: 10px;
    }
    
    .view-text {
      flex: 1;
      font-size: 14px;
      color: #1976D2;
      font-weight: 500;
    }
    
    &:first-child .view-text {
      color: #7C3AED;
    }
    
    .view-arrow {
      font-size: 18px;
      color: #1976D2;
    }
    
    &:first-child .view-arrow {
      color: #7C3AED;
    }
  }
}

.task-list {
  padding: 15px;
  height: calc(100vh - 220px);
}

.task-card {
  background: #fff;
  border-radius: 20px;
  padding: 20px;
  margin: 12px 20px;
  box-shadow: 0 2px 16px rgba(0, 0, 0, 0.04);
  border: 1px solid #F5FAF5;
  transition: all 0.3s ease;
  
  &:active {
    transform: scale(0.98);
  }
  
  &.completed {
    opacity: 0.65;
    background: #FAFFFA;
    .task-title {
      text-decoration: line-through;
      color: #A8D8A8;
    }
  }
  
  &.expanded {
    .subtask-list {
      display: block;
    }
  }
}

.task-header {
  display: flex;
  align-items: center;
  margin-bottom: 14px;
  
  .checkbox {
    width: 24px;
    height: 24px;
    border: 2px solid #C8E6C9;
    border-radius: 50%;
    margin-right: 14px;
    transition: all 0.3s ease;
    
    &.checked {
      background: linear-gradient(135deg, #81C784, #4CAF50);
      border-color: #4CAF50;
    }
  }
  
  .task-title {
    flex: 1;
    font-size: 17px;
    font-weight: 500;
    color: #3D5A4D;
    line-height: 1.4;
  }
  
  .task-priority {
    padding: 5px 14px;
    border-radius: 16px;
    font-size: 12px;
    font-weight: 500;
    
    &.priority-0 {
      background: #E8F5E9;
      color: #4CAF50;
    }
    
    &.priority-1 {
      background: #FFF3E0;
      color: #FF9800;
    }
    
    &.priority-2 {
      background: #FFEBEE;
      color: #F44336;
    }
  }
}

.task-info {
  display: flex;
  gap: 16px;
  font-size: 13px;
  color: #8B9A8B;
  margin-bottom: 12px;
}

.subtask-progress {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
  
  .progress-bar {
    flex: 1;
    height: 6px;
    background: #E0E6ED;
    border-radius: 3px;
    overflow: hidden;
    
    .progress-fill {
      height: 100%;
      background: #4CAF50;
      border-radius: 3px;
      transition: width 0.3s;
    }
  }
  
  .progress-text {
    font-size: 11px;
    color: #7F8C8D;
  }
}

.subtask-list {
  display: none;
  margin: 10px 0;
  padding: 10px;
  background: #F8F9FA;
  border-radius: 10px;
  
  .subtask-item {
    display: flex;
    align-items: center;
    padding: 8px 0;
    
    .subtask-checkbox {
      width: 18px;
      height: 18px;
      border: 2px solid #DDD;
      border-radius: 50%;
      margin-right: 10px;
      
      &.checked {
        background: #4CAF50;
        border-color: #4CAF50;
      }
    }
    
    .subtask-title {
      font-size: 14px;
      color: #333;
      
      &.completed {
        text-decoration: line-through;
        color: #999;
      }
    }
  }
  
  .add-subtask {
    text-align: center;
    padding: 10px;
    color: #4CAF50;
    font-size: 13px;
  }
}

.task-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  
  .task-tags {
    .task-tag {
      display: inline-block;
      padding: 4px 10px;
      background: #E3F2FD;
      color: #2196F3;
      border-radius: 8px;
      font-size: 11px;
    }
  }
  
  .expand-btn {
    font-size: 12px;
    color: #4CAF50;
  }
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  
  .empty-icon {
    font-size: 60px;
    margin-bottom: 16px;
    display: block;
  }
  
  .empty-text {
    font-size: 14px;
    color: #7F8C8D;
  }
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(45, 90, 61, 0.3);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: flex-end;
  justify-content: center;
  z-index: 1000;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.modal-content {
  width: 100%;
  max-height: 85vh;
  background: #FAFFFA;
  border-radius: 32px 32px 0 0;
  padding: 28px;
  overflow-y: auto;
  animation: slideUp 0.3s ease;
  
  &.detail-modal {
    width: 100%;
    border-radius: 32px 32px 0 0;
  }
}

@keyframes slideUp {
  from { transform: translateY(100%); }
  to { transform: translateY(0); }
}

.modal-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  
  .close-btn {
    width: 36px;
    height: 36px;
    background: #F5FAF5;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    
    .close-icon {
      font-size: 18px;
      color: #8B9A8B;
    }
  }
  
  .modal-title {
    font-size: 20px;
    font-weight: 600;
    color: #2D5A3D;
  }
  
  .save-btn {
    padding: 10px 24px;
    background: linear-gradient(135deg, #81C784, #4CAF50);
    border-radius: 20px;
    
    .save-text {
      color: #fff;
      font-size: 15px;
      font-weight: 500;
    }
  }
}

.modal-body {
  margin-bottom: 20px;
  
  .task-input {
    width: 100%;
    height: 56px;
    background: #fff;
    border-radius: 16px;
    padding: 0 20px;
    font-size: 17px;
    border: 2px solid #E8F5E9;
    color: #3D5A4D;
    
    &:focus {
      border-color: #81C784;
    }
  }
}

.modal-bottom {
  .form-row {
    display: flex;
    align-items: center;
    padding: 18px 20px;
    background: #fff;
    border-radius: 16px;
    margin-bottom: 12px;
    border: 1px solid #F0F5F0;
    
    .row-icon {
      font-size: 22px;
      margin-right: 14px;
    }
    
    .row-label {
      flex: 1;
      font-size: 15px;
      color: #3D5A4D;
      font-weight: 500;
    }
    
    .row-value {
      font-size: 14px;
      color: #8B9A8B;
    }
    
    .member-select {
      display: flex;
      align-items: center;
      
      .select-placeholder {
        font-size: 14px;
        color: #C8E6C9;
      }
      
      .select-value {
        font-size: 14px;
        color: #4CAF50;
        font-weight: 500;
      }
      
      .row-arrow {
        font-size: 20px;
        color: #C8E6C9;
        margin-left: 8px;
      }
    }
  }
}

.task-detail {
  .detail-status {
    display: inline-block;
    padding: 6rpx 20rpx;
    border-radius: 20rpx;
    font-size: 24rpx;
    font-weight: 500;
    margin-bottom: 20rpx;
    
    &.pending {
      background: linear-gradient(135deg, #FFF3E0, #FFE0B2);
      color: #F57C00;
    }
    
    &.completed {
      background: linear-gradient(135deg, #E8F5E9, #C8E6C9);
      color: #388E3C;
    }
  }
  
  .detail-title {
    font-size: 36rpx;
    font-weight: 700;
    color: #333;
    margin-bottom: 30rpx;
    line-height: 1.4;
  }
  
  .detail-cards {
    display: flex;
    flex-direction: column;
    gap: 20rpx;
    margin-bottom: 30rpx;
  }
  
  .info-card {
    display: flex;
    align-items: center;
    gap: 20rpx;
    padding: 24rpx;
    background: #F8FAF8;
    border-radius: 16rpx;
    
    .card-icon {
      font-size: 40rpx;
    }
    
    .card-content {
      display: flex;
      flex-direction: column;
      
      .card-label {
        font-size: 24rpx;
        color: #999;
        margin-bottom: 4rpx;
      }
      
      .card-value {
        font-size: 28rpx;
        color: #333;
        font-weight: 500;
        
        &.priority-text {
          &.priority-0 { color: #666; }
          &.priority-1 { color: #F57C00; }
          &.priority-2 { color: #E53935; }
        }
      }
    }
  }
  
  .detail-actions {
    display: flex;
    gap: 20rpx;
    margin-bottom: 30rpx;
    
    .action-btn {
      flex: 1;
      height: 80rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 40rpx;
      font-size: 28rpx;
      font-weight: 500;
      
      &.complete-btn {
        background: linear-gradient(135deg, #4CAF50, #45a049);
        color: #fff;
        box-shadow: 0 4rpx 12rpx rgba(76, 175, 80, 0.3);
      }
      
      &.delete-btn {
        background: #FFF5F5;
        color: #E53935;
        border: 2rpx solid #FFCDD2;
      }
    }
  }
}

.subtask-section {
  border-top: 1px solid #EEE;
  padding-top: 15px;
  
  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 15px;
    
    .add-btn {
      color: #4CAF50;
      font-size: 14px;
    }
  }
}

.subtask-list-detail {
  .subtask-item-detail {
    display: flex;
    align-items: center;
    padding: 12px 0;
    border-bottom: 1px solid #F0F0F0;
    
    .subtask-checkbox {
      width: 20px;
      height: 20px;
      border: 2px solid #DDD;
      border-radius: 50%;
      margin-right: 12px;
      
      &.checked {
        background: #4CAF50;
        border-color: #4CAF50;
      }
    }
    
    .subtask-title {
      flex: 1;
      font-size: 15px;
      
      &.completed {
        text-decoration: line-through;
        color: #999;
      }
    }
    
    .delete-btn {
      font-size: 16px;
      padding: 5px;
    }
  }
}

.no-subtask {
  text-align: center;
  padding: 30px;
  color: #999;
  font-size: 14px;
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
  
  .picker {
    height: 44px;
    line-height: 44px;
    background: #F5F7FA;
    border-radius: 10px;
    padding: 0 15px;
    font-size: 14px;
    color: #7F8C8D;
  }
}

.priority-options {
  display: flex;
  gap: 10px;
  
  .priority-option {
    flex: 1;
    padding: 10px;
    text-align: center;
    background: #F5F7FA;
    border-radius: 10px;
    font-size: 14px;
    
    &.active {
      background: #4CAF50;
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
    background: #4CAF50;
    color: #fff;
  }
}

/* 成员选择器样式 */
.member-picker-container {
  width: 100%;
  max-height: 60vh;
  background: #FAFFFA;
  border-radius: 32px 32px 0 0;
  padding: 24px;
  animation: slideUp 0.3s ease;
  
  .picker-header {
    text-align: center;
    margin-bottom: 20px;
    
    .picker-title {
      font-size: 18px;
      font-weight: 600;
      color: #2D5A3D;
    }
  }
  
  .member-list {
    max-height: 50vh;
    overflow-y: auto;
    
    .member-item {
      display: flex;
      align-items: center;
      padding: 16px;
      margin-bottom: 8px;
      background: #fff;
      border-radius: 16px;
      border: 2px solid transparent;
      transition: all 0.2s ease;
      
      &.active {
        border-color: #4CAF50;
        background: #F1F8E9;
      }
      
      &:active {
        transform: scale(0.98);
      }
      
      .member-avatar {
        width: 48px;
        height: 48px;
        border-radius: 50%;
        margin-right: 16px;
        background: #E8F5E9;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 14px;
        color: #4CAF50;
        font-weight: 500;
        
        &.default {
          background: linear-gradient(135deg, #81C784, #4CAF50);
          color: #fff;
        }
      }
      
      .member-name {
        flex: 1;
        font-size: 16px;
        color: #3D5A4D;
        font-weight: 500;
      }
    }
  }
}

/* 日期时间选择器样式 */
.picker-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(45, 90, 61, 0.3);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: flex-end;
  justify-content: center;
  z-index: 1001;
}

.picker-container {
  width: 100%;
  background: #FAFFFA;
  border-radius: 32px 32px 0 0;
  padding: 20px;
  animation: slideUp 0.3s ease;
  
  .picker-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    padding: 0 10px;
    
    .picker-cancel {
      font-size: 16px;
      color: #8B9A8B;
    }
    
    .picker-title {
      font-size: 18px;
      font-weight: 600;
      color: #2D5A3D;
    }
    
    .picker-confirm {
      font-size: 16px;
      color: #4CAF50;
      font-weight: 600;
    }
  }
  
  .picker-view {
    height: 480rpx;
    
    .picker-item {
      line-height: 80rpx;
      text-align: center;
      font-size: 32rpx;
      color: #3D5A4D;
    }
  }
}
</style>
