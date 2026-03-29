package com.easystation.common.cache;

import com.easystation.common.config.RedisConfig;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.concurrent.TimeUnit;

/**
 * 会话缓存服务
 * 
 * 用于缓存用户会话数据，替代数据库 session
 */
@ApplicationScoped
public class SessionCacheService {

    @Inject
    CacheService cacheService;

    /**
     * 缓存会话数据
     * 
     * @param sessionId 会话 ID
     * @param data 会话数据
     */
    public void setSession(String sessionId, Object data) {
        String key = RedisConfig.CacheKeys.sessionKey(sessionId);
        cacheService.set(key, data, RedisConfig.SESSION_TTL, TimeUnit.SECONDS);
    }

    /**
     * 获取会话数据
     * 
     * @param sessionId 会话 ID
     * @param clazz 数据类型
     * @return 会话数据
     */
    public <T> T getSession(String sessionId, Class<T> clazz) {
        String key = RedisConfig.CacheKeys.sessionKey(sessionId);
        return cacheService.get(key, clazz);
    }

    /**
     * 删除会话
     * 
     * @param sessionId 会话 ID
     */
    public void deleteSession(String sessionId) {
        String key = RedisConfig.CacheKeys.sessionKey(sessionId);
        cacheService.delete(key);
    }

    /**
     * 刷新会话过期时间
     * 
     * @param sessionId 会话 ID
     */
    public void refreshSession(String sessionId) {
        String key = RedisConfig.CacheKeys.sessionKey(sessionId);
        cacheService.expire(key, RedisConfig.SESSION_TTL, TimeUnit.SECONDS);
    }

    /**
     * 检查会话是否存在
     * 
     * @param sessionId 会话 ID
     * @return 是否存在
     */
    public boolean hasSession(String sessionId) {
        String key = RedisConfig.CacheKeys.sessionKey(sessionId);
        return cacheService.exists(key);
    }
}
