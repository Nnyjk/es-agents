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
  Switch,
  message,
  Popconfirm,
  Tabs,
  Divider,
} from "antd";
import {
  PlusOutlined,
  SyncOutlined,
  SafetyOutlined,
  ThunderboltOutlined,
  ClusterOutlined,
} from "@ant-design/icons";
import type { ColumnsType } from "antd/es/table";
import {
  getBackupPolicies,
  createBackupPolicy,
  updateBackupPolicy,
  deleteBackupPolicy,
  getDisasterRecoveryConfigs,
  createDisasterRecoveryConfig,
  updateDisasterRecoveryConfig,
  deleteDisasterRecoveryConfig,
  getScalingPolicies,
  createScalingPolicy,
  updateScalingPolicy,
  deleteScalingPolicy,
  getLoadBalancingConfigs,
  createLoadBalancingConfig,
  updateLoadBalancingConfig,
  deleteLoadBalancingConfig,
  getCloudAccounts,
} from "@/services/multicloud";
import type {
  CrossCloudBackupPolicy,
  DisasterRecoveryConfig,
  ScalingPolicy,
  ScalingRule,
  LoadBalancingConfig,
  LoadBalancingBackend,
  CloudAccount,
  CloudProvider,
  CloudResourceType,
} from "@/types/multicloud";

const { Option } = Select;

// 云厂商显示配置
const providerConfig: Record<CloudProvider, { name: string; color: string }> = {
  aliyun: { name: "阿里云", color: "orange" },
  tencent: { name: "腾讯云", color: "blue" },
  huawei: { name: "华为云", color: "red" },
  aws: { name: "AWS", color: "volcano" },
  azure: { name: "Azure", color: "cyan" },
  vmware: { name: "VMware", color: "purple" },
  openstack: { name: "OpenStack", color: "geekblue" },
};

// 资源类型显示配置
const resourceTypeConfig: Record<CloudResourceType, { name: string }> = {
  ecs: { name: "云主机" },
  rds: { name: "数据库" },
  oss: { name: "存储" },
  vpc: { name: "网络" },
  container: { name: "容器" },
  loadbalancer: { name: "负载均衡" },
  eip: { name: "弹性IP" },
  other: { name: "其他" },
};

// 备份策略调度配置
const scheduleOptions = [
  { value: "0 0 * * *", label: "每天 00:00" },
  { value: "0 2 * * *", label: "每天 02:00" },
  { value: "0 4 * * *", label: "每天 04:00" },
  { value: "0 0 * * 0", label: "每周日 00:00" },
  { value: "0 0 1 * *", label: "每月 1 日 00:00" },
];

const ScheduleManagementPage: React.FC = () => {
  const [activeTab, setActiveTab] = useState("backup");
  const [loading, setLoading] = useState(false);

  // 备份策略状态
  const [backupPolicies, setBackupPolicies] = useState<
    CrossCloudBackupPolicy[]
  >([]);
  const [backupModalVisible, setBackupModalVisible] = useState(false);
  const [editingBackupPolicy, setEditingBackupPolicy] =
    useState<CrossCloudBackupPolicy | null>(null);

  // 容灾配置状态
  const [disasterConfigs, setDisasterConfigs] = useState<
    DisasterRecoveryConfig[]
  >([]);
  const [disasterModalVisible, setDisasterModalVisible] = useState(false);
  const [editingDisasterConfig, setEditingDisasterConfig] =
    useState<DisasterRecoveryConfig | null>(null);

  // 弹性伸缩状态
  const [scalingPolicies, setScalingPolicies] = useState<ScalingPolicy[]>([]);
  const [scalingModalVisible, setScalingModalVisible] = useState(false);
  const [editingScalingPolicy, setEditingScalingPolicy] =
    useState<ScalingPolicy | null>(null);

  // 负载调度状态
  const [loadBalancingConfigs, setLoadBalancingConfigs] = useState<
    LoadBalancingConfig[]
  >([]);
  const [loadBalancingModalVisible, setLoadBalancingModalVisible] =
    useState(false);
  const [editingLoadBalancingConfig, setEditingLoadBalancingConfig] =
    useState<LoadBalancingConfig | null>(null);

  // 云账号列表
  const [cloudAccounts, setCloudAccounts] = useState<CloudAccount[]>([]);

  const [backupForm] = Form.useForm();
  const [disasterForm] = Form.useForm();
  const [scalingForm] = Form.useForm();
  const [loadBalancingForm] = Form.useForm();

  // 加载云账号列表
  useEffect(() => {
    loadCloudAccounts();
  }, []);

  const loadCloudAccounts = async () => {
    try {
      const accounts = await getCloudAccounts();
      setCloudAccounts(accounts);
    } catch (error) {
      console.error("加载云账号失败:", error);
    }
  };

  // 加载备份数据
  useEffect(() => {
    if (activeTab === "backup") {
      loadBackupPolicies();
    } else if (activeTab === "disaster") {
      loadDisasterConfigs();
    } else if (activeTab === "scaling") {
      loadScalingPolicies();
    } else if (activeTab === "loadbalancing") {
      loadLoadBalancingConfigs();
    }
  }, [activeTab]);

  // ========== 备份策略 ==========
  const loadBackupPolicies = async () => {
    setLoading(true);
    try {
      const data = await getBackupPolicies();
      setBackupPolicies(data);
    } catch (error) {
      message.error("加载备份策略失败");
    } finally {
      setLoading(false);
    }
  };

  const handleCreateBackupPolicy = () => {
    setEditingBackupPolicy(null);
    backupForm.resetFields();
    setBackupModalVisible(true);
  };

  const handleEditBackupPolicy = (record: CrossCloudBackupPolicy) => {
    setEditingBackupPolicy(record);
    backupForm.setFieldsValue(record);
    setBackupModalVisible(true);
  };

  const handleSaveBackupPolicy = async () => {
    try {
      const values = await backupForm.validateFields();
      if (editingBackupPolicy) {
        await updateBackupPolicy(editingBackupPolicy.id, values);
        message.success("更新备份策略成功");
      } else {
        await createBackupPolicy(values);
        message.success("创建备份策略成功");
      }
      setBackupModalVisible(false);
      loadBackupPolicies();
    } catch (error) {
      message.error("保存备份策略失败");
    }
  };

  const handleDeleteBackupPolicy = async (id: string) => {
    try {
      await deleteBackupPolicy(id);
      message.success("删除备份策略成功");
      loadBackupPolicies();
    } catch (error) {
      message.error("删除备份策略失败");
    }
  };

  const backupColumns: ColumnsType<CrossCloudBackupPolicy> = [
    {
      title: "策略名称",
      dataIndex: "name",
      key: "name",
      width: 150,
    },
    {
      title: "源账号",
      key: "source",
      width: 180,
      render: (_, record) => (
        <Space direction="vertical" size={0}>
          <span>{record.sourceAccountName}</span>
          <span style={{ fontSize: 12, color: "#999" }}>
            {record.sourceRegion}
          </span>
        </Space>
      ),
    },
    {
      title: "目标账号",
      key: "target",
      width: 180,
      render: (_, record) => (
        <Space direction="vertical" size={0}>
          <span>{record.targetAccountName}</span>
          <span style={{ fontSize: 12, color: "#999" }}>
            {record.targetRegion}
          </span>
        </Space>
      ),
    },
    {
      title: "资源数量",
      dataIndex: "resourceIds",
      key: "resourceIds",
      width: 100,
      render: (ids: string[]) => <Tag color="blue">{ids?.length || 0} 个</Tag>,
    },
    {
      title: "调度周期",
      dataIndex: "schedule",
      key: "schedule",
      width: 120,
      render: (schedule: string) => {
        const option = scheduleOptions.find((o) => o.value === schedule);
        return option?.label || schedule;
      },
    },
    {
      title: "保留天数",
      dataIndex: "retentionDays",
      key: "retentionDays",
      width: 100,
      render: (days: number) => `${days} 天`,
    },
    {
      title: "状态",
      dataIndex: "enabled",
      key: "enabled",
      width: 80,
      render: (enabled: boolean) => (
        <Tag color={enabled ? "green" : "default"}>
          {enabled ? "启用" : "停用"}
        </Tag>
      ),
    },
    {
      title: "下次执行",
      dataIndex: "nextRunTime",
      key: "nextRunTime",
      width: 160,
      render: (time: string) => (time ? new Date(time).toLocaleString() : "-"),
    },
    {
      title: "操作",
      key: "action",
      width: 150,
      render: (_, record) => (
        <Space>
          <Button
            type="link"
            size="small"
            onClick={() => handleEditBackupPolicy(record)}
          >
            编辑
          </Button>
          <Popconfirm
            title="确定要删除此备份策略吗？"
            onConfirm={() => handleDeleteBackupPolicy(record.id)}
          >
            <Button type="link" size="small" danger>
              删除
            </Button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  // ========== 容灾配置 ==========
  const loadDisasterConfigs = async () => {
    setLoading(true);
    try {
      const data = await getDisasterRecoveryConfigs();
      setDisasterConfigs(data);
    } catch (error) {
      message.error("加载容灾配置失败");
    } finally {
      setLoading(false);
    }
  };

  const handleCreateDisasterConfig = () => {
    setEditingDisasterConfig(null);
    disasterForm.resetFields();
    setDisasterModalVisible(true);
  };

  const handleEditDisasterConfig = (record: DisasterRecoveryConfig) => {
    setEditingDisasterConfig(record);
    disasterForm.setFieldsValue(record);
    setDisasterModalVisible(true);
  };

  const handleSaveDisasterConfig = async () => {
    try {
      const values = await disasterForm.validateFields();
      if (editingDisasterConfig) {
        await updateDisasterRecoveryConfig(editingDisasterConfig.id, values);
        message.success("更新容灾配置成功");
      } else {
        await createDisasterRecoveryConfig(values);
        message.success("创建容灾配置成功");
      }
      setDisasterModalVisible(false);
      loadDisasterConfigs();
    } catch (error) {
      message.error("保存容灾配置失败");
    }
  };

  const handleDeleteDisasterConfig = async (id: string) => {
    try {
      await deleteDisasterRecoveryConfig(id);
      message.success("删除容灾配置成功");
      loadDisasterConfigs();
    } catch (error) {
      message.error("删除容灾配置失败");
    }
  };

  const disasterColumns: ColumnsType<DisasterRecoveryConfig> = [
    {
      title: "配置名称",
      dataIndex: "name",
      key: "name",
      width: 150,
    },
    {
      title: "主站点",
      key: "primary",
      width: 180,
      render: (_, record) => (
        <Space direction="vertical" size={0}>
          <span>{record.primaryAccountName}</span>
          <span style={{ fontSize: 12, color: "#999" }}>
            {record.primaryRegion}
          </span>
        </Space>
      ),
    },
    {
      title: "备站点",
      key: "standby",
      width: 180,
      render: (_, record) => (
        <Space direction="vertical" size={0}>
          <span>{record.standbyAccountName}</span>
          <span style={{ fontSize: 12, color: "#999" }}>
            {record.standbyRegion}
          </span>
        </Space>
      ),
    },
    {
      title: "复制模式",
      dataIndex: "replicationMode",
      key: "replicationMode",
      width: 100,
      render: (mode: string) => (
        <Tag color={mode === "sync" ? "green" : "blue"}>
          {mode === "sync" ? "同步" : "异步"}
        </Tag>
      ),
    },
    {
      title: "故障切换",
      dataIndex: "failoverMode",
      key: "failoverMode",
      width: 100,
      render: (mode: string) => (
        <Tag color={mode === "auto" ? "green" : "orange"}>
          {mode === "auto" ? "自动" : "手动"}
        </Tag>
      ),
    },
    {
      title: "健康检查",
      dataIndex: "healthCheckInterval",
      key: "healthCheckInterval",
      width: 100,
      render: (interval: number) => `${interval}s`,
    },
    {
      title: "状态",
      dataIndex: "enabled",
      key: "enabled",
      width: 80,
      render: (enabled: boolean) => (
        <Tag color={enabled ? "green" : "default"}>
          {enabled ? "启用" : "停用"}
        </Tag>
      ),
    },
    {
      title: "操作",
      key: "action",
      width: 150,
      render: (_, record) => (
        <Space>
          <Button
            type="link"
            size="small"
            onClick={() => handleEditDisasterConfig(record)}
          >
            编辑
          </Button>
          <Popconfirm
            title="确定要删除此容灾配置吗？"
            onConfirm={() => handleDeleteDisasterConfig(record.id)}
          >
            <Button type="link" size="small" danger>
              删除
            </Button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  // ========== 弹性伸缩 ==========
  const loadScalingPolicies = async () => {
    setLoading(true);
    try {
      const data = await getScalingPolicies();
      setScalingPolicies(data);
    } catch (error) {
      message.error("加载弹性伸缩策略失败");
    } finally {
      setLoading(false);
    }
  };

  const handleCreateScalingPolicy = () => {
    setEditingScalingPolicy(null);
    scalingForm.resetFields();
    setScalingModalVisible(true);
  };

  const handleEditScalingPolicy = (record: ScalingPolicy) => {
    setEditingScalingPolicy(record);
    scalingForm.setFieldsValue(record);
    setScalingModalVisible(true);
  };

  const handleSaveScalingPolicy = async () => {
    try {
      const values = await scalingForm.validateFields();
      if (editingScalingPolicy) {
        await updateScalingPolicy(editingScalingPolicy.id, values);
        message.success("更新弹性伸缩策略成功");
      } else {
        await createScalingPolicy(values);
        message.success("创建弹性伸缩策略成功");
      }
      setScalingModalVisible(false);
      loadScalingPolicies();
    } catch (error) {
      message.error("保存弹性伸缩策略失败");
    }
  };

  const handleDeleteScalingPolicy = async (id: string) => {
    try {
      await deleteScalingPolicy(id);
      message.success("删除弹性伸缩策略成功");
      loadScalingPolicies();
    } catch (error) {
      message.error("删除弹性伸缩策略失败");
    }
  };

  const scalingColumns: ColumnsType<ScalingPolicy> = [
    {
      title: "策略名称",
      dataIndex: "name",
      key: "name",
      width: 150,
    },
    {
      title: "云账号",
      key: "account",
      width: 150,
      render: (_, record) => <span>{record.accountName}</span>,
    },
    {
      title: "区域",
      dataIndex: "region",
      key: "region",
      width: 120,
    },
    {
      title: "资源类型",
      dataIndex: "resourceType",
      key: "resourceType",
      width: 100,
      render: (type: CloudResourceType) =>
        resourceTypeConfig[type]?.name || type,
    },
    {
      title: "实例范围",
      key: "range",
      width: 120,
      render: (_, record) => (
        <span>
          {record.minSize} ~ {record.maxSize}
        </span>
      ),
    },
    {
      title: "期望数量",
      dataIndex: "desiredSize",
      key: "desiredSize",
      width: 100,
    },
    {
      title: "伸缩规则",
      dataIndex: "scalingRules",
      key: "scalingRules",
      width: 200,
      render: (rules: ScalingRule[]) => (
        <Space direction="vertical" size={0}>
          {rules?.slice(0, 2).map((rule, idx) => (
            <Tag
              key={idx}
              color={rule.action === "scale_out" ? "green" : "orange"}
            >
              {rule.name}
            </Tag>
          ))}
          {rules?.length > 2 && <Tag>+{rules.length - 2}</Tag>}
        </Space>
      ),
    },
    {
      title: "状态",
      dataIndex: "enabled",
      key: "enabled",
      width: 80,
      render: (enabled: boolean) => (
        <Tag color={enabled ? "green" : "default"}>
          {enabled ? "启用" : "停用"}
        </Tag>
      ),
    },
    {
      title: "操作",
      key: "action",
      width: 150,
      render: (_, record) => (
        <Space>
          <Button
            type="link"
            size="small"
            onClick={() => handleEditScalingPolicy(record)}
          >
            编辑
          </Button>
          <Popconfirm
            title="确定要删除此弹性伸缩策略吗？"
            onConfirm={() => handleDeleteScalingPolicy(record.id)}
          >
            <Button type="link" size="small" danger>
              删除
            </Button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  // ========== 负载调度 ==========
  const loadLoadBalancingConfigs = async () => {
    setLoading(true);
    try {
      const data = await getLoadBalancingConfigs();
      setLoadBalancingConfigs(data);
    } catch (error) {
      message.error("加载负载调度配置失败");
    } finally {
      setLoading(false);
    }
  };

  const handleCreateLoadBalancingConfig = () => {
    setEditingLoadBalancingConfig(null);
    loadBalancingForm.resetFields();
    setLoadBalancingModalVisible(true);
  };

  const handleEditLoadBalancingConfig = (record: LoadBalancingConfig) => {
    setEditingLoadBalancingConfig(record);
    loadBalancingForm.setFieldsValue(record);
    setLoadBalancingModalVisible(true);
  };

  const handleSaveLoadBalancingConfig = async () => {
    try {
      const values = await loadBalancingForm.validateFields();
      if (editingLoadBalancingConfig) {
        await updateLoadBalancingConfig(editingLoadBalancingConfig.id, values);
        message.success("更新负载调度配置成功");
      } else {
        await createLoadBalancingConfig(values);
        message.success("创建负载调度配置成功");
      }
      setLoadBalancingModalVisible(false);
      loadLoadBalancingConfigs();
    } catch (error) {
      message.error("保存负载调度配置失败");
    }
  };

  const handleDeleteLoadBalancingConfig = async (id: string) => {
    try {
      await deleteLoadBalancingConfig(id);
      message.success("删除负载调度配置成功");
      loadLoadBalancingConfigs();
    } catch (error) {
      message.error("删除负载调度配置失败");
    }
  };

  const loadBalancingColumns: ColumnsType<LoadBalancingConfig> = [
    {
      title: "配置名称",
      dataIndex: "name",
      key: "name",
      width: 150,
    },
    {
      title: "调度类型",
      dataIndex: "type",
      key: "type",
      width: 120,
      render: (type: string) => {
        const typeMap: Record<string, { label: string; color: string }> = {
          weighted: { label: "加权轮询", color: "blue" },
          least_conn: { label: "最小连接", color: "green" },
          geo: { label: "地理路由", color: "purple" },
        };
        const config = typeMap[type] || { label: type, color: "default" };
        return <Tag color={config.color}>{config.label}</Tag>;
      },
    },
    {
      title: "后端节点",
      dataIndex: "backends",
      key: "backends",
      width: 200,
      render: (backends: LoadBalancingBackend[]) => (
        <Space direction="vertical" size={0}>
          {backends?.slice(0, 2).map((backend, idx) => (
            <Space key={idx} size={4}>
              <Tag color={providerConfig[backend.provider]?.color}>
                {providerConfig[backend.provider]?.name}
              </Tag>
              <span style={{ fontSize: 12 }}>{backend.region}</span>
              <Tag color={backend.healthy ? "green" : "red"}>
                {backend.healthy ? "健康" : "异常"}
              </Tag>
            </Space>
          ))}
          {backends?.length > 2 && <Tag>+{backends.length - 2}</Tag>}
        </Space>
      ),
    },
    {
      title: "健康检查",
      key: "healthCheck",
      width: 150,
      render: (_, record) => (
        <Space direction="vertical" size={0}>
          <span style={{ fontSize: 12 }}>路径: {record.healthCheck?.path}</span>
          <span style={{ fontSize: 12, color: "#999" }}>
            间隔: {record.healthCheck?.interval}s
          </span>
        </Space>
      ),
    },
    {
      title: "状态",
      dataIndex: "enabled",
      key: "enabled",
      width: 80,
      render: (enabled: boolean) => (
        <Tag color={enabled ? "green" : "default"}>
          {enabled ? "启用" : "停用"}
        </Tag>
      ),
    },
    {
      title: "操作",
      key: "action",
      width: 150,
      render: (_, record) => (
        <Space>
          <Button
            type="link"
            size="small"
            onClick={() => handleEditLoadBalancingConfig(record)}
          >
            编辑
          </Button>
          <Popconfirm
            title="确定要删除此负载调度配置吗？"
            onConfirm={() => handleDeleteLoadBalancingConfig(record.id)}
          >
            <Button type="link" size="small" danger>
              删除
            </Button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <div>
      <Tabs
        activeKey={activeTab}
        onChange={setActiveTab}
        items={[
          {
            key: "backup",
            label: (
              <span>
                <SafetyOutlined />
                跨云备份策略
              </span>
            ),
            children: (
              <Card
                title="跨云备份策略"
                extra={
                  <Button
                    type="primary"
                    icon={<PlusOutlined />}
                    onClick={handleCreateBackupPolicy}
                  >
                    新建策略
                  </Button>
                }
              >
                <Table
                  columns={backupColumns}
                  dataSource={backupPolicies}
                  rowKey="id"
                  loading={loading}
                  pagination={{ pageSize: 10 }}
                />
              </Card>
            ),
          },
          {
            key: "disaster",
            label: (
              <span>
                <ClusterOutlined />
                容灾配置
              </span>
            ),
            children: (
              <Card
                title="容灾配置"
                extra={
                  <Button
                    type="primary"
                    icon={<PlusOutlined />}
                    onClick={handleCreateDisasterConfig}
                  >
                    新建配置
                  </Button>
                }
              >
                <Table
                  columns={disasterColumns}
                  dataSource={disasterConfigs}
                  rowKey="id"
                  loading={loading}
                  pagination={{ pageSize: 10 }}
                />
              </Card>
            ),
          },
          {
            key: "scaling",
            label: (
              <span>
                <ThunderboltOutlined />
                弹性伸缩
              </span>
            ),
            children: (
              <Card
                title="弹性伸缩策略"
                extra={
                  <Button
                    type="primary"
                    icon={<PlusOutlined />}
                    onClick={handleCreateScalingPolicy}
                  >
                    新建策略
                  </Button>
                }
              >
                <Table
                  columns={scalingColumns}
                  dataSource={scalingPolicies}
                  rowKey="id"
                  loading={loading}
                  pagination={{ pageSize: 10 }}
                />
              </Card>
            ),
          },
          {
            key: "loadbalancing",
            label: (
              <span>
                <SyncOutlined />
                负载调度
              </span>
            ),
            children: (
              <Card
                title="负载调度配置"
                extra={
                  <Button
                    type="primary"
                    icon={<PlusOutlined />}
                    onClick={handleCreateLoadBalancingConfig}
                  >
                    新建配置
                  </Button>
                }
              >
                <Table
                  columns={loadBalancingColumns}
                  dataSource={loadBalancingConfigs}
                  rowKey="id"
                  loading={loading}
                  pagination={{ pageSize: 10 }}
                />
              </Card>
            ),
          },
        ]}
      />

      {/* 备份策略弹窗 */}
      <Modal
        title={editingBackupPolicy ? "编辑备份策略" : "新建备份策略"}
        open={backupModalVisible}
        onOk={handleSaveBackupPolicy}
        onCancel={() => setBackupModalVisible(false)}
        width={600}
      >
        <Form form={backupForm} layout="vertical">
          <Form.Item
            name="name"
            label="策略名称"
            rules={[{ required: true, message: "请输入策略名称" }]}
          >
            <Input placeholder="请输入策略名称" />
          </Form.Item>
          <Form.Item
            name="sourceAccountId"
            label="源云账号"
            rules={[{ required: true, message: "请选择源云账号" }]}
          >
            <Select placeholder="请选择源云账号">
              {cloudAccounts.map((account) => (
                <Option key={account.id} value={account.id}>
                  {account.name}
                </Option>
              ))}
            </Select>
          </Form.Item>
          <Form.Item
            name="sourceRegion"
            label="源区域"
            rules={[{ required: true, message: "请输入源区域" }]}
          >
            <Input placeholder="请输入源区域" />
          </Form.Item>
          <Form.Item
            name="targetAccountId"
            label="目标云账号"
            rules={[{ required: true, message: "请选择目标云账号" }]}
          >
            <Select placeholder="请选择目标云账号">
              {cloudAccounts.map((account) => (
                <Option key={account.id} value={account.id}>
                  {account.name}
                </Option>
              ))}
            </Select>
          </Form.Item>
          <Form.Item
            name="targetRegion"
            label="目标区域"
            rules={[{ required: true, message: "请输入目标区域" }]}
          >
            <Input placeholder="请输入目标区域" />
          </Form.Item>
          <Form.Item
            name="schedule"
            label="调度周期"
            rules={[{ required: true, message: "请选择调度周期" }]}
          >
            <Select placeholder="请选择调度周期">
              {scheduleOptions.map((opt) => (
                <Option key={opt.value} value={opt.value}>
                  {opt.label}
                </Option>
              ))}
            </Select>
          </Form.Item>
          <Form.Item
            name="retentionDays"
            label="保留天数"
            rules={[{ required: true, message: "请输入保留天数" }]}
          >
            <InputNumber min={1} max={365} style={{ width: "100%" }} />
          </Form.Item>
          <Form.Item
            name="enabled"
            label="启用状态"
            valuePropName="checked"
            initialValue={true}
          >
            <Switch checkedChildren="启用" unCheckedChildren="停用" />
          </Form.Item>
        </Form>
      </Modal>

      {/* 容灾配置弹窗 */}
      <Modal
        title={editingDisasterConfig ? "编辑容灾配置" : "新建容灾配置"}
        open={disasterModalVisible}
        onOk={handleSaveDisasterConfig}
        onCancel={() => setDisasterModalVisible(false)}
        width={600}
      >
        <Form form={disasterForm} layout="vertical">
          <Form.Item
            name="name"
            label="配置名称"
            rules={[{ required: true, message: "请输入配置名称" }]}
          >
            <Input placeholder="请输入配置名称" />
          </Form.Item>
          <Form.Item
            name="primaryAccountId"
            label="主站点云账号"
            rules={[{ required: true, message: "请选择主站点云账号" }]}
          >
            <Select placeholder="请选择主站点云账号">
              {cloudAccounts.map((account) => (
                <Option key={account.id} value={account.id}>
                  {account.name}
                </Option>
              ))}
            </Select>
          </Form.Item>
          <Form.Item
            name="primaryRegion"
            label="主站点区域"
            rules={[{ required: true, message: "请输入主站点区域" }]}
          >
            <Input placeholder="请输入主站点区域" />
          </Form.Item>
          <Form.Item
            name="standbyAccountId"
            label="备站点云账号"
            rules={[{ required: true, message: "请选择备站点云账号" }]}
          >
            <Select placeholder="请选择备站点云账号">
              {cloudAccounts.map((account) => (
                <Option key={account.id} value={account.id}>
                  {account.name}
                </Option>
              ))}
            </Select>
          </Form.Item>
          <Form.Item
            name="standbyRegion"
            label="备站点区域"
            rules={[{ required: true, message: "请输入备站点区域" }]}
          >
            <Input placeholder="请输入备站点区域" />
          </Form.Item>
          <Form.Item
            name="replicationMode"
            label="复制模式"
            rules={[{ required: true, message: "请选择复制模式" }]}
          >
            <Select placeholder="请选择复制模式">
              <Option value="sync">同步复制</Option>
              <Option value="async">异步复制</Option>
            </Select>
          </Form.Item>
          <Form.Item
            name="failoverMode"
            label="故障切换模式"
            rules={[{ required: true, message: "请选择故障切换模式" }]}
          >
            <Select placeholder="请选择故障切换模式">
              <Option value="manual">手动切换</Option>
              <Option value="auto">自动切换</Option>
            </Select>
          </Form.Item>
          <Form.Item
            name="healthCheckInterval"
            label="健康检查间隔(秒)"
            rules={[{ required: true, message: "请输入健康检查间隔" }]}
          >
            <InputNumber min={5} max={300} style={{ width: "100%" }} />
          </Form.Item>
          <Form.Item
            name="enabled"
            label="启用状态"
            valuePropName="checked"
            initialValue={true}
          >
            <Switch checkedChildren="启用" unCheckedChildren="停用" />
          </Form.Item>
        </Form>
      </Modal>

      {/* 弹性伸缩弹窗 */}
      <Modal
        title={editingScalingPolicy ? "编辑弹性伸缩策略" : "新建弹性伸缩策略"}
        open={scalingModalVisible}
        onOk={handleSaveScalingPolicy}
        onCancel={() => setScalingModalVisible(false)}
        width={700}
      >
        <Form form={scalingForm} layout="vertical">
          <Form.Item
            name="name"
            label="策略名称"
            rules={[{ required: true, message: "请输入策略名称" }]}
          >
            <Input placeholder="请输入策略名称" />
          </Form.Item>
          <Form.Item
            name="accountId"
            label="云账号"
            rules={[{ required: true, message: "请选择云账号" }]}
          >
            <Select placeholder="请选择云账号">
              {cloudAccounts.map((account) => (
                <Option key={account.id} value={account.id}>
                  {account.name}
                </Option>
              ))}
            </Select>
          </Form.Item>
          <Form.Item
            name="region"
            label="区域"
            rules={[{ required: true, message: "请输入区域" }]}
          >
            <Input placeholder="请输入区域" />
          </Form.Item>
          <Form.Item
            name="resourceType"
            label="资源类型"
            rules={[{ required: true, message: "请选择资源类型" }]}
          >
            <Select placeholder="请选择资源类型">
              {Object.entries(resourceTypeConfig).map(([key, value]) => (
                <Option key={key} value={key}>
                  {value.name}
                </Option>
              ))}
            </Select>
          </Form.Item>
          <Form.Item label="实例范围">
            <Space>
              <Form.Item name="minSize" noStyle rules={[{ required: true }]}>
                <InputNumber min={0} max={1000} placeholder="最小" />
              </Form.Item>
              <span>~</span>
              <Form.Item name="maxSize" noStyle rules={[{ required: true }]}>
                <InputNumber min={1} max={1000} placeholder="最大" />
              </Form.Item>
            </Space>
          </Form.Item>
          <Form.Item
            name="desiredSize"
            label="期望数量"
            rules={[{ required: true, message: "请输入期望数量" }]}
          >
            <InputNumber min={0} max={1000} style={{ width: "100%" }} />
          </Form.Item>
          <Form.Item
            name="enabled"
            label="启用状态"
            valuePropName="checked"
            initialValue={true}
          >
            <Switch checkedChildren="启用" unCheckedChildren="停用" />
          </Form.Item>
        </Form>
      </Modal>

      {/* 负载调度弹窗 */}
      <Modal
        title={
          editingLoadBalancingConfig ? "编辑负载调度配置" : "新建负载调度配置"
        }
        open={loadBalancingModalVisible}
        onOk={handleSaveLoadBalancingConfig}
        onCancel={() => setLoadBalancingModalVisible(false)}
        width={600}
      >
        <Form form={loadBalancingForm} layout="vertical">
          <Form.Item
            name="name"
            label="配置名称"
            rules={[{ required: true, message: "请输入配置名称" }]}
          >
            <Input placeholder="请输入配置名称" />
          </Form.Item>
          <Form.Item
            name="type"
            label="调度类型"
            rules={[{ required: true, message: "请选择调度类型" }]}
          >
            <Select placeholder="请选择调度类型">
              <Option value="weighted">加权轮询</Option>
              <Option value="least_conn">最小连接</Option>
              <Option value="geo">地理路由</Option>
            </Select>
          </Form.Item>
          <Divider>健康检查配置</Divider>
          <Form.Item label="检查路径" name={["healthCheck", "path"]}>
            <Input placeholder="/health" />
          </Form.Item>
          <Form.Item label="检查间隔(秒)" name={["healthCheck", "interval"]}>
            <InputNumber min={5} max={60} style={{ width: "100%" }} />
          </Form.Item>
          <Form.Item label="超时时间(秒)" name={["healthCheck", "timeout"]}>
            <InputNumber min={1} max={30} style={{ width: "100%" }} />
          </Form.Item>
          <Form.Item
            label="不健康阈值"
            name={["healthCheck", "unhealthyThreshold"]}
          >
            <InputNumber min={1} max={10} style={{ width: "100%" }} />
          </Form.Item>
          <Form.Item
            name="enabled"
            label="启用状态"
            valuePropName="checked"
            initialValue={true}
          >
            <Switch checkedChildren="启用" unCheckedChildren="停用" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default ScheduleManagementPage;
