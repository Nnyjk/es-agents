import request from "../utils/request";
import type {
  Alert,
  AlertRule,
  AlertChannel,
  AlertLevel,
  AlertStatus,
  AlertRuleStatus,
  AlertChannelType,
  AlertStatistics,
  AlertRuleTestResult,
  AlertChannelTestResult,
  PageParams,
  ListResponse,
} from "../types";

// ========== 告警列表 ==========

export interface AlertQueryParams extends PageParams {
  level?: AlertLevel;
  status?: AlertStatus;
  source?: string;
  ruleId?: string;
  startTime?: string;
  endTime?: string;
  keyword?: string;
}

export interface AlertListResponse extends ListResponse<Alert> {
  statistics?: AlertStatistics;
}

export const getAlerts = async (
  params: AlertQueryParams,
): Promise<AlertListResponse> => {
  const response = await request.get<AlertListResponse>("/alerts", { params });
  return response.data;
};

export const getAlert = async (id: string): Promise<Alert> => {
  const response = await request.get<Alert>(`/alerts/${id}`);
  return response.data;
};

export const acknowledgeAlert = async (id: string): Promise<Alert> => {
  const response = await request.post<Alert>(`/alerts/${id}/acknowledge`);
  return response.data;
};

export const resolveAlert = async (id: string): Promise<Alert> => {
  const response = await request.post<Alert>(`/alerts/${id}/resolve`);
  return response.data;
};

export const ignoreAlert = async (id: string): Promise<Alert> => {
  const response = await request.post<Alert>(`/alerts/${id}/ignore`);
  return response.data;
};

export interface BatchAlertActionParams {
  ids: string[];
  action: "acknowledge" | "resolve" | "ignore";
}

export const batchAlertAction = async (
  params: BatchAlertActionParams,
): Promise<{ success: boolean; affectedCount: number }> => {
  const response = await request.post<{
    success: boolean;
    affectedCount: number;
  }>("/alerts/batch", params);
  return response.data;
};

export const getAlertStatistics = async (): Promise<AlertStatistics> => {
  const response = await request.get<AlertStatistics>("/alerts/statistics");
  return response.data;
};

// ========== 告警规则 ==========

export interface AlertRuleQueryParams extends PageParams {
  level?: AlertLevel;
  status?: AlertRuleStatus;
  source?: string;
  keyword?: string;
}

export interface AlertRuleCreateParams {
  name: string;
  description?: string;
  level: AlertLevel;
  source: string;
  condition: string;
  duration: number;
  channelIds?: string[];
  status?: AlertRuleStatus;
  labels?: Record<string, string>;
  silencePeriod?: number;
}

export interface AlertRuleUpdateParams {
  name?: string;
  description?: string;
  level?: AlertLevel;
  source?: string;
  condition?: string;
  duration?: number;
  channelIds?: string[];
  status?: AlertRuleStatus;
  labels?: Record<string, string>;
  silencePeriod?: number;
}

export const getAlertRules = async (
  params: AlertRuleQueryParams,
): Promise<ListResponse<AlertRule>> => {
  const response = await request.get<ListResponse<AlertRule>>("/alert-rules", {
    params,
  });
  return response.data;
};

export const getAlertRule = async (id: string): Promise<AlertRule> => {
  const response = await request.get<AlertRule>(`/alert-rules/${id}`);
  return response.data;
};

export const createAlertRule = async (
  params: AlertRuleCreateParams,
): Promise<AlertRule> => {
  const response = await request.post<AlertRule>("/alert-rules", params);
  return response.data;
};

export const updateAlertRule = async (
  id: string,
  params: AlertRuleUpdateParams,
): Promise<AlertRule> => {
  const response = await request.put<AlertRule>(`/alert-rules/${id}`, params);
  return response.data;
};

export const deleteAlertRule = async (id: string): Promise<void> => {
  await request.delete(`/alert-rules/${id}`);
};

export const enableAlertRule = async (id: string): Promise<AlertRule> => {
  const response = await request.post<AlertRule>(`/alert-rules/${id}/enable`);
  return response.data;
};

export const disableAlertRule = async (id: string): Promise<AlertRule> => {
  const response = await request.post<AlertRule>(`/alert-rules/${id}/disable`);
  return response.data;
};

export const testAlertRule = async (
  id: string,
): Promise<AlertRuleTestResult> => {
  const response = await request.post<AlertRuleTestResult>(
    `/alert-rules/${id}/test`,
  );
  return response.data;
};

// ========== 告警通知渠道 ==========

export interface AlertChannelQueryParams extends PageParams {
  type?: AlertChannelType;
  status?: "ENABLED" | "DISABLED";
  keyword?: string;
}

export interface AlertChannelCreateParams {
  name: string;
  type: AlertChannelType;
  config: string;
  description?: string;
  status?: "ENABLED" | "DISABLED";
}

export interface AlertChannelUpdateParams {
  name?: string;
  type?: AlertChannelType;
  config?: string;
  description?: string;
  status?: "ENABLED" | "DISABLED";
}

export const getAlertChannels = async (
  params: AlertChannelQueryParams,
): Promise<ListResponse<AlertChannel>> => {
  const response = await request.get<ListResponse<AlertChannel>>(
    "/alert-channels",
    {
      params,
    },
  );
  return response.data;
};

export const getAlertChannel = async (id: string): Promise<AlertChannel> => {
  const response = await request.get<AlertChannel>(`/alert-channels/${id}`);
  return response.data;
};

export const createAlertChannel = async (
  params: AlertChannelCreateParams,
): Promise<AlertChannel> => {
  const response = await request.post<AlertChannel>("/alert-channels", params);
  return response.data;
};

export const updateAlertChannel = async (
  id: string,
  params: AlertChannelUpdateParams,
): Promise<AlertChannel> => {
  const response = await request.put<AlertChannel>(
    `/alert-channels/${id}`,
    params,
  );
  return response.data;
};

export const deleteAlertChannel = async (id: string): Promise<void> => {
  await request.delete(`/alert-channels/${id}`);
};

export const enableAlertChannel = async (id: string): Promise<AlertChannel> => {
  const response = await request.post<AlertChannel>(
    `/alert-channels/${id}/enable`,
  );
  return response.data;
};

export const disableAlertChannel = async (
  id: string,
): Promise<AlertChannel> => {
  const response = await request.post<AlertChannel>(
    `/alert-channels/${id}/disable`,
  );
  return response.data;
};

export const testAlertChannel = async (
  id: string,
): Promise<AlertChannelTestResult> => {
  const response = await request.post<AlertChannelTestResult>(
    `/alert-channels/${id}/test`,
  );
  return response.data;
};
