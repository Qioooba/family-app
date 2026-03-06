import { weatherApi } from '../api/weather'

/**
 * 天气工具函数
 */

/**
 * 获取用户当前位置
 * @returns {Promise<{latitude: number, longitude: number, city?: string}>}
 */
export const getCurrentLocation = () => {
  return new Promise((resolve, reject) => {
    // #ifdef MP-WEIXIN
    // 微信小程序获取位置
    uni.getLocation({
      type: 'gcj02',
      isHighAccuracy: true,
      success: (res) => {
        resolve({
          latitude: res.latitude,
          longitude: res.longitude,
          city: res.address?.city || res.city
        })
      },
      fail: (err) => {
        console.error('获取位置失败:', err)
        // 默认返回北京位置
        resolve({
          latitude: 39.9042,
          longitude: 116.4074,
          city: '北京市'
        })
      }
    })
    // #endif
    
    // #ifdef H5
    // H5 使用浏览器 Geolocation API
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (position) => {
          resolve({
            latitude: position.coords.latitude,
            longitude: position.coords.longitude
          })
        },
        (err) => {
          console.error('获取位置失败:', err)
          // 默认返回北京位置
          resolve({
            latitude: 39.9042,
            longitude: 116.4074,
            city: '北京市'
          })
        },
        {
          enableHighAccuracy: true,
          timeout: 10000,
          maximumAge: 0
        }
      )
    } else {
      // 浏览器不支持定位，返回默认值
      resolve({
        latitude: 39.9042,
        longitude: 116.4074,
        city: '北京市'
      })
    }
    // #endif
    
    // #ifndef MP-WEIXIN || H5
    // 其他平台返回默认值
    resolve({
      latitude: 39.9042,
      longitude: 116.4074,
      city: '北京市'
    })
    // #endif
  })
}

/**
 * 请求位置权限（微信小程序）
 * @returns {Promise<boolean>}
 */
export const requestLocationPermission = () => {
  return new Promise((resolve) => {
    // #ifdef MP-WEIXIN
    wx.authorize({
      scope: 'scope.userLocation',
      success: () => {
        resolve(true)
      },
      fail: () => {
        // 引导用户去设置页面开启权限
        uni.showModal({
          title: '需要位置权限',
          content: '获取天气需要您的位置信息，是否去设置？',
          success: (res) => {
            if (res.confirm) {
              uni.openSetting({
                success: (settingRes) => {
                  resolve(!!settingRes.authSetting['scope.userLocation'])
                }
              })
            } else {
              resolve(false)
            }
          }
        })
      }
    })
    // #endif
    
    // #ifndef MP-WEIXIN
    resolve(true)
    // #endif
  })
}

/**
 * 根据天气代码获取生活指数建议
 * @param {number} weatherCode - WMO天气代码
 * @param {number} temp - 温度
 * @returns {Array} 生活指数列表
 */
export const getLifeIndex = (weatherCode, temp) => {
  const indexList = []
  
  // 穿衣指数
  let dressLevel, dressText
  if (temp < 5) {
    dressLevel = 4
    dressText = '寒冷'
  } else if (temp < 15) {
    dressLevel = 3
    dressText = '较冷'
  } else if (temp < 25) {
    dressLevel = 2
    dressText = '舒适'
  } else {
    dressLevel = 3
    dressText = '炎热'
  }
  indexList.push({ name: '穿衣', icon: '👔', level: dressLevel, levelText: dressText })
  
  // 运动指数
  let sportLevel, sportText
  if (weatherCode >= 51 && weatherCode <= 65) {
    sportLevel = 1
    sportText = '不宜'
  } else if (weatherCode > 3) {
    sportLevel = 2
    sportText = '较宜'
  } else {
    sportLevel = 3
    sportText = '适宜'
  }
  indexList.push({ name: '运动', icon: '🏃', level: sportLevel, levelText: sportText })
  
  // 洗车指数
  let carLevel, carText
  if (weatherCode >= 51 && weatherCode <= 99) {
    carLevel = 1
    carText = '不宜'
  } else {
    carLevel = 3
    carText = '适宜'
  }
  indexList.push({ name: '洗车', icon: '🚗', level: carLevel, levelText: carText })
  
  // 紫外线指数
  let uvLevel, uvText
  if (weatherCode <= 1) {
    uvLevel = 3
    uvText = '强'
  } else if (weatherCode <= 3) {
    uvLevel = 2
    uvText = '中等'
  } else {
    uvLevel = 1
    uvText = '弱'
  }
  indexList.push({ name: '紫外线', icon: '☀️', level: uvLevel, levelText: uvText })
  
  // 感冒指数
  let coldLevel, coldText
  if (temp < 10 || temp > 30) {
    coldLevel = 3
    coldText = '易发'
  } else if (temp < 20) {
    coldLevel = 2
    coldText = '较易发'
  } else {
    coldLevel = 1
    coldText = '少发'
  }
  indexList.push({ name: '感冒', icon: '🤧', level: coldLevel, levelText: coldText })
  
  // 钓鱼指数
  let fishLevel, fishText
  if (weatherCode >= 51 && weatherCode <= 99) {
    fishLevel = 1
    fishText = '不宜'
  } else if (weatherCode <= 3) {
    fishLevel = 3
    fishText = '适宜'
  } else {
    fishLevel = 2
    fishText = '较宜'
  }
  indexList.push({ name: '钓鱼', icon: '🎣', level: fishLevel, levelText: fishText })
  
  return indexList
}

/**
 * 格式化日期
 * @param {string} dateStr - 日期字符串 YYYY-MM-DD
 * @returns {string} 格式化的日期
 */
export const formatDate = (dateStr) => {
  const date = new Date(dateStr)
  const today = new Date()
  const tomorrow = new Date(today)
  tomorrow.setDate(tomorrow.getDate() + 1)
  
  if (dateStr === formatDateStr(today)) {
    return '今天'
  }
  if (dateStr === formatDateStr(tomorrow)) {
    return '明天'
  }
  
  const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  return weekDays[date.getDay()]
}

const formatDateStr = (date) => {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

export default {
  getCurrentLocation,
  requestLocationPermission,
  getLifeIndex,
  formatDate
}
