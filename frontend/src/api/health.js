import { request } from '../utils/request'

/**
 * 健康相关API
 * 体重记录相关API
 * BasePath: /api/diet/weight
 */
export const healthApi = {
  /**
   * 记录体重
   * POST /api/diet/weight/record
   * @param {object} data - {weight, date, note}
   * @returns {Promise<object>} 记录结果
   */
  recordWeight: (data) => request.post('/api/diet/weight/record', data),

  /**
   * 获取体重历史记录
   * GET /api/diet/weight/history
   * @returns {Promise<object>} 历史记录列表
   */
  getWeightHistory: () => request.get('/api/diet/weight/history'),

  /**
   * 获取最新体重
   * GET /api/diet/weight/latest
   * @returns {Promise<object>} 最新体重记录
   */
  getLatestWeight: () => request.get('/api/diet/weight/latest'),

  /**
   * 设置体重目标（此接口后端未实现）
   * POST /api/health/weight/target
   * @param {object} data - {targetWeight}
   * @returns {Promise<object>} 设置结果
   */
  setWeightTarget: (data) => request.post('/api/health/weight/target', data),

  /**
   * 获取体重目标（此接口后端未实现）
   * GET /api/health/weight/target
   * @returns {Promise<object>} 体重目标
   */
  getWeightTarget: () => request.get('/api/health/weight/target')
}

export default healthApi
