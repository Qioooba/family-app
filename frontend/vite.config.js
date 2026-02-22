import { defineConfig } from 'vite'
import uni from '@dcloudio/vite-plugin-uni'
import { resolve } from 'path'

// 分析依赖大小的插件（可选，构建时开启）
// import { visualizer } from 'rollup-plugin-visualizer'

export default defineConfig({
  plugins: [
    uni(),
    // 打包分析插件，默认关闭，需要时开启
    // visualizer({
    //   open: true,
    //   gzipSize: true,
    //   brotliSize: true,
    // })
  ],
  
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
  
  build: {
    // 代码分割配置
    rollupOptions: {
      output: {
        // 手动代码分割策略
        manualChunks(id) {
          // 1. 将 node_modules 中的依赖单独打包
          if (id.includes('node_modules')) {
            // 大型 UI 库单独打包
            if (id.includes('uview-plus')) {
              return 'vendor-ui'
            }
            // Vue 生态单独打包
            if (id.includes('vue') || id.includes('@dcloudio')) {
              return 'vendor-vue'
            }
            // 图表库单独打包
            if (id.includes('echarts')) {
              return 'vendor-charts'
            }
            // 其他依赖打包到一起
            return 'vendor'
          }
          
          // 2. 按功能模块分割
          // API 层单独打包
          if (id.includes('/src/api/')) {
            return 'api'
          }
          
          // 工具函数单独打包
          if (id.includes('/src/utils/')) {
            return 'utils'
          }
          
          // 3. 按页面分包（动态导入）
          // 大型页面单独打包
          if (id.includes('/pages/dashboard/')) {
            return 'page-dashboard'
          }
          if (id.includes('/pages/ai/')) {
            return 'page-ai'
          }
          if (id.includes('/pages/game/')) {
            return 'page-game'
          }
        },
        
        // 控制 chunk 文件命名
        chunkFileNames(chunkInfo) {
          const name = chunkInfo.name
          // 根据 chunk 名称归类到不同目录
          if (name.startsWith('vendor')) {
            return 'js/vendor/[name]-[hash].js'
          }
          if (name.startsWith('page-')) {
            return 'js/pages/[name]-[hash].js'
          }
          if (name === 'api' || name === 'utils') {
            return 'js/shared/[name]-[hash].js'
          }
          return 'js/[name]-[hash].js'
        },
        
        // 入口文件命名
        entryFileNames: 'js/[name]-[hash].js',
        
        // 资源文件命名
        assetFileNames: (assetInfo) => {
          const info = assetInfo.name.split('.')
          const ext = info[info.length - 1]
          
          // 根据资源类型归类
          if (/\.(png|jpe?g|gif|svg|webp|ico)$/i.test(assetInfo.name)) {
            return 'images/[name]-[hash][extname]'
          }
          if (/\.(woff2?|eot|ttf|otf)$/i.test(assetInfo.name)) {
            return 'fonts/[name]-[hash][extname]'
          }
          if (/\.css$/i.test(assetInfo.name)) {
            return 'css/[name]-[hash][extname]'
          }
          return 'assets/[name]-[hash][extname]'
        }
      }
    },
    
    // 构建优化
    minify: 'terser',
    terserOptions: {
      compress: {
        drop_console: true, // 移除 console
        drop_debugger: true, // 移除 debugger
        pure_funcs: ['console.log', 'console.info', 'console.debug', 'console.trace']
      },
      format: {
        comments: false // 移除注释
      }
    },
    
    // 资源内联阈值
    assetsInlineLimit: 4096, // 4KB 以下的资源内联为 base64
    
    // 源码映射（生产环境关闭）
    sourcemap: false,
    
    // 分块大小警告阈值
    chunkSizeWarningLimit: 1000
  },
  
  optimizeDeps: {
    // 预构建依赖
    include: [
      'vue',
      'pinia',
      'dayjs',
      'uview-plus'
    ],
    // 排除某些依赖（如果它们导致问题）
    exclude: []
  },
  
  css: {
    // CSS 优化
    devSourcemap: true,
    preprocessorOptions: {
      scss: {
        silenceDeprecations: ['legacy-js-api', 'import', 'global-builtin', 'slash-div'],
        additionalData: `
          @import "uview-plus/theme.scss";
        `
      }
    }
  },
  
  // 开发服务器配置
  server: {
    port: 3000,
    open: false,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '')
      }
    }
  }
})
