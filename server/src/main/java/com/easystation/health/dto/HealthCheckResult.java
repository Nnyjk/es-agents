package com.easystation.health.dto;

/**
 * 单项健康检查结果
 */
public record HealthCheckResult(
    String name,
    HealthStatus status,
    String message,
    long responseTime
) {
    public static HealthCheckResult up(String name, String message, long responseTime) {
        return new HealthCheckResult(name, HealthStatus.UP, message, responseTime);
    }

    public static HealthCheckResult down(String name, String message, long responseTime) {
        return new HealthCheckResult(name, HealthStatus.DOWN, message, responseTime);
    }

    public static HealthCheckResult unknown(String name, String message, long responseTime) {
        return new HealthCheckResult(name, HealthStatus.UNKNOWN, message, responseTime);
    }
}
