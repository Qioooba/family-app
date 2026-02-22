package com.family.file.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文件夹VO
 */
@Data
public class FolderVO {
    
    /**
     * 文件夹ID
     */
    private Long id;
    
    /**
     * 文件夹名称
     */
    private String name;
    
    /**
     * 父文件夹ID
     */
    private Long parentId;
    
    /**
     * 创建人ID
     */
    private Long creatorId;
    
    /**
     * 权限
     */
    private Integer permission;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
