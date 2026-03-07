<template>
  <view class="game-page">
    <!-- 头部 -->
    <view class="header">
      <view class="header-left" @click="goBack">
        <u-icon name="arrow-left" color="#fff" size="40"></u-icon>
      </view>
      <view class="header-title">你画我猜</view>
      <view class="header-right" @click="showRules">
        <text>规则</text>
      </view>
    </view>

    <!-- 游戏模式选择 -->
    <view class="mode-select" v-if="gameMode === ''">
      <view class="section-title">选择模式</view>
      
      <view class="mode-card" @click="selectMode('draw')">
        <view class="mode-icon">🎨</view>
        <view class="mode-info">
          <text class="mode-name">我来画</text>
          <text class="mode-desc">选择一个词并画出来让家人猜</text>
        </view>
        <u-icon name="arrow-right" color="#ccc" size="32"></u-icon>
      </view>
      
      <view class="mode-card" @click="selectMode('guess')">
        <view class="mode-icon">🔍</view>
        <view class="mode-info">
          <text class="mode-name">我来猜</text>
          <text class="mode-desc">看家人画画并猜出是什么</text>
        </view>
        <u-icon name="arrow-right" color="#ccc" size="32"></u-icon>
      </view>
    </view>

    <!-- 绘画模式 -->
    <view class="draw-section" v-if="gameMode === 'draw'">
      <!-- 题目展示 -->
      <view class="word-display" v-if="!isDrawing">
        <view class="word-card">
          <text class="word-label">你的题目</text>
          <text class="word-text">{{ currentWord }}</text>
          <text class="word-tip">点击开始绘画，让家人猜这个词</text>
        </view>
        <view class="btn btn-start-draw" @click="startDrawing">
          <text>开始绘画</text>
        </view>
        <view class="btn btn-change" @click="changeWord">
          <text>换一个词</text>
        </view>
      </view>

      <!-- 画板 -->
      <view class="canvas-area" v-else
        @touchstart="startDraw"
        @touchmove="draw"
        @touchend="endDraw"
      >
        <view class="canvas-tools">
          <view class="color-picker">
            <view 
              v-for="color in colors" 
              :key="color"
              class="color-item"
              :style="{ background: color }"
              :class="{ active: currentColor === color }"
              @click="currentColor = color"
            />
          </view>
          <view class="tool-btns">
            <view class="tool-btn" @click="clearCanvas">
              <text>清空</text>
            </view>
            <view class="tool-btn" @click="undo">
              <text>撤销</text>
            </view>
            <view class="tool-btn" @click="eraserMode = !eraserMode">
              <text>{{ eraserMode ? '画笔' : '橡皮' }}</text>
            </view>
          </view>
        </view>
        
        <canvas 
          canvas-id="drawCanvas" 
          class="draw-canvas"
          :style="{ width: canvasWidth + 'px', height: canvasHeight + 'px' }"
        />
        
        <view class="canvas-actions">
          <view class="word-show">画: {{ currentWord }}</view>
          <view class="btn btn-done" @click="finishDrawing">
            <text>完成</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 猜谜模式 -->
    <view class="guess-section" v-if="gameMode === 'guess'">
      <!-- 画作展示 -->
      <view class="drawing-display">
        <view class="drawing-placeholder" v-if="!hasDrawing">
          <text class="placeholder-icon">🎨</text>
          <text class="placeholder-text">等待画作...</text>
          <text class="placeholder-tip">请让家人先开始绘画</text>
        </view>
        
        <view class="drawing-image" v-else
          :style="{ backgroundImage: `url(${drawingImage})` }"
        />
      </view>

      <!-- 猜测输入 -->
      <view class="guess-input-area">
        <view class="input-wrapper">
          <input 
            v-model="guessInput"
            class="guess-input"
            placeholder="猜猜这是什么？"
            confirm-type="done"
            @confirm="submitGuess"
          />
        </view>
        <view class="btn btn-guess" @click="submitGuess">
          <text>猜测</text>
        </view>
      </view>

      <!-- 提示区域 -->
      <view class="hints-area">
        <view class="hint-btn" @click="getHint">
          <text>💡 提示 ({{ hintsUsed }}/3)</text>
        </view>
        <view class="hint-text" v-if="currentHint">提示: {{ currentHint }}</view>
      </view>
    </view>

    <!-- 结果弹窗 -->
    <u-popup :show="showResult" mode="center" round="20">
      <view class="result-popup">
        <view class="result-icon">{{ isCorrect ? '🎉' : '😅' }}</view>
        <view class="result-title">{{ isCorrect ? '猜对了！' : '继续加油' }}</view>
        
        <view class="result-word" v-if="!isCorrect">
          <text>答案是: {{ currentWord }}</text>
        </view>
        
        <view class="result-score" v-if="isCorrect">
          <text>+{{ earnedPoints }} 积分</text>
        </view>
        
        <view class="popup-actions">
          <view class="btn btn-again" @click="playAgain">
            <text>再来一局</text>
          </view>
          <view class="btn btn-back" @click="backToMode">
            <text>返回选择</text>
          </view>
        </view>
      </view>
    </u-popup>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { gameApi } from '../../../api/game.js'

// 游戏模式
const gameMode = ref('')
const currentWord = ref('')
const isDrawing = ref(false)
const hasDrawing = ref(false)
const drawingImage = ref('')

// 画板相关
const canvasWidth = ref(300)
const canvasHeight = ref(400)
const currentColor = ref('#000000')
const eraserMode = ref(false)
const colors = ['#000000', '#e74c3c', '#3498db', '#2ecc71', '#f39c12', '#9b59b6', '#1abc9c', '#e91e63']

// 猜测相关
const guessInput = ref('')
const hintsUsed = ref(0)
const currentHint = ref('')

// 结果相关
const showResult = ref(false)
const isCorrect = ref(false)
const earnedPoints = ref(0)

// 词汇库
const wordList = [
  '苹果', '香蕉', '猫咪', '狗狗', '太阳', '月亮', '汽车', '飞机',
  '房子', '大树', '花朵', '星星', '雨伞', '电话', '电脑', '书本',
  '杯子', '椅子', '桌子', '电视', '冰箱', '钥匙', '手表', '眼镜',
  '帽子', '鞋子', '袜子', '手套', '围巾', '书包', '铅笔', '橡皮',
  '大象', '老虎', '狮子', '熊猫', '兔子', '小鸟', '鱼儿', '蝴蝶'
]

// 返回
const goBack = () => {
  uni.navigateBack()
}

// 显示规则
const showRules = () => {
  uni.showModal({
    title: '你画我猜规则',
    content: '一人画画，其他人猜测。\n画家选择一个词语并画出来，\n猜词者根据画作猜测答案。',
    showCancel: false
  })
}

// 选择模式
const selectMode = (mode) => {
  gameMode.value = mode
  if (mode === 'draw') {
    currentWord.value = getRandomWord()
  }
}

// 获取随机词
const getRandomWord = () => {
  return wordList[Math.floor(Math.random() * wordList.length)]
}

// 换词
const changeWord = () => {
  currentWord.value = getRandomWord()
}

// 开始绘画
const startDrawing = () => {
  isDrawing.value = true
  // 获取画布尺寸
  const systemInfo = uni.getSystemInfoSync()
  canvasWidth.value = systemInfo.windowWidth - 64
  canvasHeight.value = 500
}

// 绘画操作
let ctx = null
let isDrawingNow = false
let lastX = 0
let lastY = 0

onMounted(() => {
  ctx = uni.createCanvasContext('drawCanvas')
  ctx.setLineCap('round')
  ctx.setLineJoin('round')
  ctx.setLineWidth(5)
})

const startDraw = (e) => {
  isDrawingNow = true
  const touch = e.touches[0]
  lastX = touch.x
  lastY = touch.y
}

const draw = (e) => {
  if (!isDrawingNow) return
  
  const touch = e.touches[0]
  const x = touch.x
  const y = touch.y
  
  ctx.beginPath()
  ctx.moveTo(lastX, lastY)
  ctx.lineTo(x, y)
  
  if (eraserMode.value) {
    ctx.setStrokeStyle('#ffffff')
    ctx.setLineWidth(20)
  } else {
    ctx.setStrokeStyle(currentColor.value)
    ctx.setLineWidth(5)
  }
  
  ctx.stroke()
  ctx.draw(true)
  
  lastX = x
  lastY = y
}

const endDraw = () => {
  isDrawingNow = false
}

// 清空画布
const clearCanvas = () => {
  ctx.clearRect(0, 0, canvasWidth.value, canvasHeight.value)
  ctx.draw()
}

// 撤销
const undo = () => {
  uni.showToast({ title: '撤销功能开发中', icon: 'none' })
}

// 完成绘画
const finishDrawing = () => {
  uni.canvasToTempFilePath({
    canvasId: 'drawCanvas',
    success: (res) => {
      drawingImage.value = res.tempFilePath
      hasDrawing.value = true
      uni.showToast({ title: '画作已保存', icon: 'success' })
    }
  })
}

// 提交猜测
const submitGuess = () => {
  if (!guessInput.value.trim()) {
    uni.showToast({ title: '请输入猜测', icon: 'none' })
    return
  }
  
  const guess = guessInput.value.trim()
  isCorrect.value = guess === currentWord.value
  
  if (isCorrect.value) {
    // 计算积分
    earnedPoints.value = Math.max(10 - hintsUsed.value * 2, 5)
    addPoints(earnedPoints.value)
  }
  
  showResult.value = true
}

// 获取提示
const getHint = () => {
  if (hintsUsed.value >= 3) {
    uni.showToast({ title: '提示次数已用完', icon: 'none' })
    return
  }
  
  hintsUsed.value++
  const word = currentWord.value
  
  if (hintsUsed.value === 1) {
    currentHint.value = `这个词有 ${word.length} 个字`
  } else if (hintsUsed.value === 2) {
    currentHint.value = `第一个字是 "${word.charAt(0)}"`
  } else {
    currentHint.value = `这个词是 "${word}"`
  }
}

// 添加积分
const addPoints = async (points) => {
  try {
    await gameApi.addPoints({
      gameType: 'draw-guess',
      points: points,
      description: '你画我猜 猜对答案'
    })
  } catch (e) {
    console.error('添加积分失败', e)
  }
}

// 再来一局
const playAgain = () => {
  showResult.value = false
  guessInput.value = ''
  hintsUsed.value = 0
  currentHint.value = ''
  isCorrect.value = false
  
  if (gameMode.value === 'guess') {
    hasDrawing.value = false
    drawingImage.value = ''
  } else {
    isDrawing.value = false
    clearCanvas()
    currentWord.value = getRandomWord()
  }
}

// 返回模式选择
const backToMode = () => {
  gameMode.value = ''
  showResult.value = false
  isDrawing.value = false
  hasDrawing.value = false
  guessInput.value = ''
  hintsUsed.value = 0
  currentHint.value = ''
}
</script>

<style lang="scss" scoped>
.game-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #f39c12 0%, #f8f9fc 30%);
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

// 模式选择
.mode-select {
  margin: 0 32rpx;
  
  .section-title {
    font-size: 32rpx;
    font-weight: 600;
    color: #2d3748;
    margin-bottom: 24rpx;
  }
  
  .mode-card {
    display: flex;
    align-items: center;
    background: #fff;
    border-radius: 24rpx;
    padding: 32rpx;
    margin-bottom: 24rpx;
    box-shadow: 0 8rpx 24rpx rgba(0,0,0,0.08);
    
    .mode-icon {
      width: 100rpx;
      height: 100rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      background: linear-gradient(135deg, #f39c12 0%, #e67e22 100%);
      border-radius: 24rpx;
      font-size: 56rpx;
      margin-right: 24rpx;
    }
    
    .mode-info {
      flex: 1;
      
      .mode-name {
        font-size: 36rpx;
        font-weight: 600;
        color: #2d3748;
        display: block;
        margin-bottom: 8rpx;
      }
      
      .mode-desc {
        font-size: 26rpx;
        color: #8b9aad;
      }
    }
  }
}

// 绘画模式
.draw-section {
  margin: 0 32rpx;
  
  .word-display {
    text-align: center;
    
    .word-card {
      background: #fff;
      border-radius: 32rpx;
      padding: 80rpx 48rpx;
      margin-bottom: 32rpx;
      box-shadow: 0 8rpx 32rpx rgba(0,0,0,0.1);
      
      .word-label {
        display: block;
        font-size: 28rpx;
        color: #8b9aad;
        margin-bottom: 24rpx;
      }
      
      .word-text {
        display: block;
        font-size: 80rpx;
        font-weight: 700;
        color: #f39c12;
        margin-bottom: 24rpx;
      }
      
      .word-tip {
        display: block;
        font-size: 26rpx;
        color: #5a6c7d;
      }
    }
    
    .btn {
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 28rpx 0;
      border-radius: 24rpx;
      margin-bottom: 24rpx;
      
      text {
        font-size: 32rpx;
        font-weight: 600;
      }
      
      &.btn-start-draw {
        background: linear-gradient(135deg, #f39c12 0%, #e67e22 100%);
        box-shadow: 0 8rpx 24rpx rgba(243, 156, 18, 0.35);
        
        text {
          color: #fff;
        }
      }
      
      &.btn-change {
        background: #fff;
        
        text {
          color: #5a6c7d;
        }
      }
    }
  }
  
  .canvas-area {
    background: #fff;
    border-radius: 24rpx;
    overflow: hidden;
    box-shadow: 0 8rpx 32rpx rgba(0,0,0,0.1);
    
    .canvas-tools {
      padding: 20rpx;
      background: #f8f9fc;
      
      .color-picker {
        display: flex;
        justify-content: center;
        gap: 16rpx;
        margin-bottom: 20rpx;
        
        .color-item {
          width: 56rpx;
          height: 56rpx;
          border-radius: 50%;
          border: 4rpx solid transparent;
          
          &.active {
            border-color: #2d3748;
            transform: scale(1.1);
          }
        }
      }
      
      .tool-btns {
        display: flex;
        justify-content: center;
        gap: 24rpx;
        
        .tool-btn {
          padding: 16rpx 32rpx;
          background: #fff;
          border-radius: 16rpx;
          
          text {
            font-size: 26rpx;
            color: #5a6c7d;
          }
        }
      }
    }
    
    .draw-canvas {
      background: #fff;
      margin: 0 auto;
    }
    
    .canvas-actions {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 20rpx 32rpx;
      background: #f8f9fc;
      
      .word-show {
        font-size: 28rpx;
        color: #f39c12;
        font-weight: 600;
      }
      
      .btn-done {
        padding: 16rpx 40rpx;
        background: linear-gradient(135deg, #f39c12 0%, #e67e22 100%);
        border-radius: 16rpx;
        
        text {
          font-size: 28rpx;
          color: #fff;
          font-weight: 600;
        }
      }
    }
  }
}

// 猜测模式
.guess-section {
  margin: 0 32rpx;
  
  .drawing-display {
    background: #fff;
    border-radius: 24rpx;
    height: 500rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 32rpx;
    box-shadow: 0 8rpx 32rpx rgba(0,0,0,0.1);
    overflow: hidden;
    
    .drawing-placeholder {
      text-align: center;
      
      .placeholder-icon {
        display: block;
        font-size: 100rpx;
        margin-bottom: 24rpx;
      }
      
      .placeholder-text {
        display: block;
        font-size: 36rpx;
        color: #2d3748;
        margin-bottom: 12rpx;
        font-weight: 600;
      }
      
      .placeholder-tip {
        font-size: 26rpx;
        color: #8b9aad;
      }
    }
    
    .drawing-image {
      width: 100%;
      height: 100%;
      background-size: contain;
      background-position: center;
      background-repeat: no-repeat;
    }
  }
  
  .guess-input-area {
    display: flex;
    gap: 16rpx;
    margin-bottom: 24rpx;
    
    .input-wrapper {
      flex: 1;
      background: #fff;
      border-radius: 24rpx;
      padding: 8rpx;
      box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.06);
      
      .guess-input {
        height: 96rpx;
        padding: 0 32rpx;
        font-size: 32rpx;
        color: #2d3748;
        text-align: center;
      }
    }
    
    .btn-guess {
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 0 48rpx;
      background: linear-gradient(135deg, #f39c12 0%, #e67e22 100%);
      border-radius: 24rpx;
      box-shadow: 0 8rpx 24rpx rgba(243, 156, 18, 0.35);
      
      text {
        font-size: 30rpx;
        font-weight: 600;
        color: #fff;
      }
    }
  }
  
  .hints-area {
    text-align: center;
    
    .hint-btn {
      display: inline-block;
      padding: 20rpx 40rpx;
      background: #fff;
      border-radius: 32rpx;
      margin-bottom: 16rpx;
      box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.06);
      
      text {
        font-size: 28rpx;
        color: #f6ad55;
      }
    }
    
    .hint-text {
      font-size: 28rpx;
      color: #5a6c7d;
      background: rgba(246, 173, 85, 0.1);
      padding: 20rpx 32rpx;
      border-radius: 16rpx;
    }
  }
}

// 结果弹窗
.result-popup {
  padding: 60rpx 48rpx;
  text-align: center;
  min-width: 480rpx;
  
  .result-icon {
    font-size: 100rpx;
    margin-bottom: 24rpx;
  }
  
  .result-title {
    font-size: 44rpx;
    font-weight: 700;
    color: #2d3748;
    margin-bottom: 16rpx;
  }
  
  .result-word {
    font-size: 32rpx;
    color: #8b9aad;
    margin-bottom: 16rpx;
  }
  
  .result-score {
    font-size: 40rpx;
    font-weight: 700;
    color: #f39c12;
    margin-bottom: 32rpx;
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
        background: linear-gradient(135deg, #f39c12 0%, #e67e22 100%);
        
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