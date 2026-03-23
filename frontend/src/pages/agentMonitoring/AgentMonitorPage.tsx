import React from "react";
import {
  Card,
  Row,
  Col,
  Statistic,
  Table,
  Tag,
  Progress,
  Typography,
  Space,
  Tooltip,
} from "antd";
import {
  DashboardOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  ExclamationCircleOutlined,
  MonitorOutlined,
  CloudServerOutlined,
} from "@ant-design/icons";
import { useQuery } from "@tanstack/react-query";
import type { ColumnsType } from "antd/es/table";
import agentMonitoringService from "../../services/agentMonitoring";
import type {
  AgentMonitorOverview,
  AgentRuntimeStatus,
} from "../../types/agentMonitoring";
import type { HealthStatus } from "../../types/agentMonitoring";
import dayjs from "dayjs";
import relativeTime from "dayjs/plugin/relativeTime";
import "dayjs/locale/zh-cn";

dayjs.extend(relativeTime);
dayjs.locale("zh-cn");

const { Title, Text } = Typography;

/**
 * 获取健康状态颜色
 */
const getHealthStatusColor = (status?: HealthStatus): string => {
  switch (status) {
    case "HEALTHY":
      return "#52c41a";
    case "WARNING":
      return "#faad14";
    case "CRITICAL":
      return "#ff4d4f";
    default:
      return "#d9d9d9";
  }
};

/**
 * 获取健康状态标签
 */
const getHealthStatusTag = (status?: HealthStatus) => {
  const color = getHealthStatusColor(status);
  const text =
    status === "HEALTHY"
      ? "健康"
      : status === "WARNING"
        ? "警告"
        : status === "CRITICAL"
          ? "异常"
          : "未知";
  return <Tag color={color}>{text}</Tag>;
};

/**
 * 获取 Agent 状态标签
 */
const getAgentStatusTag = (status: string) => {
  const statusConfig: Record<string, { color: string; text: string }> = {
    ONLINE: { color: "success", text: "在线" },
    OFFLINE: { color: "default", text: "离线" },
    ERROR: { color: "error", text: "异常" },
    DEPLOYING: { color: "processing", text: "部署中" },
    DEPLOYED: { color: "success", text: "已部署" },
    PREPARING: { color: "processing", text: "准备中" },
    READY: { color: "success", text: "就绪" },
    PACKAGING: { color: "processing", text: "打包中" },
    PACKAGED: { color: "success", text: "已打包" },
    UNCONFIGURED: { color: "default", text: "未配置" },
  };
  const config = statusConfig[status] || { color: "default", text: status };
  return <Tag color={config.color}>{config.text}</Tag>;
};

/**
 * 概览统计卡片
 */
const OverviewCards: React.FC<{
  overview: AgentMonitorOverview | undefined;
  isLoading: boolean;
}> = ({ overview, isLoading }) => {
  return (
    <Row gutter={16}>
      <Col span={4}>
        <Card loading={isLoading}>
          <Statistic
            title="Agent 总数"
            value={overview?.totalAgents ?? 0}
            prefix={<CloudServerOutlined />}
          />
        </Card>
      </Col>
      <Col span={4}>
        <Card loading={isLoading}>
          <Statistic
            title="在线"
            value={overview?.onlineAgents ?? 0}
            valueStyle={{ color: "#3f8600" }}
            prefix={<CheckCircleOutlined />}
          />
        </Card>
      </Col>
      <Col span={4}>
        <Card loading={isLoading}>
          <Statistic
            title="离线"
            value={overview?.offlineAgents ?? 0}
            valueStyle={{ color: "#8c8c8c" }}
            prefix={<CloseCircleOutlined />}
          />
        </Card>
      </Col>
      <Col span={4}>
        <Card loading={isLoading}>
          <Statistic
            title="异常"
            value={overview?.errorAgents ?? 0}
            valueStyle={{ color: "#cf1322" }}
            prefix={<ExclamationCircleOutlined />}
          />
        </Card>
      </Col>
      <Col span={4}>
        <Card loading={isLoading}>
          <Statistic
            title="CPU 使用率"
            value={overview?.resourceUsage?.cpuUsage ?? 0}
            suffix="%"
            valueStyle={{
              color:
                (overview?.resourceUsage?.cpuUsage ?? 0) > 80
                  ? "#cf1322"
                  : "#3f8600",
            }}
          />
          <Progress
            percent={overview?.resourceUsage?.cpuUsage ?? 0}
            showInfo={false}
            strokeColor={
              (overview?.resourceUsage?.cpuUsage ?? 0) > 80
                ? "#cf1322"
                : "#3f8600"
            }
            size="small"
          />
        </Card>
      </Col>
      <Col span={4}>
        <Card loading={isLoading}>
          <Statistic
            title="内存使用率"
            value={overview?.resourceUsage?.memoryUsage ?? 0}
            suffix="%"
            valueStyle={{
              color:
                (overview?.resourceUsage?.memoryUsage ?? 0) > 80
                  ? "#cf1322"
                  : "#3f8600",
            }}
          />
          <Progress
            percent={overview?.resourceUsage?.memoryUsage ?? 0}
            showInfo={false}
            strokeColor={
              (overview?.resourceUsage?.memoryUsage ?? 0) > 80
                ? "#cf1322"
                : "#3f8600"
            }
            size="small"
          />
        </Card>
      </Col>
    </Row>
  );
};

/**
 * Agent 运行状态列表
 */
const AgentStatusTable: React.FC = () => {
  const { data, isLoading } = useQuery({
    queryKey: ["agentMonitoring", "runtimeStatusList"],
    queryFn: () => agentMonitoringService.getRuntimeStatusList(),
    refetchInterval: 30000, // 30秒刷新一次
  });

  const columns: ColumnsType<AgentRuntimeStatus> = [
    {
      title: "Agent ID",
      dataIndex: "id",
      key: "id",
      width: 280,
      render: (id: string) => (
        <Tooltip title={id}>
          <Text copyable={{ text: id }}>{id.slice(0, 8)}...</Text>
        </Tooltip>
      ),
    },
    {
      title: "状态",
      dataIndex: "status",
      key: "status",
      width: 100,
      render: (status: string) => getAgentStatusTag(status),
    },
    {
      title: "健康状态",
      dataIndex: "healthStatus",
      key: "healthStatus",
      width: 100,
      render: (_: unknown, record: AgentRuntimeStatus) =>
        getHealthStatusTag(record.healthStatus),
    },
    {
      title: "版本",
      dataIndex: "version",
      key: "version",
      width: 100,
    },
    {
      title: "最后心跳",
      dataIndex: "lastHeartbeatTime",
      key: "lastHeartbeatTime",
      width: 150,
      render: (time: string | null) =>
        time ? (
          <Tooltip title={dayjs(time).format("YYYY-MM-DD HH:mm:ss")}>
            {dayjs(time).fromNow()}
          </Tooltip>
        ) : (
          <Text type="secondary">-</Text>
        ),
    },
    {
      title: "心跳延迟",
      dataIndex: "heartbeatAgeSeconds",
      key: "heartbeatAgeSeconds",
      width: 100,
      render: (seconds: number | null) => {
        if (seconds === null) return <Text type="secondary">-</Text>;
        if (seconds < 60) return <Text type="success">{seconds}s</Text>;
        if (seconds < 300)
          return <Text type="warning">{Math.floor(seconds / 60)}m</Text>;
        return <Text type="danger">{Math.floor(seconds / 60)}m (超时)</Text>;
      },
    },
    {
      title: "状态消息",
      dataIndex: "statusMessage",
      key: "statusMessage",
      ellipsis: true,
      render: (msg: string | null) =>
        msg ? (
          <Tooltip title={msg}>
            <Text type="secondary">{msg}</Text>
          </Tooltip>
        ) : (
          <Text type="secondary">-</Text>
        ),
    },
    {
      title: "更新时间",
      dataIndex: "updatedAt",
      key: "updatedAt",
      width: 150,
      render: (time: string) => (
        <Tooltip title={dayjs(time).format("YYYY-MM-DD HH:mm:ss")}>
          {dayjs(time).fromNow()}
        </Tooltip>
      ),
    },
  ];

  return (
    <Card title="Agent 运行状态" extra={<MonitorOutlined />}>
      <Table
        columns={columns}
        dataSource={data?.data ?? []}
        rowKey="id"
        loading={isLoading}
        pagination={{
          showSizeChanger: true,
          showQuickJumper: true,
          showTotal: (total) => `共 ${total} 个 Agent`,
        }}
      />
    </Card>
  );
};

/**
 * Agent 监控大盘页面
 */
const AgentMonitorPage: React.FC = () => {
  const { data: overview, isLoading: overviewLoading } = useQuery({
    queryKey: ["agentMonitoring", "overview"],
    queryFn: () => agentMonitoringService.getOverview(),
    refetchInterval: 30000,
  });

  return (
    <div style={{ padding: 24 }}>
      <Space direction="vertical" size="large" style={{ width: "100%" }}>
        <Title level={4}>
          <DashboardOutlined /> Agent 监控大盘
        </Title>

        <OverviewCards overview={overview} isLoading={overviewLoading} />

        <AgentStatusTable />
      </Space>
    </div>
  );
};

export default AgentMonitorPage;
