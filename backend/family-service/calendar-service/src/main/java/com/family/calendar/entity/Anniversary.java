package com.family.calendar.entity;

import com.family.common.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 纪念日实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Anniversary extends BaseEntity {
    private static final long serialVersionUID = 1L;
    
    private Long familyId;
    private String title;
    private String type; // birthday love wedding family traditional custom
    private String dateType; // solar lunar
    private LocalDate targetDate;
    private Boolean isRecurring;
    private String reminderDays; // JSON [1,3,7]
    private Long relatedUserId;
    private String description;
    private String images;
    private String icon;
}
