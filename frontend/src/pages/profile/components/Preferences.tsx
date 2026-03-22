import React, { useEffect, useState } from "react";
import {
  Card,
  Form,
  Select,
  Switch,
  Button,
  message,
  Spin,
  Divider,
} from "antd";
import {
  getMyPreferences,
  updateMyPreferences,
} from "../../../services/userProfile";
import type { UserPreferences } from "../../../types/userProfile";

const Preferences: React.FC = () => {
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    fetchPreferences();
  }, []);

  const fetchPreferences = async () => {
    try {
      setLoading(true);
      const data = await getMyPreferences();
      form.setFieldsValue(data);
    } catch {
      // 使用默认值
      form.setFieldsValue({
        theme: "light",
        language: "zh-CN",
        notificationEmail: true,
        notificationBrowser: true,
        notificationSms: false,
      });
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (values: Partial<UserPreferences>) => {
    try {
      setSubmitting(true);
      await updateMyPreferences(values);
      message.success("偏好设置已保存");

      // 应用主题
      if (values.theme) {
        applyTheme(values.theme);
      }
    } catch {
      message.error("保存失败");
    } finally {
      setSubmitting(false);
    }
  };

  const applyTheme = (theme: string) => {
    const root = document.documentElement;
    if (theme === "dark") {
      root.setAttribute("data-theme", "dark");
    } else if (theme === "system") {
      const prefersDark = window.matchMedia(
        "(prefers-color-scheme: dark)",
      ).matches;
      root.setAttribute("data-theme", prefersDark ? "dark" : "light");
    } else {
      root.removeAttribute("data-theme");
    }
  };

  const themeOptions = [
    { label: "亮色主题", value: "light" },
    { label: "暗色主题", value: "dark" },
    { label: "跟随系统", value: "system" },
  ];

  const languageOptions = [
    { label: "简体中文", value: "zh-CN" },
    { label: "English", value: "en-US" },
  ];

  if (loading) {
    return (
      <div style={{ textAlign: "center", padding: 50 }}>
        <Spin size="large" />
      </div>
    );
  }

  return (
    <div>
      <Card title="偏好设置" style={{ marginBottom: 24 }}>
        <Form
          form={form}
          layout="vertical"
          onFinish={handleSubmit}
          style={{ maxWidth: 400 }}
        >
          <Form.Item name="theme" label="界面主题">
            <Select options={themeOptions} />
          </Form.Item>
          <Form.Item name="language" label="语言">
            <Select options={languageOptions} />
          </Form.Item>
          <Form.Item>
            <Button type="primary" htmlType="submit" loading={submitting}>
              保存设置
            </Button>
          </Form.Item>
        </Form>
      </Card>

      <Card title="通知偏好" style={{ marginBottom: 24 }}>
        <Form form={form} layout="vertical" onFinish={handleSubmit}>
          <Form.Item
            name="notificationEmail"
            label="邮件通知"
            valuePropName="checked"
          >
            <Switch checkedChildren="开启" unCheckedChildren="关闭" />
          </Form.Item>
          <Form.Item
            name="notificationBrowser"
            label="浏览器通知"
            valuePropName="checked"
          >
            <Switch checkedChildren="开启" unCheckedChildren="关闭" />
          </Form.Item>
          <Form.Item
            name="notificationSms"
            label="短信通知"
            valuePropName="checked"
          >
            <Switch checkedChildren="开启" unCheckedChildren="关闭" />
          </Form.Item>
          <Form.Item>
            <Button type="primary" htmlType="submit" loading={submitting}>
              保存设置
            </Button>
          </Form.Item>
        </Form>
      </Card>

      <Divider />

      <Card title="其他设置">
        <p style={{ color: "#999" }}>更多设置功能即将上线...</p>
      </Card>
    </div>
  );
};

export default Preferences;
