import request from "../utils/request";
import type {
  AuditLog,
  AuditLogQuery,
  AuditLogListResponse,
  AuditLogSummary,
} from "../types/audit";

/**
 * 获取审计日志列表
 */
export const getAuditLogs = async (
  params: AuditLogQuery,
): Promise<AuditLogListResponse> => {
  const response = await request.get("/me/audit-logs", { params });
  return response.data;
};

/**
 * 获取审计日志详情
 */
export const getAuditLogDetail = async (id: string): Promise<AuditLog> => {
  const response = await request.get(`/me/audit-logs/${id}`);
  return response.data;
};

/**
 * 获取审计日志统计摘要
 */
export const getAuditLogSummary = async (
  params?: Pick<AuditLogQuery, "startTime" | "endTime">,
): Promise<AuditLogSummary> => {
  const response = await request.get("/me/audit-logs/summary", {
    params,
  });
  return response.data;
};

/**
 * 导出审计日志
 */
export const exportAuditLogs = async (
  params: AuditLogQuery,
): Promise<Blob> => {
  const response = await request.get("/me/audit-logs/export", {
    params,
    responseType: "blob",
  });
  return response.data;
};