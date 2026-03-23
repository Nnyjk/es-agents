/**
 * 测试套件管理页面
 * 支持测试套件的创建、编辑、用例管理
 */
import React, { useState, useEffect } from 'react';
import {
  Card,
  Table,
  Button,
  Space,
  Input,
  Modal,
  Form,
  Tag,
  message,
  Popconfirm,
  Transfer,
  Drawer,
  Select,
  Tooltip,
  Badge,
  Descriptions,
} from 'antd';
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  EyeOutlined,
  CopyOutlined,
} from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import type { TransferProps } from 'antd';
import dayjs from 'dayjs';
import {
  getTestSuites,
  getTestSuite,
  createTestSuite,
  updateTestSuite,
  deleteTestSuite,
  copyTestSuite,
  getTestCases,
} from '../../services/testQuality';
import type { TestSuite, TestCase } from '../../types/testQuality';

const { Search } = Input;

const TestSuiteManagementPage: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [suites, setSuites] = useState<TestSuite[]>([]);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [searchKeyword, setSearchKeyword] = useState('');
  const [modalVisible, setModalVisible] = useState(false);
  const [drawerVisible, setDrawerVisible] = useState(false);
  const [currentSuite, setCurrentSuite] = useState<TestSuite | null>(null);
  const [form] = Form.useForm();
  const [allTestCases, setAllTestCases] = useState<TestCase[]>([]);
  const [selectedCaseIds, setSelectedCaseIds] = useState<string[]>([]);

  // 加载测试套件列表
  const loadSuites = async () => {
    setLoading(true);
    try {
      const result = await getTestSuites({
        page,
        pageSize,
        keyword: searchKeyword,
      });
      setSuites(result.data);
      setTotal(result.total);
    } catch (error) {
      message.error('加载测试套件列表失败');
    } finally {
      setLoading(false);
    }
  };

  // 加载所有测试用例（用于 Transfer 组件）
  const loadTestCases = async () => {
    try {
      const result = await getTestCases({ page: 1, pageSize: 1000 });
      setAllTestCases(result.data);
    } catch (error) {
      console.error('加载测试用例失败', error);
    }
  };

  useEffect(() => {
    loadSuites();
  }, [page, pageSize, searchKeyword]);

  // 状态颜色映射
  const statusColorMap: Record<string, string> = {
    active: 'success',
    inactive: 'default',
    archived: 'warning',
  };

  const statusLabelMap: Record<string, string> = {
    active: '活跃',
    inactive: '未激活',
    archived: '已归档',
  };

  // 表格列定义
  const columns: ColumnsType<TestSuite> = [
    {
      title: '套件名称',
      dataIndex: 'name',
      key: 'name',
      width: 200,
      ellipsis: true,
    },
    {
      title: '描述',
      dataIndex: 'description',
      key: 'description',
      ellipsis: true,
    },
    {
      title: '用例数量',
      dataIndex: 'testCases',
      key: 'caseCount',
      width: 100,
      align: 'center',
      render: (cases: string[]) => <Badge count={cases?.length || 0} showZero color="blue" />,
    },
    {
      title: '环境',
      dataIndex: 'environment',
      key: 'environment',
      width: 100,
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 80,
      render: (status: string) => <Tag color={statusColorMap[status]}>{statusLabelMap[status]}</Tag>,
    },
    {
      title: '创建时间',
      dataIndex: 'createdAt',
      key: 'createdAt',
      width: 160,
      render: (date: string) => dayjs(date).format('YYYY-MM-DD HH:mm'),
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
      width: 200,
      render: (_: unknown, record: TestSuite) => (
        <Space size="small">
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
          <Tooltip title="复制">
            <Button
              type="text"
              size="small"
              icon={<CopyOutlined />}
              onClick={() => handleCopy(record)}
            />
          </Tooltip>
          <Popconfirm
            title="确定删除此测试套件？"
            onConfirm={() => handleDelete(record.id)}
          >
            <Button type="text" size="small" icon={<DeleteOutlined />} danger />
          </Popconfirm>
        </Space>
      ),
    },
  ];

  // 新建测试套件
  const handleCreate = () => {
    setCurrentSuite(null);
    setSelectedCaseIds([]);
    form.resetFields();
    form.setFieldsValue({
      status: 'active',
    });
    loadTestCases();
    setModalVisible(true);
  };

  // 编辑测试套件
  const handleEdit = async (record: TestSuite) => {
    setLoading(true);
    try {
      const suite = await getTestSuite(record.id);
      setCurrentSuite(suite);
      setSelectedCaseIds(suite.testCases || []);
      form.setFieldsValue(suite);
      loadTestCases();
      setModalVisible(true);
    } catch (error) {
      message.error('获取套件详情失败');
    } finally {
      setLoading(false);
    }
  };

  // 查看详情
  const handleViewDetail = async (record: TestSuite) => {
    setLoading(true);
    try {
      const suite = await getTestSuite(record.id);
      setCurrentSuite(suite);
      setDrawerVisible(true);
    } catch (error) {
      message.error('获取套件详情失败');
    } finally {
      setLoading(false);
    }
  };

  // 复制套件
  const handleCopy = async (record: TestSuite) => {
    try {
      await copyTestSuite(record.id);
      message.success('复制成功');
      loadSuites();
    } catch (error) {
      message.error('复制失败');
    }
  };

  // 删除套件
  const handleDelete = async (id: string) => {
    try {
      await deleteTestSuite(id);
      message.success('删除成功');
      loadSuites();
    } catch (error) {
      message.error('删除失败');
    }
  };

  // 保存套件
  const handleSave = async () => {
    try {
      const values = await form.validateFields();
      const data = {
        ...values,
        testCases: selectedCaseIds,
      };
      if (currentSuite) {
        await updateTestSuite(currentSuite.id, data);
        message.success('更新成功');
      } else {
        await createTestSuite(data);
        message.success('创建成功');
      }
      setModalVisible(false);
      loadSuites();
    } catch (error) {
      message.error('保存失败');
    }
  };

  // Transfer 数据源
  const transferDataSource: TransferProps['dataSource'] = allTestCases.map((item) => ({
    key: item.id,
    title: item.name,
    description: item.module,
  }));

  return (
    <div>
      <Card bordered={false}>
        <Space direction="vertical" style={{ width: '100%' }} size="middle">
          {/* 搜索栏 */}
          <Space>
            <Search
              placeholder="搜索套件名称"
              allowClear
              style={{ width: 300 }}
              onSearch={setSearchKeyword}
            />
            <Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>
              新建测试套件
            </Button>
          </Space>

          {/* 表格 */}
          <Table
            columns={columns}
            dataSource={suites}
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

      {/* 新建/编辑测试套件弹窗 */}
      <Modal
        title={currentSuite ? '编辑测试套件' : '新建测试套件'}
        open={modalVisible}
        onOk={handleSave}
        onCancel={() => setModalVisible(false)}
        width={700}
        destroyOnClose
      >
        <Form form={form} layout="vertical">
          <Form.Item
            name="name"
            label="套件名称"
            rules={[{ required: true, message: '请输入套件名称' }]}
          >
            <Input placeholder="请输入套件名称" />
          </Form.Item>

          <Form.Item name="description" label="描述">
            <Input.TextArea rows={3} placeholder="请输入套件描述" />
          </Form.Item>

          <Form.Item
            name="environment"
            label="执行环境"
            rules={[{ required: true, message: '请选择执行环境' }]}
          >
            <Select
              placeholder="请选择执行环境"
              options={[
                { value: 'dev', label: '开发环境' },
                { value: 'test', label: '测试环境' },
                { value: 'staging', label: '预发布环境' },
                { value: 'production', label: '生产环境' },
              ]}
            />
          </Form.Item>

          <Form.Item name="status" label="状态">
            <Select
              placeholder="请选择状态"
              options={[
                { value: 'active', label: '活跃' },
                { value: 'inactive', label: '未激活' },
                { value: 'archived', label: '已归档' },
              ]}
            />
          </Form.Item>

          <Form.Item label="测试用例">
            <Transfer
              dataSource={transferDataSource}
              titles={['可选用例', '已选用例']}
              targetKeys={selectedCaseIds}
              onChange={(newTargetKeys) => setSelectedCaseIds(newTargetKeys as string[])}
              render={(item) => item.title as string}
              listStyle={{
                width: 280,
                height: 300,
              }}
              showSearch
              filterOption={(input, option) =>
                ((option.title as string) || '').toLowerCase().includes(input.toLowerCase())
              }
            />
          </Form.Item>
        </Form>
      </Modal>

      {/* 查看详情抽屉 */}
      <Drawer
        title={`测试套件详情 - ${currentSuite?.name || ''}`}
        placement="right"
        width={600}
        onClose={() => setDrawerVisible(false)}
        open={drawerVisible}
      >
        {currentSuite && (
          <Space direction="vertical" style={{ width: '100%' }} size="large">
            <Descriptions column={1} bordered size="small">
              <Descriptions.Item label="套件名称">{currentSuite.name}</Descriptions.Item>
              <Descriptions.Item label="描述">{currentSuite.description}</Descriptions.Item>
              <Descriptions.Item label="执行环境">
                {currentSuite.environment}
              </Descriptions.Item>
              <Descriptions.Item label="状态">
                <Tag color={statusColorMap[currentSuite.status]}>
                  {statusLabelMap[currentSuite.status]}
                </Tag>
              </Descriptions.Item>
              <Descriptions.Item label="创建时间">
                {dayjs(currentSuite.createdAt).format('YYYY-MM-DD HH:mm:ss')}
              </Descriptions.Item>
              <Descriptions.Item label="更新时间">
                {dayjs(currentSuite.updatedAt).format('YYYY-MM-DD HH:mm:ss')}
              </Descriptions.Item>
            </Descriptions>

            <Card title="包含的测试用例" size="small">
              {currentSuite.testCases && currentSuite.testCases.length > 0 ? (
                <Table
                  dataSource={allTestCases.filter((c) =>
                    currentSuite.testCases?.includes(c.id)
                  )}
                  rowKey="id"
                  size="small"
                  pagination={false}
                  columns={[
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
                    },
                    {
                      title: '优先级',
                      dataIndex: 'priority',
                      key: 'priority',
                      width: 80,
                      render: (priority: string) => {
                        const colorMap: Record<string, string> = {
                          P0: 'red',
                          P1: 'orange',
                          P2: 'blue',
                          P3: 'default',
                        };
                        return <Tag color={colorMap[priority]}>{priority}</Tag>;
                      },
                    },
                    {
                      title: '状态',
                      dataIndex: 'status',
                      key: 'status',
                      width: 80,
                    },
                  ]}
                />
              ) : (
                <div style={{ textAlign: 'center', padding: 20, color: '#999' }}>
                  暂无测试用例
                </div>
              )}
            </Card>
          </Space>
        )}
      </Drawer>
    </div>
  );
};

export default TestSuiteManagementPage;