export interface CurrentUser {
  id: string;
  username: string;
  roles: string[];
  permissions?: {
    menus: string[];
    actions: string[];
  };
}

export interface LoginResult {
  token: string;
  userInfo: CurrentUser;
  permissions: {
    menus: string[];
    actions: string[];
  };
}

export interface User {
  id: string;
  username: string;
  status: "ACTIVE" | "INACTIVE" | "LOCKED";
  roles: Role[];
  roleIds?: string[];
  password?: string;
}

export interface Role {
  id: string;
  code: string;
  name: string;
  description?: string;
  moduleIds?: string[];
  actionIds?: string[];
}

export interface Module {
  id: string;
  parentId?: string;
  name: string;
  code: string;
  type: "DIRECTORY" | "MENU" | "BUTTON";
  path?: string;
  icon?: string;
  sortOrder?: number;
  children?: Module[];
}

export interface Permission {
  id: string;
  code: string;
  name: string;
  description?: string;
  resource: string;
  action: string;
  system: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface PermissionQueryParams {
  keyword?: string;
  resource?: string;
  action?: string;
  limit?: number;
  offset?: number;
}

import { Host } from "./infrastructure";

export interface AgentCredential {
  id: string;
  name: string;
  type: "STATIC_TOKEN" | "API_TOKEN" | "SCRIPT_TOKEN" | "SSO_TOKEN";
  config: string;
  createdAt: string;
  updatedAt: string;
}

export interface AgentCredentialSimple {
  id: string;
  name: string;
  type: "STATIC_TOKEN" | "API_TOKEN" | "SCRIPT_TOKEN" | "SSO_TOKEN";
}

export interface AgentRepository {
  id: string;
  name: string;
  type: "GITLAB" | "MAVEN" | "NEXTCLOUD";
  baseUrl: string;
  projectPath: string;
  defaultBranch?: string;
  credential?: AgentCredentialSimple;
  credentialId?: string;
  createdAt: string;
  updatedAt: string;
}

export interface AgentRepositorySimple {
  id: string;
  name: string;
  type: "GITLAB" | "MAVEN" | "NEXTCLOUD";
}

export interface AgentResource {
  id: string;
  name: string;
  type:
    | "GITLAB"
    | "MAVEN"
    | "NEXTCLOUD"
    | "GIT"
    | "DOCKER"
    | "HTTPS"
    | "HTTP"
    | "LOCAL"
    | "ALIYUN";
  config: string;
  repository?: AgentRepositorySimple;
  repositoryId?: string;
  credential?: AgentCredentialSimple;
  credentialId?: string;
  createdAt: string;
  updatedAt: string;
}

// 模板分类枚举
export type TemplateCategory =
  | "MONITORING"
  | "DEPLOYMENT"
  | "BACKUP"
  | "SECURITY"
  | "DATABASE"
  | "NETWORK"
  | "UTILITY"
  | "CUSTOM";

// 操作系统类型枚举
export type OsType = "ALL" | "LINUX" | "WINDOWS" | "MACOS" | "LINUX_DOCKER";

export interface AgentCommand {
  id: string;
  name: string;
  script: string;
  timeout: number;
  defaultArgs?: string;
  templateId?: string;
  hostId?: string;
}

// 命令创建/更新参数
export interface AgentCommandCreate {
  name: string;
  script: string;
  timeout?: number;
  defaultArgs?: string;
  templateId?: string;
}

export interface AgentCommandUpdate {
  name?: string;
  script?: string;
  timeout?: number;
  defaultArgs?: string;
}

export interface AgentTemplate {
  id: string;
  name: string;
  description?: string;
  category?: TemplateCategory;
  osType?: OsType;
  archSupport?: string;
  installScript?: string;
  configTemplate?: string;
  dependencies?: string;
  source?: AgentResource;
  sourceId?: string;
  commands?: AgentCommand[];
  deploymentCount?: number;
  successCount?: number;
  successRate?: number;
  createdAt: string;
  updatedAt: string;
}

// 模板创建参数
export interface AgentTemplateCreate {
  name: string;
  description?: string;
  category?: TemplateCategory;
  osType?: OsType;
  archSupport?: string;
  installScript?: string;
  configTemplate?: string;
  dependencies?: string;
  sourceId: string;
  commands?: AgentCommandCreate[];
}

// 模板更新参数
export interface AgentTemplateUpdate {
  name?: string;
  description?: string;
  category?: TemplateCategory;
  osType?: OsType;
  archSupport?: string;
  installScript?: string;
  configTemplate?: string;
  dependencies?: string;
  sourceId?: string;
  commands?: AgentCommandUpdate[];
}

// 模板查询参数
export interface AgentTemplateQueryParams {
  osType?: OsType;
  sourceType?: string;
  category?: TemplateCategory;
}

export type AgentStatus =
  | "OFFLINE"
  | "ONLINE"
  | "BUSY"
  | "UNCONFIGURED"
  | "DEPLOYING"
  | "DEPLOYED"
  | "EXCEPTION";

export interface AgentInstance {
  id: string;
  host: Host;
  hostId?: string; // Form usage
  template: AgentTemplate;
  templateId?: string; // Form usage
  status: AgentStatus;
  version?: string;
  lastHeartbeatTime?: string;
  deployedAt?: string;
  createdAt: string;
  updatedAt: string;
}

// 部署相关类型
export interface DeployParams {
  version: string;
  remarks?: string;
}

export interface DeployResult {
  instanceId: string;
  status: AgentStatus;
  message: string;
  deployedAt: string;
}

// 部署进度 WebSocket 消息类型
export type DeploymentStatus = "PENDING" | "RUNNING" | "SUCCESS" | "FAILED";
export type DeploymentMessageType =
  | "DEPLOYMENT_PROGRESS"
  | "DEPLOYMENT_STATUS"
  | "DEPLOYMENT_ERROR";

export interface DeploymentProgressMessage {
  type: DeploymentMessageType;
  deploymentId: string;
  status?: DeploymentStatus;
  progress?: number;
  stage?: string;
  message?: string;
  timestamp: string;
}

export interface DeploymentStage {
  name: string;
  status: DeploymentStatus;
  progress: number;
  message?: string;
}

export interface ExecuteCommandParams {
  commandId: string;
  args?: string;
}

// Agent 任务相关类型
export type AgentTaskStatus =
  | "PENDING"
  | "RUNNING"
  | "SUCCESS"
  | "FAILED"
  | "CANCELLED"
  | "TIMEOUT";

export interface AgentTask {
  id: string;
  agentInstanceId: string;
  agentInstanceName?: string;
  commandName?: string;
  commandId?: string;
  args?: string;
  result?: string;
  status: AgentTaskStatus;
  durationMs?: number;
  createdAt: string;
  updatedAt?: string;
}

export interface AgentTaskQueryParams {
  agentInstanceId?: string;
  status?: AgentTaskStatus;
  startTime?: string;
  endTime?: string;
  page?: number;
  pageSize?: number;
}

export interface ExecuteCommandForm {
  agentInstanceId: string;
  commandId: string;
  args?: string;
}

export interface PageParams {
  current?: number;
  pageSize?: number;
  page?: number;
}

export interface ListResponse<T> {
  data: T[];
  total: number;
  success: boolean;
}

// 告警相关类型
export type AlertLevel = "INFO" | "WARNING" | "ERROR" | "CRITICAL";
export type AlertStatus = "ACTIVE" | "ACKNOWLEDGED" | "RESOLVED" | "IGNORED";
export type AlertRuleStatus = "ENABLED" | "DISABLED";
export type AlertChannelType =
  | "EMAIL"
  | "WEBHOOK"
  | "DINGTALK"
  | "WECHAT"
  | "SLACK";

export interface Alert {
  id: string;
  ruleId?: string;
  ruleName?: string;
  level: AlertLevel;
  title: string;
  content: string;
  source: string;
  status: AlertStatus;
  labels?: Record<string, string>;
  acknowledgedBy?: string;
  acknowledgedAt?: string;
  resolvedAt?: string;
  createdAt: string;
  updatedAt: string;
}

export interface AlertRule {
  id: string;
  name: string;
  description?: string;
  level: AlertLevel;
  source: string;
  condition: string;
  duration: number;
  channels: AlertChannel[];
  channelIds?: string[];
  status: AlertRuleStatus;
  labels?: Record<string, string>;
  silencePeriod?: number;
  createdAt: string;
  updatedAt: string;
}

export interface AlertChannel {
  id: string;
  name: string;
  type: AlertChannelType;
  config: string;
  description?: string;
  status: "ENABLED" | "DISABLED";
  lastTestAt?: string;
  lastTestResult?: string;
  createdAt: string;
  updatedAt: string;
}

export interface AlertStatistics {
  total: number;
  active: number;
  acknowledged: number;
  resolved: number;
  ignored: number;
  byLevel: Record<AlertLevel, number>;
}

export interface AlertRuleTestResult {
  success: boolean;
  message: string;
  matchedCount: number;
  sampleAlerts?: Alert[];
}

export interface AlertChannelTestResult {
  success: boolean;
  message: string;
  responseTime?: number;
}

// 基础设施相关类型
export * from "./infrastructure";

// 审计日志相关类型
export * from "./audit";

// 命令执行相关类型（新 API）
export * from "./command";
