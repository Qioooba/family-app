/**
 * 日期时间处理工具函数
 * 处理后端返回的数组格式日期 [年, 月, 日, 时, 分] 或 [年, 月, 日, 时, 分, 秒, 纳秒]
 */

/**
 * 将后端返回的日期格式转换为 Date 对象
 * 支持格式：
 * - 数组: [年, 月, 日, 时, 分] 或 [年, 月, 日, 时, 分, 秒, 纳秒]
 * - 字符串: "2026-03-07 15:00:00" 或 "2026-03-07T15:00:00"
 * @param {Array|string} dateValue - 后端返回的日期
 * @returns {Date|null} Date 对象或 null
 */
export function parseDate(dateValue) {
  if (!dateValue) return null
  
  // 如果是数组格式 [年, 月, 日, 时, 分, ...]
  if (Array.isArray(dateValue)) {
    const [year, month, day, hour = 0, minute = 0, second = 0] = dateValue
    return new Date(year, month - 1, day, hour, minute, second)
  }
  
  // 如果是字符串格式
  if (typeof dateValue === 'string') {
    const date = new Date(dateValue.replace(' ', 'T'))
    if (!isNaN(date.getTime())) {
      return date
    }
  }
  
  return null
}

/**
 * 格式化日期时间为字符串
 * @param {Array|string} dateValue - 后端返回的日期
 * @param {string} format - 格式类型: 'time'|'date'|'datetime'|'dateTimeFull'
 * @returns {string} 格式化后的字符串
 */
export function formatDateTime(dateValue, format = 'datetime') {
  const date = parseDate(dateValue)
  if (!date) return ''
  
  const year = date.getFullYear()
  const month = date.getMonth() + 1
  const day = date.getDate()
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  
  switch (format) {
    case 'time':
      return `${hours}:${minutes}`
    case 'date':
      return `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')}`
    case 'datetime':
      return `${month}月${day}日 ${hours}:${minutes}`
    case 'dateTimeFull':
      return `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')} ${hours}:${minutes}:${seconds}`
    case 'full':
      return `${year}年${month}月${day}日 ${hours}:${minutes}`
    default:
      return `${month}月${day}日 ${hours}:${minutes}`
  }
}

/**
 * 提取日期和时间（用于表单编辑）
 * @param {Array|string} dueTime - 后端返回的日期
 * @returns {Object} { date: 'YYYY-MM-DD', time: 'HH:mm' }
 */
export function extractDateTime(dueTime) {
  if (!dueTime) return { date: '', time: '' }
  
  // 如果是数组格式 [年, 月, 日, 时, 分]
  if (Array.isArray(dueTime)) {
    const [year, month, day, hour = 0, minute = 0] = dueTime
    return {
      date: `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')}`,
      time: `${String(hour).padStart(2, '0')}:${String(minute).padStart(2, '0')}`
    }
  }
  
  // 如果是字符串格式
  if (typeof dueTime === 'string') {
    // 尝试解析 ISO 格式
    const date = new Date(dueTime.replace(' ', 'T'))
    if (!isNaN(date.getTime())) {
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      const hours = String(date.getHours()).padStart(2, '0')
      const minutes = String(date.getMinutes()).padStart(2, '0')
      return { date: `${year}-${month}-${day}`, time: `${hours}:${minutes}` }
    }
    
    // 尝试解析 "YYYY-MM-DD HH:mm:ss" 格式
    const parts = dueTime.split(' ')
    if (parts.length === 2) {
      const datePart = parts[0]
      const timePart = parts[1].substring(0, 5) // 只取 HH:mm
      return { date: datePart, time: timePart }
    }
  }
  
  return { date: '', time: '' }
}

/**
 * 将日期对象转换为后端数组格式
 * @param {Date} date - Date 对象
 * @returns {Array} [年, 月, 日, 时, 分]
 */
export function toArrayFormat(date) {
  return [
    date.getFullYear(),
    date.getMonth() + 1,
    date.getDate(),
    date.getHours(),
    date.getMinutes()
  ]
}

/**
 * 检查值是否为数组格式的日期
 * @param {*} value - 要检查的值
 * @returns {boolean}
 */
export function isArrayDate(value) {
  return Array.isArray(value) && value.length >= 5
}
