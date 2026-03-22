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
import java.time.DayOfWeek;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * 每日签到提醒处理器
 */
@Slf4j
@Component
public class CheckInHandler implements SceneReminderHandler {

    @Autowired
    private SceneCacheService sceneCacheService;

    private static final String SCENE_TYPE = "CHECKIN";
    private static final String ICON = "✅";
    private static final String BG_COLOR = "linear-gradient(135deg, #11998e 0%, #38ef7d 100%)";

    @Override
    public String getSceneType() {
        return SCENE_TYPE;
    }

    @Override
    public String getSceneName() {
        return "每日签到";
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
            Map<String, Object> config = JSONUtil.parseObj(reminder.getBusinessData());
            if (isWorkdayOnly(config) && !isWorkday(LocalDate.now())) {
                return false;
            }

            String reminderTime = (String) config.getOrDefault("reminderTime", "08:00");

            boolean alreadyReminded = sceneCacheService.hasRemindedForScheduledTimeToday(reminder.getId(), reminderTime, 30);
            if (Boolean.TRUE.equals(alreadyReminded)) {
                log.debug("今日已在该时段提醒过，跳过: {}", reminder.getReminderName());
                return false;
            }

            // 检查当前时间
            LocalDateTime now = LocalDateTime.now();
            String currentTime = String.format("%02d:%02d", now.getHour(), now.getMinute());

            int timeDiff = compareTime(currentTime, reminderTime);
            // 允许30分钟误差（用户可能刚醒来）
            if (timeDiff < -30 || timeDiff > 30) {
                return false;
            }

            log.info("签到提醒触发: reminderTime={}, currentTime={}", reminderTime, currentTime);
            return true;

        } catch (Exception e) {
            log.error("检查签到提醒失败: {}", reminder.getId(), e);
            return false;
        }
    }

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
            template = "✅ 每日签到提醒";
        }
        return template.replace("{userName}", user.getNickname() != null ? user.getNickname() : "亲爱的");
    }

    @Override
    public String renderContent(Reminder reminder, User user) {
        Map<String, Object> config = JSONUtil.parseObj(reminder.getBusinessData());
        String template = reminder.getContentTemplate();

        if (template == null || template.isEmpty()) {
            template = "{userName}，新的一天开始啦！\n\n" +
                "📝 今日任务：\n" +
                "• 记得签到打卡哦\n" +
                "• 完成今日待办\n" +
                "• 保持好心情\n\n" +
                "💪 加油！";
        }

        return template.replace("{userName}", user.getNickname() != null ? user.getNickname() : "亲爱的");
    }

    @Override
    public SceneTemplate getDefaultTemplate() {
        return SceneTemplate.builder()
            .sceneType(SCENE_TYPE)
            .reminderName("✅ 每日签到提醒")
            .reminderType("CHECKIN")
            .frequencyType("DAILY")
            .frequencyConfig("{\"fixedTime\": \"08:00\"}")
            .titleTemplate("✅ 每日签到提醒")
            .contentTemplate("{userName}，新的一天开始啦！\n\n📝 今日任务：\n• 记得签到打卡哦\n• 完成今日待办\n• 保持好心情\n\n💪 加油！")
            .businessData("{\"sceneType\": \"CHECKIN\", \"reminderTime\": \"08:00\", \"workDaysOnly\": true}")
            .icon(ICON)
            .description("工作日上午8点提醒签到打卡")
            .bgColor(BG_COLOR)
            .build();
    }

    public void markReminded(Long reminderId, Long userId) {
        sceneCacheService.markRemindedToday(reminderId, userId, getSceneType());
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
