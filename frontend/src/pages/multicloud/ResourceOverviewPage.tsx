import React, { useState, useEffect } from "react";
import {
  Card,
  Row,
  Col,
  Statistic,
  Table,
  Tag,
  Select,
  Space,
  Input,
  Button,
  Drawer,
  Descriptions,
  Timeline,
  Tabs,
} from "antd";
import {
  CloudServerOutlined,
  DatabaseOutlined,
  FolderOutlined,
  GlobalOutlined,
  ContainerOutlined,
} from "@ant-design/icons";
import type { ColumnsType } from "antd/es/table";
import dayjs from "dayjs";
import ReactECharts from "echarts-for-react";
import {
  getMultiCloudStatistics,
  getCloudResources,
  getResourceOperationHistory,
} from "@/services/multicloud";
import type {
  MultiCloudStatistics,
  CloudResource,
  CloudProvider,
  CloudResourceType,
  CloudResourceStatus,
  AccountAuditLog,
  PageResult,
} from "@/types/multicloud";

const { Option } = Select;
const { Search } = Input;

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

// 资源类型显示配置
const resourceTypeConfig: Record<
  CloudResourceType,
  { name: string; icon: React.ReactNode }
> = {
  ecs: { name: "云主机", icon: <CloudServerOutlined /> },
  rds: { name: "数据库", icon: <DatabaseOutlined /> },
  oss: { name: "存储", icon: <FolderOutlined /> },
  vpc: { name: "网络", icon: <GlobalOutlined /> },
  container: { name: "容器", icon: <ContainerOutlined /> },
  loadbalancer: { name: "负载均衡", icon: <GlobalOutlined /> },
  eip: { name: "弹性IP", icon: <GlobalOutlined /> },
  other: { name: "其他", icon: <CloudServerOutlined /> },
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

const ResourceOverviewPage: React.FC = () => {
  const [statistics, setStatistics] = useState<MultiCloudStatistics | null>(
    null,
  );
  const [resources, setResources] = useState<CloudResource[]>([]);
  const [loading, setLoading] = useState(false);
  const [tableLoading, setTableLoading] = useState(false);
  const [detailDrawerVisible, setDetailDrawerVisible] = useState(false);
  const [selectedResource, setSelectedResource] =
    useState<CloudResource | null>(null);
  const [operationHistory, setOperationHistory] = useState<AccountAuditLog[]>(
    [],
  );
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 10,
    total: 0,
  });
  const [filters, setFilters] = useState<{
    provider?: CloudProvider;
    resourceType?: CloudResourceType;
    status?: CloudResourceStatus;
    keyword?: string;
  }>({});

  // 加载统计数据
  const loadStatistics = async () => {
    setLoading(true);
    try {
      const data = await getMultiCloudStatistics();
      setStatistics(data);
    } catch {
      // 使用模拟数据
      setStatistics({
        totalResources: 1286,
        byProvider: {
          aliyun: 523,
          tencent: 312,
          huawei: 198,
          aws: 156,
          azure: 67,
          vmware: 18,
          openstack: 12,
        },
        byRegion: {
          "cn-hangzhou": 312,
          "cn-shanghai": 256,
          "cn-beijing": 198,
          "us-east-1": 156,
          "ap-southeast-1": 134,
        },
        byType: {
          ecs: 456,
          rds: 234,
          oss: 312,
          vpc: 123,
          container: 89,
          loadbalancer: 56,
          eip: 34,
          other: 12,
        },
        byStatus: {
          running: 1024,
          stopped: 156,
          pending: 45,
          terminated: 34,
          error: 27,
        },
        totalCost: 156789.12,
        costByProvider: {
          aliyun: 67890.23,
          tencent: 34567.45,
          huawei: 23456.78,
          aws: 19876.34,
          azure: 8765.21,
          vmware: 1567.89,
          openstack: 654.22,
        },
      });
    } finally {
      setLoading(false);
    }
  };

  // 加载资源列表
  const loadResources = async (current = 1, pageSize = 10) => {
    setTableLoading(true);
    try {
      const result: PageResult<CloudResource> = await getCloudResources({
        current,
        pageSize,
        ...filters,
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
      ];
      setResources(mockData);
      setPagination({ current, pageSize, total: 100 });
    } finally {
      setTableLoading(false);
    }
  };

  useEffect(() => {
    loadStatistics();
    loadResources();
  }, []);

  // 查看资源详情
  const handleViewDetail = async (resource: CloudResource) => {
    setSelectedResource(resource);
    setDetailDrawerVisible(true);
    try {
      const history = await getResourceOperationHistory(resource.id);
      setOperationHistory(history);
    } catch {
      // 使用模拟数据
      setOperationHistory([
        {
          id: "1",
          accountId: resource.accountId,
          accountName: resource.accountName,
          operator: "admin",
          operation: "START",
          resourceType: resource.resourceType,
          resourceId: resource.resourceId,
          detail: "启动实例",
          status: "success",
          createdAt: "2024-03-20T10:00:00Z",
        },
        {
          id: "2",
          accountId: resource.accountId,
          accountName: resource.accountName,
          operator: "admin",
          operation: "RESIZE",
          resourceType: resource.resourceType,
          resourceId: resource.resourceId,
          detail: "调整配置: 4C8G -> 4C16G",
          status: "success",
          createdAt: "2024-03-18T14:30:00Z",
        },
      ]);
    }
  };

  // 表格列定义
  const columns: ColumnsType<CloudResource> = [
    {
      title: "资源名称",
      dataIndex: "resourceName",
      key: "resourceName",
      width: 180,
      render: (text: string, record: CloudResource) => (
        <a onClick={() => handleViewDetail(record)}>{text}</a>
      ),
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
      title: "账号",
      dataIndex: "accountName",
      key: "accountName",
      width: 150,
      ellipsis: true,
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
      render: (type: CloudResourceType) => {
        const config = resourceTypeConfig[type];
        return (
          <span>
            {config?.icon} {config?.name || type}
          </span>
        );
      },
    },
    {
      title: "状态",
      dataIndex: "status",
      key: "status",
      width: 100,
      render: (status: CloudResourceStatus) => {
        const config = statusConfig[status];
        return <Tag color={config?.color}>{config?.text || status}</Tag>;
      },
    },
    {
      title: "月费用",
      dataIndex: "cost",
      key: "cost",
      width: 100,
      render: (cost: number) => `¥${cost?.toFixed(2) || "0"}`,
    },
    {
      title: "标签",
      dataIndex: "tags",
      key: "tags",
      width: 150,
      render: (tags: Record<string, string>) => (
        <Space size={4} wrap>
          {Object.entries(tags || {}).map(([key, value]) => (
            <Tag key={key} style={{ margin: 0 }}>
              {key}: {value}
            </Tag>
          ))}
        </Space>
      ),
    },
    {
      title: "更新时间",
      dataIndex: "updatedAt",
      key: "updatedAt",
      width: 160,
      render: (time: string) => dayjs(time).format("YYYY-MM-DD HH:mm"),
    },
  ];

  // 云厂商资源分布饼图
  const getProviderPieOption = () => ({
    title: {
      text: "云厂商资源分布",
      left: "center",
      textStyle: { fontSize: 14 },
    },
    tooltip: { trigger: "item", formatter: "{b}: {c} ({d}%)" },
    legend: { orient: "vertical", left: "left", top: "middle" },
    series: [
      {
        type: "pie",
        radius: ["40%", "70%"],
        center: ["60%", "50%"],
        data: Object.entries(statistics?.byProvider || {}).map(
          ([key, value]) => ({
            name: providerConfig[key as CloudProvider]?.name || key,
            value,
            itemStyle: { color: providerConfig[key as CloudProvider]?.color },
          }),
        ),
        label: { show: false },
        emphasis: { label: { show: true, fontSize: 14, fontWeight: "bold" } },
      },
    ],
  });

  // 资源类型分布饼图
  const getResourceTypePieOption = () => ({
    title: {
      text: "资源类型分布",
      left: "center",
      textStyle: { fontSize: 14 },
    },
    tooltip: { trigger: "item", formatter: "{b}: {c} ({d}%)" },
    legend: { orient: "vertical", left: "left", top: "middle" },
    series: [
      {
        type: "pie",
        radius: ["40%", "70%"],
        center: ["60%", "50%"],
        data: Object.entries(statistics?.byType || {}).map(([key, value]) => ({
          name: resourceTypeConfig[key as CloudResourceType]?.name || key,
          value,
        })),
        label: { show: false },
        emphasis: { label: { show: true, fontSize: 14, fontWeight: "bold" } },
      },
    ],
  });

  // 资源状态分布饼图
  const getStatusPieOption = () => ({
    title: {
      text: "资源状态分布",
      left: "center",
      textStyle: { fontSize: 14 },
    },
    tooltip: { trigger: "item", formatter: "{b}: {c} ({d}%)" },
    legend: { orient: "vertical", left: "left", top: "middle" },
    series: [
      {
        type: "pie",
        radius: ["40%", "70%"],
        center: ["60%", "50%"],
        data: Object.entries(statistics?.byStatus || {}).map(
          ([key, value]) => ({
            name: statusConfig[key as CloudResourceStatus]?.text || key,
            value,
            itemStyle: {
              color: statusConfig[key as CloudResourceStatus]?.color,
            },
          }),
        ),
        label: { show: false },
        emphasis: { label: { show: true, fontSize: 14, fontWeight: "bold" } },
      },
    ],
  });

  // 云厂商成本分布饼图
  const getCostPieOption = () => ({
    title: {
      text: "云厂商成本分布",
      left: "center",
      textStyle: { fontSize: 14 },
    },
    tooltip: { trigger: "item", formatter: "{b}: ¥{c} ({d}%)" },
    legend: { orient: "vertical", left: "left", top: "middle" },
    series: [
      {
        type: "pie",
        radius: ["40%", "70%"],
        center: ["60%", "50%"],
        data: Object.entries(statistics?.costByProvider || {}).map(
          ([key, value]) => ({
            name: providerConfig[key as CloudProvider]?.name || key,
            value: value?.toFixed(2),
            itemStyle: { color: providerConfig[key as CloudProvider]?.color },
          }),
        ),
        label: { show: false },
        emphasis: { label: { show: true, fontSize: 14, fontWeight: "bold" } },
      },
    ],
  });

  return (
    <div>
      {/* 统计卡片 */}
      <Row gutter={16} style={{ marginBottom: 16 }}>
        <Col span={6}>
          <Card loading={loading}>
            <Statistic
              title="资源总数"
              value={statistics?.totalResources || 0}
              prefix={<CloudServerOutlined />}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card loading={loading}>
            <Statistic
              title="运行中"
              value={statistics?.byStatus?.running || 0}
              valueStyle={{ color: "#52c41a" }}
              prefix={<CloudServerOutlined />}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card loading={loading}>
            <Statistic
              title="异常资源"
              value={statistics?.byStatus?.error || 0}
              valueStyle={{ color: "#ff4d4f" }}
              prefix={<CloudServerOutlined />}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card loading={loading}>
            <Statistic
              title="月度总费用"
              value={statistics?.totalCost || 0}
              precision={2}
              prefix="¥"
            />
          </Card>
        </Col>
      </Row>

      {/* 图表区域 */}
      <Row gutter={16} style={{ marginBottom: 16 }}>
        <Col span={6}>
          <Card size="small">
            <ReactECharts
              option={getProviderPieOption()}
              style={{ height: 250 }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card size="small">
            <ReactECharts
              option={getResourceTypePieOption()}
              style={{ height: 250 }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card size="small">
            <ReactECharts
              option={getStatusPieOption()}
              style={{ height: 250 }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card size="small">
            <ReactECharts option={getCostPieOption()} style={{ height: 250 }} />
          </Card>
        </Col>
      </Row>

      {/* 资源列表 */}
      <Card title="资源列表">
        <div style={{ marginBottom: 16 }}>
          <Space>
            <Select
              placeholder="云厂商"
              style={{ width: 120 }}
              allowClear
              onChange={(value) => setFilters({ ...filters, provider: value })}
            >
              {Object.entries(providerConfig).map(([key, value]) => (
                <Option key={key} value={key}>
                  <Tag color={value.color} style={{ marginRight: 0 }}>
                    {value.name}
                  </Tag>
                </Option>
              ))}
            </Select>
            <Select
              placeholder="资源类型"
              style={{ width: 120 }}
              allowClear
              onChange={(value) =>
                setFilters({ ...filters, resourceType: value })
              }
            >
              {Object.entries(resourceTypeConfig).map(([key, value]) => (
                <Option key={key} value={key}>
                  {value.name}
                </Option>
              ))}
            </Select>
            <Select
              placeholder="状态"
              style={{ width: 120 }}
              allowClear
              onChange={(value) => setFilters({ ...filters, status: value })}
            >
              {Object.entries(statusConfig).map(([key, value]) => (
                <Option key={key} value={key}>
                  {value.text}
                </Option>
              ))}
            </Select>
            <Search
              placeholder="搜索资源名称/ID"
              style={{ width: 200 }}
              onSearch={(value) => setFilters({ ...filters, keyword: value })}
            />
            <Button
              type="primary"
              onClick={() => loadResources(1, pagination.pageSize)}
            >
              查询
            </Button>
          </Space>
        </div>
        <Table
          columns={columns}
          dataSource={resources}
          rowKey="id"
          loading={tableLoading}
          scroll={{ x: 1500 }}
          pagination={{
            ...pagination,
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (total) => `共 ${total} 条`,
            onChange: (page, pageSize) => loadResources(page, pageSize),
          }}
        />
      </Card>

      {/* 资源详情抽屉 */}
      <Drawer
        title={`${selectedResource?.resourceName || ""} - 详情`}
        placement="right"
        width={700}
        onClose={() => setDetailDrawerVisible(false)}
        open={detailDrawerVisible}
      >
        {selectedResource && (
          <Tabs
            items={[
              {
                key: "info",
                label: "基本信息",
                children: (
                  <Descriptions column={2} bordered>
                    <Descriptions.Item label="资源ID">
                      {selectedResource.resourceId}
                    </Descriptions.Item>
                    <Descriptions.Item label="资源名称">
                      {selectedResource.resourceName}
                    </Descriptions.Item>
                    <Descriptions.Item label="云厂商">
                      <Tag
                        color={providerConfig[selectedResource.provider]?.color}
                      >
                        {providerConfig[selectedResource.provider]?.name}
                      </Tag>
                    </Descriptions.Item>
                    <Descriptions.Item label="账号">
                      {selectedResource.accountName}
                    </Descriptions.Item>
                    <Descriptions.Item label="区域">
                      {selectedResource.region}
                    </Descriptions.Item>
                    <Descriptions.Item label="类型">
                      {resourceTypeConfig[selectedResource.resourceType]?.name}
                    </Descriptions.Item>
                    <Descriptions.Item label="状态">
                      <Tag color={statusConfig[selectedResource.status]?.color}>
                        {statusConfig[selectedResource.status]?.text}
                      </Tag>
                    </Descriptions.Item>
                    <Descriptions.Item label="月费用">
                      ¥{selectedResource.cost?.toFixed(2)}
                    </Descriptions.Item>
                    <Descriptions.Item label="配置" span={2}>
                      <pre>
                        {JSON.stringify(selectedResource.config, null, 2)}
                      </pre>
                    </Descriptions.Item>
                    <Descriptions.Item label="标签" span={2}>
                      <Space size={4} wrap>
                        {Object.entries(selectedResource.tags || {}).map(
                          ([key, value]) => (
                            <Tag key={key}>
                              {key}: {value}
                            </Tag>
                          ),
                        )}
                      </Space>
                    </Descriptions.Item>
                    <Descriptions.Item label="创建时间">
                      {dayjs(selectedResource.createdAt).format(
                        "YYYY-MM-DD HH:mm:ss",
                      )}
                    </Descriptions.Item>
                    <Descriptions.Item label="更新时间">
                      {dayjs(selectedResource.updatedAt).format(
                        "YYYY-MM-DD HH:mm:ss",
                      )}
                    </Descriptions.Item>
                  </Descriptions>
                ),
              },
              {
                key: "history",
                label: "操作历史",
                children: (
                  <Timeline
                    items={operationHistory.map((log) => ({
                      color: log.status === "success" ? "green" : "red",
                      children: (
                        <div>
                          <div>
                            <strong>{log.operation}</strong> - {log.detail}
                          </div>
                          <div style={{ color: "#999", fontSize: 12 }}>
                            {log.operator} |{" "}
                            {dayjs(log.createdAt).format("YYYY-MM-DD HH:mm:ss")}
                          </div>
                        </div>
                      ),
                    }))}
                  />
                ),
              },
            ]}
          />
        )}
      </Drawer>
    </div>
  );
};

export default ResourceOverviewPage;
