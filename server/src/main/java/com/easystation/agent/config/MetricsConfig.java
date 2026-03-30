package com.easystation.agent.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Micrometer 指标配置类
 * 配置 JVM 和系统指标收集
 */
@ApplicationScoped
public class MetricsConfig {

    @Inject
    MeterRegistry registry;

    /**
     * 配置 JVM 和系统指标
     */
    @PostConstruct
    public void init() {
        // JVM 指标
        new JvmMemoryMetrics().bindTo(registry);
        new JvmGcMetrics().bindTo(registry);
        new JvmThreadMetrics().bindTo(registry);
        
        // 系统指标
        new ProcessorMetrics().bindTo(registry);
    }
}
