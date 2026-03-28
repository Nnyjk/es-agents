export type ExecutionStatus =
  | "PENDING"
  | "RUNNING"
  | "SUCCESS"
  | "FAILED"
  | "CANCELLED";

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
