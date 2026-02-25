package com.family.file.dto;

import lombok.Data;

/**
 * 文件夹重命名DTO
 */
@Data
public class FolderRenameDTO {
    
    /**
     * 文件夹ID
     */
    private Long folderId;
    
    /**
     * 新名称
     */
    private String newName;
}
