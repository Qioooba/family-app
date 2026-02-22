package com.family.file.dto;

import lombok.Data;

/**
 * 文件分片合并DTO
 */
@Data
public class FileMergeDTO {
    
    /**
     * 上传任务ID
     */
    private String uploadId;
    
    /**
     * 文件MD5
     */
    private String fileMd5;
}
