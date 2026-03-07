/**
 * 天气相关API
 * 对应后端: WeatherController.java
 * BasePath: /api/weather
 */
import { request } from '../utils/request'

export const weatherApi = {
  /**
   * 根据经纬度获取当前天气
   * @param {number} lat - 纬度
   * @param {number} lon - 经度
   * @returns {Promise<object>} 天气数据
   */
  getCurrentByLocation: (lat, lon) => {
    return request.get('/api/weather/current', { lat, lon })
  },
  
  /**
   * 根据城市名称获取当前天气
   * @param {string} city - 城市名称
   * @returns {Promise<object>} 天气数据
   */
  getCurrentByCity: (city) => {
    return request.get('/api/weather/current/by-city', { city })
  },
  
  /**
   * 获取未来天气预报
   * @param {number} lat - 纬度
   * @param {number} lon - 经度
   * @param {number} days - 预报天数 (1-7)
   * @returns {Promise<object>} 预报数据
   */
  getForecast: (lat, lon, days = 7) => {
    return request.get('/api/weather/forecast', { lat, lon, days })
  },
  
  /**
   * 获取逐小时天气预报
   * @param {number} lat - 纬度
   * @param {number} lon - 经度
   * @param {number} hours - 预报小时数 (1-48)
   * @returns {Promise<object>} 逐小时预报数据
   */
  getHourlyForecast: (lat, lon, hours = 24) => {
    return request.get('/api/weather/hourly', { lat, lon, hours })
  },
  
  /**
   * 搜索城市
   * @param {string} keyword - 搜索关键词
   * @returns {Promise<Array>} 城市列表
   */
  searchCities: (keyword) => {
    return request.get('/api/weather/cities', { keyword })
  }
}

export default weatherApi
