import request from "../utils/request";
import type {
  MetricSummary,
  TimeSeriesData,
  GrafanaDashboard,
  PromQLQuery,
  TimeRange,
  TimeRangePreset,
  MetricType,
  MetricQueryParams,
  CreateDashboardParams,
  MonitoringAlert,
} from "../types/monitoring";

/**
 * Prometheus 监控服务 API
 */
export const monitoringService = {
  /**
   * 获取监控概览指标摘要
   */
  getMetricSummary: async (): Promise<MetricSummary> => {
    const response = await request.get<MetricSummary>("/monitoring/summary");
    return response.data;
  },

  /**
   * 获取时序数据
   */
  getTimeseriesData: async (
    metric: MetricType,
    timeRange?: TimeRangePreset | TimeRange,
  ): Promise<TimeSeriesData[]> => {
    const response = await request.get<TimeSeriesData[]>(
      "/monitoring/timeseries",
      {
        params: {
          metric,
          timeRange: typeof timeRange === "string" ? timeRange : undefined,
          start: typeof timeRange !== "string" ? timeRange?.start : undefined,
          end: typeof timeRange !== "string" ? timeRange?.end : undefined,
        },
      },
    );
    return response.data;
  },

  /**
   * 执行 PromQL 查询
   */
  executePromQL: async (
    query: string,
    timeRange?: TimeRange,
  ): Promise<PromQLQuery> => {
    const response = await request.post<PromQLQuery>("/monitoring/promql", {
      query,
      timeRange,
    });
    return response.data;
  },

  /**
   * 获取 Grafana 大盘列表
   */
  getGrafanaDashboards: async (): Promise<GrafanaDashboard[]> => {
    const response = await request.get<GrafanaDashboard[]>(
      "/monitoring/dashboards",
    );
    return response.data;
  },

  /**
   * 创建 Grafana 大盘
   */
  createGrafanaDashboard: async (
    params: CreateDashboardParams,
  ): Promise<GrafanaDashboard> => {
    const response = await request.post<GrafanaDashboard>(
      "/monitoring/dashboards",
      params,
    );
    return response.data;
  },

  /**
   * 删除 Grafana 大盘
   */
  deleteGrafanaDashboard: async (id: string): Promise<void> => {
    await request.delete(`/monitoring/dashboards/${id}`);
  },

  /**
   * 获取监控告警列表
   */
  getAlerts: async (): Promise<MonitoringAlert[]> => {
    const response = await request.get<MonitoringAlert[]>("/monitoring/alerts");
    return response.data;
  },

  /**
   * 获取指标趋势数据
   */
  getMetricTrend: async (
    params: MetricQueryParams,
  ): Promise<TimeSeriesData[]> => {
    const response = await request.get<TimeSeriesData[]>(
      "/monitoring/metrics/trend",
      { params },
    );
    return response.data;
  },
};

export default monitoringService;
