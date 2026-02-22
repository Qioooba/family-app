package com.family.calendar.controller;
import cn.dev33.satoken.annotation.SaCheckLogin;
import com.family.common.core.Result;
import com.family.calendar.dto.request.ReminderRequest;
import com.family.calendar.dto.response.ReminderResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 提醒管理控制器
 * 用于管理各类提醒事项
 */
@RestController
@RequestMapping("/api/reminder")
@RequiredArgsConstructor
@SaCheckLogin
public class ReminderController {
    
    /**
     * 创建提醒
     * POST /api/reminder
     */
    @PostMapping
    public Result<ReminderResponse> create(@RequestBody ReminderRequest request) {
        // TODO: 实现创建提醒
        return Result.success(null);
    }
    
    /**
     * 更新提醒
     * PUT /api/reminder/{id}
     */
    @PutMapping("/{id}")
    public Result<ReminderResponse> update(@PathVariable Long id, @RequestBody ReminderRequest request) {
        // TODO: 实现更新提醒
        return Result.success(null);
    }
    
    /**
     * 删除提醒
     * DELETE /api/reminder/{id}
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        // TODO: 实现删除提醒
        return Result.success(null);
    }
    
    /**
     * 获取提醒详情
     * GET /api/reminder/{id}
     */
    @GetMapping("/{id}")
    public Result<ReminderResponse> getById(@PathVariable Long id) {
        // TODO: 实现获取提醒详情
        return Result.success(null);
    }
    
    /**
     * 获取用户提醒列表
     * GET /api/reminder/list/{userId}
     */
    @GetMapping("/list/{userId}")
    public Result<List<ReminderResponse>> getUserReminders(@PathVariable Long userId) {
        // TODO: 实现获取用户提醒列表
        return Result.success(null);
    }
    
    /**
     * 获取待处理提醒
     * GET /api/reminder/pending/{userId}
     */
    @GetMapping("/pending/{userId}")
    public Result<List<ReminderResponse>> getPendingReminders(@PathVariable Long userId) {
        // TODO: 实现获取待处理提醒
        return Result.success(null);
    }
    
    /**
     * 获取今日提醒
     * GET /api/reminder/today/{userId}
     */
    @GetMapping("/today/{userId}")
    public Result<List<ReminderResponse>> getTodayReminders(@PathVariable Long userId) {
        // TODO: 实现获取今日提醒
        return Result.success(null);
    }
    
    /**
     * 标记提醒为已读
     * PUT /api/reminder/{id}/read
     */
    @PutMapping("/{id}/read")
    public Result<Void> markAsRead(@PathVariable Long id) {
        // TODO: 实现标记已读
        return Result.success(null);
    }
    
    /**
     * 标记提醒为已完成
     * PUT /api/reminder/{id}/complete
     */
    @PutMapping("/{id}/complete")
    public Result<Void> markAsCompleted(@PathVariable Long id) {
        // TODO: 实现标记完成
        return Result.success(null);
    }
    
    /**
     * 批量删除提醒
     * POST /api/reminder/batch-delete
     */
    @PostMapping("/batch-delete")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        // TODO: 实现批量删除
        return Result.success(null);
    }
}
