<template>
  <view class="scan-page">
    <!-- 顶部导航 -->
    <view class="nav-bar">
      <view class="back-btn" @click="goBack">
        <u-icon name="arrow-left" size="40" color="#fff"></u-icon>
      </view>
      <text class="title">扫码录入</text>
      <view class="right-btn" @click="toggleFlash">
        <u-icon :name="flashEnabled ? 'flashlight' : 'flashlight-off'" size="36" color="#fff"></u-icon>
      </view>
    </view>

    <!-- 扫描区域 -->
    <view class="scan-container">
      <!-- 相机预览 -->
      <camera
        v-if="!scanResult && !showAlbum"
        device-position="back"
        :flash="flashEnabled ? 'on' : 'off'"
        resolution="high"
        frame-size="large"
        class="camera-preview"
        @error="onCameraError"
      />

      <!-- 扫描框 -->
      <view v-if="!scanResult && !showAlbum" class="scan-frame">
        <view class="frame-corner top-left"></text>
        <view class="frame-corner top-right"></text>
        <view class="frame-corner bottom-left"></text>
        <view class="frame-corner bottom-right"></text>
        <view class="scan-line"></text>
        
        <view class="scan-tips">
          <text>将二维码/条形码放入框内</text>
        </view>
      </view>

      <!-- 相册选择图片预览 -->
      <image 
        v-if="showAlbum && albumImage" 
        :src="albumImage" 
        class="album-preview"
        mode="aspectFit"
      />
    </view>

    <!-- 扫描结果确认弹窗 -->
    <view v-if="scanResult" class="result-overlay">
      <view class="result-card">
        <view class="result-header">
          <view class="success-icon">
            <u-icon name="checkmark-circle" size="80" color="#52C41A"></u-icon>
          </view>
          <text class="result-title">扫描成功</text>
        </view>

        <view class="result-content">
          <!-- 商品信息 -->
          <view v-if="scanResult.type === 'product'" class="product-info">
            <image :src="scanResult.image || '/static/default-product.png'" class="product-image" mode="aspectFit" />
            
            <view class="product-details"
>
              <text class="product-name">{{ scanResult.name || '未知商品' }}</text>
              <text class="product-code">条码: {{ scanResult.code }}</text>
              <view class="product-price" v-if="scanResult.price">
                <text class="price-label">参考价格</text>
                <text class="price-value">¥{{ scanResult.price }}</text>
              </view>
            </view>          </view>

          <!-- URL信息 -->
          <view v-else-if="scanResult.type === 'url'" class="url-info">
            <u-icon name="link" size="60" color="#5B8FF9"></u-icon>
            <text class="url-text">{{ scanResult.content }}</text>
          </view>

          <!-- 文本信息 -->
          <view v-else class="text-info">
            <u-icon name="file-text" size="60" color="#666"></u-icon>
            <text class="text-content">{{ scanResult.content }}</text>
          </view>

          <!-- 操作选项 -->
          <view class="action-options">
            <view class="option-item">
              <text class="option-label">添加到</text>
              <view class="option-tabs">
                <view
                  v-for="target in addTargets"
                  :key="target.value"
                  class="option-tab"
                  :class="{ active: selectedTarget === target.value }"
                  @click="selectedTarget = target.value"
                >
                  <u-icon :name="target.icon" size="28" :color="selectedTarget === target.value ? '#5B8FF9' : '#999'"></u-icon>
                  {{ target.label }}
                </view>
              </view>
            </view>

            <view v-if="selectedTarget === 'shopping'" class="option-item">
              <text class="option-label">数量</text>
              <view class="quantity-control">
                <text class="btn-minus" @click="decreaseQuantity">-</text>
                <input v-model.number="quantity" type="number" class="quantity-input" />
                <text class="btn-plus" @click="increaseQuantity">+</text>
              </view>
            </view>

            <view class="option-item">
              <text class="option-label">备注</text>
              <input
                v-model="remark"
                placeholder="添加备注..."
                class="remark-input"
              />
            </view>
          </view>
        </view>

        <view class="result-actions">
          <view class="btn-cancel" @click="cancelResult">重新扫描</view>
          <view class="btn-confirm" @click="confirmAdd"
>确认添加</view>
        </view>
      </view>
    </view>

    <!-- 底部操作栏 -->
    <view v-if="!scanResult" class="bottom-actions">
      <view class="action-item" @click="scanFromAlbum">
        <view class="action-icon">
          <u-icon name="photo" size="40" color="#fff"></u-icon>
        </view>
        <text>相册</text>
      </view>

      <view class="action-item scan-btn" @click="startScan">
        <view class="scan-button"
>
          <u-icon name="scan" size="48" color="#5B8FF9"></u-icon>
        </view>
      </view>

      <view class="action-item" @click="toggleInputMode"
>
        <view class="action-icon"
>
          <u-icon name="edit-pen" size="40" color="#fff"></u-icon>
        </view>
        <text>手动输入</text>
      </view>
    </view>

    <!-- 手动输入弹窗 -->
    <u-popup
      v-model:show="inputModalVisible"
      mode="bottom"
      round
      closeable
    >
      <view class="modal-content">
        <view class="modal-header">
          <text class="modal-title">手动输入条码</text>
        </view>

        <view class="modal-body">
          <view class="input-section">
            <text class="input-label">条形码/二维码内容</text>
            <input
              v-model="manualInput"
              placeholder="请输入条码数字或扫描内容"
              class="barcode-input"
              type="text"
            />
          </view>

          <view class="quick-types">
            <text class="quick-title">常见条码</text>
            <view class="type-tags"
>
              <view
                v-for="type in barcodeTypes"
                :key="type"
                class="type-tag"
                @click="manualInput = type"
              >
                {{ type }}
              </view>
            </view>
          </view>
        </view>

        <view class="modal-footer">
          <view class="btn-cancel" @click="inputModalVisible = false">取消</view>
          <view class="btn-confirm" @click="confirmManualInput">确认</view>
        </view>
      </view>
    </u-popup>

    <!-- 扫描历史 -->
    <view v-if="scanHistory.length > 0 && !scanResult" class="history-section">
      <view class="history-header">
        <text class="history-title">扫描历史</text>
        <text class="clear-btn" @click="clearHistory">清空</text>
      </view>

      <scroll-view class="history-list" scroll-x>
        <view
          v-for="(item, index) in scanHistory"
          :key="index"
          class="history-item"
          @click="reuseHistory(item)"
        >
          <image v-if="item.image" :src="item.image" class="history-image" mode="aspectFill" />
          <view v-else class="history-icon"
>
            <u-icon :name="item.type === 'url' ? 'link' : 'barcode'" size="40" color="#999"></u-icon>
          </view>
          
          <text class="history-name">{{ item.name || item.content.substring(0, 10) + '...' }}</text>
          <text class="history-time">{{ formatTime(item.time) }}</text>
        </view>
      </scroll-view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import dayjs from 'dayjs'

// 响应式数据
const flashEnabled = ref(false)
const scanResult = ref(null)
const showAlbum = ref(false)
const albumImage = ref('')
const selectedTarget = ref('shopping')
const quantity = ref(1)
const remark = ref('')
const inputModalVisible = ref(false)
const manualInput = ref('')
const scanHistory = ref([])

// 添加目标选项
const addTargets = [
  { label: '购物清单', value: 'shopping', icon: 'shopping-cart' },
  { label: '库存', value: 'inventory', icon: 'bag' },
  { label: '任务', value: 'task', icon: 'calendar' },
  { label: '心愿', value: 'wish', icon: 'heart' }
]

// 常见条码示例
const barcodeTypes = [
  '6901234567890',
  '9787115428028',
  'https://example.com'
]

// 页面加载
onMounted(() => {
  loadHistory()
  // 自动开始扫描
  setTimeout(() => {
    startScan()
  }, 500)
})

// 加载历史记录
const loadHistory = () => {
  const history = uni.getStorageSync('scanHistory') || []
  scanHistory.value = history.slice(0, 10)
}

// 保存历史记录
const saveHistory = (item) => {
  const history = uni.getStorageSync('scanHistory') || []
  history.unshift({
    ...item,
    time: Date.now()
  })
  // 只保留最近20条
  if (history.length > 20) {
    history.pop()
  }
  uni.setStorageSync('scanHistory', history)
  scanHistory.value = history.slice(0, 10)
}

// 切换闪光灯
const toggleFlash = () => {
  flashEnabled.value = !flashEnabled.value
}

// 开始扫描
const startScan = () => {
  uni.scanCode({
    scanType: ['qrCode', 'barCode', 'datamatrix', 'pdf417'],
    success: (res) => {
      handleScanResult(res.result)
    },
    fail: (err) => {
      console.error('扫描失败', err)
      uni.showToast({ title: '扫描失败', icon: 'none' })
    }
  })
}

// 从相册选择
const scanFromAlbum = () => {
  uni.chooseImage({
    count: 1,
    sourceType: ['album'],
    success: (res) => {
      albumImage.value = res.tempFilePaths[0]
      showAlbum.value = true
      
      // 解析图片中的二维码
      uni.scanCode({
        scanType: ['qrCode', 'barCode'],
        sourceType: 'album',
        success: (scanRes) => {
          handleScanResult(scanRes.result)
        },
        fail: () => {
          uni.showToast({ title: '未识别到二维码', icon: 'none' })
          showAlbum.value = false
        }
      })
    }
  })
}

// 处理扫描结果
const handleScanResult = (result) => {
  // 解析结果类型
  let type = 'text'
  let name = ''
  let price = ''
  
  if (result.startsWith('http://') || result.startsWith('https://')) {
    type = 'url'
  } else if (/^\d{8,13}$/.test(result)) {
    type = 'product'
    // 模拟查询商品信息
    name = queryProductInfo(result)
    price = (Math.random() * 100 + 10).toFixed(2)
  }
  
  scanResult.value = {
    type,
    content: result,
    code: result,
    name,
    price,
    image: type === 'product' ? '/static/product-' + Math.floor(Math.random() * 5) + '.png' : ''
  }
  
  // 添加到历史
  saveHistory(scanResult.value)
}

// 模拟查询商品信息
const queryProductInfo = (code) => {
  const products = [
    '可口可乐 330ml',
    '乐事薯片 原味',
    '伊利纯牛奶 250ml',
    '康师傅方便面 红烧牛肉',
    '旺仔牛奶 245ml',
    '奥利奥饼干 原味'
  ]
  // 根据条码生成固定的商品名
  const index = code.split('').reduce((a, b) => a + parseInt(b), 0) % products.length
  return products[index]
}

// 切换输入模式
const toggleInputMode = () => {
  inputModalVisible.value = true
}

// 确认手动输入
const confirmManualInput = () => {
  if (!manualInput.value.trim()) {
    uni.showToast({ title: '请输入内容', icon: 'none' })
    return
  }
  inputModalVisible.value = false
  handleScanResult(manualInput.value)
  manualInput.value = ''
}

// 数量控制
const increaseQuantity = () => {
  quantity.value++
}

const decreaseQuantity = () => {
  if (quantity.value > 1) quantity.value--
}

// 取消结果
const cancelResult = () => {
  scanResult.value = null
  showAlbum.value = false
  albumImage.value = ''
  quantity.value = 1
  remark.value = ''
}

// 确认添加
const confirmAdd = () => {
  const data = {
    target: selectedTarget.value,
    ...scanResult.value,
    quantity: quantity.value,
    remark: remark.value,
    addTime: dayjs().format('YYYY-MM-DD HH:mm:ss')
  }
  
  // 模拟添加到对应列表
  uni.showLoading({ title: '添加中...' })
  
  setTimeout(() => {
    uni.hideLoading()
    uni.showToast({
      title: `已添加到${addTargets.find(t => t.value === selectedTarget.value)?.label}`,
      icon: 'success'
    })
    
    // 通知上一页
    const eventChannel = getOpenerEventChannel()
    if (eventChannel && eventChannel.emit) {
      eventChannel.emit('onScanResult', data)
    }
    
    setTimeout(() => {
      // 返回或继续扫描
      uni.showModal({
        title: '添加成功',
        content: '是否继续扫描？',
        confirmText: '继续',
        cancelText: '返回',
        success: (res) => {
          if (res.confirm) {
            cancelResult()
            startScan()
          } else {
            uni.navigateBack()
          }
        }
      })
    }, 1500)
  }, 1000)
}

// 相机错误
const onCameraError = (e) => {
  console.error('相机错误', e)
  uni.showToast({ title: '相机启动失败', icon: 'none' })
}

// 格式化时间
const formatTime = (timestamp) => {
  return dayjs(timestamp).format('HH:mm')
}

// 重新使用历史记录
const reuseHistory = (item) => {
  scanResult.value = { ...item }
  delete scanResult.value.time
}

// 清空历史
const clearHistory = () => {
  uni.showModal({
    title: '确认清空',
    content: '确定要清空扫描历史吗？',
    success: (res) => {
      if (res.confirm) {
        uni.removeStorageSync('scanHistory')
        scanHistory.value = []
        uni.showToast({ title: '已清空', icon: 'success' })
      }
    }
  })
}

const goBack = () => {
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
.scan-page {
  min-height: 100vh;
  background: #000;
  display: flex;
  flex-direction: column;
}

.nav-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 30rpx;
  padding-top: 60rpx;
  background: rgba(0, 0, 0, 0.5);
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;

  .back-btn, .right-btn {
    width: 60rpx;
    height: 60rpx;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .title {
    font-size: 34rpx;
    font-weight: 600;
    color: #fff;
  }
}

.scan-container {
  flex: 1;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.camera-preview, .album-preview {
  width: 100%;
  height: 100%;
}

// 扫描框
.scan-frame {
  position: absolute;
  width: 500rpx;
  height: 500rpx;
  border: 2rpx solid rgba(255, 255, 255, 0.3);

  .frame-corner {
    position: absolute;
    width: 60rpx;
    height: 60rpx;
    border-color: #5B8FF9;
    border-style: solid;
    border-width: 0;

    &.top-left {
      top: -4rpx;
      left: -4rpx;
      border-top-width: 8rpx;
      border-left-width: 8rpx;
    }

    &.top-right {
      top: -4rpx;
      right: -4rpx;
      border-top-width: 8rpx;
      border-right-width: 8rpx;
    }

    &.bottom-left {
      bottom: -4rpx;
      left: -4rpx;
      border-bottom-width: 8rpx;
      border-left-width: 8rpx;
    }

    &.bottom-right {
      bottom: -4rpx;
      right: -4rpx;
      border-bottom-width: 8rpx;
      border-right-width: 8rpx;
    }
  }

  .scan-line {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 4rpx;
    background: linear-gradient(90deg, transparent, #5B8FF9, transparent);
    animation: scan 2s linear infinite;
  }

  .scan-tips {
    position: absolute;
    bottom: -80rpx;
    left: 0;
    right: 0;
    text-align: center;

    text {
      font-size: 28rpx;
      color: rgba(255, 255, 255, 0.8);
    }
  }
}

@keyframes scan {
  0% {
    top: 0;
    opacity: 0;
  }
  10% {
    opacity: 1;
  }
  90% {
    opacity: 1;
  }
  100% {
    top: 100%;
    opacity: 0;
  }
}

// 扫描结果
.result-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60rpx 40rpx;
  z-index: 200;
}

.result-card {
  width: 100%;
  background: #fff;
  border-radius: 32rpx;
  overflow: hidden;
}

.result-header {
  text-align: center;
  padding: 40rpx;
  background: linear-gradient(135deg, #52C41A 0%, #73D13D 100%);

  .success-icon {
    margin-bottom: 16rpx;
  }

  .result-title {
    font-size: 36rpx;
    font-weight: 600;
    color: #fff;
  }
}

.result-content {
  padding: 30rpx;
}

// 商品信息
.product-info {
  display: flex;
  align-items: flex-start;
  margin-bottom: 30rpx;

  .product-image {
    width: 160rpx;
    height: 160rpx;
    background: #f5f5f5;
    border-radius: 16rpx;
    margin-right: 24rpx;
  }

  .product-details {
    flex: 1;

    .product-name {
      font-size: 32rpx;
      font-weight: 600;
      color: #333;
      display: block;
      margin-bottom: 12rpx;
    }

    .product-code {
      font-size: 24rpx;
      color: #999;
      display: block;
      margin-bottom: 16rpx;
    }

    .product-price {
      display: flex;
      align-items: baseline;
      gap: 12rpx;

      .price-label {
        font-size: 24rpx;
        color: #666;
      }

      .price-value {
        font-size: 40rpx;
        font-weight: 700;
        color: #FF4D4F;
      }
    }
  }
}

// URL信息
.url-info {
  text-align: center;
  padding: 40rpx;

  .url-text {
    font-size: 28rpx;
    color: #5B8FF9;
    word-break: break-all;
    margin-top: 20rpx;
    display: block;
  }
}

// 文本信息
.text-info {
  text-align: center;
  padding: 40rpx;

  .text-content {
    font-size: 28rpx;
    color: #333;
    word-break: break-all;
    margin-top: 20rpx;
    display: block;
  }
}

// 操作选项
.action-options {
  .option-item {
    margin-bottom: 24rpx;

    .option-label {
      font-size: 26rpx;
      color: #666;
      margin-bottom: 12rpx;
      display: block;
    }
  }

  .option-tabs {
    display: flex;
    gap: 16rpx;
    flex-wrap: wrap;

    .option-tab {
      display: flex;
      align-items: center;
      gap: 8rpx;
      padding: 16rpx 24rpx;
      background: #f5f6fa;
      border-radius: 12rpx;
      font-size: 26rpx;
      color: #666;
      border: 2rpx solid transparent;

      &.active {
        border-color: #5B8FF9;
        background: #f0f5ff;
        color: #5B8FF9;
      }
    }
  }

  .quantity-control {
    display: flex;
    align-items: center;
    gap: 20rpx;
    background: #f5f6fa;
    border-radius: 12rpx;
    padding: 0 20rpx;
    width: fit-content;

    .btn-minus, .btn-plus {
      width: 60rpx;
      height: 60rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 32rpx;
      color: #5B8FF9;
      font-weight: bold;
    }

    .quantity-input {
      width: 80rpx;
      height: 60rpx;
      text-align: center;
      font-size: 32rpx;
      color: #333;
      background: transparent;
    }
  }

  .remark-input {
    width: 100%;
    background: #f5f6fa;
    border-radius: 12rpx;
    padding: 20rpx 24rpx;
    font-size: 28rpx;
    color: #333;
  }
}

// 结果操作按钮
.result-actions {
  display: flex;
  gap: 20rpx;
  padding: 20rpx 30rpx 40rpx;

  .btn-cancel {
    flex: 1;
    padding: 24rpx 0;
    background: #f5f5f5;
    border-radius: 12rpx;
    text-align: center;
    font-size: 30rpx;
    color: #666;
  }

  .btn-confirm {
    flex: 2;
    padding: 24rpx 0;
    background: #5B8FF9;
    border-radius: 12rpx;
    text-align: center;
    font-size: 30rpx;
    color: #fff;
  }
}

// 底部操作栏
.bottom-actions {
  display: flex;
  justify-content: space-around;
  align-items: center;
  padding: 30rpx 60rpx calc(30rpx + constant(safe-area-inset-bottom));
  padding: 30rpx 60rpx calc(30rpx + env(safe-area-inset-bottom));
  background: rgba(0, 0, 0, 0.5);

  .action-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 12rpx;

    text {
      font-size: 24rpx;
      color: #fff;
    }

    .action-icon {
      width: 80rpx;
      height: 80rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      background: rgba(255, 255, 255, 0.2);
      border-radius: 50%;
    }
  }

  .scan-btn {
    .scan-button {
      width: 140rpx;
      height: 140rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      background: #fff;
      border-radius: 50%;
      box-shadow: 0 8rpx 32rpx rgba(91, 143, 249, 0.4);
    }
  }
}

// 历史记录
.history-section {
  position: absolute;
  bottom: 200rpx;
  left: 0;
  right: 0;
  padding: 0 30rpx;

  .history-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16rpx;

    .history-title {
      font-size: 26rpx;
      color: rgba(255, 255, 255, 0.8);
    }

    .clear-btn {
      font-size: 24rpx;
      color: rgba(255, 255, 255, 0.6);
    }
  }

  .history-list {
    white-space: nowrap;

    .history-item {
      display: inline-flex;
      flex-direction: column;
      align-items: center;
      width: 140rpx;
      margin-right: 20rpx;

      .history-image, .history-icon {
        width: 100rpx;
        height: 100rpx;
        border-radius: 16rpx;
        background: rgba(255, 255, 255, 0.2);
        margin-bottom: 8rpx;
      }

      .history-name {
        font-size: 22rpx;
        color: rgba(255, 255, 255, 0.9);
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        max-width: 100%;
      }

      .history-time {
        font-size: 20rpx;
        color: rgba(255, 255, 255, 0.6);
      }
    }
  }
}

// 弹窗样式
.modal-content {
  padding: 30rpx 0;

  .modal-header {
    text-align: center;
    padding: 0 30rpx 30rpx;
    border-bottom: 1rpx solid #f5f5f5;

    .modal-title {
      font-size: 32rpx;
      font-weight: 600;
      color: #333;
    }
  }

  .modal-body {
    padding: 30rpx;
  }

  .modal-footer {
    display: flex;
    gap: 20rpx;
    padding: 0 30rpx;

    .btn-cancel {
      flex: 1;
      padding: 24rpx 0;
      background: #f5f5f5;
      border-radius: 12rpx;
      text-align: center;
      font-size: 30rpx;
      color: #666;
    }

    .btn-confirm {
      flex: 1;
      padding: 24rpx 0;
      background: #5B8FF9;
      border-radius: 12rpx;
      text-align: center;
      font-size: 30rpx;
      color: #fff;
    }
  }
}

.input-section {
  margin-bottom: 30rpx;

  .input-label {
    font-size: 26rpx;
    color: #666;
    margin-bottom: 12rpx;
    display: block;
  }

  .barcode-input {
    background: #f5f6fa;
    border-radius: 12rpx;
    padding: 24rpx;
    font-size: 32rpx;
    color: #333;
  }
}

.quick-types {
  .quick-title {
    font-size: 26rpx;
    color: #666;
    margin-bottom: 16rpx;
    display: block;
  }

  .type-tags {
    display: flex;
    flex-wrap: wrap;
    gap: 16rpx;

    .type-tag {
      padding: 16rpx 24rpx;
      background: #f0f5ff;
      border-radius: 8rpx;
      font-size: 26rpx;
      color: #5B8FF9;
    }
  }
}
</style>