package com.family.family;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 家庭服务启动类
 */
@SpringBootApplication
@EnableScheduling
public class FamilyServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(FamilyServiceApplication.class, args);
    }
}
