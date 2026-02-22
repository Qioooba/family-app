package com.family.family.controller;
import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;import com.family.common.core.Result;
import com.family.family.entity.Task;
import com.family.family.mapper.TaskMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 任务排序控制器
 */
@RestController
@RequestMapping("/api/task")
@SaCheckLogin
@RequiredArgsConstructor
public class TaskSortController {
    
    private final TaskMapper taskMapper;
    
    /**
     * 设置任务自定义排序
     * POST /api/task/sort
     */
    @PostMapping("/sort")
    public Result<Boolean> setTaskSort(@RequestBody SortRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        
        // 批量更新任务排序
        for (int i = 0; i < request.getTaskIds().size(); i++) {
            Long taskId = request.getTaskIds().get(i);
            Task task = taskMapper.selectById(taskId);
            if (task != null) {
                task.setSortOrder(i);
                taskMapper.updateById(task);
            }
        }
        
        return Result.success(true);
    }
    
    /**
     * 获取排序后的任务列表
     * GET /api/task/sorted
     */
    @GetMapping("/sorted")
    public Result<List<Task>> getSortedTasks(
            @RequestParam Long familyId,
            @RequestParam(defaultValue = "createTime") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder) {
        
        var wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Task>()
            .eq(Task::getFamilyId, familyId);
        
        // 动态排序
        if ("createTime".equals(sortBy)) {
            wrapper = "asc".equals(sortOrder) ? 
                wrapper.orderByAsc(Task::getCreateTime) : 
                wrapper.orderByDesc(Task::getCreateTime);
        } else if ("dueTime".equals(sortBy)) {
            wrapper = "asc".equals(sortOrder) ? 
                wrapper.orderByAsc(Task::getDueTime) : 
                wrapper.orderByDesc(Task::getDueTime);
        } else if ("priority".equals(sortBy)) {
            wrapper = "asc".equals(sortOrder) ? 
                wrapper.orderByAsc(Task::getPriority) : 
                wrapper.orderByDesc(Task::getPriority);
        } else if ("sortOrder".equals(sortBy)) {
            wrapper = "asc".equals(sortOrder) ? 
                wrapper.orderByAsc(Task::getSortOrder) : 
                wrapper.orderByDesc(Task::getSortOrder);
        }
        
        return Result.success(taskMapper.selectList(wrapper));
    }
    
    /**
     * 排序请求
     */
    public static class SortRequest {
        private List<Long> taskIds; // 按顺序排列的任务ID列表
        
        public List<Long> getTaskIds() { return taskIds; }
        public void setTaskIds(List<Long> taskIds) { this.taskIds = taskIds; }
    }
}
