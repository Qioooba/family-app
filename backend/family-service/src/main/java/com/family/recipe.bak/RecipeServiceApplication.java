package com.family.recipe;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.family"})
@MapperScan("com.family.recipe.mapper")
public class RecipeServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(RecipeServiceApplication.class, args);
    }
}
