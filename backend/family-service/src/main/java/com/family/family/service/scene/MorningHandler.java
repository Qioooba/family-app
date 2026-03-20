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
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 每日早安/晚安提醒处理器
 * 包含天气摘要、语录、新闻等
 */
@Slf4j
@Component
public class MorningHandler implements SceneReminderHandler {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private final RestTemplate restTemplate = new RestTemplate();

    // Open-Meteo 天气API
    private static final String WEATHER_API = "https://api.open-meteo.com/v1/forecast";
    private static final String GEOCODING_API = "https://geocoding-api.open-meteo.com/v1/search";

    private static final String SCENE_TYPE = "MORNING";
    private static final String ICON = "🌅";
    private static final String BG_COLOR = "linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%)";

    // 每日语录（可以后续接入API）
    private static final String[] QUOTES = {
        "早安！新的一天，新的希望🌞",
        "每一个清晨都是一个新的开始，加油！💪",
        "今天也要保持好心情哦！😊",
        "生活不易，但你可以更努力！🔥",
        "记得照顾好自己 ❤️",
        "新的一天，遇见更好的自己 ✨",
        "今天的你，比昨天更优秀 🚀",
        "保持微笑，好运自然来 🌈",
        "认真生活，每天都值得期待 🌸",
        "清晨第一缕阳光，照亮你的心房 ☀️"
    };

    // 晚安语录
    private static final String[] EVENING_QUOTES = {
        "晚安好梦 🌙",
        "辛苦了一天，好好休息 💤",
        "明天又是美好的一天 🏵️",
        "放下烦恼，轻松入睡 😴",
        "晚安，愿你有个好梦 🌟"
    };

    @Override
    public String getSceneType() {
        return SCENE_TYPE;
    }

    @Override
    public String getSceneName() {
        return "早安晚安";
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
        // 不需要特殊配置
    }

    @Override
    public boolean shouldTrigger(Reminder reminder) {
        try {
            // 检查今日是否已提醒
            String today = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
            String cacheKey = String.format("scene:morning:%d:%s", reminder.getId(), today);

            Boolean alreadyReminded = redisTemplate.hasKey(cacheKey);
            if (Boolean.TRUE.equals(alreadyReminded)) {
                log.debug("今日已提醒过，跳过: {}", reminder.getReminderName());
                return false;
            }

            // 获取配置的提醒时间
            Map<String, Object> config = JSONUtil.parseObj(reminder.getBusinessData());
            String reminderTime = (String) config.getOrDefault("reminderTime", "07:00");
            boolean isEvening = "evening".equals(config.getOrDefault("type", "morning"));

            // 检查当前时间是否到达提醒时间
            LocalDateTime now = LocalDateTime.now();
            String currentTime = String.format("%02d:%02d", now.getHour(), now.getMinute());

            // 允许5分钟误差
            int timeDiff = compareTime(currentTime, reminderTime);
            if (timeDiff < -5 || timeDiff > 5) {
                return false;
            }

            log.info("早安/晚安提醒触发: reminderTime={}, currentTime={}", reminderTime, currentTime);
            return true;

        } catch (Exception e) {
            log.error("检查早安晚安提醒失败: {}", reminder.getId(), e);
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

    @Override
    public String renderTitle(Reminder reminder, User user) {
        Map<String, Object> config = JSONUtil.parseObj(reminder.getBusinessData());
        boolean isEvening = "evening".equals(config.getOrDefault("type", "morning"));

        String template = reminder.getTitleTemplate();
        if (template == null || template.isEmpty()) {
            template = isEvening ? "🌙 晚安提醒" : "🌅 早安提醒";
        }

        return template.replace("{userName}", user.getNickname() != null ? user.getNickname() : "亲爱的");
    }

    @Override
    public String renderContent(Reminder reminder, User user) {
        Map<String, Object> config = JSONUtil.parseObj(reminder.getBusinessData());
        boolean isEvening = "evening".equals(config.getOrDefault("type", "morning"));

        String location = (String) config.getOrDefault("location", "auto");
        if ("auto".equals(location)) {
            String userLocation = getUserLocation(reminder.getCreateBy());
            location = userLocation != null ? userLocation : "南京";
        }

        // 获取天气信息
        String weatherInfo = getWeatherSummary(location);

        // 获取日期信息
        LocalDate today = LocalDate.now();
        String dateInfo = today.format(DateTimeFormatter.ofPattern("MM月dd日 EEEE", java.util.Locale.CHINA));

        // 获取语录
        String quote = getQuote(isEvening);

        // 构建内容
        StringBuilder content = new StringBuilder();

        if (isEvening) {
            // 晚安内容
            content.append(quote).append("\n\n");
            content.append("📅 ").append(dateInfo).append("\n");
            content.append("🌙 晚安，祝好梦！");
        } else {
            // 早安内容
            content.append(quote).append("\n\n");
            content.append("📅 ").append(dateInfo).append("\n\n");

            if (!weatherInfo.isEmpty()) {
                content.append(weatherInfo).append("\n");
            }

            // 今日提醒
            content.append("\n💡 今日提醒：\n");
            content.append("• 新的一天，记得签到哦\n");
            content.append("• 保持好心情\n");
        }

        return content.toString();
    }

    /**
     * 获取用户定位
     */
    private String getUserLocation(Long userId) {
        try {
            String locationKey = String.format("user:location:%d", userId);
            return redisTemplate.opsForValue().get(locationKey);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取天气摘要
     */
    private String getWeatherSummary(String location) {
        try {
            double[] coords = getCoordinates(location);
            if (coords == null) {
                return "";
            }

            String url = String.format(
                "%s?latitude=%.4f&longitude=%.4f&current=temperature_2m,weather_code&daily=temperature_2m_max,temperature_2m_min&timezone=auto",
                WEATHER_API, coords[0], coords[1]
            );

            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response == null) return "";

            Map<String, Object> current = (Map<String, Object>) response.get("current");
            Map<String, Object> daily = (Map<String, Object>) response.get("daily");

            if (current == null) return "";

            // 当前温度
            Object tempObj = current.get("temperature_2m");
            double temp = tempObj instanceof Number ? ((Number) tempObj).doubleValue() : 0;

            // 天气代码
            Object weatherCodeObj = current.get("weather_code");
            int weatherCode = weatherCodeObj instanceof Number ? ((Number) weatherCodeObj).intValue() : 0;
            String weatherDesc = getWeatherDesc(weatherCode);

            // 今日最高最低温
            String tempRange = "";
            if (daily != null) {
                @SuppressWarnings("unchecked")
                List<Number> maxTemps = (List<Number>) daily.get("temperature_2m_max");
                @SuppressWarnings("unchecked")
                List<Number> minTemps = (List<Number>) daily.get("temperature_2m_min");
                if (maxTemps != null && !maxTemps.isEmpty() && minTemps != null && !minTemps.isEmpty()) {
                    int max = maxTemps.get(0).intValue();
                    int min = minTemps.get(0).intValue();
                    tempRange = String.format("%d° / %d°", max, min);
                }
            }

            return String.format("🌤️ %s %s %s", location, weatherDesc, tempRange);

        } catch (Exception e) {
            log.warn("获取天气失败: {}", location);
            return "";
        }
    }

    /**
     * 获取天气描述
     */
    private String getWeatherDesc(int code) {
        if (code == 0) return "☀️ 晴";
        if (code <= 3) return "⛅ 多云";
        if (code <= 48) return "🌫️ 雾";
        if (code <= 67) return "🌧️ 雨";
        if (code <= 77) return "🌨️ 雪";
        if (code <= 82) return "🌧️ 阵雨";
        if (code <= 86) return "🌨️ 雪";
        if (code >= 95) return "⛈️ 雷暴";
        return "🌡️";
    }

    /**
     * 获取坐标
     */
    private double[] getCoordinates(String cityName) {
        try {
            String url = String.format("%s?name=%s&count=1&language=zh",
                GEOCODING_API, java.net.URLEncoder.encode(cityName, "UTF-8"));

            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response == null || !response.containsKey("results")) return null;

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("results");
            if (results == null || results.isEmpty()) return null;

            Map<String, Object> first = results.get(0);
            Object latObj = first.get("latitude");
            Object lonObj = first.get("longitude");

            if (latObj != null && lonObj != null) {
                return new double[]{
                    ((Number) latObj).doubleValue(),
                    ((Number) lonObj).doubleValue()
                };
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取语录
     */
    private String getQuote(boolean isEvening) {
        String[] quotes = isEvening ? EVENING_QUOTES : QUOTES;
        int index = new Random().nextInt(quotes.length);
        return quotes[index];
    }

    @Override
    public SceneTemplate getDefaultTemplate() {
        return SceneTemplate.builder()
            .sceneType(SCENE_TYPE)
            .reminderName("🌅 每日早安")
            .reminderType("MORNING")
            .frequencyType("DAILY")
            .frequencyConfig("{\"fixedTime\": \"07:00\"}")
            .titleTemplate("🌅 早安提醒")
            .contentTemplate("")
            .businessData("{\"sceneType\": \"MORNING\", \"type\": \"morning\", \"location\": \"auto\", \"reminderTime\": \"07:00\"}")
            .icon(ICON)
            .description("每天早上7点发送早安问候，包含天气和语录")
            .bgColor(BG_COLOR)
            .build();
    }

    /**
     * 标记已提醒
     */
    public void markReminded(Long reminderId) {
        String today = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        String cacheKey = String.format("scene:morning:%d:%s", reminderId, today);
        redisTemplate.opsForValue().set(cacheKey, "1", 24, TimeUnit.HOURS);
    }
}
