import { ref, watch } from 'vue'

// 主题类型
const THEME_LIGHT = 'light'
const THEME_DARK = 'dark'
const THEME_AUTO = 'auto'

// 存储键
const THEME_STORAGE_KEY = 'app_theme'

// 当前主题
const currentTheme = ref(THEME_LIGHT)
const isDarkMode = ref(false)

/**
 * 获取系统主题偏好
 */
const getSystemTheme = () => {
  if (typeof window !== 'undefined' && window.matchMedia) {
    return window.matchMedia('(prefers-color-scheme: dark)').matches ? THEME_DARK : THEME_LIGHT
  }
  return THEME_LIGHT
}

/**
 * 从存储加载主题
 */
const loadThemeFromStorage = () => {
  try {
    const saved = uni.getStorageSync(THEME_STORAGE_KEY)
    return saved || THEME_LIGHT
  } catch (e) {
    return THEME_LIGHT
  }
}

/**
 * 保存主题到存储
 */
const saveThemeToStorage = (theme) => {
  try {
    uni.setStorageSync(THEME_STORAGE_KEY, theme)
  } catch (e) {
    console.error('保存主题失败', e)
  }
}

/**
 * 应用主题到页面
 */
const applyTheme = (theme) => {
  const isDark = theme === THEME_DARK || (theme === THEME_AUTO && getSystemTheme() === THEME_DARK)
  isDarkMode.value = isDark
  
  // 添加/移除深色模式类名
  if (typeof document !== 'undefined') {
    const html = document.documentElement
    if (isDark) {
      html.classList.add('dark-mode')
      html.setAttribute('data-theme', 'dark')
    } else {
      html.classList.remove('dark-mode')
      html.setAttribute('data-theme', 'light')
    }
  }
  
  // 设置页面背景色 - 仅在 App 和小程序可用
  // #ifndef H5
  const bgColor = isDark ? '#1a1a2e' : '#f5f6fa'
  uni.setBackgroundColor({ backgroundColor: bgColor })
  // #endif
  
  // 设置导航栏颜色 - 仅在 App 和小程序可用
  // #ifndef H5
  const navColor = isDark ? '#1a1a2e' : '#ffffff'
  const titleColor = isDark ? '#ffffff' : '#000000'
  uni.setNavigationBarColor({
    frontColor: titleColor,
    backgroundColor: navColor
  })
  // #endif
  
  // 设置TabBar颜色 - 仅在 App 和小程序可用
  // #ifndef H5
  uni.setTabBarStyle({
    color: isDark ? '#999999' : '#999999',
    selectedColor: '#5B8FF9',
    backgroundColor: isDark ? '#1a1a2e' : '#ffffff',
    borderStyle: isDark ? 'black' : 'white'
  })
  // #endif
}

/**
 * 设置主题
 */
const setTheme = (theme) => {
  if (![THEME_LIGHT, THEME_DARK, THEME_AUTO].includes(theme)) {
    theme = THEME_LIGHT
  }
  currentTheme.value = theme
  saveThemeToStorage(theme)
  applyTheme(theme)
}

/**
 * 切换主题
 */
const toggleTheme = () => {
  const newTheme = isDarkMode.value ? THEME_LIGHT : THEME_DARK
  setTheme(newTheme)
}

/**
 * 初始化主题
 */
const initTheme = () => {
  const savedTheme = loadThemeFromStorage()
  currentTheme.value = savedTheme
  applyTheme(savedTheme)
  
  // 监听系统主题变化
  if (typeof window !== 'undefined' && window.matchMedia) {
    const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)')
    if (mediaQuery.addEventListener) {
      mediaQuery.addEventListener('change', (e) => {
        if (currentTheme.value === THEME_AUTO) {
          applyTheme(THEME_AUTO)
        }
      })
    }
  }
}

/**
 * 获取主题变量（用于动态样式）
 */
const getThemeVars = () => {
  return {
    isDark: isDarkMode.value,
    bgPrimary: isDarkMode.value ? '#1a1a2e' : '#f5f6fa',
    bgSecondary: isDarkMode.value ? '#16213e' : '#ffffff',
    bgCard: isDarkMode.value ? '#0f3460' : '#ffffff',
    textPrimary: isDarkMode.value ? '#ffffff' : '#333333',
    textSecondary: isDarkMode.value ? '#b8c5d6' : '#666666',
    textTertiary: isDarkMode.value ? '#8b9dc3' : '#999999',
    borderColor: isDarkMode.value ? '#2d3561' : '#e8e8e8',
    shadowColor: isDarkMode.value ? 'rgba(0,0,0,0.3)' : 'rgba(0,0,0,0.05)'
  }
}

export {
  THEME_LIGHT,
  THEME_DARK,
  THEME_AUTO,
  currentTheme,
  isDarkMode,
  setTheme,
  toggleTheme,
  initTheme,
  getThemeVars
}
