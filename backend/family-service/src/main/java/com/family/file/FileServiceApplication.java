package com.family.file;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 文件服务启动类
 */
@SpringBootApplication(scanBasePackages = "com.family")
public class FileServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(FileServiceApplication.class, args);
    }
}
