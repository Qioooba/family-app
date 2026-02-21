import { request } from '../utils/request'

/**
 * 投票相关API
 * 对应后端: VoteController.java
 * BasePath: /api/vote
 */
export const voteApi = {
  /**
   * 获取投票列表
   * @param {number} familyId - 家庭ID
   * @param {number} status - 状态(0进行中,1已结束)
   * @returns {Promise<Array>} 投票列表
   */
  getList: (familyId, status) => request.get(`/api/vote/list/${familyId}`, { status }),
  
  /**
   * 创建投票
   * @param {object} data - 投票数据
   * @returns {Promise<object>} 创建的投票
   */
  create: (data) => request.post('/api/vote/create', data),
  
  /**
   * 参与投票
   * @param {number} voteId - 投票ID
   * @param {number} userId - 用户ID
   * @param {Array} options - 选项索引数组
   * @returns {Promise<void>}
   */
  doVote: (voteId, userId, options) => request.post(`/api/vote/do/${voteId}`, { userId, options }),
  
  /**
   * 结束投票
   * @param {number} voteId - 投票ID
   * @returns {Promise<void>}
   */
  end: (voteId) => request.post(`/api/vote/end/${voteId}`),
  
  /**
   * 获取投票结果
   * @param {number} voteId - 投票ID
   * @returns {Promise<object>} 投票结果
   */
  getResult: (voteId) => request.get(`/api/vote/result/${voteId}`),
  
  /**
   * 删除投票
   * @param {number} id - 投票ID
   * @returns {Promise<void>}
   */
  delete: (id) => request.delete(`/api/vote/${id}`)
}

export default voteApi
