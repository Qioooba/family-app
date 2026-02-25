package com.family.file.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 文件存储配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "file.storage")
public class FileStorageProperties {
    
    /**
     * 本地存储配置
     */
    private LocalStorage local = new LocalStorage();
    
    /**
     * 访问URL前缀
     */
    private String urlPrefix;
    
    @Data
    public static class LocalStorage {
        /**
         * 存储路径
         */
        private String path;
        
        /**
         * 分片存储路径
         */
        private String chunkPath;
    }
}
