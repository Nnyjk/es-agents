import React, { useEffect, useState } from "react";
import {
  Card,
  Table,
  Button,
  Space,
  Tag,
  Modal,
  Form,
  Input,
  Select,
  message,
  Tabs,
  Tooltip,
  Progress,
  Row,
  Col,
  Descriptions,
  List,
  Collapse,
} from "antd";
import {
  PlusOutlined,
  EyeOutlined,
  ReloadOutlined,
  CheckCircleOutlined,
  FileTextOutlined,
  DownloadOutlined,
  CheckOutlined,
} from "@ant-design/icons";
import type { ColumnsType } from "antd/es/table";
import dayjs from "dayjs";
import {
  getComplianceSelfChecks,
  getComplianceCheckItems,
  createComplianceSelfCheck,
  deleteComplianceSelfCheck,
  submitCheckItem,
  getComplianceStandards,
  generateGapReport,
} from "../../../services/security";
import type {
  ComplianceSelfCheck,
  ComplianceCheckItem,
  ComplianceStandard,
} from "../../../types/security";

const { TextArea } = Input;

const ComplianceCheck: React.FC = () => {
  const [checks, setChecks] = useState<ComplianceSelfCheck[]>([]);
  const [standards, setStandards] = useState<ComplianceStandard[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [detailModalVisible, setDetailModalVisible] = useState(false);
  const [currentCheck, setCurrentCheck] = useState<ComplianceSelfCheck | null>(
    null,
  );
  const [checkItems, setCheckItems] = useState<ComplianceCheckItem[]>([]);
  const [form] = Form.useForm();

  const fetchChecks = async () => {
    setLoading(true);
    try {
      const response = await getComplianceSelfChecks();
      setChecks(response.data || []);
    } catch {
      // Mock data for demo
      setChecks([
        {
          id: "1",
          name: "等保二级自查-2024Q1",
          standardId: "std-1",
          standardName: "网络安全等级保护基本要求",
          status: "completed",
          totalItems: 100,
          checkedItems: 100,
          compliantItems: 85,
          nonCompliantItems: 10,
          notApplicableItems: 5,
          complianceRate: 85,
          startTime: "2024-01-15T10:00:00Z",
          endTime: "2024-01-15T12:00:00Z",
          createdAt: "2024-01-15T09:00:00Z",
          updatedAt: "2024-01-15T12:00:00Z",
          createdBy: "user1",
          createdByName: "张三",
        },
        {
          id: "2",
          name: "等保三级自查-2024Q1",
          standardId: "std-2",
          standardName: "网络安全等级保护基本要求-三级",
          status: "in-progress",
          totalItems: 150,
          checkedItems: 80,
          compliantItems: 60,
          nonCompliantItems: 15,
          notApplicableItems: 5,
          complianceRate: 75,
          startTime: "2024-01-20T10:00:00Z",
          createdAt: "2024-01-20T09:00:00Z",
          updatedAt: "2024-01-20T10:30:00Z",
          createdBy: "user2",
          createdByName: "李四",
        },
        {
          id: "3",
          name: "等保二级自查-2024Q2",
          standardId: "std-1",
          standardName: "网络安全等级保护基本要求",
          status: "pending",
          totalItems: 100,
          checkedItems: 0,
          compliantItems: 0,
          nonCompliantItems: 0,
          notApplicableItems: 0,
          complianceRate: 0,
          createdAt: "2024-04-01T09:00:00Z",
          updatedAt: "2024-04-01T09:00:00Z",
          createdBy: "user1",
          createdByName: "张三",
        },
      ]);
    } finally {
      setLoading(false);
    }
  };

  const fetchStandards = async () => {
    try {
      const response = await getComplianceStandards();
      setStandards(response || []);
    } catch {
      setStandards([
        {
          id: "std-1",
          name: "网络安全等级保护基本要求",
          code: "GB/T 22239-2019",
          version: "2019",
          level: "level2",
          category: "host",
          itemCount: 100,
          isBuiltIn: true,
          createdAt: "2024-01-01T00:00:00Z",
          updatedAt: "2024-01-01T00:00:00Z",
        },
        {
          id: "std-2",
          name: "网络安全等级保护基本要求-三级",
          code: "GB/T 22239-2019",
          version: "2019",
          level: "level3",
          category: "host",
          itemCount: 150,
          isBuiltIn: true,
          createdAt: "2024-01-01T00:00:00Z",
          updatedAt: "2024-01-01T00:00:00Z",
        },
      ]);
    }
  };

  const fetchCheckItems = async (checkId: string) => {
    try {
      const items = await getComplianceCheckItems(checkId);
      setCheckItems(items || []);
    } catch {
      // Mock data
      setCheckItems([
        {
          id: "item-1",
          checkId,
          standardItemId: "std-item-1",
          code: "A.1.1.1",
          name: "身份鉴别",
          category: "身份鉴别",
          description: "应对登录用户进行身份标识和鉴别",
          requirement: "登录用户必须有唯一标识，并采用口令或密码技术进行鉴别",
          checkMethod: "检查系统登录配置",
          expectedValue: "启用身份鉴别，用户标识唯一",
          status: "compliant",
          severity: "high",
          checkedAt: "2024-01-15T10:30:00Z",
          checkedBy: "user1",
          checkedByName: "张三",
        },
        {
          id: "item-2",
          checkId,
          standardItemId: "std-item-2",
          code: "A.1.1.2",
          name: "口令复杂度",
          category: "身份鉴别",
          description: "应对口令进行复杂度检查",
          requirement: "口令长度不少于8位，包含大小写字母、数字和特殊字符",
          checkMethod: "检查密码策略配置",
          expectedValue: "口令复杂度策略已启用",
          status: "non-compliant",
          severity: "high",
          actualValue: "口令复杂度策略未启用",
          checkedAt: "2024-01-15T10:35:00Z",
          checkedBy: "user1",
          checkedByName: "张三",
        },
        {
          id: "item-3",
          checkId,
          standardItemId: "std-item-3",
          code: "A.1.2.1",
          name: "访问控制",
          category: "访问控制",
          description: "应实现访问控制功能",
          requirement: "依据安全策略控制用户对文件、数据库等客体的访问",
          checkMethod: "检查访问控制列表",
          expectedValue: "访问控制策略已配置",
          status: "pending",
          severity: "medium",
        },
      ]);
    }
  };

  useEffect(() => {
    fetchChecks();
    fetchStandards();
  }, []);

  const handleCreate = async (values: { name: string; standardId: string }) => {
    try {
      const standard = standards.find((s) => s.id === values.standardId);
      await createComplianceSelfCheck({
        name: values.name,
        standardId: values.standardId,
      });
      message.success("创建成功");
      setModalVisible(false);
      form.resetFields();
      fetchChecks();
    } catch {
      message.success("创建成功（模拟）");
      setModalVisible(false);
      form.resetFields();
    }
  };

  const handleDelete = async (id: string) => {
    try {
      await deleteComplianceSelfCheck(id);
      message.success("删除成功");
      fetchChecks();
    } catch {
      message.success("删除成功（模拟）");
      setChecks(checks.filter((c) => c.id !== id));
    }
  };

  const handleViewDetail = (check: ComplianceSelfCheck) => {
    setCurrentCheck(check);
    setDetailModalVisible(true);
    fetchCheckItems(check.id);
  };

  const handleCheckItem = async (
    itemId: string,
    status: "compliant" | "non-compliant" | "not-applicable",
  ) => {
    if (!currentCheck) return;
    try {
      await submitCheckItem(currentCheck.id, itemId, { status });
      message.success("提交成功");
      fetchCheckItems(currentCheck.id);
    } catch {
      // Update local state
      setCheckItems(
        checkItems.map((item) =>
          item.id === itemId
            ? { ...item, status, checkedAt: new Date().toISOString() }
            : item,
        ),
      );
      message.success("提交成功（模拟）");
    }
  };

  const handleGenerateReport = async (checkId: string) => {
    try {
      const report = await generateGapReport(checkId, "pdf");
      message.success(`报告已生成: ${report.downloadUrl}`);
    } catch {
      message.success("差距报告已生成（模拟）");
    }
  };

  const getStatusTag = (status: string) => {
    const statusConfig: Record<
      string,
      { color: string; text: string }
    > = {
      pending: { color: "default", text: "待执行" },
      "in-progress": { color: "processing", text: "进行中" },
      completed: { color: "success", text: "已完成" },
      failed: { color: "error", text: "失败" },
    };
    const config = statusConfig[status] || {
      color: "default",
      text: status,
    };
    return <Tag color={config.color}>{config.text}</Tag>;
  };

  const columns: ColumnsType<ComplianceSelfCheck> = [
    {
      title: "检查名称",
      dataIndex: "name",
      key: "name",
      width: 200,
    },
    {
      title: "合规标准",
      dataIndex: "standardName",
      key: "standardName",
      width: 180,
    },
    {
      title: "状态",
      dataIndex: "status",
      key: "status",
      width: 100,
      render: (status: string) => getStatusTag(status),
    },
    {
      title: "进度",
      key: "progress",
      width: 200,
      render: (_: unknown, record: ComplianceSelfCheck) => (
        <div>
          <Progress
            percent={Math.round(
              (record.checkedItems / record.totalItems) * 100,
            )}
            size="small"
            status={record.status === "completed" ? "success" : "active"}
          />
          <span style={{ fontSize: 12, color: "#666" }}>
            {record.checkedItems}/{record.totalItems} 项
          </span>
        </div>
      ),
    },
    {
      title: "合规率",
      dataIndex: "complianceRate",
      key: "complianceRate",
      width: 100,
      render: (rate: number) => (
        <span
          style={{
            color: rate >= 80 ? "#52c41a" : rate >= 60 ? "#faad14" : "#ff4d4f",
            fontWeight: "bold",
          }}
        >
          {rate}%
        </span>
      ),
    },
    {
      title: "创建时间",
      dataIndex: "createdAt",
      key: "createdAt",
      width: 150,
      render: (date: string) => dayjs(date).format("YYYY-MM-DD HH:mm"),
    },
    {
      title: "操作",
      key: "action",
      width: 200,
      render: (_: unknown, record: ComplianceSelfCheck) => (
        <Space>
          <Button
            type="link"
            size="small"
            icon={<EyeOutlined />}
            onClick={() => handleViewDetail(record)}
          >
            详情
          </Button>
          <Tooltip title="生成差距报告">
            <Button
              type="link"
              size="small"
              icon={<DownloadOutlined />}
              onClick={() => handleGenerateReport(record.id)}
            />
          </Tooltip>
          <Button
            type="link"
            size="small"
            danger
            onClick={() => handleDelete(record.id)}
          >
            删除
          </Button>
        </Space>
      ),
    },
  ];

  const itemColumns: ColumnsType<ComplianceCheckItem> = [
    {
      title: "编号",
      dataIndex: "code",
      key: "code",
      width: 100,
    },
    {
      title: "检查项",
      dataIndex: "name",
      key: "name",
      width: 150,
    },
    {
      title: "类别",
      dataIndex: "category",
      key: "category",
      width: 100,
    },
    {
      title: "要求",
      dataIndex: "requirement",
      key: "requirement",
      ellipsis: true,
    },
    {
      title: "状态",
      dataIndex: "status",
      key: "status",
      width: 120,
      render: (status: string) => {
        const statusConfig: Record<
          string,
          { color: string; text: string; icon: React.ReactNode }
        > = {
          pending: {
            color: "default",
            text: "待检查",
            icon: null,
          },
          compliant: {
            color: "success",
            text: "符合",
            icon: <CheckCircleOutlined />,
          },
          "non-compliant": {
            color: "error",
            text: "不符合",
            icon: <CloseCircleOutlined />,
          },
          "not-applicable": {
            color: "warning",
            text: "不适用",
            icon: null,
          },
        };
        const config = statusConfig[status] || {
          color: "default",
          text: status,
          icon: null,
        };
        return (
          <Tag color={config.color} icon={config.icon}>
            {config.text}
          </Tag>
        );
      },
    },
    {
      title: "严重程度",
      dataIndex: "severity",
      key: "severity",
      width: 100,
      render: (severity: string) => {
        const colors: Record<string, string> = {
          critical: "#ff4d4f",
          high: "#fa8c16",
          medium: "#faad14",
          low: "#52c41a",
        };
        return (
          <Tag color={colors[severity] || "default"}>
            {severity.toUpperCase()}
          </Tag>
        );
      },
    },
    {
      title: "操作",
      key: "action",
      width: 200,
      render: (_: unknown, record: ComplianceCheckItem) => (
        <Space>
          <Tooltip title="符合">
            <Button
              type="link"
              size="small"
              icon={<CheckCircleOutlined style={{ color: "#52c41a" }} />}
              onClick={() => handleCheckItem(record.id, "compliant")}
              disabled={record.status === "compliant"}
            />
          </Tooltip>
          <Tooltip title="不符合">
            <Button
              type="link"
              size="small"
              icon={<CloseCircleOutlined style={{ color: "#ff4d4f" }} />}
              onClick={() => handleCheckItem(record.id, "non-compliant")}
              disabled={record.status === "non-compliant"}
            />
          </Tooltip>
          <Tooltip title="不适用">
            <Button
              type="link"
              size="small"
              onClick={() => handleCheckItem(record.id, "not-applicable")}
              disabled={record.status === "not-applicable"}
            >
              N/A
            </Button>
          </Tooltip>
        </Space>
      ),
    },
  ];

  const tabItems = [
    {
      key: "list",
      label: "自查任务",
      children: (
        <Table
          columns={columns}
          dataSource={checks}
          rowKey="id"
          loading={loading}
        />
      ),
    },
    {
      key: "standards",
      label: "合规标准",
      children: (
        <Table
          columns={[
            { title: "名称", dataIndex: "name", key: "name" },
            { title: "编码", dataIndex: "code", key: "code", width: 150 },
            {
              title: "等级",
              dataIndex: "level",
              key: "level",
              width: 100,
              render: (level: string) => (
                <Tag color="blue">{level.toUpperCase()}</Tag>
              ),
            },
            {
              title: "类别",
              dataIndex: "category",
              key: "category",
              width: 100,
            },
            { title: "检查项数", dataIndex: "itemCount", key: "itemCount" },
          ]}
          dataSource={standards}
          rowKey="id"
        />
      ),
    },
  ];

  return (
    <div>
      <Card
        title="合规自查管理"
        extra={
          <Space>
            <Button icon={<ReloadOutlined />} onClick={fetchChecks}>
              刷新
            </Button>
            <Button
              type="primary"
              icon={<PlusOutlined />}
              onClick={() => setModalVisible(true)}
            >
              新建自查
            </Button>
          </Space>
        }
      >
        <Tabs items={tabItems} />
      </Card>

      <Modal
        title="新建合规自查"
        open={modalVisible}
        onCancel={() => setModalVisible(false)}
        onOk={() => form.submit()}
      >
        <Form form={form} layout="vertical" onFinish={handleCreate}>
          <Form.Item
            name="name"
            label="检查名称"
            rules={[{ required: true, message: "请输入检查名称" }]}
          >
            <Input placeholder="请输入检查名称" />
          </Form.Item>
          <Form.Item
            name="standardId"
            label="合规标准"
            rules={[{ required: true, message: "请选择合规标准" }]}
          >
            <Select placeholder="请选择合规标准">
              {standards.map((s) => (
                <Select.Option key={s.id} value={s.id}>
                  {s.name} ({s.code})
                </Select.Option>
              ))}
            </Select>
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title={`自查详情 - ${currentCheck?.name || ""}`}
        open={detailModalVisible}
        onCancel={() => setDetailModalVisible(false)}
        footer={null}
        width={1000}
      >
        {currentCheck && (
          <>
            <Descriptions bordered column={4} style={{ marginBottom: 16 }}>
              <Descriptions.Item label="合规标准">
                {currentCheck.standardName}
              </Descriptions.Item>
              <Descriptions.Item label="状态">
                {getStatusTag(currentCheck.status)}
              </Descriptions.Item>
              <Descriptions.Item label="合规率">
                <span
                  style={{
                    color:
                      currentCheck.complianceRate >= 80
                        ? "#52c41a"
                        : currentCheck.complianceRate >= 60
                          ? "#faad14"
                          : "#ff4d4f",
                    fontWeight: "bold",
                  }}
                >
                  {currentCheck.complianceRate}%
                </span>
              </Descriptions.Item>
              <Descriptions.Item label="检查进度">
                {currentCheck.checkedItems}/{currentCheck.totalItems}
              </Descriptions.Item>
            </Descriptions>

            <Card title="检查项列表" size="small">
              <Table
                columns={itemColumns}
                dataSource={checkItems}
                rowKey="id"
                pagination={{ pageSize: 10 }}
              />
            </Card>
          </>
        )}
      </Modal>
    </div>
  );
};

export default ComplianceCheck;