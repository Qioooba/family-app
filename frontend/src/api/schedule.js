import { request } from '../utils/request'

/**
 * 家务排班表相关API
 * 对应后端: ScheduleController.java
 * BasePath: /schedule
 */
export const scheduleApi = {
  /**
   * 创建排班
   * @param {object} data - 排班数据 {familyId, taskName, assigneeId, scheduleType, scheduleDay}
   * @returns {Promise<number>} 排班ID
   */
  create: (data) => request.post('/schedule/create', data),
  
  /**
   * 获取排班列表
   * @param {number} familyId - 家庭ID
   * @returns {Promise<Array>} 排班列表
   */
  getList: (familyId) => request.get(`/schedule/list/${familyId}`),
  
  /**
   * 获取今日排班
   * @param {number} familyId - 家庭ID
   * @returns {Promise<Array>} 今日排班列表
   */
  getToday: (familyId) => request.get(`/schedule/today/${familyId}`),
  
  /**
   * 删除排班
   * @param {number} id - 排班ID
   * @returns {Promise<boolean>}
   */
  delete: (id) => request.delete(`/schedule/${id}`)
}

export default scheduleApi
