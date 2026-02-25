package com.family.notify;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 通知服务启动类
 */
@SpringBootApplication(scanBasePackages = "com.family")
@EnableAsync
@EnableScheduling
public class NotifyServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(NotifyServiceApplication.class, args);
    }
}
