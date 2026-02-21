import { request } from '../utils/request'

/**
 * 游戏模块相关API
 * 对应后端: GameController.java
 * BasePath: /api/game
 */
export const gameApi = {
  /**
   * 创建转盘
   * @param {object} request - 转盘数据
   * @returns {Promise<number>} 转盘ID
   */
  createWheel: (data) => request.post('/api/game/wheel/create', data),
  
  /**
   * 获取转盘列表
   * @param {number} familyId - 家庭ID
   * @returns {Promise<Array>} 转盘列表
   */
  getWheels: (familyId) => request.get(`/api/game/wheel/${familyId}`),
  
  /**
   * 转动转盘
   * @param {number} wheelId - 转盘ID
   * @returns {Promise<string>} 中奖结果
   */
  spinWheel: (wheelId) => request.post(`/api/game/wheel/${wheelId}/spin`),
  
  /**
   * 获取用户积分
   * @returns {Promise<object>} 积分信息
   */
  getUserPoints: () => request.get('/api/game/points'),
  
  /**
   * 获取积分历史
   * @returns {Promise<Array>} 积分记录列表
   */
  getPointsHistory: () => request.get('/api/game/points/history'),
  
  /**
   * 获取排行榜
   * @param {number} familyId - 家庭ID
   * @param {string} type - 排行类型(points)
   * @returns {Promise<Array>} 排行榜数据
   */
  getRankings: (familyId, type = 'points') => request.get(`/api/game/rankings/${familyId}`, { type }),
  
  /**
   * 获取成就列表
   * @returns {Promise<Array>} 成就列表
   */
  getAchievements: () => request.get('/api/game/achievements'),
  
  /**
   * 添加积分
   * @param {object} request - 积分数据
   * @returns {Promise<boolean>}
   */
  addPoints: (data) => request.post('/api/game/points/add', data)
}

export default gameApi
