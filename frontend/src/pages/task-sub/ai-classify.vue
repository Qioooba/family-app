<template>
  <view class="page-container"
>
    <view class="header"
>
      <view class="header-left" @click="goBack"><text>‹</text></view>
      <view class="header-title">AI智能分类</view>
      <view class="header-action" @click="showHelp"><text>❓</text></view>
    </view>
    
    <view class="input-section"
>
      <textarea 
        class="task-input" 
        v-model="taskText" 
        placeholder="输入任务内容，AI将自动分类..."
        maxlength="200"
      />
      <text class="char-count">{{ taskText.length }}/200</text>
    </view>
    
    <view class="classify-btn" :class="{ active: taskText.length > 0 }" @click="classifyTask"
>
      <text v-if="!isClassifying">🤖 AI分类</text>
      <text v-else>分析中...</text>
    </view>
    
    <view class="result-section" v-if="classificationResult"
>
      <view class="result-header"><text>📊 分类结果</text></view>
      
      <view class="category-card"
>
        <view class="category-icon" :style="{ background: classificationResult.color }">
          <text>{{ classificationResult.icon }}</text>
        </view>
        <view class="category-info"
>
          <text class="category-name">{{ classificationResult.category }}</text>
          <text class="confidence">置信度 {{ classificationResult.confidence }}%</text>
        </view>
      </view>
      
      <view class="suggestions-list" v-if="classificationResult.suggestions?.length"
>
        <text class="suggestions-title">💡 建议</text>
        <view v-for="(s, i) in classificationResult.suggestions" :key="i" class="suggestion-item"
>
          <text>{{ s }}</text>
        </view>
      </view>
      
      <view class="action-buttons"
>
        <view class="btn-secondary" @click="reset">重新输入</view>
        <view class="btn-primary" @click="createTask">创建任务</view>
      </view>
    </view>
    
    <view class="categories-grid"
>
      <text class="grid-title">分类类型</text>
      <view class="grid"
>
        <view v-for="cat in categories" :key="cat.name" class="cat-item"
>
          <text class="cat-icon">{{ cat.icon }}</text>
          <text class="cat-name">{{ cat.name }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { taskApi } from '../../api/index.js'

const categories = [
  { name: '家务', icon: '🧹', color: '#10B981' },
  { name: '购物', icon: '🛒', color: '#F59E0B' },
  { name: '工作', icon: '💼', color: '#3B82F6' },
  { name: '学习', icon: '📚', color: '#8B5CF6' },
  { name: '健康', icon: '💪', color: '#EF4444' },
  { name: '社交', icon: '👥', color: '#EC4899' }
]

const taskText = ref('')
const isClassifying = ref(false)
const classificationResult = ref(null)

const keywordMap = {
  '家务': ['打扫', '清洁', '洗碗', '洗衣', '拖地', '整理', '倒垃圾'],
  '购物': ['买', '超市', '采购', '购物', '下单', '订购'],
  '工作': ['报告', '会议', '邮件', '项目', '客户', '文档'],
  '学习': ['学习', '看书', '考试', '复习', '课程', '作业'],
  '健康': ['医生', '锻炼', '吃药', '体检', '运动', '瑜伽'],
  '社交': ['聚会', '约会', '生日', '拜访', '聚餐', '请客']
}

const classifyTask = async () => {
  if (!taskText.value.trim()) return
  
  isClassifying.value = true
  
  // 模拟AI分析
  setTimeout(() => {
    const text = taskText.value
    let bestMatch = { category: '待办', confidence: 30, icon: '📝', color: '#6B7280' }
    
    for (const [cat, keywords] of Object.entries(keywordMap)) {
      const matchCount = keywords.filter(k => text.includes(k)).length
      if (matchCount > 0) {
        const confidence = Math.min(50 + matchCount * 15, 95)
        if (confidence > bestMatch.confidence) {
          const catInfo = categories.find(c => c.name === cat)
          bestMatch = {
            category: cat,
            confidence,
            icon: catInfo?.icon || '📝',
            color: catInfo?.color || '#6B7280',
            suggestions: generateSuggestions(cat, text)
          }
        }
      }
    }
    
    classificationResult.value = bestMatch
    isClassifying.value = false
  }, 1500)
}

const generateSuggestions = (category, text) => {
  const suggestions = {
    '家务': ['建议设置周期性提醒', '可以分配给家庭成员'],
    '购物': ['建议设置预算', '可以添加到购物清单'],
    '工作': ['建议设置优先级', '可以添加截止日期'],
    '学习': ['建议制定学习计划', '可以设置复习提醒'],
    '健康': ['建议定期记录', '可以设置服药提醒'],
    '社交': ['建议提前准备', '可以添加到日历']
  }
  return suggestions[category] || []
}

const createTask = async () => {
  try {
    const familyId = uni.getStorageSync('currentFamilyId') || 1
    // 获取当前用户信息
    const userInfo = uni.getStorageSync('userInfo')
    const userId = userInfo?.id || userInfo?.userId

    // 默认今天 23:59
    const today = new Date().toISOString().split('T')[0]
    const dueTime = `${today}T23:59:00`

    const taskData = {
      title: taskText.value,
      categoryName: classificationResult.value.category,
      familyId: familyId,
      dueTime: dueTime,  // 后端期望 LocalDateTime 格式
      status: 0,
      priority: 0,
      creatorId: userId
    }

    console.log('[AIClassify] 创建任务请求数据:', taskData)
    await taskApi.create(taskData)
    uni.showToast({ title: '创建成功', icon: 'success' })
    setTimeout(() => uni.navigateBack(), 1500)
  } catch (e) {
    console.error('创建任务失败', e)
    let errorMsg = '创建失败'
    if (e?.message) {
      errorMsg = e.message
    } else if (typeof e === 'string') {
      errorMsg = e
    }
    uni.showModal({
      title: '创建失败',
      content: errorMsg,
      showCancel: false
    })
  }
}

const reset = () => {
  taskText.value = ''
  classificationResult.value = null
}

const showHelp = () => {
  uni.showModal({
    title: 'AI智能分类',
    content: '输入任务内容，AI会自动识别任务类型并分类',
    showCancel: false
  })
}

const goBack = () => uni.navigateBack()
</script>

<style lang="scss" scoped>
.page-container { min-height: 100vh; background: #F8FAFC; }
.header {
  display: flex; justify-content: space-between; align-items: center;
  padding: 50px 20px 20px;
  background: linear-gradient(135deg, #8B5CF6, #7C3AED);
  .header-left { width: 40px; height: 40px; display: flex; align-items: center; justify-content: center; }
  .header-left text { font-size: 32px; color: #fff; }
  .header-title { font-size: 20px; font-weight: 600; color: #fff; }
  .header-action { width: 40px; height: 40px; background: rgba(255,255,255,0.2); border-radius: 50%; display: flex; align-items: center; justify-content: center; }
}
.input-section { padding: 20px; position: relative; }
.task-input { width: 100%; min-height: 120px; background: #fff; border-radius: 16px; padding: 16px; font-size: 16px; }
.char-count { position: absolute; bottom: 30px; right: 30px; font-size: 12px; color: #9CA3AF; }
.classify-btn { margin: 0 20px 20px; height: 50px; background: #E5E7EB; border-radius: 25px; display: flex; align-items: center; justify-content: center; }
.classify-btn.active { background: linear-gradient(135deg, #8B5CF6, #7C3AED); }
.classify-btn text { font-size: 16px; font-weight: 500; color: #fff; }
.result-section { margin: 0 20px 20px; }
.result-header { margin-bottom: 12px; }
.result-header text { font-size: 16px; font-weight: 600; color: #1F2937; }
.category-card { background: #fff; border-radius: 16px; padding: 20px; display: flex; align-items: center; gap: 16px; margin-bottom: 16px; }
.category-icon { width: 60px; height: 60px; border-radius: 16px; display: flex; align-items: center; justify-content: center; }
.category-icon text { font-size: 28px; }
.category-info { flex: 1; }
.category-name { font-size: 20px; font-weight: 600; color: #1F2937; display: block; margin-bottom: 4px; }
.confidence { font-size: 13px; color: #9CA3AF; }
.suggestions-list { background: #fff; border-radius: 16px; padding: 16px; margin-bottom: 16px; }
.suggestions-title { font-size: 14px; font-weight: 600; color: #1F2937; margin-bottom: 12px; display: block; }
.suggestion-item { padding: 10px 0; border-bottom: 1px solid #F3F4F6; }
.suggestion-item:last-child { border-bottom: none; }
.suggestion-item text { font-size: 14px; color: #6B7280; }
.action-buttons { display: flex; gap: 12px; }
.btn-secondary, .btn-primary { flex: 1; height: 48px; border-radius: 24px; display: flex; align-items: center; justify-content: center; font-size: 15px; font-weight: 500; }
.btn-secondary { background: #F3F4F6; color: #6B7280; }
.btn-primary { background: linear-gradient(135deg, #8B5CF6, #7C3AED); color: #fff; }
.categories-grid { padding: 20px; }
.grid-title { font-size: 16px; font-weight: 600; color: #1F2937; margin-bottom: 16px; display: block; }
.grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 12px; }
.cat-item { background: #fff; border-radius: 16px; padding: 20px; text-align: center; }
.cat-icon { font-size: 32px; display: block; margin-bottom: 8px; }
.cat-name { font-size: 14px; color: #374151; }
</style>
