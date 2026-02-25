package com.family.file.dto;

import lombok.Data;

/**
 * 文件分片上传DTO
 */
@Data
public class FileChunkUploadDTO {
    
    /**
     * 上传任务ID
     */
    private String uploadId;
    
    /**
     * 分片序号(从0开始)
     */
    private Integer chunkIndex;
    
    /**
     * 总分片数
     */
    private Integer totalChunks;
}
