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
  const accountInfo = wx.getAccountInfoSync()
  const env = accountInfo.miniProgram.envVersion
  const version = accountInfo.miniProgram.version
  
  console.log('========== 小程序版本信息 ==========')
  console.log('环境类型:', env)  // develop-开发版 trial-体验版 release-正式版
  console.log('版本号:', version)
  console.log('====================================')
  
  // 可以存到全局方便调试
  uni.setStorageSync('app_version_info', {
    env: env,
    version: version,
    time: new Date().toLocaleString()
  })
  // #endif
}

// 检测小程序版本更新
const checkUpdate = () => {
  // #ifdef MP-WEIXIN
  const updateManager = wx.getUpdateManager()
  
  updateManager.onCheckForUpdate(function (res) {
    // 请求完新版本信息的回调
    console.log('[Update] 检测更新:', res.hasUpdate)
  })
  
  updateManager.onUpdateReady(function () {
    wx.showModal({
      title: '更新提示',
      content: '新版本已准备好，是否重启应用？',
      success: function (res) {
        if (res.confirm) {
          // 新的版本已经下载好，调用 applyUpdate 应用新版本并重启
          updateManager.applyUpdate()
        }
      }
    })
  })
  
  updateManager.onUpdateFailed(function () {
    // 新版本下载失败
    console.log('[Update] 新版本下载失败')
  })
  // #endif
}

// 清理过期缓存
const clearExpiredCache = () => {
  try {
    // 清理本地存储的用户数据（强制重新登录）
    const timestamp = uni.getStorageSync('cache_clear_time')
    const now = Date.now()
    
    // 如果超过1小时或强制清理标志存在，则清理
    if (!timestamp || (now - timestamp > 3600000) || uni.getStorageSync('force_clear')) {
      console.log('[App] 清理本地缓存...')
      
      // 保留邀请码等配置，只清理用户相关
      const keepKeys = ['family_invite_code', 'cache_clear_time']
      const allKeys = uni.getStorageInfoSync().keys || []
      
      allKeys.forEach(key => {
        if (!keepKeys.includes(key)) {
          uni.removeStorageSync(key)
        }
      })
      
      uni.setStorageSync('cache_clear_time', now)
      uni.removeStorageSync('force_clear')
      
      console.log('[App] 缓存清理完成')
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
