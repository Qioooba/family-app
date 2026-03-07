<template>
  <view class="game-page">
    <!-- 头部 -->
    <view class="header">
      <view class="header-left" @click="goBack">
        <u-icon name="arrow-left" color="#fff" size="40"></u-icon>
      </view>
      <view class="header-title">K歌评分</view>
      <view class="header-right" @click="showRules">
        <text>规则</text>
      </view>
    </view>

    <!-- 歌曲选择 -->
    <view class="song-select" v-if="!selectedSong">
      <view class="section-title">选择歌曲</view>
      
      <scroll-view class="song-list" scroll-y>
        <view 
          v-for="song in songs" 
          :key="song.id"
          class="song-item"
          @click="selectSong(song)"
        >
          <view class="song-cover">
            <text>🎵</text>
          </view>
          <view class="song-info">
            <text class="song-name">{{ song.name }}</text>
            <text class="song-singer">{{ song.singer }}</text>
          </view>
          <view class="song-difficulty" :class="'diff-' + song.difficulty">
            <text>{{ ['简单', '中等', '困难'][song.difficulty - 1] }}</text>
          </view>
        </view>
      </scroll-view>
    </view>

    <!-- 演唱界面 -->
    <view class="sing-section" v-else-if="!isFinished">
      <!-- 歌曲信息 -->
      <view class="song-display">
        <view class="playing-animation">
          <view class="bar" v-for="i in 5" :key="i" :style="{ animationDelay: i * 0.1 + 's' }"></view>
        </view>
        <view class="current-song">
          <text class="song-name">{{ selectedSong.name }}</text>
          <text class="song-singer">{{ selectedSong.singer }}</text>
        </view>
      </view>

      <!-- 歌词显示 -->
      <view class="lyrics-area">
        <scroll-view class="lyrics-scroll" scroll-y>
          <view 
            v-for="(line, index) in lyrics" 
            :key="index"
            class="lyric-line"
            :class="{ active: currentLyricIndex === index }"
          >
            <text>{{ line.text }}</text>
          </view>
        </scroll-view>
      </view>

      <!-- 录音控制 -->
      <view class="record-control">
        <view class="record-progress">
          <view class="progress-bar">
            <view class="progress-fill" :style="{ width: recordProgress + '%' }"></view>
          </view>
          <text class="progress-time">{{ formatTime(recordTime) }} / {{ formatTime(totalTime) }}</text>
        </view>

        <view class="control-btns">
          <view class="btn btn-stop" v-if="isRecording" @click="stopRecording">
            <view class="stop-icon"></view>
          </view>
          <view class="btn btn-record" v-else @click="startRecording">
            <view class="record-icon"></view>
          </view>
        </view>

        <view class="record-status">
          <text>{{ isRecording ? '正在录音...' : '点击开始录音' }}</text>
        </view>
      </view>

      <!-- 完成按钮 -->
      <view class="finish-btn" v-if="recordTime > 10" @click="finishSinging"
        :class="{ disabled: isRecording }"
      >
        <text>完成演唱</text>
      </view>
    </view>

    <!-- 评分结果 -->
    <view class="result-section" v-else>
      <view class="score-display">
        <view class="score-circle">
          <text class="score-value">{{ totalScore }}</text>
          <text class="score-label">总分</text>
        </view>
      </view>

      <view class="score-details">
        <view class="detail-item">
          <text class="detail-name">音准</text>
          <view class="detail-bar">
            <view class="detail-fill" :style="{ width: pitchScore + '%' }"></view>
          </view>
          <text class="detail-score">{{ pitchScore }}</text>
        </view>
        
        <view class="detail-item">
          <text class="detail-name">节奏</text>
          <view class="detail-bar">
            <view class="detail-fill" :style="{ width: rhythmScore + '%' }"></view>
          </view>
          <text class="detail-score">{{ rhythmScore }}</text>
        </view>
        
        <view class="detail-item">
          <text class="detail-name">情感</text>
          <view class="detail-bar">
            <view class="detail-fill" :style="{ width: emotionScore + '%' }"></view>
          </view>
          <text class="detail-score">{{ emotionScore }}</text>
        </view>
      </view>

      <view class="score-comment">
        <text>{{ getComment(totalScore) }}</text>
      </view>

      <view class="score-bonus" v-if="totalScore >= 80">
        <text>🎉 获得 {{ bonusPoints }} 积分</text>
      </view>

      <view class="action-btns">
        <view class="btn btn-replay" @click="replay">
          <text>再唱一次</text>
        </view>
        <view class="btn btn-back" @click="backToSelect">
          <text>选择其他歌曲</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { gameApi } from '../../../api/game.js'

// 歌曲列表
const songs = ref([
  { id: 1, name: '生日快乐', singer: '经典儿歌', difficulty: 1 },
  { id: 2, name: '小星星', singer: '经典儿歌', difficulty: 1 },
  { id: 3, name: '茉莉花', singer: '民歌', difficulty: 2 },
  { id: 4, name: '月亮代表我的心', singer: '邓丽君', difficulty: 2 },
  { id: 5, name: '童话', singer: '光良', difficulty: 3 },
  { id: 6, name: '稻香', singer: '周杰伦', difficulty: 2 },
  { id: 7, name: '朋友', singer: '周华健', difficulty: 2 },
  { id: 8, name: '明天会更好', singer: '群星', difficulty: 3 }
])

// 游戏状态
const selectedSong = ref(null)
const isRecording = ref(false)
const isFinished = ref(false)
const recordTime = ref(0)
const totalTime = ref(180)
const recordProgress = ref(0)
const currentLyricIndex = ref(0)
const timer = ref(null)

// 歌词
const lyrics = ref([
  { time: 0, text: '准备开始...' },
  { time: 5, text: '🎵 音乐响起 🎵' },
  { time: 10, text: '请跟随节奏演唱' },
  { time: 15, text: '享受音乐的快乐' }
])

// 评分
const pitchScore = ref(0)
const rhythmScore = ref(0)
const emotionScore = ref(0)
const totalScore = ref(0)
const bonusPoints = ref(0)

// 返回
const goBack = () => {
  uni.navigateBack()
}

// 显示规则
const showRules = () => {
  uni.showModal({
    title: 'K歌评分规则',
    content: '选择歌曲，跟随歌词演唱。\n系统会从音准、节奏、情感三个维度评分。\n80分以上获得积分奖励！',
    showCancel: false
  })
}

// 选择歌曲
const selectSong = (song) => {
  selectedSong.value = song
  totalTime.value = 180 // 3分钟
}

// 格式化时间
const formatTime = (seconds) => {
  const mins = Math.floor(seconds / 60).toString().padStart(2, '0')
  const secs = (seconds % 60).toString().padStart(2, '0')
  return `${mins}:${secs}`
}

// 开始录音
const startRecording = () => {
  isRecording.value = true
  
  // 开始计时
  timer.value = setInterval(() => {
    recordTime.value++
    recordProgress.value = (recordTime.value / totalTime.value) * 100
    
    // 更新歌词
    updateLyrics()
    
    // 自动停止
    if (recordTime.value >= totalTime.value) {
      stopRecording()
    }
  }, 1000)
}

// 停止录音
const stopRecording = () => {
  isRecording.value = false
  clearInterval(timer.value)
}

// 更新歌词
const updateLyrics = () => {
  // 简单的歌词切换逻辑
  const index = Math.floor(recordTime.value / 5) % lyrics.value.length
  currentLyricIndex.value = index
}

// 完成演唱
const finishSinging = () => {
  if (isRecording.value) {
    stopRecording()
  }
  
  // 模拟评分
  pitchScore.value = Math.floor(60 + Math.random() * 40)
  rhythmScore.value = Math.floor(60 + Math.random() * 40)
  emotionScore.value = Math.floor(60 + Math.random() * 40)
  totalScore.value = Math.floor((pitchScore.value + rhythmScore.value + emotionScore.value) / 3)
  
  if (totalScore.value >= 80) {
    bonusPoints.value = Math.floor(totalScore.value / 10)
    addPoints(bonusPoints.value)
  }
  
  isFinished.value = true
}

// 获取评语
const getComment = (score) => {
  if (score >= 90) return '太棒了！你是歌神！🌟'
  if (score >= 80) return '非常好听！继续加油！👏'
  if (score >= 70) return '不错的表现！👍'
  if (score >= 60) return '还可以更好哦~ 💪'
  return '别灰心，多练习就会进步！🎵'
}

// 添加积分
const addPoints = async (points) => {
  try {
    await gameApi.addPoints({
      gameType: 'karaoke',
      points: points,
      description: `K歌评分 ${totalScore.value}分`
    })
  } catch (e) {
    console.error('添加积分失败', e)
  }
}

// 再唱一次
const replay = () => {
  isFinished.value = false
  recordTime.value = 0
  recordProgress.value = 0
  currentLyricIndex.value = 0
  pitchScore.value = 0
  rhythmScore.value = 0
  emotionScore.value = 0
  totalScore.value = 0
}

// 返回选择
const backToSelect = () => {
  selectedSong.value = null
  isFinished.value = false
  recordTime.value = 0
  recordProgress.value = 0
}

onUnmounted(() => {
  clearInterval(timer.value)
})
</script>

<style lang="scss" scoped>
.game-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #e84393 0%, #f8f9fc 30%);
  padding-bottom: 40rpx;
}

// 头部
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 100rpx 32rpx 40rpx;
  
  .header-title {
    font-size: 40rpx;
    font-weight: 700;
    color: #fff;
  }
  
  .header-right {
    text {
      font-size: 28rpx;
      color: rgba(255,255,255,0.9);
    }
  }
}

// 歌曲选择
.song-select {
  margin: 0 32rpx;
  
  .section-title {
    font-size: 32rpx;
    font-weight: 600;
    color: #fff;
    margin-bottom: 24rpx;
  }
  
  .song-list {
    height: calc(100vh - 300rpx);
    
    .song-item {
      display: flex;
      align-items: center;
      background: #fff;
      border-radius: 24rpx;
      padding: 24rpx;
      margin-bottom: 20rpx;
      
      .song-cover {
        width: 100rpx;
        height: 100rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        background: linear-gradient(135deg, #e84393 0%, #fd79a8 100%);
        border-radius: 20rpx;
        margin-right: 24rpx;
        
        text {
          font-size: 48rpx;
        }
      }
      
      .song-info {
        flex: 1;
        
        .song-name {
          font-size: 32rpx;
          font-weight: 600;
          color: #2d3748;
          display: block;
          margin-bottom: 8rpx;
        }
        
        .song-singer {
          font-size: 26rpx;
          color: #8b9aad;
        }
      }
      
      .song-difficulty {
        padding: 8rpx 20rpx;
        border-radius: 20rpx;
        font-size: 22rpx;
        
        &.diff-1 {
          background: rgba(104, 211, 145, 0.1);
          color: #68d391;
        }
        
        &.diff-2 {
          background: rgba(246, 173, 85, 0.1);
          color: #f6ad55;
        }
        
        &.diff-3 {
          background: rgba(252, 129, 129, 0.1);
          color: #fc8181;
        }
      }
    }
  }
}

// 演唱界面
.sing-section {
  margin: 0 32rpx;
  
  .song-display {
    text-align: center;
    margin-bottom: 32rpx;
    
    .playing-animation {
      display: flex;
      justify-content: center;
      align-items: flex-end;
      gap: 8rpx;
      height: 60rpx;
      margin-bottom: 16rpx;
      
      .bar {
        width: 8rpx;
        background: #fff;
        border-radius: 4rpx;
        animation: sound 1s ease-in-out infinite;
        
        &:nth-child(1) { height: 20rpx; }
        &:nth-child(2) { height: 40rpx; }
        &:nth-child(3) { height: 60rpx; }
        &:nth-child(4) { height: 40rpx; }
        &:nth-child(5) { height: 20rpx; }
      }
    }
    
    .current-song {
      .song-name {
        font-size: 40rpx;
        font-weight: 700;
        color: #fff;
        display: block;
        margin-bottom: 8rpx;
      }
      
      .song-singer {
        font-size: 28rpx;
        color: rgba(255,255,255,0.8);
      }
    }
  }
  
  .lyrics-area {
    background: rgba(255,255,255,0.95);
    border-radius: 24rpx;
    height: 400rpx;
    margin-bottom: 32rpx;
    overflow: hidden;
    
    .lyrics-scroll {
      height: 100%;
      
      .lyric-line {
        text-align: center;
        padding: 24rpx;
        transition: all 0.3s ease;
        
        text {
          font-size: 32rpx;
          color: #8b9aad;
        }
        
        &.active {
          background: rgba(232, 67, 147, 0.1);
          
          text {
            font-size: 38rpx;
            color: #e84393;
            font-weight: 600;
          }
        }
      }
    }
  }
  
  .record-control {
    background: #fff;
    border-radius: 32rpx;
    padding: 32rpx;
    margin-bottom: 32rpx;
    
    .record-progress {
      margin-bottom: 32rpx;
      
      .progress-bar {
        height: 12rpx;
        background: #e2e8f0;
        border-radius: 6rpx;
        overflow: hidden;
        margin-bottom: 16rpx;
        
        .progress-fill {
          height: 100%;
          background: linear-gradient(90deg, #e84393 0%, #fd79a8 100%);
          border-radius: 6rpx;
          transition: width 0.3s ease;
        }
      }
      
      .progress-time {
        font-size: 26rpx;
        color: #8b9aad;
        text-align: center;
        display: block;
      }
    }
    
    .control-btns {
      display: flex;
      justify-content: center;
      margin-bottom: 24rpx;
      
      .btn {
        width: 120rpx;
        height: 120rpx;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        
        &.btn-record {
          background: linear-gradient(135deg, #e84393 0%, #fd79a8 100%);
          box-shadow: 0 8rpx 32rpx rgba(232, 67, 147, 0.4);
          
          .record-icon {
            width: 48rpx;
            height: 48rpx;
            background: #fff;
            border-radius: 50%;
          }
        }
        
        &.btn-stop {
          background: #fc8181;
          box-shadow: 0 8rpx 32rpx rgba(252, 129, 129, 0.4);
          
          .stop-icon {
            width: 40rpx;
            height: 40rpx;
            background: #fff;
            border-radius: 8rpx;
          }
        }
      }
    }
    
    .record-status {
      text-align: center;
      
      text {
        font-size: 28rpx;
        color: #5a6c7d;
      }
    }
  }
  
  .finish-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 28rpx 0;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 24rpx;
    box-shadow: 0 8rpx 24rpx rgba(102, 126, 234, 0.35);
    
    text {
      font-size: 32rpx;
      font-weight: 600;
      color: #fff;
    }
    
    &.disabled {
      background: #ccc;
      box-shadow: none;
    }
  }
}

// 评分结果
.result-section {
  margin: 0 32rpx;
  
  .score-display {
    display: flex;
    justify-content: center;
    margin-bottom: 48rpx;
    
    .score-circle {
      width: 240rpx;
      height: 240rpx;
      border-radius: 50%;
      background: linear-gradient(135deg, #e84393 0%, #fd79a8 100%);
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      box-shadow: 0 16rpx 48rpx rgba(232, 67, 147, 0.4);
      
      .score-value {
        font-size: 80rpx;
        font-weight: 700;
        color: #fff;
        line-height: 1;
      }
      
      .score-label {
        font-size: 28rpx;
        color: rgba(255,255,255,0.9);
        margin-top: 8rpx;
      }
    }
  }
  
  .score-details {
    background: #fff;
    border-radius: 24rpx;
    padding: 32rpx;
    margin-bottom: 32rpx;
    
    .detail-item {
      display: flex;
      align-items: center;
      margin-bottom: 24rpx;
      
      &:last-child {
        margin-bottom: 0;
      }
      
      .detail-name {
        width: 80rpx;
        font-size: 28rpx;
        color: #5a6c7d;
      }
      
      .detail-bar {
        flex: 1;
        height: 16rpx;
        background: #e2e8f0;
        border-radius: 8rpx;
        overflow: hidden;
        margin: 0 20rpx;
        
        .detail-fill {
          height: 100%;
          background: linear-gradient(90deg, #e84393 0%, #fd79a8 100%);
          border-radius: 8rpx;
          transition: width 1s ease;
        }
      }
      
      .detail-score {
        width: 60rpx;
        font-size: 28rpx;
        color: #e84393;
        font-weight: 600;
        text-align: right;
      }
    }
  }
  
  .score-comment {
    text-align: center;
    margin-bottom: 24rpx;
    
    text {
      font-size: 32rpx;
      color: #2d3748;
      font-weight: 600;
    }
  }
  
  .score-bonus {
    text-align: center;
    padding: 20rpx;
    background: rgba(255, 215, 0, 0.1);
    border-radius: 16rpx;
    margin-bottom: 32rpx;
    
    text {
      font-size: 28rpx;
      color: #f6ad55;
      font-weight: 600;
    }
  }
  
  .action-btns {
    display: flex;
    gap: 24rpx;
    
    .btn {
      flex: 1;
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 28rpx 0;
      border-radius: 24rpx;
      
      text {
        font-size: 30rpx;
        font-weight: 600;
        color: #fff;
      }
      
      &.btn-replay {
        background: linear-gradient(135deg, #e84393 0%, #fd79a8 100%);
        box-shadow: 0 8rpx 24rpx rgba(232, 67, 147, 0.35);
      }
      
      &.btn-back {
        background: #8b9aad;
      }
    }
  }
}

@keyframes sound {
  0%, 100% { transform: scaleY(1); }
  50% { transform: scaleY(0.5); }
}
</style>