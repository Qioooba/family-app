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
            <text class="scene-desc">{{ getSceneDescription(scene) }}</text>
          </view>
          <view class="scene-toggle">
            <view class="toggle-track" :class="{ 'on': scene.enabled }">
              <view class="toggle-thumb" :class="{ 'on': scene.enabled }"></view>
            </view>
          </view>
        </view>
        
        <!-- 展开的配置区域 -->
        <view v-if="scene.expanded" class="scene-config" @click.stop>
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
              <view class="config-item">
                <text class="config-label">工作时间段</text>
                <view class="time-range">
                  <picker mode="time" :value="scene.config.workHours[0]" @change="(e) => updateWorkHours(scene, 0, e.detail.value)">
                    <view class="time-value">{{ scene.config.workHours[0] }}</view>
                  </picker>
                  <text class="time-separator">至</text>
                  <picker mode="time" :value="scene.config.workHours[1]" @change="(e) => updateWorkHours(scene, 1, e.detail.value)">
                    <view class="time-value">{{ scene.config.workHours[1] }}</view>
                  </picker>
                </view>
              </view>
            </view>
          </template>
          
          <!-- 下雨提醒配置 -->
          <template v-if="scene.sceneType === 'WEATHER_RAIN'">
            <view class="config-section">
              <text class="config-section-title">🌧️ 下雨提醒</text>
              <view class="config-item">
                <text class="config-label">监测间隔</text>
                <view class="interval-options">
                  <view
                    v-for="option in intervalOptions"
                    :key="option.value"
                    class="interval-option"
                    :class="{ 'active': scene.config.intervalMinutes === option.value }"
                    @click="updateConfig(scene, 'intervalMinutes', option.value)"
                  >
                    <text>{{ option.label }}</text>
                  </view>
                </view>
              </view>
              <view class="config-item">
                <text class="config-label">降雨概率阈值</text>
                <view class="interval-options">
                  <view
                    v-for="threshold in rainProbabilityOptions"
                    :key="threshold"
                    class="interval-option"
                    :class="{ 'active': (scene.config.rainProbability || 40) === threshold }"
                    @click="updateConfig(scene, 'rainProbability', threshold)"
                  >
                    <text>{{ threshold }}%</text>
                  </view>
                </view>
                <text class="config-hint">超过此概率时提醒</text>
              </view>
              <view class="config-item">
                <text class="config-label">全天监测</text>
                <switch :checked="scene.config.allDay === true" @change="(e) => updateConfig(scene, 'allDay', e.detail.value)" />
                <text class="config-hint">{{ scene.config.allDay ? '24小时监测' : '仅工作时间内监测' }}</text>
              </view>
              <view v-if="scene.config.allDay !== true" class="config-item">
                <text class="config-label">监测时间段</text>
                <view class="time-range">
                  <picker mode="time" :value="scene.config.workHours[0]" @change="(e) => updateWorkHours(scene, 0, e.detail.value)">
                    <view class="time-value">{{ scene.config.workHours[0] }}</view>
                  </picker>
                  <text class="time-separator">至</text>
                  <picker mode="time" :value="scene.config.workHours[1]" @change="(e) => updateWorkHours(scene, 1, e.detail.value)">
                    <view class="time-value">{{ scene.config.workHours[1] }}</view>
                  </picker>
                </view>
                <text class="config-hint">建议设置在常用出行时段，避免夜间无意义监测</text>
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
                  <picker mode="time" :value="scene.config.workHours[0]" @change="(e) => updateWorkHours(scene, 0, e.detail.value)">
                    <view class="time-value">{{ scene.config.workHours[0] }}</view>
                  </picker>
                  <text class="time-separator">至</text>
                  <picker mode="time" :value="scene.config.workHours[1]" @change="(e) => updateWorkHours(scene, 1, e.detail.value)">
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
              <view class="config-item">
                <text class="config-label">工作时间段</text>
                <view class="time-range">
                  <picker mode="time" :value="scene.config.workHours[0]" @change="(e) => updateWorkHours(scene, 0, e.detail.value)">
                    <view class="time-value">{{ scene.config.workHours[0] }}</view>
                  </picker>
                  <text class="time-separator">至</text>
                  <picker mode="time" :value="scene.config.workHours[1]" @change="(e) => updateWorkHours(scene, 1, e.detail.value)">
                    <view class="time-value">{{ scene.config.workHours[1] }}</view>
                  </picker>
                </view>
              </view>
            </view>
          </template>
          
          <!-- 温度提醒配置 -->
          <template v-if="scene.sceneType === 'WEATHER_TEMP'">
            <view class="config-section">
              <text class="config-section-title">🌡️ 温度监控</text>
              <view class="config-item">
                <text class="config-label">监测间隔</text>
                <view class="interval-options">
                  <view
                    v-for="option in intervalOptions"
                    :key="option.value"
                    class="interval-option"
                    :class="{ 'active': scene.config.intervalMinutes === option.value }"
                    @click="updateConfig(scene, 'intervalMinutes', option.value)"
                  >
                    <text>{{ option.label }}</text>
                  </view>
                </view>
              </view>
              <view class="config-item">
                <text class="config-label">温度阈值</text>
                <view class="threshold-grid">
                  <view class="threshold-card">
                    <text class="threshold-card-label">高温提醒</text>
                    <input
                      class="threshold-input"
                      type="number"
                      :value="scene.config.highTempThreshold"
                      placeholder="如 35"
                      @input="(e) => updateConfig(scene, 'highTempThreshold', parseInt(e.detail.value) || 0)"
                    />
                    <text class="threshold-unit">达到或超过该温度提醒</text>
                  </view>
                  <view class="threshold-card low">
                    <text class="threshold-card-label">低温提醒</text>
                    <input
                      class="threshold-input"
                      type="number"
                      :value="scene.config.lowTempThreshold"
                      placeholder="如 5"
                      @input="(e) => updateConfig(scene, 'lowTempThreshold', parseInt(e.detail.value) || 0)"
                    />
                    <text class="threshold-unit">低于或等于该温度提醒</text>
                  </view>
                </view>
              </view>
              <view class="config-item">
                <text class="config-label">全天监测</text>
                <switch :checked="scene.config.allDay === true" @change="(e) => updateConfig(scene, 'allDay', e.detail.value)" />
                <text class="config-hint">{{ scene.config.allDay ? '24小时监测' : '仅工作时间内监测' }}</text>
              </view>
              <view v-if="scene.config.allDay !== true" class="config-item">
                <text class="config-label">监测时间段</text>
                <view class="time-range">
                  <picker mode="time" :value="scene.config.workHours[0]" @change="(e) => updateWorkHours(scene, 0, e.detail.value)">
                    <view class="time-value">{{ scene.config.workHours[0] }}</view>
                  </picker>
                  <text class="time-separator">至</text>
                  <picker mode="time" :value="scene.config.workHours[1]" @change="(e) => updateWorkHours(scene, 1, e.detail.value)">
                    <view class="time-value">{{ scene.config.workHours[1] }}</view>
                  </picker>
                </view>
              </view>
            </view>
          </template>

          <!-- 空气质量提醒配置 -->
          <template v-if="scene.sceneType === 'AIR_QUALITY'">
            <view class="config-section">
              <text class="config-section-title">🌫️ 空气质量监控</text>
              <view class="config-item">
                <text class="config-label">监测间隔</text>
                <view class="interval-options">
                  <view
                    v-for="option in intervalOptions"
                    :key="option.value"
                    class="interval-option"
                    :class="{ 'active': scene.config.intervalMinutes === option.value }"
                    @click="updateConfig(scene, 'intervalMinutes', option.value)"
                  >
                    <text>{{ option.label }}</text>
                  </view>
                </view>
              </view>
              <view class="config-item">
                <text class="config-label">PM2.5 阈值 (μg/m³)</text>
                <input
                  class="threshold-input"
                  type="number"
                  :value="scene.config.pm25Threshold || 75"
                  placeholder="请输入PM2.5阈值"
                  @input="(e) => updateConfig(scene, 'pm25Threshold', parseInt(e.detail.value) || 0)"
                />
                <text class="config-hint">超过此值提醒</text>
              </view>
              <view class="config-item">
                <text class="config-label">AQI 阈值</text>
                <input
                  class="threshold-input"
                  type="number"
                  :value="scene.config.aqiThreshold || 100"
                  placeholder="请输入AQI阈值"
                  @input="(e) => updateConfig(scene, 'aqiThreshold', parseInt(e.detail.value) || 0)"
                />
                <text class="config-hint">美国AQI指数，超过此值提醒</text>
              </view>
              <view class="config-item">
                <text class="config-label">全天监测</text>
                <switch :checked="scene.config.allDay === true" @change="(e) => updateConfig(scene, 'allDay', e.detail.value)" />
                <text class="config-hint">{{ scene.config.allDay ? '24小时监测' : '仅工作时间内监测' }}</text>
              </view>
              <view v-if="scene.config.allDay !== true" class="config-item">
                <text class="config-label">监测时间段</text>
                <view class="time-range">
                  <picker mode="time" :value="scene.config.workHours[0]" @change="(e) => updateWorkHours(scene, 0, e.detail.value)">
                    <view class="time-value">{{ scene.config.workHours[0] }}</view>
                  </picker>
                  <text class="time-separator">至</text>
                  <picker mode="time" :value="scene.config.workHours[1]" @change="(e) => updateWorkHours(scene, 1, e.detail.value)">
                    <view class="time-value">{{ scene.config.workHours[1] }}</view>
                  </picker>
                </view>
              </view>
            </view>
          </template>

          <!-- 紫外线提醒配置 -->
          <template v-if="scene.sceneType === 'UV_INDEX'">
            <view class="config-section">
              <text class="config-section-title">☀️ 紫外线监控</text>
              <view class="config-item">
                <text class="config-label">监测间隔</text>
                <view class="interval-options">
                  <view
                    v-for="option in intervalOptions"
                    :key="option.value"
                    class="interval-option"
                    :class="{ 'active': scene.config.intervalMinutes === option.value }"
                    @click="updateConfig(scene, 'intervalMinutes', option.value)"
                  >
                    <text>{{ option.label }}</text>
                  </view>
                </view>
              </view>
              <view class="config-item">
                <text class="config-label">紫外线指数阈值</text>
                <input
                  class="threshold-input"
                  type="number"
                  :value="scene.config.uvThreshold || 3"
                  placeholder="请输入紫外线指数"
                  @input="(e) => updateConfig(scene, 'uvThreshold', parseInt(e.detail.value) || 0)"
                />
                <text class="config-hint">UV≥3需要防晒，建议设置为3-5</text>
              </view>
              <view class="config-item">
                <text class="config-label">全天监测</text>
                <switch :checked="scene.config.allDay === true" @change="(e) => updateConfig(scene, 'allDay', e.detail.value)" />
                <text class="config-hint">{{ scene.config.allDay ? '24小时监测' : '仅工作时间内监测' }}</text>
              </view>
              <view v-if="scene.config.allDay !== true" class="config-item">
                <text class="config-label">监测时间段</text>
                <view class="time-range">
                  <picker mode="time" :value="scene.config.workHours[0]" @change="(e) => updateWorkHours(scene, 0, e.detail.value)">
                    <view class="time-value">{{ scene.config.workHours[0] }}</view>
                  </picker>
                  <text class="time-separator">至</text>
                  <picker mode="time" :value="scene.config.workHours[1]" @change="(e) => updateWorkHours(scene, 1, e.detail.value)">
                    <view class="time-value">{{ scene.config.workHours[1] }}</view>
                  </picker>
                </view>
              </view>
            </view>
          </template>

          <!-- 早安晚安提醒配置 -->
          <template v-if="scene.sceneType === 'MORNING'">
            <view class="config-section">
              <text class="config-section-title">🌅 早安晚安</text>
              <view class="config-item">
                <text class="config-label">提醒类型</text>
                <view class="type-selector">
                  <view class="type-option" :class="{ 'active': scene.config.type === 'morning' }" @click="updateConfig(scene, 'type', 'morning')"><text>☀️ 早安</text></view>
                  <view class="type-option" :class="{ 'active': scene.config.type === 'evening' }" @click="updateConfig(scene, 'type', 'evening')"><text>🌙 晚安</text></view>
                </view>
              </view>
              <view class="config-item"><text class="config-label">提醒时间</text><picker mode="time" :value="scene.config.reminderTime || '07:00'" @change="(e) => updateConfig(scene, 'reminderTime', e.detail.value)"><view class="picker"><text>{{ scene.config.reminderTime || '07:00' }}</text></view></picker></view>
            </view>
          </template>

          <!-- 签到提醒配置 -->
          <template v-if="scene.sceneType === 'CHECKIN'">
            <view class="config-section"><text class="config-section-title">✅ 每日签到</text><view class="config-item"><text class="config-label">签到提醒时间</text><picker mode="time" :value="scene.config.reminderTime || '08:00'" @change="(e) => updateConfig(scene, 'reminderTime', e.detail.value)"><view class="picker"><text>{{ scene.config.reminderTime || '08:00' }}</text></view></picker></view></view>
          </template>

          <!-- 今日日程提醒配置 -->
          <template v-if="scene.sceneType === 'SCHEDULE'">
            <view class="config-section"><text class="config-section-title">📅 今日日程</text><view class="config-item"><text class="config-label">日程提醒时间</text><picker mode="time" :value="scene.config.reminderTime || '07:30'" @change="(e) => updateConfig(scene, 'reminderTime', e.detail.value)"><view class="picker"><text>{{ scene.config.reminderTime || '07:30' }}</text></view></picker></view></view>
          </template>

          <!-- 早睡提醒配置 -->
          <template v-if="scene.sceneType === 'SLEEP'">
            <view class="config-section"><text class="config-section-title">🌙 早睡提醒</text><view class="config-item"><text class="config-label">提醒时间</text><picker mode="time" :value="scene.config.reminderTime || '22:00'" @change="(e) => updateConfig(scene, 'reminderTime', e.detail.value)"><view class="picker"><text>{{ scene.config.reminderTime || '22:00' }}</text></view></picker></view></view>
          </template>

          <!-- 纪念日提醒配置 -->
          <template v-if="scene.sceneType === 'ANNIVERSARY'">
            <view class="config-section">
              <text class="config-section-title">🎂 纪念日提醒</text>
              <view class="config-item">
                <text class="config-label">提醒时间</text>
                <picker mode="time" :value="scene.config.reminderTime || '09:00'" @change="(e) => updateConfig(scene, 'reminderTime', e.detail.value)">
                  <view class="picker"><text>{{ scene.config.reminderTime || '09:00' }}</text></view>
                </picker>
              </view>
              <view class="config-item">
                <text class="config-label">提前提醒天数</text>
                <picker
                  mode="selector"
                  :range="anniversaryAdvanceDayOptionLabels"
                  :value="getAnniversaryAdvanceDayIndex(scene.config.advanceDays)"
                  @change="(e) => updateAnniversaryAdvanceDays(scene, e.detail.value)"
                >
                  <view class="picker-value">
                    <text>{{ getAnniversaryAdvanceDayLabel(scene.config.advanceDays) }}</text>
                    <text class="picker-arrow">›</text>
                  </view>
                </picker>
                <text class="config-hint">会从设定天数开始，一直到纪念日当天每天提醒一次</text>
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
        <view class="expand-indicator" @click.stop="toggleExpand(scene)">
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

// 监测间隔选项（分钟）
const intervalOptions = [
  { value: 20, label: '20分钟' },
  { value: 40, label: '40分钟' },
  { value: 60, label: '1小时' },
  { value: 120, label: '2小时' },
  { value: 180, label: '3小时' },
  { value: 360, label: '6小时' },
  { value: 720, label: '12小时' }
]

const rainProbabilityOptions = [20, 30, 40, 50, 60, 70, 80]

const anniversaryAdvanceDayOptions = [
  { value: 0, label: '仅当天提醒' },
  { value: 1, label: '提前1天' },
  { value: 3, label: '提前3天' },
  { value: 7, label: '提前7天' },
  { value: 15, label: '提前15天' },
  { value: 30, label: '提前30天' }
]

const anniversaryAdvanceDayOptionLabels = anniversaryAdvanceDayOptions.map(option => option.label)

const normalizeConfig = (sceneType, rawConfig) => {
  const fallback = { ...getDefaultConfig(sceneType) }
  if (!rawConfig) {
    return fallback
  }

  let parsedConfig = rawConfig
  if (typeof rawConfig === 'string') {
    try {
      parsedConfig = JSON.parse(rawConfig)
    } catch (e) {
      console.warn('[SceneManage] 解析场景配置失败，使用默认配置:', sceneType, rawConfig)
      return fallback
    }
  }

  if (typeof parsedConfig !== 'object' || Array.isArray(parsedConfig)) {
    return fallback
  }

  const normalized = {
    ...fallback,
    ...parsedConfig
  }

  if (['WEATHER_RAIN', 'WEATHER_TEMP', 'AIR_QUALITY', 'UV_INDEX'].includes(sceneType)) {
    if (!Array.isArray(normalized.workHours) || normalized.workHours.length !== 2) {
      normalized.workHours = [...fallback.workHours]
    }
  }

  if (sceneType === 'WEATHER_TEMP') {
    const legacyThreshold = Number.isFinite(Number(parsedConfig.tempThreshold))
      ? Number(parsedConfig.tempThreshold)
      : null
    const legacyAlertType = parsedConfig.alertType

    if (!Number.isFinite(Number(normalized.highTempThreshold))) {
      normalized.highTempThreshold = legacyAlertType === 'low'
        ? fallback.highTempThreshold
        : (legacyThreshold ?? fallback.highTempThreshold)
    }

    if (!Number.isFinite(Number(normalized.lowTempThreshold))) {
      normalized.lowTempThreshold = legacyAlertType === 'low'
        ? (legacyThreshold ?? fallback.lowTempThreshold)
        : fallback.lowTempThreshold
    }
  }

  return normalized
}

// 加载场景列表
const loadScenes = async () => {
  loading.value = true
  try {
    const res = await request.get('/api/reminder/scene-templates')
    if (res) {
      scenes.value = res.map(scene => ({
        ...scene,
        expanded: false,
        // 如果API返回了实际配置(defaultConfig)，使用它；否则使用默认配置
        config: normalizeConfig(scene.sceneType, scene.defaultConfig || scene.businessData)
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
    WEATHER_RAIN: { location: 'auto', intervalMinutes: 40, rainProbability: 40, rainHoursAhead: 3, allDay: false, workHours: ['07:00', '22:00'] },
    SEDENTARY: { sitDuration: 60, breakDuration: 5, workHours: ['09:00', '18:00'], postureTips: true },
    EYE_REST: { screenTime: 45, restDuration: 10, eyeExercise: true, blinkReminder: true, workHours: ['09:00', '18:00'] },
    WEATHER_TEMP: { location: 'auto', intervalMinutes: 120, highTempThreshold: 35, lowTempThreshold: 5, allDay: false, workHours: ['07:00', '22:00'] },
    AIR_QUALITY: { location: 'auto', intervalMinutes: 120, pm25Threshold: 75, pm10Threshold: 150, aqiThreshold: 100, allDay: false, workHours: ['06:00', '22:00'] },
    UV_INDEX: { location: 'auto', intervalMinutes: 120, uvThreshold: 3, allDay: false, workHours: ['08:00', '18:00'] },
    MORNING: { type: 'morning', location: 'auto', reminderTime: '07:00' },
    CHECKIN: { reminderTime: '08:00' },
    SCHEDULE: { reminderTime: '07:30' },
    SLEEP: { reminderTime: '22:00' },
    ANNIVERSARY: { reminderTime: '09:00', advanceDays: 3 }
  }
  return configs[sceneType] || {}
}

// 切换场景开关
const toggleScene = async (scene) => {
  try {
    const res = await request.post('/api/reminder/scene/toggle', {
      sceneType: scene.sceneType
    })
    
    // request.js 已提取 data 字段，res 直接包含 {enabled, reminderId}
    if (res && typeof res.enabled === 'boolean') {
      scene.enabled = res.enabled
      scene.reminderId = res.reminderId
      
      uni.showToast({
        title: scene.enabled ? '已开启' : '已关闭',
        icon: 'none'
      })
    } else {
      uni.showToast({ title: '操作失败', icon: 'none' })
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
  scene.config = normalizeConfig(scene.sceneType, scene.config)
  scene.config[key] = value
  if (key === 'allDay' && value !== true && (!Array.isArray(scene.config.workHours) || scene.config.workHours.length !== 2)) {
    scene.config.workHours = [...getDefaultConfig(scene.sceneType).workHours]
  }
}

const getAnniversaryAdvanceDayIndex = (advanceDays) => {
  const targetValue = Number.isFinite(Number(advanceDays)) ? Number(advanceDays) : 3
  const index = anniversaryAdvanceDayOptions.findIndex(option => option.value === targetValue)
  return index >= 0 ? index : 2
}

const getAnniversaryAdvanceDayLabel = (advanceDays) => {
  return anniversaryAdvanceDayOptions[getAnniversaryAdvanceDayIndex(advanceDays)]?.label || '提前3天'
}

const updateAnniversaryAdvanceDays = (scene, selectedIndex) => {
  const option = anniversaryAdvanceDayOptions[Number(selectedIndex)]
  if (!option) {
    return
  }
  updateConfig(scene, 'advanceDays', option.value)
}

const getIntervalLabel = (intervalMinutes, fallback = '2小时') => {
  return intervalOptions.find(option => option.value === intervalMinutes)?.label || fallback
}

const formatWorkHourLabel = (workHours, fallback = '白天时段') => {
  if (Array.isArray(workHours) && workHours.length === 2) {
    return `${workHours[0]}-${workHours[1]}`
  }
  return fallback
}

const getSceneDescription = (scene) => {
  const config = normalizeConfig(scene.sceneType, scene.config || scene.defaultConfig || scene.businessData)

  switch (scene.sceneType) {
    case 'WATER':
      return `${formatWorkHourLabel(config.workHours, '白天时段')}内按节奏提醒喝水`
    case 'WEATHER_RAIN':
      return `${config.allDay ? '全天' : formatWorkHourLabel(config.workHours, '07:00-22:00')}内每${getIntervalLabel(config.intervalMinutes)}监测降雨`
    case 'SEDENTARY':
      return `${formatWorkHourLabel(config.workHours, '工作时间')}内久坐${config.sitDuration || 60}分钟提醒活动`
    case 'EYE_REST':
      return `${formatWorkHourLabel(config.workHours, '工作时间')}内用眼${config.screenTime || 45}分钟提醒休息`
    case 'WEATHER_TEMP':
      return `${config.allDay ? '全天' : formatWorkHourLabel(config.workHours, '07:00-22:00')}内监测温度变化`
    case 'AIR_QUALITY':
      return `${config.allDay ? '全天' : formatWorkHourLabel(config.workHours, '06:00-22:00')}内每${getIntervalLabel(config.intervalMinutes)}监测空气质量`
    case 'UV_INDEX':
      return `${config.allDay ? '全天' : formatWorkHourLabel(config.workHours, '08:00-18:00')}内监测紫外线指数`
    case 'MORNING':
      return `${config.type === 'evening' ? '每天' : '每天'}${config.reminderTime || '07:00'}发送${config.type === 'evening' ? '晚安问候' : '早安问候'}`
    case 'CHECKIN':
      return `每天${config.reminderTime || '08:00'}提醒签到打卡`
    case 'SCHEDULE':
      return `每天${config.reminderTime || '07:30'}提醒查看今日日程`
    case 'SLEEP':
      return `每天${config.reminderTime || '22:00'}提醒准备休息`
    case 'ANNIVERSARY':
      return `${getAnniversaryAdvanceDayLabel(config.advanceDays)}开始，到当天每天提醒一次`
    default:
      return scene.description || '按当前配置自动提醒'
  }
}

// 更新工作时间段
const updateWorkHours = (scene, index, value) => {
  scene.config = normalizeConfig(scene.sceneType, scene.config)
  scene.config.workHours[index] = value
}

// 处理位置输入
const handleLocationInput = (scene, value) => {
  scene.config = normalizeConfig(scene.sceneType, scene.config)
  if (value === '自动定位' || value === 'auto') {
    scene.config.location = 'auto'
  } else {
    scene.config.location = value
  }
}

// 设置自动定位
const setAutoLocation = (scene) => {
  scene.config = normalizeConfig(scene.sceneType, scene.config)
  scene.config.location = 'auto'
  uni.showToast({ title: '已设置为自动定位', icon: 'none' })
}

// 保存配置
const saveSceneConfig = async (scene) => {
  try {
    scene.config = normalizeConfig(scene.sceneType, scene.config)
    if (scene.sceneType === 'WEATHER_TEMP') {
      delete scene.config.alertType
      delete scene.config.tempThreshold
    }
    await request.post('/api/reminder/scene/update', {
      sceneType: scene.sceneType,
      businessData: scene.config
    })

    uni.showToast({ title: '保存成功', icon: 'success' })
  } catch (e) {
    console.error('保存配置失败', e)
    uni.showToast({ title: e.message || '保存失败', icon: 'none' })
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
  display: flex;
  align-items: center;
  flex-wrap: wrap;
}

.config-item .config-label {
  width: 100%;
}

.config-label {
  font-size: 26rpx;
  color: #666;
  margin-bottom: 16rpx;
  display: block;
}

.config-hint {
  font-size: 22rpx;
  color: #999;
  margin-top: 8rpx;
  display: block;
}

.config-input {
  border: 1rpx solid #ddd;
  border-radius: 8rpx;
  padding: 16rpx 20rpx;
  font-size: 26rpx;
  background: #fff;
  flex: 1;
}

.threshold-input {
  border: 1rpx solid #ddd;
  border-radius: 8rpx;
  padding: 16rpx 20rpx;
  font-size: 28rpx;
  background: #fff;
  width: 200rpx;
  text-align: center;
}

.threshold-grid {
  width: 100%;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16rpx;
}

.threshold-card {
  background: linear-gradient(180deg, #f8fbff 0%, #f1f6ff 100%);
  border: 1rpx solid #dbeafe;
  border-radius: 20rpx;
  padding: 20rpx;
  display: flex;
  flex-direction: column;
  gap: 12rpx;

  &.low {
    background: linear-gradient(180deg, #fbfdff 0%, #f4f7fb 100%);
    border-color: #dbe4f0;
  }

  .threshold-input {
    width: 100%;
    box-sizing: border-box;
  }
}

.threshold-card-label {
  font-size: 24rpx;
  color: #334155;
  font-weight: 600;
}

.threshold-unit {
  font-size: 22rpx;
  color: #64748b;
  line-height: 1.5;
}

.location-auto-btn {
  background: #667eea;
  color: #fff;
  padding: 12rpx 20rpx;
  border-radius: 8rpx;
  font-size: 24rpx;
  margin-left: 16rpx;
}

// 滑块样式
.slider-wrapper {
  padding: 0 10rpx;
}

// 选项按钮组
.cup-size-options,
.duration-options,
.alert-type-options,
.interval-options {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
}

.size-option,
.duration-option,
.alert-type-option,
.interval-option {
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
