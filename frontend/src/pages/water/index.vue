<template>
  <view class="water-page">
    <!-- 导航栏 -->
    <view class="nav-bar">
      <view class="back-btn" @click="goBack">
        <u-icon name="arrow-left" size="40" color="#333"></u-icon>
      </view>
    </view>

    <!-- 主内容区 -->
    <view class="content">
      <!-- 日期显示 -->
      <view class="date-section">
        <text class="date-text">{{ todayText }}</text>
        <text class="week-text">{{ weekText }}</text>
      </view>

      <!-- 水杯动画区域 -->
      <view class="water-cup-section">
        <view class="cup-container">
          <view class="cup">
            <view class="water" :style="{ height: waterPercent + '%' }">
              <view class="water-wave"></view>
            </view>
            <view class="cup-markings">
              <view class="marking" style="top: 25%"></view>
              <view class="marking" style="top: 50%"></view>
              <view class="marking" style="top: 75%"></view>
            </view>
          </view>
          <view class="cup-stand"></view>
        </view>

        <!-- 喝水数据 -->
        <view class="water-stats">
          <view class="stats-item">
            <text class="stats-num">{{ todayAmount }}</text>
            <text class="stats-unit">ml</text>
          </view>
          <text class="stats-divider">/</text>
          <view class="stats-item target-item" @click="showSettings">
            <text class="stats-num target">{{ targetAmount }}</text>
            <text class="stats-unit">ml 目标</text>
            <view class="settings-icon-small">
              <u-icon name="setting" size="24" color="#999"></u-icon>
            </view>
          </view>
        </view>

        <!-- 完成度 -->
        <view class="progress-text">
          <text :class="{ 'completed': waterPercent >= 100 }">
            {{ waterPercent >= 100 ? '今日目标已达成! 🎉' : `已完成 ${waterPercent}%` }}
          </text>
        </view>
      </view>

      <!-- 快捷喝水按钮 -->
      <view class="quick-add-section">
        <text class="section-title">快速记录</text>
        <view class="quick-buttons">
          <view
            v-for="amount in quickAmounts"
            :key="amount"
            class="quick-btn"
            :class="{ 'active': selectedAmount === amount && !isCustomAmount }"
            @click="selectQuickAmount(amount)"
          >
            <view class="water-icon">💧</view>
            <text>{{ amount }}ml</text>
          </view>
          <!-- 自定义按钮 -->
          <view
            class="quick-btn custom-btn"
            :class="{ 'active': isCustomAmount }"
            @click="toggleCustomAmount"
          >
            <view class="water-icon">✏️</view>
            <text>自定义</text>
          </view>
        </view>
        
        <!-- 自定义输入框 -->
        <view v-if="isCustomAmount" class="custom-amount-section">
          <view class="custom-input-wrapper">
            <input
              type="number"
              v-model="customAmountInput"
              placeholder="输入毫升数"
              class="custom-amount-input"
              @focus="selectedAmount = 0"
            />
            <text class="custom-unit">ml</text>
          </view>
          <text class="custom-hint-tip">输入后点击"记录喝水"即可</text>
        </view>
        <button class="add-btn" @click="recordWater">
          <view class="btn-icon">💧</view>
          <text>记录喝水</text>
        </button>
      </view>

      <!-- 今日记录 -->
      <view class="records-section">
        <view class="section-header">
          <text class="section-title">今日记录</text>
          <view class="header-actions">
            <text class="record-count">{{ records.length }} 次</text>
            <text class="history-link" @click="showHistory">查看历史></text>
          </view>
        </view>

        <view v-if="records.length === 0" class="empty-state">
          <u-icon name="clock" size="80" color="#ddd"></u-icon>
          <text>还没有喝水记录，快来打卡吧~</text>
        </view>

        <view v-else class="records-list">
          <view
            v-for="(record, index) in records"
            :key="record.id || index"
            class="record-item"
          >
            <view class="record-icon">
              <u-icon name="clock" size="36" color="#5B8FF9"></u-icon>
            </view>
            <view class="record-info">
              <text class="record-amount">+{{ record.amount }}ml</text>
              <text class="record-time">{{ formatTime(record.recordTime) }}</text>
            </view>
            <view class="record-progress">
              <text>{{ calculateProgress(record.amount) }}%</text>
            </view>
            <view class="record-delete-wrapper" @click="deleteRecord(record.id)">
              <view class="record-delete-btn" title="删除记录">
                <u-icon name="trash" size="28" color="#fff"></u-icon>
                <text class="delete-text">删除</text>
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- 健康提示 -->
      <view class="tips-section">
        <view class="tip-card">
          <u-icon name="info-circle" size="40" color="#5AD8A6"></u-icon>
          <view class="tip-content">
            <text class="tip-title">健康小贴士</text>
            <text class="tip-text">{{ currentTip }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 目标设置弹窗 -->
    <view v-if="settingsVisible" class="modal-overlay" @click="closeSettings">
      <view class="modal-content" @click.stop>
        <view class="modal-header">
          <text>设置目标</text>
          <text class="close-btn" @click="closeSettings">✕</text>
        </view>

        <view class="target-options">
          <!-- 预设选项 -->
          <view
            v-for="target in targetOptions"
            :key="target"
            class="target-option"
            :class="{ 'active': tempTarget === target && !isCustomTarget }"
            @click="selectPresetTarget(target)"
          >
            <text class="target-value">{{ target }}ml</text>
            <text class="target-desc">{{ getTargetDesc(target) }}</text>
          </view>
          
          <!-- 自定义选项 -->
          <view 
            class="target-option" 
            :class="{ 'active': isCustomTarget }"
            @click="isCustomTarget = true"
          >
            <text class="target-value">自定义</text>
            <text class="target-desc">输入任意值</text>
          </view>
        </view>
        
        <!-- 自定义输入框 -->
        <view v-if="isCustomTarget" class="custom-input-section">
          <input 
            type="number" 
            v-model="customTargetInput" 
            placeholder="请输入目标饮水量(ml)" 
            class="custom-input"
            @focus="tempTarget = 2000"
          />
          <text class="custom-hint">建议值: 1500-4000ml</text>
        </view>

        <view class="modal-actions">
          <button class="btn-cancel" @click="closeSettings">取消</button>
          <button class="btn-confirm" @click="saveTarget">保存</button>
        </view>
      </view>
    </view>

    <!-- 历史记录弹窗 -->
    <view v-if="historyVisible" class="modal-overlay" @click="closeHistory">
      <view class="modal-content history-modal" @click.stop>
        <view class="modal-header">
          <text>历史记录</text>
          <text class="close-btn" @click="closeHistory">✕</text>
        </view>

        <!-- 日期选择器 -->
        <view class="date-picker-section">
          <text class="picker-label">选择日期：</text>
          <picker mode="date" :value="historyDate" :end="today" @change="onHistoryDateChange">
            <view class="date-picker-btn">
              <text>{{ formatDateDisplay(historyDate) }}</text>
              <u-icon name="arrow-down" size="28" color="#666"></u-icon>
            </view>
          </picker>
        </view>

        <!-- 历史统计数据 -->
        <view class="history-stats">
          <view class="stat-item">
            <text class="stat-value">{{ historyData.todayAmount || 0 }}</text>
            <text class="stat-label">总饮水量(ml)</text>
          </view>
          <view class="stat-item">
            <text class="stat-value">{{ historyData.percent || 0 }}%</text>
            <text class="stat-label">目标完成度</text>
          </view>
          <view class="stat-item">
            <text class="stat-value">{{ (historyData.records || []).length }}</text>
            <text class="stat-label">记录次数</text>
          </view>
        </view>

        <!-- 历史记录列表 -->
        <scroll-view scroll-y class="history-records-list" style="max-height: 600rpx;">
          <view v-if="(historyData.records || []).length === 0" class="history-empty">
            <u-icon name="calendar" size="80" color="#ddd"></u-icon>
            <text>该日期暂无喝水记录</text>
          </view>
          
          <view
            v-for="(record, index) in (historyData.records || [])"
            :key="record.id || index"
            class="history-record-item"
          >
            <view class="record-left">
              <view class="record-icon-small">
                <u-icon name="clock" size="32" color="#5B8FF9"></u-icon>
              </view>
              <view class="record-info">
                <text class="record-amount">+{{ record.amount }}ml</text>
                <text class="record-time">{{ formatTime(record.recordTime) }}</text>
              </view>
            </view>
            <view class="record-delete-wrapper" @click="deleteHistoryRecord(record.id)">
              <view class="record-delete-btn history-delete-btn" title="删除记录">
                <u-icon name="trash" size="24" color="#fff"></u-icon>
              </view>
            </view>
          </view>
        </scroll-view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { waterApi } from '../../api/index.js'

const todayAmount = ref(0)
const targetAmount = ref(2000)
const records = ref([])
const selectedAmount = ref(200)
const settingsVisible = ref(false)
const tempTarget = ref(2000)
const historyVisible = ref(false)
const historyDate = ref('')
const historyData = ref({})
const today = ref(new Date().toISOString().split('T')[0])

const quickAmounts = [100, 125, 150, 175, 200, 250, 350, 500]
const targetOptions = [1500, 2000, 2500, 3000, 3500]
const isCustomTarget = ref(false)
const customTargetInput = ref('')

// 自定义喝水量相关
const isCustomAmount = ref(false)
const customAmountInput = ref('')

// 选择快捷金额
const selectQuickAmount = (amount) => {
  isCustomAmount.value = false
  customAmountInput.value = ''
  selectedAmount.value = amount
}

// 切换自定义输入显示
const toggleCustomAmount = () => {
  isCustomAmount.value = !isCustomAmount.value
  if (!isCustomAmount.value) {
    customAmountInput.value = ''
    // 恢复默认选中200ml
    selectedAmount.value = 200
  } else {
    customAmountInput.value = ''
    selectedAmount.value = 0
  }
}

const healthTips = [
  '早起一杯水，有助于唤醒身体代谢',
  '饭前30分钟喝水，有助于控制饮食量',
  '运动前后记得补充水分',
  '久坐办公时，每小时喝一杯水',
  '睡前1小时减少饮水，避免夜间频繁起夜',
  '感到口渴时，身体已经轻度缺水了'
]

const currentTip = computed(() => {
  const hour = new Date().getHours()
  if (hour < 9) return healthTips[0]
  if (hour < 12) return healthTips[3]
  if (hour < 14) return healthTips[1]
  if (hour < 18) return healthTips[3]
  return healthTips[4]
})

const todayText = computed(() => {
  return new Date().toLocaleDateString('zh-CN', { month: 'long', day: 'numeric' })
})

const weekText = computed(() => {
  const days = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  return days[new Date().getDay()]
})

const waterPercent = computed(() => {
  return Math.min(Math.round((todayAmount.value / targetAmount.value) * 100), 100)
})

onMounted(() => {
  loadTodayData()
})

const loadTodayData = async () => {
  try {
    const userInfo = uni.getStorageSync('userInfo')
    const userId = userInfo?.id || 1
    
    // 优先从后端获取用户的喝水目标
    try {
      const targetRes = await waterApi.getTarget(userId)
      if (targetRes && targetRes.data && targetRes.data.targetAmount) {
        targetAmount.value = targetRes.data.targetAmount
      } else {
        // 后端没有保存的目标，尝试从本地存储获取
        const localTarget = uni.getStorageSync('waterTarget')
        if (localTarget) {
          targetAmount.value = localTarget
        }
      }
    } catch (e) {
      console.error('获取喝水目标失败，使用默认值', e)
      // 后端获取失败，从本地存储获取
      const localTarget = uni.getStorageSync('waterTarget')
      if (localTarget) {
        targetAmount.value = localTarget
      }
    }
    
    // 获取今日喝水数据
    const res = await waterApi.getToday(userId)
    if (res) {
      todayAmount.value = res.todayAmount || 0
      // 使用后端返回的目标（如果有）
      if (res.targetAmount) {
        targetAmount.value = res.targetAmount
      }
      records.value = res.records || []
    }
  } catch (e) {
    console.error('加载喝水数据失败', e)
    // 不显示toast，避免干扰用户
  }
}

const recordWater = async () => {
  // 处理自定义输入
  let amountToRecord = selectedAmount.value
  
  if (isCustomAmount.value && customAmountInput.value) {
    const customValue = parseInt(customAmountInput.value)
    if (isNaN(customValue) || customValue <= 0) {
      uni.showToast({ title: '请输入有效的毫升数', icon: 'none' })
      return
    }
    if (customValue > 5000) {
      uni.showToast({ title: '单次记录不能超过5000ml', icon: 'none' })
      return
    }
    amountToRecord = customValue
  }
  
  if (amountToRecord <= 0) {
    uni.showToast({ title: '请选择或输入喝水量', icon: 'none' })
    return
  }
  
  try {
    const userInfo = uni.getStorageSync('userInfo')
    const userId = userInfo?.id || 1
    
    // 检查是否有token
    const token = uni.getStorageSync('token')
    console.log('=== 记录喝水调试信息 ===')
    console.log('Token:', token ? '存在' : '不存在')
    console.log('UserId:', userId)
    console.log('SelectedAmount:', amountToRecord)

    // 格式化时间为 HH:mm:ss 格式
    const now = new Date()
    const hours = String(now.getHours()).padStart(2, '0')
    const minutes = String(now.getMinutes()).padStart(2, '0')
    const seconds = String(now.getSeconds()).padStart(2, '0')
    const timeString = `${hours}:${minutes}:${seconds}`
    
    const requestData = {
      userId: userId,
      amount: amountToRecord,
      recordTime: timeString
    }
    console.log('请求参数:', JSON.stringify(requestData))

    const result = await waterApi.record(requestData)
    console.log('API响应:', result)

    uni.showToast({ title: '打卡成功', icon: 'success' })
    
    // 重置自定义输入状态
    isCustomAmount.value = false
    customAmountInput.value = ''
    selectedAmount.value = 200 // 恢复默认值
    
    await loadTodayData()
  } catch (e) {
    console.error('=== 记录喝水失败详情 ===')
    console.error('错误对象:', e)
    console.error('错误消息:', e.message)
    console.error('错误响应:', e.response)
    console.error('错误响应数据:', e.response?.data)
    console.error('错误状态码:', e.statusCode || e.code)
    
    let errorMsg = '记录失败'
    if (e.message) {
      errorMsg += ': ' + e.message
    } else if (e.response?.data?.message) {
      errorMsg += ': ' + e.response.data.message
    } else if (e.statusCode === 401) {
      errorMsg = '请先登录后再记录'
    } else if (e.statusCode === 500) {
      errorMsg = '服务器错误，请稍后重试'
    } else {
      errorMsg += ': 请稍后重试'
    }
    
    uni.showToast({ title: errorMsg, icon: 'none', duration: 3000 })
  }
}

const formatTime = (timeStr) => {
  if (!timeStr) return ''
  
  try {
    // 处理多种时间格式
    let hours, minutes
    
    // 如果是完整日期时间格式 (如 2024-01-15T13:25:30 或 2024-01-15 13:25:30)
    if (timeStr.includes('T') || timeStr.includes(' ')) {
      const date = new Date(timeStr)
      if (!isNaN(date.getTime())) {
        hours = date.getHours()
        minutes = date.getMinutes()
      }
    }
    // 如果是时间字符串格式 (如 13:25 或 13:25:30)
    else if (timeStr.includes(':')) {
      const parts = timeStr.split(':')
      hours = parseInt(parts[0])
      minutes = parseInt(parts[1])
    }
    
    // 验证获取的小时和分钟是否有效
    if (isNaN(hours) || isNaN(minutes)) {
      console.warn('无法解析时间:', timeStr)
      return ''
    }
    
    return `${String(hours).padStart(2, '0')}:${String(minutes).padStart(2, '0')}`
  } catch (e) {
    console.error('时间格式化错误:', timeStr, e)
    return ''
  }
}

const calculateProgress = (amount) => {
  return Math.round((amount / targetAmount.value) * 100)
}

const showSettings = () => {
  tempTarget.value = targetAmount.value
  isCustomTarget.value = false
  customTargetInput.value = ''
  settingsVisible.value = true
}

const closeSettings = () => {
  settingsVisible.value = false
  isCustomTarget.value = false
  customTargetInput.value = ''
}

const selectPresetTarget = (target) => {
  isCustomTarget.value = false
  tempTarget.value = target
}

const saveTarget = async () => {
  // 处理自定义输入
  if (isCustomTarget.value && customTargetInput.value) {
    const customValue = parseInt(customTargetInput.value)
    if (isNaN(customValue) || customValue < 500 || customValue > 10000) {
      uni.showToast({ title: '请输入500-10000之间的数值', icon: 'none' })
      return
    }
    tempTarget.value = customValue
  }
  
  targetAmount.value = tempTarget.value
  try {
    const userInfo = uni.getStorageSync('userInfo')
    const userId = userInfo?.id || 1
    // 调用API保存目标到后端，传递userId
    await waterApi.setTarget(tempTarget.value, userId)
    uni.showToast({ title: '设置已保存', icon: 'success' })
  } catch (e) {
    console.error('保存目标失败', e)
    // 即使API失败，也保存到本地存储作为备用
    uni.setStorageSync('waterTarget', tempTarget.value)
    uni.showToast({ title: '已本地保存', icon: 'success' })
  }
  closeSettings()
}

const getTargetDesc = (target) => {
  if (target <= 1500) return '轻松模式'
  if (target <= 2000) return '标准推荐'
  if (target <= 2500) return '健康模式'
  return '运动模式'
}

const goBack = () => {
  uni.switchTab({ url: '/pages/home/index' })
}

// 删除记录
const deleteRecord = async (recordId) => {
  if (!recordId) {
    uni.showToast({ title: '记录ID无效', icon: 'none' })
    return
  }
  
  uni.showModal({
    title: '确认删除',
    content: '确定要删除这条喝水记录吗？',
    confirmColor: '#FF4D4F',
    success: async (res) => {
      if (res.confirm) {
        uni.showLoading({ title: '删除中...' })
        try {
          console.log('正在删除喝水记录:', recordId)
          // 添加 silent 选项，阻止全局拦截器处理 401 错误，避免跳转
          const result = await waterApi.deleteRecord(recordId, { silent: true })
          console.log('删除成功:', result)
          uni.hideLoading()
          uni.showToast({ title: '删除成功', icon: 'success' })
          // 刷新今日记录
          await loadTodayData()
        } catch (e) {
          uni.hideLoading()
          console.error('删除记录失败:', e)
          let errorMsg = '删除失败'
          if (e.statusCode === 401) {
            errorMsg = '请先登录后再删除'
          } else if (e.statusCode === 403) {
            errorMsg = '无权删除此记录'
          } else if (e.statusCode === 404) {
            errorMsg = '记录不存在或已删除'
          } else if (e.statusCode === 500) {
            errorMsg = '服务器错误，请稍后重试'
          } else if (e.message) {
            errorMsg = e.message
          } else if (e.response?.data?.message) {
            errorMsg = e.response.data.message
          }
          uni.showToast({ title: errorMsg, icon: 'none', duration: 3000 })
        }
      }
    }
  })
}

// 显示历史记录弹窗
const showHistory = () => {
  // 默认显示昨天的日期
  const yesterday = new Date()
  yesterday.setDate(yesterday.getDate() - 1)
  historyDate.value = yesterday.toISOString().split('T')[0]
  historyData.value = {}
  historyVisible.value = true
  // 加载历史数据
  loadHistoryData()
}

// 关闭历史记录弹窗
const closeHistory = () => {
  historyVisible.value = false
}

// 历史日期改变
const onHistoryDateChange = (e) => {
  historyDate.value = e.detail.value
  loadHistoryData()
}

// 加载历史数据
const loadHistoryData = async () => {
  try {
    const userInfo = uni.getStorageSync('userInfo')
    const userId = userInfo?.id || 1
    const res = await waterApi.getHistory(userId, historyDate.value)
    console.log('历史记录API返回:', res)
    if (res) {
      // 处理API返回的数据结构
      let records = []
      if (Array.isArray(res)) {
        records = res
      } else if (res.data && Array.isArray(res.data)) {
        records = res.data
      } else if (res.records && Array.isArray(res.records)) {
        records = res.records
      }
      
      // 计算今日总量
      const todayAmount = records.reduce((sum, r) => sum + (r.amount || 0), 0)
      
      historyData.value = {
        records: records,
        todayAmount: todayAmount,
        percent: Math.min(Math.round((todayAmount / targetAmount.value) * 100), 100)
      }
    }
  } catch (e) {
    console.error('加载历史数据失败', e)
    historyData.value = { records: [], todayAmount: 0, percent: 0 }
  }
}

// 格式化日期显示
const formatDateDisplay = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const month = date.getMonth() + 1
  const day = date.getDate()
  const todayStr = new Date().toISOString().split('T')[0]
  
  if (dateStr === todayStr) return '今天'
  
  const yesterday = new Date()
  yesterday.setDate(yesterday.getDate() - 1)
  if (dateStr === yesterday.toISOString().split('T')[0]) return '昨天'
  
  return `${month}月${day}日`
}

// 删除历史记录
const deleteHistoryRecord = async (recordId) => {
  if (!recordId) {
    uni.showToast({ title: '记录ID无效', icon: 'none' })
    return
  }
  
  uni.showModal({
    title: '确认删除',
    content: '确定要删除这条喝水记录吗？',
    confirmColor: '#FF4D4F',
    success: async (res) => {
      if (res.confirm) {
        uni.showLoading({ title: '删除中...' })
        try {
          console.log('正在删除历史喝水记录:', recordId)
          // 添加 silent 选项，阻止全局拦截器处理 401 错误，避免跳转
          const result = await waterApi.deleteRecord(recordId, { silent: true })
          console.log('删除成功:', result)
          uni.hideLoading()
          uni.showToast({ title: '删除成功', icon: 'success' })
          // 刷新历史记录
          await loadHistoryData()
          // 同时刷新今日记录（可能删除的是今天的记录）
          await loadTodayData()
        } catch (e) {
          uni.hideLoading()
          console.error('删除历史记录失败:', e)
          let errorMsg = '删除失败'
          if (e.statusCode === 401) {
            errorMsg = '请先登录后再删除'
          } else if (e.statusCode === 403) {
            errorMsg = '无权删除此记录'
          } else if (e.statusCode === 404) {
            errorMsg = '记录不存在或已删除'
          } else if (e.statusCode === 500) {
            errorMsg = '服务器错误，请稍后重试'
          } else if (e.message) {
            errorMsg = e.message
          } else if (e.response?.data?.message) {
            errorMsg = e.response.data.message
          }
          uni.showToast({ title: errorMsg, icon: 'none', duration: 3000 })
        }
      }
    }
  })
}
</script>

<style lang="scss" scoped>
.water-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #E6F7FF 0%, #f5f6fa 100%);
}

.nav-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 30rpx;
  padding-top: 60rpx;
  background: transparent;

  .back-btn, .right-btn {
    width: 60rpx;
    height: 60rpx;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .title {
    font-size: 34rpx;
    font-weight: 600;
    color: #333;
  }
}

.content {
  padding: 0 30rpx 40rpx;
}

.date-section {
  text-align: center;
  margin-bottom: 30rpx;

  .date-text {
    font-size: 36rpx;
    font-weight: 600;
    color: #333;
    margin-right: 16rpx;
  }

  .week-text {
    font-size: 28rpx;
    color: #666;
  }
}

.water-cup-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 40rpx;

  .cup-container {
    position: relative;
    margin-bottom: 30rpx;

    .cup {
      width: 200rpx;
      height: 280rpx;
      background: rgba(255, 255, 255, 0.9);
      border: 4rpx solid #5B8FF9;
      border-radius: 0 0 30rpx 30rpx;
      position: relative;
      overflow: hidden;
      box-shadow: 0 8rpx 30rpx rgba(91, 143, 249, 0.2);

      .water {
        position: absolute;
        bottom: 0;
        left: 0;
        right: 0;
        background: linear-gradient(180deg, #5B8FF9 0%, #2E6AD8 100%);
        transition: height 0.5s ease;

        .water-wave {
          position: absolute;
          top: -10rpx;
          left: 0;
          right: 0;
          height: 20rpx;
          background: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 1440 320'%3E%3Cpath fill='%235B8FF9' fill-opacity='1' d='M0,96L48,112C96,128,192,160,288,160C384,160,480,128,576,112C672,96,768,96,864,112C960,128,1056,160,1152,160C1248,160,1344,128,1392,112L1440,96L1440,320L1392,320C1344,320,1248,320,1152,320C1056,320,960,320,864,320C768,320,672,320,576,320C480,320,384,320,288,320C192,320,96,320,48,320L0,320Z'%3E%3C/path%3E%3C/svg%3E");
          background-size: cover;
          animation: wave 2s linear infinite;
        }
      }

      .cup-markings {
        position: absolute;
        right: 0;
        top: 0;
        bottom: 0;
        width: 30rpx;

        .marking {
          position: absolute;
          right: 0;
          width: 20rpx;
          height: 2rpx;
          background: rgba(91, 143, 249, 0.3);
        }
      }
    }

    .cup-stand {
      width: 120rpx;
      height: 20rpx;
      background: rgba(91, 143, 249, 0.3);
      border-radius: 10rpx;
      margin: 0 auto;
      margin-top: 10rpx;
    }
  }

  .water-stats {
    display: flex;
    align-items: baseline;
    gap: 16rpx;
    margin-bottom: 16rpx;

    .stats-item {
      display: flex;
      align-items: baseline;
      gap: 8rpx;

      .stats-num {
        font-size: 56rpx;
        font-weight: bold;
        color: #5B8FF9;

        &.target {
          font-size: 40rpx;
          color: #999;
        }
      }

      .stats-unit {
        font-size: 24rpx;
        color: #666;
      }

      &.target-item {
        align-items: center;
        cursor: pointer;

        .settings-icon-small {
          width: 36rpx;
          height: 36rpx;
          display: flex;
          align-items: center;
          justify-content: center;
          margin-left: 4rpx;
          opacity: 0.7;
          transition: opacity 0.2s;

          &:hover {
            opacity: 1;
          }
        }
      }
    }

    .stats-divider {
      font-size: 40rpx;
      color: #ccc;
    }
  }

  .progress-text {
    text {
      font-size: 28rpx;
      color: #666;

      &.completed {
        color: #5AD8A6;
        font-weight: 600;
      }
    }
  }
}

@keyframes wave {
  0% { transform: translateX(0); }
  100% { transform: translateX(-50%); }
}

.quick-add-section {
  background: #fff;
  border-radius: 20rpx;
  padding: 30rpx;
  margin-bottom: 30rpx;

  .section-title {
    font-size: 30rpx;
    font-weight: 600;
    color: #333;
    display: block;
    margin-bottom: 24rpx;
  }

  .quick-buttons {
    display: flex;
    flex-wrap: wrap;
    gap: 16rpx;
    margin-bottom: 20rpx;

    .quick-btn {
      display: flex;
      align-items: center;
      gap: 8rpx;
      padding: 16rpx 24rpx;
      background: #f5f6fa;
      border-radius: 30rpx;
      border: 2rpx solid transparent;
      transition: all 0.3s;

      text {
        font-size: 26rpx;
        color: #666;
      }

      .water-icon {
        font-size: 32rpx;
      }

      &.active {
        background: #E6F7FF;
        border-color: #5B8FF9;

        text {
          color: #5B8FF9;
        }
      }
      
      &.custom-btn {
        background: #FFF7E6;
        
        &.active {
          background: #FFF7E6;
          border-color: #FA8C16;
          
          text {
            color: #FA8C16;
          }
        }
      }
    }
  }
  
  .custom-amount-section {
    margin-bottom: 20rpx;
    padding: 20rpx;
    background: #FFF7E6;
    border-radius: 16rpx;
    
    .custom-input-wrapper {
      display: flex;
      align-items: center;
      gap: 12rpx;
      
      .custom-amount-input {
        flex: 1;
        height: 72rpx;
        background: #fff;
        border-radius: 36rpx;
        padding: 0 30rpx;
        font-size: 30rpx;
        border: 2rpx solid #FA8C16;
      }
      
      .custom-unit {
        font-size: 28rpx;
        color: #FA8C16;
        font-weight: 500;
      }
    }
    
    .custom-hint-tip {
      display: block;
      font-size: 22rpx;
      color: #FA8C16;
      margin-top: 10rpx;
      text-align: center;
    }
  }

  .add-btn {
    width: 100%;
    height: 90rpx;
    background: linear-gradient(135deg, #5B8FF9 0%, #2E6AD8 100%);
    border-radius: 45rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 12rpx;
    border: none;

    .btn-icon {
      font-size: 36rpx;
    }

    text {
      font-size: 30rpx;
      color: #fff;
    }

    &:active {
      opacity: 0.9;
    }
  }
}

.records-section {
  background: #fff;
  border-radius: 20rpx;
  padding: 30rpx;
  margin-bottom: 30rpx;

  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24rpx;

    .section-title {
      font-size: 30rpx;
      font-weight: 600;
      color: #333;
    }

    .header-actions {
      display: flex;
      align-items: center;
      gap: 20rpx;
    }

    .record-count {
      font-size: 26rpx;
      color: #999;
    }

    .history-link {
      font-size: 26rpx;
      color: #5B8FF9;
      font-weight: 500;
    }
  }

  .empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 60rpx 0;

    text {
      margin-top: 20rpx;
      font-size: 28rpx;
      color: #999;
    }
  }

  .records-list {
    display: flex;
    flex-direction: column;

    .record-item {
      display: flex;
      align-items: center;
      padding: 24rpx 0;
      border-bottom: 1rpx solid #f0f0f0;

      &:last-child {
        border-bottom: none;
      }

      .record-icon {
        width: 60rpx;
        height: 60rpx;
        background: #E6F7FF;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-right: 20rpx;
      }

      .record-info {
        flex: 1;
        display: flex;
        flex-direction: column;

        .record-amount {
          font-size: 30rpx;
          font-weight: 600;
          color: #333;
        }

        .record-time {
          font-size: 24rpx;
          color: #999;
          margin-top: 4rpx;
        }
      }

      .record-progress {
        margin-right: 16rpx;
        
        text {
          font-size: 26rpx;
          color: #5B8FF9;
          font-weight: 500;
        }
      }

      .record-delete-wrapper {
        padding: 8rpx;
        
        .record-delete-btn {
          width: 100rpx;
          height: 64rpx;
          background: linear-gradient(135deg, #FF4D4F 0%, #FF7875 100%);
          border-radius: 32rpx;
          display: flex;
          align-items: center;
          justify-content: center;
          gap: 6rpx;
          box-shadow: 0 4rpx 16rpx rgba(255, 77, 79, 0.4);
          transition: all 0.2s ease;
          
          .delete-text {
            font-size: 24rpx;
            color: #fff;
            font-weight: 500;
          }
          
          &:hover {
            transform: translateY(-2rpx);
            box-shadow: 0 6rpx 20rpx rgba(255, 77, 79, 0.5);
          }
          
          &:active {
            transform: scale(0.95);
            box-shadow: 0 2rpx 8rpx rgba(255, 77, 79, 0.6);
          }
        }
      }
    }
  }
}

.tips-section {
  .tip-card {
    display: flex;
    align-items: flex-start;
    gap: 20rpx;
    background: linear-gradient(135deg, #E6FFF2 0%, #F0FFF8 100%);
    border-radius: 20rpx;
    padding: 30rpx;

    .tip-content {
      flex: 1;

      .tip-title {
        font-size: 28rpx;
        font-weight: 600;
        color: #333;
        display: block;
        margin-bottom: 8rpx;
      }

      .tip-text {
        font-size: 26rpx;
        color: #666;
        line-height: 1.5;
      }
    }
  }
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: flex-end;
  justify-content: center;
  z-index: 99999;
}

.modal-content {
  width: 100%;
  background: #fff;
  border-radius: 30rpx 30rpx 0 0;
  padding: 40rpx 30rpx;

  .modal-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 40rpx;

    text {
      font-size: 36rpx;
      font-weight: 600;
      color: #333;
    }

    .close-btn {
      font-size: 40rpx;
      color: #999;
      padding: 10rpx;
    }
  }

  .target-options {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 20rpx;
    margin-bottom: 40rpx;

    .target-option {
      padding: 30rpx;
      background: #f5f6fa;
      border-radius: 16rpx;
      text-align: center;
      border: 2rpx solid transparent;
      transition: all 0.3s;

      .target-value {
        display: block;
        font-size: 36rpx;
        font-weight: 600;
        color: #333;
        margin-bottom: 8rpx;
      }

      .target-desc {
        font-size: 24rpx;
        color: #999;
      }

      &.active {
        background: #E6F7FF;
        border-color: #5B8FF9;

        .target-value {
          color: #5B8FF9;
        }
      }
    }
  }

  .custom-input-section {
    padding: 20rpx 30rpx;
    margin-top: 20rpx;
    
    .custom-input {
      width: 100%;
      height: 80rpx;
      background: #f5f6fa;
      border-radius: 20rpx;
      padding: 0 30rpx;
      font-size: 30rpx;
      border: 2rpx solid #e0e0e0;
      
      &:focus {
        border-color: #5B8FF9;
      }
    }
    
    .custom-hint {
      display: block;
      font-size: 24rpx;
      color: #999;
      margin-top: 10rpx;
      text-align: center;
    }
  }

  .modal-actions {
    display: flex;
    gap: 20rpx;

    button {
      flex: 1;
      height: 90rpx;
      border-radius: 45rpx;
      font-size: 30rpx;
      border: none;
    }

    .btn-cancel {
      background: #f5f6fa;
      color: #666;
    }

    .btn-confirm {
      background: linear-gradient(135deg, #5B8FF9 0%, #2E6AD8 100%);
      color: #fff;
    }
  }
}

// 历史记录弹窗样式
.history-modal {
  max-height: 80vh;

  .date-picker-section {
    display: flex;
    align-items: center;
    margin-bottom: 30rpx;
    position: relative;
    z-index: 100001;

    .picker-label {
      font-size: 28rpx;
      color: #666;
    }

    .date-picker-btn {
      display: flex;
      align-items: center;
      gap: 8rpx;
      padding: 12rpx 24rpx;
      background: #f5f6fa;
      border-radius: 12rpx;
      position: relative;
      z-index: 100002;
      margin-left: 16rpx;

      text {
        font-size: 28rpx;
        color: #333;
        font-weight: 500;
      }
    }
  }

  // picker 组件样式覆盖，确保日期选择器弹窗在最上层
  ::v-deep .uni-picker-container {
    z-index: 999999 !important;
  }
  
  ::v-deep .uni-picker-view-wrapper {
    z-index: 999999 !important;
  }
  
  ::v-deep .uni-picker-header {
    z-index: 999999 !important;
  }
  
  ::v-deep .uni-picker-action uni-view {
    z-index: 999999 !important;
  }
  
  ::v-deep .uni-picker-menu {
    z-index: 999999 !important;
  }
  
  ::v-deep .uni-picker-content {
    z-index: 999999 !important;
  }
  
  // 确保 mask 也在上层
  ::v-deep .uni-picker-mask {
    z-index: 999998 !important;
  }

  .history-stats {
    display: flex;
    justify-content: space-around;
    padding: 30rpx 0;
    margin-bottom: 30rpx;
    background: linear-gradient(135deg, #E6F7FF 0%, #F0F8FF 100%);
    border-radius: 16rpx;

    .stat-item {
      display: flex;
      flex-direction: column;
      align-items: center;

      .stat-value {
        font-size: 40rpx;
        font-weight: 700;
        color: #5B8FF9;
      }

      .stat-label {
        font-size: 24rpx;
        color: #666;
        margin-top: 8rpx;
      }
    }
  }

  .history-records-list {
    .history-empty {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 80rpx 0;

      text {
        margin-top: 20rpx;
        font-size: 28rpx;
        color: #999;
      }
    }

    .history-record-item {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 20rpx 0;
      border-bottom: 1rpx solid #f0f0f0;

      &:last-child {
        border-bottom: none;
      }

      .record-left {
        display: flex;
        align-items: center;
        gap: 16rpx;

        .record-icon-small {
          width: 52rpx;
          height: 52rpx;
          background: #E6F7FF;
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
        }

        .record-info {
          display: flex;
          flex-direction: column;

          .record-amount {
            font-size: 28rpx;
            font-weight: 600;
            color: #333;
          }

          .record-time {
            font-size: 22rpx;
            color: #999;
            margin-top: 4rpx;
          }
        }
      }

      .record-delete-wrapper {
        padding: 6rpx;
        
        .record-delete-btn {
          width: 56rpx;
          height: 56rpx;
          background: linear-gradient(135deg, #FF4D4F 0%, #FF7875 100%);
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          box-shadow: 0 4rpx 12rpx rgba(255, 77, 79, 0.3);
          transition: all 0.2s ease;
          
          &:hover {
            transform: translateY(-2rpx);
            box-shadow: 0 6rpx 16rpx rgba(255, 77, 79, 0.4);
          }
          
          &:active {
            transform: scale(0.9);
            box-shadow: 0 2rpx 8rpx rgba(255, 77, 79, 0.5);
          }
        }
      }
    }
  }
}
</style>
