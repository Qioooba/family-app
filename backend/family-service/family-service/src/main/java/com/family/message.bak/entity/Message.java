package com.family.message.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.family.common.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 消息实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("message")
public class Message extends BaseEntity {
    private static final long serialVersionUID = 1L;
    
    /**
     * 消息标题
     */
    private String title;
    
    /**
     * 消息内容
     */
    private String content;
    
    /**
     * 消息类型：1-系统通知 2-家庭公告 3-@我的 4-私信
     */
    private Integer type;
    
    /**
     * 发送者ID
     */
    private Long senderId;
    
    /**
     * 发送者名称
     */
    private String senderName;
    
    /**
     * 发送者头像
     */
    private String senderAvatar;
    
    /**
     * 接收者ID(0表示全体用户)
     */
    private Long receiverId;
    
    /**
     * 家庭组ID
     */
    private Long familyId;
    
    /**
     * 业务类型
     */
    private String bizType;
    
    /**
     * 业务ID
     */
    private Long bizId;
    
    /**
     * 模板ID
     */
    private Long templateId;
    
    /**
     * 模板参数(JSON)
     */
    private String templateParams;
    
    /**
     * 是否已读：0-未读 1-已读
     */
    private Integer isRead;
    
    /**
     * 优先级：1-低 2-中 3-高
     */
    private Integer priority;
    
    /**
     * 过期时间
     */
    private LocalDateTime expireTime;
    
    /**
     * 额外数据(JSON)
     */
    private String extraData;
}
