package com.easystation.common.cache;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Redis 缓存服务测试
 */
@QuarkusTest
public class RedisCacheServiceTest {

    @Inject
    CacheService cacheService;

    @Test
    public void testSetAndGet() {
        String key = "test:key";
        String value = "test-value";

        cacheService.set(key, value);
        String retrieved = cacheService.get(key, String.class);
        
        assertEquals(value, retrieved);
        
        cacheService.delete(key);
    }

    @Test
    public void testSetWithTTL() throws InterruptedException {
        String key = "test:ttl";
        String value = "ttl-value";

        cacheService.set(key, value, 2, TimeUnit.SECONDS);
        
        String retrieved = cacheService.get(key, String.class);
        assertEquals(value, retrieved);
        
        Thread.sleep(2500);
        
        retrieved = cacheService.get(key, String.class);
        assertNull(retrieved);
    }

    @Test
    public void testExists() {
        String key = "test:exists";
        String value = "exists-value";

        assertFalse(cacheService.exists(key));
        
        cacheService.set(key, value);
        assertTrue(cacheService.exists(key));
        
        cacheService.delete(key);
        assertFalse(cacheService.exists(key));
    }

    @Test
    public void testIncrement() {
        String key = "test:counter";
        
        cacheService.delete(key);
        
        long val1 = cacheService.increment(key);
        assertEquals(1, val1);
        
        long val2 = cacheService.increment(key);
        assertEquals(2, val2);
        
        cacheService.delete(key);
    }

    @Test
    public void testExpire() throws InterruptedException {
        String key = "test:expire";
        String value = "expire-value";

        cacheService.set(key, value);
        cacheService.expire(key, 2, TimeUnit.SECONDS);
        
        assertTrue(cacheService.exists(key));
        
        Thread.sleep(2500);
        
        assertFalse(cacheService.exists(key));
    }

    @Test
    public void testGetTTL() {
        String key = "test:getttl";
        String value = "getttl-value";

        cacheService.set(key, value, 10, TimeUnit.SECONDS);
        
        long ttl = cacheService.getTTL(key, TimeUnit.SECONDS);
        assertTrue(ttl > 0 && ttl <= 10);
        
        cacheService.delete(key);
    }
}
