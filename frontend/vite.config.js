import { defineConfig } from 'vite'
import uni from '@dcloudio/vite-plugin-uni'
import { resolve } from 'path'

const fixExternalPlugin = () => ({
  name: 'fix-external',
  configResolved(config) {
    if (config.build?.rollupOptions) {
      config.build.rollupOptions.external = []
    }
    if (config.esbuild) {
      // 移除esbuild的external配置，避免冲突
    }
    if (!config.optimizeDeps) {
      config.optimizeDeps = {}
    }
    if (!config.optimizeDeps.include) {
      config.optimizeDeps.include = []
    }
    const deps = ['vue', 'pinia']
    deps.forEach(dep => {
      if (!config.optimizeDeps.include.includes(dep)) {
        config.optimizeDeps.include.push(dep)
      }
    })
  }
})

export default defineConfig({
  plugins: [uni(), fixExternalPlugin()],
  esbuild: { external: [], platform: 'browser' },
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src'),
      '@components': resolve(__dirname, 'src/components'),
      '@utils': resolve(__dirname, 'src/utils'),
      '@api': resolve(__dirname, 'src/api'),
      '@stores': resolve(__dirname, 'src/stores'),
      '@static': resolve(__dirname, 'src/static'),
      '@styles': resolve(__dirname, 'src/styles')
    }
  },
  css: {
    preprocessorOptions: {
      scss: {
        additionalData: `@import "uview-plus/theme.scss"; @import "uview-plus/libs/css/mixin.scss";`,
        silenceDeprecations: ['legacy-js-api', 'import']
      }
    }
  },
  server: {
    allowedHosts: ['qioba.cn', '.qioba.cn'],
    host: '0.0.0.0',
    port: 3000,
    open: false,
    proxy: {
      '/api': {
        target: 'http://localhost:8081',
        changeOrigin: true
      },
      '/user': {
        target: 'http://localhost:8081',
        changeOrigin: true
      }
    }
  }
})