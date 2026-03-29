package com.easystation.common.exception;

/**
 * 未授权异常
 * 
 * 当用户未认证或认证失败时抛出
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }

    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}
