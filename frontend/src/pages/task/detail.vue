<template>
  <view class="task-detail-page">
    <!-- 顶部导航 -->
    <view class="nav-bar">
      <view class="back-btn" @click="goBack">
        <up-icon name="arrow-left" size="40" color="#333"></up-icon>
      </view>
      <text class="title">任务详情</text>
      <view class="more-btn" @click="showActions">
        <up-icon name="more-dot-fill" size="40" color="#333"></up-icon>
      </view>
    </view>

    <!-- 页面内容 -->
    <scroll-view class="content-scroll" scroll-y enhanced :show-scrollbar="false">
      <!-- 任务标题卡片 -->
      <view class="task-title-card" :class="{ completed: task.status === 2 }">
        <view class="title-section">
          <view class="checkbox" :class="{ checked: task.status === 2 }" @click="toggleTaskStatus">
            <up-icon v-if="task.status === 2" name="checkmark" size="36" color="#fff"></up-icon>
          </view>
          <view class="title-content">
            <text class="task-title-text" :class="{ 'line-through': task.status === 2 }">
              {{ task.title }}
            </text>
            <view class="status-tag" :class="getStatusClass">
              <text>{{ getStatusText }}</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 任务属性卡片 -->
      <view class="info-card">
        <!-- 截止时间 -->
        <view class="info-item" @click="editDeadline">
          <view class="info-icon deadline">
            <up-icon name="clock" size="32" color="#fff"></up-icon>
          </view>
          <view class="info-content">
            <text class="info-label">截止时间</text>
            <text class="info-value">{{ formatDeadline }}</text>
          </view>
          <up-icon name="arrow-right" size="28" color="#ccc"></up-icon>
        </view>

        <!-- 分割线 -->
        <view class="divider"></view>

        <!-- 优先级 -->
        <view class="info-item" @click="editPriority">
          <view class="info-icon priority" :class="getPriorityBgClass">
            <up-icon name="flag" size="32" color="#fff"></up-icon>
          </view>
          <view class="info-content">
            <text class="info-label">优先级</text>
            <view class="priority-tag" :class="getPriorityClass">
              <view class="priority-dot"></view>
              <text>{{ getPriorityText }}</text>
            </view>
          </view>
          <up-icon name="arrow-right" size="28" color="#ccc"></up-icon>
        </view>

        <!-- 分割线 -->
        <view class="divider"></view>

        <!-- 分类 -->
        <view class="info-item" @click="editCategory">
          <view class="info-icon category">
            <up-icon name="grid" size="32" color="#fff"></up-icon>
          </view>
          <view class="info-content">
            <text class="info-label">任务分类</text>
            <text class="info-value">{{ getCategoryText }}</text>
          </view>
          <up-icon name="arrow-right" size="28" color="#ccc"></up-icon>
        </view>
      </view>

      <!-- 备注卡片 -->
      <view class="remark-card" v-if="task.remark">
        <view class="card-header">
          <up-icon name="file-text" size="32" color="#999"></up-icon>
          <text>备注</text>
        </view>
        <view class="remark-content">
          {{ task.remark }}
        </view>
      </view>

      <!-- 备注卡片（空状态） -->
      <view class="remark-card empty" v-else @click="addRemark">
        <view class="card-header">
          <up-icon name="file-text" size="32" color="#999"></up-icon>
          <text>备注</text>
        </view>
        <view class="add-remark">
          <up-icon name="plus" size="32" color="#5AD8A6"></up-icon>
          <text>添加备注</text>
        </view>
      </view>

      <!-- 提醒设置卡片 -->
      <view class="reminder-card" @click="showReminderModal">
        <view class="card-header">
          <view class="header-left">
            <up-icon name="bell" size="32" color="#5B8FF9"></up-icon>
            <text>提醒设置</text>
          </view>
          <view class="header-right">
            <up-icon name="arrow-right" size="28" color="#ccc"></up-icon>
          </view>
        </view>

        <view class="reminder-list" v-if="reminders.length > 0">
          <view v-for="(reminder, index) in reminders" :key="reminder.id" class="reminder-item">
            <view class="reminder-icon-wrapper" :class="reminder.reminderType">
              <up-icon :name="reminder.reminderType === 'location' ? 'map' : 'clock'" size="28" color="#fff"></up-icon>
            </view>
            <view class="reminder-info">
              <text class="reminder-text">{{ formatReminder(reminder) }}</text>
              <text v-if="reminder.reminderType === 'location' && reminder.locationName" class="reminder-subtext">{{ reminder.locationName }}</text>
            </view>
          </view>
        </view>
        <view class="no-reminder" v-else>
          <up-icon name="plus-circle" size="48" color="#ddd"></up-icon>
          <text>点击添加提醒</text>
        </view>
      </view>

      <!-- 创建时间 -->
      <view class="create-time">
        <text>创建于 {{ formatCreateTime }}</text>
      </view>
    </scroll-view>

    <!-- 底部操作栏 -->
    <view class="bottom-actions">
      <view v-if="task.status !== 2" class="action-btn btn-complete" @click="completeTask">
        <up-icon name="checkmark-circle-2" size="36" color="#fff"></up-icon>
        <text>标记完成</text>
      </view>
      <view v-else class="action-btn btn-reopen" @click="reopenTask">
        <up-icon name="refresh-circle" size="36" color="#5B8FF9"></up-icon>
        <text>重新打开</text>
      </view>

      <view class="action-btn btn-delete" @click="deleteTask">
        <up-icon name="trash-2" size="36" color="#FF4D4F"></up-icon>
        <text>删除任务</text>
      </view>
    </view>

    <!-- 提醒设置弹窗 -->
    <view v-if="reminderModalVisible" class="modal-overlay" @click="closeReminderModal">
      <view class="modal-content" @click.stop>
        <view class="modal-header">
          <text class="modal-title">设置提醒</text>
          <text class="close-btn" @click="closeReminderModal">✕</text>
        </view>

        <!-- 提醒类型选择 -->
        <view class="reminder-type-tabs">
          <view
            v-for="(type, index) in reminderTypes"
            :key="type.id || index"
            class="type-tab"
            :class="{ active: newReminder.reminderType === type.value }"
            @click="newReminder.reminderType = type.value"
          >
            <up-icon :name="type.icon" size="36" :color="newReminder.reminderType === type.value ? '#fff' : '#666'"></up-icon>
            <text>{{ type.label }}</text>
          </view>
        </view>

        <!-- 时间提醒设置 -->
        <view v-if="newReminder.reminderType === 'time'" class="form-section">
          <view class="form-item">
            <text class="form-label">提醒时间</text>
            <picker mode="multiSelector" :range="timeRange" :value="timeIndex" @change="onTimeChange">
              <view class="picker-value">
                {{ newReminder.reminderTime || '选择时间' }}
                <up-icon name="arrow-right" size="24" color="#ccc"></up-icon>
              </view>
            </picker>
          </view>

          <view class="form-item">
            <text class="form-label">提前提醒</text>
            <view class="chip-options">
              <view
                v-for="(adv, index) in advanceOptions"
                :key="adv.id || index"
                class="chip"
                :class="{ active: newReminder.advanceMinutes === adv.value }"
                @click="newReminder.advanceMinutes = adv.value"
              >
                {{ adv.label }}
              </view>
            </view>
          </view>
        </view>

        <!-- 位置提醒设置 -->
        <view v-if="newReminder.reminderType === 'location'" class="form-section">
          <view class="form-item">
            <text class="form-label">选择位置</text>
            <view class="location-picker" @click="chooseLocation">
              <view v-if="!newReminder.locationName" class="location-placeholder">
                <up-icon name="map" size="48" color="#ccc"></up-icon>
                <text>点击选择地图位置</text>
              </view>
              <view v-else class="location-selected">
                <up-icon name="map-fill" size="40" color="#5B8FF9"></up-icon>
                <view class="location-info">
                  <text class="location-name">{{ newReminder.locationName }}</text>
                  <text class="location-address" v-if="newReminder.locationAddress">{{ newReminder.locationAddress }}</text>
                </view>
              </view>
            </view>
          </view>

          <view class="form-item">
            <text class="form-label">提醒范围</text>
            <view class="chip-options">
              <view
                v-for="(r, index) in radiusOptions"
                :key="r.id || index"
                class="chip"
                :class="{ active: newReminder.radius === r.value }"
                @click="newReminder.radius = r.value"
              >
                {{ r.label }}
              </view>
            </view>
          </view>
        </view>

        <!-- 已设置的提醒列表 -->
        <view v-if="reminders.length > 0" class="existing-reminders">
          <text class="section-title">已设置的提醒</text>
          <view v-for="(reminder, index) in reminders" :key="reminder.id" class="existing-item">
            <view class="existing-info">
              <up-icon :name="reminder.reminderType === 'location' ? 'map' : 'clock'" size="28" color="#5B8FF9"></up-icon>
              <text>{{ formatReminder(reminder) }}</text>
            </view>
            <view class="delete-btn" @click="deleteReminder(reminder.id)">
              <up-icon name="trash" size="28" color="#FF4D4F"></up-icon>
            </view>
          </view>
        </view>

        <view class="modal-actions">
          <button class="btn-cancel" @click="closeReminderModal">取消</button>
          <button class="btn-confirm" @click="saveReminder">添加提醒</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { taskApi } from '../../api/index.js'

const taskId = ref(null)
const task = ref({
  id: 1,
  title: '买牛奶和鸡蛋',
  status: 0,
  deadline: '2026-02-21 18:00',
  category: 'shopping',
  priority: 1,
  createTime: '2026-02-21 09:00',
  remark: '记得买有机牛奶，鸡蛋要土鸡蛋'
})
const loading = ref(false)

// 提醒相关数据
const reminders = ref([])
const reminderModalVisible = ref(false)
const newReminder = ref({
  reminderType: 'time',
  reminderTime: '',
  advanceMinutes: 0,
  locationName: '',
  locationAddress: '',
  latitude: null,
  longitude: null,
  radius: 500
})

const reminderTypes = [
  { label: '时间提醒', value: 'time', icon: 'clock' },
  { label: '位置提醒', value: 'location', icon: 'map' }
]

const advanceOptions = [
  { label: '准时', value: 0 },
  { label: '5分钟前', value: 5 },
  { label: '15分钟前', value: 15 },
  { label: '30分钟前', value: 30 },
  { label: '1小时前', value: 60 }
]

const radiusOptions = [
  { label: '100米', value: 100 },
  { label: '200米', value: 200 },
  { label: '500米', value: 500 },
  { label: '1公里', value: 1000 }
]

// 时间选择器数据
const timeRange = ref([
  Array.from({ length: 24 }, (_, i) => String(i).padStart(2, '0') + '时'),
  Array.from({ length: 60 }, (_, i) => String(i).padStart(2, '0') + '分')
])
const timeIndex = ref([9, 0])

onMounted(() => {
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  taskId.value = currentPage.options.id
  // 加载任务详情
  if (taskId.value) {
    loadTaskDetail(taskId.value)
    loadReminders(taskId.value)
  }
})

// 加载任务详情
const loadTaskDetail = async (id) => {
  loading.value = true
  try {
    const res = await taskApi.getById(id)
    if (res) {
      task.value = res
    }
  } catch (e) {
    console.error('加载任务详情失败', e)
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

// 格式化截止时间
const formatDeadline = computed(() => {
  if (!task.value.deadline) return '未设置'
  const date = new Date(task.value.deadline)
  const now = new Date()
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  const tomorrow = new Date(today)
  tomorrow.setDate(tomorrow.getDate() + 1)

  const dateStr = `${date.getMonth() + 1}月${date.getDate()}日`
  const timeStr = `${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`

  if (date >= today && date < tomorrow) {
    return `今天 ${timeStr}`
  }
  const nextDay = new Date(tomorrow)
  if (date >= tomorrow && date < new Date(nextDay.setDate(nextDay.getDate() + 1))) {
    return `明天 ${timeStr}`
  }
  return `${dateStr} ${timeStr}`
})

// 格式化创建时间
const formatCreateTime = computed(() => {
  if (!task.value.createTime) return ''
  const date = new Date(task.value.createTime)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
})

// 加载提醒列表
const loadReminders = async (id) => {
  try {
    const res = await taskApi.getReminders(id)
    reminders.value = res || []
  } catch (e) {
    console.error('加载提醒失败', e)
  }
}

// 显示提醒弹窗
const showReminderModal = () => {
  reminderModalVisible.value = true
  resetNewReminder()
}

// 关闭提醒弹窗
const closeReminderModal = () => {
  reminderModalVisible.value = false
}

// 重置新提醒
const resetNewReminder = () => {
  newReminder.value = {
    reminderType: 'time',
    reminderTime: '',
    advanceMinutes: 0,
    locationName: '',
    locationAddress: '',
    latitude: null,
    longitude: null,
    radius: 500
  }
  timeIndex.value = [9, 0]
}

// 时间选择变化
const onTimeChange = (e) => {
  const [hourIndex, minuteIndex] = e.detail.value
  const hour = String(hourIndex).padStart(2, '0')
  const minute = String(minuteIndex).padStart(2, '0')
  newReminder.value.reminderTime = `${hour}:${minute}`
  timeIndex.value = [hourIndex, minuteIndex]
}

// 选择地图位置
const chooseLocation = () => {
  uni.chooseLocation({
    success: (res) => {
      newReminder.value.locationName = res.name || '选定位置'
      newReminder.value.locationAddress = res.address
      newReminder.value.latitude = res.latitude
      newReminder.value.longitude = res.longitude
    },
    fail: (err) => {
      console.error('选择位置失败', err)
      uni.showToast({ title: '选择位置失败', icon: 'none' })
    }
  })
}

// 格式化提醒显示
const formatReminder = (reminder) => {
  if (reminder.reminderType === 'time') {
    const advance = advanceOptions.find(a => a.value === reminder.advanceMinutes)
    const advanceText = advance && advance.value > 0 ? `${advance.label} · ` : ''
    return `${advanceText}${reminder.reminderTime}`
  } else {
    const radius = radiusOptions.find(r => r.value === reminder.radius)
    return `进入${radius ? radius.label : '附近'}时提醒`
  }
}

// 保存提醒
const saveReminder = async () => {
  // 验证
  if (newReminder.value.reminderType === 'time' && !newReminder.value.reminderTime) {
    uni.showToast({ title: '请选择提醒时间', icon: 'none' })
    return
  }
  if (newReminder.value.reminderType === 'location' && !newReminder.value.locationName) {
    uni.showToast({ title: '请选择提醒位置', icon: 'none' })
    return
  }

  try {
    const data = {
      reminderType: newReminder.value.reminderType,
      reminderTime: newReminder.value.reminderType === 'time' ? newReminder.value.reminderTime : null,
      advanceMinutes: newReminder.value.advanceMinutes,
      locationName: newReminder.value.locationName,
      locationAddress: newReminder.value.locationAddress,
      latitude: newReminder.value.latitude,
      longitude: newReminder.value.longitude,
      radius: newReminder.value.radius
    }

    await taskApi.setReminder(taskId.value, data)
    uni.showToast({ title: '添加成功', icon: 'success' })
    await loadReminders(taskId.value)
    resetNewReminder()
  } catch (e) {
    console.error('保存提醒失败', e)
    uni.showToast({ title: '添加失败', icon: 'none' })
  }
}

// 删除提醒
const deleteReminder = async (reminderId) => {
  uni.showModal({
    title: '确认删除',
    content: '确定删除这个提醒吗？',
    confirmColor: '#FF4D4F',
    success: async (res) => {
      if (res.confirm) {
        try {
          await taskApi.deleteReminder(reminderId)
          uni.showToast({ title: '已删除', icon: 'success' })
          await loadReminders(taskId.value)
        } catch (e) {
          console.error('删除提醒失败', e)
          uni.showToast({ title: '删除失败', icon: 'none' })
        }
      }
    }
  })
}

const getStatusClass = computed(() => {
  const map = { 0: 'pending', 1: 'progress', 2: 'completed' }
  return map[task.value.status] || 'pending'
})

const getStatusText = computed(() => {
  const map = { 0: '待处理', 1: '进行中', 2: '已完成' }
  return map[task.value.status] || '待处理'
})

const getCategoryText = computed(() => {
  const map = {
    shopping: '购物',
    housework: '家务',
    finance: '财务',
    parenting: '育儿',
    other: '其他'
  }
  return map[task.value.category] || '其他'
})

const getPriorityBgClass = computed(() => {
  const map = { 0: 'bg-normal', 1: 'bg-medium', 2: 'bg-high' }
  return map[task.value.priority] || 'bg-normal'
})

const getPriorityClass = computed(() => {
  const map = { 0: 'normal', 1: 'medium', 2: 'high' }
  return map[task.value.priority] || 'normal'
})

const getPriorityText = computed(() => {
  const map = { 0: '普通', 1: '重要', 2: '紧急' }
  return map[task.value.priority] || '普通'
})

// 切换任务状态（点击复选框）
const toggleTaskStatus = () => {
  if (task.value.status === 2) {
    reopenTask()
  } else {
    completeTask()
  }
}

const goBack = () => {
  uni.navigateBack()
}

const showActions = () => {
  uni.showActionSheet({
    itemList: ['编辑任务', '删除任务'],
    itemColor: '#333',
    success: (res) => {
      if (res.tapIndex === 0) {
        uni.navigateTo({ url: `/pages/task/edit?id=${taskId.value}` })
      } else if (res.tapIndex === 1) {
        deleteTask()
      }
    }
  })
}

// 编辑截止时间
const editDeadline = () => {
  uni.showToast({ title: '编辑功能开发中', icon: 'none' })
}

// 编辑优先级
const editPriority = () => {
  uni.showToast({ title: '编辑功能开发中', icon: 'none' })
}

// 编辑分类
const editCategory = () => {
  uni.showToast({ title: '编辑功能开发中', icon: 'none' })
}

// 添加备注
const addRemark = () => {
  uni.showToast({ title: '编辑功能开发中', icon: 'none' })
}

const completeTask = async () => {
  try {
    await taskApi.complete(taskId.value)
    task.value.status = 2
    uni.showToast({ title: '已完成', icon: 'success' })
  } catch (e) {
    console.error('完成任务失败', e)
    uni.showToast({ title: '操作失败', icon: 'none' })
  }
}

const reopenTask = async () => {
  try {
    await taskApi.update({ id: taskId.value, status: 0 })
    task.value.status = 0
    uni.showToast({ title: '已重新打开', icon: 'success' })
  } catch (e) {
    console.error('重新打开任务失败', e)
    uni.showToast({ title: '操作失败', icon: 'none' })
  }
}

const deleteTask = async () => {
  uni.showModal({
    title: '确认删除',
    content: '删除后无法恢复，是否继续？',
    confirmColor: '#FF4D4F',
    success: async (res) => {
      if (res.confirm) {
        try {
          await taskApi.delete(taskId.value)
          uni.showToast({ title: '已删除', icon: 'success' })
          setTimeout(() => uni.navigateBack(), 1500)
        } catch (e) {
          console.error('删除任务失败', e)
          uni.showToast({ title: '删除失败', icon: 'none' })
        }
      }
    }
  })
}
</script>

<style lang="scss" scoped>
.task-detail-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #f0f4ff 0%, #f8f9fc 100%);
  display: flex;
  flex-direction: column;
}

// 顶部导航
.nav-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 30rpx;
  padding-top: 60rpx;
  background: transparent;

  .back-btn, .more-btn {
    width: 72rpx;
    height: 72rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    background: rgba(255, 255, 255, 0.8);
    border-radius: 50%;
    backdrop-filter: blur(10px);
  }

  .title {
    font-size: 34rpx;
    font-weight: 600;
    color: #333;
  }
}

// 内容滚动区域
.content-scroll {
  flex: 1;
  padding: 0 30rpx;
  padding-bottom: 180rpx;
}

// 任务标题卡片
.task-title-card {
  background: linear-gradient(135deg, #5B8FF9 0%, #5AD8A6 100%);
  border-radius: 28rpx;
  padding: 40rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 8rpx 32rpx rgba(91, 143, 249, 0.2);

  &.completed {
    background: linear-gradient(135deg, #95a5a6 0%, #bdc3c7 100%);
    box-shadow: 0 8rpx 32rpx rgba(149, 165, 166, 0.2);
  }

  .title-section {
    display: flex;
    align-items: flex-start;
    gap: 24rpx;
  }

  .checkbox {
    width: 56rpx;
    height: 56rpx;
    border-radius: 50%;
    border: 4rpx solid rgba(255, 255, 255, 0.5);
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
    transition: all 0.2s ease;

    &.checked {
      background: #52C41A;
      border-color: #52C41A;
    }

    &:active {
      transform: scale(0.9);
    }
  }

  .title-content {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 16rpx;
  }

  .task-title-text {
    font-size: 40rpx;
    font-weight: 600;
    color: #fff;
    line-height: 1.4;

    &.line-through {
      text-decoration: line-through;
      opacity: 0.7;
    }
  }

  .status-tag {
    align-self: flex-start;
    padding: 8rpx 20rpx;
    border-radius: 24rpx;
    background: rgba(255, 255, 255, 0.2);

    text {
      font-size: 24rpx;
      color: #fff;
      font-weight: 500;
    }

    &.pending {
      background: rgba(24, 144, 255, 0.3);
    }

    &.progress {
      background: rgba(250, 173, 20, 0.3);
    }

    &.completed {
      background: rgba(82, 196, 26, 0.3);
    }
  }
}

// 信息卡片
.info-card {
  background: #fff;
  border-radius: 24rpx;
  padding: 16rpx 0;
  margin-bottom: 24rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);
}

.info-item {
  display: flex;
  align-items: center;
  padding: 24rpx 30rpx;

  &:active {
    background: rgba(0, 0, 0, 0.02);
  }

  .info-icon {
    width: 64rpx;
    height: 64rpx;
    border-radius: 16rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;

    &.deadline {
      background: linear-gradient(135deg, #1890FF, #36CFC9);
    }

    &.bg-normal {
      background: linear-gradient(135deg, #52C41A, #95DE64);
    }

    &.bg-medium {
      background: linear-gradient(135deg, #FAAD14, #FFD666);
    }

    &.bg-high {
      background: linear-gradient(135deg, #FF4D4F, #FF7875);
    }

    &.category {
      background: linear-gradient(135deg, #722ED1, #B37FEB);
    }
  }

  .info-content {
    flex: 1;
    margin-left: 24rpx;
    display: flex;
    flex-direction: column;
    gap: 8rpx;
  }

  .info-label {
    font-size: 24rpx;
    color: #999;
  }

  .info-value {
    font-size: 30rpx;
    color: #333;
    font-weight: 500;
  }

  .priority-tag {
    display: inline-flex;
    align-items: center;
    gap: 8rpx;
    align-self: flex-start;
    padding: 8rpx 16rpx;
    border-radius: 8rpx;
    font-size: 26rpx;
    font-weight: 500;

    &.normal {
      background: #E6F7FF;
      color: #1890FF;
      .priority-dot { background: #1890FF; }
    }

    &.medium {
      background: #FFF7E6;
      color: #FAAD14;
      .priority-dot { background: #FAAD14; }
    }

    &.high {
      background: #FFF1F0;
      color: #FF4D4F;
      .priority-dot { background: #FF4D4F; }
    }

    .priority-dot {
      width: 12rpx;
      height: 12rpx;
      border-radius: 50%;
    }
  }
}

.divider {
  height: 1rpx;
  background: linear-gradient(90deg, transparent, #eee, transparent);
  margin: 0 30rpx;
}

// 备注卡片
.remark-card {
  background: #fff;
  border-radius: 24rpx;
  padding: 30rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);

  .card-header {
    display: flex;
    align-items: center;
    gap: 12rpx;
    margin-bottom: 20rpx;

    text {
      font-size: 28rpx;
      font-weight: 600;
      color: #333;
    }
  }

  .remark-content {
    font-size: 28rpx;
    color: #666;
    line-height: 1.6;
    padding-left: 44rpx;
  }

  &.empty {
    .add-remark {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 12rpx;
      padding: 40rpx;
      margin-left: 44rpx;
      border: 2rpx dashed #ddd;
      border-radius: 16rpx;

      text {
        font-size: 28rpx;
        color: #999;
      }

      &:active {
        background: #f9f9f9;
      }
    }
  }
}

// 提醒卡片
.reminder-card {
  background: #fff;
  border-radius: 24rpx;
  padding: 30rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);

  .card-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 20rpx;

    .header-left {
      display: flex;
      align-items: center;
      gap: 12rpx;

      text {
        font-size: 28rpx;
        font-weight: 600;
        color: #333;
      }
    }
  }

  .reminder-list {
    display: flex;
    flex-direction: column;
    gap: 16rpx;
  }

  .reminder-item {
    display: flex;
    align-items: center;
    gap: 20rpx;
    padding: 20rpx;
    background: #f8f9fa;
    border-radius: 16rpx;

    .reminder-icon-wrapper {
      width: 56rpx;
      height: 56rpx;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;

      &.time {
        background: linear-gradient(135deg, #5B8FF9, #85A5FF);
      }

      &.location {
        background: linear-gradient(135deg, #5AD8A6, #87E8DE);
      }
    }

    .reminder-info {
      flex: 1;
      display: flex;
      flex-direction: column;
      gap: 4rpx;
    }

    .reminder-text {
      font-size: 28rpx;
      color: #333;
      font-weight: 500;
    }

    .reminder-subtext {
      font-size: 24rpx;
      color: #999;
    }
  }

  .no-reminder {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 16rpx;
    padding: 60rpx 0;

    text {
      font-size: 28rpx;
      color: #999;
    }
  }
}

// 创建时间
.create-time {
  text-align: center;
  padding: 30rpx 0 40rpx;

  text {
    font-size: 24rpx;
    color: #bbb;
  }
}

// 底部操作栏
.bottom-actions {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #fff;
  padding: 20rpx 40rpx 50rpx;
  display: flex;
  gap: 24rpx;
  box-shadow: 0 -4rpx 30rpx rgba(0, 0, 0, 0.08);
  border-radius: 32rpx 32rpx 0 0;

  .action-btn {
    flex: 1;
    height: 96rpx;
    border-radius: 48rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 12rpx;
    font-size: 30rpx;
    font-weight: 500;
    transition: all 0.2s ease;

    &:active {
      transform: scale(0.98);
    }

    &.btn-complete {
      background: linear-gradient(135deg, #5AD8A6, #52C41A);
      color: #fff;
      box-shadow: 0 8rpx 20rpx rgba(90, 216, 166, 0.3);
    }

    &.btn-reopen {
      background: #E6F7FF;
      color: #1890FF;
      border: 2rpx solid #91D5FF;
    }

    &.btn-delete {
      background: #FFF1F0;
      color: #FF4D4F;
      border: 2rpx solid #FFCCC7;
    }
  }
}

// 弹窗样式
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
  backdrop-filter: blur(4px);
}

.modal-content {
  width: 100%;
  max-height: 80vh;
  background: #fff;
  border-radius: 40rpx 40rpx 0 0;
  padding: 40rpx;
  animation: slideUp 0.3s ease;
  overflow-y: auto;
}

@keyframes slideUp {
  from {
    transform: translateY(100%);
  }
  to {
    transform: translateY(0);
  }
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 40rpx;

  .modal-title {
    font-size: 36rpx;
    font-weight: 600;
    color: #333;
  }

  .close-btn {
    width: 56rpx;
    height: 56rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 32rpx;
    color: #999;
    background: #f5f5f5;
    border-radius: 50%;
  }
}

// 提醒类型标签
.reminder-type-tabs {
  display: flex;
  gap: 20rpx;
  margin-bottom: 40rpx;

  .type-tab {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 12rpx;
    padding: 30rpx;
    background: #f5f5f5;
    border-radius: 20rpx;
    transition: all 0.2s ease;

    text {
      font-size: 26rpx;
      color: #666;
    }

    &.active {
      background: #5B8FF9;

      text {
        color: #fff;
      }
    }
  }
}

// 表单区域
.form-section {
  .form-item {
    margin-bottom: 32rpx;

    .form-label {
      display: block;
      font-size: 28rpx;
      color: #333;
      font-weight: 500;
      margin-bottom: 16rpx;
    }

    .picker-value {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 24rpx 30rpx;
      background: #f8f9fa;
      border-radius: 16rpx;
      font-size: 30rpx;
      color: #333;
    }
  }
}

// Chip 选项
.chip-options {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;

  .chip {
    padding: 16rpx 32rpx;
    background: #f5f5f5;
    border-radius: 32rpx;
    font-size: 26rpx;
    color: #666;
    transition: all 0.2s ease;

    &.active {
      background: #5B8FF9;
      color: #fff;
    }
  }
}

// 位置选择器
.location-picker {
  padding: 40rpx;
  background: #f8f9fa;
  border-radius: 16rpx;
  border: 2rpx dashed #ddd;

  .location-placeholder {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 16rpx;

    text {
      font-size: 28rpx;
      color: #999;
    }
  }

  .location-selected {
    display: flex;
    align-items: center;
    gap: 20rpx;

    .location-info {
      display: flex;
      flex-direction: column;

      .location-name {
        font-size: 30rpx;
        color: #333;
        font-weight: 500;
      }

      .location-address {
        font-size: 24rpx;
        color: #999;
        margin-top: 4rpx;
      }
    }
  }
}

// 已存在的提醒
.existing-reminders {
  margin-top: 40rpx;
  padding-top: 40rpx;
  border-top: 2rpx solid #f0f0f0;

  .section-title {
    display: block;
    font-size: 28rpx;
    color: #666;
    margin-bottom: 24rpx;
    font-weight: 500;
  }

  .existing-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 24rpx;
    background: #f8f9fa;
    border-radius: 16rpx;
    margin-bottom: 16rpx;

    .existing-info {
      display: flex;
      align-items: center;
      gap: 16rpx;

      text {
        font-size: 28rpx;
        color: #333;
      }
    }

    .delete-btn {
      padding: 16rpx;
    }
  }
}

// 弹窗操作按钮
.modal-actions {
  display: flex;
  gap: 24rpx;
  margin-top: 40rpx;

  button {
    flex: 1;
    height: 90rpx;
    border-radius: 45rpx;
    font-size: 30rpx;
    font-weight: 500;
    border: none;
  }

  .btn-cancel {
    background: #f5f5f5;
    color: #666;
  }

  .btn-confirm {
    background: #5B8FF9;
    color: #fff;
    box-shadow: 0 8rpx 20rpx rgba(91, 143, 249, 0.3);
  }
}
</style>