package com.easystation.health.check;

import com.easystation.agent.domain.AgentInstance;
import com.easystation.agent.domain.enums.AgentStatus;
import com.easystation.health.dto.HealthCheckResult;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

/**
 * Agent 实例健康检查
 */
@ApplicationScoped
public class AgentHealthCheck {

    /**
     * 检查 Agent 实例健康状态
     * 
     * @return 健康检查结果
     */
    public HealthCheckResult check() {
        long startTime = System.currentTimeMillis();
        try {
            // 获取所有 Agent 实例
            List<AgentInstance> instances = AgentInstance.listAll();
            int total = instances.size();
            int healthy = 0;
            int unhealthy = 0;
            
            for (AgentInstance instance : instances) {
                if (instance.status == AgentStatus.ONLINE || 
                    instance.status == AgentStatus.READY ||
                    instance.status == AgentStatus.DEPLOYED) {
                    healthy++;
                } else {
                    unhealthy++;
                }
            }
            
            long responseTime = System.currentTimeMillis() - startTime;
            String message = String.format("Total: %d, Healthy: %d, Unhealthy: %d", 
                total, healthy, unhealthy);
            
            if (unhealthy == 0 && total > 0) {
                return HealthCheckResult.up("Agent Instances", message, responseTime);
            } else if (total == 0) {
                return HealthCheckResult.up("Agent Instances", "No agent instances configured", responseTime);
            } else {
                return HealthCheckResult.down("Agent Instances", message, responseTime);
            }
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            return HealthCheckResult.down("Agent Instances", "Agent health check failed: " + e.getMessage(), responseTime);
        }
    }
}
