<template>
  <view class="ai-shopping-page">
    <!-- 头部 -->
    <view class="ai-header">
      <view class="header-content">
        <view class="header-title">
          <text class="title-icon">🛒</text>
          <text class="title-text">AI购物助手</text>
        </view>
        <view class="header-subtitle">智能生成清单，轻松购物</view>
      </view>
      <view class="header-actions">
        <view class="action-btn" @click="goToInventory">
          <text class="btn-icon">📦</text>
          <text class="btn-text">库存</text>
        </view>
        <view class="action-btn" @click="scanCode">
          <text class="btn-icon">📷</text>
          <text class="btn-text">扫码</text>
        </view>
      </view>
    </view>

    <!-- AI智能输入区 -->
    <view class="ai-input-section">
      <view class="ai-input-card">
        <view class="ai-input-header">
          <text class="ai-badge">AI</text>
          <text class="ai-tip">告诉我你想买什么，AI帮你生成清单</text>
        </view>
        <view class="ai-input-box">
          <input
            v-model="aiInputText"
            class="ai-text-input"
            placeholder="例如：周末聚餐要买什么？做红烧肉需要什么材料？"
            confirm-type="send"
            @confirm="handleAIInput"
          />
          <view class="ai-input-actions">
            <view 
              class="voice-btn" 
              :class="{ recording: isRecording }"
              @touchstart="startVoiceInput"
              @touchend="stopVoiceInput"
            >
              <text class="voice-icon">{{ isRecording ? '🔴' : '🎤' }}</text>
              <text class="voice-text">{{ isRecording ? '松开结束' : '按住说话' }}</text>
            </view>
            <view class="send-btn" :class="{ active: aiInputText.trim() }" @click="handleAIInput">
              <text class="send-icon">➤</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 快捷清单模板 -->
    <view class="quick-templates">
      <view class="section-title">快捷清单</view>
      <scroll-view class="template-scroll" scroll-x>
        <view 
          v-for="(template, index) in quickTemplates" 
          :key="index"
          class="template-card"
          :style="{ background: template.gradient }"
          @click="createFromTemplate(template)"
        >
          <text class="template-icon">{{ template.icon }}</text>
          <text class="template-name">{{ template.name }}</text>
          <text class="template-desc">{{ template.desc }}</text>
        </view>
      </scroll-view>
    </view>

    <!-- 今日待买/预警 -->
    <view class="today-section" v-if="urgentItems.length > 0 || todayLists.length > 0">
      <view class="section-header">
        <text class="section-title">今日待买</text>
        <text class="section-badge" v-if="urgentItems.length > 0">{{ urgentItems.length }}项紧急</text>
      </view>
      
      <!-- 预警物品 -->
      <view class="urgent-items" v-if="urgentItems.length > 0">
        <view 
          v-for="(item, index) in urgentItems.slice(0, 3)" 
          :key="index"
          class="urgent-item"
          @click="addToTodayList(item)"
        >
          <view class="urgent-icon" :class="{ warning: item.type === 'expiry', low: item.type === 'low' }">
            {{ item.type === 'expiry' ? '⚠️' : '🔻' }}
          </view>
          <view class="urgent-info">
            <text class="urgent-name">{{ item.name }}</text>
            <text class="urgent-reason">{{ item.reason }}</text>
          </view>
          <view class="urgent-action">
            <text class="action-text">去补货</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 进行中的清单 -->
    <view class="active-lists-section">
      <view class="section-header">
        <text class="section-title">购物清单</text>
        <view class="view-toggle" @click="showAllLists = !showAllLists">
          <text class="toggle-text">{{ showAllLists ? '收起' : '查看全部' }}</text>
          <text class="toggle-icon" :class="{ rotated: showAllLists }">›</text>
        </view>
      </view>
      
      <view class="lists-container">
        <view 
          v-for="(list, index) in displayedLists" 
          :key="list.id"
          class="shopping-list-card"
          @click="viewListDetail(list)"
        >
          <view class="list-card-header">
            <view class="list-info">
              <text class="list-name">{{ list.name }}</text>
              <text class="list-meta">{{ formatDate(list.createdAt) }} · {{ list.items?.length || 0 }}项</text>
            </view>
            <view class="list-progress">
              <view class="progress-ring" :style="{ '--progress': list.progress + '%' }">
                <text class="progress-text">{{ list.progress }}%</text>
              </view>
            </view>
          </view>
          
          <!-- 清单预览 -->
          <view class="list-preview" v-if="list.items && list.items.length > 0">
            <view 
              v-for="(item, i) in list.items.slice(0, 4)" 
              :key="i"
              class="preview-item"
              :class="{ checked: item.checked }"
            >
              <view class="preview-check" :class="{ checked: item.checked }"></view>
              <text class="preview-name">{{ item.name }}</text>
            </view>
            <view v-if="list.items.length > 4" class="more-items">
              +{{ list.items.length - 4 }} 项
            </view>
          </view>
          
          <view class="list-card-footer">
            <view class="budget-info" v-if="list.estimatedAmount">
              <text class="budget-label">预算</text>
              <text class="budget-value">¥{{ list.estimatedAmount }}</text>
            </view>
            <view class="list-status" :class="{ completed: list.progress === 100 }">
              {{ list.progress === 100 ? '已完成' : '进行中' }}
            </view>
          </view>
        </view>
      </view>
      
      <!-- 空状态 -->
      <view class="empty-state" v-if="activeLists.length === 0">
        <view class="empty-icon">🛒</view>
        <text class="empty-text">暂无购物清单</text>
        <text class="empty-tip">点击上方"新建清单"开始购物</text>
      </view>
    </view>

    <!-- 底部新建按钮 -->
    <view class="fab-button" @click="showCreateOptions">
      <text class="fab-icon">+</text>
    </view>

    <!-- AI生成中弹窗 -->
    <view v-if="aiGenerating" class="ai-generating-modal">
      <view class="modal-mask"></view>
      <view class="generating-content">
        <view class="ai-avatar">
          <view class="avatar-ring"></view>
          <text class="avatar-icon">🤖</text>
        </view>
        <text class="generating-text">AI正在生成购物清单...</text>
        <view class="generating-items">
          <view v-for="(item, i) in generatingItems" :key="i" class="generating-item" :style="{ animationDelay: i * 0.1 + 's' }">
            <text>{{ item }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 创建选项弹窗 -->
    <view v-if="showCreateModal" class="create-modal">
      <view class="modal-mask" @click="showCreateModal = false"></view>
      <view class="create-modal-content">
        <view class="create-options">
          <view class="create-option" @click="createManualList">
            <view class="option-icon" style="background: linear-gradient(135deg, #3b82f6, #2563eb);">📝</view>
            <text class="option-name">手动创建</text>
          </view>
          <view class="create-option" @click="createByVoice">
            <view class="option-icon" style="background: linear-gradient(135deg, #8b5cf6, #6366f1);">🎤</view>
            <text class="option-name">语音创建</text>
          </view>
          <view class="create-option" @click="createByScan">
            <view class="option-icon" style="background: linear-gradient(135deg, #10b981, #059669);">📷</view>
            <text class="option-name">扫码创建</text>
          </view>
        </view>
        <view class="cancel-btn" @click="showCreateModal = false">取消</view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { shoppingApi } from '../../api/index.js'

// 响应式数据
const aiInputText = ref('')
const isRecording = ref(false)
const aiGenerating = ref(false)
const showCreateModal = ref(false)
const showAllLists = ref(false)
const activeLists = ref([])
const urgentItems = ref([])
const todayLists = ref([])
const generatingItems = ref(['分析需求...', '智能分类...', '计算数量...', '预估价格...'])

// 录音管理器
let recorderManager = null

// 快捷模板
const quickTemplates = [
  { name: '周末聚餐', icon: '🍽️', desc: '10-15人聚餐', gradient: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)' },
  { name: '日常补货', icon: '🏠', desc: '常用日用品', gradient: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)' },
  { name: '早餐食材', icon: '🍳', desc: '一周早餐准备', gradient: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)' },
  { name: '火锅派对', icon: '🍲', desc: '火锅材料清单', gradient: 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)' },
  { name: '宝宝用品', icon: '👶', desc: '母婴用品采购', gradient: 'linear-gradient(135deg, #a8edea 0%, #fed6e3 100%)' },
  { name: '清洁大扫除', icon: '🧹', desc: '清洁用品清单', gradient: 'linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%)' }
]

// 显示的清单列表
const displayedLists = computed(() => {
  return showAllLists.value ? activeLists.value : activeLists.value.slice(0, 3)
})

// 页面加载
onMounted(() => {
  initRecorder()
  loadData()
})

// 页面显示时刷新
onShow(() => {
  loadData()
})

// 初始化录音
const initRecorder = () => {
  // #ifdef MP-WEIXIN || APP-PLUS
  recorderManager = uni.getRecorderManager()
  recorderManager.onStart(() => {
    console.log('录音开始')
  })
  recorderManager.onStop((res) => {
    console.log('录音结束', res)
    handleVoiceResult(res.tempFilePath)
  })
  recorderManager.onError((err) => {
    console.error('录音错误', err)
    isRecording.value = false
    uni.showToast({ title: '录音失败', icon: 'none' })
  })
  // #endif
}

// 加载数据
const loadData = async () => {
  await Promise.all([
    loadShoppingLists(),
    loadUrgentItems()
  ])
}

// 加载购物清单
const loadShoppingLists = async () => {
  try {
    const familyId = uni.getStorageSync('currentFamilyId') || 1
    const res = await shoppingApi.getLists(familyId)
    // 处理数据，添加进度计算
    activeLists.value = (res || []).map(list => {
      const items = list.items || []
      const total = items.length
      const checked = items.filter(i => i.checked || i.status === 1).length
      return {
        ...list,
        progress: total > 0 ? Math.round((checked / total) * 100) : 0
      }
    }).sort((a, b) => new Date(b.createdAt || 0) - new Date(a.createdAt || 0))
  } catch (e) {
    console.error('加载购物清单失败', e)
    // 使用模拟数据
    activeLists.value = getMockLists()
  }
}

// 加载紧急物品（库存预警）
const loadUrgentItems = async () => {
  try {
    const familyId = uni.getStorageSync('currentFamilyId') || 1
    // 这里应该调用库存API获取预警信息
    // const res = await shoppingApi.getExpiringItems(familyId)
    
    // 模拟数据
    urgentItems.value = [
      { name: '牛奶', type: 'expiry', reason: '还有2天过期' },
      { name: '洗衣液', type: 'low', reason: '库存不足' },
      { name: '鸡蛋', type: 'expiry', reason: '还有3天过期' }
    ]
  } catch (e) {
    console.error('加载预警物品失败', e)
    urgentItems.value = []
  }
}

// AI输入处理
const handleAIInput = async () => {
  const input = aiInputText.value.trim()
  if (!input) {
    uni.showToast({ title: '请输入内容', icon: 'none' })
    return
  }
  
  aiGenerating.value = true
  
  try {
    // 模拟AI生成过程
    await simulateAIGeneration()
    
    // 调用真实的AI接口（待后端实现）
    // const result = await shoppingApi.generateListByAI({ prompt: input })
    
    // 模拟AI返回结果
    const mockResult = generateMockAIResult(input)
    
    // 创建清单
    const familyId = uni.getStorageSync('currentFamilyId') || 1
    const newList = {
      name: mockResult.name,
      familyId: familyId,
      items: mockResult.items,
      estimatedAmount: mockResult.estimatedAmount
    }
    
    // 保存清单
    // await shoppingApi.createList(newList)
    
    uni.showToast({ title: '清单生成成功', icon: 'success' })
    aiInputText.value = ''
    
    // 跳转到详情页编辑
    setTimeout(() => {
      uni.navigateTo({
        url: `/pages/ai-shopping/detail?mode=ai&data=${encodeURIComponent(JSON.stringify(newList))}`
      })
    }, 500)
    
  } catch (e) {
    console.error('AI生成失败', e)
    uni.showToast({ title: '生成失败，请重试', icon: 'none' })
  } finally {
    aiGenerating.value = false
  }
}

// 模拟AI生成过程
const simulateAIGeneration = () => {
  return new Promise(resolve => setTimeout(resolve, 2000))
}

// 生成模拟AI结果
const generateMockAIResult = (input) => {
  if (input.includes('红烧肉')) {
    return {
      name: '红烧肉材料清单',
      items: [
        { name: '五花肉', quantity: 500, unit: '克', category: '生鲜', estimatedPrice: 35 },
        { name: '冰糖', quantity: 30, unit: '克', category: '调料', estimatedPrice: 5 },
        { name: '生抽', quantity: 2, unit: '勺', category: '调料', estimatedPrice: 8 },
        { name: '老抽', quantity: 1, unit: '勺', category: '调料', estimatedPrice: 6 },
        { name: '料酒', quantity: 2, unit: '勺', category: '调料', estimatedPrice: 10 },
        { name: '生姜', quantity: 3, unit: '片', category: '生鲜', estimatedPrice: 3 },
        { name: '大葱', quantity: 2, unit: '段', category: '生鲜', estimatedPrice: 4 },
        { name: '八角', quantity: 2, unit: '个', category: '调料', estimatedPrice: 3 }
      ],
      estimatedAmount: 74
    }
  } else if (input.includes('聚会') || input.includes('聚餐')) {
    return {
      name: '周末聚会采购清单',
      items: [
        { name: '可乐', quantity: 2, unit: '瓶', category: '饮料', estimatedPrice: 16 },
        { name: '薯片', quantity: 3, unit: '包', category: '零食', estimatedPrice: 24 },
        { name: '水果拼盘', quantity: 2, unit: '份', category: '生鲜', estimatedPrice: 50 },
        { name: '蛋糕', quantity: 1, unit: '个', category: '食品', estimatedPrice: 88 },
        { name: '纸巾', quantity: 2, unit: '盒', category: '日用品', estimatedPrice: 10 },
        { name: '一次性杯子', quantity: 50, unit: '个', category: '日用品', estimatedPrice: 15 }
      ],
      estimatedAmount: 203
    }
  } else {
    return {
      name: 'AI生成购物清单',
      items: [
        { name: '苹果', quantity: 1, unit: '斤', category: '生鲜', estimatedPrice: 8 },
        { name: '香蕉', quantity: 1, unit: '把', category: '生鲜', estimatedPrice: 6 },
        { name: '牛奶', quantity: 1, unit: '箱', category: '饮品', estimatedPrice: 45 },
        { name: '面包', quantity: 1, unit: '袋', category: '食品', estimatedPrice: 12 },
        { name: '鸡蛋', quantity: 30, unit: '个', category: '生鲜', estimatedPrice: 25 }
      ],
      estimatedAmount: 96
    }
  }
}

// 开始语音输入
const startVoiceInput = () => {
  // #ifdef H5
  // H5不支持录音，使用模拟
  isRecording.value = true
  uni.showToast({ title: 'H5暂不支持录音', icon: 'none' })
  setTimeout(() => {
    isRecording.value = false
  }, 1000)
  return
  // #endif
  
  isRecording.value = true
  if (recorderManager) {
    recorderManager.start({ duration: 60000, format: 'mp3' })
  }
}

// 停止语音输入
const stopVoiceInput = () => {
  isRecording.value = false
  if (recorderManager) {
    recorderManager.stop()
  }
}

// 处理语音识别结果
const handleVoiceResult = (tempFilePath) => {
  // 这里应该调用语音识别API
  uni.showLoading({ title: '识别中...' })
  
  // 模拟语音识别
  setTimeout(() => {
    uni.hideLoading()
    const mockTexts = ['帮我买苹果和牛奶', '周末聚会的零食饮料', '做西红柿炒鸡蛋的材料']
    aiInputText.value = mockTexts[Math.floor(Math.random() * mockTexts.length)]
    uni.showToast({ title: '识别成功', icon: 'success' })
    
    // 自动提交
    setTimeout(() => {
      handleAIInput()
    }, 500)
  }, 1500)
}

// 从模板创建
const createFromTemplate = (template) => {
  const familyId = uni.getStorageSync('currentFamilyId') || 1
  const templateItems = getTemplateItems(template.name)
  
  const newList = {
    name: template.name + '清单',
    familyId: familyId,
    items: templateItems,
    estimatedAmount: templateItems.reduce((sum, item) => sum + (item.estimatedPrice || 0), 0)
  }
  
  uni.navigateTo({
    url: `/pages/ai-shopping/detail?mode=template&data=${encodeURIComponent(JSON.stringify(newList))}`
  })
}

// 获取模板物品
const getTemplateItems = (templateName) => {
  const templates = {
    '周末聚餐': [
      { name: '可乐', quantity: 2, unit: '瓶', category: '饮料', estimatedPrice: 16 },
      { name: '薯片', quantity: 3, unit: '包', category: '零食', estimatedPrice: 24 },
      { name: '水果拼盘', quantity: 2, unit: '份', category: '生鲜', estimatedPrice: 50 },
      { name: '蛋糕', quantity: 1, unit: '个', category: '食品', estimatedPrice: 88 },
      { name: '纸巾', quantity: 2, unit: '盒', category: '日用品', estimatedPrice: 10 }
    ],
    '日常补货': [
      { name: '洗衣液', quantity: 2, unit: '瓶', category: '洗护', estimatedPrice: 45 },
      { name: '抽纸', quantity: 3, unit: '提', category: '日用品', estimatedPrice: 36 },
      { name: '牙膏', quantity: 2, unit: '支', category: '洗护', estimatedPrice: 24 },
      { name: '洗发水', quantity: 1, unit: '瓶', category: '洗护', estimatedPrice: 35 },
      { name: '垃圾袋', quantity: 5, unit: '卷', category: '日用品', estimatedPrice: 15 }
    ],
    '早餐食材': [
      { name: '鸡蛋', quantity: 30, unit: '个', category: '生鲜', estimatedPrice: 25 },
      { name: '牛奶', quantity: 2, unit: '箱', category: '饮品', estimatedPrice: 90 },
      { name: '面包', quantity: 2, unit: '袋', category: '食品', estimatedPrice: 24 },
      { name: '燕麦片', quantity: 1, unit: '袋', category: '食品', estimatedPrice: 28 },
      { name: '水果', quantity: 3, unit: '斤', category: '生鲜', estimatedPrice: 30 }
    ],
    '火锅派对': [
      { name: '肥牛卷', quantity: 2, unit: '盒', category: '生鲜', estimatedPrice: 76 },
      { name: '羊肉卷', quantity: 2, unit: '盒', category: '生鲜', estimatedPrice: 68 },
      { name: '虾滑', quantity: 1, unit: '袋', category: '生鲜', estimatedPrice: 35 },
      { name: '火锅底料', quantity: 1, unit: '包', category: '调料', estimatedPrice: 18 },
      { name: '蔬菜拼盘', quantity: 2, unit: '份', category: '生鲜', estimatedPrice: 40 },
      { name: '豆腐', quantity: 1, unit: '盒', category: '生鲜', estimatedPrice: 8 },
      { name: '金针菇', quantity: 2, unit: '包', category: '生鲜', estimatedPrice: 12 }
    ]
  }
  
  return templates[templateName] || templates['日常补货']
}

// 查看清单详情
const viewListDetail = (list) => {
  uni.navigateTo({
    url: `/pages/ai-shopping/detail?id=${list.id}`
  })
}

// 添加到今日清单
const addToTodayList = (item) => {
  uni.showToast({ title: `已添加 ${item.name} 到清单`, icon: 'success' })
}

// 显示创建选项
const showCreateOptions = () => {
  showCreateModal.value = true
}

// 手动创建
const createManualList = () => {
  showCreateModal.value = false
  uni.navigateTo({
    url: '/pages/ai-shopping/detail?mode=create'
  })
}

// 语音创建
const createByVoice = () => {
  showCreateModal.value = false
  // 触发语音输入
  setTimeout(() => {
    startVoiceInput()
  }, 300)
}

// 扫码创建
const createByScan = () => {
  showCreateModal.value = false
  scanCode()
}

// 扫码
const scanCode = () => {
  uni.scanCode({
    onlyFromCamera: false,
    scanType: ['barCode', 'qrCode'],
    success: (res) => {
      // 扫码成功，跳转到添加页面
      uni.navigateTo({
        url: `/pages/ai-shopping/detail?mode=scan&barcode=${res.result}`
      })
    },
    fail: (err) => {
      console.error('扫码失败:', err)
    }
  })
}

// 跳转到库存管理
const goToInventory = () => {
  uni.navigateTo({
    url: '/pages/shopping/inventory'
  })
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '今天'
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now.getDate() - date.getDate()
  
  if (diff === 0) return '今天'
  if (diff === 1) return '昨天'
  return `${date.getMonth() + 1}月${date.getDate()}日`
}

// 模拟清单数据
const getMockLists = () => {
  return [
    {
      id: 1,
      name: '周末超市采购',
      items: [
        { name: '牛奶', checked: true },
        { name: '面包', checked: true },
        { name: '鸡蛋', checked: false },
        { name: '苹果', checked: false }
      ],
      progress: 50,
      estimatedAmount: 128,
      createdAt: new Date().toISOString()
    },
    {
      id: 2,
      name: '日用品补货',
      items: [
        { name: '洗衣液', checked: false },
        { name: '抽纸', checked: false },
        { name: '牙膏', checked: false }
      ],
      progress: 0,
      estimatedAmount: 85,
      createdAt: new Date(Date.now() - 86400000).toISOString()
    }
  ]
}
</script>

<style lang="scss" scoped>
.ai-shopping-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #f8f9fc 0%, #f0f4f8 100%);
  padding-bottom: 120rpx;
}

// 头部
.ai-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 80rpx 32rpx 40rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 0 0 40rpx 40rpx;
  box-shadow: 0 20rpx 60rpx rgba(102, 126, 234, 0.25);
  
  .header-content {
    .header-title {
      display: flex;
      align-items: center;
      gap: 12rpx;
      
      .title-icon {
        font-size: 44rpx;
      }
      
      .title-text {
        font-size: 40rpx;
        font-weight: 700;
        color: #fff;
      }
    }
    
    .header-subtitle {
      font-size: 26rpx;
      color: rgba(255,255,255,0.8);
      margin-top: 8rpx;
    }
  }
  
  .header-actions {
    display: flex;
    gap: 16rpx;
    
    .action-btn {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 16rpx 20rpx;
      background: rgba(255,255,255,0.15);
      border-radius: 16rpx;
      backdrop-filter: blur(10px);
      
      .btn-icon {
        font-size: 36rpx;
        margin-bottom: 4rpx;
      }
      
      .btn-text {
        font-size: 22rpx;
        color: rgba(255,255,255,0.9);
      }
    }
  }
}

// AI输入区
.ai-input-section {
  padding: 24rpx 32rpx;
  margin-top: -20rpx;
  
  .ai-input-card {
    background: #fff;
    border-radius: 28rpx;
    padding: 28rpx;
    box-shadow: 0 8rpx 32rpx rgba(0,0,0,0.08);
    
    .ai-input-header {
      display: flex;
      align-items: center;
      gap: 12rpx;
      margin-bottom: 20rpx;
      
      .ai-badge {
        padding: 4rpx 12rpx;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        color: #fff;
        font-size: 22rpx;
        font-weight: 600;
        border-radius: 8rpx;
      }
      
      .ai-tip {
        font-size: 24rpx;
        color: #64748b;
      }
    }
    
    .ai-input-box {
      .ai-text-input {
        width: 100%;
        height: 88rpx;
        background: #f8f9fc;
        border-radius: 16rpx;
        padding: 0 24rpx;
        font-size: 28rpx;
        color: #2d3748;
        margin-bottom: 20rpx;
      }
      
      .ai-input-actions {
        display: flex;
        justify-content: space-between;
        align-items: center;
        
        .voice-btn {
          display: flex;
          align-items: center;
          gap: 12rpx;
          padding: 16rpx 28rpx;
          background: #f0f4f8;
          border-radius: 32rpx;
          transition: all 0.3s;
          
          &.recording {
            background: #fee2e2;
            animation: pulse 1s infinite;
          }
          
          .voice-icon {
            font-size: 32rpx;
          }
          
          .voice-text {
            font-size: 26rpx;
            color: #4a5568;
          }
        }
        
        .send-btn {
          width: 72rpx;
          height: 72rpx;
          display: flex;
          align-items: center;
          justify-content: center;
          background: #e2e8f0;
          border-radius: 50%;
          transition: all 0.3s;
          
          &.active {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          }
          
          .send-icon {
            font-size: 32rpx;
            color: #fff;
            transform: rotate(-45deg);
          }
        }
      }
    }
  }
}

// 快捷模板
.quick-templates {
  padding: 24rpx 0;
  
  .section-title {
    font-size: 32rpx;
    font-weight: 600;
    color: #2d3748;
    padding: 0 32rpx;
    margin-bottom: 20rpx;
  }
  
  .template-scroll {
    white-space: nowrap;
    padding: 0 32rpx;
    
    .template-card {
      display: inline-flex;
      flex-direction: column;
      align-items: center;
      padding: 28rpx 32rpx;
      border-radius: 24rpx;
      margin-right: 20rpx;
      min-width: 160rpx;
      box-shadow: 0 8rpx 24rpx rgba(0,0,0,0.1);
      
      .template-icon {
        font-size: 48rpx;
        margin-bottom: 12rpx;
      }
      
      .template-name {
        font-size: 28rpx;
        font-weight: 600;
        color: #fff;
        margin-bottom: 4rpx;
      }
      
      .template-desc {
        font-size: 22rpx;
        color: rgba(255,255,255,0.8);
      }
    }
  }
}

// 今日待买
.today-section {
  padding: 24rpx 32rpx;
  
  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20rpx;
    
    .section-title {
      font-size: 32rpx;
      font-weight: 600;
      color: #2d3748;
    }
    
    .section-badge {
      padding: 6rpx 16rpx;
      background: #fee2e2;
      color: #dc2626;
      font-size: 22rpx;
      border-radius: 20rpx;
    }
  }
  
  .urgent-items {
    .urgent-item {
      display: flex;
      align-items: center;
      gap: 20rpx;
      padding: 24rpx;
      background: #fff;
      border-radius: 20rpx;
      margin-bottom: 16rpx;
      box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.06);
      
      .urgent-icon {
        width: 72rpx;
        height: 72rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        background: #fef3c7;
        border-radius: 50%;
        font-size: 36rpx;
        
        &.warning {
          background: #fee2e2;
        }
        
        &.low {
          background: #dbeafe;
        }
      }
      
      .urgent-info {
        flex: 1;
        
        .urgent-name {
          display: block;
          font-size: 30rpx;
          font-weight: 600;
          color: #2d3748;
        }
        
        .urgent-reason {
          font-size: 24rpx;
          color: #dc2626;
          margin-top: 4rpx;
        }
      }
      
      .urgent-action {
        padding: 12rpx 24rpx;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        border-radius: 28rpx;
        
        .action-text {
          font-size: 24rpx;
          color: #fff;
        }
      }
    }
  }
}

// 购物清单
.active-lists-section {
  padding: 24rpx 32rpx;
  
  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20rpx;
    
    .section-title {
      font-size: 32rpx;
      font-weight: 600;
      color: #2d3748;
    }
    
    .view-toggle {
      display: flex;
      align-items: center;
      gap: 8rpx;
      
      .toggle-text {
        font-size: 26rpx;
        color: #667eea;
      }
      
      .toggle-icon {
        font-size: 32rpx;
        color: #667eea;
        transition: transform 0.3s;
        
        &.rotated {
          transform: rotate(90deg);
        }
      }
    }
  }
  
  .lists-container {
    .shopping-list-card {
      background: #fff;
      border-radius: 28rpx;
      padding: 28rpx;
      margin-bottom: 24rpx;
      box-shadow: 0 8rpx 32rpx rgba(0,0,0,0.08);
      
      .list-card-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 20rpx;
        
        .list-info {
          .list-name {
            display: block;
            font-size: 32rpx;
            font-weight: 600;
            color: #2d3748;
          }
          
          .list-meta {
            font-size: 24rpx;
            color: #718096;
            margin-top: 6rpx;
          }
        }
        
        .progress-ring {
          width: 88rpx;
          height: 88rpx;
          border-radius: 50%;
          background: conic-gradient(
            #667eea var(--progress, 0%),
            #e2e8f0 var(--progress, 0%)
          );
          display: flex;
          align-items: center;
          justify-content: center;
          
          &::before {
            content: '';
            width: 72rpx;
            height: 72rpx;
            background: #fff;
            border-radius: 50%;
            position: absolute;
          }
          
          .progress-text {
            font-size: 24rpx;
            font-weight: 600;
            color: #667eea;
            position: relative;
            z-index: 1;
          }
        }
      }
      
      .list-preview {
        display: flex;
        flex-wrap: wrap;
        gap: 16rpx;
        padding: 20rpx;
        background: #f8f9fc;
        border-radius: 16rpx;
        margin-bottom: 20rpx;
        
        .preview-item {
          display: flex;
          align-items: center;
          gap: 8rpx;
          
          &.checked {
            opacity: 0.5;
          }
          
          .preview-check {
            width: 28rpx;
            height: 28rpx;
            border: 2rpx solid #cbd5e0;
            border-radius: 50%;
            
            &.checked {
              background: #48bb78;
              border-color: #48bb78;
            }
          }
          
          .preview-name {
            font-size: 26rpx;
            color: #4a5568;
            text-decoration: line-through;
            
            .preview-item:not(.checked) & {
              text-decoration: none;
            }
          }
        }
        
        .more-items {
          font-size: 24rpx;
          color: #a0aec0;
          padding: 4rpx 12rpx;
          background: #e2e8f0;
          border-radius: 12rpx;
        }
      }
      
      .list-card-footer {
        display: flex;
        justify-content: space-between;
        align-items: center;
        
        .budget-info {
          .budget-label {
            font-size: 24rpx;
            color: #718096;
            margin-right: 8rpx;
          }
          
          .budget-value {
            font-size: 30rpx;
            font-weight: 600;
            color: #667eea;
          }
        }
        
        .list-status {
          padding: 8rpx 20rpx;
          background: #e0e7ff;
          color: #667eea;
          font-size: 24rpx;
          border-radius: 20rpx;
          
          &.completed {
            background: #c6f6d5;
            color: #38a169;
          }
        }
      }
    }
  }
  
  .empty-state {
    text-align: center;
    padding: 80rpx 40rpx;
    
    .empty-icon {
      font-size: 80rpx;
      margin-bottom: 24rpx;
    }
    
    .empty-text {
      display: block;
      font-size: 32rpx;
      color: #4a5568;
      margin-bottom: 12rpx;
    }
    
    .empty-tip {
      font-size: 26rpx;
      color: #a0aec0;
    }
  }
}

// 悬浮按钮
.fab-button {
  position: fixed;
  right: 40rpx;
  bottom: 140rpx;
  width: 112rpx;
  height: 112rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  box-shadow: 0 12rpx 40rpx rgba(102, 126, 234, 0.4);
  z-index: 100;
  
  .fab-icon {
    font-size: 56rpx;
    color: #fff;
    font-weight: 300;
  }
}

// AI生成弹窗
.ai-generating-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
  
  .modal-mask {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0,0,0,0.6);
  }
  
  .generating-content {
    position: relative;
    background: #fff;
    border-radius: 32rpx;
    padding: 60rpx 80rpx;
    text-align: center;
    
    .ai-avatar {
      position: relative;
      width: 120rpx;
      height: 120rpx;
      margin: 0 auto 40rpx;
      
      .avatar-ring {
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        border: 4rpx solid #667eea;
        border-radius: 50%;
        animation: spin 2s linear infinite;
      }
      
      .avatar-icon {
        font-size: 64rpx;
        line-height: 120rpx;
      }
    }
    
    .generating-text {
      display: block;
      font-size: 32rpx;
      font-weight: 600;
      color: #2d3748;
      margin-bottom: 40rpx;
    }
    
    .generating-items {
      .generating-item {
        padding: 12rpx 0;
        font-size: 26rpx;
        color: #718096;
        opacity: 0;
        animation: fadeInUp 0.5s forwards;
      }
    }
  }
}

// 创建选项弹窗
.create-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 1000;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  
  .modal-mask {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0,0,0,0.5);
  }
  
  .create-modal-content {
    position: relative;
    background: #f0f4f8;
    border-radius: 32rpx 32rpx 0 0;
    padding: 32rpx;
    animation: slideUp 0.3s ease;
    
    .create-options {
      display: flex;
      justify-content: space-around;
      padding: 40rpx 0;
      background: #fff;
      border-radius: 24rpx;
      margin-bottom: 24rpx;
      
      .create-option {
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 16rpx;
        
        .option-icon {
          width: 100rpx;
          height: 100rpx;
          display: flex;
          align-items: center;
          justify-content: center;
          border-radius: 50%;
          font-size: 48rpx;
        }
        
        .option-name {
          font-size: 26rpx;
          color: #4a5568;
        }
      }
    }
    
    .cancel-btn {
      text-align: center;
      padding: 28rpx;
      background: #fff;
      border-radius: 24rpx;
      font-size: 30rpx;
      color: #4a5568;
    }
  }
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

@keyframes pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.05); }
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes slideUp {
  from { transform: translateY(100%); }
  to { transform: translateY(0); }
}
</style>
