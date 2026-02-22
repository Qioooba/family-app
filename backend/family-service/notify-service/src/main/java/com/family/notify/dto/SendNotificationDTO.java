package com.family.notify.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 发送通知DTO
 */
@Data
public class SendNotificationDTO {
    
    /**
     * 接收用户ID列表
     */
    private List<Long> userIds;
    
    /**
     * 通知类型：1-系统通知 2-家庭公告 3-提醒 4-活动通知 5-任务通知
     */
    private Integer type;
    
    /**
     * 通知渠道：1-站内信 2-邮件 3-短信 4-微信模板消息（多个用逗号分隔）
     */
    private String channels;
    
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
     * 模板编码（使用模板发送时必填）
     */
    private String templateCode;
    
    /**
     * 模板变量数据
     */
    private Map<String, Object> templateData;
    
    /**
     * 计划发送时间（定时发送，为空则立即发送）
     */
    private LocalDateTime scheduledTime;
    
    /**
     * 家庭ID
     */
    private Long familyId;
    
    /**
     * 扩展数据
     */
    private Map<String, Object> extraData;
}
