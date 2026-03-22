/**
 * 多云与混合云资源统一管理 API 服务
 */
import request from "../utils/request";
import type {
  CloudAccount,
  AccountPermission,
  AccountAuditLog,
  MultiCloudStatistics,
  CloudResource,
  CloudResourceQueryParams,
  BatchOperationRequest,
  BatchOperationResult,
  CloudImage,
  CloudNetworkConfig,
  CostStatistics,
  IdleResource,
  ReservedInstanceRecommendation,
  CrossCloudBackupPolicy,
  DisasterRecoveryConfig,
  ScalingPolicy,
  LoadBalancingConfig,
  PageResult,
  CloudProviderConfig,
} from "@/types/multicloud";

// ============== 云厂商配置 API ==============

/**
 * 获取支持的云厂商列表
 */
export async function getCloudProviders(): Promise<CloudProviderConfig[]> {
  return request.get("/api/multicloud/providers");
}

// ============== 多云账号管理 API ==============

/**
 * 获取多云账号列表
 */
export async function getCloudAccounts(): Promise<CloudAccount[]> {
  return request.get("/api/multicloud/accounts");
}

/**
 * 获取云账号详情
 */
export async function getCloudAccount(id: string): Promise<CloudAccount> {
  return request.get(`/api/multicloud/accounts/${id}`);
}

/**
 * 创建云账号
 */
export async function createCloudAccount(
  data: Omit<CloudAccount, "id" | "createdAt" | "updatedAt" | "createdBy">,
): Promise<CloudAccount> {
  return request.post("/api/multicloud/accounts", data);
}

/**
 * 更新云账号
 */
export async function updateCloudAccount(
  id: string,
  data: Partial<CloudAccount>,
): Promise<CloudAccount> {
  return request.put(`/api/multicloud/accounts/${id}`, data);
}

/**
 * 删除云账号
 */
export async function deleteCloudAccount(id: string): Promise<void> {
  return request.delete(`/api/multicloud/accounts/${id}`);
}

/**
 * 测试云账号连接
 */
export async function testCloudAccountConnection(
  id: string,
): Promise<{ success: boolean; message: string }> {
  return request.post(`/api/multicloud/accounts/${id}/test`);
}

/**
 * 同步云账号资源
 */
export async function syncCloudAccountResources(
  id: string,
): Promise<{ success: boolean; message: string }> {
  return request.post(`/api/multicloud/accounts/${id}/sync`);
}

/**
 * 获取账号权限配置
 */
export async function getAccountPermissions(
  accountId: string,
): Promise<AccountPermission[]> {
  return request.get(`/api/multicloud/accounts/${accountId}/permissions`);
}

/**
 * 设置账号权限
 */
export async function setAccountPermission(
  accountId: string,
  data: Omit<AccountPermission, "id" | "accountId" | "createdAt">,
): Promise<AccountPermission> {
  return request.post(
    `/api/multicloud/accounts/${accountId}/permissions`,
    data,
  );
}

/**
 * 获取账号审计日志
 */
export async function getAccountAuditLogs(params: {
  accountId?: string;
  operator?: string;
  operation?: string;
  startTime?: string;
  endTime?: string;
  current?: number;
  pageSize?: number;
}): Promise<PageResult<AccountAuditLog>> {
  return request.get("/api/multicloud/accounts/audit-logs", { params });
}

// ============== 多云资源统一视图 API ==============

/**
 * 获取多云资源统计
 */
export async function getMultiCloudStatistics(): Promise<MultiCloudStatistics> {
  return request.get("/api/multicloud/statistics");
}

/**
 * 获取多云资源列表
 */
export async function getCloudResources(
  params: CloudResourceQueryParams,
): Promise<PageResult<CloudResource>> {
  return request.get("/api/multicloud/resources", { params });
}

/**
 * 获取云资源详情
 */
export async function getCloudResource(id: string): Promise<CloudResource> {
  return request.get(`/api/multicloud/resources/${id}`);
}

/**
 * 获取资源操作历史
 */
export async function getResourceOperationHistory(
  resourceId: string,
): Promise<AccountAuditLog[]> {
  return request.get(`/api/multicloud/resources/${resourceId}/history`);
}

// ============== 跨云资源操作 API ==============

/**
 * 启动资源
 */
export async function startResource(
  id: string,
): Promise<{ success: boolean; message: string }> {
  return request.post(`/api/multicloud/resources/${id}/start`);
}

/**
 * 停止资源
 */
export async function stopResource(
  id: string,
): Promise<{ success: boolean; message: string }> {
  return request.post(`/api/multicloud/resources/${id}/stop`);
}

/**
 * 重启资源
 */
export async function restartResource(
  id: string,
): Promise<{ success: boolean; message: string }> {
  return request.post(`/api/multicloud/resources/${id}/restart`);
}

/**
 * 终止资源
 */
export async function terminateResource(
  id: string,
): Promise<{ success: boolean; message: string }> {
  return request.post(`/api/multicloud/resources/${id}/terminate`);
}

/**
 * 调整资源配置
 */
export async function resizeResource(
  id: string,
  config: Record<string, unknown>,
): Promise<{ success: boolean; message: string }> {
  return request.post(`/api/multicloud/resources/${id}/resize`, config);
}

/**
 * 批量操作资源
 */
export async function batchOperation(
  data: BatchOperationRequest,
): Promise<BatchOperationResult> {
  return request.post("/api/multicloud/resources/batch-operation", data);
}

/**
 * 获取镜像列表
 */
export async function getCloudImages(params: {
  accountId?: string;
  provider?: string;
  type?: "image" | "snapshot";
  current?: number;
  pageSize?: number;
}): Promise<PageResult<CloudImage>> {
  return request.get("/api/multicloud/images", { params });
}

/**
 * 删除镜像/快照
 */
export async function deleteCloudImage(
  id: string,
): Promise<{ success: boolean; message: string }> {
  return request.delete(`/api/multicloud/images/${id}`);
}

/**
 * 获取网络配置列表
 */
export async function getNetworkConfigs(): Promise<CloudNetworkConfig[]> {
  return request.get("/api/multicloud/networks");
}

/**
 * 创建网络配置
 */
export async function createNetworkConfig(
  data: Omit<CloudNetworkConfig, "id" | "createdAt">,
): Promise<CloudNetworkConfig> {
  return request.post("/api/multicloud/networks", data);
}

/**
 * 删除网络配置
 */
export async function deleteNetworkConfig(
  id: string,
): Promise<{ success: boolean; message: string }> {
  return request.delete(`/api/multicloud/networks/${id}`);
}

// ============== 多云成本与优化 API ==============

/**
 * 获取成本统计
 */
export async function getCostStatistics(params: {
  startTime?: string;
  endTime?: string;
  provider?: string;
}): Promise<CostStatistics> {
  return request.get("/api/multicloud/costs/statistics", { params });
}

/**
 * 获取闲置资源列表
 */
export async function getIdleResources(params: {
  provider?: string;
  resourceType?: string;
  current?: number;
  pageSize?: number;
}): Promise<PageResult<IdleResource>> {
  return request.get("/api/multicloud/costs/idle-resources", { params });
}

/**
 * 获取 RI/SP 推荐
 */
export async function getReservedInstanceRecommendations(): Promise<
  ReservedInstanceRecommendation[]
> {
  return request.get("/api/multicloud/costs/ri-recommendations");
}

/**
 * 获取账单明细
 */
export async function getBillingDetails(params: {
  accountId?: string;
  startTime?: string;
  endTime?: string;
  current?: number;
  pageSize?: number;
}): Promise<
  PageResult<{
    id: string;
    accountId: string;
    accountName: string;
    provider: string;
    resourceType: string;
    resourceId: string;
    resourceName: string;
    cost: number;
    billingCycle: string;
  }>
> {
  return request.get("/api/multicloud/costs/billing", { params });
}

/**
 * 导出成本报表
 */
export async function exportCostReport(params: {
  startTime: string;
  endTime: string;
  format: "csv" | "excel";
}): Promise<{ downloadUrl: string }> {
  return request.get("/api/multicloud/costs/export", { params });
}

// ============== 多云运维调度 API ==============

/**
 * 获取跨云备份策略列表
 */
export async function getBackupPolicies(): Promise<CrossCloudBackupPolicy[]> {
  return request.get("/api/multicloud/backup-policies");
}

/**
 * 创建备份策略
 */
export async function createBackupPolicy(
  data: Omit<CrossCloudBackupPolicy, "id" | "createdAt">,
): Promise<CrossCloudBackupPolicy> {
  return request.post("/api/multicloud/backup-policies", data);
}

/**
 * 更新备份策略
 */
export async function updateBackupPolicy(
  id: string,
  data: Partial<CrossCloudBackupPolicy>,
): Promise<CrossCloudBackupPolicy> {
  return request.put(`/api/multicloud/backup-policies/${id}`, data);
}

/**
 * 删除备份策略
 */
export async function deleteBackupPolicy(
  id: string,
): Promise<{ success: boolean; message: string }> {
  return request.delete(`/api/multicloud/backup-policies/${id}`);
}

/**
 * 手动执行备份
 */
export async function executeBackup(
  id: string,
): Promise<{ success: boolean; message: string }> {
  return request.post(`/api/multicloud/backup-policies/${id}/execute`);
}

/**
 * 获取容灾配置列表
 */
export async function getDisasterRecoveryConfigs(): Promise<
  DisasterRecoveryConfig[]
> {
  return request.get("/api/multicloud/disaster-recovery");
}

/**
 * 创建容灾配置
 */
export async function createDisasterRecoveryConfig(
  data: Omit<DisasterRecoveryConfig, "id" | "createdAt">,
): Promise<DisasterRecoveryConfig> {
  return request.post("/api/multicloud/disaster-recovery", data);
}

/**
 * 更新容灾配置
 */
export async function updateDisasterRecoveryConfig(
  id: string,
  data: Partial<DisasterRecoveryConfig>,
): Promise<DisasterRecoveryConfig> {
  return request.put(`/api/multicloud/disaster-recovery/${id}`, data);
}

/**
 * 删除容灾配置
 */
export async function deleteDisasterRecoveryConfig(
  id: string,
): Promise<{ success: boolean; message: string }> {
  return request.delete(`/api/multicloud/disaster-recovery/${id}`);
}

/**
 * 触发故障转移
 */
export async function triggerFailover(
  id: string,
): Promise<{ success: boolean; message: string }> {
  return request.post(`/api/multicloud/disaster-recovery/${id}/failover`);
}

/**
 * 获取弹性伸缩策略列表
 */
export async function getScalingPolicies(): Promise<ScalingPolicy[]> {
  return request.get("/api/multicloud/scaling-policies");
}

/**
 * 创建弹性伸缩策略
 */
export async function createScalingPolicy(
  data: Omit<ScalingPolicy, "id" | "createdAt">,
): Promise<ScalingPolicy> {
  return request.post("/api/multicloud/scaling-policies", data);
}

/**
 * 更新弹性伸缩策略
 */
export async function updateScalingPolicy(
  id: string,
  data: Partial<ScalingPolicy>,
): Promise<ScalingPolicy> {
  return request.put(`/api/multicloud/scaling-policies/${id}`, data);
}

/**
 * 删除弹性伸缩策略
 */
export async function deleteScalingPolicy(
  id: string,
): Promise<{ success: boolean; message: string }> {
  return request.delete(`/api/multicloud/scaling-policies/${id}`);
}

/**
 * 获取负载调度配置列表
 */
export async function getLoadBalancingConfigs(): Promise<
  LoadBalancingConfig[]
> {
  return request.get("/api/multicloud/load-balancing");
}

/**
 * 创建负载调度配置
 */
export async function createLoadBalancingConfig(
  data: Omit<LoadBalancingConfig, "id" | "createdAt">,
): Promise<LoadBalancingConfig> {
  return request.post("/api/multicloud/load-balancing", data);
}

/**
 * 更新负载调度配置
 */
export async function updateLoadBalancingConfig(
  id: string,
  data: Partial<LoadBalancingConfig>,
): Promise<LoadBalancingConfig> {
  return request.put(`/api/multicloud/load-balancing/${id}`, data);
}

/**
 * 删除负载调度配置
 */
export async function deleteLoadBalancingConfig(
  id: string,
): Promise<{ success: boolean; message: string }> {
  return request.delete(`/api/multicloud/load-balancing/${id}`);
}