/**
 * 备份任务状态
 */
export type BackupTaskStatus = "ENABLED" | "DISABLED" | "RUNNING" | "ERROR";

/**
 * 备份类型
 */
export type BackupType = "FULL" | "INCREMENTAL" | "DIFFERENTIAL";

/**
 * 备份内容
 */
export type BackupContent = "DATABASE" | "CONFIG" | "LOGS" | "ALL";

/**
 * 存储类型
 */
export type StorageType = "LOCAL" | "S3" | "NFS" | "FTP";

/**
 * 备份任务
 */
export interface BackupTask {
  id: number;
  name: string;
  description?: string;
  backupType: BackupType;
  backupContent: BackupContent;
  cronExpression: string;
  storageType: StorageType;
  storagePath: string;
  retentionDays: number;
  maxBackups: number;
  status: BackupTaskStatus;
  lastRunTime?: string;
  nextRunTime?: string;
  lastRunStatus?: "SUCCESS" | "FAILED" | "RUNNING";
  createdAt: string;
  updatedAt: string;
}

/**
 * 备份任务创建/更新请求
 */
export interface BackupTaskRequest {
  name: string;
  description?: string;
  backupType: BackupType;
  backupContent: BackupContent;
  cronExpression: string;
  storageType: StorageType;
  storagePath: string;
  retentionDays: number;
  maxBackups: number;
  status?: BackupTaskStatus;
}

/**
 * 备份记录状态
 */
export type BackupRecordStatus =
  | "RUNNING"
  | "SUCCESS"
  | "FAILED"
  | "VALIDATING"
  | "INVALID";

/**
 * 备份记录
 */
export interface BackupRecord {
  id: number;
  taskId: number;
  taskName: string;
  backupName: string;
  backupType: BackupType;
  backupContent: BackupContent;
  storageType: StorageType;
  storagePath: string;
  fileSize: number;
  fileSizeFormatted: string;
  duration: number;
  status: BackupRecordStatus;
  checksum?: string;
  errorMessage?: string;
  operator: string;
  startTime: string;
  endTime?: string;
  createdAt: string;
}

/**
 * 恢复任务状态
 */
export type RestoreTaskStatus = "PENDING" | "RUNNING" | "SUCCESS" | "FAILED";

/**
 * 恢复范围
 */
export type RestoreScope = "DATABASE" | "CONFIG" | "LOGS" | "ALL";

/**
 * 恢复任务
 */
export interface RestoreTask {
  id: number;
  backupRecordId: number;
  backupName: string;
  restoreScope: RestoreScope;
  status: RestoreTaskStatus;
  progress: number;
  errorMessage?: string;
  operator: string;
  startTime: string;
  endTime?: string;
  createdAt: string;
}

/**
 * 恢复任务请求
 */
export interface RestoreTaskRequest {
  backupRecordId: number;
  restoreScope: RestoreScope;
}

/**
 * 存储配置
 */
export interface StorageConfig {
  id: number;
  name: string;
  storageType: StorageType;
  config: Record<string, string>;
  isDefault: boolean;
  status: "ACTIVE" | "INACTIVE";
  createdAt: string;
  updatedAt: string;
}

/**
 * 存储配置请求
 */
export interface StorageConfigRequest {
  name: string;
  storageType: StorageType;
  config: Record<string, string>;
  isDefault?: boolean;
}

/**
 * 备份告警配置
 */
export interface BackupAlertConfig {
  id: number;
  notifyOnSuccess: boolean;
  notifyOnFailure: boolean;
  notifyEmails: string[];
  notifyWebhooks: string[];
  createdAt: string;
  updatedAt: string;
}

/**
 * 备份统计
 */
export interface BackupStatistics {
  totalTasks: number;
  enabledTasks: number;
  totalRecords: number;
  totalSize: number;
  totalSizeFormatted: string;
  successRate: number;
  lastBackupTime?: string;
}
