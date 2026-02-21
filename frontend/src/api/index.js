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
import { couponApi } from './coupon.js'

export { taskApi, wishApi, recipeApi, anniversaryApi, voteApi, familyApi, shoppingApi, gameApi, scheduleApi, waterApi, couponApi }

// 食材相关API
export const ingredientApi = {
  // 获取食材列表
  getList: (familyId) => request.get(`/ingredient/list/${familyId}`),
  
  // 添加食材
  add: (data) => request.post('/ingredient/add', data),
  
  // 更新食材
  update: (data) => request.put('/ingredient/update', data),
  
  // 图像识别食材
  recognize: (imageBase64) => request.post('/ingredient/recognize', { imageBase64 }),
  
  // 获取即将过期食材
  getExpiring: (familyId) => request.get(`/ingredient/expiring/${familyId}`),
  
  // 删除食材
  delete: (id) => request.delete(`/ingredient/${id}`)
}

// 饮食记录相关API
export const dietApi = {
  // 添加记录
  addRecord: (data) => request.post('/diet/record', data),
  
  // 获取某日记录
  getDayRecords: (userId, date) => request.get(`/diet/day/${userId}`, { date }),
  
  // 获取某日统计
  getDayStatistics: (userId, date) => request.get(`/diet/statistics/day/${userId}`, { date }),
  
  // 获取本周统计
  getWeekStatistics: (userId) => request.get(`/diet/statistics/week/${userId}`),
  
  // 识别食物
  recognizeFood: (imageBase64) => request.post('/diet/recognize', { imageBase64 }),
  
  // 删除记录
  delete: (id) => request.delete(`/diet/${id}`),
  
  // 记录体重
  recordWeight: (data) => request.post('/diet/weight/record', data),
  
  // 获取体重历史
  getWeightHistory: () => request.get('/diet/weight/history'),
  
  // 获取最新体重
  getLatestWeight: () => request.get('/diet/weight/latest')
}

// 用户相关API
export const userApi = {
  // 注册
  register: (data) => request.post('/user/register', data),
  
  // 登录
  login: (data) => request.post('/user/login', data),
  
  // 获取当前用户信息
  getCurrentUser: () => request.get('/user/info'),
  
  // 获取用户信息
  getUserById: (id) => request.get(`/user/${id}`),
  
  // 更新用户信息
  updateUser: (data) => request.put('/user/info', data),
  
  // 修改密码
  changePassword: (oldPassword, newPassword) => 
    request.post('/user/password', { oldPassword, newPassword }),
  
  // 发送验证码
  sendSmsCode: (phone) => request.post('/user/sms-code', { phone })
}
