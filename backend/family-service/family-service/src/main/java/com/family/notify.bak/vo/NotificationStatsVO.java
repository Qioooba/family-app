package com.family.notify.vo;

import lombok.Data;

/**
 * 通知统计VO
 */
@Data
public class NotificationStatsVO {
    
    /**
     * 总通知数
     */
    private Long totalCount;
    
    /**
     * 未读通知数
     */
    private Long unreadCount;
    
    /**
     * 系统通知未读数
     */
    private Long systemUnreadCount;
    
    /**
     * 家庭公告未读数
     */
    private Long announcementUnreadCount;
    
    /**
     * 提醒未读数
     */
    private Long reminderUnreadCount;
    
    /**
     * 活动通知未读数
     */
    private Long activityUnreadCount;
    
    /**
     * 任务通知未读数
     */
    private Long taskUnreadCount;
}
