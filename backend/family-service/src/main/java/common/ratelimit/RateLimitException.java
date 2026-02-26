package com.family.common.ratelimit;

import com.family.common.core.exception.BusinessException;

/**
 * 限流异常
 *
 * @author family
 */
public class RateLimitException extends BusinessException {

    public RateLimitException(String message) {
        super(429, message);
    }

    public RateLimitException() {
        super(429, "请求过于频繁，请稍后再试");
    }
}
