import React, { useEffect, useRef, useState } from "react";
import {
  Card,
  Descriptions,
  Tag,
  Typography,
  Badge,
  Steps,
  Space,
  Tooltip,
} from "antd";
import {
  CheckCircleOutlined,
  CloseCircleOutlined,
  SyncOutlined,
  WarningOutlined,
  WifiOutlined,
  DisconnectOutlined,
  LoadingOutlined,
} from "@ant-design/icons";
import dayjs from "dayjs";
import relativeTime from "dayjs/plugin/relativeTime";
import "dayjs/locale/zh-cn";
import type { AgentDetailData, WebSocketMessage } from "../types";
import { AgentStatusConfig } from "../types";
import styles from "../AgentDetail.module.css";

dayjs.extend(relativeTime);
dayjs.locale("zh-cn");

const { Text } = Typography;

/**
 * WebSocket 连接状态
 */
type WSConnectionState = "connecting" | "connected" | "disconnected";

interface AgentStatusCardProps {
  agent: AgentDetailData;
  onStatusChange?: (status: string) => void;
}

/**
 * 获取状态对应的步骤索引
 */
const getStatusStep = (status: string): number => {
  const statusOrder = [
    "UNCONFIGURED",
    "PREPARING",
    "READY",
    "PACKAGING",
    "PACKAGED",
    "DEPLOYING",
    "DEPLOYED",
    "ONLINE",
  ];
  const index = statusOrder.indexOf(status);
  return index >= 0 ? index : -1;
};

/**
 * Agent 状态卡片组件
 * 展示 Agent 基本信息、状态机可视化、WebSocket 实时状态更新
 */
const AgentStatusCard: React.FC<AgentStatusCardProps> = ({
  agent,
  onStatusChange,
}) => {
  const [currentStatus, setCurrentStatus] = useState(agent.status);
  const [lastHeartbeat, setLastHeartbeat] = useState(agent.lastHeartbeatTime);
  const [wsState, setWsState] = useState<WSConnectionState>("disconnected");
  const wsRef = useRef<WebSocket | null>(null);
  const reconnectTimerRef = useRef<NodeJS.Timeout | null>(null);

  // WebSocket 连接
  useEffect(() => {
    const connectWebSocket = () => {
      if (wsRef.current) {
        wsRef.current.close();
      }

      setWsState("connecting");
      const wsUrl = `${window.location.protocol === "https:" ? "wss:" : "ws:"}//${window.location.host}/ws/agent/${agent.id}`;
      const ws = new WebSocket(wsUrl);

      ws.onopen = () => {
        setWsState("connected");
        console.log(`WebSocket connected to agent ${agent.id}`);
      };

      ws.onmessage = (event) => {
        try {
          const message: WebSocketMessage = JSON.parse(event.data);
          handleMessage(message);
        } catch (e) {
          console.warn("Failed to parse WebSocket message", e);
        }
      };

      ws.onclose = () => {
        setWsState("disconnected");
        console.log(`WebSocket disconnected from agent ${agent.id}`);
        // 自动重连
        reconnectTimerRef.current = setTimeout(connectWebSocket, 5000);
      };

      ws.onerror = (error) => {
        console.error("WebSocket error", error);
        setWsState("disconnected");
      };

      wsRef.current = ws;
    };

    connectWebSocket();

    return () => {
      if (wsRef.current) {
        wsRef.current.close();
      }
      if (reconnectTimerRef.current) {
        clearTimeout(reconnectTimerRef.current);
      }
    };
  }, [agent.id]);

  // 处理 WebSocket 消息
  const handleMessage = (message: WebSocketMessage) => {
    switch (message.type) {
      case "STATUS_CHANGE":
        setCurrentStatus(message.status);
        onStatusChange?.(message.status);
        break;
      case "HEARTBEAT":
        setLastHeartbeat(message.timestamp);
        break;
      default:
        break;
    }
  };

  // 获取 WebSocket 状态指示器
  const getWsIndicator = () => {
    const config = {
      connected: {
        icon: <WifiOutlined />,
        text: "已连接",
        className: styles.wsConnected,
      },
      connecting: {
        icon: <LoadingOutlined />,
        text: "连接中",
        className: styles.wsConnecting,
      },
      disconnected: {
        icon: <DisconnectOutlined />,
        text: "已断开",
        className: styles.wsDisconnected,
      },
    };
    const { icon, text, className } = config[wsState];
    return (
      <span className={`${styles.wsIndicator} ${className}`}>
        {icon}
        <span>{text}</span>
      </span>
    );
  };

  // 获取状态标签
  const getStatusTag = (status: string) => {
    const config = AgentStatusConfig[status as keyof typeof AgentStatusConfig];
    if (!config) {
      return <Tag>{status}</Tag>;
    }
    return <Tag color={config.color}>{config.text}</Tag>;
  };

  // 获取状态图标
  const getStatusIcon = (status: string) => {
    switch (status) {
      case "ONLINE":
        return <CheckCircleOutlined style={{ color: "#52c41a" }} />;
      case "OFFLINE":
        return <CloseCircleOutlined style={{ color: "#ff4d4f" }} />;
      case "DEPLOYING":
      case "PREPARING":
      case "PACKAGING":
        return <SyncOutlined spin style={{ color: "#1890ff" }} />;
      case "ERROR":
        return <CloseCircleOutlined style={{ color: "#ff4d4f" }} />;
      default:
        return <WarningOutlined style={{ color: "#faad14" }} />;
    }
  };

  const statusConfig =
    AgentStatusConfig[currentStatus as keyof typeof AgentStatusConfig];
  const currentStep = getStatusStep(currentStatus);

  return (
    <Card
      className={styles.statusCard}
      title={
        <Space>
          <Badge status={wsState === "connected" ? "success" : "error"} />
          <span>Agent 状态</span>
          {getWsIndicator()}
        </Space>
      }
      extra={getStatusTag(currentStatus)}
    >
      <div className={styles.statusCardBody}>
        <div className={styles.statusInfo}>
          <Descriptions column={2} size="small" bordered>
            <Descriptions.Item label="Agent ID">
              <Text copyable>{agent.id}</Text>
            </Descriptions.Item>
            <Descriptions.Item label="状态">
              <Space>
                {getStatusIcon(currentStatus)}
                {statusConfig?.text || currentStatus}
              </Space>
            </Descriptions.Item>
            <Descriptions.Item label="所在主机">
              <Tooltip title={agent.hostId}>
                {agent.hostName} {agent.hostIp ? `(${agent.hostIp})` : ""}
              </Tooltip>
            </Descriptions.Item>
            <Descriptions.Item label="使用模板">
              <Tooltip title={agent.templateId}>{agent.templateName}</Tooltip>
            </Descriptions.Item>
            <Descriptions.Item label="版本">
              {agent.version || "-"}
            </Descriptions.Item>
            <Descriptions.Item label="在线状态">
              {currentStatus === "ONLINE" ? (
                <Tag color="success">在线</Tag>
              ) : (
                <Tag color="default">离线</Tag>
              )}
            </Descriptions.Item>
            <Descriptions.Item label="最后心跳">
              {lastHeartbeat
                ? dayjs(lastHeartbeat).format("YYYY-MM-DD HH:mm:ss")
                : "-"}
            </Descriptions.Item>
            <Descriptions.Item label="心跳延迟">
              {agent.heartbeatAgeSeconds != null
                ? `${agent.heartbeatAgeSeconds}s`
                : "-"}
            </Descriptions.Item>
            <Descriptions.Item label="创建时间" span={2}>
              {dayjs(agent.createdAt).format("YYYY-MM-DD HH:mm:ss")}
            </Descriptions.Item>
            <Descriptions.Item label="更新时间" span={2}>
              {dayjs(agent.updatedAt).format("YYYY-MM-DD HH:mm:ss")}
            </Descriptions.Item>
          </Descriptions>
        </div>

        <div className={styles.statusTimeline}>
          <Steps
            direction="vertical"
            size="small"
            current={currentStep >= 0 ? currentStep : 0}
            status={
              currentStatus === "ERROR"
                ? "error"
                : currentStep >= 6
                  ? "finish"
                  : "process"
            }
            items={[
              {
                title: "未配置",
                description: "Agent 实例已创建",
                status: currentStep >= 0 ? "finish" : "wait",
              },
              {
                title: "准备中",
                description: "准备部署资源",
                status: currentStep >= 1 ? "finish" : "wait",
              },
              {
                title: "打包中",
                description: "打包部署包",
                status: currentStep >= 3 ? "finish" : "wait",
              },
              {
                title: "部署中",
                description: "执行部署流程",
                status:
                  currentStatus === "DEPLOYING"
                    ? "process"
                    : currentStep >= 5
                      ? "finish"
                      : "wait",
              },
              {
                title: "已部署",
                description: "部署完成",
                status: currentStep >= 6 ? "finish" : "wait",
              },
              {
                title: "在线",
                description: "Agent 正常运行",
                status: currentStatus === "ONLINE" ? "finish" : "wait",
              },
            ]}
          />
        </div>
      </div>
    </Card>
  );
};

export default AgentStatusCard;