/**
 * 配置中心相关类型定义
 */

/**
 * 配置项实体
 */
export interface ConfigItem {
  id: string;
  key: string;
  value: string;
  environment: string;
  group?: string;
  description?: string;
  active: boolean;
  version: number;
  createdBy?: string;
  createdAt: string;
  updatedAt: string;
}

/**
 * 配置项创建参数
 */
export interface ConfigItemCreate {
  key: string;
  value: string;
  environment: string;
  group?: string;
  description?: string;
  active?: boolean;
}

/**
 * 配置项更新参数
 */
export interface ConfigItemUpdate {
  key?: string;
  value?: string;
  environment?: string;
  group?: string;
  description?: string;
  active?: boolean;
}

/**
 * 配置项查询参数
 */
export interface ConfigItemQueryParams {
  key?: string;
  environment?: string;
  group?: string;
  active?: boolean;
  keyword?: string;
  page?: number;
  pageSize?: number;
}

/**
 * 配置版本历史
 */
export interface ConfigVersion {
  id: string;
  configId: string;
  version: number;
  key: string;
  value: string;
  environment: string;
  group?: string;
  description?: string;
  active: boolean;
  createdBy?: string;
  createdAt: string;
  changeReason?: string;
}

/**
 * 配置版本查询参数
 */
export interface ConfigVersionQueryParams {
  configId?: string;
  key?: string;
  environment?: string;
  page?: number;
  pageSize?: number;
}

/**
 * 配置环境对比差异项
 */
export interface ConfigDiffItem {
  key: string;
  group?: string;
  sourceEnvironment: string;
  sourceValue?: string;
  targetEnvironment: string;
  targetValue?: string;
  diffType: "ADDED" | "REMOVED" | "MODIFIED";
}

/**
 * 配置环境对比结果
 */
export interface ConfigDiffResult {
  sourceEnvironment: string;
  targetEnvironment: string;
  diffs: ConfigDiffItem[];
  totalDiffs: number;
}

/**
 * 配置回滚请求
 */
export interface ConfigRollbackRequest {
  versionId: string;
  reason?: string;
}

/**
 * 配置回滚结果
 */
export interface ConfigRollbackResult {
  configId: string;
  newVersion: number;
  success: boolean;
  message?: string;
}

/**
 * 环境列表项
 */
export interface ConfigEnvironment {
  name: string;
  description?: string;
  configCount?: number;
}
