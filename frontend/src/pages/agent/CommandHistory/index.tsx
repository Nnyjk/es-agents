import React, { useState, useEffect } from "react";
import {
  Card,
  Table,
  Button,
  Space,
  Tag,
  Modal,
  Form,
  Select,
  DatePicker,
  message,
  Typography,
  Descriptions,
} from "antd";
import {
  SearchOutlined,
  ReloadOutlined,
  EyeOutlined,
  HistoryOutlined,
  CodeOutlined,
  RedoOutlined,
} from "@ant-design/icons";
import type { ColumnsType } from "antd/es/table";
import { PageContainer } from "@ant-design/pro-components";
import dayjs from "dayjs";
import { queryAgentInstances } from "@/services/agent";
import {
  listCommandExecutions,
  getCommandExecution,
  retryCommandExecution,
} from "@/services/commandExecution";
import type { AgentInstance } from "@/types";
import type {
  CommandExecution,
  ExecutionStatus,
  CommandExecutionQueryParams,
} from "@/types/command";

const { Option } = Select;
const { RangePicker } = DatePicker;
const { Text } = Typography;

const statusConfig: Record<ExecutionStatus, { color: string; text: string }> = {
  PENDING: { color: "default", text: "等待中" },
  RUNNING: { color: "processing", text: "执行中" },
  SUCCESS: { color: "success", text: "成功" },
  FAILED: { color: "error", text: "失败" },
  CANCELLED: { color: "warning", text: "已取消" },
};

const CommandHistory: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [executions, setExecutions] = useState<CommandExecution[]>([]);
  const [total, setTotal] = useState(0);
  const [instances, setInstances] = useState<AgentInstance[]>([]);
  const [selectedExecution, setSelectedExecution] =
    useState<CommandExecution | null>(null);
  const [detailVisible, setDetailVisible] = useState(false);
  const [queryParams, setQueryParams] = useState<CommandExecutionQueryParams>({
    page: 1,
    size: 20,
  });

  useEffect(() => {
    // 加载所有实例用于筛选
    queryAgentInstances().then((res) => {
      const instanceList = Array.isArray(res) ? res : res.data || [];
      setInstances(instanceList);
    });
  }, []);

  useEffect(() => {
    fetchExecutions(queryParams);
  }, [queryParams]);

  const fetchExecutions = async (params: CommandExecutionQueryParams) => {
    setLoading(true);
    try {
      const response = await listCommandExecutions(params);
      setExecutions(response.items || []);
      setTotal(response.total || 0);
    } catch (error) {
      message.error("获取执行历史失败");
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = (values: any) => {
    const params: CommandExecutionQueryParams = {
      ...queryParams,
      ...values,
      page: 1,
      startTime: values.timeRange?.[0]?.format("YYYY-MM-DD HH:mm:ss"),
      endTime: values.timeRange?.[1]?.format("YYYY-MM-DD HH:mm:ss"),
    };
    delete (params as any).timeRange;
    setQueryParams(params);
  };

  const handleReset = () => {
    setQueryParams({ page: 1, size: 20 });
  };

  const handleTableChange = (pagination: any) => {
    setQueryParams({
      ...queryParams,
      page: pagination.current,
      size: pagination.pageSize,
    });
  };

  const handleViewDetail = async (execution: CommandExecution) => {
    try {
      const detail = await getCommandExecution(execution.id);
      setSelectedExecution(detail);
      setDetailVisible(true);
    } catch (error) {
      message.error("获取执行详情失败");
    }
  };

  const handleRetry = async (execution: CommandExecution) => {
    try {
      const result = await retryCommandExecution(execution.id);
      message.success(`重试任务已创建: ${result.executionId}`);
      fetchExecutions(queryParams);
    } catch (error) {
      message.error("重试失败");
    }
  };

  const formatDuration = (execution: CommandExecution) => {
    if (!execution.startedAt || !execution.finishedAt) return "-";
    const ms = dayjs(execution.finishedAt).diff(
      dayjs(execution.startedAt),
      "ms",
    );
    if (ms < 1000) return `${ms}ms`;
    if (ms < 60000) return `${(ms / 1000).toFixed(1)}s`;
    return `${(ms / 60000).toFixed(1)}min`;
  };

  const columns: ColumnsType<CommandExecution> = [
    {
      title: "执行ID",
      dataIndex: "id",
      key: "id",
      width: 280,
      ellipsis: true,
      render: (id: string) => (
        <Text copyable style={{ fontSize: 12 }}>
          {id}
        </Text>
      ),
    },
    {
      title: "Agent 实例",
      dataIndex: "agentInstanceId",
      key: "agentInstanceId",
      width: 150,
      ellipsis: true,
      render: (id: string) => {
        const instance = instances.find((i) => i.id === id);
        return instance?.template?.name || id;
      },
    },
    {
      title: "主机",
      dataIndex: "hostName",
      key: "hostName",
      width: 120,
      ellipsis: true,
      render: (text: string) => text || "-",
    },
    {
      title: "命令",
      dataIndex: "command",
      key: "command",
      width: 200,
      ellipsis: true,
      render: (text: string) => <Tag color="blue">{text || "-"}</Tag>,
    },
    {
      title: "状态",
      dataIndex: "status",
      key: "status",
      width: 100,
      render: (status: ExecutionStatus) => {
        const config = statusConfig[status] || {
          color: "default",
          text: status,
        };
        return <Tag color={config.color}>{config.text}</Tag>;
      },
    },
    {
      title: "耗时",
      key: "duration",
      width: 100,
      render: (_, record) => formatDuration(record),
    },
    {
      title: "创建时间",
      dataIndex: "createdAt",
      key: "createdAt",
      width: 170,
      render: (time: string) => dayjs(time).format("YYYY-MM-DD HH:mm:ss"),
    },
    {
      title: "操作",
      key: "action",
      width: 150,
      render: (_, record) => (
        <Space>
          <Button
            type="link"
            size="small"
            icon={<EyeOutlined />}
            onClick={() => handleViewDetail(record)}
          >
            详情
          </Button>
          {record.status === "FAILED" && (
            <Button
              type="link"
              size="small"
              icon={<RedoOutlined />}
              onClick={() => handleRetry(record)}
            >
              重试
            </Button>
          )}
        </Space>
      ),
    },
  ];

  return (
    <PageContainer>
      <Card style={{ marginBottom: 16 }}>
        <Form layout="inline" onFinish={handleSearch} onReset={handleReset}>
          <Form.Item name="agentInstanceId">
            <Select
              placeholder="Agent 实例"
              style={{ width: 200 }}
              allowClear
              showSearch
              optionFilterProp="children"
            >
              {instances.map((i) => (
                <Option key={i.id} value={i.id}>
                  {i.template?.name || i.id}
                </Option>
              ))}
            </Select>
          </Form.Item>
          <Form.Item name="status">
            <Select placeholder="状态" style={{ width: 120 }} allowClear>
              <Option value="PENDING">等待中</Option>
              <Option value="RUNNING">执行中</Option>
              <Option value="SUCCESS">成功</Option>
              <Option value="FAILED">失败</Option>
              <Option value="CANCELLED">已取消</Option>
            </Select>
          </Form.Item>
          <Form.Item name="timeRange">
            <RangePicker showTime />
          </Form.Item>
          <Form.Item>
            <Space>
              <Button
                type="primary"
                icon={<SearchOutlined />}
                htmlType="submit"
              >
                查询
              </Button>
              <Button htmlType="reset">重置</Button>
            </Space>
          </Form.Item>
        </Form>
      </Card>

      <Card
        title={
          <Space>
            <HistoryOutlined />
            <span>命令执行历史</span>
          </Space>
        }
        extra={
          <Button
            icon={<ReloadOutlined />}
            onClick={() => fetchExecutions(queryParams)}
          >
            刷新
          </Button>
        }
      >
        <Table
          rowKey="id"
          columns={columns}
          dataSource={executions}
          loading={loading}
          pagination={{
            current: queryParams.page,
            pageSize: queryParams.size,
            total,
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (total) => `共 ${total} 条`,
          }}
          onChange={handleTableChange}
        />
      </Card>

      <Modal
        title="执行详情"
        open={detailVisible}
        onCancel={() => setDetailVisible(false)}
        footer={null}
        width={800}
      >
        {selectedExecution && (
          <div>
            <Descriptions column={2} bordered size="small">
              <Descriptions.Item label="执行ID" span={2}>
                <Text copyable style={{ fontSize: 12 }}>
                  {selectedExecution.id}
                </Text>
              </Descriptions.Item>
              <Descriptions.Item label="Agent 实例">
                {selectedExecution.agentInstanceId}
              </Descriptions.Item>
              <Descriptions.Item label="主机">
                {selectedExecution.hostName || "-"}
              </Descriptions.Item>
              <Descriptions.Item label="命令">
                {selectedExecution.command || "-"}
              </Descriptions.Item>
              <Descriptions.Item label="状态">
                <Tag color={statusConfig[selectedExecution.status]?.color}>
                  {statusConfig[selectedExecution.status]?.text ||
                    selectedExecution.status}
                </Tag>
              </Descriptions.Item>
              <Descriptions.Item label="耗时">
                {formatDuration(selectedExecution)}
              </Descriptions.Item>
              <Descriptions.Item label="退出码">
                {selectedExecution.exitCode ?? "-"}
              </Descriptions.Item>
              <Descriptions.Item label="重试次数">
                {selectedExecution.retryCount ?? 0}
              </Descriptions.Item>
              <Descriptions.Item label="执行人">
                {selectedExecution.executedBy}
              </Descriptions.Item>
              <Descriptions.Item label="创建时间">
                {dayjs(selectedExecution.createdAt).format(
                  "YYYY-MM-DD HH:mm:ss",
                )}
              </Descriptions.Item>
              <Descriptions.Item label="开始时间">
                {selectedExecution.startedAt
                  ? dayjs(selectedExecution.startedAt).format(
                      "YYYY-MM-DD HH:mm:ss",
                    )
                  : "-"}
              </Descriptions.Item>
              <Descriptions.Item label="结束时间">
                {selectedExecution.finishedAt
                  ? dayjs(selectedExecution.finishedAt).format(
                      "YYYY-MM-DD HH:mm:ss",
                    )
                  : "-"}
              </Descriptions.Item>
            </Descriptions>

            {selectedExecution.output && (
              <>
                <Divider orientation="left">
                  <CodeOutlined /> 执行输出
                </Divider>
                <div
                  style={{
                    background: "#1e1e1e",
                    color: "#d4d4d4",
                    padding: 16,
                    borderRadius: 4,
                    maxHeight: 400,
                    overflow: "auto",
                    fontFamily: "Consolas, Monaco, monospace",
                    fontSize: 13,
                  }}
                >
                  <pre
                    style={{
                      margin: 0,
                      whiteSpace: "pre-wrap",
                      wordBreak: "break-all",
                    }}
                  >
                    {selectedExecution.output}
                  </pre>
                </div>
              </>
            )}

            {selectedExecution.errorMessage && (
              <>
                <Divider orientation="left">
                  <CodeOutlined /> 错误信息
                </Divider>
                <div
                  style={{
                    background: "#2d1f1f",
                    color: "#f5a6a6",
                    padding: 16,
                    borderRadius: 4,
                    maxHeight: 200,
                    overflow: "auto",
                    fontFamily: "Consolas, Monaco, monospace",
                    fontSize: 13,
                  }}
                >
                  <pre
                    style={{
                      margin: 0,
                      whiteSpace: "pre-wrap",
                      wordBreak: "break-all",
                    }}
                  >
                    {selectedExecution.errorMessage}
                  </pre>
                </div>
              </>
            )}
          </div>
        )}
      </Modal>
    </PageContainer>
  );
};

const Divider: React.FC<{
  orientation?: "left" | "right" | "center";
  children: React.ReactNode;
}> = ({ orientation = "left", children }) => (
  <div
    style={{
      display: "flex",
      alignItems: "center",
      margin: "16px 0",
      color: "#666",
      fontSize: 14,
    }}
  >
    {orientation === "right" && (
      <div
        style={{ flex: 1, height: 1, background: "#e8e8e8", marginRight: 16 }}
      />
    )}
    {orientation === "left" && (
      <div
        style={{ flex: 1, height: 1, background: "#e8e8e8", marginRight: 16 }}
      />
    )}
    {children}
    <div
      style={{ flex: 1, height: 1, background: "#e8e8e8", marginLeft: 16 }}
    />
  </div>
);

export default CommandHistory;
