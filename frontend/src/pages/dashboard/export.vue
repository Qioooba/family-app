<template>
  <view class="export-page"
  >
    <!-- 头部 -->
    <view class="header"
    >
      <view class="back-btn" @click="goBack"
    
      >
        <text class="icon"
    
    >←</text>
      </view>
      <text class="title"
    
    >数据导出</text>
      <view class="placeholder"
    
    ></text>
    </view>

    <!-- 导出类型选择 -->
    <view class="section"
    >
      <text class="section-title"
    
    >选择导出类型</text>
      
      <view class="type-list"
    
    >
        <view 
          v-for="type in exportTypes" 
          :key="type.value"
          class="type-card"
          :class="{ active: selectedType === type.value }"
          @click="selectType(type.value)"
        >
          <view class="type-icon" :style="{ background: type.color }"
    
        >
            <text>{{ type.icon }}</text>
          </view>
          <view class="type-info"
    
        >
            <text class="type-name"
    
      >{{ type.label }}</text>
            <text class="type-desc"
    
      >{{ type.description }}</text>
          </view>
          <view v-if="selectedType === type.value" class="type-check"
    
      >✓</view>
        </view>
      </view>
    </view>

    <!-- 时间范围 -->
    <view class="section"
    >
      <text class="section-title"
    
    >时间范围</text>
      
      <view class="date-range"
    
    >
        <view class="date-item" @click="selectStartDate"
    
      >
          <text class="date-label"
    
    >开始日期</text>
          <text class="date-value"
    
    >{{ startDate || '请选择' }}</text>
        </view>
        
        <text class="date-separator"
    
    >至</text>
        
        <view class="date-item" @click="selectEndDate"
    
      >
          <text class="date-label"
    
    >结束日期</text>
          <text class="date-value"
    
    >{{ endDate || '请选择' }}</text>
        </view>
      </view>
      
      <view class="quick-dates"
    
    >
        <view 
          v-for="quick in quickDates" 
          :key="quick.value"
          class="quick-tag"
          :class="{ active: selectedQuick === quick.value }"
          @click="selectQuickDate(quick)"
        >
          <text>{{ quick.label }}</text>
        </view>
      </view>
    </view>

    <!-- 导出格式 -->
    <view class="section"
    >
      <text class="section-title"
    
    >导出格式</text>
      
      <view class="format-list"
    
    >
        <view 
          v-for="format in formats" 
          :key="format.value"
          class="format-card"
          :class="{ active: selectedFormat === format.value }"
          @click="selectFormat(format.value)"
        >
          <view class="format-icon"
    
      >{{ format.icon }}</view>
          <view class="format-info"
    
      >
            <text class="format-name"
    
    >{{ format.label }}</text>
            <text class="format-ext"
    
    >{{ format.ext }}</text>
          </view>
          <view class="format-radio"
    
      >
            <view v-if="selectedFormat === format.value" class="radio-inner"
    
    ></text>
          </view>
        </view>
      </view>
    </view>

    <!-- 导出选项 -->
    <view class="section"
    >
      <text class="section-title"
    
    >导出选项</text>
      
      <view class="option-list"
    
    >
        <view 
          v-for="option in options" 
          :key="option.value"
          class="option-item"
        >
          <text class="option-label"
    
    >{{ option.label }}</text>
          <switch 
            :checked="option.checked"
            @change="toggleOption(option)"
            color="#667eea"
          />
        </view>
      </view>
    </view>

    <!-- 导出按钮 -->
    <view class="footer"
    
  >
      <view class="export-btn" :class="{ disabled: !canExport }" @click="startExport"
    
    >
        <text class="btn-icon"
    
  >📥</text>
        <view class="btn-content"
    
  >
          <text class="btn-title"
    
    >开始导出</text>
          <text v-if="canExport" class="btn-desc"
    
    >预计 {{ estimateSize }} MB</text>
        </view>
      </view>
    </view>

    <!-- 导出进度弹窗 -->
    <view v-if="showProgress" class="progress-overlay"
    >
      <view class="progress-modal"
    
    >
        <view class="progress-icon"
    
      >📊</view>
        
        <text class="progress-title"
    
    >正在导出数据...</text>
        
        <view class="progress-bar"
    
    >
          <view class="progress-fill" :style="{ width: progress + '%' }"
    
    ></text>
        </view>
        
        <text class="progress-text"
    
    >{{ progress }}%</text>
        
        <text class="progress-hint"
    
    >{{ progressHint }}</text>
      </view>
    </view>

    <!-- 导出完成弹窗 -->
    <view v-if="showComplete" class="complete-overlay"
    >
      <view class="complete-modal"
    
    >
        <view class="success-icon"
    
      >✓</view>
        <text class="complete-title"
    
    >导出成功!</text>
        <text class="complete-desc"
    
    >文件已保存到手机</text>
        
        <view class="file-info"
    
    >
          <text class="file-name"
    
    >{{ exportedFile.name }}</text>
          <text class="file-size"
    
    >{{ exportedFile.size }}</text>
        </view>
        
        <view class="complete-actions"
    
    >
          <view class="btn btn-share" @click="shareFile"
    
      >
            <text>分享</text>
          </view>
          <view class="btn btn-done" @click="closeComplete"
    
      >
            <text>完成</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'

const selectedType = ref('task')
const selectedFormat = ref('excel')
const startDate = ref('')
const endDate = ref('')
const selectedQuick = ref('')
const showProgress = ref(false)
const progress = ref(0)
const showComplete = ref(false)
const exportedFile = ref({ name: '', size: '' })

const exportTypes = [
  { 
    value: 'task', 
    label: '任务数据', 
    icon: '📋', 
    color: '#667eea',
    description: '包含所有任务记录、完成状态'
  },
  { 
    value: 'shopping', 
    label: '购物记录', 
    icon: '🛒', 
    color: '#f59e0b',
    description: '购物清单、价格追踪记录'
  },
  { 
    value: 'diet', 
    label: '饮食记录', 
    icon: '🍽️', 
    color: '#10b981',
    description: '每日饮食、营养成分摄入'
  },
  { 
    value: 'finance', 
    label: '消费账单', 
    icon: '💰', 
    color: '#ef4444',
    description: '家庭支出、消费分类统计'
  },
  { 
    value: 'all', 
    label: '全部数据', 
    icon: '📦', 
    color: '#8b5cf6',
    description: '导出所有家庭数据'
  }
]

const formats = [
  { value: 'excel', label: 'Excel表格', icon: '📊', ext: '.xlsx' },
  { value: 'pdf', label: 'PDF文档', icon: '📄', ext: '.pdf' },
  { value: 'csv', label: 'CSV文件', icon: '📑', ext: '.csv' }
]

const quickDates = [
  { label: '最近7天', value: '7days', days: 7 },
  { label: '最近30天', value: '30days', days: 30 },
  { label: '本月', value: 'thisMonth' },
  { label: '上月', value: 'lastMonth' },
  { label: '本年', value: 'thisYear' }
]

const options = ref([
  { value: 'includeImages', label: '包含图片', checked: false },
  { value: 'includeDetails', label: '包含详细信息', checked: true },
  { value: 'compress', label: '压缩文件', checked: true }
])

const progressHint = computed(() => {
  if (progress.value < 30) return '正在收集数据...'
  if (progress.value < 60) return '正在整理格式...'
  if (progress.value < 90) return '正在生成文件...'
  return '即将完成...'
})

const canExport = computed(() => {
  return startDate.value && endDate.value
})

const estimateSize = computed(() => {
  const baseSize = {
    task: 0.5,
    shopping: 0.8,
    diet: 1.2,
    finance: 0.6,
    all: 3.0
  }
  let size = baseSize[selectedType.value] || 1
  if (options.value.find(o => o.value === 'includeImages')?.checked) size *= 2
  if (options.value.find(o => o.value === 'compress')?.checked) size *= 0.6
  return size.toFixed(1)
})

const selectType = (value) => {
  selectedType.value = value
}

const selectFormat = (value) => {
  selectedFormat.value = value
}

const selectStartDate = () => {
  uni.showActionSheet({
    itemList: ['今天', '7天前', '30天前', '自定义'],
    success: (res) => {
      const date = new Date()
      if (res.tapIndex === 0) {
        startDate.value = formatDate(date)
      } else if (res.tapIndex === 1) {
        date.setDate(date.getDate() - 7)
        startDate.value = formatDate(date)
      } else if (res.tapIndex === 2) {
        date.setDate(date.getDate() - 30)
        startDate.value = formatDate(date)
      } else {
        uni.showToast({ title: '请使用日期选择', icon: 'none' })
      }
    }
  })
}

const selectEndDate = () => {
  endDate.value = formatDate(new Date())
}

const selectQuickDate = (quick) => {
  selectedQuick.value = quick.value
  const end = new Date()
  const start = new Date()
  
  if (quick.days) {
    start.setDate(end.getDate() - quick.days)
  } else if (quick.value === 'thisMonth') {
    start.setDate(1)
  } else if (quick.value === 'lastMonth') {
    start.setMonth(start.getMonth() - 1)
    start.setDate(1)
    end.setDate(0)
  } else if (quick.value === 'thisYear') {
    start.setMonth(0)
    start.setDate(1)
  }
  
  startDate.value = formatDate(start)
  endDate.value = formatDate(end)
}

const formatDate = (date) => {
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

const toggleOption = (option) => {
  option.checked = !option.checked
}

const startExport = () => {
  if (!canExport.value) {
    uni.showToast({ title: '请选择时间范围', icon: 'none' })
    return
  }

  showProgress.value = true
  progress.value = 0

  // 模拟导出进度
  const interval = setInterval(() => {
    progress.value += Math.random() * 15
    if (progress.value >= 100) {
      progress.value = 100
      clearInterval(interval)
      setTimeout(() => {
        showProgress.value = false
        showComplete.value = true
        exportedFile.value = {
          name: `家庭${exportTypes.find(t => t.value === selectedType.value)?.label}_${startDate.value}_${endDate.value}${formats.find(f => f.value === selectedFormat.value)?.ext}`,
          size: `${estimateSize.value} MB`
        }
      }, 500)
    }
  }, 300)
}

const closeComplete = () => {
  showComplete.value = false
}

const shareFile = () => {
  uni.share({
    title: exportedFile.value.name,
    type: 'file'
  })
}

const goBack = () => {
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
.export-page {
  min-height: 100vh;
  background: #0f0f23;
  padding-bottom: 200rpx;
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

.section {
  padding: 0 30rpx 30rpx;

  .section-title {
    display: block;
    font-size: 28rpx;
    color: #94a3b8;
    margin-bottom: 20rpx;
  }
}

// 导出类型
.type-list {
  .type-card {
    display: flex;
    align-items: center;
    gap: 24rpx;
    padding: 30rpx;
    background: rgba(255,255,255,0.05);
    border-radius: 20rpx;
    margin-bottom: 20rpx;
    border: 2rpx solid transparent;

    &.active {
      border-color: #667eea;
      background: rgba(102,126,234,0.1);
    }

    .type-icon {
      width: 88rpx;
      height: 88rpx;
      border-radius: 20rpx;
      display: flex;
      align-items: center;
      justify-content: center;

      text {
        font-size: 48rpx;
      }
    }

    .type-info {
      flex: 1;

      .type-name {
        display: block;
        font-size: 30rpx;
        font-weight: 600;
        color: #fff;
        margin-bottom: 8rpx;
      }

      .type-desc {
        font-size: 24rpx;
        color: #64748b;
      }
    }

    .type-check {
      width: 48rpx;
      height: 48rpx;
      background: #667eea;
      border-radius: 50%;
      text-align: center;
      line-height: 48rpx;
      color: #fff;
      font-size: 28rpx;
    }
  }
}

// 时间范围
.date-range {
  display: flex;
  align-items: center;
  gap: 20rpx;
  margin-bottom: 24rpx;

  .date-item {
    flex: 1;
    padding: 24rpx;
    background: rgba(255,255,255,0.05);
    border-radius: 16rpx;

    .date-label {
      display: block;
      font-size: 24rpx;
      color: #64748b;
      margin-bottom: 8rpx;
    }

    .date-value {
      font-size: 32rpx;
      color: #fff;
      font-weight: 500;
    }
  }

  .date-separator {
    font-size: 28rpx;
    color: #64748b;
  }
}

.quick-dates {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;

  .quick-tag {
    padding: 16rpx 32rpx;
    background: rgba(255,255,255,0.05);
    border-radius: 30rpx;
    border: 1rpx solid transparent;

    &.active {
      background: rgba(102,126,234,0.2);
      border-color: #667eea;
    }

    text {
      font-size: 26rpx;
      color: #94a3b8;
    }

    &.active text {
      color: #fff;
    }
  }
}

// 导出格式
.format-list {
  display: flex;
  gap: 20rpx;

  .format-card {
    flex: 1;
    padding: 30rpx 20rpx;
    background: rgba(255,255,255,0.05);
    border-radius: 20rpx;
    text-align: center;
    border: 2rpx solid transparent;

    &.active {
      border-color: #667eea;
      background: rgba(102,126,234,0.1);
    }

    .format-icon {
      font-size: 56rpx;
      margin-bottom: 12rpx;
    }

    .format-info {
      .format-name {
        display: block;
        font-size: 26rpx;
        color: #fff;
        margin-bottom: 4rpx;
      }

      .format-ext {
        font-size: 22rpx;
        color: #64748b;
      }
    }

    .format-radio {
      width: 32rpx;
      height: 32rpx;
      border: 2rpx solid #64748b;
      border-radius: 50%;
      margin: 16rpx auto 0;
      display: flex;
      align-items: center;
      justify-content: center;

      .radio-inner {
        width: 16rpx;
        height: 16rpx;
        background: #667eea;
        border-radius: 50%;
      }
    }
  }
}

// 导出选项
.option-list {
  background: rgba(255,255,255,0.05);
  border-radius: 20rpx;
  padding: 0 30rpx;

  .option-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 30rpx 0;
    border-bottom: 1rpx solid rgba(255,255,255,0.1);

    &:last-child {
      border-bottom: none;
    }

    .option-label {
      font-size: 30rpx;
      color: #fff;
    }
  }
}

// 底部按钮
.footer {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 30rpx;
  background: rgba(15,15,35,0.95);
  backdrop-filter: blur(20rpx);

  .export-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 20rpx;
    padding: 32rpx 0;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 40rpx;

    &.disabled {
      opacity: 0.5;
    }

    .btn-icon {
      font-size: 40rpx;
    }

    .btn-content {
      text-align: center;

      .btn-title {
        display: block;
        font-size: 32rpx;
        font-weight: 600;
        color: #fff;
      }

      .btn-desc {
        font-size: 24rpx;
        color: rgba(255,255,255,0.7);
      }
    }
  }
}

// 进度弹窗
.progress-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.8);
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;

  .progress-modal {
    width: 80%;
    background: #1a1a2e;
    border-radius: 32rpx;
    padding: 60rpx;
    text-align: center;

    .progress-icon {
      font-size: 80rpx;
      margin-bottom: 30rpx;
    }

    .progress-title {
      display: block;
      font-size: 36rpx;
      font-weight: 600;
      color: #fff;
      margin-bottom: 40rpx;
    }

    .progress-bar {
      height: 16rpx;
      background: rgba(255,255,255,0.1);
      border-radius: 8rpx;
      overflow: hidden;
      margin-bottom: 20rpx;

      .progress-fill {
        height: 100%;
        background: linear-gradient(90deg, #667eea, #764ba2);
        border-radius: 8rpx;
        transition: width 0.3s ease;
      }
    }

    .progress-text {
      font-size: 40rpx;
      font-weight: 700;
      color: #667eea;
      margin-bottom: 16rpx;
    }

    .progress-hint {
      font-size: 26rpx;
      color: #64748b;
    }
  }
}

// 完成弹窗
.complete-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.8);
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;

  .complete-modal {
    width: 80%;
    background: #1a1a2e;
    border-radius: 32rpx;
    padding: 60rpx;
    text-align: center;

    .success-icon {
      width: 120rpx;
      height: 120rpx;
      background: linear-gradient(135deg, #10b981, #059669);
      border-radius: 50%;
      margin: 0 auto 30rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 60rpx;
      color: #fff;
    }

    .complete-title {
      display: block;
      font-size: 40rpx;
      font-weight: 600;
      color: #fff;
      margin-bottom: 12rpx;
    }

    .complete-desc {
      display: block;
      font-size: 28rpx;
      color: #64748b;
      margin-bottom: 40rpx;
    }

    .file-info {
      background: rgba(255,255,255,0.05);
      border-radius: 16rpx;
      padding: 30rpx;
      margin-bottom: 40rpx;

      .file-name {
        display: block;
        font-size: 28rpx;
        color: #fff;
        margin-bottom: 8rpx;
        word-break: break-all;
      }

      .file-size {
        font-size: 24rpx;
        color: #667eea;
      }
    }

    .complete-actions {
      display: flex;
      gap: 20rpx;

      .btn {
        flex: 1;
        padding: 28rpx 0;
        border-radius: 40rpx;

        &.btn-share {
          background: rgba(255,255,255,0.1);
        }

        &.btn-done {
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