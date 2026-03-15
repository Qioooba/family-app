package com.family.family.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 字符编码配置
 * 确保返回的JSON使用UTF-8编码
 */
@Configuration
public class CharsetConfig implements WebMvcConfigurer {

    @Bean
    public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter() {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        // 设置默认字符集为UTF-8
        converter.setDefaultCharset(StandardCharsets.UTF_8);
        // Spring Boot 3中，使用APPLICATION_JSON（默认就是UTF-8）
        converter.setSupportedMediaTypes(List.of(
            MediaType.APPLICATION_JSON,
            new MediaType("application", "json", StandardCharsets.UTF_8)
        ));
        return converter;
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        // 添加自定义的JSON转换器到最前面
        converters.add(0, mappingJackson2HttpMessageConverter());
    }
    
    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        // 默认使用JSON格式
        configurer.defaultContentType(MediaType.APPLICATION_JSON);
    }
}
