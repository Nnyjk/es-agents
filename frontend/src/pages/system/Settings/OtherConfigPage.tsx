import React, { useState } from "react";
import {
  Form,
  Input,
  InputNumber,
  Switch,
  Button,
  Space,
  message,
  Card,
  Row,
  Col,
  Select,
  Divider,
  Modal,
  Alert,
  Tabs,
} from "antd";
import {
  SaveOutlined,
  MailOutlined,
  CloudServerOutlined,
  FunctionOutlined,
  FileTextOutlined,
  TestOutlined,
} from "@ant-design/icons";
import type {
  EmailConfig,
  StorageGlobalConfig,
  FeatureFlags,
  LogConfig,
} from "../../../types/settings";
import {
  updateEmailConfig,
  updateStorageConfig,
  updateFeatureFlags,
  updateLogConfig,
  testEmailConnection,
  testStorageConnection,
} from "../../../services/settings";

interface OtherConfigPageProps {
  emailData?: EmailConfig;
  storageData?: StorageGlobalConfig;
  featureData?: FeatureFlags;
  logData?: LogConfig;
  onUpdate: () => void;
}

const OtherConfigPage: React.FC<OtherConfigPageProps> = ({
  emailData,
  storageData,
  featureData,
  logData,
  onUpdate,
}) => {
  const [emailForm] = Form.useForm();
  const [storageForm] = Form.useForm();
  const [featureForm] = Form.useForm();
  const [logForm] = Form.useForm();
  const [emailLoading, setEmailLoading] = useState(false);
  const [storageLoading, setStorageLoading] = useState(false);
  const [featureLoading, setFeatureLoading] = useState(false);
  const [logLoading, setLogLoading] = useState(false);
  const [testEmailLoading, setTestEmailLoading] = useState(false);
  const [testStorageLoading, setTestStorageLoading] = useState(false);
  const [testEmailModalVisible, setTestEmailModalVisible] = useState(false);
  const [testEmailAddress, setTestEmailAddress] = useState("");

  React.useEffect(() => {
    if (emailData) {
      emailForm.setFieldsValue(emailData);
    }
    if (storageData) {
      storageForm.setFieldsValue(storageData);
    }
    if (featureData) {
      featureForm.setFieldsValue(featureData);
    }
    if (logData) {
      logForm.setFieldsValue(logData);
    }
  }, [emailData, storageData, featureData, logData, emailForm, storageForm, featureForm, logForm]);

  const handleEmailSubmit = async (values: EmailConfig) => {
    setEmailLoading(true);
    try {
      await updateEmailConfig(values);
      message.success("邮件配置保存成功");
      onUpdate();
    } catch {
      message.error("保存失败，请重试");
    } finally {
      setEmailLoading(false);
    }
  };

  const handleStorageSubmit = async (values: StorageGlobalConfig) => {
    setStorageLoading(true);
    try {
      await updateStorageConfig(values);
      message.success("存储配置保存成功");
      onUpdate();
    } catch {
      message.error("保存失败，请重试");
    } finally {
      setStorageLoading(false);
    }
  };

  const handleFeatureSubmit = async (values: FeatureFlags) => {
    setFeatureLoading(true);
    try {
      await updateFeatureFlags(values);
      message.success("功能开关设置保存成功");
      onUpdate();
    } catch {
      message.error("保存失败，请重试");
    } finally {
      setFeatureLoading(false);
    }
  };

  const handleLogSubmit = async (values: LogConfig) => {
    setLogLoading(true);
    try {
      await updateLogConfig(values);
      message.success("日志配置保存成功");
      onUpdate();
    } catch {
      message.error("保存失败，请重试");
    } finally {
      setLogLoading(false);
    }
  };

  const handleTestEmail = async () => {
    if (!testEmailAddress) {
      message.warning("请输入测试邮箱地址");
      return;
    }
    setTestEmailLoading(true);
    try {
      const result = await testEmailConnection(testEmailAddress);
      if (result.success) {
        message.success(result.message);
      } else {
        message.error(result.message);
      }
    } catch {
      message.error("测试失败");
    } finally {
      setTestEmailLoading(false);
    }
  };

  const handleTestStorage = async () => {
    setTestStorageLoading(true);
    try {
      const result = await testStorageConnection();
      if (result.success) {
        message.success(result.message);
      } else {
        message.error(result.message);
      }
    } catch {
      message.error("测试失败");
    } finally {
      setTestStorageLoading(false);
    }
  };

  const featureOptions = [
    { key: "agentManagement", label: "Agent管理" },
    { key: "deploymentCenter", label: "部署中心" },
    { key: "monitoringDashboard", label: "监控大盘" },
    { key: "alertCenter", label: "告警中心" },
    { key: "backupRestore", label: "备份恢复" },
    { key: "scheduledTasks", label: "定时任务" },
    { key: "apiTokens", label: "API令牌" },
    { key: "multiTenancy", label: "多租户" },
  ];

  return (
    <Tabs
      defaultActiveKey="email"
      items={[
        {
          key: "email",
          label: (
            <span>
              <MailOutlined />
              邮件服务
            </span>
          ),
          children: (
            <Card>
              <Form form={emailForm} layout="vertical" onFinish={handleEmailSubmit}>
                <Row gutter={16}>
                  <Col span={6}>
                    <Form.Item name="enabled" label="启用邮件服务" valuePropName="checked">
                      <Switch checkedChildren="开启" unCheckedChildren="关闭" />
                    </Form.Item>
                  </Col>
                </Row>
                <Row gutter={16}>
                  <Col span={12}>
                    <Form.Item
                      name="smtpHost"
                      label="SMTP服务器"
                      rules={[{ required: true, message: "请输入SMTP服务器地址" }]}
                    >
                      <Input placeholder="如：smtp.example.com" />
                    </Form.Item>
                  </Col>
                  <Col span={6}>
                    <Form.Item
                      name="smtpPort"
                      label="端口"
                      rules={[{ required: true, message: "请输入端口" }]}
                    >
                      <InputNumber min={1} max={65535} style={{ width: "100%" }} />
                    </Form.Item>
                  </Col>
                  <Col span={6}>
                    <Form.Item name="useTls" label="使用TLS" valuePropName="checked">
                      <Switch checkedChildren="是" unCheckedChildren="否" />
                    </Form.Item>
                  </Col>
                </Row>
                <Row gutter={16}>
                  <Col span={12}>
                    <Form.Item
                      name="smtpUsername"
                      label="用户名"
                      rules={[{ required: true, message: "请输入用户名" }]}
                    >
                      <Input placeholder="SMTP登录用户名" />
                    </Form.Item>
                  </Col>
                  <Col span={12}>
                    <Form.Item name="smtpPassword" label="密码">
                      <Input.Password placeholder="SMTP登录密码" />
                    </Form.Item>
                  </Col>
                </Row>
                <Row gutter={16}>
                  <Col span={12}>
                    <Form.Item
                      name="senderEmail"
                      label="发件人邮箱"
                      rules={[
                        { required: true, message: "请输入发件人邮箱" },
                        { type: "email", message: "请输入有效的邮箱地址" },
                      ]}
                    >
                      <Input placeholder="noreply@example.com" />
                    </Form.Item>
                  </Col>
                  <Col span={12}>
                    <Form.Item name="senderName" label="发件人名称">
                      <Input placeholder="系统通知" />
                    </Form.Item>
                  </Col>
                </Row>
                <Form.Item>
                  <Space>
                    <Button
                      type="primary"
                      htmlType="submit"
                      icon={<SaveOutlined />}
                      loading={emailLoading}
                    >
                      保存配置
                    </Button>
                    <Button
                      icon={<TestOutlined />}
                      onClick={() => setTestEmailModalVisible(true)}
                    >
                      测试连接
                    </Button>
                  </Space>
                </Form.Item>
              </Form>
            </Card>
          ),
        },
        {
          key: "storage",
          label: (
            <span>
              <CloudServerOutlined />
              存储服务
            </span>
          ),
          children: (
            <Card>
              <Form form={storageForm} layout="vertical" onFinish={handleStorageSubmit}>
                <Row gutter={16}>
                  <Col span={8}>
                    <Form.Item
                      name="defaultStorageType"
                      label="默认存储类型"
                      rules={[{ required: true, message: "请选择存储类型" }]}
                    >
                      <Select
                        options={[
                          { value: "LOCAL", label: "本地存储" },
                          { value: "S3", label: "AWS S3" },
                          { value: "MINIO", label: "MinIO" },
                        ]}
                      />
                    </Form.Item>
                  </Col>
                  <Col span={8}>
                    <Form.Item
                      name="maxFileSize"
                      label="最大文件大小(MB)"
                    >
                      <InputNumber min={1} max={10240} style={{ width: "100%" }} />
                    </Form.Item>
                  </Col>
                  <Col span={8}>
                    <Form.Item name="allowedFileTypes" label="允许的文件类型">
                      <Select
                        mode="tags"
                        placeholder="如：jpg,png,pdf"
                        style={{ width: "100%" }}
                      />
                    </Form.Item>
                  </Col>
                </Row>
                <Divider orientation="left">本地存储配置</Divider>
                <Row gutter={16}>
                  <Col span={12}>
                    <Form.Item name="localStoragePath" label="存储路径">
                      <Input placeholder="如：/data/uploads" />
                    </Form.Item>
                  </Col>
                </Row>
                <Divider orientation="left">对象存储配置 (S3/MinIO)</Divider>
                <Row gutter={16}>
                  <Col span={12}>
                    <Form.Item name="s3Endpoint" label="Endpoint">
                      <Input placeholder="如：https://s3.amazonaws.com" />
                    </Form.Item>
                  </Col>
                  <Col span={6}>
                    <Form.Item name="s3Region" label="Region">
                      <Input placeholder="如：us-east-1" />
                    </Form.Item>
                  </Col>
                  <Col span={6}>
                    <Form.Item name="s3Bucket" label="Bucket">
                      <Input placeholder="存储桶名称" />
                    </Form.Item>
                  </Col>
                </Row>
                <Row gutter={16}>
                  <Col span={12}>
                    <Form.Item name="s3AccessKey" label="Access Key">
                      <Input placeholder="Access Key ID" />
                    </Form.Item>
                  </Col>
                  <Col span={12}>
                    <Form.Item name="s3SecretKey" label="Secret Key">
                      <Input.Password placeholder="Secret Access Key" />
                    </Form.Item>
                  </Col>
                </Row>
                <Form.Item>
                  <Space>
                    <Button
                      type="primary"
                      htmlType="submit"
                      icon={<SaveOutlined />}
                      loading={storageLoading}
                    >
                      保存配置
                    </Button>
                    <Button
                      icon={<TestOutlined />}
                      loading={testStorageLoading}
                      onClick={handleTestStorage}
                    >
                      测试连接
                    </Button>
                  </Space>
                </Form.Item>
              </Form>
            </Card>
          ),
        },
        {
          key: "features",
          label: (
            <span>
              <FunctionOutlined />
              功能开关
            </span>
          ),
          children: (
            <Card>
              <Alert
                message="关闭功能模块后，相关菜单和功能将被隐藏，不影响已有数据"
                type="info"
                showIcon
                style={{ marginBottom: 16 }}
              />
              <Form form={featureForm} layout="vertical" onFinish={handleFeatureSubmit}>
                <Row gutter={[16, 8]}>
                  {featureOptions.map((option) => (
                    <Col span={6} key={option.key}>
                      <Form.Item name={option.key as keyof FeatureFlags} label={option.label} valuePropName="checked">
                        <Switch checkedChildren="开启" unCheckedChildren="关闭" />
                      </Form.Item>
                    </Col>
                  ))}
                </Row>
                <Form.Item>
                  <Button
                    type="primary"
                    htmlType="submit"
                    icon={<SaveOutlined />}
                    loading={featureLoading}
                  >
                    保存设置
                  </Button>
                </Form.Item>
              </Form>
            </Card>
          ),
        },
        {
          key: "log",
          label: (
            <span>
              <FileTextOutlined />
              日志配置
            </span>
          ),
          children: (
            <Card>
              <Form form={logForm} layout="vertical" onFinish={handleLogSubmit}>
                <Row gutter={16}>
                  <Col span={6}>
                    <Form.Item name="logLevel" label="日志级别">
                      <Select
                        options={[
                          { value: "DEBUG", label: "DEBUG" },
                          { value: "INFO", label: "INFO" },
                          { value: "WARN", label: "WARN" },
                          { value: "ERROR", label: "ERROR" },
                        ]}
                      />
                    </Form.Item>
                  </Col>
                  <Col span={6}>
                    <Form.Item name="logRetentionDays" label="日志保留天数">
                      <InputNumber min={1} max={365} style={{ width: "100%" }} />
                    </Form.Item>
                  </Col>
                  <Col span={6}>
                    <Form.Item name="maxLogSize" label="最大日志大小(MB)">
                      <InputNumber min={1} max={10240} style={{ width: "100%" }} />
                    </Form.Item>
                  </Col>
                </Row>
                <Divider orientation="left">审计日志</Divider>
                <Row gutter={16}>
                  <Col span={6}>
                    <Form.Item name="enableAuditLog" label="启用审计日志" valuePropName="checked">
                      <Switch checkedChildren="开启" unCheckedChildren="关闭" />
                    </Form.Item>
                  </Col>
                  <Col span={6}>
                    <Form.Item name="auditLogRetentionDays" label="审计日志保留天数">
                      <InputNumber min={1} max={365} style={{ width: "100%" }} />
                    </Form.Item>
                  </Col>
                </Row>
                <Form.Item>
                  <Button
                    type="primary"
                    htmlType="submit"
                    icon={<SaveOutlined />}
                    loading={logLoading}
                  >
                    保存配置
                  </Button>
                </Form.Item>
              </Form>
            </Card>
          ),
        },
      ]}
    />
    <Modal
      title="测试邮件连接"
      open={testEmailModalVisible}
      onCancel={() => {
        setTestEmailModalVisible(false);
        setTestEmailAddress("");
      }}
      onOk={handleTestEmail}
      confirmLoading={testEmailLoading}
      okText="发送测试邮件"
      cancelText="取消"
    >
      <Form.Item label="收件人邮箱">
        <Input
          placeholder="请输入接收测试邮件的邮箱地址"
          value={testEmailAddress}
          onChange={(e) => setTestEmailAddress(e.target.value)}
        />
      </Form.Item>
    </Modal>
  );
};

export default OtherConfigPage;