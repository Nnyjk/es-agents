package com.easystation.agent.tool.impl;

import com.easystation.agent.tool.domain.ToolExecutionLog;
import com.easystation.agent.tool.domain.ToolExecutionStatus;
import com.easystation.agent.tool.repository.ToolExecutionLogRepository;
import com.easystation.agent.tool.spi.Tool;
import com.easystation.agent.tool.spi.ToolExecutionResult;
import com.easystation.agent.tool.spi.ToolExecutor;
import com.easystation.agent.tool.spi.ToolRegistry;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 工具执行器实现
 * 支持同步和异步执行，带超时控制和日志记录
 */
@ApplicationScoped
public class ToolExecutorImpl implements ToolExecutor {

    @Inject
    ToolRegistry toolRegistry;

    @Inject
    ToolExecutionLogRepository executionLogRepository;

    /** 执行中的任务：executionId -> CompletableFuture */
    private final Map<String, CompletableFuture<ToolExecutionResult>> runningExecutions = new ConcurrentHashMap<>();

    /** 异步执行线程池 */
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    @Override
    public ToolExecutionResult execute(String toolId, Map<String, Object> params) {
        return execute(toolId, params, getDefaultTimeout(toolId));
    }

    @Override
    public ToolExecutionResult execute(String toolId, Map<String, Object> params, long timeoutMs) {
        String executionId = UUID.randomUUID().toString();
        long startTime = System.currentTimeMillis();

        try {
            Log.infof("Executing tool %s (executionId: %s)", toolId, executionId);

            // 获取工具
            Tool tool = toolRegistry.getTool(toolId)
                    .orElseThrow(() -> new IllegalArgumentException("Tool not found: " + toolId));

            // 执行工具
            ToolExecutionResult result = tool.execute(params);

            // 记录日志
            long durationMs = System.currentTimeMillis() - startTime;
            logExecution(executionId, toolId, null, params, result, durationMs);

            Log.infof("Tool %s executed successfully in %dms", toolId, durationMs);
            return result;

        } catch (Exception e) {
            long durationMs = System.currentTimeMillis() - startTime;
            Log.errorf(e, "Tool %s execution failed", toolId);

            // 记录错误日志
            logExecution(executionId, toolId, null, params,
                    ToolExecutionResult.failed(e.getMessage(), durationMs), durationMs);

            return ToolExecutionResult.failed(e.getMessage(), durationMs);
        }
    }

    @Override
    public CompletionStage<ToolExecutionResult> executeAsync(String toolId, Map<String, Object> params) {
        return executeAsync(toolId, params, getDefaultTimeout(toolId));
    }

    @Override
    public CompletionStage<ToolExecutionResult> executeAsync(String toolId, Map<String, Object> params, long timeoutMs) {
        String executionId = UUID.randomUUID().toString();
        CompletableFuture<ToolExecutionResult> future = new CompletableFuture<>();

        runningExecutions.put(executionId, future);

        CompletableFuture.runAsync(() -> {
            try {
                ToolExecutionResult result = execute(toolId, params, timeoutMs);
                future.complete(result);
            } catch (Exception e) {
                future.completeExceptionally(e);
            } finally {
                runningExecutions.remove(executionId);
            }
        }, executorService);

        return future;
    }

    @Override
    @Transactional
    public boolean cancelExecution(String executionId) {
        CompletableFuture<ToolExecutionResult> future = runningExecutions.get(executionId);
        if (future != null && !future.isDone()) {
            future.cancel(true);
            Log.infof("Execution %s cancelled", executionId);
            return true;
        }
        return false;
    }

    @Override
    public boolean isRunning(String executionId) {
        CompletableFuture<ToolExecutionResult> future = runningExecutions.get(executionId);
        return future != null && !future.isDone();
    }

    /**
     * 记录执行日志
     */
    @Transactional
    protected void logExecution(String executionId, String toolId, String taskId,
                                Map<String, Object> input, ToolExecutionResult result, long durationMs) {
        ToolExecutionLog log = new ToolExecutionLog();
        log.toolId = toolId;
        log.taskId = taskId;
        log.input = toJson(input);
        log.output = result.getOutput() != null ? toJson(result.getOutput()) : null;
        log.error = result.getError();
        log.status = result.getStatus();
        log.durationMs = durationMs;
        executionLogRepository.persist(log);
    }

    /**
     * 获取默认超时时间
     */
    protected long getDefaultTimeout(String toolId) {
        return toolRegistry.getTool(toolId)
                .map(Tool::getDefaultTimeout)
                .orElse(30000L);
    }

    /**
     * 对象转 JSON 字符串
     */
    protected String toJson(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof String) {
            return (String) obj;
        }
        try {
            return com.fasterxml.jackson.databind.ObjectMapperCompat.writeValueAsString(obj);
        } catch (Exception e) {
            return obj.toString();
        }
    }
}
