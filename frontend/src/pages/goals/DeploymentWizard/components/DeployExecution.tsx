import React, { useState, useEffect, useRef, useCallback } from "react";
import {
  Result,
  Spin,
  Card,
  Steps,
  Alert,
  Progress,
  Button,
  Tag,
  Space,
} from "antd";
import {
  CheckCircleOutlined,
  CloseCircleOutlined,
  LoadingOutlined,
  SyncOutlined,
  ReloadOutlined,
  LinkOutlined,
  DisconnectOutlined,
} from "@ant-design/icons";
import {
  deployAgentInstance,
  saveAgentInstance,
} from "../../../../services/agent";
import type {
  Host,
  AgentTemplate,
  DeployParams,
  DeployResult as DeployResultType,
  DeploymentProgressMessage,
  DeploymentStatus,
  DeploymentStage,
} from "../../../../types";

interface DeployExecutionProps {
  host: Host | null;
  template: AgentTemplate | null;
  deployParams: DeployParams;
  onComplete: (result: DeployResultType) => void;
}

// 部署阶段定义
const DEPLOYMENT_STAGES = [
  { key: "PREPARING", title: "准备环境", description: "初始化部署环境" },
  { key: "PACKAGING", title: "打包资源", description: "打包 Agent 资源" },
  { key: "UPLOADING", title: "上传文件", description: "上传到目标主机" },
  { key: "INSTALLING", title: "安装部署", description: "执行安装脚本" },
  { key: "CONFIGURING", title: "配置验证", description: "配置并验证 Agent" },
  { key: "COMPLETED", title: "部署完成", description: "部署成功" },
];

const stageIndexMap: Record<string, number> = {
  PREPARING: 0,
  PACKAGING: 1,
  UPLOADING: 2,
  INSTALLING: 3,
  CONFIGURING: 4,
  COMPLETED: 5,
};

const DeployExecution: React.FC<DeployExecutionProps> = ({
  host,
  template,
  deployParams,
  onComplete,
}) => {
  const [loading, setLoading] = useState(false);
  const [status, setStatus] = useState<
    "pending" | "creating" | "deploying" | "success" | "failed"
  >("pending");
  const [logs, setLogs] = useState<string[]>([]);
  const [instanceId, setInstanceId] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  // WebSocket 状态
  const [wsConnected, setWsConnected] = useState(false);
  const [overallProgress, setOverallProgress] = useState(0);
  const [currentStage, setCurrentStage] = useState<string>("PREPARING");
  const [stageProgress, setStageProgress] = useState<
    Record<string, DeploymentStage>
  >({});
  const wsRef = useRef<WebSocket | null>(null);
  const reconnectTimerRef = useRef<number | null>(null);
  const reconnectAttemptsRef = useRef<number>(0);

  const MAX_RECONNECT_ATTEMPTS = 5;
  const RECONNECT_DELAY = 3000;

  const addLog = useCallback(
    (message: string, level: "info" | "warn" | "error" = "info") => {
      const timestamp = new Date().toLocaleTimeString();
      const levelPrefix =
        level === "error" ? "[ERROR]" : level === "warn" ? "[WARN]" : "[INFO]";
      setLogs((prev) => [...prev, `[${timestamp}] ${levelPrefix} ${message}`]);
    },
    [],
  );

  // WebSocket 消息处理
  const handleWebSocketMessage = useCallback(
    (event: MessageEvent) => {
      try {
        const message: DeploymentProgressMessage = JSON.parse(event.data);
        addLog(`收到消息: ${message.type} - ${message.message || ""}`);

        switch (message.type) {
          case "DEPLOYMENT_PROGRESS":
            if (message.progress !== undefined) {
              setOverallProgress(message.progress);
            }
            if (message.stage) {
              const stage = message.stage;
              setCurrentStage(stage);
              setStageProgress((prev) => ({
                ...prev,
                [stage]: {
                  name: stage,
                  status: "RUNNING",
                  progress: message.progress || 0,
                  message: message.message,
                },
              }));
            }
            break;

          case "DEPLOYMENT_STATUS":
            if (message.status) {
              if (message.status === "SUCCESS") {
                setStatus("success");
                setOverallProgress(100);
                addLog("部署成功完成!", "info");
                // 延迟调用 onComplete，让用户看到成功状态
                setTimeout(() => {
                  onComplete({
                    instanceId: instanceId!,
                    status: "DEPLOYED",
                    message: message.message || "部署成功",
                    deployedAt: new Date().toISOString(),
                  });
                }, 1500);
              } else if (message.status === "FAILED") {
                setStatus("failed");
                setError(message.message || "部署失败");
                addLog(message.message || "部署失败", "error");
              }
            }
            if (message.stage && message.status) {
              const stage = message.stage;
              setStageProgress((prev) => ({
                ...prev,
                [stage]: {
                  name: stage,
                  status: message.status as DeploymentStatus,
                  progress:
                    message.status === "SUCCESS"
                      ? 100
                      : prev[stage]?.progress || 0,
                  message: message.message,
                },
              }));
            }
            break;

          case "DEPLOYMENT_ERROR":
            setStatus("failed");
            setError(message.message || "部署过程中发生错误");
            addLog(message.message || "部署错误", "error");
            if (message.stage) {
              const stage = message.stage;
              setStageProgress((prev) => ({
                ...prev,
                [stage]: {
                  name: stage,
                  status: "FAILED",
                  progress: prev[stage]?.progress || 0,
                  message: message.message,
                },
              }));
            }
            break;
        }
      } catch (e) {
        // 非 JSON 格式消息，作为原始日志处理
        addLog(event.data);
      }
    },
    [addLog, instanceId, onComplete],
  );

  // WebSocket 连接
  const connectWebSocket = useCallback(
    (deploymentId: string) => {
      if (wsRef.current) {
        wsRef.current.close();
      }

      const wsProtocol = window.location.protocol === "https:" ? "wss:" : "ws:";
      const wsUrl = `${wsProtocol}//${window.location.host}/ws/deployment/${deploymentId}`;

      addLog(`正在连接 WebSocket: ${wsUrl}`);
      const ws = new WebSocket(wsUrl);

      ws.onopen = () => {
        addLog("WebSocket 连接成功");
        setWsConnected(true);
        reconnectAttemptsRef.current = 0;
        // 发送心跳或状态查询
        ws.send(JSON.stringify({ type: "SUBSCRIBE", deploymentId }));
      };

      ws.onmessage = handleWebSocketMessage;

      ws.onclose = (event) => {
        setWsConnected(false);
        addLog(
          `WebSocket 连接关闭: code=${event.code}, reason=${event.reason}`,
          "warn",
        );

        // 如果部署仍在进行中，尝试重连
        if (
          status === "deploying" &&
          reconnectAttemptsRef.current < MAX_RECONNECT_ATTEMPTS
        ) {
          reconnectAttemptsRef.current++;
          addLog(
            `尝试重连 (${reconnectAttemptsRef.current}/${MAX_RECONNECT_ATTEMPTS})...`,
            "warn",
          );
          reconnectTimerRef.current = window.setTimeout(() => {
            connectWebSocket(deploymentId);
          }, RECONNECT_DELAY);
        } else if (reconnectAttemptsRef.current >= MAX_RECONNECT_ATTEMPTS) {
          addLog("WebSocket 重连失败，请刷新页面查看最新状态", "error");
        }
      };

      ws.onerror = (error) => {
        setWsConnected(false);
        addLog("WebSocket 连接错误", "error");
        console.error("WebSocket error:", error);
      };

      wsRef.current = ws;
    },
    [handleWebSocketMessage, status, addLog],
  );

  // 清理 WebSocket
  const cleanupWebSocket = useCallback(() => {
    if (reconnectTimerRef.current) {
      clearTimeout(reconnectTimerRef.current);
      reconnectTimerRef.current = null;
    }
    if (wsRef.current) {
      wsRef.current.close();
      wsRef.current = null;
    }
  }, []);

  // 开始部署
  const startDeployment = useCallback(async () => {
    if (!host || !template) return;

    try {
      setLoading(true);
      setStatus("creating");
      addLog("开始创建 Agent 实例...");

      // Step 1: Create Agent Instance
      const instance = await saveAgentInstance({
        hostId: host.id,
        templateId: template.id,
        status: "UNCONFIGURED",
      });

      setInstanceId(instance.id);
      addLog(`Agent 实例创建成功: ${instance.id}`);

      // Step 2: Connect WebSocket for real-time progress
      setStatus("deploying");
      setCurrentStage("PREPARING");
      addLog("开始部署 Agent...");
      connectWebSocket(instance.id);

      // Step 3: Call deploy API
      const result = await deployAgentInstance(instance.id, deployParams);

      addLog(`部署请求已提交: ${result.status}`);
      if (result.message) {
        addLog(`部署消息: ${result.message}`);
      }

      // 如果 WebSocket 未连接，使用 HTTP 结果
      if (!wsConnected) {
        if (result.status === "DEPLOYED") {
          setStatus("success");
          addLog("部署成功!");
          onComplete({
            instanceId: result.instanceId,
            status: result.status,
            message: result.message,
            deployedAt: result.deployedAt,
          });
        } else if (result.status === "EXCEPTION") {
          setStatus("failed");
          setError(result.message);
          addLog(`部署失败: ${result.message}`, "error");
        }
      }
    } catch (err: any) {
      console.error("Deployment failed:", err);
      setError(err.message || "部署失败");
      setStatus("failed");
      addLog(`部署失败: ${err.message || "未知错误"}`, "error");
      cleanupWebSocket();
    } finally {
      setLoading(false);
    }
  }, [
    host,
    template,
    deployParams,
    connectWebSocket,
    cleanupWebSocket,
    wsConnected,
    addLog,
    onComplete,
  ]);

  // 组件加载时自动开始部署
  useEffect(() => {
    if (host && template && deployParams.version) {
      startDeployment();
    }
  }, []);

  // 组件卸载时清理 WebSocket
  useEffect(() => {
    return () => {
      cleanupWebSocket();
    };
  }, [cleanupWebSocket]);

  // 重试部署
  const handleRetry = () => {
    setError(null);
    setLogs([]);
    setOverallProgress(0);
    setCurrentStage("PREPARING");
    setStageProgress({});
    cleanupWebSocket();
    startDeployment();
  };

  // 获取当前步骤索引
  const getCurrentStepIndex = () => {
    return stageIndexMap[currentStage] || 0;
  };

  // 获取步骤状态
  const getStepStatus = (
    stageKey: string,
  ): "wait" | "process" | "finish" | "error" => {
    const stage = stageProgress[stageKey];
    if (!stage) {
      return getCurrentStepIndex() > stageIndexMap[stageKey]
        ? "finish"
        : "wait";
    }
    switch (stage.status) {
      case "SUCCESS":
        return "finish";
      case "RUNNING":
        return "process";
      case "FAILED":
        return "error";
      default:
        return "wait";
    }
  };

  // 渲染状态图标
  const renderStatus = () => {
    if (loading && status === "pending") {
      return (
        <Result
          icon={<Spin indicator={<LoadingOutlined spin />} />}
          title="准备部署..."
        />
      );
    }

    if (status === "creating") {
      return (
        <Result
          icon={<SyncOutlined spin style={{ color: "#1890ff" }} />}
          title="正在创建 Agent 实例"
          subTitle="请稍候..."
        />
      );
    }

    if (status === "deploying") {
      return (
        <div>
          <Result
            icon={<SyncOutlined spin style={{ color: "#1890ff" }} />}
            title="正在部署 Agent"
            subTitle="部署可能需要几分钟时间，请耐心等待..."
          />
          <div style={{ textAlign: "center", marginTop: 16 }}>
            <Progress
              percent={overallProgress}
              status="active"
              strokeColor={{
                "0%": "#108ee9",
                "100%": "#87d068",
              }}
            />
            <div style={{ marginTop: 8, color: "#666" }}>
              当前阶段: {currentStage}
            </div>
          </div>
        </div>
      );
    }

    if (status === "success") {
      return (
        <Result
          icon={<CheckCircleOutlined style={{ color: "#52c41a" }} />}
          title="部署成功"
          subTitle="Agent 已成功部署到目标主机"
        >
          <Progress percent={100} strokeColor="#52c41a" />
        </Result>
      );
    }

    if (status === "failed") {
      return (
        <Result
          icon={<CloseCircleOutlined style={{ color: "#ff4d4f" }} />}
          title="部署失败"
          subTitle={error || "请检查日志了解详情"}
          extra={
            <Button
              type="primary"
              onClick={handleRetry}
              icon={<ReloadOutlined />}
            >
              重试部署
            </Button>
          }
        >
          <Progress percent={overallProgress} status="exception" />
        </Result>
      );
    }

    return null;
  };

  return (
    <div>
      {/* WebSocket 连接状态 */}
      {status === "deploying" && (
        <Card size="small" style={{ marginBottom: 16 }}>
          <Space>
            <span>WebSocket 连接状态:</span>
            {wsConnected ? (
              <Tag color="success" icon={<LinkOutlined />}>
                已连接
              </Tag>
            ) : (
              <Tag color="warning" icon={<DisconnectOutlined />}>
                未连接
              </Tag>
            )}
            <span style={{ marginLeft: 16 }}>实例 ID: {instanceId || "-"}</span>
          </Space>
        </Card>
      )}

      {/* 部署进度步骤 */}
      <Card title="部署进度" style={{ marginBottom: 16 }}>
        <Steps
          current={getCurrentStepIndex()}
          items={DEPLOYMENT_STAGES.map((stage) => ({
            title: stage.title,
            description: stage.description,
            status: getStepStatus(stage.key),
          }))}
        />
      </Card>

      {renderStatus()}

      {/* 实时日志 */}
      {logs.length > 0 && (
        <Card title="部署日志" style={{ marginTop: 16 }}>
          <div
            style={{
              background: "#1e1e1e",
              color: "#d4d4d4",
              padding: 16,
              borderRadius: 4,
              fontFamily: "monospace",
              fontSize: 12,
              maxHeight: 300,
              overflow: "auto",
            }}
          >
            {logs.map((log, index) => (
              <div
                key={index}
                style={{
                  color: log.includes("[ERROR]")
                    ? "#ff4d4f"
                    : log.includes("[WARN]")
                      ? "#fa8c16"
                      : "#d4d4d4",
                }}
              >
                {log}
              </div>
            ))}
          </div>
        </Card>
      )}

      {error && status !== "failed" && (
        <Alert
          message="部署错误"
          description={error}
          type="error"
          showIcon
          style={{ marginTop: 16 }}
        />
      )}
    </div>
  );
};

export default DeployExecution;
