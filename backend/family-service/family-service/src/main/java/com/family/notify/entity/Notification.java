package com.family.family.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.family.common.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 通知实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("notification")
public class Notification extends BaseEntity {
    private static final long serialVersionUID = 1L;
    
    /**
     * 接收用户ID
     */
    private Long userId;
    
    /**
     * 发送用户ID（系统发送为0）
     */
    private Long senderId;
    
    /**
     * 通知类型：1-系统通知 2-家庭公告 3-提醒 4-活动通知 5-任务通知
     */
    private Integer type;
    
    /**
     * 通知渠道：1-站内信 2-邮件 3-短信 4-微信模板消息
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
     * 计划发送时间（定时发送）
     */
    private LocalDateTime scheduledTime;
    
    /**
     * 发送状态：0-待发送 1-已发送 2-发送失败
     */
    private Integer sendStatus;
    
    /**
     * 失败原因
     */
    private String failReason;
    
    /**
     * 家庭ID
     */
    private Long familyId;
    
    /**
     * 扩展数据（JSON格式）
     */
    private String extraData;
}
