import React, { useState, useEffect } from 'react';
import { Card, Table, Tag, Space, Button, Input, DatePicker, Row, Col, Typography, Descriptions } from 'antd';
import { SearchOutlined, ReloadOutlined, EyeOutlined } from '@ant-design/icons';
import dayjs from 'dayjs';
import api from '../../utils/api';

const { Title } = Typography;
const { RangePicker } = DatePicker;

interface TraceSummary {
  traceId: string;
  serviceName: string;
  operation: string;
  startTime: string;
  durationMs: number;
  hasError: boolean;
}

interface TraceDetail {
  traceId: string;
  spans: Array<{
    traceId: string;
    spanId: string;
    parentSpanId: string;
    serviceName: string;
    operationName: string;
    spanKind: string;
    startTime: string;
    durationMs: number;
    hasError: boolean;
    attributes: Array<[string, string]>;
  }>;
}

const TracingPage: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [traces, setTraces] = useState<TraceSummary[]>([]);
  const [selectedTrace, setSelectedTrace] = useState<TraceDetail | null>(null);
  const [searchParams, setSearchParams] = useState({
    serviceId: '',
    operation: '',
    timeRange: null as [dayjs.Dayjs, dayjs.Dayjs] | null,
  });

  const loadTraces = async () => {
    setLoading(true);
    try {
      const params: Record<string, string> = { limit: '20' };
      if (searchParams.serviceId) params.serviceId = searchParams.serviceId;
      if (searchParams.operation) params.operation = searchParams.operation;
      
      const response = await api.get('/api/v1/tracing/traces', { params });
      setTraces(response.data.traces || []);
    } catch (error) {
      console.error('加载链路列表失败:', error);
    } finally {
      setLoading(false);
    }
  };

  const loadTraceDetail = async (traceId: string) => {
    try {
      const response = await api.get(`/api/v1/tracing/traces/${traceId}`);
      setSelectedTrace(response.data);
    } catch (error) {
      console.error('加载链路详情失败:', error);
    }
  };

  useEffect(() => {
    loadTraces();
  }, []);

  const columns = [
    {
      title: 'Trace ID',
      dataIndex: 'traceId',
      key: 'traceId',
      width: 200,
      render: (text: string) => <span style={{ fontFamily: 'monospace' }}>{text}</span>,
    },
    {
      title: '服务',
      dataIndex: 'serviceName',
      key: 'serviceName',
      width: 150,
    },
    {
      title: '操作',
      dataIndex: 'operation',
      key: 'operation',
      width: 200,
    },
    {
      title: '开始时间',
      dataIndex: 'startTime',
      key: 'startTime',
      width: 180,
      render: (text: string) => dayjs(text).format('YYYY-MM-DD HH:mm:ss'),
    },
    {
      title: '耗时',
      dataIndex: 'durationMs',
      key: 'durationMs',
      width: 100,
      render: (ms: number) => `${ms}ms`,
    },
    {
      title: '状态',
      dataIndex: 'hasError',
      key: 'hasError',
      width: 80,
      render: (hasError: boolean) => (
        <Tag color={hasError ? 'red' : 'green'}>
          {hasError ? '错误' : '成功'}
        </Tag>
      ),
    },
    {
      title: '操作',
      key: 'action',
      width: 100,
      render: (_: any, record: TraceSummary) => (
        <Button
          type="link"
          icon={<EyeOutlined />}
          onClick={() => loadTraceDetail(record.traceId)}
        >
          查看
        </Button>
      ),
    },
  ];

  return (
    <div style={{ padding: '24px' }}>
      <Title level={2}>链路追踪</Title>
      
      <Card style={{ marginBottom: 16 }}>
        <Row gutter={16}>
          <Col span={6}>
            <Input
              placeholder="服务 ID"
              value={searchParams.serviceId}
              onChange={(e) => setSearchParams({ ...searchParams, serviceId: e.target.value })}
              onPressEnter={loadTraces}
            />
          </Col>
          <Col span={6}>
            <Input
              placeholder="操作名称"
              value={searchParams.operation}
              onChange={(e) => setSearchParams({ ...searchParams, operation: e.target.value })}
              onPressEnter={loadTraces}
            />
          </Col>
          <Col span={6}>
            <RangePicker
              value={searchParams.timeRange}
              onChange={(dates) => setSearchParams({ ...searchParams, timeRange: dates as any })}
            />
          </Col>
          <Col span={6}>
            <Space>
              <Button type="primary" icon={<SearchOutlined />} onClick={loadTraces}>
                搜索
              </Button>
              <Button icon={<ReloadOutlined />} onClick={loadTraces}>
                刷新
              </Button>
            </Space>
          </Col>
        </Row>
      </Card>

      <Card style={{ marginBottom: 16 }}>
        <Table
          columns={columns}
          dataSource={traces}
          rowKey="traceId"
          loading={loading}
          pagination={{ pageSize: 20 }}
        />
      </Card>

      {selectedTrace && (
        <Card
          title={`链路详情：${selectedTrace.traceId}`}
          extra={<Button onClick={() => setSelectedTrace(null)}>关闭</Button>}
        >
          <Descriptions bordered column={1}>
            <Descriptions.Item label="Trace ID">
              <span style={{ fontFamily: 'monospace' }}>{selectedTrace.traceId}</span>
            </Descriptions.Item>
            <Descriptions.Item label="Span 数量">
              {selectedTrace.spans.length}
            </Descriptions.Item>
          </Descriptions>

          <Table
            columns={[
              {
                title: 'Span ID',
                dataIndex: 'spanId',
                key: 'spanId',
                width: 150,
                render: (text: string) => <span style={{ fontFamily: 'monospace' }}>{text}</span>,
              },
              {
                title: '父 Span',
                dataIndex: 'parentSpanId',
                key: 'parentSpanId',
                width: 150,
                render: (text: string) => text ? <span style={{ fontFamily: 'monospace' }}>{text}</span> : '-',
              },
              {
                title: '服务',
                dataIndex: 'serviceName',
                key: 'serviceName',
              },
              {
                title: '操作',
                dataIndex: 'operationName',
                key: 'operationName',
              },
              {
                title: '类型',
                dataIndex: 'spanKind',
                key: 'spanKind',
                width: 100,
                render: (kind: string) => <Tag>{kind}</Tag>,
              },
              {
                title: '耗时',
                dataIndex: 'durationMs',
                key: 'durationMs',
                width: 80,
                render: (ms: number) => `${ms}ms`,
              },
              {
                title: '状态',
                dataIndex: 'hasError',
                key: 'hasError',
                width: 80,
                render: (hasError: boolean) => (
                  <Tag color={hasError ? 'red' : 'green'}>
                    {hasError ? '错误' : '成功'}
                  </Tag>
                ),
              },
            ]}
            dataSource={selectedTrace.spans}
            rowKey="spanId"
            pagination={false}
            size="small"
          />
        </Card>
      )}
    </div>
  );
};

export default TracingPage;
