package com.family.family.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.family.common.core.Result;
import com.family.family.entity.Task;
import com.family.family.mapper.TaskMapper;
import com.family.family.service.TaskRepeatService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 任务控制器
 */
@RestController
@RequestMapping("/api/task")
@SaCheckLogin
public class TaskController {
    
    private final TaskMapper taskMapper;
    private final TaskRepeatService taskRepeatService;
    
    public TaskController(TaskMapper taskMapper, TaskRepeatService taskRepeatService) {
        this.taskMapper = taskMapper;
        this.taskRepeatService = taskRepeatService;
    }
    
    /**
     * 设置任务重复规则
     * POST /api/task/{id}/repeat
     */
    @PostMapping("/{id}/repeat")
    public Result<Boolean> setRepeatRule(
            @PathVariable Long id,
            @RequestBody RepeatRuleRequest request) {
        // 手动验证
        if (request == null || !StringUtils.hasText(request.getRepeatType())) {
            return Result.error("重复类型不能为空");
        }
        
        // 验证任务是否存在且用户有权限
        Task task = taskMapper.selectById(id);
        if (task == null) {
            return Result.error("任务不存在");
        }
        
        Long userId = StpUtil.getLoginIdAsLong();
        if (!task.getCreatorId().equals(userId) && !task.getAssigneeId().equals(userId)) {
            return Result.error(403, "无权修改此任务");
        }
        
        taskRepeatService.setRepeatRule(id, request.getRepeatType(), request.getRepeatRule());
        return Result.success(true);
    }
    
    /**
     * 获取任务重复规则
     * GET /api/task/{id}/repeat
     */
    @GetMapping("/{id}/repeat")
    public Result<RepeatRuleResponse> getRepeatRule(@PathVariable Long id) {
        // 验证任务是否存在且用户有权限
        Task task = taskMapper.selectById(id);
        if (task == null) {
            return Result.error("任务不存在");
        }
        
        Long userId = StpUtil.getLoginIdAsLong();
        if (!task.getCreatorId().equals(userId) && !task.getAssigneeId().equals(userId)) {
            return Result.error(403, "无权访问此任务");
        }
        
        return Result.success(taskRepeatService.getRepeatRule(id));
    }
    
    /**
     * 手动触发生成重复任务（管理员接口）
     * POST /api/task/generate-repeating
     */
    @PostMapping("/generate-repeating")
    public Result<String> generateRepeatingTasks() {
        taskRepeatService.generateDailyRepeatingTasks();
        return Result.success("重复任务生成完成");
    }
    
    /**
     * 批量创建任务
     * POST /api/task/batch/create
     */
    @PostMapping("/batch/create")
    @Transactional
    public Result<List<Long>> batchCreateTasks(@RequestBody List<Task> tasks) {
        Long userId = StpUtil.getLoginIdAsLong();
        List<Long> ids = new ArrayList<>();
        
        for (Task task : tasks) {
            task.setCreatorId(userId);
            task.setStatus(0);
            task.setCreateTime(LocalDateTime.now());
            task.setUpdateTime(LocalDateTime.now());
            taskMapper.insert(task);
            ids.add(task.getId());
        }
        return Result.success(ids);
    }
    
    /**
     * 批量完成任务
     * POST /api/task/batch/complete
     */
    @PostMapping("/batch/complete")
    @Transactional
    public Result<Integer> batchCompleteTasks(@RequestBody List<Long> taskIds) {
        Long userId = StpUtil.getLoginIdAsLong();
        int count = 0;
        
        for (Long taskId : taskIds) {
            Task task = taskMapper.selectById(taskId);
            if (task != null && (task.getCreatorId().equals(userId) || task.getAssigneeId().equals(userId))) {
                task.setStatus(2); // 已完成
                task.setFinishTime(LocalDateTime.now());
                task.setUpdateTime(LocalDateTime.now());
                taskMapper.updateById(task);
                count++;
            }
        }
        return Result.success(count);
    }
    
    /**
     * 批量删除任务
     * POST /api/task/batch/delete
     */
    @PostMapping("/batch/delete")
    @Transactional
    public Result<Integer> batchDeleteTasks(@RequestBody List<Long> taskIds) {
        Long userId = StpUtil.getLoginIdAsLong();
        int count = 0;
        
        for (Long taskId : taskIds) {
            Task task = taskMapper.selectById(taskId);
            if (task != null && task.getCreatorId().equals(userId)) {
                taskMapper.deleteById(taskId);
                count++;
            }
        }
        return Result.success(count);
    }
    
    /**
     * 批量更新任务状态
     * POST /api/task/batch/update-status
     */
    @PostMapping("/batch/update-status")
    @Transactional
    public Result<Integer> batchUpdateStatus(@RequestBody BatchUpdateStatusRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        int count = 0;
        
        for (Long taskId : request.getTaskIds()) {
            Task task = taskMapper.selectById(taskId);
            if (task != null && (task.getCreatorId().equals(userId) || task.getAssigneeId().equals(userId))) {
                task.setStatus(request.getStatus());
                task.setUpdateTime(LocalDateTime.now());
                taskMapper.updateById(task);
                count++;
            }
        }
        return Result.success(count);
    }
    
    /**
     * 批量指派任务
     * POST /api/task/batch/assign
     */
    @PostMapping("/batch/assign")
    @Transactional
    public Result<Integer> batchAssignTasks(@RequestBody BatchAssignRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        int count = 0;
        
        for (Long taskId : request.getTaskIds()) {
            Task task = taskMapper.selectById(taskId);
            if (task != null && task.getCreatorId().equals(userId)) {
                task.setAssigneeId(request.getAssigneeId());
                task.setUpdateTime(LocalDateTime.now());
                taskMapper.updateById(task);
                count++;
            }
        }
        return Result.success(count);
    }
    
    /**
     * 重复规则请求DTO
     */
    public static class RepeatRuleRequest {
        private String repeatType; // none/daily/weekly/monthly/yearly/custom
        private String repeatRule; // JSON格式规则
        
        public String getRepeatType() {
            return repeatType;
        }
        
        public void setRepeatType(String repeatType) {
            this.repeatType = repeatType;
        }
        
        public String getRepeatRule() {
            return repeatRule;
        }
        
        public void setRepeatRule(String repeatRule) {
            this.repeatRule = repeatRule;
        }
    }
    
    /**
     * 重复规则响应DTO
     */
    public static class RepeatRuleResponse {
        private String repeatType;
        private String repeatRule;
        private Map<String, Object> ruleParsed;
        
        public RepeatRuleResponse() {
        }
        
        public RepeatRuleResponse(String repeatType, String repeatRule) {
            this.repeatType = repeatType;
            this.repeatRule = repeatRule;
        }
        
        public String getRepeatType() {
            return repeatType;
        }
        
        public void setRepeatType(String repeatType) {
            this.repeatType = repeatType;
        }
        
        public String getRepeatRule() {
            return repeatRule;
        }
        
        public void setRepeatRule(String repeatRule) {
            this.repeatRule = repeatRule;
        }
        
        public Map<String, Object> getRuleParsed() {
            return ruleParsed;
        }
        
        public void setRuleParsed(Map<String, Object> ruleParsed) {
            this.ruleParsed = ruleParsed;
        }
    }
    
    /**
     * 批量更新状态请求
     */
    public static class BatchUpdateStatusRequest {
        private List<Long> taskIds;
        private Integer status;
        
        public List<Long> getTaskIds() {
            return taskIds;
        }
        
        public void setTaskIds(List<Long> taskIds) {
            this.taskIds = taskIds;
        }
        
        public Integer getStatus() {
            return status;
        }
        
        public void setStatus(Integer status) {
            this.status = status;
        }
    }
    
    /**
     * 批量指派请求
     */
    public static class BatchAssignRequest {
        private List<Long> taskIds;
        private Long assigneeId;
        
        public List<Long> getTaskIds() {
            return taskIds;
        }
        
        public void setTaskIds(List<Long> taskIds) {
            this.taskIds = taskIds;
        }
        
        public Long getAssigneeId() {
            return assigneeId;
        }
        
        public void setAssigneeId(Long assigneeId) {
            this.assigneeId = assigneeId;
        }
    }
}
