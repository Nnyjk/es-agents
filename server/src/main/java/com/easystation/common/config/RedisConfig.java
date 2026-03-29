package com.easystation.common.config;

import io.quarkus.redis.client.RedisClientName;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

/**
 * Redis 配置类
 * 
 * 提供 Redis 客户端的配置和缓存 TTL 设置
 */
@ApplicationScoped
public class RedisConfig {

    /**
     * 会话缓存 TTL（秒）- 30 分钟
     */
    public static final int SESSION_TTL = 1800;

    /**
     * 配置缓存 TTL（秒）- 5 分钟
     */
    public static final int CONFIG_TTL = 300;

    /**
     * 热点数据缓存 TTL（秒）- 1 小时
     */
    public static final int HOT_DATA_TTL = 3600;

    /**
     * 缓存键前缀
     */
    public static class CacheKeys {
        public static final String SESSION_PREFIX = "session:";
        public static final String CONFIG_PREFIX = "config:";
        public static final String USER_PREFIX = "user:";
        public static final String AGENT_PREFIX = "agent:";
        
        public static String sessionKey(String sessionId) {
            return SESSION_PREFIX + sessionId;
        }
        
        public static String configKey(String configCode) {
            return CONFIG_PREFIX + configCode;
        }
        
        public static String userKey(Long userId) {
            return USER_PREFIX + userId;
        }
        
        public static String agentKey(String agentCode) {
            return AGENT_PREFIX + agentCode;
        }
    }
}
