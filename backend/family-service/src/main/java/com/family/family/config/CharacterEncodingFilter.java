package com.family.family.config;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * UTF-8 字符编码过滤器
 * 确保所有请求和响应都使用 UTF-8 编码
 */
@Component
@Order(1) // 确保最先执行
@WebFilter(filterName = "CharacterEncodingFilter", urlPatterns = "/*")
public class CharacterEncodingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        // 设置请求编码
        httpRequest.setCharacterEncoding(StandardCharsets.UTF_8.name());
        
        // 设置响应编码
        httpResponse.setCharacterEncoding(StandardCharsets.UTF_8.name());
        
        // 设置 Content-Type 头部（如果尚未设置）
        String contentType = httpResponse.getContentType();
        if (contentType == null || contentType.isEmpty()) {
            httpResponse.setContentType("application/json;charset=UTF-8");
        }
        
        chain.doFilter(request, response);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // 初始化逻辑
    }

    @Override
    public void destroy() {
        // 销毁逻辑
    }
}
