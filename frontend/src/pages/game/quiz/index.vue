<template>
  <view class="game-page">
    <!-- 头部 -->
    <view class="header">
      <view class="header-left" @click="goBack">
        <u-icon name="arrow-left" color="#fff" size="40"></u-icon>
      </view>
      <view class="header-title">问答竞赛</view>
      <view class="header-right" @click="showRules">
        <text>规则</text>
      </view>
    </view>

    <!-- 计时器和进度 -->
    <view class="progress-bar">
      <view class="progress-fill" :style="{ width: progressPercent + '%' }"></view>
    </view>
    <view class="timer-bar">
      <text class="timer-text">⏱️ {{ remainingTime }}秒</text>
    </view>

    <!-- 题目卡片 -->
    <view class="quiz-card" v-if="currentQuiz">
      <view class="question-num">
        <text>第 {{ currentIndex + 1 }} / {{ totalQuestions }} 题</text>
      </view>
      
      <view class="question-content">
        <text class="question-text">{{ currentQuiz.question }}</text>
      </view>
      
      <view class="options-list">
        <view 
          v-for="(option, index) in currentQuiz.options" 
          :key="index"
          class="option-item"
          :class="{ 
            selected: selectedOption === index,
            correct: showAnswer && index === currentQuiz.correctIndex,
            wrong: showAnswer && selectedOption === index && index !== currentQuiz.correctIndex
          }"
          @click="selectOption(index)"
        >
          <text class="option-label">{{ ['A', 'B', 'C', 'D'][index] }}</text>
          <text class="option-text">{{ option }}</text>
        </view>
      </view>
    </view>

    <!-- 结果展示 -->
    <view class="result-card" v-if="showAnswer">
      <view class="result-status" :class="{ correct: isCorrect }">
        <text>{{ isCorrect ? '✅ 回答正确' : '❌ 回答错误' }}</text>
      </view>
      <view class="correct-answer" v-if="!isCorrect">
        <text>正确答案：{{ ['A', 'B', 'C', 'D'][currentQuiz.correctIndex] }}</text>
      </view>
      <view class="explanation" v-if="currentQuiz.explanation">
        <text>{{ currentQuiz.explanation }}</text>
      </view>
    </view>

    <!-- 操作按钮 -->
    <view class="action-area">
      <view class="btn btn-next" v-if="showAnswer && currentIndex < totalQuestions - 1" @click="nextQuestion">
        <text>下一题</text>
      </view>
      <view class="btn btn-finish" v-if="showAnswer && currentIndex >= totalQuestions - 1" @click="finishQuiz">
        <text>完成竞赛</text>
      </view>
    </view>

    <!-- 成绩弹窗 -->
    <u-popup :show="showResult" mode="center" round="20">
      <view class="result-popup">
        <view class="result-icon">🏆</view>
        <view class="result-title">竞赛完成</view>
        <view class="result-score">{{ finalScore }}分</view>
        <view class="result-detail">
          <text>答对 {{ correctCount }} / {{ totalQuestions }} 题</text>
        </view>
        <view class="result-bonus" v-if="finalScore >= 80">
          <text>🎉 太棒了！获得 {{ bonusPoints }} 积分奖励</text>
        </view>
        <view class="popup-actions">
          <view class="btn btn-again" @click="restartQuiz">
            <text>再来一次</text>
          </view>
          <view class="btn btn-back" @click="goBack">
            <text>返回大厅</text>
          </view>
        </view>
      </view>
    </u-popup>
  </view>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { gameApi } from '../../../api/game.js'

// 游戏状态
const currentIndex = ref(0)
const totalQuestions = ref(10)
const score = ref(0)
const correctCount = ref(0)
const selectedOption = ref(null)
const showAnswer = ref(false)
const isCorrect = ref(false)
const remainingTime = ref(30)
const timer = ref(null)
const showResult = ref(false)
const finalScore = ref(0)
const bonusPoints = ref(0)

// 题目列表
const quizList = ref([])
const currentQuiz = computed(() => quizList.value[currentIndex.value])

// 进度百分比
const progressPercent = computed(() => ((currentIndex.value + 1) / totalQuestions.value) * 100)

// 返回
const goBack = () => {
  uni.navigateBack()
}

// 显示规则
const showRules = () => {
  uni.showModal({
    title: '问答竞赛规则',
    content: '每题限时30秒，答对得分。\n快速答对有额外奖励！\n答对80分以上可获得积分奖励。',
    showCancel: false
  })
}

// 加载题目
const loadQuizzes = async () => {
  try {
    uni.showLoading({ title: '加载中...' })
    // 模拟加载多道题目
    const quizzes = []
    for (let i = 0; i < totalQuestions.value; i++) {
      const res = await gameApi.getRandomQuiz()
      if (res) {
        quizzes.push({
          ...res,
          options: res.options || generateMockOptions(res.answer)
        })
      }
    }
    quizList.value = quizzes
    startTimer()
    uni.hideLoading()
  } catch (e) {
    console.error('加载题目失败', e)
    uni.hideLoading()
    // 加载模拟数据
    loadMockData()
  }
}

// 生成模拟选项
const generateMockOptions = (answer) => {
  const mockOptions = ['选项A', '选项B', '选项C', '选项D']
  return mockOptions
}

// 加载模拟数据
const loadMockData = () => {
  quizList.value = [
    {
      question: '中国的首都是哪里？',
      options: ['上海', '北京', '广州', '深圳'],
      correctIndex: 1,
      explanation: '北京是中华人民共和国的首都。'
    },
    {
      question: '一年有多少个节气？',
      options: ['12个', '24个', '36个', '48个'],
      correctIndex: 1,
      explanation: '中国传统历法中有24个节气。'
    },
    {
      question: '彩虹通常有几种颜色？',
      options: ['5种', '6种', '7种', '8种'],
      correctIndex: 2,
      explanation: '彩虹有红、橙、黄、绿、蓝、靛、紫7种颜色。'
    }
  ]
  totalQuestions.value = quizList.value.length
  startTimer()
}

// 开始计时
const startTimer = () => {
  remainingTime.value = 30
  clearInterval(timer.value)
  timer.value = setInterval(() => {
    remainingTime.value--
    if (remainingTime.value <= 0) {
      clearInterval(timer.value)
      if (!showAnswer.value) {
        handleTimeout()
      }
    }
  }, 1000)
}

// 超时处理
const handleTimeout = () => {
  showAnswer.value = true
  isCorrect.value = false
}

// 选择选项
const selectOption = (index) => {
  if (showAnswer.value) return
  
  selectedOption.value = index
  showAnswer.value = true
  isCorrect.value = index === currentQuiz.value.correctIndex
  clearInterval(timer.value)
  
  if (isCorrect.value) {
    correctCount.value++
    // 基础分10分，速度奖励最多10分
    const timeBonus = Math.floor(remainingTime.value / 3)
    score.value += 10 + timeBonus
  }
}

// 下一题
const nextQuestion = () => {
  currentIndex.value++
  selectedOption.value = null
  showAnswer.value = false
  isCorrect.value = false
  startTimer()
}

// 完成竞赛
const finishQuiz = () => {
  finalScore.value = Math.round((correctCount.value / totalQuestions.value) * 100)
  if (finalScore.value >= 80) {
    bonusPoints.value = Math.floor(finalScore.value / 10)
    addPoints(bonusPoints.value)
  }
  showResult.value = true
}

// 添加积分
const addPoints = async (points) => {
  try {
    await gameApi.addPoints({
      gameType: 'quiz',
      points: points,
      description: `问答竞赛得分 ${finalScore.value}`
    })
  } catch (e) {
    console.error('添加积分失败', e)
  }
}

// 重新开始
const restartQuiz = () => {
  currentIndex.value = 0
  score.value = 0
  correctCount.value = 0
  selectedOption.value = null
  showAnswer.value = false
  isCorrect.value = false
  showResult.value = false
  loadQuizzes()
}

onMounted(() => {
  loadQuizzes()
})

onUnmounted(() => {
  clearInterval(timer.value)
})
</script>

<style lang="scss" scoped>
.game-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #4facfe 0%, #f8f9fc 30%);
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

// 进度条
.progress-bar {
  margin: 0 32rpx 16rpx;
  height: 12rpx;
  background: rgba(255,255,255,0.3);
  border-radius: 6rpx;
  overflow: hidden;
  
  .progress-fill {
    height: 100%;
    background: linear-gradient(90deg, #fff 0%, rgba(255,255,255,0.8) 100%);
    border-radius: 6rpx;
    transition: width 0.3s ease;
  }
}

.timer-bar {
  margin: 0 32rpx 32rpx;
  
  .timer-text {
    font-size: 28rpx;
    color: #fff;
    font-weight: 600;
  }
}

// 题目卡片
.quiz-card {
  background: #fff;
  border-radius: 32rpx;
  margin: 0 32rpx 32rpx;
  padding: 40rpx;
  box-shadow: 0 8rpx 32rpx rgba(0,0,0,0.08);
  
  .question-num {
    text-align: center;
    margin-bottom: 32rpx;
    
    text {
      font-size: 26rpx;
      color: #8b9aad;
    }
  }
  
  .question-content {
    background: linear-gradient(135deg, #f0f8ff 0%, #fff 100%);
    border-radius: 24rpx;
    padding: 48rpx 32rpx;
    margin-bottom: 32rpx;
    
    .question-text {
      font-size: 36rpx;
      color: #2d3748;
      line-height: 1.6;
      font-weight: 500;
      display: block;
      text-align: center;
    }
  }
  
  .options-list {
    .option-item {
      display: flex;
      align-items: center;
      padding: 28rpx 32rpx;
      margin-bottom: 20rpx;
      background: #f8f9fc;
      border-radius: 20rpx;
      border: 4rpx solid transparent;
      transition: all 0.2s ease;
      
      &:active {
        transform: scale(0.98);
      }
      
      &.selected {
        border-color: #4facfe;
        background: rgba(79, 172, 254, 0.1);
      }
      
      &.correct {
        border-color: #68d391;
        background: rgba(104, 211, 145, 0.1);
      }
      
      &.wrong {
        border-color: #fc8181;
        background: rgba(252, 129, 129, 0.1);
      }
      
      .option-label {
        width: 56rpx;
        height: 56rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        background: #e2e8f0;
        border-radius: 50%;
        font-size: 28rpx;
        color: #5a6c7d;
        font-weight: 600;
        margin-right: 24rpx;
      }
      
      .option-text {
        flex: 1;
        font-size: 30rpx;
        color: #2d3748;
      }
    }
  }
}

// 结果卡片
.result-card {
  background: #fff;
  border-radius: 24rpx;
  margin: 0 32rpx 32rpx;
  padding: 32rpx;
  box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.06);
  
  .result-status {
    font-size: 32rpx;
    font-weight: 600;
    color: #fc8181;
    margin-bottom: 16rpx;
    
    &.correct {
      color: #68d391;
    }
  }
  
  .correct-answer {
    font-size: 28rpx;
    color: #68d391;
    margin-bottom: 16rpx;
  }
  
  .explanation {
    padding-top: 16rpx;
    border-top: 2rpx solid #e2e8f0;
    
    text {
      font-size: 26rpx;
      color: #5a6c7d;
      line-height: 1.6;
    }
  }
}

// 操作区域
.action-area {
  margin: 0 32rpx;
  
  .btn {
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
    
    &.btn-next {
      background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
      box-shadow: 0 8rpx 24rpx rgba(79, 172, 254, 0.35);
    }
    
    &.btn-finish {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      box-shadow: 0 8rpx 24rpx rgba(102, 126, 234, 0.35);
    }
  }
}

// 结果弹窗
.result-popup {
  padding: 60rpx 48rpx;
  text-align: center;
  min-width: 500rpx;
  
  .result-icon {
    font-size: 100rpx;
    margin-bottom: 24rpx;
  }
  
  .result-title {
    font-size: 36rpx;
    font-weight: 600;
    color: #2d3748;
    margin-bottom: 16rpx;
  }
  
  .result-score {
    font-size: 72rpx;
    font-weight: 700;
    color: #4facfe;
    margin-bottom: 16rpx;
  }
  
  .result-detail {
    font-size: 28rpx;
    color: #8b9aad;
    margin-bottom: 24rpx;
  }
  
  .result-bonus {
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
  
  .popup-actions {
    display: flex;
    gap: 24rpx;
    
    .btn {
      flex: 1;
      padding: 24rpx 0;
      border-radius: 20rpx;
      
      text {
        font-size: 28rpx;
        font-weight: 600;
      }
      
      &.btn-again {
        background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
        
        text {
          color: #fff;
        }
      }
      
      &.btn-back {
        background: #f1f5f9;
        
        text {
          color: #5a6c7d;
        }
      }
    }
  }
}
</style>