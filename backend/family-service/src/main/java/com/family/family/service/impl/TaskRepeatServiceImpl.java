package com.family.family.service.impl;

import cn.hutool.json.JSONUtil;
import com.family.family.dto.RepeatRuleResponse;
import com.family.family.entity.Task;
import com.family.family.mapper.TaskMapper;
import com.family.family.service.TaskRepeatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 任务重复服务实现
 */
@Service
public class TaskRepeatServiceImpl implements TaskRepeatService {
    
    private static final Logger log = LoggerFactory.getLogger(TaskRepeatServiceImpl.class);
    
    private final TaskMapper taskMapper;
    
    // 重复类型常量
    public static final String REPEAT_NONE = "none";
    public static final String REPEAT_DAILY = "daily";
    public static final String REPEAT_WEEKLY = "weekly";
    public static final String REPEAT_MONTHLY = "monthly";
    public static final String REPEAT_YEARLY = "yearly";
    public static final String REPEAT_CUSTOM = "custom";
    
    public TaskRepeatServiceImpl(TaskMapper taskMapper) {
        this.taskMapper = taskMapper;
    }
    
    @Override
    public void setRepeatRule(Long taskId, String repeatType, String repeatRule) {
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }
        
        // 验证重复类型
        if (!isValidRepeatType(repeatType)) {
            throw new RuntimeException("无效的重复类型: " + repeatType);
        }
        
        // 验证重复规则JSON格式
        if (repeatRule != null && !repeatRule.isEmpty()) {
            try {
                JSONUtil.parseObj(repeatRule);
            } catch (Exception e) {
                throw new RuntimeException("重复规则JSON格式错误");
            }
        }
        
        task.setRepeatType(repeatType);
        task.setRepeatRule(repeatRule);
        taskMapper.updateById(task);
        
        log.info("设置任务重复规则成功, taskId={}, repeatType={}", taskId, repeatType);
    }
    
    @Override
    public RepeatRuleResponse getRepeatRule(Long taskId) {
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }
        return new RepeatRuleResponse(
            task.getRepeatType(), 
            task.getRepeatRule()
        );
    }
    
    /**
     * 每天凌晨2点执行，生成当日重复任务
     */
    @Override
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void generateDailyRepeatingTasks() {
        log.info("开始生成每日重复任务, date={}", LocalDate.now());
        
        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = LocalDateTime.of(today, LocalTime.MIN);
        LocalDateTime todayEnd = LocalDateTime.of(today, LocalTime.MAX);
        
        // 查询所有设置了重复的任务
        List<Task> repeatingTasks = taskMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Task>()
                .ne(Task::getRepeatType, REPEAT_NONE)
                .ne(Task::getRepeatType, "")
                .isNotNull(Task::getRepeatType)
        );
        
        int generatedCount = 0;
        
        for (Task templateTask : repeatingTasks) {
            try {
                // 检查今天是否需要生成该任务的实例
                if (shouldGenerateToday(templateTask, today)) {
                    // 检查今天是否已生成过
                    boolean exists = checkTaskExistsToday(templateTask.getId(), todayStart, todayEnd);
                    if (!exists) {
                        generateTaskInstance(templateTask, today);
                        generatedCount++;
                    }
                }
            } catch (Exception e) {
                log.error("生成重复任务失败, taskId={}, error={}", templateTask.getId(), e.getMessage());
            }
        }
        
        log.info("生成每日重复任务完成, 生成数量={}", generatedCount);
    }
    
    /**
     * 检查今天是否需要生成任务
     */
    private boolean shouldGenerateToday(Task task, LocalDate today) {
        String repeatType = task.getRepeatType();
        if (repeatType == null || repeatType.isEmpty() || REPEAT_NONE.equals(repeatType)) {
            return false;
        }
        
        switch (repeatType) {
            case REPEAT_DAILY:
                return shouldGenerateDaily(task, today);
            case REPEAT_WEEKLY:
                return shouldGenerateWeekly(task, today);
            case REPEAT_MONTHLY:
                return shouldGenerateMonthly(task, today);
            case REPEAT_YEARLY:
                return shouldGenerateYearly(task, today);
            case REPEAT_CUSTOM:
                return shouldGenerateCustom(task, today);
            default:
                return false;
        }
    }
    
    /**
     * 每日重复
     */
    private boolean shouldGenerateDaily(Task task, LocalDate today) {
        // 检查间隔天数
        Map<String, Object> rule = parseRule(task.getRepeatRule());
        Integer interval = (Integer) rule.getOrDefault("interval", 1);
        
        LocalDate createDate = task.getCreateTime().toLocalDate();
        long daysBetween = ChronoUnit.DAYS.between(createDate, today);
        
        return daysBetween >= 0 && daysBetween % interval == 0;
    }
    
    /**
     * 每周重复
     */
    private boolean shouldGenerateWeekly(Task task, LocalDate today) {
        Map<String, Object> rule = parseRule(task.getRepeatRule());
        @SuppressWarnings("unchecked")
        List<Integer> daysOfWeek = (List<Integer>) rule.get("daysOfWeek");
        Integer interval = (Integer) rule.getOrDefault("interval", 1);
        
        if (daysOfWeek == null || daysOfWeek.isEmpty()) {
            // 默认使用任务创建那天的星期
            daysOfWeek = List.of(task.getCreateTime().getDayOfWeek().getValue());
        }
        
        DayOfWeek todayDayOfWeek = today.getDayOfWeek();
        if (!daysOfWeek.contains(todayDayOfWeek.getValue())) {
            return false;
        }
        
        // 检查间隔周数
        LocalDate createDate = task.getCreateTime().toLocalDate();
        long weeksBetween = ChronoUnit.WEEKS.between(createDate, today);
        return weeksBetween >= 0 && weeksBetween % interval == 0;
    }
    
    /**
     * 每月重复
     */
    private boolean shouldGenerateMonthly(Task task, LocalDate today) {
        Map<String, Object> rule = parseRule(task.getRepeatRule());
        @SuppressWarnings("unchecked")
        List<Integer> daysOfMonth = (List<Integer>) rule.get("daysOfMonth");
        Integer interval = (Integer) rule.getOrDefault("interval", 1);
        
        if (daysOfMonth == null || daysOfMonth.isEmpty()) {
            // 默认使用任务创建那天的日期
            daysOfMonth = List.of(task.getCreateTime().getDayOfMonth());
        }
        
        int todayDayOfMonth = today.getDayOfMonth();
        if (!daysOfMonth.contains(todayDayOfMonth)) {
            return false;
        }
        
        // 检查间隔月数
        LocalDate createDate = task.getCreateTime().toLocalDate();
        long monthsBetween = ChronoUnit.MONTHS.between(
            createDate.withDayOfMonth(1), 
            today.withDayOfMonth(1)
        );
        return monthsBetween >= 0 && monthsBetween % interval == 0;
    }
    
    /**
     * 每年重复
     */
    private boolean shouldGenerateYearly(Task task, LocalDate today) {
        Map<String, Object> rule = parseRule(task.getRepeatRule());
        @SuppressWarnings("unchecked")
        List<String> dates = (List<String>) rule.get("dates");
        Integer interval = (Integer) rule.getOrDefault("interval", 1);
        
        if (dates == null || dates.isEmpty()) {
            // 默认使用任务创建那天的月日
            String defaultDate = String.format("%02d-%02d", 
                task.getCreateTime().getMonthValue(),
                task.getCreateTime().getDayOfMonth());
            dates = List.of(defaultDate);
        }
        
        String todayDate = String.format("%02d-%02d", today.getMonthValue(), today.getDayOfMonth());
        if (!dates.contains(todayDate)) {
            return false;
        }
        
        // 检查间隔年数
        LocalDate createDate = task.getCreateTime().toLocalDate();
        long yearsBetween = ChronoUnit.YEARS.between(createDate, today);
        return yearsBetween >= 0 && yearsBetween % interval == 0;
    }
    
    /**
     * 自定义重复规则
     */
    private boolean shouldGenerateCustom(Task task, LocalDate today) {
        Map<String, Object> rule = parseRule(task.getRepeatRule());
        @SuppressWarnings("unchecked")
        List<String> specificDates = (List<String>) rule.get("specificDates");
        
        if (specificDates != null && !specificDates.isEmpty()) {
            String todayStr = today.format(DateTimeFormatter.ISO_LOCAL_DATE);
            return specificDates.contains(todayStr);
        }
        
        return false;
    }
    
    /**
     * 检查今天是否已存在该任务的实例
     */
    private boolean checkTaskExistsToday(Long parentTaskId, LocalDateTime start, LocalDateTime end) {
        Long count = taskMapper.selectCount(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Task>()
                .eq(Task::getParentId, parentTaskId)
                .between(Task::getCreateTime, start, end)
        );
        return count > 0;
    }
    
    /**
     * 生成任务实例
     */
    private void generateTaskInstance(Task templateTask, LocalDate date) {
        Task instance = new Task();
        instance.setFamilyId(templateTask.getFamilyId());
        instance.setCategoryId(templateTask.getCategoryId());
        instance.setTitle(templateTask.getTitle());
        instance.setContent(templateTask.getContent());
        instance.setPriority(templateTask.getPriority());
        instance.setStatus(0); // 待办
        instance.setAssigneeId(templateTask.getAssigneeId());
        instance.setCreatorId(templateTask.getCreatorId());
        instance.setParentId(templateTask.getId());
        instance.setRepeatType(REPEAT_NONE); // 实例任务不重复
        instance.setLocation(templateTask.getLocation());
        instance.setAttachments(templateTask.getAttachments());
        
        // 设置截止时间（保持原任务的时间，只改日期）
        if (templateTask.getDueTime() != null) {
            LocalDateTime dueTime = LocalDateTime.of(
                date,
                templateTask.getDueTime().toLocalTime()
            );
            instance.setDueTime(dueTime);
        }
        
        // 设置提醒时间
        if (templateTask.getRemindTime() != null) {
            LocalDateTime remindTime = LocalDateTime.of(
                date,
                templateTask.getRemindTime().toLocalTime()
            );
            instance.setRemindTime(remindTime);
        }
        
        taskMapper.insert(instance);
        log.debug("生成任务实例成功, parentTaskId={}, instanceId={}", 
            templateTask.getId(), instance.getId());
    }
    
    /**
     * 解析重复规则JSON
     */
    private Map<String, Object> parseRule(String repeatRule) {
        if (repeatRule == null || repeatRule.isEmpty()) {
            return new HashMap<>();
        }
        try {
            return JSONUtil.toBean(repeatRule, Map.class);
        } catch (Exception e) {
            log.warn("解析重复规则失败: {}", repeatRule);
            return new HashMap<>();
        }
    }
    
    /**
     * 验证重复类型
     */
    private boolean isValidRepeatType(String repeatType) {
        if (repeatType == null || repeatType.isEmpty()) {
            return false;
        }
        return List.of(REPEAT_NONE, REPEAT_DAILY, REPEAT_WEEKLY, 
                      REPEAT_MONTHLY, REPEAT_YEARLY, REPEAT_CUSTOM)
                   .contains(repeatType);
    }
}
