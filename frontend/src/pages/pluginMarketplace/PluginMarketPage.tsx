/**
 * 插件市场页面
 * 支持插件浏览、搜索、安装
 */
import React, { useState, useEffect } from "react";
import {
  Card,
  Row,
  Col,
  Input,
  Select,
  Tag,
  Button,
  Modal,
  Rate,
  Empty,
  Spin,
  message,
  Tooltip,
  Progress,
  Typography,
  Pagination,
  Statistic,
  Space,
} from "antd";
import {
  DownloadOutlined,
  StarOutlined,
  AppstoreOutlined,
  MonitorOutlined,
  CloudUploadOutlined,
  SecurityScanOutlined,
  BarChartOutlined,
  NotificationOutlined,
  ApiOutlined,
  CheckCircleOutlined,
  SyncOutlined,
  InfoCircleOutlined,
} from "@ant-design/icons";
import {
  getPluginMarketList,
  installPlugin,
  getRecommendedPlugins,
} from "../../services/pluginMarketplace";
import type {
  Plugin,
  PluginQueryParams,
  PluginCategory,
  InstallProgress,
} from "../../types/pluginMarketplace";

const { Search } = Input;
const { Text, Paragraph } = Typography;

// 分类图标映射
const categoryIconMap: Record<PluginCategory, React.ReactNode> = {
  monitoring: <MonitorOutlined />,
  operations: <AppstoreOutlined />,
  deployment: <CloudUploadOutlined />,
  security: <SecurityScanOutlined />,
  "data-analysis": <BarChartOutlined />,
  notification: <NotificationOutlined />,
  integration: <ApiOutlined />,
  other: <AppstoreOutlined />,
};

// 分类名称映射
const categoryNameMap: Record<PluginCategory, string> = {
  monitoring: "监控类",
  operations: "运维类",
  deployment: "部署类",
  security: "安全类",
  "data-analysis": "数据分析类",
  notification: "通知类",
  integration: "集成类",
  other: "其他",
};

const PluginMarketPage: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [plugins, setPlugins] = useState<Plugin[]>([]);
  const [total, setTotal] = useState(0);
  const [current, setCurrent] = useState(1);
  const [pageSize, setPageSize] = useState(12);
  const [keyword, setKeyword] = useState("");
  const [category, setCategory] = useState<PluginCategory | undefined>();
  const [sortBy, setSortBy] = useState<string>("downloadCount");
  const [recommended, setRecommended] = useState<Plugin[]>([]);
  const [detailVisible, setDetailVisible] = useState(false);
  const [selectedPlugin, setSelectedPlugin] = useState<Plugin | null>(null);
  const [installModalVisible, setInstallModalVisible] = useState(false);
  const [installingPlugin, setInstallingPlugin] = useState<Plugin | null>(null);
  const [installProgress, setInstallProgress] =
    useState<InstallProgress | null>(null);
  const [selectedAgentId, setSelectedAgentId] = useState<string>("");

  // 加载插件列表
  const loadPlugins = async () => {
    setLoading(true);
    try {
      const params: PluginQueryParams = {
        page: current,
        pageSize,
        keyword: keyword || undefined,
        category,
        sortBy: sortBy as "name" | "downloadCount" | "rating" | "updatedAt",
        sortOrder: "desc",
      };
      const result = await getPluginMarketList(params);
      setPlugins(result.items);
      setTotal(result.total);
    } catch (error) {
      message.error("加载插件列表失败");
    } finally {
      setLoading(false);
    }
  };

  // 加载推荐插件
  const loadRecommended = async () => {
    try {
      const result = await getRecommendedPlugins();
      setRecommended(result);
    } catch (error) {
      console.error("加载推荐插件失败", error);
    }
  };

  useEffect(() => {
    loadPlugins();
  }, [current, pageSize, keyword, category, sortBy]);

  useEffect(() => {
    loadRecommended();
  }, []);

  // 搜索
  const handleSearch = (value: string) => {
    setKeyword(value);
    setCurrent(1);
  };

  // 分类变更
  const handleCategoryChange = (value: PluginCategory | undefined) => {
    setCategory(value);
    setCurrent(1);
  };

  // 排序变更
  const handleSortChange = (value: string) => {
    setSortBy(value);
    setCurrent(1);
  };

  // 查看详情
  const handleViewDetail = (plugin: Plugin) => {
    setSelectedPlugin(plugin);
    setDetailVisible(true);
  };

  // 安装插件
  const handleInstall = (plugin: Plugin) => {
    setInstallingPlugin(plugin);
    setInstallModalVisible(true);
    setInstallProgress(null);
  };

  // 确认安装
  const handleConfirmInstall = async () => {
    if (!installingPlugin || !selectedAgentId) {
      message.warning("请选择要安装的 Agent");
      return;
    }

    try {
      await installPlugin({
        pluginId: installingPlugin.id,
        agentId: selectedAgentId,
      });
      message.success("开始安装插件");
      setInstallModalVisible(false);
      loadPlugins();
    } catch (error) {
      message.error("安装失败");
    }
  };

  // 渲染插件卡片
  const renderPluginCard = (plugin: Plugin) => {
    const actions = [];

    if (plugin.installed) {
      actions.push(
        <Tooltip key="installed" title="已安装">
          <CheckCircleOutlined style={{ color: "#52c41a" }} />
        </Tooltip>,
      );
    }

    if (plugin.hasUpdate) {
      actions.push(
        <Tooltip key="update" title="有更新">
          <SyncOutlined style={{ color: "#1890ff" }} />
        </Tooltip>,
      );
    }

    actions.push(
      <Tooltip key="download" title="安装">
        <DownloadOutlined onClick={() => handleInstall(plugin)} />
      </Tooltip>,
      <Tooltip key="info" title="详情">
        <InfoCircleOutlined onClick={() => handleViewDetail(plugin)} />
      </Tooltip>,
    );

    return (
      <Col key={plugin.id} xs={24} sm={12} md={8} lg={6}>
        <Card
          hoverable
          actions={actions}
          cover={
            <div
              style={{
                height: 120,
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
                background: "linear-gradient(135deg, #667eea 0%, #764ba2 100%)",
              }}
            >
              {plugin.logo ? (
                <img
                  src={plugin.logo}
                  alt={plugin.name}
                  style={{ maxHeight: 80 }}
                />
              ) : (
                <AppstoreOutlined style={{ fontSize: 48, color: "#fff" }} />
              )}
            </div>
          }
        >
          <Card.Meta
            title={
              <Space>
                <Text strong>{plugin.displayName}</Text>
                <Tag>v{plugin.version}</Tag>
              </Space>
            }
            description={
              <Paragraph ellipsis={{ rows: 2 }} style={{ marginBottom: 8 }}>
                {plugin.description}
              </Paragraph>
            }
          />
          <Space wrap style={{ marginTop: 8 }}>
            <Tag icon={categoryIconMap[plugin.category]}>
              {categoryNameMap[plugin.category]}
            </Tag>
          </Space>
          <Row gutter={16} style={{ marginTop: 12 }}>
            <Col span={12}>
              <Statistic
                value={plugin.downloadCount}
                prefix={<DownloadOutlined />}
                valueStyle={{ fontSize: 14 }}
              />
            </Col>
            <Col span={12}>
              <Statistic
                value={plugin.rating}
                prefix={<StarOutlined />}
                valueStyle={{ fontSize: 14 }}
              />
            </Col>
          </Row>
        </Card>
      </Col>
    );
  };

  return (
    <div className="plugin-market-page">
      {/* 推荐插件 */}
      {recommended.length > 0 && (
        <Card
          title="推荐插件"
          style={{ marginBottom: 16 }}
          extra={<a onClick={() => setRecommended([])}>收起</a>}
        >
          <Row gutter={[16, 16]}>{recommended.map(renderPluginCard)}</Row>
        </Card>
      )}

      {/* 搜索栏 */}
      <Card style={{ marginBottom: 16 }}>
        <Row gutter={16} align="middle">
          <Col flex="auto">
            <Search
              placeholder="搜索插件名称、描述、作者..."
              allowClear
              onSearch={handleSearch}
              style={{ maxWidth: 400 }}
            />
          </Col>
          <Col>
            <Space>
              <Select
                placeholder="分类"
                allowClear
                style={{ width: 150 }}
                value={category}
                onChange={handleCategoryChange}
              >
                {Object.entries(categoryNameMap).map(([key, name]) => (
                  <Select.Option key={key} value={key}>
                    {name}
                  </Select.Option>
                ))}
              </Select>
              <Select
                placeholder="排序"
                style={{ width: 150 }}
                value={sortBy}
                onChange={handleSortChange}
              >
                <Select.Option value="downloadCount">下载量</Select.Option>
                <Select.Option value="rating">评分</Select.Option>
                <Select.Option value="updatedAt">更新时间</Select.Option>
                <Select.Option value="name">名称</Select.Option>
              </Select>
              <Button icon={<SyncOutlined />} onClick={() => loadPlugins()}>
                刷新
              </Button>
            </Space>
          </Col>
        </Row>
      </Card>

      {/* 插件列表 */}
      <Card>
        <Spin spinning={loading}>
          {plugins.length > 0 ? (
            <>
              <Row gutter={[16, 16]}>{plugins.map(renderPluginCard)}</Row>
              <div style={{ textAlign: "center", marginTop: 24 }}>
                <Pagination
                  current={current}
                  pageSize={pageSize}
                  total={total}
                  showSizeChanger
                  showQuickJumper
                  showTotal={(t) => `共 ${t} 个插件`}
                  onChange={(page, size) => {
                    setCurrent(page);
                    setPageSize(size);
                  }}
                />
              </div>
            </>
          ) : (
            <Empty description="暂无插件" />
          )}
        </Spin>
      </Card>

      {/* 安装弹窗 */}
      <Modal
        open={installModalVisible}
        title={`安装插件 - ${installingPlugin?.displayName}`}
        onCancel={() => setInstallModalVisible(false)}
        onOk={handleConfirmInstall}
      >
        {installProgress ? (
          <div>
            <Progress percent={installProgress.progress} />
            <Text>{installProgress.message}</Text>
          </div>
        ) : (
          <div>
            <Paragraph>请选择要安装插件的 Agent：</Paragraph>
            <Select
              style={{ width: "100%" }}
              placeholder="选择 Agent"
              value={selectedAgentId}
              onChange={setSelectedAgentId}
            >
              {/* TODO: 从 API 加载 Agent 列表 */}
              <Select.Option value="agent-1">Agent 1</Select.Option>
              <Select.Option value="agent-2">Agent 2</Select.Option>
            </Select>
          </div>
        )}
      </Modal>

      {/* 详情弹窗 */}
      <Modal
        open={detailVisible}
        title={selectedPlugin?.displayName}
        onCancel={() => setDetailVisible(false)}
        footer={[
          <Button key="close" onClick={() => setDetailVisible(false)}>
            关闭
          </Button>,
          selectedPlugin && !selectedPlugin.installed && (
            <Button
              key="install"
              type="primary"
              icon={<DownloadOutlined />}
              onClick={() => {
                setDetailVisible(false);
                handleInstall(selectedPlugin);
              }}
            >
              安装
            </Button>
          ),
        ]}
        width={800}
      >
        {selectedPlugin && (
          <div>
            <Paragraph>
              <Text strong>描述：</Text>
              {selectedPlugin.description}
            </Paragraph>
            <Paragraph>
              <Text strong>版本：</Text>
              {selectedPlugin.version}
            </Paragraph>
            <Paragraph>
              <Text strong>作者：</Text>
              {selectedPlugin.author}
            </Paragraph>
            <Paragraph>
              <Text strong>分类：</Text>
              <Tag>{categoryNameMap[selectedPlugin.category]}</Tag>
            </Paragraph>
            <Paragraph>
              <Text strong>下载量：</Text>
              {selectedPlugin.downloadCount}
            </Paragraph>
            <Paragraph>
              <Text strong>评分：</Text>
              <Rate disabled value={selectedPlugin.rating / 20} />
            </Paragraph>
            {selectedPlugin.documentation && (
              <Paragraph>
                <Text strong>文档：</Text>
                <a
                  href={selectedPlugin.documentation}
                  target="_blank"
                  rel="noopener noreferrer"
                >
                  查看文档
                </a>
              </Paragraph>
            )}
          </div>
        )}
      </Modal>
    </div>
  );
};

export default PluginMarketPage;
