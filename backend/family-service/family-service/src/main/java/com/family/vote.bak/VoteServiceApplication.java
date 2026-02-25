package com.family.vote;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.family"})
@MapperScan("com.family.vote.mapper")
public class VoteServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(VoteServiceApplication.class, args);
    }
}
