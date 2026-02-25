import { request } from '../utils/request'

// 消息相关API
export const messageApi = {
  /**
   * 发送消息
   * @param {Object} data - 消息数据
   * @param {number} data.type - 消息类型：1-系统通知 2-家庭公告 3-@我的 4-私信
   * @param {string} data.title - 消息标题
   * @param {string} data.content - 消息内容
   * @param {number} [data.receiverId] - 接收者ID(0表示全体用户)
   * @param {number} [data.familyId] - 家庭组ID
   * @param {string} [data.bizType] - 业务类型
   * @param {number} [data.bizId] - 业务ID
   */
  send: (data) => request.post('/api/message/send', data),

  /**
   * 获取消息列表
   * @param {Object} params - 查询参数
   * @param {number} [params.type] - 消息类型
   * @param {number} [params.isRead] - 是否已读：0-未读 1-已读
   * @param {number} [params.pageNum=1] - 页码
   * @param {number} [params.pageSize=20] - 每页大小
   */
  getList: (params) => request.get('/api/message/list', params),

  /**
   * 获取消息详情
   * @param {number} messageId - 消息ID
   */
  getDetail: (messageId) => request.get(`/api/message/${messageId}`),

  /**
   * 标记消息已读
   * @param {number} messageId - 消息ID
   */
  markAsRead: (messageId) => request.post(`/api/message/read/${messageId}`),

  /**
   * 批量标记已读
   * @param {number[]} messageIds - 消息ID数组
   */
  markAsReadBatch: (messageIds) => request.post('/api/message/read/batch', messageIds),

  /**
   * 标记所有消息已读
   */
  markAllAsRead: () => request.post('/api/message/read/all'),

  /**
   * 获取未读消息数
   */
  getUnreadCount: () => request.get('/api/message/unread-count'),

  /**
   * 获取未读消息统计(按类型)
   */
  getUnreadStats: () => request.get('/api/message/unread-stats'),

  /**
   * 删除消息
   * @param {number} messageId - 消息ID
   */
  delete: (messageId) => request.delete(`/api/message/${messageId}`)
}
