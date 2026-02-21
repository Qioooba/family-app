<template>
  <view class="task-page">
    <!-- 顶部统计 -->
    <view class="task-header">
      <view class="header-bg"></view>
      
      <view class="stats-row">
        <view class="stat-box">
          <text class="stat-num">{{ stats.pending }}</text>
          <text class="stat-label">待办</text>
        </view>
        
        <view class="stat-box">
          <text class="stat-num">{{ stats.today }}</text>
          <text class="stat-label">今日</text>
        </view>
        
        <view class="stat-box">
          <text class="stat-num">{{ stats.completed }}</text>
          <text class="stat-label">已完成</text>
        </view>
        
        <view class="stat-box">
          <text class="stat-num">{{ stats.completionRate }}%</text>
          <text class="stat-label">完成率</text>
        </view>
      </view>
    </view>
    
    <!-- 分类标签 -->
    <view class="category-tabs">
      <scroll-view scroll-x class="tabs-scroll">
        <view 
          v-for="(cat, index) in categories" 
          :key="index"
          class="tab-item"
          :class="{ active: currentCategory === cat.key }"
          @click="currentCategory = cat.key"
        >
          <text class="tab-icon">{{ cat.icon }}</text>
          <text class="tab-name">{{ cat.name }}</text>
          <text v-if="cat.count > 0" class="tab-badge">{{ cat.count }}</text>
        </view>
      </scroll-view>
    </view>
    
    <!-- 任务列表 -->
    <view class="task-list">
      <view v-if="filteredTasks.length === 0" class="empty-state">
        <image src="/static/empty-task.png" mode="aspectFit" />
        <text>暂无任务，去创建一个吧~</text>
      </view>
      
      <view 
        v-for="task in filteredTasks" 
        :key="task.id"
        class="task-card"
        :class="{ completed: task.status === 2, urgent: task.priority === 2 }"
        @click="goDetail(task)"
      >
        <!-- 左侧：完成按钮 -->
        <view class="task-check" @click.stop="toggleComplete(task)">
          <view v-if="task.status === 2" class="check-icon checked">✓</view>
          <view v-else class="check-icon"></view>
        </view>
        
        <!-- 中间：任务内容 -->
        <view class="task-content">
          <view class="task-title-row">
            <text class="task-title" :class="{ completed: task.status === 2 }">
              {{ task.title }}
            </text>
            
            <view v-if="task.priority === 2" class="urgent-tag">紧急</view>
          </view>
          
          <view class="task-desc" v-if="task.description">{{ task.description }}</view>
          
          <view class="task-meta">
            <view class="meta-item">
              <u-icon name="clock" size="20" color="#999"></u-icon>
              <text>{{ task.dueTime }}</text>
            </view>
            
            <view class="meta-item" v-if="task.assignee">
              <u-icon name="account" size="20" color="#999"></u-icon>
              <text>{{ task.assignee.nickname }}</text>
            </view>
            
            <view class="meta-item" v-if="task.subTasks && task.subTasks.length > 0">
              <u-icon name="list-dot" size="20" color="#999"></u-icon>
              <text>{{ getCompletedSubTasks(task) }}/{{ task.subTasks.length }}</text>
            </view>
          </view>
          
          <view class="task-tags" v-if="task.tags && task.tags.length > 0">
            <text 
              v-for="(tag, idx) in task.tags" 
              :key="idx"
              class="tag"
            >{{ tag }}</text>
          </view>
        </view>
        
        <!-- 右侧：更多操作 -->
        <view class="task-actions">
          <u-icon name="more-dot-fill" size="32" color="#ccc" @click.stop="showActions(task)"></u-icon>
        </view>
      </view>
    </view>
    
    <!-- 悬浮添加按钮 -->
    <view class="fab-btn" @click="createTask">
      <u-icon name="plus" size="48" color="#fff"></u-icon>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'

const currentCategory = ref('all')

const categories = [
  { key: 'all', name: '全部', icon: '📋', count: 0 },
  { key: 'shopping', name: '购物', icon: '🛒', count: 3 },
  { key: 'todo', name: '待办', icon: '📝', count: 5 },
  { key: 'housework', name: '家务', icon: '🏠', count: 2 },
  { key: 'finance', name: '财务', icon: '💰', count: 1 },
  { key: 'child', name: '育儿', icon: '👶', count: 0 },
  { key: 'health', name: '健康', icon: '🏥', count: 0 }
]

const stats = ref({
  pending: 8,
  today: 5,
  completed: 32,
  completionRate: 80
})

const tasks = ref([
  {
    id: 1,
    title: '去超市采购周末食材',
    description: '需要买：牛肉、西兰花、胡萝卜、牛奶',
    status: 0,
    priority: 1,
    dueTime: '今天 18:00',
    assignee: { nickname: '妈妈', avatar: '' },
    category: 'shopping',
    tags: ['超市', '食材'],
    subTasks: [
      { id: 1, title: '列购物清单', completed: true },
      { id: 2, title: '检查冰箱库存', completed: false }
    ]
  },
  {
    id: 2,
    title: '缴纳物业费',
    description: '本月物业费 320元',
    status: 0,
    priority: 2,
    dueTime: '今天 23:59',
    assignee: { nickname: '爸爸', avatar: '' },
    category: 'finance',
    tags: ['缴费']
  },
  {
    id: 3,
    title: '孩子数学作业检查',
    description: '检查第3单元测试卷',
    status: 0,
    priority: 1,
    dueTime: '明天 20:00',
    assignee: { nickname: '爸爸', avatar: '' },
    category: 'child',
    tags: ['作业', '数学']
  },
  {
    id: 4,
    title: '清洗空调滤网',
    description: '客厅和主卧两台空调',
    status: 2,
    priority: 0,
    dueTime: '昨天',
    assignee: { nickname: '爷爷', avatar: '' },
    category: 'housework',
    tags: ['清洁']
  }
])

const filteredTasks = computed(() => {
  if (currentCategory.value === 'all') {
    return tasks.value
  }
  return tasks.value.filter(t => t.category === currentCategory.value)
})

const getCompletedSubTasks = (task) => {
  return task.subTasks?.filter(s => s.completed).length || 0
}

const toggleComplete = (task) => {
  task.status = task.status === 2 ? 0 : 2
}

const goDetail = (task) => {
  uni.navigateTo({ url: `/pages/task/detail?id=${task.id}` })
}

const createTask = () => {
  uni.navigateTo({ url: '/pages/task/create' })
}

const showActions = (task) => {
  uni.showActionSheet({
    itemList: ['编辑', '删除', '指派给他人', '添加子任务'],
    success: (res) => {
      switch (res.tapIndex) {
        case 0:
          uni.navigateTo({ url: `/pages/task/edit?id=${task.id}` })
          break
        case 1:
          uni.showModal({
            title: '确认删除',
            content: '删除后无法恢复，是否继续？',
            success: (modalRes) => {
              if (modalRes.confirm) {
                // 删除任务
                const index = tasks.value.findIndex(t => t.id === task.id)
                if (index > -1) tasks.value.splice(index, 1)
              }
            }
          })
          break
        case 2:
          uni.navigateTo({ url: `/pages/task/assign?id=${task.id}` })
          break
        case 3:
          uni.navigateTo({ url: `/pages/task/subtask?id=${task.id}` })
          break
      }
    }
  })
}
</script>

<style lang="scss" scoped>
.task-page {
  min-height: 100vh;
  background: #f5f6fa;
}

.task-header {
  position: relative;
  padding: 40rpx;
  padding-top: 60rpx;
  
  .header-bg {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 300rpx;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 0 0 40rpx 40rpx;
  }
  
  .stats-row {
    position: relative;
    display: flex;
    justify-content: space-around;
    background: #fff;
    border-radius: 20rpx;
    padding: 30rpx;
    box-shadow: 0 4rpx 20rpx rgba(0,0,0,0.08);
    
    .stat-box {
      text-align: center;
      
      .stat-num {
        font-size: 48rpx;
        font-weight: bold;
        color: #333;
        display: block;
      }
      
      .stat-label {
        font-size: 24rpx;
        color: #999;
        margin-top: 8rpx;
      }
    }
  }
}

.category-tabs {
  margin: 30rpx 0;
  
  .tabs-scroll {
    white-space: nowrap;
    padding: 0 20rpx;
    
    .tab-item {
      display: inline-flex;
      align-items: center;
      padding: 16rpx 32rpx;
      margin-right: 16rpx;
      background: #fff;
      border-radius: 32rpx;
      
      &.active {
        background: #5B8FF9;
        
        .tab-name {
          color: #fff;
        }
      }
      
      .tab-icon {
        margin-right: 8rpx;
      }
      
      .tab-name {
        font-size: 26rpx;
        color: #666;
      }
      
      .tab-badge {
        margin-left: 8rpx;
        padding: 2rpx 12rpx;
        background: #ff4d4f;
        color: #fff;
        font-size: 20rpx;
        border-radius: 20rpx;
      }
    }
  }
}

.task-list {
  padding: 0 30rpx 120rpx;
  
  .empty-state {
    text-align: center;
    padding: 100rpx 0;
    
    image {
      width: 240rpx;
      height: 240rpx;
      margin-bottom: 30rpx;
    }
    
    text {
      font-size: 28rpx;
      color: #999;
    }
  }
  
  .task-card {
    display: flex;
    align-items: flex-start;
    background: #fff;
    border-radius: 16rpx;
    padding: 24rpx;
    margin-bottom: 20rpx;
    box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.04);
    
    &.urgant {
      border-left: 6rpx solid #ff4d4f;
    }
    
    .task-check {
      margin-right: 20rpx;
      padding-top: 4rpx;
      
      .check-icon {
        width: 44rpx;
        height: 44rpx;
        border: 2rpx solid #ddd;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 24rpx;
        color: transparent;
        
        &.checked {
          background: #5AD8A6;
          border-color: #5AD8A6;
          color: #fff;
        }
      }
    }
    
    .task-content {
      flex: 1;
      min-width: 0;
      
      .task-title-row {
        display: flex;
        align-items: center;
        margin-bottom: 12rpx;
        
        .task-title {
          font-size: 30rpx;
          color: #333;
          font-weight: 500;
          
          &.completed {
            text-decoration: line-through;
            color: #999;
          }
        }
        
        .urgent-tag {
          margin-left: 12rpx;
          padding: 4rpx 12rpx;
          background: #FFF1F0;
          color: #ff4d4f;
          font-size: 20rpx;
          border-radius: 8rpx;
        }
      }
      
      .task-desc {
        font-size: 26rpx;
        color: #666;
        margin-bottom: 12rpx;
        line-height: 1.5;
      }
      
      .task-meta {
        display: flex;
        flex-wrap: wrap;
        gap: 20rpx;
        margin-bottom: 12rpx;
        
        .meta-item {
          display: flex;
          align-items: center;
          
          text {
            margin-left: 8rpx;
            font-size: 24rpx;
            color: #999;
          }
        }
      }
      
      .task-tags {
        display: flex;
        flex-wrap: wrap;
        gap: 12rpx;
        
        .tag {
          padding: 6rpx 16rpx;
          background: #F0F5FF;
          color: #5B8FF9;
          font-size: 22rpx;
          border-radius: 8rpx;
        }
      }
    }
    
    .task-actions {
      padding: 8rpx;
    }
  }
}

.fab-btn {
  position: fixed;
  right: 40rpx;
  bottom: 160rpx;
  width: 100rpx;
  height: 100rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8rpx 24rpx rgba(102, 126, 234, 0.4);
  z-index: 100;
}
</style>
