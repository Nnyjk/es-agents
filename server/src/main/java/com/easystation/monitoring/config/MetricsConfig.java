package com.easystation.monitoring.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.quarkus.logging.Log;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

/**
 * 指标配置类
 * 在应用启动时配置 Prometheus 指标
 */
@ApplicationScoped
public class MetricsConfig {

    @Inject
    MeterRegistry meterRegistry;

    /**
     * 应用启动时初始化指标配置
     */
    void onStart(@Observes StartupEvent event) {
        // 配置通用标签
        meterRegistry.config().commonTags(
            "application", "easy-station-server",
            "version", "1.0.0"
        );

        Log.info("Prometheus 指标配置完成，端点: /q/metrics");
    }
}