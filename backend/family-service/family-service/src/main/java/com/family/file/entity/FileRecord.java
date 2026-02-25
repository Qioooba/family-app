package com.family.family.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.family.common.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 文件记录实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("file_record")
public class FileRecord extends BaseEntity {
    private static final long serialVersionUID = 1L;
    
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
     * 存储路径
     */
    private String storagePath;
    
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
     * 文件MD5值
     */
    private String fileMd5;
    
    /**
     * 权限: 0-私有, 1-家庭可读, 2-家庭可读写
     */
    private Integer permission;
    
    /**
     * 下载次数
     */
    private Integer downloadCount;
    
    /**
     * 最后下载时间
     */
    private LocalDateTime lastDownloadTime;
}
