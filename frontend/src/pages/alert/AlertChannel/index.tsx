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
  message,
  Popconfirm,
  Row,
  Col,
  Divider,
  Typography,
  Result,
} from "antd";
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  CheckCircleOutlined,
  StopOutlined,
  ThunderboltOutlined,
  MailOutlined,
  ApiOutlined,
  MessageOutlined,
  NotificationOutlined,
} from "@ant-design/icons";
import type { ColumnsType } from "antd/es/table";
import { PageContainer } from "@ant-design/pro-components";
import dayjs from "dayjs";
import {
  getAlertChannels,
  getAlertChannel,
  createAlertChannel,
  updateAlertChannel,
  deleteAlertChannel,
  enableAlertChannel,
  disableAlertChannel,
  testAlertChannel,
} from "@/services/alert";
import type {
  AlertChannel,
  AlertChannelType,
  AlertChannelQueryParams,
  AlertChannelCreateParams,
  AlertChannelUpdateParams,
} from "@/types";

const { Option } = Select;
const { TextArea } = Input;
const { Text } = Typography;

const channelTypeConfig: Record<
  AlertChannelType,
  { icon: React.ReactNode; text: string; color: string }
> = {
  EMAIL: { icon: <MailOutlined />, text: "邮件", color: "blue" },
  WEBHOOK: { icon: <ApiOutlined />, text: "Webhook", color: "green" },
  DINGTALK: { icon: <NotificationOutlined />, text: "钉钉", color: "cyan" },
  WECHAT: { icon: <MessageOutlined />, text: "企业微信", color: "geekblue" },
  SLACK: { icon: <ApiOutlined />, text: "Slack", color: "purple" },
};

const AlertChannelPage: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [channels, setChannels] = useState<AlertChannel[]>([]);
  const [total, setTotal] = useState(0);
  const [modalVisible, setModalVisible] = useState(false);
  const [testModalVisible, setTestModalVisible] = useState(false);
  const [testResult, setTestResult] = useState<{
    success: boolean;
    message: string;
  } | null>(null);
  const [editingChannel, setEditingChannel] = useState<AlertChannel | null>(
    null,
  );
  const [form] = Form.useForm();
  const [queryParams, setQueryParams] = useState<AlertChannelQueryParams>({
    page: 1,
    pageSize: 20,
  });

  const fetchChannels = async (params: AlertChannelQueryParams) => {
    setLoading(true);
    try {
      const response = await getAlertChannels(params);
      setChannels(response.data || []);
      setTotal(response.total || 0);
    } catch (error) {
      message.error("获取通知渠道失败");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchChannels(queryParams);
  }, [queryParams]);

  const handleTableChange = (pagination: any) => {
    setQueryParams({
      ...queryParams,
      page: pagination.current,
      pageSize: pagination.pageSize,
    });
  };

  const handleCreate = () => {
    setEditingChannel(null);
    form.resetFields();
    form.setFieldsValue({
      type: "EMAIL",
      status: "ENABLED",
    });
    setModalVisible(true);
  };

  const handleEdit = async (id: string) => {
    try {
      const channel = await getAlertChannel(id);
      setEditingChannel(channel);
      form.setFieldsValue(channel);
      setModalVisible(true);
    } catch (error) {
      message.error("获取渠道详情失败");
    }
  };

  const handleSubmit = async (values: any) => {
    try {
      const params: AlertChannelCreateParams | AlertChannelUpdateParams =
        values;
      if (editingChannel) {
        await updateAlertChannel(editingChannel.id, params);
        message.success("更新成功");
      } else {
        await createAlertChannel(params as AlertChannelCreateParams);
        message.success("创建成功");
      }
      setModalVisible(false);
      fetchChannels(queryParams);
    } catch (error) {
      message.error("操作失败");
    }
  };

  const handleDelete = async (id: string) => {
    try {
      await deleteAlertChannel(id);
      message.success("删除成功");
      fetchChannels(queryParams);
    } catch (error) {
      message.error("删除失败");
    }
  };

  const handleToggleStatus = async (channel: AlertChannel) => {
    try {
      if (channel.status === "ENABLED") {
        await disableAlertChannel(channel.id);
        message.success("已禁用");
      } else {
        await enableAlertChannel(channel.id);
        message.success("已启用");
      }
      fetchChannels(queryParams);
    } catch (error) {
      message.error("操作失败");
    }
  };

  const handleTest = async (id: string) => {
    setTestResult(null);
    setTestModalVisible(true);
    try {
      const result = await testAlertChannel(id);
      setTestResult(result);
    } catch (error: any) {
      setTestResult({ success: false, message: error.message || "测试失败" });
    }
  };

  const renderConfigForm = (type: AlertChannelType) => {
    switch (type) {
      case "EMAIL":
        return (
          <>
            <Form.Item
              name={["config", "smtpHost"]}
              label="SMTP服务器"
              rules={[{ required: true }]}
            >
              <Input placeholder="smtp.example.com" />
            </Form.Item>
            <Row gutter={16}>
              <Col span={12}>
                <Form.Item
                  name={["config", "smtpPort"]}
                  label="端口"
                  rules={[{ required: true }]}
                >
                  <Input placeholder="465" />
                </Form.Item>
              </Col>
              <Col span={12}>
                <Form.Item
                  name={["config", "smtpUser"]}
                  label="用户名"
                  rules={[{ required: true }]}
                >
                  <Input placeholder="user@example.com" />
                </Form.Item>
              </Col>
            </Row>
            <Form.Item
              name={["config", "smtpPassword"]}
              label="密码"
              rules={[{ required: true }]}
            >
              <Input.Password placeholder="SMTP密码" />
            </Form.Item>
            <Form.Item
              name={["config", "from"]}
              label="发件人"
              rules={[{ required: true }]}
            >
              <Input placeholder="noreply@example.com" />
            </Form.Item>
            <Form.Item
              name={["config", "to"]}
              label="收件人(多个用逗号分隔)"
              rules={[{ required: true }]}
            >
              <Input placeholder="admin1@example.com,admin2@example.com" />
            </Form.Item>
          </>
        );
      case "WEBHOOK":
        return (
          <>
            <Form.Item
              name={["config", "url"]}
              label="URL"
              rules={[{ required: true }]}
            >
              <Input placeholder="https://example.com/webhook" />
            </Form.Item>
            <Form.Item name={["config", "method"]} label="请求方法">
              <Select defaultValue="POST">
                <Option value="POST">POST</Option>
                <Option value="GET">GET</Option>
              </Select>
            </Form.Item>
            <Form.Item name={["config", "headers"]} label="请求头(JSON)">
              <TextArea
                rows={3}
                placeholder='{"Authorization": "Bearer token"}'
              />
            </Form.Item>
            <Form.Item
              name={["config", "bodyTemplate"]}
              label="请求体模板(JSON)"
            >
              <TextArea rows={4} placeholder='{"text": "{{alert.title}}"}' />
            </Form.Item>
          </>
        );
      case "DINGTALK":
        return (
          <>
            <Form.Item
              name={["config", "webhook"]}
              label="Webhook URL"
              rules={[{ required: true }]}
            >
              <Input placeholder="https://oapi.dingtalk.com/robot/send?access_token=xxx" />
            </Form.Item>
            <Form.Item name={["config", "secret"]} label="加签密钥">
              <Input.Password placeholder="SECxxx" />
            </Form.Item>
            <Form.Item
              name={["config", "atMobiles"]}
              label="@手机号(多个用逗号分隔)"
            >
              <Input placeholder="13800138000,13900139000" />
            </Form.Item>
            <Form.Item
              name={["config", "atAll"]}
              label="@所有人"
              valuePropName="checked"
            >
              <Tag checkable>是</Tag>
            </Form.Item>
          </>
        );
      case "WECHAT":
        return (
          <>
            <Form.Item
              name={["config", "webhook"]}
              label="Webhook URL"
              rules={[{ required: true }]}
            >
              <Input placeholder="https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=xxx" />
            </Form.Item>
            <Form.Item
              name={["config", "mentionedList"]}
              label="@成员(多个用逗号分隔)"
            >
              <Input placeholder="user1,user2" />
            </Form.Item>
            <Form.Item
              name={["config", "mentionedMobileList"]}
              label="@手机号(多个用逗号分隔)"
            >
              <Input placeholder="13800138000" />
            </Form.Item>
          </>
        );
      case "SLACK":
        return (
          <>
            <Form.Item
              name={["config", "webhook"]}
              label="Webhook URL"
              rules={[{ required: true }]}
            >
              <Input placeholder="https://hooks.slack.com/services/xxx/xxx/xxx" />
            </Form.Item>
            <Form.Item name={["config", "channel"]} label="频道">
              <Input placeholder="#alerts" />
            </Form.Item>
            <Form.Item name={["config", "username"]} label="用户名">
              <Input placeholder="Alert Bot" />
            </Form.Item>
          </>
        );
      default:
        return (
          <Form.Item
            name="config"
            label="配置(JSON)"
            rules={[{ required: true }]}
          >
            <TextArea rows={6} placeholder="请输入JSON格式的配置" />
          </Form.Item>
        );
    }
  };

  const columns: ColumnsType<AlertChannel> = [
    {
      title: "名称",
      dataIndex: "name",
      key: "name",
      width: 150,
    },
    {
      title: "类型",
      dataIndex: "type",
      key: "type",
      width: 120,
      render: (type: AlertChannelType) => (
        <Tag
          icon={channelTypeConfig[type].icon}
          color={channelTypeConfig[type].color}
        >
          {channelTypeConfig[type].text}
        </Tag>
      ),
    },
    {
      title: "描述",
      dataIndex: "description",
      key: "description",
      ellipsis: true,
    },
    {
      title: "状态",
      dataIndex: "status",
      key: "status",
      width: 100,
      render: (status: string) => (
        <Tag color={status === "ENABLED" ? "success" : "default"}>
          {status === "ENABLED" ? "启用" : "禁用"}
        </Tag>
      ),
    },
    {
      title: "最后测试",
      dataIndex: "lastTestAt",
      key: "lastTestAt",
      width: 170,
      render: (time: string, record) =>
        time ? (
          <div>
            <div>{dayjs(time).format("YYYY-MM-DD HH:mm:ss")}</div>
            {record.lastTestResult && (
              <Text
                type={
                  record.lastTestResult === "success" ? "success" : "danger"
                }
                style={{ fontSize: 12 }}
              >
                {record.lastTestResult === "success" ? "成功" : "失败"}
              </Text>
            )}
          </div>
        ) : (
          "-"
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
      width: 220,
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
            icon={<ThunderboltOutlined />}
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
            title="确定删除该渠道吗？"
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

  const selectedType = Form.useWatch("type", form);

  return (
    <PageContainer>
      <Card
        title={
          <Space>
            <NotificationOutlined />
            <span>通知渠道配置</span>
          </Space>
        }
        extra={
          <Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>
            新建渠道
          </Button>
        }
      >
        <Table
          rowKey="id"
          columns={columns}
          dataSource={channels}
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
        title={editingChannel ? "编辑渠道" : "新建渠道"}
        open={modalVisible}
        onCancel={() => setModalVisible(false)}
        onOk={() => form.submit()}
        width={700}
      >
        <Form form={form} layout="vertical" onFinish={handleSubmit}>
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item name="name" label="名称" rules={[{ required: true }]}>
                <Input placeholder="请输入渠道名称" />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item name="type" label="类型" rules={[{ required: true }]}>
                <Select onChange={() => form.setFieldsValue({ config: {} })}>
                  <Option value="EMAIL">
                    <Space>
                      <MailOutlined /> 邮件
                    </Space>
                  </Option>
                  <Option value="WEBHOOK">
                    <Space>
                      <ApiOutlined /> Webhook
                    </Space>
                  </Option>
                  <Option value="DINGTALK">
                    <Space>
                      <NotificationOutlined /> 钉钉
                    </Space>
                  </Option>
                  <Option value="WECHAT">
                    <Space>
                      <MessageOutlined /> 企业微信
                    </Space>
                  </Option>
                  <Option value="SLACK">
                    <Space>
                      <ApiOutlined /> Slack
                    </Space>
                  </Option>
                </Select>
              </Form.Item>
            </Col>
          </Row>
          <Form.Item name="description" label="描述">
            <Input placeholder="渠道描述" />
          </Form.Item>
          <Divider>配置信息</Divider>
          {selectedType && renderConfigForm(selectedType)}
          <Form.Item name="status" label="状态">
            <Select>
              <Option value="ENABLED">启用</Option>
              <Option value="DISABLED">禁用</Option>
            </Select>
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title="测试结果"
        open={testModalVisible}
        onCancel={() => setTestModalVisible(false)}
        footer={[
          <Button key="close" onClick={() => setTestModalVisible(false)}>
            关闭
          </Button>,
        ]}
      >
        {testResult ? (
          <Result
            status={testResult.success ? "success" : "error"}
            title={testResult.success ? "测试成功" : "测试失败"}
            subTitle={testResult.message}
          />
        ) : (
          <div style={{ textAlign: "center", padding: 40 }}>测试中...</div>
        )}
      </Modal>
    </PageContainer>
  );
};

export default AlertChannelPage;
