package com.family.family.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.family.common.core.PageResult;
import com.family.common.core.Result;
import com.family.family.entity.Task;
import com.family.family.mapper.TaskMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 任务筛选与搜索控制器
 */
@RestController
@RequestMapping("/api/task")
@SaCheckLogin
@RequiredArgsConstructor
public class TaskFilterController {
    
    private final TaskMapper taskMapper;
    
    /**
     * 高级筛选任务
     * GET /api/task/filter
     */
    @GetMapping("/filter")
    public Result<List<Task>> filterTasks(
            @RequestParam Long familyId,
            @RequestParam(required = false) List<Integer> statuses,
            @RequestParam(required = false) List<Integer> priorities,
            @RequestParam(required = false) Long assigneeId,
            @RequestParam(required = false) Long creatorId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDateTo,
            @RequestParam(required = false) Boolean overdue,
            @RequestParam(required = false) String keyword) {
        
        var wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Task>()
            .eq(Task::getFamilyId, familyId);
        
        // 状态筛选
        if (statuses != null && !statuses.isEmpty()) {
            if (statuses.size() == 1) {
                wrapper.eq(Task::getStatus, statuses.get(0));
            } else {
                wrapper.in(Task::getStatus, statuses);
            }
        }
        
        // 优先级筛选
        if (priorities != null && !priorities.isEmpty()) {
            wrapper.in(Task::getPriority, priorities);
        }
        
        // 负责人筛选
        if (assigneeId != null) {
            wrapper.eq(Task::getAssigneeId, assigneeId);
        }
        
        // 创建人筛选
        if (creatorId != null) {
            wrapper.eq(Task::getCreatorId, creatorId);
        }
        
        // 分类筛选
        if (categoryId != null) {
            wrapper.eq(Task::getCategoryId, categoryId);
        }
        
        // 截止日期范围
        if (dueDateFrom != null) {
            wrapper.ge(Task::getDueTime, dueDateFrom.atStartOfDay());
        }
        if (dueDateTo != null) {
            wrapper.le(Task::getDueTime, dueDateTo.atTime(23, 59, 59));
        }
        
        // 关键词搜索
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(Task::getTitle, keyword).or().like(Task::getContent, keyword));
        }
        
        List<Task> tasks = taskMapper.selectList(wrapper);
        
        // 逾期筛选
        if (overdue != null) {
            tasks = tasks.stream()
                .filter(t -> isOverdue(t) == overdue)
                .collect(Collectors.toList());
        }
        
        return Result.success(tasks);
    }
    
    /**
     * 全文搜索任务
     * GET /api/task/search
     */
    @GetMapping("/search")
    public Result<PageResult<Task>> searchTasks(
            @RequestParam Long familyId,
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Task> pageParam = 
            new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, size);
        
        var wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Task>()
            .eq(Task::getFamilyId, familyId)
            .and(w -> w.like(Task::getTitle, keyword)
                .or()
                .like(Task::getContent, keyword));
        
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Task> result = 
            taskMapper.selectPage(pageParam, wrapper);
        
        return Result.success(PageResult.of(result.getRecords(), result.getTotal(), page, size));
    }
    
    /**
     * 获取我的任务列表
     * GET /api/task/my-tasks
     */
    @GetMapping("/my-tasks")
    public Result<List<Task>> getMyTasks(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Boolean overdueOnly) {
        
        Long userId = StpUtil.getLoginIdAsLong();
        
        var wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Task>()
            .and(w -> w.eq(Task::getAssigneeId, userId).or().eq(Task::getCreatorId, userId));
        
        if (status != null) {
            wrapper.eq(Task::getStatus, status);
        }
        
        List<Task> tasks = taskMapper.selectList(wrapper);
        
        if (overdueOnly != null && overdueOnly) {
            tasks = tasks.stream()
                .filter(this::isOverdue)
                .collect(Collectors.toList());
        }
        
        return Result.success(tasks);
    }
    
    /**
     * 获取今日任务
     * GET /api/task/today
     */
    @GetMapping("/today")
    public Result<List<Task>> getTodayTasks(@RequestParam Long familyId) {
        Long userId = StpUtil.getLoginIdAsLong();
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(23, 59, 59);
        
        List<Task> tasks = taskMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Task>()
                .eq(Task::getFamilyId, familyId)
                .and(w -> w.eq(Task::getAssigneeId, userId).or().eq(Task::getCreatorId, userId))
                .and(w -> w.between(Task::getDueTime, startOfDay, endOfDay)
                    .or()
                    .eq(Task::getStatus, 0))
                .orderByAsc(Task::getDueTime)
        );
        
        return Result.success(tasks);
    }
    
    /**
     * 获取即将到期的任务
     * GET /api/task/upcoming
     */
    @GetMapping("/upcoming")
    public Result<List<Task>> getUpcomingTasks(
            @RequestParam Long familyId,
            @RequestParam(defaultValue = "3") int days) {
        
        Long userId = StpUtil.getLoginIdAsLong();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime future = now.plusDays(days);
        
        List<Task> tasks = taskMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Task>()
                .eq(Task::getFamilyId, familyId)
                .and(w -> w.eq(Task::getAssigneeId, userId).or().eq(Task::getCreatorId, userId))
                .between(Task::getDueTime, now, future)
                .ne(Task::getStatus, 2) // 未完成
                .orderByAsc(Task::getDueTime)
        );
        
        return Result.success(tasks);
    }
    
    /**
     * 判断任务是否逾期
     */
    private boolean isOverdue(Task task) {
        if (task.getStatus() == 2) return false;
        if (task.getDueTime() == null) return false;
        return task.getDueTime().isBefore(LocalDateTime.now());
    }
}
