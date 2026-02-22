<template>
  <view class="repeat-setting-page">
    <!-- 顶部导航 -->
    <view class="nav-bar">
      <view class="back-btn" @click="goBack">
        <u-icon name="arrow-left" size="40" color="#333"></u-icon>
      </view>
      <text class="title">重复设置</text>
      <view class="right-btn" @click="confirm">
        <text>确定</text>
      </view>
    </view>

    <view class="content">
      <!-- 重复类型选择器 - 每天/每周/每月 -->
      <view class="section">
        <text class="section-title">重复频率</text>
        <view class="repeat-type-tabs">
          <view
            v-for="(type, index) in repeatTypes" :key="type.id || index"
            class="type-tab"
            :class="{ active: repeatType === type.value }"
            @click="selectRepeatType(type.value)"
          >
            <u-icon :name="type.icon" size="36" :color="repeatType === type.value ? '#fff' : '#666'"></u-icon>
            <text class="tab-text">{{ type.label }}</text>
          </view>
        </view>
      </view>

      <!-- 每天设置 -->
      <view v-if="repeatType === 'daily'" class="section">
        <text class="section-title">每天设置</text>
        <view class="setting-card">
          <view class="setting-row">
            <text class="row-label">每</text>
            <view class="number-picker">
              <text class="minus" @click="decreaseInterval">-</text>
              <input 
                v-model.number="dailyInterval" 
                type="number" 
                class="number-input"
                @blur="validateInterval"
              />
              <text class="plus" @click="increaseInterval">+</text>
            </view>
            <text class="row-label">天重复</text>
          </view>
          <text class="setting-desc">每 {{ dailyInterval }} 天执行一次任务</text>
        </view>
      </view>

      <!-- 每周设置 -->
      <view v-if="repeatType === 'weekly'" class="section">
        <text class="section-title">每周设置</text>
        <view class="setting-card">
          <view class="weekday-selector">
            <view
              v-for="(day, index) in weekDays"
              :key="index"
              class="weekday-item"
              :class="{ 
                active: selectedWeekdays.includes(index),
                weekend: index >= 5
              }"
              @click="toggleWeekday(index)"
            >
              <text class="day-name">{{ day.short }}</text>
              <text class="day-full">{{ day.full }}</text>
            </view>
          </view>
          <view class="quick-actions">
            <text class="quick-btn" @click="selectAllWeekdays">全选</text>
            <text class="quick-btn" @click="selectWeekdays">工作日</text>
            <text class="quick-btn" @click="selectWeekend">周末</text>
            <text class="quick-btn" @click="clearWeekdays">清空</text>
          </view>
        </view>
        <view class="setting-card" style="margin-top: 20rpx;">
          <view class="setting-row">
            <text class="row-label">每</text>
            <view class="number-picker">
              <text class="minus" @click="decreaseWeekInterval">-</text>
              <input 
                v-model.number="weekInterval" 
                type="number" 
                class="number-input"
                @blur="validateWeekInterval"
              />
              <text class="plus" @click="increaseWeekInterval">+</text>
            </view>
            <text class="row-label">周重复</text>
          </view>
        </view>
      </view>

      <!-- 每月设置 -->
      <view v-if="repeatType === 'monthly'" class="section">
        <text class="section-title">每月设置</text>
        <view class="setting-card">
          <view class="month-option-tabs">
            <view
              class="month-option"
              :class="{ active: monthlyType === 'date' }"
              @click="monthlyType = 'date'"
            >
              <text>按日期</text>
            </view>
            <view
              class="month-option"
              :class="{ active: monthlyType === 'week' }"
              @click="monthlyType = 'week'"
            >
              <text>按星期</text>
            </view>
          </view>

          <!-- 按日期选择 -->
          <view v-if="monthlyType === 'date'" class="date-selector">
            <view class="date-grid">
              <view
                v-for="(date, index) in 31" :key="index"
                class="date-item"
                :class="{ active: selectedDates.includes(date) }"
                @click="toggleDate(date)"
              >
                {{ date }}
              </view>
              <view
                class="date-item last-day"
                :class="{ active: selectedDates.includes(0) }"
                @click="toggleDate(0)"
              >
                最后一天
              </view>
            </view>
            <view class="quick-actions">
              <text class="quick-btn" @click="selectAllDates">全选</text>
              <text class="quick-btn" @click="clearDates">清空</text>
              <text class="quick-btn" @click="selectLastDay">最后一天</text>
            </view>
          </view>

          <!-- 按星期选择 -->
          <view v-if="monthlyType === 'week'" class="week-selector">
            <view class="week-row">
              <text class="week-label">第</text>
              <view class="week-number-list">
                <view
                  v-for="(n, index) in 5" :key="index"
                  class="week-number"
                  :class="{ active: selectedWeekNumbers.includes(n) }"
                  @click="toggleWeekNumber(n)"
                >
                  {{ n === 5 ? '最后' : '第' + n }}
                </view>
              </view>
              <text class="week-label">个</text>
            </view>
            <view class="weekday-selector">
              <view
                v-for="(day, index) in weekDays"
                :key="index"
                class="weekday-item small"
                :class="{ active: selectedMonthWeekday === index }"
                @click="selectedMonthWeekday = index"
              >
                <text class="day-name">{{ day.short }}</text>
              </view>
            </view>
          </view>
        </view>
        <view class="setting-card" style="margin-top: 20rpx;">
          <view class="setting-row">
            <text class="row-label">每</text>
            <view class="number-picker">
              <text class="minus" @click="decreaseMonthInterval">-</text>
              <input 
                v-model.number="monthInterval" 
                type="number" 
                class="number-input"
                @blur="validateMonthInterval"
              />
              <text class="plus" @click="increaseMonthInterval">+</text>
            </view>
            <text class="row-label">月重复</text>
          </view>
        </view>
      </view>

      <!-- 重复结束设置 -->
      <view class="section">
        <text class="section-title">结束方式</text>
        <view class="end-options">
          <view
            v-for="(option, index) in endOptions" :key="option.id || index"
            class="end-option-card"
            :class="{ active: endType === option.value }"
            @click="selectEndType(option.value)"
          >
            <view class="option-radio">
              <view class="radio-inner" v-if="endType === option.value"></view>
            </view>
            <view class="option-content">
              <text class="option-title">{{ option.label }}</text>
              <text class="option-desc" v-if="option.desc">{{ option.desc }}</text>
            </view>
          </view>

          <!-- 结束日期选择 -->
          <view v-if="endType === 'date'" class="end-date-picker">
            <picker mode="date" :value="endDate" @change="onEndDateChange">
              <view class="picker-content">
                <text class="picker-label">结束日期</text>
                <text class="picker-value">{{ endDate || '请选择日期' }}</text>
                <u-icon name="arrow-right" size="28" color="#999"></u-icon>
              </view>
            </picker>
          </view>

          <!-- 重复次数输入 -->
          <view v-if="endType === 'count'" class="end-count-input">
            <text class="input-label">重复次数</text>
            <view class="count-control">
              <text class="minus" @click="decreaseCount">-</text>
              <input 
                v-model.number="repeatCount" 
                type="number" 
                class="count-field"
                placeholder="输入次数"
              />
              <text class="plus" @click="increaseCount">+</text>
            </view>
            <text class="count-unit">次</text>
          </view>
        </view>
      </view>

      <!-- 预览区域 -->
      <view class="section">
        <text class="section-title">设置预览</text>
        <view class="preview-card">
          <view class="preview-item">
            <text class="preview-label">重复规则</text>
            <text class="preview-value">{{ repeatSummary }}</text>
          </view>
          <view class="preview-item">
            <text class="preview-label">下次执行</text>
            <text class="preview-value highlight">{{ nextExecutionDate }}</text>
          </view>
          <view class="preview-item" v-if="endType !== 'never'">
            <text class="preview-label">结束时间</text>
            <text class="preview-value">{{ endSummary }}</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import dayjs from 'dayjs'

// 重复类型
const repeatTypes = [
  { label: '每天', value: 'daily', icon: 'calendar' },
  { label: '每周', value: 'weekly', icon: 'list' },
  { label: '每月', value: 'monthly', icon: 'grid' }
]

// 星期数据
const weekDays = [
  { short: '一', full: '周一' },
  { short: '二', full: '周二' },
  { short: '三', full: '周三' },
  { short: '四', full: '周四' },
  { short: '五', full: '周五' },
  { short: '六', full: '周六' },
  { short: '日', full: '周日' }
]

// 结束选项
const endOptions = [
  { label: '永不结束', value: 'never', desc: '任务将一直重复' },
  { label: '结束日期', value: 'date', desc: '到达指定日期后停止' },
  { label: '重复次数', value: 'count', desc: '完成指定次数后停止' }
]

// 响应式数据
const repeatType = ref('daily')
const dailyInterval = ref(1)
const weekInterval = ref(1)
const monthInterval = ref(1)
const selectedWeekdays = ref([])
const monthlyType = ref('date')
const selectedDates = ref([])
const selectedWeekNumbers = ref([])
const selectedMonthWeekday = ref(0)
const endType = ref('never')
const endDate = ref('')
const repeatCount = ref(10)

// 从页面参数获取初始值
onMounted(() => {
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const options = currentPage.$page?.options || currentPage.options || {}
  
  if (options.repeatType) {
    repeatType.value = options.repeatType
  }
  if (options.repeatRule) {
    try {
      const rule = JSON.parse(decodeURIComponent(options.repeatRule))
      loadRepeatRule(rule)
    } catch (e) {
      console.error('解析重复规则失败', e)
    }
  }
})

// 加载重复规则
const loadRepeatRule = (rule) => {
  if (rule.interval) {
    if (repeatType.value === 'daily') dailyInterval.value = rule.interval
    if (repeatType.value === 'weekly') weekInterval.value = rule.interval
    if (repeatType.value === 'monthly') monthInterval.value = rule.interval
  }
  if (rule.weekdays) {
    selectedWeekdays.value = rule.weekdays
  }
  if (rule.dates) {
    selectedDates.value = rule.dates
  }
  if (rule.endDate) {
    endType.value = 'date'
    endDate.value = rule.endDate
  }
  if (rule.endCount) {
    endType.value = 'count'
    repeatCount.value = rule.endCount
  }
}

// 选择重复类型
const selectRepeatType = (type) => {
  repeatType.value = type
}

// 每天设置方法
const decreaseInterval = () => {
  if (dailyInterval.value > 1) dailyInterval.value--
}
const increaseInterval = () => {
  dailyInterval.value++
}
const validateInterval = () => {
  if (dailyInterval.value < 1) dailyInterval.value = 1
}

// 每周设置方法
const toggleWeekday = (index) => {
  const i = selectedWeekdays.value.indexOf(index)
  if (i > -1) {
    selectedWeekdays.value.splice(i, 1)
  } else {
    selectedWeekdays.value.push(index)
    selectedWeekdays.value.sort()
  }
}
const selectAllWeekdays = () => {
  selectedWeekdays.value = [0, 1, 2, 3, 4, 5, 6]
}
const selectWeekdays = () => {
  selectedWeekdays.value = [0, 1, 2, 3, 4]
}
const selectWeekend = () => {
  selectedWeekdays.value = [5, 6]
}
const clearWeekdays = () => {
  selectedWeekdays.value = []
}
const decreaseWeekInterval = () => {
  if (weekInterval.value > 1) weekInterval.value--
}
const increaseWeekInterval = () => {
  weekInterval.value++
}
const validateWeekInterval = () => {
  if (weekInterval.value < 1) weekInterval.value = 1
}

// 每月设置方法
const toggleDate = (date) => {
  const i = selectedDates.value.indexOf(date)
  if (i > -1) {
    selectedDates.value.splice(i, 1)
  } else {
    selectedDates.value.push(date)
    selectedDates.value.sort((a, b) => a - b)
  }
}
const selectAllDates = () => {
  selectedDates.value = Array.from({ length: 31 }, (_, i) => i + 1)
}
const clearDates = () => {
  selectedDates.value = []
}
const selectLastDay = () => {
  if (!selectedDates.value.includes(0)) {
    selectedDates.value.push(0)
  }
}
const toggleWeekNumber = (n) => {
  const i = selectedWeekNumbers.value.indexOf(n)
  if (i > -1) {
    selectedWeekNumbers.value.splice(i, 1)
  } else {
    selectedWeekNumbers.value.push(n)
    selectedWeekNumbers.value.sort()
  }
}
const decreaseMonthInterval = () => {
  if (monthInterval.value > 1) monthInterval.value--
}
const increaseMonthInterval = () => {
  monthInterval.value++
}
const validateMonthInterval = () => {
  if (monthInterval.value < 1) monthInterval.value = 1
}

// 结束方式设置
const selectEndType = (type) => {
  endType.value = type
}
const onEndDateChange = (e) => {
  endDate.value = e.detail.value
}
const decreaseCount = () => {
  if (repeatCount.value > 1) repeatCount.value--
}
const increaseCount = () => {
  repeatCount.value++
}

// 计算属性 - 重复规则摘要
const repeatSummary = computed(() => {
  switch (repeatType.value) {
    case 'daily':
      return dailyInterval.value === 1 ? '每天' : `每 ${dailyInterval.value} 天`
    case 'weekly':
      if (selectedWeekdays.value.length === 0) return '请选择星期'
      const days = selectedWeekdays.value.map(i => weekDays[i].short).join('、')
      return weekInterval.value === 1 ? `每周 ${days}` : `每 ${weekInterval.value} 周的 ${days}`
    case 'monthly':
      if (monthlyType.value === 'date') {
        if (selectedDates.value.length === 0) return '请选择日期'
        const dates = selectedDates.value.map(d => d === 0 ? '最后一天' : `${d}日`).join('、')
        return monthInterval.value === 1 ? `每月 ${dates}` : `每 ${monthInterval.value} 个月的 ${dates}`
      } else {
        const weekNums = selectedWeekNumbers.value.map(n => n === 5 ? '最后' : `第${n}`).join('、')
        const weekday = weekDays[selectedMonthWeekday.value].full
        return monthInterval.value === 1 
          ? `每月 ${weekNums} 个 ${weekday}` 
          : `每 ${monthInterval.value} 个月的 ${weekNums} 个 ${weekday}`
      }
    default:
      return ''
  }
})

// 计算属性 - 下次执行日期
const nextExecutionDate = computed(() => {
  const today = dayjs()
  
  switch (repeatType.value) {
    case 'daily':
      return today.add(dailyInterval.value, 'day').format('YYYY-MM-DD')
    case 'weekly':
      if (selectedWeekdays.value.length === 0) return '-'
      const todayWeekday = today.day() === 0 ? 6 : today.day() - 1
      let nextDay = selectedWeekdays.value.find(d => d > todayWeekday)
      if (nextDay === undefined) {
        nextDay = selectedWeekdays.value[0]
        return today.add(7 * weekInterval.value - todayWeekday + nextDay, 'day').format('YYYY-MM-DD')
      }
      return today.add(nextDay - todayWeekday, 'day').format('YYYY-MM-DD')
    case 'monthly':
      if (monthlyType.value === 'date' && selectedDates.value.length === 0) return '-'
      const currentDate = today.date()
      if (monthlyType.value === 'date') {
        let nextDate = selectedDates.value.find(d => d > currentDate && d !== 0)
        if (nextDate === undefined) {
          nextDate = selectedDates.value.find(d => d !== 0)
          return today.add(monthInterval.value, 'month').date(nextDate).format('YYYY-MM-DD')
        }
        return today.date(nextDate).format('YYYY-MM-DD')
      }
      return today.add(monthInterval.value, 'month').format('YYYY-MM-DD')
    default:
      return '-'
  }
})

// 计算属性 - 结束摘要
const endSummary = computed(() => {
  switch (endType.value) {
    case 'date':
      return endDate.value || '未设置'
    case 'count':
      return `${repeatCount.value} 次后结束`
    default:
      return '永不结束'
  }
})

// 构建重复规则对象
const buildRepeatRule = () => {
  const rule = {
    type: repeatType.value
  }
  
  switch (repeatType.value) {
    case 'daily':
      rule.interval = dailyInterval.value
      break
    case 'weekly':
      rule.interval = weekInterval.value
      rule.weekdays = selectedWeekdays.value
      break
    case 'monthly':
      rule.interval = monthInterval.value
      rule.monthlyType = monthlyType.value
      if (monthlyType.value === 'date') {
        rule.dates = selectedDates.value
      } else {
        rule.weekNumbers = selectedWeekNumbers.value
        rule.weekday = selectedMonthWeekday.value
      }
      break
  }
  
  rule.endType = endType.value
  if (endType.value === 'date') {
    rule.endDate = endDate.value
  } else if (endType.value === 'count') {
    rule.endCount = repeatCount.value
  }
  
  return rule
}

// 验证设置
const validate = () => {
  if (repeatType.value === 'weekly' && selectedWeekdays.value.length === 0) {
    uni.showToast({ title: '请至少选择一天', icon: 'none' })
    return false
  }
  if (repeatType.value === 'monthly' && monthlyType.value === 'date' && selectedDates.value.length === 0) {
    uni.showToast({ title: '请至少选择一天', icon: 'none' })
    return false
  }
  if (repeatType.value === 'monthly' && monthlyType.value === 'week' && selectedWeekNumbers.value.length === 0) {
    uni.showToast({ title: '请至少选择一个星期', icon: 'none' })
    return false
  }
  if (endType.value === 'date' && !endDate.value) {
    uni.showToast({ title: '请选择结束日期', icon: 'none' })
    return false
  }
  return true
}

// 确认并返回
const confirm = () => {
  if (!validate()) return
  
  const rule = buildRepeatRule()
  const eventChannel = getOpenerEventChannel()
  
  if (eventChannel && eventChannel.emit) {
    eventChannel.emit('onRepeatSet', rule)
  }
  
  // 同时存储到本地，便于跨页面使用
  uni.setStorageSync('tempRepeatRule', JSON.stringify(rule))
  
  uni.navigateBack()
}

const goBack = () => {
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
.repeat-setting-page {
  min-height: 100vh;
  background: #f5f6fa;
}

.nav-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 30rpx;
  padding-top: 60rpx;
  background: #fff;
  border-bottom: 1rpx solid #f0f0f0;

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
    color: #333;
  }

  .right-btn {
    padding: 12rpx 30rpx;
    background: #5B8FF9;
    border-radius: 30rpx;

    text {
      font-size: 28rpx;
      color: #fff;
    }
  }
}

.content {
  padding: 30rpx;
}

.section {
  margin-bottom: 40rpx;
}

.section-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #333;
  margin-bottom: 20rpx;
  display: block;
}

// 重复类型选项卡
.repeat-type-tabs {
  display: flex;
  gap: 20rpx;
}

.type-tab {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 30rpx 20rpx;
  background: #fff;
  border-radius: 16rpx;
  border: 2rpx solid transparent;
  transition: all 0.3s;

  .tab-text {
    font-size: 28rpx;
    color: #666;
    margin-top: 12rpx;
  }

  &.active {
    background: #5B8FF9;
    border-color: #5B8FF9;

    .tab-text {
      color: #fff;
    }
  }
}

// 设置卡片
.setting-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
}

.setting-row {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 20rpx;

  .row-label {
    font-size: 30rpx;
    color: #333;
  }
}

.setting-desc {
  font-size: 24rpx;
  color: #999;
  text-align: center;
  margin-top: 16rpx;
  display: block;
}

// 数字选择器
.number-picker {
  display: flex;
  align-items: center;
  background: #f5f6fa;
  border-radius: 12rpx;
  overflow: hidden;

  .minus, .plus {
    width: 60rpx;
    height: 60rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 32rpx;
    color: #5B8FF9;
    font-weight: bold;
  }

  .number-input {
    width: 80rpx;
    height: 60rpx;
    text-align: center;
    font-size: 32rpx;
    color: #333;
    font-weight: 600;
    background: transparent;
  }
}

// 星期选择器
.weekday-selector {
  display: flex;
  gap: 16rpx;
  justify-content: center;

  .weekday-item {
    width: 80rpx;
    height: 100rpx;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    background: #f5f6fa;
    border-radius: 12rpx;
    transition: all 0.3s;

    .day-name {
      font-size: 32rpx;
      font-weight: 600;
      color: #333;
    }

    .day-full {
      font-size: 20rpx;
      color: #999;
      margin-top: 4rpx;
    }

    &.weekend {
      .day-name {
        color: #FF6B6B;
      }
    }

    &.active {
      background: #5B8FF9;

      .day-name, .day-full {
        color: #fff;
      }
    }

    &.small {
      width: 70rpx;
      height: 80rpx;
    }
  }
}

// 快捷操作
.quick-actions {
  display: flex;
  gap: 20rpx;
  margin-top: 24rpx;
  justify-content: center;

  .quick-btn {
    padding: 12rpx 24rpx;
    background: #f0f5ff;
    border-radius: 24rpx;
    font-size: 24rpx;
    color: #5B8FF9;
  }
}

// 每月选项卡
.month-option-tabs {
  display: flex;
  gap: 20rpx;
  margin-bottom: 24rpx;
}

.month-option {
  flex: 1;
  padding: 20rpx;
  background: #f5f6fa;
  border-radius: 12rpx;
  text-align: center;
  transition: all 0.3s;

  text {
    font-size: 28rpx;
    color: #666;
  }

  &.active {
    background: #5B8FF9;

    text {
      color: #fff;
    }
  }
}

// 日期选择网格
.date-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 16rpx;
  margin-bottom: 20rpx;

  .date-item {
    aspect-ratio: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #f5f6fa;
    border-radius: 12rpx;
    font-size: 26rpx;
    color: #333;
    transition: all 0.3s;

    &.last-day {
      grid-column: span 2;
      aspect-ratio: auto;
      font-size: 22rpx;
    }

    &.active {
      background: #5B8FF9;
      color: #fff;
    }
  }
}

// 星期选择器
.week-row {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16rpx;
  margin-bottom: 20rpx;

  .week-label {
    font-size: 28rpx;
    color: #333;
  }
}

.week-number-list {
  display: flex;
  gap: 12rpx;

  .week-number {
    padding: 12rpx 20rpx;
    background: #f5f6fa;
    border-radius: 8rpx;
    font-size: 24rpx;
    color: #666;

    &.active {
      background: #5B8FF9;
      color: #fff;
    }
  }
}

// 结束选项
.end-options {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.end-option-card {
  display: flex;
  align-items: center;
  gap: 20rpx;
  padding: 24rpx 30rpx;
  background: #fff;
  border-radius: 16rpx;
  border: 2rpx solid transparent;

  .option-radio {
    width: 36rpx;
    height: 36rpx;
    border-radius: 50%;
    border: 4rpx solid #ddd;
    display: flex;
    align-items: center;
    justify-content: center;

    .radio-inner {
      width: 18rpx;
      height: 18rpx;
      border-radius: 50%;
      background: #5B8FF9;
    }
  }

  .option-content {
    flex: 1;

    .option-title {
      font-size: 30rpx;
      color: #333;
      display: block;
    }

    .option-desc {
      font-size: 24rpx;
      color: #999;
      margin-top: 4rpx;
      display: block;
    }
  }

  &.active {
    border-color: #5B8FF9;
    background: #f0f5ff;

    .option-radio {
      border-color: #5B8FF9;
    }
  }
}

// 结束日期选择
.end-date-picker {
  background: #fff;
  border-radius: 16rpx;
  margin-top: -10rpx;
  padding: 20rpx 30rpx;

  .picker-content {
    display: flex;
    align-items: center;
    justify-content: space-between;

    .picker-label {
      font-size: 28rpx;
      color: #333;
    }

    .picker-value {
      font-size: 28rpx;
      color: #5B8FF9;
    }
  }
}

// 重复次数输入
.end-count-input {
  display: flex;
  align-items: center;
  gap: 20rpx;
  background: #fff;
  border-radius: 16rpx;
  margin-top: -10rpx;
  padding: 20rpx 30rpx;

  .input-label {
    font-size: 28rpx;
    color: #333;
  }

  .count-control {
    display: flex;
    align-items: center;
    background: #f5f6fa;
    border-radius: 12rpx;
    overflow: hidden;

    .minus, .plus {
      width: 60rpx;
      height: 60rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 32rpx;
      color: #5B8FF9;
      font-weight: bold;
    }

    .count-field {
      width: 100rpx;
      height: 60rpx;
      text-align: center;
      font-size: 30rpx;
      color: #333;
      background: transparent;
    }
  }

  .count-unit {
    font-size: 28rpx;
    color: #666;
  }
}

// 预览卡片
.preview-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 30rpx;

  .preview-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16rpx 0;
    border-bottom: 1rpx solid #f5f5f5;

    &:last-child {
      border-bottom: none;
    }

    .preview-label {
      font-size: 28rpx;
      color: #666;
    }

    .preview-value {
      font-size: 28rpx;
      color: #333;
      font-weight: 500;

      &.highlight {
        color: #5B8FF9;
      }
    }
  }
}
</style>