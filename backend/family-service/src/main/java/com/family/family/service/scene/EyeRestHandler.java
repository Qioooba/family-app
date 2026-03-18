package com.family.family.service.scene;

import cn.hutool.json.JSONUtil;
import com.family.family.entity.Reminder;
import com.family.family.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 护眼提醒处理器
 */
@Slf4j
@Component
public class EyeRestHandler implements SceneReminderHandler {
    
    @Autowired
    private StringRedisTemplate redisTemplate;
    
    private static final String SCENE_TYPE = "EYE_REST";
    private static final String ICON = "👁️";
    private static final String BG_COLOR = "linear-gradient(135deg, #30cfd0 0%, #330867 100%)";
    
    @Override
    public String getSceneType() {
        return SCENE_TYPE;
    }
    
    @Override
    public String getSceneName() {
        return "护眼提醒";
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
            
            // 验证用眼时间
            if (config.containsKey("screenTime")) {
                int screenTime = (int) config.get("screenTime");
                if (screenTime < 20 || screenTime > 120) {
                    throw new IllegalArgumentException("用眼时间应在20-120分钟之间");
                }
            }
            
            // 验证休息时间
            if (config.containsKey("restDuration")) {
                int restDuration = (int) config.get("restDuration");
                if (restDuration < 3 || restDuration > 30) {
                    throw new IllegalArgumentException("休息时间应在3-30分钟之间");
                }
            }
            
        } catch (Exception e) {
            throw new IllegalArgumentException("护眼提醒配置格式错误: " + e.getMessage());
        }
    }
    
    @Override
    public boolean shouldTrigger(Reminder reminder) {
        try {
            Map<String, Object> config = JSONUtil.parseObj(reminder.getBusinessData());
            
            // 检查是否在工作时间段内
            List<String> workHours = (List<String>) config.get("workHours");
            if (workHours != null && workHours.size() == 2) {
                LocalTime now = LocalTime.now();
                LocalTime start = LocalTime.parse(workHours.get(0));
                LocalTime end = LocalTime.parse(workHours.get(1));
                
                if (now.isBefore(start) || now.isAfter(end)) {
                    log.debug("不在工作时间段内，跳过护眼提醒");
                    return false;
                }
            }
            
            // 获取用眼时长配置
            int screenTime = (int) config.getOrDefault("screenTime", 45);
            
            // 检查上次提醒时间
            String today = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
            String cacheKey = String.format("scene:eye:%d:%s", reminder.getId(), today);
            String lastRemindTime = redisTemplate.opsForValue().get(cacheKey);
            
            if (lastRemindTime != null) {
                LocalDateTime lastTime = LocalDateTime.parse(lastRemindTime);
                LocalDateTime nextRemindTime = lastTime.plusMinutes(screenTime);
                
                if (LocalDateTime.now().isBefore(nextRemindTime)) {
                    log.debug("护眼提醒冷却中，下次提醒时间: {}", nextRemindTime);
                    return false;
                }
            }
            
            return true;
            
        } catch (Exception e) {
            log.error("检查护眼提醒失败: {}", reminder.getId(), e);
            return false;
        }
    }
    
    @Override
    public String renderTitle(Reminder reminder, User user) {
        Map<String, Object> config = JSONUtil.parseObj(reminder.getBusinessData());
        String template = reminder.getTitleTemplate();
        
        if (template == null || template.isEmpty()) {
            template = "👁️ 护眼时间到，让眼睛休息一下吧！";
        }
        
        int screenTime = (int) config.getOrDefault("screenTime", 45);
        
        return template
            .replace("{userName}", user.getNickname() != null ? user.getNickname() : "亲爱的")
            .replace("{screenTime}", String.valueOf(screenTime));
    }
    
    @Override
    public String renderContent(Reminder reminder, User user) {
        Map<String, Object> config = JSONUtil.parseObj(reminder.getBusinessData());
        String template = reminder.getContentTemplate();
        
        if (template == null || template.isEmpty()) {
            template = "{userName}，您已经连续用眼{screenTime}分钟了，建议休息{restDuration}分钟，保护视力健康！\n\n{eyeExercises}";
        }
        
        int screenTime = (int) config.getOrDefault("screenTime", 45);
        int restDuration = (int) config.getOrDefault("restDuration", 10);
        boolean eyeExercise = (boolean) config.getOrDefault("eyeExercise", true);
        boolean blinkReminder = (boolean) config.getOrDefault("blinkReminder", true);
        
        String eyeExercises = "";
        if (eyeExercise) {
            eyeExercises = "\n💡 护眼小贴士：\n" +
                "1. 20-20-20法则：每20分钟看20英尺(6米)外20秒\n" +
                "2. 眨眼练习：每分钟眨眼15-20次，缓解眼睛干涩\n" +
                "3. 闭目养神：闭上眼睛转动眼球，放松眼部肌肉\n" +
                "4. 热敷：用温水浸湿毛巾敷眼，促进血液循环";
        }
        
        if (blinkReminder && !eyeExercise) {
            eyeExercises = "\n💡 别忘了多眨眼，保持眼睛湿润！";
        }
        
        return template
            .replace("{userName}", user.getNickname() != null ? user.getNickname() : "亲爱的")
            .replace("{screenTime}", String.valueOf(screenTime))
            .replace("{restDuration}", String.valueOf(restDuration))
            .replace("{eyeExercises}", eyeExercises);
    }
    
    @Override
    public SceneTemplate getDefaultTemplate() {
        return SceneTemplate.builder()
            .sceneType(SCENE_TYPE)
            .reminderName("👁️ 护眼提醒")
            .reminderType("EYE_REST")
            .frequencyType("INTERVAL")
            .frequencyConfig("{\"intervalMinutes\": 45}")
            .titleTemplate("👁️ 护眼时间到，让眼睛休息一下吧！")
            .contentTemplate("{userName}，您已经连续用眼{screenTime}分钟了，建议休息{restDuration}分钟，保护视力健康！\n\n{eyeExercises}")
            .businessData("{\"sceneType\": \"EYE_REST\", \"screenTime\": 45, \"restDuration\": 10, \"workHours\": [\"09:00\", \"18:00\"], \"eyeExercise\": true, \"blinkReminder\": true}")
            .icon(ICON)
            .description("工作时间段内，用眼45分钟提醒休息，保护视力健康")
            .bgColor(BG_COLOR)
            .build();
    }
    
    /**
     * 标记已提醒
     */
    public void markReminded(Long reminderId) {
        String today = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        String cacheKey = String.format("scene:eye:%d:%s", reminderId, today);
        redisTemplate.opsForValue().set(cacheKey, LocalDateTime.now().toString(), 24, TimeUnit.HOURS);
    }
}
