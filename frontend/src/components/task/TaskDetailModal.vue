<template>
  <!-- 任务详情弹窗 -->
  <view v-if="visible && task" class="modal-overlay" @click="$emit('close')">
    <view class="modal-content" @click.stop>
      <!-- 头部 -->
      <view class="modal-header">
        <view class="header-left">
          <text class="modal-icon">📋</text>
          <text class="modal-title">任务详情</text>
        </view>
        <view class="close-btn" @click="$emit('close')">
          <up-icon name="close" size="20" color="#999"></up-icon>
        </view>
      </view>
      
      <!-- 任务标题区域 -->
      <view class="task-title-section">
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
      
      <!-- 信息卡片区域 -->
      <view class="info-cards">
        <!-- 截止时间 -->
        <view class="info-card">
          <view class="info-card-icon">📅</view>
          <view class="info-card-content">
            <text class="info-card-label">截止时间</text>
            <text class="info-card-value">
              {{ formatDateTime(task.dueDate, task.dueTime) }}
            </text>
          </view>
        </view>
        
        <!-- 优先级 -->
        <view class="info-card">
          <view class="info-card-icon priority-icon">⚡</view>
          <view class="info-card-content">
            <text class="info-card-label">优先级</text>
            <view class="priority-tag" :class="'priority-' + task.priority">
              <text class="priority-dot"></text>
              <text class="priority-text">{{ priorityText(task.priority) }}</text>
            </view>
          </view>
        </view>
        
        <!-- 分类 -->
        <view class="info-card">
          <view class="info-card-icon">📁</view>
          <view class="info-card-content">
            <text class="info-card-label">分类</text>
            <text class="info-card-value">{{ task.categoryName || '未分类' }}</text>
          </view>
        </view>
        
        <!-- 负责人 -->
        <view class="info-card">
          <view class="info-card-icon">👤</view>
          <view class="info-card-content">
            <text class="info-card-label">负责人</text>
            <text class="info-card-value">{{ task.assigneeName || '未指派' }}</text>
          </view>
        </view>
      </view>
      
      <!-- 备注区域 -->
      <view v-if="task.remark" class="remark-section">
        <text class="section-label">📝 备注</text>
        <view class="remark-content">
          {{ task.remark }}
        </view>
      </view>
      
      <!-- 底部操作按钮 -->
      <view class="form-actions">
        <view class="btn-cancel" @click="$emit('edit', task)">
          <text>✏️ 编辑</text>
        </view>
        <view class="btn-confirm" @click="$emit('delete', task)">
          <text>🗑️ 删除</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
const props = defineProps({
  visible: Boolean,
  task: Object,
  priorities: Array
})

const emit = defineEmits(['close', 'toggle-task', 'edit', 'delete'])

const priorityText = (p) => {
  return props.priorities?.[p] || ['普通', '重要', '紧急'][p] || '普通'
}

const formatDateTime = (date, time) => {
  if (!date && !time) return '未设置'
  if (date && time) return `${date} ${time}`
  return date || time || '未设置'
}
</script>

<style lang="scss" scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  animation: fadeIn 0.2s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.modal-content {
  width: 82%;
  max-height: 80vh;
  background: #fff;
  border-radius: 32rpx;
  padding: 40rpx;
  box-shadow: 0 20rpx 60rpx rgba(0, 0, 0, 0.2);
  animation: slideUp 0.3s ease-out;
  overflow-y: auto;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(40rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 40rpx;
  
  .header-left {
    display: flex;
    align-items: center;
    gap: 12rpx;
    
    .modal-icon {
      font-size: 36rpx;
    }
    
    .modal-title {
      font-size: 36rpx;
      font-weight: 600;
      color: #2d3748;
    }
  }
  
  .close-btn {
    width: 56rpx;
    height: 56rpx;
    background: #f5f5f5;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.2s ease;
    
    &:active {
      transform: scale(0.9);
      background: #e8e8e8;
    }
  }
}

// 任务标题区域
.task-title-section {
  display: flex;
  align-items: center;
  margin-bottom: 32rpx;
  padding: 24rpx;
  background: #f7fafc;
  border-radius: 20rpx;
  
  .detail-checkbox {
    width: 48rpx;
    height: 48rpx;
    border: 2rpx solid #cbd5e0;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 20rpx;
    flex-shrink: 0;
    transition: all 0.2s ease;
    background: #fff;
    
    &.checked {
      background: linear-gradient(135deg, #A8E6CF, #7FD8BE);
      border-color: transparent;
    }
    
    .check-icon {
      color: #fff;
      font-size: 28rpx;
      font-weight: bold;
    }
  }
  
  .detail-title {
    font-size: 32rpx;
    font-weight: 600;
    color: #2d3748;
    flex: 1;
    line-height: 1.5;
    
    &.completed {
      text-decoration: line-through;
      color: #a0aec0;
    }
  }
}

// 信息卡片区域
.info-cards {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20rpx;
  margin-bottom: 32rpx;
  
  .info-card {
    background: #f7fafc;
    border-radius: 20rpx;
    padding: 24rpx;
    display: flex;
    align-items: flex-start;
    gap: 16rpx;
    transition: all 0.2s ease;
    
    &:active {
      transform: scale(0.98);
      background: #edf2f7;
    }
    
    .info-card-icon {
      font-size: 36rpx;
      flex-shrink: 0;
    }
    
    .info-card-content {
      flex: 1;
      min-width: 0;
      
      .info-card-label {
        display: block;
        font-size: 24rpx;
        color: #718096;
        margin-bottom: 8rpx;
      }
      
      .info-card-value {
        font-size: 28rpx;
        color: #2d3748;
        font-weight: 500;
        display: block;
      }
      
      .priority-tag {
        display: inline-flex;
        align-items: center;
        gap: 8rpx;
        padding: 8rpx 16rpx;
        border-radius: 24rpx;
        
        .priority-dot {
          width: 12rpx;
          height: 12rpx;
          border-radius: 50%;
        }
        
        .priority-text {
          font-size: 26rpx;
          font-weight: 500;
        }
        
        &.priority-0 {
          background: rgba(104, 211, 145, 0.15);
          .priority-dot { background: #68d391; }
          .priority-text { color: #38a169; }
        }
        
        &.priority-1 {
          background: rgba(246, 173, 85, 0.15);
          .priority-dot { background: #f6ad55; }
          .priority-text { color: #dd6b20; }
        }
        
        &.priority-2 {
          background: rgba(252, 129, 129, 0.15);
          .priority-dot { background: #fc8181; }
          .priority-text { color: #e53e3e; }
        }
      }
    }
  }
}

// 备注区域
.remark-section {
  margin-bottom: 32rpx;
  
  .section-label {
    display: block;
    font-size: 28rpx;
    font-weight: 500;
    color: #4a5568;
    margin-bottom: 16rpx;
  }
  
  .remark-content {
    background: #f7fafc;
    border-radius: 20rpx;
    padding: 24rpx;
    font-size: 28rpx;
    color: #4a5568;
    line-height: 1.6;
  }
}

// 底部操作按钮
.form-actions {
  display: flex;
  gap: 24rpx;
  margin-top: 40rpx;
  
  .btn-cancel, .btn-confirm {
    flex: 1;
    height: 96rpx;
    border-radius: 48rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.25s ease;
    
    &:active {
      transform: scale(0.96);
    }
    
    text {
      font-size: 30rpx;
      font-weight: 600;
    }
  }
  
  .btn-cancel {
    background: #f7fafc;
    
    text {
      color: #4a5568;
    }
    
    &:active {
      background: #edf2f7;
    }
  }
  
  .btn-confirm {
    background: linear-gradient(135deg, #ff8fa3, #ffc2d1);
    box-shadow: 0 8rpx 24rpx rgba(255, 143, 163, 0.4);
    
    text {
      color: #fff;
    }
    
    &:active {
      box-shadow: 0 4rpx 16rpx rgba(255, 143, 163, 0.3);
    }
  }
}
</style>