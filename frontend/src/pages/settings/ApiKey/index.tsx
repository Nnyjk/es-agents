import React, { useRef, useState } from "react";
import {
  PlusOutlined,
  KeyOutlined,
  StopOutlined,
  ReloadOutlined,
  HistoryOutlined,
  CopyOutlined,
  CheckOutlined,
} from "@ant-design/icons";
import type { ActionType, ProColumns } from "@ant-design/pro-components";
import { ProTable } from "@ant-design/pro-components";
import {
  Button,
  message,
  Popconfirm,
  Form,
  Input,
  Switch,
  Tag,
  Space,
  Modal,
  Descriptions,
  Table,
  Typography,
  DatePicker,
  Select,
  Tooltip,
} from "antd";
import { DrawerForm } from "../../../components/DrawerForm";
import {
  listApiKeys,
  createApiKey,
  updateApiKey,
  deleteApiKey,
  revokeApiKey,
  refreshApiKey,
  getApiKeyLogs,
} from "../../../services/apiKey";
import type {
  ApiKey,
  ApiKeyCreate,
  ApiKeyUsageLog,
} from "./types";
import dayjs from "dayjs";

const { Text } = Typography;
const { TextArea } = Input;

const ApiKeyList: React.FC = () => {
  const actionRef = useRef<ActionType>();
  const [drawerVisible, setDrawerVisible] = useState(false);
  const [editingItem, setEditingItem] = useState<Partial<ApiKey> & { ipWhitelistText?: string } | null>(null);
  const [detailVisible, setDetailVisible] = useState(false);
  const [selectedKey, setSelectedKey] = useState<ApiKey | null>(null);
  const [usageLogs, setUsageLogs] = useState<ApiKeyUsageLog[]>([]);
  const [logsLoading, setLogsLoading] = useState(false);
  const [newKeyVisible, setNewKeyVisible] = useState(false);
  const [newKeyValue, setNewKeyValue] = useState<string>("");
  const [copied, setCopied] = useState(false);

  // 创建表单初始值
  const [createForm] = Form.useForm();

  // 获取状态显示
  const getStatusTags = (record: ApiKey) => {
    const tags: React.ReactNode[] = [];

    if (record.revoked) {
      tags.push(
        <Tag key="revoked" color="error">
          已吊销
        </Tag>
      );
    }

    if (record.expired) {
      tags.push(
        <Tag key="expired" color="warning">
          已过期
        </Tag>
      );
    }

    if (!record.enabled) {
      tags.push(
        <Tag key="disabled" color="default">
          已禁用
        </Tag>
      );
    }

    if (record.valid && record.enabled && !record.revoked && !record.expired) {
      tags.push(
        <Tag key="valid" color="success">
          有效
        </Tag>
      );
    }

    return tags.length > 0 ? <Space>{tags}</Space> : <Tag color="default">未知</Tag>;
  };

  // 处理创建
  const handleCreate = async (data: any) => {
    try {
      const createData: ApiKeyCreate = {
        name: data.name,
        description: data.description,
        expiresAt: data.expiresAt ? dayjs(data.expiresAt).toISOString() : undefined,
        permissions: data.permissions,
        ipWhitelist: data.ipWhitelist
          ? data.ipWhitelist.split("\n").filter((ip: string) => ip.trim())
          : undefined,
      };

      const result = await createApiKey(createData);
      message.success("创建成功");

      // 显示新创建的密钥
      if (result.key) {
        setNewKeyValue(result.key);
        setNewKeyVisible(true);
      }

      setDrawerVisible(false);
      setEditingItem(null);
      createForm.resetFields();
      actionRef.current?.reload();
    } catch (error: any) {
      message.error(error.message || "创建失败");
    }
  };

  // 处理更新
  const handleUpdate = async (data: any) => {
    try {
      if (!editingItem?.id) return;

      const updateData = {
        name: data.name,
        description: data.description,
        expiresAt: data.expiresAt ? dayjs(data.expiresAt).toISOString() : undefined,
        enabled: data.enabled,
        permissions: data.permissions,
        ipWhitelist: data.ipWhitelist
          ? data.ipWhitelist.split("\n").filter((ip: string) => ip.trim())
          : undefined,
      };

      await updateApiKey(editingItem.id, updateData);
      message.success("更新成功");
      setDrawerVisible(false);
      setEditingItem(null);
      actionRef.current?.reload();
    } catch (error: any) {
      message.error(error.message || "更新失败");
    }
  };

  // 处理删除
  const handleDelete = async (id: string) => {
    try {
      await deleteApiKey(id);
      message.success("删除成功");
      actionRef.current?.reload();
    } catch (error: any) {
      message.error(error.message || "删除失败");
    }
  };

  // 处理吊销
  const handleRevoke = async (id: string, reason: string) => {
    try {
      await revokeApiKey(id, { reason });
      message.success("吊销成功");
      actionRef.current?.reload();
    } catch (error: any) {
      message.error(error.message || "吊销失败");
    }
  };

  // 处理刷新
  const handleRefresh = async (id: string) => {
    try {
      const result = await refreshApiKey(id);
      message.success("刷新成功");

      // 显示新密钥
      if (result.key) {
        setNewKeyValue(result.key);
        setNewKeyVisible(true);
      }

      actionRef.current?.reload();
    } catch (error: any) {
      message.error(error.message || "刷新失败");
    }
  };

  // 查看访问日志
  const handleViewLogs = async (key: ApiKey) => {
    setSelectedKey(key);
    setDetailVisible(true);
    setLogsLoading(true);

    try {
      const logs = await getApiKeyLogs(key.id);
      setUsageLogs(logs);
    } catch (error: any) {
      message.error(error.message || "获取日志失败");
      setUsageLogs([]);
    } finally {
      setLogsLoading(false);
    }
  };

  // 复制密钥
  const handleCopyKey = () => {
    navigator.clipboard.writeText(newKeyValue);
    setCopied(true);
    message.success("已复制到剪贴板");
    setTimeout(() => setCopied(false), 2000);
  };

  // 关闭新密钥对话框
  const handleCloseNewKey = () => {
    setNewKeyVisible(false);
    setNewKeyValue("");
  };

  const columns: ProColumns<ApiKey>[] = [
    {
      title: "名称",
      dataIndex: "name",
      key: "name",
      width: 150,
      render: (text, record) => (
        <Space>
          <KeyOutlined />
          <a onClick={() => handleViewLogs(record)}>{text}</a>
        </Space>
      ),
    },
    {
      title: "描述",
      dataIndex: "description",
      key: "description",
      ellipsis: true,
      width: 200,
    },
    {
      title: "权限",
      dataIndex: "permissions",
      key: "permissions",
      width: 150,
      render: (_, record) =>
        record.permissions && record.permissions.length > 0
          ? record.permissions.map((p) => (
              <Tag key={p} style={{ marginBottom: 2 }}>
                {p}
              </Tag>
            ))
          : "-",
    },
    {
      title: "IP 白名单",
      dataIndex: "ipWhitelist",
      key: "ipWhitelist",
      width: 150,
      render: (_, record) =>
        record.ipWhitelist && record.ipWhitelist.length > 0
          ? record.ipWhitelist.slice(0, 3).map((ip) => (
              <Tag key={ip} color="blue" style={{ marginBottom: 2 }}>
                {ip}
              </Tag>
            ))
          : "无限制",
    },
    {
      title: "过期时间",
      dataIndex: "expiresAt",
      key: "expiresAt",
      valueType: "dateTime",
      width: 170,
      render: (time) =>
        time ? (
          dayjs(time as string).format("YYYY-MM-DD HH:mm:ss")
        ) : (
          <Tag color="blue">永久有效</Tag>
        ),
    },
    {
      title: "状态",
      key: "status",
      width: 120,
      render: (_, record) => getStatusTags(record),
    },
    {
      title: "创建时间",
      dataIndex: "createdAt",
      key: "createdAt",
      valueType: "dateTime",
      width: 170,
    },
    {
      title: "操作",
      valueType: "option",
      width: 220,
      render: (_, record) => [
        <Button
          key="view"
          type="link"
          size="small"
          icon={<HistoryOutlined />}
          onClick={() => handleViewLogs(record)}
        >
          日志
        </Button>,
        !record.revoked && (
          <Button
            key="edit"
            type="link"
            size="small"
            onClick={() => {
              setEditingItem({
                ...record,
                ipWhitelistText: record.ipWhitelist?.join("\n"),
              });
              setDrawerVisible(true);
            }}
          >
            编辑
          </Button>
        ),
        !record.revoked && !record.expired && record.enabled && (
          <Popconfirm
            key="refresh"
            title="刷新密钥将生成新的密钥值，旧密钥将失效，确定继续？"
            onConfirm={() => handleRefresh(record.id)}
          >
            <Button type="link" size="small" icon={<ReloadOutlined />}>
              刷新
            </Button>
          </Popconfirm>
        ),
        !record.revoked && (
          <Popconfirm
            key="revoke"
            title={
              <div>
                <p>确定吊销该密钥吗？</p>
                <Input.TextArea
                  id="revoke-reason"
                  placeholder="请输入吊销原因"
                  rows={2}
                  style={{ marginTop: 8 }}
                />
              </div>
            }
            onConfirm={() => {
              const reasonInput = document.getElementById("revoke-reason") as HTMLTextAreaElement;
              handleRevoke(record.id, reasonInput?.value || "");
            }}
          >
            <Button type="link" size="small" danger icon={<StopOutlined />}>
              吊销
            </Button>
          </Popconfirm>
        ),
        <Popconfirm
          key="delete"
          title="确定删除该密钥吗？"
          onConfirm={() => handleDelete(record.id)}
        >
          <Button type="link" size="small" danger>
            删除
          </Button>
        </Popconfirm>,
      ],
    },
  ];

  const logColumns = [
    {
      title: "时间",
      dataIndex: "usageTime",
      key: "usageTime",
      width: 170,
      render: (time: string) =>
        time ? dayjs(time).format("YYYY-MM-DD HH:mm:ss") : "-",
    },
    {
      title: "客户端 IP",
      dataIndex: "clientIp",
      key: "clientIp",
      width: 120,
    },
    {
      title: "请求方法",
      dataIndex: "requestMethod",
      key: "requestMethod",
      width: 80,
      render: (method: string) => (
        <Tag
          color={
            method === "GET"
              ? "green"
              : method === "POST"
                ? "blue"
                : method === "PUT"
                  ? "orange"
                  : method === "DELETE"
                    ? "red"
                    : "default"
          }
        >
          {method}
        </Tag>
      ),
    },
    {
      title: "请求路径",
      dataIndex: "requestPath",
      key: "requestPath",
      ellipsis: true,
    },
    {
      title: "状态码",
      dataIndex: "responseStatus",
      key: "responseStatus",
      width: 80,
      render: (status: number) =>
        status ? (
          <Tag color={status < 400 ? "success" : "error"}>{status}</Tag>
        ) : "-",
    },
    {
      title: "响应时间",
      dataIndex: "responseTimeMs",
      key: "responseTimeMs",
      width: 100,
      render: (ms: number) => (ms ? `${ms}ms` : "-"),
    },
    {
      title: "权限",
      dataIndex: "permissionUsed",
      key: "permissionUsed",
      width: 120,
      render: (perm: string) => perm ? <Tag>{perm}</Tag> : "-",
    },
    {
      title: "错误信息",
      dataIndex: "errorMessage",
      key: "errorMessage",
      ellipsis: true,
      render: (msg: string) =>
        msg ? <Text type="danger">{msg}</Text> : "-",
    },
  ];

  return (
    <>
      <ProTable<ApiKey>
        actionRef={actionRef}
        rowKey="id"
        columns={columns}
        request={async (params) => {
          const query: any = {};
          if (params.keyword) query.keyword = params.keyword;
          if (params.enabled !== undefined) query.enabled = params.enabled;
          if (params.expired !== undefined) query.expired = params.expired;
          if (params.revoked !== undefined) query.revoked = params.revoked;

          const res = await listApiKeys(query);
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
            创建密钥
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
        title={editingItem ? "编辑 API 密钥" : "创建 API 密钥"}
        visible={drawerVisible}
        onClose={() => {
          setDrawerVisible(false);
          setEditingItem(null);
          createForm.resetFields();
        }}
        onSave={editingItem ? handleUpdate : handleCreate}
        initialValues={
          editingItem
            ? {
                name: editingItem.name,
                description: editingItem.description,
                expiresAt: editingItem.expiresAt
                  ? dayjs(editingItem.expiresAt)
                  : undefined,
                enabled: editingItem.enabled,
                permissions: editingItem.permissions,
                ipWhitelist: editingItem.ipWhitelistText,
              }
            : { enabled: true }
        }
        form={createForm}
        width={600}
      >
        <Form.Item
          name="name"
          label="名称"
          rules={[{ required: true, message: "请输入密钥名称" }]}
        >
          <Input placeholder="请输入密钥名称" maxLength={255} />
        </Form.Item>
        <Form.Item name="description" label="描述">
          <TextArea
            rows={3}
            placeholder="请输入描述"
            maxLength={500}
          />
        </Form.Item>
        <Form.Item name="expiresAt" label="过期时间">
          <DatePicker
            showTime
            style={{ width: "100%" }}
            placeholder="不设置则永久有效"
          />
        </Form.Item>
        {editingItem && (
          <Form.Item
            name="enabled"
            label="状态"
            valuePropName="checked"
          >
            <Switch checkedChildren="启用" unCheckedChildren="禁用" />
          </Form.Item>
        )}
        <Form.Item name="permissions" label="权限">
          <Select
            mode="tags"
            placeholder="输入权限标签，如 api:read, api:write"
            style={{ width: "100%" }}
          />
        </Form.Item>
        <Form.Item
          name="ipWhitelist"
          label="IP 白名单"
          tooltip="每行一个 IP 地址，不设置则不限制"
        >
          <TextArea
            rows={3}
            placeholder="每行一个 IP 地址，如：&#10;192.168.1.100&#10;10.0.0.1"
          />
        </Form.Item>
      </DrawerForm>

      <Modal
        title={
          <Space>
            <HistoryOutlined />
            <span>{selectedKey?.name} - 访问日志</span>
          </Space>
        }
        open={detailVisible}
        onCancel={() => setDetailVisible(false)}
        footer={null}
        width={900}
      >
        {selectedKey && (
          <>
            <Descriptions
              column={2}
              bordered
              size="small"
              style={{ marginBottom: 16 }}
            >
              <Descriptions.Item label="名称">
                {selectedKey.name}
              </Descriptions.Item>
              <Descriptions.Item label="描述">
                {selectedKey.description || "-"}
              </Descriptions.Item>
              <Descriptions.Item label="状态">
                {getStatusTags(selectedKey)}
              </Descriptions.Item>
              <Descriptions.Item label="过期时间">
                {selectedKey.expiresAt
                  ? dayjs(selectedKey.expiresAt).format("YYYY-MM-DD HH:mm:ss")
                  : "永久有效"}
              </Descriptions.Item>
              <Descriptions.Item label="创建时间">
                {selectedKey.createdAt
                  ? dayjs(selectedKey.createdAt).format("YYYY-MM-DD HH:mm:ss")
                  : "-"}
              </Descriptions.Item>
              <Descriptions.Item label="最后使用">
                {selectedKey.lastUsedAt
                  ? dayjs(selectedKey.lastUsedAt).format("YYYY-MM-DD HH:mm:ss")
                  : "-"}
              </Descriptions.Item>
              {selectedKey.revoked && (
                <>
                  <Descriptions.Item label="吊销时间">
                    {selectedKey.revokedAt
                      ? dayjs(selectedKey.revokedAt).format(
                          "YYYY-MM-DD HH:mm:ss",
                        )
                      : "-"}
                  </Descriptions.Item>
                  <Descriptions.Item label="吊销原因">
                    {selectedKey.revokeReason || "-"}
                  </Descriptions.Item>
                </>
              )}
            </Descriptions>

            <Typography.Title level={5} style={{ marginBottom: 12 }}>
              使用记录 ({usageLogs.length})
            </Typography.Title>
            <Table
              rowKey="id"
              columns={logColumns}
              dataSource={usageLogs}
              size="small"
              loading={logsLoading}
              pagination={{ pageSize: 10 }}
              locale={{ emptyText: "暂无访问记录" }}
            />
          </>
        )}
      </Modal>

      <Modal
        title={
          <Space>
            <KeyOutlined />
            <span>新密钥已生成</span>
          </Space>
        }
        open={newKeyVisible}
        onCancel={handleCloseNewKey}
        footer={[
          <Button key="close" onClick={handleCloseNewKey}>
            关闭（密钥将不再显示）
          </Button>,
        ]}
        width={600}
      >
        <div style={{ marginBottom: 16 }}>
          <Text type="warning">
            请立即复制并妥善保存以下密钥值，关闭后将无法再次查看完整密钥。
          </Text>
        </div>
        <div
          style={{
            background: "#f5f5f5",
            padding: 16,
            borderRadius: 4,
            display: "flex",
            alignItems: "center",
            justifyContent: "space-between",
          }}
        >
          <Text copyable={{ text: newKeyValue }} style={{ flex: 1 }}>
            {newKeyValue}
          </Text>
          <Tooltip title={copied ? "已复制" : "复制"}>
            <Button
              type="primary"
              icon={copied ? <CheckOutlined /> : <CopyOutlined />}
              onClick={handleCopyKey}
            >
              {copied ? "已复制" : "复制"}
            </Button>
          </Tooltip>
        </div>
      </Modal>
    </>
  );
};

export default ApiKeyList;