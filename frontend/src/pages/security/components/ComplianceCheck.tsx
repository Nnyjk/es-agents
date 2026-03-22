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
  Badge,
  List,
  Checkbox,
  Collapse,
} from "antd";
import {
  PlusOutlined,
  EyeOutlined,
  ReloadOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
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
  submitCheckItem,
  generateGapReport,
} from "../../../services/security";
import type {
  ComplianceSelfCheck,
  ComplianceCheckItem,
  ComplianceGapReport,
} from "../../../types/security";

const { TextArea } = Input;
const { Panel } = Collapse;

const ComplianceCheck: React.FC = () => {
  const [checks, setChecks] = useState<ComplianceSelfCheck[]>([]);
  const [checkItems, setCheckItems] = useState<ComplianceCheckItem[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [detailModalVisible, setDetailModalVisible] = useState(false);
  const [checkModalVisible, setCheckModalVisible] = useState(false);
  const [gapReport, setGapReport] = useState<ComplianceGapReport | null>(null);
  const [currentCheck, setCurrentCheck] = useState<ComplianceSelfCheck | null>(
    null,
  );
  const [currentItem, setCurrentItem] = useState<ComplianceCheckItem | null>(
    null,
  );
  const [form] = Form.useForm();
  const [checkForm] = Form.useForm();
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 10,
    total: 0,
  });

  useEffect(() => {
    fetchChecks();
    fetchCheckItems();
  }, []);

  const fetchChecks = async (page = 1, pageSize = 10) => {
    try {
      setLoading(true);
      const data = await getComplianceSelfChecks({ page, pageSize });
      setChecks(data.list);
      setPagination((prev) => ({
        ...prev,
        current: page,
        pageSize,
        total: data.total,
      }));
    } catch {
      setChecks([
        {
          id: "1",
          name: "等保2.0三级合规自查",
          level: "level3",
          status: "in_progress",
          totalItems: 85,
          checkedItems: 60,
          compliantItems: 50,
          nonCompliantItems: 8,
          notApplicableItems: 2,
          complianceRate: 83.3,
          createdAt: "2024-01-10T08:00:00Z",
          updatedAt: "2024-01-15T10:30:00Z",
          createdBy: "user1",
          createdByName: "张三",
        },
      ]);
    } finally {
      setLoading(false);
    }
  };

  const fetchCheckItems = async () => {
    try {
      const data = await getComplianceCheckItems({ level: "level3" });
      setCheckItems(data);
    } catch {
      setCheckItems([
        {
          id: "1",
          category: "身份鉴别",
          subCategory: "密码复杂度",
          name: "密码长度不少于8位",
          level: "level3",
          description: "系统应配置密码复杂度策略，要求密码长度不少于8位",
          requirement: "GB/T 22239-2019 8.1.4.1",
          checkMethod: "检查系统密码策略配置",
          status: "pending",
        },
        {
          id: "2",
          category: "身份鉴别",
          subCategory: "登录失败处理",
          name: "登录失败锁定机制",
          level: "level3",
          description: "系统应配置登录失败处理功能，连续失败5次锁定账户",
          requirement: "GB/T 22239-2019 8.1.4.2",
          checkMethod: "检查登录失败处理配置",
          status: "pending",
        },
        {
          id: "3",
          category: "访问控制",
          subCategory: "权限管理",
          name: "最小权限原则",
          level: "level3",
          description: "应按照最小权限原则分配用户权限",
          requirement: "GB/T 22239-2019 8.1.5.1",
          checkMethod: "检查用户权限分配情况",
          status: "pending",
        },
      ]);
    }
  };

  const handleCreate = async (values: {
    name: string;
    description?: string;
    level: string;
  }) => {
    try {
      await createComplianceSelfCheck({
        name: values.name,
        description: values.description,
        level: values.level as "level2" | "level3" | "level4",
      });
      message.success("创建成功");
      setModalVisible(false);
      form.resetFields();
      fetchChecks(pagination.current, pagination.pageSize);
    } catch {
      message.error("创建失败");
    }
  };

  const handleViewDetail = (record: ComplianceSelfCheck) => {
    setCurrentCheck(record);
    setDetailModalVisible(true);
  };

  const handleStartCheck = (item: ComplianceCheckItem) => {
    setCurrentItem(item);
    checkForm.resetFields();
    setCheckModalVisible(true);
  };

  const handleSubmitCheck = async (values: {
    isCompliant: boolean;
    evidence?: string;
    notes?: string;
  }) => {
    if (!currentCheck || !currentItem) return;
    try {
      await submitCheckItem({
        checkId: currentCheck.id,
        itemId: currentItem.id,
        isCompliant: values.isCompliant,
        evidence: values.evidence,
        notes: values.notes,
      });
      message.success("提交成功");
      setCheckModalVisible(false);
      fetchChecks(pagination.current, pagination.pageSize);
    } catch {
      message.error("提交失败");
    }
  };

  const handleGenerateReport = async (checkId: string) => {
    try {
      const report = await generateGapReport(checkId);
      setGapReport(report);
    } catch {
      setGapReport({
        checkId: checkId,
        checkName: "等保2.0三级合规自查",
        generatedAt: new Date().toISOString(),
        summary: {
          totalItems: 85,
          compliantItems: 50,
          nonCompliantItems: 8,
          notApplicableItems: 2,
          complianceRate: 83.3,
        },
        gaps: [
          {
            itemId: "1",
            category: "身份鉴别",
            name: "密码长度不足",
            requirement: "密码长度不少于8位",
            currentValue: "当前配置为6位",
            expectedValue: "应配置为8位或以上",
            severity: "medium",
            remediation: "修改密码策略配置，设置最小密码长度为8位",
          },
        ],
        recommendations: [
          "建议立即修复身份鉴别相关的不合规项",
          "建议定期进行密码策略审计",
        ],
      });
    }
  };

  const getStatusTag = (status: string) => {
    const map: Record<string, { color: string; text: string }> = {
      pending: { color: "default", text: "待检查" },
      in_progress: { color: "processing", text: "检查中" },
      completed: { color: "success", text: "已完成" },
      cancelled: { color: "warning", text: "已取消" },
    };
    const item = map[status] || { color: "default", text: status };
    return <Tag color={item.color}>{item.text}</Tag>;
  };

  const getComplianceTag = (status: string) => {
    const map: Record<string, { color: string; text: string }> = {
      compliant: { color: "success", text: "合规" },
      "non-compliant": { color: "error", text: "不合规" },
      "not-applicable": { color: "default", text: "不适用" },
      pending: { color: "default", text: "待检查" },
    };
    const item = map[status] || { color: "default", text: status };
    return <Tag color={item.color}>{item.text}</Tag>;
  };

  const columns: ColumnsType<ComplianceSelfCheck> = [
    {
      title: "检查名称",
      dataIndex: "name",
      key: "name",
      width: 200,
    },
    {
      title: "等保级别",
      dataIndex: "level",
      key: "level",
      width: 100,
      render: (level: string) => {
        const map: Record<string, string> = {
          level2: "二级",
          level3: "三级",
          level4: "四级",
        };
        return <Tag color="blue">{map[level] || level}</Tag>;
      },
    },
    {
      title: "进度",
      key: "progress",
      width: 200,
      render: (_: unknown, record: ComplianceSelfCheck) => (
        <Progress
          percent={Math.round(
            (record.checkedItems / record.totalItems) * 100,
          )}
          size="small"
          status={
            record.checkedItems === record.totalItems ? "success" : "active"
          }
        />
      ),
    },
    {
      title: "合规率",
      dataIndex: "complianceRate",
      key: "complianceRate",
      width: 120,
      render: (rate: number) => (
        <span
          style={{
            color:
              rate >= 80 ? "#52c41a" : rate >= 60 ? "#faad14" : "#ff4d4f",
          }}
        >
          {rate ? `${rate.toFixed(1)}%` : "-"}
        </span>
      ),
    },
    {
      title: "状态",
      dataIndex: "status",
      key: "status",
      width: 100,
      render: (status: string) => getStatusTag(status),
    },
    {
      title: "创建时间",
      dataIndex: "createdAt",
      key: "createdAt",
      width: 160,
      render: (time: string) => dayjs(time).format("YYYY-MM-DD HH:mm"),
    },
    {
      title: "操作",
      key: "action",
      width: 200,
      render: (_: unknown, record: ComplianceSelfCheck) => (
        <Space size="small">
          <Tooltip title="查看详情">
            <Button
              type="link"
              size="small"
              icon={<EyeOutlined />}
              onClick={() => handleViewDetail(record)}
            />
          </Tooltip>
          <Tooltip title="差距报告">
            <Button
              type="link"
              size="small"
              icon={<FileTextOutlined />}
              onClick={() => handleGenerateReport(record.id)}
            />
          </Tooltip>
          <Tooltip title="下载报告">
            <Button
              type="link"
              size="small"
              icon={<DownloadOutlined />}
              disabled={!gapReport}
            />
          </Tooltip>
        </Space>
      ),
    },
  ];

  const itemColumns: ColumnsType<ComplianceCheckItem> = [
    {
      title: "分类",
      dataIndex: "category",
      key: "category",
      width: 120,
    },
    {
      title: "子分类",
      dataIndex: "subCategory",
      key: "subCategory",
      width: 120,
    },
    {
      title: "检查项名称",
      dataIndex: "name",
      key: "name",
      width: 200,
    },
    {
      title: "等级要求",
      dataIndex: "level",
      key: "level",
      width: 80,
      render: (level: string) => {
        const map: Record<string, string> = {
          level2: "二级",
          level3: "三级",
          level4: "四级",
        };
        return map[level] || level;
      },
    },
    {
      title: "状态",
      dataIndex: "status",
      key: "status",
      width: 100,
      render: (status: string) => getComplianceTag(status),
    },
    {
      title: "操作",
      key: "action",
      width: 100,
      render: (_: unknown, record: ComplianceCheckItem) => (
        <Button
          type="link"
          size="small"
          onClick={() => handleStartCheck(record)}
          disabled={record.status !== "pending"}
        >
          检查
        </Button>
      ),
    },
  ];

  const getComplianceData = () => {
    const categories = [
      { name: "身份鉴别", rate: 85 },
      { name: "访问控制", rate: 90 },
      { name: "安全审计", rate: 78 },
      { name: "入侵防范", rate: 92 },
      { name: "恶意代码防范", rate: 88 },
    ];
    return categories;
  };

  return (
    <div>
      <Tabs
        items={[
          {
            key: "checks",
            label: "自查任务",
            icon: <CheckCircleOutlined />,
            children: (
              <Card
                title="合规自查任务"
                extra={
                  <Space>
                    <Button
                      icon={<ReloadOutlined />}
                      onClick={() => fetchChecks()}
                    >
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
                <Table
                  columns={columns}
                  dataSource={checks}
                  rowKey="id"
                  loading={loading}
                  pagination={{
                    ...pagination,
                    showSizeChanger: true,
                    showQuickJumper: true,
                    onChange: (page, pageSize) => fetchChecks(page, pageSize),
                  }}
                />
              </Card>
            ),
          },
          {
            key: "items",
            label: "检查项",
            icon: <CheckOutlined />,
            children: (
              <Card title="等保2.0检查项">
                <Collapse accordion>
                  {["身份鉴别", "访问控制", "安全审计", "入侵防范"].map(
                    (category) => (
                      <Panel header={category} key={category}>
                        <Table
                          columns={itemColumns.filter(
                            (col) => col.key !== "category",
                          )}
                          dataSource={checkItems.filter(
                            (item) => item.category === category,
                          )}
                          rowKey="id"
                          pagination={false}
                          size="small"
                        />
                      </Panel>
                    ),
                  )}
                </Collapse>
              </Card>
            ),
          },
          {
            key: "analysis",
            label: "差距分析",
            icon: <FileTextOutlined />,
            children: (
              <div>
                <Row gutter={16} style={{ marginBottom: 16 }}>
                  <Col span={12}>
                    <Card title="合规率分布">
                      <List
                        dataSource={getComplianceData()}
                        renderItem={(item) => (
                          <List.Item>
                            <div
                              style={{
                                width: "100%",
                                display: "flex",
                                alignItems: "center",
                              }}
                            >
                              <span style={{ width: 120 }}>{item.name}</span>
                              <Progress
                                percent={item.rate}
                                strokeColor={
                                  item.rate >= 80
                                    ? "#52c41a"
                                    : item.rate >= 60
                                      ? "#faad14"
                                      : "#ff4d4f"
                                }
                                style={{ flex: 1 }}
                              />
                            </div>
                          </List.Item>
                        )}
                      />
                    </Card>
                  </Col>
                  <Col span={12}>
                    <Card title="整改优先级">
                      <List
                        dataSource={[
                          {
                            priority: "高",
                            count: 3,
                            items: ["密码策略配置", "登录失败处理", "权限最小化"],
                          },
                          {
                            priority: "中",
                            count: 5,
                            items: ["审计日志保存", "会话超时设置"],
                          },
                        ]}
                        renderItem={(item) => (
                          <List.Item>
                            <List.Item.Meta
                              avatar={
                                <Tag
                                  color={
                                    item.priority === "高" ? "red" : "orange"
                                  }
                                >
                                  {item.priority}优先级
                                </Tag>
                              }
                              title={`${item.count} 个待整改项`}
                              description={item.items.join("、")}
                            />
                          </List.Item>
                        )}
                      />
                    </Card>
                  </Col>
                </Row>

                {gapReport && (
                  <Card title="合规差距报告">
                    <Descriptions bordered column={2}>
                      <Descriptions.Item label="总检查项">
                        {gapReport.summary.totalItems}
                      </Descriptions.Item>
                      <Descriptions.Item label="合规项">
                        <Badge
                          status="success"
                          text={gapReport.summary.compliantItems}
                        />
                      </Descriptions.Item>
                      <Descriptions.Item label="不合规项">
                        <Badge
                          status="error"
                          text={gapReport.summary.nonCompliantItems}
                        />
                      </Descriptions.Item>
                      <Descriptions.Item label="合规率">
                        <span
                          style={{
                            color:
                              gapReport.summary.complianceRate >= 80
                                ? "#52c41a"
                                : gapReport.summary.complianceRate >= 60
                                  ? "#faad14"
                                  : "#ff4d4f",
                          }}
                        >
                          {gapReport.summary.complianceRate.toFixed(1)}%
                        </span>
                      </Descriptions.Item>
                    </Descriptions>
                  </Card>
                )}
              </div>
            ),
          },
        ]}
      />

      <Modal
        title="新建合规自查"
        open={modalVisible}
        onCancel={() => setModalVisible(false)}
        onOk={() => form.submit()}
        width={500}
      >
        <Form form={form} layout="vertical" onFinish={handleCreate}>
          <Form.Item
            name="name"
            label="自查名称"
            rules={[{ required: true, message: "请输入自查名称" }]}
          >
            <Input placeholder="请输入自查名称" />
          </Form.Item>
          <Form.Item name="description" label="描述">
            <TextArea rows={3} placeholder="请输入描述" />
          </Form.Item>
          <Form.Item
            name="level"
            label="等保级别"
            rules={[{ required: true, message: "请选择等保级别" }]}
          >
            <Select placeholder="请选择等保级别">
              <Select.Option value="level2">等保二级</Select.Option>
              <Select.Option value="level3">等保三级</Select.Option>
              <Select.Option value="level4">等保四级</Select.Option>
            </Select>
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title="自查详情"
        open={detailModalVisible}
        onCancel={() => setDetailModalVisible(false)}
        footer={null}
        width={800}
      >
        {currentCheck && (
          <div>
            <Descriptions bordered column={2} style={{ marginBottom: 16 }}>
              <Descriptions.Item label="检查名称">
                {currentCheck.name}
              </Descriptions.Item>
              <Descriptions.Item label="等保级别">
                <Tag color="blue">
                  {currentCheck.level === "level3"
                    ? "三级"
                    : currentCheck.level === "level2"
                      ? "二级"
                      : "四级"}
                </Tag>
              </Descriptions.Item>
              <Descriptions.Item label="总检查项">
                {currentCheck.totalItems}
              </Descriptions.Item>
              <Descriptions.Item label="已检查项">
                {currentCheck.checkedItems}
              </Descriptions.Item>
              <Descriptions.Item label="合规项">
                <Badge
                  status="success"
                  text={currentCheck.compliantItems}
                />
              </Descriptions.Item>
              <Descriptions.Item label="不合规项">
                <Badge
                  status="error"
                  text={currentCheck.nonCompliantItems}
                />
              </Descriptions.Item>
              <Descriptions.Item label="合规率" span={2}>
                <Progress
                  percent={currentCheck.complianceRate}
                  status={
                    currentCheck.complianceRate >= 80
                      ? "success"
                      : currentCheck.complianceRate >= 60
                        ? "normal"
                        : "exception"
                  }
                />
              </Descriptions.Item>
            </Descriptions>
          </div>
        )}
      </Modal>

      <Modal
        title="检查项确认"
        open={checkModalVisible}
        onCancel={() => setCheckModalVisible(false)}
        onOk={() => checkForm.submit()}
        width={600}
      >
        {currentItem && (
          <Descriptions bordered column={1} style={{ marginBottom: 16 }}>
            <Descriptions.Item label="检查项">
              {currentItem.name}
            </Descriptions.Item>
            <Descriptions.Item label="分类">
              {currentItem.category} - {currentItem.subCategory}
            </Descriptions.Item>
            <Descriptions.Item label="要求">
              {currentItem.description}
            </Descriptions.Item>
            <Descriptions.Item label="检查方法">
              {currentItem.checkMethod}
            </Descriptions.Item>
            <Descriptions.Item label="标准依据">
              {currentItem.requirement}
            </Descriptions.Item>
          </Descriptions>
        )}
        <Form
          form={checkForm}
          layout="vertical"
          onFinish={handleSubmitCheck}
        >
          <Form.Item
            name="isCompliant"
            label="检查结果"
            rules={[{ required: true, message: "请选择检查结果" }]}
          >
            <Select placeholder="请选择检查结果">
              <Select.Option value={true}>合规</Select.Option>
              <Select.Option value={false}>不合规</Select.Option>
            </Select>
          </Form.Item>
          <Form.Item name="evidence" label="佐证材料">
            <TextArea rows={3} placeholder="请输入佐证材料说明或上传证明文件" />
          </Form.Item>
          <Form.Item name="notes" label="备注">
            <TextArea rows={2} placeholder="请输入备注" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default ComplianceCheck;