import React, { useState } from "react";
import { Row, Col, Space, Typography } from "antd";
import { DashboardOutlined, ReloadOutlined } from "@ant-design/icons";
import { useQuery } from "@tanstack/react-query";
import { MetricCard } from "../../components/monitoring/MetricCard";
import { MetricChart } from "../../components/monitoring/MetricChart";
import { monitoringService } from "../../services/monitoring";
import type { TimeRangePreset, MetricType } from "../../types/monitoring";
import styles from "./MonitoringPage.module.css";

const { Title } = Typography;

/**
 * 监控主页面
 * 显示核心指标卡片和资源使用率趋势图
 */
const MonitoringPage: React.FC = () => {
  const [cpuTimeRange, setCpuTimeRange] = useState<TimeRangePreset>("1h");
  const [memoryTimeRange, setMemoryTimeRange] = useState<TimeRangePreset>("1h");
  const [diskTimeRange, setDiskTimeRange] = useState<TimeRangePreset>("1h");

  // 获取指标摘要数据
  const {
    data: summary,
    isLoading: summaryLoading,
    refetch: refetchSummary,
  } = useQuery({
    queryKey: ["monitoring", "summary"],
    queryFn: () => monitoringService.getMetricSummary(),
    refetchInterval: 30000, // 30秒自动刷新
  });

  // 获取 CPU 使用率趋势
  const { data: cpuData, isLoading: cpuLoading } = useQuery({
    queryKey: ["monitoring", "timeseries", "cpu_usage", cpuTimeRange],
    queryFn: () =>
      monitoringService.getTimeseriesData(
        "cpu_usage" as MetricType,
        cpuTimeRange,
      ),
  });

  // 获取内存使用率趋势
  const { data: memoryData, isLoading: memoryLoading } = useQuery({
    queryKey: ["monitoring", "timeseries", "memory_usage", memoryTimeRange],
    queryFn: () =>
      monitoringService.getTimeseriesData(
        "memory_usage" as MetricType,
        memoryTimeRange,
      ),
  });

  // 获取磁盘使用率趋势
  const { data: diskData, isLoading: diskLoading } = useQuery({
    queryKey: ["monitoring", "timeseries", "disk_usage", diskTimeRange],
    queryFn: () =>
      monitoringService.getTimeseriesData(
        "disk_usage" as MetricType,
        diskTimeRange,
      ),
  });

  const handleRefresh = () => {
    refetchSummary();
  };

  return (
    <div className={styles.container}>
      <Space direction="vertical" size="large" style={{ width: "100%" }}>
        <div className={styles.header}>
          <Title level={4}>
            <DashboardOutlined /> 监控中心
          </Title>
          <ReloadOutlined
            className={styles.refreshIcon}
            onClick={handleRefresh}
          />
        </div>

        {/* 核心指标卡片 */}
        <Row gutter={16}>
          <Col span={6}>
            <MetricCard
              title="主机数"
              value={summary?.agentCount ?? 0}
              unit="台"
              loading={summaryLoading}
              precision={0}
            />
          </Col>
          <Col span={6}>
            <MetricCard
              title="在线 Agent"
              value={summary?.agentCount ?? 0}
              unit="个"
              loading={summaryLoading}
              precision={0}
            />
          </Col>
          <Col span={6}>
            <MetricCard
              title="运行任务"
              value={summary?.taskCount ?? 0}
              unit="个"
              loading={summaryLoading}
              precision={0}
            />
          </Col>
          <Col span={6}>
            <MetricCard
              title="今日告警"
              value={summary?.alertCount ?? 0}
              unit="条"
              loading={summaryLoading}
              precision={0}
            />
          </Col>
        </Row>

        {/* 资源使用率趋势图 */}
        <Row gutter={16}>
          <Col span={8}>
            <MetricChart
              title="CPU 使用率"
              data={cpuData ?? []}
              loading={cpuLoading}
              unit="%"
              color="#1890ff"
              showTimeRangeSelector
              timeRange={cpuTimeRange}
              onTimeRangeChange={setCpuTimeRange}
              height={250}
            />
          </Col>
          <Col span={8}>
            <MetricChart
              title="内存使用率"
              data={memoryData ?? []}
              loading={memoryLoading}
              unit="%"
              color="#52c41a"
              showTimeRangeSelector
              timeRange={memoryTimeRange}
              onTimeRangeChange={setMemoryTimeRange}
              height={250}
            />
          </Col>
          <Col span={8}>
            <MetricChart
              title="磁盘使用率"
              data={diskData ?? []}
              loading={diskLoading}
              unit="%"
              color="#722ed1"
              showTimeRangeSelector
              timeRange={diskTimeRange}
              onTimeRangeChange={setDiskTimeRange}
              height={250}
            />
          </Col>
        </Row>
      </Space>
    </div>
  );
};

export default MonitoringPage;
