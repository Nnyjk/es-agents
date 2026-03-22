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
  Tabs,
  Descriptions,
  Alert,
} from "antd";
import {
  PlayCircleOutlined,
  PauseCircleOutlined,
  ReloadOutlined,
  DeleteOutlined,
  SettingOutlined,
  TagOutlined,
  PlusOutlined,
  CloudOutlined,
  GlobalOutlined,
} from "@ant-design/icons";
import type { ColumnsType } from "antd/es/table";
import dayjs from "dayjs";
import {
  getCloudResources,
  batchOperation,
  startResource,
  stopResource,
  restartResource,
  terminateResource,
  resizeResource,
  getCloudImages,
  deleteCloudImage,
  getNetworkConfigs,
  createNetworkConfig,
  deleteNetworkConfig,
} from "@/services/multicloud";
import type {
  CloudResource,
  CloudProvider,
  CloudResourceType,
  CloudResourceStatus,
  CloudImage,
  CloudNetworkConfig,
  BatchOperationType,
  PageResult,
} from "@/types/multicloud";

const { Option } = Select;
const { TextArea } = Input;

// 云厂商显示配置
const providerConfig: Record<CloudProvider, { name: string; color: string }> = {
  aliyun: { name: "阿里云", color: "#FF6A00" },
  tencent: { name: "腾讯云", color: "#00A3FF" },
  huawei: { name: "华为云", color: "#C00" },
  aws: { name: "AWS", color: "#FF9900" },
  azure: { name: "Azure", color: "#00BCF2" },
  vmware: { name: "VMware", color: "#607078" },
  openstack: { name: "OpenStack", color: "#ED1944" },
};

// 资源状态显示配置
const statusConfig: Record<
  CloudResourceStatus,
  { text: string; color: string }
> = {
  running: { text: "运行中", color: "green" },
  stopped: { text: "已停止", color: "default" },
  pending: { text: "处理中", color: "blue" },
  terminated: { text: "已销毁", color: "red" },
  error: { text: "异常", color: "red" },
};

// 网络配置类型
const networkTypeConfig = {
  vpc_peering: { name: "VPC对等连接", icon: <GlobalOutlined /> },
  direct_connect: { name: "专线连接", icon: <CloudOutlined /> },
  vpn: { name: "VPN连接", icon: <GlobalOutlined /> },
};

const CrossCloudOperationPage: React.FC = () => {
  const [resources, setResources] = useState<CloudResource[]>([]);
  const [selectedRowKeys, setSelectedRowKeys] = useState<React.Key[]>([]);
  const [loading, setLoading] = useState(false);
  const [batchModalVisible, setBatchModalVisible] = useState(false);
  const [resizeModalVisible, setResizeModalVisible] = useState(false);
  const [tagModalVisible, setTagModalVisible] = useState(false);
  const [networkModalVisible, setNetworkModalVisible] = useState(false);
  const [selectedResource, setSelectedResource] =
    useState<CloudResource | null>(null);
  const [images, setImages] = useState<CloudImage[]>([]);
  const [networks, setNetworks] = useState<CloudNetworkConfig[]>([]);
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 10,
    total: 0,
  });
  const [batchOperationType, setBatchOperationType] =
    useState<BatchOperationType | null>(null);
  const [batchForm] = Form.useForm();
  const [resizeForm] = Form.useForm();
  const [tagForm] = Form.useForm();
  const [networkForm] = Form.useForm();

  // 加载资源列表
  const loadResources = async (current = 1, pageSize = 10) => {
    setLoading(true);
    try {
      const result: PageResult<CloudResource> = await getCloudResources({
        current,
        pageSize,
      });
      setResources(result.data);
      setPagination({ current, pageSize, total: result.total });
    } catch {
      // 使用模拟数据
      const mockData: CloudResource[] = [
        {
          id: "1",
          accountId: "acc-001",
          accountName: "生产环境-阿里云",
          provider: "aliyun" as CloudProvider,
          region: "cn-hangzhou",
          resourceType: "ecs" as CloudResourceType,
          resourceId: "i-bp1234567890abcdef",
          resourceName: "web-server-01",
          status: "running" as CloudResourceStatus,
          config: { cpu: 4, memory: 16, disk: 100 },
          tags: { env: "production", app: "web" },
          cost: 456.78,
          createdAt: "2024-01-15T10:00:00Z",
          updatedAt: "2024-03-20T15:30:00Z",
        },
        {
          id: "2",
          accountId: "acc-002",
          accountName: "生产环境-腾讯云",
          provider: "tencent" as CloudProvider,
          region: "ap-guangzhou",
          resourceType: "rds" as CloudResourceType,
          resourceId: "cdb-abc123",
          resourceName: "mysql-master",
          status: "running" as CloudResourceStatus,
          config: { engine: "MySQL", version: "8.0", storage: 500 },
          tags: { env: "production", app: "database" },
          cost: 1234.56,
          createdAt: "2024-01-10T08:00:00Z",
          updatedAt: "2024-03-19T12:00:00Z",
        },
        {
          id: "3",
          accountId: "acc-001",
          accountName: "生产环境-阿里云",
          provider: "aliyun" as CloudProvider,
          region: "cn-shanghai",
          resourceType: "ecs" as CloudResourceType,
          resourceId: "i-bp0987654321fedcba",
          resourceName: "api-server-01",
          status: "stopped" as CloudResourceStatus,
          config: { cpu: 8, memory: 32, disk: 200 },
          tags: { env: "staging", app: "api" },
          cost: 789.12,
          createdAt: "2024-02-01T09:00:00Z",
          updatedAt: "2024-03-18T16:45:00Z",
        },
      ];
      setResources(mockData);
      setPagination({ current, pageSize, total: 50 });
    } finally {
      setLoading(false);
    }
  };

  // 加载镜像列表
  const loadImages = async () => {
    try {
      const result = await getCloudImages({});
      setImages(result.data);
    } catch {
      // 使用模拟数据
      setImages([
        {
          id: "1",
          accountId: "acc-001",
          accountName: "生产环境-阿里云",
          provider: "aliyun" as CloudProvider,
          region: "cn-hangzhou",
          imageId: "img-abc123",
          imageName: "centos-7-base",
          type: "image",
          size: 50,
          status: "available",
          createdAt: "2024-01-15T10:00:00Z",
        },
        {
          id: "2",
          accountId: "acc-001",
          accountName: "生产环境-阿里云",
          provider: "aliyun" as CloudProvider,
          region: "cn-hangzhou",
          imageId: "snap-xyz789",
          imageName: "web-server-snapshot",
          type: "snapshot",
          size: 120,
          status: "available",
          createdAt: "2024-03-10T15:00:00Z",
        },
      ]);
    }
  };

  // 加载网络配置
  const loadNetworks = async () => {
    try {
      const data = await getNetworkConfigs();
      setNetworks(data);
    } catch {
      // 使用模拟数据
      setNetworks([
        {
          id: "1",
          accountId: "acc-001",
          accountName: "生产环境-阿里云",
          provider: "aliyun" as CloudProvider,
          type: "vpc_peering",
          name: "跨区域VPC对等",
          sourceRegion: "cn-hangzhou",
          targetRegion: "cn-shanghai",
          status: "active",
          bandwidth: 1000,
          config: {},
          createdAt: "2024-01-20T10:00:00Z",
        },
        {
          id: "2",
          accountId: "acc-002",
          accountName: "生产环境-腾讯云",
          provider: "tencent" as CloudProvider,
          type: "direct_connect",
          name: "专线连接阿里云",
          sourceRegion: "ap-guangzhou",
          targetRegion: "cn-shenzhen",
          status: "active",
          bandwidth: 500,
          config: {},
          createdAt: "2024-02-15T14:00:00Z",
        },
      ]);
    }
  };

  useEffect(() => {
    loadResources();
    loadImages();
    loadNetworks();
  }, []);

  // 执行单个资源操作
  const handleSingleOperation = async (id: string, operation: string) => {
    try {
      let result;
      switch (operation) {
        case "start":
          result = await startResource(id);
          break;
        case "stop":
          result = await stopResource(id);
          break;
        case "restart":
          result = await restartResource(id);
          break;
        case "terminate":
          result = await terminateResource(id);
          break;
        default:
          return;
      }
      if (result.success) {
        message.success("操作成功");
        loadResources(pagination.current, pagination.pageSize);
      } else {
        message.error(`操作失败: ${result.message}`);
      }
    } catch {
      message.error("操作失败");
    }
  };

  // 执行批量操作
  const handleBatchOperation = async () => {
    if (selectedRowKeys.length === 0) {
      message.warning("请先选择资源");
      return;
    }

    try {
      const values = await batchForm.validateFields();
      const result = await batchOperation({
        resourceIds: selectedRowKeys as string[],
        operation: batchOperationType as BatchOperationType,
        params: values,
      });
      if (result.success.length > 0) {
        message.success(`成功操作 ${result.success.length} 个资源`);
      }
      if (result.failed.length > 0) {
        message.error(`${result.failed.length} 个资源操作失败`);
      }
      setBatchModalVisible(false);
      setSelectedRowKeys([]);
      loadResources(pagination.current, pagination.pageSize);
    } catch {
      message.error("批量操作失败");
    }
  };

  // 调整配置
  const handleResize = async () => {
    if (!selectedResource) return;
    try {
      const values = await resizeForm.validateFields();
      const result = await resizeResource(selectedResource.id, values);
      if (result.success) {
        message.success("配置调整成功");
        setResizeModalVisible(false);
        loadResources(pagination.current, pagination.pageSize);
      } else {
        message.error(`配置调整失败: ${result.message}`);
      }
    } catch {
      message.error("配置调整失败");
    }
  };

  // 批量打标签
  const handleBatchTag = async () => {
    if (selectedRowKeys.length === 0) {
      message.warning("请先选择资源");
      return;
    }

    try {
      const values = await tagForm.validateFields();
      const result = await batchOperation({
        resourceIds: selectedRowKeys as string[],
        operation: "tag" as BatchOperationType,
        params: values,
      });
      if (result.success.length > 0) {
        message.success(`成功打标签 ${result.success.length} 个资源`);
      }
      setTagModalVisible(false);
      setSelectedRowKeys([]);
      loadResources(pagination.current, pagination.pageSize);
    } catch {
      message.error("批量打标签失败");
    }
  };

  // 删除镜像
  const handleDeleteImage = async (id: string) => {
    try {
      const result = await deleteCloudImage(id);
      if (result.success) {
        message.success("删除成功");
        loadImages();
      } else {
        message.error(`删除失败: ${result.message}`);
      }
    } catch {
      message.error("删除失败");
    }
  };

  // 创建网络配置
  const handleCreateNetwork = async () => {
    try {
      const values = await networkForm.validateFields();
      await createNetworkConfig(values);
      message.success("创建成功");
      setNetworkModalVisible(false);
      loadNetworks();
    } catch {
      message.error("创建失败");
    }
  };

  // 删除网络配置
  const handleDeleteNetwork = async (id: string) => {
    try {
      const result = await deleteNetworkConfig(id);
      if (result.success) {
        message.success("删除成功");
        loadNetworks();
      } else {
        message.error(`删除失败: ${result.message}`);
      }
    } catch {
      message.error("删除失败");
    }
  };

  // 表格列定义
  const columns: ColumnsType<CloudResource> = [
    {
      title: "资源名称",
      dataIndex: "resourceName",
      key: "resourceName",
      width: 180,
    },
    {
      title: "资源ID",
      dataIndex: "resourceId",
      key: "resourceId",
      width: 200,
      ellipsis: true,
    },
    {
      title: "云厂商",
      dataIndex: "provider",
      key: "provider",
      width: 100,
      render: (provider: CloudProvider) => (
        <Tag color={providerConfig[provider]?.color || "#666"}>
          {providerConfig[provider]?.name || provider}
        </Tag>
      ),
    },
    {
      title: "区域",
      dataIndex: "region",
      key: "region",
      width: 120,
    },
    {
      title: "类型",
      dataIndex: "resourceType",
      key: "resourceType",
      width: 100,
      render: (type: string) => {
        const typeMap: Record<string, string> = {
          ecs: "云主机",
          rds: "数据库",
          oss: "存储",
          vpc: "网络",
          container: "容器",
        };
        return typeMap[type] || type;
      },
    },
    {
      title: "状态",
      dataIndex: "status",
      key: "status",
      width: 100,
      render: (status: CloudResourceStatus) => (
        <Tag color={statusConfig[status]?.color}>
          {statusConfig[status]?.text}
        </Tag>
      ),
    },
    {
      title: "操作",
      key: "action",
      width: 280,
      render: (_: unknown, record: CloudResource) => (
        <Space size="small">
          {record.status === "stopped" && (
            <Button
              type="link"
              size="small"
              icon={<PlayCircleOutlined />}
              onClick={() => handleSingleOperation(record.id, "start")}
            >
              启动
            </Button>
          )}
          {record.status === "running" && (
            <>
              <Button
                type="link"
                size="small"
                icon={<PauseCircleOutlined />}
                onClick={() => handleSingleOperation(record.id, "stop")}
              >
                停止
              </Button>
              <Button
                type="link"
                size="small"
                icon={<ReloadOutlined />}
                onClick={() => handleSingleOperation(record.id, "restart")}
              >
                重启
              </Button>
            </>
          )}
          <Button
            type="link"
            size="small"
            icon={<SettingOutlined />}
            onClick={() => {
              setSelectedResource(record);
              setResizeModalVisible(true);
            }}
          >
            配置
          </Button>
          <Popconfirm
            title="确定要销毁此资源吗？此操作不可恢复！"
            onConfirm={() => handleSingleOperation(record.id, "terminate")}
          >
            <Button type="link" size="small" danger icon={<DeleteOutlined />}>
              销毁
            </Button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  // 镜像列表列定义
  const imageColumns: ColumnsType<CloudImage> = [
    {
      title: "名称",
      dataIndex: "imageName",
      key: "imageName",
      width: 180,
    },
    {
      title: "类型",
      dataIndex: "type",
      key: "type",
      width: 100,
      render: (type: string) => (
        <Tag color={type === "image" ? "blue" : "green"}>
          {type === "image" ? "镜像" : "快照"}
        </Tag>
      ),
    },
    {
      title: "云厂商",
      dataIndex: "provider",
      key: "provider",
      width: 100,
      render: (provider: CloudProvider) => (
        <Tag color={providerConfig[provider]?.color}>
          {providerConfig[provider]?.name}
        </Tag>
      ),
    },
    {
      title: "区域",
      dataIndex: "region",
      key: "region",
      width: 120,
    },
    {
      title: "大小",
      dataIndex: "size",
      key: "size",
      width: 100,
      render: (size: number) => `${size} GB`,
    },
    {
      title: "状态",
      dataIndex: "status",
      key: "status",
      width: 100,
      render: (status: string) => (
        <Tag color={status === "available" ? "green" : "orange"}>
          {status === "available" ? "可用" : status}
        </Tag>
      ),
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
      width: 100,
      render: (_: unknown, record: CloudImage) => (
        <Popconfirm
          title="确定要删除此镜像/快照吗？"
          onConfirm={() => handleDeleteImage(record.id)}
        >
          <Button type="link" size="small" danger icon={<DeleteOutlined />}>
            删除
          </Button>
        </Popconfirm>
      ),
    },
  ];

  // 网络配置列定义
  const networkColumns: ColumnsType<CloudNetworkConfig> = [
    {
      title: "名称",
      dataIndex: "name",
      key: "name",
      width: 180,
    },
    {
      title: "类型",
      dataIndex: "type",
      key: "type",
      width: 120,
      render: (type: keyof typeof networkTypeConfig) => (
        <span>
          {networkTypeConfig[type]?.icon} {networkTypeConfig[type]?.name}
        </span>
      ),
    },
    {
      title: "云厂商",
      dataIndex: "provider",
      key: "provider",
      width: 100,
      render: (provider: CloudProvider) => (
        <Tag color={providerConfig[provider]?.color}>
          {providerConfig[provider]?.name}
        </Tag>
      ),
    },
    {
      title: "源区域",
      dataIndex: "sourceRegion",
      key: "sourceRegion",
      width: 120,
    },
    {
      title: "目标区域",
      dataIndex: "targetRegion",
      key: "targetRegion",
      width: 120,
    },
    {
      title: "带宽",
      dataIndex: "bandwidth",
      key: "bandwidth",
      width: 100,
      render: (bw: number) => `${bw} Mbps`,
    },
    {
      title: "状态",
      dataIndex: "status",
      key: "status",
      width: 100,
      render: (status: string) => (
        <Tag color={status === "active" ? "green" : "orange"}>
          {status === "active" ? "正常" : status}
        </Tag>
      ),
    },
    {
      title: "操作",
      key: "action",
      width: 100,
      render: (_: unknown, record: CloudNetworkConfig) => (
        <Popconfirm
          title="确定要删除此网络配置吗？"
          onConfirm={() => handleDeleteNetwork(record.id)}
        >
          <Button type="link" size="small" danger icon={<DeleteOutlined />}>
            删除
          </Button>
        </Popconfirm>
      ),
    },
  ];

  return (
    <div>
      <Tabs
        items={[
          {
            key: "resources",
            label: "资源操作",
            children: (
              <Card>
                <div style={{ marginBottom: 16 }}>
                  <Space>
                    <Button
                      type="primary"
                      icon={<PlayCircleOutlined />}
                      disabled={selectedRowKeys.length === 0}
                      onClick={() => {
                        setBatchOperationType("start" as BatchOperationType);
                        setBatchModalVisible(true);
                      }}
                    >
                      批量启动
                    </Button>
                    <Button
                      icon={<PauseCircleOutlined />}
                      disabled={selectedRowKeys.length === 0}
                      onClick={() => {
                        setBatchOperationType("stop" as BatchOperationType);
                        setBatchModalVisible(true);
                      }}
                    >
                      批量停止
                    </Button>
                    <Button
                      icon={<ReloadOutlined />}
                      disabled={selectedRowKeys.length === 0}
                      onClick={() => {
                        setBatchOperationType("restart" as BatchOperationType);
                        setBatchModalVisible(true);
                      }}
                    >
                      批量重启
                    </Button>
                    <Button
                      icon={<TagOutlined />}
                      disabled={selectedRowKeys.length === 0}
                      onClick={() => setTagModalVisible(true)}
                    >
                      批量打标签
                    </Button>
                    <Popconfirm
                      title={`确定要销毁选中的 ${selectedRowKeys.length} 个资源吗？此操作不可恢复！`}
                      onConfirm={() => {
                        setBatchOperationType(
                          "terminate" as BatchOperationType,
                        );
                        batchForm.submit();
                      }}
                    >
                      <Button
                        danger
                        disabled={selectedRowKeys.length === 0}
                        icon={<DeleteOutlined />}
                      >
                        批量销毁
                      </Button>
                    </Popconfirm>
                  </Space>
                  <span style={{ marginLeft: 16, color: "#999" }}>
                    已选择 {selectedRowKeys.length} 个资源
                  </span>
                </div>

                <Table
                  columns={columns}
                  dataSource={resources}
                  rowKey="id"
                  loading={loading}
                  scroll={{ x: 1300 }}
                  rowSelection={{
                    selectedRowKeys,
                    onChange: setSelectedRowKeys,
                  }}
                  pagination={{
                    ...pagination,
                    showSizeChanger: true,
                    showQuickJumper: true,
                    showTotal: (total) => `共 ${total} 条`,
                    onChange: (page, pageSize) => loadResources(page, pageSize),
                  }}
                />
              </Card>
            ),
          },
          {
            key: "images",
            label: "镜像/快照管理",
            children: (
              <Card>
                <Table
                  columns={imageColumns}
                  dataSource={images}
                  rowKey="id"
                  scroll={{ x: 1000 }}
                  pagination={{ pageSize: 10 }}
                />
              </Card>
            ),
          },
          {
            key: "networks",
            label: "跨云网络配置",
            children: (
              <Card>
                <div style={{ marginBottom: 16 }}>
                  <Button
                    type="primary"
                    icon={<PlusOutlined />}
                    onClick={() => setNetworkModalVisible(true)}
                  >
                    新建网络配置
                  </Button>
                </div>
                <Table
                  columns={networkColumns}
                  dataSource={networks}
                  rowKey="id"
                  scroll={{ x: 1000 }}
                  pagination={{ pageSize: 10 }}
                />
              </Card>
            ),
          },
        ]}
      />

      {/* 批量操作确认模态框 */}
      <Modal
        title={`批量${batchOperationType === "start" ? "启动" : batchOperationType === "stop" ? "停止" : batchOperationType === "restart" ? "重启" : "操作"}`}
        open={batchModalVisible}
        onOk={handleBatchOperation}
        onCancel={() => setBatchModalVisible(false)}
      >
        <Alert
          message={`将对 ${selectedRowKeys.length} 个资源执行${batchOperationType === "start" ? "启动" : batchOperationType === "stop" ? "停止" : batchOperationType === "restart" ? "重启" : "操作"}操作`}
          type="warning"
          showIcon
          style={{ marginBottom: 16 }}
        />
        <Form form={batchForm} layout="vertical">
          {batchOperationType === "resize" && (
            <>
              <Form.Item name="cpu" label="CPU核心数">
                <InputNumber min={1} max={128} style={{ width: "100%" }} />
              </Form.Item>
              <Form.Item name="memory" label="内存大小(GB)">
                <InputNumber min={1} max={1024} style={{ width: "100%" }} />
              </Form.Item>
            </>
          )}
        </Form>
      </Modal>

      {/* 配置调整模态框 */}
      <Modal
        title="调整资源配置"
        open={resizeModalVisible}
        onOk={handleResize}
        onCancel={() => setResizeModalVisible(false)}
      >
        {selectedResource && (
          <Descriptions
            column={1}
            bordered
            size="small"
            style={{ marginBottom: 16 }}
          >
            <Descriptions.Item label="资源名称">
              {selectedResource.resourceName}
            </Descriptions.Item>
            <Descriptions.Item label="当前配置">
              CPU: {String(selectedResource.config?.cpu || "-")} 核 | 内存:{" "}
              {String(selectedResource.config?.memory || "-")} GB | 磁盘:{" "}
              {String(selectedResource.config?.disk || "-")} GB
            </Descriptions.Item>
          </Descriptions>
        )}
        <Form form={resizeForm} layout="vertical">
          <Form.Item name="cpu" label="CPU核心数">
            <InputNumber
              min={1}
              max={128}
              style={{ width: "100%" }}
              placeholder="不修改请留空"
            />
          </Form.Item>
          <Form.Item name="memory" label="内存大小(GB)">
            <InputNumber
              min={1}
              max={1024}
              style={{ width: "100%" }}
              placeholder="不修改请留空"
            />
          </Form.Item>
          <Form.Item name="disk" label="磁盘大小(GB)">
            <InputNumber
              min={1}
              max={10240}
              style={{ width: "100%" }}
              placeholder="不修改请留空"
            />
          </Form.Item>
        </Form>
      </Modal>

      {/* 批量打标签模态框 */}
      <Modal
        title={`批量打标签 (${selectedRowKeys.length} 个资源)`}
        open={tagModalVisible}
        onOk={handleBatchTag}
        onCancel={() => setTagModalVisible(false)}
      >
        <Form form={tagForm} layout="vertical">
          <Form.Item
            name="tags"
            label="标签"
            rules={[{ required: true, message: "请输入标签" }]}
          >
            <TextArea placeholder="每行一个标签，格式: key=value" rows={5} />
          </Form.Item>
        </Form>
      </Modal>

      {/* 新建网络配置模态框 */}
      <Modal
        title="新建网络配置"
        open={networkModalVisible}
        onOk={handleCreateNetwork}
        onCancel={() => setNetworkModalVisible(false)}
        width={600}
      >
        <Form form={networkForm} layout="vertical">
          <Form.Item name="name" label="配置名称" rules={[{ required: true }]}>
            <Input placeholder="请输入配置名称" />
          </Form.Item>
          <Form.Item name="type" label="连接类型" rules={[{ required: true }]}>
            <Select placeholder="请选择连接类型">
              <Option value="vpc_peering">VPC对等连接</Option>
              <Option value="direct_connect">专线连接</Option>
              <Option value="vpn">VPN连接</Option>
            </Select>
          </Form.Item>
          <Form.Item
            name="sourceRegion"
            label="源区域"
            rules={[{ required: true }]}
          >
            <Select placeholder="请选择源区域">
              <Option value="cn-hangzhou">华东1（杭州）</Option>
              <Option value="cn-shanghai">华东2（上海）</Option>
              <Option value="cn-beijing">华北2（北京）</Option>
              <Option value="cn-shenzhen">华南1（深圳）</Option>
            </Select>
          </Form.Item>
          <Form.Item
            name="targetRegion"
            label="目标区域"
            rules={[{ required: true }]}
          >
            <Select placeholder="请选择目标区域">
              <Option value="cn-hangzhou">华东1（杭州）</Option>
              <Option value="cn-shanghai">华东2（上海）</Option>
              <Option value="cn-beijing">华北2（北京）</Option>
              <Option value="cn-shenzhen">华南1（深圳）</Option>
            </Select>
          </Form.Item>
          <Form.Item
            name="bandwidth"
            label="带宽(Mbps)"
            rules={[{ required: true }]}
          >
            <InputNumber min={1} max={10000} style={{ width: "100%" }} />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default CrossCloudOperationPage;
