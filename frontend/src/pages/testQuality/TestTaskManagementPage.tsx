/**
 * 自动化测试任务管理页面
 * 支持任务配置、执行监控、定时任务管理
 */
import React, { useState, useEffect } from 'react';
import {
  Card,
  Table,
  Button,
  Space,
  Input,
  Select,
  Tag,
  Modal,
  Form,
  Progress,
  message,
  Tooltip,
  Dropdown,
  Popconfirm,
  Drawer,
  Timeline,
  Badge,
  Statistic,
  Row,
  Col,
  Divider,
} from 'antd';
import {
  PlusOutlined,
  SearchOutlined,
  PlayCircleOutlined,
  PauseOutlined,
  StopOutlined,
  ReloadOutlined,
  EyeOutlined,
  EditOutlined,
  DeleteOutlined,
  MoreOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  SyncOutlined,
} from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import dayjs from 'dayjs';
import {
  getTestTasks,
  createTestTask,
  updateTestTask,
  deleteTestTask,
  executeTestTask,
  getTaskExecution,
  pauseTaskExecution,
  cancelTaskExecution,
  retryFailedCases,
  batchExecuteTasks,
} from '../../services/testQuality';
import type {
  TestTask,
  TaskExecution,
  ExecutionStrategy,
  TriggerCondition,
  ExecutionLog,
} from '../../types/testQuality';

const { Search } = Input;

const TestTaskManagementPage: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [tasks, setTasks] = useState<TestTask[]>([]);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [searchKeyword, setSearchKeyword] = useState('');
  const [filterStatus, setFilterStatus] = useState<string>();
  const [modalVisible, setModalVisible] = useState(false);
  const [drawerVisible, setDrawerVisible] = useState(false);
  const [currentTask, setCurrentTask] = useState<TestTask | null>(null);
  const [currentExecution, setCurrentExecution] = useState<TaskExecution | null>(null);
  const [selectedRowKeys, setSelectedRowKeys] = useState<React.Key[]>([]);
  const [form] = Form.useForm();

  // 加载任务列表
  const loadTasks = async () => {
    setLoading(true);
    try {
      const result = await getTestTasks({
        page,
        pageSize,
        status: filterStatus,
      });
      setTasks(result.data);
      setTotal(result.total);
    } catch (error) {
      message.error('加载任务列表失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadTasks();
  }, [page, pageSize, searchKeyword, filterStatus]);

  // 状态颜色映射
  const statusColorMap: Record<string, string> = {
    idle: 'default',
    running: 'processing',
    completed: 'success',
    failed: 'error',
    cancelled: 'warning',
  };

  const statusLabelMap: Record<string, string> = {
    idle: '空闲',
    running: '运行中',
    completed: '已完成',
    failed: '失败',
    cancelled: '已取消',
  };

  // 触发条件标签映射
  const triggerLabelMap: Record<TriggerCondition, string> = {
    manual: '手动触发',
    scheduled: '定时触发',
    webhook: 'Webhook触发',
    commit: '提交触发',
  };

  // 表格列定义
  const columns: ColumnsType<TestTask> = [
    {
      title: '任务名称',
      dataIndex: 'name',
      key: 'name',
      width: 200,
      ellipsis: true,
    },
    {
      title: '测试套件',
      dataIndex: 'testSuiteName',
      key: 'testSuiteName',
      width: 150,
      ellipsis: true,
    },
    {
      title: '执行环境',
      dataIndex: 'environment',
      key: 'environment',
      width: 100,
    },
    {
      title: '触发条件',
      dataIndex: 'triggerCondition',
      key: 'triggerCondition',
      width: 100,
      render: (trigger: TriggerCondition) => <Tag>{triggerLabelMap[trigger]}</Tag>,
    },
    {
      title: '定时配置',
      dataIndex: 'schedule',
      key: 'schedule',
      width: 120,
      render: (schedule: string) => schedule || '-',
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: (status: string) => (
        <Tag color={statusColorMap[status]}>{statusLabelMap[status]}</Tag>
      ),
    },
    {
      title: '最近执行',
      dataIndex: 'lastExecutionAt',
      key: 'lastExecutionAt',
      width: 160,
      render: (date: string) => (date ? dayjs(date).format('YYYY-MM-DD HH:mm') : '-'),
    },
    {
      title: '通过率',
      dataIndex: 'passRate',
      key: 'passRate',
      width: 100,
      render: (rate: number) =>
        rate !== undefined ? (
          <Progress
            percent={rate * 100}
            size="small"
            status={rate >= 0.9 ? 'success' : rate >= 0.7 ? 'normal' : 'exception'}
          />
        ) : (
          '-'
        ),
    },
    {
      title: '操作',
      key: 'action',
      width: 200,
      render: (_: unknown, record: TestTask) => (
        <Space size="small">
          {record.status === 'idle' && (
            <Tooltip title="执行">
              <Button
                type="text"
                size="small"
                icon={<PlayCircleOutlined />}
                onClick={() => handleExecute(record)}
              />
            </Tooltip>
          )}
          {record.status === 'running' && (
            <>
              <Tooltip title="暂停">
                <Button
                  type="text"
                  size="small"
                  icon={<PauseOutlined />}
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
          <Tooltip title="查看详情">
            <Button
              type="text"
              size="small"
              icon={<EyeOutlined />}
              onClick={() => handleViewDetail(record)}
            />
          </Tooltip>
          <Tooltip title="编辑">
            <Button
              type="text"
              size="small"
              icon={<EditOutlined />}
              onClick={() => handleEdit(record)}
            />
          </Tooltip>
          <Dropdown
            menu={{
              items: [
                {
                  key: 'delete',
                  label: (
                    <Popconfirm
                      title="确定删除此任务？"
                      onConfirm={() => handleDelete(record.id)}
                    >
                      <span style={{ color: 'red' }}>删除</span>
                    </Popconfirm>
                  ),
                  icon: <DeleteOutlined />,
                },
              ],
            }}
          >
            <Button type="text" size="small" icon={<MoreOutlined />} />
          </Dropdown>
        </Space>
      ),
    },
  ];

  // 新建任务
  const handleCreate = () => {
    setCurrentTask(null);
    form.resetFields();
    form.setFieldsValue({
      strategy: 'sequential',
      triggerCondition: 'manual',
    });
    setModalVisible(true);
  };

  // 编辑任务
  const handleEdit = (record: TestTask) => {
    setCurrentTask(record);
    form.setFieldsValue(record);
    setModalVisible(true);
  };

  // 执行任务
  const handleExecute = async (record: TestTask) => {
    try {
      const execution = await executeTestTask(record.id);
      message.success('任务开始执行');
      loadTasks();
      // 打开执行详情
      setCurrentExecution(execution);
      setDrawerVisible(true);
    } catch (error) {
      message.error('执行任务失败');
    }
  };

  // 暂停任务
  const handlePause = async (record: TestTask) => {
    try {
      // 获取最新的执行记录
      const execution = await getTaskExecution(record.id);
      await pauseTaskExecution(execution.id);
      message.success('任务已暂停');
      loadTasks();
    } catch (error) {
      message.error('暂停任务失败');
    }
  };

  // 取消任务
  const handleCancel = async (record: TestTask) => {
    try {
      const execution = await getTaskExecution(record.id);
      await cancelTaskExecution(execution.id);
      message.success('任务已取消');
      loadTasks();
    } catch (error) {
      message.error('取消任务失败');
    }
  };

  // 删除任务
  const handleDelete = async (id: string) => {
    try {
      await deleteTestTask(id);
      message.success('删除成功');
      loadTasks();
    } catch (error) {
      message.error('删除失败');
    }
  };

  // 查看详情
  const handleViewDetail = async (record: TestTask) => {
    setLoading(true);
    try {
      if (record.status === 'running') {
        const execution = await getTaskExecution(record.id);
        setCurrentExecution(execution);
      } else {
        setCurrentExecution({
          id: 'demo',
          taskId: record.id,
          taskName: record.name,
          status: 'completed',
          startTime: record.lastExecutionAt || '',
          totalCases: 100,
          passedCases: Math.floor((record.passRate || 0) * 100),
          failedCases: Math.floor((1 - (record.passRate || 0)) * 100),
          skippedCases: 0,
          progress: 100,
          logs: [],
        });
      }
      setDrawerVisible(true);
    } catch (error) {
      message.error('获取执行详情失败');
    } finally {
      setLoading(false);
    }
  };

  // 保存任务
  const handleSave = async () => {
    try {
      const values = await form.validateFields();
      if (currentTask) {
        await updateTestTask(currentTask.id, values);
        message.success('更新成功');
      } else {
        await createTestTask(values);
        message.success('创建成功');
      }
      setModalVisible(false);
      loadTasks();
    } catch (error) {
      message.error('保存失败');
    }
  };

  // 批量执行
  const handleBatchExecute = async () => {
    if (selectedRowKeys.length === 0) {
      message.warning('请选择要执行的任务');
      return;
    }
    try {
      const result = await batchExecuteTasks(selectedRowKeys as string[]);
      message.success(`成功执行 ${result.success} 个任务，失败 ${result.failed} 个`);
      setSelectedRowKeys([]);
      loadTasks();
    } catch (error) {
      message.error('批量执行失败');
    }
  };

  // 重跑失败用例
  const handleRetryFailed = async () => {
    if (!currentExecution) return;
    try {
      const execution = await retryFailedCases(currentExecution.id);
      setCurrentExecution(execution);
      message.success('开始重跑失败用例');
    } catch (error) {
      message.error('重跑失败用例失败');
    }
  };

  // 日志级别颜色
  const logLevelColor: Record<string, string> = {
    info: 'blue',
    warning: 'orange',
    error: 'red',
  };

  return (
    <div>
      <Card bordered={false}>
        <Space direction="vertical" style={{ width: '100%' }} size="middle">
          {/* 搜索和筛选 */}
          <Space wrap>
            <Search
              placeholder="搜索任务名称"
              allowClear
              style={{ width: 250 }}
              onSearch={setSearchKeyword}
            />
            <Select
              placeholder="任务状态"
              allowClear
              style={{ width: 120 }}
              onChange={setFilterStatus}
              options={[
                { value: 'idle', label: '空闲' },
                { value: 'running', label: '运行中' },
                { value: 'completed', label: '已完成' },
                { value: 'failed', label: '失败' },
                { value: 'cancelled', label: '已取消' },
              ]}
            />
          </Space>

          {/* 操作按钮 */}
          <Space>
            <Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>
              新建任务
            </Button>
            <Button
              icon={<PlayCircleOutlined />}
              onClick={handleBatchExecute}
              disabled={selectedRowKeys.length === 0}
            >
              批量执行 ({selectedRowKeys.length})
            </Button>
          </Space>

          {/* 表格 */}
          <Table
            columns={columns}
            dataSource={tasks}
            rowKey="id"
            loading={loading}
            rowSelection={{
              selectedRowKeys,
              onChange: setSelectedRowKeys,
              getCheckboxProps: (record) => ({
                disabled: record.status === 'running',
              }),
            }}
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

      {/* 新建/编辑任务弹窗 */}
      <Modal
        title={currentTask ? '编辑测试任务' : '新建测试任务'}
        open={modalVisible}
        onOk={handleSave}
        onCancel={() => setModalVisible(false)}
        width={600}
        destroyOnClose
      >
        <Form form={form} layout="vertical">
          <Form.Item
            name="name"
            label="任务名称"
            rules={[{ required: true, message: '请输入任务名称' }]}
          >
            <Input placeholder="请输入任务名称" />
          </Form.Item>

          <Form.Item name="description" label="任务描述">
            <Input.TextArea rows={2} placeholder="请输入任务描述" />
          </Form.Item>

          <Space style={{ width: '100%' }} size="large">
            <Form.Item
              name="testSuiteId"
              label="测试套件"
              rules={[{ required: true, message: '请选择测试套件' }]}
              style={{ width: 200 }}
            >
              <Select placeholder="请选择测试套件" options={[]} />
            </Form.Item>

            <Form.Item
              name="environment"
              label="执行环境"
              rules={[{ required: true, message: '请选择执行环境' }]}
              style={{ width: 150 }}
            >
              <Select
                placeholder="请选择"
                options={[
                  { value: 'dev', label: '开发环境' },
                  { value: 'test', label: '测试环境' },
                  { value: 'staging', label: '预发布环境' },
                  { value: 'production', label: '生产环境' },
                ]}
              />
            </Form.Item>
          </Space>

          <Space style={{ width: '100%' }} size="large">
            <Form.Item
              name="strategy"
              label="执行策略"
              style={{ width: 150 }}
            >
              <Select
                placeholder="请选择"
                options={[
                  { value: 'sequential', label: '顺序执行' },
                  { value: 'parallel', label: '并行执行' },
                ]}
              />
            </Form.Item>

            <Form.Item
              name="triggerCondition"
              label="触发条件"
              style={{ width: 150 }}
            >
              <Select
                placeholder="请选择"
                options={[
                  { value: 'manual', label: '手动触发' },
                  { value: 'scheduled', label: '定时触发' },
                  { value: 'webhook', label: 'Webhook触发' },
                  { value: 'commit', label: '提交触发' },
                ]}
              />
            </Form.Item>
          </Space>

          <Form.Item
            name="schedule"
            label="定时配置"
            extra="支持 Cron 表达式，如: 0 0 * * * (每天凌晨执行)"
          >
            <Input placeholder="0 0 * * *" />
          </Form.Item>
        </Form>
      </Modal>

      {/* 执行详情抽屉 */}
      <Drawer
        title={`执行详情 - ${currentExecution?.taskName || ''}`}
        placement="right"
        width={600}
        onClose={() => setDrawerVisible(false)}
        open={drawerVisible}
        extra={
          currentExecution?.status === 'completed' &&
          currentExecution.failedCases > 0 && (
            <Button icon={<ReloadOutlined />} onClick={handleRetryFailed}>
              重跑失败用例
            </Button>
          )
        }
      >
        {currentExecution && (
          <Space direction="vertical" style={{ width: '100%' }} size="large">
            {/* 执行状态概览 */}
            <Row gutter={16}>
              <Col span={6}>
                <Statistic
                  title="执行状态"
                  value={currentExecution.status}
                  valueStyle={{
                    color:
                      currentExecution.status === 'completed'
                        ? '#3f8600'
                        : currentExecution.status === 'running'
                          ? '#1677ff'
                          : currentExecution.status === 'failed'
                            ? '#cf1322'
                            : '#666',
                  }}
                />
              </Col>
              <Col span={6}>
                <Statistic
                  title="总用例数"
                  value={currentExecution.totalCases}
                />
              </Col>
              <Col span={6}>
                <Statistic
                  title="通过数"
                  value={currentExecution.passedCases}
                  valueStyle={{ color: '#3f8600' }}
                />
              </Col>
              <Col span={6}>
                <Statistic
                  title="失败数"
                  value={currentExecution.failedCases}
                  valueStyle={{ color: '#cf1322' }}
                />
              </Col>
            </Row>

            {/* 执行进度 */}
            {currentExecution.status === 'running' && (
              <div>
                <div style={{ marginBottom: 8 }}>执行进度</div>
                <Progress percent={currentExecution.progress} status="active" />
                <div style={{ color: '#666', marginTop: 8 }}>
                  当前步骤: {currentExecution.currentStep}
                </div>
              </div>
            )}

            <Divider />

            {/* 执行日志 */}
            <div>
              <div style={{ marginBottom: 8, fontWeight: 500 }}>执行日志</div>
              <div
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
                {currentExecution.logs.length > 0 ? (
                  currentExecution.logs.map((log: ExecutionLog, index: number) => (
                    <div key={index} style={{ marginBottom: 4 }}>
                      <span style={{ color: '#666' }}>
                        [{dayjs(log.timestamp).format('HH:mm:ss')}]
                      </span>{' '}
                      <span style={{ color: logLevelColor[log.level] || '#fff' }}>
                        [{log.level.toUpperCase()}]
                      </span>{' '}
                      <span style={{ color: '#fff' }}>{log.message}</span>
                    </div>
                  ))
                ) : (
                  <div style={{ color: '#666' }}>暂无日志</div>
                )}
              </div>
            </div>
          </Space>
        )}
      </Drawer>
    </div>
  );
};

export default TestTaskManagementPage;