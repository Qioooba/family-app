import { ref, onMounted } from 'vue'
import { weatherApi } from '../../api/weather'
import { getCurrentLocation, requestLocationPermission, getLifeIndex, formatDate } from '../../utils/weather'

// 当前城市
const currentCity = ref('定位中...')
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

// 生活指数
const lifeIndex = ref([])

// 搜索相关
const searchKeyword = ref('')
const searchResults = ref([])
const showCityPicker = ref(false)

// 位置缓存
let currentPosition = {
  latitude: 39.9042,
  longitude: 116.4074
}

// 获取天气数据
const loadWeatherData = async () => {
  isLoading.value = true
  try {
    // 1. 获取位置
    const location = await getCurrentLocation()
    currentPosition = {
      latitude: location.latitude,
      longitude: location.longitude
    }
    
    // 2. 获取天气数据
    const weatherRes = await weatherApi.getCurrentByLocation(
      currentPosition.latitude,
      currentPosition.longitude
    )
    
    if (weatherRes) {
      currentCity.value = weatherRes.city || location.city || '当前位置'
      currentWeather.value = {
        temperature: Math.round(weatherRes.temperature),
        description: weatherRes.description,
        icon: weatherRes.icon,
        tempMin: Math.round(weatherRes.temperature - 3), // 估算值，实际应从预报获取
        tempMax: Math.round(weatherRes.temperature + 3),
        windDirection: weatherRes.windDirection,
        windLevel: getWindLevel(weatherRes.windSpeed),
        humidity: weatherRes.humidity,
        visibility: 10, // Open-Meteo 免费版不返回能见度，使用默认值
        pressure: Math.round(weatherRes.pressure),
        feelsLike: Math.round(weatherRes.feelsLike),
        isDay: weatherRes.isDay
      }
      updateTime.value = `今天 ${weatherRes.updateTime} 更新`
      
      // 3. 获取生活指数
      lifeIndex.value = getLifeIndex(weatherRes.weatherCode, weatherRes.temperature)
      
      // 4. 获取预报
      await loadForecast()
    }
  } catch (error) {
    console.error('加载天气数据失败:', error)
    uni.showToast({
      title: '获取天气失败，请重试',
      icon: 'none'
    })
  } finally {
    isLoading.value = false
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
        date: formatDate(item.date),
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

// 搜索城市
const onSearchInput = async () => {
  if (!searchKeyword.value.trim()) {
    searchResults.value = []
    return
  }
  
  try {
    const res = await weatherApi.searchCities(searchKeyword.value)
    searchResults.value = res || []
  } catch (error) {
    console.error('搜索城市失败:', error)
  }
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
    currentCity.value = city.name
    
    const weatherRes = await weatherApi.getCurrentByLocation(
      city.latitude,
      city.longitude
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
      updateTime.value = `今天 ${weatherRes.updateTime} 更新`
      lifeIndex.value = getLifeIndex(weatherRes.weatherCode, weatherRes.temperature)
      
      await loadForecast()
    }
    
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
  isLoading,
  updateTime,
  currentWeather,
  forecast,
  lifeIndex,
  searchKeyword,
  searchResults,
  showCityPicker,
  onSearchInput,
  selectCity,
  refreshWeather,
  showIndexDetail
}
