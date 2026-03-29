package com.easystation.agent.tool.spi;

import java.util.Map;
import java.util.concurrent.CompletionStage;

/**
 * 工具执行器接口
 * 用于执行工具调用
 */
public interface ToolExecutor {

    /**
     * 同步执行工具
     * @param toolId 工具 ID
     * @param params 参数键值对
     * @return 执行结果
     */
    ToolExecutionResult execute(String toolId, Map<String, Object> params);

    /**
     * 同步执行工具（带超时）
     * @param toolId 工具 ID
     * @param params 参数键值对
     * @param timeoutMs 超时时间（毫秒）
     * @return 执行结果
     */
    ToolExecutionResult execute(String toolId, Map<String, Object> params, long timeoutMs);

    /**
     * 异步执行工具
     * @param toolId 工具 ID
     * @param params 参数键值对
     * @return 异步执行结果
     */
    CompletionStage<ToolExecutionResult> executeAsync(String toolId, Map<String, Object> params);

    /**
     * 异步执行工具（带超时）
     * @param toolId 工具 ID
     * @param params 参数键值对
     * @param timeoutMs 超时时间（毫秒）
     * @return 异步执行结果
     */
    CompletionStage<ToolExecutionResult> executeAsync(String toolId, Map<String, Object> params, long timeoutMs);

    /**
     * 取消执行
     * @param executionId 执行 ID
     * @return 是否取消成功
     */
    boolean cancelExecution(String executionId);

    /**
     * 检查执行是否在进行中
     * @param executionId 执行 ID
     * @return 是否在执行中
     */
    boolean isRunning(String executionId);
}
