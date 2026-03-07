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
  getAchievements: () => request.get('/api/game/achievements'),
  
  // ========== 默契问答 ==========
  
  /**
   * 初始化问题库
   * @returns {Promise<void>}
   */
  initMindMatchQuestions: () => request.post('/api/game/mindmatch/init'),
  
  /**
   * 获取随机问题
   * @param {string} type - 题目类型: spouse/parent/family
   * @returns {Promise<object>} 问题
   */
  getMindMatchQuestion: (type) => request.get('/api/game/mindmatch/question', { type }),
  
  /**
   * 创建游戏会话
   * @param {number} familyId - 家庭ID
   * @param {number} player2Id - 对方玩家ID
   * @returns {Promise<object>} 会话
   */
  createMindMatchSession: (familyId, player2Id) => request.post('/api/game/mindmatch/session', { familyId, player2Id }),
  
  /**
   * 回答问题
   * @param {number} sessionId - 会话ID
   * @param {string} answer - 答案
   * @returns {Promise<object>} 更新后的会话
   */
  answerMindMatchQuestion: (sessionId, answer) => request.post(`/api/game/mindmatch/session/${sessionId}/answer`, { answer }),
  
  /**
   * 获取会话详情
   * @param {number} sessionId - 会话ID
   * @returns {Promise<object>} 会话详情
   */
  getMindMatchSession: (sessionId) => request.get(`/api/game/mindmatch/session/${sessionId}`),
  
  /**
   * 获取家庭游戏历史
   * @param {number} familyId - 家庭ID
   * @returns {Promise<Array>} 历史记录
   */
  getMindMatchHistory: (familyId) => request.get(`/api/game/mindmatch/history/${familyId}`),
  
  /**
   * 获取我的游戏统计
   * @returns {Promise<object>} 统计数据
   */
  getMyMindMatchStats: () => request.get('/api/game/mindmatch/stats'),
  
  /**
   * 获取家庭默契值
   * @param {number} familyId - 家庭ID
   * @returns {Promise<object>} 默契值数据
   */
  getMindMatchRate: (familyId) => request.get(`/api/game/mindmatch/match-rate/${familyId}`),
  
  // ========== 成语接龙 ==========
  
  /**
   * 获取随机成语
   * @returns {Promise<object>} 成语
   */
  getRandomIdiom: () => request.get('/api/game/idiom/random'),
  
  /**
   * 验证成语接龙
   * @param {string} prevIdiom - 上一个成语
   * @param {string} currentIdiom - 当前成语
   * @returns {Promise<boolean>} 是否有效
   */
  verifyIdiomChain: (prevIdiom, currentIdiom) => request.post('/api/game/idiom/verify', { prevIdiom, currentIdiom }),
  
  /**
   * 获取成语提示
   * @param {string} char - 首字
   * @returns {Promise<Array>} 成语列表
   */
  getIdiomHints: (char) => request.get(`/api/game/idiom/hints?char=${char}`),
  
  // ========== 谁是卧底 ==========
  
  /**
   * 创建游戏房间
   * @param {object} data - 房间数据
   * @returns {Promise<object>} 房间信息
   */
  createUndercoverRoom: (data) => request.post('/api/game/undercover/room', data),
  
  /**
   * 加入游戏房间
   * @param {string} roomCode - 房间代码
   * @returns {Promise<object>} 房间信息
   */
  joinUndercoverRoom: (roomCode) => request.post(`/api/game/undercover/room/${roomCode}/join`),
  
  /**
   * 获取房间信息
   * @param {string} roomCode - 房间代码
   * @returns {Promise<object>} 房间信息
   */
  getUndercoverRoom: (roomCode) => request.get(`/api/game/undercover/room/${roomCode}`),
  
  /**
   * 投票
   * @param {string} roomCode - 房间代码
   * @param {number} targetId - 目标玩家ID
   * @returns {Promise<void>}
   */
  voteUndercover: (roomCode, targetId) => request.post(`/api/game/undercover/room/${roomCode}/vote`, { targetId }),
  
  /**
   * 获取随机词语
   * @param {string} category - 分类
   * @returns {Promise<object>} 词语对
   */
  getUndercoverWords: (category) => request.get(`/api/game/undercover/words?category=${category}`),
  
  // ========== 真心话大冒险 ==========
  
  /**
   * 获取随机题目
   * @param {string} type - 类型: truth/dare
   * @param {number} difficulty - 难度
   * @returns {Promise<object>} 题目
   */
  getRandomTruthDare: (type, difficulty) => request.get('/api/game/truthdare/random', { type, difficulty }),
  
  /**
   * 获取题目列表
   * @param {string} type - 类型
   * @returns {Promise<Array>} 题目列表
   */
  getTruthDareList: (type) => request.get(`/api/game/truthdare/list?type=${type}`),
  
  // ========== 你画我猜 ==========
  
  /**
   * 获取随机词语
   * @param {string} category - 分类
   * @returns {Promise<object>} 词语
   */
  getRandomDrawingWord: (category) => request.get('/api/game/drawguess/word/random', { category }),
  
  /**
   * 提交画作
   * @param {object} data - 画作数据
   * @returns {Promise<object>} 结果
   */
  submitDrawing: (data) => request.post('/api/game/drawguess/submit', data),
  
  /**
   * 验证猜测
   * @param {number} drawingId - 画作ID
   * @param {string} guess - 猜测
   * @returns {Promise<boolean>} 是否正确
   */
  verifyDrawingGuess: (drawingId, guess) => request.post(`/api/game/drawguess/${drawingId}/verify`, { guess }),
  
  // ========== 家庭运动会 ==========
  
  /**
   * 记录运动成绩
   * @param {object} data - 成绩数据
   * @returns {Promise<object>} 记录
   */
  recordSportsScore: (data) => request.post('/api/game/sports/record', data),
  
  /**
   * 获取运动成绩列表
   * @param {string} sportType - 运动类型
   * @returns {Promise<Array>} 成绩列表
   */
  getSportsRecords: (sportType) => request.get('/api/game/sports/records', { sportType }),
  
  /**
   * 获取排行榜
   * @param {string} sportType - 运动类型
   * @returns {Promise<Array>} 排行榜
   */
  getSportsRankings: (sportType) => request.get('/api/game/sports/rankings', { sportType }),
  
  // ========== 厨艺比拼 ==========
  
  /**
   * 创建比赛
   * @param {object} data - 比赛数据
   * @returns {Promise<object>} 比赛
   */
  createCookingContest: (data) => request.post('/api/game/cooking/contest', data),
  
  /**
   * 上传作品
   * @param {object} data - 作品数据
   * @returns {Promise<object>} 作品
   */
  uploadCookingWork: (data) => request.post('/api/game/cooking/work', data),
  
  /**
   * 投票
   * @param {number} workId - 作品ID
   * @returns {Promise<void>}
   */
  voteCookingWork: (workId) => request.post(`/api/game/cooking/work/${workId}/vote`),
  
  /**
   * 评分
   * @param {number} workId - 作品ID
   * @param {object} scores - 分数
   * @returns {Promise<void>}
   */
  scoreCookingWork: (workId, scores) => request.post(`/api/game/cooking/work/${workId}/score`, scores),
  
  /**
   * 获取比赛详情
   * @param {number} contestId - 比赛ID
   * @returns {Promise<object>} 比赛详情
   */
  getCookingContest: (contestId) => request.get(`/api/game/cooking/contest/${contestId}`),
  
  // ========== 家庭寻宝 ==========
  
  /**
   * 创建寻宝游戏
   * @param {object} data - 游戏数据
   * @returns {Promise<object>} 游戏
   */
  createTreasureHunt: (data) => request.post('/api/game/treasure/hunt', data),
  
  /**
   * 获取线索
   * @param {number} huntId - 游戏ID
   * @param {number} clueIndex - 线索索引
   * @returns {Promise<object>} 线索
   */
  getTreasureClue: (huntId, clueIndex) => request.get(`/api/game/treasure/hunt/${huntId}/clue/${clueIndex}`),
  
  /**
   * 验证宝藏
   * @param {number} huntId - 游戏ID
   * @param {number} treasureId - 宝藏ID
   * @param {string} photo - 照片
   * @returns {Promise<boolean>} 是否成功
   */
  verifyTreasure: (huntId, treasureId, photo) => request.post(`/api/game/treasure/hunt/${huntId}/verify`, { treasureId, photo }),
  
  /**
   * 获取游戏进度
   * @param {number} huntId - 游戏ID
   * @returns {Promise<object>} 进度
   */
  getTreasureProgress: (huntId) => request.get(`/api/game/treasure/hunt/${huntId}/progress`),
  
  // ========== 游戏配置 ==========
  
  /**
   * 保存游戏配置
   * @param {string} gameCode - 游戏代码
   * @param {object} config - 配置
   * @returns {Promise<void>}
   */
  saveGameConfig: (gameCode, config) => request.post(`/api/game/config/${gameCode}`, config),
  
  /**
   * 获取游戏配置
   * @param {string} gameCode - 游戏代码
   * @returns {Promise<object>} 配置
   */
  getGameConfig: (gameCode) => request.get(`/api/game/config/${gameCode}`),
  
  /**
   * 获取默认配置
   * @param {string} gameCode - 游戏代码
   * @returns {Promise<object>} 默认配置
   */
  getDefaultConfig: (gameCode) => request.get(`/api/game/config/${gameCode}/default`)
}

export default gameApi
