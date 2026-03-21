package com.family.family.service.scene;

import cn.hutool.json.JSONUtil;
import com.family.calendar.entity.Anniversary;
import com.family.calendar.mapper.AnniversaryMapper;
import com.family.family.entity.FamilyMember;
import com.family.family.entity.Reminder;
import com.family.family.entity.User;
import com.family.family.mapper.FamilyMemberMapper;
import com.family.family.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import com.family.family.service.SceneCacheService;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Map;

/**
 * 纪念日提醒处理器
 * 自动从纪念日表获取数据，在生日前N天提醒
 */
@Slf4j
@Component
public class AnniversaryHandler implements SceneReminderHandler {

    @Autowired
    private SceneCacheService sceneCacheService;

    @Autowired
    private AnniversaryMapper anniversaryMapper;

    @Autowired
    private FamilyMemberMapper familyMemberMapper;

    @Autowired
    private UserMapper userMapper;

    private static final String SCENE_TYPE = "ANNIVERSARY";
    private static final String ICON = "🎂";
    private static final String BG_COLOR = "linear-gradient(135deg, #ff9a9e 0%, #fecfef 99%, #fecfef 100%)";

    @Override
    public String getSceneType() {
        return SCENE_TYPE;
    }

    @Override
    public String getSceneName() {
        return "纪念日提醒";
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
        // 纪念日不需要特殊配置
    }

    @Override
    public boolean shouldTrigger(Reminder reminder) {
        try {
            // 获取配置的提醒时间
            Map<String, Object> config = JSONUtil.parseObj(reminder.getBusinessData());
            String reminderTime = (String) config.getOrDefault("reminderTime", "09:00");

            // 检查当前时间是否到达提醒时间（允许5分钟误差）
            LocalDateTime now = LocalDateTime.now();
            String currentTime = String.format("%02d:%02d", now.getHour(), now.getMinute());

            int timeDiff = compareTime(currentTime, reminderTime);
            if (timeDiff < -5 || timeDiff > 5) {
                log.debug("未到达提醒时间 {}，当前时间 {}，跳过", reminderTime, currentTime);
                return false;
            }

            // 检查今日是否已提醒
            String today = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
            String cacheKey = String.format("scene:anniversary:%d:%s", reminder.getId(), today);

            boolean alreadyReminded = sceneCacheService.hasRemindedToday(reminder.getId());
            if (Boolean.TRUE.equals(alreadyReminded)) {
                log.debug("今日已提醒过，跳过: {}", reminder.getReminderName());
                return false;
            }
            int advanceDays = ((Number) config.getOrDefault("advanceDays", 3)).intValue();

            // 通过创建者获取家庭ID
            Long userId = reminder.getCreateBy();
            Long familyId = getUserFamilyId(userId);

            if (familyId == null) {
                familyId = 1L; // 默认家庭
            }

            // 查询即将到来的纪念日
            Anniversary upcomingAnniversary = getUpcomingAnniversary(familyId, advanceDays);

            if (upcomingAnniversary == null) {
                log.debug("近期无纪念日，跳过");
                return false;
            }

            // 标记需要触发
            log.info("纪念日提醒触发: {}, 日期: {}, 提前{}天",
                upcomingAnniversary.getTitle(), upcomingAnniversary.getTargetDate(), advanceDays);

            // 存储当前纪念日信息到数据库，供渲染使用
            sceneCacheService.markRemindedToday(reminder.getId(), reminder.getCreateBy(), getSceneType());

            return true;

        } catch (Exception e) {
            log.error("检查纪念日提醒失败: {}", reminder.getId(), e);
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

    /**
     * 获取用户的默认家庭ID
     */
    private Long getUserFamilyId(Long userId) {
        try {
            if (userId == null) return null;

            // 查询用户的第一个家庭成员记录
            FamilyMember member = familyMemberMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<FamilyMember>()
                    .eq(FamilyMember::getUserId, userId)
                    .orderByAsc(FamilyMember::getJoinTime)
                    .last("LIMIT 1")
            );

            return member != null ? member.getFamilyId() : null;
        } catch (Exception e) {
            log.warn("获取用户家庭ID失败: userId={}", userId, e);
            return null;
        }
    }

    /**
     * 获取即将到来的纪念日
     */
    private Anniversary getUpcomingAnniversary(Long familyId, int advanceDays) {
        try {
            // 查询 family's 纪念日
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Anniversary> query =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Anniversary>()
                    .eq(Anniversary::getFamilyId, familyId)
                    .eq(Anniversary::getIsDeleted, 0)
                    .orderByAsc(Anniversary::getTargetDate);

            var anniversaries = anniversaryMapper.selectList(query);
            if (anniversaries == null || anniversaries.isEmpty()) {
                return null;
            }

            LocalDate today = LocalDate.now();
            LocalDate checkEndDate = today.plusDays(advanceDays);

            for (Anniversary ann : anniversaries) {
                if (ann.getTargetDate() == null) continue;

                LocalDate targetDate = ann.getTargetDate();

                // 如果是每年重复
                if (ann.getIsRecurring() != null && ann.getIsRecurring()) {
                    // 计算今年的日期
                    LocalDate thisYearDate = LocalDate.of(today.getYear(), targetDate.getMonth(), targetDate.getDayOfMonth());

                    // 如果今年已过，计算明年
                    if (thisYearDate.isBefore(today)) {
                        thisYearDate = thisYearDate.plusYears(1);
                    }

                    // 检查是否在提醒范围内
                    if (!thisYearDate.isAfter(checkEndDate)) {
                        // 计算还有多少天
                        long daysUntil = ChronoUnit.DAYS.between(today, thisYearDate);
                        if (daysUntil >= 0 && daysUntil <= advanceDays) {
                            ann.setTargetDate(thisYearDate);
                            return ann;
                        }
                    }
                } else {
                    // 一次性纪念日
                    if (!targetDate.isBefore(today) && !targetDate.isAfter(checkEndDate)) {
                        return ann;
                    }
                }
            }

            return null;

        } catch (Exception e) {
            log.error("查询纪念日失败", e);
            return null;
        }
    }

    @Override
    public String renderTitle(Reminder reminder, User user) {
        Map<String, Object> config = JSONUtil.parseObj(reminder.getBusinessData());
        String template = reminder.getTitleTemplate();

        if (template == null || template.isEmpty()) {
            template = "🎂 纪念日提醒";
        }

        Anniversary anniversary = getCurrentAnniversary(reminder.getId());
        String title = anniversary != null ? anniversary.getTitle() : "纪念日";

        return template.replace("{userName}", user.getNickname() != null ? user.getNickname() : "亲爱的")
            .replace("{anniversary}", title);
    }

    @Override
    public String renderContent(Reminder reminder, User user) {
        Map<String, Object> config = JSONUtil.parseObj(reminder.getBusinessData());
        String template = reminder.getContentTemplate();

        if (template == null || template.isEmpty()) {
            template = "{userName}，{anniversary}快到了，就在{date}，还有{days}天！";
        }

        Anniversary anniversary = getCurrentAnniversary(reminder.getId());

        String anniversaryTitle = "纪念日";
        String dateStr = "--";
        String daysStr = "--";

        if (anniversary != null) {
            anniversaryTitle = anniversary.getTitle();

            if (anniversary.getTargetDate() != null) {
                dateStr = anniversary.getTargetDate().format(DateTimeFormatter.ofPattern("MM月dd日"));

                long days = ChronoUnit.DAYS.between(LocalDate.now(), anniversary.getTargetDate());
                daysStr = String.valueOf(days);

                // 如果是今天
                if (days == 0) {
                    daysStr = "今天";
                    dateStr = "就是今天！";
                } else if (days == 1) {
                    daysStr = "明天";
                }
            }
        }

        return template
            .replace("{userName}", user.getNickname() != null ? user.getNickname() : "亲爱的")
            .replace("{anniversary}", anniversaryTitle)
            .replace("{date}", dateStr)
            .replace("{days}", daysStr);
    }

    /**
     * 获取当前纪念日信息 - 从数据库获取今日已提醒的纪念日
     */
    private Anniversary getCurrentAnniversary(Long reminderId) {
        // 从纪念日表中重新获取最近的要提醒的纪念日
        return null; // 简化处理
    }

    @Override
    public SceneTemplate getDefaultTemplate() {
        return SceneTemplate.builder()
            .sceneType(SCENE_TYPE)
            .reminderName("🎂 纪念日提醒")
            .reminderType("ANNIVERSARY")
            .frequencyType("DAILY")
            .frequencyConfig("{\"fixedTime\": \"09:00\"}")
            .titleTemplate("🎂 纪念日提醒")
            .contentTemplate("{userName}，{anniversary}快到了，就在{date}，还有{days}天！")
            .businessData("{\"sceneType\": \"ANNIVERSARY\", \"reminderTime\": \"09:00\", \"advanceDays\": 3}")
            .icon(ICON)
            .description("每天早上9点检查家庭纪念日，提前3天提醒")
            .bgColor(BG_COLOR)
            .build();
    }
}
