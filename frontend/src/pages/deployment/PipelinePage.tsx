import React, { useState, useRef } from "react";
import {
  ProTable,
  ModalForm,
  ProFormText,
  ProFormSelect,
  ProFormTextArea,
} from "@ant-design/pro-components";
import {
  Button,
  Space,
  Tag,
  message,
  Popconfirm,
  Drawer,
  Steps,
  Timeline,
  Descriptions,
  Typography,
  Badge,
  Tooltip,
  Progress,
  Dropdown,
} from "antd";
import type { ProColumns, ActionType } from "@ant-design/pro-components";
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  PlayCircleOutlined,
  CopyOutlined,
  HistoryOutlined,
  EyeOutlined,
  PauseCircleOutlined,
  ReloadOutlined,
  SettingOutlined,
} from "@ant-design/icons";
import {
  getPipelines,
  createPipeline,
  updatePipeline,
  deletePipeline,
  getPipelineExecutions,
  triggerPipeline,
  cancelPipeline,
  retryPipeline,
} from "@/services/deployment";
import type { Pipeline, PipelineExecution, PipelineStage, PipelineStatus } from "@/types/deployment";

const statusColors: Record<PipelineStatus, string> = {
  pending: "default",
  running: "blue",
  success: "green",
  failed: "red",
  cancelled: "orange",
  skipped: "default",
};

const statusLabels: Record<PipelineStatus, string> = {
  pending: "待执行",
  running: "运行中",
  success: "成功",
  failed: "失败",
  cancelled: "已取消",
  skipped: "已跳过",
};

const PipelinePage: React.FC = () => {
  const [modalVisible, setModalVisible] = useState(false);
  const [detailDrawerVisible, setDetailDrawerVisible] = useState(false);
  const [historyDrawerVisible, setHistoryDrawerVisible] = useState(false);
  const [currentPipeline, setCurrentPipeline] = useState<Pipeline | null>(null);
  const [currentExecution, setCurrentExecution] =
    useState<PipelineExecution | null>(null);
  const [executions, setExecutions] = useState<PipelineExecution[]>([]);
  const actionRef = useRef<ActionType>();

  const handleAdd = () => {
    setCurrentPipeline(null);
    setModalVisible(true);
  };

  const handleEdit = (record: Pipeline) => {
    setCurrentPipeline(record);
    setModalVisible(true);
  };

  const handleTrigger = async (record: Pipeline) => {
    try {
      await triggerPipeline(record.id);
      message.success("流水线已触发");
      actionRef.current?.reload();
    } catch {
      message.error("触发失败");
    }
  };

  const handleCancel = async (record: Pipeline) => {
    try {
      await cancelPipeline(record.id);
      message.success("已取消执行");
      actionRef.current?.reload();
    } catch {
      message.error("取消失败");
    }
  };

  const handleRetry = async (record: Pipeline) => {
    try {
      await retryPipeline(record.id);
      message.success("已重新执行");
      actionRef.current?.reload();
    } catch {
      message.error("重试失败");
    }
  };

  const handleDelete = async (id: string) => {
    await deletePipeline(id);
    message.success("删除成功");
    actionRef.current?.reload();
  };

  const handleDetail = async (record: Pipeline) => {
    setCurrentPipeline(record);
    // 获取最新执行记录
    const execs = await getPipelineExecutions(record.id, { current: 1, pageSize: 1 });
    if (execs.list.length > 0) {
      setCurrentExecution(execs.list[0]);
    }
    setDetailDrawerVisible(true);
  };

  const handleHistory = async (record: Pipeline) => {
    setCurrentPipeline(record);
    const execs = await getPipelineExecutions(record.id, { current: 1, pageSize: 50 });
    setExecutions(execs.list);
    setHistoryDrawerVisible(true);
  };

  const handleSubmit = async (values: Record<string, unknown>) => {
    if (currentPipeline) {
      await updatePipeline(currentPipeline.id, values as Parameters<typeof updatePipeline>[1]);
      message.success("更新成功");
    } else {
      await createPipeline(values as Parameters<typeof createPipeline>[0]);
      message.success("创建成功");
    }
    setModalVisible(false);
    actionRef.current?.reload();
    return true;
  };

  const renderStageStatus = (stage: PipelineStage, status?: PipelineStatus) => {
    const stageStatus = status || stage.status;
    return (
      <Tag color={statusColors[stageStatus]}>{statusLabels[stageStatus]}</Tag>
    );
  };

  const columns: ProColumns<Pipeline>[] = [
    {
      title: "流水线名称",
      dataIndex: "name",
      width: 180,
      render: (_, record) => (
        <a onClick={() => handleDetail(record)}>{record.name}</a>
      ),
    },
    {
      title: "关联应用",
      dataIndex: "applicationName",
      width: 150,
      search: false,
    },
    {
      title: "类型",
      dataIndex: "type",
      width: 100,
      valueType: "select",
      valueEnum: {
        build: { text: "构建", status: "Processing" },
        deploy: { text: "部署", status: "Success" },
        "build-deploy": { text: "构建部署", status: "Warning" },
      },
    },
    {
      title: "阶段数",
      dataIndex: "stages",
      width: 100,
      search: false,
      render: (_, record) => record.stages?.length || 0,
    },
    {
      title: "执行状态",
      dataIndex: "status",
      width: 120,
      valueType: "select",
      valueEnum: {
        pending: { text: "待执行", status: "Default" },
        running: { text: "运行中", status: "Processing" },
        success: { text: "成功", status: "Success" },
        failed: { text: "失败", status: "Error" },
      },
      render: (_, record) => (
        <Tag color={statusColors[record.status]}>
          {statusLabels[record.status]}
        </Tag>
      ),
    },
    {
      title: "最后执行",
      dataIndex: "lastExecutionAt",
      width: 160,
      valueType: "dateTime",
      search: false,
    },
    {
      title: "操作",
      key: "action",
      width: 240,
      search: false,
      render: (_, record) => {
        const canTrigger = record.status !== "running";
        const canCancel = record.status === "running";
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
            <Button
              type="link"
              size="small"
              icon={<HistoryOutlined />}
              onClick={() => handleHistory(record)}
            >
              历史
            </Button>
            <Button
              type="link"
              size="small"
              icon={<PlayCircleOutlined />}
              disabled={!canTrigger}
              onClick={() => handleTrigger(record)}
            >
              执行
            </Button>
            {canCancel && (
              <Button
                type="link"
                size="small"
                icon={<PauseCircleOutlined />}
                onClick={() => handleCancel(record)}
              >
                取消
              </Button>
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
            <Button
              type="link"
              size="small"
              icon={<EditOutlined />}
              onClick={() => handleEdit(record)}
            >
              编辑
            </Button>
            <Popconfirm
              title="确定要删除此流水线吗？"
              onConfirm={() => handleDelete(record.id)}
            >
              <Button type="link" size="small" danger icon={<DeleteOutlined />}>
                删除
              </Button>
            </Popconfirm>
          </Space>
        );
      },
    },
  ];

  return (
    <>
      <ProTable<Pipeline>
        columns={columns}
        actionRef={actionRef}
        request={async (params) => {
          const result = await getPipelines({
            current: params.current || 1,
            pageSize: params.pageSize || 10,
            name: params.name,
            type: params.type as Pipeline["type"],
          });
          return {
            data: result.list,
            total: result.total,
            success: true,
          };
        }}
        rowKey="id"
        search={{
          labelWidth: "auto",
        }}
        toolBarRender={() => [
          <Button
            key="add"
            type="primary"
            icon={<PlusOutlined />}
            onClick={handleAdd}
          >
            新建流水线
          </Button>,
        ]}
        pagination={{
          defaultPageSize: 10,
          showSizeChanger: true,
        }}
      />

      <ModalForm
        title={currentPipeline ? "编辑流水线" : "新建流水线"}
        open={modalVisible}
        onFinish={handleSubmit}
        onOpenChange={setModalVisible}
        initialValues={currentPipeline || { type: "build-deploy" }}
        modalProps={{
          destroyOnClose: true,
          width: 600,
        }}
      >
        <ProFormText
          name="name"
          label="流水线名称"
          rules={[{ required: true, message: "请输入流水线名称" }]}
          placeholder="请输入流水线名称"
        />
        <ProFormSelect
          name="applicationId"
          label="关联应用"
          rules={[{ required: true, message: "请选择关联应用" }]}
          placeholder="请选择关联应用"
          request={async () => {
            // 这里应该调用获取应用列表的接口
            return [];
          }}
        />
        <ProFormSelect
          name="type"
          label="流水线类型"
          options={[
            { label: "构建", value: "build" },
            { label: "部署", value: "deploy" },
            { label: "构建部署", value: "build-deploy" },
          ]}
          rules={[{ required: true }]}
        />
        <ProFormSelect
          name="triggerType"
          label="触发方式"
          options={[
            { label: "手动触发", value: "manual" },
            { label: "自动触发", value: "auto" },
            { label: "定时触发", value: "schedule" },
          ]}
          initialValue="manual"
        />
        <ProFormTextArea
          name="description"
          label="描述"
          placeholder="请输入流水线描述"
          fieldProps={{ rows: 3 }}
        />
      </ModalForm>

      <Drawer
        title="流水线详情"
        open={detailDrawerVisible}
        onClose={() => setDetailDrawerVisible(false)}
        width={700}
      >
        {currentPipeline && (
          <>
            <Descriptions column={2} bordered size="small">
              <Descriptions.Item label="流水线名称" span={2}>
                {currentPipeline.name}
              </Descriptions.Item>
              <Descriptions.Item label="类型">
                {currentPipeline.type}
              </Descriptions.Item>
              <Descriptions.Item label="状态">
                <Tag color={statusColors[currentPipeline.status]}>
                  {statusLabels[currentPipeline.status]}
                </Tag>
              </Descriptions.Item>
              <Descriptions.Item label="触发方式">
                {currentPipeline.triggerType}
              </Descriptions.Item>
              <Descriptions.Item label="最后执行">
                {currentPipeline.lastExecutionAt
                  ? new Date(currentPipeline.lastExecutionAt).toLocaleString()
                  : "-"}
              </Descriptions.Item>
            </Descriptions>

            <h4 style={{ marginTop: 16, marginBottom: 8 }}>阶段定义</h4>
            <Steps
              current={-1}
              items={currentPipeline.stages?.map((stage, index) => ({
                title: stage.name,
                status: "wait",
                description: (
                  <div>
                    <div>类型: {stage.type}</div>
                    {stage.timeout && <div>超时: {stage.timeout}秒</div>}
                  </div>
                ),
              }))}
            />

            {currentExecution && (
              <>
                <h4 style={{ marginTop: 16, marginBottom: 8 }}>
                  最新执行 (#{currentExecution.buildNumber})
                </h4>
                <Descriptions column={2} bordered size="small">
                  <Descriptions.Item label="状态">
                    <Tag color={statusColors[currentExecution.status]}>
                      {statusLabels[currentExecution.status]}
                    </Tag>
                  </Descriptions.Item>
                  <Descriptions.Item label="持续时间">
                    {currentExecution.duration
                      ? `${currentExecution.duration}s`
                      : "-"}
                  </Descriptions.Item>
                  <Descriptions.Item label="开始时间">
                    {new Date(currentExecution.startTime).toLocaleString()}
                  </Descriptions.Item>
                  <Descriptions.Item label="结束时间">
                    {currentExecution.endTime
                      ? new Date(currentExecution.endTime).toLocaleString()
                      : "-"}
                  </Descriptions.Item>
                </Descriptions>

                <h4 style={{ marginTop: 16, marginBottom: 8 }}>执行阶段</h4>
                {currentExecution.stages?.map((stage, index) => (
                  <div
                    key={index}
                    style={{
                      marginBottom: 8,
                      padding: 8,
                      border: "1px solid #f0f0f0",
                      borderRadius: 4,
                    }}
                  >
                    <div
                      style={{
                        display: "flex",
                        justifyContent: "space-between",
                        alignItems: "center",
                      }}
                    >
                      <span>
                        <b>{stage.name}</b>
                      </span>
                      {renderStageStatus(stage as PipelineStage, stage.status)}
                    </div>
                    {stage.duration && (
                      <div style={{ color: "#666", fontSize: 12 }}>
                        持续时间: {stage.duration}s
                      </div>
                    )}
                    {stage.steps && stage.steps.length > 0 && (
                      <div style={{ marginTop: 8 }}>
                        <Typography.Text type="secondary">
                          步骤: {stage.steps.join(" → ")}
                        </Typography.Text>
                      </div>
                    )}
                  </div>
                ))}
              </>
            )}
          </>
        )}
      </Drawer>

      <Drawer
        title="执行历史"
        open={historyDrawerVisible}
        onClose={() => setHistoryDrawerVisible(false)}
        width={600}
      >
        <Timeline
          items={executions.map((exec) => ({
            color:
              exec.status === "success"
                ? "green"
                : exec.status === "failed"
                  ? "red"
                  : "blue",
            children: (
              <div>
                <div
                  style={{
                    display: "flex",
                    justifyContent: "space-between",
                    alignItems: "center",
                  }}
                >
                  <span>
                    <b>#{exec.buildNumber}</b>
                  </span>
                  <Tag color={statusColors[exec.status]}>
                    {statusLabels[exec.status]}
                  </Tag>
                </div>
                <div style={{ color: "#666", fontSize: 12 }}>
                  {new Date(exec.startTime).toLocaleString()}
                </div>
                {exec.duration && (
                  <div style={{ color: "#666", fontSize: 12 }}>
                    持续: {exec.duration}s
                  </div>
                )}
              </div>
            ),
          }))}
        />
      </Drawer>
    </>
  );
};

export default PipelinePage;