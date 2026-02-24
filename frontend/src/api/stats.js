import { request } from '@/utils/request'

/**
 * 数据统计API
 */
export const statsApi = {
  /**
   * 获取个人统计
   */
  getPersonalStats(type, date) {
    return request.get('/api/stats/personal', { type, date }, { silent: true })
  },

  /**
   * 获取家庭统计
   */
  getFamilyStats(familyId, type) {
    return request.get(`/api/stats/family/${familyId}`, { type }, { silent: true })
  },

  /**
   * 获取任务统计
   */
  getTaskStats(familyId, startDate, endDate) {
    return request.get('/api/stats/tasks', { familyId, startDate, endDate }, { silent: true })
  },

  /**
   * 获取饮食统计
   */
  getDietStats(type) {
    return request.get('/api/stats/diet', { type }, { silent: true })
  },

  /**
   * 获取年度回忆
   */
  getYearlyStats(familyId, year) {
    return request.get('/api/stats/yearly', { familyId, year }, { silent: true })
  },

  /**
   * 获取今日概览
   */
  getTodayOverview(familyId) {
    return request.get('/api/stats/today', { familyId }, { silent: true })
  },

  /**
   * 获取家庭本月统计
   */
  getFamilyMonthlyStats(familyId) {
    return request.get(`/api/stats/family/${familyId}/monthly`, {}, { silent: true })
  }
}
