import request from "../utils/request";
import type {
  UserProfile,
  UpdateProfileRequest,
  ChangePasswordRequest,
  ApiToken,
  CreateApiTokenRequest,
  CreateApiTokenResponse,
  LoginHistory,
  UserPreferences,
  TokenAccessLog,
} from "../types/userProfile";

/**
 * 用户个人中心 API
 */

// ==================== 个人资料 ====================

/** 获取当前用户资料 */
export async function getMyProfile(): Promise<UserProfile> {
  return request.get("/api/user/profile");
}

/** 更新个人资料 */
export async function updateMyProfile(
  data: UpdateProfileRequest,
): Promise<UserProfile> {
  return request.put("/api/user/profile", data);
}

/** 上传头像 */
export async function uploadAvatar(file: File): Promise<{ url: string }> {
  const formData = new FormData();
  formData.append("file", file);
  return request.post("/api/user/profile/avatar", formData, {
    headers: { "Content-Type": "multipart/form-data" },
  });
}

// ==================== 安全设置 ====================

/** 修改密码 */
export async function changePassword(
  data: ChangePasswordRequest,
): Promise<void> {
  return request.put("/api/user/profile/password", data);
}

/** 获取登录历史 */
export async function getLoginHistory(params?: {
  page?: number;
  pageSize?: number;
}): Promise<{ list: LoginHistory[]; total: number }> {
  return request.get("/api/user/profile/login-history", { params });
}

// ==================== API令牌管理 ====================

/** 获取我的API令牌列表 */
export async function getMyApiTokens(): Promise<ApiToken[]> {
  return request.get("/api/user/tokens");
}

/** 创建API令牌 */
export async function createMyApiToken(
  data: CreateApiTokenRequest,
): Promise<CreateApiTokenResponse> {
  return request.post("/api/user/tokens", data);
}

/** 撤销API令牌 */
export async function revokeMyApiToken(id: string): Promise<void> {
  return request.put(`/api/user/tokens/${id}/revoke`);
}

/** 删除API令牌 */
export async function deleteMyApiToken(id: string): Promise<void> {
  return request.delete(`/api/user/tokens/${id}`);
}

/** 获取令牌访问日志 */
export async function getTokenAccessLogs(
  tokenId: string,
  params?: { page?: number; pageSize?: number },
): Promise<{ list: TokenAccessLog[]; total: number }> {
  return request.get(`/api/user/tokens/${tokenId}/logs`, { params });
}

// ==================== 偏好设置 ====================

/** 获取偏好设置 */
export async function getMyPreferences(): Promise<UserPreferences> {
  return request.get("/api/user/preferences");
}

/** 更新偏好设置 */
export async function updateMyPreferences(
  data: Partial<UserPreferences>,
): Promise<UserPreferences> {
  return request.put("/api/user/preferences", data);
}
