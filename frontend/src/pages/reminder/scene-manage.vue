<template>
  <view class="scene-manage-page">
    <!-- 顶部渐变背景 -->
    <view class="header-gradient">
      <view class="header-content">
        <text class="header-icon">🎯</text>
        <text class="header-title">智能场景提醒</text>
        <text class="header-subtitle">开启智能助手，让生活更健康</text>
      </view>
    </view>
    
    <!-- 场景列表 -->
    <view class="scene-list">
      <view 
        v-for="(scene, index) in scenes" 
        :key="scene.sceneType"
        class="scene-card"
        :class="{ 'active': scene.enabled, 'expanded': scene.expanded }"
        :style="{ animationDelay: `${index * 0.1}s` }"
      >
        <!-- 场景头部 -->
        <view class="scene-header" @click="toggleScene(scene)">
          <view class="scene-icon-wrapper" :style="{ background: scene.bgColor }">
            <text class="scene-icon">{{ scene.icon }}</text>
          </view>
          <view class="scene-info">
            <text class="scene-name">{{ scene.name }}</text>
            <text class="scene-desc">{{ scene.description }}</text>
          </view>
          <view class="scene-toggle">
            <view class="toggle-track" :class="{ 'on': scene.enabled }">
              <view class="toggle-thumb" :class="{ 'on': scene.enabled }"></view>
            </view>
          </view>
        </view>
        
        <!-- 展开的配置区域 -->
        <view v-if="scene.expanded" class="scene-config">
          <view class="config-divider"></view>
          
          <!-- 喝水提醒配置 -->
          <template v-if="scene.sceneType === 'WATER'">
            <view class="config-section">
              <text class="config-section-title">💧 喝水目标</text>
              <view class="config-item">
                <text class="config-label">每日目标杯数</text>
                <view class="slider-wrapper">
                  <slider 
                    :value="scene.config.targetTimes" 
                    min="4" max="16" 
                    show-value
                    activeColor="#4facfe"
                    block-size="20"
                    @change="(e) => updateConfig(scene, 'targetTimes', e.detail.value)"
                  />
                </view>
              </view>
              <view class="config-item">
                <text class="config-label">每杯容量 (ml)</text>
                <view class="cup-size-options">
                  <view 
                    v-for="size in [150, 200, 250, 300]" 
                    :key="size"
                    class="size-option"
                    :class="{ 'active': scene.config.cupSize === size }"
                    @click="updateConfig(scene, 'cupSize', size)"
                  >
                    <text>{{ size }}ml</text>
                  </view>
                </view>
              </view>
            </view>
          </template>
          
          <!-- 下雨提醒配置 -->
          <template v-if="scene.sceneType === 'WEATHER_RAIN'">
            <view class="config-section">
              <text class="config-section-title">🌧️ 天气监控</text>
              <view class="config-item">
                <text class="config-label">监控位置</text>
                <picker mode="selector" :range="locationOptions" :value="0">
                  <view class="picker-value">
                    <text>{{ scene.config.location === 'auto' ? '📍 自动定位' : scene.config.location }}</text>
                    <text class="picker-arrow">›</text>
                  </view>
                </picker>
              </view>
              <view class="config-item">
                <text class="config-label">提醒选项</text>
                <view class="checkbox-list">
                  <label class="checkbox-item">
                    <checkbox :checked="scene.config.remindWhenStop" color="#667eea" />
                    <text>雨停后提醒收伞</text>
                  </label>
                </view>
              </view>
            </view>
          </template>
          
          <!-- 久坐提醒配置 -->
          <template v-if="scene.sceneType === 'SEDENTARY'">
            <view class="config-section">
              <text class="config-section-title">🪑 久坐监控</text>
              <view class="config-item">
                <text class="config-label">久坐时长 (分钟)</text>
                <view class="duration-options">
                  <view 
                    v-for="duration in [30, 45, 60, 90, 120]" 
                    :key="duration"
                    class="duration-option"
                    :class="{ 'active': scene.config.sitDuration === duration }"
                    @click="updateConfig(scene, 'sitDuration', duration)"
                  >
                    <text>{{ duration }}分</text>
                  </view>
                </view>
              </view>
              <view class="config-item">
                <text class="config-label">工作时间段</text>
                <view class="time-range">
                  <picker mode="time" :value="scene.config.workHours[0]">
                    <view class="time-value">{{ scene.config.workHours[0] }}</view>
                  </picker>
                  <text class="time-separator">至</text>
                  <picker mode="time" :value="scene.config.workHours[1]">
                    <view class="time-value">{{ scene.config.workHours[1] }}</view>
                  </picker>
                </view>
              </view>
            </view>
          </template>
          
          <!-- 护眼提醒配置 -->
          <template v-if="scene.sceneType === 'EYE_REST'">
            <view class="config-section">
              <text class="config-section-title">👁️ 用眼保护</text>
              <view class="config-item">
                <text class="config-label">连续用眼时长 (分钟)</text>
                <view class="duration-options">
                  <view 
                    v-for="duration in [20, 30, 45, 60]" 
                    :key="duration"
                    class="duration-option"
                    :class="{ 'active': scene.config.screenTime === duration }"
                    @click="updateConfig(scene, 'screenTime', duration)"
                  >
                    <text>{{ duration }}分</text>
                  </view>
                </view>
              </view>
              <view class="config-item">
                <text class="config-label">建议休息时长 (分钟)</text>
                <slider 
                  :value="scene.config.restDuration" 
                  min="3" max="30" 
                  show-value
                  activeColor="#30cfd0"
                  block-size="20"
                  @change="(e) => updateConfig(scene, 'restDuration', e.detail.value)"
                />
              </view>
            </view>
          </template>
          
          <!-- 温度提醒配置 -->
          <template v-if="scene.sceneType === 'WEATHER_TEMP'">
            <view class="config-section">
              <text class="config-section-title">🌡️ 温度监控</text>
              <view class="config-item">
                <text class="config-label">提醒类型</text>
                <view class="alert-type-options">
                  <view 
                    v-for="type in alertTypes" 
                    :key="type.value"
                    class="alert-type-option"
                    :class="{ 'active': scene.config.alertType === type.value }"
                    @click="updateConfig(scene, 'alertType', type.value)"
                  >
                    <text>{{ type.label }}</text>
                  </view>
                </view>
              </view>
              <view class="config-item">
                <text class="config-label">温度阈值 (°C)</text>
                <slider 
                  :value="scene.config.tempThreshold" 
                  min="-10" max="45" 
                  show-value
                  activeColor="#f5576c"
                  block-size="20"
                  @change="(e) => updateConfig(scene, 'tempThreshold', e.detail.value)"
                />
              </view>
            </view>
          </template>
          
          <!-- 保存按钮 -->
          <view class="config-actions">
            <button class="save-btn" @click="saveSceneConfig(scene)">
              <text class="save-icon">✓</text>
              <text>保存设置</text>
            </button>
          </view>
        </view>
        
        <!-- 展开/收起指示器 -->
        <view class="expand-indicator" @click="toggleExpand(scene)">
          <text class="expand-text">{{ scene.expanded ? '收起' : '配置' }}</text>
          <text class="expand-icon" :class="{ 'expanded': scene.expanded }">⌄</text>
        </view>
      </view>
    </view>
    
    <!-- 底部提示 -->
    <view class="footer-tips">
      <text class="tips-icon">💡</text>
      <text class="tips-text">开启场景提醒后，系统会在合适的时间自动提醒您</text>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { request } from '@/utils/request'

const scenes = ref([])
const loading = ref(false)

const locationOptions = ['自动定位', '北京', '上海', '广州', '深圳', '杭州', '成都']

const alertTypes = [
  { value: 'high', label: '高温提醒' },
  { value: 'low', label: '低温提醒' },
  { value: 'both', label: '高低温都提醒' }
]

// 加载场景列表
const loadScenes = async () => {
  loading.value = true
  try {
    const res = await request.get('/api/reminder/scene-templates')
    if (res) {
      scenes.value = res.map(scene => ({
        ...scene,
        expanded: false,
        config: getDefaultConfig(scene.sceneType)
      }))
    }
  } catch (e) {
    console.error('加载场景模板失败', e)
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

// 获取默认配置
const getDefaultConfig = (sceneType) => {
  const configs = {
    WATER: { targetTimes: 8, cupSize: 200, workHours: ['09:00', '18:00'] },
    WEATHER_RAIN: { location: 'auto', remindBeforeRain: 30, remindWhenStop: true },
    SEDENTARY: { sitDuration: 60, breakDuration: 5, workHours: ['09:00', '18:00'], postureTips: true },
    EYE_REST: { screenTime: 45, restDuration: 10, eyeExercise: true, blinkReminder: true },
    WEATHER_TEMP: { location: 'auto', tempThreshold: 35, alertType: 'high' }
  }
  return configs[sceneType] || {}
}

// 切换场景开关
const toggleScene = async (scene) => {
  try {
    const res = await request.post('/api/reminder/scene/toggle', {
      sceneType: scene.sceneType
    })
    
    if (res && res.code === 0) {
      scene.enabled = !scene.enabled
      scene.reminderId = res.data?.reminderId
      
      uni.showToast({
        title: scene.enabled ? '已开启' : '已关闭',
        icon: 'none'
      })
    } else {
      uni.showToast({ title: res?.message || '操作失败', icon: 'none' })
    }
  } catch (e) {
    console.error('切换场景失败', e)
    uni.showToast({ title: '操作失败', icon: 'none' })
  }
}

// 展开/收起配置
const toggleExpand = (scene) => {
  // 关闭其他展开的项
  scenes.value.forEach(s => {
    if (s.sceneType !== scene.sceneType) {
      s.expanded = false
    }
  })
  scene.expanded = !scene.expanded
}

// 更新配置
const updateConfig = (scene, key, value) => {
  scene.config[key] = value
}

// 保存配置
const saveSceneConfig = async (scene) => {
  try {
    const res = await request.post('/api/reminder/scene/update', {
      sceneType: scene.sceneType,
      businessData: scene.config
    })
    
    if (res && res.code === 0) {
      uni.showToast({ title: '保存成功', icon: 'success' })
    } else {
      uni.showToast({ title: res?.message || '保存失败', icon: 'none' })
    }
  } catch (e) {
    console.error('保存配置失败', e)
    uni.showToast({ title: '保存失败', icon: 'none' })
  }
}

onMounted(() => {
  loadScenes()
})

onShow(() => {
  loadScenes()
})
</script>

<style lang="scss" scoped>
.scene-manage-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #f8f9fc 0%, #f0f4f8 100%);
  padding-bottom: 40rpx;
}

// 顶部渐变背景
.header-gradient {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 60rpx 40rpx 100rpx;
  border-radius: 0 0 40rpx 40rpx;
  margin-bottom: -40rpx;
}

.header-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  color: #fff;
}

.header-icon {
  font-size: 80rpx;
  margin-bottom: 20rpx;
}

.header-title {
  font-size: 40rpx;
  font-weight: 700;
  margin-bottom: 12rpx;
}

.header-subtitle {
  font-size: 26rpx;
  opacity: 0.9;
}

// 场景列表
.scene-list {
  padding: 0 30rpx;
}

.scene-card {
  background: #fff;
  border-radius: 24rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.06);
  overflow: hidden;
  animation: slideIn 0.5s ease forwards;
  opacity: 0;
  transform: translateY(20rpx);
  
  &.active {
    box-shadow: 0 8rpx 32rpx rgba(102, 126, 234, 0.15);
  }
}

@keyframes slideIn {
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

// 场景头部
.scene-header {
  display: flex;
  align-items: center;
  padding: 30rpx;
  cursor: pointer;
}

.scene-icon-wrapper {
  width: 96rpx;
  height: 96rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 24rpx;
  flex-shrink: 0;
}

.scene-icon {
  font-size: 48rpx;
}

.scene-info {
  flex: 1;
  min-width: 0;
}

.scene-name {
  font-size: 32rpx;
  font-weight: 600;
  color: #333;
  margin-bottom: 8rpx;
  display: block;
}

.scene-desc {
  font-size: 24rpx;
  color: #999;
  display: block;
}

// 开关样式
.scene-toggle {
  margin-left: 20rpx;
}

.toggle-track {
  width: 96rpx;
  height: 52rpx;
  background: #e0e0e0;
  border-radius: 26rpx;
  position: relative;
  transition: all 0.3s ease;
  
  &.on {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  }
}

.toggle-thumb {
  width: 44rpx;
  height: 44rpx;
  background: #fff;
  border-radius: 50%;
  position: absolute;
  top: 4rpx;
  left: 4rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.15);
  transition: all 0.3s ease;
  
  &.on {
    transform: translateX(44rpx);
  }
}

// 配置区域
.scene-config {
  padding: 0 30rpx 30rpx;
}

.config-divider {
  height: 1rpx;
  background: linear-gradient(90deg, transparent, #e0e0e0, transparent);
  margin-bottom: 30rpx;
}

.config-section {
  margin-bottom: 30rpx;
}

.config-section-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #333;
  margin-bottom: 24rpx;
  display: block;
}

.config-item {
  margin-bottom: 24rpx;
}

.config-label {
  font-size: 26rpx;
  color: #666;
  margin-bottom: 16rpx;
  display: block;
}

// 滑块样式
.slider-wrapper {
  padding: 0 10rpx;
}

// 选项按钮组
.cup-size-options,
.duration-options,
.alert-type-options {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
}

.size-option,
.duration-option,
.alert-type-option {
  padding: 16rpx 32rpx;
  background: #f5f5f5;
  border-radius: 32rpx;
  border: 2rpx solid transparent;
  transition: all 0.3s ease;
  
  &.active {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    
    text {
      color: #fff;
    }
  }
  
  text {
    font-size: 26rpx;
    color: #666;
  }
}

// picker 样式
.picker-value {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 24rpx;
  background: #f8f9fc;
  border-radius: 12rpx;
  
  text {
    font-size: 28rpx;
    color: #333;
  }
}

.picker-arrow {
  color: #999;
  font-size: 32rpx;
}

// checkbox 样式
.checkbox-list {
  background: #f8f9fc;
  border-radius: 12rpx;
  padding: 20rpx 24rpx;
}

.checkbox-item {
  display: flex;
  align-items: center;
  
  text {
    font-size: 28rpx;
    color: #333;
    margin-left: 16rpx;
  }
}

// 时间范围选择
.time-range {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 20rpx;
}

.time-value {
  padding: 20rpx 40rpx;
  background: #f8f9fc;
  border-radius: 12rpx;
  font-size: 28rpx;
  color: #333;
}

.time-separator {
  font-size: 26rpx;
  color: #999;
}

// 保存按钮
.config-actions {
  margin-top: 30rpx;
}

.save-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  border-radius: 40rpx;
  padding: 24rpx 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  font-size: 30rpx;
  border: none;
  
  &:active {
    opacity: 0.9;
  }
}

.save-icon {
  font-size: 32rpx;
}

// 展开指示器
.expand-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20rpx;
  background: #f8f9fc;
  gap: 8rpx;
}

.expand-text {
  font-size: 24rpx;
  color: #667eea;
}

.expand-icon {
  font-size: 28rpx;
  color: #667eea;
  transition: transform 0.3s ease;
  
  &.expanded {
    transform: rotate(180deg);
  }
}

// 底部提示
.footer-tips {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  padding: 40rpx 30rpx;
  margin-top: 20rpx;
}

.tips-icon {
  font-size: 32rpx;
}

.tips-text {
  font-size: 24rpx;
  color: #999;
}
</style>
