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
})

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
