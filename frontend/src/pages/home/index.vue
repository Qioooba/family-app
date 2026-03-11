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
            <text class="location-name">{{ weatherData.locationName }}</text>
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
              <text class="assignee">{{ getTaskPeople(task) }}</text>
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
    
    <!-- 任务弹窗组件 -->
    <TaskModal ref="taskModalRef" :members="familyMembers" @success="onTaskSaved" />
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
import { getAvatarUrl } from '../../utils/request'
import LazyImage from '@/components/common/LazyImage.vue'
import TaskModal from '@/components/TaskModal.vue'

const userStore = useUserStore()
const taskModalRef = ref(null)

// ========== 任务弹窗操作 ==========
const goAddTask = () => {
  console.log('点击添加按钮', taskModalRef.value)
  // 添加延迟确保组件已挂载
  setTimeout(async () => {
    if (!taskModalRef.value) {
      console.error('TaskModal 组件未加载')
      uni.showToast({ title: '组件未加载，请重试', icon: 'none' })
      return
    }
    console.log('准备打开弹窗')
    try {
      await taskModalRef.value.open()
      console.log('弹窗打开成功')
    } catch (err) {
      console.error('弹窗打开失败:', err)
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
      console.error('加载家庭成员失败', e)
      familyMembers.value = []
    }
    
    // 使用专门的今日任务API，后端已按今天日期筛选
    const res = await taskApi.getTodayTasks(familyId)
    const todayTodoTasks = res || []
    
    // 格式化并显示所有今日待办
    if (todayTodoTasks.length > 0) {
      todayTasks.value = todayTodoTasks.map(task => ({
        ...task,
        assigneeName: task.assigneeNickname || getMemberName(task.assigneeId) || '家人',
        creatorName: task.creatorNickname || getMemberName(task.creatorId) || '家人',
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

// 登录状态判断
const isLoggedIn = computed(() => {
  return !!userStore.userInfo?.id && !!userStore.token
})

const currentFamily = ref({ name: '幸福小家' })

// 快捷功能 - 添加阴影
const quickActions = [
  { name: '纪念日', icon: 'heart', bgColor: '#FF6B6B', shadow: '0 8rpx 20rpx rgba(255, 107, 107, 0.35)', path: '/pages/anniversary/index' },
  { name: '心愿', icon: 'star', bgColor: '#FFD700', shadow: '0 8rpx 20rpx rgba(255, 215, 0, 0.35)', path: '/pages/wish/index' },
  { name: '天气', icon: 'sun', bgColor: '#4facfe', shadow: '0 8rpx 20rpx rgba(79, 172, 254, 0.35)', path: '/pages/weather/index' }
]

// 今日任务
const todayTasks = ref([])

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
    console.error('加载喝水数据失败', e)
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
      // 判断强度
      let intensity = '小'
      if (precipitation >= 10 || precipitationProbability >= 70) {
        intensity = '暴'
      } else if (precipitation >= 5 || precipitationProbability >= 50) {
        intensity = '大'
      } else if (precipitation >= 2 || precipitationProbability >= 30) {
        intensity = '中'
      }
      
      alerts.push({
        hoursLater: hoursDiff,
        type: isStorm ? '雷暴' : (isSnow ? '雪' : '雨'),
        intensity: intensity,
        weatherCode: weatherCode
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
      console.log('检查位置授权失败:', e)
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
      console.log('获取位置失败:', e)
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
              console.log('获取逐小时预报失败:', err)
              resolve(null)
            }
          })
        })
      ])
      weatherJson = weatherRes
      hourlyData = hourlyRes
    } catch (e) {
      console.log('获取天气API失败:', e)
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
    console.error('加载天气失败:', error)
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
        console.error('获取位置失败:', err)
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
      }
      
      // 第一行：图标 + 位置
      .location-row {
        display: flex;
        align-items: center;
        gap: 12rpx;
        
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
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
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

</style>