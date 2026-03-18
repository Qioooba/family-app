package com.family.family.service.scene;

import lombok.Builder;
import lombok.Data;

/**
 * 场景化提醒模板
 */
@Data
@Builder
public class SceneTemplate {
    
    /** 场景类型 */
    private String sceneType;
    
    /** 提醒名称 */
    private String reminderName;
    
    /** 提醒类型 */
    private String reminderType;
    
    /** 频率类型 */
    private String frequencyType;
    
    /** 频率配置 JSON */
    private String frequencyConfig;
    
    /** 标题模板 */
    private String titleTemplate;
    
    /** 内容模板 */
    private String contentTemplate;
    
    /** 业务数据 JSON */
    private String businessData;
    
    /** 图标 */
    private String icon;
    
    /** 描述 */
    private String description;
    
    /** 背景色 */
    private String bgColor;
}
