package com.family.notify.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.family.common.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 通知设置实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("notification_setting")
public class NotificationSetting extends BaseEntity {
    private static final long serialVersionUID = 1L;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 是否开启站内信通知：0-关闭 1-开启
     */
    private Integer enableInApp;
    
    /**
     * 是否开启邮件通知：0-关闭 1-开启
     */
    private Integer enableEmail;
    
    /**
     * 是否开启短信通知：0-关闭 1-开启
     */
    private Integer enableSms;
    
    /**
     * 是否开启微信通知：0-关闭 1-开启
     */
    private Integer enableWx;
    
    /**
     * 系统通知设置：0-关闭 1-开启
     */
    private Integer notifySystem;
    
    /**
     * 家庭公告设置：0-关闭 1-开启
     */
    private Integer notifyAnnouncement;
    
    /**
     * 提醒通知设置：0-关闭 1-开启
     */
    private Integer notifyReminder;
    
    /**
     * 活动通知设置：0-关闭 1-开启
     */
    private Integer notifyActivity;
    
    /**
     * 任务通知设置：0-关闭 1-开启
     */
    private Integer notifyTask;
    
    /**
     * 免打扰开始时间（HH:mm格式）
     */
    private String quietStartTime;
    
    /**
     * 免打扰结束时间（HH:mm格式）
     */
    private String quietEndTime;
    
    /**
     * 是否开启免打扰：0-关闭 1-开启
     */
    private Integer enableQuiet;
}
