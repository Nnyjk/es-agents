import React from "react";
import { Tag, Badge, Tooltip, Space } from "antd";
import {
  CheckCircleOutlined,
  CloseCircleOutlined,
  SyncOutlined,
  WarningOutlined,
  ClockCircleOutlined,
  StopOutlined,
} from "@ant-design/icons";
import type { AgentStatus } from "./types";
import {
  AGENT_STATUS_CONFIG,
  isProcessingStatus,
  isErrorStatus,
  isSuccessStatus,
} from "./types";
import styles from "./AgentStatus.module.css";

interface AgentStatusDisplayProps {
  /** Agent 状态 */
  status: AgentStatus;
  /** 显示模式: tag (标签) | badge (徽标) | text (纯文本) */
  mode?: "tag" | "badge" | "text";
  /** 是否显示图标 */
  showIcon?: boolean;
  /** 是否显示描述提示 */
  showTooltip?: boolean;
  /** 尺寸 */
  size?: "small" | "default" | "large";
  /** 自定义样式 */
  className?: string;
  /** 自定义样式 */
  style?: React.CSSProperties;
}

/**
 * 获取状态对应的图标
 */
const getStatusIcon = (status: AgentStatus): React.ReactNode => {
  if (isProcessingStatus(status)) {
    return <SyncOutlined spin />;
  }
  if (isSuccessStatus(status)) {
    return <CheckCircleOutlined />;
  }
  if (status === "ERROR") {
    return <CloseCircleOutlined />;
  }
  if (status === "OFFLINE") {
    return <StopOutlined />;
  }
  if (status === "UNCONFIGURED") {
    return <ClockCircleOutlined />;
  }
  return <WarningOutlined />;
};

/**
 * Agent 状态展示组件
 * 可复用的状态显示组件，支持多种显示模式
 */
const AgentStatusDisplay: React.FC<AgentStatusDisplayProps> = ({
  status,
  mode = "tag",
  showIcon = false,
  showTooltip = true,
  size = "default",
  className,
  style,
}) => {
  const config = AGENT_STATUS_CONFIG[status] || {
    color: "default",
    text: status,
    description: "未知状态",
  };

  const icon = showIcon ? getStatusIcon(status) : null;

  // 标签模式
  const renderTag = () => {
    const tagElement = (
      <Tag
        color={config.color}
        className={`${styles.statusTag} ${styles[`statusTag-${size}`]} ${className || ""}`}
        style={style}
        icon={icon}
      >
        {config.text}
      </Tag>
    );

    if (showTooltip) {
      return <Tooltip title={config.description}>{tagElement}</Tooltip>;
    }
    return tagElement;
  };

  // 徽标模式
  const renderBadge = () => {
    const badgeStatus = isProcessingStatus(status)
      ? "processing"
      : isSuccessStatus(status)
        ? "success"
        : isErrorStatus(status)
          ? "error"
          : "default";

    const badgeElement = (
      <Badge
        status={badgeStatus}
        text={
          <Space size={4}>
            {icon}
            <span
              className={`${styles.statusText} ${className || ""}`}
              style={style}
            >
              {config.text}
            </span>
          </Space>
        }
      />
    );

    if (showTooltip) {
      return <Tooltip title={config.description}>{badgeElement}</Tooltip>;
    }
    return badgeElement;
  };

  // 纯文本模式
  const renderText = () => {
    const textElement = (
      <span
        className={`${styles.statusText} ${styles[`statusText-${status}`]} ${className || ""}`}
        style={style}
      >
        {icon && (
          <Space size={4}>
            {icon}
            {config.text}
          </Space>
        )}
        {!icon && config.text}
      </span>
    );

    if (showTooltip) {
      return <Tooltip title={config.description}>{textElement}</Tooltip>;
    }
    return textElement;
  };

  switch (mode) {
    case "badge":
      return renderBadge();
    case "text":
      return renderText();
    default:
      return renderTag();
  }
};

/**
 * 获取状态枚举映射，用于 ProTable valueEnum
 */
export const getAgentStatusValueEnum = () => {
  const valueEnum: Record<AgentStatus, { text: string; status: string }> =
    {} as any;
  Object.entries(AGENT_STATUS_CONFIG).forEach(([key, config]) => {
    const statusKey = key as AgentStatus;
    let proStatus = "Default";
    if (isProcessingStatus(statusKey)) {
      proStatus = "Processing";
    } else if (isSuccessStatus(statusKey)) {
      proStatus = "Success";
    } else if (statusKey === "ERROR") {
      proStatus = "Error";
    } else if (statusKey === "UNCONFIGURED") {
      proStatus = "Warning";
    }
    valueEnum[statusKey] = {
      text: config.text,
      status: proStatus,
    };
  });
  return valueEnum;
};

export { AgentStatusDisplay };
export default AgentStatusDisplay;
