package com.family.task.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Jackson 配置类 - 处理 Java 8 日期时间类型
 */
@Configuration
public class JacksonConfig {

    private static final String DATETIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss";

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // 注册 JavaTimeModule 处理 JDK8 日期时间类型
        JavaTimeModule javaTimeModule = new JavaTimeModule();

        // 自定义 LocalDateTime 序列化和反序列化格式 (ISO 8601)
        javaTimeModule.addSerializer(LocalDateTime.class,
            new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DATETIME_PATTERN)));
        javaTimeModule.addDeserializer(LocalDateTime.class,
            new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(DATETIME_PATTERN)));

        mapper.registerModule(javaTimeModule);

        // 禁用将日期写为时间戳格式
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        return mapper;
    }
}
