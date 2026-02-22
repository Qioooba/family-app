package com.family.family.controller;
import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;import com.family.common.core.Result;
import com.family.family.entity.Task;
import com.family.family.mapper.TaskMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 任务归档与回收站控制器
 */
@RestController
@RequestMapping("/api/task")
@SaCheckLogin
@RequiredArgsConstructor
public class TaskArchiveController {
    
    private final TaskMapper taskMapper;
    
    /**
     * 归档任务
     * POST /api/task/{id}/archive
     */
    @PostMapping("/{id}/archive")
    public Result<Boolean> archiveTask(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        Task task = taskMapper.selectById(id);
        
        if (task == null) {
            return Result.error("任务不存在");
        }
        
        if (!task.getCreatorId().equals(userId)) {
            return Result.error(403, "无权归档此任务");
        }
        
        task.setIsArchived(1);
        task.setArchiveTime(LocalDateTime.now());
        task.setUpdateTime(LocalDateTime.now());
        taskMapper.updateById(task);
        
        return Result.success(true);
    }
    
    /**
     * 恢复归档任务
     * POST /api/task/{id}/unarchive
     */
    @PostMapping("/{id}/unarchive")
    public Result<Boolean> unarchiveTask(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        Task task = taskMapper.selectById(id);
        
        if (task == null) {
            return Result.error("任务不存在");
        }
        
        if (!task.getCreatorId().equals(userId)) {
            return Result.error(403, "无权恢复此任务");
        }
        
        task.setIsArchived(0);
        task.setArchiveTime(null);
        task.setUpdateTime(LocalDateTime.now());
        taskMapper.updateById(task);
        
        return Result.success(true);
    }
    
    /**
     * 获取归档任务列表
     * GET /api/task/archived
     */
    @GetMapping("/archived")
    public Result<List<Task>> getArchivedTasks(@RequestParam Long familyId) {
        List<Task> tasks = taskMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Task>()
                .eq(Task::getFamilyId, familyId)
                .eq(Task::getIsArchived, 1)
                .eq(Task::getIsDeleted, 0)
                .orderByDesc(Task::getArchiveTime)
        );
        return Result.success(tasks);
    }
    
    /**
     * 软删除任务（放入回收站）
     * POST /api/task/{id}/trash
     */
    @PostMapping("/{id}/trash")
    public Result<Boolean> trashTask(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        Task task = taskMapper.selectById(id);
        
        if (task == null) {
            return Result.error("任务不存在");
        }
        
        if (!task.getCreatorId().equals(userId)) {
            return Result.error(403, "无权删除此任务");
        }
        
        task.setIsDeleted(1);
        task.setDeleteTime(LocalDateTime.now());
        task.setUpdateTime(LocalDateTime.now());
        taskMapper.updateById(task);
        
        return Result.success(true);
    }
    
    /**
     * 恢复回收站任务
     * POST /api/task/{id}/restore
     */
    @PostMapping("/{id}/restore")
    public Result<Boolean> restoreTask(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        Task task = taskMapper.selectById(id);
        
        if (task == null) {
            return Result.error("任务不存在");
        }
        
        if (!task.getCreatorId().equals(userId)) {
            return Result.error(403, "无权恢复此任务");
        }
        
        task.setIsDeleted(0);
        task.setDeleteTime(null);
        task.setUpdateTime(LocalDateTime.now());
        taskMapper.updateById(task);
        
        return Result.success(true);
    }
    
    /**
     * 彻底删除任务
     * DELETE /api/task/{id}/permanent
     */
    @DeleteMapping("/{id}/permanent")
    @Transactional
    public Result<Boolean> permanentDeleteTask(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        Task task = taskMapper.selectById(id);
        
        if (task == null) {
            return Result.error("任务不存在");
        }
        
        if (!task.getCreatorId().equals(userId)) {
            return Result.error(403, "无权删除此任务");
        }
        
        // 彻底删除
        taskMapper.deleteById(id);
        
        return Result.success(true);
    }
    
    /**
     * 获取回收站任务列表
     * GET /api/task/trash
     */
    @GetMapping("/trash")
    public Result<List<Task>> getTrashTasks(@RequestParam Long familyId) {
        List<Task> tasks = taskMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Task>()
                .eq(Task::getFamilyId, familyId)
                .eq(Task::getIsDeleted, 1)
                .orderByDesc(Task::getDeleteTime)
        );
        return Result.success(tasks);
    }
    
    /**
     * 清空回收站
     * POST /api/task/trash/clear
     */
    @PostMapping("/trash/clear")
    @Transactional
    public Result<Integer> clearTrash(@RequestParam Long familyId) {
        Long userId = StpUtil.getLoginIdAsLong();
        
        // 查询回收站中该用户创建的任务
        List<Task> trashTasks = taskMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Task>()
                .eq(Task::getFamilyId, familyId)
                .eq(Task::getIsDeleted, 1)
                .eq(Task::getCreatorId, userId)
        );
        
        int count = 0;
        for (Task task : trashTasks) {
            taskMapper.deleteById(task.getId());
            count++;
        }
        
        return Result.success(count);
    }
}
