package com.family.family.dto.response;

import lombok.Data;

import java.util.List;

/**
 * 开关/偏好设置响应DTO
 */
@Data
public class SwitchResponse {
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 家庭ID
     */
    private Long familyId;
    
    /**
     * 设置分类列表
     */
    private List<SettingCategory> categories;
    
    /**
     * 设置分类
     */
    @Data
    public static class SettingCategory {
        /**
         * 分类编码
         */
        private String code;
        
        /**
         * 分类名称
         */
        private String name;
        
        /**
         * 分类描述
         */
        private String description;
        
        /**
         * 设置项列表
         */
        private List<SettingItem> items;
    }
    
    /**
     * 设置项
     */
    @Data
    public static class SettingItem {
        /**
         * 设置项Key
         */
        private String key;
        
        /**
         * 设置项名称
         */
        private String name;
        
        /**
         * 设置项描述
         */
        private String description;
        
        /**
         * 设置值
         */
        private String value;
        
        /**
         * 设置类型: boolean-开关, string-文本, number-数字, select-选项
         */
        private String type;
        
        /**
         * 开关状态(仅boolean类型)
         */
        private Boolean switchOn;
        
        /**
         * 可选值列表(仅select类型)
         */
        private List<String> options;
        
        /**
         * 是否可修改
         */
        private Boolean editable;
    }
}
