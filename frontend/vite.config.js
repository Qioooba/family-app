import { defineConfig } from 'vite'
import uni from '@dcloudio/vite-plugin-uni'
import { resolve } from 'path'

export default defineConfig({
  plugins: [uni()],
  build: {
    sourcemap: false,
    minify: 'esbuild'
  },
  esbuild: { 
    platform: 'browser',
    sourcemap: false
  },
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
  },
  optimizeDeps: {
    noDiscovery: true,
    include: undefined
  },
  // 禁用所有 sourcemap 相关功能
  vueCompilerOptions: {
    reactivityTransform: false
  }
})
