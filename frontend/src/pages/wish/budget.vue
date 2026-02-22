<template>
  <view class="wish-budget-page">
    <!-- 顶部导航 -->
    <view class="nav-bar">
      <view class="back-btn" @click="goBack">
        <u-icon name="arrow-left" size="40" color="#333"></u-icon>
      </view>
      <text class="title">心愿预算</text>
      <view class="right-btn" @click="showBudgetModal">
        <u-icon name="setting" size="36" color="#fff"></u-icon>
      </view>
    </view>

    <view class="content">
      <!-- 总预算卡片 -->
      <view class="budget-overview">
        <view class="budget-card">
          <view class="budget-header">
            <view class="budget-info"
>
              <text class="budget-label">本月心愿预算</text>
              <text class="budget-amount">¥{{ formatAmount(totalBudget) }}</text>
            </view>
            <view class="budget-status" :class="budgetStatusClass">
              {{ budgetStatusText }}
            </view>
          </view>

          <!-- 进度条 -->
          <view class="progress-section">
            <view class="progress-bar">
              <view class="progress-track"></view>
              <view 
                class="progress-fill" 
                :class="progressClass"
                :style="{ width: progressPercent + '%' }"
              ></view>
              <view 
                class="progress-marker"
                :style="{ left: progressPercent + '%' }"
              ></view>
            </view>
            
            <view class="progress-labels">
              <text class="used-label">已使用 ¥{{ formatAmount(usedBudget) }}</text>
              <text class="remain-label">剩余 ¥{{ formatAmount(remainingBudget) }}</text>
            </view>
          </view>

          <!-- 统计数字 -->
          <view class="budget-stats">
            <view class="stat-item">
              <text class="stat-value">{{ wishStats.total }}</text>
              <text class="stat-label">心愿总数</text>
            </view>
            <view class="stat-divider"></view>
            <view class="stat-item">
              <text class="stat-value">{{ wishStats.claimed }}</text>
              <text class="stat-label">已认领</text>
            </view>
            <view class="stat-divider"></view>
            <view class="stat-item">
              <text class="stat-value">{{ wishStats.completed }}</text>
              <text class="stat-label">已实现</text>
            </view>
            <view class="stat-divider"></view>
            <view class="stat-item">
              <text class="stat-value highlight">{{ wishStats.pending }}</text>
              <text class="stat-label">待实现</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 分类预算 -->
      <view class="section">
        <view class="section-header">
          <text class="section-title">分类预算</text>
          <text class="section-action" @click="showCategoryModal">调整分配</text>
        </view>

        <view class="category-list">
          <view
            v-for="cat in categoryBudgets"
            :key="cat.type"
            class="category-item"
            @click="showCategoryDetail(cat)"
          >
            <view class="category-icon" :style="{ background: cat.color + '20', color: cat.color }"
>
              <text>{{ cat.icon }}</text>
            </view>
            
            <view class="category-info">
              <view class="category-header">
                <text class="category-name">{{ cat.name }}</text>
                <text class="category-amount">¥{{ formatAmount(cat.used) }}/¥{{ formatAmount(cat.budget) }}</text>
              </view>
              
              <view class="category-progress"
>
                <view class="mini-progress-bar">
                  <view 
                    class="mini-progress-fill"
                    :style="{ 
                      width: (cat.used / cat.budget * 100) + '%',
                      background: cat.color
                    }"
                  ></view>
                </view>
                <text class="category-percent">{{ Math.round(cat.used / cat.budget * 100) }}%</text>
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- 心愿支出明细 -->
      <view class="section">
        <view class="section-header"
>
          <text class="section-title">支出明细</text>
          <text class="section-action" @click="showFilterModal">筛选</text>
        </view>

        <scroll-view class="expense-list" scroll-y>
          <view
            v-for="expense in expenseList"
            :key="expense.id"
            class="expense-item"
          >
            <view class="expense-icon">
              <text>{{ expense.icon }}</text>
            </view>
            
            <view class="expense-info">
              <text class="expense-name">{{ expense.name }}</text>
              <text class="expense-date">{{ expense.date }}</text>
            </view>
            
            <view class="expense-amount">
              <text :class="{ 'amount-negative': expense.amount < 0 }">
                {{ expense.amount > 0 ? '+' : '' }}¥{{ formatAmount(Math.abs(expense.amount)) }}
              </text>
            </view>
          </view>
          
          <view v-if="expenseList.length === 0" class="empty-state">
            <u-icon name="file-text" size="80" color="#ddd"></u-icon>
            <text class="empty-text">暂无支出记录</text>
          </view>
        </scroll-view>
      </view>
    </view>

    <!-- 预算设置弹窗 -->
    <u-popup 
      v-model:show="budgetModalVisible" 
      mode="bottom"
      round
      closeable
    >
      <view class="modal-content">
        <view class="modal-header">
          <text class="modal-title">设置预算</text>
        </view>

        <view class="modal-body">
          <view class="input-group">
            <text class="input-label">每月心愿预算</text>
            <view class="amount-input"
>
              <text class="currency">¥</text>
              <input
                v-model="tempBudget"
                type="digit"
                placeholder="请输入金额"
                class="budget-input"
              />
            </view>
          </view>

          <view class="quick-amounts">
            <text class="quick-title">快捷选择</text>
            <view class="quick-options"
>
              <view
                v-for="amount in quickAmounts"
                :key="amount"
                class="quick-option"
                :class="{ active: tempBudget == amount }"
                @click="tempBudget = amount"
              >
                ¥{{ amount }}
              </view>
            </view>
          </view>

          <view class="input-group">
            <text class="input-label">提醒阈值</text>
            <view class="threshold-slider">
              <slider
                :value="thresholdPercent"
                min="50"
                max="100"
                show-value
                activeColor="#5B8FF9"
                @change="onThresholdChange"
              />
            </view>
            <text class="threshold-hint">预算使用超过{{ thresholdPercent }}%时提醒我</text>
          </view>
        </view>

        <view class="modal-footer">
          <view class="btn-cancel" @click="budgetModalVisible = false">取消</view>
          <view class="btn-confirm" @click="saveBudget">确定</view>
        </view>
      </view>
    </u-popup>

    <!-- 分类预算设置弹窗 -->
    <u-popup 
      v-model:show="categoryModalVisible" 
      mode="bottom"
      round
      closeable
    >
      <view class="modal-content">
        <view class="modal-header">
          <text class="modal-title">分配分类预算</text>
          <text class="modal-subtitle">总预算 ¥{{ formatAmount(totalBudget) }}</text>
        </view>

        <scroll-view class="modal-body" scroll-y style="max-height: 600rpx;">
          <view
            v-for="cat in tempCategoryBudgets"
            :key="cat.type"
            class="category-budget-item"
          >
            <view class="cat-info">
              <text class="cat-icon">{{ cat.icon }}</text>
              <text class="cat-name">{{ cat.name }}</text>
            </view>
            
            <view class="cat-input">
              <text class="currency">¥</text>
              <input
                v-model="cat.budget"
                type="digit"
                class="cat-budget-input"
                @blur="validateCategoryBudget(cat)"
              />
            </view>
          </view>

          <view class="budget-summary">
            <text class="summary-text">已分配: ¥{{ formatAmount(allocatedBudget) }}</text>
            <text 
              class="summary-remain"
              :class="{ warning: remainingAllocation < 0 }"
            >
              剩余: ¥{{ formatAmount(remainingAllocation) }}
            </text>
          </view>
        </scroll-view>

        <view class="modal-footer">
          <view class="btn-cancel" @click="categoryModalVisible = false">取消</view>
          <view class="btn-confirm" @click="saveCategoryBudget">确定</view>
        </view>
      </view>
    </u-popup>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'

// 响应式数据
const totalBudget = ref(5000)
const usedBudget = ref(3250)
const thresholdPercent = ref(80)
const budgetModalVisible = ref(false)
const categoryModalVisible = ref(false)
const tempBudget = ref('')

// 分类预算
const categoryBudgets = ref([
  { type: 'item', name: '物品心愿', icon: '🎁', budget: 2000, used: 1500, color: '#FF6B6B' },
  { type: 'experience', name: '体验心愿', icon: '✈️', budget: 1500, used: 800, color: '#4ECDC4' },
  { type: 'goal', name: '目标心愿', icon: '🎯', budget: 800, used: 600, color: '#45B7D1' },
  { type: 'learn', name: '学习心愿', icon: '📚', budget: 500, used: 200, color: '#96CEB4' },
  { type: 'relation', name: '关系心愿', icon: '❤️', budget: 200, used: 150, color: '#FFEAA7' }
])

const tempCategoryBudgets = ref([])

// 快捷金额
const quickAmounts = [1000, 2000, 3000, 5000, 8000, 10000]

// 心愿统计
const wishStats = ref({
  total: 12,
  claimed: 8,
  completed: 5,
  pending: 3
})

// 支出明细
const expenseList = ref([
  { id: 1, name: '购买乐高套装', icon: '🎁', date: '2026-02-20', amount: -599 },
  { id: 2, name: '周末亲子游', icon: '✈️', date: '2026-02-18', amount: -1200 },
  { id: 3, name: '预算调整', icon: '💰', date: '2026-02-15', amount: 2000 },
  { id: 4, name: '在线课程', icon: '📚', date: '2026-02-10', amount: -299 },
  { id: 5, name: '生日礼物', icon: '❤️', date: '2026-02-08', amount: -150 }
])

// 计算属性
const remainingBudget = computed(() => totalBudget.value - usedBudget.value)

const progressPercent = computed(() => {
  const percent = (usedBudget.value / totalBudget.value * 100)
  return Math.min(percent, 100)
})

const progressClass = computed(() => {
  if (progressPercent.value >= 100) return 'danger'
  if (progressPercent.value >= thresholdPercent.value) return 'warning'
  return 'normal'
})

const budgetStatusText = computed(() => {
  if (progressPercent.value >= 100) return '超支'
  if (progressPercent.value >= thresholdPercent.value) return '紧张'
  if (progressPercent.value >= 50) return '正常'
  return '充足'
})

const budgetStatusClass = computed(() => {
  if (progressPercent.value >= 100) return 'status-danger'
  if (progressPercent.value >= thresholdPercent.value) return 'status-warning'
  if (progressPercent.value >= 50) return 'status-normal'
  return 'status-good'
})

const allocatedBudget = computed(() => {
  return tempCategoryBudgets.value.reduce((sum, cat) => sum + (parseFloat(cat.budget) || 0), 0)
})

const remainingAllocation = computed(() => {
  return totalBudget.value - allocatedBudget.value
})

// 方法
const formatAmount = (amount) => {
  if (!amount) return '0'
  return parseFloat(amount).toLocaleString('zh-CN', {
    minimumFractionDigits: 0,
    maximumFractionDigits: 2
  })
}

const showBudgetModal = () => {
  tempBudget.value = totalBudget.value.toString()
  budgetModalVisible.value = true
}

const onThresholdChange = (e) => {
  thresholdPercent.value = e.detail.value
}

const saveBudget = () => {
  const budget = parseFloat(tempBudget.value)
  if (!budget || budget <= 0) {
    uni.showToast({ title: '请输入有效金额', icon: 'none' })
    return
  }
  totalBudget.value = budget
  budgetModalVisible.value = false
  uni.showToast({ title: '设置成功', icon: 'success' })
}

const showCategoryModal = () => {
  tempCategoryBudgets.value = JSON.parse(JSON.stringify(categoryBudgets.value))
  categoryModalVisible.value = true
}

const validateCategoryBudget = (cat) => {
  let budget = parseFloat(cat.budget) || 0
  if (budget < 0) budget = 0
  cat.budget = budget
}

const saveCategoryBudget = () => {
  if (remainingAllocation.value < 0) {
    uni.showToast({ title: '超出总预算', icon: 'none' })
    return
  }
  categoryBudgets.value = JSON.parse(JSON.stringify(tempCategoryBudgets.value))
  categoryModalVisible.value = false
  uni.showToast({ title: '设置成功', icon: 'success' })
}

const showCategoryDetail = (cat) => {
  uni.showModal({
    title: cat.name,
    content: `预算: ¥${formatAmount(cat.budget)}\n已用: ¥${formatAmount(cat.used)}\n剩余: ¥${formatAmount(cat.budget - cat.used)}`,
    showCancel: false
  })
}

const showFilterModal = () => {
  uni.showActionSheet({
    itemList: ['全部', '收入', '支出', '本月', '上月'],
    success: (res) => {
    }
  })
}

const goBack = () => {
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
.wish-budget-page {
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
  padding: 0 30rpx 30rpx;
}

// 预算总览
.budget-overview {
  margin-top: -40rpx;
  position: relative;
  z-index: 1;
}

.budget-card {
  background: #fff;
  border-radius: 24rpx;
  padding: 40rpx;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.08);
}

.budget-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 40rpx;

  .budget-label {
    font-size: 26rpx;
    color: #999;
    display: block;
    margin-bottom: 12rpx;
  }

  .budget-amount {
    font-size: 56rpx;
    font-weight: 700;
    color: #333;
  }

  .budget-status {
    padding: 8rpx 20rpx;
    border-radius: 20rpx;
    font-size: 24rpx;
    font-weight: 500;

    &.status-good {
      background: #E6F7FF;
      color: #1890FF;
    }

    &.status-normal {
      background: #F6FFED;
      color: #52C41A;
    }

    &.status-warning {
      background: #FFF7E6;
      color: #FAAD14;
    }

    &.status-danger {
      background: #FFF1F0;
      color: #FF4D4F;
    }
  }
}

// 进度条
.progress-section {
  margin-bottom: 40rpx;

  .progress-bar {
    position: relative;
    height: 24rpx;
    background: #f5f5f5;
    border-radius: 12rpx;
    overflow: visible;
    margin-bottom: 20rpx;

    .progress-track {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      border-radius: 12rpx;
    }

    .progress-fill {
      position: absolute;
      top: 0;
      left: 0;
      height: 100%;
      border-radius: 12rpx;
      transition: all 0.3s;

      &.normal {
        background: linear-gradient(90deg, #52C41A 0%, #73D13D 100%);
      }

      &.warning {
        background: linear-gradient(90deg, #FAAD14 0%, #FFC53D 100%);
      }

      &.danger {
        background: linear-gradient(90deg, #FF4D4F 0%, #FF7875 100%);
      }
    }

    .progress-marker {
      position: absolute;
      top: -8rpx;
      width: 40rpx;
      height: 40rpx;
      background: #fff;
      border: 6rpx solid #5B8FF9;
      border-radius: 50%;
      transform: translateX(-50%);
      box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.15);
      transition: all 0.3s;
    }
  }

  .progress-labels {
    display: flex;
    justify-content: space-between;

    .used-label {
      font-size: 26rpx;
      color: #666;
    }

    .remain-label {
      font-size: 26rpx;
      color: #999;
    }
  }
}

// 预算统计
.budget-stats {
  display: flex;
  justify-content: space-around;
  padding-top: 30rpx;
  border-top: 1rpx solid #f5f5f5;

  .stat-item {
    display: flex;
    flex-direction: column;
    align-items: center;

    .stat-value {
      font-size: 36rpx;
      font-weight: 600;
      color: #333;

      &.highlight {
        color: #5B8FF9;
      }
    }

    .stat-label {
      font-size: 24rpx;
      color: #999;
      margin-top: 8rpx;
    }
  }

  .stat-divider {
    width: 1rpx;
    height: 60rpx;
    background: #f0f0f0;
  }
}

// 分区块
.section {
  margin-top: 40rpx;

  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20rpx;

    .section-title {
      font-size: 32rpx;
      font-weight: 600;
      color: #333;
    }

    .section-action {
      font-size: 26rpx;
      color: #5B8FF9;
    }
  }
}

// 分类列表
.category-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;

  .category-item {
    display: flex;
    align-items: center;
    background: #fff;
    border-radius: 16rpx;
    padding: 24rpx;

    .category-icon {
      width: 80rpx;
      height: 80rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 16rpx;
      font-size: 40rpx;
      margin-right: 20rpx;
    }

    .category-info {
      flex: 1;
      min-width: 0;

      .category-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 12rpx;

        .category-name {
          font-size: 28rpx;
          font-weight: 500;
          color: #333;
        }

        .category-amount {
          font-size: 24rpx;
          color: #666;
        }
      }

      .category-progress {
        display: flex;
        align-items: center;
        gap: 16rpx;

        .mini-progress-bar {
          flex: 1;
          height: 12rpx;
          background: #f5f5f5;
          border-radius: 6rpx;
          overflow: hidden;

          .mini-progress-fill {
            height: 100%;
            border-radius: 6rpx;
            transition: all 0.3s;
          }
        }

        .category-percent {
          font-size: 22rpx;
          color: #999;
          min-width: 60rpx;
          text-align: right;
        }
      }
    }
  }
}

// 支出明细
.expense-list {
  max-height: 600rpx;
  background: #fff;
  border-radius: 16rpx;
  padding: 0 24rpx;

  .expense-item {
    display: flex;
    align-items: center;
    padding: 24rpx 0;
    border-bottom: 1rpx solid #f5f5f5;

    &:last-child {
      border-bottom: none;
    }

    .expense-icon {
      width: 72rpx;
      height: 72rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      background: #f5f6fa;
      border-radius: 16rpx;
      font-size: 36rpx;
      margin-right: 20rpx;
    }

    .expense-info {
      flex: 1;

      .expense-name {
        font-size: 28rpx;
        color: #333;
        display: block;
      }

      .expense-date {
        font-size: 24rpx;
        color: #999;
        margin-top: 4rpx;
        display: block;
      }
    }

    .expense-amount {
      font-size: 30rpx;
      font-weight: 500;
      color: #333;

      .amount-negative {
        color: #FF4D4F;
      }
    }
  }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 80rpx 0;

  .empty-text {
    font-size: 28rpx;
    color: #999;
    margin-top: 20rpx;
  }
}

// 弹窗样式
.modal-content {
  padding: 30rpx 0;

  .modal-header {
    text-align: center;
    padding: 0 30rpx 30rpx;
    border-bottom: 1rpx solid #f5f5f5;

    .modal-title {
      font-size: 32rpx;
      font-weight: 600;
      color: #333;
      display: block;
    }

    .modal-subtitle {
      font-size: 26rpx;
      color: #999;
      margin-top: 8rpx;
      display: block;
    }
  }

  .modal-body {
    padding: 30rpx;
  }

  .modal-footer {
    display: flex;
    gap: 20rpx;
    padding: 0 30rpx;

    .btn-cancel, .btn-confirm {
      flex: 1;
      padding: 24rpx 0;
      border-radius: 12rpx;
      text-align: center;
      font-size: 30rpx;
    }

    .btn-cancel {
      background: #f5f5f5;
      color: #666;
    }

    .btn-confirm {
      background: #5B8FF9;
      color: #fff;
    }
  }
}

// 输入组
.input-group {
  margin-bottom: 40rpx;

  .input-label {
    font-size: 28rpx;
    color: #333;
    margin-bottom: 16rpx;
    display: block;
  }

  .amount-input {
    display: flex;
    align-items: center;
    background: #f5f6fa;
    border-radius: 12rpx;
    padding: 0 24rpx;

    .currency {
      font-size: 36rpx;
      color: #333;
      font-weight: 600;
      margin-right: 12rpx;
    }

    .budget-input {
      flex: 1;
      height: 80rpx;
      font-size: 36rpx;
      color: #333;
      background: transparent;
    }
  }
}

// 快捷金额
.quick-amounts {
  margin-bottom: 40rpx;

  .quick-title {
    font-size: 26rpx;
    color: #666;
    margin-bottom: 16rpx;
    display: block;
  }

  .quick-options {
    display: flex;
    flex-wrap: wrap;
    gap: 16rpx;

    .quick-option {
      padding: 16rpx 32rpx;
      background: #f5f6fa;
      border-radius: 12rpx;
      font-size: 28rpx;
      color: #666;
      border: 2rpx solid transparent;

      &.active {
        border-color: #5B8FF9;
        background: #f0f5ff;
        color: #5B8FF9;
      }
    }
  }
}

// 阈值滑块
.threshold-hint {
  font-size: 24rpx;
  color: #999;
  margin-top: 16rpx;
  display: block;
  text-align: center;
}

// 分类预算设置
.category-budget-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #f5f5f5;

  .cat-info {
    display: flex;
    align-items: center;
    gap: 16rpx;

    .cat-icon {
      font-size: 36rpx;
    }

    .cat-name {
      font-size: 28rpx;
      color: #333;
    }
  }

  .cat-input {
    display: flex;
    align-items: center;
    background: #f5f6fa;
    border-radius: 8rpx;
    padding: 12rpx 16rpx;

    .currency {
      font-size: 24rpx;
      color: #666;
      margin-right: 8rpx;
    }

    .cat-budget-input {
      width: 150rpx;
      font-size: 28rpx;
      color: #333;
      text-align: right;
      background: transparent;
    }
  }
}

// 预算汇总
.budget-summary {
  display: flex;
  justify-content: space-between;
  padding: 24rpx 0;
  margin-top: 20rpx;
  border-top: 2rpx solid #f0f0f0;

  .summary-text {
    font-size: 28rpx;
    color: #333;
  }

  .summary-remain {
    font-size: 28rpx;
    color: #52C41A;
    font-weight: 500;

    &.warning {
      color: #FF4D4F;
    }
  }
}
</style>