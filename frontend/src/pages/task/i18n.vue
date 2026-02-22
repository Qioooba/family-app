<template>
  <view class="page-container">
    <view class="header"
003e
      <view class="header-left" @click="goBack"><text>‹</text></view>
      <view class="header-title">国际化</view>
    </view>
    
    <view class="language-section"
003e
      <text class="section-title">选择语言</text>
      
      <view class="language-list"
003e
        <view 
          v-for="lang in languages" 
          :key="lang.code"
          class="language-item"
          :class="{ active: currentLang === lang.code }"
          @click="selectLang(lang.code)"
        >
          <text class="lang-flag">{{ lang.flag }}</text>
          <view class="lang-info"
003e
            <text class="lang-name">{{ lang.name }}</text>
            <text class="lang-local">{{ lang.localName }}</text>
          </view>
          <text v-if="currentLang === lang.code" class="check-mark">✓</text>
        </view>
      </view>
    </view>
    
    <view class="preview-section"
003e
      <text class="section-title">预览</text>
      
      <view class="preview-card"
003e
        <text class="preview-label">{{ t('welcome') }}</text>
        <text class="preview-text">{{ t('taskCount', { count: 5 }) }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'

const languages = [
  { code: 'zh-CN', name: '简体中文', localName: '简体中文', flag: '🇨🇳' },
  { code: 'zh-TW', name: '繁体中文', localName: '繁體中文', flag: '🇹🇼' },
  { code: 'en', name: 'English', localName: 'English', flag: '🇺🇸' },
  { code: 'ja', name: '日语', localName: '日本語', flag: '🇯🇵' },
  { code: 'ko', name: '韩语', localName: '한국어', flag: '🇰🇷' }
]

const currentLang = ref('zh-CN')

const messages = {
  'zh-CN': { welcome: '欢迎使用', taskCount: '您有 {count} 个任务' },
  'zh-TW': { welcome: '歡迎使用', taskCount: '您有 {count} 個任務' },
  'en': { welcome: 'Welcome', taskCount: 'You have {count} tasks' },
  'ja': { welcome: 'ようこそ', taskCount: '{count}件のタスクがあります' },
  'ko': { welcome: '환영합니다', taskCount: '{count}개의 작업이 있습니다' }
}

const t = (key, params = {}) => {
  let text = messages[currentLang.value]?.[key] || key
  Object.entries(params).forEach(([k, v]) => {
    text = text.replace(`{${k}}`, v)
  })
  return text
}

const selectLang = (code) => {
  currentLang.value = code
  uni.setStorageSync('language', code)
  uni.showToast({ title: '已切换语言', icon: 'none' })
}

const goBack = () => uni.navigateBack()
</script>

<style lang="scss" scoped>
.page-container { min-height: 100vh; background: #F8FAFC; }
.header {
  display: flex; justify-content: space-between; align-items: center;
  padding: 50px 20px 20px;
  background: linear-gradient(135deg, #6366F1, #4F46E5);
  .header-left { width: 40px; height: 40px; display: flex; align-items: center; justify-content: center; }
  .header-left text { font-size: 32px; color: #fff; }
  .header-title { font-size: 20px; font-weight: 600; color: #fff; }
}
.language-section { padding: 20px; }
.section-title { font-size: 16px; font-weight: 600; color: #1F2937; margin-bottom: 16px; display: block; }
.language-list { background: #fff; border-radius: 16px; padding: 8px; }
.language-item { display: flex; align-items: center; gap: 12px; padding: 16px; border-bottom: 1px solid #F3F4F6; }
.language-item:last-child { border-bottom: none; }
.language-item.active { background: #EEF2FF; border-radius: 12px; }
.lang-flag { font-size: 28px; }
.lang-info { flex: 1; }
.lang-name { font-size: 15px; font-weight: 600; color: #1F2937; display: block; }
.lang-local { font-size: 13px; color: #9CA3AF; }
.check-mark { font-size: 20px; color: #6366F1; font-weight: bold; }
.preview-section { padding: 0 20px; }
.preview-card { background: #fff; border-radius: 16px; padding: 20px; }
.preview-label { font-size: 14px; color: #6B7280; display: block; margin-bottom: 8px; }
.preview-text { font-size: 18px; font-weight: 600; color: #1F2937; }
</style>
