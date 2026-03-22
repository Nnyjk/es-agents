/**
 * 系统全局设置类型定义
 */

/**
 * 系统基础信息设置
 */
export interface SystemBasicSettings {
  systemName: string;
  systemLogo?: string;
  favicon?: string;
  copyright?: string;
  icpNumber?: string;
  loginPageBackground?: string;
  welcomeMessage?: string;
  showRegisterButton: boolean;
}

/**
 * 密码策略配置
 */
export interface PasswordPolicy {
  minLength: number;
  requireUppercase: boolean;
  requireLowercase: boolean;
  requireNumber: boolean;
  requireSpecialChar: boolean;
  expirationDays: number;
  maxLoginAttempts: number;
  lockoutDuration: number;
}

/**
 * 会话配置
 */
export interface SessionConfig {
  sessionTimeout: number;
  idleTimeout: number;
  maxConcurrentSessions: number;
}

/**
 * IP 访问规则
 */
export interface IpAccessRule {
  id: number;
  type: "ALLOW" | "DENY";
  ipRange: string;
  description?: string;
  createdAt: string;
  updatedAt: string;
}

/**
 * IP 访问规则请求
 */
export interface IpAccessRuleRequest {
  type: "ALLOW" | "DENY";
  ipRange: string;
  description?: string;
}

/**
 * 安全设置
 */
export interface SecuritySettings {
  passwordPolicy: PasswordPolicy;
  sessionConfig: SessionConfig;
  twoFactorEnabled: boolean;
  ipWhitelistEnabled: boolean;
  ipBlacklistEnabled: boolean;
}

/**
 * 系统维护设置
 */
export interface MaintenanceSettings {
  maintenanceMode: boolean;
  maintenanceMessage: string;
  maintenanceStartTime?: string;
  maintenanceEndTime?: string;
  allowedAdminsOnly: boolean;
}

/**
 * 邮件服务配置
 */
export interface EmailConfig {
  enabled: boolean;
  smtpHost: string;
  smtpPort: number;
  smtpUsername: string;
  smtpPassword?: string;
  useTls: boolean;
  senderEmail: string;
  senderName: string;
}

/**
 * 存储服务配置
 */
export interface StorageGlobalConfig {
  defaultStorageType: "LOCAL" | "S3" | "MINIO";
  localStoragePath?: string;
  s3Endpoint?: string;
  s3Region?: string;
  s3Bucket?: string;
  s3AccessKey?: string;
  s3SecretKey?: string;
  maxFileSize: number;
  allowedFileTypes: string[];
}

/**
 * 功能模块开关
 */
export interface FeatureFlags {
  agentManagement: boolean;
  deploymentCenter: boolean;
  monitoringDashboard: boolean;
  alertCenter: boolean;
  backupRestore: boolean;
  scheduledTasks: boolean;
  apiTokens: boolean;
  multiTenancy: boolean;
}

/**
 * 日志配置
 */
export interface LogConfig {
  logRetentionDays: number;
  logLevel: "DEBUG" | "INFO" | "WARN" | "ERROR";
  maxLogSize: number;
  enableAuditLog: boolean;
  auditLogRetentionDays: number;
}

/**
 * 系统设置更新请求
 */
export interface SystemSettingsRequest {
  basic?: Partial<SystemBasicSettings>;
  security?: Partial<SecuritySettings>;
  maintenance?: Partial<MaintenanceSettings>;
  email?: Partial<EmailConfig>;
  storage?: Partial<StorageGlobalConfig>;
  features?: Partial<FeatureFlags>;
  log?: Partial<LogConfig>;
}

/**
 * 系统设置响应
 */
export interface SystemSettingsResponse {
  basic: SystemBasicSettings;
  security: SecuritySettings;
  maintenance: MaintenanceSettings;
  email: EmailConfig;
  storage: StorageGlobalConfig;
  features: FeatureFlags;
  log: LogConfig;
}

/**
 * 系统操作日志
 */
export interface SystemOperationLog {
  id: number;
  operation: string;
  module: string;
  operator: string;
  ipAddress: string;
  details?: string;
  createdAt: string;
}

/**
 * 系统状态
 */
export interface SystemStatus {
  version: string;
  uptime: number;
  cpuUsage: number;
  memoryUsage: number;
  diskUsage: number;
  databaseStatus: "HEALTHY" | "UNHEALTHY";
  cacheStatus: "HEALTHY" | "UNHEALTHY";
  lastBackupTime?: string;
}

/**
 * 缓存清理结果
 */
export interface CacheClearResult {
  clearedCaches: string[];
  freedMemory: number;
  freedMemoryFormatted: string;
}
