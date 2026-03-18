package com.family.family.dto.request;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;

/**
 * 场景提醒创建请求
 */
@Data
public class SceneCreateRequest {
    
    /** 场景类型 */
    @NotBlank(message = "场景类型不能为空")
    private String sceneType;
    
    /** 自定义名称（可选） */
    private String customName;
    
    /** 自定义配置（可选，覆盖默认配置） */
    private Map<String, Object> customConfig;
    
    /** 推送范围：SELF/ALL/SPECIFIED */
    private String pushScope;
    
    /** 指定用户ID列表 */
    private List<Long> targetUserIds;
}
