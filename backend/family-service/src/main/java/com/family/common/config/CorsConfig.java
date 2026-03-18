package com.family.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

/**
 * CORS 跨域配置
 * 
 * 安全策略：只允许明确的、受信任的域名
 * 不再使用通配符模式
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    /**
     * 信任的前端域名列表
     * 生产环境应只保留 qioba.cn 相关域名
     */
    private static final List<String> TRUSTED_ORIGINS = Arrays.asList(
        // 本地开发
        "http://localhost:3000",
        "http://localhost:3001",
        "http://localhost:3002",
        "http://localhost:3003",
        "http://127.0.0.1:3000",
        "http://127.0.0.1:3001",
        // 局域网开发
        "http://192.168.1.209:3000",
        // 生产域名
        "http://qioba.cn",
        "https://qioba.cn",
        "http://www.qioba.cn",
        "https://www.qioba.cn",
        "http://qioba.cn:3000",
        "https://qioba.cn:3000",
        "http://qioba.cn:8080",
        "https://qioba.cn:8443"
    );

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                // 允许明确列出的前端地址
                .allowedOrigins(TRUSTED_ORIGINS.toArray(new String[0]))
                // 允许的 HTTP 方法
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                // 允许的请求头
                .allowedHeaders("Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin")
                // 允许携带凭证
                .allowCredentials(true)
                // 预检请求缓存时间（秒）
                .maxAge(3600)
                // 暴露响应头（允许前端访问的响应头）
                .exposedHeaders("Authorization", "X-Total-Count");
    }

    /**
     * CORS Filter 配置
     */
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        
        // 使用信任的域名列表，不使用通配符
        config.setAllowedOriginPatterns(TRUSTED_ORIGINS);
        
        // 只允许必要的 HTTP 方法
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        
        // 只允许必要的请求头
        config.setAllowedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type", 
            "X-Requested-With",
            "Accept",
            "Origin",
            "Referer",
            "User-Agent"
        ));
        
        // 允许暴露的响应头
        config.setExposedHeaders(Arrays.asList(
            "Authorization",
            "X-Total-Count",
            "Content-Disposition"
        ));
        
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}
