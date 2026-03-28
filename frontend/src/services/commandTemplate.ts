/**
 * 命令模板管理 API 服务
 */

import request from "../utils/request";
import type {
  CommandTemplate,
  CommandTemplateDetail,
  CommandTemplateCreate,
  CommandTemplateUpdate,
  CommandTemplateExecuteRequest,
  CommandTemplateExecuteResponse,
  CommandCategory,
} from "../types/command";

const BASE_PATH = "/v1/commands/templates";

/**
 * 获取命令模板列表
 */
export const queryCommandTemplates = async (
  category?: CommandCategory,
  activeOnly?: boolean,
): Promise<CommandTemplate[]> => {
  const params: Record<string, string | boolean> = {};
  if (category) params.category = category;
  if (activeOnly !== undefined) params.activeOnly = activeOnly;
  const response = await request.get<CommandTemplate[]>(BASE_PATH, { params });
  return response.data;
};

/**
 * 获取单个命令模板详情
 */
export const getCommandTemplate = async (
  id: string,
): Promise<CommandTemplateDetail> => {
  const response = await request.get<CommandTemplateDetail>(
    `${BASE_PATH}/${id}`,
  );
  return response.data;
};

/**
 * 创建命令模板
 */
export const createCommandTemplate = async (
  data: CommandTemplateCreate,
): Promise<CommandTemplateDetail> => {
  const response = await request.post<CommandTemplateDetail>(BASE_PATH, data);
  return response.data;
};

/**
 * 更新命令模板
 */
export const updateCommandTemplate = async (
  id: string,
  data: CommandTemplateUpdate,
): Promise<CommandTemplateDetail> => {
  const response = await request.put<CommandTemplateDetail>(
    `${BASE_PATH}/${id}`,
    data,
  );
  return response.data;
};

/**
 * 删除命令模板
 */
export const removeCommandTemplate = async (id: string): Promise<void> => {
  await request.delete(`${BASE_PATH}/${id}`);
};

/**
 * 保存命令模板（创建或更新）
 */
export const saveCommandTemplate = async (
  data: Partial<CommandTemplateDetail> & { id?: string },
): Promise<CommandTemplateDetail> => {
  if (data.id) {
    const { id, createdAt, updatedAt, createdBy, ...payload } = data;
    return updateCommandTemplate(id, payload as CommandTemplateUpdate);
  }
  return createCommandTemplate(data as CommandTemplateCreate);
};

/**
 * 执行命令模板测试
 */
export const executeCommandTemplate = async (
  id: string,
  data: CommandTemplateExecuteRequest,
): Promise<CommandTemplateExecuteResponse> => {
  const response = await request.post<CommandTemplateExecuteResponse>(
    `${BASE_PATH}/${id}/execute`,
    data,
  );
  return response.data;
};

/**
 * 获取模板执行历史
 */
export const getTemplateExecutionHistory = async (
  id: string,
): Promise<any[]> => {
  const response = await request.get<any[]>(`${BASE_PATH}/${id}/executions`);
  return response.data;
};

/**
 * 获取执行详情
 */
export const getExecutionDetail = async (executionId: string): Promise<any> => {
  const response = await request.get<any>(
    `${BASE_PATH}/executions/${executionId}`,
  );
  return response.data;
};
