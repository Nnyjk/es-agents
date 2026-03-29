package com.easystation.common.cache;

import com.easystation.common.config.RedisConfig;
import com.easystation.config.domain.ConfigItem;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.concurrent.TimeUnit;

/**
 * 配置缓存服务
 * 
 * 用于缓存系统配置项，提升配置读取性能
 */
@ApplicationScoped
public class ConfigCacheService {

    @Inject
    CacheService cacheService;

    /**
     * 缓存配置项
     * 
     * @param configCode 配置编码
     * @param configItem 配置项
     */
    public void setConfig(String configCode, ConfigItem configItem) {
        String key = RedisConfig.CacheKeys.configKey(configCode);
        cacheService.set(key, configItem, RedisConfig.CONFIG_TTL, TimeUnit.SECONDS);
    }

    /**
     * 获取配置项
     * 
     * @param configCode 配置编码
     * @return 配置项
     */
    public ConfigItem getConfig(String configCode) {
        String key = RedisConfig.CacheKeys.configKey(configCode);
        return cacheService.get(key, ConfigItem.class);
    }

    /**
     * 删除配置缓存
     * 
     * @param configCode 配置编码
     */
    public void deleteConfig(String configCode) {
        String key = RedisConfig.CacheKeys.configKey(configCode);
        cacheService.delete(key);
    }

    /**
     * 刷新配置缓存过期时间
     * 
     * @param configCode 配置编码
     */
    public void refreshConfig(String configCode) {
        String key = RedisConfig.CacheKeys.configKey(configCode);
        cacheService.expire(key, RedisConfig.CONFIG_TTL, TimeUnit.SECONDS);
    }

    /**
     * 检查配置缓存是否存在
     * 
     * @param configCode 配置编码
     * @return 是否存在
     */
    public boolean hasConfig(String configCode) {
        String key = RedisConfig.CacheKeys.configKey(configCode);
        return cacheService.exists(key);
    }
}
