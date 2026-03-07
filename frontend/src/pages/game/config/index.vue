<template>
  <view class="config-page">
    <!-- 头部 -->
    <view class="header">
      <view class="header-left" @click="goBack">
        <u-icon name="arrow-left" color="#fff" size="40"></u-icon>
      </view>
      <view class="header-title">游戏设置</view>
      <view class="header-right">
        <u-button type="primary" size="small" text="保存" @click="saveConfig"></u-button>
      </view>
    </view>

    <!-- 游戏列表 -->
    <view class="game-list">
      <view 
        v-for="game in games" 
        :key="game.code"
        class="game-item"
        :class="{ active: currentGame === game.code }"
        @click="selectGame(game.code)"
      >
        <text class="game-icon">{{ game.icon }}</text>
        <text class="game-name">{{ game.name }}</text>
      </view>
    </view>

    <!-- 配置表单 -->
    <view class="config-form" v-if="currentGame">
      <view class="form-section">
        <view class="section-title">通用设置</view>
        
        <u-form :model="configData">
          <u-form-item label="游戏时长(分钟)">
            <u-slider v-model="configData.duration" min="1" max="60" show-value></u-slider>
          </u-form-item>
          
          <u-form-item label="参与人数">
            <u-number-box v-model="configData.maxPlayers" min="1" max="10"></u-number-box>
          </u-form-item>
          
          <u-form-item label="难度等级">
            <u-radio-group v-model="configData.difficulty">
              <u-radio label="简单" name="1"></u-radio>
              <u-radio label="中等" name="2"></u-radio>
              <u-radio label="困难" name="3"></u-radio>
            </u-radio-group>
          </u-form-item>
        </u-form>
      </view>

      <view class="form-section" v-if="currentGame === 'riddle'">
        <view class="section-title">猜谜语设置</view>
        
        <u-form :model="configData">
          <u-form-item label="题目数量">
            <u-number-box v-model="configData.questionCount" min="5" max="50"></u-number-box>
          </u-form-item>
          
          <u-form-item label="答题时间(秒)">
            <u-number-box v-model="configData.answerTime" min="10" max="300"></u-number-box>
          </u-form-item>
          
          <u-form-item label="启用提示">
            <u-switch v-model="configData.enableHint"></u-switch>
          </u-form-item>
          
          <u-form-item label="题目分类">
            <u-checkbox-group v-model="configData.categories">
              <u-checkbox label="动物" name="animal"></u-checkbox>
              <u-checkbox label="植物" name="plant"></u-checkbox>
              <u-checkbox label="物品" name="item"></u-checkbox>
              <u-checkbox label="成语" name="idiom"></u-checkbox>
            </u-checkbox-group>
          </u-form-item>
        </u-form>
      </view>

      <view class="form-section" v-if="currentGame === 'quiz'">
        <view class="section-title">问答竞赛设置</view>
        
        <u-form :model="configData">
          <u-form-item label="题目数量">
            <u-number-box v-model="configData.questionCount" min="5" max="50"></u-number-box>
          </u-form-item>
          
          <u-form-item label="每题时间(秒)">
            <u-number-box v-model="configData.questionTime" min="5" max="60"></u-number-box>
          </u-form-item>
          
          <u-form-item label="题目分类">
            <u-checkbox-group v-model="configData.categories">
              <u-checkbox label="常识" name="general"></u-checkbox>
              <u-checkbox label="历史" name="history"></u-checkbox>
              <u-checkbox label="科学" name="science"></u-checkbox>
              <u-checkbox label="生活" name="life"></u-checkbox>
            </u-checkbox-group>
          </u-form-item>
        </u-form>
      </view>

      <view class="form-section" v-if="currentGame === 'idiom'">
        <view class="section-title">成语接龙设置</view>
        
        <u-form :model="configData">
          <u-form-item label="时间限制(秒)">
            <u-number-box v-model="configData.timeLimit" min="10" max="120"></u-number-box>
          </u-form-item>
          
          <u-form-item label="启用提示">
            <u-switch v-model="configData.enableHint"></u-switch>
          </u-form-item>
          
          <u-form-item label="允许成语类型">
            <u-checkbox-group v-model="configData.idiomTypes">
              <u-checkbox label="四字成语" name="four"></u-checkbox>
              <u-checkbox label="五字成语" name="five"></u-checkbox>
            </u-checkbox-group>
          </u-form-item>
        </u-form>
      </view>

      <view class="form-section" v-if="currentGame === 'undercover'">
        <view class="section-title">谁是卧底设置</view>
        
        <u-form :model="configData">
          <u-form-item label="玩家人数">
            <u-number-box v-model="configData.playerCount" min="4" max="12"></u-number-box>
          </u-form-item>
          
          <u-form-item label="卧底数量">
            <u-number-box v-model="configData.undercoverCount" min="1" max="3"></u-number-box>
          </u-form-item>
          
          <u-form-item label="投票时间(秒)">
            <u-number-box v-model="configData.voteTime" min="30" max="180"></u-number-box>
          </u-form-item>
          
          <u-form-item label="词库类型">
            <u-radio-group v-model="configData.wordType">
              <u-radio label="日常物品" name="daily"></u-radio>
              <u-radio label="食物" name="food"></u-radio>
              <u-radio label="动物" name="animal"></u-radio>
              <u-radio label="电影" name="movie"></u-radio>
            </u-radio-group>
          </u-form-item>
        </u-form>
      </view>

      <view class="form-section" v-if="currentGame === 'truth_dare'">
        <view class="section-title">真心话大冒险设置</view>
        
        <u-form :model="configData">
          <u-form-item label="题库类型">
            <u-checkbox-group v-model="configData.questionTypes">
              <u-checkbox label="真心话" name="truth"></u-checkbox>
              <u-checkbox label="大冒险" name="dare"></u-checkbox>
            </u-checkbox-group>
          </u-form-item>
          
          <u-form-item label="难度筛选">
            <u-checkbox-group v-model="configData.difficulties">
              <u-checkbox label="轻度" name="1"></u-checkbox>
              <u-checkbox label="中度" name="2"></u-checkbox>
              <u-checkbox label="重度" name="3"></u-checkbox>
            </u-checkbox-group>
          </u-form-item>
        </u-form>
      </view>

      <view class="form-section" v-if="currentGame === 'draw_guess'">
        <view class="section-title">你画我猜设置</view>
        
        <u-form :model="configData">
          <u-form-item label="画图时间(秒)">
            <u-number-box v-model="configData.drawTime" min="30" max="180"></u-number-box>
          </u-form-item>
          
          <u-form-item label="词库选择">
            <u-radio-group v-model="configData.wordLibrary">
              <u-radio label="日常" name="daily"></u-radio>
              <u-radio label="食物" name="food"></u-radio>
              <u-radio label="动物" name="animal"></u-radio>
              <u-radio label="成语" name="idiom"></u-radio>
            </u-radio-group>
          </u-form-item>
          
          <u-form-item label="轮数">
            <u-number-box v-model="configData.rounds" min="3" max="15"></u-number-box>
          </u-form-item>
        </u-form>
      </view>

      <view class="form-section" v-if="currentGame === 'sports'">
        <view class="section-title">运动会设置</view>
        
        <u-form :model="configData">
          <u-form-item label="运动项目">
            <u-checkbox-group v-model="configData.sports">
              <u-checkbox label="跳绳" name="rope"></u-checkbox>
              <u-checkbox label="俯卧撑" name="pushup"></u-checkbox>
              <u-checkbox label="平板支撑" name="plank"></u-checkbox>
              <u-checkbox label="深蹲" name="squat"></u-checkbox>
            </u-checkbox-group>
          </u-form-item>
          
          <u-form-item label="计时模式">
            <u-switch v-model="configData.timerMode"></u-switch>
          </u-form-item>
        </u-form>
      </view>
    </view>

    <!-- 重置按钮 -->
    <view class="reset-section">
      <u-button type="error" text="恢复默认设置" @click="resetConfig"></u-button>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'

const games = [
  { code: 'riddle', name: '猜谜语', icon: '🎯' },
  { code: 'quiz', name: '问答竞赛', icon: '❓' },
  { code: 'idiom', name: '成语接龙', icon: '📚' },
  { code: 'undercover', name: '谁是卧底', icon: '🕵️' },
  { code: 'truth_dare', name: '真心话大冒险', icon: '🎭' },
  { code: 'draw_guess', name: '你画我猜', icon: '🎨' },
  { code: 'karaoke', name: '家庭K歌', icon: '🎤' },
  { code: 'sports', name: '运动会', icon: '🏃' },
  { code: 'cooking', name: '厨艺比拼', icon: '👨‍🍳' },
  { code: 'treasure', name: '家庭寻宝', icon: '🗺️' }
]

const currentGame = ref('riddle')

const configData = reactive({
  // 通用设置
  duration: 10,
  maxPlayers: 4,
  difficulty: '2',
  
  // 猜谜语
  questionCount: 10,
  answerTime: 30,
  enableHint: true,
  categories: ['animal', 'plant', 'item'],
  
  // 问答竞赛
  questionTime: 15,
  
  // 成语接龙
  timeLimit: 30,
  idiomTypes: ['four'],
  
  // 谁是卧底
  playerCount: 6,
  undercoverCount: 1,
  voteTime: 60,
  wordType: 'daily',
  
  // 真心话大冒险
  questionTypes: ['truth', 'dare'],
  difficulties: ['1', '2'],
  
  // 你画我猜
  drawTime: 60,
  wordLibrary: 'daily',
  rounds: 5,
  
  // 运动会
  sports: ['rope', 'pushup', 'plank'],
  timerMode: true
})

const selectGame = (code) => {
  currentGame.value = code
  loadConfig(code)
}

const loadConfig = (gameCode) => {
  const saved = uni.getStorageSync(`game_config_${gameCode}`)
  if (saved) {
    Object.assign(configData, saved)
  }
}

const saveConfig = () => {
  uni.setStorageSync(`game_config_${currentGame.value}`, JSON.parse(JSON.stringify(configData)))
  uni.showToast({ title: '保存成功', icon: 'success' })
}

const resetConfig = () => {
  uni.showModal({
    title: '确认重置',
    content: '确定要恢复默认设置吗？',
    success: (res) => {
      if (res.confirm) {
        // 重置当前游戏配置
        Object.keys(configData).forEach(key => {
          if (Array.isArray(configData[key])) {
            configData[key] = []
          } else if (typeof configData[key] === 'boolean') {
            configData[key] = true
          } else if (typeof configData[key] === 'number') {
            configData[key] = 10
          } else {
            configData[key] = ''
          }
        })
        
        // 设置默认值
        configData.duration = 10
        configData.maxPlayers = 4
        configData.difficulty = '2'
        configData.questionCount = 10
        configData.answerTime = 30
        configData.enableHint = true
        configData.categories = ['animal', 'plant', 'item']
        
        uni.removeStorageSync(`game_config_${currentGame.value}`)
        uni.showToast({ title: '已重置', icon: 'success' })
      }
    }
  })
}

const goBack = () => {
  uni.navigateBack()
}

onMounted(() => {
  loadConfig(currentGame.value)
})
</script>

<style lang="scss" scoped>
.config-page {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 40rpx;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 100rpx 32rpx 30rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  
  .header-left {
    width: 80rpx;
    height: 80rpx;
    display: flex;
    align-items: center;
    justify-content: center;
  }
  
  .header-title {
    font-size: 36rpx;
    font-weight: bold;
    color: #fff;
  }
  
  .header-right {
    width: 120rpx;
  }
}

.game-list {
  display: flex;
  padding: 20rpx;
  gap: 16rpx;
  overflow-x: auto;
  background: #fff;
  margin-bottom: 20rpx;
  
  .game-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 20rpx 24rpx;
    background: #f5f5f5;
    border-radius: 16rpx;
    min-width: 140rpx;
    
    &.active {
      background: #667eea;
      
      .game-name {
        color: #fff;
      }
    }
    
    .game-icon {
      font-size: 40rpx;
      margin-bottom: 8rpx;
    }
    
    .game-name {
      font-size: 24rpx;
      color: #666;
      white-space: nowrap;
    }
  }
}

.config-form {
  padding: 0 20rpx;
  
  .form-section {
    background: #fff;
    border-radius: 20rpx;
    padding: 30rpx;
    margin-bottom: 20rpx;
    
    .section-title {
      font-size: 32rpx;
      font-weight: bold;
      color: #333;
      margin-bottom: 20rpx;
      padding-bottom: 20rpx;
      border-bottom: 2rpx solid #f5f5f5;
    }
  }
}

.reset-section {
  padding: 20rpx 40rpx;
  margin-top: 40rpx;
}
</style>
