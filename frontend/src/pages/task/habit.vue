<template>
  <view class="page-container">
    <view class="header">
      <view class="header-left" @click="goBack"><text>‹</text></view>
      <view class="header-title">习惯打卡</view>
    </view>
    
    <view class="habit-stats"
>
      <view class="stat-item">
        <text class="stat-num">{{ stats.total }}</text>
        <text class="stat-label">习惯数</text>
      </view>
      <view class="stat-item">
        <text class="stat-num">{{ stats.completed }}</text>
        <text class="stat-label">今日完成</text>
      </view>
      <view class="stat-item">
        <text class="stat-num">{{ stats.streak }}</text>
        <text class="stat-label">连续天数</text>
      </view>
    </view>
    
    <view class="habits-list"
>
      <view v-for="habit in habits" :key="habit.id" class="habit-card"
>
        <view class="habit-icon" :style="{ background: habit.color }">{{ habit.emoji }}</view>
        
        <view class="habit-info"
>
          <text class="habit-name">{{ habit.name }}</text>
          <view class="habit-progress"
>
            <view class="progress-dots"
>
              <view v-for="n in 7" :key="n" class="dot" :class="{ active: n <= habit.weekProgress }"></view>
            </view>
            <text class="streak-text">连续{{ habit.streak }}天</text>
          </view>
        </view>
        
        <view class="check-btn" :class="{ checked: habit.todayDone }" @click="toggleHabit(habit)"
>
          <text v-if="!habit.todayDone">✓</text>
          <text v-else>✓</text>
        </view>
      </view>
    </view>
    
    <view class="add-btn" @click="addHabit">
      <text>+ 新建习惯</text>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'

const habits = ref([
  { id: 1, name: '早起', emoji: '🌅', color: '#F59E0B', weekProgress: 5, streak: 12, todayDone: true },
  { id: 2, name: '喝水', emoji: '💧', color: '#3B82F6', weekProgress: 6, streak: 8, todayDone: true },
  { id: 3, name: '运动', emoji: '🏃', color: '#10B981', weekProgress: 3, streak: 3, todayDone: false },
  { id: 4, name: '阅读', emoji: '📚', color: '#8B5CF6', weekProgress: 4, streak: 15, todayDone: false }
])

const stats = computed(() => ({
  total: habits.value.length,
  completed: habits.value.filter(h => h.todayDone).length,
  streak: Math.max(...habits.value.map(h => h.streak))
}))

const toggleHabit = (habit) => {
  habit.todayDone = !habit.todayDone
  if (habit.todayDone) {
    habit.streak++
    habit.weekProgress = Math.min(habit.weekProgress + 1, 7)
    uni.showToast({ title: '打卡成功！', icon: 'none' })
  } else {
    habit.streak--
    habit.weekProgress = Math.max(habit.weekProgress - 1, 0)
  }
}

const addHabit = () => {
  uni.showToast({ title: '创建习惯功能开发中', icon: 'none' })
}

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
.habit-stats { display: flex; justify-content: space-around; padding: 20px; background: #fff; margin: -10px 15px 15px; border-radius: 16px; box-shadow: 0 2px 8px rgba(0,0,0,0.04); }
.stat-item { text-align: center; }
.stat-num { font-size: 28px; font-weight: 700; color: #F59E0B; display: block; }
.stat-label { font-size: 12px; color: #9CA3AF; }
.habits-list { padding: 0 15px; }
.habit-card { display: flex; align-items: center; gap: 16px; background: #fff; border-radius: 16px; padding: 16px; margin-bottom: 12px; }
.habit-icon { width: 50px; height: 50px; border-radius: 14px; display: flex; align-items: center; justify-content: center; font-size: 24px; }
.habit-info { flex: 1; }
.habit-name { font-size: 16px; font-weight: 600; color: #1F2937; display: block; margin-bottom: 8px; }
.habit-progress { display: flex; align-items: center; gap: 12px; }
.progress-dots { display: flex; gap: 4px; }
.dot { width: 8px; height: 8px; background: #E5E7EB; border-radius: 50%; }
.dot.active { background: #F59E0B; }
.streak-text { font-size: 12px; color: #9CA3AF; }
.check-btn { width: 44px; height: 44px; border: 2px solid #E5E7EB; border-radius: 50%; display: flex; align-items: center; justify-content: center; }
.check-btn text { font-size: 20px; color: #D1D5DB; }
.check-btn.checked { background: #10B981; border-color: #10B981; }
.check-btn.checked text { color: #fff; }
.add-btn { margin: 20px; height: 50px; background: linear-gradient(135deg, #F59E0B, #D97706); border-radius: 25px; display: flex; align-items: center; justify-content: center; }
.add-btn text { font-size: 16px; font-weight: 600; color: #fff; }
</style>
