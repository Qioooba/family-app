import request from '@/utils/request'

/**
 * 智能购物API
 */
export const shoppingApi = {
  /**
   * 创建购物清单
   */
  createList(data) {
    return request.post('/api/shopping/list/create', data)
  },

  /**
   * 获取购物清单列表
   */
  getLists(familyId) {
    return request.get(`/api/shopping/list/${familyId}`)
  },

  /**
   * 获取清单详情
   */
  getListDetail(listId) {
    return request.get(`/api/shopping/list/detail/${listId}`)
  },

  /**
   * 添加购物项
   */
  addItem(data) {
    return request.post('/api/shopping/item/add', data)
  },

  /**
   * 更新购物项状态
   */
  updateItemStatus(itemId, status, actualPrice) {
    return request.put(`/api/shopping/item/${itemId}/status`, { status, actualPrice })
  },

  /**
   * 扫码识别商品
   */
  scanProduct(barcode, image) {
    return request.post('/api/shopping/scan', { barcode, image })
  },

  /**
   * 添加库存
   */
  addInventory(data) {
    return request.post('/api/shopping/inventory/add', data)
  },

  /**
   * 获取库存列表
   */
  getInventory(familyId) {
    return request.get(`/api/shopping/inventory/${familyId}`)
  },

  /**
   * 获取临期商品
   */
  getExpiringItems(familyId) {
    return request.get(`/api/shopping/inventory/expiring/${familyId}`)
  }
}
