<template>
  <!-- 设置饮水目标弹窗 -->
  <view v-if="visible" class="modal-overlay" @click="handleClose">
    <view class="modal-content" @click.stop>
      <view class="modal-header">
        <text class="modal-title">设置饮水目标</text>
        <text class="close-btn" @click="handleClose">✕</text>
      </view>
      
      <view class="modal-body">
        <view class="goal-display">
          <text class="goal-value">{{ localTarget }}</text>
          <text class="goal-unit">ml</text>
        </view>
        
        <view class="slider-section">
          <view class="slider-labels">
            <text class="slider-min">500ml</text>
            <text class="slider-max">5000ml</text>
          </view>
          <slider 
            class="water-slider"
            :value="localTarget"
            min="500"
            max="5000"
            step="100"
            activeColor="#4ECDC4"
            backgroundColor="#e8f5f3"
            block-color="#4ECDC4"
            block-size="24"
            @change="onSliderChange"
          />
        </view>
        
        <view class="quick-options">
          <view 
            v-for="option in quickOptions" 
            :key="option"
            class="quick-option"
            :class="{ active: localTarget === option }"
            @click="selectQuickOption(option)"
          >
            <text>{{ option }}ml</text>
          </view>
        </view>
        
        <view class="tip">
          <text class="tip-icon">💡</text>
          <text class="tip-text">建议成年人每日饮水量为2000-2500ml</text>
        </view>
      </view>
      
      <view class="modal-actions">
        <button class="btn-cancel" @click="handleClose">取消</button>
        <button class="btn-confirm" @click="handleConfirm" :disabled="loading">
          {{ loading ? '保存中...' : '保存' }}
        </button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  visible: Boolean,
  currentTarget: {
    type: Number,
    default: 2000
  }
})

const emit = defineEmits(['close', 'confirm'])

const loading = ref(false)
const localTarget = ref(2000)

const quickOptions = [1500, 2000, 2500, 3000]

watch(() => props.visible, (val) => {
  if (val) {
    localTarget.value = props.currentTarget || 2000
  }
})

const onSliderChange = (e) => {
  localTarget.value = e.detail.value
}

const selectQuickOption = (value) => {
  localTarget.value = value
}

const handleClose = () => {
  emit('close')
}

const handleConfirm = async () => {
  if (loading.value) return
  
  loading.value = true
  try {
    emit('confirm', localTarget.value)
  } catch (error) {
    console.error('设置饮水目标失败:', error)
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  animation: fadeIn 0.2s ease;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.modal-content {
  width: 620rpx;
  background: #fff;
  border-radius: 32rpx;
  padding: 40rpx;
  animation: slideUp 0.3s ease;
}

@keyframes slideUp {
  from { 
    opacity: 0;
    transform: translateY(40rpx);
  }
  to { 
    opacity: 1;
    transform: translateY(0);
  }
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 40rpx;
  
  .modal-title {
    font-size: 36rpx;
    font-weight: 600;
    color: #2d3748;
  }
  
  .close-btn {
    font-size: 36rpx;
    color: #8b9aad;
    padding: 10rpx;
  }
}

.modal-body {
  .goal-display {
    text-align: center;
    padding: 40rpx 0;
    
    .goal-value {
      font-size: 96rpx;
      font-weight: 700;
      color: #4ECDC4;
    }
    
    .goal-unit {
      font-size: 36rpx;
      color: #8b9aad;
      margin-left: 12rpx;
    }
  }
  
  .slider-section {
    margin-bottom: 32rpx;
    
    .slider-labels {
      display: flex;
      justify-content: space-between;
      margin-bottom: 16rpx;
      
      .slider-min,
      .slider-max {
        font-size: 24rpx;
        color: #8b9aad;
      }
    }
    
    .water-slider {
      width: 100%;
    }
  }
  
  .quick-options {
    display: flex;
    justify-content: space-between;
    margin-bottom: 32rpx;
    
    .quick-option {
      flex: 1;
      text-align: center;
      padding: 20rpx 0;
      margin: 0 8rpx;
      background: #f1f5f9;
      border-radius: 16rpx;
      transition: all 0.2s ease;
      
      &.active {
        background: linear-gradient(135deg, #4ECDC4, #44A08D);
        box-shadow: 0 8rpx 20rpx rgba(78, 205, 196, 0.35);
        
        text {
          color: #fff;
        }
      }
      
      text {
        font-size: 26rpx;
        color: #5a6c7d;
        font-weight: 500;
      }
    }
  }
  
  .tip {
    display: flex;
    align-items: center;
    padding: 20rpx;
    background: rgba(78, 205, 196, 0.1);
    border-radius: 16rpx;
    
    .tip-icon {
      font-size: 28rpx;
      margin-right: 12rpx;
    }
    
    .tip-text {
      font-size: 24rpx;
      color: #5a6c7d;
    }
  }
}

.modal-actions {
  display: flex;
  justify-content: space-between;
  margin-top: 40rpx;
  
  .btn-cancel,
  .btn-confirm {
    flex: 1;
    padding: 24rpx 0;
    border-radius: 24rpx;
    font-size: 32rpx;
    font-weight: 500;
    text-align: center;
    border: none;
  }
  
  .btn-cancel {
    background: #f1f5f9;
    color: #5a6c7d;
    margin-right: 20rpx;
  }
  
  .btn-confirm {
    background: linear-gradient(135deg, #4ECDC4, #44A08D);
    color: #fff;
    box-shadow: 0 8rpx 20rpx rgba(78, 205, 196, 0.35);
    
    &:disabled {
      opacity: 0.6;
    }
  }
}
</style>
