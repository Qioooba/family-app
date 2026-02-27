package com.family.common.core.exception;

public class BusinessException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    
    public BusinessException(String message) {
        super(message);
    }
    
    public BusinessException(Integer code, String message) {
        super(message);
    }
}
