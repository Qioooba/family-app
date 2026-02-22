<template>
  <view class="page-container">
    <view class="header">
      <view class="header-left" @click="goBack"><text>‹</text></view>
      <view class="header-title">文件协作</view>
    </view>
    
    <view class="files-list"
>
      <view v-for="file in files" :key="file.id" class="file-card"
>
        <text class="file-icon">{{ file.icon }}</text>
        <view class="file-info"
>
          <text class="file-name">{{ file.name }}</text>
          <text class="file-meta">{{ file.size }} · {{ file.date }}</text>
        </view>
        <view class="file-actions"
>
          <text @click="preview(file)">👁️</text>
          <text @click="share(file)">📤</text>
        </view>
      </view>
    </view>
    
    <view class="upload-btn" @click="upload"
>
      <text>+ 上传文件</text>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'

const files = ref([
  { id: 1, name: '家庭预算表.xlsx', icon: '📊', size: '45KB', date: '今天' },
  { id: 2, name: '旅行计划.pdf', icon: '📄', size: '2.3MB', date: '昨天' },
  { id: 3, name: '购物清单.docx', icon: '📝', size: '12KB', date: '2天前' }
])

const preview = (f) => uni.showToast({ title: `预览${f.name}`, icon: 'none' })
const share = (f) => uni.showToast({ title: `分享${f.name}`, icon: 'none' })
const upload = () => uni.showToast({ title: '选择文件上传', icon: 'none' })

const goBack = () => uni.navigateBack()
</script>

<style lang="scss" scoped>
.page-container { min-height: 100vh; background: #F8FAFC; }
.header {
  display: flex; justify-content: space-between; align-items: center;
  padding: 50px 20px 20px;
  background: linear-gradient(135deg, #10B981, #059669);
  .header-left { width: 40px; height: 40px; display: flex; align-items: center; justify-content: center; }
  .header-left text { font-size: 32px; color: #fff; }
  .header-title { font-size: 20px; font-weight: 600; color: #fff; }
}
.files-list { padding: 20px; }
.file-card { display: flex; align-items: center; gap: 16px; background: #fff; border-radius: 16px; padding: 16px; margin-bottom: 12px; }
.file-icon { font-size: 36px; }
.file-info { flex: 1; }
.file-name { font-size: 15px; font-weight: 600; color: #1F2937; display: block; margin-bottom: 4px; }
.file-meta { font-size: 13px; color: #9CA3AF; }
.file-actions { display: flex; gap: 16px; }
.file-actions text { font-size: 20px; padding: 8px; }
.upload-btn { margin: 0 20px; height: 54px; background: linear-gradient(135deg, #10B981, #059669); border-radius: 27px; display: flex; align-items: center; justify-content: center; }
.upload-btn text { font-size: 16px; font-weight: 600; color: #fff; }
</style>
