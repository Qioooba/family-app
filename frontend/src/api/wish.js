import { request } from '../utils/request'

/**
 * 心愿墙相关API
 */
export const wishApi = {
  /**
   * 获取心愿列表
   * @param {number} familyId - 家庭ID
   * @returns {Promise<Array>}
   */
  getList: (familyId) => {
    return request.get('/api/wish/list', { familyId }).then(res => {
      if (res && Array.isArray(res)) {
        return res
      }
      return res?.data || []
    })
  },
  
  /**
   * 获取我的心愿
   * @param {number} familyId - 家庭ID
   * @returns {Promise<Array>}
   */
  getMyList: (familyId) => {
    return request.get('/api/wish/my', { familyId }).then(res => {
      if (res && Array.isArray(res)) {
        return res
      }
      return res?.data || []
    })
  },
  
  /**
   * 获取心愿详情
   * @param {number} id - 心愿ID
   * @returns {Promise<Object>}
   */
  getDetail: (id) => {
    return request.get('/api/wish/detail', { id }).then(res => {
      return res?.data || res
    })
  },
  
  /**
   * 创建心愿
   * @param {Object} data - 心愿数据
   * @returns {Promise<Object>}
   */
  create: (data) => {
    return request.post('/api/wish/create', data)
  },
  
  /**
   * 更新心愿
   * @param {Object} data - 心愿数据
   * @returns {Promise<Object>}
   */
  update: (data) => {
    return request.put('/api/wish/update', data)
  },
  
  /**
   * 删除心愿
   * @param {number} id - 心愿ID
   * @returns {Promise}
   */
  delete: (id) => {
    return request.delete(`/api/wish/${id}`)
  },
  
  /**
   * 认领心愿
   * @param {number} wishId - 心愿ID
   * @returns {Promise<Object>}
   */
  claim: (wishId) => {
    return request.post(`/api/wish/claim/${wishId}`, {})
  },
  
  /**
   * 取消认领
   * @param {number} wishId - 心愿ID
   * @returns {Promise<Object>}
   */
  cancelClaim: (wishId) => {
    return request.post(`/api/wish/cancel/${wishId}`, {})
  },
  
  /**
   * 更新进度
   * @param {number} wishId - 心愿ID
   * @param {Object} data - 进度数据 { progress }
   * @returns {Promise<Object>}
   */
  updateProgress: (wishId, data) => {
    return request.post(`/api/wish/progress/${wishId}`, data)
  },
  
  /**
   * 完成心愿
   * @param {number} wishId - 心愿ID
   * @returns {Promise<Object>}
   */
  complete: (wishId) => {
    return request.post(`/api/wish/complete/${wishId}`)
  },
  
  /**
   * 放弃心愿
   * @param {number} wishId - 心愿ID
   * @returns {Promise<Object>}
   */
  abandon: (wishId) => {
    return request.post(`/api/wish/abandon/${wishId}`)
  }
}

export default wishApi
