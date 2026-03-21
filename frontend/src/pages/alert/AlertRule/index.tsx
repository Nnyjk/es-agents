import React, { useState, useEffect } from "react";
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
  InputNumber,
  message,
  Popconfirm,
  Row,
  Col,
} from "antd";
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  CheckCircleOutlined,
  StopOutlined,
  PlayCircleOutlined,
  SettingOutlined,
} from "@ant-design/icons";
import type { ColumnsType } from "antd/es/table";
import { PageContainer } from "@ant-design/pro-components";
import dayjs from "dayjs";
import {
  getAlertRules,
  getAlertRule,
  createAlertRule,
  updateAlertRule,
  deleteAlertRule,
  enableAlertRule,
  disableAlertRule,
  testAlertRule,
  getAlertChannels,
  type AlertRuleQueryParams,
  type AlertRuleCreateParams,
  type AlertRuleUpdateParams,
} from "@/services/alert";
import type {
  AlertRule,
  AlertLevel,
  AlertRuleStatus,
  AlertChannel,
} from "@/types";

const { Option } = Select;
const { TextArea } = Input;

const levelConfig: Record<AlertLevel, { color: string; text: string }> = {
  INFO: { color: "processing", text: "信息" },
  WARNING: { color: "warning", text: "警告" },
  ERROR: { color: "error", text: "错误" },
  CRITICAL: { color: "magenta", text: "严重" },
};

const AlertRulePage: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [rules, setRules] = useState<AlertRule[]>([]);
  const [total, setTotal] = useState(0);
  const [channels, setChannels] = useState<AlertChannel[]>([]);
  const [modalVisible, setModalVisible] = useState(false);
  const [editingRule, setEditingRule] = useState<AlertRule | null>(null);
  const [form] = Form.useForm();
  const [queryParams, setQueryParams] = useState<AlertRuleQueryParams>({
    page: 1,
    pageSize: 20,
  });

  const fetchRules = async (params: AlertRuleQueryParams) => {
    setLoading(true);
    try {
      const response = await getAlertRules(params);
      setRules(response.data || []);
      setTotal(response.total || 0);
    } catch (error) {
      message.error("获取告警规则失败");
    } finally {
      setLoading(false);
    }
  };

  const fetchChannels = async () => {
    try {
      const response = await getAlertChannels({ pageSize: 100 });
      setChannels(response.data || []);
    } catch (error) {
      console.error("获取通知渠道失败", error);
    }
  };

  useEffect(() => {
    fetchRules(queryParams);
    fetchChannels();
  }, [queryParams]);

  const handleTableChange = (pagination: any) => {
    setQueryParams({
      ...queryParams,
      page: pagination.current,
      pageSize: pagination.pageSize,
    });
  };

  const handleCreate = () => {
    setEditingRule(null);
    form.resetFields();
    form.setFieldsValue({
      level: "WARNING",
      duration: 60,
      status: "ENABLED",
    });
    setModalVisible(true);
  };

  const handleEdit = async (id: string) => {
    try {
      const rule = await getAlertRule(id);
      setEditingRule(rule);
      form.setFieldsValue({
        ...rule,
        channelIds: rule.channels?.map((c) => c.id) || rule.channelIds || [],
      });
      setModalVisible(true);
    } catch (error) {
      message.error("获取规则详情失败");
    }
  };

  const handleSubmit = async (values: any) => {
    try {
      const params: AlertRuleCreateParams | AlertRuleUpdateParams = {
        ...values,
        labels: values.labels ? JSON.parse(values.labels) : undefined,
      };
      if (editingRule) {
        await updateAlertRule(editingRule.id, params);
        message.success("更新成功");
      } else {
        await createAlertRule(params as AlertRuleCreateParams);
        message.success("创建成功");
      }
      setModalVisible(false);
      fetchRules(queryParams);
    } catch (error) {
      message.error("操作失败");
    }
  };

  const handleDelete = async (id: string) => {
    try {
      await deleteAlertRule(id);
      message.success("删除成功");
      fetchRules(queryParams);
    } catch (error) {
      message.error("删除失败");
    }
  };

  const handleToggleStatus = async (rule: AlertRule) => {
    try {
      if (rule.status === "ENABLED") {
        await disableAlertRule(rule.id);
        message.success("已禁用");
      } else {
        await enableAlertRule(rule.id);
        message.success("已启用");
      }
      fetchRules(queryParams);
    } catch (error) {
      message.error("操作失败");
    }
  };

  const handleTest = async (id: string) => {
    try {
      const result = await testAlertRule(id);
      if (result.success) {
        message.success(`测试通过，匹配 ${result.matchedCount} 条告警`);
      } else {
        message.warning(result.message);
      }
    } catch (error) {
      message.error("测试失败");
    }
  };

  const columns: ColumnsType<AlertRule> = [
    {
      title: "规则名称",
      dataIndex: "name",
      key: "name",
      width: 200,
    },
    {
      title: "告警级别",
      dataIndex: "level",
      key: "level",
      width: 100,
      render: (level: AlertLevel) => (
        <Tag color={levelConfig[level].color}>{levelConfig[level].text}</Tag>
      ),
    },
    {
      title: "来源",
      dataIndex: "source",
      key: "source",
      width: 120,
      ellipsis: true,
    },
    {
      title: "条件",
      dataIndex: "condition",
      key: "condition",
      ellipsis: true,
      render: (text: string) => (
        <code
          style={{ background: "#f5f5f5", padding: "2px 6px", borderRadius: 4 }}
        >
          {text}
        </code>
      ),
    },
    {
      title: "持续时间",
      dataIndex: "duration",
      key: "duration",
      width: 100,
      render: (duration: number) => `${duration}s`,
    },
    {
      title: "状态",
      dataIndex: "status",
      key: "status",
      width: 100,
      render: (status: AlertRuleStatus) => (
        <Tag color={status === "ENABLED" ? "success" : "default"}>
          {status === "ENABLED" ? "启用" : "禁用"}
        </Tag>
      ),
    },
    {
      title: "通知渠道",
      dataIndex: "channels",
      key: "channels",
      width: 150,
      render: (channels: AlertChannel[]) => (
        <Space size={4}>
          {channels?.slice(0, 2).map((c) => (
            <Tag key={c.id} color="blue">
              {c.name}
            </Tag>
          ))}
          {channels?.length > 2 && <Tag>+{channels.length - 2}</Tag>}
        </Space>
      ),
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
      width: 200,
      render: (_, record) => (
        <Space size="small">
          <Button
            type="link"
            size="small"
            icon={<EditOutlined />}
            onClick={() => handleEdit(record.id)}
          >
            编辑
          </Button>
          <Button
            type="link"
            size="small"
            icon={<PlayCircleOutlined />}
            onClick={() => handleTest(record.id)}
          >
            测试
          </Button>
          <Button
            type="link"
            size="small"
            icon={
              record.status === "ENABLED" ? (
                <StopOutlined />
              ) : (
                <CheckCircleOutlined />
              )
            }
            onClick={() => handleToggleStatus(record)}
          >
            {record.status === "ENABLED" ? "禁用" : "启用"}
          </Button>
          <Popconfirm
            title="确定删除该规则吗？"
            onConfirm={() => handleDelete(record.id)}
          >
            <Button type="link" size="small" danger icon={<DeleteOutlined />}>
              删除
            </Button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <PageContainer>
      <Card
        title={
          <Space>
            <SettingOutlined />
            <span>告警规则配置</span>
          </Space>
        }
        extra={
          <Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>
            新建规则
          </Button>
        }
      >
        <Table
          rowKey="id"
          columns={columns}
          dataSource={rules}
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
        title={editingRule ? "编辑规则" : "新建规则"}
        open={modalVisible}
        onCancel={() => setModalVisible(false)}
        onOk={() => form.submit()}
        width={600}
      >
        <Form form={form} layout="vertical" onFinish={handleSubmit}>
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                name="name"
                label="规则名称"
                rules={[{ required: true }]}
              >
                <Input placeholder="请输入规则名称" />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                name="level"
                label="告警级别"
                rules={[{ required: true }]}
              >
                <Select>
                  <Option value="INFO">信息</Option>
                  <Option value="WARNING">警告</Option>
                  <Option value="ERROR">错误</Option>
                  <Option value="CRITICAL">严重</Option>
                </Select>
              </Form.Item>
            </Col>
          </Row>
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                name="source"
                label="来源"
                rules={[{ required: true }]}
              >
                <Input placeholder="如: agent, deployment, system" />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                name="duration"
                label="持续时间(秒)"
                rules={[{ required: true }]}
              >
                <InputNumber min={0} style={{ width: "100%" }} />
              </Form.Item>
            </Col>
          </Row>
          <Form.Item
            name="condition"
            label="触发条件"
            rules={[{ required: true }]}
          >
            <TextArea rows={3} placeholder="如: cpu_usage > 80" />
          </Form.Item>
          <Form.Item name="description" label="描述">
            <Input placeholder="规则描述" />
          </Form.Item>
          <Form.Item name="channelIds" label="通知渠道">
            <Select mode="multiple" placeholder="选择通知渠道">
              {channels.map((c) => (
                <Option key={c.id} value={c.id}>
                  {c.name} ({c.type})
                </Option>
              ))}
            </Select>
          </Form.Item>
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item name="silencePeriod" label="静默周期(分钟)">
                <InputNumber
                  min={0}
                  style={{ width: "100%" }}
                  placeholder="相同告警的静默时间"
                />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item name="status" label="状态">
                <Select>
                  <Option value="ENABLED">启用</Option>
                  <Option value="DISABLED">禁用</Option>
                </Select>
              </Form.Item>
            </Col>
          </Row>
          <Form.Item name="labels" label="标签(JSON格式)">
            <TextArea
              rows={2}
              placeholder='{"env": "prod", "service": "api"}'
            />
          </Form.Item>
        </Form>
      </Modal>
    </PageContainer>
  );
};

export default AlertRulePage;
