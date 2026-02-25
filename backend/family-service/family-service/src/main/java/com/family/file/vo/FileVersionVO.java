package com.family.file.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文件版本VO
 */
@Data
public class FileVersionVO {
    
    /**
     * 版本ID
     */
    private Long id;
    
    /**
     * 文件ID
     */
    private Long fileId;
    
    /**
     * 版本号
     */
    private Integer version;
    
    /**
     * 文件大小
     */
    private Long fileSize;
    
    /**
     * 文件大小(格式化)
     */
    private String fileSizeStr;
    
    /**
     * 版本描述
     */
    private String description;
    
    /**
     * 创建人ID
     */
    private Long creatorId;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
