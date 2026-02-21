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

export { taskApi, wishApi, recipeApi, anniversaryApi, voteApi, familyApi, shoppingApi, gameApi, scheduleApi, waterApi }

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
  addRecord: (data) => request.post('/api/diet/record', data),
  
  // 获取某日记录
  getDayRecords: (userId, date) => request.get(`/api/diet/day/${userId}`, { date }),
  
  // 获取某日统计
  getDayStatistics: (userId, date) => request.get(`/api/diet/statistics/day/${userId}`, { date }),
  
  // 获取本周统计
  getWeekStatistics: (userId) => request.get(`/api/diet/statistics/week/${userId}`),
  
  // 识别食物
  recognizeFood: (imageBase64) => request.post('/api/diet/recognize', { imageBase64 }),
  
  // 删除记录
  delete: (id) => request.delete(`/api/diet/${id}`),
  
  // 记录体重
  recordWeight: (data) => request.post('/api/diet/weight/record', data),
  
  // 获取体重历史
  getWeightHistory: () => request.get('/api/diet/weight/history'),
  
  // 获取最新体重
  getLatestWeight: () => request.get('/api/diet/weight/latest')
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
