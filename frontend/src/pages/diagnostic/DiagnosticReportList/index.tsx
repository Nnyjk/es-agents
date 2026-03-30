import React, { useState, useEffect } from "react";
import {
  Card,
  Table,
  Button,
  Space,
  Tag,
  Modal,
  Form,
  Input,
  Badge,
  Row,
  Col,
  Statistic,
  message,
  Typography,
  Descriptions,
  List,
  Spin,
} from "antd";
import {
  FileTextOutlined,
  ReloadOutlined,
  PlusOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  SyncOutlined,
} from "@ant-design/icons";
import type { ColumnsType } from "antd/es/table";
import { PageContainer } from "@ant-design/pro-components";
import dayjs from "dayjs";
import {
  getDiagnosticReports,
  getDiagnosticReport,
  generateDiagnosticReport,
  deleteDiagnosticReport,
  type DiagnosticReportSummary,
  type DiagnosticReportWithFindings,
  type FindingSeverity,
  type ReportStatus,
} from "@/services/diagnostic";

const { Text, Title } = Typography;

const statusConfig: Record<
  ReportStatus,
  { color: string; icon: React.ReactNode; text: string }
> = {
  GENERATING: {
    color: "processing",
    icon: <SyncOutlined spin />,
    text: "生成中",
  },
  COMPLETED: {
    color: "success",
    icon: <CheckCircleOutlined />,
    text: "已完成",
  },
  FAILED: { color: "error", icon: <CloseCircleOutlined />, text: "失败" },
};

const severityConfig: Record<
  FindingSeverity,
  {
    color: "default" | "processing" | "success" | "warning" | "error";
    text: string;
  }
> = {
  INFO: { color: "default", text: "信息" },
  WARNING: { color: "warning", text: "警告" },
  CRITICAL: { color: "error", text: "严重" },
  FATAL: { color: "error", text: "致命" },
};

const DiagnosticReportList: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [reports, setReports] = useState<DiagnosticReportSummary[]>([]);
  const [detailVisible, setDetailVisible] = useState(false);
  const [currentReport, setCurrentReport] =
    useState<DiagnosticReportWithFindings | null>(null);
  const [detailLoading, setDetailLoading] = useState(false);
  const [generateModalVisible, setGenerateModalVisible] = useState(false);
  const [generating, setGenerating] = useState(false);
  const [form] = Form.useForm();

  const fetchReports = async () => {
    setLoading(true);
    try {
      const data = await getDiagnosticReports();
      setReports(data);
    } catch (error) {
      message.error("获取诊断报告列表失败");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchReports();
  }, []);

  const handleViewDetail = async (reportId: string) => {
    setDetailLoading(true);
    setDetailVisible(true);
    try {
      const data = await getDiagnosticReport(reportId);
      setCurrentReport(data);
    } catch (error) {
      message.error("获取报告详情失败");
      setDetailVisible(false);
    } finally {
      setDetailLoading(false);
    }
  };

  const handleGenerate = async (values: { title: string }) => {
    setGenerating(true);
    try {
      await generateDiagnosticReport({ title: values.title });
      message.success("诊断报告生成中");
      setGenerateModalVisible(false);
      form.resetFields();
      fetchReports();
    } catch (error) {
      message.error("生成诊断报告失败");
    } finally {
      setGenerating(false);
    }
  };

  const handleDelete = (reportId: string) => {
    Modal.confirm({
      title: "确认删除",
      content: "确定要删除该诊断报告吗？",
      onOk: async () => {
        try {
          await deleteDiagnosticReport(reportId);
          message.success("删除成功");
          fetchReports();
        } catch (error) {
          message.error("删除失败");
        }
      },
    });
  };

  const columns: ColumnsType<DiagnosticReportSummary> = [
    {
      title: "报告ID",
      dataIndex: "reportId",
      key: "reportId",
      width: 150,
    },
    {
      title: "标题",
      dataIndex: "title",
      key: "title",
      ellipsis: true,
    },
    {
      title: "状态",
      dataIndex: "status",
      key: "status",
      width: 100,
      render: (status: ReportStatus) => {
        const config = statusConfig[status];
        return (
          <Tag color={config.color} icon={config.icon}>
            {config.text}
          </Tag>
        );
      },
    },
    {
      title: "发现问题",
      dataIndex: "totalFindings",
      key: "totalFindings",
      width: 100,
      render: (total: number, record) => (
        <Space size="small">
          <Text>{total}</Text>
          {record.fatalCount > 0 && (
            <Badge
              count={record.fatalCount}
              style={{ backgroundColor: "#ff4d4f" }}
            />
          )}
          {record.criticalCount > 0 && (
            <Badge
              count={record.criticalCount}
              style={{ backgroundColor: "#ff7a45" }}
            />
          )}
        </Space>
      ),
    },
    {
      title: "警告",
      dataIndex: "warningCount",
      key: "warningCount",
      width: 80,
      render: (count: number) =>
        count > 0 ? <Tag color="warning">{count}</Tag> : "-",
    },
    {
      title: "严重",
      dataIndex: "criticalCount",
      key: "criticalCount",
      width: 80,
      render: (count: number) =>
        count > 0 ? <Tag color="error">{count}</Tag> : "-",
    },
    {
      title: "生成时间",
      dataIndex: "createdAt",
      key: "createdAt",
      width: 180,
      render: (time: string) => dayjs(time).format("YYYY-MM-DD HH:mm:ss"),
    },
    {
      title: "操作",
      key: "action",
      width: 150,
      render: (_, record) => (
        <Space>
          <Button
            type="link"
            size="small"
            onClick={() => handleViewDetail(record.reportId)}
          >
            详情
          </Button>
          <Button
            type="link"
            size="small"
            danger
            onClick={() => handleDelete(record.reportId)}
          >
            删除
          </Button>
        </Space>
      ),
    },
  ];

  return (
    <PageContainer>
      <Card>
        <Row gutter={16} style={{ marginBottom: 16 }}>
          <Col span={6}>
            <Statistic
              title="报告总数"
              value={reports.length}
              prefix={<FileTextOutlined />}
            />
          </Col>
          <Col span={6}>
            <Statistic
              title="已完成"
              value={reports.filter((r) => r.status === "COMPLETED").length}
              valueStyle={{ color: "#52c41a" }}
            />
          </Col>
          <Col span={6}>
            <Statistic
              title="生成中"
              value={reports.filter((r) => r.status === "GENERATING").length}
              valueStyle={{ color: "#1890ff" }}
            />
          </Col>
          <Col span={6}>
            <Statistic
              title="发现问题总数"
              value={reports.reduce((sum, r) => sum + r.totalFindings, 0)}
            />
          </Col>
        </Row>

        <div style={{ marginBottom: 16 }}>
          <Space>
            <Button
              type="primary"
              icon={<PlusOutlined />}
              onClick={() => setGenerateModalVisible(true)}
            >
              生成报告
            </Button>
            <Button icon={<ReloadOutlined />} onClick={fetchReports}>
              刷新
            </Button>
          </Space>
        </div>

        <Table
          columns={columns}
          dataSource={reports}
          rowKey="reportId"
          loading={loading}
          pagination={{ pageSize: 10 }}
        />
      </Card>

      <Modal
        title="生成诊断报告"
        open={generateModalVisible}
        onCancel={() => setGenerateModalVisible(false)}
        footer={null}
      >
        <Form form={form} layout="vertical" onFinish={handleGenerate}>
          <Form.Item
            name="title"
            label="报告标题"
            rules={[{ required: true, message: "请输入报告标题" }]}
            initialValue={`系统诊断报告 - ${dayjs().format("YYYY-MM-DD HH:mm")}`}
          >
            <Input placeholder="请输入报告标题" />
          </Form.Item>
          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit" loading={generating}>
                生成
              </Button>
              <Button onClick={() => setGenerateModalVisible(false)}>
                取消
              </Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title={`报告详情 - ${currentReport?.title || ""}`}
        open={detailVisible}
        onCancel={() => setDetailVisible(false)}
        footer={null}
        width={900}
      >
        {detailLoading ? (
          <div style={{ textAlign: "center", padding: 40 }}>
            <Spin />
          </div>
        ) : currentReport ? (
          <div>
            <Descriptions
              bordered
              column={2}
              size="small"
              style={{ marginBottom: 16 }}
            >
              <Descriptions.Item label="报告ID">
                {currentReport.reportId}
              </Descriptions.Item>
              <Descriptions.Item label="状态">
                <Tag color={statusConfig[currentReport.status].color}>
                  {statusConfig[currentReport.status].text}
                </Tag>
              </Descriptions.Item>
              <Descriptions.Item label="生成时间">
                {dayjs(currentReport.createdAt).format("YYYY-MM-DD HH:mm:ss")}
              </Descriptions.Item>
              <Descriptions.Item label="完成时间">
                {currentReport.completedAt
                  ? dayjs(currentReport.completedAt).format(
                      "YYYY-MM-DD HH:mm:ss",
                    )
                  : "-"}
              </Descriptions.Item>
              <Descriptions.Item label="发现问题" span={2}>
                <Space>
                  <Text>总计: {currentReport.totalFindings}</Text>
                  <Badge
                    status="default"
                    text={`信息: ${currentReport.infoCount}`}
                  />
                  <Badge
                    status="warning"
                    text={`警告: ${currentReport.warningCount}`}
                  />
                  <Badge
                    status="error"
                    text={`严重: ${currentReport.criticalCount}`}
                  />
                  <Badge
                    status="error"
                    text={`致命: ${currentReport.fatalCount}`}
                  />
                </Space>
              </Descriptions.Item>
              <Descriptions.Item label="摘要" span={2}>
                {currentReport.summary || "-"}
              </Descriptions.Item>
            </Descriptions>

            <Title level={5}>发现问题</Title>
            <List
              dataSource={currentReport.findings}
              renderItem={(item) => (
                <List.Item>
                  <List.Item.Meta
                    avatar={
                      <Badge
                        status={severityConfig[item.severity].color}
                        text={severityConfig[item.severity].text}
                      />
                    }
                    title={item.title}
                    description={
                      <div>
                        <Text type="secondary">{item.description}</Text>
                        <br />
                        {item.recommendation && (
                          <Text type="success">
                            建议: {item.recommendation}
                          </Text>
                        )}
                      </div>
                    }
                  />
                </List.Item>
              )}
            />
          </div>
        ) : null}
      </Modal>
    </PageContainer>
  );
};

export default DiagnosticReportList;
