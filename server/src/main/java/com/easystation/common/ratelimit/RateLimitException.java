package com.easystation.common.ratelimit;

/**
 * 限流异常
 * 
 * 当请求超出限流阈值时抛出
 */
public class RateLimitException extends RuntimeException {

    private final String limitKey;
    private final int maxRequests;
    private final int windowSeconds;

    public RateLimitException(String limitKey, int maxRequests, int windowSeconds) {
        super(String.format("Rate limit exceeded for key '%s': %d requests per %d seconds",
                limitKey, maxRequests, windowSeconds));
        this.limitKey = limitKey;
        this.maxRequests = maxRequests;
        this.windowSeconds = windowSeconds;
    }

    public String getLimitKey() {
        return limitKey;
    }

    public int getMaxRequests() {
        return maxRequests;
    }

    public int getWindowSeconds() {
        return windowSeconds;
    }
}
