package com.family.message.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 消息VO
 */
@Data
public class MessageVO {
    
    private Long id;
    
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
     * 类型名称
     */
    private String typeName;
    
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
     * 接收者ID
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
     * 是否已读：0-未读 1-已读
     */
    private Integer isRead;
    
    /**
     * 优先级：1-低 2-中 3-高
     */
    private Integer priority;
    
    /**
     * 优先级名称
     */
    private String priorityName;
    
    /**
     * 额外数据
     */
    private String extraData;
    
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
