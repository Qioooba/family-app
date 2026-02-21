package com.family.family.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.family.common.core.Result;
import com.family.family.entity.Task;
import com.family.family.entity.TaskTemplate;
import com.family.family.mapper.TaskMapper;
import com.family.family.mapper.TaskTemplateMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 任务模板控制器
 */
@RestController
@RequestMapping("/api/task")
@SaCheckLogin
@RequiredArgsConstructor
public class TaskTemplateController {
    
    private final TaskTemplateMapper templateMapper;
    private final TaskMapper taskMapper;
    
    /**
     * 创建任务模板
     * POST /api/task/templates
     */
    @PostMapping("/templates")
    public Result<Long> createTemplate(@RequestBody TaskTemplate template) {
        Long userId = StpUtil.getLoginIdAsLong();
        template.setCreatorId(userId);
        template.setUsageCount(0);
        template.setCreateTime(LocalDateTime.now());
        template.setUpdateTime(LocalDateTime.now());
        templateMapper.insert(template);
        return Result.success(template.getId());
    }
    
    /**
     * 获取任务模板列表
     * GET /api/task/templates
     */
    @GetMapping("/templates")
    public Result<List<TaskTemplate>> getTemplates(
            @RequestParam Long familyId,
            @RequestParam(required = false) Boolean publicOnly) {
        
        var wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TaskTemplate>()
            .and(w -> w.eq(TaskTemplate::getFamilyId, familyId)
                .or()
                .eq(TaskTemplate::getIsPublic, 1))
            .orderByDesc(TaskTemplate::getUsageCount);
        
        if (publicOnly != null && publicOnly) {
            wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TaskTemplate>()
                .eq(TaskTemplate::getIsPublic, 1)
                .orderByDesc(TaskTemplate::getUsageCount);
        }
        
        return Result.success(templateMapper.selectList(wrapper));
    }
    
    /**
     * 获取模板详情
     * GET /api/task/templates/{id}
     */
    @GetMapping("/templates/{id}")
    public Result<TaskTemplate> getTemplate(@PathVariable Long id) {
        return Result.success(templateMapper.selectById(id));
    }
    
    /**
     * 更新模板
     * PUT /api/task/templates/{id}
     */
    @PutMapping("/templates/{id}")
    public Result<Boolean> updateTemplate(@PathVariable Long id, @RequestBody TaskTemplate template) {
        template.setId(id);
        template.setUpdateTime(LocalDateTime.now());
        templateMapper.updateById(template);
        return Result.success(true);
    }
    
    /**
     * 删除模板
     * DELETE /api/task/templates/{id}
     */
    @DeleteMapping("/templates/{id}")
    public Result<Boolean> deleteTemplate(@PathVariable Long id) {
        templateMapper.deleteById(id);
        return Result.success(true);
    }
    
    /**
     * 应用模板创建任务
     * POST /api/task/templates/{id}/apply
     */
    @PostMapping("/templates/{id}/apply")
    @Transactional
    public Result<Long> applyTemplate(@PathVariable Long id, @RequestBody ApplyRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        
        TaskTemplate template = templateMapper.selectById(id);
        if (template == null) {
            return Result.error("模板不存在");
        }
        
        // 创建任务
        Task task = new Task();
        task.setFamilyId(request.getFamilyId());
        task.setTitle(template.getTitle());
        task.setContent(template.getContent());
        task.setCategoryId(template.getCategoryId());
        task.setPriority(template.getPriority());
        task.setRepeatType(template.getRepeatType());
        task.setRepeatRule(template.getRepeatRule());
        task.setCreatorId(userId);
        task.setStatus(0);
        task.setCreateTime(LocalDateTime.now());
        task.setUpdateTime(LocalDateTime.now());
        
        taskMapper.insert(task);
        
        // 更新模板使用次数
        template.setUsageCount(template.getUsageCount() + 1);
        templateMapper.updateById(template);
        
        return Result.success(task.getId());
    }
    
    /**
     * 从任务创建模板
     * POST /api/task/{taskId}/create-template
     */
    @PostMapping("/{taskId}/create-template")
    public Result<Long> createTemplateFromTask(@PathVariable Long taskId, @RequestParam String name) {
        Long userId = StpUtil.getLoginIdAsLong();
        
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            return Result.error("任务不存在");
        }
        
        TaskTemplate template = new TaskTemplate();
        template.setFamilyId(task.getFamilyId());
        template.setCreatorId(userId);
        template.setName(name);
        template.setTitle(task.getTitle());
        template.setContent(task.getContent());
        template.setCategoryId(task.getCategoryId());
        template.setPriority(task.getPriority());
        template.setRepeatType(task.getRepeatType());
        template.setRepeatRule(task.getRepeatRule());
        template.setUsageCount(0);
        template.setCreateTime(LocalDateTime.now());
        template.setUpdateTime(LocalDateTime.now());
        
        templateMapper.insert(template);
        
        return Result.success(template.getId());
    }
    
    /**
     * 应用模板请求
     */
    public static class ApplyRequest {
        private Long familyId;
        private Long assigneeId;
        private LocalDateTime dueTime;
        
        public Long getFamilyId() {
            return familyId;
        }
        
        public void setFamilyId(Long familyId) {
            this.familyId = familyId;
        }
        
        public Long getAssigneeId() {
            return assigneeId;
        }
        
        public void setAssigneeId(Long assigneeId) {
            this.assigneeId = assigneeId;
        }
        
        public LocalDateTime getDueTime() {
            return dueTime;
        }
        
        public void setDueTime(LocalDateTime dueTime) {
            this.dueTime = dueTime;
        }
    }
}
