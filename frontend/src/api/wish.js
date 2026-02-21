import { request } from '../utils/request'

/**
 * 心愿相关API
 * 对应后端: WishController.java
 * BasePath: /api/wish
 */
export const wishApi = {
  /**
   * 获取心愿列表
   * @param {number} familyId - 家庭ID
   * @param {object} params - 可选参数: type, status
   * @returns {Promise<Array>} 心愿列表
   */
  getList: (familyId, params = {}) => request.get('/api/wish/list', { familyId, ...params }),
  
  /**
   * 创建心愿
   * @param {object} data - 心愿数据
   * @returns {Promise<object>} 创建的心愿
   */
  create: (data) => request.post('/api/wish/create', data),
  
  /**
   * 更新心愿
   * @param {object} data - 心愿数据(需包含id)
   * @returns {Promise<object>} 更新后的心愿
   */
  update: (data) => request.put('/api/wish/update', data),
  
  /**
   * 认领心愿
   * @param {number} wishId - 心愿ID
   * @param {number} userId - 认领用户ID
   * @returns {Promise<void>}
   */
  claim: (wishId, userId) => request.post(`/api/wish/claim/${wishId}`, { userId }),
  
  /**
   * 更新进度
   * @param {number} wishId - 心愿ID
   * @param {number} progress - 进度(0-100)
   * @returns {Promise<void>}
   */
  updateProgress: (wishId, progress) => request.post(`/api/wish/progress/${wishId}`, { progress }),
  
  /**
   * 完成心愿
   * @param {number} wishId - 心愿ID
   * @returns {Promise<void>}
   */
  complete: (wishId) => request.post(`/api/wish/complete/${wishId}`),
  
  /**
   * 放弃心愿
   * @param {number} wishId - 心愿ID
   * @returns {Promise<void>}
   */
  abandon: (wishId) => request.post(`/api/wish/abandon/${wishId}`),
  
  /**
   * 删除心愿
   * @param {number} id - 心愿ID
   * @returns {Promise<void>}
   */
  delete: (id) => request.delete(`/api/wish/${id}`),

  /**
   * 设置心愿预算
   * @param {number} id - 心愿ID
   * @param {object} data - {targetAmount, currentAmount, currency}
   * @returns {Promise<void>}
   */
  setBudget: (id, data) => request.post(`/api/wish/${id}/budget`, data),

  /**
   * 获取预算统计
   * @param {number} familyId - 家庭ID
   * @returns {Promise<object>} 预算统计
   */
  getBudgetStats: (familyId) => request.get('/api/wish/budget-stats', { familyId }),

  /**
   * 获取里程碑列表
   * @param {number} id - 心愿ID
   * @returns {Promise<Array>} 里程碑列表
   */
  getMilestones: (id) => request.get(`/api/wish/${id}/milestones`),

  /**
   * 添加里程碑
   * @param {number} id - 心愿ID
   * @param {object} data - {title, targetDate, description}
   * @returns {Promise<object>} 创建的里程碑
   */
  addMilestone: (id, data) => request.post(`/api/wish/${id}/milestone`, data),

  /**
   * 完成里程碑
   * @param {number} milestoneId - 里程碑ID
   * @returns {Promise<void>}
   */
  completeMilestone: (milestoneId) => request.put(`/api/milestone/${milestoneId}/complete`),

  /**
   * 删除里程碑
   * @param {number} milestoneId - 里程碑ID
   * @returns {Promise<void>}
   */
  deleteMilestone: (milestoneId) => request.delete(`/api/milestone/${milestoneId}`)
}

export default wishApi
