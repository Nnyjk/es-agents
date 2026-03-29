/**
 * 站内消息通知类型定义
 */

export type MessageType = 'SYSTEM' | 'ALERT' | 'OPERATION';

export type MessageLevel = 'INFO' | 'WARNING' | 'ERROR';

/**
 * 消息详情
 */
export interface NotificationMessage {
  id: string;
  userId: string;
  username: string;
  title: string;
  content: string;
  type: MessageType;
  level: MessageLevel;
  isRead: boolean;
  relatedType?: string;
  relatedId?: string;
  jumpUrl?: string;
  readAt?: string;
  createdAt: string;
}

/**
 * 消息列表项
 */
export interface NotificationListItem {
  id: string;
  title: string;
  type: MessageType;
  level: MessageLevel;
  isRead: boolean;
  relatedType?: string;
  createdAt: string;
}

/**
 * 消息查询参数
 */
export interface NotificationQueryParams {
  userId?: string;
  type?: MessageType;
  level?: MessageLevel;
  isRead?: boolean;
  relatedType?: string;
  relatedId?: string;
  startTime?: string;
  endTime?: string;
  keyword?: string;
  limit?: number;
  offset?: number;
}

/**
 * 未读数量统计
 */
export interface UnreadCount {
  total: number;
  systemCount: number;
  alertCount: number;
  operationCount: number;
}

/**
 * 消息统计
 */
export interface NotificationStatistics {
  totalCount: number;
  readCount: number;
  unreadCount: number;
  todayCount: number;
}

/**
 * 创建消息请求
 */
export interface NotificationCreateRequest {
  userId: string;
  username: string;
  title: string;
  content: string;
  type: MessageType;
  level?: MessageLevel;
  relatedType?: string;
  relatedId?: string;
  jumpUrl?: string;
}
