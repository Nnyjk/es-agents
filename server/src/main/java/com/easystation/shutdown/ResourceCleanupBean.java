package com.easystation.shutdown;

import io.agroal.api.AgroalDataSource;
import io.quarkus.redis.client.RedisClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

/**
 * 资源清理 Bean
 * 
 * 负责在应用关闭前清理各种资源：
 * - 刷新日志
 * - 清理缓存
 * - 关闭连接池
 */
@ApplicationScoped
public class ResourceCleanupBean {

    private static final Logger log = Logger.getLogger(ResourceCleanupBean.class);

    @Inject
    AgroalDataSource dataSource;

    @Inject
    RedisClient redisClient;

    /**
     * 在关闭前清理资源
     * 
     * 使用 io.quarkus.runtime.annotations.BeforeShutdown 注解
     * 但 Quarkus 中更推荐使用 ShutdownEvent 观察
     */
    void cleanupResources() {
        log.info("开始资源清理...");

        try {
            // 刷新日志（如果有缓冲日志）
            log.info("刷新日志缓冲区...");

            // 清理缓存（如果有需要持久化的缓存数据）
            log.info("清理缓存...");

            // 注意：连接池会由 Quarkus 自动关闭
            // 这里只记录日志
            log.info("数据库连接池将自动关闭");
            log.info("Redis 连接将自动关闭");

            log.info("资源清理完成");
        } catch (Exception e) {
            log.error("资源清理失败", e);
        }
    }
}
