package com.family.message.dto;

import lombok.Data;

import java.util.Map;

/**
 * 发送消息DTO
 */
@Data
public class MessageSendDTO {
    
    /**
     * 消息类型：1-系统通知 2-家庭公告 3-@我的 4-私信
     */
    private Integer type;
    
    /**
     * 消息标题
     */
    private String title;
    
    /**
     * 消息内容
     */
    private String content;
    
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
     * 模板编码
     */
    private String templateCode;
    
    /**
     * 模板参数
     */
    private Map<String, Object> templateParams;
    
    /**
     * 优先级：1-低 2-中 3-高
     */
    private Integer priority;
    
    /**
     * 额外数据
     */
    private Map<String, Object> extraData;
}
