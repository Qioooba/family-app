<template>
  <view class="game-page">
    <!-- 头部 -->
    <view class="header">
      <view class="header-left" @click="goBack">
        <u-icon name="arrow-left" color="#fff" size="40"></u-icon>
      </view>
      <view class="header-title">猜谜语</view>
      <view class="header-right" @click="showRules">
        <text>规则</text>
      </view>
    </view>

    <!-- 游戏状态卡片 -->
    <view class="status-card">
      <view class="status-item">
        <text class="status-value">{{ score }}</text>
        <text class="status-label">得分</text>
      </view>
      <view class="status-divider"></view>
      <view class="status-item">
        <text class="status-value">{{ streak }}</text>
        <text class="status-label">连对</text>
      </view>
      <view class="status-divider"></view>
      <view class="status-item">
        <text class="status-value">{{ answeredCount }}</text>
        <text class="status-label">已答</text>
      </view>
    </view>

    <!-- 谜语卡片 -->
    <view class="riddle-card" v-if="currentRiddle">
      <view class="riddle-category">
        <text class="category-tag">{{ currentRiddle.category || '谜语' }}</text>
        <text class="difficulty-tag" :class="'diff-' + (currentRiddle.difficulty || 1)">
          {{ ['简单', '中等', '困难'][currentRiddle.difficulty - 1] || '简单' }}
        </text>
      </view>
      
      <view class="riddle-content">
        <text class="riddle-text">{{ currentRiddle.question }}</text>
      </view>
      
      <view class="riddle-hint" v-if="showHint">
        <text class="hint-label">提示：</text>
        <text class="hint-text">{{ currentRiddle.hint }}</text>
      </view>
      
      <view class="answer-section" v-if="showAnswer">
        <view class="answer-reveal">
          <text class="answer-label">谜底：</text>
          <text class="answer-text">{{ currentRiddle.answer }}</text>
        </view>
        <view class="explanation" v-if="currentRiddle.explanation">
          <text class="explanation-text">{{ currentRiddle.explanation }}</text>
        </view>
      </view>
    </view>

    <!-- 答案输入区 -->
    <view class="answer-area" v-if="!showAnswer">
      <view class="input-wrapper">
        <input 
          v-model="userAnswer"
          class="answer-input"
          placeholder="请输入谜底..."
          confirm-type="done"
          @confirm="submitAnswer"
        />
      </view>
      
      <view class="action-btns">
        <view class="btn btn-hint" @click="toggleHint">
          <text>💡 {{ showHint ? '隐藏提示' : '提示' }}</text>
        </view>
        <view class="btn btn-submit" @click="submitAnswer">
          <text>提交答案</text>
        </view>
      </view>
    </view>

    <!-- 结果反馈 -->
    <view class="result-area" v-if="showAnswer">
      <view class="result-icon">
        <text>{{ isCorrect ? '🎉' : '😅' }}</text>
      </view>
      <view class="result-text" :class="{ correct: isCorrect }">
        <text>{{ isCorrect ? '回答正确！' : '回答错误' }}</text>
      </view>
      <view class="points-earned" v-if="isCorrect">
        <text>+{{ earnedPoints }} 积分</text>
      </view>
      
      <view class="action-btns">
        <view class="btn btn-next" @click="nextRiddle">
          <text>下一题 ➡️</text>
        </view>
      </view>
    </view>

    <!-- 分类选择 -->
    <view class="category-section">
      <view class="section-title">选择分类</view>
      <scroll-view class="category-list" scroll-x>
        <view 
          v-for="cat in categories" 
          :key="cat.value"
          class="category-item"
          :class="{ active: currentCategory === cat.value }"
          @click="changeCategory(cat.value)"
        >
          <text>{{ cat.label }}</text>
        </view>
      </scroll-view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { gameApi } from '../../../api/game.js'

// 游戏状态
const score = ref(0)
const streak = ref(0)
const answeredCount = ref(0)
const userAnswer = ref('')
const showHint = ref(false)
const showAnswer = ref(false)
const isCorrect = ref(false)
const earnedPoints = ref(0)

// 当前谜语
const currentRiddle = ref(null)

// 分类
const categories = [
  { label: '全部', value: 'all' },
  { label: '动物', value: 'animal' },
  { label: '植物', value: 'plant' },
  { label: '物品', value: 'object' },
  { label: '字谜', value: 'character' },
  { label: '成语', value: 'idiom' },
  { label: '地名', value: 'place' }
]
const currentCategory = ref('all')

// 返回上一页
const goBack = () => {
  uni.navigateBack()
}

// 显示规则
const showRules = () => {
  uni.showModal({
    title: '猜谜语规则',
    content: '根据谜面猜出谜底，答对获得积分奖励。\n\n💡 使用提示会减半积分\n🔥 连续答对有额外奖励',
    showCancel: false
  })
}

// 加载谜语
const loadRiddle = async () => {
  try {
    uni.showLoading({ title: '加载中...' })
    const res = await gameApi.getRandomRiddle()
    if (res) {
      currentRiddle.value = res
      userAnswer.value = ''
      showHint.value = false
      showAnswer.value = false
      isCorrect.value = false
      earnedPoints.value = 0
    }
    uni.hideLoading()
  } catch (e) {
    console.error('加载谜语失败', e)
    uni.hideLoading()
    uni.showToast({ title: '加载失败', icon: 'none' })
  }
}

// 提交答案
const submitAnswer = async () => {
  if (!userAnswer.value.trim()) {
    uni.showToast({ title: '请输入答案', icon: 'none' })
    return
  }
  
  try {
    const res = await gameApi.verifyRiddleAnswer(
      currentRiddle.value.id, 
      userAnswer.value.trim()
    )
    
    showAnswer.value = true
    isCorrect.value = res === true
    answeredCount.value++
    
    if (isCorrect.value) {
      streak.value++
      // 计算积分
      let points = (currentRiddle.value.difficulty || 1) * 10
      if (showHint.value) points = Math.floor(points / 2)
      if (streak.value >= 3) points += 5 // 连对奖励
      earnedPoints.value = points
      score.value += points
      
      // 添加积分
      addPoints(points)
      
      uni.showToast({ title: '回答正确！', icon: 'success' })
    } else {
      streak.value = 0
      uni.showToast({ title: '回答错误', icon: 'none' })
    }
  } catch (e) {
    console.error('提交答案失败', e)
    uni.showToast({ title: '提交失败', icon: 'none' })
  }
}

// 添加积分
const addPoints = async (points) => {
  try {
    await gameApi.addPoints({
      gameType: 'riddle',
      points: points,
      description: `猜谜语 ${isCorrect.value ? '答对' : '答错'}`
    })
  } catch (e) {
    console.error('添加积分失败', e)
  }
}

// 切换提示
const toggleHint = () => {
  showHint.value = !showHint.value
}

// 下一题
const nextRiddle = () => {
  loadRiddle()
}

// 切换分类
const changeCategory = (category) => {
  currentCategory.value = category
  // 实际应用中这里应该根据分类加载谜语
  loadRiddle()
}

onMounted(() => {
  loadRiddle()
})
</script>

<style lang="scss" scoped>
.game-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #FF6B6B 0%, #f8f9fc 30%);
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

// 状态卡片
.status-card {
  display: flex;
  justify-content: space-around;
  align-items: center;
  background: #fff;
  border-radius: 24rpx;
  margin: 0 32rpx 32rpx;
  padding: 32rpx;
  box-shadow: 0 8rpx 32rpx rgba(255, 107, 107, 0.2);
  
  .status-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    
    .status-value {
      font-size: 48rpx;
      font-weight: 700;
      color: #FF6B6B;
      margin-bottom: 8rpx;
    }
    
    .status-label {
      font-size: 24rpx;
      color: #8b9aad;
    }
  }
  
  .status-divider {
    width: 2rpx;
    height: 60rpx;
    background: #e2e8f0;
  }
}

// 谜语卡片
.riddle-card {
  background: #fff;
  border-radius: 32rpx;
  margin: 0 32rpx 32rpx;
  padding: 40rpx;
  box-shadow: 0 8rpx 32rpx rgba(0,0,0,0.08);
  
  .riddle-category {
    display: flex;
    gap: 16rpx;
    margin-bottom: 32rpx;
    
    .category-tag {
      padding: 8rpx 20rpx;
      background: rgba(255, 107, 107, 0.1);
      color: #FF6B6B;
      font-size: 24rpx;
      border-radius: 20rpx;
    }
    
    .difficulty-tag {
      padding: 8rpx 20rpx;
      font-size: 24rpx;
      border-radius: 20rpx;
      
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
  
  .riddle-content {
    background: linear-gradient(135deg, #fff5f5 0%, #fff 100%);
    border-radius: 24rpx;
    padding: 48rpx 32rpx;
    margin-bottom: 32rpx;
    
    .riddle-text {
      font-size: 36rpx;
      color: #2d3748;
      line-height: 1.8;
      text-align: center;
      font-weight: 500;
    }
  }
  
  .riddle-hint {
    background: rgba(255, 215, 0, 0.1);
    border-radius: 16rpx;
    padding: 24rpx;
    margin-bottom: 24rpx;
    
    .hint-label {
      font-size: 26rpx;
      color: #f6ad55;
      font-weight: 600;
    }
    
    .hint-text {
      font-size: 28rpx;
      color: #5a6c7d;
      margin-left: 8rpx;
    }
  }
  
  .answer-section {
    background: linear-gradient(135deg, #f0fff4 0%, #fff 100%);
    border-radius: 16rpx;
    padding: 32rpx;
    
    .answer-reveal {
      text-align: center;
      margin-bottom: 20rpx;
      
      .answer-label {
        font-size: 28rpx;
        color: #8b9aad;
      }
      
      .answer-text {
        font-size: 48rpx;
        font-weight: 700;
        color: #68d391;
        margin-left: 16rpx;
      }
    }
    
    .explanation {
      border-top: 2rpx solid #e2e8f0;
      padding-top: 20rpx;
      
      .explanation-text {
        font-size: 26rpx;
        color: #5a6c7d;
        line-height: 1.6;
      }
    }
  }
}

// 答案输入区
.answer-area {
  margin: 0 32rpx 32rpx;
  
  .input-wrapper {
    background: #fff;
    border-radius: 24rpx;
    padding: 8rpx;
    margin-bottom: 24rpx;
    box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.06);
    
    .answer-input {
      height: 100rpx;
      padding: 0 32rpx;
      font-size: 32rpx;
      color: #2d3748;
      text-align: center;
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
      }
      
      &.btn-hint {
        background: #fff;
        box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.06);
        
        text {
          color: #f6ad55;
        }
      }
      
      &.btn-submit {
        background: linear-gradient(135deg, #FF6B6B 0%, #ee5a24 100%);
        box-shadow: 0 8rpx 24rpx rgba(255, 107, 107, 0.35);
        
        text {
          color: #fff;
        }
      }
    }
  }
}

// 结果区域
.result-area {
  margin: 0 32rpx 32rpx;
  background: #fff;
  border-radius: 32rpx;
  padding: 48rpx;
  box-shadow: 0 8rpx 32rpx rgba(0,0,0,0.08);
  text-align: center;
  
  .result-icon {
    font-size: 80rpx;
    margin-bottom: 24rpx;
  }
  
  .result-text {
    font-size: 36rpx;
    font-weight: 600;
    color: #fc8181;
    margin-bottom: 16rpx;
    
    &.correct {
      color: #68d391;
    }
  }
  
  .points-earned {
    font-size: 32rpx;
    color: #FF6B6B;
    font-weight: 600;
    margin-bottom: 32rpx;
  }
  
  .btn-next {
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 28rpx 0;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 24rpx;
    box-shadow: 0 8rpx 24rpx rgba(102, 126, 234, 0.35);
    
    text {
      font-size: 30rpx;
      font-weight: 600;
      color: #fff;
    }
  }
}

// 分类选择
.category-section {
  margin: 0 32rpx;
  
  .section-title {
    font-size: 30rpx;
    font-weight: 600;
    color: #2d3748;
    margin-bottom: 20rpx;
  }
  
  .category-list {
    white-space: nowrap;
    
    .category-item {
      display: inline-block;
      padding: 16rpx 32rpx;
      margin-right: 16rpx;
      background: #fff;
      border-radius: 32rpx;
      font-size: 26rpx;
      color: #5a6c7d;
      box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.06);
      
      &.active {
        background: linear-gradient(135deg, #FF6B6B 0%, #ee5a24 100%);
        color: #fff;
      }
    }
  }
}
</style>