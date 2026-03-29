package com.easystation.common.exception;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

/**
 * 未授权异常
 */
public class UnauthorizedException extends WebApplicationException {

    public UnauthorizedException(String message) {
        super(message, Response.Status.UNAUTHORIZED);
    }

    public UnauthorizedException() {
        super(Response.Status.UNAUTHORIZED);
    }
}
