/**
 * 审计日志相关类型定义
 */

export type AuditAction =
  | 'CREATE'
  | 'UPDATE'
  | 'DELETE'
  | 'LOGIN'
  | 'LOGOUT'
  | 'EXPORT'
  | 'IMPORT'
  | 'DEPLOY'
  | 'EXECUTE';

export type AuditStatus = 'SUCCESS' | 'FAILED' | 'PENDING';

export interface AuditLog {
  id: string;
  action: AuditAction;
  resourceType: string;
  resourceId: string;
  description: string;
  ipAddress: string;
  userAgent: string;
  status: AuditStatus;
  errorMessage?: string;
  durationMs?: number;
  createdAt: string;
}

export interface AuditLogQuery {
  keyword?: string;
  action?: AuditAction;
  resourceType?: string;
  status?: AuditStatus;
  startTime?: string;
  endTime?: string;
  current?: number;
  pageSize?: number;
}

export interface AuditLogListResponse {
  data: AuditLog[];
  total: number;
  success: boolean;
}

export interface ActionCount {
  action: string;
  count: number;
}

export interface AuditLogSummary {
  totalCount: number;
  successCount: number;
  failedCount: number;
  topActions: ActionCount[];
}