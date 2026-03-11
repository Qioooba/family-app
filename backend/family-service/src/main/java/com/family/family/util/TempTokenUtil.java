package com.family.family.util;

import cn.dev33.satoken.stp.StpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 临时Token工具类 - 用于免密登录
 */
@Slf4j
@Component
public class TempTokenUtil {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String TEMP_TOKEN_PREFIX = "temp:token:";
    private static final long TOKEN_EXPIRE_MINUTES = 30; // Token有效期30分钟

    /**
     * 生成临时Token
     * @param userId 用户ID
     * @return 临时Token
     */
    public String generateTempToken(Long userId) {
        String tempToken = UUID.randomUUID().toString().replace("-", "");
        String key = TEMP_TOKEN_PREFIX + tempToken;
        
        // 存储到Redis，设置过期时间
        redisTemplate.opsForValue().set(key, String.valueOf(userId), TOKEN_EXPIRE_MINUTES, TimeUnit.MINUTES);
        
        log.info("生成临时Token: userId={}, tempToken={}", userId, tempToken);
        return tempToken;
    }

    /**
     * 验证临时Token并获取用户ID
     * @param tempToken 临时Token
     * @return 用户ID，验证失败返回null
     */
    public Long validateTempToken(String tempToken) {
        String key = TEMP_TOKEN_PREFIX + tempToken;
        String userIdStr = redisTemplate.opsForValue().get(key);
        
        if (userIdStr == null) {
            log.warn("临时Token无效或已过期: {}", tempToken);
            return null;
        }
        
        // 验证成功后删除Token（一次性使用）
        redisTemplate.delete(key);
        
        Long userId = Long.valueOf(userIdStr);
        log.info("临时Token验证成功: userId={}", userId);
        return userId;
    }

    /**
     * 获取免密登录跳转链接
     * @param userId 用户ID
     * @return 完整的跳转URL
     */
    public String getAutoLoginUrl(Long userId) {
        String tempToken = generateTempToken(userId);
        return String.format("https://qioba.cn:8443/auto-login.html?token=%s", tempToken);
    }
}
