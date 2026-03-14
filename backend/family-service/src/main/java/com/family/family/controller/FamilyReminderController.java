package com.family.family.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.family.family.entity.Reminder;
import com.family.family.service.ReminderScheduleService;
import com.family.family.service.ReminderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 提醒管理 Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/reminder")
@SaCheckLogin
public class FamilyReminderController {
    
    @Autowired
    private ReminderService reminderService;
    
    @Autowired
    private ReminderScheduleService reminderScheduleService;
    
    /**
     * 获取今日提醒
     */
    @GetMapping("/today")
    public Map<String, Object> getTodayReminders() {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            List<Reminder> reminders = reminderService.getTodayReminders(userId);
            return success(reminders);
        } catch (Exception e) {
            log.error("获取今日提醒失败", e);
            return error("获取失败");
        }
    }
    
    /**
     * 获取我的提醒列表
     */
    @GetMapping("/list")
    public Map<String, Object> getMyReminders() {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            List<Reminder> reminders = reminderService.getUserReminders(userId);
            return success(reminders);
        } catch (Exception e) {
            log.error("获取提醒列表失败", e);
            return error("获取失败");
        }
    }
    
    /**
     * 获取提醒详情
     */
    @GetMapping("/detail/{id}")
    public Map<String, Object> getReminderDetail(@PathVariable Long id) {
        try {
            Reminder reminder = reminderService.getById(id);
            if (reminder == null) {
                return error("提醒不存在");
            }
            return success(reminder);
        } catch (Exception e) {
            log.error("获取提醒详情失败", e);
            return error("获取失败");
        }
    }
    
    /**
     * 创建提醒
     */
    @PostMapping("/add")
    public Map<String, Object> addReminder(@RequestBody Reminder reminder) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            reminder.setCreateBy(userId);
            
            boolean success = reminderService.createReminder(reminder);
            if (success) {
                // 初始化下次执行时间
                reminderScheduleService.initNextExecuteTime(reminder);
                return success(null, "创建成功");
            }
            return error("创建失败");
        } catch (Exception e) {
            log.error("创建提醒失败", e);
            return error("创建失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新提醒
     */
    @PostMapping("/update")
    public Map<String, Object> updateReminder(@RequestBody Reminder reminder) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            if (!reminderService.isOwner(reminder.getId(), userId)) {
                return error("无权限修改");
            }
            
            boolean success = reminderService.updateReminder(reminder);
            if (success) {
                // 重新计算下次执行时间
                reminderScheduleService.initNextExecuteTime(reminder);
                return success(null, "更新成功");
            }
            return error("更新失败");
        } catch (Exception e) {
            log.error("更新提醒失败", e);
            return error("更新失败");
        }
    }
    
    /**
     * 删除提醒
     */
    @PostMapping("/delete")
    public Map<String, Object> deleteReminder(@RequestParam Long id) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            boolean success = reminderService.deleteReminder(id, userId);
            if (success) {
                return success(null, "删除成功");
            }
            return error("删除失败");
        } catch (Exception e) {
            log.error("删除提醒失败", e);
            return error("删除失败");
        }
    }
    
    /**
     * 切换状态
     */
    @PostMapping("/toggle")
    public Map<String, Object> toggleStatus(@RequestParam Long id) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            boolean success = reminderService.toggleStatus(id, userId);
            if (success) {
                return success(null, "状态已切换");
            }
            return error("切换失败");
        } catch (Exception e) {
            log.error("切换状态失败", e);
            return error("切换失败");
        }
    }
    
    /**
     * 立即执行（测试用）
     */
    @PostMapping("/execute")
    public Map<String, Object> executeNow(@RequestParam Long id) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            Reminder reminder = reminderService.getById(id);
            
            if (reminder == null) {
                return error("提醒不存在");
            }
            
            if (!reminder.getCreateBy().equals(userId)) {
                return error("无权限");
            }
            
            // 立即执行
            reminderScheduleService.executeReminder(reminder);
            return success(null, "执行成功");
        } catch (Exception e) {
            log.error("立即执行失败", e);
            return error("执行失败");
        }
    }
    
    /**
     * 获取频率类型选项
     */
    @GetMapping("/frequency-types")
    public Map<String, Object> getFrequencyTypes() {
        Map<String, Object> types = new HashMap<>();
        types.put("ONCE", "一次性");
        types.put("DAILY", "每天");
        types.put("HOURLY", "每小时");
        types.put("WEEKLY", "每周");
        types.put("MONTHLY", "每月");
        types.put("YEARLY", "每年");
        types.put("INTERVAL", "间隔");
        return success(types);
    }
    
    /**
     * 获取提醒类型选项
     */
    @GetMapping("/reminder-types")
    public Map<String, Object> getReminderTypes() {
        Map<String, Object> types = new HashMap<>();
        types.put("WATER", "喝水提醒");
        types.put("MEDICINE", "用药提醒");
        types.put("EXPIRE", "保质期");
        types.put("BIRTHDAY", "生日提醒");
        types.put("FINANCE", "财务提醒");
        types.put("SYSTEM", "系统提醒");
        return success(types);
    }
    
    private Map<String, Object> success(Object data) {
        return success(data, "success");
    }
    
    private Map<String, Object> success(Object data, String message) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("message", message);
        result.put("data", data);
        return result;
    }
    
    private Map<String, Object> error(String message) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 500);
        result.put("message", message);
        return result;
    }
}
