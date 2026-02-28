package com.family.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

/**
 * CORS 跨域配置
 * 允许前端 localhost:3000 和局域网访问后端服务
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                // 允许的前端地址
                .allowedOrigins(
                    "http://localhost:3000",
                    "http://localhost:3001",
                    "http://localhost:3002",
                    "http://localhost:3003",
                    "http://127.0.0.1:3000",
                    "http://192.168.1.209:3000",
                    "http://192.168.1.*:3000",
                    "http://192.168.*.*:3000",
                    "http://10.0.0.*:3000",
                    "http://10.*.*.*:3000",
                    "http://qioba.cn",
                    "https://qioba.cn",
                    "http://www.qioba.cn",
                    "https://www.qioba.cn",
                    "http://qioba.cn:3000",
                    "https://qioba.cn:3000"
                )
                // 允许的 HTTP 方法
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                // 允许的请求头
                .allowedHeaders("*")
                // 允许携带凭证（如 cookies、authorization header）
                .allowCredentials(true)
                // 预检请求缓存时间（秒）
                .maxAge(3600);
    }

    /**
     * 配置 CORS Filter（作为备用方案）
     */
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        
        // 允许所有常见的前端地址
        config.setAllowedOriginPatterns(Arrays.asList(
            "http://localhost:*",
            "http://127.0.0.1:*",
            "http://192.168.*.*:*",
            "http://10.*.*.*:*",
            "http://*.*.*.*:*",
            "http://qioba.cn",
            "https://qioba.cn",
            "http://*.qioba.cn",
            "https://*.qioba.cn",
            "http://qioba.cn:*",
            "https://qioba.cn:*",
            "http://*.qioba.cn:*",
            "https://*.qioba.cn:*"
        ));
        
        config.addAllowedMethod("*");
        config.addAllowedHeader("*");
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}
