package com.family.family.controller;
import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;import com.family.common.core.Result;
import com.family.family.entity.Task;
import com.family.family.mapper.TaskMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 任务统计控制器
 * 完成任务完成率、逾期率、趋势分析等统计功能
 */
@RestController
@RequestMapping("/api/task")
@SaCheckLogin
@RequiredArgsConstructor
public class TaskStatsController {
    
    private final TaskMapper taskMapper;
    
    /**
     * 获取家庭任务统计
     * GET /api/task/stats/overview
     */
    @GetMapping("/stats/overview")
    public Result<Map<String, Object>> getOverviewStats(@RequestParam Long familyId) {
        Long userId = StpUtil.getLoginIdAsLong();
        
        // 查询家庭所有任务
        List<Task> allTasks = taskMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Task>()
                .eq(Task::getFamilyId, familyId)
        );
        
        Map<String, Object> stats = new HashMap<>();
        
        // 基本统计
        int total = allTasks.size();
        int pending = (int) allTasks.stream().filter(t -> t.getStatus() == 0).count();
        int inProgress = (int) allTasks.stream().filter(t -> t.getStatus() == 1).count();
        int completed = (int) allTasks.stream().filter(t -> t.getStatus() == 2).count();
        int overdue = (int) allTasks.stream().filter(this::isOverdue).count();
        
        stats.put("total", total);
        stats.put("pending", pending);
        stats.put("inProgress", inProgress);
        stats.put("completed", completed);
        stats.put("overdue", overdue);
        
        // 完成率
        double completionRate = total > 0 ? (double) completed / total * 100 : 0;
        stats.put("completionRate", Math.round(completionRate * 100) / 100.0);
        
        // 逾期率
        double overdueRate = total > 0 ? (double) overdue / total * 100 : 0;
        stats.put("overdueRate", Math.round(overdueRate * 100) / 100.0);
        
        // 今日统计
        LocalDate today = LocalDate.now();
        long todayCreated = allTasks.stream()
            .filter(t -> t.getCreateTime() != null && t.getCreateTime().toLocalDate().equals(today))
            .count();
        long todayCompleted = allTasks.stream()
            .filter(t -> t.getStatus() == 2 && t.getFinishTime() != null && t.getFinishTime().toLocalDate().equals(today))
            .count();
        stats.put("todayCreated", todayCreated);
        stats.put("todayCompleted", todayCompleted);
        
        return Result.success(stats);
    }
    
    /**
     * 获取任务趋势分析
     * GET /api/task/stats/trend
     */
    @GetMapping("/stats/trend")
    public Result<List<Map<String, Object>>> getTrendStats(
            @RequestParam Long familyId,
            @RequestParam(defaultValue = "7") int days) {
        
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);
        
        List<Task> allTasks = taskMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Task>()
                .eq(Task::getFamilyId, familyId)
        );
        
        List<Map<String, Object>> trend = new ArrayList<>();
        
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            final LocalDate currentDate = date;
            
            long created = allTasks.stream()
                .filter(t -> t.getCreateTime() != null && t.getCreateTime().toLocalDate().equals(currentDate))
                .count();
            long completed = allTasks.stream()
                .filter(t -> t.getStatus() == 2 && t.getFinishTime() != null && t.getFinishTime().toLocalDate().equals(currentDate))
                .count();
            
            Map<String, Object> dayStats = new HashMap<>();
            dayStats.put("date", currentDate.toString());
            dayStats.put("created", created);
            dayStats.put("completed", completed);
            trend.add(dayStats);
        }
        
        return Result.success(trend);
    }
    
    /**
     * 获取成员任务统计
     * GET /api/task/stats/by-member
     */
    @GetMapping("/stats/by-member")
    public Result<List<Map<String, Object>>> getMemberStats(@RequestParam Long familyId) {
        List<Task> allTasks = taskMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Task>()
                .eq(Task::getFamilyId, familyId)
        );
        
        Map<Long, List<Task>> tasksByAssignee = allTasks.stream()
            .filter(t -> t.getAssigneeId() != null)
            .collect(Collectors.groupingBy(Task::getAssigneeId));
        
        List<Map<String, Object>> memberStats = new ArrayList<>();
        
        tasksByAssignee.forEach((memberId, tasks) -> {
            Map<String, Object> member = new HashMap<>();
            member.put("memberId", memberId);
            member.put("total", tasks.size());
            member.put("completed", tasks.stream().filter(t -> t.getStatus() == 2).count());
            member.put("pending", tasks.stream().filter(t -> t.getStatus() == 0).count());
            member.put("overdue", tasks.stream().filter(this::isOverdue).count());
            
            double rate = tasks.size() > 0 ? 
                (double) tasks.stream().filter(t -> t.getStatus() == 2).count() / tasks.size() * 100 : 0;
            member.put("completionRate", Math.round(rate * 100) / 100.0);
            
            memberStats.add(member);
        });
        
        // 按完成率排序
        memberStats.sort((a, b) -> 
            Double.compare((Double) b.get("completionRate"), (Double) a.get("completionRate")));
        
        return Result.success(memberStats);
    }
    
    /**
     * 获取优先级分布统计
     * GET /api/task/stats/by-priority
     */
    @GetMapping("/stats/by-priority")
    public Result<Map<String, Object>> getPriorityStats(@RequestParam Long familyId) {
        List<Task> allTasks = taskMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Task>()
                .eq(Task::getFamilyId, familyId)
        );
        
        Map<String, Object> stats = new HashMap<>();
        
        Map<Integer, List<Task>> byPriority = allTasks.stream()
            .filter(t -> t.getPriority() != null)
            .collect(Collectors.groupingBy(Task::getPriority));
        
        stats.put("high", byPriority.getOrDefault(3, new ArrayList<>()).size());
        stats.put("medium", byPriority.getOrDefault(2, new ArrayList<>()).size());
        stats.put("low", byPriority.getOrDefault(1, new ArrayList<>()).size());
        stats.put("none", byPriority.getOrDefault(0, new ArrayList<>()).size());
        
        return Result.success(stats);
    }
    
    /**
     * 获取逾期任务列表
     * GET /api/task/stats/overdue-list
     */
    @GetMapping("/stats/overdue-list")
    public Result<List<Task>> getOverdueTasks(@RequestParam Long familyId) {
        List<Task> allTasks = taskMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Task>()
                .eq(Task::getFamilyId, familyId)
                .ne(Task::getStatus, 2) // 未完成
        );
        
        List<Task> overdueTasks = allTasks.stream()
            .filter(this::isOverdue)
            .sorted(Comparator.comparing(Task::getDueTime))
            .collect(Collectors.toList());
        
        return Result.success(overdueTasks);
    }
    
    /**
     * 判断任务是否逾期
     */
    private boolean isOverdue(Task task) {
        if (task.getStatus() == 2) return false; // 已完成不算逾期
        if (task.getDueTime() == null) return false;
        return task.getDueTime().isBefore(LocalDateTime.now());
    }
}
