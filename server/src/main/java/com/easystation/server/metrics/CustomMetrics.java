package com.easystation.server.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 自定义业务指标类
 * 提供以下 Prometheus 指标：
 * - api_requests_total: API 请求总数
 * - api_request_duration_seconds: API 请求延迟
 * - deployments_total: 部署总次数
 * - agents_online: 在线 Agent 数量
 * - active_users: 活跃用户数
 */
@ApplicationScoped
public class CustomMetrics {

    @Inject
    MeterRegistry meterRegistry;

    // API 请求计数器
    private Counter apiRequestsTotal;

    // API 请求延迟计时器
    private Timer apiRequestDuration;

    // 部署计数器
    private Counter deploymentsTotal;

    // 在线 Agent 数量
    private final AtomicLong agentsOnline = new AtomicLong(0);

    // 活跃用户数
    private final AtomicLong activeUsers = new AtomicLong(0);

    /**
     * 初始化指标（由 Quarkus 自动调用）
     */
    public void init() {
        // 创建应用标签
        meterRegistry.config().commonTags("application", "easy-station-server");

        // API 请求总数计数器
        apiRequestsTotal = Counter.builder("api_requests_total")
                .description("API 请求总数")
                .register(meterRegistry);

        // API 请求延迟计时器
        apiRequestDuration = Timer.builder("api_request_duration_seconds")
                .description("API 请求延迟")
                .register(meterRegistry);

        // 部署总次数计数器
        deploymentsTotal = Counter.builder("deployments_total")
                .description("部署总次数")
                .register(meterRegistry);

        // 在线 Agent 数量 Gauge
        Gauge.builder("agents_online", agentsOnline, AtomicLong::get)
                .description("在线 Agent 数量")
                .register(meterRegistry);

        // 活跃用户数 Gauge
        Gauge.builder("active_users", activeUsers, AtomicLong::get)
                .description("活跃用户数")
                .register(meterRegistry);

        Log.info("自定义业务指标初始化完成");
    }

    /**
     * 记录 API 请求
     */
    public void recordApiRequest() {
        if (apiRequestsTotal != null) {
            apiRequestsTotal.increment();
        }
    }

    /**
     * 记录 API 请求（带标签）
     * @param method HTTP 方法
     * @param path API 路径
     * @param status HTTP 状态码
     */
    public void recordApiRequest(String method, String path, int status) {
        if (apiRequestsTotal != null) {
            apiRequestsTotal.increment();
        }
    }

    /**
     * 记录 API 请求延迟
     * @param duration 延迟时间（毫秒）
     */
    public void recordApiRequestDuration(long duration) {
        if (apiRequestDuration != null) {
            apiRequestDuration.record(duration, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * 记录部署操作
     */
    public void recordDeployment() {
        if (deploymentsTotal != null) {
            deploymentsTotal.increment();
        }
    }

    /**
     * 更新在线 Agent 数量
     * @param count Agent 数量
     */
    public void updateAgentsOnline(long count) {
        agentsOnline.set(count);
    }

    /**
     * 增加在线 Agent
     */
    public void incrementAgentsOnline() {
        agentsOnline.incrementAndGet();
    }

    /**
     * 减少在线 Agent
     */
    public void decrementAgentsOnline() {
        agentsOnline.decrementAndGet();
    }

    /**
     * 更新活跃用户数
     * @param count 用户数
     */
    public void updateActiveUsers(long count) {
        activeUsers.set(count);
    }

    /**
     * 增加活跃用户
     */
    public void incrementActiveUsers() {
        activeUsers.incrementAndGet();
    }

    /**
     * 减少活跃用户
     */
    public void decrementActiveUsers() {
        activeUsers.decrementAndGet();
    }

    /**
     * 获取当前在线 Agent 数量
     */
    public long getAgentsOnline() {
        return agentsOnline.get();
    }

    /**
     * 获取当前活跃用户数
     */
    public long getActiveUsers() {
        return activeUsers.get();
    }
}