package com.family.file.dto;

import lombok.Data;

/**
 * 访问分享DTO
 */
@Data
public class FileShareAccessDTO {
    
    /**
     * 分享链接
     */
    private String shareLink;
    
    /**
     * 访问密码
     */
    private String accessPassword;
}
