package com.family.notify.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 通知查询DTO
 */
@Data
public class NotificationQueryDTO {
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 通知类型：1-系统通知 2-家庭公告 3-提醒 4-活动通知 5-任务通知
     */
    private Integer type;
    
    /**
     * 是否已读：0-未读 1-已读
     */
    private Integer isRead;
    
    /**
     * 开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 结束时间
     */
    private LocalDateTime endTime;
    
    /**
     * 页码
     */
    private Integer pageNum = 1;
    
    /**
     * 每页大小
     */
    private Integer pageSize = 10;
}
