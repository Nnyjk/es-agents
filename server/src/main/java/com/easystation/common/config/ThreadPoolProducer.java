package com.easystation.common.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Named;
import jakarta.annotation.PreDestroy;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@ApplicationScoped
public class ThreadPoolProducer {

    private final ExecutorService agentConnectionExecutor = Executors.newCachedThreadPool();

    @Produces
    @ApplicationScoped
    @Named("agentConnectionExecutor")
    public ExecutorService agentConnectionExecutor() {
        return agentConnectionExecutor;
    }

    @PreDestroy
    public void destroy() {
        agentConnectionExecutor.shutdown();
    }
}
