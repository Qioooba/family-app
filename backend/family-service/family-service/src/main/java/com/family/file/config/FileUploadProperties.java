package com.family.file.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 文件上传配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "file.upload")
public class FileUploadProperties {
    
    /**
     * 分片大小(字节)
     */
    private Long chunkSize = 5 * 1024 * 1024L; // 5MB
    
    /**
     * 最大文件大小(字节)
     */
    private Long maxFileSize = 1024 * 1024 * 1024L; // 1GB
    
    /**
     * 临时文件过期时间(小时)
     */
    private Integer tempExpireHours = 24;
}
