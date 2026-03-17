<template>
  <view class="page">
    <!-- 顶部导航 - Tab页面不需要返回按钮 -->
    <view class="nav-bar">
      <text class="title">提醒管理</text>
      <view class="right-btn" @click="goAdd">
        <text class="icon">+</text>
      </view>
    </view>
    
    <!-- 统计卡片 -->
    <view class="stats-card">
      <view class="stat-item">
        <text class="stat-num">{{ activeCount }}</text>
        <text class="stat-label">启用中</text>
      </view>
      <view class="stat-divider"></view>
      <view class="stat-item">
        <text class="stat-num">{{ reminders.length }}</text>
        <text class="stat-label">总计</text>
      </view>
      <view class="stat-divider"></view>
      <view class="stat-item">
        <text class="stat-num">{{ todayCount }}</text>
        <text class="stat-label">今日</text>
      </view>
    </view>
    
    <!-- 提醒列表 -->
    <scroll-view class="reminder-list" scroll-y @scrolltolower="loadMore">
      <view 
        v-for="item in reminders" 
        :key="item.id"
        class="reminder-card"
        :class="{ disabled: item.status !== 1 }"
        @click="goDetail(item)"
        @longpress="showDeleteMenu(item)"
      >
        <!-- 删除按钮 -->
        <view class="delete-icon" @click.stop="deleteReminder(item)">
          <text>🗑️</text>
        </view>
        <view class="card-left">
          <view class="type-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%)">
            <text>⏰</text>
          </view>
          <view class="status-dot" v-if="item.status === 1"></view>
        </view>
        
        <view class="card-body">
          <view class="card-header">
            <text class="reminder-name">{{ item.reminderName }}</text>
          </view>
          <view class="card-info">
            <text class="frequency">{{ formatFrequency(item) }}</text>
            <text class="next-time" v-if="item.nextExecuteTime">
              下次: {{ formatTime(item.nextExecuteTime) }}
            </text>
          </view>
          <view class="card-tags" v-if="showTags(item)">
            <text v-if="item.pushScope === 'ALL'" class="tag tag-all">全员</text>
            <text v-if="item.pushScope === 'SPECIFIED'" class="tag tag-specified">指定</text>
            <text v-if="hasQuietHours(item)" class="tag tag-quiet">免打扰</text>
            <text v-if="isWorkDaysOnly(item)" class="tag tag-workday">工作日</text>
          </view>
        </view>
        
        <view class="card-right" @click.stop="toggleStatus(item)">
          <switch 
            :checked="item.status === 1" 
            color="#667eea"
            @change.stop="onSwitchChange($event, item)"
          />
        </view>
      </view>
      
      <!-- 空状态 -->
      <view v-if="reminders.length === 0 && !loading" class="empty-state">
        <text class="empty-icon">⏰</text>
        <text class="empty-title">暂无提醒</text>
        <text class="empty-desc">点击下方按钮创建第一个提醒</text>
      </view>
      
      <!-- 加载更多 -->
      <view v-if="loading" class="loading-more">
        <text>加载中...</text>
      </view>
    </scroll-view>
    
    <!-- 底部操作栏 -->
    <view class="bottom-bar">
      <button class="add-btn" @click="goAdd">
        <text class="add-icon">+</text>
        <text>新建提醒</text>
      </button>
    </view>
    
    <!-- 编辑/新建提醒弹窗 -->
    <view v-if="showEditModal" class="modal-mask" @click="closeEditModal">
      <view class="modal-content" @click.stop>
        <!-- 弹窗头部 -->
        <view class="modal-header">
          <text class="modal-title">{{ isNew ? '新建提醒' : '编辑提醒' }}</text>
          <view class="modal-close" @click="closeEditModal">
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
                v-model="editForm.titleTemplate" 
                placeholder="请输入提醒标题"
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
            <view class="form-item" v-if="editForm.frequencyType === 'DAILY' || editForm.frequencyType === 'ONCE'">
              <text class="form-label">提醒时间</text>
              <picker mode="time" :value="editForm.remindTime" @change="onTimeChange">
                <view class="picker">
                  <text>{{ editForm.remindTime || '选择时间' }}</text>
                  <text class="picker-arrow">›</text>
                </view>
              </picker>
            </view>
            
            <!-- 一次性：选择日期 -->
            <view class="form-item" v-if="editForm.frequencyType === 'ONCE'">
              <text class="form-label">提醒日期</text>
              <picker mode="date" :value="editForm.onceDate" @change="onOnceDateChange">
                <view class="picker">
                  <text>{{ editForm.onceDate || '选择日期' }}</text>
                  <text class="picker-arrow">›</text>
                </view>
              </picker>
            </view>
            
            <!-- 每周：选择星期 -->
            <view class="form-item" v-if="editForm.frequencyType === 'WEEKLY'">
              <text class="form-label">选择星期</text>
              <view class="week-selector">
                <view 
                  v-for="day in weekDays" 
                  :key="day.value"
                  class="week-day"
                  :class="{ active: editForm.weekDays.includes(day.value) }"
                  @click="toggleWeekDay(day.value)"
                >
                  {{ day.label }}
                </view>
              </view>
            </view>
            
            <!-- 每月：选择日期 -->
            <view class="form-item" v-if="editForm.frequencyType === 'MONTHLY'">
              <text class="form-label">选择日期</text>
              <picker :range="dayOptions" :value="editForm.monthDay - 1" @change="onMonthDayChange">
                <view class="picker">
                  <text>{{ dayOptions[editForm.monthDay - 1] || '1日' }}</text>
                  <text class="picker-arrow">›</text>
                </view>
              </picker>
            </view>
            
            <!-- 仅工作日 -->
            <view class="form-item switch-item">
              <text class="form-label">仅工作日推送</text>
              <switch :checked="editForm.workDaysOnly" @change="editForm.workDaysOnly = $event.detail.value" color="#667eea" />
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
          </view>
        </scroll-view>
        
        <!-- 弹窗底部 -->
        <view class="modal-footer">
          <button class="cancel-btn" @click="closeEditModal">取消</button>
          <button class="save-btn" @click="saveReminder">保存</button>
        </view>
      </view>
    </view>

    <!-- 用户选择弹窗 -->
    <view v-if="showUserPicker" class="modal-mask" @click="closeUserPicker">
      <view class="picker-content" @click.stop>
        <view class="picker-header">
          <text class="cancel" @click="closeUserPicker">取消</text>
          <text class="title">选择指定用户</text>
          <text class="confirm" @click="confirmUserPicker">确定</text>
        </view>
        <scroll-view class="picker-body" scroll-y>
          <view 
            v-for="user in targetUsers" 
            :key="user.id"
            class="user-item"
            :class="{ selected: selectedUsers.includes(user.id) }"
            @click="toggleUserSelection(user.id)"
          >
            <image class="user-avatar" :src="user.avatar || '/static/default-avatar.png'" mode="aspectFill" />
            <text class="user-name">{{ user.nickname || user.username }}</text>
            <view v-if="selectedUsers.includes(user.id)" class="check-icon">✓</view>
          </view>
        </scroll-view>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      reminders: [],
      loading: false,
      page: 1,
      pageSize: 20,
      hasMore: true,
      
      // 弹窗显示状态
      showEditModal: false,
      
      // 是否为新建模式
      isNew: true,
      
      // 当前查看/编辑的提醒
      currentReminder: null,
      targetUsers: [],
      
      // 统一表单数据
      editForm: {
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
    activeCount() {
      return this.reminders.filter(r => r.status === 1).length
    },
    todayCount() {
      const today = new Date().toISOString().split('T')[0]
      return this.reminders.filter(r => {
        if (!r.nextExecuteTime) return false
        return r.nextExecuteTime.includes(today)
      }).length
    }
  },
  onLoad(options) {
    this.loadReminders()
    
    // 如果从首页点击提醒跳转过来，自动打开编辑弹窗
    if (options.autoOpen === 'true' && options.editId) {
      const id = parseInt(options.editId)
      // 等待列表加载完成后打开编辑
      setTimeout(() => {
        const reminder = this.reminders.find(r => r.id === id)
        if (reminder) {
          this.goDetail(reminder)
        }
      }, 300)
    }
  },
  onShow() {
    this.loadReminders()
  },
  onPullDownRefresh() {
    this.page = 1
    this.hasMore = true
    this.loadReminders().finally(() => {
      uni.stopPullDownRefresh()
    })
  },
  methods: {
    // 加载提醒列表
    async loadReminders() {
      if (this.loading) return
      this.loading = true
      
      try {
        const res = await this.$request.get('/api/reminder/list')
        this.reminders = res || []
      } catch (e) {
        console.error('加载提醒失败', e)
        uni.showToast({ title: '加载失败', icon: 'none' })
      } finally {
        this.loading = false
      }
    },
    
    // 加载更多
    loadMore() {
      // 如果需要分页，这里实现加载更多逻辑
    },
    
    // 切换状态
    async toggleStatus(item) {
      const originalStatus = item.status
      const newStatus = originalStatus === 1 ? 0 : 1
      
      try {
        await this.$request.post('/api/reminder/toggle', { id: item.id })
        item.status = newStatus
        uni.showToast({ 
          title: newStatus === 1 ? '已启用' : '已停用', 
          icon: 'none' 
        })
      } catch (e) {
        console.error('切换状态失败', e)
        uni.showToast({ title: '操作失败', icon: 'none' })
      }
    },
    
    // Switch 组件变更处理
    onSwitchChange(e, item) {
      e.preventDefault()
      e.stopPropagation()
      // 阻止默认行为，手动调用 toggle
      this.toggleStatus(item)
    },
    
    // 长按显示删除菜单
    showDeleteMenu(item) {
      uni.showActionSheet({
        itemList: ['删除提醒'],
        itemColor: '#ff4d4f',
        success: (res) => {
          if (res.tapIndex === 0) {
            this.deleteReminder(item)
          }
        }
      })
    },
    
    // 删除提醒
    async deleteReminder(item) {
      uni.showModal({
        title: '确认删除',
        content: `确定要删除「${item.reminderName || '提醒'}」吗？`,
        confirmColor: '#ff4d4f',
        success: async (res) => {
          if (res.confirm) {
            try {
              await this.$request.post('/api/reminder/delete', { id: item.id })
              uni.showToast({ title: '删除成功' })
              // 从列表中移除
              const index = this.reminders.findIndex(r => r.id === item.id)
              if (index > -1) {
                this.reminders.splice(index, 1)
              }
            } catch (e) {
              console.error('删除失败', e)
              uni.showToast({ title: e.message || '删除失败', icon: 'none' })
            }
          }
        }
      })
    },
    
    // 获取图标
    getIcon(type) {
      const map = {
        WATER: '💧',
        MEDICINE: '💊',
        EXPIRE: '📦',
        BIRTHDAY: '🎂',
        FINANCE: '💰',
        SYSTEM: '⏰'
      }
      return map[type] || '⏰'
    },
    
    // 获取图标背景色
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
    
    // 获取类型文本
    getTypeText(type) {
      const map = {
        WATER: '喝水',
        MEDICINE: '用药',
        EXPIRE: '保质期',
        BIRTHDAY: '生日',
        FINANCE: '财务',
        SYSTEM: '系统'
      }
      return map[type] || '提醒'
    },
    
    // 格式化频率
    formatFrequency(item) {
      const typeMap = {
        ONCE: '一次性',
        DAILY: '每天',
        HOURLY: '每小时',
        WEEKLY: '每周',
        MONTHLY: '每月',
        YEARLY: '每年',
        INTERVAL: '间隔'
      }
      
      let text = typeMap[item.frequencyType] || item.frequencyType
      
      // 解析频率配置获取具体时间
      try {
        const config = JSON.parse(item.frequencyConfig || '{}')
        if (config.fixedTime) {
          text += ` ${config.fixedTime}`
        } else if (config.intervalMinutes) {
          text += ` ${config.intervalMinutes}分钟`
        } else if (config.intervalHours) {
          text += ` ${config.intervalHours}小时`
        } else if (config.intervalDays) {
          text += ` ${config.intervalDays}天`
        }
      } catch (e) {}
      
      return text
    },
    
    // 格式化时间
    formatTime(time) {
      if (!time) return '-'
      // 处理数组格式 [2026,3,17,0,14,18]
      if (Array.isArray(time)) {
        const [year, month, day, hour, minute] = time
        const date = new Date(year, month - 1, day, hour, minute)
        const now = new Date()
        const isToday = date.toDateString() === now.toDateString()
        const isTomorrow = new Date(now.getTime() + 86400000).toDateString() === date.toDateString()
        const timeStr = `${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}`
        if (isToday) return `今天 ${timeStr}`
        if (isTomorrow) return `明天 ${timeStr}`
        return `${month}/${day} ${timeStr}`
      }
      // 处理字符串格式
      const date = new Date(time.replace(/-/g, '/'))
      const now = new Date()
      const isToday = date.toDateString() === now.toDateString()
      const isTomorrow = new Date(now.getTime() + 86400000).toDateString() === date.toDateString()
      const hours = date.getHours().toString().padStart(2, '0')
      const minutes = date.getMinutes().toString().padStart(2, '0')
      const timeStr = `${hours}:${minutes}`
      if (isToday) return `今天 ${timeStr}`
      if (isTomorrow) return `明天 ${timeStr}`
      return `${date.getMonth() + 1}/${date.getDate()} ${timeStr}`
    },
    
    // 显示标签
    showTags(item) {
      return item.pushScope === 'ALL' || 
             item.pushScope === 'SPECIFIED' || 
             this.hasQuietHours(item) || 
             this.isWorkDaysOnly(item)
    },
    
    // 是否有免打扰设置
    hasQuietHours(item) {
      return item.quietHoursStart && item.quietHoursEnd
    },
    
    // 是否仅工作日
    isWorkDaysOnly(item) {
      try {
        const config = JSON.parse(item.frequencyConfig || '{}')
        return config.workDaysOnly === true
      } catch (e) {
        return false
      }
    },
    
    // 导航
    goAdd() {
      this.isNew = true
      this.resetForm()
      this.showEditModal = true
    },
    goDetail(item) {
      this.isNew = false
      this.currentReminder = item
      // 初始化编辑表单
      this.editForm = {
        id: item.id,
        reminderType: item.reminderType || 'SYSTEM',
        frequencyType: item.frequencyType || 'DAILY',
        remindTime: item.remindTime || '08:00',
        titleTemplate: item.titleTemplate || '',
        contentTemplate: item.contentTemplate || '',
        pushScope: item.pushScope || 'SELF',
        onceDate: '',
        yearMonthDay: '',
        weekDays: [],
        monthDay: 1,
        intervalValue: 60,
        intervalHours: 1,
        intervalUnit: 'minutes',
        workDaysOnly: false,
        targetUserIds: []
      }
      this.showEditModal = true
    },
    
    // 获取推送范围文本
    getScopeText(scope) {
      const map = { 'SELF': '仅自己', 'ALL': '全部用户', 'SPECIFIED': '指定用户' }
      return map[scope] || '仅自己'
    },
    
    // 关闭弹窗
    closeEditModal() {
      this.showEditModal = false
      this.resetForm()
    },
    
    // 重置表单
    resetForm() {
      this.editForm = {
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
    },
    
    // 保存提醒（统一方法）
    async saveReminder() {
      if (!this.editForm.titleTemplate) {
        uni.showToast({ title: '请输入提醒标题', icon: 'none' })
        return
      }
      
      // 构建频率配置
      const config = {}
      const f = this.editForm
      
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
        if (this.isNew) {
          // 新建提醒
          await this.$request.post('/api/reminder/add', data)
          uni.showToast({ title: '创建成功' })
        } else {
          // 编辑提醒
          data.id = f.id
          await this.$request.post('/api/reminder/update', data)
          uni.showToast({ title: '保存成功' })
        }
        this.closeEditModal()
        this.loadReminders()
      } catch (e) {
        console.error('保存失败', e)
        uni.showToast({ title: '保存失败', icon: 'none' })
      }
    },
    
    // 弹窗事件处理
    onFreqChange(e) {
      this.editForm.frequencyType = this.freqOptions[e.detail.value].value
    },
    onTimeChange(e) {
      this.editForm.remindTime = e.detail.value
    },
    onOnceDateChange(e) {
      this.editForm.onceDate = e.detail.value
    },
    onMonthDayChange(e) {
      this.editForm.monthDay = e.detail.value + 1
    },
    toggleWeekDay(value) {
      const index = this.editForm.weekDays.indexOf(value)
      if (index > -1) {
        this.editForm.weekDays.splice(index, 1)
      } else {
        this.editForm.weekDays.push(value)
      }
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

// 导航栏
.nav-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 60rpx 30rpx 20rpx;
  background: #fff;
  
  .back-btn, .right-btn {
    width: 60rpx;
    height: 60rpx;
    display: flex;
    align-items: center;
    justify-content: center;
  }
  
  .icon {
    font-size: 48rpx;
    color: #333;
  }
  
  .title {
    font-size: 34rpx;
    font-weight: 600;
    color: #333;
  }
}

// 统计卡片
.stats-card {
  display: flex;
  align-items: center;
  justify-content: space-around;
  margin: 20rpx;
  padding: 30rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 20rpx;
  
  .stat-item {
    display: flex;
    flex-direction: column;
    align-items: center;
  }
  
  .stat-num {
    font-size: 48rpx;
    font-weight: 700;
    color: #fff;
  }
  
  .stat-label {
    font-size: 24rpx;
    color: rgba(255,255,255,0.8);
    margin-top: 8rpx;
  }
  
  .stat-divider {
    width: 2rpx;
    height: 60rpx;
    background: rgba(255,255,255,0.3);
  }
}

// 列表
.reminder-list {
  flex: 1;
  padding: 0 20rpx;
  padding-bottom: 120rpx;
}

.reminder-card {
  display: flex;
  align-items: center;
  background: #fff;
  padding: 24rpx;
  border-radius: 16rpx;
  margin-bottom: 16rpx;
  position: relative;
  
  // 删除按钮
  .delete-icon {
    position: absolute;
    top: 12rpx;
    right: 12rpx;
    width: 48rpx;
    height: 48rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 28rpx;
    opacity: 0.6;
    transition: opacity 0.2s;
    z-index: 10;
    
    &:active {
      opacity: 1;
      transform: scale(1.1);
    }
  }
  box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.04);
  
  &.disabled {
    opacity: 0.6;
  }
}

.card-left {
  position: relative;
  margin-right: 20rpx;
  
  .type-icon {
    width: 88rpx;
    height: 88rpx;
    border-radius: 20rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 44rpx;
  }
  
  .status-dot {
    position: absolute;
    top: -4rpx;
    right: -4rpx;
    width: 20rpx;
    height: 20rpx;
    background: #52c41a;
    border-radius: 50%;
    border: 4rpx solid #fff;
  }
}

.card-body {
  flex: 1;
  min-width: 0;
}

.card-header {
  display: flex;
  align-items: center;
  margin-bottom: 8rpx;
  
  .reminder-name {
    font-size: 30rpx;
    font-weight: 600;
    color: #333;
    margin-right: 12rpx;
  }
  
  .reminder-type {
    font-size: 22rpx;
    color: #667eea;
    background: rgba(102,126,234,0.1);
    padding: 4rpx 12rpx;
    border-radius: 8rpx;
  }
}

.card-info {
  display: flex;
  flex-direction: column;
  gap: 4rpx;
  
  .frequency {
    font-size: 26rpx;
    color: #666;
  }
  
  .next-time {
    font-size: 24rpx;
    color: #999;
  }
}

.card-tags {
  display: flex;
  gap: 8rpx;
  margin-top: 12rpx;
  flex-wrap: wrap;
  
  .tag {
    font-size: 20rpx;
    padding: 4rpx 12rpx;
    border-radius: 8rpx;
  }
  
  .tag-all {
    background: #e6f7ff;
    color: #1890ff;
  }
  
  .tag-specified {
    background: #f6ffed;
    color: #52c41a;
  }
  
  .tag-quiet {
    background: #fff7e6;
    color: #fa8c16;
  }
  
  .tag-workday {
    background: #f9f0ff;
    color: #722ed1;
  }
}

.card-right {
  margin-left: 16rpx;
}

// 空状态
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 100rpx 40rpx;
  
  .empty-icon {
    font-size: 120rpx;
    margin-bottom: 20rpx;
  }
  
  .empty-title {
    font-size: 32rpx;
    font-weight: 600;
    color: #333;
    margin-bottom: 12rpx;
  }
  
  .empty-desc {
    font-size: 26rpx;
    color: #999;
  }
}

// 底部栏
.bottom-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  padding: 20rpx 30rpx;
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
  background: #fff;
  border-top: 1rpx solid #f0f0f0;
}

.add-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  width: 100%;
  height: 88rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 44rpx;
  color: #fff;
  font-size: 30rpx;
  font-weight: 500;
  
  .add-icon {
    font-size: 36rpx;
  }
}

.loading-more {
  text-align: center;
  padding: 30rpx;
  color: #999;
  font-size: 24rpx;
}

// 弹窗样式
.modal-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 1000;
  display: flex;
  align-items: flex-end;
}

.modal-content {
  width: 100%;
  max-height: 90vh;
  background: #fff;
  border-radius: 32rpx 32rpx 0 0;
  display: flex;
  flex-direction: column;
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 30rpx;
  border-bottom: 1rpx solid #f0f0f0;
  
  .modal-title {
    font-size: 32rpx;
    font-weight: 600;
    color: #333;
  }
  
  .modal-close {
    width: 60rpx;
    height: 60rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    
    text {
      font-size: 36rpx;
      color: #999;
    }
  }
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
  
  .cancel-btn, .save-btn {
    flex: 1;
    height: 80rpx;
    border-radius: 40rpx;
    font-size: 30rpx;
    display: flex;
    align-items: center;
    justify-content: center;
  }
  
  .cancel-btn {
    background: #f5f5f5;
    color: #666;
  }
  
  .save-btn {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: #fff;
  }
}

// 表单样式
.form-section {
  padding: 0 30rpx 30rpx;
  
  .section-title {
    font-size: 28rpx;
    font-weight: 600;
    color: #333;
    padding: 20rpx 0;
  }
}

.form-item {
  margin-bottom: 24rpx;
  
  .form-label {
    display: block;
    font-size: 26rpx;
    color: #666;
    margin-bottom: 12rpx;
    
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
    align-items: center;
    justify-content: space-between;
    
    text {
      color: #333;
    }
    
    .picker-arrow {
      color: #999;
      font-size: 32rpx;
    }
  }
}

.week-selector {
  display: flex;
  gap: 12rpx;
  
  .week-day {
    width: 64rpx;
    height: 64rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #f5f6fa;
    border-radius: 50%;
    font-size: 24rpx;
    color: #666;
    
    &.active {
      background: #667eea;
      color: #fff;
    }
  }
}

.radio-group {
  display: flex;
  gap: 20rpx;
  
  .radio-item {
    display: flex;
    align-items: center;
    gap: 12rpx;
    padding: 16rpx 24rpx;
    background: #f5f6fa;
    border-radius: 12rpx;
    
    &.active {
      background: #e8f0ff;
    }
    
    .radio-circle {
      width: 32rpx;
      height: 32rpx;
      border: 2rpx solid #ddd;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      
      .radio-dot {
        width: 20rpx;
        height: 20rpx;
        background: #667eea;
        border-radius: 50%;
      }
    }
    
    text {
      font-size: 26rpx;
      color: #333;
    }
  }
}

.switch-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

// 详情弹窗样式
.detail-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40rpx 30rpx;
  
  .detail-icon {
    width: 120rpx;
    height: 120rpx;
    border-radius: 60rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 20rpx;
    
    text {
      font-size: 60rpx;
    }
  }
  
  .detail-title {
    font-size: 36rpx;
    font-weight: 600;
    color: #333;
    margin-bottom: 16rpx;
  }
  
  .detail-status {
    font-size: 24rpx;
    padding: 8rpx 24rpx;
    border-radius: 24rpx;
    background: #f5f5f5;
    color: #999;
    
    &.active {
      background: #e6f7ff;
      color: #1890ff;
    }
  }
}

.detail-section {
  padding: 0 30rpx 30rpx;
  
  .section-title {
    font-size: 28rpx;
    font-weight: 600;
    color: #333;
    padding: 20rpx 0;
  }
}

.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #f5f5f5;
  
  &:last-child {
    border-bottom: none;
  }
  
  .detail-label {
    font-size: 26rpx;
    color: #666;
  }
  
  .detail-value {
    font-size: 28rpx;
    color: #333;
    max-width: 400rpx;
    text-align: right;
  }
}

.loading-state {
  text-align: center;
  padding: 100rpx;
  color: #999;
  font-size: 28rpx;
}
</style>
