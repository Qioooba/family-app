package com.family.family.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.family.common.core.Result;
import com.family.family.entity.TaskSubtask;
import com.family.family.mapper.TaskSubtaskMapper;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/task/subtask")
public class SubtaskController {
    
    private final TaskSubtaskMapper subtaskMapper;
    
    public SubtaskController(TaskSubtaskMapper subtaskMapper) {
        this.subtaskMapper = subtaskMapper;
    }
    
    @PostMapping("/add")
    public Result<Long> addSubtask(@RequestBody AddSubtaskRequest request) {
        TaskSubtask subtask = new TaskSubtask();
        subtask.setTaskId(request.getTaskId());
        subtask.setTitle(request.getTitle());
        subtask.setStatus(0);
        subtask.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        
        subtaskMapper.insert(subtask);
        return Result.success(subtask.getId());
    }
    
    @GetMapping("/list/{taskId}")
    public Result<List<TaskSubtask>> getSubtasks(@PathVariable Long taskId) {
        List<TaskSubtask> list = subtaskMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TaskSubtask>()
                .eq(TaskSubtask::getTaskId, taskId)
                .orderByAsc(TaskSubtask::getSortOrder)
        );
        return Result.success(list);
    }
    
    @PutMapping("/{id}/toggle")
    public Result<Boolean> toggleSubtask(@PathVariable Long id) {
        TaskSubtask subtask = subtaskMapper.selectById(id);
        if (subtask == null) {
            return Result.error("子任务不存在");
        }
        subtask.setStatus(subtask.getStatus() == 0 ? 1 : 0);
        subtaskMapper.updateById(subtask);
        return Result.success(true);
    }
    
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteSubtask(@PathVariable Long id) {
        subtaskMapper.deleteById(id);
        return Result.success(true);
    }
    
    public static class AddSubtaskRequest {
        private Long taskId;
        private String title;
        private Integer sortOrder;
        
        public Long getTaskId() { return taskId; }
        public void setTaskId(Long taskId) { this.taskId = taskId; }
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public Integer getSortOrder() { return sortOrder; }
        public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    }
}
