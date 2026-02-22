package com.family.file.dto;

import lombok.Data;

/**
 * 文件权限更新DTO
 */
@Data
public class FilePermissionDTO {
    
    /**
     * 文件ID
     */
    private Long fileId;
    
    /**
     * 权限: 0-私有, 1-家庭可读, 2-家庭可读写
     */
    private Integer permission;
}
