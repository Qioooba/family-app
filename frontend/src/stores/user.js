import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { request } from '../utils/request'

export const useUserStore = defineStore('user', () => {
  // State
  const token = ref(uni.getStorageSync('token') || '')
  const userInfo = ref(uni.getStorageSync('userInfo') || null)
  const isLogin = computed(() => !!token.value)
  
  // Actions
  const setToken = (val) => {
    token.value = val
    uni.setStorageSync('token', val)
  }
  
  const setUserInfo = (val) => {
    userInfo.value = val
    uni.setStorageSync('userInfo', val)
  }
  
  const login = async (loginData) => {
    const res = await request.post('/api/user/login', loginData)
    setToken(res)
    await getUserInfo()
    return res
  }
  
  const register = async (registerData) => {
    const res = await request.post('/api/user/register', registerData)
    return res
  }
  
  const getUserInfo = async () => {
    try {
      const res = await request.get('/api/user/info')
      setUserInfo(res)
      return res
    } catch (e) {
      return null
    }
  }
  
  const updateUserInfo = async (data) => {
    const res = await request.put('/api/user/info', data)
    setUserInfo(res)
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
    register,
    getUserInfo,
    updateUserInfo,
    logout,
    checkLogin
  }
})
