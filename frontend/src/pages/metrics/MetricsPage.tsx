import React, { useEffect, useState } from "react";
import { Card, Col, Row, Statistic, Progress, Spin, Alert, Table } from "antd";
import {
  CpuOutlined,
  MemoryOutlined,
  DashboardOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  RocketOutlined,
} from "@ant-design/icons";

interface MetricsSummary {
  jvmMemory: {
    heap?: number;
    nonheap?: number;
  };
  jvmThreads: {
    live?: number;
    daemon?: number;
  };
  cpu: {
    usage?: number;
    count?: number;
  };
  agent: {
    count?: number;
  };
  tasks: {
    executionTotal?: number;
    successTotal?: number;
    failureTotal?: number;
  };
}

const MetricsPage: React.FC = () => {
  const [loading, setLoading] = useState<boolean>(true);
  const [metrics, setMetrics] = useState<MetricsSummary | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetchMetrics();
    // 每 30 秒刷新一次
    const interval = setInterval(fetchMetrics, 30000);
    return () => clearInterval(interval);
  }, []);

  const fetchMetrics = async () => {
    try {
      setLoading(true);
      const response = await fetch("/api/v1/metrics/summary", {
        headers: {
          "Content-Type": "application/json",
        },
      });
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      const data = await response.json();
      setMetrics(data);
      setError(null);
    } catch (err: any) {
      setError(`获取指标失败：${err.message}`);
    } finally {
      setLoading(false);
    }
  };

  const formatBytes = (bytes?: number): string => {
    if (bytes === undefined) return "N/A";
    const gb = bytes / (1024 * 1024 * 1024);
    return `${gb.toFixed(2)} GB`;
  };

  const formatPercentage = (value?: number): number => {
    if (value === undefined) return 0;
    return Math.round(value * 100);
  };

  const getTaskSuccessRate = (): number => {
    if (!metrics?.tasks) return 0;
    const total = metrics.tasks.executionTotal || 0;
    const success = metrics.tasks.successTotal || 0;
    if (total === 0) return 100;
    return Math.round((success / total) * 100);
  };

  if (loading && !metrics) {
    return (
      <div style={{ textAlign: "center", padding: "100px" }}>
        <Spin size="large" tip="加载指标数据..." />
      </div>
    );
  }

  if (error) {
    return (
      <Alert
        message="错误"
        description={error}
        type="error"
        showIcon
        style={{ margin: "20px" }}
      />
    );
  }

  return (
    <div style={{ padding: "24px" }}>
      <h1>系统指标监控</h1>

      {/* 资源使用概览 */}
      <Row gutter={[16, 16]} style={{ marginBottom: "24px" }}>
        <Col xs={24} sm={12} md={6}>
          <Card>
            <Statistic
              title="CPU 使用率"
              value={formatPercentage(metrics?.cpu?.usage)}
              suffix="%"
              prefix={<CpuOutlined />}
              valueStyle={{
                color:
                  (metrics?.cpu?.usage || 0) > 0.8
                    ? "#cf1322"
                    : (metrics?.cpu?.usage || 0) > 0.5
                      ? "#faad14"
                      : "#3f8600",
              }}
            />
            <div style={{ marginTop: "8px", color: "#666" }}>
              CPU 核心数：{metrics?.cpu?.count || "N/A"}
            </div>
          </Card>
        </Col>

        <Col xs={24} sm={12} md={6}>
          <Card>
            <Statistic
              title="内存使用"
              value={formatBytes(metrics?.jvmMemory?.heap)}
              prefix={<MemoryOutlined />}
            />
            <div style={{ marginTop: "8px", color: "#666" }}>
              非堆内存：{formatBytes(metrics?.jvmMemory?.nonheap)}
            </div>
          </Card>
        </Col>

        <Col xs={24} sm={12} md={6}>
          <Card>
            <Statistic
              title="活跃线程"
              value={metrics?.jvmThreads?.live || 0}
              prefix={<DashboardOutlined />}
            />
            <div style={{ marginTop: "8px", color: "#666" }}>
              守护线程：{metrics?.jvmThreads?.daemon || 0}
            </div>
          </Card>
        </Col>

        <Col xs={24} sm={12} md={6}>
          <Card>
            <Statistic
              title="Agent 实例"
              value={metrics?.agent?.count || 0}
              prefix={<RocketOutlined />}
              valueStyle={{ color: "#1890ff" }}
            />
          </Card>
        </Col>
      </Row>

      {/* 任务统计 */}
      <Row gutter={[16, 16]} style={{ marginBottom: "24px" }}>
        <Col xs={24} md={8}>
          <Card title="任务执行统计">
            <Statistic
              title="总执行数"
              value={metrics?.tasks?.executionTotal || 0}
              valueStyle={{ color: "#1890ff" }}
            />
          </Card>
        </Col>

        <Col xs={24} md={8}>
          <Card title="成功任务">
            <Statistic
              title="成功数"
              value={metrics?.tasks?.successTotal || 0}
              prefix={<CheckCircleOutlined />}
              valueStyle={{ color: "#52c41a" }}
            />
          </Card>
        </Col>

        <Col xs={24} md={8}>
          <Card title="失败任务">
            <Statistic
              title="失败数"
              value={metrics?.tasks?.failureTotal || 0}
              prefix={<CloseCircleOutlined />}
              valueStyle={{ color: "#cf1322" }}
            />
          </Card>
        </Col>
      </Row>

      {/* 成功率进度条 */}
      <Card title="任务成功率" style={{ marginBottom: "24px" }}>
        <Progress
          percent={getTaskSuccessRate()}
          status={
            getTaskSuccessRate() >= 95
              ? "success"
              : getTaskSuccessRate() >= 80
                ? "normal"
                : "exception"
          }
          format={(percent) => `${percent}% 成功率`}
        />
      </Card>

      {/* 详细指标表 */}
      <Card title="详细指标">
        <Table
          dataSource={[
            {
              name: "CPU 使用率",
              value: `${formatPercentage(metrics?.cpu?.usage)}%`,
            },
            { name: "CPU 核心数", value: metrics?.cpu?.count || "N/A" },
            { name: "堆内存", value: formatBytes(metrics?.jvmMemory?.heap) },
            {
              name: "非堆内存",
              value: formatBytes(metrics?.jvmMemory?.nonheap),
            },
            { name: "活跃线程", value: metrics?.jvmThreads?.live || "N/A" },
            { name: "守护线程", value: metrics?.jvmThreads?.daemon || "N/A" },
            { name: "Agent 实例数", value: metrics?.agent?.count || "N/A" },
            {
              name: "任务执行总数",
              value: metrics?.tasks?.executionTotal || "N/A",
            },
            {
              name: "任务成功数",
              value: metrics?.tasks?.successTotal || "N/A",
            },
            {
              name: "任务失败数",
              value: metrics?.tasks?.failureTotal || "N/A",
            },
            { name: "任务成功率", value: `${getTaskSuccessRate()}%` },
          ]}
          columns={[
            { title: "指标名称", dataIndex: "name", key: "name" },
            { title: "数值", dataIndex: "value", key: "value" },
          ]}
          pagination={false}
          size="small"
        />
      </Card>
    </div>
  );
};

export default MetricsPage;
