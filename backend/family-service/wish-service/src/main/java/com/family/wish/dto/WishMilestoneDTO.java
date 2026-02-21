package com.family.wish.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * 心愿里程碑DTO
 */
@Data
public class WishMilestoneDTO {
    
    private String title;       // 里程碑标题
    private String description; // 描述
    private LocalDate targetDate; // 目标日期
    private Integer sortOrder;  // 排序
}