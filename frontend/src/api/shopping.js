import { request } from '../utils/request'

/**
 * 购物模块相关API
 * 对应后端: ShoppingController.java
 * BasePath: /api/shopping
 */
export const shoppingApi = {
  /**
   * 创建购物清单
   * @param {object} request - 清单数据
   * @returns {Promise<number>} 清单ID
   */
  createList: (data) => request.post('/api/shopping/list/create', data),
  
  /**
   * 获取购物清单列表
   * @param {number} familyId - 家庭ID
   * @returns {Promise<Array>} 清单列表
   */
  getLists: (familyId) => request.get(`/api/shopping/list/${familyId}`),
  
  /**
   * 获取清单详情
   * @param {number} listId - 清单ID
   * @returns {Promise<object>} 清单详情
   */
  getListDetail: (listId) => request.get(`/api/shopping/list/detail/${listId}`),
  
  /**
   * 添加商品
   * @param {object} request - 商品数据
   * @returns {Promise<boolean>}
   */
  addItem: (data) => request.post('/api/shopping/item/add', data),
  
  /**
   * 更新商品状态
   * @param {number} itemId - 商品ID
   * @param {number} status - 状态
   * @param {number} actualPrice - 实际价格(可选)
   * @returns {Promise<boolean>}
   */
  updateItemStatus: (itemId, status, actualPrice) => 
    request.put(`/api/shopping/item/${itemId}/status`, { status, actualPrice }),
  
  /**
   * 扫描商品条码
   * @param {object} request - 条码数据
   * @returns {Promise<object>} 商品信息
   */
  scanProduct: (data) => request.post('/api/shopping/scan', data),
  
  /**
   * 添加库存
   * @param {object} request - 库存数据
   * @returns {Promise<number>} 库存ID
   */
  addInventory: (data) => request.post('/api/shopping/inventory/add', data),
  
  /**
   * 获取库存列表
   * @param {number} familyId - 家庭ID
   * @returns {Promise<Array>} 库存列表
   */
  getInventory: (familyId) => request.get(`/api/shopping/inventory/${familyId}`),
  
  /**
   * 获取即将过期商品
   * @param {number} familyId - 家庭ID
   * @returns {Promise<Array>} 即将过期商品列表
   */
  getExpiringItems: (familyId) => request.get(`/api/shopping/inventory/expiring/${familyId}`)
}

export default shoppingApi
