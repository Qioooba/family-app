package com.family.notify.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.family.common.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 通知模板实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("notification_template")
public class NotificationTemplate extends BaseEntity {
    private static final long serialVersionUID = 1L;
    
    /**
     * 模板编码
     */
    private String code;
    
    /**
     * 模板名称
     */
    private String name;
    
    /**
     * 模板类型：1-系统通知 2-家庭公告 3-提醒 4-活动通知 5-任务通知
     */
    private Integer type;
    
    /**
     * 适用渠道：1-站内信 2-邮件 3-短信 4-微信模板消息（多个用逗号分隔）
     */
    private String channels;
    
    /**
     * 模板标题
     */
    private String title;
    
    /**
     * 模板内容
     */
    private String content;
    
    /**
     * 邮件模板内容（HTML）
     */
    private String emailContent;
    
    /**
     * 短信模板内容
     */
    private String smsContent;
    
    /**
     * 微信模板ID
     */
    private String wxTemplateId;
    
    /**
     * 微信模板数据（JSON格式）
     */
    private String wxTemplateData;
    
    /**
     * 模板变量说明（JSON格式）
     */
    private String variables;
    
    /**
     * 是否系统模板：0-否 1-是
     */
    private Integer isSystem;
    
    /**
     * 创建用户ID
     */
    private Long createUserId;
    
    /**
     * 描述说明
     */
    private String description;
}
