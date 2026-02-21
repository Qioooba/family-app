package com.family.family.controller;

import com.family.common.core.Result;
import com.family.family.dto.TaskReminderDTO;
import com.family.family.entity.Task;
import com.family.family.mapper.TaskMapper;
import com.family.family.service.TaskReminderService;
import com.family.family.service.TaskRepeatService;
import com.family.family.vo.TaskReminderVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 任务控制器
 */
@RestController
@RequestMapping("/api/task")
@Tag(name = "任务管理", description = "任务清单相关接口")
public class TaskController {
    
    private final TaskMapper taskMapper;
    private final TaskRepeatService taskRepeatService;
    private final TaskReminderService taskReminderService;
    
    public TaskController(TaskMapper taskMapper, TaskRepeatService taskRepeatService, TaskReminderService taskReminderService) {
        this.taskMapper = taskMapper;
        this.taskRepeatService = taskRepeatService;
        this.taskReminderService = taskReminderService;
    }
    
    /**
     * 获取家庭任务列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取任务列表", description = "根据家庭ID获取任务列表")
    public Result<List<Task>> list(
            @Parameter(description = "家庭ID") @RequestParam Long familyId,
            @Parameter(description = "分类ID") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "状态：0待办 1进行中 2已完成") @RequestParam(required = false) Integer status) {
        List<Task> tasks = taskMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Task>()
                .eq(Task::getFamilyId, familyId)
                .eq(categoryId != null, Task::getCategoryId, categoryId)
                .eq(status != null, Task::getStatus, status)
                .orderByDesc(Task::getPriority)
                .orderByAsc(Task::getDueTime)
        );
        return Result.success(tasks);
    }
    
    /**
     * 创建任务
     */
    @PostMapping("/create")
    public Result<Task> create(@RequestBody Task task) {
        task.setStatus(0);
        taskMapper.insert(task);
        return Result.success(task);
    }
    
    /**
     * 更新任务
     */
    @PutMapping("/update")
    public Result<Task> update(@RequestBody Task task) {
        taskMapper.updateById(task);
        return Result.success(task);
    }
    
    /**
     * 完成任务
     */
    @PostMapping("/complete/{id}")
    public Result<Void> complete(@PathVariable Long id) {
        Task task = taskMapper.selectById(id);
        if (task == null) {
            return Result.error("任务不存在");
        }
        task.setStatus(2);
        task.setFinishTime(java.time.LocalDateTime.now());
        taskMapper.updateById(task);
        return Result.success();
    }
    
    /**
     * 删除任务
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        taskMapper.deleteById(id);
        return Result.success();
    }
    
    /**
     * 设置任务重复规则
     * POST /api/task/{id}/repeat
     */
    @PostMapping("/{id}/repeat")
    public Result<Void> setRepeatRule(@PathVariable Long id, @RequestBody RepeatRuleRequest request) {
        taskRepeatService.setRepeatRule(id, request.getRepeatType(), request.getRepeatRule());
        return Result.success();
    }
    
    /**
     * 获取任务重复规则
     */
    @GetMapping("/{id}/repeat")
    public Result<RepeatRuleResponse> getRepeatRule(@PathVariable Long id) {
        return Result.success(taskRepeatService.getRepeatRule(id));
    }
    
    /**
     * 获取今日任务
     */
    @GetMapping("/today/{familyId}")
    public Result<List<Task>> getTodayTasks(@PathVariable Long familyId) {
        java.time.LocalDateTime start = java.time.LocalDateTime.of(
            java.time.LocalDate.now(), java.time.LocalTime.MIN);
        java.time.LocalDateTime end = java.time.LocalDateTime.of(
            java.time.LocalDate.now(), java.time.LocalTime.MAX);
        
        List<Task> tasks = taskMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Task>()
                .eq(Task::getFamilyId, familyId)
                .between(Task::getDueTime, start, end)
                .ne(Task::getStatus, 2)
                .orderByDesc(Task::getPriority)
        );
        return Result.success(tasks);
    }
    
    /**
     * 重复规则请求
     */
    public static class RepeatRuleRequest {
        private String repeatType;  // none/daily/weekly/monthly/yearly/custom
        private String repeatRule;  // JSON格式规则
        
        public String getRepeatType() { return repeatType; }
        public void setRepeatType(String repeatType) { this.repeatType = repeatType; }
        
        public String getRepeatRule() { return repeatRule; }
        public void setRepeatRule(String repeatRule) { this.repeatRule = repeatRule; }
    }
    
    /**
     * 重复规则响应
     */
    public static class RepeatRuleResponse {
        private String repeatType;
        private String repeatRule;
        
        public RepeatRuleResponse(String repeatType, String repeatRule) {
            this.repeatType = repeatType;
            this.repeatRule = repeatRule;
        }
        
        public String getRepeatType() { return repeatType; }
        public void setRepeatType(String repeatType) { this.repeatType = repeatType; }
        
        public String getRepeatRule() { return repeatRule; }
        public void setRepeatRule(String repeatRule) { this.repeatRule = repeatRule; }
    }
    
    /**
     * 设置任务提醒
     * POST /api/task/{id}/reminder
     */
    @PostMapping("/{id}/reminder")
    public Result<Void> setReminder(@PathVariable Long id, @RequestBody TaskReminderDTO dto) {
        taskReminderService.setReminder(id, dto);
        return Result.success();
    }
    
    /**
     * 获取任务提醒列表
     * GET /api/task/{id}/reminders
     */
    @GetMapping("/{id}/reminders")
    public Result<List<TaskReminderVO>> getReminders(@PathVariable Long id) {
        return Result.success(taskReminderService.getReminders(id));
    }
    
    /**
     * 删除任务提醒
     * DELETE /api/task/reminder/{reminderId}
     */
    @DeleteMapping("/reminder/{reminderId}")
    public Result<Void> deleteReminder(@PathVariable Long reminderId) {
        taskReminderService.deleteReminder(reminderId);
        return Result.success();
    }
    
    /**
     * 获取用户待处理的时间提醒
     * GET /api/task/reminders/pending
     */
    @GetMapping("/reminders/pending")
    public Result<List<TaskReminderVO>> getPendingReminders(@RequestParam Long userId) {
        return Result.success(taskReminderService.getPendingTimeReminders(userId));
    }
    
    /**
     * 上报位置（触发位置提醒）
     * POST /api/task/location/report
     */
    @PostMapping("/location/report")
    public Result<Void> reportLocation(@RequestParam Long userId,
                                        @RequestParam Double latitude,
                                        @RequestParam Double longitude) {
        taskReminderService.processLocationReminders(userId, latitude, longitude);
        return Result.success();
    }
}
