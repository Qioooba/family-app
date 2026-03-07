<template>
  <view class="game-page">
    <!-- 头部 -->
    <view class="header">
      <view class="header-left" @click="goBack">
        <u-icon name="arrow-left" color="#fff" size="40"></u-icon>
      </view>
      <view class="header-title">家庭寻宝</view>
      <view class="header-right" @click="showRules">
        <text>规则</text>
      </view>
    </view>

    <!-- 游戏状态 -->
    <view class="game-status">
      <view class="status-item">
        <text class="status-value">{{ currentLevel }}</text>
        <text class="status-label">当前关卡</text>
      </view>
      <view class="status-divider"></view>
      <view class="status-item">
        <text class="status-value">{{ foundTreasures.length }}/{{ totalTreasures }}</text>
        <text class="status-label">已找到</text>
      </view>
      <view class="status-divider"></view>
      <view class="status-item">
        <text class="status-value">{{ formatTime(elapsedTime) }}</text>
        <text class="status-label">用时</text>
      </view>
    </view>

    <!-- 地图区域 -->
    <view class="map-container">
      <view class="map-placeholder">
        <text class="map-icon">🗺️</text>
        <text class="map-text">寻宝地图</text>
        <text class="map-hint">根据线索寻找隐藏的宝藏</text>
      </view>
      
      <!-- 宝藏标记点 -->
      <view 
        v-for="(treasure, index) in treasures" 
        :key="index"
        class="treasure-marker"
        :class="{ found: treasure.found }"
        :style="{ left: treasure.x + '%', top: treasure.y + '%' }"
        @click="onMarkerClick(treasure)"
      >
        <text class="marker-icon">{{ treasure.found ? '✅' : '💎' }}</text>
      </view>
    </view>

    <!-- 线索卡片 -->
    <view class="clue-card" v-if="currentClue">
      <view class="clue-header">
        <text class="clue-title">线索 {{ currentLevel }}</text>
        <view class="clue-progress">
          <view 
            class="progress-bar"
            :style="{ width: (foundTreasures.length / totalTreasures * 100) + '%' }"
          ></view>
        </view>
      </view>
      
      <view class="clue-content">
        <text class="clue-text">{{ currentClue.text }}</text>
        <view class="clue-hint" v-if="currentClue.hint">
          <text class="hint-label">💡 提示：</text>
          <text class="hint-text">{{ currentClue.hint }}</text>
        </view>
      </view>
      
      <view class="clue-actions">
        <u-button type="primary" text="拍照验证" @click="verifyTreasure"></u-button>
        <u-button type="info" text="显示提示" @click="showHint"></u-button>
      </view>
    </view>

    <!-- 宝藏列表 -->
    <view class="treasure-list">
      <view class="section-title">
        <text>宝藏清单</text>
        <text class="progress-text">{{ foundTreasures.length }}/{{ totalTreasures }}</text>
      </view>
      
      <view class="treasure-items">
        <view 
          v-for="(treasure, index) in treasures" 
          :key="index"
          class="treasure-item"
          :class="{ found: treasure.found }"
        >
          <text class="item-icon">{{ treasure.found ? treasure.icon : '❓' }}</text>
          <view class="item-info">
            <text class="item-name">{{ treasure.found ? treasure.name : '未找到' }}</text>
            <text class="item-status">{{ treasure.found ? '已找到' : '寻找中...' }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 完成弹窗 -->
    <u-popup v-model:show="showComplete" mode="center" round="20">
      <view class="complete-popup">
        <text class="complete-icon">🎉</text>
        <text class="complete-title">恭喜你！</text>
        <text class="complete-desc">完成了所有寻宝任务</text>
        
        <view class="complete-stats">
          <view class="stat-item">
            <text class="stat-value">{{ formatTime(elapsedTime) }}</text>
            <text class="stat-label">总用时</text>
          </view>
          <view class="stat-item">
            <text class="stat-value">{{ totalTreasures }}</text>
            <text class="stat-label">找到宝藏</text>
          </view>
        </view>
        
        <u-button type="primary" text="下一关" @click="nextLevel"></u-button>
        <u-button type="info" text="返回大厅" @click="goBack"></u-button>
      </view>
    </u-popup>

    <!-- 验证弹窗 -->
    <u-popup v-model:show="showVerify" mode="center" round="20">
      <view class="verify-popup">
        <text class="verify-title">拍照验证</text>
        
        <view class="verify-content">
          <view class="camera-area" @click="takePhoto">
            <image v-if="verifyPhoto" :src="verifyPhoto" mode="aspectFill"></image>
            <view v-else class="camera-placeholder">
              <u-icon name="camera" color="#999" size="64"></u-icon>
              <text>点击拍照</text>
            </view>
          </view>
          
          <text class="verify-hint">请拍摄找到的宝藏物品</text>
        </view>
        
        <view class="verify-actions">
          <u-button type="info" text="取消" @click="showVerify = false"></u-button>
          <u-button type="success" text="确认" @click="confirmVerify"></u-button>
        </view>
      </view>
    </u-popup>

    <!-- 规则弹窗 -->
    <u-popup v-model:show="showRulePopup" mode="center" round="20">
      <view class="rules-popup">
        <view class="rules-title">游戏规则</view>
        <view class="rules-content">
          <text class="rule-item">1. 根据提供的线索寻找隐藏的宝藏</text>
          <text class="rule-item">2. 找到宝藏后拍照验证</text>
          <text class="rule-item">3. 可以使用提示功能获得额外线索</text>
          <text class="rule-item">4. 完成所有关卡后可解锁新地图</text>
          <text class="rule-item">5. 记录用时，与家人比拼速度</text>
        </view>
        <u-button type="primary" text="知道了" @click="showRulePopup = false"></u-button>
      </view>
    </u-popup>
  </view>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'

const currentLevel = ref(1)
const elapsedTime = ref(0)
const timerInterval = ref(null)
const showComplete = ref(false)
const showVerify = ref(false)
const showRulePopup = ref(false)
const verifyPhoto = ref('')
const currentClueIndex = ref(0)
const selectedTreasure = ref(null)

// 宝藏数据
const treasures = ref([
  { id: 1, name: '神秘盒子', icon: '📦', found: false, x: 20, y: 30 },
  { id: 2, name: '金币', icon: '🪙', found: false, x: 60, y: 20 },
  { id: 3, name: '宝石', icon: '💎', found: false, x: 40, y: 60 },
  { id: 4, name: '钥匙', icon: '🗝️', found: false, x: 80, y: 50 }
])

// 线索数据
const clues = [
  { text: '在沙发下面寻找第一个线索', hint: '看看客厅的沙发底下' },
  { text: '在书架的第三层有一个秘密', hint: '书房的书架上' },
  { text: '厨房的冰箱里藏着什么？', hint: '打开冰箱看看' },
  { text: '最后一个藏在床底下', hint: '主卧的床下' }
]

// 当前线索
const currentClue = computed(() => {
  if (currentClueIndex.value < clues.length) {
    return clues[currentClueIndex.value]
  }
  return null
})

// 已找到的宝藏
const foundTreasures = computed(() => {
  return treasures.value.filter(t => t.found)
})

// 宝藏总数
const totalTreasures = computed(() => {
  return treasures.value.length
})

// 格式化时间
const formatTime = (seconds) => {
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
}

// 点击标记
const onMarkerClick = (treasure) => {
  if (treasure.found) {
    uni.showToast({ title: '已经找到过了', icon: 'none' })
    return
  }
  selectedTreasure.value = treasure
}

// 验证宝藏
const verifyTreasure = () => {
  showVerify.value = true
}

// 拍照
const takePhoto = () => {
  uni.chooseImage({
    count: 1,
    sourceType: ['camera'],
    success: (res) => {
      verifyPhoto.value = res.tempFilePaths[0]
    }
  })
}

// 确认验证
const confirmVerify = () => {
  if (!verifyPhoto.value) {
    uni.showToast({ title: '请先拍照', icon: 'none' })
    return
  }
  
  // 标记宝藏为已找到
  if (selectedTreasure.value) {
    selectedTreasure.value.found = true
  }
  
  // 找到当前关卡对应的宝藏
  const currentTreasure = treasures.value[currentClueIndex.value]
  if (currentTreasure) {
    currentTreasure.found = true
  }
  
  currentClueIndex.value++
  
  showVerify.value = false
  verifyPhoto.value = ''
  
  uni.showToast({ title: '验证成功！', icon: 'success' })
  
  // 检查是否完成所有宝藏
  if (foundTreasures.value.length === totalTreasures.value) {
    clearInterval(timerInterval.value)
    setTimeout(() => {
      showComplete.value = true
    }, 500)
  }
}

// 显示提示
const showHint = () => {
  if (currentClue.value) {
    uni.showModal({
      title: '提示',
      content: currentClue.value.hint,
      showCancel: false
    })
  }
}

// 下一关
const nextLevel = () => {
  currentLevel.value++
  elapsedTime.value = 0
  currentClueIndex.value = 0
  showComplete.value = false
  
  // 重置宝藏状态
  treasures.value.forEach(t => {
    t.found = false
  })
  
  // 重新开始计时
  startTimer()
}

// 开始计时
const startTimer = () => {
  timerInterval.value = setInterval(() => {
    elapsedTime.value++
  }, 1000)
}

// 返回
const goBack = () => {
  uni.navigateBack()
}

// 显示规则
const showRules = () => {
  showRulePopup.value = true
}

onMounted(() => {
  startTimer()
})

onUnmounted(() => {
  clearInterval(timerInterval.value)
})
</script>

<style lang="scss" scoped>
.game-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
  padding-bottom: 40rpx;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 100rpx 32rpx 30rpx;
  
  .header-left, .header-right {
    width: 80rpx;
    height: 80rpx;
    display: flex;
    align-items: center;
    justify-content: center;
  }
  
  .header-right {
    text {
      color: #fff;
      font-size: 28rpx;
    }
  }
  
  .header-title {
    font-size: 36rpx;
    font-weight: bold;
    color: #fff;
  }
}

.game-status {
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 32rpx 20rpx;
  padding: 30rpx;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 24rpx;
  
  .status-item {
    flex: 1;
    text-align: center;
    
    .status-value {
      font-size: 48rpx;
      font-weight: bold;
      color: #11998e;
      display: block;
    }
    
    .status-label {
      font-size: 24rpx;
      color: #999;
      margin-top: 8rpx;
    }
  }
  
  .status-divider {
    width: 2rpx;
    height: 60rpx;
    background: #e5e5e5;
  }
}

.map-container {
  margin: 0 32rpx 20rpx;
  height: 400rpx;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 24rpx;
  position: relative;
  overflow: hidden;
  
  .map-placeholder {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    text-align: center;
    
    .map-icon {
      font-size: 80rpx;
      display: block;
      margin-bottom: 16rpx;
    }
    
    .map-text {
      font-size: 36rpx;
      font-weight: bold;
      color: #333;
      display: block;
      margin-bottom: 10rpx;
    }
    
    .map-hint {
      font-size: 24rpx;
      color: #999;
    }
  }
}

.treasure-marker {
  position: absolute;
  width: 80rpx;
  height: 80rpx;
  transform: translate(-50%, -50%);
  display: flex;
  align-items: center;
  justify-content: center;
  
  &.found {
    opacity: 0.5;
  }
  
  .marker-icon {
    font-size: 48rpx;
  }
}

.clue-card {
  margin: 0 32rpx 20rpx;
  padding: 30rpx;
  background: #fff;
  border-radius: 24rpx;
  
  .clue-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20rpx;
    
    .clue-title {
      font-size: 32rpx;
      font-weight: bold;
      color: #333;
    }
    
    .clue-progress {
      width: 200rpx;
      height: 16rpx;
      background: #f5f5f5;
      border-radius: 8rpx;
      overflow: hidden;
      
      .progress-bar {
        height: 100%;
        background: linear-gradient(90deg, #11998e, #38ef7d);
        border-radius: 8rpx;
        transition: width 0.3s;
      }
    }
  }
  
  .clue-content {
    margin-bottom: 20rpx;
    
    .clue-text {
      font-size: 32rpx;
      color: #333;
      line-height: 1.6;
      display: block;
      margin-bottom: 20rpx;
    }
    
    .clue-hint {
      padding: 20rpx;
      background: #fffbe6;
      border-radius: 16rpx;
      border-left: 8rpx solid #faad14;
      
      .hint-label {
        font-size: 26rpx;
        color: #faad14;
        margin-right: 10rpx;
      }
      
      .hint-text {
        font-size: 26rpx;
        color: #666;
      }
    }
  }
  
  .clue-actions {
    display: flex;
    gap: 20rpx;
    
    ::v-deep .u-button {
      flex: 1;
    }
  }
}

.treasure-list {
  margin: 0 32rpx;
  padding: 30rpx;
  background: #fff;
  border-radius: 24rpx;
  
  .section-title {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20rpx;
    
    text {
      font-size: 32rpx;
      font-weight: bold;
      color: #333;
    }
    
    .progress-text {
      font-size: 28rpx;
      color: #11998e;
    }
  }
  
  .treasure-items {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 20rpx;
  }
  
  .treasure-item {
    text-align: center;
    padding: 20rpx;
    background: #f5f5f5;
    border-radius: 16rpx;
    
    &.found {
      background: #f6ffed;
      border: 2rpx solid #52c41a;
    }
    
    .item-icon {
      font-size: 48rpx;
      display: block;
      margin-bottom: 10rpx;
    }
    
    .item-info {
      .item-name {
        font-size: 24rpx;
        color: #333;
        display: block;
        margin-bottom: 4rpx;
      }
      
      .item-status {
        font-size: 20rpx;
        color: #999;
      }
    }
  }
}

.complete-popup {
  padding: 60rpx;
  text-align: center;
  width: 600rpx;
  
  .complete-icon {
    font-size: 120rpx;
    display: block;
    margin-bottom: 20rpx;
  }
  
  .complete-title {
    font-size: 48rpx;
    font-weight: bold;
    color: #11998e;
    display: block;
    margin-bottom: 10rpx;
  }
  
  .complete-desc {
    font-size: 28rpx;
    color: #666;
    display: block;
    margin-bottom: 40rpx;
  }
  
  .complete-stats {
    display: flex;
    justify-content: center;
    gap: 60rpx;
    margin-bottom: 40rpx;
    
    .stat-item {
      text-align: center;
      
      .stat-value {
        font-size: 40rpx;
        font-weight: bold;
        color: #11998e;
        display: block;
      }
      
      .stat-label {
        font-size: 24rpx;
        color: #999;
        margin-top: 8rpx;
      }
    }
  }
  
  ::v-deep .u-button {
    margin-bottom: 20rpx;
  }
}

.verify-popup {
  padding: 40rpx;
  width: 600rpx;
  
  .verify-title {
    font-size: 36rpx;
    font-weight: bold;
    text-align: center;
    margin-bottom: 30rpx;
    color: #333;
  }
  
  .verify-content {
    margin-bottom: 30rpx;
    
    .camera-area {
      width: 100%;
      height: 400rpx;
      background: #f5f5f5;
      border-radius: 20rpx;
      margin-bottom: 20rpx;
      overflow: hidden;
      
      image {
        width: 100%;
        height: 100%;
      }
      
      .camera-placeholder {
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        height: 100%;
        
        text {
          font-size: 28rpx;
          color: #999;
          margin-top: 20rpx;
        }
      }
    }
    
    .verify-hint {
      text-align: center;
      font-size: 28rpx;
      color: #666;
    }
  }
  
  .verify-actions {
    display: flex;
    gap: 20rpx;
    
    ::v-deep .u-button {
      flex: 1;
    }
  }
}

.rules-popup {
  padding: 40rpx;
  width: 600rpx;
  
  .rules-title {
    font-size: 36rpx;
    font-weight: bold;
    text-align: center;
    margin-bottom: 30rpx;
    color: #333;
  }
  
  .rules-content {
    margin-bottom: 30rpx;
    
    .rule-item {
      display: block;
      font-size: 28rpx;
      color: #666;
      line-height: 1.8;
      margin-bottom: 10rpx;
    }
  }
}
</style>
