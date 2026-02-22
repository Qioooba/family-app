<template>
  <view class="lunar-page">
    <!-- 顶部导航 -->
    <view class="nav-bar">
      <view class="back-btn" @click="goBack">
        <u-icon name="arrow-left" size="40" color="#333"></u-icon>
      </view>
      <text class="title">农历日历</text>
      <view class="right-btn" @click="showDatePicker">
        <text>{{ currentYear }}年</text>
        <u-icon name="arrow-down" size="24" color="#333"></u-icon>
      </view>
    </view>

    <view class="content">
      <!-- 今日概览卡片 -->
      <view class="today-card">
        <view class="today-solar"
>
          <text class="solar-date">{{ today.solarDate }}</text>
          <text class="solar-week">{{ today.weekDay }}</text>
        </view>

        <view class="today-lunar"
>
          <view class="lunar-main"
>
            <text class="lunar-month">{{ today.lunarMonth }}</text>
            <text class="lunar-day">{{ today.lunarDay }}</text>
          </view>          
          
          <view class="lunar-info">
            <text class="ganzhi">{{ today.ganZhiYear }}年 {{ today.ganZhiMonth }}月 {{ today.ganZhiDay }}日</text>
            <text class="zodiac">{{ today.zodiac }}</text>
          </view>
        </view>

        <view v-if="today.festival" class="today-festival">
          <u-icon name="gift" size="32" color="#FF6B6B"></u-icon>
          <text>{{ today.festival }}</text>
        </view>

        <view class="yi-ji-section">
          <view class="yi-ji-item yi"
>
            <text class="yi-ji-label">宜</text>
            <view class="yi-ji-content">
              <text 
                v-for="(yi, index) in today.yi" 
                :key="index"
                class="yi-ji-tag"
              >
                {{ yi }}
              </text>
            </view>
          </view>
          
          <view class="yi-ji-item ji"
>
            <text class="yi-ji-label">忌</text>
            <view class="yi-ji-content">
              <text 
                v-for="(ji, index) in today.ji" 
                :key="index"
                class="yi-ji-tag"
              >
                {{ ji }}
              </text>
            </view>
          </view>
        </view>
      </view>

      <!-- 月份日历 -->
      <view class="calendar-section"
>
        <view class="calendar-header"
>
          <view class="month-nav"
>
            <text class="nav-btn" @click="prevMonth">‹</text>
            <text class="current-month">{{ currentYear }}年{{ currentMonth }}月</text>
            <text class="nav-btn" @click="nextMonth">›</text>
          </view>
          
          <view class="week-header"
>
            <text v-for="day in weekDays" :key="day" class="week-day">{{ day }}</text>
          </view>
        </view>

        <view class="calendar-grid"
>
          <view
            v-for="(day, index) in calendarDays"
            :key="index"
            class="day-cell"
            :class="{
              'other-month': !day.isCurrentMonth,
              'is-today': day.isToday,
              'is-festival': day.festival,
              'is-selected': selectedDate === day.date
            }"
            @click="selectDay(day)"
          >
            <text class="solar-text">{{ day.solarDay }}</text>
            <text class="lunar-text" :class="{ 'is-festival-text': day.festival }">
              {{ day.lunarText }}
            </text>
            
            <view v-if="day.festival" class="festival-dot"></view>
          </view>
        </view>
      </view>

      <!-- 节日列表 -->
      <view class="festivals-section"
>
        <view class="section-header">
          <text class="section-title">本月节日</text>
          <text class="view-all" @click="viewAllFestivals">查看全部 ›</text>
        </view>

        <view class="festival-list"
>
          <view
            v-for="festival in monthFestivals"
            :key="festival.name"
            class="festival-card"
          >
            <view class="festival-date"
>
              <text class="date-day">{{ festival.day }}</text>
              <text class="date-month"
{{ festival.month }}月</text>
            </view>            
            
            <view class="festival-info"
>
              <text class="festival-name">{{ festival.name }}</text>
              <text class="festival-type">{{ festival.type }}</text>
              <text class="days-left">{{ festival.daysLeft }}</text>
            </view>
            
            <view class="festival-icon"
>
              <text>{{ festival.icon }}</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 今日详情弹窗 -->
      <u-popup
        v-model:show="dayDetailVisible"
        mode="bottom"
        round
        closeable
      >
        <view v-if="selectedDay" class="day-detail"
>
          <view class="detail-header"
>
            <view class="detail-solar"
>
              <text class="detail-date">{{ selectedDay.solarDate }}</text>
              <text class="detail-week">{{ selectedDay.weekDay }}</text>
            </view>
            
            <view class="detail-lunar"
>
              <text class="lunar-title">{{ selectedDay.lunarMonth }}{{ selectedDay.lunarDay }}</text>
              <text class="ganzhi-text">{{ selectedDay.ganZhiYear }} {{ selectedDay.ganZhiMonth }} {{ selectedDay.ganZhiDay }}</text>
            </view>
          </view>

          <view v-if="selectedDay.festival" class="detail-festival"
>
            <u-icon name="gift" size="40" color="#FF6B6B"></u-icon>
            <text>{{ selectedDay.festival }}</text>
          </view>

          <view class="detail-yiji"
>
            <view class="yiji-row yi"
>
              <text class="yiji-label">宜</text>
              <view class="yiji-tags"
>
                <text v-for="(yi, i) in selectedDay.yi" :key="i" class="tag">{{ yi }}</text>
              </view>
            </view>
            
            <view class="yiji-row ji"
>
              <text class="yiji-label">忌</text>
              <view class="yiji-tags"
>
                <text v-for="(ji, i) in selectedDay.ji" :key="i" class="tag">{{ ji }}</text>
              </view>
            </view>
          </view>

          <view class="detail-actions"
>
            <view class="action-btn" @click="addReminder">
              <u-icon name="bell" size="32" color="#5B8FF9"></u-icon>
              <text>添加提醒</text>
            </view>
            
            <view class="action-btn" @click="shareDate"
>
              <u-icon name="share" size="32" color="#52C41A"></u-icon>
              <text>分享</text>
            </view>
          </view>
        </view>
      </u-popup>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import dayjs from 'dayjs'

// 农历数据
const lunarMonths = ['正', '二', '三', '四', '五', '六', '七', '八', '九', '十', '冬', '腊']
const lunarDays = [
  '初一', '初二', '初三', '初四', '初五', '初六', '初七', '初八', '初九', '初十',
  '十一', '十二', '十三', '十四', '十五', '十六', '十七', '十八', '十九', '二十',
  '廿一', '廿二', '廿三', '廿四', '廿五', '廿六', '廿七', '廿八', '廿九', '三十'
]
const gan = ['甲', '乙', '丙', '丁', '戊', '己', '庚', '辛', '壬', '癸']
const zhi = ['子', '丑', '寅', '卯', '辰', '巳', '午', '未', '申', '酉', '戌', '亥']
const animals = ['鼠', '牛', '虎', '兔', '龙', '蛇', '马', '羊', '猴', '鸡', '狗', '猪']
const weekDays = ['日', '一', '二', '三', '四', '五', '六']

// 响应式数据
const currentYear = ref(dayjs().year())
const currentMonth = ref(dayjs().month() + 1)
const selectedDate = ref(dayjs().format('YYYY-MM-DD'))
const selectedDay = ref(null)
const dayDetailVisible = ref(false)

// 宜忌数据（模拟）
const yiJiData = {
  yi: ['出行', '搬家', '结婚', '理发', '签订合同', '交易', '开业', '动土'],
  ji: ['诉讼', '打猎', '栽种', '安葬', '开仓', '破土', '行丧']
}

// 节日数据
const festivals = [
  { name: '春节', month: 1, day: 1, type: '传统节日', icon: '🧧' },
  { name: '元宵节', month: 1, day: 15, type: '传统节日', icon: '🏮' },
  { name: '清明节', month: 4, day: 4, type: '传统节日', icon: '🌿' },
  { name: '劳动节', month: 5, day: 1, type: '法定假日', icon: '🛠️' },
  { name: '端午节', month: 5, day: 5, type: '传统节日', icon: '🐲' },
  { name: '中秋节', month: 8, day: 15, type: '传统节日', icon: '🥮' },
  { name: '国庆节', month: 10, day: 1, type: '法定假日', icon: '🇨🇳' },
  { name: '重阳节', month: 9, day: 9, type: '传统节日', icon: '🌼' },
  { name: '七夕节', month: 7, day: 7, type: '传统节日', icon: '💕' },
  { name: '腊八节', month: 12, day: 8, type: '传统节日', icon: '🥣' }
]

// 今日数据
const today = computed(() => {
  const now = dayjs()
  return getDayInfo(now)
})

// 日历天数
const calendarDays = computed(() => {
  const days = []
  const firstDay = dayjs(`${currentYear.value}-${currentMonth.value}-1`)
  const startOfMonth = firstDay.startOf('month')
  const endOfMonth = firstDay.endOf('month')
  
  // 填充上个月的日期
  const startDayOfWeek = startOfMonth.day()
  for (let i = startDayOfWeek - 1; i >= 0; i--) {
    const date = startOfMonth.subtract(i + 1, 'day')
    days.push(createDayData(date, false))
  }
  
  // 当月日期
  for (let i = 0; i < endOfMonth.date(); i++) {
    const date = startOfMonth.add(i, 'day')
    days.push(createDayData(date, true))
  }
  
  // 填充下个月的日期
  const endDayOfWeek = endOfMonth.day()
  for (let i = 1; i < 7 - endDayOfWeek; i++) {
    const date = endOfMonth.add(i, 'day')
    days.push(createDayData(date, false))
  }
  
  return days
})

// 本月节日
const monthFestivals = computed(() => {
  const now = dayjs()
  return festivals
    .filter(f => f.month === currentMonth.value)
    .map(f => {
      const festivalDate = dayjs(`${currentYear.value}-${f.month}-${f.day}`)
      const daysLeft = festivalDate.diff(now, 'day')
      return {
        ...f,
        daysLeft: daysLeft >= 0 ? `${daysLeft}天后` : '已过'
      }
    })
    .sort((a, b) => a.day - b.day)
})

// 创建日期数据
function createDayData(date, isCurrentMonth) {
  const dateStr = date.format('YYYY-MM-DD')
  const isToday = date.isSame(dayjs(), 'day')
  
  // 模拟农历数据
  const lunarDate = getLunarDate(date)
  
  // 检查节日
  const festival = festivals.find(f => 
    f.month === date.month() + 1 && f.day === date.date()
  )
  
  return {
    date: dateStr,
    solarDay: date.date(),
    solarDate: date.format('YYYY年M月D日'),
    weekDay: `星期${weekDays[date.day()]}`,
    lunarText: festival ? festival.name : lunarDate.dayName,
    lunarMonth: lunarDate.monthName,
    lunarDay: lunarDate.dayName,
    ganZhiYear: lunarDate.ganZhiYear,
    ganZhiMonth: lunarDate.ganZhiMonth,
    ganZhiDay: lunarDate.ganZhiDay,
    zodiac: lunarDate.zodiac,
    festival: festival?.name,
    isCurrentMonth,
    isToday,
    yi: getRandomYiJi(yiJiData.yi, 3),
    ji: getRandomYiJi(yiJiData.ji, 2)
  }
}

// 获取农历日期（简化版）
function getLunarDate(date) {
  // 这里使用简化的算法，实际项目中应该使用完整的农历转换库
  const year = date.year()
  const month = date.month()
  const day = date.date()
  
  // 简单的偏移计算（仅为演示）
  const lunarOffset = Math.floor((year - 1900) * 365.25) % 360
  const lunarMonthIndex = (month + Math.floor(day / 15)) % 12
  const lunarDayIndex = (day + lunarOffset) % 30
  
  // 计算干支
  const yearGanIndex = (year - 4) % 10
  const yearZhiIndex = (year - 4) % 12
  const dayGanIndex = (lunarOffset + day) % 10
  const dayZhiIndex = (lunarOffset + day) % 12
  
  return {
    monthName: lunarMonths[lunarMonthIndex] + '月',
    dayName: lunarDays[lunarDayIndex] || '初一',
    ganZhiYear: gan[yearGanIndex] + zhi[yearZhiIndex],
    ganZhiMonth: gan[(yearGanIndex + month) % 10] + zhi[(yearZhiIndex + month) % 12],
    ganZhiDay: gan[dayGanIndex] + zhi[dayZhiIndex],
    zodiac: animals[yearZhiIndex] + '年'
  }
}

// 获取某天信息
function getDayInfo(date) {
  return createDayData(date, true)
}

// 随机获取宜忌
function getRandomYiJi(arr, count) {
  const shuffled = [...arr].sort(() => 0.5 - Math.random())
  return shuffled.slice(0, count)
}

// 方法
const prevMonth = () => {
  const date = dayjs(`${currentYear.value}-${currentMonth.value}-1`).subtract(1, 'month')
  currentYear.value = date.year()
  currentMonth.value = date.month() + 1
}

const nextMonth = () => {
  const date = dayjs(`${currentYear.value}-${currentMonth.value}-1`).add(1, 'month')
  currentYear.value = date.year()
  currentMonth.value = date.month() + 1
}

const selectDay = (day) => {
  selectedDate.value = day.date
  selectedDay.value = day
  dayDetailVisible.value = true
}

const showDatePicker = () => {
  uni.showActionSheet({
    itemList: ['2024年', '2025年', '2026年'],
    success: (res) => {
      currentYear.value = 2024 + res.tapIndex
    }
  })
}

const viewAllFestivals = () => {
  uni.showModal({
    title: '全年节日',
    content: festivals.map(f => `${f.month}月${f.day}日 ${f.name}`).join('\n'),
    showCancel: false
  })
}

const addReminder = () => {
  uni.showToast({ title: '提醒功能开发中', icon: 'none' })
}

const shareDate = () => {
  uni.showShareMenu({
    withShareTicket: true
  })
}

const goBack = () => {
  uni.navigateBack()
}

onMounted(() => {
  // 页面加载
})
</script>

<style lang="scss" scoped>
.lunar-page {
  min-height: 100vh;
  background: #f5f6fa;
}

.nav-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 30rpx;
  padding-top: 60rpx;
  background: linear-gradient(135deg, #8B4513 0%, #D2691E 100%);

  .back-btn {
    width: 60rpx;
    height: 60rpx;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .title {
    font-size: 34rpx;
    font-weight: 600;
    color: #fff;
  }

  .right-btn {
    display: flex;
    align-items: center;
    gap: 8rpx;

    text {
      font-size: 26rpx;
      color: #fff;
    }
  }
}

.content {
  padding: 0 30rpx 50rpx;
}

// 今日卡片
.today-card {
  background: linear-gradient(135deg, #FFF8DC 0%, #FFE4B5 100%);
  border-radius: 24rpx;
  padding: 40rpx;
  margin-top: -40rpx;
  position: relative;
  z-index: 1;
  box-shadow: 0 8rpx 32rpx rgba(139, 69, 19, 0.15);
}

.today-solar {
  text-align: center;
  margin-bottom: 20rpx;

  .solar-date {
    font-size: 36rpx;
    font-weight: 600;
    color: #8B4513;
    display: block;
  }

  .solar-week {
    font-size: 26rpx;
    color: #A0522D;
    margin-top: 8rpx;
    display: block;
  }
}

.today-lunar {
  text-align: center;
  padding: 30rpx 0;
  border-top: 2rpx dashed #DEB887;
  border-bottom: 2rpx dashed #DEB887;
  margin-bottom: 20rpx;

  .lunar-main {
    margin-bottom: 16rpx;

    .lunar-month {
      font-size: 32rpx;
      color: #8B4513;
      margin-right: 16rpx;
    }

    .lunar-day {
      font-size: 64rpx;
      font-weight: 700;
      color: #8B4513;
    }
  }

  .lunar-info {
    .ganzhi {
      font-size: 26rpx;
      color: #A0522D;
      display: block;
    }

    .zodiac {
      font-size: 26rpx;
      color: #A0522D;
      margin-top: 8rpx;
      display: block;
    }
  }
}

.today-festival {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  padding: 16rpx;
  background: rgba(255, 107, 107, 0.1);
  border-radius: 12rpx;
  margin-bottom: 20rpx;

  text {
    font-size: 28rpx;
    color: #FF6B6B;
    font-weight: 500;
  }
}

.yi-ji-section {
  display: flex;
  gap: 20rpx;

  .yi-ji-item {
    flex: 1;
    display: flex;
    align-items: flex-start;
    gap: 12rpx;

    .yi-ji-label {
      width: 48rpx;
      height: 48rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 50%;
      font-size: 26rpx;
      font-weight: 600;
      flex-shrink: 0;
    }

    &.yi {
      .yi-ji-label {
        background: #52C41A;
        color: #fff;
      }
    }

    &.ji {
      .yi-ji-label {
        background: #FF4D4F;
        color: #fff;
      }
    }

    .yi-ji-content {
      display: flex;
      flex-wrap: wrap;
      gap: 8rpx;
      flex: 1;

      .yi-ji-tag {
        padding: 8rpx 16rpx;
        background: rgba(255, 255, 255, 0.6);
        border-radius: 8rpx;
        font-size: 24rpx;
        color: #8B4513;
      }
    }
  }
}

// 日历区域
.calendar-section {
  background: #fff;
  border-radius: 24rpx;
  padding: 30rpx;
  margin-top: 30rpx;
}

.calendar-header {
  margin-bottom: 20rpx;

  .month-nav {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20rpx;

    .nav-btn {
      width: 60rpx;
      height: 60rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 40rpx;
      color: #8B4513;
    }

    .current-month {
      font-size: 32rpx;
      font-weight: 600;
      color: #333;
    }
  }

  .week-header {
    display: flex;

    .week-day {
      flex: 1;
      text-align: center;
      font-size: 26rpx;
      color: #999;
      padding: 16rpx 0;
    }
  }
}

.calendar-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 8rpx;
}

.day-cell {
  aspect-ratio: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border-radius: 12rpx;
  position: relative;
  padding: 8rpx;

  .solar-text {
    font-size: 28rpx;
    color: #333;
    font-weight: 500;
  }

  .lunar-text {
    font-size: 20rpx;
    color: #999;
    margin-top: 4rpx;

    &.is-festival-text {
      color: #FF6B6B;
    }
  }

  .festival-dot {
    position: absolute;
    top: 4rpx;
    right: 4rpx;
    width: 12rpx;
    height: 12rpx;
    background: #FF6B6B;
    border-radius: 50%;
  }

  &.other-month {
    opacity: 0.4;
  }

  &.is-today {
    background: #FFF8DC;

    .solar-text {
      color: #8B4513;
      font-weight: 700;
    }
  }

  &.is-selected {
    background: #8B4513;

    .solar-text, .lunar-text {
      color: #fff;
    }
  }
}

// 节日区域
.festivals-section {
  margin-top: 30rpx;

  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20rpx;

    .section-title {
      font-size: 30rpx;
      font-weight: 600;
      color: #333;
    }

    .view-all {
      font-size: 26rpx;
      color: #8B4513;
    }
  }
}

.festival-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.festival-card {
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;

  .festival-date {
    display: flex;
    flex-direction: column;
    align-items: center;
    width: 100rpx;
    margin-right: 20rpx;

    .date-day {
      font-size: 40rpx;
      font-weight: 700;
      color: #8B4513;
    }

    .date-month {
      font-size: 22rpx;
      color: #999;
    }
  }

  .festival-info {
    flex: 1;

    .festival-name {
      font-size: 30rpx;
      font-weight: 600;
      color: #333;
      display: block;
      margin-bottom: 4rpx;
    }

    .festival-type {
      font-size: 22rpx;
      color: #999;
    }

    .days-left {
      font-size: 24rpx;
      color: #FF6B6B;
      margin-top: 4rpx;
      display: block;
    }
  }

  .festival-icon {
    font-size: 48rpx;
  }
}

// 详情弹窗
.day-detail {
  padding: 30rpx;

  .detail-header {
    text-align: center;
    margin-bottom: 30rpx;

    .detail-solar {
      margin-bottom: 16rpx;

      .detail-date {
        font-size: 32rpx;
        font-weight: 600;
        color: #333;
        display: block;
      }

      .detail-week {
        font-size: 26rpx;
        color: #999;
        margin-top: 8rpx;
        display: block;
      }
    }

    .detail-lunar {
      .lunar-title {
        font-size: 48rpx;
        font-weight: 700;
        color: #8B4513;
        display: block;
        margin-bottom: 8rpx;
      }

      .ganzhi-text {
        font-size: 26rpx;
        color: #A0522D;
      }
    }
  }

  .detail-festival {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 16rpx;
    padding: 24rpx;
    background: linear-gradient(135deg, #FFF8DC 0%, #FFE4B5 100%);
    border-radius: 16rpx;
    margin-bottom: 30rpx;

    text {
      font-size: 32rpx;
      font-weight: 600;
      color: #FF6B6B;
    }
  }

  .detail-yiji {
    margin-bottom: 30rpx;

    .yiji-row {
      display: flex;
      align-items: flex-start;
      gap: 16rpx;
      margin-bottom: 20rpx;

      .yiji-label {
        width: 56rpx;
        height: 56rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        border-radius: 50%;
        font-size: 28rpx;
        font-weight: 600;
        color: #fff;
        flex-shrink: 0;
      }

      &.yi .yiji-label {
        background: #52C41A;
      }

      &.ji .yiji-label {
        background: #FF4D4F;
      }

      .yiji-tags {
        display: flex;
        flex-wrap: wrap;
        gap: 12rpx;
        flex: 1;

        .tag {
          padding: 10rpx 20rpx;
          background: #f5f6fa;
          border-radius: 8rpx;
          font-size: 26rpx;
          color: #333;
        }
      }
    }
  }

  .detail-actions {
    display: flex;
    gap: 20rpx;

    .action-btn {
      flex: 1;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 12rpx;
      padding: 24rpx;
      background: #f5f6fa;
      border-radius: 12rpx;

      text {
        font-size: 28rpx;
        color: #333;
      }
    }
  }
}
</style>