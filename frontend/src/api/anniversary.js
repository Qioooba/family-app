import { request } from '../utils/request'

/**
 * 纪念日相关API
 * 对应后端: AnniversaryController.java
 * BasePath: /api/anniversary
 */
export const anniversaryApi = {
  /**
   * 获取纪念日列表
   * @param {number} familyId - 家庭ID
   * @returns {Promise<Array>} 纪念日列表
   */
  getList: (familyId) => request.get(`/api/anniversary/list/${familyId}`),
  
  /**
   * 获取即将到期纪念日
   * @param {number} familyId - 家庭ID
   * @param {number} days - 天数范围(默认30天)
   * @returns {Promise<Array>} 即将到期的纪念日列表
   */
  getUpcoming: (familyId, days = 30) => request.get(`/api/anniversary/upcoming/${familyId}`, { days }),
  
  /**
   * 创建纪念日
   * @param {object} data - 纪念日数据
   * @returns {Promise<object>} 创建的纪念日
   */
  create: (data) => request.post('/api/anniversary/create', data),
  
  /**
   * 获取今日倒计时
   * @param {number} familyId - 家庭ID
   * @returns {Promise<Array>} 今日相关的纪念日
   */
  getToday: (familyId) => request.get(`/api/anniversary/today/${familyId}`),
  
  /**
   * 删除纪念日
   * @param {number} id - 纪念日ID
   * @returns {Promise<void>}
   */
  delete: (id) => request.delete(`/api/anniversary/${id}`)
}

export default anniversaryApi
