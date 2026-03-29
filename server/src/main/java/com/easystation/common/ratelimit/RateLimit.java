package com.easystation.common.ratelimit;

import jakarta.interceptor.InterceptorBinding;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * API 限流注解
 * 
 * 用于标记需要限流的接口，支持基于 IP、用户、API 的限流
 */
@InterceptorBinding
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

    /**
     * 限流键前缀
     * 用于区分不同业务场景的限流
     */
    String key() default "api";

    /**
     * 最大请求数
     * 在时间窗口内允许的最大请求次数
     */
    int maxRequests() default 100;

    /**
     * 时间窗口（秒）
     * 限流统计的时间周期
     */
    int windowSeconds() default 60;

    /**
     * 限流类型
     */
    LimitType limitType() default LimitType.IP;

    /**
     * 限流类型枚举
     */
    enum LimitType {
        /**
         * 基于 IP 限流
         */
        IP,

        /**
         * 基于用户限流
         */
        USER,

        /**
         * 基于 API 限流（全局）
         */
        API,

        /**
         * 基于 IP+API 限流
         */
        IP_API,

        /**
         * 基于用户+API 限流
         */
        USER_API
    }
}
