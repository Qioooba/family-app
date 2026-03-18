package com.family.common.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Jackson 配置类
 * 用于处理 Java 8 日期时间类型的序列化和反序列化
 * 
 * 安全配置：
 * 1. 禁用默认类型信息（防止反序列化攻击）
 * 2. 忽略未知属性
 * 3. 不序列化 null 值
 */
@Configuration
public class JacksonConfig {

    /**
     * 自定义 LocalDateTime 反序列化器 - 支持多种日期格式
     */
    public static class CustomLocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {
        private static final DateTimeFormatter[] FORMATTERS = new DateTimeFormatter[] {
            DateTimeFormatter.ISO_LOCAL_DATE_TIME,                    // 2026-03-08T15:00:00
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),       // 2026-03-08 15:00:00
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),          // 2026-03-08 15:00
            DateTimeFormatter.ofPattern("yyyy-MM-dd")                  // 2026-03-08
        };
        
        @Override
        public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            String text = p.getValueAsString();
            if (text == null || text.isEmpty()) {
                return null;
            }
            
            for (DateTimeFormatter formatter : FORMATTERS) {
                try {
                    return LocalDateTime.parse(text, formatter);
                } catch (DateTimeParseException ignored) {
                    // 尝试下一个格式
                }
            }
            
            throw new IOException("无法解析日期时间: " + text + "，支持的格式: yyyy-MM-ddTHH:mm:ss, yyyy-MM-dd HH:mm:ss, yyyy-MM-dd HH:mm, yyyy-MM-dd");
        }
    }
    
    /**
     * 安全的 ObjectMapper Bean
     * 禁用不安全的配置防止反序列化攻击
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        
        // 注册 JavaTimeModule 以支持 Java 8 日期时间类型
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        
        // 序列化格式：yyyy-MM-dd HH:mm:ss
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(formatter));
        
        // 反序列化：使用自定义反序列化器
        javaTimeModule.addDeserializer(LocalDateTime.class, new CustomLocalDateTimeDeserializer());
        
        mapper.registerModule(javaTimeModule);
        
        // ========== 安全配置 ==========
        
        // 1. 禁用将日期写入为时间戳的形式
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        // 2. 忽略未知属性（防止因前端字段变化导致反序列化失败）
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        
        // 3. 不序列化 null 值（减少响应大小）
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        
        // 5. 禁用 Jackson 的类型信息（防止反序列化攻击）
        // 重要：关闭 default typing 防止通过 @class 字段执行任意类
        mapper.deactivateDefaultTyping();
        
        // 6. 不允许单引号作为属性名
        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, false);
        
        return mapper;
    }
}
