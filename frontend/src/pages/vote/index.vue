<template>
  <view class="page-container">
    <view class="header">
      <view class="header-title">家庭投票 🗳️</view>
      <view class="header-action" @click="createVote">
        <text class="icon">+</text>
      </view>
    </view>
    
    <view class="vote-tabs">
      <view 
        v-for="(tab, index) in tabs" 
        :key="index"
        class="vote-tab"
        :class="{ active: currentTab === index }"
        @click="currentTab = index"
      >
        <text>{{ tab }}</text>
      </view>
    </view>
    
    <scroll-view class="vote-list" scroll-y>
      <view 
        v-for="(vote, index) in votes" 
        :key="index"
        class="vote-card"
        @click="viewVote(vote)"
      >
        <view class="vote-status" :class="{ ended: vote.ended }">
          <text>{{ vote.ended ? '已结束' : '进行中' }}</text>
        </view>
        
        <view class="vote-title">{{ vote.title }}</view>
        
        <view class="vote-options-preview">
          <view v-for="(opt, i) in vote.options.slice(0, 2)" :key="i" class="option-preview">
            <view class="option-bar" :style="{ width: opt.percent + '%' }"></view>
            <view class="option-info">
              <text>{{ opt.name }}</text>
              <text>{{ opt.percent }}%</text>
            </view>
          </view>
        </view>
        
        <view class="vote-footer">
          <text class="vote-creator">发起人：{{ vote.creator }}</text>
          <text class="vote-count">{{ vote.totalVotes }}人已投</text>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref } from 'vue'

const tabs = ['进行中', '已结束', '我发起的']
const currentTab = ref(0)

const votes = ref([
  {
    id: 1,
    title: '周末去哪里玩？',
    ended: false,
    creator: '妈妈',
    totalVotes: 3,
    options: [
      { name: '动物园', percent: 60, votes: 2 },
      { name: '科技馆', percent: 40, votes: 1 },
      { name: '游乐园', percent: 0, votes: 0 }
    ]
  },
  {
    id: 2,
    title: '今晚吃什么？',
    ended: false,
    creator: '爸爸',
    totalVotes: 2,
    options: [
      { name: '火锅', percent: 50, votes: 1 },
      { name: '日料', percent: 50, votes: 1 }
    ]
  },
  {
    id: 3,
    title: '暑假去哪里旅游？',
    ended: true,
    creator: '妈妈',
    totalVotes: 4,
    options: [
      { name: '三亚', percent: 75, votes: 3 },
      { name: '云南', percent: 25, votes: 1 }
    ]
  }
])

const viewVote = (vote) => {
  if (vote.ended) {
    uni.showModal({
      title: vote.title,
      content: '投票已结束\n\n' + vote.options.map(o => `${o.name}: ${o.votes}票 (${o.percent}%)`).join('\n'),
      showCancel: false,
      confirmText: '知道了'
    })
  } else {
    const options = vote.options.map(o => o.name)
    uni.showActionSheet({
      itemList: options,
      title: vote.title,
      success: (res) => {
        uni.showToast({ title: `已投：${options[res.tapIndex]}`, icon: 'success' })
      }
    })
  }
}

const createVote = () => {
  uni.showModal({
    title: '创建投票',
    editable: true,
    placeholderText: '输入投票主题...',
    success: (res) => {
      if (res.confirm && res.content) {
        uni.showToast({ title: '创建成功', icon: 'success' })
      }
    }
  })
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
  background: linear-gradient(135deg, #FFEBEE, #FFCDD2);
  
  .header-title {
    font-size: 24px;
    font-weight: 700;
    color: #2C3E50;
  }
  
  .header-action {
    width: 44px;
    height: 44px;
    background: #F44336;
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

.vote-tabs {
  display: flex;
  padding: 15px;
  background: #fff;
  gap: 10px;
  
  .vote-tab {
    flex: 1;
    text-align: center;
    padding: 10px;
    background: #F5F7FA;
    border-radius: 20px;
    font-size: 14px;
    color: #7F8C8D;
    
    &.active {
      background: #F44336;
      color: #fff;
    }
  }
}

.vote-list {
  padding: 15px;
  height: calc(100vh - 200px);
}

.vote-card {
  background: #fff;
  border-radius: 16px;
  padding: 16px;
  margin-bottom: 12px;
  position: relative;
  
  .vote-status {
    position: absolute;
    top: 16px;
    right: 16px;
    padding: 4px 10px;
    background: #E8F5E9;
    color: #4CAF50;
    border-radius: 10px;
    font-size: 11px;
    
    &.ended {
      background: #F5F5F5;
      color: #9E9E9E;
    }
  }
  
  .vote-title {
    font-size: 16px;
    font-weight: 600;
    color: #2C3E50;
    margin-bottom: 12px;
    padding-right: 60px;
  }
}

.vote-options-preview {
  margin-bottom: 12px;
  
  .option-preview {
    margin-bottom: 8px;
    position: relative;
    height: 32px;
    background: #F5F7FA;
    border-radius: 8px;
    overflow: hidden;
    
    .option-bar {
      position: absolute;
      left: 0;
      top: 0;
      bottom: 0;
      background: linear-gradient(90deg, #FFCDD2, #EF9A9A);
      border-radius: 8px;
    }
    
    .option-info {
      position: relative;
      display: flex;
      justify-content: space-between;
      align-items: center;
      height: 100%;
      padding: 0 12px;
      font-size: 13px;
      color: #2C3E50;
    }
  }
}

.vote-footer {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #7F8C8D;
}
</style>
