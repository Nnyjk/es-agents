import React from "react";
import { Timeline, Typography, Space, Tag, Empty } from "antd";
import {
  CheckCircleOutlined,
  CloseCircleOutlined,
  SyncOutlined,
  ClockCircleOutlined,
  MinusCircleOutlined,
} from "@ant-design/icons";
import dayjs from "dayjs";
import type { AgentStatus, StatusHistoryRecord } from "./types";
import {
  AGENT_STATUS_CONFIG,
  STATUS_FLOW_ORDER,
  getStatusStepIndex,
} from "./types";
import styles from "./AgentStatus.module.css";

const { Text } = Typography;

interface StatusHistoryItem {
  status: AgentStatus;
  timestamp: string;
  reason?: string;
}

interface AgentStatusTimelineProps {
  /** 状态历史记录 */
  history?: StatusHistoryItem[] | StatusHistoryRecord[];
  /** 当前状态 */
  currentStatus?: AgentStatus;
  /** 显示模式: flow (流转步骤) | history (历史记录) */
  mode?: "flow" | "history";
  /** 自定义样式 */
  className?: string;
}

/**
 * 获取状态时间线图标
 */
const getTimelineIcon = (
  status: AgentStatus,
  isActive: boolean,
): React.ReactNode => {
  const config = AGENT_STATUS_CONFIG[status];
  const isProcessing = config.color === "processing";
  const isError = config.color === "error";
  const isSuccess = config.color === "success";

  if (isError) {
    return <CloseCircleOutlined style={{ color: "#ff4d4f" }} />;
  }
  if (isActive && isProcessing) {
    return <SyncOutlined spin style={{ color: "#1890ff" }} />;
  }
  if (isActive && status === "ONLINE") {
    return <CheckCircleOutlined style={{ color: "#52c41a" }} />;
  }
  if (isSuccess) {
    return <CheckCircleOutlined style={{ color: "#52c41a" }} />;
  }
  return <ClockCircleOutlined style={{ color: "#d9d9d9" }} />;
};

/**
 * 获取时间线颜色
 */
const getTimelineColor = (
  status: AgentStatus,
  stepIndex: number,
  currentStepIndex: number,
  currentStatus: AgentStatus,
): string => {
  if (currentStatus === "ERROR" && stepIndex === currentStepIndex) {
    return "red";
  }
  if (stepIndex < currentStepIndex) {
    return "green";
  }
  if (stepIndex === currentStepIndex) {
    const config = AGENT_STATUS_CONFIG[status];
    if (config.color === "processing") {
      return "blue";
    }
    if (status === "ONLINE") {
      return "green";
    }
    return "blue";
  }
  return "gray";
};

/**
 * Agent 状态流转时间线组件
 * 显示状态流转过程或历史记录
 */
export const AgentStatusTimeline: React.FC<AgentStatusTimelineProps> = ({
  history = [],
  currentStatus,
  mode = "history",
  className,
}) => {
  // 流转步骤模式
  const renderFlowTimeline = () => {
    if (!currentStatus) {
      return <Empty description="暂无状态信息" />;
    }

    const currentStepIndex = getStatusStepIndex(currentStatus);

    const timelineItems = STATUS_FLOW_ORDER.map((status, index) => {
      const config = AGENT_STATUS_CONFIG[status];
      const isActive = index === currentStepIndex;
      const isCurrentError = currentStatus === "ERROR" && isActive;

      const dot = getTimelineIcon(status, isActive);
      const color = getTimelineColor(
        status,
        index,
        currentStepIndex,
        currentStatus,
      );

      return {
        key: status,
        dot,
        color,
        children: (
          <div className={styles.timelineItem}>
            <Space>
              <Text strong={isActive}>{config.text}</Text>
              {isActive && (
                <Tag
                  color={
                    isCurrentError
                      ? "error"
                      : isActive
                        ? "processing"
                        : "success"
                  }
                >
                  {isCurrentError ? "异常" : "当前"}
                </Tag>
              )}
            </Space>
            <Text type="secondary" className={styles.timelineDescription}>
              {config.description}
            </Text>
          </div>
        ),
      };
    });

    // 如果当前状态是 ERROR 或 OFFLINE，添加特殊节点
    if (currentStatus === "ERROR") {
      timelineItems.push({
        key: "ERROR",
        dot: <CloseCircleOutlined style={{ color: "#ff4d4f" }} />,
        color: "red",
        children: (
          <div className={styles.timelineItem}>
            <Space>
              <Text strong type="danger">
                {AGENT_STATUS_CONFIG.ERROR.text}
              </Text>
              <Tag color="error">异常</Tag>
            </Space>
            <Text type="secondary" className={styles.timelineDescription}>
              {AGENT_STATUS_CONFIG.ERROR.description}
            </Text>
          </div>
        ),
      });
    }

    if (currentStatus === "OFFLINE") {
      timelineItems.push({
        key: "OFFLINE",
        dot: <MinusCircleOutlined style={{ color: "#8c8c8c" }} />,
        color: "gray",
        children: (
          <div className={styles.timelineItem}>
            <Space>
              <Text type="secondary">{AGENT_STATUS_CONFIG.OFFLINE.text}</Text>
              <Tag>离线</Tag>
            </Space>
            <Text type="secondary" className={styles.timelineDescription}>
              {AGENT_STATUS_CONFIG.OFFLINE.description}
            </Text>
          </div>
        ),
      });
    }

    return (
      <div className={`${styles.timelineContainer} ${className || ""}`}>
        <Timeline items={timelineItems} />
      </div>
    );
  };

  // 历史记录模式
  const renderHistoryTimeline = () => {
    // 如果没有历史记录，使用当前状态生成一条记录
    const timelineItems =
      history.length > 0
        ? history
        : currentStatus
          ? [
              {
                status: currentStatus,
                timestamp: new Date().toISOString(),
              },
            ]
          : [];

    if (timelineItems.length === 0) {
      return <Empty description="暂无状态变更历史" />;
    }

    return (
      <div className={`${styles.timelineContainer} ${className || ""}`}>
        <Timeline
          items={timelineItems.map((item, index) => {
            const status =
              (item as StatusHistoryRecord).toStatus ||
              (item as StatusHistoryItem).status;
            const timestamp =
              (item as StatusHistoryRecord).timestamp ||
              (item as StatusHistoryItem).timestamp;
            const reason =
              (item as StatusHistoryRecord).reason ||
              (item as StatusHistoryItem).reason;
            const fromStatus = (item as StatusHistoryRecord).fromStatus;
            const triggeredBy = (item as StatusHistoryRecord).triggeredBy;

            const config = AGENT_STATUS_CONFIG[status];
            const fromConfig = fromStatus
              ? AGENT_STATUS_CONFIG[fromStatus]
              : null;

            return {
              key: index,
              color: config.color,
              children: (
                <div className={styles.timelineItem}>
                  <div className={styles.historyHeader}>
                    <Space>
                      <Tag color={config.color}>{config.text}</Tag>
                      {fromConfig && (
                        <Text type="secondary">从 {fromConfig.text} 变更</Text>
                      )}
                    </Space>
                  </div>
                  {reason && (
                    <Text type="secondary" style={{ fontSize: 12 }}>
                      原因: {reason}
                    </Text>
                  )}
                  {triggeredBy && (
                    <Text type="secondary" style={{ fontSize: 12 }}>
                      操作人: {triggeredBy}
                    </Text>
                  )}
                  <div>
                    <Text type="secondary" style={{ fontSize: 12 }}>
                      {dayjs(timestamp).format("YYYY-MM-DD HH:mm:ss")}
                    </Text>
                  </div>
                </div>
              ),
              dot: getTimelineIcon(status, true),
            };
          })}
        />
      </div>
    );
  };

  return mode === "flow" ? renderFlowTimeline() : renderHistoryTimeline();
};

export default AgentStatusTimeline;
