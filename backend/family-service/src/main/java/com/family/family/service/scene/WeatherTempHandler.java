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
import java.util.List;
import java.util.Map;

/**
 * 温度提醒处理器（高温/低温提醒）
 * 使用 Open-Meteo 免费天气API获取真实预报数据
 */
@Slf4j
@Component
public class WeatherTempHandler implements SceneReminderHandler {

    @Autowired
    private SceneCacheService sceneCacheService;

    private final RestTemplate restTemplate = new RestTemplate();

    // Open-Meteo 天气API
    private static final String WEATHER_API = "https://api.open-meteo.com/v1/forecast";
    private static final String GEOCODING_API = "https://geocoding-api.open-meteo.com/v1/search";

    private static final String SCENE_TYPE = "WEATHER_TEMP";
    private static final String ICON = "🌡️";
    private static final String BG_COLOR = "linear-gradient(135deg, #f093fb 0%, #f5576c 100%)";

    @Override
    public String getSceneType() {
        return SCENE_TYPE;
    }

    @Override
    public String getSceneName() {
        return "温度提醒";
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

            // 验证温度阈值
            if (config.containsKey("tempThreshold")) {
                int threshold = ((Number) config.get("tempThreshold")).intValue();
                if (threshold < -20 || threshold > 50) {
                    throw new IllegalArgumentException("温度阈值应在-20°C至50°C之间");
                }
            }

            // 验证提醒类型
            if (config.containsKey("alertType")) {
                String alertType = (String) config.get("alertType");
                if (!alertType.matches("high|low|both")) {
                    throw new IllegalArgumentException("提醒类型必须是 high/low/both");
                }
            }

        } catch (Exception e) {
            throw new IllegalArgumentException("温度提醒配置格式错误: " + e.getMessage());
        }
    }

    @Override
    public boolean shouldTrigger(Reminder reminder) {
        try {
            // 获取配置的提醒时间
            Map<String, Object> config = JSONUtil.parseObj(reminder.getBusinessData());
            String reminderTime = (String) config.getOrDefault("reminderTime", "08:00");

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
            String cacheKey = String.format("scene:temp:%d:%s", reminder.getId(), today);

            boolean alreadyReminded = sceneCacheService.hasRemindedToday(reminder.getId());
            if (Boolean.TRUE.equals(alreadyReminded)) {
                log.debug("今日已提醒过，跳过温度提醒");
                return false;
            }

            // 获取位置
            String location = (String) config.getOrDefault("location", "auto");
            if ("auto".equals(location)) {
                String userLocation = getUserLocation(reminder.getCreateBy());
                location = userLocation != null ? userLocation : "南京";
            }

            // 获取温度信息
            CurrentWeather weather = getCurrentWeather(location);
            if (weather == null) {
                log.warn("获取天气温度失败，位置: {}", location);
                return false;
            }

            int currentTemp = (int) Math.round(weather.getTemperature());
            int threshold = ((Number) config.getOrDefault("tempThreshold", 35)).intValue();
            String alertType = (String) config.getOrDefault("alertType", "high");

            boolean shouldAlert = false;

            if (("high".equals(alertType) || "both".equals(alertType)) && currentTemp >= threshold) {
                shouldAlert = true;
            }

            if (("low".equals(alertType) || "both".equals(alertType)) && currentTemp <= threshold) {
                shouldAlert = true;
            }

            log.info("温度提醒检查 - 位置: {}, 当前温度: {}, 阈值: {}, 提醒类型: {}, 是否触发: {}",
                location, currentTemp, threshold, alertType, shouldAlert);

            return shouldAlert;

        } catch (Exception e) {
            log.error("检查温度提醒失败: {}", reminder.getId(), e);
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
     * 获取当前天气（调用真实API）
     */
    private CurrentWeather getCurrentWeather(String location) {
        try {
            // 1. 获取坐标
            double[] coords = getCoordinates(location);
            if (coords == null) {
                log.warn("无法获取城市坐标: {}", location);
                return null;
            }

            // 2. 调用天气API - 获取当前温度和今日最高最低温
            String url = String.format(
                "%s?latitude=%.4f&longitude=%.4f" +
                "&current=temperature_2m,weather_code" +
                "&daily=temperature_2m_max,temperature_2m_min" +
                "&timezone=auto" +
                "&forecast_days=2",
                WEATHER_API, coords[0], coords[1]
            );

            log.debug("请求天气API: {}", url);

            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response == null) {
                return null;
            }

            CurrentWeather weather = new CurrentWeather();
            weather.setLocation(location);

            // 解析当前温度
            Map<String, Object> current = (Map<String, Object>) response.get("current");
            if (current != null) {
                Object tempObj = current.get("temperature_2m");
                weather.setTemperature(tempObj instanceof Number ? ((Number) tempObj).doubleValue() : 0);
            }

            // 解析今日最高最低温
            Map<String, Object> daily = (Map<String, Object>) response.get("daily");
            if (daily != null) {
                @SuppressWarnings("unchecked")
                List<Number> maxTemps = (List<Number>) daily.get("temperature_2m_max");
                @SuppressWarnings("unchecked")
                List<Number> minTemps = (List<Number>) daily.get("temperature_2m_min");
                if (maxTemps != null && !maxTemps.isEmpty()) {
                    weather.setMaxTemp(maxTemps.get(0).doubleValue());
                }
                if (minTemps != null && !minTemps.isEmpty()) {
                    weather.setMinTemp(minTemps.get(0).doubleValue());
                }
            }

            log.info("获取{}天气: 当前温度={}°C, 最高={}°C, 最低={}°C",
                location, weather.getTemperature(), weather.getMaxTemp(), weather.getMinTemp());

            return weather;

        } catch (Exception e) {
            log.error("获取天气温度失败: {}", location, e);
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

        String alertType = (String) config.getOrDefault("alertType", "high");
        if (template == null || template.isEmpty()) {
            if ("high".equals(alertType)) {
                template = "🌡️ 高温预警，注意防暑！";
            } else if ("low".equals(alertType)) {
                template = "❄️ 低温预警，注意保暖！";
            } else {
                template = "🌡️ 温度异常提醒";
            }
        }

        String location = (String) config.getOrDefault("location", "auto");
        if ("auto".equals(location)) {
            String userLocation = getUserLocation(reminder.getCreateBy());
            location = userLocation != null ? userLocation : "南京";
        }

        CurrentWeather weather = getCurrentWeather(location);
        int currentTemp = weather != null ? (int) Math.round(weather.getTemperature()) : 0;

        return template
            .replace("{userName}", user.getNickname() != null ? user.getNickname() : "亲爱的")
            .replace("{currentTemp}", String.valueOf(currentTemp))
            .replace("{location}", location);
    }

    @Override
    public String renderContent(Reminder reminder, User user) {
        Map<String, Object> config = JSONUtil.parseObj(reminder.getBusinessData());
        String template = reminder.getContentTemplate();

        if (template == null || template.isEmpty()) {
            template = "{userName}，{location}当前气温{currentTemp}°C，{tempAdvice}\n\n{healthTips}";
        }

        String location = (String) config.getOrDefault("location", "auto");
        if ("auto".equals(location)) {
            String userLocation = getUserLocation(reminder.getCreateBy());
            location = userLocation != null ? userLocation : "南京";
        }

        CurrentWeather weather = getCurrentWeather(location);
        int currentTemp = weather != null ? (int) Math.round(weather.getTemperature()) : 0;
        int maxTemp = weather != null ? (int) Math.round(weather.getMaxTemp()) : 0;
        int minTemp = weather != null ? (int) Math.round(weather.getMinTemp()) : 0;

        String alertType = (String) config.getOrDefault("alertType", "high");
        int threshold = ((Number) config.getOrDefault("tempThreshold", 35)).intValue();

        String tempAdvice;
        String healthTips;

        if ("high".equals(alertType) || currentTemp >= 35) {
            tempAdvice = "今日最高温" + maxTemp + "°C，当前" + currentTemp + "°C，天气较热，请注意防暑降温！";
            healthTips = "💡 防暑小贴士：\n" +
                "1. 多喝水，及时补充流失的水分\n" +
                "2. 避免在中午12点至下午3点外出\n" +
                "3. 穿着宽松透气的衣物\n" +
                "4. 室内保持通风，适当使用空调";
        } else {
            tempAdvice = "今日最低温" + minTemp + "°C，当前" + currentTemp + "°C，天气较冷，请注意保暖！";
            healthTips = "💡 保暖小贴士：\n" +
                "1. 及时增添衣物，注意防寒\n" +
                "2. 多喝热水，保持身体温暖\n" +
                "3. 注意手脚保暖，避免冻伤\n" +
                "4. 适当运动，促进血液循环";
        }

        return template
            .replace("{userName}", user.getNickname() != null ? user.getNickname() : "亲爱的")
            .replace("{location}", location)
            .replace("{currentTemp}", String.valueOf(currentTemp))
            .replace("{tempAdvice}", tempAdvice)
            .replace("{healthTips}", healthTips);
    }

    @Override
    public SceneTemplate getDefaultTemplate() {
        return SceneTemplate.builder()
            .sceneType(SCENE_TYPE)
            .reminderName("🌡️ 高温提醒")
            .reminderType("WEATHER_TEMP")
            .frequencyType("DAILY")
            .frequencyConfig("{\"fixedTime\": \"08:00\"}")
            .titleTemplate("🌡️ 高温预警，注意防暑！")
            .contentTemplate("{userName}，{location}当前气温{currentTemp}°C，{tempAdvice}\n\n{healthTips}")
            .businessData("{\"sceneType\": \"WEATHER_TEMP\", \"location\": \"auto\", \"reminderTime\": \"08:00\", \"tempThreshold\": 35, \"alertType\": \"high\"}")
            .icon(ICON)
            .description("温度超过35°C时自动提醒防暑")
            .bgColor(BG_COLOR)
            .build();
    }

    /**
     * 标记今日已提醒
     */
    public void markReminded(Long reminderId, Long userId) {
        sceneCacheService.markRemindedToday(reminderId, userId, getSceneType());
    }

    // ========== 内部类 ==========

    @lombok.Data
    public static class CurrentWeather {
        private String location;
        private double temperature;
        private double maxTemp;
        private double minTemp;
    }
}
