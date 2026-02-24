import { request } from '../utils/request'

/**
 * 用户相关API
 * 对应后端: UserController.java
 * BasePath: /api/user
 */
export const userApi = {
  /**
   * 切换当前家庭
   * @param {number} familyId - 家庭ID
   * @returns {Promise<object>}
   */
  switchFamily: (familyId) => request.post('/api/user/switch-family', { familyId }),
  
  /**
   * 获取当前用户信息
   * @returns {Promise<object>}
   */
  getCurrentUser: () => request.get('/api/user/info'),
  
  /**
   * 导出家庭数据
   * @param {number} familyId - 家庭ID
   * @returns {Promise<blob>} 导出文件
   */
  exportFamilyData: (familyId) => request.get('/api/export/family-data', { familyId }, { responseType: 'blob' })
}

export default userApi