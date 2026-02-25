package com.family.calendar.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 提醒响应DTO
 */
@Data
public class ReminderResponse {
    
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
     * 提醒类型
     */
    private String reminderType;
    
    /**
     * 提醒时间
     */
    private LocalDateTime remindTime;
    
    /**
     * 重复类型
     */
    private String repeatType;
    
    /**
     * 提醒方式
     */
    private String notifyType;
    
    /**
     * 提前提醒时间(分钟)
     */
    private Integer advanceMinutes;
    
    /**
     * 是否已读
     */
    private Boolean isRead;
    
    /**
     * 是否已完成
     */
    private Boolean isCompleted;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 标签列表
     */
    private List<String> tags;
    
    /**
     * 距离提醒时间还有多久
     */
    private String timeUntil;
}
