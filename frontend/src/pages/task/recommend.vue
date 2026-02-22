<template>
  <view class="page-container">
    <view class="header">
      <view class="header-left" @click="goBack"><text>‹</text></text>
      <view class="header-title">推荐任务</view>
    </view>
    
    <view class="recommend-section"
>
      <view class="section-header"
>
        <text class="section-title">为您推荐</text>
        <text class="section-subtitle">基于您的习惯智能推荐</text>
      </view>
      
      <view class="recommend-list"
>
        <view v-for="item in recommendations" :key="item.id" class="recommend-card"
>
          <view class="card-image" :style="{ background: item.color }">
            <text>{{ item.icon }}</text>
          </view>
          
          <view class="card-content"
>
            <text class="card-title">{{ item.title }}</text>
            <text class="card-reason">{{ item.reason }}</text>
            <view class="card-tags"
>
              <text v-for="tag in item.tags" :key="tag" class="tag">{{ tag }}</text>
            </view>
          </view>
          
          <view class="add-btn" @click="addTask(item)"
+ 添加</view>
        </view>
      </view>
    </view>
    
    <view class="history-section"
>
      <view class="section-title">📈 历史趋势</view>
      
      <view class="trend-chart"
>
        <view v-for="(bar, index) in trends" :key="index" class="trend-bar"
>
          <view class="bar-fill" :style="{ height: bar.value + '%', background: bar.color }"></text>
          <text class="bar-label">{{ bar.day }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'

const recommendations = ref([
  { id: 1, title: '晨跑5公里', icon: '🏃', color: '#10B981', reason: '您经常早晨运动', tags: ['健康', '运动'] },
  { id: 2, title: '读30分钟书', icon: '📚', color: '#8B5CF6', reason: '您的阅读习惯很好', tags: ['学习', '成长'] },
  { id: 3, title: '整理房间', icon: '🧹', color: '#F59E0B', reason: '建议每周清洁一次', tags: ['家务'] }
])

const trends = ref([
  { day: '一', value: 60, color: '#3B82F6' },
  { day: '二', value: 80, color: '#3B82F6' },
  { day: '三', value: 45, color: '#3B82F6' },
  { day: '四', value: 90, color: '#10B981' },
  { day: '五', value: 70, color: '#3B82F6' },
  { day: '六', value: 30, color: '#EF4444' },
  { day: '日', value: 40, color: '#EF4444' }
])

const addTask = (item) => {
  uni.showToast({ title: '已添加到任务', icon: 'success' })
}

const goBack = () => uni.navigateBack()
</script>

<style lang="scss" scoped>
.page-container { min-height: 100vh; background: #F8FAFC; }
.header {
  display: flex; justify-content: space-between; align-items: center;
  padding: 50px 20px 20px;
  background: linear-gradient(135deg, #8B5CF6, #7C3AED);
  .header-left { width: 40px; height: 40px; display: flex; align-items: center; justify-content: center; }
  .header-left text { font-size: 32px; color: #fff; }
  .header-title { font-size: 20px; font-weight: 600; color: #fff; }
}
.recommend-section { padding: 20px; }
.section-header { margin-bottom: 20px; }
.section-title { font-size: 18px; font-weight: 700; color: #1F2937; display: block; margin-bottom: 4px; }
.section-subtitle { font-size: 13px; color: #9CA3AF; }
.recommend-list { display: flex; flex-direction: column; gap: 12px; }
.recommend-card { display: flex; align-items: center; gap: 16px; background: #fff; border-radius: 16px; padding: 16px; }
.card-image { width: 60px; height: 60px; border-radius: 16px; display: flex; align-items: center; justify-content: center; font-size: 28px; }
.card-content { flex: 1; }
.card-title { font-size: 16px; font-weight: 600; color: #1F2937; display: block; margin-bottom: 4px; }
.card-reason { font-size: 13px; color: #9CA3AF; display: block; margin-bottom: 8px; }
.card-tags { display: flex; gap: 6px; }
.tag { font-size: 11px; padding: 4px 10px; background: #F3F4F6; color: #6B7280; border-radius: 10px; }
.add-btn { padding: 10px 20px; background: linear-gradient(135deg, #8B5CF6, #7C3AED); color: #fff; border-radius: 20px; font-size: 14px; font-weight: 500; }
.history-section { padding: 0 20px 20px; }
.trend-chart { display: flex; justify-content: space-around; align-items: flex-end; height: 150px; background: #fff; border-radius: 16px; padding: 20px; }
.trend-bar { display: flex; flex-direction: column; align-items: center; gap: 8px; }
.bar-fill { width: 24px; border-radius: 12px; transition: height 0.5s; }
.bar-label { font-size: 12px; color: #6B7280; }
</style>
