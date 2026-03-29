package com.easystation.health.resource;

import com.easystation.health.check.AgentHealthCheck;
import com.easystation.health.check.DatabaseHealthCheck;
import com.easystation.health.check.RedisHealthCheck;
import com.easystation.health.dto.HealthCheckResult;
import com.easystation.health.dto.HealthSummary;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.ArrayList;
import java.util.List;

/**
 * 健康检查 REST API
 */
@Path("/api/v1/health")
@Produces(MediaType.APPLICATION_JSON)
public class HealthResource {

    @Inject
    DatabaseHealthCheck databaseHealthCheck;

    @Inject
    RedisHealthCheck redisHealthCheck;

    @Inject
    AgentHealthCheck agentHealthCheck;

    /**
     * 获取总体健康状态
     */
    @GET
    public HealthSummary getHealth() {
        List<HealthCheckResult> checks = new ArrayList<>();
        checks.add(databaseHealthCheck.check());
        checks.add(redisHealthCheck.check());
        checks.add(agentHealthCheck.check());
        return HealthSummary.create(checks);
    }

    /**
     * 存活检查 (Kubernetes liveness probe)
     */
    @GET
    @Path("/live")
    public HealthCheckResult getLiveness() {
        return HealthCheckResult.up("live", "Service is alive", 0);
    }

    /**
     * 就绪检查 (Kubernetes readiness probe)
     */
    @GET
    @Path("/ready")
    public HealthSummary getReadiness() {
        List<HealthCheckResult> checks = new ArrayList<>();
        checks.add(databaseHealthCheck.check());
        checks.add(redisHealthCheck.check());
        return HealthSummary.create(checks);
    }
}
