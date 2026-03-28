import request from "../utils/request";
import type {
  ApiKey,
  ApiKeyCreate,
  ApiKeyUpdate,
  ApiKeyQuery,
  ApiKeyRevoke,
  ApiKeyRefresh,
  ApiKeyUsageLog,
  ApiKeyUsageLogQuery,
} from "../pages/settings/ApiKey/types";

// 获取 API Key 列表
export const listApiKeys = async (params?: ApiKeyQuery): Promise<ApiKey[]> => {
  return request.get("/v1/api-keys", { params });
};

// 获取单个 API Key 详情
export const getApiKey = async (id: string): Promise<ApiKey> => {
  return request.get(`/v1/api-keys/${id}`);
};

// 创建 API Key
export const createApiKey = async (data: ApiKeyCreate): Promise<ApiKey> => {
  return request.post("/v1/api-keys", data);
};

// 更新 API Key
export const updateApiKey = async (
  id: string,
  data: ApiKeyUpdate,
): Promise<ApiKey> => {
  return request.put(`/v1/api-keys/${id}`, data);
};

// 删除 API Key
export const deleteApiKey = async (id: string): Promise<void> => {
  return request.delete(`/v1/api-keys/${id}`);
};

// 吊销 API Key
export const revokeApiKey = async (
  id: string,
  data: ApiKeyRevoke,
): Promise<ApiKey> => {
  return request.post(`/v1/api-keys/${id}/revoke`, data);
};

// 刷新 API Key
export const refreshApiKey = async (
  id: string,
  data?: ApiKeyRefresh,
): Promise<ApiKey> => {
  return request.post(`/v1/api-keys/${id}/refresh`, data);
};

// 获取 API Key 访问日志
export const getApiKeyLogs = async (
  id: string,
  params?: ApiKeyUsageLogQuery,
): Promise<ApiKeyUsageLog[]> => {
  return request.get(`/v1/api-keys/${id}/logs`, { params });
};
