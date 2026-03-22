/**
 * 用户个人资料相关类型定义
 */

/** 用户个人资料 */
export interface UserProfile {
  id: string;
  username: string;
  nickname?: string;
  email: string;
  avatar?: string;
  phone?: string;
  roles: UserRole[];
  status: "ACTIVE" | "INACTIVE" | "LOCKED";
  createdAt: string;
  updatedAt: string;
  lastLoginAt?: string;
  lastLoginIp?: string;
}

/** 用户角色 */
export interface UserRole {
  id: string;
  code: string;
  name: string;
}

/** 修改密码请求 */
export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
  confirmPassword: string;
}

/** 个人资料更新请求 */
export interface UpdateProfileRequest {
  nickname?: string;
  avatar?: string;
  phone?: string;
}

/** API令牌 */
export interface ApiToken {
  id: string;
  name: string;
  description?: string;
  scopes: string[];
  expiresAt?: string;
  createdAt: string;
  lastUsedAt?: string;
  status: "ACTIVE" | "REVOKED" | "EXPIRED";
}

/** 创建API令牌请求 */
export interface CreateApiTokenRequest {
  name: string;
  description?: string;
  scopes: string[];
  expiresAt?: string;
}

/** 创建API令牌响应（包含令牌值） */
export interface CreateApiTokenResponse {
  id: string;
  name: string;
  token: string;
  expiresAt?: string;
}

/** 登录历史记录 */
export interface LoginHistory {
  id: string;
  loginAt: string;
  ip: string;
  userAgent: string;
  device?: string;
  location?: string;
  status: "SUCCESS" | "FAILED";
  failReason?: string;
}

/** 用户偏好设置 */
export interface UserPreferences {
  theme: "light" | "dark" | "system";
  language: string;
  notificationEmail: boolean;
  notificationBrowser: boolean;
  notificationSms: boolean;
}

/** 令牌访问日志 */
export interface TokenAccessLog {
  id: string;
  tokenId: string;
  accessedAt: string;
  ip: string;
  endpoint: string;
  method: string;
  statusCode: number;
}