package com.family.calendar.dto.request;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 提醒设置请求DTO
 */
@Data
public class ReminderRequest {
    
    /**
     * 提醒ID
     */
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 家庭ID
     */
    private Long familyId;
    
    /**
     * 提醒标题
     */
    private String title;
    
    /**
     * 提醒内容
     */
    private String content;
    
    /**
     * 提醒类型: event-事件, task-任务, birthday-生日, anniversary-纪念日, custom-自定义
     */
    private String reminderType;
    
    /**
     * 提醒时间
     */
    private LocalDateTime remindTime;
    
    /**
     * 重复类型: none-不重复, daily-每天, weekly-每周, monthly-每月, yearly-每年
     */
    private String repeatType;
    
    /**
     * 重复规则(如每周一、三、五: 1,3,5)
     */
    private String repeatRule;
    
    /**
     * 提醒方式: app-应用内, sms-短信, email-邮件, all-全部
     */
    private String notifyType;
    
    /**
     * 提前提醒时间(分钟)
     */
    private Integer advanceMinutes;
    
    /**
     * 关联的实体ID(如任务ID、生日ID等)
     */
    private Long relatedId;
    
    /**
     * 是否启用
     */
    private Boolean enabled;
    
    /**
     * 标签列表
     */
    private List<String> tags;
}
