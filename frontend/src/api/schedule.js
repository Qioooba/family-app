import { request } from '../utils/request'

/**
 * 排班相关API
 * 对应后端: ScheduleController.java
 * BasePath: /api/schedule
 */
export const scheduleApi = {
  /**
   * 获取排班列表
   * @param {number} familyId - 家庭ID
   * @returns {Promise<Array>} 排班列表
   */
  getList: (familyId) => {
    // 获取当前周的日期范围
    const today = new Date()
    const startOfWeek = new Date(today)
    startOfWeek.setDate(today.getDate() - today.getDay() + 1) // 周一
    const endOfWeek = new Date(startOfWeek)
    endOfWeek.setDate(startOfWeek.getDate() + 6) // 周日

    const startDate = formatDate(startOfWeek)
    const endDate = formatDate(endOfWeek)

    return request.get('/api/schedule/list', { familyId, startDate, endDate })
  },

  /**
   * 获取今日排班
   * @param {number} familyId - 家庭ID
   * @returns {Promise<Array>} 今日排班列表
   */
  getToday: (familyId) => {
    const today = new Date()
    const dateStr = formatDate(today)

    return request.get('/api/schedule/list', {
      familyId,
      startDate: dateStr,
      endDate: dateStr
    })
  },

  /**
   * 创建排班
   * @param {object} data - 排班数据
   * @returns {Promise<object>} 创建的排班
   */
  create: (data) => request.post('/api/schedule/create', data),

  /**
   * 更新排班
   * @param {object} data - 排班数据
   * @returns {Promise<object>} 更新的排班
   */
  update: (data) => request.post('/api/schedule/update', data),

  /**
   * 删除排班
   * @param {number} id - 排班ID
   * @returns {Promise<void>}
   */
  delete: (id) => request.post('/api/schedule/delete', null, { params: { scheduleId: id } }),

  /**
   * 获取我的排班
   * @param {string} startDate - 开始日期
   * @param {string} endDate - 结束日期
   * @returns {Promise<Array>} 我的排班列表
   */
  getMySchedule: (startDate, endDate) => request.get('/api/schedule/my', { startDate, endDate }),

  /**
   * 交换排班
   * @param {number} scheduleId1 - 排班1 ID
   * @param {number} scheduleId2 - 排班2 ID
   * @returns {Promise<void>}
   */
  swap: (scheduleId1, scheduleId2) => request.post('/api/schedule/swap', null, { params: { scheduleId1, scheduleId2 } })
}

// 格式化日期为 YYYY-MM-DD
function formatDate(date) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

export default scheduleApi
