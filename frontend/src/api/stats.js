import request from '@/utils/request'

/**
 * 数据统计API
 */
export const statsApi = {
  /**
   * 获取个人统计
   */
  getPersonalStats(type, date) {
    return request.get('/api/stats/personal', { type, date })
  },

  /**
   * 获取家庭统计
   */
  getFamilyStats(familyId, type) {
    return request.get(`/api/stats/family/${familyId}`, { type })
  },

  /**
   * 获取任务统计
   */
  getTaskStats(startDate, endDate) {
    return request.get('/api/stats/tasks', { startDate, endDate })
  },

  /**
   * 获取饮食统计
   */
  getDietStats(type) {
    return request.get('/api/stats/diet', { type })
  },

  /**
   * 获取年度回忆
   */
  getYearlyStats(year) {
    return request.get('/api/stats/yearly', { year })
  },

  /**
   * 获取今日概览
   */
  getTodayOverview(familyId) {
    return request.get('/api/stats/today', { familyId })
  }
}
