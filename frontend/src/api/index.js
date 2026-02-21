import { request } from '../utils/request'
import { taskApi } from './task.js'

export { taskApi }

// 心愿相关API
export const wishApi = {
  // 获取心愿列表
  getList: (familyId, params = {}) => request.get('/wish/list', { familyId, ...params }),
  
  // 创建心愿
  create: (data) => request.post('/wish/create', data),
  
  // 更新心愿
  update: (data) => request.put('/wish/update', data),
  
  // 认领心愿
  claim: (wishId, userId) => request.post(`/wish/claim/${wishId}`, null, { params: { userId } }),
  
  // 更新进度
  updateProgress: (wishId, progress) => request.post(`/wish/progress/${wishId}`, null, { params: { progress } }),
  
  // 完成心愿
  complete: (wishId) => request.post(`/wish/complete/${wishId}`),
  
  // 放弃心愿
  abandon: (wishId) => request.post(`/wish/abandon/${wishId}`),
  
  // 删除心愿
  delete: (id) => request.delete(`/wish/${id}`)
}

// 菜谱相关API
export const recipeApi = {
  // 搜索菜谱
  search: (params) => request.get('/recipe/search', params),
  
  // 获取家庭菜谱
  getFamilyRecipes: (familyId) => request.get(`/recipe/family/${familyId}`),
  
  // AI推荐菜谱
  recommend: (ingredients) => request.post('/recipe/recommend', ingredients),
  
  // 创建菜谱
  create: (data) => request.post('/recipe/create', data),
  
  // 获取菜谱详情
  getById: (id) => request.get(`/recipe/${id}`),
  
  // 收藏菜谱
  favorite: (id, userId) => request.post(`/recipe/favorite/${id}`, null, { params: { userId } }),
  
  // 记录做过
  recordCooking: (id, userId) => request.post(`/recipe/cook/${id}`, null, { params: { userId } })
}

// 食材相关API
export const ingredientApi = {
  // 获取食材列表
  getList: (familyId) => request.get(`/ingredient/list/${familyId}`),
  
  // 添加食材
  add: (data) => request.post('/ingredient/add', data),
  
  // 更新食材
  update: (data) => request.put('/ingredient/update', data),
  
  // 图像识别食材
  recognize: (imageBase64) => request.post('/ingredient/recognize', null, { params: { imageBase64 } }),
  
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
  recognizeFood: (imageBase64) => request.post('/diet/recognize', null, { params: { imageBase64 } }),
  
  // 删除记录
  delete: (id) => request.delete(`/diet/${id}`)
}

// 纪念日相关API
export const anniversaryApi = {
  // 获取纪念日列表
  getList: (familyId) => request.get(`/anniversary/list/${familyId}`),
  
  // 获取即将到期纪念日
  getUpcoming: (familyId, days = 30) => request.get(`/anniversary/upcoming/${familyId}`, { days }),
  
  // 创建纪念日
  create: (data) => request.post('/anniversary/create', data),
  
  // 获取今日倒计时
  getToday: (familyId) => request.get(`/anniversary/today/${familyId}`),
  
  // 删除纪念日
  delete: (id) => request.delete(`/anniversary/${id}`)
}

// 投票相关API
export const voteApi = {
  // 获取投票列表
  getList: (familyId, status) => request.get(`/vote/list/${familyId}`, { status }),
  
  // 创建投票
  create: (data) => request.post('/vote/create', data),
  
  // 参与投票
  doVote: (voteId, userId, options) => request.post(`/vote/do/${voteId}`, options, { params: { userId } }),
  
  // 结束投票
  end: (voteId) => request.post(`/vote/end/${voteId}`),
  
  // 获取结果
  getResult: (voteId) => request.get(`/vote/result/${voteId}`),
  
  // 删除投票
  delete: (id) => request.delete(`/vote/${id}`)
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
    request.post('/user/password', null, { params: { oldPassword, newPassword } }),
  
  // 发送验证码
  sendSmsCode: (phone) => request.post('/user/sms-code', null, { params: { phone } })
}
