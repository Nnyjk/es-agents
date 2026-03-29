package com.easystation.plugin.sandbox.exception;

/**
 * 资源超限异常
 */
public class ResourceLimitExceededException extends RuntimeException {
    
    public ResourceLimitExceededException(String message) {
        super(message);
    }
    
    public ResourceLimitExceededException(String message, Throwable cause) {
        super(message, cause);
    }
}
