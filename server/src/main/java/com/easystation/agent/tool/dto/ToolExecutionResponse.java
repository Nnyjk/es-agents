package com.easystation.agent.tool.dto;

import com.easystation.agent.tool.domain.ToolExecutionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * 工具执行响应 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolExecutionResponse {

    /** 执行 ID */
    private UUID executionId;

    /** 执行状态 */
    private ToolExecutionStatus status;

    /** 输出结果 */
    private Object output;

    /** 错误信息 */
    private String error;

    /** 执行耗时（毫秒） */
    private long durationMs;

    /** 执行时间 */
    private LocalDateTime executedAt;
}
