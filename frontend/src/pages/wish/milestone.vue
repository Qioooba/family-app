<template>
  <view class="milestone-page">
    <!-- 顶部导航 -->
    <view class="nav-bar">
      <view class="back-btn" @click="goBack">
        <u-icon name="arrow-left" size="40" color="#333"></u-icon>
      </view>
      <text class="title">里程碑</text>
      <view class="right-btn" @click="showAddMilestone">
        <u-icon name="plus" size="36" color="#fff"></u-icon>
      </view>
    </view>

    <view class="content">
      <!-- 心愿信息卡 -->
      <view class="wish-card">
        <view class="wish-icon">{{ wish.icon }}</view>
        <view class="wish-info">
          <text class="wish-title">{{ wish.title }}</text>
          <text class="wish-desc">{{ wish.description }}</text>
          
          <view class="wish-progress">
            <view class="progress-bar">
              <view class="progress-fill" :style="{ width: wish.progress + '%' }"></text>
            </view>
            <text class="progress-text">{{ wish.progress }}%</text>
          </view>
        </view>
      </view>

      <!-- 统计卡片 -->
      <view class="stats-row">
        <view class="stat-card">
          <text class="stat-num">{{ milestones.length }}</text>
          <text class="stat-label">里程碑</text>
        </view>
        
        <view class="stat-card">
          <text class="stat-num completed">{{ completedCount }}</text>
          <text class="stat-label">已完成</text>
        </view>
        
        <view class="stat-card">
          <text class="stat-num pending">{{ pendingCount }}</text>
          <text class="stat-label">进行中</text>
        </view>
        
        <view class="stat-card">
          <text class="stat-num">{{ daysRemaining }}</text>
          <text class="stat-label">剩余天数</text>
        </view>
      </view>

      <!-- 时间线 -->
      <view class="timeline-section">
        <view class="section-header">
          <text class="section-title">实现路径</text>
          
          <view class="filter-tabs"
>
            <view
              v-for="tab in filterTabs"
              :key="tab.value"
              class="filter-tab"
              :class="{ active: currentFilter === tab.value }"
              @click="currentFilter = tab.value"
            >
              {{ tab.label }}
            </view>
          </view>
        </view>

        <scroll-view class="timeline" scroll-y>
          <view class="timeline-container">
            <view
              v-for="(milestone, index) in filteredMilestones"
              :key="milestone.id"
              class="timeline-item"
              :class="{ 
                completed: milestone.status === 'completed',
                active: milestone.status === 'active',
                pending: milestone.status === 'pending'
              }"
            >
              <!-- 时间线左侧 -->
              <view class="timeline-left"
>
                <text class="milestone-date">{{ formatDate(milestone.date) }}</text>
                <text class="milestone-time">{{ milestone.time || '' }}</text>
              </view>

              <!-- 时间线中轴 -->
              <view class="timeline-axis">
                <view class="axis-dot">
                  <u-icon 
                    v-if="milestone.status === 'completed'" 
                    name="checkmark" 
                    size="20" 
                    color="#fff"
                  ></u-icon>
                  <view v-else-if="milestone.status === 'active'" class="pulse-dot"></text>
                </view>
                <view v-if="index !== filteredMilestones.length - 1" class="axis-line"></text>
              </view>

              <!-- 时间线内容 -->
              <view class="timeline-content">
                <view class="milestone-card" @click="showMilestoneDetail(milestone)"
>
                  <view class="milestone-header"
>
                    <text class="milestone-title">{{ milestone.title }}</text>
                    
                    <view class="milestone-status" :class="milestone.status">
                      {{ getStatusText(milestone.status) }}
                    </view>
                  </view>

                  <text class="milestone-desc">{{ milestone.description }}</text>

                  <view v-if="milestone.tasks && milestone.tasks.length" class="milestone-tasks"
>
                    <view
                      v-for="(task, tIndex) in milestone.tasks"
                      :key="tIndex"
                      class="sub-task"
                      :class="{ done: task.done }"
                    >
                      <view class="task-checkbox">
                        <u-icon v-if="task.done" name="checkmark" size="16" color="#52C41A"></u-icon>
                      </view>
                      <text class="task-text">{{ task.name }}</text>
                    </view>
                  </view>

                  <view v-if="milestone.reward" class="milestone-reward">
                    <u-icon name="gift" size="24" color="#FAAD14"></u-icon>
                    <text>{{ milestone.reward }}</text>
                  </view>

                  <view class="milestone-footer">
                    <view class="milestone-actions"
>
                      <text 
                        v-if="milestone.status === 'pending'" 
                        class="action-btn start"
                        @click.stop="startMilestone(milestone)"
                      >
                        开始
                      </text>
                      <text 
                        v-if="milestone.status === 'active'" 
                        class="action-btn complete"
                        @click.stop="completeMilestone(milestone)"
                      >
                        完成
                      </text>
                      <text 
                        class="action-btn edit"
                        @click.stop="editMilestone(milestone)"
                      >
                        编辑
                      </text>
                    </view>
                  </view>
                </view>
              </view>
            </view>
          </view>
          
          <view v-if="filteredMilestones.length === 0" class="empty-timeline">
            <u-icon name="file-text" size="80" color="#ddd"></u-icon>
            <text class="empty-text">暂无里程碑</text>
            <text class="empty-hint">点击右上角 + 添加第一个里程碑</text>
          </view>
        </scroll-view>
      </view>
    </view>

    <!-- 添加/编辑里程碑弹窗 -->
    <u-popup
      v-model:show="milestoneModalVisible"
      mode="bottom"
      round
      closeable
    >
      <view class="modal-content">
        <view class="modal-header"
>
          <text class="modal-title">{{ isEditing ? '编辑里程碑' : '添加里程碑' }}</text>
        </view>

        <view class="modal-body">
          <view class="form-item">
            <text class="form-label">标题</text>
            <input
              v-model="milestoneForm.title"
              placeholder="请输入里程碑标题"
              class="form-input"
            />
          </view>

          <view class="form-item"
>
            <text class="form-label">描述</text>
            <textarea
              v-model="milestoneForm.description"
              placeholder="描述这个里程碑..."
              class="form-textarea"
            />
          </view>

          <view class="form-row">
            <view class="form-item half">
              <text class="form-label">日期</text>
              <picker mode="date" :value="milestoneForm.date" @change="onDateChange">
                <view class="picker-value">
                  {{ milestoneForm.date || '选择日期' }}
                </view>
              </picker>
            </view>

            <view class="form-item half">
              <text class="form-label">时间</text>
              <picker mode="time" :value="milestoneForm.time" @change="onTimeChange"
>
                <view class="picker-value">
                  {{ milestoneForm.time || '选择时间' }}
                </view>
              </picker>
            </view>
          </view>

          <view class="form-item">
            <text class="form-label">状态</text>
            <view class="status-options">
              <view
                v-for="status in statusOptions"
                :key="status.value"
                class="status-option"
                :class="{ active: milestoneForm.status === status.value }"
                @click="milestoneForm.status = status.value"
              >
                {{ status.label }}
              </view>
            </view>
          </view>

          <view class="form-item">
            <text class="form-label">完成奖励（可选）</text>
            <input
              v-model="milestoneForm.reward"
              placeholder="例如：吃一顿大餐、买个小礼物..."
              class="form-input"
            />
          </view>
        </view>

        <view class="modal-footer">
          <view v-if="isEditing" class="btn-delete" @click="deleteMilestone">删除</view>
          <view class="btn-cancel" @click="milestoneModalVisible = false">取消</view>
          <view class="btn-confirm" @click="saveMilestone">确定</view>
        </view>
      </view>
    </u-popup>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import dayjs from 'dayjs'

// 响应式数据
const wish = ref({
  id: 1,
  title: '全家去日本旅行',
  description: '2026年暑假，一家三口去日本东京、大阪游玩',
  icon: '✈️',
  progress: 45,
  targetDate: '2026-07-15'
})

const milestones = ref([
  {
    id: 1,
    title: '制定旅行计划',
    description: '确定旅行日期、目的地、行程安排',
    date: '2026-02-01',
    time: '10:00',
    status: 'completed',
    tasks: [
      { name: '确定出行日期', done: true },
      { name: '选择目的地', done: true },
      { name: '制定行程大纲', done: true }
    ],
    reward: '买一本旅行指南'
  },
  {
    id: 2,
    title: '预订机票',
    description: '比较价格，预订往返机票',
    date: '2026-03-15',
    time: '14:00',
    status: 'completed',
    tasks: [
      { name: '比较航班价格', done: true },
      { name: '预订去程机票', done: true },
      { name: '预订返程机票', done: true }
    ],
    reward: '积分+100'
  },
  {
    id: 3,
    title: '预订酒店',
    description: '选择合适的地段和价格，预订住宿',
    date: '2026-04-01',
    time: '20:00',
    status: 'active',
    tasks: [
      { name: '研究酒店位置', done: true },
      { name: '比较价格和评价', done: false },
      { name: '完成预订', done: false }
    ],
    reward: '晚上看电影'
  },
  {
    id: 4,
    title: '办理签证',
    description: '准备签证材料，提交申请',
    date: '2026-04-20',
    time: '09:00',
    status: 'pending',
    tasks: [
      { name: '准备护照', done: false },
      { name: '填写申请表', done: false },
      { name: '预约递签', done: false }
    ],
    reward: '买件新衣服'
  },
  {
    id: 5,
    title: '兑换外币',
    description: '提前兑换日元现金',
    date: '2026-06-01',
    time: '10:00',
    status: 'pending',
    reward: '无'
  },
  {
    id: 6,
    title: '出发！',
    description: '踏上美好的旅程',
    date: '2026-07-15',
    time: '08:00',
    status: 'pending',
    reward: '美好回忆'
  }
])

const currentFilter = ref('all')
const milestoneModalVisible = ref(false)
const isEditing = ref(false)
const editingId = ref(null)

const milestoneForm = ref({
  title: '',
  description: '',
  date: '',
  time: '',
  status: 'pending',
  reward: ''
})

// 筛选选项
const filterTabs = [
  { label: '全部', value: 'all' },
  { label: '已完成', value: 'completed' },
  { label: '进行中', value: 'active' },
  { label: '待开始', value: 'pending' }
]

// 状态选项
const statusOptions = [
  { label: '待开始', value: 'pending' },
  { label: '进行中', value: 'active' },
  { label: '已完成', value: 'completed' }
]

// 计算属性
const filteredMilestones = computed(() => {
  let list = [...milestones.value].sort((a, b) => 
    dayjs(a.date).valueOf() - dayjs(b.date).valueOf()
  )
  
  if (currentFilter.value !== 'all') {
    list = list.filter(m => m.status === currentFilter.value)
  }
  
  return list
})

const completedCount = computed(() => 
  milestones.value.filter(m => m.status === 'completed').length
)

const pendingCount = computed(() => 
  milestones.value.filter(m => m.status === 'active').length
)

const daysRemaining = computed(() => {
  if (!wish.value.targetDate) return 0
  const days = dayjs(wish.value.targetDate).diff(dayjs(), 'day')
  return Math.max(0, days)
})

// 方法
const formatDate = (date) => {
  return dayjs(date).format('MM-DD')
}

const getStatusText = (status) => {
  const map = { pending: '待开始', active: '进行中', completed: '已完成' }
  return map[status] || status
}

const showAddMilestone = () => {
  isEditing.value = false
  editingId.value = null
  milestoneForm.value = {
    title: '',
    description: '',
    date: dayjs().format('YYYY-MM-DD'),
    time: '10:00',
    status: 'pending',
    reward: ''
  }
  milestoneModalVisible.value = true
}

const editMilestone = (milestone) => {
  isEditing.value = true
  editingId.value = milestone.id
  milestoneForm.value = { ...milestone }
  milestoneModalVisible.value = true
}

const onDateChange = (e) => {
  milestoneForm.value.date = e.detail.value
}

const onTimeChange = (e) => {
  milestoneForm.value.time = e.detail.value
}

const saveMilestone = () => {
  if (!milestoneForm.value.title.trim()) {
    uni.showToast({ title: '请输入标题', icon: 'none' })
    return
  }
  
  if (isEditing.value) {
    const index = milestones.value.findIndex(m => m.id === editingId.value)
    if (index > -1) {
      milestones.value[index] = { ...milestones.value[index], ...milestoneForm.value }
    }
  } else {
    const newMilestone = {
      id: Date.now(),
      ...milestoneForm.value,
      tasks: []
    }
    milestones.value.push(newMilestone)
  }
  
  milestoneModalVisible.value = false
  uni.showToast({ title: '保存成功', icon: 'success' })
  
  // 更新进度
  updateProgress()
}

const deleteMilestone = () => {
  uni.showModal({
    title: '确认删除',
    content: '确定要删除这个里程碑吗？',
    success: (res) => {
      if (res.confirm) {
        const index = milestones.value.findIndex(m => m.id === editingId.value)
        if (index > -1) {
          milestones.value.splice(index, 1)
        }
        milestoneModalVisible.value = false
        uni.showToast({ title: '已删除', icon: 'success' })
        updateProgress()
      }
    }
  })
}

const startMilestone = (milestone) => {
  milestone.status = 'active'
  uni.showToast({ title: '已开始', icon: 'success' })
}

const completeMilestone = (milestone) => {
  milestone.status = 'completed'
  if (milestone.reward) {
    uni.showModal({
      title: '🎉 恭喜完成！',
      content: `奖励：${milestone.reward}`,
      showCancel: false
    })
  } else {
    uni.showToast({ title: '已完成', icon: 'success' })
  }
  updateProgress()
}

const updateProgress = () => {
  const total = milestones.value.length
  const completed = milestones.value.filter(m => m.status === 'completed').length
  wish.value.progress = total > 0 ? Math.round(completed / total * 100) : 0
}

const showMilestoneDetail = (milestone) => {
  uni.showModal({
    title: milestone.title,
    content: `${milestone.description}\n\n日期: ${milestone.date}${milestone.time ? ' ' + milestone.time : ''}\n状态: ${getStatusText(milestone.status)}${milestone.reward ? '\n奖励: ' + milestone.reward : ''}`,
    showCancel: false
  })
}

const goBack = () => {
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
.milestone-page {
  min-height: 100vh;
  background: #f5f6fa;
}

.nav-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 30rpx;
  padding-top: 60rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);

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
    width: 60rpx;
    height: 60rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    background: rgba(255, 255, 255, 0.2);
    border-radius: 50%;
  }
}

.content {
  padding: 0 30rpx 30rpx;
}

// 心愿卡片
.wish-card {
  display: flex;
  align-items: flex-start;
  background: #fff;
  border-radius: 24rpx;
  padding: 30rpx;
  margin-top: -40rpx;
  position: relative;
  z-index: 1;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.08);

  .wish-icon {
    width: 100rpx;
    height: 100rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 24rpx;
    font-size: 48rpx;
    margin-right: 24rpx;
    flex-shrink: 0;
  }

  .wish-info {
    flex: 1;
    min-width: 0;

    .wish-title {
      font-size: 32rpx;
      font-weight: 600;
      color: #333;
      display: block;
      margin-bottom: 8rpx;
    }

    .wish-desc {
      font-size: 26rpx;
      color: #666;
      display: block;
      margin-bottom: 16rpx;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .wish-progress {
      display: flex;
      align-items: center;
      gap: 16rpx;

      .progress-bar {
        flex: 1;
        height: 16rpx;
        background: #f5f5f5;
        border-radius: 8rpx;
        overflow: hidden;

        .progress-fill {
          height: 100%;
          background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
          border-radius: 8rpx;
          transition: all 0.3s;
        }
      }

      .progress-text {
        font-size: 24rpx;
        color: #667eea;
        font-weight: 600;
        min-width: 60rpx;
        text-align: right;
      }
    }
  }
}

// 统计行
.stats-row {
  display: flex;
  gap: 16rpx;
  margin-top: 30rpx;

  .stat-card {
    flex: 1;
    background: #fff;
    border-radius: 16rpx;
    padding: 24rpx;
    text-align: center;

    .stat-num {
      font-size: 40rpx;
      font-weight: 700;
      color: #333;
      display: block;
      margin-bottom: 8rpx;

      &.completed {
        color: #52C41A;
      }

      &.pending {
        color: #FAAD14;
      }
    }

    .stat-label {
      font-size: 24rpx;
      color: #999;
    }
  }
}

// 时间线区块
.timeline-section {
  margin-top: 40rpx;

  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 30rpx;

    .section-title {
      font-size: 32rpx;
      font-weight: 600;
      color: #333;
    }

    .filter-tabs {
      display: flex;
      gap: 12rpx;

      .filter-tab {
        padding: 10rpx 20rpx;
        background: #fff;
        border-radius: 24rpx;
        font-size: 24rpx;
        color: #666;
        border: 2rpx solid transparent;

        &.active {
          border-color: #5B8FF9;
          color: #5B8FF9;
          background: #f0f5ff;
        }
      }
    }
  }
}

// 时间线
.timeline {
  max-height: 800rpx;
  background: #fff;
  border-radius: 24rpx;
  padding: 30rpx;
}

.timeline-container {
  padding-left: 20rpx;
}

.timeline-item {
  display: flex;
  position: relative;
  padding-bottom: 40rpx;

  &:last-child {
    padding-bottom: 0;
  }

  // 时间线左侧
  .timeline-left {
    width: 100rpx;
    text-align: right;
    padding-right: 20rpx;
    flex-shrink: 0;

    .milestone-date {
      font-size: 24rpx;
      font-weight: 600;
      color: #333;
      display: block;
    }

    .milestone-time {
      font-size: 20rpx;
      color: #999;
      margin-top: 4rpx;
      display: block;
    }
  }

  // 时间线中轴
  .timeline-axis {
    position: relative;
    width: 40rpx;
    display: flex;
    flex-direction: column;
    align-items: center;
    flex-shrink: 0;

    .axis-dot {
      width: 40rpx;
      height: 40rpx;
      border-radius: 50%;
      background: #ddd;
      display: flex;
      align-items: center;
      justify-content: center;
      z-index: 1;

      .pulse-dot {
        width: 16rpx;
        height: 16rpx;
        background: #fff;
        border-radius: 50%;
        animation: pulse 1.5s infinite;
      }
    }

    .axis-line {
      flex: 1;
      width: 4rpx;
      background: #eee;
      margin-top: 8rpx;
    }
  }

  // 时间线内容
  .timeline-content {
    flex: 1;
    padding-left: 20rpx;

    .milestone-card {
      background: #f9f9f9;
      border-radius: 16rpx;
      padding: 24rpx;
      border-left: 6rpx solid #ddd;

      .milestone-header {
        display: flex;
        justify-content: space-between;
        align-items: flex-start;
        margin-bottom: 12rpx;

        .milestone-title {
          font-size: 30rpx;
          font-weight: 600;
          color: #333;
          flex: 1;
          margin-right: 16rpx;
        }

        .milestone-status {
          padding: 6rpx 16rpx;
          border-radius: 8rpx;
          font-size: 22rpx;

          &.pending {
            background: #f5f5f5;
            color: #999;
          }

          &.active {
            background: #FFF7E6;
            color: #FAAD14;
          }

          &.completed {
            background: #F6FFED;
            color: #52C41A;
          }
        }
      }

      .milestone-desc {
        font-size: 26rpx;
        color: #666;
        line-height: 1.6;
        display: block;
        margin-bottom: 16rpx;
      }

      .milestone-tasks {
        margin-bottom: 16rpx;

        .sub-task {
          display: flex;
          align-items: center;
          gap: 12rpx;
          padding: 8rpx 0;

          .task-checkbox {
            width: 28rpx;
            height: 28rpx;
            border: 2rpx solid #ddd;
            border-radius: 6rpx;
            display: flex;
            align-items: center;
            justify-content: center;
          }

          .task-text {
            font-size: 24rpx;
            color: #666;
          }

          &.done {
            .task-text {
              text-decoration: line-through;
              color: #999;
            }
          }
        }
      }

      .milestone-reward {
        display: flex;
        align-items: center;
        gap: 8rpx;
        padding: 12rpx 16rpx;
        background: #FFF7E6;
        border-radius: 8rpx;
        margin-bottom: 16rpx;

        text {
          font-size: 24rpx;
          color: #FAAD14;
        }
      }

      .milestone-footer {
        display: flex;
        justify-content: flex-end;

        .milestone-actions {
          display: flex;
          gap: 20rpx;

          .action-btn {
            font-size: 24rpx;
            padding: 8rpx 16rpx;
            border-radius: 8rpx;

            &.start {
              color: #5B8FF9;
              background: #f0f5ff;
            }

            &.complete {
              color: #52C41A;
              background: #F6FFED;
            }

            &.edit {
              color: #999;
            }
          }
        }
      }
    }
  }

  // 不同状态样式
  &.completed {
    .axis-dot {
      background: #52C41A;
    }

    .timeline-content .milestone-card {
      border-left-color: #52C41A;
      background: #F6FFED;
    }
  }

  &.active {
    .axis-dot {
      background: #FAAD14;
    }

    .timeline-content .milestone-card {
      border-left-color: #FAAD14;
      background: #FFF7E6;
    }
  }
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
    opacity: 1;
  }
  50% {
    transform: scale(1.2);
    opacity: 0.8;
  }
}

.empty-timeline {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 100rpx 0;

  .empty-text {
    font-size: 28rpx;
    color: #999;
    margin-top: 20rpx;
  }

  .empty-hint {
    font-size: 24rpx;
    color: #bbb;
    margin-top: 12rpx;
  }
}

// 弹窗样式
.modal-content {
  padding: 30rpx 0;

  .modal-header {
    text-align: center;
    padding: 0 30rpx 30rpx;
    border-bottom: 1rpx solid #f5f5f5;

    .modal-title {
      font-size: 32rpx;
      font-weight: 600;
      color: #333;
    }
  }

  .modal-body {
    padding: 30rpx;
  }

  .modal-footer {
    display: flex;
    gap: 20rpx;
    padding: 0 30rpx;

    .btn-delete {
      padding: 24rpx 40rpx;
      border-radius: 12rpx;
      font-size: 30rpx;
      color: #FF4D4F;
      background: #FFF1F0;
    }

    .btn-cancel {
      flex: 1;
      padding: 24rpx 0;
      border-radius: 12rpx;
      text-align: center;
      font-size: 30rpx;
      background: #f5f5f5;
      color: #666;
    }

    .btn-confirm {
      flex: 1;
      padding: 24rpx 0;
      border-radius: 12rpx;
      text-align: center;
      font-size: 30rpx;
      background: #5B8FF9;
      color: #fff;
    }
  }
}

.form-item {
  margin-bottom: 30rpx;

  &.half {
    flex: 1;
  }

  .form-label {
    font-size: 26rpx;
    color: #666;
    margin-bottom: 12rpx;
    display: block;
  }

  .form-input, .form-textarea, .picker-value {
    background: #f5f6fa;
    border-radius: 12rpx;
    padding: 20rpx 24rpx;
    font-size: 28rpx;
    color: #333;
  }

  .form-textarea {
    height: 160rpx;
    width: 100%;
  }

  .picker-value {
    color: #333;
  }
}

.form-row {
  display: flex;
  gap: 20rpx;
}

.status-options {
  display: flex;
  gap: 16rpx;

  .status-option {
    flex: 1;
    padding: 20rpx;
    background: #f5f6fa;
    border-radius: 12rpx;
    text-align: center;
    font-size: 28rpx;
    color: #666;
    border: 2rpx solid transparent;

    &.active {
      border-color: #5B8FF9;
      color: #5B8FF9;
      background: #f0f5ff;
    }
  }
}
</style>