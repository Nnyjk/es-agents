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
  DatePicker,
  Tooltip,
  Badge,
  Row,
  Col,
  Statistic,
  message,
  Dropdown,
  Typography,
} from "antd";
import {
  ExclamationCircleOutlined,
  WarningOutlined,
  InfoCircleOutlined,
  CloseCircleOutlined,
  ReloadOutlined,
  BellOutlined,
} from "@ant-design/icons";
import type { ColumnsType } from "antd/es/table";
import { PageContainer } from "@ant-design/pro-components";
import dayjs from "dayjs";
import {
  getAlerts,
  acknowledgeAlert,
  resolveAlert,
  ignoreAlert,
  batchAlertAction,
  getAlertStatistics,
} from "@/services/alert";
import type {
  Alert,
  AlertLevel,
  AlertStatus,
  AlertStatistics,
  AlertQueryParams,
} from "@/types";

const { RangePicker } = DatePicker;
const { Option } = Select;
const { Text } = Typography;

const levelConfig: Record<AlertLevel, { color: string; icon: React.ReactNode; text: string }> = {
  INFO: { color: "processing", icon: <InfoCircleOutlined />, text: "信息" },
  WARNING: { color: "warning", icon: <WarningOutlined />, text: "警告" },
  ERROR: { color: "error", icon: <CloseCircleOutlined />, text: "错误" },
  CRITICAL: { color: "magenta", icon: <ExclamationCircleOutlined />, text: "严重" },
};

const statusConfig: Record<AlertStatus, { color: string; text: string }> = {
  ACTIVE: { color: "processing", text: "活动" },
  ACKNOWLEDGED: { color: "warning", text: "已确认" },
  RESOLVED: { color: "success", text: "已解决" },
  IGNORED: { color: "default", text: "已忽略" },
};

const AlertList: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [alerts, setAlerts] = useState<Alert[]>([]);
  const [total, setTotal] = useState(0);
  const [statistics, setStatistics] = useState<AlertStatistics | null>(null);
  const [selectedRowKeys, setSelectedRowKeys] = useState<React.Key[]>([]);
  const [selectedAlert, setSelectedAlert] = useState<Alert | null>(null);
  const [detailVisible, setDetailVisible] = useState(false);
  const [queryParams, setQueryParams] = useState<AlertQueryParams>({
    page: 1,
    pageSize: 20,
  });

  const fetchAlerts = async (params: AlertQueryParams) => {
    setLoading(true);
    try {
      const response = await getAlerts(params);
      setAlerts(response.data || []);
      setTotal(response.total || 0);
      if (response.statistics) {
        setStatistics(response.statistics);
      }
    } catch (error) {
      message.error("获取告警列表失败");
    } finally {
      setLoading(false);
    }
  };

  const fetchStatistics = async () => {
    try {
      const stats = await getAlertStatistics();
      setStatistics(stats);
    } catch (error) {
      console.error("获取告警统计失败", error);
    }
  };

  useEffect(() => {
    fetchAlerts(queryParams);
    fetchStatistics();
  }, [queryParams]);

  const handleSearch = (values: any) => {
    const params: AlertQueryParams = {
      ...queryParams,
      ...values,
      page: 1,
      startTime: values.timeRange?.[0]?.format("YYYY-MM-DD HH:mm:ss"),
      endTime: values.timeRange?.[1]?.format("YYYY-MM-DD HH:mm:ss"),
    };
    delete (params as any).timeRange;
    setQueryParams(params);
  };

  const handleReset = () => {
    setQueryParams({ page: 1, pageSize: 20 });
  };

  const handleTableChange = (pagination: any) => {
    setQueryParams({
      ...queryParams,
      page: pagination.current,
      pageSize: pagination.pageSize,
    });
  };

  const handleAction = async (id: string, action: "acknowledge" | "resolve" | "ignore") => {
    try {
      switch (action) {
        case "acknowledge":
          await acknowledgeAlert(id);
          message.success("告警已确认");
          break;
        case "resolve":
          await resolveAlert(id);
          message.success("告警已解决");
          break;
        case "ignore":
          await ignoreAlert(id);
          message.success("告警已忽略");
          break;
      }
      fetchAlerts(queryParams);
      fetchStatistics();
    } catch (error) {
      message.error("操作失败");
    }
  };

  const handleBatchAction = async (action: "acknowledge" | "resolve" | "ignore") => {
    if (selectedRowKeys.length === 0) {
      message.warning("请先选择告警");
      return;
    }
    Modal.confirm({
      title: `确认批量${action === "acknowledge" ? "确认" : action === "resolve" ? "解决" : "忽略"}告警`,
      content: `将对 ${selectedRowKeys.length} 条告警执行该操作`,
      onOk: async () => {
        try {
          await batchAlertAction({
            ids: selectedRowKeys as string[],
            action,
          });
          message.success("批量操作成功");
          setSelectedRowKeys([]);
          fetchAlerts(queryParams);
          fetchStatistics();
        } catch (error) {
          message.error("批量操作失败");
        }
      },
    });
  };

  const columns: ColumnsType<Alert> = [
    {
      title: "级别",
      dataIndex: "level",
      key: "level",
      width: 100,
      render: (level: AlertLevel) => (
        <Tag icon={levelConfig[level].icon} color={levelConfig[level].color}>
          {levelConfig[level].text}
        </Tag>
      ),
    },
    {
      title: "标题",
      dataIndex: "title",
      key: "title",
      ellipsis: true,
      render: (text: string, record: Alert) => (
        <a onClick={() => { setSelectedAlert(record); setDetailVisible(true); }}>
          {text}
        </a>
      ),
    },
    {
      title: "来源",
      dataIndex: "source",
      key: "source",
      width: 120,
      ellipsis: true,
    },
    {
      title: "状态",
      dataIndex: "status",
      key: "status",
      width: 100,
      render: (status: AlertStatus) => (
        <Tag color={statusConfig[status].color}>{statusConfig[status].text}</Tag>
      ),
    },
    {
      title: "触发时间",
      dataIndex: "createdAt",
      key: "createdAt",
      width: 170,
      render: (time: string) => dayjs(time).format("YYYY-MM-DD HH:mm:ss"),
    },
    {
      title: "操作",
      key: "action",
      width: 180,
      render: (_, record) => (
        <Space size="small">
          {record.status === "ACTIVE" && (
            <>
              <Button type="link" size="small" onClick={() => handleAction(record.id, "acknowledge")}>
                确认
              </Button>
              <Button type="link" size="small" onClick={() => handleAction(record.id, "resolve")}>
                解决
              </Button>
            </>
          )}
          {record.status === "ACKNOWLEDGED" && (
            <Button type="link" size="small" onClick={() => handleAction(record.id, "resolve")}>
              解决
            </Button>
          )}
          {record.status !== "IGNORED" && (
            <Button type="link" size="small" danger onClick={() => handleAction(record.id, "ignore")}>
              忽略
            </Button>
          )}
        </Space>
      ),
    },
  ];

  return (
    <PageContainer>
      {statistics && (
        <Card style={{ marginBottom: 16 }}>
          <Row gutter={24}>
            <Col span={4}>
              <Statistic
                title="活动告警"
                value={statistics.active}
                valueStyle={{ color: "#1890ff" }}
                prefix={<Badge status="processing" />}
              />
            </Col>
            <Col span={4}>
              <Statistic
                title="已确认"
                value={statistics.acknowledged}
                valueStyle={{ color: "#faad14" }}
                prefix={<Badge status="warning" />}
              />
            </Col>
            <Col span={4}>
              <Statistic
                title="已解决"
                value={statistics.resolved}
                valueStyle={{ color: "#52c41a" }}
                prefix={<Badge status="success" />}
              />
            </Col>
            <Col span={4}>
              <Statistic
                title="已忽略"
                value={statistics.ignored}
                prefix={<Badge status="default" />}
              />
            </Col>
            <Col span={4}>
              <Statistic
                title="严重告警"
                value={statistics.byLevel?.CRITICAL || 0}
                valueStyle={{ color: "#eb2f96" }}
              />
            </Col>
            <Col span={4}>
              <Statistic
                title="错误告警"
                value={statistics.byLevel?.ERROR || 0}
                valueStyle={{ color: "#ff4d4f" }}
              />
            </Col>
          </Row>
        </Card>
      )}

      <Card style={{ marginBottom: 16 }}>
        <Form layout="inline" onFinish={handleSearch} onReset={handleReset}>
          <Form.Item name="keyword">
            <Input placeholder="搜索告警标题" style={{ width: 200 }} allowClear />
          </Form.Item>
          <Form.Item name="level">
            <Select placeholder="告警级别" style={{ width: 120 }} allowClear>
              <Option value="INFO">信息</Option>
              <Option value="WARNING">警告</Option>
              <Option value="ERROR">错误</Option>
              <Option value="CRITICAL">严重</Option>
            </Select>
          </Form.Item>
          <Form.Item name="status">
            <Select placeholder="告警状态" style={{ width: 120 }} allowClear>
              <Option value="ACTIVE">活动</Option>
              <Option value="ACKNOWLEDGED">已确认</Option>
              <Option value="RESOLVED">已解决</Option>
              <Option value="IGNORED">已忽略</Option>
            </Select>
          </Form.Item>
          <Form.Item name="source">
            <Input placeholder="来源" style={{ width: 150 }} allowClear />
          </Form.Item>
          <Form.Item name="timeRange">
            <RangePicker showTime />
          </Form.Item>
          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit">
                查询
              </Button>
              <Button htmlType="reset">重置</Button>
            </Space>
          </Form.Item>
        </Form>
      </Card>

      <Card
        title={
          <Space>
            <BellOutlined />
            <span>告警列表</span>
          </Space>
        }
        extra={
          <Space>
            {selectedRowKeys.length > 0 && (
              <Dropdown
                menu={{
                  items: [
                    { key: "acknowledge", label: "批量确认" },
                    { key: "resolve", label: "批量解决" },
                    { key: "ignore", label: "批量忽略" },
                  ],
                  onClick: ({ key }) => handleBatchAction(key as any),
                }}
              >
                <Button>批量操作 ({selectedRowKeys.length})</Button>
              </Dropdown>
            )}
            <Button icon={<ReloadOutlined />} onClick={() => fetchAlerts(queryParams)}>
              刷新
            </Button>
          </Space>
        }
      >
        <Table
          rowKey="id"
          columns={columns}
          dataSource={alerts}
          loading={loading}
          pagination={{
            current: queryParams.page,
            pageSize: queryParams.pageSize,
            total,
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (total) => `共 ${total} 条`,
          }}
          rowSelection={{
            selectedRowKeys,
            onChange: setSelectedRowKeys,
          }}
          onChange={handleTableChange}
        />
      </Card>

      <Modal
        title="告警详情"
        open={detailVisible}
        onCancel={() => setDetailVisible(false)}
        footer={null}
        width={600}
      >
        {selectedAlert && (
          <div>
            <p><strong>级别：</strong>
              <Tag icon={levelConfig[selectedAlert.level].icon} color={levelConfig[selectedAlert.level].color}>
                {levelConfig[selectedAlert.level].text}
              </Tag>
            </p>
            <p><strong>标题：</strong>{selectedAlert.title}</p>
            <p><strong>来源：</strong>{selectedAlert.source}</p>
            <p><strong>状态：</strong>
              <Tag color={statusConfig[selectedAlert.status].color}>
                {statusConfig[selectedAlert.status].text}
              </Tag>
            </p>
            <p><strong>内容：</strong></p>
            <div style={{ background: "#f5f5f5", padding: 12, borderRadius: 4 }}>
              <Text style={{ whiteSpace: "pre-wrap" }}>{selectedAlert.content}</Text>
            </div>
            {selectedAlert.labels && Object.keys(selectedAlert.labels).length > 0 && (
              <>
                <p><strong>标签：</strong></p>
                <div>
                  {Object.entries(selectedAlert.labels).map(([key, value]) => (
                    <Tag key={key}>{key}={value}</Tag>
                  ))}
                </div>
              </>
            )}
            <p><strong>触发时间：</strong>{dayjs(selectedAlert.createdAt).format("YYYY-MM-DD HH:mm:ss")}</p>
            {selectedAlert.acknowledgedBy && (
              <p><strong>确认人：</strong>{selectedAlert.acknowledgedBy}</p>
            )}
            {selectedAlert.acknowledgedAt && (
              <p><strong>确认时间：</strong>{dayjs(selectedAlert.acknowledgedAt).format("YYYY-MM-DD HH:mm:ss")}</p>
            )}
          </div>
        )}
      </Modal>
    </PageContainer>
  );
};

export default AlertList;