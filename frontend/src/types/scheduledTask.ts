// 定时任务类型
export type TaskType =
  | "COMMAND"
  | "DEPLOYMENT"
  | "BACKUP"
  | "SYNC"
  | "CLEANUP"
  | "CUSTOM";

export type TaskStatus = "ENABLED" | "DISABLED" | "PAUSED";

export type ExecutionStatus =
  | "PENDING"
  | "RUNNING"
  | "SUCCESS"
  | "FAILED"
  | "CANCELLED"
  | "TIMEOUT";

// 定时任务
export interface ScheduledTask {
  id: string;
  name: string;
  type: TaskType;
  description?: string;
  cronExpression: string;
  status: TaskStatus;
  config?: string;
  targetId?: string;
  targetType?: string;
  maxRetries?: number;
  timeoutSeconds?: number;
  alertOnFailure?: boolean;
  createdBy?: string;
  lastExecuteTime?: string;
  nextExecuteTime?: string;
  createdAt: string;
  updatedAt: string;
}

// 任务执行记录
export interface TaskExecution {
  id: string;
  taskId: string;
  taskName?: string;
  status: ExecutionStatus;
  startTime: string;
  endTime?: string;
  duration?: number;
  output?: string;
  errorMessage?: string;
  executeNode?: string;
  retryCount?: number;
}

// 查询参数
export interface ScheduledTaskQueryParams {
  keyword?: string;
  type?: TaskType;
  status?: TaskStatus;
  limit?: number;
  offset?: number;
}

// 创建任务参数
export interface CreateScheduledTaskParams {
  name: string;
  type: TaskType;
  description?: string;
  cronExpression: string;
  config?: string;
  targetId?: string;
  targetType?: string;
  maxRetries?: number;
  timeoutSeconds?: number;
  alertOnFailure?: boolean;
  createdBy?: string;
}

// 更新任务参数
export interface UpdateScheduledTaskParams {
  name?: string;
  description?: string;
  cronExpression?: string;
  status?: TaskStatus;
  config?: string;
  targetId?: string;
  targetType?: string;
  maxRetries?: number;
  timeoutSeconds?: number;
  alertOnFailure?: boolean;
}

// 任务类型配置
export const taskTypeConfig: Record<
  TaskType,
  { label: string; color: string; description: string }
> = {
  COMMAND: {
    label: "命令执行",
    color: "blue",
    description: "执行指定的命令或脚本",
  },
  DEPLOYMENT: {
    label: "部署任务",
    color: "green",
    description: "执行应用部署流程",
  },
  BACKUP: {
    label: "备份任务",
    color: "orange",
    description: "执行数据备份操作",
  },
  SYNC: {
    label: "同步任务",
    color: "cyan",
    description: "执行数据同步操作",
  },
  CLEANUP: {
    label: "清理任务",
    color: "purple",
    description: "执行清理操作",
  },
  CUSTOM: {
    label: "自定义任务",
    color: "default",
    description: "用户自定义任务",
  },
};

// 任务状态配置
export const taskStatusConfig: Record<
  TaskStatus,
  { label: string; color: string }
> = {
  ENABLED: { label: "已启用", color: "success" },
  DISABLED: { label: "已禁用", color: "default" },
  PAUSED: { label: "已暂停", color: "warning" },
};

// 执行状态配置
export const executionStatusConfig: Record<
  ExecutionStatus,
  { label: string; color: string }
> = {
  PENDING: { label: "等待中", color: "default" },
  RUNNING: { label: "执行中", color: "processing" },
  SUCCESS: { label: "成功", color: "success" },
  FAILED: { label: "失败", color: "error" },
  CANCELLED: { label: "已取消", color: "warning" },
  TIMEOUT: { label: "超时", color: "error" },
};
