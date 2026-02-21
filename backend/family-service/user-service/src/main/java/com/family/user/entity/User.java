package com.family.user.entity;

import com.family.common.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 用户实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class User extends BaseEntity {
    private static final long serialVersionUID = 1L;
    
    private String username;
    private String password;
    private String nickname;
    private String avatar;
    private String phone;
    private String email;
    private Integer gender;
    private LocalDate birthday;
    private BigDecimal height;
    private BigDecimal weight;
    private BigDecimal targetWeight;
    private Integer dailyCalories;
    private String openId;
    private String unionId;
    private Integer loginType;
    private LocalDate lastLoginTime;
    private String lastLoginIp;
}
