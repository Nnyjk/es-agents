import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  Card,
  Form,
  Input,
  Select,
  Button,
  Space,
  message,
  Steps,
  Typography,
  Divider,
  Spin,
} from "antd";
import {
  ArrowLeftOutlined,
  RocketOutlined,
  AppstoreOutlined,
  SettingOutlined,
} from "@ant-design/icons";
import {
  createDeployment,
  getApplications,
  getEnvironments,
} from "@/services/deployment";
import type {
  CreateDeploymentParams,
  Application,
  Environment,
} from "@/types/deployment";

const DeploymentCreatePage: React.FC = () => {
  const navigate = useNavigate();
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);
  const [currentStep, setCurrentStep] = useState(0);
  const [applications, setApplications] = useState<Application[]>([]);
  const [environments, setEnvironments] = useState<Environment[]>([]);
  const [loadingApps, setLoadingApps] = useState(false);
  const [loadingEnvs, setLoadingEnvs] = useState(false);

  // Fetch applications
  const fetchApplications = async () => {
    setLoadingApps(true);
    try {
      const result = await getApplications({ current: 1, pageSize: 100 });
      setApplications(result.list);
    } catch {
      // Applications might not be available, allow manual input
      setApplications([]);
    }
    setLoadingApps(false);
  };

  // Fetch environments
  const fetchEnvironments = async () => {
    setLoadingEnvs(true);
    try {
      const result = await getEnvironments({ current: 1, pageSize: 100 });
      setEnvironments(result.list);
    } catch {
      // Environments might not be available, allow manual input
      setEnvironments([]);
    }
    setLoadingEnvs(false);
  };

  // Initial data fetch
  React.useEffect(() => {
    fetchApplications();
    fetchEnvironments();
  }, []);

  const handleSubmit = async (values: CreateDeploymentParams) => {
    setLoading(true);
    try {
      const result = await createDeployment(values);
      message.success("部署已创建");
      navigate(`/deployments/${result.id}`);
    } catch (error: any) {
      message.error(`创建失败: ${error.message || "未知错误"}`);
    }
    setLoading(false);
  };

  const handleNext = async () => {
    try {
      // Validate current step fields
      const fieldsToValidate =
        currentStep === 0
          ? ["applicationId", "environmentId"]
          : currentStep === 1
            ? ["version"]
            : [];

      await form.validateFields(fieldsToValidate);
      setCurrentStep(currentStep + 1);
    } catch {
      // Validation failed
    }
  };

  const handlePrev = () => {
    setCurrentStep(currentStep - 1);
  };

  const steps = [
    {
      title: "选择目标",
      icon: <AppstoreOutlined />,
    },
    {
      title: "配置版本",
      icon: <RocketOutlined />,
    },
    {
      title: "确认创建",
      icon: <SettingOutlined />,
    },
  ];

  return (
    <Card>
      <div style={{ marginBottom: 24 }}>
        <Button
          icon={<ArrowLeftOutlined />}
          onClick={() => navigate("/deployments")}
        >
          返回列表
        </Button>
      </div>

      <Typography.Title level={4} style={{ marginBottom: 24 }}>
        <RocketOutlined style={{ marginRight: 8 }} />
        创建新部署
      </Typography.Title>

      <Steps current={currentStep} items={steps} style={{ marginBottom: 32 }} />

      <Form
        form={form}
        layout="vertical"
        onFinish={handleSubmit}
        initialValues={{ triggerType: "manual" }}
      >
        {currentStep === 0 && (
          <div>
            <Typography.Title level={5}>
              <AppstoreOutlined style={{ marginRight: 8 }} />
              选择应用和环境
            </Typography.Title>
            <Divider />

            <Form.Item
              name="applicationId"
              label="应用"
              rules={[{ required: true, message: "请选择或输入应用ID" }]}
            >
              {loadingApps ? (
                <Spin size="small" />
              ) : applications.length > 0 ? (
                <Select
                  placeholder="请选择应用"
                  showSearch
                  optionFilterProp="label"
                  options={applications.map((app) => ({
                    value: app.id,
                    label: app.name,
                  }))}
                />
              ) : (
                <Input placeholder="请输入应用ID" />
              )}
            </Form.Item>

            <Form.Item
              name="environmentId"
              label="环境"
              rules={[{ required: true, message: "请选择或输入环境ID" }]}
            >
              {loadingEnvs ? (
                <Spin size="small" />
              ) : environments.length > 0 ? (
                <Select
                  placeholder="请选择环境"
                  showSearch
                  optionFilterProp="label"
                  options={environments.map((env) => ({
                    value: env.id,
                    label: env.name,
                  }))}
                />
              ) : (
                <Input placeholder="请输入环境ID" />
              )}
            </Form.Item>

            <Form.Item name="pipelineId" label="流水线（可选）">
              <Input placeholder="可选，输入流水线ID" />
            </Form.Item>
          </div>
        )}

        {currentStep === 1 && (
          <div>
            <Typography.Title level={5}>
              <RocketOutlined style={{ marginRight: 8 }} />
              配置版本信息
            </Typography.Title>
            <Divider />

            <Form.Item
              name="version"
              label="版本号"
              rules={[{ required: true, message: "请输入版本号" }]}
            >
              <Input placeholder="请输入要部署的版本号，例如：v1.0.0" />
            </Form.Item>

            <Form.Item name="triggerType" label="触发类型">
              <Select
                options={[
                  { label: "手动触发", value: "manual" },
                  { label: "自动触发", value: "auto" },
                  { label: "Webhook触发", value: "webhook" },
                  { label: "定时触发", value: "schedule" },
                ]}
              />
            </Form.Item>

            <Form.Item name="description" label="部署描述">
              <Input.TextArea rows={4} placeholder="请输入部署描述" />
            </Form.Item>
          </div>
        )}

        {currentStep === 2 && (
          <div>
            <Typography.Title level={5}>
              <SettingOutlined style={{ marginRight: 8 }} />
              确认部署配置
            </Typography.Title>
            <Divider />

            <Typography.Paragraph>
              请确认以下部署配置信息：
            </Typography.Paragraph>

            <Card size="small" style={{ background: "#fafafa" }}>
              <pre style={{ margin: 0 }}>
                {JSON.stringify(form.getFieldsValue(), null, 2)}
              </pre>
            </Card>
          </div>
        )}

        <Divider />

        <Space style={{ width: "100%", justifyContent: "space-between" }}>
          <Button disabled={currentStep === 0} onClick={handlePrev}>
            上一步
          </Button>
          <Space>
            {currentStep < steps.length - 1 ? (
              <Button type="primary" onClick={handleNext}>
                下一步
              </Button>
            ) : (
              <Button type="primary" htmlType="submit" loading={loading}>
                <RocketOutlined style={{ marginRight: 4 }} />
                开始部署
              </Button>
            )}
          </Space>
        </Space>
      </Form>
    </Card>
  );
};

export default DeploymentCreatePage;
