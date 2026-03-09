package com.family.family.entity;

import lombok.Data;
import java.util.Map;

/**
 * 企业微信消息内容对象
 */
@Data
public class WechatMessage {
    
    private MessageType type;           // 消息类型
    private Long targetUserId;          // 接收者用户ID（数据库ID）
    private String title;               // 消息标题
    private String description;         // 详细描述
    private String url;                 // 点击跳转链接
    private Map<String, Object> extra;  // 扩展字段
    
    public WechatMessage() {}
    
    public WechatMessage(MessageType type, Long targetUserId, String title, String description) {
        this.type = type;
        this.targetUserId = targetUserId;
        this.title = title;
        this.description = description;
        this.url = "https://qioba.cn/pages/task/index";
    }
}
