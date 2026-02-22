<template>
  <view class="tasks-section">
    <!-- 任务卡片 -->
    <view 
      v-for="(task, index) in tasks" 
      :key="task.id"
      class="task-card"
      :class="{ 
        completed: task.status === 2,
        overdue: isOverdue(task) && task.status !== 2
      }"
      @click="$emit('viewTask', task)"
      @longpress="$emit('showMenu', task)"
    >
      <view class="task-checkbox" :class="{ checked: task.status === 2 }" @click.stop="$emit('toggleTask', task)">
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
        
        <!-- 子任务进度 -->
        <view v-if="task.subtasks && task.subtasks.length > 0" class="subtask-progress">
          <view class="progress-bar">
            <view class="progress-fill" :style="{ width: subtaskProgress(task) + '%' }"></view>
          </view>
          <text class="progress-text">{{ subtaskCompleted(task) }}/{{ task.subtasks.length }}</text>
        </view>
      </view>
      
      <view class="task-status">
        <text v-if="task.status === 2" class="status-badge completed">已完成</text>
        <text v-else-if="isOverdue(task)" class="status-badge overdue">已逾期</text>
        <text v-else class="status-badge pending">进行中</text>
      </view>
    </view>
    
    <!-- 空状态 -->
    <view v-if="tasks.length === 0" class="empty-state">
      <text class="empty-icon">📅</text>
      <text class="empty-text">{{ emptyText }}</text>
      <text class="empty-action" @click="$emit('addTask')">点击添加任务</text>
    </view>
  </view>
</template>

<script setup>
const props = defineProps({
  tasks: {
    type: Array,
    default: () => []
  },
  emptyText: {
    type: String,
    default: '暂无任务'
  }
})

const priorities = ['普通', '重要', '紧急']

const priorityText = (p) => priorities[p] || '普通'

const isOverdue = (task) => {
  if (!task.dueDate || task.status === 2) return false
  const dueDate = new Date(task.dueDate)
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  return dueDate < today
}

const subtaskCompleted = (task) => {
  if (!task.subtasks) return 0
  return task.subtasks.filter(s => s.status === 1).length
}

const subtaskProgress = (task) => {
  if (!task.subtasks || task.subtasks.length === 0) return 0
  return Math.round((subtaskCompleted(task) / task.subtasks.length) * 100)
}

defineEmits(['viewTask', 'showMenu', 'toggleTask', 'addTask'])
</script>

<style lang="scss" scoped>
.tasks-section {
  padding: 0;
}

// 任务卡片
.task-card {
  display: flex;
  align-items: flex-start;
  padding: 16px;
  background: #F9FAFB;
  border-radius: 16px;
  margin-bottom: 12px;
  transition: all 0.2s;
  
  &:active {
    transform: scale(0.98);
    background: #F3F4F6;
  }
  
  &.completed {
    opacity: 0.7;
    background: #ECFDF5;
    
    .task-title {
      text-decoration: line-through;
      color: #6B7280;
    }
  }
  
  &.overdue {
    background: #FEF2F2;
    border: 1px solid #FECACA;
  }
}

.task-checkbox {
  width: 24px;
  height: 24px;
  border: 2px solid #D1D5DB;
  border-radius: 50%;
  margin-right: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  margin-top: 2px;
  
  &.checked {
    background: #10B981;
    border-color: #10B981;
  }
  
  .check-icon {
    color: #fff;
    font-size: 12px;
    font-weight: bold;
  }
}

.task-content {
  flex: 1;
  min-width: 0;
}

.task-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 8px;
  
  .task-title {
    font-size: 15px;
    font-weight: 500;
    color: #1F2937;
    flex: 1;
    margin-right: 8px;
    line-height: 1.4;
  }
  
  .task-priority {
    font-size: 11px;
    padding: 3px 8px;
    border-radius: 10px;
    font-weight: 500;
    flex-shrink: 0;
    
    &.priority-0 {
      background: #E5E7EB;
      color: #6B7280;
    }
    
    &.priority-1 {
      background: #FEF3C7;
      color: #D97706;
    }
    
    &.priority-2 {
      background: #FEE2E2;
      color: #DC2626;
    }
  }
}

.task-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 8px;
  
  .meta-item {
    font-size: 12px;
    color: #6B7280;
    display: flex;
    align-items: center;
    gap: 4px;
    
    .meta-icon {
      font-size: 12px;
    }
  }
}

.subtask-progress {
  display: flex;
  align-items: center;
  gap: 8px;
  
  .progress-bar {
    flex: 1;
    height: 4px;
    background: #E5E7EB;
    border-radius: 2px;
    overflow: hidden;
    
    .progress-fill {
      height: 100%;
      background: #10B981;
      border-radius: 2px;
      transition: width 0.3s;
    }
  }
  
  .progress-text {
    font-size: 11px;
    color: #6B7280;
  }
}

.task-status {
  margin-left: 8px;
  
  .status-badge {
    font-size: 11px;
    padding: 4px 8px;
    border-radius: 8px;
    font-weight: 500;
    
    &.completed {
      background: #D1FAE5;
      color: #059669;
    }
    
    &.overdue {
      background: #FEE2E2;
      color: #DC2626;
    }
    
    &.pending {
      background: #DBEAFE;
      color: #2563EB;
    }
  }
}

// 空状态
.empty-state {
  text-align: center;
  padding: 40px 20px;
  
  .empty-icon {
    font-size: 48px;
    margin-bottom: 12px;
    display: block;
  }
  
  .empty-text {
    font-size: 14px;
    color: #9CA3AF;
    display: block;
    margin-bottom: 8px;
  }
  
  .empty-action {
    font-size: 13px;
    color: #667EEA;
    text-decoration: underline;
  }
}
</style>
