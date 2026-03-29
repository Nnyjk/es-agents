package com.easystation.plugin.sandbox.exception;

/**
 * 安全违规异常
 */
public class SecurityViolationException extends RuntimeException {
    
    public SecurityViolationException(String message) {
        super(message);
    }
    
    public SecurityViolationException(String message, Throwable cause) {
        super(message, cause);
    }
}
