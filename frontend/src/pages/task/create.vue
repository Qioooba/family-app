<template>
  <view class="create-task-page">
    <!-- 顶部导航 -->
    <view class="nav-bar">
      <view class="back-btn" @click="goBack">
        <up-icon name="arrow-left" size="40" color="#333"></up-icon>
      </view>
      <text class="title">{{ pageTitle }}</text>
      <view class="right-btn" @click="saveTask">
        <text>保存</text>
      </view>
    </view>

    <view class="form-container">
      <!-- 任务标题 -->
      <view class="form-item">
        <text class="label">任务标题 <text class="required">*</text></text>
        <view class="input-wrapper title-input-wrapper">
          <input
            v-model="form.title"
            placeholder="请输入任务标题，如：买牛奶"
            class="input task-title-input"
            placeholder-class="input-placeholder"
          />
        </view>
      </view>

      <!-- 任务分类 -->
      <view class="form-item">
        <text class="label">分类</text>
        <view class="category-list">
          <view
            v-for="(cat, index) in categories" :key="cat.id || index"
            class="category-item"
            :class="{ active: form.category === cat.value }"
            @click="form.category = cat.value"
          >
            {{ cat.label }}
          </view>
        </view>
      </view>

      <!-- 优先级 -->
      <view class="form-item">
        <text class="label">优先级</text>
        <view class="priority-list">
          <view
            v-for="(p, index) in priorities" :key="p.id || index"
            class="priority-item"
            :class="{ active: form.priority === p.value, [p.class]: true }"
            @click="form.priority = p.value"
          >
            <view class="dot"></view>
            {{ p.label }}
          </view>
        </view>
      </view>

      <!-- 截止时间 -->
      <view class="form-item">
        <text class="label">截止时间</text>
        <picker mode="multiSelector" :range="dateRange" :value="dateIndex" @change="onDateChange">
          <view class="picker-value">
            {{ form.deadline || '请选择截止时间' }}
            <up-icon name="arrow-right" size="28" color="#999"></up-icon>
          </view>
        </picker>
      </view>

      <!-- 指派给 -->
      <view class="form-item">
        <text class="label">指派给</text>
        <view class="member-list">
          <view
            v-for="(member, index) in members" :key="member.id || index"
            class="member-item"
            :class="{ active: form.assigneeId === member.id }"
            @click="form.assigneeId = member.id"
          >
            <image :src="member.avatar" class="avatar" />
            <text class="name">{{ member.name }}</text>
          </view>
        </view>
      </view>

      <!-- 备注 -->
      <view class="form-item">
        <text class="label">备注</text>
        <textarea
          v-model="form.remark"
          placeholder="添加备注说明..."
          class="textarea"
        />
      </view>

      <!-- 重复设置 -->
      <view class="form-item">
        <text class="label">重复</text>
        <view class="repeat-list">
          <view
            v-for="(r, index) in repeats" :key="r.id || index"
            class="repeat-item"
            :class="{ active: form.repeatType === r.value }"
            @click="selectRepeat(r.value)"
          >
            {{ r.label }}
          </view>
        </view>

        <!-- 自定义重复规则 -->
        <view v-if="form.repeatType === 'custom'" class="custom-repeat">
          <view class="custom-row">
            <text>每</text>
            <input v-model.number="customRepeat.interval" type="number" class="interval-input" />
            <picker mode="selector" :range="customUnits" :value="customRepeat.unitIndex" @change="onCustomUnitChange">
              <text class="unit-picker">{{ customUnits[customRepeat.unitIndex] }}</text>
            </picker>
          </view>

          <!-- 每周选项 -->
          <view v-if="customRepeat.unitIndex === 1" class="weekdays-select">
            <view
              v-for="(day, index) in weekDays"
              :key="index"
              class="weekday-item"
              :class="{ active: customRepeat.weekdays.includes(index + 1) }"
              @click="toggleWeekday(index + 1)"
            >
              {{ day }}
            </view>
          </view>
        </view>

        <!-- 重复结束条件 -->
        <view v-if="form.repeatType !== 'none'" class="repeat-end">
          <text class="sub-label">结束</text>
          <view class="end-options">
            <view
              v-for="(opt, index) in endOptions" :key="opt.id || index"
              class="end-option"
              :class="{ active: form.repeatEndType === opt.value }"
              @click="form.repeatEndType = opt.value"
            >
              {{ opt.label }}
            </view>
          </view>
          <view v-if="form.repeatEndType === 'date'" class="end-date-picker">
            <picker mode="date" :value="form.repeatEndDate" @change="onEndDateChange">
              <text class="picker-value">{{ form.repeatEndDate || '选择结束日期' }}</text>
            </picker>
          </view>
          <view v-if="form.repeatEndType === 'count'" class="end-count-input">
            <input v-model.number="form.repeatCount" type="number" placeholder="重复次数" class="count-input" />
            <text>次</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { taskApi } from '../../api/index.js'

const form = ref({
  title: '',
  category: 'shopping',
  priority: 0,
  deadline: '',
  assigneeId: null,
  remark: '',
  repeatType: 'none',
  repeatEndType: 'never',
  repeatEndDate: '',
  repeatCount: null
})

// 编辑模式标识
const isEditMode = ref(false)
const taskId = ref(null)

// 页面标题
const pageTitle = computed(() => isEditMode.value ? '编辑任务' : '创建任务')

// 加载任务数据（编辑模式）
const loadTaskData = async (id) => {
  try {
    uni.showLoading({ title: '加载中...' })
    
    const res = await taskApi.getById(id)
    console.log('[TaskCreate] 获取任务详情:', res)
    
    if (res) {
      isEditMode.value = true
      taskId.value = id
      
      // 填充表单数据
      form.value.title = res.title || ''
      form.value.category = res.category || 'shopping'
      form.value.priority = res.priority || 0
      form.value.assigneeId = res.assigneeId || null
      form.value.remark = res.remark || ''
      form.value.status = res.status || 0
      
      // 处理截止时间
      if (res.dueTime) {
        const date = new Date(res.dueTime)
        const year = date.getFullYear()
        const month = String(date.getMonth() + 1).padStart(2, '0')
        const day = String(date.getDate()).padStart(2, '0')
        const hours = String(date.getHours()).padStart(2, '0')
        const minutes = String(date.getMinutes()).padStart(2, '0')
        form.value.deadline = `${year}-${month}-${day} ${hours}:${minutes}`
      }
      
      console.log('[TaskCreate] 表单数据填充完成:', form.value)
    }
    
    uni.hideLoading()
  } catch (error) {
    uni.hideLoading()
    console.error('[TaskCreate] 获取任务详情失败:', error)
    uni.showToast({ title: '加载任务失败', icon: 'none' })
  }
}

// 自定义重复规则
const customRepeat = ref({
  interval: 1,
  unitIndex: 0,
  weekdays: []
})

const weekDays = ['一', '二', '三', '四', '五', '六', '日']
const customUnits = ['天', '周', '月']
const endOptions = [
  { label: '永不', value: 'never' },
  { label: '截止日期', value: 'date' },
  { label: '重复次数', value: 'count' }
]

const categories = [
  { label: '购物', value: 'shopping' },
  { label: '家务', value: 'housework' },
  { label: '财务', value: 'finance' },
  { label: '育儿', value: 'parenting' },
  { label: '其他', value: 'other' }
]

const priorities = [
  { label: '普通', value: 0, class: 'normal' },
  { label: '重要', value: 1, class: 'medium' },
  { label: '紧急', value: 2, class: 'high' }
]

const repeats = [
  { label: '不重复', value: 'none' },
  { label: '每天', value: 'daily' },
  { label: '每周', value: 'weekly' },
  { label: '每月', value: 'monthly' },
  { label: '自定义', value: 'custom' }
]

const members = ref([
  { id: 1, name: '爸爸', avatar: '/static/avatar/dad.png' },
  { id: 2, name: '妈妈', avatar: '/static/avatar/mom.png' },
  { id: 3, name: '宝贝', avatar: '/static/avatar/kid.png' }
])

// 检查URL参数（判断是编辑还是新增模式）
onMounted(() => {
  // 获取页面传递的参数
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const options = currentPage?.options || {}
  
  console.log('[TaskCreate] 页面参数:', options)
  
  if (options.id) {
    // 编辑模式 - 加载任务数据
    loadTaskData(options.id)
  }
})

// 选择重复类型
const selectRepeat = (value) => {
  form.value.repeatType = value
  if (value !== 'custom') {
    customRepeat.value.weekdays = []
  }
}

// 切换星期选择
const toggleWeekday = (day) => {
  const index = customRepeat.value.weekdays.indexOf(day)
  if (index > -1) {
    customRepeat.value.weekdays.splice(index, 1)
  } else {
    customRepeat.value.weekdays.push(day)
  }
}

// 自定义单位变化
const onCustomUnitChange = (e) => {
  customRepeat.value.unitIndex = e.detail.value
  if (customRepeat.value.unitIndex !== 1) {
    customRepeat.value.weekdays = []
  }
}

// 结束日期变化
const onEndDateChange = (e) => {
  form.value.repeatEndDate = e.detail.value
}

// 生成重复规则
const buildRepeatRule = () => {
  if (form.value.repeatType === 'none') return null

  let repeatRule = {}

  if (form.value.repeatType === 'custom') {
    const unitMap = ['day', 'week', 'month']
    repeatRule = {
      interval: customRepeat.value.interval || 1,
      unit: unitMap[customRepeat.value.unitIndex],
      weekdays: customRepeat.value.weekdays
    }
  }

  // 结束条件
  if (form.value.repeatEndType === 'date') {
    repeatRule.endDate = form.value.repeatEndDate
  } else if (form.value.repeatEndType === 'count') {
    repeatRule.endCount = form.value.repeatCount
  }

  return JSON.stringify(repeatRule)
}

// 生成日期选择器数据
const dateRange = computed(() => {
  const years = []
  const months = []
  const days = []
  const hours = []
  const minutes = ['00', '30']

  const now = new Date()
  for (let i = now.getFullYear(); i <= now.getFullYear() + 1; i++) {
    years.push(i + '年')
  }
  for (let i = 1; i <= 12; i++) {
    months.push(i + '月')
  }
  for (let i = 1; i <= 31; i++) {
    days.push(i + '日')
  }
  for (let i = 0; i < 24; i++) {
    hours.push(i + '时')
  }

  return [years, months, days, hours, minutes]
})

const dateIndex = ref([0, 0, 0, 0, 0])

const onDateChange = (e) => {
  const val = e.detail.value
  const year = dateRange.value[0][val[0]].replace('年', '')
  const month = dateRange.value[1][val[1]].replace('月', '').padStart(2, '0')
  const day = dateRange.value[2][val[2]].replace('日', '').padStart(2, '0')
  const hour = dateRange.value[3][val[3]].replace('时', '').padStart(2, '0')
  const minute = dateRange.value[4][val[4]]

  form.value.deadline = `${year}-${month}-${day} ${hour}:${minute}`
}

const goBack = () => {
  uni.navigateBack()
}

const saveTask = async () => {
  if (!form.value.title.trim()) {
    uni.showToast({ title: '请输入任务标题', icon: 'none' })
    return
  }

  // 自定义重复验证
  if (form.value.repeatType === 'custom') {
    if (customRepeat.value.unitIndex === 1 && customRepeat.value.weekdays.length === 0) {
      uni.showToast({ title: '请至少选择一天', icon: 'none' })
      return
    }
  }

  try {
    const familyId = uni.getStorageSync('currentFamilyId') || 1
    // 获取当前用户信息
    const userInfo = uni.getStorageSync('userInfo')
    const userId = userInfo?.id || userInfo?.userId

    // 处理截止时间格式
    let dueTime = null
    if (form.value.deadline) {
      // 将 '2026-02-23 14:30' 转换为 ISO 8601 格式
      dueTime = form.value.deadline.replace(' ', 'T') + ':00'
    } else {
      // 默认今天 23:59
      const today = new Date().toISOString().split('T')[0]
      dueTime = `${today}T23:59:00`
    }

    const data = {
      title: form.value.title,
      familyId: familyId,
      category: form.value.category,
      priority: form.value.priority,
      dueTime: dueTime,  // 后端期望 LocalDateTime 格式
      assigneeId: form.value.assigneeId,
      remark: form.value.remark,
      status: form.value.status || 0,
      creatorId: userId
    }

    console.log('[TaskCreate] 保存任务请求数据:', data)

    if (isEditMode.value && taskId.value) {
      // 编辑模式 - 更新任务
      data.id = taskId.value
      await taskApi.update(data)
      uni.showToast({ title: '更新成功', icon: 'success' })
    } else {
      // 新增模式 - 创建任务
      const res = await taskApi.create(data)
      const newTaskId = res.id

      // 设置重复规则
      if (form.value.repeatType !== 'none' && newTaskId) {
        const repeatRule = buildRepeatRule()
        await taskApi.setRepeatRule(newTaskId, {
          repeatType: form.value.repeatType,
          repeatRule: repeatRule
        })
      }
      uni.showToast({ title: '创建成功', icon: 'success' })
    }

    setTimeout(() => {
      uni.navigateBack()
    }, 1500)
  } catch (e) {
    console.error('保存任务失败', e)
    let errorMsg = '保存失败'
    if (e?.message) {
      errorMsg = e.message
    } else if (typeof e === 'string') {
      errorMsg = e
    }
    uni.showModal({
      title: '保存失败',
      content: errorMsg,
      showCancel: false
    })
  }
}
</script>

<style lang="scss" scoped>
.create-task-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #f8f9fc 0%, #f0f4f8 100%);
}

.nav-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 30rpx;
  padding-top: 60rpx;
  background: linear-gradient(135deg, #6B8DD6 0%, #8B5CF6 100%);
  box-shadow: 0 8rpx 32rpx rgba(107, 141, 214, 0.25);

  .back-btn {
    width: 64rpx;
    height: 64rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    background: rgba(255,255,255,0.15);
    border-radius: 50%;
    transition: all 0.25s ease;
    
    &:active {
      background: rgba(255,255,255,0.25);
      transform: scale(0.92);
    }
  }

  .title {
    font-size: 36rpx;
    font-weight: 600;
    color: #fff;
    text-shadow: 0 2rpx 8rpx rgba(0,0,0,0.1);
  }

  .right-btn {
    padding: 14rpx 32rpx;
    background: rgba(255,255,255,0.95);
    border-radius: 32rpx;
    box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.1);
    transition: all 0.25s ease;
    
    &:active {
      transform: scale(0.95);
      box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.08);
    }

    text {
      font-size: 28rpx;
      color: #6B8DD6;
      font-weight: 600;
    }
  }
}

.form-container {
  padding: 30rpx;
}

.form-item {
  background: #fff;
  border-radius: 28rpx;
  padding: 32rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 8rpx 32rpx rgba(107, 141, 214, 0.08), 0 2rpx 8rpx rgba(0,0,0,0.03);
  transition: all 0.3s ease;
  
  &:active {
    transform: scale(0.995);
  }

  .label {
    font-size: 30rpx;
    font-weight: 600;
    color: #2d3748;
    display: block;
    margin-bottom: 20rpx;
    
    .required {
      color: #ff6b6b;
      margin-left: 4rpx;
    }
  }
  
  .input-wrapper {
    background: #f8f9fc;
    border-radius: 20rpx;
    padding: 0 24rpx;
    border: 2rpx solid transparent;
    transition: all 0.25s ease;
    
    &:focus-within {
      background: #fff;
      border-color: #6B8DD6;
      box-shadow: 0 0 0 6rpx rgba(107, 141, 214, 0.1);
    }
  }
  
  .title-input-wrapper {
    padding: 0;
    background: transparent;
    
    &:focus-within {
      background: transparent;
      box-shadow: none;
    }
  }

  .input {
    font-size: 32rpx;
    color: #2d3748;
    height: 90rpx;
    background: #f8f9fc;
    border-radius: 20rpx;
    padding: 0 24rpx;
    border: 2rpx solid transparent;
    transition: all 0.25s ease;
    width: 100%;
    box-sizing: border-box;
    
    &:focus {
      background: #fff;
      border-color: #6B8DD6;
      box-shadow: 0 0 0 6rpx rgba(107, 141, 214, 0.1);
    }
  }
  
  .input-placeholder {
    color: #a0aec0;
    font-size: 30rpx;
  }

  .textarea {
    font-size: 30rpx;
    color: #2d3748;
    height: 180rpx;
    width: 100%;
    background: #f8f9fc;
    border-radius: 20rpx;
    padding: 24rpx;
    border: 2rpx solid transparent;
    transition: all 0.25s ease;
    box-sizing: border-box;
    
    &:focus {
      background: #fff;
      border-color: #6B8DD6;
      box-shadow: 0 0 0 6rpx rgba(107, 141, 214, 0.1);
    }
  }

  .picker-value {
    display: flex;
    align-items: center;
    justify-content: space-between;
    font-size: 30rpx;
    color: #2d3748;
    height: 90rpx;
    background: #f8f9fc;
    border-radius: 20rpx;
    padding: 0 24rpx;
  }
}

.category-list {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;

  .category-item {
    padding: 18rpx 36rpx;
    background: #f8f9fc;
    border-radius: 28rpx;
    font-size: 28rpx;
    color: #5a6c7d;
    transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
    border: 2rpx solid transparent;
    
    &:active {
      transform: scale(0.95);
    }

    &.active {
      background: linear-gradient(135deg, #6B8DD6 0%, #8B5CF6 100%);
      color: #fff;
      box-shadow: 0 8rpx 24rpx rgba(107, 141, 214, 0.35);
    }
  }
}

.priority-list {
  display: flex;
  gap: 20rpx;

  .priority-item {
    display: flex;
    align-items: center;
    gap: 12rpx;
    padding: 18rpx 32rpx;
    background: #f8f9fc;
    border-radius: 28rpx;
    font-size: 28rpx;
    color: #5a6c7d;
    transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
    border: 2rpx solid transparent;
    
    &:active {
      transform: scale(0.95);
    }

    .dot {
      width: 16rpx;
      height: 16rpx;
      border-radius: 50%;
      background: #cbd5e0;
      transition: all 0.25s ease;
    }

    &.active {
      &.normal {
        background: rgba(24, 144, 255, 0.1);
        color: #1890FF;
        border-color: rgba(24, 144, 255, 0.3);
        box-shadow: 0 4rpx 16rpx rgba(24, 144, 255, 0.15);
        .dot { background: #1890FF; }
      }
      &.medium {
        background: rgba(250, 173, 20, 0.1);
        color: #FAAD14;
        border-color: rgba(250, 173, 20, 0.3);
        box-shadow: 0 4rpx 16rpx rgba(250, 173, 20, 0.15);
        .dot { background: #FAAD14; }
      }
      &.high {
        background: rgba(255, 77, 79, 0.1);
        color: #FF4D4F;
        border-color: rgba(255, 77, 79, 0.3);
        box-shadow: 0 4rpx 16rpx rgba(255, 77, 79, 0.15);
        .dot { background: #FF4D4F; }
      }
    }
  }
}

.member-list {
  display: flex;
  gap: 30rpx;

  .member-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    opacity: 0.5;
    transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
    padding: 16rpx;
    border-radius: 20rpx;

    &.active {
      opacity: 1;
      background: rgba(107, 141, 214, 0.08);

      .avatar {
        border: 4rpx solid #6B8DD6;
        box-shadow: 0 8rpx 24rpx rgba(107, 141, 214, 0.35);
      }
    }
    
    &:active {
      transform: scale(0.95);
    }

    .avatar {
      width: 100rpx;
      height: 100rpx;
      border-radius: 50%;
      margin-bottom: 12rpx;
      border: 4rpx solid transparent;
      transition: all 0.25s ease;
      box-shadow: 0 4rpx 12rpx rgba(0,0,0,0.08);
    }

    .name {
      font-size: 26rpx;
      color: #2d3748;
      font-weight: 500;
    }
  }
}

.repeat-list {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;

  .repeat-item {
    padding: 16rpx 32rpx;
    background: #f5f5f5;
    border-radius: 30rpx;
    font-size: 26rpx;
    color: #666;

    &.active {
      background: #5B8FF9;
      color: #fff;
    }
  }
}

// 自定义重复样式
.custom-repeat {
  margin-top: 20rpx;
  padding: 20rpx;
  background: #f9f9f9;
  border-radius: 12rpx;

  .custom-row {
    display: flex;
    align-items: center;
    gap: 16rpx;
    font-size: 28rpx;
    color: #333;

    .interval-input {
      width: 100rpx;
      height: 60rpx;
      background: #fff;
      border-radius: 8rpx;
      text-align: center;
      font-size: 28rpx;
    }

    .unit-picker {
      padding: 10rpx 20rpx;
      background: #fff;
      border-radius: 8rpx;
      color: #5B8FF9;
    }
  }

  .weekdays-select {
    display: flex;
    gap: 12rpx;
    margin-top: 20rpx;

    .weekday-item {
      width: 60rpx;
      height: 60rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      background: #fff;
      border-radius: 50%;
      font-size: 26rpx;
      color: #666;

      &.active {
        background: #5B8FF9;
        color: #fff;
      }
    }
  }
}

// 重复结束条件样式
.repeat-end {
  margin-top: 20rpx;
  padding-top: 20rpx;
  border-top: 1rpx solid #eee;

  .sub-label {
    display: block;
    font-size: 26rpx;
    color: #666;
    margin-bottom: 16rpx;
  }

  .end-options {
    display: flex;
    gap: 16rpx;
    margin-bottom: 16rpx;

    .end-option {
      padding: 12rpx 24rpx;
      background: #f5f5f5;
      border-radius: 24rpx;
      font-size: 24rpx;
      color: #666;

      &.active {
        background: #5B8FF9;
        color: #fff;
      }
    }
  }

  .end-date-picker {
    padding: 16rpx;
    background: #f9f9f9;
    border-radius: 8rpx;

    .picker-value {
      font-size: 28rpx;
      color: #333;
    }
  }

  .end-count-input {
    display: flex;
    align-items: center;
    gap: 16rpx;

    .count-input {
      width: 150rpx;
      height: 60rpx;
      background: #f9f9f9;
      border-radius: 8rpx;
      text-align: center;
      font-size: 28rpx;
    }

    text {
      font-size: 28rpx;
      color: #666;
    }
  }
}
</style>