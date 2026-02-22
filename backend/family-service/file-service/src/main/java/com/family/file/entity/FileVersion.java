package com.family.file.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.family.common.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 文件版本实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("file_version")
public class FileVersion extends BaseEntity {
    private static final long serialVersionUID = 1L;
    
    /**
     * 文件ID
     */
    private Long fileId;
    
    /**
     * 版本号
     */
    private Integer version;
    
    /**
     * 存储路径
     */
    private String storagePath;
    
    /**
     * 文件大小
     */
    private Long fileSize;
    
    /**
     * 文件MD5
     */
    private String fileMd5;
    
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
