<template>
  <view v-if="visible" class="modal-overlay" @click="close">
    <view class="modal-content" :class="{ 'detail-modal': type === 'detail' }" @click.stop>
      <view class="modal-header">
        <text class="modal-title">{{ title }}</text>
        <text class="close-btn" @click="close">✕</text>
      </view>
      
      <!-- 添加任务表单 -->
      <view v-if="type === 'add'">
        <view class="form-item">
          <text class="label">任务标题</text>
          <input class="input" :value="formData.title" @input="e => updateForm('title', e.detail.value)" placeholder="输入任务标题" />
        </view>
        
        <view class="form-item">
          <text class="label">截止日期</text>
          <picker mode="date" :value="formData.dueDate" @change="e => updateForm('dueDate', e.detail.value)">
            <view class="picker">{{ formData.dueDate || '请选择日期' }}</view>
          </picker>
        </view>
        
        <view class="form-item">
          <text class="label">截止时间</text>
          <picker mode="time" :value="formData.dueTime" @change="e => updateForm('dueTime', e.detail.value)">
            <view class="picker">{{ formData.dueTime || '请选择时间(可选)' }}</view>
          </picker>
        </view>
        
        <view class="form-item">
          <text class="label">优先级</text>
          <view class="priority-options">
            <view 
              v-for="(p, i) in priorities" 
              :key="i"
              class="priority-option"
              :class="{ active: formData.priority === i }"
              @click="updateForm('priority', i)"
            >
              <text>{{ p }}</text>
            </view>
          </view>
        </view>
        
        <view class="form-item">
          <text class="label">分类</text>
          <view class="category-options">
            <view 
              v-for="cat in categories" 
              :key="cat.id"
              class="category-option"
              :class="{ active: formData.categoryId === cat.id }"
              @click="updateForm('categoryId', cat.id)"
            >
              <text>{{ cat.name }}</text>
            </view>
          </view>
        </view>
        
        <view class="form-actions">
          <button class="btn-cancel" @click="close">取消</button>
          <button class="btn-confirm" @click="confirm">确认添加</button>
        </view>
      </view>
      
      <!-- 任务详情 -->
      <view v-else-if="type === 'detail' && task" class="task-detail">
        <view class="detail-header">
          <view class="detail-checkbox" :class="{ checked: task.status === 2 }" @click="$emit('toggleTask', task)">
            <text v-if="task.status === 2" class="check-icon">✓</text>
          </view>
          <view class="detail-title" :class="{ completed: task.status === 2 }">{{ task.title }}</view>
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
            <text class="info-value" :class="'priority-text-' + task.priority">{{ priorityText(task.priority) }}</text>
          </view>
        </view>
        
        <view class="detail-actions">
          <view class="detail-btn edit" @click="$emit('editTask', task)">
            <text>✏️ 编辑</text>
          </view>
          <view class="detail-btn delete" @click="$emit('deleteTask', task)">
            <text>🗑️ 删除</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  type: {
    type: String,
    default: 'add', // 'add' or 'detail'
    validator: (value) => ['add', 'detail'].includes(value)
  },
  title: {
    type: String,
    default: '添加新任务'
  },
  formData: {
    type: Object,
    default: () => ({
      title: '',
      dueDate: '',
      dueTime: '',
      priority: 0,
      categoryId: 1
    })
  },
  task: {
    type: Object,
    default: null
  }
})

const priorities = ['普通', '重要', '紧急']
const categories = [
  { name: '待办', id: 1 },
  { name: '购物', id: 2 },
  { name: '家务', id: 3 },
  { name: '工作', id: 4 }
]

const priorityText = (p) => priorities[p] || '普通'

const emit = defineEmits(['update:visible', 'update:formData', 'confirm', 'toggleTask', 'editTask', 'deleteTask'])

const close = () => {
  emit('update:visible', false)
}

const updateForm = (key, value) => {
  emit('update:formData', { ...props.formData, [key]: value })
}

const confirm = () => {
  emit('confirm')
}
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
  padding: 20px;
}

.modal-content {
  width: 100%;
  max-height: 80vh;
  background: #fff;
  border-radius: 24px;
  padding: 24px;
  overflow-y: auto;
  
  &.detail-modal {
    max-height: 70vh;
  }
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  
  .modal-title {
    font-size: 18px;
    font-weight: 600;
    color: #1F2937;
  }
  
  .close-btn {
    font-size: 20px;
    color: #9CA3AF;
    padding: 4px;
  }
}

// 表单样式
.form-item {
  margin-bottom: 20px;
  
  .label {
    display: block;
    font-size: 14px;
    color: #374151;
    font-weight: 500;
    margin-bottom: 8px;
  }
  
  .input {
    width: 100%;
    height: 48px;
    background: #F3F4F6;
    border-radius: 12px;
    padding: 0 16px;
    font-size: 15px;
    color: #1F2937;
  }
  
  .picker {
    height: 48px;
    line-height: 48px;
    background: #F3F4F6;
    border-radius: 12px;
    padding: 0 16px;
    font-size: 15px;
    color: #6B7280;
  }
}

.priority-options, .category-options {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.priority-option, .category-option {
  padding: 10px 20px;
  background: #F3F4F6;
  border-radius: 12px;
  font-size: 14px;
  color: #6B7280;
  
  &.active {
    background: #667EEA;
    color: #fff;
  }
}

.form-actions {
  display: flex;
  gap: 12px;
  margin-top: 30px;
  
  button {
    flex: 1;
    height: 48px;
    border-radius: 24px;
    font-size: 15px;
    border: none;
    font-weight: 500;
  }
  
  .btn-cancel {
    background: #F3F4F6;
    color: #6B7280;
  }
  
  .btn-confirm {
    background: linear-gradient(135deg, #667EEA, #764BA2);
    color: #fff;
  }
}

// 任务详情样式
.task-detail {
  .detail-header {
    display: flex;
    align-items: flex-start;
    margin-bottom: 20px;
    
    .detail-checkbox {
      width: 28px;
      height: 28px;
      border: 2px solid #D1D5DB;
      border-radius: 50%;
      margin-right: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
      
      &.checked {
        background: #10B981;
        border-color: #10B981;
      }
      
      .check-icon {
        color: #fff;
        font-size: 14px;
        font-weight: bold;
      }
    }
    
    .detail-title {
      font-size: 18px;
      font-weight: 600;
      color: #1F2937;
      line-height: 1.4;
      flex: 1;
      
      &.completed {
        text-decoration: line-through;
        color: #9CA3AF;
      }
    }
  }
  
  .detail-info {
    background: #F9FAFB;
    border-radius: 16px;
    padding: 16px;
    margin-bottom: 20px;
    
    .info-item {
      display: flex;
      justify-content: space-between;
      padding: 10px 0;
      border-bottom: 1px solid #E5E7EB;
      
      &:last-child {
        border-bottom: none;
      }
      
      .info-label {
        font-size: 14px;
        color: #6B7280;
      }
      
      .info-value {
        font-size: 14px;
        color: #1F2937;
        font-weight: 500;
        
        &.priority-text-0 { color: #6B7280; }
        &.priority-text-1 { color: #D97706; }
        &.priority-text-2 { color: #DC2626; }
      }
    }
  }
}

.detail-actions {
  display: flex;
  gap: 12px;
  
  .detail-btn {
    flex: 1;
    padding: 14px;
    border-radius: 12px;
    text-align: center;
    font-size: 14px;
    font-weight: 500;
    
    &.edit {
      background: #DBEAFE;
      color: #2563EB;
    }
    
    &.delete {
      background: #FEE2E2;
      color: #DC2626;
    }
  }
}
</style>