package com.family.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * CORS 跨域配置
 * 
 * 安全策略：只允许明确的、受信任的域名
 * 不再使用通配符模式
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    private final AppProperties appProperties;

    public CorsConfig(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    private List<String> getTrustedOrigins() {
        List<String> origins = appProperties.getCorsAllowedOrigins();
        return origins == null || origins.isEmpty() ? Collections.emptyList() : origins;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                // 允许明确列出的前端地址
                .allowedOrigins(getTrustedOrigins().toArray(new String[0]))
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
        config.setAllowedOriginPatterns(getTrustedOrigins());
        
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
