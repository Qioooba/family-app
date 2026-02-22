package com.family.message.dto;

import lombok.Data;

/**
 * 消息列表查询DTO
 */
@Data
public class MessageListDTO {
    
    /**
     * 消息类型
     */
    private Integer type;
    
    /**
     * 是否已读：0-未读 1-已读
     */
    private Integer isRead;
    
    /**
     * 页码
     */
    private Integer pageNum = 1;
    
    /**
     * 每页大小
     */
    private Integer pageSize = 20;
}
