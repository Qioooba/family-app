import { request } from '../utils/request'

/**
 * 健康相关API
 * 体重记录相关API
 * BasePath: /api/health/nutrition/weight
 */
export const healthApi = {
  /**
   * 记录体重
   * POST /api/health/nutrition/weight
   * @param {object} data - {weight, recordDate, note}
   * @returns {Promise<object>} 记录结果
   */
  recordWeight: (data) => request.post('/api/health/nutrition/weight', data),

  /**
   * 获取体重历史记录
   * GET /api/health/nutrition/weight/history
   * @returns {Promise<object>} 历史记录列表
   */
  getWeightHistory: () => request.get('/api/health/nutrition/weight/history'),

  /**
   * 获取最新体重
   * GET /api/health/nutrition/weight/latest
   * @returns {Promise<object>} 最新体重记录
   */
  getLatestWeight: () => request.get('/api/health/nutrition/weight/latest'),

  /**
   * 设置体重目标
   * POST /api/health/weight/target
   * @param {object} data - {targetWeight}
   * @returns {Promise<object>} 设置结果
   */
  setWeightTarget: (data) => request.post('/api/health/weight/target', data),

  /**
   * 获取体重目标
   * GET /api/health/weight/target
   * @returns {Promise<object>} 体重目标
   */
  getWeightTarget: () => request.get('/api/health/weight/target')
}

export default healthApi
