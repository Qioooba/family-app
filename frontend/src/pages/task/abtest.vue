<template>
  <view class="page-container">
    <view class="header">
      <view class="header-left" @click="goBack"><text>‹</text></view>
      <view class="header-title">A/B测试</view>
    </view>
    
    <view class="ab-section"
>
      <view class="section-title">当前测试</view>
      
      <view v-for="test in abTests" :key="test.id" class="test-card"
>
        <view class="test-header"
>
          <text class="test-name">{{ test.name }}</text>
          <view class="test-status" :class="test.status">{{ test.statusText }}</view>
        </view>
        
        <view class="test-variants"
>
          <view v-for="(v, i) in test.variants" :key="i" class="variant-bar"
>
            <text class="variant-label">{{ v.name }}</text>
            <view class="variant-progress"
>
              <view class="variant-fill" :style="{ width: v.percent + '%', background: v.color }"></view>
            </view>
            <text class="variant-value">{{ v.percent }}%</text>
          </view>
        </view>
        
        <view class="test-stats"
>
          <view class="stat"
>
            <text class="stat-value">{{ test.users }}</text>
            <text class="stat-label">参与用户</text>
          </view>
          <view class="stat"
>
            <text class="stat-value">{{ test.conversion }}%</text>
            <text class="stat-label">转化率</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'

const abTests = ref([
  {
    id: 1,
    name: '首页布局测试',
    status: 'running',
    statusText: '进行中',
    variants: [
      { name: '方案A-列表', percent: 52, color: '#3B82F6' },
      { name: '方案B-卡片', percent: 48, color: '#10B981' }
    ],
    users: 1250,
    conversion: 24.5
  },
  {
    id: 2,
    name: '按钮颜色测试',
    status: 'completed',
    statusText: '已完成',
    variants: [
      { name: '蓝色', percent: 45, color: '#3B82F6' },
      { name: '绿色', percent: 55, color: '#10B981' }
    ],
    users: 800,
    conversion: 28.3
  }
])

const goBack = () => uni.navigateBack()
</script>

<style lang="scss" scoped>
.page-container { min-height: 100vh; background: #F8FAFC; }
.header {
  display: flex; justify-content: space-between; align-items: center;
  padding: 50px 20px 20px;
  background: linear-gradient(135deg, #F59E0B, #D97706);
  .header-left { width: 40px; height: 40px; display: flex; align-items: center; justify-content: center; }
  .header-left text { font-size: 32px; color: #fff; }
  .header-title { font-size: 20px; font-weight: 600; color: #fff; }
}
.ab-section { padding: 20px; }
.section-title { font-size: 16px; font-weight: 600; color: #1F2937; margin-bottom: 16px; display: block; }
.test-card { background: #fff; border-radius: 16px; padding: 20px; margin-bottom: 16px; }
.test-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.test-name { font-size: 16px; font-weight: 600; color: #1F2937; }
.test-status { font-size: 12px; padding: 4px 12px; border-radius: 12px; }
.test-status.running { background: #DBEAFE; color: #2563EB; }
.test-status.completed { background: #D1FAE5; color: #059669; }
.test-variants { margin-bottom: 16px; }
.variant-bar { display: flex; align-items: center; gap: 12px; margin-bottom: 12px; }
.variant-label { width: 100px; font-size: 13px; color: #6B7280; }
.variant-progress { flex: 1; height: 8px; background: #F3F4F6; border-radius: 4px; overflow: hidden; }
.variant-fill { height: 100%; border-radius: 4px; transition: width 0.5s; }
.variant-value { width: 50px; font-size: 13px; color: #374151; text-align: right; font-weight: 600; }
.test-stats { display: flex; gap: 40px; padding-top: 16px; border-top: 1px solid #F3F4F6; }
.stat { text-align: center; }
.stat-value { font-size: 20px; font-weight: 700; color: #1F2937; display: block; margin-bottom: 4px; }
.stat-label { font-size: 12px; color: #9CA3AF; }
</style>
