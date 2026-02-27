package com.family.family;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 家庭服务启动类
 */
@SpringBootApplication
@ComponentScan("com.family")
@MapperScan("com.family.**.mapper")
@EnableScheduling
public class FamilyServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(FamilyServiceApplication.class, args);
    }
}
