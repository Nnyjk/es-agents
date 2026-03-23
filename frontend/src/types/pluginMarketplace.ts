/**
 * Agent 插件市场与服务目录类型定义
 */

// ============ 插件市场相关类型 ============

/** 插件分类 */
export type PluginCategory =
  | "monitoring"
  | "operations"
  | "deployment"
  | "security"
  | "data-analysis"
  | "notification"
  | "integration"
  | "other";

/** 插件状态 */
export type PluginStatus = "active" | "inactive" | "error" | "installing" | "updating";

/** 插件安装状态 */
export type InstallStatus = "installed" | "not-installed" | "updating" | "installing";

/** 插件 */
export interface Plugin {
  id: string;
  name: string;
  displayName: string;
  description: string;
  category: PluginCategory;
  version: string;
  author: string;
  logo?: string;
  tags: string[];
  rating: number;
  installCount: number;
  downloadCount: number;
  homepage?: string;
  repository?: string;
  license: string;
  createdAt: string;
  updatedAt: string;
  features: PluginFeature[];
  versions: PluginVersion[];
  screenshots?: string[];
  documentation?: string;
  changelog?: string;
  installed?: boolean;
  installedVersion?: string;
  hasUpdate?: boolean;
}

/** 插件功能特性 */
export interface PluginFeature {
  title: string;
  description: string;
  icon?: string;
}

/** 插件版本 */
export interface PluginVersion {
  version: string;
  releaseNotes: string;
  releasedAt: string;
  compatibility: string;
  downloadUrl: string;
  checksum: string;
}

/** 插件配置项 */
export interface PluginConfigField {
  name: string;
  displayName: string;
  label?: string;
  type: "string" | "text" | "password" | "number" | "boolean" | "select" | "textarea" | "json";
  required: boolean;
  defaultValue?: string | number | boolean;
  options?: { label: string; value: string }[];
  placeholder?: string;
  description?: string;
  validation?: {
    min?: number;
    max?: number;
    pattern?: string;
    message?: string;
  };
}

/** 插件配置 */
export interface PluginConfig {
  pluginId: string;
  fields: PluginConfigField[];
  schema: Record<string, unknown>;
  description?: string;
  values?: Record<string, unknown>;
}

/** 已安装插件 */
export interface InstalledPlugin {
  id: string;
  pluginId: string;
  name: string;
  displayName: string;
  version: string;
  status: PluginStatus;
  installedAt: string;
  updatedAt: string;
  agentId?: string;
  agentName?: string;
  nodeId?: string;
  nodeName?: string;
  config: Record<string, unknown>;
  metrics?: PluginMetrics;
  logs?: string;
  logo?: string;
  hasUpdate?: boolean;
  latestVersion?: string;
}

/** 插件运行指标 */
export interface PluginMetrics {
  cpuUsage: number;
  memoryUsage: number;
  requestCount: number;
  errorCount: number;
  avgResponseTime: number;
  uptime: number;
  lastError?: string;
  lastErrorAt?: string;
}

/** 插件安装进度 */
export interface InstallProgress {
  pluginId: string;
  status: "downloading" | "extracting" | "installing" | "configuring" | "completed" | "failed";
  progress: number;
  message: string;
  error?: string;
}

// ============ 服务目录相关类型 ============

/** 服务分类 */
export type ServiceCategory =
  | "automation"
  | "deployment"
  | "monitoring"
  | "backup"
  | "security"
  | "maintenance"
  | "reporting"
  | "integration";

/** 服务类型 */
export type ServiceType = "script" | "playbook" | "template" | "workflow" | "api";

/** 服务状态 */
export type ServiceStatus = "active" | "inactive" | "deprecated";

/** 服务目录项 */
export interface ServiceCatalogItem {
  id: string;
  name: string;
  displayName: string;
  description: string;
  category: ServiceCategory;
  type: ServiceType;
  version: string;
  author: string;
  icon?: string;
  tags: string[];
  rating: number;
  usageCount: number;
  status: ServiceStatus;
  createdAt: string;
  updatedAt: string;
  inputs: ServiceInput[];
  outputs: ServiceOutput[];
  examples?: ServiceExample[];
  documentation?: string;
  requiredPlugins?: string[];
}

/** 服务输入参数 */
export interface ServiceInput {
  name: string;
  label: string;
  type: "string" | "number" | "boolean" | "array" | "object" | "file";
  required: boolean;
  defaultValue?: unknown;
  description?: string;
  options?: { label: string; value: string }[];
  validation?: {
    min?: number;
    max?: number;
    pattern?: string;
    message?: string;
  };
}

/** 服务输出 */
export interface ServiceOutput {
  name: string;
  label: string;
  type: "string" | "number" | "boolean" | "array" | "object";
  description?: string;
}

/** 服务使用示例 */
export interface ServiceExample {
  name: string;
  description: string;
  inputs: Record<string, unknown>;
  expectedOutput?: Record<string, unknown>;
}

/** 服务执行记录 */
export interface ServiceExecution {
  id: string;
  serviceId: string;
  serviceName: string;
  status: "pending" | "running" | "success" | "failed" | "cancelled";
  inputs: Record<string, unknown>;
  outputs?: Record<string, unknown>;
  startTime: string;
  endTime?: string;
  duration?: number;
  executor: string;
  logs?: string;
  error?: string;
}

/** 我的插件 */
export interface MyPlugin {
  id: string;
  name: string;
  displayName: string;
  description: string;
  category: PluginCategory;
  status: "draft" | "pending" | "approved" | "rejected";
  version: string;
  createdAt: string;
  updatedAt: string;
  downloadCount: number;
  installCount: number;
  rating: number;
  reviewComment?: string;
}

/** 插件审核状态 */
export type PluginReviewStatus = "pending" | "approved" | "rejected";

/** 插件提交请求 */
export interface PluginSubmitRequest {
  name: string;
  displayName: string;
  description: string;
  category: PluginCategory;
  version: string;
  author: string;
  license: string;
  repository?: string;
  homepage?: string;
  tags: string[];
  features: PluginFeature[];
  configSchema?: Record<string, unknown>;
}

// ============ 分页与查询类型 ============

/** 插件查询参数 */
export interface PluginQueryParams {
  page: number;
  pageSize: number;
  keyword?: string;
  category?: PluginCategory;
  status?: InstallStatus;
  sortBy?: "rating" | "installCount" | "downloadCount" | "updatedAt" | "name";
  sortOrder?: "asc" | "desc";
}

/** 已安装插件查询参数 */
export interface InstalledPluginQueryParams {
  page: number;
  pageSize: number;
  keyword?: string;
  status?: PluginStatus;
  agentId?: string;
  nodeId?: string;
}

/** 服务目录查询参数 */
export interface ServiceCatalogQueryParams {
  page: number;
  pageSize: number;
  keyword?: string;
  category?: ServiceCategory;
  type?: ServiceType;
  status?: ServiceStatus;
  sortBy?: "rating" | "usageCount" | "updatedAt" | "name";
  sortOrder?: "asc" | "desc";
}

/** 服务执行查询参数 */
export interface ServiceExecutionQueryParams {
  page: number;
  pageSize: number;
  serviceId?: string;
  status?: "pending" | "running" | "success" | "failed" | "cancelled";
  startTime?: string;
  endTime?: string;
}

/** 分页结果 */
export interface PageResult<T> {
  items: T[];
  total: number;
  page: number;
  pageSize: number;
}