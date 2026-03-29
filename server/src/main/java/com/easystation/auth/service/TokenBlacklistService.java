package com.easystation.auth.service;

import com.easystation.common.cache.RedisCacheService;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.concurrent.TimeUnit;

/**
 * Token 黑名单服务
 * 
 * 使用 Redis 存储已注销的 JWT Token，防止 Token 被重复使用
 */
@ApplicationScoped
public class TokenBlacklistService {

    private static final String BLACKLIST_KEY_PREFIX = "token:blacklist:";

    @Inject
    RedisCacheService redisCacheService;

    @ConfigProperty(name = "auth.jwt.blacklist.enabled", defaultValue = "true")
    boolean blacklistEnabled;

    @ConfigProperty(name = "auth.jwt.blacklist.ttl", defaultValue = "86400")
    long blacklistTtlSeconds;

    /**
     * 将 Token 加入黑名单
     * 
     * @param tokenHash Token 的哈希值
     * @param ttlSeconds 黑名单存活时间（秒）
     */
    public void addToBlacklist(String tokenHash, long ttlSeconds) {
        if (!blacklistEnabled) {
            Log.debugf("Token blacklist is disabled, skipping add to blacklist: %s", tokenHash);
            return;
        }

        String key = BLACKLIST_KEY_PREFIX + tokenHash;
        redisCacheService.set(key, "blacklisted", ttlSeconds, TimeUnit.SECONDS);
        Log.debugf("Token added to blacklist: %s (TTL: %ds)", tokenHash, ttlSeconds);
    }

    /**
     * 将 Token 加入黑名单（使用默认 TTL）
     * 
     * @param tokenHash Token 的哈希值
     */
    public void addToBlacklist(String tokenHash) {
        addToBlacklist(tokenHash, blacklistTtlSeconds);
    }

    /**
     * 检查 Token 是否在黑名单中
     * 
     * @param tokenHash Token 的哈希值
     * @return true 如果在黑名单中，false 否则
     */
    public boolean isBlacklisted(String tokenHash) {
        if (!blacklistEnabled) {
            Log.debugf("Token blacklist is disabled, allowing token: %s", tokenHash);
            return false;
        }

        String key = BLACKLIST_KEY_PREFIX + tokenHash;
        String value = redisCacheService.get(key, String.class);
        boolean blacklisted = "blacklisted".equals(value);
        
        if (blacklisted) {
            Log.debugf("Token is blacklisted: %s", tokenHash);
        }
        
        return blacklisted;
    }

    /**
     * 从黑名单中移除 Token
     * 
     * @param tokenHash Token 的哈希值
     */
    public void removeFromBlacklist(String tokenHash) {
        if (!blacklistEnabled) {
            return;
        }

        String key = BLACKLIST_KEY_PREFIX + tokenHash;
        redisCacheService.delete(key);
        Log.debugf("Token removed from blacklist: %s", tokenHash);
    }

    /**
     * 检查黑名单功能是否启用
     * 
     * @return true 如果启用，false 否则
     */
    public boolean isEnabled() {
        return blacklistEnabled;
    }
}
