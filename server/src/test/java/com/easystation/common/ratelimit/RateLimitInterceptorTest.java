package com.easystation.common.ratelimit;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;

/**
 * 限流拦截器测试
 * 
 * 验证限流注解和异常类的基本功能
 * 注：集成测试将在后续使用 JAX-RS Filter 方式实现
 */
@QuarkusTest
public class RateLimitInterceptorTest {

    @Test
    public void testRateLimitAnnotationExists() {
        // 验证限流注解存在且配置正确
        assertNotNull(RateLimit.class);
        assertNotNull(RateLimit.LimitType.class);
        
        RateLimit.LimitType[] types = RateLimit.LimitType.values();
        assertEquals(5, types.length);
        assertTrue(Arrays.asList(types).contains(RateLimit.LimitType.IP));
        assertTrue(Arrays.asList(types).contains(RateLimit.LimitType.USER));
        assertTrue(Arrays.asList(types).contains(RateLimit.LimitType.API));
    }

    @Test
    public void testRateLimitException() {
        // 验证限流异常类
        RateLimitException exception = new RateLimitException("test-key", 10, 60);
        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("Rate limit exceeded"));
        assertTrue(exception.getMessage().contains("test-key"));
        assertEquals(10, exception.getMaxRequests());
        assertEquals(60, exception.getWindowSeconds());
    }
}
