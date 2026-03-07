import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { request } from '../utils/request'

export const useUserStore = defineStore('user', () => {
  // State - 直接使用 ref
  const token = ref(uni.getStorageSync('token') || '')
  const userInfo = ref(uni.getStorageSync('userInfo') || null)
  
  // Computed - 基于 ref
  const isLogin = computed(() => !!token.value)
  
  // Actions
  const setToken = (val) => {
    // 处理对象类型（后端返回 {token: "xxx", ...}）或字符串类型
    const tokenValue = typeof val === 'string' ? val : (val?.token || '')
    token.value = tokenValue
    // H5 兼容：同时设置 window 全局变量
    if (typeof window !== 'undefined') {
      try {
        window.__APP_TOKEN__ = tokenValue
      } catch (e) {
        console.log('[Store] window.__APP_TOKEN__ 设置失败:', e)
      }
    }
    uni.setStorageSync('token', tokenValue)
    // 确保同步完成
    // 简化日志
    // console.log('[Store] setToken:', tokenValue ? '已设置' : '空')
    // console.log('[Store] verify token:', uni.getStorageSync('token') ? 'OK' : 'FAIL')
  }
  
  const setUserInfo = (val) => {
    userInfo.value = val
    uni.setStorageSync('userInfo', val)
  }
  
  const login = async (loginData) => {
    const res = await request.post('/api/user/login', loginData)
    setToken(res)
    // 保存 currentFamilyId（request.post 已自动 unwrap response，res 已是内层 data）
    const familyId = res?.currentFamilyId
    if (familyId) {
      uni.setStorageSync('currentFamilyId', familyId)
      console.log('[Store] 保存 currentFamilyId:', familyId)
    }
    await getUserInfo()
    return res
  }
  
  const register = async (registerData) => {
    const res = await request.post('/api/user/register', registerData)
    // 保存 currentFamilyId（register 也会返回 currentFamilyId）
    const familyId = res?.currentFamilyId
    if (familyId) {
      uni.setStorageSync('currentFamilyId', familyId)
      console.log('[Store] 注册后保存 currentFamilyId:', familyId)
    }
    return res
  }
  
  // 微信登录
  const wxLogin = async (wxLoginData) => {
    const res = await request.post('/api/user/wx-login', wxLoginData)
    console.log('[Store] wxLogin 原始响应:', JSON.stringify(res))
    
    // 处理响应格式
    let loginData = res
    if (res && res.code === 200 && res.data) {
      loginData = res.data
    }
    
    setToken(loginData)
    // 保存 currentFamilyId
    const familyId = loginData?.currentFamilyId
    if (familyId) {
      uni.setStorageSync('currentFamilyId', familyId)
    }
    await getUserInfo()
    return loginData
  }
  
  const getUserInfo = async () => {
    try {
      const res = await request.get('/api/user/info')
      console.log('[Store] getUserInfo result:', JSON.stringify(res))
      
      // 处理错误状态码
      if (res.code === 401) {
        console.error('[Store] Token已过期，需要重新登录')
        // 清除本地token
        token.value = ''
        userInfo.value = null
        uni.removeStorageSync('token')
        uni.removeStorageSync('userInfo')
        // 显示提示
        uni.showToast({
          title: '登录已过期，请重新登录',
          icon: 'none',
          duration: 2000
        })
        // 延迟跳转到登录页
        setTimeout(() => {
          uni.reLaunch({ url: '/pages/login/index' })
        }, 1500)
        return null
      }
      
      if (res.code === 404) {
        console.error('[Store] 用户不存在:', res.message)
        // 清除本地token
        token.value = ''
        userInfo.value = null
        uni.removeStorageSync('token')
        uni.removeStorageSync('userInfo')
        // 显示具体错误提示
        uni.showToast({
          title: res.message || '用户数据不存在',
          icon: 'none',
          duration: 2500
        })
        // 延迟跳转到登录页
        setTimeout(() => {
          uni.reLaunch({ url: '/pages/login/index' })
        }, 2000)
        return null
      }
      
      // 成功获取用户信息
      if (res.code === 200 && res.data) {
        setUserInfo(res.data)
        // 同时保存 currentFamilyId
        const familyId = res.data.currentFamilyId
        if (familyId) {
          uni.setStorageSync('currentFamilyId', familyId)
        }
        return res.data
      }
      
      // 其他错误
      console.error('[Store] 获取用户信息失败:', res.message)
      return null
    } catch (e) {
      console.error('[Store] getUserInfo error:', e)
      // 网络错误或其他异常
      if (e.code === 401 || (e.message && e.message.includes('登录'))) {
        token.value = ''
        userInfo.value = null
        uni.removeStorageSync('token')
        uni.removeStorageSync('userInfo')
        uni.showToast({
          title: '登录已过期，请重新登录',
          icon: 'none'
        })
        setTimeout(() => {
          uni.reLaunch({ url: '/pages/login/index' })
        }, 1500)
      }
      return null
    }
  }
  
  const updateUserInfo = async (data) => {
    const res = await request.put('/api/user/info', data)
    // res 是完整响应 {code, message, data}，需要提取 data 部分
    if (res && res.data) {
      setUserInfo(res.data)
    } else {
      setUserInfo(res)
    }
    return res
  }
  
  const logout = () => {
    token.value = ''
    userInfo.value = null
    uni.removeStorageSync('token')
    uni.removeStorageSync('userInfo')
    uni.navigateTo({ url: '/pages/login/index' })
  }
  
  const checkLogin = () => {
    if (!isLogin.value) {
      uni.navigateTo({ url: '/pages/login/index' })
      return false
    }
    return true
  }
  
  return {
    token,
    userInfo,
    isLogin,
    setToken,
    setUserInfo,
    login,
    wxLogin,
    register,
    getUserInfo,
    updateUserInfo,
    logout,
    checkLogin
  }
})
