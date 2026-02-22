package com.family.file.dto;

import lombok.Data;

/**
 * 文件移动DTO
 */
@Data
public class FileMoveDTO {
    
    /**
     * 文件ID
     */
    private Long fileId;
    
    /**
     * 目标文件夹ID
     */
    private Long targetFolderId;
}
