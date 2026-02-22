<template>
  <view class="budget-page">
    <!-- 自定义导航栏 -->
    <view class="custom-nav">
      <view class="nav-back" @click="goBack">
        <text class="icon">‹</text>
      </view>
      <view class="nav-title">预算管理</view>
      <view class="nav-action" @click="editBudget">
        <text>设置</text>
      </view>
    </view>

    <!-- 总预算概览 -->
    <view class="budget-overview">
      <view class="overview-header">
        <text class="month-label">{{ currentMonth }}月预算</text>
        <view class="warning-badge" v-if="isOverBudget">
          <text>⚠️ 已超支</text>
        </view>
      </view>
      
      <view class="budget-progress">
        <view class="progress-ring">
          <view class="ring-center">
            <text class="used-amount">¥{{ usedAmount }}</text>
            <text class="total-amount">/ ¥{{ totalBudget }}</text>
          </view>
        </view>
        <view class="progress-info">
          <view class="info-item">
            <text class="info-value" :class="{ danger: usedPercent > 80 }">{{ usedPercent }}%</text>
            <text class="info-label">已使用</text>
          </view>
          <view class="info-divider"></view>
          <view class="info-item">
            <text class="info-value">¥{{ remainingAmount }}</text>
            <text class="info-label">剩余预算</text>
          </view>
        </view>
      </view>

      <view class="daily-suggestion">
        <text class="suggestion-label">💡 每日建议消费</text>
        <text class="suggestion-value">¥{{ dailyLimit }}</text>
        <text class="suggestion-note">（剩余 {{ remainingDays }} 天）</text>
      </view>
    </view>

    <!-- 分类预算 -->
    <view class="category-budget">
      <view class="section-header">
        <text class="section-title">📊 分类预算</text>
        <text class="section-action" @click="showAllCategories">查看全部 ›</text>
      </view>

      <view 
        v-for="category in categoryBudgets" 
        :key="category.name"
        class="category-item"
      >
        <view class="category-header">
          <view class="category-left">
            <text class="category-icon">{{ category.icon }}</text>
            <text class="category-name">{{ category.name }}</text>
          </view>
          <view class="category-right">
            <text class="category-used">¥{{ category.used }}</text>
            <text class="category-total">/ ¥{{ category.budget }}</text>
          </view>
        </view>

        <view class="progress-bar-wrapper">
          <view 
            class="progress-bar" 
            :style="{ width: category.percent + '%' }"
            :class="{ warning: category.percent > 80, danger: category.percent > 100 }"
          ></view>
        </view>

        <view class="category-footer">
          <text class="percent-text" :class="{ danger: category.percent > 100 }">{{ category.percent }}%</text>
          <text v-if="category.percent > 100" class="over-warning">已超支 ¥{{ category.used - category.budget }}</text>
          <text v-else class="remaining-text">剩余 ¥{{ category.budget - category.used }}</text>
        </view>
      </view>
    </view>

    <!-- 历史对比 -->
    <view class="history-compare">
      <view class="section-header">
        <text class="section-title">📈 历史对比</text>
      </view>

      <view class="compare-chart">
        <view 
          v-for="month in historyData" 
          :key="month.month"
          class="compare-item"
        >
          <text class="month-label">{{ month.month }}</text>
          <view class="bar-wrapper">
            <view 
              class="budget-bar" 
              :style="{ height: month.budgetPercent + '%' }"
            ></view>
            <view 
              class="used-bar" 
              :style="{ height: month.usedPercent + '%' }"
              :class="{ over: month.usedPercent > month.budgetPercent }"
            ></view>
          </view>
          <text class="amount-label">¥{{ month.used }}</text>
        </view>
      </view>

      <view class="legend">
        <view class="legend-item">
          <view class="legend-dot budget"></view>
          <text>预算</text>
        </view>
        <view class="legend-item">
          <view class="legend-dot used"></view>
          <text>实际支出</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'

const currentMonth = ref('2')
const totalBudget = ref(5000)
const usedAmount = ref(3850)
const remainingDays = ref(7)

const usedPercent = computed(() => Math.round((usedAmount.value / totalBudget.value) * 100))
const remainingAmount = computed(() => totalBudget.value - usedAmount.value)
const isOverBudget = computed(() => usedAmount.value > totalBudget.value)
const dailyLimit = computed(() => Math.round(remainingAmount.value / remainingDays.value))

const categoryBudgets = ref([
  { name: '食材采购', icon: '🥬', budget: 2000, used: 1850, percent: 92 },
  { name: '日用百货', icon: '🧻', budget: 800, used: 650, percent: 81 },
  { name: '水果零食', icon: '🍎', budget: 600, used: 720, percent: 120 },
  { name: '外出就餐', icon: '🍽️', budget: 1000, used: 480, percent: 48 },
  { name: '其他支出', icon: '📦', budget: 600, used: 150, percent: 25 }
])

const historyData = ref([
  { month: '10月', budgetPercent: 100, usedPercent: 85, used: 4250 },
  { month: '11月', budgetPercent: 100, usedPercent: 98, used: 4900 },
  { month: '12月', budgetPercent: 100, usedPercent: 110, used: 5500 },
  { month: '1月', budgetPercent: 100, usedPercent: 92, used: 4600 },
  { month: '2月', budgetPercent: 100, usedPercent: 77, used: 3850 }
])

const goBack = () => {
  uni.navigateBack()
}

const editBudget = () => {
  uni.showModal({
    title: '设置月预算',
    editable: true,
    placeholderText: '请输入预算金额',
    success: (res) => {
      if (res.confirm && res.content) {
        totalBudget.value = parseInt(res.content) || totalBudget.value
        uni.showToast({ title: '设置成功', icon: 'success' })
      }
    }
  })
}

const showAllCategories = () => {
  uni.showToast({ title: '查看全部分类', icon: 'none' })
}
</script>

<style lang="scss" scoped>
.budget-page {
  min-height: 100vh;
  background: #0f0f23;
}

.custom-nav {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 50px 20px 20px;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);

  .nav-back {
    width: 36px;
    height: 36px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: rgba(255,255,255,0.1);
    border-radius: 10px;

    .icon {
      font-size: 24px;
      color: #fff;
    }
  }

  .nav-title {
    font-size: 18px;
    font-weight: 600;
    color: #fff;
  }

  .nav-action {
    padding: 8px 16px;
    background: rgba(255,255,255,0.1);
    border-radius: 8px;
    font-size: 13px;
    color: #3b82f6;
  }
}

.budget-overview {
  margin: 15px;
  padding: 20px;
  background: linear-gradient(135deg, rgba(102,126,234,0.2) 0%, rgba(118,75,162,0.1) 100%);
  border-radius: 20px;
  border: 1px solid rgba(102,126,234,0.3);

  .overview-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    .month-label {
      font-size: 14px;
      color: rgba(255,255,255,0.7);
    }

    .warning-badge {
      padding: 4px 10px;
      background: rgba(239,68,68,0.2);
      border-radius: 8px;
      font-size: 12px;
      color: #ef4444;
    }
  }

  .budget-progress {
    display: flex;
    align-items: center;
    gap: 30px;

    .progress-ring {
      width: 120px;
      height: 120px;
      border-radius: 50%;
      background: conic-gradient(
        from 0deg,
        #667eea 0deg,
        #764ba2 calc(v-bind(usedPercent) * 3.6deg),
        rgba(255,255,255,0.1) calc(v-bind(usedPercent) * 3.6deg)
      );
      display: flex;
      align-items: center;
      justify-content: center;

      .ring-center {
        width: 90px;
        height: 90px;
        background: #1a1a2e;
        border-radius: 50%;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;

        .used-amount {
          font-size: 18px;
          font-weight: 700;
          color: #fff;
        }

        .total-amount {
          font-size: 12px;
          color: #64748b;
        }
      }
    }

    .progress-info {
      flex: 1;

      .info-item {
        margin-bottom: 15px;

        .info-value {
          display: block;
          font-size: 22px;
          font-weight: 700;
          color: #fff;
          margin-bottom: 4px;

          &.danger {
            color: #ef4444;
          }
        }

        .info-label {
          font-size: 12px;
          color: #64748b;
        }
      }

      .info-divider {
        width: 100%;
        height: 1px;
        background: rgba(255,255,255,0.1);
        margin: 15px 0;
      }
    }
  }

  .daily-suggestion {
    margin-top: 20px;
    padding: 15px;
    background: rgba(34,197,94,0.1);
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: space-between;

    .suggestion-label {
      font-size: 13px;
      color: #22c55e;
    }

    .suggestion-value {
      font-size: 20px;
      font-weight: 700;
      color: #22c55e;
    }

    .suggestion-note {
      font-size: 12px;
      color: #64748b;
    }
  }
}

.category-budget {
  padding: 0 15px;
  margin-bottom: 20px;

  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 15px;

    .section-title {
      font-size: 14px;
      color: #fff;
      font-weight: 500;
    }

    .section-action {
      font-size: 13px;
      color: #3b82f6;
    }
  }

  .category-item {
    padding: 16px;
    background: rgba(255,255,255,0.05);
    border-radius: 12px;
    margin-bottom: 10px;

    .category-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 12px;

      .category-left {
        display: flex;
        align-items: center;
        gap: 10px;

        .category-icon {
          font-size: 20px;
        }

        .category-name {
          font-size: 14px;
          color: #fff;
        }
      }

      .category-right {
        .category-used {
          font-size: 16px;
          font-weight: 600;
          color: #fff;
        }

        .category-total {
          font-size: 12px;
          color: #64748b;
        }
      }
    }

    .progress-bar-wrapper {
      height: 8px;
      background: rgba(255,255,255,0.1);
      border-radius: 4px;
      overflow: hidden;
      margin-bottom: 8px;

      .progress-bar {
        height: 100%;
        background: linear-gradient(90deg, #22c55e 0%, #16a34a 100%);
        border-radius: 4px;
        transition: width 0.3s;

        &.warning {
          background: linear-gradient(90deg, #f59e0b 0%, #d97706 100%);
        }

        &.danger {
          background: linear-gradient(90deg, #ef4444 0%, #dc2626 100%);
        }
      }
    }

    .category-footer {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .percent-text {
        font-size: 12px;
        color: #22c55e;

        &.danger {
          color: #ef4444;
        }
      }

      .over-warning {
        font-size: 11px;
        color: #ef4444;
        padding: 2px 8px;
        background: rgba(239,68,68,0.1);
        border-radius: 4px;
      }

      .remaining-text {
        font-size: 12px;
        color: #64748b;
      }
    }
  }
}

.history-compare {
  padding: 0 15px 30px;

  .section-header {
    margin-bottom: 15px;

    .section-title {
      font-size: 14px;
      color: #fff;
      font-weight: 500;
    }
  }

  .compare-chart {
    display: flex;
    justify-content: space-around;
    align-items: flex-end;
    height: 180px;
    padding: 20px;
    background: rgba(255,255,255,0.05);
    border-radius: 16px;

    .compare-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      flex: 1;

      .month-label {
        font-size: 12px;
        color: #64748b;
        margin-bottom: 8px;
      }

      .bar-wrapper {
        width: 30px;
        height: 120px;
        position: relative;
        margin-bottom: 8px;

        .budget-bar {
          position: absolute;
          bottom: 0;
          left: 0;
          right: 0;
          background: rgba(255,255,255,0.1);
          border-radius: 4px;
        }

        .used-bar {
          position: absolute;
          bottom: 0;
          left: 4px;
          right: 4px;
          background: linear-gradient(180deg, #667eea 0%, #764ba2 100%);
          border-radius: 4px;

          &.over {
            background: linear-gradient(180deg, #ef4444 0%, #dc2626 100%);
          }
        }
      }

      .amount-label {
        font-size: 11px;
        color: #94a3b8;
      }
    }
  }

  .legend {
    display: flex;
    justify-content: center;
    gap: 30px;
    margin-top: 15px;

    .legend-item {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 12px;
      color: #64748b;

      .legend-dot {
        width: 12px;
        height: 12px;
        border-radius: 3px;

        &.budget {
          background: rgba(255,255,255,0.1);
        }

        &.used {
          background: linear-gradient(180deg, #667eea 0%, #764ba2 100%);
        }
      }
    }
  }
}
</style>
