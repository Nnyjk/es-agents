import React, { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import {
  Card,
  Descriptions,
  Tag,
  Typography,
  Progress,
  Button,
  Space,
  message,
  Spin,
  Timeline,
  Popconfirm,
  Row,
  Col,
  Statistic,
} from "antd";
import {
  ArrowLeftOutlined,
  StopOutlined,
  ReloadOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  ClockCircleOutlined,
  PlayCircleOutlined,
} from "@ant-design/icons";
import {
  getDeployment,
  getDeploymentProgress,
  cancelDeployment,
  retryDeployment,
} from "@/services/deployment";
import type {
  DeploymentDetail,
  DeploymentProgress,
  DeploymentRecordStatus,
  DeploymentStageProgress,
} from "@/types/deployment";

const statusColors: Record<DeploymentRecordStatus, string> = {
  pending: "default",
  running: "processing",
  success: "success",
  failed: "error",
  cancelled: "warning",
};

const statusLabels: Record<DeploymentRecordStatus, string> = {
  pending: "待执行",
  running: "执行中",
  success: "成功",
  failed: "失败",
  cancelled: "已取消",
};

const DeploymentDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [deployment, setDeployment] = useState<DeploymentDetail | null>(null);
  const [progress, setProgress] = useState<DeploymentProgress | null>(null);
  const [loading, setLoading] = useState(true);
  const [progressLoading, setProgressLoading] = useState(false);
  const [pollingId, setPollingId] = useState<number | null>(null);

  // Fetch deployment detail
  const fetchDeployment = async () => {
    if (!id) return;
    setLoading(true);
    try {
      const detail = await getDeployment(id);
      setDeployment(detail);
    } catch {
      message.error("获取部署详情失败");
      navigate("/deployments");
    }
    setLoading(false);
  };

  // Fetch deployment progress
  const fetchProgress = async () => {
    if (!id) return;
    setProgressLoading(true);
    try {
      const progressData = await getDeploymentProgress(id);
      setProgress(progressData);
      // Update deployment status from progress
      if (progressData.status !== "running") {
        stopPolling();
        fetchDeployment();
      }
    } catch {
      // Progress endpoint might not be available
    }
    setProgressLoading(false);
  };

  // Start polling for progress updates
  const startPolling = () => {
    if (pollingId) {
      clearInterval(pollingId);
    }
    const idNum = window.setInterval(() => {
      fetchProgress();
    }, 5000);
    setPollingId(idNum);
  };

  // Stop polling
  const stopPolling = () => {
    if (pollingId) {
      clearInterval(pollingId);
      setPollingId(null);
    }
  };

  // Initial load and polling setup
  useEffect(() => {
    fetchDeployment();
    return () => {
      stopPolling();
    };
  }, [id]);

  // Start polling if deployment is running
  useEffect(() => {
    if (deployment?.status === "running") {
      fetchProgress();
      startPolling();
    }
  }, [deployment?.status]);

  const handleCancel = async () => {
    if (!id) return;
    try {
      await cancelDeployment(id);
      message.success("部署已取消");
      stopPolling();
      fetchDeployment();
    } catch {
      message.error("取消失败");
    }
  };

  const handleRetry = async () => {
    if (!id) return;
    try {
      await retryDeployment(id);
      message.success("已重新执行");
      fetchDeployment();
    } catch {
      message.error("重试失败");
    }
  };

  const renderStageProgress = (stage: DeploymentStageProgress) => {
    const stageStatus = stage.status as DeploymentRecordStatus;
    return (
      <div
        key={stage.id}
        style={{
          marginBottom: 16,
          padding: 16,
          border: "1px solid #f0f0f0",
          borderRadius: 8,
          background: "#fafafa",
        }}
      >
        <div style={{ display: "flex", justifyContent: "space-between", marginBottom: 8 }}>
          <Typography.Text strong>{stage.name}</Typography.Text>
          <Tag color={statusColors[stageStatus]}>{statusLabels[stageStatus]}</Tag>
        </div>
        <Progress
          percent={stage.progress}
          status={
            stageStatus === "running"
              ? "active"
              : stageStatus === "failed"
                ? "exception"
                : "success"
          }
          strokeColor={
            stageStatus === "running"
              ? "#1890ff"
              : stageStatus === "failed"
                ? "#ff4d4f"
                : "#52c41a"
          }
        />
        {stage.message && (
          <Typography.Text type="secondary" style={{ marginTop: 4 }}>
            {stage.message}
          </Typography.Text>
        )}
        {stage.startTime && (
          <Typography.Text type="secondary" style={{ display: "block", marginTop: 8 }}>
            开始时间: {new Date(stage.startTime).toLocaleString()}
          </Typography.Text>
        )}
        {stage.endTime && (
          <Typography.Text type="secondary">
            结束时间: {new Date(stage.endTime).toLocaleString()}
          </Typography.Text>
        )}
        {stage.logs && stage.logs.length > 0 && (
          <div style={{ marginTop: 8 }}>
            <Typography.Text type="secondary" style={{ fontSize: 12 }}>
              日志:
            </Typography.Text>
            <div
              style={{
                background: "#1e1e1e",
                color: "#d4d4d4",
                padding: 8,
                borderRadius: 4,
                marginTop: 4,
                fontFamily: "monospace",
                fontSize: 12,
                maxHeight: 200,
                overflow: "auto",
              }}
            >
              {stage.logs.map((log, idx) => (
                <div key={idx}>{log}</div>
              ))}
            </div>
          </div>
        )}
      </div>
    );
  };

  if (loading) {
    return (
      <Card>
        <Spin size="large" style={{ display: "block", margin: "100px auto" }} />
      </Card>
    );
  }

  if (!deployment) {
    return (
      <Card>
        <Typography.Text>部署不存在</Typography.Text>
      </Card>
    );
  }

  const canCancel = deployment.status === "running" || deployment.status === "pending";
  const canRetry = deployment.status === "failed" || deployment.status === "cancelled";

  return (
    <Card>
      <div style={{ marginBottom: 16 }}>
        <Space>
          <Button icon={<ArrowLeftOutlined />} onClick={() => navigate("/deployments")}>
            返回列表
          </Button>
          {canCancel && (
            <Popconfirm title="确定要取消此部署吗？" onConfirm={handleCancel}>
              <Button danger icon={<StopOutlined />}>
                取消部署
              </Button>
            </Popconfirm>
          )}
          {canRetry && (
            <Button icon={<ReloadOutlined />} onClick={handleRetry}>
              重试部署
            </Button>
          )}
        </Space>
      </div>

      <Row gutter={16} style={{ marginBottom: 24 }}>
        <Col span={6}>
          <Card>
            <Statistic
              title="部署状态"
              value={statusLabels[deployment.status]}
              valueStyle={{
                color:
                  deployment.status === "success"
                    ? "#52c41a"
                    : deployment.status === "failed"
                      ? "#ff4d4f"
                      : deployment.status === "running"
                        ? "#1890ff"
                        : undefined,
              }}
              prefix={
                deployment.status === "success" ? (
                  <CheckCircleOutlined />
                ) : deployment.status === "failed" ? (
                  <CloseCircleOutlined />
                ) : deployment.status === "running" ? (
                  <ClockCircleOutlined />
                ) : (
                  <PlayCircleOutlined />
                )
              }
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="总体进度"
              value={progress?.progress || deployment.progress || 0}
              suffix="%"
              valueStyle={{
                color:
                  deployment.status === "success"
                    ? "#52c41a"
                    : deployment.status === "failed"
                      ? "#ff4d4f"
                      : "#1890ff",
              }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="耗时"
              value={deployment.duration || 0}
              suffix="秒"
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="阶段数"
              value={deployment.stages?.length || 0}
            />
          </Card>
        </Col>
      </Row>

      <Card title="基本信息" style={{ marginBottom: 16 }}>
        <Descriptions column={2} bordered size="small">
          <Descriptions.Item label="部署ID" span={2}>
            <Typography.Text copyable>{deployment.id}</Typography.Text>
          </Descriptions.Item>
          <Descriptions.Item label="应用">
            {deployment.applicationName || deployment.applicationId}
          </Descriptions.Item>
          <Descriptions.Item label="环境">
            {deployment.environmentName || deployment.environmentId}
          </Descriptions.Item>
          <Descriptions.Item label="版本">
            <Typography.Text code>{deployment.version}</Typography.Text>
          </Descriptions.Item>
          <Descriptions.Item label="流水线">
            {deployment.pipelineName || deployment.pipelineId || "-"}
          </Descriptions.Item>
          <Descriptions.Item label="触发类型">
            {deployment.triggerType}
          </Descriptions.Item>
          <Descriptions.Item label="触发人">
            {deployment.triggeredBy}
          </Descriptions.Item>
          <Descriptions.Item label="开始时间">
            {deployment.startTime
              ? new Date(deployment.startTime).toLocaleString()
              : "-"}
          </Descriptions.Item>
          <Descriptions.Item label="结束时间">
            {deployment.endTime
              ? new Date(deployment.endTime).toLocaleString()
              : "-"}
          </Descriptions.Item>
          {deployment.message && (
            <Descriptions.Item label="消息" span={2}>
              {deployment.message}
            </Descriptions.Item>
          )}
        </Descriptions>
      </Card>

      <Card title="部署进度" style={{ marginBottom: 16 }}>
        {progressLoading ? (
          <Spin />
        ) : (
          <>
            <Progress
              percent={progress?.progress || deployment.progress || 0}
              status={
                deployment.status === "running"
                  ? "active"
                  : deployment.status === "failed"
                    ? "exception"
                    : "success"
              }
              style={{ marginBottom: 24 }}
            />
            {progress?.currentStage && (
              <Typography.Text type="secondary" style={{ marginBottom: 16 }}>
                当前阶段: {progress.currentStage}
              </Typography.Text>
            )}
            <div style={{ marginTop: 16 }}>
              {progress?.stages?.map(renderStageProgress) ||
                deployment.stages?.map(renderStageProgress)}
            </div>
          </>
        )}
      </Card>

      {deployment.logs && (
        <Card title="部署日志" style={{ marginBottom: 16 }}>
          <div
            style={{
              background: "#1e1e1e",
              color: "#d4d4d4",
              padding: 16,
              borderRadius: 8,
              fontFamily: "monospace",
              fontSize: 12,
              maxHeight: 400,
              overflow: "auto",
              whiteSpace: "pre-wrap",
            }}
          >
            {deployment.logs}
          </div>
        </Card>
      )}

      {deployment.artifacts && deployment.artifacts.length > 0 && (
        <Card title="构建产物">
          <Timeline
            items={deployment.artifacts.map((artifact) => ({
              color: "blue",
              children: (
                <div>
                  <Typography.Link href={artifact.url} target="_blank">
                    {artifact.name}
                  </Typography.Link>
                  {artifact.size && (
                    <Typography.Text type="secondary">
                      {" "}
                      ({artifact.size} bytes)
                    </Typography.Text>
                  )}
                </div>
              ),
            }))}
          />
        </Card>
      )}
    </Card>
  );
};

export default DeploymentDetailPage;