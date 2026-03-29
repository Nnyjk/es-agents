package com.easystation.health.dto;

import java.util.List;

/**
 * 健康汇总信息
 */
public record HealthSummary(
    HealthStatus overallStatus,
    long timestamp,
    List<HealthCheckResult> checks,
    int totalChecks,
    int healthyChecks,
    int unhealthyChecks
) {
    public static HealthSummary create(List<HealthCheckResult> checks) {
        int total = checks.size();
        int healthy = (int) checks.stream().filter(c -> c.status() == HealthStatus.UP).count();
        int unhealthy = (int) checks.stream().filter(c -> c.status() == HealthStatus.DOWN).count();
        
        HealthStatus overall = unhealthy > 0 ? HealthStatus.DOWN : HealthStatus.UP;
        if (healthy == 0 && unhealthy == 0) {
            overall = HealthStatus.UNKNOWN;
        }
        
        return new HealthSummary(overall, System.currentTimeMillis(), checks, total, healthy, unhealthy);
    }
}
