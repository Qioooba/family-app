<template>
  <view class="report-page">
    <!-- 头部 -->
    <view class="header"
    >
      <view class="back-btn" @click="goBack"
    
      >
        <text class="icon"
    >←</text>
      </view>
      <text class="title"
    >家庭报告</text>
      <view class="filter-btn" @click="showFilter = true"
    
      >
        <text>{{ currentPeriod.label }}</text>
        <text class="icon"
    >▼</text>
      </view>
    </view>

    <!-- 报告概览 -->
    <view class="overview-section"
    >
      <view class="overview-card"
    
      >
        <view class="card-header"
    
      >
          <text class="card-title"
    
        >{{ currentPeriod.label }}家庭报告</text>
          <text class="report-date"
    
        >{{ reportDate }}</text>
        </view>
        
        <view class="score-section"
    
      >
          <view class="score-circle"
    
        >
            <text class="score-num"
    
          >{{ reportData.score }}</text>
            <text class="score-label"
    
          >家庭活跃度</text>
          </view>
          
          <view class="score-rank"
    
        >
            <text class="rank-text"
    
          >超越 {{ reportData.rankPercent }}% 的家庭</text>
            <view class="rank-bar"
    
          >
              <view class="rank-fill" :style="{ width: reportData.rankPercent + '%' }"
    
            ></view>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 数据图表 -->
    <view class="charts-section"
    >
      <!-- 任务完成率 -->
      <view class="chart-card"
    
      >
        <view class="chart-header"
    
        >
          <text class="chart-title"
    
      >任务完成情况</text>
          <text class="chart-value"
    
      >{{ reportData.taskCompletion }}%</text>
        </view>
        
        <view class="progress-ring"
    
        >
          <view class="ring-bg"
    
      ></view>
          <view class="ring-fill" :style="{ '--progress': reportData.taskCompletion }"
    
      ></view>
          <view class="ring-center"
    
      >
            <text>{{ reportData.completedTasks }}/{{ reportData.totalTasks }}</text>
          </view>
        </view>
        
        <view class="task-legend"
    
        >
          <view class="legend-item"
    
      >
            <view class="legend-dot" style="background: #10B981;"
    
        ></view>
            <text>已完成 {{ reportData.completedTasks }}</text>
          </view>
          <view class="legend-item"
    
      >
            <view class="legend-dot" style="background: #EF4444;"
    
        ></view>
            <text>未完成 {{ reportData.totalTasks - reportData.completedTasks }}</text>
          </view>
        </view>
      </view>

      <!-- 成员贡献 -->
      <view class="chart-card"
    
      >
        <view class="chart-header"
    
        >
          <text class="chart-title"
    
      >成员贡献排行</text>
        </view>
        
        <view class="member-list"
    
        >
          <view 
            v-for="(member, index) in reportData.memberStats" 
            :key="index"
            class="member-item"
          >
            <view class="member-rank"
    
        >{{ index + 1 }}</view>
            <image :src="member.avatar" class="member-avatar" />
            <view class="member-info"
    
        >
              <text class="member-name"
    
      >{{ member.name }}</text>
              <view class="member-progress"
    
      >
                <view class="progress-fill" :style="{ width: member.contribution + '%', background: member.color }"
    
        ></view>
              </view>
            </view>
            <text class="member-score"
    
        >{{ member.score }}</text>
          </view>
        </view>
      </view>

      <!-- 活动趋势 -->
      <view class="chart-card"
    
      >
        <view class="chart-header"
    
        >
          <text class="chart-title"
    
      >家庭活动趋势</text>
        </view>
        
        <view class="trend-chart"
    
        >
          <view class="chart-y-axis"
    
      >
            <text v-for="i in 5" :key="i"
    
        >{{ getYAxisValue(i) }}</text>
          </view>
          
          <view class="chart-bars"
    
      >
            <view 
              v-for="(data, index) in reportData.weeklyActivity" 
              :key="index"
              class="bar-item"
            >
              <view class="bar-wrapper"
    
        >
                <view 
                  class="bar" 
                  :style="{ height: getBarHeight(data.value) + '%', background: data.color }"
    
          
                ></view>
              </view>
              <text class="bar-label"
    
        >{{ data.day }}</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 消费统计 -->
      <view class="chart-card"
    
      >
        <view class="chart-header"
    
        >
          <text class="chart-title"
    
      >家庭消费分析</text>
          <text class="chart-total"
    
      >¥{{ reportData.totalExpense }}</text>
        </view>
        
        <view class="expense-categories"
    
        >
          <view 
            v-for="(cat, index) in reportData.expenseCategories" 
            :key="index"
            class="expense-item"
          >
            <view class="expense-icon" :style="{ background: cat.color }"
    
        >
              <text>{{ cat.icon }}</text>
            </view>
            <view class="expense-info"
    
        >
              <text class="expense-name"
    
      >{{ cat.name }}</text>
              <view class="expense-bar"
    
      >
                <view class="expense-fill" :style="{ width: cat.percent + '%', background: cat.color }"
    
        ></view>
              </view>
            </view>
            <view class="expense-amount"
    
        >
              <text class="amount"
    
      >¥{{ cat.amount }}</text>
              <text class="percent"
    
      >{{ cat.percent }}%</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 导出按钮 -->
    <view class="export-section"
    
  >
      <view class="export-btn" @click="exportReport"
    
    >
        <text class="btn-icon"
    
  >📊</text>
        <text>导出完整报告</text>
      </view>
    </view>

    <!-- 时间筛选弹窗 -->
    <view v-if="showFilter" class="modal-overlay"
    >
      <view class="modal-mask" @click="showFilter = false"
    
    ></view>
      <view class="modal-content"
    
    >
        <view class="modal-header"
    
      >
          <text class="modal-title"
    
    >选择时间范围</text>
        </view>
        
        <view class="period-list"
    
      >
          <view 
            v-for="period in periods" 
            :key="period.value"
            class="period-item"
            :class="{ active: currentPeriod.value === period.value }"
            @click="selectPeriod(period)"
          >
            <text class="period-label"
    
      >{{ period.label }}</text>
            <view v-if="currentPeriod.value === period.value" class="period-check"
    
      >✓</view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'

const showFilter = ref(false)
const currentPeriod = ref({ label: '本周', value: 'week' })

const periods = [
  { label: '本周', value: 'week' },
  { label: '上周', value: 'lastWeek' },
  { label: '本月', value: 'month' },
  { label: '上月', value: 'lastMonth' }
]

// 模拟报告数据
const reportData = ref({
  score: 85,
  rankPercent: 72,
  taskCompletion: 78,
  completedTasks: 28,
  totalTasks: 36,
  totalExpense: 2580,
  memberStats: [
    { name: '妈妈', avatar: '../../static/avatar/mom.png', score: 450, contribution: 90, color: '#FF6B6B' },
    { name: '爸爸', avatar: '../../static/avatar/dad.png', score: 380, contribution: 76, color: '#3B82F6' },
    { name: '宝贝', avatar: '../../static/avatar/baby.png', score: 320, contribution: 64, color: '#F59E0B' },
    { name: '爷爷', avatar: '../../static/avatar/grandpa.png', score: 280, contribution: 56, color: '#10B981' }
  ],
  weeklyActivity: [
    { day: '周一', value: 85, color: '#667eea' },
    { day: '周二', value: 65, color: '#667eea' },
    { day: '周三', value: 90, color: '#667eea' },
    { day: '周四', value: 75, color: '#667eea' },
    { day: '周五', value: 95, color: '#667eea' },
    { day: '周六', value: 100, color: '#F59E0B' },
    { day: '周日', value: 88, color: '#F59E0B' }
  ],
  expenseCategories: [
    { name: '食品生鲜', icon: '🥬', amount: 860, percent: 33, color: '#10B981' },
    { name: '日用百货', icon: '🧻', amount: 520, percent: 20, color: '#3B82F6' },
    { name: '餐饮外卖', icon: '🍔', amount: 480, percent: 19, color: '#F59E0B' },
    { name: '交通出行', icon: '🚗', amount: 420, percent: 16, color: '#8B5CF6' },
    { name: '其他', icon: '📦', amount: 300, percent: 12, color: '#6B7280' }
  ]
})

const reportDate = computed(() => {
  const now = new Date()
  const start = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000)
  return `${formatDate(start)} - ${formatDate(now)}`
})

onMounted(() => {
  loadReportData()
})

const loadReportData = () => {
  // 加载报告数据
}

const formatDate = (date) => {
  return `${date.getMonth() + 1}.${date.getDate()}`
}

const getYAxisValue = (i) => {
  const max = 100
  return Math.round(max - (max / 4) * (i - 1))
}

const getBarHeight = (value) => {
  return Math.min(value, 100)
}

const selectPeriod = (period) => {
  currentPeriod.value = period
  showFilter.value = false
  loadReportData()
}

const exportReport = () => {
  uni.showActionSheet({
    itemList: ['导出为图片', '导出为PDF', '分享报告'],
    success: (res) => {
      switch (res.tapIndex) {
        case 0:
          uni.showToast({ title: '已保存为图片', icon: 'success' })
          break
        case 1:
          uni.showToast({ title: 'PDF生成中...', icon: 'loading' })
          break
        case 2:
          uni.showShareMenu({
            withShareTicket: true,
            menus: ['shareAppMessage', 'shareTimeline']
          })
          break
      }
    }
  })
}

const goBack = () => {
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
.report-page {
  min-height: 100vh;
  background: #0f0f23;
  padding-bottom: 40rpx;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 50rpx 30rpx 30rpx;

  .back-btn {
    width: 60rpx;
    height: 60rpx;
    display: flex;
    align-items: center;
    justify-content: center;

    .icon {
      font-size: 40rpx;
      color: #fff;
    }
  }

  .title {
    font-size: 36rpx;
    font-weight: 600;
    color: #fff;
  }

  .filter-btn {
    display: flex;
    align-items: center;
    gap: 8rpx;
    padding: 12rpx 24rpx;
    background: rgba(255,255,255,0.1);
    border-radius: 30rpx;

    text {
      font-size: 26rpx;
      color: #94a3b8;

      &.icon {
        font-size: 20rpx;
      }
    }
  }
}

.overview-section {
  padding: 0 30rpx 30rpx;

  .overview-card {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 32rpx;
    padding: 40rpx;

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 40rpx;

      .card-title {
        font-size: 32rpx;
        font-weight: 600;
        color: #fff;
      }

      .report-date {
        font-size: 24rpx;
        color: rgba(255,255,255,0.7);
      }
    }

    .score-section {
      display: flex;
      align-items: center;
      gap: 40rpx;

      .score-circle {
        width: 180rpx;
        height: 180rpx;
        background: rgba(255,255,255,0.15);
        border-radius: 50%;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        border: 8rpx solid rgba(255,255,255,0.2);

        .score-num {
          font-size: 64rpx;
          font-weight: 700;
          color: #fff;
        }

        .score-label {
          font-size: 24rpx;
          color: rgba(255,255,255,0.8);
        }
      }

      .score-rank {
        flex: 1;

        .rank-text {
          font-size: 28rpx;
          color: rgba(255,255,255,0.9);
          margin-bottom: 16rpx;
          display: block;
        }

        .rank-bar {
          height: 12rpx;
          background: rgba(255,255,255,0.2);
          border-radius: 6rpx;
          overflow: hidden;

          .rank-fill {
            height: 100%;
            background: #fbbf24;
            border-radius: 6rpx;
            transition: width 1s ease;
          }
        }
      }
    }
  }
}

.charts-section {
  padding: 0 30rpx;

  .chart-card {
    background: rgba(255,255,255,0.05);
    border-radius: 24rpx;
    padding: 30rpx;
    margin-bottom: 24rpx;
    border: 1rpx solid rgba(255,255,255,0.08);

    .chart-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 30rpx;

      .chart-title {
        font-size: 30rpx;
        font-weight: 600;
        color: #fff;
      }

      .chart-value, .chart-total {
        font-size: 36rpx;
        font-weight: 700;
        color: #667eea;
      }

      .chart-total {
        color: #fbbf24;
      }
    }
  }
}

// 任务完成环形图
.progress-ring {
  position: relative;
  width: 200rpx;
  height: 200rpx;
  margin: 0 auto 30rpx;

  .ring-bg {
    position: absolute;
    width: 100%;
    height: 100%;
    border-radius: 50%;
    background: conic-gradient(
      #10B981 0deg,
      #10B981 calc(var(--progress) * 3.6deg),
      #EF4444 calc(var(--progress) * 3.6deg),
      #EF4444 360deg
    );
    -webkit-mask: radial-gradient(transparent 55%, black 56%);
    mask: radial-gradient(transparent 55%, black 56%);
  }

  .ring-center {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    text-align: center;

    text {
      font-size: 36rpx;
      font-weight: 600;
      color: #fff;
    }
  }
}

.task-legend {
  display: flex;
  justify-content: center;
  gap: 40rpx;

  .legend-item {
    display: flex;
    align-items: center;
    gap: 12rpx;

    .legend-dot {
      width: 16rpx;
      height: 16rpx;
      border-radius: 50%;
    }

    text {
      font-size: 26rpx;
      color: #94a3b8;
    }
  }
}

// 成员贡献
.member-list {
  .member-item {
    display: flex;
    align-items: center;
    gap: 20rpx;
    padding: 20rpx 0;

    .member-rank {
      width: 40rpx;
      font-size: 28rpx;
      font-weight: 600;
      color: #64748b;
      text-align: center;
    }

    .member-avatar {
      width: 64rpx;
      height: 64rpx;
      border-radius: 50%;
      background: rgba(255,255,255,0.1);
    }

    .member-info {
      flex: 1;

      .member-name {
        display: block;
        font-size: 28rpx;
        color: #fff;
        margin-bottom: 12rpx;
      }

      .member-progress {
        height: 10rpx;
        background: rgba(255,255,255,0.1);
        border-radius: 5rpx;
        overflow: hidden;

        .progress-fill {
          height: 100%;
          border-radius: 5rpx;
          transition: width 0.5s ease;
        }
      }
    }

    .member-score {
      font-size: 32rpx;
      font-weight: 600;
      color: #fbbf24;
    }
  }
}

// 趋势图
.trend-chart {
  display: flex;
  height: 240rpx;

  .chart-y-axis {
    width: 60rpx;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    text-align: right;
    padding-right: 16rpx;

    text {
      font-size: 20rpx;
      color: #64748b;
    }
  }

  .chart-bars {
    flex: 1;
    display: flex;
    justify-content: space-around;
    align-items: flex-end;

    .bar-item {
      flex: 1;
      display: flex;
      flex-direction: column;
      align-items: center;

      .bar-wrapper {
        width: 40rpx;
        height: 180rpx;
        background: rgba(255,255,255,0.05);
        border-radius: 20rpx;
        overflow: hidden;
        display: flex;
        align-items: flex-end;

        .bar {
          width: 100%;
          border-radius: 20rpx;
          transition: height 0.5s ease;
        }
      }

      .bar-label {
        margin-top: 12rpx;
        font-size: 22rpx;
        color: #64748b;
      }
    }
  }
}

// 消费统计
.expense-categories {
  .expense-item {
    display: flex;
    align-items: center;
    gap: 20rpx;
    padding: 20rpx 0;
    border-bottom: 1rpx solid rgba(255,255,255,0.05);

    &:last-child {
      border-bottom: none;
    }

    .expense-icon {
      width: 72rpx;
      height: 72rpx;
      border-radius: 16rpx;
      display: flex;
      align-items: center;
      justify-content: center;

      text {
        font-size: 36rpx;
      }
    }

    .expense-info {
      flex: 1;

      .expense-name {
        display: block;
        font-size: 28rpx;
        color: #fff;
        margin-bottom: 12rpx;
      }

      .expense-bar {
        height: 8rpx;
        background: rgba(255,255,255,0.1);
        border-radius: 4rpx;
        overflow: hidden;

        .expense-fill {
          height: 100%;
          border-radius: 4rpx;
        }
      }
    }

    .expense-amount {
      text-align: right;

      .amount {
        display: block;
        font-size: 30rpx;
        font-weight: 600;
        color: #fff;
        margin-bottom: 4rpx;
      }

      .percent {
        font-size: 22rpx;
        color: #64748b;
      }
    }
  }
}

// 导出按钮
.export-section {
  padding: 40rpx 30rpx;

  .export-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 16rpx;
    padding: 32rpx 0;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 40rpx;

    .btn-icon {
      font-size: 40rpx;
    }

    text {
      font-size: 32rpx;
      font-weight: 600;
      color: #fff;
    }
  }
}

// 弹窗
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 1000;
  display: flex;
  align-items: flex-end;
  justify-content: center;

  .modal-mask {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0,0,0,0.7);
  }

  .modal-content {
    position: relative;
    width: 100%;
    background: #1a1a2e;
    border-radius: 40rpx 40rpx 0 0;
    padding: 40rpx;
    animation: slideUp 0.3s ease;

    @keyframes slideUp {
      from { transform: translateY(100%); }
      to { transform: translateY(0); }
    }

    .modal-header {
      margin-bottom: 40rpx;

      .modal-title {
        font-size: 36rpx;
        font-weight: 600;
        color: #fff;
      }
    }

    .period-list {
      .period-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 30rpx 0;
        border-bottom: 1rpx solid rgba(255,255,255,0.1);

        &:last-child {
          border-bottom: none;
        }

        &.active {
          .period-label {
            color: #667eea;
            font-weight: 600;
          }
        }

        .period-label {
          font-size: 30rpx;
          color: #fff;
        }

        .period-check {
          width: 40rpx;
          height: 40rpx;
          background: #667eea;
          border-radius: 50%;
          text-align: center;
          line-height: 40rpx;
          color: #fff;
          font-size: 24rpx;
        }
      }
    }
  }
}
</style>