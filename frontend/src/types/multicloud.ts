/**
 * 多云与混合云资源统一管理类型定义
 */

/**
 * 云厂商类型枚举
 */
export enum CloudProvider {
  ALIYUN = "aliyun",
  TENCENT = "tencent",
  HUAWEI = "huawei",
  AWS = "aws",
  AZURE = "azure",
  VMWARE = "vmware",
  OPENSTACK = "openstack",
}

/**
 * 云账号状态枚举
 */
export enum CloudAccountStatus {
  HEALTHY = "healthy",
  WARNING = "warning",
  ERROR = "error",
  UNKNOWN = "unknown",
}

/**
 * 云资源状态枚举
 */
export enum CloudResourceStatus {
  RUNNING = "running",
  STOPPED = "stopped",
  PENDING = "pending",
  TERMINATED = "terminated",
  ERROR = "error",
}

/**
 * 云资源类型枚举
 */
export enum CloudResourceType {
  ECS = "ecs", // 云主机
  RDS = "rds", // 数据库
  OSS = "oss", // 存储
  VPC = "vpc", // 网络
  CONTAINER = "container", // 容器
  LOADBALANCER = "loadbalancer", // 负载均衡
  EIP = "eip", // 弹性IP
  OTHER = "other",
}

/**
 * 云厂商配置
 */
export interface CloudProviderConfig {
  provider: CloudProvider;
  name: string;
  logo: string;
  regions: CloudRegion[];
  resourceTypes: string[];
}

/**
 * 云区域
 */
export interface CloudRegion {
  id: string;
  name: string;
  provider: CloudProvider;
}

/**
 * 云账号信息
 */
export interface CloudAccount {
  id: string;
  name: string;
  provider: CloudProvider;
  accessKeyId: string;
  accessKeySecret?: string;
  regions: string[];
  status: CloudAccountStatus;
  lastSyncTime?: string;
  quotaUsed?: Record<string, number>;
  quotaLimit?: Record<string, number>;
  permissionExpiry?: string;
  apiConnectivity: boolean;
  createdAt: string;
  updatedAt: string;
  createdBy: string;
}

/**
 * 账号权限配置
 */
export interface AccountPermission {
  id: string;
  accountId: string;
  userId: string;
  userName: string;
  operations: string[];
  resourceScope: string[];
  createdAt: string;
}

/**
 * 账号审计日志
 */
export interface AccountAuditLog {
  id: string;
  accountId: string;
  accountName: string;
  operator: string;
  operation: string;
  resourceType: string;
  resourceId: string;
  detail: string;
  status: "success" | "failed";
  createdAt: string;
}

/**
 * 多云资源统计
 */
export interface MultiCloudStatistics {
  totalResources: number;
  byProvider: Record<CloudProvider, number>;
  byRegion: Record<string, number>;
  byType: Record<CloudResourceType, number>;
  byStatus: Record<CloudResourceStatus, number>;
  totalCost: number;
  costByProvider: Record<CloudProvider, number>;
}

/**
 * 云资源
 */
export interface CloudResource {
  id: string;
  accountId: string;
  accountName: string;
  provider: CloudProvider;
  region: string;
  resourceType: CloudResourceType;
  resourceId: string;
  resourceName: string;
  status: CloudResourceStatus;
  config: Record<string, unknown>;
  tags: Record<string, string>;
  cost: number;
  createdAt: string;
  updatedAt: string;
}

/**
 * 资源查询参数
 */
export interface CloudResourceQueryParams {
  current?: number;
  pageSize?: number;
  accountId?: string;
  provider?: CloudProvider;
  region?: string;
  resourceType?: CloudResourceType;
  status?: CloudResourceStatus;
  keyword?: string;
  tags?: Record<string, string>;
}

/**
 * 批量操作类型
 */
export enum BatchOperationType {
  START = "start",
  STOP = "stop",
  RESTART = "restart",
  TERMINATE = "terminate",
  RESIZE = "resize",
  TAG = "tag",
}

/**
 * 批量操作请求
 */
export interface BatchOperationRequest {
  resourceIds: string[];
  operation: BatchOperationType;
  params?: Record<string, unknown>;
}

/**
 * 批量操作结果
 */
export interface BatchOperationResult {
  success: string[];
  failed: Array<{ id: string; reason: string }>;
}

/**
 * 镜像/快照信息
 */
export interface CloudImage {
  id: string;
  accountId: string;
  accountName: string;
  provider: CloudProvider;
  region: string;
  imageId: string;
  imageName: string;
  type: "image" | "snapshot";
  size: number;
  status: string;
  createdAt: string;
}

/**
 * 网络配置
 */
export interface CloudNetworkConfig {
  id: string;
  accountId: string;
  accountName: string;
  provider: CloudProvider;
  type: "vpc_peering" | "direct_connect" | "vpn";
  name: string;
  sourceRegion: string;
  targetRegion: string;
  status: string;
  bandwidth: number;
  config: Record<string, unknown>;
  createdAt: string;
}

/**
 * 成本统计
 */
export interface CostStatistics {
  totalCost: number;
  byProvider: Record<CloudProvider, number>;
  byRegion: Record<string, number>;
  byType: Record<CloudResourceType, number>;
  byProject: Record<string, number>;
  trend: Array<{ date: string; cost: number }>;
  forecast: Array<{ date: string; cost: number }>;
}

/**
 * 闲置资源
 */
export interface IdleResource {
  id: string;
  resourceId: string;
  resourceName: string;
  provider: CloudProvider;
  region: string;
  resourceType: CloudResourceType;
  reason: string;
  monthlyCost: number;
  lastActiveTime?: string;
  suggestedAction: string;
}

/**
 * RI/SP 推荐
 */
export interface ReservedInstanceRecommendation {
  id: string;
  provider: CloudProvider;
  resourceType: string;
  instanceType: string;
  region: string;
  onDemandCost: number;
  reservedCost: number;
  savings: number;
  savingsRate: number;
  term: string;
  paymentOption: string;
}

/**
 * 跨云备份策略
 */
export interface CrossCloudBackupPolicy {
  id: string;
  name: string;
  sourceAccountId: string;
  sourceAccountName: string;
  sourceRegion: string;
  targetAccountId: string;
  targetAccountName: string;
  targetRegion: string;
  resourceIds: string[];
  schedule: string;
  retentionDays: number;
  enabled: boolean;
  lastRunTime?: string;
  nextRunTime?: string;
  createdAt: string;
}

/**
 * 容灾配置
 */
export interface DisasterRecoveryConfig {
  id: string;
  name: string;
  primaryAccountId: string;
  primaryAccountName: string;
  primaryRegion: string;
  standbyAccountId: string;
  standbyAccountName: string;
  standbyRegion: string;
  resourceIds: string[];
  replicationMode: "sync" | "async";
  failoverMode: "manual" | "auto";
  healthCheckInterval: number;
  enabled: boolean;
  createdAt: string;
}

/**
 * 弹性伸缩策略
 */
export interface ScalingPolicy {
  id: string;
  name: string;
  accountId: string;
  accountName: string;
  region: string;
  resourceType: CloudResourceType;
  minSize: number;
  maxSize: number;
  desiredSize: number;
  scalingRules: ScalingRule[];
  enabled: boolean;
  createdAt: string;
}

/**
 * 伸缩规则
 */
export interface ScalingRule {
  id: string;
  name: string;
  metricType: "cpu" | "memory" | "network" | "custom";
  threshold: number;
  comparison: "gt" | "lt" | "gte" | "lte";
  action: "scale_out" | "scale_in";
  actionValue: number;
  cooldownMinutes: number;
}

/**
 * 负载调度配置
 */
export interface LoadBalancingConfig {
  id: string;
  name: string;
  type: "weighted" | "least_conn" | "geo";
  backends: LoadBalancingBackend[];
  healthCheck: {
    path: string;
    interval: number;
    timeout: number;
    unhealthyThreshold: number;
  };
  enabled: boolean;
  createdAt: string;
}

/**
 * 负载均衡后端
 */
export interface LoadBalancingBackend {
  accountId: string;
  accountName: string;
  provider: CloudProvider;
  region: string;
  weight: number;
  endpoint: string;
  healthy: boolean;
}

/**
 * 分页结果
 */
export interface PageResult<T> {
  data: T[];
  total: number;
  current: number;
  pageSize: number;
}