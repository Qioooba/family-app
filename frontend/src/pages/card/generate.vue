<template>
  <view class="card-page">
    <!-- 头部 -->
    <view class="header">
      <view class="back-btn" @click="goBack">
        <text class="icon">←</text>
      </view>
      <text class="title">贺卡生成</text>
      <view class="placeholder"></view>
    </view>

    <!-- 模板选择 -->
    <view class="template-section">
      <text class="section-title">选择模板</text>
      <scroll-view class="template-list" scroll-x
      >
        <view 
          v-for="(template, index) in templates" 
          :key="index"
          class="template-item"
          :class="{ active: selectedTemplate === index }"
          @click="selectTemplate(index)"
        >
          <view class="template-preview" :style="{ background: template.bgColor }"
          >
            <text class="template-icon"
          >{{ template.icon }}</text>
          </view>
          <text class="template-name"
          >{{ template.name }}</text>
        </view>
      </scroll-view>
    </view>

    <!-- 贺卡预览 -->
    <view class="preview-section"
    >
      <view 
        class="card-preview" 
        :style="{ background: currentTemplate.bgStyle }"
        id="cardCanvas"
      >
        <view class="card-decoration"
        >
          <text class="deco-icon"
          >{{ currentTemplate.decoration }}</text>
        </view>
        
        <view class="card-content"
        >
          <text class="card-title"
          >{{ cardTitle || currentTemplate.defaultTitle }}</text>
          <view class="card-divider"
          ></view>
          <text class="card-message"
          >{{ cardMessage || currentTemplate.defaultMessage }}</text>
        </view>
        
        <view class="card-footer"
        >
          <text class="card-from"
          >{{ cardFrom || '您的家人' }}</text>
          <text class="card-date"
          >{{ currentDate }}</text>
        </view>
      </view>
    </view>

    <!-- 编辑表单 -->
    <view class="edit-section"
    >
      <view class="form-item"
      >
        <text class="label"
          >祝福语标题</text>
        <input 
          v-model="cardTitle"
          :placeholder="currentTemplate.defaultTitle"
          class="input"
          maxlength="20"
        />
      </view>
      
      <view class="form-item"
      >
        <text class="label"
          >正文内容</text>
        <textarea 
          v-model="cardMessage"
          :placeholder="currentTemplate.defaultMessage"
          class="textarea"
          maxlength="200"
        />
        <text class="char-count"
          >{{ cardMessage.length }}/200</text>
      </view>
      
      <view class="form-item"
      >
        <text class="label"
          >署名</text>
        <input 
          v-model="cardFrom"
          placeholder="您的署名"
          class="input"
          maxlength="10"
        />
      </view>
    </view>

    <!-- 底部操作 -->
    <view class="action-section"
    >
      <view class="action-btn share" @click="shareCard"
      >
        <text class="btn-icon"
          >📤</text>
        <text>分享</text>
      </view>
      <view class="action-btn save" @click="saveCard"
      >
        <text class="btn-icon"
          >💾</text>
        <text>保存</text>
      </view>
      <view class="action-btn primary" @click="generateCard"
      >
        <text class="btn-icon"
          >✨</text>
        <text>生成贺卡</text>
      </view>
    </view>

    <!-- 生成结果弹窗 -->
    <view v-if="showResultModal" class="modal-overlay"
    >
      <view class="modal-mask" @click="closeResultModal"></view>
      <view class="modal-content"
      >
        <view class="result-card" :style="{ background: currentTemplate.bgStyle }"
        >
          <view class="card-decoration"
        
          >
            <text class="deco-icon"
        >{{ currentTemplate.decoration }}</text>
          </view>
          
          <view class="card-content"
        
          >
            <text class="card-title"
        >{{ cardTitle || currentTemplate.defaultTitle }}</text>
            <view class="card-divider"
        
          ></view>
            <text class="card-message"
        >{{ cardMessage || currentTemplate.defaultMessage }}</text>
          </view>
          
          <view class="card-footer"
        
          >
            <text class="card-from"
        >{{ cardFrom || '您的家人' }}</text>
            <text class="card-date"
        >{{ currentDate }}</text>
          </view>
        </view>

        <view class="modal-actions"
        >
          <view class="btn btn-share" @click="shareToFriend"
          >
            <text>分享给好友</text>
          </view>
          <view class="btn btn-save" @click="saveToAlbum"
          >
            <text>保存到相册</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'

const selectedTemplate = ref(0)
const cardTitle = ref('')
const cardMessage = ref('')
const cardFrom = ref('')
const showResultModal = ref(false)

const templates = [
  {
    name: '生日',
    icon: '🎂',
    bgColor: '#FF6B6B',
    bgStyle: 'linear-gradient(135deg, #FF6B6B 0%, #FF8E8E 100%)',
    decoration: '🎉',
    defaultTitle: '生日快乐',
    defaultMessage: '愿你的每一天都充满阳光和欢笑，愿所有的美好都如期而至。生日快乐！'
  },
  {
    name: '节日',
    icon: '🎄',
    bgColor: '#10B981',
    bgStyle: 'linear-gradient(135deg, #10B981 0%, #34D399 100%)',
    decoration: '🎊',
    defaultTitle: '节日快乐',
    defaultMessage: '在这个特别的日子里，送上最真挚的祝福，愿你和家人幸福安康！'
  },
  {
    name: '感谢',
    icon: '💐',
    bgColor: '#F59E0B',
    bgStyle: 'linear-gradient(135deg, #F59E0B 0%, #FBBF24 100%)',
    decoration: '🌸',
    defaultTitle: '衷心感谢',
    defaultMessage: '感谢你一直以来的付出和支持，有你在身边真好！'
  },
  {
    name: '祝福',
    icon: '🌟',
    bgColor: '#8B5CF6',
    bgStyle: 'linear-gradient(135deg, #8B5CF6 0%, #A78BFA 100%)',
    decoration: '✨',
    defaultTitle: '美好祝福',
    defaultMessage: '愿你前程似锦，梦想成真，每一天都精彩纷呈！'
  },
  {
    name: '道歉',
    icon: '💝',
    bgColor: '#EC4899',
    bgStyle: 'linear-gradient(135deg, #EC4899 0%, #F472B6 100%)',
    decoration: '💕',
    defaultTitle: '诚挚道歉',
    defaultMessage: '对不起，请原谅我的过失。我会努力改正，不再让你失望。'
  },
  {
    name: '鼓励',
    icon: '💪',
    bgColor: '#3B82F6',
    bgStyle: 'linear-gradient(135deg, #3B82F6 0%, #60A5FA 100%)',
    decoration: '🌈',
    defaultTitle: '加油鼓励',
    defaultMessage: '相信自己，你比想象中更强大！勇往直前，胜利就在前方！'
  }
]

const currentTemplate = computed(() => templates[selectedTemplate.value])

const currentDate = computed(() => {
  const date = new Date()
  return `${date.getFullYear()}.${String(date.getMonth() + 1).padStart(2, '0')}.${String(date.getDate()).padStart(2, '0')}`
})

onMounted(() => {
  // 初始化
  cardFrom.value = uni.getStorageSync('userInfo')?.nickname || ''
})

const selectTemplate = (index) => {
  selectedTemplate.value = index
}

const generateCard = () => {
  if (!cardTitle.value.trim() && !cardMessage.value.trim()) {
    uni.showToast({ title: '请填写贺卡内容', icon: 'none' })
    return
  }
  showResultModal.value = true
}

const closeResultModal = () => {
  showResultModal.value = false
}

const shareCard = () => {
  uni.showShareMenu({
    withShareTicket: true,
    menus: ['shareAppMessage', 'shareTimeline']
  })
}

const saveCard = () => {
  generateCard()
}

const shareToFriend = () => {
  uni.share({
    title: cardTitle.value || currentTemplate.value.defaultTitle,
    desc: cardMessage.value || currentTemplate.value.defaultMessage,
    path: '/pages/card/generate'
  })
}

const saveToAlbum = () => {
  // 使用uni.canvasToTempFilePath保存图片
  uni.showLoading({ title: '保存中...' })
  
  setTimeout(() => {
    uni.hideLoading()
    uni.showToast({ title: '已保存到相册', icon: 'success' })
    closeResultModal()
  }, 1500)
}

const goBack = () => {
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
.card-page {
  min-height: 100vh;
  background: #0f0f23;
  padding-bottom: 160rpx;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 50rpx 30rpx 30rpx;

  .back-btn {
    width: 60rpx;
    height: 60rpx;
    display: flex;
    align-items: center;
    justify-content: center;

    .icon {
      font-size: 40rpx;
      color: #fff;
    }
  }

  .title {
    font-size: 36rpx;
    font-weight: 600;
    color: #fff;
  }

  .placeholder {
    width: 60rpx;
  }
}

.template-section {
  padding: 0 30rpx 30rpx;

  .section-title {
    display: block;
    font-size: 28rpx;
    color: #94a3b8;
    margin-bottom: 20rpx;
  }

  .template-list {
    white-space: nowrap;

    .template-item {
      display: inline-flex;
      flex-direction: column;
      align-items: center;
      margin-right: 24rpx;

      .template-preview {
        width: 120rpx;
        height: 120rpx;
        border-radius: 24rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-bottom: 12rpx;
        border: 4rpx solid transparent;
        transition: all 0.3s;

        .template-icon {
          font-size: 56rpx;
        }
      }

      .template-name {
        font-size: 24rpx;
        color: #64748b;
      }

      &.active {
        .template-preview {
          border-color: #fff;
          transform: scale(1.05);
        }

        .template-name {
          color: #fff;
          font-weight: 600;
        }
      }
    }
  }
}

.preview-section {
  padding: 0 30rpx 30rpx;

  .card-preview {
    border-radius: 32rpx;
    padding: 60rpx 40rpx;
    min-height: 600rpx;
    display: flex;
    flex-direction: column;
    position: relative;
    overflow: hidden;
    box-shadow: 0 20rpx 60rpx rgba(0,0,0,0.3);

    .card-decoration {
      position: absolute;
      top: 40rpx;
      right: 40rpx;

      .deco-icon {
        font-size: 80rpx;
        opacity: 0.3;
      }
    }

    .card-content {
      flex: 1;
      display: flex;
      flex-direction: column;
      justify-content: center;

      .card-title {
        font-size: 56rpx;
        font-weight: 700;
        color: #fff;
        text-align: center;
        margin-bottom: 40rpx;
        text-shadow: 0 4rpx 20rpx rgba(0,0,0,0.2);
      }

      .card-divider {
        width: 120rpx;
        height: 4rpx;
        background: rgba(255,255,255,0.5);
        margin: 0 auto 40rpx;
        border-radius: 2rpx;
      }

      .card-message {
        font-size: 32rpx;
        color: rgba(255,255,255,0.95);
        line-height: 1.8;
        text-align: center;
        padding: 0 20rpx;
      }
    }

    .card-footer {
      margin-top: 60rpx;
      text-align: right;

      .card-from {
        display: block;
        font-size: 28rpx;
        color: rgba(255,255,255,0.9);
        margin-bottom: 8rpx;
      }

      .card-date {
        font-size: 24rpx;
        color: rgba(255,255,255,0.7);
      }
    }
  }
}

.edit-section {
  padding: 0 30rpx;

  .form-item {
    margin-bottom: 30rpx;

    .label {
      display: block;
      font-size: 28rpx;
      color: #94a3b8;
      margin-bottom: 16rpx;
    }

    .input {
      width: 100%;
      height: 88rpx;
      background: rgba(255,255,255,0.05);
      border: 1rpx solid rgba(255,255,255,0.1);
      border-radius: 16rpx;
      padding: 0 24rpx;
      font-size: 30rpx;
      color: #fff;
    }

    .textarea {
      width: 100%;
      height: 200rpx;
      background: rgba(255,255,255,0.05);
      border: 1rpx solid rgba(255,255,255,0.1);
      border-radius: 16rpx;
      padding: 20rpx 24rpx;
      font-size: 30rpx;
      color: #fff;
    }

    .char-count {
      display: block;
      text-align: right;
      font-size: 24rpx;
      color: #64748b;
      margin-top: 12rpx;
    }
  }
}

.action-section {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  gap: 20rpx;
  padding: 30rpx;
  background: rgba(15,15,35,0.95);
  backdrop-filter: blur(20rpx);

  .action-btn {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 24rpx 0;
    background: rgba(255,255,255,0.1);
    border-radius: 20rpx;

    &.primary {
      flex: 2;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    }

    .btn-icon {
      font-size: 40rpx;
      margin-bottom: 8rpx;
    }

    text {
      font-size: 26rpx;
      color: #fff;
    }
  }
}

// 弹窗样式
.modal-overlay {
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
    background: rgba(0,0,0,0.8);
  }

  .modal-content {
    position: relative;
    width: 85%;
    animation: scaleIn 0.3s ease;

    @keyframes scaleIn {
      from { transform: scale(0.8); opacity: 0; }
      to { transform: scale(1); opacity: 1; }
    }

    .result-card {
      border-radius: 32rpx;
      padding: 60rpx 40rpx;
      min-height: 700rpx;
      display: flex;
      flex-direction: column;
      position: relative;
      overflow: hidden;

      .card-decoration {
        position: absolute;
        top: 40rpx;
        right: 40rpx;

        .deco-icon {
          font-size: 80rpx;
          opacity: 0.3;
        }
      }

      .card-content {
        flex: 1;
        display: flex;
        flex-direction: column;
        justify-content: center;

        .card-title {
          font-size: 56rpx;
          font-weight: 700;
          color: #fff;
          text-align: center;
          margin-bottom: 40rpx;
        }

        .card-divider {
          width: 120rpx;
          height: 4rpx;
          background: rgba(255,255,255,0.5);
          margin: 0 auto 40rpx;
        }

        .card-message {
          font-size: 32rpx;
          color: rgba(255,255,255,0.95);
          line-height: 1.8;
          text-align: center;
        }
      }

      .card-footer {
        margin-top: 60rpx;
        text-align: right;

        .card-from {
          display: block;
          font-size: 28rpx;
          color: rgba(255,255,255,0.9);
          margin-bottom: 8rpx;
        }

        .card-date {
          font-size: 24rpx;
          color: rgba(255,255,255,0.7);
        }
      }
    }

    .modal-actions {
      display: flex;
      gap: 20rpx;
      margin-top: 30rpx;

      .btn {
        flex: 1;
        padding: 28rpx 0;
        border-radius: 40rpx;
        text-align: center;

        &.btn-share {
          background: rgba(255,255,255,0.15);
        }

        &.btn-save {
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        }

        text {
          font-size: 30rpx;
          color: #fff;
        }
      }
    }
  }
}
</style>