package com.family.wish;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.family"})
@MapperScan("com.family.wish.mapper")
public class WishServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(WishServiceApplication.class, args);
    }
}
