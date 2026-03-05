<template>
  <view class="home-page">
    <PullRefresh2
      ref="pullRefreshRef"
      :enabled="true"
      :threshold="80"
      :haptic-enabled="true"
      @refresh="onRefresh"
    >
      <!-- 骨架屏 -->
      <Skeleton v-if="pageLoading" type="card" :rows="5" show-image :list-count="3" />
    
    <!-- 实际内容 -->
      <!-- 顶部欢迎区 -->
    <view class="header-section">
      <view class="header-bg">
        <view class="bg-pattern"></view>
      </view>
      <view class="header-content">
        <view class="header-left">
          <!-- 用户头像 -->
          <view class="user-avatar" @click="goToProfile">
            <image 
              v-if="userStore.userInfo?.avatar" 
              :src="userStore.userInfo.avatar" 
              class="avatar-img"
              mode="aspectFill"
            />
            <text v-else class="avatar-placeholder">
              {{ (userStore.userInfo?.nickname || userStore.userInfo?.username || '用').substring(0, 1) }}
            </text>
          </view>
          <view class="header-info">
            <view class="greeting-row">
              <text class="time-label">{{ greeting }}</text>
              <text class="user-name">{{ userStore.userInfo?.nickname || '亲爱的用户' }}</text>
            </view>
            <view class="date-row">
              <text class="date-icon">📅</text>
              <text class="current-date">{{ currentDate }}</text>
            </view>
          </view>
        </view>
      </view>
    </view>
    
    <!-- 快捷功能入口 -->
    <view class="quick-actions">
      <view 
        v-for="(item, index) in quickActions" 
        :key="index"
        class="action-item"
        :class="`item-${index}`"
        @click="navigateTo(item.path)"
        :style="{ animationDelay: `${index * 0.05}s` }"
      >
        <view class="icon-box" :style="{ background: item.bgColor, boxShadow: item.shadow }">
          <text class="icon-emoji">{{ item.icon }}</text>
        </view>
        <text class="action-name">{{ item.name }}</text>
      </view>
    </view>
    
    <!-- 今日概览卡片 -->
    <view class="overview-cards animate-in">
      <!-- 今日喝水 -->
      <view class="overview-card water-card" @click="navigateTo('/pages/water/index')">
        <view class="card-header">
          <view class="card-icon-wrapper water-icon">
            <text class="card-icon">💧</text>
          </view>
          <text class="card-title">今日喝水</text>
        </view>
        <view class="water-progress-section">
          <view class="water-progress-bar">
            <view 
              class="water-progress-fill"
              :style="{ width: (overviewData.water / overviewData.waterTarget * 100) + '%' }"
            ></view>
          </view>
          <text class="water-text">{{ overviewData.water }}ml / {{ overviewData.waterTarget }}ml</text>
        </view>
      </view>
      
      <!-- 今日饮食 -->
      <view class="overview-card diet-card" @click="navigateTo('/pages/food/record')">
        <view class="card-header">
          <view class="card-icon-wrapper diet-icon">
            <text class="card-icon">🍽️</text>
          </view>
          <text class="card-title">今日饮食</text>
        </view>
        <view class="card-value">
          <text class="value-num">{{ overviewData.calories }}</text>
          <text class="value-unit">千卡</text>
        </view>
      </view>
    </view>
    
    <!-- 今日概览 -->
    <view class="section-card animate-in">
      <view class="section-header">
        <view class="title-wrapper">
          <text class="section-icon">📋</text>
          <text class="section-title">今日待办</text>
        </view>
        <view class="header-actions">
          <view class="add-btn" @click="goAddTask">
            <text>+</text>
          </view>
          <view class="more-btn" @click="goTaskList">
            <text>更多</text>
            ›
          </view>
        </view>
      </view>
      
      <view v-if="todayTasks.length > 0" class="task-list">
        <view 
          v-for="(task, index) in todayTasks" 
          :key="task.id"
          class="task-item"
          :class="{ completed: task.status === 2 }"
          :style="{ animationDelay: `${index * 0.08}s` }"
          @click="goTaskDetail(task)"
        >
          <view class="task-checkbox" @click.stop="toggleTask(task)">
            <view v-if="task.status === 2" class="checked">
              ✓
            </view>
            <view v-else class="unchecked"></view>
          </view>
          
          <view class="task-info">
            <text class="task-title" :class="{ completed: task.status === 2 }">
              {{ task.title }}
            </text>
            <view class="task-meta">
              <text class="assignee">{{ task.assigneeName }}</text>
              <text class="divider">·</text>
              <text class="time">{{ task.time }}</text>
            </view>
          </view>
          
          <view 
            class="task-priority"
            :class="{ high: task.priority === 2, medium: task.priority === 1, low: task.priority === 0 }"
          >
            <text v-if="task.priority === 2">紧急</text>
            <text v-else-if="task.priority === 1">重要</text>
            <text v-else>普通</text>
          </view>
        </view>
      </view>
      
      <view v-else class="empty-state">
        <view class="empty-icon-wrapper">
          <text class="empty-icon">✨</text>
        </view>
        <text class="empty-text">今天没有待办事项</text>
        <text class="empty-subtext">享受生活吧！</text>
      </view>
    </view>
    
    <!-- 纪念日提醒 -->
    <view class="section-card anniversary-card animate-in" v-if="upcomingAnniversaries.length > 0">
      <view class="section-header">
        <view class="title-wrapper">
          <text class="section-icon">💝</text>
          <text class="section-title">近期纪念</text>
        </view>
        <view class="more-btn" @click="navigateTo('/pages/calendar/index')">
          <text>更多</text>
          ›
        </view>
      </view>
      
      <view class="anniversary-list">
        <view 
          v-for="(item, index) in upcomingAnniversaries" 
          :key="item.id"
          class="anni-item"
          :style="{ animationDelay: `${index * 0.1}s` }"
        >
          <view class="anni-icon-wrapper" :class="{ urgent: item.days <= 3 }">
            <text class="anni-icon">{{ item.icon }}</text>
          </view>
          
          <view class="anni-info">
            <text class="anni-title">{{ item.title }}</text>
            <view class="anni-date-wrapper">
              <text class="anni-date">{{ item.date }}</text>
            </view>
          </view>
          
          <view class="anni-days" :class="{ today: item.days === 0, soon: item.days <= 3 && item.days > 0 }">
            <text class="days-num">{{ item.days === 0 ? '今天' : item.days }}</text>
            <text class="days-label">{{ item.days > 0 ? '天后' : '' }}</text>
          </view>
        </view>
      </view>
    </view>
    
    <!-- 今日菜谱推荐 -->
    <view class="section-card recipe-section animate-in">
      <view class="section-header">
        <view class="title-wrapper">
          <text class="section-icon">🍳</text>
          <text class="section-title">今日菜谱</text>
        </view>
        <view class="more-btn" @click="navigateTo('/pages/recipe/index')">
          <text>更多</text>
          ›
        </view>
      </view>
      
      <view class="recipe-scroll">
        <scroll-view scroll-x class="recipe-list" enhanced :show-scrollbar="false">
          <view 
            v-for="(recipe, index) in todayRecipes" 
            :key="recipe.id"
            class="recipe-card"
            :style="{ animationDelay: `${index * 0.1}s` }"
            @click="goRecipeDetail(recipe)"
          >
            <view class="recipe-image-wrapper">
              <LazyImage 
                :src="recipe.cover" 
                mode="aspectFill"
                width="280rpx"
                height="180rpx"
                :threshold="50"
              />
              <view class="recipe-overlay">
                <text class="cook-time">⏱️ {{ recipe.time }}分</text>
              </view>
            </view>
            
            <view class="recipe-info">
              <text class="recipe-name">{{ recipe.name }}</text>
              <view class="recipe-meta">
                <view class="meta-item">
                  <text class="meta-icon">🔥</text>
                  <text class="meta-text">{{ recipe.calories }}卡</text>
                </view>
              </view>
            </view>
          </view>
        </scroll-view>
      </view>
    </view>
    
    </PullRefresh2>
    
    <!-- 设置饮水目标弹窗 -->
    <WaterGoalModal
      :visible="waterGoalModalVisible"
      :current-target="overviewData.waterTarget"
      @close="waterGoalModalVisible = false"
      @confirm="handleSetWaterGoal"
    />
    
    <!-- 添加任务弹窗 - 与待办页面一致 -->
    <view v-if="showAddTaskModal" class="modal-overlay" @click="closeAddTaskModal">
      <view class="modal-content task-modal" @click.stop>
        <!-- 顶部：添加待办 -->
        <view class="modal-top">
          <view class="close-btn" @click="closeAddTaskModal">
            <text class="close-icon">✕</text>
          </view>
          <text class="modal-title">添加待办</text>
          <view class="save-btn" @click="saveTask">
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
              <view v-for="minute in 60" :key="minute-1" class="picker-item">{{ String(minute-1).padStart(2, '0') }}分</view>
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
                :src="member.avatar || '../../static/avatar-default.png'" 
                mode="aspectFill" 
              />
              <text class="member-name">{{ member.nickname || member.name || '家人' }}</text>
            </view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useUserStore } from '../../stores/user'
import { waterApi } from '../../api/water'
import { taskApi } from '../../api/task'
import { familyApi } from '../../api/family'
import { anniversaryApi } from '../../api/anniversary'
import LazyImage from '@/components/common/LazyImage.vue'
import Skeleton from '@/components/common/Skeleton.vue'
import PullRefresh2 from '@/components/common/PullRefresh2.vue'
import WaterGoalModal from '@/components/water/WaterGoalModal.vue'
import { useSkeleton } from '@/utils/performance.js'

const userStore = useUserStore()
const pullRefreshRef = ref(null)
const waterGoalModalVisible = ref(false)
const { loading: pageLoading, hide: hideSkeleton } = useSkeleton({ minDuration: 500 })

// ========== 添加任务弹窗相关 ==========
const showAddTaskModal = ref(false)
const showTimePicker = ref(false)
const showMemberPicker = ref(false)
const newTask = ref({
  title: '',
  dueDate: '',
  dueTime: '',
  assigneeId: null
})

// 年份范围
const yearRange = ref([2024, 2025, 2026, 2027, 2028])
// 当前选中的日期时间
const pickerValue = ref([1, new Date().getMonth(), new Date().getDate() - 1, 15, 0])

// 计算当月天数
const daysInMonth = computed(() => {
  const year = yearRange.value[pickerValue.value[0]]
  const month = pickerValue.value[1] + 1
  return new Date(year, month, 0).getDate()
})

// 打开添加任务弹窗
const goAddTask = () => {
  // 重置表单
  newTask.value = {
    title: '',
    dueDate: '',
    dueTime: '',
    assigneeId: null
  }
  showAddTaskModal.value = true
}

// 关闭添加任务弹窗
const closeAddTaskModal = () => {
  showAddTaskModal.value = false
}

// 保存任务
const saveTask = async () => {
  if (!newTask.value.title.trim()) {
    uni.showToast({ title: '请输入待办事项', icon: 'none' })
    return
  }
  
  try {
    const familyId = uni.getStorageSync('currentFamilyId') || 1
    const dueTime = newTask.value.dueDate && newTask.value.dueTime 
      ? `${newTask.value.dueDate} ${newTask.value.dueTime}`
      : null
    
    await taskApi.create({
      title: newTask.value.title,
      familyId,
      assigneeId: newTask.value.assigneeId,
      dueTime,
      status: 0,
      priority: 0
    })
    
    uni.showToast({ title: '添加成功', icon: 'success' })
    showAddTaskModal.value = false
    // 刷新任务列表
    loadTodayTasks()
  } catch (error) {
    console.error('添加任务失败:', error)
    uni.showToast({ title: '添加失败', icon: 'none' })
  }
}

// 获取成员名称
const getMemberName = (userId) => {
  const member = familyMembers.value.find(m => m.userId === userId)
  return member ? (member.nickname || member.name || '家人') : '自己'
}

// 格式化任务时间
const formatTaskTime = (dueTime) => {
  if (!dueTime) return '今天'
  
  // 如果是数组格式 [year, month, day, hour, minute]，转换为时间字符串
  let timeStr = dueTime
  if (Array.isArray(dueTime)) {
    const [year, month, day, hour, minute] = dueTime
    timeStr = `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')} ${String(hour).padStart(2, '0')}:${String(minute || 0).padStart(2, '0')}`
  }
  
  // 如果包含空格，提取时间部分
  const timePart = timeStr.includes(' ') ? timeStr.split(' ')[1] : timeStr
  // 只显示小时和分钟
  return '今天 ' + timePart.substring(0, 5)
}

// 加载今日待办数据
const loadTodayTasks = async () => {
  try {
    const familyId = uni.getStorageSync('currentFamilyId') || 1
    
    // 先加载家庭成员（用于显示指派人名称）
    try {
      const membersRes = await familyApi.getMembers(familyId)
      familyMembers.value = membersRes || []
    } catch (e) {
      console.error('加载家庭成员失败', e)
      familyMembers.value = []
    }
    
    // 使用与待办列表页相同的API，获取所有待办任务（status=0）
    const res = await taskApi.getList(familyId, 0)
    const allTodoTasks = res.list || []
    
    // 获取今天的日期字符串
    const today = new Date()
    const todayStr = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, '0')}-${String(today.getDate()).padStart(2, '0')}`
    const todayStrShort = `${today.getFullYear()}-${today.getMonth() + 1}-${today.getDate()}`
    
    // 筛选今日待办任务（dueDate 是今天）
    const todayTodoTasks = allTodoTasks.filter(task => {
      // 如果任务没有截止日期，默认也算今天
      if (!task.dueDate) return true
      
      // 处理日期格式：可能是字符串 "2026-03-03" 或数组 [2026,3,3]
      let taskDateStr = ''
      if (Array.isArray(task.dueDate)) {
        const [y, m, d] = task.dueDate
        taskDateStr = `${y}-${String(m).padStart(2, '0')}-${String(d).padStart(2, '0')}`
      } else if (typeof task.dueDate === 'string') {
        taskDateStr = task.dueDate
      }
      
      return taskDateStr === todayStr || taskDateStr === todayStrShort
    })
    
    // 格式化并显示所有今日待办
    if (todayTodoTasks.length > 0) {
      todayTasks.value = todayTodoTasks.map(task => ({
        ...task,
        assigneeName: getMemberName(task.assigneeId) || '家人',
        time: formatTaskTime(task.dueTime)
      }))
    } else {
      todayTasks.value = []
    }
  } catch (e) {
    console.error('加载待办数据失败', e)
    todayTasks.value = []
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
  
  newTask.value.dueDate = `${year}-${month}-${day}`
  newTask.value.dueTime = `${hour}:${minute}`
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
  newTask.value.assigneeId = userId
  showMemberPicker.value = false
}
// ========== 添加任务弹窗相关结束 ==========

// 问候语
const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '凌晨好'
  if (hour < 9) return '早上好'
  if (hour < 12) return '上午好'
  if (hour < 14) return '中午好'
  if (hour < 18) return '下午好'
  return '晚上好'
})

// 当前日期
const currentDate = computed(() => {
  const now = new Date()
  const month = now.getMonth() + 1
  const day = now.getDate()
  const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  const weekDay = weekDays[now.getDay()]
  return `${month}月${day}日 ${weekDay}`
})

const currentFamily = ref({ name: '幸福小家' })

// 快捷功能 - 添加阴影
const quickActions = [
  { name: '纪念日', icon: '❤️', bgColor: '#FF6B6B', shadow: '0 8rpx 20rpx rgba(255, 107, 107, 0.35)', path: '/pages/anniversary/index' },
  { name: '记账', icon: '💰', bgColor: '#68d391', shadow: '0 8rpx 20rpx rgba(104, 211, 145, 0.35)', path: '/pages/food/record' },
  { name: 'AI', icon: '🤖', bgColor: '#9B59B6', shadow: '0 8rpx 20rpx rgba(155, 89, 182, 0.35)', path: '/pages/ai/chat' },
  { name: '天气', icon: '🌤️', bgColor: '#4facfe', shadow: '0 8rpx 20rpx rgba(79, 172, 254, 0.35)', path: '/pages/weather/index' }
]

// 今日任务
const todayTasks = ref([])

// 家庭成员列表（用于显示指派人名称）
const familyMembers = ref([])

// 纪念日
const upcomingAnniversaries = ref([])

// 今日菜谱
const todayRecipes = ref([
  { id: 1, name: '番茄炒蛋', cover: '../../static/recipes/fanqie.jpg', time: 15, calories: 180 },
  { id: 2, name: '红烧排骨', cover: '../../static/recipes/paigu.jpg', time: 45, calories: 320 },
  { id: 3, name: '蒜蓉西兰花', cover: '../../static/recipes/xilanhua.jpg', time: 10, calories: 85 }
])

// 健康数据
const healthData = ref({
  calories: 1250,
  target: 2000,
  water: 5
})

// 今日概览数据
const overviewData = ref({
  water: 500,
  waterTarget: 2000,
  calories: 1200,
  expense: 128
})

const navigateTo = (path) => {
  // tabBar 页面使用 switchTab
  if (path && path.startsWith('/pages/task')) {
    uni.switchTab({ url: '/pages/task/index' })
  } else {
    uni.navigateTo({ url: path })
  }
}

const toggleTask = async (task) => {
  const isCompleting = task.status === 0
  const originalStatus = task.status
  
  // 乐观更新 UI
  task.status = isCompleting ? 2 : 0
  
  try {
    // 调用后端 API 完成/取消完成任务
    await taskApi.complete(task.id)
  } catch (error) {
    // API 调用失败，回滚状态
    task.status = originalStatus
    console.error('切换任务状态失败:', error)
    uni.showToast({
      title: isCompleting ? '完成任务失败' : '取消任务失败',
      icon: 'none'
    })
  }
}

const goTaskDetail = (task) => {
  // 先存储任务ID到本地，跳转到待办页面后再读取显示详情
  uni.setStorageSync('pendingTaskDetailId', task.id)
  uni.switchTab({ url: '/pages/task/index' })
}

const goTaskList = () => {
  // 跳转到待办列表页面
  uni.switchTab({ url: '/pages/task/index' })
}

const goToProfile = () => {
  uni.navigateTo({ url: '/pages/profile/index' })
}

const handleSetWaterGoal = (goal) => {
  waterGoalModalVisible.value = false
  overviewData.value.waterTarget = goal
}

const goRecipeDetail = (recipe) => {
  uni.navigateTo({ url: `/pages/recipe/detail?id=${recipe.id}` })
}

// 下拉刷新
const onRefresh = async ({ finish, success, error }) => {
  try {
    await refreshHomeData()
    success()
    uni.showToast({ title: '刷新成功', icon: 'success' })
  } catch (e) {
    console.error('刷新失败:', e)
    error()
    uni.showToast({ title: '刷新失败', icon: 'none' })
  }
}

// 防止重复加载的标志
let isUserInfoLoaded = false

// 刷新首页数据
const refreshHomeData = async () => {
  // 加载用户信息
  try {
    await userStore.getUserInfo()
  } catch (e) {
    console.log('获取用户信息失败', e)
  }
  
  // 重新加载家庭成员
  const familyId = uni.getStorageSync('currentFamilyId') || 1
  try {
    const membersRes = await familyApi.getMembers(familyId)
    familyMembers.value = membersRes || []
  } catch (e) {
    console.error('加载家庭成员失败', e)
  }
  
  // 重新加载喝水数据
  try {
    const userId = userStore.userInfo?.id || 1
    const waterData = await waterApi.getToday(userId)
    if (waterData) {
      overviewData.value.water = waterData.todayAmount || 0
    }
  } catch (e) {
    console.error('加载喝水数据失败', e)
  }
  
  // 加载今日待办
  await loadTodayTasks()
}

// 每次页面显示时都刷新数据
onShow(async () => {
  // H5 兼容：强制从 storage 同步 token
  const storedToken = uni.getStorageSync('token')
  // 简化日志
  // console.log('[Home onShow] storage token:', storedToken ? '存在' : '空')
  // console.log('[Home onShow] store token:', userStore.token ? '存在' : '空')
  
  if (storedToken && storedToken !== userStore.token) {
    userStore.setToken(storedToken)
  }
  
  // 每次显示页面都刷新数据
  await refreshHomeData()
})

// 获取纪念日图标
const getAnniversaryIcon = (type) => {
  const iconMap = {
    'birthday': '🎂',
    'wedding': '💒',
    'love': '❤️',
    'baby': '👶',
    'work': '💼',
    'holiday': '🎉',
    'memory': '📸',
    'other': '🎁'
  }
  return iconMap[type] || '🎁'
}
</script>

<style lang="scss" scoped>
.home-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #f8f9fc 0%, #f0f4f8 100%);
  padding-bottom: 140rpx;
}

// 顶部欢迎区
.header-section {
  position: relative;
  padding: 40rpx 32rpx 60rpx;
  padding-top: 100rpx;
  overflow: hidden;
  
  .header-bg {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 420rpx;
    background: linear-gradient(135deg, #6B8DD6 0%, #8B5CF6 50%, #A78BFA 100%);
    border-radius: 0 0 60rpx 60rpx;
    box-shadow: 0 20rpx 60rpx rgba(107, 141, 214, 0.3);
    
    .bg-pattern {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      opacity: 0.1;
      background-image: radial-gradient(circle at 20% 50%, rgba(255,255,255,0.8) 0%, transparent 50%),
                        radial-gradient(circle at 80% 20%, rgba(255,255,255,0.6) 0%, transparent 40%);
    }
  }
  
  .header-content {
    position: relative;
    z-index: 1;
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
  }
  
  .header-left {
    display: flex;
    align-items: center;
    
    .user-avatar {
      width: 96rpx;
      height: 96rpx;
      border-radius: 50%;
      overflow: hidden;
      margin-right: 24rpx;
      border: 4rpx solid rgba(255,255,255,0.3);
      box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.15);
      
      .avatar-img {
        width: 100%;
        height: 100%;
      }
      
      .avatar-placeholder {
        width: 100%;
        height: 100%;
        display: flex;
        align-items: center;
        justify-content: center;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        color: #fff;
        font-size: 36rpx;
        font-weight: 600;
      }
    }
    
    .header-info {
      .greeting-row {
        display: flex;
        align-items: center;
        flex-wrap: wrap;
        gap: 8rpx;
        margin-bottom: 8rpx;
        
        .time-label {
          font-size: 26rpx;
          color: rgba(255,255,255,0.85);
          font-weight: 500;
        }
        
        .user-name {
          font-size: 40rpx;
          font-weight: 700;
          color: #fff;
          text-shadow: 0 2rpx 8rpx rgba(0,0,0,0.15);
        }
      }
      
      .date-row {
        display: flex;
        align-items: center;
        gap: 6rpx;
        
        .date-icon {
          font-size: 22rpx;
        }
        
        .current-date {
          font-size: 24rpx;
          color: rgba(255,255,255,0.8);
          background: rgba(255,255,255,0.15);
          padding: 6rpx 16rpx;
          border-radius: 20rpx;
        }
      }
    }
  }
  
  .header-right {
    .family-selector {
    display: inline-flex;
    align-items: center;
    background: rgba(255,255,255,0.2);
    backdrop-filter: blur(10rpx);
    padding: 16rpx 28rpx;
    border-radius: 40rpx;
    border: 1rpx solid rgba(255,255,255,0.15);
    transition: all 0.25s ease;
    
    &:active {
      transform: scale(0.96);
      background: rgba(255,255,255,0.25);
    }
    
    .family-name {
      font-size: 26rpx;
      color: #fff;
      margin-right: 12rpx;
      font-weight: 500;
    }
    
    .selector-arrow {
      opacity: 0.8;
      
      .arrow-icon {
        color: #fff;
        font-size: 20rpx;
      }
    }
  }
  }
}

// 快捷功能入口
.quick-actions {
  display: flex;
  justify-content: space-between;
  flex-wrap: wrap;
  padding: 36rpx 48rpx;
  margin: 0 32rpx;
  background: #fff;
  border-radius: 28rpx;
  margin-top: -50rpx;
  position: relative;
  box-shadow: 0 12rpx 40rpx rgba(107, 141, 214, 0.12), 0 4rpx 12rpx rgba(0,0,0,0.04);
  z-index: 2;
  
  .action-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    width: calc(50% - 24rpx);
    margin-bottom: 28rpx;
    animation: fadeInUp 0.5s ease-out forwards;
    opacity: 0;
    
    &:nth-child(n+3) {
      margin-bottom: 0;
    }
    
    &:active {
      transform: scale(0.92);
    }
    
    .icon-box {
      width: 96rpx;
      height: 96rpx;
      border-radius: 28rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-bottom: 16rpx;
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      
      &:active {
        transform: scale(0.9);
      }
    }
    
    .action-name {
      font-size: 24rpx;
      color: #5a6c7d;
      font-weight: 500;
    }
  }
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

// 今日概览卡片
.overview-cards {
  display: flex;
  justify-content: space-between;
  padding: 0 32rpx;
  margin-top: 28rpx;
  
  .overview-card {
    flex: 1;
    background: #fff;
    border-radius: 24rpx;
    padding: 24rpx;
    margin-right: 20rpx;
    box-shadow: 0 8rpx 24rpx rgba(107, 141, 214, 0.1), 0 2rpx 8rpx rgba(0,0,0,0.04);
    transition: all 0.3s ease;
    animation: fadeInUp 0.5s ease-out forwards;
    opacity: 0;
    
    &:last-child {
      margin-right: 0;
    }
    
    &:active {
      transform: scale(0.96);
    }
    
    .card-header {
      display: flex;
      align-items: center;
      margin-bottom: 20rpx;
      
      .card-icon-wrapper {
        width: 56rpx;
        height: 56rpx;
        border-radius: 16rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-right: 12rpx;
        
        .card-icon {
          font-size: 28rpx;
        }
        
        &.water-icon {
          background: linear-gradient(135deg, #4ECDC4, #44A08D);
          box-shadow: 0 6rpx 16rpx rgba(78, 205, 196, 0.35);
        }
        
        &.diet-icon {
          background: linear-gradient(135deg, #FF6B6B, #FF8E8E);
          box-shadow: 0 6rpx 16rpx rgba(255, 107, 107, 0.35);
        }
        
        &.expense-icon {
          background: linear-gradient(135deg, #68d391, #48bb78);
          box-shadow: 0 6rpx 16rpx rgba(104, 211, 145, 0.35);
        }
      }
      
      .card-title {
        font-size: 26rpx;
        color: #5a6c7d;
        font-weight: 500;
      }
    }
    
    .water-progress-section {
      .water-progress-bar {
        height: 12rpx;
        background: #e8f5f3;
        border-radius: 6rpx;
        overflow: hidden;
        margin-bottom: 12rpx;
        
        .water-progress-fill {
          height: 100%;
          background: linear-gradient(90deg, #4ECDC4, #44A08D);
          border-radius: 6rpx;
          transition: width 0.5s ease;
        }
      }
      
      .water-text {
        font-size: 22rpx;
        color: #8b9aad;
      }
    }
    
    .card-value {
      .value-num {
        font-size: 40rpx;
        font-weight: 700;
        color: #2d3748;
      }
      
      .value-unit {
        font-size: 24rpx;
        color: #8b9aad;
        margin-left: 6rpx;
      }
    }
  }
}

// 卡片基础样式
.section-card {
  background: #fff;
  border-radius: 28rpx;
  margin: 28rpx 32rpx;
  padding: 32rpx;
  box-shadow: 0 8rpx 32rpx rgba(107, 141, 214, 0.08), 0 2rpx 8rpx rgba(0,0,0,0.03);
  transition: all 0.3s ease;
  
  &.animate-in {
    animation: slideIn 0.5s ease-out forwards;
    opacity: 0;
    transform: translateY(20rpx);
  }
  
  &:active {
    transform: scale(0.995);
  }
  
  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 28rpx;
    
    .title-wrapper {
      display: flex;
      align-items: center;
      
      .section-icon {
        font-size: 36rpx;
        margin-right: 12rpx;
      }
      
      .section-title {
        font-size: 32rpx;
        font-weight: 600;
        color: #2d3748;
        letter-spacing: -0.3rpx;
      }
    }
    
    .more-btn {
      display: flex;
      align-items: center;
      padding: 10rpx 16rpx;
      background: rgba(107, 141, 214, 0.08);
      border-radius: 24rpx;
      transition: all 0.2s ease;
      
      &:active {
        background: rgba(107, 141, 214, 0.15);
        transform: scale(0.95);
      }
      
      text {
        font-size: 24rpx;
        color: #6B8DD6;
        font-weight: 500;
        margin-right: 6rpx;
      }
    }
    
    .header-actions {
      display: flex;
      align-items: center;
      gap: 12rpx;
    }
    
    .add-btn {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 56rpx;
      height: 56rpx;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      border-radius: 50%;
      box-shadow: 0 4rpx 12rpx rgba(102, 126, 234, 0.4);
      transition: all 0.2s ease;
      
      &:active {
        transform: scale(0.9);
        box-shadow: 0 2rpx 8rpx rgba(102, 126, 234, 0.3);
      }
      
      text {
        font-size: 36rpx;
        color: #fff;
        font-weight: 400;
        line-height: 1;
      }
    }
  }
}

@keyframes slideIn {
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

// 任务列表
.task-list {
  .task-item {
    display: flex;
    align-items: flex-start;
    padding: 24rpx 0;
    border-bottom: 2rpx solid #f1f5f9;
    transition: all 0.2s ease;
    animation: fadeInUp 0.4s ease-out forwards;
    opacity: 0;
    
    &:last-child {
      border-bottom: none;
    }
    
    &:active {
      background: #f8f9fc;
      border-radius: 16rpx;
      margin: 0 -16rpx;
      padding-left: 16rpx;
      padding-right: 16rpx;
    }
    
    &.completed {
      .task-title {
        color: #8b9aad;
      }
    }
    
    .task-checkbox {
      margin-right: 24rpx;
      flex-shrink: 0;
      margin-top: 4rpx;
      
      .unchecked {
        width: 44rpx;
        height: 44rpx;
        border: 3rpx solid #e2e8f0;
        border-radius: 50%;
        transition: all 0.2s ease;
      }
      
      .checked {
        width: 44rpx;
        height: 44rpx;
        background: linear-gradient(135deg, #68d391, #48bb78);
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        box-shadow: 0 4rpx 12rpx rgba(104, 211, 145, 0.35);
      }
    }
    
    .task-info {
      flex: 1;
      min-width: 0;
      overflow: visible;
      
      .task-title {
        font-size: 30rpx;
        color: #2d3748;
        display: block;
        margin-bottom: 10rpx;
        font-weight: 500;
        transition: all 0.2s ease;
        overflow-wrap: break-word;
        word-break: break-all;
        white-space: normal;
        
        &.completed {
          text-decoration: line-through;
          color: #8b9aad;
        }
      }
      
      .task-meta {
        font-size: 24rpx;
        color: #8b9aad;
        overflow-wrap: break-word;
        word-break: break-all;
        white-space: normal;
        line-height: 1.6;
        display: flex;
        flex-wrap: wrap;
        gap: 4rpx 8rpx;
        
        text {
          display: inline;
        }
        
        .assignee {
          color: #6B8DD6;
          font-weight: 500;
        }
        
        .divider {
          opacity: 0.5;
        }
        
        .time {
          display: inline;
        }
      }
    }
    
    .task-priority {
      padding: 8rpx 18rpx;
      border-radius: 24rpx;
      font-size: 22rpx;
      font-weight: 500;
      flex-shrink: 0;
      margin-left: 16rpx;
      margin-top: 4rpx;
      
      &.high {
        background: rgba(252, 129, 129, 0.12);
        color: #fc8181;
      }
      
      &.medium {
        background: rgba(246, 173, 85, 0.12);
        color: #f6ad55;
      }
      
      &.low {
        background: rgba(104, 211, 145, 0.12);
        color: #68d391;
      }
    }
  }
}

// 空状态
.empty-state {
  text-align: center;
  padding: 60rpx 0;
  
  .empty-icon-wrapper {
    width: 120rpx;
    height: 120rpx;
    background: linear-gradient(135deg, #f0fff4, #e6fffa);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 auto 24rpx;
    
    .empty-icon {
      font-size: 60rpx;
    }
  }
  
  .empty-text {
    display: block;
    font-size: 30rpx;
    color: #5a6c7d;
    font-weight: 500;
    margin-bottom: 8rpx;
  }
  
  .empty-subtext {
    display: block;
    font-size: 26rpx;
    color: #8b9aad;
  }
}

// 纪念日卡片
.anniversary-card {
  background: linear-gradient(135deg, #fff 0%, #fff5f5 100%);
}

.anniversary-list {
  .anni-item {
    display: flex;
    align-items: center;
    padding: 24rpx 0;
    border-bottom: 2rpx solid rgba(226, 232, 240, 0.5);
    animation: fadeInUp 0.4s ease-out forwards;
    opacity: 0;
    
    &:last-child {
      border-bottom: none;
    }
    
    .anni-icon-wrapper {
      width: 88rpx;
      height: 88rpx;
      background: linear-gradient(135deg, #fff0f6, #ffe4e6);
      border-radius: 24rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 44rpx;
      margin-right: 24rpx;
      transition: all 0.3s ease;
      
      &.urgent {
        background: linear-gradient(135deg, #fc8181, #f56565);
        box-shadow: 0 8rpx 24rpx rgba(252, 129, 129, 0.35);
      }
    }
    
    .anni-info {
      flex: 1;
      
      .anni-title {
        font-size: 30rpx;
        color: #2d3748;
        display: block;
        margin-bottom: 10rpx;
        font-weight: 500;
      }
      
      .anni-date-wrapper {
        .anni-date {
          font-size: 24rpx;
          color: #8b9aad;
        }
      }
    }
    
    .anni-days {
      text-align: center;
      padding: 16rpx 24rpx;
      background: rgba(107, 141, 214, 0.08);
      border-radius: 20rpx;
      min-width: 100rpx;
      
      &.today {
        background: linear-gradient(135deg, #68d391, #48bb78);
        box-shadow: 0 8rpx 24rpx rgba(104, 211, 145, 0.35);
        
        .days-num, .days-label {
          color: #fff;
        }
      }
      
      &.soon {
        background: linear-gradient(135deg, #fc8181, #f56565);
        box-shadow: 0 8rpx 24rpx rgba(252, 129, 129, 0.35);
        
        .days-num, .days-label {
          color: #fff;
        }
      }
      
      .days-num {
        font-size: 36rpx;
        font-weight: 700;
        color: #6B8DD6;
        display: block;
        line-height: 1.2;
      }
      
      .days-label {
        font-size: 22rpx;
        color: #8b9aad;
      }
    }
  }
}

// 菜谱区域
.recipe-section {
  .recipe-scroll {
    margin: 0 -32rpx;
    padding: 0 32rpx;
  }
  
  .recipe-list {
    white-space: nowrap;
    
    .recipe-card {
      display: inline-block;
      width: 300rpx;
      margin-right: 24rpx;
      border-radius: 24rpx;
      overflow: hidden;
      background: #fff;
      box-shadow: 0 8rpx 24rpx rgba(0,0,0,0.08);
      transition: all 0.3s ease;
      animation: fadeInUp 0.5s ease-out forwards;
      opacity: 0;
      
      &:active {
        transform: scale(0.96);
      }
      
      &:last-child {
        margin-right: 0;
      }
      
      .recipe-image-wrapper {
        position: relative;
        
        .recipe-overlay {
          position: absolute;
          bottom: 0;
          left: 0;
          right: 0;
          padding: 20rpx;
          background: linear-gradient(transparent, rgba(0,0,0,0.6));
          
          .cook-time {
            font-size: 24rpx;
            color: #fff;
            font-weight: 500;
          }
        }
      }
      
      .recipe-info {
        padding: 20rpx;
        
        .recipe-name {
          font-size: 30rpx;
          color: #2d3748;
          display: block;
          margin-bottom: 16rpx;
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
          font-weight: 500;
        }
        
        .recipe-meta {
          .meta-item {
            display: flex;
            align-items: center;
            
            .meta-icon {
              font-size: 24rpx;
              margin-right: 8rpx;
            }
            
            .meta-text {
              font-size: 24rpx;
              color: #8b9aad;
            }
          }
        }
      }
    }
  }
}

// 健康卡片
.health-card {
  background: linear-gradient(135deg, #fff 0%, #f0fff4 100%);
}

.health-stats {
  display: flex;
  justify-content: space-around;
  padding: 28rpx 0;
  
  .stat-item {
    text-align: center;
    flex: 1;
    
    .stat-icon-wrapper {
      width: 80rpx;
      height: 80rpx;
      border-radius: 24rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      margin: 0 auto 16rpx;
      box-shadow: 0 8rpx 20rpx rgba(0,0,0,0.15);
      
      .stat-icon {
        font-size: 36rpx;
      }
    }
    
    .stat-value {
      font-size: 40rpx;
      font-weight: 700;
      color: #2d3748;
      display: block;
      line-height: 1.3;
    }
    
    .stat-label {
      font-size: 24rpx;
      color: #8b9aad;
      margin-top: 8rpx;
      display: block;
    }
  }
}

.calorie-progress {
  margin-top: 28rpx;
  padding-top: 28rpx;
  border-top: 2rpx solid #f1f5f9;
  
  .progress-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16rpx;
    
    .progress-title {
      font-size: 26rpx;
      color: #5a6c7d;
      font-weight: 500;
    }
    
    .progress-percent {
      font-size: 32rpx;
      font-weight: 700;
      color: #6B8DD6;
    }
  }
  
  .progress-bar {
    height: 16rpx;
    background: #e2e8f0;
    border-radius: 8rpx;
    overflow: hidden;
    
    .progress-fill {
      height: 100%;
      background: linear-gradient(90deg, #6B8DD6, #68d391);
      border-radius: 8rpx;
      transition: width 0.6s cubic-bezier(0.4, 0, 0.2, 1);
      position: relative;
      overflow: hidden;
      
      .progress-shine {
        position: absolute;
        top: 0;
        left: -100%;
        width: 100%;
        height: 100%;
        background: linear-gradient(90deg, transparent, rgba(255,255,255,0.4), transparent);
        animation: shine 2s infinite;
      }
    }
  }
  
  .progress-text {
    display: block;
    text-align: center;
    font-size: 26rpx;
    color: #8b9aad;
    margin-top: 16rpx;
  }
}

@keyframes shine {
  0% {
    left: -100%;
  }
  100% {
    left: 100%;
  }
}

// ========== 添加任务弹窗样式 ==========
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: flex-end;
  justify-content: center;
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
  width: 100%;
  max-height: 80vh;
  animation: slideUp 0.3s ease;
}

@keyframes slideUp {
  from { transform: translateY(100%); }
  to { transform: translateY(0); }
}

.task-modal {
  padding: 32rpx;
  
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
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: flex-end;
  z-index: 1001;
}

.picker-container {
  background: #fff;
  width: 100%;
  border-radius: 32rpx 32rpx 0 0;
  padding: 32rpx;
  
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
  width: 100%;
  border-radius: 32rpx 32rpx 0 0;
  padding: 32rpx;
  max-height: 60vh;
  
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
    .member-item {
      display: flex;
      align-items: center;
      padding: 24rpx;
      border-radius: 16rpx;
      margin-bottom: 16rpx;
      transition: all 0.2s ease;
      
      &.active {
        background: #f0f4ff;
        
        .member-name {
          color: #6B8DD6;
          font-weight: 500;
        }
      }
      
      .member-avatar {
        width: 80rpx;
        height: 80rpx;
        border-radius: 50%;
        margin-right: 24rpx;
        background: linear-gradient(135deg, #6B8DD6, #8B5CF6);
        display: flex;
        align-items: center;
        justify-content: center;
        color: #fff;
        font-size: 28rpx;
        
        &.default {
          background: linear-gradient(135deg, #68d391, #48bb78);
        }
      }
      
      .member-name {
        font-size: 30rpx;
        color: #2d3748;
      }
    }
  }
}
</style>