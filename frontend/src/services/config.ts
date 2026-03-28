import request from "../utils/request";
import type {
  ConfigItem,
  ConfigItemCreate,
  ConfigItemUpdate,
  ConfigItemQueryParams,
  ConfigVersion,
  ConfigVersionQueryParams,
  ConfigDiffResult,
  ConfigRollbackRequest,
  ConfigRollbackResult,
  ConfigEnvironment,
} from "../types/config";

/**
 * 获取配置项列表
 */
export const listConfigs = async (
  params?: ConfigItemQueryParams,
): Promise<ConfigItem[]> => {
  return request.get("/v1/configs", { params });
};

/**
 * 获取单个配置项详情
 */
export const getConfig = async (id: string): Promise<ConfigItem> => {
  return request.get(`/v1/configs/${id}`);
};

/**
 * 创建配置项
 */
export const createConfig = async (
  data: ConfigItemCreate,
): Promise<ConfigItem> => {
  return request.post("/v1/configs", data);
};

/**
 * 更新配置项
 */
export const updateConfig = async (
  id: string,
  data: ConfigItemUpdate,
): Promise<ConfigItem> => {
  return request.put(`/v1/configs/${id}`, data);
};

/**
 * 删除配置项
 */
export const deleteConfig = async (id: string): Promise<void> => {
  return request.delete(`/v1/configs/${id}`);
};

/**
 * 获取配置项版本历史
 */
export const getConfigVersions = async (
  id: string,
  params?: ConfigVersionQueryParams,
): Promise<ConfigVersion[]> => {
  return request.get(`/v1/configs/${id}/versions`, { params });
};

/**
 * 获取指定版本详情
 */
export const getConfigVersion = async (
  configId: string,
  versionId: string,
): Promise<ConfigVersion> => {
  return request.get(`/v1/configs/${configId}/versions/${versionId}`);
};

/**
 * 回滚到指定版本
 */
export const rollbackConfig = async (
  configId: string,
  data: ConfigRollbackRequest,
): Promise<ConfigRollbackResult> => {
  return request.post(`/v1/configs/${configId}/rollback`, data);
};

/**
 * 对比两个环境的配置差异
 */
export const compareConfigs = async (
  sourceEnvironment: string,
  targetEnvironment: string,
): Promise<ConfigDiffResult> => {
  return request.get("/v1/configs/compare", {
    params: {
      sourceEnvironment,
      targetEnvironment,
    },
  });
};

/**
 * 获取环境列表
 */
export const listEnvironments = async (): Promise<ConfigEnvironment[]> => {
  return request.get("/v1/configs/environments");
};
