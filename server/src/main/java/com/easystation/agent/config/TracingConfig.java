package com.easystation.agent.config;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

/**
 * 链路追踪配置
 * 配置 OpenTelemetry 追踪参数
 */
@ConfigMapping(prefix = "tracing")
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
public interface TracingConfig {

    /**
     * 是否启用链路追踪
     */
    @WithDefault("true")
    boolean enabled();

    /**
     * 服务名称
     */
    @WithDefault("es-agents-server")
    String serviceName();

    /**
     * Jaeger 端点 URL
     */
    @WithDefault("http://localhost:4317")
    String jaegerEndpoint();

    /**
     * 采样率 (0.0-1.0)
     */
    @WithDefault("1.0")
    double sampleRate();

    /**
     * 是否导出到控制台（调试用）
     */
    @WithDefault("false")
    boolean consoleExporter();
}
