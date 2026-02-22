<template>
  <view class="page-container"
>
    <view class="header"
>
      <view class="header-left" @click="goBack"><text>‹</text></view>
      <view class="header-title">拍照创建</view>
    </view>
    
    <view class="camera-section" v-if="!imageUrl"
>
      <view class="camera-placeholder" @click="takePhoto"
>
        <text class="camera-icon">📷</text>
        <text class="camera-text">点击拍照</text>
        <text class="camera-hint">拍摄清单、便签、白板等内容</text>
      </view>
      
      <view class="album-btn" @click="chooseFromAlbum"
>
        <text>从相册选择</text>
      </view>
    </view>
    
    <view class="preview-section" v-else
>
      <image class="preview-image" :src="imageUrl" mode="widthFix" />
      
      <view class="preview-actions"
>
        <view class="action-btn" @click="retake">重拍</view>
        <view class="action-btn primary" :class="{ loading: isProcessing }" @click="processImage"
>
          <text v-if="!isProcessing">识别内容</text>
          <text v-else>识别中...</text>
        </view>
      </view>
    </view>
    
    <view class="results-section" v-if="recognizedItems.length > 0"
>
      <view class="results-header"
>
        <text>识别结果</text>
        <text class="count">{{ recognizedItems.length }}项</text>
      </view>
      
      <view class="results-list"
>
        <view v-for="(item, index) in recognizedItems" :key="index" class="result-item"
>
          <view class="item-checkbox" :class="{ checked: item.selected }" @click="toggleItem(index)"
</view>
          <input class="item-input" v-model="item.text" />
          <text class="item-delete" @click="removeItem(index)">✕</text>
        </view>
      </view>
      
      <view class="add-item-btn" @click="addItem"
>
        <text>+ 添加项目</text>
      </view>
      
      <view class="create-btn" @click="createTasks"
>
        <text>创建任务</text>
      </view>
    </view>
    
    <view class="tips-section"
>
      <text class="tips-title">💡 拍照技巧</text>
      <view class="tip-item" v-for="(tip, i) in tips" :key="i"
>
        <text>{{ tip }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { taskApi } from '../../api/index.js'

const tips = [
  '确保光线充足，避免阴影',
  '保持文字清晰可见，不要模糊',
  '尽量正对拍摄，避免倾斜',
  '背景尽量简洁，突出文字内容'
]

const imageUrl = ref('')
const isProcessing = ref(false)
const recognizedItems = ref([])

const takePhoto = () => {
  uni.chooseImage({
    count: 1,
    sourceType: ['camera'],
    success: (res) => {
      imageUrl.value = res.tempFilePaths[0]
    }
  })
}

const chooseFromAlbum = () => {
  uni.chooseImage({
    count: 1,
    sourceType: ['album'],
    success: (res) => {
      imageUrl.value = res.tempFilePaths[0]
    }
  })
}

const retake = () => {
  imageUrl.value = ''
  recognizedItems.value = []
}

const processImage = () => {
  isProcessing.value = true
  
  // 模拟OCR识别
  setTimeout(() => {
    const mockItems = [
      { text: '去超市买牛奶', selected: true },
      { text: '交电费', selected: true },
      { text: '预约医生', selected: true },
      { text: '洗衣服', selected: false }
    ]
    recognizedItems.value = mockItems
    isProcessing.value = false
  }, 2000)
}

const toggleItem = (index) => {
  recognizedItems.value[index].selected = !recognizedItems.value[index].selected
}

const removeItem = (index) => {
  recognizedItems.value.splice(index, 1)
}

const addItem = () => {
  recognizedItems.value.push({ text: '', selected: true })
}

const createTasks = async () => {
  const selected = recognizedItems.value.filter(i => i.selected && i.text.trim())
  if (selected.length === 0) {
    uni.showToast({ title: '请选择要创建的任务', icon: 'none' })
    return
  }
  
  try {
    const familyId = uni.getStorageSync('currentFamilyId') || 1
    for (const item of selected) {
      await taskApi.create({ title: item.text, familyId })
    }
    uni.showToast({ title: `创建${selected.length}个任务成功`, icon: 'success' })
    setTimeout(() => uni.navigateBack(), 1500)
  } catch (e) {
    uni.showToast({ title: '创建失败', icon: 'none' })
  }
}

const goBack = () => uni.navigateBack()
</script>

<style lang="scss" scoped>
.page-container { min-height: 100vh; background: #F8FAFC; }
.header {
  display: flex; justify-content: space-between; align-items: center;
  padding: 50px 20px 20px;
  background: linear-gradient(135deg, #F59E0B, #D97706);
  .header-left { width: 40px; height: 40px; display: flex; align-items: center; justify-content: center; }
  .header-left text { font-size: 32px; color: #fff; }
  .header-title { font-size: 20px; font-weight: 600; color: #fff; }
}
.camera-section { padding: 40px 20px; }
.camera-placeholder { background: #fff; border-radius: 20px; padding: 60px; text-align: center; border: 2px dashed #E5E7EB; }
.camera-icon { font-size: 60px; display: block; margin-bottom: 16px; }
.camera-text { font-size: 18px; font-weight: 600; color: #374151; display: block; margin-bottom: 8px; }
.camera-hint { font-size: 13px; color: #9CA3AF; }
.album-btn { margin-top: 20px; text-align: center; }
.album-btn text { font-size: 15px; color: #F59E0B; background: rgba(245,158,11,0.1); padding: 12px 32px; border-radius: 24px; }
.preview-section { padding: 20px; }
.preview-image { width: 100%; border-radius: 16px; }
.preview-actions { display: flex; gap: 12px; margin-top: 16px; }
.action-btn { flex: 1; height: 48px; background: #F3F4F6; border-radius: 24px; display: flex; align-items: center; justify-content: center; font-size: 15px; color: #374151; }
.action-btn.primary { background: linear-gradient(135deg, #F59E0B, #D97706); color: #fff; }
.results-section { padding: 20px; }
.results-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.results-header text { font-size: 16px; font-weight: 600; color: #1F2937; }
.results-header .count { font-size: 14px; color: #9CA3AF; font-weight: normal; }
.results-list { background: #fff; border-radius: 16px; padding: 8px; margin-bottom: 12px; }
.result-item { display: flex; align-items: center; gap: 12px; padding: 12px; border-bottom: 1px solid #F3F4F6; }
.result-item:last-child { border-bottom: none; }
.item-checkbox { width: 22px; height: 22px; border: 2px solid #D1D5DB; border-radius: 50%; }
.item-checkbox.checked { background: #F59E0B; border-color: #F59E0B; }
.item-input { flex: 1; height: 40px; font-size: 15px; }
.item-delete { font-size: 16px; color: #EF4444; padding: 4px; }
.add-item-btn { text-align: center; padding: 12px; margin-bottom: 16px; }
.add-item-btn text { font-size: 14px; color: #F59E0B; }
.create-btn { height: 50px; background: linear-gradient(135deg, #F59E0B, #D97706); border-radius: 25px; display: flex; align-items: center; justify-content: center; }
.create-btn text { font-size: 16px; font-weight: 600; color: #fff; }
.tips-section { padding: 20px; }
.tips-title { font-size: 14px; font-weight: 600; color: #1F2937; margin-bottom: 12px; display: block; }
.tip-item { padding: 10px 0; }
.tip-item text { font-size: 13px; color: #6B7280; }
</style>
