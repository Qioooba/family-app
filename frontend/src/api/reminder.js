import { request } from '../utils/request'

/**
 * 提醒相关API
 * 对应后端: FamilyReminderController.java
 * BasePath: /api/reminder
 */
export const reminderApi = {
  /**
   * 获取今日提醒
   * @returns {Promise<Array>} 今日提醒列表
   */
  getToday: () => request.get('/api/reminder/today'),

  /**
   * 获取我的提醒列表
   * @returns {Promise<Array>} 提醒列表
   */
  getList: () => request.get('/api/reminder/list'),

  /**
   * 获取提醒详情
   * @param {number} id - 提醒ID
   * @returns {Promise<object>} 提醒详情
   */
  getDetail: (id) => request.get(`/api/reminder/detail/${id}`),

  /**
   * 创建提醒
   * @param {object} data - 提醒数据
   * @returns {Promise<object>} 创建结果
   */
  add: (data) => request.post('/api/reminder/add', data),

  /**
   * 更新提醒
   * @param {object} data - 提醒数据
   * @returns {Promise<object>} 更新结果
   */
  update: (data) => request.post('/api/reminder/update', data),

  /**
   * 删除提醒
   * @param {number} id - 提醒ID
   * @returns {Promise<void>}
   */
  delete: (id) => request.post('/api/reminder/delete', { id }),

  /**
   * 切换提醒状态
   * @param {number} id - 提醒ID
   * @returns {Promise<object>} 操作结果
   */
  toggle: (id) => request.post('/api/reminder/toggle', { id }),

  /**
   * 手动执行提醒
   * @param {number} id - 提醒ID
   * @returns {Promise<object>} 执行结果
   */
  execute: (id) => request.post('/api/reminder/execute', { id }),

  /**
   * 获取频率类型列表
   * @returns {Promise<Array>} 频率类型
   */
  getFrequencyTypes: () => request.get('/api/reminder/frequency-types'),

  /**
   * 获取提醒类型列表
   * @returns {Promise<Array>} 提醒类型
   */
  getReminderTypes: () => request.get('/api/reminder/reminder-types'),

  /**
   * 获取推送范围选项
   * @returns {Promise<Array>} 推送范围选项
   */
  getPushScopes: () => request.get('/api/reminder/push-scopes'),

  /**
   * 获取家庭成员列表
   * @param {number} familyId - 家庭ID
   * @returns {Promise<Array>} 成员列表
   */
  getFamilyMembers: (familyId) => request.get(`/api/family/${familyId}/members`),

  /**
   * 获取当前用户信息
   * @returns {Promise<object>} 当前用户
   */
  getCurrentUser: () => request.get('/api/reminder/current-user'),

  /**
   * 获取场景模板列表
   * @returns {Promise<Array>} 场景模板
   */
  getSceneTemplates: () => request.get('/api/reminder/scene-templates'),

  /**
   * 创建场景提醒
   * @param {object} data - 场景数据
   * @returns {Promise<object>} 创建结果
   */
  createScene: (data) => request.post('/api/reminder/scene/create', data),

  /**
   * 切换场景提醒状态
   * @param {number} sceneId - 场景ID
   * @returns {Promise<object>} 操作结果
   */
  toggleScene: (sceneId) => request.post('/api/reminder/scene/toggle', { sceneId }),

  /**
   * 获取场景配置
   * @param {string} sceneType - 场景类型
   * @returns {Promise<object>} 场景配置
   */
  getSceneConfig: (sceneType) => request.get(`/api/reminder/scene/config/${sceneType}`),

  /**
   * 更新场景配置
   * @param {object} data - 场景数据
   * @returns {Promise<object>} 更新结果
   */
  updateScene: (data) => request.post('/api/reminder/scene/update', data),

  /**
   * 获取位置列表
   * @param {number} familyId - 家庭ID
   * @returns {Promise<Array>} 位置列表
   */
  getLocations: (familyId) => request.get('/api/reminder/scene/location', { familyId }),

  /**
   * 添加位置
   * @param {object} data - 位置数据
   * @returns {Promise<object>} 添加结果
   */
  addLocation: (data) => request.post('/api/reminder/scene/location', data)
}

export default reminderApi
