package com.easystation.common.cache;

import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.value.SetArgs;
import io.quarkus.redis.datasource.value.ValueCommands;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

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
    public void increment(String key) {
        redisDataSource.string(String.class).incr(key);
    }

    @Override
    public void decrement(String key) {
        redisDataSource.string(String.class).decr(key);
    }

    @Override
    public Long getCounter(String key) {
        String value = getValueCommands().get(key);
        if (value == null) {
            return null;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
