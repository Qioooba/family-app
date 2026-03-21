package com.family.family.service.scene;

import cn.hutool.json.JSONUtil;
import com.family.family.entity.Reminder;
import com.family.family.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import com.family.family.service.SceneCacheService;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalTime;
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
            Integer highThreshold = resolveHighThreshold(config);
            Integer lowThreshold = resolveLowThreshold(config);
            if (highThreshold != null && (highThreshold < -20 || highThreshold > 50)) {
                throw new IllegalArgumentException("高温阈值应在-20°C至50°C之间");
            }
            if (lowThreshold != null && (lowThreshold < -20 || lowThreshold > 50)) {
                throw new IllegalArgumentException("低温阈值应在-20°C至50°C之间");
            }
            if (highThreshold != null && lowThreshold != null && lowThreshold >= highThreshold) {
                throw new IllegalArgumentException("低温阈值必须小于高温阈值");
            }
            if (config.containsKey("workHours")) {
                @SuppressWarnings("unchecked")
                List<String> workHours = (List<String>) config.get("workHours");
                if (workHours == null || workHours.size() != 2) {
                    throw new IllegalArgumentException("监测时间段格式错误");
                }
                LocalTime.parse(workHours.get(0));
                LocalTime.parse(workHours.get(1));
            }

        } catch (Exception e) {
            throw new IllegalArgumentException("温度提醒配置格式错误: " + e.getMessage());
        }
    }

    @Override
    public boolean shouldTrigger(Reminder reminder) {
        try {
            Map<String, Object> config = JSONUtil.parseObj(reminder.getBusinessData());

            // 获取监测间隔（分钟）
            int intervalMinutes = ((Number) config.getOrDefault("intervalMinutes", 120)).intValue();

            // 检查是否到达监测间隔
            boolean shouldNotify = sceneCacheService.shouldNotifyAgain(reminder.getId(), intervalMinutes);
            if (!shouldNotify) {
                log.debug("温度提醒未到达间隔 {} 分钟，跳过: {}", intervalMinutes, reminder.getReminderName());
                return false;
            }

            boolean allDay = Boolean.TRUE.equals(config.get("allDay"));
            if (!allDay) {
                @SuppressWarnings("unchecked")
                List<String> workHours = (List<String>) config.get("workHours");
                if (workHours != null && workHours.size() == 2) {
                    LocalTime now = LocalTime.now();
                    LocalTime start = LocalTime.parse(workHours.get(0));
                    LocalTime end = LocalTime.parse(workHours.get(1));
                    if (now.isBefore(start) || now.isAfter(end)) {
                        log.debug("不在温度提醒监测时间段内，跳过: {}-{}", start, end);
                        return false;
                    }
                }
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
            Integer highThreshold = resolveHighThreshold(config);
            Integer lowThreshold = resolveLowThreshold(config);
            boolean shouldAlert = (highThreshold != null && currentTemp >= highThreshold)
                || (lowThreshold != null && currentTemp <= lowThreshold);

            log.info("温度提醒检查 - 位置: {}, 当前温度: {}, 高温阈值: {}, 低温阈值: {}, 是否触发: {}",
                location, currentTemp, highThreshold, lowThreshold, shouldAlert);

            return shouldAlert;

        } catch (Exception e) {
            log.error("检查温度提醒失败: {}", reminder.getId(), e);
            return false;
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

        String location = (String) config.getOrDefault("location", "auto");
        if ("auto".equals(location)) {
            String userLocation = getUserLocation(reminder.getCreateBy());
            location = userLocation != null ? userLocation : "南京";
        }

        CurrentWeather weather = getCurrentWeather(location);
        int currentTemp = weather != null ? (int) Math.round(weather.getTemperature()) : 0;
        Integer highThreshold = resolveHighThreshold(config);
        Integer lowThreshold = resolveLowThreshold(config);

        if (template == null || template.isEmpty()) {
            if (highThreshold != null && currentTemp >= highThreshold) {
                template = "🌡️ 高温预警，注意防暑！";
            } else if (lowThreshold != null && currentTemp <= lowThreshold) {
                template = "❄️ 低温预警，注意保暖！";
            } else {
                template = "🌡️ 温度提醒";
            }
        }

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

        Integer highThreshold = resolveHighThreshold(config);
        Integer lowThreshold = resolveLowThreshold(config);

        String tempAdvice;
        String healthTips;

        if (highThreshold != null && currentTemp >= highThreshold) {
            tempAdvice = "今日最高温" + maxTemp + "°C，当前" + currentTemp + "°C，天气较热，请注意防暑降温！";
            healthTips = "💡 防暑小贴士：\n" +
                "1. 多喝水，及时补充流失的水分\n" +
                "2. 避免在中午12点至下午3点外出\n" +
                "3. 穿着宽松透气的衣物\n" +
                "4. 室内保持通风，适当使用空调";
        } else if (lowThreshold != null && currentTemp <= lowThreshold) {
            tempAdvice = "今日最低温" + minTemp + "°C，当前" + currentTemp + "°C，天气较冷，请注意保暖！";
            healthTips = "💡 保暖小贴士：\n" +
                "1. 及时增添衣物，注意防寒\n" +
                "2. 多喝热水，保持身体温暖\n" +
                "3. 注意手脚保暖，避免冻伤\n" +
                "4. 适当运动，促进血液循环";
        } else {
            tempAdvice = "当前气温" + currentTemp + "°C，今日最高" + maxTemp + "°C，最低" + minTemp + "°C。";
            healthTips = "💡 可根据当天温差及时增减衣物，注意补水与休息。";
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
            .reminderName("🌡️ 温度提醒")
            .reminderType("WEATHER_TEMP")
            .frequencyType("INTERVAL")
            .frequencyConfig("{\"intervalMinutes\": 120}")
            .titleTemplate("")
            .contentTemplate("")
            .businessData("{\"sceneType\": \"WEATHER_TEMP\", \"location\": \"auto\", \"intervalMinutes\": 120, \"highTempThreshold\": 35, \"lowTempThreshold\": 5, \"allDay\": false, \"workHours\": [\"07:00\", \"22:00\"]}")
            .icon(ICON)
            .description("在常用活动时段监测温度变化，过热或过冷时提醒")
            .bgColor(BG_COLOR)
            .build();
    }

    private Integer resolveHighThreshold(Map<String, Object> config) {
        Object value = config.get("highTempThreshold");
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value != null) {
            try {
                return Integer.parseInt(String.valueOf(value));
            } catch (Exception ignored) {
            }
        }
        Object legacy = config.get("tempThreshold");
        String alertType = String.valueOf(config.getOrDefault("alertType", "high"));
        if ("low".equals(alertType)) {
            return 35;
        }
        if (legacy instanceof Number number) {
            return number.intValue();
        }
        return 35;
    }

    private Integer resolveLowThreshold(Map<String, Object> config) {
        Object value = config.get("lowTempThreshold");
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value != null) {
            try {
                return Integer.parseInt(String.valueOf(value));
            } catch (Exception ignored) {
            }
        }
        Object legacy = config.get("tempThreshold");
        String alertType = String.valueOf(config.getOrDefault("alertType", "high"));
        if ("low".equals(alertType) && legacy instanceof Number number) {
            return number.intValue();
        }
        return 5;
    }

    /**
     * 标记已提醒（使用时间戳，支持间隔提醒）
     */
    public void markReminded(Long reminderId, Long userId) {
        sceneCacheService.markNotified(reminderId, userId, getSceneType());
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
