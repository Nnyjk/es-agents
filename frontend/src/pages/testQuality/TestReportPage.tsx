/**
 * 测试报告页面
 * 展示测试执行结果、统计数据、趋势分析
 */
import React, { useState, useEffect } from 'react';
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
  Select,
  Progress,
  Tabs,
  Empty,
  Descriptions,
  Drawer,
  Timeline,
  Tooltip,
  message,
  Dropdown,
  Badge,
  Typography,
} from 'antd';
import {
  DownloadOutlined,
  PrinterOutlined,
  ShareAltOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  ExclamationCircleOutlined,
  LineChartOutlined,
  PieChartOutlined,
  FileTextOutlined,
  BugOutlined,
} from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import dayjs from 'dayjs';
import {
  getTestReports,
  getTestReportDetail,
  getReportTrends,
  exportReport,
} from '../../services/testQuality';
import type { TestReport, ReportTrend, ReportCase } from '../../types/testQuality';

const { RangePicker } = DatePicker;
const { Title, Text } = Typography;

const TestReportPage: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [reports, setReports] = useState<TestReport[]>([]);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [dateRange, setDateRange] = useState<[dayjs.Dayjs, dayjs.Dayjs]>();
  const [filterStatus, setFilterStatus] = useState<string>();
  const [drawerVisible, setDrawerVisible] = useState(false);
  const [currentReport, setCurrentReport] = useState<TestReport | null>(null);
  const [trends, setTrends] = useState<ReportTrend[]>([]);

  // 加载报告列表
  const loadReports = async () => {
    setLoading(true);
    try {
      const result = await getTestReports({
        page,
        pageSize,
        status: filterStatus,
        startDate: dateRange?.[0].format('YYYY-MM-DD'),
        endDate: dateRange?.[1].format('YYYY-MM-DD'),
      });
      setReports(result.data);
      setTotal(result.total);
    } catch (error) {
      message.error('加载报告列表失败');
    } finally {
      setLoading(false);
    }
  };

  // 加载趋势数据
  const loadTrends = async () => {
    try {
      const trendData = await getReportTrends();
      setTrends(trendData);
    } catch (error) {
      console.error('加载趋势数据失败', error);
    }
  };

  useEffect(() => {
    loadReports();
    loadTrends();
  }, [page, pageSize, dateRange, filterStatus]);

  // 查看报告详情
  const handleViewDetail = async (record: TestReport) => {
    setLoading(true);
    try {
      const detail = await getTestReportDetail(record.id);
      setCurrentReport(detail);
      setDrawerVisible(true);
    } catch (error) {
      message.error('加载报告详情失败');
    } finally {
      setLoading(false);
    }
  };

  // 导出报告
  const handleExport = async (record: TestReport, format: string) => {
    try {
      await exportReport(record.id, format);
      message.success('导出成功');
    } catch (error) {
      message.error('导出失败');
    }
  };

  // 状态颜色映射
  const statusColorMap: Record<string, string> = {
    passed: 'success',
    failed: 'error',
    partial: 'warning',
    running: 'processing',
  };

  const statusLabelMap: Record<string, string> = {
    passed: '通过',
    failed: '失败',
    partial: '部分通过',
    running: '执行中',
  };

  // 表格列定义
  const columns: ColumnsType<TestReport> = [
    {
      title: '报告名称',
      dataIndex: 'name',
      key: 'name',
      width: 250,
      ellipsis: true,
    },
    {
      title: '测试任务',
      dataIndex: 'taskName',
      key: 'taskName',
      width: 150,
      ellipsis: true,
    },
    {
      title: '执行时间',
      dataIndex: 'executionTime',
      key: 'executionTime',
      width: 160,
      render: (time: string) => dayjs(time).format('YYYY-MM-DD HH:mm'),
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: (status: string) => (
        <Badge
          status={
            statusColorMap[status] as
              | 'success'
              | 'processing'
              | 'error'
              | 'warning'
              | 'default'
          }
          text={statusLabelMap[status]}
        />
      ),
    },
    {
      title: '总用例数',
      dataIndex: 'totalCases',
      key: 'totalCases',
      width: 100,
      align: 'right',
    },
    {
      title: '通过率',
      dataIndex: 'passRate',
      key: 'passRate',
      width: 120,
      render: (rate: number) => (
        <Progress
          percent={rate * 100}
          size="small"
          status={rate >= 0.9 ? 'success' : rate >= 0.7 ? 'normal' : 'exception'}
        />
      ),
    },
    {
      title: '执行时长',
      dataIndex: 'duration',
      key: 'duration',
      width: 100,
      render: (duration: number) => {
        if (duration < 60) return `${duration}秒`;
        if (duration < 3600) return `${Math.floor(duration / 60)}分`;
        return `${Math.floor(duration / 3600)}时${Math.floor((duration % 3600) / 60)}分`;
      },
    },
    {
      title: '操作',
      key: 'action',
      width: 180,
      render: (_: unknown, record: TestReport) => (
        <Space size="small">
          <Button type="link" size="small" onClick={() => handleViewDetail(record)}>
            查看详情
          </Button>
          <Dropdown
            menu={{
              items: [
                {
                  key: 'pdf',
                  label: '导出 PDF',
                  onClick: () => handleExport(record, 'pdf'),
                },
                {
                  key: 'excel',
                  label: '导出 Excel',
                  onClick: () => handleExport(record, 'excel'),
                },
                {
                  key: 'html',
                  label: '导出 HTML',
                  onClick: () => handleExport(record, 'html'),
                },
              ],
            }}
          >
            <Button type="link" size="small" icon={<DownloadOutlined />}>
              导出
            </Button>
          </Dropdown>
        </Space>
      ),
    },
  ];

  // 用例详情表格列
  const caseColumns: ColumnsType<ReportCase> = [
    {
      title: '用例名称',
      dataIndex: 'name',
      key: 'name',
      width: 200,
      ellipsis: true,
    },
    {
      title: '模块',
      dataIndex: 'module',
      key: 'module',
      width: 120,
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 80,
      render: (status: string) => (
        <Tag color={status === 'passed' ? 'success' : 'error'}>
          {status === 'passed' ? '通过' : '失败'}
        </Tag>
      ),
    },
    {
      title: '执行时间',
      dataIndex: 'duration',
      key: 'duration',
      width: 100,
      render: (time: number) => `${time}ms`,
    },
    {
      title: '失败原因',
      dataIndex: 'errorMessage',
      key: 'errorMessage',
      ellipsis: true,
      render: (text: string, record) =>
        record.status === 'failed' ? (
          <Tooltip title={text}>
            <Text type="danger" ellipsis>
              {text}
            </Text>
          </Tooltip>
        ) : (
          '-'
        ),
    },
  ];

  return (
    <div>
      <Tabs
        defaultActiveKey="list"
        items={[
          {
            key: 'list',
            label: (
              <span>
                <FileTextOutlined /> 报告列表
              </span>
            ),
            children: (
              <Card bordered={false}>
                <Space direction="vertical" style={{ width: '100%' }} size="middle">
                  {/* 筛选栏 */}
                  <Row justify="space-between">
                    <Col>
                      <Space>
                        <RangePicker
                          onChange={(_, dateStrings) =>
                            setDateRange([
                              dayjs(dateStrings[0]),
                              dayjs(dateStrings[1]),
                            ])
                          }
                        />
                        <Select
                          placeholder="状态"
                          allowClear
                          style={{ width: 120 }}
                          onChange={setFilterStatus}
                          options={[
                            { value: 'passed', label: '通过' },
                            { value: 'failed', label: '失败' },
                            { value: 'partial', label: '部分通过' },
                          ]}
                        />
                      </Space>
                    </Col>
                    <Col>
                      <Button icon={<PrinterOutlined />}>打印报告</Button>
                    </Col>
                  </Row>

                  {/* 报告表格 */}
                  <Table
                    columns={columns}
                    dataSource={reports}
                    rowKey="id"
                    loading={loading}
                    pagination={{
                      current: page,
                      pageSize,
                      total,
                      showSizeChanger: true,
                      showQuickJumper: true,
                      onChange: (p, ps) => {
                        setPage(p);
                        setPageSize(ps);
                      },
                    }}
                  />
                </Space>
              </Card>
            ),
          },
          {
            key: 'trends',
            label: (
              <span>
                <LineChartOutlined /> 趋势分析
              </span>
            ),
            children: (
              <Row gutter={16}>
                <Col span={24}>
                  <Card title="通过率趋势" bordered={false}>
                    {trends.length > 0 ? (
                      <div style={{ padding: '20px 0' }}>
                        {/* 简易趋势图展示 */}
                        <Row gutter={16}>
                          {trends.map((trend, index) => (
                            <Col span={Math.floor(24 / trends.length)} key={index}>
                              <Card size="small">
                                <Statistic
                                  title={trend.date}
                                  value={trend.passRate * 100}
                                  precision={1}
                                  suffix="%"
                                  valueStyle={{
                                    color:
                                      trend.passRate >= 0.9
                                        ? '#3f8600'
                                        : trend.passRate >= 0.7
                                          ? '#1677ff'
                                          : '#cf1322',
                                  }}
                                />
                                <div style={{ marginTop: 8 }}>
                                  <Text type="secondary">
                                    通过 {trend.passedCases} / 总计 {trend.totalCases}
                                  </Text>
                                </div>
                              </Card>
                            </Col>
                          ))}
                        </Row>
                      </div>
                    ) : (
                      <Empty description="暂无趋势数据" />
                    )}
                  </Card>
                </Col>
                <Col span={12} style={{ marginTop: 16 }}>
                  <Card title="执行统计" bordered={false}>
                    <Row gutter={16}>
                      <Col span={12}>
                        <Statistic
                          title="总执行次数"
                          value={trends.reduce((sum, t) => sum + t.executionCount, 0)}
                          suffix="次"
                        />
                      </Col>
                      <Col span={12}>
                        <Statistic
                          title="平均通过率"
                          value={
                            trends.length > 0
                              ? (trends.reduce((sum, t) => sum + t.passRate, 0) /
                                  trends.length) *
                                100
                              : 0
                          }
                          precision={1}
                          suffix="%"
                        />
                      </Col>
                    </Row>
                  </Card>
                </Col>
                <Col span={12} style={{ marginTop: 16 }}>
                  <Card title="问题分布" bordered={false}>
                    <Empty description="暂无问题数据" image={Empty.PRESENTED_IMAGE_SIMPLE} />
                  </Card>
                </Col>
              </Row>
            ),
          },
        ]}
      />

      {/* 报告详情抽屉 */}
      <Drawer
        title={`测试报告 - ${currentReport?.name || ''}`}
        placement="right"
        width={800}
        onClose={() => setDrawerVisible(false)}
        open={drawerVisible}
        extra={
          <Space>
            <Button icon={<ShareAltOutlined />}>分享</Button>
            <Button icon={<DownloadOutlined />}>导出</Button>
          </Space>
        }
      >
        {currentReport && (
          <Space direction="vertical" style={{ width: '100%' }} size="large">
            {/* 执行概况 */}
            <Card title="执行概况" size="small">
              <Row gutter={24}>
                <Col span={6}>
                  <Statistic
                    title="总用例数"
                    value={currentReport.totalCases}
                    prefix={<FileTextOutlined />}
                  />
                </Col>
                <Col span={6}>
                  <Statistic
                    title="通过数"
                    value={currentReport.passedCases}
                    valueStyle={{ color: '#3f8600' }}
                    prefix={<CheckCircleOutlined />}
                  />
                </Col>
                <Col span={6}>
                  <Statistic
                    title="失败数"
                    value={currentReport.failedCases}
                    valueStyle={{ color: '#cf1322' }}
                    prefix={<CloseCircleOutlined />}
                  />
                </Col>
                <Col span={6}>
                  <Statistic
                    title="跳过数"
                    value={currentReport.skippedCases}
                    valueStyle={{ color: '#faad14' }}
                    prefix={<ExclamationCircleOutlined />}
                  />
                </Col>
              </Row>

              <div style={{ marginTop: 24 }}>
                <Text strong>通过率</Text>
                <Progress
                  percent={currentReport.passRate * 100}
                  status={
                    currentReport.passRate >= 0.9
                      ? 'success'
                      : currentReport.passRate >= 0.7
                        ? 'normal'
                        : 'exception'
                  }
                />
              </div>
            </Card>

            {/* 基本信息 */}
            <Card title="基本信息" size="small">
              <Descriptions column={2}>
                <Descriptions.Item label="测试任务">
                  {currentReport.taskName}
                </Descriptions.Item>
                <Descriptions.Item label="执行环境">
                  {currentReport.environment}
                </Descriptions.Item>
                <Descriptions.Item label="执行时间">
                  {dayjs(currentReport.executionTime).format('YYYY-MM-DD HH:mm:ss')}
                </Descriptions.Item>
                <Descriptions.Item label="执行时长">
                  {currentReport.duration < 60
                    ? `${currentReport.duration}秒`
                    : `${Math.floor(currentReport.duration / 60)}分${currentReport.duration % 60}秒`}
                </Descriptions.Item>
                <Descriptions.Item label="执行者">
                  {currentReport.executor}
                </Descriptions.Item>
                <Descriptions.Item label="触发方式">
                  {currentReport.triggerType}
                </Descriptions.Item>
              </Descriptions>
            </Card>

            {/* 用例详情 */}
            <Card title="用例详情" size="small">
              <Table
                columns={caseColumns}
                dataSource={currentReport.caseDetails || []}
                rowKey="id"
                size="small"
                pagination={{
                  pageSize: 10,
                  showSizeChanger: true,
                }}
              />
            </Card>

            {/* 失败用例分析 */}
            {currentReport.failedCases > 0 && (
              <Card
                title={
                  <Space>
                    <BugOutlined />
                    <span>失败用例分析</span>
                  </Space>
                }
                size="small"
              >
                <Timeline
                  items={currentReport.caseDetails
                    ?.filter((c) => c.status === 'failed')
                    .map((c) => ({
                      color: 'red',
                      children: (
                        <div>
                          <Text strong>{c.name}</Text>
                          <br />
                          <Text type="secondary" style={{ fontSize: 12 }}>
                            模块: {c.module} | 执行时间: {c.duration}ms
                          </Text>
                          <br />
                          <Text type="danger" style={{ fontSize: 12 }}>
                            错误: {c.errorMessage}
                          </Text>
                        </div>
                      ),
                    }))}
                />
              </Card>
            )}
          </Space>
        )}
      </Drawer>
    </div>
  );
};

export default TestReportPage;