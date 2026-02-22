<template>
  <view class="account-page">
    <!-- 自定义导航栏 -->
    <u-navbar title="家庭账本" :autoBack="true" bgColor="#fff" :fixed="true" :placeholder="true"></u-navbar>
    
    <!-- 月度概览卡片 -->
    <view class="overview-card">
      <view class="overview-header">
        <view class="month-selector" @click="showMonthPicker">
          <text class="month-text">{{ currentMonth }}</text>
          <u-icon name="arrow-down" size="24" color="#fff"></u-icon>
        </view>
        <view class="budget-status" :class="{ warning: budgetProgress > 80 }">
          <text>{{ budgetProgress }}%</text>
        </view>
      </view>
      
      <view class="amount-row">
        <view class="amount-item">
          <text class="amount-label">本月支出</text>
          <text class="amount-value out">¥{{ totalExpense }}</text>
        </view>
        <view class="divider"></text>
        <view class="amount-item">
          <text class="amount-label">本月收入</text>
          <text class="amount-value in">¥{{ totalIncome }}</text>
        </view>
        <view class="divider"></text>
        <view class="amount-item">
          <text class="amount-label">结余</text>
          <text class="amount-value" :class="balance >= 0 ? 'in' : 'out'">¥{{ Math.abs(balance) }}</text>
        </view>
      </view>
      
      <!-- 预算进度 -->
      <view class="budget-bar">
        <view class="budget-info">
          <text>月度预算：¥{{ monthlyBudget }}</text>
          <text>剩余：¥{{ budgetRemain }}</text>
        </view>
        <view class="progress-bg">
          <view 
            class="progress-fill" 
            :style="{ width: budgetProgress + '%' }"
            :class="{ warning: budgetProgress > 80 }"
          ></text>
        </view>
      </view>
    </view>
    
    <!-- 快速记账按钮 -->
    <view class="quick-actions">
      <view class="action-btn expense" @click="openRecord('expense')">
        <u-icon name="minus-circle" size="40" color="#fff"></u-icon>
        <text>记支出</text>
      </view>
      <view class="action-btn income" @click="openRecord('income')">
        <u-icon name="plus-circle" size="40" color="#fff"></u-icon>
        <text>记收入</text>
      </view>
      <view class="action-btn stats" @click="showStats">
        <u-icon name="chart" size="40" color="#fff"></u-icon>
        <text>统计</text>
      </view>
    </view>
    
    <!-- 分类图表 -->
    <view class="chart-section" v-if="showChart">
      <view class="section-header">
        <text class="section-title">支出分析</text>
        <view class="chart-tabs">
          <text 
            class="tab-item" 
            :class="{ active: chartType === 'pie' }"
            @click="chartType = 'pie'"
          >饼图</text>
          <text 
            class="tab-item" 
            :class="{ active: chartType === 'bar' }"
            @click="chartType = 'bar'"
          >趋势</text>
        </view>
      </view>
      <view class="chart-container">
        <qiun-data-charts 
          type="pie" 
          :opts="pieOpts" 
          :chartData="chartData"
          v-if="chartType === 'pie'"
        />
        <qiun-data-charts 
          type="column" 
          :opts="barOpts" 
          :chartData="trendData"
          v-else
        />
      </view>
    </view>
    
    <!-- 筛选栏 -->
    <view class="filter-bar">
      <view class="filter-item" @click="showTypeFilter">
        <text :class="{ active: filterType }">{{ filterType || '全部类型' }}</text>
        <u-icon name="arrow-down" size="20" :color="filterType ? '#667eea' : '#999'"></u-icon>
      </view>
      <view class="filter-item" @click="showCategoryFilter">
        <text :class="{ active: filterCategory }">{{ filterCategory || '全部分类' }}</text>
        <u-icon name="arrow-down" size="20" :color="filterCategory ? '#667eea' : '#999'"></u-icon>
      </view>
      <view class="filter-item" @click="showDateFilter">
        <text :class="{ active: filterDate }">{{ filterDate || '全部时间' }}</text>
        <u-icon name="arrow-down" size="20" :color="filterDate ? '#667eea' : '#999'"></u-icon>
      </view>
    </view>
    
    <!-- 账单列表 -->
    <scroll-view 
      scroll-y 
      class="record-list"
      @scrolltolower="loadMore"
    >
      <view v-for="(group, date) in groupedRecords" :key="date" class="record-group">
        <view class="group-header">
          <text class="group-date">{{ formatGroupDate(date) }}</text>
          <view class="group-sum">
            <text class="sum-label">支</text>
            <text class="sum-value out">¥{{ group.expense }}</text>
            <text class="sum-label" style="margin-left: 20rpx;">收</text>
            <text class="sum-value in">¥{{ group.income }}</text>
          </view>
        </view>
        
        <view 
          v-for="record in group.items" 
          :key="record.id"
          class="record-item"
          @click="editRecord(record)"
          @longpress="deleteRecord(record)"
        >
          <view class="record-icon" :style="{ background: record.color }">
            <u-icon :name="record.icon" size="36" color="#fff"></u-icon>
          </view>
          <view class="record-info">
            <text class="record-category">{{ record.category }}</text>
            <text class="record-note" v-if="record.note">{{ record.note }}</text>
          </view>
          <view class="record-amount">
            <text :class="record.type">{{ record.type === 'income' ? '+' : '-' }}¥{{ record.amount }}</text>
            <text class="record-time">{{ record.time }}</text>
          </view>
        </view>
      </view>
      
      <u-loadmore :status="loadStatus" />
    </scroll-view>
    
    <!-- 记帐弹窗 -->
    <u-popup :show="showRecordPopup" mode="bottom" @close="closeRecordPopup" round="20">
      <view class="record-popup">
        <view class="popup-header">
          <text class="popup-title">{{ recordForm.type === 'income' ? '记收入' : '记支出' }}</text>
          <u-icon name="close" size="32" color="#999" @click="closeRecordPopup"></u-icon>
        </view>
        
        <view class="amount-input">
          <text class="currency">¥</text>
          <input 
            type="digit" 
            v-model="recordForm.amount" 
            placeholder="0.00"
            class="amount-field"
            focus
          />
        </view>
        
        <view class="type-tabs">
          <view 
            class="type-tab" 
            :class="{ active: recordForm.type === 'expense' }"
            @click="recordForm.type = 'expense'"
          >支出</view>
          <view 
            class="type-tab" 
            :class="{ active: recordForm.type === 'income' }"
            @click="recordForm.type = 'income'"
          >收入</view>
        </view>
        
        <view class="category-grid">
          <view 
            v-for="cat in currentCategories" 
            :key="cat.name"
            class="category-item"
            :class="{ active: recordForm.category === cat.name }"
            @click="selectCategory(cat)"
          >
            <view class="cat-icon" :style="{ background: cat.color }">
              <u-icon :name="cat.icon" size="32" color="#fff"></u-icon>
            </view>
            <text>{{ cat.name }}</text>
          </view>
        </view>
        
        <view class="form-fields">
          <u-input
            v-model="recordForm.note"
            placeholder="添加备注（可选）"
            border="surround"
            clearable
          ></u-input>
          <u-datetime-picker
            :show="showDatePicker"
            v-model="recordForm.datetime"
            mode="datetime"
            @confirm="confirmDate"
            @cancel="showDatePicker = false"
          ></u-datetime-picker>
          <view class="date-field" @click="showDatePicker = true">
            <u-icon name="calendar" size="28" color="#999"></u-icon>
            <text>{{ formatDateTime(recordForm.datetime) }}</text>
          </view>
        </view>
        
        <view class="popup-actions">
          <u-button type="primary" text="保存" @click="saveRecord" :loading="saving"></u-button>
        </view>
      </view>
    </u-popup>
    
    <!-- 预算设置弹窗 -->
    <u-popup :show="showBudgetPopup" mode="center" @close="showBudgetPopup = false" round="20">
      <view class="budget-popup">
        <view class="popup-title">设置月度预算</view>
        <input 
          type="digit" 
          v-model="tempBudget" 
          placeholder="请输入预算金额"
          class="budget-input"
        />
        <view class="popup-actions">
          <u-button text="取消" @click="showBudgetPopup = false"></u-button>
          <u-button type="primary" text="确定" @click="saveBudget"></u-button>
        </view>
      </view>
    </u-popup>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'

// 数据
const currentMonth = ref('2024年2月')
const monthlyBudget = ref(10000)
const totalExpense = ref(3850.50)
const totalIncome = ref(15000)
const showChart = ref(true)
const chartType = ref('pie')
const showRecordPopup = ref(false)
const showBudgetPopup = ref(false)
const showDatePicker = ref(false)
const saving = ref(false)
const loadStatus = ref('loadmore')

// 筛选
const filterType = ref('')
const filterCategory = ref('')
const filterDate = ref('')

// 表单
const recordForm = ref({
  type: 'expense',
  amount: '',
  category: '',
  note: '',
  datetime: Date.now()
})
const tempBudget = ref('')

// 分类配置
const expenseCategories = [
  { name: '餐饮', icon: 'food', color: '#FF6B6B' },
  { name: '交通', icon: 'car', color: '#4ECDC4' },
  { name: '购物', icon: 'shopping-cart', color: '#45B7D1' },
  { name: '娱乐', icon: 'movie', color: '#96CEB4' },
  { name: '居住', icon: 'home', color: '#FFEAA7' },
  { name: '医疗', icon: 'heart', color: '#DFE6E9' },
  { name: '教育', icon: 'book', color: '#A29BFE' },
  { name: '其他', icon: 'more-circle', color: '#B2BEC3' }
]

const incomeCategories = [
  { name: '工资', icon: 'rmb-circle', color: '#52C41A' },
  { name: '奖金', icon: 'gift', color: '#FAAD14' },
  { name: '投资', icon: 'trending-up', color: '#722ED1' },
  { name: '兼职', icon: 'briefcase', color: '#13C2C2' },
  { name: '其他', icon: 'more-circle', color: '#B2BEC3' }
]

const currentCategories = computed(() => {
  return recordForm.value.type === 'income' ? incomeCategories : expenseCategories
})

// 计算属性
const balance = computed(() => totalIncome.value - totalExpense.value)
const budgetRemain = computed(() => Math.max(0, monthlyBudget.value - totalExpense.value))
const budgetProgress = computed(() => {
  return Math.min(100, Math.round((totalExpense.value / monthlyBudget.value) * 100))
})

// 图表数据
const chartData = ref({
  series: [
    { data: [{ name: '餐饮', value: 1200 }, { name: '购物', value: 850 }, { name: '交通', value: 600 }, { name: '娱乐', value: 500 }, { name: '居住', value: 400 }, { name: '其他', value: 300 }] }
  ]
})

const trendData = ref({
  categories: ['2/15', '2/16', '2/17', '2/18', '2/19', '2/20'],
  series: [{ name: '支出', data: [120, 350, 80, 200, 450, 180] }]
})

const pieOpts = {
  color: ['#FF6B6B', '#4ECDC4', '#45B7D1', '#96CEB4', '#FFEAA7', '#DFE6E9'],
  dataLabel: true,
  legend: { show: true, position: 'bottom' }
}

const barOpts = {
  xAxis: { disableGrid: true },
  yAxis: { min: 0 },
  extra: { column: { type: 'group', width: 20 } }
}

// 账单记录
const records = ref([
  { id: 1, type: 'expense', amount: '158.00', category: '餐饮', icon: 'food', color: '#FF6B6B', note: '家庭聚餐', date: '2024-02-20', time: '18:30' },
  { id: 2, type: 'expense', amount: '88.00', category: '购物', icon: 'shopping-cart', color: '#45B7D1', note: '日用品', date: '2024-02-20', time: '14:20' },
  { id: 3, type: 'expense', amount: '35.00', category: '交通', icon: 'car', color: '#4ECDC4', note: '', date: '2024-02-20', time: '08:15' },
  { id: 4, type: 'income', amount: '15000.00', category: '工资', icon: 'rmb-circle', color: '#52C41A', note: '2月工资', date: '2024-02-19', time: '09:00' },
  { id: 5, type: 'expense', amount: '268.00', category: '娱乐', icon: 'movie', color: '#96CEB4', note: '电影票', date: '2024-02-19', time: '19:30' },
  { id: 6, type: 'expense', amount: '1280.00', category: '购物', icon: 'shopping-cart', color: '#45B7D1', note: '换季衣服', date: '2024-02-18', time: '15:00' },
  { id: 7, type: 'expense', amount: '450.00', category: '居住', icon: 'home', color: '#FFEAA7', note: '水电费', date: '2024-02-18', time: '10:00' },
  { id: 8, type: 'expense', amount: '180.00', category: '医疗', icon: 'heart', color: '#DFE6E9', note: '感冒药', date: '2024-02-17', time: '16:45' }
])

const groupedRecords = computed(() => {
  const groups = {}
  records.value.forEach(record => {
    if (!groups[record.date]) {
      groups[record.date] = { items: [], income: 0, expense: 0 }
    }
    groups[record.date].items.push(record)
    if (record.type === 'income') {
      groups[record.date].income += parseFloat(record.amount)
    } else {
      groups[record.date].expense += parseFloat(record.amount)
    }
  })
  return groups
})

// 方法
const showMonthPicker = () => {
  // 月份选择
}

const showStats = () => {
  showChart.value = !showChart.value
}

const openRecord = (type) => {
  recordForm.value = {
    type,
    amount: '',
    category: '',
    note: '',
    datetime: Date.now()
  }
  showRecordPopup.value = true
}

const closeRecordPopup = () => {
  showRecordPopup.value = false
}

const selectCategory = (cat) => {
  recordForm.value.category = cat.name
}

const formatDateTime = (timestamp) => {
  const d = new Date(timestamp)
  return `${d.getMonth() + 1}月${d.getDate()}日 ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

const confirmDate = (e) => {
  recordForm.value.datetime = e.value
  showDatePicker.value = false
}

const saveRecord = () => {
  if (!recordForm.value.amount || !recordForm.value.category) {
    uni.showToast({ title: '请填写完整信息', icon: 'none' })
    return
  }
  
  saving.value = true
  const cat = recordForm.value.type === 'income' 
    ? incomeCategories.find(c => c.name === recordForm.value.category)
    : expenseCategories.find(c => c.name === recordForm.value.category)
  
  setTimeout(() => {
    const d = new Date(recordForm.value.datetime)
    const newRecord = {
      id: Date.now(),
      type: recordForm.value.type,
      amount: recordForm.value.amount,
      category: recordForm.value.category,
      icon: cat.icon,
      color: cat.color,
      note: recordForm.value.note,
      date: d.toISOString().split('T')[0],
      time: `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
    }
    records.value.unshift(newRecord)
    
    if (recordForm.value.type === 'income') {
      totalIncome.value += parseFloat(recordForm.value.amount)
    } else {
      totalExpense.value += parseFloat(recordForm.value.amount)
    }
    
    saving.value = false
    closeRecordPopup()
    uni.showToast({ title: '记账成功', icon: 'success' })
  }, 500)
}

const formatGroupDate = (date) => {
  const today = new Date().toISOString().split('T')[0]
  if (date === today) return '今天'
  const d = new Date(date)
  return `${d.getMonth() + 1}月${d.getDate()}日`
}

const editRecord = (record) => {
  uni.showActionSheet({
    itemList: ['编辑', '删除'],
    success: (res) => {
      if (res.tapIndex === 1) deleteRecord(record)
    }
  })
}

const deleteRecord = (record) => {
  uni.showModal({
    title: '确认删除',
    content: '确定删除这条记录吗？',
    confirmColor: '#FF4D4F',
    success: (res) => {
      if (res.confirm) {
        const idx = records.value.findIndex(r => r.id === record.id)
        if (idx > -1) {
          records.value.splice(idx, 1)
          if (record.type === 'income') {
            totalIncome.value -= parseFloat(record.amount)
          } else {
            totalExpense.value -= parseFloat(record.amount)
          }
          uni.showToast({ title: '已删除', icon: 'success' })
        }
      }
    }
  })
}

const loadMore = () => {
  loadStatus.value = 'loading'
  setTimeout(() => {
    loadStatus.value = 'nomore'
  }, 500)
}

const showTypeFilter = () => {
  uni.showActionSheet({
    itemList: ['全部', '支出', '收入'],
    success: (res) => {
      const types = ['', 'expense', 'income']
      filterType.value = types[res.tapIndex] || ''
    }
  })
}

const showCategoryFilter = () => {
  const cats = ['全部', ...expenseCategories.map(c => c.name), ...incomeCategories.map(c => c.name)]
  uni.showActionSheet({
    itemList: cats,
    success: (res) => {
      filterCategory.value = cats[res.tapIndex] === '全部' ? '' : cats[res.tapIndex]
    }
  })
}

const showDateFilter = () => {
  uni.showActionSheet({
    itemList: ['全部时间', '今天', '本周', '本月'],
    success: (res) => {
      const dates = ['', 'today', 'week', 'month']
      filterDate.value = dates[res.tapIndex] || ''
    }
  })
}

const saveBudget = () => {
  if (tempBudget.value) {
    monthlyBudget.value = parseFloat(tempBudget.value)
    showBudgetPopup.value = false
    uni.showToast({ title: '设置成功', icon: 'success' })
  }
}
</script>

<style lang="scss" scoped>
.account-page {
  min-height: 100vh;
  background: #f5f6fa;
}

.overview-card {
  margin: 20rpx 30rpx;
  padding: 30rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 24rpx;
  color: #fff;
  
  .overview-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 30rpx;
    
    .month-selector {
      display: flex;
      align-items: center;
      
      .month-text {
        font-size: 32rpx;
        font-weight: 600;
        margin-right: 8rpx;
      }
    }
    
    .budget-status {
      padding: 8rpx 20rpx;
      background: rgba(255,255,255,0.2);
      border-radius: 30rpx;
      font-size: 26rpx;
      
      &.warning {
        background: #FF4D4F;
      }
    }
  }
  
  .amount-row {
    display: flex;
    justify-content: space-around;
    align-items: center;
    margin-bottom: 30rpx;
    
    .divider {
      width: 2rpx;
      height: 60rpx;
      background: rgba(255,255,255,0.3);
    }
    
    .amount-item {
      text-align: center;
      
      .amount-label {
        display: block;
        font-size: 24rpx;
        color: rgba(255,255,255,0.8);
        margin-bottom: 8rpx;
      }
      
      .amount-value {
        font-size: 40rpx;
        font-weight: bold;
        
        &.out { color: #FFCDD2; }
        &.in { color: #C8E6C9; }
      }
    }
  }
  
  .budget-bar {
    .budget-info {
      display: flex;
      justify-content: space-between;
      font-size: 24rpx;
      color: rgba(255,255,255,0.8);
      margin-bottom: 12rpx;
    }
    
    .progress-bg {
      height: 12rpx;
      background: rgba(255,255,255,0.2);
      border-radius: 6rpx;
      overflow: hidden;
      
      .progress-fill {
        height: 100%;
        background: #52C41A;
        border-radius: 6rpx;
        transition: width 0.3s;
        
        &.warning {
          background: #FF4D4F;
        }
      }
    }
  }
}

.quick-actions {
  display: flex;
  justify-content: space-around;
  padding: 20rpx 30rpx;
  background: #fff;
  margin: 0 30rpx 20rpx;
  border-radius: 20rpx;
  
  .action-btn {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 20rpx 40rpx;
    border-radius: 16rpx;
    
    &.expense { background: linear-gradient(135deg, #FF6B6B 0%, #EE5A6F 100%); }
    &.income { background: linear-gradient(135deg, #52C41A 0%, #73D13D 100%); }
    &.stats { background: linear-gradient(135deg, #45B7D1 0%, #4ECDC4 100%); }
    
    text {
      font-size: 24rpx;
      color: #fff;
      margin-top: 8rpx;
    }
  }
}

.chart-section {
  background: #fff;
  margin: 0 30rpx 20rpx;
  padding: 30rpx;
  border-radius: 20rpx;
  
  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20rpx;
    
    .section-title {
      font-size: 30rpx;
      font-weight: 600;
      color: #333;
    }
    
    .chart-tabs {
      display: flex;
      background: #f5f6fa;
      border-radius: 24rpx;
      padding: 4rpx;
      
      .tab-item {
        padding: 12rpx 30rpx;
        font-size: 26rpx;
        color: #666;
        border-radius: 20rpx;
        
        &.active {
          background: #667eea;
          color: #fff;
        }
      }
    }
  }
  
  .chart-container {
    height: 400rpx;
  }
}

.filter-bar {
  display: flex;
  justify-content: space-around;
  padding: 20rpx 30rpx;
  background: #fff;
  margin: 0 30rpx;
  border-radius: 20rpx 20rpx 0 0;
  border-bottom: 2rpx solid #f5f5f5;
  
  .filter-item {
    display: flex;
    align-items: center;
    
    text {
      font-size: 28rpx;
      color: #666;
      margin-right: 8rpx;
      
      &.active {
        color: #667eea;
        font-weight: 500;
      }
    }
  }
}

.record-list {
  height: calc(100vh - 800rpx);
  padding: 0 30rpx 30rpx;
  
  .record-group {
    background: #fff;
    margin-bottom: 20rpx;
    border-radius: 16rpx;
    overflow: hidden;
    
    .group-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 20rpx 30rpx;
      background: #f9f9f9;
      
      .group-date {
        font-size: 28rpx;
        font-weight: 500;
        color: #333;
      }
      
      .group-sum {
        display: flex;
        align-items: center;
        
        .sum-label {
          font-size: 22rpx;
          color: #999;
          margin-right: 8rpx;
        }
        
        .sum-value {
          font-size: 24rpx;
          font-weight: 500;
          
          &.out { color: #FF6B6B; }
          &.in { color: #52C41A; }
        }
      }
    }
    
    .record-item {
      display: flex;
      align-items: center;
      padding: 24rpx 30rpx;
      border-bottom: 2rpx solid #f5f5f5;
      
      &:last-child {
        border-bottom: none;
      }
      
      .record-icon {
        width: 80rpx;
        height: 80rpx;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-right: 20rpx;
      }
      
      .record-info {
        flex: 1;
        
        .record-category {
          display: block;
          font-size: 30rpx;
          color: #333;
          margin-bottom: 8rpx;
        }
        
        .record-note {
          font-size: 24rpx;
          color: #999;
        }
      }
      
      .record-amount {
        text-align: right;
        
        text {
          display: block;
          font-size: 32rpx;
          font-weight: 600;
          
          &.income { color: #52C41A; }
          &.expense { color: #FF6B6B; }
        }
        
        .record-time {
          font-size: 22rpx;
          color: #999;
          margin-top: 4rpx;
        }
      }
    }
  }
}

.record-popup {
  background: #fff;
  padding: 30rpx;
  padding-bottom: calc(30rpx + env(safe-area-inset-bottom));
  
  .popup-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 30rpx;
    
    .popup-title {
      font-size: 32rpx;
      font-weight: 600;
      color: #333;
    }
  }
  
  .amount-input {
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 40rpx;
    
    .currency {
      font-size: 48rpx;
      color: #333;
      margin-right: 16rpx;
    }
    
    .amount-field {
      font-size: 72rpx;
      color: #333;
      width: 400rpx;
      text-align: center;
    }
  }
  
  .type-tabs {
    display: flex;
    justify-content: center;
    margin-bottom: 30rpx;
    
    .type-tab {
      padding: 16rpx 60rpx;
      font-size: 30rpx;
      color: #666;
      border-bottom: 4rpx solid transparent;
      
      &.active {
        color: #667eea;
        border-bottom-color: #667eea;
      }
    }
  }
  
  .category-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 20rpx;
    margin-bottom: 30rpx;
    
    .category-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 20rpx;
      border-radius: 16rpx;
      
      &.active {
        background: #f0f5ff;
      }
      
      .cat-icon {
        width: 80rpx;
        height: 80rpx;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-bottom: 12rpx;
      }
      
      text {
        font-size: 24rpx;
        color: #666;
      }
    }
  }
  
  .form-fields {
    margin-bottom: 30rpx;
    
    .date-field {
      display: flex;
      align-items: center;
      padding: 20rpx 0;
      margin-top: 20rpx;
      
      text {
        font-size: 28rpx;
        color: #666;
        margin-left: 16rpx;
      }
    }
  }
  
  .popup-actions {
    padding-top: 20rpx;
  }
}

.budget-popup {
  width: 560rpx;
  padding: 40rpx;
  
  .popup-title {
    font-size: 32rpx;
    font-weight: 600;
    text-align: center;
    margin-bottom: 30rpx;
  }
  
  .budget-input {
    width: 100%;
    height: 100rpx;
    background: #f5f6fa;
    border-radius: 16rpx;
    padding: 0 30rpx;
    font-size: 36rpx;
    text-align: center;
    margin-bottom: 30rpx;
  }
  
  .popup-actions {
    display: flex;
    gap: 20rpx;
  }
}
</style>