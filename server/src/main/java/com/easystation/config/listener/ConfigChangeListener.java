package com.easystation.config.listener;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.jboss.logging.Logger;

/**
 * 配置变更监听器
 * 
 * 监听配置变更事件，实现热重载
 * 
 * 注意：Redis Pub/Sub 功能需要正确的 Quarkus Redis 客户端配置
 * 当前版本使用简化实现，配置变更时直接从数据库读取
 */
@ApplicationScoped
public class ConfigChangeListener {

    private static final Logger log = Logger.getLogger(ConfigChangeListener.class);

    /**
     * 应用启动后初始化
     */
    @PostConstruct
    void init() {
        log.info("配置变更监听器已初始化");
    }

    /**
     * 应用关闭前清理
     */
    @PreDestroy
    void destroy() {
        log.info("配置变更监听器已关闭");
    }

    /**
     * 收到配置变更消息时的回调
     * 
     * TODO: 实现 Redis Pub/Sub 监听
     * 当前版本配置变更时直接从数据库读取
     */
    public void onConfigChange(String configKey) {
        log.infof("检测到配置变更：%s", configKey);
        // 配置直接从数据库读取，Quarkus Hibernate 会自动刷新
    }

    /**
     * 刷新配置
     */
    public void refreshConfig(String configKey) {
        log.infof("刷新配置：%s", configKey);
    }
}
