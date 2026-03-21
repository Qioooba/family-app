package com.family.family.service.scene;

import cn.hutool.json.JSONUtil;
import com.family.family.entity.Reminder;
import com.family.family.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import com.family.family.service.SceneCacheService;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 下雨提醒处理器
 * 使用 Open-Meteo 免费天气API获取真实预报数据
 */
@Slf4j
@Component
public class WeatherRainHandler implements SceneReminderHandler {

    @Autowired
    private SceneCacheService sceneCacheService;

    private final RestTemplate restTemplate = new RestTemplate();

    // Open-Meteo 天气API
    private static final String WEATHER_API = "https://api.open-meteo.com/v1/forecast";
    private static final String GEOCODING_API = "https://geocoding-api.open-meteo.com/v1/search";

    private static final String SCENE_TYPE = "WEATHER_RAIN";
    private static final String ICON = "🌧️";
    private static final String BG_COLOR = "linear-gradient(135deg, #667eea 0%, #764ba2 100%)";

    @Override
    public String getSceneType() {
        return SCENE_TYPE;
    }

    @Override
    public String getSceneName() {
        return "下雨提醒";
    }

    @Override
    public String getIcon() {
        return ICON;
    }

    @Override
    public String getBgColor() {
        return BG_COLOR;
    }

    @Override
    public void validateConfig(String businessData) {
        try {
            Map<String, Object> config = JSONUtil.parseObj(businessData);
            if (config.containsKey("location")) {
                String location = (String) config.get("location");
                if (location == null || location.isEmpty()) {
                    throw new IllegalArgumentException("位置不能为空");
                }
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("下雨提醒配置格式错误: " + e.getMessage());
        }
    }

    @Override
    public boolean shouldTrigger(Reminder reminder) {
        try {
            // 获取配置的提醒时间
            Map<String, Object> config = JSONUtil.parseObj(reminder.getBusinessData());
            String reminderTime = (String) config.getOrDefault("reminderTime", "07:00");

            // 检查当前时间是否到达提醒时间（允许5分钟误差）
            LocalDateTime now = LocalDateTime.now();
            String currentTime = String.format("%02d:%02d", now.getHour(), now.getMinute());

            int timeDiff = compareTime(currentTime, reminderTime);
            if (timeDiff < -5 || timeDiff > 5) {
                log.debug("未到达提醒时间 {}，当前时间 {}，跳过", reminderTime, currentTime);
                return false;
            }

            // 检查今日是否已提醒
            String today = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
            String cacheKey = String.format("scene:rain:%d:%s", reminder.getId(), today);

            boolean alreadyReminded = sceneCacheService.hasRemindedToday(reminder.getId());
            if (Boolean.TRUE.equals(alreadyReminded)) {
                log.debug("今日已提醒过，跳过: {}", reminder.getReminderName());
                return false;
            }

            // 获取位置
            String location = (String) config.getOrDefault("location", "auto");
            if ("auto".equals(location)) {
                String userLocation = getUserLocation(reminder.getCreateBy());
                location = userLocation != null ? userLocation : "南京";
            }

            // 获取天气预报数据
            WeatherForecast forecast = getWeatherForecast(location);
            if (forecast == null) {
                log.warn("获取天气预报失败，位置: {}", location);
                return false;
            }

            // 获取用户设置的阈值
            int rainProbabilityThreshold = ((Number) config.getOrDefault("rainProbability", 30)).intValue();
            int rainHoursAhead = ((Number) config.getOrDefault("rainHoursAhead", 3)).intValue(); // 默认检查未来3小时

            // 检查未来N小时内是否有雨
            boolean willRain = checkWillRain(forecast, rainHoursAhead, rainProbabilityThreshold);

            log.info("下雨提醒检查 - 位置: {}, 未来{}小时降雨概率阈值: {}, 实际最高概率: {}, 触发: {}",
                location, rainHoursAhead, rainProbabilityThreshold, forecast.getMaxRainProbability(), willRain);

            return willRain;

        } catch (Exception e) {
            log.error("检查下雨提醒失败: {}", reminder.getId(), e);
            return false;
        }
    }

    /**
     * 比较时间
     */
    private int compareTime(String time1, String time2) {
        try {
            int h1 = Integer.parseInt(time1.split(":")[0]);
            int m1 = Integer.parseInt(time1.split(":")[1]);
            int h2 = Integer.parseInt(time2.split(":")[0]);
            int m2 = Integer.parseInt(time2.split(":")[1]);
            return (h1 * 60 + m1) - (h2 * 60 + m2);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 获取用户定位
     */
    private String getUserLocation(Long userId) {
        try {
            return sceneCacheService.getUserLocation(userId);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 检查未来N小时内是否下雨
     */
    private boolean checkWillRain(WeatherForecast forecast, int hoursAhead, int threshold) {
        // 检查逐小时预报
        for (HourlyWeather hourly : forecast.getHourlyForecasts()) {
            if (hourly.getRainProbability() >= threshold) {
                return true;
            }
        }

        // 也检查每日预报的降雨概率
        if (forecast.getMaxDailyRainProbability() >= threshold) {
            return true;
        }

        return false;
    }

    /**
     * 获取天气预报（调用真实API）
     */
    private WeatherForecast getWeatherForecast(String location) {
        try {
            // 1. 获取坐标
            double[] coords = getCoordinates(location);
            if (coords == null) {
                log.warn("无法获取城市坐标: {}", location);
                return null;
            }

            // 2. 调用天气API - 获取逐小时预报（未来48小时）
            String url = String.format(
                "%s?latitude=%.4f&longitude=%.4f" +
                "&hourly=weather_code,precipitation_probability,precipitation,temperature_2m" +
                "&daily=weather_code,precipitation_probability_max,temperature_2m_max,temperature_2m_min" +
                "&timezone=auto" +
                "&forecast_days=2",
                WEATHER_API, coords[0], coords[1]
            );

            log.debug("请求天气API: {}", url);

            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response == null) {
                return null;
            }

            WeatherForecast forecast = new WeatherForecast();
            forecast.setLocation(location);

            // 解析逐小时数据
            Map<String, Object> hourly = (Map<String, Object>) response.get("hourly");
            if (hourly != null) {
                List<String> times = (List<String>) hourly.get("time");
                List<Number> rainProbs = (List<Number>) hourly.get("precipitation_probability");
                List<Number> temps = (List<Number>) hourly.get("temperature_2m");

                if (times != null) {
                    for (int i = 0; i < Math.min(times.size(), 48); i++) {
                        HourlyWeather hw = new HourlyWeather();
                        hw.setTime(times.get(i));
                        hw.setRainProbability(rainProbs != null && i < rainProbs.size() ?
                            rainProbs.get(i).intValue() : 0);
                        hw.setTemperature(temps != null && i < temps.size() ?
                            temps.get(i).doubleValue() : 0);
                        forecast.getHourlyForecasts().add(hw);
                    }
                }
            }

            // 解析每日数据
            Map<String, Object> daily = (Map<String, Object>) response.get("daily");
            if (daily != null) {
                List<Number> dailyRainProbs = (List<Number>) daily.get("precipitation_probability_max");
                if (dailyRainProbs != null && !dailyRainProbs.isEmpty()) {
                    forecast.setMaxDailyRainProbability(dailyRainProbs.get(0).intValue());
                }
            }

            // 计算最大降雨概率
            int maxProb = 0;
            for (HourlyWeather hw : forecast.getHourlyForecasts()) {
                if (hw.getRainProbability() > maxProb) {
                    maxProb = hw.getRainProbability();
                }
            }
            forecast.setMaxRainProbability(maxProb);

            log.info("获取{}天气预报: 未来48小时最高降雨概率={}%, 明日最高概率={}%",
                location, forecast.getMaxRainProbability(), forecast.getMaxDailyRainProbability());

            return forecast;

        } catch (Exception e) {
            log.error("获取天气预报失败: {}", location, e);
            return null;
        }
    }

    /**
     * 获取城市坐标
     */
    private double[] getCoordinates(String cityName) {
        try {
            // 如果是坐标格式
            if (cityName.matches(".*\\d+.*")) {
                String[] parts = cityName.replaceAll("[^0-9.,-]", "").split(",");
                if (parts.length >= 2) {
                    try {
                        return new double[]{Double.parseDouble(parts[0].trim()), Double.parseDouble(parts[1].trim())};
                    } catch (NumberFormatException ignored) {}
                }
            }

            // 地理编码
            String url = String.format("%s?name=%s&count=1&language=zh",
                GEOCODING_API, java.net.URLEncoder.encode(cityName, "UTF-8"));

            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response == null || !response.containsKey("results")) {
                return null;
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("results");
            if (results == null || results.isEmpty()) {
                return null;
            }

            Map<String, Object> first = results.get(0);
            Object lat = first.get("latitude");
            Object lon = first.get("longitude");

            if (lat != null && lon != null) {
                return new double[]{((Number) lat).doubleValue(), ((Number) lon).doubleValue()};
            }

            return null;
        } catch (Exception e) {
            log.error("获取城市坐标失败: {}", cityName, e);
            return null;
        }
    }

    @Override
    public String renderTitle(Reminder reminder, User user) {
        Map<String, Object> config = JSONUtil.parseObj(reminder.getBusinessData());
        String template = reminder.getTitleTemplate();

        if (template == null || template.isEmpty()) {
            template = "🌧️ 今日有雨，记得带伞！";
        }

        String location = (String) config.getOrDefault("location", "auto");
        if ("auto".equals(location)) {
            String userLocation = getUserLocation(reminder.getCreateBy());
            location = userLocation != null ? userLocation : "南京";
        }

        return template
            .replace("{userName}", user.getNickname() != null ? user.getNickname() : "亲爱的")
            .replace("{location}", location);
    }

    @Override
    public String renderContent(Reminder reminder, User user) {
        Map<String, Object> config = JSONUtil.parseObj(reminder.getBusinessData());
        String template = reminder.getContentTemplate();

        if (template == null || template.isEmpty()) {
            template = "{userName}，{location}今天有雨，出门记得带{umbrellaIcon}！";
        }

        String location = (String) config.getOrDefault("location", "auto");
        if ("auto".equals(location)) {
            String userLocation = getUserLocation(reminder.getCreateBy());
            location = userLocation != null ? userLocation : "南京";
        }

        // 获取最新预报
        WeatherForecast forecast = getWeatherForecast(location);

        String rainTime = "今天";
        String rainProb = "--";

        if (forecast != null) {
            rainProb = forecast.getMaxRainProbability() + "%";
            // 找到第一次高概率降雨时间
            int threshold = ((Number) config.getOrDefault("rainProbability", 30)).intValue();
            for (HourlyWeather hw : forecast.getHourlyForecasts()) {
                if (hw.getRainProbability() >= threshold) {
                    if (hw.getTime() != null && hw.getTime().contains("T")) {
                        rainTime = hw.getTime().split("T")[1].substring(0, 5);
                    }
                    break;
                }
            }
        }

        return template
            .replace("{userName}", user.getNickname() != null ? user.getNickname() : "亲爱的")
            .replace("{location}", location)
            .replace("{rainTime}", rainTime)
            .replace("{rainProbability}", rainProb)
            .replace("{umbrellaIcon}", "☔");
    }

    @Override
    public SceneTemplate getDefaultTemplate() {
        return SceneTemplate.builder()
            .sceneType(SCENE_TYPE)
            .reminderName("🌧️ 下雨提醒")
            .reminderType("WEATHER_RAIN")
            .frequencyType("DAILY")
            .frequencyConfig("{\"fixedTime\": \"07:00\"}")
            .titleTemplate("🌧️ 今日有雨，记得带伞！")
            .contentTemplate("{userName}，{location}{rainTime}有雨，降雨概率{rainProbability}，出门记得带☔！")
            .businessData("{\"sceneType\": \"WEATHER_RAIN\", \"location\": \"auto\", \"reminderTime\": \"07:00\", \"rainProbability\": 30, \"rainHoursAhead\": 3}")
            .icon(ICON)
            .description("每天早上7点检查天气，预报有雨时自动提醒带伞")
            .bgColor(BG_COLOR)
            .build();
    }

    // ========== 内部类 ==========

    @lombok.Data
    public static class WeatherForecast {
        private String location;
        private int maxRainProbability;
        private int maxDailyRainProbability;
        private List<HourlyWeather> hourlyForecasts = new ArrayList<>();
    }

    @lombok.Data
    public static class HourlyWeather {
        private String time;
        private int rainProbability;
        private double temperature;
    }
}
