package com.family.file.dto;

import lombok.Data;

/**
 * 文件夹移动DTO
 */
@Data
public class FolderMoveDTO {
    
    /**
     * 文件夹ID
     */
    private Long folderId;
    
    /**
     * 目标父文件夹ID(0为根目录)
     */
    private Long targetParentId;
}
