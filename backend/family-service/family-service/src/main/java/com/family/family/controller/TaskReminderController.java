package com.family.family.controller;
import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;import com.family.common.core.Result;
import com.family.family.dto.TaskReminderDTO;
import com.family.family.entity.Task;
import com.family.family.mapper.TaskMapper;
import com.family.family.service.TaskReminderService;
import com.family.family.vo.TaskReminderVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 任务提醒控制器
 */
@RestController
@RequestMapping("/api/task")
@SaCheckLogin
public class TaskReminderController {
    
    private final TaskReminderService taskReminderService;
    private final TaskMapper taskMapper;
    
    public TaskReminderController(TaskReminderService taskReminderService, TaskMapper taskMapper) {
        this.taskReminderService = taskReminderService;
        this.taskMapper = taskMapper;
    }
    
    /**
     * 设置任务提醒
     * POST /api/task/{id}/reminder
     */
    @PostMapping("/{id}/reminder")
    public Result<Boolean> setReminder(
            @PathVariable Long id,
            @RequestBody TaskReminderDTO dto) {
        // 验证任务是否存在且用户有权限
        Task task = taskMapper.selectById(id);
        if (task == null) {
            return Result.error("任务不存在");
        }
        
        Long userId = StpUtil.getLoginIdAsLong();
        if (!task.getCreatorId().equals(userId) && !task.getAssigneeId().equals(userId)) {
            return Result.error(403, "无权修改此任务");
        }
        
        // 验证提醒类型
        if (dto.getReminderType() == null || 
            (!"time".equals(dto.getReminderType()) && !"location".equals(dto.getReminderType()))) {
            return Result.error("提醒类型只能是time或location");
        }
        
        // 时间提醒验证
        if ("time".equals(dto.getReminderType()) && dto.getReminderTime() == null) {
            return Result.error("时间提醒需要设置提醒时间");
        }
        
        // 位置提醒验证
        if ("location".equals(dto.getReminderType()) && 
            (dto.getLocationLat() == null || dto.getLocationLng() == null)) {
            return Result.error("位置提醒需要设置经纬度");
        }
        
        taskReminderService.setReminder(id, dto);
        return Result.success(true);
    }
    
    /**
     * 获取任务提醒列表
     * GET /api/task/{id}/reminders
     */
    @GetMapping("/{id}/reminders")
    public Result<List<TaskReminderVO>> getReminders(@PathVariable Long id) {
        // 验证任务是否存在且用户有权限
        Task task = taskMapper.selectById(id);
        if (task == null) {
            return Result.error("任务不存在");
        }
        
        Long userId = StpUtil.getLoginIdAsLong();
        if (!task.getCreatorId().equals(userId) && !task.getAssigneeId().equals(userId)) {
            return Result.error(403, "无权访问此任务");
        }
        
        return Result.success(taskReminderService.getReminders(id));
    }
    
    /**
     * 删除任务提醒
     * DELETE /api/task/reminder/{reminderId}
     */
    @DeleteMapping("/reminder/{reminderId}")
    public Result<Boolean> deleteReminder(@PathVariable Long reminderId) {
        taskReminderService.deleteReminder(reminderId);
        return Result.success(true);
    }
    
    /**
     * 获取用户待触发的时间提醒
     * GET /api/task/reminders/pending
     */
    @GetMapping("/reminders/pending")
    public Result<List<TaskReminderVO>> getPendingReminders() {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(taskReminderService.getPendingTimeReminders(userId));
    }
    
    /**
     * 上报位置并检查位置提醒
     * POST /api/task/reminders/location-check
     */
    @PostMapping("/reminders/location-check")
    public Result<String> checkLocationReminders(@RequestBody LocationCheckRequest request) {
        if (request.getLatitude() == null || request.getLongitude() == null) {
            return Result.error("需要提供经纬度");
        }
        
        Long userId = StpUtil.getLoginIdAsLong();
        taskReminderService.processLocationReminders(userId, request.getLatitude(), request.getLongitude());
        return Result.success("位置提醒检查完成");
    }
    
    /**
     * 手动触发时间提醒检查（管理员接口）
     * POST /api/task/reminders/process-time
     */
    @PostMapping("/reminders/process-time")
    public Result<String> processTimeReminders() {
        taskReminderService.processTimeReminders();
        return Result.success("时间提醒处理完成");
    }
    
    /**
     * 位置检查请求
     */
    public static class LocationCheckRequest {
        private Double latitude;
        private Double longitude;
        
        public Double getLatitude() {
            return latitude;
        }
        
        public void setLatitude(Double latitude) {
            this.latitude = latitude;
        }
        
        public Double getLongitude() {
            return longitude;
        }
        
        public void setLongitude(Double longitude) {
            this.longitude = longitude;
        }
    }
}
