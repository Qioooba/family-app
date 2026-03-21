package com.family.family.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.jwt.StpLogicJwtForSimple;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token JWT 配置
 * 使用 JWT 模式，无需 Redis 存储会话
 */
@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    /**
     * 配置 JWT 认证逻辑
     */
    @Bean
    public StpLogicJwtForSimple stpLogic() {
        // 使用简单 JWT 模式（无状态）
        return new StpLogicJwtForSimple();
    }

    /**
     * 注册 Sa-Token 拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> {
            // 所有需要登录的请求都会经过这里
            // Sa-Token 会自动验证 token
        })).addPathPatterns("/api/**")
           .excludePathPatterns(
               "/api/user/login",
               "/api/user/register",
               "/api/user/send-code",
               "/api/user/reset-password",
               "/api/user/wx/login",
               "/auto-login.html",
               "/swagger-ui/**",
               "/swagger-ui.html",
               "/v3/api-docs/**",
               "/doc.html",
               "/webjars/**"
           );
    }
}
