import React, { useRef, useState } from "react";
import {
  PlusOutlined,
  FileTextOutlined,
  InfoCircleOutlined,
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
  Tag,
  Space,
  Typography,
  Tooltip,
} from "antd";
import { DrawerForm } from "../../../components/DrawerForm";
import {
  listNotificationTemplates,
  createNotificationTemplate,
  updateNotificationTemplate,
  deleteNotificationTemplate,
} from "../../../services/notification";
import type {
  NotificationTemplate,
  NotificationTemplateCreate,
  ChannelType,
  TemplateType,
} from "../NotificationChannels/types";
import dayjs from "dayjs";

const { Text } = Typography;
const { TextArea } = Input;

const channelTypeLabels: Record<ChannelType, string> = {
  EMAIL: "邮件",
  WEBHOOK: "WebHook",
  DINGTALK: "钉钉",
  WECHAT_WORK: "企业微信",
};

const templateTypeLabels: Record<TemplateType, string> = {
  ALERT: "告警通知",
  TASK_COMPLETE: "任务完成",
  DEPLOY_SUCCESS: "部署成功",
  DEPLOY_FAILED: "部署失败",
  SYSTEM_NOTICE: "系统通知",
};

// Variable placeholders for different template types
const templateVariables: Record<TemplateType, string[]> = {
  ALERT: ["${alertName}", "${alertLevel}", "${alertTime}", "${alertContent}"],
  TASK_COMPLETE: ["${taskName}", "${taskId}", "${completeTime}", "${result}"],
  DEPLOY_SUCCESS: [
    "${appName}",
    "${envName}",
    "${deployTime}",
    "${version}",
  ],
  DEPLOY_FAILED: [
    "${appName}",
    "${envName}",
    "${deployTime}",
    "${errorMsg}",
  ],
  SYSTEM_NOTICE: ["${noticeTitle}", "${noticeContent}", "${noticeTime}"],
};

const NotificationTemplatesList: React.FC = () => {
  const actionRef = useRef<ActionType>();
  const [drawerVisible, setDrawerVisible] = useState(false);
  const [editingItem, setEditingItem] = useState<
    Partial<NotificationTemplate> | null
  >(null);

  const [createForm] = Form.useForm();

  const handleSave = async (data: any) => {
    try {
      if (editingItem?.id) {
        await updateNotificationTemplate(editingItem.id, {
          name: data.name,
          type: data.type,
          channelType: data.channelType,
          content: data.content,
          variables: data.variables,
        });
        message.success("更新成功");
      } else {
        const createData: NotificationTemplateCreate = {
          name: data.name,
          type: data.type,
          channelType: data.channelType,
          content: data.content,
          variables: data.variables,
        };
        await createNotificationTemplate(createData);
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
      await deleteNotificationTemplate(id);
      message.success("删除成功");
      actionRef.current?.reload();
    } catch (error: any) {
      message.error(error.message || "删除失败");
    }
  };

  const columns: ProColumns<NotificationTemplate>[] = [
    {
      title: "模板名称",
      dataIndex: "name",
      key: "name",
      width: 150,
      render: (text) => (
        <Space>
          <FileTextOutlined />
          <Text>{text}</Text>
        </Space>
      ),
    },
    {
      title: "模板类型",
      dataIndex: "type",
      key: "type",
      width: 120,
      render: (type) => {
        const colors: Record<TemplateType, string> = {
          ALERT: "orange",
          TASK_COMPLETE: "blue",
          DEPLOY_SUCCESS: "green",
          DEPLOY_FAILED: "red",
          SYSTEM_NOTICE: "purple",
        };
        return (
          <Tag color={colors[type as TemplateType] || "default"}>
            {templateTypeLabels[type as TemplateType] || type}
          </Tag>
        );
      },
    },
    {
      title: "关联渠道",
      dataIndex: "channelType",
      key: "channelType",
      width: 100,
      render: (channelType) => (
        <Tag color="cyan">
          {channelTypeLabels[channelType as ChannelType] || channelType}
        </Tag>
      ),
    },
    {
      title: "模板内容",
      dataIndex: "content",
      key: "content",
      ellipsis: true,
      width: 250,
      render: (content) => (content ? <Text ellipsis>{content}</Text> : "-"),
    },
    {
      title: "变量",
      dataIndex: "variables",
      key: "variables",
      ellipsis: true,
      width: 150,
      render: (variables) =>
        variables ? (
          <Tooltip title={variables}>
            <Text ellipsis style={{ maxWidth: 150 }}>
              {variables}
            </Text>
          </Tooltip>
        ) : (
          "-"
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
      width: 150,
      render: (_, record) => [
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
          title="确定删除该模板吗？"
          onConfirm={() => handleDelete(record.id)}
        >
          <Button type="link" size="small" danger>
            删除
          </Button>
        </Popconfirm>,
      ],
    },
  ];

  // Watch template type to show variable hints
  const templateType = Form.useWatch("type", createForm);

  return (
    <>
      <ProTable<NotificationTemplate>
        actionRef={actionRef}
        rowKey="id"
        columns={columns}
        request={async (_params) => {
          const res = await listNotificationTemplates();
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
            创建模板
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
        title={editingItem ? "编辑通知模板" : "创建通知模板"}
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
                channelType: editingItem.channelType,
                content: editingItem.content,
                variables: editingItem.variables,
              }
            : {}
        }
        form={createForm}
        width={700}
      >
        <Form.Item
          name="name"
          label="模板名称"
          rules={[{ required: true, message: "请输入模板名称" }]}
        >
          <Input placeholder="请输入模板名称" maxLength={255} />
        </Form.Item>
        <Form.Item
          name="type"
          label="模板类型"
          rules={[{ required: true, message: "请选择模板类型" }]}
        >
          <Select placeholder="请选择模板类型">
            <Select.Option value="ALERT">告警通知</Select.Option>
            <Select.Option value="TASK_COMPLETE">任务完成</Select.Option>
            <Select.Option value="DEPLOY_SUCCESS">部署成功</Select.Option>
            <Select.Option value="DEPLOY_FAILED">部署失败</Select.Option>
            <Select.Option value="SYSTEM_NOTICE">系统通知</Select.Option>
          </Select>
        </Form.Item>
        <Form.Item
          name="channelType"
          label="关联渠道类型"
          rules={[{ required: true, message: "请选择关联渠道类型" }]}
        >
          <Select placeholder="请选择关联渠道类型">
            <Select.Option value="EMAIL">邮件</Select.Option>
            <Select.Option value="WEBHOOK">WebHook</Select.Option>
            <Select.Option value="DINGTALK">钉钉</Select.Option>
            <Select.Option value="WECHAT_WORK">企业微信</Select.Option>
          </Select>
        </Form.Item>
        {templateType && (
          <Form.Item label="可用变量">
            <Space wrap>
              <InfoCircleOutlined style={{ color: "#1890ff" }} />
              <Text type="secondary">
                可使用以下变量占位符（点击可复制）：
              </Text>
              {templateVariables[templateType as TemplateType]?.map((v) => (
                <Tag
                  key={v}
                  color="blue"
                  style={{ cursor: "pointer" }}
                  onClick={() => {
                    navigator.clipboard.writeText(v);
                    message.success("已复制变量");
                  }}
                >
                  {v}
                </Tag>
              ))}
            </Space>
          </Form.Item>
        )}
        <Form.Item
          name="content"
          label="模板内容"
          rules={[{ required: true, message: "请输入模板内容" }]}
        >
          <TextArea
            rows={8}
            placeholder="请输入模板内容，可使用变量占位符如 ${alertName}"
          />
        </Form.Item>
        <Form.Item
          name="variables"
          label="变量定义"
          tooltip="可选：定义变量及其说明，JSON 格式"
        >
          <TextArea
            rows={4}
            placeholder={"可选：定义变量说明，如：\n{\n  \"alertName\": \"告警名称\",\n  \"alertLevel\": \"告警级别\"\n}"}
          />
        </Form.Item>
      </DrawerForm>
    </>
  );
};

export default NotificationTemplatesList;