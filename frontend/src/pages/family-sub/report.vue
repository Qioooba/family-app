<template>
  <view class="family-report-page"
>
    <!-- 顶部导航 -->
    <view class="nav-bar">
      <view class="back-btn" @click="goBack"
>
        <u-icon name="arrow-left" size="40" color="#fff"></u-icon>
      </view>
      <text class="title">家庭报告</text>
      <view class="right-btn" @click="shareReport">
        <u-icon name="share" size="36" color="#fff"></u-icon>
      </view>
    </view>

    <view class="content">
      <!-- 报告周期选择 -->
      <view class="period-section">
        <view class="period-tabs"
>
          <view
            v-for="period in periods"
            :key="period.value"
            class="period-tab"
            :class="{ active: currentPeriod === period.value }"
            @click="currentPeriod = period.value"
          >
            {{ period.label }}
          </view>
        </view>

        <view class="period-info">
          <text class="period-date">{{ reportData.period }}</text>
          <text class="update-time">更新于 {{ reportData.updateTime }}</text>
        </view>
      </view>

      <!-- 家庭得分卡片 -->
      <view class="score-card">
        <view class="score-main"
>
          <view class="score-ring"
>
            <view class="ring-bg"></view>
            <view 
              class="ring-progress"
              :style="{ transform: `rotate(${reportData.score * 3.6}deg)` }"
            ></view>
            <view class="ring-center"
>
              <text class="score-value">{{ reportData.score }}</text>
              <text class="score-label">家庭分</text>
            </view>
          </view>
          
          <view class="score-rank"
>
            <text class="rank-text">超越 {{ reportData.rank }}% 家庭</text>
            <view class="trend-badge" :class="reportData.trend"
>
              <u-icon 
                :name="reportData.trend === 'up' ? 'arrow-upward' : 'arrow-downward'" 
                size="20" 
                color="#fff"
              ></u-icon>
              {{ reportData.trendValue }}
            </view>
          </view>
        </view>

        <view class="score-dimensions"
>
          <view
            v-for="dim in reportData.dimensions"
            :key="dim.name"
            class="dimension-item"
          >
            <view class="dim-header">
              <text class="dim-name">{{ dim.name }}</text>
              <text class="dim-score">{{ dim.score }}分</text>
            </view>            
            
            <view class="dim-bar"
>
              <view class="dim-track"></view>
              <view 
                class="dim-fill"
                :style="{ width: dim.score + '%', background: dim.color }"
              ></view>
            </view>
          </view>
        </view>
      </view>

      <!-- 成员贡献排行 -->
      <view class="members-section"
>
        <view class="section-header">
          <text class="section-title">成员贡献</text>
          <text class="view-detail" @click="viewMemberDetail">查看详情 ›</text>
        </view>

        <view class="members-list"
>
          <view
            v-for="(member, index) in reportData.members"
            :key="member.name"
            class="member-item"
          >
            <view class="member-rank" :class="{ top3: index < 3 }">{{ index + 1 }}</view>
            
            <image :src="member.avatar" class="member-avatar" />
            
            <view class="member-info"
>
              <text class="member-name">{{ member.name }}</text>
              <view class="member-tags"
>
                <text v-for="tag in member.tags" :key="tag" class="tag">{{ tag }}</text>
              </view>
            </view>            
            
            <view class="member-score"
>
              <text class="score-num">{{ member.score }}</text>
              <text class="score-unit">分</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 任务完成情况 -->
      <view class="tasks-section">
        <view class="section-header"
>
          <text class="section-title">任务统计</text>
        </view>

        <view class="tasks-stats"
>
          <view class="stat-item"
>
            <text class="stat-num">{{ reportData.tasks.total }}</text>
            <text class="stat-label">总任务</text>
          </view>          
          
          <view class="stat-item completed"
>
            <text class="stat-num">{{ reportData.tasks.completed }}</text>
            <text class="stat-label">已完成</text>
          </view>          
          
          <view class="stat-item"
>
            <text class="stat-num">{{ reportData.tasks.completionRate }}%</text>
            <text class="stat-label">完成率</text>
          </view>          
          
          <view class="stat-item"
>
            <text class="stat-num">{{ reportData.tasks.avgTime }}h</text>
            <text class="stat-label">平均耗时</text>
          </view>
        </view>

        <view class="tasks-chart"
>
          <text class="chart-title">任务类型分布</text>          
          
          <view class="chart-bars"
>
            <view
              v-for="type in reportData.tasks.byType"
              :key="type.name"
              class="chart-bar-item"
            >
              <view class="bar-label"
{{ type.name }}</view>              
              
              <view class="bar-visual">
                <view 
                  class="bar-fill"
                  :style="{ width: type.percent + '%', background: type.color }"
                ></view>
                <text class="bar-value">{{ type.count }}</text>
              </view>            
            </view>
          </view>        
        </view>
      </view>

      <!-- 支出分析 -->
      <view class="expense-section">
        <view class="section-header"
>
          <text class="section-title">支出分析</text>
          <text class="total-expense"
>¥{{ reportData.expense.total }}</text>
        </view>

        <view class="expense-chart"
>
          <view class="pie-chart">
            <view class="pie-center">
              <text class="center-label">总支出</text>
              <text class="center-value">¥{{ reportData.expense.total }}</text>
            </view>          
          </view>          
          
          <view class="pie-legend">
            <view
              v-for="item in reportData.expense.categories"
              :key="item.name"
              class="legend-item"
            >
              <view class="legend-dot" :style="{ background: item.color }"></view>
              <view class="legend-info"
>
                <text class="legend-name">{{ item.name }}</text>
                <text class="legend-percent">{{ item.percent }}%</text>
              </view>              
              
              <text class="legend-amount">¥{{ item.amount }}</text>
            </view>          
          </view>        
        </view>
      </view>

      <!-- 心情趋势 -->
      <view class="mood-section">
        <view class="section-header"
>
          <text class="section-title">家庭心情指数</text>
          <view class="mood-average"
>
            <text class="mood-emoji">{{ reportData.mood.emoji }}</text>
            <text class="mood-score">{{ reportData.mood.score }}</text>
          </view>
        </view>

        <view class="mood-chart"
>
          <view class="mood-timeline"
>
            <view
              v-for="(day, index) in reportData.mood.trend"
              :key="index"
              class="mood-day"
            >
              <text class="day-emoji">{{ day.emoji }}</text>              
              
              <view 
                class="day-bar"
                :style="{ height: day.score + '%', background: day.color }"
              ></view>              
              
              <text class="day-label">{{ day.day }}</text>            
            </view>          
          </view>        
        </view>
      </view>

      <!-- 生成报告按钮 -->
      <view class="generate-section"
>
        <view class="generate-btn" @click="generateReport">
          <u-icon name="file-text" size="32" color="#fff"></u-icon>
          <text>生成完整报告</text>
        </view>        
        
        <text class="generate-hint">生成后可以分享到朋友圈或保存到相册</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'

// 响应式数据
const currentPeriod = ref('week')

const periods = [
  { label: '本周', value: 'week' },
  { label: '本月', value: 'month' },
  { label: '本年', value: 'year' }
]

const reportData = ref({
  period: '2026年2月17日 - 2月23日',
  updateTime: '2026-02-23 22:00',
  score: 85,
  rank: 72,
  trend: 'up',
  trendValue: 5,
  dimensions: [
    { name: '任务完成', score: 90, color: '#52C41A' },
    { name: '家庭互动', score: 82, color: '#1890FF' },
    { name: '财务健康', score: 78, color: '#FAAD14' },
    { name: '成员参与', score: 88, color: '#722ED1' }
  ],
  members: [
    {
      name: '爸爸',
      avatar: '../../static/avatar/dad.svg',
      score: 320,
      tags: ['任务达人', '财务管家']
    },
    {
      name: '妈妈',
      avatar: '../../static/avatar/mom.svg',
      score: 295,
      tags: ['家务能手']
    },
    {
      name: '宝贝',
      avatar: '../../static/avatar/baby.svg',
      score: 180,
      tags: ['学习之星']
    }
  ],
  tasks: {
    total: 45,
    completed: 38,
    completionRate: 84,
    avgTime: 2.5,
    byType: [
      { name: '家务', count: 15, percent: 40, color: '#52C41A' },
      { name: '购物', count: 10, percent: 26, color: '#1890FF' },
      { name: '育儿', count: 8, percent: 21, color: '#FAAD14' },
      { name: '其他', count: 5, percent: 13, color: '#999999' }
    ]
  },
  expense: {
    total: 3250,
    categories: [
      { name: '餐饮', amount: 1200, percent: 37, color: '#FF6B6B' },
      { name: '购物', amount: 800, percent: 25, color: '#4ECDC4' },
      { name: '交通', amount: 500, percent: 15, color: '#45B7D1' },
      { name: '娱乐', amount: 450, percent: 14, color: '#96CEB4' },
      { name: '其他', amount: 300, percent: 9, color: '#FFEAA7' }
    ]
  },
  mood: {
    score: 8.2,
    emoji: '😊',
    trend: [
      { day: '周一', emoji: '😊', score: 80, color: '#52C41A' },
      { day: '周二', emoji: '😄', score: 90, color: '#52C41A' },
      { day: '周三', emoji: '😐', score: 60, color: '#FAAD14' },
      { day: '周四', emoji: '😊', score: 75, color: '#52C41A' },
      { day: '周五', emoji: '😄', score: 95, color: '#52C41A' },
      { day: '周六', emoji: '🥰', score: 100, color: '#52C41A' },
      { day: '周日', emoji: '😊', score: 85, color: '#52C41A' }
    ]
  }
})

// 方法
const shareReport = () => {
  uni.showShareMenu({
    withShareTicket: true,
    menus: ['shareAppMessage', 'shareTimeline']
  })
}

const viewMemberDetail = () => {
  uni.navigateTo({ url: '/pages/family/members' })
}

const generateReport = () => {
  uni.showLoading({ title: '生成中...' })
  
  setTimeout(() => {
    uni.hideLoading()
    uni.showModal({
      title: '报告已生成',
      content: '是否保存到相册？',
      success: (res) => {
        if (res.confirm) {
          uni.showToast({ title: '已保存', icon: 'success' })
        }
      }
    })
  }, 1500)
}

const goBack = () => {
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
.family-report-page {
  min-height: 100vh;
  background: #f5f6fa;
}

.nav-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 30rpx;
  padding-top: 60rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);

  .back-btn {
    width: 60rpx;
    height: 60rpx;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .title {
    font-size: 34rpx;
    font-weight: 600;
    color: #fff;
  }

  .right-btn {
    width: 60rpx;
    height: 60rpx;
    display: flex;
    align-items: center;
    justify-content: center;
  }
}

.content {
  padding: 0 30rpx 50rpx;
}

// 周期选择
.period-section {
  margin-top: -30rpx;
  position: relative;
  z-index: 1;
  margin-bottom: 30rpx;
}

.period-tabs {
  display: flex;
  gap: 16rpx;
  margin-bottom: 20rpx;

  .period-tab {
    flex: 1;
    padding: 20rpx 0;
    background: rgba(255, 255, 255, 0.9);
    border-radius: 12rpx;
    text-align: center;
    font-size: 28rpx;
    color: #666;
    border: 2rpx solid transparent;

    &.active {
      background: #fff;
      border-color: #667eea;
      color: #667eea;
      font-weight: 500;
    }
  }
}

.period-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20rpx;

  .period-date {
    font-size: 26rpx;
    color: #666;
  }

  .update-time {
    font-size: 22rpx;
    color: #999;
  }
}

// 得分卡片
.score-card {
  background: #fff;
  border-radius: 24rpx;
  padding: 40rpx;
  margin-bottom: 30rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.06);
}

.score-main {
  display: flex;
  align-items: center;
  gap: 40rpx;
  margin-bottom: 40rpx;

  .score-ring {
    position: relative;
    width: 200rpx;
    height: 200rpx;

    .ring-bg {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      border-radius: 50%;
      background: conic-gradient(#f0f0f0 0deg, #f0f0f0 360deg);
    }

    .ring-progress {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      border-radius: 50%;
      background: conic-gradient(transparent 0deg, transparent var(--progress, 306deg), #667eea var(--progress, 306deg));
      mask: radial-gradient(transparent 55%, black 56%);
      -webkit-mask: radial-gradient(transparent 55%, black 56%);
    }

    .ring-center {
      position: absolute;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      text-align: center;

      .score-value {
        font-size: 56rpx;
        font-weight: 700;
        color: #667eea;
        display: block;
      }

      .score-label {
        font-size: 24rpx;
        color: #999;
      }
    }
  }

  .score-rank {
    flex: 1;

    .rank-text {
      font-size: 28rpx;
      color: #333;
      display: block;
      margin-bottom: 16rpx;
    }

    .trend-badge {
      display: inline-flex;
      align-items: center;
      gap: 8rpx;
      padding: 12rpx 24rpx;
      border-radius: 30rpx;
      font-size: 26rpx;
      color: #fff;

      &.up {
        background: #52C41A;
      }

      &.down {
        background: #FF4D4F;
      }
    }
  }
}

.score-dimensions {
  .dimension-item {
    margin-bottom: 24rpx;

    &:last-child {
      margin-bottom: 0;
    }

    .dim-header {
      display: flex;
      justify-content: space-between;
      margin-bottom: 12rpx;

      .dim-name {
        font-size: 26rpx;
        color: #666;
      }

      .dim-score {
        font-size: 26rpx;
        font-weight: 600;
        color: #333;
      }
    }

    .dim-bar {
      position: relative;
      height: 16rpx;
      background: #f5f5f5;
      border-radius: 8rpx;
      overflow: hidden;

      .dim-fill {
        height: 100%;
        border-radius: 8rpx;
        transition: width 0.5s;
      }
    }
  }
}

// 成员贡献
.members-section {
  background: #fff;
  border-radius: 24rpx;
  padding: 30rpx;
  margin-bottom: 30rpx;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;

  .section-title {
    font-size: 30rpx;
    font-weight: 600;
    color: #333;
  }

  .view-detail {
    font-size: 26rpx;
    color: #667eea;
  }

  .total-expense {
    font-size: 40rpx;
    font-weight: 700;
    color: #FF6B6B;
  }
}

.members-list {
  .member-item {
    display: flex;
    align-items: center;
    padding: 20rpx 0;
    border-bottom: 1rpx solid #f5f5f5;

    &:last-child {
      border-bottom: none;
    }

    .member-rank {
      width: 48rpx;
      height: 48rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      background: #f5f5f5;
      border-radius: 50%;
      font-size: 24rpx;
      font-weight: 600;
      color: #999;
      margin-right: 20rpx;

      &.top3 {
        background: #FFF7E6;
        color: #FAAD14;
      }
    }

    .member-avatar {
      width: 80rpx;
      height: 80rpx;
      border-radius: 50%;
      margin-right: 20rpx;
    }

    .member-info {
      flex: 1;

      .member-name {
        font-size: 30rpx;
        font-weight: 500;
        color: #333;
        display: block;
        margin-bottom: 8rpx;
      }

      .member-tags {
        display: flex;
        gap: 12rpx;

        .tag {
          padding: 6rpx 16rpx;
          background: #f0f5ff;
          border-radius: 8rpx;
          font-size: 22rpx;
          color: #667eea;
        }
      }
    }

    .member-score {
      .score-num {
        font-size: 36rpx;
        font-weight: 700;
        color: #667eea;
      }

      .score-unit {
        font-size: 24rpx;
        color: #999;
      }
    }
  }
}

// 任务统计
.tasks-section {
  background: #fff;
  border-radius: 24rpx;
  padding: 30rpx;
  margin-bottom: 30rpx;
}

.tasks-stats {
  display: flex;
  justify-content: space-around;
  padding: 30rpx 0;
  margin-bottom: 30rpx;
  border-bottom: 1rpx solid #f5f5f5;

  .stat-item {
    text-align: center;

    .stat-num {
      font-size: 40rpx;
      font-weight: 700;
      color: #333;
      display: block;
      margin-bottom: 8rpx;
    }

    .stat-label {
      font-size: 24rpx;
      color: #999;
    }

    &.completed {
      .stat-num {
        color: #52C41A;
      }
    }
  }
}

.tasks-chart {
  .chart-title {
    font-size: 28rpx;
    color: #666;
    margin-bottom: 20rpx;
    display: block;
  }

  .chart-bars {
    .chart-bar-item {
      display: flex;
      align-items: center;
      margin-bottom: 20rpx;

      .bar-label {
        width: 100rpx;
        font-size: 26rpx;
        color: #666;
      }

      .bar-visual {
        flex: 1;
        display: flex;
        align-items: center;
        gap: 16rpx;

        .bar-fill {
          height: 24rpx;
          border-radius: 12rpx;
          min-width: 40rpx;
        }

        .bar-value {
          font-size: 26rpx;
          color: #333;
          font-weight: 500;
        }
      }
    }
  }
}

// 支出分析
.expense-section {
  background: #fff;
  border-radius: 24rpx;
  padding: 30rpx;
  margin-bottom: 30rpx;
}

.expense-chart {
  display: flex;
  gap: 40rpx;

  .pie-chart {
    position: relative;
    width: 200rpx;
    height: 200rpx;
    border-radius: 50%;
    background: conic-gradient(
      #FF6B6B 0deg 133deg,
      #4ECDC4 133deg 223deg,
      #45B7D1 223deg 277deg,
      #96CEB4 277deg 327deg,
      #FFEAA7 327deg 360deg
    );

    .pie-center {
      position: absolute;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      width: 120rpx;
      height: 120rpx;
      background: #fff;
      border-radius: 50%;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;

      .center-label {
        font-size: 22rpx;
        color: #999;
      }

      .center-value {
        font-size: 28rpx;
        font-weight: 700;
        color: #333;
      }
    }
  }

  .pie-legend {
    flex: 1;

    .legend-item {
      display: flex;
      align-items: center;
      margin-bottom: 16rpx;

      .legend-dot {
        width: 16rpx;
        height: 16rpx;
        border-radius: 50%;
        margin-right: 16rpx;
      }

      .legend-info {
        flex: 1;

        .legend-name {
          font-size: 26rpx;
          color: #333;
          display: block;
        }

        .legend-percent {
          font-size: 22rpx;
          color: #999;
        }
      }

      .legend-amount {
        font-size: 28rpx;
        font-weight: 600;
        color: #333;
      }
    }
  }
}

// 心情指数
.mood-section {
  background: #fff;
  border-radius: 24rpx;
  padding: 30rpx;
  margin-bottom: 30rpx;
}

.mood-average {
  display: flex;
  align-items: center;
  gap: 12rpx;

  .mood-emoji {
    font-size: 48rpx;
  }

  .mood-score {
    font-size: 40rpx;
    font-weight: 700;
    color: #52C41A;
  }
}

.mood-chart {
  .mood-timeline {
    display: flex;
    justify-content: space-around;
    align-items: flex-end;
    height: 200rpx;
    padding-top: 40rpx;

    .mood-day {
      display: flex;
      flex-direction: column;
      align-items: center;
      flex: 1;

      .day-emoji {
        font-size: 32rpx;
        margin-bottom: 12rpx;
      }

      .day-bar {
        width: 40rpx;
        border-radius: 20rpx 20rpx 0 0;
        transition: height 0.3s;
        margin-bottom: 12rpx;
      }

      .day-label {
        font-size: 24rpx;
        color: #999;
      }
    }
  }
}

// 生成按钮
.generate-section {
  text-align: center;
  padding: 20rpx 0;

  .generate-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 12rpx;
    padding: 30rpx 0;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 16rpx;
    margin-bottom: 20rpx;

    text {
      font-size: 32rpx;
      font-weight: 600;
      color: #fff;
    }
  }

  .generate-hint {
    font-size: 24rpx;
    color: #999;
  }
}
</style>