import React, { useRef, useState } from "react";
import {
  PlusOutlined,
  BellOutlined,
  SendOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
} from "@ant-design/icons";
import type { ActionType, ProColumns } from "@ant-design/pro-components";
import { ProTable } from "@ant-design/pro-components";
import {
  Button,
  message,
  Popconfirm,
  Form,
  Input,
  Select,
  Switch,
  Tag,
  Space,
  Modal,
  Typography,
} from "antd";
import { DrawerForm } from "../../../components/DrawerForm";
import {
  listNotificationChannels,
  createNotificationChannel,
  updateNotificationChannel,
  deleteNotificationChannel,
  testNotificationChannel,
} from "../../../services/notification";
import type {
  NotificationChannel,
  NotificationChannelCreate,
  ChannelType,
} from "./types";
import dayjs from "dayjs";

const { Text } = Typography;
const { TextArea } = Input;

const channelTypeLabels: Record<ChannelType, string> = {
  EMAIL: "邮件",
  WEBHOOK: "WebHook",
  DINGTALK: "钉钉",
  WECHAT_WORK: "企业微信",
};

const NotificationChannelsList: React.FC = () => {
  const actionRef = useRef<ActionType>();
  const [drawerVisible, setDrawerVisible] = useState(false);
  const [editingItem, setEditingItem] =
    useState<Partial<NotificationChannel> | null>(null);
  const [testVisible, setTestVisible] = useState(false);
  const [testingChannel, setTestingChannel] =
    useState<NotificationChannel | null>(null);
  const [testLoading, setTestLoading] = useState(false);
  const [testResult, setTestResult] = useState<{
    success: boolean;
    message: string;
  } | null>(null);

  const [createForm] = Form.useForm();
  const [testForm] = Form.useForm();

  const handleSave = async (data: any) => {
    try {
      if (editingItem?.id) {
        await updateNotificationChannel(editingItem.id, {
          name: data.name,
          type: data.type,
          config: data.config,
          enabled: data.enabled,
        });
        message.success("更新成功");
      } else {
        const createData: NotificationChannelCreate = {
          name: data.name,
          type: data.type,
          config: data.config,
          enabled: data.enabled ?? true,
        };
        await createNotificationChannel(createData);
        message.success("创建成功");
      }
      setDrawerVisible(false);
      setEditingItem(null);
      createForm.resetFields();
      actionRef.current?.reload();
    } catch (error: any) {
      message.error(error.message || "保存失败");
    }
  };

  const handleDelete = async (id: string) => {
    try {
      await deleteNotificationChannel(id);
      message.success("删除成功");
      actionRef.current?.reload();
    } catch (error: any) {
      message.error(error.message || "删除失败");
    }
  };

  const handleTest = async (data: any) => {
    if (!testingChannel?.id) return;

    setTestLoading(true);
    try {
      const result = await testNotificationChannel(testingChannel.id, {
        recipient: data.recipient,
        title: data.title,
        content: data.content,
      });
      setTestResult(result);
      if (result.success) {
        message.success("测试发送成功");
      } else {
        message.error(result.message || "测试发送失败");
      }
    } catch (error: any) {
      setTestResult({ success: false, message: error.message || "测试失败" });
      message.error(error.message || "测试失败");
    } finally {
      setTestLoading(false);
    }
  };

  const openTestModal = (channel: NotificationChannel) => {
    setTestingChannel(channel);
    setTestResult(null);
    testForm.resetFields();
    setTestVisible(true);
  };

  const columns: ProColumns<NotificationChannel>[] = [
    {
      title: "名称",
      dataIndex: "name",
      key: "name",
      width: 150,
      render: (text, record) => (
        <Space>
          <BellOutlined />
          <a onClick={() => openTestModal(record)}>{text}</a>
        </Space>
      ),
    },
    {
      title: "渠道类型",
      dataIndex: "type",
      key: "type",
      width: 120,
      render: (type) => {
        const colors: Record<ChannelType, string> = {
          EMAIL: "blue",
          WEBHOOK: "purple",
          DINGTALK: "cyan",
          WECHAT_WORK: "green",
        };
        return (
          <Tag color={colors[type as ChannelType] || "default"}>
            {channelTypeLabels[type as ChannelType] || type}
          </Tag>
        );
      },
    },
    {
      title: "配置信息",
      dataIndex: "config",
      key: "config",
      ellipsis: true,
      width: 200,
      render: (config) => (config ? <Text ellipsis>{config}</Text> : "-"),
    },
    {
      title: "状态",
      dataIndex: "enabled",
      key: "enabled",
      width: 80,
      render: (enabled) => (
        <Tag color={enabled ? "success" : "default"}>
          {enabled ? "启用" : "禁用"}
        </Tag>
      ),
    },
    {
      title: "创建时间",
      dataIndex: "createdAt",
      key: "createdAt",
      valueType: "dateTime",
      width: 170,
      render: (time) =>
        time ? dayjs(time as string).format("YYYY-MM-DD HH:mm:ss") : "-",
    },
    {
      title: "操作",
      valueType: "option",
      width: 200,
      render: (_, record) => [
        <Button
          key="test"
          type="link"
          size="small"
          icon={<SendOutlined />}
          onClick={() => openTestModal(record)}
        >
          测试
        </Button>,
        <Button
          key="edit"
          type="link"
          size="small"
          onClick={() => {
            setEditingItem(record);
            setDrawerVisible(true);
          }}
        >
          编辑
        </Button>,
        <Popconfirm
          key="delete"
          title="确定删除该渠道吗？"
          onConfirm={() => handleDelete(record.id)}
        >
          <Button type="link" size="small" danger>
            删除
          </Button>
        </Popconfirm>,
      ],
    },
  ];

  return (
    <>
      <ProTable<NotificationChannel>
        actionRef={actionRef}
        rowKey="id"
        columns={columns}
        request={async (_params) => {
          const res = await listNotificationChannels();
          return {
            data: res || [],
            total: res?.length || 0,
            success: true,
          };
        }}
        toolBarRender={() => [
          <Button
            key="create"
            type="primary"
            icon={<PlusOutlined />}
            onClick={() => {
              setEditingItem(null);
              setDrawerVisible(true);
            }}
          >
            创建渠道
          </Button>,
        ]}
        search={{
          filterType: "light",
        }}
        options={{
          density: false,
          fullScreen: false,
          reload: true,
          setting: false,
        }}
      />

      <DrawerForm
        title={editingItem ? "编辑通知渠道" : "创建通知渠道"}
        visible={drawerVisible}
        onClose={() => {
          setDrawerVisible(false);
          setEditingItem(null);
          createForm.resetFields();
        }}
        onSave={handleSave}
        initialValues={
          editingItem
            ? {
                name: editingItem.name,
                type: editingItem.type,
                config: editingItem.config,
                enabled: editingItem.enabled,
              }
            : { enabled: true }
        }
        form={createForm}
        width={600}
      >
        <Form.Item
          name="name"
          label="名称"
          rules={[{ required: true, message: "请输入渠道名称" }]}
        >
          <Input placeholder="请输入渠道名称" maxLength={255} />
        </Form.Item>
        <Form.Item
          name="type"
          label="渠道类型"
          rules={[{ required: true, message: "请选择渠道类型" }]}
        >
          <Select placeholder="请选择渠道类型" disabled={!!editingItem}>
            <Select.Option value="EMAIL">邮件</Select.Option>
            <Select.Option value="WEBHOOK">WebHook</Select.Option>
            <Select.Option value="DINGTALK">钉钉</Select.Option>
            <Select.Option value="WECHAT_WORK">企业微信</Select.Option>
          </Select>
        </Form.Item>
        <Form.Item
          name="config"
          label="配置信息"
          tooltip="根据渠道类型填写相应的配置，如 JSON 格式的配置参数"
        >
          <TextArea
            rows={6}
            placeholder={
              '请输入配置信息，如：\n{\n  "host": "smtp.example.com",\n  "port": 465\n}'
            }
          />
        </Form.Item>
        {editingItem && (
          <Form.Item name="enabled" label="状态" valuePropName="checked">
            <Switch checkedChildren="启用" unCheckedChildren="禁用" />
          </Form.Item>
        )}
      </DrawerForm>

      <Modal
        title={
          <Space>
            <SendOutlined />
            <span>测试通知渠道 - {testingChannel?.name}</span>
          </Space>
        }
        open={testVisible}
        onCancel={() => setTestVisible(false)}
        footer={null}
        width={600}
      >
        <Form
          form={testForm}
          layout="vertical"
          onFinish={handleTest}
          initialValues={{ title: "测试通知", content: "这是一条测试通知消息" }}
        >
          <Form.Item
            name="recipient"
            label="接收人"
            rules={[{ required: true, message: "请输入接收人" }]}
          >
            <Input placeholder="请输入接收人地址（邮箱/钉钉用户/企业微信用户）" />
          </Form.Item>
          <Form.Item name="title" label="通知标题">
            <Input placeholder="请输入通知标题" />
          </Form.Item>
          <Form.Item name="content" label="通知内容">
            <TextArea rows={4} placeholder="请输入通知内容" />
          </Form.Item>
          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit" loading={testLoading}>
                发送测试
              </Button>
              <Button onClick={() => setTestVisible(false)}>关闭</Button>
            </Space>
          </Form.Item>
        </Form>
        {testResult && (
          <div style={{ marginTop: 16 }}>
            <Space>
              {testResult.success ? (
                <CheckCircleOutlined style={{ color: "#52c41a" }} />
              ) : (
                <CloseCircleOutlined style={{ color: "#ff4d4f" }} />
              )}
              <Text type={testResult.success ? "success" : "danger"}>
                {testResult.message}
              </Text>
            </Space>
          </div>
        )}
      </Modal>
    </>
  );
};

export default NotificationChannelsList;
