package com.family.message;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 消息服务启动类
 */
@SpringBootApplication(scanBasePackages = {"com.family.message", "com.family.common"})
@MapperScan("com.family.message.mapper")
public class MessageServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(MessageServiceApplication.class, args);
    }
}
