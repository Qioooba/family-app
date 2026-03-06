package com.family.weather.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.family.common.core.Result;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 天气控制器
 * 使用 Open-Meteo 免费天气API (无需API Key)
 * 文档: https://open-meteo.com/
 */
@Slf4j
@RestController
@SaCheckLogin
@RequestMapping("/api/weather")
public class WeatherController {

    private final RestTemplate restTemplate = new RestTemplate();
    
    // Open-Meteo API 基础地址
    private static final String OPEN_METEO_API = "https://api.open-meteo.com/v1/forecast";
    
    // 地理编码API (用于城市名称转坐标)
    private static final String GEOCODING_API = "https://geocoding-api.open-meteo.com/v1/search";
    
    /**
     * 根据经纬度获取当前天气
     * GET /api/weather/current?lat={lat}&lon={lon}
     * 
     * @param lat 纬度
     * @param lon 经度
     * @return 天气数据
     */
    @GetMapping("/current")
    public Result<WeatherData> getCurrentWeather(
            @RequestParam Double lat,
            @RequestParam Double lon) {
        try {
            log.info("获取天气数据: lat={}, lon={}", lat, lon);
            
            // 构建请求URL
            String url = String.format(
                "%s?latitude=%.4f&longitude=%.4f&current=temperature_2m,relative_humidity_2m,apparent_temperature,is_day,precipitation,rain,showers,snowfall,weather_code,cloud_cover,pressure_msl,surface_pressure,wind_speed_10m,wind_direction_10m&timezone=auto",
                OPEN_METEO_API, lat, lon
            );
            
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            if (response == null || !response.containsKey("current")) {
                return Result.error("获取天气数据失败");
            }
            
            WeatherData weatherData = parseWeatherData(response);
            return Result.success(weatherData);
            
        } catch (Exception e) {
            log.error("获取天气数据失败", e);
            return Result.error("获取天气数据失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据城市名称获取当前天气
     * GET /api/weather/current?city={city}
     * 
     * @param city 城市名称
     * @return 天气数据
     */
    @GetMapping("/current/by-city")
    public Result<WeatherData> getWeatherByCity(@RequestParam String city) {
        try {
            log.info("根据城市获取天气: city={}", city);
            
            // 1. 先获取城市坐标
            String geoUrl = String.format("%s?name=%s&count=1&language=zh&format=json", 
                GEOCODING_API, city);
            
            Map<String, Object> geoResponse = restTemplate.getForObject(geoUrl, Map.class);
            
            if (geoResponse == null || !geoResponse.containsKey("results")) {
                return Result.error("未找到该城市: " + city);
            }
            
            var results = (java.util.List<Map<String, Object>>) geoResponse.get("results");
            if (results == null || results.isEmpty()) {
                return Result.error("未找到该城市: " + city);
            }
            
            Map<String, Object> cityInfo = results.get(0);
            Double lat = ((Number) cityInfo.get("latitude")).doubleValue();
            Double lon = ((Number) cityInfo.get("longitude")).doubleValue();
            String cityName = (String) cityInfo.get("name");
            String country = (String) cityInfo.get("country");
            
            // 2. 获取天气数据
            String weatherUrl = String.format(
                "%s?latitude=%.4f&longitude=%.4f&current=temperature_2m,relative_humidity_2m,apparent_temperature,is_day,precipitation,rain,showers,snowfall,weather_code,cloud_cover,pressure_msl,surface_pressure,wind_speed_10m,wind_direction_10m&timezone=auto",
                OPEN_METEO_API, lat, lon
            );
            
            Map<String, Object> weatherResponse = restTemplate.getForObject(weatherUrl, Map.class);
            
            if (weatherResponse == null || !weatherResponse.containsKey("current")) {
                return Result.error("获取天气数据失败");
            }
            
            WeatherData weatherData = parseWeatherData(weatherResponse);
            weatherData.setCity(cityName);
            weatherData.setCountry(country);
            
            return Result.success(weatherData);
            
        } catch (Exception e) {
            log.error("获取天气数据失败", e);
            return Result.error("获取天气数据失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取未来天气预报
     * GET /api/weather/forecast?lat={lat}&lon={lon}&days={days}
     * 
     * @param lat 纬度
     * @param lon 经度
     * @param days 预报天数 (1-7)
     * @return 天气预报数据
     */
    @GetMapping("/forecast")
    public Result<ForecastData> getForecast(
            @RequestParam Double lat,
            @RequestParam Double lon,
            @RequestParam(defaultValue = "7") Integer days) {
        try {
            log.info("获取天气预报: lat={}, lon={}, days={}", lat, lon, days);
            
            // 限制预报天数
            days = Math.min(Math.max(days, 1), 7);
            
            String url = String.format(
                "%s?latitude=%.4f&longitude=%.4f&daily=weather_code,temperature_2m_max,temperature_2m_min,sunrise,sunset,precipitation_sum,precipitation_probability_max&timezone=auto&forecast_days=%d",
                OPEN_METEO_API, lat, lon, days
            );
            
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            if (response == null || !response.containsKey("daily")) {
                return Result.error("获取天气预报失败");
            }
            
            ForecastData forecastData = parseForecastData(response);
            return Result.success(forecastData);
            
        } catch (Exception e) {
            log.error("获取天气预报失败", e);
            return Result.error("获取天气预报失败: " + e.getMessage());
        }
    }
    
    /**
     * 搜索城市
     * GET /api/weather/cities?keyword={keyword}
     * 
     * @param keyword 搜索关键词
     * @return 城市列表
     */
    @GetMapping("/cities")
    public Result<java.util.List<CityInfo>> searchCities(@RequestParam String keyword) {
        try {
            log.info("搜索城市: keyword={}", keyword);
            
            String url = String.format("%s?name=%s&count=10&language=zh&format=json", 
                GEOCODING_API, keyword);
            
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            if (response == null || !response.containsKey("results")) {
                return Result.success(java.util.Collections.emptyList());
            }
            
            var results = (java.util.List<Map<String, Object>>) response.get("results");
            if (results == null) {
                return Result.success(java.util.Collections.emptyList());
            }
            
            java.util.List<CityInfo> cities = new java.util.ArrayList<>();
            for (Map<String, Object> result : results) {
                CityInfo city = new CityInfo();
                city.setName((String) result.get("name"));
                city.setCountry((String) result.get("country"));
                city.setLatitude(((Number) result.get("latitude")).doubleValue());
                city.setLongitude(((Number) result.get("longitude")).doubleValue());
                city.setAdmin1((String) result.get("admin1")); // 省份/州
                cities.add(city);
            }
            
            return Result.success(cities);
            
        } catch (Exception e) {
            log.error("搜索城市失败", e);
            return Result.error("搜索城市失败: " + e.getMessage());
        }
    }
    
    /**
     * 解析天气数据
     */
    private WeatherData parseWeatherData(Map<String, Object> response) {
        Map<String, Object> current = (Map<String, Object>) response.get("current");
        Map<String, String> units = new HashMap<>();
        
        // 提取单位信息
        Map<String, Object> currentUnits = (Map<String, Object>) response.get("current_units");
        if (currentUnits != null) {
            currentUnits.forEach((k, v) -> units.put(k, (String) v));
        }
        
        WeatherData data = new WeatherData();
        data.setTemperature(getDoubleValue(current, "temperature_2m"));
        data.setFeelsLike(getDoubleValue(current, "apparent_temperature"));
        data.setHumidity(getIntValue(current, "relative_humidity_2m"));
        data.setWindSpeed(getDoubleValue(current, "wind_speed_10m"));
        data.setWindDirection(getWindDirection(getIntValue(current, "wind_direction_10m")));
        data.setPressure(getDoubleValue(current, "surface_pressure"));
        data.setCloudCover(getIntValue(current, "cloud_cover"));
        data.setIsDay(getIntValue(current, "is_day") == 1);
        
        // 天气代码转描述和图标
        int weatherCode = getIntValue(current, "weather_code");
        data.setWeatherCode(weatherCode);
        data.setDescription(getWeatherDescription(weatherCode));
        data.setIcon(getWeatherIcon(weatherCode, data.getIsDay()));
        
        data.setUpdateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM-dd HH:mm")));
        
        return data;
    }
    
    /**
     * 解析预报数据
     */
    private ForecastData parseForecastData(Map<String, Object> response) {
        Map<String, Object> daily = (Map<String, Object>) response.get("daily");
        
        var dates = (java.util.List<String>) daily.get("time");
        var maxTemps = (java.util.List<Number>) daily.get("temperature_2m_max");
        var minTemps = (java.util.List<Number>) daily.get("temperature_2m_min");
        var weatherCodes = (java.util.List<Number>) daily.get("weather_code");
        var precipitationSums = (java.util.List<Number>) daily.get("precipitation_sum");
        
        java.util.List<DailyForecast> forecasts = new java.util.ArrayList<>();
        
        for (int i = 0; i < dates.size(); i++) {
            DailyForecast forecast = new DailyForecast();
            forecast.setDate(dates.get(i));
            forecast.setTempMax(maxTemps.get(i).doubleValue());
            forecast.setTempMin(minTemps.get(i).doubleValue());
            
            int code = weatherCodes.get(i).intValue();
            forecast.setWeatherCode(code);
            forecast.setDescription(getWeatherDescription(code));
            forecast.setIcon(getWeatherIcon(code, true));
            
            if (precipitationSums != null && i < precipitationSums.size()) {
                forecast.setPrecipitation(precipitationSums.get(i).doubleValue());
            }
            
            forecasts.add(forecast);
        }
        
        ForecastData data = new ForecastData();
        data.setForecasts(forecasts);
        return data;
    }
    
    /**
     * 获取风向
     */
    private String getWindDirection(int degrees) {
        String[] directions = {"北", "东北", "东", "东南", "南", "西南", "西", "西北"};
        int index = Math.round(degrees / 45.0f) % 8;
        return directions[index];
    }
    
    /**
     * 根据WMO天气代码获取描述
     */
    private String getWeatherDescription(int code) {
        Map<Integer, String> descriptions = new HashMap<>();
        descriptions.put(0, "晴");
        descriptions.put(1, "主要晴朗");
        descriptions.put(2, "多云");
        descriptions.put(3, "阴");
        descriptions.put(45, "雾");
        descriptions.put(48, "雾凇");
        descriptions.put(51, "毛毛雨 (轻)");
        descriptions.put(53, "毛毛雨 (中)");
        descriptions.put(55, "毛毛雨 (密)");
        descriptions.put(61, "小雨");
        descriptions.put(63, "中雨");
        descriptions.put(65, "大雨");
        descriptions.put(71, "小雪");
        descriptions.put(73, "中雪");
        descriptions.put(75, "大雪");
        descriptions.put(77, "雪粒");
        descriptions.put(80, "阵雨 (轻)");
        descriptions.put(81, "阵雨 (中)");
        descriptions.put(82, "阵雨 (强)");
        descriptions.put(85, "阵雪 (轻)");
        descriptions.put(86, "阵雪 (强)");
        descriptions.put(95, "雷雨");
        descriptions.put(96, "雷雨伴有冰雹");
        descriptions.put(99, "雷雨伴有强冰雹");
        
        return descriptions.getOrDefault(code, "未知");
    }
    
    /**
     * 根据WMO天气代码获取图标
     */
    private String getWeatherIcon(int code, boolean isDay) {
        // 返回emoji图标
        if (code == 0) return isDay ? "☀️" : "🌙"; // 晴
        if (code <= 3) return isDay ? "⛅" : "☁️"; // 多云
        if (code <= 48) return "🌫️"; // 雾
        if (code <= 55) return "🌦️"; // 毛毛雨
        if (code <= 65) return "🌧️"; // 雨
        if (code <= 77) return "🌨️"; // 雪
        if (code <= 82) return "🌦️"; // 阵雨
        if (code <= 86) return "🌨️"; // 阵雪
        if (code <= 99) return "⛈️"; // 雷雨
        return "🌡️";
    }
    
    private Double getDoubleValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return 0.0;
    }
    
    private Integer getIntValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return 0;
    }
    
    // ============== 数据类 ==============
    
    @Data
    public static class WeatherData {
        private String city;
        private String country;
        private Double temperature;
        private Double feelsLike;
        private Integer humidity;
        private Double windSpeed;
        private String windDirection;
        private Double pressure;
        private Integer cloudCover;
        private Boolean isDay;
        private Integer weatherCode;
        private String description;
        private String icon;
        private String updateTime;
    }
    
    @Data
    public static class ForecastData {
        private java.util.List<DailyForecast> forecasts;
    }
    
    @Data
    public static class DailyForecast {
        private String date;
        private Double tempMax;
        private Double tempMin;
        private Integer weatherCode;
        private String description;
        private String icon;
        private Double precipitation;
    }
    
    @Data
    public static class CityInfo {
        private String name;
        private String country;
        private String admin1;
        private Double latitude;
        private Double longitude;
    }
}
