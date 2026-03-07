import { ref, onMounted } from 'vue'
import { weatherApi } from '../../api/weather'
import { 
  getCurrentLocation, 
  getCurrentLocationWithAddress,
  requestLocationPermission, 
  getLifeIndex, 
  formatDate,
  formatDateWithWeekday,
  formatHour,
  getFullLocationName,
  getShortLocationName
} from '../../utils/weather'

// 当前城市
const currentCity = ref('定位中...')
const currentDistrict = ref('') // 区/县
const fullLocation = ref('定位中...') // 完整位置：南京市 鼓楼区
const hasLocationPermission = ref(true)

const isLoading = ref(false)
const updateTime = ref('')

// 当前天气
const currentWeather = ref({
  temperature: '--',
  description: '--',
  icon: '🌡️',
  tempMin: '--',
  tempMax: '--',
  windDirection: '--',
  windLevel: '--',
  humidity: '--',
  visibility: '--',
  pressure: '--'
})

// 未来预报
const forecast = ref([])

// 逐小时预报
const hourlyForecast = ref([])

// 生活指数
const lifeIndex = ref([])

// 搜索相关
const searchKeyword = ref('')
const searchResults = ref([])
const showCityPicker = ref(false)

// 位置信息
let currentPosition = {
  latitude: 39.9042,
  longitude: 116.4074
}
let currentLocationInfo = null

// 获取天气数据
const loadWeatherData = async () => {
  isLoading.value = true
  try {
    // 1. 获取位置和详细地址
    const location = await getCurrentLocationWithAddress()
    currentPosition = {
      latitude: location.latitude,
      longitude: location.longitude
    }
    currentLocationInfo = location.locationInfo
    
    // 更新位置显示
    hasLocationPermission.value = true
    updateLocationDisplay()
    
    // 2. 获取天气数据
    await fetchWeatherData()
    
  } catch (error) {
    console.error('加载天气数据失败:', error)
    hasLocationPermission.value = false
    fullLocation.value = '点击获取位置'
    currentCity.value = '点击获取位置'
    uni.showToast({
      title: '获取位置失败，请检查权限',
      icon: 'none'
    })
  } finally {
    isLoading.value = false
  }
}

// 更新位置显示
const updateLocationDisplay = () => {
  if (currentLocationInfo) {
    fullLocation.value = getFullLocationName(currentLocationInfo)
    currentCity.value = currentLocationInfo.city || currentLocationInfo.district || '定位中...'
    currentDistrict.value = currentLocationInfo.district || ''
  }
}

// 获取天气数据（不带位置获取）
const fetchWeatherData = async () => {
  try {
    const weatherRes = await weatherApi.getCurrentByLocation(
      currentPosition.latitude,
      currentPosition.longitude
    )
    
    if (weatherRes) {
      currentWeather.value = {
        temperature: Math.round(weatherRes.temperature),
        description: weatherRes.description,
        icon: weatherRes.icon,
        tempMin: Math.round(weatherRes.temperature - 3),
        tempMax: Math.round(weatherRes.temperature + 3),
        windDirection: weatherRes.windDirection,
        windLevel: getWindLevel(weatherRes.windSpeed),
        humidity: weatherRes.humidity,
        visibility: 10,
        pressure: Math.round(weatherRes.pressure),
        feelsLike: Math.round(weatherRes.feelsLike),
        isDay: weatherRes.isDay
      }
      updateTime.value = `更新于 ${weatherRes.updateTime}`
      
      // 3. 获取生活指数
      lifeIndex.value = getLifeIndex(weatherRes.weatherCode, weatherRes.temperature)
      
      // 4. 获取预报
      await loadForecast()
      
      // 5. 获取逐小时预报
      await loadHourlyForecast()
    }
  } catch (error) {
    console.error('获取天气数据失败:', error)
    uni.showToast({
      title: '获取天气失败，请重试',
      icon: 'none'
    })
  }
}

// 获取预报数据
const loadForecast = async () => {
  try {
    const forecastRes = await weatherApi.getForecast(
      currentPosition.latitude,
      currentPosition.longitude,
      7
    )
    
    if (forecastRes && forecastRes.forecasts) {
      forecast.value = forecastRes.forecasts.map(item => ({
        date: formatDateWithWeekday(item.date),
        fullDate: item.date,
        icon: item.icon,
        description: item.description,
        tempMin: Math.round(item.tempMin),
        tempMax: Math.round(item.tempMax),
        precipitation: item.precipitation
      }))
      
      // 更新今日最高最低温
      if (forecast.value.length > 0) {
        currentWeather.value.tempMin = forecast.value[0].tempMin
        currentWeather.value.tempMax = forecast.value[0].tempMax
      }
    }
  } catch (error) {
    console.error('加载预报数据失败:', error)
  }
}

// 获取逐小时预报数据
const loadHourlyForecast = async () => {
  try {
    const hourlyRes = await weatherApi.getHourlyForecast(
      currentPosition.latitude,
      currentPosition.longitude,
      24
    )
    
    if (hourlyRes && hourlyRes.forecasts) {
      hourlyForecast.value = hourlyRes.forecasts.map(item => ({
        time: item.time,
        hour: item.hour,
        temperature: Math.round(item.temperature),
        icon: item.icon,
        description: item.description,
        hasRain: item.hasRain,
        precipitation: item.precipitation,
        precipitationProbability: item.precipitationProbability || 0,
        weatherCode: item.weatherCode
      }))
    }
  } catch (error) {
    console.error('加载逐小时预报失败:', error)
  }
}

// 根据风速获取风力等级
const getWindLevel = (windSpeed) => {
  if (windSpeed < 1) return 0
  if (windSpeed < 6) return 1
  if (windSpeed < 12) return 2
  if (windSpeed < 20) return 3
  if (windSpeed < 29) return 4
  if (windSpeed < 39) return 5
  return 6
}

// 请求位置权限
const requestLocation = async () => {
  const hasPermission = await requestLocationPermission()
  if (hasPermission) {
    loadWeatherData()
  }
}

// 搜索城市
const onSearchInput = async () => {
  if (!searchKeyword.value.trim()) {
    searchResults.value = []
    return
  }
  
  try {
    const res = await weatherApi.searchCities(searchKeyword.value)
    // 对结果进行排序：优先显示中国大城市
    searchResults.value = sortCitiesByPriority(res || [])
  } catch (error) {
    console.error('搜索城市失败:', error)
  }
}

// 城市优先级排序：优先显示中国大城市
const sortCitiesByPriority = (cities) => {
  if (!cities || cities.length === 0) return []
  
  // 中国主要省份列表（用于优先排序）
  const chinaProvinces = [
    '江苏', '北京', '上海', '广东', '浙江', '四川', '湖北', '湖南', 
    '河南', '山东', '河北', '福建', '安徽', '陕西', '辽宁', '江西',
    '重庆', '天津', '山西', '广西', '云南', '贵州', '黑龙江', '吉林',
    '甘肃', '海南', '内蒙古', '新疆', '西藏', '青海', '宁夏', '台湾',
    '香港', '澳门'
  ]
  
  // 中国主要城市（用于优先排序）
  const majorCities = [
    '北京', '上海', '广州', '深圳', '成都', '杭州', '武汉', '西安',
    '重庆', '南京', '天津', '苏州', '长沙', '郑州', '沈阳', '青岛',
    '宁波', '东莞', '无锡', '佛山', '合肥', '大连', '厦门', '济南',
    '昆明', '福州', '温州', '常州', '哈尔滨', '长春', '石家庄', '南宁'
  ]
  
  return cities.sort((a, b) => {
    const aIsChina = a.country === 'China' || a.country === '中国'
    const bIsChina = b.country === 'China' || b.country === '中国'
    
    // 优先显示中国城市
    if (aIsChina && !bIsChina) return -1
    if (!aIsChina && bIsChina) return 1
    
    // 都是中国城市的，按省份优先级排序
    if (aIsChina && bIsChina) {
      const aProvinceIndex = chinaProvinces.findIndex(p => a.admin1 && a.admin1.includes(p))
      const bProvinceIndex = chinaProvinces.findIndex(p => b.admin1 && b.admin1.includes(p))
      
      if (aProvinceIndex !== -1 && bProvinceIndex !== -1) {
        if (aProvinceIndex !== bProvinceIndex) {
          return aProvinceIndex - bProvinceIndex
        }
      } else if (aProvinceIndex !== -1) {
        return -1
      } else if (bProvinceIndex !== -1) {
        return 1
      }
      
      // 同一省份或都没有匹配，按主要城市优先级排序
      const aCityIndex = majorCities.findIndex(c => a.name && a.name.includes(c))
      const bCityIndex = majorCities.findIndex(c => b.name && b.name.includes(c))
      
      if (aCityIndex !== -1 && bCityIndex !== -1) {
        return aCityIndex - bCityIndex
      } else if (aCityIndex !== -1) {
        return -1
      } else if (bCityIndex !== -1) {
        return 1
      }
    }
    
    // 默认按名称排序
    return (a.name || '').localeCompare(b.name || '')
  })
}

// 选择城市
const selectCity = async (city) => {
  showCityPicker.value = false
  isLoading.value = true
  
  try {
    currentPosition = {
      latitude: city.latitude,
      longitude: city.longitude
    }
    currentLocationInfo = {
      city: city.name,
      district: '',
      admin1: city.admin1,
      country: city.country
    }
    updateLocationDisplay()
    
    await fetchWeatherData()
    
    searchKeyword.value = ''
    searchResults.value = []
  } catch (error) {
    console.error('切换城市失败:', error)
    uni.showToast({
      title: '切换城市失败',
      icon: 'none'
    })
  } finally {
    isLoading.value = false
  }
}

// 刷新天气
const refreshWeather = async () => {
  if (isLoading.value) return
  
  uni.showLoading({ title: '刷新中...' })
  await loadWeatherData()
  uni.hideLoading()
  uni.showToast({ title: '已刷新', icon: 'success' })
}

// 显示指数详情
const showIndexDetail = (item) => {
  uni.showToast({ title: `${item.name}: ${item.levelText}`, icon: 'none' })
}

onMounted(() => {
  // 请求位置权限并加载天气
  requestLocationPermission().then(() => {
    loadWeatherData()
  })
})

// 暴露给模板使用
export {
  currentCity,
  currentDistrict,
  fullLocation,
  hasLocationPermission,
  isLoading,
  updateTime,
  currentWeather,
  forecast,
  hourlyForecast,
  lifeIndex,
  searchKeyword,
  searchResults,
  showCityPicker,
  onSearchInput,
  selectCity,
  refreshWeather,
  showIndexDetail,
  loadWeatherData,
  requestLocation,
  formatHour
}
