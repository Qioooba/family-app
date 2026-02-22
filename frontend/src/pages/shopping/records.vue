<template>
  <view class="records-page">
    <!-- 自定义导航栏 -->
    <view class="custom-nav">
      <view class="nav-back" @click="goBack">
        <text class="icon">‹</text>
      </view>
      <view class="nav-title">采购记录</view>
      <view class="nav-action" @click="showFilter">
        <text>{{ currentMonth }}</text>
        <text class="icon-small">▼</text>
      </view>
    </view>

    <!-- 统计卡片 -->
    <view class="stats-section">
      <view class="stat-card primary">
        <text class="stat-label">本月采购总额</text>
        <text class="stat-value">¥{{ monthlyTotal }}</text>
        <text class="stat-trend" :class="{ up: trend > 0 }">{{ trend > 0 ? '↑' : '↓' }} {{ Math.abs(trend) }}% 环比</text>
      </view>
      <view class="stats-row">
        <view class="stat-item">
          <text class="item-value">{{ recordCount }}</text>
          <text class="item-label">采购次数</text>
        </view>
        <view class="stat-item">
          <text class="item-value">{{ avgAmount }}</text>
          <text class="item-label">平均金额</text>
        </view>
        <view class="stat-item">
          <text class="item-value">{{ topCategory }}</text>
          <text class="item-label">主要品类</text>
        </view>
      </view>
    </view>

    <!-- 图表区域 -->
    <view class="chart-section">
      <view class="section-header">
        <text class="section-title">📊 采购趋势</text>
        <view class="chart-tabs">
          <text 
            v-for="tab in chartTabs" 
            :key="tab"
            class="tab-item"
            :class="{ active: currentTab === tab }"
            @click="currentTab = tab"
          >{{ tab }}</text>
        </view>
      </view>
      <view class="chart-container">
        <view v-if="currentTab === '金额'" class="amount-chart">
          <view 
            v-for="(item, index) in weeklyData" 
            :key="index"
            class="chart-bar"
          >
            <view class="bar-wrapper">
              <view 
                class="bar-fill" 
                :style="{ height: item.percent + '%' }"
                :class="{ highlight: item.isToday }"
              ></view>
            </view>
            <text class="bar-label">{{ item.day }}</text>
            <text v-if="item.amount > 0" class="bar-value">¥{{ item.amount }}</text>
          </view>
        </view>
        <view v-else class="freq-chart">
          <view 
            v-for="(item, index) in categoryData" 
            :key="index"
            class="freq-item"
          >
            <text class="freq-name">{{ item.name }}</text>
            <view class="freq-bar-wrapper">
              <view 
                class="freq-bar" 
                :style="{ width: item.percent + '%' }"
              ></view>
            </view>
            <text class="freq-count">{{ item.count }}次</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 记录列表 -->
    <view class="records-section">
      <view class="section-header">
        <text class="section-title">📝 采购明细</text>
        <text class="section-count">共 {{ filteredRecords.length }} 条</text>
      </view>
      
      <scroll-view class="records-list" scroll-y @scrolltolower="loadMore">
        <view 
          v-for="record in filteredRecords" 
          :key="record.id"
          class="record-card"
          @click="viewDetail(record)"
        >
          <view class="record-left">
            <view class="record-icon" :class="record.category">
              <text>{{ getCategoryIcon(record.category) }}</text>
            </view>
            <view class="record-info">
              <text class="record-name">{{ record.name }}</text>
              <text class="record-date">{{ record.date }} {{ record.time }}</text>
              <text class="record-buyer">👤 {{ record.buyer }}</text>
            </view>
          </view>
          <view class="record-right">
            <text class="record-amount">-¥{{ record.amount }}</text>
            <text class="record-category">{{ getCategoryName(record.category) }}</text>
          </view>
        </view>

        <view class="loading-more" v-if="loading">
          <text>加载中...</text>
        </view>
      </scroll-view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'

const loading = ref(false)
const currentMonth = ref('2025年2月')
const currentTab = ref('金额')
const trend = ref(-12.5)
const chartTabs = ['金额', '频次']

const weeklyData = ref([
  { day: '周一', amount: 156, percent: 45, isToday: false },
  { day: '周二', amount: 0, percent: 0, isToday: false },
  { day: '周三', amount: 238, percent: 70, isToday: false },
  { day: '周四', amount: 89, percent: 25, isToday: false },
  { day: '周五', amount: 345, percent: 100, isToday: true },
  { day: '周六', amount: 0, percent: 0, isToday: false },
  { day: '周日', amount: 0, percent: 0, isToday: false }
])

const categoryData = ref([
  { name: '食材', count: 12, percent: 80 },
  { name: '日用品', count: 5, percent: 50 },
  { name: '水果', count: 8, percent: 65 },
  { name: '零食', count: 3, percent: 30 },
  { name: '其他', count: 2, percent: 20 }
])

const records = ref([
  {
    id: 1,
    name: '超市大采购',
    amount: 345.50,
    category: 'food',
    date: '02-21',
    time: '14:30',
    buyer: '妈妈',
    items: ['猪肉', '青菜', '鸡蛋', '牛奶']
  },
  {
    id: 2,
    name: '水果店',
    amount: 89.00,
    category: 'fruit',
    date: '02-20',
    time: '10:15',
    buyer: '爸爸',
    items: ['苹果', '香蕉', '橙子']
  },
  {
    id: 3,
    name: '便利店',
    amount: 45.00,
    category: 'daily',
    date: '02-19',
    time: '19:20',
    buyer: '宝贝',
    items: ['饮料', '零食']
  },
  {
    id: 4,
    name: '菜市场',
    amount: 128.50,
    category: 'food',
    date: '02-18',
    time: '08:45',
    buyer: '妈妈',
    items: ['鱼', '虾', '蔬菜']
  },
  {
    id: 5,
    name: '网上购物',
    amount: 238.00,
    category: 'daily',
    date: '02-17',
    time: '21:00',
    buyer: '爸爸',
    items: ['洗衣液', '洗发水', '纸巾']
  }
])

const filteredRecords = computed(() => records.value)
const monthlyTotal = computed(() => records.value.reduce((sum, r) => sum + r.amount, 0).toFixed(2))
const recordCount = computed(() => records.value.length)
const avgAmount = computed(() => (monthlyTotal.value / recordCount.value || 0).toFixed(0))
const topCategory = computed(() => '食材')

const getCategoryIcon = (category) => {
  const icons = { food: '🥬', fruit: '🍎', daily: '🧻', snack: '🍪', other: '📦' }
  return icons[category] || '📦'
}

const getCategoryName = (category) => {
  const names = { food: '食材', fruit: '水果', daily: '日用品', snack: '零食', other: '其他' }
  return names[category] || '其他'
}

const goBack = () => {
  uni.navigateBack()
}

const showFilter = () => {
  uni.showActionSheet({
    itemList: ['2025年2月', '2025年1月', '2024年12月'],
    success: (res) => {
      currentMonth.value = ['2025年2月', '2025年1月', '2024年12月'][res.tapIndex]
    }
  })
}

const viewDetail = (record) => {
  uni.showModal({
    title: record.name,
    content: `金额: ¥${record.amount}\n购买人: ${record.buyer}\n物品: ${record.items.join(', ')}`,
    showCancel: false
  })
}

const loadMore = () => {
  loading.value = true
  setTimeout(() => {
    loading.value = false
  }, 500)
}
</script>

<style lang="scss" scoped>
.records-page {
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
    display: flex;
    align-items: center;
    gap: 4px;
    padding: 8px 12px;
    background: rgba(255,255,255,0.1);
    border-radius: 8px;
    font-size: 13px;
    color: #94a3b8;

    .icon-small {
      font-size: 10px;
    }
  }
}

.stats-section {
  padding: 15px;

  .stat-card {
    padding: 20px;
    border-radius: 16px;
    text-align: center;
    margin-bottom: 15px;

    &.primary {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    }

    .stat-label {
      display: block;
      font-size: 13px;
      color: rgba(255,255,255,0.7);
      margin-bottom: 8px;
    }

    .stat-value {
      display: block;
      font-size: 32px;
      font-weight: 700;
      color: #fff;
      margin-bottom: 8px;
    }

    .stat-trend {
      display: inline-block;
      padding: 4px 10px;
      background: rgba(255,255,255,0.2);
      border-radius: 10px;
      font-size: 12px;
      color: #22c55e;

      &.up {
        color: #ef4444;
      }
    }
  }

  .stats-row {
    display: flex;
    gap: 10px;

    .stat-item {
      flex: 1;
      padding: 15px;
      background: rgba(255,255,255,0.05);
      border-radius: 12px;
      text-align: center;

      .item-value {
        display: block;
        font-size: 20px;
        font-weight: 700;
        color: #fff;
        margin-bottom: 4px;
      }

      .item-label {
        font-size: 12px;
        color: #64748b;
      }
    }
  }
}

.chart-section {
  padding: 0 15px 15px;

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

    .chart-tabs {
      display: flex;
      gap: 8px;

      .tab-item {
        padding: 6px 12px;
        background: rgba(255,255,255,0.05);
        border-radius: 8px;
        font-size: 12px;
        color: #64748b;

        &.active {
          background: rgba(59,130,246,0.2);
          color: #3b82f6;
        }
      }
    }
  }

  .chart-container {
    padding: 20px;
    background: rgba(255,255,255,0.05);
    border-radius: 16px;

    .amount-chart {
      display: flex;
      justify-content: space-around;
      align-items: flex-end;
      height: 150px;

      .chart-bar {
        display: flex;
        flex-direction: column;
        align-items: center;
        flex: 1;

        .bar-wrapper {
          width: 24px;
          height: 120px;
          background: rgba(255,255,255,0.05);
          border-radius: 12px;
          overflow: hidden;
          position: relative;

          .bar-fill {
            position: absolute;
            bottom: 0;
            left: 0;
            right: 0;
            background: linear-gradient(180deg, #3b82f6 0%, #1d4ed8 100%);
            border-radius: 12px;
            transition: height 0.3s;

            &.highlight {
              background: linear-gradient(180deg, #22c55e 0%, #16a34a 100%);
            }
          }
        }

        .bar-label {
          margin-top: 8px;
          font-size: 11px;
          color: #64748b;
        }

        .bar-value {
          font-size: 10px;
          color: #94a3b8;
          margin-top: 4px;
        }
      }
    }

    .freq-chart {
      .freq-item {
        display: flex;
        align-items: center;
        gap: 12px;
        margin-bottom: 12px;

        .freq-name {
          width: 50px;
          font-size: 12px;
          color: #94a3b8;
        }

        .freq-bar-wrapper {
          flex: 1;
          height: 8px;
          background: rgba(255,255,255,0.05);
          border-radius: 4px;
          overflow: hidden;

          .freq-bar {
            height: 100%;
            background: linear-gradient(90deg, #8b5cf6 0%, #7c3aed 100%);
            border-radius: 4px;
            transition: width 0.3s;
          }
        }

        .freq-count {
          width: 40px;
          text-align: right;
          font-size: 12px;
          color: #64748b;
        }
      }
    }
  }
}

.records-section {
  padding: 0 15px;

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

    .section-count {
      font-size: 12px;
      color: #64748b;
    }
  }

  .records-list {
    padding-bottom: 30px;

    .record-card {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 16px;
      background: rgba(255,255,255,0.05);
      border-radius: 12px;
      margin-bottom: 10px;

      .record-left {
        display: flex;
        align-items: center;
        gap: 12px;

        .record-icon {
          width: 44px;
          height: 44px;
          border-radius: 12px;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 20px;
          flex-shrink: 0;

          &.food {
            background: rgba(34,197,94,0.2);
          }
          &.fruit {
            background: rgba(245,158,11,0.2);
          }
          &.daily {
            background: rgba(59,130,246,0.2);
          }
          &.snack {
            background: rgba(236,72,153,0.2);
          }
        }

        .record-info {
          .record-name {
            display: block;
            font-size: 15px;
            color: #fff;
            margin-bottom: 4px;
          }

          .record-date {
            font-size: 12px;
            color: #64748b;
            margin-right: 8px;
          }

          .record-buyer {
            font-size: 12px;
            color: #94a3b8;
          }
        }
      }

      .record-right {
        text-align: right;

        .record-amount {
          display: block;
          font-size: 16px;
          font-weight: 600;
          color: #ef4444;
          margin-bottom: 4px;
        }

        .record-category {
          font-size: 11px;
          color: #64748b;
          padding: 2px 8px;
          background: rgba(255,255,255,0.05);
          border-radius: 4px;
        }
      }
    }
  }
}

.loading-more {
  text-align: center;
  padding: 20px;
  color: #64748b;
  font-size: 13px;
}
</style>
