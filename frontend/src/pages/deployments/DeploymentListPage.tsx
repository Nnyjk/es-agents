import React, { useState, useRef, useEffect } from "react";
import {
  ProTable,
  ModalForm,
  ProFormSelect,
  ProFormText,
  ProFormTextArea,
} from "@ant-design/pro-components";
import {
  Button,
  Space,
  Tag,
  message,
  Popconfirm,
  Drawer,
  Descriptions,
  Typography,
  Progress,
  Card,
  Row,
  Col,
  Statistic,
  Spin,
} from "antd";
import type { ProColumns, ActionType } from "@ant-design/pro-components";
import {
  PlusOutlined,
  EyeOutlined,
  StopOutlined,
  ReloadOutlined,
  PlayCircleOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  ClockCircleOutlined,
} from "@ant-design/icons";
import {
  getDeployments,
  getDeployment,
  createDeployment,
  cancelDeployment,
  retryDeployment,
  getDeploymentProgress,
} from "@/services/deployment";
import type {
  DeploymentRecord,
  DeploymentRecordStatus,
  DeploymentDetail,
  DeploymentProgress,
  DeploymentStageProgress,
  CreateDeploymentParams,
  DeploymentQueryParams,
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

const DeploymentListPage: React.FC = () => {
  const [detailDrawerVisible, setDetailDrawerVisible] = useState(false);
  const [createModalVisible, setCreateModalVisible] = useState(false);
  const [currentDeployment, setCurrentDeployment] =
    useState<DeploymentDetail | null>(null);
  const [deploymentProgress, setDeploymentProgress] =
    useState<DeploymentProgress | null>(null);
  const [progressLoading, setProgressLoading] = useState(false);
  const [pollingId, setPollingId] = useState<number | null>(null);
  const actionRef = useRef<ActionType>();

  // Cleanup polling on unmount
  useEffect(() => {
    return () => {
      if (pollingId) {
        clearInterval(pollingId);
      }
    };
  }, [pollingId]);

  const handleDetail = async (record: DeploymentRecord) => {
    try {
      const detail = await getDeployment(record.id);
      setCurrentDeployment(detail);
      setDetailDrawerVisible(true);

      // Fetch progress if deployment is running
      if (record.status === "running") {
        fetchProgress(record.id);
        startPolling(record.id);
      } else {
        setDeploymentProgress(null);
      }
    } catch {
      message.error("获取部署详情失败");
    }
  };

  const fetchProgress = async (id: string) => {
    setProgressLoading(true);
    try {
      const progress = await getDeploymentProgress(id);
      setDeploymentProgress(progress);
    } catch {
      // Progress endpoint might not be available
      setDeploymentProgress(null);
    }
    setProgressLoading(false);
  };

  const startPolling = (id: string) => {
    if (pollingId) {
      clearInterval(pollingId);
    }
    const idNum = window.setInterval(() => {
      fetchProgress(id);
    }, 5000);
    setPollingId(idNum);
  };

  const stopPolling = () => {
    if (pollingId) {
      clearInterval(pollingId);
      setPollingId(null);
    }
  };

  const handleCancel = async (record: DeploymentRecord) => {
    try {
      await cancelDeployment(record.id);
      message.success("部署已取消");
      actionRef.current?.reload();
    } catch {
      message.error("取消失败");
    }
  };

  const handleRetry = async (record: DeploymentRecord) => {
    try {
      await retryDeployment(record.id);
      message.success("已重新执行");
      actionRef.current?.reload();
    } catch {
      message.error("重试失败");
    }
  };

  const handleCreate = async (values: CreateDeploymentParams) => {
    try {
      await createDeployment(values);
      message.success("部署已创建");
      setCreateModalVisible(false);
      actionRef.current?.reload();
      return true;
    } catch {
      message.error("创建失败");
      return false;
    }
  };

  const renderStageProgress = (stage: DeploymentStageProgress) => {
    const colors: Record<string, string> = {
      pending: "#d9d9d9",
      running: "#1890ff",
      success: "#52c41a",
      failed: "#ff4d4f",
    };

    return (
      <div
        key={stage.id}
        style={{
          marginBottom: 12,
          padding: 12,
          border: "1px solid #f0f0f0",
          borderRadius: 4,
        }}
      >
        <div style={{ display: "flex", justifyContent: "space-between" }}>
          <span style={{ fontWeight: 500 }}>{stage.name}</span>
          <Tag color={statusColors[stage.status as DeploymentRecordStatus]}>
            {statusLabels[stage.status as DeploymentRecordStatus]}
          </Tag>
        </div>
        <Progress
          percent={stage.progress}
          status={
            stage.status === "running"
              ? "active"
              : stage.status === "failed"
                ? "exception"
                : "success"
          }
          strokeColor={colors[stage.status]}
          style={{ marginTop: 8 }}
        />
        {stage.message && (
          <Typography.Text type="secondary" style={{ marginTop: 4 }}>
            {stage.message}
          </Typography.Text>
        )}
      </div>
    );
  };

  const columns: ProColumns<DeploymentRecord>[] = [
    {
      title: "部署ID",
      dataIndex: "id",
      width: 180,
      render: (_, record) => (
        <Typography.Text copyable style={{ fontSize: 12 }}>
          {record.id}
        </Typography.Text>
      ),
    },
    {
      title: "应用",
      dataIndex: "applicationName",
      width: 150,
      hideInSearch: true,
    },
    {
      title: "环境",
      dataIndex: "environmentName",
      width: 100,
      hideInSearch: true,
    },
    {
      title: "版本",
      dataIndex: "version",
      width: 120,
      render: (text) => <Typography.Text code>{text}</Typography.Text>,
    },
    {
      title: "状态",
      dataIndex: "status",
      width: 100,
      valueType: "select",
      valueEnum: {
        pending: { text: "待执行", status: "Default" },
        running: { text: "执行中", status: "Processing" },
        success: { text: "成功", status: "Success" },
        failed: { text: "失败", status: "Error" },
        cancelled: { text: "已取消", status: "Warning" },
      },
      render: (_, record) => (
        <Space>
          <Tag color={statusColors[record.status]}>
            {statusLabels[record.status]}
          </Tag>
          {record.status === "running" && record.progress !== undefined && (
            <Progress
              percent={record.progress}
              size="small"
              style={{ width: 60 }}
            />
          )}
        </Space>
      ),
    },
    {
      title: "当前阶段",
      dataIndex: "currentStage",
      width: 120,
      hideInSearch: true,
    },
    {
      title: "触发类型",
      dataIndex: "triggerType",
      width: 100,
      hideInSearch: true,
      render: (_, record) => {
        const labels: Record<string, string> = {
          manual: "手动",
          auto: "自动",
          webhook: "Webhook",
          schedule: "定时",
        };
        return labels[record.triggerType] || record.triggerType;
      },
    },
    {
      title: "触发人",
      dataIndex: "triggeredBy",
      width: 100,
    },
    {
      title: "开始时间",
      dataIndex: "startTime",
      width: 160,
      valueType: "dateTime",
      hideInSearch: true,
    },
    {
      title: "耗时",
      dataIndex: "duration",
      width: 100,
      hideInSearch: true,
      render: (_, record) => {
        if (record.duration) {
          const seconds = record.duration;
          if (seconds < 60) return `${seconds}秒`;
          const minutes = Math.floor(seconds / 60);
          const remainingSeconds = seconds % 60;
          return `${minutes}分${remainingSeconds}秒`;
        }
        return "-";
      },
    },
    {
      title: "时间范围",
      valueType: "dateTimeRange",
      dataIndex: "timeRange",
      hideInTable: true,
    },
    {
      title: "操作",
      valueType: "option",
      width: 200,
      render: (_, record) => {
        const canCancel = record.status === "running" || record.status === "pending";
        const canRetry = record.status === "failed" || record.status === "cancelled";

        return (
          <Space size="small">
            <Button
              type="link"
              size="small"
              icon={<EyeOutlined />}
              onClick={() => handleDetail(record)}
            >
              详情
            </Button>
            {canCancel && (
              <Popconfirm
                title="确定要取消此部署吗？"
                onConfirm={() => handleCancel(record)}
              >
                <Button
                  type="link"
                  size="small"
                  danger
                  icon={<StopOutlined />}
                >
                  取消
                </Button>
              </Popconfirm>
            )}
            {canRetry && (
              <Button
                type="link"
                size="small"
                icon={<ReloadOutlined />}
                onClick={() => handleRetry(record)}
              >
                重试
              </Button>
            )}
          </Space>
        );
      },
    },
  ];

  return (
    <>
      <Card style={{ marginBottom: 16 }}>
        <Row gutter={16}>
          <Col span={6}>
            <Statistic
              title="总部署数"
              value={0}
              prefix={<PlayCircleOutlined />}
            />
          </Col>
          <Col span={6}>
            <Statistic
              title="执行中"
              value={0}
              valueStyle={{ color: "#1890ff" }}
              prefix={<ClockCircleOutlined />}
            />
          </Col>
          <Col span={6}>
            <Statistic
              title="成功"
              value={0}
              valueStyle={{ color: "#52c41a" }}
              prefix={<CheckCircleOutlined />}
            />
          </Col>
          <Col span={6}>
            <Statistic
              title="失败"
              value={0}
              valueStyle={{ color: "#ff4d4f" }}
              prefix={<CloseCircleOutlined />}
            />
          </Col>
        </Row>
      </Card>

      <ProTable<DeploymentRecord>
        headerTitle="部署流水线列表"
        columns={columns}
        actionRef={actionRef}
        rowKey="id"
        search={{ labelWidth: "auto" }}
        request={async (params) => {
          const { timeRange, ...restParams } = params;
          const queryParams: DeploymentQueryParams = {
            current: params.current || 1,
            pageSize: params.pageSize || 10,
            ...restParams,
          };
          if (timeRange && timeRange.length === 2) {
            queryParams.startTime = timeRange[0];
            queryParams.endTime = timeRange[1];
          }
          try {
            const result = await getDeployments(queryParams);
            return {
              data: result.list,
              total: result.total,
              success: true,
            };
          } catch {
            message.error("获取部署列表失败");
            return { data: [], success: false, total: 0 };
          }
        }}
        toolBarRender={() => [
          <Button
            key="create"
            type="primary"
            icon={<PlusOutlined />}
            onClick={() => setCreateModalVisible(true)}
          >
            新建部署
          </Button>,
        ]}
        pagination={{
          defaultPageSize: 10,
          showSizeChanger: true,
        }}
      />

      <Drawer
        title="部署详情"
        placement="right"
        width={720}
        onClose={() => {
          setDetailDrawerVisible(false);
          stopPolling();
          setCurrentDeployment(null);
          setDeploymentProgress(null);
        }}
        open={detailDrawerVisible}
      >
        {currentDeployment && (
          <>
            <Descriptions column={2} bordered size="small">
              <Descriptions.Item label="部署ID" span={2}>
                <Typography.Text copyable>{currentDeployment.id}</Typography.Text>
              </Descriptions.Item>
              <Descriptions.Item label="应用">
                {currentDeployment.applicationName || currentDeployment.applicationId}
              </Descriptions.Item>
              <Descriptions.Item label="环境">
                {currentDeployment.environmentName || currentDeployment.environmentId}
              </Descriptions.Item>
              <Descriptions.Item label="版本">
                <Typography.Text code>{currentDeployment.version}</Typography.Text>
              </Descriptions.Item>
              <Descriptions.Item label="状态">
                <Tag color={statusColors[currentDeployment.status]}>
                  {statusLabels[currentDeployment.status]}
                </Tag>
              </Descriptions.Item>
              <Descriptions.Item label="触发类型">
                {currentDeployment.triggerType}
              </Descriptions.Item>
              <Descriptions.Item label="触发人">
                {currentDeployment.triggeredBy}
              </Descriptions.Item>
              <Descriptions.Item label="开始时间">
                {currentDeployment.startTime
                  ? new Date(currentDeployment.startTime).toLocaleString()
                  : "-"}
              </Descriptions.Item>
              <Descriptions.Item label="结束时间">
                {currentDeployment.endTime
                  ? new Date(currentDeployment.endTime).toLocaleString()
                  : "-"}
              </Descriptions.Item>
              {currentDeployment.duration && (
                <Descriptions.Item label="耗时">
                  {currentDeployment.duration}秒
                </Descriptions.Item>
              )}
              {currentDeployment.message && (
                <Descriptions.Item label="消息" span={2}>
                  {currentDeployment.message}
                </Descriptions.Item>
              )}
            </Descriptions>

            <h4 style={{ marginTop: 16, marginBottom: 8 }}>部署进度</h4>
            {progressLoading ? (
              <Spin />
            ) : deploymentProgress ? (
              <div>
                <Progress
                  percent={deploymentProgress.progress}
                  status={
                    deploymentProgress.status === "running"
                      ? "active"
                      : deploymentProgress.status === "failed"
                        ? "exception"
                        : "success"
                  }
                  style={{ marginBottom: 16 }}
                />
                <Typography.Text type="secondary">
                  当前阶段: {deploymentProgress.currentStage}
                </Typography.Text>
                <div style={{ marginTop: 16 }}>
                  {deploymentProgress.stages?.map(renderStageProgress)}
                </div>
              </div>
            ) : (
              currentDeployment.stages?.map(renderStageProgress)
            )}

            {currentDeployment.artifacts && currentDeployment.artifacts.length > 0 && (
              <>
                <h4 style={{ marginTop: 16, marginBottom: 8 }}>构建产物</h4>
                {currentDeployment.artifacts.map((artifact, idx) => (
                  <div key={idx} style={{ marginBottom: 8 }}>
                    <Typography.Link href={artifact.url}>
                      {artifact.name}
                    </Typography.Link>
                    {artifact.size && (
                      <Typography.Text type="secondary">
                        ({artifact.size} bytes)
                      </Typography.Text>
                    )}
                  </div>
                ))}
              </>
            )}
          </>
        )}
      </Drawer>

      <ModalForm<CreateDeploymentParams>
        title="新建部署"
        open={createModalVisible}
        onOpenChange={setCreateModalVisible}
        onFinish={handleCreate}
        modalProps={{ destroyOnClose: true, width: 600 }}
      >
        <ProFormText
          name="applicationId"
          label="应用ID"
          rules={[{ required: true, message: "请输入应用ID" }]}
          placeholder="请输入应用ID"
        />
        <ProFormText
          name="environmentId"
          label="环境ID"
          rules={[{ required: true, message: "请输入环境ID" }]}
          placeholder="请输入环境ID"
        />
        <ProFormText
          name="pipelineId"
          label="流水线ID"
          placeholder="可选，输入流水线ID"
        />
        <ProFormText
          name="version"
          label="版本号"
          rules={[{ required: true, message: "请输入版本号" }]}
          placeholder="请输入要部署的版本号"
        />
        <ProFormSelect
          name="triggerType"
          label="触发类型"
          options={[
            { label: "手动触发", value: "manual" },
            { label: "自动触发", value: "auto" },
          ]}
          initialValue="manual"
        />
        <ProFormTextArea
          name="description"
          label="描述"
          placeholder="部署描述"
          fieldProps={{ rows: 3 }}
        />
      </ModalForm>
    </>
  );
};

export default DeploymentListPage;