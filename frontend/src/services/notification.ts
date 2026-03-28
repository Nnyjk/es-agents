import request from "../utils/request";
import type {
  NotificationChannel,
  NotificationChannelCreate,
  NotificationChannelUpdate,
  ChannelTestRequest,
  ChannelTestResult,
  NotificationTemplate,
  NotificationTemplateCreate,
  NotificationTemplateUpdate,
  NotificationHistory,
  NotificationHistoryQuery,
} from "../pages/settings/NotificationChannels/types";

// Notification Channel APIs

// Get notification channel list
export const listNotificationChannels = async (): Promise<
  NotificationChannel[]
> => {
  return request.get("/v1/notification-channels");
};

// Get single notification channel
export const getNotificationChannel = async (
  id: string,
): Promise<NotificationChannel> => {
  return request.get(`/v1/notification-channels/${id}`);
};

// Create notification channel
export const createNotificationChannel = async (
  data: NotificationChannelCreate,
): Promise<NotificationChannel> => {
  return request.post("/v1/notification-channels", data);
};

// Update notification channel
export const updateNotificationChannel = async (
  id: string,
  data: NotificationChannelUpdate,
): Promise<NotificationChannel> => {
  return request.put(`/v1/notification-channels/${id}`, data);
};

// Delete notification channel
export const deleteNotificationChannel = async (id: string): Promise<void> => {
  return request.delete(`/v1/notification-channels/${id}`);
};

// Test notification channel
export const testNotificationChannel = async (
  id: string,
  data: ChannelTestRequest,
): Promise<ChannelTestResult> => {
  return request.post(`/v1/notification-channels/${id}/test`, data);
};

// Notification Template APIs

// Get notification template list
export const listNotificationTemplates = async (): Promise<
  NotificationTemplate[]
> => {
  return request.get("/v1/notification-templates");
};

// Get single notification template
export const getNotificationTemplate = async (
  id: string,
): Promise<NotificationTemplate> => {
  return request.get(`/v1/notification-templates/${id}`);
};

// Create notification template
export const createNotificationTemplate = async (
  data: NotificationTemplateCreate,
): Promise<NotificationTemplate> => {
  return request.post("/v1/notification-templates", data);
};

// Update notification template
export const updateNotificationTemplate = async (
  id: string,
  data: NotificationTemplateUpdate,
): Promise<NotificationTemplate> => {
  return request.put(`/v1/notification-templates/${id}`, data);
};

// Delete notification template
export const deleteNotificationTemplate = async (id: string): Promise<void> => {
  return request.delete(`/v1/notification-templates/${id}`);
};

// Notification History APIs

// Get notification history list
export const listNotificationHistory = async (
  params?: NotificationHistoryQuery,
): Promise<NotificationHistory[]> => {
  return request.get("/v1/notification-history", { params });
};

// Get single notification history
export const getNotificationHistory = async (
  id: string,
): Promise<NotificationHistory> => {
  return request.get(`/v1/notification-history/${id}`);
};

// Delete notification history
export const deleteNotificationHistory = async (id: string): Promise<void> => {
  return request.delete(`/v1/notification-history/${id}`);
};