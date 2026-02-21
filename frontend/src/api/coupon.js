import { request } from '../utils/request'

/**
 * 优惠券相关API
 * 对应后端: CouponController.java
 * BasePath: /api/coupon
 */
export const couponApi = {
  /**
   * 获取优惠券列表
   * @param {number} familyId - 家庭ID
   * @returns {Promise<Array>} 优惠券列表
   */
  getList: (familyId) => request.get('/api/coupons', { familyId }),
  
  /**
   * 添加优惠券
   * @param {object} data - 优惠券数据
   * @returns {Promise<object>} 创建的优惠券
   */
  create: (data) => request.post('/api/coupon', data),
  
  /**
   * 标记优惠券为已使用
   * @param {number} id - 优惠券ID
   * @returns {Promise<void>}
   */
  use: (id) => request.put(`/api/coupon/${id}/use`),
  
  /**
   * 删除优惠券
   * @param {number} id - 优惠券ID
   * @returns {Promise<void>}
   */
  delete: (id) => request.delete(`/api/coupon/${id}`)
}

export default couponApi