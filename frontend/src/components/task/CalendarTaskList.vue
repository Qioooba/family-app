<template>
  <!-- 任务列表 -->
  <view class="task-list-container">
    <view class="section-header">
      <view class="section-title">
        <text class="date-text">{{ selectedDateStr }}</text>
        <text class="task-count" v-if="tasks.length > 0">({{ tasks.length }})</text>
      </view>
      <view class="section-actions">
        <text class="filter-btn" @click="showFilter = !showFilter">
          {{ currentFilterLabel }}
        </text>
      </view>
    </view>
    
    <!-- 筛选下拉 -->
    <view v-if="showFilter" class="filter-dropdown">
      <text 
        v-for="filter in filters" 
        :key="filter.value"
        class="filter-option"
        :class="{ active: currentFilter === filter.value }"
        @click="selectFilter(filter.value)"
      >{{ filter.label }}</text>
    </view>
    
    <!-- 任务列表 -->
    <scroll-view class="tasks-list" scroll-y>
      <view 
        v-for="(task, index) in filteredTasks" 
        :key="task.id"
        class="task-card"
        :class="{ 
          completed: task.status === 2,
          overdue: isOverdue(task) && task.status !== 2
        }"
        @click="$emit('view-task', task)"
        @longpress="$emit('show-menu', task)"
      >
        <view 
          class="task-checkbox" 
          :class="{ checked: task.status === 2 }" 
          @click.stop="$emit('toggle-task', task)"
        >
          <text v-if="task.status === 2" class="check-icon">✓</text>
        </view>
        
        <view class="task-content">
          <view class="task-header">
            <text class="task-title">{{ task.title }}</text>
            <view class="task-priority" :class="'priority-' + task.priority">
              {{ priorityText(task.priority) }}
            </view>
          </view>
          
          <view class="task-meta">
            <text class="meta-item" v-if="task.categoryName">
              <text class="meta-icon">📁</text>{{ task.categoryName }}
            </text>
            <text class="meta-item" v-if="task.assigneeName">
              <text class="meta-icon">👤</text>{{ task.assigneeName }}
            </text>
            <text class="meta-item" v-if="task.dueTime">
              <text class="meta-icon">⏰</text>{{ task.dueTime }}
            </text>
          </view>
        </view>
        
        <view class="task-status">
          <text v-if="task.status === 2" class="status-badge completed">已完成</text>
          <text v-else-if="isOverdue(task)" class="status-badge overdue">已逾期</text>
          <text v-else class="status-badge pending">进行中</text>
        </view>
      </view>
      
      <!-- 空状态 -->
      <view v-if="filteredTasks.length === 0" class="empty-state">
        <text class="empty-icon">📅</text>
        <text class="empty-text">{{ tasks.length === 0 ? '该日期暂无任务' : '没有符合条件的任务' }}</text>
        <text class="empty-action" @click="$emit('add-task')">点击添加任务</text>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import dayjs from 'dayjs'

const props = defineProps({
  tasks: Array,
  selectedDate: Date,
  priorities: Array
})

const emit = defineEmits(['view-task', 'toggle-task', 'show-menu', 'add-task'])

const showFilter = ref(false)
const currentFilter = ref('all')

const filters = [
  { label: '全部', value: 'all' },
  { label: '待办', value: 'pending' },
  { label: '已完成', value: 'completed' }
]

const currentFilterLabel = computed(() => {
  const filter = filters.find(f => f.value === currentFilter.value)
  return filter ? filter.label : '全部'
})

const selectedDateStr = computed(() => {
  return dayjs(props.selectedDate).format('MM月DD日')
})

const filteredTasks = computed(() => {
  if (currentFilter.value === 'all') return props.tasks
  if (currentFilter.value === 'pending') return props.tasks.filter(t => t.status !== 2)
  if (currentFilter.value === 'completed') return props.tasks.filter(t => t.status === 2)
  return props.tasks
})

const priorityText = (p) => {
  return props.priorities[p] || '普通'
}

const isOverdue = (task) => {
  if (!task.dueDate || task.status === 2) return false
  const due = new Date(task.dueDate)
  const now = new Date()
  return due < now && due.toDateString() !== now.toDateString()
}

const selectFilter = (value) => {
  currentFilter.value = value
  showFilter.value = false
}
</script>

<style lang="scss" scoped>
.task-list-container {
  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 30rpx;
    border-bottom: 1rpx solid #f0f0f0;
    
    .section-title {
      display: flex;
      align-items: center;
      gap: 16rpx;
      
      .date-text {
        font-size: 32rpx;
        font-weight: 600;
        color: #333;
      }
      
      .task-count {
        font-size: 26rpx;
        color: #999;
      }
    }
    
    .filter-btn {
      font-size: 26rpx;
      color: #4caf50;
      padding: 12rpx 24rpx;
      background: #e8f5e9;
      border-radius: 24rpx;
    }
  }
  
  .filter-dropdown {
    display: flex;
    gap: 20rpx;
    padding: 20rpx 30rpx;
    background: #fafafa;
    
    .filter-option {
      font-size: 26rpx;
      color: #666;
      padding: 12rpx 24rpx;
      border-radius: 24rpx;
      background: #fff;
      
      &.active {
        background: #4caf50;
        color: #fff;
      }
    }
  }
  
  .tasks-list {
    max-height: 600rpx;
    
    .task-card {
      display: flex;
      align-items: center;
      padding: 24rpx 30rpx;
      border-bottom: 1rpx solid #f5f5f5;
      background: #fff;
      
      &.completed {
        opacity: 0.7;
        .task-title {
          text-decoration: line-through;
        }
      }
      
      &.overdue {
        background: #fff8e1;
      }
      
      .task-checkbox {
        width: 44rpx;
        height: 44rpx;
        border: 2rpx solid #ddd;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-right: 24rpx;
        flex-shrink: 0;
        
        &.checked {
          background: #4caf50;
          border-color: #4caf50;
        }
        
        .check-icon {
          color: #fff;
          font-size: 24rpx;
        }
      }
      
      .task-content {
        flex: 1;
        min-width: 0;
        
        .task-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 12rpx;
          
          .task-title {
            font-size: 30rpx;
            color: #333;
            font-weight: 500;
            flex: 1;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }
          
          .task-priority {
            font-size: 22rpx;
            padding: 6rpx 16rpx;
            border-radius: 8rpx;
            margin-left: 16rpx;
            flex-shrink: 0;
            
            &.priority-0 {
              background: #e8f5e9;
              color: #4caf50;
            }
            
            &.priority-1 {
              background: #fff3e0;
              color: #ff9800;
            }
            &.priority-2 {
              background: #ffebee;
              color: #f44336;
            }
          }
        }
        
        .task-meta {
          display: flex;
          flex-wrap: wrap;
          gap: 20rpx;
          
          .meta-item {
            font-size: 24rpx;
            color: #999;
            
            .meta-icon {
              margin-right: 8rpx;
            }
          }
        }
      }
      
      .task-status {
        margin-left: 20rpx;
        flex-shrink: 0;
        
        .status-badge {
          font-size: 22rpx;
          padding: 8rpx 16rpx;
          border-radius: 8rpx;
          
          &.completed {
            background: #e8f5e9;
            color: #4caf50;
          }
          
          &.overdue {
            background: #ffebee;
            color: #f44336;
          }
          
          &.pending {
            background: #e3f2fd;
            color: #2196f3;
          }
        }
      }
    }
    
    .empty-state {
      text-align: center;
      padding: 100rpx 60rpx;
      
      .empty-icon {
        font-size: 80rpx;
        margin-bottom: 24rpx;
        display: block;
      }
      
      .empty-text {
        font-size: 28rpx;
        color: #999;
        display: block;
        margin-bottom: 20rpx;
      }
      
      .empty-action {
        font-size: 28rpx;
        color: #4caf50;
      }
    }
  }
}
</style>