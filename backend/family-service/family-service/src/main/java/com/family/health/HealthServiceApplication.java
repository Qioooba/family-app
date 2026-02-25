package com.family.health;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.family"})
@MapperScan("com.family.health.mapper")
public class HealthServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(HealthServiceApplication.class, args);
    }
}
