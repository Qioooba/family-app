import request from '@/utils/request'

/**
 * AI助手API
 */
export const aiApi = {
  /**
   * AI对话
   */
  chat(data) {
    return request.post('/api/ai/chat', data)
  },

  /**
   * 获取对话历史
   */
  getChatHistory(sessionId) {
    return request.get('/api/ai/chat/history', { sessionId })
  },

  /**
   * 创建新会话
   */
  createSession() {
    return request.post('/api/ai/chat/session')
  },

  /**
   * 获取早安日报
   */
  getMorningReport(userName) {
    return request.get('/api/ai/daily-report/morning', { userName })
  },

  /**
   * 获取晚安总结
   */
  getEveningSummary(userName) {
    return request.get('/api/ai/daily-report/evening', { userName })
  },

  /**
   * 菜谱推荐
   */
  recommendRecipe(data) {
    return request.post('/api/ai/recipe/recommend', data)
  },

  /**
   * 营养分析
   */
  analyzeNutrition(data) {
    return request.post('/api/ai/nutrition/analyze', data)
  }
}
