package com.family.notify.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 通知VO
 */
@Data
public class NotificationVO {
    
    /**
     * 通知ID
     */
    private Long id;
    
    /**
     * 发送用户ID
     */
    private Long senderId;
    
    /**
     * 发送用户昵称
     */
    private String senderName;
    
    /**
     * 发送用户头像
     */
    private String senderAvatar;
    
    /**
     * 通知类型：1-系统通知 2-家庭公告 3-提醒 4-活动通知 5-任务通知
     */
    private Integer type;
    
    /**
     * 通知类型名称
     */
    private String typeName;
    
    /**
     * 通知渠道
     */
    private Integer channel;
    
    /**
     * 通知标题
     */
    private String title;
    
    /**
     * 通知内容
     */
    private String content;
    
    /**
     * 关联业务类型
     */
    private String bizType;
    
    /**
     * 关联业务ID
     */
    private Long bizId;
    
    /**
     * 是否已读：0-未读 1-已读
     */
    private Integer isRead;
    
    /**
     * 读取时间
     */
    private LocalDateTime readTime;
    
    /**
     * 发送时间
     */
    private LocalDateTime sendTime;
    
    /**
     * 家庭ID
     */
    private Long familyId;
    
    /**
     * 扩展数据
     */
    private String extraData;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
