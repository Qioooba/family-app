package com.family.family.service.scene;

import cn.hutool.json.JSONUtil;
import com.family.family.entity.Reminder;
import com.family.family.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 空气质量/PM2.5提醒处理器
 * 使用 Open-Meteo 免费空气质量API (无需API Key)
 */
@Slf4j
@Component
public class AirQualityHandler implements SceneReminderHandler {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private final RestTemplate restTemplate = new RestTemplate();

    // Open-Meteo 空气质量API
    private static final String AIR_QUALITY_API = "https://air-quality-api.open-meteo.com/v1/air-quality";

    // Open-Meteo 地理编码API
    private static final String GEOCODING_API = "https://geocoding-api.open-meteo.com/v1/search";

    private static final String SCENE_TYPE = "AIR_QUALITY";
    private static final String ICON = "🌫️";
    private static final String BG_COLOR = "linear-gradient(135deg, #434343 0%, #000000 100%)";

    @Override
    public String getSceneType() {
        return SCENE_TYPE;
    }

    @Override
    public String getSceneName() {
        return "空气质量提醒";
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
            // 验证必要字段
            if (config.containsKey("location")) {
                String location = (String) config.get("location");
                if (location == null || location.isEmpty()) {
                    throw new IllegalArgumentException("位置不能为空");
                }
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("空气质量提醒配置格式错误: " + e.getMessage());
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
            String cacheKey = String.format("scene:air_quality:%d:%s", reminder.getId(), today);

            Boolean alreadyReminded = redisTemplate.hasKey(cacheKey);
            if (Boolean.TRUE.equals(alreadyReminded)) {
                log.debug("今日已提醒过，跳过: {}", reminder.getReminderName());
                return false;
            }

            String location = (String) config.getOrDefault("location", "auto");

            // 如果是auto，尝试获取用户定位信息
            if ("auto".equals(location)) {
                String userLocation = getUserLocation(reminder.getCreateBy());
                if (userLocation != null) {
                    location = userLocation;
                } else {
                    location = "南京";
                }
            }

            // 获取空气质量信息
            AirQualityInfo airQuality = getAirQualityInfo(location);
            if (airQuality == null) {
                log.warn("获取空气质量信息失败，位置: {}", location);
                return false;
            }

            // 获取配置的阈值
            int pm25Threshold = ((Number) config.getOrDefault("pm25Threshold", 75)).intValue();
            int pm10Threshold = ((Number) config.getOrDefault("pm10Threshold", 150)).intValue();
            int aqiThreshold = ((Number) config.getOrDefault("aqiThreshold", 100)).intValue();

            // 判断是否超过阈值
            boolean shouldRemind = false;
            String reason = "";

            if (airQuality.getPm25() != null && airQuality.getPm25() > pm25Threshold) {
                shouldRemind = true;
                reason = String.format("PM2.5超标: %d > %d", airQuality.getPm25(), pm25Threshold);
            } else if (airQuality.getPm10() != null && airQuality.getPm10() > pm10Threshold) {
                shouldRemind = true;
                reason = String.format("PM10超标: %d > %d", airQuality.getPm10(), pm10Threshold);
            } else if (airQuality.getUsAqi() != null && airQuality.getUsAqi() > aqiThreshold) {
                shouldRemind = true;
                reason = String.format("AQI超标: %d > %d", airQuality.getUsAqi(), aqiThreshold);
            }

            log.info("空气质量提醒检查 - 位置: {}, PM2.5: {}, PM10: {}, AQI: {}, 阈值: PM2.5={}, PM10={}, AQI={}, 触发: {}, 原因: {}",
                location, airQuality.getPm25(), airQuality.getPm10(), airQuality.getUsAqi(),
                pm25Threshold, pm10Threshold, aqiThreshold, shouldRemind, reason);

            return shouldRemind;

        } catch (Exception e) {
            log.error("检查空气质量提醒失败: {}", reminder.getId(), e);
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
     * 获取用户定位信息
     */
    private String getUserLocation(Long userId) {
        try {
            String locationKey = String.format("user:location:%d", userId);
            String location = redisTemplate.opsForValue().get(locationKey);
            if (location != null && !location.isEmpty()) {
                return location;
            }
            return null;
        } catch (Exception e) {
            log.warn("获取用户定位失败: {}", userId, e);
            return null;
        }
    }

    @Override
    public String renderTitle(Reminder reminder, User user) {
        Map<String, Object> config = JSONUtil.parseObj(reminder.getBusinessData());
        String template = reminder.getTitleTemplate();

        if (template == null || template.isEmpty()) {
            template = "🌫️ 空气质量较差，注意防护！";
        }

        String location = (String) config.getOrDefault("location", "auto");
        if ("auto".equals(location)) {
            String userLocation = getUserLocation(reminder.getCreateBy());
            location = userLocation != null ? userLocation : "南京";
        }

        AirQualityInfo airQuality = getAirQualityInfo(location);

        String aqiText = "优";
        if (airQuality != null && airQuality.getUsAqi() != null) {
            aqiText = getAqiLevel(airQuality.getUsAqi());
        }

        return template
            .replace("{userName}", user.getNickname() != null ? user.getNickname() : "亲爱的")
            .replace("{location}", location)
            .replace("{aqiLevel}", aqiText);
    }

    @Override
    public String renderContent(Reminder reminder, User user) {
        Map<String, Object> config = JSONUtil.parseObj(reminder.getBusinessData());
        String template = reminder.getContentTemplate();

        if (template == null || template.isEmpty()) {
            template = "{userName}，当前位置{location}的空气质量{aqiLevel}，PM2.5浓度{pm25}μg/m³，建议减少户外活动，外出佩戴口罩😷";
        }

        String location = (String) config.getOrDefault("location", "auto");
        if ("auto".equals(location)) {
            String userLocation = getUserLocation(reminder.getCreateBy());
            location = userLocation != null ? userLocation : "南京";
        }

        AirQualityInfo airQuality = getAirQualityInfo(location);

        String aqiText = "优";
        String pm25Text = "--";
        String pm10Text = "--";

        if (airQuality != null) {
            if (airQuality.getUsAqi() != null) {
                aqiText = getAqiLevel(airQuality.getUsAqi());
            }
            if (airQuality.getPm25() != null) {
                pm25Text = airQuality.getPm25().toString();
            }
            if (airQuality.getPm10() != null) {
                pm10Text = airQuality.getPm10().toString();
            }
        }

        return template
            .replace("{userName}", user.getNickname() != null ? user.getNickname() : "亲爱的")
            .replace("{location}", location)
            .replace("{aqiLevel}", aqiText)
            .replace("{pm25}", pm25Text)
            .replace("{pm10}", pm10Text);
    }

    @Override
    public SceneTemplate getDefaultTemplate() {
        return SceneTemplate.builder()
            .sceneType(SCENE_TYPE)
            .reminderName("🌫️ 南京空气质量提醒")
            .reminderType("AIR_QUALITY")
            .frequencyType("DAILY")
            .frequencyConfig("{\"fixedTime\": \"07:00\"}")
            .titleTemplate("🌫️ 空气质量较差，注意防护！")
            .contentTemplate("{userName}，当前位置{location}的空气质量{aqiLevel}，PM2.5浓度{pm25}μg/m³，建议减少户外活动，外出佩戴口罩😷")
            .businessData("{\"sceneType\": \"AIR_QUALITY\", \"location\": \"auto\", \"reminderTime\": \"07:00\", \"pm25Threshold\": 75, \"pm10Threshold\": 150, \"aqiThreshold\": 100}")
            .icon(ICON)
            .description("每天早上7点检查空气质量，PM2.5或AQI超标时提醒（优先使用您的定位）")
            .bgColor(BG_COLOR)
            .build();
    }

    /**
     * 获取空气质量信息（调用 Open-Meteo API）
     */
    private AirQualityInfo getAirQualityInfo(String location) {
        try {
            // 1. 先通过城市名获取坐标
            double[] coords = getCoordinates(location);
            if (coords == null) {
                log.warn("无法获取城市坐标: {}", location);
                return getDefaultAirQuality(location);
            }

            // 2. 调用空气质量API
            String url = String.format(
                "%s?latitude=%.4f&longitude=%.4f&current=pm2_5,pm10,us_aqi, european_aqi",
                AIR_QUALITY_API, coords[0], coords[1]
            );

            log.debug("请求空气质量API: {}", url);

            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response == null || !response.containsKey("current")) {
                log.warn("空气质量API返回为空");
                return getDefaultAirQuality(location);
            }

            Map<String, Object> current = (Map<String, Object>) response.get("current");

            AirQualityInfo info = new AirQualityInfo();
            info.setLocation(location);

            // 解析PM2.5
            Object pm25Value = current.get("pm2_5");
            if (pm25Value instanceof Number) {
                info.setPm25(((Number) pm25Value).doubleValue());
            }

            // 解析PM10
            Object pm10Value = current.get("pm10");
            if (pm10Value instanceof Number) {
                info.setPm10(((Number) pm10Value).doubleValue());
            }

            // 解析US AQI
            Object usAqiValue = current.get("us_aqi");
            if (usAqiValue instanceof Number) {
                info.setUsAqi(((Number) usAqiValue).intValue());
            }

            // 解析欧洲AQI
            Object euAqiValue = current.get("european_aqi");
            if (euAqiValue instanceof Number) {
                info.setEuAqi(((Number) euAqiValue).intValue());
            }

            log.info("获取{}空气质量: PM2.5={}, PM10={}, US_AQI={}, EU_AQI={}",
                location, info.getPm25(), info.getPm10(), info.getUsAqi(), info.getEuAqi());

            return info;

        } catch (Exception e) {
            log.error("获取空气质量信息失败: {}", location, e);
            return getDefaultAirQuality(location);
        }
    }

    /**
     * 获取城市坐标
     */
    private double[] getCoordinates(String cityName) {
        try {
            // 尝试直接从坐标解析（如果已经是坐标格式）
            if (cityName.matches(".*\\d+.*")) {
                // 简单的坐标解析，尝试提取数字
                String[] parts = cityName.replaceAll("[^0-9.,-]", "").split(",");
                if (parts.length >= 2) {
                    try {
                        double lat = Double.parseDouble(parts[0].trim());
                        double lon = Double.parseDouble(parts[1].trim());
                        return new double[]{lat, lon};
                    } catch (NumberFormatException ignored) {}
                }
            }

            // 使用地理编码API获取坐标
            String url = String.format("%s?name=%s&count=1&language=zh&format=json",
                GEOCODING_API, java.net.URLEncoder.encode(cityName, "UTF-8"));

            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response == null || !response.containsKey("results")) {
                return null;
            }

            @SuppressWarnings("unchecked")
            java.util.List<Map<String, Object>> results = (java.util.List<Map<String, Object>>) response.get("results");
            if (results == null || results.isEmpty()) {
                return null;
            }

            Map<String, Object> firstResult = results.get(0);
            Map<String, Object> latitude = (Map<String, Object>) firstResult.get("latitude");
            Map<String, Object> longitude = (Map<String, Object>) firstResult.get("longitude");

            if (latitude != null && longitude != null) {
                return new double[]{
                    ((Number) latitude.get("value")).doubleValue(),
                    ((Number) longitude.get("value")).doubleValue()
                };
            }

            return null;

        } catch (Exception e) {
            log.error("获取城市坐标失败: {}", cityName, e);
            return null;
        }
    }

    /**
     * 默认空气质量（API失败时返回）
     */
    private AirQualityInfo getDefaultAirQuality(String location) {
        AirQualityInfo info = new AirQualityInfo();
        info.setLocation(location);
        // 返回良好默认值，避免误报
        info.setPm25(30.0);
        info.setPm10(50.0);
        info.setUsAqi(40);
        return info;
    }

    /**
     * 根据AQI获取等级描述
     */
    private String getAqiLevel(int aqi) {
        if (aqi <= 50) return "优";
        if (aqi <= 100) return "良";
        if (aqi <= 150) return "轻度污染";
        if (aqi <= 200) return "中度污染";
        if (aqi <= 300) return "重度污染";
        return "严重污染";
    }

    /**
     * 标记今日已提醒
     */
    public void markReminded(Long reminderId) {
        String today = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        String cacheKey = String.format("scene:air_quality:%d:%s", reminderId, today);
        redisTemplate.opsForValue().set(cacheKey, "1", 24, TimeUnit.HOURS);
    }

    /**
     * 空气质量信息内部类
     */
    @lombok.Data
    public static class AirQualityInfo {
        private String location;
        private Double pm25;
        private Double pm10;
        private Integer usAqi;
        private Integer euAqi;
    }
}
