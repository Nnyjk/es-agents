/**
 * Agent 详情页类型定义
 */

import type { AgentStatus, LogLevel, LogEntry } from "@/types/agentMonitoring";

// Re-export for convenience
export type { AgentStatus, LogLevel, LogEntry } from "@/types/agentMonitoring";

/**
 * Agent 详情数据
 */
export interface AgentDetailData {
  id: string;
  hostId: string;
  hostName: string;
  hostIp?: string;
  templateId: string;
  templateName: string;
  status: AgentStatus;
  version: string | null;
  lastHeartbeatTime: string | null;
  heartbeatAgeSeconds: number | null;
  isOnline: boolean;
  createdAt: string;
  updatedAt: string;
}

/**
 * WebSocket 状态消息类型
 */
export type WebSocketMessageType =
  | "STATUS_CHANGE"
  | "LOG_APPEND"
  | "DEPLOYMENT_PROGRESS"
  | "DEPLOYMENT_STATUS"
  | "DEPLOYMENT_ERROR"
  | "HEARTBEAT";

/**
 * WebSocket 状态变更消息
 */
export interface StatusChangeMessage {
  type: "STATUS_CHANGE";
  agentId: string;
  status: AgentStatus;
  timestamp: string;
  message?: string;
}

/**
 * WebSocket 日志追加消息
 */
export interface LogAppendMessage {
  type: "LOG_APPEND";
  agentId: string;
  logs: LogEntry[];
  timestamp: string;
}

/**
 * WebSocket 部署进度消息
 */
export interface DeploymentProgressMessage {
  type: "DEPLOYMENT_PROGRESS" | "DEPLOYMENT_STATUS" | "DEPLOYMENT_ERROR";
  deploymentId: string;
  progress?: number;
  stage?: string;
  status?: string;
  message: string;
  timestamp: string;
}

/**
 * WebSocket 心跳消息
 */
export interface HeartbeatMessage {
  type: "HEARTBEAT";
  agentId: string;
  timestamp: string;
}

/**
 * WebSocket 消息联合类型
 */
export type WebSocketMessage =
  | StatusChangeMessage
  | LogAppendMessage
  | DeploymentProgressMessage
  | HeartbeatMessage;

/**
 * 部署历史记录
 */
export interface DeploymentHistoryRecord {
  id: string;
  agentInstanceId: string;
  version: string;
  status: "PENDING" | "RUNNING" | "SUCCESS" | "FAILED";
  triggerType: "MANUAL" | "AUTO";
  triggeredBy: string;
  startedAt: string | null;
  finishedAt: string | null;
  durationMs: number | null;
  message?: string;
  createdAt: string;
}

/**
 * Agent 操作类型
 */
export type AgentActionType = "DEPLOY" | "RESTART" | "STOP" | "DELETE";

/**
 * Agent 操作参数
 */
export interface AgentActionParams {
  action: AgentActionType;
  version?: string;
  remarks?: string;
}

/**
 * Agent 操作结果
 */
export interface AgentActionResult {
  success: boolean;
  message?: string;
  taskId?: string;
}

/**
 * 状态配置映射
 */
export const AgentStatusConfig: Record<
  AgentStatus,
  { color: string; text: string; description: string }
> = {
  ONLINE: {
    color: "success",
    text: "在线",
    description: "Agent 正常运行",
  },
  OFFLINE: {
    color: "default",
    text: "离线",
    description: "Agent 已断开连接",
  },
  DEPLOYING: {
    color: "processing",
    text: "部署中",
    description: "正在部署 Agent",
  },
  DEPLOYED: {
    color: "success",
    text: "已部署",
    description: "部署完成，等待启动",
  },
  PREPARING: {
    color: "processing",
    text: "准备中",
    description: "准备部署资源",
  },
  READY: {
    color: "success",
    text: "就绪",
    description: "资源准备完成",
  },
  PACKAGING: {
    color: "processing",
    text: "打包中",
    description: "打包部署包",
  },
  PACKAGED: {
    color: "success",
    text: "已打包",
    description: "打包完成",
  },
  UNCONFIGURED: {
    color: "warning",
    text: "未配置",
    description: "Agent 未配置",
  },
  ERROR: {
    color: "error",
    text: "异常",
    description: "Agent 运行异常",
  },
};

/**
 * 日志级别配置映射
 */
export const LogLevelConfig: Record<LogLevel, { color: string; icon: string }> =
  {
    DEBUG: { color: "default", icon: "BugOutlined" },
    INFO: { color: "blue", icon: "InfoCircleOutlined" },
    WARN: { color: "orange", icon: "WarningOutlined" },
    ERROR: { color: "red", icon: "CloseCircleOutlined" },
  };
