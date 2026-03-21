package com.family.family.util;

import cn.dev33.satoken.stp.StpUtil;
import com.family.common.config.AppProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 临时Token工具类 - 用于免密登录
 * 使用内存存储（单实例部署）
 */
@Slf4j
@Component
public class TempTokenUtil {

    @Autowired
    private AppProperties appProperties;

    private static final Map<String, TokenEntry> TOKEN_STORE = new ConcurrentHashMap<>();

    private static final long TOKEN_EXPIRE_MILLIS = 3L * 24 * 60 * 60 * 1000; // Token有效期3天

    private static class TokenEntry {
        final Long userId;
        final long expireTime;

        TokenEntry(Long userId, long expireTime) {
            this.userId = userId;
            this.expireTime = expireTime;
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expireTime;
        }
    }

    /**
     * 生成临时Token
     * @param userId 用户ID
     * @return 临时Token
     */
    public String generateTempToken(Long userId) {
        String tempToken = UUID.randomUUID().toString().replace("-", "");
        long expireTime = System.currentTimeMillis() + TOKEN_EXPIRE_MILLIS;

        // 存储到内存
        TOKEN_STORE.put(tempToken, new TokenEntry(userId, expireTime));

        log.info("生成临时Token: userId={}, tempToken={}", userId, tempToken);
        return tempToken;
    }

    /**
     * 验证临时Token并获取用户ID
     * @param tempToken 临时Token
     * @return 用户ID，验证失败返回null
     */
    public Long validateTempToken(String tempToken) {
        TokenEntry entry = TOKEN_STORE.get(tempToken);

        if (entry == null || entry.isExpired()) {
            if (entry != null) {
                TOKEN_STORE.remove(tempToken); // 清理过期Token
            }
            log.warn("临时Token无效或已过期: {}", tempToken);
            return null;
        }

        // 验证成功后删除Token（一次性使用）
        TOKEN_STORE.remove(tempToken);

        log.info("临时Token验证成功: userId={}", entry.userId);
        return entry.userId;
    }

    /**
     * 获取免密登录跳转链接
     * @param userId 用户ID
     * @return 完整的跳转URL
     */
    public String getAutoLoginUrl(Long userId) {
        String tempToken = generateTempToken(userId);
        return String.format("%s/auto-login.html?token=%s", appProperties.getBaseUrl(), tempToken);
    }
}
