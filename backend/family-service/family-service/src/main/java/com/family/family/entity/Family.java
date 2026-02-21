package com.family.family.entity;

import com.family.common.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Family extends BaseEntity {
    private static final long serialVersionUID = 1L;
    
    private String name;
    private String avatar;
    private Long creatorId;
    private String inviteCode;
    private Integer memberCount;
    private Long storageUsed;
    private Long storageLimit;
}
