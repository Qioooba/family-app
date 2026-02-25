package com.family.family.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.family.common.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 消息模板实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("message_template")
public class MessageTemplate extends BaseEntity {
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
     * 模板类型：1-系统 2-家庭 3-私信
     */
    private Integer type;
    
    /**
     * 消息标题模板
     */
    private String titleTemplate;
    
    /**
     * 消息内容模板
     */
    private String contentTemplate;
    
    /**
     * 参数定义(JSON数组)
     */
    private String params;
    
    /**
     * 业务类型
     */
    private String bizType;
    
    /**
     * 是否启用
     */
    private Integer isEnabled;
    
    /**
     * 描述
     */
    private String description;
}
