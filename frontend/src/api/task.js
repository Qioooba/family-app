import { request } from '../utils/request'

/**
 * 任务相关API
 * 对应后端: TaskController.java
 * BasePath: /api/task
 */
export const taskApi = {
  /**
   * 获取任务列表
   * @param {number} familyId - 家庭ID
   * @param {object} params - 可选参数: categoryId, status
   * @returns {Promise<Array>} 任务列表
   */
  getList: (familyId, params = {}) => request.get('/api/task/list', { familyId, ...params }),
  
  /**
   * 创建任务
   * @param {object} data - 任务数据
   * @returns {Promise<object>} 创建的任务
   */
  create: (data) => request.post('/api/task/create', data),
  
  /**
   * 更新任务
   * @param {object} data - 任务数据(需包含id)
   * @returns {Promise<object>} 更新后的任务
   */
  update: (data) => request.put('/api/task/update', data),
  
  /**
   * 完成任务
   * @param {number} id - 任务ID
   * @returns {Promise<void>}
   */
  complete: (id) => request.post(`/api/task/complete/${id}`),
  
  /**
   * 指派任务
   * @param {number} id - 任务ID
   * @param {number} assigneeId - 被指派人ID
   * @returns {Promise<void>}
   */
  assign: (id, assigneeId) => request.post(`/api/task/assign/${id}`, { assigneeId }),
  
  /**
   * 获取今日任务
   * @param {number} familyId - 家庭ID
   * @returns {Promise<Array>} 今日任务列表
   */
  getTodayTasks: (familyId) => request.get(`/api/task/today/${familyId}`),
  
  /**
   * 删除任务
   * @param {number} id - 任务ID
   * @returns {Promise<void>}
   */
  delete: (id) => request.delete(`/api/task/${id}`),
  
  /**
   * 获取任务详情
   * @param {number} id - 任务ID
   * @returns {Promise<object>} 任务详情
   */
  getById: (id) => request.get(`/api/task/${id}`),

  /**
   * 获取子任务列表
   * @param {number} taskId - 任务ID
   * @returns {Promise<Array>} 子任务列表
   */
  getSubtasks: (taskId) => request.get(`/api/task/subtask/list/${taskId}`),

  /**
   * 添加子任务
   * @param {object} data - 子任务数据 {taskId, title, sortOrder}
   * @returns {Promise<number>} 子任务ID
   */
  addSubtask: (data) => request.post('/api/task/subtask/add', data),

  /**
   * 切换子任务状态
   * @param {number} id - 子任务ID
   * @returns {Promise<boolean>}
   */
  toggleSubtask: (id) => request.put(`/api/task/subtask/${id}/toggle`),

  /**
   * 删除子任务
   * @param {number} id - 子任务ID
   * @returns {Promise<boolean>}
   */
  deleteSubtask: (id) => request.delete(`/api/task/subtask/${id}`),

  /**
   * 设置任务重复规则
   * @param {number} taskId - 任务ID
   * @param {object} data - {repeatType, repeatRule}
   * @returns {Promise<void>}
   */
  setRepeatRule: (taskId, data) => request.post(`/api/task/${taskId}/repeat`, data),

  /**
   * 获取任务重复规则
   * @param {number} taskId - 任务ID
   * @returns {Promise<object>} 重复规则
   */
  getRepeatRule: (taskId) => request.get(`/api/task/${taskId}/repeat`),

  /**
   * 设置任务提醒
   * @param {number} taskId - 任务ID
   * @param {object} data - 提醒数据
   * @returns {Promise<void>}
   */
  setReminder: (taskId, data) => request.post(`/api/task/${taskId}/reminder`, data),

  /**
   * 获取任务提醒列表
   * @param {number} taskId - 任务ID
   * @returns {Promise<Array>} 提醒列表
   */
  getReminders: (taskId) => request.get(`/api/task/${taskId}/reminders`),

  /**
   * 删除任务提醒
   * @param {number} reminderId - 提醒ID
   * @returns {Promise<void>}
   */
  deleteReminder: (reminderId) => request.delete(`/api/task/reminder/${reminderId}`)
}

export default taskApi
