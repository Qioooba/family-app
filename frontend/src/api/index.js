import { request } from '../utils/request'
import { taskApi } from './task.js'
import { wishApi } from './wish.js'
import { recipeApi } from './recipe.js'
import { anniversaryApi } from './anniversary.js'
import { voteApi } from './vote.js'
import { familyApi } from './family.js'
import { shoppingApi } from './shopping.js'
import { gameApi } from './game.js'
import { scheduleApi } from './schedule.js'
import { waterApi } from './water.js'
import { aiShoppingApi } from './ai-shopping.js'

export { taskApi, wishApi, recipeApi, anniversaryApi, voteApi, familyApi, shoppingApi, gameApi, scheduleApi, waterApi, aiShoppingApi }

// 食材相关API
export const ingredientApi = {
  // 获取食材列表
  getList: (familyId) => request.get(`/api/ingredient/list/${familyId}`),
  
  // 添加食材
  add: (data) => request.post('/api/ingredient/add', data),
  
  // 更新食材
  update: (data) => request.put('/api/ingredient/update', data),
  
  // 图像识别食材
  recognize: (imageBase64) => request.post('/api/ingredient/recognize', { imageBase64 }),
  
  // 获取即将过期食材
  getExpiring: (familyId) => request.get(`/api/ingredient/expiring/${familyId}`),
  
  // 删除食材
  delete: (id) => request.delete(`/api/ingredient/${id}`)
}

// 饮食记录相关API
export const dietApi = {
  // 添加记录
  addRecord: (data) => request.post('/api/health/diet/record', data),
  
  // 获取某日记录
  getDayRecords: (userId, date) => request.get('/api/health/diet/day', { userId, date }),
  
  // 获取某日统计
  getDayStatistics: (userId, date) => request.get('/api/health/diet/statistics', { userId, date }),
  
  // 获取本周统计
  getWeekStatistics: (userId) => request.get('/api/health/diet/weekly', { userId }),
  
  // 识别食物
  recognizeFood: (imageBase64) => request.post('/api/health/diet/recognize', { imageBase64 }),
  
  // AI计算卡路里
  calculateCalories: (data) => request.post('/api/health/diet/calculate', data),
  
  // 删除记录
  delete: (id) => request.delete(`/api/health/diet/${id}`),
  
  // 记录体重 - 使用营养模块的体重记录
  recordWeight: (data) => request.post('/api/health/nutrition/weight', data),
  
  // 获取体重历史
  getWeightHistory: () => request.get('/api/health/nutrition/weight/history'),
  
  // 获取最新体重
  getLatestWeight: () => request.get('/api/health/nutrition/weight/latest')
}

// 用户相关API
export const userApi = {
  // 注册
  register: (data) => request.post('/api/user/register', data),
  
  // 登录
  login: (data) => request.post('/api/user/login', data),
  
  // 获取当前用户信息
  getCurrentUser: () => request.get('/api/user/info'),
  
  // 获取用户信息
  getUserById: (id) => request.get(`/api/user/${id}`),
  
  // 更新用户信息
  updateUser: (data) => request.put('/api/user/info', data),
  
  // 修改密码
  changePassword: (oldPassword, newPassword) => 
    request.post('/api/user/password', { oldPassword, newPassword }),
  
  // 发送验证码
  sendSmsCode: (phone) => request.post('/api/user/sms-code', { phone })
}
