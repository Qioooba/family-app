package com.family.calendar.dto.response;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 卡片响应DTO
 */
@Data
public class CardResponse {
    
    /**
     * 卡片ID
     */
    private Long id;
    
    /**
     * 家庭ID
     */
    private Long familyId;
    
    /**
     * 创建者ID
     */
    private Long creatorId;
    
    /**
     * 创建者名称
     */
    private String creatorName;
    
    /**
     * 卡片类型
     */
    private String cardType;
    
    /**
     * 卡片标题
     */
    private String title;
    
    /**
     * 卡片内容
     */
    private String content;
    
    /**
     * 卡片日期
     */
    private LocalDate cardDate;
    
    /**
     * 卡片时间
     */
    private LocalDateTime cardTime;
    
    /**
     * 主题
     */
    private String theme;
    
    /**
     * 封面图片
     */
    private String coverImage;
    
    /**
     * 重复类型
     */
    private String repeatType;
    
    /**
     * 距离天数
     */
    private Integer daysUntil;
    
    /**
     * 年龄(仅生日类型)
     */
    private Integer age;
    
    /**
     * 周几年数(仅纪念日类型)
     */
    private Integer anniversaryYears;
    
    /**
     * 是否公开
     */
    private Boolean isPublic;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 标签
     */
    private List<String> tags;
    
    /**
     * 关联用户信息
     */
    private List<RelatedUser> relatedUsers;
    
    /**
     * 关联用户
     */
    @Data
    public static class RelatedUser {
        private Long userId;
        private String userName;
        private String avatar;
    }
}
