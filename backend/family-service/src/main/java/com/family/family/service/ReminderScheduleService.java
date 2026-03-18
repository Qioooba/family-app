package com.family.family.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.family.family.entity.Reminder;
import com.family.family.entity.User;
import com.family.family.entity.WechatMessage;
import com.family.family.entity.MessageType;
import com.family.family.mapper.ReminderMapper;
import com.family.family.mapper.TaskMapper;
import com.family.family.mapper.UserMapper;
import com.family.family.service.scene.SceneHandlerFactory;
import com.family.family.service.scene.SceneReminderHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.script.DefaultRedisScript;

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
    private TaskMapper taskMapper;
    
    @Autowired
    private WechatWorkService wechatWorkService;
    
    @Autowired
    private StringRedisTemplate redisTemplate;
    
    @Autowired
    private ReminderLogService reminderLogService;
    
    @Autowired
    private HolidayService holidayService;
    
    @Autowired
    private SceneHandlerFactory sceneHandlerFactory;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // Lua脚本：原子性释放分布式锁（只有锁值匹配时才删除）
    private static final String RELEASE_LOCK_SCRIPT = 
        "if redis.call('get', KEYS[1]) == ARGV[1] then " +
        "    return redis.call('del', KEYS[1]) " +
        "else " +
        "    return 0 " +
        "end";
    
    /**
     * 每分钟扫描一次需要执行的提醒
     * 使用分布式锁防止多实例重复执行
     */
    @Scheduled(cron = "0 * * * * ?")
    public void scanAndExecuteReminders() {
        // 分布式锁键
        String lockKey = "reminder:schedule:lock";
        String lockValue = UUID.randomUUID().toString();
        
        try {
            // 尝试获取锁，有效期2分钟
            Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, lockValue, 2, TimeUnit.MINUTES);
            if (!Boolean.TRUE.equals(locked)) {
                log.debug("未获取到分布式锁，跳过本次执行");
                return;
            }
            
            log.debug("获取到分布式锁，开始扫描定时提醒任务...");
            
            LocalDateTime now = LocalDateTime.now();
            
            // 查询所有需要执行的提醒（最多100条）
            List<Reminder> dueReminders = reminderMapper.selectDueReminders(now);
            
            log.info("扫描到 {} 个需要执行的提醒", dueReminders.size());
            
            for (Reminder reminder : dueReminders) {
                try {
                    executeReminder(reminder);
                } catch (Exception e) {
                    log.error("执行提醒失败: id={}, name={}", reminder.getId(), reminder.getReminderName(), e);
                }
            }
        } finally {
            // 使用Lua脚本原子性释放锁
            try {
                DefaultRedisScript<Long> script = new DefaultRedisScript<>(RELEASE_LOCK_SCRIPT, Long.class);
                Long result = redisTemplate.execute(script, Collections.singletonList(lockKey), lockValue);
                if (result != null && result > 0) {
                    log.debug("释放分布式锁成功");
                }
            } catch (Exception e) {
                log.warn("释放分布式锁失败", e);
            }
        }
    }
    
    /**
     * 执行单个提醒 - 参考待办任务推送格式，带小程序码
     */
    @Transactional(rollbackFor = Exception.class)
    public void executeReminder(Reminder reminder) {
        log.info("执行提醒: {}", reminder.getReminderName());
        
        // 1. 检查免打扰时间
        if (isInQuietHours(reminder)) {
            log.debug("当前在免打扰时间，跳过: {}", reminder.getReminderName());
            return;
        }
        
        // 2. 检查节假日跳过逻辑
        if (shouldSkipHoliday(reminder)) {
            log.debug("当前是非工作日且设置了仅工作日推送，跳过: {}", reminder.getReminderName());
            // 更新下次执行时间到下一个工作日
            skipToNextWorkDay(reminder);
            return;
        }
        
        // 3. 获取目标用户列表
        List<Long> targetUserIds = getTargetUserIds(reminder);
        
        // 调试日志
        log.info("提醒[{}]推送范围: scope={}, targetUserIds={}, 实际目标用户: {}", 
                reminder.getReminderName(), 
                reminder.getPushScope(),
                reminder.getTargetUserIds(),
                targetUserIds);
        
        if (targetUserIds.isEmpty()) {
            log.warn("提醒没有目标用户: {}", reminder.getReminderName());
            updateExecuteRecord(reminder, "NO_TARGET", "没有目标用户");
            return;
        }
        
        // 4. 为每个用户推送（使用模板变量渲染）
        log.info("开始推送提醒[{}]给{}个用户", reminder.getReminderName(), targetUserIds.size());
        for (Long userId : targetUserIds) {            log.info("正在推送提醒[{}]给用户{}", reminder.getReminderName(), userId);
            
            try {
                // 渲染模板变量
                String title = renderTemplate(reminder.getTitleTemplate(), userId, reminder);
                String content = renderTemplate(reminder.getContentTemplate(), userId, reminder);
                
                // 简化消息格式 - 只保留核心信息
                String timeStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM-dd HH:mm"));
                
                // 构建简洁的消息描述（去除重复内容）
                StringBuilder desc = new StringBuilder();
                
                // 如果有内容且内容不等于标题，显示内容
                if (content != null && !content.trim().isEmpty() && !content.equals(title)) {
                    desc.append(content).append("\n\n");
                }
                
                // 时间信息（简化，不显示下次执行）
                desc.append("⏰ ").append(timeStr).append("\n");
                
                // 推送范围（可选）
                if ("ALL".equals(reminder.getPushScope())) {
                    desc.append("👥 全员推送\n");
                }
                
                // 使用专门的提醒推送方法（带小程序码）
                wechatWorkService.sendReminderNotification(
                    userId, 
                    title, 
                    desc.toString(), 
                    reminder.getReminderType(),
                    "/pages/reminder/index"
                );
                
                // 记录日志
                reminderLogService.saveLog(reminder, userId, title, desc.toString(), "SUCCESS");
                
            } catch (Exception e) {
                log.error("推送提醒给用户失败: userId={}", userId, e);
                reminderLogService.saveLog(reminder, userId, null, null, "FAILED");
            }
        }
        
        // 5. 更新执行记录
        updateExecuteRecord(reminder, "SUCCESS", null);
        
        // 6. 计算下次执行时间
        calculateNextExecuteTime(reminder);
    }
    
    /**
     * 渲染模板，替换变量
     * 支持的变量：
     * - {userName} 用户名
     * - {date} 当前日期 (yyyy-MM-dd)
     * - {time} 当前时间 (HH:mm)
     * - {weekday} 星期几
     * - {weather} 天气
     * - {todoCount} 今日待办数量
     * - {businessData.key} 业务数据中的字段
     */
    private String renderTemplate(String template, Long userId, Reminder reminder) {
        if (template == null || template.isEmpty()) {
            return reminder.getReminderName();
        }
        
        User user = userMapper.selectById(userId);
        String userName = user != null ? user.getNickname() : "亲爱的用户";
        
        LocalDateTime now = LocalDateTime.now();
        Map<String, String> variables = new HashMap<>();
        
        // 基础变量
        variables.put("userName", userName);
        variables.put("date", now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        variables.put("time", now.format(DateTimeFormatter.ofPattern("HH:mm")));
        variables.put("weekday", getWeekdayText(now.getDayOfWeek().getValue()));
        
        // 天气（如果有天气服务）
        variables.put("weather", getWeather());
        
        // 待办数量
        int todoCount = getTodayTodoCount(userId);
        variables.put("todoCount", String.valueOf(todoCount));
        
        // 提醒相关变量
        variables.put("reminderName", reminder.getReminderName());
        variables.put("reminderType", getReminderTypeText(reminder.getReminderType()));
        
        // 业务数据变量（支持嵌套属性）
        try {
            String businessData = reminder.getBusinessData();
            if (businessData != null && !businessData.isEmpty()) {
                Map<String, Object> business = objectMapper.readValue(businessData, new TypeReference<Map<String, Object>>() {});
                flattenMap(business, "", variables);
            }
        } catch (Exception e) {
            log.warn("解析业务数据失败", e);
        }
        
        // 频率配置变量
        try {
            String freqConfig = reminder.getFrequencyConfig();
            if (freqConfig != null && !freqConfig.isEmpty()) {
                Map<String, Object> config = objectMapper.readValue(freqConfig, new TypeReference<Map<String, Object>>() {});
                config.forEach((k, v) -> {
                    if (v != null) {
                        variables.put("config." + k, v.toString());
                    }
                });
            }
        } catch (Exception e) {
            log.warn("解析频率配置失败", e);
        }
        
        // 替换变量（支持默认值语法 {variable:defaultValue}）
        String result = template;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            // 标准格式 {key}
            result = result.replace("{" + key + "}", value);
            // 带默认值的格式 {key:default}
            result = result.replaceAll("\\{" + key + ":([^}]*)\\}", value);
        }
        
        // 清理未匹配的变量（保留默认值或清空）
        result = result.replaceAll("\\{[^:}]+:([^}]*)\\}", "$1"); // {var:default} -> default
        result = result.replaceAll("\\{[^}]+\\}", ""); // {var} -> ""
        
        return result;
    }
    
    /**
     * 扁平化Map，支持嵌套属性
     */
    private void flattenMap(Map<String, Object> map, String prefix, Map<String, String> result) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
            Object value = entry.getValue();
            
            if (value instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> nestedMap = (Map<String, Object>) value;
                flattenMap(nestedMap, key, result);
            } else {
                result.put(key, value != null ? value.toString() : "");
            }
        }
    }
    
    /**
     * 判断是否应跳过节假日
     */
    private boolean shouldSkipHoliday(Reminder reminder) {
        try {
            String freqConfig = reminder.getFrequencyConfig();
            if (freqConfig == null || freqConfig.isEmpty()) {
                return false;
            }
            
            Map<String, Object> config = objectMapper.readValue(freqConfig, new TypeReference<Map<String, Object>>() {});
            Object workDaysOnly = config.get("workDaysOnly");
            
            if (workDaysOnly != null && Boolean.TRUE.equals(workDaysOnly)) {
                // 设置了仅工作日推送，检查今天是否是工作日
                LocalDate today = LocalDate.now();
                return !holidayService.isWorkDay(today);
            }
        } catch (Exception e) {
            log.warn("检查节假日配置失败", e);
        }
        
        return false;
    }
    
    /**
     * 跳过到下一个工作日
     */
    private void skipToNextWorkDay(Reminder reminder) {
        LocalDateTime nextTime = reminder.getNextExecuteTime();
        if (nextTime == null) {
            nextTime = LocalDateTime.now();
        }
        
        // 找到下一个工作日
        LocalDate nextDate = nextTime.toLocalDate();
        do {
            nextDate = nextDate.plusDays(1);
        } while (!holidayService.isWorkDay(nextDate));
        
        // 保持原来的时间
        LocalDateTime newNextTime = LocalDateTime.of(nextDate, nextTime.toLocalTime());
        reminder.setNextExecuteTime(newNextTime);
        reminderMapper.updateById(reminder);
        
        log.info("提醒 {} 已跳过到下一个工作日: {}", reminder.getReminderName(), newNextTime);
    }
    
    /**
     * 获取频率中文文本
     */
    private String getFrequencyText(String frequencyType) {
        Map<String, String> map = new HashMap<>();
        map.put("ONCE", "一次性");
        map.put("DAILY", "每天");
        map.put("HOURLY", "每小时");
        map.put("WEEKLY", "每周");
        map.put("MONTHLY", "每月");
        map.put("YEARLY", "每年");
        map.put("INTERVAL", "间隔");
        return map.getOrDefault(frequencyType, frequencyType);
    }
    
    /**
     * 获取提醒类型中文文本
     */
    private String getReminderTypeText(String type) {
        Map<String, String> map = new HashMap<>();
        map.put("WATER", "喝水提醒");
        map.put("MEDICINE", "用药提醒");
        map.put("EXPIRE", "保质期");
        map.put("BIRTHDAY", "生日提醒");
        map.put("FINANCE", "财务提醒");
        map.put("SYSTEM", "系统提醒");
        return map.getOrDefault(type, "提醒");
    }
    
    /**
     * 获取星期中文文本
     */
    private String getWeekdayText(int weekday) {
        String[] weekdays = {"", "周一", "周二", "周三", "周四", "周五", "周六", "周日"};
        return weekdays[weekday];
    }
    
    /**
     * 获取目标用户ID列表
     */
    private List<Long> getTargetUserIds(Reminder reminder) {
        String scope = reminder.getPushScope();
        
        if (scope == null || "SELF".equals(scope)) {
            // 仅创建者自己
            return Collections.singletonList(reminder.getCreateBy());
        } else if ("ALL".equals(scope)) {
            // 全部用户（最多100个）
            return userMapper.selectList(null).stream()
                    .map(User::getId)
                    .limit(100)
                    .collect(Collectors.toList());
        } else if ("SPECIFIED".equals(scope)) {
            // 指定用户列表
            try {
                String json = reminder.getTargetUserIds();
                if (json != null && !json.isEmpty()) {
                    List<Long> ids = objectMapper.readValue(json, new TypeReference<List<Long>>() {});
                    return ids.stream().limit(100).collect(Collectors.toList());
                }
            } catch (Exception e) {
                log.error("解析目标用户列表失败", e);
            }
        }
        
        // 默认返回创建者
        return Collections.singletonList(reminder.getCreateBy());
    }
    
    /**
     * 判断是否在免打扰时间
     */
    private boolean isInQuietHours(Reminder reminder) {
        String start = reminder.getQuietHoursStart();
        String end = reminder.getQuietHoursEnd();
        
        if (start == null || start.isEmpty() || end == null || end.isEmpty()) {
            return false;
        }
        
        LocalTime now = LocalTime.now();
        LocalTime startTime;
        LocalTime endTime;
        
        try {
            startTime = LocalTime.parse(start);
            endTime = LocalTime.parse(end);
        } catch (Exception e) {
            log.warn("解析免打扰时间失败: start={}, end={}", start, end);
            return false;
        }
        
        if (startTime.isBefore(endTime)) {
            // 正常情况：如 22:00 - 08:00
            return now.isAfter(startTime) && now.isBefore(endTime);
        } else {
            // 跨天情况：如 22:00 - 08:00
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
                    // 一次性提醒：如果是新建（未执行过），设置执行时间；已执行过则保持null
                    if (reminder.getLastExecuteTime() == null) {
                        // 新建提醒，设置下次执行时间
                        String onceDate = (String) config.get("date");
                        String onceTime = (String) config.get("time");
                        if (onceDate != null && onceTime != null) {
                            nextTime = LocalDateTime.parse(onceDate + "T" + onceTime);
                        } else {
                            nextTime = LocalDateTime.now().plusMinutes(1);
                        }
                    }
                    // 已执行过的ONCE提醒，nextTime保持null，后面会设置status=0
                    break;
                    
                case "DAILY":
                    // 每天
                    String fixedTime = (String) config.get("fixedTime");
                    LocalTime dailyTime = LocalTime.parse(fixedTime);
                    LocalDateTime todayWithTime = LocalDateTime.of(LocalDate.now(), dailyTime);
                    
                    // 如果今天的时间还没到，设为今天；否则设为明天
                    if (todayWithTime.isAfter(LocalDateTime.now())) {
                        nextTime = todayWithTime;
                    } else {
                        nextTime = todayWithTime.plusDays(1);
                    }
                    break;
                    
                case "HOURLY":
                    // 每小时
                    nextTime = LocalDateTime.now().plusHours(1).withMinute(0).withSecond(0);
                    break;
                    
                case "WEEKLY":
                    // 每周指定几天
                    @SuppressWarnings("unchecked")
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
                    if (reminder.getCronExpression() != null && !reminder.getCronExpression().isEmpty()) {
                        nextTime = calculateNextCronTime(reminder.getCronExpression());
                    }
            }
            
            // 检查是否工作日限制
            if (nextTime != null && isWorkDaysOnly(config)) {
                LocalDate checkDate = nextTime.toLocalDate();
                int maxSkipDays = 30; // 最多跳过30天
                int skipped = 0;
                while (!holidayService.isWorkDay(checkDate) && skipped < maxSkipDays) {
                    checkDate = checkDate.plusDays(1);
                    skipped++;
                }
                if (skipped > 0) {
                    nextTime = LocalDateTime.of(checkDate, nextTime.toLocalTime());
                    log.info("提醒 {} 已调整到下一个工作日: {}", reminder.getReminderName(), nextTime);
                }
            }
            
        } catch (Exception e) {
            log.error("计算下次执行时间失败, reminderId={}, frequencyType={}", reminder.getId(), reminder.getFrequencyType(), e);
        }
        
        // 更新数据库
        if (nextTime != null) {
            reminder.setNextExecuteTime(nextTime);
            reminderMapper.updateById(reminder);
            log.info("提醒[{}]下次执行时间已更新: {}", reminder.getReminderName(), nextTime);
        } else if ("ONCE".equals(frequencyType)) {
            // 一次性提醒：nextTime为null是正常的，但需要更新status=0
            reminder.setStatus(0);  // 立即停用，防止重复执行
            reminder.setNextExecuteTime(null);
            reminderMapper.updateById(reminder);
            log.info("一次性提醒[{}]已执行完成并停用", reminder.getReminderName());
        } else {
            log.warn("无法计算提醒[{}]的下次执行时间", reminder.getReminderName());
        }
    }
    
    /**
     * 计算下次周几
     * 修复：确保找到下一个执行日期，避免当天重复执行
     */
    private LocalDateTime calculateNextWeekDay(List<Integer> weekDays, Map<String, Object> config) {
        if (weekDays == null || weekDays.isEmpty()) return null;
        
        LocalDate today = LocalDate.now();
        int todayWeekDay = today.getDayOfWeek().getValue();
        String fixedTimeStr = (String) config.getOrDefault("fixedTime", "08:00");
        LocalTime fixedTime = LocalTime.parse(fixedTimeStr);
        LocalTime nowTime = LocalTime.now();
        
        // 找到下一个匹配的周几
        int daysToAdd = 0;
        boolean found = false;
        
        for (int i = 1; i <= 7; i++) {
            int checkDay = ((todayWeekDay - 1 + i) % 7) + 1;
            if (weekDays.contains(checkDay)) {
                // 如果是今天，检查时间是否已过
                if (i == 7) { // i=7表示一周后的同一天（今天）
                    // 如果今天的固定时间还没到，可以今天执行
                    if (nowTime.isBefore(fixedTime)) {
                        daysToAdd = 0;
                    } else {
                        // 今天时间已过，找下一个周期
                        continue;
                    }
                } else {
                    daysToAdd = i;
                }
                found = true;
                break;
            }
        }
        
        // 如果没找到（比如所有配置的时间今天都已过），找下一周的同一天
        if (!found) {
            for (int i = 1; i <= 7; i++) {
                int checkDay = ((todayWeekDay - 1 + i) % 7) + 1;
                if (weekDays.contains(checkDay)) {
                    daysToAdd = i;
                    found = true;
                    break;
                }
            }
        }
        
        if (!found) return null;
        
        LocalDate nextDate = today.plusDays(daysToAdd);
        return LocalDateTime.of(nextDate, fixedTime);
    }
    
    /**
     * 计算下次月几号
     */
    private LocalDateTime calculateNextMonthDay(Integer monthDay, Map<String, Object> config) {
        if (monthDay == null) return null;
        
        LocalDate today = LocalDate.now();
        LocalDate targetDate = LocalDate.of(today.getYear(), today.getMonth(), Math.min(monthDay, today.lengthOfMonth()));
        
        if (targetDate.isBefore(today) || targetDate.isEqual(today)) {
            // 本月已过，取下个月
            targetDate = targetDate.plusMonths(1);
            // 处理月份天数不一致的情况（如1月31日到2月）
            if (monthDay > targetDate.lengthOfMonth()) {
                targetDate = targetDate.withDayOfMonth(targetDate.lengthOfMonth());
            } else {
                targetDate = targetDate.withDayOfMonth(monthDay);
            }
        }
        
        String fixedTime = (String) config.getOrDefault("fixedTime", "08:00");
        return LocalDateTime.of(targetDate, LocalTime.parse(fixedTime));
    }
    
    /**
     * 计算下次年几月几号
     */
    private LocalDateTime calculateNextYearDay(String yearMonthDay) {
        if (yearMonthDay == null || yearMonthDay.isEmpty()) return null;
        
        try {
            String[] parts = yearMonthDay.split("-");
            int month = Integer.parseInt(parts[0]);
            int day = Integer.parseInt(parts[1]);
            
            LocalDate today = LocalDate.now();
            LocalDate targetDate = LocalDate.of(today.getYear(), month, day);
            
            if (targetDate.isBefore(today) || targetDate.isEqual(today)) {
                targetDate = targetDate.plusYears(1);
            }
            
            return LocalDateTime.of(targetDate, LocalTime.of(8, 0));
        } catch (Exception e) {
            log.error("解析年月日失败: {}", yearMonthDay, e);
            return null;
        }
    }
    
    /**
     * 计算间隔时间
     */
    private LocalDateTime calculateIntervalTime(Map<String, Object> config) {
        // 使用 Number 类型避免 Long/Integer 转换问题
        Number intervalMinutes = (Number) config.get("intervalMinutes");
        Number intervalHours = (Number) config.get("intervalHours");
        Number intervalDays = (Number) config.get("intervalDays");
        
        LocalDateTime now = LocalDateTime.now();
        
        if (intervalMinutes != null && intervalMinutes.intValue() > 0) {
            return now.plusMinutes(intervalMinutes.intValue());
        } else if (intervalHours != null && intervalHours.intValue() > 0) {
            return now.plusHours(intervalHours.intValue());
        } else if (intervalDays != null && intervalDays.intValue() > 0) {
            return now.plusDays(intervalDays.intValue());
        }
        
        return now.plusDays(1); // 默认1天
    }
    
    /**
     * 计算Cron下次执行时间（使用Spring的CronExpression）
     */
    private LocalDateTime calculateNextCronTime(String cron) {
        try {
            CronExpression cronExpression = CronExpression.parse(cron);
            // 获取下次执行时间（从当前时间开始）
            ZonedDateTime nextExecution = cronExpression.next(ZonedDateTime.now());
            if (nextExecution != null) {
                return nextExecution.toLocalDateTime();
            }
        } catch (Exception e) {
            log.error("解析Cron表达式失败: {}", cron, e);
        }
        // 解析失败时默认1小时后
        return LocalDateTime.now().plusHours(1);
    }
    
    /**
     * 是否仅工作日
     */
    private boolean isWorkDaysOnly(Map<String, Object> config) {
        Object val = config.get("workDaysOnly");
        return val != null && Boolean.TRUE.equals(val);
    }
    
    /**
     * 更新执行记录
     */
    private void updateExecuteRecord(Reminder reminder, String result, String message) {
        reminder.setLastExecuteTime(LocalDateTime.now());
        reminder.setLastExecuteResult(result);
        reminder.setExecuteCount(reminder.getExecuteCount() != null ? reminder.getExecuteCount() + 1 : 1);
        reminderMapper.updateById(reminder);
    }
    
    /**
     * 获取今日待办数量
     */
    private int getTodayTodoCount(Long userId) {
        try {
            return taskMapper.countTodayTodos(userId);
        } catch (Exception e) {
            log.warn("查询今日待办数量失败: userId={}", userId, e);
            return 0;
        }
    }
    
    /**
     * 获取天气（简化版，后续可接入天气API）
     */
    private String getWeather() {
        // TODO: 接入天气API，如和风天气、OpenWeatherMap等
        // 临时返回简化天气，避免模板变量为空
        return "☀️";
    }
    
    /**
     * 保存提醒时初始化下次执行时间
     */
    public void initNextExecuteTime(Reminder reminder) {
        calculateNextExecuteTime(reminder);
    }
    
    /**
     * 手动触发提醒执行（用于测试）
     */
    public void executeReminderManually(Long reminderId) {
        Reminder reminder = reminderMapper.selectById(reminderId);
        if (reminder != null) {
            executeReminder(reminder);
        } else {
            throw new RuntimeException("提醒不存在: " + reminderId);
        }
    }
    
    // ==================== 场景化提醒调度 ====================
    
    /**
     * 场景化提醒调度（每天早上7/8点执行）
     * 用于天气类、需要动态判断的提醒
     */
    @Scheduled(cron = "0 0 7,8,12,18 * * ?")
    public void triggerSceneReminders() {
        log.info("开始检查场景化提醒...");
        
        // 场景化提醒类型列表
        List<String> sceneTypes = Arrays.asList(
            "WEATHER_RAIN", "WEATHER_TEMP", "SEDENTARY", "EYE_REST", "WATER"
        );
        
        for (String sceneType : sceneTypes) {
            try {
                triggerSceneReminderByType(sceneType);
            } catch (Exception e) {
                log.error("触发场景提醒失败: {}", sceneType, e);
            }
        }
        
        log.info("场景化提醒检查完成");
    }
    
    /**
     * 触发指定类型的场景提醒
     */
    private void triggerSceneReminderByType(String sceneType) {
        SceneReminderHandler handler = sceneHandlerFactory.getHandler(sceneType);
        if (handler == null) {
            log.warn("未找到场景处理器: {}", sceneType);
            return;
        }
        
        // 查询该类型的所有启用提醒
        List<Reminder> reminders = reminderMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Reminder>()
                .eq(Reminder::getReminderType, sceneType)
                .eq(Reminder::getStatus, 1)
        );
        
        if (reminders.isEmpty()) {
            return;
        }
        
        log.info("检查场景提醒[{}]: 找到{}个提醒", sceneType, reminders.size());
        
        for (Reminder reminder : reminders) {
            try {
                // 使用场景处理器判断是否触发
                if (handler.shouldTrigger(reminder)) {
                    log.info("场景提醒[{}]触发执行: {}", sceneType, reminder.getReminderName());
                    executeSceneReminder(reminder, handler);
                }
            } catch (Exception e) {
                log.error("执行场景提醒失败: id={}, type={}", reminder.getId(), sceneType, e);
            }
        }
    }
    
    /**
     * 执行场景化提醒
     */
    private void executeSceneReminder(Reminder reminder, SceneReminderHandler handler) {
        // 1. 检查免打扰时间
        if (isInQuietHours(reminder)) {
            log.debug("当前在免打扰时间，跳过场景提醒: {}", reminder.getReminderName());
            return;
        }
        
        // 2. 获取目标用户
        List<Long> targetUserIds = getTargetUserIds(reminder);
        if (targetUserIds.isEmpty()) {
            log.warn("场景提醒没有目标用户: {}", reminder.getReminderName());
            return;
        }
        
        // 3. 为每个用户推送
        log.info("开始推送场景提醒[{}]给{}个用户", reminder.getReminderName(), targetUserIds.size());
        
        for (Long userId : targetUserIds) {            
            try {
                User user = userMapper.selectById(userId);
                if (user == null) continue;
                
                // 使用场景处理器渲染内容
                String title = handler.renderTitle(reminder, user);
                String content = handler.renderContent(reminder, user);
                
                // 构建消息
                String timeStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM-dd HH:mm"));
                StringBuilder desc = new StringBuilder();
                if (content != null && !content.trim().isEmpty()) {
                    desc.append(content).append("\n\n");
                }
                desc.append("⏰ ").append(timeStr).append("\n");
                
                // 发送推送
                wechatWorkService.sendReminderNotification(
                    userId, 
                    title, 
                    desc.toString(), 
                    reminder.getReminderType(),
                    "/pages/reminder/index"
                );
                
                // 记录日志
                reminderLogService.saveLog(reminder, userId, title, desc.toString(), "SUCCESS");
                
            } catch (Exception e) {
                log.error("推送场景提醒给用户失败: userId={}", userId, e);
                reminderLogService.saveLog(reminder, userId, null, null, "FAILED");
            }
        }
        
        // 4. 更新执行记录
        updateExecuteRecord(reminder, "SUCCESS", null);
        
        // 5. 标记已提醒（防止重复）
        markSceneReminded(reminder.getId(), reminder.getReminderType());
    }
    
    /**
     * 标记场景提醒已执行（防重复）
     */
    private void markSceneReminded(Long reminderId, String sceneType) {
        String today = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        String cacheKey = String.format("scene:%s:%d:%s", sceneType, reminderId, today);
        redisTemplate.opsForValue().set(cacheKey, "1", 24, TimeUnit.HOURS);
    }
}
