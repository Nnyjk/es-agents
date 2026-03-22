/**
 * 系统全局设置 API 服务
 */
import request from "./request";
import type {
  SystemSettingsResponse,
  SystemSettingsRequest,
  IpAccessRule,
  IpAccessRuleRequest,
  SystemOperationLog,
  SystemStatus,
  CacheClearResult,
} from "../types/settings";

const BASE_URL = "/api/v1/system/settings";

/**
 * 获取系统设置
 */
export async function getSystemSettings(): Promise<SystemSettingsResponse> {
  const response = await request.get<SystemSettingsResponse>(BASE_URL);
  return response.data;
}

/**
 * 更新基础信息设置
 */
export async function updateBasicSettings(
  data: Partial<SystemSettingsResponse["basic"]>
): Promise<void> {
  await request.put(`${BASE_URL}/basic`, data);
}

/**
 * 更新安全设置
 */
export async function updateSecuritySettings(
  data: Partial<SystemSettingsResponse["security"]>
): Promise<void> {
  await request.put(`${BASE_URL}/security`, data);
}

/**
 * 更新维护设置
 */
export async function updateMaintenanceSettings(
  data: Partial<SystemSettingsResponse["maintenance"]>
): Promise<void> {
  await request.put(`${BASE_URL}/maintenance`, data);
}

/**
 * 更新邮件配置
 */
export async function updateEmailConfig(
  data: Partial<SystemSettingsResponse["email"]>
): Promise<void> {
  await request.put(`${BASE_URL}/email`, data);
}

/**
 * 测试邮件连接
 */
export async function testEmailConnection(
  recipient: string
): Promise<{ success: boolean; message: string }> {
  const response = await request.post<{ success: boolean; message: string }>(
    `${BASE_URL}/email/test`,
    { recipient }
  );
  return response.data;
}

/**
 * 更新存储配置
 */
export async function updateStorageConfig(
  data: Partial<SystemSettingsResponse["storage"]>
): Promise<void> {
  await request.put(`${BASE_URL}/storage`, data);
}

/**
 * 测试存储连接
 */
export async function testStorageConnection(): Promise<{
  success: boolean;
  message: string;
}> {
  const response = await request.post<{ success: boolean; message: string }>(
    `${BASE_URL}/storage/test`
  );
  return response.data;
}

/**
 * 更新功能开关
 */
export async function updateFeatureFlags(
  data: Partial<SystemSettingsResponse["features"]>
): Promise<void> {
  await request.put(`${BASE_URL}/features`, data);
}

/**
 * 更新日志配置
 */
export async function updateLogConfig(
  data: Partial<SystemSettingsResponse["log"]>
): Promise<void> {
  await request.put(`${BASE_URL}/log`, data);
}

/**
 * 获取 IP 白名单列表
 */
export async function getIpWhitelist(): Promise<IpAccessRule[]> {
  const response = await request.get<IpAccessRule[]>(`${BASE_URL}/ip/whitelist`);
  return response.data;
}

/**
 * 获取 IP 黑名单列表
 */
export async function getIpBlacklist(): Promise<IpAccessRule[]> {
  const response = await request.get<IpAccessRule[]>(`${BASE_URL}/ip/blacklist`);
  return response.data;
}

/**
 * 添加 IP 访问规则
 */
export async function addIpAccessRule(data: IpAccessRuleRequest): Promise<IpAccessRule> {
  const response = await request.post<IpAccessRule>(`${BASE_URL}/ip/rules`, data);
  return response.data;
}

/**
 * 删除 IP 访问规则
 */
export async function deleteIpAccessRule(id: number): Promise<void> {
  await request.delete(`${BASE_URL}/ip/rules/${id}`);
}

/**
 * 获取系统状态
 */
export async function getSystemStatus(): Promise<SystemStatus> {
  const response = await request.get<SystemStatus>("/api/v1/system/status");
  return response.data;
}

/**
 * 清理系统缓存
 */
export async function clearSystemCache(): Promise<CacheClearResult> {
  const response = await request.post<CacheClearResult>(
    "/api/v1/system/cache/clear"
  );
  return response.data;
}

/**
 * 获取操作日志
 */
export async function getOperationLogs(params: {
  page?: number;
  pageSize?: number;
  module?: string;
  operator?: string;
  startTime?: string;
  endTime?: string;
}): Promise<{ list: SystemOperationLog[]; total: number }> {
  const response = await request.get<{ list: SystemOperationLog[]; total: number }>(
    "/api/v1/system/logs",
    { params }
  );
  return response.data;
}

/**
 * 重启系统服务
 */
export async function restartSystem(): Promise<{ success: boolean; message: string }> {
  const response = await request.post<{ success: boolean; message: string }>(
    "/api/v1/system/restart"
  );
  return response.data;
}

/**
 * 检查系统更新
 */
export async function checkSystemUpdate(): Promise<{
  hasUpdate: boolean;
  currentVersion: string;
  latestVersion?: string;
  updateNotes?: string;
}> {
  const response = await request.get<{
    hasUpdate: boolean;
    currentVersion: string;
    latestVersion?: string;
    updateNotes?: string;
  }>("/api/v1/system/update/check");
  return response.data;
}