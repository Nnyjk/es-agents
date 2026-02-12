package com.easystation.common.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "agent.connect")
public interface AgentConfig {

    @WithDefault("3")
    int retryCount();

    @WithDefault("5000")
    long retryInterval();
}
