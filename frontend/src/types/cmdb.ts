/**
 * CMDB 资源与资产管理类型定义
 */

/**
 * 资源类型枚举
 */
export enum ResourceType {
  SERVER = "server",
  NETWORK_DEVICE = "network_device",
  DATABASE = "database",
  MIDDLEWARE = "middleware",
  APPLICATION = "application",
  STORAGE = "storage",
  OTHER = "other",
}

/**
 * 资源状态枚举
 */
export enum ResourceStatus {
  PLANNING = "planning", // 规划中
  PURCHASING = "purchasing", // 采购中
  ONLINE = "online", // 运行中
  MAINTAINING = "maintaining", // 维护中
  OFFLINE = "offline", // 已下线
  SCRAPPED = "scrapped", // 已报废
}

/**
 * 资源关联关系类型
 */
export enum RelationType {
  DEPENDS_ON = "depends_on", // 依赖
  CONTAINS = "contains", // 包含
  CONNECTS_TO = "connects_to", // 连接
  RUNS_ON = "runs_on", // 运行于
  MANAGES = "manages", // 管理
  BACKUP_FOR = "backup_for", // 备份
}

/**
 * 自定义属性定义
 */
export interface CustomFieldDefinition {
  id: string;
  name: string;
  label: string;
  fieldType: "text" | "number" | "select" | "date" | "textarea" | "boolean";
  required: boolean;
  options?: string[]; // select 类型的选项
  defaultValue?: string;
  validation?: {
    min?: number;
    max?: number;
    pattern?: string;
    message?: string;
  };
}

/**
 * 资源类型配置
 */
export interface ResourceTypeConfig {
  id: string;
  name: string;
  code: ResourceType;
  description?: string;
  icon?: string;
  color?: string;
  customFields: CustomFieldDefinition[];
  createdAt: string;
  updatedAt: string;
}

/**
 * 资源台账
 */
export interface Resource {
  id: string;
  name: string;
  type: ResourceType;
  typeId: string;
  status: ResourceStatus;
  environment: string;
  department: string;
  owner: string;
  users?: string[];
  location?: string;
  ip?: string;
  port?: number;
  vendor?: string;
  model?: string;
  serialNumber?: string;
  purchaseDate?: string;
  warrantyExpiry?: string;
  maintenanceExpiry?: string;
  customFields?: Record<string, string | number | boolean>;
  tags?: string[];
  description?: string;
  createdAt: string;
  updatedAt: string;
  createdBy: string;
  updatedBy: string;
}

/**
 * 资源关联关系
 */
export interface ResourceRelation {
  id: string;
  sourceId: string;
  sourceName: string;
  targetId: string;
  targetName: string;
  relationType: RelationType;
  description?: string;
  createdAt: string;
  createdBy: string;
}

/**
 * 资源拓扑节点
 */
export interface TopologyNode {
  id: string;
  name: string;
  type: ResourceType;
  status: ResourceStatus;
  x: number;
  y: number;
}

/**
 * 资源拓扑边
 */
export interface TopologyEdge {
  id: string;
  source: string;
  target: string;
  relationType: RelationType;
}

/**
 * 资源拓扑图
 */
export interface ResourceTopology {
  nodes: TopologyNode[];
  edges: TopologyEdge[];
}

/**
 * 资源查询参数
 */
export interface ResourceQueryParams {
  current: number;
  pageSize: number;
  name?: string;
  type?: ResourceType;
  status?: ResourceStatus;
  environment?: string;
  department?: string;
  owner?: string;
  tags?: string[];
}

/**
 * 资源导入结果
 */
export interface ResourceImportResult {
  total: number;
  success: number;
  failed: number;
  errors: Array<{
    row: number;
    message: string;
  }>;
}

/**
 * 资源变更历史
 */
export interface ResourceChangeHistory {
  id: string;
  resourceId: string;
  field: string;
  oldValue: string;
  newValue: string;
  changedBy: string;
  changedAt: string;
  reason?: string;
}

/**
 * 到期提醒
 */
export interface ExpiryReminder {
  id: string;
  resourceId: string;
  resourceName: string;
  type: "warranty" | "maintenance" | "certificate";
  expiryDate: string;
  daysRemaining: number;
  notified: boolean;
  createdAt: string;
}

/**
 * 环境配置
 */
export interface Environment {
  id: string;
  name: string;
  code: string;
  description?: string;
}

/**
 * 部门配置
 */
export interface Department {
  id: string;
  name: string;
  code: string;
  parentId?: string;
  description?: string;
}

/**
 * API 响应类型
 */
export interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
}

export interface PageResult<T> {
  list: T[];
  total: number;
  current: number;
  pageSize: number;
}
