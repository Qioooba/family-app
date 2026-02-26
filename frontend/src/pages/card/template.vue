<template>
  <view class="card-page"
>
    <!-- 顶部导航 -->
    <view class="nav-bar"
>
      <view class="back-btn" @click="goBack"
>
        <u-icon name="arrow-left" size="40" color="#333"></u-icon>
      </view>
      <text class="title">贺卡生成</text>
      <view class="right-btn" @click="showHistory">
        <u-icon name="file-text" size="36" color="#333"></u-icon>
      </view>
    </view>

    <view class="content">
      <!-- 贺卡预览 -->
      <view class="preview-section"
>
        <view 
          class="card-preview"
          :style="{ background: currentTemplate?.gradient || 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)' }"
        >
          <view class="card-decoration top">{{ currentTemplate?.decoration?.[0] || '✨' }}</view>
          
          <view class="card-content"
>
            <text class="card-to">{{ form.to || '致亲爱的你' }}</text>
            
            <view class="card-message-wrap">
              <text class="card-message">{{ form.message || currentTemplate?.defaultMessage || '祝你节日快乐！' }}</text>
            </view>
            
            <text class="card-from">{{ form.from || '你的朋友' }}</text>
          </view>          
          
          <view class="card-decoration bottom">{{ currentTemplate?.decoration?.[1] || '🎊' }}</view>
        </view>
      </view>

      <!-- 模板选择 -->
      <view class="templates-section"
>
        <text class="section-title">选择模板</text>
        
        <scroll-view class="template-list" scroll-x>
          <view
            v-for="template in templates"
            :key="template.id"
            class="template-item"
            :class="{ active: selectedTemplate === template.id }"
            @click="selectTemplate(template)"
          >
            <view 
              class="template-preview"
              :style="{ background: template.gradient }"
            >
              <text class="template-icon">{{ template.icon }}</text>
            </view>
            
            <text class="template-name">{{ template.name }}</text>
          </view>
        </scroll-view>
      </view>

      <!-- 贺卡表单 -->
      <view class="form-section">
        <view class="form-item"
>
          <text class="form-label">收卡人</text>
          <input
            v-model="form.to"
            placeholder="请输入收卡人称呼"
            class="form-input"
          />
        </view>

        <view class="form-item">
          <text class="form-label">祝福语</text>
          <view class="quick-messages"
>
            <view
              v-for="msg in quickMessages"
              :key="msg"
              class="quick-tag"
              @click="form.message = msg"
            >
              {{ msg }}
            </view>
          </view>
          <textarea
            v-model="form.message"
            placeholder="写下你的祝福..."
            class="form-textarea"
            maxlength="200"
          />
          <text class="char-count">{{ form.message.length }}/200</text>
        </view>

        <view class="form-item">
          <text class="form-label">署名</text>
          <input
            v-model="form.from"
            placeholder="你的署名"
            class="form-input"
          />
        </view>
      </view>

      <!-- 操作按钮 -->
      <view class="actions-section">
        <view class="action-btn primary" @click="generateCard">
          <u-icon name="photo" size="32" color="#fff"></u-icon>
          <text>生成贺卡</text>
        </view>
        
        <view class="action-row"
>
          <view class="action-btn secondary" @click="saveToAlbum">
            <u-icon name="download" size="28" color="#5B8FF9"></u-icon>
            <text>保存</text>
          </view>
          
          <view class="action-btn secondary" @click="shareCard"
>
            <u-icon name="share" size="28" color="#52C41A"></u-icon>
            <text>分享</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 生成的贺卡预览弹窗 -->
    <up-popup
      v-model:show="previewModalVisible"
      mode="center"
    >
      <view class="preview-modal"
>
        <view 
          class="generated-card"
          :style="{ background: currentTemplate?.gradient }"
        >
          <view class="card-decoration top">{{ currentTemplate?.decoration?.[0] }}</view>
          
          <view class="card-content">
            <text class="card-to">{{ form.to }}</text>
            
            <view class="card-message-wrap"
>
              <text class="card-message">{{ form.message }}</text>
            </view>
            
            <text class="card-from">{{ form.from }}</text>
          </view>          
          
          <view class="card-decoration bottom">{{ currentTemplate?.decoration?.[1] }}</view>
        </view>

        <view class="preview-actions"
>
          <view class="btn-close" @click="previewModalVisible = false">关闭</view>
          <view class="btn-share" @click="shareCard">分享贺卡</view>
        </view>
      </view>
    </up-popup>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'

// 响应式数据
const selectedTemplate = ref(1)
const previewModalVisible = ref(false)

const form = ref({
  to: '',
  message: '',
  from: ''
})

// 贺卡模板
const templates = [
  {
    id: 1,
    name: '梦幻紫',
    icon: '💜',
    gradient: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
    decoration: ['✨', '🌟'],
    defaultMessage: '愿你每一天都充满阳光和欢笑！'
  },
  {
    id: 2,
    name: '暖心橙',
    icon: '🧡',
    gradient: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
    decoration: ['🌸', '💮'],
    defaultMessage: '感谢有你，生活更加精彩！'
  },
  {
    id: 3,
    name: '清新绿',
    icon: '💚',
    gradient: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
    decoration: ['🍃', '🌿'],
    defaultMessage: '愿你如春风般温暖，如绿叶般生机勃勃！'
  },
  {
    id: 4,
    name: '喜庆红',
    icon: '❤️',
    gradient: 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)',
    decoration: ['🎊', '🎉'],
    defaultMessage: '祝你节日快乐，万事如意！'
  },
  {
    id: 5,
    name: '星空蓝',
    icon: '💙',
    gradient: 'linear-gradient(135deg, #30cfd0 0%, #330867 100%)',
    decoration: ['⭐', '🌙'],
    defaultMessage: '愿你的梦想如繁星般闪耀！'
  },
  {
    id: 6,
    name: '阳光黄',
    icon: '💛',
    gradient: 'linear-gradient(135deg, #f6d365 0%, #fda085 100%)',
    decoration: ['☀️', '🌻'],
    defaultMessage: '愿你每一天都被阳光温暖！'
  }
]

// 快捷祝福语
const quickMessages = [
  '祝你节日快乐！',
  '感谢有你相伴！',
  '愿你天天开心！',
  '身体健康，万事如意！',
  '心想事成，梦想成真！',
  '愿我们的友谊长存！'
]

// 计算属性
const currentTemplate = computed(() => {
  return templates.find(t => t.id === selectedTemplate.value)
})

// 方法
const selectTemplate = (template) => {
  selectedTemplate.value = template.id
  if (!form.value.message) {
    form.value.message = template.defaultMessage
  }
}

const generateCard = () => {
  if (!form.value.to.trim()) {
    uni.showToast({ title: '请填写收卡人', icon: 'none' })
    return
  }
  if (!form.value.message.trim()) {
    uni.showToast({ title: '请填写祝福语', icon: 'none' })
    return
  }
  
  previewModalVisible.value = true
  
  // 保存生成记录
  saveCardHistory()
}

const saveCardHistory = () => {
  const history = uni.getStorageSync('cardHistory') || []
  history.unshift({
    template: currentTemplate.value,
    form: { ...form.value },
    createTime: Date.now()
  })
  if (history.length > 20) {
    history.pop()
  }
  uni.setStorageSync('cardHistory', history)
}

const saveToAlbum = () => {
  uni.showLoading({ title: '保存中...' })
  
  // 模拟保存过程
  setTimeout(() => {
    uni.hideLoading()
    uni.showToast({ title: '已保存到相册', icon: 'success' })
  }, 1500)
}

const shareCard = () => {
  uni.showShareMenu({
    withShareTicket: true,
    menus: ['shareAppMessage', 'shareTimeline']
  })
}

const showHistory = () => {
  const history = uni.getStorageSync('cardHistory') || []
  if (history.length === 0) {
    uni.showToast({ title: '暂无历史记录', icon: 'none' })
    return
  }
  
  // 显示历史记录
  uni.navigateTo({
    url: '/pages/card/history'
  })
}

const goBack = () => {
  uni.navigateBack()
}

// 分享配置
uni.showShareMenu({
  withShareTicket: true,
  menus: ['shareAppMessage', 'shareTimeline']
})
</script>

<style lang="scss" scoped>
.card-page {
  min-height: 100vh;
  background: #f5f6fa;
}

.nav-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 30rpx;
  padding-top: 60rpx;
  background: #fff;
  border-bottom: 1rpx solid #f0f0f0;

  .back-btn {
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

  .right-btn {
    width: 60rpx;
    height: 60rpx;
    display: flex;
    align-items: center;
    justify-content: center;
  }
}

.content {
  padding: 30rpx;
  padding-bottom: 50rpx;
}

// 预览区域
.preview-section {
  margin-bottom: 40rpx;
}

.card-preview {
  border-radius: 24rpx;
  padding: 60rpx 40rpx;
  min-height: 400rpx;
  display: flex;
  flex-direction: column;
  position: relative;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.15);

  .card-decoration {
    font-size: 48rpx;
    opacity: 0.6;

    &.top {
      text-align: left;
    }

    &.bottom {
      text-align: right;
      margin-top: auto;
    }
  }

  .card-content {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: center;
    padding: 40rpx 0;

    .card-to {
      font-size: 32rpx;
      color: rgba(255, 255, 255, 0.9);
      margin-bottom: 30rpx;
    }

    .card-message-wrap {
      background: rgba(255, 255, 255, 0.2);
      border-radius: 16rpx;
      padding: 40rpx;
      backdrop-filter: blur(10rpx);
      margin-bottom: 30rpx;

      .card-message {
        font-size: 40rpx;
        font-weight: 600;
        color: #fff;
        line-height: 1.6;
        display: block;
      }
    }

    .card-from {
      font-size: 28rpx;
      color: rgba(255, 255, 255, 0.9);
      text-align: right;
    }
  }
}

// 模板选择
.templates-section {
  margin-bottom: 40rpx;

  .section-title {
    font-size: 30rpx;
    font-weight: 600;
    color: #333;
    margin-bottom: 20rpx;
    display: block;
  }
}

.template-list {
  white-space: nowrap;

  .template-item {
    display: inline-flex;
    flex-direction: column;
    align-items: center;
    margin-right: 24rpx;

    .template-preview {
      width: 140rpx;
      height: 180rpx;
      border-radius: 16rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-bottom: 12rpx;
      border: 4rpx solid transparent;
      transition: all 0.3s;

      .template-icon {
        font-size: 48rpx;
      }
    }

    .template-name {
      font-size: 24rpx;
      color: #666;
    }

    &.active {
      .template-preview {
        border-color: #5B8FF9;
        transform: scale(1.05);
      }

      .template-name {
        color: #5B8FF9;
        font-weight: 600;
      }
    }
  }
}

// 表单区域
.form-section {
  background: #fff;
  border-radius: 24rpx;
  padding: 30rpx;
  margin-bottom: 40rpx;
}

.form-item {
  margin-bottom: 30rpx;

  &:last-child {
    margin-bottom: 0;
  }

  .form-label {
    font-size: 28rpx;
    font-weight: 500;
    color: #333;
    margin-bottom: 16rpx;
    display: block;
  }

  .form-input, .form-textarea {
    background: #f5f6fa;
    border-radius: 12rpx;
    padding: 24rpx;
    font-size: 28rpx;
    color: #333;
  }

  .form-textarea {
    height: 160rpx;
    width: 100%;
  }

  .quick-messages {
    display: flex;
    flex-wrap: wrap;
    gap: 12rpx;
    margin-bottom: 16rpx;

    .quick-tag {
      padding: 12rpx 20rpx;
      background: #f0f5ff;
      border-radius: 24rpx;
      font-size: 24rpx;
      color: #5B8FF9;
      border: 2rpx solid transparent;

      &:active {
        border-color: #5B8FF9;
      }
    }
  }

  .char-count {
    font-size: 22rpx;
    color: #999;
    text-align: right;
    display: block;
    margin-top: 12rpx;
  }
}

// 操作按钮
.actions-section {
  .action-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 12rpx;
    padding: 28rpx 0;
    border-radius: 16rpx;

    text {
      font-size: 30rpx;
      font-weight: 500;
    }

    &.primary {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: #fff;
      margin-bottom: 20rpx;
      box-shadow: 0 8rpx 24rpx rgba(102, 126, 234, 0.3);
    }
  }

  .action-row {
    display: flex;
    gap: 20rpx;

    .action-btn {
      flex: 1;
      background: #fff;
      border: 2rpx solid #e8e8e8;

      text {
        color: #333;
      }
    }
  }
}

// 预览弹窗
.preview-modal {
  width: 600rpx;
  padding: 30rpx;

  .generated-card {
    border-radius: 24rpx;
    padding: 50rpx 40rpx;
    min-height: 500rpx;
    display: flex;
    flex-direction: column;
    margin-bottom: 30rpx;

    .card-decoration {
      font-size: 48rpx;
      opacity: 0.6;

      &.top {
        text-align: left;
      }

      &.bottom {
        text-align: right;
        margin-top: auto;
      }
    }

    .card-content {
      flex: 1;
      display: flex;
      flex-direction: column;
      justify-content: center;
      padding: 40rpx 0;

      .card-to {
        font-size: 32rpx;
        color: rgba(255, 255, 255, 0.9);
        margin-bottom: 30rpx;
      }

      .card-message-wrap {
        background: rgba(255, 255, 255, 0.2);
        border-radius: 16rpx;
        padding: 40rpx;
        backdrop-filter: blur(10rpx);
        margin-bottom: 30rpx;

        .card-message {
          font-size: 40rpx;
          font-weight: 600;
          color: #fff;
          line-height: 1.6;
          display: block;
        }
      }

      .card-from {
        font-size: 28rpx;
        color: rgba(255, 255, 255, 0.9);
        text-align: right;
      }
    }
  }

  .preview-actions {
    display: flex;
    gap: 20rpx;

    .btn-close, .btn-share {
      flex: 1;
      padding: 24rpx 0;
      border-radius: 12rpx;
      text-align: center;
      font-size: 30rpx;
    }

    .btn-close {
      background: #f5f5f5;
      color: #666;
    }

    .btn-share {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: #fff;
    }
  }
}
</style>