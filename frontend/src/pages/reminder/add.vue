<template>
  <view class="page">
    <!-- 顶部导航 -->
    <view class="nav-bar">
      <view class="close-btn" @click="goBack">
        <text class="icon">✕</text>
      </view>
      <text class="title">新建提醒</text>
      <view class="save-btn" @click="saveReminder">
        <text>保存</text>
      </view>
    </view>
    
    <scroll-view class="content" scroll-y>
      <!-- 基本信息 -->
      <view class="form-section">
        <view class="section-title">基本信息</view>
        
        <view class="form-item">
          <text class="form-label">标题 <text class="required">*</text></text>
          <input 
            class="form-input" 
            v-model="form.titleTemplate" 
            placeholder="请输入提醒标题，例如：该喝水啦"
            maxlength="100"
          />
        </view>
        
        <view class="form-item">
          <text class="form-label">内容</text>
          <textarea 
            class="form-textarea" 
            v-model="form.contentTemplate" 
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
        
        <!-- 每天：选择时间 -->
        <view class="form-item" v-if="form.frequencyType === 'DAILY'">
          <text class="form-label">提醒时间</text>
          <picker mode="time" :value="form.remindTime" @change="onTimeChange">
            <view class="picker">
              <text>{{ form.remindTime || '选择时间' }}</text>
              <text class="picker-arrow">›</text>
            </view>
          </picker>
        </view>
        
        <!-- 一次性：选择日期和时间 -->
        <view class="form-item" v-if="form.frequencyType === 'ONCE'">
          <text class="form-label">提醒日期</text>
          <picker mode="date" :value="form.onceDate" @change="onOnceDateChange">
            <view class="picker">
              <text>{{ form.onceDate || '选择日期' }}</text>
              <text class="picker-arrow">›</text>
            </view>
          </picker>
        </view>
        
        <view class="form-item" v-if="form.frequencyType === 'ONCE'">
          <text class="form-label">提醒时间</text>
          <picker mode="time" :value="form.remindTime" @change="onTimeChange">
            <view class="picker">
              <text>{{ form.remindTime || '选择时间' }}</text>
              <text class="picker-arrow">›</text>
            </view>
          </picker>
        </view>
        
        <!-- 每周：选择星期和时间 -->
        <view class="form-item" v-if="form.frequencyType === 'WEEKLY'">
          <text class="form-label">选择星期</text>
          <view class="week-selector">
            <view 
              v-for="day in weekDays" 
              :key="day.value"
              class="week-day"
              :class="{ active: form.weekDays.includes(day.value) }"
              @click="toggleWeekDay(day.value)"
            >
              {{ day.label }}
            </view>
          </view>
        </view>
        
        <view class="form-item" v-if="form.frequencyType === 'WEEKLY'">
          <text class="form-label">提醒时间</text>
          <picker mode="time" :value="form.remindTime" @change="onTimeChange">
            <view class="picker">
              <text>{{ form.remindTime || '选择时间' }}</text>
              <text class="picker-arrow">›</text>
            </view>
          </picker>
        </view>
        
        <!-- 每月：选择日期和时间 -->
        <view class="form-item" v-if="form.frequencyType === 'MONTHLY'">
          <text class="form-label">每月几号</text>
          <picker :range="dayOptions" :value="monthDayIndex" @change="onMonthDayChange">
            <view class="picker">
              <text>每月 {{ form.monthDay }} 日</text>
              <text class="picker-arrow">›</text>
            </view>
          </picker>
        </view>
        
        <view class="form-item" v-if="form.frequencyType === 'MONTHLY'">
          <text class="form-label">提醒时间</text>
          <picker mode="time" :value="form.remindTime" @change="onTimeChange">
            <view class="picker">
              <text>{{ form.remindTime || '选择时间' }}</text>
              <text class="picker-arrow">›</text>
            </view>
          </picker>
        </view>
        
        <!-- 每年：选择月日和时间 -->
        <view class="form-item" v-if="form.frequencyType === 'YEARLY'">
          <text class="form-label">月-日</text>
          <picker mode="date" :value="form.yearMonthDay" fields="month" @change="onYearMonthChange">
            <view class="picker">
              <text>{{ form.yearMonthDay || '选择月-日' }}</text>
              <text class="picker-arrow">›</text>
            </view>
          </picker>
        </view>
        
        <view class="form-item" v-if="form.frequencyType === 'YEARLY'">
          <text class="form-label">提醒时间</text>
          <picker mode="time" :value="form.remindTime" @change="onTimeChange">
            <view class="picker">
              <text>{{ form.remindTime || '选择时间' }}</text>
              <text class="picker-arrow">›</text>
            </view>
          </picker>
        </view>
        
        <!-- 间隔模式 -->
        <view class="form-item" v-if="form.frequencyType === 'INTERVAL'">
          <text class="form-label">间隔设置</text>
          <view class="interval-row">
            <input 
              class="interval-input" 
              type="number" 
              v-model="form.intervalValue" 
              placeholder="数值"
            />
            <picker :range="intervalUnits" :value="intervalUnitIndex" @change="onIntervalUnitChange">
              <view class="picker-small">
                {{ intervalUnits[intervalUnitIndex] }}
              </view>
            </picker>
          </view>
        </view>
        
        <!-- 每小时 -->
        <view class="form-item" v-if="form.frequencyType === 'HOURLY'">
          <text class="form-label">间隔小时数</text>
          <input 
            class="form-input" 
            type="number" 
            v-model="form.intervalHours" 
            placeholder="请输入间隔小时数"
          />
        </view>
        
        <!-- 仅工作日 -->
        <view class="form-item switch-item">
          <text class="form-label">仅工作日推送</text>
          <switch :checked="form.workDaysOnly" @change="form.workDaysOnly = $event.detail.value" color="#667eea" />
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
              :class="{ active: form.pushScope === scope.value }"
              @click="form.pushScope = scope.value"
            >
              <view class="radio-circle">
                <view v-if="form.pushScope === scope.value" class="radio-dot"></view>
              </view>
              <text>{{ scope.label }}</text>
            </view>
          </view>
        </view>
        
        <!-- 指定用户 -->
        <view class="form-item" v-if="form.pushScope === 'SPECIFIED'">
          <text class="form-label">选择用户</text>
          <view v-if="userLoading" class="loading-text">加载中...</view>
          <view v-else class="user-selector">
            <view 
              v-for="user in userList" 
              :key="user.id"
              class="user-option"
              :class="{ active: form.targetUserIds.includes(user.id) }"
              @click="toggleUser(user.id)"
            >
              <image v-if="user.avatar" :src="user.avatar" class="user-avatar" />
              <view v-else class="user-avatar default">{{ (user.nickname || '?').charAt(0) }}</view>
              <text class="user-name">{{ user.nickname }}</text>
              <text v-if="form.targetUserIds.includes(user.id)" class="check-icon">✓</text>
            </view>
          </view>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      form: {
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
      },
      
      // 选项
      freqOptions: [
        { value: 'ONCE', label: '一次性' },
        { value: 'DAILY', label: '每天' },
        { value: 'HOURLY', label: '每小时' },
        { value: 'WEEKLY', label: '每周' },
        { value: 'MONTHLY', label: '每月' },
        { value: 'YEARLY', label: '每年' },
        { value: 'INTERVAL', label: '间隔' }
      ],
      scopeOptions: [
        { value: 'SELF', label: '仅自己' },
        { value: 'ALL', label: '全部用户' },
        { value: 'SPECIFIED', label: '指定用户' }
      ],
      weekDays: [
        { value: 1, label: '一' },
        { value: 2, label: '二' },
        { value: 3, label: '三' },
        { value: 4, label: '四' },
        { value: 5, label: '五' },
        { value: 6, label: '六' },
        { value: 7, label: '日' }
      ],
      dayOptions: Array.from({ length: 31 }, (_, i) => `${i + 1}日`),
      intervalUnits: ['分钟', '小时', '天'],
      
      userList: [],
      userLoading: false
    }
  },
  computed: {
    freqIndex() {
      return this.freqOptions.findIndex(f => f.value === this.form.frequencyType)
    },
    monthDayIndex() {
      return this.form.monthDay - 1
    },
    intervalUnitIndex() {
      return ['minutes', 'hours', 'days'].indexOf(this.form.intervalUnit)
    }
  },
  onLoad() {
    this.loadUserList()
    // 设置默认标题模板
    this.form.titleTemplate = '定时提醒'
  },
  methods: {
    // 加载用户列表
    async loadUserList() {
      if (this.form.pushScope !== 'SPECIFIED') return
      
      this.userLoading = true
      try {
        const data = await this.$request.get('/api/reminder/users')
        this.userList = data?.list || []
      } catch (e) {
        console.error('加载用户列表失败', e)
      } finally {
        this.userLoading = false
      }
    },
    
    // 保存提醒
    async saveReminder() {
      if (!this.form.titleTemplate) {
        uni.showToast({ title: '请输入提醒标题', icon: 'none' })
        return
      }
      
      // 构建频率配置
      const config = {}
      const f = this.form
      
      if (f.frequencyType === 'ONCE') {
        config.fixedDate = f.onceDate
        config.fixedTime = f.remindTime
      } else if (f.frequencyType === 'DAILY') {
        config.fixedTime = f.remindTime
      } else if (f.frequencyType === 'WEEKLY') {
        config.weekDays = f.weekDays
        config.fixedTime = f.remindTime
      } else if (f.frequencyType === 'MONTHLY') {
        config.monthDay = f.monthDay
        config.fixedTime = f.remindTime
      } else if (f.frequencyType === 'YEARLY') {
        config.yearMonthDay = f.yearMonthDay
        config.fixedTime = f.remindTime
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
        await this.$request.post('/api/reminder/add', data)
        uni.showToast({ title: '创建成功' })
        setTimeout(() => uni.navigateBack(), 1500)
      } catch (e) {
        console.error('创建提醒失败', e)
        uni.showToast({ title: e.message || '创建失败', icon: 'none' })
      }
    },
    
    // 事件处理
    onFreqChange(e) {
      this.form.frequencyType = this.freqOptions[e.detail.value].value
    },
    onTimeChange(e) {
      this.form.remindTime = e.detail.value
    },
    onOnceDateChange(e) {
      this.form.onceDate = e.detail.value
    },
    onYearMonthChange(e) {
      // 提取月-日
      const date = e.detail.value // 格式: YYYY-MM
      this.form.yearMonthDay = date.substring(5) // 取 MM-DD 部分
    },
    onMonthDayChange(e) {
      this.form.monthDay = parseInt(e.detail.value) + 1
    },
    onIntervalUnitChange(e) {
      const units = ['minutes', 'hours', 'days']
      this.form.intervalUnit = units[e.detail.value]
    },
    
    // 切换星期选择
    toggleWeekDay(value) {
      const index = this.form.weekDays.indexOf(value)
      if (index > -1) {
        this.form.weekDays.splice(index, 1)
      } else {
        this.form.weekDays.push(value)
      }
    },
    
    // 切换用户选择
    toggleUser(userId) {
      const index = this.form.targetUserIds.indexOf(userId)
      if (index > -1) {
        this.form.targetUserIds.splice(index, 1)
      } else {
        this.form.targetUserIds.push(userId)
      }
    },
    
    goBack() {
      uni.navigateBack()
    }
  }
}
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: #f5f6fa;
  display: flex;
  flex-direction: column;
}

.nav-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 60rpx 30rpx 20rpx;
  background: #fff;
  
  .close-btn, .save-btn {
    width: 120rpx;
    display: flex;
    align-items: center;
  }
  
  .close-btn { justify-content: flex-start; }
  .save-btn { justify-content: flex-end; }
  
  .icon {
    font-size: 36rpx;
    color: #999;
  }
  
  .title {
    font-size: 34rpx;
    font-weight: 600;
    color: #333;
  }
  
  .save-btn text {
    color: #667eea;
    font-size: 30rpx;
    font-weight: 500;
  }
}

.content {
  flex: 1;
  padding: 20rpx;
  padding-bottom: 40rpx;
}

.form-section {
  background: #fff;
  border-radius: 16rpx;
  margin-bottom: 20rpx;
  overflow: hidden;
}

.section-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #333;
  padding: 24rpx 30rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.form-item {
  padding: 24rpx 30rpx;
  border-bottom: 1rpx solid #f5f5f5;
  
  &:last-child {
    border-bottom: none;
  }
  
  &.switch-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
}

.form-label {
  font-size: 28rpx;
  color: #333;
  margin-bottom: 16rpx;
  display: block;
  
  .required {
    color: #ff4d4f;
  }
}

.form-input, .form-textarea, .picker {
  width: 100%;
  padding: 20rpx;
  background: #f5f6fa;
  border-radius: 12rpx;
  font-size: 28rpx;
  box-sizing: border-box;
}

.form-input {
  height: 88rpx;
  line-height: 48rpx;
}

.form-textarea {
  min-height: 160rpx;
}

.picker {
  display: flex;
  justify-content: space-between;
  align-items: center;
  
  .picker-arrow {
    color: #999;
    font-size: 32rpx;
  }
}

// 星期选择器
.week-selector {
  display: flex;
  gap: 12rpx;
  flex-wrap: wrap;
}

.week-day {
  width: 72rpx;
  height: 72rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2rpx solid #eee;
  border-radius: 50%;
  font-size: 28rpx;
  color: #666;
  
  &.active {
    background: #667eea;
    border-color: #667eea;
    color: #fff;
  }
}

// 间隔设置
.interval-row {
  display: flex;
  gap: 20rpx;
}

.interval-input {
  flex: 1;
  padding: 20rpx;
  background: #f5f6fa;
  border-radius: 12rpx;
  font-size: 28rpx;
}

.picker-small {
  flex: 1;
  padding: 20rpx;
  background: #f5f6fa;
  border-radius: 12rpx;
  font-size: 28rpx;
  text-align: center;
}

// 单选组
.radio-group {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.radio-item {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 20rpx;
  border: 2rpx solid #eee;
  border-radius: 12rpx;
  
  &.active {
    border-color: #667eea;
    background: #f0f3ff;
  }
  
  text {
    font-size: 28rpx;
    color: #333;
  }
}

.radio-circle {
  width: 36rpx;
  height: 36rpx;
  border: 4rpx solid #ddd;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  
  .radio-item.active & {
    border-color: #667eea;
  }
}

.radio-dot {
  width: 18rpx;
  height: 18rpx;
  background: #667eea;
  border-radius: 50%;
}

// 用户选择
.user-selector {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
}

.loading-text {
  text-align: center;
  color: #999;
  padding: 20rpx;
}

.user-option {
  display: flex;
  align-items: center;
  gap: 12rpx;
  padding: 16rpx 24rpx;
  background: #f5f6fa;
  border-radius: 40rpx;
  border: 2rpx solid transparent;
  
  &.active {
    border-color: #667eea;
    background: #f0f3ff;
  }
}

.user-avatar {
  width: 48rpx;
  height: 48rpx;
  border-radius: 50%;
  
  &.default {
    background: #667eea;
    color: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 24rpx;
  }
}

.user-name {
  font-size: 26rpx;
  color: #333;
}

.check-icon {
  color: #667eea;
  font-size: 28rpx;
}
</style>
