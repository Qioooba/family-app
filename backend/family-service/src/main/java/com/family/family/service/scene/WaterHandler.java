package com.family.family.service.scene;

import cn.hutool.json.JSONUtil;
import com.family.family.entity.Reminder;
import com.family.family.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import com.family.family.service.SceneCacheService;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * 喝水提醒处理器
 */
@Slf4j
@Component
public class WaterHandler implements SceneReminderHandler {
    
    @Autowired
    private SceneCacheService sceneCacheService;
    
    private static final String SCENE_TYPE = "WATER";
    private static final String ICON = "💧";
    private static final String BG_COLOR = "linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)";
    
    @Override
    public String getSceneType() {
        return SCENE_TYPE;
    }
    
    @Override
    public String getSceneName() {
        return "喝水提醒";
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
            
            // 验证目标杯数
            if (config.containsKey("targetTimes")) {
                int target = (int) config.get("targetTimes");
                if (target < 4 || target > 16) {
                    throw new IllegalArgumentException("每日目标应在4-16杯之间");
                }
            }
            
            // 验证水杯容量
            if (config.containsKey("cupSize")) {
                int cupSize = (int) config.get("cupSize");
                if (cupSize < 100 || cupSize > 1000) {
                    throw new IllegalArgumentException("水杯容量应在100-1000ml之间");
                }
            }
            
        } catch (Exception e) {
            throw new IllegalArgumentException("喝水提醒配置格式错误: " + e.getMessage());
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
                    log.debug("不在工作时间段内，跳过喝水提醒");
                    return false;
                }
            }
            
            // 获取目标杯数和当前进度
            int targetTimes = (int) config.getOrDefault("targetTimes", 8);
            int currentCups = getTodayCups(reminder.getId());
            
            // 如果已完成目标，不再提醒
            if (currentCups >= targetTimes) {
                log.debug("今日喝水目标已达成: {}/{}", currentCups, targetTimes);
                return false;
            }
            
            return true;
            
        } catch (Exception e) {
            log.error("检查喝水提醒失败: {}", reminder.getId(), e);
            return false;
        }
    }
    
    @Override
    public String renderTitle(Reminder reminder, User user) {
        Map<String, Object> config = JSONUtil.parseObj(reminder.getBusinessData());
        String template = reminder.getTitleTemplate();
        
        if (template == null || template.isEmpty()) {
            template = "💧 该喝水了！已喝{cupCount}/{targetTimes}杯";
        }
        
        int targetTimes = (int) config.getOrDefault("targetTimes", 8);
        int currentCups = getTodayCups(reminder.getId());
        
        return template
            .replace("{userName}", user.getNickname() != null ? user.getNickname() : "亲爱的")
            .replace("{cupCount}", String.valueOf(currentCups))
            .replace("{targetTimes}", String.valueOf(targetTimes));
    }
    
    @Override
    public String renderContent(Reminder reminder, User user) {
        Map<String, Object> config = JSONUtil.parseObj(reminder.getBusinessData());
        String template = reminder.getContentTemplate();
        
        if (template == null || template.isEmpty()) {
            template = "{userName}，该喝水啦！💧\n\n今日进度：{cupCount}/{targetTimes}杯\n水杯容量：{cupSize}ml\n\n{hydrationTips}\n\n保持水分充足，身体更健康！";
        }
        
        int targetTimes = (int) config.getOrDefault("targetTimes", 8);
        int cupSize = (int) config.getOrDefault("cupSize", 200);
        int currentCups = getTodayCups(reminder.getId());
        
        // 根据进度给出不同提示
        String hydrationTips = getHydrationTips(currentCups, targetTimes);
        
        return template
            .replace("{userName}", user.getNickname() != null ? user.getNickname() : "亲爱的")
            .replace("{cupCount}", String.valueOf(currentCups))
            .replace("{targetTimes}", String.valueOf(targetTimes))
            .replace("{cupSize}", String.valueOf(cupSize))
            .replace("{hydrationTips}", hydrationTips);
    }
    
    @Override
    public SceneTemplate getDefaultTemplate() {
        return SceneTemplate.builder()
            .sceneType(SCENE_TYPE)
            .reminderName("💧 喝水提醒")
            .reminderType("WATER")
            .frequencyType("INTERVAL")
            .frequencyConfig("{\"intervalMinutes\": 60}")
            .titleTemplate("💧 该喝水了！已喝{cupCount}/{targetTimes}杯")
            .contentTemplate("{userName}，该喝水啦！💧\n\n今日进度：{cupCount}/{targetTimes}杯\n水杯容量：{cupSize}ml\n\n{hydrationTips}\n\n保持水分充足，身体更健康！")
            .businessData("{\"sceneType\": \"WATER\", \"targetTimes\": 8, \"cupSize\": 200, \"workHours\": [\"09:00\", \"18:00\"], \"smartInterval\": true, \"trackProgress\": true}")
            .icon(ICON)
            .description("工作时间段内，每小时提醒您喝水，追踪每日8杯水目标")
            .bgColor(BG_COLOR)
            .build();
    }
    
    /**
     * 获取今日已喝水杯数
     */
    private int getTodayCups(Long userId) {
        try {
            return sceneCacheService.getTodayCupCount(userId);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 记录喝水
     */
    public void recordWater(Long userId) {
        try {
            sceneCacheService.incrementCupCount(userId);
            log.info("记录喝水: userId={}", userId);
        } catch (Exception e) {
            log.error("记录喝水失败", e);
        }
    }
    
    /**
     * 获取补水提示
     */
    private String getHydrationTips(int current, int target) {
        double progress = (double) current / target;
        
        if (progress < 0.25) {
            return "💡 刚开始，加油！早晨第一杯水帮助唤醒身体新陈代谢。";
        } else if (progress < 0.5) {
            return "💡 进度不错！保持水分充足有助于提高注意力和工作效率。";
        } else if (progress < 0.75) {
            return "💡 已经完成一半以上了！适量饮水有助于皮肤保湿和排毒。";
        } else if (progress < 1.0) {
            return "💡 马上达成目标！再坚持一下，今日补水任务即将完成。";
        } else {
            return "🎉 恭喜！今日喝水目标已达成，保持良好的饮水习惯对身体很重要！";
        }
    }
}
