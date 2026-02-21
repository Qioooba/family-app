package com.family.wish.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 心愿里程碑VO
 */
@Data
public class WishMilestoneVO {
    
    private Long id;
    private Long wishId;
    private String wishTitle;    // 心愿标题
    private String title;        // 里程碑标题
    private String description;  // 描述
    private LocalDate targetDate; // 目标日期
    private Integer isCompleted; // 是否完成
    private Integer sortOrder;   // 排序
    private LocalDateTime createTime;
}