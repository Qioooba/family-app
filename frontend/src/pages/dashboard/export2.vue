<template>
  <view class="export-page">
    <!-- 顶部导航 -->
    <view class="nav-bar"
>
      <view class="back-btn" @click="goBack">
        <u-icon name="arrow-left" size="40" color="#333"></u-icon>
      </view>
      <text class="title">数据导出</text>
      <view class="right-btn" @click="showHistory">
        <text>记录</text>
      </view>
    </view>

    <view class="content">
      <!-- 导出概览 -->
      <view class="overview-card">
        <view class="overview-icon">📊</view>        
        <view class="overview-info"
>
          <text class="overview-title">家庭数据导出</text>          
          <text class="overview-desc">将您的家庭数据导出为Excel或PDF格式，方便备份和分析</text>
        </view>      
      </view>

      <!-- 数据类型选择 -->
      <view class="data-types-section"
>
        <text class="section-title">选择导出数据</text>
        
        <view class="data-types"
>
          <view
            v-for="type in dataTypes"
            :key="type.value"
            class="type-item"
            :class="{ 
              selected: selectedTypes.includes(type.value),
              disabled: exportStatus === 'exporting'
            }"
            @click="toggleType(type.value)"
          >
            <view class="type-checkbox"
>
              <view v-if="selectedTypes.includes(type.value)" class="check-inner"></text>
            </view>            
            
            <view class="type-info">
              <text class="type-icon">{{ type.icon }}</text>              
              <view class="type-text">
                <text class="type-name">{{ type.name }}</text>                
                <text class="type-count">{{ type.count }}条记录</text>
              </view>            
            </view>          
          </view>        
        </view>        
        
        <view class="select-all-row">
          <text class="select-text" @click="selectAll">
            {{ isAllSelected ? '取消全选' : '全选' }}
          </text>          
          <text class="selected-count">已选 {{ selectedTypes.length }} 项</text>
        </view>
      </view>

      <!-- 时间范围 -->
      <view class="time-range-section">
        <text class="section-title">时间范围</text>
        
        <view class="time-options"
>
          <view
            v-for="option in timeOptions"
            :key="option.value"
            class="time-option"
            :class="{ active: timeRange === option.value }"
            @click="timeRange = option.value"
          >
            {{ option.label }}
          </view>        
        </view>        
        
        <view v-if="timeRange === 'custom'" class="custom-range"
>
          <view class="date-picker"
>
            <text class="picker-label">开始日期</text>            
            <picker mode="date" :value="startDate" @change="onStartDateChange">
              <view class="picker-value">{{ startDate || '请选择' }}</view>
            </picker>          
          </view>          
          
          <text class="range-separator">至</text>          
          
          <view class="date-picker"
>
            <text class="picker-label">结束日期</text>            
            <picker mode="date" :value="endDate" @change="onEndDateChange">
              <view class="picker-value">{{ endDate || '请选择' }}</view>
            </picker>          
          </view>        
        </view>
      </view>

      <!-- 导出格式 -->
      <view class="format-section">
        <text class="section-title">导出格式</text>
        
        <view class="format-options"
>
          <view
            v-for="format in formats"
            :key="format.value"
            class="format-option"
            :class="{ active: selectedFormat === format.value }"
            @click="selectedFormat = format.value"
          >
            <view class="format-icon" :style="{ background: format.color + '20' }"
>
              <text :style="{ color: format.color }">{{ format.icon }}</text>
            </view>            
            
            <view class="format-info">
              <text class="format-name">{{ format.name }}</text>              
              <text class="format-desc">{{ format.desc }}</text>
            </view>            
            
            <view class="format-radio"
>
              <view v-if="selectedFormat === format.value" class="radio-inner"></text>
            </view>          
          </view>        
        </view>
      </view>

      <!-- 导出按钮 -->
      <view class="export-action"
>
        <view 
          class="export-btn"
          :class="{ 
            disabled: !canExport || exportStatus === 'exporting',
            success: exportStatus === 'success'
          }"
          @click="startExport"
        >
          <view v-if="exportStatus === 'exporting'" class="btn-loading">
            <view class="spinner"></view>            
            <text>导出中 {{ exportProgress }}%</text>          
          </view>          
          <block v-else>
            <u-icon :name="exportStatus === 'success' ? 'checkmark' : 'download'" size="32" color="#fff"
</u-icon>            
            <text>{{ exportButtonText }}</text>          
          </block>        
        </view>        
        
        <text v-if="estimatedSize" class="size-hint">预计文件大小：{{ estimatedSize }}</text>      
      </view>

      <!-- 导出说明 -->
      <view class="tips-section"
>
        <view class="tip-item">
          <u-icon name="info-circle" size="28" color="#999"></u-icon>          
          <text>导出的数据仅包含您有权限查看的内容</text>        
        </view>        
        
        <view class="tip-item">
          <u-icon name="info-circle" size="28" color="#999"></u-icon>          
          <text>导出完成后将自动保存到手机相册或下载目录</text>        
        </view>        
        
        <view class="tip-item">
          <u-icon name="info-circle" size="28" color="#999"></u-icon>          
          <text>大型数据导出可能需要较长时间，请保持应用在前台</text>        
        </view>      
      </view>
    </view>

    <!-- 导出成功弹窗 -->
    <u-popup
      v-model:show="successModalVisible"
      mode="center"
    >
      <view class="success-modal"
>
        <view class="success-icon">
          <view class="icon-circle"
>
            <u-icon name="checkmark" size="60" color="#fff"></u-icon>
          </view>        
        </view>        
        
        <text class="success-title">导出成功！</text>        
        
        <text class="success-desc">
          文件已保存到您的设备
        </text>        
        
        <view class="file-info">
          <text class="file-name">{{ exportedFile.name }}</text>          
          <text class="file-size">{{ exportedFile.size }}</text>        
        </view>

        <view class="success-actions"
>
          <view class="btn-share" @click="shareFile">分享</view>          
          
          <view class="btn-done" @click="successModalVisible = false">完成</view>        
        </view>      
      </view>    
    </u-popup>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import dayjs from 'dayjs'

// 响应式数据
const selectedTypes = ref(['tasks', 'expenses'])
const timeRange = ref('month')
const selectedFormat = ref('excel')
const exportStatus = ref('idle') // idle, exporting, success
const exportProgress = ref(0)
const startDate = ref('')
const endDate = ref('')
const successModalVisible = ref(false)

const exportedFile = ref({
  name: '家庭数据_2026-02-23.xlsx',
  size: '2.3 MB'
})

// 数据类型
const dataTypes = [
  { name: '任务记录', value: 'tasks', icon: '📋', count: 156 },
  { name: '支出明细', value: 'expenses', icon: '💰', count: 89 },
  { name: '心愿列表', value: 'wishes', icon: '💝', count: 24 },
  { name: '购物清单', value: 'shopping', icon: '🛒', count: 45 },
  { name: '家庭动态', value: 'moments', icon: '📝', count: 78 },
  { name: '日历事件', value: 'calendar', icon: '📅', count: 32 }
]

// 时间选项
const timeOptions = [
  { label: '最近7天', value: 'week' },
  { label: '最近30天', value: 'month' },
  { label: '最近3个月', value: 'quarter' },
  { label: '最近1年', value: 'year' },
  { label: '全部', value: 'all' },
  { label: '自定义', value: 'custom' }
]

// 导出格式
const formats = [
  { 
    name: 'Excel表格', 
    value: 'excel', 
    icon: '📊', 
    desc: '适合数据分析',
    color: '#52C41A'
  },
  { 
    name: 'PDF文档', 
    value: 'pdf', 
    icon: '📄', 
    desc: '适合打印和分享',
    color: '#FF4D4F'
  },
  { 
    name: 'JSON数据', 
    value: 'json', 
    icon: '📑', 
    desc: '适合开发者',
    color: '#1890FF'
  }
]

// 计算属性
const isAllSelected = computed(() => {
  return selectedTypes.value.length === dataTypes.length
})

const canExport = computed(() => {
  if (selectedTypes.value.length === 0) return false
  if (timeRange.value === 'custom' && (!startDate.value || !endDate.value)) return false
  return true
})

const exportButtonText = computed(() => {
  switch (exportStatus.value) {
    case 'success':
      return '导出完成'
    default:
      return '开始导出'
  }
})

const estimatedSize = computed(() => {
  if (selectedTypes.value.length === 0) return ''
  const totalRecords = dataTypes
    .filter(t => selectedTypes.value.includes(t.value))
    .reduce((sum, t) => sum + t.count, 0)
  const size = (totalRecords * 0.5).toFixed(1)
  return `${size} KB`
})

// 方法
const toggleType = (value) => {
  if (exportStatus.value === 'exporting') return
  
  const index = selectedTypes.value.indexOf(value)
  if (index > -1) {
    selectedTypes.value.splice(index, 1)
  } else {
    selectedTypes.value.push(value)
  }
}

const selectAll = () => {
  if (isAllSelected.value) {
    selectedTypes.value = []
  } else {
    selectedTypes.value = dataTypes.map(t => t.value)
  }
}

const onStartDateChange = (e) => {
  startDate.value = e.detail.value
}

const onEndDateChange = (e) => {
  endDate.value = e.detail.value
}

const startExport = () => {
  if (!canExport.value || exportStatus.value === 'exporting') return
  
  exportStatus.value = 'exporting'
  exportProgress.value = 0
  
  // 模拟导出进度
  const interval = setInterval(() => {
    exportProgress.value += Math.floor(Math.random() * 15) + 5
    
    if (exportProgress.value >= 100) {
      exportProgress.value = 100
      clearInterval(interval)
      
      setTimeout(() => {
        exportStatus.value = 'success'
        successModalVisible.value = true
        
        // 生成文件名
        const timestamp = dayjs().format('YYYY-MM-DD_HHmmss')
        const ext = selectedFormat.value === 'excel' ? 'xlsx' : selectedFormat.value
        exportedFile.value.name = `家庭数据_${timestamp}.${ext}`
        
        // 保存导出记录
        saveExportHistory()
      }, 500)
    }
  }, 300)
}

const saveExportHistory = () => {
  const history = uni.getStorageSync('exportHistory') || []
  history.unshift({
    types: [...selectedTypes.value],
    format: selectedFormat.value,
    timeRange: timeRange.value,
    fileName: exportedFile.value.name,
    fileSize: exportedFile.value.size,
    exportTime: dayjs().format('YYYY-MM-DD HH:mm:ss')
  })
  if (history.length > 20) {
    history.pop()
  }
  uni.setStorageSync('exportHistory', history)
}

const shareFile = () => {
  uni.showShareMenu({
    withShareTicket: true
  })
}

const showHistory = () => {
  uni.navigateTo({ url: '/pages/dashboard/export-history' })
}

const goBack = () => {
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
.export-page {
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
    padding: 10rpx 24rpx;
    background: #f5f6fa;
    border-radius: 30rpx;

    text {
      font-size: 26rpx;
      color: #666;
    }
  }
}

.content {
  padding: 30rpx;
  padding-bottom: 50rpx;
}

// 概览卡片
.overview-card {
  display: flex;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 24rpx;
  padding: 40rpx;
  margin-bottom: 40rpx;

  .overview-icon {
    font-size: 80rpx;
    margin-right: 24rpx;
  }

  .overview-info {
    flex: 1;

    .overview-title {
      font-size: 36rpx;
      font-weight: 600;
      color: #fff;
      display: block;
      margin-bottom: 12rpx;
    }

    .overview-desc {
      font-size: 26rpx;
      color: rgba(255, 255, 255, 0.8);
      line-height: 1.6;
    }
  }
}

// 数据类型选择
.data-types-section {
  background: #fff;
  border-radius: 24rpx;
  padding: 30rpx;
  margin-bottom: 30rpx;
}

.section-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #333;
  margin-bottom: 24rpx;
  display: block;
}

.data-types {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16rpx;
  margin-bottom: 24rpx;
}

.type-item {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 24rpx;
  background: #f5f6fa;
  border-radius: 16rpx;
  border: 2rpx solid transparent;

  &.selected {
    border-color: #667eea;
    background: #f0f5ff;

    .type-checkbox {
      border-color: #667eea;
      background: #667eea;
    }
  }

  &.disabled {
    opacity: 0.5;
  }

  .type-checkbox {
    width: 36rpx;
    height: 36rpx;
    border: 4rpx solid #ddd;
    border-radius: 8rpx;
    display: flex;
    align-items: center;
    justify-content: center;

    .check-inner {
      width: 18rpx;
      height: 18rpx;
      background: #fff;
      border-radius: 4rpx;
    }
  }

  .type-info {
    flex: 1;
    display: flex;
    align-items: center;
    gap: 12rpx;

    .type-icon {
      font-size: 40rpx;
    }

    .type-text {
      .type-name {
        font-size: 28rpx;
        color: #333;
        display: block;
      }

      .type-count {
        font-size: 22rpx;
        color: #999;
      }
    }
  }
}

.select-all-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 20rpx;
  border-top: 1rpx solid #f5f5f5;

  .select-text {
    font-size: 28rpx;
    color: #667eea;
  }

  .selected-count {
    font-size: 26rpx;
    color: #999;
  }
}

// 时间范围
.time-range-section {
  background: #fff;
  border-radius: 24rpx;
  padding: 30rpx;
  margin-bottom: 30rpx;
}

.time-options {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
  margin-bottom: 24rpx;

  .time-option {
    padding: 16rpx 24rpx;
    background: #f5f6fa;
    border-radius: 12rpx;
    font-size: 26rpx;
    color: #666;
    border: 2rpx solid transparent;

    &.active {
      border-color: #667eea;
      color: #667eea;
      background: #f0f5ff;
    }
  }
}

.custom-range {
  display: flex;
  align-items: center;
  gap: 20rpx;
  padding: 24rpx;
  background: #f5f6fa;
  border-radius: 16rpx;

  .date-picker {
    flex: 1;

    .picker-label {
      font-size: 22rpx;
      color: #999;
      margin-bottom: 8rpx;
      display: block;
    }

    .picker-value {
      font-size: 28rpx;
      color: #333;
      font-weight: 500;
    }
  }

  .range-separator {
    font-size: 26rpx;
    color: #999;
  }
}

// 导出格式
.format-section {
  background: #fff;
  border-radius: 24rpx;
  padding: 30rpx;
  margin-bottom: 40rpx;
}

.format-options {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.format-option {
  display: flex;
  align-items: center;
  padding: 24rpx;
  background: #f5f6fa;
  border-radius: 16rpx;
  border: 2rpx solid transparent;

  &.active {
    border-color: #667eea;
    background: #f0f5ff;

    .format-radio {
      border-color: #667eea;
    }
  }

  .format-icon {
    width: 80rpx;
    height: 80rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 16rpx;
    font-size: 40rpx;
    margin-right: 20rpx;
  }

  .format-info {
    flex: 1;

    .format-name {
      font-size: 30rpx;
      font-weight: 500;
      color: #333;
      display: block;
      margin-bottom: 4rpx;
    }

    .format-desc {
      font-size: 24rpx;
      color: #999;
    }
  }

  .format-radio {
    width: 40rpx;
    height: 40rpx;
    border-radius: 50%;
    border: 4rpx solid #ddd;
    display: flex;
    align-items: center;
    justify-content: center;

    .radio-inner {
      width: 20rpx;
      height: 20rpx;
      background: #667eea;
      border-radius: 50%;
    }
  }
}

// 导出按钮
.export-action {
  margin-bottom: 40rpx;

  .export-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 12rpx;
    padding: 30rpx 0;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 16rpx;
    margin-bottom: 16rpx;

    text {
      font-size: 32rpx;
      font-weight: 600;
      color: #fff;
    }

    &.disabled {
      background: #ccc;
    }

    &.success {
      background: #52C41A;
    }

    .btn-loading {
      display: flex;
      align-items: center;
      gap: 16rpx;

      text {
        font-size: 32rpx;
        color: #fff;
      }

      .spinner {
        width: 36rpx;
        height: 36rpx;
        border: 4rpx solid rgba(255, 255, 255, 0.3);
        border-top-color: #fff;
        border-radius: 50%;
        animation: spin 1s linear infinite;
      }
    }
  }

  .size-hint {
    font-size: 24rpx;
    color: #999;
    text-align: center;
    display: block;
  }
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

// 提示
.tips-section {
  .tip-item {
    display: flex;
    align-items: flex-start;
    gap: 12rpx;
    margin-bottom: 16rpx;

    text {
      flex: 1;
      font-size: 24rpx;
      color: #999;
      line-height: 1.6;
    }
  }
}

// 成功弹窗
.success-modal {
  width: 560rpx;
  background: #fff;
  border-radius: 24rpx;
  padding: 50rpx 40rpx;
  text-align: center;
}

.success-icon {
  margin-bottom: 30rpx;

  .icon-circle {
    width: 120rpx;
    height: 120rpx;
    background: linear-gradient(135deg, #52C41A 0%, #73D13D 100%);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 auto;
  }
}

.success-title {
  font-size: 40rpx;
  font-weight: 700;
  color: #333;
  display: block;
  margin-bottom: 16rpx;
}

.success-desc {
  font-size: 28rpx;
  color: #666;
  display: block;
  margin-bottom: 30rpx;
}

.file-info {
  background: #f5f6fa;
  border-radius: 12rpx;
  padding: 24rpx;
  margin-bottom: 30rpx;

  .file-name {
    font-size: 28rpx;
    font-weight: 500;
    color: #333;
    display: block;
    margin-bottom: 8rpx;
    word-break: break-all;
  }

  .file-size {
    font-size: 24rpx;
    color: #999;
  }
}

.success-actions {
  display: flex;
  gap: 20rpx;

  .btn-share, .btn-done {
    flex: 1;
    padding: 24rpx 0;
    border-radius: 12rpx;
    font-size: 30rpx;
  }

  .btn-share {
    background: #f5f5f5;
    color: #666;
  }

  .btn-done {
    background: linear-gradient(135deg, #52C41A 0%, #73D13D 100%);
    color: #fff;
  }
}
</style>