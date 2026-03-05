<template>
  <view class="vote-page">
    <!-- 顶部背景 -->
    <view class="header-bg"></view>
    
    <!-- 顶部标题 -->
    <view class="header-bar">
      <text class="title">家庭投票</text>
      <text class="subtitle">一起做决定</text>
    </view>
    
    <!-- 创建投票按钮 -->
    <view class="create-section">
      <view class="create-btn" @click="showCreateModal = true">
        <text class="plus">+</text>
        <text>发起投票</text>
      </view>
    </view>
    
    <!-- 投票列表 -->
    <view class="vote-list">
      <view v-if="loading" class="loading-state">
        <text>加载中...</text>
      </view>
      
      <view v-else-if="voteList.length === 0" class="empty-state">
        <text class="empty-icon">🗳️</text>
        <text class="empty-text">还没有投票</text>
        <text class="empty-sub">发起第一个投票吧</text>
      </view>
      
      <view v-else class="vote-cards">
        <view 
          v-for="vote in voteList" 
          :key="vote.id"
          class="vote-card"
          @click="goDetail(vote)"
        >
          <!-- 投票头部 -->
          <view class="vote-header">
            <view class="vote-title">
              <text>{{ vote.title }}</text>
            </view>
            <view class="vote-status" :class="'status-' + vote.status">
              <text>{{ getStatusText(vote.status) }}</text>
            </view>
          </view>
          
          <!-- 投票选项 -->
          <view class="vote-options">
            <view 
              v-for="(option, idx) in vote.options" 
              :key="idx"
              class="option-item"
              :class="{ 
                selected: vote.myVote === idx,
                winner: vote.status === 2 && idx === vote.winnerIndex
              }"
              @click.stop="voteOption(vote, idx)"
            >
              <view class="option-radio">
                <view v-if="vote.myVote === idx" class="radio-checked"></view>
              </view>
              <text class="option-text">{{ option.text }}</text>
              <view class="option-bar">
                <view class="bar-fill" :style="{ width: option.percent + '%' }"></view>
              </view>
              <text class="option-percent">{{ option.percent }}%</text>
            </view>
          </view>
          
          <!-- 投票信息 -->
          <view class="vote-info">
            <text class="info-item">👤 {{ vote.creatorName }}</text>
            <text class="info-item">📊 {{ vote.totalVotes }}人参与</text>
            <text class="info-item">⏰ {{ vote.endTime }}</text>
          </view>
          
          <!-- 操作按钮 -->
          <view class="vote-actions">
            <view v-if="vote.status === 1 && !vote.myVote" class="action-btn primary" @click.stop="voteOption(vote, 0)">
              <text>参与投票</text>
            </view>
            <view v-if="vote.status === 1" class="action-btn" @click.stop="shareVote(vote)">
              <text>分享</text>
            </view>
          </view>
        </view>
      </view>
    </view>
    
    <!-- 创建投票弹窗 -->
    <view v-if="showCreateModal" class="modal-overlay" @click="showCreateModal = false">
      <view class="modal-content" @click.stop>
        <view class="modal-header">
          <text class="modal-title">发起投票</text>
          <text class="modal-close" @click="showCreateModal = false">✕</text>
        </view>
        
        <view class="modal-body">
          <view class="form-item">
            <text class="form-label">投票标题 *</text>
            <input class="form-input" v-model="newVote.title" placeholder="请输入投票标题" />
          </view>
          
          <view class="form-item">
            <text class="form-label">投票选项 *</text>
            <view class="options-list">
              <view v-for="(opt, idx) in newVote.options" :key="idx" class="option-input">
                <input v-model="newVote.options[idx]" :placeholder="'选项' + (idx + 1)" />
                <text v-if="newVote.options.length > 2" class="remove-btn" @click="removeOption(idx)">✕</text>
              </view>
              <view class="add-option" @click="addOption">
                <text>+ 添加选项</text>
              </view>
            </view>
          </view>
          
          <view class="form-item">
            <text class="form-label">截止时间</text>
            <picker mode="date" :value="newVote.endDate" @change="onEndDateChange">
              <view class="date-picker">
                <text :class="{ placeholder: !newVote.endDate }">
                  {{ newVote.endDate || '选择日期' }}
                </text>
              </view>
            </picker>
          </view>
          
          <view class="form-item">
            <text class="form-label">投票设置</text>
            <view class="settings-row">
              <view 
                class="setting-item" 
                :class="{ active: newVote.isMultiple }"
                @click="newVote.isMultiple = !newVote.isMultiple"
              >
                <text>多选</text>
              </view>
              <view 
                class="setting-item" 
                :class="{ active: newVote.isAnonymous }"
                @click="newVote.isAnonymous = !newVote.isAnonymous"
              >
                <text>匿名</text>
              </view>
            </view>
          </view>
        </view>
        
        <view class="modal-footer">
          <view class="cancel-btn" @click="showCreateModal = false">
            <text>取消</text>
          </view>
          <view class="submit-btn" @click="createVote">
            <text>发起投票</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'

const loading = ref(false)
const showCreateModal = ref(false)

const newVote = ref({
  title: '',
  options: ['', ''],
  endDate: '',
  isMultiple: false,
  isAnonymous: false
})

const voteList = ref([
  {
    id: 1,
    title: '周末去哪儿玩？',
    status: 1,
    creatorName: '老婆',
    totalVotes: 3,
    endTime: '2026-03-15',
    myVote: null,
    winnerIndex: null,
    options: [
      { text: '去公园野餐', votes: 2, percent: 67 },
      { text: '看电影', votes: 1, percent: 33 }
    ]
  },
  {
    id: 2,
    title: '今晚吃什么？',
    status: 2,
    creatorName: '老公',
    totalVotes: 4,
    endTime: '已结束',
    myVote: 0,
    winnerIndex: 1,
    options: [
      { text: '火锅', votes: 2, percent: 50 },
      { text: '烧烤', votes: 2, percent: 50 }
    ]
  }
])

const getStatusText = (status) => {
  const map = { 0: '草稿', 1: '进行中', 2: '已结束' }
  return map[status] || '未知'
}

const addOption = () => {
  if (newVote.value.options.length < 6) {
    newVote.value.options.push('')
  }
}

const removeOption = (idx) => {
  newVote.value.options.splice(idx, 1)
}

const onEndDateChange = (e) => {
  newVote.value.endDate = e.detail.value
}

const createVote = () => {
  if (!newVote.value.title.trim()) {
    uni.showToast({ title: '请输入投票标题', icon: 'none' })
    return
  }
  
  const validOptions = newVote.value.options.filter(o => o.trim())
  if (validOptions.length < 2) {
    uni.showToast({ title: '至少需要2个选项', icon: 'none' })
    return
  }
  
  const vote = {
    id: Date.now(),
    title: newVote.value.title,
    status: 1,
    creatorName: '我',
    totalVotes: 0,
    endTime: newVote.value.endDate || '3天后',
    myVote: null,
    winnerIndex: null,
    options: validOptions.map(text => ({ text, votes: 0, percent: 0 }))
  }
  
  voteList.value.unshift(vote)
  showCreateModal.value = false
  
  newVote.value = {
    title: '',
    options: ['', ''],
    endDate: '',
    isMultiple: false,
    isAnonymous: false
  }
  
  uni.showToast({ title: '投票已发起', icon: 'success' })
}

const voteOption = (vote, idx) => {
  if (vote.status !== 1) return
  if (vote.myVote !== null) {
    uni.showToast({ title: '您已投票', icon: 'none' })
    return
  }
  
  vote.myVote = idx
  vote.options[idx].votes++
  vote.totalVotes++
  
  // 更新百分比
  vote.options.forEach(opt => {
    opt.percent = Math.round(opt.votes / vote.totalVotes * 100)
  })
  
  uni.showToast({ title: '投票成功', icon: 'success' })
}

const goDetail = (vote) => {
  uni.navigateTo({ url: `/pages/vote/detail?id=${vote.id}` })
}

const shareVote = (vote) => {
  uni.showToast({ title: '分享功能开发中', icon: 'none' })
}
</script>

<style lang="scss" scoped>
.vote-page {
  min-height: 100vh;
  background: #f8f9fc;
}

.header-bg {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 400rpx;
  background: linear-gradient(135deg, #11998e, #38ef7d);
}

.header-bar {
  position: relative;
  padding: 120rpx 32rpx 32rpx;
  text-align: center;
  
  .title {
    font-size: 48rpx;
    font-weight: 700;
    color: #fff;
    display: block;
  }
  
  .subtitle {
    font-size: 26rpx;
    color: rgba(255,255,255,0.8);
  }
}

.create-section {
  padding: 0 32rpx;
  margin-bottom: 32rpx;
  
  .create-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 16rpx;
    background: #fff;
    border-radius: 24rpx;
    padding: 28rpx;
    box-shadow: 0 8rpx 24rpx rgba(17, 153, 142, 0.15);
    
    .plus {
      font-size: 40rpx;
      color: #11998e;
      font-weight: bold;
    }
    
    text {
      font-size: 30rpx;
      color: #11998e;
      font-weight: 500;
    }
  }
}

.vote-list {
  padding: 0 32rpx;
}

.empty-state {
  text-align: center;
  padding: 100rpx 0;
  
  .empty-icon {
    font-size: 120rpx;
    display: block;
    margin-bottom: 24rpx;
  }
  
  .empty-text {
    font-size: 32rpx;
    color: #2d3748;
    display: block;
  }
  
  .empty-sub {
    font-size: 26rpx;
    color: #8b9aad;
  }
}

.vote-card {
  background: #fff;
  border-radius: 24rpx;
  padding: 32rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.05);
  
  .vote-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24rpx;
    
    .vote-title text {
      font-size: 32rpx;
      font-weight: 600;
      color: #2d3748;
    }
    
    .vote-status {
      padding: 8rpx 20rpx;
      border-radius: 16rpx;
      font-size: 24rpx;
      
      &.status-0 { background: #f1f5f9; color: #8b9aad; }
      &.status-1 { background: rgba(17, 153, 142, 0.1); color: #11998e; }
      &.status-2 { background: #f1f5f9; color: #8b9aad; }
    }
  }
  
  .vote-options {
    margin-bottom: 24rpx;
    
    .option-item {
      display: flex;
      align-items: center;
      padding: 20rpx;
      background: #f8f9fc;
      border-radius: 16rpx;
      margin-bottom: 16rpx;
      border: 2rpx solid transparent;
      
      &.selected {
        border-color: #11998e;
        background: rgba(17, 153, 142, 0.05);
      }
      
      &.winner {
        background: rgba(17, 153, 142, 0.1);
        
        .option-text {
          color: #11998e;
          font-weight: 600;
        }
      }
      
      .option-radio {
        width: 40rpx;
        height: 40rpx;
        border: 3rpx solid #ddd;
        border-radius: 50%;
        margin-right: 16rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        
        .radio-checked {
          width: 24rpx;
          height: 24rpx;
          background: #11998e;
          border-radius: 50%;
        }
      }
      
      .option-text {
        flex: 1;
        font-size: 28rpx;
        color: #2d3748;
      }
      
      .option-bar {
        width: 120rpx;
        height: 12rpx;
        background: #e2e8f0;
        border-radius: 6rpx;
        margin: 0 16rpx;
        overflow: hidden;
        
        .bar-fill {
          height: 100%;
          background: linear-gradient(90deg, #11998e, #38ef7d);
          border-radius: 6rpx;
        }
      }
      
      .option-percent {
        font-size: 24rpx;
        color: #8b9aad;
        width: 60rpx;
        text-align: right;
      }
    }
  }
  
  .vote-info {
    display: flex;
    gap: 24rpx;
    margin-bottom: 24rpx;
    
    .info-item {
      font-size: 24rpx;
      color: #8b9aad;
    }
  }
  
  .vote-actions {
    display: flex;
    gap: 16rpx;
    
    .action-btn {
      flex: 1;
      text-align: center;
      padding: 20rpx;
      border-radius: 20rpx;
      font-size: 28rpx;
      background: #f8f9fc;
      color: #5a6c7d;
      
      &.primary {
        background: linear-gradient(135deg, #11998e, #38ef7d);
        color: #fff;
      }
    }
  }
}

// 弹窗样式
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: flex-end;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: #fff;
  border-radius: 40rpx 40rpx 0 0;
  width: 100%;
  max-height: 85vh;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 40rpx;
  
  .modal-title {
    font-size: 36rpx;
    font-weight: 600;
  }
  
  .modal-close {
    font-size: 40rpx;
    color: #8b9aad;
  }
}

.modal-body {
  padding: 0 40rpx 40rpx;
  max-height: 60vh;
}

.form-item {
  margin-bottom: 32rpx;
  
  .form-label {
    font-size: 26rpx;
    color: #5a6c7d;
    display: block;
    margin-bottom: 12rpx;
  }
  
  .form-input {
    width: 100%;
    padding: 24rpx;
    background: #f8f9fc;
    border-radius: 20rpx;
    font-size: 30rpx;
  }
}

.options-list {
  .option-input {
    display: flex;
    align-items: center;
    margin-bottom: 16rpx;
    
    input {
      flex: 1;
      padding: 20rpx 24rpx;
      background: #f8f9fc;
      border-radius: 16rpx;
      font-size: 28rpx;
    }
    
    .remove-btn {
      margin-left: 16rpx;
      color: #fc8181;
      font-size: 32rpx;
    }
  }
  
  .add-option {
    text-align: center;
    padding: 20rpx;
    color: #11998e;
    font-size: 28rpx;
  }
}

.date-picker {
  padding: 24rpx;
  background: #f8f9fc;
  border-radius: 20rpx;
  
  text {
    font-size: 30rpx;
    
    &.placeholder { color: #a0aec0; }
  }
}

.settings-row {
  display: flex;
  gap: 16rpx;
  
  .setting-item {
    padding: 16rpx 32rpx;
    background: #f8f9fc;
    border-radius: 16rpx;
    font-size: 26rpx;
    color: #5a6c7d;
    
    &.active {
      background: rgba(17, 153, 142, 0.1);
      color: #11998e;
    }
  }
}

.modal-footer {
  display: flex;
  gap: 24rpx;
  padding: 20rpx 40rpx 60rpx;
  
  .cancel-btn, .submit-btn {
    flex: 1;
    text-align: center;
    padding: 28rpx;
    border-radius: 32rpx;
    font-size: 30rpx;
  }
  
  .cancel-btn {
    background: #f1f5f9;
    color: #5a6c7d;
  }
  
  .submit-btn {
    background: linear-gradient(135deg, #11998e, #38ef7d);
    color: #fff;
  }
}
</style>
