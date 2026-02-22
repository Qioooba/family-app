/**
 * 性能优化配置
 * 包含：预加载、缓存、请求合并等策略
 */

// 预加载配置
export const preloadConfig = {
  // 需要预加载的页面
  pages: [
    '/pages/task/index',
    '/pages/task/calendar',
    '/pages/task/create'
  ],
  // 预加载时机
  trigger: 'onLaunch', // onLaunch | enterPage | idle
  // 延迟时间
  delay: 2000
}

// 缓存配置
export const cacheConfig = {
  // 启用缓存
  enabled: true,
  // 缓存有效期（毫秒）
  maxAge: 5 * 60 * 1000, // 5分钟
  // 最大缓存条目
  maxEntries: 50,
  // 排除的接口
  exclude: ['/api/auth', '/api/upload']
}

// 请求合并配置
export const mergeConfig = {
  // 启用合并
  enabled: true,
  // 合并时间窗口（毫秒）
  window: 100,
  // 最大合并数
  maxMerge: 10,
  // 支持合并的接口
  include: ['/api/task/list', '/api/task/detail']
}

// 图片懒加载配置
export const lazyLoadConfig = {
  // 预加载高度
  preloadHeight: 500,
  // 占位图
  placeholder: '/static/placeholder.png',
  // 错误图
  errorImage: '/static/error.png'
}

// 离线支持配置
export const offlineConfig = {
  // 启用离线
  enabled: true,
  // 离线页面
  offlinePage: '/pages/offline/index',
  // 缓存白名单
  cacheList: [
    '/api/task/list',
    '/api/user/info'
  ]
}
