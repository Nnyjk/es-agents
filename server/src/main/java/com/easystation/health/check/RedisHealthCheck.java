package com.easystation.health.check;

import com.easystation.health.dto.HealthCheckResult;
import io.quarkus.redis.datasource.RedisDataSource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Redis 健康检查
 */
@ApplicationScoped
public class RedisHealthCheck {

    @Inject
    RedisDataSource redisDataSource;

    /**
     * 检查 Redis 连接
     * 
     * @return 健康检查结果
     */
    public HealthCheckResult check() {
        long startTime = System.currentTimeMillis();
        try {
            // 尝试执行 PING 命令
            var commands = redisDataSource.value(String.class, String.class);
            String pingResult = commands.get("health_check_ping");
            // 如果连接成功（即使返回 null），说明 Redis 可用
            long responseTime = System.currentTimeMillis() - startTime;
            return HealthCheckResult.up("Redis", "Redis connection successful", responseTime);
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            return HealthCheckResult.down("Redis", "Redis connection failed: " + e.getMessage(), responseTime);
        }
    }
}
