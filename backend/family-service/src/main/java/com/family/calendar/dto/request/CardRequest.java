package com.family.calendar.dto.request;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 卡片创建/更新请求DTO
 */
@Data
public class CardRequest {
    
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
     * 卡片类型: anniversary-纪念日, birthday-生日, event-事件, task-任务, wish-心愿, custom-自定义
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
     * 背景颜色/主题
     */
    private String theme;
    
    /**
     * 封面图片
     */
    private String coverImage;
    
    /**
     * 是否重复: none-不重复, yearly-每年重复
     */
    private String repeatType;
    
    /**
     * 提醒时间(提前天数)
     */
    private Integer remindDays;
    
    /**
     * 是否公开
     */
    private Boolean isPublic;
    
    /**
     * 关联的用户ID列表
     */
    private List<Long> relatedUserIds;
    
    /**
     * 标签
     */
    private List<String> tags;
}
