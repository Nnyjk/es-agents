export interface SystemEventLog {
  id: number;
  eventType: string;
  eventLevel: "INFO" | "WARN" | "ERROR" | "DEBUG";
  module: string;
  action: string;
  status: "SUCCESS" | "FAILURE" | "PENDING";
  userId?: number;
  username?: string;
  agentId?: number;
  agentName?: string;
  goalId?: number;
  goalName?: string;
  batchOperationId?: number;
  description: string;
  metadata?: string;
  errorMessage?: string;
  duration?: number;
  createdAt: string;
}

export interface SystemEventLogDTO {
  eventType: string;
  eventLevel: "INFO" | "WARN" | "ERROR" | "DEBUG";
  module: string;
  action: string;
  status: "SUCCESS" | "FAILURE" | "PENDING";
  userId?: number;
  agentId?: number;
  goalId?: number;
  batchOperationId?: number;
  description: string;
  metadata?: string;
  errorMessage?: string;
  duration?: number;
}

export interface EventQueryCriteria {
  eventType?: string;
  eventLevel?: string;
  module?: string;
  action?: string;
  status?: string;
  userId?: number;
  agentId?: number;
  goalId?: number;
  batchOperationId?: number;
  startTime?: string;
  endTime?: string;
  page?: number;
  size?: number;
}

export interface EventLogPage {
  items: SystemEventLog[];
  total: number;
  page: number;
  size: number;
}
