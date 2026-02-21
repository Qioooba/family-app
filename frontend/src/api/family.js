import { request } from '../utils/request'

/**
 * 家庭圈相关API
 * 对应后端: FamilyController.java
 * BasePath: /family
 */
export const familyApi = {
  /**
   * 创建家庭
   * @param {object} data - 家庭数据
   * @param {number} creatorId - 创建者ID
   * @returns {Promise<object>} 创建的家庭
   */
  create: (data, creatorId) => request.post('/family/create', data, { params: { creatorId } }),
  
  /**
   * 加入家庭
   * @param {string} inviteCode - 邀请码
   * @param {number} userId - 用户ID
   * @returns {Promise<object>} 加入的家庭
   */
  join: (inviteCode, userId) => request.post('/family/join', { inviteCode, userId }),
  
  /**
   * 获取家庭详情
   * @param {number} id - 家庭ID
   * @returns {Promise<object>} 家庭详情
   */
  getById: (id) => request.get(`/family/${id}`),
  
  /**
   * 获取家庭成员
   * @param {number} id - 家庭ID
   * @returns {Promise<Array>} 成员列表
   */
  getMembers: (id) => request.get(`/family/${id}/members`),
  
  /**
   * 移除成员
   * @param {number} id - 家庭ID
   * @param {number} userId - 要移除的用户ID
   * @returns {Promise<void>}
   */
  removeMember: (id, userId) => request.post(`/family/${id}/remove`, { userId })
}

export default familyApi
