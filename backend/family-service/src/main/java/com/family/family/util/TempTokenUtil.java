package com.family.family.util;

import com.family.common.config.AppProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
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

    @Value("${sa-token.jwt-secret-key:change-me-in-production}")
    private String jwtSecretKey;

    private static final Map<String, TokenEntry> TOKEN_STORE = new ConcurrentHashMap<>();

    private static final long TOKEN_EXPIRE_MILLIS = 3L * 24 * 60 * 60 * 1000; // Token有效期3天
    private static final String TOKEN_TYPE = "temp-login";

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
        long now = System.currentTimeMillis();
        String tempToken = Jwts.builder()
            .claim("uid", userId)
            .claim("typ", TOKEN_TYPE)
            .setIssuedAt(new java.util.Date(now))
            .setExpiration(new java.util.Date(now + TOKEN_EXPIRE_MILLIS))
            .signWith(getSigningKey(), SignatureAlgorithm.HS256)
            .compact();
        log.info("生成临时Token: userId={}", userId);
        return tempToken;
    }

    /**
     * 验证临时Token并获取用户ID
     * @param tempToken 临时Token
     * @return 用户ID，验证失败返回null
     */
    public Long validateTempToken(String tempToken) {
        try {
            Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(tempToken)
                .getBody();
            if (!TOKEN_TYPE.equals(claims.get("typ", String.class))) {
                log.warn("临时Token类型非法");
                return null;
            }
            Long userId = claims.get("uid", Long.class);
            log.info("临时Token验证成功: userId={}", userId);
            return userId;
        } catch (JwtException | IllegalArgumentException e) {
            TokenEntry entry = TOKEN_STORE.get(tempToken);
            if (entry == null || entry.isExpired()) {
                if (entry != null) {
                    TOKEN_STORE.remove(tempToken);
                }
                log.warn("临时Token无效或已过期");
                return null;
            }
            log.info("兼容旧版临时Token验证成功: userId={}", entry.userId);
            return entry.userId;
        } catch (Exception e) {
            log.warn("临时Token验证失败", e);
            return null;
        }
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

    private SecretKey getSigningKey() {
        try {
            byte[] raw = MessageDigest.getInstance("SHA-256")
                .digest(jwtSecretKey.getBytes(StandardCharsets.UTF_8));
            return Keys.hmacShaKeyFor(raw);
        } catch (Exception e) {
            throw new IllegalStateException("无法初始化临时Token签名密钥", e);
        }
    }
}
