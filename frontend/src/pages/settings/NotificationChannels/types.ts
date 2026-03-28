// Notification Channel Types

export type ChannelType = "EMAIL" | "WEBHOOK" | "DINGTALK" | "WECHAT_WORK";

export type TemplateType =
  | "ALERT"
  | "TASK_COMPLETE"
  | "DEPLOY_SUCCESS"
  | "DEPLOY_FAILED"
  | "SYSTEM_NOTICE";

export type NotificationStatus = "PENDING" | "SENT" | "FAILED";

export interface NotificationChannel {
  id: string;
  name: string;
  type: ChannelType;
  config?: string;
  enabled: boolean;
  createdBy?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface NotificationChannelCreate {
  name: string;
  type: ChannelType;
  config?: string;
  enabled?: boolean;
}

export interface NotificationChannelUpdate {
  name?: string;
  type?: ChannelType;
  config?: string;
  enabled?: boolean;
}

export interface ChannelTestRequest {
  recipient: string;
  title?: string;
  content?: string;
}

export interface ChannelTestResult {
  success: boolean;
  message: string;
}

// Notification Template Types

export interface NotificationTemplate {
  id: string;
  name: string;
  type: TemplateType;
  channelType: ChannelType;
  content: string;
  variables?: string;
  createdBy?: string;
  createdAt?: string;
}

export interface NotificationTemplateCreate {
  name: string;
  type: TemplateType;
  channelType: ChannelType;
  content: string;
  variables?: string;
}

export interface NotificationTemplateUpdate {
  name?: string;
  type?: TemplateType;
  channelType?: ChannelType;
  content?: string;
  variables?: string;
}

// Notification History Types

export interface NotificationHistory {
  id: string;
  channelId: string;
  templateId?: string;
  recipient: string;
  title: string;
  content: string;
  status: NotificationStatus;
  sentAt?: string;
  errorMessage?: string;
  retryCount: number;
  createdAt?: string;
}

export interface NotificationHistoryQuery {
  channelId?: string;
  status?: NotificationStatus;
  startTime?: string;
  endTime?: string;
  limit?: number;
  offset?: number;
}

// Channel Type Labels
export const ChannelTypeLabels: Record<ChannelType, string> = {
  EMAIL: "邮件",
  WEBHOOK: "WebHook",
  DINGTALK: "钉钉",
  WECHAT_WORK: "企业微信",
};

// Template Type Labels
export const TemplateTypeLabels: Record<TemplateType, string> = {
  ALERT: "告警通知",
  TASK_COMPLETE: "任务完成",
  DEPLOY_SUCCESS: "部署成功",
  DEPLOY_FAILED: "部署失败",
  SYSTEM_NOTICE: "系统通知",
};

// Notification Status Labels
export const NotificationStatusLabels: Record<NotificationStatus, string> = {
  PENDING: "待发送",
  SENT: "已发送",
  FAILED: "发送失败",
};
