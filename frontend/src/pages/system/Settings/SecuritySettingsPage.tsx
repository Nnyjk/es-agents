import React, { useState } from "react";
import {
  Form,
  InputNumber,
  Switch,
  Button,
  message,
  Card,
  Row,
  Col,
  Table,
  Modal,
  Form as AntForm,
  Input,
  Popconfirm,
  Divider,
  Alert,
} from "antd";
import {
  SaveOutlined,
  PlusOutlined,
  DeleteOutlined,
  SafetyOutlined,
  LockOutlined,
  ClockCircleOutlined,
  GlobalOutlined,
} from "@ant-design/icons";
import type { ColumnsType } from "antd/es/table";
import type {
  SecuritySettings,
  IpAccessRule,
  IpAccessRuleRequest,
} from "../../../types/settings";
import {
  updateSecuritySettings,
  getIpWhitelist,
  getIpBlacklist,
  addIpAccessRule,
  deleteIpAccessRule,
} from "../../../services/settings";

interface SecuritySettingsPageProps {
  data?: SecuritySettings;
  onUpdate: () => void;
}

const SecuritySettingsPage: React.FC<SecuritySettingsPageProps> = ({
  data,
  onUpdate,
}) => {
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);
  const [whitelist, setWhitelist] = useState<IpAccessRule[]>([]);
  const [blacklist, setBlacklist] = useState<IpAccessRule[]>([]);
  const [ipModalVisible, setIpModalVisible] = useState(false);
  const [ipModalType, setIpModalType] = useState<"ALLOW" | "DENY">("ALLOW");
  const [ipForm] = AntForm.useForm();

  React.useEffect(() => {
    if (data) {
      form.setFieldsValue({
        passwordPolicy: data.passwordPolicy,
        sessionConfig: data.sessionConfig,
        twoFactorEnabled: data.twoFactorEnabled,
        ipWhitelistEnabled: data.ipWhitelistEnabled,
        ipBlacklistEnabled: data.ipBlacklistEnabled,
      });
    }
    loadIpRules();
  }, [data, form]);

  const loadIpRules = async () => {
    try {
      const [whitelistData, blacklistData] = await Promise.all([
        getIpWhitelist(),
        getIpBlacklist(),
      ]);
      setWhitelist(whitelistData);
      setBlacklist(blacklistData);
    } catch {
      // 加载失败时使用空数组
    }
  };

  const handleSubmit = async (values: SecuritySettings) => {
    setLoading(true);
    try {
      await updateSecuritySettings(values);
      message.success("安全设置保存成功");
      onUpdate();
    } catch {
      message.error("保存失败，请重试");
    } finally {
      setLoading(false);
    }
  };

  const handleAddIpRule = async (values: IpAccessRuleRequest) => {
    try {
      await addIpAccessRule({ ...values, type: ipModalType });
      message.success("添加成功");
      setIpModalVisible(false);
      ipForm.resetFields();
      loadIpRules();
    } catch {
      message.error("添加失败");
    }
  };

  const handleDeleteIpRule = async (id: number) => {
    try {
      await deleteIpAccessRule(id);
      message.success("删除成功");
      loadIpRules();
    } catch {
      message.error("删除失败");
    }
  };

  const ipColumns: ColumnsType<IpAccessRule> = [
    {
      title: "IP范围",
      dataIndex: "ipRange",
      key: "ipRange",
      width: 200,
    },
    {
      title: "描述",
      dataIndex: "description",
      key: "description",
      ellipsis: true,
    },
    {
      title: "创建时间",
      dataIndex: "createdAt",
      key: "createdAt",
      width: 180,
    },
    {
      title: "操作",
      key: "action",
      width: 80,
      render: (_, record) => (
        <Popconfirm
          title="确定删除此规则？"
          onConfirm={() => handleDeleteIpRule(record.id)}
          okText="确定"
          cancelText="取消"
        >
          <Button type="link" danger icon={<DeleteOutlined />} />
        </Popconfirm>
      ),
    },
  ];

  return (
    <Form form={form} layout="vertical" onFinish={handleSubmit}>
      <Row gutter={24}>
        <Col span={24}>
          <Card
            title={
              <span>
                <LockOutlined style={{ marginRight: 8 }} />
                密码策略配置
              </span>
            }
            style={{ marginBottom: 16 }}
          >
            <Row gutter={16}>
              <Col span={6}>
                <Form.Item
                  name={["passwordPolicy", "minLength"]}
                  label="密码最小长度"
                >
                  <InputNumber min={6} max={32} style={{ width: "100%" }} />
                </Form.Item>
              </Col>
              <Col span={6}>
                <Form.Item
                  name={["passwordPolicy", "expirationDays"]}
                  label="密码过期天数"
                  extra="0表示永不过期"
                >
                  <InputNumber min={0} max={365} style={{ width: "100%" }} />
                </Form.Item>
              </Col>
              <Col span={6}>
                <Form.Item
                  name={["passwordPolicy", "maxLoginAttempts"]}
                  label="最大登录尝试次数"
                >
                  <InputNumber min={1} max={10} style={{ width: "100%" }} />
                </Form.Item>
              </Col>
              <Col span={6}>
                <Form.Item
                  name={["passwordPolicy", "lockoutDuration"]}
                  label="锁定时长(分钟)"
                >
                  <InputNumber min={1} max={1440} style={{ width: "100%" }} />
                </Form.Item>
              </Col>
            </Row>
            <Row gutter={16}>
              <Col span={6}>
                <Form.Item
                  name={["passwordPolicy", "requireUppercase"]}
                  label="需要大写字母"
                  valuePropName="checked"
                >
                  <Switch checkedChildren="是" unCheckedChildren="否" />
                </Form.Item>
              </Col>
              <Col span={6}>
                <Form.Item
                  name={["passwordPolicy", "requireLowercase"]}
                  label="需要小写字母"
                  valuePropName="checked"
                >
                  <Switch checkedChildren="是" unCheckedChildren="否" />
                </Form.Item>
              </Col>
              <Col span={6}>
                <Form.Item
                  name={["passwordPolicy", "requireNumber"]}
                  label="需要数字"
                  valuePropName="checked"
                >
                  <Switch checkedChildren="是" unCheckedChildren="否" />
                </Form.Item>
              </Col>
              <Col span={6}>
                <Form.Item
                  name={["passwordPolicy", "requireSpecialChar"]}
                  label="需要特殊字符"
                  valuePropName="checked"
                >
                  <Switch checkedChildren="是" unCheckedChildren="否" />
                </Form.Item>
              </Col>
            </Row>
          </Card>
        </Col>

        <Col span={24}>
          <Card
            title={
              <span>
                <ClockCircleOutlined style={{ marginRight: 8 }} />
                会话配置
              </span>
            }
            style={{ marginBottom: 16 }}
          >
            <Row gutter={16}>
              <Col span={8}>
                <Form.Item
                  name={["sessionConfig", "sessionTimeout"]}
                  label="会话有效期(分钟)"
                >
                  <InputNumber min={5} max={1440} style={{ width: "100%" }} />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item
                  name={["sessionConfig", "idleTimeout"]}
                  label="闲置超时时间(分钟)"
                >
                  <InputNumber min={5} max={480} style={{ width: "100%" }} />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item
                  name={["sessionConfig", "maxConcurrentSessions"]}
                  label="最大并发会话数"
                >
                  <InputNumber min={1} max={10} style={{ width: "100%" }} />
                </Form.Item>
              </Col>
            </Row>
          </Card>
        </Col>

        <Col span={24}>
          <Card
            title={
              <span>
                <SafetyOutlined style={{ marginRight: 8 }} />
                其他安全设置
              </span>
            }
            style={{ marginBottom: 16 }}
          >
            <Row gutter={16}>
              <Col span={8}>
                <Form.Item
                  name="twoFactorEnabled"
                  label="启用两步验证"
                  valuePropName="checked"
                >
                  <Switch checkedChildren="开启" unCheckedChildren="关闭" />
                </Form.Item>
              </Col>
            </Row>
          </Card>
        </Col>

        <Col span={24}>
          <Card
            title={
              <span>
                <GlobalOutlined style={{ marginRight: 8 }} />
                IP访问控制
              </span>
            }
            style={{ marginBottom: 16 }}
          >
            <Alert
              message="启用IP访问控制后，只有在白名单中的IP地址才能访问系统，或在黑名单中的IP地址将被禁止访问"
              type="info"
              showIcon
              style={{ marginBottom: 16 }}
            />
            <Row gutter={16} style={{ marginBottom: 16 }}>
              <Col span={6}>
                <Form.Item
                  name="ipWhitelistEnabled"
                  label="启用IP白名单"
                  valuePropName="checked"
                >
                  <Switch checkedChildren="开启" unCheckedChildren="关闭" />
                </Form.Item>
              </Col>
              <Col span={6}>
                <Form.Item
                  name="ipBlacklistEnabled"
                  label="启用IP黑名单"
                  valuePropName="checked"
                >
                  <Switch checkedChildren="开启" unCheckedChildren="关闭" />
                </Form.Item>
              </Col>
            </Row>

            <Divider orientation="left">IP白名单</Divider>
            <div style={{ marginBottom: 16 }}>
              <Button
                type="dashed"
                icon={<PlusOutlined />}
                onClick={() => {
                  setIpModalType("ALLOW");
                  setIpModalVisible(true);
                }}
              >
                添加白名单规则
              </Button>
            </div>
            <Table
              columns={ipColumns}
              dataSource={whitelist}
              rowKey="id"
              pagination={false}
              size="small"
              style={{ marginBottom: 24 }}
            />

            <Divider orientation="left">IP黑名单</Divider>
            <div style={{ marginBottom: 16 }}>
              <Button
                type="dashed"
                danger
                icon={<PlusOutlined />}
                onClick={() => {
                  setIpModalType("DENY");
                  setIpModalVisible(true);
                }}
              >
                添加黑名单规则
              </Button>
            </div>
            <Table
              columns={ipColumns}
              dataSource={blacklist}
              rowKey="id"
              pagination={false}
              size="small"
            />
          </Card>
        </Col>
      </Row>

      <Form.Item>
        <Button
          type="primary"
          htmlType="submit"
          icon={<SaveOutlined />}
          loading={loading}
        >
          保存设置
        </Button>
      </Form.Item>

      <Modal
        title={ipModalType === "ALLOW" ? "添加IP白名单" : "添加IP黑名单"}
        open={ipModalVisible}
        onOk={() => ipForm.submit()}
        onCancel={() => {
          setIpModalVisible(false);
          ipForm.resetFields();
        }}
        destroyOnClose
      >
        <AntForm form={ipForm} layout="vertical" onFinish={handleAddIpRule}>
          <AntForm.Item
            name="ipRange"
            label="IP范围"
            rules={[
              { required: true, message: "请输入IP范围" },
              {
                pattern: /^(\d{1,3}\.){3}\d{1,3}(\/\d{1,2})?$/,
                message:
                  "请输入有效的IP地址或CIDR格式，如192.168.1.1或192.168.1.0/24",
              },
            ]}
          >
            <Input placeholder="如：192.168.1.1 或 192.168.1.0/24" />
          </AntForm.Item>
          <AntForm.Item name="description" label="描述">
            <Input.TextArea rows={2} placeholder="可选的描述信息" />
          </AntForm.Item>
        </AntForm>
      </Modal>
    </Form>
  );
};

export default SecuritySettingsPage;
