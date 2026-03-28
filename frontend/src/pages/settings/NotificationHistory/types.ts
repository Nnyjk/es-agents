import type {
  NotificationStatus,
  NotificationHistory,
  NotificationHistoryQuery,
} from "../NotificationChannels/types";

// Re-export types for convenience
export type {
  NotificationStatus,
  NotificationHistory,
  NotificationHistoryQuery,
};

// Notification Status Labels
export const NotificationStatusLabels: Record<NotificationStatus, string> = {
  PENDING: "待发送",
  SENT: "已发送",
  FAILED: "发送失败",
};
