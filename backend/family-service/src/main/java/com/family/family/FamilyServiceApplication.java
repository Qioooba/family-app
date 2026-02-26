package com.family.family;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ComponentScan(basePackages = {
    "com.family.user",
    "com.family.common"
})
@MapperScan("com.family.user.mapper")
@EnableScheduling
public class FamilyServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(FamilyServiceApplication.class, args);
    }
}
