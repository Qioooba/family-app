package com.family.file.dto;

import lombok.Data;

/**
 * 文件分片上传初始化DTO
 */
@Data
public class FileUploadInitDTO {
    
    /**
     * 文件名
     */
    private String fileName;
    
    /**
     * 文件MD5
     */
    private String fileMd5;
    
    /**
     * 文件大小(字节)
     */
    private Long fileSize;
    
    /**
     * MIME类型
     */
    private String mimeType;
    
    /**
     * 目标文件夹ID
     */
    private Long folderId;
    
    /**
     * 权限: 0-私有, 1-家庭可读, 2-家庭可读写
     */
    private Integer permission;
}
