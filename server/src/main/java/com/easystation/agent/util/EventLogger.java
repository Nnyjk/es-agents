package com.easystation.agent.util;

import com.easystation.agent.dto.SystemEventLogDTO;
import com.easystation.agent.service.SystemEventLogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * 事件日志记录工具类
 */
@Slf4j
@ApplicationScoped
public class EventLogger {

    @Inject
    SystemEventLogService eventLogService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 记录系统启动事件
     */
    public void logSystemStart(String version) {
        Map<String, Object> details = new HashMap<>();
        details.put("version", version);
        log("SYSTEM_START", "INFO", "system", null, null, null, 
            "系统启动", details, null, null, null, null);
    }

    /**
     * 记录系统停止事件
     */
    public void logSystemStop(String reason) {
        Map<String, Object> details = new HashMap<>();
        details.put("reason", reason);
        log("SYSTEM_STOP", "INFO", "system", "STOP", null, null,
            "系统停止：" + reason, details, null, null, null, null);
    }

    /**
     * 记录异常事件
     */
    public void logException(String module, Throwable e) {
        Map<String, Object> details = new HashMap<>();
        details.put("exception", e.getClass().getName());
        details.put("message", e.getMessage());
        log("EXCEPTION", "ERROR", module, null, null, null,
            "系统异常：" + e.getMessage(), details, null, null, null, null);
    }

    /**
     * 记录部署事件
     */
    public void logDeploy(Long deploymentId, String status, Long userId) {
        Map<String, Object> details = new HashMap<>();
        details.put("status", status);
        log("DEPLOY", "INFO", "deployment", "DEPLOY", "DEPLOYMENT", deploymentId,
            "部署任务 #" + deploymentId + " 状态：" + status, details, null, null, null, userId);
    }

    /**
     * 记录命令执行事件
     */
    public void logCommand(Long agentId, String command, String result, Long userId) {
        Map<String, Object> details = new HashMap<>();
        details.put("command", command);
        details.put("result", result);
        log("COMMAND", "INFO", "agent", "EXECUTE", "AGENT", agentId,
            "命令执行：" + command, details, null, null, null, userId);
    }

    /**
     * 记录实体操作事件
     */
    public void logEntityOperation(String operation, String targetType, Long targetId, 
                                   String message, Long userId) {
        log(operation.toUpperCase(), "INFO", targetType.toLowerCase(), 
            operation.toUpperCase(), targetType.toUpperCase(), targetId,
            message, null, null, null, null, userId);
    }

    /**
     * 通用日志记录方法
     */
    private void log(String eventType, String eventLevel, String module,
                     String operation, String targetType, Long targetId,
                     String message, Object details, String clientIp, 
                     String requestPath, Long duration, Long userId) {
        try {
            String detailsJson = details != null ? objectMapper.writeValueAsString(details) : null;
            
            SystemEventLogDTO dto = new SystemEventLogDTO(
                null, eventType, eventLevel, module, operation, targetType, targetId,
                userId, message, detailsJson, clientIp, requestPath, duration, null, null
            );
            
            eventLogService.logEvent(dto);
        } catch (Exception e) {
            log.error("记录事件日志失败", e);
        }
    }
}
