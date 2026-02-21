import { request } from '../utils/request'

/**
 * 喝水打卡相关API
 * 对应后端: WaterController.java
 * BasePath: /api/diet/water
 */
export const waterApi = {
  /**
   * 记录喝水
   * @param {object} data - {userId, amount, recordTime}
   * @returns {Promise<object>} 记录结果
   */
  record: (data) => request.post('/api/diet/water', data),
  
  /**
   * 获取今日喝水量
   * @param {number} userId - 用户ID
   * @returns {Promise<object>} {todayAmount, targetAmount, percent, records}
   */
  getToday: (userId) => request.get('/api/diet/water/today', { userId })
}

export default waterApi