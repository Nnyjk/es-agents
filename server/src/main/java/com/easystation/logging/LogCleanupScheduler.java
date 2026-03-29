package com.easystation.logging;

import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

/**
 * 日志清理定时任务
 */
@ApplicationScoped
public class LogCleanupScheduler {
    
    private static final Logger LOG = Logger.getLogger(LogCleanupScheduler.class);
    
    @Inject
    LogManagementService logManagementService;
    
    @ConfigProperty(name = "app.log.cleanup.enabled", defaultValue = "true")
    boolean cleanupEnabled;
    
    @ConfigProperty(name = "app.log.cleanup.retention-days", defaultValue = "30")
    int retentionDays;
    
    /**
     * 每天凌晨 2 点执行日志清理
     * Cron 表达式：0 0 2 * * ?
     */
    @Scheduled(cron = "{app.log.cleanup.cron:0 0 2 * * ?}")
    public void cleanupOldLogs() {
        if (!cleanupEnabled) {
            LOG.info("日志清理功能已禁用");
            return;
        }
        
        LOG.infof("开始执行日志清理任务，保留最近 %d 天的归档", retentionDays);
        
        try {
            int deletedCount = logManagementService.cleanupOldArchives(retentionDays);
            LOG.infof("日志清理任务完成，删除 %d 个过期归档文件", deletedCount);
        } catch (Exception e) {
            LOG.errorf("日志清理任务执行失败：%s", e.getMessage());
        }
    }
}
