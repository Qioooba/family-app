package com.family.vote.entity;

import com.family.common.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 投票实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Vote extends BaseEntity {
    private static final long serialVersionUID = 1L;
    
    private Long familyId;
    private Long creatorId;
    private String title;
    private String description;
    private String voteType; // single multiple rating rank binary
    private Integer maxSelect;
    private Boolean isAnonymous;
    private Boolean canChange;
    private Integer minVotes;
    private Double passThreshold;
    private LocalDateTime endTime;
    private Integer status; // 0进行中 1已结束
    private String options; // JSON选项
    private String result;
}
