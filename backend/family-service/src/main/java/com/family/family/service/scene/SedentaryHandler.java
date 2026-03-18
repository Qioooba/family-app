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
 * 久坐提醒处理器
 */
@Slf4j
@Component
public class SedentaryHandler implements SceneReminderHandler {
    
    @Autowired
    private StringRedisTemplate redisTemplate;
    
    private static final String SCENE_TYPE = "SEDENTARY";
    private static final String ICON = "🪑";
    private static final String BG_COLOR = "linear-gradient(135deg, #fa709a 0%, #fee140 100%)";
    
    @Override
    public String getSceneType() {
        return SCENE_TYPE;
    }
    
    @Override
    public String getSceneName() {
        return "久坐提醒";
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
            
            // 验证久坐时间
            if (config.containsKey("sitDuration")) {
                int duration = (int) config.get("sitDuration");
                if (duration < 30 || duration > 180) {
                    throw new IllegalArgumentException("久坐时间应在30-180分钟之间");
                }
            }
            
            // 验证工作时间段
            if (config.containsKey("workHours")) {
                List<String> workHours = (List<String>) config.get("workHours");
                if (workHours.size() != 2) {
                    throw new IllegalArgumentException("工作时间段需要开始和结束时间");
                }
            }
            
        } catch (Exception e) {
            throw new IllegalArgumentException("久坐提醒配置格式错误: " + e.getMessage());
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
                    log.debug("不在工作时间段内，跳过久坐提醒");
                    return false;
                }
            }
            
            // 获取久坐时长配置
            int sitDuration = (int) config.getOrDefault("sitDuration", 60);
            
            // 检查上次提醒时间
            String today = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
            String cacheKey = String.format("scene:sedentary:%d:%s", reminder.getId(), today);
            String lastRemindTime = redisTemplate.opsForValue().get(cacheKey);
            
            if (lastRemindTime != null) {
                LocalDateTime lastTime = LocalDateTime.parse(lastRemindTime);
                LocalDateTime nextRemindTime = lastTime.plusMinutes(sitDuration);
                
                if (LocalDateTime.now().isBefore(nextRemindTime)) {
                    log.debug("久坐提醒冷却中，下次提醒时间: {}", nextRemindTime);
                    return false;
                }
            }
            
            return true;
            
        } catch (Exception e) {
            log.error("检查久坐提醒失败: {}", reminder.getId(), e);
            return false;
        }
    }
    
    @Override
    public String renderTitle(Reminder reminder, User user) {
        Map<String, Object> config = JSONUtil.parseObj(reminder.getBusinessData());
        String template = reminder.getTitleTemplate();
        
        if (template == null || template.isEmpty()) {
            template = "🪑 久坐提醒，起来活动一下吧！";
        }
        
        int sitDuration = (int) config.getOrDefault("sitDuration", 60);
        
        return template
            .replace("{userName}", user.getNickname() != null ? user.getNickname() : "亲爱的")
            .replace("{sitDuration}", String.valueOf(sitDuration));
    }
    
    @Override
    public String renderContent(Reminder reminder, User user) {
        Map<String, Object> config = JSONUtil.parseObj(reminder.getBusinessData());
        String template = reminder.getContentTemplate();
        
        if (template == null || template.isEmpty()) {
            template = "{userName}，您已经坐了{sitDuration}分钟了，建议起身活动{breakDuration}分钟，伸伸懒腰、走走楼梯，保护腰椎健康！{stretchTips}";
        }
        
        int sitDuration = (int) config.getOrDefault("sitDuration", 60);
        int breakDuration = (int) config.getOrDefault("breakDuration", 5);
        boolean postureTips = (boolean) config.getOrDefault("postureTips", true);
        
        String stretchTips = postureTips ? 
            "\n\n💡 小贴士：\n1. 转动脖子，放松肩颈\n2. 伸展手臂，活动手腕\n3. 站起来走动几分钟" : "";
        
        return template
            .replace("{userName}", user.getNickname() != null ? user.getNickname() : "亲爱的")
            .replace("{sitDuration}", String.valueOf(sitDuration))
            .replace("{breakDuration}", String.valueOf(breakDuration))
            .replace("{stretchTips}", stretchTips);
    }
    
    @Override
    public SceneTemplate getDefaultTemplate() {
        return SceneTemplate.builder()
            .sceneType(SCENE_TYPE)
            .reminderName("🪑 久坐提醒")
            .reminderType("SEDENTARY")
            .frequencyType("INTERVAL")
            .frequencyConfig("{\"intervalMinutes\": 60}")
            .titleTemplate("🪑 久坐提醒，起来活动一下吧！")
            .contentTemplate("{userName}，您已经坐了{sitDuration}分钟了，建议起身活动{breakDuration}分钟，伸伸懒腰、走走楼梯，保护腰椎健康！{stretchTips}")
            .businessData("{\"sceneType\": \"SEDENTARY\", \"sitDuration\": 60, \"breakDuration\": 5, \"workHours\": [\"09:00\", \"18:00\"], \"postureTips\": true, \"stretchGuide\": true}")
            .icon(ICON)
            .description("工作时间段内，久坐1小时提醒您起身活动")
            .bgColor(BG_COLOR)
            .build();
    }
    
    /**
     * 标记已提醒
     */
    public void markReminded(Long reminderId) {
        String today = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        String cacheKey = String.format("scene:sedentary:%d:%s", reminderId, today);
        redisTemplate.opsForValue().set(cacheKey, LocalDateTime.now().toString(), 24, TimeUnit.HOURS);
    }
}
