package com.family.message.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.family.common.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 消息已读状态实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("message_read_status")
public class MessageReadStatus extends BaseEntity {
    private static final long serialVersionUID = 1L;
    
    /**
     * 消息ID
     */
    private Long messageId;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 是否已读：0-未读 1-已读
     */
    private Integer isRead;
    
    /**
     * 首次阅读时间
     */
    private LocalDateTime firstReadTime;
    
    /**
     * 最后阅读时间
     */
    private LocalDateTime lastReadTime;
}
