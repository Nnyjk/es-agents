package com.easystation.common.cache;

import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.value.SetArgs;
import io.quarkus.redis.datasource.value.ValueCommands;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Redis 缓存服务实现
 * 
 * 基于 Quarkus Redis Client 实现缓存服务接口
 */
@ApplicationScoped
public class RedisCacheService implements CacheService {

    @Inject
    RedisDataSource redisDataSource;

    @Inject
    ObjectMapper objectMapper;

    private ValueCommands<String, String> getValueCommands() {
        return redisDataSource.value(String.class, String.class);
    }

    @Override
    public <T> T get(String key, Class<T> clazz) {
        String json = getValueCommands().get(key);
        if (json == null) {
            return null;
        }
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize cache value for key: " + key, e);
        }
    }

    @Override
    public void set(String key, Object value) {
        set(key, value, 3600, TimeUnit.SECONDS);
    }

    @Override
    public void set(String key, Object value, long ttl, TimeUnit unit) {
        try {
            String json = objectMapper.writeValueAsString(value);
            SetArgs setArgs = new SetArgs().ex(unit.toSeconds(ttl));
            getValueCommands().set(key, json, setArgs);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize cache value for key: " + key, e);
        }
    }

    @Override
    public void delete(String key) {
        redisDataSource.key(String.class).del(key);
    }

    @Override
    public boolean exists(String key) {
        return getValueCommands().get(key) != null;
    }

    @Override
    public <T> Map<String, T> multiGet(List<String> keys, Class<T> clazz) {
        // TODO: 实现批量获取
        throw new UnsupportedOperationException("multiGet not implemented yet");
    }

    @Override
    public void multiSet(Map<String, Object> map) {
        multiSet(map, 3600, TimeUnit.SECONDS);
    }

    @Override
    public void multiSet(Map<String, Object> map, long ttl, TimeUnit unit) {
        // TODO: 实现批量设置
        throw new UnsupportedOperationException("multiSet not implemented yet");
    }

    @Override
    public long increment(String key) {
        return redisDataSource.string(String.class).incr(key);
    }

    @Override
    public long decrement(String key) {
        return redisDataSource.string(String.class).decr(key);
    }

    @Override
    public void expire(String key, long ttl, TimeUnit unit) {
        redisDataSource.key(String.class).expire(key, unit.toSeconds(ttl));
    }

    @Override
    public long getTTL(String key, TimeUnit unit) {
        Long ttlSeconds = redisDataSource.key(String.class).ttl(key);
        if (ttlSeconds == null || ttlSeconds < 0) {
            return -1;
        }
        return unit.convert(ttlSeconds, TimeUnit.SECONDS);
    }
}
