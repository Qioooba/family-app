package com.family.family.controller;

import com.family.common.core.Result;
import com.family.family.entity.Task;
import com.family.family.mapper.TaskMapper;
import com.family.family.service.TaskRepeatService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 任务控制器
 */
@RestController
@RequestMapping("/api/task")
public class TaskController {
    
    private final TaskMapper taskMapper;
    private final TaskRepeatService taskRepeatService;
    
    public TaskController(TaskMapper taskMapper, TaskRepeatService taskRepeatService) {
        this.taskMapper = taskMapper;
        this.taskRepeatService = taskRepeatService;
    }
    
    /**
     * 获取家庭任务列表
     */
    @GetMapping("/list")
    public Result<List<Task>> list(@RequestParam Long familyId,
                                    @RequestParam(required = false) Long categoryId,
                                    @RequestParam(required = false) Integer status) {
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
}
