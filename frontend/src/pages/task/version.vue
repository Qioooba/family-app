<template>
  <view class="page-container">
    <view class="header">
      <view class="header-left" @click="goBack"><text>‹</text></view>
      <view class="header-title">版本对比</view>
    </view>
    
    <view class="version-list"
>
      <view v-for="v in versions" :key="v.id" class="version-card"
>
        <view class="version-header"
>
          <text class="version-number">{{ v.version }}</text>
          <text class="version-date">{{ v.date }}</text>
        </view>
        
        <view class="version-content"
>
          <text class="content-text">{{ v.content }}</text>
        </view>
        
        <view class="version-actions"
>
          <view class="compare-btn" @click="compare(v)">对比</view>
          <view class="restore-btn" @click="restore(v)">恢复</view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'

const versions = ref([
  { id: 1, version: 'v1.3', date: '2024-02-20', content: '添加了提醒功能，优化了界面' },
  { id: 2, version: 'v1.2', date: '2024-02-15', content: '新增子任务功能，修复已知bug' },
  { id: 3, version: 'v1.1', date: '2024-02-10', content: '优化任务列表，添加排序功能' },
  { id: 4, version: 'v1.0', date: '2024-02-01', content: '初始版本发布' }
])

const compare = (v) => {
  uni.showToast({ title: `对比${v.version}`, icon: 'none' })
}

const restore = (v) => {
  uni.showModal({
    title: '恢复版本',
    content: `确定要恢复到${v.version}吗？`,
    success: (res) => {
      if (res.confirm) {
        uni.showToast({ title: '已恢复', icon: 'success' })
      }
    }
  })
}

const goBack = () => uni.navigateBack()
</script>

<style lang="scss" scoped>
.page-container { min-height: 100vh; background: #F8FAFC; }
.header {
  display: flex; justify-content: space-between; align-items: center;
  padding: 50px 20px 20px;
  background: linear-gradient(135deg, #EC4899, #DB2777);
  .header-left { width: 40px; height: 40px; display: flex; align-items: center; justify-content: center; }
  .header-left text { font-size: 32px; color: #fff; }
  .header-title { font-size: 20px; font-weight: 600; color: #fff; }
}
.version-list { padding: 20px; }
.version-card { background: #fff; border-radius: 16px; padding: 16px; margin-bottom: 12px; }
.version-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.version-number { font-size: 16px; font-weight: 700; color: #1F2937; }
.version-date { font-size: 13px; color: #9CA3AF; }
.version-content { padding: 12px 0; border-top: 1px solid #F3F4F6; border-bottom: 1px solid #F3F4F6; margin-bottom: 12px; }
.content-text { font-size: 14px; color: #6B7280; line-height: 1.5; }
.version-actions { display: flex; gap: 12px; }
.compare-btn, .restore-btn { flex: 1; height: 40px; border-radius: 20px; display: flex; align-items: center; justify-content: center; font-size: 14px; }
.compare-btn { background: #F3F4F6; color: #6B7280; }
.restore-btn { background: linear-gradient(135deg, #EC4899, #DB2777); color: #fff; }
</style>
