package com.family.family.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.family.family.entity.Reminder;
import com.family.family.mapper.ReminderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.List;

/**
 * 提醒 Service（完整版）
 */
@Slf4j
@Service
public class ReminderService extends ServiceImpl<ReminderMapper, Reminder> {

    private static final Set<String> SCENE_REMINDER_TYPES = Set.of(
        "WATER", "WEATHER_RAIN", "WEATHER_TEMP", "AIR_QUALITY", "UV_INDEX",
        "MORNING", "CHECKIN", "SCHEDULE", "SLEEP", "ANNIVERSARY", "EYE_REST", "SEDENTARY"
    );
    
    @Autowired
    private ReminderMapper reminderMapper;
    
    /**
     * 创建提醒
     */
    public boolean createReminder(Reminder reminder) {
        // 设置默认值（只在为null时）
        if (reminder.getStatus() == null) {
            reminder.setStatus(1);
        }
        if (reminder.getCreateType() == null) {
            reminder.setCreateType(1);
        }
        if (reminder.getPushScope() == null || reminder.getPushScope().isEmpty()) {
            reminder.setPushScope("SELF");
        }
        if (reminder.getExecuteCount() == null) {
            reminder.setExecuteCount(0);
        }
        if (reminder.getPriority() == null) {
            reminder.setPriority(5);
        }
        
        // 如果没有设置提醒类型，默认为系统提醒
        if (reminder.getReminderType() == null || reminder.getReminderType().trim().isEmpty()) {
            reminder.setReminderType("SYSTEM");
        }
        
        // 如果没有设置下次执行时间，初始化为现在（这样调度器会立即处理它）
        if (reminder.getNextExecuteTime() == null) {
            reminder.setNextExecuteTime(LocalDateTime.now());
        }
        
        return this.save(reminder);
    }
    
    /**
     * 更新提醒
     */
    public boolean updateReminder(Reminder reminder) {
        return this.updateById(reminder);
    }
    
    /**
     * 删除提醒
     */
    public boolean deleteReminder(Long id, Long userId) {
        LambdaQueryWrapper<Reminder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Reminder::getId, id)
               .eq(Reminder::getCreateBy, userId);
        return this.remove(wrapper);
    }
    
    @Autowired
    private ReminderScheduleService reminderScheduleService;
    
    /**
     * 切换状态
     */
    public boolean toggleStatus(Long id, Long userId) {
        LambdaQueryWrapper<Reminder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Reminder::getId, id)
               .eq(Reminder::getCreateBy, userId);
        
        Reminder reminder = this.getOne(wrapper);
        if (reminder != null) {
            Integer newStatus = reminder.getStatus() == 1 ? 0 : 1;
            reminder.setStatus(newStatus);
            
            // 如果启用提醒，重新计算下次执行时间
            if (newStatus == 1) {
                // 对于一次性提醒，如果已经执行过，需要重新设置时间
                if ("ONCE".equals(reminder.getFrequencyType()) && reminder.getLastExecuteTime() != null) {
                    // 一次性提醒已执行过，不应再启用，但用户要求启用，设为当前时间
                    reminder.setNextExecuteTime(LocalDateTime.now().plusMinutes(1));
                } else {
                    // 重新计算下次执行时间
                    reminderScheduleService.calculateNextExecuteTime(reminder);
                }
            }
            
            return this.updateById(reminder);
        }
        return false;
    }
    
    /**
     * 检查是否是创建者
     */
    public boolean isOwner(Long id, Long userId) {
        Reminder reminder = this.getById(id);
        return reminder != null && reminder.getCreateBy().equals(userId);
    }
    
    /**
     * 获取用户的今日提醒（只返回今天需要执行的提醒）
     */
    public List<Reminder> getTodayReminders(Long userId) {
        // 今天的开始和结束时间
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime todayEnd = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
        
        LambdaQueryWrapper<Reminder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Reminder::getCreateBy, userId)
               .eq(Reminder::getStatus, 1)  // 只查询启用的提醒
               .notIn(Reminder::getReminderType, SCENE_REMINDER_TYPES)
               .isNotNull(Reminder::getNextExecuteTime)  // 确保下次执行时间不为空
               .between(Reminder::getNextExecuteTime, todayStart, todayEnd)
               .orderByAsc(Reminder::getNextExecuteTime);
        return this.list(wrapper);
    }
    
    /**
     * 获取用户的所有提醒
     */
    public List<Reminder> getUserReminders(Long userId) {
        LambdaQueryWrapper<Reminder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Reminder::getCreateBy, userId)
               .notIn(Reminder::getReminderType, SCENE_REMINDER_TYPES)
               .orderByDesc(Reminder::getCreateTime);
        return this.list(wrapper);
    }
}
