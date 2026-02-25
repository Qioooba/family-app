package com.family.file.dto;

import lombok.Data;

/**
 * 创建文件夹DTO
 */
@Data
public class FolderCreateDTO {
    
    /**
     * 文件夹名称
     */
    private String name;
    
    /**
     * 父文件夹ID(不传或为0表示根目录)
     */
    private Long parentId;
    
    /**
     * 权限: 0-私有, 1-家庭可读, 2-家庭可读写
     */
    private Integer permission;
}
