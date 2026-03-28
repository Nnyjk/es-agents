import React from "react";
import { Button, Space, Tooltip } from "antd";
import type { AgentStatus } from "./types";
import styles from "./AgentStatus.module.css";

interface StatusAction {
  key: string;
  label: string;
  onClick: () => void;
  disabled?: boolean;
  danger?: boolean;
  tooltip?: string;
}

interface AgentStatusActionsProps {
  /** 当前状态 */
  status: AgentStatus;
  /** 操作回调 */
  onPackage?: () => void;
  onDeploy?: () => void;
  onStart?: () => void;
  onStop?: () => void;
  onRollback?: () => void;
  onRetry?: () => void;
  onViewLog?: () => void;
  onConfigure?: () => void;
  /** 自定义样式 */
  className?: string;
}

/**
 * 根据状态获取可用操作
 */
const getAvailableActions = (
  status: AgentStatus,
  callbacks: Omit<AgentStatusActionsProps, "status" | "className">,
): StatusAction[] => {
  const actions: StatusAction[] = [];

  switch (status) {
    case "UNCONFIGURED":
      actions.push({
        key: "configure",
        label: "配置",
        onClick: callbacks.onConfigure || (() => {}),
      });
      break;

    case "READY":
      actions.push({
        key: "package",
        label: "打包",
        onClick: callbacks.onPackage || (() => {}),
      });
      break;

    case "PACKAGED":
      actions.push(
        {
          key: "package",
          label: "打包",
          onClick: callbacks.onPackage || (() => {}),
        },
        {
          key: "deploy",
          label: "部署",
          onClick: callbacks.onDeploy || (() => {}),
        },
      );
      break;

    case "DEPLOYED":
      actions.push({
        key: "start",
        label: "启动",
        onClick: callbacks.onStart || (() => {}),
      });
      break;

    case "ONLINE":
      actions.push(
        {
          key: "stop",
          label: "停止",
          onClick: callbacks.onStop || (() => {}),
          danger: true,
        },
        {
          key: "viewLog",
          label: "日志",
          onClick: callbacks.onViewLog || (() => {}),
        },
      );
      break;

    case "OFFLINE":
      actions.push({
        key: "start",
        label: "启动",
        onClick: callbacks.onStart || (() => {}),
      });
      break;

    case "ERROR":
      actions.push(
        {
          key: "retry",
          label: "重试",
          onClick: callbacks.onRetry || (() => {}),
        },
        {
          key: "viewLog",
          label: "日志",
          onClick: callbacks.onViewLog || (() => {}),
        },
      );
      break;

    default:
      break;
  }

  return actions;
};

/**
 * Agent 状态操作按钮组件
 */
export const AgentStatusActions: React.FC<AgentStatusActionsProps> = ({
  status,
  className,
  ...callbacks
}) => {
  const actions = getAvailableActions(status, callbacks);

  if (actions.length === 0) {
    return null;
  }

  return (
    <div className={`${styles.actionsContainer} ${className || ""}`}>
      <Space wrap>
        {actions.map((action) => (
          <Tooltip key={action.key} title={action.tooltip}>
            <Button
              type={action.danger ? "primary" : "default"}
              danger={action.danger}
              onClick={action.onClick}
              className={styles.actionButton}
            >
              {action.label}
            </Button>
          </Tooltip>
        ))}
      </Space>
    </div>
  );
};

export default AgentStatusActions;
