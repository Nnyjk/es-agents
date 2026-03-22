import React, { useEffect, useState } from "react";
import {
  Card,
  Table,
  Button,
  Space,
  Tag,
  Modal,
  Form,
  Input,
  Select,
  message,
  Popconfirm,
  Progress,
  Descriptions,
  Tabs,
  Tooltip,
  Badge,
} from "antd";
import {
  PlusOutlined,
  PlayCircleOutlined,
  DeleteOutlined,
  EyeOutlined,
  FileTextOutlined,
  ReloadOutlined,
} from "@ant-design/icons";
import type { ColumnsType } from "antd/es/table";
import dayjs from "dayjs";
import {
  getBaselineTasks,
  getBaselineTemplates,
  createBaselineTask,
  deleteBaselineTask,
  executeBaselineTask,
  getBaselineResult,
  generateBaselineReport,
} from "../../../services/security";
import type {
  BaselineTask,
  BaselineTemplate,
  BaselineResult,
  BaselineCheckItem,
} from "../../../types/security";

const { TextArea } = Input;

const BaselineCheck: React.FC = () => {
  const [tasks, setTasks] = useState<BaselineTask[]>([]);
  const [templates, setTemplates] = useState<BaselineTemplate[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [resultModalVisible, setResultModalVisible] = useState(false);
  const [currentResult, setCurrentResult] = useState<BaselineResult | null>(
    null,
  );
  const [executingTask, setExecutingTask] = useState<string | null>(null);
  const [form] = Form.useForm();
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 10,
    total: 0,
  });

  useEffect(() => {
    fetchTasks();
    fetchTemplates();
  }, []);

  const fetchTasks = async (page = 1, pageSize = 10) => {
    try {
      setLoading(true);
      const response = await getBaselineTasks({ page, pageSize });
      setTasks(response.data);
      setPagination((prev) => ({
        ...prev,
        current: page,
        pageSize,
        total: response.total,
      }));
    } catch {
      message.error("获取基线检查任务列表失败");
    } finally {
      setLoading(false);
    }
  };

  const fetchTemplates = async () => {
    try {
      const data = await getBaselineTemplates();
      setTemplates(data);
    } catch {
      setTemplates([
        {
          id: "1",
          name: "等保2.0三级-主机安全基线",
          level: "level3",
          category: "host",
          itemCount: 85,
          isBuiltIn: true,
          createdAt: "2024-01-01T00:00:00Z",
          updatedAt: "2024-01-01T00:00:00Z",
        },
        {
          id: "2",
          name: "等保2.0三级-数据库安全基线",
          level: "level3",
          category: "database",
          itemCount: 62,
          isBuiltIn: true,
          createdAt: "2024-01-01T00:00:00Z",
          updatedAt: "2024-01-01T00:00:00Z",
        },
      ]);
    }
  };

  const handleCreate = async (values: {
    name: string;
    description?: string;
    templateId: string;
    targetType: string;
    targetIds: string[];
  }) => {
    try {
      await createBaselineTask({
        name: values.name,
        description: values.description,
        templateId: values.templateId,
        targetType: values.targetType as
          | "host"
          | "database"
          | "application"
          | "network",
        targetIds: values.targetIds,
      });
      message.success("创建成功");
      setModalVisible(false);
      form.resetFields();
      fetchTasks(pagination.current, pagination.pageSize);
    } catch {
      message.error("创建失败");
    }
  };

  const handleExecute = async (taskId: string) => {
    try {
      setExecutingTask(taskId);
      message.info("开始执行基线检查...");
      const result = await executeBaselineTask(taskId);
      message.success("执行完成");
      setCurrentResult(result);
      setResultModalVisible(true);
      fetchTasks(pagination.current, pagination.pageSize);
    } catch {
      message.error("执行失败");
    } finally {
      setExecutingTask(null);
    }
  };

  const handleViewResult = async (taskId: string) => {
    try {
      setLoading(true);
      const result = await getBaselineResult(taskId);
      setCurrentResult(result);
      setResultModalVisible(true);
    } catch {
      message.error("获取检查结果失败");
    } finally {
      setLoading(false);
    }
  };

  const handleGenerateReport = async (
    taskId: string,
    format: "pdf" | "word" | "html",
  ) => {
    try {
      const report = await generateBaselineReport(taskId, format);
      if (report.downloadUrl) {
        window.open(report.downloadUrl, "_blank");
      }
      message.success("报告生成成功");
    } catch {
      message.error("报告生成失败");
    }
  };

  const handleDelete = async (id: string) => {
    try {
      await deleteBaselineTask(id);
      message.success("删除成功");
      fetchTasks(pagination.current, pagination.pageSize);
    } catch {
      message.error("删除失败");
    }
  };

  const getStatusTag = (status: string) => {
    const map: Record<string, { color: string; text: string }> = {
      pending: { color: "default", text: "待执行" },
      running: { color: "processing", text: "执行中" },
      completed: { color: "success", text: "已完成" },
      failed: { color: "error", text: "失败" },
      cancelled: { color: "warning", text: "已取消" },
    };
    const item = map[status] || { color: "default", text: status };
    return <Tag color={item.color}>{item.text}</Tag>;
  };

  const getSeverityTag = (severity: string) => {
    const map: Record<string, { color: string; text: string }> = {
      critical: { color: "red", text: "严重" },
      high: { color: "orange", text: "高" },
      medium: { color: "gold", text: "中" },
      low: { color: "blue", text: "低" },
    };
    const item = map[severity] || { color: "default", text: severity };
    return <Tag color={item.color}>{item.text}</Tag>;
  };

  const getComplianceTag = (status: string) => {
    const map: Record<string, { color: string; text: string }> = {
      compliant: { color: "success", text: "合规" },
      "non-compliant": { color: "error", text: "不合规" },
      "not-applicable": { color: "default", text: "不适用" },
      error: { color: "warning", text: "检查失败" },
    };
    const item = map[status] || { color: "default", text: status };
    return <Tag color={item.color}>{item.text}</Tag>;
  };

  const columns: ColumnsType<BaselineTask> = [
    {
      title: "任务名称",
      dataIndex: "name",
      key: "name",
      width: 200,
    },
    {
      title: "基线模板",
      dataIndex: "templateName",
      key: "templateName",
      width: 180,
    },
    {
      title: "目标类型",
      dataIndex: "targetType",
      key: "targetType",
      width: 100,
      render: (type: string) => {
        const map: Record<string, string> = {
          host: "主机",
          database: "数据库",
          application: "应用",
          network: "网络",
        };
        return map[type] || type;
      },
    },
    {
      title: "状态",
      dataIndex: "status",
      key: "status",
      width: 100,
      render: (status: string) => getStatusTag(status),
    },
    {
      title: "上次执行",
      dataIndex: "lastRunTime",
      key: "lastRunTime",
      width: 160,
      render: (time: string) =>
        time ? dayjs(time).format("YYYY-MM-DD HH:mm") : "-",
    },
    {
      title: "创建时间",
      dataIndex: "createdAt",
      key: "createdAt",
      width: 160,
      render: (time: string) => dayjs(time).format("YYYY-MM-DD HH:mm"),
    },
    {
      title: "操作",
      key: "action",
      width: 200,
      render: (_: unknown, record: BaselineTask) => (
        <Space size="small">
          <Tooltip title="执行检查">
            <Button
              type="link"
              size="small"
              icon={<PlayCircleOutlined />}
              loading={executingTask === record.id}
              onClick={() => handleExecute(record.id)}
              disabled={record.status === "running"}
            />
          </Tooltip>
          <Tooltip title="查看结果">
            <Button
              type="link"
              size="small"
              icon={<EyeOutlined />}
              onClick={() => handleViewResult(record.id)}
              disabled={!record.lastRunTime}
            />
          </Tooltip>
          <Tooltip title="生成报告">
            <Button
              type="link"
              size="small"
              icon={<FileTextOutlined />}
              onClick={() => handleGenerateReport(record.id, "pdf")}
              disabled={!record.lastRunTime}
            />
          </Tooltip>
          <Popconfirm
            title="确定删除此任务吗？"
            onConfirm={() => handleDelete(record.id)}
          >
            <Button
              type="link"
              size="small"
              danger
              icon={<DeleteOutlined />}
            />
          </Popconfirm>
        </Space>
      ),
    },
  ];

  const resultColumns: ColumnsType<BaselineCheckItem> = [
    {
      title: "检查项",
      dataIndex: "name",
      key: "name",
      width: 200,
    },
    {
      title: "分类",
      dataIndex: "category",
      key: "category",
      width: 100,
    },
    {
      title: "严重程度",
      dataIndex: "severity",
      key: "severity",
      width: 80,
      render: (severity: string) => getSeverityTag(severity),
    },
    {
      title: "状态",
      dataIndex: "status",
      key: "status",
      width: 100,
      render: (status: string) => getComplianceTag(status),
    },
    {
      title: "期望值",
      dataIndex: "expectedValue",
      key: "expectedValue",
      width: 150,
      ellipsis: true,
    },
    {
      title: "实际值",
      dataIndex: "actualValue",
      key: "actualValue",
      width: 150,
      ellipsis: true,
    },
    {
      title: "修复建议",
      dataIndex: "remediation",
      key: "remediation",
      ellipsis: true,
    },
  ];

  return (
    <div>
      <Card
        title="基线检查任务"
        extra={
          <Space>
            <Button icon={<ReloadOutlined />} onClick={() => fetchTasks()}>
              刷新
            </Button>
            <Button
              type="primary"
              icon={<PlusOutlined />}
              onClick={() => setModalVisible(true)}
            >
              新建任务
            </Button>
          </Space>
        }
      >
        <Table
          columns={columns}
          dataSource={tasks}
          rowKey="id"
          loading={loading}
          pagination={{
            ...pagination,
            showSizeChanger: true,
            showQuickJumper: true,
            onChange: (page, pageSize) => fetchTasks(page, pageSize),
          }}
        />
      </Card>

      <Modal
        title="新建基线检查任务"
        open={modalVisible}
        onCancel={() => setModalVisible(false)}
        onOk={() => form.submit()}
        width={600}
      >
        <Form form={form} layout="vertical" onFinish={handleCreate}>
          <Form.Item
            name="name"
            label="任务名称"
            rules={[{ required: true, message: "请输入任务名称" }]}
          >
            <Input placeholder="请输入任务名称" />
          </Form.Item>
          <Form.Item name="description" label="任务描述">
            <TextArea rows={3} placeholder="请输入任务描述" />
          </Form.Item>
          <Form.Item
            name="templateId"
            label="基线模板"
            rules={[{ required: true, message: "请选择基线模板" }]}
          >
            <Select placeholder="请选择基线模板">
              {templates.map((t) => (
                <Select.Option key={t.id} value={t.id}>
                  {t.name} ({t.itemCount} 项)
                </Select.Option>
              ))}
            </Select>
          </Form.Item>
          <Form.Item
            name="targetType"
            label="目标类型"
            rules={[{ required: true, message: "请选择目标类型" }]}
          >
            <Select placeholder="请选择目标类型">
              <Select.Option value="host">主机</Select.Option>
              <Select.Option value="database">数据库</Select.Option>
              <Select.Option value="application">应用</Select.Option>
              <Select.Option value="network">网络</Select.Option>
            </Select>
          </Form.Item>
          <Form.Item
            name="targetIds"
            label="检查目标"
            rules={[{ required: true, message: "请选择检查目标" }]}
          >
            <Select mode="multiple" placeholder="请选择检查目标">
              <Select.Option value="host-1">
                主机-1 (192.168.1.10)
              </Select.Option>
              <Select.Option value="host-2">
                主机-2 (192.168.1.11)
              </Select.Option>
              <Select.Option value="db-1">数据库-1 (MySQL)</Select.Option>
            </Select>
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title="检查结果详情"
        open={resultModalVisible}
        onCancel={() => setResultModalVisible(false)}
        footer={null}
        width={1000}
      >
        {currentResult && (
          <div>
            <Descriptions bordered column={4} style={{ marginBottom: 16 }}>
              <Descriptions.Item label="任务名称">
                {currentResult.taskName}
              </Descriptions.Item>
              <Descriptions.Item label="总检查项">
                {currentResult.totalCount}
              </Descriptions.Item>
              <Descriptions.Item label="合规项">
                <Badge status="success" text={currentResult.compliantCount} />
              </Descriptions.Item>
              <Descriptions.Item label="不合规项">
                <Badge
                  status="error"
                  text={currentResult.nonCompliantCount}
                />
              </Descriptions.Item>
              <Descriptions.Item label="合规率" span={2}>
                <Progress
                  percent={currentResult.complianceRate}
                  status={
                    currentResult.complianceRate >= 80
                      ? "success"
                      : currentResult.complianceRate >= 60
                        ? "normal"
                        : "exception"
                  }
                />
              </Descriptions.Item>
              <Descriptions.Item label="检查耗时">
                {currentResult.duration ? `${currentResult.duration} 秒` : "-"}
              </Descriptions.Item>
              <Descriptions.Item label="检查状态">
                {getStatusTag(currentResult.status)}
              </Descriptions.Item>
            </Descriptions>

            <Tabs
              items={[
                {
                  key: "all",
                  label: "全部检查项",
                  children: (
                    <Table
                      columns={resultColumns}
                      dataSource={currentResult.items}
                      rowKey="id"
                      pagination={{ pageSize: 10 }}
                    />
                  ),
                },
                {
                  key: "non-compliant",
                  label: `不合规项 (${currentResult.nonCompliantCount})`,
                  children: (
                    <Table
                      columns={resultColumns}
                      dataSource={currentResult.items.filter(
                        (item) => item.status === "non-compliant",
                      )}
                      rowKey="id"
                      pagination={{ pageSize: 10 }}
                    />
                  ),
                },
              ]}
            />
          </div>
        )}
      </Modal>
    </div>
  );
};

export default BaselineCheck;