import request from "../utils/request";
import type {
  BackupTask,
  BackupTaskRequest,
  BackupRecord,
  RestoreTask,
  RestoreTaskRequest,
  StorageConfig,
  StorageConfigRequest,
  BackupAlertConfig,
  BackupStatistics,
} from "../types/backup";

// ========== 备份任务 API ==========

/**
 * 获取备份任务列表
 */
export const getBackupTasks = (): Promise<BackupTask[]> => {
  return request.get("/api/backup/tasks");
};

/**
 * 获取备份任务详情
 */
export const getBackupTask = (id: number): Promise<BackupTask> => {
  return request.get(`/api/backup/tasks/${id}`);
};

/**
 * 创建备份任务
 */
export const createBackupTask = (
  data: BackupTaskRequest,
): Promise<BackupTask> => {
  return request.post("/api/backup/tasks", data);
};

/**
 * 更新备份任务
 */
export const updateBackupTask = (
  id: number,
  data: BackupTaskRequest,
): Promise<BackupTask> => {
  return request.put(`/api/backup/tasks/${id}`, data);
};

/**
 * 删除备份任务
 */
export const deleteBackupTask = (id: number): Promise<void> => {
  return request.delete(`/api/backup/tasks/${id}`);
};

/**
 * 启用备份任务
 */
export const enableBackupTask = (id: number): Promise<void> => {
  return request.post(`/api/backup/tasks/${id}/enable`);
};

/**
 * 禁用备份任务
 */
export const disableBackupTask = (id: number): Promise<void> => {
  return request.post(`/api/backup/tasks/${id}/disable`);
};

/**
 * 手动触发备份任务
 */
export const triggerBackupTask = (id: number): Promise<BackupRecord> => {
  return request.post(`/api/backup/tasks/${id}/trigger`);
};

// ========== 备份记录 API ==========

/**
 * 获取备份记录列表
 */
export const getBackupRecords = (taskId?: number): Promise<BackupRecord[]> => {
  const params = taskId ? { taskId } : {};
  return request.get("/api/backup/records", { params });
};

/**
 * 获取备份记录详情
 */
export const getBackupRecord = (id: number): Promise<BackupRecord> => {
  return request.get(`/api/backup/records/${id}`);
};

/**
 * 删除备份记录
 */
export const deleteBackupRecord = (id: number): Promise<void> => {
  return request.delete(`/api/backup/records/${id}`);
};

/**
 * 下载备份文件
 */
export const downloadBackupRecord = (id: number): string => {
  return `/api/backup/records/${id}/download`;
};

/**
 * 校验备份文件
 */
export const validateBackupRecord = (
  id: number,
): Promise<{ valid: boolean; message: string }> => {
  return request.post(`/api/backup/records/${id}/validate`);
};

// ========== 恢复操作 API ==========

/**
 * 创建恢复任务
 */
export const createRestoreTask = (
  data: RestoreTaskRequest,
): Promise<RestoreTask> => {
  return request.post("/api/backup/restore", data);
};

/**
 * 获取恢复任务列表
 */
export const getRestoreTasks = (): Promise<RestoreTask[]> => {
  return request.get("/api/backup/restore");
};

/**
 * 获取恢复任务详情
 */
export const getRestoreTask = (id: number): Promise<RestoreTask> => {
  return request.get(`/api/backup/restore/${id}`);
};

/**
 * 取消恢复任务
 */
export const cancelRestoreTask = (id: number): Promise<void> => {
  return request.post(`/api/backup/restore/${id}/cancel`);
};

// ========== 存储配置 API ==========

/**
 * 获取存储配置列表
 */
export const getStorageConfigs = (): Promise<StorageConfig[]> => {
  return request.get("/api/backup/storage-configs");
};

/**
 * 创建存储配置
 */
export const createStorageConfig = (
  data: StorageConfigRequest,
): Promise<StorageConfig> => {
  return request.post("/api/backup/storage-configs", data);
};

/**
 * 更新存储配置
 */
export const updateStorageConfig = (
  id: number,
  data: StorageConfigRequest,
): Promise<StorageConfig> => {
  return request.put(`/api/backup/storage-configs/${id}`, data);
};

/**
 * 删除存储配置
 */
export const deleteStorageConfig = (id: number): Promise<void> => {
  return request.delete(`/api/backup/storage-configs/${id}`);
};

/**
 * 测试存储配置连接
 */
export const testStorageConfig = (
  id: number,
): Promise<{ success: boolean; message: string }> => {
  return request.post(`/api/backup/storage-configs/${id}/test`);
};

// ========== 告警配置 API ==========

/**
 * 获取告警配置
 */
export const getAlertConfig = (): Promise<BackupAlertConfig> => {
  return request.get("/api/backup/alert-config");
};

/**
 * 更新告警配置
 */
export const updateAlertConfig = (
  data: Partial<BackupAlertConfig>,
): Promise<BackupAlertConfig> => {
  return request.put("/api/backup/alert-config", data);
};

// ========== 统计 API ==========

/**
 * 获取备份统计
 */
export const getBackupStatistics = (): Promise<BackupStatistics> => {
  return request.get("/api/backup/statistics");
};
