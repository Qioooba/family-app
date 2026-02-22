<template>
  <view class="page-container">
    <view class="header">
      <view class="header-left" @click="goBack"><text>‹</text></text>
      <view class="header-title">电子签名</view>
    </view>
    
    <view class="signature-area"
>
      <canvas 
        canvas-id="signatureCanvas" 
        class="signature-canvas"
        @touchstart="startDraw"
        @touchmove="draw"
        @touchend="endDraw"
      ></canvas>
      
      <view v-if="!hasDrawn" class="signature-hint"
>
        <text>✍️ 在此区域签名</text>
      </view>
    </view>
    
    <view class="signature-actions"
>
      <view class="action-btn" @click="clear"
清空</view>
      <view class="action-btn primary" @click="save"
保存签名</view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'

const hasDrawn = ref(false)
let ctx = null
let isDrawing = false

const startDraw = (e) => {
  isDrawing = true
  const { x, y } = e.touches[0]
  ctx = uni.createCanvasContext('signatureCanvas')
  ctx.beginPath()
  ctx.moveTo(x, y)
}

const draw = (e) => {
  if (!isDrawing) return
  const { x, y } = e.touches[0]
  ctx.lineTo(x, y)
  ctx.stroke()
  ctx.draw(true)
  hasDrawn.value = true
}

const endDraw = () => {
  isDrawing = false
}

const clear = () => {
  ctx.clearRect(0, 0, 400, 300)
  ctx.draw()
  hasDrawn.value = false
}

const save = () => {
  uni.canvasToTempFilePath({
    canvasId: 'signatureCanvas',
    success: (res) => {
      uni.showToast({ title: '签名已保存', icon: 'success' })
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
  background: linear-gradient(135deg, #8B5CF6, #7C3AED);
  .header-left { width: 40px; height: 40px; display: flex; align-items: center; justify-content: center; }
  .header-left text { font-size: 32px; color: #fff; }
  .header-title { font-size: 20px; font-weight: 600; color: #fff; }
}
.signature-area { margin: 20px; background: #fff; border-radius: 16px; height: 300px; position: relative; overflow: hidden; }
.signature-canvas { width: 100%; height: 100%; }
.signature-hint { position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%); }
.signature-hint text { font-size: 16px; color: #9CA3AF; }
.signature-actions { display: flex; gap: 16px; padding: 0 20px; }
.action-btn { flex: 1; height: 50px; background: #F3F4F6; border-radius: 25px; display: flex; align-items: center; justify-content: center; font-size: 16px; color: #6B7280; }
.action-btn.primary { background: linear-gradient(135deg, #8B5CF6, #7C3AED); color: #fff; }
</style>
