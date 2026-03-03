// 微信登录功能

/**
 * 获取微信登录凭证
 * @returns {Promise<string>} code
 */
export const getWxLoginCode = () => {
  return new Promise((resolve, reject) => {
    uni.login({
      provider: 'weixin',
      success: (res) => {
        if (res.code) {
          resolve(res.code)
        } else {
          reject(new Error('获取微信登录凭证失败'))
        }
      },
      fail: reject
    })
  })
}

/**
 * 获取微信手机号
 * @param {Object} e - 按钮回调事件
 * @returns {Promise<{encryptedData: string, iv: string}>}
 */
export const getWxPhoneNumber = (e) => {
  return new Promise((resolve, reject) => {
    if (e.detail.errMsg === 'getPhoneNumber:ok') {
      resolve({
        encryptedData: e.detail.encryptedData,
        iv: e.detail.iv,
        code: e.detail.code // 新版本返回的code
      })
    } else {
      reject(new Error(e.detail.errMsg || '用户拒绝获取手机号'))
    }
  })
}

/**
 * 检查微信登录环境
 * @returns {boolean}
 */
export const isWeixinEnvironment = () => {
  // #ifdef MP-WEIXIN
  return true
  // #endif
  // #ifndef MP-WEIXIN
  return false
  // #endif
}

export default {
  getWxLoginCode,
  getWxPhoneNumber,
  isWeixinEnvironment
}