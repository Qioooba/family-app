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
 * 温度提醒处理器（高温/低温提醒）
 */
@Slf4j
@Component
public class WeatherTempHandler implements SceneReminderHandler {
    
    @Autowired
    private StringRedisTemplate redisTemplate;
    
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
                int threshold = (int) config.get("tempThreshold");
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
            Map<String, Object> config = JSONUtil.parseObj(reminder.getBusinessData());
            
            // 检查今日是否已提醒
            String today = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
            String cacheKey = String.format("scene:temp:%d:%s", reminder.getId(), today);
            
            Boolean alreadyReminded = redisTemplate.hasKey(cacheKey);
            if (Boolean.TRUE.equals(alreadyReminded)) {
                log.debug("今日已提醒过，跳过温度提醒");
                return false;
            }
            
            // 获取温度信息（简化实现）
            String location = (String) config.getOrDefault("location", "auto");
            int currentTemp = getCurrentTemp(location);
            
            int threshold = (int) config.getOrDefault("tempThreshold", 35);
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
    
    @Override
    public String renderTitle(Reminder reminder, User user) {
        Map<String, Object> config = JSONUtil.parseObj(reminder.getBusinessData());
        String template = reminder.getTitleTemplate();
        
        if (template == null || template.isEmpty()) {
            String alertType = (String) config.getOrDefault("alertType", "high");
            if ("high".equals(alertType)) {
                template = "🌡️ 高温预警，注意防暑！";
            } else if ("low".equals(alertType)) {
                template = "❄️ 低温预警，注意保暖！";
            } else {
                template = "🌡️ 温度异常提醒";
            }
        }
        
        String location = (String) config.getOrDefault("location", "auto");
        int currentTemp = getCurrentTemp(location);
        
        return template
            .replace("{userName}", user.getNickname() != null ? user.getNickname() : "亲爱的")
            .replace("{currentTemp}", String.valueOf(currentTemp));
    }
    
    @Override
    public String renderContent(Reminder reminder, User user) {
        Map<String, Object> config = JSONUtil.parseObj(reminder.getBusinessData());
        String template = reminder.getContentTemplate();
        
        if (template == null || template.isEmpty()) {
            template = "{userName}，当前气温{currentTemp}°C，{tempAdvice}\n\n{healthTips}";
        }
        
        String location = (String) config.getOrDefault("location", "auto");
        int currentTemp = getCurrentTemp(location);
        String alertType = (String) config.getOrDefault("alertType", "high");
        
        String tempAdvice;
        String healthTips;
        
        if ("high".equals(alertType) || (currentTemp >= 35)) {
            tempAdvice = "天气较热，请注意防暑降温！";
            healthTips = "💡 防暑小贴士：\n" +
                "1. 多喝水，及时补充流失的水分\n" +
                "2. 避免在中午12点至下午3点外出\n" +
                "3. 穿着宽松透气的衣物\n" +
                "4. 室内保持通风，适当使用空调";
        } else {
            tempAdvice = "天气较冷，请注意保暖！";
            healthTips = "💡 保暖小贴士：\n" +
                "1. 及时增添衣物，注意防寒\n" +
                "2. 多喝热水，保持身体温暖\n" +
                "3. 注意手脚保暖，避免冻伤\n" +
                "4. 适当运动，促进血液循环";
        }
        
        return template
            .replace("{userName}", user.getNickname() != null ? user.getNickname() : "亲爱的")
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
            .contentTemplate("{userName}，当前气温{currentTemp}°C，{tempAdvice}\n\n{healthTips}")
            .businessData("{\"sceneType\": \"WEATHER_TEMP\", \"location\": \"auto\", \"tempThreshold\": 35, \"alertType\": \"high\"}")
            .icon(ICON)
            .description("温度超过35°C时自动提醒防暑")
            .bgColor(BG_COLOR)
            .build();
    }
    
    /**
     * 获取当前温度（简化实现）
     */
    private int getCurrentTemp(String location) {
        // 实际项目中应该调用天气API
        // 这里返回模拟数据
        return 36; // 模拟高温
    }
    
    /**
     * 标记今日已提醒
     */
    public void markReminded(Long reminderId) {
        String today = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        String cacheKey = String.format("scene:temp:%d:%s", reminderId, today);
        redisTemplate.opsForValue().set(cacheKey, "1", 24, TimeUnit.HOURS);
    }
}
