import { defineConfig, loadEnv } from 'vite'
import uni from '@dcloudio/vite-plugin-uni'
import { resolve } from 'path'

export default defineConfig(({ mode }) => {
  const localEnv = loadEnv(mode, process.cwd(), '')
  const rootEnv = loadEnv(mode, resolve(__dirname, '..'), '')
  const env = {
    ...rootEnv,
    ...localEnv,
    ...process.env
  }

  const proxyTarget = env.VITE_PROXY_TARGET || env.PROXY_TARGET || 'http://localhost:8443'
  const apiBaseUrl = env.VITE_API_BASE_URL || env.API_BASE_URL || ''
  const tencentMapKey = env.VITE_TENCENT_MAP_KEY || env.TENCENT_MAP_KEY || ''
  const weixinAppId = env.VITE_WEIXIN_APPID || env.WEIXIN_APPID || ''
  const extraHosts = (env.VITE_ALLOWED_HOSTS || env.ALLOWED_HOSTS || '')
    .split(',')
    .map(host => host.trim())
    .filter(Boolean)

  return {
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
      allowedHosts: extraHosts,
      host: '0.0.0.0',
      port: Number(env.VITE_DEV_SERVER_PORT || 3000),
      open: false,
      proxy: {
        '/api': {
          target: proxyTarget,
          changeOrigin: true
        },
        '/user': {
          target: proxyTarget,
          changeOrigin: true
        }
      }
    },
    optimizeDeps: {
      noDiscovery: true,
      include: undefined
    },
    vueCompilerOptions: {
      reactivityTransform: false
    },
    define: {
      __VUE_PROD_DEVTOOLS__: true,
      __APP_API_BASE_URL__: JSON.stringify(apiBaseUrl),
      __APP_TENCENT_MAP_KEY__: JSON.stringify(tencentMapKey),
      __APP_WEIXIN_APPID__: JSON.stringify(weixinAppId)
    }
  }
})
