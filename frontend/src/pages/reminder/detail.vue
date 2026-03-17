<template>
  <view class="page">
    <!-- 顶部导航 -->
    <view class="nav-bar">
      <view class="back-btn" @click="goBack">
        <text class="icon">‹</text>
      </view>
      <text class="title">提醒详情</text>
      <view class="right-btn" @click="toggleEdit">
        <text>{{ isEditing ? '取消' : '编辑' }}</text>
      </view>
    </view>
    
    <!-- 加载中 -->
    <view v-if="loading" class="loading-state">
      <text>加载中...</text>
    </view>
    
    <!-- 查看模式 -->
    <scroll-view v-else-if="!isEditing" class="content" scroll-y>
      <!-- 头部信息卡片 -->
      <view class="header-card">
        <view class="icon-wrap" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%)">
          <text class="type-icon">⏰</text>
        </view>
        <text class="reminder-name">{{ reminder.reminderName }}</text>
        <view class="status-wrap">
          <text class="status-badge" :class="{ active: reminder.status === 1 }">
            {{ reminder.status === 1 ? '启用中' : '已停用' }}
          </text>
        </view>
      </view>
      
      <!-- 基本信息 -->
      <view class="info-section">
        <view class="section-title">基本信息</view>
        <view class="info-list">
          <view class="info-item">
            <text class="label">提醒频率</text>
            <text class="value">{{ formatFrequency(reminder) }}</text>
          </view>
          <view class="info-item" v-if="reminder.nextExecuteTime">
            <text class="label">下次执行</text>
            <text class="value">{{ formatDateTime(reminder.nextExecuteTime) }}</text>
          </view>
          <view class="info-item" v-if="reminder.lastExecuteTime">
            <text class="label">上次执行</text>
            <text class="value">{{ formatDateTime(reminder.lastExecuteTime) }}</text>
          </view>
          <view class="info-item" v-if="reminder.lastExecuteResult">
            <text class="label">执行结果</text>
            <text class="value" :class="getResultClass(reminder.lastExecuteResult)">
              {{ getResultText(reminder.lastExecuteResult) }}
            </text>
          </view>
        </view>
      </view>
      
      <!-- 推送设置 -->
      <view class="info-section">
        <view class="section-title">推送设置</view>
        <view class="info-list">
          <view class="info-item">
            <text class="label">推送范围</text>
            <text class="value">{{ getScopeText(reminder.pushScope) }}</text>
          </view>
          <view class="info-item" v-if="reminder.pushScope === 'SPECIFIED' && targetUsers.length > 0">
            <text class="label">指定用户</text>
            <view class="user-tags">
              <text v-for="user in targetUsers" :key="user.id" class="user-tag">
                {{ user.nickname }}
              </text>
            </view>
          </view>
        </view>
      </view>
      
      <!-- 消息内容 -->
      <view class="info-section">
        <view class="section-title">消息内容</view>
        <view class="template-box">
          <view class="template-item">
            <text class="template-label">标题</text>
            <text class="template-content">{{ reminder.titleTemplate || '-' }}</text>
          </view>
          <view class="template-item" v-if="reminder.contentTemplate">
            <text class="template-label">内容</text>
            <text class="template-content">{{ reminder.contentTemplate }}</text>
          </view>
        </view>
      </view>
      
      <!-- 操作按钮 -->
      <view class="action-section">
        <button class="action-btn test-btn" @click="executeNow">
          <text>立即执行测试</text>
        </button>
        <button class="action-btn delete-btn" @click="deleteReminder">
          <text>删除提醒</text>
        </button>
      </view>
    </scroll-view>
    
    <!-- 编辑模式 -->
    <scroll-view v-else class="content" scroll-y>
      <!-- 基本信息编辑 -->
      <view class="form-section">
        <view class="section-title">基本信息</view>
        
        <view class="form-item">
          <text class="form-label">提醒名称 <text class="required">*</text></text>
          <input 
            class="form-input" 
            v-model="editForm.reminderName" 
            placeholder="请输入提醒名称"
            maxlength="50"
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
        
        <!-- 每天/每周/每月/每年：固定时间 -->
        <view class="form-item" v-if="showTimePicker">
          <text class="form-label">提醒时间</text>
          <picker mode="time" :value="editForm.remindTime" @change="onTimeChange">
            <view class="picker">
              <text>{{ editForm.remindTime || '选择时间' }}</text>
              <text class="picker-arrow">›</text>
            </view>
          </picker>
        </view>
        
        <!-- 每周：选择星期 -->
        <view class="form-item" v-if="editForm.frequencyType === 'WEEKLY'">
          <text class="form-label">选择星期</text>
          <view class="week-selector">
            <view 
              v-for="(day, index) in weekDays" 
              :key="index"
              class="week-day"
              :class="{ active: selectedWeekDays.includes(day.value) }"
              @click="toggleWeekDay(day.value)"
            >
              {{ day.label }}
            </view>
          </view>
        </view>
        
        <!-- 每月：选择日期 -->
        <view class="form-item" v-if="editForm.frequencyType === 'MONTHLY'">
          <text class="form-label">每月几号</text>
          <picker :range="dayOptions" :value="monthDayIndex" @change="onMonthDayChange">
            <view class="picker">
              <text>每月 {{ editForm.monthDay || 1 }} 日</text>
              <text class="picker-arrow">›</text>
            </view>
          </picker>
        </view>
        
        <!-- 间隔模式 -->
        <view class="form-item" v-if="editForm.frequencyType === 'INTERVAL'">
          <text class="form-label">间隔设置</text>
          <view class="interval-row">
            <input 
              class="interval-input" 
              type="number" 
              v-model="editForm.intervalValue" 
              placeholder="数值"
            />
            <picker :range="intervalUnits" :value="intervalUnitIndex" @change="onIntervalUnitChange">
              <view class="picker-small">
                {{ intervalUnits[intervalUnitIndex] }}
              </view>
            </picker>
          </view>
        </view>
        
        <!-- 仅工作日推送 -->
        <view class="form-item switch-item">
          <text class="form-label">仅工作日推送</text>
          <switch :checked="editForm.workDaysOnly" @change="editForm.workDaysOnly = $event.detail.value" color="#667eea" />
        </view>
      </view>
      
      <!-- 推送范围设置 -->
      <view class="form-section">
        <view class="section-title">推送范围</view>
        
        <view class="form-item">
          <text class="form-label">推送对象</text>
          <view class="radio-group">
            <view 
              v-for="scope in scopeOptions" 
              :key="scope.value"
              class="radio-item"
              :class="{ active: editForm.pushScope === scope.value }"
              @click="editForm.pushScope = scope.value"
            >
              <view class="radio-circle">
                <view v-if="editForm.pushScope === scope.value" class="radio-dot"></view>
              </view>
              <text>{{ scope.label }}</text>
            </view>
          </view>
        </view>
        
        <!-- 指定用户选择 -->
        <view class="form-item" v-if="editForm.pushScope === 'SPECIFIED'">
          <text class="form-label">选择用户</text>
          <view class="user-selector">
            <view 
              v-for="user in userList" 
              :key="user.id"
              class="user-option"
              :class="{ active: editForm.targetUserIds.includes(user.id) }"
              @click="toggleUser(user.id)"
            >
              <image v-if="user.avatar" :src="user.avatar" class="user-avatar" />
              <view v-else class="user-avatar default">{{ user.nickname?.charAt(0) || '?' }}</view>
              <text class="user-name">{{ user.nickname }}</text>
              <text v-if="editForm.targetUserIds.includes(user.id)" class="check-icon">✓</text>
            </view>
          </view>
        </view>
      </view>
      
      <!-- 消息内容 -->
      <view class="form-section">
        <view class="section-title">消息内容</view>
        
        <view class="form-item">
          <text class="form-label">标题 <text class="required">*</text></text>
          <input 
            class="form-input" 
            v-model="editForm.titleTemplate" 
            placeholder="请输入提醒标题，例如：该喝水啦"
            maxlength="100"
          />
        </view>
        
        <view class="form-item">
          <text class="form-label">内容</text>
          <textarea 
            class="form-textarea" 
            v-model="editForm.contentTemplate" 
            placeholder="请输入提醒内容（选填）"
            maxlength="500"
          />
        </view>
      </view>
      
      <!-- 保存按钮 -->
      <view class="save-section">
        <button class="save-btn" @click="saveEdit">保存修改</button>
      </view>
    </scroll-view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      id: null,
      loading: true,
      isEditing: false,
      reminder: {},
      targetUsers: [],
      userList: [],
      
      // 编辑表单
      editForm: {
        reminderName: '',
        frequencyType: 'DAILY',
        remindTime: '08:00',
        weekDays: [],
        monthDay: 1,
        intervalValue: 60,
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
      intervalUnits: ['分钟', '小时', '天']
    }
  },
  computed: {
    freqIndex() {
      return this.freqOptions.findIndex(f => f.value === this.editForm.frequencyType)
    },
    monthDayIndex() {
      return (this.editForm.monthDay || 1) - 1
    },
    intervalUnitIndex() {
      return ['minutes', 'hours', 'days'].indexOf(this.editForm.intervalUnit)
    },
    showTimePicker() {
      return ['DAILY', 'WEEKLY', 'MONTHLY', 'YEARLY'].includes(this.editForm.frequencyType)
    },
    selectedWeekDays() {
      return this.editForm.weekDays || []
    }
  },
  onLoad(options) {
    if (options.id) {
      this.id = options.id
      this.loadDetail()
      this.loadUserList()
    }
  },
  methods: {
    // 加载详情
    async loadDetail() {
      this.loading = true
      try {
        const data = await this.$request.get(`/api/reminder/detail/${this.id}`)
        this.reminder = data || {}
        this.parseTargetUsers()
        this.initEditForm()
      } catch (e) {
        console.error('加载详情失败', e)
        uni.showToast({ title: e.message || '加载失败', icon: 'none' })
      } finally {
        this.loading = false
      }
    },
    
    // 加载用户列表
    async loadUserList() {
      try {
        const data = await this.$request.get('/api/reminder/users')
        this.userList = data?.list || []
      } catch (e) {
        console.error('加载用户列表失败', e)
      }
    },
    
    // 解析目标用户
    parseTargetUsers() {
      if (this.reminder.pushScope === 'SPECIFIED' && this.reminder.targetUserIds) {
        try {
          const ids = JSON.parse(this.reminder.targetUserIds)
          // 这里简化处理，实际应该从userList中匹配
          this.targetUsers = ids.map(id => ({ id, nickname: `用户${id}` }))
        } catch (e) {}
      }
    },
    
    // 初始化编辑表单
    initEditForm() {
      const r = this.reminder
      let config = {}
      try {
        config = JSON.parse(r.frequencyConfig || '{}')
      } catch (e) {}
      
      this.editForm = {
        reminderName: r.reminderName || '',
        reminderType: r.reminderType || 'SYSTEM',
        frequencyType: r.frequencyType || 'DAILY',
        remindTime: config.fixedTime || r.remindTime || '08:00',
        weekDays: config.weekDays || [],
        monthDay: config.monthDay || 1,
        intervalValue: config.intervalMinutes || config.intervalHours || config.intervalDays || 60,
        intervalUnit: config.intervalMinutes ? 'minutes' : config.intervalHours ? 'hours' : 'days',
        workDaysOnly: config.workDaysOnly || false,
        pushScope: r.pushScope || 'SELF',
        targetUserIds: [],
        titleTemplate: r.titleTemplate || '',
        contentTemplate: r.contentTemplate || ''
      }
      
      // 解析targetUserIds
      if (r.targetUserIds) {
        try {
          this.editForm.targetUserIds = JSON.parse(r.targetUserIds)
        } catch (e) {}
      }
    },
    
    // 切换编辑模式
    toggleEdit() {
      if (this.isEditing) {
        this.isEditing = false
      } else {
        this.initEditForm()
        this.isEditing = true
      }
    },
    
    // 保存编辑
    async saveEdit() {
      if (!this.editForm.reminderName) {
        uni.showToast({ title: '请输入提醒名称', icon: 'none' })
        return
      }
      
      if (!this.editForm.titleTemplate) {
        uni.showToast({ title: '请输入标题模板', icon: 'none' })
        return
      }
      
      // 构建频率配置
      const config = {}
      const f = this.editForm
      
      if (f.frequencyType === 'DAILY') {
        config.fixedTime = f.remindTime
      } else if (f.frequencyType === 'WEEKLY') {
        config.weekDays = f.weekDays
        config.fixedTime = f.remindTime
      } else if (f.frequencyType === 'MONTHLY') {
        config.monthDay = f.monthDay
        config.fixedTime = f.remindTime
      } else if (f.frequencyType === 'INTERVAL') {
        const key = f.intervalUnit === 'minutes' ? 'intervalMinutes' : 
                    f.intervalUnit === 'hours' ? 'intervalHours' : 'intervalDays'
        config[key] = parseInt(f.intervalValue) || 1
      }
      config.workDaysOnly = f.workDaysOnly
      
      const data = {
        id: this.id,
        reminderName: f.reminderName,
        reminderType: f.reminderType,
        frequencyType: f.frequencyType,
        frequencyConfig: JSON.stringify(config),
        pushScope: f.pushScope,
        targetUserIds: f.pushScope === 'SPECIFIED' ? JSON.stringify(f.targetUserIds) : null,
        titleTemplate: f.titleTemplate,
        contentTemplate: f.contentTemplate,
        remindTime: f.remindTime
      }
      
      try {
        await this.$request.post('/api/reminder/update', data)
        uni.showToast({ title: '保存成功' })
        this.isEditing = false
        this.loadDetail()
      } catch (e) {
        console.error('保存失败', e)
        uni.showToast({ title: e.message || '保存失败', icon: 'none' })
      }
    },
    
    // 立即执行
    async executeNow() {
      uni.showModal({
        title: '确认执行',
        content: '确定要立即执行这个提醒吗？',
        success: async (res) => {
          if (res.confirm) {
            try {
              await this.$request.post('/api/reminder/execute', { id: this.id })
              uni.showToast({ title: '执行成功' })
            } catch (e) {
              uni.showToast({ title: e.message || '执行失败', icon: 'none' })
            }
          }
        }
      })
    },
    
    // 删除提醒
    deleteReminder() {
      uni.showModal({
        title: '确认删除',
        content: '确定要删除这个提醒吗？删除后不可恢复。',
        confirmColor: '#ff4d4f',
        success: async (res) => {
          if (res.confirm) {
            try {
              await this.$request.post('/api/reminder/delete', { id: this.id })
              uni.showToast({ title: '删除成功' })
              setTimeout(() => uni.navigateBack(), 1000)
            } catch (e) {
              uni.showToast({ title: e.message || '删除失败', icon: 'none' })
            }
          }
        }
      })
    },
    
    // 选项变更处理
    onFreqChange(e) {
      this.editForm.frequencyType = this.freqOptions[e.detail.value].value
    },
    onTimeChange(e) {
      this.editForm.remindTime = e.detail.value
    },
    onMonthDayChange(e) {
      this.editForm.monthDay = e.detail.value + 1
    },
    onIntervalUnitChange(e) {
      const units = ['minutes', 'hours', 'days']
      this.editForm.intervalUnit = units[e.detail.value]
    },
    
    // 切换星期选择
    toggleWeekDay(value) {
      const index = this.editForm.weekDays.indexOf(value)
      if (index > -1) {
        this.editForm.weekDays.splice(index, 1)
      } else {
        this.editForm.weekDays.push(value)
      }
    },
    
    // 切换用户选择
    toggleUser(userId) {
      const index = this.editForm.targetUserIds.indexOf(userId)
      if (index > -1) {
        this.editForm.targetUserIds.splice(index, 1)
      } else {
        this.editForm.targetUserIds.push(userId)
      }
    },
    
    // 格式化方法
    getIcon(type) {
      const map = { WATER: '💧', MEDICINE: '💊', EXPIRE: '📦', BIRTHDAY: '🎂', FINANCE: '💰', SYSTEM: '⏰' }
      return map[type] || '⏰'
    },
    getIconBg(type) {
      const map = {
        WATER: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
        MEDICINE: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
        EXPIRE: 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)',
        BIRTHDAY: 'linear-gradient(135deg, #ff9a9e 0%, #fecfef 100%)',
        FINANCE: 'linear-gradient(135deg, #30cfd0 0%, #330867 100%)',
        SYSTEM: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
      }
      return map[type] || map.SYSTEM
    },
    getTypeText(type) {
      const map = { WATER: '喝水提醒', MEDICINE: '用药提醒', EXPIRE: '保质期', BIRTHDAY: '生日提醒', FINANCE: '财务提醒', SYSTEM: '系统提醒' }
      return map[type] || '其他'
    },
    getScopeText(scope) {
      const map = { SELF: '仅自己', ALL: '全部用户', SPECIFIED: '指定用户' }
      return map[scope] || scope
    },
    getResultText(result) {
      const map = { SUCCESS: '成功', FAILED: '失败', NO_TARGET: '无目标用户' }
      return map[result] || result
    },
    getResultClass(result) {
      return {
        'result-success': result === 'SUCCESS',
        'result-failed': result === 'FAILED',
        'result-warning': result === 'NO_TARGET'
      }
    },
    formatFrequency(item) {
      const typeMap = { ONCE: '一次性', DAILY: '每天', HOURLY: '每小时', WEEKLY: '每周', MONTHLY: '每月', YEARLY: '每年', INTERVAL: '间隔' }
      let text = typeMap[item.frequencyType] || item.frequencyType
      try {
        const config = JSON.parse(item.frequencyConfig || '{}')
        if (config.fixedTime) text += ` ${config.fixedTime}`
      } catch (e) {}
      return text
    },
    formatDateTime(timeStr) {
      if (!timeStr) return '-'
      const date = new Date(timeStr.replace(/-/g, '/'))
      return `${date.getMonth() + 1}月${date.getDate()}日 ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
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
}

.nav-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 60rpx 30rpx 20rpx;
  background: #fff;
  
  .back-btn, .right-btn {
    width: 100rpx;
    display: flex;
    align-items: center;
  }
  
  .back-btn { justify-content: flex-start; }
  .right-btn { justify-content: flex-end; }
  
  .icon {
    font-size: 48rpx;
    color: #333;
  }
  
  .title {
    font-size: 34rpx;
    font-weight: 600;
    color: #333;
  }
  
  .right-btn text {
    color: #667eea;
    font-size: 28rpx;
  }
}

.loading-state {
  text-align: center;
  padding: 100rpx;
  color: #999;
}

.content {
  padding: 20rpx;
  padding-bottom: 40rpx;
}

// 头部卡片
.header-card {
  background: #fff;
  border-radius: 20rpx;
  padding: 40rpx;
  text-align: center;
  margin-bottom: 20rpx;
  
  .icon-wrap {
    width: 120rpx;
    height: 120rpx;
    border-radius: 30rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 auto 20rpx;
  }
  
  .type-icon {
    font-size: 60rpx;
  }
  
  .reminder-name {
    font-size: 36rpx;
    font-weight: 600;
    color: #333;
    display: block;
    margin-bottom: 16rpx;
  }
  
  .status-badge {
    display: inline-block;
    font-size: 24rpx;
    padding: 8rpx 24rpx;
    border-radius: 20rpx;
    background: #f5f5f5;
    color: #999;
    
    &.active {
      background: #e6f7ff;
      color: #1890ff;
    }
  }
}

// 信息区块
.info-section {
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

.info-list {
  padding: 0 30rpx;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx 0;
  border-bottom: 1rpx solid #f5f5f5;
  
  &:last-child {
    border-bottom: none;
  }
  
  .label {
    font-size: 28rpx;
    color: #666;
  }
  
  .value {
    font-size: 28rpx;
    color: #333;
    text-align: right;
    
    &.result-success { color: #52c41a; }
    &.result-failed { color: #ff4d4f; }
    &.result-warning { color: #fa8c16; }
  }
}

.user-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8rpx;
  justify-content: flex-end;
  
  .user-tag {
    font-size: 22rpx;
    padding: 4rpx 16rpx;
    background: #f0f3ff;
    color: #667eea;
    border-radius: 8rpx;
  }
}

// 模板展示
.template-box {
  padding: 24rpx 30rpx;
}

.template-item {
  margin-bottom: 20rpx;
  
  &:last-child {
    margin-bottom: 0;
  }
  
  .template-label {
    font-size: 24rpx;
    color: #999;
    margin-bottom: 8rpx;
    display: block;
  }
  
  .template-content {
    font-size: 28rpx;
    color: #333;
    background: #f5f6fa;
    padding: 20rpx;
    border-radius: 12rpx;
    display: block;
    word-break: break-all;
  }
}

// 操作按钮
.action-section {
  margin-top: 40rpx;
  
  .action-btn {
    width: 100%;
    height: 88rpx;
    line-height: 88rpx;
    border-radius: 44rpx;
    font-size: 30rpx;
    margin-bottom: 20rpx;
    
    &.test-btn {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: #fff;
    }
    
    &.delete-btn {
      background: #fff;
      color: #ff4d4f;
      border: 2rpx solid #ff4d4f;
    }
  }
}

// 编辑表单样式
.form-section {
  background: #fff;
  border-radius: 16rpx;
  margin-bottom: 20rpx;
  overflow: hidden;
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

// 用户选择器
.user-selector {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
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

// 保存按钮
.save-section {
  padding: 40rpx 30rpx;
}

.save-btn {
  width: 100%;
  height: 88rpx;
  line-height: 88rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  border-radius: 44rpx;
  font-size: 30rpx;
}
</style>
