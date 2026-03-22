package com.family.family.service.scene;

import cn.hutool.json.JSONUtil;
import com.family.common.config.AppProperties;
import com.family.family.entity.Reminder;
import com.family.family.entity.User;
import com.family.family.entity.UserLocation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import com.family.family.service.SceneCacheService;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 每日早安提醒处理器
 * 包含天气摘要、每日心语与简讯。
 */
@Slf4j
@Component
public class MorningHandler implements SceneReminderHandler {

    @Autowired
    private SceneCacheService sceneCacheService;

    @Autowired
    private AppProperties appProperties;

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

    private final ConcurrentHashMap<LocalDate, String> dailyQuoteCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<LocalDate, String> dailyBriefCache = new ConcurrentHashMap<>();

    @Override
    public String getSceneType() {
        return SCENE_TYPE;
    }

    @Override
    public String getSceneName() {
        return "每日早安";
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
            String reminderTime = String.valueOf(config.getOrDefault("reminderTime", "07:00"));
            LocalTime.parse(reminderTime);
        } catch (Exception e) {
            throw new IllegalArgumentException("早安提醒配置格式错误: " + e.getMessage());
        }
    }

    @Override
    public boolean shouldTrigger(Reminder reminder) {
        try {
            // 检查今日是否已提醒
            String today = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
            String cacheKey = String.format("scene:morning:%d:%s", reminder.getId(), today);

            boolean alreadyReminded = sceneCacheService.hasRemindedToday(reminder.getId());
            if (Boolean.TRUE.equals(alreadyReminded)) {
                log.debug("今日已提醒过，跳过: {}", reminder.getReminderName());
                return false;
            }

            // 获取配置的提醒时间
            Map<String, Object> config = JSONUtil.parseObj(reminder.getBusinessData());
            String reminderTime = (String) config.getOrDefault("reminderTime", "07:00");
            // 检查当前时间是否到达提醒时间
            LocalDateTime now = LocalDateTime.now();
            String currentTime = String.format("%02d:%02d", now.getHour(), now.getMinute());

            // 允许5分钟误差
            int timeDiff = compareTime(currentTime, reminderTime);
            if (timeDiff < -5 || timeDiff > 5) {
                return false;
            }

            log.info("早安提醒触发: reminderTime={}, currentTime={}", reminderTime, currentTime);
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
        String template = reminder.getTitleTemplate();
        if (template == null || template.isEmpty()) {
            template = "🌅 早安";
        }

        return template.replace("{userName}", user.getNickname() != null ? user.getNickname() : "亲爱的");
    }

    @Override
    public String renderContent(Reminder reminder, User user) {
        Map<String, Object> config = JSONUtil.parseObj(reminder.getBusinessData());

        UserLocation resolvedLocation = resolveLocation(
            reminder.getCreateBy(), (String) config.getOrDefault("location", "auto"));
        String location = resolvedLocation.getLocation();

        // 获取天气信息
        String weatherInfo = getWeatherSummary(
            location, resolvedLocation.getLatitude(), resolvedLocation.getLongitude());

        // 获取日期信息
        LocalDate today = LocalDate.now();
        String dateInfo = today.format(DateTimeFormatter.ofPattern("MM月dd日 EEEE", java.util.Locale.CHINA));

        String quote = getQuote();
        String dailyBrief = getDailyBrief();

        // 构建内容
        StringBuilder content = new StringBuilder();

        content.append("📅 ").append(dateInfo).append("\n");
        if (!weatherInfo.isEmpty()) {
            content.append(weatherInfo).append("\n");
        }
        if (!quote.isEmpty()) {
            content.append("💬 今日心语：").append(quote).append("\n");
        }
        if (!dailyBrief.isEmpty()) {
            content.append("📰 今日简讯：").append(dailyBrief).append("\n");
        }

        return content.toString().trim();
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

    private UserLocation resolveLocation(Long userId, String configuredLocation) {
        UserLocation resolved = new UserLocation();
        if (!"auto".equals(configuredLocation)) {
            resolved.setLocation(configuredLocation);
            return resolved;
        }
        try {
            UserLocation record = sceneCacheService.getUserLocationRecord(userId);
            if (record != null) {
                resolved.setLocation(record.getLocation());
                resolved.setLatitude(record.getLatitude());
                resolved.setLongitude(record.getLongitude());
            }
        } catch (Exception ignored) {
        }
        if (resolved.getLocation() == null || resolved.getLocation().isBlank()) {
            resolved.setLocation("南京");
        }
        return resolved;
    }

    /**
     * 获取天气摘要
     */
    private String getWeatherSummary(String location, Double latitude, Double longitude) {
        try {
            double[] coords = getCoordinates(location, latitude, longitude);
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
    private double[] getCoordinates(String cityName, Double latitude, Double longitude) {
        try {
            return OpenMeteoGeocodingSupport.resolveCoordinates(
                restTemplate, GEOCODING_API, cityName, latitude, longitude);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取语录
     */
    private String getQuote() {
        LocalDate today = LocalDate.now();
        return dailyQuoteCache.computeIfAbsent(today, ignored -> {
            String onlineQuote = fetchOnlineQuote();
            if (!onlineQuote.isEmpty()) {
                return onlineQuote;
            }
            return QUOTES[Math.floorMod(today.getDayOfYear(), QUOTES.length)];
        });
    }

    private String fetchOnlineQuote() {
        try {
            Map<String, Object> response = restTemplate.getForObject(
                "https://v1.hitokoto.cn/?c=d&c=h&c=i&encode=json&max_length=28",
                Map.class
            );
            if (response == null) {
                return "";
            }
            Object hitokoto = response.get("hitokoto");
            return hitokoto == null ? "" : clampText(String.valueOf(hitokoto), 32);
        } catch (Exception e) {
            log.debug("获取每日心语失败，回退本地文案", e);
            return "";
        }
    }

    private String getDailyBrief() {
        LocalDate today = LocalDate.now();
        return dailyBriefCache.computeIfAbsent(today, ignored -> fetchDailyBrief());
    }

    private String fetchDailyBrief() {
        String feedUrl = appProperties.getDailyBriefFeedUrl();
        if (feedUrl == null || feedUrl.isBlank()) {
            return "";
        }
        try {
            String xml = restTemplate.getForObject(feedUrl, String.class);
            if (xml == null || xml.isBlank()) {
                return "";
            }

            Document document = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
            document.getDocumentElement().normalize();

            NodeList itemNodes = document.getElementsByTagName("item");
            if (itemNodes.getLength() == 0) {
                return "";
            }
            NodeList titleNodes = document.getElementsByTagName("title");
            for (int i = 0; i < titleNodes.getLength(); i++) {
                String title = titleNodes.item(i).getTextContent();
                if (title != null && !title.isBlank() && !title.contains("知乎日报")) {
                    return clampText(title.replaceAll("\\s+", " ").trim(), 34);
                }
            }
        } catch (Exception e) {
            log.debug("获取今日简讯失败，自动省略", e);
        }
        return "";
    }

    private String clampText(String text, int maxLength) {
        if (text == null) {
            return "";
        }
        String normalized = text.replaceAll("\\s+", " ").trim();
        if (normalized.length() <= maxLength) {
            return normalized;
        }
        return normalized.substring(0, Math.max(0, maxLength - 1)) + "…";
    }

    @Override
    public SceneTemplate getDefaultTemplate() {
        return SceneTemplate.builder()
            .sceneType(SCENE_TYPE)
            .reminderName("🌅 每日早安")
            .reminderType("MORNING")
            .frequencyType("DAILY")
            .frequencyConfig("{\"fixedTime\": \"07:00\"}")
            .titleTemplate("🌅 早安")
            .contentTemplate("")
            .businessData("{\"sceneType\": \"MORNING\", \"location\": \"auto\", \"reminderTime\": \"07:00\"}")
            .icon(ICON)
            .description("每天早上发送早安提醒，包含天气、每日心语和一条简讯")
            .bgColor(BG_COLOR)
            .build();
    }

    /**
     * 标记已提醒
     */
    public void markReminded(Long reminderId, Long userId) {
        sceneCacheService.markRemindedToday(reminderId, userId, getSceneType());
    }
}
