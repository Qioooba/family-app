<template>
  <view class="page-container">
    <view class="header">
      <view class="header-left" @click="goBack"><text>‹</text></view>
      <view class="header-title">心情记录</view>
    </view>
    
    <view class="today-mood"
>
      <text class="mood-title">今天的心情如何？</text>
      
      <view class="mood-options"
>
        <view 
          v-for="(mood, index) in moods" :key="mood.id || index" 
          
          class="mood-item"
          :class="{ selected: selectedMood === mood.value }"
          @click="selectMood(mood.value)"
        >
          <text class="mood-emoji">{{ mood.emoji }}</text>
          <text class="mood-label">{{ mood.label }}</text>
        </view>
      </view>
    </view>
    
    <view class="mood-note"
>
      <text class="note-label">记录一下</text>
      <textarea class="note-input" v-model="moodNote" placeholder="写下今天的心情..." />
    </view>
    
    <view class="save-btn" @click="saveMood"
>
      <text>保存记录</text>
    </view>
    
    <view class="history-section"
>
      <view class="section-title">历史记录</view>
      
      <view class="mood-chart"
>
        <view class="chart-placeholder">
          <text>📊 心情趋势图</text>
        </view>
      </view>
      
      <view class="history-list"
>
        <view v-for="(record, index) in moodHistory" :key="record.id || index"  class="history-item"
>
          <text class="history-emoji">{{ record.emoji }}</text>
          <view class="history-content"
>
            <text class="history-note">{{ record.note || record.label }}</text>
            <text class="history-date">{{ record.date }}</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'

const moods = [
  { value: 5, emoji: '😄', label: '超开心' },
  { value: 4, emoji: '😊', label: '开心' },
  { value: 3, emoji: '😐', label: '一般' },
  { value: 2, emoji: '😔', label: '难过' },
  { value: 1, emoji: '😢', label: '糟糕' }
]

const selectedMood = ref(null)
const moodNote = ref('')

const moodHistory = ref([
  { id: 1, emoji: '😊', label: '开心', note: '今天完成了很多任务', date: '今天 14:30' },
  { id: 2, emoji: '😐', label: '一般', note: '普通的一天', date: '昨天 20:00' },
  { id: 3, emoji: '😄', label: '超开心', note: '周末出去玩', date: '2天前' }
])

const selectMood = (value) => {
  selectedMood.value = value
}

const saveMood = () => {
  if (!selectedMood.value) {
    uni.showToast({ title: '请选择心情', icon: 'none' })
    return
  }
  uni.showToast({ title: '记录已保存', icon: 'success' })
}

const goBack = () => uni.navigateBack()
</script>

<style lang="scss" scoped>
.page-container { min-height: 100vh; background: #F8FAFC; }
.header {
  display: flex; justify-content: space-between; align-items: center;
  padding: 50px 20px 20px;
  background: linear-gradient(135deg, #EC4899, #DB2777);
  .header-left { width: 40px; height: 40px; display: flex; align-items: center; justify-content: center; }
  .header-left text { font-size: 32px; color: #fff; }
  .header-title { font-size: 20px; font-weight: 600; color: #fff; }
}
.today-mood { padding: 30px 20px; }
.mood-title { font-size: 18px; font-weight: 600; color: #1F2937; text-align: center; display: block; margin-bottom: 24px; }
.mood-options { display: flex; justify-content: space-around; }
.mood-item { display: flex; flex-direction: column; align-items: center; gap: 8px; padding: 16px 12px; border-radius: 16px; transition: all 0.3s; }
.mood-item.selected { background: #FCE7F3; transform: scale(1.1); }
.mood-emoji { font-size: 36px; }
.mood-label { font-size: 12px; color: #6B7280; }
.mood-item.selected .mood-label { color: #DB2777; font-weight: 600; }
.mood-note { padding: 0 20px 20px; }
.note-label { font-size: 14px; color: #6B7280; margin-bottom: 8px; display: block; }
.note-input { width: 100%; min-height: 100px; background: #fff; border-radius: 16px; padding: 16px; font-size: 15px; }
.save-btn { margin: 0 20px 20px; height: 50px; background: linear-gradient(135deg, #EC4899, #DB2777); border-radius: 25px; display: flex; align-items: center; justify-content: center; }
.save-btn text { font-size: 16px; font-weight: 600; color: #fff; }
.history-section { padding: 20px; }
.section-title { font-size: 16px; font-weight: 600; color: #1F2937; margin-bottom: 16px; display: block; }
.mood-chart { background: #fff; border-radius: 16px; padding: 20px; margin-bottom: 16px; }
.chart-placeholder { height: 120px; display: flex; align-items: center; justify-content: center; background: #F3F4F6; border-radius: 12px; }
.chart-placeholder text { font-size: 16px; color: #9CA3AF; }
.history-list { background: #fff; border-radius: 16px; padding: 8px; }
.history-item { display: flex; align-items: center; gap: 12px; padding: 14px; border-bottom: 1px solid #F3F4F6; }
.history-item:last-child { border-bottom: none; }
.history-emoji { font-size: 24px; }
.history-content { flex: 1; }
.history-note { font-size: 14px; color: #374151; display: block; margin-bottom: 2px; }
.history-date { font-size: 12px; color: #9CA3AF; }
</style>
