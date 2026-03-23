/**
 * 测试配置管理页面
 * 管理测试环境配置、全局配置、通知配置等
 */
import React, { useState, useEffect } from 'react';
import {
  Card,
  Tabs,
  Form,
  Input,
  Button,
  Select,
  Switch,
  Space,
  Divider,
  message,
  Table,
  Modal,
  Popconfirm,
  Tag,
  InputNumber,
  Row,
  Col,
  Alert,
  Tooltip,
} from 'antd';
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  SaveOutlined,
  ExperimentOutlined,
  EnvironmentOutlined,
  SettingOutlined,
  BellOutlined,
  CloudOutlined,
} from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import {
  getTestEnvironments,
  createTestEnvironment,
  updateTestEnvironment,
  deleteTestEnvironment,
  testConnection,
  getGlobalConfig,
  updateGlobalConfig,
  getNotificationConfig,
  updateNotificationConfig,
} from '../../services/testQuality';
import type { TestEnvironment, GlobalConfig, NotificationConfig } from '../../types/testQuality';

const { TextArea } = Input;

const TestConfigPage: React.FC = () => {
  // 环境配置
  const [environments, setEnvironments] = useState<TestEnvironment[]>([]);
  const [envLoading, setEnvLoading] = useState(false);
  const [envModalVisible, setEnvModalVisible] = useState(false);
  const [currentEnv, setCurrentEnv] = useState<TestEnvironment | null>(null);
  const [envForm] = Form.useForm();

  // 全局配置
  const [globalConfig, setGlobalConfig] = useState<GlobalConfig | null>(null);
  const [globalForm] = Form.useForm();

  // 通知配置
  const [notifyConfig, setNotifyConfig] = useState<NotificationConfig | null>(null);
  const [notifyForm] = Form.useForm();

  // 加载环境列表
  const loadEnvironments = async () => {
    setEnvLoading(true);
    try {
      const result = await getTestEnvironments();
      setEnvironments(result);
    } catch (error) {
      message.error('加载环境配置失败');
    } finally {
      setEnvLoading(false);
    }
  };

  // 加载全局配置
  const loadGlobalConfig = async () => {
    try {
      const config = await getGlobalConfig();
      setGlobalConfig(config);
      globalForm.setFieldsValue(config);
    } catch (error) {
      message.error('加载全局配置失败');
    }
  };

  // 加载通知配置
  const loadNotifyConfig = async () => {
    try {
      const config = await getNotificationConfig();
      setNotifyConfig(config);
      notifyForm.setFieldsValue(config);
    } catch (error) {
      message.error('加载通知配置失败');
    }
  };

  useEffect(() => {
    loadEnvironments();
    loadGlobalConfig();
    loadNotifyConfig();
  }, []);

  // 新建环境
  const handleCreateEnv = () => {
    setCurrentEnv(null);
    envForm.resetFields();
    envForm.setFieldsValue({
      type: 'http',
      status: 'active',
    });
    setEnvModalVisible(true);
  };

  // 编辑环境
  const handleEditEnv = (record: TestEnvironment) => {
    setCurrentEnv(record);
    envForm.setFieldsValue(record);
    setEnvModalVisible(true);
  };

  // 保存环境
  const handleSaveEnv = async () => {
    try {
      const values = await envForm.validateFields();
      if (currentEnv) {
        await updateTestEnvironment(currentEnv.id, values);
        message.success('更新成功');
      } else {
        await createTestEnvironment(values);
        message.success('创建成功');
      }
      setEnvModalVisible(false);
      loadEnvironments();
    } catch (error) {
      message.error('保存失败');
    }
  };

  // 删除环境
  const handleDeleteEnv = async (id: string) => {
    try {
      await deleteTestEnvironment(id);
      message.success('删除成功');
      loadEnvironments();
    } catch (error) {
      message.error('删除失败');
    }
  };

  // 测试连接
  const handleTestConnection = async (record: TestEnvironment) => {
    try {
      const result = await testConnection(record.id);
      if (result.success) {
        message.success('连接测试成功');
      } else {
        message.error(`连接测试失败: ${result.message}`);
      }
    } catch (error) {
      message.error('连接测试失败');
    }
  };

  // 保存全局配置
  const handleSaveGlobal = async () => {
    try {
      const values = await globalForm.validateFields();
      await updateGlobalConfig(values);
      message.success('保存成功');
    } catch (error) {
      message.error('保存失败');
    }
  };

  // 保存通知配置
  const handleSaveNotify = async () => {
    try {
      const values = await notifyForm.validateFields();
      await updateNotificationConfig(values);
      message.success('保存成功');
    } catch (error) {
      message.error('保存失败');
    }
  };

  // 环境类型标签
  const envTypeLabelMap: Record<string, string> = {
    http: 'HTTP服务',
    database: '数据库',
    redis: 'Redis',
    mq: '消息队列',
    sftp: 'SFTP',
  };

  // 环境状态标签
  const envStatusColorMap: Record<string, string> = {
    active: 'success',
    inactive: 'default',
    error: 'error',
  };

  const envStatusLabelMap: Record<string, string> = {
    active: '正常',
    inactive: '未激活',
    error: '异常',
  };

  // 环境表格列
  const envColumns: ColumnsType<TestEnvironment> = [
    {
      title: '环境名称',
      dataIndex: 'name',
      key: 'name',
      width: 150,
    },
    {
      title: '类型',
      dataIndex: 'type',
      key: 'type',
      width: 100,
      render: (type: string) => <Tag>{envTypeLabelMap[type] || type}</Tag>,
    },
    {
      title: '地址',
      dataIndex: 'host',
      key: 'host',
      width: 200,
      ellipsis: true,
      render: (host: string, record) => `${host}:${record.port}`,
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 80,
      render: (status: string) => (
        <Tag color={envStatusColorMap[status]}>{envStatusLabelMap[status]}</Tag>
      ),
    },
    {
      title: '描述',
      dataIndex: 'description',
      key: 'description',
      ellipsis: true,
    },
    {
      title: '操作',
      key: 'action',
      width: 180,
      render: (_: unknown, record: TestEnvironment) => (
        <Space size="small">
          <Tooltip title="测试连接">
            <Button
              type="text"
              size="small"
              icon={<ExperimentOutlined />}
              onClick={() => handleTestConnection(record)}
            />
          </Tooltip>
          <Tooltip title="编辑">
            <Button
              type="text"
              size="small"
              icon={<EditOutlined />}
              onClick={() => handleEditEnv(record)}
            />
          </Tooltip>
          <Popconfirm
            title="确定删除此环境配置？"
            onConfirm={() => handleDeleteEnv(record.id)}
          >
            <Button type="text" size="small" icon={<DeleteOutlined />} danger />
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <div>
      <Tabs
        defaultActiveKey="environment"
        items={[
          {
            key: 'environment',
            label: (
              <span>
                <EnvironmentOutlined /> 环境配置
              </span>
            ),
            children: (
              <Card bordered={false}>
                <Space direction="vertical" style={{ width: '100%' }} size="middle">
                  <Alert
                    message="配置测试所需的各类环境，包括HTTP服务、数据库、Redis、消息队列等"
                    type="info"
                    showIcon
                  />
                  <Button type="primary" icon={<PlusOutlined />} onClick={handleCreateEnv}>
                    新建环境
                  </Button>
                  <Table
                    columns={envColumns}
                    dataSource={environments}
                    rowKey="id"
                    loading={envLoading}
                    pagination={false}
                  />
                </Space>
              </Card>
            ),
          },
          {
            key: 'global',
            label: (
              <span>
                <SettingOutlined /> 全局配置
              </span>
            ),
            children: (
              <Card bordered={false}>
                <Form form={globalForm} layout="vertical" style={{ maxWidth: 800 }}>
                  <Alert
                    message="配置测试框架的全局参数"
                    type="info"
                    showIcon
                    style={{ marginBottom: 24 }}
                  />
                  <Row gutter={24}>
                    <Col span={12}>
                      <Form.Item name="defaultTimeout" label="默认超时时间(毫秒)">
                        <InputNumber min={1000} max={300000} style={{ width: '100%' }} />
                      </Form.Item>
                    </Col>
                    <Col span={12}>
                      <Form.Item name="retryCount" label="失败重试次数">
                        <InputNumber min={0} max={5} style={{ width: '100%' }} />
                      </Form.Item>
                    </Col>
                  </Row>
                  <Row gutter={24}>
                    <Col span={12}>
                      <Form.Item name="maxConcurrent" label="最大并发数">
                        <InputNumber min={1} max={50} style={{ width: '100%' }} />
                      </Form.Item>
                    </Col>
                    <Col span={12}>
                      <Form.Item name="reportRetentionDays" label="报告保留天数">
                        <InputNumber min={1} max={365} style={{ width: '100%' }} />
                      </Form.Item>
                    </Col>
                  </Row>
                  <Divider />
                  <Form.Item name="baseAssertion" label="基础断言配置">
                    <TextArea rows={4} placeholder="JSON格式的断言配置" />
                  </Form.Item>
                  <Form.Item name="globalVariables" label="全局变量">
                    <TextArea rows={4} placeholder="JSON格式的全局变量配置" />
                  </Form.Item>
                  <Form.Item>
                    <Button type="primary" icon={<SaveOutlined />} onClick={handleSaveGlobal}>
                      保存配置
                    </Button>
                  </Form.Item>
                </Form>
              </Card>
            ),
          },
          {
            key: 'notification',
            label: (
              <span>
                <BellOutlined /> 通知配置
              </span>
            ),
            children: (
              <Card bordered={false}>
                <Form form={notifyForm} layout="vertical" style={{ maxWidth: 800 }}>
                  <Alert
                    message="配置测试执行结果的通知方式"
                    type="info"
                    showIcon
                    style={{ marginBottom: 24 }}
                  />
                  <Form.Item name="enableEmail" label="邮件通知" valuePropName="checked">
                    <Switch checkedChildren="开启" unCheckedChildren="关闭" />
                  </Form.Item>
                  <Form.Item name="emailRecipients" label="收件人列表">
                    <Select
                      mode="tags"
                      placeholder="输入邮箱地址后按回车添加"
                      tokenSeparators={[',']}
                    />
                  </Form.Item>
                  <Divider />
                  <Form.Item name="enableWebhook" label="Webhook通知" valuePropName="checked">
                    <Switch checkedChildren="开启" unCheckedChildren="关闭" />
                  </Form.Item>
                  <Form.Item name="webhookUrl" label="Webhook URL">
                    <Input placeholder="https://your-webhook-url" />
                  </Form.Item>
                  <Form.Item name="webhookTemplate" label="消息模板">
                    <TextArea
                      rows={4}
                      placeholder="自定义消息模板，支持变量: ${taskName}, ${status}, ${passRate}"
                    />
                  </Form.Item>
                  <Divider />
                  <Form.Item name="notifyOnSuccess" label="成功时通知" valuePropName="checked">
                    <Switch />
                  </Form.Item>
                  <Form.Item name="notifyOnFailure" label="失败时通知" valuePropName="checked">
                    <Switch />
                  </Form.Item>
                  <Form.Item name="notifyOnTimeout" label="超时时通知" valuePropName="checked">
                    <Switch />
                  </Form.Item>
                  <Form.Item>
                    <Button type="primary" icon={<SaveOutlined />} onClick={handleSaveNotify}>
                      保存配置
                    </Button>
                  </Form.Item>
                </Form>
              </Card>
            ),
          },
          {
            key: 'integration',
            label: (
              <span>
                <CloudOutlined /> 集成配置
              </span>
            ),
            children: (
              <Card bordered={false}>
                <Alert
                  message="配置与CI/CD、版本控制等系统的集成"
                  type="info"
                  showIcon
                  style={{ marginBottom: 24 }}
                />
                <div style={{ textAlign: 'center', padding: '60px 0', color: '#999' }}>
                  集成配置功能开发中...
                </div>
              </Card>
            ),
          },
        ]}
      />

      {/* 环境配置弹窗 */}
      <Modal
        title={currentEnv ? '编辑环境配置' : '新建环境配置'}
        open={envModalVisible}
        onOk={handleSaveEnv}
        onCancel={() => setEnvModalVisible(false)}
        width={600}
        destroyOnClose
      >
        <Form form={envForm} layout="vertical">
          <Form.Item
            name="name"
            label="环境名称"
            rules={[{ required: true, message: '请输入环境名称' }]}
          >
            <Input placeholder="如：测试环境、预发布环境" />
          </Form.Item>
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                name="type"
                label="类型"
                rules={[{ required: true, message: '请选择类型' }]}
              >
                <Select
                  options={[
                    { value: 'http', label: 'HTTP服务' },
                    { value: 'database', label: '数据库' },
                    { value: 'redis', label: 'Redis' },
                    { value: 'mq', label: '消息队列' },
                    { value: 'sftp', label: 'SFTP' },
                  ]}
                />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item name="status" label="状态">
                <Select
                  options={[
                    { value: 'active', label: '正常' },
                    { value: 'inactive', label: '未激活' },
                  ]}
                />
              </Form.Item>
            </Col>
          </Row>
          <Row gutter={16}>
            <Col span={16}>
              <Form.Item
                name="host"
                label="主机地址"
                rules={[{ required: true, message: '请输入主机地址' }]}
              >
                <Input placeholder="如：192.168.1.100 或 test.example.com" />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item
                name="port"
                label="端口"
                rules={[{ required: true, message: '请输入端口' }]}
              >
                <InputNumber min={1} max={65535} style={{ width: '100%' }} />
              </Form.Item>
            </Col>
          </Row>
          <Form.Item name="username" label="用户名">
            <Input placeholder="认证用户名" />
          </Form.Item>
          <Form.Item name="password" label="密码">
            <Input.Password placeholder="认证密码" />
          </Form.Item>
          <Form.Item name="description" label="描述">
            <TextArea rows={2} placeholder="环境描述信息" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default TestConfigPage;