package com.family.wish.entity;

import com.family.common.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 心愿实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Wish extends BaseEntity {
    private static final long serialVersionUID = 1L;
    
    private Long familyId;
    private Long userId;
    private String type; // item-物品 experience-体验 learn-学习 relation-关系 charity-公益 goal-目标
    private String title;
    private String description;
    private BigDecimal budgetMin;
    private BigDecimal budgetMax;
    private LocalDate expectDate;
    private String visibility; // public-公开 family-家庭 couple-伴侣 private-私密
    private Integer priority; // 1-5
    private Integer difficulty; // 1-3
    private Integer status; // 0待实现 1进行中 2已实现 3已放弃
    private Long claimantId;
    private Integer progress;
    private String images;
    private LocalDate finishTime;
}
