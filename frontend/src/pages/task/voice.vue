<template>
  <view class="page-container">
    <!-- 头部 -->
    <view class="header">
      <view class="header-left" @click="goBack">
        <text class="back-icon">‹</text>
      </view>
      <view class="header-title">语音创建</view>
      <view class="header-action" @click="showHelp">
        <text class="icon">❓</text>
      </view>
    </view>
    
    <!-- 语音识别状态 -->
    <view class="voice-status"
003e
      <view class="status-animation" :class="{ recording: isRecording }"
003e
        <view class="voice-waves"
003e
          <view v-for="i in 5" :key="i" class="wave-bar" :style="getWaveStyle(i)"></view>
        </view>
        
        <view class="voice-icon">
          <text v-if="!isRecording">🎤</text>
          <text v-else>🔴</text>
        </view>
      </view>
      
      <text class="status-text">{{ statusText }}</text>
      
      <text v-if="isRecording" class="recording-time">{{ formatTime(recordingTime) }}</text>
    </view>
    
    <!-- 识别结果区域 -->
    <view class="result-section" v-if="recognizedText || parsedTask.title"
003e
      <view class="section-header"
003e
        <text class="section-title">识别结果</text>
        <text class="edit-btn" @click="editText">编辑</text>
      </view>
      
      <view class="result-content"
003e
        <text v-if="!isEditing">{{ recognizedText || parsedTask.title }}</text>
        <textarea 
          v-else
          class="edit-textarea"
          v-model="recognizedText"
          auto-height
          focus
        />
      </view>
      
      <!-- 解析的任务信息 -->
      <view class="parsed-info" v-if="parsedTask.title"
003e
        <view class="info-header">
          <text>📋 解析出的任务信息</text>
        </view>
        
        <view class="info-item"
003e
          <text class="info-label">任务标题</text>
          <text class="info-value">{{ parsedTask.title }}</text>
        </view>
        
        <view class="info-item" v-if="parsedTask.dueDate"
003e
          <text class="info-label">截止日期</text>
          <text class="info-value">{{ parsedTask.dueDate }}</text>
        </view>
        
        <view class="info-item" v-if="parsedTask.dueTime"
003e
          <text class="info-label">截止时间</text>
          <text class="info-value">{{ parsedTask.dueTime }}</text>
        </view>
        
        <view class="info-item" v-if="parsedTask.priority"
003e
          <text class="info-label">优先级</text>
          <text class="info-value priority" :class="'p' + parsedTask.priority">{{ getPriorityText(parsedTask.priority) }}</text>
        </view>
        
        <view class="info-item" v-if="parsedTask.category"
003e
          <text class="info-label">分类</text>
          <text class="info-value">{{ parsedTask.category }}</text>
        </view>
        
        <view class="info-item" v-if="parsedTask.assignee"
003e
          <text class="info-label">负责人</text>
          <text class="info-value">{{ parsedTask.assignee }}</text>
        </view>
      </view>
    </view>
    
    <!-- 示例语音指令 -->
    <view class="examples-section" v-if="!isRecording && !recognizedText"
003e
      <view class="section-title">可以说</text>
      
      <view class="examples-list"
003e
        <view 
          v-for="(example, index) in examples" 
          :key="index"
          class="example-item"
          @click="useExample(example)"
        >
          <text class="example-icon">🎤</text>
          <text class="example-text">{{ example }}</text>
        </view>
      </view>
    </view>
    
    <!-- 快捷输入栏 -->
    <view class="quick-input-section" v-if="!isRecording"
003e
      <view class="section-title">快捷输入</text>
      
      <view class="quick-tags"
003e
        <text 
          v-for="tag in quickTags" 
          :key="tag"
          class="quick-tag"
          @click="appendTag(tag)"
        >{{ tag }}</text>
      </view>
    </view>
    
    <!-- 底部操作栏 -->
    <view class="bottom-actions"
003e
      <view v-if="!isRecording" class="action-row"
003e
        <view class="action-btn secondary" @click="goBack">
          <text>取消</text>
        </view>
        
        <view class="action-btn primary record" @touchstart="startRecording" @touchend="stopRecording"
003e
          <text class="record-icon">🎤</text>
          <text>按住说话</text>
        </view>
      </view>
      
      <view v-else class="action-row recording"
003e
        <view class="action-btn secondary" @click="cancelRecording"
003e
          <text>取消</text>
        </view>
        
        <view class="recording-indicator"
003e
          <view class="recording-dot"></view>
          <text>录音中...</text>
        </view>
      </view>
      
      <view v-if="recognizedText && !isRecording" class="action-row"
003e
        <view class="action-btn secondary" @click="clearResult"
003e
          <text>重新录制</text>
        </view>
        
        <view class="action-btn primary" @click="createTask"
003e
          <text>创建任务</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { taskApi } from '../../api/index.js'
import dayjs from 'dayjs'

// 示例语句
const examples = [
  '明天下午3点去超市买牛奶',
  '周五之前完成报告，优先级高',
  '每天晚上8点提醒我倒垃圾',
  '下周三约医生检查',
  '紧急：今天下午5点前交电费',
  '周末带狗去洗澡，指派给小李'
]

// 快捷标签
const quickTags = ['今天', '明天', '下周', '紧急', '重要', '提醒', '指派']

// 响应式数据
const isRecording = ref(false)
const recordingTime = ref(0)
const recognizedText = ref('')
const isEditing = ref(false)
const parsedTask = ref({})

let recordingTimer = null
let recorderManager = null

// 计算属性
const statusText = computed(() => {
  if (isRecording.value) return '正在聆听...'
  if (recognizedText.value) return '识别成功'
  return '点击麦克风开始说话'
})

// 方法
const initRecorder = () => {
  // 微信小程序录音管理器
  // #ifdef MP-WEIXIN
  recorderManager = uni.getRecorderManager()
  
  recorderManager.onStart(() => {
  })
  
  recorderManager.onStop((res) => {
    // 这里应该调用语音识别API
    // 暂时使用模拟数据
    simulateRecognition()
  })
  
  recorderManager.onError((res) => {
    console.error('录音错误', res)
    uni.showToast({ title: '录音失败', icon: 'none' })
    isRecording.value = false
  })
  // #endif
}

const startRecording = () => {
  isRecording.value = true
  recordingTime.value = 0
  
  // 开始计时
  recordingTimer = setInterval(() => {
    recordingTime.value++
    if (recordingTime.value >= 60) {
      stopRecording()
    }
  }, 1000)
  
  // 开始录音
  // #ifdef MP-WEIXIN
  if (recorderManager) {
    recorderManager.start({
      duration: 60000,
      sampleRate: 16000,
      numberOfChannels: 1,
      encodeBitRate: 48000,
      format: 'mp3'
    })
  }
  // #endif
  
  // H5/APP 模拟录音
  // #ifndef MP-WEIXIN
  setTimeout(() => {
    if (isRecording.value) {
      stopRecording()
    }
  }, 3000)
  // #endif
}

const stopRecording = () => {
  if (!isRecording.value) return
  
  isRecording.value = false
  clearInterval(recordingTimer)
  
  // #ifdef MP-WEIXIN
  if (recorderManager) {
    recorderManager.stop()
  }
  // #endif
  
  // #ifndef MP-WEIXIN
  simulateRecognition()
  // #endif
}

const cancelRecording = () => {
  isRecording.value = false
  clearInterval(recordingTimer)
  
  // #ifdef MP-WEIXIN
  if (recorderManager) {
    recorderManager.stop()
  }
  // #endif
}

const simulateRecognition = () => {
  // 模拟语音识别结果
  const mockTexts = [
    '明天下午3点去超市买牛奶',
    '周五之前完成报告，优先级高',
    '明天早上8点提醒我开会'
  ]
  
  recognizedText.value = mockTexts[Math.floor(Math.random() * mockTexts.length)]
  
  // 解析任务信息
  parseTaskInfo(recognizedText.value)
}

const parseTaskInfo = (text) => {
  const task = { title: text }
  
  // 解析日期
  if (text.includes('今天')) {
    task.dueDate = dayjs().format('YYYY-MM-DD')
  } else if (text.includes('明天')) {
    task.dueDate = dayjs().add(1, 'day').format('YYYY-MM-DD')
  } else if (text.includes('下周')) {
    task.dueDate = dayjs().add(7, 'day').format('YYYY-MM-DD')
  } else if (text.includes('周五')) {
    task.dueDate = dayjs().day(5).format('YYYY-MM-DD')
  }
  
  // 解析时间
  const timeMatch = text.match(/(\d+)点/)
  if (timeMatch) {
    task.dueTime = timeMatch[1].padStart(2, '0') + ':00'
  }
  
  // 解析优先级
  if (text.includes('紧急') || text.includes('重要') || text.includes('优先级高')) {
    task.priority = 2
  } else if (text.includes('优先级中')) {
    task.priority = 1
  }
  
  // 解析分类
  if (text.includes('购物') || text.includes('买')) {
    task.category = '购物'
  } else if (text.includes('工作') || text.includes('报告')) {
    task.category = '工作'
  } else if (text.includes('医生') || text.includes('检查')) {
    task.category = '健康'
  }
  
  // 解析指派
  const assigneeMatch = text.match(/指派给(\w+)/)
  if (assigneeMatch) {
    task.assignee = assigneeMatch[1]
  }
  
  // 清理标题中的元信息
  task.title = text
    .replace(/今天|明天|下周|周五/g, '')
    .replace(/\d+点/g, '')
    .replace(/优先级[高中低]/g, '')
    .replace(/指派给\w+/g, '')
    .replace(/之前|之前完成/g, '')
    .trim()
  
  parsedTask.value = task
}

const getWaveStyle = (index) => {
  if (!isRecording.value) {
    return { height: '20px', animation: 'none' }
  }
  
  // 模拟声波动画
  const delay = index * 0.1
  const minHeight = 20
  const maxHeight = 60
  const randomHeight = Math.random() * (maxHeight - minHeight) + minHeight
  
  return {
    height: randomHeight + 'px',
    animationDelay: delay + 's'
  }
}

const formatTime = (seconds) => {
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
}

const getPriorityText = (p) => {
  const map = { 0: '普通', 1: '重要', 2: '紧急' }
  return map[p] || '普通'
}

const editText = () => {
  isEditing.value = true
}

const useExample = (example) => {
  recognizedText.value = example
  parseTaskInfo(example)
}

const appendTag = (tag) => {
  recognizedText.value = recognizedText.value ? recognizedText.value + ' ' + tag : tag
}

const clearResult = () => {
  recognizedText.value = ''
  parsedTask.value = {}
  isEditing.value = false
}

const createTask = async () => {
  if (!parsedTask.value.title) {
    uni.showToast({ title: '请先输入任务内容', icon: 'none' })
    return
  }
  
  try {
    const familyId = uni.getStorageSync('currentFamilyId') || 1
    
    await taskApi.create({
      title: parsedTask.value.title,
      dueDate: parsedTask.value.dueDate,
      dueTime: parsedTask.value.dueTime,
      priority: parsedTask.value.priority || 0,
      categoryName: parsedTask.value.category,
      familyId
    })
    
    uni.showToast({ title: '创建成功', icon: 'success' })
    
    setTimeout(() => {
      uni.navigateBack()
    }, 1500)
  } catch (e) {
    console.error('创建任务失败', e)
    uni.showToast({ title: '创建失败', icon: 'none' })
  }
}

const showHelp = () => {
  uni.showModal({
    title: '语音创建帮助',
    content: '• 按住麦克风按钮说话\n• 可以说"明天下午3点去超市"\n• 系统会自动识别时间、优先级等信息\n• 识别结果可以编辑后确认创建',
    showCancel: false
  })
}

const goBack = () => {
  uni.navigateBack()
}

onMounted(() => {
  initRecorder()
})

onUnmounted(() => {
  if (recordingTimer) {
    clearInterval(recordingTimer)
  }
})
</script>

<style lang="scss" scoped>
.page-container {
  min-height: 100vh;
  background: #F8FAFC;
  display: flex;
  flex-direction: column;
}

// 头部
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 50px 20px 20px;
  background: linear-gradient(135deg, #EF4444 0%, #DC2626 100%);
  
  .header-left {
    width: 40px;
    height: 40px;
    display: flex;
    align-items: center;
    justify-content: center;
    
    .back-icon {
      font-size: 32px;
      color: #fff;
      font-weight: 300;
    }
  }
  
  .header-title {
    font-size: 20px;
    font-weight: 600;
    color: #fff;
  }
  
  .header-action {
    width: 40px;
    height: 40px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: rgba(255,255,255,0.2);
    border-radius: 50%;
    
    .icon {
      font-size: 16px;
      color: #fff;
    }
  }
}

// 语音状态
.voice-status {
  padding: 40px 20px;
  text-align: center;
  
  .status-animation {
    position: relative;
    width: 150px;
    height: 150px;
    margin: 0 auto 30px;
    display: flex;
    align-items: center;
    justify-content: center;
    
    &.recording {
      .voice-waves {
        opacity: 1;
      }
      
      .voice-icon {
        transform: scale(1.1);
      }
    }
  }
  
  .voice-waves {
    position: absolute;
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 4px;
    opacity: 0.3;
    transition: opacity 0.3s;
    
    .wave-bar {
      width: 8px;
      background: linear-gradient(180deg, #EF4444, #F87171);
      border-radius: 4px;
      animation: wave 1s ease-in-out infinite;
    }
  }
  
  .voice-icon {
    width: 100px;
    height: 100px;
    background: linear-gradient(135deg, #EF4444, #DC2626);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 8px 32px rgba(239, 68, 68, 0.4);
    transition: transform 0.3s;
    z-index: 1;
    
    text {
      font-size: 40px;
    }
  }
  
  .status-text {
    font-size: 18px;
    color: #374151;
    font-weight: 500;
    display: block;
    margin-bottom: 10px;
  }
  
  .recording-time {
    font-size: 24px;
    color: #EF4444;
    font-weight: 700;
    font-family: monospace;
  }
}

@keyframes wave {
  0%, 100% { transform: scaleY(0.5); }
  50% { transform: scaleY(1); }
}

// 结果区域
.result-section {
  flex: 1;
  padding: 0 20px;
  overflow-y: auto;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  
  .section-title {
    font-size: 16px;
    font-weight: 600;
    color: #1F2937;
  }
  
  .edit-btn {
    font-size: 14px;
    color: #EF4444;
  }
}

.result-content {
  background: #fff;
  border-radius: 16px;
  padding: 16px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  
  text {
    font-size: 16px;
    color: #374151;
    line-height: 1.6;
  }
  
  .edit-textarea {
    width: 100%;
    font-size: 16px;
    color: #374151;
    line-height: 1.6;
    min-height: 60px;
  }
}

// 解析的任务信息
.parsed-info {
  background: #fff;
  border-radius: 16px;
  padding: 16px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  
  .info-header {
    margin-bottom: 16px;
    
    text {
      font-size: 14px;
      font-weight: 600;
      color: #1F2937;
    }
  }
  
  .info-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 0;
    border-bottom: 1px solid #F3F4F6;
    
    &:last-child {
      border-bottom: none;
    }
    
    .info-label {
      font-size: 14px;
      color: #6B7280;
    }
    
    .info-value {
      font-size: 14px;
      color: #374151;
      font-weight: 500;
      
      &.priority {
        padding: 4px 12px;
        border-radius: 10px;
        
        &.p1 {
          background: #FEF3C7;
          color: #D97706;
        }
        
        &.p2 {
          background: #FEE2E2;
          color: #DC2626;
        }
      }
    }
  }
}

// 示例区域
.examples-section {
  padding: 0 20px;
  margin-bottom: 20px;
  
  .section-title {
    font-size: 14px;
    color: #6B7280;
    margin-bottom: 12px;
    display: block;
  }
  
  .examples-list {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }
  
  .example-item {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 12px 16px;
    background: #fff;
    border-radius: 12px;
    box-shadow: 0 1px 3px rgba(0,0,0,0.04);
    
    .example-icon {
      font-size: 16px;
      opacity: 0.5;
    }
    
    .example-text {
      font-size: 14px;
      color: #6B7280;
    }
  }
}

// 快捷输入
.quick-input-section {
  padding: 0 20px;
  margin-bottom: 20px;
  
  .section-title {
    font-size: 14px;
    color: #6B7280;
    margin-bottom: 12px;
    display: block;
  }
  
  .quick-tags {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
  }
  
  .quick-tag {
    font-size: 13px;
    padding: 8px 16px;
    background: #FEE2E2;
    color: #DC2626;
    border-radius: 16px;
  }
}

// 底部操作栏
.bottom-actions {
  padding: 20px;
  background: #fff;
  box-shadow: 0 -4px 20px rgba(0,0,0,0.05);
}

.action-row {
  display: flex;
  gap: 12px;
  
  &.recording {
    justify-content: space-between;
    align-items: center;
  }
}

.action-btn {
  flex: 1;
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 25px;
  
  text {
    font-size: 16px;
    font-weight: 500;
  }
  
  &.secondary {
    background: #F3F4F6;
    
    text {
      color: #6B7280;
    }
  }
  
  &.primary {
    background: linear-gradient(135deg, #EF4444, #DC2626);
    
    text {
      color: #fff;
    }
    
    &.record {
      flex: 2;
      gap: 8px;
      
      .record-icon {
        font-size: 20px;
      }
    }
  }
}

.recording-indicator {
  display: flex;
  align-items: center;
  gap: 8px;
  
  .recording-dot {
    width: 10px;
    height: 10px;
    background: #EF4444;
    border-radius: 50%;
    animation: blink 1s infinite;
  }
  
  text {
    font-size: 16px;
    color: #EF4444;
    font-weight: 500;
  }
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.3; }
}
</style>
