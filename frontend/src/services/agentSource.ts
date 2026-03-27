/**
 * Agent 资源来源 API 服务
 */

import request from "../utils/request";
import type {
  AgentSource,
  AgentSourceCreate,
  AgentSourceUpdate,
} from "../types/agent";

const BASE_PATH = "/agents/sources";

/**
 * 获取资源来源列表
 */
export const queryAgentSources = (): Promise<AgentSource[]> => {
  return request.get(BASE_PATH);
};

/**
 * 获取单个资源来源
 */
export const getAgentSource = (id: string): Promise<AgentSource> => {
  return request.get(`${BASE_PATH}/${id}`);
};

/**
 * 创建资源来源
 */
export const createAgentSource = (data: AgentSourceCreate): Promise<AgentSource> => {
  return request.post(BASE_PATH, data);
};

/**
 * 更新资源来源
 */
export const updateAgentSource = (id: string, data: AgentSourceUpdate): Promise<AgentSource> => {
  return request.put(`${BASE_PATH}/${id}`, data);
};

/**
 * 删除资源来源
 */
export const removeAgentSource = (id: string): Promise<void> => {
  return request.delete(`${BASE_PATH}/${id}`);
};

/**
 * 保存资源来源（创建或更新）
 */
export const saveAgentSource = (
  data: Partial<AgentSource> & { id?: string },
): Promise<AgentSource> => {
  if (data.id) {
    const { id, ...payload } = data;
    return request.put(`${BASE_PATH}/${id}`, payload);
  }
  return request.post(BASE_PATH, data);
};

/**
 * 下载资源来源文件
 */
export const downloadAgentSource = (id: string): Promise<Blob> => {
  return request.get(`${BASE_PATH}/${id}/download`, {
    responseType: "blob",
  });
};