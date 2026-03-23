import React, { useRef, useState } from "react";
import {
  PlusOutlined,
  PlayCircleOutlined,
  PauseCircleOutlined,
  HistoryOutlined,
  ClockCircleOutlined,
} from "@ant-design/icons";
import type { ActionType, ProColumns } from "@ant-design/pro-components";
import { ProTable, PageContainer } from "@ant-design/pro-components";
import {
  Button,
  message,
  Popconfirm,
  Form,
  Input,
  Select,
  InputNumber,
  Switch,
  Space,
  Tag,
  Tooltip,
  Drawer,
  Descriptions,
  Typography,
  Badge,
} from "antd";
import dayjs from "dayjs";
import {
  getScheduledTasks,
  createScheduledTask,
  updateScheduledTask,
  deleteScheduledTask,
  enableScheduledTask,
  disableScheduledTask,
  triggerScheduledTask,
  getTaskExecutions,
  getTaskExecutionStats,
} from "@/services/scheduledTask";
import type { ScheduledTask, TaskExecution } from "@/types/scheduledTask";
import {
  taskTypeConfig,
  taskStatusConfig,
  executionStatusConfig,
} from "@/types/scheduledTask";

const { Option } = Select;
const { TextArea } = Input;
const { Text } = Typography;

// Cron 表达式帮助提示
const cronHelp = (
  <div style={{ fontSize: 12 }}>
    <p>格式: 秒 分 时 日 月 周</p>
    <p>示例:</p>
    <ul style={{ paddingLeft: 16, margin: 0 }}>
      <li>0 0 * * * ? - 每小时执行</li>
      <li>0 0 0 * * ? - 每天零点执行</li>
      <li>0 0 9-17 * * MON-FRI - 工作日9-17点每小时执行</li>
    </ul>
  </div>
);

const ScheduledTaskList: React.FC = () => {
  const actionRef = useRef<ActionType>();
  const [drawerVisible, setDrawerVisible] = useState(false);
  const [editingItem, setEditingItem] = useState<ScheduledTask | null>(null);
  const [historyDrawerVisible, setHistoryDrawerVisible] = useState(false);
  const [currentTask, setCurrentTask] = useState<ScheduledTask | null>(null);
  const [executions, setExecutions] = useState<TaskExecution[]>([]);
  const [stats, setStats] = useState<{
    totalExecutions: number;
    successCount: number;
    successRate: number;
    avgDuration: number;
  } | null>(null);
  const [form] = Form.useForm();

  // 显示执行历史
  const showExecutionHistory = async (record: ScheduledTask) => {
    setCurrentTask(record);
    try {
      const [execResult, statsResult] = await Promise.all([
        getTaskExecutions(record.id, { limit: 50 }),
        getTaskExecutionStats(record.id),
      ]);
      setExecutions(execResult.data);
      setStats(statsResult);
      setHistoryDrawerVisible(true);
    } catch {
      message.error("获取执行历史失败");
    }
  };

  // 处理新增
  const handleAdd = () => {
    setEditingItem(null);
    form.resetFields();
    setDrawerVisible(true);
  };

  // 处理编辑
  const handleEdit = (record: ScheduledTask) => {
    setEditingItem(record);
    form.setFieldsValue({
      name: record.name,
      type: record.type,
      description: record.description,
      cronExpression: record.cronExpression,
      config: record.config,
      targetId: record.targetId,
      targetType: record.targetType,
      maxRetries: record.maxRetries,
      timeoutSeconds: record.timeoutSeconds,
      alertOnFailure: record.alertOnFailure,
    });
    setDrawerVisible(true);
  };

  // 处理删除
  const handleDelete = async (id: string) => {
    try {
      await deleteScheduledTask(id);
      message.success("删除成功");
      actionRef.current?.reload();
    } catch {
      message.error("删除失败");
    }
  };

  // 处理启用
  const handleEnable = async (id: string) => {
    try {
      await enableScheduledTask(id);
      message.success("已启用");
      actionRef.current?.reload();
    } catch {
      message.error("启用失败");
    }
  };

  // 处理禁用
  const handleDisable = async (id: string) => {
    try {
      await disableScheduledTask(id);
      message.success("已禁用");
      actionRef.current?.reload();
    } catch {
      message.error("禁用失败");
    }
  };

  // 手动触发执行
  const handleTrigger = async (id: string) => {
    try {
      await triggerScheduledTask(id);
      message.success("已触发执行");
    } catch {
      message.error("触发失败");
    }
  };

  // 提交表单
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const handleSubmit = async (values: any) => {
    try {
      if (editingItem) {
        await updateScheduledTask(editingItem.id, values);
        message.success("更新成功");
      } else {
        await createScheduledTask(values);
        message.success("创建成功");
      }
      setDrawerVisible(false);
      actionRef.current?.reload();
    } catch {
      message.error("操作失败");
    }
  };

  const columns: ProColumns<ScheduledTask>[] = [
    {
      title: "任务名称",
      dataIndex: "name",
      key: "name",
      width: 180,
      ellipsis: true,
      render: (_, record) => (
        <Tooltip title={record.description}>
          <span>{record.name}</span>
        </Tooltip>
      ),
    },
    {
      title: "任务类型",
      dataIndex: "type",
      key: "type",
      width: 120,
      valueType: "select",
      valueEnum: Object.fromEntries(
        Object.entries(taskTypeConfig).map(([key, value]) => [
          key,
          { text: value.label },
        ]),
      ),
      render: (_, record) => {
        const config = taskTypeConfig[record.type];
        return <Tag color={config.color}>{config.label}</Tag>;
      },
    },
    {
      title: "Cron 表达式",
      dataIndex: "cronExpression",
      key: "cronExpression",
      width: 150,
      copyable: true,
      render: (_, record) => (
        <Tooltip title={record.cronExpression}>
          <code style={{ fontSize: 12 }}>{record.cronExpression}</code>
        </Tooltip>
      ),
    },
    {
      title: "状态",
      dataIndex: "status",
      key: "status",
      width: 100,
      valueType: "select",
      valueEnum: Object.fromEntries(
        Object.entries(taskStatusConfig).map(([key, value]) => [
          key,
          { text: value.label },
        ]),
      ),
      render: (_, record) => {
        const config = taskStatusConfig[record.status];
        return <Badge status={config.color as any} text={config.label} />;
      },
    },
    {
      title: "最后执行",
      dataIndex: "lastExecuteTime",
      key: "lastExecuteTime",
      width: 160,
      hideInSearch: true,
      render: (_, record) =>
        record.lastExecuteTime
          ? dayjs(record.lastExecuteTime).format("YYYY-MM-DD HH:mm:ss")
          : "-",
    },
    {
      title: "下次执行",
      dataIndex: "nextExecuteTime",
      key: "nextExecuteTime",
      width: 160,
      hideInSearch: true,
      render: (_, record) =>
        record.nextExecuteTime
          ? dayjs(record.nextExecuteTime).format("YYYY-MM-DD HH:mm:ss")
          : "-",
    },
    {
      title: "创建时间",
      dataIndex: "createdAt",
      key: "createdAt",
      width: 160,
      hideInSearch: true,
      sorter: true,
      render: (_, record) =>
        dayjs(record.createdAt).format("YYYY-MM-DD HH:mm:ss"),
    },
    {
      title: "操作",
      key: "action",
      width: 240,
      fixed: "right",
      hideInSearch: true,
      render: (_, record) => (
        <Space size="small">
          <Tooltip title="执行历史">
            <Button
              type="text"
              size="small"
              icon={<HistoryOutlined />}
              onClick={() => showExecutionHistory(record)}
            />
          </Tooltip>
          <Tooltip title="立即执行">
            <Button
              type="text"
              size="small"
              icon={<PlayCircleOutlined />}
              onClick={() => handleTrigger(record.id)}
              disabled={record.status !== "ENABLED"}
            />
          </Tooltip>
          {record.status === "ENABLED" ? (
            <Tooltip title="禁用">
              <Button
                type="text"
                size="small"
                icon={<PauseCircleOutlined />}
                onClick={() => handleDisable(record.id)}
              />
            </Tooltip>
          ) : (
            <Tooltip title="启用">
              <Button
                type="text"
                size="small"
                icon={<PlayCircleOutlined />}
                onClick={() => handleEnable(record.id)}
              />
            </Tooltip>
          )}
          <Button type="link" size="small" onClick={() => handleEdit(record)}>
            编辑
          </Button>
          <Popconfirm
            title="确定删除此任务？"
            onConfirm={() => handleDelete(record.id)}
          >
            <Button type="link" size="small" danger>
              删除
            </Button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  // 执行历史表格列
  const executionColumns: ProColumns<TaskExecution>[] = [
    {
      title: "执行时间",
      dataIndex: "startTime",
      key: "startTime",
      width: 160,
      render: (_, record) =>
        dayjs(record.startTime).format("YYYY-MM-DD HH:mm:ss"),
    },
    {
      title: "状态",
      dataIndex: "status",
      key: "status",
      width: 100,
      render: (_, record) => {
        const config = executionStatusConfig[record.status];
        return <Tag color={config.color}>{config.label}</Tag>;
      },
    },
    {
      title: "耗时",
      dataIndex: "duration",
      key: "duration",
      width: 100,
      render: (_, record) => (record.duration ? `${record.duration}ms` : "-"),
    },
    {
      title: "执行节点",
      dataIndex: "executeNode",
      key: "executeNode",
      width: 120,
      ellipsis: true,
    },
    {
      title: "重试次数",
      dataIndex: "retryCount",
      key: "retryCount",
      width: 80,
      render: (_, record) => record.retryCount || 0,
    },
  ];

  return (
    <PageContainer>
      <ProTable<ScheduledTask>
        actionRef={actionRef}
        columns={columns}
        request={async (params) => {
          const { current, pageSize, ...rest } = params;
          const result = await getScheduledTasks({
            ...rest,
            limit: pageSize,
            offset: ((current || 1) - 1) * (pageSize || 10),
          });
          return {
            data: result.data,
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
            新建任务
          </Button>,
        ]}
        scroll={{ x: 1400 }}
      />

      {/* 新建/编辑任务抽屉 */}
      <Drawer
        title={editingItem ? "编辑任务" : "新建任务"}
        width={600}
        onClose={() => setDrawerVisible(false)}
        open={drawerVisible}
        footer={
          <Space style={{ width: "100%", justifyContent: "flex-end" }}>
            <Button onClick={() => setDrawerVisible(false)}>取消</Button>
            <Button type="primary" onClick={() => form.submit()}>
              确定
            </Button>
          </Space>
        }
      >
        <Form
          form={form}
          layout="vertical"
          onFinish={handleSubmit}
          initialValues={{
            maxRetries: 0,
            timeoutSeconds: 300,
            alertOnFailure: true,
          }}
        >
          <Form.Item
            name="name"
            label="任务名称"
            rules={[{ required: true, message: "请输入任务名称" }]}
          >
            <Input placeholder="请输入任务名称" />
          </Form.Item>
          <Form.Item
            name="type"
            label="任务类型"
            rules={[{ required: true, message: "请选择任务类型" }]}
          >
            <Select placeholder="请选择任务类型" disabled={!!editingItem}>
              {Object.entries(taskTypeConfig).map(([key, value]) => (
                <Option key={key} value={key}>
                  <Tag color={value.color} style={{ marginRight: 4 }}>
                    {value.label}
                  </Tag>
                  <Text type="secondary" style={{ fontSize: 12 }}>
                    {value.description}
                  </Text>
                </Option>
              ))}
            </Select>
          </Form.Item>
          <Form.Item name="description" label="任务描述">
            <TextArea rows={2} placeholder="请输入任务描述" />
          </Form.Item>
          <Form.Item
            name="cronExpression"
            label="Cron 表达式"
            rules={[{ required: true, message: "请输入 Cron 表达式" }]}
            extra={cronHelp}
          >
            <Input placeholder="0 0 0 * * ?" />
          </Form.Item>
          <Form.Item name="config" label="执行配置">
            <TextArea
              rows={4}
              placeholder="根据任务类型填写配置（JSON 格式）"
            />
          </Form.Item>
          <Form.Item name="targetId" label="目标 ID">
            <Input placeholder="目标主机或环境 ID" />
          </Form.Item>
          <Form.Item name="targetType" label="目标类型">
            <Select placeholder="请选择目标类型" allowClear>
              <Option value="HOST">主机</Option>
              <Option value="ENVIRONMENT">环境</Option>
              <Option value="AGENT">Agent</Option>
            </Select>
          </Form.Item>
          <Form.Item name="maxRetries" label="最大重试次数">
            <InputNumber min={0} max={10} style={{ width: "100%" }} />
          </Form.Item>
          <Form.Item name="timeoutSeconds" label="超时时间（秒）">
            <InputNumber min={10} max={3600} style={{ width: "100%" }} />
          </Form.Item>
          <Form.Item
            name="alertOnFailure"
            label="失败时告警"
            valuePropName="checked"
          >
            <Switch />
          </Form.Item>
        </Form>
      </Drawer>

      {/* 执行历史抽屉 */}
      <Drawer
        title={
          <Space>
            <ClockCircleOutlined />
            执行历史
            {currentTask && <Text type="secondary">- {currentTask.name}</Text>}
          </Space>
        }
        width={800}
        onClose={() => setHistoryDrawerVisible(false)}
        open={historyDrawerVisible}
      >
        {stats && (
          <Descriptions
            bordered
            size="small"
            column={4}
            style={{ marginBottom: 16 }}
          >
            <Descriptions.Item label="总执行次数">
              {stats.totalExecutions}
            </Descriptions.Item>
            <Descriptions.Item label="成功次数">
              {stats.successCount}
            </Descriptions.Item>
            <Descriptions.Item label="成功率">
              {(stats.successRate * 100).toFixed(1)}%
            </Descriptions.Item>
            <Descriptions.Item label="平均耗时">
              {stats.avgDuration}ms
            </Descriptions.Item>
          </Descriptions>
        )}
        <ProTable<TaskExecution>
          columns={executionColumns}
          dataSource={executions}
          rowKey="id"
          search={false}
          pagination={{ pageSize: 10 }}
          toolBarRender={false}
          scroll={{ x: 600 }}
        />
      </Drawer>
    </PageContainer>
  );
};

export default ScheduledTaskList;
