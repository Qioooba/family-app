/**
 * 默认家庭管理工具
 * 单家庭模式：自动获取或创建默认家庭"幸福小家"
 */

import { request } from '../utils/request'

const DEFAULT_FAMILY_NAME = '幸福小家'

/**
 * 获取默认家庭ID，如果不存在则自动创建
 * @returns {Promise<{id: number, name: string}>}
 */
export const getDefaultFamily = async () => {
  // 1. 首先尝试从本地存储获取
  const storedFamilyId = uni.getStorageSync('currentFamilyId')
  const storedFamily = uni.getStorageSync('currentFamily')
  
  if (storedFamily) {
    try {
      const family = typeof storedFamily === 'string' ? JSON.parse(storedFamily) : storedFamily
      if (family.id) {
        console.log('[DefaultFamily] 从存储获取到家庭:', family)
        return family
      }
    } catch (e) {
      console.error('[DefaultFamily] 解析家庭信息失败:', e)
    }
  }
  
  if (storedFamilyId) {
    console.log('[DefaultFamily] 从存储获取到familyId:', storedFamilyId)
    return { id: storedFamilyId, name: DEFAULT_FAMILY_NAME }
  }
  
  // 2. 尝试从后端获取用户家庭列表
  try {
    console.log('[DefaultFamily] 从后端获取家庭列表...')
    const res = await request.get('/api/family/list')
    
    let families = []
    if (Array.isArray(res)) {
      families = res
    } else if (res?.data && Array.isArray(res.data)) {
      families = res.data
    }
    
    if (families.length > 0) {
      // 使用第一个家庭作为默认家庭
      const defaultFamily = {
        id: families[0].id,
        name: families[0].name || DEFAULT_FAMILY_NAME
      }
      saveDefaultFamily(defaultFamily)
      console.log('[DefaultFamily] 从后端获取到默认家庭:', defaultFamily)
      return defaultFamily
    }
  } catch (err) {
    console.error('[DefaultFamily] 获取家庭列表失败:', err)
  }
  
  // 3. 没有家庭，创建默认家庭"幸福小家"
  try {
    console.log('[DefaultFamily] 正在创建默认家庭...')
    const userInfo = uni.getStorageSync('userInfo')
    const userId = userInfo?.id || userInfo?.userId
    
    if (!userId) {
      console.warn('[DefaultFamily] 用户未登录，无法创建家庭')
      // 返回一个临时 ID，稍后登录后会自动获取
      return { id: 1, name: DEFAULT_FAMILY_NAME }
    }
    
    const createData = {
      name: DEFAULT_FAMILY_NAME
    }
    
    const res = await request.post('/api/family/create', createData, { params: { creatorId: userId } })
    console.log('[DefaultFamily] 创建家庭成功:', res)
    
    const newFamily = {
      id: res.id,
      name: res.name || DEFAULT_FAMILY_NAME
    }
    saveDefaultFamily(newFamily)
    return newFamily
  } catch (err) {
    console.error('[DefaultFamily] 创建默认家庭失败:', err)
    // 返回临时 ID
    return { id: 1, name: DEFAULT_FAMILY_NAME }
  }
}

/**
 * 保存默认家庭到存储
 * @param {object} family 
 */
export const saveDefaultFamily = (family) => {
  if (!family) return
  
  uni.setStorageSync('currentFamilyId', family.id)
  uni.setStorageSync('currentFamily', JSON.stringify(family))
  console.log('[DefaultFamily] 保存默认家庭:', family)
}

/**
 * 获取当前家庭ID（兼容旧代码）
 * @returns {number|null}
 */
export const getCurrentFamilyId = () => {
  return uni.getStorageSync('currentFamilyId') || null
}

/**
 * 清除当前家庭（退出登录时使用）
 */
export const clearCurrentFamily = () => {
  uni.removeStorageSync('currentFamilyId')
  uni.removeStorageSync('currentFamily')
}
