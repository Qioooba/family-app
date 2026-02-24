import { request } from '../utils/request'

/**
 * 喝水打卡相关API
 * 对应后端: WaterController.java
 * BasePath: /api/health/water
 */
export const waterApi = {
  /**
   * 记录喝水
   * @param {object} data - {userId, amount, recordTime}
   * @returns {Promise<object>} 记录结果
   */
  record: (data) => request.post('/api/health/water', data),

  /**
   * 获取今日喝水量
   * @param {number} userId - 用户ID
   * @returns {Promise<object>} {todayAmount, targetAmount, percent, records}
   */
  getToday: (userId) => request.get('/api/health/water/today', { userId }),

  /**
   * 删除喝水记录
   * @param {number} id - 记录ID
   * @param {object} options - 请求选项（可选，用于传递 silent 等）
   * @returns {Promise<void>}
   */
  deleteRecord: (id, options = {}) => request.delete(`/api/health/water/${id}`, {}, options),

  /**
   * 获取历史喝水记录
   * @param {number} userId - 用户ID
   * @param {string} date - 日期格式 YYYY-MM-DD
   * @returns {Promise<object>} {todayAmount, targetAmount, percent, records}
   */
  getHistory: (userId, date) => request.get('/api/health/water/history', { userId, date }),

  /**
   * 设置饮水目标
   * @param {number} targetAmount - 目标饮水量（毫升）
   * @returns {Promise<object>} {userId, targetAmount, success}
   */
  setTarget: (targetAmount) => {
    console.log('[WaterAPI] setTarget 被调用，参数:', targetAmount, '类型:', typeof targetAmount)
    const data = { targetAmount: parseInt(targetAmount) || 2000 }
    console.log('[WaterAPI] 请求体:', data)
    return request.post('/api/health/water/target', data)
  },

  /**
   * 获取饮水目标
   * @param {number} userId - 用户ID（可选）
   * @returns {Promise<object>} {userId, targetAmount, defaultTarget}
   */
  getTarget: (userId) => request.get('/api/health/water/target', { userId })
}

export default waterApi