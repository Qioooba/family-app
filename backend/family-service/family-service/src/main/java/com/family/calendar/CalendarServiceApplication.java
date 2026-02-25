package com.family.calendar;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.family"})
@MapperScan("com.family.calendar.mapper")
public class CalendarServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(CalendarServiceApplication.class, args);
    }
}
