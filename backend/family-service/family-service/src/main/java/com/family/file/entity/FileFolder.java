package com.family.family.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.family.common.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 文件夹实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("file_folder")
public class FileFolder extends BaseEntity {
    private static final long serialVersionUID = 1L;
    
    /**
     * 文件夹名称
     */
    private String name;
    
    /**
     * 父文件夹ID(0为根目录)
     */
    private Long parentId;
    
    /**
     * 创建人ID
     */
    private Long creatorId;
    
    /**
     * 文件夹路径(冗余字段，便于查询)
     */
    private String path;
    
    /**
     * 权限: 0-私有, 1-家庭可读, 2-家庭可读写
     */
    private Integer permission;
}
