package com.family.file.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文件VO
 */
@Data
public class FileVO {
    
    /**
     * 文件ID
     */
    private Long id;
    
    /**
     * 文件名
     */
    private String fileName;
    
    /**
     * 原始文件名
     */
    private String originalName;
    
    /**
     * 文件类型
     */
    private String fileType;
    
    /**
     * MIME类型
     */
    private String mimeType;
    
    /**
     * 文件大小(字节)
     */
    private Long fileSize;
    
    /**
     * 文件大小(格式化)
     */
    private String fileSizeStr;
    
    /**
     * 访问URL
     */
    private String url;
    
    /**
     * 文件夹ID
     */
    private Long folderId;
    
    /**
     * 上传人ID
     */
    private Long uploaderId;
    
    /**
     * 版本号
     */
    private Integer version;
    
    /**
     * 是否为最新版本
     */
    private Integer isLatest;
    
    /**
     * 文件MD5
     */
    private String fileMd5;
    
    /**
     * 权限
     */
    private Integer permission;
    
    /**
     * 下载次数
     */
    private Integer downloadCount;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
