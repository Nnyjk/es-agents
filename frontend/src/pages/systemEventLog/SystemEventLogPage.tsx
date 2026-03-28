import React, { useEffect, useState } from 'react';
import { Table, Card, Form, Input, Select, Button, DatePicker, Space, Tag, Typography, Row, Col, Modal } from 'antd';
import { SearchOutlined, ReloadOutlined, ClearOutlined } from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import dayjs from 'dayjs';
import { systemEventLogApi } from '../../services/systemEventLog';
import type { SystemEventLog, EventQueryCriteria } from '../../types/systemEventLog';
import styles from './SystemEventLogPage.module.css';

const { Title } = Typography;
const { Option } = Select;

const SystemEventLogPage: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [events, setEvents] = useState<SystemEventLog[]>([]);
  const [total, setTotal] = useState(0);
  const [form] = Form.useForm();
  const [selectedEvent, setSelectedEvent] = useState<SystemEventLog | null>(null);
  const [detailModalVisible, setDetailModalVisible] = useState(false);

  const loadEvents = async (criteria: EventQueryCriteria = {}) => {
    setLoading(true);
    try {
      const page = criteria.page || 1;
      const size = criteria.size || 10;
      const result = await systemEventLogApi.queryEvents({ ...criteria, page, size });
      setEvents(result.items);
      setTotal(result.total);
    } catch (error) {
      console.error('Failed to load events:', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadEvents();
  }, []);

  const handleSearch = (values: EventQueryCriteria) => {
    const criteria: EventQueryCriteria = {};
    
    if (values.eventType) criteria.eventType = values.eventType;
    if (values.eventLevel) criteria.eventLevel = values.eventLevel;
    if (values.module) criteria.module = values.module;
    if (values.action) criteria.action = values.action;
    if (values.status) criteria.status = values.status;
    if (values.userId) criteria.userId = values.userId;
    if (values.agentId) criteria.agentId = values.agentId;
    if (values.startTime) criteria.startTime = dayjs(values.startTime).format('YYYY-MM-DD HH:mm:ss');
    if (values.endTime) criteria.endTime = dayjs(values.endTime).format('YYYY-MM-DD HH:mm:ss');
    
    loadEvents(criteria);
  };

  const handleReset = () => {
    form.resetFields();
    loadEvents();
  };

  const showDetail = (record: SystemEventLog) => {
    setSelectedEvent(record);
    setDetailModalVisible(true);
  };

  const getLevelColor = (level: string) => {
    switch (level) {
      case 'ERROR': return 'red';
      case 'WARN': return 'orange';
      case 'INFO': return 'blue';
      case 'DEBUG': return 'gray';
      default: return 'default';
    }
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'SUCCESS': return 'green';
      case 'FAILURE': return 'red';
      case 'PENDING': return 'orange';
      default: return 'default';
    }
  };

  const columns: ColumnsType<SystemEventLog> = [
    {
      title: 'ID',
      dataIndex: 'id',
      key: 'id',
      width: 80,
    },
    {
      title: '事件类型',
      dataIndex: 'eventType',
      key: 'eventType',
      width: 120,
    },
    {
      title: '级别',
      dataIndex: 'eventLevel',
      key: 'eventLevel',
      width: 80,
      render: (level: string) => <Tag color={getLevelColor(level)}>{level}</Tag>,
    },
    {
      title: '模块',
      dataIndex: 'module',
      key: 'module',
      width: 100,
    },
    {
      title: '动作',
      dataIndex: 'action',
      key: 'action',
      width: 120,
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 90,
      render: (status: string) => <Tag color={getStatusColor(status)}>{status}</Tag>,
    },
    {
      title: '描述',
      dataIndex: 'description',
      key: 'description',
      ellipsis: true,
    },
    {
      title: '时间',
      dataIndex: 'createdAt',
      key: 'createdAt',
      width: 180,
      render: (createdAt: string) => dayjs(createdAt).format('YYYY-MM-DD HH:mm:ss'),
    },
    {
      title: '操作',
      key: 'action',
      width: 80,
      render: (_, record) => (
        <Button type="link" onClick={() => showDetail(record)}>详情</Button>
      ),
    },
  ];

  return (
    <div className={styles.container}>
      <Card className={styles.filterCard}>
        <Form form={form} onFinish={handleSearch} layout="vertical">
          <Row gutter={16}>
            <Col span={6}>
              <Form.Item name="eventType" label="事件类型">
                <Input placeholder="请输入事件类型" />
              </Form.Item>
            </Col>
            <Col span={6}>
              <Form.Item name="eventLevel" label="级别">
                <Select placeholder="请选择级别" allowClear>
                  <Option value="INFO">INFO</Option>
                  <Option value="WARN">WARN</Option>
                  <Option value="ERROR">ERROR</Option>
                  <Option value="DEBUG">DEBUG</Option>
                </Select>
              </Form.Item>
            </Col>
            <Col span={6}>
              <Form.Item name="module" label="模块">
                <Input placeholder="请输入模块名" />
              </Form.Item>
            </Col>
            <Col span={6}>
              <Form.Item name="status" label="状态">
                <Select placeholder="请选择状态" allowClear>
                  <Option value="SUCCESS">SUCCESS</Option>
                  <Option value="FAILURE">FAILURE</Option>
                  <Option value="PENDING">PENDING</Option>
                </Select>
              </Form.Item>
            </Col>
          </Row>
          <Row gutter={16}>
            <Col span={6}>
              <Form.Item name="startTime" label="开始时间">
                <DatePicker showTime style={{ width: '100%' }} />
              </Form.Item>
            </Col>
            <Col span={6}>
              <Form.Item name="endTime" label="结束时间">
                <DatePicker showTime style={{ width: '100%' }} />
              </Form.Item>
            </Col>
            <Col span={12} style={{ display: 'flex', alignItems: 'flex-end' }}>
              <Space>
                <Button type="primary" htmlType="submit" icon={<SearchOutlined />}>
                  查询
                </Button>
                <Button onClick={handleReset} icon={<ClearOutlined />}>
                  重置
                </Button>
                <Button onClick={() => loadEvents()} icon={<ReloadOutlined />}>
                  刷新
                </Button>
              </Space>
            </Col>
          </Row>
        </Form>
      </Card>

      <Card className={styles.tableCard}>
        <Title level={4}>系统事件日志</Title>
        <Table
          columns={columns}
          dataSource={events}
          rowKey="id"
          loading={loading}
          pagination={{
            total,
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (total) => `共 ${total} 条`,
          }}
        />
      </Card>

      <Modal
        title="事件详情"
        open={detailModalVisible}
        onCancel={() => setDetailModalVisible(false)}
        footer={null}
        width={800}
      >
        {selectedEvent && (
          <div className={styles.detailContent}>
            <Row gutter={16}>
              <Col span={12}>
                <p><strong>ID:</strong> {selectedEvent.id}</p>
                <p><strong>事件类型:</strong> {selectedEvent.eventType}</p>
                <p><strong>级别:</strong> <Tag color={getLevelColor(selectedEvent.eventLevel)}>{selectedEvent.eventLevel}</Tag></p>
                <p><strong>模块:</strong> {selectedEvent.module}</p>
                <p><strong>动作:</strong> {selectedEvent.action}</p>
                <p><strong>状态:</strong> <Tag color={getStatusColor(selectedEvent.status)}>{selectedEvent.status}</Tag></p>
              </Col>
              <Col span={12}>
                <p><strong>用户:</strong> {selectedEvent.username || selectedEvent.userId || '-'}</p>
                <p><strong>Agent:</strong> {selectedEvent.agentName || selectedEvent.agentId || '-'}</p>
                <p><strong>目标:</strong> {selectedEvent.goalName || selectedEvent.goalId || '-'}</p>
                <p><strong>批次:</strong> {selectedEvent.batchOperationId || '-'}</p>
                <p><strong>耗时:</strong> {selectedEvent.duration ? `${selectedEvent.duration}ms` : '-'}</p>
                <p><strong>时间:</strong> {dayjs(selectedEvent.createdAt).format('YYYY-MM-DD HH:mm:ss')}</p>
              </Col>
            </Row>
            <p><strong>描述:</strong></p>
            <p>{selectedEvent.description}</p>
            {selectedEvent.errorMessage && (
              <>
                <p><strong>错误信息:</strong></p>
                <pre className={styles.errorMessage}>{selectedEvent.errorMessage}</pre>
              </>
            )}
            {selectedEvent.metadata && (
              <>
                <p><strong>元数据:</strong></p>
                <pre className={styles.metadata}>{selectedEvent.metadata}</pre>
              </>
            )}
          </div>
        )}
      </Modal>
    </div>
  );
};

export default SystemEventLogPage;
