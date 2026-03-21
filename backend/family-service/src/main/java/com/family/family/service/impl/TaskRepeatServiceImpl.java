package com.family.family.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.family.common.core.Result;
import com.family.family.dto.RepeatRuleResponse;
import com.family.family.entity.Task;
import com.family.family.mapper.TaskMapper;
import com.family.family.service.HolidayService;
import com.family.family.service.TaskRepeatService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 任务重复服务实现
 */
@Service
public class TaskRepeatServiceImpl implements TaskRepeatService {

    private static final Logger log = LoggerFactory.getLogger(TaskRepeatServiceImpl.class);

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private HolidayService holidayService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public TaskRepeatServiceImpl() {
    }

    @Override
    public void setRepeatRule(Long taskId, String repeatType, String repeatRule) {
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            log.warn("设置重复规则失败，任务不存在: taskId={}", taskId);
            return;
        }

        // 转换重复类型字符串到整数
        Integer repeatTypeInt = convertRepeatTypeStringToInt(repeatType);
        task.setRepeatType(repeatTypeInt);
        task.setRepeatRule(repeatRule);
        task.setRepeatStartDate(LocalDate.now());

        // 计算下次执行时间
        LocalDateTime nextRun = calculateNextRunTime(task.getDueTime(), repeatTypeInt, repeatRule, LocalDate.now());
        task.setNextRunTime(nextRun);

        taskMapper.updateById(task);
        log.info("设置重复规则成功: taskId={}, repeatType={}, nextRunTime={}", taskId, repeatType, nextRun);
    }

    @Override
    public RepeatRuleResponse getRepeatRule(Long taskId) {
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            return new RepeatRuleResponse(null, null);
        }
        return new RepeatRuleResponse(
            convertRepeatTypeIntToString(task.getRepeatType()),
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
        log.info("开始生成每日重复任务");
        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime todayEnd = today.atTime(LocalTime.MAX);

        // 查询今天需要执行的任务（nextRunTime在今天范围内）
        List<Task> tasksToRepeat = taskMapper.selectList(
            new LambdaQueryWrapper<Task>()
                .eq(Task::getRepeatType, 0) // 获取有重复类型的任务
                .isNotNull(Task::getNextRunTime)
                .ge(Task::getNextRunTime, todayStart)
                .le(Task::getNextRunTime, todayEnd)
        );

        log.info("找到 {} 个需要生成重复实例的任务", tasksToRepeat.size());

        for (Task sourceTask : tasksToRepeat) {
            try {
                // 创建新的任务实例（基于原任务）
                Task newTask = createRepeatInstance(sourceTask, today);
                taskMapper.insert(newTask);

                // 更新原任务的下次执行时间
                LocalDateTime nextRun = calculateNextRunTime(
                    sourceTask.getDueTime(),
                    sourceTask.getRepeatType(),
                    sourceTask.getRepeatRule(),
                    today
                );
                sourceTask.setNextRunTime(nextRun);
                taskMapper.updateById(sourceTask);

                log.info("生成重复任务实例: 原任务={}, 新任务={}, 下次执行={}",
                    sourceTask.getId(), newTask.getId(), nextRun);
            } catch (Exception e) {
                log.error("生成重复任务失败: sourceTaskId={}", sourceTask.getId(), e);
            }
        }

        log.info("每日重复任务生成完成");
    }

    /**
     * 计算下次执行时间
     */
    private LocalDateTime calculateNextRunTime(LocalDateTime baseTime, Integer repeatType, String repeatRule, LocalDate fromDate) {
        if (repeatType == null || repeatType == 0) {
            return null;
        }

        LocalDateTime base = baseTime != null ? baseTime : fromDate.atTime(9, 0); // 默认早上9点

        switch (repeatType) {
            case 1: // 每天
                return base.with(temporal -> fromDate.plusDays(1)).withHour(base.getHour()).withMinute(base.getMinute());
            case 2: // 每周
                return base.with(temporal -> fromDate.plusWeeks(1)).withHour(base.getHour()).withMinute(base.getMinute());
            case 3: // 每月
                return base.with(temporal -> fromDate.plusMonths(1)).withHour(base.getHour()).withMinute(base.getMinute());
            case 4: // 每年
                return base.with(temporal -> fromDate.plusYears(1)).withHour(base.getHour()).withMinute(base.getMinute());
            case 5: // 工作日（仅工作日）
                return calculateNextWorkday(base, fromDate);
            case 6: // 自定义（通过repeatRule JSON配置）
                return calculateCustomRepeat(base, repeatRule, fromDate);
            default:
                return null;
        }
    }

    /**
     * 计算下一个工作日
     */
    private LocalDateTime calculateNextWorkday(LocalDateTime base, LocalDate fromDate) {
        LocalDate next = fromDate.plusDays(1);
        while (!isWorkDay(next)) {
            next = next.plusDays(1);
        }
        return base.with(next).withHour(base.getHour()).withMinute(base.getMinute());
    }

    /**
     * 判断是否为工作日
     */
    private boolean isWorkDay(LocalDate date) {
        DayOfWeek dow = date.getDayOfWeek();
        if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) {
            return false;
        }
        return !holidayService.isHoliday(date);
    }

    /**
     * 计算自定义重复的下次执行时间
     * repeatRule JSON格式: {"interval": 1, "unit": "day", "daysOfWeek": [1,3,5]}
     */
    private LocalDateTime calculateCustomRepeat(LocalDateTime base, String repeatRule, LocalDate fromDate) {
        if (repeatRule == null || repeatRule.isEmpty()) {
            return calculateNextWorkday(base, fromDate);
        }

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> rule = objectMapper.readValue(repeatRule, Map.class);
            Integer interval = (Integer) rule.getOrDefault("interval", 1);
            String unit = (String) rule.getOrDefault("unit", "day");
            @SuppressWarnings("unchecked")
            List<Integer> daysOfWeek = (List<Integer>) rule.get("daysOfWeek");
            @SuppressWarnings("unchecked")
            List<Integer> daysOfMonth = (List<Integer>) rule.get("daysOfMonth");

            LocalDate next = fromDate;

            if ("day".equals(unit)) {
                next = fromDate.plusDays(interval);
            } else if ("week".equals(unit)) {
                next = fromDate.plusWeeks(interval);
                if (daysOfWeek != null && !daysOfWeek.isEmpty()) {
                    // 找到下一周中第一个匹配的星期
                    while (!daysOfWeek.contains(next.getDayOfWeek().getValue())) {
                        next = next.plusDays(1);
                    }
                }
            } else if ("month".equals(unit)) {
                next = fromDate.plusMonths(interval);
                if (daysOfMonth != null && !daysOfMonth.isEmpty()) {
                    next = next.withDayOfMonth(daysOfMonth.get(0));
                }
            }

            return base.with(next).withHour(base.getHour()).withMinute(base.getMinute());
        } catch (Exception e) {
            log.error("解析重复规则失败: {}", repeatRule, e);
            return calculateNextWorkday(base, fromDate);
        }
    }

    /**
     * 创建重复任务实例
     */
    private Task createRepeatInstance(Task source, LocalDate repeatDate) {
        Task newTask = new Task();
        newTask.setFamilyId(source.getFamilyId());
        newTask.setCategoryId(source.getCategoryId());
        newTask.setTitle(source.getTitle());
        newTask.setContent(source.getContent());
        newTask.setRemark(source.getRemark());
        newTask.setPriority(source.getPriority());
        newTask.setStatus(0); // 新任务状态为待办
        newTask.setAssigneeId(source.getAssigneeId());
        newTask.setCreatorId(source.getCreatorId());
        newTask.setDueTime(repeatDate.atTime(source.getDueTime() != null ? source.getDueTime().toLocalTime() : LocalTime.of(9, 0)));
        newTask.setRepeatType(source.getRepeatType());
        newTask.setRepeatRule(source.getRepeatRule());
        newTask.setRepeatStartDate(source.getRepeatStartDate());
        newTask.setRepeatEndDate(source.getRepeatEndDate());
        newTask.setCreateTime(LocalDateTime.now());
        newTask.setUpdateTime(LocalDateTime.now());
        newTask.setIsArchived(0);
        newTask.setIsDeleted(0);
        newTask.setSortOrder(source.getSortOrder());
        return newTask;
    }

    /**
     * 转换重复类型字符串到整数
     */
    private Integer convertRepeatTypeStringToInt(String repeatType) {
        if (repeatType == null) return 0;
        switch (repeatType.toLowerCase()) {
            case "none": case "0": return 0;
            case "daily": case "1": return 1;
            case "weekly": case "2": return 2;
            case "monthly": case "3": return 3;
            case "yearly": case "4": return 4;
            case "workday": case "5": return 5;
            case "custom": case "6": return 6;
            default: return 0;
        }
    }

    /**
     * 转换重复类型整数到字符串
     */
    private String convertRepeatTypeIntToString(Integer repeatType) {
        if (repeatType == null) return "none";
        switch (repeatType) {
            case 0: return "none";
            case 1: return "daily";
            case 2: return "weekly";
            case 3: return "monthly";
            case 4: return "yearly";
            case 5: return "workday";
            case 6: return "custom";
            default: return "none";
        }
    }
}
