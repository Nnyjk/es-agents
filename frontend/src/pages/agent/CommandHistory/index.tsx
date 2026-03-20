import React, { useState, useEffect, useRef } from "react";
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
  Tabs,
  Descriptions,
  Row,
  Col,
} from "antd";
import {
  SearchOutlined,
  ReloadOutlined,
  EyeOutlined,
  HistoryOutlined,
  CodeOutlined,
} from "@ant-design/icons";
import type { ColumnsType } from "antd/es/table";
import { PageContainer } from "@ant-design/pro-components";
import dayjs from "dayjs";
import {
  queryAgentTasks,
  getAgentTask,
} from "@/services/agent";
import { queryAgentInstances } from "@/services/agent";
import type {
  AgentTask,
  AgentTaskStatus,
  AgentTaskQueryParams,
  AgentInstance,
} from "@/types";

const { Option } = Select;
const { RangePicker } = DatePicker;
const { Text, Paragraph } = Typography;

const statusConfig: Record<AgentTaskStatus, { color: string; text: string }> = {
  PENDING: { color: "default", text: "等待中" },
  RUNNING: { color: "processing", text: "执行中" },
  SUCCESS: { color: "success", text: "成功" },
  FAILED: { color: "error", text: "失败" },
  CANCELLED: { color: "warning", text: "已取消" },
  TIMEOUT: { color: "magenta", text: "超时" },
};

const CommandHistory: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [tasks, setTasks] = useState<AgentTask[]>([]);
  const [total, setTotal] = useState(0);
  const [instances, setInstances] = useState<AgentInstance[]>([]);
  const [selectedTask, setSelectedTask] = useState<AgentTask | null>(null);
  const [detailVisible, setDetailVisible] = useState(false);
  const [queryParams, setQueryParams] = useState<AgentTaskQueryParams>({
    page: 1,
    pageSize: 20,
  });

  useEffect(() => {
    // 加载所有实例用于筛选
    queryAgentInstances().then((res) => {
      const instanceList = Array.isArray(res) ? res : res.data || [];
      setInstances(instanceList);
    });
  }, []);

  useEffect(() => {
    fetchTasks(queryParams);
  }, [queryParams]);

  const fetchTasks = async (params: AgentTaskQueryParams) => {
    setLoading(true);
    try {
      const response = await queryAgentTasks(params);
      setTasks(response.data || []);
      setTotal(response.total || 0);
    } catch (error) {
      message.error("获取任务列表失败");
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = (values: any) => {
    const params: AgentTaskQueryParams = {
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
    setQueryParams({ page: 1, pageSize: 20 });
  };

  const handleTableChange = (pagination: any) => {
    setQueryParams({
      ...queryParams,
      page: pagination.current,
      pageSize: pagination.pageSize,
    });
  };

  const handleViewDetail = async (task: AgentTask) => {
    try {
      const detail = await getAgentTask(task.id);
      setSelectedTask(detail);
      setDetailVisible(true);
    } catch (error) {
      message.error("获取任务详情失败");
    }
  };

  const formatDuration = (ms?: number) => {
    if (!ms) return "-";
    if (ms < 1000) return `${ms}ms`;
    if (ms < 60000) return `${(ms / 1000).toFixed(1)}s`;
    return `${(ms / 60000).toFixed(1)}min`;
  };

  const columns: ColumnsType<AgentTask> = [
    {
      title: "任务ID",
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
      dataIndex: "agentInstanceName",
      key: "agentInstanceName",
      width: 150,
      ellipsis: true,
    },
    {
      title: "命令",
      dataIndex: "commandName",
      key: "commandName",
      width: 150,
      ellipsis: true,
      render: (text: string) => (
        <Tag color="blue">{text || "-"}</Tag>
      ),
    },
    {
      title: "参数",
      dataIndex: "args",
      key: "args",
      width: 200,
      ellipsis: true,
      render: (text: string) => text || "-",
    },
    {
      title: "状态",
      dataIndex: "status",
      key: "status",
      width: 100,
      render: (status: AgentTaskStatus) => {
        const config = statusConfig[status] || { color: "default", text: status };
        return <Tag color={config.color}>{config.text}</Tag>;
      },
    },
    {
      title: "耗时",
      dataIndex: "durationMs",
      key: "durationMs",
      width: 100,
      render: (ms: number) => formatDuration(ms),
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
      width: 100,
      render: (_, record) => (
        <Button
          type="link"
          size="small"
          icon={<EyeOutlined />}
          onClick={() => handleViewDetail(record)}
        >
          详情
        </Button>
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
              <Option value="TIMEOUT">超时</Option>
            </Select>
          </Form.Item>
          <Form.Item name="timeRange">
            <RangePicker showTime />
          </Form.Item>
          <Form.Item>
            <Space>
              <Button type="primary" icon={<SearchOutlined />} htmlType="submit">
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
            onClick={() => fetchTasks(queryParams)}
          >
            刷新
          </Button>
        }
      >
        <Table
          rowKey="id"
          columns={columns}
          dataSource={tasks}
          loading={loading}
          pagination={{
            current: queryParams.page,
            pageSize: queryParams.pageSize,
            total,
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (total) => `共 ${total} 条`,
          }}
          onChange={handleTableChange}
        />
      </Card>

      <Modal
        title="任务详情"
        open={detailVisible}
        onCancel={() => setDetailVisible(false)}
        footer={null}
        width={800}
      >
        {selectedTask && (
          <div>
            <Descriptions column={2} bordered size="small">
              <Descriptions.Item label="任务ID" span={2}>
                <Text copyable style={{ fontSize: 12 }}>
                  {selectedTask.id}
                </Text>
              </Descriptions.Item>
              <Descriptions.Item label="Agent 实例">
                {selectedTask.agentInstanceName || selectedTask.agentInstanceId}
              </Descriptions.Item>
              <Descriptions.Item label="命令">
                {selectedTask.commandName || "-"}
              </Descriptions.Item>
              <Descriptions.Item label="状态">
                <Tag color={statusConfig[selectedTask.status]?.color}>
                  {statusConfig[selectedTask.status]?.text || selectedTask.status}
                </Tag>
              </Descriptions.Item>
              <Descriptions.Item label="耗时">
                {formatDuration(selectedTask.durationMs)}
              </Descriptions.Item>
              <Descriptions.Item label="创建时间">
                {dayjs(selectedTask.createdAt).format("YYYY-MM-DD HH:mm:ss")}
              </Descriptions.Item>
              <Descriptions.Item label="更新时间">
                {selectedTask.updatedAt
                  ? dayjs(selectedTask.updatedAt).format("YYYY-MM-DD HH:mm:ss")
                  : "-"}
              </Descriptions.Item>
              {selectedTask.args && (
                <Descriptions.Item label="参数" span={2}>
                  <code style={{ background: "#f5f5f5", padding: "2px 6px" }}>
                    {selectedTask.args}
                  </code>
                </Descriptions.Item>
              )}
            </Descriptions>

            {selectedTask.result && (
              <>
                <Divider orientation="left">
                  <CodeOutlined /> 执行结果
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
                  <pre style={{ margin: 0, whiteSpace: "pre-wrap", wordBreak: "break-all" }}>
                    {selectedTask.result}
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

const Divider: React.FC<{ orientation?: "left" | "right" | "center"; children: React.ReactNode }> = ({
  orientation = "left",
  children,
}) => (
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
      <div style={{ flex: 1, height: 1, background: "#e8e8e8", marginRight: 16 }} />
    )}
    {orientation === "left" && (
      <div style={{ flex: 1, height: 1, background: "#e8e8e8", marginRight: 16 }} />
    )}
    {children}
    <div style={{ flex: 1, height: 1, background: "#e8e8e8", marginLeft: 16 }} />
  </div>
);

export default CommandHistory;