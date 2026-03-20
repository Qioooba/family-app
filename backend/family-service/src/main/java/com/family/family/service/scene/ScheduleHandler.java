package com.family.family.service.scene;

import cn.hutool.json.JSONUtil;
import com.family.family.entity.Reminder;
import com.family.family.entity.User;
import com.family.family.entity.Task;
import com.family.family.mapper.TaskMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.concurrent.TimeUnit;

/**
 * 今日日程提醒处理器
 * 查询当天的任务/日程进行提醒
 */
@Slf4j
@Component
public class ScheduleHandler implements SceneReminderHandler {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private TaskMapper taskMapper;

    private static final String SCENE_TYPE = "SCHEDULE";
    private static final String ICON = "📅";
    private static final String BG_COLOR = "linear-gradient(135deg, #667eea 0%, #764ba2 100%)";

    @Override
    public String getSceneType() {
        return SCENE_TYPE;
    }

    @Override
    public String getSceneName() {
        return "今日日程";
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
            String cacheKey = String.format("scene:schedule:%d:%s", reminder.getId(), today);

            Boolean alreadyReminded = redisTemplate.hasKey(cacheKey);
            if (Boolean.TRUE.equals(alreadyReminded)) {
                log.debug("今日已提醒过，跳过: {}", reminder.getReminderName());
                return false;
            }

            // 获取配置的提醒时间
            Map<String, Object> config = JSONUtil.parseObj(reminder.getBusinessData());
            String reminderTime = (String) config.getOrDefault("reminderTime", "07:30");

            LocalDateTime now = LocalDateTime.now();
            String currentTime = String.format("%02d:%02d", now.getHour(), now.getMinute());

            int timeDiff = compareTime(currentTime, reminderTime);
            if (timeDiff < -30 || timeDiff > 30) {
                return false;
            }

            // 检查是否有今日日程
            Long userId = reminder.getCreateBy();
            boolean hasSchedule = checkHasSchedule(userId);

            if (!hasSchedule) {
                log.debug("今日无日程，跳过提醒");
                return false;
            }

            log.info("今日日程提醒触发: reminderTime={}, currentTime={}", reminderTime, currentTime);
            return true;

        } catch (Exception e) {
            log.error("检查今日日程提醒失败: {}", reminder.getId(), e);
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

    /**
     * 检查是否有今日日程
     */
    private boolean checkHasSchedule(Long userId) {
        try {
            // 查询今日待办任务
            LocalDate today = LocalDate.now();
            LocalDateTime startOfDay = today.atStartOfDay();
            LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();

            List<Task> todayTasks = taskMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Task>()
                    .eq(Task::getAssigneeId, userId)
                    .eq(Task::getStatus, 0)
                    .eq(Task::getIsDeleted, 0)
                    .between(Task::getDueTime, startOfDay, endOfDay)
                    .or()
                    .eq(Task::getCreatorId, userId)
                    .eq(Task::getStatus, 0)
                    .eq(Task::getIsDeleted, 0)
                    .between(Task::getDueTime, startOfDay, endOfDay)
            );

            return todayTasks != null && !todayTasks.isEmpty();
        } catch (Exception e) {
            log.warn("查询今日日程失败", e);
            return false;
        }
    }

    @Override
    public String renderTitle(Reminder reminder, User user) {
        String template = reminder.getTitleTemplate();
        if (template == null || template.isEmpty()) {
            template = "📅 今日日程提醒";
        }
        return template.replace("{userName}", user.getNickname() != null ? user.getNickname() : "亲爱的");
    }

    @Override
    public String renderContent(Reminder reminder, User user) {
        Long userId = reminder.getCreateBy();
        List<String> todayTasks = getTodayTasks(userId);

        StringBuilder content = new StringBuilder();
        content.append(user.getNickname() != null ? user.getNickname() : "亲爱的").append("，今日日程如下：\n\n");

        if (todayTasks.isEmpty()) {
            content.append("📝 今日暂无任务安排\n\n");
        } else {
            content.append("📝 今日任务 (").append(todayTasks.size()).append("项)：\n");
            for (int i = 0; i < Math.min(todayTasks.size(), 5); i++) {
                content.append(i + 1).append(". ").append(todayTasks.get(i)).append("\n");
            }
            if (todayTasks.size() > 5) {
                content.append("... 还有").append(todayTasks.size() - 5).append("项\n");
            }
        }

        content.append("\n💪 加油完成今日任务！");

        return content.toString();
    }

    /**
     * 获取今日任务列表
     */
    private List<String> getTodayTasks(Long userId) {
        try {
            LocalDate today = LocalDate.now();
            LocalDateTime startOfDay = today.atStartOfDay();
            LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();

            List<Task> tasks = taskMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Task>()
                    .and(w -> w
                        .eq(Task::getAssigneeId, userId)
                        .or()
                        .eq(Task::getCreatorId, userId))
                    .eq(Task::getStatus, 0)
                    .eq(Task::getIsDeleted, 0)
                    .between(Task::getDueTime, startOfDay, endOfDay)
            );

            return tasks.stream()
                .map(Task::getTitle)
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("获取今日任务失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    public SceneTemplate getDefaultTemplate() {
        return SceneTemplate.builder()
            .sceneType(SCENE_TYPE)
            .reminderName("📅 今日日程提醒")
            .reminderType("SCHEDULE")
            .frequencyType("DAILY")
            .frequencyConfig("{\"fixedTime\": \"07:30\"}")
            .titleTemplate("📅 今日日程提醒")
            .contentTemplate("")
            .businessData("{\"sceneType\": \"SCHEDULE\", \"reminderTime\": \"07:30\"}")
            .icon(ICON)
            .description("每天早上7点半提醒今日待办任务")
            .bgColor(BG_COLOR)
            .build();
    }

    public void markReminded(Long reminderId) {
        String today = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        String cacheKey = String.format("scene:schedule:%d:%s", reminderId, today);
        redisTemplate.opsForValue().set(cacheKey, "1", 24, TimeUnit.HOURS);
    }
}
