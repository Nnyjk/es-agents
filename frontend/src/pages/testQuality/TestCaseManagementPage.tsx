/**
 * 测试用例管理页面
 * 支持测试用例的增删改查、导入导出、执行记录查看
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
  InputNumber,
  DatePicker,
  Tooltip,
  message,
  Dropdown,
  Upload,
  Popconfirm,
  Tabs,
  Descriptions,
  List,
  Divider,
} from 'antd';
import {
  PlusOutlined,
  SearchOutlined,
  EditOutlined,
  DeleteOutlined,
  DownloadOutlined,
  UploadOutlined,
  MoreOutlined,
  FileTextOutlined,
  HistoryOutlined,
  CopyOutlined,
} from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import type { UploadProps } from 'antd';
import dayjs from 'dayjs';
import {
  getTestCases,
  createTestCase,
  updateTestCase,
  deleteTestCase,
  importTestCases,
  exportTestCases,
  getCaseExecutions,
} from '../../services/testQuality';
import type {
  TestCase,
  TestCasePriority,
  TestCaseType,
  TestCaseStatus,
  TestCaseExecution,
} from '../../types/testQuality';

const { Search } = Input;
const { TextArea } = Input;
const { RangePicker } = DatePicker;

const TestCaseManagementPage: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [testCases, setTestCases] = useState<TestCase[]>([]);
  const [executions, setExecutions] = useState<TestCaseExecution[]>([]);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [searchKeyword, setSearchKeyword] = useState('');
  const [filterType, setFilterType] = useState<string>();
  const [filterPriority, setFilterPriority] = useState<string>();
  const [filterStatus, setFilterStatus] = useState<string>();
  const [modalVisible, setModalVisible] = useState(false);
  const [executionModalVisible, setExecutionModalVisible] = useState(false);
  const [currentTestCase, setCurrentTestCase] = useState<TestCase | null>(null);
  const [form] = Form.useForm();
  const [stepForm] = Form.useForm();

  // 加载测试用例列表
  const loadTestCases = async () => {
    setLoading(true);
    try {
      const result = await getTestCases({
        page,
        pageSize,
        keyword: searchKeyword,
        type: filterType,
        priority: filterPriority,
        status: filterStatus,
      });
      setTestCases(result.data);
      setTotal(result.total);
    } catch (error) {
      message.error('加载测试用例列表失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadTestCases();
  }, [page, pageSize, searchKeyword, filterType, filterPriority, filterStatus]);

  // 加载执行记录
  const loadExecutions = async (testCaseId: string) => {
    setLoading(true);
    try {
      const result = await getCaseExecutions({ testCaseId, page: 1, pageSize: 100 });
      setExecutions(result.data);
    } catch (error) {
      message.error('加载执行记录失败');
    } finally {
      setLoading(false);
    }
  };

  // 优先级颜色映射
  const priorityColorMap: Record<TestCasePriority, string> = {
    P0: 'red',
    P1: 'orange',
    P2: 'blue',
    P3: 'default',
  };

  // 类型标签映射
  const typeLabelMap: Record<TestCaseType, string> = {
    functional: '功能测试',
    performance: '性能测试',
    security: '安全测试',
    compatibility: '兼容性测试',
    integration: '集成测试',
    unit: '单元测试',
  };

  // 状态颜色映射
  const statusColorMap: Record<TestCaseStatus, string> = {
    draft: 'default',
    reviewing: 'processing',
    approved: 'success',
    deprecated: 'warning',
  };

  const statusLabelMap: Record<TestCaseStatus, string> = {
    draft: '草稿',
    reviewing: '评审中',
    approved: '已通过',
    deprecated: '已废弃',
  };

  // 表格列定义
  const columns: ColumnsType<TestCase> = [
    {
      title: '用例编号',
      dataIndex: 'id',
      key: 'id',
      width: 120,
      render: (text: string) => <a>{text}</a>,
    },
    {
      title: '用例名称',
      dataIndex: 'name',
      key: 'name',
      ellipsis: true,
    },
    {
      title: '类型',
      dataIndex: 'type',
      key: 'type',
      width: 100,
      render: (type: TestCaseType) => <Tag>{typeLabelMap[type]}</Tag>,
    },
    {
      title: '优先级',
      dataIndex: 'priority',
      key: 'priority',
      width: 80,
      render: (priority: TestCasePriority) => (
        <Tag color={priorityColorMap[priority]}>{priority}</Tag>
      ),
    },
    {
      title: '模块',
      dataIndex: 'module',
      key: 'module',
      width: 120,
      ellipsis: true,
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: (status: TestCaseStatus) => (
        <Tag color={statusColorMap[status]}>{statusLabelMap[status]}</Tag>
      ),
    },
    {
      title: '版本',
      dataIndex: 'version',
      key: 'version',
      width: 80,
      render: (v: number) => `v${v}`,
    },
    {
      title: '更新时间',
      dataIndex: 'updatedAt',
      key: 'updatedAt',
      width: 160,
      render: (date: string) => dayjs(date).format('YYYY-MM-DD HH:mm'),
    },
    {
      title: '操作',
      key: 'action',
      width: 180,
      render: (_: unknown, record: TestCase) => (
        <Space size="small">
          <Tooltip title="编辑">
            <Button
              type="text"
              size="small"
              icon={<EditOutlined />}
              onClick={() => handleEdit(record)}
            />
          </Tooltip>
          <Tooltip title="执行记录">
            <Button
              type="text"
              size="small"
              icon={<HistoryOutlined />}
              onClick={() => {
                setCurrentTestCase(record);
                loadExecutions(record.id);
                setExecutionModalVisible(true);
              }}
            />
          </Tooltip>
          <Tooltip title="复制">
            <Button
              type="text"
              size="small"
              icon={<CopyOutlined />}
              onClick={() => handleCopy(record)}
            />
          </Tooltip>
          <Dropdown
            menu={{
              items: [
                {
                  key: 'delete',
                  label: (
                    <Popconfirm
                      title="确定删除此测试用例？"
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

  // 执行记录表格列
  const executionColumns: ColumnsType<TestCaseExecution> = [
    {
      title: '执行时间',
      dataIndex: 'executedAt',
      key: 'executedAt',
      width: 160,
      render: (date: string) => dayjs(date).format('YYYY-MM-DD HH:mm:ss'),
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: (status: string) => {
        const colorMap: Record<string, string> = {
          passed: 'success',
          failed: 'error',
          blocked: 'warning',
          skipped: 'default',
        };
        const labelMap: Record<string, string> = {
          passed: '通过',
          failed: '失败',
          blocked: '阻塞',
          skipped: '跳过',
        };
        return <Tag color={colorMap[status]}>{labelMap[status]}</Tag>;
      },
    },
    {
      title: '耗时(s)',
      dataIndex: 'duration',
      key: 'duration',
      width: 80,
      render: (v: number) => (v / 1000).toFixed(2),
    },
    {
      title: '执行人',
      dataIndex: 'executor',
      key: 'executor',
      width: 100,
    },
    {
      title: '错误信息',
      dataIndex: 'errorMessage',
      key: 'errorMessage',
      ellipsis: true,
    },
  ];

  // 新建测试用例
  const handleCreate = () => {
    setCurrentTestCase(null);
    form.resetFields();
    form.setFieldsValue({
      steps: [{ step: 1, action: '', expectedResult: '' }],
    });
    setModalVisible(true);
  };

  // 编辑测试用例
  const handleEdit = (record: TestCase) => {
    setCurrentTestCase(record);
    form.setFieldsValue(record);
    setModalVisible(true);
  };

  // 复制测试用例
  const handleCopy = (record: TestCase) => {
    setCurrentTestCase(null);
    form.setFieldsValue({
      ...record,
      name: `${record.name} (副本)`,
      status: 'draft',
      version: 1,
    });
    setModalVisible(true);
  };

  // 删除测试用例
  const handleDelete = async (id: string) => {
    try {
      await deleteTestCase(id);
      message.success('删除成功');
      loadTestCases();
    } catch (error) {
      message.error('删除失败');
    }
  };

  // 保存测试用例
  const handleSave = async () => {
    try {
      const values = await form.validateFields();
      if (currentTestCase) {
        await updateTestCase(currentTestCase.id, values);
        message.success('更新成功');
      } else {
        await createTestCase(values);
        message.success('创建成功');
      }
      setModalVisible(false);
      loadTestCases();
    } catch (error) {
      message.error('保存失败');
    }
  };

  // 导入测试用例
  const uploadProps: UploadProps = {
    accept: '.xlsx,.xls,.json',
    showUploadList: false,
    beforeUpload: async (file) => {
      try {
        const result = await importTestCases(file);
        message.success(`导入成功 ${result.success} 条，失败 ${result.failed} 条`);
        loadTestCases();
      } catch (error) {
        message.error('导入失败');
      }
      return false;
    },
  };

  // 导出测试用例
  const handleExport = async (format: 'excel' | 'json') => {
    try {
      const selectedIds = testCases.map((tc) => tc.id);
      const blob = await exportTestCases(selectedIds, format);
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `test-cases.${format === 'excel' ? 'xlsx' : 'json'}`;
      a.click();
      window.URL.revokeObjectURL(url);
      message.success('导出成功');
    } catch (error) {
      message.error('导出失败');
    }
  };

  return (
    <div>
      <Card bordered={false}>
        <Space direction="vertical" style={{ width: '100%' }} size="middle">
          {/* 搜索和筛选 */}
          <Space wrap>
            <Search
              placeholder="搜索用例名称/编号"
              allowClear
              style={{ width: 250 }}
              onSearch={setSearchKeyword}
            />
            <Select
              placeholder="用例类型"
              allowClear
              style={{ width: 120 }}
              onChange={setFilterType}
              options={[
                { value: 'functional', label: '功能测试' },
                { value: 'performance', label: '性能测试' },
                { value: 'security', label: '安全测试' },
                { value: 'compatibility', label: '兼容性测试' },
                { value: 'integration', label: '集成测试' },
                { value: 'unit', label: '单元测试' },
              ]}
            />
            <Select
              placeholder="优先级"
              allowClear
              style={{ width: 100 }}
              onChange={setFilterPriority}
              options={[
                { value: 'P0', label: 'P0' },
                { value: 'P1', label: 'P1' },
                { value: 'P2', label: 'P2' },
                { value: 'P3', label: 'P3' },
              ]}
            />
            <Select
              placeholder="状态"
              allowClear
              style={{ width: 100 }}
              onChange={setFilterStatus}
              options={[
                { value: 'draft', label: '草稿' },
                { value: 'reviewing', label: '评审中' },
                { value: 'approved', label: '已通过' },
                { value: 'deprecated', label: '已废弃' },
              ]}
            />
          </Space>

          {/* 操作按钮 */}
          <Space>
            <Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>
              新建用例
            </Button>
            <Upload {...uploadProps}>
              <Button icon={<UploadOutlined />}>导入用例</Button>
            </Upload>
            <Dropdown
              menu={{
                items: [
                  { key: 'excel', label: '导出为 Excel' },
                  { key: 'json', label: '导出为 JSON' },
                ],
                onClick: ({ key }) => handleExport(key as 'excel' | 'json'),
              }}
            >
              <Button icon={<DownloadOutlined />}>导出用例</Button>
            </Dropdown>
          </Space>

          {/* 表格 */}
          <Table
            columns={columns}
            dataSource={testCases}
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

      {/* 新建/编辑用例弹窗 */}
      <Modal
        title={currentTestCase ? '编辑测试用例' : '新建测试用例'}
        open={modalVisible}
        onOk={handleSave}
        onCancel={() => setModalVisible(false)}
        width={800}
        destroyOnClose
      >
        <Form form={form} layout="vertical">
          <Form.Item
            name="name"
            label="用例名称"
            rules={[{ required: true, message: '请输入用例名称' }]}
          >
            <Input placeholder="请输入用例名称" />
          </Form.Item>

          <Space style={{ width: '100%' }} size="large">
            <Form.Item
              name="type"
              label="用例类型"
              rules={[{ required: true, message: '请选择用例类型' }]}
              style={{ width: 200 }}
            >
              <Select
                placeholder="请选择"
                options={[
                  { value: 'functional', label: '功能测试' },
                  { value: 'performance', label: '性能测试' },
                  { value: 'security', label: '安全测试' },
                  { value: 'compatibility', label: '兼容性测试' },
                  { value: 'integration', label: '集成测试' },
                  { value: 'unit', label: '单元测试' },
                ]}
              />
            </Form.Item>

            <Form.Item
              name="priority"
              label="优先级"
              rules={[{ required: true, message: '请选择优先级' }]}
              style={{ width: 120 }}
            >
              <Select
                placeholder="请选择"
                options={[
                  { value: 'P0', label: 'P0 - 最高' },
                  { value: 'P1', label: 'P1 - 高' },
                  { value: 'P2', label: 'P2 - 中' },
                  { value: 'P3', label: 'P3 - 低' },
                ]}
              />
            </Form.Item>

            <Form.Item name="module" label="所属模块" style={{ width: 200 }}>
              <Input placeholder="请输入模块名称" />
            </Form.Item>
          </Space>

          <Form.Item name="description" label="用例描述">
            <TextArea rows={2} placeholder="请输入用例描述" />
          </Form.Item>

          <Form.Item name="preconditions" label="前置条件">
            <TextArea rows={2} placeholder="请输入前置条件" />
          </Form.Item>

          <Form.Item name="expectedResults" label="预期结果">
            <TextArea rows={2} placeholder="请输入预期结果" />
          </Form.Item>

          <Form.Item name="tags" label="标签">
            <Select
              mode="tags"
              placeholder="输入标签后按回车添加"
              style={{ width: '100%' }}
            />
          </Form.Item>
        </Form>
      </Modal>

      {/* 执行记录弹窗 */}
      <Modal
        title={`执行记录 - ${currentTestCase?.name || ''}`}
        open={executionModalVisible}
        onCancel={() => setExecutionModalVisible(false)}
        footer={null}
        width={900}
      >
        <Table
          columns={executionColumns}
          dataSource={executions}
          rowKey="id"
          loading={loading}
          pagination={{ pageSize: 10 }}
        />
      </Modal>
    </div>
  );
};

export default TestCaseManagementPage;