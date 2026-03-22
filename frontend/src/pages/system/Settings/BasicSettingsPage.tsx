import React from "react";
import {
  Form,
  Input,
  Switch,
  Button,
  Space,
  message,
  Card,
  Row,
  Col,
  Upload,
  Image,
} from "antd";
import {
  SaveOutlined,
  UploadOutlined,
  GlobalOutlined,
  LoginOutlined,
} from "@ant-design/icons";
import type { UploadProps } from "antd";
import type { SystemBasicSettings } from "../../../types/settings";
import { updateBasicSettings } from "../../../services/settings";

interface BasicSettingsPageProps {
  data?: SystemBasicSettings;
  onUpdate: () => void;
}

const BasicSettingsPage: React.FC<BasicSettingsPageProps> = ({ data, onUpdate }) => {
  const [form] = Form.useForm();
  const [loading, setLoading] = React.useState(false);

  React.useEffect(() => {
    if (data) {
      form.setFieldsValue(data);
    }
  }, [data, form]);

  const handleSubmit = async (values: SystemBasicSettings) => {
    setLoading(true);
    try {
      await updateBasicSettings(values);
      message.success("基础信息设置保存成功");
      onUpdate();
    } catch {
      message.error("保存失败，请重试");
    } finally {
      setLoading(false);
    }
  };

  const uploadProps: UploadProps = {
    name: "file",
    showUploadList: false,
    beforeUpload: () => false,
    onChange: (info) => {
      // 处理上传逻辑
      console.log("Upload:", info);
    },
  };

  return (
    <Form
      form={form}
      layout="vertical"
      onFinish={handleSubmit}
      initialValues={data}
    >
      <Row gutter={24}>
        <Col span={24}>
          <Card
            title={
              <span>
                <GlobalOutlined style={{ marginRight: 8 }} />
                系统基础信息
              </span>
            }
            style={{ marginBottom: 16 }}
          >
            <Row gutter={16}>
              <Col span={12}>
                <Form.Item
                  name="systemName"
                  label="系统名称"
                  rules={[{ required: true, message: "请输入系统名称" }]}
                >
                  <Input placeholder="请输入系统名称" maxLength={50} />
                </Form.Item>
              </Col>
              <Col span={12}>
                <Form.Item name="copyright" label="版权信息">
                  <Input placeholder="如：Copyright © 2024 Company Name" />
                </Form.Item>
              </Col>
            </Row>
            <Row gutter={16}>
              <Col span={12}>
                <Form.Item name="systemLogo" label="系统Logo">
                  <Space>
                    {data?.systemLogo && (
                      <Image
                        src={data.systemLogo}
                        width={60}
                        height={60}
                        style={{ objectFit: "contain" }}
                      />
                    )}
                    <Upload {...uploadProps}>
                      <Button icon={<UploadOutlined />}>上传Logo</Button>
                    </Upload>
                  </Space>
                </Form.Item>
              </Col>
              <Col span={12}>
                <Form.Item name="favicon" label="Favicon">
                  <Space>
                    {data?.favicon && (
                      <Image
                        src={data.favicon}
                        width={32}
                        height={32}
                        style={{ objectFit: "contain" }}
                      />
                    )}
                    <Upload {...uploadProps}>
                      <Button icon={<UploadOutlined />}>上传Favicon</Button>
                    </Upload>
                  </Space>
                </Form.Item>
              </Col>
            </Row>
            <Form.Item name="icpNumber" label="ICP备案号">
              <Input placeholder="如：京ICP备XXXXXXXX号" />
            </Form.Item>
          </Card>
        </Col>

        <Col span={24}>
          <Card
            title={
              <span>
                <LoginOutlined style={{ marginRight: 8 }} />
                登录页配置
              </span>
            }
            style={{ marginBottom: 16 }}
          >
            <Form.Item name="loginPageBackground" label="登录页背景图">
              <Space direction="vertical">
                {data?.loginPageBackground && (
                  <Image
                    src={data.loginPageBackground}
                    width={200}
                    height={120}
                    style={{ objectFit: "cover", borderRadius: 4 }}
                  />
                )}
                <Upload {...uploadProps}>
                  <Button icon={<UploadOutlined />}>上传背景图</Button>
                </Upload>
              </Space>
            </Form.Item>
            <Form.Item name="welcomeMessage" label="欢迎语">
              <Input.TextArea
                placeholder="登录页面显示的欢迎语"
                rows={2}
                maxLength={200}
                showCount
              />
            </Form.Item>
            <Form.Item
              name="showRegisterButton"
              label="显示注册按钮"
              valuePropName="checked"
            >
              <Switch checkedChildren="显示" unCheckedChildren="隐藏" />
            </Form.Item>
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
    </Form>
  );
};

export default BasicSettingsPage;