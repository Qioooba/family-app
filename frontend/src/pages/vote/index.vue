<template>
  <view class="vote-page">
    <!-- 进行中的投票 -->
    <view class="vote-section"
      <view class="section-header"
        <text class="section-title">🗳️ 进行中</text>
        <text class="create-btn" @click="createVote">+ 发起投票</text>
      </view>
      
      <view class="vote-list"
        <view 
          v-for="vote in activeVotes" 
          :key="vote.id"
          class="vote-card"
          @click="goVote(vote)"
        >
          <view class="vote-header"
            <text class="vote-title">{{ vote.title }}</text>
            
            <view class="vote-status" :class="{ voted: vote.hasVoted }"
              <text>{{ vote.hasVoted ? '已参与' : '进行中' }}</text>
            </view>
          </view>
          
          <text class="vote-desc" v-if="vote.description">{{ vote.description }}</text>
          
          <view class="vote-options-preview"
            <text v-for="(opt, idx) in vote.options.slice(0, 3)" :key="idx"
              <text class="opt-tag">{{ opt }}</text>
            </text>
            
            <text v-if="vote.options.length > 3" class="more-opt">+{{ vote.options.length - 3 }}</text>
          </view>
          
          <view class="vote-footer"
            <view class="vote-stats"
              <u-icon name="account" size="24" color="#999"></u-icon>
              <text>{{ vote.participants }}人参与</text>
            </view>
            
            <view class="vote-time"
              <u-icon name="clock" size="24" color="#999"></u-icon>
              <text>{{ vote.endTime }}截止</text>
            </view>
          </view>
          
          <!-- 进度条（已投票显示） -->
          <view v-if="vote.hasVoted && vote.results" class="vote-results"
            <view 
              v-for="(result, idx) in vote.results" 
              :key="idx"
              class="result-bar"
            >
              <view class="result-info"
                <text class="opt-name">{{ result.option }}</text>
                
                <text class="opt-count">{{ result.count }}票</text>
              </view>
              
              <view class="progress-line"
                <view class="line-fill" :style="{ width: result.percent + '%' }"></view>
              </view>
            </view>
          </view>
        </view>
      </view>
    </view>
    
    <!-- 已结束的投票 -->
    <view class="vote-section" v-if="endedVotes.length > 0"
      <view class="section-header"
        <text class="section-title">📊 已结束</text>
      </view>
      
      <view class="vote-list"
        <view 
          v-for="vote in endedVotes" 
          :key="vote.id"
          class="vote-card ended"
          @click="goResult(vote)"
        >
          <view class="vote-header"
            <text class="vote-title">{{ vote.title }}</text>
            
            <view class="ended-tag">已结束</view>
          </view>
          
          <view class="vote-result-preview"
            <text class="winner">🏆 {{ vote.winner }}</text>
            
            <text class="winner-count">{{ vote.winnerVotes }}票</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'

const activeVotes = ref([
  {
    id: 1,
    title: '今晚吃什么？',
    description: '大家投票决定今晚的菜谱',
    options: ['火锅', '烧烤', '家常菜', '外卖'],
    participants: 4,
    endTime: '今天 18:00',
    hasVoted: false,
    type: 'single'
  },
  {
    id: 2,
    title: '周末去哪里玩？',
    options: ['动物园', '博物馆', '公园野餐', '在家休息', '商场'],
    participants: 5,
    endTime: '明天 12:00',
    hasVoted: true,
    type: 'single',
    results: [
      { option: '动物园', count: 3, percent: 60 },
      { option: '博物馆', count: 1, percent: 20 },
      { option: '公园野餐', count: 1, percent: 20 }
    ]
  }
])

const endedVotes = ref([
  {
    id: 3,
    title: '买哪个品牌的洗衣机？',
    winner: '海尔',
    winnerVotes: 4
  },
  {
    id: 4,
    title: '春节期间去哪里拜年顺序',
    winner: '初一爷爷奶奶家',
    winnerVotes: 5
  }
])

const createVote = () => {
  uni.navigateTo({ url: '/pages/vote/create' })
}

const goVote = (vote) => {
  if (vote.hasVoted) {
    uni.navigateTo({ url: `/pages/vote/result?id=${vote.id}` })
  } else {
    uni.navigateTo({ url: `/pages/vote/detail?id=${vote.id}` })
  }
}

const goResult = (vote) => {
  uni.navigateTo({ url: `/pages/vote/result?id=${vote.id}` })
}
</script>

<style lang="scss" scoped>
.vote-page {
  min-height: 100vh;
  background: #f5f6fa;
  padding: 30rpx;
  padding-bottom: 120rpx;
}

.vote-section {
  margin-bottom: 40rpx;
  
  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24rpx;
    
    .section-title {
      font-size: 32rpx;
      font-weight: 600;
      color: #333;
    }
    
    .create-btn {
      font-size: 28rpx;
      color: #5B8FF9;
      font-weight: 500;
    }
  }
  
  .vote-list {
    .vote-card {
      background: #fff;
      border-radius: 20rpx;
      padding: 30rpx;
      margin-bottom: 20rpx;
      
      &.ended {
        opacity: 0.8;
      }
      
      .vote-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 16rpx;
        
        .vote-title {
          font-size: 32rpx;
          color: #333;
          font-weight: 500;
          flex: 1;
        }
        
        .vote-status {
          padding: 8rpx 20rpx;
          background: #F0F5FF;
          color: #5B8FF9;
          font-size: 24rpx;
          border-radius: 20rpx;
          
          &.voted {
            background: #F6FFED;
            color: #52C41A;
          }
        }
        
        .ended-tag {
          padding: 8rpx 20rpx;
          background: #f5f5f5;
          color: #999;
          font-size: 24rpx;
          border-radius: 20rpx;
        }
      }
      
      .vote-desc {
        font-size: 26rpx;
        color: #666;
        margin-bottom: 20rpx;
        line-height: 1.5;
      }
      
      .vote-options-preview {
        display: flex;
        flex-wrap: wrap;
        gap: 12rpx;
        margin-bottom: 20rpx;
        
        .opt-tag {
          padding: 12rpx 24rpx;
          background: #F0F5FF;
          color: #5B8FF9;
          font-size: 26rpx;
          border-radius: 12rpx;
        }
        
        .more-opt {
          padding: 12rpx 24rpx;
          background: #f5f5f5;
          color: #999;
          font-size: 26rpx;
          border-radius: 12rpx;
        }
      }
      
      .vote-footer {
        display: flex;
        justify-content: space-between;
        padding-top: 20rpx;
        border-top: 2rpx solid #f5f5f5;
        
        .vote-stats, .vote-time {
          display: flex;
          align-items: center;
          
          text {
            margin-left: 8rpx;
            font-size: 24rpx;
            color: #999;
          }
        }
      }
      
      .vote-results {
        margin-top: 24rpx;
        padding-top: 24rpx;
        border-top: 2rpx dashed #eee;
        
        .result-bar {
          margin-bottom: 16rpx;
          
          .result-info {
            display: flex;
            justify-content: space-between;
            margin-bottom: 8rpx;
            
            .opt-name {
              font-size: 26rpx;
              color: #333;
            }
            
            .opt-count {
              font-size: 24rpx;
              color: #999;
            }
          }
          
          .progress-line {
            height: 12rpx;
            background: #f0f0f0;
            border-radius: 6rpx;
            overflow: hidden;
            
            .line-fill {
              height: 100%;
              background: linear-gradient(90deg, #5B8FF9, #5AD8A6);
              border-radius: 6rpx;
            }
          }
        }
      }
      
      .vote-result-preview {
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 20rpx;
        background: #FFFBE6;
        border-radius: 12rpx;
        margin-top: 16rpx;
        
        .winner {
          font-size: 30rpx;
          color: #333;
          font-weight: 500;
        }
        
        .winner-count {
          font-size: 28rpx;
          color: #FAAD14;
          font-weight: 500;
        }
      }
    }
  }
}
</style>
