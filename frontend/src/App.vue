<template>
  <view class="app-container">
    <router-view />
    <!-- 全局TabBar -->
    <u-tabbar
      v-model="current"
      :list="tabList"
      active-color="#5B8FF9"
      inactive-color="#999"
      @change="onTabChange"
    />
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from './stores/user'
import { initTheme } from './utils/theme.js'

const userStore = useUserStore()
const current = ref(0)

// 初始化主题
onMounted(() => {
  initTheme()
  
  // 启动时清理过期缓存（开发测试用）
  clearExpiredCache()
  
  // 检测小程序版本更新
  checkUpdate()
  
  // 输出当前版本信息
  logVersionInfo()
})

// 输出版本信息
const logVersionInfo = () => {
  // #ifdef MP-WEIXIN
  try {
    // 使用 uni.getAccountInfoSync 替代 wx.getAccountInfoSync
    const accountInfo = uni.getAccountInfoSync()
    
    if (accountInfo && accountInfo.miniProgram) {
      const env = accountInfo.miniProgram.envVersion
      const version = accountInfo.miniProgram.version
      
      
      // 存到全局方便调试
      uni.setStorageSync('app_version_info', {
        env: env || 'unknown',
        version: version || 'unknown',
        time: new Date().toLocaleString()
      })
    } else {
    }
  } catch (e) {
    console.error('获取版本信息失败:', e)
    // 备用方案：从 manifest.json 读取
    const manifest = uni.getStorageSync('__UNI_MANIFEST__')
  }
  // #endif
  
  // #ifndef MP-WEIXIN
  // #endif
}

// 检测小程序版本更新 - 强制刷新机制
const checkUpdate = () => {
  // #ifdef MP-WEIXIN
  const updateManager = wx.getUpdateManager()
  
  // 本地存储的版本号
  const localVersion = uni.getStorageSync('app_version_local') || '0.0.0'
  
  updateManager.onCheckForUpdate(function (res) {
    // 请求完新版本信息的回调
    
    // 如果有更新，先记录下来
    if (res.hasUpdate) {
      uni.setStorageSync('has_new_version', true)
    }
  })
  
  updateManager.onUpdateReady(function () {
    
    // 强制应用更新 - 不给用户拒绝的机会
    wx.showModal({
      title: '🎉 版本更新',
      content: '新版本已准备好，需要重启应用以使用最新功能。',
      showCancel: false,
      confirmText: '立即重启',
      confirmColor: '#667eea',
      success: function (res) {
        // 无论用户点什么，都强制应用更新
        updateManager.applyUpdate()
      },
      fail: function() {
        // 即使弹窗失败，也强制更新
        updateManager.applyUpdate()
      }
    })
  })
  
  updateManager.onUpdateFailed(function () {
    // 新版本下载失败
    uni.showModal({
      title: '更新失败',
      content: '新版本下载失败，请检查网络后重试',
      showCancel: false,
      confirmText: '知道了'
    })
  })
  
  // 额外的缓存清理机制 - 每次启动都检查
  checkVersionMismatch()
  // #endif
}

// 检查版本号是否匹配 - 防止缓存问题
const checkVersionMismatch = () => {
  try {
    // 从 storage 读取上次记录的版本
    const lastVersion = uni.getStorageSync('app_version_recorded')
    const buildTime = uni.getStorageSync('app_build_time')
    
    // 当前构建版本（每次构建时通过构建脚本注入）
    const currentBuildVersion = '1.0.3' // 与 manifest.json 保持一致
    const currentBuildTime = '20260313'
    
    // 如果版本不匹配，强制清理缓存
    if (lastVersion && lastVersion !== currentBuildVersion) {
      forceClearCache('版本已更新，正在清理缓存...')
      return
    }
    
    // 如果构建时间不一致（同一天多次构建）
    if (buildTime && buildTime !== currentBuildTime) {
      forceClearCache('应用已更新，正在清理缓存...')
      return
    }
    
    // 记录当前版本
    uni.setStorageSync('app_version_recorded', currentBuildVersion)
    uni.setStorageSync('app_build_time', currentBuildTime)
    
  } catch (e) {
    console.error('[Version] 版本检查失败:', e)
  }
}

// 强制清理缓存
const forceClearCache = (message) => {
  
  // 显示提示
  uni.showToast({
    title: message,
    icon: 'loading',
    duration: 2000
  })
  
  // 延迟清理，让用户看到提示
  setTimeout(() => {
    // 清理所有缓存（除了版本记录本身）
    const versionRecorded = uni.getStorageSync('app_version_recorded')
    const buildTime = uni.getStorageSync('app_build_time')
    
    uni.clearStorageSync()
    
    // 恢复版本记录
    uni.setStorageSync('app_version_recorded', versionRecorded)
    uni.setStorageSync('app_build_time', buildTime)
    
    // 重新加载
    uni.reLaunch({
      url: '/pages/home/index',
      success: () => {
        uni.showToast({
          title: '已更新到最新版本',
          icon: 'success',
          duration: 2000
        })
      }
    })
  }, 1500)
}

// 清理过期缓存
const clearExpiredCache = () => {
  try {
    // 清理本地存储的用户数据（强制重新登录）
    const timestamp = uni.getStorageSync('cache_clear_time')
    const now = Date.now()

    // 如果超过1小时或强制清理标志存在，则清理
    if (!timestamp || (now - timestamp > 3600000) || uni.getStorageSync('force_clear')) {

      // 保留邀请码、token 等重要配置，只清理临时缓存
      const keepKeys = [
        'family_invite_code',
        'cache_clear_time',
        'token',
        'userInfo',
        'currentFamilyId',
        'familyList'
      ]
      const allKeys = uni.getStorageInfoSync().keys || []

      allKeys.forEach(key => {
        if (!keepKeys.includes(key)) {
          uni.removeStorageSync(key)
        }
      })

      uni.setStorageSync('cache_clear_time', now)
      uni.removeStorageSync('force_clear')

    }
  } catch (e) {
    console.error('[App] 清理缓存失败:', e)
  }
}

const tabList = [
  {
    iconPath: 'home',
    selectedIconPath: 'home-fill',
    text: '首页',
    pagePath: '/pages/home/index'
  },
  {
    iconPath: 'calendar',
    selectedIconPath: 'calendar-fill',
    text: '待办',
    pagePath: '/pages/task/index'
  },
  {
    iconPath: 'account',
    selectedIconPath: 'account-fill',
    text: '家庭',
    pagePath: '/pages/family/index'
  },
  {
    iconPath: 'account',
    selectedIconPath: 'account-fill',
    text: '我的',
    pagePath: '/pages/profile/index'
  }
]

onMounted(() => {
  // 检查登录状态
  userStore.checkLogin()
})

const onTabChange = (index) => {
  const page = tabList[index].pagePath
  uni.switchTab({ url: page })
}
</script>

<style lang="scss">
.app-container {
  min-height: 100vh;
  background: #f5f6fa;
}
</style>

<style>
/* 全局修复 picker 弹窗层级 */
.uni-picker-container,
.uni-picker-view-wrapper,
.uni-picker-header,
.uni-picker-action,
.uni-picker-menu,
.uni-picker-content,
.uni-picker-mask {
  z-index: 9999999 !important;
  position: fixed !important;
}
</style>
