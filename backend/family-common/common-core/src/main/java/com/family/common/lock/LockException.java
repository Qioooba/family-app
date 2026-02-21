package com.family.common.lock;

/**
 * 分布式锁异常
 *
 * @author family
 */
public class LockException extends RuntimeException {
    
    public LockException(String message) {
        super(message);
    }
    
    public LockException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public LockException(Throwable cause) {
        super(cause);
    }
}