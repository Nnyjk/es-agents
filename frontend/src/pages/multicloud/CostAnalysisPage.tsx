import React, { useState, useEffect } from "react";
import {
  Card,
  Row,
  Col,
  Statistic,
  Table,
  Tag,
  Space,
  Button,
  DatePicker,
  Tabs,
  Badge,
  Drawer,
  Descriptions,
  Alert,
  message,
} from "antd";
import {
  DollarOutlined,
  WarningOutlined,
  ThunderboltOutlined,
  DownloadOutlined,
} from "@ant-design/icons";
import type { ColumnsType } from "antd/es/table";
import ReactECharts from "echarts-for-react";
import dayjs from "dayjs";
import {
  getCostStatistics,
  getIdleResources,
  getReservedInstanceRecommendations,
  getBillingDetails,
  exportCostReport,
} from "@/services/multicloud";
import type {
  CostStatistics,
  CloudProvider,
  CloudResourceType,
  IdleResource,
  ReservedInstanceRecommendation,
  PageResult,
} from "@/types/multicloud";

const { RangePicker } = DatePicker;

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
const resourceTypeConfig: Record<string, { name: string }> = {
  ecs: { name: "云主机" },
  rds: { name: "数据库" },
  oss: { name: "存储" },
  vpc: { name: "网络" },
  container: { name: "容器" },
  loadbalancer: { name: "负载均衡" },
  eip: { name: "弹性IP" },
  other: { name: "其他" },
};

const CostAnalysisPage: React.FC = () => {
  const [statistics, setStatistics] = useState<CostStatistics | null>(null);
  const [idleResources, setIdleResources] = useState<IdleResource[]>([]);
  const [riRecommendations, setRIRecommendations] = useState<
    ReservedInstanceRecommendation[]
  >([]);
  const [billingData, setBillingData] = useState<unknown[]>([]);
  const [loading, setLoading] = useState(false);
  const [idleLoading, setIdleLoading] = useState(false);
  const [riLoading, setRILoading] = useState(false);
  const [billingLoading, setBillingLoading] = useState(false);
  const [dateRange, setDateRange] = useState<[dayjs.Dayjs, dayjs.Dayjs]>([
    dayjs().subtract(30, "days"),
    dayjs(),
  ]);
  const [idleDetailDrawerVisible, setIdleDetailDrawerVisible] = useState(false);
  const [selectedIdleResource, setSelectedIdleResource] =
    useState<IdleResource | null>(null);
  const [billingPagination, setBillingPagination] = useState({
    current: 1,
    pageSize: 10,
    total: 0,
  });

  // 加载成本统计
  const loadStatistics = async () => {
    setLoading(true);
    try {
      const data = await getCostStatistics({
        startTime: dateRange[0].format("YYYY-MM-DD"),
        endTime: dateRange[1].format("YYYY-MM-DD"),
      });
      setStatistics(data);
    } catch {
      // 使用模拟数据
      setStatistics({
        totalCost: 156789.12,
        byProvider: {
          aliyun: 67890.23,
          tencent: 34567.45,
          huawei: 23456.78,
          aws: 19876.34,
          azure: 8765.21,
          vmware: 1567.89,
          openstack: 654.22,
        },
        byRegion: {
          "cn-hangzhou": 45678.9,
          "cn-shanghai": 34567.89,
          "cn-beijing": 28901.23,
          "us-east-1": 19876.54,
        },
        byType: {
          ecs: 78901.23,
          rds: 34567.89,
          oss: 23456.78,
          vpc: 12345.67,
          container: 6789.12,
          loadbalancer: 4567.89,
          eip: 2345.67,
          other: 1234.56,
        },
        byProject: {
          production: 89012.34,
          staging: 45678.9,
          development: 22097.88,
        },
        trend: Array.from({ length: 30 }, (_, i) => ({
          date: dayjs()
            .subtract(29 - i, "days")
            .format("YYYY-MM-DD"),
          cost: Math.random() * 10000 + 3000,
        })),
        forecast: Array.from({ length: 7 }, (_, i) => ({
          date: dayjs()
            .add(i + 1, "days")
            .format("YYYY-MM-DD"),
          cost: Math.random() * 10000 + 4000,
        })),
      });
    } finally {
      setLoading(false);
    }
  };

  // 加载闲置资源
  const loadIdleResources = async (current = 1, pageSize = 10) => {
    setIdleLoading(true);
    try {
      const result: PageResult<IdleResource> = await getIdleResources({
        current,
        pageSize,
      });
      setIdleResources(result.data);
    } catch {
      // 使用模拟数据
      setIdleResources([
        {
          id: "1",
          resourceId: "i-bp1234567890abcdef",
          resourceName: "test-server-01",
          provider: "aliyun" as CloudProvider,
          region: "cn-hangzhou",
          resourceType: "ecs" as CloudResourceType,
          reason: "CPU使用率低于5%持续30天",
          monthlyCost: 456.78,
          lastActiveTime: "2024-02-15T10:00:00Z",
          suggestedAction: "建议释放或降配",
        },
        {
          id: "2",
          resourceId: "disk-xyz789",
          resourceName: "unmounted-disk-01",
          provider: "tencent" as CloudProvider,
          region: "ap-guangzhou",
          resourceType: "oss" as CloudResourceType,
          reason: "未挂载的云盘",
          monthlyCost: 234.56,
          suggestedAction: "建议删除或挂载使用",
        },
        {
          id: "3",
          resourceId: "eip-abc123",
          resourceName: "unused-eip-01",
          provider: "huawei" as CloudProvider,
          region: "cn-north-4",
          resourceType: "eip" as CloudResourceType,
          reason: "未绑定的弹性IP",
          monthlyCost: 89.12,
          suggestedAction: "建议释放",
        },
      ]);
    } finally {
      setIdleLoading(false);
    }
  };

  // 加载 RI 推荐
  const loadRIRecommendations = async () => {
    setRILoading(true);
    try {
      const data = await getReservedInstanceRecommendations();
      setRIRecommendations(data);
    } catch {
      // 使用模拟数据
      setRIRecommendations([
        {
          id: "1",
          provider: "aliyun" as CloudProvider,
          resourceType: "ecs",
          instanceType: "ecs.g6.xlarge",
          region: "cn-hangzhou",
          onDemandCost: 1234.56,
          reservedCost: 765.32,
          savings: 469.24,
          savingsRate: 38,
          term: "1年",
          paymentOption: "全预付",
        },
        {
          id: "2",
          provider: "aws" as CloudProvider,
          resourceType: "ecs",
          instanceType: "m5.xlarge",
          region: "us-east-1",
          onDemandCost: 987.65,
          reservedCost: 612.34,
          savings: 375.31,
          savingsRate: 38,
          term: "1年",
          paymentOption: "部分预付",
        },
        {
          id: "3",
          provider: "tencent" as CloudProvider,
          resourceType: "rds",
          instanceType: "mysql.x4.large",
          region: "ap-guangzhou",
          onDemandCost: 2345.67,
          reservedCost: 1456.78,
          savings: 888.89,
          savingsRate: 38,
          term: "1年",
          paymentOption: "全预付",
        },
      ]);
    } finally {
      setRILoading(false);
    }
  };

  // 加载账单明细
  const loadBillingData = async (current = 1, pageSize = 10) => {
    setBillingLoading(true);
    try {
      const result = await getBillingDetails({ current, pageSize });
      setBillingData(result.data);
      setBillingPagination({ current, pageSize, total: result.total });
    } catch {
      // 使用模拟数据
      setBillingData([
        {
          id: "1",
          accountId: "acc-001",
          accountName: "生产环境-阿里云",
          provider: "aliyun",
          resourceType: "ecs",
          resourceId: "i-bp1234567890abcdef",
          resourceName: "web-server-01",
          cost: 456.78,
          billingCycle: "2024-03",
        },
      ]);
      setBillingPagination({ current, pageSize, total: 100 });
    } finally {
      setBillingLoading(false);
    }
  };

  useEffect(() => {
    loadStatistics();
    loadIdleResources();
    loadRIRecommendations();
    loadBillingData();
  }, []);

  // 导出报表
  const handleExport = async () => {
    try {
      const result = await exportCostReport({
        startTime: dateRange[0].format("YYYY-MM-DD"),
        endTime: dateRange[1].format("YYYY-MM-DD"),
        format: "excel",
      });
      if (result.downloadUrl) {
        window.open(result.downloadUrl);
        message.success("导出成功");
      }
    } catch {
      message.error("导出失败");
    }
  };

  // 查看闲置资源详情
  const handleViewIdleDetail = (resource: IdleResource) => {
    setSelectedIdleResource(resource);
    setIdleDetailDrawerVisible(true);
  };

  // 成本趋势图
  const getCostTrendOption = () => ({
    title: { text: "成本趋势", left: "center", textStyle: { fontSize: 14 } },
    tooltip: { trigger: "axis" },
    legend: { data: ["实际成本", "预测成本"], bottom: 0 },
    xAxis: {
      type: "category",
      data: [
        ...(statistics?.trend?.map((d) => d.date) || []),
        ...(statistics?.forecast?.map((d) => d.date) || []),
      ],
      axisLabel: { rotate: 45, interval: 4 },
    },
    yAxis: { type: "value", name: "成本(元)" },
    series: [
      {
        name: "实际成本",
        type: "line",
        data: statistics?.trend?.map((d) => d.cost.toFixed(2)) || [],
        smooth: true,
        itemStyle: { color: "#1890ff" },
      },
      {
        name: "预测成本",
        type: "line",
        data: [
          ...Array(statistics?.trend?.length || 0).fill(null),
          ...(statistics?.forecast?.map((d) => d.cost.toFixed(2)) || []),
        ],
        smooth: true,
        itemStyle: { color: "#52c41a" },
        lineStyle: { type: "dashed" },
      },
    ],
    grid: { bottom: 60, right: 20 },
  });

  // 云厂商成本饼图
  const getProviderCostPieOption = () => ({
    title: {
      text: "云厂商成本占比",
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
        data: Object.entries(statistics?.byProvider || {}).map(
          ([key, value]) => ({
            name: providerConfig[key as CloudProvider]?.name || key,
            value: value?.toFixed(2),
            itemStyle: { color: providerConfig[key as CloudProvider]?.color },
          }),
        ),
        label: { show: false },
        emphasis: { label: { show: true, fontSize: 12, fontWeight: "bold" } },
      },
    ],
  });

  // 资源类型成本饼图
  const getTypeCostPieOption = () => ({
    title: {
      text: "资源类型成本占比",
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
        data: Object.entries(statistics?.byType || {}).map(([key, value]) => ({
          name: resourceTypeConfig[key as CloudResourceType]?.name || key,
          value: value?.toFixed(2),
        })),
        label: { show: false },
        emphasis: { label: { show: true, fontSize: 12, fontWeight: "bold" } },
      },
    ],
  });

  // 闲置资源列表列定义
  const idleColumns: ColumnsType<IdleResource> = [
    {
      title: "资源名称",
      dataIndex: "resourceName",
      key: "resourceName",
      width: 180,
      render: (text: string, record: IdleResource) => (
        <a onClick={() => handleViewIdleDetail(record)}>{text}</a>
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
      title: "类型",
      dataIndex: "resourceType",
      key: "resourceType",
      width: 100,
      render: (type: CloudResourceType) =>
        resourceTypeConfig[type]?.name || type,
    },
    {
      title: "闲置原因",
      dataIndex: "reason",
      key: "reason",
      width: 200,
      ellipsis: true,
    },
    {
      title: "月费用",
      dataIndex: "monthlyCost",
      key: "monthlyCost",
      width: 100,
      render: (cost: number) => (
        <span style={{ color: "#ff4d4f" }}>¥{cost?.toFixed(2)}</span>
      ),
    },
    {
      title: "建议操作",
      dataIndex: "suggestedAction",
      key: "suggestedAction",
      width: 150,
    },
    {
      title: "最后活跃",
      dataIndex: "lastActiveTime",
      key: "lastActiveTime",
      width: 160,
      render: (time: string) =>
        time ? dayjs(time).format("YYYY-MM-DD HH:mm") : "-",
    },
  ];

  // RI 推荐列表列定义
  const riColumns: ColumnsType<ReservedInstanceRecommendation> = [
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
      title: "实例类型",
      dataIndex: "instanceType",
      key: "instanceType",
      width: 150,
    },
    {
      title: "区域",
      dataIndex: "region",
      key: "region",
      width: 120,
    },
    {
      title: "按需费用",
      dataIndex: "onDemandCost",
      key: "onDemandCost",
      width: 120,
      render: (cost: number) => `¥${cost?.toFixed(2)}/月`,
    },
    {
      title: "预留费用",
      dataIndex: "reservedCost",
      key: "reservedCost",
      width: 120,
      render: (cost: number) => (
        <span style={{ color: "#52c41a" }}>¥{cost?.toFixed(2)}/月</span>
      ),
    },
    {
      title: "节省金额",
      dataIndex: "savings",
      key: "savings",
      width: 120,
      render: (savings: number) => (
        <span style={{ color: "#52c41a" }}>¥{savings?.toFixed(2)}/月</span>
      ),
    },
    {
      title: "节省比例",
      dataIndex: "savingsRate",
      key: "savingsRate",
      width: 100,
      render: (rate: number) => <Tag color="green">{rate}%</Tag>,
    },
    {
      title: "期限",
      dataIndex: "term",
      key: "term",
      width: 80,
    },
    {
      title: "付款方式",
      dataIndex: "paymentOption",
      key: "paymentOption",
      width: 100,
    },
  ];

  // 账单明细列定义
  const billingColumns = [
    {
      title: "账期",
      dataIndex: "billingCycle",
      key: "billingCycle",
      width: 100,
    },
    {
      title: "账号",
      dataIndex: "accountName",
      key: "accountName",
      width: 150,
    },
    {
      title: "云厂商",
      dataIndex: "provider",
      key: "provider",
      width: 100,
      render: (provider: string) => (
        <Tag color={providerConfig[provider as CloudProvider]?.color}>
          {providerConfig[provider as CloudProvider]?.name || provider}
        </Tag>
      ),
    },
    {
      title: "资源名称",
      dataIndex: "resourceName",
      key: "resourceName",
      width: 180,
    },
    {
      title: "资源类型",
      dataIndex: "resourceType",
      key: "resourceType",
      width: 100,
      render: (type: string) =>
        resourceTypeConfig[type as CloudResourceType]?.name || type,
    },
    {
      title: "费用",
      dataIndex: "cost",
      key: "cost",
      width: 120,
      render: (cost: number) => `¥${cost?.toFixed(2)}`,
    },
  ];

  // 计算总闲置费用
  const totalIdleCost = idleResources.reduce(
    (sum, r) => sum + (r.monthlyCost || 0),
    0,
  );
  // 计算总 RI 节省
  const totalRISavings = riRecommendations.reduce(
    (sum, r) => sum + (r.savings || 0),
    0,
  );

  return (
    <div>
      {/* 统计卡片 */}
      <Row gutter={16} style={{ marginBottom: 16 }}>
        <Col span={6}>
          <Card loading={loading}>
            <Statistic
              title="本月总费用"
              value={statistics?.totalCost || 0}
              precision={2}
              prefix={<DollarOutlined />}
              suffix="元"
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card loading={loading}>
            <Statistic
              title="闲置资源费用"
              value={totalIdleCost}
              precision={2}
              valueStyle={{ color: "#ff4d4f" }}
              prefix={<WarningOutlined />}
              suffix="元/月"
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card loading={loading}>
            <Statistic
              title="RI/SP 可节省"
              value={totalRISavings}
              precision={2}
              valueStyle={{ color: "#52c41a" }}
              prefix={<ThunderboltOutlined />}
              suffix="元/月"
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card loading={loading}>
            <Statistic
              title="预测下月费用"
              value={
                statistics?.forecast?.reduce((sum, f) => sum + f.cost, 0) || 0
              }
              precision={2}
              prefix={<DollarOutlined />}
              suffix="元"
            />
          </Card>
        </Col>
      </Row>

      {/* 图表区域 */}
      <Row gutter={16} style={{ marginBottom: 16 }}>
        <Col span={12}>
          <Card size="small">
            <ReactECharts
              option={getCostTrendOption()}
              style={{ height: 300 }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card size="small">
            <ReactECharts
              option={getProviderCostPieOption()}
              style={{ height: 300 }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card size="small">
            <ReactECharts
              option={getTypeCostPieOption()}
              style={{ height: 300 }}
            />
          </Card>
        </Col>
      </Row>

      <Tabs
        items={[
          {
            key: "idle",
            label: (
              <span>
                <WarningOutlined /> 闲置资源
                <Badge count={idleResources.length} style={{ marginLeft: 8 }} />
              </span>
            ),
            children: (
              <Card>
                <Alert
                  message={`发现 ${idleResources.length} 个闲置资源，每月浪费 ¥${totalIdleCost.toFixed(2)}`}
                  type="warning"
                  showIcon
                  style={{ marginBottom: 16 }}
                />
                <Table
                  columns={idleColumns}
                  dataSource={idleResources}
                  rowKey="id"
                  loading={idleLoading}
                  scroll={{ x: 1200 }}
                  pagination={{ pageSize: 10 }}
                />
              </Card>
            ),
          },
          {
            key: "ri",
            label: (
              <span>
                <ThunderboltOutlined /> RI/SP 推荐
                <Badge
                  count={riRecommendations.length}
                  style={{ marginLeft: 8 }}
                />
              </span>
            ),
            children: (
              <Card>
                <Alert
                  message={`发现 ${riRecommendations.length} 个预留实例推荐，每月可节省 ¥${totalRISavings.toFixed(2)}`}
                  type="success"
                  showIcon
                  style={{ marginBottom: 16 }}
                />
                <Table
                  columns={riColumns}
                  dataSource={riRecommendations}
                  rowKey="id"
                  loading={riLoading}
                  scroll={{ x: 1100 }}
                  pagination={{ pageSize: 10 }}
                />
              </Card>
            ),
          },
          {
            key: "billing",
            label: "账单明细",
            children: (
              <Card>
                <div style={{ marginBottom: 16 }}>
                  <Space>
                    <RangePicker
                      value={dateRange}
                      onChange={(dates) =>
                        dates &&
                        setDateRange(dates as [dayjs.Dayjs, dayjs.Dayjs])
                      }
                    />
                    <Button type="primary" onClick={() => loadBillingData()}>
                      查询
                    </Button>
                    <Button icon={<DownloadOutlined />} onClick={handleExport}>
                      导出报表
                    </Button>
                  </Space>
                </div>
                <Table
                  columns={billingColumns}
                  dataSource={billingData}
                  rowKey="id"
                  loading={billingLoading}
                  scroll={{ x: 1000 }}
                  pagination={{
                    ...billingPagination,
                    showSizeChanger: true,
                    showTotal: (total) => `共 ${total} 条`,
                    onChange: (page, pageSize) =>
                      loadBillingData(page, pageSize),
                  }}
                />
              </Card>
            ),
          },
        ]}
      />

      {/* 闲置资源详情抽屉 */}
      <Drawer
        title={`${selectedIdleResource?.resourceName || ""} - 闲置详情`}
        placement="right"
        width={500}
        onClose={() => setIdleDetailDrawerVisible(false)}
        open={idleDetailDrawerVisible}
      >
        {selectedIdleResource && (
          <Descriptions column={1} bordered>
            <Descriptions.Item label="资源ID">
              {selectedIdleResource.resourceId}
            </Descriptions.Item>
            <Descriptions.Item label="资源名称">
              {selectedIdleResource.resourceName}
            </Descriptions.Item>
            <Descriptions.Item label="云厂商">
              <Tag color={providerConfig[selectedIdleResource.provider]?.color}>
                {providerConfig[selectedIdleResource.provider]?.name}
              </Tag>
            </Descriptions.Item>
            <Descriptions.Item label="区域">
              {selectedIdleResource.region}
            </Descriptions.Item>
            <Descriptions.Item label="类型">
              {resourceTypeConfig[selectedIdleResource.resourceType]?.name}
            </Descriptions.Item>
            <Descriptions.Item label="闲置原因">
              {selectedIdleResource.reason}
            </Descriptions.Item>
            <Descriptions.Item label="月费用">
              <span style={{ color: "#ff4d4f" }}>
                ¥{selectedIdleResource.monthlyCost?.toFixed(2)}
              </span>
            </Descriptions.Item>
            <Descriptions.Item label="最后活跃时间">
              {selectedIdleResource.lastActiveTime
                ? dayjs(selectedIdleResource.lastActiveTime).format(
                    "YYYY-MM-DD HH:mm:ss",
                  )
                : "-"}
            </Descriptions.Item>
            <Descriptions.Item label="建议操作">
              <Alert
                message={selectedIdleResource.suggestedAction}
                type="info"
              />
            </Descriptions.Item>
          </Descriptions>
        )}
      </Drawer>
    </div>
  );
};

export default CostAnalysisPage;
