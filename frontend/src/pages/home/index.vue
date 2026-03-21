<template>
  <view class="home-page">
    <!-- 实际内容 -->
    <!-- 顶部欢迎区 -->
    <view class="header-section">
      <view class="header-bg">
        <view class="bg-pattern"></view>
      </view>
      <view class="header-content">
        <view class="header-left">
          <!-- 未登录状态 -->
          <template v-if="!isLoggedIn">
            <view class="user-avatar" @click="goLogin">
              <view class="avatar-placeholder">
                <text>登录</text>
              </view>
            </view>
            <view class="header-info">
              <view class="greeting-row">
                <text class="time-label">{{ greeting }}</text>
                <text class="user-name" @click="goLogin">点击登录/注册</text>
              </view>
              <view class="date-row">
                <text class="date-icon">📅</text>
                <text class="current-date">{{ currentDate }}</text>
              </view>
            </view>
          </template>
          
          <!-- 已登录状态 -->
          <template v-else>
            <!-- 用户头像 -->
            <view class="user-avatar" @click="goToProfile">
              <image 
                v-if="userStore.userInfo?.avatar" 
                :src="getAvatarUrl(userStore.userInfo.avatar)" 
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
          </template>
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
          <u-icon :name="item.icon" color="#fff" size="48"></u-icon>
        </view>
        <text class="action-name">{{ item.name }}</text>
      </view>
    </view>
    
    <!-- 今日概览卡片 -->
    <view class="overview-cards animate-in">
      <!-- 天气卡片 - 优化版 -->
      <view class="overview-card weather-card" @click="handleWeatherClick">
        <view class="weather-card-content">
          <!-- 第一行：图标 + 位置 -->
          <view class="weather-row location-row">
            <view class="weather-icon-wrapper" :style="{ background: weatherData.iconBg }">
              <text class="weather-icon">{{ weatherData.icon }}</text>
            </view>
            <text class="location-name" :class="{ 'location-name-long': weatherData.locationName && weatherData.locationName.length > 8 }">{{ weatherData.locationName }}</text>
          </view>
          
          <!-- 第二行：温度 + 天气描述 -->
          <view class="weather-row temp-row">
            <text class="temp-value">{{ weatherData.temperature === '--' ? '--' : weatherData.temperature + '°' }}</text>
            <text class="weather-desc">{{ weatherData.description }}</text>
          </view>
          
          <!-- 第三行：当前位置提示 + 降雨提醒 -->
          <view class="weather-row hint-row">
            <text v-if="!weatherData.isLocationAuthorized" class="location-hint">点击开启定位</text>
            <text v-else-if="weatherData.loading" class="location-hint">获取位置中...</text>
            <text v-else class="location-hint">当前位置</text>
          </view>
          
          <!-- 第四行：降雨提醒 -->
          <view v-if="weatherData.rainAlert" class="weather-row rain-alert-row">
            <text class="rain-alert-text">☔ {{ weatherData.rainAlert }}</text>
          </view>
        </view>
      </view>
      
      <!-- 喝水卡片 -->
      <view class="overview-card water-card" @click="navigateTo('/pages/water/index')">
        <view class="card-header">
          <view class="card-icon-wrapper water-icon">
            <text class="card-icon">💧</text>
          </view>
          <text class="card-title">今日喝水</text>
        </view>
        <view class="water-content">
          <text class="water-amount">{{ waterData.todayAmount }}</text>
          <text class="water-unit">ml</text>
        </view>
        <view class="water-progress">
          <view class="progress-bar">
            <view class="progress-fill" :style="{ width: waterData.percent + '%' }"></view>
          </view>
          <text class="progress-text">{{ waterData.percent }}%</text>
        </view>
        <text class="water-target">目标: {{ waterData.targetAmount }}ml</text>
      </view>
      
    </view>
    
    <!-- 今日提醒 -->
    <view class="section-card reminder-card-small animate-in">
      <view class="section-header">
        <view class="title-wrapper">
          <text class="section-icon">✨</text>
          <text class="section-title">今日提醒</text>

        </view>
        <view class="header-actions">
          <view class="add-btn" @click="goAddReminder">
            <text>+</text>
          </view>
          <view class="more-btn" @click="goReminderList">
            <text>更多</text>
            ›
          </view>
          <view class="collapse-btn" @click="toggleReminderCollapse">
            <text class="collapse-icon" :class="{ 'collapsed': isReminderCollapsed }">⌄</text>
          </view>
        </view>
      </view>
      
      <view v-if="!isReminderCollapsed" class="section-content">
        <view v-if="todayReminders.length > 0" class="reminder-list">
          <view 
            v-for="(reminder, index) in todayReminders" 
            :key="reminder.id"
            class="reminder-item"
            :style="{ animationDelay: `${index * 0.08}s` }"
            @click="goReminderDetail(reminder)"
          >
            <view class="reminder-icon-wrapper" :style="{ background: reminder.iconBg }">
              <text class="reminder-icon">{{ reminder.icon }}</text>
            </view>
            <view class="reminder-info">
              <text class="reminder-title">{{ reminder.title }}</text>
              <text class="reminder-time">{{ reminder.time }}</text>
            </view>
            <view class="reminder-tag" :class="reminder.frequencyType?.toLowerCase()">
              <text>{{ formatFrequencyType(reminder.frequencyType) }}</text>
            </view>
          </view>
        </view>
        
        <view v-else class="empty-state" @click="goAddReminder">
          <view class="empty-icon-wrapper">
            <text class="empty-icon">✨</text>
          </view>
          <text class="empty-text">暂无提醒</text>
          <text class="empty-subtext">点击添加提醒</text>
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
          <view class="collapse-btn" @click="toggleTaskCollapse">
            <text class="collapse-icon" :class="{ 'collapsed': isTaskCollapsed }">⌄</text>
          </view>
        </view>
      </view>
      
      <view v-if="!isTaskCollapsed" class="section-content">
      
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
              <view class="people-display">
                <!-- 创建者 -->
                <view class="person" v-if="task.creatorId">
                  <image 
                    class="person-avatar-small" 
                    :src="getMemberAvatar(task.creatorId) || '/static/avatar-default.png'" 
                    mode="aspectFill"
                  />
                  <text class="person-name-small">{{ task.creatorNickname || getMemberName(task.creatorId) }}</text>
                </view>
                <!-- 箭头 -->
                <text class="arrow-small" v-if="task.creatorId !== task.assigneeId">→</text>
                <!-- 执行者 -->
                <view class="person" v-if="task.assigneeId && task.creatorId !== task.assigneeId">
                  <image 
                    class="person-avatar-small" 
                    :src="getMemberAvatar(task.assigneeId) || '/static/avatar-default.png'" 
                    mode="aspectFill"
                  />
                  <text class="person-name-small">{{ task.assigneeNickname || getMemberName(task.assigneeId) }}</text>
                </view>
              </view>
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
    
    <!-- 任务弹窗组件 -->
    <TaskModal ref="taskModalRef" :members="familyMembers" @success="onTaskSaved" />
    
    <!-- 提醒编辑/新建弹窗 -->
    <view v-if="showReminderModal" class="modal-mask" @click="closeReminderModal">
      <view class="modal-content" @click.stop>
        <!-- 弹窗头部 -->
        <view class="modal-header">
          <text class="modal-title">{{ isNewReminder ? '新建提醒' : '编辑提醒' }}</text>
          <view class="modal-close" @click="closeReminderModal">
            <text>✕</text>
          </view>
        </view>
        
        <scroll-view class="modal-body" scroll-y>
          <!-- 基本信息 -->
          <view class="form-section">
            <view class="section-title">基本信息</view>
            
            <view class="form-item">
              <text class="form-label">标题 <text class="required">*</text></text>
              <input 
                class="form-input" 
                v-model="reminderForm.titleTemplate" 
                placeholder="请输入提醒标题"
                maxlength="100"
              />
            </view>
            
            <view class="form-item">
              <text class="form-label">内容</text>
              <textarea 
                class="form-textarea" 
                v-model="reminderForm.contentTemplate" 
                placeholder="请输入提醒内容（选填）"
                maxlength="500"
              />
            </view>
          </view>
          
          <!-- 频率设置 -->
          <view class="form-section">
            <view class="section-title">频率设置</view>
            
            <view class="form-item">
              <text class="form-label">提醒频率</text>
              <picker :range="freqOptions" :range-key="'label'" :value="freqIndex" @change="onFreqChange">
                <view class="picker">
                  <text>{{ freqOptions[freqIndex]?.label || '请选择' }}</text>
                  <text class="picker-arrow">›</text>
                </view>
              </picker>
            </view>
            
            <!-- 每天/一次性：选择时间 -->
            <view class="form-item" v-if="reminderForm.frequencyType === 'DAILY' || reminderForm.frequencyType === 'ONCE'">
              <text class="form-label">提醒时间</text>
              <picker mode="time" :value="reminderForm.remindTime" @change="onTimeChange">
                <view class="picker">
                  <text>{{ reminderForm.remindTime || '选择时间' }}</text>
                  <text class="picker-arrow">›</text>
                </view>
              </picker>
            </view>
            
            <!-- 一次性：选择日期 -->
            <view class="form-item" v-if="reminderForm.frequencyType === 'ONCE'">
              <text class="form-label">提醒日期</text>
              <picker mode="date" :value="reminderForm.onceDate" @change="onOnceDateChange">
                <view class="picker">
                  <text>{{ reminderForm.onceDate || '选择日期' }}</text>
                  <text class="picker-arrow">›</text>
                </view>
              </picker>
            </view>
            
            <!-- 每周：选择星期 -->
            <view class="form-item" v-if="reminderForm.frequencyType === 'WEEKLY'">
              <text class="form-label">选择星期</text>
              <view class="week-selector">
                <view 
                  v-for="day in weekDays" 
                  :key="day.value"
                  class="week-day"
                  :class="{ active: reminderForm.weekDays.includes(day.value) }"
                  @click="toggleWeekDay(day.value)"
                >
                  {{ day.label }}
                </view>
              </view>
            </view>
            
            <!-- 每月：选择日期 -->
            <view class="form-item" v-if="reminderForm.frequencyType === 'MONTHLY'">
              <text class="form-label">选择日期</text>
              <picker :range="dayOptions" :value="reminderForm.monthDay - 1" @change="onMonthDayChange">
                <view class="picker">
                  <text>{{ dayOptions[reminderForm.monthDay - 1] || '1日' }}</text>
                  <text class="picker-arrow">›</text>
                </view>
              </picker>
            </view>
            
            <!-- 间隔：每N分钟/小时/天 -->
            <view class="form-item" v-if="reminderForm.frequencyType === 'INTERVAL'">
              <text class="form-label">间隔时间</text>
              <view class="interval-input-group">
                <text class="interval-label">每</text>
                <input 
                  class="interval-input" 
                  type="number" 
                  v-model="reminderForm.intervalValue"
                  placeholder="60"
                />
                <picker :range="['分钟', '小时', '天']" :value="['minutes', 'hours', 'days'].indexOf(reminderForm.intervalUnit)" @change="onIntervalUnitChange">
                  <view class="interval-unit-picker">
                    <text>{{ intervalUnitText }}</text>
                    <text class="picker-arrow">›</text>
                  </view>
                </picker>
              </view>
            </view>
            
            <!-- 每小时：间隔几小时 -->
            <view class="form-item" v-if="reminderForm.frequencyType === 'HOURLY'">
              <text class="form-label">间隔小时</text>
              <view class="interval-input-group">
                <text class="interval-label">每</text>
                <input 
                  class="interval-input" 
                  type="number" 
                  v-model="reminderForm.intervalHours"
                  placeholder="1"
                />
                <text class="interval-label">小时</text>
              </view>
            </view>
            
            <!-- 仅工作日 -->
            <view class="form-item switch-item">
              <text class="form-label">仅工作日推送</text>
              <switch :checked="reminderForm.workDaysOnly" @change="reminderForm.workDaysOnly = $event.detail.value" color="#667eea" />
            </view>
          </view>
          
          <!-- 推送范围 -->
          <view class="form-section">
            <view class="section-title">推送范围</view>
            
            <view class="form-item">
              <text class="form-label">推送对象</text>
              <view class="radio-group">
                <view 
                  v-for="scope in scopeOptions" 
                  :key="scope.value"
                  class="radio-item"
                  :class="{ active: reminderForm.pushScope === scope.value }"
                  @click="selectPushScope(scope.value)"
                >
                  <view class="radio-circle">
                    <view v-if="reminderForm.pushScope === scope.value" class="radio-dot"></view>
                  </view>
                  <text>{{ scope.label }}</text>
                </view>
              </view>
            </view>
            
            <!-- 已选择的用户显示 -->
            <view class="form-item" v-if="reminderForm.pushScope === 'SPECIFIED'">
              <text class="form-label">选择用户 ({{ targetUsers.length }}人)</text>
              <view v-if="targetUsers.length === 0" class="loading-text" @click="openUserPicker">点击选择用户</view>
              <view v-else class="selected-users" @click="openUserPicker">
                <text class="selected-count">{{ reminderForm.targetUserIds.length }} 人</text>
                <text class="change-btn">修改</text>
              </view>
            </view>
          </view>
        </scroll-view>
        
        <!-- 弹窗底部 -->
        <view class="modal-footer">
          <button class="cancel-btn" @click="closeReminderModal">取消</button>
          <button class="save-btn" @click="saveReminder">保存</button>
        </view>
      </view>
    </view>
    
    <!-- 用户选择弹窗 -->
    <view v-if="showUserPicker" class="user-picker-mask" @click="closeUserPicker">
      <view class="user-picker-content" @click.stop>
        <view class="user-picker-header">
          <text class="cancel" @click="closeUserPicker">取消</text>
          <text class="title">选择指定用户 ({{ targetUsers.length }})</text>
          <text class="confirm" @click="confirmUserPicker">确定</text>
        </view>
        <scroll-view class="user-picker-body" scroll-y>
          <view v-if="targetUsers.length === 0" style="text-align: center; padding: 100rpx; color: #999;">
            暂无用户数据
          </view>
          <block v-else>
            <view 
              v-for="(user, index) in targetUsers" 
              :key="index"
              class="user-item"
              :class="{ selected: selectedUsers.includes(user.userId) }"
              @click="toggleUserSelection(user.userId)"
            >
              <image class="user-avatar" :src="getAvatarUrl(user.avatar)" mode="aspectFill" />
              <text class="user-name">{{ user.nickname || '用户' }}</text>
              <view v-if="selectedUsers.includes(user.userId)" class="check-icon">✓</view>
            </view>
          </block>
        </scroll-view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useUserStore } from '../../stores/user'
import { taskApi } from '../../api/task'
import { familyApi } from '../../api/family'
import { anniversaryApi } from '../../api/anniversary'
import { weatherApi } from '../../api/weather'
import { waterApi } from '../../api/water'
import { getCurrentLocationWithAddress, getWeatherByCode, getShortLocationName } from '../../utils/weather'
import { formatDateTime } from '../../utils/dateHelper'
import { checkAutoLogin, doAutoLogin } from '../../utils/autoLogin'
import { getAvatarUrl, request } from '../../utils/request'
import LazyImage from '@/components/common/LazyImage.vue'
import TaskModal from '@/components/TaskModal.vue'

const userStore = useUserStore()
const taskModalRef = ref(null)

// ========== 任务弹窗操作 ==========
const goAddTask = () => {
   // console.log('点击添加按钮', taskModalRef.value)
  // 添加延迟确保组件已挂载
  setTimeout(async () => {
    if (!taskModalRef.value) {
       // console.error('TaskModal 组件未加载')
      uni.showToast({ title: '组件未加载，请重试', icon: 'none' })
      return
    }
     // console.log('准备打开弹窗')
    try {
      await taskModalRef.value.open()
       // console.log('弹窗打开成功')
    } catch (err) {
       // console.error('弹窗打开失败:', err)
    }
  }, 100)
}

const onTaskSaved = () => {
  // 刷新今日任务列表
  loadTodayTasks()
}

// 获取成员名称
const getMemberName = (userId) => {
  const member = familyMembers.value.find(m => m.userId === userId)
  return member ? (member.nickname || member.name || '家人') : '未知'
}

// 获取成员头像
const getMemberAvatar = (userId) => {
  if (!userId) {
     // console.log('getMemberAvatar: userId is empty')
    return '/static/avatar-default.png'
  }
  try {
    const userIdNum = Number(userId)
    const member = familyMembers.value?.find(m => Number(m?.userId) === userIdNum)
    if (!member) {
       // console.log('getMemberAvatar: member not found for userId:', userId)
      return '/static/avatar-default.png'
    }
    const avatarUrl = getAvatarUrl(member.avatar)
     // console.log('getMemberAvatar: found for userId:', userId, 'avatar:', avatarUrl)
    return avatarUrl || '/static/avatar-default.png'
  } catch (e) {
     // console.error('getMemberAvatar error:', e)
    return '/static/avatar-default.png'
  }
}

// 获取任务人员显示（创建人 → 被指派人）
const getTaskPeople = (task) => {
  if (!task) return ''
  const creatorId = task.creatorId
  const assigneeId = task.assigneeId
  const creatorName = task.creatorName || task.creatorNickname || getMemberName(creatorId)
  const assigneeName = task.assigneeName || task.assigneeNickname || getMemberName(assigneeId)
  
  // 如果是自己创建并指派给自己，只显示创建人
  if (creatorId === assigneeId) {
    return creatorName
  }
  
  // 否则显示 创建人 → 被指派人
  return `${creatorName} → ${assigneeName}`
}

// 格式化任务时间
const formatTaskTime = (dueTime) => {
  return formatDateTime(dueTime, 'time')
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
       // console.error('加载家庭成员失败', e)
      familyMembers.value = []
    }
    
    // 使用专门的今日任务API，后端已按今天日期筛选
    const res = await taskApi.getTodayTasks(familyId)
    const todayTodoTasks = res || []
    
    // 格式化并显示所有今日待办（过滤掉已完成的）
    if (todayTodoTasks.length > 0) {
      todayTasks.value = todayTodoTasks
        .filter(task => task.status !== 2) // 过滤已完成的任务
        .map(task => ({
          ...task,
          assigneeName: task.assigneeNickname || getMemberName(task.assigneeId) || '家人',
          creatorName: task.creatorNickname || getMemberName(task.creatorId) || '家人',
          time: formatTaskTime(task.dueTime)
        }))
    } else {
      todayTasks.value = []
    }
  } catch (e) {
     // console.error('加载待办数据失败', e)
    todayTasks.value = []
  }
}

// 格式化频率类型为中文
const formatFrequencyType = (type) => {
  const typeMap = {
    'ONCE': '一次性',
    'DAILY': '每天',
    'HOURLY': '每小时',
    'WEEKLY': '每周',
    'MONTHLY': '每月',
    'YEARLY': '每年',
    'INTERVAL': '间隔'
  }
  return typeMap[type] || type || '定时'
}

const parseReminderBusinessData = (businessData) => {
  if (!businessData) {
    return {}
  }

  if (typeof businessData === 'object') {
    return businessData
  }

  if (typeof businessData !== 'string') {
    return {}
  }

  try {
    const parsed = JSON.parse(businessData)
    return parsed && typeof parsed === 'object' ? parsed : {}
  } catch (error) {
    return {}
  }
}

const getReminderCardTitle = (reminder) => {
  const businessData = parseReminderBusinessData(reminder.businessData)
  const sceneType = businessData.sceneType || reminder.reminderType
  const sceneTitleMap = {
    WATER: '喝水提醒',
    WEATHER_RAIN: '下雨提醒',
    WEATHER_TEMP: '温度提醒',
    SEDENTARY: '久坐提醒',
    EYE_REST: '护眼提醒'
  }

  if (sceneTitleMap[sceneType]) {
    return sceneTitleMap[sceneType]
  }

  return reminder.reminderName || reminder.titleTemplate || '提醒'
}

// 加载今日提醒
const loadTodayReminders = async () => {
  try {
    const res = await request.get('/api/reminder/today')
    // 格式化提醒数据
    todayReminders.value = (res || []).map(reminder => {
        const iconMap = {
          'WATER': { icon: '💧', bg: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)' },
          'MEDICINE': { icon: '💊', bg: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)' },
          'EXPIRE': { icon: '📦', bg: 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)' },
          'BIRTHDAY': { icon: '🎂', bg: 'linear-gradient(135deg, #ff9a9e 0%, #fecfef 100%)' },
          'FINANCE': { icon: '💰', bg: 'linear-gradient(135deg, #30cfd0 0%, #330867 100%)' },
          'SYSTEM': { icon: '✨', bg: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)' }
        }
        const iconConfig = iconMap[reminder.reminderType] || iconMap['SYSTEM']
        // 优先使用下次执行时间，特别是间隔类型
        let displayTime = reminder.nextExecuteTime || reminder.remindTime || ''
        // 如果是数组格式，转换为字符串
        if (Array.isArray(displayTime)) {
          const [year, month, day, hour, minute] = displayTime
          displayTime = `${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}`
        }
        return {
          ...reminder,
          icon: iconConfig.icon,
          iconBg: iconConfig.bg,
          title: getReminderCardTitle(reminder),
          time: displayTime
        }
      })
    
    // 如果没有提醒内容，默认折叠起来
    if (todayReminders.value.length === 0) {
      isReminderCollapsed.value = true
    }
  } catch (e) {
     // console.error('加载今日提醒失败', e)
    todayReminders.value = []
    // 加载失败也默认折叠
    isReminderCollapsed.value = true
  }
}

// 跳转到提醒管理页面新增 - 统一使用提醒管理页面的代码
const goAddReminder = () => {
  uni.switchTab({
    url: '/pages/reminder/index',
    success: () => {
      // 延迟打开新增弹窗
      setTimeout(() => {
        uni.$emit('openAddReminder')
      }, 100)
    }
  })
}

// 跳转到提醒列表
const goReminderList = () => {
  uni.switchTab({
    url: '/pages/reminder/index'
  })
}

// 跳转到提醒详情 - 使用列表数据（不再调用接口）
const goReminderDetail = (reminder) => {
  isNewReminder.value = false
  
  // 调试：查看列表数据
   // console.log('首页提醒详情（列表数据）:', reminder)
  
  // 解析 frequencyConfig
  let config = {}
  try {
    config = JSON.parse(reminder.frequencyConfig || '{}')
  } catch (e) {
     // console.error('解析 frequencyConfig 失败', e)
  }
  
  // 解析日期和配置
  let onceDate = ''
  let yearMonthDay = ''
  let weekDays = []
  let monthDay = 1
  let intervalValue = 60
  let intervalHours = 1
  let intervalUnit = 'minutes'
  
  if (reminder.frequencyType === 'ONCE' && config.date) {
    onceDate = config.date
  } else if (reminder.frequencyType === 'WEEKLY' && config.weekDays) {
    weekDays = config.weekDays
  } else if (reminder.frequencyType === 'MONTHLY' && config.monthDay) {
    monthDay = config.monthDay
  } else if (reminder.frequencyType === 'INTERVAL') {
    if (config.intervalMinutes) {
      intervalValue = config.intervalMinutes
      intervalUnit = 'minutes'
    } else if (config.intervalHours) {
      intervalValue = config.intervalHours
      intervalUnit = 'hours'
    } else if (config.intervalDays) {
      intervalValue = config.intervalDays
      intervalUnit = 'days'
    }
  } else if (reminder.frequencyType === 'HOURLY' && config.intervalHours) {
    intervalHours = config.intervalHours
  }
  
  // 解析 targetUserIds
  let targetUserIds = []
  if (reminder.targetUserIds) {
    try {
      targetUserIds = JSON.parse(reminder.targetUserIds)
    } catch (e) {}
  }
  
  // 初始化编辑表单
  reminderForm.value = {
    id: reminder.id,
    reminderType: reminder.reminderType || 'SYSTEM',
    frequencyType: reminder.frequencyType || 'DAILY',
    remindTime: config.fixedTime || reminder.remindTime || '08:00',
    titleTemplate: reminder.titleTemplate || '',
    contentTemplate: reminder.contentTemplate || '',
    pushScope: reminder.pushScope || 'SELF',
    onceDate: onceDate,
    yearMonthDay: yearMonthDay,
    weekDays: weekDays,
    monthDay: monthDay,
    intervalValue: intervalValue,
    intervalHours: intervalHours,
    intervalUnit: intervalUnit,
    workDaysOnly: config.workDaysOnly || false,
    targetUserIds: targetUserIds
  }
  showReminderModal.value = true
}

// 关闭提醒弹窗
const closeReminderModal = () => {
  showReminderModal.value = false
  resetReminderForm()
}

// 重置提醒表单
const resetReminderForm = () => {
  reminderForm.value = {
    id: null,
    reminderType: 'SYSTEM',
    frequencyType: 'DAILY',
    remindTime: '08:00',
    onceDate: '',
    yearMonthDay: '',
    weekDays: [],
    monthDay: 1,
    intervalValue: 60,
    intervalHours: 1,
    intervalUnit: 'minutes',
    workDaysOnly: false,
    pushScope: 'SELF',
    targetUserIds: [],
    titleTemplate: '',
    contentTemplate: ''
  }
}

// 弹窗事件处理
const onFreqChange = (e) => {
  reminderForm.value.frequencyType = freqOptions[e.detail.value].value
}

const onTimeChange = (e) => {
  reminderForm.value.remindTime = e.detail.value
}

const onOnceDateChange = (e) => {
  reminderForm.value.onceDate = e.detail.value
}

const onMonthDayChange = (e) => {
  reminderForm.value.monthDay = e.detail.value + 1
}

const onIntervalUnitChange = (e) => {
  const units = ['minutes', 'hours', 'days']
  reminderForm.value.intervalUnit = units[e.detail.value]
}

const toggleWeekDay = (value) => {
  const index = reminderForm.value.weekDays.indexOf(value)
  if (index > -1) {
    reminderForm.value.weekDays.splice(index, 1)
  } else {
    reminderForm.value.weekDays.push(value)
  }
}

// 保存提醒
const saveReminder = async () => {
  if (!reminderForm.value.titleTemplate) {
    uni.showToast({ title: '请输入提醒标题', icon: 'none' })
    return
  }
  
  // 构建频率配置
  const config = {}
  const f = reminderForm.value
  
  if (f.frequencyType === 'ONCE') {
    if (!f.onceDate) {
      uni.showToast({ title: '请选择提醒日期', icon: 'none' })
      return
    }
    config.date = f.onceDate
    config.time = f.remindTime
  } else if (['DAILY', 'WEEKLY', 'MONTHLY', 'YEARLY'].includes(f.frequencyType)) {
    config.fixedTime = f.remindTime
    if (f.frequencyType === 'WEEKLY') {
      if (f.weekDays.length === 0) {
        uni.showToast({ title: '请选择星期', icon: 'none' })
        return
      }
      config.weekDays = f.weekDays
    } else if (f.frequencyType === 'MONTHLY') {
      config.monthDay = f.monthDay
    } else if (f.frequencyType === 'YEARLY') {
      if (!f.yearMonthDay) {
        uni.showToast({ title: '请选择月日', icon: 'none' })
        return
      }
      config.monthDay = parseInt(f.yearMonthDay.split('-')[0])
      config.month = parseInt(f.yearMonthDay.split('-')[1])
    }
  } else if (f.frequencyType === 'INTERVAL') {
    const key = f.intervalUnit === 'minutes' ? 'intervalMinutes' : 
                f.intervalUnit === 'hours' ? 'intervalHours' : 'intervalDays'
    config[key] = parseInt(f.intervalValue) || 1
  } else if (f.frequencyType === 'HOURLY') {
    config.intervalHours = parseInt(f.intervalHours) || 1
  }
  config.workDaysOnly = f.workDaysOnly
  
  const data = {
    reminderName: f.titleTemplate,
    reminderType: f.reminderType || 'SYSTEM',
    frequencyType: f.frequencyType,
    frequencyConfig: JSON.stringify(config),
    pushScope: f.pushScope,
    targetUserIds: f.pushScope === 'SPECIFIED' ? JSON.stringify(f.targetUserIds) : null,
    titleTemplate: f.titleTemplate,
    contentTemplate: f.contentTemplate,
    remindTime: f.remindTime,
    status: 1
  }
  
  try {
    if (isNewReminder.value) {
      // 新建提醒
      await request.post('/api/reminder/add', data)
      uni.showToast({ title: '创建成功' })
    } else {
      // 编辑提醒
      data.id = f.id
      await request.post('/api/reminder/update', data)
      uni.showToast({ title: '保存成功' })
    }
    closeReminderModal()
    // 先展开提醒列表，再加载数据
    isReminderCollapsed.value = false
    await loadTodayReminders()
  } catch (e) {
     // console.error('保存失败', e)
    uni.showToast({ title: '保存失败', icon: 'none' })
  }
}

// ========== 提醒弹窗相关结束 ==========

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

// 登录状态判断
const isLoggedIn = computed(() => {
  return !!userStore.userInfo?.id && !!userStore.token
})

const currentFamily = ref({ name: '幸福小家' })

// 快捷功能 - 添加阴影
const quickActions = [
  { name: '纪念日', icon: 'heart', bgColor: '#FF6B6B', shadow: '0 8rpx 20rpx rgba(255, 107, 107, 0.35)', path: '/pages/anniversary/index' },
  { name: '心愿', icon: 'gift', bgColor: '#FF6B9D', shadow: '0 8rpx 20rpx rgba(255, 107, 157, 0.35)', path: '/pages/wish/index' },
  { name: '智能场景', icon: 'grid', bgColor: '#0EA5E9', shadow: '0 8rpx 20rpx rgba(14, 165, 233, 0.35)', path: '/pages/reminder/scene-manage' }
]

// 今日任务
const todayTasks = ref([])

// 今日提醒
const todayReminders = ref([])

// 折叠状态
const isReminderCollapsed = ref(false)
const isTaskCollapsed = ref(false)

// 跳转场景管理
const goSceneManage = () => {
  uni.navigateTo({
    url: '/pages/reminder/scene-manage'
  })
}

// 切换折叠状态
const toggleReminderCollapse = () => {
  isReminderCollapsed.value = !isReminderCollapsed.value
}

const toggleTaskCollapse = () => {
  isTaskCollapsed.value = !isTaskCollapsed.value
}

// 提醒弹窗相关数据
const showReminderModal = ref(false)
const isNewReminder = ref(true)

// 提醒表单数据
const reminderForm = ref({
  id: null,
  reminderType: 'SYSTEM',
  frequencyType: 'DAILY',
  remindTime: '08:00',
  onceDate: '',
  yearMonthDay: '',
  weekDays: [],
  monthDay: 1,
  intervalValue: 60,
  intervalHours: 1,
  intervalUnit: 'minutes',
  workDaysOnly: false,
  pushScope: 'SELF',
  targetUserIds: [],
  titleTemplate: '',
  contentTemplate: ''
})

// 用户选择相关
const showUserPicker = ref(false)
const targetUsers = ref([])
const selectedUsers = ref([])

// 选择推送范围
const selectPushScope = (value) => {
  reminderForm.value.pushScope = value
  if (value === 'SPECIFIED') {
    openUserPicker()
  }
}

// 打开用户选择弹窗
const openUserPicker = async () => {
  showUserPicker.value = true
  try {
    // 使用和待办一样的接口：/api/family/{familyId}/members
    const familyId = uni.getStorageSync('currentFamilyId') || 1
    const res = await request.get(`/api/family/${familyId}/members`)
     // console.log('家庭成员接口返回:', res)
    
    // 处理响应数据 - 可能是 res.data 或直接是数组
    let members = []
    if (res && res.data && Array.isArray(res.data)) {
      members = res.data
    } else if (Array.isArray(res)) {
      members = res
    } else if (res && res.list && Array.isArray(res.list)) {
      members = res.list
    }
    
     // console.log('解析的成员列表:', members)
    
    // 排除当前用户自己
    const currentUserId = uni.getStorageSync('userId')
    targetUsers.value = members.filter(m => m.userId != currentUserId)
     // console.log('过滤后的用户列表:', targetUsers.value)
    
    // 初始化已选择状态
    selectedUsers.value = [...reminderForm.value.targetUserIds]
  } catch (e) {
     // console.error('加载用户列表失败', e)
    uni.showToast({ title: '加载失败', icon: 'none' })
  }
}

// 关闭用户选择弹窗
const closeUserPicker = () => {
  showUserPicker.value = false
}

// 确认用户选择
const confirmUserPicker = () => {
  reminderForm.value.targetUserIds = [...selectedUsers.value]
  closeUserPicker()
}

// 切换用户选择
const toggleUserSelection = (userId) => {
  const index = selectedUsers.value.indexOf(userId)
  if (index > -1) {
    selectedUsers.value.splice(index, 1)
  } else {
    selectedUsers.value.push(userId)
  }
}

// 选项配置
const freqOptions = [
  { value: 'ONCE', label: '一次性' },
  { value: 'DAILY', label: '每天' },
  { value: 'HOURLY', label: '每小时' },
  { value: 'WEEKLY', label: '每周' },
  { value: 'MONTHLY', label: '每月' },
  { value: 'YEARLY', label: '每年' },
  { value: 'INTERVAL', label: '间隔' }
]

const scopeOptions = [
  { value: 'SELF', label: '仅自己' },
  { value: 'SPECIFIED', label: '指定用户' }
]

const weekDays = [
  { value: 1, label: '一' },
  { value: 2, label: '二' },
  { value: 3, label: '三' },
  { value: 4, label: '四' },
  { value: 5, label: '五' },
  { value: 6, label: '六' },
  { value: 7, label: '日' }
]

const dayOptions = Array.from({ length: 31 }, (_, i) => `${i + 1}日`)

// 计算属性：频率索引
const freqIndex = computed(() => {
  return freqOptions.findIndex(f => f.value === reminderForm.value.frequencyType)
})

// 间隔单位显示文本
const intervalUnitText = computed(() => {
  const unitMap = {
    'minutes': '分钟',
    'hours': '小时',
    'days': '天'
  }
  return unitMap[reminderForm.value.intervalUnit] || '分钟'
})

// 家庭成员列表（用于显示指派人名称）
const familyMembers = ref([])

// 纪念日
const upcomingAnniversaries = ref([])

// 今日概览数据
const overviewData = ref({
  calories: 1200
})

// 喝水数据
const waterData = ref({
  todayAmount: 0,
  targetAmount: 2000,
  percent: 0
})

// 加载喝水数据
const loadWaterData = async () => {
  try {
    const userInfo = uni.getStorageSync('userInfo')
    const userId = userInfo?.id || 1
    const res = await waterApi.getToday(userId)
    if (res) {
      waterData.value.todayAmount = res.todayAmount || 0
      waterData.value.targetAmount = res.targetAmount || 2000
      waterData.value.percent = res.percent || 0
    }
  } catch (e) {
     // console.error('加载喝水数据失败', e)
  }
}

// 天气数据 - 优化版
const weatherData = ref({
  locationName: '定位中...', // 简短位置名（区/街道）
  fullLocation: '',          // 完整位置信息
  temperature: '--',
  description: '加载中...',
  icon: '📍',
  iconBg: 'linear-gradient(135deg, #FFD93D 0%, #F6AD55 100%)', // 默认背景
  weatherCode: 0,
  tempMin: 0,
  tempMax: 0,
  humidity: 0,
  isLoaded: false,
  isLocationAuthorized: true,
  loading: false,
  rainAlert: null // 降雨提醒信息
})

// 获取天气图标背景色
const getWeatherIconBg = (weatherCode) => {
  const code = Number(weatherCode)
  // 晴天 - 暖黄色
  if (code === 0) return 'linear-gradient(135deg, #FFD93D 0%, #FF9F43 100%)'
  // 多云 - 橙色
  if (code === 1) return 'linear-gradient(135deg, #FFA726 0%, #FF7043 100%)'
  // 阴天 - 灰色
  if (code === 2 || code === 3) return 'linear-gradient(135deg, #90A4AE 0%, #607D8B 100%)'
  // 雾 - 浅灰
  if (code === 45 || code === 48) return 'linear-gradient(135deg, #B0BEC5 0%, #78909C 100%)'
  // 毛毛雨 - 浅蓝
  if (code >= 51 && code <= 55) return 'linear-gradient(135deg, #64B5F6 0%, #42A5F5 100%)'
  // 雨 - 蓝色
  if (code >= 61 && code <= 67) return 'linear-gradient(135deg, #42A5F5 0%, #2196F3 100%)'
  // 雪 - 淡蓝
  if (code >= 71 && code <= 77) return 'linear-gradient(135deg, #81D4FA 0%, #4FC3F7 100%)'
  // 阵雨 - 蓝紫
  if (code >= 80 && code <= 82) return 'linear-gradient(135deg, #4FC3F7 0%, #29B6F6 100%)'
  // 阵雪 - 冰蓝
  if (code >= 85 && code <= 86) return 'linear-gradient(135deg, #B3E5FC 0%, #81D4FA 100%)'
  // 雷雨 - 深紫
  if (code >= 95) return 'linear-gradient(135deg, #7E57C2 0%, #5E35B1 100%)'
  // 默认
  return 'linear-gradient(135deg, #FFD93D 0%, #F6AD55 100%)'
}

// 分析降雨提醒
const analyzeRainAlert = (hourlyData) => {
  // 调试：强制显示降雨提醒（测试用）
  // 取消下面注释可测试显示效果
  // return { hoursLater: 2, type: '雨', intensity: '大', weatherCode: 61 }
  
  if (!hourlyData || !hourlyData.time || hourlyData.time.length === 0) {
    return null
  }
  
  const now = new Date()
  const alerts = []
  
  for (let i = 0; i < hourlyData.time.length; i++) {
    const hourTime = new Date(hourlyData.time[i])
    const hoursDiff = Math.round((hourTime - now) / (1000 * 60 * 60))
    
    // 只关注未来24小时内
    if (hoursDiff < 0 || hoursDiff > 24) continue
    
    const weatherCode = hourlyData.weathercode[i]
    const precipitation = hourlyData.precipitation[i] || 0
    const precipitationProbability = hourlyData.precipitation_probability?.[i] || 0
    
    // 判断是否有雨/雪/雷暴
    const isRain = [51, 53, 55, 61, 63, 65, 66, 67, 80, 81, 82, 95, 96, 99].includes(weatherCode)
    const isSnow = [71, 73, 75, 77, 85, 86].includes(weatherCode)
    const isStorm = [95, 96, 99].includes(weatherCode)
    
    if (isRain || isSnow || isStorm) {
      // 判断强度 - 根据气象标准（单位：mm/h）
      // 小雨: < 2.5, 中雨: 2.5-8, 大雨: 8-16, 暴雨: >= 16
      let intensity = '小'
      
      // 优先根据天气代码判断强度
      if (isStorm) {
        intensity = '暴'
      } else if ([65, 67, 82].includes(weatherCode)) {
        // 大雨、冻雨、大阵雨
        intensity = '大'
      } else if ([63, 81].includes(weatherCode)) {
        // 中雨、中阵雨
        intensity = '中'
      } else if ([61, 80].includes(weatherCode) || precipitation >= 2.5) {
        // 小雨、小阵雨 或 降水量 >= 2.5mm/h
        intensity = '小'
      } else if (precipitation >= 8) {
        intensity = '中'
      } else if (precipitation >= 16) {
        intensity = '大'
      } else if (precipitation >= 30) {
        intensity = '暴'
      }
      
      // 降水概率辅助判断（仅当降水量较低时）
      if (precipitation < 2.5 && precipitationProbability >= 80) {
        // 高概率但降水量小，仍为小
        intensity = '小'
      }
      
      alerts.push({
        hoursLater: hoursDiff,
        type: isStorm ? '雷暴' : (isSnow ? '雪' : '雨'),
        intensity: intensity,
        weatherCode: weatherCode,
        precipitation: precipitation
      })
    }
  }
  
  // 返回最近的降雨提醒
  if (alerts.length > 0) {
    // 按时间排序，取最近的一个
    alerts.sort((a, b) => a.hoursLater - b.hoursLater)
    return alerts[0]
  }
  
  return null
}

// 生成降雨提醒文案
const generateRainAlertText = (alert) => {
  if (!alert) return null
  
  const { hoursLater, type, intensity } = alert
  
  // 根据时间范围生成不同文案
  if (hoursLater === 0) {
    return `正在${intensity}${type}`
  } else if (hoursLater === 1) {
    return `1小时后可能会${intensity}${type}`
  } else if (hoursLater <= 3) {
    return `未来${hoursLater}小时可能会${intensity}${type}`
  } else if (hoursLater <= 12) {
    return `${hoursLater}小时后可能会有${intensity}${type}`
  } else {
    return `${hoursLater}小时后可能会有${intensity}${type}`
  }
}

// 获取位置和加载天气
const loadWeatherData = async () => {
  try {
    weatherData.value.loading = true
    
    // 先检查位置授权状态
    let locationAuth = { authorized: false }
    try {
      locationAuth = await checkLocationAuth()
    } catch (e) {
       // console.log('检查位置授权失败:', e)
    }
    
    if (!locationAuth.authorized) {
      // 未授权，显示提示
      weatherData.value = {
        locationName: '定位未开启',
        fullLocation: '',
        temperature: '--',
        description: '点击开启定位',
        icon: '📍',
        iconBg: 'linear-gradient(135deg, #BDBDBD 0%, #9E9E9E 100%)',
        tempMin: 0,
        tempMax: 0,
        humidity: 0,
        isLoaded: true,
        isLocationAuthorized: false,
        loading: false
      }
      return
    }
    
    // 获取位置（带详细地址）
    let location = null
    try {
      location = await getCurrentLocationWithAddress()
    } catch (e) {
       // console.log('获取位置失败:', e)
    }
    
    // 如果位置获取失败，使用默认位置（北京）
    if (!location || !location.latitude) {
      location = {
        latitude: 39.9042,
        longitude: 116.4074,
        locationInfo: { city: '北京市', district: '朝阳区', street: '' }
      }
    }
    
    // 获取简短位置名称
    const shortLocationName = getShortLocationName(location.locationInfo)
    weatherData.value.fullLocation = location.locationInfo?.fullAddress || shortLocationName
    weatherData.value.locationName = shortLocationName
    
    // 调用 Open-Meteo API 获取天气和逐小时预报
    let weatherJson = null
    let hourlyData = null
    try {
      const [weatherRes, hourlyRes] = await Promise.all([
        new Promise((resolve, reject) => {
          uni.request({
            url: `https://api.open-meteo.com/v1/forecast?latitude=${location.latitude}&longitude=${location.longitude}&current_weather=true`,
            method: 'GET',
            success: (res) => resolve(res.data),
            fail: (err) => reject(err)
          })
        }),
        new Promise((resolve, reject) => {
          uni.request({
            url: `https://api.open-meteo.com/v1/forecast?latitude=${location.latitude}&longitude=${location.longitude}&hourly=weathercode,precipitation,precipitation_probability&timezone=Asia/Shanghai&forecast_days=2`,
            method: 'GET',
            success: (res) => resolve(res.data?.hourly || null),
            fail: (err) => {
               // console.log('获取逐小时预报失败:', err)
              resolve(null)
            }
          })
        })
      ])
      weatherJson = weatherRes
      hourlyData = hourlyRes
    } catch (e) {
       // console.log('获取天气API失败:', e)
    }
    
    // 分析降雨提醒
    const rainAlert = analyzeRainAlert(hourlyData)
    const rainAlertText = generateRainAlertText(rainAlert)
    
    if (weatherJson && weatherJson.current_weather) {
      const current = weatherJson.current_weather
      const weatherInfo = getWeatherByCode(current.weathercode)
      
      weatherData.value = {
        locationName: shortLocationName,
        fullLocation: location.locationInfo?.fullAddress || shortLocationName,
        temperature: Math.round(current.temperature),
        description: weatherInfo.desc,
        icon: weatherInfo.icon,
        iconBg: getWeatherIconBg(current.weathercode),
        weatherCode: current.weathercode,
        tempMin: Math.round(current.temperature - 3),
        tempMax: Math.round(current.temperature + 3),
        humidity: 0,
        isLoaded: true,
        isLocationAuthorized: true,
        loading: false,
        rainAlert: rainAlertText
      }
    } else {
      // API 返回数据异常，显示默认天气
      weatherData.value = {
        locationName: shortLocationName || '当前位置',
        fullLocation: '',
        temperature: '--',
        description: '暂无数据',
        icon: '☁️',
        iconBg: 'linear-gradient(135deg, #90A4AE 0%, #607D8B 100%)',
        tempMin: 0,
        tempMax: 0,
        humidity: 0,
        isLoaded: true,
        isLocationAuthorized: true,
        loading: false,
        rainAlert: null
      }
    }
  } catch (error) {
     // console.error('加载天气失败:', error)
    // 使用默认天气数据，而不是显示失败
    weatherData.value = {
      locationName: '当前位置',
      fullLocation: '',
      temperature: '--',
      description: '点击刷新',
      icon: '☁️',
      iconBg: 'linear-gradient(135deg, #90A4AE 0%, #607D8B 100%)',
      tempMin: 0,
      tempMax: 0,
      humidity: 0,
      isLoaded: true,
      isLocationAuthorized: true,
      loading: false,
      rainAlert: null
    }
  }
}

// 检查位置授权状态
const checkLocationAuth = () => {
  return new Promise((resolve) => {
    // #ifdef MP-WEIXIN
    uni.getSetting({
      success: (res) => {
        const authorized = res.authSetting['scope.userLocation']
        resolve({ authorized: authorized === true })
      },
      fail: () => {
        resolve({ authorized: false })
      }
    })
    // #endif
    
    // #ifndef MP-WEIXIN
    // H5/App 环境默认尝试获取位置
    resolve({ authorized: true })
    // #endif
  })
}

// 获取用户位置
const getUserLocation = () => {
  return new Promise((resolve) => {
    uni.getLocation({
      type: 'gcj02',
      success: (res) => {
        resolve({ lat: res.latitude, lon: res.longitude })
      },
      fail: (err) => {
         // console.error('获取位置失败:', err)
        resolve(null)
      }
    })
  })
}

// 请求位置授权
const requestLocationAuth = () => {
  // #ifdef MP-WEIXIN
  uni.authorize({
    scope: 'scope.userLocation',
    success: () => {
      // 授权成功，重新加载天气
      loadWeatherData()
    },
    fail: () => {
      // 授权失败，提示用户去设置
      uni.showModal({
        title: '需要位置权限',
        content: '获取天气需要您的位置信息，是否去设置开启？',
        success: (res) => {
          if (res.confirm) {
            uni.openSetting({
              success: (settingRes) => {
                if (settingRes.authSetting['scope.userLocation']) {
                  loadWeatherData()
                }
              }
            })
          }
        }
      })
    }
  })
  // #endif
  
  // #ifndef MP-WEIXIN
  // H5/App 直接尝试获取位置
  loadWeatherData()
  // #endif
}

const navigateTo = (path) => {
  // tabBar 页面需要用 switchTab
  const tabBarPages = ['/pages/reminder/index', '/pages/task/index', '/pages/home/index', '/pages/family/index', '/pages/profile/index']
  if (tabBarPages.includes(path)) {
    uni.switchTab({ url: path })
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
    
    // 如果是完成任务，立即从今日待办列表中移除
    if (isCompleting) {
      todayTasks.value = todayTasks.value.filter(t => t.id !== task.id)
      uni.showToast({
        title: '任务已完成',
        icon: 'success',
        duration: 1500
      })
    }
  } catch (error) {
    // API 调用失败，回滚状态
    task.status = originalStatus
     // console.error('切换任务状态失败:', error)
    uni.showToast({
      title: isCompleting ? '完成任务失败' : '取消任务失败',
      icon: 'none'
    })
  }
}

const goTaskDetail = (task) => {
  // 直接打开编辑弹窗，不跳转页面
  taskModalRef.value?.openEdit(task)
}

const goTaskList = () => {
  // 跳转到待办列表页面
  uni.switchTab({ url: '/pages/task/index' })
}

const goToProfile = () => {
  uni.navigateTo({ url: '/pages/profile/index' })
}

const goLogin = () => {
  uni.navigateTo({ url: '/pages/login/index' })
}

// 处理天气卡片点击
const handleWeatherClick = () => {
  if (!weatherData.value.isLocationAuthorized || weatherData.value.city === '定位失败' || weatherData.value.city === '加载失败') {
    // 未授权或加载失败，请求授权
    requestLocationAuth()
  } else {
    // 已授权且加载成功，跳转到天气详情页
    uni.navigateTo({ url: '/pages/weather/index' })
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
     // console.log('获取用户信息失败', e)
  }
  
  // 重新加载家庭成员
  const familyId = uni.getStorageSync('currentFamilyId') || 1
  try {
    const membersRes = await familyApi.getMembers(familyId)
    familyMembers.value = membersRes || []
  } catch (e) {
     // console.error('加载家庭成员失败', e)
  }
  
  // 加载今日待办
  await loadTodayTasks()
  
  // 加载喝水数据
  await loadWaterData()
}

// 每次页面显示时都刷新数据
onShow(async () => {
  // H5 兼容：强制从 storage 同步 token
  const storedToken = uni.getStorageSync('token')
  
  if (storedToken && storedToken !== userStore.token) {
    userStore.setToken(storedToken)
  }
  
  // 检查免密登录（从企业微信跳转过来）
  const autoLoginResult = await checkAutoLogin()
  if (autoLoginResult.needLogin && autoLoginResult.token) {
    const loginRes = await doAutoLogin(autoLoginResult.token)
    if (loginRes.success) {
      // 登录成功，刷新用户信息
      await userStore.fetchUserInfo()
    }
  }
  
  // 每次显示页面都刷新数据
  await refreshHomeData()
  
  // 加载天气数据
  loadWeatherData()
  
  // 加载今日提醒
  loadTodayReminders()
  
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
  padding: 36rpx 32rpx;
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
    width: calc(25% - 16rpx);
    margin-bottom: 0;
    animation: fadeInUp 0.5s ease-out forwards;
    opacity: 0;
    
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
    min-width: 0; // 防止内容溢出挤压
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
        
        &.weather-icon {
          background: linear-gradient(135deg, #4facfe, #00f2fe);
          box-shadow: 0 6rpx 16rpx rgba(79, 172, 254, 0.35);
        }
      }
      
      .card-title {
        font-size: 26rpx;
        color: #5a6c7d;
        font-weight: 500;
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
    
    // 喝水卡片样式
    .water-content {
      display: flex;
      align-items: baseline;
      margin-bottom: 16rpx;
      
      .water-amount {
        font-size: 44rpx;
        font-weight: 700;
        color: #2d3748;
      }
      
      .water-unit {
        font-size: 24rpx;
        color: #8b9aad;
        margin-left: 6rpx;
      }
    }
    
    .water-progress {
      margin-bottom: 12rpx;
      
      .progress-bar {
        height: 12rpx;
        background: #e2e8f0;
        border-radius: 6rpx;
        overflow: hidden;
        margin-bottom: 8rpx;
        
        .progress-fill {
          height: 100%;
          background: linear-gradient(90deg, #4ECDC4, #44A08D);
          border-radius: 6rpx;
          transition: width 0.6s cubic-bezier(0.4, 0, 0.2, 1);
        }
      }
      
      .progress-text {
        font-size: 22rpx;
        color: #4ECDC4;
        font-weight: 600;
      }
    }
    
    .water-target {
      font-size: 22rpx;
      color: #8b9aad;
    }
    
    // 天气卡片样式 - 优化版
    &.weather-card {
      background: linear-gradient(135deg, #ffffff 0%, #f8fafc 100%);
      padding: 24rpx 20rpx;
      min-height: 200rpx;
      display: flex;
      flex-direction: column;
      justify-content: center;
      
      .weather-card-content {
        display: flex;
        flex-direction: column;
        gap: 12rpx;
        width: 100%; // 确保内容区占满卡片
        box-sizing: border-box;
      }
      
      // 第一行：图标 + 位置
      .location-row {
        display: flex;
        align-items: center;
        gap: 12rpx;
        min-width: 0; // 防止溢出
        width: 100%; // 确保占满容器宽度
        
        .weather-icon-wrapper {
          width: 52rpx;
          height: 52rpx;
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          flex-shrink: 0;
          box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
          
          .weather-icon {
            font-size: 32rpx;
          }
        }
        
        .location-name {
          font-size: 28rpx;
          font-weight: 600;
          color: #2d3748;
          flex: 1;
          line-height: 1.3;
          word-break: break-all; // 允许在任何位置换行
          white-space: normal; // 允许换行
          display: -webkit-box;
          -webkit-line-clamp: 2; // 最多显示两行
          -webkit-box-orient: vertical;
          overflow: hidden;
          
          &.location-name-long {
            font-size: 24rpx;
          }
        }
      }
      
      // 第二行：温度 + 天气描述
      .temp-row {
        display: flex;
        align-items: baseline;
        gap: 16rpx;
        
        .temp-value {
          font-size: 48rpx;
          font-weight: 700;
          color: #2d3748;
          line-height: 1;
        }
        
        .weather-desc {
          font-size: 26rpx;
          color: #5a6c7d;
          font-weight: 500;
        }
      }
      
      // 第三行：提示
      .hint-row {
        .location-hint {
          font-size: 22rpx;
          color: #8b9aad;
        }
      }
      
      // 第四行：降雨提醒
      .rain-alert-row {
        margin-top: 4rpx;
        
        .rain-alert-text {
          font-size: 22rpx;
          color: #3182ce;
          font-weight: 500;
          display: flex;
          align-items: center;
          gap: 6rpx;
        }
      }
    }
    
    .weather-info {
      display: flex;
      align-items: baseline;
      gap: 8rpx;
      margin-bottom: 8rpx;
      
      .temp-value {
        font-size: 44rpx;
        font-weight: 700;
        color: #2d3748;
        line-height: 1;
      }
      
      .weather-desc {
        font-size: 24rpx;
        color: #5a6c7d;
      }
    }
    
    .city-name {
      font-size: 22rpx;
      color: #8b9aad;
    }
    
    .location-tip {
      font-size: 20rpx;
      color: #6B8DD6;
      margin-top: 8rpx;
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
      flex-wrap: nowrap;
      
      .section-icon {
        font-size: 36rpx;
        margin-right: 12rpx;
        line-height: 1;
      }
      
      .section-title {
        font-size: 32rpx;
        font-weight: 600;
        color: #2d3748;
        letter-spacing: -0.3rpx;
        line-height: 1;
        margin-top: 20rpx;
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
    
    .collapse-btn {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 56rpx;
      height: 56rpx;
      background: rgba(0, 0, 0, 0.04);
      border-radius: 50%;
      transition: all 0.2s ease;
      margin-left: 4rpx;
      
      &:active {
        background: rgba(0, 0, 0, 0.08);
        transform: scale(0.9);
      }
      
      .collapse-icon {
        font-size: 36rpx;
        color: #718096;
        line-height: 1;
        transform: rotate(0deg);
        transition: transform 0.3s ease;
        margin-top: -8rpx;
        
        &.collapsed {
          transform: rotate(-180deg);
          margin-top: 4rpx;
        }
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
      background: rgba(107, 141, 214, 0.08);
      border-radius: 50%;
      transition: all 0.2s ease;
      
      &:active {
        transform: scale(0.9);
        background: rgba(107, 141, 214, 0.15);
      }
      
      text {
        font-size: 36rpx;
        color: #6B8DD6;
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
        align-items: center;
        
        text {
          display: inline;
        }
        
        .people-display {
          display: flex;
          align-items: center;
          gap: 4rpx;
        }
        
        .person {
          display: flex;
          align-items: center;
          gap: 4rpx;
        }
        
        .person-avatar-small {
          width: 28rpx;
          height: 28rpx;
          border-radius: 50%;
          background: #E8F5E9;
        }
        
        .person-name-small {
          color: #6B8DD6;
          font-weight: 500;
        }
        
        .arrow-small {
          font-size: 20rpx;
          color: #999;
          margin: 0 4rpx;
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

/* 今日提醒样式 */
.reminder-card {
  margin-top: 24rpx;
}

.reminder-card-small {
  margin-top: 16rpx;
  background: #fff;
  border-radius: 28rpx;
  padding: 32rpx;
  // 去掉黄色背景和边框，和今日待办保持一致
}

.reminder-list {
  padding: 0 20rpx;
}

.reminder-item {
  display: flex;
  align-items: center;
  padding: 24rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
}

.reminder-item:last-child {
  border-bottom: none;
}

.reminder-icon-wrapper {
  width: 72rpx;
  height: 72rpx;
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20rpx;
  flex-shrink: 0;
}

.reminder-icon {
  font-size: 36rpx;
}

.reminder-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.reminder-title {
  font-size: 28rpx;
  color: #333;
  font-weight: 500;
}

.reminder-time {
  font-size: 24rpx;
  color: #999;
}

.reminder-tag {
  font-size: 22rpx;
  padding: 6rpx 16rpx;
  border-radius: 20rpx;
  background: #f0f4ff;
  color: #667eea;
  font-weight: 500;
}

.reminder-tag.daily {
  background: #e8f5e9;
  color: #07c160;
}

.reminder-tag.weekly {
  background: #fff3e0;
  color: #ff9800;
}

.reminder-tag.monthly {
  background: #fce4ec;
  color: #e91e63;
}

.reminder-tag.interval,
.reminder-tag.hourly {
  background: #f3e5f5;
  color: #9c27b0;
}

.reminder-tag.once {
  background: #e3f2fd;
  color: #2196f3;
}

.reminder-status {
  font-size: 24rpx;
  padding: 8rpx 16rpx;
  border-radius: 12rpx;
  background: #f5f5f5;
  color: #666;
}

.reminder-status.completed {
  background: #e8f5e9;
  color: #07c160;
}

/* 提醒弹窗样式 */
.modal-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40rpx;
}

.modal-content {
  width: 100%;
  max-width: 680rpx;
  max-height: 85vh;
  background: #fff;
  border-radius: 24rpx;
  display: flex;
  flex-direction: column;
  box-shadow: 0 20rpx 60rpx rgba(0, 0, 0, 0.3);
  animation: modalShow 0.3s ease;
}

@keyframes modalShow {
  from {
    opacity: 0;
    transform: scale(0.9);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 30rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.modal-header .modal-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #333;
}

.modal-header .modal-close {
  width: 60rpx;
  height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.modal-header .modal-close text {
  font-size: 36rpx;
  color: #999;
}

.modal-body {
  flex: 1;
  max-height: 65vh;
  padding: 20rpx 0;
}

.modal-footer {
  display: flex;
  gap: 20rpx;
  padding: 20rpx 30rpx;
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
  border-top: 1rpx solid #f0f0f0;
}

.modal-footer .cancel-btn,
.modal-footer .save-btn {
  flex: 1;
  height: 80rpx;
  border-radius: 40rpx;
  font-size: 30rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
}

.modal-footer .cancel-btn {
  background: #f5f5f5;
  color: #666;
}

.modal-footer .save-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
}

// 已选择用户显示
.selected-users {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx;
  background: #f0f7ff;
  border-radius: 12rpx;
}

.selected-users .selected-count {
  font-size: 28rpx;
  color: #667eea;
  font-weight: 500;
}

.selected-users .change-btn {
  font-size: 26rpx;
  color: #667eea;
  padding: 8rpx 20rpx;
  background: #fff;
  border-radius: 8rpx;
}

.loading-text {
  font-size: 28rpx;
  color: #999;
  padding: 20rpx;
  text-align: center;
}

// 用户选择弹窗样式
.user-picker-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  z-index: 2000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40rpx;
}

.user-picker-content {
  width: 100%;
  max-width: 600rpx;
  max-height: 70vh;
  background: #fff;
  border-radius: 24rpx;
  display: flex;
  flex-direction: column;
  box-shadow: 0 20rpx 60rpx rgba(0, 0, 0, 0.3);
}

.user-picker-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 30rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.user-picker-header .cancel,
.user-picker-header .confirm {
  font-size: 30rpx;
  color: #667eea;
  padding: 10rpx 20rpx;
}

.user-picker-header .title {
  font-size: 32rpx;
  font-weight: 600;
  color: #333;
}

.user-picker-body {
  flex: 1;
  max-height: 50vh;
}

.user-picker-body .user-item {
  display: flex;
  align-items: center;
  padding: 24rpx 30rpx;
  border-bottom: 1rpx solid #f5f5f5;
}

.user-picker-body .user-item.selected {
  background: #f0f4ff;
}

.user-picker-body .user-avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  margin-right: 24rpx;
  background: #e0e0e0;
}

.user-picker-body .user-name {
  flex: 1;
  font-size: 30rpx;
  color: #333;
}

.user-picker-body .check-icon {
  width: 48rpx;
  height: 48rpx;
  border-radius: 50%;
  background: #667eea;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
}

/* 表单样式 */
.form-section {
  padding: 0 30rpx 30rpx;
}

.form-section .section-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #333;
  padding: 20rpx 0;
}

.form-item {
  margin-bottom: 24rpx;
}

.form-item .form-label {
  display: block;
  font-size: 26rpx;
  color: #666;
  margin-bottom: 12rpx;
}

.form-item .form-label .required {
  color: #ff4d4f;
}

.form-item .form-input,
.form-item .form-textarea,
.form-item .picker {
  width: 100%;
  padding: 20rpx;
  background: #f5f6fa;
  border-radius: 12rpx;
  font-size: 28rpx;
  box-sizing: border-box;
}

.form-item .form-input {
  height: 88rpx;
  line-height: 48rpx;
}

.form-item .form-textarea {
  min-height: 160rpx;
}

.form-item .picker {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.form-item .picker text {
  color: #333;
}

.form-item .picker .picker-arrow {
  color: #999;
  font-size: 32rpx;
}

/* 间隔输入组 */
.interval-input-group {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 0 20rpx;
  background: #f5f6fa;
  border-radius: 12rpx;
  height: 88rpx;
}

.interval-input-group .interval-label {
  font-size: 28rpx;
  color: #666;
}

.interval-input-group .interval-input {
  flex: 1;
  height: 60rpx;
  background: transparent;
  border: none;
  font-size: 32rpx;
  color: #333;
  text-align: center;
}

.interval-input-group .interval-unit-picker {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 10rpx 16rpx;
  background: #fff;
  border-radius: 8rpx;
}

.interval-input-group .interval-unit-picker text {
  font-size: 28rpx;
  color: #667eea;
}

/* 星期选择器 */
.week-selector {
  display: flex;
  gap: 12rpx;
}

.week-selector .week-day {
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f6fa;
  border-radius: 50%;
  font-size: 24rpx;
  color: #666;
}

.week-selector .week-day.active {
  background: #667eea;
  color: #fff;
}

/* 单选按钮组 */
.radio-group {
  display: flex;
  gap: 20rpx;
}

.radio-group .radio-item {
  display: flex;
  align-items: center;
  gap: 12rpx;
  padding: 16rpx 24rpx;
  background: #f5f6fa;
  border-radius: 12rpx;
}

.radio-group .radio-item.active {
  background: #e8f0ff;
}

.radio-group .radio-item .radio-circle {
  width: 32rpx;
  height: 32rpx;
  border: 2rpx solid #ddd;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.radio-group .radio-item .radio-circle .radio-dot {
  width: 20rpx;
  height: 20rpx;
  background: #667eea;
  border-radius: 50%;
}

.radio-group .radio-item text {
  font-size: 26rpx;
  color: #333;
}

/* 开关项 */
.switch-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

/* ========== 场景化提醒样式 ========== */
.scene-reminders {
  background: linear-gradient(135deg, #fff 0%, #f8f9fc 100%);
}

.scene-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20rpx;
  padding: 20rpx 10rpx;
}

.scene-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20rpx 10rpx;
  border-radius: 20rpx;
  background: #f8f9fc;
  transition: all 0.3s ease;
  position: relative;
  animation: fadeInUp 0.5s ease forwards;
  opacity: 0;
  transform: translateY(20rpx);
}

.scene-item:active {
  transform: scale(0.95);
}

.scene-item.active {
  background: linear-gradient(135deg, #fff 0%, #f0f4ff 100%);
  box-shadow: 0 4rpx 16rpx rgba(102, 126, 234, 0.15);
}

.scene-item.active .scene-name-text {
  color: #667eea;
  font-weight: 600;
}

@keyframes fadeInUp {
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.scene-icon-box {
  width: 88rpx;
  height: 88rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
}

.scene-icon-emoji {
  font-size: 44rpx;
}

.scene-name-text {
  font-size: 24rpx;
  color: #666;
  text-align: center;
  transition: all 0.3s ease;
}

.scene-status-dot {
  position: absolute;
  top: 12rpx;
  right: 12rpx;
  width: 16rpx;
  height: 16rpx;
  border-radius: 50%;
  background: #ddd;
  border: 2rpx solid #fff;
  transition: all 0.3s ease;
}

.scene-status-dot.on {
  background: #52c41a;
  box-shadow: 0 0 8rpx rgba(82, 196, 26, 0.5);
}

.more-btn .arrow {
  margin-left: 4rpx;
  font-size: 24rpx;
}
</style>
