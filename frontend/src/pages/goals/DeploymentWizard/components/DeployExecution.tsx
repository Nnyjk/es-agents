import React, { useState, useEffect } from "react";
import { Result, Spin, Card, Steps, Alert } from "antd";
import { CheckCircleOutlined, CloseCircleOutlined, LoadingOutlined, SyncOutlined } from "@ant-design/icons";
import { deployAgentInstance, saveAgentInstance } from "../../../../services/agent";
import type { Host, AgentTemplate, DeployParams, DeployResult as DeployResultType } from "../../../../types";

interface DeployExecutionProps {
  host: Host | null;
  template: AgentTemplate | null;
  deployParams: DeployParams;
  onComplete: (result: DeployResultType) => void;
}

const DeployExecution: React.FC<DeployExecutionProps> = ({
  host,
  template,
  deployParams,
  onComplete,
}) => {
  const [loading, setLoading] = useState(false);
  const [status, setStatus] = useState<"pending" | "creating" | "deploying" | "success" | "failed">("pending");
  const [logs, setLogs] = useState<string[]>([]);
  const [_instanceId, setInstanceId] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  const addLog = (message: string) => {
    setLogs((prev) => [...prev, `[${new Date().toLocaleTimeString()}] ${message}`]);
  };

  useEffect(() => {
    if (host && template && deployParams.version) {
      startDeployment();
    }
  }, []);

  const startDeployment = async () => {
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
      setStatus("deploying");
      addLog("开始部署 Agent...");
      
      // Step 2: Deploy the instance
      const result = await deployAgentInstance(instance.id, deployParams);
      
      addLog(`部署完成: ${result.status}`);
      if (result.message) {
        addLog(`部署消息: ${result.message}`);
      }
      
      setStatus("success");
      addLog("部署成功!");
      
      onComplete({
        instanceId: result.instanceId,
        status: result.status,
        message: result.message,
        deployedAt: result.deployedAt,
      });
    } catch (err: any) {
      console.error("Deployment failed:", err);
      setError(err.message || "部署失败");
      setStatus("failed");
      addLog(`部署失败: ${err.message || "未知错误"}`);
    } finally {
      setLoading(false);
    }
  };

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
        <Result
          icon={<SyncOutlined spin style={{ color: "#1890ff" }} />}
          title="正在部署 Agent"
          subTitle="部署可能需要几分钟时间，请耐心等待..."
        />
      );
    }

    if (status === "success") {
      return (
        <Result
          icon={<CheckCircleOutlined style={{ color: "#52c41a" }} />}
          title="部署成功"
          subTitle="Agent 已成功部署到目标主机"
        />
      );
    }

    if (status === "failed") {
      return (
        <Result
          icon={<CloseCircleOutlined style={{ color: "#ff4d4f" }} />}
          title="部署失败"
          subTitle={error || "请检查日志了解详情"}
        />
      );
    }

    return null;
  };

  return (
    <div>
      <Card title="部署进度" style={{ marginBottom: 16 }}>
        <Steps
          current={["pending", "creating", "deploying", "success", "failed"].indexOf(status)}
          items={[
            { title: "创建实例", status: status === "pending" ? "process" : "finish" },
            { title: "部署 Agent", status: status === "deploying" ? "process" : status === "success" || status === "failed" ? "finish" : "wait" },
            { title: "完成", status: status === "success" ? "finish" : status === "failed" ? "error" : "wait" },
          ]}
        />
      </Card>

      {renderStatus()}

      {logs.length > 0 && (
        <Card title="部署日志" style={{ marginTop: 16 }}>
          <div style={{ 
            background: "#1e1e1e", 
            color: "#d4d4d4", 
            padding: 16, 
            borderRadius: 4,
            fontFamily: "monospace",
            fontSize: 12,
            maxHeight: 200,
            overflow: "auto"
          }}>
            {logs.map((log, index) => (
              <div key={index}>{log}</div>
            ))}
          </div>
        </Card>
      )}

      {error && (
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