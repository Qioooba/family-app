package com.family.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 用户注册DTO
 */
@Data
public class UserRegisterDTO {
    
    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    private String username;
    
    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    private String password;
    
    /**
     * 手机号
     */
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
    
    /**
     * 验证码
     */
    private String code;
    
    /**
     * 昵称
     */
    private String nickname;
    
    /**
     * 性别
     */
    private Integer gender;
    
    /**
     * 生日
     */
    private LocalDate birthday;
}
