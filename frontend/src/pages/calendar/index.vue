<template>
  <view class="page-container">
    <view class="header">
      <view class="header-title">纪念日 📅</view>
      <view class="header-actions">
        <view class="lunar-toggle" @click="showLunar = !showLunar">
          <text :class="{ active: showLunar }">农历{{ showLunar ? '开' : '关' }}</text>
        </view>
        <view class="header-action" @click="showAddModal">
          <text class="icon">+</text>
        </view>
      </view>
    </view>
    
    <!-- 节日横幅 -->
    <view v-if="upcomingFestivals.length > 0" class="festival-banner">
      <scroll-view scroll-x class="festival-scroll">
        <view 
          v-for="(festival, index) in upcomingFestivals" 
          :key="index"
          class="festival-tag"
          :class="{ imminent: festival.days <= 7 }"
        >
          <text class="festival-icon">{{ festival.icon }}</text>
          <text class="festival-name">{{ festival.name }}</text>
          <text class="festival-countdown">{{ festival.days === 0 ? '今天' : festival.days + '天后' }}</text>
        </view>
      </scroll-view>
    </view>
    
    <view class="calendar-section">
      <view class="month-header">
        <text class="month-text">{{ currentYear }}年{{ currentMonth }}月</text>
        <text v-if="showLunar" class="lunar-month">{{ currentLunarMonth }}</text>
      </view>
      
      <view class="weekdays">
        <text v-for="day in weekdays" :key="day">{{ day }}</text>
      </view>
      
      <view class="days-grid">
        <view 
          v-for="(day, index) in daysInMonth" 
          :key="index"
          class="day-cell"
          :class="{ 
            today: isToday(day), 
            hasEvent: hasEvent(day),
            festival: isFestival(day)
          }"
          @click="selectDay(day)"
        >
          <text class="solar">{{ day }}</text>
          <text v-if="showLunar" class="lunar">{{ getLunarDay(day) }}</text>
          <view v-if="hasEvent(day)" class="event-dot"></text>
          <view v-if="isFestival(day)" class="festival-dot">🎉</view>
        </view>
      </view>
    </view>
    
    <view class="events-section">
      <view class="section-header">
        <text class="section-title">即将到来的纪念日</text>
        <view class="filter-tabs">
          <text 
            v-for="tab in filterTabs" 
            :key="tab.value"
            class="tab"
            :class="{ active: currentFilter === tab.value }"
            @click="currentFilter = tab.value"
          >{{ tab.label }}</text>
        </view>
      </view>
      
      <scroll-view class="events-list" scroll-y>
        <view 
          v-for="(event, index) in filteredEvents" 
          :key="index"
          class="event-card"
          :class="{ lunar: event.isLunar }"
          @click="viewEventDetail(event)"
        >
          <view class="event-icon">{{ event.icon }}</view>
          
          <view class="event-info">
            <view class="event-title">
              {{ event.title }}
              <text v-if="event.isLunar" class="lunar-badge">农历</text>
            </view>
            <view class="event-date">
              {{ event.date }}
              <text v-if="event.lunarDate" class="lunar-text">{{ event.lunarDate }}</text>
            </view>
          </view>
          
          <view class="event-countdown" :class="{ urgent: event.days <= 3 }"
          >
            <text class="days">{{ event.days }}</text>
            <text class="label">天后</text>
          </view>
        </view>
        
        <view v-if="filteredEvents.length === 0" class="empty-state"
3e
          <text class="empty-icon">🎊</text>
          <text class="empty-text">暂无纪念日</text>
        </view>
      </scroll-view>
    </view>
    
    <!-- 添加纪念日弹窗 -->
    <view v-if="showAddModalFlag" class="modal-overlay" @click="showAddModalFlag = false"
    >
      <view class="modal-content" @click.stop>
        <view class="modal-header"
        >
          <text class="modal-title">添加纪念日</text>
          <view class="close-btn" @click="showAddModalFlag = false">×</view>
        </view>
        
        <view class="modal-body"
        >
          <!-- 日期类型选择 -->
          <view class="date-type-tabs"
          >
            <view 
              class="tab" 
              :class="{ active: newEvent.isLunar === false }"
              @click="newEvent.isLunar = false"
            >
              <text>📅 公历</text>
            </view>
            <view 
              class="tab" 
              :class="{ active: newEvent.isLunar === true }"
              @click="newEvent.isLunar = true"
            >
              <text>🌙 农历</text>
            </view>
          </view>
          
          <!-- 名称输入 -->
          <view class="form-item"
          >
            <text class="label">纪念日名称</text>
            <input 
              v-model="newEvent.title"
              class="input"
              placeholder="例如：妈妈生日"
            />
          </view>
          
          <!-- 公历日期选择 -->
          <view v-if="!newEvent.isLunar" class="form-item"
          >
            <text class="label">选择日期</text>
            <picker mode="date" :value="newEvent.date" @change="onDateChange"
            >
              <view class="picker">{{ newEvent.date || '请选择日期' }}</view>
            </picker>
          </view>
          
          <!-- 农历日期选择 -->
          <view v-else class="form-item lunar-picker"
          >
            <text class="label">选择农历日期</text>
            <view class="lunar-selects"
            >
              <picker mode="selector" :range="lunarMonths" :value="lunarMonthIndex" @change="onLunarMonthChange"
              >
                <view class="picker">{{ lunarMonths[lunarMonthIndex] }}</view>
              </picker>
              <picker mode="selector" :range="lunarDays" :value="lunarDayIndex" @change="onLunarDayChange"
              >
                <view class="picker">{{ lunarDays[lunarDayIndex] }}</view>
              </picker>
            </view>
          </view>
          
          <!-- 图标选择 -->
          <view class="form-item"
          >
            <text class="label">选择图标</text>
            <scroll-view scroll-x class="icon-list"
            >
              <text 
                v-for="icon in eventIcons" 
                :key="icon"
                class="icon-option"
                :class="{ selected: newEvent.icon === icon }"
                @click="newEvent.icon = icon"
              >{{ icon }}</text>
            </scroll-view>
          </view>
          
          <!-- 重复提醒 -->
          <view class="form-item"
          >
            <text class="label">重复提醒</text>
            <view class="remind-options"
            >
              <view 
                v-for="opt in remindOptions" 
                :key="opt.value"
                class="remind-option"
                :class="{ active: newEvent.remindDays === opt.value }"
                @click="newEvent.remindDays = opt.value"
              >{{ opt.label }}</view>
            </view>
          </view>
          
          <view class="submit-btn" @click="submitEvent"
          >确认添加</view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { anniversaryApi } from '../../api/index.js'
import { solarToLunar, lunarToSolar, getLunarDateString, getSolarDateString, isToday as isLunarToday } from '../../utils/lunar.js'

const currentYear = ref(new Date().getFullYear())
const currentMonth = ref(new Date().getMonth() + 1)
const weekdays = ['日', '一', '二', '三', '四', '五', '六']
const events = ref([])
const loading = ref(false)
const showLunar = ref(true)
const currentFilter = ref('all')
const showAddModalFlag = ref(false)

// 农历月份和日期选择
const lunarMonths = ['正月', '二月', '三月', '四月', '五月', '六月', '七月', '八月', '九月', '十月', '冬月', '腊月']
const lunarDays = ['初一', '初二', '初三', '初四', '初五', '初六', '初七', '初八', '初九', '初十',
  '十一', '十二', '十三', '十四', '十五', '十六', '十七', '十八', '十九', '二十',
  '廿一', '廿二', '廿三', '廿四', '廿五', '廿六', '廿七', '廿八', '廿九', '三十']
const lunarMonthIndex = ref(0)
const lunarDayIndex = ref(0)

// 新增事件表单
const newEvent = ref({
  title: '',
  date: '',
  isLunar: false,
  icon: '🎂',
  remindDays: 7
})

// 事件图标选项
const eventIcons = ['🎂', '💑', '💍', '🎓', '🏠', '🚗', '📅', '🎄', '🧧', '👶', '🎉', '💐']

// 提醒选项
const remindOptions = [
  { label: '当天', value: 0 },
  { label: '提前1天', value: 1 },
  { label: '提前3天', value: 3 },
  { label: '提前7天', value: 7 },
  { label: '提前30天', value: 30 }
]

// 过滤选项
const filterTabs = [
  { label: '全部', value: 'all' },
  { label: '公历', value: 'solar' },
  { label: '农历', value: 'lunar' }
]

// 中国传统节日数据（农历）
const lunarFestivals = {
  '正月初一': { name: '春节', icon: '🧧' },
  '正月十五': { name: '元宵节', icon: '🏮' },
  '五月初五': { name: '端午节', icon: '🐲' },
  '七月初七': { name: '七夕节', icon: '💕' },
  '七月十五': { name: '中元节', icon: '🕯️' },
  '八月十五': { name: '中秋节', icon: '🥮' },
  '九月初九': { name: '重阳节', icon: '🌼' },
  '腊月初八': { name: '腊八节', icon: '🥣' },
  '腊月廿三': { name: '小年', icon: '🧹' },
  '腊月廿四': { name: '小年', icon: '🧹' },
  '腊月三十': { name: '除夕', icon: '🎊' }
}

// 公历节日
const solarFestivals = {
  '01-01': { name: '元旦', icon: '🎆' },
  '02-14': { name: '情人节', icon: '🌹' },
  '03-08': { name: '妇女节', icon: '👩' },
  '03-12': { name: '植树节', icon: '🌳' },
  '04-01': { name: '愚人节', icon: '🤡' },
  '05-01': { name: '劳动节', icon: '👷' },
  '05-04': { name: '青年节', icon: '👦' },
  '06-01': { name: '儿童节', icon: '🧒' },
  '07-01': { name: '建党节', icon: '🇨🇳' },
  '08-01': { name: '建军节', icon: '🎖️' },
  '09-10': { name: '教师节', icon: '👨‍🏫' },
  '10-01': { name: '国庆节', icon: '🇨🇳' },
  '10-24': { name: '程序员节', icon: '💻' },
  '11-11': { name: '双十一', icon: '🛍️' },
  '12-24': { name: '平安夜', icon: '🍎' },
  '12-25': { name: '圣诞节', icon: '🎄' }
}

// 当前农历月份显示
const currentLunarMonth = computed(() => {
  const today = new Date()
  const lunar = solarToLunar(currentYear.value, currentMonth.value, today.getDate())
  return lunar ? `${lunar.year}年${lunar.month}` : ''
})

// 即将到来的节日
const upcomingFestivals = computed(() => {
  const festivals = []
  const today = new Date()

  // 检查未来30天的节日
  for (let i = 0; i < 30; i++) {
    const date = new Date(today)
    date.setDate(today.getDate() + i)
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    const dateKey = `${month}-${day}`

    // 检查公历节日
    if (solarFestivals[dateKey]) {
      festivals.push({
        ...solarFestivals[dateKey],
        days: i,
        type: 'solar'
      })
    }

    // 检查农历节日
    const lunar = solarToLunar(date.getFullYear(), date.getMonth() + 1, date.getDate())
    if (lunar) {
      const lunarKey = `${lunar.month}${lunar.day}`
      if (lunarFestivals[lunarKey]) {
        festivals.push({
          ...lunarFestivals[lunarKey],
          days: i,
          type: 'lunar'
        })
      }
    }
  }

  return festivals.slice(0, 5)
})

// 过滤后的纪念日
const filteredEvents = computed(() => {
  let list = events.value.map(event => {
    let eventDate
    let displayDate
    let lunarDate = ''

    if (event.isLunar) {
      // 农历日期需要转换为当年公历
      const [month, day] = parseLunarDate(event.date)
      const solar = lunarToSolar(currentYear.value, month, day)
      if (solar) {
        eventDate = new Date(solar.year, solar.month - 1, solar.day)
        displayDate = `${solar.year}-${String(solar.month).padStart(2, '0')}-${String(solar.day).padStart(2, '0')}`
        lunarDate = event.date
      }
    } else {
      eventDate = new Date(event.date)
      displayDate = event.date
      // 转换为农历显示
      const [year, month, day] = event.date.split('-').map(Number)
      const lunar = solarToLunar(year, month, day)
      if (lunar) {
        lunarDate = `${lunar.month}${lunar.day}`
      }
    }

    const today = new Date()
    today.setHours(0, 0, 0, 0)
    if (eventDate < today) {
      // 如果已过，算下一年的
      eventDate.setFullYear(eventDate.getFullYear() + 1)
      const [year, month, day] = displayDate.split('-').map(Number)
      displayDate = `${year + 1}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')}`
    }

    const diffTime = eventDate - today
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))

    return {
      ...event,
      icon: event.icon || '📅',
      displayDate,
      lunarDate,
      days: diffDays > 0 ? diffDays : 0
    }
  }).filter(e => e.days >= 0)

  // 按天数排序
  list.sort((a, b) => a.days - b.days)

  // 应用过滤器
  if (currentFilter.value === 'solar') {
    list = list.filter(e => !e.isLunar)
  } else if (currentFilter.value === 'lunar') {
    list = list.filter(e => e.isLunar)
  }

  return list
})

// 加载纪念日列表
const loadAnniversaries = async () => {
  loading.value = true
  try {
    const familyId = uni.getStorageSync('currentFamilyId') || 1
    const res = await anniversaryApi.getList(familyId)
    events.value = res || []
  } catch (e) {
    console.error('加载纪念日失败', e)
  } finally {
    loading.value = false
  }
}

// 页面加载时获取数据
onMounted(() => {
  loadAnniversaries()
})

// 计算当月天数
const daysInMonth = computed(() => {
  const days = new Date(currentYear.value, currentMonth.value, 0).getDate()
  return Array.from({ length: days }, (_, i) => i + 1)
})

const isToday = (day) => {
  const today = new Date()
  return day === today.getDate() &&
    currentMonth.value === today.getMonth() + 1 &&
    currentYear.value === today.getFullYear()
}

const hasEvent = (day) => {
  return events.value.some(e => {
    if (e.isLunar) return false // 农历需要特殊处理
    const date = new Date(e.date)
    return date.getDate() === day &&
      date.getMonth() + 1 === currentMonth.value &&
      date.getFullYear() === currentYear.value
  })
}

// 判断是否是节日
const isFestival = (day) => {
  // 检查公历节日
  const month = String(currentMonth.value).padStart(2, '0')
  const dayStr = String(day).padStart(2, '0')
  if (solarFestivals[`${month}-${dayStr}`]) return true

  // 检查农历节日
  const lunar = solarToLunar(currentYear.value, currentMonth.value, day)
  if (lunar) {
    const lunarKey = `${lunar.month}${lunar.day}`
    return !!lunarFestivals[lunarKey]
  }
  return false
}

// 获取农历日期显示
const getLunarDay = (day) => {
  const lunar = solarToLunar(currentYear.value, currentMonth.value, day)
  if (!lunar) return ''

  // 如果是节日，显示节日名
  const lunarKey = `${lunar.month}${lunar.day}`
  if (lunarFestivals[lunarKey]) {
    return lunarFestivals[lunarKey].name
  }

  // 否则显示农历日期
  return lunar.day === '初一' ? lunar.month : lunar.day
}

// 解析农历日期字符串
const parseLunarDate = (dateStr) => {
  // 格式：正月初一、腊月三十等
  for (let i = 0; i < lunarMonths.length; i++) {
    if (dateStr.includes(lunarMonths[i])) {
      for (let j = 0; j < lunarDays.length; j++) {
        if (dateStr.includes(lunarDays[j])) {
          return [i + 1, j + 1]
        }
      }
    }
  }
  return [1, 1]
}

// 显示添加弹窗
const showAddModal = () => {
  newEvent.value = {
    title: '',
    date: '',
    isLunar: false,
    icon: '🎂',
    remindDays: 7
  }
  lunarMonthIndex.value = 0
  lunarDayIndex.value = 0
  showAddModalFlag.value = true
}

// 日期变更
const onDateChange = (e) => {
  newEvent.value.date = e.detail.value
}

// 农历月份变更
const onLunarMonthChange = (e) => {
  lunarMonthIndex.value = e.detail.value
  updateLunarDate()
}

// 农历日期变更
const onLunarDayChange = (e) => {
  lunarDayIndex.value = e.detail.value
  updateLunarDate()
}

// 更新农历日期
const updateLunarDate = () => {
  newEvent.value.date = `${lunarMonths[lunarMonthIndex.value]}${lunarDays[lunarDayIndex.value]}`
}

// 提交新纪念日
const submitEvent = async () => {
  if (!newEvent.value.title) {
    uni.showToast({ title: '请输入纪念日名称', icon: 'none' })
    return
  }

  if (!newEvent.value.date) {
    uni.showToast({ title: '请选择日期', icon: 'none' })
    return
  }

  try {
    const familyId = uni.getStorageSync('currentFamilyId') || 1

    // 如果是农历，先转换为当年公历存储
    let storeDate = newEvent.value.date
    if (newEvent.value.isLunar) {
      const [month, day] = parseLunarDate(newEvent.value.date)
      const solar = lunarToSolar(new Date().getFullYear(), month, day)
      if (solar) {
        storeDate = `${solar.year}-${String(solar.month).padStart(2, '0')}-${String(solar.day).padStart(2, '0')}`
      }
    }

    await anniversaryApi.create({
      title: newEvent.value.title,
      familyId,
      date: storeDate,
      isLunar: newEvent.value.isLunar,
      icon: newEvent.value.icon,
      remindDays: newEvent.value.remindDays
    })

    uni.showToast({ title: '添加成功', icon: 'success' })
    showAddModalFlag.value = false
    loadAnniversaries()
  } catch (e) {
    console.error('添加失败', e)
    uni.showToast({ title: '添加失败', icon: 'none' })
  }
}

// 查看事件详情
const viewEventDetail = (event) => {
  uni.showModal({
    title: event.title,
    content: `${event.displayDate}\n${event.isLunar ? `农历：${event.date}\n` : ''}提醒：提前${event.remindDays || 7}天`,
    showCancel: true,
    confirmText: '删除',
    cancelText: '关闭',
    success: async (res) => {
      if (res.confirm) {
        try {
          await anniversaryApi.delete(event.id)
          uni.showToast({ title: '已删除', icon: 'success' })
          loadAnniversaries()
        } catch (e) {
          uni.showToast({ title: '删除失败', icon: 'none' })
        }
      }
    }
  })
}

// 选择日期
const selectDay = (day) => {
  const dateStr = `${currentYear.value}-${String(currentMonth.value).padStart(2, '0')}-${String(day).padStart(2, '0')}`

  // 检查是否是节日
  const lunar = solarToLunar(currentYear.value, currentMonth.value, day)
  let festivalName = ''
  if (lunar) {
    const lunarKey = `${lunar.month}${lunar.day}`
    if (lunarFestivals[lunarKey]) {
      festivalName = lunarFestivals[lunarKey].name
    }
  }
  const month = String(currentMonth.value).padStart(2, '0')
  const dayStr = String(day).padStart(2, '0')
  if (solarFestivals[`${month}-${dayStr}`]) {
    festivalName = solarFestivals[`${month}-${dayStr}`].name
  }

  const event = events.value.find(e => {
    if (e.isLunar) return false
    return e.date === dateStr
  })

  if (event) {
    viewEventDetail(event)
  } else if (festivalName) {
    uni.showToast({ title: `${day}日 ${festivalName}`, icon: 'none' })
  } else {
    uni.showToast({ title: `${day}日 无纪念日`, icon: 'none' })
  }
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
  background: linear-gradient(135deg, #F3E5F5, #E1BEE7);
  
  .header-title {
    font-size: 24px;
    font-weight: 700;
    color: #2C3E50;
  }
  
  .header-action {
    width: 44px;
    height: 44px;
    background: #9C27B0;
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

.calendar-section {
  background: #fff;
  padding: 20px;
  
  .month-header {
    text-align: center;
    margin-bottom: 20px;
    
    .month-text {
      font-size: 18px;
      font-weight: 600;
      color: #2C3E50;
    }
  }
  
  .weekdays {
    display: flex;
    justify-content: space-around;
    margin-bottom: 10px;
    
    text {
      font-size: 14px;
      color: #7F8C8D;
    }
  }
  
  .days-grid {
    display: grid;
    grid-template-columns: repeat(7, 1fr);
    gap: 8px;
    
    .day-cell {
      aspect-ratio: 1;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      border-radius: 10px;
      font-size: 14px;
      position: relative;
      
      &.today {
        background: #9C27B0;
        color: #fff;
      }
      
      .event-dot {
        width: 6px;
        height: 6px;
        background: #F44336;
        border-radius: 50%;
        position: absolute;
        bottom: 4px;
      }
    }
  }
}

.events-section {
  padding: 20px;
  
  .section-title {
    font-size: 16px;
    font-weight: 600;
    color: #2C3E50;
    margin-bottom: 15px;
  }
}

.events-list {
  height: calc(100vh - 500px);
}

.event-card {
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 16px;
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  
  .event-icon {
    font-size: 36px;
    margin-right: 16px;
  }
  
  .event-info {
    flex: 1;
    
    .event-title {
      font-size: 16px;
      font-weight: 600;
      color: #2C3E50;
      margin-bottom: 4px;
    }
    
    .event-date {
      font-size: 13px;
      color: #7F8C8D;
    }
  }
  
  .event-countdown {
    text-align: center;
    padding: 8px 16px;
    background: #F3E5F5;
    border-radius: 12px;
    
    .days {
      display: block;
      font-size: 24px;
      font-weight: 700;
      color: #9C27B0;
    }
    
    .label {
      font-size: 11px;
      color: #9C27B0;
    }
  }
}
</style>
