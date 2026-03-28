// API Key 类型定义

export interface ApiKey {
  id: string;
  key?: string; // 仅在创建时返回
  name: string;
  description?: string;
  expiresAt?: string;
  enabled: boolean;
  permissions?: string[];
  ipWhitelist?: string[];
  createdBy?: string;
  createdAt?: string;
  updatedAt?: string;
  lastUsedAt?: string;
  revokedAt?: string;
  revokedBy?: string;
  revokeReason?: string;
  expired: boolean;
  valid: boolean;
  revoked: boolean;
}

export interface ApiKeyCreate {
  name: string;
  description?: string;
  expiresAt?: string;
  permissions?: string[];
  ipWhitelist?: string[];
  createdBy?: string;
}

export interface ApiKeyUpdate {
  name?: string;
  description?: string;
  expiresAt?: string;
  enabled?: boolean;
  permissions?: string[];
  ipWhitelist?: string[];
}

export interface ApiKeyQuery {
  keyword?: string;
  createdBy?: string;
  enabled?: boolean;
  expired?: boolean;
  revoked?: boolean;
  limit?: number;
  offset?: number;
}

export interface ApiKeyRevoke {
  revokedBy?: string;
  reason: string;
}

export interface ApiKeyRefresh {
  updatedBy?: string;
}

export interface ApiKeyUsageLog {
  id: string;
  keyId: string;
  usageTime?: string;
  clientIp?: string;
  requestMethod?: string;
  requestPath?: string;
  responseStatus?: number;
  responseTimeMs?: number;
  permissionUsed?: string;
  errorMessage?: string;
  createdAt?: string;
}

export interface ApiKeyUsageLogQuery {
  keyId?: string;
  clientIp?: string;
  requestMethod?: string;
  requestPath?: string;
  responseStatus?: number;
  startTime?: string;
  endTime?: string;
  limit?: number;
  offset?: number;
}

export interface ApiKeyValidationResult {
  valid: boolean;
  keyId?: string;
  keyName?: string;
  permissions?: string[];
  message: string;
}
