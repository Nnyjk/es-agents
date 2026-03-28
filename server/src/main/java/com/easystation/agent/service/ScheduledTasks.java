package com.easystation.agent.service;

import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

/**
 * 定时任务管理
 */
@Slf4j
@ApplicationScoped
public class ScheduledTasks {

    @Inject
    SystemEventLogService eventLogService;

    /**
     * 每天凌晨 2 点清理 30 天前的日志
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupOldEventLogs() {
        try {
            long deleted = eventLogService.cleanupOldLogs(30);
            log.info("定时清理系统事件日志完成，删除 {} 条记录", deleted);
        } catch (Exception e) {
            log.error("定时清理系统事件日志失败", e);
        }
    }
}
