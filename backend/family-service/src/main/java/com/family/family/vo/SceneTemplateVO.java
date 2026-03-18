package com.family.family.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 场景模板VO
 */
@Data
@Builder
public class SceneTemplateVO {
    
    /** 场景类型 */
    private String sceneType;
    
    /** 场景名称 */
    private String name;
    
    /** 描述 */
    private String description;
    
    /** 图标 */
    private String icon;
    
    /** 背景色 */
    private String bgColor;
    
    /** 默认配置 */
    private Object defaultConfig;
    
    /** 是否已创建 */
    private Boolean created;
    
    /** 提醒ID（如果已创建） */
    private Long reminderId;
    
    /** 是否启用 */
    private Boolean enabled;
}
