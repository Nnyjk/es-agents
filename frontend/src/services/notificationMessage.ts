import request from "../utils/request";
import type {
  NotificationMessage,
  NotificationListItem,
  NotificationQueryParams,
  UnreadCount,
  NotificationStatistics,
  NotificationCreateRequest,
} from "../types/notification";

/**
 * 查询参数（API 格式）
 */
interface ApiQueryParams extends NotificationQueryParams {
  type?: string;
  level?: string;
}

/**
 * 获取消息列表
 */
export const getMessages = async (
  params: NotificationQueryParams,
): Promise<NotificationListItem[]> => {
  const apiParams: ApiQueryParams = { ...params };
  if (params.type) apiParams.type = params.type;
  if (params.level) apiParams.level = params.level;

  return request.get("/api/v1/notification/messages", { params: apiParams });
};

/**
 * 获取消息详情
 */
export const getMessageDetail = async (
  id: string,
): Promise<NotificationMessage> => {
  return request.get(`/api/v1/notification/messages/${id}`);
};

/**
 * 标记消息为已读
 */
export const markAsRead = async (id: string): Promise<{ success: boolean }> => {
  return request.put(`/api/v1/notification/messages/${id}/read`);
};

/**
 * 批量标记消息为已读
 */
export const markBatchAsRead = async (
  ids: string[],
): Promise<{ count: number }> => {
  return request.put("/api/v1/notification/messages/read-batch", ids);
};

/**
 * 删除消息
 */
export const deleteMessage = async (
  id: string,
): Promise<{ success: boolean }> => {
  return request.delete(`/api/v1/notification/messages/${id}`);
};

/**
 * 批量删除消息
 */
export const deleteBatchMessages = async (
  ids: string[],
): Promise<{ count: number }> => {
  return request.delete("/api/v1/notification/messages/batch", { data: ids });
};

/**
 * 获取未读数量
 */
export const getUnreadCount = async (userId: string): Promise<UnreadCount> => {
  return request.get("/api/v1/notification/messages/unread-count", {
    params: { userId },
  });
};

/**
 * 获取消息统计
 */
export const getStatistics = async (
  userId: string,
): Promise<NotificationStatistics> => {
  return request.get("/api/v1/notification/messages/statistics", {
    params: { userId },
  });
};

/**
 * 创建消息
 */
export const createMessage = async (
  data: NotificationCreateRequest,
): Promise<NotificationMessage> => {
  return request.post("/api/v1/notification/messages", data);
};
