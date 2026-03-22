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
  DatePicker,
  message,
  Popconfirm,
  Typography,
} from "antd";
import {
  PlusOutlined,
  DeleteOutlined,
  StopOutlined,
  EyeOutlined,
} from "@ant-design/icons";
import type { ColumnsType } from "antd/es/table";
import dayjs from "dayjs";
import {
  getMyApiTokens,
  createMyApiToken,
  revokeMyApiToken,
  deleteMyApiToken,
  getTokenAccessLogs,
} from "../../../services/userProfile";
import type { ApiToken, TokenAccessLog } from "../../../types/userProfile";

const { Text, Paragraph } = Typography;

const ApiTokenList: React.FC = () => {
  const [tokens, setTokens] = useState<ApiToken[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [logsModalVisible, setLogsModalVisible] = useState(false);
  const [accessLogs, setAccessLogs] = useState<TokenAccessLog[]>([]);
  const [logsLoading, setLogsLoading] = useState(false);
  const [createdToken, setCreatedToken] = useState<string>("");
  const [showTokenModal, setShowTokenModal] = useState(false);
  const [form] = Form.useForm();

  useEffect(() => {
    fetchTokens();
  }, []);

  const fetchTokens = async () => {
    try {
      setLoading(true);
      const data = await getMyApiTokens();
      setTokens(data);
    } catch {
      message.error("获取令牌列表失败");
    } finally {
      setLoading(false);
    }
  };

  const handleCreate = async (values: {
    name: string;
    description?: string;
    scopes: string[];
    expiresAt?: dayjs.Dayjs;
  }) => {
    try {
      const result = await createMyApiToken({
        name: values.name,
        description: values.description,
        scopes: values.scopes,
        expiresAt: values.expiresAt?.toISOString(),
      });
      message.success("令牌创建成功");
      setCreatedToken(result.token);
      setShowTokenModal(true);
      setModalVisible(false);
      form.resetFields();
      fetchTokens();
    } catch {
      message.error("创建令牌失败");
    }
  };

  const handleRevoke = async (id: string) => {
    try {
      await revokeMyApiToken(id);
      message.success("令牌已撤销");
      fetchTokens();
    } catch {
      message.error("撤销失败");
    }
  };

  const handleDelete = async (id: string) => {
    try {
      await deleteMyApiToken(id);
      message.success("令牌已删除");
      fetchTokens();
    } catch {
      message.error("删除失败");
    }
  };

  const handleViewLogs = async (tokenId: string) => {
    try {
      setLogsLoading(true);
      setLogsModalVisible(true);
      const result = await getTokenAccessLogs(tokenId);
      setAccessLogs(result.list);
    } catch {
      message.error("获取访问日志失败");
    } finally {
      setLogsLoading(false);
    }
  };

  const getStatusTag = (status: string) => {
    const statusMap: Record<string, { color: string; text: string }> = {
      ACTIVE: { color: "green", text: "有效" },
      REVOKED: { color: "red", text: "已撤销" },
      EXPIRED: { color: "default", text: "已过期" },
    };
    const config = statusMap[status] || { color: "default", text: status };
    return <Tag color={config.color}>{config.text}</Tag>;
  };

  const columns: ColumnsType<ApiToken> = [
    {
      title: "名称",
      dataIndex: "name",
      key: "name",
    },
    {
      title: "描述",
      dataIndex: "description",
      key: "description",
      render: (text) => text || "-",
    },
    {
      title: "权限范围",
      dataIndex: "scopes",
      key: "scopes",
      render: (scopes: string[]) => (
        <Space>
          {scopes.map((scope) => (
            <Tag key={scope}>{scope}</Tag>
          ))}
        </Space>
      ),
    },
    {
      title: "状态",
      dataIndex: "status",
      key: "status",
      render: (status: string) => getStatusTag(status),
    },
    {
      title: "过期时间",
      dataIndex: "expiresAt",
      key: "expiresAt",
      render: (value: string) =>
        value ? new Date(value).toLocaleString() : "永不过期",
    },
    {
      title: "创建时间",
      dataIndex: "createdAt",
      key: "createdAt",
      render: (value: string) => new Date(value).toLocaleString(),
    },
    {
      title: "最后使用",
      dataIndex: "lastUsedAt",
      key: "lastUsedAt",
      render: (value: string) =>
        value ? new Date(value).toLocaleString() : "从未使用",
    },
    {
      title: "操作",
      key: "actions",
      render: (_, record) => (
        <Space>
          <Button
            type="link"
            size="small"
            icon={<EyeOutlined />}
            onClick={() => handleViewLogs(record.id)}
          >
            日志
          </Button>
          {record.status === "ACTIVE" && (
            <Popconfirm
              title="确定要撤销此令牌吗？"
              onConfirm={() => handleRevoke(record.id)}
            >
              <Button type="link" size="small" danger icon={<StopOutlined />}>
                撤销
              </Button>
            </Popconfirm>
          )}
          {record.status !== "ACTIVE" && (
            <Popconfirm
              title="确定要删除此令牌吗？"
              onConfirm={() => handleDelete(record.id)}
            >
              <Button type="link" size="small" danger icon={<DeleteOutlined />}>
                删除
              </Button>
            </Popconfirm>
          )}
        </Space>
      ),
    },
  ];

  const logColumns = [
    {
      title: "访问时间",
      dataIndex: "accessedAt",
      key: "accessedAt",
      render: (value: string) => new Date(value).toLocaleString(),
    },
    {
      title: "IP地址",
      dataIndex: "ip",
      key: "ip",
    },
    {
      title: "请求方法",
      dataIndex: "method",
      key: "method",
    },
    {
      title: "请求路径",
      dataIndex: "endpoint",
      key: "endpoint",
    },
    {
      title: "状态码",
      dataIndex: "statusCode",
      key: "statusCode",
      render: (code: number) => (
        <Tag color={code < 400 ? "green" : "red"}>{code}</Tag>
      ),
    },
  ];

  const scopeOptions = [
    { label: "读取", value: "read" },
    { label: "写入", value: "write" },
    { label: "删除", value: "delete" },
    { label: "管理", value: "admin" },
  ];

  return (
    <div>
      <Card
        title="API令牌"
        extra={
          <Button
            type="primary"
            icon={<PlusOutlined />}
            onClick={() => setModalVisible(true)}
          >
            创建令牌
          </Button>
        }
      >
        <Table
          columns={columns}
          dataSource={tokens}
          rowKey="id"
          loading={loading}
          pagination={{ pageSize: 10 }}
        />
      </Card>

      {/* 创建令牌弹窗 */}
      <Modal
        title="创建API令牌"
        open={modalVisible}
        onCancel={() => setModalVisible(false)}
        footer={null}
      >
        <Form form={form} layout="vertical" onFinish={handleCreate}>
          <Form.Item
            name="name"
            label="令牌名称"
            rules={[{ required: true, message: "请输入令牌名称" }]}
          >
            <Input placeholder="请输入令牌名称" />
          </Form.Item>
          <Form.Item name="description" label="描述">
            <Input.TextArea placeholder="请输入描述（可选）" rows={3} />
          </Form.Item>
          <Form.Item
            name="scopes"
            label="权限范围"
            rules={[{ required: true, message: "请选择权限范围" }]}
          >
            <Select
              mode="multiple"
              placeholder="请选择权限范围"
              options={scopeOptions}
            />
          </Form.Item>
          <Form.Item name="expiresAt" label="过期时间">
            <DatePicker
              showTime
              style={{ width: "100%" }}
              placeholder="选择过期时间（可选）"
            />
          </Form.Item>
          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit">
                创建
              </Button>
              <Button onClick={() => setModalVisible(false)}>取消</Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>

      {/* 显示令牌弹窗 */}
      <Modal
        title="令牌已创建"
        open={showTokenModal}
        onOk={() => setShowTokenModal(false)}
        onCancel={() => setShowTokenModal(false)}
      >
        <div style={{ marginBottom: 16 }}>
          <Text type="danger">请立即复制令牌，此令牌只会显示一次！</Text>
        </div>
        <Paragraph
          copyable={{ text: createdToken }}
          style={{
            background: "#f5f5f5",
            padding: 16,
            borderRadius: 4,
            wordBreak: "break-all",
          }}
        >
          {createdToken}
        </Paragraph>
      </Modal>

      {/* 访问日志弹窗 */}
      <Modal
        title="访问日志"
        open={logsModalVisible}
        onCancel={() => setLogsModalVisible(false)}
        footer={null}
        width={900}
      >
        <Table
          columns={logColumns}
          dataSource={accessLogs}
          rowKey="id"
          loading={logsLoading}
          pagination={{ pageSize: 10 }}
        />
      </Modal>
    </div>
  );
};

export default ApiTokenList;
