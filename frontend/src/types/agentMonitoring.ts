/**
 * Agent 监控相关类型定义
 */

/**
 * Agent 状态枚举
 */
export type AgentStatus =
  | "UNCONFIGURED"
  | "PREPARING"
  | "READY"
  | "PACKAGING"
  | "PACKAGED"
  | "DEPLOYING"
  | "DEPLOYED"
  | "ONLINE"
  | "OFFLINE"
  | "ERROR";

/**
 * Agent 健康状态枚举
 */
export type HealthStatus = "HEALTHY" | "WARNING" | "CRITICAL" | "UNKNOWN";

/**
 * 指标类型枚举
 */
export type MetricType =
  // 主机指标
  | "HOST_CPU_USAGE"
  | "HOST_MEMORY_USAGE"
  | "HOST_DISK_USAGE"
  | "HOST_DISK_IO_READ"
  | "HOST_DISK_IO_WRITE"
  | "HOST_NETWORK_IN"
  | "HOST_NETWORK_OUT"
  | "HOST_LOAD_1"
  | "HOST_LOAD_5"
  | "HOST_LOAD_15"
  // Agent 进程指标
  | "AGENT_CPU_USAGE"
  | "AGENT_MEMORY_USAGE"
  | "AGENT_MEMORY_RSS"
  | "AGENT_UPTIME"
  | "AGENT_THREAD_COUNT"
  | "AGENT_CONNECTION_COUNT"
  // Agent 业务指标
  | "AGENT_TASK_TOTAL"
  | "AGENT_TASK_SUCCESS"
  | "AGENT_TASK_FAILED"
  | "AGENT_HEARTBEAT_COUNT"
  | "AGENT_COMMAND_EXECUTED";

/**
 * Agent 运行时状态
 */
export interface AgentRuntimeStatus {
  id: string;
  status: AgentStatus;
  version: string;
  lastHeartbeatTime: string | null;
  heartbeatAgeSeconds: number | null;
  isOnline: boolean;
  statusMessage: string | null;
  createdAt: string;
  updatedAt: string;
  healthStatus?: HealthStatus;
}

/**
 * Agent 健康记录
 */
export interface AgentHealthRecord {
  id: string;
  agentId: string;
  agentName: string;
  hostId: string;
  hostName: string;
  status: AgentStatus;
  healthStatus: HealthStatus;
  lastHeartbeatTime: string | null;
  heartbeatAgeSeconds: number | null;
  version: string;
  cpuUsage: number | null;
  memoryUsage: number | null;
  errorMessage: string | null;
  createdAt: string;
}

/**
 * Agent 实例记录
 */
export interface AgentInstanceRecord {
  id: string;
  hostId: string;
  hostName: string;
  templateId: string;
  templateName: string;
  status: AgentStatus;
  version: string;
  createdAt: string;
  updatedAt: string;
}

/**
 * 指标数据点
 */
export interface MetricData {
  type: MetricType;
  value: number;
  tags?: string;
  collectedAt: string;
}

/**
 * 指标详情
 */
export interface MetricDetail {
  id: string;
  agentId: string;
  hostId: string | null;
  type: MetricType;
  value: number;
  unit: string;
  tags: string | null;
  collectedAt: string;
}

/**
 * 指标趋势数据
 */
export interface MetricTrend {
  type: MetricType;
  unit: string;
  points: MetricTrendPoint[];
}

/**
 * 指标趋势数据点
 */
export interface MetricTrendPoint {
  time: string;
  value: number;
}

/**
 * 指标聚合结果
 */
export interface MetricAggregate {
  type: MetricType;
  unit: string;
  avg: number;
  min: number;
  max: number;
  count: number;
}

/**
 * Agent 监控概览统计
 */
export interface AgentMonitorOverview {
  totalAgents: number;
  onlineAgents: number;
  offlineAgents: number;
  errorAgents: number;
  versionDistribution: VersionDistribution[];
  resourceUsage: ResourceUsage;
}

/**
 * 版本分布
 */
export interface VersionDistribution {
  version: string;
  count: number;
}

/**
 * 资源使用率
 */
export interface ResourceUsage {
  cpuUsage: number;
  memoryUsage: number;
  diskUsage: number;
}

/**
 * Agent 任务记录
 */
export interface AgentTaskRecord {
  id: string;
  agentId: string;
  agentName: string;
  commandId: string;
  commandName: string;
  status: "PENDING" | "RUNNING" | "SUCCESS" | "FAILED" | "CANCELLED";
  args: string | null;
  output: string | null;
  errorMessage: string | null;
  startedAt: string | null;
  finishedAt: string | null;
  createdAt: string;
}

/**
 * 指标查询参数
 */
export interface MetricQueryParams {
  agentId?: string;
  hostId?: string;
  types?: MetricType[];
  startTime?: string;
  endTime?: string;
  limit?: number;
  offset?: number;
}

/**
 * Agent 列表查询参数
 */
export interface AgentListParams {
  status?: AgentStatus;
  version?: string;
  hostId?: string;
  keyword?: string;
  page?: number;
  size?: number;
}

// ========== 日志相关类型 ==========

/**
 * 日志级别
 */
export type LogLevel = "DEBUG" | "INFO" | "WARN" | "ERROR";

/**
 * 日志条目
 */
export interface LogEntry {
  lineNumber: number;
  timestamp: string;
  level: LogLevel;
  message: string;
}

/**
 * Agent 日志记录
 */
export interface AgentLogRecord {
  agentId: string;
  agentName: string;
  totalCount: number;
  logs: LogEntry[];
}

/**
 * 日志查询参数
 */
export interface LogQueryParams {
  agentId: string;
  limit?: number;
  offset?: number;
  level?: LogLevel;
  keyword?: string;
  startTime?: string;
  endTime?: string;
}

/**
 * 日志统计
 */
export interface LogStats {
  totalCount: number;
  errorCount: number;
  warnCount: number;
  infoCount: number;
  debugCount: number;
}

/**
 * 部署日志查询参数
 */
export interface DeploymentLogQueryParams {
  agentId: string;
  deploymentId?: string;
  limit?: number;
  offset?: number;
  startTime?: string;
  endTime?: string;
}

/**
 * 命令执行日志查询参数
 */
export interface CommandLogQueryParams {
  agentId: string;
  executionId?: string;
  limit?: number;
  offset?: number;
  startTime?: string;
  endTime?: string;
}

// ========== 命令执行相关类型 ==========

/**
 * 命令执行状态
 */
export type ExecutionStatus =
  | "PENDING"
  | "RUNNING"
  | "SUCCESS"
  | "FAILED"
  | "CANCELLED"
  | "TIMEOUT";

/**
 * 命令执行记录
 */
export interface CommandExecutionRecord {
  id: string;
  templateId?: string;
  templateName?: string;
  agentInstanceId: string;
  hostName: string;
  command: string;
  status: ExecutionStatus;
  startedAt?: string;
  finishedAt?: string;
  exitCode?: number;
  createdAt: string;
  executedBy?: string;
}

/**
 * 命令执行详情
 */
export interface CommandExecutionDetail extends CommandExecutionRecord {
  output?: string;
  error?: string;
  args?: string;
  duration?: number;
}

/**
 * 执行命令参数
 */
export interface ExecuteCommandParams {
  agentId: string;
  command: string;
  args?: string;
  timeout?: number;
}
