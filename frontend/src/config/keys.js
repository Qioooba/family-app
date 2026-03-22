// 配置文件 - 敏感信息从环境变量或构建时注入
// 注意：不要在此文件写入真实密钥，使用环境变量

const injectedTencentMapKey = typeof __APP_TENCENT_MAP_KEY__ !== 'undefined' ? __APP_TENCENT_MAP_KEY__ : ''
const injectedWeixinAppId = typeof __APP_WEIXIN_APPID__ !== 'undefined' ? __APP_WEIXIN_APPID__ : ''

// 腾讯地图 API Key
// 生产环境应从环境变量读取，这里使用占位符
export const TENCENT_MAP_KEY = injectedTencentMapKey || import.meta.env.VITE_TENCENT_MAP_KEY || ''

// 微信小程序 AppID（可公开）
export const WEIXIN_APPID = injectedWeixinAppId || import.meta.env.VITE_WEIXIN_APPID || ''
