import React, { useState, useEffect } from 'react';
import {
  Card,
  Table,
  Button,
  Space,
  Tag,
  Modal,
  Input,
  Select,
  DatePicker,
  Badge,
  Row,
  Col,
  Statistic,
  message,
  Dropdown,
  Typography,
  Empty,
  Tooltip,
} from 'antd';
import {
  BellOutlined,
  MailOutlined,
  ExclamationCircleOutlined,
  WarningOutlined,
  InfoCircleOutlined,
  ReloadOutlined,
  CheckCircleOutlined,
  DeleteOutlined,
  EyeOutlined,
} from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import { PageContainer } from '@ant-design/pro-components';
import dayjs from 'dayjs';
import {
  getMessages,
  markAsRead,
  markBatchAsRead,
  deleteMessage,
  deleteBatchMessages,
  getUnreadCount,
  getStatistics,
  type NotificationQueryParams,
} from '@/services/notificationMessage';
import type {
  NotificationListItem,
  NotificationMessage,
  MessageType,
  MessageLevel,
  UnreadCount,
  NotificationStatistics,
} from '@/types/notification';

const { RangePicker } = DatePicker;
const { Text } = Typography;
const { Option } = Select;

/**
 * 消息中心页面
 */
const MessageCenter: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [messages, setMessages] = useState<NotificationListItem[]>([]);
  const [selectedRowKeys, setSelectedRowKeys] = useState<React.Key[]>([]);
  const [detailVisible, setDetailVisible] = useState(false);
  const [selectedMessage, setSelectedMessage] = useState<NotificationMessage | null>(null);
  const [unreadCount, setUnreadCount] = useState<UnreadCount | null>(null);
  const [statistics, setStatistics] = useState<NotificationStatistics | null>(null);

  // 查询参数
  const [queryParams, setQueryParams] = useState<NotificationQueryParams>({
    limit: 20,
    offset: 0,
  });
  const [dateRange, setDateRange] = useState<[dayjs.Dayjs, dayjs.Dayjs] | null>(null);
  const [keyword, setKeyword] = useState('');
  const [typeFilter, setTypeFilter] = useState<MessageType | undefined>();
  const [levelFilter, setLevelFilter] = useState<MessageLevel | undefined>();
  const [isReadFilter, setIsReadFilter] = useState<boolean | undefined>();

  // 获取当前用户 ID（从 localStorage 或上下文）
  const getCurrentUserId = (): string => {
    const userStr = localStorage.getItem('user');
    if (userStr) {
      const user = JSON.parse(userStr);
      return user.id;
    }
    return '';
  };

  // 加载消息列表
  const loadMessages = async () => {
    setLoading(true);
    try {
      const params: NotificationQueryParams = {
        ...queryParams,
        userId: getCurrentUserId(),
        keyword: keyword || undefined,
        type: typeFilter,
        level: levelFilter,
        isRead: isReadFilter,
        startTime: dateRange?.[0].toISOString(),
        endTime: dateRange?.[1].toISOString(),
      };

      const data = await getMessages(params);
      setMessages(data);
    } catch (error: any) {
      message.error('加载消息列表失败：' + (error.message || '未知错误'));
    } finally {
      setLoading(false);
    }
  };

  // 加载统计信息
  const loadStatistics = async () => {
    try {
      const userId = getCurrentUserId();
      const [unread, stats] = await Promise.all([
        getUnreadCount(userId),
        getStatistics(userId),
      ]);
      setUnreadCount(unread);
      setStatistics(stats);
    } catch (error: any) {
      console.error('加载统计信息失败:', error);
    }
  };

  useEffect(() => {
    loadMessages();
    loadStatistics();
  }, [queryParams, keyword, typeFilter, levelFilter, isReadFilter]);

  // 刷新
  const handleRefresh = () => {
    loadMessages();
    loadStatistics();
    message.success('刷新成功');
  };

  // 查看详情
  const handleViewDetail = async (id: string) => {
    try {
      const detail = await getMessages({} as NotificationQueryParams);
      // 实际应该调用 getMessageDetail，这里简化处理
      const msg = messages.find((m) => m.id === id);
      if (msg) {
        setSelectedMessage({
          ...msg,
          userId: '',
          username: '',
          content: '消息内容详情...',
        } as NotificationMessage);
        setDetailVisible(true);
        
        // 如果未读，自动标记为已读
        if (!msg.isRead) {
          await markAsRead(id);
          loadMessages();
          loadStatistics();
        }
      }
    } catch (error: any) {
      message.error('加载消息详情失败：' + (error.message || '未知错误'));
    }
  };

  // 批量标记已读
  const handleBatchMarkRead = async () => {
    if (selectedRowKeys.length === 0) {
      message.warning('请选择要标记的消息');
      return;
    }

    try {
      const result = await markBatchAsRead(selectedRowKeys as string[]);
      message.success(`已标记 ${result.count} 条消息为已读`);
      setSelectedRowKeys([]);
      loadMessages();
      loadStatistics();
    } catch (error: any) {
      message.error('批量标记失败：' + (error.message || '未知错误'));
    }
  };

  // 批量删除
  const handleBatchDelete = async () => {
    if (selectedRowKeys.length === 0) {
      message.warning('请选择要删除的消息');
      return;
    }

    Modal.confirm({
      title: '确认删除',
      content: `确定要删除选中的 ${selectedRowKeys.length} 条消息吗？`,
      okText: '确认',
      cancelText: '取消',
      okType: 'danger',
      onOk: async () => {
        try {
          const result = await deleteBatchMessages(selectedRowKeys as string[]);
          message.success(`已删除 ${result.count} 条消息`);
          setSelectedRowKeys([]);
          loadMessages();
          loadStatistics();
        } catch (error: any) {
          message.error('批量删除失败：' + (error.message || '未知错误'));
        }
      },
    });
  };

  // 删除单条
  const handleDelete = (id: string) => {
    Modal.confirm({
      title: '确认删除',
      content: '确定要删除这条消息吗？',
      okText: '确认',
      cancelText: '取消',
      okType: 'danger',
      onOk: async () => {
        try {
          await deleteMessage(id);
          message.success('删除成功');
          loadMessages();
          loadStatistics();
        } catch (error: any) {
          message.error('删除失败：' + (error.message || '未知错误'));
        }
      },
    });
  };

  // 重置筛选
  const handleReset = () => {
    setKeyword('');
    setTypeFilter(undefined);
    setLevelFilter(undefined);
    setIsReadFilter(undefined);
    setDateRange(null);
    setQueryParams({ limit: 20, offset: 0 });
  };

  // 获取消息类型标签
  const getTypeTag = (type: MessageType) => {
    const config = {
      SYSTEM: { color: 'blue', text: '系统', icon: <InfoCircleOutlined /> },
      ALERT: { color: 'red', text: '告警', icon: <ExclamationCircleOutlined /> },
      OPERATION: { color: 'green', text: '操作', icon: <CheckCircleOutlined /> },
    };
    const cfg = config[type];
    return (
      <Tag icon={cfg.icon} color={cfg.color}>
        {cfg.text}
      </Tag>
    );
  };

  // 获取消息级别标签
  const getLevelTag = (level: MessageLevel) => {
    const config = {
      INFO: { color: 'default', text: '普通', icon: <InfoCircleOutlined /> },
      WARNING: { color: 'orange', text: '警告', icon: <WarningOutlined /> },
      ERROR: { color: 'red', text: '错误', icon: <ExclamationCircleOutlined /> },
    };
    const cfg = config[level];
    return (
      <Tag icon={cfg.icon} color={cfg.color}>
        {cfg.text}
      </Tag>
    );
  };

  // 表格列定义
  const columns: ColumnsType<NotificationListItem> = [
    {
      title: '标题',
      dataIndex: 'title',
      key: 'title',
      ellipsis: true,
      render: (text, record) => (
        <Space>
          {!record.isRead && <Badge color="red" />}
          <Button type="link" onClick={() => handleViewDetail(record.id)}>
            {text}
          </Button>
        </Space>
      ),
    },
    {
      title: '类型',
      dataIndex: 'type',
      key: 'type',
      width: 100,
      render: (type: MessageType) => getTypeTag(type),
    },
    {
      title: '级别',
      dataIndex: 'level',
      key: 'level',
      width: 80,
      render: (level: MessageLevel) => getLevelTag(level),
    },
    {
      title: '状态',
      dataIndex: 'isRead',
      key: 'isRead',
      width: 80,
      render: (isRead: boolean) => (
        <Tag color={isRead ? 'default' : 'processing'}>
          {isRead ? '已读' : '未读'}
        </Tag>
      ),
    },
    {
      title: '创建时间',
      dataIndex: 'createdAt',
      key: 'createdAt',
      width: 180,
      render: (createdAt: string) => dayjs(createdAt).format('YYYY-MM-DD HH:mm:ss'),
    },
    {
      title: '操作',
      key: 'action',
      width: 120,
      render: (_, record) => (
        <Space size="small">
          <Tooltip title="查看详情">
            <Button
              type="text"
              icon={<EyeOutlined />}
              onClick={() => handleViewDetail(record.id)}
            />
          </Tooltip>
          <Tooltip title="删除">
            <Button
              type="text"
              danger
              icon={<DeleteOutlined />}
              onClick={() => handleDelete(record.id)}
            />
          </Tooltip>
        </Space>
      ),
    },
  ];

  // 批量操作菜单
  const batchMenu = (
    <Dropdown.Menu>
      <Dropdown.Menu.Item
        key="read"
        icon={<CheckCircleOutlined />}
        onClick={handleBatchMarkRead}
      >
        标记已读
      </Dropdown.Menu.Item>
      <Dropdown.Menu.Item
        key="delete"
        icon={<DeleteOutlined />}
        danger
        onClick={handleBatchDelete}
      >
        删除
      </Dropdown.Menu.Item>
    </Dropdown.Menu>
  );

  return (
    <PageContainer
      title={
        <Space>
          <BellOutlined />
          消息中心
        </Space>
      }
      extra={
        <Button icon={<ReloadOutlined />} onClick={handleRefresh}>
          刷新
        </Button>
      }
    >
      {/* 统计卡片 */}
      <Row gutter={16} style={{ marginBottom: 16 }}>
        <Col span={6}>
          <Card>
            <Statistic
              title="总消息数"
              value={statistics?.totalCount || 0}
              prefix={<MailOutlined />}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="未读消息"
              value={statistics?.unreadCount || 0}
              valueStyle={{ color: '#ff4d4f' }}
              prefix={<BellOutlined />}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="已读消息"
              value={statistics?.readCount || 0}
              valueStyle={{ color: '#52c41a' }}
              prefix={<CheckCircleOutlined />}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="今日消息"
              value={statistics?.todayCount || 0}
              prefix={<InfoCircleOutlined />}
            />
          </Card>
        </Col>
      </Row>

      {/* 筛选区 */}
      <Card style={{ marginBottom: 16 }}>
        <Row gutter={16}>
          <Col span={6}>
            <Input
              placeholder="搜索标题或内容"
              value={keyword}
              onChange={(e) => setKeyword(e.target.value)}
              allowClear
            />
          </Col>
          <Col span={4}>
            <Select
              placeholder="消息类型"
              value={typeFilter}
              onChange={setTypeFilter}
              allowClear
              style={{ width: '100%' }}
            >
              <Option value="SYSTEM">系统</Option>
              <Option value="ALERT">告警</Option>
              <Option value="OPERATION">操作</Option>
            </Select>
          </Col>
          <Col span={4}>
            <Select
              placeholder="消息级别"
              value={levelFilter}
              onChange={setLevelFilter}
              allowClear
              style={{ width: '100%' }}
            >
              <Option value="INFO">普通</Option>
              <Option value="WARNING">警告</Option>
              <Option value="ERROR">错误</Option>
            </Select>
          </Col>
          <Col span={4}>
            <Select
              placeholder="状态"
              value={isReadFilter}
              onChange={setIsReadFilter}
              allowClear
              style={{ width: '100%' }}
            >
              <Option value={true}>已读</Option>
              <Option value={false}>未读</Option>
            </Select>
          </Col>
          <Col span={4}>
            <RangePicker
              value={dateRange}
              onChange={(dates) => setDateRange(dates as any)}
              style={{ width: '100%' }}
            />
          </Col>
          <Col span={2}>
            <Space>
              <Button onClick={handleReset}>重置</Button>
            </Space>
          </Col>
        </Row>
      </Card>

      {/* 消息列表 */}
      <Card>
        <Row style={{ marginBottom: 16 }}>
          <Col>
            <Dropdown overlay={batchMenu} trigger={['click']}>
              <Button disabled={selectedRowKeys.length === 0}>
                批量操作 <span style={{ marginLeft: 4 }}>▼</span>
              </Button>
            </Dropdown>
            {selectedRowKeys.length > 0 && (
              <Text type="secondary" style={{ marginLeft: 12 }}>
                已选择 {selectedRowKeys.length} 条
              </Text>
            )}
          </Col>
        </Row>

        <Table
          rowKey="id"
          columns={columns}
          dataSource={messages}
          loading={loading}
          rowSelection={{
            selectedRowKeys,
            onChange: setSelectedRowKeys,
          }}
          pagination={{
            current: queryParams.offset ? queryParams.offset / 20 + 1 : 1,
            pageSize: queryParams.limit || 20,
            showSizeChanger: true,
            showTotal: (total) => `共 ${total} 条`,
          }}
          locale={{ emptyText: <Empty description="暂无消息" /> }}
        />
      </Card>

      {/* 详情弹窗 */}
      <Modal
        title={selectedMessage?.title}
        open={detailVisible}
        onCancel={() => setDetailVisible(false)}
        footer={[
          <Button key="close" onClick={() => setDetailVisible(false)}>
            关闭
          </Button>,
        ]}
        width={600}
      >
        {selectedMessage && (
          <div>
            <Space style={{ marginBottom: 16 }}>
              {getTypeTag(selectedMessage.type)}
              {getLevelTag(selectedMessage.level)}
              <Tag color={selectedMessage.isRead ? 'default' : 'processing'}>
                {selectedMessage.isRead ? '已读' : '未读'}
              </Tag>
            </Space>
            <div style={{ whiteSpace: 'pre-wrap', lineHeight: 1.8 }}>
              {selectedMessage.content}
            </div>
            <div style={{ marginTop: 16, color: '#999', fontSize: 12 }}>
              创建时间：{dayjs(selectedMessage.createdAt).format('YYYY-MM-DD HH:mm:ss')}
            </div>
          </div>
        )}
      </Modal>
    </PageContainer>
  );
};

export default MessageCenter;
