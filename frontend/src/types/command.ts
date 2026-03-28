export type ExecutionStatus =
  | "PENDING"
  | "RUNNING"
  | "SUCCESS"
  | "FAILED"
  | "CANCELLED";

// 命令模板分类枚举
export type CommandCategory =
  | "SYSTEM"
  | "DEPLOYMENT"
  | "MONITORING"
  | "MAINTENANCE"
  | "CUSTOM";

// 命令模板类型定义（列表项）
export interface CommandTemplate {
  id: string;
  name: string;
  description?: string;
  category?: CommandCategory;
  tags?: string;
  timeout?: number;
  isActive?: boolean;
  createdAt: string;
  updatedAt?: string;
}

// 命令模板详情
export interface CommandTemplateDetail {
  id: string;
  name: string;
  description?: string;
  script: string;
  category?: CommandCategory;
  tags?: string;
  parameters?: string;
  timeout?: number;
  retryCount?: number;
  isActive?: boolean;
  createdAt: string;
  updatedAt?: string;
  createdBy?: string;
}

// 命令模板参数定义
export interface CommandTemplateParameter {
  name: string;
  type: "STRING" | "NUMBER" | "BOOLEAN" | "SELECT" | "FILE";
  required?: boolean;
  defaultValue?: string;
  description?: string;
  options?: string[]; // 用于 SELECT 类型
}

// 命令模板创建请求
export interface CommandTemplateCreate {
  name: string;
  description?: string;
  script: string;
  category?: CommandCategory;
  tags?: string;
  parameters?: string;
  timeout?: number;
  retryCount?: number;
}

// 命令模板更新请求
export interface CommandTemplateUpdate {
  name?: string;
  description?: string;
  script?: string;
  category?: CommandCategory;
  tags?: string;
  parameters?: string;
  timeout?: number;
  retryCount?: number;
  isActive?: boolean;
}

// 命令模板执行请求
export interface CommandTemplateExecuteRequest {
  agentInstanceId: string;
  parameters?: string;
}

// 命令模板执行响应
export interface CommandTemplateExecuteResponse {
  executionId: string;
  message: string;
}

// 分类标签映射
export const categoryLabels: Record<CommandCategory, string> = {
  SYSTEM: "系统命令",
  DEPLOYMENT: "部署相关",
  MONITORING: "监控相关",
  MAINTENANCE: "维护相关",
  CUSTOM: "自定义",
};

export const categoryColors: Record<CommandCategory, string> = {
  SYSTEM: "blue",
  DEPLOYMENT: "green",
  MONITORING: "purple",
  MAINTENANCE: "orange",
  CUSTOM: "default",
};

export interface CommandExecution {
  id: string;
  templateId?: string;
  templateName?: string;
  agentInstanceId: string;
  hostName?: string;
  command: string;
  status: ExecutionStatus;
  startedAt?: string;
  finishedAt?: string;
  exitCode?: number;
  createdAt: string;
  executedBy: string;
  output?: string;
  errorMessage?: string;
  retryCount?: number;
}

export interface ExecuteCommandRequest {
  agentInstanceId: string;
  templateId?: string;
  command: string;
  parameters?: Record<string, any>;
  timeout?: number;
}

export interface ExecuteCommandResponse {
  executionId: string;
  message: string;
}

export interface CommandExecutionQueryParams {
  agentInstanceId?: string;
  templateId?: string;
  status?: ExecutionStatus;
  executedBy?: string;
  startTime?: string;
  endTime?: string;
  page?: number;
  size?: number;
}

export interface CommandExecutionListResponse {
  items: CommandExecution[];
  total: number;
  page: number;
  size: number;
}
