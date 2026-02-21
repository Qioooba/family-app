import request from '@/utils/request'

/**
 * 家庭游戏API
 */
export const gameApi = {
  /**
   * 创建转盘
   */
  createWheel(data) {
    return request.post('/api/game/wheel/create', data)
  },

  /**
   * 获取转盘列表
   */
  getWheels(familyId) {
    return request.get(`/api/game/wheel/${familyId}`)
  },

  /**
   * 转动转盘
   */
  spinWheel(wheelId) {
    return request.post(`/api/game/wheel/${wheelId}/spin`)
  },

  /**
   * 获取用户积分
   */
  getUserPoints() {
    return request.get('/api/game/points')
  },

  /**
   * 获取积分历史
   */
  getPointsHistory() {
    return request.get('/api/game/points/history')
  },

  /**
   * 获取排行榜
   */
  getRankings(familyId, type = 'points') {
    return request.get(`/api/game/rankings/${familyId}`, { type })
  },

  /**
   * 获取成就列表
   */
  getAchievements() {
    return request.get('/api/game/achievements')
  },

  /**
   * 增加积分（内部调用）
   */
  addPoints(data) {
    return request.post('/api/game/points/add', data)
  }
}
