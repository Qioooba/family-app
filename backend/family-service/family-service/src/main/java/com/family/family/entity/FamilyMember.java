package com.family.family.entity;

import com.family.common.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class FamilyMember extends BaseEntity {
    private static final long serialVersionUID = 1L;
    
    private Long familyId;
    private Long userId;
    private String role;
    private String nickname;
    private LocalDateTime joinTime;
}
