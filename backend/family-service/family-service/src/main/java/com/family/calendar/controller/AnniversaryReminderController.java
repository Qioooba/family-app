
package com.family.calendar.controller;
import cn.dev33.satoken.annotation.SaCheckLogin;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.family.calendar.dto.AnniversaryReminderDTO;
import com.family.family.entity.AnniversaryReminder;
import com.family.calendar.mapper.AnniversaryReminderMapper;
import com.family.common.core.Result;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 纪念日提醒控制器
 */
@RestController
@SaCheckLogin
@RequestMapping("/api/calendar/anniversary/reminder")
public class AnniversaryReminderController {
    
    private final AnniversaryReminderMapper reminderMapper;
    
    public AnniversaryReminderController(AnniversaryReminderMapper reminderMapper) {
        this.reminderMapper = reminderMapper;
    }
    
    /**
     * 设置纪念日提醒
     * POST /api/anniversary/reminder
     */
    @PostMapping
    public Result<AnniversaryReminder> setReminder(@RequestBody AnniversaryReminderDTO dto) {
        // 检查是否已存在
        QueryWrapper<AnniversaryReminder> wrapper = new QueryWrapper<>();
        wrapper.eq("anniversary_id", dto.getAnniversaryId())
               .eq("reminder_days", dto.getReminderDays());
        AnniversaryReminder existing = reminderMapper.selectOne(wrapper);
        
        AnniversaryReminder reminder;
        if (existing != null) {
            reminder = existing;
            reminder.setReminderType(dto.getReminderType());
            reminder.setIsEnabled(dto.getIsEnabled() != null ? dto.getIsEnabled() : 1);
            reminderMapper.updateById(reminder);
        } else {
            reminder = new AnniversaryReminder();
            reminder.setAnniversaryId(dto.getAnniversaryId());
            reminder.setReminderDays(dto.getReminderDays());
            reminder.setReminderType(dto.getReminderType());
            reminder.setIsEnabled(1);
            reminderMapper.insert(reminder);
        }
        
        return Result.success(reminder);
    }
    
    /**
     * 获取纪念日的提醒设置列表
     * GET /api/anniversary/reminder/{anniversaryId}
     */
    @GetMapping("/{anniversaryId}")
    public Result<List<AnniversaryReminder>> getReminders(@PathVariable Long anniversaryId) {
        QueryWrapper<AnniversaryReminder> wrapper = new QueryWrapper<>();
        wrapper.eq("anniversary_id", anniversaryId)
               .orderByAsc("reminder_days");
        List<AnniversaryReminder> reminders = reminderMapper.selectList(wrapper);
        return Result.success(reminders);
    }
    
    /**
     * 删除提醒设置
     * DELETE /api/anniversary/reminder/{id}
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteReminder(@PathVariable Long id) {
        reminderMapper.deleteById(id);
        return Result.success();
    }
    
    /**
     * 启用/禁用提醒
     * PUT /api/anniversary/reminder/{id}/toggle
     */
    @PutMapping("/{id}/toggle")
    public Result<Void> toggleReminder(@PathVariable Long id) {
        AnniversaryReminder reminder = reminderMapper.selectById(id);
        if (reminder != null) {
            reminder.setIsEnabled(reminder.getIsEnabled() == 1 ? 0 : 1);
            reminderMapper.updateById(reminder);
        }
        return Result.success();
    }
}
