import { request } from '../utils/request'

/**
 * 心愿相关API
 * 对应后端: WishController.java
 * BasePath: /wish
 */
export const wishApi = {
  /**
   * 获取心愿列表
   * @param {number} familyId - 家庭ID
   * @param {object} params - 可选参数: type, status
   * @returns {Promise<Array>} 心愿列表
   */
  getList: (familyId, params = {}) => request.get('/wish/list', { familyId, ...params }),
  
  /**
   * 创建心愿
   * @param {object} data - 心愿数据
   * @returns {Promise<object>} 创建的心愿
   */
  create: (data) => request.post('/wish/create', data),
  
  /**
   * 更新心愿
   * @param {object} data - 心愿数据(需包含id)
   * @returns {Promise<object>} 更新后的心愿
   */
  update: (data) => request.put('/wish/update', data),
  
  /**
   * 认领心愿
   * @param {number} wishId - 心愿ID
   * @param {number} userId - 认领用户ID
   * @returns {Promise<void>}
   */
  claim: (wishId, userId) => request.post(`/wish/claim/${wishId}`, { userId }),
  
  /**
   * 更新进度
   * @param {number} wishId - 心愿ID
   * @param {number} progress - 进度(0-100)
   * @returns {Promise<void>}
   */
  updateProgress: (wishId, progress) => request.post(`/wish/progress/${wishId}`, { progress }),
  
  /**
   * 完成心愿
   * @param {number} wishId - 心愿ID
   * @returns {Promise<void>}
   */
  complete: (wishId) => request.post(`/wish/complete/${wishId}`),
  
  /**
   * 放弃心愿
   * @param {number} wishId - 心愿ID
   * @returns {Promise<void>}
   */
  abandon: (wishId) => request.post(`/wish/abandon/${wishId}`),
  
  /**
   * 删除心愿
   * @param {number} id - 心愿ID
   * @returns {Promise<void>}
   */
  delete: (id) => request.delete(`/wish/${id}`)
}

export default wishApi
