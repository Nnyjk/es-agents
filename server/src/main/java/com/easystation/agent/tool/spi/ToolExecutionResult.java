package com.easystation.agent.tool.spi;

import com.easystation.agent.tool.domain.ToolExecutionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 工具执行结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolExecutionResult {

    /** 执行状态 */
    private ToolExecutionStatus status;

    /** 输出结果（可以是任意对象） */
    private Object output;

    /** 错误信息 */
    private String error;

    /** 执行耗时（毫秒） */
    private long durationMs;

    /** 附加数据 */
    private Map<String, Object> metadata;

    /**
     * 创建成功结果
     */
    public static ToolExecutionResult success(Object output) {
        return ToolExecutionResult.builder()
                .status(ToolExecutionStatus.SUCCESS)
                .output(output)
                .build();
    }

    /**
     * 创建成功结果（带耗时）
     */
    public static ToolExecutionResult success(Object output, long durationMs) {
        return ToolExecutionResult.builder()
                .status(ToolExecutionStatus.SUCCESS)
                .output(output)
                .durationMs(durationMs)
                .build();
    }

    /**
     * 创建失败结果
     */
    public static ToolExecutionResult failed(String error) {
        return ToolExecutionResult.builder()
                .status(ToolExecutionStatus.FAILED)
                .error(error)
                .build();
    }

    /**
     * 创建失败结果（带耗时）
     */
    public static ToolExecutionResult failed(String error, long durationMs) {
        return ToolExecutionResult.builder()
                .status(ToolExecutionStatus.FAILED)
                .error(error)
                .durationMs(durationMs)
                .build();
    }

    /**
     * 是否成功
     */
    public boolean isSuccess() {
        return status == ToolExecutionStatus.SUCCESS;
    }
}
