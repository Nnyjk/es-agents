/**
 * 已安装插件管理页面
 * 支持插件启停、配置、卸载、日志查看
 */
import React, { useState, useEffect } from 'react';
import {
  Card,
  Table,
  Button,
  Tag,
  Space,
  Form,
  Input,
  Select,
  Switch,
  message,
  Popconfirm,
  Tooltip,
  Drawer,
  Descriptions,
  Typography,
  Tabs,
  Statistic,
  Row,
  Col,
  Spin,
} from 'antd';
import {
  PlayCircleOutlined,
  StopOutlined,
  SettingOutlined,
  DeleteOutlined,
  ReloadOutlined,
  SyncOutlined,
  InfoCircleOutlined,
} from '@ant-design/icons';
import {
  getInstalledPlugins,
  uninstallPlugin,
  enablePlugin,
  disablePlugin,
  updatePlugin,
  getPluginConfig,
  updatePluginConfig,
  getPluginLogs,
  getPluginMetrics,
  restartPlugin,
  testPluginConfig,
} from '../../services/pluginMarketplace';
import type {
  InstalledPlugin,
  InstalledPluginQueryParams,
  PluginStatus,
  PluginConfig,
  PluginMetrics,
} from '../../types/pluginMarketplace';

const { Text, Paragraph } = Typography;
const { TabPane } = Tabs;

// 状态颜色映射
const statusColorMap: Record<PluginStatus, string> = {
  active: 'green',
  inactive: 'default',
  error: 'red',
  installing: 'blue',
  updating: 'orange',
};

// 状态名称映射
const statusNameMap: Record<PluginStatus, string> = {
  active: '运行中',
  inactive: '已停止',
  error: '错误',
  installing: '安装中',
  updating: '更新中',
};

const InstalledPluginsPage: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [plugins, setPlugins] = useState<InstalledPlugin[]>([]);
  const [total, setTotal] = useState(0);
  const [current, setCurrent] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [statusFilter, setStatusFilter] = useState<PluginStatus | undefined>();
  const [keyword, setKeyword] = useState('');
  const [configDrawerVisible, setConfigDrawerVisible] = useState(false);
  const [selectedPlugin, setSelectedPlugin] = useState<InstalledPlugin | null>(null);
  const [pluginConfig, setPluginConfig] = useState<PluginConfig | null>(null);
  const [configForm] = Form.useForm();
  const [detailDrawerVisible, setDetailDrawerVisible] = useState(false);
  const [pluginDetail, setPluginDetail] = useState<InstalledPlugin | null>(null);
  const [pluginMetrics, setPluginMetrics] = useState<PluginMetrics | null>(null);
  const [pluginLogs, setPluginLogs] = useState<string>('');
  const [metricsLoading, setMetricsLoading] = useState(false);
  const [logsLoading, setLogsLoading] = useState(false);

  // 加载已安装插件列表
  const loadPlugins = async () => {
    setLoading(true);
    try {
      const params: InstalledPluginQueryParams = {
        page: current,
        pageSize,
        keyword: keyword || undefined,
        status: statusFilter,
      };
      const result = await getInstalledPlugins(params);
      setPlugins(result.items);
      setTotal(result.total);
    } catch (error) {
      message.error('加载插件列表失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadPlugins();
  }, [current, pageSize, keyword, statusFilter]);

  // 启用插件
  const handleEnable = async (plugin: InstalledPlugin) => {
    try {
      await enablePlugin(plugin.id);
      message.success('插件已启用');
      loadPlugins();
    } catch (error) {
      message.error('启用失败');
    }
  };

  // 禁用插件
  const handleDisable = async (plugin: InstalledPlugin) => {
    try {
      await disablePlugin(plugin.id);
      message.success('插件已禁用');
      loadPlugins();
    } catch (error) {
      message.error('禁用失败');
    }
  };

  // 重启插件
  const handleRestart = async (plugin: InstalledPlugin) => {
    try {
      await restartPlugin(plugin.id);
      message.success('插件重启中');
      loadPlugins();
    } catch (error) {
      message.error('重启失败');
    }
  };

  // 卸载插件
  const handleUninstall = async (plugin: InstalledPlugin) => {
    try {
      await uninstallPlugin(plugin.id);
      message.success('插件已卸载');
      loadPlugins();
    } catch (error) {
      message.error('卸载失败');
    }
  };

  // 更新插件
  const handleUpdate = async (plugin: InstalledPlugin) => {
    try {
      await updatePlugin(plugin.id, {});
      message.success('插件更新中');
      loadPlugins();
    } catch (error) {
      message.error('更新失败');
    }
  };

  // 打开配置
  const handleOpenConfig = async (plugin: InstalledPlugin) => {
    setLoading(true);
    try {
      const config = await getPluginConfig(plugin.id);
      setSelectedPlugin(plugin);
      setPluginConfig(config);
      configForm.setFieldsValue(config.values);
      setConfigDrawerVisible(true);
    } catch (error) {
      message.error('获取配置失败');
    } finally {
      setLoading(false);
    }
  };

  // 保存配置
  const handleSaveConfig = async () => {
    try {
      const values = await configForm.validateFields();
      if (!selectedPlugin) return;
      
      await updatePluginConfig(selectedPlugin.id, values);
      message.success('配置已保存');
      setConfigDrawerVisible(false);
    } catch (error) {
      message.error('保存配置失败');
    }
  };

  // 测试配置
  const handleTestConfig = async () => {
    try {
      const values = await configForm.validateFields();
      if (!selectedPlugin) return;
      
      const result = await testPluginConfig(selectedPlugin.id, values);
      if (result.success) {
        message.success('配置测试通过');
      } else {
        message.warning(result.message || '配置测试失败');
      }
    } catch (error) {
      message.error('测试失败');
    }
  };

  // 查看详情
  const handleViewDetail = async (plugin: InstalledPlugin) => {
    setDetailDrawerVisible(true);
    setPluginDetail(plugin);
    setPluginMetrics(null);
    setPluginLogs('');
    
    // 加载指标
    setMetricsLoading(true);
    try {
      const metrics = await getPluginMetrics(plugin.id);
      setPluginMetrics(metrics);
    } catch (error) {
      console.error('获取指标失败', error);
    } finally {
      setMetricsLoading(false);
    }
  };

  // 加载日志
  const loadLogs = async (pluginId: string) => {
    setLogsLoading(true);
    try {
      const result = await getPluginLogs(pluginId, { lines: 100 });
      setPluginLogs(result.logs);
    } catch (error) {
      message.error('获取日志失败');
    } finally {
      setLogsLoading(false);
    }
  };

  // 表格列定义
  const columns = [
    {
      title: '插件名称',
      dataIndex: 'displayName',
      key: 'displayName',
      render: (text: string, record: InstalledPlugin) => (
        <Space>
          {record.logo ? (
            <img src={record.logo} alt={text} style={{ width: 32, height: 32 }} />
          ) : (
            <div style={{ 
              width: 32, 
              height: 32, 
              background: '#f0f0f0', 
              display: 'flex', 
              alignItems: 'center', 
              justifyContent: 'center' 
            }}>
              <InfoCircleOutlined />
            </div>
          )}
          <span>{text}</span>
        </Space>
      ),
    },
    {
      title: '版本',
      dataIndex: 'version',
      key: 'version',
      width: 100,
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: (status: PluginStatus) => (
        <Tag color={statusColorMap[status]}>
          {statusNameMap[status]}
        </Tag>
      ),
    },
    {
      title: 'Agent',
      dataIndex: 'agentId',
      key: 'agentId',
      width: 120,
      ellipsis: true,
    },
    {
      title: '安装时间',
      dataIndex: 'installedAt',
      key: 'installedAt',
      width: 180,
      render: (date: string) => new Date(date).toLocaleString(),
    },
    {
      title: '操作',
      key: 'actions',
      width: 240,
      render: (_: unknown, record: InstalledPlugin) => (
        <Space size="small">
          {record.status === 'active' ? (
            <Tooltip title="停止">
              <Button
                type="text"
                icon={<StopOutlined />}
                onClick={() => handleDisable(record)}
              />
            </Tooltip>
          ) : record.status === 'inactive' ? (
            <Tooltip title="启动">
              <Button
                type="text"
                icon={<PlayCircleOutlined />}
                onClick={() => handleEnable(record)}
              />
            </Tooltip>
          ) : null}
          <Tooltip title="重启">
            <Button
              type="text"
              icon={<ReloadOutlined />}
              onClick={() => handleRestart(record)}
            />
          </Tooltip>
          <Tooltip title="配置">
            <Button
              type="text"
              icon={<SettingOutlined />}
              onClick={() => handleOpenConfig(record)}
            />
          </Tooltip>
          <Tooltip title="详情">
            <Button
              type="text"
              icon={<InfoCircleOutlined />}
              onClick={() => handleViewDetail(record)}
            />
          </Tooltip>
          {record.hasUpdate && (
            <Tooltip title="有更新">
              <Button
                type="text"
                icon={<SyncOutlined />}
                onClick={() => handleUpdate(record)}
              />
            </Tooltip>
          )}
          <Popconfirm
            title="确定要卸载此插件吗？"
            onConfirm={() => handleUninstall(record)}
          >
            <Tooltip title="卸载">
              <Button
                type="text"
                danger
                icon={<DeleteOutlined />}
              />
            </Tooltip>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <div className="installed-plugins-page">
      {/* 搜索栏 */}
      <Card style={{ marginBottom: 16 }}>
        <Space>
          <Input.Search
            placeholder="搜索插件名称"
            allowClear
            style={{ width: 200 }}
            onSearch={setKeyword}
          />
          <Select
            placeholder="状态"
            allowClear
            style={{ width: 120 }}
            value={statusFilter}
            onChange={setStatusFilter}
          >
            {Object.entries(statusNameMap).map(([key, name]) => (
              <Select.Option key={key} value={key}>
                {name}
              </Select.Option>
            ))}
          </Select>
          <Button icon={<ReloadOutlined />} onClick={loadPlugins}>
            刷新
          </Button>
        </Space>
      </Card>

      {/* 插件列表 */}
      <Card>
        <Table
          loading={loading}
          columns={columns}
          dataSource={plugins}
          rowKey="id"
          pagination={{
            current,
            pageSize,
            total,
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (t) => `共 ${t} 个插件`,
            onChange: (page, size) => {
              setCurrent(page);
              setPageSize(size);
            },
          }}
        />
      </Card>

      {/* 配置抽屉 */}
      <Drawer
        title={`配置 - ${selectedPlugin?.displayName}`}
        placement="right"
        width={600}
        open={configDrawerVisible}
        onClose={() => setConfigDrawerVisible(false)}
        footer={
          <Space>
            <Button onClick={() => setConfigDrawerVisible(false)}>取消</Button>
            <Button onClick={handleTestConfig}>测试</Button>
            <Button type="primary" onClick={handleSaveConfig}>保存</Button>
          </Space>
        }
      >
        {pluginConfig && (
          <>
            <Paragraph>
              <Text strong>配置说明：</Text>
              {pluginConfig.description}
            </Paragraph>
            <Form form={configForm} layout="vertical">
              {pluginConfig.fields.map((field) => (
                <Form.Item
                  key={field.name}
                  name={field.name}
                  label={field.displayName}
                  rules={[
                    { required: field.required, message: `请输入${field.displayName}` },
                  ]}
                  tooltip={field.description}
                >
                  {field.type === 'string' && field.options ? (
                    <Select>
                      {field.options.map((opt) => (
                        <Select.Option key={opt.value} value={opt.value}>
                          {opt.label}
                        </Select.Option>
                      ))}
                    </Select>
                  ) : field.type === 'string' ? (
                    <Input.TextArea rows={3} />
                  ) : field.type === 'number' ? (
                    <Input type="number" />
                  ) : field.type === 'boolean' ? (
                    <Switch />
                  ) : field.type === 'password' ? (
                    <Input.Password />
                  ) : (
                    <Input />
                  )}
                </Form.Item>
              ))}
            </Form>
          </>
        )}
      </Drawer>

      {/* 详情抽屉 */}
      <Drawer
        title={`详情 - ${pluginDetail?.displayName}`}
        placement="right"
        width={800}
        open={detailDrawerVisible}
        onClose={() => setDetailDrawerVisible(false)}
      >
        {pluginDetail && (
          <Tabs defaultActiveKey="info">
            <TabPane tab="基本信息" key="info">
              <Descriptions bordered column={2}>
                <Descriptions.Item label="插件名称">{pluginDetail.displayName}</Descriptions.Item>
                <Descriptions.Item label="版本">{pluginDetail.version}</Descriptions.Item>
                <Descriptions.Item label="状态">
                  <Tag color={statusColorMap[pluginDetail.status]}>
                    {statusNameMap[pluginDetail.status]}
                  </Tag>
                </Descriptions.Item>
                <Descriptions.Item label="Agent">{pluginDetail.agentId}</Descriptions.Item>
                <Descriptions.Item label="安装时间">
                  {new Date(pluginDetail.installedAt).toLocaleString()}
                </Descriptions.Item>
                <Descriptions.Item label="最新版本">
                  {pluginDetail.latestVersion || pluginDetail.version}
                </Descriptions.Item>
                {pluginDetail.hasUpdate && (
                  <Descriptions.Item label="更新">
                    <Tag color="orange">有新版本可用</Tag>
                  </Descriptions.Item>
                )}
              </Descriptions>
            </TabPane>
            <TabPane tab="运行指标" key="metrics">
              <Spin spinning={metricsLoading}>
                {pluginMetrics ? (
                  <Row gutter={16}>
                    <Col span={6}>
                      <Statistic
                        title="运行时间"
                        value={pluginMetrics.uptime}
                        suffix="秒"
                      />
                    </Col>
                    <Col span={6}>
                      <Statistic
                        title="CPU 使用率"
                        value={pluginMetrics.cpuUsage}
                        suffix="%"
                      />
                    </Col>
                    <Col span={6}>
                      <Statistic
                        title="内存使用"
                        value={pluginMetrics.memoryUsage}
                        suffix="MB"
                      />
                    </Col>
                    <Col span={6}>
                      <Statistic
                        title="错误数"
                        value={pluginMetrics.errorCount}
                        valueStyle={{ color: pluginMetrics.errorCount > 0 ? '#ff4d4f' : undefined }}
                      />
                    </Col>
                  </Row>
                ) : (
                  <Text>暂无指标数据</Text>
                )}
              </Spin>
            </TabPane>
            <TabPane tab="日志" key="logs">
              <Button 
                style={{ marginBottom: 16 }} 
                onClick={() => loadLogs(pluginDetail.id)}
                loading={logsLoading}
              >
                刷新日志
              </Button>
              <Input.TextArea
                value={pluginLogs}
                rows={15}
                readOnly
                style={{ fontFamily: 'monospace' }}
              />
            </TabPane>
          </Tabs>
        )}
      </Drawer>
    </div>
  );
};

export default InstalledPluginsPage;