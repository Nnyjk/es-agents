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
} from "../types/batch";

/**
 * 批量执行命令
 */
export const batchExecuteCommand = async (
  requestParams: BatchCommandRequest,
): Promise<BatchOperation> => {
  return request.post("/batch/command", requestParams);
};

/**
 * 批量部署
 */
export const batchDeploy = async (
  requestParams: BatchDeployRequest,
): Promise<BatchOperation> => {
  return request.post("/batch/deploy", requestParams);
};

/**
 * 批量升级
 */
export const batchUpgrade = async (
  requestParams: BatchUpgradeRequest,
): Promise<BatchOperation> => {
  return request.post("/batch/upgrade", requestParams);
};

/**
 * 获取批量操作详情
 */
export const getBatchOperation = async (id: string): Promise<BatchOperation> => {
  return request.get(`/batch/${id}`);
};

/**
 * 获取批量操作项列表
 */
export const getBatchOperationItems = async (
  id: string,
): Promise<BatchOperationItem[]> => {
  return request.get(`/batch/${id}/items`);
};

/**
 * 获取批量操作列表
 */
export const listBatchOperations = async (
  page: number,
  size: number,
): Promise<BatchOperationListResponse> => {
  return request.get("/batch", { params: { page, size } });
};