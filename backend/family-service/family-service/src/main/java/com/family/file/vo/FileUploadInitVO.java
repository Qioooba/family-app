package com.family.file.vo;

import lombok.Data;

import java.util.List;

/**
 * 文件上传初始化VO
 */
@Data
public class FileUploadInitVO {
    
    /**
     * 上传任务ID
     */
    private String uploadId;
    
    /**
     * 总分片数
     */
    private Integer totalChunks;
    
    /**
     * 分片大小
     */
    private Long chunkSize;
    
    /**
     * 已上传的分片索引列表
     */
    private List<Integer> uploadedChunks;
    
    /**
     * 是否已存在(秒传)
     */
    private Boolean existing;
    
    /**
     * 已存在文件的ID
     */
    private Long existingFileId;
}
