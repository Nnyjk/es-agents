import request from "../utils/request";
import type {
  AgentRuntimeStatus,
  AgentHealthRecord,
  AgentInstanceRecord,
  MetricDetail,
  MetricTrend,
  MetricAggregate,
  AgentMonitorOverview,
  AgentTaskRecord,
  MetricQueryParams,
  AgentListParams,
  AgentLogRecord,
  LogQueryParams,
  LogStats,
  DeploymentLogQueryParams,
  CommandLogQueryParams,
  CommandExecutionRecord,
  CommandExecutionDetail,
  ExecuteCommandParams,
} from "../types/agentMonitoring";
import type { ListResponse, PageParams } from "../types";

/**
 * Agent 监控服务 API
 */
export const agentMonitoringService = {
  /**
   * 获取 Agent 监控概览统计
   */
  getOverview: async (): Promise<AgentMonitorOverview> => {
    const response = await request.get<AgentMonitorOverview>(
      "/agents/monitoring/overview",
    );
    return response.data;
  },

  /**
   * 获取 Agent 运行时状态列表
   */
  getRuntimeStatusList: async (
    params?: AgentListParams,
  ): Promise<ListResponse<AgentRuntimeStatus>> => {
    const response = await request.get<ListResponse<AgentRuntimeStatus>>(
      "/agents/instances/runtime-status",
      { params },
    );
    return response.data;
  },

  /**
   * 获取单个 Agent 运行时状态
   */
  getRuntimeStatus: async (id: string): Promise<AgentRuntimeStatus> => {
    const response = await request.get<AgentRuntimeStatus>(
      `/agents/instances/${id}/runtime-status`,
    );
    return response.data;
  },

  /**
   * 获取 Agent 健康状态列表
   */
  getHealthList: async (
    params?: AgentListParams,
  ): Promise<ListResponse<AgentHealthRecord>> => {
    const response = await request.get<ListResponse<AgentHealthRecord>>(
      "/agents/instances/health",
      { params },
    );
    return response.data;
  },

  /**
   * 获取 Agent 实例列表
   */
  getInstanceList: async (
    params?: PageParams & { hostId?: string },
  ): Promise<ListResponse<AgentInstanceRecord>> => {
    const response = await request.get<ListResponse<AgentInstanceRecord>>(
      "/agents/instances",
      { params },
    );
    return response.data;
  },

  /**
   * 获取 Agent 实例详情
   */
  getInstance: async (id: string): Promise<AgentInstanceRecord> => {
    const response = await request.get<AgentInstanceRecord>(
      `/agents/instances/${id}`,
    );
    return response.data;
  },

  /**
   * 获取 Agent 指标列表
   */
  getMetrics: async (
    params: MetricQueryParams,
  ): Promise<ListResponse<MetricDetail>> => {
    const response = await request.get<ListResponse<MetricDetail>>(
      "/agents/metrics",
      { params },
    );
    return response.data;
  },

  /**
   * 获取 Agent 指标趋势
   */
  getMetricTrend: async (
    agentId: string,
    types: string[],
    startTime?: string,
    endTime?: string,
  ): Promise<MetricTrend[]> => {
    const response = await request.get<MetricTrend[]>(`/agents/metrics/trend`, {
      params: {
        agentId,
        types: types.join(","),
        startTime,
        endTime,
      },
    });
    return response.data;
  },

  /**
   * 获取 Agent 指标聚合
   */
  getMetricAggregate: async (
    agentId: string,
    types: string[],
    startTime?: string,
    endTime?: string,
  ): Promise<MetricAggregate[]> => {
    const response = await request.get<MetricAggregate[]>(
      `/agents/metrics/aggregate`,
      {
        params: {
          agentId,
          types: types.join(","),
          startTime,
          endTime,
        },
      },
    );
    return response.data;
  },

  /**
   * 获取 Agent 任务列表
   */
  getTaskList: async (
    agentId: string,
    params?: PageParams,
  ): Promise<ListResponse<AgentTaskRecord>> => {
    const response = await request.get<ListResponse<AgentTaskRecord>>(
      `/agents/instances/${agentId}/tasks`,
      { params },
    );
    return response.data;
  },

  /**
   * 获取 Agent 任务详情
   */
  getTask: async (
    agentId: string,
    taskId: string,
  ): Promise<AgentTaskRecord> => {
    const response = await request.get<AgentTaskRecord>(
      `/agents/instances/${agentId}/tasks/${taskId}`,
    );
    return response.data;
  },

  /**
   * 取消 Agent 任务
   */
  cancelTask: async (agentId: string, taskId: string): Promise<void> => {
    await request.post(`/agents/instances/${agentId}/tasks/${taskId}/cancel`);
  },

  /**
   * 重跑 Agent 任务
   */
  rerunTask: async (agentId: string, taskId: string): Promise<void> => {
    await request.post(`/agents/instances/${agentId}/tasks/${taskId}/rerun`);
  },

  // ========== 日志相关 API ==========

  /**
   * 查询 Agent 日志
   */
  getLogs: async (params: LogQueryParams): Promise<AgentLogRecord> => {
    const { agentId, ...rest } = params;
    const response = await request.get<AgentLogRecord>(
      `/agents/logs/${agentId}`,
      { params: rest },
    );
    return response.data;
  },

  /**
   * 获取日志统计
   */
  getLogStats: async (agentId: string): Promise<LogStats> => {
    const response = await request.get<LogStats>(
      `/agents/logs/${agentId}/stats`,
    );
    return response.data;
  },

  /**
   * 获取最新日志
   */
  getLatestLogs: async (
    agentId: string,
    limit: number = 100,
  ): Promise<LogEntry[]> => {
    const response = await request.get<LogEntry[]>(
      `/agents/logs/${agentId}/tail`,
      { params: { limit } },
    );
    return response.data;
  },

  /**
   * 查询部署日志
   */
  getDeploymentLogs: async (
    params: DeploymentLogQueryParams,
  ): Promise<AgentLogRecord> => {
    const { agentId, ...rest } = params;
    const response = await request.get<AgentLogRecord>(
      `/agents/logs/${agentId}/deployment`,
      { params: rest },
    );
    return response.data;
  },

  /**
   * 查询命令执行日志
   */
  getCommandLogs: async (
    params: CommandLogQueryParams,
  ): Promise<AgentLogRecord> => {
    const { agentId, executionId, ...rest } = params;
    const response = await request.get<AgentLogRecord>(
      `/agents/logs/${agentId}/command/${executionId}`,
      { params: rest },
    );
    return response.data;
  },

  // ========== 命令执行相关 API ==========

  /**
   * 执行命令
   */
  executeCommand: async (
    params: ExecuteCommandParams,
  ): Promise<CommandExecutionDetail> => {
    const response = await request.post<CommandExecutionDetail>(
      "/agents/commands/execute",
      params,
    );
    return response.data;
  },

  /**
   * 获取命令执行历史
   */
  getCommandHistory: async (
    agentId: string,
    params?: PageParams,
  ): Promise<ListResponse<CommandExecutionRecord>> => {
    const response = await request.get<ListResponse<CommandExecutionRecord>>(
      `/agents/commands/history/${agentId}`,
      { params },
    );
    return response.data;
  },

  /**
   * 获取命令执行详情
   */
  getCommandExecution: async (
    executionId: string,
  ): Promise<CommandExecutionDetail> => {
    const response = await request.get<CommandExecutionDetail>(
      `/agents/commands/executions/${executionId}`,
    );
    return response.data;
  },
};

export default agentMonitoringService;
