<template>
  <!-- 任务详情弹窗 -->
  <view v-if="visible && task" class="modal-overlay" @click="$emit('close')">
    <view class="modal-content detail-modal" @click.stop>
      <view class="modal-header">
        <text class="modal-title">任务详情</text>
        <text class="close-btn" @click="$emit('close')">✕</text>
      </view>
      
      <view class="task-detail">
        <view class="detail-header">
          <view 
            class="detail-checkbox" 
            :class="{ checked: task.status === 2 }" 
            @click="$emit('toggle-task', task)"
          >
            <text v-if="task.status === 2" class="check-icon">✓</text>
          </view>
          <view class="detail-title" :class="{ completed: task.status === 2 }">
            {{ task.title }}
          </view>
        </view>
        
        <view class="detail-info">
          <view class="info-item">
            <text class="info-label">📅 截止日期</text>
            <text class="info-value">{{ task.dueDate || '未设置' }}</text>
          </view>
          <view class="info-item">
            <text class="info-label">⏰ 截止时间</text>
            <text class="info-value">{{ task.dueTime || '未设置' }}</text>
          </view>
          <view class="info-item">
            <text class="info-label">👤 负责人</text>
            <text class="info-value">{{ task.assigneeName || '未指派' }}</text>
          </view>
          <view class="info-item">
            <text class="info-label">📁 分类</text>
            <text class="info-value">{{ task.categoryName || '未分类' }}</text>
          </view>
          <view class="info-item">
            <text class="info-label">⚡ 优先级</text>
            <text 
              class="info-value" 
              :class="'priority-text-' + task.priority"
            >
              {{ priorityText(task.priority) }}
            </text>
          </view>
        </view>
        
        <!-- 子任务 -->
        <view 
          v-if="task.subtasks && task.subtasks.length > 0" 
          class="subtask-section"
        >
          <view class="section-title">
            子任务 ({{ subtaskCompleted }}/{{ task.subtasks.length }})
          </view>
          <view 
            v-for="(sub, idx) in task.subtasks" 
            :key="idx"
            class="subtask-item"
            @click="$emit('toggle-subtask', sub)"
          >
            <view 
              class="subtask-checkbox" 
              :class="{ checked: sub.status === 1 }"
            ></view>
            <text 
              class="subtask-title" 
              :class="{ completed: sub.status === 1 }"
            >
              {{ sub.title }}
            </text>
          </view>
        </view>
        
        <view class="detail-actions">
          <view class="detail-btn edit" @click="$emit('edit', task)">
            <text>✏️ 编辑</text>
          </view>
          <view class="detail-btn delete" @click="$emit('delete', task)">
            <text>🗑️ 删除</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  visible: Boolean,
  task: Object,
  priorities: Array
})

const emit = defineEmits(['close', 'toggle-task', 'toggle-subtask', 'edit', 'delete'])

const priorityText = (p) => {
  return props.priorities[p] || '普通'
}

const subtaskCompleted = computed(() => {
  if (!props.task || !props.task.subtasks) return 0
  return props.task.subtasks.filter(s => s.status === 1).length
})
</script>

<style lang="scss" scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  width: 85%;
  max-height: 80vh;
  background: #fff;
  border-radius: 20rpx;
  padding: 30rpx;
  overflow-y: auto;
  
  &.detail-modal {
    width: 90%;
  }
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30rpx;
  
  .modal-title {
    font-size: 32rpx;
    font-weight: 600;
    color: #333;
  }
  
  .close-btn {
    font-size: 36rpx;
    color: #999;
  }
}

.task-detail {
  .detail-header {
    display: flex;
    align-items: center;
    margin-bottom: 30rpx;
    
    .detail-checkbox {
      width: 48rpx;
      height: 48rpx;
      border: 2rpx solid #ddd;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 20rpx;
      flex-shrink: 0;
      
      &.checked {
        background: #4caf50;
        border-color: #4caf50;
      }
      
      .check-icon {
        color: #fff;
        font-size: 28rpx;
      }
    }
    
    .detail-title {
      font-size: 36rpx;
      font-weight: 600;
      color: #333;
      flex: 1;
      
      &.completed {
        text-decoration: line-through;
        color: #999;
      }
    }
  }
  
  .detail-info {
    margin-bottom: 30rpx;
    
    .info-item {
      display: flex;
      justify-content: space-between;
      padding: 20rpx 0;
      border-bottom: 1rpx solid #f5f5f5;
      
      .info-label {
        font-size: 28rpx;
        color: #666;
      }
      
      .info-value {
        font-size: 28rpx;
        color: #333;
        
        &.priority-text-0 { color: #4caf50; }
        &.priority-text-1 { color: #ff9800; }
        &.priority-text-2 { color: #f44336; }
      }
    }
  }
  
  .subtask-section {
    border-top: 1rpx solid #f0f0f0;
    padding-top: 30rpx;
    margin-bottom: 30rpx;
    
    .section-title {
      font-size: 28rpx;
      font-weight: 600;
      color: #333;
      margin-bottom: 20rpx;
    }
    
    .subtask-item {
      display: flex;
      align-items: center;
      padding: 20rpx 0;
      
      .subtask-checkbox {
        width: 40rpx;
        height: 40rpx;
        border: 2rpx solid #ddd;
        border-radius: 50%;
        margin-right: 20rpx;
        flex-shrink: 0;
        
        &.checked {
          background: #4caf50;
          border-color: #4caf50;
        }
      }
      
      .subtask-title {
        font-size: 30rpx;
        color: #333;
        flex: 1;
        
        &.completed {
          text-decoration: line-through;
          color: #999;
        }
      }
    }
  }
  
  .detail-actions {
    display: flex;
    gap: 20rpx;
    border-top: 1rpx solid #f0f0f0;
    padding-top: 30rpx;
    
    .detail-btn {
      flex: 1;
      padding: 24rpx;
      border-radius: 12rpx;
      text-align: center;
      
      &.edit {
        background: #e3f2fd;
        color: #2196f3;
      }
      
      &.delete {
        background: #ffebee;
        color: #f44336;
      }
      
      text {
        font-size: 28rpx;
      }
    }
  }
}
</style>
