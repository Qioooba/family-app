<template>
  <view class="changelog-page">
    <!-- 顶部装饰背景 -->
    <view class="header-bg">
      <view class="gradient-orb orb-1"></view>
      <view class="gradient-orb orb-2"></view>
      <view class="gradient-orb orb-3"></view>
    </view>
    
    <!-- 导航栏 -->
    <view class="nav-bar">
      <view class="back-btn" @click="goBack">
        <u-icon name="arrow-left" size="40" color="#fff"></u-icon>
      </view>
      <text class="title">版本演进</text>
      <view class="menu-btn" @click="showVersionMenu">
        <u-icon name="more-dot-fill" size="40" color="#fff"></u-icon>
      </view>
    </view>
    
    <!-- 当前版本卡片 -->
    <view class="version-hero">
      <view class="version-badge">
        <text class="version-icon">🚀</text>
        <view class="version-pulse"></view>
      </view>
      <text class="current-version-num">v{{ currentVersion }}</text>
      <text class="version-status">{{ updateStatus }}</text>
      <view class="version-meta">
        <view class="meta-item">
          <text class="meta-icon">📅</text>
          <text class="meta-text">{{ currentDate }}</text>
        </view>
        <view class="meta-item">
          <text class="meta-icon">🏗️</text>
          <text class="meta-text">Build {{ buildNumber }}</text>
        </view>
      </view>
      
      <!-- 检查更新按钮 -->
      <view class="check-update-btn" :class="{ checking: isChecking }" @click="checkForUpdate">
        <text class="btn-icon">{{ isChecking ? '⏳' : '🔍' }}</text>
        <text class="btn-text">{{ isChecking ? '检查中...' : '检查更新' }}</text>
      </view>
    </view>
    
    <!-- 统计信息 -->
    <view class="stats-section">
      <view class="stats-card">
        <view class="stat-item">
          <text class="stat-num">{{ versionCount }}</text>
          <text class="stat-label">版本迭代</text>
        </view>
        <view class="stat-divider"></view>
        <view class="stat-item">
          <text class="stat-num">{{ featureCount }}</text>
          <text class="stat-label">功能特性</text>
        </view>
        <view class="stat-divider"></view>
        <view class="stat-item">
          <text class="stat-num">{{ fixCount }}</text>
          <text class="stat-label">问题修复</text>
        </view>
      </view>
    </view>
    
    <!-- 时间轴版本列表 -->
    <view class="timeline-section">
      <view class="section-title">
        <text class="title-icon">📜</text>
        <text class="title-text">演进历程</text>
      </view>
      
      <view class="timeline">
        <view 
          v-for="(version, index) in versions" 
          :key="version.version"
          class="timeline-item"
          :class="{ expanded: expandedVersion === version.version, latest: index === 0 }"
          @click="toggleExpand(version.version)"
        >
          <!-- 时间轴线 -->
          <view class="timeline-line">
            <view class="timeline-dot" :class="{ 'dot-latest': index === 0, 'dot-normal': index > 0 }">
              <text v-if="index === 0" class="dot-icon">⭐</text>
            </view>
            <view v-if="index < versions.length - 1" class="timeline-connector"></view>
          </view>
          
          <!-- 版本卡片 -->
          <view class="version-card">
            <view class="version-header">
              <view class="version-badge-row">
                <text class="version-name">{{ version.version }}</text>
                <view v-if="index === 0" class="latest-tag">最新</view>
                <view v-if="version.type === 'major'" class="type-tag major">重大</view>
                <view v-if="version.type === 'feature'" class="type-tag feature">功能</view>
              </view>
              <text class="version-date">{{ version.date }}</text>
            </view>
            
            <view class="version-slogan" v-if="version.slogan">
              <text class="slogan-text">"{{ version.slogan }}"</text>
            </view>
            
            <view class="version-content" :class="{ collapsed: expandedVersion !== version.version }">
              <!-- 新增功能 -->
              <view v-if="version.features?.length" class="change-group">
                <view class="group-header">
                  <text class="group-icon">✨</text>
                  <text class="group-title">新增功能</text>
                  <text class="group-count">{{ version.features.length }}</text>
                </view>
                <view class="change-list">
                  <view v-for="(feature, fIndex) in version.features" :key="fIndex" class="change-row">
                    <text class="change-bullet">▸</text>
                    <text class="change-text">{{ feature }}</text>
                  </view>
                </view>
              </view>
              
              <!-- 优化改进 -->
              <view v-if="version.optimizations?.length" class="change-group">
                <view class="group-header">
                  <text class="group-icon">🚀</text>
                  <text class="group-title">优化改进</text>
                  <text class="group-count">{{ version.optimizations.length }}</text>
                </view>
                <view class="change-list">
                  <view v-for="(opt, oIndex) in version.optimizations" :key="oIndex" class="change-row">
                    <text class="change-bullet">▸</text>
                    <text class="change-text">{{ opt }}</text>
                  </view>
                </view>
              </view>
              
              <!-- 问题修复 -->
              <view v-if="version.fixes?.length" class="change-group">
                <view class="group-header">
                  <text class="group-icon">🔧</text>
                  <text class="group-title">问题修复</text>
                  <text class="group-count">{{ version.fixes.length }}</text>
                </view>
                <view class="change-list">
                  <view v-for="(fix, xIndex) in version.fixes" :key="xIndex" class="change-row">
                    <text class="change-bullet">▸</text>
                    <text class="change-text">{{ fix }}</text>
                  </view>
                </view>
              </view>
            </view>
            
            <!-- 展开/收起提示 -->
            <view class="expand-hint" v-if="hasMoreContent(version)">
              <text class="hint-text">{{ expandedVersion === version.version ? '收起详情' : '展开详情' }}</text>
              <text class="hint-icon" :class="{ rotated: expandedVersion === version.version }">▼</text>
            </view>
          </view>
        </view>
      </view>
    </view>
    
    <!-- 底部信息 -->
    <view class="footer-section">
      <view class="footer-card">
        <text class="footer-title">🏠 家庭智能管理平台</text>
        <text class="footer-desc">让家庭生活更有序、更温馨、更有趣</text>
        <view class="footer-links">
          <text class="footer-link" @click="showPrivacy">隐私政策</text>
          <text class="footer-divider">|</text>
          <text class="footer-link" @click="showTerms">用户协议</text>
          <text class="footer-divider">|</text>
          <text class="footer-link" @click="contactDev">联系开发者</text>
        </view>
        <text class="copyright">© 2026 Family App. All rights reserved.</text>
      </view>
    </view>
    
    <!-- 更新弹窗 -->
    <view class="update-modal" v-if="showUpdateModal" @click="closeUpdateModal">
      <view class="modal-content" @click.stop>
        <view class="modal-header">
          <text class="modal-icon">🎉</text>
          <text class="modal-title">发现新版本</text>
        </view>
        <view class="modal-body">
          <text class="modal-version">{{ newVersion }}</text>
          <text class="modal-desc">新版本已准备好，立即更新体验最新功能！</text>
        </view>
        <view class="modal-footer">
          <view class="modal-btn secondary" @click="closeUpdateModal">稍后</view>
          <view class="modal-btn primary" @click="doUpdate">立即更新</view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'

const currentVersion = ref('1.1.1')
const currentDate = ref('2026-03-17')
const buildNumber = ref('20260317.1')
const updateStatus = ref('已是最新版本')
const isChecking = ref(false)
const showUpdateModal = ref(false)
const newVersion = ref('')
const expandedVersion = ref('1.0.3')

// 版本数据
const versions = ref([
  {
    version: 'v1.1.1',
    date: '2026-03-17',
    type: 'major',
    slogan: '智能提醒，贴心守护',
    features: [
      '全新定时提醒系统 - 支持每天/每小时/每周/每月/每年/一次性等多种频率',
      '提醒推送范围设置 - 支持推送给所有人、仅自己或指定成员',
      '智能安静时段 - 支持设置免打扰时间段，深夜不打扰',
      '节假日智能跳过 - 可选择节假日不发送提醒',
      '雨天预警提醒 - 基于实时天气数据，自动推送带伞提醒',
      '提醒模板变量 - 支持日期、天气等动态变量',
      '分布式锁机制 - 防止定时任务重复执行，确保推送准确性',
      '任务分配优化 - 成员选择器样式优化，选中状态更清晰'
    ],
    optimizations: [
      '天气地址显示优化 - 长地址自动缩小字号，完整展示不截断',
      '小时级天气预报 - 仅展示当前时间之后的预报数据',
      '首页提醒/任务区域 - 支持折叠展开，页面更简洁',
      'UI配色统一优化 - 心愿图标改为粉色礼盒，提醒按钮改为紫色',
      'API响应处理标准化 - 统一前后端数据交互格式'
    ],
    fixes: [
      '修复提醒功能SQL条件问题 - 修复AND/OR优先级导致的查询错误',
      '修复Cron表达式计算 - 使用Spring CronExpression准确计算下次执行时间',
      '修复日期解析问题 - 正确处理ONCE和YEARLY类型的日期配置',
      '修复提醒类型默认值 - 解决reminderType为空的问题',
      '修复任务成员选择器布局 - 选中图标正常显示'
    ]
  },
  {
    version: 'v1.0.3',
    date: '2026-03-13',
    type: 'feature',
    slogan: '智能进化，体验升级',
    features: [
      '版本自动更新检测功能 - 自动发现新版本并提醒',
      '生产环境vConsole调试支持 - 线上问题快速定位',
      '强制刷新机制 - 确保用户始终使用最新版本'
    ],
    optimizations: [
      '优化缓存清理策略 - 启动时自动清理过期数据',
      '提升版本检测可靠性 - 多层级版本号对比'
    ],
    fixes: [
      '修复头像显示异常问题 - 解决部分用户头像加载失败',
      '修复待办列表头像显示格式 - 统一头像展示样式'
    ]
  },
  {
    version: 'v1.0.2',
    date: '2026-03-12',
    type: 'feature',
    slogan: '连接企业，高效协作',
    features: [
      '企业微信推送免密登录 - 企业内部用户一键登录',
      '待办列表双头像显示 - 清晰展示创建者与执行者'
    ],
    optimizations: [
      '优化任务卡片信息密度 - 显示更多关键信息'
    ],
    fixes: [
      '修复心愿墙日期显示问题 - 解决农历日期计算错误'
    ]
  },
  {
    version: 'v1.0.1',
    date: '2026-03-11',
    type: 'feature',
    slogan: '消息触达，关怀到位',
    features: [
      '企业微信推送通知功能 - 任务提醒直达企业微信',
      '喝水打卡125ml和175ml档位 - 更灵活的喝水记录'
    ],
    optimizations: [
      '优化推送消息格式 - 更清晰的信息展示'
    ],
    fixes: []
  },
  {
    version: 'v1.0.0',
    date: '2026-03-01',
    type: 'major',
    slogan: '智慧家庭，从此开始',
    features: [
      '家庭小程序正式上线 - 完整的家庭管理平台',
      '任务管理系统 - 创建、分配、追踪家庭任务',
      '纪念日管理 - 农历/公历支持，自动提醒',
      '心愿墙功能 - 记录家庭愿望，共同实现',
      '喝水打卡 - 健康习惯养成',
      '家庭游戏中心 - 10+款家庭互动小游戏'
    ],
    optimizations: [],
    fixes: []
  }
])

// 统计计算
const versionCount = computed(() => versions.value.length)
const featureCount = computed(() => {
  return versions.value.reduce((sum, v) => sum + (v.features?.length || 0), 0)
})
const fixCount = computed(() => {
  return versions.value.reduce((sum, v) => sum + (v.fixes?.length || 0), 0)
})

// 检查是否有更多内容
const hasMoreContent = (version) => {
  const total = (version.features?.length || 0) + 
                (version.optimizations?.length || 0) + 
                (version.fixes?.length || 0)
  return total > 3
}

// 展开/收起
const toggleExpand = (version) => {
  expandedVersion.value = expandedVersion.value === version ? null : version
}

// 返回
const goBack = () => {
  uni.navigateBack()
}

// 显示版本菜单
const showVersionMenu = () => {
  uni.showActionSheet({
    itemList: ['检查更新', '清除缓存', '版本说明'],
    success: (res) => {
      switch (res.tapIndex) {
        case 0:
          checkForUpdate()
          break
        case 1:
          clearCache()
          break
        case 2:
          showVersionInfo()
          break
      }
    }
  })
}

// 检查更新
const checkForUpdate = () => {
  if (isChecking.value) return
  
  isChecking.value = true
  updateStatus.value = '检查中...'
  
  // #ifdef MP-WEIXIN
  const updateManager = wx.getUpdateManager()
  
  updateManager.onCheckForUpdate((res) => {
    setTimeout(() => {
      isChecking.value = false
      if (res.hasUpdate) {
        updateStatus.value = '发现新版本'
        newVersion.value = 'v1.0.4' // 模拟新版本
        showUpdateModal.value = true
      } else {
        updateStatus.value = '已是最新版本'
        uni.showToast({
          title: '已是最新版本',
          icon: 'success'
        })
      }
    }, 1500)
  })
  // #endif
  
  // #ifndef MP-WEIXIN
  setTimeout(() => {
    isChecking.value = false
    updateStatus.value = '已是最新版本'
    uni.showToast({
      title: '已是最新版本',
      icon: 'success'
    })
  }, 1500)
  // #endif
}

// 执行更新
const doUpdate = () => {
  // #ifdef MP-WEIXIN
  const updateManager = wx.getUpdateManager()
  updateManager.onUpdateReady(() => {
    updateManager.applyUpdate()
  })
  // #endif
  closeUpdateModal()
}

// 关闭更新弹窗
const closeUpdateModal = () => {
  showUpdateModal.value = false
}

// 清除缓存
const clearCache = () => {
  uni.showModal({
    title: '清除缓存',
    content: '确定要清除本地缓存吗？清除后需要重新登录',
    success: (res) => {
      if (res.confirm) {
        uni.clearStorageSync()
        uni.showToast({
          title: '缓存已清除',
          icon: 'success'
        })
        setTimeout(() => {
          uni.reLaunch({ url: '/pages/login/index' })
        }, 1500)
      }
    }
  })
}

// 显示版本信息
const showVersionInfo = () => {
  uni.showModal({
    title: '版本说明',
    content: `当前版本：v${currentVersion.value}\n构建时间：${currentDate.value}\n构建号：${buildNumber.value}\n\n版本号规则：\n主版本.功能版本.修复版本`,
    showCancel: false
  })
}

// 显示隐私政策
const showPrivacy = () => {
  uni.navigateTo({ url: '/pages/profile/privacy' })
}

// 显示用户协议
const showTerms = () => {
  uni.showModal({
    title: '用户协议',
    content: '用户协议内容...',
    showCancel: false
  })
}

// 联系开发者
const contactDev = () => {
  uni.showModal({
    title: '联系开发者',
    content: '如有问题或建议，欢迎反馈！',
    confirmText: '复制邮箱',
    success: (res) => {
      if (res.confirm) {
        uni.setClipboardData({
          data: 'developer@familyapp.com',
          success: () => {
            uni.showToast({ title: '已复制' })
          }
        })
      }
    }
  })
}

onMounted(() => {
  // 默认展开最新版本
  expandedVersion.value = 'v1.0.3'
})
</script>

<style scoped lang="scss">
.changelog-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
  position: relative;
  overflow-x: hidden;
}

/* 顶部装饰背景 */
.header-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 600rpx;
  overflow: hidden;
  pointer-events: none;
}

.gradient-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80rpx);
  opacity: 0.6;
}

.orb-1 {
  width: 400rpx;
  height: 400rpx;
  background: radial-gradient(circle, #667eea 0%, transparent 70%);
  top: -100rpx;
  right: -100rpx;
}

.orb-2 {
  width: 300rpx;
  height: 300rpx;
  background: radial-gradient(circle, #f093fb 0%, transparent 70%);
  top: 100rpx;
  left: -50rpx;
}

.orb-3 {
  width: 250rpx;
  height: 250rpx;
  background: radial-gradient(circle, #4facfe 0%, transparent 70%);
  top: 200rpx;
  right: 100rpx;
}

/* 导航栏 */
.nav-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 80rpx 30rpx 30rpx;
  position: relative;
  z-index: 10;
  
  .back-btn, .menu-btn {
    width: 70rpx;
    height: 70rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    background: rgba(255,255,255,0.1);
    border-radius: 50%;
    backdrop-filter: blur(10rpx);
  }
  
  .title {
    font-size: 36rpx;
    font-weight: 600;
    color: #fff;
  }
}

/* 版本主卡片 */
.version-hero {
  margin: 0 30rpx 40rpx;
  padding: 60rpx 40rpx;
  background: linear-gradient(135deg, rgba(255,255,255,0.1) 0%, rgba(255,255,255,0.05) 100%);
  border-radius: 40rpx;
  border: 1rpx solid rgba(255,255,255,0.1);
  backdrop-filter: blur(20rpx);
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  z-index: 10;
}

.version-badge {
  position: relative;
  width: 140rpx;
  height: 140rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 30rpx;
  
  .version-icon {
    font-size: 80rpx;
    z-index: 2;
  }
  
  .version-pulse {
    position: absolute;
    width: 100%;
    height: 100%;
    border-radius: 50%;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    opacity: 0.3;
    animation: pulse 2s ease-in-out infinite;
  }
}

@keyframes pulse {
  0%, 100% { transform: scale(1); opacity: 0.3; }
  50% { transform: scale(1.1); opacity: 0.5; }
}

.current-version-num {
  font-size: 64rpx;
  font-weight: 800;
  color: #fff;
  background: linear-gradient(135deg, #fff 0%, #a8b2d1 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  margin-bottom: 10rpx;
}

.version-status {
  font-size: 28rpx;
  color: #52c41a;
  margin-bottom: 30rpx;
}

.version-meta {
  display: flex;
  gap: 40rpx;
  margin-bottom: 40rpx;
  
  .meta-item {
    display: flex;
    align-items: center;
    gap: 10rpx;
    background: rgba(255,255,255,0.1);
    padding: 12rpx 24rpx;
    border-radius: 30rpx;
    
    .meta-icon {
      font-size: 24rpx;
    }
    
    .meta-text {
      font-size: 24rpx;
      color: rgba(255,255,255,0.8);
    }
  }
}

.check-update-btn {
  display: flex;
  align-items: center;
  gap: 12rpx;
  padding: 24rpx 60rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50rpx;
  transition: all 0.3s ease;
  
  &.checking {
    opacity: 0.7;
  }
  
  .btn-icon {
    font-size: 32rpx;
  }
  
  .btn-text {
    font-size: 30rpx;
    font-weight: 600;
    color: #fff;
  }
}

/* 统计卡片 */
.stats-section {
  margin: 0 30rpx 40rpx;
}

.stats-card {
  display: flex;
  justify-content: space-around;
  align-items: center;
  padding: 40rpx;
  background: linear-gradient(135deg, rgba(255,255,255,0.1) 0%, rgba(255,255,255,0.05) 100%);
  border-radius: 30rpx;
  border: 1rpx solid rgba(255,255,255,0.1);
  backdrop-filter: blur(20rpx);
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  
  .stat-num {
    font-size: 48rpx;
    font-weight: 800;
    color: #fff;
    margin-bottom: 8rpx;
  }
  
  .stat-label {
    font-size: 24rpx;
    color: rgba(255,255,255,0.6);
  }
}

.stat-divider {
  width: 2rpx;
  height: 60rpx;
  background: rgba(255,255,255,0.2);
}

/* 时间轴区域 */
.timeline-section {
  margin: 0 30rpx 40rpx;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 30rpx;
  
  .title-icon {
    font-size: 36rpx;
  }
  
  .title-text {
    font-size: 32rpx;
    font-weight: 700;
    color: #fff;
  }
}

.timeline {
  position: relative;
}

.timeline-item {
  display: flex;
  margin-bottom: 30rpx;
  
  &.latest {
    .version-card {
      border: 1rpx solid rgba(102, 126, 234, 0.5);
      box-shadow: 0 8rpx 32rpx rgba(102, 126, 234, 0.2);
    }
  }
}

.timeline-line {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 60rpx;
  flex-shrink: 0;
}

.timeline-dot {
  width: 40rpx;
  height: 40rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  
  &.dot-latest {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    box-shadow: 0 0 20rpx rgba(102, 126, 234, 0.5);
  }
  
  &.dot-normal {
    background: rgba(255,255,255,0.2);
    border: 4rpx solid rgba(255,255,255,0.3);
  }
  
  .dot-icon {
    font-size: 20rpx;
  }
}

.timeline-connector {
  flex: 1;
  width: 2rpx;
  background: linear-gradient(180deg, rgba(255,255,255,0.3) 0%, rgba(255,255,255,0.1) 100%);
  margin: 10rpx 0;
}

.version-card {
  flex: 1;
  margin-left: 20rpx;
  padding: 30rpx;
  background: linear-gradient(135deg, rgba(255,255,255,0.1) 0%, rgba(255,255,255,0.05) 100%);
  border-radius: 24rpx;
  border: 1rpx solid rgba(255,255,255,0.1);
  backdrop-filter: blur(20rpx);
}

.version-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16rpx;
}

.version-badge-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.version-name {
  font-size: 32rpx;
  font-weight: 700;
  color: #fff;
}

.latest-tag {
  padding: 4rpx 12rpx;
  background: linear-gradient(135deg, #52c41a 0%, #389e0d 100%);
  border-radius: 8rpx;
  font-size: 20rpx;
  color: #fff;
}

.type-tag {
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
  font-size: 20rpx;
  
  &.major {
    background: linear-gradient(135deg, #f5222d 0%, #cf1322 100%);
    color: #fff;
  }
  
  &.feature {
    background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
    color: #fff;
  }
}

.version-date {
  font-size: 24rpx;
  color: rgba(255,255,255,0.5);
}

.version-slogan {
  margin-bottom: 20rpx;
  padding: 16rpx 20rpx;
  background: rgba(102, 126, 234, 0.1);
  border-left: 4rpx solid #667eea;
  border-radius: 8rpx;
  
  .slogan-text {
    font-size: 26rpx;
    color: rgba(255,255,255,0.8);
    font-style: italic;
  }
}

.version-content {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
  
  &.collapsed {
    max-height: 300rpx;
    overflow: hidden;
    mask-image: linear-gradient(180deg, #000 60%, transparent 100%);
    -webkit-mask-image: linear-gradient(180deg, #000 60%, transparent 100%);
  }
}

.change-group {
  .group-header {
    display: flex;
    align-items: center;
    gap: 10rpx;
    margin-bottom: 12rpx;
    
    .group-icon {
      font-size: 28rpx;
    }
    
    .group-title {
      font-size: 26rpx;
      font-weight: 600;
      color: #fff;
    }
    
    .group-count {
      padding: 2rpx 10rpx;
      background: rgba(255,255,255,0.1);
      border-radius: 10rpx;
      font-size: 20rpx;
      color: rgba(255,255,255,0.6);
    }
  }
}

.change-list {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
  padding-left: 40rpx;
}

.change-row {
  display: flex;
  gap: 10rpx;
  
  .change-bullet {
    font-size: 20rpx;
    color: #667eea;
    line-height: 1.6;
  }
  
  .change-text {
    flex: 1;
    font-size: 26rpx;
    color: rgba(255,255,255,0.8);
    line-height: 1.6;
  }
}

.expand-hint {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  margin-top: 20rpx;
  padding-top: 20rpx;
  border-top: 1rpx solid rgba(255,255,255,0.1);
  
  .hint-text {
    font-size: 24rpx;
    color: rgba(255,255,255,0.6);
  }
  
  .hint-icon {
    font-size: 20rpx;
    color: rgba(255,255,255,0.6);
    transition: transform 0.3s ease;
    
    &.rotated {
      transform: rotate(180deg);
    }
  }
}

/* 底部区域 */
.footer-section {
  margin: 0 30rpx 60rpx;
}

.footer-card {
  padding: 40rpx;
  background: linear-gradient(135deg, rgba(255,255,255,0.05) 0%, rgba(255,255,255,0.02) 100%);
  border-radius: 30rpx;
  border: 1rpx solid rgba(255,255,255,0.1);
  text-align: center;
}

.footer-title {
  display: block;
  font-size: 32rpx;
  font-weight: 700;
  color: #fff;
  margin-bottom: 12rpx;
}

.footer-desc {
  display: block;
  font-size: 26rpx;
  color: rgba(255,255,255,0.6);
  margin-bottom: 30rpx;
}

.footer-links {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 20rpx;
  margin-bottom: 20rpx;
  
  .footer-link {
    font-size: 24rpx;
    color: #667eea;
  }
  
  .footer-divider {
    font-size: 24rpx;
    color: rgba(255,255,255,0.3);
  }
}

.copyright {
  font-size: 22rpx;
  color: rgba(255,255,255,0.4);
}

/* 更新弹窗 */
.update-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 60rpx;
}

.modal-content {
  width: 100%;
  max-width: 560rpx;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
  border-radius: 40rpx;
  border: 1rpx solid rgba(255,255,255,0.1);
  padding: 50rpx 40rpx;
  text-align: center;
}

.modal-header {
  margin-bottom: 30rpx;
  
  .modal-icon {
    font-size: 80rpx;
    display: block;
    margin-bottom: 20rpx;
  }
  
  .modal-title {
    font-size: 36rpx;
    font-weight: 700;
    color: #fff;
  }
}

.modal-body {
  margin-bottom: 40rpx;
  
  .modal-version {
    display: block;
    font-size: 48rpx;
    font-weight: 800;
    color: #667eea;
    margin-bottom: 16rpx;
  }
  
  .modal-desc {
    font-size: 28rpx;
    color: rgba(255,255,255,0.7);
    line-height: 1.5;
  }
}

.modal-footer {
  display: flex;
  gap: 20rpx;
}

.modal-btn {
  flex: 1;
  padding: 28rpx 0;
  border-radius: 16rpx;
  font-size: 30rpx;
  font-weight: 600;
  
  &.secondary {
    background: rgba(255,255,255,0.1);
    color: rgba(255,255,255,0.8);
  }
  
  &.primary {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: #fff;
  }
}
</style>
