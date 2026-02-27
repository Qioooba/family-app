import { request } from '../utils/request'

/**
 * 游戏模块相关API
 * 对应后端: GameController.java
 * BasePath: /api/game
 */
export const gameApi = {
  // ========== 家庭挑战 ==========
  
  /**
   * 创建挑战
   * @param {object} challenge - 挑战数据
   * @returns {Promise<object>} 创建的挑战
   */
  createChallenge: (challenge) => request.post('/api/game/challenge', challenge),
  
  /**
   * 更新挑战
   * @param {number} id - 挑战ID
   * @param {object} challenge - 挑战数据
   * @returns {Promise<object>} 更新后的挑战
   */
  updateChallenge: (id, challenge) => request.put(`/api/game/challenge/${id}`, challenge),
  
  /**
   * 删除挑战
   * @param {number} id - 挑战ID
   * @returns {Promise<void>}
   */
  deleteChallenge: (id) => request.delete(`/api/game/challenge/${id}`),
  
  /**
   * 获取挑战详情
   * @param {number} id - 挑战ID
   * @returns {Promise<object>} 挑战详情
   */
  getChallengeById: (id) => request.get(`/api/game/challenge/${id}`),
  
  /**
   * 获取家庭挑战列表
   * @param {number} familyId - 家庭ID
   * @returns {Promise<Array>} 挑战列表
   */
  getFamilyChallenges: (familyId) => request.get(`/api/game/challenges/family/${familyId}`),
  
  /**
   * 获取活跃挑战
   * @param {number} familyId - 家庭ID
   * @returns {Promise<Array>} 活跃挑战列表
   */
  getActiveChallenges: (familyId) => request.get(`/api/game/challenges/active/${familyId}`),
  
  // ========== 挑战参与 ==========
  
  /**
   * 加入挑战
   * @param {number} challengeId - 挑战ID
   * @returns {Promise<object>} 参与者信息
   */
  joinChallenge: (challengeId) => request.post(`/api/game/challenge/${challengeId}/join`),
  
  /**
   * 离开挑战
   * @param {number} challengeId - 挑战ID
   * @returns {Promise<void>}
   */
  leaveChallenge: (challengeId) => request.post(`/api/game/challenge/${challengeId}/leave`),
  
  /**
   * 获取挑战参与者列表
   * @param {number} challengeId - 挑战ID
   * @returns {Promise<Array>} 参与者列表
   */
  getChallengeParticipants: (challengeId) => request.get(`/api/game/challenge/${challengeId}/participants`),
  
  /**
   * 获取当前用户的参与信息
   * @param {number} challengeId - 挑战ID
   * @returns {Promise<object>} 参与者信息
   */
  getMyParticipant: (challengeId) => request.get(`/api/game/challenge/${challengeId}/participant`),
  
  // ========== 打卡 ==========
  
  /**
   * 打卡
   * @param {object} checkin - 打卡数据 {challengeId, note, image}
   * @returns {Promise<object>} 打卡记录
   */
  checkin: (checkin) => request.post('/api/game/challenge/checkin', checkin),
  
  /**
   * 获取打卡记录
   * @param {number} challengeId - 挑战ID
   * @returns {Promise<Array>} 打卡记录列表
   */
  getCheckinRecords: (challengeId) => request.get(`/api/game/challenge/${challengeId}/checkins`),
  
  /**
   * 获取连续打卡天数
   * @param {number} challengeId - 挑战ID
   * @returns {Promise<number>} 连续天数
   */
  getConsecutiveDays: (challengeId) => request.get(`/api/game/challenge/${challengeId}/consecutive-days`),
  
  /**
   * 获取挑战排行榜
   * @param {number} challengeId - 挑战ID
   * @returns {Promise<Array>} 排行榜数据
   */
  getChallengeRank: (challengeId) => request.get(`/api/game/challenge/${challengeId}/rank`),
  
  // ========== 猜谜游戏 ==========
  
  /**
   * 获取随机谜语
   * @returns {Promise<object>} 谜语
   */
  getRandomRiddle: () => request.get('/api/game/riddle/random'),
  
  /**
   * 获取谜语详情
   * @param {number} id - 谜语ID
   * @returns {Promise<object>} 谜语详情
   */
  getRiddleById: (id) => request.get(`/api/game/riddle/${id}`),
  
  /**
   * 验证谜语答案
   * @param {number} id - 谜语ID
   * @param {string} answer - 答案
   * @returns {Promise<boolean>} 是否正确
   */
  verifyRiddleAnswer: (id, answer) => request.post(`/api/game/riddle/${id}/verify?answer=${encodeURIComponent(answer)}`),
  
  /**
   * 按分类获取谜语
   * @param {string} category - 分类
   * @returns {Promise<Array>} 谜语列表
   */
  getRiddlesByCategory: (category) => request.get(`/api/game/riddles/category/${category}`),
  
  // ========== 答题游戏 ==========
  
  /**
   * 获取随机题目
   * @returns {Promise<object>} 题目
   */
  getRandomQuiz: () => request.get('/api/game/quiz/random'),
  
  /**
   * 获取题目详情
   * @param {number} id - 题目ID
   * @returns {Promise<object>} 题目详情
   */
  getQuizById: (id) => request.get(`/api/game/quiz/${id}`),
  
  /**
   * 验证答案
   * @param {number} id - 题目ID
   * @param {string} answer - 答案
   * @returns {Promise<boolean>} 是否正确
   */
  verifyQuizAnswer: (id, answer) => request.post(`/api/game/quiz/${id}/verify?answer=${encodeURIComponent(answer)}`),
  
  /**
   * 按分类获取题目
   * @param {string} category - 分类
   * @returns {Promise<Array>} 题目列表
   */
  getQuizzesByCategory: (category) => request.get(`/api/game/quizzes/category/${category}`),

  // ========== 积分相关 ==========
  
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
   * @returns {Promise<Array>} 排行榜数据
   */
  getRankings: (familyId) => request.get(`/api/game/rankings/${familyId}`),
  
  /**
   * 添加积分
   * @param {object} data - 积分数据
   * @returns {Promise<void>}
   */
  addPoints: (data) => request.post('/api/game/points/add', data),
  
  // ========== 成就系统 ==========
  
  /**
   * 获取成就列表
   * @returns {Promise<Array>} 成就列表
   */
  getAchievements: () => request.get('/api/game/achievements')
}

export default gameApi
