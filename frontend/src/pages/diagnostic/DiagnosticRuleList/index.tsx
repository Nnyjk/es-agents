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
  Select,
  Switch,
  Badge,
  message,
} from "antd";
import {
  ReloadOutlined,
  PlusOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
} from "@ant-design/icons";
import type { ColumnsType } from "antd/es/table";
import { PageContainer } from "@ant-design/pro-components";
import {
  getDiagnosticRules,
  getDiagnosticRule,
  createDiagnosticRule,
  updateDiagnosticRule,
  enableDiagnosticRule,
  disableDiagnosticRule,
  deleteDiagnosticRule,
  type DiagnosticRuleSummary,
  type DiagnosticRule,
  type DiagnosticCategory,
  type FindingSeverity,
} from "@/services/diagnostic";

const { Option } = Select;
const { TextArea } = Input;

const categoryConfig: Record<DiagnosticCategory, { color: string; text: string }> = {
  SYSTEM: { color: "blue", text: "系统" },
  PERFORMANCE: { color: "purple", text: "性能" },
  SECURITY: { color: "red", text: "安全" },
  BUSINESS: { color: "green", text: "业务" },
  ALERT: { color: "orange", text: "告警" },
};

const severityConfig: Record<FindingSeverity, { color: "default" | "processing" | "success" | "warning" | "error"; text: string }> = {
  INFO: { color: "default", text: "信息" },
  WARNING: { color: "warning", text: "警告" },
  CRITICAL: { color: "error", text: "严重" },
  FATAL: { color: "error", text: "致命" },
};

const DiagnosticRuleList: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [rules, setRules] = useState<DiagnosticRuleSummary[]>([]);
  const [modalVisible, setModalVisible] = useState(false);
  const [editingRule, setEditingRule] = useState<DiagnosticRule | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [form] = Form.useForm();

  const fetchRules = async () => {
    setLoading(true);
    try {
      const data = await getDiagnosticRules();
      setRules(data);
    } catch (error) {
      message.error("获取诊断规则列表失败");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchRules();
  }, []);

  const handleCreate = () => {
    setEditingRule(null);
    form.resetFields();
    form.setFieldsValue({ enabled: true });
    setModalVisible(true);
  };

  const handleEdit = async (ruleId: string) => {
    setLoading(true);
    try {
      const rule = await getDiagnosticRule(ruleId);
      setEditingRule(rule);
      form.setFieldsValue(rule);
      setModalVisible(true);
    } catch (error) {
      message.error("获取规则详情失败");
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (values: any) => {
    setSubmitting(true);
    try {
      if (editingRule) {
        await updateDiagnosticRule(editingRule.ruleId, values);
        message.success("更新成功");
      } else {
        await createDiagnosticRule(values);
        message.success("创建成功");
      }
      setModalVisible(false);
      fetchRules();
    } catch (error) {
      message.error(editingRule ? "更新失败" : "创建失败");
    } finally {
      setSubmitting(false);
    }
  };

  const handleToggleEnabled = async (ruleId: string, enabled: boolean) => {
    try {
      if (enabled) {
        await enableDiagnosticRule(ruleId);
        message.success("已启用");
      } else {
        await disableDiagnosticRule(ruleId);
        message.success("已禁用");
      }
      fetchRules();
    } catch (error) {
      message.error("操作失败");
    }
  };

  const handleDelete = (ruleId: string) => {
    Modal.confirm({
      title: "确认删除",
      content: "确定要删除该诊断规则吗？",
      onOk: async () => {
        try {
          await deleteDiagnosticRule(ruleId);
          message.success("删除成功");
          fetchRules();
        } catch (error) {
          message.error("删除失败");
        }
      },
    });
  };

  const columns: ColumnsType<DiagnosticRuleSummary> = [
    {
      title: "规则ID",
      dataIndex: "ruleId",
      key: "ruleId",
      width: 120,
    },
    {
      title: "规则名称",
      dataIndex: "name",
      key: "name",
      ellipsis: true,
    },
    {
      title: "分类",
      dataIndex: "category",
      key: "category",
      width: 100,
      render: (category: DiagnosticCategory) => {
        const config = categoryConfig[category];
        return <Tag color={config.color}>{config.text}</Tag>;
      },
    },
    {
      title: "严重程度",
      dataIndex: "severity",
      key: "severity",
      width: 100,
      render: (severity: FindingSeverity) => {
        const config = severityConfig[severity];
        return <Badge status={config.color} text={config.text} />;
      },
    },
    {
      title: "状态",
      dataIndex: "enabled",
      key: "enabled",
      width: 80,
      render: (enabled: boolean, record) => (
        <Switch
          checked={enabled}
          onChange={(checked) => handleToggleEnabled(record.ruleId, checked)}
          checkedChildren={<CheckCircleOutlined />}
          unCheckedChildren={<CloseCircleOutlined />}
        />
      ),
    },
    {
      title: "操作",
      key: "action",
      width: 150,
      render: (_, record) => (
        <Space>
          <Button type="link" size="small" onClick={() => handleEdit(record.ruleId)}>
            编辑
          </Button>
          <Button type="link" size="small" danger onClick={() => handleDelete(record.ruleId)}>
            删除
          </Button>
        </Space>
      ),
    },
  ];

  return (
    <PageContainer>
      <Card>
        <div style={{ marginBottom: 16 }}>
          <Space>
            <Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>
              新建规则
            </Button>
            <Button icon={<ReloadOutlined />} onClick={fetchRules}>
              刷新
            </Button>
          </Space>
        </div>

        <Table
          columns={columns}
          dataSource={rules}
          rowKey="ruleId"
          loading={loading}
          pagination={{ pageSize: 20 }}
        />
      </Card>

      <Modal
        title={editingRule ? "编辑规则" : "新建规则"}
        open={modalVisible}
        onCancel={() => setModalVisible(false)}
        footer={null}
        width={600}
      >
        <Form form={form} layout="vertical" onFinish={handleSubmit}>
          <Form.Item
            name="name"
            label="规则名称"
            rules={[{ required: true, message: "请输入规则名称" }]}
          >
            <Input placeholder="请输入规则名称" />
          </Form.Item>
          <Form.Item name="description" label="描述">
            <TextArea rows={2} placeholder="请输入规则描述" />
          </Form.Item>
          <Form.Item
            name="category"
            label="分类"
            rules={[{ required: true, message: "请选择分类" }]}
          >
            <Select placeholder="请选择分类">
              {Object.entries(categoryConfig).map(([key, value]) => (
                <Option key={key} value={key}>
                  {value.text}
                </Option>
              ))}
            </Select>
          </Form.Item>
          <Form.Item
            name="condition"
            label="条件表达式"
            rules={[{ required: true, message: "请输入条件表达式" }]}
            extra="格式: metric > threshold，例如: system.cpu.usage > 80"
          >
            <Input placeholder="例如: system.cpu.usage > 80" />
          </Form.Item>
          <Form.Item
            name="severity"
            label="严重程度"
            rules={[{ required: true, message: "请选择严重程度" }]}
          >
            <Select placeholder="请选择严重程度">
              {Object.entries(severityConfig).map(([key, value]) => (
                <Option key={key} value={key}>
                  {value.text}
                </Option>
              ))}
            </Select>
          </Form.Item>
          <Form.Item name="recommendation" label="修复建议">
            <TextArea rows={3} placeholder="请输入修复建议" />
          </Form.Item>
          <Form.Item name="enabled" label="启用状态" valuePropName="checked">
            <Switch checkedChildren="启用" unCheckedChildren="禁用" />
          </Form.Item>
          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit" loading={submitting}>
                {editingRule ? "更新" : "创建"}
              </Button>
              <Button onClick={() => setModalVisible(false)}>取消</Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>
    </PageContainer>
  );
};

export default DiagnosticRuleList;
