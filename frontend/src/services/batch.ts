/**
 * 批量操作 API 服务
 */

import request from "../utils/request";
import type {
  BatchOperation,
  BatchOperationItem,
  BatchCommandRequest,
  BatchDeployRequest,
  BatchUpgradeRequest,
  BatchOperationListResponse,
  BatchOperationQueryParams,
} from "../types/batch";

/**
 * 批量执行命令
 */
export const batchExecuteCommand = async (
  requestParams: BatchCommandRequest,
): Promise<BatchOperation> => {
  return request.post("/api/v1/batch-operations/commands", requestParams);
};

/**
 * 批量部署
 */
export const batchDeploy = async (
  requestParams: BatchDeployRequest,
): Promise<BatchOperation> => {
  return request.post("/api/v1/batch-operations/deploy", requestParams);
};

/**
 * 批量升级
 */
export const batchUpgrade = async (
  requestParams: BatchUpgradeRequest,
): Promise<BatchOperation> => {
  return request.post("/api/v1/batch-operations/upgrade", requestParams);
};

/**
 * 获取批量操作详情
 */
export const getBatchOperation = async (
  id: string,
): Promise<BatchOperation> => {
  return request.get(`/api/v1/batch-operations/${id}`);
};

/**
 * 获取批量操作项列表
 */
export const getBatchOperationItems = async (
  id: string,
): Promise<BatchOperationItem[]> => {
  return request.get(`/api/v1/batch-operations/${id}/items`);
};

/**
 * 获取批量操作列表
 */
export const listBatchOperations = async (
  params?: BatchOperationQueryParams,
): Promise<BatchOperationListResponse> => {
  return request.get("/api/v1/batch-operations", { params });
};
