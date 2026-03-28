/**
 * 批量操作相关类型定义
 */

/**
 * 批量操作类型枚举
 */
export type BatchOperationType =
  | "BATCH_COMMAND"
  | "BATCH_DEPLOY"
  | "BATCH_UPGRADE";

/**
 * 批量操作状态枚举
 */
export type BatchOperationStatus =
  | "PENDING"
  | "RUNNING"
  | "PARTIAL_SUCCESS"
  | "SUCCESS"
  | "FAILED";

/**
 * 批量操作项状态枚举
 */
export type BatchOperationItemStatus =
  | "PENDING"
  | "RUNNING"
  | "SUCCESS"
  | "FAILED";

/**
 * 批量操作项目标类型枚举
 */
export type BatchOperationTargetType = "HOST" | "AGENT";

/**
 * 批量操作实体
 */
export interface BatchOperation {
  id: string;
  operationType: BatchOperationType;
  status: BatchOperationStatus;
  operatorId: string;
  createdAt: string;
  completedAt?: string;
  totalItems: number;
  successCount: number;
  failedCount: number;
}

/**
 * 批量操作项实体
 */
export interface BatchOperationItem {
  id: string;
  targetId: string;
  targetType: BatchOperationTargetType;
  status: BatchOperationItemStatus;
  errorMessage?: string;
  startedAt?: string;
  completedAt?: string;
  createdAt?: string;
}

/**
 * 批量命令执行请求
 */
export interface BatchCommandRequest {
  hostIds: string[];
  command: string;
}

/**
 * 批量部署请求
 */
export interface BatchDeployRequest {
  agentIds: string[];
}

/**
 * 批量升级请求
 */
export interface BatchUpgradeRequest {
  agentIds: string[];
  version: string;
}

/**
 * 批量操作查询参数
 */
export interface BatchOperationQueryParams {
  page?: number;
  size?: number;
  operationType?: BatchOperationType;
  status?: BatchOperationStatus;
}

/**
 * 批量操作列表响应
 */
export interface BatchOperationListResponse {
  data: BatchOperation[];
  total: number;
  page?: number;
  size?: number;
}

/**
 * 批量操作类型显示名称映射
 */
export const BatchOperationTypeLabels: Record<BatchOperationType, string> = {
  BATCH_COMMAND: "批量命令执行",
  BATCH_DEPLOY: "批量部署",
  BATCH_UPGRADE: "批量升级",
};

/**
 * 批量操作状态显示名称映射
 */
export const BatchOperationStatusLabels: Record<BatchOperationStatus, string> = {
  PENDING: "待执行",
  RUNNING: "执行中",
  PARTIAL_SUCCESS: "部分成功",
  SUCCESS: "成功",
  FAILED: "失败",
};

/**
 * 批量操作项状态显示名称映射
 */
export const BatchOperationItemStatusLabels: Record<
  BatchOperationItemStatus,
  string
> = {
  PENDING: "待执行",
  RUNNING: "执行中",
  SUCCESS: "成功",
  FAILED: "失败",
};

/**
 * 批量操作状态颜色映射
 */
export const BatchOperationStatusColors: Record<BatchOperationStatus, string> = {
  PENDING: "default",
  RUNNING: "processing",
  PARTIAL_SUCCESS: "warning",
  SUCCESS: "success",
  FAILED: "error",
};

/**
 * 批量操作项状态颜色映射
 */
export const BatchOperationItemStatusColors: Record<
  BatchOperationItemStatus,
  string
> = {
  PENDING: "default",
  RUNNING: "processing",
  SUCCESS: "success",
  FAILED: "error",
};