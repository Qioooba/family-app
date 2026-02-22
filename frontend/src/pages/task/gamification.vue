<template>
  <view class="page-container">
    <view class="header">
      <view class="header-left" @click="goBack"><text>‹</text></text>
      <view class="header-title">积分成就</view>
    </view>
    
    <view class="score-card"
>
      <view class="score-main"
>
        <text class="score-label">我的积分</text>
        <text class="score-value">{{ userScore }}</text>
        <text class="score-rank">家庭排名 #{{ userRank }}</text>
      </view>
      
      <view class="score-level"
>
        <text class="level-name">{{ currentLevel.name }}</text>
        <view class="level-progress"
>
          <view class="progress-bar">
            <view class="progress-fill" :style="{ width: levelProgress + '%' }"></text>
          </view>
          <text class="progress-text">{{ currentLevel.progress }}/{{ currentLevel.max }}</text>
        </view>
      </view>
    </view>
    
    <view class="achievements-section"
>
      <view class="section-title">🏆 成就徽章</view>
      
      <view class="achievements-grid"
>
        <view v-for="ach in achievements" :key="ach.id" class="achievement-card" :class="{ unlocked: ach.unlocked }"
>
          <view class="achievement-icon">{{ ach.icon }}</view>
          <text class="achievement-name">{{ ach.name }}</text>
          <text v-if="ach.unlocked" class="achievement-date">{{ ach.date }}</text>
          <view v-else class="achievement-lock">🔒</view>
        </view>
      </view>
    </view>
    
    <view class="leaderboard-section"
>
      <view class="section-title">📊 家庭排行榜</view>
      
      <view class="leaderboard-list"
>
        <view v-for="(user, index) in leaderboard" :key="user.id" class="leaderboard-item" :class="{ 'is-me': user.isMe }"
>
          <text class="rank">{{ index + 1 }}</text>
          <view class="user-avatar">{{ user.avatar }}</view>
          <text class="user-name">{{ user.name }}</text>
          <text class="user-score">{{ user.score }}分</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'

const userScore = ref(1250)
const userRank = ref(2)

const currentLevel = ref({ name: '勤劳蜜蜂', progress: 1250, max: 2000 })
const levelProgress = computed(() => currentLevel.value.progress / currentLevel.value.max * 100)

const achievements = ref([
  { id: 1, name: '初次打卡', icon: '🌟', unlocked: true, date: '2024-01-15' },
  { id: 2, name: '连续7天', icon: '🔥', unlocked: true, date: '2024-01-22' },
  { id: 3, name: '任务达人', icon: '🏆', unlocked: true, date: '2024-02-01' },
  { id: 4, name: '完美一周', icon: '💯', unlocked: false },
  { id: 5, name: '月度之星', icon: '🌙', unlocked: false },
  { id: 6, name: '习惯大师', icon: '👑', unlocked: false }
])

const leaderboard = ref([
  { id: 1, name: '妈妈', avatar: '👩', score: 1580, isMe: false },
  { id: 2, name: '我', avatar: '👤', score: 1250, isMe: true },
  { id: 3, name: '爸爸', avatar: '👨', score: 980, isMe: false },
  { id: 4, name: '妹妹', avatar: '👧', score: 720, isMe: false }
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
.score-card { background: linear-gradient(135deg, #F59E0B, #D97706); margin: -20px 15px 15px; padding: 24px; border-radius: 20px; color: #fff; }
.score-main { text-align: center; margin-bottom: 20px; }
.score-label { font-size: 14px; opacity: 0.9; display: block; margin-bottom: 8px; }
.score-value { font-size: 48px; font-weight: 700; display: block; margin-bottom: 4px; }
.score-rank { font-size: 13px; opacity: 0.8; }
.score-level { background: rgba(255,255,255,0.2); border-radius: 16px; padding: 16px; }
.level-name { font-size: 16px; font-weight: 600; margin-bottom: 10px; display: block; }
.level-progress { display: flex; align-items: center; gap: 12px; }
.progress-bar { flex: 1; height: 8px; background: rgba(255,255,255,0.3); border-radius: 4px; overflow: hidden; }
.progress-fill { height: 100%; background: #fff; border-radius: 4px; transition: width 0.5s; }
.progress-text { font-size: 12px; }
.achievements-section { padding: 0 15px 15px; }
.section-title { font-size: 16px; font-weight: 600; color: #1F2937; margin-bottom: 16px; padding: 0 5px; display: block; }
.achievements-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 12px; }
.achievement-card { background: #fff; border-radius: 16px; padding: 20px 10px; text-align: center; opacity: 0.5; }
.achievement-card.unlocked { opacity: 1; box-shadow: 0 2px 8px rgba(0,0,0,0.06); }
.achievement-icon { font-size: 36px; margin-bottom: 8px; display: block; }
.achievement-name { font-size: 12px; color: #374151; display: block; margin-bottom: 4px; }
.achievement-date { font-size: 10px; color: #10B981; }
.achievement-lock { font-size: 16px; opacity: 0.3; }
.leaderboard-section { padding: 0 15px; }
.leaderboard-list { background: #fff; border-radius: 16px; padding: 8px; }
.leaderboard-item { display: flex; align-items: center; gap: 12px; padding: 14px; border-bottom: 1px solid #F3F4F6; }
.leaderboard-item:last-child { border-bottom: none; }
.leaderboard-item.is-me { background: #FEF3C7; border-radius: 12px; }
.rank { width: 28px; text-align: center; font-size: 16px; font-weight: 700; color: #9CA3AF; }
.user-avatar { font-size: 28px; }
.user-name { flex: 1; font-size: 15px; color: #374151; }
.user-score { font-size: 15px; font-weight: 600; color: #F59E0B; }
</style>
