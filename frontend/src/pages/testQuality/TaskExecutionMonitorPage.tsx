/**
 * 任务执行监控页面
 * 实时监控测试任务执行状态、进度和日志
 */
import React, { useState, useEffect, useRef } from 'react';
import {
  Card,
  Row,
  Col,
  Statistic,
  Progress,
  Table,
  Tag,
  Space,
  Button,
  Input,
  Select,
  Alert,
  Descriptions,
  Drawer,
  Empty,
  Spin,
  message,
  Tooltip,
} from 'antd';
import {
  SyncOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  PauseCircleOutlined,
  StopOutlined,
  EyeOutlined,
  ReloadOutlined,
  ClockCircleOutlined,
  LineChartOutlined,
} from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import dayjs from 'dayjs';
import {
  getTaskExecutions,
  getExecutionDetail,
  getExecutionLogs,
  getExecutionStatistics,
  pauseTaskExecution,
  cancelTaskExecution,
  retryFailedCases,
} from '../../services/testQuality';
import type { TaskExecution, ExecutionLog, ExecutionStatistics } from '../../types/testQuality';

const { Search } = Input;

const TaskExecutionMonitorPage: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [executions, setExecutions] = useState<TaskExecution[]>([]);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(20);
  const [searchKeyword, setSearchKeyword] = useState('');
  const [filterStatus, setFilterStatus] = useState<string>();
  const [statistics, setStatistics] = useState<ExecutionStatistics | null>(null);
  const [drawerVisible, setDrawerVisible] = useState(false);
  const [currentExecution, setCurrentExecution] = useState<TaskExecution | null>(null);
  const [logs, setLogs] = useState<ExecutionLog[]>([]);
  const [logsLoading, setLogsLoading] = useState(false);
  const [autoRefresh, setAutoRefresh] = useState(false);
  const logsRef = useRef<HTMLDivElement>(null);
  const refreshInterval = useRef<NodeJS.Timeout | null>(null);

  // 加载执行列表
  const loadExecutions = async () => {
    setLoading(true);
    try {
      const result = await getTaskExecutions({
        page,
        pageSize,
        status: filterStatus,
      });
      setExecutions(result.data);
      setTotal(result.total);
    } catch (error) {
      message.error('加载执行列表失败');
    } finally {
      setLoading(false);
    }
  };

  // 加载统计数据
  const loadStatistics = async () => {
    try {
      const stats = await getExecutionStatistics();
      setStatistics(stats);
    } catch (error) {
      console.error('加载统计数据失败', error);
    }
  };

  // 初始化加载
  useEffect(() => {
    loadExecutions();
    loadStatistics();
  }, [page, pageSize, searchKeyword, filterStatus]);

  // 自动刷新
  useEffect(() => {
    if (autoRefresh) {
      refreshInterval.current = setInterval(() => {
        loadExecutions();
        loadStatistics();
        if (currentExecution && drawerVisible) {
          loadLogs(currentExecution.id);
        }
      }, 3000);
    } else if (refreshInterval.current) {
      clearInterval(refreshInterval.current);
    }
    return () => {
      if (refreshInterval.current) {
        clearInterval(refreshInterval.current);
      }
    };
  }, [autoRefresh, currentExecution, drawerVisible]);

  // 状态颜色
  const statusConfig: Record<string, { color: string; icon: React.ReactNode }> = {
    running: { color: 'processing', icon: <SyncOutlined spin /> },
    completed: { color: 'success', icon: <CheckCircleOutlined /> },
    failed: { color: 'error', icon: <CloseCircleOutlined /> },
    paused: { color: 'warning', icon: <PauseCircleOutlined /> },
    cancelled: { color: 'default', icon: <StopOutlined /> },
  };

  const statusLabelMap: Record<string, string> = {
    running: '执行中',
    completed: '已完成',
    failed: '失败',
    paused: '已暂停',
    cancelled: '已取消',
  };

  // 加载执行详情和日志
  const loadLogs = async (executionId: string) => {
    setLogsLoading(true);
    try {
      const logsData = await getExecutionLogs(executionId);
      setLogs(logsData);
    } catch (error) {
      console.error('加载日志失败', error);
    } finally {
      setLogsLoading(false);
    }
  };

  // 查看详情
  const handleViewDetail = async (record: TaskExecution) => {
    setLoading(true);
    try {
      const detail = await getExecutionDetail(record.id);
      setCurrentExecution(detail);
      setDrawerVisible(true);
      await loadLogs(record.id);
    } catch (error) {
      message.error('加载详情失败');
    } finally {
      setLoading(false);
    }
  };

  // 暂停执行
  const handlePause = async (record: TaskExecution) => {
    try {
      await pauseTaskExecution(record.id);
      message.success('执行已暂停');
      loadExecutions();
    } catch (error) {
      message.error('暂停失败');
    }
  };

  // 取消执行
  const handleCancel = async (record: TaskExecution) => {
    try {
      await cancelTaskExecution(record.id);
      message.success('执行已取消');
      loadExecutions();
    } catch (error) {
      message.error('取消失败');
    }
  };

  // 重跑失败用例
  const handleRetryFailed = async (record: TaskExecution) => {
    try {
      await retryFailedCases(record.id);
      message.success('开始重跑失败用例');
      loadExecutions();
    } catch (error) {
      message.error('重跑失败');
    }
  };

  // 表格列
  const columns: ColumnsType<TaskExecution> = [
    {
      title: '任务名称',
      dataIndex: 'taskName',
      key: 'taskName',
      width: 200,
      ellipsis: true,
      render: (text, record) => (
        <Space>
          {statusConfig[record.status]?.icon}
          <span>{text}</span>
        </Space>
      ),
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: (status: string) => (
        <Tag color={statusConfig[status]?.color}>
          {statusLabelMap[status] || status}
        </Tag>
      ),
    },
    {
      title: '执行进度',
      key: 'progress',
      width: 200,
      render: (_: unknown, record: TaskExecution) => {
        if (record.status === 'running') {
          return (
            <div>
              <Progress
                percent={record.progress}
                size="small"
                status="active"
              />
              <div style={{ fontSize: 12, color: '#666', marginTop: 4 }}>
                {record.currentStep}
              </div>
            </div>
          );
        }
        return (
          <div>
            <Progress
              percent={record.progress}
              size="small"
              status={record.status === 'completed' ? 'success' : 'exception'}
            />
          </div>
        );
      },
    },
    {
      title: '用例统计',
      key: 'caseStats',
      width: 150,
      render: (_: unknown, record: TaskExecution) => (
        <Space split={<span style={{ color: '#d9d9d9' }}>|</span>}>
          <span style={{ color: '#52c41a' }}>通过 {record.passedCases}</span>
          {record.failedCases > 0 && (
            <span style={{ color: '#ff4d4f' }}>失败 {record.failedCases}</span>
          )}
          {record.skippedCases > 0 && (
            <span style={{ color: '#faad14' }}>跳过 {record.skippedCases}</span>
          )}
        </Space>
      ),
    },
    {
      title: '开始时间',
      dataIndex: 'startTime',
      key: 'startTime',
      width: 160,
      render: (time: string) => dayjs(time).format('YYYY-MM-DD HH:mm:ss'),
    },
    {
      title: '执行时长',
      key: 'duration',
      width: 100,
      render: (_: unknown, record: TaskExecution) => {
        if (!record.startTime) return '-';
        const start = dayjs(record.startTime);
        const end = record.endTime ? dayjs(record.endTime) : dayjs();
        const duration = end.diff(start, 'second');
        if (duration < 60) return `${duration}秒`;
        if (duration < 3600) return `${Math.floor(duration / 60)}分${duration % 60}秒`;
        return `${Math.floor(duration / 3600)}时${Math.floor((duration % 3600) / 60)}分`;
      },
    },
    {
      title: '执行者',
      dataIndex: 'executor',
      key: 'executor',
      width: 100,
      render: (executor: string) => executor || '-',
    },
    {
      title: '操作',
      key: 'action',
      width: 180,
      render: (_: unknown, record: TaskExecution) => (
        <Space size="small">
          <Tooltip title="查看详情">
            <Button
              type="text"
              size="small"
              icon={<EyeOutlined />}
              onClick={() => handleViewDetail(record)}
            />
          </Tooltip>
          {record.status === 'running' && (
            <>
              <Tooltip title="暂停">
                <Button
                  type="text"
                  size="small"
                  icon={<PauseCircleOutlined />}
                  onClick={() => handlePause(record)}
                />
              </Tooltip>
              <Tooltip title="取消">
                <Button
                  type="text"
                  size="small"
                  icon={<StopOutlined />}
                  onClick={() => handleCancel(record)}
                />
              </Tooltip>
            </>
          )}
          {record.status === 'completed' && record.failedCases > 0 && (
            <Tooltip title="重跑失败用例">
              <Button
                type="text"
                size="small"
                icon={<ReloadOutlined />}
                onClick={() => handleRetryFailed(record)}
              />
            </Tooltip>
          )}
        </Space>
      ),
    },
  ];

  // 日志级别颜色
  const logLevelColor: Record<string, string> = {
    info: '#1677ff',
    warning: '#faad14',
    error: '#ff4d4f',
    debug: '#8c8c8c',
  };

  return (
    <div>
      {/* 统计卡片 */}
      <Row gutter={16} style={{ marginBottom: 16 }}>
        <Col span={6}>
          <Card bordered={false}>
            <Statistic
              title="今日执行"
              value={statistics?.todayExecutions || 0}
              suffix="次"
              prefix={<ClockCircleOutlined />}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card bordered={false}>
            <Statistic
              title="正在执行"
              value={statistics?.runningCount || 0}
              suffix="个"
              prefix={<SyncOutlined spin />}
              valueStyle={{ color: '#1677ff' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card bordered={false}>
            <Statistic
              title="成功率"
              value={(statistics?.successRate || 0) * 100}
              precision={1}
              suffix="%"
              prefix={<CheckCircleOutlined />}
              valueStyle={{ color: '#52c41a' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card bordered={false}>
            <Statistic
              title="平均耗时"
              value={statistics?.avgDuration || 0}
              suffix="秒"
              prefix={<LineChartOutlined />}
            />
          </Card>
        </Col>
      </Row>

      <Card bordered={false}>
        <Space direction="vertical" style={{ width: '100%' }} size="middle">
          {/* 工具栏 */}
          <Row justify="space-between">
            <Col>
              <Space>
                <Search
                  placeholder="搜索任务名称"
                  allowClear
                  style={{ width: 250 }}
                  onSearch={setSearchKeyword}
                />
                <Select
                  placeholder="执行状态"
                  allowClear
                  style={{ width: 120 }}
                  onChange={setFilterStatus}
                  options={[
                    { value: 'running', label: '执行中' },
                    { value: 'completed', label: '已完成' },
                    { value: 'failed', label: '失败' },
                    { value: 'paused', label: '已暂停' },
                    { value: 'cancelled', label: '已取消' },
                  ]}
                />
              </Space>
            </Col>
            <Col>
              <Button
                icon={<SyncOutlined spin={autoRefresh} />}
                onClick={() => setAutoRefresh(!autoRefresh)}
                type={autoRefresh ? 'primary' : 'default'}
              >
                自动刷新 {autoRefresh ? '开启' : '关闭'}
              </Button>
            </Col>
          </Row>

          {/* 执行列表表格 */}
          <Table
            columns={columns}
            dataSource={executions}
            rowKey="id"
            loading={loading}
            pagination={{
              current: page,
              pageSize,
              total,
              showSizeChanger: true,
              showQuickJumper: true,
              showTotal: (t) => `共 ${t} 条记录`,
              onChange: (p, ps) => {
                setPage(p);
                setPageSize(ps);
              },
            }}
          />
        </Space>
      </Card>

      {/* 执行详情抽屉 */}
      <Drawer
        title={`执行详情 - ${currentExecution?.taskName || ''}`}
        placement="right"
        width={700}
        onClose={() => setDrawerVisible(false)}
        open={drawerVisible}
      >
        {currentExecution && (
          <Space direction="vertical" style={{ width: '100%' }} size="large">
            {/* 基本信息 */}
            <Card title="执行信息" size="small">
              <Descriptions column={2} size="small">
                <Descriptions.Item label="执行ID">{currentExecution.id}</Descriptions.Item>
                <Descriptions.Item label="状态">
                  <Tag color={statusConfig[currentExecution.status]?.color}>
                    {statusLabelMap[currentExecution.status]}
                  </Tag>
                </Descriptions.Item>
                <Descriptions.Item label="开始时间">
                  {dayjs(currentExecution.startTime).format('YYYY-MM-DD HH:mm:ss')}
                </Descriptions.Item>
                <Descriptions.Item label="结束时间">
                  {currentExecution.endTime
                    ? dayjs(currentExecution.endTime).format('YYYY-MM-DD HH:mm:ss')
                    : '-'}
                </Descriptions.Item>
                <Descriptions.Item label="执行者">
                  {currentExecution.executor || '-'}
                </Descriptions.Item>
                <Descriptions.Item label="触发方式">
                  {currentExecution.triggerType || '手动触发'}
                </Descriptions.Item>
              </Descriptions>
            </Card>

            {/* 执行进度 */}
            <Card title="执行进度" size="small">
              {currentExecution.status === 'running' ? (
                <div>
                  <Progress
                    percent={currentExecution.progress}
                    status="active"
                    style={{ marginBottom: 8 }}
                  />
                  <Alert message={`当前步骤: ${currentExecution.currentStep}`} type="info" />
                </div>
              ) : (
                <Progress
                  percent={currentExecution.progress}
                  status={currentExecution.status === 'completed' ? 'success' : 'exception'}
                />
              )}

              <Row gutter={16} style={{ marginTop: 16 }}>
                <Col span={6}>
                  <Statistic
                    title="总用例"
                    value={currentExecution.totalCases}
                    valueStyle={{ fontSize: 20 }}
                  />
                </Col>
                <Col span={6}>
                  <Statistic
                    title="通过"
                    value={currentExecution.passedCases}
                    valueStyle={{ fontSize: 20, color: '#52c41a' }}
                  />
                </Col>
                <Col span={6}>
                  <Statistic
                    title="失败"
                    value={currentExecution.failedCases}
                    valueStyle={{ fontSize: 20, color: '#ff4d4f' }}
                  />
                </Col>
                <Col span={6}>
                  <Statistic
                    title="跳过"
                    value={currentExecution.skippedCases}
                    valueStyle={{ fontSize: 20, color: '#faad14' }}
                  />
                </Col>
              </Row>
            </Card>

            {/* 执行日志 */}
            <Card title="执行日志" size="small">
              <Spin spinning={logsLoading}>
                <div
                  ref={logsRef}
                  style={{
                    background: '#1e1e1e',
                    padding: 12,
                    borderRadius: 4,
                    maxHeight: 400,
                    overflow: 'auto',
                    fontFamily: 'monospace',
                    fontSize: 12,
                  }}
                >
                  {logs.length > 0 ? (
                    logs.map((log, index) => (
                      <div key={index} style={{ marginBottom: 4 }}>
                        <span style={{ color: '#666' }}>
                          [{dayjs(log.timestamp).format('HH:mm:ss.SSS')}]
                        </span>{' '}
                        <span style={{ color: logLevelColor[log.level] || '#fff' }}>
                          [{log.level.toUpperCase()}]
                        </span>{' '}
                        <span style={{ color: '#fff' }}>{log.message}</span>
                      </div>
                    ))
                  ) : (
                    <Empty description="暂无日志" image={Empty.PRESENTED_IMAGE_SIMPLE} />
                  )}
                </div>
              </Spin>
            </Card>

            {/* 失败用例列表 */}
            {currentExecution.failedCases > 0 && currentExecution.failedCaseDetails && (
              <Card title="失败用例" size="small">
                <Table
                  dataSource={currentExecution.failedCaseDetails}
                  rowKey="id"
                  size="small"
                  pagination={false}
                  columns={[
                    {
                      title: '用例名称',
                      dataIndex: 'caseName',
                      key: 'caseName',
                      ellipsis: true,
                    },
                    {
                      title: '失败原因',
                      dataIndex: 'failureReason',
                      key: 'failureReason',
                      ellipsis: true,
                      render: (text: string) => (
                        <Tooltip title={text}>
                          <span style={{ color: '#ff4d4f' }}>{text}</span>
                        </Tooltip>
                      ),
                    },
                  ]}
                />
              </Card>
            )}
          </Space>
        )}
      </Drawer>
    </div>
  );
};

export default TaskExecutionMonitorPage;