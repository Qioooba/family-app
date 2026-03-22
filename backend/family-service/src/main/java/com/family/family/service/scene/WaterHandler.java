package com.family.family.service.scene;

import cn.hutool.json.JSONUtil;
import com.family.family.entity.Reminder;
import com.family.family.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import com.family.family.service.WaterRecordService;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.DayOfWeek;
import java.util.List;
import java.util.Map;

/**
 * 喝水提醒处理器
 */
@Slf4j
@Component
public class WaterHandler implements SceneReminderHandler {
    
    @Autowired
    private WaterRecordService waterRecordService;
    
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

            if (config.containsKey("intervalMinutes")) {
                int intervalMinutes = Integer.parseInt(String.valueOf(config.get("intervalMinutes")));
                if (intervalMinutes < 20 || intervalMinutes > 180) {
                    throw new IllegalArgumentException("提醒间隔应在20-180分钟之间");
                }
            }

            if (config.containsKey("workHours")) {
                List<String> workHours = (List<String>) config.get("workHours");
                if (workHours == null || workHours.size() != 2) {
                    throw new IllegalArgumentException("工作时间段需要开始和结束时间");
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

            if (isWorkdayOnly(config) && !isWorkday(LocalDate.now())) {
                log.debug("喝水提醒设置为仅工作日，今天非工作日");
                return false;
            }
            
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
            
            Map<String, Object> todayWaterInfo = waterRecordService.getTodayWaterInfo(reminder.getCreateBy());
            int todayAmount = getIntValue(todayWaterInfo.get("todayAmount"), 0);
            int targetAmount = getIntValue(todayWaterInfo.get("targetAmount"), 2000);
            
            // 如果已完成目标，不再提醒
            if (todayAmount >= targetAmount) {
                log.debug("今日饮水目标已达成: {}/{}ml", todayAmount, targetAmount);
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
        Map<String, Object> todayWaterInfo = waterRecordService.getTodayWaterInfo(user.getId());
        int todayAmount = getIntValue(todayWaterInfo.get("todayAmount"), 0);
        int targetAmount = getIntValue(todayWaterInfo.get("targetAmount"), 2000);
        int remainingAmount = Math.max(targetAmount - todayAmount, 0);

        if (todayAmount <= 0) {
            return "💧 该喝水了，今天还没开始补水";
        }

        if (remainingAmount <= 0) {
            return "💧 今日饮水目标已完成";
        }

        return String.format("💧 该喝水了，今日已喝 %d/%dml", todayAmount, targetAmount);
    }
    
    @Override
    public String renderContent(Reminder reminder, User user) {
        Map<String, Object> todayWaterInfo = waterRecordService.getTodayWaterInfo(user.getId());
        int todayAmount = getIntValue(todayWaterInfo.get("todayAmount"), 0);
        int targetAmount = getIntValue(todayWaterInfo.get("targetAmount"), 2000);
        int percent = getIntValue(todayWaterInfo.get("percent"), 0);
        int remainingAmount = Math.max(targetAmount - todayAmount, 0);
        String hydrationTips = getHydrationTips(todayAmount, targetAmount, remainingAmount);
        String userName = user.getNickname() != null ? user.getNickname() : "亲爱的";

        if (remainingAmount <= 0) {
            return String.format("%s，今天已经喝了 %dml，达到目标 %dml。\n\n%s", userName, todayAmount, targetAmount, hydrationTips);
        }

        return String.format(
            "%s，该补水了。当前已喝 %dml，目标 %dml，已完成 %d%%。\n\n距离今日目标还差 %dml。\n\n%s",
            userName, todayAmount, targetAmount, percent, remainingAmount, hydrationTips
        );
    }
    
    @Override
    public SceneTemplate getDefaultTemplate() {
        return SceneTemplate.builder()
            .sceneType(SCENE_TYPE)
            .reminderName("💧 喝水提醒")
            .reminderType("WATER")
            .frequencyType("INTERVAL")
            .frequencyConfig("{\"intervalMinutes\": 60}")
            .titleTemplate("")
            .contentTemplate("")
            .businessData("{\"sceneType\": \"WATER\", \"intervalMinutes\": 60, \"workHours\": [\"09:00\", \"18:00\"], \"workDaysOnly\": true}")
            .icon(ICON)
            .description("工作日工作时间内按间隔提醒喝水，进度自动读取喝水记录")
            .bgColor(BG_COLOR)
            .build();
    }
    
    /**
     * 获取补水提示
     */
    private String getHydrationTips(int currentAmount, int targetAmount, int remainingAmount) {
        double progress = targetAmount > 0 ? (double) currentAmount / targetAmount : 0;
        
        if (progress < 0.25) {
            return "💡 今天补水刚开始，先喝一杯让身体进入状态。";
        } else if (progress < 0.5) {
            return String.format("💡 进度不错，再补 %dml 就更接近今天的目标了。", remainingAmount);
        } else if (progress < 0.75) {
            return "💡 已经完成一半以上，继续保持，下午也别忘记补水。";
        } else if (progress < 1.0) {
            return String.format("💡 快达标了，今天只差 %dml。", remainingAmount);
        } else {
            return "🎉 今日饮水目标已完成，保持这个节奏就很好。";
        }
    }

    private int getIntValue(Object value, int defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private boolean isWorkdayOnly(Map<String, Object> config) {
        Object value = config.get("workDaysOnly");
        return Boolean.TRUE.equals(value) || "true".equals(String.valueOf(value));
    }

    private boolean isWorkday(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY;
    }
}
