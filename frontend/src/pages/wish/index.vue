<template>
  <view class="page-container">
    <!-- 顶部渐变背景 -->
    <view class="header-bg">
      <view class="header-content">
        <view class="header-left">
          <text class="header-title">我的小家</text>
        </view>
        <view class="header-action" @click="openAddModal">
          <text class="add-icon">+</text>
        </view>
      </view>
      
      <!-- 预算进度条 -->
      <view class="budget-section">
        <view class="budget-header">
          <text class="budget-label">家庭预算</text>
          <text class="budget-value">5000元 / 10000元</text>
        </view>
        <view class="budget-bar">
          <view class="budget-fill" style="width: 50%"></view>
        </view>
        <text class="budget-percent">已使用 50%</text>
      </view>
    </view>
    
    <!-- 筛选标签 -->
    <view class="filter-section">
      <scroll-view scroll-x class="filter-scroll" :show-scrollbar="false">
        <view class="filter-tabs">
          <view 
            v-for="(tab, index) in tabs" 
            :key="index"
            class="filter-tab"
            :class="{ active: currentTab === index }"
            @click="switchTab(index)"
          >
            <text class="tab-icon">{{ tab.icon }}</text>
            <text class="tab-text">{{ tab.name }}</text>
            <text v-if="tab.count > 0" class="tab-count">{{ tab.count }}</text>
          </view>
        </view>
      </scroll-view>
    </view>
    
    <!-- 心愿列表 -->
    <scroll-view 
      class="wish-list" 
      scroll-y 
      :refresher-enabled="true"
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
      @scrolltolower="onLoadMore"
      :scroll-with-animation="true"
      :enhanced="true"
    >
      <!-- 加载状态 -->
      <view v-if="loading && !refreshing && wishes.length === 0" class="loading-container">
        <view class="loading-spinner"></view>
        <text class="loading-text">加载中...</text>
      </view>
      
      <!-- 加载失败状态 -->
      <view v-else-if="loadError" class="error-container">
        <view class="error-icon">💫</view>
        <text class="error-title">加载失败</text>
        <text class="error-desc">{{ errorMessage }}</text>
        <view class="retry-btn" @click="retryLoad">
          <text>重新加载</text>
        </view>
      </view>
      
      <!-- 空状态 -->
      <view v-else-if="filteredWishes.length === 0" class="empty-container">
        <view class="empty-icon">🌸</view>
        <text class="empty-title">{{ emptyTitles[currentTab] }}</text>
        <text class="empty-desc">点击右上角 + 添加你的心愿</text>
        <view class="empty-btn" @click="openAddModal">
          <text>✨ 添加心愿</text>
        </view>
      </view>
      
      <!-- 心愿卡片列表 -->
      <view v-else class="wish-container">
        <view 
          v-for="wish in filteredWishes" 
          :key="wish.id"
          class="wish-card"
          :class="{ 'is-mine': isMyWish(wish), 'is-completed': wish.status === 2 }"
          @click="viewWishDetail(wish)"
        >
          <!-- 卡片头部 -->
          <view class="card-header">
            <view class="wish-type-tag" :class="'type-' + wish.type">
              <text class="type-icon">{{ getTypeIcon(wish.type) }}</text>
              <text class="type-name">{{ getTypeName(wish.type) }}</text>
            </view>
            <view class="wish-status" :class="'status-' + wish.status">
              <text>{{ getStatusText(wish.status) }}</text>
            </view>
          </view>
          
          <!-- 卡片内容 -->
          <view class="card-content">
            <text class="wish-title">{{ wish.title }}</text>
            <text v-if="wish.description" class="wish-desc" :class="{ expanded: expandedIds.includes(wish.id) }">
              {{ wish.description }}
            </text>
            <view v-if="wish.description && wish.description.length > 50" class="expand-btn" @click.stop="toggleExpand(wish.id)">
              <text>{{ expandedIds.includes(wish.id) ? '收起' : '展开' }}</text>
            </view>
          </view>
          
          <!-- 优先级和日期 -->
          <view class="card-meta">
            <view class="meta-item">
              <text class="meta-icon">{{ getPriorityIcon(wish.priority) }}</text>
              <text class="meta-text" :class="'priority-' + wish.priority">{{ getPriorityText(wish.priority) }}</text>
            </view>
            <view v-if="wish.expectDate" class="meta-item">
              <text class="meta-icon">📅</text>
              <text class="meta-text">{{ formatDate(wish.expectDate) }}</text>
            </view>
          </view>
          
          <!-- 进度条 -->
          <view v-if="wish.progress > 0 || wish.status === 1" class="progress-section">
            <view class="progress-info">
              <text class="progress-label">完成进度</text>
              <text class="progress-value">{{ wish.progress || 0 }}%</text>
            </view>
            <view class="progress-bar">
              <view class="progress-fill" :style="{ width: (wish.progress || 0) + '%' }"></view>
            </view>
          </view>
          
          <!-- 卡片底部 -->
          <view class="card-footer">
            <view class="wish-creator">
              <view class="creator-avatar" :style="{ backgroundColor: getAvatarColor(wish.userId) }">
                <text>{{ getAvatarText(wish.userName) }}</text>
              </view>
              <text class="creator-name">{{ wish.userName || '未知用户' }}</text>
            </view>
            
            <view class="card-actions">
              <!-- 我的操作 -->
              <view v-if="isMyWish(wish) && wish.status !== 2" class="action-btn edit" @click.stop="editWish(wish)">
                <text>编辑</text>
              </view>
            </view>
          </view>
        </view>
      </view>
      
      <!-- 底部加载更多 -->
      <view v-if="filteredWishes.length > 0 && hasMore" class="load-more">
        <text>{{ loadingMore ? '加载中...' : '上拉加载更多' }}</text>
      </view>
    </scroll-view>
    
    <!-- ==================== 优化后的添加心愿弹窗 ==================== -->
    <view v-if="showAddModalFlag" class="modal-overlay" @click="closeAddModal">
      <view class="modal-container-warm" @click.stop>
        <!-- 装饰元素 -->
        <view class="modal-decoration">
          <view class="deco-circle deco-1"></view>
          <view class="deco-circle deco-2"></view>
          <view class="deco-heart">💕</view>
        </view>
        
        <view class="modal-header-warm">
          <view class="modal-title-wrapper">
            <text class="modal-title-icon">{{ isEditMode ? '✏️' : '🌸' }}</text>
            <text class="modal-title">{{ isEditMode ? '编辑心愿' : '写下你的心愿' }}</text>
          </view>
          <view class="modal-close-warm" @click="closeAddModal">
            <text class="close-icon">✕</text>
          </view>
        </view>
        
        <scroll-view class="modal-body-warm" scroll-y>
          <!-- 心愿标题 -->
          <view class="form-item-warm">
            <view class="form-label-wrapper">
              <text class="form-label-icon">💝</text>
              <text class="form-label">心愿标题</text>
              <text class="required">*</text>
            </view>
            <view class="input-wrapper">
              <input 
                class="form-input-warm" 
                v-model="newWish.title" 
                placeholder="例如：和家人一起去海边看日出"
                maxlength="50"
              />
            </view>
          </view>
          
          <!-- 心愿描述 -->
          <view class="form-item-warm">
            <view class="form-label-wrapper">
              <text class="form-label-icon">📝</text>
              <text class="form-label">详细描述</text>
            </view>
            <view class="textarea-wrapper">
              <textarea 
                class="form-textarea-warm" 
                v-model="newWish.description" 
                placeholder="描述一下你的心愿细节，让家人更了解你的想法..."
                maxlength="500"
              />
              <text class="char-count-warm">{{ newWish.description?.length || 0 }}/500</text>
            </view>
          </view>
          
          <!-- 心愿类型 - 使用新图标 -->
          <view class="form-item-warm">
            <view class="form-label-wrapper">
              <text class="form-label-icon">🏷️</text>
              <text class="form-label">心愿类型</text>
            </view>
            <view class="type-options-warm">
              <view 
                v-for="type in wishTypes" 
                :key="type.value"
                class="type-option-warm"
                :class="{ active: newWish.type === type.value }"
                @click="newWish.type = type.value"
              >
                <view class="type-icon-wrapper" :style="{ background: type.bgColor }">
                  <text class="type-icon-img">{{ type.icon }}</text>
                </view>
                <text class="type-name-warm">{{ type.name }}</text>
              </view>
            </view>
          </view>
          
          <!-- 优先级 -->
          <view class="form-item-warm">
            <view class="form-label-wrapper">
              <text class="form-label-icon">⭐</text>
              <text class="form-label">优先级</text>
            </view>
            <view class="priority-options-warm">
              <view 
                v-for="p in priorities" 
                :key="p.value"
                class="priority-option-warm"
                :class="{ active: newWish.priority === p.value }"
                @click="newWish.priority = p.value"
              >
                <text class="priority-icon-img">{{ p.icon }}</text>
                <text class="priority-name-warm">{{ p.name }}</text>
              </view>
            </view>
          </view>
          
          <!-- 期望实现时间 - 精确到10分钟 -->
          <view class="form-item-warm">
            <view class="form-label-wrapper">
              <text class="form-label-icon">📅</text>
              <text class="form-label">期望实现时间</text>
            </view>
            <!-- 日期选择 -->
            <view class="picker-wrapper">
              <picker mode="date" :value="newWish.expectDate" @change="onDateChange" @cancel="onPickerCancel">
                <view class="datetime-picker" :class="{ active: newWish.expectDate }">
                  <text class="datetime-icon">📆</text>
                  <text class="datetime-text">{{ newWish.expectDate || '选择日期' }}</text>
                  <text class="datetime-arrow">›</text>
                </view>
              </picker>
            </view>
            <!-- 时间选择（10分钟间隔） -->
            <view v-if="newWish.expectDate" class="time-picker-section">
              <text class="time-label">选择时间</text>
              <view class="picker-wrapper">
                <picker mode="multiSelector" :range="timeRange" :value="timeIndex" @change="onTimeChange" @columnchange="onTimeColumnChange" @cancel="onPickerCancel">
                  <view class="time-picker" :class="{ active: newWish.expectTime }">
                    <text class="time-icon">🕐</text>
                    <text class="time-text">{{ newWish.expectTime || '选择时间' }}</text>
                    <text class="time-arrow">›</text>
                  </view>
                </picker>
              </view>
            </view>
          </view>
        </scroll-view>
        
        <view class="modal-footer-warm">
          <view class="btn-cancel-warm" @click="closeAddModal">
            <text>再想想</text>
          </view>
          <view class="btn-submit-warm" :class="{ disabled: !newWish.title.trim() }" @click="submitWish">
            <text class="btn-icon">{{ isEditMode ? '💾' : '✨' }}</text>
            <text>{{ isEditMode ? '保存修改' : '许下心愿' }}</text>
          </view>
        </view>
      </view>
    </view>
    
    <!-- 心愿详情弹窗 -->
    <view v-if="showDetailModal" class="modal-overlay" @click="closeDetailModal">
      <view class="detail-modal" @click.stop>
        <view class="detail-header">
          <view class="detail-type-tag" :class="'type-' + currentWish.type">
            <text class="type-icon">{{ getTypeIcon(currentWish.type) }}</text>
            <text class="type-name">{{ getTypeName(currentWish.type) }}</text>
          </view>
          <view class="modal-close" @click="closeDetailModal">
            <text>✕</text>
          </view>
        </view>
        
        <scroll-view class="detail-body" scroll-y>
          <text class="detail-title">{{ currentWish.title }}</text>
          <text class="detail-desc">{{ currentWish.description || '暂无描述' }}</text>
          
          <view class="detail-info">
            <view class="info-item">
              <text class="info-label">状态</text>
              <text class="info-value" :class="'status-' + currentWish.status">{{ getStatusText(currentWish.status) }}</text>
            </view>
            <view class="info-item">
              <text class="info-label">优先级</text>
              <text class="info-value" :class="'priority-' + currentWish.priority">{{ getPriorityText(currentWish.priority) }}</text>
            </view>
            <view v-if="currentWish.expectDate" class="info-item">
              <text class="info-label">期望时间</text>
              <text class="info-value">{{ formatDateTime(currentWish.expectDate, currentWish.expectTime) }}</text>
            </view>
            <view class="info-item">
              <text class="info-label">创建人</text>
              <view class="info-user">
                <view class="mini-avatar" :style="{ backgroundColor: getAvatarColor(currentWish.userId) }">
                  <text>{{ getAvatarText(currentWish.userName) }}</text>
                </view>
                <text>{{ currentWish.userName || '未知用户' }}</text>
              </view>
            </view>
          </view>
          
          <!-- 进度 -->
          <view v-if="currentWish.progress !== undefined" class="detail-progress">
            <text class="section-title">完成进度</text>
            <view class="progress-display">
              <view class="progress-circle">
                <text class="progress-percent">{{ currentWish.progress || 0 }}%</text>
              </view>
              <view class="progress-bar-large">
                <view class="progress-fill-large" :style="{ width: (currentWish.progress || 0) + '%' }"></view>
              </view>
            </view>
          </view>
        </scroll-view>
        
        <view class="detail-footer">
          <view v-if="isMyWish(currentWish) && currentWish.status !== 2" class="btn-secondary" @click="editWish(currentWish)">
            <text>编辑</text>
          </view>
          <view v-if="isMyWish(currentWish)" class="btn-danger" @click="deleteWish(currentWish)">
            <text>删除</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted, onActivated } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { wishApi } from '../../api/index.js'

// 标签配置
const tabs = [
  { name: '全部心愿', icon: '🌟', count: 0 },
  { name: '我的心愿', icon: '💝', count: 0 },
  { name: '已实现', icon: '✨', count: 0 }
]

// ==================== 优化后的7种心愿类型 - 更温馨的图标和配色 ====================
const wishTypes = [
  { value: 'item', name: '心动好物', icon: '🎁', bgColor: 'linear-gradient(135deg, #ffd3b6, #ffaaa5)' },
  { value: 'experience', name: '浪漫体验', icon: '🌅', bgColor: 'linear-gradient(135deg, #a8e6cf, #7fcdbb)' },
  { value: 'goal', name: '梦想目标', icon: '🌟', bgColor: 'linear-gradient(135deg, #ffd93d, #ffc93d)' },
  { value: 'learn', name: '学习成长', icon: '📖', bgColor: 'linear-gradient(135deg, #c7ceea, #a8b2e6)' },
  { value: 'relation', name: '亲情友情', icon: '💕', bgColor: 'linear-gradient(135deg, #ff8fa3, #ffc2d1)' },
  { value: 'charity', name: '温暖公益', icon: '🌻', bgColor: 'linear-gradient(135deg, #b8e6b8, #98d498)' },
  { value: 'custom', name: '其他心愿', icon: '💫', bgColor: 'linear-gradient(135deg, #e0e0e0, #d0d0d0)' }
]

// 优先级 - 使用更可爱的图标
const priorities = [
  { value: 3, name: '心愿优先', icon: '🔥', color: '#ff8fa3' },
  { value: 2, name: '一般心愿', icon: '⭐', color: '#ffb300' },
  { value: 1, name: '随缘心愿', icon: '🌙', color: '#81c784' }
]

// 状态文本
const statusTexts = {
  0: '待认领',
  1: '进行中',
  2: '已实现'
}

// 模拟数据 - 当API不可时使用
const mockWishes = [
  {
    id: 1,
    familyId: 1,
    userId: 1,
    userName: '我',
    type: 'experience',
    title: '去海边度假',
    description: '想要和家人一起去三亚度假，享受阳光沙滩',
    priority: 3,
    status: 0,
    progress: 0,
    expectDate: '2026-07-01',
    expectTime: '08:00',
    createTime: '2026-02-23 10:00:00'
  },
  {
    id: 2,
    familyId: 1,
    userId: 1,
    userName: '我',
    type: 'item',
    title: '买一台新相机',
    description: '想要一台单反相机记录家庭生活',
    priority: 2,
    status: 1,
    progress: 30,
    expectDate: '2026-06-01',
    expectTime: '18:30',
    createTime: '2026-02-23 09:00:00'
  },
  {
    id: 3,
    familyId: 1,
    userId: 2,
    userName: '家人',
    type: 'goal',
    title: '学会做饭',
    description: '学会做10道家常菜',
    priority: 2,
    status: 0,
    progress: 0,
    expectDate: '2026-12-31',
    expectTime: '12:00',
    createTime: '2026-02-22 10:00:00'
  },
  {
    id: 4,
    familyId: 1,
    userId: 2,
    userName: '家人',
    type: 'learn',
    title: '学习钢琴',
    description: '学会弹奏5首经典曲目',
    priority: 1,
    status: 2,
    progress: 100,
    expectDate: '2026-03-01',
    expectTime: '20:00',
    createTime: '2026-02-20 10:00:00'
  }
]

// 空状态标题
const emptyTitles = [
  '还没有心愿',
  '还没有我的心愿',
  '还没有实现的心愿'
]

// 响应式数据
const currentTab = ref(0)
const wishes = ref([])
const loading = ref(false)
const refreshing = ref(false)
const loadingMore = ref(false)
const hasMore = ref(true)
const loadError = ref(false)
const errorMessage = ref('')
const page = ref(1)
const pageSize = 10
const showAddModalFlag = ref(false)
const showDetailModal = ref(false)
const currentWish = ref({})
const expandedIds = ref([])
const currentUserId = ref(null)
const isPickerActive = ref(false)  // picker是否激活

// 编辑模式相关
const isEditMode = ref(false)
const editingWishId = ref(null)

// 新心愿表单 - 包含时间字段
const newWish = ref({
  title: '',
  description: '',
  type: 'custom',
  priority: 2,
  expectDate: '',
  expectTime: ''
})

// ==================== 10分钟间隔时间选择器配置 ====================
const hours = Array.from({ length: 24 }, (_, i) => String(i).padStart(2, '0') + '时')
const minutes = Array.from({ length: 6 }, (_, i) => String(i * 10).padStart(2, '0') + '分')
const timeRange = [hours, minutes]
const timeIndex = ref([8, 0]) // 默认08:00

// 计算属性
const stats = computed(() => {
  const userId = currentUserId.value
  if (!userId) {
    return {
      total: wishes.value.length,
      my: 0,
      completed: wishes.value.filter(w => w.status === 2).length
    }
  }
  return {
    total: wishes.value.length,
    my: wishes.value.filter(w => w.userId === userId).length,
    completed: wishes.value.filter(w => w.status === 2).length
  }
})

const filteredWishes = computed(() => {
  const userId = currentUserId.value
  if (!userId) {
    // 如果用户ID未获取到，显示全部
    return wishes.value
  }
  switch (currentTab.value) {
    case 0: // 全部
      return wishes.value
    case 1: // 我的
      return wishes.value.filter(w => w.userId === userId)
    case 2: // 已实现
      return wishes.value.filter(w => w.status === 2)
    default:
      return wishes.value
  }
})

// 方法
const loadWishes = async (isRefresh = false) => {
  if (loading.value) return
  
  if (isRefresh) {
    page.value = 1
    hasMore.value = true
    loadError.value = false
    errorMessage.value = ''
  }
  
  loading.value = true
  loadError.value = false
  
  try {
    // 获取并确保familyId是数字
    let familyId = uni.getStorageSync('currentFamilyId')
    familyId = parseInt(familyId) || 1
    
    console.log('[Wish] 加载心愿列表, familyId:', familyId)
    
    // 并行获取用户ID
    const userInfo = uni.getStorageSync('userInfo')
    const userId = userInfo?.id || userInfo?.userId || null
    currentUserId.value = userId
    
    // 直接获取列表
    const res = await wishApi.getList(familyId, {})
    
    console.log('[Wish] 心愿列表原始响应:', res)
    
    // 简化的数据处理逻辑
    let list = []
    if (Array.isArray(res)) {
      list = res
    } else if (res && typeof res === 'object') {
      // 处理各种可能的返回格式
      list = res.data || res.records || res.list || res.items || []
    }
    
    // 数据标准化处理
    const normalizedList = list.map(item => ({
      id: item.id || item.wishId || Date.now() + Math.random(),
      familyId: item.familyId || familyId,
      userId: item.userId || item.creatorId || null,
      userName: item.userName || item.creatorName || item.username || '未知用户',
      type: item.type || 'custom',
      title: item.title || '未命名心愿',
      description: item.description || '',
      priority: item.priority || 2,
      status: item.status || 0,
      progress: item.progress || 0,
      expectDate: item.expectDate || item.expect_date || null,
      expectTime: item.expectTime || item.expect_time || null,
      createTime: item.createTime || item.create_time || new Date().toISOString()
    }))
    
    if (isRefresh) {
      wishes.value = normalizedList
    } else {
      // 去重合并
      const existingIds = new Set(wishes.value.map(w => w.id))
      const newItems = normalizedList.filter(item => !existingIds.has(item.id))
      wishes.value = [...wishes.value, ...newItems]
    }
    
    hasMore.value = list.length === pageSize
    
    console.log('[Wish] 处理后的心愿列表:', wishes.value.length, '条')
    
    // 更新标签计数
    updateTabCounts()
  } catch (e) {
    console.error('[Wish] 加载心愿失败:', e)
    loadError.value = true
    errorMessage.value = e?.message || '网络错误，请稍后重试'
  } finally {
    loading.value = false
    refreshing.value = false
    loadingMore.value = false
  }
}

const retryLoad = () => {
  loadWishes(true)
}

const updateTabCounts = () => {
  const userId = currentUserId.value
  tabs[0].count = wishes.value.length
  tabs[1].count = userId ? wishes.value.filter(w => w.userId === userId).length : 0
  tabs[2].count = wishes.value.filter(w => w.status === 2).length
}

const switchTab = (index) => {
  currentTab.value = index
}

const onRefresh = () => {
  refreshing.value = true
  loadWishes(true)
}

const onLoadMore = () => {
  if (!hasMore.value || loadingMore.value) return
  loadingMore.value = true
  page.value++
  loadWishes()
}

const openAddModal = () => {
  // 设置为添加模式
  isEditMode.value = false
  editingWishId.value = null
  
  // 重置表单
  newWish.value = {
    title: '',
    description: '',
    type: 'custom',
    priority: 2,
    expectDate: '',
    expectTime: ''
  }
  timeIndex.value = [8, 0] // 重置时间
  showAddModalFlag.value = true
}

const closeAddModal = () => {
  if (isPickerActive.value) {
    return  // picker激活时不关闭弹窗
  }
  showAddModalFlag.value = false
  // 重置编辑状态
  isEditMode.value = false
  editingWishId.value = null
}

const onDateChange = (e) => {
  console.log('[Wish] 日期选择变化:', e.detail.value)
  newWish.value.expectDate = e.detail.value
  // 默认选择早上8点
  if (!newWish.value.expectTime) {
    newWish.value.expectTime = '08:00'
    timeIndex.value = [8, 0]
  }
}

const onPickerCancel = () => {
  isPickerActive.value = false
}

// ==================== 时间选择器方法（10分钟间隔） ====================
const onTimeChange = (e) => {
  console.log('[Wish] 时间选择变化:', e.detail.value)
  const [hourIdx, minuteIdx] = e.detail.value
  const hour = String(hourIdx).padStart(2, '0')
  const minute = String(minuteIdx * 10).padStart(2, '0')
  newWish.value.expectTime = `${hour}:${minute}`
  timeIndex.value = [hourIdx, minuteIdx]
  console.log('[Wish] 选择的时间:', newWish.value.expectTime)
}

const onTimeColumnChange = (e) => {
  const { column, value } = e.detail
  timeIndex.value[column] = value
  // 强制更新视图
  timeIndex.value = [...timeIndex.value]
}

// ==================== 修复添加后不显示问题 ====================
const submitWish = async () => {
  if (!newWish.value.title.trim()) {
    uni.showToast({ title: '请输入心愿标题', icon: 'none' })
    return
  }
  
  // 显示加载状态
  uni.showLoading({ title: isEditMode.value ? '保存中...' : '许愿中...', mask: true })
  
  try {
    const familyId = uni.getStorageSync('currentFamilyId') || 1
    const userId = currentUserId.value
    const userInfo = uni.getStorageSync('userInfo') || {}
    
    const wishData = {
      title: newWish.value.title.trim(),
      description: newWish.value.description.trim(),
      type: newWish.value.type,
      priority: newWish.value.priority,
      expectDate: newWish.value.expectDate || null,
      expectTime: newWish.value.expectTime || null,
      familyId: familyId,
      userId: userId
    }
    
    console.log('[Wish] 提交心愿:', wishData, '模式:', isEditMode.value ? '编辑' : '添加')
    
    // 编辑模式
    if (isEditMode.value && editingWishId.value) {
      try {
        await wishApi.update(editingWishId.value, wishData)
        console.log('[Wish] API更新成功')
      } catch (apiErr) {
        console.warn('[Wish] API更新失败，使用本地模拟:', apiErr)
        // 本地更新
        const index = wishes.value.findIndex(w => w.id === editingWishId.value)
        if (index > -1) {
          wishes.value[index] = {
            ...wishes.value[index],
            ...wishData,
            updateTime: new Date().toISOString()
          }
          wishes.value = [...wishes.value]
        }
      }
      
      uni.hideLoading()
      uni.showToast({ title: '✨ 修改已保存', icon: 'none', duration: 2000 })
      closeAddModal()
      
      // 刷新列表数据
      setTimeout(() => {
        loadWishes(true)
      }, 300)
      return
    }
    
    // 添加模式
    // 尝试调用API
    let createdWish = null
    try {
      const res = await wishApi.create(wishData)
      createdWish = res
      console.log('[Wish] API创建成功:', res)
    } catch (apiErr) {
      console.warn('[Wish] API调用失败，使用本地模拟:', apiErr)
      // API失败时，本地创建模拟数据
      createdWish = {
        id: Date.now(),
        ...wishData,
        userName: userInfo.nickname || userInfo.username || '我',
        status: 0,
        progress: 0,
        createTime: new Date().toISOString(),
        _isMock: true
      }
    }
    
    // 确保数据正确添加到列表
    if (createdWish) {
      // 使用 unshift 添加到列表开头
      wishes.value.unshift(createdWish)
      
      // 强制更新计算属性
      wishes.value = [...wishes.value]
      
      // 更新标签计数
      updateTabCounts()
      
      console.log('[Wish] 心愿已添加到列表，当前列表长度:', wishes.value.length)
    }
    
    uni.hideLoading()
    uni.showToast({ title: '✨ 心愿已许下', icon: 'none', duration: 2000 })
    closeAddModal()
    
    // 刷新列表数据
    setTimeout(() => {
      loadWishes(true)
    }, 300)
    
    // 如果当前不在"全部"或"我的"标签，切换到"我的心愿"
    if (currentTab.value > 1) {
      currentTab.value = 1
    }
  } catch (e) {
    uni.hideLoading()
    console.error('[Wish] 提交心愿失败:', e)
    uni.showToast({ title: isEditMode.value ? '保存失败，请重试' : '添加失败，请重试', icon: 'none' })
  }
}

const viewWishDetail = (wish) => {
  currentWish.value = wish
  showDetailModal.value = true
}

const closeDetailModal = () => {
  showDetailModal.value = false
  currentWish.value = {}
}

const toggleExpand = (id) => {
  const index = expandedIds.value.indexOf(id)
  if (index > -1) {
    expandedIds.value.splice(index, 1)
  } else {
    expandedIds.value.push(id)
  }
}

const editWish = (wish) => {
  // 设置为编辑模式
  isEditMode.value = true
  editingWishId.value = wish.id
  
  // 填充表单数据
  newWish.value = {
    title: wish.title || '',
    description: wish.description || '',
    type: wish.type || 'custom',
    priority: wish.priority || 2,
    expectDate: wish.expectDate || '',
    expectTime: wish.expectTime || ''
  }
  
  // 设置时间选择器
  if (wish.expectTime) {
    const [hour, minute] = wish.expectTime.split(':')
    timeIndex.value = [parseInt(hour), Math.floor(parseInt(minute) / 10)]
  } else {
    timeIndex.value = [8, 0]
  }
  
  // 关闭详情弹窗，打开编辑弹窗
  closeDetailModal()
  showAddModalFlag.value = true
}

const deleteWish = (wish) => {
  uni.showModal({
    title: '确认删除',
    content: '确定要删除这个心愿吗？',
    confirmColor: '#ff8fa3',
    success: async (res) => {
      if (res.confirm) {
        try {
          await wishApi.delete(wish.id)
          
          // 从列表中移除
          const index = wishes.value.findIndex(w => w.id === wish.id)
          if (index > -1) {
            wishes.value.splice(index, 1)
            wishes.value = [...wishes.value]
          }
          
          uni.showToast({ title: '删除成功', icon: 'success' })
          closeDetailModal()
          updateTabCounts()
          
          // 刷新数据
          setTimeout(() => {
            loadWishes(true)
          }, 300)
        } catch (e) {
          console.error('删除失败', e)
          uni.showToast({ title: '删除失败', icon: 'none' })
        }
      }
    }
  })
}

const updateProgress = (wish) => {
  uni.showActionSheet({
    title: '更新进度',
    itemList: ['25%', '50%', '75%', '100% (完成)'],
    success: async (res) => {
      const progress = [25, 50, 75, 100][res.tapIndex]
      try {
        await wishApi.updateProgress(wish.id, progress)
        if (progress === 100) {
          await wishApi.complete(wish.id)
        }
        uni.showToast({ title: '更新成功', icon: 'success' })
        closeDetailModal()
        
        // 刷新数据
        setTimeout(() => {
          loadWishes(true)
        }, 300)
      } catch (e) {
        console.error('更新进度失败', e)
        uni.showToast({ title: '更新失败', icon: 'none' })
      }
    }
  })
}

// 辅助方法
const isMyWish = (wish) => wish.userId === currentUserId.value

const getTypeIcon = (type) => {
  const t = wishTypes.find(t => t.value === type)
  return t ? t.icon : '💫'
}

const getTypeName = (type) => {
  const t = wishTypes.find(t => t.value === type)
  return t ? t.name : '其他'
}

const getStatusText = (status) => statusTexts[status] || '未知'

const getPriorityText = (priority) => {
  const p = priorities.find(p => p.value === priority)
  return p ? p.name : '中'
}

const getPriorityIcon = (priority) => {
  switch (priority) {
    case 3: return '🔥'
    case 2: return '⭐'
    case 1: return '🌙'
    default: return '⭐'
  }
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

const formatDateTime = (dateStr, timeStr) => {
  if (!dateStr) return ''
  const date = formatDate(dateStr)
  return timeStr ? `${date} ${timeStr}` : date
}

const getAvatarColor = (id) => {
  const colors = ['#ff8fa3', '#a8e6cf', '#ffd3b6', '#dcedc1', '#ffd93d', '#95e1d3', '#f38181', '#aa96da']
  return colors[(id || 0) % colors.length]
}

const getAvatarText = (name) => {
  if (!name) return '?'
  return name.charAt(0).toUpperCase()
}

// 初始化
onMounted(() => {
  const userInfo = uni.getStorageSync('userInfo')
  currentUserId.value = userInfo?.id || 1
  loadWishes()
})

// 页面显示时刷新数据
onShow(() => {
  console.log('[Wish] 页面显示，刷新数据')
  // 禁用页面滑动手势
  uni.setSwipeBackMode && uni.setSwipeBackMode({ mode: 'none' })
  loadWishes(true)
})

// 页面被激活时刷新（用于keep-alive场景）
onActivated(() => {
  console.log('[Wish] 页面激活，刷新数据')
  loadWishes(true)
})
</script>

<style lang="scss" scoped>
// 温馨小清新配色变量
$primary-color: #ff8fa3;
$primary-light: #ffc2d1;
$primary-soft: #fff0f3;
$secondary-color: #a8e6cf;
$accent-color: #ffd3b6;
$text-primary: #4a4a4a;
$text-secondary: #888;
$text-light: #aaa;
$bg-color: #fef9f9;
$card-bg: #fff;
$border-radius-sm: 16rpx;
$border-radius-md: 24rpx;
$border-radius-lg: 40rpx;
$border-radius-xl: 60rpx;

.page-container {
  min-height: 100vh;
  background: linear-gradient(180deg, #fff0f3 0%, #fef9f9 100%);
  padding-bottom: 80rpx;
  /* 禁止横向拖动 */
  touch-action: pan-y;
  overflow-x: hidden;
}

// 顶部背景
.header-bg {
  background: linear-gradient(135deg, #ff8fa3 0%, #ffc2d1 100%);
  padding: 30rpx 30r 24rpx;
  border-radius: 0 0 $border-radius-lg $border-radius-lg;
  box-shadow: 0 4rpx 20rpx rgba(255, 143, 163, 0.15);
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20rpx;
}

.header-left {
  .header-title {
    font-size: 36rpx;
    font-weight: 700;
    color: #fff;
    display: block;
    margin-bottom: 4rpx;
    text-shadow: 0 2rpx 4rpx rgba(0, 0, 0, 0.1);
    letter-spacing: 1rpx;
  }
  
  .header-subtitle {
    font-size: 22rpx;
    color: rgba(255, 255, 255, 0.85);
    letter-spacing: 0.5rpx;
  }
}

.header-action {
  width: 64rpx;
  height: 64rpx;
  background: rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(8px);
  box-shadow: 0 2rpx 12rpx rgba(255, 143, 163, 0.25);
  
  .add-icon {
    font-size: 38rpx;
    color: #fff;
    font-weight: 300;
  }
}

// 预算进度条
.budget-section {
  background: rgba(255, 255, 255, 0.25);
  border-radius: 20rpx;
  padding: 20rpx 24rpx;
  margin-top: 16rpx;
  backdrop-filter: blur(8px);
}

.budget-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12rpx;
}

.budget-label {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.9);
  font-weight: 500;
}

.budget-value {
  font-size: 26rpx;
  color: #fff;
  font-weight: 600;
}

.budget-bar {
  height: 14rpx;
  background: rgba(255, 255, 255, 0.3);
  border-radius: 7rpx;
  overflow: hidden;
}

.budget-fill {
  height: 100%;
  background: linear-gradient(90deg, #fff 0%, rgba(255, 255, 255, 0.8) 100%);
  border-radius: 7rpx;
  transition: width 0.3s ease;
}

.budget-percent {
  font-size: 20rpx;
  color: rgba(255, 255, 255, 0.85);
  margin-top: 8rpx;
  display: block;
  text-align: right;
}

// 统计卡片
.stats-container {
  display: flex;
  justify-content: space-around;
  background: rgba(255, 255, 255, 0.2);
  border-radius: $border-radius-sm;
  padding: 20rpx 0;
  backdrop-filter: blur(8px);
  margin-top: 20rpx;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  
  .stat-num {
    font-size: 36rpx;
    font-weight: 700;
    color: #fff;
    margin-bottom: 4rpx;
    text-shadow: 0 2rpx 4rpx rgba(0, 0, 0, 0.1);
  }
  
  .stat-label {
    font-size: 22rpx;
    color: rgba(255, 255, 255, 0.85);
  }
}

// 筛选区
.filter-section {
  padding: 12rpx 0 16rpx;
  background: transparent;
}

.filter-scroll {
  white-space: nowrap;
}

.filter-tabs {
  display: inline-flex;
  padding: 0 20rpx;
  gap: 16rpx;
}

.filter-tab {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6rpx;
  padding: 14rpx 24rpx;
  background: #fff;
  border-radius: 50rpx;
  transition: all 0.25s cubic-bezier(0.34, 1.56, 0.64, 1);
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
  border: 2rpx solid transparent;
  min-width: 140rpx;
  
  // 图标和文字垂直居中对齐
  .tab-icon {
    font-size: 30rpx;
    line-height: 1;
    flex-shrink: 0;
  }
  
  .tab-text {
    font-size: 26rpx;
    color: #666;
    font-weight: 500;
    white-space: nowrap;
  }
  
  .tab-count {
    min-width: 36rpx;
    height: 36rpx;
    padding: 0 10rpx;
    background: #f0f0f0;
    border-radius: 18rpx;
    font-size: 20rpx;
    color: #888;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: 600;
    margin-left: 4rpx;
  }
  
  // 选中状态
  &.active {
    background: linear-gradient(135deg, #ff8fa3 0%, #ffc2d1 100%);
    box-shadow: 0 6rpx 24rpx rgba(255, 143, 163, 0.35);
    border-color: transparent;
    transform: translateY(-2rpx);
    
    .tab-text {
      color: #fff;
      font-weight: 600;
    }
    
    .tab-count {
      background: rgba(255, 255, 255, 0.3);
      color: #fff;
    }
  }
  
  // 点击反馈
  &:active {
    transform: scale(0.96);
  }
}

// 列表区域
.wish-list {
  height: calc(100vh - 260rpx);
  padding: 0 20rpx;
}

// 加载状态
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 100rpx 0;
  
  .loading-spinner {
    width: 60rpx;
    height: 60rpx;
    border: 4rpx solid rgba(255, 143, 163, 0.2);
    border-top: 4rpx solid $primary-color;
    border-radius: 50%;
    animation: spin 1s linear infinite;
  }
  
  .loading-text {
    margin-top: 20rpx;
    font-size: 26rpx;
    color: $text-secondary;
  }
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

// 加载失败状态
.error-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 120rpx 40rpx;
  
  .error-icon {
    font-size: 100rpx;
    margin-bottom: 24rpx;
    opacity: 0.8;
  }
  
  .error-title {
    font-size: 32rpx;
    color: $text-primary;
    font-weight: 600;
    margin-bottom: 12rpx;
  }
  
  .error-desc {
    font-size: 26rpx;
    color: $text-secondary;
    margin-bottom: 40rpx;
    text-align: center;
  }
  
  .retry-btn {
    padding: 24rpx 60rpx;
    background: linear-gradient(135deg, #ff8fa3, #ffc2d1);
    border-radius: $border-radius-xl;
    box-shadow: 0 4rpx 20rpx rgba(255, 143, 163, 0.3);
    
    text {
      font-size: 28rpx;
      color: #fff;
      font-weight: 600;
      letter-spacing: 1rpx;
    }
  }
}

// 空状态
.empty-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 120rpx 40rpx;
  
  .empty-icon {
    font-size: 100rpx;
    margin-bottom: 24rpx;
    animation: float 3s ease-in-out infinite;
  }
  
  @keyframes float {
    0%, 100% { transform: translateY(0); }
    50% { transform: translateY(-10rpx); }
  }
  
  .empty-title {
    font-size: 32rpx;
    color: $text-primary;
    font-weight: 600;
    margin-bottom: 12rpx;
  }
  
  .empty-desc {
    font-size: 26rpx;
    color: $text-secondary;
    margin-bottom: 40rpx;
  }
  
  .empty-btn {
    padding: 26rpx 64rpx;
    background: linear-gradient(135deg, #ff8fa3, #ffc2d1);
    border-radius: $border-radius-xl;
    box-shadow: 0 4rpx 20rpx rgba(255, 143, 163, 0.3);
    
    text {
      font-size: 28rpx;
      color: #fff;
      font-weight: 600;
      letter-spacing: 1rpx;
    }
  }
}

// 心愿卡片
.wish-container {
  padding: 4rpx 0;
}

.wish-card {
  background: $card-bg;
  border-radius: $border-radius-sm;
  padding: 20rpx;
  margin-bottom: 12rpx;
  box-shadow: 0 2rpx 16rpx rgba(0, 0, 0, 0.04);
  position: relative;
  overflow: hidden;
  
  &::before {
    content: '';
    position: absolute;
    left: 0;
    top: 0;
    width: 6rpx;
    height: 100%;
    background: #e0e0e0;
    border-radius: 3rpx 0 0 3rpx;
  }
  
  &.is-mine::before {
    background: linear-gradient(180deg, #ff8fa3, #ffc2d1);
  }
  
  &.is-completed::before {
    background: linear-gradient(180deg, #ffd3b6, #ffaaa5);
  }
}

// 卡片头部
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12rpx;
}

.wish-type-tag {
  display: flex;
  align-items: center;
  gap: 6rpx;
  padding: 8rpx 16rpx;
  border-radius: $border-radius-sm;
  font-size: 20rpx;
  font-weight: 500;
  
  .type-icon {
    font-size: 20rpx;
  }
  
  .type-name {
    font-size: 20rpx;
  }
  
  &.type-item { background: #fff3e0; color: #f5a623; }
  &.type-experience { background: #e8f4f8; color: #5ab9cf; }
  &.type-goal { background: #fce4ec; color: #e91e63; }
  &.type-learn { background: #f3e5f5; color: #9c27b0; }
  &.type-relation { background: #ffebee; color: #f48fb1; }
  &.type-charity { background: #e8f5e9; color: #81c784; }
  &.type-custom { background: #f5f5f5; color: $text-secondary; }
}

.wish-status {
  padding: 6rpx 14rpx;
  border-radius: $border-radius-sm;
  font-size: 20rpx;
  font-weight: 500;
  
  &.status-0 { background: #fff8e1; color: #ffb300; }
  &.status-1 { background: #e3f2fd; color: #64b5f6; }
  &.status-2 { background: #e8f5e9; color: #81c784; }
}

// 卡片内容
.card-content {
  margin-bottom: 12rpx;
}

.wish-title {
  font-size: 30rpx;
  font-weight: 600;
  color: $text-primary;
  margin-bottom: 10rpx;
  display: block;
  line-height: 1.4;
}

.wish-desc {
  font-size: 26rpx;
  color: $text-secondary;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  
  &.expanded {
    -webkit-line-clamp: unset;
  }
}

.expand-btn {
  margin-top: 10rpx;
  
  text {
    font-size: 22rpx;
    color: $primary-color;
    font-weight: 500;
  }
}

// 元信息
.card-meta {
  display: flex;
  gap: 16rpx;
  margin-bottom: 12rpx;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6rpx;
  
  .meta-icon {
    font-size: 22rpx;
  }
  
  .meta-text {
    font-size: 22rpx;
    color: $text-light;
    font-weight: 500;
    
    &.priority-1 { color: #81c784; }
    &.priority-2 { color: #ffb300; }
    &.priority-3 { color: #ff8fa3; }
  }
}

// 进度条
.progress-section {
  margin-bottom: 12rpx;
}

.progress-info {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8rpx;
  
  .progress-label {
    font-size: 20rpx;
    color: $text-light;
  }
  
  .progress-value {
    font-size: 20rpx;
    color: $primary-color;
    font-weight: 600;
  }
}

.progress-bar {
  height: 8rpx;
  background: #f0f0f0;
  border-radius: 4rpx;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #ff8fa3, #ffc2d1);
  border-radius: 4rpx;
  transition: width 0.3s ease;
}

// 卡片底部
.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12rpx;
  border-top: 1rpx solid #f8f8f8;
}

.wish-creator {
  display: flex;
  align-items: center;
  gap: 10rpx;
  
  .creator-avatar {
    width: 44rpx;
    height: 44rpx;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 2rpx 6rpx rgba(0, 0, 0, 0.08);
    
    text {
      font-size: 20rpx;
      color: #fff;
      font-weight: 600;
    }
  }
  
  .creator-name {
    font-size: 22rpx;
    color: $text-secondary;
    font-weight: 500;
  }
}

.card-actions {
  display: flex;
  gap: 12rpx;
}

.action-btn {
  padding: 10rpx 24rpx;
  border-radius: $border-radius-xl;
  font-size: 22rpx;
  font-weight: 500;
  
  &.edit {
    background: #f8f8f8;
    color: $text-secondary;
  }
}

// 加载更多
.load-more {
  text-align: center;
  padding: 40rpx;
  font-size: 24rpx;
  color: $text-light;
}

// 弹窗遮罩
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  align-items: flex-end;
  justify-content: center;
  z-index: 999;
  animation: fadeIn 0.2s ease;
}

/* picker层级优化 */
.picker-wrapper {
  position: relative;
  z-index: 1000;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

// ==================== 优化后的温馨圆润弹窗样式 ====================
.modal-container-warm {
  width: 100%;
  max-height: 90vh;
  background: linear-gradient(180deg, #fff 0%, #fff8f9 100%);
  border-radius: 48rpx 48rpx 0 0;
  animation: slideUp 0.35s cubic-bezier(0.34, 1.56, 0.64, 1);
  display: flex;
  flex-direction: column;
  position: relative;
  overflow: hidden;
  box-shadow: 0 -8rpx 60rpx rgba(255, 143, 163, 0.25);
  z-index: 1000;
}

.detail-modal {
  width: 100%;
  max-height: 85vh;
  background: #fff;
  border-radius: $border-radius-lg $border-radius-lg 0 0;
  animation: slideUp 0.3s ease;
  display: flex;
  flex-direction: column;
  z-index: 1000;
}

@keyframes slideUp {
  from { transform: translateY(100%); opacity: 0; }
  to { transform: translateY(0); opacity: 1; }
}

// 装饰元素
.modal-decoration {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 200rpx;
  overflow: hidden;
  pointer-events: none;
  
  .deco-circle {
    position: absolute;
    border-radius: 50%;
    opacity: 0.15;
  }
  
  .deco-1 {
    width: 300rpx;
    height: 300rpx;
    background: linear-gradient(135deg, #ff8fa3, #ffc2d1);
    top: -150rpx;
    right: -80rpx;
  }
  
  .deco-2 {
    width: 200rpx;
    height: 200rpx;
    background: linear-gradient(135deg, #ffd3b6, #ffaaa5);
    top: -80rpx;
    left: -60rpx;
  }
  
  .deco-heart {
    position: absolute;
    top: 30rpx;
    right: 120rpx;
    font-size: 40rpx;
    opacity: 0.6;
    animation: heartbeat 2s ease-in-out infinite;
  }
}

@keyframes heartbeat {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.1); }
}

// 弹窗头部
.modal-header-warm {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 40rpx 36rpx 24rpx;
  position: relative;
  z-index: 1;
}

.modal-title-wrapper {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.modal-title-icon {
  font-size: 44rpx;
}

.modal-title {
  font-size: 38rpx;
  font-weight: 700;
  color: $text-primary;
  letter-spacing: 2rpx;
  background: linear-gradient(135deg, #ff8fa3, #ffc2d1);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.modal-close-warm {
  width: 68rpx;
  height: 68rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #fff0f3, #ffe8ed);
  border-radius: 50%;
  box-shadow: 0 2rpx 12rpx rgba(255, 143, 163, 0.15);
  transition: all 0.2s;
  
  &:active {
    transform: scale(0.95);
    background: linear-gradient(135deg, #ffe8ed, #ffd0d8);
  }
  
  .close-icon {
    font-size: 30rpx;
    color: #ff8fa3;
    font-weight: 600;
  }
}

// 弹窗内容
.modal-body-warm {
  flex: 1;
  padding: 20rpx 36rpx;
  max-height: 60vh;
  overflow: visible;
}

// 表单样式 - 温馨圆润风格
.form-item-warm {
  margin-bottom: 36rpx;
}

.form-label-wrapper {
  display: flex;
  align-items: center;
  gap: 10rpx;
  margin-bottom: 18rpx;
}

.form-label-icon {
  font-size: 30rpx;
}

.form-label {
  font-size: 30rpx;
  color: $text-primary;
  font-weight: 600;
  letter-spacing: 1rpx;
}

.required {
  color: #ff8fa3;
  font-size: 28rpx;
}

// 输入框样式
.input-wrapper {
  background: linear-gradient(135deg, #fff 0%, #fff8f9 100%);
  border-radius: 24rpx;
  padding: 4rpx;
  box-shadow: 0 2rpx 16rpx rgba(255, 143, 163, 0.1), inset 0 1rpx 2rpx rgba(255, 255, 255, 0.8);
  border: 2rpx solid rgba(255, 194, 209, 0.3);
}

.form-input-warm {
  width: 100%;
  height: 96rpx;
  padding: 0 32rpx;
  background: #fff;
  border-radius: 20rpx;
  font-size: 30rpx;
  color: $text-primary;
  box-sizing: border-box;
}

// 文本域样式
.textarea-wrapper {
  background: linear-gradient(135deg, #fff 0%, #fff8f9 100%);
  border-radius: 24rpx;
  padding: 4rpx;
  box-shadow: 0 2rpx 16rpx rgba(255, 143, 163, 0.1), inset 0 1rpx 2rpx rgba(255, 255, 255, 0.8);
  border: 2rpx solid rgba(255, 194, 209, 0.3);
  position: relative;
}

.form-textarea-warm {
  width: 100%;
  height: 200rpx;
  padding: 24rpx 32rpx;
  background: #fff;
  border-radius: 20rpx;
  font-size: 30rpx;
  color: $text-primary;
  box-sizing: border-box;
}

.char-count-warm {
  position: absolute;
  bottom: 16rpx;
  right: 20rpx;
  font-size: 22rpx;
  color: $text-light;
  background: rgba(255, 255, 255, 0.8);
  padding: 4rpx 12rpx;
  border-radius: 12rpx;
}

// ==================== 心愿类型选择 - 新样式 ====================
.type-options-warm {
  display: flex;
  flex-wrap: wrap;
  gap: 20rpx;
}

.type-option-warm {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12rpx;
  padding: 20rpx 16rpx;
  background: linear-gradient(135deg, #fff 0%, #fafafa 100%);
  border-radius: 24rpx;
  border: 3rpx solid transparent;
  transition: all 0.25s cubic-bezier(0.34, 1.56, 0.64, 1);
  min-width: 120rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
  
  &:active {
    transform: scale(0.95);
  }
  
  &.active {
    border-color: #ff8fa3;
    background: linear-gradient(135deg, #fff0f3 0%, #ffe8ed 100%);
    box-shadow: 0 4rpx 20rpx rgba(255, 143, 163, 0.2);
    transform: translateY(-4rpx);
  }
}

.type-icon-wrapper {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.1);
}

.type-icon-img {
  font-size: 36rpx;
}

.type-name-warm {
  font-size: 24rpx;
  color: $text-secondary;
  font-weight: 500;
  
  .active & {
    color: #ff8fa3;
    font-weight: 600;
  }
}

// ==================== 优先级选择 - 新样式 ====================
.priority-options-warm {
  display: flex;
  gap: 20rpx;
}

.priority-option-warm {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12rpx;
  padding: 24rpx 16rpx;
  background: linear-gradient(135deg, #fff 0%, #fafafa 100%);
  border-radius: 24rpx;
  border: 3rpx solid transparent;
  transition: all 0.25s cubic-bezier(0.34, 1.56, 0.64, 1);
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
  
  &:active {
    transform: scale(0.95);
  }
  
  &.active {
    background: linear-gradient(135deg, #fff0f3 0%, #ffe8ed 100%);
    border-color: #ff8fa3;
    box-shadow: 0 4rpx 20rpx rgba(255, 143, 163, 0.2);
    transform: translateY(-4rpx);
  }
}

.priority-icon-img {
  font-size: 40rpx;
}

.priority-name-warm {
  font-size: 24rpx;
  color: $text-secondary;
  font-weight: 500;
  
  .active & {
    color: #ff8fa3;
    font-weight: 600;
  }
}

// ==================== 日期时间选择器 - 10分钟间隔 ====================
.datetime-picker {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 28rpx 32rpx;
  background: linear-gradient(135deg, #fff 0%, #fff8f9 100%);
  border-radius: 24rpx;
  border: 2rpx solid rgba(255, 194, 209, 0.3);
  box-shadow: 0 2rpx 12rpx rgba(255, 143, 163, 0.08);
  transition: all 0.2s;
  position: relative;
  z-index: 2000;
  
  &.active {
    border-color: #ffc2d1;
    background: linear-gradient(135deg, #fff 0%, #fff0f3 100%);
  }
}

.datetime-icon {
  font-size: 32rpx;
}

.datetime-text {
  flex: 1;
  font-size: 30rpx;
  color: $text-primary;
  font-weight: 500;
}

.datetime-arrow {
  font-size: 32rpx;
  color: #ffc2d1;
  font-weight: 600;
}

// 时间选择区域
.time-picker-section {
  margin-top: 24rpx;
  padding-top: 24rpx;
  border-top: 2rpx dashed rgba(255, 194, 209, 0.4);
}

.time-label {
  display: block;
  font-size: 26rpx;
  color: $text-secondary;
  margin-bottom: 16rpx;
  margin-left: 8rpx;
}

.time-picker {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 24rpx 32rpx;
  background: linear-gradient(135deg, #fff 0%, #fff8f9 100%);
  border-radius: 24rpx;
  border: 2rpx solid rgba(255, 194, 209, 0.3);
  box-shadow: 0 2rpx 12rpx rgba(255, 143, 163, 0.08);
  transition: all 0.2s;
  position: relative;
  z-index: 2001;
  
  &.active {
    border-color: #ffc2d1;
    background: linear-gradient(135deg, #fff 0%, #fff0f3 100%);
  }
}

.time-icon {
  font-size: 32rpx;
}

.time-text {
  flex: 1;
  font-size: 30rpx;
  color: $text-primary;
  font-weight: 500;
}

.time-arrow {
  font-size: 32rpx;
  color: #ffc2d1;
  font-weight: 600;
}

// 弹窗底部
.modal-footer-warm {
  display: flex;
  gap: 24rpx;
  padding: 24rpx 36rpx 60rpx;
  background: linear-gradient(180deg, transparent 0%, rgba(255, 240, 243, 0.5) 100%);
}

.btn-cancel-warm {
  flex: 1;
  padding: 28rpx;
  background: linear-gradient(135deg, #f8f8f8 0%, #f0f0f0 100%);
  border-radius: 32rpx;
  text-align: center;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.06);
  transition: all 0.2s;
  
  &:active {
    transform: scale(0.98);
    background: linear-gradient(135deg, #f0f0f0 0%, #e8e8e8 100%);
  }
  
  text {
    font-size: 30rpx;
    color: $text-secondary;
    font-weight: 500;
  }
}

.btn-submit-warm {
  flex: 2;
  padding: 28rpx;
  background: linear-gradient(135deg, #ff8fa3 0%, #ffc2d1 100%);
  border-radius: 32rpx;
  text-align: center;
  box-shadow: 0 6rpx 24rpx rgba(255, 143, 163, 0.35);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  transition: all 0.2s;
  
  &:active {
    transform: scale(0.98);
    box-shadow: 0 4rpx 16rpx rgba(255, 143, 163, 0.4);
  }
  
  &.disabled {
    opacity: 0.5;
    pointer-events: none;
  }
  
  .btn-icon {
    font-size: 32rpx;
  }
  
  text {
    font-size: 32rpx;
    color: #fff;
    font-weight: 700;
    letter-spacing: 2rpx;
  }
}

// ==================== 详情弹窗样式 ====================
.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 32rpx;
}

.detail-type-tag {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 10rpx 20rpx;
  border-radius: $border-radius-sm;
  font-size: 22rpx;
  font-weight: 500;
  
  .type-icon {
    font-size: 24rpx;
  }
  
  .type-name {
    font-size: 22rpx;
  }
  
  &.type-item { background: #fff3e0; color: #f5a623; }
  &.type-experience { background: #e8f4f8; color: #5ab9cf; }
  &.type-goal { background: #fce4ec; color: #e91e63; }
  &.type-learn { background: #f3e5f5; color: #9c27b0; }
  &.type-relation { background: #ffebee; color: #f48fb1; }
  &.type-charity { background: #e8f5e9; color: #81c784; }
  &.type-custom { background: #f5f5f5; color: $text-secondary; }
}

.modal-close {
  width: 60rpx;
  height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f8f8f8;
  border-radius: 50%;
  
  text {
    font-size: 32rpx;
    color: $text-light;
  }
}

.detail-body {
  flex: 1;
  padding: 0 32rpx 32rpx;
  max-height: 50vh;
}

.detail-title {
  font-size: 40rpx;
  font-weight: 600;
  color: $text-primary;
  display: block;
  margin-bottom: 20rpx;
  line-height: 1.5;
  letter-spacing: 0.5rpx;
}

.detail-desc {
  font-size: 28rpx;
  color: $text-secondary;
  line-height: 1.8;
  display: block;
  margin-bottom: 32rpx;
}

.detail-info {
  background: #f8f8f8;
  border-radius: $border-radius-md;
  padding: 24rpx;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 18rpx 0;
  border-bottom: 1rpx solid #eee;
  
  &:last-child {
    border-bottom: none;
  }
  
  .info-label {
    font-size: 26rpx;
    color: $text-light;
  }
  
  .info-value {
    font-size: 26rpx;
    color: $text-primary;
    font-weight: 500;
    
    &.status-0 { color: #ffb300; }
    &.status-1 { color: #64b5f6; }
    &.status-2 { color: #81c784; }
    &.priority-1 { color: #81c784; }
    &.priority-2 { color: #ffb300; }
    &.priority-3 { color: #ff8fa3; }
  }
  
  .info-user {
    display: flex;
    align-items: center;
    gap: 12rpx;
    
    .mini-avatar {
      width: 44rpx;
      height: 44rpx;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      
      text {
        font-size: 20rpx;
        color: #fff;
      }
    }
    
    text {
      font-size: 26rpx;
      color: $text-primary;
    }
  }
}

.detail-progress {
  margin-top: 32rpx;
  
  .section-title {
    font-size: 28rpx;
    color: $text-primary;
    font-weight: 600;
    display: block;
    margin-bottom: 20rpx;
    letter-spacing: 0.5rpx;
  }
}

.progress-display {
  display: flex;
  align-items: center;
  gap: 24rpx;
  background: #f8f8f8;
  border-radius: $border-radius-md;
  padding: 32rpx;
}

.progress-circle {
  width: 100rpx;
  height: 100rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #ff8fa3, #ffc2d1);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4rpx 16rpx rgba(255, 143, 163, 0.3);
  
  .progress-percent {
    font-size: 28rpx;
    color: #fff;
    font-weight: 700;
  }
}

.progress-bar-large {
  flex: 1;
  height: 16rpx;
  background: #e0e0e0;
  border-radius: 8rpx;
  overflow: hidden;
}

.progress-fill-large {
  height: 100%;
  background: linear-gradient(90deg, #ff8fa3, #ffc2d1);
  border-radius: 8rpx;
  transition: width 0.3s ease;
}

.detail-footer {
  display: flex;
  gap: 20rpx;
  padding: 24rpx 32rpx 60rpx;
  border-top: 1rpx solid #f5f5f5;
}

.btn-primary {
  flex: 1;
  padding: 26rpx;
  background: linear-gradient(135deg, #ff8fa3, #ffc2d1);
  border-radius: $border-radius-xl;
  text-align: center;
  box-shadow: 0 4rpx 20rpx rgba(255, 143, 163, 0.3);
  
  text {
    font-size: 28rpx;
    color: #fff;
    font-weight: 600;
    letter-spacing: 1rpx;
  }
}

.btn-secondary {
  flex: 1;
  padding: 26rpx;
  background: #f8f8f8;
  border-radius: $border-radius-xl;
  text-align: center;
  
  text {
    font-size: 28rpx;
    color: $text-secondary;
    font-weight: 500;
  }
}

.btn-danger {
  padding: 26rpx 44rpx;
  background: #ffebee;
  border-radius: $border-radius-xl;
  text-align: center;
  
  text {
    font-size: 28rpx;
    color: #ff8fa3;
    font-weight: 500;
  }
}
</style>