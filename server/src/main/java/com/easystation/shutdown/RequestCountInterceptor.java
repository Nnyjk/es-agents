package com.easystation.shutdown;

import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;

/**
 * 请求计数拦截器
 * 
 * 用于跟踪在途请求数量，支持优雅关闭。
 * 在请求开始时增加计数，在请求结束时减少计数。
 */
@Provider
public class RequestCountInterceptor implements ContainerRequestFilter, ContainerResponseFilter {

    @Inject
    GracefulShutdownListener shutdownListener;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        // 请求开始时增加计数
        shutdownListener.incrementActiveRequests();
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        // 请求结束时减少计数
        shutdownListener.decrementActiveRequests();
    }
}
