package com.easystation.common.cache;

import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.value.ValueCommands;
import io.smallrye.mutiny.Uni;
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
        try {
            String json = objectMapper.writeValueAsString(value);
            getValueCommands().set(key, json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize cache value for key: " + key, e);
        }
    }

    @Override
    public void set(String key, Object value, long ttl, TimeUnit unit) {
        try {
            String json = objectMapper.writeValueAsString(value);
            getValueCommands().set(key, json, unit.toSeconds(ttl));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize cache value for key: " + key, e);
        }
    }

    @Override
    public void delete(String key) {
        getValueCommands().del(key);
    }

    @Override
    public boolean exists(String key) {
        return getValueCommands().get(key) != null;
    }

    @Override
    public <T> Map<String, T> multiGet(List<String> keys, Class<T> clazz) {
        throw new UnsupportedOperationException("multiGet not yet implemented");
    }

    @Override
    public void multiSet(Map<String, Object> map) {
        throw new UnsupportedOperationException("multiSet not yet implemented");
    }

    @Override
    public void multiSet(Map<String, Object> map, long ttl, TimeUnit unit) {
        throw new UnsupportedOperationException("multiSet with TTL not yet implemented");
    }

    @Override
    public long increment(String key) {
        return redisDataSource.key(String.class).incr(key);
    }

    @Override
    public long decrement(String key) {
        return redisDataSource.key(String.class).decr(key);
    }

    @Override
    public void expire(String key, long ttl, TimeUnit unit) {
        redisDataSource.key(String.class).expire(key, unit.toSeconds(ttl));
    }

    @Override
    public long getTTL(String key, TimeUnit unit) {
        Long seconds = redisDataSource.key(String.class).ttl(key);
        if (seconds == null || seconds < 0) {
            return -1;
        }
        return unit.convert(seconds, TimeUnit.SECONDS);
    }
}
