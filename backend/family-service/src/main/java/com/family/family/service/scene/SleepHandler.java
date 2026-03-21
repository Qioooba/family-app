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
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * 早睡提醒处理器
 */
@Slf4j
@Component
public class SleepHandler implements SceneReminderHandler {

    @Autowired
    private SceneCacheService sceneCacheService;

    private static final String SCENE_TYPE = "SLEEP";
    private static final String ICON = "🌙";
    private static final String BG_COLOR = "linear-gradient(135deg, #2c3e50 0%, #3498db 100%)";

    // 健康睡眠建议
    private static final String[] SLEEP_TIPS = {
        "🌙 熬夜伤身，记得早点休息哦",
        "💤 良好的睡眠是健康的基石",
        "😴 美容觉时间到了！",
        "🌟 早点睡，明天精神好",
        "💤 放下手机，让眼睛休息一下吧",
        "🌙 熬夜会变老哦，快去睡觉吧",
        "😪 身体是革命的本钱，休息很重要",
        "💤 睡个好觉，明天更美好"
    };

    @Override
    public String getSceneType() {
        return SCENE_TYPE;
    }

    @Override
    public String getSceneName() {
        return "早睡提醒";
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
            String today = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
            String cacheKey = String.format("scene:sleep:%d:%s", reminder.getId(), today);

            boolean alreadyReminded = sceneCacheService.hasRemindedToday(reminder.getId());
            if (Boolean.TRUE.equals(alreadyReminded)) {
                log.debug("今日已提醒过，跳过: {}", reminder.getReminderName());
                return false;
            }

            // 获取配置的提醒时间
            Map<String, Object> config = JSONUtil.parseObj(reminder.getBusinessData());
            String reminderTime = (String) config.getOrDefault("reminderTime", "22:00");

            LocalDateTime now = LocalDateTime.now();
            String currentTime = String.format("%02d:%02d", now.getHour(), now.getMinute());

            int timeDiff = compareTime(currentTime, reminderTime);
            // 允许5分钟误差
            if (timeDiff < -5 || timeDiff > 5) {
                return false;
            }

            log.info("早睡提醒触发: reminderTime={}, currentTime={}", reminderTime, currentTime);
            return true;

        } catch (Exception e) {
            log.error("检查早睡提醒失败: {}", reminder.getId(), e);
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
            template = "🌙 早睡提醒";
        }
        return template.replace("{userName}", user.getNickname() != null ? user.getNickname() : "亲爱的");
    }

    @Override
    public String renderContent(Reminder reminder, User user) {
        String template = reminder.getContentTemplate();

        if (template == null || template.isEmpty()) {
            // 获取随机睡眠建议
            int index = new java.util.Random().nextInt(SLEEP_TIPS.length);
            String sleepTip = SLEEP_TIPS[index];

            template = "{userName}，" + sleepTip + "\n\n" +
                "🌟 睡眠小知识：\n" +
                "• 成人每天需要7-9小时睡眠\n" +
                "• 睡前1小时不要看手机\n" +
                "• 保持卧室安静和黑暗\n\n" +
                "💤 晚安，好梦！";
        }

        return template.replace("{userName}", user.getNickname() != null ? user.getNickname() : "亲爱的");
    }

    @Override
    public SceneTemplate getDefaultTemplate() {
        return SceneTemplate.builder()
            .sceneType(SCENE_TYPE)
            .reminderName("🌙 早睡提醒")
            .reminderType("SLEEP")
            .frequencyType("DAILY")
            .frequencyConfig("{\"fixedTime\": \"22:00\"}")
            .titleTemplate("🌙 早睡提醒")
            .contentTemplate("")
            .businessData("{\"sceneType\": \"SLEEP\", \"reminderTime\": \"22:00\"}")
            .icon(ICON)
            .description("每天晚上10点提醒睡觉")
            .bgColor(BG_COLOR)
            .build();
    }

    public void markReminded(Long reminderId, Long userId) {
        sceneCacheService.markRemindedToday(reminderId, userId, getSceneType());
    }
}
