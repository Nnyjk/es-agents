import request from "../utils/request";
import type {
  CommandExecution,
  ExecuteCommandRequest,
  ExecuteCommandResponse,
  CommandExecutionQueryParams,
  CommandExecutionListResponse,
} from "../types/command";

export const executeCommand = async (
  params: ExecuteCommandRequest,
): Promise<ExecuteCommandResponse> => {
  const response = await request.post<ExecuteCommandResponse>(
    "/v1/agent-commands/execute",
    params,
  );
  return response.data;
};

export const getCommandExecution = async (
  executionId: string,
): Promise<CommandExecution> => {
  const response = await request.get<CommandExecution>(
    `/v1/agent-commands/${executionId}`,
  );
  return response.data;
};

export const getCommandExecutionStatus = async (
  executionId: string,
): Promise<CommandExecution> => {
  const response = await request.get<CommandExecution>(
    `/v1/agent-commands/${executionId}/status`,
  );
  return response.data;
};

export const retryCommandExecution = async (
  executionId: string,
): Promise<ExecuteCommandResponse> => {
  const response = await request.post<ExecuteCommandResponse>(
    `/v1/agent-commands/${executionId}/retry`,
  );
  return response.data;
};

export const cancelCommandExecution = async (
  executionId: string,
): Promise<CommandExecution> => {
  const response = await request.post<CommandExecution>(
    `/v1/agent-commands/${executionId}/cancel`,
  );
  return response.data;
};

export const listCommandExecutions = async (
  params: CommandExecutionQueryParams,
): Promise<CommandExecutionListResponse> => {
  const response = await request.get<CommandExecutionListResponse>(
    "/v1/agent-commands",
    { params },
  );
  return response.data;
};
