package com.family.file.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 创建分享DTO
 */
@Data
public class FileShareCreateDTO {
    
    /**
     * 文件ID
     */
    private Long fileId;
    
    /**
     * 分享类型: 1-链接分享, 2-家庭分享, 3-指定用户分享
     */
    private Integer shareType;
    
    /**
     * 被分享用户ID(share_type=3时有效)
     */
    private Long shareUserId;
    
    /**
     * 访问密码(可选)
     */
    private String accessPassword;
    
    /**
     * 权限: 1-只读, 2-读写
     */
    private Integer permission;
    
    /**
     * 有效期至(可选)
     */
    private LocalDateTime expireTime;
}
