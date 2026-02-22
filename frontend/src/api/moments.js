import { request } from '@/utils/request'

/**
 * 家庭圈API
 */
export const momentsApi = {
  /**
   * 发布动态
   */
  create(data) {
    return request.post('/api/moments/create', data)
  },

  /**
   * 获取动态流
   */
  getFeed(familyId, page = 1, size = 10) {
    return request.get(`/api/moments/feed/${familyId}`, { page, size })
  },

  /**
   * 点赞/取消点赞
   */
  like(momentId) {
    return request.post(`/api/moments/${momentId}/like`)
  },

  /**
   * 评论
   */
  comment(momentId, data) {
    return request.post(`/api/moments/${momentId}/comment`, data)
  },

  /**
   * 获取评论列表
   */
  getComments(momentId) {
    return request.get(`/api/moments/${momentId}/comments`)
  },

  /**
   * 删除动态
   */
  delete(momentId) {
    return request.delete(`/api/moments/${momentId}`)
  }
}
