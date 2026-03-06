<template>
  <view class="page-container">
    <!-- 头部 -->
    <view class="header">
      <view class="header-title">待办事项</view>
      <view class="header-action" @click="openTaskModal()">
        <text class="icon">+</text>
      </view>
    </view>
    
    <!-- 分类标签 -->
    <scroll-view class="category-tabs" scroll-x>
      <view 
        v-for="(cat, index) in categories" 
        :key="cat.id || index"
        class="category-tab"
        :class="{ active: currentCategory === index }"
        @click="switchCategory(index)"
      >
        <text>{{ cat.name }}</text>
        <text class="category-count">({{ categoryCounts[index] || 0 }})</text>
      </view>
    </scroll-view>
    
    <!-- 任务列表 -->
    <scroll-view 
      class="task-list" 
      scroll-y
      @scrolltolower="loadMoreTasks"
      :lower-threshold="100"
    >
      <view 
        v-for="(task, index) in filteredTasks" 
        :key="task.id || index"
        class="task-card"
        :class="{ completed: task.status === 2 }"
        @click="openTaskModal(task)"
      >
        <view class="task-header">
          <view class="checkbox" :class="{ checked: task.status === 2 }" @click.stop="toggleTask(task)"></view>
          <view class="task-title">{{ task.title }}</view>
        </view>
        
        <view class="task-info">
          <text class="task-time" v-if="task.dueTime">⏰ {{ formatTime(task.dueTime) }}</text>
          <text class="task-assignee" v-if="task.assigneeName">👤 {{ task.assigneeName }}</text>
        </view>
        
        <view class="task-footer" v-if="task.remark">
          <text class="task-remark">{{ task.remark }}</text>
        </view>
      </view>
      
      <view v-if="filteredTasks.length === 0" class="empty-state">
        <text class="empty-icon">📝</text>
        <text class="empty-text">暂无任务，点击右上角添加</text>
      </view>
      
      <!-- 加载更多提示 -->
      <view v-if="filteredTasks.length > 0" class="load-more">
        <text v-if="isLoadingMore" class="load-more-text">加载中...</text>
        <text v-else-if="!hasMore" class="load-more-text">没有更多了</text>
        <text v-else class="load-more-text">上拉加载更多</text>
      </view>
    </scroll-view>
    
    <!-- 任务弹窗组件 -->
    <TaskModal ref="taskModalRef" @success="onTaskSaved" />
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { onShow, onLoad } from '@dcloudio/uni-app'
import { taskApi, familyApi } from '../../api/index.js'
import TaskModal from '@/components/TaskModal.vue'

// ========== 页面状态 ==========
const taskModalRef = ref(null)

const categories = ref([
  { name: '待办', id: 1, status: 0 },
  { name: '完成', id: 3, status: 2 }
])

const currentCategory = ref(0)
const tasks = ref([])
const loading = ref(false)
const isLoadingMore = ref(false)
const hasMore = ref(true)
const page = ref(1)
const pageSize = ref(20)

// 家庭成员
const familyMembers = ref([])

// 计算各分类数量
const categoryCounts = computed(() => {
  const todoCount = tasks.value.filter(t => t.status === 0 || t.status === 1).length
  const doneCount = tasks.value.filter(t => t.status === 2).length
  return [todoCount, doneCount]
})

// 过滤任务
const filteredTasks = computed(() => {
  try {
    if (!tasks.value || !Array.isArray(tasks.value)) {
      return []
    }
    const status = categories.value[currentCategory.value]?.status
    let result = tasks.value.filter(t => t.status === status)
    
    // 按截止时间排序
    if (result.length > 1) {
      const now = Date.now()
      result = result.slice().sort((a, b) => {
        const dateA = a.dueTime ? new Date(a.dueTime).getTime() : Infinity
        const dateB = b.dueTime ? new Date(b.dueTime).getTime() : Infinity
        
        if (dateA < now && dateB < now) return dateB - dateA
        if (dateA >= now && dateB >= now) return dateA - dateB
        return dateA >= now ? -1 : 1
      })
    }
    return result
  } catch(e) {
    console.error('filteredTasks error:', e)
    return []
  }
})

// ========== 生命周期 ==========
onLoad((options) => {
  // 检查是否需要打开添加弹窗（从首页跳转）- 兼容旧代码
  if (options && options.action === 'add') {
    setTimeout(() => {
      taskModalRef.value?.open()
    }, 300)
  }
})

onMounted(() => {
  loadTasks(true)
  loadFamilyMembers()
})

onShow(() => {
  loadTasks(true)
})

// ========== 数据加载 ==========
async function loadTasks(force = false, loadMore = false) {
  if (!force && isLoadingMore.value && !loadMore) return
  if (loadMore && !hasMore.value) return
  
  if (loadMore) {
    isLoadingMore.value = true
  } else {
    loading.value = true
    page.value = 1
    hasMore.value = true
  }
  
  try {
    const familyId = uni.getStorageSync('currentFamilyId') || 1
    const status = categories.value[currentCategory.value]?.status
    
    const res = await taskApi.getList({
      familyId,
      status,
      page: page.value,
      size: pageSize.value
    })
    
    const taskList = res.list || res.data || res || []
    const formattedTasks = taskList.map(task => ({
      ...task,
      assigneeName: getMemberName(task.assigneeId)
    }))
    
    if (loadMore) {
      tasks.value = [...tasks.value, ...formattedTasks]
    } else {
      tasks.value = formattedTasks
    }
    
    const total = res.total || 0
    const pages = res.pages || Math.ceil(total / pageSize.value) || 1
    hasMore.value = page.value < pages
    
  } catch (e) {
    console.error('加载任务失败', e)
    uni.showToast({ title: '加载任务失败', icon: 'none' })
  } finally {
    loading.value = false
    isLoadingMore.value = false
  }
}

async function loadFamilyMembers() {
  try {
    const familyId = uni.getStorageSync('currentFamilyId') || 1
    const members = await familyApi.getMembers(familyId)
    familyMembers.value = members || []
  } catch (e) {
    console.error('加载家庭成员失败', e)
    familyMembers.value = []
  }
}

function loadMoreTasks() {
  if (!hasMore.value || isLoadingMore.value) return
  page.value++
  loadTasks(false, true)
}

// ========== 弹窗操作 ==========
function openTaskModal(task = null) {
  if (task && task.id) {
    taskModalRef.value?.openEdit(task)
  } else {
    taskModalRef.value?.open()
  }
}

function onTaskSaved() {
  // 刷新任务列表
  loadTasks(true)
}

// ========== 任务操作 ==========
function switchCategory(index) {
  currentCategory.value = index
  loadTasks(true)
}

async function toggleTask(task) {
  const newStatus = task.status === 2 ? 0 : 2
  try {
    if (newStatus === 2) {
      await taskApi.complete(task.id)
    } else {
      await taskApi.restore(task.id)
    }
    task.status = newStatus
    uni.showToast({ title: newStatus === 2 ? '任务已完成' : '任务已恢复', icon: 'none' })
  } catch (e) {
    console.error('更新任务状态失败', e)
    uni.showToast({ title: '操作失败', icon: 'none' })
  }
}

// ========== 工具函数 ==========
function formatTime(timeValue) {
  if (!timeValue) return ''
  if (Array.isArray(timeValue)) {
    const [year, month, day, hour, minute] = timeValue
    return `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')} ${String(hour).padStart(2, '0')}:${String(minute).padStart(2, '0')}:00`
  }
  return timeValue
}
</script>

<style lang="scss" scoped>
.page-container {
  min-height: 100vh;
  background: #F8FBF8;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 60px 24px 24px;
  background: linear-gradient(180deg, #E8F5E9 0%, #F8FBF8 100%);
  
  .header-title {
    font-size: 28px;
    font-weight: 600;
    color: #2D5A3D;
    letter-spacing: 1px;
  }
  
  .header-action {
    position: fixed;
    right: 30rpx;
    bottom: 30rpx;
    width: 56px;
    height: 56px;
    background: linear-gradient(135deg, #81C784, #4CAF50);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 6px 20px rgba(76, 175, 80, 0.4);
    z-index: 100;
    
    .icon {
      font-size: 32px;
      color: #fff;
      font-weight: 300;
    }
  }
}

.category-tabs {
  padding: 16px 20px;
  white-space: nowrap;
  background: #fff;
  border-bottom: 1px solid #F0F5F0;
  
  .category-tab {
    display: inline-block;
    padding: 10px 24px;
    margin-right: 12px;
    background: #F5FAF5;
    border-radius: 24px;
    font-size: 14px;
    color: #8B9A8B;
    transition: all 0.3s ease;
    
    .category-count {
      margin-left: 4px;
      font-size: 12px;
    }
    
    &.active {
      background: linear-gradient(135deg, #81C784, #4CAF50);
      color: #fff;
      box-shadow: 0 4px 12px rgba(76, 175, 80, 0.25);
    }
  }
}

.task-list {
  padding: 15px;
  height: calc(100vh - 220px);
}

.task-card {
  background: #fff;
  border-radius: 20px;
  padding: 20px;
  margin: 12px 20px;
  box-shadow: 0 2px 16px rgba(0, 0, 0, 0.04);
  border: 1px solid #F5FAF5;
  transition: all 0.3s ease;
  
  &:active {
    transform: scale(0.98);
  }
  
  &.completed {
    opacity: 0.65;
    background: #FAFFFA;
    .task-title {
      text-decoration: line-through;
      color: #A8D8A8;
    }
  }
}

.task-header {
  display: flex;
  align-items: center;
  margin-bottom: 14px;
  
  .checkbox {
    width: 24px;
    height: 24px;
    border: 2px solid #C8E6C9;
    border-radius: 50%;
    margin-right: 14px;
    transition: all 0.3s ease;
    flex-shrink: 0;
    
    &.checked {
      background: linear-gradient(135deg, #81C784, #4CAF50);
      border-color: #4CAF50;
    }
  }
  
  .task-title {
    flex: 1;
    font-size: 17px;
    font-weight: 500;
    color: #3D5A4D;
    line-height: 1.4;
  }
}

.task-info {
  display: flex;
  gap: 16px;
  font-size: 13px;
  color: #8B9A8B;
  margin-bottom: 12px;
  flex-wrap: wrap;
}

.task-footer {
  .task-remark {
    font-size: 13px;
    color: #A8B8A8;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
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

.load-more {
  text-align: center;
  padding: 20px;
  
  .load-more-text {
    font-size: 13px;
    color: #8B9A8B;
  }
}
</style>