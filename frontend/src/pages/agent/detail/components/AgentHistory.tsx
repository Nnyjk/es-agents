import React, { useEffect, useState } from "react";
import {
  Card,
  Timeline,
  Tag,
  Typography,
  Empty,
  Spin,
  Button,
  Drawer,
  Descriptions,
  Space,
} from "antd";
import {
  CheckCircleOutlined,
  CloseCircleOutlined,
  SyncOutlined,
  ClockCircleOutlined,
  EyeOutlined,
  UserOutlined,
  CalendarOutlined,
} from "@ant-design/icons";
import dayjs from "dayjs";
import type { DeploymentHistoryRecord } from "../types";
import styles from "../AgentDetail.module.css";

const { Text, Paragraph } = Typography;

interface AgentHistoryProps {
  agentId: string;
}

const StatusConfig: Record<
  string,
  { color: string; text: string; icon: React.ReactNode }
> = {
  SUCCESS: {
    color: "success",
    text: "成功",
    icon: <CheckCircleOutlined />,
  },
  FAILED: {
    color: "error",
    text: "失败",
    icon: <CloseCircleOutlined />,
  },
  RUNNING: {
    color: "processing",
    text: "进行中",
    icon: <SyncOutlined spin />,
  },
  PENDING: {
    color: "default",
    text: "等待",
    icon: <ClockCircleOutlined />,
  },
};

/**
 * Agent 部署历史组件
 * 展示部署历史时间线，支持查看详情
 */
const AgentHistory: React.FC<AgentHistoryProps> = ({ agentId }) => {
  const [history, setHistory] = useState<DeploymentHistoryRecord[]>([]);
  const [loading, setLoading] = useState(false);
  const [detailDrawerVisible, setDetailDrawerVisible] = useState(false);
  const [selectedRecord, setSelectedRecord] =
    useState<DeploymentHistoryRecord | null>(null);

  // 加载部署历史
  useEffect(() => {
    const loadHistory = async () => {
      setLoading(true);
      try {
        const response = await fetch(`/api/agents/instances/${agentId}/deployments`);
        if (response.ok) {
          const data = await response.json();
          setHistory(data || []);
        }
      } catch (e) {
        console.error("Failed to load deployment history", e);
      } finally {
        setLoading(false);
      }
    };
    loadHistory();
  }, [agentId]);

  // 查看详情
  const handleViewDetail = (record: DeploymentHistoryRecord) => {
    setSelectedRecord(record);
    setDetailDrawerVisible(true);
  };

  // 渲染时间线项
  const renderTimelineItem = (record: DeploymentHistoryRecord) => {
    const statusConfig = StatusConfig[record.status] || {
      color: "default",
      text: record.status,
      icon: <ClockCircleOutlined />,
    };

    const durationText =
      record.durationMs != null
        ? record.durationMs < 1000
          ? `${record.durationMs}ms`
          : `${Math.round(record.durationMs / 1000)}s`
        : "-";

    return (
      <Timeline.Item
        key={record.id}
        dot={statusConfig.icon}
        color={
          record.status === "SUCCESS"
            ? "green"
            : record.status === "FAILED"
              ? "red"
              : "blue"
        }
      >
        <div style={{ marginBottom: 8 }}>
          <Space>
            <Tag color={statusConfig.color}>{statusConfig.text}</Tag>
            <Text strong>v{record.version}</Text>
            <Text type="secondary">{durationText}</Text>
          </Space>
        </div>
        <div style={{ marginBottom: 4 }}>
          <Space split={<Text type="secondary">|</Text>}>
            <Text type="secondary">
              <CalendarOutlined />{" "}
              {dayjs(record.createdAt).format("YYYY-MM-DD HH:mm")}
            </Text>
            <Text type="secondary">
              <UserOutlined /> {record.triggeredBy || "系统"}
            </Text>
          </Space>
        </div>
        {record.message && (
          <Paragraph
            type="secondary"
            ellipsis={{ rows: 1 }}
            style={{ marginBottom: 4, fontSize: 12 }}
          >
            {record.message}
          </Paragraph>
        )}
        <Button
          type="link"
          size="small"
          icon={<EyeOutlined />}
          onClick={() => handleViewDetail(record)}
        >
          详情
        </Button>
      </Timeline.Item>
    );
  };

  return (
    <Card className={styles.historyCard} title="部署历史" extra={<Text type="secondary">共 {history.length} 次</Text>}>
      <Spin spinning={loading}>
        {history.length > 0 ? (
          <Timeline className={styles.historyTimeline}>
            {history.map(renderTimelineItem)}
          </Timeline>
        ) : (
          <Empty description="暂无部署历史" />
        )}
      </Spin>

      {/* 详情抽屉 */}
      <Drawer
        title="部署详情"
        placement="right"
        width={600}
        onClose={() => {
          setDetailDrawerVisible(false);
          setSelectedRecord(null);
        }}
        open={detailDrawerVisible}
      >
        {selectedRecord && (
          <Descriptions column={1} bordered size="small">
            <Descriptions.Item label="部署 ID">
              <Text copyable>{selectedRecord.id}</Text>
            </Descriptions.Item>
            <Descriptions.Item label="版本">
              <Text code>v{selectedRecord.version}</Text>
            </Descriptions.Item>
            <Descriptions.Item label="状态">
              <Tag color={StatusConfig[selectedRecord.status]?.color}>
                {StatusConfig[selectedRecord.status]?.text || selectedRecord.status}
              </Tag>
            </Descriptions.Item>
            <Descriptions.Item label="触发类型">
              {selectedRecord.triggerType === "MANUAL" ? "手动部署" : "自动部署"}
            </Descriptions.Item>
            <Descriptions.Item label="触发人">
              {selectedRecord.triggeredBy || "系统"}
            </Descriptions.Item>
            <Descriptions.Item label="开始时间">
              {selectedRecord.startedAt
                ? dayjs(selectedRecord.startedAt).format("YYYY-MM-DD HH:mm:ss")
                : "-"}
            </Descriptions.Item>
            <Descriptions.Item label="结束时间">
              {selectedRecord.finishedAt
                ? dayjs(selectedRecord.finishedAt).format("YYYY-MM-DD HH:mm:ss")
                : "-"}
            </Descriptions.Item>
            <Descriptions.Item label="耗时">
              {selectedRecord.durationMs != null
                ? selectedRecord.durationMs < 1000
                  ? `${selectedRecord.durationMs}ms`
                  : `${Math.round(selectedRecord.durationMs / 1000)}s`
                : "-"}
            </Descriptions.Item>
            <Descriptions.Item label="创建时间">
              {dayjs(selectedRecord.createdAt).format("YYYY-MM-DD HH:mm:ss")}
            </Descriptions.Item>
            {selectedRecord.message && (
              <Descriptions.Item label="备注">
                <Paragraph>{selectedRecord.message}</Paragraph>
              </Descriptions.Item>
            )}
          </Descriptions>
        )}
      </Drawer>
    </Card>
  );
};

export default AgentHistory;