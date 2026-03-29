package com.easystation.plugin.sandbox.exception;

/**
 * 超时异常
 */
public class TimeoutException extends RuntimeException {
    
    public TimeoutException(String message) {
        super(message);
    }
    
    public TimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}
