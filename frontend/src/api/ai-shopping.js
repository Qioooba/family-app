import { request } from '../utils/request'

/**
 * AI购物助手API
 * 
 * 预留接口说明：
 * 1. generateListByAI - AI生成购物清单
 * 2. voiceToText - 语音转文字（可接入科大讯飞、百度语音等）
 * 3. recognizeProduct - 拍照识别商品
 * 4. getRecommendations - 智能推荐
 */
export const aiShoppingApi = {
  /**
   * AI生成购物清单
   * @param {string} prompt - 用户输入的文本
   * @param {number} familyId - 家庭ID
   * @returns {Promise<object>} 生成的清单数据
   * 
   * 预期返回格式：
   * {
   *   name: "红烧肉材料清单",
   *   items: [
   *     { name: "五花肉", quantity: 500, unit: "克", category: "生鲜", estimatedPrice: 35 },
   *     ...
   *   ],
   *   estimatedAmount: 74,
   *   reasoning: "根据红烧肉的做法，需要五花肉、调料等"
   * }
   */
  generateListByAI: (prompt, familyId) => {
    // TODO: 接入后端AI接口
    // return request.post('/api/ai-shopping/generate', { prompt, familyId })
    
    // 模拟AI返回（开发时使用）
    return new Promise((resolve) => {
      setTimeout(() => {
        resolve(generateMockAIResult(prompt))
      }, 1500)
    })
  },

  /**
   * 语音转文字
   * @param {string} audioFile - 录音文件路径（小程序临时文件）
   * @returns {Promise<string>} 识别出的文本
   * 
   * 接入方案：
   * 1. 微信小程序自带语音识别（需配置）
   * 2. 科大讯飞语音识别API
   * 3. 百度语音API
   * 4. 阿里云语音服务
   */
  voiceToText: (audioFile) => {
    // TODO: 接入语音识别服务
    // 方式1: 调用后端接口
    // return request.post('/api/ai-shopping/voice-to-text', { audioFile })
    
    // 方式2: 直接调用云厂商API（需要配置密钥）
    // return uni.uploadFile({
    //   url: 'https://语音识别API地址',
    //   filePath: audioFile,
    //   name: 'audio'
    // })
    
    // 模拟返回
    return new Promise((resolve) => {
      setTimeout(() => {
        const mockTexts = [
          '帮我买苹果和牛奶',
          '周末聚会的零食饮料',
          '做西红柿炒鸡蛋的材料',
          '帮我生成一个日常用品清单'
        ]
        resolve(mockTexts[Math.floor(Math.random() * mockTexts.length)])
      }, 1000)
    })
  },

  /**
   * 拍照识别商品
   * @param {string} imageBase64 - 图片Base64数据
   * @returns {Promise<object>} 识别结果
   * 
   * 接入方案：
   * 1. 百度AI商品识别
   * 2. 阿里云图像搜索
   * 3. 腾讯云图像识别
   * 4. 自训练模型
   */
  recognizeProduct: (imageBase64) => {
    // TODO: 接入图像识别API
    // return request.post('/api/ai-shopping/recognize-product', { imageBase64 })
    
    // 模拟返回
    return new Promise((resolve) => {
      setTimeout(() => {
        resolve({
          name: '可口可乐',
          brand: 'Coca-Cola',
          category: '饮料',
          confidence: 0.95
        })
      }, 1000)
    })
  },

  /**
   * 获取智能推荐
   * @param {number} familyId - 家庭ID
   * @returns {Promise<Array>} 推荐商品列表
   * 
   * 推荐策略：
   * 1. 基于历史购买记录
   * 2. 基于家庭消费偏好
   * 3. 季节性推荐
   * 4. 促销活动推荐
   */
  getRecommendations: (familyId) => {
    // TODO: 接入推荐系统
    // return request.get('/api/ai-shopping/recommendations', { familyId })
    
    // 模拟返回
    return new Promise((resolve) => {
      setTimeout(() => {
        resolve([
          { name: '牛奶', reason: '基于您的购买习惯', category: '饮品' },
          { name: '鸡蛋', reason: '常购商品补货提醒', category: '生鲜' },
          { name: '苹果', reason: '季节推荐', category: '水果' }
        ])
      }, 500)
    })
  },

  /**
   * 获取价格预测
   * @param {string} productName - 商品名称
   * @returns {Promise<object>} 价格预测信息
   * 
   * 数据来源：
   * 1. 历史价格走势
   * 2. 促销日历
   * 3. 电商API价格
   */
  getPricePrediction: (productName) => {
    // TODO: 接入价格预测服务
    // return request.get('/api/ai-shopping/price-prediction', { productName })
    
    return Promise.resolve({
      currentPrice: 35,
      predictedPrice: 28,
      bestTime: '3天后',
      trend: 'down'
    })
  },

  /**
   * 智能补货建议
   * @param {number} familyId - 家庭ID
   * @returns {Promise<Array>} 补货建议列表
   * 
   * 分析维度：
   * 1. 库存余量
   * 2. 消耗速度
   * 3. 保质期
   * 4. 采购周期
   */
  getRestockSuggestions: (familyId) => {
    // TODO: 接入库存分析服务
    // return request.get('/api/ai-shopping/restock-suggestions', { familyId })
    
    return Promise.resolve([
      { name: '洗衣液', reason: '预计3天后用完', urgency: 'medium' },
      { name: '牛奶', reason: '库存不足', urgency: 'high' }
    ])
  }
}

/**
 * 根据输入生成模拟AI结果
 */
function generateMockAIResult(prompt) {
  const keywords = {
    '红烧肉': {
      name: '红烧肉材料清单',
      items: [
        { name: '五花肉', quantity: 500, unit: '克', category: '生鲜', estimatedPrice: 35 },
        { name: '冰糖', quantity: 30, unit: '克', category: '调料', estimatedPrice: 5 },
        { name: '生抽', quantity: 2, unit: '勺', category: '调料', estimatedPrice: 8 },
        { name: '老抽', quantity: 1, unit: '勺', category: '调料', estimatedPrice: 6 },
        { name: '料酒', quantity: 2, unit: '勺', category: '调料', estimatedPrice: 10 },
        { name: '生姜', quantity: 3, unit: '片', category: '生鲜', estimatedPrice: 3 },
        { name: '大葱', quantity: 2, unit: '段', category: '生鲜', estimatedPrice: 4 },
        { name: '八角', quantity: 2, unit: '个', category: '调料', estimatedPrice: 3 }
      ],
      estimatedAmount: 74
    },
    '西红柿炒蛋': {
      name: '西红柿炒蛋材料清单',
      items: [
        { name: '西红柿', quantity: 2, unit: '个', category: '生鲜', estimatedPrice: 6 },
        { name: '鸡蛋', quantity: 3, unit: '个', category: '生鲜', estimatedPrice: 3 },
        { name: '小葱', quantity: 2, unit: '根', category: '生鲜', estimatedPrice: 2 },
        { name: '食用油', quantity: 30, unit: 'ml', category: '调料', estimatedPrice: 5 },
        { name: '盐', quantity: 5, unit: '克', category: '调料', estimatedPrice: 1 },
        { name: '糖', quantity: 5, unit: '克', category: '调料', estimatedPrice: 1 }
      ],
      estimatedAmount: 18
    },
    '聚会': {
      name: '周末聚会采购清单',
      items: [
        { name: '可乐', quantity: 2, unit: '瓶', category: '饮料', estimatedPrice: 16 },
        { name: '薯片', quantity: 3, unit: '包', category: '零食', estimatedPrice: 24 },
        { name: '水果拼盘', quantity: 2, unit: '份', category: '生鲜', estimatedPrice: 50 },
        { name: '蛋糕', quantity: 1, unit: '个', category: '食品', estimatedPrice: 88 },
        { name: '纸巾', quantity: 2, unit: '盒', category: '日用品', estimatedPrice: 10 },
        { name: '一次性杯子', quantity: 50, unit: '个', category: '日用品', estimatedPrice: 15 }
      ],
      estimatedAmount: 203
    },
    '火锅': {
      name: '火锅派对材料清单',
      items: [
        { name: '肥牛卷', quantity: 2, unit: '盒', category: '生鲜', estimatedPrice: 76 },
        { name: '羊肉卷', quantity: 2, unit: '盒', category: '生鲜', estimatedPrice: 68 },
        { name: '虾滑', quantity: 1, unit: '袋', category: '生鲜', estimatedPrice: 35 },
        { name: '火锅底料', quantity: 1, unit: '包', category: '调料', estimatedPrice: 18 },
        { name: '蔬菜拼盘', quantity: 2, unit: '份', category: '生鲜', estimatedPrice: 40 },
        { name: '豆腐', quantity: 1, unit: '盒', category: '生鲜', estimatedPrice: 8 },
        { name: '金针菇', quantity: 2, unit: '包', category: '生鲜', estimatedPrice: 12 },
        { name: '饮料', quantity: 6, unit: '瓶', category: '饮料', estimatedPrice: 36 }
      ],
      estimatedAmount: 293
    }
  }

  // 匹配关键词
  for (const [key, value] of Object.entries(keywords)) {
    if (prompt.includes(key)) {
      return value
    }
  }

  // 默认返回
  return {
    name: 'AI生成购物清单',
    items: [
      { name: '苹果', quantity: 1, unit: '斤', category: '生鲜', estimatedPrice: 8 },
      { name: '香蕉', quantity: 1, unit: '把', category: '生鲜', estimatedPrice: 6 },
      { name: '牛奶', quantity: 1, unit: '箱', category: '饮品', estimatedPrice: 45 },
      { name: '面包', quantity: 1, unit: '袋', category: '食品', estimatedPrice: 12 },
      { name: '鸡蛋', quantity: 30, unit: '个', category: '生鲜', estimatedPrice: 25 }
    ],
    estimatedAmount: 96
  }
}

export default aiShoppingApi
