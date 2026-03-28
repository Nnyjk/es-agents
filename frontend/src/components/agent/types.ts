/**
 * Agent 状态可视化组件类型定义
 */

import type { AgentStatus } from "@/types/agentMonitoring";

// Re-export AgentStatus for convenience
export type { AgentStatus } from "@/types/agentMonitoring";

/**
 * 状态配置接口
 */
export interface StatusConfig {
  color: string;
  text: string;
  description: string;
  icon?: React.ReactNode;
}

/**
 * 状态配置映射
 */
export const AGENT_STATUS_CONFIG: Record<AgentStatus, StatusConfig> = {
  UNCONFIGURED: {
    color: "default",
    text: "未配置",
    description: "Agent 实例已创建但未配置",
  },
  PREPARING: {
    color: "processing",
    text: "准备中",
    description: "正在准备部署资源",
  },
  READY: {
    color: "success",
    text: "就绪",
    description: "资源准备完成，可以打包",
  },
  PACKAGING: {
    color: "processing",
    text: "打包中",
    description: "正在打包部署包",
  },
  PACKAGED: {
    color: "success",
    text: "已打包",
    description: "打包完成，可以部署",
  },
  DEPLOYING: {
    color: "processing",
    text: "部署中",
    description: "正在执行部署流程",
  },
  DEPLOYED: {
    color: "success",
    text: "已部署",
    description: "部署完成，等待启动",
  },
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
  ERROR: {
    color: "error",
    text: "异常",
    description: "Agent 运行异常",
  },
};

/**
 * 状态操作配置接口
 */
export interface StatusAction {
  key: string;
  label: string;
  icon?: React.ReactNode;
  danger?: boolean;
  tooltip?: string;
}

/**
 * 状态操作映射
 * 定义每个状态下可执行的操作
 */
export const STATUS_ACTIONS: Record<AgentStatus, StatusAction[]> = {
  UNCONFIGURED: [{ key: "CONFIG", label: "配置", tooltip: "配置 Agent 参数" }],
  PREPARING: [],
  READY: [{ key: "PACKAGE", label: "打包", tooltip: "开始打包部署包" }],
  PACKAGING: [],
  PACKAGED: [{ key: "DEPLOY", label: "部署", tooltip: "开始部署到目标主机" }],
  DEPLOYING: [],
  DEPLOYED: [{ key: "START", label: "启动", tooltip: "启动 Agent 服务" }],
  ONLINE: [
    { key: "STOP", label: "停止", tooltip: "停止 Agent 服务" },
    { key: "RESTART", label: "重启", tooltip: "重启 Agent 服务" },
    { key: "VIEW_LOG", label: "查看日志", tooltip: "查看 Agent 运行日志" },
  ],
  OFFLINE: [{ key: "START", label: "启动", tooltip: "启动 Agent 服务" }],
  ERROR: [
    { key: "RETRY", label: "重试", tooltip: "重新尝试部署" },
    { key: "VIEW_ERROR", label: "查看错误", tooltip: "查看错误详情" },
  ],
};

/**
 * 状态历史记录接口
 */
export interface StatusHistoryRecord {
  id: string;
  agentId: string;
  fromStatus: AgentStatus | null;
  toStatus: AgentStatus;
  reason?: string;
  triggeredBy?: string;
  timestamp: string;
}

/**
 * 状态流转顺序
 */
export const STATUS_FLOW_ORDER: AgentStatus[] = [
  "UNCONFIGURED",
  "PREPARING",
  "READY",
  "PACKAGING",
  "PACKAGED",
  "DEPLOYING",
  "DEPLOYED",
  "ONLINE",
];

/**
 * 获取状态在流转顺序中的位置
 */
export const getStatusStepIndex = (status: AgentStatus): number => {
  const index = STATUS_FLOW_ORDER.indexOf(status);
  return index >= 0 ? index : -1;
};

/**
 * 判断状态是否为处理中状态
 */
export const isProcessingStatus = (status: AgentStatus): boolean => {
  return (
    status === "PREPARING" || status === "PACKAGING" || status === "DEPLOYING"
  );
};

/**
 * 判断状态是否为成功状态
 */
export const isSuccessStatus = (status: AgentStatus): boolean => {
  return (
    status === "READY" ||
    status === "PACKAGED" ||
    status === "DEPLOYED" ||
    status === "ONLINE"
  );
};

/**
 * 判断状态是否为错误状态
 */
export const isErrorStatus = (status: AgentStatus): boolean => {
  return status === "ERROR" || status === "OFFLINE";
};
