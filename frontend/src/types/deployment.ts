/**
 * 部署与发布管理类型定义
 */

/**
 * 应用状态
 */
export type ApplicationStatus = "active" | "inactive" | "archived";

/**
 * 流水线状态
 */
export type PipelineStatus =
  | "pending"
  | "running"
  | "success"
  | "failed"
  | "cancelled"
  | "skipped";

/**
 * 发布状态
 */
export type ReleaseStatus =
  | "draft"
  | "pending"
  | "approved"
  | "deploying"
  | "success"
  | "failed"
  | "cancelled"
  | "rolled_back";

/**
 * 发布类型
 */
export type ReleaseType = "major" | "minor" | "patch" | "hotfix";

/**
 * 流水线类型
 */
export type PipelineType = "build" | "deploy" | "build-deploy";

/**
 * 应用配置
 */
export interface ApplicationConfig {
  repositoryUrl: string;
  branch: string;
  buildScript: string;
  deployPath: string;
  healthCheckUrl: string;
  buildCommand: string;
  startCommand: string;
  stopCommand: string;
}

/**
 * 应用
 */
export interface Application {
  id: string;
  name: string;
  project: string;
  owner: string;
  techStack: string[];
  currentVersion?: string;
  status: ApplicationStatus;
  config: ApplicationConfig;
  environments?: ApplicationEnvironment[];
  createdAt?: string;
  updatedAt?: string;
}

/**
 * 应用环境状态
 */
export interface ApplicationEnvironment {
  environmentId: string;
  environmentName: string;
  version: string;
  status: "running" | "stopped" | "deploying" | "error";
  lastDeployTime?: string;
  healthStatus: "healthy" | "unhealthy" | "unknown";
}

/**
 * 流水线阶段配置
 */
export interface PipelineStage {
  id: string;
  name: string;
  type: string;
  order: number;
  config?: Record<string, unknown>;
  timeout?: number;
  retryCount?: number;
  status?: PipelineStatus;
}

/**
 * 流水线
 */
export interface Pipeline {
  id: string;
  name: string;
  applicationId: string;
  applicationName?: string;
  stages: PipelineStage[];
  type: PipelineType;
  status: PipelineStatus;
  triggerType?: "manual" | "auto" | "schedule";
  description?: string;
  lastExecutionAt?: string;
  createdAt?: string;
  updatedAt?: string;
}

/**
 * 流水线执行记录
 */
export interface PipelineExecution {
  id: string;
  pipelineId: string;
  buildNumber: number;
  status: PipelineStatus;
  triggerType: "manual" | "auto" | "webhook";
  triggerUser?: string;
  startTime: string;
  endTime?: string;
  duration?: number;
  stages: StageExecution[];
  logs?: string;
}

/**
 * 阶段执行记录
 */
export interface StageExecution {
  id: string;
  name: string;
  status: PipelineStatus;
  startTime?: string;
  endTime?: string;
  duration?: number;
  steps?: string[];
  logs?: string;
  error?: string;
}

/**
 * 发布工单
 */
export interface Release {
  id: string;
  releaseId: string;
  applicationId: string;
  applicationName: string;
  environmentId: string;
  environmentName: string;
  version: string;
  type: ReleaseType;
  status: ReleaseStatus;
  applicant: string;
  approver?: string;
  scheduledAt?: string;
  deployedAt?: string;
  completedAt?: string;
  releaseNotes: string;
  changes?: ReleaseChanges;
  deployProgress?: DeployProgress;
  rollbackFrom?: string;
  createdAt: string;
  updatedAt?: string;
}

/**
 * 发布变更内容
 */
export interface ReleaseChanges {
  features?: string[];
  fixes?: string[];
  others?: string[];
}

/**
 * 发布部署进度
 */
export interface DeployProgress {
  currentStep: number;
  totalSteps: number;
  steps?: string[];
  deployedInstances: number;
  totalInstances: number;
}

/**
 * 灰度发布配置
 */
export interface CanaryConfig {
  enabled: boolean;
  trafficPercentage: number;
  userGroups: string[];
  rules: CanaryRule[];
}

/**
 * 灰度规则
 */
export interface CanaryRule {
  id: string;
  type: "header" | "cookie" | "query" | "ip";
  key: string;
  value: string;
  operator: "equals" | "contains" | "regex";
}

/**
 * 环境
 */
export interface Environment {
  id: string;
  name: string;
  type: "dev" | "test" | "staging" | "prod";
  description?: string;
  clusterEndpoint: string;
  namespace?: string;
  status: "active" | "inactive" | "maintenance";
  healthStatus?: "healthy" | "unhealthy" | "unknown";
  applicationCount?: number;
  resourceUsage?: ResourceUsage;
  autoDeploy?: boolean;
  createdAt: string;
  updatedAt: string;
}

/**
 * 资源使用情况
 */
export interface ResourceUsage {
  cpu: number;
  memory: number;
}

/**
 * 环境资源
 */
export interface EnvironmentResource {
  nodes: {
    total: number;
    healthy: number;
  };
  cpu: {
    total: number;
    used: number;
  };
  memory: {
    total: number;
    used: number;
  };
  storage: {
    total: number;
    used: number;
  };
}

/**
 * 环境应用
 */
export interface EnvironmentApplication {
  applicationId: string;
  applicationName: string;
  version: string;
  status: "running" | "stopped" | "deploying" | "error";
  replicas: number;
  readyReplicas: number;
  healthStatus: "healthy" | "unhealthy" | "unknown";
  updatedAt: string;
}

/**
 * 应用查询参数
 */
export interface ApplicationQueryParams {
  current: number;
  pageSize: number;
  name?: string;
  project?: string;
  owner?: string;
  status?: ApplicationStatus;
}

/**
 * 流水线查询参数
 */
export interface PipelineQueryParams {
  current: number;
  pageSize: number;
  name?: string;
  type?: PipelineType;
}

/**
 * 发布查询参数
 */
export interface ReleaseQueryParams {
  current: number;
  pageSize: number;
  releaseId?: string;
  version?: string;
  type?: ReleaseType;
  status?: ReleaseStatus;
}

/**
 * 环境查询参数
 */
export interface EnvironmentQueryParams {
  current: number;
  pageSize: number;
  name?: string;
  type?: string;
  status?: string;
}

/**
 * 分页结果
 */
export interface PageResult<T> {
  list: T[];
  total: number;
  current: number;
  pageSize: number;
}
