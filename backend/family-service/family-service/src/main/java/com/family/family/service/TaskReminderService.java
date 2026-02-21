package com.family.family.service;

import com.family.family.dto.TaskReminderDTO;
import com.family.family.vo.TaskReminderVO;

import java.util.List;

/**
 * 任务提醒服务
 */
public interface TaskReminderService {
    
    /**
     * 设置任务提醒
     * @param taskId 任务ID
     * @param dto 提醒信息
     */
    void setReminder(Long taskId, TaskReminderDTO dto);
    
    /**
     * 获取任务提醒列表
     * @param taskId 任务ID
     * @return 提醒列表
     */
    List<TaskReminderVO> getReminders(Long taskId);
    
    /**
     * 删除任务提醒
     * @param reminderId 提醒ID
     */
    void deleteReminder(Long reminderId);
    
    /**
     * 处理时间提醒（定时任务调用）
     */
    void processTimeReminders();
    
    /**
     * 处理位置提醒（当用户位置更新时调用）
     * @param userId 用户ID
     * @param latitude 纬度
     * @param longitude 经度
     */
    void processLocationReminders(Long userId, Double latitude, Double longitude);
    
    /**
     * 获取用户未触发的时间提醒列表
     * @param userId 用户ID
     * @return 提醒列表
     */
    List<TaskReminderVO> getPendingTimeReminders(Long userId);
}