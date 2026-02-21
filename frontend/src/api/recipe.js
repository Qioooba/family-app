import { request } from '../utils/request'

/**
 * 菜谱相关API
 * 对应后端: RecipeController.java
 * BasePath: /api/recipe
 */
export const recipeApi = {
  /**
   * 搜索菜谱
   * @param {object} params - 搜索参数: keyword, category, difficulty
   * @returns {Promise<Array>} 菜谱列表
   */
  search: (params) => request.get('/api/recipe/search', params),
  
  /**
   * 获取家庭菜谱
   * @param {number} familyId - 家庭ID
   * @returns {Promise<Array>} 菜谱列表
   */
  getFamilyRecipes: (familyId) => request.get(`/api/recipe/family/${familyId}`),
  
  /**
   * AI推荐菜谱
   * @param {Array} ingredients - 食材列表
   * @returns {Promise<Array>} 推荐菜谱列表
   */
  recommend: (ingredients) => request.post('/api/recipe/recommend', ingredients),
  
  /**
   * 创建菜谱
   * @param {object} data - 菜谱数据
   * @returns {Promise<object>} 创建的菜谱
   */
  create: (data) => request.post('/api/recipe/create', data),
  
  /**
   * 获取菜谱详情
   * @param {number} id - 菜谱ID
   * @returns {Promise<object>} 菜谱详情
   */
  getById: (id) => request.get(`/api/recipe/${id}`),
  
  /**
   * 收藏菜谱
   * @param {number} id - 菜谱ID
   * @param {number} userId - 用户ID
   * @returns {Promise<void>}
   */
  favorite: (id, userId) => request.post(`/api/recipe/favorite/${id}`, { userId }),
  
  /**
   * 记录做过
   * @param {number} id - 菜谱ID
   * @param {number} userId - 用户ID
   * @returns {Promise<void>}
   */
  recordCooking: (id, userId) => request.post(`/api/recipe/cook/${id}`, { userId })
}

export default recipeApi
