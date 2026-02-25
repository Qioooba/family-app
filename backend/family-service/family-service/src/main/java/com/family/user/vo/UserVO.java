package com.family.user.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户VO
 */
@Data
public class UserVO {
    private Long id;
    private String username;
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
    private LocalDateTime createTime;
}
