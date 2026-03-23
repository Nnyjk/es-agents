import React, { useState } from "react";
import {
  Card,
  Table,
  Tag,
  Button,
  Space,
  Input,
  Select,
  Typography,
  Tooltip,
  Modal,
  Descriptions,
  Tabs,
  Empty,
  Spin,
  message,
  Popconfirm,
} from "antd";
import {
  SearchOutlined,
  ReloadOutlined,
  EyeOutlined,
  StopOutlined,
  PlayCircleOutlined,
  SettingOutlined,
  HistoryOutlined,
} from "@ant-design/icons";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import type { ColumnsType } from "antd/es/table";
import agentMonitoringService from "../../services/agentMonitoring";
import type {
  AgentInstanceRecord,
  AgentTaskRecord,
} from "../../types/agentMonitoring";
import dayjs from "dayjs";
import relativeTime from "dayjs/plugin/relativeTime";
import "dayjs/locale/zh-cn";

dayjs.extend(relativeTime);
dayjs.locale("zh-cn");

const { Title, Text } = Typography;
const { Search } = Input;
const { TabPane } = Tabs;

/**
 * 获取 Agent 状态标签
 */
const getAgentStatusTag = (status: string) => {
  const statusConfig: Record<string, { color: string; text: string }> = {
    ONLINE: { color: "success", text: "在线" },
    OFFLINE: { color: "default", text: "离线" },
    ERROR: { color: "error", text: "异常" },
    DEPLOYING: { color: "processing", text: "部署中" },
    DEPLOYED: { color: "success", text: "已部署" },
    PREPARING: { color: "processing", text: "准备中" },
    READY: { color: "success", text: "就绪" },
    PACKAGING: { color: "processing", text: "打包中" },
    PACKAGED: { color: "success", text: "已打包" },
    UNCONFIGURED: { color: "default", text: "未配置" },
  };
  const config = statusConfig[status] || { color: "default", text: status };
  return <Tag color={config.color}>{config.text}</Tag>;
};

/**
 * 获取任务状态标签
 */
const getTaskStatusTag = (status: string) => {
  const statusConfig: Record<string, { color: string; text: string }> = {
    PENDING: { color: "default", text: "等待中" },
    RUNNING: { color: "processing", text: "执行中" },
    SUCCESS: { color: "success", text: "成功" },
    FAILED: { color: "error", text: "失败" },
    CANCELLED: { color: "warning", text: "已取消" },
  };
  const config = statusConfig[status] || { color: "default", text: status };
  return <Tag color={config.color}>{config.text}</Tag>;
};

/**
 * Agent 实例详情模态框
 */
const AgentDetailModal: React.FC<{
  visible: boolean;
  agentId: string | null;
  onClose: () => void;
}> = ({ visible, agentId, onClose }) => {
  const { data: instance, isLoading: instanceLoading } = useQuery({
    queryKey: ["agentInstance", agentId],
    queryFn: () =>
      agentId ? agentMonitoringService.getInstance(agentId) : null,
    enabled: !!agentId,
  });

  const { data: runtimeStatus, isLoading: statusLoading } = useQuery({
    queryKey: ["agentRuntimeStatus", agentId],
    queryFn: () =>
      agentId ? agentMonitoringService.getRuntimeStatus(agentId) : null,
    enabled: !!agentId,
  });

  const { data: tasks, isLoading: tasksLoading } = useQuery({
    queryKey: ["agentTasks", agentId],
    queryFn: () =>
      agentId ? agentMonitoringService.getTaskList(agentId) : null,
    enabled: !!agentId,
  });

  const queryClient = useQueryClient();

  const cancelTaskMutation = useMutation({
    mutationFn: (taskId: string) => {
      if (!agentId) return Promise.reject(new Error("No agent ID"));
      return agentMonitoringService.cancelTask(agentId, taskId);
    },
    onSuccess: () => {
      message.success("任务已取消");
      queryClient.invalidateQueries({ queryKey: ["agentTasks", agentId] });
    },
    onError: () => {
      message.error("取消任务失败");
    },
  });

  const rerunTaskMutation = useMutation({
    mutationFn: (taskId: string) => {
      if (!agentId) return Promise.reject(new Error("No agent ID"));
      return agentMonitoringService.rerunTask(agentId, taskId);
    },
    onSuccess: () => {
      message.success("任务已重新执行");
      queryClient.invalidateQueries({ queryKey: ["agentTasks", agentId] });
    },
    onError: () => {
      message.error("重跑任务失败");
    },
  });

  const taskColumns: ColumnsType<AgentTaskRecord> = [
    {
      title: "命令",
      dataIndex: "commandName",
      key: "commandName",
      width: 150,
    },
    {
      title: "状态",
      dataIndex: "status",
      key: "status",
      width: 100,
      render: (status: string) => getTaskStatusTag(status),
    },
    {
      title: "参数",
      dataIndex: "args",
      key: "args",
      ellipsis: true,
      render: (args: string | null) => args || "-",
    },
    {
      title: "创建时间",
      dataIndex: "createdAt",
      key: "createdAt",
      width: 150,
      render: (time: string) => dayjs(time).format("MM-DD HH:mm:ss"),
    },
    {
      title: "操作",
      key: "action",
      width: 120,
      render: (_: unknown, record: AgentTaskRecord) => (
        <Space size="small">
          {record.status === "RUNNING" && (
            <Popconfirm
              title="确认取消任务？"
              onConfirm={() => cancelTaskMutation.mutate(record.id)}
            >
              <Button type="link" size="small" danger icon={<StopOutlined />}>
                取消
              </Button>
            </Popconfirm>
          )}
          {record.status === "FAILED" && (
            <Popconfirm
              title="确认重新执行？"
              onConfirm={() => rerunTaskMutation.mutate(record.id)}
            >
              <Button type="link" size="small" icon={<PlayCircleOutlined />}>
                重跑
              </Button>
            </Popconfirm>
          )}
        </Space>
      ),
    },
  ];

  return (
    <Modal
      title="Agent 实例详情"
      open={visible}
      onCancel={onClose}
      footer={null}
      width={800}
    >
      <Spin spinning={instanceLoading || statusLoading}>
        <Tabs defaultActiveKey="info">
          <TabPane tab="基础信息" key="info">
            {instance ? (
              <Descriptions bordered column={2}>
                <Descriptions.Item label="实例 ID">
                  <Text copyable>{instance.id}</Text>
                </Descriptions.Item>
                <Descriptions.Item label="状态">
                  {getAgentStatusTag(instance.status)}
                </Descriptions.Item>
                <Descriptions.Item label="主机 ID">
                  <Text copyable>{instance.hostId}</Text>
                </Descriptions.Item>
                <Descriptions.Item label="主机名称">
                  {instance.hostName}
                </Descriptions.Item>
                <Descriptions.Item label="模板 ID">
                  <Text copyable>{instance.templateId}</Text>
                </Descriptions.Item>
                <Descriptions.Item label="模板名称">
                  {instance.templateName}
                </Descriptions.Item>
                <Descriptions.Item label="版本">
                  {instance.version || "-"}
                </Descriptions.Item>
                <Descriptions.Item label="在线状态">
                  {runtimeStatus?.isOnline ? (
                    <Tag color="success">在线</Tag>
                  ) : (
                    <Tag color="default">离线</Tag>
                  )}
                </Descriptions.Item>
                <Descriptions.Item label="最后心跳">
                  {runtimeStatus?.lastHeartbeatTime
                    ? dayjs(runtimeStatus.lastHeartbeatTime).format(
                        "YYYY-MM-DD HH:mm:ss",
                      )
                    : "-"}
                </Descriptions.Item>
                <Descriptions.Item label="心跳延迟">
                  {runtimeStatus?.heartbeatAgeSeconds != null
                    ? `${runtimeStatus.heartbeatAgeSeconds}s`
                    : "-"}
                </Descriptions.Item>
                <Descriptions.Item label="创建时间" span={2}>
                  {dayjs(instance.createdAt).format("YYYY-MM-DD HH:mm:ss")}
                </Descriptions.Item>
                <Descriptions.Item label="更新时间" span={2}>
                  {dayjs(instance.updatedAt).format("YYYY-MM-DD HH:mm:ss")}
                </Descriptions.Item>
              </Descriptions>
            ) : (
              <Empty description="暂无数据" />
            )}
          </TabPane>
          <TabPane tab="任务列表" key="tasks" icon={<HistoryOutlined />}>
            <Table
              columns={taskColumns}
              dataSource={tasks?.data ?? []}
              rowKey="id"
              loading={tasksLoading}
              size="small"
              pagination={{
                pageSize: 5,
                showSizeChanger: false,
              }}
            />
          </TabPane>
        </Tabs>
      </Spin>
    </Modal>
  );
};

/**
 * Agent 实例管理页面
 */
const AgentInstancePage: React.FC = () => {
  const [searchKeyword, setSearchKeyword] = useState("");
  const [statusFilter, setStatusFilter] = useState<string | undefined>();
  const [detailAgentId, setDetailAgentId] = useState<string | null>(null);
  const [selectedRowKeys, setSelectedRowKeys] = useState<React.Key[]>([]);

  const { data, isLoading, refetch } = useQuery({
    queryKey: ["agentInstances", searchKeyword, statusFilter],
    queryFn: () =>
      agentMonitoringService.getInstanceList({
        page: 0,
        pageSize: 20,
      }),
  });

  const columns: ColumnsType<AgentInstanceRecord> = [
    {
      title: "实例 ID",
      dataIndex: "id",
      key: "id",
      width: 280,
      render: (id: string) => (
        <Tooltip title={id}>
          <Text copyable={{ text: id }}>{id.slice(0, 8)}...</Text>
        </Tooltip>
      ),
    },
    {
      title: "主机名称",
      dataIndex: "hostName",
      key: "hostName",
      width: 150,
    },
    {
      title: "模板名称",
      dataIndex: "templateName",
      key: "templateName",
      width: 150,
    },
    {
      title: "状态",
      dataIndex: "status",
      key: "status",
      width: 100,
      render: (status: string) => getAgentStatusTag(status),
    },
    {
      title: "版本",
      dataIndex: "version",
      key: "version",
      width: 100,
      render: (version: string) => version || "-",
    },
    {
      title: "创建时间",
      dataIndex: "createdAt",
      key: "createdAt",
      width: 150,
      render: (time: string) => (
        <Tooltip title={dayjs(time).format("YYYY-MM-DD HH:mm:ss")}>
          {dayjs(time).fromNow()}
        </Tooltip>
      ),
    },
    {
      title: "操作",
      key: "action",
      width: 100,
      render: (_: unknown, record: AgentInstanceRecord) => (
        <Button
          type="link"
          size="small"
          icon={<EyeOutlined />}
          onClick={() => setDetailAgentId(record.id)}
        >
          详情
        </Button>
      ),
    },
  ];

  const filteredData =
    data?.data?.filter((item) => {
      const matchesKeyword =
        !searchKeyword ||
        item.id.toLowerCase().includes(searchKeyword.toLowerCase()) ||
        item.hostName?.toLowerCase().includes(searchKeyword.toLowerCase()) ||
        item.templateName?.toLowerCase().includes(searchKeyword.toLowerCase());
      const matchesStatus = !statusFilter || item.status === statusFilter;
      return matchesKeyword && matchesStatus;
    }) ?? [];

  return (
    <div style={{ padding: 24 }}>
      <Space direction="vertical" size="large" style={{ width: "100%" }}>
        <Title level={4}>
          <SettingOutlined /> Agent 实例管理
        </Title>

        <Card>
          <Space style={{ marginBottom: 16 }}>
            <Search
              placeholder="搜索 ID/主机名/模板名"
              allowClear
              onSearch={setSearchKeyword}
              style={{ width: 250 }}
              prefix={<SearchOutlined />}
            />
            <Select
              placeholder="状态筛选"
              allowClear
              style={{ width: 120 }}
              onChange={setStatusFilter}
              options={[
                { label: "在线", value: "ONLINE" },
                { label: "离线", value: "OFFLINE" },
                { label: "异常", value: "ERROR" },
                { label: "部署中", value: "DEPLOYING" },
                { label: "已部署", value: "DEPLOYED" },
              ]}
            />
            <Button icon={<ReloadOutlined />} onClick={() => refetch()}>
              刷新
            </Button>
          </Space>

          <Table
            columns={columns}
            dataSource={filteredData}
            rowKey="id"
            loading={isLoading}
            rowSelection={{
              selectedRowKeys,
              onChange: setSelectedRowKeys,
            }}
            pagination={{
              showSizeChanger: true,
              showQuickJumper: true,
              showTotal: (total) => `共 ${total} 个 Agent`,
            }}
          />
        </Card>
      </Space>

      <AgentDetailModal
        visible={!!detailAgentId}
        agentId={detailAgentId}
        onClose={() => setDetailAgentId(null)}
      />
    </div>
  );
};

export default AgentInstancePage;
