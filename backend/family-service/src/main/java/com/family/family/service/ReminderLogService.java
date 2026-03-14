package com.family.family.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.family.family.entity.Reminder;
import com.family.family.entity.ReminderLog;
import com.family.family.mapper.ReminderLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 提醒日志服务
 */
@Slf4j
@Service
public class ReminderLogService extends ServiceImpl<ReminderLogMapper, ReminderLog> {
    
    /**
     * 保存执行日志
     */
    public void saveLog(Reminder reminder, Long userId, String title, String content, String status) {
        ReminderLog log = new ReminderLog();
        log.setReminderId(reminder.getId());
        log.setUserId(userId);
        log.setExecuteTime(LocalDateTime.now());
        log.setActualExecuteTime(LocalDateTime.now());
        log.setTitle(title);
        log.setContent(content);
        log.setPushStatus(status);
        
        this.save(log);
    }
    
    /**
     * 标记为已读
     */
    public boolean markAsRead(Long logId, Long userId) {
        LambdaQueryWrapper<ReminderLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReminderLog::getId, logId)
               .eq(ReminderLog::getUserId, userId);
        
        ReminderLog log = this.getOne(wrapper);
        if (log != null) {
            log.setReadStatus(1);
            log.setReadTime(LocalDateTime.now());
            return this.updateById(log);
        }
        return false;
    }
}
