package com.family.family.dto.request;

import lombok.Data;

import java.util.List;

/**
 * 开关/偏好设置请求DTO
 */
@Data
public class SwitchRequest {
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 家庭ID
     */
    private Long familyId;
    
    /**
     * 设置项Key
     */
    private String settingKey;
    
    /**
     * 设置项值
     */
    private String settingValue;
    
    /**
     * 设置类型: notification-通知, privacy-隐私, display-显示, system-系统
     */
    private String settingType;
    
    /**
     * 开关状态: true-开启, false-关闭
     */
    private Boolean switchStatus;
    
    /**
     * 批量设置项
     */
    private List<SettingItem> settings;
    
    /**
     * 设置项
     */
    @Data
    public static class SettingItem {
        private String key;
        private String value;
    }
}
