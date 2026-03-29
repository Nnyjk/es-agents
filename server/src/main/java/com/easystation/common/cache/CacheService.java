package com.easystation.common.cache;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 缓存服务接口
 * 
 * 提供统一的缓存操作 API，支持多种缓存实现
 */
public interface CacheService {

    /**
     * 获取缓存值
     * 
     * @param key 缓存键
     * @param clazz 返回值类型
     * @return 缓存值，不存在返回 null
     */
    <T> T get(String key, Class<T> clazz);

    /**
     * 设置缓存值（使用默认 TTL）
     * 
     * @param key 缓存键
     * @param value 缓存值
     */
    void set(String key, Object value);

    /**
     * 设置缓存值（指定 TTL）
     * 
     * @param key 缓存键
     * @param value 缓存值
     * @param ttl 过期时间
     * @param unit 时间单位
     */
    void set(String key, Object value, long ttl, TimeUnit unit);

    /**
     * 删除缓存
     * 
     * @param key 缓存键
     */
    void delete(String key);

    /**
     * 检查键是否存在
     * 
     * @param key 缓存键
     * @return 是否存在
     */
    boolean exists(String key);

    /**
     * 获取多个键的值
     * 
     * @param keys 缓存键列表
     * @param clazz 返回值类型
     * @return 缓存值 Map
     */
    <T> Map<String, T> multiGet(List<String> keys, Class<T> clazz);

    /**
     * 设置多个键值对
     * 
     * @param map 键值对 Map
     */
    void multiSet(Map<String, Object> map);

    /**
     * 设置多个键值对（指定 TTL）
     * 
     * @param map 键值对 Map
     * @param ttl 过期时间
     * @param unit 时间单位
     */
    void multiSet(Map<String, Object> map, long ttl, TimeUnit unit);

    /**
     * 原子递增
     * 
     * @param key 缓存键
     * @return 递增后的值
     */
    long increment(String key);

    /**
     * 原子递减
     * 
     * @param key 缓存键
     * @return 递减后的值
     */
    long decrement(String key);

    /**
     * 设置键的过期时间
     * 
     * @param key 缓存键
     * @param ttl 过期时间
     * @param unit 时间单位
     */
    void expire(String key, long ttl, TimeUnit unit);

    /**
     * 获取键的剩余过期时间
     * 
     * @param key 缓存键
     * @param unit 时间单位
     * @return 剩余时间
     */
    long getTTL(String key, TimeUnit unit);
}
