import { createSSRApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import uviewPlus from 'uview-plus'

// 引入全局样式
import './styles/index.scss'

// 引入工具
import { request } from './utils/request'
import { toast, loading, modal } from './utils/ui'
import { vLazy } from './utils/lazyLoad'

export function createApp() {
  const app = createSSRApp(App)
  const pinia = createPinia()
  
  app.use(pinia)
  app.use(uviewPlus)
  
  // 注册全局指令
  app.directive('lazy', vLazy)
  
  // 全局挂载
  app.config.globalProperties.$request = request
  app.config.globalProperties.$toast = toast
  app.config.globalProperties.$loading = loading
  app.config.globalProperties.$modal = modal
  
  return {
    app
  }
}
