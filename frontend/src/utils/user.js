/**
 * 用户显示名称工具函数
 * 优先使用 nickname
 */

/**
 * 获取显示名称
 * @param {Object} user - 用户对象
 * @returns {string} 显示名称
 */
export const getDisplayName = (user) => {
  if (!user) return '未命名'
  return user.nickname || user.username || '未命名'
}

/**
 * 获取完整显示名称
 * @param {Object} user - 用户对象
 * @returns {string} 显示名称
 */
export const getFullDisplayName = (user) => {
  if (!user) return '未命名'
  return user.nickname || user.username || '未命名'
}

/**
 * 获取家庭成员显示名称
 * @param {Object} member - 家庭成员对象
 * @returns {string} 显示名称
 */
export const getMemberDisplayName = (member) => {
  if (!member) return '家人'
  return member.nickname || member.name || '家人'
}

export default {
  getDisplayName,
  getFullDisplayName,
  getMemberDisplayName
}
