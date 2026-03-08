/**
 * 天气工具函数 - 优化版
 * 包含更精确的地理位置获取和天气代码映射
 */

import { weatherApi } from '../api/weather'

/**
 * 天气代码映射表 (WMO Weather interpretation codes)
 * 代码对应：https://open-meteo.com/en/docs
 */
export const WEATHER_CODES = {
  // 晴
  0: { desc: '晴', icon: '☀️', iconClass: 'sunny', color: '#FFD93D' },
  // 多云
  1: { desc: '多云', icon: '⛅', iconClass: 'partly-cloudy', color: '#FFA726' },
  2: { desc: '多云', icon: '☁️', iconClass: 'cloudy', color: '#90A4AE' },
  3: { desc: '阴天', icon: '☁️', iconClass: 'overcast', color: '#78909C' },
  // 雾
  45: { desc: '雾', icon: '🌫️', iconClass: 'fog', color: '#B0BEC5' },
  48: { desc: '雾凇', icon: '🌫️', iconClass: 'fog', color: '#B0BEC5' },
  // 毛毛雨
  51: { desc: '毛毛雨', icon: '🌦️', iconClass: 'drizzle', color: '#64B5F6' },
  53: { desc: '中雨', icon: '🌦️', iconClass: 'drizzle', color: '#42A5F5' },
  55: { desc: '大雨', icon: '🌧️', iconClass: 'drizzle', color: '#2196F3' },
  // 冻雨
  56: { desc: '冻雨', icon: '🌨️', iconClass: 'freezing-rain', color: '#81D4FA' },
  57: { desc: '大冻雨', icon: '🌨️', iconClass: 'freezing-rain', color: '#4FC3F7' },
  // 雨
  61: { desc: '小雨', icon: '🌧️', iconClass: 'rain', color: '#42A5F5' },
  63: { desc: '中雨', icon: '🌧️', iconClass: 'rain', color: '#2196F3' },
  65: { desc: '大雨', icon: '🌧️', iconClass: 'rain', color: '#1E88E5' },
  // 冻雨
  66: { desc: '冻雨', icon: '🌨️', iconClass: 'freezing-rain', color: '#81D4FA' },
  67: { desc: '大冻雨', icon: '🌨️', iconClass: 'freezing-rain', color: '#4FC3F7' },
  // 雪
  71: { desc: '小雪', icon: '🌨️', iconClass: 'snow', color: '#B3E5FC' },
  73: { desc: '中雪', icon: '❄️', iconClass: 'snow', color: '#81D4FA' },
  75: { desc: '大雪', icon: '❄️', iconClass: 'snow', color: '#4FC3F7' },
  // 冰雹
  77: { desc: '冰粒', icon: '🌨️', iconClass: 'hail', color: '#E1F5FE' },
  // 阵雨
  80: { desc: '阵雨', icon: '🌦️', iconClass: 'showers', color: '#64B5F6' },
  81: { desc: '强阵雨', icon: '🌧️', iconClass: 'showers', color: '#42A5F5' },
  82: { desc: '暴雨', icon: '⛈️', iconClass: 'showers', color: '#1E88E5' },
  // 阵雪
  85: { desc: '阵雪', icon: '🌨️', iconClass: 'snow-showers', color: '#B3E5FC' },
  86: { desc: '强阵雪', icon: '❄️', iconClass: 'snow-showers', color: '#81D4FA' },
  // 雷雨
  95: { desc: '雷雨', icon: '⛈️', iconClass: 'thunderstorm', color: '#7E57C2' },
  96: { desc: '雷雨伴冰雹', icon: '⛈️', iconClass: 'thunderstorm', color: '#5E35B1' },
  99: { desc: '强雷雨', icon: '⛈️', iconClass: 'thunderstorm', color: '#4527A0' }
}

/**
 * 根据天气代码获取天气信息
 * @param {number} code - WMO天气代码
 * @returns {object} 天气信息对象
 */
export const getWeatherByCode = (code) => {
  return WEATHER_CODES[code] || { desc: '未知', icon: '🌡️', iconClass: 'unknown', color: '#9E9E9E' }
}

/**
 * 反向地理编码 - 获取详细地址
 * 使用腾讯地图API（精确到街道、小区）
 * @param {number} latitude - 纬度
 * @param {number} longitude - 经度
 * @returns {Promise<object>} 地址信息 { district: '鼓楼区', street: '新街口', city: '南京市', fullAddress: '...' }
 */
export const reverseGeocode = (latitude, longitude) => {
  return getLocationByTencentMap(latitude, longitude)
}

/**
 * 使用腾讯地图API进行反向地理编码（精确到街道、小区）
 * @param {number} latitude - 纬度
 * @param {number} longitude - 经度
 * @returns {Promise<object>} 地址信息
 */
export const getLocationByTencentMap = (latitude, longitude) => {
  return new Promise((resolve) => {
    const TENCENT_MAP_KEY = 'QCEBZ-25QC3-SCE3O-O557W-SS4VJ-KYFZY'
    const url = `https://apis.map.qq.com/ws/geocoder/v1/?location=${latitude},${longitude}&key=${TENCENT_MAP_KEY}&get_poi=1&poi_options=address_format=short`
    
    uni.request({
      url,
      method: 'GET',
      success: (res) => {
        if (res.data && res.data.status === 0 && res.data.result) {
          const result = res.data.result
          const address = result.address_component || {}
          const pois = result.pois || []
          const poi = pois[0] || {} // 最近的POI（小区、大厦等）
          
          // 优先使用POI名称（小区/大厦），其次是街道
          const locationName = poi.title || address.street || address.district || ''
          
          resolve({
            province: address.province || '',
            city: address.city || '',
            district: address.district || '',
            street: address.street || '',
            streetNumber: address.street_number || '',
            // POI信息（小区、大厦、商场等）
            poiName: poi.title || '',
            poiCategory: poi.category || '',
            // 用于显示的完整地址
            fullAddress: result.address || '',
            // 推荐显示格式：省份 + 城市 + 区县 + 街道/POI
            displayName: `${address.province || ''}${address.city || ''}${address.district || ''}${locationName}`.replace(/^(.*?省)?(.*?市)?(.*?区)?(.*)$/, '$1$2$3$4'),
            // 简写：区县 + 街道/POI
            shortName: `${address.district || ''}${locationName}`
          })
        } else {
          resolve({ 
            province: '', city: '当前位置', district: '', street: '', 
            poiName: '', fullAddress: '', displayName: '当前位置', shortName: '' 
          })
        }
      },
      fail: () => {
        resolve({ 
          province: '', city: '当前位置', district: '', street: '', 
          poiName: '', fullAddress: '', displayName: '当前位置', shortName: '' 
        })
      }
    })
  })
}

/**
 * 格式化地址信息
 * @param {object} address - 地址对象
 * @returns {string} 格式化后的地址
 */
const formatAddress = (address) => {
  const parts = []
  if (address.province) parts.push(address.province)
  if (address.city && address.city !== address.province) parts.push(address.city)
  if (address.district) parts.push(address.district)
  if (address.street) parts.push(address.street)
  if (address.streetNum) parts.push(address.streetNum)
  return parts.join('')
}

/**
 * 格式化日期 - 墨迹天气风格：3月7日 周五
 * @param {string} dateStr - 日期字符串 YYYY-MM-DD
 * @returns {string} 格式化的日期
 */
export const formatDateWithWeekday = (dateStr) => {
  const date = new Date(dateStr)
  const today = new Date()
  const tomorrow = new Date(today)
  tomorrow.setDate(tomorrow.getDate() + 1)
  
  const month = date.getMonth() + 1
  const day = date.getDate()
  const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  const weekday = weekDays[date.getDay()]
  
  if (dateStr === formatDateStr(today)) {
    return `今天 ${month}月${day}日 ${weekday}`
  }
  if (dateStr === formatDateStr(tomorrow)) {
    return `明天 ${month}月${day}日 ${weekday}`
  }
  
  return `${month}月${day}日 ${weekday}`
}

/**
 * 格式化小时时间 - 显示为 14:00
 * @param {string} timeStr - 时间字符串 HH:MM
 * @returns {string} 格式化的小时
 */
export const formatHour = (timeStr) => {
  if (!timeStr) return ''
  // 如果传入的是完整时间字符串 YYYY-MM-DDTHH:MM，只取时间部分
  if (timeStr.includes('T')) {
    return timeStr.split('T')[1].substring(0, 5)
  }
  return timeStr.substring(0, 5)
}

/**
 * 判断是否为当前小时
 * @param {string} timeStr - 时间字符串
 * @returns {boolean}
 */
export const isCurrentHour = (timeStr) => {
  const now = new Date()
  const hour = now.getHours()
  const timeHour = parseInt(timeStr.split(':')[0])
  return hour === timeHour
}

/**
 * 获取简短位置名称（用于天气小组件）- 支持精确到街道、小区
 * @param {object} locationInfo - 位置信息对象
 * @returns {string} 简短位置名
 */
export const getShortLocationName = (locationInfo) => {
  if (!locationInfo) return '当前位置'
  
  // 优先使用腾讯地图返回的简写名称（区县+POI/街道）
  if (locationInfo.shortName) {
    return locationInfo.shortName
  }
  
  // 优先显示POI名称（小区、大厦）
  if (locationInfo.poiName) {
    return locationInfo.poiName
  }
  
  // 其次显示区/县名称
  if (locationInfo.district) {
    // 去掉"区"、"县"、"市辖区"等后缀，使显示更简洁
    return locationInfo.district.replace(/(区|县|市辖区)$/g, '')
  }
  
  // 再次显示街道
  if (locationInfo.street) {
    return locationInfo.street.replace(/(街道|镇|乡)$/g, '')
  }
  
  // 最后返回城市名
  if (locationInfo.city) {
    return locationInfo.city.replace(/(市|地区)$/g, '')
  }
  
  return '当前位置'
}

/**
 * 获取完整位置显示（省+市+区+街道/POI）- 如"江苏省南京市秦淮区月牙湖街道"
 * @param {object} locationInfo - 位置信息对象
 * @returns {string} 完整位置名
 */
export const getFullLocationName = (locationInfo) => {
  if (!locationInfo) return '定位中...'
  
  // 优先使用腾讯地图返回的完整显示名称
  if (locationInfo.displayName) {
    return locationInfo.displayName
  }
  
  const province = locationInfo.province || ''
  const city = locationInfo.city || ''
  const district = locationInfo.district || ''
  const street = locationInfo.street || ''
  const poiName = locationInfo.poiName || ''
  
  // 组合省市区+街道/POI
  const parts = []
  if (province) parts.push(province)
  if (city) parts.push(city)
  if (district) parts.push(district)
  // 优先显示POI（小区/大厦），其次是街道
  if (poiName) {
    parts.push(poiName)
  } else if (street) {
    parts.push(street)
  }
  
  if (parts.length > 0) {
    return parts.join('')
  }
  
  return '定位中...'
}

/**
 * 获取用户当前位置（带详细地址）
 * @returns {Promise<{latitude: number, longitude: number, locationInfo: object}>}
 */
export const getCurrentLocationWithAddress = () => {
  return new Promise(async (resolve) => {
    try {
      const location = await getCurrentLocation()
      const locationInfo = await reverseGeocode(location.latitude, location.longitude)
      
      resolve({
        ...location,
        locationInfo
      })
    } catch (error) {
      console.error('获取位置失败:', error)
      // 默认返回北京位置
      resolve({
        latitude: 39.9042,
        longitude: 116.4074,
        locationInfo: { city: '北京市', district: '朝阳区', street: '' }
      })
    }
  })
}

/**
 * 获取用户当前位置
 * @returns {Promise<{latitude: number, longitude: number, city?: string}>}
 */
export const getCurrentLocation = () => {
  return new Promise((resolve, reject) => {
    // #ifdef MP-WEIXIN
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
        resolve({
          latitude: 39.9042,
          longitude: 116.4074,
          city: '北京市'
        })
      }
    })
    // #endif
    
    // #ifdef H5
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
      resolve({
        latitude: 39.9042,
        longitude: 116.4074,
        city: '北京市'
      })
    }
    // #endif
    
    // #ifndef MP-WEIXIN || H5
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
  WEATHER_CODES,
  getWeatherByCode,
  reverseGeocode,
  getShortLocationName,
  getCurrentLocationWithAddress,
  getCurrentLocation,
  requestLocationPermission,
  getLifeIndex,
  formatDate
}
