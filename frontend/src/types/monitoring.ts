/**
 * Prometheus 监控相关类型定义
 */

/**
 * 监控概览指标摘要
 */
export interface MetricSummary {
  cpuUsage: number;
  memoryUsage: number;
  diskUsage: number;
  agentCount: number;
  taskCount: number;
  alertCount: number;
}

/**
 * 时序数据点
 */
export interface TimeSeriesData {
  timestamp: string;
  value: number;
  metric: string;
}

/**
 * Grafana 大盘配置
 */
export interface GrafanaDashboard {
  id: string;
  name: string;
  url: string;
  isPublic: boolean;
  createdAt: string;
}

/**
 * PromQL 查询参数
 */
export interface PromQLQuery {
  query: string;
  timeRange: TimeRange;
  result: PromQLResult;
}

/**
 * 时间范围
 */
export interface TimeRange {
  start: string;
  end: string;
}

/**
 * PromQL 查询结果
 */
export interface PromQLResult {
  status: "success" | "error";
  data: PromQLData;
  error?: string;
}

/**
 * PromQL 数据
 */
export interface PromQLData {
  resultType: "vector" | "matrix" | "scalar" | "string";
  result: PromQLResultValue[];
}

/**
 * PromQL 结果值
 */
export interface PromQLResultValue {
  metric: Record<string, string>;
  value: [number, string] | [number, string][];
}

/**
 * 时间范围枚举
 */
export type TimeRangePreset =
  | "5m"
  | "15m"
  | "30m"
  | "1h"
  | "3h"
  | "6h"
  | "12h"
  | "24h"
  | "7d"
  | "30d";

/**
 * 指标类型
 */
export type MetricType =
  | "cpu_usage"
  | "memory_usage"
  | "disk_usage"
  | "network_in"
  | "network_out"
  | "agent_count"
  | "task_count"
  | "alert_count";

/**
 * 指标趋势
 */
export type TrendDirection = "up" | "down" | "stable";

/**
 * 指标查询参数
 */
export interface MetricQueryParams {
  metric: MetricType;
  timeRange?: TimeRangePreset | TimeRange;
  step?: string;
}

/**
 * 大盘创建参数
 */
export interface CreateDashboardParams {
  name: string;
  url: string;
  isPublic?: boolean;
}

/**
 * 监控告警
 */
export interface MonitoringAlert {
  id: string;
  name: string;
  severity: "info" | "warning" | "critical";
  message: string;
  source: string;
  status: "firing" | "resolved";
  startsAt: string;
  endsAt?: string;
  labels?: Record<string, string>;
}
