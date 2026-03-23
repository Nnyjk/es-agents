import React, { useState, useEffect } from "react";
import {
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
  Descriptions,
  Badge,
  Tooltip,
  Tabs,
  Drawer,
  Progress,
} from "antd";
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  SyncOutlined,
  ApiOutlined,
  SafetyOutlined,
  FileTextOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
} from "@ant-design/icons";
import type { ColumnsType } from "antd/es/table";
import dayjs from "dayjs";
import {
  getCloudAccounts,
  createCloudAccount,
  updateCloudAccount,
  deleteCloudAccount,
  testCloudAccountConnection,
  syncCloudAccountResources,
  getAccountPermissions,
  setAccountPermission,
  getAccountAuditLogs,
} from "@/services/multicloud";
import type {
  CloudAccount,
  CloudAccountStatus,
  CloudProvider,
  AccountPermission,
  AccountAuditLog,
} from "@/types/multicloud";

const { Option } = Select;

// 云厂商显示配置
const providerConfig: Record<string, { name: string; color: string }> = {
  aliyun: { name: "阿里云", color: "#FF6A00" },
  tencent: { name: "腾讯云", color: "#00A3FF" },
  huawei: { name: "华为云", color: "#C00" },
  aws: { name: "AWS", color: "#FF9900" },
  azure: { name: "Azure", color: "#00BCF2" },
  vmware: { name: "VMware", color: "#607078" },
  openstack: { name: "OpenStack", color: "#ED1944" },
};

// 账号状态显示配置
const statusConfig: Record<
  string,
  { text: string; status: "success" | "warning" | "error" | "default" }
> = {
  healthy: { text: "健康", status: "success" },
  warning: { text: "警告", status: "warning" },
  error: { text: "异常", status: "error" },
  unknown: { text: "未知", status: "default" },
};

const CloudAccountPage: React.FC = () => {
  const [accounts, setAccounts] = useState<CloudAccount[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [permissionDrawerVisible, setPermissionDrawerVisible] = useState(false);
  const [auditDrawerVisible, setAuditDrawerVisible] = useState(false);
  const [detailDrawerVisible, setDetailDrawerVisible] = useState(false);
  const [editingAccount, setEditingAccount] = useState<CloudAccount | null>(
    null,
  );
  const [selectedAccount, setSelectedAccount] = useState<CloudAccount | null>(
    null,
  );
  const [permissions, setPermissions] = useState<AccountPermission[]>([]);
  const [auditLogs, setAuditLogs] = useState<AccountAuditLog[]>([]);
  const [auditLoading, setAuditLoading] = useState(false);
  const [form] = Form.useForm();
  const [permissionForm] = Form.useForm();

  // 加载账号列表
  const loadAccounts = async () => {
    setLoading(true);
    try {
      const data = await getCloudAccounts();
      setAccounts(data);
    } catch {
      message.error("加载云账号列表失败");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadAccounts();
  }, []);

  // 打开创建/编辑模态框
  const handleOpenModal = (account?: CloudAccount) => {
    setEditingAccount(account || null);
    if (account) {
      form.setFieldsValue(account);
    } else {
      form.resetFields();
    }
    setModalVisible(true);
  };

  // 保存账号
  const handleSave = async () => {
    try {
      const values = await form.validateFields();
      if (editingAccount) {
        await updateCloudAccount(editingAccount.id, values);
        message.success("更新成功");
      } else {
        await createCloudAccount(values);
        message.success("创建成功");
      }
      setModalVisible(false);
      loadAccounts();
    } catch {
      message.error("保存失败");
    }
  };

  // 删除账号
  const handleDelete = async (id: string) => {
    try {
      await deleteCloudAccount(id);
      message.success("删除成功");
      loadAccounts();
    } catch {
      message.error("删除失败");
    }
  };

  // 测试连接
  const handleTestConnection = async (id: string) => {
    try {
      const result = await testCloudAccountConnection(id);
      if (result.success) {
        message.success("连接测试成功");
      } else {
        message.error(`连接测试失败: ${result.message}`);
      }
    } catch {
      message.error("连接测试失败");
    }
  };

  // 同步资源
  const handleSync = async (id: string) => {
    try {
      const result = await syncCloudAccountResources(id);
      if (result.success) {
        message.success("资源同步成功");
        loadAccounts();
      } else {
        message.error(`资源同步失败: ${result.message}`);
      }
    } catch {
      message.error("资源同步失败");
    }
  };

  // 查看权限
  const handleViewPermissions = async (account: CloudAccount) => {
    setSelectedAccount(account);
    try {
      const data = await getAccountPermissions(account.id);
      setPermissions(data);
      setPermissionDrawerVisible(true);
    } catch {
      message.error("加载权限配置失败");
    }
  };

  // 查看审计日志
  const handleViewAuditLogs = async (account: CloudAccount) => {
    setSelectedAccount(account);
    setAuditDrawerVisible(true);
    setAuditLoading(true);
    try {
      const result = await getAccountAuditLogs({
        accountId: account.id,
        pageSize: 50,
      });
      setAuditLogs(result.data);
    } catch {
      message.error("加载审计日志失败");
    } finally {
      setAuditLoading(false);
    }
  };

  // 查看详情
  const handleViewDetail = (account: CloudAccount) => {
    setSelectedAccount(account);
    setDetailDrawerVisible(true);
  };

  // 表格列定义
  const columns: ColumnsType<CloudAccount> = [
    {
      title: "账号名称",
      dataIndex: "name",
      key: "name",
      width: 180,
      render: (text: string, record: CloudAccount) => (
        <a onClick={() => handleViewDetail(record)}>{text}</a>
      ),
    },
    {
      title: "云厂商",
      dataIndex: "provider",
      key: "provider",
      width: 120,
      render: (provider: CloudProvider) => (
        <Tag color={providerConfig[provider]?.color || "#666"}>
          {providerConfig[provider]?.name || provider}
        </Tag>
      ),
    },
    {
      title: "Access Key ID",
      dataIndex: "accessKeyId",
      key: "accessKeyId",
      width: 200,
      render: (key: string) => (
        <Tooltip title={key}>
          <span>
            {key.slice(0, 8)}...{key.slice(-4)}
          </span>
        </Tooltip>
      ),
    },
    {
      title: "区域",
      dataIndex: "regions",
      key: "regions",
      width: 150,
      render: (regions: string[]) => (
        <Tooltip title={regions.join(", ")}>
          <span>{regions.length} 个区域</span>
        </Tooltip>
      ),
    },
    {
      title: "状态",
      dataIndex: "status",
      key: "status",
      width: 100,
      render: (status: CloudAccountStatus) => {
        const config = statusConfig[status];
        return <Badge status={config.status} text={config.text} />;
      },
    },
    {
      title: "API 连通性",
      dataIndex: "apiConnectivity",
      key: "apiConnectivity",
      width: 100,
      render: (connected: boolean) =>
        connected ? (
          <CheckCircleOutlined style={{ color: "#52c41a" }} />
        ) : (
          <CloseCircleOutlined style={{ color: "#ff4d4f" }} />
        ),
    },
    {
      title: "最后同步",
      dataIndex: "lastSyncTime",
      key: "lastSyncTime",
      width: 160,
      render: (time: string) =>
        time ? dayjs(time).format("YYYY-MM-DD HH:mm") : "-",
    },
    {
      title: "操作",
      key: "action",
      width: 280,
      render: (_: unknown, record: CloudAccount) => (
        <Space size="small">
          <Button
            type="link"
            size="small"
            icon={<ApiOutlined />}
            onClick={() => handleTestConnection(record.id)}
          >
            测试
          </Button>
          <Button
            type="link"
            size="small"
            icon={<SyncOutlined />}
            onClick={() => handleSync(record.id)}
          >
            同步
          </Button>
          <Button
            type="link"
            size="small"
            icon={<SafetyOutlined />}
            onClick={() => handleViewPermissions(record)}
          >
            权限
          </Button>
          <Button
            type="link"
            size="small"
            icon={<FileTextOutlined />}
            onClick={() => handleViewAuditLogs(record)}
          >
            日志
          </Button>
          <Button
            type="link"
            size="small"
            icon={<EditOutlined />}
            onClick={() => handleOpenModal(record)}
          >
            编辑
          </Button>
          <Popconfirm
            title="确定要删除此账号吗？"
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

  // 审计日志列定义
  const auditColumns: ColumnsType<AccountAuditLog> = [
    {
      title: "时间",
      dataIndex: "createdAt",
      key: "createdAt",
      width: 160,
      render: (time: string) => dayjs(time).format("YYYY-MM-DD HH:mm:ss"),
    },
    {
      title: "操作人",
      dataIndex: "operator",
      key: "operator",
      width: 120,
    },
    {
      title: "操作类型",
      dataIndex: "operation",
      key: "operation",
      width: 120,
    },
    {
      title: "资源类型",
      dataIndex: "resourceType",
      key: "resourceType",
      width: 120,
    },
    {
      title: "资源ID",
      dataIndex: "resourceId",
      key: "resourceId",
      width: 150,
    },
    {
      title: "状态",
      dataIndex: "status",
      key: "status",
      width: 80,
      render: (status: string) => (
        <Tag color={status === "success" ? "green" : "red"}>
          {status === "success" ? "成功" : "失败"}
        </Tag>
      ),
    },
    {
      title: "详情",
      dataIndex: "detail",
      key: "detail",
      ellipsis: true,
    },
  ];

  return (
    <div>
      <div style={{ marginBottom: 16 }}>
        <Button
          type="primary"
          icon={<PlusOutlined />}
          onClick={() => handleOpenModal()}
        >
          添加云账号
        </Button>
      </div>

      <Table
        columns={columns}
        dataSource={accounts}
        rowKey="id"
        loading={loading}
        scroll={{ x: 1500 }}
        pagination={{
          showSizeChanger: true,
          showQuickJumper: true,
          showTotal: (total) => `共 ${total} 条`,
        }}
      />

      {/* 创建/编辑模态框 */}
      <Modal
        title={editingAccount ? "编辑云账号" : "添加云账号"}
        open={modalVisible}
        onOk={handleSave}
        onCancel={() => setModalVisible(false)}
        width={600}
      >
        <Form form={form} layout="vertical">
          <Form.Item
            name="name"
            label="账号名称"
            rules={[{ required: true, message: "请输入账号名称" }]}
          >
            <Input placeholder="请输入账号名称" />
          </Form.Item>
          <Form.Item
            name="provider"
            label="云厂商"
            rules={[{ required: true, message: "请选择云厂商" }]}
          >
            <Select placeholder="请选择云厂商">
              {Object.entries(providerConfig).map(([key, value]) => (
                <Option key={key} value={key}>
                  <Tag color={value.color}>{value.name}</Tag>
                </Option>
              ))}
            </Select>
          </Form.Item>
          <Form.Item
            name="accessKeyId"
            label="Access Key ID"
            rules={[{ required: true, message: "请输入 Access Key ID" }]}
          >
            <Input placeholder="请输入 Access Key ID" />
          </Form.Item>
          <Form.Item
            name="accessKeySecret"
            label="Access Key Secret"
            rules={[
              {
                required: !editingAccount,
                message: "请输入 Access Key Secret",
              },
            ]}
          >
            <Input.Password placeholder="请输入 Access Key Secret" />
          </Form.Item>
          <Form.Item
            name="regions"
            label="区域"
            rules={[{ required: true, message: "请选择区域" }]}
          >
            <Select mode="multiple" placeholder="请选择区域">
              <Option value="cn-hangzhou">华东1（杭州）</Option>
              <Option value="cn-shanghai">华东2（上海）</Option>
              <Option value="cn-beijing">华北2（北京）</Option>
              <Option value="cn-shenzhen">华南1（深圳）</Option>
              <Option value="us-east-1">美国东部（弗吉尼亚）</Option>
              <Option value="us-west-1">美国西部（硅谷）</Option>
              <Option value="ap-southeast-1">亚太东南（新加坡）</Option>
              <Option value="eu-west-1">欧洲（爱尔兰）</Option>
            </Select>
          </Form.Item>
        </Form>
      </Modal>

      {/* 权限配置抽屉 */}
      <Drawer
        title={`${selectedAccount?.name || ""} - 权限配置`}
        placement="right"
        width={600}
        onClose={() => setPermissionDrawerVisible(false)}
        open={permissionDrawerVisible}
      >
        <Tabs
          items={[
            {
              key: "permissions",
              label: "权限列表",
              children: (
                <Table
                  columns={[
                    { title: "用户", dataIndex: "userName", key: "userName" },
                    {
                      title: "操作权限",
                      dataIndex: "operations",
                      key: "operations",
                      render: (ops: string[]) => ops.join(", "),
                    },
                    {
                      title: "资源范围",
                      dataIndex: "resourceScope",
                      key: "resourceScope",
                      render: (scope: string[]) => scope.join(", "),
                    },
                  ]}
                  dataSource={permissions}
                  rowKey="id"
                  size="small"
                />
              ),
            },
            {
              key: "add",
              label: "添加权限",
              children: (
                <Form form={permissionForm} layout="vertical">
                  <Form.Item
                    name="userId"
                    label="用户"
                    rules={[{ required: true, message: "请选择用户" }]}
                  >
                    <Select placeholder="请选择用户">
                      <Option value="user1">张三</Option>
                      <Option value="user2">李四</Option>
                      <Option value="user3">王五</Option>
                    </Select>
                  </Form.Item>
                  <Form.Item
                    name="operations"
                    label="操作权限"
                    rules={[{ required: true, message: "请选择操作权限" }]}
                  >
                    <Select mode="multiple" placeholder="请选择操作权限">
                      <Option value="view">查看</Option>
                      <Option value="operate">操作</Option>
                      <Option value="admin">管理</Option>
                    </Select>
                  </Form.Item>
                  <Form.Item
                    name="resourceScope"
                    label="资源范围"
                    rules={[{ required: true, message: "请选择资源范围" }]}
                  >
                    <Select mode="multiple" placeholder="请选择资源范围">
                      {selectedAccount?.regions?.map((r: string) => (
                        <Option key={r} value={r}>
                          {r}
                        </Option>
                      ))}
                    </Select>
                  </Form.Item>
                  <Form.Item>
                    <Button
                      type="primary"
                      onClick={async () => {
                        try {
                          const values = await permissionForm.validateFields();
                          await setAccountPermission(
                            selectedAccount!.id,
                            values,
                          );
                          message.success("添加权限成功");
                          const data = await getAccountPermissions(
                            selectedAccount!.id,
                          );
                          setPermissions(data);
                          permissionForm.resetFields();
                        } catch {
                          message.error("添加权限失败");
                        }
                      }}
                    >
                      添加
                    </Button>
                  </Form.Item>
                </Form>
              ),
            },
          ]}
        />
      </Drawer>

      {/* 审计日志抽屉 */}
      <Drawer
        title={`${selectedAccount?.name || ""} - 审计日志`}
        placement="right"
        width={800}
        onClose={() => setAuditDrawerVisible(false)}
        open={auditDrawerVisible}
      >
        <Table
          columns={auditColumns}
          dataSource={auditLogs}
          rowKey="id"
          loading={auditLoading}
          size="small"
          pagination={{ pageSize: 20 }}
        />
      </Drawer>

      {/* 详情抽屉 */}
      <Drawer
        title={`${selectedAccount?.name || ""} - 详情`}
        placement="right"
        width={600}
        onClose={() => setDetailDrawerVisible(false)}
        open={detailDrawerVisible}
      >
        {selectedAccount && (
          <Descriptions column={1} bordered>
            <Descriptions.Item label="账号ID">
              {selectedAccount.id}
            </Descriptions.Item>
            <Descriptions.Item label="账号名称">
              {selectedAccount.name}
            </Descriptions.Item>
            <Descriptions.Item label="云厂商">
              <Tag color={providerConfig[selectedAccount.provider]?.color}>
                {providerConfig[selectedAccount.provider]?.name}
              </Tag>
            </Descriptions.Item>
            <Descriptions.Item label="Access Key ID">
              {selectedAccount.accessKeyId}
            </Descriptions.Item>
            <Descriptions.Item label="区域">
              {selectedAccount.regions?.join(", ")}
            </Descriptions.Item>
            <Descriptions.Item label="状态">
              <Badge
                status={statusConfig[selectedAccount.status]?.status}
                text={statusConfig[selectedAccount.status]?.text}
              />
            </Descriptions.Item>
            <Descriptions.Item label="API连通性">
              {selectedAccount.apiConnectivity ? (
                <Tag color="green">正常</Tag>
              ) : (
                <Tag color="red">异常</Tag>
              )}
            </Descriptions.Item>
            <Descriptions.Item label="配额使用">
              {selectedAccount.quotaUsed && selectedAccount.quotaLimit && (
                <div>
                  {Object.entries(selectedAccount.quotaUsed).map(
                    ([key, value]) => (
                      <div key={key} style={{ marginBottom: 8 }}>
                        <span>{key}: </span>
                        <Progress
                          percent={Math.round(
                            (value / (selectedAccount.quotaLimit?.[key] || 1)) *
                              100,
                          )}
                          size="small"
                          style={{ width: 200, display: "inline-block" }}
                        />
                        <span>
                          {" "}
                          {value}/{selectedAccount.quotaLimit?.[key]}
                        </span>
                      </div>
                    ),
                  )}
                </div>
              )}
            </Descriptions.Item>
            <Descriptions.Item label="权限过期">
              {selectedAccount.permissionExpiry
                ? dayjs(selectedAccount.permissionExpiry).format("YYYY-MM-DD")
                : "-"}
            </Descriptions.Item>
            <Descriptions.Item label="最后同步">
              {selectedAccount.lastSyncTime
                ? dayjs(selectedAccount.lastSyncTime).format(
                    "YYYY-MM-DD HH:mm:ss",
                  )
                : "-"}
            </Descriptions.Item>
            <Descriptions.Item label="创建时间">
              {dayjs(selectedAccount.createdAt).format("YYYY-MM-DD HH:mm:ss")}
            </Descriptions.Item>
            <Descriptions.Item label="创建人">
              {selectedAccount.createdBy}
            </Descriptions.Item>
          </Descriptions>
        )}
      </Drawer>
    </div>
  );
};

export default CloudAccountPage;
