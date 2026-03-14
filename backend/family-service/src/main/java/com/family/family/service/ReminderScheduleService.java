package com.family.family.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.family.family.entity.Reminder;
import com.family.family.entity.User;
import com.family.family.mapper.ReminderMapper;
import com.family.family.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 提醒调度服务（核心）
 * 每分钟扫描需要执行的提醒任务
 */
@Slf4j
@Service
public class ReminderScheduleService {
    
    @Autowired
    private ReminderMapper reminderMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private WechatWorkService wechatWorkService;
    
    @Autowired
    private ReminderLogService reminderLogService;
    
    @Autowired
    private HolidayService holidayService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 每分钟扫描一次需要执行的提醒
     */
    @Scheduled(cron = "0 * * * * ?")
    public void scanAndExecuteReminders() {
        log.debug("开始扫描定时提醒任务...");
        
        LocalDateTime now = LocalDateTime.now();
        
        // 查询所有需要执行的提醒
        List<Reminder> dueReminders = reminderMapper.selectDueReminders(now);
        
        for (Reminder reminder : dueReminders) {
            try {
                executeReminder(reminder);
            } catch (Exception e) {
                log.error("执行提醒失败: id={}, name={}", reminder.getId(), reminder.getReminderName(), e);
            }
        }
    }
    
    /**
     * 执行单个提醒
     */
    public void executeReminder(Reminder reminder) {
        log.info("执行提醒: {}", reminder.getReminderName());
        
        // 1. 检查免打扰时间
        if (isInQuietHours(reminder)) {
            log.debug("当前在免打扰时间，跳过: {}", reminder.getReminderName());
            return;
        }
        
        // 2. 获取目标用户列表
        List<Long> targetUserIds = getTargetUserIds(reminder);
        
        // 3. 为每个用户生成个性化内容并推送
        for (Long userId : targetUserIds) {
            try {
                // 渲染模板
                String title = renderTemplate(reminder.getTitleTemplate(), userId, reminder);
                String content = renderTemplate(reminder.getContentTemplate(), userId, reminder);
                
                // 企业微信推送
                boolean pushed = wechatWorkService.pushReminder(userId, title, content);
                
                // 记录日志
                reminderLogService.saveLog(reminder, userId, title, content, pushed ? "SUCCESS" : "FAILED");
                
            } catch (Exception e) {
                log.error("推送提醒给用户失败: userId={}", userId, e);
                reminderLogService.saveLog(reminder, userId, null, null, "FAILED");
            }
        }
        
        // 4. 更新执行记录
        updateExecuteRecord(reminder);
        
        // 5. 计算下次执行时间
        calculateNextExecuteTime(reminder);
    }
    
    /**
     * 获取目标用户ID列表
     */
    private List<Long> getTargetUserIds(Reminder reminder) {
        String scope = reminder.getPushScope();
        
        if ("SELF".equals(scope)) {
            // 仅创建者自己
            return Collections.singletonList(reminder.getCreateBy());
        } else if ("ALL".equals(scope)) {
            // 全部用户
            return userMapper.selectList(null).stream()
                    .map(User::getId)
                    .collect(Collectors.toList());
        } else if ("SPECIFIED".equals(scope)) {
            // 指定用户列表
            try {
                String json = reminder.getTargetUserIds();
                if (json != null && !json.isEmpty()) {
                    return objectMapper.readValue(json, new TypeReference<List<Long>>() {});
                }
            } catch (Exception e) {
                log.error("解析目标用户列表失败", e);
            }
        }
        
        return Collections.emptyList();
    }
    
    /**
     * 渲染模板，替换变量
     */
    private String renderTemplate(String template, Long userId, Reminder reminder) {
        if (template == null) return "";
        
        User user = userMapper.selectById(userId);
        String userName = user != null ? user.getNickname() : "亲爱的用户";
        
        Map<String, String> variables = new HashMap<>();
        
        // 基础变量
        variables.put("userName", userName);
        variables.put("date", LocalDate.now().toString());
        variables.put("time", LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
        
        // 天气（如果有天气服务）
        variables.put("weather", getWeather());
        
        // 待办数量
        int todoCount = getTodayTodoCount(userId);
        variables.put("todoCount", String.valueOf(todoCount));
        
        // 业务数据变量
        try {
            String businessData = reminder.getBusinessData();
            if (businessData != null && !businessData.isEmpty()) {
                Map<String, Object> business = objectMapper.readValue(businessData, new TypeReference<Map<String, Object>>() {});
                business.forEach((k, v) -> variables.put(k, v != null ? v.toString() : ""));
            }
        } catch (Exception e) {
            log.warn("解析业务数据失败", e);
        }
        
        // 替换变量
        String result = template;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        
        return result;
    }
    
    /**
     * 判断是否在免打扰时间
     */
    private boolean isInQuietHours(Reminder reminder) {
        String start = reminder.getQuietHoursStart();
        String end = reminder.getQuietHoursEnd();
        
        if (start == null || end == null) return false;
        
        LocalTime now = LocalTime.now();
        LocalTime startTime = LocalTime.parse(start);
        LocalTime endTime = LocalTime.parse(end);
        
        if (startTime.isBefore(endTime)) {
            // 正常情况：22:00 - 08:00
            return now.isAfter(startTime) && now.isBefore(endTime);
        } else {
            // 跨天情况：22:00 - 08:00
            return now.isAfter(startTime) || now.isBefore(endTime);
        }
    }
    
    /**
     * 计算下次执行时间
     */
    public void calculateNextExecuteTime(Reminder reminder) {
        String frequencyType = reminder.getFrequencyType();
        LocalDateTime nextTime = null;
        
        try {
            String configJson = reminder.getFrequencyConfig();
            Map<String, Object> config = objectMapper.readValue(configJson, new TypeReference<Map<String, Object>>() {});
            
            switch (frequencyType) {
                case "ONCE":
                    // 一次性，不计算下次
                    nextTime = null;
                    break;
                    
                case "DAILY":
                    // 每天
                    String fixedTime = (String) config.get("fixedTime");
                    nextTime = LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.parse(fixedTime + ":00"));
                    break;
                    
                case "HOURLY":
                    // 每小时
                    nextTime = LocalDateTime.now().plusHours(1).withMinute(0).withSecond(0);
                    break;
                    
                case "WEEKLY":
                    // 每周指定几天
                    List<Integer> weekDays = (List<Integer>) config.get("weekDays");
                    nextTime = calculateNextWeekDay(weekDays, config);
                    break;
                    
                case "MONTHLY":
                    // 每月指定日期
                    Integer monthDay = (Integer) config.get("monthDay");
                    nextTime = calculateNextMonthDay(monthDay, config);
                    break;
                    
                case "YEARLY":
                    // 每年
                    String yearMonthDay = (String) config.get("yearMonthDay");
                    nextTime = calculateNextYearDay(yearMonthDay);
                    break;
                    
                case "INTERVAL":
                    // 间隔模式
                    nextTime = calculateIntervalTime(config);
                    break;
                    
                default:
                    // 尝试用Cron表达式
                    if (reminder.getCronExpression() != null) {
                        nextTime = calculateNextCronTime(reminder.getCronExpression());
                    }
            }
            
            // 检查是否工作日限制
            if (nextTime != null && isWorkDaysOnly(config)) {
                while (!holidayService.isWorkDay(nextTime.toLocalDate())) {
                    nextTime = nextTime.plusDays(1);
                }
            }
            
        } catch (Exception e) {
            log.error("计算下次执行时间失败", e);
        }
        
        // 更新数据库
        reminder.setNextExecuteTime(nextTime);
        reminderMapper.updateById(reminder);
    }
    
    /**
     * 计算下次周几
     */
    private LocalDateTime calculateNextWeekDay(List<Integer> weekDays, Map<String, Object> config) {
        if (weekDays == null || weekDays.isEmpty()) return null;
        
        LocalDate today = LocalDate.now();
        int todayWeekDay = today.getDayOfWeek().getValue();
        
        // 找到下一个匹配的周几
        int daysToAdd = 0;
        boolean found = false;
        
        for (int i = 1; i <= 7; i++) {
            int checkDay = ((todayWeekDay - 1 + i) % 7) + 1;
            if (weekDays.contains(checkDay)) {
                daysToAdd = i;
                found = true;
                break;
            }
        }
        
        if (!found) return null;
        
        LocalDate nextDate = today.plusDays(daysToAdd);
        String fixedTime = (String) config.getOrDefault("fixedTime", "08:00");
        return LocalDateTime.of(nextDate, LocalTime.parse(fixedTime + ":00"));
    }
    
    /**
     * 计算下次月几号
     */
    private LocalDateTime calculateNextMonthDay(Integer monthDay, Map<String, Object> config) {
        if (monthDay == null) return null;
        
        LocalDate today = LocalDate.now();
        LocalDate targetDate = LocalDate.of(today.getYear(), today.getMonth(), monthDay);
        
        if (targetDate.isBefore(today) || targetDate.isEqual(today)) {
            // 本月已过，取下个月
            targetDate = targetDate.plusMonths(1);
        }
        
        String fixedTime = (String) config.getOrDefault("fixedTime", "08:00");
        return LocalDateTime.of(targetDate, LocalTime.parse(fixedTime + ":00"));
    }
    
    /**
     * 计算下次年几月几号
     */
    private LocalDateTime calculateNextYearDay(String yearMonthDay) {
        if (yearMonthDay == null) return null;
        
        String[] parts = yearMonthDay.split("-");
        int month = Integer.parseInt(parts[0]);
        int day = Integer.parseInt(parts[1]);
        
        LocalDate today = LocalDate.now();
        LocalDate targetDate = LocalDate.of(today.getYear(), month, day);
        
        if (targetDate.isBefore(today) || targetDate.isEqual(today)) {
            targetDate = targetDate.plusYears(1);
        }
        
        return LocalDateTime.of(targetDate, LocalTime.of(8, 0));
    }
    
    /**
     * 计算间隔时间
     */
    private LocalDateTime calculateIntervalTime(Map<String, Object> config) {
        Integer intervalMinutes = (Integer) config.get("intervalMinutes");
        Integer intervalHours = (Integer) config.get("intervalHours");
        Integer intervalDays = (Integer) config.get("intervalDays");
        
        LocalDateTime now = LocalDateTime.now();
        
        if (intervalMinutes != null) {
            return now.plusMinutes(intervalMinutes);
        } else if (intervalHours != null) {
            return now.plusHours(intervalHours);
        } else if (intervalDays != null) {
            return now.plusDays(intervalDays);
        }
        
        return now.plusDays(1); // 默认1天
    }
    
    /**
     * 计算Cron下次执行时间（简化版）
     */
    private LocalDateTime calculateNextCronTime(String cron) {
        // 实际项目中可以使用 CronExpression 类
        // 这里简化处理：默认1小时后
        return LocalDateTime.now().plusHours(1);
    }
    
    /**
     * 是否仅工作日
     */
    private boolean isWorkDaysOnly(Map<String, Object> config) {
        Object val = config.get("workDaysOnly");
        return val != null && (Boolean) val;
    }
    
    /**
     * 更新执行记录
     */
    private void updateExecuteRecord(Reminder reminder) {
        reminder.setLastExecuteTime(LocalDateTime.now());
        reminder.setLastExecuteResult("SUCCESS");
        reminder.setExecuteCount(reminder.getExecuteCount() + 1);
        reminderMapper.updateById(reminder);
    }
    
    /**
     * 获取今日待办数量（简化）
     */
    private int getTodayTodoCount(Long userId) {
        // 实际应该查询 task 表
        return 3;
    }
    
    /**
     * 获取天气（简化）
     */
    private String getWeather() {
        return "☀️ 晴 18°C";
    }
    
    /**
     * 保存提醒时初始化下次执行时间
     */
    public void initNextExecuteTime(Reminder reminder) {
        calculateNextExecuteTime(reminder);
    }
}
