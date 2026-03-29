package com.easystation.common.ratelimit;

import io.quarkus.logging.Log;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.value.ValueCommands;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.SecurityContext;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * 限流拦截器
 * 
 * 拦截标记了 @RateLimit 注解的方法，实现 API 限流功能
 */
@Interceptor
@RateLimit
@Priority(jakarta.interceptor.Interceptor.Priority.APPLICATION)
public class RateLimitInterceptor {

    @Inject
    RedisDataSource redisDataSource;

    @Inject
    SecurityContext securityContext;

    @Inject
    HttpHeaders httpHeaders;

    @ConfigProperty(name = "rate.limit.enabled", defaultValue = "true")
    boolean rateLimitEnabled;

    @ConfigProperty(name = "rate.limit.default.max-requests", defaultValue = "100")
    int defaultMaxRequests;

    @ConfigProperty(name = "rate.limit.default.window-seconds", defaultValue = "60")
    int defaultWindowSeconds;

    private static final String RATE_LIMIT_KEY_PREFIX = "ratelimit:";

    @AroundInvoke
    public Object intercept(InvocationContext context) throws Exception {
        if (!rateLimitEnabled) {
            return context.proceed();
        }

        RateLimit rateLimit = context.getMethod().getAnnotation(RateLimit.class);
        if (rateLimit == null) {
            rateLimit = context.getMethod().getDeclaringClass().getAnnotation(RateLimit.class);
        }

        if (rateLimit == null) {
            return context.proceed();
        }

        String limitKey = buildLimitKey(rateLimit);
        int maxRequests = rateLimit.maxRequests() > 0 ? rateLimit.maxRequests() : defaultMaxRequests;
        int windowSeconds = rateLimit.windowSeconds() > 0 ? rateLimit.windowSeconds() : defaultWindowSeconds;

        if (isRateLimitExceeded(limitKey, maxRequests, windowSeconds)) {
            Log.warnf("Rate limit exceeded for key: %s, max: %d, window: %ds", limitKey, maxRequests, windowSeconds);
            throw new RateLimitException(limitKey, maxRequests, windowSeconds);
        }

        return context.proceed();
    }

    private String buildLimitKey(RateLimit rateLimit) {
        StringBuilder keyBuilder = new StringBuilder(RATE_LIMIT_KEY_PREFIX);
        keyBuilder.append(rateLimit.key());
        keyBuilder.append(":");
        keyBuilder.append(rateLimit.limitType().name());
        keyBuilder.append(":");

        String identifier = getIdentifier(rateLimit.limitType());
        keyBuilder.append(identifier);

        return keyBuilder.toString();
    }

    private String getIdentifier(RateLimit.LimitType limitType) {
        String ip = getClientIp();
        String username = getUsername();

        return switch (limitType) {
            case IP -> ip;
            case USER -> username != null ? username : "anonymous";
            case API -> "global";
            case IP_API -> ip + ":" + "api";
            case USER_API -> username != null ? username : ip;
        };
    }

    private String getClientIp() {
        String xForwardedFor = httpHeaders.getHeaderString("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = httpHeaders.getHeaderString("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return "unknown";
    }

    private String getUsername() {
        if (securityContext != null && securityContext.getUserPrincipal() != null) {
            return securityContext.getUserPrincipal().getName();
        }
        return null;
    }

    private boolean isRateLimitExceeded(String key, int maxRequests, int windowSeconds) {
        try {
            ValueCommands<String, String> valueCommands = redisDataSource.value(String.class, String.class);

            String currentValue = valueCommands.get(key);
            long count = currentValue != null ? Long.parseLong(currentValue) : 0;
            count++;

            if (count == 1) {
                valueCommands.set(key, "1");
                redisDataSource.key(String.class).expire(key, windowSeconds);
            } else {
                valueCommands.set(key, String.valueOf(count));
            }

            boolean exceeded = count > maxRequests;

            if (exceeded) {
                Log.warnf("Rate limit exceeded: key=%s, count=%d, max=%d", key, count, maxRequests);
            }

            return exceeded;
        } catch (Exception e) {
            Log.warnf("Failed to check rate limit, allowing request: %s", e.getMessage());
            return false;
        }
    }
}
