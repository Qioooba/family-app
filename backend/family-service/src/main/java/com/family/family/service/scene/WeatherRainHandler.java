package com.family.family.service.scene;

import cn.hutool.json.JSONUtil;
import com.family.family.entity.Reminder;
import com.family.family.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 下雨提醒处理器
 */
@Slf4j
@Component
public class WeatherRainHandler implements SceneReminderHandler {
    
    @Autowired
    private StringRedisTemplate redisTemplate;
    
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
            // 验证必要字段
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
            // 检查今日是否已提醒
            String today = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
            String cacheKey = String.format("scene:rain:%d:%s", reminder.getId(), today);
            
            Boolean alreadyReminded = redisTemplate.hasKey(cacheKey);
            if (Boolean.TRUE.equals(alreadyReminded)) {
                log.debug("今日已提醒过，跳过: {}", reminder.getReminderName());
                return false;
            }
            
            // 解析配置
            Map<String, Object> config = JSONUtil.parseObj(reminder.getBusinessData());
            String location = (String) config.getOrDefault("location", "auto");
            
            // 如果是auto，尝试获取用户定位信息
            if ("auto".equals(location)) {
                String userLocation = getUserLocation(reminder.getCreateBy());
                if (userLocation != null) {
                    location = userLocation;
                    log.info("使用用户定位位置: {}", location);
                } else {
                    // 默认使用南京
                    location = "南京";
                    log.info("用户无定位信息，使用默认城市: 南京");
                }
            }
            
            // 获取天气信息
            WeatherInfo weather = getWeatherInfo(location);
            if (weather == null) {
                log.warn("获取天气信息失败，位置: {}", location);
                return false;
            }
            
            // 判断是否有雨
            boolean hasRain = checkHasRain(weather);
            
            log.info("下雨提醒检查 - 位置: {}, 天气: {}, 温度: {}, 有雨: {}", 
                location, weather.getText(), weather.getTemperature(), hasRain);
            
            return hasRain;
            
        } catch (Exception e) {
            log.error("检查下雨提醒失败: {}", reminder.getId(), e);
            return false;
        }
    }
    
    /**
     * 获取用户定位信息
     * 从Redis或数据库中获取用户最近一次定位
     */
    private String getUserLocation(Long userId) {
        try {
            // 尝试从Redis获取用户定位
            String locationKey = String.format("user:location:%d", userId);
            String location = redisTemplate.opsForValue().get(locationKey);
            if (location != null && !location.isEmpty()) {
                return location;
            }
            
            // 如果Redis没有，返回null（使用默认值）
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
            template = "🌧️ 今天有雨，记得带伞！";
        }
        
        String location = (String) config.getOrDefault("location", "auto");
        
        // 处理自动定位
        if ("auto".equals(location)) {
            String userLocation = getUserLocation(reminder.getCreateBy());
            location = userLocation != null ? userLocation : "南京";
        }
        
        WeatherInfo weather = getWeatherInfo(location);
        
        return template
            .replace("{userName}", user.getNickname() != null ? user.getNickname() : "亲爱的")
            .replace("{location}", weather != null ? weather.getLocation() : location);
    }
    
    @Override
    public String renderContent(Reminder reminder, User user) {
        Map<String, Object> config = JSONUtil.parseObj(reminder.getBusinessData());
        String template = reminder.getContentTemplate();
        
        if (template == null || template.isEmpty()) {
            template = "{userName}，预计{rainStartTime}开始下雨，气温{temperature}，出门记得带{umbrellaIcon}哦！";
        }
        
        String location = (String) config.getOrDefault("location", "auto");
        
        // 处理自动定位
        if ("auto".equals(location)) {
            String userLocation = getUserLocation(reminder.getCreateBy());
            location = userLocation != null ? userLocation : "南京";
        }
        
        WeatherInfo weather = getWeatherInfo(location);
        
        if (weather == null) {
            return template.replace("{userName}", user.getNickname() != null ? user.getNickname() : "亲爱的");
        }
        
        // 计算雨开始时间（简化处理，实际可调用天气API获取）
        String rainStartTime = weather.getRainStartTime() != null ? 
            weather.getRainStartTime() : "今天";
        
        return template
            .replace("{userName}", user.getNickname() != null ? user.getNickname() : "亲爱的")
            .replace("{weather}", weather.getText())
            .replace("{temperature}", weather.getTemperature() + "°C")
            .replace("{rainStartTime}", rainStartTime)
            .replace("{umbrellaIcon}", "☔")
            .replace("{location}", weather.getLocation());
    }
    
    @Override
    public SceneTemplate getDefaultTemplate() {
        return SceneTemplate.builder()
            .sceneType(SCENE_TYPE)
            .reminderName("🌧️ 南京下雨提醒")
            .reminderType("WEATHER_RAIN")
            .frequencyType("DAILY")
            .frequencyConfig("{\"fixedTime\": \"07:00\"}")
            .titleTemplate("🌧️ 南京今天有雨，记得带伞！")
            .contentTemplate("{userName}，南京预计{rainStartTime}开始下雨，气温{temperature}，出门记得带{umbrellaIcon}哦！")
            .businessData("{\"sceneType\": \"WEATHER_RAIN\", \"location\": \"南京\", \"remindBeforeRain\": 30, \"remindWhenStop\": true, \"useUserLocation\": true}")
            .icon(ICON)
            .description("每天早上7点检查南京天气，有雨时自动提醒带伞（优先使用您的定位）")
            .bgColor(BG_COLOR)
            .build();
    }
    
    /**
     * 检查是否有雨
     */
    private boolean checkHasRain(WeatherInfo weather) {
        String text = weather.getText();
        if (text == null) return false;
        
        text = text.toLowerCase();
        return text.contains("雨") || text.contains("rain") || 
               text.contains("阵雨") || text.contains("雷雨") ||
               text.contains("小雨") || text.contains("中雨") || 
               text.contains("大雨") || text.contains("暴雨");
    }
    
    /**
     * 获取天气信息
     * 实际项目中应该调用 WeatherService 或天气API
     * 当前使用模拟数据，根据位置返回不同结果
     */
    private WeatherInfo getWeatherInfo(String location) {
        try {
            WeatherInfo info = new WeatherInfo();
            info.setLocation(location);
            
            // 根据城市模拟不同天气（实际应调用天气API）
            if (location.contains("南京")) {
                // 南京天气（模拟下雨）
                info.setText("小雨");
                info.setTemperature("18");
                info.setRainStartTime("09:00");
            } else if (location.contains("北京")) {
                info.setText("晴");
                info.setTemperature("25");
                info.setRainStartTime(null);
            } else if (location.contains("上海")) {
                info.setText("多云转小雨");
                info.setTemperature("20");
                info.setRainStartTime("14:00");
            } else {
                // 其他城市默认下雨（便于测试）
                info.setText("阵雨");
                info.setTemperature("22");
                info.setRainStartTime("今天晚些时候");
            }
            
            log.debug("获取{}天气: {}, 温度: {}", location, info.getText(), info.getTemperature());
            return info;
        } catch (Exception e) {
            log.error("获取天气信息失败: {}", location, e);
            return null;
        }
    }
    
    /**
     * 标记今日已提醒
     */
    public void markReminded(Long reminderId) {
        String today = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        String cacheKey = String.format("scene:rain:%d:%s", reminderId, today);
        redisTemplate.opsForValue().set(cacheKey, "1", 24, TimeUnit.HOURS);
    }
    
    /**
     * 天气信息内部类
     */
    @lombok.Data
    public static class WeatherInfo {
        private String location;
        private String text;
        private String temperature;
        private String rainStartTime;
    }
}
