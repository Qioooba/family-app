import { request } from '../utils/request'

/**
 * 家庭圈相关API
 * 对应后端: FamilyController.java + InviteCodeController.java
 * BasePath: /api/family
 */
export const familyApi = {
  /**
   * 创建家庭（第一个用户创建自动成为家长）
   * @param {object} data - 家庭数据 {name}
   * @param {number} creatorId - 创建者ID
   * @returns {Promise<object>} 创建的家庭
   */
  create: (data, creatorId) => request.post('/api/family/create', { ...data, creatorId }),
  
  /**
   * 使用邀请码加入家庭
   * @param {string} code - 邀请码
   * @param {number} userId - 用户ID
   * @returns {Promise<object>} 加入的家庭
   */
  joinByCode: (code, userId) => request.post('/api/family/join-by-code', { code, userId }),
  
  /**
   * 验证邀请码
   * @param {string} code - 邀请码
   * @returns {Promise<object>} 验证结果
   */
  verifyCode: (code) => request.post('/api/family/verify-code', { code }),
  
  /**
   * 创建邀请码（家长/管理员）
   * @param {number} familyId - 家庭ID
   * @param {number} creatorId - 创建者ID
   * @param {number} maxUses - 最大使用次数
   * @param {number} expireDays - 过期天数
   * @returns {Promise<object>} 创建的邀请码
   */
  createInviteCode: (familyId, creatorId, maxUses = 5, expireDays = 30) => 
    request.post('/api/family/invite-code', { familyId, creatorId, maxUses, expireDays }),
  
  /**
   * 获取家庭邀请码列表
   * @param {number} familyId - 家庭ID
   * @returns {Promise<Array>} 邀请码列表
   */
  getInviteCodes: (familyId) => request.get(`/api/family/${familyId}/invite-codes`),
  
  /**
   * 禁用邀请码
   * @param {number} codeId - 邀请码ID
   * @returns {Promise<void>}
   */
  disableInviteCode: (codeId) => request.delete(`/api/family/invite-code/${codeId}`),
  
  /**
   * 检查用户是否为管理员
   * @param {number} familyId - 家庭ID
   * @param {number} userId - 用户ID
   * @returns {Promise<boolean>} 是否为管理员
   */
  checkAdmin: (familyId, userId) => request.get(`/api/family/${familyId}/check-admin/${userId}`),
  
  /**
   * 获取家庭详情
   * @param {number} id - 家庭ID
   * @returns {Promise<object>} 家庭详情
   */
  getById: (id) => request.get(`/api/family/info?familyId=${id}`),
  
  /**
   * 获取家庭成员
   * @param {number} id - 家庭ID
   * @returns {Promise<Array>} 成员列表
   */
  getMembers: (id) => request.get(`/api/family/${id}/members`),
  
  /**
   * 移除成员（管理员操作）
   * @param {number} familyId - 家庭ID
   * @param {number} userId - 要移除的用户ID
   * @returns {Promise<void>}
   */
  removeMember: (familyId, userId) => request.delete(`/api/family/${familyId}/members/${userId}`),
  
  /**
   * 更新家庭信息
   * @param {object} data - 家庭数据
   * @returns {Promise<void>}
   */
  update: (data) => request.put('/api/family/update', data),
  
  /**
   * 获取家庭列表
   * @returns {Promise<Array>} 家庭列表
   */
  list: () => request.get('/api/family/list'),
  
  /**
   * 切换当前家庭
   * @param {number} familyId - 家庭ID
   * @returns {Promise<void>}
   */
  switchFamily: (familyId) => request.post(`/api/family/switch/${familyId}`),
  
  /**
   * 获取家庭统计数据
   * @param {number} familyId - 家庭ID
   * @returns {Promise<object>} 统计数据
   */
  getStatistics: (familyId) => request.get(`/api/family/${familyId}/statistics`)
}

export default familyApi
