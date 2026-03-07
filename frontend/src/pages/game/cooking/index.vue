<template>
  <view class="game-page">
    <!-- 头部 -->
    <view class="header">
      <view class="header-left" @click="goBack">
        <u-icon name="arrow-left" color="#fff" size="40"></u-icon>
      </view>
      <view class="header-title">厨艺比拼</view>
      <view class="header-right" @click="showRules">
        <text>规则</text>
      </view>
    </view>

    <!-- 当前比赛状态 -->
    <view class="contest-status" v-if="currentContest">
      <view class="status-badge">
        <text class="badge-text">{{ currentContest.status === 'ongoing' ? '进行中' : '已结束' }}</text>
      </view>
      <view class="contest-info">
        <text class="contest-title">{{ currentContest.title }}</text>
        <text class="contest-theme">主题：{{ currentContest.theme }}</text>
        <view class="countdown" v-if="currentContest.status === 'ongoing'">
          <text>距离结束：{{ countdownText }}</text>
        </view>
      </view>
    </view>

    <!-- 功能选项卡 -->
    <view class="tabs">
      <view 
        v-for="tab in tabs" 
        :key="tab.key"
        class="tab-item"
        :class="{ active: activeTab === tab.key }"
        @click="activeTab = tab.key"
      >
        <text>{{ tab.name }}</text>
      </view>
    </view>

    <!-- 作品展示区 -->
    <view class="content-area" v-if="activeTab === 'works'">
      <view class="upload-btn" @click="showUpload = true" v-if="currentContest?.status === 'ongoing'">
        <u-icon name="plus" color="#fff" size="48"></u-icon>
        <text>上传作品</text>
      </view>

      <view class="works-list">
        <view 
          v-for="work in works" 
          :key="work.id"
          class="work-card"
          @click="viewWorkDetail(work)"
        >
          <image :src="work.image" class="work-image" mode="aspectFill"></image>
          <view class="work-info">
            <view class="work-header">
              <image :src="work.authorAvatar" class="author-avatar" mode="aspectFill"></image>
              <text class="author-name">{{ work.authorName }}</text>
            </view>
            <text class="work-name">{{ work.name }}</text>
            <text class="work-desc">{{ work.description }}</text>
            
            <view class="work-footer">
              <view class="work-score">
                <text class="score-value">{{ work.avgScore }}</text>
                <text class="score-label">分</text>
              </view>
              <view class="work-votes">
                <u-icon name="heart" color="#ff6b6b" size="28"></u-icon>
                <text>{{ work.votes }}</text>
              </view>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 排行榜 -->
    <view class="content-area" v-if="activeTab === 'rank'">
      <view class="rank-list">
        <view 
          v-for="(work, index) in rankedWorks" 
          :key="work.id"
          class="rank-item"
          :class="{ 'rank-top3': index < 3 }"
        >
          <view class="rank-number">{{ index + 1 }}</view>
          <image :src="work.image" class="rank-image" mode="aspectFill"></image>
          <view class="rank-info">
            <text class="rank-name">{{ work.name }}</text>
            <text class="rank-author">{{ work.authorName }}</text>
            <view class="rank-dimensions">
              <text>色{{ work.scores.color }}·香{{ work.scores.aroma }}·味{{ work.scores.taste }}·创意{{ work.scores.creativity }}</text>
            </view>
          </view>
          <view class="rank-score">
            <text class="score-value">{{ work.avgScore }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 菜单规划 -->
    <view class="content-area" v-if="activeTab === 'menu'">
      <view class="menu-list">
        <view 
          v-for="menu in menus" 
          :key="menu.id"
          class="menu-card"
        >
          <view class="menu-header">
            <text class="menu-name">{{ menu.name }}</text>
            <text class="menu-date">{{ menu.date }}</text>
          </view>
          
          <view class="menu-items">
            <view 
              v-for="item in menu.items" 
              :key="item"
              class="menu-item"
            >
              <text>{{ item }}</text>
            </view>
          </view>
          
          <view class="menu-actions">
            <u-button type="primary" size="small" text="编辑"></u-button>
            <u-button type="error" size="small" text="删除"></u-button>
          </view>
        </view>
      </view>
      
      <view class="add-menu-btn" @click="showMenuForm = true">
        <u-icon name="plus" color="#667eea" size="40"></u-icon>
        <text>添加菜单</text>
      </view>
    </view>

    <!-- 上传作品弹窗 -->
    <u-popup v-model:show="showUpload" mode="bottom" round="20">
      <view class="upload-popup">
        <view class="popup-title">上传作品</view>
        
        <view class="upload-area" @click="chooseImage">
          <image v-if="uploadForm.image" :src="uploadForm.image" mode="aspectFill"></image>
          <view v-else class="upload-placeholder">
            <u-icon name="camera" color="#999" size="64"></u-icon>
            <text>点击上传菜品照片</text>
          </view>
        </view>
        
        <u-form :model="uploadForm">
          <u-form-item label="菜品名称">
            <u-input v-model="uploadForm.name" placeholder="请输入菜品名称"></u-input>
          </u-form-item>
          
          <u-form-item label="菜品描述">
            <u-textarea v-model="uploadForm.description" placeholder="请描述你的菜品..."></u-textarea>
          </u-form-item>
        </u-form>
        
        <view class="popup-actions">
          <u-button type="info" text="取消" @click="showUpload = false"></u-button>
          <u-button type="primary" text="提交" @click="submitWork"></u-button>
        </view>
      </view>
    </u-popup>

    <!-- 作品详情弹窗 -->
    <u-popup v-model:show="showDetail" mode="center" round="20">
      <view class="detail-popup" v-if="selectedWork">
        <image :src="selectedWork.image" class="detail-image" mode="aspectFill"></image>
        
        <view class="detail-info">
          <text class="detail-name">{{ selectedWork.name }}</text>
          
          <view class="detail-author">
            <image :src="selectedWork.authorAvatar" class="author-avatar" mode="aspectFill"></image>
            <text>{{ selectedWork.authorName }}</text>
          </view>
          
          <text class="detail-desc">{{ selectedWork.description }}</text>
          
          <!-- 评分维度 -->
          <view class="dimensions" v-if="currentContest?.status === 'ended'">
            <view class="dimension-item">
              <text class="dim-label">色泽</text>
              <u-rate v-model="selectedWork.scores.color" readonly></u-rate>
              <text class="dim-score">{{ selectedWork.scores.color }}</text>
            </view>
            
            <view class="dimension-item">
              <text class="dim-label">香气</text>
              <u-rate v-model="selectedWork.scores.aroma" readonly></u-rate>
              <text class="dim-score">{{ selectedWork.scores.aroma }}</text>
            </view>
            
            <view class="dimension-item">
              <text class="dim-label">味道</text>
              <u-rate v-model="selectedWork.scores.taste" readonly></u-rate>
              <text class="dim-score">{{ selectedWork.scores.taste }}</text>
            </view>
            
            <view class="dimension-item">
              <text class="dim-label">创意</text>
              <u-rate v-model="selectedWork.scores.creativity" readonly></u-rate>
              <text class="dim-score">{{ selectedWork.scores.creativity }}</text>
            </view>
          </view>
          
          <!-- 投票按钮 -->
          <view class="vote-section" v-if="currentContest?.status === 'ongoing'">
            <u-button 
              type="error" 
              :text="hasVoted(selectedWork.id) ? '已投票' : '投票'"
              :disabled="hasVoted(selectedWork.id)"
              @click="vote(selectedWork.id)"
            ></u-button>
          </view>
        </view>
        
        <view class="close-btn" @click="showDetail = false">
          <u-icon name="close" color="#999" size="40"></u-icon>
        </view>
      </view>
    </u-popup>

    <!-- 规则弹窗 -->
    <u-popup v-model:show="showRulePopup" mode="center" round="20">
      <view class="rules-popup">
        <view class="rules-title">游戏规则</view>
        <view class="rules-content">
          <text class="rule-item">1. 每个家庭成员可以上传自己的菜品作品</text>
          <text class="rule-item">2. 评分维度：色(25%)、香(25%)、味(30%)、创意(20%)</text>
          <text class="rule-item">3. 家庭成员可以相互投票，每人限投3票</text>
          <text class="rule-item">4. 比赛结束后根据总分排名</text>
          <text class="rule-item">5. 可以使用菜单规划功能安排每日食谱</text>
        </view>
        <u-button type="primary" text="知道了" @click="showRulePopup = false"></u-button>
      </view>
    </u-popup>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'

const tabs = [
  { key: 'works', name: '作品' },
  { key: 'rank', name: '排行' },
  { key: 'menu', name: '菜单' }
]

const activeTab = ref('works')
const showUpload = ref(false)
const showDetail = ref(false)
const showRulePopup = ref(false)
const selectedWork = ref(null)
const votedWorks = ref([])

// 当前比赛
const currentContest = ref({
  id: 1,
  title: '周末厨艺大比拼',
  theme: '家常菜',
  status: 'ongoing',
  endTime: new Date().getTime() + 86400000 * 2
})

// 倒计时
const countdownText = ref('')

// 更新倒计时
const updateCountdown = () => {
  const now = new Date().getTime()
  const end = currentContest.value.endTime
  const diff = end - now
  
  if (diff <= 0) {
    countdownText.value = '已结束'
    return
  }
  
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60))
  
  countdownText.value = `${days}天${hours}小时`
}

// 作品列表
const works = ref([
  {
    id: 1,
    name: '红烧肉',
    description: '肥而不腻，入口即化',
    image: '/static/recipes/placeholder.png',
    authorName: '妈妈',
    authorAvatar: '/static/avatar-default.svg',
    avgScore: 9.2,
    votes: 12,
    scores: { color: 9, aroma: 9, taste: 10, creativity: 8 }
  },
  {
    id: 2,
    name: '糖醋里脊',
    description: '酸甜可口，老少皆宜',
    image: '/static/recipes/placeholder.png',
    authorName: '爸爸',
    authorAvatar: '/static/avatar-default.svg',
    avgScore: 8.8,
    votes: 10,
    scores: { color: 9, aroma: 8, taste: 9, creativity: 8 }
  }
])

// 排序后的作品
const rankedWorks = computed(() => {
  return [...works.value].sort((a, b) => b.avgScore - a.avgScore)
})

// 菜单列表
const menus = ref([
  {
    id: 1,
    name: '周末家庭聚餐',
    date: '2024-03-10',
    items: ['红烧肉', '糖醋里脊', '清炒时蔬', '番茄蛋汤']
  }
])

// 上传表单
const uploadForm = ref({
  image: '',
  name: '',
  description: ''
})

// 选择图片
const chooseImage = () => {
  uni.chooseImage({
    count: 1,
    success: (res) => {
      uploadForm.value.image = res.tempFilePaths[0]
    }
  })
}

// 提交作品
const submitWork = () => {
  if (!uploadForm.value.image || !uploadForm.value.name) {
    uni.showToast({ title: '请完善信息', icon: 'none' })
    return
  }
  
  const newWork = {
    id: works.value.length + 1,
    name: uploadForm.value.name,
    description: uploadForm.value.description,
    image: uploadForm.value.image,
    authorName: '我',
    authorAvatar: '/static/avatar-default.svg',
    avgScore: 0,
    votes: 0,
    scores: { color: 0, aroma: 0, taste: 0, creativity: 0 }
  }
  
  works.value.unshift(newWork)
  showUpload.value = false
  
  // 重置表单
  uploadForm.value = { image: '', name: '', description: '' }
  
  uni.showToast({ title: '上传成功', icon: 'success' })
}

// 查看作品详情
const viewWorkDetail = (work) => {
  selectedWork.value = work
  showDetail.value = true
}

// 检查是否已投票
const hasVoted = (workId) => {
  return votedWorks.value.includes(workId)
}

// 投票
const vote = (workId) => {
  if (votedWorks.value.length >= 3) {
    uni.showToast({ title: '每人限投3票', icon: 'none' })
    return
  }
  
  votedWorks.value.push(workId)
  
  const work = works.value.find(w => w.id === workId)
  if (work) {
    work.votes++
  }
  
  uni.showToast({ title: '投票成功', icon: 'success' })
}

// 返回
const goBack = () => {
  uni.navigateBack()
}

// 显示规则
const showRules = () => {
  showRulePopup.value = true
}

onMounted(() => {
  updateCountdown()
  setInterval(updateCountdown, 60000)
  
  // 加载已投票记录
  votedWorks.value = uni.getStorageSync('votedWorks') || []
})
</script>

<style lang="scss" scoped>
.game-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  padding-bottom: 40rpx;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 100rpx 32rpx 30rpx;
  
  .header-left, .header-right {
    width: 80rpx;
    height: 80rpx;
    display: flex;
    align-items: center;
    justify-content: center;
  }
  
  .header-right {
    text {
      color: #fff;
      font-size: 28rpx;
    }
  }
  
  .header-title {
    font-size: 36rpx;
    font-weight: bold;
    color: #fff;
  }
}

.contest-status {
  margin: 0 32rpx 20rpx;
  padding: 30rpx;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 24rpx;
  position: relative;
  
  .status-badge {
    position: absolute;
    top: 20rpx;
    right: 20rpx;
    padding: 8rpx 20rpx;
    background: #52c41a;
    border-radius: 20rpx;
    
    .badge-text {
      font-size: 24rpx;
      color: #fff;
    }
  }
  
  .contest-info {
    .contest-title {
      font-size: 36rpx;
      font-weight: bold;
      color: #333;
      display: block;
      margin-bottom: 10rpx;
    }
    
    .contest-theme {
      font-size: 28rpx;
      color: #666;
      display: block;
      margin-bottom: 10rpx;
    }
    
    .countdown {
      text {
        font-size: 24rpx;
        color: #f5576c;
      }
    }
  }
}

.tabs {
  display: flex;
  margin: 0 32rpx 20rpx;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 16rpx;
  padding: 8rpx;
  
  .tab-item {
    flex: 1;
    text-align: center;
    padding: 20rpx;
    border-radius: 12rpx;
    
    &.active {
      background: #fff;
      
      text {
        color: #f5576c;
      }
    }
    
    text {
      font-size: 28rpx;
      color: #fff;
    }
  }
}

.content-area {
  margin: 0 32rpx;
}

.upload-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16rpx;
  padding: 40rpx;
  background: #fff;
  border-radius: 24rpx;
  margin-bottom: 20rpx;
  border: 2rpx dashed #f5576c;
  
  text {
    font-size: 32rpx;
    color: #f5576c;
    font-weight: bold;
  }
}

.works-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20rpx;
}

.work-card {
  background: #fff;
  border-radius: 20rpx;
  overflow: hidden;
  
  .work-image {
    width: 100%;
    height: 240rpx;
  }
  
  .work-info {
    padding: 20rpx;
    
    .work-header {
      display: flex;
      align-items: center;
      margin-bottom: 10rpx;
      
      .author-avatar {
        width: 40rpx;
        height: 40rpx;
        border-radius: 50%;
        margin-right: 10rpx;
      }
      
      .author-name {
        font-size: 24rpx;
        color: #666;
      }
    }
    
    .work-name {
      font-size: 32rpx;
      font-weight: bold;
      color: #333;
      display: block;
      margin-bottom: 6rpx;
    }
    
    .work-desc {
      font-size: 24rpx;
      color: #999;
      display: block;
      margin-bottom: 16rpx;
    }
    
    .work-footer {
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      .work-score {
        .score-value {
          font-size: 36rpx;
          font-weight: bold;
          color: #f5576c;
        }
        
        .score-label {
          font-size: 24rpx;
          color: #999;
        }
      }
      
      .work-votes {
        display: flex;
        align-items: center;
        gap: 6rpx;
        
        text {
          font-size: 24rpx;
          color: #666;
        }
      }
    }
  }
}

.rank-list {
  background: #fff;
  border-radius: 24rpx;
  padding: 20rpx;
}

.rank-item {
  display: flex;
  align-items: center;
  padding: 20rpx;
  border-bottom: 1rpx solid #f5f5f5;
  
  &:last-child {
    border-bottom: none;
  }
  
  &.rank-top3 {
    .rank-number {
      background: linear-gradient(135deg, #ffd700, #ffaa00);
      color: #fff;
    }
  }
  
  .rank-number {
    width: 48rpx;
    height: 48rpx;
    border-radius: 50%;
    background: #f5f5f5;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 24rpx;
    font-weight: bold;
    color: #666;
    margin-right: 20rpx;
  }
  
  .rank-image {
    width: 120rpx;
    height: 120rpx;
    border-radius: 16rpx;
    margin-right: 20rpx;
  }
  
  .rank-info {
    flex: 1;
    
    .rank-name {
      font-size: 30rpx;
      font-weight: bold;
      color: #333;
      display: block;
      margin-bottom: 6rpx;
    }
    
    .rank-author {
      font-size: 24rpx;
      color: #666;
      display: block;
      margin-bottom: 6rpx;
    }
    
    .rank-dimensions {
      text {
        font-size: 22rpx;
        color: #999;
      }
    }
  }
  
  .rank-score {
    .score-value {
      font-size: 40rpx;
      font-weight: bold;
      color: #f5576c;
    }
  }
}

.menu-list {
  .menu-card {
    background: #fff;
    border-radius: 24rpx;
    padding: 30rpx;
    margin-bottom: 20rpx;
    
    .menu-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20rpx;
      
      .menu-name {
        font-size: 32rpx;
        font-weight: bold;
        color: #333;
      }
      
      .menu-date {
        font-size: 24rpx;
        color: #999;
      }
    }
    
    .menu-items {
      display: flex;
      flex-wrap: wrap;
      gap: 16rpx;
      margin-bottom: 20rpx;
      
      .menu-item {
        padding: 12rpx 24rpx;
        background: #fff5f5;
        border-radius: 30rpx;
        
        text {
          font-size: 26rpx;
          color: #f5576c;
        }
      }
    }
    
    .menu-actions {
      display: flex;
      justify-content: flex-end;
      gap: 16rpx;
    }
  }
}

.add-menu-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16rpx;
  padding: 40rpx;
  background: #fff;
  border-radius: 24rpx;
  border: 2rpx dashed #f5576c;
  
  text {
    font-size: 32rpx;
    color: #f5576c;
    font-weight: bold;
  }
}

.upload-popup {
  padding: 40rpx;
  
  .popup-title {
    font-size: 36rpx;
    font-weight: bold;
    text-align: center;
    margin-bottom: 30rpx;
    color: #333;
  }
  
  .upload-area {
    width: 100%;
    height: 400rpx;
    background: #f5f5f5;
    border-radius: 20rpx;
    margin-bottom: 30rpx;
    overflow: hidden;
    
    image {
      width: 100%;
      height: 100%;
    }
    
    .upload-placeholder {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      height: 100%;
      
      text {
        font-size: 28rpx;
        color: #999;
        margin-top: 20rpx;
      }
    }
  }
  
  .popup-actions {
    display: flex;
    gap: 20rpx;
    margin-top: 30rpx;
    
    ::v-deep .u-button {
      flex: 1;
    }
  }
}

.detail-popup {
  position: relative;
  width: 680rpx;
  max-height: 900rpx;
  overflow-y: auto;
  
  .detail-image {
    width: 100%;
    height: 400rpx;
    border-radius: 20rpx 20rpx 0 0;
  }
  
  .detail-info {
    padding: 30rpx;
    
    .detail-name {
      font-size: 40rpx;
      font-weight: bold;
      color: #333;
      display: block;
      margin-bottom: 20rpx;
    }
    
    .detail-author {
      display: flex;
      align-items: center;
      margin-bottom: 20rpx;
      
      .author-avatar {
        width: 60rpx;
        height: 60rpx;
        border-radius: 50%;
        margin-right: 16rpx;
      }
      
      text {
        font-size: 28rpx;
        color: #666;
      }
    }
    
    .detail-desc {
      font-size: 28rpx;
      color: #666;
      line-height: 1.6;
      margin-bottom: 30rpx;
      display: block;
    }
    
    .dimensions {
      background: #f5f5f5;
      border-radius: 16rpx;
      padding: 20rpx;
      margin-bottom: 30rpx;
      
      .dimension-item {
        display: flex;
        align-items: center;
        padding: 16rpx 0;
        border-bottom: 1rpx solid #e5e5e5;
        
        &:last-child {
          border-bottom: none;
        }
        
        .dim-label {
          width: 80rpx;
          font-size: 28rpx;
          color: #666;
        }
        
        .dim-score {
          margin-left: 20rpx;
          font-size: 32rpx;
          font-weight: bold;
          color: #f5576c;
        }
      }
    }
  }
  
  .close-btn {
    position: absolute;
    top: 20rpx;
    right: 20rpx;
    width: 60rpx;
    height: 60rpx;
    background: rgba(0, 0, 0, 0.3);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
  }
}

.rules-popup {
  padding: 40rpx;
  width: 600rpx;
  
  .rules-title {
    font-size: 36rpx;
    font-weight: bold;
    text-align: center;
    margin-bottom: 30rpx;
    color: #333;
  }
  
  .rules-content {
    margin-bottom: 30rpx;
    
    .rule-item {
      display: block;
      font-size: 28rpx;
      color: #666;
      line-height: 1.8;
      margin-bottom: 10rpx;
    }
  }
}
</style>
