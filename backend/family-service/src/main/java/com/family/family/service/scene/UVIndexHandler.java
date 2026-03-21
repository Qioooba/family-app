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
import java.util.Map;

/**
 * 紫外线提醒处理器
 * 使用 Open-Meteo 免费API (无需API Key)
 */
@Slf4j
@Component
public class UVIndexHandler implements SceneReminderHandler {

    @Autowired
    private SceneCacheService sceneCacheService;

    private final RestTemplate restTemplate = new RestTemplate();

    // Open-Meteo 天气API (包含紫外线指数)
    private static final String WEATHER_API = "https://api.open-meteo.com/v1/forecast";

    // Open-Meteo 地理编码API
    private static final String GEOCODING_API = "https://geocoding-api.open-meteo.com/v1/search";

    private static final String SCENE_TYPE = "UV_INDEX";
    private static final String ICON = "☀️";
    private static final String BG_COLOR = "linear-gradient(135deg, #f6d365 0%, #fda085 100%)";

    @Override
    public String getSceneType() {
        return SCENE_TYPE;
    }

    @Override
    public String getSceneName() {
        return "紫外线提醒";
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
            if (config.containsKey("workHours")) {
                @SuppressWarnings("unchecked")
                java.util.List<String> workHours = (java.util.List<String>) config.get("workHours");
                if (workHours == null || workHours.size() != 2) {
                    throw new IllegalArgumentException("监测时间段格式错误");
                }
                LocalTime.parse(workHours.get(0));
                LocalTime.parse(workHours.get(1));
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("紫外线提醒配置格式错误: " + e.getMessage());
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
                log.debug("紫外线提醒未到达间隔 {} 分钟，跳过: {}", intervalMinutes, reminder.getReminderName());
                return false;
            }

            boolean allDay = Boolean.TRUE.equals(config.get("allDay"));
            if (!allDay) {
                @SuppressWarnings("unchecked")
                java.util.List<String> workHours = (java.util.List<String>) config.get("workHours");
                if (workHours != null && workHours.size() == 2) {
                    LocalTime now = LocalTime.now();
                    LocalTime start = LocalTime.parse(workHours.get(0));
                    LocalTime end = LocalTime.parse(workHours.get(1));
                    if (now.isBefore(start) || now.isAfter(end)) {
                        log.debug("不在紫外线提醒监测时间段内，跳过: {}-{}", start, end);
                        return false;
                    }
                }
            }

            // 获取位置
            String location = (String) config.getOrDefault("location", "auto");
            if ("auto".equals(location)) {
                String userLocation = getUserLocation(reminder.getCreateBy());
                if (userLocation != null) {
                    location = userLocation;
                } else {
                    location = "南京";
                }
            }

            // 获取紫外线信息
            UVIndexInfo uvInfo = getUVIndexInfo(location);
            if (uvInfo == null) {
                log.warn("获取紫外线信息失败，位置: {}", location);
                return false;
            }

            // 获取配置的阈值 (默认UV 3需要防晒)
            int uvThreshold = ((Number) config.getOrDefault("uvThreshold", 3)).intValue();

            boolean shouldRemind = uvInfo.getUvIndex() != null && uvInfo.getUvIndex() >= uvThreshold;

            log.info("紫外线提醒检查 - 位置: {}, 紫外线指数: {}, 阈值: {}, 触发: {}",
                location, uvInfo.getUvIndex(), uvThreshold, shouldRemind);

            return shouldRemind;

        } catch (Exception e) {
            log.error("检查紫外线提醒失败: {}", reminder.getId(), e);
            return false;
        }
    }

    /**
     * 获取用户定位信息
     */
    private String getUserLocation(Long userId) {
        try {
            return sceneCacheService.getUserLocation(userId);
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
            template = "☀️ 紫外线较强，注意防晒！";
        }

        String location = (String) config.getOrDefault("location", "auto");
        if ("auto".equals(location)) {
            String userLocation = getUserLocation(reminder.getCreateBy());
            location = userLocation != null ? userLocation : "南京";
        }

        UVIndexInfo uvInfo = getUVIndexInfo(location);

        String uvLevel = "弱";
        if (uvInfo != null && uvInfo.getUvIndex() != null) {
            uvLevel = getUVLevel(uvInfo.getUvIndex());
        }

        return template
            .replace("{userName}", user.getNickname() != null ? user.getNickname() : "亲爱的")
            .replace("{location}", location)
            .replace("{uvLevel}", uvLevel);
    }

    @Override
    public String renderContent(Reminder reminder, User user) {
        Map<String, Object> config = JSONUtil.parseObj(reminder.getBusinessData());
        String template = reminder.getContentTemplate();

        if (template == null || template.isEmpty()) {
            template = "{userName}，当前位置{location}当前紫外线指数{uvIndex}，{uvLevel}，建议涂抹防晒霜，佩戴遮阳帽或太阳镜🕶️";
        }

        String location = (String) config.getOrDefault("location", "auto");
        if ("auto".equals(location)) {
            String userLocation = getUserLocation(reminder.getCreateBy());
            location = userLocation != null ? userLocation : "南京";
        }

        UVIndexInfo uvInfo = getUVIndexInfo(location);

        String uvIndexText = "--";
        String uvLevel = "弱";

        if (uvInfo != null && uvInfo.getUvIndex() != null) {
            uvIndexText = String.valueOf(uvInfo.getUvIndex());
            uvLevel = getUVLevel(uvInfo.getUvIndex());
        }

        return template
            .replace("{userName}", user.getNickname() != null ? user.getNickname() : "亲爱的")
            .replace("{location}", location)
            .replace("{uvIndex}", uvIndexText)
            .replace("{uvLevel}", uvLevel);
    }

    @Override
    public SceneTemplate getDefaultTemplate() {
        return SceneTemplate.builder()
            .sceneType(SCENE_TYPE)
            .reminderName("☀️ 紫外线提醒")
            .reminderType("UV_INDEX")
            .frequencyType("INTERVAL")
            .frequencyConfig("{\"intervalMinutes\": 120}")
            .titleTemplate("☀️ 紫外线较强，注意防晒！")
            .contentTemplate("{userName}，当前位置{location}当前紫外线指数{uvIndex}，{uvLevel}，建议涂抹防晒霜，佩戴遮阳帽或太阳镜🕶️")
            .businessData("{\"sceneType\": \"UV_INDEX\", \"location\": \"auto\", \"intervalMinutes\": 120, \"uvThreshold\": 3, \"allDay\": false, \"workHours\": [\"08:00\", \"18:00\"]}")
            .icon(ICON)
            .description("在白天时段监测紫外线，偏强时提醒防晒")
            .bgColor(BG_COLOR)
            .build();
    }

    /**
     * 获取紫外线指数信息
     */
    private UVIndexInfo getUVIndexInfo(String location) {
        try {
            // 1. 获取坐标
            double[] coords = getCoordinates(location);
            if (coords == null) {
                log.warn("无法获取城市坐标: {}", location);
                return getDefaultUVInfo(location);
            }

            // 2. 调用天气API获取紫外线指数
            // 获取今天和明天的数据
            String url = String.format(
                "%s?latitude=%.4f&longitude=%.4f&daily=uv_index_max,uv_index_clear_sky_max&timezone=auto",
                WEATHER_API, coords[0], coords[1]
            );

            log.debug("请求紫外线API: {}", url);

            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response == null) {
                return getDefaultUVInfo(location);
            }

            Map<String, Object> daily = (Map<String, Object>) response.get("daily");
            if (daily == null) {
                return getDefaultUVInfo(location);
            }

            // 获取今天的紫外线指数
            @SuppressWarnings("unchecked")
            java.util.List<Number> uvIndexList = (java.util.List<Number>) daily.get("uv_index_max");

            UVIndexInfo info = new UVIndexInfo();
            info.setLocation(location);

            if (uvIndexList != null && !uvIndexList.isEmpty()) {
                // 今天的UV指数
                Number todayUV = uvIndexList.get(0);
                info.setUvIndex(todayUV != null ? todayUV.doubleValue() : null);

                // 明天的UV指数（用于预告）
                if (uvIndexList.size() > 1) {
                    Number tomorrowUV = uvIndexList.get(1);
                    info.setTomorrowUvIndex(tomorrowUV != null ? tomorrowUV.doubleValue() : null);
                }
            }

            log.info("获取{}紫外线指数: 今天={}, 明天={}",
                location, info.getUvIndex(), info.getTomorrowUvIndex());

            return info;

        } catch (Exception e) {
            log.error("获取紫外线信息失败: {}", location, e);
            return getDefaultUVInfo(location);
        }
    }

    /**
     * 获取城市坐标
     */
    private double[] getCoordinates(String cityName) {
        try {
            if (cityName.matches(".*\\d+.*")) {
                String[] parts = cityName.replaceAll("[^0-9.,-]", "").split(",");
                if (parts.length >= 2) {
                    try {
                        double lat = Double.parseDouble(parts[0].trim());
                        double lon = Double.parseDouble(parts[1].trim());
                        return new double[]{lat, lon};
                    } catch (NumberFormatException ignored) {}
                }
            }

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
            Object latObj = firstResult.get("latitude");
            Object lonObj = firstResult.get("longitude");

            if (latObj != null && lonObj != null) {
                return new double[]{
                    ((Number) latObj).doubleValue(),
                    ((Number) lonObj).doubleValue()
                };
            }

            return null;

        } catch (Exception e) {
            log.error("获取城市坐标失败: {}", cityName, e);
            return null;
        }
    }

    /**
     * 默认紫外线信息
     */
    private UVIndexInfo getDefaultUVInfo(String location) {
        UVIndexInfo info = new UVIndexInfo();
        info.setLocation(location);
        info.setUvIndex(2.0); // 默认低风险
        return info;
    }

    /**
     * 根据UV指数获取等级
     */
    private String getUVLevel(double uvIndex) {
        if (uvIndex < 3) return "弱（无需防护）";
        if (uvIndex < 5) return "中等（建议涂抹防晒霜）";
        if (uvIndex < 7) return "较强（需积极防护）";
        if (uvIndex < 10) return "很强（尽量避免外出）";
        return "极强（禁止外出）";
    }

    /**
     * 标记已提醒（使用时间戳，支持间隔提醒）
     */
    public void markReminded(Long reminderId, Long userId) {
        sceneCacheService.markNotified(reminderId, userId, getSceneType());
    }

    /**
     * 紫外线信息内部类
     */
    @lombok.Data
    public static class UVIndexInfo {
        private String location;
        private Double uvIndex;
        private Double tomorrowUvIndex;
    }
}
