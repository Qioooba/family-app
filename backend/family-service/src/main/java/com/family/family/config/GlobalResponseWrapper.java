package com.family.family.config;

import com.family.common.core.Result;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * 全局响应包装器
 * 将Controller返回的Map<String, Object>包装为Result格式
 * 保持已使用Result的接口不变
 */
@RestControllerAdvice
public class GlobalResponseWrapper implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        // 只处理返回值类型，不处理void和基本类型
        Class<?> returnClass = returnType.getParameterType();
        return !returnClass.equals(void.class)
            && !returnClass.equals(Void.class)
            && !returnClass.equals(String.class)
            && !returnClass.equals(Integer.class)
            && !returnClass.equals(Long.class)
            && !returnClass.equals(Boolean.class);
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                   Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                   ServerHttpRequest request, ServerHttpResponse response) {
        // 如果已经是Result类型，直接返回
        if (body instanceof Result) {
            return body;
        }

        // 如果是Map类型，包装为Result
        if (body instanceof java.util.Map) {
            java.util.Map<?, ?> mapBody = (java.util.Map<?, ?>) body;

            // 检查是否已经是Result格式 (包含code, message, data字段)
            if (mapBody.containsKey("code") && mapBody.containsKey("message")) {
                // 已经是Result格式，创建新的Result保持兼容
                Integer code = getIntegerFromMap(mapBody, "code");
                String message = getStringFromMap(mapBody, "message");
                Object data = mapBody.get("data");

                if (code == null) {
                    return Result.success(message, data);
                } else if (code == 0 || code == 200) {
                    return Result.success(message, data);
                } else {
                    return Result.error(code, message);
                }
            }

            // 普通Map包装为成功响应
            return Result.success(body);
        }

        // 其他类型直接返回
        return body;
    }

    private Integer getIntegerFromMap(java.util.Map<?, ?> map, String key) {
        Object value = map.get(key);
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof Number) {
            return ((Number) value).intValue();
        } else if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private String getStringFromMap(java.util.Map<?, ?> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }
}
