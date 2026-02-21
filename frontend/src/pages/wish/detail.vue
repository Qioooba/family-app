<template>
  <view class="wish-detail-page">
    <view class="nav-bar">
      <view class="back-btn" @click="goBack">
        <u-icon name="arrow-left" size="40" color="#333"></u-icon>
      </view>
      <text class="title">心愿详情</text>
      <view class="more-btn" @click="showActions">
        <u-icon name="more-dot-fill" size="40" color="#333"></u-icon>
      </view>
    </view>
    
    <view class="detail-container">
      <!-- 心愿头部 -->
      <view class="wish-header">
        <view class="wish-type">{{ getTypeLabel }}</view>
        <text class="content">{{ wish.content }}</text>
        
        <view class="author">
          <image :src="wish.authorAvatar" class="avatar" />
          <text>{{ wish.authorName }} 的心愿</text>
        </view>
      </view>
      
      <!-- 进度 -->
      <view class="section-card">
        <view class="section-title">实现进度</view>
        
        <view class="progress-section">
          <view class="progress-bar">
            <view class="progress-fill" :style="{ width: wish.progress + '%' }"></view>
          </view>
          <text class="progress-text">{{ wish.progress }}%</text>
        </view>
        
        <view class="budget-info" v-if="wish.budget">
          <text>预计花费: ¥{{ wish.budget }}</text>
        </view>
      </view>
      
      <!-- 认领人 -->
      <view class="section-card" v-if="wish.claimer">
        <view class="section-title">认领人</view>
        
        <view class="claimer-info">
          <image :src="wish.claimerAvatar" class="avatar" />
          <view class="info">
            <text class="name">{{ wish.claimerName }}</text>
            <text class="time">于 {{ wish.claimTime }} 认领</text>
          </view>
        </view>
      </view>
      
      <!-- 预算 -->
      <view class="section-card budget-section">
        <view class="section-title">
          <text>预算</text>
          <view class="budget-edit" @click="showBudgetModal">
            <u-icon name="edit-pen" size="28" color="#5B8FF9"></u-icon>
            <text>编辑</text>
          </view>
        </view>

        <view v-if="wish.targetAmount" class="budget-content">
          <view class="budget-progress">
            <view class="progress-header">
              <text class="current">¥{{ wish.currentAmount || 0 }}</text>
              <text class="target">/ ¥{{ wish.targetAmount }}</text>
            </view>
            <view class="progress-bar">
              <view class="progress-fill" :style="{ width: budgetProgress + '%' }"></view>
            </view>
            <text class="progress-percent">{{ budgetProgress }}%</text>
          </view>
        </view>
        <view v-else class="budget-empty" @click="showBudgetModal">
          <text>设置预算目标 🎯</text>
        </view>
      </view>

      <!-- 里程碑 -->
      <view class="section-card milestone-section">
        <view class="section-title">
          <text>里程碑</text>
          <view class="milestone-add" @click="showMilestoneModal">
            <u-icon name="plus" size="28" color="#5B8FF9"></u-icon>
            <text>添加</text>
          </view>
        </view>

        <view v-if="milestones.length > 0" class="milestone-list">
          <view
            v-for="(milestone, index) in milestones"
            :key="milestone.id"
            class="milestone-item"
            :class="{ completed: milestone.status === 1 }"
          >
            <view class="milestone-left">
              <view class="milestone-dot" :class="{ active: milestone.status === 1 }"></view>
              <view class="milestone-line" v-if="index < milestones.length - 1"></view>
            </view>
            <view class="milestone-content">
              <text class="milestone-title">{{ milestone.title }}</text>
              <text v-if="milestone.description" class="milestone-desc">{{ milestone.description }}</text>
              <view class="milestone-footer">
                <text class="milestone-date">{{ formatDate(milestone.targetDate) }}</text>
                <view v-if="milestone.status === 0 && wish.claimer === currentUserId" class="milestone-actions">
                  <text class="complete-btn" @click="completeMilestone(milestone.id)">完成</text>
                  <text class="delete-btn" @click="deleteMilestone(milestone.id)">删除</text>
                </view>
              </view>
            </view>
          </view>
        </view>
        <view v-else class="milestone-empty">
          <text>还没有里程碑，添加一个吧 📝</text>
        </view>
      </view>

      <!-- 描述 -->
      <view class="section-card" v-if="wish.description">
        <view class="section-title">详细描述</view>
        <text class="description">{{ wish.description }}</text>
      </view>
      
      <!-- 操作按钮 -->
      <view class="action-buttons">
        <view 
          v-if="!wish.claimer && wish.authorId !== currentUserId"
          class="btn btn-primary"
          @click="claimWish"
        >
          <u-icon name="heart" size="32" color="#fff"></u-icon>
          认领心愿
        </view>
        
        <view 
          v-if="wish.claimer === currentUserId"
          class="btn btn-secondary"
          @click="updateProgress"
        >
          更新进度
        </view>
        
        <view class="btn btn-danger" @click="deleteWish">
          删除
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { wishApi } from '../../api/index.js'

const wishId = ref(null)
const currentUserId = ref(uni.getStorageSync('userInfo')?.id || 1)
const loading = ref(false)

const wish = ref({
  id: 1,
  content: '想要一台咖啡机',
  type: 'item',
  authorId: 1,
  authorName: '妈妈',
  authorAvatar: '/static/avatar/mom.png',
  progress: 30,
  budget: 2999,
  targetAmount: 2999,
  currentAmount: 1200,
  description: '想要一台全自动咖啡机，早上可以自己做咖啡喝',
  claimer: null,
  claimerName: '',
  claimerAvatar: '',
  claimTime: ''
})

// 预算和里程碑数据
const milestones = ref([])
const budgetModalVisible = ref(false)
const milestoneModalVisible = ref(false)
const budgetForm = ref({
  targetAmount: '',
  currentAmount: ''
})
const milestoneForm = ref({
  title: '',
  targetDate: '',
  description: ''
})

onMounted(() => {
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  wishId.value = currentPage.options.id
  if (wishId.value) {
    loadWishDetail(wishId.value)
    loadMilestones(wishId.value)
  }
})

// 加载心愿详情
const loadWishDetail = async (id) => {
  // 目前后端没有单独的详情接口，从列表获取
  const familyId = uni.getStorageSync('currentFamilyId') || 1
  try {
    const res = await wishApi.getList(familyId)
    if (res) {
      const found = res.find(w => w.id == id)
      if (found) {
        wish.value = { ...wish.value, ...found }
      }
    }
  } catch (e) {
    console.error('加载心愿详情失败', e)
  }
}

const getTypeLabel = computed(() => {
  const map = {
    item: '物品',
    experience: '体验',
    learning: '学习',
    relationship: '关系'
  }
  return map[wish.value.type] || '其他'
})

const goBack = () => {
  uni.navigateBack()
}

const showActions = () => {
  uni.showActionSheet({
    itemList: ['编辑', '分享'],
    success: (res) => {
      if (res.tapIndex === 0) {
        uni.navigateTo({ url: `/pages/wish/edit?id=${wishId.value}` })
      }
    }
  })
}

const claimWish = async () => {
  try {
    await wishApi.claim(wishId.value, currentUserId.value)
    wish.value.claimer = currentUserId.value
    wish.value.claimerName = '我'
    wish.value.claimTime = new Date().toLocaleDateString('zh-CN')
    uni.showToast({ title: '认领成功', icon: 'success' })
  } catch (e) {
    console.error('认领失败', e)
    uni.showToast({ title: '认领失败', icon: 'none' })
  }
}

const updateProgress = () => {
  uni.showActionSheet({
    itemList: ['10%', '30%', '50%', '70%', '90%', '100% 已完成'],
    success: async (res) => {
      const progresses = [10, 30, 50, 70, 90, 100]
      const newProgress = progresses[res.tapIndex]
      try {
        if (newProgress === 100) {
          await wishApi.complete(wishId.value)
          uni.showToast({ title: '心愿已完成！', icon: 'success' })
        } else {
          await wishApi.updateProgress(wishId.value, newProgress)
          uni.showToast({ title: '进度已更新', icon: 'success' })
        }
        wish.value.progress = newProgress
      } catch (e) {
        console.error('更新进度失败', e)
        uni.showToast({ title: '更新失败', icon: 'none' })
      }
    }
  })
}

const deleteWish = async () => {
  uni.showModal({
    title: '确认删除',
    content: '删除后无法恢复，是否继续？',
    confirmColor: '#FF4D4F',
    success: async (res) => {
      if (res.confirm) {
        try {
          await wishApi.delete(wishId.value)
          uni.showToast({ title: '已删除', icon: 'success' })
          setTimeout(() => uni.navigateBack(), 1500)
        } catch (e) {
          console.error('删除失败', e)
          uni.showToast({ title: '删除失败', icon: 'none' })
        }
      }
    }
  })
}
</script>

<style lang="scss" scoped>
.wish-detail-page {
  min-height: 100vh;
  background: #f5f6fa;
}

.nav-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 30rpx;
  padding-top: 60rpx;
  background: #fff;
  
  .back-btn, .more-btn {
    width: 60rpx;
    height: 60rpx;
    display: flex;
    align-items: center;
    justify-content: center;
  }
  
  .title {
    font-size: 34rpx;
    font-weight: 600;
    color: #333;
  }
}

.detail-container {
  padding: 30rpx;
}

.wish-header {
  background: linear-gradient(135deg, #F6BD16 0%, #E8684A 100%);
  border-radius: 20rpx;
  padding: 40rpx;
  margin-bottom: 30rpx;
  
  .wish-type {
    display: inline-block;
    padding: 8rpx 20rpx;
    background: rgba(255,255,255,0.2);
    border-radius: 20rpx;
    font-size: 24rpx;
    color: #fff;
    margin-bottom: 20rpx;
  }
  
  .content {
    font-size: 40rpx;
    font-weight: bold;
    color: #fff;
    display: block;
    margin-bottom: 30rpx;
    line-height: 1.5;
  }
  
  .author {
    display: flex;
    align-items: center;
    gap: 16rpx;
    
    .avatar {
      width: 60rpx;
      height: 60rpx;
      border-radius: 50%;
    }
    
    text {
      font-size: 26rpx;
      color: rgba(255,255,255,0.9);
    }
  }
}

.section-card {
  background: #fff;
  border-radius: 20rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
  
  .section-title {
    font-size: 28rpx;
    font-weight: 600;
    color: #333;
    margin-bottom: 20rpx;
  }
}

.progress-section {
  display: flex;
  align-items: center;
  gap: 20rpx;
  
  .progress-bar {
    flex: 1;
    height: 16rpx;
    background: #f0f0f0;
    border-radius: 8rpx;
    overflow: hidden;
    
    .progress-fill {
      height: 100%;
      background: linear-gradient(90deg, #F6BD16, #E8684A);
      border-radius: 8rpx;
      transition: width 0.3s;
    }
  }
  
  .progress-text {
    font-size: 28rpx;
    font-weight: 600;
    color: #E8684A;
  }
}

.budget-info {
  margin-top: 20rpx;
  font-size: 28rpx;
  color: #666;
}

.claimer-info {
  display: flex;
  align-items: center;
  gap: 20rpx;
  
  .avatar {
    width: 80rpx;
    height: 80rpx;
    border-radius: 50%;
  }
  
  .info {
    .name {
      font-size: 30rpx;
      color: #333;
      display: block;
      margin-bottom: 8rpx;
    }
    
    .time {
      font-size: 24rpx;
      color: #999;
    }
  }
}

.description {
  font-size: 28rpx;
  color: #666;
  line-height: 1.6;
}

.action-buttons {
  display: flex;
  gap: 20rpx;
  margin-top: 40rpx;
  
  .btn {
    flex: 1;
    height: 90rpx;
    border-radius: 45rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 30rpx;
    gap: 12rpx;
    
    &-primary {
      background: #F6BD16;
      color: #fff;
    }
    
    &-secondary {
      background: #5B8FF9;
      color: #fff;
    }
    
    &-danger {
      background: #FFF1F0;
      color: #FF4D4F;
      border: 2rpx solid #FFCCC7;
    }
  }
}
</style>