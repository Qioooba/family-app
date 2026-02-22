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
    text: '日程',
    pagePath: '/pages/task/index'
  },
  {
    iconPath: 'plus-circle',
    selectedIconPath: 'plus-circle-fill',
    text: '添加',
    midButton: true,
    pagePath: '/pages/quick-add/index'
  },
  {
    iconPath: 'heart',
    selectedIconPath: 'heart-fill',
    text: '心愿',
    pagePath: '/pages/wish/index'
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
