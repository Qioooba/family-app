<template>
  <view class="page-container">
    <view class="header">
      <view class="header-left" @click="goBack"><text>‹</text></text>
      <view class="header-title">数据统计</view>
    </view>
    
    <view class="stats-overview"
>
      <view class="overview-card"
>
        <text class="card-value">{{ stats.total }}</text>
        <text class="card-label">总任务</text>
      </view>
      <view class="overview-card"
>
        <text class="card-value">{{ stats.completed }}</text>
        <text class="card-label">已完成</text>
      </view>
      <view class="overview-card"
>
        <text class="card-value">{{ stats.rate }}%</text>
        <text class="card-label">完成率</text>
      </view>
    </view>
    
    <view class="charts-section"
>
      <view class="section-title">任务趋势</view>
      <view class="chart-placeholder"
>
        <text>📈 图表区域</text>
      </view>
    </view>
    
    <view class="distribution-section"
>
      <view class="section-title">分类分布</view>
      
      <view class="distribution-list"
>
        <view v-for="item in distribution" :key="item.name" class="dist-item"
>
          <text class="dist-name">{{ item.name }}</text>
          <view class="dist-bar">
            <view class="dist-fill" :style="{ width: item.percent + '%', background: item.color }"></text>
          </view>
          <text class="dist-value">{{ item.value }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'

const stats = ref({ total: 128, completed: 96, rate: 75 })

const distribution = ref([
  { name: '家务', value: 45, percent: 35, color: '#10B981' },
  { name: '工作', value: 38, percent: 30, color: '#3B82F6' },
  { name: '购物', value: 25, percent: 20, color: '#F59E0B' },
  { name: '其他', value: 20, percent: 15, color: '#8B5CF6' }
])

const goBack = () => uni.navigateBack()
</script>

<style lang="scss" scoped>
.page-container { min-height: 100vh; background: #F8FAFC; }
.header {
  display: flex; justify-content: space-between; align-items: center;
  padding: 50px 20px 20px;
  background: linear-gradient(135deg, #3B82F6, #2563EB);
  .header-left { width: 40px; height: 40px; display: flex; align-items: center; justify-content: center; }
  .header-left text { font-size: 32px; color: #fff; }
  .header-title { font-size: 20px; font-weight: 600; color: #fff; }
}
.stats-overview { display: flex; gap: 12px; padding: 15px; }
.overview-card { flex: 1; background: #fff; border-radius: 16px; padding: 20px; text-align: center; }
.card-value { font-size: 28px; font-weight: 700; color: #3B82F6; display: block; margin-bottom: 4px; }
.card-label { font-size: 13px; color: #9CA3AF; }
.charts-section { padding: 0 15px 15px; }
.section-title { font-size: 16px; font-weight: 600; color: #1F2937; margin-bottom: 12px; display: block; }
.chart-placeholder { background: #fff; border-radius: 16px; padding: 60px; text-align: center; }
.chart-placeholder text { font-size: 16px; color: #9CA3AF; }
.distribution-section { padding: 0 15px; }
.distribution-list { background: #fff; border-radius: 16px; padding: 16px; }
.dist-item { display: flex; align-items: center; gap: 12px; padding: 12px 0; border-bottom: 1px solid #F3F4F6; }
.dist-item:last-child { border-bottom: none; }
.dist-name { width: 60px; font-size: 14px; color: #374151; }
.dist-bar { flex: 1; height: 8px; background: #F3F4F6; border-radius: 4px; overflow: hidden; }
.dist-fill { height: 100%; border-radius: 4px; transition: width 0.5s; }
.dist-value { width: 40px; text-align: right; font-size: 14px; color: #6B7280; }
</style>
