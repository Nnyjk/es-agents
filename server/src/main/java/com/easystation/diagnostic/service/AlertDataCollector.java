package com.easystation.diagnostic.service;

import com.easystation.alert.domain.AlertEvent;
import com.easystation.alert.enums.AlertLevel;
import com.easystation.alert.enums.AlertStatus;
import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDateTime;

/**
 * 告警数据收集器
 * 为诊断引擎提供告警相关数据
 */
@ApplicationScoped
public class AlertDataCollector {

    /**
     * 获取最近24小时的告警数量
     */
    public long getAlertCountLast24Hours() {
        LocalDateTime since = LocalDateTime.now().minusHours(24);
        return AlertEvent.count("createdAt >= :since", Parameters.with("since", since));
    }

    /**
     * 获取当前活跃告警数量（待处理和已通知状态）
     */
    public long getActiveAlertCount() {
        return AlertEvent.count("status in ?1", 
                java.util.List.of(AlertStatus.PENDING, AlertStatus.NOTIFIED));
    }

    /**
     * 获取严重级别告警数量
     */
    public long getCriticalAlertCount() {
        return AlertEvent.count("level", AlertLevel.CRITICAL);
    }

    /**
     * 获取最近24小时按级别统计的告警数量
     */
    public long getAlertCountByLevelLast24Hours(AlertLevel level) {
        LocalDateTime since = LocalDateTime.now().minusHours(24);
        return AlertEvent.count("createdAt >= :since and level = :level", 
                Parameters.with("since", since).and("level", level));
    }

    /**
     * 获取告警趋势数据
     */
    public AlertTrend getAlertTrend() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime hour1 = now.minusHours(1);
        LocalDateTime hour24 = now.minusHours(24);
        
        long count1h = AlertEvent.count("createdAt >= :since", Parameters.with("since", hour1));
        long count24h = AlertEvent.count("createdAt >= :since", Parameters.with("since", hour24));
        
        return new AlertTrend(count1h, count24h);
    }

    public record AlertTrend(long lastHour, long last24Hours) {}
}
