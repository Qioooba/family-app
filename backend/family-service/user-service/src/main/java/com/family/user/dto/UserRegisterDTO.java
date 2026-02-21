package com.family.user.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 用户注册DTO
 */
@Data
public class UserRegisterDTO {
    private String username;
    private String password;
    private String phone;
    private String code;
    private String nickname;
    private Integer gender;
    private LocalDate birthday;
}
