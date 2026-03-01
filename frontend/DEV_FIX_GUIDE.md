# 前端开发环境问题排查指南

## 问题描述

uni-app H5 开发模式下：
1. 首次访问页面白屏
2. 第二次访问时服务崩溃，报错：
   ```
   Cannot read properties of undefined (reading 'length')
   at .../@dcloudio/uni-h5-vite/dist/plugins/sourcemap.js:37
   ```

## 根本原因

`@dcloudio/uni-h5-vite` 的 sourcemap 插件在处理某些模块时，`map.sources` 为 `undefined`，但代码直接访问 `map.sources.length` 导致报错。

## 解决方案

### 方案一：临时补丁（推荐）

直接修改 node_modules 中的文件：

**文件位置**: `node_modules/@dcloudio/uni-h5-vite/dist/plugins/sourcemap.js`

**修改内容**（约第37行）:

```javascript
// 修改前
const map = request?.map;
if (map) {

// 修改后
const map = request?.map;
if (map && map.sources && Array.isArray(map.sources)) {
```

> ⚠️ 注意：此补丁在重装依赖后会失效，需要重新应用。

### 方案二：禁用 sourcemap（备选）

在 `vite.config.js` 中禁用 sourcemap：

```javascript
export default defineConfig({
  build: {
    sourcemap: false,
  },
  esbuild: {
    sourcemap: false
  }
})
```

## 相关配置

### vite.config.js 完整配置

```javascript
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
  vueCompilerOptions: {
    reactivityTransform: false
  }
})
```

### Node 版本要求

- 推荐使用 **Node 18**（项目根目录已配置 `.nvmrc`）
- 使用 nvm 管理多版本：
  ```bash
  nvm use 18  # 切换到 Node 18
  npm run dev:h5  # 启动开发服务
  ```

## 启动命令

```bash
cd frontend
nvm use 18
npm run dev:h5
```

访问 http://localhost:3000
