package com.family.anniversary;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.family"})
@MapperScan("com.family.anniversary.mapper")
public class AnniversaryServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AnniversaryServiceApplication.class, args);
    }
}
