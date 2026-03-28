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
  DatePicker,
  Form,
  Divider,
  Statistic,
  Row,
  Col,
} from "antd";
import {
  SearchOutlined,
  ReloadOutlined,
  EyeOutlined,
  StopOutlined,
  PlayCircleOutlined,
  SettingOutlined,
  HistoryOutlined,
  FileTextOutlined,
  CodeOutlined,
  WarningOutlined,
  InfoCircleOutlined,
  BugOutlined,
} from "@ant-design/icons";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import type { ColumnsType } from "antd/es/table";
import agentMonitoringService from "../../services/agentMonitoring";
import type {
  AgentInstanceRecord,
  AgentTaskRecord,
  LogEntry,
  LogLevel,
  CommandExecutionRecord,
} from "../../types/agentMonitoring";
import { AgentStatusDisplay } from "../../components/agent";
import type { AgentStatus } from "../../components/agent/types";
import dayjs from "dayjs";
import relativeTime from "dayjs/plugin/relativeTime";
import "dayjs/locale/zh-cn";

dayjs.extend(relativeTime);
dayjs.locale("zh-cn");

const { Title, Text, Paragraph } = Typography;
const { Search } = Input;
const { TabPane } = Tabs;
const { RangePicker } = DatePicker;

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
 * 获取日志级别标签
 */
const getLogLevelTag = (level: LogLevel) => {
  const levelConfig: Record<
    LogLevel,
    { color: string; icon: React.ReactNode }
  > = {
    DEBUG: { color: "default", icon: <BugOutlined /> },
    INFO: { color: "blue", icon: <InfoCircleOutlined /> },
    WARN: { color: "orange", icon: <WarningOutlined /> },
    ERROR: { color: "red", icon: <WarningOutlined /> },
  };
  const config = levelConfig[level];
  return (
    <Tag color={config.color} icon={config.icon}>
      {level}
    </Tag>
  );
};

/**
 * 日志查看组件
 */
const LogViewer: React.FC<{ agentId: string }> = ({ agentId }) => {
  const [levelFilter, setLevelFilter] = useState<LogLevel | undefined>();
  const [keyword, setKeyword] = useState("");
  const [timeRange, setTimeRange] = useState<[dayjs.Dayjs, dayjs.Dayjs] | null>(
    null,
  );

  const { data: logStats, isLoading: statsLoading } = useQuery({
    queryKey: ["agentLogStats", agentId],
    queryFn: () => agentMonitoringService.getLogStats(agentId),
    enabled: !!agentId,
  });

  const {
    data: logs,
    isLoading: logsLoading,
    refetch,
  } = useQuery({
    queryKey: ["agentLogs", agentId, levelFilter, keyword, timeRange],
    queryFn: () =>
      agentMonitoringService.getLogs({
        agentId,
        limit: 200,
        level: levelFilter,
        keyword: keyword || undefined,
        startTime: timeRange?.[0]?.toISOString(),
        endTime: timeRange?.[1]?.toISOString(),
      }),
    enabled: !!agentId,
  });

  const logColumns: ColumnsType<LogEntry> = [
    {
      title: "行号",
      dataIndex: "lineNumber",
      key: "lineNumber",
      width: 60,
    },
    {
      title: "时间",
      dataIndex: "timestamp",
      key: "timestamp",
      width: 160,
      render: (time: string) => dayjs(time).format("HH:mm:ss.SSS"),
    },
    {
      title: "级别",
      dataIndex: "level",
      key: "level",
      width: 80,
      render: (level: LogLevel) => getLogLevelTag(level),
    },
    {
      title: "消息",
      dataIndex: "message",
      key: "message",
      ellipsis: true,
      render: (msg: string) => (
        <Paragraph
          style={{ margin: 0, fontFamily: "monospace", fontSize: 12 }}
          ellipsis={{ rows: 2, expandable: true }}
        >
          {msg}
        </Paragraph>
      ),
    },
  ];

  return (
    <Spin spinning={statsLoading || logsLoading}>
      <Space direction="vertical" size="small" style={{ width: "100%" }}>
        {/* 日志统计 */}
        {logStats && (
          <Row gutter={16}>
            <Col span={4}>
              <Statistic title="总条数" value={logStats.totalCount} />
            </Col>
            <Col span={4}>
              <Statistic
                title="错误"
                value={logStats.errorCount}
                valueStyle={{ color: "#cf1322" }}
              />
            </Col>
            <Col span={4}>
              <Statistic
                title="警告"
                value={logStats.warnCount}
                valueStyle={{ color: "#fa8c16" }}
              />
            </Col>
            <Col span={4}>
              <Statistic title="信息" value={logStats.infoCount} />
            </Col>
            <Col span={4}>
              <Statistic title="调试" value={logStats.debugCount} />
            </Col>
          </Row>
        )}

        <Divider style={{ margin: "12px 0" }} />

        {/* 筛选条件 */}
        <Space wrap>
          <Select
            placeholder="日志级别"
            allowClear
            style={{ width: 100 }}
            onChange={setLevelFilter}
            options={[
              { label: "ERROR", value: "ERROR" },
              { label: "WARN", value: "WARN" },
              { label: "INFO", value: "INFO" },
              { label: "DEBUG", value: "DEBUG" },
            ]}
          />
          <Search
            placeholder="搜索关键词"
            allowClear
            onSearch={setKeyword}
            style={{ width: 200 }}
          />
          <RangePicker
            showTime
            size="small"
            onChange={(dates) =>
              setTimeRange(dates as [dayjs.Dayjs, dayjs.Dayjs] | null)
            }
          />
          <Button
            icon={<ReloadOutlined />}
            size="small"
            onClick={() => refetch()}
          >
            刷新
          </Button>
        </Space>

        {/* 日志表格 */}
        <Table
          columns={logColumns}
          dataSource={logs?.logs ?? []}
          rowKey="lineNumber"
          size="small"
          pagination={{
            pageSize: 20,
            showSizeChanger: true,
            showTotal: (total) => `共 ${total} 条`,
          }}
          scroll={{ y: 300 }}
        />
      </Space>
    </Spin>
  );
};

/**
 * 命令执行面板组件
 */
const CommandExecutePanel: React.FC<{ agentId: string }> = ({ agentId }) => {
  const [form] = Form.useForm();
  const [executing, setExecuting] = useState(false);
  const [output, setOutput] = useState<string>("");
  const [error, setError] = useState<string>("");

  const {
    data: history,
    isLoading: historyLoading,
    refetch,
  } = useQuery({
    queryKey: ["commandHistory", agentId],
    queryFn: () =>
      agentMonitoringService.getCommandHistory(agentId, {
        page: 0,
        pageSize: 10,
      }),
    enabled: !!agentId,
  });

  const handleExecute = async () => {
    try {
      const values = await form.validateFields();
      setExecuting(true);
      setOutput("");
      setError("");

      const result = await agentMonitoringService.executeCommand({
        agentId,
        command: values.command,
        args: values.args,
        timeout: values.timeout ? Number(values.timeout) * 1000 : undefined,
      });

      setOutput(result.output || "执行成功，无输出");
      if (result.error) {
        setError(result.error);
      }
      message.success("命令执行完成");
      refetch();
    } catch (err: unknown) {
      const errorMessage = err instanceof Error ? err.message : String(err);
      setError(errorMessage);
      message.error("命令执行失败");
    } finally {
      setExecuting(false);
    }
  };

  const historyColumns: ColumnsType<CommandExecutionRecord> = [
    {
      title: "命令",
      dataIndex: "command",
      key: "command",
      ellipsis: true,
    },
    {
      title: "状态",
      dataIndex: "status",
      key: "status",
      width: 80,
      render: (status: string) => getTaskStatusTag(status),
    },
    {
      title: "执行时间",
      dataIndex: "startedAt",
      key: "startedAt",
      width: 140,
      render: (time: string) =>
        time ? dayjs(time).format("MM-DD HH:mm:ss") : "-",
    },
    {
      title: "耗时",
      dataIndex: "finishedAt",
      key: "duration",
      width: 80,
      render: (finishedAt: string, record: CommandExecutionRecord) => {
        if (!record.startedAt || !finishedAt) return "-";
        return `${dayjs(finishedAt).diff(dayjs(record.startedAt), "s")}s`;
      },
    },
  ];

  return (
    <Spin spinning={historyLoading}>
      <Space direction="vertical" size="small" style={{ width: "100%" }}>
        {/* 命令输入表单 */}
        <Form form={form} layout="inline" size="small">
          <Form.Item
            name="command"
            rules={[{ required: true, message: "请输入命令" }]}
            style={{ flex: 1 }}
          >
            <Input.TextArea
              placeholder="输入要执行的命令..."
              rows={2}
              style={{ fontFamily: "monospace" }}
            />
          </Form.Item>
          <Form.Item name="args">
            <Input placeholder="参数（可选）" style={{ width: 150 }} />
          </Form.Item>
          <Form.Item name="timeout">
            <Input placeholder="超时(秒)" style={{ width: 100 }} />
          </Form.Item>
          <Form.Item>
            <Button
              type="primary"
              icon={<PlayCircleOutlined />}
              loading={executing}
              onClick={handleExecute}
            >
              执行
            </Button>
          </Form.Item>
        </Form>

        {/* 执行结果 */}
        {(output || error) && (
          <Card size="small" title="执行结果">
            {output && (
              <Paragraph
                style={{
                  fontFamily: "monospace",
                  fontSize: 12,
                  background: "#f5f5f5",
                  padding: 8,
                  whiteSpace: "pre-wrap",
                }}
              >
                {output}
              </Paragraph>
            )}
            {error && (
              <Paragraph
                type="danger"
                style={{
                  fontFamily: "monospace",
                  fontSize: 12,
                  background: "#fff2f0",
                  padding: 8,
                }}
              >
                {error}
              </Paragraph>
            )}
          </Card>
        )}

        <Divider style={{ margin: "12px 0" }} />

        {/* 执行历史 */}
        <Title level={5}>执行历史</Title>
        <Table
          columns={historyColumns}
          dataSource={history?.data ?? []}
          rowKey="id"
          size="small"
          pagination={false}
        />
      </Space>
    </Spin>
  );
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
      width={900}
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
                  <AgentStatusDisplay
                    status={instance.status as AgentStatus}
                    mode="tag"
                    showIcon
                  />
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
          <TabPane tab="日志" key="logs" icon={<FileTextOutlined />}>
            {agentId ? (
              <LogViewer agentId={agentId} />
            ) : (
              <Empty description="暂无数据" />
            )}
          </TabPane>
          <TabPane tab="命令执行" key="command" icon={<CodeOutlined />}>
            {agentId ? (
              <CommandExecutePanel agentId={agentId} />
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
      render: (status: string) => (
        <AgentStatusDisplay
          status={status as AgentStatus}
          mode="tag"
          showIcon={false}
          size="small"
        />
      ),
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
