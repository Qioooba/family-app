<template>
  <view class="page-container">
    <!-- 头部 -->
    <view class="header">
      <view class="header-title">任务清单</view>
      <view class="header-action" @click="showAddModal">
        <text class="icon">+</text>
      </view>
    </view>
    
    <!-- 分类标签 -->
    <scroll-view class="category-tabs" scroll-x>
      <view 
        v-for="(cat, index) in categories" 
        :key="index"
        class="category-tab"
        :class="{ active: currentCategory === index }"
        @click="switchCategory(index)"
      >
        <text>{{ cat.name }}</text>
      </view>
    </scroll-view>
    
    <!-- 新增：排班表入口 -->
    <view class="schedule-entry" @click="goToSchedule">
      <text class="schedule-icon">📅</text>
      <text class="schedule-text">查看家务排班表</text>
      <text class="schedule-arrow">›</text>
    </view>
    
    <!-- 任务列表 -->
    <scroll-view class="task-list" scroll-y>
      <view 
        v-for="(task, index) in filteredTasks" 
        :key="index"
        class="task-card"
        :class="{ completed: task.status === 2, expanded: task.showSubtasks }"
        @click="viewTask(task)"
      >
        <view class="task-header">
          <view class="checkbox" :class="{ checked: task.status === 2 }" @click.stop="toggleTask(task)"></view>
          <view class="task-title">{{ task.title }}</view>
          <view class="task-priority" :class="'priority-' + task.priority">{{ priorityText(task.priority) }}</view>
        </view>
        
        <view class="task-info">
          <text class="task-time">⏰ {{ task.dueTime }}</text>
          <text class="task-assignee">👤 {{ task.assigneeName || '未指派' }}</text>
        </view>
        
        <!-- 子任务进度 -->
        <view v-if="task.subtasks && task.subtasks.length > 0" class="subtask-progress">
          <view class="progress-bar">
            <view class="progress-fill" :style="{ width: subtaskProgress(task) + '%' }"></view>
          </view>
          <text class="progress-text">{{ subtaskCompleted(task) }}/{{ task.subtasks.length }}</text>
        </view>
        
        <!-- 展开的子任务列表 -->
        <view v-if="task.showSubtasks && task.subtasks" class="subtask-list">
          <view 
            v-for="(sub, sidx) in task.subtasks" 
            :key="sidx"
            class="subtask-item"
            @click.stop="toggleSubtask(task, sub)"
          >
            <view class="subtask-checkbox" :class="{ checked: sub.status === 1 }"></view>
            <text class="subtask-title" :class="{ completed: sub.status === 1 }">{{ sub.title }}</text>
          </view>
          <view class="add-subtask" @click.stop="addSubtask(task)">
            <text>+ 添加子任务</text>
          </view>
        </view>
        
        <view class="task-footer">
          <view class="task-tags">
            <view class="task-tag">{{ task.categoryName }}</view>
          </view>
          <!-- 展开按钮 -->
          <view v-if="task.subtasks && task.subtasks.length > 0" class="expand-btn" @click.stop="task.showSubtasks = !task.showSubtasks">
            <text>{{ task.showSubtasks ? '收起' : '展开' }}</text>
          </view>
        </view>
      </view>
      
      <view v-if="filteredTasks.length === 0" class="empty-state">
        <text class="empty-icon">📝</text>
        <text class="empty-text">暂无任务，点击右上角添加</text>
      </view>
    </scroll-view>
    
    <!-- 任务详情弹窗（含子任务） -->
    <view v-if="showDetailModal" class="modal-overlay" @click="closeDetailModal">
      <view class="modal-content detail-modal" @click.stop>
        <view class="modal-header">
          <text>任务详情</text>
          <text class="close-btn" @click="closeDetailModal">✕</text>
        </view>
        
        <view v-if="selectedTask" class="task-detail">
          <view class="detail-title">{{ selectedTask.title }}</view>
          <view class="detail-info">
            <text>⏰ {{ selectedTask.dueTime }}</text>
            <text>👤 {{ selectedTask.assigneeName || '未指派' }}</text>
          </view>
          
          <!-- 子任务管理 -->
          <view class="subtask-section">
            <view class="section-header">
              <text>子任务 ({{ subtaskCompleted(selectedTask) }}/{{ selectedTask.subtasks?.length || 0 }})</text>
              <text class="add-btn" @click="addSubtask(selectedTask)">+ 添加</text>
            </view>
            
            <view v-if="selectedTask.subtasks && selectedTask.subtasks.length > 0" class="subtask-list-detail">
              <view 
                v-for="(sub, idx) in selectedTask.subtasks" 
                :key="idx"
                class="subtask-item-detail"
              >
                <view class="subtask-checkbox" :class="{ checked: sub.status === 1 }" @click="toggleSubtask(selectedTask, sub)"></view>
                <text class="subtask-title" :class="{ completed: sub.status === 1 }">{{ sub.title }}</text>
                <text class="delete-btn" @click="deleteSubtask(selectedTask, sub, idx)">🗑️</text>
              </view>
            </view>
            <view v-else class="no-subtask">
              <text>暂无子任务，点击添加</text>
            </view>
          </view>
        </view>
      </view>
    </view>
    
    <!-- 添加任务弹窗 -->
    <view v-if="showModal" class="modal-overlay" @click="closeModal">
      <view class="modal-content" @click.stop>
        <view class="modal-header">
          <text>添加新任务</text>
          <text class="close-btn" @click="closeModal">✕</text>
        </view>
        
        <view class="form-item">
          <text class="label">任务标题</text>
          <input class="input" v-model="newTask.title" placeholder="输入任务标题" />
        </view>
        
        <view class="form-item">
          <text class="label">截止时间</text>
          <picker mode="date" @change="onDateChange">
            <view class="picker">{{ newTask.dueDate || '请选择日期' }}</view>
          </picker>
        </view>
        
        <view class="form-item">
          <text class="label">优先级</text>
          <view class="priority-options">
            <view 
              v-for="(p, i) in priorities" 
              :key="i"
              class="priority-option"
              :class="{ active: newTask.priority === i }"
              @click="newTask.priority = i"
            >
              <text>{{ p }}</text>
            </view>
          </view>
        </view>
        
        <view class="form-actions">
          <button class="btn-cancel" @click="closeModal">取消</button>
          <button class="btn-confirm" @click="addTask">确认添加</button>
        </view>
      </view>
    </view>
    
    <!-- 添加子任务弹窗 -->
    <view v-if="showSubtaskModal" class="modal-overlay" @click="closeSubtaskModal">
      <view class="modal-content" @click.stop>
        <view class="modal-header">
          <text>添加子任务</text>
          <text class="close-btn" @click="closeSubtaskModal">✕</text>
        </view>
        <view class="form-item">
          <input class="input" v-model="newSubtaskTitle" placeholder="输入子任务名称" />
        </view>
        <view class="form-actions">
          <button class="btn-cancel" @click="closeSubtaskModal">取消</button>
          <button class="btn-confirm" @click="confirmAddSubtask">确认</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'

const categories = [
  { name: '全部', id: 0 },
  { name: '待办', id: 1 },
  { name: '购物', id: 2 },
  { name: '家务', id: 3 },
  { name: '排班', id: 4 }
]

const priorities = ['普通', '重要', '紧急']

const currentCategory = ref(0)
const showModal = ref(false)
const showDetailModal = ref(false)
const showSubtaskModal = ref(false)
const selectedTask = ref(null)
const currentSubtaskTask = ref(null)
const newSubtaskTitle = ref('')

const tasks = ref([
  { 
    id: 1, 
    title: '买牛奶和鸡蛋', 
    status: 0, 
    priority: 1, 
    categoryName: '购物', 
    dueTime: '今天 18:00', 
    assigneeName: '爸爸',
    showSubtasks: false,
    subtasks: [
      { title: '检查冰箱库存', status: 1 },
      { title: '列购物清单', status: 0 },
      { title: '去超市采购', status: 0 }
    ]
  },
  { 
    id: 2, 
    title: '给孩子检查作业', 
    status: 0, 
    priority: 2, 
    categoryName: '待办', 
    dueTime: '今天 20:00', 
    assigneeName: '妈妈',
    showSubtasks: false,
    subtasks: []
  },
  { 
    id: 3, 
    title: '周末大扫除', 
    status: 0, 
    priority: 0, 
    categoryName: '家务', 
    dueTime: '明天 10:00', 
    assigneeName: '',
    showSubtasks: false,
    subtasks: [
      { title: '扫地', status: 0 },
      { title: '擦桌子', status: 0 },
      { title: '整理房间', status: 0 }
    ]
  }
])

const newTask = ref({
  title: '',
  dueDate: '',
  priority: 0,
  categoryId: 1
})

const filteredTasks = computed(() => {
  if (currentCategory.value === 0) return tasks.value
  if (currentCategory.value === 4) {
    // 排班视图
    return tasks.value.filter(t => t.categoryName === '家务')
  }
  return tasks.value.filter(t => t.status === (currentCategory.value === 1 ? 0 : t.status))
})

const switchCategory = (index) => {
  currentCategory.value = index
}

const priorityText = (p) => priorities[p] || '普通'

const toggleTask = (task) => {
  task.status = task.status === 2 ? 0 : 2
  uni.showToast({ title: task.status === 2 ? '任务已完成' : '任务已恢复', icon: 'none' })
}

const viewTask = (task) => {
  selectedTask.value = task
  showDetailModal.value = true
}

const closeDetailModal = () => {
  showDetailModal.value = false
  selectedTask.value = null
}

const subtaskCompleted = (task) => {
  if (!task.subtasks) return 0
  return task.subtasks.filter(s => s.status === 1).length
}

const subtaskProgress = (task) => {
  if (!task.subtasks || task.subtasks.length === 0) return 0
  return Math.round((subtaskCompleted(task) / task.subtasks.length) * 100)
}

const toggleSubtask = (task, sub) => {
  sub.status = sub.status === 0 ? 1 : 0
  // 检查是否全部完成
  if (subtaskProgress(task) === 100) {
    uni.showToast({ title: '所有子任务完成！', icon: 'success' })
  }
}

const addSubtask = (task) => {
  currentSubtaskTask.value = task
  newSubtaskTitle.value = ''
  showSubtaskModal.value = true
}

const closeSubtaskModal = () => {
  showSubtaskModal.value = false
  currentSubtaskTask.value = null
}

const confirmAddSubtask = () => {
  if (!newSubtaskTitle.value.trim()) {
    uni.showToast({ title: '请输入子任务名称', icon: 'none' })
    return
  }
  
  if (!currentSubtaskTask.value.subtasks) {
    currentSubtaskTask.value.subtasks = []
  }
  
  currentSubtaskTask.value.subtasks.push({
    title: newSubtaskTitle.value,
    status: 0
  })
  
  uni.showToast({ title: '添加成功', icon: 'success' })
  closeSubtaskModal()
}

const deleteSubtask = (task, sub, index) => {
  uni.showModal({
    title: '确认删除',
    content: '确定要删除这个子任务吗？',
    success: (res) => {
      if (res.confirm) {
        task.subtasks.splice(index, 1)
        uni.showToast({ title: '已删除', icon: 'success' })
      }
    }
  })
}

const goToSchedule = () => {
  uni.navigateTo({ url: '/pages/task/schedule' })
}

const showAddModal = () => {
  showModal.value = true
}

const closeModal = () => {
  showModal.value = false
  newTask.value = { title: '', dueDate: '', priority: 0, categoryId: 1 }
}

const onDateChange = (e) => {
  newTask.value.dueDate = e.detail.value
}

const addTask = () => {
  if (!newTask.value.title) {
    uni.showToast({ title: '请输入任务标题', icon: 'none' })
    return
  }
  
  tasks.value.unshift({
    id: Date.now(),
    title: newTask.value.title,
    status: 0,
    priority: newTask.value.priority,
    categoryName: '待办',
    dueTime: newTask.value.dueDate || '未设置',
    assigneeName: '',
    showSubtasks: false,
    subtasks: []
  })
  
  uni.showToast({ title: '添加成功', icon: 'success' })
  closeModal()
}
</script>

<style lang="scss" scoped>
.page-container {
  min-height: 100vh;
  background: #F5F7FA;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 50px 20px 20px;
  background: linear-gradient(135deg, #E8F5E9, #C8E6C9);
  
  .header-title {
    font-size: 24px;
    font-weight: 700;
    color: #2C3E50;
  }
  
  .header-action {
    width: 44px;
    height: 44px;
    background: #4CAF50;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    
    .icon {
      font-size: 28px;
      color: #fff;
      font-weight: 300;
    }
  }
}

.category-tabs {
  padding: 15px;
  white-space: nowrap;
  background: #fff;
  
  .category-tab {
    display: inline-block;
    padding: 8px 20px;
    margin-right: 10px;
    background: #F5F7FA;
    border-radius: 20px;
    font-size: 14px;
    color: #7F8C8D;
    
    &.active {
      background: #4CAF50;
      color: #fff;
    }
  }
}

.schedule-entry {
  display: flex;
  align-items: center;
  margin: 10px 15px;
  padding: 12px 16px;
  background: linear-gradient(135deg, #E3F2FD, #BBDEFB);
  border-radius: 12px;
  
  .schedule-icon {
    font-size: 20px;
    margin-right: 10px;
  }
  
  .schedule-text {
    flex: 1;
    font-size: 14px;
    color: #1976D2;
  }
  
  .schedule-arrow {
    font-size: 18px;
    color: #1976D2;
  }
}

.task-list {
  padding: 15px;
  height: calc(100vh - 220px);
}

.task-card {
  background: #fff;
  border-radius: 16px;
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  
  &.completed {
    opacity: 0.7;
    .task-title {
      text-decoration: line-through;
    }
  }
  
  &.expanded {
    .subtask-list {
      display: block;
    }
  }
}

.task-header {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
  
  .checkbox {
    width: 22px;
    height: 22px;
    border: 2px solid #E0E6ED;
    border-radius: 50%;
    margin-right: 12px;
    
    &.checked {
      background: #4CAF50;
      border-color: #4CAF50;
    }
  }
  
  .task-title {
    flex: 1;
    font-size: 16px;
    font-weight: 500;
    color: #2C3E50;
  }
  
  .task-priority {
    padding: 4px 10px;
    border-radius: 10px;
    font-size: 11px;
    
    &.priority-0 {
      background: #E8F5E9;
      color: #4CAF50;
    }
    
    &.priority-1 {
      background: #FFF3E0;
      color: #FF9800;
    }
    
    &.priority-2 {
      background: #FFEBEE;
      color: #F44336;
    }
  }
}

.task-info {
  display: flex;
  gap: 15px;
  font-size: 12px;
  color: #7F8C8D;
  margin-bottom: 10px;
}

.subtask-progress {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
  
  .progress-bar {
    flex: 1;
    height: 6px;
    background: #E0E6ED;
    border-radius: 3px;
    overflow: hidden;
    
    .progress-fill {
      height: 100%;
      background: #4CAF50;
      border-radius: 3px;
      transition: width 0.3s;
    }
  }
  
  .progress-text {
    font-size: 11px;
    color: #7F8C8D;
  }
}

.subtask-list {
  display: none;
  margin: 10px 0;
  padding: 10px;
  background: #F8F9FA;
  border-radius: 10px;
  
  .subtask-item {
    display: flex;
    align-items: center;
    padding: 8px 0;
    
    .subtask-checkbox {
      width: 18px;
      height: 18px;
      border: 2px solid #DDD;
      border-radius: 50%;
      margin-right: 10px;
      
      &.checked {
        background: #4CAF50;
        border-color: #4CAF50;
      }
    }
    
    .subtask-title {
      font-size: 14px;
      color: #333;
      
      &.completed {
        text-decoration: line-through;
        color: #999;
      }
    }
  }
  
  .add-subtask {
    text-align: center;
    padding: 10px;
    color: #4CAF50;
    font-size: 13px;
  }
}

.task-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  
  .task-tags {
    .task-tag {
      display: inline-block;
      padding: 4px 10px;
      background: #E3F2FD;
      color: #2196F3;
      border-radius: 8px;
      font-size: 11px;
    }
  }
  
  .expand-btn {
    font-size: 12px;
    color: #4CAF50;
  }
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  
  .empty-icon {
    font-size: 60px;
    margin-bottom: 16px;
    display: block;
  }
  
  .empty-text {
    font-size: 14px;
    color: #7F8C8D;
  }
}

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
  border-radius: 20px;
  padding: 20px;
  overflow-y: auto;
  
  &.detail-modal {
    width: 90%;
  }
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  font-size: 18px;
  font-weight: 600;
  
  .close-btn {
    font-size: 20px;
    color: #7F8C8D;
  }
}

.task-detail {
  .detail-title {
    font-size: 20px;
    font-weight: 600;
    margin-bottom: 10px;
  }
  
  .detail-info {
    display: flex;
    gap: 15px;
    font-size: 14px;
    color: #666;
    margin-bottom: 20px;
  }
}

.subtask-section {
  border-top: 1px solid #EEE;
  padding-top: 15px;
  
  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 15px;
    
    .add-btn {
      color: #4CAF50;
      font-size: 14px;
    }
  }
}

.subtask-list-detail {
  .subtask-item-detail {
    display: flex;
    align-items: center;
    padding: 12px 0;
    border-bottom: 1px solid #F0F0F0;
    
    .subtask-checkbox {
      width: 20px;
      height: 20px;
      border: 2px solid #DDD;
      border-radius: 50%;
      margin-right: 12px;
      
      &.checked {
        background: #4CAF50;
        border-color: #4CAF50;
      }
    }
    
    .subtask-title {
      flex: 1;
      font-size: 15px;
      
      &.completed {
        text-decoration: line-through;
        color: #999;
      }
    }
    
    .delete-btn {
      font-size: 16px;
      padding: 5px;
    }
  }
}

.no-subtask {
  text-align: center;
  padding: 30px;
  color: #999;
  font-size: 14px;
}

.form-item {
  margin-bottom: 20px;
  
  .label {
    display: block;
    font-size: 14px;
    color: #2C3E50;
    margin-bottom: 8px;
  }
  
  .input {
    width: 100%;
    height: 44px;
    background: #F5F7FA;
    border-radius: 10px;
    padding: 0 15px;
    font-size: 14px;
  }
  
  .picker {
    height: 44px;
    line-height: 44px;
    background: #F5F7FA;
    border-radius: 10px;
    padding: 0 15px;
    font-size: 14px;
    color: #7F8C8D;
  }
}

.priority-options {
  display: flex;
  gap: 10px;
  
  .priority-option {
    flex: 1;
    padding: 10px;
    text-align: center;
    background: #F5F7FA;
    border-radius: 10px;
    font-size: 14px;
    
    &.active {
      background: #4CAF50;
      color: #fff;
    }
  }
}

.form-actions {
  display: flex;
  gap: 12px;
  margin-top: 30px;
  
  button {
    flex: 1;
    height: 44px;
    border-radius: 22px;
    font-size: 15px;
    border: none;
  }
  
  .btn-cancel {
    background: #F5F7FA;
    color: #7F8C8D;
  }
  
  .btn-confirm {
    background: #4CAF50;
    color: #fff;
  }
}
</style>
